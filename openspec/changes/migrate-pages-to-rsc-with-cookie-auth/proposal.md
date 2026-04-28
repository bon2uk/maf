## Why

Every page under `frontend/src/app/` is currently a Client Component (`"use client"`), even when its job is mostly to render data and a few static cards. This forces all data fetching to run in the browser, ships unnecessary JavaScript, and makes the dashboard feel slow on first load. The root cause is that the JWT lives in a Zustand store backed by `localStorage` (`access_token`), so the server has no way to authenticate fetches. We want to fix the auth model and convert the pages that genuinely benefit from RSC, without breaking the interactive UX of the products list or the existing storefront flows.

## What Changes

- **BREAKING**: Move JWT storage from `localStorage` (`access_token`) and the Zustand-persisted `auth-storage` blob to an HTTP-only, Secure, SameSite cookie set by a Next.js Route Handler (`POST /api/auth/login`). Logout clears the cookie via `POST /api/auth/logout`.
- The login Route Handler **double-hops**: it forwards credentials to the backend `POST /auth/login` (which returns `{ token, refreshToken? }`), then calls `GET /users/me` with that token to obtain the user payload, sets the `maf_session` cookie, and returns `{ user }` to the client. The browser never sees the token.
- Introduce a server-side fetch helper `serverFetch<T>(...)` in `frontend/src/shared/api/server.ts` that reads the auth cookie via `next/headers`'s `cookies()` and forwards it as `Authorization: Bearer ...` to the backend through `api-gateway` at `process.env.INTERNAL_API_URL`.
- Add typed errors `UnauthorizedError`, `NotFoundError` to `frontend/src/shared/api/errors.ts`. Keep the existing `ApiError` class in `frontend/src/shared/lib/api-client.ts` (and re-export from `errors.ts`) — do not duplicate.
- Add a same-origin catch-all proxy at `frontend/src/app/api/[...path]/route.ts` that reads the cookie, attaches `Authorization`, and forwards arbitrary `GET/POST/PUT/PATCH/DELETE` to the backend gateway. This lets the existing browser API client become same-origin without one route handler per endpoint.
- Convert the following pages to async Server Components, fetching their data server-side:
  - `app/(dashboard)/page.tsx` (dashboard home)
  - `app/(dashboard)/profile/page.tsx`
  - `app/(dashboard)/products/[id]/page.tsx`
  - `app/(dashboard)/products/new/page.tsx` (already trivially convertible — no data fetch)
