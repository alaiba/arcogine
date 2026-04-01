# Rust-Based Factory Economy Simulation — MVP Plan

> **Date:** 2026-04-02
> **Scope:** Define and implement a GitHub-ready MVP for a single-user, locally runnable factory-and-economy simulation platform in Rust, with minimal UI, API-driven experimentation, deterministic testing, and containerized local deployment.
> **Primary sources:** `README.md:2` (project description), `LICENSE` (Apache 2.0), `docs/vision.md` (project identity and core loop), `docs/architecture-overview.md` (design philosophy and technology stack), `docs/standards-alignment.md` (industry standards mapping)

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

The repository contains six files: `README.md` (3 lines, project description at `README.md:2`), `LICENSE` (Apache 2.0), `devel/Original-plan.md` (planning notes), `docs/vision.md` (project identity, core loop, naming, and long-term directions), `docs/architecture-overview.md` (design philosophy, technology stack, crate structure, agent architecture, and DES model), and `docs/standards-alignment.md` (industry standards mapping with tiered alignment strategy: ISA-95, DES, RAMI 4.0, AAS, OPC UA, OpenAPI, and others). There is no `Cargo.toml`, no source code, no tests, no CI workflows, no Docker files, and no `.gitignore`. The project is greenfield for code, but architectural, vision, and standards documentation already exists and is authoritative. [F10, F11, F25 applied]

### 3.2 Architectural direction

`docs/architecture-overview.md` defines the target architecture: a Cargo workspace with crates `sim-core`, `sim-factory`, `sim-economy`, `sim-agents`, `sim-types`, `sim-cli`, and `sim-api`. The design uses discrete-event simulation, typed IDs, data-oriented state (SoA layouts), event logging, and API-driven control. The technology stack (Axum + Tokio, ChaCha8Rng, serde + toml, React + Vite) is specified. `docs/standards-alignment.md` maps industry standards (ISA-95, ISO 22400, RAMI 4.0, AAS, DES, OpenAPI, and others) to Arcogine's architecture with a tiered strategy: "align now" for MVP data model, KPI definitions, and API; "design for" to preserve future compatibility; "note for later" for post-MVP expansions. It also documents the Romanian/EU adoption context (ASRO SR EN transpositions, GDPR applicability). None of this is implemented yet. [F10, F11, F25 applied]

### 3.3 Deployment and collaboration expectations

The vision and architecture docs establish expectations for GitHub-readiness (README, CONTRIBUTING, architectural documentation), CI via GitHub Actions, Docker Compose for local multi-service runs, and benchmark scaffolding. `docs/vision.md` and `docs/architecture-overview.md` exist; CONTRIBUTING, CI, Docker, and benchmarks do not. [F10, F11 applied]

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

1. Create a Cargo workspace root `Cargo.toml` and crate directories for `sim-core`, `sim-factory`, `sim-economy`, `sim-agents`, `sim-types`, `sim-cli`, and `sim-api`. Each crate gets a minimal `Cargo.toml` and `src/lib.rs` (or `src/main.rs` for binaries). Include `serde` (with `derive` feature) and `toml` as dependencies in `sim-core` and `sim-types` for scenario serialization. Include `rand` and `rand_chacha` in `sim-core` for deterministic RNG. Include `tracing` and `tracing-subscriber` in `sim-core` for structured application logging. Under `[dev-dependencies]` in `sim-core`, include `proptest` for property testing and `criterion` for benchmarks (with `[[bench]]` targets). No existing source files to modify. [F6, F7, F13, F17, F18, F19 applied]
2. Add repository health files: `README.md` (expand from current 3-line stub at `README.md:1-3`, using `docs/vision.md` as the authoritative source for project identity and core loop), `CONTRIBUTING.md`, `SECURITY.md`, `CODE_OF_CONDUCT.md`, and `.gitignore`. Augment the existing `docs/architecture-overview.md` with a "Determinism Contract" section documenting ChaCha8Rng, seed propagation, and replay guarantees (do not create a separate root-level `ARCHITECTURE.md`). Also update the Repository Structure diagram in `docs/architecture-overview.md` to reflect the actual crate layout, benchmark locations under `crates/sim-core/benches/`, and the `examples/` directory. `LICENSE` already exists and needs no changes. [F7, F10, F11, F12, F22 applied]
3. Add baseline CI in `.github/workflows/ci.yml` for `cargo fmt --check`, `cargo clippy`, and `cargo test`.
4. Add `examples/` directory with a placeholder file explaining intended contents. The `docs/` directory already exists (contains `vision.md`, `architecture-overview.md`, and `standards-alignment.md`); add a placeholder `docs/README.md` index if needed. [F10, F11, F25 applied]

Files expected:
- `Cargo.toml` (new — workspace root)
- `crates/sim-core/Cargo.toml` (new — `[dependencies]`: `serde`, `toml`, `rand`, `rand_chacha`, `tracing`, `tracing-subscriber`; `[dev-dependencies]`: `proptest`, `criterion`), `crates/sim-core/src/lib.rs`
- `crates/sim-factory/Cargo.toml`, `crates/sim-factory/src/lib.rs`
- `crates/sim-economy/Cargo.toml`, `crates/sim-economy/src/lib.rs`
- `crates/sim-agents/Cargo.toml`, `crates/sim-agents/src/lib.rs`
- `crates/sim-types/Cargo.toml` (new — includes `serde`), `crates/sim-types/src/lib.rs`
- `crates/sim-cli/Cargo.toml` (new — includes `clap` with `derive` feature), `crates/sim-cli/src/main.rs`
- `crates/sim-api/Cargo.toml`, `crates/sim-api/src/lib.rs`
- `README.md` (modify existing `README.md:1-3`)
- `docs/architecture-overview.md` (modify — add Determinism Contract section), `CONTRIBUTING.md`, `SECURITY.md`, `CODE_OF_CONDUCT.md` (new)
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

