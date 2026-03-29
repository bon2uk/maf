# MAF Dashboard - Frontend

## Tech Stack

- **Next.js 14** with App Router
- **TypeScript** for type safety
- **Tailwind CSS** for styling
- **shadcn/ui** for UI components
- **TanStack Query** for server state management
- **React Hook Form + Zod** for forms and validation
- **Zustand** for client state (auth/session)

## Getting Started

### Prerequisites

- Node.js 18+ 
- npm or yarn

### Installation

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Copy environment variables
cp .env.example .env.local

# Start development server
npm run dev
```

The app will be available at [http://localhost:3000](http://localhost:3000)

### Environment Variables

```env
# Backend API Base URL
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api

# App Configuration
NEXT_PUBLIC_APP_NAME=MAF Dashboard
```

## Project Structure (DDD-Inspired)

```
src/
├── app/                          # Next.js App Router
│   ├── (auth)/                   # Auth route group (public)
│   │   └── login/
│   ├── (dashboard)/              # Dashboard route group (protected)
│   │   ├── products/
│   │   │   ├── [id]/             # Edit product
│   │   │   └── new/              # Create product
│   │   └── profile/
│   ├── layout.tsx                # Root layout
│   └── globals.css
│
├── domains/                      # Business domains (DDD)
│   ├── auth/
│   │   ├── domain/               # Domain types & entities
│   │   ├── application/          # Use cases
│   │   ├── infrastructure/       # API clients, stores
│   │   └── presentation/         # Components, hooks
│   ├── user/
│   │   ├── domain/
│   │   ├── application/
│   │   ├── infrastructure/
│   │   └── presentation/
│   └── product/
│       ├── domain/
│       ├── application/
│       ├── infrastructure/
│       └── presentation/
│
├── shared/                       # Shared utilities
│   ├── components/               # Layout components
│   ├── hooks/                    # Shared hooks
│   ├── lib/                      # Utils, API client
│   └── providers/                # React providers
│
└── components/
    └── ui/                       # shadcn/ui components
```

## Architecture Overview

### Domain Layer (`domain/`)
- **Types**: TypeScript interfaces for domain models
- **Entities**: Domain entity classes with business logic

### Application Layer (`application/`)
- **Use Cases**: Business operations (login, create product)
- **Mappers**: Transform DTOs to/from domain entities

### Infrastructure Layer (`infrastructure/`)
- **API**: HTTP clients for backend communication
- **DTOs**: Data Transfer Objects matching API contracts
- **Store**: Zustand stores for client state

### Presentation Layer (`presentation/`)
- **Components**: React components for the domain
- **Hooks**: TanStack Query hooks for data fetching
- **Forms**: Zod schemas and form components

## Key Features

### Authentication
- JWT token storage in localStorage
- Automatic token attachment to requests
- Redirect to login on 401 responses
- Protected routes with `AuthGuard`

### API Integration
- Type-safe API client with request/response typing
- Automatic error handling
- Token refresh support ready

### State Management
- **Server State**: TanStack Query for caching, fetching, mutations
- **Client State**: Zustand for auth/session only

### Forms
- React Hook Form for form state
- Zod schemas for validation
- Integrated with shadcn/ui form components

## API Endpoints Expected

The frontend expects these REST endpoints:

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | User login |
| GET | `/api/users/me` | Get current user |
| PUT | `/api/users/me` | Update current user |
| GET | `/api/products` | List products (paginated) |
| GET | `/api/products/{id}` | Get product by ID |
| POST | `/api/products` | Create product |
| PUT | `/api/products/{id}` | Update product |
| DELETE | `/api/products/{id}` | Delete product |

### Query Parameters for Products

- `page` (number): Page number (0-indexed)
- `size` (number): Page size
- `search` (string): Search term
- `category` (string): Category filter
- `status` (string): Status filter

### Response Formats

**Login Response:**
```json
{
  "access_token": "string",
  "refresh_token": "string",
  "expires_in": 3600,
  "token_type": "Bearer"
}
```

**User Response:**
```json
{
  "id": "string",
  "email": "string",
  "first_name": "string",
  "last_name": "string",
  "avatar_url": "string",
  "role": "USER|ADMIN|MANAGER",
  "created_at": "ISO8601",
  "updated_at": "ISO8601"
}
```

**Product Response:**
```json
{
  "id": "string",
  "name": "string",
  "description": "string",
  "price": 99.99,
  "category": "string",
  "status": "ACTIVE|INACTIVE|OUT_OF_STOCK",
  "stock": 100,
  "created_at": "ISO8601",
  "updated_at": "ISO8601"
}
```

**Paginated Products Response:**
```json
{
  "content": [...products],
  "total_elements": 100,
  "page": 0,
  "size": 10,
  "total_pages": 10
}
```

## Scripts

```bash
npm run dev          # Start development server
npm run build        # Build for production
npm run start        # Start production server
npm run lint         # Run ESLint
npm run type-check   # Run TypeScript compiler
```

## Design Decisions

1. **DDD-Inspired, Not Academic**: The architecture uses DDD concepts pragmatically without unnecessary abstraction.

2. **Feature/Domain Organization**: Code is organized by business domain rather than technical type for better cohesion.

3. **Separation of Concerns**: 
   - Domain logic is separate from UI
   - API DTOs are separate from domain entities
   - Business logic lives in use-cases/hooks, not in page components

4. **Type Safety**: Full TypeScript with strict mode, typed API responses, and Zod validation.

5. **Server State vs Client State**: TanStack Query handles all server data; Zustand is only used for truly local state (auth tokens, UI state).
