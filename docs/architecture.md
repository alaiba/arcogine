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

Each subsystem exclusively owns its mutable domain state. Pricing owns `OfferPrice` and its history (`PricingState`) — the firm's own current asking price, not any individual order's terms and not an external market signal. Factory owns machines, jobs, queues, completion state, production metrics, and the derived `CompletedSalesValue` (`FactoryHandler`). A future inventory subsystem would own stock; finance would own financial state; workforce would own labor state.

Commercial terms (`OrderPrice`/`OrderValue`) are a separate case worth being precise about: they are immutable transaction facts, fixed at `OrderCreation`, that travel *into* Factory with the order rather than being something Factory conceptually owns. `Job` is where they're carried today because there is currently no separate commercial/order concept distinct from the production-lifecycle object — but that's an implementation convenience, not a claim that production and commercial terms are the same kind of state. If a second reason ever emerges to split them (e.g. an order existing before production starts, or one order spanning multiple jobs), the natural refactor is an `Order` (commercial: `orderId`, product, quantity, `unitPrice`) referenced by `Job` (operational: `jobId`, `orderId`, routing, status) — not introduced speculatively now, but the conceptual ownership should already reflect this distinction so the code doesn't imply Factory owns a fact it merely carries.

State should:

- have exactly one authoritative owner;
- avoid synchronized duplicate representations of the same fact across subsystems;
- be mutated only by its owning subsystem, in response to an event it handles;
- never be mutated directly by agents or by unrelated domains.

#### State ownership table

Concrete, source-level version of the rule above — checkable in review, not just implicit:

| Fact | Owning class | Mutated by |
|---|---|---|
| `OfferPrice`, price history | `PricingState` | `PriceChange` |
| Machines, machine availability | `MachineStore` (owned by `FactoryHandler`) | `MachineAvailabilityChange` |
| Jobs, job status (production lifecycle) | `JobStore` (owned by `FactoryHandler`) | `OrderCreation` (creates), `TaskEnd` (advances/completes) |
| `OrderPrice`/`OrderValue` (commercial terms — carried into Factory with the order, not owned by it; see the "State" section above) | `Job` (implementation convenience — there is no separate commercial/order concept yet) | Fixed at `OrderCreation`, immutable thereafter |
| `CompletedSalesValue`, `completedSales` | `FactoryHandler` | `TaskEnd` (on completion) |
| Ledger, `Cash`/`Sales` balances | `Ledger` (owned by `FinanceHandler`) | `OrderCompleted` |
| `SalesAgent`'s last observation, intervention count | `SalesAgent` | `observe(...)` (called by `IntegratedHandler`), `AgentEvaluation` |
| `IntegratedHandler.agentEnabled` | `IntegratedHandler` | `AgentEnabledChanged` |
| `EventLog` | `EventLog` (owned by `SimThread`) | every dispatched event, appended after `handleEvent` |
| Published API snapshot | `AtomicReference<SimSnapshot>` (owned by `SimThread`) | `SnapshotBuilder.buildSnapshot(...)`, called after each processed event/batch |

`DemandModel` reads `OfferPrice` and lead time on demand, via `DoubleSupplier`s bound to `PricingState`/`FactoryHandler` at construction — it has no state of its own to keep in sync, so it isn't listed as an owner above.

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

### Pricing, orders, and money: OfferPrice vs. OrderPrice

`price` is not one universal simulation value — collapsing it into a single field is what caused the coupling and bugs described in [`devel/architecture-assessment-events-state-observations.md`](../devel/architecture-assessment-events-state-observations.md). Arcogine distinguishes:

