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

1. Create a Cargo workspace root `Cargo.toml` with `resolver = "2"` and crate directories for `sim-core`, `sim-factory`, `sim-economy`, `sim-agents`, `sim-types`, `sim-cli`, and `sim-api`. Each crate gets a minimal `Cargo.toml` (with `edition = "2021"`) and `src/lib.rs` (or `src/main.rs` for binaries). Add a `rust-toolchain.toml` at the workspace root pinning the Rust stable channel for reproducible builds. Include `serde` (with `derive` feature) and `toml` as dependencies in `sim-core` and `sim-types` for scenario serialization. Include `rand` and `rand_chacha` in `sim-core` for deterministic RNG. Include `tracing` and `tracing-subscriber` in `sim-core` for structured application logging. Under `[dev-dependencies]` in `sim-core`, include `proptest` for property testing and `criterion` for benchmarks (with `[[bench]]` targets). No existing source files to modify. [F6, F7, F13, F17, F18, F19, F31 applied]
2. Add repository health files: `README.md` (expand from current 3-line stub at `README.md:1-3`, using `docs/vision.md` as the authoritative source for project identity and core loop), `CONTRIBUTING.md`, `SECURITY.md`, `CODE_OF_CONDUCT.md`, and `.gitignore`. Augment the existing `docs/architecture-overview.md` with a "Determinism Contract" section documenting ChaCha8Rng, seed propagation, and replay guarantees (do not create a separate root-level `ARCHITECTURE.md`). Also update the Repository Structure diagram in `docs/architecture-overview.md` to reflect the actual crate layout, benchmark locations under `crates/sim-core/benches/`, and the `examples/` directory. `LICENSE` already exists and needs no changes. [F7, F10, F11, F12, F22 applied]
3. Add baseline CI in `.github/workflows/ci.yml` for `cargo fmt --check`, `cargo clippy`, and `cargo test`.
4. Add `examples/` directory with a placeholder file explaining intended contents. The `docs/` directory already exists (contains `vision.md`, `architecture-overview.md`, and `standards-alignment.md`); add a placeholder `docs/README.md` index if needed. [F10, F11, F25 applied]

Files expected:
- `Cargo.toml` (new — workspace root with `resolver = "2"`)
- `rust-toolchain.toml` (new — pin Rust stable channel)
- `crates/sim-core/Cargo.toml` (new — `edition = "2021"`, `[dependencies]`: `serde`, `toml`, `rand`, `rand_chacha`, `tracing`, `tracing-subscriber`; `[dev-dependencies]`: `proptest`, `criterion`), `crates/sim-core/src/lib.rs`
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

1. Define typed IDs (`MachineId`, `ProductId`, `JobId`), simulation time (`SimTime`), shared enums/structs, and a shared error enum `SimError` (with variants for invalid state transitions, unknown IDs, event ordering violations) in `crates/sim-types/src/lib.rs`. Domain concepts should map cleanly to ISA-95 terminology (see `docs/standards-alignment.md`): machines are Equipment, products are Material Definitions, routing steps are Process Segments, and product routings are Operations Definitions. Use Arcogine's own naming in code (`Machine`, `Product`, `RoutingStep`) but include doc-comments noting the ISA-95 correspondence. **Design-for (Phase 7):** Define quantity types as an enum or trait (e.g., `Quantity::Units(u64)` / `Quantity::Volume { liters: f64 }`) rather than a bare integer, so that batch/process manufacturing can use volume-based quantities without rewriting existing discrete-manufacturing code. Reserve a `BatchId` typed ID slot even if the struct is not populated until Phase 7. [F4, F25, F28 applied]
2. Implement event types (order creation, task start, task end, machine availability change, price change, agent decision), a priority-queue-based event scheduler, and deterministic event dispatch in `crates/sim-core/src/event.rs` and `crates/sim-core/src/queue.rs`. Use `ChaCha8Rng` from `rand_chacha` seeded from the scenario configuration for all stochastic decisions. [F7 applied]
3. Implement append-only event logging in `crates/sim-core/src/log.rs`. Define a `Kpi` trait in `crates/sim-core/src/kpi.rs` and implement at least one concrete KPI (e.g., `TotalSimulatedTime` or `EventCount`) sufficient to validate deterministic replay. [F2 applied]
4. Define the scenario file schema (machine definitions, product routings, initial conditions, run parameters, RNG seed) in TOML and implement a scenario loader in `crates/sim-core/src/scenario.rs`. Use TOML section names that correspond to ISA-95 concepts where practical (e.g., `[[equipment]]`, `[[material]]`, `[[process_segment]]`) to ease future data interchange; see `docs/standards-alignment.md` for the mapping. [F1, F7, F25 applied]
5. Define state stores for machines, products, jobs, and work queues using data-oriented structures in `crates/sim-factory/src/machines.rs`, `crates/sim-factory/src/jobs.rs`, and `crates/sim-factory/src/routing.rs`. **Design-for (Phase 7):** Machine definitions should include an optional `capacity` field (defaulting to concurrency=1 for discrete manufacturing) and an optional `setup_time` field (defaulting to zero), so that Phase 7 can add volume-based capacity and cleaning cycles without restructuring the machine model. Routing steps should accept a generic duration rather than assuming instantaneous completion, even if MVP steps use fixed processing times. [F28 applied]
6. Write unit tests for event ordering, monotonic time progression, state transition safety, deterministic replay, and scenario loading in `crates/sim-core/tests/`. Write unit tests for machine state management, job lifecycle, and routing correctness in `crates/sim-factory/tests/machine_state.rs` and `crates/sim-factory/tests/job_routing.rs`. Include error-path tests: invalid state transitions return appropriate `SimError` variants, unknown IDs are rejected, and out-of-order event insertion is handled correctly. [F14, F29 applied]
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

