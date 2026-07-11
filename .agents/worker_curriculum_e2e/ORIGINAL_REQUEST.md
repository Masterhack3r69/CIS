# E2E Verification & Build Hardening for Curriculum Management

## Objective
Programmatically verify the new Curriculum Management workflows by writing Playwright E2E tests, running the test suite, and executing production frontend build checks.

## Details
1. **Playwright E2E Tests**:
   - Write a new E2E test file: `frontend/e2e/specs/curriculum.spec.ts` (or add to an existing file if appropriate).
   - The test suite must cover:
     - **Navigation**: Login, navigate to Academic Setup, select "Curricula" tab, verify the URL is `/setup/curricula` and the tab is active.
     - **Curriculum CRUD**:
       - Create a new Curriculum (fills Program dropdown, Code, Name, School Year, Version, Status='DRAFT', Description, and submits). Verifies the new curriculum appears in the list.
       - Edit the Curriculum (modifies name or description). Verifies change.
       - Activate the Curriculum (clicks Activate, confirms the warning dialog). Verifies the status updates to `ACTIVE` in the table.
     - **Curriculum Builder**:
       - Click "Open Builder" (or navigate to `/setup/curricula/{id}`).
       - Verify it displays the header card with correct metadata.
       - Click "Add Course" inside a term block (e.g. First Year - First Semester).
       - In the modal, select a course, set sort order = 1, required status = REQUIRED, select one or more courses as prerequisites, and submit.
       - Verify that the course is successfully assigned and displayed under the correct term heading, showing Course Code, Descriptive Title, Lec/Lab Hours, Credit Units, Pre-requisite codes, and Required status.
       - Also verify that the term summary (at the bottom/summary of the block) aggregates and displays correct totals for Lecture Hours, Lab Hours, and Credit Units.
       - Edit the assigned course (e.g. change sort order or requirements) and verify.
       - Remove/Delete the course assignment and verify it no longer appears in the block.

2. **Run E2E Tests**:
   - Execute the newly written E2E tests using Playwright.
   - Execute the existing E2E test suite to ensure zero regressions in other Academic Setup modules (Departments, Programs, etc.).
   - Report test outcomes (number of tests run, pass/fail status, command logs).

3. **Production Compilation & Build Checks**:
   - In the `frontend` folder, run:
     - `npm run typecheck`
     - `npm run build`
   - Confirm both commands complete with zero errors.

## MANDATORY INTEGRITY WARNING
> DO NOT CHEAT. All implementations must be genuine. DO NOT
> hardcode test results, create dummy/facade implementations, or
> circumvent the intended task. A Forensic Auditor will independently
> verify your work. Integrity violations WILL be detected and your
> work WILL be rejected.

## 2026-07-11T10:40:52Z
Please write and execute the E2E tests and production build verification for Curriculum Management. Read details and constraints in c:\Users\PC\Projects\cis\.agents\worker_curriculum_e2e\ORIGINAL_REQUEST.md. Reply with a full test and compilation report.
