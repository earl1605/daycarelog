IT342-G01
SYSTEMS INTEGRATION AND ARCHITECTURE 1
SOFTWARE REQUIREMENTS SPECIFICATION (SRS)

Project Title: DaycareLog: A Digital Management System for Barangay Daycare Centers
Prepared By: Christian Earl V. Mahumot
Date of Submission: June 30, 2026 (mobile-parity revision: July 2, 2026; parent/guardian-portal revision: July 3, 2026; health/immunization revision: July 12, 2026)
Version: 2.3 (Health Record & Immunization Tracking Revision — supersedes Version 2.2, July 3, 2026)

> "I certify that this finalized SRS and UML models are my own individual work. I understand that copied, duplicated, or AI-generated submissions without proper understanding and revision may be subject to verification and possible deductions."
> — Christian Earl V. Mahumot

---

## 1. Introduction

### 1.1 Project Title
DaycareLog: A Digital Management System for Barangay Daycare Centers

### 1.2 System Overview
DaycareLog is an information system that digitizes the core recordkeeping operations of a single barangay daycare center: child enrollment, guardian information, health monitoring, and daily attendance. The system follows a client–server architecture composed of a Spring Boot 3 REST API backend secured with custom JSON Web Token (JWT) authentication, a PostgreSQL relational database (hosted on Supabase), and two clients that share the same backend: a React (Vite + Tailwind CSS) single-page web client deployed on Vercel, and a native Android application (Kotlin + Jetpack Compose, Material 3). The backend is deployed on Railway. The Android client was originally scoped as a future phase; it is now implemented, though with a narrower feature set than the web client in a few areas — see 1.6 for the specific gaps.

### 1.3 Purpose of the System
The purpose of DaycareLog is to replace manual, paper-based enrollment and health recordkeeping at the barangay daycare level with a centralized digital system. The system is intended to reduce data-entry errors and record loss and give Daycare Staff real-time visibility into each child's enrollment and health status.

### 1.4 Target Users
The system currently supports four account roles — **Admin**, **Teacher**, **Staff**, and **Parent** — as implemented in the `users` table and enforced at the API layer. The Parent role is new as of this revision: it is never self-registered, but created by an Admin or Staff user when they set up a portal account for a child's guardian. Full role descriptions and the actual scope of access enforced per role are provided in Section 2.

### 1.5 Problem Statement
Barangay daycare centers currently rely on manual, paper-based processes to manage child enrollment and health records. This results in records that are vulnerable to loss or physical damage, frequent data-entry errors, and slow record retrieval. The absence of a centralized digital system limits the ability of daycare staff to monitor children's health status in a timely and consistent manner.

### 1.6 Scope of the System
The current, implemented scope of DaycareLog covers:

1. **Child Enrollment Management** – registration, profile editing, and hard deletion of child records; client-side search by name and filter by enrollment status (web and mobile), with computed/displayed current age (web and mobile).
2. **Guardian Records** – a `guardians` database table (name, relationship, contact number, email, address, primary flag, linked to a child) is now **fully exposed through REST endpoints and dedicated UI on both clients**: a per-child guardian section embedded in the child's edit screen (add/remove contact-only guardians), and a standalone Guardians directory screen (web `Guardians.jsx`, mobile `GuardiansScreen.kt`) for managing guardians that have a parent-portal login. See item 8 below for the portal-account behavior.
3. **Health Record Monitoring** – weight/height capture per child, from the same simplified WHO weight-for-age median table (weight and age/sex only — height is not currently factored into the classification despite being captured). As of this revision, **nutritional status is computed server-side on the web client's path** (`HealthRecordService`, ported value-for-value from the prior client-side table): the backend recomputes and persists the status itself rather than trusting whatever value the client submits, closing a prior gap where a client could submit an arbitrary status string. **The Android app still computes the classification client-side and sends it to the backend as before** — this is a new, currently-undiscovered client discrepancy: since the server now ignores the submitted value and recomputes its own, the Android-submitted classification is silently discarded and replaced with the server's computation, so *displayed results are unaffected in practice*, but the two clients no longer share one code path for this logic. A child's profile also now carries **allergies** (free text), **medical conditions** (free text), and **blood type** (one of the 8 standard ABO/Rh types, or unset) — **web-only**, added to the Enroll/Edit Child form; allergies render as a persistent red alert banner on the child's profile when set. **Weight/height entry has moved (web only)**: there is no longer a standalone "Add Health Record" screen on web — weight, height, measurement date, and remarks are entered as an optional section directly on the Enroll/Edit Child form, and saving with those fields filled in creates a new dated health-record row alongside the child update. The Android app still uses its own, separate "Add Health Record" screen (unchanged, not migrated to this pattern). The child's own profile page (web) also shows a **weight/height-over-time line chart** (recharts) built from that history — web-only, no mobile equivalent.

   Health records can also be **deleted**, available on both clients via `DELETE /api/health-records/{id}`. As of this revision this is a **soft delete** (`deleted_at` timestamp) rather than a permanent removal — the underlying database change applies to both clients since it lives in the shared backend, so a record deleted from the Android app is just as recoverable as one deleted from web. However, the **recovery UI (a Recycle Bin) is web-only and Admin-only**: `GET /api/health-records/trash`, `PUT /api/health-records/{id}/restore`, and `DELETE /api/health-records/{id}/permanent` are Admin-role-gated endpoints with no equivalent screen on Android, so a record deleted via the mobile app can only be restored by an Admin on the web client.
