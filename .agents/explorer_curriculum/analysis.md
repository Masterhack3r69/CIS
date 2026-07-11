# Curriculum Management Backend API and Database Analysis

This document provides a comprehensive analysis of the Curriculum Management module's backend REST APIs, database schemas, TypeScript interfaces, and integration recommendations.

---

## 1. Database Schema Structure & Relationships

The database schema is defined in the Flyway migration file `V2__curriculum_management.sql`. The core entities are `Curriculum` and `CurriculumCourse`, which represent the curriculum records and their associated course assignments, including pre-requisite and co-requisite rules.

### Entity Relationship Diagram (Conceptual)
```
  [Program] 1 ----- 0..* [Curriculum]
                              1
                              |
                              | 0..*
                      [CurriculumCourse] 0..* ----- 1 [Course]
                              |
             +----------------+---------------+
             |                                |
             | 0..*                           | 0..*
    (Prerequisites)                  (Corequisites)
             |                                |
             +----------------+---------------+
                              |
                              v
                          [Course]
```

### Table Definitions

#### A. `curricula`
Stores the metadata for a curriculum associated with an academic program.
- **`id`** (`UUID`, PRIMARY KEY): Generated via Java's `UUID.randomUUID()` during pre-persist.
- **`program_id`** (`UUID`, NOT NULL): Foreign key referencing `programs(id)`.
- **`curriculum_code`** (`VARCHAR(60)`, NOT NULL, UNIQUE): Unique identifier (e.g., "BSCS-2026").
- **`curriculum_name`** (`TEXT`, NOT NULL): Descriptive name (e.g., "BS in Computer Science - Version 2026").
- **`effective_school_year`** (`VARCHAR(20)`, NOT NULL): The school year the curriculum starts taking effect (e.g., "2026-2027").
- **`version`** (`VARCHAR(40)`, NOT NULL): Version number or tag.
- **`status`** (`VARCHAR(20)`, NOT NULL, DEFAULT `'DRAFT'`): Maps to Java enum `CurriculumStatus` (`DRAFT`, `ACTIVE`, `INACTIVE`, `ARCHIVED`).
- **`description`** (`TEXT`, NULL): Optional detailed description.
- **`created_at`** (`TIMESTAMPTZ`, NOT NULL, DEFAULT `now()`): Auditing field.
- **`updated_at`** (`TIMESTAMPTZ`, NOT NULL, DEFAULT `now()`): Auditing field.

**Indexes & Constraints**:
- Index: `idx_curricula_program_id` on `program_id`.
- Unique Partial Index: `ux_curricula_one_active_per_program` on `(program_id) WHERE status = 'ACTIVE'`. This guarantees that an academic program has **at most one active curriculum** at any time.

#### B. `curriculum_courses`
Links courses to curricula, detailing their sequence (year level, semester) and requirement status.
- **`id`** (`UUID`, PRIMARY KEY): Generated via `UUID.randomUUID()`.
- **`curriculum_id`** (`UUID`, NOT NULL): Foreign key referencing `curricula(id) ON DELETE CASCADE`.
- **`course_id`** (`UUID`, NOT NULL): Foreign key referencing `courses(id)`.
- **`year_level`** (`INTEGER`, NOT NULL): Must be greater than 0.
- **`semester`** (`VARCHAR(40)`, NOT NULL): The academic semester (e.g., "FIRST_SEMESTER").
- **`sort_order`** (`INTEGER`, NOT NULL): Must be greater than 0. Controls course rendering sequence.
- **`required_status`** (`VARCHAR(20)`, NOT NULL, DEFAULT `'REQUIRED'`): Maps to Java enum `RequiredStatus` (`REQUIRED`, `OPTIONAL`, `ELECTIVE`).
- **`created_at`** (`TIMESTAMPTZ`, NOT NULL, DEFAULT `now()`).
- **`updated_at`** (`TIMESTAMPTZ`, NOT NULL, DEFAULT `now()`).

**Indexes & Constraints**:
- Check Constraint: `curriculum_courses_year_level_positive CHECK (year_level > 0)`.
- Check Constraint: `curriculum_courses_sort_order_positive CHECK (sort_order > 0)`.
- Unique Constraint: `curriculum_courses_unique_term_course` on `(curriculum_id, year_level, semester, course_id)`. Ensures a course cannot be assigned multiple times to the exact same term/semester within a curriculum.
- Indexes: `idx_curriculum_courses_curriculum_id` and `idx_curriculum_courses_course_id`.