1. Implement a command/query interface in `crates/sim-api/src/routes.rs` and `crates/sim-api/src/server.rs` using Axum + Tokio, supporting: load scenario, step/run sim, change price, change machine count, toggle agent, query KPIs, query event log. Add `axum`, `tokio` (feature: `full`), `tower-http` (features: `trace`, `cors`), `serde_json`, and `utoipa` (with `axum_extras` feature) to `sim-api/Cargo.toml`. Wire `tracing` middleware (via `tower-http`) into the Axum server for structured request logging and error reporting (distinguish application-level observability from simulation event logging). Configure CORS via `tower-http::cors::CorsLayer` — permissive for development (allow all origins), with a note to restrict for production. Generate an OpenAPI 3.x specification from route definitions using `utoipa` and serve it at `/api-docs/openapi.json`; see `docs/standards-alignment.md` for the OpenAPI alignment rationale. In addition to the core command/query routes, implement the following endpoints required by the Phase 5 UI:
   - `GET /api/events/stream` — Server-Sent Events (SSE) endpoint using `axum::response::sse::Sse` that pushes simulation events to the UI in real time during a running simulation. No new dependency required (SSE support is built into Axum). Implement in `crates/sim-api/src/sse.rs`.
   - `GET /api/factory/topology` — returns the machine graph (nodes with state and queue depth, edges with routing connections and in-transit counts) as JSON for the factory flow visualization.
   - `GET /api/jobs` — returns active and completed jobs/orders with fields: job ID, product, status, current step, time in system, revenue.
   - `GET /api/export/events` — returns the full event log as a JSON array for client-side download/export.
   [F3, F17, F23, F26, F30 applied]
2. Wire `crates/sim-cli/src/main.rs` as the single binary entrypoint supporting both headless CLI mode and HTTP server mode (e.g., `arcogine run --headless` vs `arcogine serve`). `sim-api` remains a library crate providing route handlers and server setup; `sim-cli` depends on it and hosts the binary. [F16 applied]
3. Implement a `SalesAgent` in `crates/sim-agents/src/sales_agent.rs` that observes backlog, lead time, and revenue, then adjusts price using approved commands. The agent architecture must support future agent types (Planning, Procurement, Maintenance) and future LLM-based strategy agents, so the interface should be trait-based and agent-type-agnostic.
4. Ensure all commands are validated, logged in the event log, and replayable. Modify `crates/sim-core/src/log.rs` and `crates/sim-core/src/event.rs` as needed.
5. Write integration tests in `crates/sim-api/tests/api_smoke.rs` (using `tower::ServiceExt` to test routes without starting an HTTP server) and `crates/sim-core/tests/agent_integration.rs`. Write unit tests for the agent trait and `SalesAgent` decision logic in `crates/sim-agents/tests/sales_agent.rs`. API smoke tests must include error-path cases: malformed requests return appropriate HTTP error codes, commands on non-running simulations are rejected, and invalid scenario references produce typed errors. [F24, F29, F34 applied]

Files expected:
- `crates/sim-api/src/lib.rs` (modify), `crates/sim-api/src/server.rs`, `crates/sim-api/src/routes.rs` (new)
- `crates/sim-api/src/sse.rs` (new — SSE event stream handler for `GET /api/events/stream`)
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
- The SSE endpoint streams simulation events to connected clients during a running simulation; disconnection and reconnection are handled gracefully.
- The topology, jobs, and export endpoints return well-typed JSON responses consistent with the OpenAPI spec.

---

### Phase 5. Add the single-user experimentation UI

Objective: Provide a structured local dashboard that makes experiments visible, comparable, explainable, and accessible — acting as an experiment console, not a game client.

#### 5.1 Technology stack

| Concern | Choice | Rationale |
|---------|--------|-----------|
| Framework | React 18 + TypeScript | Lightweight, familiar, web-accessible (per `docs/architecture-overview.md`) |
| Bundler | Vite | Fast HMR, native ESM, zero-config TypeScript |
| Component library | Tailwind CSS + shadcn/ui | Composable, dark-mode ready, accessible primitives with WCAG 2.1 AA contrast by default |
| Charting | Recharts | React-native SVG charts, good TypeScript support, built-in PNG export |
| State management | Zustand | Minimal boilerplate, works well with polling and SSE-based state updates |
| API client | Typed `fetch` wrapper | Consume typed responses aligned to the OpenAPI spec generated in Phase 4 (`utoipa`) |
| Live updates | EventSource (SSE) | Subscribe to `GET /api/events/stream` (added in Phase 4) for real-time simulation event delivery; fall back to REST polling when paused |
| E2E testing | Playwright | Browser automation for smoke tests; added to CI |

#### 5.2 Screen layout and information hierarchy

The UI follows a three-region layout with a clear reading order: status at the top, trends and operations in the center, raw data at the bottom.

```text
+-----------------------------------------------------------------------+
| TOOLBAR                                                               |
| [Scenario: v] [> Run] [|| Pause] [Step] [Reset] [1x v] [Agent: ON]  |
+-----------------------------------------------------------------------+
|                                            |                          |
|  MAIN AREA (left, wider)                   |  SIDEBAR (right, narrow) |
|                                            |                          |
|  [Revenue]  [Backlog]  [Lead T]  [Thru]    |  CONTROLS                |
|   $12,400    23 jobs    4.2 hrs   8/hr     |  Price: [====o===]       |
|                                            |  Machines: [- 3 +]       |
|  +--------------------------------------+  |                          |
|  | Time-Series Chart                    |  |  BASELINE COMPARE        |
|  |  --- Revenue  --- Throughput         |  |  Rev:  +12% ▲            |
|  |  --- Lead Time                       |  |  Back: -8%  ▼            |
|  |                                      |  |  Lead: -15% ▼            |
|  +--------------------------------------+  |  Thru: +5%  ▲            |
|                                            |                          |
|  [Factory Flow] [Machines] [Jobs]          |  [Save Baseline]         |
|  +--------------------------------------+  |  [Export ▾]              |
|  | Mill --[5]--> Lathe --[2]--> QC      |  |                          |
|  | (busy)        (busy)      (idle)     |  |                          |
|  +--------------------------------------+  |                          |
+-----------------------------------------------------------------------+
| BOTTOM DRAWER (collapsible)                                           |
| ▾ Event Log  [Filter: All ▾] [Search...]                             |
| 14:32  OrderCreated   Order #47, Product A, qty 10                   |
| 14:33  TaskStarted    Job #47-1, Mill-1                              |
| 14:38  PriceChange    Agent set price $12.50 → $13.00               |
+-----------------------------------------------------------------------+
```

