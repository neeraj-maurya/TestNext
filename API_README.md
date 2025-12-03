# TestNext API Documentation

This document provides details on how to access the TestNext REST APIs.

## Base URL
```
http://localhost:8080
```

## Authentication
All API endpoints (except public ones, if any) require authentication.

### 1. Basic Authentication
Use standard HTTP Basic Auth with your username and password.
- **Header**: `Authorization: Basic <base64_encoded_credentials>`
- **Credentials**: `username:password` (e.g., `SysAdmin:neeraj.maurya@testnext.com`)

### 2. API Key Authentication
Use your generated API key for programmatic access.
- **Header**: `x-api-key: <your_api_key>`
- **Generation**: You can generate an API key via the User Profile page or the `/api/system/users/{id}/api-key` endpoint.

### 3. User Impersonation (Dev/Test Only)
In the `dev` profile, you can impersonate any user using a special header.
- **Header**: `X-TestNext-User: <username>` (e.g., `X-TestNext-User: SysAdmin`)

---

## Endpoints

### System Users
Manage system-level users. Requires `ROLE_SYSTEM_ADMIN`.

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/system/users` | List all system users |
| `POST` | `/api/system/users` | Create a new system user |
| `PUT` | `/api/system/users/{id}` | Update a system user |
| `DELETE` | `/api/system/users/{id}` | Delete a system user |
| `GET` | `/api/system/users/me` | Get current user profile (Any Auth User) |
| `POST` | `/api/system/users/{id}/api-key` | Generate API key for a user |

### Tenants
Manage tenants (organizations). Requires `ROLE_SYSTEM_ADMIN`.

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/tenants` | List all tenants |
| `POST` | `/api/tenants` | Create a new tenant |
| `PUT` | `/api/tenants/{id}` | Update a tenant |
| `DELETE` | `/api/tenants/{id}` | Delete a tenant |

### Projects
Manage projects within a tenant.

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/tenants/{tenantId}/projects` | List projects for a tenant |
| `POST` | `/api/tenants/{tenantId}/projects` | Create a new project |

### Test Suites
Manage test suites within a project.

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/projects/{projectId}/suites` | List test suites in a project |
| `POST` | `/api/projects/{projectId}/suites` | Create a new test suite |
| `GET` | `/api/projects/{projectId}/suites/{suiteId}` | Get test suite details |

### Test Cases
Manage test cases.

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/test-suites/{suiteId}/tests` | List tests in a suite |
| `POST` | `/api/test-suites/{suiteId}/tests` | Create a new test case |
| `GET` | `/api/projects/{projectId}/tests` | List all tests in a project |
| `DELETE` | `/api/tests/{testId}` | Delete a test case |

### Step Definitions
Manage reusable test steps.

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/tenants/{tenantId}/step-definitions` | List step definitions |
| `POST` | `/api/tenants/{tenantId}/step-definitions` | Create a step definition |

### Executions
Run tests and view results.

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/tests/{testId}/executions` | Run a single test case |
| `POST` | `/api/executions` | Run a test suite (Body: `{"suiteId": 123}`) |
| `GET` | `/api/executions` | List all executions |
| `GET` | `/api/executions/{executionId}` | Get execution details |
| `GET` | `/api/projects/{projectId}/executions` | List executions for a project |
| `DELETE` | `/api/executions/{executionId}` | Delete an execution record |
