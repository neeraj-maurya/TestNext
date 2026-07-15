---
type: onboarding
title: TestNext Development Rules & Onboarding Context
description: Core repository context, commands, architecture, and guidelines for developer agents to minimize context setup time and token consumption.
tags: [onboarding, guidelines, rules, spring-boot, react, maven]
version: 1.1.0
---

# TestNext Developer Context & Rules

## Project Context
- **Product**: TestNext — SaaS Test Automation Framework (POC).
- **Backend Stack**: Java 24, Spring Boot 4.0.0, Maven 3.9.x, H2 (dev/test profile), PostgreSQL (production/Docker Compose).
- **Frontend Stack**: React 18.2.0, Vite 5.x, Material-UI 5.x.
- **Schema Management**: Hibernate `ddl-auto: update` (NOT Flyway — Flyway is disabled). Schema is created automatically from JPA entities on startup.
- **Seed Data**: `data-h2.sql` (H2) and `data-postgres.sql` (PostgreSQL) seed the bootstrap admin user on every startup using idempotent upsert. No manual seeding required.
- **Pre-seeded Admin**: Username `neera` / Password `neera123` / `ROLE_SYSTEM_ADMIN`.

---

## Workspace Layout & Key Paths
- **Backend Java Code**: [src/main/java/com/testnext/](file:///d:/Workspace/TestNext/src/main/java/com/testnext/)
- **Seed Files**: [src/main/resources/data-postgres.sql](file:///d:/Workspace/TestNext/src/main/resources/data-postgres.sql) (PostgreSQL), [src/main/resources/data-h2.sql](file:///d:/Workspace/TestNext/src/main/resources/data-h2.sql) (H2)
- **Frontend App**: [ui/](file:///d:/Workspace/TestNext/ui/) — Vite + React proxying `/api` → `http://localhost:8080` via `ui/vite.config.js`
- **Docker Compose**: [docker-compose.yml](file:///d:/Workspace/TestNext/docker-compose.yml) and [Dockerfile](file:///d:/Workspace/TestNext/Dockerfile)

---

## Critical Commands

### Backend
- **Build Package**: `mvn clean package -DskipTests` (fast build)
- **Run (Dev profile)**: `java -jar target/testnext-0.1.0-SNAPSHOT.jar --spring.profiles.active=dev`
  - Starts H2 dev DB on port 8080, seeds admin user via `data-h2.sql`
- **Run Tests**: `mvn clean test`

### Frontend (UI)
- **Install deps**: `npm install` (in `ui/` directory)
- **Run Dev Server**: `npm run dev` (in `ui/` directory, starts on port 5173)

---

## Roles & Permissions
There are four system roles. All roles are stored with the `ROLE_` prefix in the DB:

| Role | Constant | Access Level |
|---|---|---|
| System Admin | `ROLE_SYSTEM_ADMIN` | Full access — tenants, users, all projects |
| Test Manager | `ROLE_TEST_MANAGER` | Tenant-scoped — manages projects, suites, users within their tenant |
| Test Engineer | `ROLE_TEST_ENGINEER` | Tenant-scoped — creates/runs tests |
| Viewer | `ROLE_VIEWER` | Read-only access within their tenant |

---

## Authentication & Security

### How Auth Works (Dev Profile)
Authentication is handled by `DevAuthFilter` (runs on every request, before Spring Security's standard filters). It supports three mechanisms in priority order:

1. **`X-TestNext-User` header** — Impersonates the named user. No password required. Useful for integration tests and Postman.
2. **`x-api-key` header** — Looks up user by API key stored in `system_users.api_key`.
3. **`Authorization: Basic ...`** — Decodes Base64 credentials, looks up user by username, verifies password with **BCrypt** (`passwordEncoder.matches(raw, hashed)`).

### Password Rules
- Passwords are stored as **BCrypt hashes** in `system_users.hashed_password`.
- `SystemUserService.create()` always encodes the provided password with `BCryptPasswordEncoder` before saving.
- `SystemUserService.update()` also BCrypt-encodes if a new password is provided.
- Never store or compare plain-text passwords.

### Security Config Note
`SecurityConfig` currently permits all `/api/**` requests unconditionally (POC shortcut). Role enforcement is done via `@PreAuthorize` annotations on individual controllers and the `DevAuthFilter` setting the `SecurityContext`. OAuth2 resource server is configured but commented out — it will be enabled when moving beyond POC.

---

## Development & Coding Standards

### General Rules
1. **No Placeholders**: Do not write mock UI state or logic placeholders. Use empty arrays `[]` and visible error states when data is unavailable.
2. **Docs & Comments**: Maintain existing code comments and Javadocs. Update or add detailed documentation when updating functionality.
3. **Verification**: Always compile/build and verify changes by running tests via Maven before finalizing.
4. **Logging**: Use SLF4J (`log.debug/info/warn/error`) everywhere. Never use `System.out.println`.

### Backend/Database Patterns
1. **Schema Changes**: Add fields to JPA entities. Hibernate `ddl-auto: update` adds new columns automatically. For renames/deletes, a manual migration step is needed.
2. **Flyway**: Disabled (`flyway.enabled: false`). Do not re-enable without a full migration strategy.
3. **Seed Data**: Put bootstrap data in `data-postgres.sql` (Postgres) and `data-h2.sql` (H2). Use `ON CONFLICT DO NOTHING` (Postgres) or `MERGE INTO ... KEY(...)` (H2) to keep seeds idempotent.
4. **Database Profiles**: Active profile for local dev is `dev` (H2). Docker Compose / production uses PostgreSQL (base `application.yml`).
5. **Execution Service**: The active service is `com.testnext.service.ExecutionService` (`@Service`). It uses an in-process `ExecutorService` thread pool for async test runs.
6. **Job Queue**: `com.testnext.queue.InMemoryJobQueue` is the active implementation. `RedisJobQueue` exists as a future-production stub — it throws `UnsupportedOperationException` and must not be used in the dev path.

### Frontend/UI Patterns
1. **Design System**: Use Material-UI (MUI) components throughout.
2. **API Calls**: Always use the `useApi` hook from `ui/src/hooks/useApi.js`. Use relative paths (e.g. `/api/system/users`) — never hardcode `http://localhost:8080`. The Vite proxy forwards all `/api` requests to the backend.
3. **Auth State**: Logged-in user, role, and auth header are stored in `localStorage` (keys: `currentUser`, `currentRole`, `authHeader`).
4. **Error Handling**: On API failure, show an error message to the user. Do not fall back to mock/hardcoded data silently.

### Plugin System (Future Use)
`com.testnext.plugin.PluginManager` can load external `StepExecutor` implementations from JARs in a plugins directory using `ServiceLoader`. It is not currently wired as a Spring bean — this is intentional for the POC. Do not remove it.

---

## Context Loading & Token Optimization
- Coding agents automatically load this file (`CLAUDE.md`) as system context at startup.
- Read this file alongside [README.md](file:///c:/Users/neera/VSCodeProjects/TestNext/README.md) to understand dependencies, endpoints, and setup.
