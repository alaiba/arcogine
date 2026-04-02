# Testability Improvement Plan

> **Date:** 2026-04-02
> **Scope:** Identify opportunities to improve quality assurance across the Arcogine codebase — Rust crates, UI, CI/CD, and documentation — and provide actionable, prioritized work items.
> **Primary sources:** All crates under `crates/`, `ui/`, `.github/workflows/ci.yml`, `TESTING.md`, `CONTRIBUTING.md`, `docs/architecture-overview.md`

---

## 1. Current State Assessment

### 1.1 Rust Test Inventory

| Crate | Test directory | Test files | Patterns | Coverage focus |
|-------|---------------|-----------|----------|---------------|
| `sim-types` | — | **None** | — | Types exercised indirectly via other crate tests |
| `sim-core` | `tests/` | `determinism.rs`, `event_ordering.rs`, `properties.rs`, `scenario_loading.rs` | `#[test]`, `proptest` | Scheduler ordering, scenario loading/validation, determinism, properties |
| `sim-core` | `benches/` | `scheduler.rs`, `scenario_runtime.rs` | Criterion | Scheduler throughput, scenario execution time |
| `sim-factory` | `tests/` | `job_routing.rs`, `machine_state.rs` | `#[test]` | Job lifecycle, machine state transitions, routing lookups |
| `sim-economy` | `tests/` | `demand_model.rs`, `pricing.rs` | `#[test]` | Demand formula correctness, pricing state tracking |
| `sim-agents` | `tests/` | `sales_agent.rs` | `#[test]` | Agent decision logic, event handling, min/max clamping |
| `sim-api` | `tests/` | `api_smoke.rs`, `agent_integration.rs`, `scenario_baselines.rs` | `#[tokio::test]`, `#[test]` | HTTP route smoke tests, full-loop integration, baseline assertions |
| `sim-cli` | — | **None** | — | No test coverage |

Inline `#[cfg(test)]` modules: **none** in any source file across all crates.

### 1.2 Frontend Test Inventory

| Area | Files | Patterns | Coverage |
|------|-------|----------|----------|
| E2E | `ui/e2e/smoke.spec.ts` | Playwright | Welcome overlay, scenario load, partial run/events |
| Unit/Component | **None** | — | No unit tests for any UI code |

No `test` script in `ui/package.json`. No Vitest, Jest, or Testing Library in dependencies.

### 1.3 CI Pipeline (`ci.yml`)

| Job | Steps | Missing |
|-----|-------|---------|
| `rust` | `cargo fmt --check`, `cargo clippy -- -D warnings`, `cargo test` | Coverage reporting, benchmark regression gates |
| `frontend` | `npm ci`, `npx tsc --noEmit`, `npm run build` | `npm run lint`, Playwright, unit tests, coverage |

### 1.4 Key Gaps Identified

1. **No frontend unit tests** — 18 components, 2 stores, 2 API modules with zero unit-level tests
2. **No inline unit tests** — all Rust crates lack `#[cfg(test)]` modules for per-function testing
3. **`sim-types` untested** — the foundational crate has no direct tests
4. **`sim-cli` untested** — the CLI binary has no test coverage
5. **`FactoryHandler.handle_event()` (`process.rs`) never exercised by any test** — `sim-api` integration tests use inline reimplementations that diverge from `FactoryHandler` (see §2.6); the only caller of `FactoryHandler.handle_event()` is the untested `HeadlessHandler` in `sim-cli`
6. **`IntegratedHandler` in `state.rs` duplicates `FactoryHandler` logic** — large regression surface
7. **No code coverage tooling** — neither Rust (`cargo-tarpaulin`, `llvm-cov`) nor frontend
8. **CI does not run ESLint or Playwright** — despite both being configured
9. **`proptest` unused in `sim-factory`** — declared as `dev-dependency` in `Cargo.toml` but no usage
10. **E2E tests rely on `waitForTimeout`** — fragile; several tests lack assertions after actions
11. **API routes use fixed `sleep` for command propagation** — makes integration tests slow/flaky

---

## 2. Improvement Plan

### 2.1 Add Inline Unit Tests to Rust Crates

**Priority:** High
**Effort:** Medium
**Crates:** All seven (`sim-types`, `sim-core`, `sim-factory`, `sim-economy`, `sim-agents`, `sim-api`, `sim-cli`)

Add `#[cfg(test)] mod tests { ... }` modules to source files for fine-grained, per-function tests. These complement the existing integration tests by testing internal logic that is not exposed through public APIs.

#### 2.1.1 `sim-types` — Foundation Types

File: `crates/sim-types/src/lib.rs` (201 lines)

Tests to add:
- `SimTime::ticks()` returns the inner value
- `SimTime + u64` arithmetic correctness
- `SimTime - SimTime` produces correct delta
- `SimTime::ZERO` equals `SimTime(0)`
- `Quantity::units()` / `Quantity::as_units()` round-trip
- `Quantity::default()` is zero
- `SimError::Display` produces readable messages for each variant
- `MachineState` and `JobStatus` enum serialization round-trips

File: `crates/sim-types/src/scenario.rs` (137 lines)

Tests to add:
- Default values (`default_demand_interval`, `default_agent_interval`, etc.) return documented values
- `ScenarioConfig` round-trip through `serde` (`Serialize` + `Deserialize`)
- Partial TOML with defaults fills in correctly

#### 2.1.2 `sim-core` — Event Engine

File: `crates/sim-core/src/event.rs` (76 lines)

Tests to add:
- `Event::new` sets `time` and `payload` correctly
- `EventType` derived from each `EventPayload` variant is correct

File: `crates/sim-core/src/handler.rs` (35 lines)

