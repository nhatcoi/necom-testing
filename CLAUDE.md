# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repo is

This repo is **Necom Testing** — a dedicated quality / test-management system for the Necom e-commerce platform. It contains both the **system under test** (the Necom app, vendored under `necom/`) and the **testing workspaces** that exercise it. The end-state product is a test platform whose UI screens (Dashboard, Test Cases, Test Suites, Test Runs, Test Plans, Environments, Test Data, plus API/UI/Performance Tests, Coverage, Flaky Tests, Analytics) are mocked up in `ui/*.png` and will be implemented as a separate front-end + back-end alongside the test code itself.

Two strategic documents drive everything in this repo. **Read them before doing non-trivial work:**
- `PLAN.md` — five-layer test pyramid (Unit → Integration → Contract → SIT → UI E2E), recommended stack, quality gates, KPIs, flaky-test policy, and a four-week roadmap (Foundation → Unit/Integration → SIT → E2E + Quality Gates).
- `AGENTS.md` — agent playbook: white-box vs black-box separation, naming conventions, priority business flows, CI gates, anti-patterns, four-person ownership model, required output format.

If `PLAN.md` and `AGENTS.md` disagree with anything below, prefer them.

## Repository Layout

- `necom/necom-server/` — Spring Boot 2.6.7 / Java 11 / MySQL 8 (port 8085). JWT, MapStruct, RSQL, WebSocket, FreeMarker mail. **White-box tests live in-repo** under `src/test/java/com/necom/{controller,service,repository}/...` (currently has `OrderServiceTest`, `PromotionServiceTest`, `PaymentMethodRepositoryIT`, `ClientPaymentMethodControllerIT`, etc.).
- `necom/necom-client/` — React 17 + TS (CRA / `react-scripts` 5), Mantine v4, Zustand, react-query, react-router 6. White-box component/hook/util tests live next to source as `*.test.ts(x)`.
- `necom/docker-compose.yml` — local stack (`necom-database`, `necom-server`, `necom-client` on `:3000`).
- `necom-testing/` — *intended* black-box workspace for `api-sit/**/*.test.ts` + `ui-e2e/**/*.cy.ts` (Cypress) per `AGENTS.md`. **Currently empty in this repo**; the CI job `blackbox-sit-e2e` already expects it. `PLAN.md` recommends restructuring into `api-sit/` (REST Assured or Karate) + `ui-e2e/` (Playwright, not Cypress) + `performance/k6/`. Reconcile with the user before picking a stack.
- `ui/` — PNG mockups of the test-management product (Dashboard, Test Cases, Test Suites, Test Runs, Test Plans, Environments, Test Data). Treat these as the spec for the future test-platform front-end.
- `agent-docs/` — placeholder directory (currently empty).
- `cursor_x_y_d_ng_h_th_ng_ki_m_th_cho_nec.md` — original Vietnamese planning notes; superseded by `PLAN.md` + `AGENTS.md`.
- `.github/workflows/testing.yml` — CI with three jobs: `backend-unit-integration` (Maven), `frontend-rtl` (CRA), `blackbox-sit-e2e` (boots full stack via docker-compose, waits on `/actuator/health`, runs `npm run test:api` then `npm run test:ui`).

## The five layers (per PLAN.md)

| Layer | Where | Tooling already wired | Tooling planned |
|---|---|---|---|
| 1. Unit | `necom-server/src/test/.../*Test.java`, `necom-client/src/**/*.test.ts(x)` | JUnit 5, Mockito, Spring Boot Test, RTL + Jest | AssertJ, Instancio/EasyRandom, Vitest+MSW (frontend migration) |
| 2. Integration | `necom-server/src/test/.../*IT.java` | `@DataJpaTest`, Spring Security Test, H2 + Testcontainers MySQL deps on classpath | Shared `BaseIntegrationTest` w/ reusable Testcontainers |
| 2.5. Contract | not yet started | — | Pact or OpenAPI schema validation between client ↔ server |
| 3. SIT (API black-box) | `necom-testing/api-sit/` (empty) | — | REST Assured + JUnit 5, or Karate DSL; full stack via `docker-compose.test.yml` |
| 4. UI E2E | `necom-testing/ui-e2e/` (empty) | — | Playwright (PLAN.md preference) or Cypress (AGENTS.md current wording); Page Object Model required |
| 5. CI/CD + reporting | `.github/workflows/testing.yml` | JaCoCo report, JUnit/coverage artifacts | Allure, flaky-test dashboard, performance smoke (k6) |

