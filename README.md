# TestNext — SaaS Test Automation Framework (POC)

This workspace contains a runnable POC of TestNext: a Spring Boot backend (Java 24 / Spring Boot 3.5.x)
and a Vite + React UI in `ui/`.

This branch is configured for local development using an H2 file-based database for a stable POC experience.

Quick start — backend + UI
---------------------------

Open two PowerShell terminals in the project root.

1) Start the backend with the `dev` profile (file-based H2):

```powershell
# Build the JAR package (skip tests for faster build)
mvn clean package -DskipTests

# Run the JAR with dev profile
java -jar target/testnext-0.1.0-SNAPSHOT.jar --spring.profiles.active=dev
```

The backend will start on **http://localhost:8080**. The H2 database files are created under `./data/` (e.g. `data/testnext.mv.db`).

2) Start the UI dev server (Vite) in a separate terminal:

```powershell
cd ui
npm install
npm run dev
```

The UI runs at **http://localhost:5173** and forwards `/api` requests to the backend (see `ui/vite.config.js`).

### Login to the Application

The current UI implementation has a **mock login** for quick testing:

1. Open http://localhost:5173 in your browser
2. You'll see the Login page with a pre-filled username field
3. **Default username**: `admin` (or use `neera` for the system admin account)
4. Click **"Sign in"** to proceed

The login currently stores the username in session state (mock implementation). A full OAuth2/JWT integration with the backend is planned for next phase.

**For real authentication**, the backend is configured with OAuth2 resource server support:
- Configure your OAuth2 provider in `application.yml`
- System users are stored in the `system_users` table
- The pre-seeded admin account is: **Username: `neera`** / **Email: `neera@example.com`**

### Accessing the Application

| Component | URL | Notes |
|-----------|-----|-------|
| **Frontend UI** | http://localhost:5173 | React + Vite dev server |
| **Backend API** | http://localhost:8080 | Spring Boot REST API |
| **API Docs** | http://localhost:8080/api/tenants | Example endpoint |
| **Health Check** | http://localhost:8080/actuator/health | Spring Boot actuator |

### System Admin User (Pre-seeded)

Use these credentials for testing the system admin functions:

| Property | Value |
|----------|-------|
| **Username** | `neera` |
| **Email** | `neera@example.com` |
| **Role** | `SYSTEM_ADMIN` |
| **Password Hash** | `$2a$10$k/k/k/k/k/k/k/k/k/k/k/7eMpJSLPKMPxUX5UvUJt1QQjqaOFwu` |

The admin user is automatically created when the application starts via the database migration `V5__create_system_admin.sql`.

### API Endpoints Reference

**Tenants**
- `GET /api/tenants` - List all tenants
- `POST /api/tenants` - Create new tenant (requires SYSTEM_ADMIN role)
- Request body: `{"name": "tenant-name"}`

**Projects**
- `GET /api/projects` - List all projects
- `POST /api/projects` - Create new project (requires SYSTEM_ADMIN role)
- Request body: `{"name": "project-name", "tenantId": 1}`

**System Users**
- `GET /api/system/users/me` - Get current authenticated user info
- `GET /api/system/users` - List all system users (SYSTEM_ADMIN only)

**Actuator (Monitoring)**
- `GET /actuator/health` - Application health status
- `GET /actuator/info` - Application info
- `GET /actuator/metrics` - Application metrics

Notes about the database
------------------------

- Dev profile now uses H2 file DB: `jdbc:h2:file:./data/testnext;DB_CLOSE_ON_EXIT=FALSE;AUTO_SERVER=TRUE`.
- Flyway migrations are under `src/main/resources/db/migration` and run automatically when the app starts with the `dev` profile.

Execution persistence adapter
-----------------------------

To maintain compatibility while migrating to JPA-backed persistence, the code includes a thin adapter:

- `com.testnext.execution.JdbcExecutionRepository` — existing JDBC-based helper that executes SQL via JdbcTemplate.
- `com.testnext.execution.JpaExecutionRepositoryAdapter` — implements `ExecutionRepositoryI` and delegates to the JDBC helper.

This keeps `ExecutionRepositoryI` satisfied for the `ExecutionEngine` while we complete a full JPA migration. A follow-up change
will replace the adapter with a pure JPA-backed implementation (and update migrations/schema to store UUIDs consistently).

Common problems & troubleshooting
---------------------------------

- **Port 8080 already in use**: Change the backend port by adding `--server.port=8081` to the java command
- **Port 5173 already in use**: Change the UI port by modifying `ui/vite.config.js` or running `npm run dev -- --port 5174`
- **Database file locked**: Remove the `data/` folder and restart the app to let Flyway recreate the schema
- **Flyway migration failed**: Ensure `src/main/resources/db/migration/` folder exists and contains migration files (V1...V5)
- **UI cannot reach API**: Verify the backend is running on port 8080 and check `ui/vite.config.js` proxy configuration
- **Test failure on schema mismatch**: Run `mvn clean test` to refresh the test schema from `src/test/resources/schema.sql`

Database & Migrations
---------------------

- **Dev profile database**: H2 file-based at `jdbc:h2:file:./data/testnext;DB_CLOSE_ON_EXIT=FALSE;AUTO_SERVER=TRUE`
- **Flyway migrations**: Located in `src/main/resources/db/migration/` (V1 through V5)
  - V1: Core tables (tenants, projects)
  - V2: Step definitions
  - V3: Tests and test suites
  - V4: Test steps and executions
  - V5: System users and admin seeding
- **Migrations run automatically** when the app starts with the `dev` profile

Next recommended steps
----------------------

1. Replace the execution adapter with a JPA-backed implementation and update migrations to store UUIDs (if you prefer UUID primary keys).
2. Wire remaining UI pages to the backend endpoints and add small seed data for the UI to demo flows.
3. Add a GitHub Actions workflow to run `mvn test` on PRs.

If you want, I can implement the JPA-backed execution repository next (update migrations + entities) or wire more UI endpoints — tell me which.

Development & Testing
---------------------

### Run Tests

```powershell
# Run all tests
mvn clean test

# Run specific test
mvn -Dtest=DevProfileIntegrationTest test

# Run without tests (faster build)
mvn clean package -DskipTests
```

**Test Results**: All 8 tests pass (1 integration test + 7 unit tests)

### Build Information

- **Java Version**: 24.0.1
- **Spring Boot**: 4.0.0 (managed parent)
- **Hibernate ORM**: 7.1.8.Final
- **Build Tool**: Maven 3.9.11
- **Test Framework**: JUnit Jupiter 5.10.1
- **Database**: PostgreSQL (production), H2 (development/testing)
- **UI Framework**: React 18.2.0, Vite 5.4.20, Material-UI 5.14.0
- **Node.js**: 22.20.0, npm 10.9.3

### Project Structure

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