| Concept | Meaning | Owner / location | Mutability |
|---|---|---|---|
| `ObservedMarketPrice` | External/environmental market signal — what the broader market says the product is worth, or what comparable products are being offered for. **Not implemented**: reserved for a future external-market/environment domain. Do not use this name for the firm's own price. | Future environment/market domain | — |
| **OfferPrice** | The simulated firm's current asking price — mutable commercial state controlled by pricing policy/agents; what the demand model actually responds to today. | Economy/Pricing (`PricingState`) | Mutable — changes on `PriceChange` events |
| **OrderPrice** (unit price) | The price agreed when a specific order was created — `OfferPrice` at that instant, frozen. | Immutable order/transaction data, captured on the `OrderCreation` event and carried by the order/job for its lifetime | Immutable once the order exists |
| **OrderValue** | `quantity × OrderPrice` for one order. | Derived from the order, from the moment it's created | Derived (not separately mutated) |
| **CompletedSalesValue** | The sum of `OrderValue` for orders that have completed production/fulfillment. | Factory/operational KPI | Accumulates as orders complete, using each order's own `OrderPrice` |
| Revenue | Reserved terminology for a future finance/accounting domain (recognition policy, receivables, deferred revenue, etc.) | Not currently modeled | — |

The lifecycle:

```text
ObservedMarketPrice        [not required yet]
        |
        v
   Pricing policy
        |
        v
     OfferPrice
        |
        v
Demand Evaluation
        |
        v
Order Creation
        |
        +--> capture OrderPrice (= OfferPrice at that instant)
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

`ObservedMarketPrice` does not need to be implemented now — there is no external market/environment model in Arcogine today. The name is reserved so that today's firm-controlled price is never mistakenly called a "market price": `OfferPrice` is what the firm sets, not what an outside market observes.

The temporal boundary is **order creation**: before it, `OfferPrice` is the firm's own mutable commercial state (forward-looking, drives future demand); after it, the agreed unit price is a historical transaction fact that belongs to the order and must not change when `OfferPrice` later changes.

```text
CURRENT OFFER STATE               HISTORICAL TRANSACTION
OfferPrice = $15                  Order A
       |                            unitPrice = $10
       |                            quantity = 5
       v
future demand                      orderValue = $50
```

Changing the left side must never mutate the right side. Concretely: a `SalesAgent` observes `OfferPrice`, decides a new `OfferPrice`, and emits `PriceChange` — this affects only future demand evaluations and future orders. It must never reprice an order that already exists, including one still in production. This also closes off an invalid strategy where an agent could lower the offer price to generate backlog cheaply, then raise it before those orders complete to inflate their apparent value; existing orders are economically invariant under later offer-price changes.

No settlement pricing, indexed contracts, rebates, or discounts are introduced by this model — `OrderPrice = OfferPrice at OrderCreation`, full stop, and it remains immutable thereafter.

This is a deliberate **product decision, not sophistication in accounting**: `CompletedSalesValue` is an operational/commercial KPI (how much value has this factory shipped), computed from completed orders' own agreed prices. It answers "what commercial value has completed production?" — a different question from "what has Finance recorded as sales under the active financial policy?", covered next. Concepts such as configurable revenue-recognition policy, tax, depreciation, or multi-currency remain future scope — but the domain that would own them, Finance, is established now, deliberately minimal. See the next section.

### What should trigger architectural review

Treat any of the following as a signal to stop and reconsider the design, not just implement around it:

- one subsystem mutating another subsystem's state;
- a mutable "observation" (anything handed to an agent/consumer that they could write through);
- duplicated authoritative state (the same fact represented as separate mutable fields in two subsystems);
- synchronization setters proliferating between domains (`setX`/`syncX`-style cross-domain pushes);
- agents or policies reaching directly into mutable subsystem internals instead of going through an observation;
- adding a subsystem requiring pairwise wiring changes to every existing subsystem;
- event ordering becoming implicit, or dependent on registration/construction order rather than an explicit, documented contract;
- a monetary accumulator (cash, profit, receivables, or similar) appearing outside Finance;
- Finance inspecting another domain's mutable state to infer what happened, instead of reacting to an event that domain emitted;
- an unbalanced journal entry able to enter financial state.

Arcogine's current implementation already exhibits some of these signals — see [`devel/architecture-assessment-events-state-observations.md`](../devel/architecture-assessment-events-state-observations.md) for a source-level review against this philosophy and a staged backlog for closing the gaps.

## Commercial, Operational, and Financial Truth: the Finance Domain

Arcogine distinguishes three kinds of truth that are related by events but are never interchangeable:

```text
COMMERCIAL TRUTH
"Order 42 was agreed at $12/unit for 10 units."
             |
             v
