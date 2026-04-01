# GitHub-Ready MVP Plan for a Rust-Based Factory Economy Simulation

> **Date:** 2026-04-02
> **Scope:** Define a GitHub-ready MVP for a single-user, locally runnable factory-and-economy simulation platform in Rust, with minimal UI, API-driven experimentation, testing, and containerized local deployment.
> **Primary sources:** None available (no repository/files/docs/tests were provided in this conversation)

---

## 1. Goal

* Establish a practical MVP that proves a closed-loop factory + economy simulation can run deterministically in Rust.
* Define the minimum architecture, testing strategy, local deployment model, and UI needed for single-user experimentation.
* Preserve the long-term path toward digital-twin, serious-game, multi-agent, and MMO-adjacent extensions without overbuilding the MVP.
* Structure the work so it can be published on GitHub as a reproducible, contributor-friendly open project.

---

## 2. Non-Negotiable Constraints

1. The implementation language for the core system is Rust. No repository/file citations are available; this constraint is derived from the conversation.
2. The architecture should center on a headless simulation core first, with visualization and game-like layers added later. No repository/file citations are available; this constraint is derived from the conversation.
3. The MVP must support a simple factory flow plus a minimal economy loop, not just production simulation in isolation. No repository/file citations are available; this constraint is derived from the conversation.
4. The design must remain GitHub-friendly: readable, modular, testable, reproducible, and suitable for public collaboration. No repository/file citations are available; this constraint is derived from the conversation.
5. A minimal single-user UI is required for experimentation, but it should behave like an experiment console rather than a full game client. No repository/file citations are available; this constraint is derived from the conversation.
6. Local execution should support both native development and containerized multi-service runs. No repository/file citations are available; this constraint is derived from the conversation.
7. The system must include a testing strategy with explicit acceptance criteria, determinism expectations, and scenario-level validation. No repository/file citations are available; this constraint is derived from the conversation.
8. The MVP should support an agent interacting through approved interfaces, not direct mutation of simulation state. No repository/file citations are available; this constraint is derived from the conversation.

---

## 3. Verified Current State

### 3.1 Repository baseline

No repository, codebase, documentation set, or test suite was provided in the conversation, so there is no verified current implementation state and no `<repo/path:line>` citations are available yet.

### 3.2 Architectural direction already chosen

The conversation converged on a Rust-based, data-oriented simulation core using discrete-event simulation, typed IDs, packed state, event logging, API-driven control, and a minimal web UI for experimentation. This is a conversational baseline only; it is not yet verified against source files because no source files were provided.

### 3.3 Deployment and collaboration expectations

The conversation established that the project should be GitHub-ready, support containerized local deployment, and prioritize reproducibility, CI, modularity, and public contribution hygiene. These are agreed planning assumptions, but not verified in code because no repository was provided.

### 3.4 MVP product definition

The conversation defined the MVP as a headless Rust simulation that models a simple factory, links pricing to demand, allows an agent to adjust price via API, and produces explainable economic and operational outcomes. This is the current planning target, not a code-verified implementation state.

---

## 4. Recommended Approach

(Recommended) Build a GitHub-ready Rust workspace around a headless discrete-event simulation core, add a thin API and minimal web UI for single-user experimentation, and defer advanced game/MMO/twin integrations until the core loop is validated through deterministic scenario tests.

Rationale:

* It satisfies the core architectural preference for simulation-first design.
* It proves the central business hypothesis: decisions in pricing and capacity affect demand, backlog, lead time, and revenue in a traceable way.
* It preserves a path toward multi-agent control and future distributed scaling without forcing those concerns into the MVP.
* It fits public GitHub development better than an engine-first approach by keeping the core modular, testable, and reproducible.
* It aligns with the local deployment decision to support both fast native development and Docker Compose-based reproducible runs.

---

## 5. Phased Plan

### Phase 1. Establish the public repository foundation

