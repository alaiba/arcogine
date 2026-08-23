# Testing Guide

This document covers all test categories in Arcogine, how to run them, and the rationale behind the testing architecture. **`./arcogine`** is the canonical entry point for cross-project quality gates from the repository root; anything more specific runs through its own native tool (`./gradlew`, `npm`/`npx`, `docker compose`, `trivy`, `gitleaks`) as noted per category below.

This is about testing and quality-verifying Arcogine's own software — a different concept from the [Product Charter](/docs/product/charter.md)'s "Verify" mode, which describes a future capability for verifying a *user's* production model or configuration against their own objectives and constraints. Don't conflate the two terminologically: this document is entirely about the former.

## Quick reference

```bash
./arcogine check         # fast gates (before pushing): compile, Checkstyle, tests, coverage, frontend lint/typecheck/test/build
./arcogine check --full  # everything: check + Playwright E2E + Docker smoke + security scans
./arcogine --help        # list all ./arcogine commands
```

## Quality gates

| Command | Scope |
|---------|-------|
| `./arcogine check` | Java compile (`-Xlint:all -Werror`), Checkstyle, JUnit tests, Jacoco coverage gates, frontend lint, typecheck, unit tests, frontend coverage, production build |
| `./arcogine check --full` | Everything above, plus Playwright, Docker build/smoke, and security scans (dependency audit, frontend audit, Trivy image scans, Gitleaks) |

### Command model

`./arcogine` stays deliberately small (`setup`, `test`, `check`/`check --full`, `run api`/`run web`) and composes each subsystem's native tool rather than wrapping every operation:

- **Java** (`cd product && ./gradlew <task>`): `compileJava`/`compileTestJava`, `checkstyleMain`/`checkstyleTest`, `test`, `jacocoTestReport`/`jacocoTestCoverageVerification`, `:cli:bootJar`, `:simulation:jmh`, `cyclonedxBom`
- **Frontend** (`cd product/interfaces/web && npm ...`/`npx ...`): `npm run lint`, `npx tsc --noEmit`, `npm test`/`npm run test:coverage`, `npm run build`, `npm audit --audit-level=high`
- **E2E** (`cd product/interfaces/web && npx playwright test`)
- **Containers** (`./arcogine build`/`image`/`up`/`down`, or `docker compose` directly against `infra/docker/compose.yaml`)
- **Security** (`trivy image`/`trivy sbom`, `gitleaks detect`)

## Prerequisites

- **Java 25 (LTS)** — Temurin, provided by the devcontainer (SDKMAN). The build uses the Gradle wrapper (`product/gradlew`, Gradle 9.1.0); no system Gradle needed.
- **Node.js** (24+): for frontend checks and tests.
- **Docker** and Docker Compose: for container checks (optional for local dev).

All Java commands run through the Gradle wrapper under `product/`.

### Running Java tests without a local JDK 25

If your machine doesn't have JDK 25 installed and Gradle's toolchain auto-provisioning isn't configured (no `settings.gradle.kts` foojay resolver), run the build inside a matching `gradle:9-jdk25` image, rather than skipping verification (note: `infra/docker/api.Dockerfile` is runtime-only and does not build Java — this is a standalone dev-workflow image, unrelated to it):

```bash
# One-off run (simplest; pays container/JVM startup cost each time):
docker run --rm -v "$(pwd)/product:/app" -v arcogine_gradle_cache:/root/.gradle -w /app \
  gradle:9-jdk25 sh -c "./gradlew test --no-daemon"
```

For repeated runs during active development, start a long-lived container once and `exec` into it — this lets the Gradle daemon (and JIT-warmed JVM) stay resident across invocations instead of restarting from cold every time:

```bash
# Start once (per machine/session):
docker run -d --name arcogine-build -v "$(pwd)/product:/app" -v arcogine_gradle_cache:/root/.gradle \
  -w /app gradle:9-jdk25 tail -f /dev/null

# Then, repeatedly:
docker exec arcogine-build ./gradlew test
docker exec arcogine-build ./gradlew :factory:test --tests "com.arcogine.factory.process.FactoryHandlerTest"

# When done:
docker stop arcogine-build && docker rm arcogine-build
```

