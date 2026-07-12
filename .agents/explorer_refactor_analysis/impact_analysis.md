# Impact Analysis Report: Student Profiling and Enrollment Refactoring

## Executive Summary
This report analyzes the impact of refactoring the student profiling, enrollment, and section management modules in the College Student Information System (SIS). The main objective is to normalize sections and enrollments, remove duplicate student section associations, and transition to a term-specific section structure.

During the codebase investigation, a critical compilation blocker was identified: `ReportService.java` references the previously removed `student.getSemester()` and `student.getSection()` methods, causing the backend build to fail. This report outlines the exact locations, logic chains, affected files, migration risks, and testing guidelines needed to complete this refactoring safely.

---

## 1. Current Entity Models, Tables, and FK Relationships

### 1.1 Students and Nested Objects
The Student entity follows a modular composition pattern. The primary table `students` holds core personal and academic data, while specialized details are isolated in separate, one-to-one mapped tables sharing the student's primary key (`student_id`).

*   **`students` (Table & Hibernate Model)**
    *   **Primary Key**: `id UUID`
    *   **Fields**: `student_number` (Unique), `first_name`, `middle_name`, `last_name`, `suffix`, `gender`, `birthdate`, `birthplace`, `civil_status`, `nationality`, `religion`, `profile_photo_path`, `status`, `year_level`, `date_admitted`, `school_year_admitted`, `classification`, `academic_status`.
    *   **Relationships**:
        *   `program_id` -> `programs(id)` (Many-to-One, Required)
        *   `curriculum_id` -> `curricula(id)` (Many-to-One, Required)
        *   `contact` -> mapped by `StudentContact` (One-to-One, Lazy)
        *   `familyBackground` -> mapped by `StudentFamilyBackground` (One-to-One, Lazy)
        *   `educationalBackground` -> mapped by `StudentEducationalBackground` (One-to-One, Lazy)

*   **`student_contacts` (Table & Hibernate Model)**
    *   **Primary Key**: `student_id UUID` (Foreign Key referencing `students(id)` ON DELETE CASCADE)
    *   **Fields**: `mobile_number`, `telephone_number`, `email_address` (Unique), `current_address`, `permanent_address`, and granular PSGC region/province/city/barangay details.

*   **`student_family_backgrounds` (Table & Hibernate Model)**
    *   **Primary Key**: `student_id UUID` (Foreign Key referencing `students(id)` ON DELETE CASCADE)
    *   **Fields**: Father/Mother/Guardian names, occupations, contact numbers, and household income range.

*   **`student_educational_backgrounds` (Table & Hibernate Model)**
    *   **Primary Key**: `student_id UUID` (Foreign Key referencing `students(id)` ON DELETE CASCADE)
    *   **Fields**: Elementary, junior high, senior high school details, previous college/program (if transferee), and admission type.

---

### 1.2 Enrollments
The Enrollment entity links a student to a specific term (school year and semester), tracking their year level and assigned section.

*   **`enrollments` (Table & Hibernate Model)**
    *   **Primary Key**: `id UUID`
    *   **Fields**: `year_level` (Integer, Required, Positive), `status` (`DRAFT`, `CONFIRMED`, `CANCELLED`), `remarks`, timestamps.
    *   **Relationships**:
        *   `student_id` -> `students(id)` (Many-to-One, Required)
        *   `program_id` -> `programs(id)` (Many-to-One, Required)
        *   `section_id` -> `sections(id)` (Many-to-One, Optional)
        *   `school_year_id` -> `school_years(id)` (Many-to-One, Required)
        *   `semester_id` -> `semesters(id)` (Many-to-One, Required)
    *   **Uniqueness**: Enforced via composite unique index `ux_enrollments_active_term` on `(student_id, school_year_id, semester_id)` for non-cancelled enrollments.

*   **`enrollment_subjects` (Table & Hibernate Model)**
    *   **Primary Key**: `id UUID`
    *   **Fields**: `status` (`ENROLLED`, `DROPPED`), `dropped_at`.
    *   **Relationships**:
        *   `enrollment_id` -> `enrollments(id)` (Many-to-One, Required)
        *   `class_schedule_id` -> `class_schedules(id)` (Many-to-One, Required)

---

### 1.3 Sections
Sections are academic groupings that are specific to a program, curriculum, and term.

