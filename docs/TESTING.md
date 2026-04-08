# Testing Guide

This document covers all test categories in Arcogine, how to run them, and the rationale behind the testing architecture. **Make targets are the canonical quality-gate interface** — use `make <target>` from the repository root.

## Quick reference

```bash
make quality        # fast gates (before pushing): fmt, clippy, tests, coverage, lint, typecheck, build
make quality-full   # everything: quality + Playwright E2E + Docker smoke + security scans
make help           # list all available targets
```

## Quality gates

| Command | Scope |
|---------|-------|
| `make quality` | Rust format, Clippy, workspace tests, Rust coverage, frontend lint, typecheck, unit tests, frontend coverage, production build |
| `make quality-full` | Everything above, plus Playwright, Docker build/smoke, and security scans (Rust audit, frontend audit, Trivy, Gitleaks) |

Leaf targets follow a `<domain>-<action>` naming convention (e.g., `rust-test`, `frontend-lint`).

### Target model

- **Discovery:** `help` (default), `list`
- **Rust:** `fmt`, `clippy`, `rust-test`, `rust-audit`, `rust-coverage`
- **Frontend:** `frontend-lint`, `frontend-typecheck`, `frontend-test`, `frontend-coverage`, `frontend-build`, `frontend-audit`
- **E2E:** `playwright`
- **Docker:** `docker-build`, `docker-smoke`
- **Security:** `trivy-scan-api`, `trivy-scan-ui`, `gitleaks`
- **CI composites:** `ci-rust`, `ci-frontend`, `ci-playwright`, `ci-docker`, `ci-security`
- **Developer entrypoints:** `quality`, `quality-full`, `clean`

## Prerequisites

- **Rust** (stable, as specified in `rust-toolchain.toml`): `rustup update stable`
- **Node.js** (20+): for frontend checks and tests
- **Docker** and Docker Compose: for container checks (optional for local dev)

## Test categories

### 1. Rust formatting

`make fmt` — runs `cargo fmt --check`.

### 2. Rust lints

`make clippy` — runs `cargo clippy -- -D warnings`.

### 3. Rust unit tests

Inline `#[cfg(test)]` modules in every crate cover typed IDs, machine state, job routing, demand model, pricing, agent logic, handler delegation, snapshot building, SSE serialization, and headless CLI execution.

`make rust-test` — runs `cargo test`.

**Success:** 180+ tests pass, zero warnings.

### 4. Property tests

Uses `proptest` to verify invariants like monotonic time progression, no event loss, machine concurrency limits, and queue FIFO ordering.

```bash
cargo test -p sim-core --test properties
cargo test -p sim-factory --test properties
```

These run as part of `make rust-test`.

### 5. Integration tests

Cross-crate tests in `crates/sim-api/tests/` validate scenario baselines, agent integration, API route behavior, and simulation-thread interactions.

`make rust-test` or directly: `cargo test -p sim-api`

### 6. Determinism tests

Verify that identical seeds produce identical event logs and KPIs.

```bash
cargo test -p sim-core --test determinism
```

### 7. Rust coverage

`make rust-coverage` — runs `cargo llvm-cov` and outputs to `target/coverage/cobertura.xml`.

### 8. Rust dependency audit

`make rust-audit` — runs `cargo audit`. Also part of `make quality-full`.

### 9. Benchmarks

Criterion benchmarks for scheduler throughput and scenario runtime. No dedicated Make target.

```bash
cargo bench -p sim-core
```

### 10. Frontend lint

`make frontend-lint` — runs `cd ui && npm run lint`.

### 11. Frontend type check

`make frontend-typecheck` — runs `cd ui && npx tsc --noEmit`.

### 12. Frontend unit tests

Store, API client, SSE client, and component tests using Vitest and Testing Library.

`make frontend-test` — runs `cd ui && npm test`.

**Success:** 51+ tests pass, zero warnings.

### 13. Frontend coverage

`make frontend-coverage` — runs `cd ui && npm run test:coverage`.

### 14. Frontend build

`make frontend-build` — runs `cd ui && npm run build`.

### 15. Frontend dependency audit

