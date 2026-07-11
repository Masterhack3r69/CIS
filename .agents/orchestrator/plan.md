# Implementation Plan: Student Profiling Frontend Workflows

## Objectives
Implement the React frontend workflows for the Student Profiling module, ensuring full alignment with backend APIs and data types, complete user flows for list/create/edit/documents, and successful compilation/build.

## Milestones

### Milestone 1: Exploration and API/Schema Mapping
- Explore backend Java entity models in `src/main/java/com/school/sis/student/entity/*` and DTOs.
- Explore existing frontend state, types, and endpoints.
- Define explicit typescript models and endpoints in `PROJECT.md`.
- Output: Explorer analysis report.

### Milestone 2: API Hooks & Types Definition
- Update `frontend/src/lib/types.ts` with complete type definitions matching backend Java fields.
- Create React Query query/mutation hooks (`frontend/src/hooks/use-students.ts` or extend existing setup) for all Student API endpoints:
  - List / Search students
  - Retrieve single student
  - Create student
  - Update student
  - Update student status
  - Upload student document
  - List student documents
  - Verify student document
- Output: Subagent implementation verification.

### Milestone 3: Student List View & Search
- Update the student list view to support full searching, pagination, and filters (e.g. status, program, year level, section).
- Add "New Student" button and flow (modal dialog or dedicated create route) that allows setting up a student's personal details, contact info, family background, educational background, and academic info.
- Output: Subagent implementation verification.

### Milestone 4: Student Detail & Tabbed Profile View
- Update the student detail page with edit capability for each tab:
  - Personal Details (Edit form)
  - Contact Information (Edit form)
  - Family Background (Edit form)
  - Educational History (Edit form)
  - Academic Details (Edit form/status change)
- Ensure all forms use validation matching backend JSR-380 constraints via React Hook Form and Zod.
- Output: Subagent implementation verification.

### Milestone 5: Document Upload & Verification
- Implement Document Management tab in the student detail view.
- Support uploading files for various document types.
- Support displaying document verification status (Pending, Submitted, Verified, Rejected, Missing) and verification remarks.
- Add document verification action (for authorized users).
- Output: Subagent implementation verification.

### Milestone 6: Build & E2E Validation
- Verify compilation with `npm run tsc` in the frontend directory.
- Verify production build with `npm run build`.
- Verify runtime rendering and integration.
- Run Forensic Auditor checks to ensure clean, non-cheating implementations.
- Output: Clean build and auditor report.
