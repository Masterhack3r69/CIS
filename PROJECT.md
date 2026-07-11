# Project: Academic Setup Frontend

## Architecture
The Academic Setup module provides the structural foundation for the College Student Information System (SIS). The frontend is built as a React v19 Single Page Application (SPA) using:
- **Tailwind CSS v4** for styling
- **React Router v7** for routing
- **TanStack React Query v5** for API client caching and synchronization
- **React Hook Form + Zod** for client-side validation
- **shadcn/ui** components for the layout

The backend runs as a Spring Boot application. All frontend requests starting with `/api` are proxied to the backend at `http://localhost:8080`.

## Code Layout
- TypeScript types: `frontend/src/lib/types.ts`
- Query/Mutation hooks: `frontend/src/hooks/use-setup.ts`
- App routes/sidebar: `frontend/src/App.tsx`
- Parent Layout component: `frontend/src/pages/setup/setup-layout.tsx`
- Setup sub-module tab pages:
  - Departments: `frontend/src/pages/setup/departments-tab.tsx`
  - Programs: `frontend/src/pages/setup/programs-tab.tsx`
  - Courses: `frontend/src/pages/setup/courses-tab.tsx`
  - Faculty: `frontend/src/pages/setup/faculty-tab.tsx`
  - Rooms: `frontend/src/pages/setup/rooms-tab.tsx`
  - School Years: `frontend/src/pages/setup/school-years-tab.tsx`
  - Semesters: `frontend/src/pages/setup/semesters-tab.tsx`
  - Sections: `frontend/src/pages/setup/sections-tab.tsx`

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|---|---|---|---|
| 1 | Shared Layout & Navigation | Setup `/setup` layout, sidebar link, routing, and blank tabs | None | PLANNED |
| 2 | Base Master Data CRUD | School Years, Semesters, Rooms, Departments CRUD | M1 | PLANNED |
| 3 | Department-Linked Data CRUD | Programs, Courses, Faculty CRUD | M2 | PLANNED |
| 4 | Operational Data CRUD | Sections CRUD & Final Integration | M3 | PLANNED |
| 5 | E2E Verification & Hardening | E2E Test Suite verification (Tiers 1-4) & adversarial hardening (Tier 5) | M4, E2E Testing Track | PLANNED |

## Interface Contracts

### 1. Data Enums
- **`ActiveStatus`**: `"ACTIVE" | "INACTIVE"`
- **`DegreeType`**: `"BACHELOR" | "ASSOCIATE" | "DIPLOMA" | "CERTIFICATE" | "GRADUATE_PROGRAM"`
- **`CourseType`**: `"MAJOR" | "PROFESSIONAL_COURSE" | "GENERAL_EDUCATION" | "PHYSICAL_EDUCATION" | "NSTP" | "ELECTIVE" | "LABORATORY" | "SEMINAR" | "THESIS_CAPSTONE"`
- **`EmploymentStatus`**: `"FULL_TIME" | "PART_TIME" | "CONTRACTUAL" | "VISITING_LECTURER" | "INACTIVE"`
- **`FacultyType`**: `"INSTRUCTOR" | "PROFESSOR" | "LECTURER" | "DEAN" | "PROGRAM_HEAD"`

### 2. API Endpoint Matrix

