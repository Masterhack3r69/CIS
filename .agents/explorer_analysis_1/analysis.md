# Academic Setup Module — Backend API & Database Schemas Analysis

## Executive Summary
The Academic Setup module provides the structural foundation for the College Student Information System (SIS), defining critical reference tables (Departments, Programs, Courses, Faculty, Rooms, School Years, Semesters, Sections) used across Enrollment, Scheduling, and Grading modules. Access to all setup records is governed by `ACADEMIC_SETUP_VIEW` and `ACADEMIC_SETUP_MANAGE` permissions, implemented with a consistent Spring Boot API layer and a structured PostgreSQL relational schema.

---

## 1. Database Architecture (Schemas & Constraints)

All academic setup tables are managed within the PostgreSQL database. The schema leverages UUIDs as primary keys and maintains strict foreign key relationships, active/inactive enums, and unique checks.

### 1.1 Support Enums
The following PostgreSQL types (stored as standard string fields in PostgreSQL and mapped as enums in Java JPA) define valid boundaries for fields in the entities:

*   **`ActiveStatus`**: Mapped from string columns. Used for soft active/inactive state in Departments, Programs, Courses, Faculty, Rooms, and Sections.
    *   Values: `ACTIVE`, `INACTIVE`
*   **`DegreeType`**: Mapped from string columns. Used in Programs.
    *   Values: `BACHELOR`, `ASSOCIATE`, `DIPLOMA`, `CERTIFICATE`, `GRADUATE_PROGRAM`
*   **`CourseType`**: Mapped from string columns. Used in Courses.
    *   Values: `MAJOR`, `PROFESSIONAL_COURSE`, `GENERAL_EDUCATION`, `PHYSICAL_EDUCATION`, `NSTP`, `ELECTIVE`, `LABORATORY`, `SEMINAR`, `THESIS_CAPSTONE`
*   **`EmploymentStatus`**: Mapped from string columns. Used in Faculty.
    *   Values: `FULL_TIME`, `PART_TIME`, `CONTRACTUAL`, `VISITING_LECTURER`, `INACTIVE`
*   **`FacultyType`**: Mapped from string columns. Used in Faculty.
    *   Values: `INSTRUCTOR`, `PROFESSOR`, `LECTURER`, `DEAN`, `PROGRAM_HEAD`

---

### 1.2 Table Specifications

#### 1.2.1 `departments`
Stores academic department data (e.g., College of Engineering).
*   **Columns**:
    *   `id`: `UUID` (Primary Key)
    *   `department_code`: `VARCHAR(40)` (Not Null, Unique)
    *   `department_name`: `TEXT` (Not Null, Unique)
    *   `dean`: `TEXT` (Nullable)
    *   `description`: `TEXT` (Nullable)
    *   `status`: `VARCHAR(20)` (Not Null, Default `'ACTIVE'`, maps to `ActiveStatus`)
    *   `created_at`: `TIMESTAMPTZ` (Not Null, Default `now()`)
    *   `updated_at`: `TIMESTAMPTZ` (Not Null, Default `now()`)

#### 1.2.2 `programs`
Stores degree programs housed under specific departments.
*   **Columns**:
    *   `id`: `UUID` (Primary Key)
    *   `program_code`: `VARCHAR(40)` (Not Null, Unique)
    *   `program_name`: `TEXT` (Not Null)
    *   `department_id`: `UUID` (Not Null, Foreign Key referencing `departments(id)`)
    *   `degree_type`: `VARCHAR(40)` (Not Null, maps to `DegreeType`)
    *   `program_duration`: `INTEGER` (Nullable)
    *   `description`: `TEXT` (Nullable)
    *   `status`: `VARCHAR(20)` (Not Null, Default `'ACTIVE'`, maps to `ActiveStatus`)
    *   `created_at`: `TIMESTAMPTZ` (Not Null, Default `now()`)
    *   `updated_at`: `TIMESTAMPTZ` (Not Null, Default `now()`)

