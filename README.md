# TestNext — SaaS Test Automation Framework (POC)

This workspace contains a runnable POC of TestNext: a Spring Boot backend (Java 24 / Spring Boot 3.5.x) and a Vite + React UI in `ui/`.

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
The UI runs at **http://localhost:5173** and forwards `/api` requests to the backend (see `ui/vite.config.js`).

---

## Login & Access Reference

### Login to the Application
The current UI implementation has a **mock login** for quick testing:
1. Open **http://localhost:5173** in your browser.
2. You'll see the Login page with a pre-filled username field.
3. **Default username**: `admin` (or use `neera` for the system admin account).
4. Click **"Sign in"** to proceed.

The login currently stores the username in session state (mock implementation). A full OAuth2/JWT integration with the backend is planned for the next phase.

**For real authentication**, the backend is configured with OAuth2 resource server support:
- Configure your OAuth2 provider in `application.yml`.
- System users are stored in the `system_users` table.
- The pre-seeded admin account is: **Username: `neera`** / **Email: `neera@example.com`**.

### Accessing the Application

| Component | URL | Notes |
|-----------|-----|-------|
| **Frontend UI** | http://localhost:5173 | React + Vite dev server |
| **Backend API** | http://localhost:8080 | Spring Boot REST API |
| **API Docs** | http://localhost:8080/api/tenants | Example endpoint |
| **Health Check** | http://localhost:8080/actuator/health | Spring Boot actuator |

### System Admin User (Pre-seeded)
Use these credentials for testing system admin functions:

| Property | Value |
|----------|-------|
| **Username** | `neera` |
| **Email** | `neera@example.com` |
| **Role** | `SYSTEM_ADMIN` |
| **Password Hash** | `$2a$10$k/k/k/k/k/k/k/k/k/k/k/7eMpJSLPKMPxUX5UvUJt1QQjqaOFwu` |

*The admin user is automatically created when the application starts via the database migration `V5__create_system_admin.sql`.*

---

## API Documentation & Reference

### Base URL
```
http://localhost:8080
```

### Authentication
All API endpoints (except public ones, if any) require authentication.

#### 1. Basic Authentication
Use standard HTTP Basic Auth with your username and password.
- **Header**: `Authorization: Basic <base64_encoded_credentials>`
- **Credentials**: `username:password` (e.g., `SysAdmin:neeraj.maurya@testnext.com`)

#### 2. API Key Authentication
Use your generated API key for programmatic access.
- **Header**: `x-api-key: <your_api_key>`
- **Generation**: You can generate an API key via the User Profile page or the `/api/system/users/{id}/api-key` endpoint.

#### 3. User Impersonation (Dev/Test Only)
In the `dev` profile, you can impersonate any user using a special header.
- **Header**: `X-TestNext-User: <username>` (e.g., `X-TestNext-User: SysAdmin`)

---

### Endpoints Reference

#### System Users
Manage system-level users. Requires `ROLE_SYSTEM_ADMIN`.

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/system/users` | List all system users |
| `POST` | `/api/system/users` | Create a new system user |
| `PUT` | `/api/system/users/{id}` | Update a system user |
| `DELETE` | `/api/system/users/{id}` | Delete a system user |
| `GET` | `/api/system/users/me` | Get current user profile (Any Auth User) |
| `POST` | `/api/system/users/{id}/api-key` | Generate API key for a user |

#### Tenants
Manage tenants (organizations). Requires `ROLE_SYSTEM_ADMIN`.

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/tenants` | List all tenants |
| `POST` | `/api/tenants` | Create a new tenant |
| `PUT` | `/api/tenants/{id}` | Update a tenant |
| `DELETE` | `/api/tenants/{id}` | Delete a tenant |

#### Projects
Manage projects within a tenant.

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/tenants/{tenantId}/projects` | List projects for a tenant |
| `POST` | `/api/tenants/{tenantId}/projects` | Create a new project |

#### Test Suites
Manage test suites within a project.

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/projects/{projectId}/suites` | List test suites in a project |
| `POST` | `/api/projects/{projectId}/suites` | Create a new test suite |
| `GET` | `/api/projects/{projectId}/suites/{suiteId}` | Get test suite details |

#### Test Cases
Manage test cases.

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/test-suites/{suiteId}/tests` | List tests in a suite |
| `POST` | `/api/test-suites/{suiteId}/tests` | Create a new test case |
| `GET` | `/api/projects/{projectId}/tests` | List all tests in a project |
| `DELETE` | `/api/tests/{testId}` | Delete a test case |

#### Step Definitions
Manage reusable test steps.

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/tenants/{tenantId}/step-definitions` | List step definitions |
| `POST` | `/api/tenants/{tenantId}/step-definitions` | Create a step definition |

