# Rust-Based Factory Economy Simulation — MVP Plan

> **Date:** 2026-04-02
> **Scope:** Define and implement a GitHub-ready MVP for a single-user, locally runnable factory-and-economy simulation platform in Rust, with minimal UI, API-driven experimentation, deterministic testing, and containerized local deployment.
> **Primary sources:** `README.md:2` (project description), `LICENSE` (Apache 2.0), `devel/Original-plan.md` (prior planning notes)

---

## 1. Goal

- Prove that a closed-loop factory + economy simulation can run deterministically in Rust with reproducible outcomes.
- Define the minimum architecture, testing strategy, local deployment model, and UI needed for single-user experimentation.
- Structure the work so it can be published on GitHub as a reproducible, contributor-friendly open project.
- Preserve the long-term path toward digital-twin, serious-game, multi-agent, and MMO-adjacent extensions without overbuilding the MVP.

---

## 2. Non-Negotiable Constraints

1. The core simulation must be written in Rust. (`README.md:2` — "deterministic simulation engine")
2. A headless simulation core comes first; visualization and game-like layers are additive. (Architectural decision from planning conversation)
3. The MVP must model a factory flow linked to an economy loop — production simulation alone is insufficient. (Planning conversation)
4. The repository must be GitHub-friendly: modular, testable, reproducible, and suitable for public collaboration. (Planning conversation)
5. A minimal single-user UI acts as an experiment console, not a game client. (Planning conversation)
6. Local execution must support both native development and containerized multi-service runs. (Planning conversation)
7. The system must include deterministic testing with explicit acceptance criteria and scenario-level validation. (Planning conversation)
8. Agents interact through approved command interfaces only — no direct mutation of simulation state. (Planning conversation)

---

## 3. Verified Current State

### 3.1 Repository baseline

The repository contains three files: `README.md` (3 lines, project description at `README.md:2`), `LICENSE` (Apache 2.0), and `devel/Original-plan.md` (planning notes). There is no `Cargo.toml`, no source code, no tests, no CI workflows, no Docker files, and no `.gitignore`. The project is entirely greenfield.

### 3.2 Architectural direction

Planning notes in `devel/Original-plan.md` converge on a Cargo workspace with crates `sim-core`, `sim-factory`, `sim-economy`, `sim-agents`, `sim-types`, `sim-cli`, and `sim-api`. The design uses discrete-event simulation, typed IDs, packed state, event logging, and API-driven control. None of this is implemented yet.

### 3.3 Deployment and collaboration expectations

The plan calls for GitHub-readiness (README, CONTRIBUTING, ARCHITECTURE docs), CI via GitHub Actions, Docker Compose for local multi-service runs, and benchmark scaffolding. None of these exist in the repository.

### 3.4 MVP product definition

The MVP target is a headless Rust simulation that models a simple factory (2–3 machine types, 2–3 SKUs), links pricing to demand via a minimal economy model, allows an agent to adjust price via API, and produces explainable economic and operational outcomes. A thin web UI serves as the experiment console.

---

## 4. Recommended Approach

(Recommended) Build a Cargo workspace around a headless discrete-event simulation core, add a thin REST API and minimal web UI for single-user experimentation, and defer advanced game/MMO/twin integrations until the core loop is validated through deterministic scenario tests.

Rationale:
- Satisfies the simulation-first architectural constraint (§2.2) by making the engine independent of any UI or network layer.
- Proves the central hypothesis — pricing and capacity decisions affect demand, backlog, lead time, and revenue — in an observable, reproducible way.
- Preserves extensibility toward multi-agent control and distributed scaling without forcing those concerns into the MVP.
- Fits public GitHub development by keeping the core modular, testable, and reproducible before adding deployment complexity.
- The phased structure lets each phase's tests pass independently of later phases (template rule).

---

## 5. Phased Plan

### Phase 1. Establish the public repository foundation

Objective: Create a GitHub-ready project skeleton that is reproducible, modular, and ready for contribution before simulation logic is added.