Notes:

- The `arcogine_gradle_cache` named volume caches the downloaded Gradle distribution and dependencies across runs — without it, each fresh container re-downloads `gradle-9.1.0-bin.zip`. Neither the volume nor the `arcogine-build` container survive a move to a different machine; recreate them there with the commands above.
- If `product/**/build/` directories already exist on the host from an earlier run with a *different* JDK (e.g. a stray local JDK 21 run), Gradle may report tasks `UP-TO-DATE` from stale, filesystem-timestamp-based caching without actually re-executing them in the container — confirm real execution by checking for `PASSED`/`FAILED` lines per test, not just `BUILD SUCCESSFUL`. Delete those stale `build/` dirs once (`rm -rf product/**/build product/build`) rather than passing `--rerun-tasks` on every invocation.
- `product/gradle.properties` enables `org.gradle.parallel=true` and `org.gradle.caching=true` — these are committed to the repo and apply automatically regardless of how you invoke Gradle.

## Test categories

### 1. Java static analysis (Checkstyle)

`cd product && ./gradlew checkstyleMain checkstyleTest` (part of `./arcogine check`) — runs Checkstyle 13.5.0 against a deliberately minimal, high-signal ruleset (`product/config/checkstyle/checkstyle.xml`): unused/redundant/star imports plus a few bug-oriented checks. The compiler does **not** flag unused imports, so this is genuinely additive. Expand the ruleset deliberately rather than adopting a large style guide wholesale.

### 2. Java compilation

`cd product && ./gradlew compileJava compileTestJava` — compiles all modules' main and test sources with `-Xlint:all -Werror`, so every compiler warning (including deprecations from newer Spring/Jackson) is a hard error.

### 3. Java unit tests (JUnit 5)

~178 tests across the seven modules cover typed IDs and `SimTime`, scenario schema and TOML loading, the scheduler/runner/KPI/event-log core, machine state and job routing, demand and pricing, the sales agent, the HTTP API contract, and the headless CLI.

`cd product && ./gradlew test`.

### 4. Property tests

Invariants (monotonic time, no event loss, machine concurrency limits, queue FIFO ordering) are expressed as JUnit 5 parameterized/randomized-seed tests in `simulation` and `domains/factory`. They run as part of `./gradlew test`.

### 5. Integration tests

`interfaces/api` tests use `@SpringBootTest(webEnvironment = RANDOM_PORT)` and a `WebTestClient` built via `WebTestClient.bindToServer()` against the live server. They exercise the full HTTP contract (scenario load, run/pause/step, price/machine/agent commands, KPIs, topology, SSE), so they double as the API integration layer. Part of `./gradlew test`.

### 6. Determinism tests

