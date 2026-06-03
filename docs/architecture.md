# Arcogine — Architectural Overview

This document describes the design philosophy and architectural principles that guide Arcogine's implementation.

## Non-Negotiable Constraints

1. Core simulation is written in Java 25.
2. Headless simulation core is primary; UI/API are additive.
3. MVP must tie factory flow to economy loop.
4. Repository must be reproducible, modular, testable, and collaboration-ready.
5. UI is a single-user experiment console, not a game client.
6. Support native and containerized local execution.
7. Deterministic acceptance tests and scenario-level validation are mandatory.
8. Agents only use approved command interfaces and never mutate simulation state directly.
9. Security-sensitive defaults remain local-first by default; non-local exposure requires explicit hardening controls.

## Simulation-First

The system is built around a **headless simulation core**, not a game engine.

- No rendering dependency in the core
- Deterministic execution — same inputs always produce the same outputs
- Reproducible outcomes for testing, comparison, and analysis
- Designed for experimentation: the engine runs independently of any UI or network layer

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

- Only meaningful moments consume compute
- Time can skip between events of interest
- Simulation runtime scales with event density, not wall-clock time

## Module Structure

The Java codebase follows a **modular monolith** pattern with Gradle multi-module layout:

```text
java/
├── sim-types/      Domain primitives: SimTime, MachineId, ProductId, JobId,
│                   Quantity, SimError, scenario config records
├── sim-core/       DES engine: Scheduler, Event, EventHandler interface,
│                   CompositeHandler, EventLog, KPIs, SimRunner, ScenarioLoader
├── sim-factory/    Factory domain: Machine, Job, Routing, FactoryHandler
├── sim-economy/    Economic layer: PricingState, DemandModel
├── sim-agents/     Agent framework: SalesAgent, AgentObservation
├── sim-api/        Spring Boot HTTP + SSE server: controllers, SimThread,
│                   IntegratedHandler, SnapshotBuilder, DTOs
└── sim-cli/        Picocli CLI entry point: serve + headless run modes
```

### Dependency graph

```text
sim-types ← sim-core ← sim-factory
                      ← sim-economy
                      ← sim-agents
                           ↑
            sim-api ←──────┘ (all of the above)
                ↑
            sim-cli (entry point)
```

Each module exposes a clean public API and hides implementation details. Domain modules (`sim-factory`, `sim-economy`, `sim-agents`) implement the `EventHandler` interface and are wired together by `IntegratedHandler` in the API layer.

## Event Dispatch Architecture

Events flow through a chain of handlers in deterministic order:

```text
Scheduler (priority queue by SimTime)
    │
    ▼
IntegratedHandler
    ├── PricingState.handleEvent()
    ├── DemandModel.handleEvent()      ← price/lead-time synced from pricing/factory
    ├── FactoryHandler.handleEvent()
    └── SalesAgent.handleEvent()       ← only on AgentEvaluation, if enabled
```

The `EventHandler` interface:

```java
public interface EventHandler {
    void handleEvent(Event event, Scheduler scheduler);
}
```

Handlers may schedule new events via the `Scheduler` but never reach into other handlers directly. Cross-handler data flows through explicit field synchronization in `IntegratedHandler`.

## Type System

Java 25 features map cleanly to the domain:

| Concept | Java feature |
|---------|-------------|
| Typed IDs | `record MachineId(long value)` |
| Value objects | `record SimTime(long value)` |
| Sum types | `sealed interface EventPayload` with record permits |
| Error hierarchy | `sealed class SimError extends RuntimeException` |
| Config DTOs | Records with `@JsonProperty` for TOML deserialization |
| Pattern matching | `switch (event.payload())` with exhaustive pattern matching |

## Determinism Contract

The simulation guarantees deterministic execution:

- `java.util.Random` seeded with `rng_seed` from scenario config
- Priority queue orders events by time, with FIFO tie-breaking
- No floating-point non-determinism (Java 25 uses strict FP by default)
- No concurrent mutation of simulation state

Given identical scenario TOML and the same seed, the simulation produces identical event logs, KPIs, and final state.

## API Layer

The HTTP API uses Spring Boot 3 with Spring MVC:

- REST endpoints for scenario loading, simulation control, interventions, and queries
- Server-Sent Events (SSE) via `SseEmitter` for real-time event streaming
- Simulation runs on a dedicated thread (`SimThread`) communicating via `BlockingQueue`
- `AtomicReference<SimSnapshot>` provides lock-free snapshot reads for API handlers

## Technology Stack

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Language | Java 25 LTS | Records, sealed types, pattern matching |
| Framework | Spring Boot 3.4 | HTTP server, DI, config |
| CLI | Picocli | Command-line parsing |
| Build | Gradle 8 (Kotlin DSL) | Multi-module build |
| Config format | TOML | Scenario files (via Jackson TOML) |
| Serialization | Jackson | JSON API responses, TOML parsing |
| Testing | JUnit 5 | Unit and integration tests |
| Coverage | JaCoCo | Code coverage reporting |
| Container | Eclipse Temurin 25 | Docker runtime |
| Frontend | React 19 + TypeScript 6 | Vite, Zustand, Tailwind CSS, Recharts |
