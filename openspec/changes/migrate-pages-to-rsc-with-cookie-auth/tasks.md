## 1. Foundations: env, errors, server fetch, route handlers

- [x] 1.1 Add `INTERNAL_API_URL` to `frontend/.env.example` (default `http://api-gateway:8080/api` for compose, `http://localhost:8080/api` for local) and to `frontend/.env.local`. Document both `INTERNAL_API_URL` (server) and `NEXT_PUBLIC_API_BASE_URL` (browser, now defaults to `/api`).
- [x] 1.2 Create `frontend/src/shared/api/errors.ts` defining `UnauthorizedError`, `NotFoundError`, and re-exporting the existing `ApiError` from `@/shared/lib/api-client`.
- [x] 1.3 Create `frontend/src/shared/api/server.ts` with `serverFetch<T>(path, init?)` that reads `cookies().get("maf_session")?.value`, sets `Authorization: Bearer <jwt>`, targets `process.env.INTERNAL_API_URL`, defaults `cache: "no-store"`, and maps 401/404/non-2xx to `UnauthorizedError`/`NotFoundError`/`ApiError`.
- [x] 1.4 Create `frontend/src/app/api/auth/login/route.ts` that POSTs `{ email, password }` to `${INTERNAL_API_URL}/auth/login`, then GETs `${INTERNAL_API_URL}/users/me` with the returned token, sets `maf_session` cookie, and returns `{ user: <plain user> }` (no token in body). Maps 401 → 401, /users/me failure → 502.
- [x] 1.5 Create `frontend/src/app/api/auth/logout/route.ts` that clears `maf_session` (`Max-Age=0`) and returns 204.
- [x] 1.6 Create the same-origin catch-all proxy at `frontend/src/app/api/[...path]/route.ts` that reads `maf_session`, attaches `Authorization: Bearer`, and forwards `GET/POST/PUT/PATCH/DELETE` to `${INTERNAL_API_URL}/<...path>`. Returns 401 if cookie missing.

## 2. Server-callable data fetchers

- [x] 2.1 Add `userMapper.toPlain(entity: UserEntity): User` returning a plain object that includes `fullName` as an own property.
- [x] 2.2 Add `productMapper.toPlain(entity: ProductEntity): Product` returning a plain object.
- [x] 2.3 In `frontend/src/domains/user/infrastructure/server/user-server-api.ts`, add `getCurrentUser(): Promise<User>` that calls `serverFetch<UserResponse>("/users/me")` and returns `userMapper.toPlain(userMapper.toDomain(dto))`.
- [x] 2.4 In `frontend/src/domains/product/infrastructure/server/product-server-api.ts`, add `getProducts(filters): Promise<PaginatedProducts>` and `getProduct(id): Promise<Product>` reusing the existing client-side filter/pagination logic (the backend `GET /products/list` returns the full list).

## 3. Middleware-based route protection

- [x] 3.1 Add `frontend/src/middleware.ts` with a matcher that excludes `/_next/*`, `/favicon.ico`, `/api/auth/*`. The `/login` path is matched but handled inside the middleware so it can redirect authenticated users to `/`.
- [x] 3.2 Implement: redirect to `/login?next=<encoded-path>` when `maf_session` is absent; redirect `/login` to `/` when present.

## 4. Trivial RSC conversion: products/new

- [x] 4.1 Remove `"use client"` from `frontend/src/app/(dashboard)/products/new/page.tsx`.

## 5. RSC conversion: dashboard layout + AuthGuard removal

- [x] 5.1 Convert `frontend/src/app/(dashboard)/layout.tsx` to a Server Component: drop `"use client"` and the `<AuthGuard>` wrapper.
- [x] 5.2 Add a `<HydrateAuth user={user} />` client island that calls `useAuthStore.setUser(user)` once on mount; render it in the dashboard layout after `await getCurrentUser()`.
- [x] 5.3 Delete `frontend/src/domains/auth/presentation/components/auth-guard.tsx`.
- [x] 5.4 Update `frontend/src/domains/auth/index.ts` to drop the `auth-guard` export.

## 6. RSC conversion: profile

- [x] 6.1 Convert `frontend/src/app/(dashboard)/profile/page.tsx` to an async Server Component that `await`s `getCurrentUser()` and passes the user to `<ProfileCard>` and `<ProfileForm>`.
- [x] 6.2 Map `UnauthorizedError` from `getCurrentUser()` to `redirect("/login")`.
- [x] 6.3 `<ProfileForm>` keeps using `useUpdateCurrentUser` (now hits `/api/users/me` via the same-origin proxy).

## 7. RSC conversion: product detail

- [x] 7.1 Convert `frontend/src/app/(dashboard)/products/[id]/page.tsx` to async RSC that `await`s `getProduct(params.id)` and calls `notFound()` on `NotFoundError`.
- [x] 7.2 Pass the resolved plain product to `<ProductForm product={...} mode="edit" />`.

## 8. RSC conversion: dashboard home

- [x] 8.1 Convert `frontend/src/app/(dashboard)/page.tsx` to async RSC that `await Promise.all([getCurrentUser(), getProducts({ size: 5 })])`.
- [x] 8.2 Remove all `<Skeleton>` usage from this page; render values directly.

## 9. RSC conversion: products list (URL-driven filters)