Planned work:

1. Create a Cargo workspace root `Cargo.toml` and crate directories for `sim-core`, `sim-factory`, `sim-economy`, `sim-agents`, `sim-types`, `sim-cli`, and `sim-api`. Each crate gets a minimal `Cargo.toml` and `src/lib.rs` (or `src/main.rs` for binaries). Include `serde` (with `derive` feature) and `toml` as dependencies in `sim-core` and `sim-types` for scenario serialization. Include `rand` and `rand_chacha` in `sim-core` for deterministic RNG. No existing files to modify. [F6, F7 applied]
2. Add repository health files: `README.md` (expand from current 3-line stub at `README.md:1-3`), `ARCHITECTURE.md` (must document the determinism contract: ChaCha8Rng, seed propagation, and replay guarantees), `CONTRIBUTING.md`, `SECURITY.md`, `CODE_OF_CONDUCT.md`, and `.gitignore`. `LICENSE` already exists and needs no changes. [F7 applied]
3. Add baseline CI in `.github/workflows/ci.yml` for `cargo fmt --check`, `cargo clippy`, and `cargo test`.
4. Add `examples/` and `docs/` directories with placeholder files explaining their intended contents.

Files expected:
- `Cargo.toml` (new — workspace root)
- `crates/sim-core/Cargo.toml` (new — includes `serde`, `toml`, `rand`, `rand_chacha`), `crates/sim-core/src/lib.rs`
- `crates/sim-factory/Cargo.toml`, `crates/sim-factory/src/lib.rs`
- `crates/sim-economy/Cargo.toml`, `crates/sim-economy/src/lib.rs`
- `crates/sim-agents/Cargo.toml`, `crates/sim-agents/src/lib.rs`
- `crates/sim-types/Cargo.toml` (new — includes `serde`), `crates/sim-types/src/lib.rs`
- `crates/sim-cli/Cargo.toml`, `crates/sim-cli/src/main.rs`
- `crates/sim-api/Cargo.toml`, `crates/sim-api/src/lib.rs`
- `README.md` (modify existing `README.md:1-3`)
- `ARCHITECTURE.md` (new — includes determinism contract), `CONTRIBUTING.md`, `SECURITY.md`, `CODE_OF_CONDUCT.md` (new)
- `.gitignore` (new)
- `.github/workflows/ci.yml` (new)

Acceptance criteria:
- A new contributor can clone the repo and understand purpose, layout, contribution flow, and local run path from the docs alone.
- `cargo fmt --check`, `cargo clippy`, and `cargo test` pass in CI with zero errors (tests may be empty but must compile).
- The crate structure clearly separates simulation core, types, factory logic, economy logic, agent logic, API surface, and CLI entrypoint.

---

### Phase 2. Build the deterministic simulation kernel

Objective: Implement the smallest useful discrete-event simulation engine and core typed state needed to run factory scenarios reproducibly.

Planned work:

1. Define typed IDs (`MachineId`, `ProductId`, `JobId`), simulation time (`SimTime`), shared enums/structs, and a shared error enum `SimError` (with variants for invalid state transitions, unknown IDs, event ordering violations) in `crates/sim-types/src/lib.rs`. [F4 applied]
2. Implement event types (order creation, task start, task end, machine availability change, price change, agent decision), a priority-queue-based event scheduler, and deterministic event dispatch in `crates/sim-core/src/event.rs` and `crates/sim-core/src/queue.rs`. Use `ChaCha8Rng` from `rand_chacha` seeded from the scenario configuration for all stochastic decisions. [F7 applied]
3. Implement append-only event logging in `crates/sim-core/src/log.rs`. Define a `Kpi` trait in `crates/sim-core/src/kpi.rs` and implement at least one concrete KPI (e.g., `TotalSimulatedTime` or `EventCount`) sufficient to validate deterministic replay. [F2 applied]
4. Define the scenario file schema (machine definitions, product routings, initial conditions, run parameters, RNG seed) in TOML and implement a scenario loader in `crates/sim-core/src/scenario.rs`. [F1, F7 applied]
5. Define state stores for machines, products, jobs, and work queues using data-oriented structures in `crates/sim-factory/src/machines.rs`, `crates/sim-factory/src/jobs.rs`, and `crates/sim-factory/src/routing.rs`.
6. Write unit tests for event ordering, monotonic time progression, state transition safety, deterministic replay, and scenario loading in `crates/sim-core/tests/`.
7. Write property tests in `crates/sim-core/tests/properties.rs` using `proptest` or `quickcheck` to verify invariants: no negative inventory, no duplicate job completion, monotonic time progression, and event causality consistency.

