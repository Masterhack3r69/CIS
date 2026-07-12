## 2026-07-11T15:38:08Z
You are the Worker agent for the student profiling and enrollment refactoring implementation phase.
Your working directory is: c:\Users\PC\Projects\cis\.agents\worker_refactor_implementation

Please implement the following refactoring and normalization requirements across the codebase:

1. CRITICAL COMPILATION FIX (Phase 3 Backend):
   - Edit `src/main/java/com/school/sis/enrollment/repository/EnrollmentRepository.java`:
     Add method: `java.util.Optional<Enrollment> findFirstByStudentIdOrderBySchoolYearSchoolYearDescSemesterSortOrderDesc(java.util.UUID studentId);`
   - Edit `src/main/java/com/school/sis/report/service/ReportService.java`:
     Update the `academicHeader(PdfReportBuilder pdf, Student student)` method (lines 315-322). Instead of calling `student.getSemester()` and `student.getSection()` (which were removed/dropped in Flyway V10), query the student's latest active/confirmed enrollment using `enrollmentRepository.findFirstByStudentIdOrderBySchoolYearSchoolYearDescSemesterSortOrderDesc(student.getId()).orElse(null)`.
     - For "Semester": output `latestEnrollment != null ? latestEnrollment.getSemester().getName() : ""`.
     - For "Section": output `(latestEnrollment != null && latestEnrollment.getSection() != null) ? latestEnrollment.getSection().getSectionCode() : ""`.

2. ENROLLMENT VALIDATION (Phase 3 Backend):
   - Edit `src/main/java/com/school/sis/enrollment/service/EnrollmentService.java`:
     In the `validateSection` method, if the section is not null, verify that the section's status is `ActiveStatus.ACTIVE`. If the section is inactive, throw a `BusinessRuleException` with an appropriate message (e.g. "Selected section is inactive"). Check `com/school/sis/setup/entity/Section.java` to see how status is accessed (e.g. `getStatus()`).

3. CREATE STUDENT FORM UI IMPROVEMENTS (Phase 2 Frontend):
   - Edit `frontend/src/pages/students-page.tsx`:
     - In the Academic tab of the Create/Edit Student modal, remove the "Semester" and "Section" selection dropdowns/fields.
     - Keep and ensure layout is responsive for: Program, Curriculum, Current Year Level, Date Admitted, School Year Admitted, Classification, and Academic Status.
     - Filter Curricula by Program (when a Program is selected, retrieve and display only the Curricula associated with that Program, and clear the selected Curriculum if the Program changes).

4. STUDENT PROFILE "CURRENT ENROLLMENT" PANEL (Phase 3 Frontend):
   - Edit `frontend/src/pages/students-page.tsx`:
     - In the Student detail profile views, remove the display of a permanent section (since section is now term-specific and stored in the enrollment table, not the student table).
     - Add a "Current Enrollment" tab/panel to the student profile.
     - Perform a query to fetch the student's latest enrollment using the existing TanStack React Query or fetch hooks. If no hook exists, use an API fetch to `/api/v1/enrollments?studentId=${id}&size=1`.
     - If an enrollment exists, display the current Term (School Year & Semester), Year Level, Section Code, and Status. Below it, render a table/list of the enrolled subjects (Course Code, Descriptive Title, Section, Room, Faculty, Credit Units, Schedule Meetings).
     - If no enrollment exists, render an alert panel saying "No enrollment recorded for this student."

5. SECTION NORMALIZATION & UI SETUP (Phase 4 Frontend):
   - Edit `frontend/src/pages/setup/sections-tab.tsx`:
     - Update the Create/Edit Section form/dialog to require selecting a Program and Curriculum.
     - Filter the available Curricula dropdown based on the selected Program.
     - Support selecting active school years and semesters from dropdowns.
     - Ensure Zod validation schema validates these fields correctly.

6. COMPILATION & BUILD VERIFICATION:
   - Ensure the backend compiles successfully by running: `mvn clean package -DskipTests`
   - Ensure the frontend compiles and builds successfully by running: `npm run tsc` and `npm run build` in the `frontend` directory.

Write a detailed summary of all modified files and verified build outputs in your handoff report.

MANDATORY INTEGRITY WARNING:
DO NOT CHEAT. All implementations must be genuine. DO NOT
hardcode test results, create dummy/facade implementations, or
circumvent the intended task. A Forensic Auditor will independently
verify your work. Integrity violations WILL be detected and your
work WILL be rejected.
