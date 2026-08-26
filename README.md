# InfoLink

InfoLink is an internal information search platform. Authenticated users search organizational records across group-scoped MongoDB collections, while administrators manage users, groups, and which collections each group can access. Every search is logged for auditing.

> **Status: backend and frontend are wired together and functional.** See [Known Limitations](#known-limitations--roadmap) for what's still missing before wider use.

## Architecture

| Folder | Description |
| ------ | ----------- |
| `BackEnd/` | Spring Boot REST API (Java 26, Maven), which also serves the frontend as static resources |
| `BackEnd/src/main/resources/static/` | The frontend — HTML/CSS/JS pages served directly by Spring Boot and calling the API below |
| `Database/` | SQL Server schema script, a `.bak` backup, and sample MongoDB collection data (CSV/JSON) |

### Backend

- **Framework:** Spring Boot 4.1 (Java 26), Spring MVC, Spring Security, springdoc-openapi (Swagger UI)
- **Relational storage (SQL Server):** users, groups, group→collection access mappings, and search logs (Spring Data JPA)
- **Document storage (MongoDB):** the searchable organizational records, stored as flat, all-string documents so search stays a simple regex match with no type-handling logic
- **Authentication:** short-lived JWT access tokens + longer-lived, single-use rotating refresh tokens (`RefreshTokenService`), validated per request via `JwtFilter`; passwords hashed with BCrypt
- **Roles:** `USER`, `ADMIN`, `SYSADMIN` (`BackEnd/src/main/java/com/InfoLink/model/Role.java`), stored as a `TINYINT` ordinal in SQL Server, enforced per-route in `SecurityConfig`
- **CORS:** configurable via the `infolink.cors.allowed-origins` property
- **File upload:** admins can create an empty MongoDB collection, then upload a flat CSV or JSON array into it; uploads can repeat (append-only, no dedup)

### Frontend

Static pages under `BackEnd/src/main/resources/static/`, styled with a shared dark theme (`shared.css`). All API communication goes through `data-store.js` (`InfoLinkStore`), a thin client that:
- attaches the bearer access token to every request,
- transparently refreshes it on a `401` (a single in-flight refresh is shared across concurrent requests, so several requests expiring at once don't race each other into an unwanted logout),
- and revokes the refresh token server-side (`POST /auth/logout`) on sign-out, in addition to clearing local session state.

Recent frontend fixes: live password-strength and confirm-password-match validation on the Add User page, pagination button styling on the Logs page (was missing `.page-btn`/`.pagination` CSS), and the refresh-token/logout hardening described above.

## Features

### Implemented (verified against source)
- JWT sign-in with refresh-token rotation (`POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout`), BCrypt password verification
- User management — list (paginated, filterable by keyword/role/group/active), get by id, add, partially update, change password, delete, current-user profile (`/users/**`)
- Group management — list (paginated, filterable), add, update, delete (`/group/**`)
- Group-scoped search — a search is only allowed against collections the caller's group has been granted access to; every search is logged (user, group, collection, keyword, timestamp, IP, hit/miss); `ADMIN`/`SYSADMIN` also get a cross-collection "deep search" (`/api/search/**`)
- Log retrieval — paginated log listing/search, current-user log history and counters (`/logs/**`)
- Collection lifecycle — create, delete, grant/revoke group access, list groups with access, and CSV/JSON data upload (`/api/collections/**`)
- Role-based route authorization in `SecurityConfig` (see the endpoint tables below for who can call what)
- Centralized request validation (Jakarta Bean Validation) and a global exception handler returning consistent error shapes
- Structured logging via SLF4J/Logback

## API Endpoints

Base URL: `http://localhost:8080`. Swagger UI: `http://localhost:8080/swagger-ui/index.html`.

### Authentication

| Method | Path | Auth | Description |
| ------ | ---- | ---- | ----------- |
| `POST` | `/auth/login` | public | Authenticate, returns `{ accessToken, refreshToken }` |
| `POST` | `/auth/refresh` | public (valid refresh token required) | Rotates a refresh token, returns a new `{ accessToken, refreshToken }` pair. The old refresh token is deleted on use — it's single-use. Also rejected if unused for 30+ minutes. |
| `POST` | `/auth/logout` | public (refresh token in body) | Deletes/revokes the given refresh token server-side |

### Users

| Method | Path | Auth | Description |
| ------ | ---- | ---- | ----------- |
| `GET`    | `/users` | `ADMIN` / `SYSADMIN` | List users — paginated, filterable by `keyword`, `role`, `groupId`, `active` |
| `GET`    | `/users/{id}` | `ADMIN` / `SYSADMIN` | Get a single user |
| `POST`   | `/users/add` | `ADMIN` / `SYSADMIN` | Create a user |
| `PATCH`  | `/users/{id}` | `ADMIN` / `SYSADMIN` | Partially update a user |
| `DELETE` | `/users/delete/{id}` | `ADMIN` / `SYSADMIN` | Delete a user |
| `GET`    | `/users/profile` | any authenticated user | Get the calling user's own profile |
| `POST`   | `/users/change-password` | any authenticated user | Change the calling user's own password |

### Groups

| Method | Path | Auth | Description |
| ------ | ---- | ---- | ----------- |
| `GET`    | `/group` | `ADMIN` / `SYSADMIN` | List groups — paginated, filterable by `keyword`, `active` |
| `POST`   | `/group` | `ADMIN` / `SYSADMIN` | Create a group |
| `PUT`    | `/group/{id}` | `ADMIN` / `SYSADMIN` | Update a group |
| `DELETE` | `/group/{id}` | `ADMIN` / `SYSADMIN` | Delete a group |

### Collections

| Method | Path | Auth | Description |
| ------ | ---- | ---- | ----------- |
| `GET`    | `/api/collections?groupId=` | any authenticated user | List collections a given group can access |
| `GET`    | `/api/collections/all` | `ADMIN` / `SYSADMIN` | List every collection |
| `POST`   | `/api/collections?name=&groupId=` | `ADMIN` / `SYSADMIN` | Create an empty MongoDB collection, optionally granting one group immediate access |
| `DELETE` | `/api/collections/{name}` | `ADMIN` / `SYSADMIN` | Delete a collection and all its group-access rows |
| `POST`   | `/api/collections/{name}/upload` | `ADMIN` / `SYSADMIN` | Upload a `.csv` or `.json` file (flat records only) into a collection |
| `POST`   | `/api/collections/{name}/groups/{groupId}` | `ADMIN` / `SYSADMIN` | Grant a group access to a collection |
| `DELETE` | `/api/collections/{name}/groups/{groupId}` | `ADMIN` / `SYSADMIN` | Revoke a group's access to a collection |
| `GET`    | `/api/collections/{name}/groups` | `ADMIN` / `SYSADMIN` | List which groups have access to a collection |

### Search

| Method | Path | Auth | Description |
| ------ | ---- | ---- | ----------- |
| `GET`  | `/api/search?collection=&field=&keyword=&page=&size=` | any authenticated user, group-checked | Regex search a collection the caller's group can access; logged |
| `GET`  | `/api/search/fields` / `/api/search/fields/{collectionName}` | any authenticated user | List searchable fields (overall, or for one collection) |
| `POST` | `/api/search/deep` | `ADMIN` / `SYSADMIN` | Cross-collection search, bypassing group access checks |
| `GET`  | `/api/search/deep/fields` | `ADMIN` / `SYSADMIN` | List fields available for deep search |

### Logs

| Method | Path | Auth | Description |
| ------ | ---- | ---- | ----------- |
| `GET` | `/logs` | `ADMIN` / `SYSADMIN` | List all logs — paginated |
| `GET` | `/logs/search?keyword=` | `ADMIN` / `SYSADMIN` | Search logs by keyword — paginated |
| `GET` | `/logs/today/count` | `ADMIN` / `SYSADMIN` | Count of all searches logged today |
| `GET` | `/logs/me` | any authenticated user | The calling user's own log history — paginated |
| `GET` | `/logs/me/today/count` | any authenticated user | The calling user's search count for today |
| `GET` | `/logs/me/success/count` | any authenticated user | The calling user's successful-search count |

## Known Limitations / Roadmap

These are real gaps in the current code, not hypothetical — worth fixing before wider use:

1. **`ExcelToCsvConverter.java` is unused.** It's implemented (including zip-slip-safe extraction) but never wired into `CollectionController`, so `.xlsx`/`.xls` upload isn't actually available despite the dependency (`org.apache.poi`) and code existing.
2. **No automated tests** beyond the default Spring Boot context-load placeholder.
3. **Secrets in `application.yaml`.** Correctly `.gitignore`'d and never committed to git history, but it's plaintext on disk (Mongo URI+password, SQL Server password, JWT secret). Rotate these before any shared/production use, and keep using environment variables or a secrets manager going forward.
4. **Refresh tokens live in `sessionStorage`.** Simpler than a cookie-based setup, but readable by any JS on the page — an XSS bug anywhere in the frontend could exfiltrate a live refresh token. Moving to an `HttpOnly` cookie for the refresh token would close this off, at the cost of more backend/CORS plumbing.

## Data Model

- `User` — account: username, BCrypt password hash, full name, role (`USER`/`ADMIN`/`SYSADMIN`, stored as ordinal), group, active flag, creation timestamp (auto-set via `@CreationTimestamp`)
- `Groups` — department/team: name, description, active flag
- `GroupsCollections` — which MongoDB collection names a group is allowed to search
- `Log` — one row per search: user, collection, keyword, timestamp, IP, hit/miss status
- `Role` — `USER`, `ADMIN`, `SYSADMIN`

## Setup & Prerequisites

1. **Databases**
   - SQL Server — run `Database/InfoLink_Database.sql` to create `Groups`, `Group_Collections`, `Users`, `Logs`.
   - MongoDB — the searchable collections are created via the API (`POST /api/collections`), not manually. Sample data is available under `Database/MongoDB/` if you want to seed a collection via the upload endpoint.

2. **Backend configuration**
   - Create `BackEnd/src/main/resources/application.yaml` (gitignored — not shipped in the repo) with your SQL Server datasource, MongoDB URI, and `JWT_SECRET` (base64-encoded). See `BackEnd/src/main/java/com/InfoLink/.env.example` for the required variable name.
   - Run: `./mvnw spring-boot:run` (from `BackEnd/`)
   - Swagger UI: `http://localhost:8080/swagger-ui/index.html` — click **Authorize** and paste a JWT from `POST /auth/login` to test protected endpoints.

3. **Frontend**
   - Served automatically by the backend as static resources — once `spring-boot:run` is up, open `http://localhost:8080/login.html` and sign in. No separate build or server needed.
