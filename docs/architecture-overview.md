# Arcogine — Architectural Overview

This document describes the design philosophy and architectural principles that guide Arcogine's implementation.

## Non-Negotiable Constraints

1. Core simulation is written in Rust.
2. Headless simulation core is primary; UI/API are additive.
3. MVP must tie factory flow to economy loop.
4. Repository must be reproducible, modular, testable, and collaboration-ready.
5. UI is a single-user experiment console, not a game client.
6. Support native and containerized local execution.
7. Deterministic acceptance tests and scenario-level validation are mandatory.
8. Agents only use approved command interfaces and never mutate simulation state directly.

## Simulation-First

The system is built around a **headless simulation core**, not a game engine.

- No rendering dependency in the core
- Deterministic execution — same inputs always produce the same outputs
- Reproducible outcomes for testing, comparison, and analysis
- Designed for experimentation: the engine runs independently of any UI or network layer

## Data-Oriented Design

Performance-critical simulation state follows data-oriented principles:

- **Struct-of-arrays (SoA)** layout for cache-friendly iteration
- **Typed IDs** (`MachineId`, `ProductId`, `JobId`) instead of pointers or references
- **Contiguous memory layout** for hot-path data
- **Minimal dynamic allocation** during simulation ticks

These choices support efficient scaling and make the simulation amenable to profiling and optimization.

## Discrete-Event Simulation (DES)

The simulation advances via discrete events rather than fixed time steps:

- **Order creation** — new demand enters the system
- **Task start / end** — production work begins and completes
- **Machine availability** — machines go online, offline, or change state
- **Price changes** — pricing adjustments affect future demand
- **Agent decisions** — external actors submit commands that influence the system
- **Demand evaluation** — periodic trigger that samples the demand model and generates orders
- **Agent evaluation** — periodic trigger that invokes registered agents for decision-making

Benefits of DES:

- Efficient scaling — idle periods are skipped entirely
- Realistic modeling — events occur at their natural times
- Clear causality — every state change traces back to a specific event

## Determinism Contract

Arcogine guarantees reproducible simulation results: given identical inputs (scenario file + RNG seed), the engine produces identical outputs (final state, KPIs, and event stream).

### How it works

- **RNG**: All stochastic behavior uses `ChaCha8Rng` from `rand_chacha`, which produces platform-independent deterministic sequences. The seed is specified in the scenario TOML file.
- **Seed propagation**: The scenario's `rng_seed` initializes a single `ChaCha8Rng` instance owned by the simulation runner. Sub-RNGs for domain modules (demand sampling, agent noise) are derived from this root RNG using `ChaCha8Rng::seed_from_u64`.
- **Replay guarantees**: Running the same scenario file twice with the same seed produces byte-identical event logs and final state. Determinism tests verify this by running a scenario twice and asserting `PartialEq` equality on the full state and event log.
- **What breaks determinism**: Non-seeded randomness, floating-point non-associativity (avoided by using integer arithmetic or fixed evaluation order), time-of-day dependencies, and concurrent mutation of simulation state.

### Testing

Determinism is verified by property tests and scenario replay tests that run the same configuration twice and assert identical results.

## Event Dispatch Architecture

The simulation engine uses a trait-based event dispatch pattern that preserves the crate dependency DAG while enabling domain-specific event handling.

### The `EventHandler` trait

Defined in `sim-core`, the `EventHandler` trait provides a single method:

```rust
fn handle_event(&mut self, event: &Event, scheduler: &mut Scheduler) -> Result<(), SimError>
```

Domain crates (`sim-factory`, `sim-economy`, `sim-agents`) implement this trait. Each handler processes the event types it cares about and ignores the rest.

### The `run_scenario` runner

The headless simulation runner in `sim-core` accepts a composed `EventHandler` implementation:

```rust
fn run_scenario(config: &ScenarioConfig, handler: &mut dyn EventHandler) -> Result<SimResult, SimError>
```

