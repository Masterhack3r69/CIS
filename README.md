# Student Information System MVP

Spring Boot 3 / Java 21 backend scaffold for the college SIS described in `PROJECT_CONTEXT.md`.

## Implemented In This Slice

- Spring Boot backend with Maven
- PostgreSQL and Flyway migration setup
- Docker Compose for backend, PostgreSQL, and Redis
- Standard API response envelope
- Global validation and exception handling
- JWT login, refresh, logout, and current-user endpoints
- Users, roles, permissions, role-permission mapping, and refresh tokens
- Seeded roles and permissions
- Seeded super admin account
- Academic setup entities and CRUD APIs for departments, programs, courses, faculty, rooms, school years, semesters, and sections
- Curriculum management APIs with version activation, course assignment, prerequisites, corequisites, and checklist totals
- Student profile APIs with program/curriculum assignment, nested profile details, document upload metadata, verification, and student search
- Schedule management APIs with class meetings, filtering, soft archive, and room/faculty/section conflict checking
- Enrollment management APIs with draft enrollment, schedule selection, validation, confirmation, cancellation, and status history
- Fee setup and assessment APIs with fixed/per-unit fee rules, assessment generation, recalculation, and status tracking
- Grade recording APIs with faculty-owned encoding, review, approval, locking, and academic record updates
- Backend readiness APIs for admin setup frontend, including faculty-user linking and user/role management
- React/Vite admin setup frontend for users, departments, programs, courses, faculty, rooms, school years, semesters, and sections
- Registrar frontend screens for student profiles, documents, academic records, enrollment drafts, subject selection, validation, confirmation, cancellation, and assessment generation
- Audit/report tracking tables reserved for later workflows

## Local Run

Start PostgreSQL and the backend:

```powershell
docker compose up --build
```

Default seeded admin:

```text
username: admin
password: admin123
email: admin@sis.local
```

Login:

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "admin",
  "password": "admin123"
}
```

Run the admin setup frontend:

```powershell
cd frontend
npm install
npm run dev
```

The Vite app runs at `http://localhost:5173` and proxies `/api` calls to `http://localhost:8080`.

## Implemented Endpoints

Auth:

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/me`

Users and roles:

- `GET /api/v1/users`
- `POST /api/v1/users`
- `GET /api/v1/users/{id}`
- `PUT /api/v1/users/{id}`
- `PATCH /api/v1/users/{id}/status`
- `GET /api/v1/roles`

Academic setup:

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

Curriculum:

- `GET /api/v1/curricula`
- `POST /api/v1/curricula`
- `GET /api/v1/curricula/{id}`
- `PUT /api/v1/curricula/{id}`
- `POST /api/v1/curricula/{id}/courses`
- `PUT /api/v1/curricula/{id}/courses/{curriculumCourseId}`
- `DELETE /api/v1/curricula/{id}/courses/{curriculumCourseId}`
- `GET /api/v1/curricula/{id}/checklist`
- `POST /api/v1/curricula/{id}/activate`

Students:

- `GET /api/v1/students`
- `POST /api/v1/students`
- `GET /api/v1/students/{id}`
- `PUT /api/v1/students/{id}`
- `PATCH /api/v1/students/{id}/status`
- `POST /api/v1/students/{id}/documents`
- `GET /api/v1/students/{id}/documents`
- `PATCH /api/v1/students/{id}/documents/{documentId}/verify`
- `GET /api/v1/students/{id}/academic-records`

Schedules:

- `GET /api/v1/schedules`
- `POST /api/v1/schedules`
- `GET /api/v1/schedules/{id}`
- `PUT /api/v1/schedules/{id}`
- `DELETE /api/v1/schedules/{id}`
- `POST /api/v1/schedules/check-conflict`

Enrollments:

- `GET /api/v1/enrollments`
- `POST /api/v1/enrollments`
- `GET /api/v1/enrollments/{id}`
- `PUT /api/v1/enrollments/{id}`
- `POST /api/v1/enrollments/{id}/subjects`
- `DELETE /api/v1/enrollments/{id}/subjects/{subjectId}`
- `POST /api/v1/enrollments/{id}/validate`
- `POST /api/v1/enrollments/{id}/confirm`
- `POST /api/v1/enrollments/{id}/cancel`
- `POST /api/v1/enrollments/{id}/generate-assessment`

Fees and assessments:

- `GET /api/v1/fees`
- `POST /api/v1/fees`
- `GET /api/v1/fees/{id}`
- `PUT /api/v1/fees/{id}`
- `PATCH /api/v1/fees/{id}/status`
- `GET /api/v1/assessments`
- `GET /api/v1/assessments/{id}`
- `POST /api/v1/assessments/{id}/recalculate`
- `PATCH /api/v1/assessments/{id}/status`

Grades:

- `GET /api/v1/grades`
- `GET /api/v1/grades/class/{scheduleId}`
- `POST /api/v1/grades/class/{scheduleId}/encode`
- `POST /api/v1/grades/class/{scheduleId}/submit`
- `POST /api/v1/grades/class/{scheduleId}/review`
- `POST /api/v1/grades/class/{scheduleId}/approve`
- `POST /api/v1/grades/class/{scheduleId}/lock`
- `GET /api/v1/grades/student/{studentId}`

## Verify

```powershell
mvn test
cd frontend
npm run build
```

## Next Implementation Slice

1. Build the next frontend workflow, starting with schedule management or cashier/assessment screens.
2. Implement reports and PDF generation, starting with enrollment, assessment, class list, and grade slip forms.
3. Add audit logging for sensitive workflows.
