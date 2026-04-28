# Project Context

> Background that every OpenSpec proposal, design, and spec should respect.
> Keep this concise — link out to deeper docs instead of duplicating them.

## Purpose

`maf` is a microservices-based marketplace / social-commerce platform.
A Telegram bot ingests user messages, an LLM-powered parser turns them into
structured product / order intents, and the platform fulfils those intents
through standard commerce services (catalog, cart, orders, payments,
notifications) exposed to a Next.js storefront via an API gateway.

## Tech Stack

### Backend (`backend/`)
- **Language / runtime**: Java 21
- **Framework**: Spring Boot 4.0.3 (Spring MVC, Spring Data JPA, Spring Security, Spring Cloud Gateway in `api-gateway`)
- **Build**: Maven multi-module (`backend/pom.xml` is the parent POM)
- **Persistence**: PostgreSQL 16, Flyway 11.9.1 for migrations, Hibernate / JPA, schema-per-service
- **Messaging**: Apache Kafka (KRaft mode), transactional outbox pattern in services that emit events (e.g. `telegram`, `parser`)
- **LLM**: Ollama-hosted local model exposed via the `llm` service; consumed by `parser`
- **Auth**: JWT issued by the `auth` service, validated downstream via shared secret
- **Shared code**: `backend/libs/common` (Kafka topics, Jackson config, common DTOs)
- **Lombok** is used across services
- **Code quality**: Spotless (google-java-format), Checkstyle, PMD, SpotBugs — all wired into the Maven build

### Frontend (`frontend/`)
- **Framework**: Next.js 14 (App Router) on React 18
- **Language**: TypeScript (strict)
- **Styling**: Tailwind CSS + `tailwindcss-animate`, shadcn/ui components on Radix primitives, `class-variance-authority`, `clsx`, `tailwind-merge`
- **State / data**: TanStack Query (`@tanstack/react-query`) for server state, Zustand for client state
- **Forms**: `react-hook-form` + `zod` via `@hookform/resolvers`
- **Icons**: `lucide-react`
- **Tooling**: ESLint (`eslint-config-next`), Prettier, `tsc --noEmit` for type-check

### Infrastructure (`infra/`, `backend/infrastructure/`, `backend/docker-compose.yml`)
- **Local dev**: Docker Compose orchestrates Postgres, Kafka, Ollama, all Java services, and the LLM helper
- **Cloud / IaC**: Terraform in `infra/terraform/`
- **Runtime networking**: each backend service exposes its own port; the `api-gateway` is the single public entry point

## Services (`backend/services/`)

| Service          | Port  | Responsibility                                            |
|------------------|-------|-----------------------------------------------------------|
| `api-gateway`    | 8080  | Public ingress, CORS, routing to internal services        |
| `auth`           | 8081  | Authentication, JWT issuance                              |
| `user`           | 8082  | User profile / account data                               |
| `product`        | 8083  | Product catalog                                           |
| `order`          | 8084  | Orders                                                    |
| `cart`           | 8085  | Shopping cart                                             |
| `messaging`      | 8086  | In-app messaging                                          |
| `notification`   | 8087  | Outbound notifications                                    |
| `payment`        | 8088  | Payments                                                  |
| `telegram`       | 8090  | Telegram bot ingress, emits messages via Kafka outbox     |
| `llm`            | 8091  | Wraps Ollama; provides internal-token-protected LLM API   |
| `parser`         | 8092  | Consumes Telegram messages, calls `llm`, produces intents |

## Project Conventions

### Code Style
- Java: enforced by **Spotless (google-java-format)**, Checkstyle (`backend/checkstyle.xml`), PMD, SpotBugs. Run `./mvnw spotless:apply` before committing.
- Frontend: ESLint + Prettier. Run `npm run lint:fix` and `npm run format`.
- Prefer Lombok (`@Getter`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`) over hand-rolled boilerplate.
- TypeScript: `strict` mode is on, no implicit `any`, no default exports for components unless required by Next.js routing files.

### Architecture Patterns
- **Microservices**: each service owns its database/schema; no cross-service DB joins.
- **Sync calls**: REST through the `api-gateway`; service-to-service REST is allowed for read paths but discouraged for writes.
- **Async events**: Kafka is the integration backbone for write side-effects.
- **Transactional outbox**: services that publish domain events write them to an `outbox_event` table inside the same DB transaction as the state change; a separate publisher (`OutboxPublisher`) pushes them to Kafka. New event-emitting code must follow this pattern, not direct `KafkaTemplate.send` from the request thread.
- **Shared event schema**: defined in `backend/libs/common` (`KafkaTopics`, event DTOs). New topics or event types go there.
- **Frontend domains**: `frontend/src/domains/<domain>` holds feature code; shared building blocks live under `frontend/src/shared` and `frontend/src/components` (shadcn/ui).

### Testing Strategy
- Java: JUnit 5 + Spring Boot Test; Testcontainers for integration tests that need Postgres/Kafka. Unit tests live next to the code under `src/test/java`.
- Frontend: type-check (`npm run type-check`) is the minimum bar; component / e2e tests are not yet standardized — propose them in a change before adding.
- Always keep the `verify` Maven phase green (Checkstyle + PMD + SpotBugs + tests).

### Git Workflow
- Trunk-based: feature branches off `main`, merged via PR.
- Commit messages: short imperative subject (Conventional Commits is welcome but not required).
- Do not commit `.env*`, build outputs (`target/`, `dist/`, `node_modules/`), or IDE files — see `.gitignore`.

## Domain Context

- **Telegram → Parser → Commerce flow** is the project's defining capability. Changes that touch this pipeline must consider: bot ingestion latency, Kafka topic compatibility, LLM prompt/response schema, and idempotency of downstream commerce actions.
- **Multi-tenant data per service** is enforced by `SPRING_JPA_PROPERTIES_HIBERNATE_DEFAULT_SCHEMA` in compose; respect schema isolation when adding tables.
- **Internal-only endpoints** (`llm`, outbox publishers) are guarded by `LLM_INTERNAL_TOKEN` / similar secrets — never expose them through `api-gateway`.

## Important Constraints

- **Java 21 baseline** — do not use preview features.
- **Spring Boot 4.x** — APIs may differ from the more common 3.x examples; verify against the Spring Boot 4.x docs.
- **Local-first LLM** — production must work without third-party LLM APIs. Cloud LLMs are acceptable only behind a feature flag with an Ollama fallback.
- **No secrets in repo** — all credentials come from `.env` files (gitignored) or the deployment environment.
- **Docker Compose is the source of truth for local topology** — keep `backend/docker-compose.yml` in sync when adding services or env vars.

## External Dependencies

- **PostgreSQL 16**
- **Apache Kafka** (KRaft, single-node in dev)
- **Ollama** with the model named by `LLM_OLLAMA_MODEL` (default `llama3.2:3b`)
- **Telegram Bot API** (token in `TELEGRAM_BOT_TOKEN`)
- **Node.js ≥ 20.19** for the frontend and for the OpenSpec CLI itself