## Common Commands

### Backend (`necom/necom-server`)
```bash
mvn test                                              # Unit only (Surefire: *Test.java)
mvn -Dtest=OrderServiceTest test                      # Single unit class
mvn failsafe:integration-test failsafe:verify         # Integration + system (*IT.java, *SIT.java)
mvn -Dit.test=PaymentMethodRepositoryIT failsafe:integration-test failsafe:verify
mvn jacoco:report                                     # Coverage → target/site/jacoco
mvn clean package -DskipTests -Pci                    # CI build profile
mvn generate-resources -Pgen-address-sql              # Regenerate address.sql from address/*.sql
```

### Frontend (`necom/necom-client`)
```bash
npm start                                             # Dev server
npm run build
CI=true npm test -- --watchAll=false                  # One-shot
CI=true npm test -- --watchAll=false --coverage       # With coverage (matches CI)
CI=true npm test -- --watchAll=false src/utils/NotifyUtils.test.tsx
```

### Local stack
```bash
cd necom && docker compose up -d                              # Full stack
cd necom && docker compose up -d necom-database necom-server  # Backend only
curl -fsS http://localhost:8085/actuator/health               # CI gates SIT/E2E on this
```

### Black-box (when `necom-testing/` is populated)
```bash
cd necom-testing
npm run test:api      # API SIT
npm run test:ui       # UI E2E (Cypress headless)
npm run ui            # Cypress with macOS Electron hardening (per AGENTS.md)
```

## Backend Architecture (system under test)

- **Test split is convention-driven, not directory-driven.** `pom.xml` configures Surefire to match `**/*Test.java` and Failsafe to match `**/*IT.java` + `**/*SIT.java`, with mutual excludes. Naming a file `FooTests.java` (plural) silently runs in **neither** phase. CI further filters with `-Dtest="*Test"` and `-Dit.test="*IT"`.
- **Schema is not Hibernate-managed.** `spring.jpa.hibernate.ddl-auto=none`; Spring SQL init runs `address.sql` then `schema.sql` from `src/main/resources` on startup (`continue-on-error: true`). For repository tests, `@DataJpaTest` works against the H2 + Testcontainers MySQL deps already on the test classpath.
- **Address dataset is generated.** `src/main/resources/address.sql` is built from per-province files in `src/main/resources/address/` via `scripts/build_address_sql.py` (also wired into the `gen-address-sql` Maven profile). Edit the source files, regenerate — never hand-edit `address.sql`.
- **Domain modules** (mirrored across `entity/`, `service/`, `controller/`, `repository/`): `address`, `authentication`, `cart`, `cashbook`, `chat`, `client`, `customer`, `employee`, `general`, `inventory`, `order`, `product`, `promotion`, `review`, `reward`, `waybill`. Controllers split admin (`controller/<module>`) vs storefront (`controller/client`).
- **Generic CRUD layer.** `GenericController`, `GenericService`, `GenericMappingRegister`, and `FunctionalEndpointRegister` provide reflection/RSQL-driven CRUD for many entities — when adding a new admin entity, prefer extending these. Dynamic filtering via `rsql-jpa-spring-boot-starter`. DTO ↔ entity translation is MapStruct. Tests should still cover the concrete service logic, not the generic plumbing.
- **API surface split by URL prefix.** Nginx proxies `/api`, `/client-api`, and `/ws` to the backend; the client uses these prefixes (admin / storefront / WebSocket). New endpoints must land under the correct prefix and be reflected in `nginx.conf` if they break the pattern.

## Frontend Notes