#### C. `curriculum_course_prerequisites`
Many-to-many relationship mapping courses that must be completed before enrolling in a given curriculum course.
- **`curriculum_course_id`** (`UUID`, NOT NULL): Foreign key referencing `curriculum_courses(id) ON DELETE CASCADE`.
- **`prerequisite_course_id`** (`UUID`, NOT NULL): Foreign key referencing `courses(id)`.
- **Primary Key**: `(curriculum_course_id, prerequisite_course_id)`.

#### D. `curriculum_course_corequisites`
Many-to-many relationship mapping courses that must be taken simultaneously with a given curriculum course.
- **`curriculum_course_id`** (`UUID`, NOT NULL): Foreign key referencing `curriculum_courses(id) ON DELETE CASCADE`.
- **`corequisite_course_id`** (`UUID`, NOT NULL): Foreign key referencing `courses(id)`.
- **Primary Key**: `(curriculum_course_id, corequisite_course_id)`.

---

## 2. API Endpoint Matrix

All REST endpoints reside under the base path `/api/v1/curricula`. Access to endpoints is secured via authorization checks (authority flags `CURRICULUM_VIEW` and `CURRICULUM_MANAGE`).

| Method | Path | Request Payload | Response Schema | Required Authority | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **`GET`** | `/api/v1/curricula` | Query: `search?: string`, Pageable params (`page`, `size`, `sort`) | `ApiResponse<PageResponse<CurriculumResponse>>` | `CURRICULUM_VIEW` | Paginated search of curricula by curriculum code or name. |
| **`POST`** | `/api/v1/curricula` | `CurriculumRequest` (JSON) | `ApiResponse<CurriculumResponse>` | `CURRICULUM_MANAGE` | Creates a new curriculum record in `DRAFT` status by default. |
| **`GET`** | `/api/v1/curricula/{id}` | *None* | `ApiResponse<CurriculumDetailResponse>` | `CURRICULUM_VIEW` | Retrieves the curriculum metadata along with the flat list of mapped courses. |
| **`PUT`** | `/api/v1/curricula/{id}` | `CurriculumRequest` (JSON) | `ApiResponse<CurriculumResponse>` | `CURRICULUM_MANAGE` | Updates the basic fields of a curriculum. |
| **`POST`** | `/api/v1/curricula/{id}/activate` | *None* | `ApiResponse<CurriculumResponse>` | `CURRICULUM_MANAGE` | Activates a curriculum. Sets all other active curricula under the same program to `INACTIVE`. |
| **`GET`** | `/api/v1/curricula/{id}/checklist` | *None* | `ApiResponse<CurriculumChecklistResponse>` | `CURRICULUM_VIEW` | Retrieves curriculum details with courses pre-grouped by year level and semester, including aggregated term hours/credits. |
| **`POST`** | `/api/v1/curricula/{id}/courses` | `CurriculumCourseRequest` (JSON) | `ApiResponse<CurriculumCourseResponse>` | `CURRICULUM_MANAGE` | Links a course to the curriculum at a designated year, semester, and sort order. |
| **`PUT`** | `/api/v1/curricula/{id}/courses/{curriculumCourseId}` | `CurriculumCourseRequest` (JSON) | `ApiResponse<CurriculumCourseResponse>` | `CURRICULUM_MANAGE` | Updates a course's placement, requirements, or prerequisites inside the curriculum. |
| **`DELETE`** | `/api/v1/curricula/{id}/courses/{curriculumCourseId}` | *None* | `ApiResponse<Void>` | `CURRICULUM_MANAGE` | Removes the course mapping from the curriculum. |

---

## 3. TypeScript Interfaces

The following TypeScript definitions map directly to the backend DTOs and database enums.

