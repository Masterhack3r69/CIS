# Original User Request

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
