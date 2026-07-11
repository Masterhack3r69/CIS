# E2E Testing Framework Investigation and Setup Proposal

## 1. Executive Summary
This report analyzes the `cis` repository workspace to determine the optimal End-to-End (E2E) testing framework for the React-based frontend. Based on our analysis of the frontend stack (React v19, Vite v8, TypeScript v6, and Tailwind CSS v4), **Playwright** is recommended as the optimal testing framework. We propose a full E2E setup integrating Playwright seamlessly with the existing npm-based workspace, with dedicated configurations for TypeScript and Vite dev server automation.

---

## 2. Workspace Investigation Findings

### 2.1 Package Manager Alignment
* **Observation**: The `frontend/` directory contains a `package-lock.json` file, indicating **npm** is the primary package manager. 
* **Tool Check**: System-level commands confirm:
  * `npm --version`: `11.5.2` (Available)
  * `pnpm --version`: `10.22.0` (Available)
  * `yarn --version`: Not installed.
* **Recommendation**: We must stick to `npm` for all installations to prevent lockfile conflicts and remain consistent with the existing `package-lock.json` and Docker builds.

### 2.2 Existing Testing Tools & Configurations
* **Observation**: In `frontend/package.json`, unit/integration testing libraries are declared in `devDependencies`:
  * `"@testing-library/jest-dom": "^6.9.1"`
  * `"@testing-library/react": "^16.3.2"`
  * `"jsdom": "^29.1.1"`
  * `"vitest": "^4.1.10"`
* **Configuration State**:
  * No test runner configuration files exist (e.g., there is no `vitest.config.ts`).
  * No test scripts are defined in `package.json`.
  * No test files (matching `*.test.*` or `*.spec.*`) exist in the source tree.
  * **No E2E testing framework** (like Playwright, Cypress, or Selenium) is currently installed or configured.

---

## 3. Framework Selection Analysis

We evaluated **Playwright** vs **Cypress** for this project:

| Feature | Playwright | Cypress | Recommended Selection |
| :--- | :--- | :--- | :--- |
| **React 19 & Vite 8 Support** | Native, out-of-the-box support for ESM and modern bundling. | Requires extra preprocessors/bundler configurations for Vite ESM. | **Playwright** (zero configuration for Vite 8 / React 19) |
| **Performance & Speed** | Runs tests in parallel using multiple browser worker processes natively. | Runs tests inside a single browser instance; parallelization requires paid Cypress Cloud. | **Playwright** |
| **Handling Multi-Tab & Origins** | Native support for multiple browser contexts, tabs, and domains. | Historically weak, requires complex workarounds due to architecture constraints. | **Playwright** |
| **CI Integration** | Built-in lightweight CLI, trace viewer, and video recordings. | Heavier installation footprint (includes Electron), complex CI recording setup. | **Playwright** |
| **TypeScript Support** | Native compiler built-in (directly runs `.ts` files). | Requires configuring TypeScript bundler plugins/loaders. | **Playwright** |

**Conclusion**: Playwright is the optimal tool. It is lightweight, fast, modern, and perfectly aligned with Vite 8 + React 19 + TypeScript 6.

---

## 4. Proposed E2E Test Infrastructure

We recommend setting up Playwright directly in the `frontend/` workspace under a dedicated directory structure.

### 4.1 Folder Structure
We suggest the following layout inside `frontend/` to keep test code structured and maintainable:

```
frontend/
├── e2e/                             # Main directory for E2E tests
│   ├── specs/                       # All E2E test specifications
│   │   ├── auth.spec.ts             # Auth workflows (login, logout, token refresh)
│   │   └── academic-setup.spec.ts   # CRUD verification specs for the 8 modules
│   └── helpers/                     # Shared test helpers and utilities
│       ├── auth-helper.ts           # Storage state save/load helper for fast login
│       └── page-objects/            # Page Object Model (POM) page wrappers
│           ├── layout.page.ts       # Sidebar layout page object
│           └── departments.page.ts  # CRUD page object for departments
├── playwright.config.ts             # Playwright main configuration file
├── tsconfig.e2e.json                # TypeScript settings for E2E tests
└── package.json                     # Node config updated with test scripts
```

### 4.2 Playwright Configuration (`playwright.config.ts`)
Create this file in `frontend/playwright.config.ts`. It includes automated local server start-up via `webServer` so tests run reliably locally and in CI.

```typescript
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e/specs',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: 'html',
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },
    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] },
    },
  ],
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: !process.env.CI,
    timeout: 10 * 1000,
  },
});
```

### 4.3 TypeScript Configuration (`tsconfig.e2e.json`)
Create this file in `frontend/tsconfig.e2e.json` to configure E2E tests correctly without polluting the main React DOM project compiler config:

```json
{
  "extends": "./tsconfig.json",
  "compilerOptions": {
    "composite": true,
    "lib": ["ES2023", "DOM"],
    "types": ["node"]
  },
  "include": ["e2e/**/*", "playwright.config.ts"]
}
```

Then, update `frontend/tsconfig.json` to register the reference:
```json
  "references": [
    { "path": "./tsconfig.app.json" },
    { "path": "./tsconfig.node.json" },
    { "path": "./tsconfig.e2e.json" }
  ]
```

### 4.4 Run Scripts
Update the `scripts` section in `frontend/package.json` with:
```json
"scripts": {
  "test:e2e": "playwright test",
  "test:e2e:ui": "playwright test --ui",
  "test:e2e:debug": "playwright test --debug",
  "test:e2e:report": "playwright show-report"
}
```

---

## 5. Setup & Installation Commands

Execute the following commands in the `frontend/` directory to set up the infrastructure:

1. **Install Playwright package**:
   ```bash
   npm install -D @playwright/test
   ```

2. **Install browser binaries**:
   ```bash
   npx playwright install --with-deps
   ```

3. **Verify infrastructure installation** (with a placeholder smoke test):
   Create `frontend/e2e/specs/smoke.spec.ts`:
   ```typescript
   import { test, expect } from '@playwright/test';

   test('basic smoke test', async ({ page }) => {
     await page.goto('/');
     await expect(page).toHaveTitle(/CIS/i);
   });
   ```
   Run the test suite:
   ```bash
   npm run test:e2e
   ```