*   **`sections` (Table & Hibernate Model)**
    *   **Primary Key**: `id UUID`
    *   **Fields**: `section_code`, `year_level`, `status` (`ACTIVE`, `INACTIVE`), timestamps.
    *   **Relationships**:
        *   `program_id` -> `programs(id)` (Many-to-One, Required)
        *   `curriculum_id` -> `curricula(id)` (Many-to-One, Optional)
        *   `school_year_id` -> `school_years(id)` (Many-to-One, Required)
        *   `semester_id` -> `semesters(id)` (Many-to-One, Required)
    *   **Uniqueness**: Constraint `sections_unique_term` on `(section_code, school_year_id, semester_id)`.

---

## 2. Existing Database Migrations & Schemas
The database schema is managed via Flyway. The migrations related to these entities are:
1.  **`V1__foundation_auth_and_setup.sql`**: Creates `sections`, `school_years`, `semesters`, `programs`, and `departments`. Enforces unique sections per term via `CONSTRAINT sections_unique_term UNIQUE (section_code, school_year_id, semester_id)`.
2.  **`V3__student_profile_management.sql`**: Creates `students`, `student_contacts`, `student_family_backgrounds`, and `student_educational_backgrounds`. Initially included a foreign key constraint `section_id UUID REFERENCES sections(id)` and a `semester` column in the `students` table.
3.  **`V5__enrollment_management.sql`**: Creates `enrollments`, `enrollment_subjects`, and `enrollment_status_history`.
4.  **`V10__normalize_sections_enrollment_and_scheduling.sql`**:
    *   Adds `curriculum_id` to `sections` table.
    *   Adds `year_level` to `enrollments` table, backfilling it from `students.year_level`, setting it to `NOT NULL`, and adding a positive check constraint.
    *   Drops `section_id` and `semester` from the `students` table.

---

## 3. Current REST APIs (Controllers and DTOs)

### 3.1 Student APIs (`StudentController.java`)
*   `GET /api/v1/students` (PreAuthorize: `STUDENT_VIEW`): Lists students paginated, supporting search and filtering by program, year, status, school year admitted, and document status.
*   `POST /api/v1/students` (PreAuthorize: `STUDENT_CREATE`): Creates a student. Accepts `StudentRequest` DTO containing nested personal, contact, family, educational, and academic DTOs.
*   `GET /api/v1/students/{id}` (PreAuthorize: `STUDENT_VIEW`): Retrieves a student's full profile response (`StudentResponse` DTO).
*   `PUT /api/v1/students/{id}` (PreAuthorize: `STUDENT_UPDATE`): Updates a student's profile.
*   `PATCH /api/v1/students/{id}/status` (PreAuthorize: `STUDENT_UPDATE`): Updates a student's status (`StudentStatusRequest` DTO).
*   `GET /api/v1/students/{id}/academic-records` (PreAuthorize: `STUDENT_VIEW`): Returns the student's chronological academic history (`StudentAcademicRecordsResponse` DTO).

### 3.2 Enrollment APIs (`EnrollmentController.java`)
*   `GET /api/v1/enrollments` (PreAuthorize: `ENROLLMENT_VIEW`): Lists enrollments, filtering by search criteria, student, program, section, school year, semester, or status.
*   `POST /api/v1/enrollments` (PreAuthorize: `ENROLLMENT_CREATE`): Creates a draft enrollment (`EnrollmentRequest` DTO).
*   `PUT /api/v1/enrollments/{id}` (PreAuthorize: `ENROLLMENT_CREATE`): Updates enrollment metadata (`EnrollmentUpdateRequest` DTO).
*   `POST /api/v1/enrollments/{id}/subjects` (PreAuthorize: `ENROLLMENT_CREATE`): Enrolls a subject schedule (`EnrollmentSubjectRequest` DTO).
*   `DELETE /api/v1/enrollments/{id}/subjects/{subjectId}` (PreAuthorize: `ENROLLMENT_CREATE`): Drops a subject.
*   `POST /api/v1/enrollments/{id}/validate` (PreAuthorize: `ENROLLMENT_VIEW`): Validates prerequisite and co-requisite rules, schedule overlaps, and section alignments.
*   `POST /api/v1/enrollments/{id}/confirm` (PreAuthorize: `ENROLLMENT_APPROVE`): Confirms enrollment, setting status to `CONFIRMED` and updating the student's year level.

### 3.3 Section APIs (`SectionController.java`)
*   `GET /api/v1/sections` (PreAuthorize: `ACADEMIC_SETUP_VIEW`): Lists sections paginated.
*   `POST /api/v1/sections` (PreAuthorize: `ACADEMIC_SETUP_MANAGE`): Creates a section (`SectionRequest` DTO).
*   `PUT /api/v1/sections/{id}` (PreAuthorize: `ACADEMIC_SETUP_MANAGE`): Updates section properties.
*   `PATCH /api/v1/sections/{id}/status` (PreAuthorize: `ACADEMIC_SETUP_MANAGE`): Toggles active status.