#### 1.2.3 `courses`
Stores courses/subjects offered by departments.
*   **Columns**:
    *   `id`: `UUID` (Primary Key)
    *   `course_code`: `VARCHAR(40)` (Not Null, Unique)
    *   `course_title`: `TEXT` (Not Null)
    *   `course_description`: `TEXT` (Nullable)
    *   `lecture_hours_per_week`: `NUMERIC(8, 2)` (Not Null, Default `0`)
    *   `laboratory_hours_per_week`: `NUMERIC(8, 2)` (Not Null, Default `0`)
    *   `credit_units`: `NUMERIC(8, 2)` (Not Null, Default `0`)
    *   `course_type`: `VARCHAR(40)` (Not Null, maps to `CourseType`)
    *   `department_id`: `UUID` (Not Null, Foreign Key referencing `departments(id)`)
    *   `status`: `VARCHAR(20)` (Not Null, Default `'ACTIVE'`, maps to `ActiveStatus`)
    *   `created_at`: `TIMESTAMPTZ` (Not Null, Default `now()`)
    *   `updated_at`: `TIMESTAMPTZ` (Not Null, Default `now()`)
*   **Table-level Constraints**:
    *   `courses_non_negative_hours CHECK (lecture_hours_per_week >= 0 AND laboratory_hours_per_week >= 0 AND credit_units >= 0)`

#### 1.2.4 `school_years`
Defines academic years (e.g., "2025-2026").
*   **Columns**:
    *   `id`: `UUID` (Primary Key)
    *   `school_year`: `VARCHAR(20)` (Not Null, Unique)
    *   `active`: `BOOLEAN` (Not Null, Default `false`)
    *   `created_at`: `TIMESTAMPTZ` (Not Null, Default `now()`)
    *   `updated_at`: `TIMESTAMPTZ` (Not Null, Default `now()`)

#### 1.2.5 `semesters`
Defines academic terms within a school year.
*   **Columns**:
    *   `id`: `UUID` (Primary Key)
    *   `name`: `VARCHAR(40)` (Not Null, Unique, e.g., `FIRST_SEMESTER`, `SECOND_SEMESTER`, `SUMMER`)
    *   `sort_order`: `INTEGER` (Not Null)
    *   `active`: `BOOLEAN` (Not Null, Default `true`)
    *   `created_at`: `TIMESTAMPTZ` (Not Null, Default `now()`)
    *   `updated_at`: `TIMESTAMPTZ` (Not Null, Default `now()`)

#### 1.2.6 `rooms`
Defines physical classrooms or laboratories.
*   **Columns**:
    *   `id`: `UUID` (Primary Key)
    *   `room_code`: `VARCHAR(40)` (Not Null, Unique)
    *   `room_name`: `TEXT` (Not Null)
    *   `capacity`: `INTEGER` (Nullable)
    *   `status`: `VARCHAR(20)` (Not Null, Default `'ACTIVE'`, maps to `ActiveStatus`)
    *   `created_at`: `TIMESTAMPTZ` (Not Null, Default `now()`)
    *   `updated_at`: `TIMESTAMPTZ` (Not Null, Default `now()`)

#### 1.2.7 `faculty`
Defines teacher profile records.
*   **Columns**:
    *   `id`: `UUID` (Primary Key)
    *   `employee_number`: `VARCHAR(60)` (Not Null, Unique)
    *   `first_name`: `TEXT` (Not Null)
    *   `middle_name`: `TEXT` (Nullable)
    *   `last_name`: `TEXT` (Not Null)
    *   `suffix`: `TEXT` (Nullable)
    *   `email`: `TEXT` (Not Null, Unique)
    *   `contact_number`: `TEXT` (Nullable)
    *   `department_id`: `UUID` (Not Null, Foreign Key referencing `departments(id)`)
    *   `employment_status`: `VARCHAR(40)` (Not Null, maps to `EmploymentStatus`)
    *   `faculty_type`: `VARCHAR(40)` (Not Null, maps to `FacultyType`)
    *   `specialization`: `TEXT` (Nullable)
    *   `status`: `VARCHAR(20)` (Not Null, Default `'ACTIVE'`, maps to `ActiveStatus`)
    *   `created_at`: `TIMESTAMPTZ` (Not Null, Default `now()`)
    *   `updated_at`: `TIMESTAMPTZ` (Not Null, Default `now()`)

