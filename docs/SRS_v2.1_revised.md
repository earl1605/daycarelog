IT342-G01
SYSTEMS INTEGRATION AND ARCHITECTURE 1
SOFTWARE REQUIREMENTS SPECIFICATION (SRS)

Project Title: DaycareLog: A Digital Management System for Barangay Daycare Centers
Prepared By: Christian Earl V. Mahumot
Version: 2.1 (Implementation-Aligned Revision — supersedes Version 2.0, June 29, 2026)

> "I certify that this finalized SRS and UML models are my own individual work. I understand that copied, duplicated, or AI-generated submissions without proper understanding and revision may be subject to verification and possible deductions."
> — Christian Earl V. Mahumot

---

## 1. Introduction

### 1.1 Project Title
DaycareLog: A Digital Management System for Barangay Daycare Centers

### 1.2 System Overview
DaycareLog is a web-based information system that digitizes the core recordkeeping operations of a single barangay daycare center: child enrollment, guardian information, health monitoring, and daily attendance. The system follows a client–server architecture composed of a Spring Boot 3 REST API backend secured with custom JSON Web Token (JWT) authentication, a PostgreSQL relational database (hosted on Supabase), and a React (Vite + Tailwind CSS) single-page web client deployed on Vercel, with the backend deployed on Railway. A companion Android application (Kotlin) is planned as a future phase of the project and is not part of the current implementation.

### 1.3 Purpose of the System
The purpose of DaycareLog is to replace manual, paper-based enrollment and health recordkeeping at the barangay daycare level with a centralized digital system. The system is intended to reduce data-entry errors and record loss and give Daycare Staff real-time visibility into each child's enrollment and health status.

### 1.4 Target Users
The system currently supports three account roles — **Admin**, **Teacher**, and **Staff** — as implemented in the `users` table and enforced at the API layer. Full role descriptions and the actual scope of access enforced per role are provided in Section 2.

### 1.5 Problem Statement
*(Unchanged from v2.0 — narrative problem statement, not implementation-dependent.)*

Barangay daycare centers currently rely on manual, paper-based processes to manage child enrollment and health records. This results in records that are vulnerable to loss or physical damage, frequent data-entry errors, and slow record retrieval. The absence of a centralized digital system limits the ability of daycare staff to monitor children's health status in a timely and consistent manner.

### 1.6 Scope of the System
The current, implemented scope of DaycareLog covers:

1. **Child Enrollment Management** – registration, profile editing, and hard deletion of child records; client-side search by name and filter by enrollment status.
2. **Guardian Records** – a `guardians` database table exists (name, relationship, contact number, primary flag, linked to a child), but is **not yet exposed through any REST endpoint or UI screen**. It is schema-only at this stage.
3. **Health Record Monitoring** – weight/height capture per child, with nutritional status classification computed **client-side in the React app** from a simplified WHO weight-for-age median table (weight and age/sex only — height is not currently factored into the classification despite being captured).
4. **Attendance Tracking** – daily attendance recording (Present/Absent) with time-in/time-out per child, with one entry enforced per child per day at the database level.
5. **Report Generation** – a monthly enrollment/attendance/nutritional-status summary, exportable as **CSV** (not PDF/Excel).
6. **User Account & Role Management** – custom JWT-based authentication with three roles (Admin, Teacher, Staff). Role-based access control is currently enforced **only** on the user-management endpoints (`/api/users/**`); all other endpoints (children, health records, attendance, reports) are open to any authenticated user regardless of role.

**Not implemented in the current version** (removed from scope vs. v2.0, candidates for future work): Immunization tracking, PDF/Excel report export, account lockout after failed logins, child-profile archiving with reactivation workflow, duplicate-enrollment prevention, age-eligibility validation at enrollment, developmental milestone logging as a distinct record type, and the Android companion app.

The system serves one barangay daycare center per deployment instance and is accessible via web browser.

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
- Any authenticated user who self-registers can select **any** role at sign-up, including Admin — there is currently no approval workflow gating Admin self-assignment. This is a known limitation, not an intentional design.
- The system does not integrate with external government databases (PSA, DSWD).
- A mobile companion application is planned but not yet built.

---

## 2. Stakeholders and Users

### 2.1 Direct System Users (as implemented)