**Toolbar** — Scenario selector dropdown, simulation controls (run/pause/step/reset), simulation speed multiplier, agent toggle. All actions dispatch REST commands to Phase 4's API.

**Main area (left column, ~70% width):**
- KPI summary cards (revenue, backlog, lead time, throughput) with trend micro-indicators (up/down/flat vs. previous snapshot).
- Time-series chart: multi-line Recharts plot of KPI values over simulation time. Supports zoom and pan.
- Tabbed lower section with three views:
  - **Factory Flow** — directed-graph topology of the production flow. Nodes represent machines (colored by state: idle/busy/offline; sized or badged by queue depth). Edges represent routing connections with in-transit/waiting job counts. Implemented with SVG; no heavy graph library required for 2–3 machine types.
  - **Machines** — table of per-machine state, current job, queue depth, and utilization percentage.
  - **Jobs** — table of active and completed jobs/orders: Job ID, Product, Status, Current Step, Time in System, Revenue. Sortable and filterable. Clicking a job filters the event log to that job's events.

**Sidebar (right column, ~30% width):**
- Control levers: price slider, machine-count stepper. Changes fire REST commands and are reflected after the next simulation step.
- Baseline comparison panel: shows current-vs-saved deltas for revenue, backlog, lead time, and throughput with green/red directional indicators. "Save as baseline" button captures the current run's final KPIs and scenario config (up to 3 baselines, in-memory, session-scoped).
- Export menu: KPI summary as CSV, event log as JSON, current chart as PNG.

**Bottom drawer (collapsible):**
- Chronological event log with type filter (dropdown: All / Orders / Production / Pricing / Agent) and text search. New events stream in via SSE during a running simulation.

#### 5.3 Data flow

```text
Phase 4 Axum API
  │
  ├── REST endpoints (query KPIs, load scenario, change levers, list jobs, get topology)
  │     │
  │     └──► ui/src/api/client.ts (typed fetch wrapper)
  │               │
  │               └──► Zustand stores (simulation.ts, baselines.ts)
  │                         │
  │                         └──► React components (read from store, dispatch via client)
  │
  └── SSE endpoint (GET /api/events/stream)
        │
        └──► ui/src/api/sse.ts (EventSource wrapper)
                  │
                  └──► Zustand simulation store (append events, update KPIs in real time)
```

- All mutations flow through the REST API. The UI never holds simulation state — it mirrors what the API reports.
- During a running simulation, the SSE stream pushes events. The Zustand store appends them and recomputes derived state (KPI deltas, trend indicators). When the simulation is paused or stopped, the UI queries REST for the final snapshot.
- Baseline data is held client-side in a separate Zustand store (session-scoped, not persisted).

#### 5.4 First-run experience

A `WelcomeOverlay` component appears when no scenario is loaded:
- Displays the three built-in scenarios (basic, overload, capacity expansion) as cards with a one-sentence description each.
- A "Quick start" button loads the basic scenario and auto-runs it for 100 ticks, immediately populating the dashboard.
- Dismissable; not shown again after the first scenario load within the session.

#### 5.5 Error handling and loading states

- Skeleton loaders (shadcn/ui `Skeleton`) for KPI cards, charts, and tables during initial load and scenario transitions.
- Toast notifications (shadcn/ui `Toast`) for API errors, with the HTTP status and a human-readable message.
- Controls are disabled when no scenario is loaded. The price slider and machine-count stepper are disabled during an active run if the API rejects mid-run mutations (depends on Phase 4 design).
- A connection-lost banner appears if the API becomes unreachable (detected by SSE `onerror` or a periodic health-check ping).

#### 5.6 Accessibility baseline

- All interactive elements are keyboard-navigable (shadcn/ui provides this by default).
- Charts include `aria-label` attributes with current KPI values for screen readers.
- Machine states use both color and icon/text labels (not color alone) to convey state.
- Minimum contrast ratios per WCAG 2.1 AA (Tailwind + shadcn defaults satisfy this).

#### 5.7 Export capability

The export menu in the sidebar provides:
- **KPI summary as CSV** — downloads a CSV file with columns: KPI name, value, unit, delta-vs-baseline.
- **Event log as JSON** — calls `GET /api/export/events` (Phase 4) and triggers a browser download of the full event log.
- **Chart as PNG** — uses Recharts' built-in `toDataURL` export to save the current chart view.

Planned work:

1. Scaffold a TypeScript/React project in `ui/` with Vite. Install Tailwind CSS, shadcn/ui, Recharts, and Zustand. Configure `package.json`, `tsconfig.json`, `vite.config.ts`, `tailwind.config.ts`, `postcss.config.js`, and `index.html`.
2. Implement the typed API client in `ui/src/api/client.ts` (wrapping `fetch` with typed request/response interfaces aligned to the OpenAPI spec) and the SSE client in `ui/src/api/sse.ts` (wrapping `EventSource` with typed event parsing and reconnection logic).
3. Implement Zustand stores: `ui/src/stores/simulation.ts` (simulation state, KPIs, event log, connection status) and `ui/src/stores/baselines.ts` (saved baseline snapshots, comparison deltas).
4. Build the layout shell in `ui/src/App.tsx`: toolbar, two-column main area, collapsible bottom drawer.
5. Build layout components: `ui/src/components/layout/Toolbar.tsx` (scenario selector, sim controls, speed, agent toggle), `ui/src/components/layout/Sidebar.tsx` (levers panel, baseline comparison, export menu), `ui/src/components/layout/BottomDrawer.tsx` (collapsible event log container with filter and search).
6. Build dashboard components: `ui/src/components/dashboard/KpiCards.tsx` (four summary cards with trend micro-indicators), `ui/src/components/dashboard/TimeSeriesChart.tsx` (multi-line Recharts KPI chart with zoom/pan), `ui/src/components/dashboard/FactoryFlow.tsx` (SVG directed-graph topology of machines and routing), `ui/src/components/dashboard/MachineTable.tsx` (per-machine state, queue depth, utilization), `ui/src/components/dashboard/JobTracker.tsx` (active/completed jobs table, sortable, filterable, click-to-filter event log).
7. Build experiment components: `ui/src/components/experiment/BaselineCompare.tsx` (structured comparison with deltas and directional indicators, save/load up to 3 baselines), `ui/src/components/experiment/ExportMenu.tsx` (CSV, JSON, PNG export triggers).
8. Build the onboarding overlay: `ui/src/components/onboarding/WelcomeOverlay.tsx` (scenario cards, quick-start button).
9. Build shared components: `ui/src/components/shared/ErrorBoundary.tsx`, `ui/src/components/shared/SkeletonLoader.tsx`, `ui/src/components/shared/Toast.tsx`.
10. Wire all UI interactions to the REST API and SSE stream from Phase 4 — no direct state coupling. [F5 applied]
11. Add Playwright e2e smoke tests in `ui/e2e/smoke.spec.ts` that verify: scenario load and KPI display, lever change reflected in KPIs, event log populates during a run, factory flow diagram renders machine nodes, export button produces a downloadable file. Add `ui/playwright.config.ts` and the Playwright test runner to CI.

Files expected:
- `ui/package.json`, `ui/tsconfig.json`, `ui/vite.config.ts`, `ui/tailwind.config.ts`, `ui/postcss.config.js`, `ui/index.html` (new)
- `ui/src/main.tsx`, `ui/src/App.tsx` (new)
- `ui/src/api/client.ts` (new — typed API client wrapper)
- `ui/src/api/sse.ts` (new — EventSource wrapper for live updates)
- `ui/src/stores/simulation.ts` (new — Zustand store for sim state, KPIs, events)
- `ui/src/stores/baselines.ts` (new — Zustand store for saved baselines)
- `ui/src/components/layout/Toolbar.tsx`, `ui/src/components/layout/Sidebar.tsx`, `ui/src/components/layout/BottomDrawer.tsx` (new)
- `ui/src/components/dashboard/KpiCards.tsx`, `ui/src/components/dashboard/TimeSeriesChart.tsx`, `ui/src/components/dashboard/FactoryFlow.tsx`, `ui/src/components/dashboard/MachineTable.tsx`, `ui/src/components/dashboard/JobTracker.tsx` (new)
- `ui/src/components/experiment/BaselineCompare.tsx`, `ui/src/components/experiment/ExportMenu.tsx` (new)
- `ui/src/components/onboarding/WelcomeOverlay.tsx` (new)
- `ui/src/components/shared/ErrorBoundary.tsx`, `ui/src/components/shared/SkeletonLoader.tsx`, `ui/src/components/shared/Toast.tsx` (new)
- `ui/e2e/smoke.spec.ts` (new — Playwright tests)
- `ui/playwright.config.ts` (new)

Acceptance criteria:
- A single user can load a built-in scenario, change a lever, run the simulation, and see KPI changes reflected in real time.
- The UI makes bottlenecks visible through queue length, utilization, or lead-time indicators.
- A user can inspect the event stream and trace why a KPI changed.
- Baseline-versus-current comparison is visible for at least revenue, backlog, lead time, and throughput.
- Playwright e2e smoke tests pass in CI, verifying scenario load and KPI display.
- KPI cards and charts update live during a running simulation via SSE without requiring manual refresh.
- The factory flow diagram visually distinguishes idle, busy, and offline machines and shows queue depth.
- A first-time user can reach a populated dashboard within two clicks from the welcome screen.
- All interactive controls are keyboard-accessible.
- KPI data and event logs can be exported for external analysis (CSV, JSON, PNG).

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

### Phase 7. Extend to batch/process manufacturing (post-MVP)

> **Status:** Post-MVP extension. Phases 1–6 must be complete and stable before this phase begins. This phase is included in the plan to ensure MVP design decisions preserve extensibility.

Objective: Extend Arcogine from discrete manufacturing to batch and process manufacturing, using gin distillery production as the reference scenario. Prove that the simulation engine can model material transformation, volume-based production, recipe-driven processes, and multi-stage inventory — unlocking an entirely new class of industrial scenarios.

**Why gin distillery:** Gin production exercises every gap between discrete and process manufacturing in a single, concrete, end-to-end flow: batch-based liquid processing, time-based transformations (infusion, distillation), material conversion with yield/loss, volume tracking (liters, not units), specialized equipment (stills, tanks, bottling lines), multi-level inventory (raw materials → intermediates → finished goods → packaging), and regulatory constraints (excise duty, batch traceability). If Arcogine can simulate a gin distillery, it can simulate most batch/process manufacturing scenarios.

**Reference scenario flow:**