- Refactor `app/(dashboard)/products/page.tsx` to a Server Component that reads `searchParams` (`search`, `page`, `size`) for filters/pagination, with small client children (`<ProductsFilters>`, `<ProductsPagination>`) that update the URL via `router.replace` / `router.push`. Pagination remains computed in the Next.js process (the backend's `/products/list` returns the full list today; this is unchanged in this scope).
- Convert `app/(auth)/login/page.tsx` to a Server Component that calls `redirect("/")` when the cookie is present; the form stays a Client Component but submits to `/api/auth/login`.
- Replace browser-only `useCurrentUser` / `useProducts` / `useProduct` invocations on pages with server fetches. React Query is retained for client-driven mutations and for the products page filters re-using `useProducts` against same-origin paths.
- Add Next.js `middleware.ts` to enforce auth on dashboard routes by checking the cookie and redirecting unauthenticated users to `/login?next=<path>` (and authenticated users away from `/login`).
- Convert `app/(dashboard)/layout.tsx` to a Server Component and **remove** `frontend/src/domains/auth/presentation/components/auth-guard.tsx` (its job is now done by middleware). Update `frontend/src/domains/auth/index.ts` to drop the `AuthGuard` export.
- Reduce `auth-store` to `{ user, setUser, logout }`: remove `tokens`, `isHydrated`, the `persist` middleware, and the `localStorage("access_token")` mirror. Server hydrates the store via a `<HydrateAuth>` client island in the dashboard layout.
- Update `QueryProvider`: on `ApiError.isUnauthorized()`, call `POST /api/auth/logout` (not `clearTokens`) and `router.replace("/login")`.
- Repoint `frontend/src/shared/lib/api-client.ts` to same-origin (`/api`) and remove its `getAuthToken()` / `Authorization` header logic — the cookie is sent automatically.
- Server fetchers MUST return **plain serializable objects** that match the existing `User` / `Product` interfaces (with `fullName` / `formattedPrice` / etc. eagerly computed) so they can cross the RSC → Client Component boundary. Add `toPlain` helpers to the existing mappers; keep `UserEntity` / `ProductEntity` for client-side query consumers.

## Capabilities

### New Capabilities
- `frontend-cookie-auth`: HTTP-only-cookie-based session for the Next.js frontend, replacing the localStorage-backed Zustand auth model. Covers login/logout route handlers (with the post-login `/users/me` hop), cookie shape, middleware-based route protection, the same-origin catch-all proxy, and server-side auth helpers.
- `frontend-rsc-pages`: Server-Component-first page architecture for the dashboard. Covers which pages must be RSC, how filters/pagination are passed via `searchParams`, the plain-object data contract at the RSC → Client boundary, and client-boundary rules for interactive subtrees.

### Modified Capabilities
<!-- No existing specs in openspec/specs/ yet, so nothing is being modified. -->

## Impact

- **Affected code**:
  - `frontend/src/app/**` — all six page files, both layouts (`app/layout.tsx`, `app/(dashboard)/layout.tsx`), new `middleware.ts`, new `app/api/auth/{login,logout}/route.ts`, new `app/api/[...path]/route.ts`
  - `frontend/src/domains/auth/**` — `auth-store`, `auth-api`, `application/use-cases/login.ts`, `presentation/hooks/use-auth.ts`, `presentation/components/login-form.tsx`. **Deletes** `presentation/components/auth-guard.tsx`. Updates `domains/auth/index.ts` barrel.
  - `frontend/src/domains/user/**` — adds `infrastructure/server/user-server-api.ts`; mappers gain `toPlain`; `presentation/hooks/use-current-user.ts` no longer reads `isAuthenticated`/`isHydrated`.
  - `frontend/src/domains/product/**` — adds `infrastructure/server/product-server-api.ts`; same `toPlain` change; `presentation/hooks/use-products.ts` updated to drop `isAuthenticated`/`isHydrated` checks.
  - `frontend/src/shared/lib/api-client.ts` — base URL becomes `/api`, `getAuthToken` deleted, no `Authorization` header injection.
  - `frontend/src/shared/api/server.ts`, `frontend/src/shared/api/errors.ts` — new files.
  - `frontend/src/shared/components/header.tsx`, `frontend/src/shared/components/sidebar.tsx` — `header` updated to use plain `user` prop or simplified store; `sidebar` unchanged.
  - `frontend/src/shared/providers/query-provider.tsx` — 401 handler routes through `/api/auth/logout`.
  - `frontend/.env.example` and `.env.local` — add `INTERNAL_API_URL`; `NEXT_PUBLIC_API_BASE_URL` becomes optional (only used for the *path prefix*, no longer a host).
- **APIs**:
  - New internal Next.js routes: `POST /api/auth/login`, `POST /api/auth/logout`, catch-all `/api/[...path]`.
  - No changes to backend service contracts; `auth-service` still issues JWTs the same way, `user-service` `/users/me` is unchanged.
- **Dependencies**: No new npm dependencies in this change. Playwright (task 12.6) is **deferred** — kept as a tracked task but skipped in the apply session for this change.
- **Refresh tokens**: out of scope. The existing `authApi.refreshToken` and `Session.fromTokens` are left untouched; in practice they are no longer invoked from the new code paths. A follow-up change will move refresh-token handling to a second HTTP-only cookie.
- **Migration risk**: existing logged-in users will be logged out on deploy because their tokens live in `localStorage` and will not be migrated to cookies. Acceptable for a pre-production app; should be called out in release notes.
