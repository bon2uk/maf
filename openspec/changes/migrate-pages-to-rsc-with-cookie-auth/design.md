## Context

The Next.js 14 frontend (`frontend/`) ships every page as a Client Component. Authentication state lives in a Zustand store with `localStorage` persistence (`frontend/src/domains/auth/infrastructure/store/auth-store.ts`), keyed `auth-storage`, plus a parallel `localStorage["access_token"]` mirror written by `setTokens` and read by `getAuthToken()` in `frontend/src/shared/lib/api-client.ts`. The JWT therefore only ever exists in the browser. Every page that needs data calls a TanStack Query hook (`useCurrentUser`, `useProducts`, `useProduct`) gated on `isAuthenticated && isHydrated`. The result is:

- First paint waits on a client-side fetch (visible skeleton flicker).
- The full page bundle ships to the browser even when the page renders mostly static content.
- The server has no way to render or guard pages; auth checks happen with a `useEffect` redirect inside `<AuthGuard>` (wrapping `(dashboard)/layout.tsx`) and a similar `useEffect` in the login page, causing brief flashes of authenticated/unauthenticated UI.

The backend contract is unchanged: `auth-service` exposes `POST /auth/login` returning `{ token, refreshToken? }`; downstream services (e.g. `user-service` `/users/me`) accept `Authorization: Bearer <jwt>`. The Next.js server can act as a confidential client that holds the cookie and forwards it on server-side fetches.

Stakeholders: frontend (primary), `auth-service` (no contract change but consumed by a new Route Handler), `api-gateway` (CORS unchanged because the browser no longer addresses it directly with credentials — it talks to Next.js, which talks to the gateway).

## Goals / Non-Goals

**Goals:**
- Eliminate `"use client"` from any page that does not need browser APIs, state, or event handlers.
- Make the JWT readable on the Next.js server so server fetches are authenticated.
- Stop rendering authenticated UI for unauthenticated users (no post-hydration flash, no `<AuthGuard>` spinner).
- Keep `react-hook-form` + `zod` forms and the products list filters interactive without regressing UX.
- Preserve the existing `domains/<domain>` layering — server fetchers and client hooks share types, DTOs, and mappers.

**Non-Goals:**
- Switching auth providers, refresh-token rotation, or OAuth — out of scope.
- Switching to Next.js 15 or React 19. We stay on `next@14.2.11` / `react@18.3.1`.
- Server-Action-based mutations. Mutations remain client-side via React Query for now (smaller blast radius).
- Any backend (`auth-service`, `api-gateway`) code changes.
- Persisting React Query cache across server/client boundary on every page (only optionally on the products list, if it materially helps).
- Adding Playwright in this change (deferred — task remains in `tasks.md` with status "deferred").
- Moving server-side pagination into the backend (today `productApi.getProducts` fetches `/products/list` and pages in-process; this stays).

## Decisions

### 1. Where the JWT lives: HTTP-only cookie set by a Next.js Route Handler

The browser never sees the token. Login form posts credentials to `POST /api/auth/login` (a Next.js Route Handler), which:

1. Calls `POST <INTERNAL_API_URL>/auth/login` with `{ email, password }`.
2. On success, calls `GET <INTERNAL_API_URL>/users/me` with `Authorization: Bearer <token>` to fetch the user payload.
3. Sets the cookie:

```
Set-Cookie: maf_session=<jwt>; HttpOnly; SameSite=Lax; Path=/; Max-Age=<ttl>
```

