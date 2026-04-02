# Arcogine — Vision & Identity

## What is Arcogine?

Arcogine is a **simulation-first platform** designed to model and experiment with **factory systems, economic dynamics, and decision-making processes**.

It is not just a game. It is:

> **A system for understanding how decisions shape complex industrial and economic behavior.**

At its core, Arcogine bridges three domains:

- **Operations** — production, capacity, bottlenecks
- **Economics** — pricing, demand, revenue
- **Decision systems** — agents, policies, incentives

## Core Loop

Arcogine is fundamentally a deterministic, event-driven system where decisions affect operations, and operations feed back into decisions:

```text
Pricing / Incentives
        ↓
Demand (Orders)
        ↓
Factory (Production)
        ↓
Lead Time / Throughput
        ↓
Revenue / KPIs
        ↓
Agent / Manager Decisions
        ↺
```

## Delivery Constraints (Executed)

- The implemented MVP is a single-user, locally runnable experiment platform.
- The simulation is deterministic and repeatable using explicit seeded inputs.
- UI and API are intentionally thin: both are experiment controls, not game mechanics.
- Repository and API surfaces were built for extensibility while keeping MVP behavior stable.
- For the completed phased implementation record and validation matrix, see `implementation-roadmap.md`.

## Long-Term Vision

The platform aims to grow into:

- **Digital twins** for industrial systems (connected to real ERP/MES/CRM data)
- **Serious games** for management training and education
- **Multi-agent decision environments** for research and experimentation
- **MMO-scale economic simulations** where many participants interact through shared markets

## Naming

**Arcogine** is derived from:

- **Arcology** — a self-contained industrial system
- **Engine** — the simulation core that drives everything

## GitHub Description

> Arcogine is a deterministic simulation engine for factory systems, economic dynamics, and agent-driven decision making.

## License

The project uses **Apache-2.0** (see `LICENSE`).

This choice:

- Matches Rust ecosystem conventions
- Encourages adoption and contribution
- Supports commercial use while providing patent protection