1. Define typed IDs (`MachineId`, `ProductId`, `JobId`), simulation time (`SimTime`), shared enums/structs, and a shared error enum `SimError` (with variants for invalid state transitions, unknown IDs, event ordering violations) in `crates/sim-types/src/lib.rs`. Domain concepts should map cleanly to ISA-95 terminology (see `docs/standards-alignment.md`): machines are Equipment, products are Material Definitions, routing steps are Process Segments, and product routings are Operations Definitions. Use Arcogine's own naming in code (`Machine`, `Product`, `RoutingStep`) but include doc-comments noting the ISA-95 correspondence. [F4, F25 applied]
2. Implement event types (order creation, task start, task end, machine availability change, price change, agent decision), a priority-queue-based event scheduler, and deterministic event dispatch in `crates/sim-core/src/event.rs` and `crates/sim-core/src/queue.rs`. Use `ChaCha8Rng` from `rand_chacha` seeded from the scenario configuration for all stochastic decisions. [F7 applied]
3. Implement append-only event logging in `crates/sim-core/src/log.rs`. Define a `Kpi` trait in `crates/sim-core/src/kpi.rs` and implement at least one concrete KPI (e.g., `TotalSimulatedTime` or `EventCount`) sufficient to validate deterministic replay. [F2 applied]
4. Define the scenario file schema (machine definitions, product routings, initial conditions, run parameters, RNG seed) in TOML and implement a scenario loader in `crates/sim-core/src/scenario.rs`. Use TOML section names that correspond to ISA-95 concepts where practical (e.g., `[[equipment]]`, `[[material]]`, `[[process_segment]]`) to ease future data interchange; see `docs/standards-alignment.md` for the mapping. [F1, F7, F25 applied]
5. Define state stores for machines, products, jobs, and work queues using data-oriented structures in `crates/sim-factory/src/machines.rs`, `crates/sim-factory/src/jobs.rs`, and `crates/sim-factory/src/routing.rs`.
6. Write unit tests for event ordering, monotonic time progression, state transition safety, deterministic replay, and scenario loading in `crates/sim-core/tests/`. Write unit tests for machine state management, job lifecycle, and routing correctness in `crates/sim-factory/tests/machine_state.rs` and `crates/sim-factory/tests/job_routing.rs`. [F14 applied]
7. Write property tests in `crates/sim-core/tests/properties.rs` using `proptest` to verify invariants: no negative inventory, no duplicate job completion, monotonic time progression, and event causality consistency. [F13 applied]

Files expected:
- `crates/sim-types/src/lib.rs` (modify from Phase 1 stub — adds typed IDs, `SimError`)
- `crates/sim-core/src/lib.rs` (modify), `crates/sim-core/src/event.rs`, `crates/sim-core/src/queue.rs`, `crates/sim-core/src/log.rs`, `crates/sim-core/src/kpi.rs`, `crates/sim-core/src/scenario.rs` (new)
- `crates/sim-factory/src/lib.rs` (modify), `crates/sim-factory/src/machines.rs`, `crates/sim-factory/src/jobs.rs`, `crates/sim-factory/src/routing.rs` (new)
- `crates/sim-core/tests/determinism.rs`, `crates/sim-core/tests/event_ordering.rs`, `crates/sim-core/tests/scenario_loading.rs`, `crates/sim-core/tests/properties.rs` (new)
- `crates/sim-factory/tests/machine_state.rs`, `crates/sim-factory/tests/job_routing.rs` (new) [F14 applied]

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
4. Add revenue, backlog, throughput, lead-time, and utilization KPI computations to `crates/sim-core/src/kpi.rs`. Use definitions and naming consistent with ISO 22400 (SR EN ISO 22400) Part 2 where applicable: throughput rate (KPI 1200), utilization efficiency, production lead time, work-in-process. Include doc-comments referencing the ISO 22400 KPI identifier; see `docs/standards-alignment.md` for the full mapping. [F27 applied]
5. Create scenario fixture files in `examples/` for baseline, overload, and capacity-expansion runs using the TOML schema defined in Phase 2.
6. Write scenario acceptance tests in `crates/sim-core/tests/scenario_baselines.rs` that validate behavioral outcomes. Write unit tests for demand-model response to price and lead-time inputs in `crates/sim-economy/tests/demand_model.rs` and pricing logic in `crates/sim-economy/tests/pricing.rs`. [F15 applied]

Files expected:
- `crates/sim-factory/src/products.rs`, `crates/sim-factory/src/process.rs` (new)
- `crates/sim-factory/src/lib.rs` (modify to re-export new modules)
- `crates/sim-economy/src/lib.rs` (modify from Phase 1 stub), `crates/sim-economy/src/demand.rs`, `crates/sim-economy/src/pricing.rs` (new)
- `crates/sim-core/src/kpi.rs` (modify from Phase 2)
- `examples/basic_scenario.toml`, `examples/overload_scenario.toml`, `examples/capacity_expansion_scenario.toml` (new)
- `crates/sim-core/tests/scenario_baselines.rs` (new)
- `crates/sim-economy/tests/demand_model.rs`, `crates/sim-economy/tests/pricing.rs` (new) [F15 applied]

Acceptance criteria:
- Lowering price increases demand and creates observable backlog under constrained capacity.
- Raising price reduces load under otherwise identical conditions.
- A bottleneck machine produces measurable queue buildup and longer average lead time than the theoretical no-wait baseline.
- Completed production generates revenue exactly once per sale event.

---

### Phase 4. Add the command/query surface and simple agent

Objective: Allow controlled external influence over the simulation through explicit APIs and validate that an agent can improve or stabilize outcomes.

Planned work:

