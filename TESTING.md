# Testing Guide

This document describes all test categories in Arcogine, how to run each, and what success looks like. **Make targets are the canonical quality-gate interface**; use `make <target>` from the repository root unless you need a scoped direct command. For the architectural rationale behind the test stack and CI split, see [`docs/testing-strategy.md`](docs/testing-strategy.md).

## Quality Gates

| Command | Scope |
|--------|--------|
| `make quality` | Fast gates: Rust format, Clippy, workspace tests, Rust coverage, frontend lint, typecheck, unit tests, frontend coverage, production build. No Docker, Playwright, or security scans. |
| `make quality-full` | Everything: runs `make quality`, then Playwright, Docker build and smoke, and the full security composite (`ci-security`: Rust audit, frontend audit, Trivy API/UI images, Gitleaks). |

Leaf targets follow a **`<domain>-<action>`** naming convention (for example `rust-test`, `frontend-lint`). Run `make help` for a grouped list.

## Prerequisites

- **Host-based checks**
  - **Rust** (stable, ≥1.94): `rustup update stable`
  - **Node.js** (≥20): for frontend checks and tests
  - **Cargo** tools: `cargo fmt`, `cargo clippy`, `cargo test`, `cargo bench`
- **Containerized checks**
  - **Docker** and Docker Compose
  - Covers runtime startup parity without requiring local Rust/Node

## Test Categories

### 1. Rust formatting (`fmt`)

**Canonical:** `make fmt`

**Direct command:** `cargo fmt --check`

### 2. Rust lints (`clippy`)

**Canonical:** `make clippy`

**Direct command:** `cargo clippy -- -D warnings`

### 3. Unit Tests (Rust) (`rust-test`)

Inline `#[cfg(test)]` modules in every crate cover typed IDs, machine state, job routing, demand model, pricing, agent logic, handler delegation, snapshot building, SSE serialization, and headless CLI execution.

**Canonical:** `make rust-test`

**Direct command:** `cargo test`

**What success looks like:** All tests pass (180+), zero warnings.

### 4. Property Tests

Uses `proptest` in `sim-core` and `sim-factory` to verify invariants like monotonic time progression, no event loss, machine concurrency limits, and queue FIFO ordering.

**Canonical:** `make rust-test` (runs the full workspace, including property tests)

**Direct command:**

```bash
cargo test -p sim-core --test properties
cargo test -p sim-factory --test properties
```

### 5. Integration Tests

Cross-crate tests validating behavioral outcomes:
- **Scenario baselines** — validates factory flow, demand response, revenue generation
- **Agent integration** — verifies agent interventions and backlog reduction
- **API smoke tests** — tests REST endpoints via `tower::ServiceExt`

**Canonical:** `make rust-test`

**Direct command:** `cargo test -p sim-api`

**What success looks like:** Scenario baselines confirm demand-price relationship, agent reduces backlog, all API endpoints return correct status codes.

### 6. Error Path Tests

Included in the API smoke tests and factory/core tests:
- Malformed JSON returns 4xx
- Commands on non-running simulations return 409
- Negative price returns 400
- Invalid state transitions return typed errors

**Canonical:** `make rust-test`

**Direct command:** `cargo test` (or `cargo test -p sim-api` for API-focused runs)

### 7. Determinism Tests

Verify that identical seeds produce identical event logs and KPIs.

**Canonical:** `make rust-test`

**Direct command:** `cargo test -p sim-core --test determinism`

### 8. Benchmarks

Uses Criterion for repeatable performance measurement. There is no dedicated Make target for benchmarks; run them directly.

**Direct command:** `cargo bench -p sim-core`

Benchmarks:
- **scheduler** — event scheduling and dequeuing throughput (1000 events)
- **scenario_runtime** — full scenario execution time, scenario load/validate time

### 9. Rust dependency audit (`rust-audit`)

**Canonical:** `make rust-audit` (also part of `make quality-full` via `make ci-security`)

**Direct command:** `cargo audit` (install with `cargo install cargo-audit` if needed; the Make target installs it when missing)

