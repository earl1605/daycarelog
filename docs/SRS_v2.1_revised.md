IT342-G01
SYSTEMS INTEGRATION AND ARCHITECTURE 1
SOFTWARE REQUIREMENTS SPECIFICATION (SRS)

Project Title: DaycareLog: A Digital Management System for Barangay Daycare Centers
Prepared By: Christian Earl V. Mahumot
Date of Submission: June 30, 2026
Version: 2.0 (Implementation-Aligned Revision — supersedes Version 1.0, June 29, 2026)

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

> **Note:** Unlike v2.0's three-tier role model, the implementation has a flat three-role scheme where Admin is the only role with any enforced elevated permission, scoped solely to user management.

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
| FR-004 | RBAC enforced only on `/api/users/**` (Admin-only); all other endpoints require auth but not a specific role. | **Partially Implemented** |
| FR-005 | Admin can view all users, change any user's role, and permanently delete a user. | **Implemented** |
| FR-006 | Passwords stored via bcrypt; never logged in plaintext. | **Implemented** |
| FR-006a | *(New)* Any self-registering user may pick their own role, including Admin, with no approval step. | **Implemented (flagged limitation)** |

### 3.2 Child Enrollment Management

| ID | Requirement | Status |
|---|---|---|
| FR-007 | Create a child profile: first/last name, DOB, sex, address, enrollment date. | **Implemented** |
| FR-008 | Compute/display current age (frontend only). | **Implemented (client-side)** |
| FR-009 | Edit any field of a child profile. No `updated_at` timestamp is tracked. | **Implemented (partial claim removed)** |
| FR-010 | Client-side search by name and filter by status (no backend search, no age/sex filter). | **Implemented (client-side only)** |
| FR-011 | Archive child profile workflow with reactivation. | **Not Implemented as distinct workflow** |
| FR-012 | Prevent duplicate enrollment (name + DOB). | **Not Implemented** |
| FR-013 | Dashboard: active children, present today, total enrolled, attendance rate. | **Implemented** |

### 3.3 Health Record Monitoring

| ID | Requirement | Status |
|---|---|---|
| FR-014 | Create health record: date, weight (kg), height (cm), free-text remarks. | **Implemented** |
| FR-015 | Classify nutritional status (Normal/Underweight/Severely Underweight/Overweight) client-side, weight+age+sex only — no height factor, no "Obese" category. | **Implemented (corrected description)** |
| FR-016 | Color-coded health status indicator reflecting latest classification. | **Implemented** |
| FR-017 | Structured developmental milestone log (description + date) as its own record type. | **Not Implemented (only a generic remarks field exists)** |
| FR-018 | Chronological health history per child. | **Implemented** |
| FR-019 | Reject missing/out-of-range weight/height with field-specific errors. | **Not Implemented** |

### 3.4 Immunization Tracking

**Removed from current scope.** No `ImmunizationRecord` entity, repository, controller, or UI exists anywhere in the codebase.

### 3.5 Attendance Tracking

| ID | Requirement | Status |
|---|---|---|
| FR-020 | Mark daily attendance as Present or Absent. | **Implemented** |
| FR-021 | Record time-in/time-out per attendance entry. | **Implemented** |
| FR-022 | One attendance entry per child per day, enforced via DB unique constraint + upsert. | **Implemented (matches v2.0 exactly)** |

### 3.6 Report Generation

| ID | Requirement | Status |
|---|---|---|
| FR-023 | Any authenticated user (not Admin-restricted) generates a monthly summary: enrollment, present/absent counts, school days, attendance rate, nutritional-status breakdown. | **Implemented** |
| FR-024 | Export report as CSV (corrected from "PDF or Excel"). | **Implemented** |
| FR-025 | Include immunization completion rate in the report. | **Not Implemented (no immunization data exists)** |

---

## 4. Non-Functional Requirements

### 4.1 Security

