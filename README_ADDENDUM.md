Docker & CI Addendum
=====================

This addendum provides quick commands and notes for the project's Docker Compose setup and CI tools.

Docker Compose (local)
----------------------

Build and run the full stack (Postgres + Redis + app):

```powershell
docker build -t testnext:local .
docker-compose up --build
```

Stop and remove containers:

```powershell
docker-compose down
```

Notes:
- The default `dev` Spring profile still uses H2. To run the app against Postgres, set the proper `SPRING_DATASOURCE_URL` env var or update the `application.yml`/profiles.

CI and Dependency Automation
----------------------------

- `/.github/workflows/ci.yml`: main CI that runs `mvn test` on PRs and pushes.
- `/.github/workflows/quality.yml`: runs `mvn -DskipTests verify` and optional SpotBugs/Checkstyle checks.
- `/.github/dependabot.yml`: Dependabot configured to check Maven dependencies weekly and open PRs to update them.

If you want these checks to fail the build on SpotBugs/Checkstyle issues, I can add the plugin configuration to `pom.xml` and enable strict checks.
