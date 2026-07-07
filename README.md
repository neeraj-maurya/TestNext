# TestNext — SaaS Test Automation Framework (POC)

This workspace contains a runnable POC of TestNext: a Spring Boot backend (Java 24 / Spring Boot 4.0.0) and a Vite + React UI in `ui/`.

This branch is configured for local development using an H2 file-based database for a stable POC experience.

---

## Quick Start — Backend + UI

One-click (VS Code):
- [Start Backend (VS Code Task)](command:workbench.action.tasks.runTask?%7B%22task%22%3A%22Start%20Backend%22%7D)
- [Start Frontend (VS Code Task)](command:workbench.action.tasks.runTask?%7B%22task%22%3A%22Start%20Frontend%22%7D)

*(or use the convenience scripts in `scripts/`)*

Open two PowerShell terminals in the project root:

### 1) Start the backend with the `dev` profile (file-based H2)
```powershell
# Build the JAR package (skip tests for faster build)
mvn clean package -DskipTests

# Run the JAR with dev profile
java -jar target/testnext-0.1.0-SNAPSHOT.jar --spring.profiles.active=dev
```
The backend will start on **http://localhost:8080**. The H2 database files are created under `./data/` (e.g. `data/testnext.mv.db`).

### 2) Start the UI dev server (Vite)
```powershell
cd ui
npm install
npm run dev
```
The UI runs at **http://localhost:5173** and forwards `/api` requests to the backend via the Vite proxy configured in `ui/vite.config.js`.

---

## Login & Access Reference

### Login to the Application
1. Open **http://localhost:5173** in your browser.
2. Enter your username and password on the Login page.
3. **Default admin credentials**: Username `neera` / Password `neera123`.
4. Click **"Sign In"** to proceed.

Login uses HTTP Basic Auth verified against the backend (`/api/system/users/me`). The auth token is stored in `localStorage` for the session duration.

**For real authentication**, the backend is configured with OAuth2 resource server support (currently disabled for POC — see `SecurityConfig`):
- Configure your OAuth2 provider in `application.yml`.
- System users are stored in the `system_users` table.

### Accessing the Application

| Component | URL | Notes |
|-----------|-----|-------|
| **Frontend UI** | http://localhost:5173 | React + Vite dev server |
| **Backend API** | http://localhost:8080 | Spring Boot REST API |
| **Health Check** | http://localhost:8080/actuator/health | Spring Boot actuator |
| **H2 Console** | http://localhost:8080/h2-console | Available in `dev` profile only |

### System Admin User (Pre-seeded)
The admin user is automatically created by `data-dev.sql` when the application starts (H2 dev profile) or by `data.sql` (PostgreSQL). It is safe to delete the H2 database file — the admin will be re-seeded on next startup.

| Property | Value |
|----------|-------|
| **Username** | `neera` |
| **Email** | `neera@example.com` |
| **Role** | `ROLE_SYSTEM_ADMIN` |
| **Password** | `neera123` |

---

## API Documentation & Reference

### Base URL
```
http://localhost:8080
```

### Authentication
All API endpoints require authentication via one of:

#### 1. Basic Authentication
- **Header**: `Authorization: Basic <base64_encoded_credentials>`
- **Credentials**: `username:password`

#### 2. API Key Authentication
- **Header**: `x-api-key: <your_api_key>`
- **Generation**: Via the User Profile page or `POST /api/system/users/{id}/api-key`

#### 3. User Impersonation (Dev/Test Only)
In the `dev` profile, you can impersonate any user using a special header (no password required).
- **Header**: `X-TestNext-User: <username>`

---

### Endpoints Reference

#### System Users
Requires `ROLE_SYSTEM_ADMIN`.

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/system/users` | List all system users |
| `POST` | `/api/system/users` | Create a new system user |
| `PUT` | `/api/system/users/{id}` | Update a system user |
| `DELETE` | `/api/system/users/{id}` | Delete a system user |
| `GET` | `/api/system/users/me` | Get current user profile (any authenticated user) |
| `POST` | `/api/system/users/{id}/api-key` | Generate API key for a user |

#### Tenants
Requires `ROLE_SYSTEM_ADMIN`.

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/tenants` | List all tenants |
| `POST` | `/api/tenants` | Create a new tenant |
| `PUT` | `/api/tenants/{id}` | Update a tenant |
| `DELETE` | `/api/tenants/{id}` | Delete a tenant |

#### Projects

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/tenants/{tenantId}/projects` | List projects for a tenant |
| `POST` | `/api/tenants/{tenantId}/projects` | Create a new project |

#### Test Suites

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/projects/{projectId}/suites` | List test suites in a project |
| `POST` | `/api/projects/{projectId}/suites` | Create a new test suite |
| `GET` | `/api/projects/{projectId}/suites/{suiteId}` | Get test suite details |

