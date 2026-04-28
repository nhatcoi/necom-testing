# AGENTS.md - Necom Testing Playbook

## 1) Document Purpose

This document defines how AI agents should work consistently when building and maintaining the testing system for the entire Necom platform:

- Not only writing tests, but also standardizing test operations.
- Prioritizing reliability and scalability.
- Staying aligned with the team's real CI/CD workflow.

---

## 2) Repository Map

- `necom/necom-server`
  - Spring Boot backend.
  - White-box tests: unit, integration, and internal SIT when needed.
- `necom/necom-client`
  - React frontend.
  - White-box tests: component/unit/hook tests.
- `necom-testing`
  - Black-box workspace: API SIT + UI E2E (Cypress).
  - Must not depend on internal backend/frontend classes.
- `necom-test-platform` (optional)
  - Advanced testing platform (for example: Playwright, orchestration, reporting portal).

---

## 3) Test Architecture Principles (Mandatory)

1. Keep two layers clearly separated:
   - White-box in application repositories.
   - Black-box in a dedicated testing workspace.
2. Every critical business flow must have at least one SIT/API or E2E coverage path.
3. Test data must be deterministic and repeatable regardless of execution order.
4. Avoid long and ambiguous tests; prefer short, readable, business-meaningful scenarios.
5. Any business-impacting change must include evidence of test execution.

---

## 4) Naming Conventions and Structure

### Backend (`necom-server`)

- `*Test.java` -> Unit test (Surefire)
- `*IT.java` -> Integration test (Failsafe)
- `*SIT.java` -> System-level integration test (Failsafe)

Recommended directories:

- `src/test/java/com/necom/service/...`
- `src/test/java/com/necom/repository/...`
- `src/test/java/com/necom/controller/...`

### Frontend (`necom-client`)

- `*.test.ts`
- `*.test.tsx`

### Black-box (`necom-testing`)

- API SIT: `api-sit/**/*.test.ts`
- UI E2E: `ui-e2e/**/*.cy.ts`

---

## 5) Priority Business Flows (Must-have Coverage)

1. Auth: signup -> verification -> login.
2. Cart/Checkout: add to cart -> apply promotion -> create order.
3. Order lifecycle: create order -> update status -> cancel/refund.
4. Notification: trigger and display user-facing notifications.
5. Payment flow (if payment gateway exists): success/fail/cancel paths.

---

## 6) Standard Agent Workflow

1. Identify change scope (backend/frontend/black-box).
2. Add or update directly related tests first.
3. Run minimum tests for the scope:
   - code-level tests
   - one SIT/E2E flow if the change is business-critical
4. Check linter and test outputs.
5. Report clearly:
   - changed files
   - executed tests
   - tests not run and reasons

---

## 7) Standard Commands

### Backend

```bash
cd necom/necom-server
mvn test
mvn failsafe:integration-test failsafe:verify
```

### Frontend

```bash
cd necom/necom-client
CI=true npm test -- --watchAll=false
```

### Black-box

```bash
cd necom-testing
npm run test:api
npm run test:ui
npm run ui
```

---

## 8) CI/CD Quality Gates

### PR Gate (Mandatory)

- Backend unit + integration tests pass.
- Frontend unit/component tests pass.
- API SIT smoke tests pass.

### Nightly/Release Gate

- Full API SIT passes.
- Full UI E2E passes.
- Artifacts exported: JUnit, coverage, screenshots/videos (on failure).

---

## 9) Test Data Governance

1. Do not use real production data for automation.
2. Keep a clear `.env.example` for every testing workspace.
3. Version datasets and include metadata (owner, environment, last update).
4. Mask sensitive data.
5. Avoid hard dependency on unstable shared-environment live data.

---

## 10) Definition of Done

A change is considered complete only when:

- Relevant tests for the scope pass.
- Coverage exists for new/fixed behavior.
- No critical flaky-test increase is introduced.
- CI artifacts/reports are traceable.
- Local rerun instructions are provided if needed.

---

## 11) Anti-patterns to Avoid

- Relying only on manual testing for critical flows.
- Writing overly long E2E tests with unclear assertions.
- Mixing white-box and black-box concerns in the same test suite.
- Ignoring flaky failures without root-cause notes.
- Reporting "pass on local" without command/output evidence.

---

## 12) Common Troubleshooting

### Cypress UI white screen on macOS

- Use `npm run ui` (the script includes Electron hardening).
- Verify Cypress version (prefer 14+).
- Clear Cypress cache if needed.
- If still failing, run from internal disk (not `/Volumes/...`).

### SIT failures caused by environment issues

- Verify environment URL, backend health endpoint, and seed data.
- Separate test-logic failures from infrastructure failures.
- Use controlled rerun policy; do not rerun indefinitely.

---

## 13) Ownership (4-person model)

- Test Architect: strategy, quality gates, reporting.
- Backend QA Engineer: backend unit/integration coverage.
- Frontend QA Engineer: frontend component/unit coverage.
- E2E/SIT Engineer: black-box flows and environment stability.

---

## 14) Required Output Format for Agents

When a task is completed, the agent response must include at least:

1. What was done (short and clear).
2. Files/paths changed.
3. Test commands executed and their outcomes.
4. Remaining risks and recommended next steps.
