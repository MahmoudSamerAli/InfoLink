# InfoLink

InfoLink is an internal information search platform. Authenticated users search organizational records across group-scoped MongoDB collections, while administrators manage users, groups, and which collections each group can access. Every search is logged for auditing.

> **Status: backend in active development, frontend is a disconnected UI mock.** See [Known Limitations](#known-limitations--roadmap) before relying on this for anything beyond local backend development.

## Architecture

| Folder | Description |
| ------ | ----------- |
| `BackEnd/` | Spring Boot REST API (Java 26, Maven) |
| `FrontEnd/` | Static HTML/CSS/JS pages — currently a **UI mock**, not wired to the backend (see below) |
| `Database/` | SQL Server schema script, a `.bak` backup, and sample MongoDB collection data (CSV/JSON) |

### Backend

- **Framework:** Spring Boot 4.1 (Java 26), Spring MVC, Spring Security, springdoc-openapi (Swagger UI)
- **Relational storage (SQL Server):** users, groups, group→collection access mappings, and search logs (Spring Data JPA)
- **Document storage (MongoDB):** the searchable organizational records, stored as flat, all-string documents so search stays a simple regex match with no type-handling logic
- **Authentication:** stateless JWT issued on login, validated per request via `JwtFilter`; passwords hashed with BCrypt
- **Roles:** `USER`, `ADMIN`, `SYSADMIN` (`BackEnd/src/main/java/com/InfoLink/model/Role.java`), stored as a `TINYINT` ordinal in SQL Server
- **File upload:** admins can create an empty MongoDB collection, then upload a flat CSV or JSON array into it; uploads can repeat (append-only, no dedup)

### Frontend

Static pages in `FrontEnd/` styled with a shared dark theme (`shared.css`). **They currently run entirely on browser `localStorage`** via `data-store.js` — a self-contained mock data layer with seeded demo groups/users. There is no HTTP client calling the backend API yet (no `api.js`, no `fetch()` to `/auth`, `/users`, `/group`, or `/api/search` anywhere in this folder). Treat these pages as a UI/UX prototype, not a working client.

## Features

### Implemented (backend, verified against source)
- JWT sign-in (`POST /auth/login`), BCrypt password verification
- User management — list, get by id, add, partially update, delete (`/users/**`)
- Group management — list, add, update, delete (`/group/**`)
- Group-scoped search — a search is only allowed against collections the caller's group has been granted access to; every search is logged (user, group, collection, keyword, timestamp, IP, hit/miss)
- Collection lifecycle — create, delete, grant/revoke group access, list groups with access, and CSV/JSON data upload (`/api/collections/**`)
- Centralized request validation (Jakarta Bean Validation) and a global exception handler returning consistent error shapes
- Structured logging via SLF4J/Logback

### UI-only (not backend-connected)
- Sign-in, dashboards, user CRUD screens, group screens, search screens — all present as HTML pages, all currently reading/writing `localStorage` instead of the API

## API Endpoints

Base URL: `http://localhost:8080`. Swagger UI: `http://localhost:8080/swagger-ui/index.html`.

### Authentication

| Method | Path | Auth | Description |
| ------ | ---- | ---- | ----------- |
| `POST` | `/auth/login` | public | Authenticate, returns `{ "token": "..." }` |

### Users

| Method | Path | Auth | Description |
| ------ | ---- | ---- | ----------- |
| `GET`    | `/users` | any authenticated user¹ | List all users |
| `GET`    | `/users/{id}` | any authenticated user¹ | Get a single user |
| `POST`   | `/users/add` | any authenticated user¹ | Create a user |
| `PATCH`  | `/users/{id}` | any authenticated user¹ | Partially update a user |
| `DELETE` | `/users/delete/{id}` | any authenticated user¹ | Delete a user |

### Groups

| Method | Path | Auth | Description |
| ------ | ---- | ---- | ----------- |
| `GET`    | `/group` | any authenticated user¹ | List all groups |
| `POST`   | `/group` | any authenticated user¹ | Create a group |
| `PUT`    | `/group/{id}` | any authenticated user¹ | Update a group |
| `DELETE` | `/group/{id}` | any authenticated user¹ | Delete a group |

### Collections

| Method | Path | Auth | Description |
| ------ | ---- | ---- | ----------- |
| `POST`   | `/api/collections?name=&groupId=` | `ADMIN` / `SYSADMIN` | Create an empty MongoDB collection, optionally granting one group immediate access |
| `DELETE` | `/api/collections/{name}` | `ADMIN` / `SYSADMIN` | Delete a collection and all its group-access rows |
| `POST`   | `/api/collections/{name}/upload` | `ADMIN` / `SYSADMIN` | Upload a `.csv` or `.json` file (flat records only) into a collection |
| `POST`   | `/api/collections/{name}/groups/{groupId}` | `ADMIN` / `SYSADMIN` | Grant a group access to a collection |
| `DELETE` | `/api/collections/{name}/groups/{groupId}` | `ADMIN` / `SYSADMIN` | Revoke a group's access to a collection |
| `GET`    | `/api/collections/{name}/groups` | any authenticated user¹ | List which groups have access to a collection |

### Search

| Method | Path | Auth | Description |
| ------ | ---- | ---- | ----------- |
| `GET` | `/api/search?collection=&field=&keyword=` | any authenticated user, group-checked | Regex search a collection the caller's group can access; logged |

¹ **Known gap — see below.** These routes currently only require a valid JWT, not a specific role. There is no ownership or role check stopping a `USER` from managing other users' accounts or groups.

## Known Limitations / Roadmap

These are real gaps in the current code, not hypothetical — worth fixing before wider use:

1. **Missing role enforcement on `/users/**` and `/group/**`.** Only `/api/collections/**` writes are role-restricted in `SecurityConfig`. Any authenticated user can currently create/edit/delete any user (including changing their own role to `SYSADMIN`) or any group. Needs `.hasAnyRole("ADMIN","SYSADMIN")` added to the relevant matchers, plus a self-vs-admin ownership check inside `UserService`.
2. **Frontend isn't connected to the backend.** `FrontEnd/` runs entirely on `localStorage` (`data-store.js`). Wiring it up means adding an API client (JWT attach + error handling), replacing every `InfoLinkStore.*` call with real `fetch()` calls, and enabling CORS on the backend (`SecurityConfig` currently has none configured).
3. **No logout / token revocation.** JWTs are stateless and remain valid until natural expiry (1 hour) even after "logging out" client-side. A server-side revocation list (or short-lived tokens + refresh tokens) would close this gap.
4. **`ExcelToCsvConverter.java` is unused.** It's implemented (including zip-slip-safe extraction) but never wired into `CollectionController`, so `.xlsx`/`.xls` upload isn't actually available despite the dependency (`org.apache.poi`) and code existing.
5. **No automated tests** beyond the default Spring Boot context-load placeholder.
6. **Secrets in `application.yaml`.** Correctly `.gitignore`'d and never committed to git history, but it's plaintext on disk (Mongo URI+password, SQL Server password, JWT secret). Rotate these before any shared/production use, and keep using environment variables or a secrets manager going forward.

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
   - Currently standalone — open the HTML pages directly, or serve `FrontEnd/` from any static server. They will run against mock local data only until wired to the API (see Roadmap above).
