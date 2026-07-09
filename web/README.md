# DaycareLog — Web

React + Vite single-page app for DaycareLog, a daycare management system for tracking children, attendance, health records, and reports. Talks to the [Spring Boot backend](../backend) over a REST API.

## Tech stack

- **React 18** + **React Router 6**
- **Vite** — dev server and build
- **Tailwind CSS** — styling
- **Recharts** — the weekly attendance chart on the dashboard
- **react-hot-toast** — toast notifications

## Requirements

- Node.js and npm

## Run

```bash
cd web
npm install
npm run dev
```

The dev server proxies `/api` requests to the backend at `http://localhost:8080` (see `vite.config.js`), so start the backend first.

## Build

```bash
npm run build
```

Outputs a production build to `dist/`.

## Test

```bash
npm test
```

Runs the pure-logic unit tests (currently `utils/emailValidation.test.js`) using Node's built-in test runner (`node --test`) — there's no Jest/Vitest/RTL dependency in this project, so component/DOM-rendering tests aren't covered yet.

## Project structure

```
src/
├── pages/          One component per route (Dashboard, Children, ChildForm, ChildDetail, Attendance, HealthRecords, HealthForm, Guardians, Reports, Users, Settings, Login, Register, Landing, ParentDashboard, ParentAttendance, ParentHealthRecords, ...)
├── components/      Shared UI — Sidebar, Layout, StatCard, GuardiansSection, NutritionalStatusBadge, ThemeToggle, icons, route guards
├── contexts/        AuthContext (session/JWT), ThemeContext (light/dark mode)
├── lib/api.js       Thin fetch wrapper around the backend REST API
└── utils/           Nutritional status classification, name capitalization, local-date formatting
```

## Notable behavior

- **Auth**: JWT stored client-side; route guards gate by login state and role — `StaffRoute` for Admin/Staff-only pages, `ParentRoute` for the read-only Parent dashboard/attendance/health pages (`admin`/`staff`/`teacher` vs `parent`).
- **Navigation**: sidebar (`Sidebar.jsx`) grouped into Main / Management / Admin sections, theme-toggleable (light/dark, `ThemeToggle.jsx`), matching the design carried over to the [Android app](../mobile).
- **Guardians & Parent portal**: a standalone Guardians directory page manages guardian portal accounts (create/reset password/remove), and a per-child `GuardiansSection` on the child edit form manages contact-only guardians; either can optionally get a `parent`-role login tied to that child.
- **Name fields**: First/Last/Middle Name inputs on Registration and Settings auto-capitalize each word as you type (`utils/capitalizeFirstLetters.js`), without disturbing cursor position or overriding intentional casing like "McDonald".
- **Email validation**: email fields on Registration and both Guardian-creation forms validate format on blur and offer a clickable "Did you mean…" correction for common domain typos (`gmial.com` → `gmail.com`, etc — see `utils/emailValidation.js`). This is a UX nicety only; the backend enforces the authoritative format/disposable-domain/MX checks regardless of what the client sends.
- **Nutritional status**: computed client-side from a simplified WHO weight-for-age table (`utils/nutritionalStatus.js`) — see the [SRS](../docs/SRS_v2.1_revised.md) for the exact classification rules and known limitations.