Objective: Create a GitHub-ready project skeleton that is reproducible, modular, and ready for contribution before simulation logic is added.

Planned work:

1. Create a Cargo workspace with initial crates for `sim-core`, `sim-factory`, `sim-economy`, `sim-agents`, `sim-types`, `sim-cli`, and `sim-api`; there is no existing source tree to cite yet, so these will be net-new files.
2. Add repository health files: `README.md`, `ARCHITECTURE.md`, `CONTRIBUTING.md`, `SECURITY.md`, `CODE_OF_CONDUCT.md`, `LICENSE`, and `.gitignore`; there is no current repo documentation to cite yet.
3. Add baseline CI in `.github/workflows/ci.yml` for formatting, linting, tests, and scenario-smoke execution; there is no existing workflow file to cite yet.
4. Add `examples/` and `docs/` directories for scenario examples and user-facing architecture/testing documentation; there is no current structure to cite yet.

Files expected:

* `Cargo.toml`
* `crates/sim-core/Cargo.toml`
* `crates/sim-factory/Cargo.toml`
* `crates/sim-economy/Cargo.toml`
* `crates/sim-agents/Cargo.toml`
* `crates/sim-types/Cargo.toml`
* `crates/sim-cli/Cargo.toml`
* `crates/sim-api/Cargo.toml`
* `README.md`
* `ARCHITECTURE.md`
* `CONTRIBUTING.md`
* `SECURITY.md`
* `CODE_OF_CONDUCT.md`
* `LICENSE`
* `.github/workflows/ci.yml`

Acceptance criteria:

* A new contributor can clone the repository and understand the project purpose, layout, contribution flow, and local run path from the docs.
* `cargo fmt --check`, `cargo clippy`, and `cargo test` run in CI, even if tests are initially minimal.
* The repository structure clearly separates simulation core, economy logic, agent logic, API surface, and CLI entrypoints.

---

### Phase 2. Build the deterministic simulation kernel

Objective: Implement the smallest useful discrete-event simulation engine and core typed state needed to run factory scenarios reproducibly.

Planned work:

1. Implement typed IDs, simulation time, event types, event queue, and deterministic event processing in `sim-types` and `sim-core`; these are net-new files.
2. Define state stores for machines, products, jobs, and queues using packed data-oriented structures in `sim-factory`; these are net-new files.
3. Add append-only event logging and snapshotable KPI accumulation hooks in `sim-core`; these are net-new files.
4. Add baseline unit tests for event ordering, monotonic time progression, state transition safety, and deterministic replay; these test files will be new.

Files expected:

* `crates/sim-types/src/lib.rs`
* `crates/sim-core/src/lib.rs`
* `crates/sim-core/src/event.rs`
* `crates/sim-core/src/queue.rs`
* `crates/sim-core/src/log.rs`
* `crates/sim-factory/src/lib.rs`
* `crates/sim-factory/src/machines.rs`
* `crates/sim-factory/src/jobs.rs`
* `crates/sim-factory/src/routing.rs`
* `crates/sim-core/tests/determinism.rs`
* `crates/sim-core/tests/event_ordering.rs`

Acceptance criteria:

* The engine can run a fixed scenario with a fixed seed and produce identical final state, KPIs, and event stream across repeated runs.
* Jobs advance through routing in the correct order and machines never process more than one active task at a time unless explicitly configured otherwise.
* Event times are processed in non-decreasing order and invalid state transitions are rejected.

---

### Phase 3. Add the minimal factory flow and economy loop

Objective: Prove the closed-loop relationship between factory capacity, lead time, pricing, demand, and revenue.

Planned work:

1. Implement a minimal product/routing model with 2–3 machine types and 2–3 SKUs in `sim-factory`; these will be new or newly extended files from Phase 2.
2. Implement a simple demand model driven by price and delivery performance in `sim-economy`; these will be net-new files.
3. Add revenue, backlog, throughput, lead-time, and utilization KPIs; these will extend KPI modules added in Phase 2.
4. Create scenario fixtures in `examples/` covering baseline, overload, and capacity-expansion runs; these will be new files.
5. Add scenario acceptance tests that compare expected behavioral outcomes, not just internal state; these test files will be new.

