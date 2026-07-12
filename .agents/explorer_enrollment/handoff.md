# Handoff Report: Enrollment and Schedule Exploration

## 1. Observation
The following file structures and sections were directly inspected:

*   **Backend Enrollment Creation**: In `src/main/java/com/school/sis/enrollment/service/EnrollmentService.java` (lines 111-136), the creation code does not assign any subjects/schedules to the newly created enrollment:
    ```java
    Enrollment enrollment = new Enrollment();
    enrollment.setStudent(student);
    enrollment.setProgram(student.getProgram());
    enrollment.setSchoolYear(schoolYear);
    enrollment.setSemester(semester);
    enrollment.setYearLevel(request.yearLevel());
    enrollment.setSection(section);
    enrollment.setRemarks(request.remarks());
    enrollment.setStatus(EnrollmentStatus.DRAFT);
    Enrollment saved = enrollmentRepository.save(enrollment);
    ```
*   **Backend Cancellation Endpoint**: In `src/main/java/com/school/sis/enrollment/controller/EnrollmentController.java` (lines 97-101), the cancel method accepts no request body payload:
    ```java
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('ENROLLMENT_APPROVE')")
    public ApiResponse<EnrollmentResponse> cancel(@PathVariable UUID id) {
        return ApiResponse.success("Enrollment cancelled", enrollmentService.cancel(id));
    }
    ```
*   **Frontend Enrollment Page**: In `frontend/src/pages/enrollment-page.tsx` (lines 31-35), flexible classifications send `null` section IDs:
    ```typescript
    const flexible = student.data?.academic.classification === "IRREGULAR" || student.data?.academic.classification === "CROSS_ENROLLEE"
    const assignmentReady = flexible ? !!sectionChoice : !!sectionChoice && sectionChoice !== "__mixed__"
    const scheduleSectionQuery = sectionChoice && sectionChoice !== "__mixed__" ? `&sectionId=${sectionChoice}` : ""
    ```
    And the `create` mutation sends `sectionId: sectionChoice === "__mixed__" ? null : sectionChoice`.
*   **E2E Tests**: A lookup of files in `frontend/e2e/specs/` found 5 files: `curriculum.spec.ts`, `tier1_feature_coverage.spec.ts`, `tier2_boundary_validation.spec.ts`, `tier3_cross_feature.spec.ts`, and `tier4_real_world.spec.ts`. RiPGREP search for "enrollment" inside this folder returned **0 results**.
*   **Compilation State**: Executed `mvn clean test-compile` on the workspace root, which returned `BUILD SUCCESS`.

---

## 2. Logic Chain
1.  **Draft Auto-population (R1)**: Creating an enrollment in the backend only saves the header and leaves `subjects` empty. Therefore, regular student draft enrollment currently does not auto-populate section schedules.
2.  **Section Designation (R2)**: The frontend sends `sectionId: null` when the registrar chooses `"__mixed__"`, and the backend accepts `section == null` for irregular classifications. This contradicts the requirement: "They must be allowed to have a 'Mixed sections' designation, but cannot use a null section." This indicates a database or logical gap for mixed sections.
3.  **UI Layout and Records (R3)**: The current `App.tsx` and `enrollment-page.tsx` route only displays the active enrollment workspace. The tabs for "Enrollment Records" and "Enroll Student", along with the listing, search, filtering, and detailed inspect views for past records, are completely missing from the UI.
4.  **API Payloads (R3/R5)**: The cancellation API lacks a request body parameter for capturing the reason, and the backend service hardcodes the reason to `"Enrollment cancelled"`. The UI lacks any prompt to enter the reason.
5.  **Schedule Filtering (R4)**: The "Filter" button on the schedules table is static, and the backend query criteria does not support filtering schedules by `curriculumId` or `yearLevel`.

---

## 3. Caveats
*   **Mixed Section Representation**: We assume that representing "Mixed sections" will require either a special Section record seeded in the database per term/program with section code `"MIXED"`, or a virtual resolution layer. Creating parallel tables is prohibited.
*   **Prerequisite Override**: The JBP description mentions "Authorized users may override prerequisite validation with remarks," but this is currently not present in the backend validation or confirmation endpoints.

---

## 4. Conclusion
The codebase is currently in a compiling state, but several functional and user interface gaps exist between the current codebase and requirements R1-R5. These gaps include missing backend auto-population logic, incomplete schedule checks, missing UI tabs/grids for enrollment records, lack of cancellation payload fields, and lack of visual indicators for full/unavailable schedules.

---

## 5. Verification Method
1.  **Backend Compilation**: Run `mvn clean compile` in the workspace root. It must build successfully.
2.  **Existing Backend Tests**: Run `mvn test`. All 11 test suites must pass.
3.  **E2E Specs Audit**: Inspect `frontend/e2e/specs/` to verify that no specs currently test schedules or enrollments directly.