1. Implement a command/query interface in `crates/sim-api/src/routes.rs` and `crates/sim-api/src/server.rs` using Axum + Tokio, supporting: load scenario, step/run sim, change price, change machine count, toggle agent, query KPIs, query event log. Add `axum`, `tokio` (feature: `full`), `tower-http` (feature: `trace`), `serde_json`, and `utoipa` (with `axum_extras` feature) to `sim-api/Cargo.toml`. Wire `tracing` middleware (via `tower-http`) into the Axum server for structured request logging and error reporting (distinguish application-level observability from simulation event logging). Generate an OpenAPI 3.x specification from route definitions using `utoipa` and serve it at `/api-docs/openapi.json`; see `docs/standards-alignment.md` for the OpenAPI alignment rationale. [F3, F17, F23, F26 applied]
2. Wire `crates/sim-cli/src/main.rs` as the single binary entrypoint supporting both headless CLI mode and HTTP server mode (e.g., `arcogine run --headless` vs `arcogine serve`). `sim-api` remains a library crate providing route handlers and server setup; `sim-cli` depends on it and hosts the binary. [F16 applied]
3. Implement a `SalesAgent` in `crates/sim-agents/src/sales_agent.rs` that observes backlog, lead time, and revenue, then adjusts price using approved commands. The agent architecture must support future agent types (Planning, Procurement, Maintenance) and future LLM-based strategy agents, so the interface should be trait-based and agent-type-agnostic.
4. Ensure all commands are validated, logged in the event log, and replayable. Modify `crates/sim-core/src/log.rs` and `crates/sim-core/src/event.rs` as needed.
5. Write integration tests in `crates/sim-api/tests/api_smoke.rs` and `crates/sim-core/tests/agent_integration.rs`. Write unit tests for the agent trait and `SalesAgent` decision logic in `crates/sim-agents/tests/sales_agent.rs`. [F24 applied]

Files expected:
- `crates/sim-api/src/lib.rs` (modify), `crates/sim-api/src/server.rs`, `crates/sim-api/src/routes.rs` (new)
- `crates/sim-cli/src/main.rs` (modify from Phase 1 stub)
- `crates/sim-agents/src/lib.rs` (modify from Phase 1 stub), `crates/sim-agents/src/sales_agent.rs` (new)
- `crates/sim-core/src/log.rs`, `crates/sim-core/src/event.rs` (modify from Phase 2)
- `crates/sim-api/tests/api_smoke.rs`, `crates/sim-core/tests/agent_integration.rs`, `crates/sim-agents/tests/sales_agent.rs` (new) [F24 applied]

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

1. Add `Dockerfile` (builds the `sim-cli` binary, which hosts both CLI and HTTP server modes) and `ui/Dockerfile` (UI) with multi-stage builds. [F20 applied]
2. Add `compose.yaml` orchestrating API and UI services (Postgres deferred to post-MVP unless needed for event persistence).
3. Add `.dockerignore`, `.env.example`, and expand `README.md` with local run instructions for both native and containerized paths.
4. Add benchmark scaffolding in `crates/sim-core/benches/scheduler.rs` and `crates/sim-core/benches/scenario_runtime.rs` using Criterion (with `[[bench]]` targets in `sim-core/Cargo.toml` and `criterion` as a dev-dependency, already declared in Phase 1). [F18 applied]
5. Add `TESTING.md` documenting how to run unit, integration, scenario, and benchmark test suites.

Files expected:
- `Dockerfile`, `ui/Dockerfile` (new)
- `compose.yaml` (new)
- `.dockerignore`, `.env.example` (new)
- `README.md` (modify from Phase 1)
- `crates/sim-core/benches/scheduler.rs`, `crates/sim-core/benches/scenario_runtime.rs` (new) [F18 applied]
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
- Full ISA-95/B2MML data exchange (MVP uses ISA-95-aligned naming only; see `docs/standards-alignment.md`)
- OPC UA / MQTT connectivity (deferred to digital twin phase)
- AAS (Asset Administration Shell) export (deferred; MVP model is AAS-compatible by design)
- FMI (Functional Mock-up Interface) adapter (deferred to co-simulation phase)
- FIPA agent communication protocols (deferred to multi-agent phase)
- BPMN process workflow modeling (deferred to serious-game phase)

---

## 9. Future Directions (Post-MVP)

These capabilities build on the MVP foundation and are preserved as long-term directions. See `docs/standards-alignment.md` for the full standards mapping.

1. **Digital twin integration** — Connect to real ERP/MES/CRM systems via ISA-95/B2MML data exchange and OPC UA telemetry for live factory modeling. Expose machine and product models as Asset Administration Shell (AAS) submodels.
2. **Serious games** — Management training scenarios using the simulation as a game backend, with BPMN-like process definitions for workflow modeling.
3. **Multi-agent decision environments** — Multiple agents (Sales, Planning, Procurement, Maintenance) negotiating and competing within the same simulation. Consider FIPA agent communication protocols for multi-vendor interoperability.
4. **LLM-based agents** — Strategy-level agents powered by language models for decision-making research.
5. **MMO-scale economic simulations** — Partitioned simulation (per factory/region) with a shared economy layer, strong consistency for financial state, and event-driven inter-system communication via MQTT or similar messaging.
6. **Co-simulation** — FMI (Functional Mock-up Interface) adapter to integrate Arcogine with other simulation tools (energy models, physics engines, logistics simulators).
7. **Supply chain expansion** — Extend beyond single-factory modeling using SCOR (Supply Chain Operations Reference) process categories: plan, source, make, deliver, return.
8. **3D visualization layer** — Optional rendering for spatial factory layouts.
9. **Advanced scheduling and optimization** — Constraint-based or heuristic scheduling algorithms.
10. **Scenario authoring tools** — UI or DSL for creating and sharing simulation scenarios.
11. **Real-time analytics and dashboards** — Production-grade monitoring beyond the MVP experiment console, with Apache Arrow/Parquet for efficient analytical data storage.

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

**Recommendation:** Specify `rand` + `rand_chacha` (ChaCha8Rng) as the PRNG. Add the seed to the scenario configuration schema (F1). Document the determinism contract in `docs/architecture-overview.md`.