| Role | Description | Actual System Permissions |
|---|---|---|
| **Staff** | Default role assigned at registration; day-to-day data entry. | Full access to children, health records, attendance, and reports — identical at the API level to Teacher. |
| **Teacher** | Selectable role at registration; intended for classroom/instructional staff. | Functionally identical to Staff today — no endpoint currently differentiates Teacher from Staff permissions. |
| **Admin** | Combines the oversight and technical-administration duties described separately in v2.0 (Barangay Administrator + System Administrator are **not** separate roles in the implementation). | Everything Staff/Teacher can do, **plus** the only role-gated capabilities in the system: view the full user list, change another user's role, and delete a user account. |

> **Note:** Unlike v2.0's three-tier role model (Daycare Worker / Barangay Administrator / System Administrator), the implementation has a flat three-role scheme (Staff / Teacher / Admin) where Admin is the only role with any enforced elevated permission, and that permission is scoped solely to user management.

### 2.2 Indirect Stakeholders
*(Unchanged from v2.0.)*

| Stakeholder | Interest in the System |
|---|---|
| Enrolled Children | Beneficiaries of more timely and accurate health monitoring; do not interact with the system directly. |
| Parents / Guardians | Rely on the accuracy of health and attendance records maintained on the child's behalf. |
| Barangay Local Government Unit | Consumes consolidated enrollment and attendance reports for local planning. |

---

## 3. Functional Requirements

Requirements are renumbered FR-0XX. Each entry below is annotated **[Implemented]**, **[Partially Implemented]**, or **[Not Implemented]** against the current codebase.

### 3.1 User Authentication and Account Management

| ID | Requirement | Status |
|---|---|---|
| FR-001 | The system shall allow a registered user to log in using an **email** and password, returning a signed JWT access token upon successful authentication. | Implemented |
| FR-002 | The system shall reject login attempts with invalid credentials and return an error message. | Implemented |
| FR-003 | ~~Account lockout after failed login attempts~~ | **Not Implemented** — no lockout logic exists. Removed from current scope. |
| FR-004 | The system shall enforce role-based access control on `/api/users/**` endpoints only (list users, change role, delete user — Admin-only). All other endpoints require authentication but not a specific role. | Partially Implemented |
| FR-005 | The system shall allow an Admin to view all user accounts, change any user's role, and permanently delete a user account. | Implemented |
| FR-006 | The system shall store all user passwords using one-way bcrypt hashing (Spring Security `BCryptPasswordEncoder`); plaintext passwords are never persisted or logged. | Implemented |
| FR-006a | *(New)* Any self-registering user may select their own role, including Admin, with no approval step. | Implemented (flagged as a limitation, see 1.7) |

### 3.2 Child Enrollment Management

| ID | Requirement | Status |
|---|---|---|
| FR-007 | The system shall allow a user to create a new child profile with first name, last name, date of birth, sex, address, and enrollment date. | Implemented |
| FR-008 | The system shall compute and display the child's current age (in the frontend) based on date of birth. | Implemented (client-side) |
| FR-009 | The system shall allow a user to edit any field of an existing child profile. | Implemented — note: **no `updated_at` timestamp is tracked**, so the "automatically record the date/time of last update" behavior from v2.0 does not exist. |
| FR-010 | The system shall allow client-side searching by name and filtering by enrollment status. | Implemented (client-side only — no backend search/filter query params; no filtering by age range or sex). |
| FR-011 | ~~Archive child profile on graduation/withdrawal~~ | **Not Implemented as a distinct workflow.** A child has an `enrollmentStatus` field that can be set via the generic edit endpoint, but there is no dedicated archive/reactivate action, and the only delete operation is a permanent hard delete. |
| FR-012 | ~~Prevent duplicate enrollment by name + date of birth~~ | **Not Implemented** — no uniqueness check exists in `ChildService.create()` or at the database level. |
| FR-013 | The system shall display a dashboard summarizing active children, present-today count, total enrolled, and attendance rate. | Implemented |

### 3.3 Health Record Monitoring

