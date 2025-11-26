# TestNext — SaaS Test Automation Framework (POC)

This workspace contains a runnable POC of TestNext: a Spring Boot backend (Java 24 / Spring Boot 3.5.x)
and a Vite + React UI in `ui/`.

This branch is configured for local development using an H2 file-based database for a stable POC experience.

Quick start — backend + UI
---------------------------

Open two PowerShell terminals in the project root.

1) Start the backend with the `dev` profile (file-based H2):

```powershell
# build (optional)
mvn -DskipTests package

# run with the dev profile (the -D property should appear before the goal or after mvn):
mvn -Dspring-boot.run.profiles=dev spring-boot:run
# alternative form:
# mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The backend will start on http://localhost:8080. The H2 database files are created under `./data/` (e.g. `data/testnext.mv.db`).

2) Start the UI dev server (Vite) so the UI proxies `/api` to the backend:

```powershell
cd ui
npm install
npm run dev
```

The UI runs at http://localhost:5173 and forwards `/api` requests to the backend (see `ui/vite.config.js`).

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

- Unknown lifecycle phase ".run.profiles=dev": make sure the `-D` option placement is correct. Use one of the exact example commands above.
- If Flyway fails due to an existing incompatible DB state, remove the `data/` folder and restart the app to let Flyway recreate the schema.

Next recommended steps
----------------------

1. Replace the execution adapter with a JPA-backed implementation and update migrations to store UUIDs (if you prefer UUID primary keys).
2. Wire remaining UI pages to the backend endpoints and add small seed data for the UI to demo flows.
3. Add a GitHub Actions workflow to run `mvn test` on PRs.

If you want, I can implement the JPA-backed execution repository next (update migrations + entities) or wire more UI endpoints — tell me which.