4. **Attendance Tracking** – daily attendance recording per child (web and mobile both expose Present/Absent/Late/Excused, broader than the original Present/Absent scope), with one entry enforced per child per day at the database level. Both clients now restrict attendance entry to **weekdays only (Monday–Friday)**: the date picker defaults to the nearest weekday and rejects weekend dates client-side before submission — this is enforced independently in web (`Attendance.jsx`) and mobile (`AttendanceScreen.kt`), not at the database level. Neither client's UI currently captures time-in/time-out, despite the `attendance` table having columns for both — see FR-021.
5. **Report Generation** – a monthly enrollment/attendance/nutritional-status summary (web and mobile). CSV export of the report is **web-only**; the Android client has no export/share action on this screen. **As of this revision, the web report additionally includes (web-only, no mobile equivalent):** a donut chart of the nutritional-status breakdown (replacing plain progress bars), an Immunization Coverage bar chart showing, per DOH EPI vaccine, how many currently-active children are fully dosed, and a paginated table listing every health record logged within the selected month.
6. **User Account & Role Management** – custom JWT-based authentication with four roles (Admin, Teacher, Staff, Parent). Role-based access control is now enforced **broadly**, not just on user-management endpoints: all write operations and "all records"/"any child by ID" read operations on children, attendance, health records, and immunizations require Admin or Staff; guardian management endpoints require Admin or Staff; the Parent-facing `/mine` endpoints (see item 8) are open to any authenticated user but resolve the caller's own linked children server-side from the JWT, so a Parent account can never see another child's data. Restoring or permanently deleting a soft-deleted health record or immunization is **Admin-only**, a narrower gate than ordinary delete (Admin or Staff). Admin user management (view, change role, deactivate/reactivate, reset password, delete) has full parity between web and mobile. Self-registration differs by client: web lets the registrant pick any role including Admin (see 1.7's known-limitation note); the Android app has no role picker and always registers as Staff. The Parent role is never available at self-registration on either client — it is only created by an Admin/Staff user through the guardian-portal-account flow (item 8).
7. **Android Client** – Kotlin + Jetpack Compose (Material 3), consuming the same REST API as the web client, so data is shared in real time across both. No dynamic/wallpaper-based system theming, but the app now has its own **manual light/dark mode toggle** (Settings screen), matching the web client's theme toggle and mirrored two-tier light/dark surface design. Covers dashboard, children, attendance, health records, guardians, reports, and admin user management, plus the read-only Parent screens (item 8); does not cover CSV export, admin self-registration, immunization tracking, allergy/medical-condition/blood-type fields, the growth chart, or the Recycle Bin (see items 3, 5, and 9 above/below for what's web-only as of this revision).
8. **Parent Portal** – An Admin or Staff user can, from the Guardians screen or a child's guardian section, optionally create a portal login for a guardian by supplying an email and marking "create portal account." This creates (or reuses, if the email already belongs to a Parent account) a `users` row with role `parent` and a system-generated temporary password, and links it to the `guardians` row via `guardians.user_id`. One Parent account can be linked to multiple children (multiple `guardians` rows sharing the same `user_id`). Once logged in, a Parent sees read-only Dashboard, Attendance, and Health Record screens (web: `ParentDashboard.jsx`/`ParentAttendance.jsx`/`ParentHealthRecords.jsx`; mobile: `ParentDashboardScreen.kt`/`ParentAttendanceScreen.kt`/`ParentHealthScreen.kt`) sourced from `/mine` endpoints (`/api/children/mine`, `/api/attendance/mine`, `/api/health-records/mine`) that resolve the linked child IDs from the caller's own JWT rather than accepting a child ID from the client. **As of this revision, web also has a fourth read-only Parent screen, `ParentImmunizations.jsx`**, sourced from a new `/api/immunizations/mine` endpoint (same JWT-scoping pattern) — **web-only, no Android equivalent.**
9. **Immunization Tracking** – *(new in this revision; previously out of scope, see the v2.0 revision's removal note)* A new `immunizations` table/entity tracks per-child vaccine doses against the DOH Expanded Program on Immunization (EPI) schedule: BCG (1 dose), Hepatitis B (1), Pentavalent (3), OPV (3), IPV (1), PCV (3), MMR (2), hardcoded server-side as the source of truth (`EpiVaccineSchedule`) and exposed read-only at `GET /api/immunizations/schedule`. Admin/Staff can record a dose (child, vaccine, dose number, date given, administered-by, notes) via a dedicated form, view a per-child checklist (complete doses shown with a checkmark and date, missing doses shown gray) on the child's profile, and delete a recorded dose. The backend rejects an unknown vaccine name, a dose number exceeding that vaccine's expected count, or a second active record for the same child/vaccine/dose combination (enforced both at the application layer and, if the accompanying migration SQL has been run, by a database-level partial unique index scoped to non-deleted rows). **Web-only** — see item 7 for the Android gap.

**Not implemented in the current version** (removed from scope vs. v2.0, candidates for future work): PDF/Excel report export, account lockout after failed logins, child-profile archiving with reactivation workflow, duplicate-enrollment prevention, age-eligibility validation at enrollment, developmental milestone logging as a distinct record type, time-in/time-out capture in either client's UI, and any write access for the Parent role (Parents are strictly read-only today). Immunization tracking, previously listed here as removed from scope, is implemented as of this revision (item 9) — web-only.

The system serves one barangay daycare center per deployment instance and is accessible via web browser or the Android app.

### 1.7 Assumptions and Constraints

**Assumptions**
- The barangay daycare center has at least one internet-connected computer or mobile device available to staff.
- Users receive basic orientation/training on the system prior to go-live.
- Data entered by Daycare Staff is assumed accurate at the point of entry.
- A Supabase-hosted PostgreSQL instance and standard web technologies are available throughout the deployment lifecycle.

**Constraints**
- The system requires an active internet connection for the web client; offline operation is not supported.
- The backend connects to Supabase PostgreSQL.
- Authentication is implemented as a custom JWT scheme (HS256), with a token validity of **7 days** (`app.jwt.expiration=604800000` ms), not 24 hours.
- Any authenticated user who self-registers **on the web client** can select **any** role at sign-up, including Admin — there is currently no approval workflow gating Admin self-assignment. This is a known limitation, not an intentional design. The Android client does not have this exposure since its registration screen has no role picker at all (always Staff).
- The system does not integrate with external government databases (PSA, DSWD).
- The Android client requires an active internet connection to the same backend as the web client; it has no offline mode either.

---

## 2. Stakeholders and Users

### 2.1 Direct System Users (as implemented)

| Role | Description | Actual System Permissions |
|---|---|---|
| **Staff** | Default role assigned at registration; day-to-day data entry. | Full access to children, guardians, health records, attendance, and reports — identical at the API level to Teacher. |
| **Teacher** | Selectable role at registration; intended for classroom/instructional staff. | Functionally identical to Staff today — no endpoint currently differentiates Teacher from Staff permissions. |
| **Admin** | Combines the oversight and technical-administration duties described separately in v2.0 (Barangay Administrator + System Administrator are **not** separate roles in the implementation). | Everything Staff/Teacher can do, **plus** the only Admin-exclusive capabilities in the system: view the full user list, change another user's role, and delete a user account. |
| **Parent** | Created only by an Admin/Staff user via the guardian-portal-account flow (see 1.6 item 8); never self-registered. | Strictly read-only: can view the Dashboard, Attendance, Health Records, and (web-only, as of this revision) Immunizations for the specific child(ren) they are linked to as a guardian, via the `/mine` endpoints. Cannot view any other child's data, and has no access to children/attendance/health-record/immunization write endpoints, the Recycle Bin, guardian management, user management, or reports. |

> **Note:** Unlike v2.0's three-tier role model, the implementation has a four-role scheme: Staff/Teacher/Admin share full read-write access to daycare records with Admin additionally gated on user management, while Parent is a separate, strictly read-only tier scoped to the caller's own linked children.

### 2.2 Indirect Stakeholders

| Stakeholder | Interest in the System |
|---|---|
| Enrolled Children | Beneficiaries of more timely and accurate health monitoring; do not interact with the system directly. |
| Parents / Guardians | Rely on the accuracy of health and attendance records maintained on the child's behalf. |
| Barangay Local Government Unit | Consumes consolidated enrollment and attendance reports for local planning. |

---

## 3. Functional Requirements

Each entry is annotated **Implemented**, **Partially Implemented**, or **Not Implemented** against the current codebase.

### 3.1 User Authentication and Account Management

| ID | Requirement | Status |
|---|---|---|
| FR-001 | Log in using email and password, returning a signed JWT. | **Implemented** |
| FR-002 | Reject invalid credentials with an error message. | **Implemented** |
| FR-003 | Account lockout after failed logins. | **Not Implemented** |
| FR-004 | RBAC enforced on `/api/users/**` (Admin-only) **and** on all children/attendance/health-record/guardian write and full-list endpoints (Admin or Staff); the Parent-facing `/mine` endpoints are open to any authenticated user but scope results server-side to the caller's own linked children. | **Implemented** |
| FR-005 | Admin can view all users, change any user's role, and permanently delete a user. | **Implemented** |
| FR-006 | Passwords stored via bcrypt; never logged in plaintext. | **Implemented** |
| FR-006a | *(New)* Any self-registering user may pick their own role, including Admin, with no approval step. | **Implemented on web (flagged limitation); not present on Android** — mobile's register screen has no role field and always creates a Staff account |

### 3.2 Child Enrollment Management

| ID | Requirement | Status |
|---|---|---|
| FR-007 | Create a child profile: first/last name, DOB, sex, address, enrollment date. | **Implemented** |
| FR-008 | Compute/display current age (frontend only). | **Implemented (client-side, web and mobile)** |
| FR-009 | Edit any field of a child profile. No `updated_at` timestamp is tracked. | **Implemented (partial claim removed)** |
| FR-010 | Client-side search by name and filter by status (no backend search, no age/sex filter). | **Implemented (client-side only, web and mobile)** |
| FR-011 | Archive child profile workflow with reactivation. | **Not Implemented as distinct workflow** |
| FR-012 | Prevent duplicate enrollment (name + DOB). | **Not Implemented** |
| FR-013 | Dashboard: active children, present today, total enrolled, attendance rate. | **Implemented** |

### 3.2a Guardian Records & Parent Portal *(New in v2.2)*

| ID | Requirement | Status |
|---|---|---|
| FR-026 | Create/list/remove guardian records per child (name, relationship, contact number, email, address, primary flag) via `/api/children/{id}/guardians`. | **Implemented (web and mobile)** |
| FR-027 | Optionally create a Parent-role portal account for a guardian (requires email), reusing an existing Parent account across multiple children if the email matches; view/remove portal accounts via a standalone Guardians directory (`/api/guardians`). | **Implemented (web and mobile)** |
| FR-028 | Parent users can retrieve their own linked children, attendance, and health records read-only via `/api/children/mine`, `/api/attendance/mine`, `/api/health-records/mine`, resolved server-side from the JWT (never from a client-supplied child ID). | **Implemented (web and mobile)** |

### 3.3 Health Record Monitoring

| ID | Requirement | Status |
|---|---|---|
| FR-014 | Create health record: date, weight (kg), height (cm), free-text remarks. | **Implemented** |
| FR-015 | Classify nutritional status (Normal/Underweight/Severely Underweight/Overweight), weight+age+sex only — no height factor, no "Obese" category. | **Implemented, computation location now differs by client**: web computes server-side (`HealthRecordService`/`NutritionalStatusCalculator`) as of this revision and ignores any client-submitted value; the Android app still computes client-side and submits it as before, but that submitted value is now silently discarded and replaced by the server's own computation. Both use the same WHO median table (ported value-for-value). |
| FR-016 | Color-coded health status indicator reflecting latest classification. | **Implemented (web and mobile)** |
| FR-017 | Structured developmental milestone log (description + date) as its own record type. | **Not Implemented (only a generic remarks field exists)** |
| FR-018 | Chronological health history per child. | **Implemented** |
| FR-019 | Reject missing/out-of-range weight/height with field-specific errors. | **Not Implemented** |
| FR-030 | Delete a health record, available on both clients via `DELETE /api/health-records/{id}`. | **Implemented (web and mobile) as a soft delete** as of this revision — see FR-033 |
| FR-031 | *(New)* Child profile carries allergies (free text), medical conditions (free text), and blood type (8 standard ABO/Rh types or unset); allergies render as a persistent red alert banner on the child's profile when set. | **Implemented, web-only** |
| FR-032 | *(New)* Weight/height-over-time line chart on the child's own profile, built from that child's health-record history. | **Implemented, web-only** |
| FR-033 | *(New)* Deleting a health record sets a `deleted_at` timestamp instead of removing the row (soft delete); an Admin-only Recycle Bin can list, restore, or permanently delete trashed records via `GET /api/health-records/trash`, `PUT /api/health-records/{id}/restore`, `DELETE /api/health-records/{id}/permanent`. | **Soft-delete behavior implemented for both clients (shared backend); Recycle Bin UI is web-only and Admin-only** |
| FR-034 | *(New)* Weight, height, measurement date, and remarks are entered as an optional section on the Enroll/Edit Child form rather than a separate "Add Health Record" screen; leaving weight/height blank saves the rest of the child's info without creating a new health-record row. | **Implemented, web-only** — Android retains its original standalone Add Health Record screen, unchanged |

### 3.4 Immunization Tracking

*(New in this revision — previously removed from scope; see the v2.0 revision's removal note in Section 8.)*

| ID | Requirement | Status |
|---|---|---|
| FR-035 | Record an immunization dose (child, vaccine name, dose number, date given, administered-by, notes) against the DOH Expanded Program on Immunization (EPI) schedule — BCG (1 dose), Hepatitis B (1), Pentavalent (3), OPV (3), IPV (1), PCV (3), MMR (2) — server-defined in `EpiVaccineSchedule` and exposed read-only at `GET /api/immunizations/schedule`. | **Implemented, web-only** |
| FR-036 | Reject an immunization record for an unknown vaccine name, a dose number exceeding that vaccine's expected count, or a second active record for the same child/vaccine/dose (application-layer check; a matching database-level partial unique index on non-deleted rows is provided as a reference migration for defense-in-depth). | **Implemented, web-only** |
| FR-037 | Per-child immunization checklist showing complete doses (with the date given) versus missing doses, for all seven EPI vaccines. | **Implemented, web-only** |
| FR-038 | Parent users can retrieve their own linked children's immunization records read-only via `/api/immunizations/mine`, resolved server-side from the JWT. | **Implemented, web-only** |
| FR-039 | Delete an immunization record (Admin/Staff) as a soft delete, with an Admin-only Recycle Bin to restore or permanently delete via `GET /api/immunizations/trash`, `PUT /api/immunizations/{id}/restore`, `DELETE /api/immunizations/{id}/permanent`. Restoring is rejected if a new active record already occupies that child/vaccine/dose combination. | **Implemented, web-only** |

### 3.5 Attendance Tracking

| ID | Requirement | Status |
|---|---|---|
| FR-020 | Mark daily attendance as Present or Absent. | **Implemented (web and mobile both expose a broader Present/Absent/Late/Excused set)** |
| FR-021 | Record time-in/time-out per attendance entry. | **Partially Implemented** — the `attendance` table has `time_in`/`time_out` columns and the backend model supports them, but neither the web nor the Android attendance screen has an input for either field, so they are never populated today |
| FR-022 | One attendance entry per child per day, enforced via DB unique constraint + upsert. | **Implemented (matches v2.0 exactly)** |
| FR-029 | *(New)* Restrict attendance recording to weekdays (Monday–Friday); the date input defaults to the nearest weekday and rejects weekend dates before submission. | **Implemented client-side only (web and mobile); not enforced at the database or API level** |

### 3.6 Report Generation

| ID | Requirement | Status |
|---|---|---|
| FR-023 | Any authenticated user (not Admin-restricted) generates a monthly summary: enrollment, present/absent counts, school days, attendance rate, nutritional-status breakdown. | **Implemented.** As of this revision, web additionally renders the nutritional-status breakdown as a donut chart (previously plain progress bars) and lists every health record logged in the selected month in a paginated table — **web-only, no mobile equivalent**. |
| FR-024 | Export report as CSV (corrected from "PDF or Excel"). | **Implemented on web; not implemented on Android** — the mobile Reports screen displays the same monthly summary data but has no export/share action |
| FR-025 | Include immunization completion rate in the report. | **Implemented as of this revision, web-only** — a bar chart shows, per EPI vaccine, how many currently-active children are fully dosed (`immunizationCoverage` in the `/api/reports/monthly` response); reflects current status, not scoped to the selected month. No mobile equivalent. |

---

## 4. Non-Functional Requirements

### 4.1 Security

| ID | Requirement | Status |
|---|---|---|
| NFR-001 | RBAC enforced on user-management endpoints (Admin-only) and on all children/attendance/health-record/immunization/guardian write and full-list endpoints (Admin/Staff only), returning 403 on violation; the Parent-facing `/mine` endpoints require only a valid JWT and scope data server-side to the caller's own linked children. | **Implemented** |
| NFR-002 | bcrypt password hashing (cost factor 10), no plaintext transmission/logging. | **Implemented** |
| NFR-003 | HTTPS/TLS in production (Vercel/Railway). | **Implemented** |
| NFR-004 | JWT validity of 7 days (corrected from 24 hours). | **Implemented** |
| NFR-005 | Log all failed authentication attempts. | **Not Implemented** |
| NFR-006 | Permissive CORS (`Access-Control-Allow-Origin: *`) via Spring Security CORS config and a servlet-level filter, appropriate for stateless bearer-token auth with no cookies. | **Implemented** |
| NFR-012 | *(New)* A stale or otherwise-invalid Bearer token no longer breaks a `permitAll()` endpoint (e.g. registration): `JwtAuthFilter` catches any exception while resolving the token and proceeds unauthenticated instead of letting it propagate into a bare 403. | **Implemented** |
| NFR-013 | *(New)* Restoring or permanently deleting a soft-deleted health record or immunization requires the Admin role specifically — a narrower gate than the ordinary delete action (Admin or Staff) — enforced via `SecurityConfig` rules declared ahead of the broader Admin/Staff wildcard rules so the narrower rule isn't shadowed. | **Implemented, web-only UI (no mobile Recycle Bin screen)** |

### 4.2 Performance
Unchanged from v2.0 — targets retained, not independently load-tested.

### 4.3 Usability
Filipino/English bilingual UI is not implemented (English only). The 1024px minimum width claim is outdated for at least the Users page, which was recently made responsive down to mobile widths.

### 4.4 Reliability
Unchanged narrative from v2.0 — uptime/backup targets are infrastructure-level commitments from Supabase/Railway, not application-level guarantees.

### 4.5 Compatibility

| ID | Requirement | Status |
|---|---|---|
| NFR-007 | Web client targets current Chrome/Firefox/Edge, desktop and mobile. | **Implemented/targeted** |
| NFR-008 | Android companion app, Android 10.0+ (`minSdk 24` in the actual Gradle config, i.e. Android 7.0+ — corrected from the original "10.0+" target). | **Implemented** — Kotlin + Jetpack Compose (Material 3) native client, consuming the same backend API as the web client; see 1.6 for the specific feature gaps versus web (no CSV export, no admin self-registration). The project's Gradle toolchain (AGP 8.3.2, Gradle 8.4, Kotlin 1.9.22, Compose Compiler 1.5.10) is deliberately pinned below the latest available versions for compatibility with Android Studio Iguana (2023.2.1), not because of any application-level constraint. |
| NFR-009 | API exposes endpoints under `/api/**` (no version segment — corrected from "`/api/v1/...`"). | **Implemented, unversioned** |

### 4.6 Maintainability

| ID | Requirement | Status |
|---|---|---|
| NFR-010 | Layered backend: controller → service → repository, plus model/dto/security/config. | **Implemented** |
| NFR-011 | Config externalized via environment variables. | **Implemented** |

---

## 5. Business Rules

| Rule ID | Business Rule | Status |
|---|---|---|
| BR-01 | Unique child profile by name + DOB. | **Not Enforced** |
| BR-02 | Only children aged 3–5 eligible for enrollment. | **Not Enforced** |
| BR-03 | Nutritional status reflects most recent measurement. | **Implemented** |
| BR-04 | Health indicator auto-updates with new measurements. | **Implemented** |
| BR-05 | Archived profiles blocked from new records until reactivated. | **Not Enforced** |
| BR-06 | Each user has exactly one role (Staff/Teacher/Admin/Parent). | **Implemented** |
| BR-07 | 15-minute lockout after 3 failed logins. | **Not Implemented** |
| BR-08 | Only Admin can view/modify/delete user accounts. (Caveat: any user can self-assign Admin at registration on web; the Android client has no self-registration role picker; the Parent role can never be self-assigned on either client.) | **Implemented (with web-only caveat)** |
| BR-09 | One attendance entry per child per day; resubmission updates the existing entry. | **Implemented** |
| BR-10 | Reports exclude archived children unless explicitly included. | **N/A — archiving isn't a real workflow; report includes all "active" children** |
| BR-11 | Attendance can only be recorded for Monday–Friday dates; weekend dates are rejected before submission. | **Implemented client-side only (web and mobile)** |
| BR-12 | A Parent portal account is scoped to exactly the children linked to it via `guardians.user_id`; one Parent account may be linked to multiple children, and can never view or act on any other child's data. | **Implemented** |
| BR-13 | *(New)* A child cannot have two active (non-deleted) immunization records for the same vaccine and dose number. | **Implemented** — enforced in `ImmunizationService`; the matching database-level partial unique index is reference-only and must be applied by hand (`V4__soft_delete_health_and_immunizations.sql`) |
| BR-14 | *(New)* A soft-deleted health record or immunization record can only be restored or permanently purged by an Admin. | **Implemented** |
| BR-15 | *(New)* Restoring a soft-deleted immunization record is rejected if a new active record already occupies that same child/vaccine/dose combination, to avoid resurrecting a duplicate. | **Implemented** |

---

## 6. System Models (UML Diagrams)

The four diagrams below are redrawn to reflect the actual implementation, replacing the v2.0 diagrams that described the originally planned (but not fully built) system.

### 6.1 Use Case Diagram

```mermaid
flowchart LR
    ActorUser(["Authenticated User - Staff or Teacher"])
    ActorAdmin(["Admin"])
    ActorParent(["Parent"])

    subgraph SYS["DaycareLog System"]
        UC1(("Register / Login"))
        UC2(("View Dashboard"))
        UC3(("Enroll Child"))
        UC4(("Update Child Profile"))
        UC5(("Record Health Data"))
        UC6(("View Health Alerts"))
        UC7(("Track Attendance"))
        UC8(("Generate Reports"))
        UC9(("Manage User Accounts"))
        UC10(("Manage Guardians"))
        UC11(("Create Parent Portal Account"))
        UC12(("View Own Child's Records"))
        UC13(("Track Immunizations"))
        UC14(("Manage Recycle Bin"))
    end

    ActorUser --> UC1
    ActorUser --> UC2
    ActorUser --> UC3
    ActorUser --> UC4
    ActorUser --> UC5
    ActorUser --> UC7
    ActorUser --> UC8
    ActorUser --> UC10
    ActorUser --> UC13
    UC5 -. "include" .-> UC6
    UC10 -. "include" .-> UC11
    ActorAdmin -. "generalizes" .-> ActorUser
    ActorAdmin --> UC9
    ActorAdmin --> UC14
    ActorParent --> UC1
    ActorParent --> UC12
```

*Figure 6.1 – Use Case Diagram (implementation-aligned). Three actors: Authenticated User (Staff/Teacher, identical permissions), Admin (extends Authenticated User, adds Manage User Accounts and Manage Recycle Bin), and Parent (a strictly read-only actor limited to viewing the specific child/children it is linked to as a guardian, including immunization status as of this revision — folded into UC12 rather than drawn separately). **Track Immunizations (UC13) and Manage Recycle Bin (UC14) are new as of this revision**, both web-only. Configure System and Perform Backup use cases remain removed — neither exists in the application.*

### 6.2 Entity Relationship Diagram

```mermaid
erDiagram
    USER {
        bigint id PK
        varchar email UK
        varchar password
        varchar first_name
        varchar last_name
        varchar middle_name
        varchar suffix
        text profile_photo
        varchar role "admin, teacher, staff, or parent"
        timestamp created_at
    }
    CHILD {
        bigint id PK
        varchar first_name
        varchar last_name
        date date_of_birth
        varchar sex
        varchar address
        date enrollment_date
        varchar enrollment_status
        text allergies "nullable, added this revision"
        text medical_conditions "nullable, added this revision"
        varchar blood_type "nullable, added this revision"
        bigint created_by FK
        timestamp created_at
    }
    GUARDIAN {
        bigint id PK
        bigint child_id FK
        varchar name
        varchar relationship
        varchar contact_number
        varchar email
        varchar address
        boolean is_primary
        bigint user_id FK "nullable - set only for portal-account guardians"
    }
    HEALTH_RECORD {
        bigint id PK
        bigint child_id FK
        date measurement_date
        decimal weight_kg
        decimal height_cm
        varchar nutritional_status
        varchar remarks
        bigint recorded_by FK
        timestamp created_at
        timestamp deleted_at "nullable - soft delete, added this revision"
    }
    IMMUNIZATION {
        bigint id PK
        bigint child_id FK
        varchar vaccine_name
        int dose_number
        date date_given
        varchar administered_by "nullable, free text"
        text notes
        timestamp created_at
        timestamp deleted_at "nullable - soft delete"
    }
    ATTENDANCE {
        bigint id PK
        bigint child_id FK
        date date
        varchar status
        time time_in
        time time_out
        bigint recorded_by FK
        timestamp created_at
    }

    USER       ||--o{ CHILD         : "created_by"
    USER       ||--o{ HEALTH_RECORD : "recorded_by"
    USER       ||--o{ ATTENDANCE    : "recorded_by"
    USER       ||--o{ GUARDIAN      : "user_id (parent portal login)"
    CHILD      ||--o{ GUARDIAN      : "has"
    CHILD      ||--o{ HEALTH_RECORD : "has"
    CHILD      ||--o{ ATTENDANCE    : "has"
    CHILD      ||--o{ IMMUNIZATION  : "has"
```

*Figure 6.2 – Entity Relationship Diagram (implementation-aligned). `REPORT` entity remains removed (no such table exists — the report endpoint computes its response on the fly). `IMMUNIZATION`, previously removed in the v2.0 revision, is reinstated as of this revision (new table, no legacy schema to reconcile). `GUARDIAN` is a fully REST-and-UI-exposed table as of v2.2 (previously schema-only), with `email`/`address` fields and an optional `user_id` linking it to a `parent`-role `USER` row for portal login. A guardian with no `user_id` is contact-only info with no login. `CHILD` gains `allergies`/`medical_conditions`/`blood_type` this revision (web-entry only, per 1.6 item 3). `HEALTH_RECORD` and `IMMUNIZATION` both gain `deleted_at` this revision, backing the soft-delete/Recycle Bin feature (1.6 item 3, FR-033/FR-039); a non-null `deleted_at` means the row is trashed but not physically removed. No `USER.username`, no `CHILD.updated_at`. `ATTENDANCE` carries a `unique(child_id, date)` constraint, and `IMMUNIZATION` a reference-only partial-unique index on `(child_id, vaccine_name, dose_number) WHERE deleted_at IS NULL`, neither expressible in the ER notation above.*

### 6.3 Activity Diagram – Recording Health Data

```mermaid
flowchart TD
    subgraph Client["Daycare Worker - Client, Web or Android"]
        A1([Log in to system]) --> A2["Open weight/height entry - web: section on the Edit Child form; Android: standalone Add Health Record screen"]
        A2 --> A3[Enter weight, height, date, remarks]
        A3 --> A6[Submit POST to /api/health-records]
        A9[Display confirmation and updated health alert badge] --> A10([End])
    end
    subgraph Backend["Backend - Spring Boot"]
        B1[Validate JWT via JwtAuthFilter, no role check]
        B2[HealthRecordService looks up the child's date of birth and sex]
        B2b[Compute nutritional status server-side from the WHO weight-for-age table - any client-submitted status value is ignored]
        B3[Persist the record; return 200 OK with recordId and the server-computed status]
    end
    subgraph Database["Database - PostgreSQL"]
        D1[INSERT INTO health_record]
    end

    A6 --> B1 --> B2 --> B2b --> D1 --> B3 --> A9
```

*Figure 6.3 – Activity Diagram: Recording Health Data (implementation-aligned, revised this revision). Nutritional status computation moved from a client-side step to a backend step (`HealthRecordService`/`NutritionalStatusCalculator`), for both clients' data — a client-submitted status value, if any, is now simply discarded server-side rather than trusted. The Android app still computes its own client-side value in parallel (unchanged) before submitting, but that computed value has no effect on the record that's actually saved. The validation branch from v2.0 remains removed (no field-level validation exists, e.g. no rejection of an out-of-range weight).*

### 6.4 Sequence Diagram – Recording Health Data

```mermaid
sequenceDiagram
    actor DW as Daycare Worker
    participant UI as Health Entry UI (web: Edit Child form section; Android: standalone Add Health Record screen)
    participant Ctrl as HealthRecord Controller
    participant Svc as HealthRecord Service
    participant ChildRepo as Child Repository
    participant DB as DB

    DW->>UI: 1. enter weight, height, date, remarks
    UI->>Ctrl: 2. POST /api/health-records with childId, weight, height, remarks
    Ctrl->>Ctrl: 3. validate JWT via JwtAuthFilter, no role or field validation
    Ctrl->>Svc: 4. create request for userId
    Svc->>ChildRepo: 5. look up the child's date of birth and sex
    ChildRepo-->>Svc: Child
    Svc->>Svc: 6. compute nutritional status server-side from the WHO weight-for-age table
    Svc->>DB: 7. INSERT INTO health_record with the server-computed status
    DB-->>Svc: recordId
    Svc-->>Ctrl: HealthRecord entity
    Ctrl-->>UI: 8. 200 OK with recordId and nutritionalStatus
    UI-->>DW: 9. display confirmation and health alert badge
```

*Figure 6.4 – Sequence Diagram: Recording Health Data (implementation-aligned, revised this revision). Adds the Child Repository lookup and server-side computation step (5–6) that didn't exist before this revision — the backend now needs the child's date of birth and sex to classify the submitted weight itself, rather than accepting a pre-classified value from the client. No role check at the controller beyond JWT validity, and no invalid-input alt branch — both remain absent, as in the prior version of this diagram.*

---

## 7. Requirements Traceability Table

| Req. ID | Requirement | System Function | Status |
|---|---|---|---|
| FR-001–FR-002 | Email/password login, JWT issuance | Authenticate User | **Implemented** |
| FR-004–FR-006 | Admin-only user management; bcrypt storage | Manage User Accounts | **Implemented (narrow RBAC)** |
| FR-007–FR-009 | Create/edit child profile | Enroll/Update Child | **Implemented** |
| FR-010 | Client-side search/filter | Search Records | **Implemented (frontend only)** |
| FR-013 | Enrollment dashboard | View Dashboard | **Implemented** |
| FR-014–FR-016, FR-018 | Health record CRUD, classification, history | Record Health Data | **Implemented (web and mobile)** |
| FR-020, FR-022 | Mark/validate daily attendance | Track Attendance | **Implemented** |
| FR-021 | Time-in/time-out capture | Track Attendance | **Partially Implemented — DB/backend only, no UI on either client** |
| FR-023 | Generate monthly report | Generate Reports | **Implemented (web and mobile)**; nutritional-status donut chart and per-month health-records table are web-only additions this revision |
| FR-024 | Export report as CSV | Generate Reports | **Implemented (web only)** |
| FR-025 | Immunization completion rate in reports | Generate Reports | **Implemented (web only)**, this revision |
| FR-006a | Self-registration role picker | Authenticate User | **Implemented (web only)** |
| FR-026–FR-027 | Guardian CRUD, parent-portal account creation | Manage Guardians | **Implemented (web and mobile)** |
| FR-028 | Parent read-only `/mine` endpoints | View Own Child's Records | **Implemented (web and mobile)** |
| FR-029 | Weekday-only attendance restriction | Track Attendance | **Implemented client-side only (web and mobile)** |
| FR-030 | Health record deletion | Record Health Data | **Implemented (web and mobile)**, now a soft delete as of this revision |
| FR-031 | Allergies / medical conditions / blood type on child profile | Enroll/Update Child | **Implemented (web only)**, this revision |
| FR-032 | Weight/height growth chart on child profile | Record Health Data | **Implemented (web only)**, this revision |
| FR-033 | Soft delete + Admin Recycle Bin for health records | Record Health Data | Soft-delete: **Implemented (web and mobile, shared backend)**; Recycle Bin UI: **web-only, Admin-only** — this revision |
| FR-034 | Weight/height entry merged into the Child form | Enroll/Update Child, Record Health Data | **Implemented (web only)**, this revision — Android keeps its separate Add Health Record screen |
| FR-035–FR-037 | Record/validate immunization doses, per-child checklist | Track Immunizations | **Implemented (web only)**, this revision |
| FR-038 | Parent read-only `/api/immunizations/mine` | View Own Child's Records | **Implemented (web only)**, this revision |
| FR-039 | Soft delete + Admin Recycle Bin for immunizations | Track Immunizations | **Implemented (web only)**, this revision |
| FR-011, FR-012, FR-017, FR-019 | Archiving, duplicate prevention, milestones, validation | — | **Not Implemented** |
| BR-09 | One attendance entry per child per day | Track Attendance | **Implemented** |
| BR-11 | Weekday-only attendance restriction | Track Attendance | **Implemented client-side only** |
| BR-12 | Parent portal account scoped to linked children only | View Own Child's Records | **Implemented** |
| BR-13 | One active immunization record per child/vaccine/dose | Track Immunizations | **Implemented**, this revision |
| BR-14 | Recycle Bin restore/purge is Admin-only | Record Health Data, Track Immunizations | **Implemented**, this revision |
| BR-15 | Restoring a superseded immunization dose is rejected | Track Immunizations | **Implemented**, this revision |
| BR-01, BR-02, BR-05, BR-07 | Duplicate prevention, age eligibility, archiving, lockout | — | **Not Implemented** |

---

## 8. Revision History

| Version | Date | Summary of Changes |
|---|---|---|
| 1.0 | June 22, 2026 | Initial SRS with narrative requirements and placeholder UML sections. |
| 2.0 | June 29, 2026 | Added Immunization Tracking, Report Generation, Business Rules, Compatibility NFRs, traceability table. Renumbered FR/NFR schemes. |
| 2.0 | June 30, 2026 | **Implementation-aligned revision.** Audited every requirement against the actual Spring Boot/React codebase. Corrected role model, login field, JWT expiry, report export format, nutritional classification location/inputs, CORS policy. Removed as not implemented: account lockout, immunization tracking, duplicate-enrollment prevention, age-eligibility validation, child archiving, structured milestones, server-side health-record validation, failed-login audit logging, bilingual UI, versioned API routes. Added: Guardian entity (schema-only), self-service Admin role assignment caveat. Redrew all four UML diagrams in Section 6 to match the implementation. |
| 2.1 | July 2, 2026 | **Mobile-parity revision.** The Android (Kotlin + Jetpack Compose) client, previously scoped as future work, is now implemented and audited alongside web. Updated NFR-008 from "not built" to Implemented. Brought mobile to feature parity with web on age display (FR-008), status filtering (FR-010), and client-side nutritional status classification (FR-015/FR-016) — the WHO median table was ported value-for-value from the web app and verified to match exactly; a pre-existing mobile bug where health records were serialized with the wrong JSON field name for the measurement date (silently dropping it) was fixed in the same pass. Documented two intentional client differences that were *not* changed: CSV export (FR-024) remains web-only, and self-registration's role picker (FR-006a) remains web-only — the Android client always registers as Staff, which is arguably the safer default given FR-006a is itself flagged as an unintentional limitation. Corrected FR-021 from "Implemented" to "Partially Implemented": the `attendance` table has time-in/time-out columns, but neither client's UI actually captures them, on any version of the app to date — this was already inaccurate in v2.0 and predates the mobile client. Advanced the version number to 2.1 to resolve the prior revision-history rows both being labeled "2.0" for two distinct dates. |
| 2.2 | July 3, 2026 | **Parent/Guardian portal revision.** Added a fourth account role, **Parent**, created only by an Admin/Staff user (never self-registered) through a new guardian-portal-account flow, and scoped to strictly read-only access over the caller's own linked children via three new `/mine` endpoints (FR-028). Moved Guardian Records (1.6 item 2) from "schema-only" to fully implemented: guardian CRUD is now exposed via REST and UI on both clients, including a standalone Guardians directory for managing portal accounts (FR-026/FR-027). Corrected FR-004/NFR-001 from "Partially Implemented" to **Implemented** — RBAC now covers all children/attendance/health-record/guardian write and full-list endpoints (Admin/Staff), not just `/api/users/**`; this had already been true in the codebase before this revision and was simply undocumented. Added FR-029/BR-11 (attendance restricted to weekdays, enforced client-side on both web and mobile) and FR-030 (health record deletion, Admin/Staff only, added to close a gap where the mobile UI called a delete endpoint that did not yet exist server-side). Noted the mobile client's new manual light/dark theme toggle (distinct from, and not a reversal of, the existing "no dynamic/wallpaper theming" statement, which refers only to Android's system-wide dynamic color feature) and the Iguana-compatible Gradle toolchain pin (AGP 8.3.2/Gradle 8.4/Kotlin 1.9.22), added to NFR-008 as an infrastructure note. Redrew the Use Case Diagram (added the Parent actor and three related use cases) and the ERD (`GUARDIAN` gained `email`/`address`/`user_id` columns and a relationship to `USER`) in Section 6. |
| 2.3 | July 12, 2026 | **Health record & immunization tracking revision, web-only.** Reinstated **Immunization Tracking** (Section 3.4), previously removed from scope in the v2.0 revision: a new `immunizations` table/entity tracks doses against the DOH EPI schedule (BCG, Hepatitis B, Pentavalent, OPV, IPV, PCV, MMR), with server-side dose/vaccine validation (FR-035/FR-036), a per-child checklist (FR-037), a Parent read-only `/mine` view (FR-038), and Admin-only recovery via a new Recycle Bin (FR-039) — every part of this **web-only**, no Android equivalent. Moved nutritional-status classification (FR-015) from a client-computed, client-submitted value to a **server-computed** one on web (`HealthRecordService`/`NutritionalStatusCalculator`), closing a gap where the server previously trusted whatever status string the client sent; **the Android client was not changed** and still computes/submits its own value, which the server now silently discards and replaces — a new, previously-undocumented client discrepancy worth flagging even though it doesn't affect what's displayed. Added allergies/medical-conditions/blood-type fields to the child profile (FR-031, web-only) and a weight/height growth chart on the child's own page (FR-032, web-only). Replaced the standalone "Add Health Record" screen on web with an optional section on the Enroll/Edit Child form (FR-034, web-only) — Android's separate screen is unchanged. Introduced **soft delete** for both health records and immunizations (FR-033/FR-039, `deleted_at` column, shared backend so it applies to deletions from either client) backing a new Admin-only Recycle Bin page (web-only UI) to restore or permanently purge trashed records; restoring an immunization dose is rejected if a new record already occupies that child/vaccine/dose combination (BR-15). Extended the monthly report (FR-023/FR-025, web-only additions) with a nutritional-status donut chart, an immunization-coverage bar chart, and a paginated table of the month's health records. Fixed a regression this revision's own earlier work introduced and then caught before release: the report's nutritional-status bucketing still used the old "Normal"/"Underweight" string labels after the classifier switched to an uppercase-code vocabulary, which would have silently broken the report's breakdown for any record created after that switch. Also fixed an unrelated, pre-existing bug found during this pass: a stale/invalid Bearer token could 403 a `permitAll()` endpoint such as registration (NFR-012). Redrew the Use Case Diagram (added Track Immunizations and Manage Recycle Bin), the ERD (`CHILD` gained three columns; `HEALTH_RECORD` and the new `IMMUNIZATION` entity both gained `deleted_at`), and the Recording Health Data activity/sequence diagrams (nutritional-status computation moved into the backend swimlane) in Section 6. |

---

## Individual Work Declaration

"I certify that this finalized SRS and UML models are my own individual work. I understand that copied, duplicated, or AI-generated submissions without proper understanding and revision may be subject to verification and possible deductions."

Name: Christian Earl V. Mahumot
Course & Section: IT342-G01 – Systems Integration and Architecture 1
Date: June 30, 2026
Signature: ____________________________
