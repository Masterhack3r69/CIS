# SIS Project Handoff: Progress and Next Steps

Date: 2026-07-09

This document summarizes what was accomplished today in the Student Information System backend and what should be implemented next. It is intended as a handoff for a new Codex/chat session so the next agent can continue without needing the full prior conversation.

## Current Project State

The repository is a Spring Boot backend for a college Student Information System.

Tech stack currently in use:

- Java 21
- Spring Boot 3.3.7
- Maven
- Spring Web
- Spring Data JPA / Hibernate
- Spring Security with JWT
- PostgreSQL 16
- Flyway
- Docker Compose
- PDFBox and Spring Mail dependencies are present for later report/email work

Current branch:

- `master`

Latest commit:

- `c70c71d Add curriculum and student profile modules`

Default seeded admin account:

```text
username: admin
password: admin123
email: admin@sis.local
```

Run locally:

```powershell
docker compose up --build
```

Verify:

```powershell
mvn test
```

## Completed Today

### 1. Foundation Already Present

The backend has a working foundation:

- Maven Spring Boot application.
- Docker Compose services for backend, PostgreSQL, and Redis.
- Flyway migrations.
- Standard API response shape through `ApiResponse<T>`.
- Pagination response shape through `PageResponse<T>`.
- Global exception handling.
- JWT login, refresh, logout, and current-user endpoint.
- Role and permission model.
- BCrypt password hashing.
- Stateless Spring Security configuration.
- Minimal user and role management APIs for admin setup.

