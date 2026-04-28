## ADDED Requirements

### Requirement: Pages and dashboard layout default to Server Components

Every file under `frontend/src/app/**/page.tsx` and `frontend/src/app/**/layout.tsx` SHALL be a React Server Component (i.e., MUST NOT contain a top-level `"use client"` directive) UNLESS the file requires browser-only APIs, React state hooks, effect hooks, or event handler props at the file's top-level scope. Interactive UI MUST be encapsulated in dedicated client components imported into the server file.

#### Scenario: Pages and dashboard layout do not declare "use client"
- **WHEN** any of `app/layout.tsx`, `app/(dashboard)/layout.tsx`, `app/(dashboard)/page.tsx`, `app/(dashboard)/profile/page.tsx`, `app/(dashboard)/products/page.tsx`, `app/(dashboard)/products/new/page.tsx`, `app/(dashboard)/products/[id]/page.tsx`, `app/(auth)/login/page.tsx` is opened
- **THEN** the file MUST NOT contain a `"use client"` directive at the top
- **AND** any interactive sub-tree (forms, filters, pagination controls) MUST be implemented as separate components that themselves declare `"use client"` where needed

### Requirement: Dashboard data is fetched server-side

The dashboard home (`app/(dashboard)/page.tsx`), profile (`app/(dashboard)/profile/page.tsx`), and product detail (`app/(dashboard)/products/[id]/page.tsx`) pages SHALL fetch their primary data via `serverFetch`-backed server fetchers (`getCurrentUser`, `getProducts`, `getProduct`) and render the result without an intermediate loading skeleton inside the page itself. These pages MUST NOT call `useQuery`, `useCurrentUser`, `useProducts`, or `useProduct` at the page level.

#### Scenario: Dashboard home renders data without a client fetch
- **WHEN** an authenticated user navigates to `/`
- **THEN** the rendered HTML returned by the server contains the user's first name and the recent product counts
- **AND** no `useQuery` call is made for these values from the page's client bundle

#### Scenario: Product detail returns 404 for missing products
- **WHEN** an authenticated user navigates to `/products/<unknown-id>`
- **THEN** the page MUST call `notFound()` so Next.js renders the 404 boundary

### Requirement: Server fetchers return plain serializable objects

Server fetchers in `domains/<domain>/infrastructure/server/` SHALL return plain JavaScript objects that satisfy the existing `User` / `Product` / `PaginatedProducts` interfaces. They MUST NOT return class instances (`UserEntity`, `ProductEntity`) directly across the RSC → Client Component boundary. Computed properties declared on those interfaces (e.g. `User.fullName`) MUST be eagerly populated as own properties on the returned object via `userMapper.toPlain` / `productMapper.toPlain` helpers.

#### Scenario: getCurrentUser returns a plain object
- **WHEN** a Server Component calls `getCurrentUser()`
- **THEN** the returned value satisfies `Object.getPrototypeOf(value) === Object.prototype`
- **AND** `value.fullName` is a string equal to `<firstName> <lastName>` trimmed

#### Scenario: getProduct returns a plain object
- **WHEN** a Server Component calls `getProduct(id)`
- **THEN** the returned value satisfies `Object.getPrototypeOf(value) === Object.prototype`
- **AND** all fields of the `Product` interface are present as own properties

### Requirement: Products list filters use URL searchParams

The products list page (`app/(dashboard)/products/page.tsx`) SHALL accept `search`, `page`, and `size` from `searchParams`, fetch the filtered list server-side, and pass the data to client child components for rendering. A client component (`<ProductsFilters>`) MUST update the URL via `router.replace(...)` on user input rather than holding filter state in local `useState`. A second client component (`<ProductsPagination>`) MUST update `?page=` via `router.push`. Default values (`search=""`, `page=0`, `size=10`) MUST be omitted from the URL.

#### Scenario: Server reads filters from the URL
- **WHEN** the user navigates to `/products?search=widget&page=1`
- **THEN** the server fetches with `{ search: "widget", page: 1, size: 10 }` and renders the matching items in the initial HTML

#### Scenario: Client filter input updates the URL
- **WHEN** the user types `widget` in the search input on the products page
- **THEN** the URL transitions (debounced) to `/products?search=widget` via `router.replace`
- **AND** the page re-renders with server-fetched results, not a client-side `useQuery`

### Requirement: Login page is a Server Component that redirects authenticated users

The login page (`app/(auth)/login/page.tsx`) SHALL be a Server Component that reads the `maf_session` cookie via `cookies()` and calls `redirect(searchParams.next ?? "/")` when the cookie is present. It MUST NOT use `useEffect`, `useRouter`, or any Zustand-derived `isHydrated` flag to perform this redirect. The login form itself MAY remain a Client Component.

#### Scenario: Authenticated user lands on /login and is redirected server-side
- **WHEN** a request with `maf_session` arrives at `/login`
- **THEN** the page invokes `redirect(searchParams.next ?? "/")` before any HTML for the login form is sent

#### Scenario: Unauthenticated user sees the login form without a hydration flash
- **WHEN** a request without `maf_session` arrives at `/login`
- **THEN** the server-rendered HTML already contains the `<LoginForm>` markup (no spinner placeholder rendered first)

### Requirement: Client mutations and authenticated UI fragments survive

The migration MUST preserve interactive behavior of `<LoginForm>`, `<ProductForm>` (create and edit modes), `<ProfileForm>`, `<ProductsFilters>`, and `<ProductsPagination>`. These components MAY remain Client Components and MAY use `react-hook-form`, `zod`, and `@tanstack/react-query` mutations. They MUST submit through same-origin `/api/...` paths (catch-all proxy or explicit Route Handlers) rather than addressing `api-gateway` with explicit tokens.

#### Scenario: Product creation still works from the new product page
- **WHEN** the user submits the create form on `/products/new`
- **THEN** the request hits a same-origin path under `/api/...`
- **AND** on success the user is navigated to the products list

#### Scenario: Login form submits to /api/auth/login
- **WHEN** the user submits the login form
- **THEN** the request hits `POST /api/auth/login`
- **AND** on success the user is navigated to `searchParams.next ?? "/"`
