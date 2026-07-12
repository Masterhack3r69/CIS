## 2026-07-12T03:40:50Z

Implement the backend changes for the Enrollment and Schedule Management modules:

1. Update `src/main/java/com/school/sis/setup/repository/SectionRepository.java` to add:
   `java.util.Optional<Section> findBySectionCodeAndSchoolYearIdAndSemesterId(String sectionCode, java.util.UUID schoolYearId, java.util.UUID semesterId);`

2. Update `src/main/java/com/school/sis/schedule/dto/ScheduleSearchCriteria.java` to add:
   `java.util.UUID curriculumId` and `Integer yearLevel` to the record.

3. Update `src/main/java/com/school/sis/schedule/controller/ScheduleController.java` to:
   - Accept `@RequestParam(required = false) UUID curriculumId` and `@RequestParam(required = false) Integer yearLevel` in the `list` method.
   - Pass these new params when instantiating `new ScheduleSearchCriteria(...)`.

4. Update `src/main/java/com/school/sis/schedule/service/ScheduleService.java` to:
   - Add filters for `curriculumId` and `yearLevel` to the JPA Criteria query in `specification(ScheduleSearchCriteria criteria)`. Filter section curriculum and section year level:
     - `criteria.curriculumId() != null` -> `cb.equal(root.get("section").get("curriculum").get("id"), criteria.curriculumId())`
     - `criteria.yearLevel() != null` -> `cb.equal(root.get("section").get("yearLevel"), criteria.yearLevel())`

5. Create `src/main/java/com/school/sis/enrollment/dto/EnrollmentCancelRequest.java` as a new Java record:
   ```java
   package com.school.sis.enrollment.dto;
   
   import jakarta.validation.constraints.NotBlank;
   
   public record EnrollmentCancelRequest(
       @NotBlank(message = "Reason is required") String reason
   ) {}
   ```

6. Update `src/main/java/com/school/sis/enrollment/controller/EnrollmentController.java` to:
   - Accept `@Valid @RequestBody EnrollmentCancelRequest request` in `cancel(@PathVariable UUID id, ...)`.
   - Pass the reason to the service: `enrollmentService.cancel(id, request.reason())`.

7. Update `src/main/java/com/school/sis/enrollment/service/EnrollmentService.java`:
   - Modify the signature of `cancel(UUID id)` to `cancel(UUID id, String reason)`: set `remarks` of the enrollment to `reason`, record status history using `reason`, and log `reason` in `auditService.log`.
   - Implement "Mixed sections" designation logic:
     - For `IRREGULAR` and `CROSS_ENROLLEE` classifications (flexible students): if `sectionId` is null (or when chosen), dynamically resolve (and create if not exists) a section with `sectionCode` = `"MIXED-" + student.getProgram().getProgramCode() + "-" + yearLevel` for the current school year, semester, and year level. Capacity should be 999.
     - For other classifications: throw `BusinessRuleException` if `sectionId` is null.
     - Add helper method `private boolean isMixedSection(Section section)` that checks if section code starts with `"MIXED"`.
     - In `validateScheduleForEnrollment`, if the enrollment section is a mixed section, do NOT enforce that the schedule's section matches the enrollment's section. All other validations (status, term, program, curriculum eligibility) still apply.
   - Implement draft auto-population logic for regular flow:
     - In `create(EnrollmentRequest request)`: if the student is `REGULAR` (or non-flexible) and a section is resolved, fetch all active schedules matching the section, school year, and semester. Check if their courses exist in the student's curriculum for the enrollment's year level and semester. Automatically add these schedules to the enrollment's subjects list as `ENROLLED` within the same transaction.
   - Implement completeness validation for regular flow:
     - In `validateEnrollment(Enrollment enrollment)`: for `REGULAR` students (or non-flexible), ensure that the enrollment contains a selected schedule for every required course in the curriculum for that year level and semester. If any is missing, add a blocking issue `REQUIRED_COURSE_MISSING` (message: `"Enrollment is missing required course: [CourseCode]"`).
     - Make sure that other validation checks (time conflicts, capacity, active status) block confirmation if violated.

MANDATORY INTEGRITY WARNING — DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A Forensic Auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.

Run `mvn clean test-compile` to verify compilation, and run `mvn test` to ensure existing backend tests pass. Let us know when completed.

## 2026-07-12T11:46:15+08:00