**Choices:**
- [x] Use `rand` + `rand_chacha` with seed in scenario config; document in `docs/architecture-overview.md`
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

### F10: Verified current state claims are stale — repo has 5 files, not 3 [Applied]
<!-- severity: critical -->
<!-- dimension: correctness -->

**Context:** §3.1 (line 35) says "The repository contains three files: `README.md`, `LICENSE`, and `devel/Original-plan.md`." The repository actually contains five files: `README.md`, `LICENSE`, `devel/Original-plan.md`, `docs/vision.md`, and `docs/architecture-overview.md`.

**Issue:** A coding agent executing Phase 1 will create `ARCHITECTURE.md` at the repo root (line 87) when an architectural document already exists at `docs/architecture-overview.md`. The `docs/` directory already exists, contradicting "Add `examples/` and `docs/` directories" (line 75) as if they are new. This creates file duplication and conflicting sources of truth.

**Recommendation:** Update §3.1 to list all five files. Update Phase 1 task 2 to reference and expand `docs/architecture-overview.md` instead of creating a new root-level `ARCHITECTURE.md`. Update Phase 1 task 4 to note that `docs/` already exists.

**Choices:**
- [x] Update §3.1 file inventory; expand existing `docs/architecture-overview.md` instead of creating `ARCHITECTURE.md`
- [ ] Delete `docs/architecture-overview.md` and `docs/vision.md`, create fresh `ARCHITECTURE.md`
- [ ] Keep both — root `ARCHITECTURE.md` for determinism contract, `docs/` for detailed docs

---

### F11: Plan ignores existing `docs/vision.md` entirely [Applied]
<!-- severity: major -->
<!-- dimension: correctness -->

**Context:** `docs/vision.md` defines the project identity, core loop, naming, long-term vision, and GitHub description. The plan never references it. Phase 1 task 2 creates `README.md` from scratch and Phase 1 task 4 creates `docs/` as if empty.

**Issue:** A coding agent may produce a README or architecture doc that contradicts or duplicates `docs/vision.md`. The vision doc's core-loop diagram, naming explanation, and long-term directions are authoritative context that Phase 1 docs should reference, not reinvent.

**Recommendation:** Add `docs/vision.md` to §3.1 inventory. Phase 1 task 2 should reference `docs/vision.md` as the source of project identity for `README.md` expansion. Phase 1 task 4 should note `docs/` exists and contains `vision.md` and `architecture-overview.md`.

**Choices:**
- [x] Reference `docs/vision.md` as authoritative for project identity; update §3.1 and Phase 1 accordingly
- [ ] Merge `docs/vision.md` content into `README.md` and delete the file

---

### F12: `ARCHITECTURE.md` planned at root but `docs/architecture-overview.md` already covers the same content [Applied]
<!-- severity: major -->
<!-- dimension: correctness -->
<!-- Depends on: F10 choice 1 -->

**Context:** Phase 1 creates `ARCHITECTURE.md` at the repo root (line 87) with determinism contract documentation. `docs/architecture-overview.md` already documents: simulation-first philosophy, data-oriented design, DES event types, separation of concerns (5-layer table with crate names), agent architecture, technology stack (including Axum, ChaCha8Rng, serde+toml), and repository structure.

**Issue:** Nearly everything the plan asks `ARCHITECTURE.md` to contain already exists in `docs/architecture-overview.md`. Creating a second file produces conflicting authorities. The only gap in the existing doc is the explicit determinism contract (seed propagation, replay guarantees).

**Recommendation:** Phase 1 should augment `docs/architecture-overview.md` with a "Determinism Contract" section rather than creating a separate root-level `ARCHITECTURE.md`. Update the "Files expected" list accordingly.

**Choices:**
- [x] Augment `docs/architecture-overview.md` with determinism contract; drop root `ARCHITECTURE.md`
- [ ] Create root `ARCHITECTURE.md` as a slim pointer to `docs/architecture-overview.md` plus the determinism contract

---

### F13: Property tests specify `proptest` or `quickcheck` but neither appears in Phase 1 dependencies [Applied]
<!-- severity: major -->
<!-- dimension: testing -->

**Context:** Phase 2 task 7 (line 110) specifies property tests using `proptest` or `quickcheck`. Phase 1 task 1 lists `serde`, `toml`, `rand`, and `rand_chacha` as dependencies but neither `proptest` nor `quickcheck`.

**Issue:** The coding agent will reach Phase 2 task 7 and need to add a dev-dependency that was never specified. The choice between `proptest` and `quickcheck` should be resolved upfront — they have different APIs, shrinking behavior, and macro styles.

**Recommendation:** Choose `proptest` (better shrinking, more popular in the Rust ecosystem). Add it as a dev-dependency to `sim-core` in Phase 1 task 1. Update Phase 2 task 7 to remove the "or `quickcheck`" ambiguity.

**Choices:**
- [x] Add `proptest` as dev-dependency in Phase 1; resolve to `proptest` only in Phase 2
- [ ] Defer to Phase 2 and let the implementer choose
- [ ] Use `quickcheck` for lighter weight

---

### F14: No `sim-factory` unit tests defined in any phase [Applied]
<!-- severity: major -->
<!-- dimension: testing -->

**Context:** Phase 2 tasks 5–6 implement state stores in `sim-factory` (machines, jobs, routing) and write tests only in `crates/sim-core/tests/`. Phase 3 adds products and process logic to `sim-factory` but lists tests only in `crates/sim-core/tests/scenario_baselines.rs`. No phase defines `crates/sim-factory/tests/` or unit tests within `sim-factory` modules.

**Issue:** The factory layer — machines, jobs, routing, products, process flow — has zero planned unit tests. All testing goes through `sim-core` integration/scenario tests. This makes defect isolation harder and violates the plan's own modularity principle.

