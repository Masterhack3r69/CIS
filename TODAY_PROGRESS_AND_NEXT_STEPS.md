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

Auth endpoints:

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/me`

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

Important note:

- Do not edit already-applied migrations for normal feature work.
- Add new migrations such as `V4__...sql` for the next module.

## Verified Commands

The test suite was run successfully:

```powershell
mvn test
```

The backend was manually run and tested at:

```text
http://localhost:8080
```

## What To Build Next

The recommended next slice is Schedule Management and Conflict Checking.

Why this is next:

- Programs, courses, faculty, rooms, school years, semesters, and sections already exist.
- Curricula exist.
- Students exist.
- Enrollment depends on available class schedules.
- Grade encoding depends on class schedules and faculty assignments.

### Next Module 1: Schedule Management

Implement this next.

Suggested migration:

- `V4__schedule_management.sql`

Suggested package:

- `src/main/java/com/school/sis/schedule`

Tables to add:

- `class_schedules`
- `schedule_meetings`

Suggested fields for `class_schedules`:

- `id`
- `section_id`
- `course_id`
- `faculty_id`
- `room_id`
- `school_year_id`
- `semester_id`
- `capacity`
- `status`
- `created_at`
- `updated_at`

Suggested fields for `schedule_meetings`:

- `id`
- `class_schedule_id`
- `day_of_week`
- `start_time`
- `end_time`
- `created_at`
- `updated_at`

Suggested enums:

- `ScheduleStatus`: `DRAFT`, `ACTIVE`, `CANCELLED`, `ARCHIVED`
- `DayOfWeek`: use Java `java.time.DayOfWeek` or a local enum matching `MONDAY` through `SUNDAY`.

Suggested permissions:

- Add `SCHEDULE_VIEW`
- Add `SCHEDULE_MANAGE`
- Grant both to `SUPER_ADMIN`, `REGISTRAR`
- Grant view to `DEAN`, `PROGRAM_HEAD`, `FACULTY`

Endpoints to implement:

- `GET /api/v1/schedules`
- `POST /api/v1/schedules`
- `GET /api/v1/schedules/{id}`
- `PUT /api/v1/schedules/{id}`
- `DELETE /api/v1/schedules/{id}`
- `POST /api/v1/schedules/check-conflict`

Required behavior:

- Validate section, course, faculty, room, school year, and semester exist.
- Validate each meeting has day, start time, and end time.
- Reject meeting where end time is not after start time.
- Check conflicts against active schedules:
  - same room + same day + overlapping time
  - same faculty + same day + overlapping time
  - same section + same day + overlapping time
- Allow updates to ignore the schedule currently being edited.
- Return clear conflict details from `/check-conflict`.

Manual test scenario:

1. Login as admin.
2. Create section, room, faculty, course if needed.
3. Create a schedule with one meeting.
4. Create another schedule with same room and overlapping time and confirm conflict.
5. Create another schedule with same faculty and overlapping time and confirm conflict.
6. Create another schedule with same section and overlapping time and confirm conflict.
7. Create non-overlapping schedule and confirm it succeeds.
8. Confirm unauthenticated access returns `401`.
9. Confirm validation failures return `400`.

### Next Module 2: Enrollment

Build after schedules.

Suggested migration:

- `V5__enrollment_management.sql`

Tables:

- `enrollments`
- `enrollment_subjects`
- `enrollment_status_history`

Core behavior:

- Create enrollment for student, school year, semester.
- Prevent duplicate active enrollment for same student/school year/semester.
- Load curriculum subjects.
- Add/drop selected schedules.
- Validate schedule conflicts.
- Validate prerequisites later using grades when grade data exists.
- Confirm enrollment.
- Track enrollment statuses.

Suggested endpoints:

- `GET /api/v1/enrollments`
- `POST /api/v1/enrollments`
- `GET /api/v1/enrollments/{id}`
- `PUT /api/v1/enrollments/{id}`
- `POST /api/v1/enrollments/{id}/subjects`
- `DELETE /api/v1/enrollments/{id}/subjects/{subjectId}`
- `POST /api/v1/enrollments/{id}/validate`
- `POST /api/v1/enrollments/{id}/confirm`
- `POST /api/v1/enrollments/{id}/cancel`

### Next Module 3: Fees and Assessment

Build after enrollment basics.

Tables:

- `fee_items`
- `fee_rules`
- `assessments`
- `assessment_items`
- optional `payments`

Core behavior:

- Manage fee setup.
- Generate assessment from enrollment subjects.
- Compute per-unit and fixed fees.
- Recalculate assessment.
- Track assessment status.

Suggested endpoints:

- `GET /api/v1/fees`
- `POST /api/v1/fees`
- `GET /api/v1/fees/{id}`
- `PUT /api/v1/fees/{id}`
- `PATCH /api/v1/fees/{id}/status`
- `GET /api/v1/assessments`
- `GET /api/v1/assessments/{id}`
- `POST /api/v1/assessments/{id}/recalculate`
- `PATCH /api/v1/assessments/{id}/status`

### Next Module 4: Grade Recording

Build after schedules and enrollment.

Tables:

- `grades`
- `grade_status_history`
- later: `grade_change_requests`

Core behavior:

- Faculty class list.
- Grade encoding.
- Grade submission.
- Registrar approval.
- Grade locking.
- Update academic records when grade is approved/locked.

Suggested endpoints:

- `GET /api/v1/grades`
- `GET /api/v1/grades/class/{scheduleId}`
- `POST /api/v1/grades/class/{scheduleId}/encode`
- `POST /api/v1/grades/class/{scheduleId}/submit`
- `POST /api/v1/grades/class/{scheduleId}/approve`
- `POST /api/v1/grades/class/{scheduleId}/lock`
- `GET /api/v1/grades/student/{studentId}`

### Next Module 5: Reports and PDFs

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

## Current Known Limitations

- No frontend has been built yet.
- No schedule module yet.
- No enrollment module yet.
- No fee/assessment module yet.
- No grade recording module yet.
- Academic records endpoint exists only as an empty placeholder.
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

Next task:
Implement Schedule Management and Conflict Checking as described in TODAY_PROGRESS_AND_NEXT_STEPS.md.

Please create a plan first if we are in Plan Mode, otherwise implement it directly, run mvn test, restart the backend, and manually verify the schedule endpoints.
```