The runner dequeues events from the priority queue and dispatches each to the handler. Domain handlers schedule follow-up events by pushing to the `Scheduler`.

### Why this pattern

`sim-core` depends only on `sim-types` — it cannot import domain crates. The `EventHandler` trait inverts the dependency: domain crates depend on `sim-core` for the trait definition and implement it. The binary crate (`sim-cli`) or API crate (`sim-api`) assembles domain handlers into a composite handler and passes it to the runner. This preserves the acyclic dependency graph while allowing the core event loop to dispatch to domain-specific logic.

### Crate dependency DAG

```text
sim-types          (no upstream dependencies)
  └─► sim-core
        ├─► sim-factory
        ├─► sim-economy
        └─► sim-agents
              └─► sim-api
                    └─► sim-cli
```

- `sim-types` has no upstream dependencies.
- `sim-core` depends on `sim-types`.
- `sim-factory`, `sim-economy`, `sim-agents` depend on `sim-core` + `sim-types`.
- `sim-api` depends on all domain crates and shared modules.
- `sim-cli` depends on `sim-api`.

## IO Contracts and Runtime Boundaries

- Scenario files are TOML. Schema structs live in `sim-types`; loader and validation logic live in `sim-core` and return structured `SimError`.
- The simulation command and query path is synchronous and deterministic inside `sim-cli`/`sim-api` runners.
- HTTP API and UI run in separate process layers and interact via commands/events, not direct state mutation.

## Concurrency Model

- The API layer runs on a Tokio async runtime.
- The simulation engine runs on a deterministic synchronous execution path.
- The API uses bounded command channels (`std::sync::mpsc`) and broadcast event channels to communicate without sharing mutable simulation state across threads. `std::sync::mpsc` is used because the simulation executes on a synchronous OS thread.

## Separation of Concerns

Arcogine is organized in two explicit status bands:

- **Current (implemented)**: six active layers today.
- **Planned (Phase 7)**: material simulation depth and richer workflow tooling are not yet in the active crate graph.

| Layer | Responsibility | Crate(s) |
|-------|---------------|-----------|
| **Simulation Core** | Event scheduling, time, logging, determinism | `sim-core`, `sim-types` |
| **Factory Layer** | Machines, jobs, routing, queues | `sim-factory` |
| **Economy Layer** | Pricing, demand, revenue, cost, supply | `sim-economy` |
| **Agent Layer** | Decision-making actors that observe and command | `sim-agents` |
| **API / UI Layer** | HTTP surface, CLI, web dashboard | `sim-api`, `sim-cli`, `ui/` |
| **Material Layer** *(planned, Phase 7)* | Recipes, inventory, material transformation | `sim-material` |

Agents and UI never directly mutate simulation state. All mutations flow through validated command interfaces.

## Agent Architecture

Agents operate **above the simulation**, not inside it:

- They **observe** state through read-only query APIs
- They **submit commands** through the same interface as human users
- They **do not own or mutate** simulation state directly

This separation ensures that agent behavior is auditable, replayable, and interchangeable.

MVP agent: `SalesAgent` (observes backlog, lead time, revenue; adjusts price).

Future agent types: Planning, Procurement, Maintenance, and LLM-based strategy agents.

## Technology Stack

| Component | Technology | Rationale |
|-----------|-----------|-----------|
| Core simulation | Rust | Performance, safety, determinism |
| HTTP API | Axum + Tokio | Ergonomic, actively maintained, Tokio ecosystem |
| UI | React + TypeScript (Vite) | Lightweight, familiar, web-accessible |
| Deterministic RNG | `rand` + `rand_chacha` (ChaCha8Rng) | Platform-independent reproducibility |
| Serialization | `serde` + `toml` | Human-readable scenario files, Rust-native |
| Deployment | Native + Docker Compose | Fast dev loop + reproducible stack |

## Standards Alignment

Arcogine's architecture is designed for compatibility with industry standards without implementing them prematurely. See `docs/standards-alignment.md` for the full mapping. Key points:

- **ISA-95 / IEC 62264** — Domain concepts (machines, products, routings) map to ISA-95 terminology. MVP code uses Arcogine naming with ISA-95 correspondences documented.
- **DES methodology** — Core simulation approach. Event scheduling, monotonic time, event causality follow standard DES patterns.
- **RAMI 4.0** — Arcogine's layered crate architecture maps naturally to RAMI 4.0 layers (asset → factory, functional → core, business → agents/economy).
- **OpenAPI** — **Planned**. `utoipa` is present as dependency planning metadata, but the current routed surface is consumed by direct API clients and SSE wiring rather than a generated OpenAPI pipeline.

Future directions (AAS, OPC UA, FMI, MQTT, FIPA) are preserved by architectural choices documented in `docs/standards-alignment.md`.

## Extensibility: Discrete → Batch/Process Manufacturing

The MVP models **discrete manufacturing** (jobs with unit counts advancing through machine routing steps). The architecture is designed so that **batch and process manufacturing** — where material is transformed in volume-based batches with yield, loss, and time-dependent reactions — can be added without restructuring the core engine.

Key design-for decisions in the MVP:
- **Quantity types** in `sim-types` use an enum that accommodates both discrete units and volumes, so Phase 7 batch quantities integrate cleanly.
- **Machine capacity** includes an optional field for volume-based capacity alongside concurrency, allowing tanks and stills to coexist with discrete machines.
- **Routing steps** accept generic durations and optional setup/cleaning times, enabling time-based process steps.
- **Event scheduler** is quantity-agnostic — it schedules events by time, not by production paradigm.

Phase 7 introduces a `sim-material` crate (recipes, inventory, material transformation) and extends `sim-factory` and `sim-economy` with batch entities, equipment specialization, and multi-component cost structures. The reference scenario is a gin distillery.

## UI Architecture

The experiment console (`ui/`) is a React + TypeScript single-page application built with Vite.
It acts as a read-mostly client: all simulation state lives in the Rust backend, and the UI mirrors it via REST plus SSE.

### Current layout status (mounted components)

The active runtime mounts:

- `Toolbar`
- `WelcomeOverlay`
- `KpiCards`
- `TimeSeriesChart`
- `FactoryFlow` (within tabbed main area)
- `MachineTable` (within tabbed main area)
- `JobTracker` (within tabbed main area)
- `Sidebar`
- `BottomDrawer`

Baseline and export controls are implemented in `Sidebar`, not as separate mounted pages.

### Layout

The active layout is a single page with:

- a top toolbar for run/pause/step/reset controls,
- a main region for KPI, charts, and tabbed simulation views,
- a sidebar for interactive controls and baselines,
- a bottom drawer for event logs.

On first load, `WelcomeOverlay` presents the scenario entry points.

### State Management

Client state is managed by two Zustand stores:

- **`simulation`** — current scenario, simulation status, KPI snapshots, event log buffer, machine states, job list, connection health. Updated by REST responses and SSE events.
- **`baselines`** — up to 3 saved baseline snapshots (KPI values + scenario config) for comparison. Session-scoped, not persisted.

Components subscribe to store slices and re-render only on relevant changes.

### Data Flow

```text
Axum API (Phase 4)
  │
  ├── REST (request/response)
  │     └──► api/client.ts ──► Zustand stores ──► React components
  │
  └── SSE (GET /api/events/stream, server-push)
        └──► api/sse.ts ──► Zustand simulation store ──► React components
```

- **REST** handles commands (load scenario, change price, toggle machine/agent) and queries (get KPIs, get topology, list jobs). The `api/client.ts` module currently owns typed request/response call contracts directly.
- **SSE** delivers simulation events in real time during a running simulation. The EventSource wrapper parses typed events and appends them to the store. On pause or stop, the UI falls back to a final REST snapshot.
- **No direct state coupling** — the UI never holds or computes simulation state. It reflects what the API reports.