- [x] 9.1 Convert `frontend/src/app/(dashboard)/products/page.tsx` to async RSC accepting `searchParams: { search?: string; page?: string; size?: string }`. Parse / clamp values, default `size=10`, and call `getProducts(...)` server-side.
- [x] 9.2 Extract `<ProductsFiltersUrl>` (client) that reads `useSearchParams`/`usePathname`, debounces input, and calls `router.replace` to update the URL.
- [x] 9.3 Extract `<ProductsPaginationUrl>` (client) that updates `?page=` via `router.push`. Reuse the existing `<Pagination>` UI shell.
- [x] 9.4 Pass server-fetched data to `<ProductTable>` directly; remove the page-level `useState` / `useDebounce` / `useProducts`.
- [x] 9.5 Add `frontend/src/app/(dashboard)/products/loading.tsx` to handle navigation between filter states.

## 10. RSC conversion: login

- [x] 10.1 Convert `frontend/src/app/(auth)/login/page.tsx` to a Server Component that reads `cookies().get("maf_session")` and `redirect(searchParams.next ?? "/")` if present.
- [x] 10.2 `<LoginForm>` already submits via `useAuth`, which now calls `POST /api/auth/login` (via the rewritten `authApi.login`) and `router.replace(searchParams.next ?? "/")` on success.
- [x] 10.3 Remove `useEffect` redirect and `isHydrated` checks from this file.

## 11. Auth store, use-auth, and api-client cleanup

- [x] 11.1 Reduce `frontend/src/domains/auth/infrastructure/store/auth-store.ts` to `{ user, setUser, logout }`. Remove `persist`, `tokens`, `isHydrated`, `setTokens`, `clearTokens`, `getAccessToken`, `setHydrated`, `isUserAuthenticated`. `logout` calls `POST /api/auth/logout` and sets `user = null`.
- [x] 11.2 Add `frontend/src/domains/auth/presentation/components/hydrate-auth.tsx` (client island) that calls `useAuthStore.setUser(user)` once on mount.
- [x] 11.3 Rewrite `frontend/src/domains/auth/application/use-cases/login.ts`: `executeLogin(credentials)` now POSTs to `/api/auth/login` and returns the resulting user; `executeLogout()` POSTs to `/api/auth/logout`.
- [x] 11.4 Rewrite `frontend/src/domains/auth/infrastructure/api/auth-api.ts`: `login(credentials)` calls the same-origin route handler `/api/auth/login` and returns `{ user }`. The deprecated `refreshToken` flow is removed (kept only conceptually for the follow-up change).
- [x] 11.5 Update `frontend/src/domains/auth/presentation/hooks/use-auth.ts`: drop `isAuthenticated` / `isHydrated`; expose `user`, `login`, `logout`, `isLoggingIn`. Read `user` from the store. The login mutation calls the new `authApi.login` and `setUser(response.user)`.
- [x] 11.6 Update `frontend/src/shared/lib/api-client.ts`: `API_BASE_URL = "/api"`. Remove `getAuthToken()` and the `Authorization` header injection in `apiClient`. Keep the `skipAuth` flag for backward compatibility (it becomes a no-op).
- [x] 11.7 Update `frontend/src/shared/providers/query-provider.tsx`: on `ApiError.isUnauthorized()`, fire-and-forget `fetch("/api/auth/logout", { method: "POST" })` and `window.location.assign("/login")` instead of touching the auth store.
- [x] 11.8 Update `frontend/src/shared/components/header.tsx`: read `user` from `useAuthStore` (now plain object), drop `useCurrentUser`. `logout` still comes from `useAuth`.
- [x] 11.9 Update `frontend/src/domains/user/presentation/hooks/use-current-user.ts`: drop the `isAuthenticated && isHydrated` `enabled` gate. Same for `frontend/src/domains/product/presentation/hooks/use-products.ts`.

## 12. Verification

- [x] 12.1 `npm run type-check` passes.
- [x] 12.2 `npm run lint` passes (no unused imports / unreachable code from removed hooks).
- [ ] 12.3 **HUMAN FOLLOW-UP** — Manual smoke in `docker compose up`: login → dashboard → products → product detail → profile → logout. Verify no `maf_session` cookie in `document.cookie`. *(Cannot be performed by the agent.)*
- [x] 12.4 `rg '"use client"' frontend/src/app/**/page.tsx frontend/src/app/**/layout.tsx` returns no matches in the migrated files (the spec's anchor scenario).
- [ ] 12.5 **HUMAN FOLLOW-UP** — Verify in browser DevTools: dashboard / profile / product-detail HTML responses already include the data; client bundle has no `useQuery` for those queries. *(Cannot be performed by the agent.)*
- [ ] 12.6 **DEFERRED** — Add a Playwright happy-path test: login → dashboard renders user name → logout returns to /login. Tracked here, executed in a follow-up change (proposal-level no-deps decision).
- [x] 12.7 Run `openspec validate migrate-pages-to-rsc-with-cookie-auth` and confirm the change is valid.

## 13. Rollout (HUMAN FOLLOW-UP)

- [ ] 13.1 **HUMAN** — Decide PR splitting (single PR vs. several PRs along sections 1–11 boundaries).
- [ ] 13.2 **HUMAN** — Merge order in dev compose; verify cookie propagation before merging to main.
- [ ] 13.3 **HUMAN** — Update `frontend/README.md` and (optionally) `openspec/project.md` to document cookie-auth + RSC-first conventions. *(Agent can draft text on request.)*
- [ ] 13.4 **HUMAN** — Add release note: existing browser sessions are invalidated on deploy.
- [ ] 13.5 **HUMAN** — Open a follow-up change for refresh tokens (HTTP-only `maf_refresh` cookie + `/api/auth/refresh` Route Handler).