#### 1.2.8 `sections`
Represents standard student cohorts for courses/classes during a specific term.
*   **Columns**:
    *   `id`: `UUID` (Primary Key)
    *   `section_code`: `VARCHAR(60)` (Not Null)
    *   `program_id`: `UUID` (Not Null, Foreign Key referencing `programs(id)`)
    *   `school_year_id`: `UUID` (Not Null, Foreign Key referencing `school_years(id)`)
    *   `semester_id`: `UUID` (Not Null, Foreign Key referencing `semesters(id)`)
    *   `year_level`: `INTEGER` (Not Null)
    *   `status`: `VARCHAR(20)` (Not Null, Default `'ACTIVE'`, maps to `ActiveStatus`)
    *   `created_at`: `TIMESTAMPTZ` (Not Null, Default `now()`)
    *   `updated_at`: `TIMESTAMPTZ` (Not Null, Default `now()`)
*   **Table-level Constraints**:
    *   `sections_unique_term UNIQUE (section_code, school_year_id, semester_id)`

---

## 2. API Architecture

All endpoints are built using Spring Boot REST controllers. They follow a predictable API wrapping pattern using `ApiResponse<T>` and `PageResponse<T>` wrappers.

### 2.1 Standard API Response Schemas

#### Happy Path Responses
*   **Paginated Success Response** (`ApiResponse<PageResponse<T>>`):
    ```json
    {
      "success": true,
      "message": "Resource list retrieved successfully",
      "data": {
        "items": [ ... ],
        "page": 0,
        "size": 20,
        "totalElements": 100,
        "totalPages": 5
      },
      "timestamp": "2026-07-11T07:40:31Z"
    }
    ```
*   **Single Resource Success Response** (`ApiResponse<T>`):
    ```json
    {
      "success": true,
      "message": "Resource retrieved successfully",
      "data": { ... },
      "timestamp": "2026-07-11T07:40:31Z"
    }
    ```

#### Error Path Responses
*   **Validation Failure** (HTTP 400 - `ApiResponse<Void>`):
    ```json
    {
      "success": false,
      "message": "Validation failed",
      "errors": [
        {
          "field": "code",
          "message": "must not be blank"
        }
      ],
      "timestamp": "2026-07-11T07:40:31Z"
    }
    ```
*   **Business Rule/Not Found/Unauthorized/Forbidden** (HTTP 400/404/401/403):
    ```json
    {
      "success": false,
      "message": "Error details here",
      "timestamp": "2026-07-11T07:40:31Z"
    }
    ```

---

### 2.2 Endpoint Matrix