OPERATIONAL TRUTH
"Order 42 completed at t=500."
             |
             v
FINANCIAL TRUTH
"That completion caused these ledger postings."
```

- **Commercial truth** — the terms a transaction was agreed under (`OrderPrice`, `OrderValue`, from the "Pricing, orders, and money" section above). Owned by the order itself, immutable once created.
- **Operational truth** — what physically/operationally happened (a job moved through routing steps, a machine went offline, an order finished production). Owned by `FactoryHandler` and peers. Operational domains **emit facts**; they do not interpret them financially.
- **Financial truth** — the financial consequence of an operational fact, under the active financial policy. Owned by Finance. Finance **owns the financial interpretation** of facts operational domains emit; it does not infer them by inspecting operational state.

Commercial terms must never be reconstructed from current offer state (that's the `OfferPrice`/`OrderPrice` distinction above). Operational completion is not itself revenue — it's a fact that Finance interprets. Financial interpretation must not happen inside Factory.

### Why a Finance domain, deliberately minimal

Arcogine has a minimal Finance domain — a deliberately minimal double-entry ledger — so monetary concepts (revenue, cost, cash, receivables) have a clear owner instead of leaking into Factory or Economy. This is not a decision to build sophisticated accounting — the ledger's policy is intentionally the smallest thing that's still correct:

1. Single currency.
2. Customer settlement is immediate when an order completes (no Accounts Receivable yet).
3. No Accounts Payable, payment terms, tax, depreciation, or financing.
4. No inventory accounting.
5. No GAAP/IFRS revenue-recognition policy.

Under these assumptions, an `OrderCompleted` event with value $120 produces exactly:

```text
DR Cash     120
CR Sales    120
```

The point of keeping Finance this minimal is that future financial sophistication (payment terms, receivables, tax) should change Finance's internal policy, not force Factory or Economy to grow accounting concepts. For example, adding payment terms would change only the postings Finance makes — `OrderCompleted` still fires the same way, but Finance would post to `AccountsReceivable` instead of `Cash`, and a later `PaymentReceived` event would move it to `Cash`. Factory never needs to change.

### Finance follows the same Events–State–Observations model

Finance is not a special side system — it's another state-owning domain, governed by the same invariant as everything else:

```text
Events mutate State.
State produces Observations.
Observations inform Decisions.
Decisions produce Events.
```

```text
                  EVENTS
                     |
       +-------------+-------------+
       |             |             |
       v             v             v
    Economy        Factory       Finance
     State          State         State
                                   |
                                   v
                                 Ledger