**Recommendation:** Add unit tests for `sim-factory` in Phase 2 (for machines, jobs, routing) and Phase 3 (for products, process). At minimum: `crates/sim-factory/tests/machine_state.rs`, `crates/sim-factory/tests/job_routing.rs`.

**Choices:**
- [x] Add `sim-factory` unit tests to Phase 2 and Phase 3
- [ ] Rely on `sim-core` scenario tests to cover factory logic indirectly

---

### F15: No `sim-economy` unit tests defined [Applied]
<!-- severity: major -->
<!-- dimension: testing -->

**Context:** Phase 3 implements demand and pricing in `sim-economy` but lists no test files for `crates/sim-economy/tests/`. Testing is only through `crates/sim-core/tests/scenario_baselines.rs`.

**Issue:** Demand and pricing models are central to the MVP hypothesis. Without unit tests, verifying that the demand function responds correctly to price/lead-time inputs requires running full scenarios — slow and coarse-grained.

**Recommendation:** Add unit tests in Phase 3: `crates/sim-economy/tests/demand_model.rs` and `crates/sim-economy/tests/pricing.rs`.

**Choices:**
- [x] Add `sim-economy` unit tests to Phase 3
- [ ] Rely solely on scenario-level tests

---

### F16: `sim-api` listed as `src/lib.rs` in Phase 1 but needs `src/main.rs` or a binary target [Applied]
<!-- severity: minor -->
<!-- dimension: correctness -->

**Context:** Phase 1 files list `crates/sim-api/Cargo.toml`, `crates/sim-api/src/lib.rs` (line 85). Phase 4 creates `server.rs` and `routes.rs` and adds Axum, but the API crate serves HTTP — it needs to be runnable. The Dockerfile in Phase 6 builds the API as a binary.

**Issue:** If `sim-api` is a library crate only, it cannot be run directly. Phase 4 will need either a `main.rs` in `sim-api` or the `sim-cli` crate must host the server. The plan is ambiguous about which binary serves HTTP.

**Recommendation:** Clarify that `sim-api` is a library crate providing route handlers and server setup, while `sim-cli` is the single binary that can run in both headless-CLI and HTTP-server modes. Alternatively, add `src/main.rs` to `sim-api`. The first option is cleaner — one binary, two modes.

**Choices:**
- [x] `sim-api` stays a library; `sim-cli` is the single binary with CLI and server modes
- [ ] Add `src/main.rs` to `sim-api` as a second binary target

---

### F17: No observability / structured logging strategy [Applied]
<!-- severity: major -->
<!-- dimension: gaps -->

**Context:** The plan mentions "append-only event logging" (Phase 2 task 3) for simulation events but never addresses application-level observability — structured logging for the API server, startup diagnostics, error reporting, or request tracing.

**Issue:** When a coding agent implements the Axum API (Phase 4), there's no guidance on logging middleware, log levels, or output format. Debugging issues in integration tests or containerized runs will be difficult. The Rust ecosystem standard is `tracing` + `tracing-subscriber`.

**Recommendation:** Add `tracing` and `tracing-subscriber` to `sim-core` dependencies in Phase 1. Add a task to Phase 4 to wire `tracing` middleware into the Axum server. Distinguish simulation event logging (domain) from application observability (infrastructure).

**Choices:**
- [x] Add `tracing` + `tracing-subscriber` in Phase 1; wire into API in Phase 4
- [ ] Defer all observability to post-MVP
- [ ] Use `log` + `env_logger` for simplicity

---

### F18: Benchmark crate location is ambiguous [Applied]
<!-- severity: minor -->
<!-- dimension: plan-hygiene -->

**Context:** Phase 6 task 4 (line 223) places benchmarks in `benches/scheduler.rs` and `benches/scenario_runtime.rs`. The architecture doc (line 98) shows `benches/` at workspace root. However, Criterion benchmarks in a Cargo workspace typically live inside individual crates (`crates/sim-core/benches/`), not at the workspace root, unless a dedicated benchmark crate exists.

**Issue:** A workspace-root `benches/` directory won't compile unless it's part of a crate with `[[bench]]` targets in its `Cargo.toml`. The plan doesn't specify which crate owns the benchmarks.

**Recommendation:** Place benchmarks in `crates/sim-core/benches/` and add `[[bench]]` targets to `crates/sim-core/Cargo.toml`. Add `criterion` as a dev-dependency there.

**Choices:**
- [x] Place benchmarks in `crates/sim-core/benches/` with Criterion dev-dependency
- [ ] Create a dedicated `crates/sim-bench/` crate
- [ ] Keep at workspace root with a top-level `Cargo.toml` `[[bench]]` section

---

### F19: Phase 2 test files placed in `crates/sim-core/tests/` but `proptest` dev-dependency ownership unclear [Applied]
<!-- severity: minor -->
<!-- dimension: plan-hygiene -->
<!-- Depends on: F13 choice proptest -->

**Context:** Phase 2 lists `crates/sim-core/tests/properties.rs` and other test files in the `tests/` directory of `sim-core`. Integration tests in `tests/` are compiled as separate binaries and need dev-dependencies declared in `sim-core/Cargo.toml`.

**Issue:** The plan doesn't explicitly state that `proptest` and other test-only crates go in `[dev-dependencies]`. A coding agent might put them in `[dependencies]`, inflating the production binary.

**Recommendation:** Phase 1 task 1 should explicitly note that `proptest` (and later `criterion`) go under `[dev-dependencies]` in `sim-core/Cargo.toml`.

**Choices:**
- [x] Clarify dev-dependencies in Phase 1 task 1
- [ ] Assume the coding agent knows Rust conventions

---

### F20: Phase 6 Dockerfile label says "Rust API" but should build `sim-cli` binary [Applied]
<!-- severity: major -->
<!-- dimension: correctness -->
<!-- Depends on: F16 choice 1 -->

**Context:** Phase 6 task 1 (line 222) says `Add Dockerfile (Rust API)`. Per F16 resolution, `sim-api` is a library crate; the actual binary is `sim-cli` which hosts both CLI and server modes.

