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
- Academic setup entities and CRUD APIs for departments, programs, and courses
- Setup persistence model for school years, semesters, rooms, faculty, and sections
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

## Verify

```powershell
mvn test
```

## Next Implementation Slice

1. Add full CRUD controllers for faculty, school years, semesters, rooms, and sections.
2. Implement curriculum entities, activation behavior, and checklist API.
3. Implement student profile entities and APIs.
4. Implement schedule conflict checks.
5. Implement enrollment, assessment, and grade workflows.