Files expected:

* `crates/sim-factory/src/products.rs`
* `crates/sim-factory/src/process.rs`
* `crates/sim-economy/src/lib.rs`
* `crates/sim-economy/src/demand.rs`
* `crates/sim-economy/src/pricing.rs`
* `crates/sim-core/src/kpi.rs`
* `examples/basic_scenario.json`
* `examples/overload_scenario.json`
* `examples/capacity_expansion_scenario.json`
* `crates/sim-core/tests/scenario_baselines.rs`

Acceptance criteria:

* Lower price increases demand and can create backlog under constrained capacity.
* Higher price reduces load under otherwise identical conditions.
* A bottleneck machine produces observable queue buildup and longer average lead time than the theoretical no-wait baseline.
* Completed production generates revenue exactly once per sale.

---

### Phase 4. Add the command/query surface and simple agent

Objective: Allow controlled external influence over the simulation through explicit APIs and validate that an agent can improve or stabilize outcomes.

Planned work:

1. Implement a narrow command/query interface in `sim-api` and `sim-cli` for loading scenarios, stepping/running the sim, changing price, changing machine count, and toggling the agent; these will be net-new files.
2. Implement a simple in-process `SalesAgent` in `sim-agents` that observes backlog, lead time, and revenue, then adjusts price using approved commands; these will be net-new files.
3. Ensure all commands are validated, logged, and replayable through the event log; these will extend files from Phases 2–3.
4. Add integration tests covering end-to-end agent behavior through the public command path; these will be new test files.

Files expected:

* `crates/sim-api/src/lib.rs`
* `crates/sim-api/src/server.rs`
* `crates/sim-api/src/routes.rs`
* `crates/sim-cli/src/main.rs`
* `crates/sim-agents/src/lib.rs`
* `crates/sim-agents/src/sales_agent.rs`
* `crates/sim-core/tests/agent_integration.rs`
* `crates/sim-api/tests/api_smoke.rs`

Acceptance criteria:

* Users can run a scenario, change price, and inspect updated KPIs through the public interface.
* The agent can observe state and adjust price without direct mutable access to simulation internals.
* Under an overload scenario, the agent produces at least one logged intervention and measurably reduces backlog growth or improves a target KPI relative to a fixed-price baseline.
* Invalid commands are rejected cleanly and do not corrupt simulation state.

---

### Phase 5. Add the minimal single-user experimentation UI

Objective: Provide a lightweight local dashboard that makes experiments visible, comparable, and explainable.

Planned work:

1. Build a minimal UI with controls for scenario selection, run/pause/reset/step, price changes, machine-count changes, and agent toggling; these will be net-new UI files.
2. Add KPI cards, one or more time-series charts, a machine-status table, and an event-log panel; these will be net-new UI files.
3. Add a baseline comparison feature so a user can compare current run versus saved baseline for key KPIs; these will be net-new UI files.
4. Wire the UI to the existing command/query API only; no direct state coupling; these will be new UI integration files.

Files expected:

* `ui/package.json`
* `ui/src/main.tsx`
* `ui/src/App.tsx`
* `ui/src/components/Controls.tsx`
* `ui/src/components/KpiCards.tsx`
* `ui/src/components/MachineTable.tsx`
* `ui/src/components/EventLog.tsx`
* `ui/src/components/Charts.tsx`
* `ui/src/components/BaselineCompare.tsx`

Acceptance criteria:

* A single user can load a built-in scenario, change a lever, run the sim, and see the resulting KPI changes.
* The UI makes the bottleneck visible through queue length, utilization, or lead-time signals.
* A user can inspect the event stream and understand why a KPI changed.
* Baseline-versus-current comparison is visible for at least revenue, backlog, lead time, and throughput.