```typescript
export type CurriculumStatus = 'DRAFT' | 'ACTIVE' | 'INACTIVE' | 'ARCHIVED';

export type RequiredStatus = 'REQUIRED' | 'OPTIONAL' | 'ELECTIVE';

export interface FieldErrorResponse {
  field: string;
  message: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T | null;
  errors: FieldErrorResponse[] | null;
  timestamp: string; // ISO-8601 string
}

export interface PageResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface CurriculumRequest {
  programId: string; // UUID
  curriculumCode: string;
  curriculumName: string;
  effectiveSchoolYear: string;
  version: string;
  status: CurriculumStatus;
  description?: string;
}

export interface CurriculumResponse {
  id: string; // UUID
  programId: string; // UUID
  programCode: string;
  curriculumCode: string;
  curriculumName: string;
  effectiveSchoolYear: string;
  version: string;
  status: CurriculumStatus;
  description: string | null;
}

export interface CourseLinkResponse {
  id: string; // UUID (Course ID)
  courseCode: string;
  courseTitle: string;
}

export interface CurriculumCourseRequest {
  yearLevel: number; // >= 1 (@Min(1))
  semester: string; // @NotBlank (e.g. "FIRST_SEMESTER", "SECOND_SEMESTER")
  courseId: string; // UUID, @NotNull
  sortOrder: number; // >= 1 (@Min(1))
  requiredStatus: RequiredStatus; // @NotNull
  prerequisiteCourseIds: string[] | null; // UUIDs
  corequisiteCourseIds: string[] | null; // UUIDs
}

export interface CurriculumCourseResponse {
  id: string; // UUID (Curriculum Course mapping ID)
  yearLevel: number;
  semester: string;
  courseId: string; // UUID (Course ID)
  courseCode: string;
  courseTitle: string;
  lectureHoursPerWeek: number;
  laboratoryHoursPerWeek: number;
  creditUnits: number;
  sortOrder: number;
  requiredStatus: RequiredStatus;
  prerequisites: CourseLinkResponse[];
  corequisites: CourseLinkResponse[];
}

export interface CurriculumDetailResponse {
  curriculum: CurriculumResponse;
  courses: CurriculumCourseResponse[];
}

export interface CurriculumTermResponse {
  yearLevel: number;
  semester: string;
  totalLectureHours: number;
  totalLaboratoryHours: number;
  totalCreditUnits: number;
  courses: CurriculumCourseResponse[];
}

export interface CurriculumChecklistResponse {
  curriculum: CurriculumResponse;
  terms: CurriculumTermResponse[];
}
```

---

## 4. Key Architectural Patterns & Constraints

1. **Checklist Grouping / Course Builder Structure**:
   - The `/api/v1/curricula/{id}/checklist` endpoint handles grouping on the server side using the format `yearLevel | semester` (e.g., `1|FIRST_SEMESTER`).
   - The server aggregates lecture hours, lab hours, and credit units per term. The frontend does not need to compute these values manually.
   - Map key concatenation logic in Java: `course -> course.yearLevel() + "|" + course.semester()`.

2. **Validation Annotations**:
   - **`CurriculumRequest`**:
     - `@NotNull programId`
     - `@NotBlank` constraints on `curriculumCode`, `curriculumName`, `effectiveSchoolYear`, and `version`.
     - `@NotNull status`
   - **`CurriculumCourseRequest`**:
     - `@Min(1) yearLevel`
     - `@NotBlank semester`
     - `@NotNull courseId`
     - `@Min(1) sortOrder`
     - `@NotNull requiredStatus`

3. **Activation Behavior**:
   - Activating a curriculum (via `POST /api/v1/curricula/{id}/activate`) is a non-destructive state transition, but it triggers a cascade. In `CurriculumService.java`:
     - It fetches all curricula under the same `programId` that are currently marked as `ACTIVE`.
     - It sets those other curricula to `INACTIVE`.
     - It saves/updates the current curriculum status to `ACTIVE`.

4. **Circular/Duplicate Mappings**:
   - The backend checks for duplicate course mappings within the same year level and semester:
     ```java
     boolean duplicate = curriculumCourseRepository.existsByCurriculumIdAndYearLevelAndSemesterIgnoreCaseAndCourseId(...)
     ```
     If a duplicate exists and is not the record currently being updated, the API returns a `400 Bad Request` with message `Course already exists in this curriculum year level and semester`.

---

## 5. Integration Recommendations for Frontend Implementation

1. **Curriculum Builder View**:
   - Utilize `/api/v1/curricula/{id}/checklist` as the primary data fetch endpoint. The grouped structure (`terms`) can be directly mapped to UI columns or accordion sections.
   - For course drag-and-drop or reordering within/across semesters, trigger the `PUT /api/v1/curricula/{id}/courses/{curriculumCourseId}` endpoint using updated `yearLevel`, `semester`, and `sortOrder`.

2. **Adding/Editing Prerequisites and Corequisites**:
   - When assigning a course to a curriculum (either via `POST` to `/courses` or `PUT` to `/courses/{id}`), provide multi-select components (e.g., combobox or tag inputs) showing the list of courses.
   - Submit arrays of UUIDs under `prerequisiteCourseIds` and `corequisiteCourseIds` in the request payload.

3. **Status Transitions & Safe Guards**:
   - Show a warning to the user when triggering activation. Activating one curriculum automatically deactivates another active curriculum for the program.
   - Once a curriculum is `ACTIVE` or `INACTIVE`, limit metadata modification or course additions unless the status is returned to `DRAFT`. Validate state constraints in the UI to prevent API rejects.