Tests to add:
- `CompositeHandler` dispatches to all inner handlers
- `CompositeHandler` propagates first `Err` and short-circuits

File: `crates/sim-core/src/kpi.rs` (95 lines)

Tests to add:
- Each `Kpi::compute` on empty `EventLog` returns zero/baseline value
- `TotalSimulatedTime::compute` equals `final_time.ticks()` as `f64`
- `EventCount::compute` counts all events in the log
- `ThroughputRate::compute` divides completed count by simulated time
- `OrderCount::compute` counts `OrderCreation` events

File: `crates/sim-core/src/log.rs` (51 lines)

Tests to add:
- `EventLog::new` is empty
- `append` increases `count()`
- `filter_by_type` returns only matching events
- `snapshot` returns a clone of the current log state
- `iter` yields events in insertion order

File: `crates/sim-core/src/runner.rs` (90 lines)

Tests to add:
- `run_scenario` with zero `max_ticks` returns immediately
- `run_scenario` seeds `DemandEvaluation` at `demand_eval_interval`
- `run_scenario` seeds `AgentEvaluation` only when `agent` config is present AND `agent.enabled` is true
- `run_scenario` with non-zero `agent_eval_interval` but no `[agent]` config section does **not** seed `AgentEvaluation`
- Handler error propagates as `Err` from `run_scenario`

#### 2.1.3 `sim-factory` — Factory Domain

File: `crates/sim-factory/src/process.rs` (264 lines)

Tests to add:
- `FactoryHandler::new` initializes with correct machine/routing counts
- `backlog()` correctly counts `Queued`-status jobs
- `avg_lead_time()` returns `0.0` when no completed jobs exist
- `avg_lead_time()` computes correct average over multiple completed jobs
- `throughput(elapsed_ticks)` returns `completed_sales / elapsed_ticks` and returns `0.0` when `elapsed_ticks` is `0`
- `handle_event` for `OrderCreation` creates job and dispatches to first machine
- `handle_event` for `OrderCreation` enqueues job when machine is at capacity
- `handle_event` for `TaskEnd` completes job and dequeues next from machine
- `handle_event` for `TaskEnd` on multi-step routing advances to next step
- `handle_event` for `MachineAvailabilityChange` dispatches queued jobs when going online
- Revenue is tracked as `current_price * quantity` on job completion (note: `process.rs` uses `current_price: 0.0` — this is a known divergence from `IntegratedHandler` in `state.rs`)

File: `crates/sim-factory/src/routing.rs` (102 lines)

Tests to add:
- `Routing::step_count()` returns correct count
- `Routing::get_step()` out-of-bounds returns `None`
- `RoutingStore::add_product_routing` → `get_routing_for_product` round-trip

#### 2.1.4 `sim-economy` — Economy Domain

File: `crates/sim-economy/src/demand.rs` (109 lines)

Tests to add:
- `generate_orders` with zero computed demand produces no events
- `generate_orders` schedules `OrderCreation` events at `current_time` with products drawn from `product_ids`
- `handle_event` ignores non-`DemandEvaluation`/`PriceChange` events
- `handle_event` for `DemandEvaluation` calls `generate_orders` and schedules `OrderCreation` events (note: rescheduling of periodic `DemandEvaluation` is done by the runner in `sim-core`, not by `DemandModel`)
- `handle_event` for `PriceChange` updates `current_price`

File: `crates/sim-economy/src/pricing.rs` (37 lines)

Tests to add:
- `handle_event` for `PriceChange` updates `current_price` and appends to history
- `handle_event` ignores non-`PriceChange` events

#### 2.1.5 `sim-agents` — Agent Layer

File: `crates/sim-agents/src/sales_agent.rs` (131 lines)

Tests to add:
- `observe` updates internal state correctly
- `decide` returns `None` when price delta is below epsilon threshold
- `SalesAgentConfig::Default` values are within documented bounds

#### 2.1.6 `sim-api` — API Layer

File: `crates/sim-api/src/state.rs` (770 lines)

Tests to add:
- `SimSnapshot::default()` has `run_state == Idle` and `scenario_loaded == false`
- `IntegratedHandler` event dispatch order: pricing → demand → inline factory logic (does NOT delegate to `FactoryHandler.handle_event`)
- `build_snapshot` produces correct topology edges for multi-step routings
- `build_snapshot` handles empty routings (no edges)
- `spawn_sim_thread` + `LoadScenario` command: snapshot transitions to scenario_loaded
- `spawn_sim_thread` + `Run` without loaded scenario: returns error state
- `spawn_sim_thread` + `Step` advances exactly one event
- `ChangePrice` with negative value: handled correctly
- `ChangeMachineCount`: machine goes offline → online triggers dispatch of queued jobs (currently broken — §2.6 divergence 3)
- `handle_event` error during `Step` or `Run` is reported in snapshot (currently suppressed — §2.6 divergence 4)
- Event log from `IntegratedHandler` includes `TaskStart` events (currently missing — §2.6 divergence 2)

File: `crates/sim-api/src/sse.rs` (32 lines)

Tests to add:
- `event_stream` serialization failure produces empty data (current behavior), not panic

#### 2.1.7 `sim-cli` — CLI Binary

File: `crates/sim-cli/src/main.rs` (168 lines)

Tests to add:
- `HeadlessHandler` delegates to `pricing → demand → factory.handle_event()` (note: this differs from `IntegratedHandler` which reimplements factory logic inline — see §2.6 for divergences; the §2.6 refactor should unify them)
- CLI arg parsing with `clap` test helpers
- `Run` subcommand with invalid file path exits with error
- Headless run produces correct `completed_sales` and `total_revenue` (currently `total_revenue` is zero because `FactoryHandler` uses `current_price: 0.0` — documents the §2.6 divergence 1 before refactoring)