Auth endpoints:

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/me`

User and role endpoints:

- `GET /api/v1/users`
- `POST /api/v1/users`
- `GET /api/v1/users/{id}`
- `PUT /api/v1/users/{id}`
- `PATCH /api/v1/users/{id}/status`
- `GET /api/v1/roles`

### 2. Academic Setup Module

Academic setup is implemented and usable.

Implemented setup entities/APIs:

- Departments
- Programs
- Courses
- Faculty
- Rooms
- School years
- Semesters
- Sections

Important behavior:

- List/get endpoints require `ACADEMIC_SETUP_VIEW`.
- Create/update/status endpoints require `ACADEMIC_SETUP_MANAGE`.
- Setup records use UUID IDs.
- Important uniqueness constraints are enforced by the database.
- Status updates exist where relevant, such as departments, faculty, rooms, sections.
- Faculty records can optionally link to users for grade encoding ownership.

Academic setup endpoints:

- `GET /api/v1/departments`
- `POST /api/v1/departments`
- `GET /api/v1/departments/{id}`
- `PUT /api/v1/departments/{id}`
- `PATCH /api/v1/departments/{id}/status`
- `GET /api/v1/programs`
- `POST /api/v1/programs`
- `GET /api/v1/programs/{id}`
- `PUT /api/v1/programs/{id}`
- `GET /api/v1/courses`
- `POST /api/v1/courses`
- `GET /api/v1/courses/{id}`
- `PUT /api/v1/courses/{id}`
- `GET /api/v1/faculty`
- `POST /api/v1/faculty`
- `GET /api/v1/faculty/{id}`
- `PUT /api/v1/faculty/{id}`
- `PATCH /api/v1/faculty/{id}/status`
- `GET /api/v1/rooms`
- `POST /api/v1/rooms`
- `GET /api/v1/rooms/{id}`
- `PUT /api/v1/rooms/{id}`
- `PATCH /api/v1/rooms/{id}/status`
- `GET /api/v1/school-years`
- `POST /api/v1/school-years`
- `GET /api/v1/school-years/{id}`
- `PUT /api/v1/school-years/{id}`
- `GET /api/v1/semesters`
- `POST /api/v1/semesters`
- `GET /api/v1/semesters/{id}`
- `PUT /api/v1/semesters/{id}`
- `GET /api/v1/sections`
- `POST /api/v1/sections`
- `GET /api/v1/sections/{id}`
- `PUT /api/v1/sections/{id}`
- `PATCH /api/v1/sections/{id}/status`

### 3. Curriculum Management Module

Curriculum management was implemented today.

Migration:

- `src/main/resources/db/migration/V2__curriculum_management.sql`

Main tables:

- `curricula`
- `curriculum_courses`
- `curriculum_course_prerequisites`
- `curriculum_course_corequisites`

Main package:

- `src/main/java/com/school/sis/curriculum`

Implemented behavior:

- Create/update/list/get curriculum versions.
- Assign courses to a curriculum by year level and semester label.
- Store prerequisite and corequisite links to catalog courses.
- Prevent duplicate course assignment for the same curriculum, year level, and semester.
- Activate a curriculum transactionally.
- When one curriculum is activated, any other active curriculum for the same program becomes `INACTIVE`.
- Generate checklist response grouped by year level and semester.
- Compute lecture, laboratory, and credit-unit totals per term.

Curriculum permissions:

- `CURRICULUM_VIEW`
- `CURRICULUM_MANAGE`

Curriculum endpoints:

- `GET /api/v1/curricula`
- `POST /api/v1/curricula`
- `GET /api/v1/curricula/{id}`
- `PUT /api/v1/curricula/{id}`
- `POST /api/v1/curricula/{id}/courses`
- `PUT /api/v1/curricula/{id}/courses/{curriculumCourseId}`
- `DELETE /api/v1/curricula/{id}/courses/{curriculumCourseId}`
- `GET /api/v1/curricula/{id}/checklist`
- `POST /api/v1/curricula/{id}/activate`

Verified manually:

- Unauthenticated curriculum access returns `401`.
- Curriculum creation works.
- Curriculum courses can be added for first/second semester.
- Prerequisite and corequisite links work.
- Duplicate course assignment returns `400`.
- Detail and checklist endpoints work.
- Term totals are computed.
- Activation works.
- Activating a second curriculum marks the first as `INACTIVE`.
- Course update/delete endpoints work.
- Validation failures return `400`.

### 4. Student Profile Management Module

Student profile management was implemented today.

Migration:

- `src/main/resources/db/migration/V3__student_profile_management.sql`

Main tables:

- `students`
- `student_contacts`
- `student_family_backgrounds`
- `student_educational_backgrounds`
- `student_documents`

Main package:

- `src/main/java/com/school/sis/student`

Implemented behavior:

- Create/update/list/get student profiles.
- Nested student profile request/response structure:
  - personal
  - contact
  - family
  - educational
  - academic
- Student is linked to:
  - program
  - curriculum
  - optional section
- Validates that curriculum belongs to the selected program.
- Enforces unique student number.
- Enforces unique student email when provided.
- Supports student status patching.
- Supports paginated student search/filtering.
- Supports document upload using multipart form data.
- Stores uploaded files under configured local document storage:
  - default: `uploads/documents`
- Stores document metadata.
- Supports document verification status updates.
- Adds a placeholder academic records endpoint returning an empty records list until enrollment/grades exist.

Student permissions used:

- `STUDENT_VIEW`
- `STUDENT_CREATE`
- `STUDENT_UPDATE`

Student endpoints:

- `GET /api/v1/students`
- `POST /api/v1/students`
- `GET /api/v1/students/{id}`
- `PUT /api/v1/students/{id}`
- `PATCH /api/v1/students/{id}/status`
- `POST /api/v1/students/{id}/documents`
- `GET /api/v1/students/{id}/documents`
- `PATCH /api/v1/students/{id}/documents/{documentId}/verify`
- `GET /api/v1/students/{id}/academic-records`

Verified manually:

- Unauthenticated student access returns `401`.
- Student creation works with program/curriculum assignment.
- Duplicate student number returns `400`.
- Duplicate student email returns `400`.
- Student list filters work.
- Student detail retrieval works.
- Student update works.
- Student status patch works.
- Document upload works.
- Document listing works.
- Document verification works.
- Document-status filter works.
- Academic records endpoint returns an empty records shape.
- Validation failures return `400`.

## Flyway Migrations

Current migrations:

- `V1__foundation_auth_and_setup.sql`
  - auth tables
  - roles/permissions
  - academic setup tables
  - initial admin user
  - audit/report placeholder tables
- `V2__curriculum_management.sql`
  - curriculum tables
  - curriculum permissions
- `V3__student_profile_management.sql`
  - student profile tables
  - student document tables
  - student permission grants
- `V4__schedule_management.sql`
  - class schedule tables
  - schedule meeting tables
  - schedule permission grants
- `V5__enrollment_management.sql`
  - enrollment tables
  - enrollment subject tables
  - enrollment status history tables
  - enrollment view permission grant
- `V6__fees_and_assessments.sql`
  - fee setup tables
  - assessment and assessment item tables
  - fee view permission grant
- `V7__grade_recording.sql`
  - faculty user mapping
  - grade tables
  - academic record tables
  - grade view/review permission grants

Important note:

- Do not edit already-applied migrations for normal feature work.
- Add new migrations such as `V8__...sql` for the next module.

## Verified Commands

The test suite was run successfully:

```powershell
mvn test
```

The backend was manually run and tested at:

```text
http://localhost:8080
```

### 5. Schedule Management Module

Schedule management has now been implemented.

Migration:

- `src/main/resources/db/migration/V4__schedule_management.sql`

Main tables:

- `class_schedules`
- `schedule_meetings`

Main package:

- `src/main/java/com/school/sis/schedule`

Implemented behavior:

- Create/update/list/get class schedules.
- Store one or more meetings per schedule.
- Soft-delete schedules by marking them `ARCHIVED`.
- Validate section, course, faculty, room, school year, and semester references.
- Validate meeting day, start time, end time, and invalid time ranges.
- Validate the schedule term matches the selected section term.
- Prevent active schedules from assigning inactive faculty, inactive rooms, or inactive sections.
- Check active-schedule conflicts for room, faculty, and section overlaps.
- Allow updates to ignore the schedule currently being edited.
- Return detailed conflict records from `/check-conflict`.

Schedule permissions:

- `SCHEDULE_VIEW`
- `SCHEDULE_MANAGE`

Schedule endpoints:

- `GET /api/v1/schedules`
- `POST /api/v1/schedules`
- `GET /api/v1/schedules/{id}`
- `PUT /api/v1/schedules/{id}`
- `DELETE /api/v1/schedules/{id}`
- `POST /api/v1/schedules/check-conflict`

Verified by automated tests:

- Room/faculty/section overlaps are reported.
- Active schedule creation rejects overlapping room conflicts.
- Back-to-back meetings are allowed.
- Updating a schedule ignores itself during conflict checks.
- Invalid time ranges are rejected.

### 6. Enrollment Management Module

Enrollment management has now been implemented.

Migration:

- `src/main/resources/db/migration/V5__enrollment_management.sql`

Main tables:

- `enrollments`
- `enrollment_subjects`
- `enrollment_status_history`

Main package:

- `src/main/java/com/school/sis/enrollment`

Implemented behavior:

- Create/update/list/get enrollment records.
- Create enrollments as `DRAFT` headers first.
- Add active class schedules as enrollment subjects.
- Drop enrollment subjects by marking them `DROPPED`.
- Confirm valid draft enrollments as `CONFIRMED`.
- Cancel draft or confirmed enrollments as `CANCELLED`.
- Record enrollment status history on creation, confirmation, and cancellation.
- Prevent duplicate active enrollment for the same student, school year, and semester.
- Validate selected schedules against term, optional section, program, active schedule status, curriculum membership, and selected-subject time conflicts.
- Return validation details with blocking issues, warnings, selected subject count, and total credit units.
- Report prerequisites as not evaluated until grade and academic record data exist.

Enrollment permissions:

- `ENROLLMENT_VIEW`
- `ENROLLMENT_CREATE`
- `ENROLLMENT_APPROVE`

Enrollment endpoints:

- `GET /api/v1/enrollments`
- `POST /api/v1/enrollments`
- `GET /api/v1/enrollments/{id}`
- `PUT /api/v1/enrollments/{id}`
- `POST /api/v1/enrollments/{id}/subjects`
- `DELETE /api/v1/enrollments/{id}/subjects/{subjectId}`
- `POST /api/v1/enrollments/{id}/validate`
- `POST /api/v1/enrollments/{id}/confirm`
- `POST /api/v1/enrollments/{id}/cancel`

Verified by automated tests:

- Draft enrollment creation works.
- Duplicate active enrollment is rejected.
- Valid active schedules can be added.
- Duplicate subjects are rejected.
- Non-curriculum schedules are rejected.
- Schedule term mismatches are rejected.
- Conflicting selected schedules are rejected.
- Back-to-back selected schedules are allowed.
- Dropped subjects are excluded from totals and validation.
- Confirmation records status history and locks enrollment.
- Cancellation records status history.
- Prerequisite warnings do not block validation.

### 7. Fees and Assessment Module

Fees and assessment has now been implemented.

Migration:

- `src/main/resources/db/migration/V6__fees_and_assessments.sql`

Main tables:

- `fee_items`
- `fee_rules`
- `assessments`
- `assessment_items`

Main package:

- `src/main/java/com/school/sis/fee`

Implemented behavior:

- Create/update/list/get fee setup records.
- Attach active or inactive fee rules to fee items.
- Support fixed fee rules and per-unit fee rules.
- Scope fee rules by optional program, school year, semester, and year level.
- Generate assessments from confirmed enrollments.
- Snapshot fee code, fee name, rule type, quantity, unit amount, and line amount into assessment items.
- Prevent duplicate non-void assessments for the same enrollment.
- Recalculate draft or unpaid assessments from current active fee rules.
- Block recalculation for paid or void assessments.
- Track assessment status as `DRAFT`, `UNPAID`, `PAID`, or `VOID`.

Fee permissions:

- `FEE_VIEW`
- `FEE_MANAGE`

Fee and assessment endpoints:

- `GET /api/v1/fees`
- `POST /api/v1/fees`
- `GET /api/v1/fees/{id}`
- `PUT /api/v1/fees/{id}`
- `PATCH /api/v1/fees/{id}/status`
- `POST /api/v1/enrollments/{id}/generate-assessment`
- `GET /api/v1/assessments`
- `GET /api/v1/assessments/{id}`
- `POST /api/v1/assessments/{id}/recalculate`
- `PATCH /api/v1/assessments/{id}/status`

Verified by automated tests:

- Fee setup with scoped rules works.
- Confirmed enrollments can generate assessments.
- Draft enrollments cannot generate assessments.
- Duplicate active assessments are rejected.
- Fixed and per-unit fees compute expected totals.
- Recalculation uses current fee rules.
- Paid assessments cannot be recalculated or changed.
- Inactive fee rules are ignored.

### 8. Grade Recording Module

Grade recording has now been implemented.

Migration:

- `src/main/resources/db/migration/V7__grade_recording.sql`

Main tables:

- `grades`
- `grade_status_history`
- `academic_records`

Main package:

- `src/main/java/com/school/sis/grade`

Implemented behavior:

- Link faculty records to users through nullable `faculty.user_id`.
- Encode grades against confirmed enrollment subjects for a class schedule.
- Enforce assigned faculty user ownership for encode and submit.
- Validate Philippine numeric grades from `1.00` to `5.00` in `0.25` increments.
- Support special grades `INC`, `DRP`, `NG`, `W`, and `COND`.
- Derive grade remarks as passed, failed, incomplete, dropped, no grade, withdrawn, or conditional.
- Support workflow `ENCODED -> SUBMITTED -> REVIEWED -> APPROVED -> LOCKED`.
- Require a complete class sheet before submission, review, approval, or locking.
- Upsert typed academic records on approval and refresh them on lock.
- Replace the student academic-records placeholder with typed academic record responses.

Grade permissions:

- `GRADE_VIEW`
- `GRADE_ENCODE`
- `GRADE_REVIEW`
- `GRADE_APPROVE`

Grade endpoints:

- `GET /api/v1/grades`
- `GET /api/v1/grades/class/{scheduleId}`
- `POST /api/v1/grades/class/{scheduleId}/encode`
- `POST /api/v1/grades/class/{scheduleId}/submit`
- `POST /api/v1/grades/class/{scheduleId}/review`
- `POST /api/v1/grades/class/{scheduleId}/approve`
- `POST /api/v1/grades/class/{scheduleId}/lock`
- `GET /api/v1/grades/student/{studentId}`

Verified by automated tests:

- Assigned faculty users can encode and submit grades.
- Mismatched or unlinked faculty users cannot encode.
- Invalid grade ranges, increments, and duplicate grade value types are rejected.
- Failed and special grades derive the expected remarks.
- Incomplete class sheets cannot be submitted.
- Review, approval, and lock workflow works.
- Approval and lock update student academic records.
- Locked grades cannot be edited.
- Failed and special grades do not earn units.

### 9. Backend Readiness for Admin Setup Frontend

Backend readiness for the admin setup frontend has now been implemented.

Implemented behavior:

- Added missing setup services/controllers for faculty, rooms, school years, semesters, and sections.
- Completed faculty-user linking through optional `FacultyRequest.userId`.
- Exposed linked user data in `FacultyResponse`.
- Added minimal user management for listing, creating, updating, activating/deactivating, and assigning seeded roles.
- Added role listing for admin setup screens.
- Protected user/role management with `USER_MANAGE`.

Verified by automated tests:

- Faculty create/update works with optional user linking.
- Faculty rejects missing department or user references.
- Room create/update/status works.
- School year and semester create/update works.
- Section create/update/status works and validates references.
- User create/update/status works with role assignment and BCrypt password hashing.
- Duplicate usernames and missing roles are rejected.

### 10. Admin Setup Frontend

The admin setup frontend has now been implemented under `frontend/`.

Implemented screens:

- Login and authenticated admin shell.
- Users with role assignment and active/inactive status toggling.
- Departments, programs, and courses.
- Faculty with optional linked user selection.
- Rooms, school years, semesters, and sections.

Frontend behavior:

- Uses React, TypeScript, and Vite.
- Stores the JWT session in local storage.
- Uses the existing `ApiResponse<T>` and `PageResponse<T>` backend shapes.
- Vite proxies `/api` requests to the backend at `http://localhost:8080`.
- The setup screens use the real backend APIs added for admin readiness.

