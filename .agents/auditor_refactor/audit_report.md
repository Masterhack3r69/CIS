## Forensic Audit Report

**Work Product**: Refactored Student Profiling, Sections, Scheduling, and Enrollment Integration (backend & frontend)
**Profile**: General Project (Development Mode)
**Verdict**: CLEAN

### Phase Results
- **Check 1: Hardcoded test results detection**: PASS — Looked for hardcoded expectations or verification strings in services and controllers. The backend and frontend logic evaluates data dynamically, with no hardcoded test shortcuts.
- **Check 2: Facade detection**: PASS — Inspected `EnrollmentService.java`, `SectionService.java`, `ReportService.java`, and `ScheduleService.java`. Interfaces are fully integrated with the database repositories and include genuine business logic (such as active section checking, year-level propagation, curriculum matching, and conflict validation).
- **Check 3: Pre-populated artifact detection**: PASS — Checked for pre-populated logs or result files in the workspace. None were found.
- **Check 4: Build and run verification**: PASS/FAIL (Functional regression found) — Frontend builds cleanly with zero TypeScript errors (`npm run tsc` and `npm run build` both succeed). Backend compilation succeeds, but 1 test fails due to transactional database flush behavior.
- **Check 5: Dependency audit**: PASS — Third-party libraries are used only for auxiliary tasks (e.g., standard Spring Web, JPA, H2, Lombok, Jackson) with no execution delegation violations.

### Findings & Functional Issues
Although the verdict is **CLEAN** from an code integrity perspective (no cheating, dummy, or facade patterns), the following test failure was observed during behavioural verification:
1. **Failing Test**: `SectionDuplicateCodeTests.rejectsDuplicateSectionCodeInSameTerm`
   - **Reason**: The test asserts that a `DataIntegrityViolationException` is thrown when creating duplicate sections in the same term. However, inside a `@Transactional` test method, Hibernate defers database INSERT execution (and thus the UNIQUE constraint check) until the transaction commits. Because `SectionService.create()` only calls `save()` without flushing, the constraint check is not evaluated during service execution, causing the test assertion to fail.
   - **Recommendation**: In `SectionService.java`, add a service-level verification check using `sectionRepository.existsBySectionCodeAndSchoolYearAndSemester(...)` and throw a `BusinessRuleException` when a duplicate is found, OR call `saveAndFlush()` or force `flush()` inside the test.

---

### Evidence

#### 1. Frontend Build Verification (tsc & build)
```
vite v8.1.4 building client environment for production...
transforming...✓ 2063 modules transformed.
rendering chunks...
computing gzip size...
dist/index.html                                                0.47 kB │ gzip:   0.31 kB
dist/assets/index-DaLkDWbm.css                               112.50 kB │ gzip:  18.36 kB
dist/assets/index-c2EMFkze.js                              3,044.55 kB │ gzip: 558.24 kB
✓ built in 8.95s
```

#### 2. Backend Test Output Failure
```
[ERROR] Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.432 s <<< FAILURE! -- in com.school.sis.setup.SectionDuplicateCodeTests
[ERROR] com.school.sis.setup.SectionDuplicateCodeTests.rejectsDuplicateSectionCodeInSameTerm -- Time elapsed: 0.171 s <<< FAILURE!
java.lang.AssertionError: 

Expecting code to raise a throwable.
	at com.school.sis.setup.SectionDuplicateCodeTests.rejectsDuplicateSectionCodeInSameTerm(SectionDuplicateCodeTests.java:108)
```

#### 3. SectionService Refactored Implementation (Diff Snippet)
```java
    @Transactional
    public SectionResponse create(SectionRequest request) {
        Section section = new Section();
        apply(section, request);
        SectionResponse response = toResponse(sectionRepository.save(section)); auditService.log("SECTION_CREATED", AuditModule.ACADEMIC_SETUP, "Section", response.id(), null, response); return response;
    }
```