| Module | Method | Path | Request Payload (Validation) | Response Schema | Required Permission |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Departments** | `GET` | `/api/v1/departments` | Parameters: `search` (Optional String), `page`, `size` | `ApiResponse<PageResponse<Department>>` | `ACADEMIC_SETUP_VIEW` |
| | `GET` | `/api/v1/departments/{id}` | N/A | `ApiResponse<Department>` | `ACADEMIC_SETUP_VIEW` |
| | `POST` | `/api/v1/departments` | `DepartmentRequest` | `ApiResponse<Department>` | `ACADEMIC_SETUP_MANAGE` |
| | `PUT` | `/api/v1/departments/{id}` | `DepartmentRequest` | `ApiResponse<Department>` | `ACADEMIC_SETUP_MANAGE` |
| | `PATCH` | `/api/v1/departments/{id}/status` | `{"status": ActiveStatus}` | `ApiResponse<Department>` | `ACADEMIC_SETUP_MANAGE` |
| **Programs** | `GET` | `/api/v1/programs` | Parameters: `search` (Optional String), `page`, `size` | `ApiResponse<PageResponse<Program>>` | `ACADEMIC_SETUP_VIEW` |
| | `GET` | `/api/v1/programs/{id}` | N/A | `ApiResponse<Program>` | `ACADEMIC_SETUP_VIEW` |
| | `POST` | `/api/v1/programs` | `ProgramRequest` | `ApiResponse<Program>` | `ACADEMIC_SETUP_MANAGE` |
| | `PUT` | `/api/v1/programs/{id}` | `ProgramRequest` | `ApiResponse<Program>` | `ACADEMIC_SETUP_MANAGE` |
| **Courses** | `GET` | `/api/v1/courses` | Parameters: `search` (Optional String), `page`, `size` | `ApiResponse<PageResponse<Course>>` | `ACADEMIC_SETUP_VIEW` |
| | `GET` | `/api/v1/courses/{id}` | N/A | `ApiResponse<Course>` | `ACADEMIC_SETUP_VIEW` |
| | `POST` | `/api/v1/courses` | `CourseRequest` | `ApiResponse<Course>` | `ACADEMIC_SETUP_MANAGE` |
| | `PUT` | `/api/v1/courses/{id}` | `CourseRequest` | `ApiResponse<Course>` | `ACADEMIC_SETUP_MANAGE` |
| **Faculty** | `GET` | `/api/v1/faculty` | Parameters: `search` (Optional String), `page`, `size` | `ApiResponse<PageResponse<Faculty>>` | `ACADEMIC_SETUP_VIEW` |
| | `GET` | `/api/v1/faculty/{id}` | N/A | `ApiResponse<Faculty>` | `ACADEMIC_SETUP_VIEW` |
| | `POST` | `/api/v1/faculty` | `FacultyRequest` | `ApiResponse<Faculty>` | `ACADEMIC_SETUP_MANAGE` |
| | `PUT` | `/api/v1/faculty/{id}` | `FacultyRequest` | `ApiResponse<Faculty>` | `ACADEMIC_SETUP_MANAGE` |
| | `PATCH` | `/api/v1/faculty/{id}/status` | `{"status": ActiveStatus}` | `ApiResponse<Faculty>` | `ACADEMIC_SETUP_MANAGE` |
| **Rooms** | `GET` | `/api/v1/rooms` | Parameters: `search` (Optional String), `page`, `size` | `ApiResponse<PageResponse<Room>>` | `ACADEMIC_SETUP_VIEW` |
| | `GET` | `/api/v1/rooms/{id}` | N/A | `ApiResponse<Room>` | `ACADEMIC_SETUP_VIEW` |
| | `POST` | `/api/v1/rooms` | `RoomRequest` | `ApiResponse<Room>` | `ACADEMIC_SETUP_MANAGE` |
| | `PUT` | `/api/v1/rooms/{id}` | `RoomRequest` | `ApiResponse<Room>` | `ACADEMIC_SETUP_MANAGE` |
| | `PATCH` | `/api/v1/rooms/{id}/status` | `{"status": ActiveStatus}` | `ApiResponse<Room>` | `ACADEMIC_SETUP_MANAGE` |
| **School Years** | `GET` | `/api/v1/school-years` | Parameters: `page`, `size` | `ApiResponse<PageResponse<SchoolYear>>` | `ACADEMIC_SETUP_VIEW` |
| | `GET` | `/api/v1/school-years/{id}` | N/A | `ApiResponse<SchoolYear>` | `ACADEMIC_SETUP_VIEW` |
| | `POST` | `/api/v1/school-years` | `SchoolYearRequest` | `ApiResponse<SchoolYear>` | `ACADEMIC_SETUP_MANAGE` |
| | `PUT` | `/api/v1/school-years/{id}` | `SchoolYearRequest` | `ApiResponse<SchoolYear>` | `ACADEMIC_SETUP_MANAGE` |
| **Semesters** | `GET` | `/api/v1/semesters` | Parameters: `page`, `size` | `ApiResponse<PageResponse<Semester>>` | `ACADEMIC_SETUP_VIEW` |
| | `GET` | `/api/v1/semesters/{id}` | N/A | `ApiResponse<Semester>` | `ACADEMIC_SETUP_VIEW` |
| | `POST` | `/api/v1/semesters` | `SemesterRequest` | `ApiResponse<Semester>` | `ACADEMIC_SETUP_MANAGE` |
| | `PUT` | `/api/v1/semesters/{id}` | `SemesterRequest` | `ApiResponse<Semester>` | `ACADEMIC_SETUP_MANAGE` |
| **Sections** | `GET` | `/api/v1/sections` | Parameters: `search` (Optional String), `page`, `size` | `ApiResponse<PageResponse<Section>>` | `ACADEMIC_SETUP_VIEW` |
| | `GET` | `/api/v1/sections/{id}` | N/A | `ApiResponse<Section>` | `ACADEMIC_SETUP_VIEW` |
| | `POST` | `/api/v1/sections` | `SectionRequest` | `ApiResponse<Section>` | `ACADEMIC_SETUP_MANAGE` |
| | `PUT` | `/api/v1/sections/{id}` | `SectionRequest` | `ApiResponse<Section>` | `ACADEMIC_SETUP_MANAGE` |
| | `PATCH` | `/api/v1/sections/{id}/status` | `{"status": ActiveStatus}` | `ApiResponse<Section>` | `ACADEMIC_SETUP_MANAGE` |

### 3. Verification Constraints (Zod Validations)
All frontend forms must validate inputs to match the backend JSR-380 annotations:
- **Code/Name fields**: Must not be blank (e.g. `@NotBlank` -> `z.string().min(1, "Required")`).
- **Emails**: Must be valid syntax (e.g. `@Email` -> `z.string().email("Invalid email")`).
- **Hours/Units**: Must be non-negative (e.g. `@DecimalMin("0.0")` -> `z.coerce.number().min(0, "Must be >= 0")`).
- **Min values**: sortOrder and yearLevel must be >= 1 (e.g. `@Min(1)` -> `z.coerce.number().min(1, "Must be >= 1")`).