| Module | Method | Path | Request Payload (Validation) | Response Schema | Required Permission |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Departments** | `GET` | `/api/v1/departments` | Parameters: `search` (Optional String), `page`, `size`, `sort` | `ApiResponse<PageResponse<DepartmentResponse>>` | `ACADEMIC_SETUP_VIEW` |
| | `GET` | `/api/v1/departments/{id}` | N/A | `ApiResponse<DepartmentResponse>` | `ACADEMIC_SETUP_VIEW` |
| | `POST` | `/api/v1/departments` | `DepartmentRequest` | `ApiResponse<DepartmentResponse>` | `ACADEMIC_SETUP_MANAGE` |
| | `PUT` | `/api/v1/departments/{id}` | `DepartmentRequest` | `ApiResponse<DepartmentResponse>` | `ACADEMIC_SETUP_MANAGE` |
| | `PATCH` | `/api/v1/departments/{id}/status` | `{"status": "ACTIVE"/"INACTIVE"}` | `ApiResponse<DepartmentResponse>` | `ACADEMIC_SETUP_MANAGE` |
| **Programs** | `GET` | `/api/v1/programs` | Parameters: `search` (Optional String), `page`, `size`, `sort` | `ApiResponse<PageResponse<ProgramResponse>>` | `ACADEMIC_SETUP_VIEW` |
| | `GET` | `/api/v1/programs/{id}` | N/A | `ApiResponse<ProgramResponse>` | `ACADEMIC_SETUP_VIEW` |
| | `POST` | `/api/v1/programs` | `ProgramRequest` | `ApiResponse<ProgramResponse>` | `ACADEMIC_SETUP_MANAGE` |
| | `PUT` | `/api/v1/programs/{id}` | `ProgramRequest` | `ApiResponse<ProgramResponse>` | `ACADEMIC_SETUP_MANAGE` |
| **Courses** | `GET` | `/api/v1/courses` | Parameters: `search` (Optional String), `page`, `size`, `sort` | `ApiResponse<PageResponse<CourseResponse>>` | `ACADEMIC_SETUP_VIEW` |
| | `GET` | `/api/v1/courses/{id}` | N/A | `ApiResponse<CourseResponse>` | `ACADEMIC_SETUP_VIEW` |
| | `POST` | `/api/v1/courses` | `CourseRequest` | `ApiResponse<CourseResponse>` | `ACADEMIC_SETUP_MANAGE` |
| | `PUT` | `/api/v1/courses/{id}` | `CourseRequest` | `ApiResponse<CourseResponse>` | `ACADEMIC_SETUP_MANAGE` |
| **Faculty** | `GET` | `/api/v1/faculty` | Parameters: `search` (Optional String), `page`, `size`, `sort` | `ApiResponse<PageResponse<FacultyResponse>>` | `ACADEMIC_SETUP_VIEW` |
| | `GET` | `/api/v1/faculty/{id}` | N/A | `ApiResponse<FacultyResponse>` | `ACADEMIC_SETUP_VIEW` |
| | `POST` | `/api/v1/faculty` | `FacultyRequest` | `ApiResponse<FacultyResponse>` | `ACADEMIC_SETUP_MANAGE` |
| | `PUT` | `/api/v1/faculty/{id}` | `FacultyRequest` | `ApiResponse<FacultyResponse>` | `ACADEMIC_SETUP_MANAGE` |
| | `PATCH` | `/api/v1/faculty/{id}/status` | `{"status": "ACTIVE"/"INACTIVE"}` | `ApiResponse<FacultyResponse>` | `ACADEMIC_SETUP_MANAGE` |
| **Rooms** | `GET` | `/api/v1/rooms` | Parameters: `search` (Optional String), `page`, `size`, `sort` | `ApiResponse<PageResponse<RoomResponse>>` | `ACADEMIC_SETUP_VIEW` |
| | `GET` | `/api/v1/rooms/{id}` | N/A | `ApiResponse<RoomResponse>` | `ACADEMIC_SETUP_VIEW` |
| | `POST` | `/api/v1/rooms` | `RoomRequest` | `ApiResponse<RoomResponse>` | `ACADEMIC_SETUP_MANAGE` |
| | `PUT` | `/api/v1/rooms/{id}` | `RoomRequest` | `ApiResponse<RoomResponse>` | `ACADEMIC_SETUP_MANAGE` |
| | `PATCH` | `/api/v1/rooms/{id}/status` | `{"status": "ACTIVE"/"INACTIVE"}` | `ApiResponse<RoomResponse>` | `ACADEMIC_SETUP_MANAGE` |
| **School Years** | `GET` | `/api/v1/school-years` | Parameters: `page`, `size`, `sort` | `ApiResponse<PageResponse<SchoolYearResponse>>` | `ACADEMIC_SETUP_VIEW` |
| | `GET` | `/api/v1/school-years/{id}` | N/A | `ApiResponse<SchoolYearResponse>` | `ACADEMIC_SETUP_VIEW` |
| | `POST` | `/api/v1/school-years` | `SchoolYearRequest` | `ApiResponse<SchoolYearResponse>` | `ACADEMIC_SETUP_MANAGE` |
| | `PUT` | `/api/v1/school-years/{id}` | `SchoolYearRequest` | `ApiResponse<SchoolYearResponse>` | `ACADEMIC_SETUP_MANAGE` |
| **Semesters** | `GET` | `/api/v1/semesters` | Parameters: `page`, `size`, `sort` | `ApiResponse<PageResponse<SemesterResponse>>` | `ACADEMIC_SETUP_VIEW` |
| | `GET` | `/api/v1/semesters/{id}` | N/A | `ApiResponse<SemesterResponse>` | `ACADEMIC_SETUP_VIEW` |
| | `POST` | `/api/v1/semesters` | `SemesterRequest` | `ApiResponse<SemesterResponse>` | `ACADEMIC_SETUP_MANAGE` |
| | `PUT` | `/api/v1/semesters/{id}` | `SemesterRequest` | `ApiResponse<SemesterResponse>` | `ACADEMIC_SETUP_MANAGE` |
| **Sections** | `GET` | `/api/v1/sections` | Parameters: `search` (Optional String), `page`, `size`, `sort` | `ApiResponse<PageResponse<SectionResponse>>` | `ACADEMIC_SETUP_VIEW` |
| | `GET` | `/api/v1/sections/{id}` | N/A | `ApiResponse<SectionResponse>` | `ACADEMIC_SETUP_VIEW` |
| | `POST` | `/api/v1/sections` | `SectionRequest` | `ApiResponse<SectionResponse>` | `ACADEMIC_SETUP_MANAGE` |
| | `PUT` | `/api/v1/sections/{id}` | `SectionRequest` | `ApiResponse<SectionResponse>` | `ACADEMIC_SETUP_MANAGE` |
| | `PATCH` | `/api/v1/sections/{id}/status` | `{"status": "ACTIVE"/"INACTIVE"}` | `ApiResponse<SectionResponse>` | `ACADEMIC_SETUP_MANAGE` |

