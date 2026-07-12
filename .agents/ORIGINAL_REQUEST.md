# Original User Request

## Initial Request — 2026-07-11T07:38:57Z

Build the React frontend workflows for the Academic Setup module, including Departments, Programs, Courses, Faculty, Rooms, School Years, Semesters, and Sections. The agent team should first analyze the backend API and database to understand the relationships and flows before implementing the UI.

Working directory: c:\Users\PC\Projects\cis\frontend
Integrity mode: development

## Requirements

### R1. Admin Dashboard Interface
Build a standard admin dashboard layout featuring a persistent sidebar navigation menu and a main content area. The sidebar must provide links to the 8 academic setup modules.

### R2. CRUD Workflows
Implement data tables and forms (create/edit) for each of the following entities: Departments, Programs, Courses, Faculty, Rooms, School Years, Semesters, and Sections.

### R3. API Integration
Integrate the frontend directly with the live Spring Boot API running locally via Docker. Ensure that API authentication/authorization headers are sent properly if required by the backend.

## Acceptance Criteria

### Dashboard & Navigation
- [ ] The Vite development server starts successfully without compilation errors.
- [ ] The user can click through the sidebar to view all 8 academic setup module pages without routing errors.

### Data Management
- [ ] Creating a new entity (e.g., a Department) through the frontend UI results in a 201 Created API response and the new record appears in the UI data table.
- [ ] Updating an existing entity through the frontend UI correctly modifies the record in the backend and reflects the change in the UI.
- [ ] Any server-side validation errors are caught and displayed to the user in the UI forms.

## Follow-up — 2026-07-11T10:28:23Z

Build the React frontend workflows for the Curriculum Management module. The interface should allow administrators to create and manage academic curricula, and include a curriculum builder that displays courses grouped by year level and semester, closely mirroring standard university prospectus documents.

Working directory: c:\Users\PC\Projects\cis\frontend
Integrity mode: development

## Requirements

### R1. Curriculum Listing & CRUD
Implement a data table and forms to create, read, update, and delete Curricula (integrating with `/api/v1/curricula`). Users must be able to specify the curriculum code, name, and associated academic Program.

### R2. Curriculum Builder Interface
Build a detailed builder view for a specific curriculum that visually groups courses by `year_level` and `semester` (e.g., First Year - First Semester). 

### R3. Course & Pre-requisite Assignment
Inside each Year/Semester block, include an "Add Course" button that opens a searchable modal dialog. In this modal, administrators must be able to assign a course to that block and fully manage its pre-requisites and co-requisites.

### R4. Verification Strategy
The implementation must be verified programmatically by ensuring the React application compiles without TypeScript errors (`npm run tsc`) and successfully builds (`npm run build`). 

## Acceptance Criteria

### Curriculum Management
- [ ] The Curriculum listing page displays data fetched from the backend API.
- [ ] Users can successfully create a new Curriculum and save it to the database.

### Curriculum Builder
- [ ] The builder UI accurately groups courses by Year Level and Semester, displaying Course Code, Descriptive Title, and Units (Lec/Lab) similar to a standard prospectus.
- [ ] Users can successfully assign a new course to a specific Year/Semester block using a modal dialog.
- [ ] The course assignment modal allows users to select and save pre-requisite courses.
- [ ] The frontend builds successfully (`npm run build`) with zero TypeScript errors.

## Follow-up — 2026-07-11T12:15:44Z

Build the React frontend workflows for the Student Profiling module. The interface should integrate with the existing Java backend APIs to display, create, and manage comprehensive student profiles (including personal details, contact info, family background, educational history, and documents).

Working directory: `c:\Users\PC\Projects\cis`
Integrity mode: development

## Requirements

### R1. Student List View
Build a searchable data table to display all students. It should allow navigation to individual student profiles.

### R2. Tabbed Profile View
Implement a detailed Student Profile view using a tabbed interface. The tabs should logically organize the data into categories: Personal Details, Contact Information, Family Background, Educational History, and Documents.

### R3. Backend Schema Alignment
The frontend components and data types must strictly align with the existing backend Java entities located in `c:\Users\PC\Projects\cis\src\main\java\com\school\sis\student\entity\*` (e.g., `Student`, `StudentContact`, `StudentEducationalBackground`, etc.). Note that the backend root is `c:\Users\PC\Projects\cis\src`, not `backend/src`.

## Acceptance Criteria

### Implementation Quality
- [ ] A dedicated "Students" route exists and renders the student data table.
- [ ] The Student Profile view successfully implements a tabbed navigation structure.
- [ ] Frontend types and form schemas for the Student module accurately mirror the backend database relationships.

### Verification
- [ ] Running `npm run build` succeeds without any TypeScript errors related to the new student components.
- [ ] The Student list and tabbed profile views render in the browser without runtime crashes.

## Follow-up — 2026-07-11T15:32:51Z

# Teamwork Project Prompt — Draft

> Status: Launched
> Goal: Project is currently being built by the agent team.

Refactor Student Profiling, Sections, Scheduling, and Enrollment Integration to separate long-lived student profile data from term-specific enrollment data.

Working directory: c:\Users\PC\Projects\cis
Integrity mode: development

## Requirements

### R1. Phase 1: Analyze the existing implementation
Before editing code, inspect and document current entities, relationships, database usage, APIs, validation logic, existing data, and permissions. Create a short impact analysis containing affected files, proposed relationships, migration risks, backfill strategy, API/Frontend changes, and required test coverage. Do not begin major refactoring until this is understood.

### R2. Phase 2: Improve the Create Student form
Update the Academic tab of the Create/Edit Student modal. Remove Semester and Section. Keep Program, Curriculum, Current Year Level, Date Admitted, School Year Admitted, Classification, and Academic Status. Update layout to be responsive. Rename fields and update dropdown values as specified (e.g. Enrollment Classification vs Academic Status). Ensure Curriculum is filtered by Program.

### R3. Phase 3: Normalize student and enrollment data
Remove `semester`, `section_id`, and `section_code` from the student model, requests, and responses. Keep profile-level fields. Update the Enrollment model to require `year_level`, keep school year, semester, section, and status. Enforce that section is required unless classification is IRREGULAR or CROSS_ENROLLEE. Update confirmed enrollment logic to update `students.year_level` but NOT store section in the student table. Add a "Current Enrollment" panel to the Student Profile.

### R4. Phase 4: Normalize section relationships
Define section as term-specific. Add `sections.curriculum_id`. Ensure section uniqueness is based on `section_code + school_year_id + semester_id`. Add validation for active status, valid relationships, and capacity. Update the Create Section UI to include the new fields. Handle migrated sections by automatically assigning curriculum if unambiguous, else mark as REQUIRES_SETUP.

## Verification Strategy
Verification will be primarily manual via the UI, supplemented by programmatic checks to ensure the application still builds successfully:
1. Ensure `mvn clean package -DskipTests` succeeds.
2. Ensure `npm run tsc` and `npm run build` succeed in the frontend directory.
3. Start the Docker services and manually verify the Create Student form and Section form behave as required in the UI.

## Acceptance Criteria

### Compilation & Build
- [ ] Backend compiles without errors after the database schema and entity refactor.
- [ ] Frontend compiles without TypeScript errors after updating the DTOs and API types.

### UI Validation
- [ ] The Create/Edit Student modal successfully saves a student profile without requiring a Semester or Section.
- [ ] Changing a Program in the Create/Edit Student modal correctly re-filters Curriculum choices.
- [ ] The Create Section modal correctly allows selecting a Curriculum based on the selected Program.
- [ ] The Student Profile correctly displays the new "Current Enrollment" panel instead of a permanent section.