### 2.2 Add Property Tests to `sim-factory`

**Priority:** Medium
**Effort:** Low

`sim-factory/Cargo.toml` declares `proptest` as a `dev-dependency` but no proptest tests exist. Add property tests for:

File: `crates/sim-factory/tests/properties.rs` (new file)

- **Job lifecycle invariant**: for any sequence of `start`/`complete_step` operations on a valid job, `current_step` never exceeds `total_steps`
- **Machine concurrency invariant**: for any sequence of `start_job`/`complete_job`, `active_jobs.len()` never exceeds `concurrency`
- **Queue FIFO invariant**: enqueued jobs are dequeued in insertion order
- **No lost jobs**: total of active + queued + completed jobs always equals created jobs

### 2.3 Add Frontend Unit Testing Infrastructure

**Priority:** High
**Effort:** Medium

#### 2.3.1 Install Vitest and Testing Library

Add to `ui/package.json` `devDependencies`:
- `vitest`
- `@testing-library/react`
- `@testing-library/jest-dom`
- `@testing-library/user-event`
- `jsdom`

Add `test` config to `ui/vite.config.ts` (add the triple-slash reference so TypeScript recognizes the `test` property from Vitest):

```typescript
/// <reference types="vitest/config" />
export default defineConfig({
  // ... existing plugins, server config
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/test/setup.ts'],
    include: ['src/**/*.test.{ts,tsx}'],
  },
})
```

Add `test` script to `ui/package.json`:

```json
"test": "vitest run",
"test:watch": "vitest",
"test:coverage": "vitest run --coverage"
```

Create `ui/src/test/setup.ts`:

```typescript
import '@testing-library/jest-dom'
```

#### 2.3.2 Store Unit Tests

File: `ui/src/stores/simulation.test.ts` (new)

Tests:
- `mergeSnapshot` appends to `kpiHistory`
- `mergeSnapshot` limits `kpiHistory` to max entries
- `withLoading` sets `loading` to `true` then `false`
- `withLoading` sets `error` on rejection
- `clearError` clears the error state
- `loadScenario` calls `postScenario` then `fetchSnapshot`
- `connectSse` / `disconnectSse` lifecycle

File: `ui/src/stores/baselines.test.ts` (new)

Tests:
- `saveBaseline` adds a baseline to the list
- `saveBaseline` limits to 3 baselines (oldest removed)
- `removeBaseline` by id works correctly
- `clearBaselines` empties the list
- `getDeltas` computes correct percentage differences
- `getDeltas` handles zero baseline values (no division by zero)

#### 2.3.3 API Client Unit Tests

File: `ui/src/api/client.test.ts` (new)

Tests (mock `fetch`):
- `readErrorMessage` extracts `error` field from JSON response
- `readErrorMessage` extracts `message` field as fallback
- `readErrorMessage` falls back to status text for non-JSON
- `jsonRequest` throws on non-OK response with server message
- `postScenario` sends correct payload and returns parsed response
- `getHealth` returns health status
- Error responses from each endpoint are handled

File: `ui/src/api/sse.test.ts` (new)

Note: `EventSource` is not available in jsdom. Mock it using a custom class or install the `eventsource` npm package as a polyfill in the test setup file.

Tests (mock `EventSource`):
- Reconnect delay doubles on each failure up to cap
- `connect` is idempotent when already open
- `onEvent` callback fires for valid JSON payloads
- Malformed JSON is silently ignored (no throw)
- `disconnect` closes the connection

#### 2.3.4 Component Unit Tests (Priority Components)

File: `ui/src/components/dashboard/KpiCards.test.tsx` (new)

Tests:
- Renders placeholder cards when snapshot is `null`
- Renders four KPI cards with correct labels
- `avgLeadTimeTicks` computes correct value from jobs
- `findKpi` returns fallback values when KPI is missing
- Non-finite numbers in `formatNumber` are handled

File: `ui/src/components/dashboard/JobTracker.test.tsx` (new)

Tests:
- Renders empty state when no jobs
- Renders job rows with correct status badges
- `compareJobs` handles null `revenue` values
- Sort toggle changes direction

File: `ui/src/components/shared/ErrorBoundary.test.tsx` (new)

Tests:
- Renders children normally when no error
- Shows error message when child throws
- Retry button re-renders children

File: `ui/src/components/shared/Toast.test.tsx` (new)

Tests:
- Renders message text
- Auto-dismiss after timeout
- Manual dismiss calls `onDismiss`

File: `ui/src/components/experiment/BaselineCompare.test.tsx` (new)

Tests:
- `isImprovement` returns correct boolean for each metric
- `formatDelta` formats positive/negative deltas correctly
- Renders baseline list from store

### 2.4 Improve E2E Test Quality

**Priority:** Medium
**Effort:** Low

File: `ui/e2e/smoke.spec.ts`

Changes:
1. **Replace `waitForTimeout` with `waitForSelector` or `expect(...).toBeVisible()`** — all five tests use fixed delays that create flaky behavior
2. **Add assertions to conditional tests** — "scenario selector loads and run produces events" (line 32), "event log drawer" (line 51), and "factory flow" (line 61) have `if (await ...)` guards with no assertions if the condition is false; these should `test.skip()` with a reason or always assert
3. **Add post-run assertions** — after clicking "Run", assert that `events_processed > 0` by checking a KPI card or the event log count
4. **Add E2E coverage for untested routes** — `/api/sim/pause`, `/api/snapshot`, `/api/jobs`, `/api/agent`, `/api/events/stream` (SSE)

### 2.5 Extend CI Pipeline