| ID | Requirement | Status |
|---|---|---|
| NFR-001 | RBAC enforced on user-management endpoints only (403 on violation); all other endpoints require valid JWT only. | **Partially Implemented** |
| NFR-002 | bcrypt password hashing (cost factor 10), no plaintext transmission/logging. | **Implemented** |
| NFR-003 | HTTPS/TLS in production (Vercel/Railway). | **Implemented** |
| NFR-004 | JWT validity of 7 days (corrected from 24 hours). | **Implemented** |
| NFR-005 | Log all failed authentication attempts. | **Not Implemented** |
| NFR-006 | *(New)* Permissive CORS (`Access-Control-Allow-Origin: *`) via Spring Security CORS config and a servlet-level filter, appropriate for stateless bearer-token auth with no cookies. | **Implemented** |

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
| NFR-008 | Android companion app, Android 10.0+. | **Not built — planned future work** |
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
| BR-06 | Each user has exactly one role (Staff/Teacher/Admin). | **Implemented** |
| BR-07 | 15-minute lockout after 3 failed logins. | **Not Implemented** |
| BR-08 | Only Admin can view/modify/delete user accounts. (Caveat: any user can self-assign Admin at registration.) | **Implemented (with caveat)** |
| BR-09 | One attendance entry per child per day; resubmission updates the existing entry. | **Implemented** |
| BR-10 | Reports exclude archived children unless explicitly included. | **N/A — archiving isn't a real workflow; report includes all "active" children** |

---

## 6. System Models (UML Diagrams)

The four diagrams below are redrawn to reflect the actual implementation, replacing the v2.0 diagrams that described the originally planned (but not fully built) system.

### 6.1 Use Case Diagram

```mermaid
flowchart LR
    ActorUser(["Authenticated User<br/>(Staff / Teacher)"])
    ActorAdmin(["Admin"])

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
    end

    ActorUser --> UC1
    ActorUser --> UC2
    ActorUser --> UC3
    ActorUser --> UC4
    ActorUser --> UC5
    ActorUser --> UC7
    ActorUser --> UC8
    UC5 -. "«include»" .-> UC6
    ActorAdmin -. generalizes .-> ActorUser
    ActorAdmin --> UC9
```

