# Task: Forensic Integrity Audit of Student Profiling Workflows

## Objective
Perform a forensic audit of the newly implemented Student Profiling frontend files to verify complete authenticity and integrity. Ensure there is no hardcoding, fake/dummy implementations, or cheating logic.

## Scope of Inspection
- `frontend/src/lib/types.ts`
- `frontend/src/hooks/use-students.ts`
- `frontend/src/pages/students-page.tsx`
- Any related components or routes.

## Audit Checks
1. Check for hardcoded API responses or mocked data in hook files or pages.
2. Verify that API calls are actually sent to the backend endpoints (`/api/v1/students...`) and query parameters/JSON bodies are properly formatted.
3. Validate that form fields and validation rules correspond directly to backend Java validation constraints.
4. Verify that the build output compiles without typescript errors.
5. Report any cheating or integrity violation immediately in the handoff report.
