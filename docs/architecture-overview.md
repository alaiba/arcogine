# Arcogine — Architectural Overview

This document describes the design philosophy and architectural principles that guide Arcogine's implementation. For the phased build plan, see `devel/Original-plan.md`.

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

Benefits of DES:

- Efficient scaling — idle periods are skipped entirely
- Realistic modeling — events occur at their natural times
- Clear causality — every state change traces back to a specific event

## Separation of Concerns

Arcogine is layered into five distinct tiers:

| Layer | Responsibility | Crate(s) |
|-------|---------------|-----------|
| **Simulation Core** | Event scheduling, time, logging, determinism | `sim-core`, `sim-types` |
| **Factory Layer** | Machines, jobs, routing, queues | `sim-factory` |
| **Material Layer** | Recipes, inventory, material transformation (Phase 7) | `sim-material` |
| **Economy Layer** | Pricing, demand, revenue, cost, supply | `sim-economy` |
| **Agent Layer** | Decision-making actors that observe and command | `sim-agents` |
| **API / UI Layer** | HTTP surface, CLI, web dashboard | `sim-api`, `sim-cli`, `ui/` |

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
- **OpenAPI** — REST API is specified via OpenAPI 3.x for machine discoverability and integration readiness.

Future directions (AAS, OPC UA, FMI, MQTT, FIPA) are preserved by architectural choices documented in `docs/standards-alignment.md`.

## Extensibility: Discrete → Batch/Process Manufacturing

The MVP models **discrete manufacturing** (jobs with unit counts advancing through machine routing steps). The architecture is designed so that **batch and process manufacturing** — where material is transformed in volume-based batches with yield, loss, and time-dependent reactions — can be added without restructuring the core engine.

Key design-for decisions in the MVP:
- **Quantity types** in `sim-types` use an enum that accommodates both discrete units and volumes, so Phase 7 batch quantities integrate cleanly.
- **Machine capacity** includes an optional field for volume-based capacity alongside concurrency, allowing tanks and stills to coexist with discrete machines.
- **Routing steps** accept generic durations and optional setup/cleaning times, enabling time-based process steps.
- **Event scheduler** is quantity-agnostic — it schedules events by time, not by production paradigm.

Phase 7 introduces a `sim-material` crate (recipes, inventory, material transformation) and extends `sim-factory` and `sim-economy` with batch entities, equipment specialization, and multi-component cost structures. The reference scenario is a gin distillery. See `devel/Original-plan.md` Phase 7 for the detailed plan.

## UI Architecture

The experiment console (`ui/`) is a React + TypeScript single-page application built with Vite. It acts as a read-mostly client: all simulation state lives in the Rust backend, and the UI mirrors it via the REST API and a Server-Sent Events (SSE) stream.

### Layout

The UI uses a three-region layout optimized for a single-monitor experiment workflow:

| Region | Content | Width |
|--------|---------|-------|
| **Toolbar** (top) | Scenario selector, sim controls (run/pause/step/reset), speed multiplier, agent toggle | Full width |
| **Main area** (center-left) | KPI summary cards, time-series chart, tabbed view (factory flow / machine table / job tracker) | ~70% |
| **Sidebar** (center-right) | Control levers (price, machine count), baseline comparison, export menu | ~30% |
| **Bottom drawer** (collapsible) | Chronological event log with type filter and search | Full width |

A welcome overlay appears on first load, presenting the built-in scenarios as cards with a quick-start option.

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

- **REST** handles commands (load scenario, change price, toggle agent) and queries (get KPIs, get topology, list jobs). The typed API client generates request/response types aligned to the OpenAPI spec produced by Phase 4's `utoipa`.
- **SSE** delivers simulation events in real time during a running simulation. The EventSource wrapper parses typed events and appends them to the store. On pause or stop, the UI falls back to a final REST snapshot.
- **No direct state coupling** — the UI never holds or computes simulation state. It reflects what the API reports.

### Technology Choices

| Concern | Choice | Rationale |
|---------|--------|-----------|
| Component library | Tailwind CSS + shadcn/ui | Composable, dark-mode ready, WCAG 2.1 AA accessible by default |
| Charting | Recharts | React-native SVG charts with TypeScript support and built-in PNG export |
| State management | Zustand | Minimal boilerplate, works well with both polling and SSE patterns |
| E2E testing | Playwright | Browser automation for smoke tests, integrated into CI |

### Accessibility

- All interactive elements are keyboard-navigable (provided by shadcn/ui primitives).
- Charts carry `aria-label` attributes with current KPI values.
- Machine states use both color and icon/text labels — color is never the sole indicator.
- Contrast ratios meet WCAG 2.1 AA (Tailwind + shadcn defaults).

## Repository Structure

```text
arcogine/
  Cargo.toml              # Workspace root

  crates/
    sim-core/             # Event engine, scheduler, logging, KPIs, scenario loader
      benches/            # Criterion benchmarks (scheduler, scenario runtime)
    sim-factory/          # Machines, jobs, routing, queues
    sim-material/         # Recipes, inventory, material transformation (Phase 7)
    sim-economy/          # Pricing, demand, revenue, cost, supply
    sim-agents/           # Agent trait and implementations
    sim-types/            # Typed IDs, shared structs, error types
    sim-api/              # HTTP API (Axum), SSE stream, OpenAPI spec
    sim-cli/              # CLI entrypoint (headless + server modes)

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