**Issue:** A coding agent will try to build a binary from `sim-api` which has no `main.rs`. The Dockerfile must build the `sim-cli` binary (which depends on `sim-api` as a library).

**Recommendation:** Update Phase 6 task 1 to say `Dockerfile (builds sim-cli binary)` instead of `Dockerfile (Rust API)`.

**Choices:**
- [x] Update Dockerfile description to reference `sim-cli` binary
- [ ] Leave as-is and assume the builder will figure it out

---

### F21: No CLI argument parsing framework specified [Applied]
<!-- severity: minor -->
<!-- dimension: gaps -->

**Context:** Phase 4 task 2 (line 165) specifies that `sim-cli` supports both headless CLI mode and HTTP server mode (`arcogine run --headless` vs `arcogine serve`). No phase specifies a CLI argument parsing library.

**Issue:** Subcommand-style CLI parsing requires a library (e.g., `clap` with derive). Without specifying this, the coding agent may implement ad-hoc argument parsing or choose an unexpected library.

**Recommendation:** Add `clap` (with `derive` feature) as a dependency of `sim-cli` in Phase 1 task 1. It's the dominant Rust CLI framework and integrates well with the ecosystem.

**Choices:**
- [x] Add `clap` with `derive` feature to `sim-cli` in Phase 1
- [ ] Defer to Phase 4 implementation
- [ ] Use raw `std::env::args` parsing

---

### F22: `docs/architecture-overview.md` shows `benches/` at workspace root, inconsistent with F18 resolution [Applied]
<!-- severity: minor -->
<!-- dimension: correctness -->
<!-- Depends on: F18 choice 1 -->

**Context:** `docs/architecture-overview.md` line 98 shows `benches/` at the workspace root in the Repository Structure section. The plan (F18 applied) now places benchmarks in `crates/sim-core/benches/`.

**Issue:** After Phase 1 augments `docs/architecture-overview.md` (F12), the repository structure tree will be stale. The coding agent should update the structure diagram as part of Phase 1 task 2.

**Recommendation:** Phase 1 task 2 should update the Repository Structure section in `docs/architecture-overview.md` to show benchmarks under `crates/sim-core/benches/` and add `examples/` to the tree.

**Choices:**
- [x] Update `docs/architecture-overview.md` repo structure as part of Phase 1 task 2
- [ ] Defer repo structure update to Phase 6

---

### F23: Phase 4 Axum/Tokio dependencies not declared in any phase's `Cargo.toml` plan [Applied]
<!-- severity: major -->
<!-- dimension: gaps -->

**Context:** Phase 4 task 1 specifies Axum + Tokio for the HTTP server. Phase 1 task 1 declares dependencies for `sim-core` and `sim-types` but never mentions Axum, Tokio, or any HTTP-related dependencies for `sim-api` or `sim-cli`.

**Issue:** The coding agent reaching Phase 4 will need to add `axum`, `tokio` (with `full` feature), `tower-http` (for tracing middleware), and `serde_json` (for JSON API responses) to `sim-api/Cargo.toml`. This should be specified so Phase 4 task 1 is self-contained.

**Recommendation:** Add a note in Phase 4 task 1 specifying the dependencies to add: `axum`, `tokio` (features: `full`), `tower-http` (feature: `trace`), `serde_json` in `sim-api/Cargo.toml`. Also add `clap` to `sim-cli/Cargo.toml` if not already done in Phase 1.

**Choices:**
- [x] Specify Axum/Tokio/tower-http dependencies explicitly in Phase 4 task 1
- [ ] Let the coding agent infer required dependencies

---

### F24: No `sim-agents` unit tests defined [Applied]
<!-- severity: minor -->
<!-- dimension: testing -->

**Context:** Phase 4 implements the `SalesAgent` in `sim-agents` and the agent trait interface. Tests are only in `crates/sim-api/tests/api_smoke.rs` and `crates/sim-core/tests/agent_integration.rs`. No unit tests are planned for the agent logic itself.

**Issue:** The agent's decision logic (when to adjust price, by how much) is testable in isolation. Integration tests via the API are coarser and slower. Unit-testing the agent ensures the trait interface works and the decision policy is correct without requiring a running server.

**Recommendation:** Add `crates/sim-agents/tests/sales_agent.rs` to Phase 4 files.

**Choices:**
- [x] Add `sim-agents` unit tests to Phase 4
- [ ] Rely on integration tests only

---

### Re-sweep 2 (post-F10–F19 application)

| Dimension | Result |
|-----------|--------|
| **testing** | F24 found — minor; `sim-agents` lacks unit tests. Applied above. |
| **correctness** | F20 found — major; Dockerfile references wrong binary. F22 found — minor; arch doc repo structure stale. Applied above. |
| **gaps** | F21 found — minor; no CLI framework specified. F23 found — major; Axum/Tokio deps undeclared. Applied above. |
| **best-practices** | Pass — Rust workspace conventions, CI setup, doc structure all consistent with ecosystem norms. |
| **plan-hygiene** | Pass — phases are self-contained after dependency declarations; acceptance criteria are testable. |

### Re-sweep 3 (post-F20–F24 application)

All five dimensions re-swept against the updated plan:

| Dimension | Result |
|-----------|--------|
| **testing** | Pass — every crate with logic has unit tests (`sim-core`, `sim-factory`, `sim-economy`, `sim-agents`); integration tests cover API and agent flows; property tests use `proptest`; UI has Playwright e2e; benchmarks use Criterion. Dev-dependencies explicitly declared. |
| **correctness** | Pass — §3.1 matches actual repo (5 files); `docs/vision.md` and `docs/architecture-overview.md` referenced as authoritative; no root `ARCHITECTURE.md`; Dockerfile builds `sim-cli`; `sim-api` is a library; benchmarks in `crates/sim-core/benches/`; repo structure diagram update planned. |
| **gaps** | Pass — scenario format (TOML), error strategy (`SimError`), REST framework (Axum+Tokio with deps), CLI framework (`clap`), observability (`tracing`), KPI boundary, and dependency declarations are all specified. |
| **best-practices** | Pass — Rust workspace conventions, CI (fmt/clippy/test), separated dev-dependencies, docs referencing existing authoritative files, Playwright for UI tests. |
| **plan-hygiene** | Pass — phases are self-contained; dependencies declared before use; acceptance criteria testable within each phase; no ambiguous choices remain; all applied findings preserved with context. |

