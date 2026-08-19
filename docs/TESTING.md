# Testing Guide

This document covers all test categories in Arcogine, how to run them, and the rationale behind the testing architecture. **Make targets are the canonical quality-gate interface** — use `make <target>` from the repository root.

## Quick reference

```bash
make quality        # fast gates (before pushing): compile, Checkstyle, tests, coverage, frontend lint/typecheck/test/build
make quality-full   # everything: quality + Playwright E2E + Docker smoke + security scans
make help           # list all available targets
```

## Quality gates

| Command | Scope |
|---------|-------|
| `make quality` | Java compile (`-Xlint:all -Werror`), Checkstyle, JUnit tests, Jacoco coverage gates, frontend lint, typecheck, unit tests, frontend coverage, production build |
| `make quality-full` | Everything above, plus Playwright, Docker build/smoke, and security scans (frontend audit, Trivy image scans, Gitleaks) |

Leaf targets follow a `<domain>-<action>` naming convention (e.g. `java-test`, `frontend-lint`).

### Target model

- **Discovery:** `help` (default), `list`
- **Java:** `java-compile`, `java-lint` (Checkstyle), `java-test`, `java-coverage`, `java-bootjar`
- **Frontend:** `frontend-lint`, `frontend-typecheck`, `frontend-test`, `frontend-coverage`, `frontend-build`, `frontend-audit`
- **E2E:** `playwright`
- **Docker:** `docker-build`, `docker-smoke`
- **Security:** `trivy-scan-api`, `trivy-scan-ui`, `gitleaks`
- **CI composites:** `ci-java`, `ci-frontend`, `ci-playwright`, `ci-docker`, `ci-security`
- **Developer entrypoints:** `quality`, `quality-full`, `clean`

## Prerequisites

- **Java 25 (LTS)** — Temurin, provided by the devcontainer (SDKMAN). The build uses the Gradle wrapper (`java/gradlew`, Gradle 9.1.0); no system Gradle needed.
- **Node.js** (24+): for frontend checks and tests.
- **Docker** and Docker Compose: for container checks (optional for local dev).

All Java commands run through the Gradle wrapper under `java/`; the Make targets wrap them.

### Running Java tests without a local JDK 25

If your machine doesn't have JDK 25 installed and Gradle's toolchain auto-provisioning isn't configured (no `settings.gradle.kts` foojay resolver), run the build inside the same image the project's own `Dockerfile` uses (`gradle:9-jdk25`), rather than skipping verification:

```bash
# One-off run (simplest; pays container/JVM startup cost each time):
docker run --rm -v "$(pwd)/java:/app" -v arcogine_gradle_cache:/root/.gradle -w /app \
  gradle:9-jdk25 sh -c "./gradlew test --no-daemon"
```

For repeated runs during active development, start a long-lived container once and `exec` into it — this lets the Gradle daemon (and JIT-warmed JVM) stay resident across invocations instead of restarting from cold every time:

```bash
# Start once (per machine/session):
docker run -d --name arcogine-build -v "$(pwd)/java:/app" -v arcogine_gradle_cache:/root/.gradle \
  -w /app gradle:9-jdk25 tail -f /dev/null

# Then, repeatedly:
docker exec arcogine-build ./gradlew test
docker exec arcogine-build ./gradlew :sim-factory:test --tests "com.arcogine.factory.process.FactoryHandlerTest"

# When done:
docker stop arcogine-build && docker rm arcogine-build
```

Notes:

- The `arcogine_gradle_cache` named volume caches the downloaded Gradle distribution and dependencies across runs — without it, each fresh container re-downloads `gradle-9.1.0-bin.zip`. Neither the volume nor the `arcogine-build` container survive a move to a different machine; recreate them there with the commands above.
- If `java/*/build/` directories already exist on the host from an earlier run with a *different* JDK (e.g. a stray local JDK 21 run), Gradle may report tasks `UP-TO-DATE` from stale, filesystem-timestamp-based caching without actually re-executing them in the container — confirm real execution by checking for `PASSED`/`FAILED` lines per test, not just `BUILD SUCCESSFUL`. Delete those stale `build/` dirs once (`rm -rf java/*/build java/build`) rather than passing `--rerun-tasks` on every invocation.
- `java/gradle.properties` enables `org.gradle.parallel=true` and `org.gradle.caching=true` — these are committed to the repo and apply automatically regardless of how you invoke Gradle.

