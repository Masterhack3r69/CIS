# Plan: Enrollment Workspace and Schedules Integration

## Architecture
The enrollment workspace involves:
- Backend: `EnrollmentService.java`, `EnrollmentController.java`, and other entities/repositories.
- Frontend: `frontend/src/pages/enrollment-page.tsx`, `frontend/src/lib/api.ts`, `frontend/src/lib/types.ts`.
- Database: migrations for tracking cancellation status history if needed, or using existing columns.

## Milestones

| # | Name | Scope | Dependencies | Status |
|---|------|-------|--------------|--------|
| 1 | Exploration & Analysis | Analyze current backend/frontend enrollment and schedule mechanisms, list gaps against requirements. | None | PLANNED |
| 2 | Backend Implementation | Implement auto-population, section validations (regular vs irregular), cancellation details/reasons, and validation logic. | M1 | PLANNED |
| 3 | Frontend Integration | Update enrollment workspace forms, tables, filters, cancellation modals, and record tabs. | M2 | PLANNED |
| 4 | Verification & Testing | Build backend, build frontend, compile TypeScript, run unit/integration tests and E2E tests. | M3 | PLANNED |
| 5 | Forensic Audit | Perform security/integrity audit to ensure compliance and authenticity. | M4 | PLANNED |

## Interface Contracts

### 1. Enrollment API Updates
- **`POST /api/v1/enrollments`**: Creates a draft enrollment.
  - Payload: `{ studentId, schoolYearId, semesterId, yearLevel, sectionId, remarks }`
  - Behavior: If student is REGULAR and section is selected, auto-populate all active, curriculum-eligible schedules of that section in the same transaction.
- **`POST /api/v1/enrollments/{id}/cancel`**: Cancels enrollment.
  - Payload: `{ reason: string }`
  - Behavior: Updates status to CANCELLED, records status history with reason, prevents further edits.
- **`GET /api/v1/schedules`**: Fetches active schedules.
  - Parameters: `schoolYearId`, `semesterId`, `programId`, `curriculumId`, `yearLevel`, `sectionId`, `status=ACTIVE`

### 2. Validation Checks
- **Regular Student**:
  - Must not mix sections (all schedules must belong to the selected section).
  - Must have all required curriculum courses' schedules.
  - Confirmation blocked if any schedule is missing, full, inactive, or unavailable.
- **Irregular / Cross-Enrolled**:
  - May select schedules across active sections.
  - Must not have a null section (resolved section must be provided or "mixed").
  - Restricted by program, curriculum, year level, term.