**No critical or major findings remain. Iteration complete.**

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
| F10 | Verified current state claims are stale — repo has 5 files, not 3 | critical | correctness | — |
| F11 | Plan ignores existing `docs/vision.md` entirely | major | correctness | — |
| F12 | `ARCHITECTURE.md` at root duplicates `docs/architecture-overview.md` | major | correctness | F10 |
| F13 | Property test framework unspecified in dependencies | major | testing | — |
| F14 | No `sim-factory` unit tests defined | major | testing | — |
| F15 | No `sim-economy` unit tests defined | major | testing | — |
| F16 | `sim-api` lib vs binary ambiguity | minor | correctness | — |
| F17 | No observability / structured logging strategy | major | gaps | — |
| F18 | Benchmark crate location is ambiguous | minor | plan-hygiene | — |
| F19 | `proptest` dev-dependency ownership unclear | minor | plan-hygiene | F13 |
| F20 | Dockerfile should build `sim-cli`, not `sim-api` | major | correctness | F16 |
| F21 | No CLI argument parsing framework specified | minor | gaps | — |
| F22 | `docs/architecture-overview.md` repo structure stale | minor | correctness | F18 |
| F23 | Phase 4 Axum/Tokio dependencies undeclared | major | gaps | — |
| F24 | No `sim-agents` unit tests defined | minor | testing | — |

---

### F25: No industry standards alignment strategy [Applied]
<!-- severity: major -->
<!-- dimension: gaps -->

**Context:** Arcogine sits at the intersection of manufacturing systems, digital twins, simulation, and agent-based decision making. The plan mentions ISA-95, DES, and digital twin concepts in Future Directions (§9) but provides no guidance on how the MVP data model, naming, or API should relate to industry standards. The existing `docs/vision.md` and `docs/architecture-overview.md` do not reference standards.

**Issue:** Without standards awareness, the MVP data model may use naming conventions that require painful refactoring when ISA-95 integration, AAS export, or OPC UA connectivity are added. Some standards (ISA-95 naming, DES methodology, OpenAPI) can be aligned with at near-zero MVP cost by choosing compatible names and adding a spec file. Others (OPC UA, FMI, FIPA) are purely post-MVP but the architecture should not preclude them.

**Recommendation:** Create `docs/standards-alignment.md` documenting a tiered alignment strategy: "align now" (ISA-95 naming in `sim-types`/`sim-factory`, DES methodology, OpenAPI spec), "design for" (RAMI 4.0 layer mapping, AAS-compatible asset model, FMI-compatible headless core), "note for later" (OPC UA, MQTT, BPMN, SCOR, FIPA, ISO 8000, Arrow/Parquet). Update Phase 2 typed IDs and scenario schema to use ISA-95-aligned naming with doc-comments. Add OpenAPI spec generation (`utoipa`) to Phase 4. Add standards reference to `docs/architecture-overview.md`. Enrich §9 Future Directions and §8 Out of Scope with standards context.

**Choices:**
- [x] Create `docs/standards-alignment.md` with tiered strategy; align naming and add OpenAPI in MVP
- [ ] Defer all standards work to post-MVP
- [ ] Implement full ISA-95 data model in MVP (scope creep)

---

### F26: No OpenAPI specification for the REST API [Applied]
<!-- severity: minor -->
<!-- dimension: best-practices -->
<!-- Depends on: F25 choice 1 -->

**Context:** Phase 4 implements a REST API but generates no machine-readable API specification. The standards alignment strategy (F25) identifies OpenAPI as a "Tier 1 — Align now" standard.

**Issue:** Without an OpenAPI spec, the API is not machine-discoverable, cannot be tested with standard tools (e.g., Swagger UI, Postman import), and integration with external systems requires manual documentation.

**Recommendation:** Add `utoipa` (with `axum_extras` feature) to `sim-api/Cargo.toml` in Phase 4. Generate and serve an OpenAPI 3.x spec at `/api-docs/openapi.json`.

**Choices:**
- [x] Add `utoipa` to Phase 4 and serve OpenAPI spec
- [ ] Write a static OpenAPI YAML file manually
- [ ] Defer API documentation to post-MVP

---

### Re-sweep 4 (post-F25–F26 application)

All five dimensions re-swept against the updated plan:

| Dimension | Result |
|-----------|--------|
| **testing** | Pass — no new test gaps from standards changes (OpenAPI spec is a served endpoint, testable via existing API smoke tests). |
| **correctness** | Pass — §3.1 updated to 6 files including `docs/standards-alignment.md`. Phase 2 typed IDs and scenario schema reference ISA-95 mapping. Phase 4 includes `utoipa` dependency. `docs/architecture-overview.md` references standards. §8 Out of Scope and §9 Future Directions enriched with standards context. |
| **gaps** | Pass — standards alignment strategy documented; ISA-95 naming, DES methodology, and OpenAPI covered in MVP; remaining standards explicitly deferred with rationale. |
| **best-practices** | Pass — OpenAPI spec generation follows REST API best practices; ISA-95 doc-comments align with manufacturing domain conventions without overcomplicating code. |
| **plan-hygiene** | Pass — standards changes are minimal and localized (naming guidance, one dependency, doc-comments); no new phases, no scope creep; Future Directions and Out of Scope are consistent with the tiered strategy. |