| ID | Requirement | Status |
|---|---|---|
| FR-014 | The system shall allow a user to create a health record entry per child consisting of measurement date, weight (kg), height (cm), and free-text remarks. | Implemented |
| FR-015 | The system shall classify a child's nutritional status (Normal, Underweight, Severely Underweight, or Overweight) using a simplified WHO weight-for-age median table, computed **client-side**, based on weight, age, and sex. | Implemented, with two corrections vs. v2.0: (a) computed in the React frontend, not the Spring Boot service layer; (b) height is captured but not used in the classification; (c) there is no "Obese" category. |
| FR-016 | The system shall display a color-coded health status indicator on a child's profile/card reflecting the most recent classification. | Implemented |
| FR-017 | ~~Log a structured developmental milestone observation (description + date) per child~~ | **Not Implemented as a separate record type.** A free-text `remarks` field on each health record can be used for notes, but there is no dedicated milestone entity or list. |
| FR-018 | The system shall display a chronological health history per child, ordered by most recent measurement date. | Implemented |
| FR-019 | ~~Reject health record submissions with missing/out-of-range weight or height and return field-specific validation errors~~ | **Not Implemented** — no server-side validation beyond basic JSON deserialization exists in `HealthRecordService`/`HealthRecordController`. |

### 3.4 Immunization Tracking

**Removed from current scope.** No `ImmunizationRecord` entity, repository, controller, or UI exists anywhere in the codebase. This entire feature area from v2.0 (FR-020, FR-021) is future work, not an implemented requirement.

### 3.5 Attendance Tracking

| ID | Requirement | Status |
|---|---|---|
| FR-020 | The system shall allow a user to mark daily attendance for each enrolled child as Present or Absent. | Implemented |
| FR-021 | The system shall record time-in and time-out for each attendance entry. | Implemented |
| FR-022 | The system shall enforce one attendance entry per child per calendar date at the database level (unique constraint on `child_id, date`); resubmission for the same date updates the existing entry (upsert) rather than creating a duplicate. | Implemented — this is the one area that matches the original BR-09 description exactly. |

### 3.6 Report Generation

| ID | Requirement | Status |
|---|---|---|
| FR-023 | The system shall allow any authenticated user (not Admin-restricted) to generate a monthly summary for all active children, including total enrollment, present/absent counts, school days, attendance rate, and nutritional-status breakdown. | Implemented |
| FR-024 | The system shall allow the generated report to be exported as **CSV**. | Implemented — v2.0's PDF/XLSX export claim is incorrect; the implementation builds a CSV blob client-side and triggers a browser download. |
| FR-025 | ~~Include immunization completion rate in the summary report~~ | **Not Implemented** — no immunization data exists to aggregate. |

---

## 4. Non-Functional Requirements

### 4.1 Security

