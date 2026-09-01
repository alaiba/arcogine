# Arcogine — Architectural Overview

This document sits under the [Product Charter](/docs/product/charter.md), which defines Arcogine's enduring product direction and principles. This document describes the design philosophy and architectural principles that guide Arcogine's implementation *today*, and distinguishes principles expected to persist regardless of implementation from constraints specific to the current MVP. For the rationale behind specific significant decisions and their history, see [Architecture Decision Records](decisions/README.md).

## Enduring architectural principles

These are expected to hold regardless of how the implementation evolves, because they follow directly from the Product Charter:

1. Repository must be reproducible, modular, testable, and collaboration-ready.
2. Deterministic acceptance tests and scenario-level validation are mandatory for simulation, replay, and verification contexts (see the [Determinism Contract](#determinism-contract) below for scope).
3. Agents only use approved command interfaces and never mutate simulation state directly — this is the same governance boundary the Charter asks of human and autonomous decision-makers alike.

## Current implementation constraints (MVP)

These describe today's implementation choices. They are not claims about Arcogine's permanent identity — see the Product Charter's [product boundaries](/docs/product/charter.md#9-what-arcogine-is-not) for why Java, the current UI, and the current deployment model are implementation choices rather than product identity, subject to change as the product grows toward the full lifecycle described there.

1. Core simulation is written in Java with a **Java 21 language/API/bytecode compatibility baseline**. The preferred devcontainer currently uses JDK 25, and CI runs on JDK 21 to prove the supported floor; the compiler JDK and compatibility baseline are deliberately separate concerns.
2. The headless simulation core is the current implementation's primary layer; the UI and API are additive consumers of it. This describes today's layering, not a permanent claim that Arcogine's mature product surface is UI-secondary.
3. MVP ties factory flow to the economy loop.
4. Support native and containerized local execution.
5. The current UI is a single-user experiment console — one current mode of engaging with Arcogine (see the Charter's [modes of engagement](/docs/product/charter.md#5-modes-of-engagement-not-personas)), not "the Arcogine UX" in the mature-product sense, and not a game client.
6. Security-sensitive defaults remain local-first by default; non-local exposure requires explicit hardening controls (see [SECURITY.md](/.github/SECURITY.md)).

## Architectural implications of the Product Charter

Consequences of the Charter's thesis, stated at the conceptual level only — none of this is a module design, schema, or implementation commitment; see the Charter's [Architectural implications](/docs/product/charter.md#7-architectural-implications) section for the full list:

- Arcogine should not evolve separate simulation-only and production-only domain semantics.
- Model, version, and provenance concepts become fundamental once changes can move from design to reality — today's `EventLog` and deterministic replay are an early, simulation-scoped instance of this, not the final answer.
- Purpose-specific observations and capabilities (already the pattern for `AgentObservation`/`FinanceObservation`, see [Observations](#observations) below) are preferable to exposing unrestricted mutable state, and are expected to remain so as new consumers (human roles, external systems, execution surfaces) are added.
- Real execution, when it exists, introduces safety, authorization, auditability, failure, and operational consequence as architectural concerns — the current implementation does not yet need to solve these because it does not yet execute anything real (see [SECURITY.md](/.github/SECURITY.md)).

## Simulation-First (current implementation)

Today's system is built around a **headless simulation core**, not a game engine. This describes the current implementation's architecture — simulation is a major Arcogine capability (per the Product Charter), not the entirety of its identity.

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

Each subsystem exclusively owns its mutable domain state. Pricing owns `OfferPrice` and its history (`PricingState`) — the firm's own current asking price, not any individual order's terms and not an external market signal. Factory owns accepted orders, machines, jobs, queues, completion state, and production metrics (`FactoryHandler`), including the cached `CompletedSalesValue` KPI state — but the immutable accepted order and completed execution facts, not the cache, remain the authoritative facts it's derived from (see the "stored incrementally" note below). A future inventory subsystem would own stock; finance would own financial state; workforce would own labor state.

Commercial/order intent and mutable production execution are represented separately. `Order` is immutable accepted intent; its same `OrderId` identifies an authoritative order-execution aggregate. Acceptance deterministically materializes one unit-quantity `Job` per requested unit, in zero-based ordinal order. Each child has its own `JobId`, traverses the routing once, and can be dispatched independently under the existing selector. The aggregate records release and completion quantities and is the sole source for order completion, backlog, sales KPIs, and lead time; only its final child completion emits `OrderCompleted` with both identities and full order commercial facts.

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
| Accepted order intent (`OrderId`, product, quantity, creation time, `OrderPrice`) | immutable `Order` in `OrderStore` (owned by `FactoryHandler`) | Created at `OrderCreation`; immutable thereafter |
| Jobs, job status (production lifecycle) | `JobStore` (owned by `FactoryHandler`) | `OrderCreation` (creates), `TaskEnd` (advances/completes) |
| `OrderValue` | Derived by immutable `Order` from quantity × `OrderPrice` | Derived, not separately mutated |
| `CompletedSalesValue`, `completedSales` | `FactoryHandler` | `TaskEnd` (once when the order-level execution aggregate completes) |
| Ledger, `Cash`/`Sales` balances | `Ledger` (owned by `FinanceHandler`) | `OrderCompleted` |
| `SalesAgent`'s last observation, intervention count | `SalesAgent` | `observe(...)` (called by `IntegratedHandler`), `AgentEvaluation` |
| `IntegratedHandler.agentEnabled` | `IntegratedHandler` | `AgentEnabledChanged` |
| `EventLog` | `EventLog` (owned by `SimThread`) | every dispatched internal `Event`; current legacy `SimThread` paths append and notify SSE listeners before `handleEvent(...)` |
| Published API snapshot | `AtomicReference<SimSnapshot>` (owned by `SimThread`) | `SnapshotBuilder.buildSnapshot(...)`, called after each processed event/batch |

The current `SimThread` ordering in the `EventLog` row is legacy API behavior, not the target supported event contract. [ADR-0011](decisions/0011-runtime-observation-and-event-contract.md) requires supported `RuntimeEvent` state-change facts to be derived/published only after authoritative processing succeeds.

`DemandModel` reads `OfferPrice` and lead time on demand, via `DoubleSupplier`s bound to `PricingState`/`FactoryHandler` at construction — it has no state of its own to keep in sync, so it isn't listed as an owner above.

Order/job creation is not exclusively event-driven: `FactoryRuntime.submitWorkload(productId, quantity, unitPrice)` is the supported, consumer-neutral entry point a caller uses to submit production workload directly, with no economy/pricing/demand/agent dependency and no need to own a `Scheduler` or choose a simulation time. `FactoryRuntime` is only built via `FactoryRuntime.forModel(FactoryModelVersion)`, which assembles and owns its own exclusive `FactoryHandler`/`Scheduler` pair — it is never wrapped around an already-live `FactoryHandler` another scheduler might also be driving, and it does not expose that `FactoryHandler` directly (callers observe state through its own read-only projections instead). It resolves to the same package-private `FactoryHandler.submitOrder(...)` acceptance operation that the `OrderCreation` event handled above calls — the one `DemandModel` schedules. Both routes create the same immutable `Order` and deterministically materialize quantity-`N` as `N` unit-quantity sibling `Job`s under the same `OrderId` aggregate, with identical routing/dispatch semantics; `submitOrder` itself is not public, so scheduler/time plumbing never leaks past `FactoryHandler`.

`FactoryRuntime` also implements Gate 3's consumer-neutral session-control semantics (see [Factory Simulation Engine Readiness](../planning/factory-simulation-engine-readiness.md) §7 and [ADR-0007](decisions/0007-gate-3-session-control-primitives.md)), additive to the shape above: `modelVersion()` retains and exposes the exact `FactoryModelVersion` the session was instantiated from, for the session's full lifetime; `advanceUntil(SimTime targetTime, long maxEvents)` sits alongside the unchanged single-event `advance()`, processing pending events one at a time until either the next event's time would exceed `targetTime` or `maxEvents` events have been processed, implemented directly in terms of `advance()` so the two can never diverge in ordering or dispatch behavior; `reset()` returns a fresh `FactoryRuntime.forModel(modelVersion())` rather than mutating the existing session in place, since `FactoryHandler`'s stores have no partial-reset subsystem to mutate safely. `submitWorkload` and `setMachineAvailability` — the two externally initiated runtime changes `FactoryRuntime` exposes — always return a definite `CommandResult<T>` (a stable code/diagnostic, `modelVersion()` provenance, and every `Event` scheduled as a direct effect of the command, captured by a command-scoped `RecordingScheduler` window rather than a permanently growing history) instead of ever throwing or returning `void`. `CommandResult` is a three-way sealed type: `Accepted`, `Rejected` (wraps the original, already-structured, sealed `SimError`; verified pre-mutation — `FactoryHandler.submitOrder` preflights its scheduling check before mutating any store, `setMachineAvailability` verifies its own two rejectable conditions from `machinesView()` before calling into `FactoryHandler` at all — so a `Rejected` result never follows partial mutation), and `Faulted` (a genuine engine fault surfacing from deep in `setMachineAvailability`'s online-machine dispatch cascade, after mutation may already have started; making that whole cascade provably preflight-safe was judged disproportionate, so `Faulted` reports it as a definite result instead of letting it throw past the command boundary, while making clear — unlike `Rejected` — that it does not promise zero mutation). Acceptance and execution outcome are independent facts, not one axis, so `Faulted` carries the same accepted value `Accepted` would have alongside the fault — the requested change genuinely was applied before the later failure, and a caller must not lose which entity was affected just because execution subsequently failed. `pendingWorkView()` exposes `FactoryHandler`'s cross-machine `pendingMultiEligible` backlog (from Gate 2/ADR-0005) as read-only `PendingWorkView` entries — necessary because that waiting work is not associated with any single machine and so is invisible to `MachineView.queueDepth()`.

Gate 4-A adds `FactoryRuntime.observe()` as the separate supported current-state boundary required by [ADR-0011](decisions/0011-runtime-observation-and-event-contract.md). It returns immutable, deterministically ordered resource, aggregate-order, W1 child-job, and multi-eligible-pending-work projections plus the factory's authoritative backlog, completed-order/value, lead-time, and throughput calculations. Metadata carries an opaque per-runtime `RunId`, `FactoryModelVersion.fingerprint()` durable provenance, current simulated time, explicit active/quiescent advancement state, and a `latestEventSequence` of zero until G4-B introduces supported runtime events. This projection never exposes `FactoryHandler`, mutable stores, or internal scheduler events; the legacy API/SSE still projects its existing internal-event behavior until the later G4-D migration.

### Observations

Observations are immutable, read-only projections of current simulation state, purpose-built for consumers that need information but must not own or mutate it — agents, decision policies, demand models, experiments, reporting/evaluation components. `AgentObservation` is the canonical example.

Observations should:

- be derived from authoritative state, computed on demand rather than cached as a second source of truth;
- be purpose-specific — expose what the consumer needs, not the internals of the owning subsystem;
- be immutable;
- define the capability and visibility boundary for whoever consumes them (an agent can only act on what its observation exposes).

### Domain observations vs. API/UI snapshots

Two different things are easy to conflate because they can look similar in shape: a domain observation (e.g. `AgentObservation`, `FinanceObservation`) and an API/UI snapshot (e.g. `SnapshotBuilder`'s DTOs, `SimSnapshot`). The distinction is about audience and lifecycle, not just structure:

- A **domain observation** exists to support a decision made *inside* the simulation, this tick, by a consumer that is itself part of the deterministic event loop (an agent, a policy, a future evaluation component). It is scoped to exactly what that decision needs, is constructed fresh from authoritative state, and is never serialized or versioned — its contract is Java-internal.
- An **API/UI snapshot** exists to serialize simulation state *outward*, to an external, non-deterministic consumer (an HTTP client, the UI) that is not part of the simulation loop and does not make simulation decisions. It has a wire contract (JSON field names, versioning concerns) that a domain observation must never be shaped by.

Concretely: `FinanceObservation` (cash, sales balance, as `BigDecimal`) is what a future `FinanceAgent` would read to decide something *inside* the tick. `SimSnapshot`'s finance-facing fields, if ever added, would be what the UI reads to *display* the same underlying ledger state, independently shaped by JSON/display concerns (e.g. rounding for presentation, field names following the `snake_case` DTO convention rather than domain vocabulary). The two are allowed to report the same numbers; they must never be the same type, and a domain handler must never accept a DTO as an argument or return one.

The practical rule: if you find yourself passing a `SimSnapshot`/`JobInfo`/other DTO into a handler or agent to make a simulation decision, that's the DTO being used as an ad hoc internal read model — introduce or extend a domain observation instead. `SnapshotBuilder` is the one place allowed to read domain state broadly, precisely because its output never re-enters the simulation.

**Known compatibility debt**: `JobInfo.revenue` (JSON field `revenue`) and `SimSnapshot.totalRevenue`/`currentPrice` (JSON fields `total_revenue`/`current_price`) still use pre-rename vocabulary — `revenue`/`totalRevenue` instead of `CompletedSalesValue`, `currentPrice` instead of `OfferPrice` — even though the domain model has since converged on the latter (see the Terminology table above). These are left unrenamed deliberately, as an external wire-contract boundary, not an oversight — renaming a public JSON field is a breaking API change, out of scope for an internal vocabulary cleanup. They are explicitly flagged, in code and here, as debt to resolve in a future API-versioning change, not a naming decision anyone should treat as settled or extend by adding more `revenue`-named fields.

### Query dependencies between domains

`DemandModel` reads `OfferPrice` and lead time via `DoubleSupplier`s bound to `PricingState`/`FactoryHandler` at construction, rather than an interface type — deliberately, because the dependency is a single scalar per call. That is the general rule, not a special case:

- **A single scalar (or a handful of independently-meaningful scalars), read without any relationship between them** → a bound `Supplier`/`DoubleSupplier`/similar functional read is enough. It costs nothing to add, doesn't require a new named type, and makes the dependency's narrowness obvious at the call site (a `DoubleSupplier` cannot accidentally expose more than one `double`).
- **A read contract that is multi-field, or where the fields are semantically related and should be read together as one consistent snapshot** → introduce a purpose-specific interface or a small observation record instead (the way `AgentObservation`/`FinanceObservation` already do for their consumers). The signal that a supplier has outgrown itself is needing *two or more* suppliers from the same domain in the same consumer to represent what is really one coherent fact.

This avoids both extremes: raw concrete dependencies on another domain's mutable class (which would violate the state-ownership rule above), and a proliferation of tiny single-method interfaces for every scalar read. When in doubt, prefer the supplier until a second correlated field is actually needed — don't pre-build the interface for a dependency that doesn't exist yet.

### Decisions

Decisions are an important consequence of this model, even though they are not one of the three top-level concepts:

```text
Observation -> Decision -> Event
```

Agents and policies observe, decide, and emit events — they never directly mutate simulation state. `SalesAgent.decide()` is a pure function over an `AgentObservation`; when it decides to act, it schedules `PriceChange`/`AgentDecision` events rather than calling a setter on `PricingState`. This is the pattern all future decision-making code should follow.

### Pricing, orders, and money: OfferPrice vs. OrderPrice

`price` is not one universal simulation value. Arcogine distinguishes:

| Concept | Meaning | Owner / location | Mutability |
|---|---|---|---|
| `ObservedMarketPrice` | External/environmental market signal — what the broader market says the product is worth, or what comparable products are being offered for. **Not implemented**: reserved for a future external-market/environment domain. Do not use this name for the firm's own price. | Future environment/market domain | — |
| **OfferPrice** | The simulated firm's current asking price — mutable commercial state controlled by pricing policy/agents; what the demand model actually responds to today. | Economy/Pricing (`PricingState`) | Mutable — changes on `PriceChange` events |
| **OrderPrice** (unit price) | The price agreed when a specific order was created — `OfferPrice` at that instant, frozen. | Immutable `Order`, captured at `OrderCreation`; `JobView` may project it for compatibility | Immutable once the order exists |
| **OrderValue** | `quantity × OrderPrice` for one order. | Derived by immutable `Order` | Derived (not separately mutated) |
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

`CompletedSalesValue` and `completedSales` (the count) are **stored incrementally, not derived on read** — `FactoryHandler` increments both exactly once when the order-level execution aggregate reaches completion on its final child `TaskEnd`, rather than once per completed child job or by rescanning all jobs on read. They are cached order-level aggregates with an invariant that must hold at every point in the simulation:

```text
CompletedSalesValue = Sum(order.orderValue() for completed order-execution aggregates)
completedSales      = Count(completed order-execution aggregates)
```

The authoritative facts are each immutable `Order` plus its order-level execution aggregate; individual child completion is only progress toward that aggregate. `IntraOrderExecutionAcceptanceTest.quantityTwentyCreatesDeterministicChildrenAndOneAggregateCompletion` proves the W1 shape: twenty child jobs complete under one order, while `completedSales == 1` and `CompletedSalesValue` is incremented once by the parent order value.

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

These guardrails are part of the current architecture and are reinforced by executable architecture tests where the invariant can be checked mechanically. Remaining runtime-readiness work is tracked separately in [Factory Simulation Engine Readiness](../planning/factory-simulation-engine-readiness.md); that document is planning guidance, not architectural authority.

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

- **Commercial truth** — the terms a transaction was agreed under (`OrderPrice`, `OrderValue`, from the "Pricing, orders, and money" section above). Owned by the immutable accepted order itself.
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

**`Sales` is a model-specific account, not a claim of standards-compliant revenue recognition.** Under GAAP/IFRS, when revenue may be recognized (and under what conditions) is its own body of policy — performance obligations, variable consideration, contract modifications, and so on. Arcogine's `Sales` account is the credit side of the immediate-settlement posting this simplified model makes on `OrderCompleted`; it is intentionally named after what it structurally is (a credit-normal financial balance) rather than implying it satisfies any accounting standard. If a future scenario needs actual revenue-recognition policy, that's new Finance-domain logic layered on top of (or replacing) this posting rule — not a reinterpretation of what `Sales` already means today.

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

`TaskEnd` means "a production step finished" — a different claim from "the order fulfilled its operational lifecycle," which is what Finance (and any operational KPI/projection) actually needs. When the final child causes the order-level execution aggregate to complete, `FactoryHandler` schedules exactly one `OrderCompleted` event (in addition to updating its own state). The current payload carries both the authoritative `OrderId` and the completing child `JobId`, plus the minimal immutable order facts a downstream consumer needs to interpret the transaction: product, quantity, and unit price. `OrderValue` is deliberately **not** duplicated onto the event since it's a trivial, guaranteed derivation (`quantity x unitPrice`); carrying it too would just be another consistency invariant to maintain for no benefit. `FinanceHandler` reacts to `OrderCompleted`; `FactoryHandler` itself stays ignorant of what Finance does with the fact.

### A minimal double-entry ledger, not an accounting framework

Finance uses a minimal double-entry representation rather than ad-hoc accumulators (`totalRevenue`, `cash`, `profit` fields scattered across handlers). The core invariant: **for every journal entry, `sum(debit postings) == sum(credit postings)`**, enforced so that an unbalanced entry cannot enter financial state at all — `JournalEntry`'s constructor rejects one outright. The mechanism stays small enough to read in one sitting: `Account`/`Posting`/`JournalEntry` with a two-account chart of accounts (`Cash`, `Sales`), not a chart-of-accounts system, plugin architecture, or GAAP/IFRS policy engine.

**Money representation**: `double` is not an appropriate representation for ledger amounts — a balance invariant (`debits == credits`) should not rely on floating-point epsilon comparisons. The boundary:

- Economic model calculations (`PricingState`, `DemandModel`) keep using `double` — no reason to destabilize already-tested code for values that were never meant to be exact currency. This is safe for Arcogine's determinism contract specifically because `double` arithmetic is IEEE-754 deterministic given a fixed operation order — same seed, same sequence of operations, same bits, every run. What `double` doesn't give you is *exact decimal equality*, which only matters where something actually checks it as an invariant — nothing does in the economic model.
- Commercial transaction creation (`OrderPrice`/`OrderValue`, the immutable `Order`, and the `OrderCompleted` event) also keeps `double` for the same reason — changing this would ripple through accepted-order construction, `FactoryHandler`, Finance's event boundary, and their tests for a value that isn't yet entering a balance-checked ledger.
- The Finance ledger itself (`Posting`/`JournalEntry` amounts) uses `BigDecimal` from the start, converting at the `FinanceHandler` boundary (where an event's `double` orderValue becomes a precise `BigDecimal` posting amount) — this is the one place the balance invariant is actually checked, so it's the one place that needs exactness.

This keeps the conversion boundary in exactly one place instead of threading `BigDecimal` through code that doesn't need it yet.

**Canonical rounding policy**: converting `double` to `BigDecimal` without a stated scale/rounding rule would just move floating-point artifacts across the boundary instead of resolving them — two independent conversions of the same economic quantity could round differently and appear to disagree. `com.arcogine.finance.ledger.CurrencyPolicy` is the single, explicit answer: amounts entering Finance are quantized to 2 decimal places using `RoundingMode.HALF_UP`, applied once, at the `FinanceHandler` boundary. This is a quantization rule for Arcogine's one simulation currency, not a multi-currency policy.

**The ledger amount is authoritative.** Once `CurrencyPolicy` has quantized an `OrderValue` into a posted `Posting`/`JournalEntry` amount, that `BigDecimal` — not the originating `double` `OrderValue` — is the financially authoritative figure for that transaction. The two are expected to agree to the cent for realistic scenario values, but nothing guarantees bit-for-bit equality between a raw `double` product and its quantized `BigDecimal` counterpart, and no code should assert exact equality between them. If a future scenario ever needs commercial and financial amounts to reconcile exactly, that reconciliation belongs in Finance (comparing quantized amounts to quantized amounts), not as an assumption that `OrderValue` and the posted amount are the same value under two representations.

### Ownership table

| Concept | Meaning | Owner |
|---|---|---|
| `ObservedMarketPrice` | External market signal | Future environment/market domain; not currently required |
| `OfferPrice` | Firm's current asking price | Economy/Pricing (`PricingState`) |
| Demand state | — | Economy (`DemandModel`) |
| `OrderPrice` | Price agreed for an accepted order | Immutable Factory `Order` created from the commercial `OrderCreation` fact |
| `OrderValue` | Quantity × `OrderPrice` | Derived by `Order.orderValue()` |
| Production state (machines, jobs, queues, job status) | — | Factory (`FactoryHandler`) |
| Order completion | Operational fact | Factory-owned, expressed as `OrderCompleted` |
| `CompletedSalesValue`, `completedSales` | Cached aggregates, not derived-on-read — see note below | Factory (`FactoryHandler`), incremented once on aggregate order completion |
| Backlog / throughput / lead time | — | Factory, or a KPI/projection layer over it |
| Financial postings | Financial consequence of relevant events | Finance |
| Cash | — | Finance |
| Sales (financial balance) | — | Finance |
| Future receivables/payables | — | Finance |

The key invariant: **the environment may inform the `OfferPrice`; the firm controls the `OfferPrice`; accepting an order freezes that price into the `OrderPrice`; Finance later consumes the resulting immutable commercial facts.** More generally: **operational domains emit facts; Finance owns the financial interpretation of those facts.**

### Agent and observation boundaries stay purpose-specific

Adding Finance must not become an excuse to introduce a universal `WorldState` or `EverythingObservation` exposing all mutable state to every agent. A `SalesAgent` observes `OfferPrice`, backlog, lead time, `CompletedSalesValue` — commercial/operational concerns. A future `FinanceAgent` would observe Finance's own purpose-specific projection (cash, sales balance, receivables) — it would not receive `SalesAgent`'s observation type, and `SalesAgent` would not receive Finance's. Each domain's observation stays scoped to what its own consumers need, per the [Observations](#observations) rules above.

### Non-goal: sophisticated accounting

Out of scope: GAAP/IFRS compliance, configurable revenue-recognition frameworks, accounts receivable/payable unless a scenario needs them, tax, depreciation, multi-currency, debt/equity financing, inventory accounting, budgeting, forecasting, or fiscal periods. A minimal double-entry ledger with an immediate-settlement policy is not that — it's the intentional current architecture, sized to establish ownership rather than sophistication. Further finance capability should be introduced through an explicit planning and decision record when requirements justify it, rather than inferred from removed migration notes.

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

### Event taxonomy

Not every `EventPayload` plays the same role, even though all of them flow through the same `Scheduler`/`IntegratedHandler` mechanism uniformly. Distinguishing the roles helps reason about a given event without changing how any of them are dispatched:

- **Domain events** — facts about simulation state changing, owned by exactly one domain: `OrderCreation`, `TaskEnd`, `OrderCompleted`, `MachineAvailabilityChange`, `PriceChange`. These are what the Events–State–Observations invariant is fundamentally about.
- **Evaluation/timer events** — periodic triggers with no state-owning payload of their own, whose purpose is to cause a domain to re-evaluate: `DemandEvaluation`, `AgentEvaluation`. They don't carry a fact so much as invoke a domain's own decision logic on schedule.
- **Orchestration/control events** — signals about how the simulation is being run or which decision sources are active, rather than facts about the simulated world: `AgentEnabledChanged`. This is why `AgentEnabledChanged` is handled by `IntegratedHandler` itself (toggling whether `SalesAgent` participates in dispatch) rather than by a domain handler that owns simulation state — it controls the orchestrator's behavior, not a domain's.

All three kinds remain scheduled `Event`s through the same `Scheduler`, deliberately — this taxonomy is a reading aid, not a proposal to split them into different mechanisms (that would reintroduce exactly the kind of special-casing the event system exists to avoid). It exists so a contributor adding a new event can ask "which of these three is this?" and get a clear answer, rather than defaulting every new signal into "domain event" whether or not it actually represents domain state changing.

### When a new domain deserves its own module

Not every new metric or piece of derived behavior warrants a new `XHandler`/module — `sim-finance` was justified by more than "it computes a number Factory doesn't." The admission rule: **a new domain deserves its own handler/module when it owns mutable state with its own invariants and lifecycle, not merely because it has a new metric or helper function.** Concretely, ask:

- Does it own state that nothing else should be able to mutate directly (the way `Ledger` owns postings, or `FactoryHandler` owns job/machine lifecycle)?
- Does that state have its own invariants worth protecting at construction/mutation time (the way `JournalEntry` rejects unbalanced entries)?
- Does it react to events from other domains and produce its own facts, rather than just recomputing a view over another domain's existing state?

If the answer is genuinely yes to state-with-invariants, it's a domain — a new `XHandler implements EventHandler`, its own `XObservation`, one line in `IntegratedHandler`'s explicit dispatch sequence (see [Event Dispatch Architecture](#event-dispatch-architecture)). If the answer is no — it's a computed value over state another domain already owns — it belongs as a method/projection on the existing owner (like `FactoryHandler.completedSalesValue()`) or in a KPI/projection layer, not a new module. This keeps the module count matched to genuine ownership boundaries instead of granularity of features.

## Module Structure

The Java codebase follows a **modular monolith** pattern with Gradle multi-module layout, rooted at `product/`:

```text
product/
├── types/                DES primitives: SimTime, MachineId, ProductId, OrderId, JobId,
│                         Quantity, SimError, scenario config records
├── governance/           Controlled-revision identity, lineage, recording provenance,
│                         authoritative durable history, and historical resolution;
│                         current filesystem adapter, no simulation event handling
├── simulation/           DES engine: Scheduler, Event, EventHandler interface,
│                         CompositeHandler, EventLog, KPIs, SimRunner, ScenarioLoader
├── domains/
│   ├── factory/          Factory domain: Machine, immutable Order, mutable Job, Routing,
│   │                     FactoryHandler
│   ├── economy/          Economic layer: PricingState, DemandModel
│   └── finance/          Finance domain: FinanceHandler, Ledger, Account, Posting,
│                         JournalEntry, FinanceObservation — see "Commercial,
│                         Operational, and Financial Truth" above
├── agents/               Agent framework: SalesAgent, AgentObservation
├── consumer/
│   └── challenge/        Challenge Readiness: game-owned challenge definition/validation,
│                         catalogue/economics, candidate admissibility, deterministic
│                         challenge evaluation, and attempt provenance/design-to-design
│                         comparison (see
│                         docs/planning/factory-design-game-challenge-readiness.md).
│                         Headless — no dependency on any module below.
└── interfaces/
    ├── api/              Spring Boot HTTP + SSE server: controllers, SimThread,
    │                     IntegratedHandler, SnapshotBuilder, DTOs
    ├── cli/               Picocli CLI entry point: serve + headless run modes
    └── web/               React/TypeScript experiment console
```

### Dependency graph

```text
types ← simulation ← factory
                    ← economy
                    ← agents
                    ← finance
                         ↑
            api ←────────┘ (all of the above)
                ↑
            cli (entry point)

types ← governance

challenge   (no dependency on any module above; a sibling, game-owned boundary)
```

Each module exposes a clean public API and hides implementation details. Event-handling modules (`factory`, `economy`, `agents`, `finance`) implement the `EventHandler` interface and are wired together by `IntegratedHandler` in the API layer. Governance's production dependency remains on `:types`; the factory-domain `factory-model:v1` artifact codec stays domain-owned and is supplied through the Governance `SemanticArtifactVerifier` port rather than introducing a Governance-to-factory production dependency.

`challenge` is deliberately outside this dependency graph: it is a game-owned Challenge Readiness
module (`com.arcogine.challenge`) that has no `project(...)` dependency on `types`, `simulation`,
any domain module, `api`, or `cli`, and no Spring dependency. It defines immutable challenge
definitions and validation, game-owned catalogue/economics, deterministic candidate admissibility,
deterministic challenge evaluation over supplied authoritative outcome facts, and immutable attempt
provenance with deterministic design-to-design comparison. These are distinct validation domains
from `FactoryModelValidator` and do not inspect factory/runtime state — see the Challenge Readiness
planning doc for the ownership boundary.

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

Java features available within the **Java 21 compatibility baseline** map cleanly to the domain:

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
- Java strict floating-point semantics; compilation targets the Java 21 compatibility baseline
- No concurrent mutation of simulation state

Given identical scenario TOML and the same seed, the simulation produces identical event logs, KPIs, and final state.

This determinism contract is scoped to simulation, replay, and verification contexts, where it is a critical property. It is not a claim that real-world execution itself must be, or will be made, deterministic — production operates in a non-deterministic world of real machines, people, and failures. See the Product Charter's [continuity with current architecture](/docs/product/charter.md#8-continuity-with-current-architecture) section for this distinction.

## Factory Model Identity (current state)

Scenario factory semantics are instantiated through an implemented canonical-model seam: `FactoryModel` (validated) → `FactoryModelVersion` (immutable, published) → `FactoryRuntimeAssembler` (deterministic runtime instantiation). See [ADR-0003](decisions/0003-canonical-factory-model-boundary.md) for the accepted boundary this implements.

`FactoryModelVersion.fingerprint()` implements the durable `factory-model:v1` semantic fingerprint contract accepted by [ADR-0006](decisions/0006-durable-semantic-fingerprint-contract.md). The contract uses the typed `ModelFingerprint` value and a language-independent canonical binary encoding with explicit policy versioning and compatibility vectors. Equal canonical semantic content therefore has a durable identity that is independent of process memory and implementation language under the v1 policy.

`FactoryModelVersion.contentHash()` remains a separate legacy compatibility surface. It is deterministic for the current Java model but is not the durable fingerprint contract and historical bare content hashes must not be reinterpreted as `factory-model:v1` fingerprints. Existing `IntegratedHandler`/`SimResult.modelContentHash` provenance still carries that legacy hash; broader provenance migration and run identity (run ID, scenario/input fingerprint, engine build) remain separate follow-up concerns.

`:types` provides the opaque UUIDv4 `ControlledRevisionId` value model, and `:governance` provides the immutable `ControlledRevision`, lineage, and recording-provenance values fixed by [ADR-0008](decisions/0008-controlled-revision-identity-and-lineage.md). Governance G1 is complete: `ControlledRevisionAuthority` defines the authoritative acceptance/lookup/resolution boundary, and `accept(...)` returns the immutable accepted record after the authority establishes its `recordedAt` at the commit boundary rather than trusting the candidate's timestamp. The current `FileControlledRevisionAuthority` adapter persists append-only revision records and immutable semantic artifacts across process/reopen boundaries, rejects duplicate/rebound IDs, requires an already-authoritative parent under the current `0..1` lineage policy, verifies the supplied canonical artifact reproduces the revision's `ModelFingerprint`, and atomically installs the revision record under process/filesystem locking. Historical resolution returns the accepted immutable revision together with its exact semantic artifact; missing/corrupt metadata or artifacts and fingerprint mismatches fail explicitly rather than falling back to current model state.

The factory proving ground reuses ADR-0006's exact `factory-model:v1` canonical bytes as its historical semantic artifact. `FactoryModelArtifactV1` strictly decodes and canonical-reencodes those bytes to reconstruct the exact historical `FactoryModelVersion`, while the Governance store remains artifact-policy-agnostic through `SemanticArtifactVerifier`. Distinct revisions may therefore share one `ModelFingerprint` and one immutable artifact — including the `F1 -> F2 -> F1` rollback case — without becoming the same historical occurrence. The current filesystem record layout and locking mechanics are replaceable adapter details, not a selected permanent production persistence architecture; no new G1.3 ADR was required. Approval/authorization, ChangeSets, conformance/evidence, deployment, external change-management relationships, labels/tags/branches, and multi-parent merge semantics remain separate G2+ concerns rather than revision identity.

## API Layer

The HTTP API uses Spring Boot 4 with Spring MVC:

- REST endpoints for scenario loading, simulation control, interventions, and queries
- Server-Sent Events (SSE) via `SseEmitter` for real-time event streaming
- Simulation runs on a dedicated thread (`SimThread`) communicating via `BlockingQueue`
- `AtomicReference<SimSnapshot>` provides lock-free snapshot reads for API handlers

## Technology Stack

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Language | Java (release 21 compatibility baseline) | Records, sealed types, pattern matching |
| Framework | Spring Boot 4 | HTTP server, DI, config |
| CLI | Picocli | Command-line parsing |
| Build | Gradle (Kotlin DSL, repository wrapper) | Multi-module build; exact version pinned by `product/gradle/wrapper/gradle-wrapper.properties` |
| Config format | TOML | Scenario files (via Jackson TOML) |
| Serialization | Jackson | JSON API responses, TOML parsing |
| Testing | JUnit 6 | Unit and integration tests |
| Coverage | JaCoCo | Code coverage reporting |
| Container | Eclipse Temurin 25 | Docker runtime |
| Frontend | React 19 + TypeScript 6 | Vite, Zustand, Tailwind CSS, Recharts |