```text
Botanicals + Neutral Spirit (raw materials)
        ↓
Infusion Tank (8h, batch, volume-based)
        ↓
Still (6h, pot or column, capacity-constrained)
        ↓
Dilution Tank (2h, blending to target ABV)
        ↓
Bottling Line (fast, discrete unit output)
        ↓
Finished Goods Inventory
```

Planned work:

1. **Batch entity and volume tracking.** Extend `sim-types` with `BatchId`, volume quantities (liters), and batch state lifecycle (created → processing → completed → consumed). Batches replace jobs as the primary work unit for process manufacturing. The job/batch distinction should be modeled via a trait or enum so both discrete jobs and process batches coexist in the same simulation. [F28 applied]
2. **Material and recipe system (`sim-material` crate).** New crate for: material definitions (ingredients with quantity types — volume, mass, count), bill of materials (BOM) / recipe definitions (input materials → output material with ratios, yield, and waste), and inventory state stores (raw materials, intermediates, finished goods, packaging). Track inventory levels by material and location. ISA-95 correspondence: Material Definition, Material Lot, Bill of Material.
3. **Process-step model with time-based transformation.** Extend the routing model in `sim-factory` to support process steps that: occupy equipment for a defined duration (not instantaneous), transform input materials into output materials with yield and loss, respect equipment capacity (liters, not just concurrency), and support cooldown/cleaning cycles between batches. Each step is a DES event pair (process-start, process-end) with the equipment locked for the duration.
4. **Equipment specialization.** Extend `sim-factory` machine model with: capacity (volume, e.g., liters for tanks/stills), equipment type constraints (which process steps can run on which equipment), and setup/cleaning time between batches. Reference equipment types for gin: infusion tank, still (pot/column), dilution tank, bottling line.
5. **Multi-stage lead time and cost modeling.** Extend `sim-economy` with: multi-component cost structures (raw material cost, energy, labor, packaging, excise duty), cost accumulation through production stages, and multi-stage lead time tracking (production delay + holding time + bottling delay). Lead time feeds back into demand as in the MVP.
6. **Supply chain layer (minimal).** Add supplier entities to `sim-economy` or a new `sim-supply` module: supplier lead times for raw materials, purchase order events, and price variability for inputs. This closes the loop: supply constraints → production constraints → delivery performance → demand.
7. **Gin distillery scenario fixtures.** Create TOML scenario files in `examples/` for: a baseline gin distillery (single still, balanced throughput), a bottleneck scenario (still is the constraint), and a scaling scenario (add a second still or larger tank). These use the extended schema from tasks 1–6.
8. **Tests.** Unit tests for batch lifecycle, material/recipe validation, volume tracking, and inventory constraints in `crates/sim-material/tests/`. Scenario acceptance tests validating: still is the bottleneck (queue builds), batch sizing affects throughput, raw material depletion halts production, and cost model produces correct margin calculations. Property tests verifying: material conservation (inputs consumed = outputs produced + waste), no negative inventory, batch integrity (no partial consumption).

Files expected:
- `crates/sim-types/src/lib.rs` (modify — add `BatchId`, volume types, batch lifecycle states)
- `crates/sim-material/Cargo.toml`, `crates/sim-material/src/lib.rs`, `crates/sim-material/src/recipe.rs`, `crates/sim-material/src/inventory.rs` (new crate)
- `crates/sim-factory/src/machines.rs`, `crates/sim-factory/src/routing.rs` (modify — add capacity, equipment types, process durations, cleaning cycles)
- `crates/sim-economy/src/cost.rs`, `crates/sim-economy/src/supply.rs` (new modules)
- `examples/gin_baseline.toml`, `examples/gin_bottleneck.toml`, `examples/gin_scaling.toml` (new)
- `crates/sim-material/tests/recipe_validation.rs`, `crates/sim-material/tests/inventory.rs` (new)
- `crates/sim-core/tests/gin_scenario.rs` (new — scenario acceptance tests)

Acceptance criteria:
- A gin distillery scenario runs to completion with material transformation: input botanicals + neutral spirit are consumed, output gin volume is produced with defined yield/loss.
- The still is the bottleneck: queue buildup is observable, and adding capacity (second still) measurably increases throughput.
- Batch sizing decisions affect throughput and lead time: smaller batches reduce lead time but lower utilization; larger batches increase utilization but lengthen cycle time.
- Raw material depletion halts production until resupply; supplier lead time affects production continuity.
- Cost model produces per-batch and per-liter cost breakdowns; revenue minus cost yields correct margin.
- Material conservation holds as a property-test invariant: total input volume = total output volume + total waste, within floating-point tolerance.
- All MVP phases (1–6) acceptance criteria continue to pass — discrete manufacturing scenarios are unaffected by the batch/process extensions.

**What is NOT in Phase 7:**
- Quality metrics / batch variability / rejection-rework (deferred)
- Regulatory compliance steps / excise duty calculation (deferred — noted as a Phase 7+ extension)
- Multi-factor demand models (seasonality, brand, distribution) — deferred
- Multi-site / multi-distillery scenarios — deferred
- Aging/maturation time for spirits that require it (e.g., whisky) — deferred

---

## 6. Validation Plan

### 6.1 Simulation engine and API (Phases 1–4)

1. Clone the repository into a clean environment and follow `README.md` to run the native development path.
2. Run `cargo fmt --check`, `cargo clippy`, `cargo test` and verify zero failures.
3. Load the baseline scenario via CLI and confirm that repeated runs with the same seed produce byte-identical output.
4. Run the overload scenario at low price and verify that backlog and lead time rise relative to the baseline scenario.
5. Increase price through the REST API and verify that demand falls and backlog pressure eases.
6. Enable the `SalesAgent`, rerun the overload scenario, and confirm that at least one agent intervention is logged and the target KPI improves relative to the fixed-price control.
7. Verify the SSE endpoint (`GET /api/events/stream`) streams events to a connected client during a running simulation and stops cleanly when the simulation pauses or ends.
8. Verify that `/api/factory/topology`, `/api/jobs`, and `/api/export/events` return well-typed JSON responses consistent with the OpenAPI spec at `/api-docs/openapi.json`.

