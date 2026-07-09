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
| `EMAIL_MX_CHECK_ENABLED` | (optional) Set to `false` to skip the DNS MX/A lookup at registration - see [Blocking dummy/fake emails](#blocking-dummyfake-emails) below. Defaults to `true` |

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

### Blocking dummy/fake emails

Registration (public Staff/BHW sign-up, and Parent/Guardian accounts created from the Guardians page) runs an email address through three layers before an account is created and a verification email is sent. Each layer runs in order and stops at the first failure:

1. **Format** (`EmailFormatValidator`) - rejects malformed addresses: missing/invalid TLD, leading/trailing or consecutive dots, embedded whitespace, addresses over 254 characters. Returns `EMAIL_INVALID_FORMAT`.
2. **Disposable/reserved domains** (`DisposableEmailService`) - rejects known temporary-email providers (loaded from `backend/src/main/resources/disposable-domains.txt`) and RFC 2606 reserved domains/TLDs (`example.com`, `.test`, `.invalid`, etc). Returns `DISPOSABLE_EMAIL`.
3. **DNS MX/A record check** (`MxRecordService`) - confirms the domain can actually receive mail (has an MX record, or falls back to an A record per RFC 5321 §5.1). Uses the JDK's built-in JNDI DNS resolver - no external dependency. Results are cached for 1 hour per domain; a DNS timeout (3s) or resolver failure **fails open** (registration is allowed, and a warning is logged) rather than blocking a real user. Returns `EMAIL_DOMAIN_INVALID`. Controlled by `EMAIL_MX_CHECK_ENABLED` (default `true`) - set to `false` for offline local dev, since it would otherwise reject every address.

Email verification (the link/code flow above) remains the final proof that the address is real and owned by the registrant - these three layers only filter out addresses that are obviously fake or unreachable before a verification email is even sent.

To avoid leaking which emails are already registered, registering with an email that already has an account returns the exact same response as a brand-new registration (no new verification email is sent for the existing account).

**Updating the disposable-domain blocklist:** add or remove one domain per line in `backend/src/main/resources/disposable-domains.txt` (`#`-prefixed lines and blank lines are ignored) and redeploy - no code changes needed. Don't add domains that merely *sound* generic (e.g. `email.com`, `mail.com`) - only add domains that are verifiably disposable/temporary-email services.

A daily scheduled job (`UnverifiedAccountCleanupJob`, 3am server time) permanently deletes accounts that are still unverified 7 days after registration, so mistyped or abandoned dummy signups don't pile up. Accounts with linked Guardian records are skipped and left for manual admin follow-up instead of being deleted out from under real child data.

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