---

## 4. Validation Logic (Backend & Frontend)

### 4.1 Backend JSR-380 Annotations
*   **Student Academic Requests (`StudentAcademicRequest`)**:
    *   `@NotNull UUID programId`
    *   `@NotNull UUID curriculumId`
    *   `@Min(1) int yearLevel`
    *   `@NotNull LocalDate dateAdmitted`
    *   `@NotBlank String schoolYearAdmitted`
*   **Enrollment Requests (`EnrollmentRequest`)**:
    *   `@NotNull UUID studentId`
    *   `@NotNull UUID schoolYearId`
    *   `@NotNull UUID semesterId`
    *   `@NotNull Integer yearLevel`
*   **Section Requests (`SectionRequest`)**:
    *   `@NotBlank String sectionCode`
    *   `@NotNull UUID programId`
    *   `@NotNull UUID curriculumId`
    *   `@NotNull UUID schoolYearId`
    *   `@NotNull UUID semesterId`
    *   `@Min(1) int yearLevel`

### 4.2 Frontend Zod Schemas
*   **Student Academic (`academicSchema` in `students-page.tsx`)**:
    *   `programId`: `z.string().min(1, "Program is required")`
    *   `curriculumId`: `z.string().min(1, "Curriculum is required")`
    *   `yearLevel`: `z.coerce.number().min(1, "Year level must be at least 1")`
    *   `dateAdmitted`: `z.string().min(1, "Date admitted is required")`
    *   `schoolYearAdmitted`: `z.string().min(1, "School year admitted is required")`
*   **Section Form (`sectionSchema` in `sections-tab.tsx`)**:
    *   `sectionCode`: `z.string().min(1, "Section code is required").trim()`
    *   `programId`: `z.string().min(1, "Program is required")`
    *   `curriculumId`: `z.string().min(1, "Curriculum is required")`
    *   `schoolYearId`: `z.string().min(1, "School year is required")`
    *   `semesterId`: `z.string().min(1, "Semester is required")`
    *   `yearLevel`: `z.coerce.number().int().min(1, "Year level must be at least 1")`

---

## 5. Frontend Views and Layouts

### 5.1 Student Forms and Views (`students-page.tsx`)
*   **Create Student Modal**: Utilizes a step-by-step tab layout (Personal, Academic, Contact, Family, Education) within a `Dialog`. The Academic tab gathers program, curriculum, year level, and admission criteria.
*   **Student Profile View**: Rendered by `StudentDetailPage`. Shows categorized information grids inside tabs. In the Academic tab, it currently renders curriculum assignments and a grid of previous academic records (`AcademicRecordsTable`).

### 5.2 Section Creation and Edit (`sections-tab.tsx`)
*   **Section Workspace**: Shows a list of sections in a table, displaying code, program, curriculum, school year, semester, year level, and status toggle.
*   **Creation/Edit Modal**: Uses a standard dialog form. Program selection dynamically filters available curricula via local component state. Term setup requires selecting a School Year and Semester.

---

## 6. Refactoring Impacts and Affected Files

### 6.1 Critical Compilation Error: `ReportService.java`
*   **Impact**: When `semester` and `section_id` were removed from the `Student` model, `ReportService.java` was not updated. The compiler fails on lines 319-320 with:
    *   `cannot find symbol student.getSemester()`
    *   `cannot find symbol student.getSection()`
*   **Resolution**: Update `ReportService.academicHeader()` to look up the student's latest active or confirmed enrollment using `EnrollmentRepository` and draw the current section and semester from that enrollment entity instead.

### 6.2 Requiring Year Level on Enrollment & Section Enforcement
*   **Impact**: `year_level` is already marked as `@NotNull` on the backend `EnrollmentRequest` and migrated via `V10` to be non-nullable on the database.
*   **Section Rule**: Section is required on enrollment *unless* the student classification is `IRREGULAR` or `CROSS_ENROLLEE`.
    *   **Backend**: `EnrollmentService.validateSection()` already enforces this. However, it must also be updated to ensure the retrieved section is active:
        `if (section.getStatus() != ActiveStatus.ACTIVE) throw new BusinessRuleException("Selected section is inactive");`
    *   **Frontend**: In `enrollment-page.tsx`, `assignmentReady` restricts form submission if no section is chosen for regular students:
        `const assignmentReady = flexible ? !!sectionChoice : !!sectionChoice && sectionChoice !== "__mixed__"`
        This matches the backend logic.