### 6.2 Experiment console UI (Phase 5)

9. Open the UI and confirm the welcome overlay appears with scenario cards and a quick-start button.
10. Load a scenario via the welcome overlay's quick-start button and confirm the dashboard populates within two clicks.
11. Adjust the price slider and confirm the KPI cards and time-series chart reflect the change after the next simulation step.
12. Run the simulation and confirm that KPI cards, the time-series chart, and the event log update live via SSE without manual refresh.
13. Switch to the factory flow tab and confirm that machine nodes show state (idle/busy/offline) with both color and icon/text, and that queue depth is visible on edges or nodes.
14. Switch to the jobs tab and confirm that active and completed jobs are listed with correct fields. Click a job and confirm the event log filters to that job's events.
15. Save a baseline, modify a lever, rerun, and confirm the baseline comparison panel shows deltas with directional indicators for revenue, backlog, lead time, and throughput.
16. Use the export menu to download KPI summary as CSV, event log as JSON, and chart as PNG. Verify each file is valid and contains expected data.
17. Navigate the entire UI using only the keyboard (Tab, Enter, Space, Escape) and confirm all interactive controls are reachable and operable.
18. Playwright e2e smoke tests pass in CI, covering: scenario load, KPI display, lever change, event log population, factory flow rendering, and export.

### 6.3 Deployment and performance (Phase 6)

19. Run the containerized stack via `docker compose up --build` and confirm the same scenario produces consistent KPI behavior.
20. Run benchmarks via `cargo bench` and record baseline throughput/latency numbers.
21. Review the event log for a sample run and confirm pricing actions, order creation, job completion, and revenue events form a coherent causal chain.

---

## 7. Implementation Order

1. **Phase 1 — Establish the public repository foundation.** GitHub-readiness, crate boundaries, docs, and CI must shape all subsequent work. No simulation logic depends on this phase's acceptance criteria.
2. **Phase 2 — Build the deterministic simulation kernel.** The engine must exist before factory, economy, agent, or UI layers can be tested. Phase 1 acceptance criteria remain passing.
3. **Phase 3 — Add the minimal factory flow and economy loop.** First phase that proves the product hypothesis: operations and economics interact meaningfully. Phases 1–2 acceptance criteria remain passing.
4. **Phase 4 — Add the command/query surface and simple agent.** The API and agent layer on top of a proven closed-loop simulation. Phases 1–3 acceptance criteria remain passing.
5. **Phase 5 — Add the minimal single-user experimentation UI.** The UI exposes already-working simulation behavior. Phases 1–4 acceptance criteria remain passing.
6. **Phase 6 — Add reproducible local deployment and performance validation.** Containerization and benchmarks formalize what is already usable. All prior phase acceptance criteria remain passing.
7. **Phase 7 — Extend to batch/process manufacturing (post-MVP).** Introduces material transformation, volume tracking, recipes, equipment specialization, and a multi-stage cost model using gin distillery as the reference scenario. All prior phase acceptance criteria remain passing; discrete manufacturing scenarios are unaffected.

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
- Batch/process manufacturing, material transformation, and recipe systems (deferred to Phase 7; MVP models discrete manufacturing only)
- Volume-based production and multi-level inventory management (deferred to Phase 7)
- Equipment specialization beyond generic machine types (deferred to Phase 7)
- Multi-component cost structures, excise duty, and regulatory compliance (deferred to Phase 7+)
- Quality metrics, batch variability, and rejection/rework modeling (deferred to Phase 7+)
- Multi-site / multi-distillery scenarios (deferred beyond Phase 7)
- Aging/maturation time modeling for long-cycle products (deferred beyond Phase 7)

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
12. **Batch/process manufacturing (Phase 7)** — Material transformation, volume-based production, recipe/BOM systems, equipment specialization, and multi-level inventory. Gin distillery as the reference scenario; generalizable to food & beverage, chemical, and pharmaceutical batch manufacturing. See Phase 7 for the detailed plan.
13. **Regulatory and compliance modeling** — Excise duty calculation, batch traceability, production limits, and audit trails for regulated industries (alcohol, pharma, food). Builds on Phase 7 material tracking.
14. **Quality and variability modeling** — Batch-level quality metrics (ABV, flavor profile), acceptance/rejection criteria, rework flows, and statistical process control.
15. **Multi-factor demand models** — Extend beyond price-driven demand to include seasonality, brand equity, distribution channels, and market trends.
16. **Multi-site operations** — Multiple factories/distilleries sharing a common market, with inter-site transfer logistics and centralized planning agents.

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

### F28: No batch/process manufacturing extensibility; gin distillery use case unaddressed [Applied]
<!-- severity: major -->
<!-- dimension: gaps -->

**Context:** The MVP plan models discrete manufacturing only (jobs with unit counts advancing through machine routing steps). A concrete use case — gin distillery production — requires batch/process manufacturing: material transformation, volume-based production, recipes with yield/loss, time-based reactions, specialized equipment (stills, tanks), multi-level inventory, and multi-component cost structures. None of these are addressed in Phases 1–6, and the MVP typed IDs, machine model, and routing model could block future extension if designed too narrowly.

**Issue:** Without a documented extensibility path, the MVP risks designing typed IDs as unit-only, machines as concurrency-only (no volume capacity), and routing steps as instantaneous (no duration). These choices would require significant restructuring to support batch/process manufacturing. The gin distillery use case is the first concrete scenario that validates Arcogine's generality beyond discrete manufacturing.