#### Test Cases

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/test-suites/{suiteId}/tests` | List tests in a suite |
| `POST` | `/api/test-suites/{suiteId}/tests` | Create a new test case |
| `GET` | `/api/projects/{projectId}/tests` | List all tests in a project |
| `DELETE` | `/api/tests/{testId}` | Delete a test case |

#### Step Definitions

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/test-steps-library` | List all step definitions |
| `POST` | `/api/test-steps-library` | Create a step definition |
| `GET` | `/api/tenants/{tenantId}/step-definitions` | List step definitions scoped to a tenant |

#### Executions

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/tests/{testId}/executions` | Run a single test case |
| `POST` | `/api/executions` | Run a test suite (Body: `{"suiteId": 123}`) |
| `GET` | `/api/executions` | List all executions |
| `GET` | `/api/executions/{executionId}` | Get execution details |
| `GET` | `/api/projects/{projectId}/executions` | List executions for a project |
| `DELETE` | `/api/executions/{executionId}` | Delete an execution record |

#### Actuator (Monitoring)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/actuator/health` | Application health status |
| `GET` | `/actuator/info` | Application info |
| `GET` | `/actuator/metrics` | Application metrics |

---

## Docker & CI/CD Setup

### Docker Compose (Local)
Build and run the full stack (Postgres + Redis + app):
```powershell
docker build -t testnext:local .
docker-compose up --build
```
Stop and remove containers:
```powershell
docker-compose down
```

**Note**: The Docker Compose stack uses PostgreSQL. The admin user is seeded via `data.sql` on first startup (uses `ON CONFLICT DO NOTHING` — safe to restart repeatedly).

### CI and Dependency Automation
- `/.github/workflows/ci.yml`: Main CI workflow that runs `mvn test` on PRs and pushes.
- `/.github/workflows/quality.yml`: Runs `mvn -DskipTests verify` and optional SpotBugs/Checkstyle checks.
- `/.github/dependabot.yml`: Dependabot configured to check Maven dependencies weekly.

---

## Database & Schema

- **Dev Profile Database**: H2 file-based at `jdbc:h2:file:./data/testnext;AUTO_SERVER=TRUE`
- **Schema Management**: Hibernate `ddl-auto: update` creates/updates tables automatically from JPA entities on startup.
- **Seed Data**: `data-dev.sql` (H2) and `data.sql` (PostgreSQL) seed the admin user on every startup using idempotent upsert — safe to re-run.
- **H2 Console** (dev only): http://localhost:8080/h2-console — JDBC URL: `jdbc:h2:file:./data/testnext;AUTO_SERVER=TRUE`, username `sa`, no password.

> **Note on Flyway**: Flyway is configured in `pom.xml` and `application.yml` but is currently disabled (`flyway.enabled: false`). Schema changes are handled by Hibernate auto-DDL for the POC. Flyway will be enabled when moving to a stable production schema.

---

## Development & Testing

### Run Tests
```powershell
# Run all tests
mvn clean test

# Run specific test
mvn -Dtest=DevProfileIntegrationTest test

# Run without tests (faster build)
mvn clean package -DskipTests
```

---

## Common Problems & Troubleshooting

- **Port 8080 already in use**: Add `--server.port=8081` to the java command.
- **Port 5173 already in use**: Run `npm run dev -- --port 5174` in `ui/`.
- **Database file locked**: Remove the `data/` folder and restart — the schema and admin user will be recreated automatically.
- **Admin user missing after DB reset**: Restart the app — `data-dev.sql` re-seeds the admin on every startup.
- **UI cannot reach API**: Verify the backend is running on port 8080. The Vite proxy forwards `/api` requests automatically — do not hardcode `localhost:8080` in UI code.
- **Login fails with correct credentials**: Passwords are BCrypt encoded. If you inserted a user with a plain-text password directly in SQL, it will not match. Use the API (`POST /api/system/users`) to create users.

---

## Build Information

- **Java Version**: 24.0.1
- **Spring Boot**: 4.0.0
- **Hibernate ORM**: 7.x (managed by Spring Boot parent)
- **Build Tool**: Maven 3.9.x
- **Test Framework**: JUnit Jupiter 5.x
- **Database**: PostgreSQL (production/Docker), H2 (development/testing)
- **UI Framework**: React 18.2.0, Vite 5.x, Material-UI 5.x
- **Node.js**: 22.x, npm 10.x

---

## Project Structure

```
TestNext/
├── src/
│   ├── main/
│   │   ├── java/com/testnext/          # Backend source code
│   │   └── resources/
│   │       ├── application.yml         # Base Spring Boot config (PostgreSQL)
│   │       ├── application-dev.yml     # Dev profile config (H2)
│   │       ├── data.sql                # PostgreSQL bootstrap seed (admin user)
│   │       └── data-dev.sql            # H2 bootstrap seed (admin user, dev profile)
│   └── test/
│       ├── java/com/testnext/          # Test files
│       └── resources/
│           ├── application-test.yml    # Test config
│           └── schema.sql              # Test schema init
├── ui/                                  # React frontend
│   ├── src/
│   ├── vite.config.js                  # Vite + API proxy config (/api → localhost:8080)
│   └── package.json
├── sql/                                 # SQL seed scripts
├── pom.xml                              # Maven configuration
└── README.md                            # This file
```