#### Executions
Run tests and view results.

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

**Note**: The default `dev` Spring profile still uses H2. To run the app against Postgres, set the proper `SPRING_DATASOURCE_URL` env var or update the `application.yml`/profiles.

### CI and Dependency Automation
- `/.github/workflows/ci.yml`: Main CI workflow that runs `mvn test` on PRs and pushes.
- `/.github/workflows/quality.yml`: Runs `mvn -DskipTests verify` and optional SpotBugs/Checkstyle checks.
- `/.github/dependabot.yml`: Dependabot configured to check Maven dependencies weekly and open PRs to update them.

---

## Database & Migrations

- **Dev Profile Database**: H2 file-based at `jdbc:h2:file:./data/testnext;DB_CLOSE_ON_EXIT=FALSE;AUTO_SERVER=TRUE`
- **Flyway Migrations**: Located in `src/main/resources/db/migration/` (V1 through V5)
  - V1: Core tables (tenants, projects)
  - V2: Step definitions
  - V3: Tests and test suites
  - V4: Test steps and executions
  - V5: System users and admin seeding
- **Migrations Run Automatically** when the application starts with the `dev` profile.

### Execution Persistence Adapter
To maintain compatibility while migrating to JPA-backed persistence, the code includes a thin adapter:
- `com.testnext.execution.JdbcExecutionRepository` — existing JDBC-based helper that executes SQL via JdbcTemplate.
- `com.testnext.execution.JpaExecutionRepositoryAdapter` — implements `ExecutionRepositoryI` and delegates to the JDBC helper.

This keeps `ExecutionRepositoryI` satisfied for the `ExecutionEngine` while we complete a full JPA migration. A follow-up change will replace the adapter with a pure JPA-backed implementation (and update migrations/schema to store UUIDs consistently).

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
**Test Results**: All 8 tests pass (1 integration test + 7 unit tests).

---

## Common Problems & Troubleshooting

- **Port 8080 already in use**: Change the backend port by adding `--server.port=8081` to the java command.
- **Port 5173 already in use**: Change the UI port by modifying `ui/vite.config.js` or running `npm run dev -- --port 5174`.
- **Database file locked**: Remove the `data/` folder and restart the app to let Flyway recreate the schema.
- **Flyway migration failed**: Ensure `src/main/resources/db/migration/` folder exists and contains migration files (V1...V5).
- **UI cannot reach API**: Verify the backend is running on port 8080 and check `ui/vite.config.js` proxy configuration.
- **Test failure on schema mismatch**: Run `mvn clean test` to refresh the test schema from `src/test/resources/schema.sql`.

---

## Next Recommended Steps
1. Replace the execution adapter with a JPA-backed implementation and update migrations to store UUIDs (if you prefer UUID primary keys).
2. Wire remaining UI pages to the backend endpoints and add small seed data for the UI to demo flows.
3. Add a GitHub Actions workflow to run `mvn test` on PRs.

---

## Build Information

- **Java Version**: 24.0.1
- **Spring Boot**: 4.0.0 (managed parent)
- **Hibernate ORM**: 7.1.8.Final
- **Build Tool**: Maven 3.9.11
- **Test Framework**: JUnit Jupiter 5.10.1
- **Database**: PostgreSQL (production), H2 (development/testing)
- **UI Framework**: React 18.2.0, Vite 5.4.20, Material-UI 5.14.0
- **Node.js**: 22.20.0, npm 10.9.3

---

## Project Structure

```
TestNext/
├── src/
│   ├── main/
│   │   ├── java/com/testnext/          # Backend source code (84 files)
│   │   └── resources/
│   │       ├── application.yml         # Base Spring Boot config
│   │       ├── application-dev.yml     # Dev profile config (H2)
│   │       └── db/migration/           # Flyway migrations (V1-V5)
│   └── test/
│       ├── java/com/testnext/          # Test files (8 tests)
│       └── resources/
│           ├── application-test.yml    # Test config
│           └── schema.sql              # Test schema init
├── ui/                                  # React frontend
│   ├── src/
│   ├── vite.config.js                  # Vite + API proxy config
│   └── package.json
├── sql/                                 # SQL seed scripts
├── pom.xml                              # Maven configuration
└── README.md                            # This file
```