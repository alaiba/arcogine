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
| **Economy Layer** | Pricing, demand, revenue | `sim-economy` |
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

## Repository Structure

```text
arcogine/
  Cargo.toml              # Workspace root

  crates/
    sim-core/             # Event engine, scheduler, logging, KPIs, scenario loader
    sim-factory/          # Machines, jobs, routing, queues
    sim-economy/          # Pricing, demand, revenue
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