## Test categories

### 1. Java static analysis (Checkstyle)

`make java-lint` — runs `checkstyleMain checkstyleTest` (Checkstyle 13.5.0) against a deliberately minimal, high-signal ruleset (`java/config/checkstyle/checkstyle.xml`): unused/redundant/star imports plus a few bug-oriented checks. The compiler does **not** flag unused imports, so this is genuinely additive. Expand the ruleset deliberately rather than adopting a large style guide wholesale.

### 2. Java compilation

`make java-compile` — compiles all modules' main and test sources with `-Xlint:all -Werror`, so every compiler warning (including deprecations from newer Spring/Jackson) is a hard error.

### 3. Java unit tests (JUnit 5)

~178 tests across the seven modules cover typed IDs and `SimTime`, scenario schema and TOML loading, the scheduler/runner/KPI/event-log core, machine state and job routing, demand and pricing, the sales agent, the HTTP API contract, and the headless CLI.

`make java-test` — runs `./gradlew test`.

### 4. Property tests

Invariants (monotonic time, no event loss, machine concurrency limits, queue FIFO ordering) are expressed as JUnit 5 parameterized/randomized-seed tests in `sim-core` and `sim-factory`. They run as part of `make java-test`.

### 5. Integration tests

`sim-api` tests use `@SpringBootTest(webEnvironment = RANDOM_PORT)` and a `WebTestClient` built via `WebTestClient.bindToServer()` against the live server. They exercise the full HTTP contract (scenario load, run/pause/step, price/machine/agent commands, KPIs, topology, SSE), so they double as the API integration layer. Part of `make java-test`.

### 6. Determinism tests

