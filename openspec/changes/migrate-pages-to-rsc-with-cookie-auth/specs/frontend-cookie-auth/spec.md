## ADDED Requirements

### Requirement: Session cookie holds the JWT

The frontend SHALL store the user's authentication JWT in an HTTP-only cookie named `maf_session` set on the Next.js origin. The cookie MUST be marked `HttpOnly`, `SameSite=Lax`, and `Secure` (the latter MAY be omitted only when `process.env.NODE_ENV !== "production"`). The cookie's `Max-Age` SHALL match the configured JWT TTL (default 3600 seconds, i.e. one hour). The JWT MUST NOT be readable from JavaScript and MUST NOT be persisted in `localStorage`, `sessionStorage`, or any client-accessible store.

#### Scenario: Cookie is set after successful login
- **WHEN** the user submits valid credentials to `POST /api/auth/login`
- **THEN** the response sets a `Set-Cookie: maf_session=<jwt>; HttpOnly; SameSite=Lax; Path=/; Max-Age=<ttl>` header
- **AND** the response body contains a `{ user: { id, email, firstName, lastName, fullName, role, ... } }` payload, with no `token`, `accessToken`, or `refreshToken` fields

#### Scenario: Cookie is unreadable from client JavaScript
- **WHEN** a client-side script evaluates `document.cookie`
- **THEN** the `maf_session` cookie MUST NOT appear in the result

#### Scenario: Cookie is cleared on logout
- **WHEN** the user calls `POST /api/auth/logout`
- **THEN** the response sets a `Set-Cookie` header that expires `maf_session` immediately (`Max-Age=0`)
- **AND** returns HTTP 204

### Requirement: Login route handler double-hops to obtain the user

The frontend SHALL expose `POST /api/auth/login` as a Next.js Route Handler under `frontend/src/app/api/auth/login/route.ts`. The handler MUST:
1. Forward `{ email, password }` to `${INTERNAL_API_URL}/auth/login`.
2. On success, call `${INTERNAL_API_URL}/users/me` with `Authorization: Bearer <token>` to fetch the user payload.
3. Set the `maf_session` cookie with the JWT received in step 1.
4. Return `{ user: <plain user object> }` to the client.

The raw JWT MUST NEVER appear in the response body.

#### Scenario: Backend authentication failure is propagated as 401
- **WHEN** `auth-service` returns 401 for the supplied credentials
- **THEN** `POST /api/auth/login` returns 401 with body `{ error: "Invalid credentials" }`
- **AND** no `Set-Cookie` header is set

#### Scenario: Backend success completes both hops and sets cookie
- **WHEN** `auth-service` returns 200 with `{ token }` and the subsequent `GET /users/me` returns 200 with the user payload
- **THEN** `POST /api/auth/login` returns 200 with the plain user object and the `maf_session` cookie set

#### Scenario: /users/me failure is reported but does not set the cookie
- **WHEN** `auth-service` returns 200 with `{ token }` but `GET /users/me` then fails with 5xx
- **THEN** `POST /api/auth/login` returns 502 with body `{ error: "Could not load user profile" }`
- **AND** no `Set-Cookie` header is set

### Requirement: Logout route handler clears the cookie

The frontend SHALL expose `POST /api/auth/logout` under `frontend/src/app/api/auth/logout/route.ts`. The handler MUST clear the `maf_session` cookie and respond with 204. It MUST NOT contact `auth-service`.

#### Scenario: Logout clears the cookie regardless of session validity
- **WHEN** `POST /api/auth/logout` is invoked, with or without a valid `maf_session`
- **THEN** the response is 204 with `Set-Cookie: maf_session=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax`

### Requirement: Server-side fetch attaches the session token

The frontend SHALL provide a `serverFetch` helper in `frontend/src/shared/api/server.ts` that reads `maf_session` via `next/headers`'s `cookies()` and attaches `Authorization: Bearer <jwt>` to every outbound request. `serverFetch` MUST target `process.env.INTERNAL_API_URL`, MUST default to `cache: "no-store"`, MUST throw `UnauthorizedError` on a 401 response or when the cookie is missing, and MUST throw `NotFoundError` on a 404 response.

#### Scenario: Server fetch attaches the JWT
- **WHEN** a Server Component calls `serverFetch("/users/me")` while the request has a valid `maf_session` cookie
- **THEN** the outbound request to `INTERNAL_API_URL` includes header `Authorization: Bearer <jwt>`