- Tests use Jest + React Testing Library (CRA defaults); `setupTests.ts` only loads `@testing-library/jest-dom`. Mock Mantine globals per-test (see `src/utils/NotifyUtils.test.tsx` — `jest.mock('@mantine/notifications', …)`).
- Runtime API base comes from `REACT_APP_API_ORIGIN` / `REACT_APP_WS_ORIGIN` (`.env`). The Docker build leaves these empty so nginx handles routing.
- PLAN.md recommends migrating frontend tests to Vitest + MSW; nothing has been migrated yet — coordinate before starting.

## Priority business flows (must have coverage at SIT/E2E level)

From `AGENTS.md` §5 + `PLAN.md` §4:

1. Auth: signup → verification → login (+ refresh token, RBAC).
2. Cart / Checkout: add to cart → apply promotion → create order.
3. Order lifecycle: create → status update → cancel / refund / tracking.
4. Notification: trigger and surface user-facing notifications (often async — use `await().untilAsserted(...)`).
5. Payment: success / fail / cancel paths, with idempotency under webhook retry and F5.
6. E-commerce edge cases worth designing for: inventory race conditions, voucher stacking, cart guest→logged-in handoff, shipping calculation by region/weight/carrier.

## Quality Gates (from PLAN.md §6)

| Metric | Threshold | Action on fail |
|---|---|---|
| Unit pass rate | ≥ 95% | Block merge |
| Backend coverage (core) | ≥ 70% | Warning |
| Backend coverage (new code) | ≥ 80% | Block merge |
| Critical-flow SIT | 100% | Block release |
| E2E smoke | 100% pass | Block release-merge |
| PR feedback time | < 15 min | Alert DevOps |
| Flaky rate | < 2% | Quarantine immediately |

Flaky policy: auto-retry max 2× on CI (never local), quarantine after 3 fails/week, SLA to fix = 1 week or delete/rewrite.

## Test-platform UI (the product surface in `ui/*.png`)

The screenshots define the future test-management application's information architecture. Use them as the spec when implementing or discussing the platform UI:

- **Dashboard** — KPI tiles (Total Test Cases, Execution Test Runs, Pass Rate, Active Bugs, Automation Rate), Test Execution Trend, Test Results Distribution, Recent Activity, Recent Test Runs, Test Coverage (Code), Flaky Tests, Automation Rate, Test Velocity, Environment Status.
- **Test Cases** — hierarchical Test Suites tree (Authentication, Product Catalog, Shopping Cart, Checkout & Payment, Order Management, User Management, Notifications…) with case table (ID, Title, Test Type, Level, Priority, Status).
- **Test Suites** — suite list with counts, type, owner; right-pane suite detail (description, owner, tags, recent runs).
- **Test Runs / Test Plans** — execution history, release-scoped plans (e.g. "Release 1.2.0 – Full Regression"), per-run progress bars and assignees.
- **Environments** — Production / Staging / QA / Local with status, URL, owner, last-deploy.
- **Test Data** — versioned datasets (User Accounts, Products Catalog, Payment Cards, Order Templates) with row counts and last-updated.
- Side-nav also lists: API Tests, UI Tests, Performance Tests, Schedules, Test Reports, Coverage, Flaky Tests, Analytics, Projects, Integrations, Settings.

This product does not exist in code yet — when implementing, treat the screenshots as the source of truth for layout/labels and reconcile data shapes with the layered test execution backend.

## Operational gotchas

- **Cypress on macOS / `/Volumes/...`**: this checkout lives on `/Volumes/NVME_NHAT/...`. AGENTS.md flags this path as a known cause of Cypress white-screen issues — the documented workaround is running E2E from the internal disk, or using `npm run ui` (which bundles Electron hardening).
- **Stack mismatch between PLAN.md and reality**: PLAN.md examples assume PostgreSQL/Redis Testcontainers, but Necom currently runs **MySQL 8** with no Redis. Use `MySQLContainer` (the dep is already in `pom.xml`); don't blindly copy the `PostgreSQLContainer` snippet.
- **No Hibernate DDL** — any new entity needs a corresponding `schema.sql` change, otherwise the app will boot but fail at first query.
- **Don't mix layers**: white-box tests must not live in `necom-testing/`, and black-box tests must not import classes from `necom/necom-server` or `necom/necom-client`. This is the load-bearing rule of the whole strategy.