`make frontend-audit` — runs `cd ui && npm audit --audit-level=high`.

### 16. Playwright E2E

Browser-level user journey tests. Requires both the API server and UI dev server.

`make playwright` — runs `cd ui && npx playwright test`.

### 17. Docker build and smoke

`make docker-build` — runs `docker compose build`.

`make docker-smoke` — builds, starts containers, verifies API/UI health endpoints, then tears down.

### 18. Container image scans

`make trivy-scan-api` and `make trivy-scan-ui` — scan built images for CRITICAL/HIGH vulnerabilities.

### 19. Secret scan

`make gitleaks` — scans git history for leaked secrets.

## CI pipeline

The GitHub Actions workflow (`.github/workflows/ci.yml`) runs these jobs:

| Job | Make target | What it checks |
|-----|------------|----------------|
| Rust | `make ci-rust` | `fmt`, `clippy`, `rust-test`, `rust-coverage` |
| Frontend | `make ci-frontend` | `frontend-lint`, `frontend-typecheck`, `frontend-coverage`, `frontend-build`, `frontend-audit` |
| Playwright | `make playwright` | Browser E2E tests (CI handles binary build and browser install) |
| Docker | `make ci-docker` | `docker-build`, `docker-smoke` |
| Image scans | `make trivy-scan-{api,ui}` | Container vulnerability scanning |
| Secret scan | `make gitleaks` | Repository secret detection |
| Contract check | `make help && make -n ci-*` | Verifies all Make targets resolve |

## Testing architecture

### Why this structure

The test layers are designed to preserve four properties:

1. **Deterministic behavior** across identical seeds and scenarios.
2. **Behavioral parity** between the headless CLI path and the API-driven runtime.
3. **Fast feedback** for crate-local logic and frontend state changes.
4. **Layered confidence** from unit, property, integration, browser, and container checks.

Route matrices and runtime error handling are cheaper to validate in API smoke tests. Playwright focuses on user-visible flows, not exhaustively rechecking backend routes.

### Handler delegation contract

Factory event semantics have a single implementation authority: `FactoryHandler`. Both `sim-cli` (headless) and `sim-api` (server) use the same dispatch order:

1. Pricing
2. Demand
3. Factory
4. Agent evaluation (when applicable)

Tests protect parity between the headless and API paths. If you change event-handling behavior, ensure both runtime paths stay aligned.

### Frontend testing conventions

- **Vitest** matches the Vite toolchain. **Testing Library** asserts behavior, not implementation details.
- **jsdom** provides the browser-like unit-test environment. Since jsdom has no native `EventSource`, SSE tests must mock or polyfill it.
- KPI history is capped by `MAX_KPI_HISTORY_POINTS` — tests assert the cap rather than treating history as unbounded.

### Security verification tests

The hardening layer added 18 tests covering body-size limits, scenario validation, error propagation, CORS restrictions, SSE connection limits, event log capacity, economy value bounds, and CLI bind-address defaults. These are part of the regular test suite, not a separate pipeline:

- `oversized_body_returns_payload_too_large`, `body_under_limit_is_accepted`
- `load_valid_scenario_returns_success`, `load_invalid_toml_returns_bad_request`
- `load_scenario_with_zero_max_ticks_returns_bad_request`, `load_scenario_with_missing_equipment_returns_bad_request`
- `handler_error_surfaces_in_snapshot`, `serve_default_addr_is_localhost`
- `sse_connection_limit_returns_503`
- `event_log_caps_at_max_capacity`, `event_log_equality_ignores_capacity`, `event_log_is_truncated`
- `scenario_with_nan_price_rejected`, `scenario_with_inf_demand_rejected`
- `scenario_with_extreme_price_rejected`, `scenario_with_extreme_base_demand_rejected`
- `extreme_price_returns_bad_request`, `cors_with_env_var_restricts_origin`

## Governance

When changing quality gates, update all of these together:

- `Makefile`
- `.github/workflows/ci.yml`
- This document (`docs/TESTING.md`)
- `SECURITY.md` (if security scan commands change)
- `CONTRIBUTING.md` (if the developer workflow changes)