Files expected:
- `crates/sim-types/src/lib.rs` (modify from Phase 1 stub — adds typed IDs, `SimError`)
- `crates/sim-core/src/lib.rs` (modify), `crates/sim-core/src/event.rs`, `crates/sim-core/src/queue.rs`, `crates/sim-core/src/log.rs`, `crates/sim-core/src/kpi.rs`, `crates/sim-core/src/scenario.rs` (new)
- `crates/sim-factory/src/lib.rs` (modify), `crates/sim-factory/src/machines.rs`, `crates/sim-factory/src/jobs.rs`, `crates/sim-factory/src/routing.rs` (new)
- `crates/sim-core/tests/determinism.rs`, `crates/sim-core/tests/event_ordering.rs`, `crates/sim-core/tests/scenario_loading.rs`, `crates/sim-core/tests/properties.rs` (new)

Acceptance criteria:
- Running a fixed scenario file with a fixed seed produces identical final state, KPIs (at minimum `TotalSimulatedTime` or `EventCount`), and event stream across repeated runs.
- Jobs advance through routing steps in correct order; machines never process more than one active task concurrently unless explicitly configured for parallel processing.
- Event times are processed in non-decreasing order; invalid state transitions are rejected with `SimError` variants.
- A TOML scenario file can be loaded, validated, and used to initialize a simulation run.
- Property tests pass: no negative inventory, no duplicate job completion, monotonic time progression.

---

### Phase 3. Add the minimal factory flow and economy loop

Objective: Prove the closed-loop relationship between factory capacity, lead time, pricing, demand, and revenue.

Planned work:

1. Implement a product/routing model supporting 2–3 machine types and 2–3 SKUs in `crates/sim-factory/src/products.rs` and `crates/sim-factory/src/process.rs`.
2. Implement a demand model driven by price and delivery performance in `crates/sim-economy/src/demand.rs`.
3. Implement a pricing module in `crates/sim-economy/src/pricing.rs` that holds the current price and exposes it to the demand model.
4. Add revenue, backlog, throughput, lead-time, and utilization KPI computations to `crates/sim-core/src/kpi.rs`.
5. Create scenario fixture files in `examples/` for baseline, overload, and capacity-expansion runs using the TOML schema defined in Phase 2.
6. Write scenario acceptance tests in `crates/sim-core/tests/scenario_baselines.rs` that validate behavioral outcomes.

Files expected:
- `crates/sim-factory/src/products.rs`, `crates/sim-factory/src/process.rs` (new)
- `crates/sim-factory/src/lib.rs` (modify to re-export new modules)
- `crates/sim-economy/src/lib.rs` (modify from Phase 1 stub), `crates/sim-economy/src/demand.rs`, `crates/sim-economy/src/pricing.rs` (new)
- `crates/sim-core/src/kpi.rs` (modify from Phase 2)
- `examples/basic_scenario.toml`, `examples/overload_scenario.toml`, `examples/capacity_expansion_scenario.toml` (new)
- `crates/sim-core/tests/scenario_baselines.rs` (new)

Acceptance criteria:
- Lowering price increases demand and creates observable backlog under constrained capacity.
- Raising price reduces load under otherwise identical conditions.
- A bottleneck machine produces measurable queue buildup and longer average lead time than the theoretical no-wait baseline.
- Completed production generates revenue exactly once per sale event.

