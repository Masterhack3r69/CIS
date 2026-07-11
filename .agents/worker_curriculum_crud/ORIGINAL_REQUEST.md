# Curriculum CRUD Implementation Task

## Objective
Implement the types, React Query hooks, routing, navigation, and listing/CRUD tab for Curriculum Management in the React frontend.

## Details
1. **TypeScript Types**:
   Update `frontend/src/lib/types.ts` to include:
   - `CurriculumStatus` ('DRAFT' | 'ACTIVE' | 'INACTIVE' | 'ARCHIVED')
   - `RequiredStatus` ('REQUIRED' | 'OPTIONAL' | 'ELECTIVE')
   - `CurriculumRequest`
   - `CurriculumResponse`
   - `CourseLinkResponse`
   - `CurriculumCourseRequest`
   - `CurriculumCourseResponse`
   - `CurriculumDetailResponse`
   - `CurriculumTermResponse`
   - `CurriculumChecklistResponse`

2. **React Query Hooks**:
   Create a new file `frontend/src/hooks/use-curriculum.ts` containing queries and mutations:
   - `useCurricula(search?: string, page?: number, size?: number)` for paginated list from `/curricula`
   - `useCreateCurriculum()` for `POST /curricula`
   - `useUpdateCurriculum()` for `PUT /curricula/{id}`
   - `useActivateCurriculum()` for `POST /curricula/{id}/activate`
   - `useCurriculumChecklist(id: string)` for `/curricula/{id}/checklist`
   - `useAddCurriculumCourse(id: string)` for `POST /curricula/{id}/courses`
   - `useUpdateCurriculumCourse(id: string)` for `PUT /curricula/{id}/courses/{ccId}`
   - `useDeleteCurriculumCourse(id: string)` for `DELETE /curricula/{id}/courses/{ccId}`

3. **Routing and Navigation Links**:
   - In `frontend/src/pages/setup/setup-layout.tsx`, add a new tab `{ to: "curricula", label: "Curricula" }` to the `tabs` array.
   - In `frontend/src/App.tsx`, import `CurriculaTab` from `@/pages/setup/curricula-tab` and `CurriculumBuilder` from `@/pages/setup/curriculum-builder`.
   - Add routes under `/setup`:
     - `<Route path="curricula" element={<CurriculaTab />} />`
     - `<Route path="curricula/:id" element={<CurriculumBuilder />} />`

4. **Curricula Tab UI (`frontend/src/pages/setup/curricula-tab.tsx`)**:
   - Create the page using standard Tailwind and shadcn styling similar to other tabs.
   - Display a paginated, searchable table of curricula. Show columns: Code, Name, Program (display program code/name), School Year, Version, Status, and Actions.
   - Load programs for form dropdown using the existing `usePrograms` hook from `use-setup.ts`.
   - Implement "Create Curriculum" and "Edit" dialog forms (supporting code, name, program selection, effective school year, version, status, and description fields). Use React Hook Form + Zod validation matching backend constraints.
   - Statuses should be mapped to select option values: `DRAFT`, `ACTIVE`, `INACTIVE`, `ARCHIVED`.
   - Add an "Activate" button for Draft/Inactive curricula, which opens a warning modal asking the user to confirm activation (noting that other curricula under the same program will be set to inactive).
   - Add an "Open Builder" button/link that routes the user to `/setup/curricula/:id`.

5. **Skeleton Curriculum Builder (`frontend/src/pages/setup/curriculum-builder.tsx`)**:
   - Create a skeleton page that displays the curriculum name, code, program, status, and a button to return to the Curricula tab.

6. **Compilation Check**:
   - Run `npm run tsc` and `npm run build` in the `frontend` folder to verify compilation.

## MANDATORY INTEGRITY WARNING
> DO NOT CHEAT. All implementations must be genuine. DO NOT
> hardcode test results, create dummy/facade implementations, or
> circumvent the intended task. A Forensic Auditor will independently
> verify your work. Integrity violations WILL be detected and your
> work WILL be rejected.

## 2026-07-11T10:32:21Z
Please implement the Curriculum CRUD frontend workflows. Read the details and constraints in c:\Users\PC\Projects\cis\.agents\worker_curriculum_crud\ORIGINAL_REQUEST.md. Ensure the project builds cleanly, and reply with a report when done.