`Secure` is set when `process.env.NODE_ENV === "production"`. `Max-Age` defaults to one hour (matching the backend's `JWT_EXPIRATION_MS=3600000`); a follow-up may make this dynamic by decoding the JWT `exp` claim. The Route Handler returns `{ user: <plain UserDto> }`.

Logout posts to `POST /api/auth/logout` which clears the cookie (`Max-Age=0`) and returns 204.

**Alternatives considered:**
- *Keep token in `localStorage` and rehydrate on the client.* Rejected: the whole point is to authenticate server fetches.
- *Use a session store (Redis) keyed by an opaque cookie.* Rejected: requires new infra; the JWT is already a self-contained session.
- *NextAuth.js.* Rejected: adds a dependency and an auth abstraction we don't need.
- *Have the backend return both `{ token, user }` from `/auth/login` to avoid the second hop.* Preferred long-term but out of scope for this change (backend modification).

### 2. Server-side API client reads the cookie via `next/headers`

A new `frontend/src/shared/api/server.ts` exposes:

```
export async function serverFetch<T>(path: string, init?: RequestInit): Promise<T>
```

It reads the cookie with `cookies().get("maf_session")?.value`, attaches `Authorization: Bearer <jwt>` and `Accept: application/json`, points at `process.env.INTERNAL_API_URL` (the in-cluster `api-gateway` URL — `http://api-gateway:8080/api` in docker-compose; `http://localhost:8080/api` for local dev outside compose). It defaults to `cache: "no-store"`. Per-call overrides allow `next: { revalidate: N }` where appropriate.

It maps response statuses:
- `401` → throws `UnauthorizedError`
- `404` → throws `NotFoundError`
- non-2xx → throws `ApiError` (re-using the existing class in `shared/lib/api-client.ts`)

Server fetchers (`getCurrentUser`, `getProducts`, `getProduct`) live under `domains/<domain>/infrastructure/server/` and use the same mappers as the client API to produce typed domain values. They return **plain serializable objects** (see Decision 7).

### 3. Browser-side: same-origin catch-all proxy + repointed API client

`frontend/src/app/api/[...path]/route.ts` is a single catch-all Route Handler that:
- Reads the `maf_session` cookie.
- If missing, returns 401 immediately.
- Otherwise forwards the request method, headers (sans `cookie`), and body to `${INTERNAL_API_URL}/<...path>` with `Authorization: Bearer <jwt>` injected.
- Streams the backend response status and JSON body back.

`frontend/src/shared/lib/api-client.ts` is repointed:
- `API_BASE_URL = "/api"` (always; `NEXT_PUBLIC_API_BASE_URL` is now ignored or kept only for backwards compatibility).
- `getAuthToken()` is removed.
- `apiClient` no longer attaches an `Authorization` header.

So `api.get("/users/me")` → fetches same-origin `/api/users/me` → catch-all proxy → `${INTERNAL_API_URL}/users/me`. Existing callers (`authApi`, `userApi`, `productApi`) require **no changes** to their endpoint paths. The existing `skipAuth` flag (used for `/auth/login`) becomes a no-op but is kept to avoid churn.

`POST /api/auth/login` and `POST /api/auth/logout` are explicit Route Handlers; Next.js routing matches them before the catch-all.

**Alternatives considered:**
- *One Route Handler per endpoint.* Rejected: lots of boilerplate, no benefit over a catch-all.
- *Make the browser call the gateway directly with credentials.* Rejected: defeats HTTP-only cookies.

### 4. Route protection via `middleware.ts`

`frontend/src/middleware.ts` matches `/((?!_next/|favicon.ico|api/auth/|login).*)`:
- If `maf_session` is absent, redirect to `/login?next=<encoded original path>`.
- If `maf_session` is present and the path is `/login`, redirect to `/`.

The middleware does **not** verify the JWT signature; signature/expiry validation is the responsibility of backend services and surfaces as 401 responses through `serverFetch` (which gets mapped to `redirect("/login")` in page-level error boundaries).

A `MIDDLEWARE_ENFORCE` env var is **not** introduced — the migration plan staggers landing of pieces but enforcement flips on at the same commit as the cookie write. Any soft-launch is performed in dev compose by manual testing before the change is merged.

### 5. `<AuthGuard>` is removed

The `frontend/src/domains/auth/presentation/components/auth-guard.tsx` component, currently wrapping `(dashboard)/layout.tsx`, becomes redundant once middleware enforces. It is deleted; `(dashboard)/layout.tsx` becomes a Server Component. `domains/auth/index.ts` drops the export.

### 6. Per-page conversion plan

| Page                                          | Target            | Notes                                                                                                    |
|-----------------------------------------------|-------------------|----------------------------------------------------------------------------------------------------------|
| `app/(dashboard)/layout.tsx`                  | RSC               | Drop `"use client"`, drop `<AuthGuard>`. Optionally `await getCurrentUser()` and pass to a `<HydrateAuth user={...} />` client island so `Header` can use it without re-fetching. |
| `app/(dashboard)/page.tsx`                    | RSC               | `await Promise.all([getCurrentUser(), getProducts({ size: 5 })])`. Render data directly; no skeletons.   |
| `app/(dashboard)/profile/page.tsx`            | RSC               | `await getCurrentUser()`; pass to `<ProfileCard>` and `<ProfileForm>`.                                    |
| `app/(dashboard)/products/[id]/page.tsx`      | RSC               | `await getProduct(params.id)`; `notFound()` on `NotFoundError`. Pass to `<ProductForm product={...} mode="edit" />`. |
| `app/(dashboard)/products/new/page.tsx`       | RSC               | Drop `"use client"` only. Existing `<ProductForm mode="create" />` is the client boundary.               |
| `app/(dashboard)/products/page.tsx`           | RSC + client child | Read `searchParams.{search,page,size}`, fetch list server-side, render `<ProductsTable>`. Extract `<ProductsFilters>` and `<ProductsPagination>` as client components that update URL via `router.replace` / `router.push`. |
| `app/(auth)/login/page.tsx`                   | RSC               | Server checks `cookies().get("maf_session")` and `redirect(searchParams.next ?? "/")` if present. Renders `<LoginForm>` (client) otherwise. |
| `app/layout.tsx`                              | RSC (already)     | Keep `QueryProvider`. No changes.                                                                        |

### 7. RSC → Client serialization: server fetchers return plain objects

`UserEntity` and `ProductEntity` are class instances with getters (`fullName`, `formattedPrice`, `isActive`, `isAdmin`, `initials`). React Server Component → Client Component serialization requires plain serializable values, so passing class instances would either drop the getters or throw "Only plain objects... can be passed to Client Components".

Decision: **server fetchers return plain `User` / `Product` objects** (interface-shaped) with computed properties eagerly serialized:

```
type PlainUser = User & { fullName: string };  // already in the interface
toPlain(entity: UserEntity): PlainUser
```

A `toPlain` helper is added to `userMapper` and `productMapper`. Client-side consumers of `useQuery` keep receiving `UserEntity` / `ProductEntity` (no change). Components that previously called `user.fullName` / `product.formattedPrice` continue to work whether they receive an entity (getter) or a plain object (eager field), because both expose the same interface field.

`product.isActive()` and `user.isAdmin()` are method calls — these are only used in client-side query consumers today (verified: no page passes them across the RSC boundary), so no change is required for this scope. Should this change in future, the methods will need to become standalone helpers (`isActive(product)`).

### 8. React Query stays — for client-only concerns

We keep `@tanstack/react-query` for:
- Mutations on client forms (`<LoginForm>`, `<ProductForm>`, `<ProfileForm>`) so they get `useToast()` notifications and consistent error handling.
- The interactive products filtering UX, where `<ProductsFilters>` (client) reads `useSearchParams` from `next/navigation` and triggers navigation; the page-level fetch happens server-side via `searchParams`.

We do **not** introduce `HydrationBoundary` / `dehydrate` on every page — only on the products list if measured benefit warrants it (deferred follow-up).

The `QueryProvider` is updated: on `ApiError.isUnauthorized()`, call `fetch("/api/auth/logout", { method: "POST" })` and `window.location.assign("/login")`. It no longer touches the auth store directly.

### 9. `auth-store` is reduced

`auth-store` becomes a thin client store holding only:
- `user: User | null` — populated from a `<HydrateAuth user={...} />` client island rendered as a sibling at the root of `(dashboard)/layout.tsx`.
- `setUser(user)`.
- `logout()` — calls `POST /api/auth/logout`, clears `user`, then `router.replace("/login")`.

It no longer persists a token, no longer mirrors `localStorage["access_token"]`, no `isHydrated` flag, no `tokens` field. The `persist` middleware is removed.

`Session` and `AuthTokens` types remain (still used by the unused refresh path and by `executeLogin` if we keep the use-case file). `executeLogin` is rewritten to call `POST /api/auth/login` and call `setUser(response.user)`. `executeLogout` calls `POST /api/auth/logout` and `setUser(null)`.

`use-auth` no longer exposes `isAuthenticated` / `isHydrated`. Pages don't need these; client components that did (`<Header>`, the products page) get `user` directly from props or from the store.

## Risks / Trade-offs

- **Existing logged-in users get logged out on deploy** → Mitigation: release notes; pre-production tolerates it.
- **CSRF surface for the new Route Handlers** → Mitigation: `SameSite=Lax` blocks cross-site form submits; the catch-all proxy accepts JSON only and inherits the same defense. We do not add an explicit CSRF token in this change.
- **Doubled hop for browser-driven calls (browser → Next.js → gateway → service)** → Mitigation: this hop already exists conceptually; latency cost is one extra in-cluster RTT, negligible in dev compose.
- **Cookie size if the JWT grows** → Mitigation: keep claims minimal; alert if the cookie exceeds 4 KB.
- **Login is a *double-hop*** (login → /users/me) → Mitigation: accept the latency cost for this change; backend follow-up to return `{ token, user }` from `/auth/login` is tracked separately.
- **Refresh tokens become dead code** → Mitigation: explicitly out of scope; cookie TTL = JWT TTL; user re-logs in when it expires. A follow-up change will introduce a `maf_refresh` cookie.
- **`searchParams`-driven filters change the products page UX (URL gets messy)** → Mitigation: only `search`, `page`, `size` go in the URL; default values are omitted; the URL becomes shareable.
- **Class-instance vs plain-object drift** → Mitigation: TypeScript `User` / `Product` interfaces already declare computed fields (`fullName`); `toPlain` ensures parity. Linter rule (manual review for now) discourages calling instance methods (`product.isActive()`) on RSC-sourced data.
- **Test coverage around auth is thin** → Mitigation: Playwright happy-path test is **tracked but deferred** to a follow-up change.

## Migration Plan

1. Land foundations: `errors.ts`, `server.ts`, `app/api/auth/{login,logout}/route.ts`, catch-all `app/api/[...path]/route.ts`. No page changes yet. Verify in compose.
2. Land server fetchers (`getCurrentUser`, `getProducts`, `getProduct`) and `toPlain` helpers. No page changes.
3. Land `middleware.ts` enforcing the cookie. Verify cookie propagation in compose.
4. Convert `products/new/page.tsx` (zero-risk).
5. Convert `profile/page.tsx`, `products/[id]/page.tsx`, dashboard home `page.tsx`, dashboard `layout.tsx`. Delete `<AuthGuard>`.
6. Convert `products/page.tsx` to URL-driven filters.
7. Convert `login/page.tsx` and switch `<LoginForm>` to `/api/auth/login`.
8. Reduce `auth-store`; rewire `<Header>`, `QueryProvider`, `useAuth`, `executeLogin/Logout`. Delete dead code (`getAuthToken`, persist middleware, localStorage `access_token` mirror, `<AuthGuard>` import in `domains/auth/index.ts`).
9. Repoint `api-client.ts` to same-origin (`/api`).
10. Verify: `npm run type-check`, `npm run lint`. Manual smoke deferred to human follow-up.

**Rollback**: each step is reversible by reverting its commit. Steps 1–3 are additive and safe to keep even if step 4+ is rolled back.

## Open Questions

- Should the cookie's `Max-Age` be derived from the JWT `exp` claim instead of a hard-coded `JWT_EXPIRATION_MS`? (Default for this change: hard-coded; a follow-up may decode the JWT.)
- Should we co-locate server fetchers under `domains/<domain>/infrastructure/server/` or all under `shared/api/server.ts`? (Default: per-domain to mirror the existing client-side hook layout.)
- Should `executeLogin` / `executeLogout` use-case files survive, given the Route Handler now does most of the work? (Default: keep, repoint to `/api/auth/...`; deletion is a tidy-up, separate.)