**Recommendation:** Add Phase 7 (post-MVP) documenting the full batch/process manufacturing extension with gin distillery as the reference scenario. Add design-for notes in Phase 2 typed IDs (quantity enum supporting units and volumes, reserved `BatchId`), machine definitions (optional volume capacity, setup/cleaning time), and routing steps (generic duration). Update Out of Scope and Future Directions. Update `docs/architecture-overview.md` with extensibility section.

**Choices:**
- [x] Add Phase 7 with design-for notes in Phase 2, keeping MVP scope unchanged
- [ ] Merge batch/process manufacturing into the MVP (excessive scope growth)
- [ ] Defer entirely without documenting design-for constraints (risks MVP design blocking extension)

---

### F29: No error-path or negative tests planned [Applied]
<!-- severity: major -->
<!-- dimension: testing -->

**Context:** Phase 2 acceptance criteria (line 122) say "invalid state transitions are rejected with `SimError` variants." Phase 4 acceptance criteria (line 182) say "Invalid commands are rejected with typed errors and do not corrupt simulation state." No test file in any phase explicitly targets error paths — all planned tests (`determinism.rs`, `event_ordering.rs`, `scenario_loading.rs`, `machine_state.rs`, `job_routing.rs`, `scenario_baselines.rs`, `demand_model.rs`, `pricing.rs`, `api_smoke.rs`, `agent_integration.rs`, `sales_agent.rs`) are named for happy-path validation.

**Issue:** Acceptance criteria that mention error handling are untestable if no tests exercise invalid inputs, unknown IDs, out-of-order events, or malformed commands. A coding agent may skip error-path logic entirely if no test demands it.

**Recommendation:** Add explicit error-path test expectations to Phase 2 and Phase 4. Phase 2: `crates/sim-core/tests/event_ordering.rs` and `crates/sim-factory/tests/machine_state.rs` should include cases for invalid state transitions, unknown IDs, and out-of-order events. Phase 4: `crates/sim-api/tests/api_smoke.rs` should include cases for malformed requests, invalid scenario IDs, and commands on non-running simulations. No new test files needed — extend existing ones.

**Choices:**
- [x] Add error-path test expectations to Phase 2 and Phase 4 test descriptions
- [ ] Create separate `error_paths.rs` test files
- [ ] Defer error-path testing to post-MVP

---

### F30: No CORS configuration for API–UI communication [Applied]
<!-- severity: major -->
<!-- dimension: gaps -->

**Context:** Phase 4 creates an Axum HTTP API, and Phase 5 creates a React/Vite UI that calls this API. In development, Vite serves the UI on a different port than the Axum API. No phase mentions CORS (Cross-Origin Resource Sharing) configuration.

**Issue:** Without CORS headers, the browser will block all API requests from the UI. The React app will not function during development or in the containerized setup (where API and UI are separate services at different origins). This is a blocking issue for Phase 5.

**Recommendation:** Add CORS middleware configuration to Phase 4 task 1 using `tower-http`'s `CorsLayer`. Specify permissive CORS for development (allow all origins) with a note to restrict in production. `tower-http` is already a dependency (F17/F23 applied).

**Choices:**
- [x] Add CORS via `tower-http::cors::CorsLayer` in Phase 4 task 1
- [ ] Use Vite proxy to avoid CORS in development (still need CORS in production)
- [ ] Defer to Phase 5 when the UI is built

---

### F31: Workspace `Cargo.toml` missing `resolver = "2"` specification [Applied]
<!-- severity: major -->
<!-- dimension: best-practices -->

**Context:** Phase 1 task 1 (line 72) creates a workspace root `Cargo.toml`. The plan does not specify the Rust edition or the resolver version. Since Rust edition 2021 (the current default and the edition any new project in 2026 would use), virtual workspaces require `resolver = "2"` to be explicitly set in the workspace `Cargo.toml`. Without it, Cargo uses resolver 1, which has incorrect feature unification behavior in workspaces.

**Issue:** A workspace `Cargo.toml` without `resolver = "2"` will produce Cargo warnings and may cause subtle dependency resolution bugs. This is a well-known Rust workspace gotcha that a coding agent may miss if not instructed.

**Recommendation:** Phase 1 task 1 should specify that the workspace `Cargo.toml` includes `resolver = "2"` (or the workspace members have `edition = "2021"` which implies resolver 2). Also specify the Rust edition (`edition = "2021"` in each crate) and add a `rust-toolchain.toml` to pin the Rust version for reproducibility.

**Choices:**
- [x] Add `resolver = "2"`, `edition = "2021"`, and `rust-toolchain.toml` to Phase 1 task 1
- [ ] Assume the coding agent knows Rust 2021 conventions
- [ ] Skip `rust-toolchain.toml` and only specify resolver

---

### F32: Re-sweep artifacts and duplicate summary tables clutter the plan for coding agent execution [Applied]
<!-- severity: major -->
<!-- dimension: plan-hygiene -->

**Context:** The Findings section contains five re-sweep reports (lines 790–812, 800–812, 880–892, 945–957, 1011–1023), two separate `### Summary` tables (lines 816–842 and 896–924), and `[Applied]` tags on every finding title (F1–F28). These are review-process artifacts from the iteration that produced the findings.

**Issue:** A coding agent executing the plan will encounter ~230 lines of review-process narrative that provides no implementation guidance. The two summary tables are nearly identical (the second is a superset of the first). The `[Applied]` tags and `<!-- severity / dimension -->` HTML comments are useful for the reviewer but noise for the executor. The re-sweep reports say "pass" for everything — they confirm no issues but add no information.

**Recommendation:** Consolidate the findings section: keep one summary table (the final, most complete one), remove the intermediate re-sweep reports, and keep the `[Applied]` tags since they signal which findings were already incorporated into the plan text. The severity/dimension HTML comments should stay — they're invisible in rendered markdown and useful metadata.

