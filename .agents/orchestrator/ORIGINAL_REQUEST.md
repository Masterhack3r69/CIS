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
