# Codebase Exploration and Gap Analysis: Enrollment and Schedule Management

## Executive Summary
This report presents a thorough investigation of the enrollment and class schedule management modules in the SIS codebase. It maps the backend controllers, services, repositories, database entities, frontend React pages, type contracts, and tests. A comprehensive gap analysis highlights the missing architectural and user-interface features required to fulfill specifications R1 through R5.

---

## 1. Backend Code Mapping

### 1.1 DB Models and Entity Schemas
All tables use `UUID` keys and track audit logs via a base class `AuditableEntity`.

1. **`class_schedules` (Mapped by `com.school.sis.schedule.entity.ClassSchedule`)**
   - `id`: `UUID PRIMARY KEY`
   - `section_id`: `UUID NOT NULL REFERENCES sections(id)` (Term-specific section)
   - `course_id`: `UUID NOT NULL REFERENCES courses(id)`
   - `faculty_id`: `UUID NOT NULL REFERENCES faculty(id)`
   - `room_id`: `UUID NOT NULL REFERENCES rooms(id)`
   - `school_year_id`: `UUID NOT NULL REFERENCES school_years(id)`
   - `semester_id`: `UUID NOT NULL REFERENCES semesters(id)`
   - `capacity`: `INTEGER NOT NULL`
   - `status`: `VARCHAR(20) NOT NULL` (`DRAFT`, `ACTIVE`, `CANCELLED`, `ARCHIVED`)
   - **Relationships**: Many-to-One relationships with `Section`, `Course`, `Faculty`, `Room`, `SchoolYear`, and `Semester`. One-to-Many cascade relationship with `ScheduleMeeting`.

2. **`schedule_meetings` (Mapped by `com.school.sis.schedule.entity.ScheduleMeeting`)**
   - `id`: `UUID PRIMARY KEY`
   - `class_schedule_id`: `UUID NOT NULL REFERENCES class_schedules(id) ON DELETE CASCADE`
   - `day_of_week`: `VARCHAR(20) NOT NULL` (`DayOfWeek` Enum)
   - `start_time`: `TIME NOT NULL` (`LocalTime`)
   - `end_time`: `TIME NOT NULL` (`LocalTime`)

3. **`enrollments` (Mapped by `com.school.sis.enrollment.entity.Enrollment`)**
   - `id`: `UUID PRIMARY KEY`
   - `student_id`: `UUID NOT NULL REFERENCES students(id)`
   - `program_id`: `UUID NOT NULL REFERENCES programs(id)`
   - `section_id`: `UUID REFERENCES sections(id)` (Nullable)
   - `school_year_id`: `UUID NOT NULL REFERENCES school_years(id)`
   - `semester_id`: `UUID NOT NULL REFERENCES semesters(id)`
   - `year_level`: `INTEGER NOT NULL`
   - `status`: `VARCHAR(20) NOT NULL` (`DRAFT`, `PENDING_ASSESSMENT`, `ASSESSED`, `ENROLLED`, `CANCELLED`, `DROPPED`, `COMPLETED`)
   - `remarks`: `TEXT`
   - **Constraint**: Composite unique index `ux_enrollments_active_term` on `(student_id, school_year_id, semester_id)` for non-cancelled enrollments.

4. **`enrollment_subjects` (Mapped by `com.school.sis.enrollment.entity.EnrollmentSubject`)**
   - `id`: `UUID PRIMARY KEY`
   - `enrollment_id`: `UUID NOT NULL REFERENCES enrollments(id) ON DELETE CASCADE`
   - `class_schedule_id`: `UUID NOT NULL REFERENCES class_schedules(id)`
   - `status`: `VARCHAR(20) NOT NULL` (`ENROLLED`, `DROPPED`, `CANCELLED`, `COMPLETED`)
   - `dropped_at`: `TIMESTAMPTZ`
   - **Constraint**: Composite unique index `ux_enrollment_subjects_active_schedule` on `(enrollment_id, class_schedule_id)` when status is `ENROLLED`.

5. **`enrollment_status_history` (Mapped by `com.school.sis.enrollment.entity.EnrollmentStatusHistory`)**
   - `id`: `UUID PRIMARY KEY`
   - `enrollment_id`: `UUID NOT NULL REFERENCES enrollments(id) ON DELETE CASCADE`
   - `from_status`: `VARCHAR(20)`
   - `to_status`: `VARCHAR(20) NOT NULL`
   - `remarks`: `TEXT`
   - `changed_at`: `TIMESTAMPTZ NOT NULL DEFAULT now()`

### 1.2 Controllers and API Endpoint Matrix