Run locally:

```powershell
cd frontend
npm install
npm run dev
```

Verify frontend build:

```powershell
cd frontend
npm run build
```

### 11. Registrar and Enrollment Frontend

The first registrar frontend slice has now been implemented in the same Vite app.

Implemented screens:

- Student list/search.
- Student create/update with core personal, contact, and academic fields.
- Student status activation/deactivation.
- Student document upload and verification/rejection.
- Student academic-record read view.
- Enrollment list/status filter.
- Enrollment draft creation.
- Enrollment subject add/drop from active schedules.
- Enrollment validation, confirmation, cancellation, and assessment generation actions.

Backend setup check:

- No additional setup APIs were required for this slice.
- Existing setup, curriculum, student, schedule, enrollment, assessment, and academic-record endpoints were sufficient.

### 12. Curriculum Management Frontend

Curriculum management now has a dedicated frontend screen in the same Vite app.

Implemented screens and actions:

- Curriculum list/search.
- Curriculum header create/update.
- Curriculum activation.
- Curriculum detail view.
- Course assignment add/update/delete.
- Prerequisite and corequisite selection from assigned curriculum courses.
- Read-only checklist view grouped by year level and semester.
- Term totals for lecture hours, laboratory hours, and credit units.

Backend setup check:

- No additional backend APIs or migrations were required.
- Existing curriculum and course endpoints were sufficient.

