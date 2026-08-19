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

## Core Architecture Philosophy: Events, State, Observations

This is the first-class architectural principle for Arcogine. It is a design heuristic *and* an architectural invariant: new features and refactors should be evaluated against it, and deviations should be a deliberate, documented decision rather than an accident of implementation order.

```text
Events mutate State.
State produces Observations.
Observations inform Decisions.
Decisions produce Events.
```

### Events

Events are immutable facts, or scheduled facts, in simulated time — order creation, task completion, machine availability changes, price changes, demand evaluation, agent evaluation, agent decisions.

Events:

- are immutable (Arcogine implements them as Java records, e.g. `Event`, `EventPayload`);
- carry only the domain-relevant facts needed to apply the transition;
- participate in deterministic ordering via the `Scheduler`;
- are the primary — ideally the *only* — mechanism for causing a simulation state transition;
- remain suitable for inspection, testing, replay, and experiment analysis (`EventLog`, `/api/export/events`).

### State

Each subsystem exclusively owns its mutable domain state. Pricing owns `MarketPrice` and its history (`PricingState`) — the current market offer, not any individual order's terms. Factory owns machines, jobs, queues, completion state, and production metrics, including each order's immutable `OrderPrice`/`OrderValue` and the derived `CompletedSalesValue` (`FactoryHandler`). A future inventory subsystem would own stock; finance would own financial state; workforce would own labor state.

State should:

- have exactly one authoritative owner;
- avoid synchronized duplicate representations of the same fact across subsystems;
- be mutated only by its owning subsystem, in response to an event it handles;
- never be mutated directly by agents or by unrelated domains.

### Observations

Observations are immutable, read-only projections of current simulation state, purpose-built for consumers that need information but must not own or mutate it — agents, decision policies, demand models, experiments, reporting/evaluation components. `AgentObservation` is the canonical example.

Observations should:

- be derived from authoritative state, computed on demand rather than cached as a second source of truth;
- be purpose-specific — expose what the consumer needs, not the internals of the owning subsystem;
- be immutable;
- define the capability and visibility boundary for whoever consumes them (an agent can only act on what its observation exposes).

### Decisions

Decisions are an important consequence of this model, even though they are not one of the three top-level concepts:

```text
Observation -> Decision -> Event
```

Agents and policies observe, decide, and emit events — they never directly mutate simulation state. `SalesAgent.decide()` is a pure function over an `AgentObservation`; when it decides to act, it schedules `PriceChange`/`AgentDecision` events rather than calling a setter on `PricingState`. This is the pattern all future decision-making code should follow.

### Pricing, orders, and money: MarketPrice vs. OrderPrice

`price` is not one universal simulation value — collapsing it into a single field is what caused the coupling and bugs described in [`devel/architecture-assessment-events-state-observations.md`](../devel/architecture-assessment-events-state-observations.md). Arcogine distinguishes:

| Concept | Meaning | Owner / location | Mutability |
|---|---|---|---|
| **MarketPrice** | The price currently being offered to the market; an input to the demand model. | Pricing/economy state (`PricingState`) | Mutable — changes on `PriceChange` events |
| **OrderPrice** (unit price) | The price agreed when a specific order was created. | Immutable order/transaction data, captured on the `OrderCreation` event and carried by the order/job for its lifetime | Immutable once the order exists |
| **OrderValue** | `quantity × OrderPrice` for one order. | Derived from the order, from the moment it's created | Derived (not separately mutated) |
| **CompletedSalesValue** | The sum of `OrderValue` for orders that have completed production/fulfillment. | Factory/operational KPI | Accumulates as orders complete, using each order's own `OrderPrice` |
| Revenue | Reserved terminology for a future finance/accounting domain (recognition policy, receivables, deferred revenue, etc.) | Not currently modeled | — |

The lifecycle:

```text
MarketPrice
    |
    v
Demand Evaluation
    |
    v
Order Creation
    |
    +--> capture OrderPrice (= MarketPrice at that instant)
    |
    +--> derive OrderValue = quantity x OrderPrice
    |
    v
Production / Fulfillment
    |
    v
Order Completion
    |
    v
CompletedSalesValue += OrderValue
```

The temporal boundary is **order creation**: before it, price is market state (mutable, forward-looking, drives future demand); after it, the agreed unit price is a historical transaction fact that belongs to the order and must not change when `MarketPrice` later changes.

```text
CURRENT MARKET STATE              HISTORICAL TRANSACTION
MarketPrice = $15                 Order A
       |                            unitPrice = $10
       |                            quantity = 5
       v
future demand                      orderValue = $50
```

Changing the left side must never mutate the right side. Concretely: a `SalesAgent` observes `MarketPrice`, decides a new `MarketPrice`, and emits `PriceChange` — this affects only future demand evaluations and future orders. It must never reprice an order that already exists, including one still in production. This also closes off an invalid strategy where an agent could lower the market price to generate backlog cheaply, then raise it before those orders complete to inflate their apparent value; existing orders are economically invariant under later market-price changes.

This is a deliberate **product decision, not an accounting model**: `CompletedSalesValue` is an operational KPI (how much value has this factory shipped), not formal revenue recognition. Concepts such as revenue recognition policy, accounts receivable, payment terms, cash receipts, accrued/deferred revenue belong to a future finance domain, if and when one is needed, and should not be implied by current naming.

### What should trigger architectural review

Treat any of the following as a signal to stop and reconsider the design, not just implement around it:

- one subsystem mutating another subsystem's state;
- a mutable "observation" (anything handed to an agent/consumer that they could write through);
- duplicated authoritative state (the same fact represented as separate mutable fields in two subsystems);
- synchronization setters proliferating between domains (`setX`/`syncX`-style cross-domain pushes);
- agents or policies reaching directly into mutable subsystem internals instead of going through an observation;
- adding a subsystem requiring pairwise wiring changes to every existing subsystem;
- event ordering becoming implicit, or dependent on registration/construction order rather than an explicit, documented contract.

Arcogine's current implementation already exhibits some of these signals — see [`devel/architecture-assessment-events-state-observations.md`](../devel/architecture-assessment-events-state-observations.md) for a source-level review against this philosophy and a staged backlog for closing the gaps.

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

Handlers may schedule new events via the `Scheduler` but never reach into other handlers directly. Today, cross-handler data (current price, average lead time) flows through explicit field synchronization in `IntegratedHandler` (`demand.setPrice(...)`, `demand.setAvgLeadTime(...)`, `factory.setCurrentPrice(...)`), and `IntegratedHandler` also assembles `AgentObservation` by reading raw fields off `FactoryHandler` and `PricingState` directly.

This is a **transitional** pattern under the [Events–State–Observations philosophy](#core-architecture-philosophy-events-state-observations): it duplicates `MarketPrice` as a mutable copy in three places (`PricingState`, `DemandModel`, `FactoryHandler`) instead of `PricingState` being the sole owner that others read on demand, and it embeds observation-construction logic in the orchestration handler instead of a dedicated projector. `FactoryHandler.setCurrentPrice(...)` in particular is now understood to be unnecessary under the [resolved pricing/order semantics](#pricing-orders-and-money-marketprice-vs-orderprice): the factory should compute `CompletedSalesValue` from each order's own captured `OrderPrice`, not by reading current `MarketPrice` at completion time — so the `PricingState -> FactoryHandler` sync should be removed outright rather than kept as a "read on demand" seam. It is called out explicitly, rather than presented as the target design, in [`devel/architecture-assessment-events-state-observations.md`](../devel/architecture-assessment-events-state-observations.md), which also lays out the staged backlog for closing this gap without introducing a generic event bus or otherwise weakening deterministic, explicit handler ordering.

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