```

Financial state changes only in response to explicit events — Finance must never periodically inspect `FactoryHandler` and infer what happened. Prefer:

```text
OrderCompleted -> FinanceHandler -> Ledger
```

over `FinanceHandler` reaching into `Factory.jobs.completedJobs()` to guess at transactions. This is the same "events carry the facts a downstream domain needs" principle already established for `OrderCreation` carrying `unitPrice` — applied one hop further downstream.

### A first-class `OrderCompleted` event

`TaskEnd` means "a production step finished" — a different claim from "the order fulfilled its operational lifecycle," which is what Finance (and any operational KPI/projection) actually needs. When `FactoryHandler` detects `job.isComplete()`, it schedules a new `OrderCompleted` event (in addition to updating its own state) carrying the minimal immutable facts a downstream consumer needs to interpret the transaction — an order identifier, product, quantity, and unit price. `OrderValue` is deliberately **not** duplicated onto the event since it's a trivial, guaranteed derivation (`quantity x unitPrice`); carrying it too would just be another consistency invariant to maintain for no benefit. `FinanceHandler` reacts to `OrderCompleted`; `FactoryHandler` itself stays ignorant of what Finance does with the fact.

### A minimal double-entry ledger, not an accounting framework

Finance uses a minimal double-entry representation rather than ad-hoc accumulators (`totalRevenue`, `cash`, `profit` fields scattered across handlers). The core invariant: **for every journal entry, `sum(debit postings) == sum(credit postings)`**, enforced so that an unbalanced entry cannot enter financial state at all — `JournalEntry`'s constructor rejects one outright. The mechanism stays small enough to read in one sitting: `Account`/`Posting`/`JournalEntry` with a two-account chart of accounts (`Cash`, `Sales`), not a chart-of-accounts system, plugin architecture, or GAAP/IFRS policy engine.

**Money representation**: `double` is not an appropriate representation for ledger amounts — a balance invariant (`debits == credits`) should not rely on floating-point epsilon comparisons. The boundary:

- Economic model calculations (`PricingState`, `DemandModel`) keep using `double` — no reason to destabilize already-tested code for values that were never meant to be exact currency. This is safe for Arcogine's determinism contract specifically because `double` arithmetic is IEEE-754 deterministic given a fixed operation order — same seed, same sequence of operations, same bits, every run. What `double` doesn't give you is *exact decimal equality*, which only matters where something actually checks it as an invariant — nothing does in the economic model.
- Commercial transaction creation (`OrderPrice`/`OrderValue`, the `OrderCompleted` event) also keeps `double` for the same reason — changing this would ripple through `Job`, `FactoryHandler`, and their tests for a value that isn't yet entering a balance-checked ledger.
- The Finance ledger itself (`Posting`/`JournalEntry` amounts) uses `BigDecimal` from the start, converting at the `FinanceHandler` boundary (where an event's `double` orderValue becomes a precise `BigDecimal` posting amount) — this is the one place the balance invariant is actually checked, so it's the one place that needs exactness.

This keeps the conversion boundary in exactly one place instead of threading `BigDecimal` through code that doesn't need it yet.

**Canonical rounding policy**: converting `double` to `BigDecimal` without a stated scale/rounding rule would just move floating-point artifacts across the boundary instead of resolving them — two independent conversions of the same economic quantity could round differently and appear to disagree. `com.arcogine.finance.ledger.CurrencyPolicy` is the single, explicit answer: amounts entering Finance are quantized to 2 decimal places using `RoundingMode.HALF_UP`, applied once, at the `FinanceHandler` boundary. This is a quantization rule for Arcogine's one simulation currency, not a multi-currency policy.

### Ownership table

| Concept | Meaning | Owner |
|---|---|---|
| `ObservedMarketPrice` | External market signal | Future environment/market domain; not currently required |
| `OfferPrice` | Firm's current asking price | Economy/Pricing (`PricingState`) |
| Demand state | — | Economy (`DemandModel`) |
| `OrderPrice` | Price agreed for an accepted order | Commercial/transaction fact — carried into Factory with the order (currently on `Job`), not conceptually owned by Factory |
| `OrderValue` | Quantity × `OrderPrice` | Derived from `OrderPrice` (currently `Job.orderValue()`) |
| Production state (machines, jobs, queues, job status) | — | Factory (`FactoryHandler`) |
| Order completion | Operational fact | Factory-owned, expressed as `OrderCompleted` |
| Backlog / throughput / lead time | — | Factory, or a KPI/projection layer over it |
| Financial postings | Financial consequence of relevant events | Finance |
| Cash | — | Finance |
| Sales (financial balance) | — | Finance |
| Future receivables/payables | — | Finance |

The key invariant: **the environment may inform the `OfferPrice`; the firm controls the `OfferPrice`; accepting an order freezes that price into the `OrderPrice`; Finance later consumes the resulting immutable commercial facts.** More generally: **operational domains emit facts; Finance owns the financial interpretation of those facts.**

### Agent and observation boundaries stay purpose-specific

Adding Finance must not become an excuse to introduce a universal `WorldState` or `EverythingObservation` exposing all mutable state to every agent. A `SalesAgent` observes `OfferPrice`, backlog, lead time, `CompletedSalesValue` — commercial/operational concerns. A future `FinanceAgent` would observe Finance's own purpose-specific projection (cash, sales balance, receivables) — it would not receive `SalesAgent`'s observation type, and `SalesAgent` would not receive Finance's. Each domain's observation stays scoped to what its own consumers need, per the [Observations](#observations) rules above.

### Non-goal: sophisticated accounting

Out of scope: GAAP/IFRS compliance, configurable revenue-recognition frameworks, accounts receivable/payable unless a scenario needs them, tax, depreciation, multi-currency, debt/equity financing, inventory accounting, budgeting, forecasting, or fiscal periods. A minimal double-entry ledger with an immediate-settlement policy is not that — it's the intentional current architecture, sized to establish ownership rather than sophistication. See [`devel/architecture-assessment-events-state-observations.md`](../devel/architecture-assessment-events-state-observations.md) for the remaining migration plan.

## Discrete-Event Simulation (DES)

The simulation advances via discrete events rather than fixed time steps:

- **Order creation** — new demand enters the system, its unit price locked in at this instant
- **Task start / end** — production work begins and completes
- **Order completed** — the operational fact that an order fulfilled its full routing, distinct from a single `TaskEnd`; see "Commercial, Operational, and Financial Truth" above
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
├── sim-finance/    Finance domain: FinanceHandler, Ledger, Account, Posting,
│                   JournalEntry, FinanceObservation — see "Commercial,
│                   Operational, and Financial Truth" above
├── sim-api/        Spring Boot HTTP + SSE server: controllers, SimThread,
│                   IntegratedHandler, SnapshotBuilder, DTOs
└── sim-cli/        Picocli CLI entry point: serve + headless run modes
```