**Priority:** High
**Effort:** Low

File: `.github/workflows/ci.yml`

#### 2.5.1 Add ESLint to Frontend Job

```yaml
- name: Lint
  run: npm run lint
```

This already works locally (`ui/package.json` has a `lint` script and ESLint 9 is configured).

#### 2.5.2 Add Frontend Unit Tests to CI

After installing Vitest (§2.3.1):

```yaml
- name: Unit tests
  run: npm test
```

#### 2.5.3 Add Playwright to CI

Add a new job or extend the frontend job:

```yaml
e2e:
  name: E2E tests
  runs-on: ubuntu-latest
  steps:
    - uses: actions/checkout@v4
    - name: Install Rust toolchain
      uses: dtolnay/rust-toolchain@stable
    - name: Cache cargo
      uses: actions/cache@v4
      with:
        path: |
          ~/.cargo/registry
          ~/.cargo/git
          target
        key: ${{ runner.os }}-cargo-${{ hashFiles('**/Cargo.lock') }}
        restore-keys: ${{ runner.os }}-cargo-
    - name: Build API
      run: cargo build -p sim-cli
    - name: Setup Node.js
      uses: actions/setup-node@v4
      with:
        node-version: '20'
        cache: 'npm'
        cache-dependency-path: ui/package-lock.json
    - name: Install dependencies
      run: npm ci
      working-directory: ui
    - name: Install Playwright browsers
      run: npx playwright install --with-deps chromium
      working-directory: ui
    - name: Run E2E tests
      run: npx playwright test
      working-directory: ui
```

#### 2.5.4 Add Rust Code Coverage

Add `cargo-tarpaulin` or `llvm-cov` to the Rust CI job:

```yaml
- name: Code coverage
  run: cargo install cargo-tarpaulin && cargo tarpaulin --workspace --out xml
- name: Upload coverage
  uses: codecov/codecov-action@v4
  with:
    files: cobertura.xml
```

### 2.6 Address Handler Duplication and Behavioral Divergences

**Priority:** High
**Effort:** Medium

Three handler implementations exist, with documented behavioral divergences:

| Handler | Location | Delegates to `FactoryHandler`? |
|---------|----------|-------------------------------|
| `IntegratedHandler` | `crates/sim-api/src/state.rs` lines 143–280 | **No** — reimplements `TaskEnd`, `OrderCreation`, `MachineAvailabilityChange` inline |
| `HeadlessHandler` | `crates/sim-cli/src/main.rs` lines 131–150 | **Yes** — calls `factory.handle_event()` |
| Test handlers | `crates/sim-api/tests/agent_integration.rs`, `scenario_baselines.rs` | **No** — each file has its own inline copy |

#### Known behavioral divergences

1. **Revenue tracking**: `FactoryHandler.handle_event` passes `current_price: 0.0` for `TaskEnd` (`process.rs` line 254), so `total_revenue` is always zero when run through `HeadlessHandler`. `IntegratedHandler` uses `self.pricing.current_price` (`state.rs` line 172), producing correct revenue. Revenue in the headless CLI path is therefore wrong.
2. **`TaskStart` events**: `FactoryHandler::try_dispatch_from_queue` (`process.rs` lines 73–80) schedules **both** `TaskStart` and `TaskEnd` events. `IntegratedHandler` only schedules `TaskEnd` (state.rs lines 186–193, 212–219, 242–249). The API event log will lack `TaskStart` events.
3. **Queue dispatch on `MachineAvailabilityChange`**: `FactoryHandler::handle_machine_availability` (`process.rs` lines 191–206) calls `try_dispatch_from_queue` when a machine comes online, dequeuing waiting jobs. `IntegratedHandler` (`state.rs` lines 255–260) only calls `set_availability` — it does **not** dispatch queued jobs. This is a production bug: machines brought online via the API leave queued jobs stranded.
4. **Error suppression**: The simulation loop in `spawn_sim_thread` (`state.rs` lines 533, 582, 664, 692) uses `let _ = h.handle_event(...)`, silently discarding handler errors. The runner (`runner.rs` line 64) propagates errors via `?`.

#### Recommended approach

1. Refactor `IntegratedHandler` to **delegate** `TaskEnd`, `OrderCreation`, and `MachineAvailabilityChange` to `FactoryHandler.handle_event()` instead of reimplementing them. Add a method on `FactoryHandler` (e.g., `set_current_price(f64)`) so it uses the real price for revenue rather than the placeholder `0.0`.
2. Add integration tests that verify `IntegratedHandler` and `HeadlessHandler` produce **identical event logs** (including `TaskStart` events and event type counts) for the same scenario.
3. After refactoring, update `crates/sim-api/tests/agent_integration.rs` and `crates/sim-api/tests/scenario_baselines.rs` to use the refactored handler from `sim-api::state` (or a shared test helper) instead of maintaining their own inline copies.
4. Replace `let _ = h.handle_event(...)` in `state.rs` with proper error propagation — log the error and set an error field on the snapshot so API clients can observe simulation failures.
5. Replace `let _ = scheduler.schedule(...)` in `state.rs` with error handling or at minimum logging.

### 2.7 Add Rust Coverage Tooling Locally

**Priority:** Medium
**Effort:** Low

Add to `TESTING.md` and optionally to a Makefile or `justfile`:

```bash
# Generate HTML coverage report
cargo install cargo-tarpaulin
cargo tarpaulin --workspace --out html --output-dir target/coverage
```

Or with `llvm-cov` (more accurate):

```bash
cargo install cargo-llvm-cov
cargo llvm-cov --workspace --html --output-dir target/coverage
```

### 2.8 Improve API Test Infrastructure

**Priority:** Medium
**Effort:** Medium

