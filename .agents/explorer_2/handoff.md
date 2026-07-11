# Handoff Report: E2E Testing Framework Investigation

## 1. Observation
* **Frontend configuration** (`c:\Users\PC\Projects\cis\frontend\package.json`):
  * React: `"react": "^19.2.6"`, `"react-dom": "^19.2.6"` (lines 25-26)
  * Vite: `"vite": "^8"` (line 53)
  * TypeScript: `"typescript": "~6"` (line 51)
  * Vitest: `"vitest": "^4.1.10"` (line 54)
  * No existing E2E testing dependencies (Cypress or Playwright are not present).
  * No test run scripts are configured under the `"scripts"` object (lines 6-13).
* **Package manager check**:
  * Lockfiles: `package-lock.json` is present at `c:\Users\PC\Projects\cis\frontend\package-lock.json`. No `yarn.lock` or `pnpm-lock.yaml` exist.
  * System tools: Running `npm --version` returns `11.5.2`, `pnpm --version` returns `10.22.0`, while `yarn` is not recognized.
* **Test file search**:
  * Zero test or spec files (`*test*` or `*spec*`) exist under `c:\Users\PC\Projects\cis\frontend\src`.
  * The only test files found are backend Java tests in `src/test/java` and configuration in `src/main/resources/application-test.yml`.
* **Workspace layout**:
  * Frontend port settings in `c:\Users\PC\Projects\cis\docker-compose.yml` list `http://localhost:5173` in `CORS_ALLOWED_ORIGINS` (line 38) and maps the production build container to host port `3000` (line 48).

## 2. Logic Chain
1. **Package Manager Choice**: The presence of `package-lock.json` coupled with the global availability of npm `11.5.2` and lack of yarn indicates `npm` is the designated package manager for the frontend workspace.
2. **E2E Framework Selection**:
   * The frontend uses extremely modern versions of React (v19) and Vite (v8).
   * Playwright compiles TypeScript natively, runs browser contexts in parallel across multiple worker processes, supports multi-tab and iframe actions cleanly, and integrates seamlessly with Vite 8 without complex bundler preprocessors.
   * Cypress has a heavier Electron footprint, requires extra loaders/preprocessors for ESM/Vite 8 compiling, executes tests in a single browser tab, and requires paid orchestration for parallel runs.
   * Therefore, Playwright is the optimal and modern fit for the E2E testing layer.
3. **Infrastructure Design**:
   * Creating `frontend/playwright.config.ts` allows us to define the E2E directory `./e2e/specs` and configure automated starting/teardown of the Vite dev server (via `webServer` block pointing to `npm run dev` on port 5173).
   * Adding a dedicated `frontend/tsconfig.e2e.json` and registering it in `tsconfig.json` references ensures E2E test files receive editor type assistance without polluting the main React compiler configuration.

## 3. Caveats
* **Port Conflict**: The Playwright `webServer` is configured to look for the dev server on port `5173`. If port `5173` is busy on the execution host, Playwright will wait or fail. The URL/port must be updated if Vite binds to a different port.
* **Execution Environment**: We did not install the package or run actual tests since this is a read-only investigation task.

## 4. Conclusion
* **Optimal Framework**: **Playwright** is recommended.
* **Package Manager**: **npm** is the active workspace package manager.
* **Proposed Infrastructure**: Place E2E tests under `frontend/e2e/`, use `playwright.config.ts` for browser setup and Vite dev server automation, and configure `tsconfig.e2e.json` for compilation separation.

## 5. Verification Method
* **Configuration Validity**: Inspect the proposed configuration files in the report: `c:\Users\PC\Projects\cis\.agents\explorer_2\e2e_testing_investigation_report.md`.
* **Lockfile**: Verify `package-lock.json` is the sole lockfile in `frontend/`.
* **Execution check (to be done by the implementer)**: 
  * Run `npm install -D @playwright/test` and `npx playwright install` in `frontend/`.
  * Run `npm run test:e2e` to verify the test runner executes and spins up the dev server.