#### Scenario: Missing cookie causes UnauthorizedError without leaving the server
- **WHEN** a Server Component calls `serverFetch("/users/me")` and `maf_session` is absent
- **THEN** `serverFetch` throws `UnauthorizedError` before performing any network request

#### Scenario: 404 surfaces as NotFoundError
- **WHEN** the backend returns 404 for the requested path
- **THEN** `serverFetch` throws `NotFoundError`

### Requirement: Browser fetches go through a same-origin catch-all proxy

Browser-originated API calls SHALL be addressed to same-origin paths under `/api/...` so the HTTP-only cookie is sent automatically. The frontend SHALL provide a catch-all Route Handler at `frontend/src/app/api/[...path]/route.ts` that reads `maf_session`, attaches `Authorization: Bearer <jwt>`, forwards the method/headers/body to `${INTERNAL_API_URL}/<...path>`, and streams the response status and body back. The `frontend/src/shared/lib/api-client.ts` browser API client SHALL use base URL `/api` and MUST NOT attach an `Authorization` header itself.

#### Scenario: Client request is forwarded with the cookie
- **WHEN** a client component triggers `api.get("/products/list")`
- **THEN** the network request targets same-origin `/api/products/list`
- **AND** the catch-all Route Handler forwards to `${INTERNAL_API_URL}/products/list` with `Authorization: Bearer <jwt>`

#### Scenario: Catch-all rejects unauthenticated requests
- **WHEN** the browser sends a request to `/api/products/list` without `maf_session`
- **THEN** the catch-all Route Handler returns 401 without contacting the backend

### Requirement: Middleware guards routes via cookie presence

A Next.js `middleware.ts` SHALL match all routes except `/_next/*`, `/favicon.ico`, `/api/auth/*`, and `/login`. For matched requests it MUST:
- Redirect to `/login?next=<encoded-original-path>` when `maf_session` is absent.
- Redirect to `/` when `maf_session` is present and the requested path is `/login`.

The middleware MUST NOT verify the JWT signature; signature/expiry validation is performed by backend services and surfaced as 401 responses by `serverFetch` or by the catch-all proxy.

#### Scenario: Unauthenticated request to a dashboard route is redirected
- **WHEN** a request without `maf_session` arrives at `/products`
- **THEN** the middleware returns a 307/308 redirect to `/login?next=%2Fproducts`

#### Scenario: Authenticated request to /login is redirected to dashboard
- **WHEN** a request with `maf_session` arrives at `/login`
- **THEN** the middleware returns a 307/308 redirect to `/`

### Requirement: Auth client store no longer persists tokens

The Zustand `auth-store` SHALL hold only `user: User | null`, `setUser`, and `logout` actions. It MUST NOT use `persist` middleware. It MUST NOT mirror any value to `localStorage` or `sessionStorage`. It MUST NOT contain `tokens`, `accessToken`, or `isHydrated` fields. The legacy `<AuthGuard>` component SHALL be removed and its export dropped from `frontend/src/domains/auth/index.ts`.

#### Scenario: Auth store has no token field
- **WHEN** any code reads `useAuthStore.getState()`
- **THEN** the returned object MUST NOT contain `tokens`, `accessToken`, `isHydrated`, or `getAccessToken`

#### Scenario: Logout clears the cookie and the local user
- **WHEN** a client component calls `useAuthStore.getState().logout()`
- **THEN** a `POST /api/auth/logout` is issued
- **AND** on a successful response, `user` is set to `null` and the user is navigated to `/login`

#### Scenario: AuthGuard component is removed
- **WHEN** the codebase is searched for the `AuthGuard` symbol
- **THEN** there are no exports or imports of `AuthGuard` in `frontend/src/`

### Requirement: 401s clear the session

Client-side query / mutation cache handlers SHALL react to a 401 response by calling `POST /api/auth/logout` and navigating to `/login`. They MUST NOT call any token-clearing helper on the auth store directly (those helpers no longer exist).

#### Scenario: 401 from React Query triggers logout flow
- **WHEN** a TanStack Query receives an `ApiError` whose `isUnauthorized()` is true
- **THEN** the query/mutation cache handler issues `POST /api/auth/logout` and navigates the browser to `/login`