File: `crates/sim-api/tests/api_smoke.rs`

Current tests use `tower::ServiceExt::oneshot` for in-process testing (good). However:

1. **Missing route coverage**: `/api/sim/pause`, `/api/snapshot`, `/api/jobs`, `/api/agent`, `/api/events/stream` (SSE) are not tested in `api_smoke.rs`
2. **Fixed `sleep`** in `routes.rs` (line 40: `tokio::time::sleep(Duration::from_millis(50))`) makes tests coupled to timing; consider replacing with a mechanism to await snapshot update (e.g., watch the `snapshot_rx` channel)
3. **Invalid TOML test**: `api_smoke.rs` tests malformed JSON but not invalid TOML (e.g., valid JSON wrapping invalid TOML content)

Tests to add to `api_smoke.rs`:
- Load scenario → pause → resume → pause → step → verify single event
- Load scenario → run → query snapshot mid-run
- Load scenario → query jobs returns job list
- Load scenario → toggle agent on/off
- SSE endpoint returns event stream
- Invalid TOML content returns appropriate error
- Trigger a handler error mid-simulation (e.g., scenario referencing invalid machine in a command) and verify error is surfaced in snapshot or API response (tests §2.6 divergence 4)

### 2.9 Documentation Alignment

**Priority:** Low
**Effort:** Low

1. **`TESTING.md`** — update to reflect new test categories (frontend unit tests, coverage commands)
2. **`CONTRIBUTING.md` line 62** — claims `proptest` is used in `sim-factory`; this should be updated to either add property tests (§2.2) or remove the claim
3. **`docs/architecture-overview.md` line 198** — states "integrated into CI" in the E2E testing rationale; Playwright is not in CI. Change `integrated into CI` to `planned for CI`, then update to `integrated into CI` when §2.5.3 is completed
4. **`docs/architecture-overview.md` line 217** — the directory tree lists `sim-material/` without annotation; the layer table (line 94) and paragraph (line 147) already say "(Phase 7)". Add a `(Phase 7)` comment to the directory tree entry at line 217 to match
5. **`ui/README.md`** — contains default Vite template text; replace with Arcogine-specific UI documentation

---

## 3. Priority Matrix

| # | Item | Priority | Effort | Impact |
|---|------|----------|--------|--------|
| 2.1 | Inline Rust unit tests | High | Medium | Catches regressions at the function level |
| 2.2 | `sim-factory` property tests | Medium | Low | Validates structural invariants with random inputs |
| 2.3 | Frontend unit testing infrastructure | High | Medium | Covers 18 untested components and 2 stores |
| 2.4 | E2E test quality improvements | Medium | Low | Reduces flakiness and increases assertion coverage |
| 2.5 | CI pipeline extensions | High | Low | Automates checks already configured but not run |
| 2.6 | Handler duplication refactor + divergence fixes | High | Medium | Eliminates 4 behavioral divergences including a production bug |
| 2.7 | Rust coverage tooling | Medium | Low | Enables data-driven test prioritization |
| 2.8 | API test infrastructure | Medium | Medium | Covers missing routes and reduces timing flakiness |
| 2.9 | Documentation alignment | Low | Low | Prevents developer confusion from stale docs |

---

## 4. Recommended Execution Order

1. **§2.5.1** — Add ESLint to CI (quick win, no dependencies)
2. **§2.3.1** — Install Vitest + Testing Library (unblocks all frontend tests)
3. **§2.5.2** — Add `npm test` to CI (depends on §2.3.1)
4. **§2.1.1** — `sim-types` inline tests (foundational, fast to write)
5. **§2.1.2** — `sim-core` inline tests (event, handler, kpi, log, runner)
6. **§2.6** — Handler duplication refactor (reduces risk before adding more integration tests)
7. **§2.1.3 + §2.2** — `sim-factory` inline + property tests
8. **§2.1.4 + §2.1.5** — `sim-economy` + `sim-agents` inline tests
9. **§2.3.2 + §2.3.3** — Store + API client unit tests
10. **§2.3.4** — Component unit tests
11. **§2.1.6 + §2.1.7** — `sim-api` + `sim-cli` inline tests
12. **§2.8** — API test infrastructure improvements
13. **§2.4** — E2E test quality improvements
14. **§2.5.3** — Playwright in CI (after E2E stabilized)
15. **§2.5.4 + §2.7** — Coverage tooling
16. **§2.9** — Documentation updates

---

## Findings

### F1: `IntegratedHandler` does not dispatch queued jobs after `MachineAvailabilityChange` [Applied]
<!-- severity: critical -->
<!-- dimension: correctness -->

**Context:** §2.6 identifies `IntegratedHandler` duplication but does not call out this specific behavioral divergence. In `crates/sim-api/src/state.rs` lines 255–260, `MachineAvailabilityChange` only calls `set_availability(*online)?` — it does **not** call `try_dispatch_from_queue` afterward. In contrast, `crates/sim-factory/src/process.rs` lines 191–206, `FactoryHandler::handle_machine_availability` calls `set_availability` **and** `try_dispatch_from_queue(machine_id, scheduler, current_time)` when the machine goes online.

**Issue:** When a machine comes back online via the API (`ChangeMachineCount` command), queued jobs waiting on that machine are never dispatched. This is a production bug: the API server (which uses `IntegratedHandler`) behaves differently than the headless CLI runner (which delegates to `FactoryHandler`). The plan's §2.6 description is incomplete — it focuses on revenue tracking divergence but misses this dispatch bug.

**Recommendation:** Update §2.6 to explicitly enumerate all behavioral divergences, including the missing queue dispatch on `MachineAvailabilityChange`. Add a dedicated test in §2.1.6 that verifies machines coming online trigger queued job dispatch.