---

### Phase 4. Add the command/query surface and simple agent

Objective: Allow controlled external influence over the simulation through explicit APIs and validate that an agent can improve or stabilize outcomes.

Planned work:

1. Implement a command/query interface in `crates/sim-api/src/routes.rs` and `crates/sim-api/src/server.rs` using Axum + Tokio, supporting: load scenario, step/run sim, change price, change machine count, toggle agent, query KPIs, query event log. [F3 applied]
2. Wire `crates/sim-cli/src/main.rs` as a thin CLI that drives the same command interface for headless use.
3. Implement a `SalesAgent` in `crates/sim-agents/src/sales_agent.rs` that observes backlog, lead time, and revenue, then adjusts price using approved commands. The agent architecture must support future agent types (Planning, Procurement, Maintenance) and future LLM-based strategy agents, so the interface should be trait-based and agent-type-agnostic.
4. Ensure all commands are validated, logged in the event log, and replayable. Modify `crates/sim-core/src/log.rs` and `crates/sim-core/src/event.rs` as needed.
5. Write integration tests in `crates/sim-api/tests/api_smoke.rs` and `crates/sim-core/tests/agent_integration.rs`.

Files expected:
- `crates/sim-api/src/lib.rs` (modify), `crates/sim-api/src/server.rs`, `crates/sim-api/src/routes.rs` (new)
- `crates/sim-cli/src/main.rs` (modify from Phase 1 stub)
- `crates/sim-agents/src/lib.rs` (modify from Phase 1 stub), `crates/sim-agents/src/sales_agent.rs` (new)
- `crates/sim-core/src/log.rs`, `crates/sim-core/src/event.rs` (modify from Phase 2)
- `crates/sim-api/tests/api_smoke.rs`, `crates/sim-core/tests/agent_integration.rs` (new)

Acceptance criteria:
- A user can load a scenario, run it, change price, and query updated KPIs entirely through the REST API.
- The CLI can execute the same workflow headlessly and produce identical results.
- The agent observes state and adjusts price without direct mutable access to simulation internals.
- Under an overload scenario, the agent produces at least one logged intervention and measurably reduces backlog growth or improves a target KPI relative to a fixed-price baseline.
- Invalid commands are rejected with typed errors and do not corrupt simulation state.

---

### Phase 5. Add the minimal single-user experimentation UI

Objective: Provide a lightweight local dashboard that makes experiments visible, comparable, and explainable.

Planned work:

1. Scaffold a TypeScript/React project in `ui/` with `package.json`, `tsconfig.json`, and a bundler (Vite).
2. Build controls for scenario selection, run/pause/reset/step, price slider, machine-count adjustment, and agent toggle in `ui/src/components/Controls.tsx`.
3. Build KPI cards (`ui/src/components/KpiCards.tsx`), a time-series chart (`ui/src/components/Charts.tsx`), a machine-status table (`ui/src/components/MachineTable.tsx`), and an event-log panel (`ui/src/components/EventLog.tsx`).
4. Build a baseline comparison view (`ui/src/components/BaselineCompare.tsx`) showing current-vs-saved deltas for revenue, backlog, lead time, and throughput.
5. Wire all UI interactions to the REST API from Phase 4 — no direct state coupling.
6. Add Playwright e2e smoke tests that verify the UI can load a scenario, display KPI data, and reflect a lever change. Add the Playwright test runner to CI. [F5 applied]

Files expected:
- `ui/package.json`, `ui/tsconfig.json`, `ui/vite.config.ts`, `ui/index.html` (new)
- `ui/src/main.tsx`, `ui/src/App.tsx` (new)
- `ui/src/components/Controls.tsx`, `ui/src/components/KpiCards.tsx`, `ui/src/components/MachineTable.tsx`, `ui/src/components/EventLog.tsx`, `ui/src/components/Charts.tsx`, `ui/src/components/BaselineCompare.tsx` (new)
- `ui/src/api/client.ts` (new — API client wrapper)
- `ui/e2e/smoke.spec.ts` (new — Playwright tests)
- `ui/playwright.config.ts` (new)

