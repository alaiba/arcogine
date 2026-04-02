# Testing Guide

This document describes all test categories in Arcogine, how to run each, and what success looks like.

## Prerequisites

- **Host-based checks**
  - **Rust** (stable, ≥1.94): `rustup update stable`
  - **Node.js** (≥20): for frontend checks and tests
  - **Cargo** tools: `cargo fmt`, `cargo clippy`, `cargo test`, `cargo bench`
- **Containerized checks**
  - **Docker** and Docker Compose
  - Covers runtime startup parity without requiring local Rust/Node

## Test Categories

### 1. Unit Tests (Rust)

Tests for individual modules — typed IDs, machine state, job routing, demand model, pricing, and agent logic.

```bash
cargo test
```

**What success looks like:** All tests pass (81+), zero warnings.

### 2. Property Tests

Uses `proptest` to verify invariants like monotonic time progression and no event loss.

```bash
cargo test -p sim-core --test properties
```

### 3. Integration Tests

Cross-crate tests validating behavioral outcomes:
- **Scenario baselines** — validates factory flow, demand response, revenue generation
- **Agent integration** — verifies agent interventions and backlog reduction
- **API smoke tests** — tests REST endpoints via `tower::ServiceExt`

```bash
cargo test -p sim-api
```

**What success looks like:** Scenario baselines confirm demand-price relationship, agent reduces backlog, all API endpoints return correct status codes.

### 4. Error Path Tests

Included in the API smoke tests and factory/core tests:
- Malformed JSON returns 4xx
- Commands on non-running simulations return 409
- Negative price returns 400
- Invalid state transitions return typed errors

### 5. Determinism Tests

Verify that identical seeds produce identical event logs and KPIs.

```bash
cargo test -p sim-core --test determinism
```

### 6. Benchmarks

Uses Criterion for repeatable performance measurement:

```bash
cargo bench -p sim-core
```

Benchmarks:
- **scheduler** — event scheduling and dequeuing throughput (1000 events)
- **scenario_runtime** — full scenario execution time, scenario load/validate time

### 7. Frontend Type Check

```bash
cd ui && npx tsc --noEmit
```

### 8. Frontend Build

```bash
cd ui && npm run build
```

### 9. Frontend E2E Tests (Playwright)

Requires both the API server and UI dev server running:

```bash
cd ui && npx playwright test
```

Or let Playwright manage the servers:

```bash
cd ui && npx playwright test  # uses playwright.config.ts webServer
```

This Playwright configuration starts both API and UI servers automatically in CI.

## Running Everything

```bash
# Rust checks
cargo fmt --check
cargo clippy -- -D warnings
cargo test

# Frontend checks
cd ui
npx tsc --noEmit
npm run build

# Benchmarks (optional, slow)
cargo bench -p sim-core
```

## CI

The GitHub Actions workflow (`.github/workflows/ci.yml`) runs:
1. **Rust job**: fmt check, clippy, test
2. **Frontend job**: npm ci, tsc, build, and Playwright test
3. **Compose job**: docker compose build + startup checks for API and UI
