# Refactoring Review Report — Student Profiling, Sections, Scheduling, and Enrollment Integration

**Verdict**: REQUEST_CHANGES (FAIL)
**Overall Risk Assessment**: MEDIUM

---

## 1. Quality Review Summary

The refactoring work done in Phase 2-4 shows high quality in the logical implementation:
- Student profile data is cleanly separated from term-specific enrollment data.
- The React forms for creating/editing students no longer require semester or section fields, and correctly filter curricula based on the selected program.
- Enrollment confirmations update student year levels but do not persist sections in the `students` table, matching normalization goals.
- Frontend TypeScript type check (`npm run tsc`) and production build (`npm run build`) completed successfully with zero compilation errors.
- Unit and integration tests for the modified backend files (`EnrollmentServiceTests`, `FeeAssessmentServiceTests`, `GradeServiceTests`, `ReportServiceTests`, and `ScheduleServiceTests`) pass successfully.

However, a **build block/test failure** exists in the backend test suite due to missing database constraints in the H2 test environment schema.

---

## 2. Findings

### [Critical] Finding 1: Test Failure in `SectionDuplicateCodeTests.rejectsDuplicateSectionCodeInSameTerm`
- **What**: The unit test `SectionDuplicateCodeTests.rejectsDuplicateSectionCodeInSameTerm` fails because no database constraint violation is thrown when creating duplicate sections in the same term.
- **Where**: `src/test/java/com/school/sis/setup/SectionDuplicateCodeTests.java` (Line 108) and `src/main/java/com/school/sis/setup/entity/Section.java`.
- **Why**: 
  - Flyway migrations are disabled in the test profile (`flyway.enabled: false` in `application-test.yml`).
  - The unique constraint `sections_unique_term` is defined only in the PostgreSQL migration `V1__foundation_auth_and_setup.sql`.
  - The `Section` JPA entity lacks `@Table(uniqueConstraints = ...)` or other JPA-level constraint definitions, causing Hibernate's auto-DDL (`ddl-auto: create-drop`) to generate the table without a unique constraint in H2.
  - The `SectionService` does not validate section uniqueness at the application level before calling `sectionRepository.save(section)`.
- **Suggestion**: 
  - **Option A (Recommended)**: Add a unique constraint definition to the `@Table` annotation of the `Section` entity in `Section.java`:
    ```java
    @Table(name = "sections", uniqueConstraints = {
        @UniqueConstraint(name = "sections_unique_term", columnNames = {"section_code", "school_year_id", "semester_id"})
    })
    ```
  - **Option B**: Add an application-level duplicate check in `SectionService.java` (e.g., query the repository for duplicate sections and throw a `BusinessRuleException` if found).

---

## 3. Verified Claims

- **Frontend Typecheck passes** → Verified via `npm run tsc` in `frontend/` → **PASS**
- **Frontend Production Build succeeds** → Verified via `npm run build` in `frontend/` → **PASS**
- **Backend modified tests pass** → Verified via `mvn test` running `EnrollmentServiceTests`, `FeeAssessmentServiceTests`, `GradeServiceTests`, `ReportServiceTests`, `ScheduleServiceTests` → **PASS**
- **Curriculum filtering in Student Form** → Verified via code review of `frontend/src/pages/students-page.tsx` (`filteredCurricula`) → **PASS**
- **Curriculum filtering in Section Form** → Verified via code review of `frontend/src/pages/setup/sections-tab.tsx` (`curricula`) → **PASS**
- **Removal of semester/section in Student forms** → Verified via code review of `students-page.tsx` (`academicSchema`) → **PASS**
- **Current Enrollment panel in Student Profile** → Verified via code review of `students-page.tsx` (`CurrentEnrollmentTab`) → **PASS**

---

## 4. Adversarial Review (Critic) & Challenge Report

### [High] Challenge 1: Absence of Application-level Section Uniqueness Checks
- **Assumption Challenged**: Uniqueness checks can be left entirely to database constraints.
- **Attack Scenario**: If database constraints are bypassed (e.g., during database migrations or if the constraint is accidentally dropped/altered), the application will permit saving duplicate section codes in the same school year and semester. This will cause duplicate sections to appear in UI select lists, breaking schedules and enrollment mappings.
- **Blast Radius**: Large. Direct impact on enrollment routing, schedule mappings, and fee assessments.
- **Mitigation**: Introduce an application-level validation rule in `SectionService` checking for duplicates before insertion, raising a clear, localized `BusinessRuleException` rather than relying on raw SQL/constraint violation exceptions.

### [Medium] Challenge 2: Lack of Program-Curriculum Coherency Validation in Section Creation
- **Assumption Challenged**: Curricula selected for a section always belong to the same program.
- **Attack Scenario**: While the frontend filters the dropdown choices, an API client can send a direct POST request to `/api/v1/sections` with a mismatched `programId` and `curriculumId`.
- **Blast Radius**: Medium. Broken section profiles where the curriculum belongs to BSCS but the section is marked as BSIT.
- **Mitigation**: Verified that `SectionService.apply()` checks this constraint:
  ```java
  if (!curriculum.getProgram().getId().equals(program.getId())) {
      throw new BusinessRuleException("Curriculum does not belong to the selected program");
  }
  ```
  *(Mitigated correctly in service layer)*

---

## 5. Coverage Gaps & Unverified Items

- **Docker/Postgres Integration runtime testing** — Risk: Low/Medium — Recommendation: Run and verify migrations and E2E flows against a live Postgres instance once the test failure is fixed.