| Module | Method | Path | Request Payload / Params | Required Permission | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Schedules** | `GET` | `/api/v1/schedules` | Params: `search`, `schoolYearId`, `semesterId`, `programId`, `sectionId`, `facultyId`, `roomId`, `courseId`, `dayOfWeek`, `status` | `SCHEDULE_VIEW` | Paginated search of schedules |
| | `GET` | `/api/v1/schedules/{id}` | N/A | `SCHEDULE_VIEW` | Retrieve schedule details |
| | `POST` | `/api/v1/schedules` | `ScheduleRequest` | `SCHEDULE_MANAGE` | Create class schedule |
| | `PUT` | `/api/v1/schedules/{id}` | `ScheduleRequest` | `SCHEDULE_MANAGE` | Update class schedule |
| | `DELETE` | `/api/v1/schedules/{id}` | N/A | `SCHEDULE_MANAGE` | Soft-delete/Archive schedule |
| | `POST` | `/api/v1/schedules/check-conflict` | `ScheduleConflictRequest` | `SCHEDULE_VIEW` | Validate conflicts without saving |
| **Enrollment** | `GET` | `/api/v1/enrollments` | Params: `search`, `studentId`, `programId`, `sectionId`, `schoolYearId`, `semesterId`, `status` | `ENROLLMENT_VIEW` | Paginated search of enrollments |
| | `GET` | `/api/v1/enrollments/{id}` | N/A | `ENROLLMENT_VIEW` | Retrieve enrollment details |
| | `POST` | `/api/v1/enrollments` | `EnrollmentRequest` | `ENROLLMENT_CREATE` | Create a draft enrollment header |
| | `PUT` | `/api/v1/enrollments/{id}` | `EnrollmentUpdateRequest` | `ENROLLMENT_CREATE` | Update enrollment metadata |
| | `POST` | `/api/v1/enrollments/{id}/subjects` | `EnrollmentSubjectRequest` | `ENROLLMENT_CREATE` | Add a schedule to draft |
| | `DELETE` | `/api/v1/enrollments/{id}/subjects/{subjectId}`| N/A | `ENROLLMENT_CREATE` | Drop a selected schedule |
| | `POST` | `/api/v1/enrollments/{id}/validate` | N/A | `ENROLLMENT_VIEW` | Trigger prerequisite and conflict checks |
| | `POST` | `/api/v1/enrollments/{id}/confirm` | N/A | `ENROLLMENT_APPROVE` | Transition status to `CONFIRMED` |
| | `POST` | `/api/v1/enrollments/{id}/cancel` | N/A | `ENROLLMENT_APPROVE` | Cancel the enrollment |

### 1.3 Service and Repository Logic
* **`ScheduleService`**: Performs overlap detection across Rooms, Faculty, and Sections during schedule creation and update. Conflict check queries the database for `ACTIVE` schedules. Handles soft deletion via `ARCHIVED` status.
* **`EnrollmentService`**: Drives the enrollment lifecycle (Draft -> Validate -> Confirm / Cancel). Enforces classification rules (Regular students must have a section; Irregular/Cross-Enrolled do not require one). Performs schedule conflict checks for the student's selected courses.
* **`ClassScheduleRepository`**: Implements custom specification-based paginated queries.
* **`ScheduleMeetingRepository`**: Houses the overlapping query `findOverlappingActiveMeetings(...)` that checks:
  $$\text{existing.startTime} < \text{new.endTime} \quad \text{AND} \quad \text{new.startTime} < \text{existing.endTime}$$
* **`EnrollmentRepository`**: Enforces active term uniqueness index and retrieves a student's latest enrollment.
* **`EnrollmentSubjectRepository`**: Tracks selected schedule counts, seats available, and confirmed student lists.

---

## 2. Frontend Code Mapping

### 2.1 UI Component Architecture
* **`schedules-page.tsx`**: Renders two layouts: a tabular directory listing all schedules with paging/filters, and a visual `WeeklyView` showing M-S weekly grids. The schedule editor dialog selects section first, auto-loading curriculum details, room capacity, and active conflicts.
* **`enrollment-page.tsx`**: Facilitates the enrollment builder. First forces selecting an active student, then displays student academic meta-card, lets the registrar select a section (or "Mixed sections" for irregulars), creates the draft, and then renders the schedule picker table, validation warnings, and confirmation controls.
* **`lib/api.ts`**: Implements a promise-based `api` caller around `fetch`, adding bearer authorization headers, intercepting `401` errors, resolving JWT refresh logic, and handling blob file streams for PDF generation.
* **`lib/types.ts`**: Typescript definitions matching DB/DTO shapes (e.g., `Schedule`, `Meeting`, `Enrollment`, `EnrollmentValidation`).

---

## 3. Test Suite Mapping

### 3.1 Backend Service Tests
* **`ScheduleServiceTests.java`**: Runs active schedule checks, validates invalid time ranges, and verifies that updating a schedule ignores itself to prevent false overlap conflicts.
* **`EnrollmentServiceTests.java`**: Validates draft creation, rejects inactive sections, blocks duplicate term enrollments, checks prerequisite status, prevents time overlaps in student class selections, and ensures confirmed enrollments lock subjects.

