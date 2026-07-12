# Challenge Report - Enrollment Refactoring Verification

**Overall risk assessment**: MEDIUM

## Challenge Summary

The refactored code successfully compiles on both the backend and frontend. The section status active check behaves correctly and is validated by passing test suites. However, the verification of the duplicate section code constraint in the same term revealed a critical bug: the service layer lacks explicit validation for duplicate section codes in the same term, and the test verifying this constraint fails. This failure is due to Hibernate's deferred execution model within a transactional test, which prevents database-level UNIQUE constraint violations from being thrown before the test assertion. In production, this would manifest as a `500 Internal Server Error` instead of a validation error (`400 Bad Request`).

---

## Challenges

### [Medium] Challenge 1: Lack of Service-Level Unique Constraint Validation for Sections and Deferred DB Constraint Checks

- **Assumption challenged**: The assumption that a duplicate section code cannot be created in the same term, and that this constraint is properly verified by the test suite.
- **Attack scenario**: A user makes an API request to create a section with a code that already exists in the same school year and semester. Because `SectionService.create()` lacks service-level verification and does not flush the session, the database UNIQUE constraint is not evaluated immediately. The transaction fails on commit, throwing a database `DataIntegrityViolationException` resulting in a `500 Internal Server Error` response rather than a clean validation failure. In addition, the test case `SectionDuplicateCodeTests.rejectsDuplicateSectionCodeInSameTerm` fails because it asserts the service call throws a `DataIntegrityViolationException`, but no exception is raised during the method's execution in the transactional test context.
- **Blast radius**: Test suite failure (blocking CI/CD builds). API returns a 500 error instead of a client-friendly 400 validation warning when duplicates are submitted.
- **Mitigation**: 
  1. Add a validation check in `SectionService.create()` and `SectionService.update()` using a query like `sectionRepository.existsBySectionCodeAndSchoolYearAndSemester(...)` and throw a `BusinessRuleException("Section code already exists in this term")` if a duplicate is found.
  2. Alternatively (or additionally), call `sectionRepository.saveAndFlush(...)` or `sectionRepository.flush()` inside the service or test to force database constraint evaluation.

---

## Stress Test Results

### 1. Backend Compilation
- **Scenario**: Run `mvn clean test-compile` to verify compilation.
- **Expected behavior**: Build Success.
- **Actual behavior**: Build Success.
- **Status**: PASS

### 2. Inactive Section Enrollment Check
- **Scenario**: Validate that an inactive section is blocked during enrollment creation.
- **Expected behavior**: `EnrollmentServiceTests.rejectsInactiveSection` throws a `BusinessRuleException` with message "Selected section is inactive".
- **Actual behavior**: The test executes successfully and verifies that `BusinessRuleException` is thrown with the exact message.
- **Status**: PASS

### 3. Duplicate Section Code in Same Term Check
- **Scenario**: Attempt to create two sections with identical section codes, school years, and semesters.
- **Expected behavior**: The operation is rejected, throwing an exception.
- **Actual behavior**: The service execution `sectionService.create(request2)` succeeds without throwing an exception because the transaction is deferred and never flushed inside the test method. The test `SectionDuplicateCodeTests.rejectsDuplicateSectionCodeInSameTerm` fails.
- **Status**: FAIL

### 4. Frontend Compilation
- **Scenario**: Run `npm run tsc` and `npm run build` in the `frontend/` directory.
- **Expected behavior**: Clean TypeScript check and production build output.
- **Actual behavior**: Both commands succeeded without any errors.
- **Status**: PASS

---

## Unchallenged Areas

- **E2E Playwright integration** — Playwright E2E tests are out of scope for this backend/frontend compilation and unit level validation run.
- **Redis caching functionality** — Redis cache layer is defined but not actively utilized or checked for other modules under testing.