### 13. Schedule Management Frontend

Schedule management now has a dedicated frontend screen in the same Vite app.

Implemented screens and actions:

- Schedule list/search/filter by term, program, section, faculty, room, course, day, and status.
- Schedule create/update with section, course, faculty, room, school year, semester, capacity, and status.
- Multiple meeting row editor with day, start time, and end time.
- Conflict check action with room, faculty, and section conflict details.
- Soft archive action through the schedule delete endpoint.
- Enrollment schedule dropdown labels now include course, section, faculty, room, and meeting times.

Backend setup check:

- No additional backend APIs or migrations were required.
- Existing schedule, setup, and enrollment endpoints were sufficient.

## What To Build Next

The recommended next slice is the next frontend workflow after schedule management.

Why this is next:

- Admin setup, curriculum, schedule, and first registrar/enrollment data can now be created through the UI.
- Cashier, faculty grade encoding, and reporting workflows still need their own screens before the system feels end-to-end usable.
- Building one workflow at a time keeps the frontend aligned with the existing backend modules.

### Next Module 1: Cashier and Assessment Frontend

Recommended first screens:

- Assessment list/search/filter.
- Assessment detail with itemized fees.
- Recalculate assessment.
- Update assessment status.
- Fee setup and fee rule management if not already comfortable through admin setup.