*Note: Programs, Courses, School Years, and Semesters do not have specialized `PATCH /status` endpoints. Any updates to their statuses or details should be performed through the standard `PUT` endpoints.*

---

### 2.3 Payload Mappings (Java DTO Definitions)

#### 2.3.1 Department DTOs
*   **`DepartmentRequest`**:
    *   `departmentCode`: `String` (Required, `@NotBlank`)
    *   `departmentName`: `String` (Required, `@NotBlank`)
    *   `dean`: `String` (Optional)
    *   `description`: `String` (Optional)
    *   `status`: `ActiveStatus` (Optional, defaults to `ACTIVE`)
*   **`DepartmentResponse`**:
    *   `id`: `UUID`, `departmentCode`: `String`, `departmentName`: `String`, `dean`: `String`, `description`: `String`, `status`: `ActiveStatus`

#### 2.3.2 Program DTOs
*   **`ProgramRequest`**:
    *   `programCode`: `String` (Required, `@NotBlank`)
    *   `programName`: `String` (Required, `@NotBlank`)
    *   `departmentId`: `UUID` (Required, `@NotNull`)
    *   `degreeType`: `DegreeType` (Required, `@NotNull`)
    *   `programDuration`: `Integer` (Optional)
    *   `description`: `String` (Optional)
    *   `status`: `ActiveStatus` (Optional, defaults to `ACTIVE`)
*   **`ProgramResponse`**:
    *   `id`: `UUID`, `programCode`: `String`, `programName`: `String`, `departmentId`: `UUID`, `departmentCode`: `String`, `degreeType`: `DegreeType`, `programDuration`: `Integer`, `description`: `String`, `status`: `ActiveStatus`