Acceptance criteria:
- A single user can load a built-in scenario, change a lever, run the simulation, and see KPI changes reflected in real time.
- The UI makes bottlenecks visible through queue length, utilization, or lead-time indicators.
- A user can inspect the event stream and trace why a KPI changed.
- Baseline-versus-current comparison is visible for at least revenue, backlog, lead time, and throughput.
- Playwright e2e smoke tests pass in CI, verifying scenario load and KPI display.

---

### Phase 6. Add reproducible local deployment and performance validation

Objective: Make the MVP easy to run locally, easy to demo, and measurable under repeatable conditions.

Planned work:

1. Add `Dockerfile` (Rust API) and `ui/Dockerfile` (UI) with multi-stage builds.
2. Add `compose.yaml` orchestrating API and UI services (Postgres deferred to post-MVP unless needed for event persistence).
3. Add `.dockerignore`, `.env.example`, and expand `README.md` with local run instructions for both native and containerized paths.
4. Add benchmark scaffolding in `benches/scheduler.rs` and `benches/scenario_runtime.rs` using Criterion.
5. Add `TESTING.md` documenting how to run unit, integration, scenario, and benchmark test suites.

Files expected:
- `Dockerfile`, `ui/Dockerfile` (new)
- `compose.yaml` (new)
- `.dockerignore`, `.env.example` (new)
- `README.md` (modify from Phase 1)
- `benches/scheduler.rs`, `benches/scenario_runtime.rs` (new)
- `TESTING.md` (new)

Acceptance criteria:
- A new contributor can run the full stack with `cargo run` (native) or `docker compose up --build` (containerized) by following documented instructions.
- Benchmarks produce repeatable baseline numbers for core event processing throughput and full scenario execution time.
- `TESTING.md` clearly documents all test categories, how to run each, and what success looks like.

---

## 6. Validation Plan

1. Clone the repository into a clean environment and follow `README.md` to run the native development path.
2. Run `cargo fmt --check`, `cargo clippy`, `cargo test` and verify zero failures.
3. Load the baseline scenario via CLI and confirm that repeated runs with the same seed produce byte-identical output.
4. Run the overload scenario at low price and verify that backlog and lead time rise relative to the baseline scenario.
5. Increase price through the REST API and verify that demand falls and backlog pressure eases.
6. Enable the `SalesAgent`, rerun the overload scenario, and confirm that at least one agent intervention is logged and the target KPI improves relative to the fixed-price control.
7. Open the UI, load a scenario, adjust a lever, and confirm the KPI dashboard reflects the change consistently with the event log.
8. Compare a baseline run versus a modified run in the UI and confirm deltas are visible for revenue, backlog, lead time, and throughput.
9. Run the containerized stack via `docker compose up --build` and confirm the same scenario produces consistent KPI behavior.
10. Run benchmarks via `cargo bench` and record baseline throughput/latency numbers.
11. Review the event log for a sample run and confirm pricing actions, order creation, job completion, and revenue events form a coherent causal chain.

---

## 7. Implementation Order

1. **Phase 1 — Establish the public repository foundation.** GitHub-readiness, crate boundaries, docs, and CI must shape all subsequent work. No simulation logic depends on this phase's acceptance criteria.
2. **Phase 2 — Build the deterministic simulation kernel.** The engine must exist before factory, economy, agent, or UI layers can be tested. Phase 1 acceptance criteria remain passing.
3. **Phase 3 — Add the minimal factory flow and economy loop.** First phase that proves the product hypothesis: operations and economics interact meaningfully. Phases 1–2 acceptance criteria remain passing.
4. **Phase 4 — Add the command/query surface and simple agent.** The API and agent layer on top of a proven closed-loop simulation. Phases 1–3 acceptance criteria remain passing.
5. **Phase 5 — Add the minimal single-user experimentation UI.** The UI exposes already-working simulation behavior. Phases 1–4 acceptance criteria remain passing.
6. **Phase 6 — Add reproducible local deployment and performance validation.** Containerization and benchmarks formalize what is already usable. All prior phase acceptance criteria remain passing.