**Choices:**
- [x] Add this divergence to §2.6 and add a targeted test to §2.1.6
- [ ] File as a separate bug fix outside the testability plan

### F2: `IntegratedHandler` does not emit `TaskStart` events for dispatched jobs [Applied]
<!-- severity: critical -->
<!-- dimension: correctness -->

**Context:** §2.6 describes duplication but does not note this event difference. `FactoryHandler::try_dispatch_from_queue` (`process.rs` lines 73–89) schedules **both** `TaskStart` and `TaskEnd` events. `IntegratedHandler` (state.rs lines 186–193 and elsewhere) only schedules `TaskEnd` events for dispatched jobs — no `TaskStart` is emitted.

**Issue:** The event log produced by the API server path will lack `TaskStart` events, affecting `ThroughputRate` KPI calculation (which counts `TaskEnd` events) less directly, but it means the event stream differs between API and headless runs. Any future test or feature relying on `TaskStart` events (e.g., machine utilization, event log completeness assertions) will see different behavior depending on which handler path is used. This is an undocumented divergence.

**Recommendation:** Document the `TaskStart` omission in §2.6 alongside the other divergences. The integration test proposed in §2.6 bullet 3 ("verify `IntegratedHandler` and `HeadlessHandler` produce identical results") will catch this, but only if the test compares full event logs including event types.

**Choices:**
- [x] Add to §2.6 divergence list and ensure the proposed integration test compares full event logs by type
- [ ] Treat as acceptable simplification and document the difference

### F3: Plan §2.1.4 incorrectly claims `DemandModel.handle_event` reschedules [Applied]
<!-- severity: major -->
<!-- dimension: correctness -->

**Context:** §2.1.4 proposes: "handle_event for DemandEvaluation calls generate_orders and reschedules". Looking at `crates/sim-economy/src/demand.rs` lines 99–108, `DemandModel::handle_event` only calls `generate_orders` (for `DemandEvaluation`) and `set_price` (for `PriceChange`). Rescheduling of periodic `DemandEvaluation` events is performed by the **runner** (`crates/sim-core/src/runner.rs` lines 68–73) and by `reschedule_periodic` in `state.rs` (lines 747–769).

**Issue:** A coding agent following this plan would write a test asserting that `DemandModel::handle_event` schedules a follow-up `DemandEvaluation`, which would fail. The test description is factually wrong.

**Recommendation:** Correct the test description to: "handle_event for DemandEvaluation calls generate_orders and schedules OrderCreation events" and "handle_event for PriceChange updates current_price".

**Choices:**
- [x] Correct the test descriptions in §2.1.4 to match actual behavior
- [ ] Move the rescheduling test to §2.1.2 under runner tests

### F4: Plan §2.1.3 describes `throughput()` inaccurately [Applied]
<!-- severity: major -->
<!-- dimension: correctness -->

**Context:** §2.1.3 proposes: "throughput() returns completed sales count". The actual signature is `pub fn throughput(&self, elapsed_ticks: u64) -> f64` (`crates/sim-factory/src/process.rs` line 224), which computes `completed_sales as f64 / elapsed_ticks as f64`. It returns a *rate*, not a count, and requires an `elapsed_ticks` argument.

**Issue:** A coding agent would write a test that calls `throughput()` with no argument and asserts it equals a count. This will not compile.

**Recommendation:** Correct to: "throughput(elapsed_ticks) returns completed_sales / elapsed_ticks and returns 0.0 when elapsed_ticks is 0"

**Choices:**
- [x] Fix the test description to match the actual signature and behavior
- [ ] Simplify to just referencing the function signature

### F5: `IntegratedHandler` suppresses errors from `handle_event` in simulation loop [Applied]
<!-- severity: major -->
<!-- dimension: gaps -->

**Context:** §2.6 bullet 3 mentions "`state.rs` silently discards `handle_event` errors with `let _ = ...`" but the plan does not propose a specific test to detect this. In `crates/sim-api/src/state.rs`, lines 533, 582, 664, and 692 all use `let _ = h.handle_event(&event, &mut scheduler);` — discarding `Result<(), SimError>`. Additionally, lines 492, 500, 634, 642, 759, 765 suppress scheduler errors.

**Issue:** If a simulation handler encounters an error (e.g., unknown machine ID, invalid state transition), the simulation continues in an inconsistent state. No error is reported to the API client. The plan identifies this problem but does not include specific test items to verify error propagation.

**Recommendation:** Add to §2.1.6 (or §2.8): a test that triggers a handler error mid-simulation (e.g., referencing a nonexistent machine) and verifies the error surfaces in the snapshot or API response rather than being silently consumed.

**Choices:**
- [x] Add error-propagation tests to §2.1.6 and a recommendation to change `let _ =` to proper error handling
- [ ] Defer error handling changes to a separate refactoring plan

### F6: `HeadlessHandler` dispatch order differs from `IntegratedHandler` [Applied]
<!-- severity: major -->
<!-- dimension: correctness -->

**Context:** §2.1.7 proposes: "HeadlessHandler dispatch order matches IntegratedHandler for pricing → demand → factory". Looking at the actual code: `HeadlessHandler` (`crates/sim-cli/src/main.rs` lines 137–149) calls `pricing.handle_event → demand.handle_event → factory.handle_event`. `IntegratedHandler` (`crates/sim-api/src/state.rs` lines 153–279) calls `pricing → demand → inline TaskEnd/OrderCreation/MachineAvailabilityChange/AgentEvaluation` — it does **not** delegate to `factory.handle_event()` for these event types. It accesses `factory.*` fields directly.