**Choices:**
- [x] Remove intermediate re-sweep reports and the first (subset) summary table; keep final summary and all finding details
- [ ] Remove all findings except the summary table (loses context)
- [ ] Leave as-is (acceptable but noisy)

---

### F33: `§7 Implementation Order` is redundant with `§5 Phased Plan`
<!-- severity: minor -->
<!-- dimension: plan-hygiene -->

**Context:** `§7 Implementation Order` (lines 321–329) restates the phase sequence that is already implicit in `§5 Phased Plan` (phases are numbered 1–7 and each phase's introduction states its prerequisites). The only additional content is the "Phases 1–N acceptance criteria remain passing" note on each item.

**Issue:** A coding agent reading both sections gets the same information twice. The "acceptance criteria remain passing" constraint is important but could be stated once as a general rule rather than repeated seven times.

**Recommendation:** Replace the per-phase repetition in §7 with a single general rule: "Each phase must leave all prior phases' acceptance criteria passing. Phases are executed in order (1 through 7); no phase may begin until its predecessor's acceptance criteria are met." Then §7 becomes a brief statement rather than a duplicate list.

**Choices:**
- [x] Condense §7 to a general rule and a brief ordered list
- [ ] Remove §7 entirely and add the "prior phases pass" rule to §5's introduction
- [ ] Leave as-is

---

### F34: Phase 4 API smoke tests need `tokio` test runtime
<!-- severity: minor -->
<!-- dimension: testing -->

**Context:** Phase 4 task 5 (line 168) plans `crates/sim-api/tests/api_smoke.rs` which will test Axum routes. Axum tests require `#[tokio::test]` to run async handlers. Phase 4 task 1 (line 164) adds `tokio` to `sim-api/Cargo.toml` under `[dependencies]`, but integration tests in the `tests/` directory also need `tokio` available — which it is, since it's a direct dependency. However, if the tests use `reqwest` or a test client for HTTP-level testing, that dependency is not listed.

**Issue:** If API smoke tests use an HTTP client (e.g., `reqwest`, `hyper::Client`, or Axum's `TestClient` from `axum-test`) to make actual HTTP requests, those crates need to be in `[dev-dependencies]`. If tests use Axum's `Router` directly via `tower::ServiceExt`, no additional deps are needed. The plan doesn't specify the testing approach.

**Recommendation:** Specify that Phase 4 API smoke tests use Axum's `tower::ServiceExt` (`oneshot` method) to test routes without starting an HTTP server. This avoids additional dependencies and port allocation in tests. If full HTTP-level tests are desired, add `reqwest` as a dev-dependency.

**Choices:**
- [x] Specify `tower::ServiceExt`-based route testing in Phase 4 (no extra deps)
- [ ] Add `reqwest` as dev-dependency for HTTP-level testing
- [ ] Leave unspecified

---

### F35: No Rust edition or MSRV specified
<!-- severity: minor -->
<!-- dimension: best-practices -->
<!-- Depends on: F31 choice 1 -->

**Context:** No phase specifies the Rust edition for crate `Cargo.toml` files or a minimum supported Rust version (MSRV). The plan uses features (async/await, edition 2021 paths) that require at least Rust 1.56 (edition 2021), and dependencies like `axum` may require newer versions.

**Issue:** Without an edition and MSRV, different contributors may target different Rust versions, causing CI failures or incompatible code. Pinning the toolchain ensures reproducibility — a core plan value.

**Recommendation:** Covered by F31 — `rust-toolchain.toml` and `edition = "2021"` in Phase 1.

**Choices:**
- [x] Covered by F31
- [ ] Skip MSRV pinning

---

### Re-sweep 7 (post-F29–F35 application)

All five dimensions re-swept against the updated plan:

| Dimension | Result |
|-----------|--------|
| **testing** | Pass — error-path tests added to Phase 2 and Phase 4 descriptions (F29); API smoke test approach specified as `tower::ServiceExt` (F34); all crates with logic have unit tests; property tests, e2e tests, and benchmarks all planned. |
| **correctness** | Pass — §3.1 matches actual repo (6 files); all doc references verified against `docs/vision.md`, `docs/architecture-overview.md`, `docs/standards-alignment.md`; ISA-95 mappings match standards doc; Phase 4 deps include `tower-http` with `cors` feature; workspace `Cargo.toml` specifies `resolver = "2"`; `rust-toolchain.toml` added to Phase 1. |
| **gaps** | Pass — CORS configured for API–UI communication (F30); error handling strategy, scenario format, REST framework, CLI framework, observability, KPI boundary, and all dependency declarations specified. No new functionality gaps. |
| **best-practices** | Pass — `resolver = "2"` and `edition = "2021"` specified (F31); `rust-toolchain.toml` pins Rust version; CI includes fmt/clippy/test; dev-dependencies separated; docs reference existing authoritative files. |
| **plan-hygiene** | Pass — intermediate re-sweep reports and duplicate summary tables removed (F32); findings section consolidated; phases are self-contained; acceptance criteria testable; `[Applied]` tags preserved as status indicators. F33 (§7 redundancy) is minor and left as recommendation. |

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
| F28 | No batch/process manufacturing path; gin distillery unaddressed | major | gaps | — |
| F29 | No error-path or negative tests planned | major | testing | — |
| F30 | No CORS configuration for API–UI communication | major | gaps | — |
| F31 | Workspace `Cargo.toml` missing `resolver = "2"` | major | best-practices | — |
| F32 | Re-sweep artifacts and duplicate summary tables | major | plan-hygiene | — |
| F33 | `§7 Implementation Order` redundant with `§5` | minor | plan-hygiene | — |
| F34 | Phase 4 API smoke tests need `tokio` test runtime | minor | testing | — |
| F35 | No Rust edition or MSRV specified | minor | best-practices | F31 |
