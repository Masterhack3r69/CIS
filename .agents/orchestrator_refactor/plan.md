# Refactoring Plan: Student Profiling and Enrollment normalization

## Milestones

### Milestone 1: Phase 1 Analysis & Impact Assessment
- **Objective**: Inspect the existing implementation (entities, database schemas, API, frontend views) and generate a comprehensive impact analysis report.
- **Scope**: Locate and document all affected files, schemas, APIs, validation rules, data constraints, migration risks, and testing requirements.
- **Deliverable**: `impact_analysis.md` in the working directory.
- **Status**: PLANNED

### Milestone 2: Phase 2 Create Student Form UI Improvements
- **Objective**: Improve the Create/Edit Student modal UI as per R2.
- **Scope**:
  - Remove Semester and Section from the Academic tab.
  - Keep/rename fields: Program, Curriculum, Current Year Level, Date Admitted, School Year Admitted, Classification, Academic Status.
  - Make form responsive.
  - Add filtering for Curriculum by Program.
- **Status**: PLANNED

### Milestone 3: Phase 3 Student/Enrollment Backend Refactoring
- **Objective**: Normalize Student and Enrollment entities/DTOs on the backend.
- **Scope**:
  - Remove `semester`, `section_id`, and `section_code` from student database tables, Hibernate entities, and request/response DTOs.
  - Update the Enrollment model/database table to require `year_level`.
  - Enforce validation: Section is required in enrollment unless classification is IRREGULAR or CROSS_ENROLLEE.
  - Update enrollment confirmation logic: update `students.year_level`, but do NOT store section in the student table.
- **Status**: PLANNED

### Milestone 4: Phase 3 Student/Enrollment Frontend Refactoring
- **Objective**: Update the Student Profile and general student pages to reflect data normalization.
- **Scope**:
  - Remove reference to permanent section in student display views.
  - Add a "Current Enrollment" panel to the Student Profile page, displaying the term-specific enrollment details.
  - Ensure all compilation and type checks pass.
- **Status**: PLANNED

### Milestone 5: Phase 4 Section Backend Refactoring
- **Objective**: Normalize sections and make them term-specific on the backend.
- **Scope**:
  - Add `curriculum_id` to section database table and entity.
  - Enforce section uniqueness constraint based on `section_code + school_year_id + semester_id`.
  - Add validations: active status check, relationship validation, and capacity constraints.
  - Implement migration logic to assign curriculum to existing sections if unambiguous, or set status to `REQUIRES_SETUP`.
- **Status**: PLANNED

### Milestone 6: Phase 4 Section Frontend Refactoring
- **Objective**: Update Create/Edit Section UI and list views.
- **Scope**:
  - Update the Create Section UI to include the program and curriculum selection fields.
  - Ensure validations and constraints are checked when creating or updating sections.
- **Status**: PLANNED

### Milestone 7: Integration & Build Verification
- **Objective**: End-to-end compilation, build, and manual/automated verification.
- **Scope**:
  - Verify backend compiles: `mvn clean package -DskipTests` succeeds.
  - Verify frontend builds: `npm run tsc` and `npm run build` succeed in the frontend directory.
  - Verify UI behaviors and data constraints manually.
- **Status**: PLANNED
