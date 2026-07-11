# Handoff Report — Academic Setup Module Analysis

## 1. Observation

I observed the following source files, database migrations, and configurations in the workspace `c:\Users\PC\Projects\cis`:

### 1.1 Database Migration Schema
The database tables and validation constraints for the Academic Setup are defined in `src/main/resources/db/migration/V1__foundation_auth_and_setup.sql`.
For example, the `departments` table is defined as:
```sql
52: CREATE TABLE departments (
53:     id UUID PRIMARY KEY,
54:     department_code VARCHAR(40) NOT NULL UNIQUE,
55:     department_name TEXT NOT NULL UNIQUE,
56:     dean TEXT,
57:     description TEXT,
58:     status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
59:     created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
60:     updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
61: );
```
And check constraints such as those in the `courses` table:
```sql
89:     CONSTRAINT courses_non_negative_hours CHECK (
90:         lecture_hours_per_week >= 0
91:         AND laboratory_hours_per_week >= 0
92:         AND credit_units >= 0
93:     )
```

### 1.2 JPA Entities and Supporting Enums
The entities are defined in `src/main/java/com/school/sis/setup/entity/` (e.g. `Department.java`, `Program.java`, `Course.java`, `Faculty.java`, `Room.java`, `SchoolYear.java`, `Semester.java`, `Section.java`).
Supporting enums define boundaries for domain fields:
*   `ActiveStatus` in `ActiveStatus.java` (`ACTIVE`, `INACTIVE`)
*   `DegreeType` in `DegreeType.java` (`BACHELOR`, `ASSOCIATE`, `DIPLOMA`, `CERTIFICATE`, `GRADUATE_PROGRAM`)
*   `CourseType` in `CourseType.java` (`MAJOR`, `PROFESSIONAL_COURSE`, `GENERAL_EDUCATION`, etc.)
*   `EmploymentStatus` in `EmploymentStatus.java` (`FULL_TIME`, `PART_TIME`, etc.)
*   `FacultyType` in `FacultyType.java` (`INSTRUCTOR`, `PROFESSOR`, etc.)

### 1.3 Backend API Endpoints & Request/Response Payloads
The controllers in `src/main/java/com/school/sis/setup/controller/` expose the API endpoints.
For example, `DepartmentController.java` specifies:
```java
26: @RequestMapping("/api/v1/departments")
...
35:     @GetMapping
36:     @PreAuthorize("hasAuthority('ACADEMIC_SETUP_VIEW')")
37:     public ApiResponse<PageResponse<DepartmentResponse>> list(@RequestParam(required = false) String search, Pageable pageable)
...
47:     @PostMapping
48:     @PreAuthorize("hasAuthority('ACADEMIC_SETUP_MANAGE')")
49:     public ApiResponse<DepartmentResponse> create(@Valid @RequestBody DepartmentRequest request)
```
Request payload definitions reside in `src/main/java/com/school/sis/setup/dto/` (e.g. `DepartmentRequest.java` maps `@NotBlank` fields to validate request contents).

### 1.4 Global Exception Mappings
Exceptions are intercepted globally in `src/main/java/com/school/sis/common/exception/GlobalExceptionHandler.java`:
*   `MethodArgumentNotValidException` maps validation errors into HTTP 400 with field errors.
*   `NotFoundException` maps to HTTP 404.
*   `BusinessRuleException` maps to HTTP 400.

### 1.5 Frontend Router and Auth Client
In `c:\Users\PC\Projects\cis\frontend`:
*   `src/lib/api.ts` implements a fetch-based client wrapper `api<T>` handling automatic Bearer token insertion and 401 JWT refresh.
*   `src/lib/auth.tsx` provides `AuthProvider` exposing permission checking via `can(permission)`.
*   `src/App.tsx` configures the React Router v7 routes list and matches them using route level `Guard` checks.

---

## 2. Logic Chain

1.  **Backend Readiness**: The database migrations (`V1__foundation_auth_and_setup.sql`) and JPA entities define the structures and fields exactly matching the model. There are no pending schema adjustments, meaning the backend data storage capability is fully implemented.
2.  **API Structure Validation**: The 8 Spring Boot controllers handle standard REST CRUD (GET, POST, PUT, and status PATCH where appropriate) and explicitly enforce security checks using `@PreAuthorize("hasAuthority('ACADEMIC_SETUP_VIEW')")` for reading and `ACADEMIC_SETUP_MANAGE` for mutation.
3.  **Request Validation**: Validation rules are strictly specified in the request DTOs (e.g. `CourseRequest.java` requiring non-negative numbers via `@DecimalMin("0.0")` and non-blank identifiers via `@NotBlank`). This matches the PostgreSQL check constraints (e.g. `courses_non_negative_hours`).
4.  **Frontend Compatibility**: The frontend project relies on standard query hooks via `@tailwindcss/vite`, `@tanstack/react-query`, and Zod-based forms. Creating the layout and integrating the tabs/routes for Academic Setup under a new `/setup` path will inherit the existing sidebar layout and permission checks seamlessly.

---

## 3. Caveats

*   **No Module-Specific Automated Tests**: No specialized test suites for the `setup` module controller or service layers were found in `src/test/java/com/school/sis/setup`. Tests are assumed to be covered by global context boot checks (`SisApplicationTests` and `PostgresMigrationTests`).
*   **Varying Endpoints**: Not all setup modules support status `PATCH` (e.g., `programs` and `courses` do not have specialized status patch endpoints in their controllers). The frontend implementation must use full `PUT` updates for these modules, while utilizing `PATCH` for others (Departments, Faculty, Rooms, Sections).

---

## 4. Conclusion

The Academic Setup module is fully prepared from a database and API perspective. The frontend can integrate this new setup module under the route path `/setup` (guarded by `ACADEMIC_SETUP_VIEW`), using a multi-tab layout mapping to the 8 CRUD sections, Zod validations matching the Java DTO constraints, and TanStack query caching.

---

## 5. Verification Method

To independently verify the observations and schema configurations:
1.  **Inspect Migration Scripts & Entities**:
    *   Open `src/main/resources/db/migration/V1__foundation_auth_and_setup.sql` to confirm table fields.
    *   Open `src/main/java/com/school/sis/setup/entity/` classes to confirm JPA mappings.
2.  **Execute Backend Tests**:
    *   Run `mvn test` in `c:\Users\PC\Projects\cis` to ensure migration schemas and context bootstrap tests pass.
3.  **Inspect Frontend Files**:
    *   Check `frontend/src/lib/types.ts` to verify the existing structure and place the new definitions.
    *   Run `npm run build` or `npm run typecheck` inside `frontend/` directory to ensure type compliance.