### Technology Choices

| Concern | Choice | Rationale |
|---------|--------|-----------|
| Component styling | Tailwind CSS + custom components | Lightweight, consistent utility styling with explicit ownership |
| Charting | Recharts | SVG charting with TypeScript support |
| State management | Zustand | Minimal boilerplate, works well with both polling and SSE patterns |
| Unit testing | Vitest | Fast Vite-native test runner for store and utility logic |
| E2E testing | Playwright | Browser automation for smoke tests (including CI) |

### Accessibility

- Interactive controls are implemented as native form controls where possible, with explicit labels and state feedback.
- Machine states use both color and icon/text labels — color is never the sole indicator.
- Contrast and interaction states are tuned through Tailwind utility styles.

### Component status notes

`BaselineCompare` and `ExportMenu` exist in `src/components/experiment/` and are validated as implemented utilities.
They are intentionally not mounted as top-level sections in the default `App` composition yet.

## Design Decisions

These choices were made deliberately during the initial implementation and remain load-bearing:

- **Event-based command model** over direct shared-state mutation: preserves reproducibility and testability.
- **`EventHandler` + composite runner** instead of inverse dependencies from `sim-core` to domain crates.
- **`arcogine` binary with mode split** (`run` / `serve`) in one CLI for headless and service workflows.
- **Separate API command channel model** rather than shared mutable state between async API and deterministic runner.
- **TOML scenario schema** with explicit naming alignment to ISA-95 vocabulary where useful.
- **UI via API only** — the frontend communicates exclusively through REST and SSE, never accessing simulation internals.
- **Same-origin `/api` routing** for lower operational complexity — Vite proxy in dev, Nginx proxy in containers.
- **Floating Rust `stable` toolchain** — avoids false pinning expectations while keeping builds reproducible.
- **`"Current"` / `"Planned"` / `"Phase 7"` labels** in architecture-facing docs to distinguish shipped from future capabilities.

## Documented Trade-Offs

- Postponed full OpenAPI endpoint annotation while keeping command and schema compatibility stable. `utoipa` is a dependency but the generated spec is not yet wired.
- Used `std::sync::mpsc` in the runtime path because simulation executes on a synchronous OS thread; async channels are unnecessary overhead.
- Kept Material Layer (`sim-material`) as a planned phase to avoid changing discrete-MVP scope.
- Playwright CI execution depends on a browser-capable runner; smoke tests are bootstrapped but require a local or CI environment with browser support.
- Unmounted UI components (`BaselineCompare`, `ExportMenu`) are tracked as implementation status rather than removed, preserving forward intent.

## Repository Structure

```text
arcogine/
  Cargo.toml              # Workspace root

  crates/
    sim-core/             # Event engine, scheduler, logging, KPIs, scenario loader
      benches/            # Criterion benchmarks (scheduler, scenario runtime)
    sim-factory/          # Machines, jobs, routing, queues
    sim-economy/          # Pricing, demand, revenue, cost, supply
    sim-agents/           # Agent trait and implementations
    sim-types/            # Typed IDs, shared structs, error types
    sim-api/              # HTTP API (Axum), SSE stream
    sim-cli/              # CLI entrypoint (headless + server modes)
    # sim-material/         # Planned in Phase 7: recipes, inventory, material transformation

  ui/                     # React/TypeScript experiment console
    src/
      api/                # Typed REST client, SSE EventSource wrapper
      stores/             # Zustand stores (simulation, baselines)
      components/
        layout/           # Toolbar, Sidebar, BottomDrawer
        dashboard/        # KpiCards, TimeSeriesChart, FactoryFlow, MachineTable, JobTracker
        experiment/       # BaselineCompare, ExportMenu
        onboarding/       # WelcomeOverlay
        shared/           # ErrorBoundary, SkeletonLoader, Toast
    e2e/                  # Playwright smoke tests

  examples/               # TOML scenario files
  docs/                   # Project documentation
  .github/                # CI workflows
```