Suggested frontend stack:

- React with TypeScript.
- Vite.
- Fetch is currently used directly; TanStack Query can be introduced when workflows become more stateful.
- React Hook Form and Zod can be added when form validation becomes heavier.

### Next Module 2: Reports and PDFs

Build after source workflows are stable.

Recommended first reports:

- Student profile report
- Student list report
- Curriculum checklist
- Enrollment form
- Assessment form
- Class list
- Grade sheet
- Grade slip

PDF library:

- Apache PDFBox is already included as a dependency.

### Next Module 3: Audit Logging

Build after the first reports or alongside them if sensitive workflow tracking becomes more urgent.

Recommended first audit events:

- Grade encode, submit, review, approve, and lock.
- Assessment generation and recalculation.
- Student profile update and document verification.

## Current Known Limitations

- Admin setup, curriculum, schedule, and first registrar/enrollment frontend screens exist, but cashier, faculty, student portal, and reporting screens are not built yet.
- Audit logs table exists, but no audit service writes events yet.
- Document storage is local filesystem only.
- Redis is present in Docker Compose but not meaningfully used yet.
- No hard-delete workflows are implemented for academic records.
- No student portal behavior yet, only backend roles/permissions.

## Suggested First Prompt For New Chat

Use this prompt in a new chat:

```text
We are building a Spring Boot 3 / Java 21 Student Information System backend in C:\Users\Technical Support\Documents\GitHub\cis.

Please read TODAY_PROGRESS_AND_NEXT_STEPS.md and README.md first.

Current completed modules:
- Auth/JWT/RBAC
- Academic setup
- Curriculum management
- Student profile management
- Schedule management and conflict checking
- Enrollment management
- Fee setup and assessment generation
- Grade recording and academic record updates
- Backend readiness for admin setup frontend
- Admin setup frontend
- Curriculum management frontend
- Schedule management frontend
- Registrar and enrollment frontend

Next task:
Implement the next frontend workflow, starting with cashier and assessment screens.

Please create a plan first if we are in Plan Mode, otherwise implement it directly, run backend tests, run the frontend build, start the backend, start the frontend dev server, and manually verify the new screens.
```