### 6.3 Enrollment Confirmation Logic
*   **Impact**: `EnrollmentService.confirm()` must only update the student's `yearLevel` to match the enrollment's `yearLevel`.
    *   **Current State**: Already updated to `enrollment.getStudent().setYearLevel(enrollment.getYearLevel())` without touching sections, because `students.section_id` has been dropped.

### 6.4 "Current Enrollment" Panel on Student Profile
*   **Impact**: The frontend student profile (`students-page.tsx`) must display term-specific enrollment.
    *   **Proposed UI Modification**: Add a "Current Enrollment" tab to the `StudentDetailPage` tabs.
    *   **Implementation details**:
        *   Fetch the student's enrollments using a query to `/api/v1/enrollments?studentId=${id}&size=1`.
        *   If an enrollment exists, display the Term (School Year & Semester), Year Level, Section Code, Status, and a detailed list of subjects (Course Code, Title, Section Code, Room, Faculty, Credit Units, and Schedule Meetings).
        *   If no enrollment exists, render an alert panel stating "No enrollment recorded for this student."

### 6.5 Term-Specific Section Setup & Uniqueness
*   **Impact**: Sections must be unique based on code + school year + semester.
    *   **Backend**: Database constraint `sections_unique_term` handles uniqueness. `SectionService.apply()` must throw a meaningful business exception if a section code duplicate is detected, rather than throwing a raw database integrity violation.
    *   **UI Updates**: In `sections-tab.tsx`, curriculum selection is required when creating/editing. The form dropdowns must support selecting active school years and semesters.

### 6.6 Inventory of Affected Files
*   **Database**:
    *   `src/main/resources/db/migration/V10__normalize_sections_enrollment_and_scheduling.sql` (Schema migrations)
*   **Backend**:
    *   `src/main/java/com/school/sis/report/service/ReportService.java` (CRITICAL compilation fix)
    *   `src/main/java/com/school/sis/enrollment/service/EnrollmentService.java` (Add ActiveStatus check in section validation)
*   **Frontend**:
    *   `frontend/src/pages/students-page.tsx` (Add "Current Enrollment" panel to Profile UI)
    *   `frontend/src/lib/types.ts` (Verify aligned interface fields)

---

## 7. Migration Risks and Backfill Strategy
When transitioning section associations from the static `students` table to the `enrollments` table, the following risks must be mitigated:

1.  **Risk**: Students who do not have an enrollment record in the database yet will lose their section assignment and current semester information once columns are dropped.
2.  **Backfill Strategy**:
    *   Before applying `V10` schema modifications to drop `section_id` and `semester` from the `students` table, execute an initial data migration script.
    *   Create a default `DRAFT` or `CONFIRMED` enrollment record in the `enrollments` table for each student who has a `section_id` in the `students` table, ensuring their `year_level`, `section_id`, and `semester` (resolved to a UUID semester entry) are preserved.
    *   Only after this data transition is confirmed should the `students.section_id` and `students.semester` columns be dropped.

---

## 8. Proposed Test Coverage & Verification Checks

### 8.1 Required Test Coverage
1.  **Backend Compiler Validation**: Run `mvn clean compile` to verify that `ReportService.java` compile issues are resolved.
2.  **Enrollment Validation Service Tests**:
    *   Test validation blocks enrollment if section is null for `REGULAR` students.
    *   Test validation allows enrollment if section is null for `IRREGULAR` or `CROSS_ENROLLEE` students.
    *   Test validation blocks enrollment if the selected section is `INACTIVE`.
    *   Test validation blocks enrollment if the schedule's section capacity is exceeded.
3.  **Section Service Tests**:
    *   Test database unique constraint triggers when creating duplicate sections in the same term.
    *   Test section status toggle blocks activation if no curriculum is assigned.

### 8.2 Proposed Verification Checks (E2E)
*   **Student Profile View Verification**:
    *   Ensure the "Current Enrollment" tab displays the exact term-specific subject list, teacher, room, and capacity slots.
*   **Enrollment Creation Workspace Verification**:
    *   Log in as Registrar. Select a regular student. Verify that if no section is selected, the "Create Draft Enrollment" button remains disabled.
    *   Select an irregular student. Verify that the "Mixed sections" choice allows draft enrollment creation with a null section.