---

## 8. Out of Scope (MVP)

- Full multiplayer or MMO infrastructure
- Distributed simulation shards
- Partitioned simulation (per factory/region) with shared economy layer
- Real ERP/MES/CRM digital-twin integrations
- Photoreal 3D visualization
- Complex workforce movement or floor-layout simulation
- Large-scale optimization engines
- LLM-driven autonomous control loops
- Multi-agent negotiation systems
- Production-grade authentication/authorization
- Full plugin marketplace or modding system
- Enterprise deployment orchestration beyond local/containerized MVP use
- Postgres-backed event persistence (deferred to post-MVP evaluation)
- Advanced scheduling and optimization algorithms
- Scenario authoring tools
- Real-time analytics dashboards beyond MVP experiment console

---

## 9. Future Directions (Post-MVP)

These capabilities build on the MVP foundation and are preserved as long-term directions:

1. **Digital twin integration** — Connect to real ERP/MES/CRM systems for live factory modeling.
2. **Serious games** — Management training scenarios using the simulation as a game backend.
3. **Multi-agent decision environments** — Multiple agents (Sales, Planning, Procurement, Maintenance) negotiating and competing within the same simulation.
4. **LLM-based agents** — Strategy-level agents powered by language models for decision-making research.
5. **MMO-scale economic simulations** — Partitioned simulation (per factory/region) with a shared economy layer, strong consistency for financial state, and event-driven inter-system communication.
6. **3D visualization layer** — Optional rendering for spatial factory layouts.
7. **Advanced scheduling and optimization** — Constraint-based or heuristic scheduling algorithms.
8. **Scenario authoring tools** — UI or DSL for creating and sharing simulation scenarios.
9. **Real-time analytics and dashboards** — Production-grade monitoring beyond the MVP experiment console.

---

## Findings

### F1: No scenario fixture format specified [Applied]
<!-- severity: major -->
<!-- dimension: gaps -->

**Context:** Phase 3 tasks 5–6 reference scenario fixtures in `examples/` (e.g., `basic_scenario.toml`) and scenario acceptance tests that load them, but no phase defines the schema, serialization format, or loading mechanism for scenario files.

**Issue:** Without a defined scenario format and a loader, Phase 3 acceptance tests cannot load fixtures. The format choice (TOML vs JSON vs RON) also affects the `serde` dependencies declared in Phase 1's `Cargo.toml` files.

**Recommendation:** Add a task in Phase 2 to define the scenario file schema (machine definitions, product routings, initial conditions, run parameters) and implement a loader in `sim-core`. Choose TOML for human readability and Rust ecosystem alignment.

**Choices:**
- [x] Add scenario schema definition and TOML loader to Phase 2
- [ ] Defer format choice to Phase 3 and accept that Phase 2 tests use hardcoded fixtures only
- [ ] Use JSON for broader tooling compatibility

---

### F2: KPI module split across Phase 2 and Phase 3 without clear boundary [Applied]
<!-- severity: major -->
<!-- dimension: plan-hygiene -->

**Context:** Phase 2 task 3 creates `crates/sim-core/src/kpi.rs` with "snapshotable KPI accumulation hooks," and Phase 3 task 4 extends it with revenue, backlog, throughput, lead-time, and utilization computations. The acceptance criteria for Phase 2 reference "KPIs" but do not specify which KPIs must exist.

**Issue:** Phase 2's acceptance criteria ("identical final state, KPIs, and event stream") are untestable if no concrete KPIs are defined until Phase 3. This creates an implicit Phase 3 dependency in Phase 2 tests.