#### 2.3.3 Course DTOs
*   **`CourseRequest`**:
    *   `courseCode`: `String` (Required, `@NotBlank`)
    *   `courseTitle`: `String` (Required, `@NotBlank`)
    *   `courseDescription`: `String` (Optional)
    *   `lectureHoursPerWeek`: `BigDecimal` (Required, `@NotNull`, `@DecimalMin("0.0")`)
    *   `laboratoryHoursPerWeek`: `BigDecimal` (Required, `@NotNull`, `@DecimalMin("0.0")`)
    *   `creditUnits`: `BigDecimal` (Required, `@NotNull`, `@DecimalMin("0.0")`)
    *   `courseType`: `CourseType` (Required, `@NotNull`)
    *   `departmentId`: `UUID` (Required, `@NotNull`)
    *   `status`: `ActiveStatus` (Optional, defaults to `ACTIVE`)
*   **`CourseResponse`**:
    *   `id`: `UUID`, `courseCode`: `String`, `courseTitle`: `String`, `courseDescription`: `String`, `lectureHoursPerWeek`: `BigDecimal`, `laboratoryHoursPerWeek`: `BigDecimal`, `creditUnits`: `BigDecimal`, `courseType`: `CourseType`, `departmentId`: `UUID`, `departmentCode`: `String`, `status`: `ActiveStatus`

#### 2.3.4 Faculty DTOs
*   **`FacultyRequest`**:
    *   `employeeNumber`: `String` (Required, `@NotBlank`)
    *   `firstName`: `String` (Required, `@NotBlank`)
    *   `middleName`: `String` (Optional)
    *   `lastName`: `String` (Required, `@NotBlank`)
    *   `suffix`: `String` (Optional)
    *   `email`: `String` (Required, `@NotBlank`, `@Email`)
    *   `contactNumber`: `String` (Optional)
    *   `departmentId`: `UUID` (Required, `@NotNull`)
    *   `employmentStatus`: `EmploymentStatus` (Required, `@NotNull`)
    *   `facultyType`: `FacultyType` (Required, `@NotNull`)
    *   `specialization`: `String` (Optional)
    *   `status`: `ActiveStatus` (Optional, defaults to `ACTIVE`)
*   **`FacultyResponse`**:
    *   `id`: `UUID`, `employeeNumber`: `String`, `firstName`: `String`, `middleName`: `String`, `lastName`: `String`, `suffix`: `String`, `email`: `String`, `contactNumber`: `String`, `departmentId`: `UUID`, `departmentCode`: `String`, `employmentStatus`: `EmploymentStatus`, `facultyType`: `FacultyType`, `specialization`: `String`, `status`: `ActiveStatus`

#### 2.3.5 Room DTOs
*   **`RoomRequest`**:
    *   `roomCode`: `String` (Required, `@NotBlank`)
    *   `roomName`: `String` (Required, `@NotBlank`)
    *   `capacity`: `Integer` (Optional, `@Min(0)`)
    *   `status`: `ActiveStatus` (Optional, defaults to `ACTIVE`)
*   **`RoomResponse`**:
    *   `id`: `UUID`, `roomCode`: `String`, `roomName`: `String`, `capacity`: `Integer`, `status`: `ActiveStatus`

#### 2.3.6 SchoolYear DTOs
*   **`SchoolYearRequest`**:
    *   `schoolYear`: `String` (Required, `@NotBlank`)
    *   `active`: `boolean` (Required)
*   **`SchoolYearResponse`**:
    *   `id`: `UUID`, `schoolYear`: `String`, `active`: `boolean`

#### 2.3.7 Semester DTOs
*   **`SemesterRequest`**:
    *   `name`: `String` (Required, `@NotBlank`)
    *   `sortOrder`: `int` (Required, `@Min(1)`)
    *   `active`: `boolean` (Required)
