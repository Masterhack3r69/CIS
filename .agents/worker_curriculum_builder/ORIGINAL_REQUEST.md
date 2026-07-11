# Curriculum Builder & Course/Requirement Assignment Implementation Task

## Objective
Implement the detailed Curriculum Builder interface and course/pre-requisite/co-requisite assignment workflows in `frontend/src/pages/setup/curriculum-builder.tsx`.

## Details
1. **API Integration**:
   - Fetch the curriculum data using `useCurriculumChecklist(id)` (from `use-curriculum.ts` / `/api/v1/curricula/{id}/checklist`).
   - Use the mutations:
     - `useAddCurriculumCourse(id)` for assigning a course.
     - `useUpdateCurriculumCourse(id)` for updating an assignment.
     - `useDeleteCurriculumCourse(id)` for removing an assignment.
   - Fetch available courses using `useCourses` (from `use-setup.ts`) with a query or large size (e.g., search text, page=1, size=100) to populate selection options in the modal.

2. **Builder Layout**:
   - At the top, show a header card with curriculum info: Code, Name, Program (code/name), Status (Draft/Active/etc.), Effective School Year, Version, and Description.
   - Include a "Back to Curricula" button that navigates back to `/setup/curricula`.
   - Iterate through `terms` from the API response:
     - Group headings must map year levels and semesters to human-readable names (e.g., `1` -> "First Year", `2` -> "Second Year", etc.; `FIRST_SEMESTER` -> "First Semester", `SECOND_SEMESTER` -> "Second Semester", `SUMMER` -> "Summer").
     - Show the term totals: Lecture Hours, Lab Hours, and Credit Units, directly using `totalLectureHours`, `totalLaboratoryHours`, and `totalCreditUnits` returned by the API.
     - Within each term block, render a table listing assigned courses showing: Course Code, Descriptive Title, Lec/Lab Hours, Credit Units, Pre-requisites (list of codes), Co-requisites (list of codes), Required Status (Required/Optional/Elective), and action buttons (Edit, Remove).

3. **Add/Edit Course Assignment Modal**:
   - In each term block, include an "Add Course" button. Clicking this opens a dialog modal.
   - Form fields:
     - **Course**: Searchable select dropdown/combobox of courses. Show course code and descriptive title. Ensure the selected course id is sent as `courseId`.
     - **Sort Order**: Number input, must be >= 1, defaults to 1.
     - **Requirement Status**: Dropdown selector (`REQUIRED`, `OPTIONAL`, `ELECTIVE`), defaults to `REQUIRED`.
     - **Prerequisite Courses**: Multi-select or checkbox-based selector allowing selection of multiple courses from all courses in the system. Submits an array of course UUIDs under `prerequisiteCourseIds`.
     - **Corequisite Courses**: Multi-select or checkbox-based selector allowing selection of multiple courses. Submits an array of course UUIDs under `corequisiteCourseIds`.
   - On submission:
     - For **Add**: call `POST /api/v1/curricula/{id}/courses` via hook.
     - For **Edit**: call `PUT /api/v1/curricula/{id}/courses/{curriculumCourseId}` via hook.
   - Ensure the modal displays API validation errors correctly if returned by the server.

4. **Delete Assignment Action**:
   - In the course rows, include a "Remove" button.
   - Clicking "Remove" opens a confirmation dialog, and on approval calls `DELETE /api/v1/curricula/{id}/courses/{curriculumCourseId}`.

5. **Compilation Check**:
   - Verify frontend compiles cleanly using `npm run typecheck` and `npm run build`.

## MANDATORY INTEGRITY WARNING
> DO NOT CHEAT. All implementations must be genuine. DO NOT
> hardcode test results, create dummy/facade implementations, or
> circumvent the intended task. A Forensic Auditor will independently
> verify your work. Integrity violations WILL be detected and your
> work WILL be rejected.

## 2026-07-11T10:37:38Z
Please implement the Curriculum Builder UI and course assignment workflows. Read the details and constraints in c:\Users\PC\Projects\cis\.agents\worker_curriculum_builder\ORIGINAL_REQUEST.md. Ensure the project builds cleanly, and reply with a report when done.