Implement the frontend changes for the Enrollment workspace in `frontend/src/pages/enrollment-page.tsx`:

1. Tab Structure:
   - Wrap the page in a shadcn Tabs component with two tabs: "Enroll Student" (value: "builder") and "Enrollment Records" (value: "records").
   - Import necessary components: `import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"`.
   - "Enroll Student" tab should render the current student search and active enrollment building view.
   - "Enrollment Records" tab should render the list of all past and current enrollment records.

2. "Enrollment Records" Tab Implementation:
   - Fetches paginated enrollment records from `GET /api/v1/enrollments` with state-controlled filters:
     - `search` (text search matching student name, student number, or program)
     - `schoolYearId` (select dropdown populated from `years.data`)
     - `semesterId` (select dropdown populated from `semesters.data`)
     - `status` (select dropdown: ALL, DRAFT, CONFIRMED, CANCELLED)
     - `page` & `size` (standard pagination)
   - Renders a data table showing:
     - Student Name & Number
     - Program & Year Level
     - Section Code (if section starts with "MIXED", display "Mixed sections", otherwise display the section code)
     - Status Badge (DRAFT -> secondary/gray, CONFIRMED -> success/green, CANCELLED -> destructive/red)
     - Term (School Year / Semester)
     - Action Buttons:
       - **Inspect**: Opens a dialog/sheet displaying the detailed enrollment view. Shows:
         - Student details, academic profile.
         - Enrolled subjects in a table (Course Code, Title, Section, Room, Faculty, Schedule, Units).
         - Total Units.
         - Status History list (retrieved from enrollment detail or mapped).
         - Download buttons (only if status is CONFIRMED): "Enrollment Form (PDF)" and "Assessment (PDF)" using `openPdf`.
       - **Resume Draft**: Only visible if status is `DRAFT`. Clicking it:
         - Sets `selectedId` to `studentId` (active student).
         - Sets `yearId` to `schoolYearId`.
         - Sets `semesterId` to `semesterId`.
         - Sets `yearLevel` to `yearLevel`.
         - Sets `sectionChoice` to `sectionId ?? "__mixed__"`.
         - Switches active tab to "builder".
       - **Cancel**: Only visible if status is `DRAFT` or `CONFIRMED`. Clicking it:
         - Opens an AlertDialog or Dialog prompting for a cancellation reason (textarea, required).
         - Submitting calls `POST /api/v1/enrollments/{id}/cancel` sending request body `{ reason }`.
         - Shows toast notification on success and refetches the list.

3. Schedule Filters in Builder:
   - In the "Available Class Schedules" section of the builder, make the static Filter button functional or add inline filter controls (collapsible or permanent):
     - Course Code / Title filter (text input)
     - Section filter (text input or dropdown of available sections)
     - Day of Week filter (select dropdown: Monday, Tuesday, etc.)
     - Availability filter (toggle or select: "All" vs "Available Only")
   - Filter the schedules list client-side based on these input values before rendering the table rows.

4. Schedule Availability Visual Indicators:
   - For each row in the available schedules table, compute:
     - `isSelected = selected.has(s.id)`
     - `isFull = s.availableSeats <= 0`
     - `hasConflict = !isSelected && Array.from(selected).some(selId => { const selSched = schedules.data?.items.find(x => x.id === selId); return selSched ? hasFrontendMeetingConflict(selSched, s) : false; })`
   - Define a frontend time-overlap check helper `hasFrontendMeetingConflict(s1, s2)` comparing meetings (dayOfWeek, startTime, and endTime).
   - Display a status Badge in the row:
     - Selected -> `<Badge className="bg-emerald-600 text-white">Selected</Badge>`
     - Full -> `<Badge variant="destructive">Full</Badge>`
     - Conflict -> `<Badge variant="outline" className="border-amber-500 text-amber-600 bg-amber-50">Conflict</Badge>`
     - Available -> `<Badge variant="outline" className="border-emerald-500 text-emerald-600 bg-emerald-50">Available</Badge>`
   - Update the row checkbox disabled condition: `disabled={locked || busy || (!isSelected && (isFull || hasConflict))}`.

MANDATORY INTEGRITY WARNING — DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A Forensic Auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.

Once completed, run `npm run tsc` and `npm run build` in the `frontend` folder to make sure there are no TypeScript or bundler errors. Let us know when completed.