`sim-core` verifies that identical seeds produce identical event logs and KPIs. The rewrite uses `java.util.Random`/`SplittableRandom` (not Rust's ChaCha8), so determinism is asserted as **reproducibility** — two Java runs with the same seed are byte-identical — and any golden values are captured from Java runs, never copied from the Rust implementation.

### 7. Java coverage (Jacoco) + per-module gates

`make java-coverage` — runs `test jacocoTestReport jacocoTestCoverageVerification`. Each module declares a `jacocoTestCoverageVerification` gate (a `LINE` minimum, set a few points below measured actual) wired into `check`, so removing a module's tests fails the build instead of passing vacuously. CI uploads the per-module `jacocoTestReport.xml` to Codecov.

### 8. Benchmarks (JMH)

`make java-bench` — runs the JMH microbenchmarks in `sim-core` (`./gradlew :sim-core:jmh`), ported from the Rust Criterion suites: scheduler throughput (schedule / dequeue / interleaved over 1000 events) and scenario runtime (run a 1000-tick scenario, and load+validate). Sources live in `java/sim-core/src/jmh/java/com/arcogine/core/bench/`. Benchmarks are **on-demand** (not a CI gate). Note: ASM is pinned to a Java 25-aware version so the JMH bytecode generator can read the toolchain's class files, and JMH's machine-generated classes are exempt from `-Werror`.

### 9. Java dependency audit (CycloneDX SBOM + Trivy)

`make java-audit` — generates a CycloneDX SBOM of the whole build (the `org.cyclonedx.bom` plugin → `java/build/reports/cyclonedx/bom.json`, ~179 components) and scans it with `trivy sbom` for fixable CRITICAL/HIGH CVEs. **This is a blocking gate** (`--exit-code 1`), run as part of `ci-security`, and complements `trivy-scan-api` (which scans the built image). (`trivy fs` is not used: it does not introspect a Spring Boot fat jar's nested `BOOT-INF/lib` jars without Trivy's separate Java DB.)

Shipped-runtime CVEs in `tomcat-embed-core` (3 CRITICAL + 3 HIGH) were remediated by overriding the Spring-managed version — `extra["tomcat.version"] = "11.0.22"` in `sim-api`/`sim-cli`.

A `.trivyignore` at the repo root suppresses **only non-shipped** findings, each justified inline: `netty-codec-*` (test-only — pulled by `spring-boot-starter-webflux`, the `WebTestClient` reactive client) and `plexus-utils` (build tooling). Shipped-runtime CVEs are never suppressed and will fail the gate.

(OWASP dependency-check was considered but requires an NVD API key and a large database download; Trivy reuses the vulnerability DB already present from the image scans.)

### 10–15. Frontend (lint, typecheck, unit tests, coverage, build, audit)

The React/TypeScript UI is unchanged by the Rust→Java rewrite. `make frontend-lint` (ESLint), `frontend-typecheck` (`tsc --noEmit`), `frontend-test` (Vitest), `frontend-coverage`, `frontend-build`, `frontend-audit` (`npm audit --audit-level=high`).

### 16. Playwright E2E

Browser-level user-journey tests. Requires the API server and UI dev server. `make playwright` — runs the Playwright suite against the **Java** API unchanged. (`cd ui && npx playwright test`.)

### 17. Docker build and smoke

`make docker-build` (`docker compose build`) and `make docker-smoke` (build, start, health-check `:3000/api/health` and the UI, tear down).

### 18. Container image scans

`make trivy-scan-api` and `make trivy-scan-ui` — scan built images for CRITICAL/HIGH vulnerabilities (this is where bundled Java dependencies are scanned today).

### 19. Secret scan

`make gitleaks` — scans the repo for leaked secrets.

## CI pipeline

The GitHub Actions workflow (`.github/workflows/ci.yml`) runs these jobs:

| Job | Make target | What it checks |
|-----|------------|----------------|
| Java | `make ci-java` | `java-compile`, `java-lint` (Checkstyle), `java-test`, `java-coverage` (Jacoco gates) |
| Frontend | `make ci-frontend` | `frontend-lint`, `frontend-typecheck`, `frontend-coverage`, `frontend-build`, `frontend-audit` |
| Playwright | `make playwright` | Browser E2E against the Java API |
| Docker | `make ci-docker` | `docker-build`, `docker-smoke` |
| Security | `make ci-security` | frontend audit, Trivy image scans, Gitleaks |

The Java job uses Temurin 25 + the Gradle wrapper and uploads coverage to Codecov.

## Testing architecture

### Why this structure

The test layers preserve four properties:

1. **Deterministic behavior** across identical seeds and scenarios.
2. **Behavioral parity** between the headless CLI path and the API-driven runtime.
3. **Fast feedback** for module-local logic and frontend state changes.
4. **Layered confidence** from unit, property, integration, browser, and container checks.

Route matrices and runtime error handling are validated in the `sim-api` smoke tests; Playwright focuses on user-visible flows rather than re-checking backend routes.

### Handler delegation contract

Factory event semantics have a single implementation authority: `FactoryHandler`. Both `sim-cli` (headless) and `sim-api` (server) use the same dispatch order:

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

The hardening checks live in the regular `sim-api` suite (`ApiSmokeTest`), not a separate pipeline: body-size limits, scenario validation, error propagation, CORS restrictions, SSE connection limits, economy value bounds, and CLI bind-address defaults — e.g. `oversizedBodyReturnsContentTooLarge`, `loadInvalidTomlReturnsBadRequest`, `loadScenarioWithZeroMaxTicksReturnsBadRequest`, `negativePriceReturnsBadRequest`, `extremePriceReturnsBadRequest`, `sseConnectionLimitReturns503`, `defaultBindAddressIsLocalhost`.

## Governance

When changing quality gates, update all of these together:

- `Makefile`
- `.github/workflows/ci.yml`
- This document (`docs/TESTING.md`)
- `SECURITY.md` (if security scan commands change)
- `CONTRIBUTING.md` (if the developer workflow changes)