| ID | Requirement | Status |
|---|---|---|
| NFR-001 | The system enforces role-based access control on user-management endpoints only; unauthorized requests to those endpoints return HTTP 403. All other endpoints require a valid JWT but not a specific role. | Partially Implemented (narrower than v2.0's blanket claim) |
| NFR-002 | The system stores all passwords using bcrypt hashing (default `BCryptPasswordEncoder` cost factor of 10) and never transmits or logs plaintext passwords. | Implemented |
| NFR-003 | The system transmits client–server traffic over HTTPS/TLS in production (Vercel and Railway both terminate TLS). | Implemented |
| NFR-004 | The system issues JWT access tokens with a validity of **7 days**, after which the user must re-authenticate. | Implemented (corrected from v2.0's "24 hours") |
| NFR-005 | ~~Log all failed authentication attempts for audit purposes~~ | **Not Implemented** — no audit logging of failed logins exists. |
| NFR-006 | *(New)* CORS is permissive (`Access-Control-Allow-Origin: *`) at both the Spring Security CORS configuration and a servlet-level filter, since the API uses stateless bearer-token auth with no cookies. | Implemented |

### 4.2 Performance
*(Unchanged from v2.0 — not independently verifiable without load testing; retained as target, not measured.)*

### 4.3 Usability
*(Unchanged from v2.0, except:)* NFR-009's Filipino/English bilingual interface is **not implemented** — the UI is English-only. NFR-011's 1024px minimum is also not strictly accurate: the Users page table was recently made responsive down to mobile widths (sm/md breakpoints), so usability now extends below 1024px for at least that screen.

### 4.4 Reliability
*(Unchanged narrative from v2.0 — uptime/backup targets are infrastructure-level commitments from Supabase/Railway, not something the application code enforces directly.)*

### 4.5 Compatibility

| ID | Requirement | Status |
|---|---|---|
| NFR-007 | The web client shall function correctly on current versions of Chrome, Firefox, and Edge (desktop and mobile). | Implemented/targeted |
| NFR-008 | ~~Android companion app, Android 10.0+~~ | **Not built.** Planned future work. |
| NFR-009 | The backend REST API exposes endpoints under `/api/**`. *(v2.0's claim of versioned `/api/v1/...` endpoints is incorrect — there is no version segment in any route.)* | Implemented, unversioned |

### 4.6 Maintainability

| ID | Requirement | Status |
|---|---|---|
| NFR-010 | The backend follows a layered architecture: `controller` → `service` → `repository`, with `model` (entities), `dto`, `security`, and `config` packages. | Implemented |
| NFR-011 | Environment-specific configuration (database credentials, JWT secret) is externalized via environment variables rather than hardcoded. | Implemented |

---

## 5. Business Rules

| Rule ID | Business Rule | Status |
|---|---|---|
| BR-01 | ~~A child profile is uniquely identified by full name + date of birth; no duplicates allowed~~ | **Not enforced.** |
| BR-02 | ~~Only children aged 3–5 are eligible for enrollment~~ | **Not enforced** — no age validation exists at creation time. |
| BR-03 | A child's nutritional status reflects the most recently recorded measurement; the frontend always classifies against the latest health record per child. | Implemented |
| BR-04 | A color-coded health indicator reflects the current classification (Normal/Underweight/Severely Underweight/Overweight); it updates automatically as new measurements are recorded. | Implemented |
| BR-05 | ~~Archived child profiles cannot receive new records until reactivated~~ | **Not enforced** — `enrollmentStatus` does not gate record creation in any controller. |
| BR-06 | Each user account has exactly one role (Staff, Teacher, or Admin); multi-role accounts are not supported. | Implemented |
| BR-07 | ~~Accounts lock for 15 minutes after 3 failed logins~~ | **Not implemented.** |
| BR-08 | Only an Admin may view the full user list, change another user's role, or delete a user account. *(Caveat: any user can self-assign the Admin role at registration — see 1.7.)* | Implemented (with the caveat above) |
| BR-09 | Only one attendance entry is permitted per child per calendar date; a second submission updates the existing entry. Enforced via a database unique constraint plus upsert logic. | Implemented |
| BR-10 | ~~Reports exclude archived children unless explicitly included~~ | Not applicable — archiving isn't a real workflow today (see BR-05); the monthly report currently includes all children with `enrollmentStatus = "active"`. |

---

## 6. System Models (UML Diagrams)

The diagrams below describe what should be **redrawn** to match the implementation. (This document does not include rendered images — recreate these in your diagramming tool of choice using the descriptions below.)

### 6.1 Use Case Diagram — required changes from v2.0
- Replace the three actors **Daycare Worker / Barangay Administrator / System Administrator** with two actors: **Authenticated User** (covers Staff and Teacher, who have identical permissions) and **Admin** (extends Authenticated User, adds *Manage User Accounts*).
- Remove the *Log Immunization* use case entirely.
- Remove *Configure System* and *Perform Backup* — no in-app functionality exists for either; these are operator/infrastructure tasks (Supabase/Railway dashboards), not application use cases.
- Keep: *Register/Login, Enroll Child, Update Child Profile, Record Health Data, View Health Alerts, Track Attendance, Generate Reports, View Dashboard, Manage User Accounts.*
- Remove the *«extend»* relationship between *Update Child Profile* and *Archive Child Record* (archiving isn't a real, separate use case — status is just a field on the edit form).

### 6.2 Entity Relationship Diagram — required changes from v2.0
- Remove `IMMUNIZATION_RECORD` and `REPORT` entities entirely — neither exists in the database/JPA model.
- **Add** a `GUARDIAN` entity (table `guardians`): `id` (PK), `child_id` (FK → CHILD), `name`, `relationship`, `contact_number`, `is_primary`. Note in the diagram that this table exists but has **no REST endpoint** yet — mark it visually as "schema-only / not exposed."
- `USER` table fields: `id, email (unique), password (hashed), first_name, last_name, middle_name, suffix, profile_photo, role, created_at`. There is no `username` field — login is by email.
- `CHILD` table fields: `id, first_name, last_name, date_of_birth, sex, address, enrollment_date, enrollment_status, created_by (FK→USER), created_at`. No `updated_at`.
- `HEALTH_RECORD` table fields: `id, child_id (FK), measurement_date, weight_kg, height_cm, nutritional_status, remarks, recorded_by (FK→USER), created_at`.
- `ATTENDANCE` table fields: `id, child_id (FK), date, status, time_in, time_out, recorded_by (FK→USER), created_at`, with a **unique constraint on (child_id, date)**.
- Cardinalities: USER 1—M CHILD (via created_by), CHILD 1—M HEALTH_RECORD, CHILD 1—M ATTENDANCE, CHILD 1—M GUARDIAN.

### 6.3 Activity Diagram – Recording Health Data — required changes from v2.0
- Remove the "validate input fields → display validation error" branch — there is no server-side field validation today; bad input either gets silently stored or causes an unhandled error.
- Move the "compute nutritional status" step from the System/Database swimlane to the **Daycare Worker (client) swimlane**, since classification happens in the React app before the record is even submitted to the API.
- The "raise health alert indicator" step is a frontend rendering decision (color the badge), not a persisted flag — there is no `health_alert` boolean column in `health_record`.

### 6.4 Sequence Diagram – Recording Health Data — required changes from v2.0
- Remove step 3 ("validate JWT, check role = DAYCARE_WORKER") — `HealthRecordController` has no role check; only the JWT validity is checked by the shared `JwtAuthFilter`, not a specific role.
- Remove step 6 ("computeNutritionalStatus(age, weight)") from `HealthRecordService` — this computation happens in the browser before step 2 (the POST already includes the computed `nutritionalStatus` string).
- Remove the `alt [invalid input]` 400 Bad Request branch — there is no field-level validation in the service/controller to trigger it.

---

## 7. Requirements Traceability Table

| Req. ID | Requirement | System Function | Status |
|---|---|---|---|
| FR-001–FR-002 | Email/password login, JWT issuance | Authenticate User | Implemented |
| FR-004–FR-006 | Admin-only user management; bcrypt storage | Manage User Accounts | Implemented (narrow RBAC scope) |
| FR-007–FR-009 | Create/edit child profile | Enroll / Update Child | Implemented |
| FR-010 | Client-side search/filter | Search Records | Implemented (frontend only) |
| FR-013 | Enrollment dashboard | View Dashboard | Implemented |
| FR-014–FR-016, FR-018 | Health record CRUD, client-side classification, history view | Record Health Data | Implemented |
| FR-020–FR-022 | Mark and validate daily attendance | Track Attendance | Implemented |
| FR-023–FR-024 | Generate and export monthly report (CSV) | Generate Reports | Implemented |
| FR-011, FR-012, FR-017, FR-019 | Archiving, duplicate prevention, milestones, server-side validation | — | **Not Implemented** |
| FR-020–FR-021 (v2.0 numbering) | Immunization tracking | — | **Removed from scope** |
| BR-09 | One attendance entry per child per day | Track Attendance | Implemented (DB constraint + upsert) |
| BR-01, BR-02, BR-05, BR-07 | Duplicate prevention, age eligibility, archiving, login lockout | — | **Not Implemented** |

---

## 8. Revision History

| Version | Date | Summary of Changes |
|---|---|---|
| 1.0 | June 22, 2026 | Initial SRS with narrative requirements and placeholder UML sections. |
| 2.0 | June 29, 2026 | Added Immunization Tracking, Report Generation, Business Rules, Compatibility NFRs, traceability table. Renumbered FR/NFR schemes. |
| **2.1** | **June 30, 2026** | **Implementation-aligned revision.** Audited every functional and non-functional requirement against the actual Spring Boot/React codebase. Corrected: role model (Staff/Teacher/Admin replaces the three-tier Daycare Worker/Barangay Admin/System Admin scheme, with RBAC enforced only on user-management endpoints); login field (email, not username); JWT expiry (7 days, not 24 hours); report export format (CSV, not PDF/XLSX); nutritional classification location (client-side, weight+age+sex only, no height, no "Obese" category); CORS policy (`*`, not credentialed). Removed as not implemented: account lockout, immunization tracking, duplicate-enrollment prevention, age-eligibility validation, child archiving/reactivation workflow, structured developmental milestones, server-side health-record validation, failed-login audit logging, Filipino/English bilingual UI, versioned API routes. Added: Guardian entity (schema-only, no endpoint yet), self-service Admin role assignment at registration (flagged as a limitation). Updated all four UML diagrams' required changes in Section 6 (redraw guidance, since this revision does not include new diagram images).

---

## Individual Work Declaration

"I certify that this finalized SRS and UML models are my own individual work. I understand that copied, duplicated, or AI-generated submissions without proper understanding and revision may be subject to verification and possible deductions."

Name: Christian Earl V. Mahumot
Course & Section: IT342-G01 – Systems Integration and Architecture 1
Date: June 30, 2026