**Recommendation:** Phase 2 should define the KPI trait/interface and implement at least one concrete KPI (e.g., event count or total simulated time) sufficient to validate deterministic replay. Defer domain-specific KPIs (revenue, backlog, etc.) to Phase 3.

**Choices:**
- [x] Define KPI trait in Phase 2 with a trivial concrete KPI; add domain KPIs in Phase 3
- [ ] Move all KPI work to Phase 3 and remove KPI references from Phase 2 acceptance criteria

---

### F3: REST framework dependency not specified [Applied]
<!-- severity: minor -->
<!-- dimension: gaps -->

**Context:** Phase 4 introduces a REST API in `crates/sim-api/` but does not specify the HTTP framework (e.g., Axum, Actix-web, Warp).

**Issue:** The choice of HTTP framework affects `Cargo.toml` dependencies, async runtime selection (Tokio vs async-std), and API design patterns. Leaving it unspecified risks mid-implementation rework.

**Recommendation:** Specify Axum + Tokio as the HTTP stack. Axum is actively maintained, uses Tokio (dominant Rust async runtime), and has good ergonomics for typed extractors and state management.

**Choices:**
- [x] Use Axum + Tokio
- [ ] Use Actix-web
- [ ] Defer framework choice to implementation time

---

### F4: No error handling strategy defined [Applied]
<!-- severity: major -->
<!-- dimension: gaps -->

**Context:** Phase 2 acceptance criteria mention "invalid state transitions are rejected with typed errors" and Phase 4 mentions "invalid commands are rejected with typed errors," but no phase defines the error types, error propagation strategy, or the crate where shared error types live.

**Issue:** Without an agreed error strategy, each phase may introduce incompatible error patterns. Typed errors across crate boundaries need a shared foundation, likely in `sim-types`.

**Recommendation:** Add a task in Phase 2 to define a shared error enum in `sim-types` (e.g., `SimError`) with variants for invalid state transitions, unknown IDs, and event ordering violations. Extend it in later phases as needed.

**Choices:**
- [x] Define `SimError` in `sim-types` during Phase 2
- [ ] Let each crate define its own error types and unify later

---

### F5: UI testing strategy absent [Applied]
<!-- severity: minor -->
<!-- dimension: testing -->

**Context:** Phase 5 defines acceptance criteria for the UI (scenario loading, KPI visualization, baseline comparison) but lists no test files and no testing approach.

**Issue:** Phase 5 acceptance criteria are not testable through Rust's `cargo test`. Without at least smoke-level browser or integration tests, regressions in the UI will go undetected.

**Recommendation:** Add a task in Phase 5 for basic end-to-end tests (e.g., Playwright or Cypress) that verify the UI can load a scenario and display KPI data. Add the test runner to CI.

**Choices:**
- [x] Add Playwright e2e tests to Phase 5 and CI
- [ ] Defer UI testing to post-MVP
- [ ] Manual testing only for MVP

---

### F6: Scenario format choice should propagate to Phase 1 dependencies [Applied]
<!-- severity: minor -->
<!-- dimension: correctness -->
<!-- Depends on: F1 choice TOML -->

**Context:** If F1 is resolved by choosing TOML, then `serde` and `toml` must appear as dependencies in the relevant `Cargo.toml` files created in Phase 1.

**Issue:** Phase 1 creates the crate `Cargo.toml` files. If they lack `serde`/`toml` dependencies, Phase 2's scenario loader task will require modifying Phase 1 outputs, which is fine but should be explicit.

**Recommendation:** Phase 1 should include `serde` (with `derive` feature) and `toml` as dependencies in `sim-core/Cargo.toml` and `sim-types/Cargo.toml` since serialization is a core concern. Other crates add it when needed.

**Choices:**
- [x] Add `serde` and `toml` to `sim-core` and `sim-types` in Phase 1
- [ ] Add dependencies only when first used in Phase 2

---

### F7: No deterministic RNG strategy specified [Applied]
<!-- severity: major -->
<!-- dimension: correctness -->