**Issue:** The plan claims dispatch orders match, but they are structurally different. `HeadlessHandler` delegates TaskEnd/OrderCreation to `FactoryHandler` (which uses `current_price: 0.0`), while `IntegratedHandler` reimplements that logic inline (using `self.pricing.current_price`). These produce different revenue numbers and different event schedules (TaskStart present vs absent). The plan should describe this accurately.

**Recommendation:** Rewrite §2.1.7 test to: "HeadlessHandler delegates to FactoryHandler.handle_event, producing different revenue and event-log behavior than IntegratedHandler — the proposed §2.6 refactor should unify them. Until then, add a test documenting the divergence."

**Choices:**
- [x] Correct §2.1.7 to accurately describe the divergence and reference §2.6 as the fix
- [ ] Remove the test since it would fail by design

### F7: Plan §2.9 item 4 — `sim-material` already has "(Phase 7)" in most references [Applied]
<!-- severity: minor -->
<!-- dimension: plan-hygiene -->

**Context:** §2.9 item 4 says: "`docs/architecture-overview.md` line 217 — lists `sim-material/` as a crate directory; this crate does not exist yet. Add '(Phase 7)' note." The architecture doc already annotates `sim-material` as "(Phase 7)" in the layer table (line 94) and in the paragraph at line 147. Only the directory listing at line 217 omits it.

**Issue:** The recommendation is slightly imprecise — it sounds like "(Phase 7)" is missing everywhere, when it's only missing in the directory tree diagram.

**Recommendation:** Narrow the recommendation to: "Add '(Phase 7)' comment to the `sim-material/` entry in the directory tree at line 217 of `architecture-overview.md`, matching the annotation already present in the layer table at line 94."

**Choices:**
- [x] Narrow the recommendation to the specific directory tree line
- [ ] Leave as-is since the intent is clear enough

### F8: Plan §2.9 item 3 line reference is correct but wording is imprecise [Applied]
<!-- severity: minor -->
<!-- dimension: plan-hygiene -->

**Context:** §2.9 item 3 says: "`docs/architecture-overview.md` line 198 — states 'Playwright, integrated into CI'". The actual text at line 198 is: `| E2E testing | Playwright | Browser automation for smoke tests, integrated into CI |`. The plan says to "Update after §2.5.3 or add a 'Planned' note."

**Issue:** The recommendation is fine but should specify exactly what to change: the word "integrated" should become "planned" in the rationale column until CI is actually set up.

**Recommendation:** Specify: change `integrated into CI` to `planned for CI` at `docs/architecture-overview.md` line 198, then update to `integrated into CI` when §2.5.3 is completed.

**Choices:**
- [x] Be explicit about the text change needed
- [ ] Leave as-is

### F9: Missing test for `run_scenario` with `agent.enabled = true` but no `agent` config section [Applied]
<!-- severity: minor -->
<!-- dimension: testing -->

**Context:** §2.1.2 proposes: "run_scenario seeds AgentEvaluation only when agent config present". In `crates/sim-core/src/runner.rs` lines 43–54, the seeding logic checks `agent_interval > 0 && config.agent.is_some() && config.agent.unwrap().enabled`. But the default `agent_eval_interval` (from `scenario.rs`) is non-zero while `config.agent` can be `None`.

**Issue:** The proposed test description is correct but could also test the case where `agent_eval_interval > 0` but `config.agent` is `None` (should not seed). This edge case is important for scenario files that omit the `[agent]` section.

**Recommendation:** Add a specific test case: "run_scenario with non-zero agent_eval_interval but no [agent] config section does not seed AgentEvaluation"

**Choices:**
- [x] Add the edge case to the §2.1.2 test list
- [ ] Covered implicitly by existing description

### F10: Plan §2.3.1 Vitest config should import from `vitest/config` [Applied]
<!-- severity: minor -->
<!-- dimension: best-practices -->

**Context:** §2.3.1 shows adding a `test` block directly inside `defineConfig` from `vite`. With Vitest, the recommended pattern is to use `defineConfig` from `vitest/config` or to use `/// <reference types="vitest/config" />` at the top of the file so TypeScript recognizes the `test` property.

**Issue:** Without the Vitest config import, TypeScript will report a type error on the `test` property since Vite's `defineConfig` doesn't include it.

**Recommendation:** Update the code snippet to use `import { defineConfig } from 'vitest/config'` or add the triple-slash reference directive. Since the existing config uses `import { defineConfig } from 'vite'`, the cleanest approach is to use `/// <reference types="vitest/config" />` to extend the type.

**Choices:**
- [x] Add `/// <reference types="vitest/config" />` directive to the code snippet
- [ ] Switch the import to `vitest/config`

### F11: No mention of testing the SSE client reconnect backoff against the actual implementation [Applied]
<!-- severity: minor -->
<!-- dimension: gaps -->