*Figure 6.1 – Use Case Diagram (implementation-aligned). Two actors only: Authenticated User (Staff/Teacher, identical permissions) and Admin (extends Authenticated User, adds Manage User Accounts). Immunization, Configure System, and Perform Backup use cases removed — none exist in the application.*

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
        varchar role
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
        bigint created_by FK
        timestamp created_at
    }
    GUARDIAN {
        bigint id PK
        bigint child_id FK
        varchar name
        varchar relationship
        varchar contact_number
        boolean is_primary
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
    CHILD      ||--o{ GUARDIAN      : "has"
    CHILD      ||--o{ HEALTH_RECORD : "has"
    CHILD      ||--o{ ATTENDANCE    : "has"
```

*Figure 6.2 – Entity Relationship Diagram (implementation-aligned). `IMMUNIZATION_RECORD` and `REPORT` entities removed (neither exists). `GUARDIAN` added as a real table with no REST endpoint yet. No `USER.username`, no `CHILD.updated_at`. `ATTENDANCE` carries a `unique(child_id, date)` constraint not expressible in the ER notation above.*

### 6.3 Activity Diagram – Recording Health Data

```mermaid
flowchart TD
    subgraph Client["Daycare Worker (Client / React)"]
        A1([Log in to system]) --> A2[Select child & open health form]
        A2 --> A3[Enter weight, height, remarks]
        A3 --> A4["Compute nutritional status client-side<br/>(WHO weight-for-age table)"]
        A4 --> A5[Render color-coded health alert badge]
        A5 --> A6[Submit POST /api/health-records]
        A9[Display confirmation] --> A10([End])
    end
    subgraph Backend["Backend (Spring Boot)"]
        B1["Validate JWT (JwtAuthFilter) — no role check"]
        B2["HealthRecordService.create(req)"]
        B3["Return 200 OK + recordId"]
    end
    subgraph Database["Database (PostgreSQL)"]
        D1[INSERT INTO health_record]
    end

    A6 --> B1 --> B2 --> D1 --> B3 --> A9
```

*Figure 6.3 – Activity Diagram: Recording Health Data (implementation-aligned). The validation branch from v2.0 is removed (no server-side field validation exists); nutritional status computation and the health alert badge are both client-side steps (highlighted), not backend logic.*

### 6.4 Sequence Diagram – Recording Health Data

```mermaid
sequenceDiagram
    actor DW as Daycare Worker
    participant UI as Health Form (React UI)
    participant Ctrl as HealthRecord Controller
    participant Svc as HealthRecord Service
    participant DB as DB

    DW->>UI: 1. enter weight, height, remarks
    UI->>UI: 2. compute nutritional status<br/>(client-side, WHO weight-for-age table)
    UI->>Ctrl: 3. POST /api/health-records<br/>{childId, weight, height, nutritionalStatus, remarks}
    Ctrl->>Ctrl: 4. validate JWT (JwtAuthFilter)<br/>— no role / no field validation
    Ctrl->>Svc: 5. create(req, userId)
    Svc->>DB: 6. INSERT INTO health_record (...)
    DB-->>Svc: recordId
    Svc-->>Ctrl: HealthRecord entity
    Ctrl-->>UI: 7. 200 OK { recordId, nutritionalStatus }
    UI-->>DW: 8. display confirmation / health alert badge
```

*Figure 6.4 – Sequence Diagram: Recording Health Data (implementation-aligned). No role check at the controller (JWT validity only), no backend nutritional-status computation, and no invalid-input alt branch — all removed from the v2.0 version since none exist in code.*

---

## 7. Requirements Traceability Table

| Req. ID | Requirement | System Function | Status |
|---|---|---|---|
| FR-001–FR-002 | Email/password login, JWT issuance | Authenticate User | **Implemented** |
| FR-004–FR-006 | Admin-only user management; bcrypt storage | Manage User Accounts | **Implemented (narrow RBAC)** |
| FR-007–FR-009 | Create/edit child profile | Enroll/Update Child | **Implemented** |
| FR-010 | Client-side search/filter | Search Records | **Implemented (frontend only)** |
| FR-013 | Enrollment dashboard | View Dashboard | **Implemented** |
| FR-014–FR-016, FR-018 | Health record CRUD, classification, history | Record Health Data | **Implemented** |
| FR-020–FR-022 | Mark/validate daily attendance | Track Attendance | **Implemented** |
| FR-023–FR-024 | Generate/export monthly report (CSV) | Generate Reports | **Implemented** |
| FR-011, FR-012, FR-017, FR-019 | Archiving, duplicate prevention, milestones, validation | — | **Not Implemented** |
| Immunization (v2.0 FR-020–021) | Immunization tracking | — | **Removed from scope** |
| BR-09 | One attendance entry per child per day | Track Attendance | **Implemented** |
| BR-01, BR-02, BR-05, BR-07 | Duplicate prevention, age eligibility, archiving, lockout | — | **Not Implemented** |

---

## 8. Revision History

| Version | Date | Summary of Changes |
|---|---|---|
| 1.0 | June 22, 2026 | Initial SRS with narrative requirements and placeholder UML sections. |
| 2.0 | June 29, 2026 | Added Immunization Tracking, Report Generation, Business Rules, Compatibility NFRs, traceability table. Renumbered FR/NFR schemes. |
| 2.0 | June 30, 2026 | **Implementation-aligned revision.** Audited every requirement against the actual Spring Boot/React codebase. Corrected role model, login field, JWT expiry, report export format, nutritional classification location/inputs, CORS policy. Removed as not implemented: account lockout, immunization tracking, duplicate-enrollment prevention, age-eligibility validation, child archiving, structured milestones, server-side health-record validation, failed-login audit logging, bilingual UI, versioned API routes. Added: Guardian entity (schema-only), self-service Admin role assignment caveat. Redrew all four UML diagrams in Section 6 to match the implementation. |

---

## Individual Work Declaration

"I certify that this finalized SRS and UML models are my own individual work. I understand that copied, duplicated, or AI-generated submissions without proper understanding and revision may be subject to verification and possible deductions."

Name: Christian Earl V. Mahumot
Course & Section: IT342-G01 – Systems Integration and Architecture 1
Date: June 30, 2026
Signature: ____________________________
