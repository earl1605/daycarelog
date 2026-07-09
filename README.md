# DaycareLog

A daycare management system for tracking children, attendance, health records, and reports.

## Repository structure

```
daycarelog/
├── backend/   Spring Boot 3 REST API (Java 17, Maven)
├── web/       React + Vite web client
├── mobile/    Android client (Kotlin + Jetpack Compose)
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
| `MAIL_MODE` | (optional) Set to `console` to log verification emails to stdout instead of sending them - see [Email verification](#email-verification) below |
| `MAIL_HOST` | SMTP host (e.g. `smtp.gmail.com`) - required unless `MAIL_MODE=console` |
| `MAIL_PORT` | (optional) SMTP port, defaults to `587` |
| `MAIL_USERNAME` | SMTP username |
| `MAIL_PASSWORD` | SMTP password (a Gmail **app password**, not your regular Gmail password - see below) |
| `MAIL_FROM` | (optional) "From" address on verification emails, defaults to `no-reply@daycarelog.local` |
| `WEB_BASE_URL` | Public URL of the React web app, used to build the verification link (e.g. `https://daycarelog.vercel.app`) |

### Email verification

New accounts (public Staff/BHW registration, and Parent/Guardian accounts created from the Guardians page) must verify their email before they can use anything beyond the auth endpoints (`verify-email`, `resend-verification`, `me`, `refresh-token`, `logout`). The email contains both a clickable link (for the web app) and a 6-digit code (for the Android app) - either one verifies the account.

**Local development - no real SMTP needed:**

Set `MAIL_MODE=console` and leave `MAIL_HOST`/`MAIL_USERNAME`/`MAIL_PASSWORD` unset. Verification emails are logged to the backend's console instead of being sent - copy the link or code straight out of the log to verify:

```
[MAIL_MODE=console] Verification email for someone@example.com
Subject: Verify your DaycareLog account
Link: http://localhost:5173/verify-email?token=...
Code: 123456
```

**Production / real emails via Gmail:**

1. Enable 2-Step Verification on the Gmail account you want to send from (required for app passwords).
2. Go to [Google Account → Security → App passwords](https://myaccount.google.com/apppasswords), create one for "Mail", and copy the 16-character password.
3. Set:
   ```
   MAIL_HOST=smtp.gmail.com
   MAIL_PORT=587
   MAIL_USERNAME=your-address@gmail.com
   MAIL_PASSWORD=<the 16-character app password, no spaces>
   ```
4. Do **not** set `MAIL_MODE` (or set it to anything other than `console`) so real sending is used.

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

React + Vite single-page app. See [`web/README.md`](web/README.md) for tech stack, run/build instructions, and project structure.

## Mobile (`/mobile`)

Android client (Kotlin + Jetpack Compose, Material 3), consuming the same backend API as the web client. See [`mobile/README.md`](mobile/README.md) for tech stack, run instructions, and project structure.

## Docs (`/docs`)

Project documentation, including the Software Requirements Specification.