---

### Phase 6. Add reproducible local deployment and performance validation

Objective: Make the MVP easy to run locally, easy to demo, and measurable under repeatable conditions.

Planned work:

1. Add Dockerfiles for the API and UI, plus `compose.yaml` for API, UI, and Postgres-backed local runs; these will be net-new files.
2. Add `.dockerignore`, `env.example`, and run instructions to the repository docs; these will be new or extended files from Phase 1.
3. Add benchmark scaffolding for scheduler throughput, scenario runtime, and memory-sensitive workload sizes; these benchmark files will be new.
4. Add a `TESTING.md` or equivalent execution guide that explains unit, integration, scenario, and benchmark runs; this will be a new documentation file.

Files expected:

* `Dockerfile`
* `ui/Dockerfile`
* `compose.yaml`
* `.dockerignore`
* `.env.example`
* `TESTING.md`
* `benches/scheduler.rs`
* `benches/scenario_runtime.rs`

Acceptance criteria:

* A new contributor can run the stack locally with native commands for development and with `docker compose up --build` for a reproducible demo path.
* Benchmarks produce repeatable baseline numbers for core event processing and scenario execution.
* The repository clearly documents how to run tests, scenario checks, and local services.

---

## 6. Validation Plan

1. Clone the repository into a clean environment and follow the README exactly to run the native development path.
2. Run formatting, linting, unit tests, integration tests, and scenario acceptance tests from the documented commands.
3. Load the baseline scenario and confirm that repeated runs with the same seed produce identical output.
4. Run an overload scenario at low price and verify that backlog and lead time rise relative to baseline.
5. Increase price manually through the API or UI and verify that demand falls and backlog pressure eases.
6. Enable the `SalesAgent`, rerun the overload scenario, and confirm that at least one agent intervention is logged and that the chosen stabilization KPI improves relative to the fixed-price control run.
7. Use the UI to compare a baseline run versus a changed run and confirm the deltas are visible and consistent with the event log.
8. Run the containerized stack via Compose and confirm that the same built-in scenario is available and produces consistent KPI behavior.
9. Run benchmarks and record baseline throughput/latency numbers for the scheduler and full scenario execution.
10. Review the event log for a sample run and confirm that pricing actions, order creation, job completion, and revenue events form a coherent causal chain.

---

## 7. Implementation Order

1. **Phase 1 — Establish the public repository foundation**
   This comes first because GitHub-readiness, documentation, crate boundaries, and CI should shape all later work.

2. **Phase 2 — Build the deterministic simulation kernel**
   The engine must exist before factory, economy, agent, UI, or deployment layers can be validated.

3. **Phase 3 — Add the minimal factory flow and economy loop**
   This is the first phase that proves the product hypothesis: operations and economics interact meaningfully.

4. **Phase 4 — Add the command/query surface and simple agent**
   The API and agent should be layered on top of a proven closed-loop sim, not invented before the loop exists.

5. **Phase 5 — Add the minimal single-user experimentation UI**
   The UI should expose already-working simulation behavior rather than drive core design prematurely.

6. **Phase 6 — Add reproducible local deployment and performance validation**
   Containerization and benchmark formalization come after the basic stack is already usable and testable.

---

## 8. Out of Scope

* Full multiplayer or MMO infrastructure
* Distributed simulation shards
* Real ERP/MES/CRM integrations
* Photoreal 3D visualization
* Complex workforce movement or floor-layout simulation
* Large-scale optimization engines
* LLM-driven autonomous control loops
* Production-grade authentication/authorization
* Full plugin marketplace or modding system
* Enterprise deployment orchestration beyond local/containerized MVP use

---

## Findings

### Summary

| # | Title | Severity | Dimension | Depends on |
| - | ----- | -------- | --------- | ---------- |
