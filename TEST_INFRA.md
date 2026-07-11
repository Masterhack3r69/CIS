# E2E Test Infra: Academic Setup Frontend

## Test Philosophy
- Opaque-box, requirement-driven. No dependency on implementation design.
- Methodology: Category-Partition + BVA + Pairwise + Workload Testing.

## Feature Inventory
| # | Feature | Source (requirement) | Tier 1 | Tier 2 | Tier 3 |
|---|---------|---------------------|:------:|:------:|:------:|
| 1 | Departments | ORIGINAL_REQUEST §R2 | 5 | 5 | ✓ |
| 2 | Programs | ORIGINAL_REQUEST §R2 | 5 | 5 | ✓ |
| 3 | Courses | ORIGINAL_REQUEST §R2 | 5 | 5 | ✓ |
| 4 | Faculty | ORIGINAL_REQUEST §R2 | 5 | 5 | ✓ |
| 5 | Rooms | ORIGINAL_REQUEST §R2 | 5 | 5 | ✓ |
| 6 | School Years | ORIGINAL_REQUEST §R2 | 5 | 5 | ✓ |
| 7 | Semesters | ORIGINAL_REQUEST §R2 | 5 | 5 | ✓ |
| 8 | Sections | ORIGINAL_REQUEST §R2 | 5 | 5 | ✓ |

## Test Architecture
- **Test Runner**: Playwright (v1.49+ or latest), configured to run tests in parallel, capture traces, screenshots, and videos on failure.
- **Vite Dev Server Integration**: Configured via the `webServer` block in `playwright.config.ts` to spin up `npm run dev` (on port 5173) and verify health before starting tests.
- **Directory Layout**:
  - `frontend/e2e/specs/`: E2E test files containing the 93 test cases.
  - `frontend/e2e/helpers/`: Shared auth and setup helpers.
  - `frontend/playwright.config.ts`: Configuration file.
  - `frontend/tsconfig.e2e.json`: Isolated TypeScript config for E2E tests.

## Real-World Application Scenarios (Tier 4)
| # | Scenario | Features Exercised | Complexity |
|---|----------|--------------------|------------|
| 1 | New Academic Term Setup Flow | School Years, Semesters, Departments, Programs, Courses, Sections | High |
| 2 | Faculty Onboarding and Assignment | Departments, Faculty, Sections | Medium |
| 3 | Curriculum/Facilities Expansion | Rooms, Courses, Sections | Medium |
| 4 | Comprehensive Validation Recovery | Departments (Forms, Validations, Server Errors) | Medium |
| 5 | Search, Filter and Bulk Pagination | Courses (Search, Filter, Pagination) | Medium |

## Coverage Thresholds
- Tier 1: ≥5 per feature (Total: 40 cases)
- Tier 2: ≥5 per feature (where boundaries exist) (Total: 40 cases)
- Tier 3: pairwise coverage of major feature interactions (Total: 8 cases)
- Tier 4: ≥5 realistic application scenarios (Total: 5 cases)
- **Total Minimum Cases**: 93
