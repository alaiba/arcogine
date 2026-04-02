# Arcogine — Vision & Identity

## What is Arcogine?

Arcogine is a **simulation-first platform** designed to model and experiment with **factory systems, economic dynamics, and decision-making processes**.

It is intentionally simulation-first, while still being enjoyable to use:

> **A system for understanding how decisions shape complex industrial and economic behavior.**

At its core, Arcogine bridges three domains:

- **Operations** — production, capacity, bottlenecks
- **Economics** — pricing, demand, revenue
- **Decision systems** — agents, policies, incentives

## Playful onboarding path

You can also use Arcogine as a lightweight simulation challenge:

- Run `Basic` first to learn controls and pacing.
- Move to `Overload` to manage backlog and lead-time pressure.
- Compare short-term tuning versus structural improvement in `Capacity Expansion`.

### Suggested first session loop

For a first run, the loop is:

- load one of the scenarios,
- run or step the simulation,
- adjust product price and machine operating state,
- toggle the agent off/on,
- save at least one baseline snapshot,
- compare outcomes with throughput, backlog, and revenue targets in view.

This sequence keeps the first-session experience focused on control behavior before adding deeper tuning experiments.

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

## Out of Scope (MVP)

These capabilities are intentionally excluded from the current MVP:

- Multiplayer/MMO, distributed shards, advanced auth, full ERP/MES integration.
- Advanced scheduling optimization and planning algorithms.
- Full ISA-95/B2MML exchange and FMI/OPC-UA/FIPA production use.
- LLM-native agent autonomy, complex protocol interop, and enterprise observability stacks.

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
