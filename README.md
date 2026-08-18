# InfoLink

InfoLink is an internal information search platform. Authorized users sign in and search organizational records across group-scoped MongoDB collections, while administrators manage users and their access groups. Every search is logged for auditing.

## Architecture

| Folder | Description |
| ------ | ----------- |
| `BackEnd/` | Spring Boot REST API (Java, Maven) |
| `FrontEnd/infolink-ui/` | Static HTML / CSS / JavaScript admin & user interfaces |

### Backend

- **Framework:** Spring Boot 4.1 (Java 26), Spring MVC, Spring Security
- **Relational storage (SQL Server):** users, groups, group–collection mappings, and search logs (JPA)
- **Document storage (MongoDB):** the searchable organizational records
- **Authentication:** stateless JWT issued on login, validated per request via a JWT filter; passwords hashed with BCrypt
- **Roles:** `ADMIN` and `USER` (see `BackEnd/src/main/java/com/InfoLink/model/Role.java`)

### Frontend

Static pages in `FrontEnd/infolink-ui/` that talk to the backend through a shared HTTP client:

- `api.js` — central API client; attaches the JWT (`Authorization: Bearer <token>`) and centralizes error handling
- `data-store.js` — session helpers (token + logged-in user) and legacy localStorage store
- `shared.css` — shared dark-theme layout (sidebar, topbar, cards, tables)

## Features

- **JWT sign-in** with role-based redirect (admin → admin dashboard, user → user dashboard)
- **User management** — list, search, filter, add, edit, disable, and delete users
- **Group-scoped search** — records are searched inside the collections assigned to the user's group; access is enforced server-side
- **Search auditing** — every search is recorded (user, group, collection, keyword, timestamp, IP, success status)
- **Dashboards** — system stats for admins and personal access/summary for standard users

## API Endpoints

Base URL: `http://localhost:8080`

### Authentication

| Method | Path | Description |
| ------ | ---- | ----------- |
| `POST` | `/auth/login` | Authenticate and receive a JWT (`{ "token": "..." }`) |
| `GET`  | `/users/me` | Current authenticated user's profile (username, fullName, role, group, etc.) |

### Users

| Method | Path | Description |
| ------ | ---- | ----------- |
| `GET`    | `/users` | List all users |
| `POST`   | `/users/add` | Create a user |
| `PATCH`  | `/users/{id}` | Partially update a user (e.g. disable) |
| `PUT`    | `/users/{id}` | Full replacement of a user |
| `DELETE` | `/users/{id}` | Delete a user |

### Groups

| Method | Path | Description |
| ------ | ---- | ----------- |
| `GET`    | `/group` | List all groups |
| `POST`   | `/group` | Create a group |
| `PUT`    | `/group/{id}` | Update a group |
| `DELETE` | `/group/{id}` | Delete a group |

### Search

| Method | Path | Description |
| ------ | ---- | ----------- |
| `GET` | `/api/search?collection=<name>&field=<field>&keyword=<value>` | Search a collection (auth required, access-checked, logged) |

## Frontend Pages

| Page | Audience | Purpose |
| ---- | -------- | ------- |
| `login.html` | all | Sign in |
| `dashboard.html` | admin | System overview and stats |
| `dashboard-user.html` | user | Personal access and recent searches |
| `users.html` | admin | User list, search, disable, delete |
| `add-user.html` | admin | Create a user |
| `edit-user.html` | admin | Edit a user / reset password |
| `search-admin.html` | admin | Search across all groups |
| `search-user.html` | user | Search within assigned collections |

## Security Flow

1. User signs in via `POST /auth/login`.
2. Backend validates credentials and returns a JWT.
3. The frontend stores the token in `sessionStorage` and fetches the profile via `GET /users/me`.
4. `api.js` sends the token as an `Authorization: Bearer <token>` header on every request.
5. The backend's JWT filter validates the token and loads the user (including group and role) per request.

## Setup & Prerequisites

1. **Databases**
   - SQL Server with the `Users`, `Groups`, `GroupsCollections`, and `Logs` tables.
   - MongoDB with the searchable collections.

2. **Backend configuration**
   - Create `BackEnd/src/main/resources/application.properties` with your SQL Server datasource, MongoDB connection settings, and server port.
   - Set `JWT_SECRET` (base64-encoded secret, see `BackEnd/src/main/java/com/InfoLink/.env.example`).
   - Run: `./mvnw spring-boot:run`

3. **Frontend**
   - Serve `FrontEnd/infolink-ui/` from any static server (or open the pages directly).
   - Point `API_BASE` in `FrontEnd/infolink-ui/api.js` at the backend URL.
   - Ensure the backend allows the frontend origin (CORS).

## Data Model

- `User` — account with username, password hash, full name, role, group, and active flag
- `Groups` — department/team with name, description, and active flag
- `GroupsCollections` — mapping of a group to the MongoDB collection names it may search
- `Log` — audit entry for every search (user, group, collection, keyword, date, IP, status)
- `Role` — `ADMIN` / `USER`