### 3.2 Database Migration Tests
* **`PostgresMigrationTests.java`**: Uses a docker-backed PostgreSQL container to apply and validate all Flyway migration scripts (versions 1 through 8, but version 10 is the current latest). Checks that Hibernate database definitions successfully validate.

### 3.3 Frontend Playwright E2E Tests
E2E specs reside under `frontend/e2e/specs/`:
- **`curriculum.spec.ts`**: Curriculum builder CRUD, activation, course mapping, prerequisites, and layout validation.
- **`tier1_feature_coverage.spec.ts`**: Setup entity CRUD (Departments, Programs, Rooms, School Years, etc.).
- **`tier2_boundary_validation.spec.ts`**: Edge-case validations (duplicate codes, deactivations, boundary rules).
- **`tier3_cross_feature.spec.ts`**: Dropdown interactions and term synchronization.
- **`tier4_real_world.spec.ts`**: End-to-end user workflows (e.g., faculty onboarding, facilities expansion).
- *Observation*: **No E2E tests exist for Schedules and Enrollment modules.** The only schedule reference is a room deactivation blocker test in `tier2_boundary_validation.spec.ts`.

---

## 4. Detailed Gap Analysis

### R1. Regular Student Enrollment Flow
* **Gaps in Auto-population**:
  - The backend `EnrollmentService.create` method creates an empty draft header. It does **not** fetch and auto-populate the active, curriculum-eligible schedules of the selected section.
  - The frontend `EnrollmentPage` does not trigger auto-population of subjects upon draft creation. The user is presented with checkboxes and must manually select schedules.
* **Gaps in Load Completeness & Availability**:
  - The backend verification (`validateEnrollment` / `confirm`) does not verify if the section's required schedule load is complete. Confirmation proceeds as long as there is at least one subject.
  - There is no check blocking confirmation if a required section course schedule is missing, full, inactive, or unavailable.
* **Gaps in Section Mixing**:
  - Although the backend throws an exception if a regular student selects a schedule belonging to a different section, there is no validation preventing regular students from saving or confirming an incomplete/custom schedule mixture.

### R2. Irregular and Cross-Enrolled Student Flow
* **Gaps in Section Designation**:
  - The requirement mandates that irregular students cannot have a null section; they must use a "Mixed sections" designation. However, the backend currently accepts `sectionId: null` for irregular students, and the database `enrollments.section_id` column is nullable.
  - In `enrollment-page.tsx`, choosing "Mixed sections" (value `__mixed__`) posts `sectionId: null` to the API. This creates a schema gap since no concrete "mixed" section record is referenced or defined.
* **Gaps in Choice Restrictions**:
  - Irregular students' choices must be restricted by program, curriculum, enrollment year level, and term.
  - Current backend restricts by curriculum and term. However, the system must ensure that for irregular students selecting schedules from multiple sections, each section's program, curriculum, and term are validated dynamically.

### R3. Enrollment Records Management
* **Gaps in Navigation & Layout**:
  - The `/enrollment` route in the frontend completely lacks the "Enrollment Records" and "Enroll Student" tabs. The workspace only supports searching for a student and building an enrollment. There is no grid or list view displaying historical or existing enrollments.
* **Gaps in Record Management Operations**:
  - The records directory, search filters, detail inspection panels, and draft-resuming mechanisms are entirely absent in the UI.
* **Gaps in Cancellation Workflow**:
  - The backend cancel endpoint (`POST /api/v1/enrollments/{id}/cancel`) does not accept a payload. It performs cancellation silently and hardcodes the reason to `"Enrollment cancelled"`.
  - The system does not prompt the user for a cancellation reason in the UI, nor does it pass the reason to the backend to update `remarks` and status history.

### R4. Schedule Integration & UI Updates
* **Gaps in Filters & Searching**:
  - Backend search params (`/api/v1/schedules`) do not support filtering by `curriculumId` or `yearLevel` directly.
  - In `EnrollmentPage`, the "Filter" button is static and has no interactive controls. Filters for section, course, day, or availability are unimplemented.
* **Gaps in UI Schedule Availability Indicators**:
  - The available class schedules table does not clearly distinguish between selected, available, full, or unavailable options. It only disables the checkbox without visual labels (e.g., a "Full" badge or red highlights for time conflicts).
* **Gaps in Blocking Confirms**:
  - System warnings (e.g., prerequisite gaps) do not block confirmation, even though they should be clearly highlighted.

### R5. Backend Enforcement
* **Gaps in Transaction Management**:
  - Regular enrollment creation must handle draft header creation and schedule auto-population inside a single `@Transactional` method, ensuring rollbacks on schedule exhaustion or conflicts.
* **Gaps in DTOs and Payload Handling**:
  - The cancellation endpoint needs to accept a request body DTO containing a reason string.
  - No database representations exist for a "Mixed sections" designation, necessitating either a virtual Section resolution or schema-enforced fallback.
