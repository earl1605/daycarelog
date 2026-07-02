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

## Project structure

```
src/
├── pages/          One component per route (Dashboard, Children, Attendance, HealthRecords, Reports, Users, Settings, Login, Register, Landing, ...)
├── components/      Shared UI — Sidebar, Layout, StatCard, ChildCard, NutritionalStatusBadge, icons, route guards
├── contexts/        AuthContext (session/JWT), ThemeContext
├── lib/api.js       Thin fetch wrapper around the backend REST API
└── utils/           Nutritional status classification, name capitalization
```

## Notable behavior

- **Auth**: JWT stored client-side; `ProtectedRoute`/`AdminRoute` gate routes by login state and role (`admin` vs `staff`).
- **Navigation**: light-themed sidebar (`Sidebar.jsx`) grouped into Main / Management / Admin sections, matching the design carried over to the [Android app](../mobile).
- **Name fields**: First/Last/Middle Name inputs on Registration and Settings auto-capitalize each word as you type (`utils/capitalizeFirstLetters.js`), without disturbing cursor position or overriding intentional casing like "McDonald".
- **Nutritional status**: computed client-side from a simplified WHO weight-for-age table (`utils/nutritionalStatus.js`) — see the [SRS](../docs/SRS_v2.1_revised.md) for the exact classification rules and known limitations.
