# DaycareLog

A daycare management system for tracking children, attendance, health records, and reports.

## Repository structure

```
daycarelog/
├── backend/   Spring Boot 3 REST API (Java 17, Maven)
├── web/       React + Vite web client
├── mobile/    Mobile client (planned)
└── docs/      Project documentation (SRS, etc.)
```

## Backend (`/backend`)

Spring Boot app backed by PostgreSQL (Supabase), using JWT auth.

### Requirements
- Java 17
- Maven
- A PostgreSQL database (e.g. Supabase)

### Environment variables

Set these before running (locally or on your deploy platform) — never commit real values:

| Variable | Description |
|---|---|
| `DB_URL` | JDBC URL for the PostgreSQL database |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | Long random string used to sign JWTs |
| `ADMIN_SEED_EMAIL` | (optional) Email for the first admin account, seeded on startup |
| `ADMIN_SEED_PASSWORD` | (optional) Password for the first admin account, seeded on startup |

### Run

```bash
cd backend
mvn spring-boot:run
```

The API starts on `http://localhost:8080`.

### Test

```bash
cd backend
mvn test
```

Tests run against an in-memory H2 database (see `backend/src/test/resources/application.properties`) and never touch the real database.

## Web (`/web`)

React + Vite single-page app.

### Requirements
- Node.js and npm

### Run

```bash
cd web
npm install
npm run dev
```

The dev server runs on Vite's default port and proxies `/api` requests to the backend at `http://localhost:8080` (see `web/vite.config.js`), so start the backend first.

### Build

```bash
cd web
npm run build
```

Outputs a production build to `web/dist`.

## Mobile (`/mobile`)

Not yet implemented.

## Docs (`/docs`)

Project documentation, including the Software Requirements Specification.
