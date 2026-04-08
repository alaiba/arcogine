# Arcogine — Testing Strategy

This document captures the long-lived testing decisions behind Arcogine's quality strategy. Use [`../TESTING.md`](../TESTING.md) for day-to-day commands and pass criteria.

## Goals

Arcogine's test strategy is designed to preserve four properties:

1. **Deterministic behavior** across identical seeds and scenarios.
2. **Behavioral parity** between the headless CLI path and the API-driven runtime.
3. **Fast feedback** for crate-local logic and frontend state changes.
4. **Layered confidence** from unit, property, integration, browser, and container checks.

## Test Layering

Arcogine intentionally uses different test styles for different risks:

- **Inline Rust unit tests** (`#[cfg(test)]`) cover crate-local logic close to the code that owns it.
- **Rust integration tests** cover cross-crate behavior, API contracts, and scenario outcomes.
- **Property tests** protect invariants that should hold across many generated inputs.
- **Frontend unit tests** cover stores, typed API clients, SSE behavior, and priority UI components.
- **E2E tests** cover browser workflows and wiring across the API, SSE, and UI.
- **Docker compose smoke checks** verify the shipped container topology starts and serves traffic.

This split is deliberate: route matrices and runtime error handling are cheaper and more stable to validate in API smoke tests, while Playwright focuses on user-visible flows instead of exhaustively rechecking every backend route.

## Rust Testing Conventions

### Unit tests live with the code

Rust crates prefer inline `#[cfg(test)]` modules for function-level behavior, defaults, serialization, and handler delegation. This keeps tests close to internal APIs without widening the public surface.

### Integration tests validate end-to-end outcomes

Cross-crate behaviors live under `crates/sim-api/tests/`, where tests validate:

- scenario baselines,
- agent integration,
- API route behavior, and
- simulation-thread interactions exposed through the HTTP layer.

### Property tests protect invariants

`proptest` is reserved for invariants that benefit from generated inputs rather than hand-authored fixtures, including determinism, queue ordering, machine concurrency limits, and job lifecycle constraints.

## Handler Delegation Contract

Factory event semantics have a single implementation authority: `FactoryHandler`.

Both runtime entrypoints use the same dispatch order:

1. pricing,
2. demand,
3. factory,
4. agent evaluation when applicable.

This is a design constraint, not an implementation detail. Arcogine previously carried duplicated factory-event logic in multiple runtime paths; the current contract keeps queue dispatch, `TaskStart`/`TaskEnd` emission, revenue accounting, and machine-availability behavior aligned across headless and API execution.

Tests should continue to protect parity between the API runtime and the headless runner whenever event-handling behavior changes.

## Frontend Testing Conventions

### Scope

Frontend unit tests cover:

- Zustand stores,
- the typed REST client,
- the SSE client and reconnect behavior,
- critical dashboard and shared components, and
- baseline comparison behavior.

### Tooling choices

- **Vitest** is the primary unit-test runner because it matches the Vite toolchain.
- **Testing Library** is used for component behavior rather than implementation-detail assertions.
- **jsdom** provides the browser-like unit-test environment.

Because `jsdom` does not provide a native `EventSource`, SSE tests must mock or polyfill it explicitly.

### Long-running session guardrails

The frontend simulation store caps KPI history with `MAX_KPI_HISTORY_POINTS` so long SSE sessions remain bounded in memory. Tests should continue to assert the cap rather than treating history as unbounded.

## E2E Strategy

Playwright is used for user journeys, not as a replacement for API smoke tests.

E2E tests should:

- prefer event- or visibility-based waits over fixed sleeps,
- make post-action assertions after run/pause/step flows,
- verify browser-visible outcomes instead of internal implementation details, and
- stay focused on representative operator workflows.

## CI Quality Gates

CI consumes Make targets as the single source of truth for quality gate commands:

- **Rust job** (`make ci-rust`): formatting, clippy, workspace tests, and coverage.
- **Frontend job** (`make ci-frontend`): ESLint, type-checking, Vitest unit tests with coverage, production build, and npm audit.
- **Playwright job** (`make playwright`): browser E2E validation with CI-managed servers and browsers.
- **Docker job** (`make ci-docker`): container build/startup parity plus API/UI reachability checks.
- **Security jobs**: `make trivy-scan-api`, `make trivy-scan-ui`, `make gitleaks`, and `make rust-audit`.

Coverage is collected in CI as an informational signal. Functional correctness, linting, formatting, and build/test success remain the blocking quality gates.

## Security hardening verification coverage

The completed hardening work added explicit verification for API, CLI, simulator, and CI control-plane changes.
Deployment posture is documented in `../SECURITY.md`; architecture and runtime constraints are in
`architecture-overview.md`; the primary test evidence is this section.

- `api_smoke.rs`: body-size limits, scenario load success/error propagation, invalid command state transitions, and CORS restriction checks.
- `sim-cli` unit tests: default CLI bind address remains `127.0.0.1` for non-container execution.
- `sim-core` unit tests: event log capacity behavior, equality semantics, and economy value bounds.
- CI workflow jobs: Rust dependency audit (`rustsec/audit-check`), npm audit, Trivy image scans, and Gitleaks secret scan.
- Existing `PLAYWRIGHT` and `cargo-tarpaulin` coverage jobs continue to validate runtime behavior and regression resistance.

Residual risk evidence:

- R1 (unauthenticated API): mitigated by local-first default binding; external exposure requires deployment controls.
- R2 (CORS): validated with env-driven origin tests.
- R3 (body size): validated at router layer.
- R4 (event log growth): validated with truncation-cap tests.
- R5 (SSE fan-out): validated with concurrency limit test.
- R6/R7 (scenario and runtime errors): validated through explicit error propagation tests.
- R8 (bind address): validated in CLI default-address test.
- R9 (headers): verified in container verification path and CI container job.
- R10 (reproducibility): lockfile/caching posture remains controlled by committed workspace lockfile.
- R11/R12 (supply-chain and image risk): validated by audit/scan CI jobs.
- R14 (economy/price bounds): validated with TOML + API tests.

Named verification tests now included for the hardening set:

- `oversized_body_returns_payload_too_large`
- `body_under_limit_is_accepted`
- `load_valid_scenario_returns_success`
- `load_invalid_toml_returns_bad_request`
- `load_scenario_with_zero_max_ticks_returns_bad_request`
- `load_scenario_with_missing_equipment_returns_bad_request`
- `handler_error_surfaces_in_snapshot`
- `serve_default_addr_is_localhost`
- `sse_connection_limit_returns_503`
- `event_log_caps_at_max_capacity`
- `event_log_equality_ignores_capacity`
- `event_log_is_truncated`
- `scenario_with_nan_price_rejected`
- `scenario_with_inf_demand_rejected`
- `scenario_with_extreme_price_rejected`
- `scenario_with_extreme_base_demand_rejected`
- `extreme_price_returns_bad_request`
- `cors_with_env_var_restricts_origin`

This hardening layer added 19 new tests and keeps security controls part of the same long-lived quality gate strategy instead of maintaining a separate security-only pipeline.

## Documentation Boundaries

- Use [`../TESTING.md`](../TESTING.md) for run commands, prerequisites, and "what success looks like".
- Use this document for testing architecture, conventions, and rationale.
- Use [`architecture-overview.md`](architecture-overview.md) when a testing decision is really an architectural constraint, such as shared handler delegation or determinism guarantees.