### 10. Rust code coverage (`rust-coverage`)

**Canonical:** `make rust-coverage`

**Direct command:** `cargo tarpaulin --workspace --out xml --out html --output-dir target/coverage --skip-clean` (requires `cargo-tarpaulin`)

### 11. Frontend lint (`frontend-lint`)

**Canonical:** `make frontend-lint`

**Direct command:** `cd ui && npm run lint`

### 12. Frontend unit tests (`frontend-test`)

Store, API client, SSE client, and component tests using Vitest and Testing Library.

**Canonical:** `make frontend-test`

**Direct command:** `cd ui && npm test`

**What success looks like:** All tests pass (51+), zero warnings.

### 13. Frontend type check (`frontend-typecheck`)

**Canonical:** `make frontend-typecheck`

**Direct command:** `cd ui && npx tsc --noEmit`

### 14. Frontend build (`frontend-build`)

**Canonical:** `make frontend-build`

**Direct command:** `cd ui && npm run build`

### 15. Frontend coverage (`frontend-coverage`)

**Canonical:** `make frontend-coverage`

**Direct command:** `cd ui && npm run test:coverage`

### 16. Frontend dependency audit (`frontend-audit`)

**Canonical:** `make frontend-audit` (also part of `make ci-frontend` and `make ci-security`)

**Direct command:** `cd ui && npm audit --audit-level=high`

### 17. Frontend E2E tests (`playwright`)

Requires both the API server and UI dev server running (or use Playwright’s `webServer` in `playwright.config.ts`, as in CI).

**Canonical:** `make playwright`

**Direct command:** `cd ui && npx playwright test`

### 18. Docker image build (`docker-build`)

**Canonical:** `make docker-build`

**Direct command:** `docker compose build`

### 19. Docker compose smoke (`docker-smoke`)

**Canonical:** `make docker-smoke` (also part of `make ci-docker`)

**Direct command:** Copies `.env.example` to `.env`, runs `docker compose up -d --wait`, curls API and UI health URLs, then `docker compose down` (see the `Makefile` for exact steps and `UI_PORT`).

### 20. Container image scan — API (`trivy-scan-api`)

**Canonical:** `make trivy-scan-api`

**Direct command:** `docker build -t arcogine-api:ci .` then `trivy image --severity CRITICAL,HIGH --ignore-unfixed --exit-code 1 arcogine-api:ci`

### 21. Container image scan — UI (`trivy-scan-ui`)

**Canonical:** `make trivy-scan-ui`

**Direct command:** `docker build -t arcogine-ui:ci ui` then `trivy image --severity CRITICAL,HIGH --ignore-unfixed --exit-code 1 arcogine-ui:ci`

### 22. Secret scan (`gitleaks`)

**Canonical:** `make gitleaks`

**Direct command:** `gitleaks detect --source . --config .gitleaks.toml --verbose`

## Running Everything

From the repository root:

- **Fast local gate (recommended before push):** `make quality`
- **Full gate (matches extended CI surface, including E2E, Docker, security):** `make quality-full`

**Optional (slow, no Make target):** `cargo bench -p sim-core`

## CI

The GitHub Actions workflow (`.github/workflows/ci.yml`) invokes Make targets:

1. **Rust job** — `make ci-rust` (`fmt`, `clippy`, `rust-test`, `rust-coverage`)
2. **Frontend job** — `make ci-frontend` (`frontend-lint`, `frontend-typecheck`, `frontend-coverage`, `frontend-build`, `frontend-audit`). Note: CI uses `frontend-coverage` rather than `frontend-test` alone.
3. **Playwright job** — builds the API binary and installs Chromium in the workflow, then `make playwright`
4. **Docker job** — `make ci-docker` (`docker-build`, `docker-smoke`)
5. **Docker image scan job** — `make trivy-scan-api` or `make trivy-scan-ui` (matrix)
6. **Secret scan job** — `make gitleaks`
7. **Makefile contract job** — verifies `make help`, `make list`, and dry-runs for composite targets (`make -n ci-rust ci-frontend ci-playwright ci-docker ci-security rust-audit`)