**Context:** §2.3.3 proposes testing `sse.ts` reconnect delay doubling. The `SseClient` in `ui/src/api/sse.ts` implements exponential backoff with a cap. The test list correctly identifies the behavior to test but does not note that `EventSource` is a browser API that requires mocking in `jsdom` (which doesn't implement `EventSource` natively).

**Issue:** The test will fail unless the plan specifies that `EventSource` must be mocked or polyfilled. This is a blocker for implementation.

**Recommendation:** Add a note to §2.3.3: "EventSource is not available in jsdom; mock it using a custom class or use the `eventsource` npm package as a polyfill in the test setup."

**Choices:**
- [x] Add a note about EventSource mocking requirement
- [ ] Defer SSE testing to E2E only

### F12: §2.5.2 depends on §2.3.1 but execution order §4 lists §2.5.1 + §2.5.2 first [Applied]
<!-- severity: major -->
<!-- dimension: plan-hygiene -->

**Context:** §4 Recommended Execution Order step 1 is "§2.5.1 + §2.5.2 — Add ESLint and unit test steps to CI." But §2.5.2 says "After installing Vitest (§2.3.1)" — the CI step depends on Vitest being installed first, which is step 2 in the execution order.

**Issue:** If a coding agent follows the execution order literally, it will add `npm test` to CI before Vitest is installed, causing CI to fail on the missing script.

**Recommendation:** Split step 1: add ESLint to CI immediately (§2.5.1), but defer adding `npm test` (§2.5.2) until after §2.3.1 installs Vitest. Reorder step 1 to be only §2.5.1, and move §2.5.2 to after step 2.

**Choices:**
- [x] Reorder: step 1 = §2.5.1 only; step 2 = §2.3.1; step 3 = §2.5.2
- [ ] Merge §2.3.1 and §2.5.2 into one step

### F13: Plan does not address `sim-api/tests/agent_integration.rs` duplicating `IntegratedHandler` [Applied]
<!-- severity: major -->
<!-- dimension: testing -->

**Context:** `crates/sim-api/tests/agent_integration.rs` (503 lines) contains its own inline `IntegratedHandler` that duplicates the one in `state.rs`. If the production handler is refactored per §2.6, this test handler must also be updated. The plan does not mention this test file's duplication or the need to keep it in sync.

**Issue:** After the §2.6 refactor, `agent_integration.rs` will still use the old inlined handler, silently testing stale behavior. Similarly, `scenario_baselines.rs` (407 lines) also defines its own `IntegratedHandler` variant.

**Recommendation:** Add to §2.6: "After refactoring, update `crates/sim-api/tests/agent_integration.rs` and `crates/sim-api/tests/scenario_baselines.rs` to use the refactored handler from `sim-api::state` (or a shared test helper) instead of maintaining their own copies."

**Choices:**
- [x] Add test file updates to §2.6 scope
- [ ] Create a separate test utilities crate for shared handler construction

### F14: `FactoryHandler` is tested indirectly but §1.4 item 5 phrasing is misleading [Applied]
<!-- severity: minor -->
<!-- dimension: plan-hygiene -->

**Context:** §1.4 item 5 says: "`FactoryHandler` (`process.rs`) untested directly — only covered through `sim-api` integration tests". However, `crates/sim-api/tests/scenario_baselines.rs` and `agent_integration.rs` use their own `IntegratedHandler` which reimplements factory logic inline — they do not actually exercise `FactoryHandler.handle_event()`. The CLI's `HeadlessHandler` is the only path that actually delegates to `FactoryHandler.handle_event()`, and it has no tests.

**Issue:** The statement "covered through sim-api integration tests" is misleading. Those tests cover reimplemented logic, not the actual `FactoryHandler`.

**Recommendation:** Reword to: "`FactoryHandler.handle_event()` (`process.rs`) is not exercised by any test. The `sim-api` integration tests use an inline reimplementation of factory logic that diverges from `FactoryHandler` (see §2.6). The only caller of `FactoryHandler.handle_event()` is the untested `HeadlessHandler` in `sim-cli`."

**Choices:**
- [x] Reword §1.4 item 5 to accurately reflect the test coverage situation
- [ ] Leave as-is with a footnote

### Summary

| # | Title | Severity | Dimension | Depends on |
|---|-------|----------|-----------|------------|
| F1 | IntegratedHandler missing queue dispatch on MachineAvailabilityChange | critical | correctness | — |
| F2 | IntegratedHandler omits TaskStart events | critical | correctness | — |
| F3 | Plan claims DemandModel.handle_event reschedules | major | correctness | — |
| F4 | Plan describes throughput() inaccurately | major | correctness | — |
| F5 | No test proposed for error suppression in simulation loop | major | gaps | — |
| F6 | HeadlessHandler dispatch order claim is incorrect | major | correctness | — |
| F7 | sim-material Phase 7 note recommendation is imprecise | minor | plan-hygiene | — |
| F8 | Wording imprecise for architecture-overview CI reference | minor | plan-hygiene | — |
| F9 | Missing edge-case test for agent seeding | minor | testing | — |
| F10 | Vitest config snippet missing type reference | minor | best-practices | — |
| F11 | No mention of EventSource mocking requirement | minor | gaps | — |
| F12 | Execution order has dependency violation (§2.5.2 before §2.3.1) | major | plan-hygiene | — |
| F13 | Test files duplicating IntegratedHandler not addressed in refactor scope | major | testing | F1 |
| F14 | §1.4 item 5 misleadingly says FactoryHandler is covered by sim-api tests | minor | plan-hygiene | F1 |

### Iteration 2

Full re-sweep across all five dimensions after applying F1–F14. Verified:

- **Testing:** All proposed test descriptions match actual function signatures, return types, and behavior. No new testing gaps found.
- **Correctness:** All line references verified against source files. §2.6 divergence table entries confirmed at `state.rs:144–280`, `process.rs:73–89,191–206,246–254`, `main.rs:131–150`. Cross-references between §2.1.6, §2.1.7, §2.6, and §2.8 are consistent.
- **Gaps:** Error propagation is now covered in §2.1.6, §2.6 bullet 4, and §2.8. EventSource mocking addressed. No new gaps found.
- **Best practices:** Vitest config pattern corrected. CI workflow snippets follow project conventions (Node 20, actions/checkout@v4, etc.).
- **Plan hygiene:** Execution order dependencies resolved (§2.5.1 → §2.3.1 → §2.5.2). Priority matrix updated to reflect §2.6 scope. All section cross-references verified consistent.

**Result: Zero critical or major findings. Iteration complete.**