*   **`SemesterResponse`**:
    *   `id`: `UUID`, `name`: `String`, `sortOrder`: `int`, `active`: `boolean`

#### 2.3.8 Section DTOs
*   **`SectionRequest`**:
    *   `sectionCode`: `String` (Required, `@NotBlank`)
    *   `programId`: `UUID` (Required, `@NotNull`)
    *   `schoolYearId`: `UUID` (Required, `@NotNull`)
    *   `semesterId`: `UUID` (Required, `@NotNull`)
    *   `yearLevel`: `int` (Required, `@Min(1)`)
    *   `status`: `ActiveStatus` (Optional, defaults to `ACTIVE`)
*   **`SectionResponse`**:
    *   `id`: `UUID`, `sectionCode`: `String`, `programId`: `UUID`, `programCode`: `String`, `schoolYearId`: `UUID`, `schoolYear`: `String`, `semesterId`: `UUID`, `semesterName`: `String`, `yearLevel`: `int`, `status`: `ActiveStatus`

---

## 3. Frontend Integration Setup

The frontend codebase is a modern TypeScript, Vite, React (v19) application built on top of Tailwind CSS v4, `@tanstack/react-query` v5 for state synchronization/queries, `react-router-dom` v7 for client-side routing, and shadcn-ui components.

### 3.1 Existing Foundations

#### 3.1.1 Authentication & Guards
Located in `src/lib/auth.tsx` and protected at route levels via `Guard` components in `src/App.tsx`.
*   **`useAuth()` Hook**: Exposes the `user` context, authentication `ready` state, `login` / `logout` actions, and a permission helper `can(permission: string) => boolean`.
*   **Routing Guard**: Checks if the user is authenticated and matches the route's permission. For example, `Guard(permission="ACADEMIC_SETUP_VIEW")` will restrict navigation and render a forbidden state if a user does not have this permission.

#### 3.1.2 API Client
The `api<T>` function in `src/lib/api.ts` handles:
*   Automatically inserting the `Authorization: Bearer <accessToken>` header.
*   Token expiration interception: if a call returns `401`, it automatically invokes `/auth/refresh` using the `refreshToken` stored in `sessionStorage` and retries the original request.
*   Error handling: Throws an `ApiError` which parses field errors.

---

### 3.2 Step-by-Step Integration Plan

To implement the Academic Setup module, we should follow these steps:

#### Step 1: Update Types (`src/lib/types.ts`)
Add the newly identified types to `src/lib/types.ts`:
```typescript
export type ActiveStatus = "ACTIVE" | "INACTIVE";

export type DegreeType = "BACHELOR" | "ASSOCIATE" | "DIPLOMA" | "CERTIFICATE" | "GRADUATE_PROGRAM";

export type CourseType = "MAJOR" | "PROFESSIONAL_COURSE" | "GENERAL_EDUCATION" | "PHYSICAL_EDUCATION" | "NSTP" | "ELECTIVE" | "LABORATORY" | "SEMINAR" | "THESIS_CAPSTONE";

export type EmploymentStatus = "FULL_TIME" | "PART_TIME" | "CONTRACTUAL" | "VISITING_LECTURER" | "INACTIVE";

export type FacultyType = "INSTRUCTOR" | "PROFESSOR" | "LECTURER" | "DEAN" | "PROGRAM_HEAD";

export interface Department {
  id: string;
  departmentCode: string;
  departmentName: string;
  dean?: string;
  description?: string;
  status: ActiveStatus;
}

export interface Program {
  id: string;
  programCode: string;
  programName: string;
  departmentId: string;
  departmentCode: string;
  degreeType: DegreeType;
  programDuration?: number;
  description?: string;
  status: ActiveStatus;
}

export interface Course {
  id: string;
  courseCode: string;
  courseTitle: string;
  courseDescription?: string;
  lectureHoursPerWeek: number;
  laboratoryHoursPerWeek: number;
  creditUnits: number;
  courseType: CourseType;
  departmentId: string;
  departmentCode: string;
  status: ActiveStatus;
}

export interface Faculty {
  id: string;
  employeeNumber: string;
  firstName: string;
  middleName?: string;
  lastName: string;
  suffix?: string;
  email: string;
  contactNumber?: string;
  departmentId: string;
  departmentCode: string;
  employmentStatus: EmploymentStatus;
  facultyType: FacultyType;
  specialization?: string;
  status: ActiveStatus;
}

export interface Room {
  id: string;
  roomCode: string;
  roomName: string;
  capacity?: number;
  status: ActiveStatus;
}

export interface Section {
  id: string;
  sectionCode: string;
  programId: string;
  programCode: string;
  schoolYearId: string;
  schoolYear: string;
  semesterId: string;
  semesterName: string;
  yearLevel: number;
  status: ActiveStatus;
}
```