`simulation` verifies that identical seeds produce identical event logs and KPIs. The rewrite uses `java.util.Random`/`SplittableRandom` (not Rust's ChaCha8), so determinism is asserted as **reproducibility** — two Java runs with the same seed are byte-identical — and any golden values are captured from Java runs, never copied from the Rust implementation.

### 7. Java coverage (Jacoco) + per-module gates

`cd product && ./gradlew test jacocoTestReport jacocoTestCoverageVerification` (part of `./arcogine check`). Each module declares a `jacocoTestCoverageVerification` gate (a `LINE` minimum, set a few points below measured actual) wired into `check`, so removing a module's tests fails the build instead of passing vacuously. CI uploads the per-module `jacocoTestReport.xml` to Codecov.

### 8. Benchmarks (JMH)

`cd product && ./gradlew :simulation:jmh` — runs the JMH microbenchmarks in `simulation`, ported from the Rust Criterion suites: scheduler throughput (schedule / dequeue / interleaved over 1000 events) and scenario runtime (run a 1000-tick scenario, and load+validate). Sources live in `product/simulation/src/jmh/java/com/arcogine/core/bench/`. Benchmarks are **on-demand** (not a CI gate). Note: ASM is pinned to a Java 25-aware version so the JMH bytecode generator can read the toolchain's class files, and JMH's machine-generated classes are exempt from `-Werror`.

### 9. Java dependency audit (CycloneDX SBOM + Trivy)

`cd product && ./gradlew cyclonedxBom && trivy sbom --severity CRITICAL,HIGH --ignore-unfixed --exit-code 1 product/build/reports/cyclonedx/bom.json` (part of `./arcogine check --full`) — generates a CycloneDX SBOM of the whole build (the `org.cyclonedx.bom` plugin → `product/build/reports/cyclonedx/bom.json`, ~179 components) and scans it with `trivy sbom` for fixable CRITICAL/HIGH CVEs. **This is a blocking gate** (`--exit-code 1`) and complements the Trivy image scan of the built API image (see below). (`trivy fs` is not used: it does not introspect a Spring Boot fat jar's nested `BOOT-INF/lib` jars without Trivy's separate Java DB.)

Shipped-runtime CVEs in `tomcat-embed-core` (3 CRITICAL + 3 HIGH) were remediated by overriding the Spring-managed version — `extra["tomcat.version"] = "11.0.22"` in `api`/`cli`. Non-shipped Netty CVEs (test-only, pulled by `spring-boot-starter-webflux`'s `WebTestClient`) are remediated the same way where practical — `extra["netty.version"] = "4.2.16.Final"` in `interfaces/api` — rather than suppressed, so the whole-build SBOM audit stays clean without relying on `.trivyignore`.

A `.trivyignore` at the repo root suppresses **only non-shipped** findings that aren't otherwise remediated by a version override, each justified inline: `netty-codec-*` (test-only — pulled by `spring-boot-starter-webflux`, the `WebTestClient` reactive client) and `plexus-utils` (build tooling). Shipped-runtime CVEs are never suppressed and will fail the gate.

(OWASP dependency-check was considered but requires an NVD API key and a large database download; Trivy reuses the vulnerability DB already present from the image scans.)

### 10–15. Frontend (lint, typecheck, unit tests, coverage, build, audit)

The React/TypeScript UI is unchanged by the Rust→Java rewrite. From `product/interfaces/web/`: `npm run lint` (ESLint), `npx tsc --noEmit` (typecheck), `npm test` (Vitest), `npm run test:coverage`, `npm run build`, `npm audit --audit-level=high` (dependency audit). Lint/typecheck/coverage/build run as part of `./arcogine check`; the audit runs as part of `./arcogine check --full`.

### 16. Playwright E2E

Browser-level user-journey tests. Requires the API server and UI dev server. `cd product/interfaces/web && npx playwright test` (part of `./arcogine check --full`) — runs the Playwright suite against the **Java** API unchanged. Playwright's own config (`product/interfaces/web/playwright.config.ts`) starts both servers via `webServer`, but the API jar (`cd product && ./gradlew :cli:bootJar`) must already be built once for a clean checkout.

### 17. Docker build and smoke

`./arcogine build` (produces `dist/`) then `./arcogine image` (builds runtime images from `dist/`), then the smoke sequence (start via `./arcogine up --no-rebuild`, health-check `:3000/api/health` and the web app, tear down via `./arcogine down`) — all part of `./arcogine check --full`. `infra/docker/api.Dockerfile` and `infra/docker/web.Dockerfile` are runtime-only: they package the already-built `dist/api/arcogine.jar` and `dist/web/` static site, never compile source.

### 18. Container image scans

`docker build -f infra/docker/api.Dockerfile -t arcogine-api:ci dist/api && trivy image ... arcogine-api:ci` and the same for `arcogine-ui:ci` (`infra/docker/web.Dockerfile`, context `dist/web/`) — scan built images for CRITICAL/HIGH vulnerabilities (this is where bundled Java dependencies are scanned today). Both run as part of `./arcogine check --full`.

### 19. Secret scan

`gitleaks detect --source . --config .gitleaks.toml --verbose` (part of `./arcogine check --full`) — scans the repo for leaked secrets.

## CI pipeline

The GitHub Actions workflow (`.github/workflows/ci.yml`) runs these jobs, each invoking its native tool directly:

| Job | Command | What it checks |
|-----|---------|----------------|
| Java | `./gradlew compileJava compileTestJava checkstyleMain checkstyleTest test jacocoTestReport jacocoTestCoverageVerification` | Compile, Checkstyle, unit tests, Jacoco coverage gates |
| Frontend | `npm run lint`, `npx tsc --noEmit`, `npm run test:coverage`, `npm run build`, `npm audit --audit-level=high` | Lint, typecheck, coverage, build, dependency audit |
| Playwright | `npx playwright test` (after `./gradlew :cli:bootJar`) | Browser E2E against the Java API |
| Build dist/ | `./arcogine build` | Canonical `dist/api/arcogine.jar` + `dist/web/`, uploaded as a CI artifact |
| Docker | `./arcogine image`, then the smoke sequence | Runtime image build from `dist/` + startup health-check |
| Docker image scan | `docker build` (from `dist/`) + `trivy image` (matrix: api, ui) | CRITICAL/HIGH vulnerabilities in built images |
| Java dependency audit | `./gradlew cyclonedxBom` + `trivy sbom` | CycloneDX SBOM scan for fixable CRITICAL/HIGH CVEs (see above) |
| Secret scan | `gitleaks detect` | Leaked secrets |

The Java job uses Temurin 25 + the Gradle wrapper and uploads coverage to Codecov.

## Testing architecture

### Why this structure

The test layers preserve four properties:

1. **Deterministic behavior** across identical seeds and scenarios.
2. **Behavioral parity** between the headless CLI path and the API-driven runtime.
3. **Fast feedback** for module-local logic and frontend state changes.
4. **Layered confidence** from unit, property, integration, browser, and container checks.

Route matrices and runtime error handling are validated in the `interfaces/api` smoke tests; Playwright focuses on user-visible flows rather than re-checking backend routes.

### Handler delegation contract

Factory event semantics have a single implementation authority: `FactoryHandler`. Both `interfaces/cli` (headless) and `interfaces/api` (server) use the same dispatch order:

1. Pricing
2. Demand
3. Factory
4. Agent evaluation (when applicable)

Tests protect parity between the headless and API paths. If you change event-handling behavior, ensure both runtime paths stay aligned.

### Testing SSE

The `/api/events/stream` endpoint is a servlet `SseEmitter`. The controller sends a priming SSE comment on connect so the response headers flush immediately (otherwise a client blocks waiting for headers while the simulation is idle). Tests assert status/content-type directly, and the connection-limit test holds streams open via Reactor subscriptions (disposed in a `finally`) so the semaphore limit is reached.

### Frontend testing conventions

- **Vitest** matches the Vite toolchain. **Testing Library** asserts behavior, not implementation details.
- **jsdom** provides the browser-like unit-test environment. Since jsdom has no native `EventSource`, SSE tests must mock or polyfill it.
- KPI history is capped by `MAX_KPI_HISTORY_POINTS` — tests assert the cap rather than treating history as unbounded.

### Security verification tests

The hardening checks live in the regular `interfaces/api` suite (`ApiSmokeTest`), not a separate pipeline: body-size limits, scenario validation, error propagation, CORS restrictions, SSE connection limits, economy value bounds, and CLI bind-address defaults — e.g. `oversizedBodyReturnsContentTooLarge`, `loadInvalidTomlReturnsBadRequest`, `loadScenarioWithZeroMaxTicksReturnsBadRequest`, `negativePriceReturnsBadRequest`, `extremePriceReturnsBadRequest`, `sseConnectionLimitReturns503`, `defaultBindAddressIsLocalhost`.

## Governance

When changing quality gates, update all of these together:

- `arcogine` (the `cmd_check` function)
- `.github/workflows/ci.yml`
- This document (`docs/development/testing.md`)
- `.github/SECURITY.md` (if security scan commands change)
- `.github/CONTRIBUTING.md` (if the developer workflow changes)
