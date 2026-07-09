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

## Implemented Endpoints

Auth:

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/me`

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

## Verify

```powershell
mvn test
```

## Next Implementation Slice

1. Implement schedule and conflict-check APIs.
2. Implement enrollment and assessment workflows.
3. Implement grade recording and academic record updates.
