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