#### Step 2: Establish API Hooks (`src/hooks/use-setup.ts`)
Create a custom hooks file leveraging React Query (`@tanstack/react-query`) to encapsulate list queries, pagination state, and mutation logic:
*   `useDepartments(search, page, size)` / `useCreateDepartment()` / `useUpdateDepartment()` / `useUpdateDepartmentStatus()`
*   Follow the exact same pattern for Programs, Courses, Faculty, Rooms, School Years, Semesters, and Sections.
*   *Implementation Example*:
    ```typescript
    import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
    import { api } from "@/lib/api";
    import type { PageResponse, Department } from "@/lib/types";

    export function useDepartments(search?: string, page = 0, size = 10) {
      return useQuery({
        queryKey: ["departments", search, page, size],
        queryFn: () => api<PageResponse<Department>>(`/departments?search=${encodeURIComponent(search ?? "")}&page=${page}&size=${size}`)
      });
    }
    ```

#### Step 3: Integrate Router Navigation (`src/App.tsx`)
1.  Add the Academic Setup module to the navigation list (`nav`):
    ```typescript
    const nav = [
      ...
      { to: "/setup", label: "Academic Setup", icon: Settings, permission: "ACADEMIC_SETUP_VIEW" },
    ]
    ```
2.  Update the routes tree inside `App()` to hook up the setup workspace:
    ```tsx
    <Route element={<Guard permission="ACADEMIC_SETUP_VIEW" />}>
      <Route path="setup/*" element={<SetupLayout />}>
        {/* We default to departments or list them here */}
        <Route index element={<Navigate to="departments" replace />} />
        <Route path="departments" element={<DepartmentsTab />} />
        <Route path="programs" element={<ProgramsTab />} />
        <Route path="courses" element={<CoursesTab />} />
        <Route path="faculty" element={<FacultyTab />} />
        <Route path="rooms" element={<RoomsTab />} />
        <Route path="school-years" element={<SchoolYearsTab />} />
        <Route path="semesters" element={<SemestersTab />} />
        <Route path="sections" element={<SectionsTab />} />
      </Route>
    </Route>
    ```

#### Step 4: Create Setup Layout & Pages (`src/pages/setup/`)
*   **`SetupLayout`**: Renders a secondary sidebar or tabbed list (e.g. using Radix `<Tabs>` or inline nav links) allowing the administrator to switch tabs between the 8 sub-modules. It wraps rendering within a unified screen structure containing header actions, search bars, and the central grid.
*   **Module CRUD Sub-pages**:
    *   Implement standard datatables utilizing `<Table>` and `<Pagination>` from `components/ui`.
    *   Incorporate `<Dialog>` / `<Sheet>` modals containing React Hook Form instances powered by Zod validation matching the backend request payload requirements (e.g. checking blank fields, numeric boundaries, email syntax).
    *   Enable inline state switches utilizing the status `PATCH` mapping endpoints (for Departments, Faculty, Rooms, Sections) or standard `PUT` updates (for Programs, Courses, School Years, Semesters).
