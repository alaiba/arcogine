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

## Repository Structure

```text
arcogine/
  Cargo.toml              # Workspace root

  crates/
    sim-core/             # Event engine, scheduler, logging, KPIs, scenario loader
    sim-factory/          # Machines, jobs, routing, queues
    sim-material/         # Recipes, inventory, material transformation (Phase 7)
    sim-economy/          # Pricing, demand, revenue, cost, supply
    sim-agents/           # Agent trait and implementations
    sim-types/            # Typed IDs, shared structs, error types
    sim-api/              # HTTP API (Axum)
    sim-cli/              # CLI entrypoint

  ui/                     # React/TypeScript experiment console
  examples/               # TOML scenario files
  docs/                   # Project documentation
  benches/                # Criterion benchmarks
  .github/                # CI workflows
```