**Context:** Phase 2 acceptance criteria require "identical final state, KPIs, and event stream across repeated runs" with a "fixed seed." Phase 3 scenarios likely need randomized demand arrival. However, no phase specifies which RNG crate to use or how the seed flows through the system.

**Issue:** Deterministic replay requires a specific seedable PRNG (e.g., `rand` with `ChaCha8Rng` or `StdRng` from `rand_chacha`). If this is not specified, different contributors may introduce non-deterministic randomness that breaks the core constraint.

**Recommendation:** Specify `rand` + `rand_chacha` (ChaCha8Rng) as the PRNG. Add the seed to the scenario configuration schema (F1). Document the determinism contract in `ARCHITECTURE.md`.

**Choices:**
- [x] Use `rand` + `rand_chacha` with seed in scenario config; document in `ARCHITECTURE.md`
- [ ] Use `rand::StdRng` and accept platform-dependent determinism
- [ ] Defer RNG choice to Phase 2 implementation

---

### F8: Postgres listed in Phase 6 compose.yaml but marked out of scope [Applied]
<!-- severity: minor -->
<!-- dimension: plan-hygiene -->

**Context:** The original plan mentioned "Postgres-backed local runs" in Phase 6. The rewritten plan moves Postgres to Out of Scope but `compose.yaml` in Phase 6 only orchestrates API + UI.

**Issue:** No actual conflict — this is a consistency note. The Out of Scope section correctly defers Postgres, and Phase 6's planned work correctly omits it.

**Recommendation:** No change needed. The plan is already consistent after the rewrite.

**Choices:**
- [x] No change — plan is consistent
- [ ] Remove the out-of-scope bullet about Postgres to reduce noise

---

### F9: Phase 3 task 5 referenced ambiguous format after F1 resolution [Applied]
<!-- severity: minor -->
<!-- dimension: plan-hygiene -->

**Context:** Phase 3 task 5 originally said "JSON or TOML format" despite F1 resolving the format to TOML.

**Issue:** Inconsistency with F1 resolution. A contributor reading Phase 3 might think the format is still open.

**Recommendation:** Change Phase 3 task 5 to reference the TOML schema defined in Phase 2.

**Choices:**
- [x] Update Phase 3 task 5 to reference Phase 2 TOML schema
- [ ] Leave both options open

---

### Re-sweep 1 (post-F1–F8 application)

All five dimensions re-swept against the updated plan:

| Dimension | Result |
|-----------|--------|
| **testing** | Pass — every phase has named test files or frameworks; acceptance criteria are testable within each phase independently. |
| **correctness** | Pass — dependencies, format choices, RNG strategy, and error types are consistent across all phases after F9 fix. |
| **gaps** | Pass — scenario loader, KPI trait, error enum, RNG, and REST framework are all specified. No missing work identified. |
| **best-practices** | Pass — crate structure follows Rust workspace conventions; CI covers fmt/clippy/test; docs include ARCHITECTURE, CONTRIBUTING, and TESTING. |
| **plan-hygiene** | Pass — phases are ordered so each phase's tests can run independently; constraints are respected; no scope creep. F9 inconsistency fixed. |

**No critical or major findings remain. Iteration complete.**

---

### Summary

| # | Title | Severity | Dimension | Depends on |
|---|-------|----------|-----------|------------|
| F1 | No scenario fixture format specified | major | gaps | — |
| F2 | KPI module split without clear boundary | major | plan-hygiene | — |
| F3 | REST framework dependency not specified | minor | gaps | — |
| F4 | No error handling strategy defined | major | gaps | — |
| F5 | UI testing strategy absent | minor | testing | — |
| F6 | Scenario format choice propagates to Phase 1 deps | minor | correctness | F1 |
| F7 | No deterministic RNG strategy specified | major | correctness | — |
| F8 | Postgres scope consistency | minor | plan-hygiene | — |
| F9 | Phase 3 format reference inconsistency | minor | plan-hygiene | F1 |