### Dependency graph

```text
sim-types ← sim-core ← sim-factory
                      ← sim-economy
                      ← sim-agents
                      ← sim-finance
                           ↑
            sim-api ←──────┘ (all of the above)
                ↑
            sim-cli (entry point)
```

Each module exposes a clean public API and hides implementation details. Domain modules (`sim-factory`, `sim-economy`, `sim-agents`) implement the `EventHandler` interface and are wired together by `IntegratedHandler` in the API layer.

## Event Dispatch Architecture

Events flow through a chain of handlers in deterministic order:

```text
Scheduler (priority queue by SimTime, FIFO among same-tick events)
    │
    ▼
IntegratedHandler
    ├── PricingState.handleEvent()
    ├── DemandModel.handleEvent()      ← reads OfferPrice/leadTime on demand, via suppliers
    ├── FactoryHandler.handleEvent()   ← may schedule OrderCompleted
    ├── FinanceHandler.handleEvent()   ← reacts to OrderCompleted
    └── SalesAgent.handleEvent()       ← only on AgentEvaluation, if enabled
```

The `EventHandler` interface:

```java
public interface EventHandler {
    void handleEvent(Event event, Scheduler scheduler);
}
```

Handlers may schedule new events via the `Scheduler` but never reach into other handlers directly, and never receive a mutable reference to another handler's internals. `DemandModel` reads `OfferPrice`/lead time on demand via `DoubleSupplier`s bound at construction (not pushed copies); `FactoryHandler` never references `PricingState` at all — it only needs each order's own `OrderPrice`, captured once at `OrderCreation`. `AgentObservation` construction lives in a dedicated `AgentObservationProjector`, not inlined into `IntegratedHandler`. Every command (`ChangePrice`, `ChangeMachine`, `ToggleAgent`) becomes a domain event dispatched the same way — none of them bypass the event system.

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