**No critical or major findings remain. Iteration complete.**

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
| F10 | Verified current state claims are stale — repo has 5 files, not 3 | critical | correctness | — |
| F11 | Plan ignores existing `docs/vision.md` entirely | major | correctness | — |
| F12 | `ARCHITECTURE.md` at root duplicates `docs/architecture-overview.md` | major | correctness | F10 |
| F13 | Property test framework unspecified in dependencies | major | testing | — |
| F14 | No `sim-factory` unit tests defined | major | testing | — |
| F15 | No `sim-economy` unit tests defined | major | testing | — |
| F16 | `sim-api` lib vs binary ambiguity | minor | correctness | — |
| F17 | No observability / structured logging strategy | major | gaps | — |
| F18 | Benchmark crate location is ambiguous | minor | plan-hygiene | — |
| F19 | `proptest` dev-dependency ownership unclear | minor | plan-hygiene | F13 |
| F20 | Dockerfile should build `sim-cli`, not `sim-api` | major | correctness | F16 |
| F21 | No CLI argument parsing framework specified | minor | gaps | — |
| F22 | `docs/architecture-overview.md` repo structure stale | minor | correctness | F18 |
| F23 | Phase 4 Axum/Tokio dependencies undeclared | major | gaps | — |
| F24 | No `sim-agents` unit tests defined | minor | testing | — |
| F25 | No industry standards alignment strategy | major | gaps | — |
| F26 | No OpenAPI specification for REST API | minor | best-practices | F25 |

---

### F27: KPIs not aligned with ISO 22400; no Romanian/EU adoption context [Applied]
<!-- severity: major -->
<!-- dimension: gaps -->
<!-- Depends on: F25 choice 1 -->

**Context:** Phase 3 task 4 defines KPIs (throughput, utilization, lead time, backlog) but does not reference ISO 22400 (SR EN ISO 22400), the international standard that defines manufacturing KPI formulas, units, and timing semantics. The standards alignment document (F25) covers ISA-95 and OpenAPI but omits ISO 22400 and the Romanian/EU regional adoption context (ASRO, SR EN transpositions).

**Issue:** ISO 22400 defines exactly the KPIs that Arcogine computes. Without alignment, KPI names and definitions may diverge from industry-standard reporting, reducing credibility in industrial and academic contexts. The Romanian standards context (ASRO, SR EN) is relevant for positioning in the target market but was not documented.

**Recommendation:** Promote ISO 22400 to Tier 1 in `docs/standards-alignment.md` with a mapping table (ISO 22400 KPI → Arcogine KPI). Add a Regional Adoption Context section documenting the ASRO/SR EN system and confirming that ISO/EN alignment satisfies Romanian requirements. Add ISO 9001 (Tier 2, design for — quality management traceability), ISO 10303/STEP (Tier 3 — product data), GDPR (Tier 3 — personal data when real data enters), and industrial fieldbus protocols (Tier 3). Update Phase 3 task 4 to reference ISO 22400 KPI definitions.

**Choices:**
- [x] Add ISO 22400 to Tier 1, regional adoption context, and additional standards to appropriate tiers
- [ ] Defer all regional and KPI standard alignment to post-MVP
- [ ] Implement full ISO 22400 KPI set (~35 KPIs) in MVP (scope creep)

---

### Re-sweep 5 (post-F27 application)

All five dimensions re-swept against the updated plan and standards document:

| Dimension | Result |
|-----------|--------|
| **testing** | Pass — ISO 22400 KPI alignment is naming/doc-comments only; no test changes needed. Existing Phase 3 scenario tests already validate the KPI quantities. |
| **correctness** | Pass — §3.2 updated to reference ISO 22400. Phase 3 task 4 references ISO 22400 KPI identifiers. `docs/standards-alignment.md` includes Regional Adoption Context, ISO 22400 Tier 1 mapping, and all new Tier 2/3 entries. Summary table includes Romanian transposition column. |
| **gaps** | Pass — ISO 22400 (manufacturing KPIs), ISO 9001 (quality management), ISO 10303 (product data), GDPR, and industrial protocols all placed in appropriate tiers with clear MVP/post-MVP boundaries. |
| **best-practices** | Pass — KPI doc-comments referencing ISO 22400 identifiers follow manufacturing software conventions. Regional context section establishes market positioning. |
| **plan-hygiene** | Pass — no new phases; one task updated (Phase 3.4); standards doc is self-contained and cross-referenced from plan. |

**No critical or major findings remain. Iteration complete.**

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
| F10 | Verified current state claims are stale — repo has 5 files, not 3 | critical | correctness | — |
| F11 | Plan ignores existing `docs/vision.md` entirely | major | correctness | — |
| F12 | `ARCHITECTURE.md` at root duplicates `docs/architecture-overview.md` | major | correctness | F10 |
| F13 | Property test framework unspecified in dependencies | major | testing | — |
| F14 | No `sim-factory` unit tests defined | major | testing | — |
| F15 | No `sim-economy` unit tests defined | major | testing | — |
| F16 | `sim-api` lib vs binary ambiguity | minor | correctness | — |
| F17 | No observability / structured logging strategy | major | gaps | — |
| F18 | Benchmark crate location is ambiguous | minor | plan-hygiene | — |
| F19 | `proptest` dev-dependency ownership unclear | minor | plan-hygiene | F13 |
| F20 | Dockerfile should build `sim-cli`, not `sim-api` | major | correctness | F16 |
| F21 | No CLI argument parsing framework specified | minor | gaps | — |
| F22 | `docs/architecture-overview.md` repo structure stale | minor | correctness | F18 |
| F23 | Phase 4 Axum/Tokio dependencies undeclared | major | gaps | — |
| F24 | No `sim-agents` unit tests defined | minor | testing | — |
| F25 | No industry standards alignment strategy | major | gaps | — |
| F26 | No OpenAPI specification for REST API | minor | best-practices | F25 |
| F27 | KPIs not aligned with ISO 22400; no Romanian/EU context | major | gaps | F25 |
