## 2026-07-12T00:00:25Z
You are the Worker agent for the student profiling and enrollment refactoring bugfix phase (Iteration 2).
Your working directory is: c:\Users\PC\Projects\cis\.agents\worker_refactor_bugfix

Please implement the following fixes to resolve the section uniqueness validation issues:

1. ENTITY METADATA FIX:
   - Edit `src/main/java/com/school/sis/setup/entity/Section.java`:
     Add unique constraints metadata to the `@Table` annotation to ensure H2/Hibernate auto-DDL generates the constraint properly in tests:
     ```java
     @Table(name = "sections", uniqueConstraints = {
         @UniqueConstraint(name = "sections_unique_term", columnNames = {"section_code", "school_year_id", "semester_id"})
     })
     ```
     Ensure `jakarta.persistence.UniqueConstraint` is imported.

2. REPOSITORY QUERY METHODS:
   - Edit `src/main/java/com/school/sis/setup/repository/SectionRepository.java`:
     Add the following query methods to the interface:
     - `boolean existsBySectionCodeAndSchoolYearIdAndSemesterId(String sectionCode, java.util.UUID schoolYearId, java.util.UUID semesterId);`
     - `boolean existsBySectionCodeAndSchoolYearIdAndSemesterIdAndIdNot(String sectionCode, java.util.UUID schoolYearId, java.util.UUID semesterId, java.util.UUID id);`

3. SERVICE-LEVEL VALIDATION:
   - Edit `src/main/java/com/school/sis/setup/service/SectionService.java`:
     In `create(SectionRequest request)`, add validation check:
     ```java
     if (sectionRepository.existsBySectionCodeAndSchoolYearIdAndSemesterId(request.sectionCode(), request.schoolYearId(), request.semesterId())) {
         throw new BusinessRuleException("Section code already exists in this term");
     }
     ```
     In `update(UUID id, SectionRequest request)`, add validation check:
     ```java
     if (sectionRepository.existsBySectionCodeAndSchoolYearIdAndSemesterIdAndIdNot(request.sectionCode(), request.schoolYearId(), request.semesterId(), id)) {
         throw new BusinessRuleException("Section code already exists in this term");
     }
     ```

4. INTEGRATION TEST UPDATES:
   - Edit `src/test/java/com/school/sis/setup/SectionDuplicateCodeTests.java`:
     Change the assert expectation in the `rejectsDuplicateSectionCodeInSameTerm` test (line 119) from `DataIntegrityViolationException.class` to `BusinessRuleException.class` (since the service now throws a custom business rule validation exception instead of triggering a raw SQL constraint violation).

5. BUILD & TEST VERIFICATION:
   - Run `mvn clean package -DskipTests` to verify backend compiles cleanly.
   - Run `mvn test` to verify all backend unit and integration tests pass successfully (especially `SectionDuplicateCodeTests` and `EnrollmentServiceTests`).
   - Run `npm run tsc` and `npm run build` in the `frontend` directory to ensure frontend build integrity remains intact.

Write a detailed summary of all modified files and verified build outputs in your handoff report.

MANDATORY INTEGRITY WARNING:
DO NOT CHEAT. All implementations must be genuine. DO NOT
hardcode test results, create dummy/facade implementations, or
circumvent the intended task. A Forensic Auditor will independently
verify your work. Integrity violations WILL be detected and your
work WILL be rejected.
