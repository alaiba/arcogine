# Arcogine — Architectural Overview

This document sits under the [Product Charter](/docs/product/charter.md), which defines Arcogine's enduring product direction and principles. This document describes the design philosophy and architectural principles that guide Arcogine's implementation *today*, and distinguishes principles expected to persist regardless of implementation from constraints specific to the current MVP. This document and the focused architecture/specification documents it links are the current architectural authority; Git and pull-request history hold the historical rationale for how they reached their present form.

## Enduring architectural principles

These are expected to hold regardless of how the implementation evolves, because they follow directly from the Product Charter:

1. Repository must be reproducible, modular, testable, and collaboration-ready.
2. Deterministic acceptance tests and scenario-level validation are mandatory for simulation, replay, and verification contexts (see the [Determinism Contract](#determinism-contract) below for scope).
3. Agents only use approved command interfaces and never mutate simulation state directly — this is the same governance boundary the Charter asks of human and autonomous decision-makers alike.

### Domain authority

Each kind of fact has exactly one authoritative owner, and exposing a fact never transfers its
ownership:

- **Factory** owns authored production/design facts: the canonical model, its validation,
  publication and content identity ([Factory Design](factory-design.md)).
- **Engine** owns deterministic simulation interpretation and authoritative runtime state: dispatch,
  decomposition, scheduling, transfer and derived-result rules under one identified interpretation
  ([Engine Semantics](engine-semantics-v1.md), [Determinism Contract](#determinism-contract)).
- **Governance** owns controlled history, requirements/assertions, evidence use, conformance
  findings and governed change ([Governance and Conformance](governance-conformance.md)).
- **Operational** owns real-operation integration, accountable continuation and the relationship
  to external authority ([Operational Execution](operational-execution-digital-twin.md)).
- Observations, events, projections, DTOs, serializations and outward views expose these facts
  without becoming domain truth, and consumer domains (Challenge/Game, analytics, UI) never
  redefine authoritative production semantics.

### Semantic evolution and support

Arcogine attributes durable facts to named semantic definitions: a fingerprint under a named
canonicalization policy, a controlled revision bound to one historical occurrence, a run attributed
to one Engine interpretation, evidence referencing one exact source revision, an operational
continuation answering for the facts it accepted. Replay, historical explanation, conformance
evaluation, comparison and audit all depend on those references keeping the meaning they had when
they were made — while the definitions themselves still have to be correctable and extensible, and
while decoding, executing, migrating or interoperating with every definition ever named is an
open-ended cost no consumer has asked Arcogine to pay. These cross-domain rules reconcile the two
forces:

1. **Semantic identity never rebinds.** An identity denotes exactly one definition and one
   intrinsic provenance. A materially changed definition — changed field membership, canonical
   bytes, result-affecting interpretation, or the referent of a historical record — requires a
   distinguishable identity. Corrections, changed interpretations and later occurrences are new
   distinguishable things; they never rewrite what an existing identity denotes. This holds for
   content identities, historical occurrence identities, evidence references and accountable
   continuations alike, each under the equality rule its owning contract defines.
2. **Attribution fixes the whole definition.** A definition may be corrected in place only until
   the first retained or accepted record is attributed to it. From that point the whole definition
   is fixed, including rules no fixture has exercised and rejection behavior no consumer has yet
   observed.
3. **Historical meaning and continuing support are different obligations.** Retaining the exact
   definition an identity denotes, retaining content, decoding, executing, migrating and
   interoperating are separately scoped promises with their own dependencies. Naming an identity
   creates none of them automatically — treating every named definition as permanently supported
   would manufacture a compatibility estate no consumer requires and make ordinary correction
   impossible — and a digest or identifier alone never substitutes for an explanation whose basis
   was not retained.
4. **Retained attribution requires resolvable definitions.** Every identity stamped on a retained
   or accepted record keeps its exact identity-defining definition resolvable for as long as that
   record is retained. An owning contract may promise more — for example retained conformance
   fixtures for released Engine interpretations.
5. **Support obligations arise from accepted use and are scoped by the owning contract.** An
   obligation exists when the owning contract publishes reliance, or when an authority that has
   declared its custody accepts a record at its commit boundary. Disposable activity — tests,
   scratch stores, drained events, local runs — creates no obligation by existing; admitting such
   material into retained authority is a new decision. Acceptance by an authority that never
   declared custody is a defect to account for, not a waiver: the accepted fact is never disposed
   of to escape the obligation it created.
6. **Withdrawing support never frees an identity for changed meaning.** Retiring execution,
   decoding or compatibility for a definition removes a capability, not the meaning of the records
   attributed to it. Narrowing an in-scope promise requires an explicit, authorized transition
   under the owning contract, and that transition must neither rewrite history nor silently
   discharge obligations already created.
7. **There is no universal lifecycle.** Arcogine adopts no repository-wide maturity state such as
   `proving`/`promoted` for whole contracts, no single retention horizon and no single version
   scheme: retained attribution, executed behavior and outward compatibility are independent
   promises that do not share one state, so a contract can carry enduring attribution obligations
   while only part of its behavior is executed or supported. Nor does Arcogine freeze only the
   exercised sections of a definition — specification sections are editorial units, the rules
   interact, and rejection behavior matters without a happy-path fixture exercising it.

Each owning contract therefore states its identity's equality rule, its fixed aspects, and the
support it actually promises; silence offers no support but waives no obligation an actual accepted
use created. Evolving a fingerprint policy, Engine interpretation, evidence reference scheme or
continuation rule means introducing a distinguishable identity and reconciling the consumers in
scope, never editing an attributed definition in place — which is what lets historical
reconstruction stay truthful without permanent executors or eternal readers. The owning contracts
apply these rules to [fingerprints](factory-model-v1.md),
[controlled revisions](controlled-revisions.md), [Engine interpretation](engine-semantics-v1.md),
[evidence](governance-evidence.md) and [operational continuity](operational-continuity.md);
declaration and review mechanics live in
[Semantic contract support](../development/semantic-contract-support.md). Same-identity amendment
of an attributed Engine definition, custody mechanics for retained proving artifacts, and closure
of an operational continuation remain open questions for those owning contracts.

## Current implementation constraints (MVP)

These describe today's implementation choices. They are not claims about Arcogine's permanent identity — see the Product Charter's [product boundaries](/docs/product/charter.md#9-what-arcogine-is-not) for why Java, current interfaces, and the current deployment model are implementation choices rather than product identity, subject to change as the product grows toward the full lifecycle described there.

1. Core simulation is written in Java with a **Java 21 language/API/bytecode compatibility baseline**. The preferred devcontainer currently uses JDK 25, and CI runs on JDK 21 to prove the supported floor; the compiler JDK and compatibility baseline are deliberately separate concerns.
2. The headless simulation core is the current implementation's primary layer; the UI and API are additive consumers of it. This describes today's layering, not a permanent claim that Arcogine's mature product surface is UI-secondary.
3. MVP ties factory flow to the economy loop.
4. Support native and containerized local execution.
5. Security-sensitive defaults remain local-first by default; non-local exposure requires explicit hardening controls (see [SECURITY.md](/.github/SECURITY.md)).

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

The current `SimThread` ordering in the `EventLog` row is legacy API behavior, not the target supported event contract. The [runtime contract](runtime-contract.md) requires supported `RuntimeEvent` state-change facts to be derived/published only after authoritative processing succeeds.

`DemandModel` reads `OfferPrice` and lead time on demand, via `DoubleSupplier`s bound to `PricingState`/`FactoryHandler` at construction — it has no state of its own to keep in sync, so it isn't listed as an owner above.

Order/job creation is not exclusively event-driven: `FactoryRuntime.submitWorkload(productId, quantity, unitPrice)` is the supported, consumer-neutral entry point a caller uses to submit production workload directly, with no economy/pricing/demand/agent dependency and no need to own a `Scheduler` or choose a simulation time. `FactoryRuntime` is only built via `FactoryRuntime.forModel(FactoryModelVersion)`, which assembles and owns its own exclusive `FactoryHandler`/`Scheduler` pair — it is never wrapped around an already-live `FactoryHandler` another scheduler might also be driving, and it does not expose that `FactoryHandler` directly (callers observe state through its own read-only projections instead). It resolves to the same package-private `FactoryHandler.submitOrder(...)` acceptance operation that the `OrderCreation` event handled above calls — the one `DemandModel` schedules. Both routes create the same immutable `Order` and deterministically materialize quantity-`N` as `N` unit-quantity sibling `Job`s under the same `OrderId` aggregate, with identical routing/dispatch semantics; `submitOrder` itself is not public, so scheduler/time plumbing never leaks past `FactoryHandler`.

`FactoryRuntime` also implements the consumer-neutral session-control semantics of [Engine Semantics v1 §1.2](engine-semantics-v1.md#12-session-and-control-semantics), additive to the shape above: `modelVersion()` retains and exposes the exact `FactoryModelVersion` the session was instantiated from, for the session's full lifetime; `advanceUntil(SimTime targetTime, long maxEvents)` sits alongside the unchanged single-event `advance()`, processing pending events one at a time until either the next event's time would exceed `targetTime` or `maxEvents` events have been processed, implemented directly in terms of `advance()` so the two can never diverge in ordering or dispatch behavior; `reset()` returns a fresh `FactoryRuntime.forModel(modelVersion())` rather than mutating the existing session in place, since `FactoryHandler`'s stores have no partial-reset subsystem to mutate safely. `submitWorkload` and `setMachineAvailability` — the two externally initiated runtime changes `FactoryRuntime` exposes — always return a definite `CommandResult<T>` (a stable code/diagnostic, `modelVersion()` provenance, and every `Event` scheduled as a direct effect of the command, captured by a command-scoped `RecordingScheduler` window rather than a permanently growing history) instead of ever throwing or returning `void`. `CommandResult` is a three-way sealed type: `Accepted`, `Rejected` (wraps the original, already-structured, sealed `SimError`; verified pre-mutation — `FactoryHandler.submitOrder` preflights its scheduling check before mutating any store, `setMachineAvailability` verifies its own two rejectable conditions from `machinesView()` before calling into `FactoryHandler` at all — so a `Rejected` result never follows partial mutation), and `Faulted` (a genuine engine fault surfacing from deep in `setMachineAvailability`'s online-machine dispatch cascade, after mutation may already have started; making that whole cascade provably preflight-safe was judged disproportionate, so `Faulted` reports it as a definite result instead of letting it throw past the command boundary, while making clear — unlike `Rejected` — that it does not promise zero mutation). Acceptance and execution outcome are independent facts, not one axis, so `Faulted` carries the same accepted value `Accepted` would have alongside the fault — the requested change genuinely was applied before the later failure, and a caller must not lose which entity was affected just because execution subsequently failed. `pendingWorkView()` exposes `FactoryHandler`'s cross-machine `pendingMultiEligible` backlog (see [Engine Semantics v1 §2](engine-semantics-v1.md#2-resource-selection-and-dispatch-semantics)) as read-only `PendingWorkView` entries — necessary because that waiting work is not associated with any single machine and so is invisible to `MachineView.queueDepth()`.

supported runtime observation/event contract adds `FactoryRuntime.observe()` as the separate supported current-state boundary required by the [runtime contract](runtime-contract.md). It returns immutable, deterministically ordered resource, aggregate-order, unit-work decomposition child-job, and multi-eligible-pending-work projections plus the factory's authoritative backlog, completed-order/value, lead-time, and throughput calculations. Metadata carries an opaque per-runtime `RunId`, `FactoryModelVersion.fingerprint()` durable provenance, current simulated time, explicit active/quiescent advancement state, and a `latestEventSequence`. This projection never exposes `FactoryHandler`, mutable stores, or internal scheduler events; the legacy API/SSE still projects its existing internal-event behavior until the later outward-consumer-convergence migration.

the supported runtime observation/event contract also implements the supported `RuntimeEventEnvelope` contract on top of that boundary: `RuntimeEventType`/`RuntimeEventPayload`/`AffectedEntityRef` (`product/domains/factory/.../process/`) are a taxonomy distinct from the internal scheduler's `EventType`/`EventPayload`, and `FactoryRuntime` only ever constructs an envelope, via its single package-private `emit(...)` point, after the authoritative transition it describes has already succeeded. `submitWorkload` emits `ORDER_ACCEPTED` (carrying every created child `JobId`) followed by one `JOB_DISPATCHED`/`JOB_WAITING` per created job describing its resulting placement; `setMachineAvailability` emits `MACHINE_AVAILABILITY_CHANGED` only for a genuine online/offline transition (a no-op request emits nothing) plus any `JOB_DISPATCHED` a resulting dispatch cascade produced, including on the `Faulted` path where only the mutation that actually occurred is reported; `advance()` emits, for each processed `TaskEnd`, `JOB_STEP_COMPLETED`, then `ORDER_COMPLETED` when that step completed the order, then `JOB_DISPATCHED`/`JOB_WAITING` for every placement change the same `TaskEnd` authoritatively caused — freed capacity re-places both the completing job onto its next routing step and whatever queued or multi-eligible backlog work that machine can now accept, derived by diffing authoritative placement rather than by re-exposing internal scheduler events. Internal scheduler markers `FactoryHandler` ignores (`TaskStart`; the `OrderCompleted` a terminal `TaskEnd` schedules for other internal handlers) emit nothing and, by construction, change nothing `observe()` reports: observed time advances only with emission, and `RuntimeRunState` reflects pending *authoritative* work rather than a non-empty queue, so every observation fact stays coherent with one `latestEventSequence` boundary. `RuntimeObservationMetadata.latestEventSequence()` is a live cursor advanced in lockstep with emission — no longer hardcoded to zero — independent of when a caller retrieves the events themselves: `FactoryRuntime.drainSupportedEvents()` returns and clears everything accumulated since it was last called, rather than retaining an unbounded, cursor-replayable history. That retained/replayable-by-cursor responsibility is deliberately not part of this boundary (see the [runtime contract](runtime-contract.md); recovery/resynchronization is later distribution hardening); a caller needing durable replay retains the drained events itself.

supported runtime observation/event contract closes the headless contract without adding new abstractions to it. `HeadlessClosureAcceptanceTest` proves that a consumer joining an already-progressed runtime reconstructs the complete supported view from one `observe()` result alone — no retained or replayed runtime events, no `FactoryHandler`/mutable store, no scheduler `Event`/`EventLog` replay, no API/Spring/frontend DTO — and that `latestEventSequence` survives draining so such a consumer knows where to continue; that supported events and observations close over the same authoritative transitions (the delta after an observation at sequence `S` is exactly `S+1..S'`, is consistent with the later observation, and explains what changed, without events carrying redundant full-state payloads); and that the active production bottleneck is identifiable from `ResourceObservation` facts alone, independently by carried load (active plus queued work) and by busy-tick utilization, deterministically and reproducibly. Making those real required three production corrections, each to a fact the contract already claimed: `FactoryHandler.handleTaskEnd` now credits the finished step's duration to the resource that performed it, so `ResourceObservation.busyTicks()` reports genuine cumulative utilization instead of the constant zero it previously always was; `FactoryRuntime.advance()` now reports the whole `TaskEnd` placement cascade as `JOB_DISPATCHED`/`JOB_WAITING` per the runtime contract, pinned by `taskEndDispatchCascadeIsReportedByTheSupportedEventStream`; and `observe()` now derives time, throughput, and `RuntimeRunState` from the supported boundary rather than the raw scheduler cursor per the runtime contract, pinned by `processingANoOpInternalMarkerLeavesTheSupportedObservationUnchanged`. The complementary structural fact — API/UI DTOs never re-entering domain decision paths — is enforced by `ArchitectureTest.api_dtos_must_not_reenter_domain_decision_paths` in `interfaces/api`, which fails the build if anything in `com.arcogine.factory..` depends on `com.arcogine.api..`, `org.springframework..`, or `jakarta.servlet..`. supported runtime observation/event contract core/headless closure is therefore complete; outward-consumer convergence (SSE/API DTO migration, CLI) and runtime-event recovery//resynchronization hardening (retained runtime-event history, replay-by-cursor, reconnect/resume, checkpoint/restore) remain outstanding, and the legacy API/SSE still projects its existing internal-event behavior until that outward-consumer-convergence migration.

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

This loop describes how a choice becomes a state change. Who is attributable for it, what mechanism produced it, and what it acted on are separate questions, recorded in [Attribution and decision boundaries](#attribution-and-decision-boundaries) below.

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

The authoritative facts are each immutable `Order` plus its order-level execution aggregate; individual child completion is only progress toward that aggregate. `IntraOrderExecutionAcceptanceTest.quantityTwentyCreatesDeterministicChildrenAndOneAggregateCompletion` proves the unit-work decomposition shape: twenty child jobs complete under one order, while `completedSales == 1` and `CompletedSalesValue` is incremented once by the parent order value.

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
- an unbalanced journal entry able to enter financial state;
- attribution or provenance metadata being read by domain, dispatch, pricing, or simulation logic instead of the requester appearing as a modelled payload field;
- a shared actor, subject, decision-source, or capability type appearing before multiple consumers have demonstrated the same equality/namespace/lifecycle contract (see [Attribution and decision boundaries](#attribution-and-decision-boundaries)).

These guardrails are part of the current architecture and are reinforced by executable architecture tests where the invariant can be checked mechanically. Remaining runtime-readiness work is tracked separately in [Factory Simulation Engine Readiness](../planning/factory-simulation-engine-readiness.md); that document is planning guidance, not architectural authority.

## Attribution and decision boundaries

The [Events–State–Observations](#core-architecture-philosophy-events-state-observations) model says how a choice becomes a state change. It does not say **who is answerable** for that change, **what produced** the choice, or **what was acted on**. Those are separate questions, and they are easy to conflate because the implementation currently answers all of them with free-form strings.

This section records the durable semantic result of the [Agency and decision boundary investigation](../research/investigations/agency-decision-boundary.md). Everything here is a **rule, not a type**. No Java type, module, persisted field, identity contract, or delivery track is introduced by it, and nothing here describes implemented capability: `Event`, `RuntimeEventEnvelope`, and `AgentObservation` carry no attribution today. Read these as constraints on future work.

The headline result: **no platform-level `Agent` abstraction is currently justified.** Designs that collapse or hard-bind the actor, decision-source, and subject roles below fail the investigation's proving cases, and no additional cross-case invariant was found that warrants a shared platform `Agent` concept now. This is a claim about current evidence and current consumers, not an impossibility claim — a future design that preserves the roles compositionally is not foreclosed. `agent` remains a useful application/domain label (`SalesAgent`, the `:agents` module); it must not become a platform ontology, superclass, shared module, or durable identity category merely because that label already exists.

### Actor, decision source, and subject are distinct roles

```text
Actor            who or what bears responsibility for a participation
Decision source  the mechanism that produced the choice
Subject          what is acted on or represented
```

- **Role is a property of a participation, not an intrinsic permanent kind of an identity.** The roles must stay distinguishable; the identities occupying them need not be different. A human acting directly is both actor and decision source, and that is not a modelling failure.
- **One decision source may serve many actors**, and **one actor may change decision sources without changing its attributable identity**. Attribution that dissolves when a controller is replaced is not attribution.
- **A responsibility-bearing actor concept survives** across humans, organizations, software, planners, simulated participants, and external systems — as a concept, and not always present. An external data source is not automatically an actor.
- **The actor concept does not justify a shared actor type.** Arcogine has no `ActorId`, actor reference, actor-kind enum, actor namespace, or actor equality/lifecycle rule, and should not acquire one until multiple concrete consumers demonstrate the *same* equality, namespace, lifecycle, and interoperability contract strongly enough that domain-local representations would duplicate one semantic invariant. A committed implementation consumer is strong evidence for that; a consumer *count* is not a threshold. A closed actor taxonomy is specifically not justified — W3C PROV, the usual source cited for one, states its agent types do not cover all kinds of agent.
- **An acting party and a represented principal may differ**, and where they do, both must remain recoverable. Whether the represented principal retains responsibility is a **domain policy question**: delegation, impersonation, and transfer of responsibility are different relations, and Arcogine takes no general position that a delegator stays accountable.

### Four kinds of provenance must stay distinct

```text
attributable actor      who is answerable
recording provenance    what caused Arcogine to record something
decision provenance     which mechanism(s) produced the choice
external data source    where an observation came from
```

These must not collapse into one `source` or `actor` field merely because several current records happen to be strings. On current `main`, attribution-shaped names already mean different things, two of them are not pinned down by durable authority at all, and not all of them are even strings. **No field below may be renamed, reinterpreted, or mechanically migrated** into a future attribution capability:

- `RevisionRecorder` is, **as a whole**, persisted recording provenance identifying what caused Arcogine to record a controlled revision. Its internal `source` / `subject` decomposition is **underspecified by durable authority** — the [controlled revision contract](controlled-revisions.md) permits representing the recorder with a small source/subject value without fixing which slot means what, and the implementation says only that the pair identifies "the source and subject that caused a revision to be recorded". Do not read `source` as a canonical mechanism/channel or `subject` as a canonical actor/principal. It is persisted and participates in idempotency equality in immutable governance history, so any future attribution must be **additive**.
- `ChangeProvenance.source` is *producer* provenance for a change set: its contract states plainly that **none of its fields are identity**. What the slot carries beyond that is **not established** — current call sites use both role-like and mechanism-like values — so it is neither a party identity nor, on current authority, a role label. It must not be migrated into a shared actor identity.
- `RequirementSource` is a requirement's *governing publication* provenance, and is a sealed domain type rather than a free-form string. Its sense of "authority" is **publishing body**, not authorization authority — a direct collision with the authorization sense of the word used elsewhere in this section.
- `EventPayload.AgentDecision` is a narrative string. It is not attribution at all.

**Subject is distinct from actor, and there is no universal subject reference.** `AffectedEntityRef` (Factory-owned, sealed, transition-scoped) and `ChangedEntityRef` (Governance-owned) coexist deliberately, because their equality, namespace, ownership, and lifecycle contracts differ — shared appearance is not shared semantic identity. Neither is Arcogine's universal subject reference, and the word `subject` in `RevisionRecorder` does not establish Arcogine's meaning of "the subject of an operation": when the operation sense is meant, say so explicitly.

### Decision-source internals are not world semantics

Goals, beliefs, memory, plans, prompts, behaviour-tree state, planner search state, model weights, and hidden reasoning traces stay **private to the mechanism** that uses them. They do not become shared Arcogine semantics because a decision-maker happens to have them.

Recording a hidden reasoning trace as authoritative causal provenance would manufacture **false provenance** — a stated rationale need not be the operative cause. A voluntarily recorded public rationale, declared policy identifier, commitment, or audit explanation is a legitimate durable record of what a party *asserted*, and must never be presented as evidence of the hidden cause.

Where it is materially relevant, Arcogine must be able to explain **which mechanism(s) were relied on to produce a decision**. That requirement is deliberately weaker than a schema:

- an exact immutable source version is required **only** where a consumer's own reproducibility contract requires it, and must **never be fabricated** for an opaque external participant that cannot supply one;
- where a version does exist, it must be a resolved immutable identifier, never a mutable alias;
- there is **no single `DecisionSourceRef`** and no commitment to a singular source — multi-source, ensemble, recommender-plus-human, and policy-check provenance must remain expressible later;
- a **stateful or online-learning source**, whose material behaviour changes without any version changing, is an explicitly **unresolved** case. Identity plus version may not identify it. Reopen this when a concrete consumer needs such a source to be explainable or replayable; do not force it into an identity+version shape that does not describe it.

### Requests, replay, and the attribution/outcome boundary

**An attributed operation request is the preferred first and common durable boundary** when a decision results in a requested semantic change, and Arcogine does not need a universal shared `Decision` type. This is the narrow form and the only form: a decision and the operation it requests are *not* universally one durable fact. A domain remains free to own a distinct durable recommendation, approval, denial, standing authorization, or exception/risk-acceptance record when a real consumer requires it — [Governance and Conformance](governance-conformance.md) already names several — and those must not be recast as operation requests for vocabulary symmetry.

Where a decision is durably recorded, it should identify the **observation boundary that bounded it** — what the decision source could actually know at the time — so the decision stays explainable from that input plus the source's own private state. This is what keeps a simulated participant from acting on information it could not have had. The reference itself stays domain-owned; there is no shared cross-domain observation reference, and an Engine observation cursor is not one.

Four replay operations stay distinct:

```text
re-execute the decision source
!= replay the recorded decision
!= replay the requested operation
!= replay the resulting transition
```

The general move for nondeterministic behaviour is to **convert the relevant nondeterministic boundary into durable recorded input** where a consumer's contract requires replayability — the [Determinism Contract](#determinism-contract)'s ordered external commands already work this way. Hidden source internals never become replay state.

Attribution and outcome have a two-part rule:

1. **Incidental attribution metadata must never affect outcome.** Attribution attached to a request for provenance must not reach any domain, dispatch, pricing, or simulation computation. If a requester genuinely should influence dispatch, it must appear as a **modelled field in the operation payload**, never be smuggled in through attribution metadata.
2. **Explicitly modelled authority is a legitimate exception.** Actor identity may affect behaviour only through an authority determination that is explicitly modelled, whose outcome is recorded, and which is itself a reproducible input on replay — never through an implicit read of attribution by unrelated domain logic.

This preserves the boundary between provenance and explicit policy input; it does not settle an authorization model. The cross-cutting authority question — *may this actor perform this operation on this subject under the applicable policy?* — is a useful **question shape** for locating where policy belongs. It is not a persisted or public input schema, Arcogine asserts no rule that authorization consults only the current actor, and ownership of reusable actor/capability semantics remains open (see [Operational Execution and Digital Twin](operational-execution-digital-twin.md) §5, which must not become that owner by default).

### What is deliberately not introduced, and what would reopen it

| Not introduced | Why | Reopen when |
|---|---|---|
| Platform `Agent` type, actor/subject/decision-source value types, actor kind enum | No current consumer proves a shared equality/namespace/lifecycle contract | Multiple concrete consumers demonstrate the *same* such contract (see above) |
| Shared temporally extended `Capability`, procedure, or skill type | The [unit-work decomposition](engine-semantics-v1.md#3-unit-work-decomposition-semantics) aggregate/child pattern already supplies aggregate intent, child identity, correlation, and a completion rule | A concrete consumer shows that pattern cannot express a real temporally extended capability |
| Agent message bus or conversation ontology | Typed operations, events, observations, results, and explicit public commitments suffice; an accepted order is already such a commitment | A proving case shows ordinary domain interaction cannot represent a required interaction cleanly |
| Agency module, subsystem, or delivery track | The result is cross-cutting semantic distinctions, not a coherent implementation responsibility | A surviving shared contract acquires an owner that no existing module can hold |
| A universal `Decision` record, subject reference, or observation reference | Each is domain-owned today for reasons that differ per domain | A cross-domain consumer needs one contract, not merely one shape |

Each row is a refusal justified by *current* evidence and current consumers, not a permanent prohibition, which is why each states what would reopen it. None of them commits Arcogine to persisted or public identity equality, a shared namespace or lifecycle contract, a permanent closed taxonomy, or cross-module equality semantics, which is why they belong in this section at all. A future change that would introduce any of those, or another hard-to-reverse public or persisted semantic commitment, is a change to Arcogine's architecture: it must be reconciled into the architecture or specification document that would own the new semantics, with its consumers and executable invariants updated in the same change, rather than absorbed into this section as another refusal.

W3C PROV and comparable external models remain **vocabulary donors and outward projection targets**, consistent with the [external representation policy](external-representations.md) and [Standards Alignment](standards-alignment.md). They are not Arcogine's domain model, and adopting their vocabulary never imports their ontology.

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

Out of scope: GAAP/IFRS compliance, configurable revenue-recognition frameworks, accounts receivable/payable unless a scenario needs them, tax, depreciation, multi-currency, debt/equity financing, inventory accounting, budgeting, forecasting, or fiscal periods. A minimal double-entry ledger with an immediate-settlement policy is not that — it's the intentional current architecture, sized to establish ownership rather than sophistication. Further finance capability should be introduced through explicit planning and a reconciled architecture change when requirements justify it, rather than inferred from removed migration notes.

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
│                         authoritative durable history, historical resolution, the
│                         generic semantic ChangeSet/impact contract (com.arcogine.
│                         governance.change), and the generic requirement/assertion
│                         contract (com.arcogine.governance.{requirement,assertion,
│                         catalogue}); current filesystem adapter, no simulation
│                         event handling
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
│                         challenge evaluation, attempt provenance/design-to-design
│                         comparison, and (challenge content-loading layer) a rendering-technology-independent JSON
│                         content-loading layer — schema-versioned decode, catalogue
│                         loading, and evaluation-policy resolution — that reuses the
│                         existing definition/catalogue validators rather than
│                         reimplementing their rules (see
│                         docs/planning/factory-design-game-challenge-readiness.md).
│                         Headless — no dependency on any module below.
└── interfaces/
    ├── api/              Spring Boot HTTP + SSE server: controllers, SimThread,
    │                     IntegratedHandler, SnapshotBuilder, DTOs
    ├── cli/               Picocli CLI entry point: serve + headless run modes
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

types ← governance ← factory

challenge   (no dependency on any module above; a sibling, game-owned boundary)
```

Each module exposes a clean public API and hides implementation details. Event-handling modules (`factory`, `economy`, `agents`, `finance`) implement the `EventHandler` interface and are wired together by `IntegratedHandler` in the API layer. Governance's production dependency remains on `:types` only — it has no dependency on `factory` or any other domain. `factory`'s production source depends on `governance` (in addition to its existing `types`/`simulation` dependency) for two narrow adapter ports: `SemanticArtifactVerifier` (the authoritative controlled-revision persistence and historical resolution historical-artifact codec boundary; `factory-model:v1` artifact encode/decode/fingerprint logic stays domain-owned in `FactoryModelArtifactV1`) and `SemanticChangeExtractor` (the semantic ChangeSet/impact capability semantic-comparison boundary; domain-specific diff logic stays domain-owned in `com.arcogine.factory.change.FactoryModelSemanticComparator`, classifying changes using Governance's generic `SemanticChangeKind`/`ChangedEntityRef` vocabulary). Governance never introspects `FactoryModel` internals directly; it only depends on the narrow SPIs the domain implements.

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

Deterministic simulation is architectural, not an implementation convenience: acceptance tests,
comparison of design candidates, historical explanation of a run, and challenge evaluation are all
meaningless if two executions of the same explicit inputs can legitimately disagree. At the same
time Arcogine must be able to change how it interprets a design — dispatch ranking, work
decomposition, scheduling, transfer timing — without pretending the designer authored a different
production system, and without claiming that historical results were produced under rules they were
not. Five rules hold that line:

1. **A simulation outcome is a function of explicit inputs and one identified Engine
   interpretation.** The reproducibility inputs are the authored model identity, the Engine
   interpretation identity, the explicit workload, the seed and other random inputs, the ordered
   external commands, and any other explicitly identified result-affecting input. Nothing else may
   influence acceptance, rejection, assignment, ordering, simulated time, terminal state or derived
   results; run identity is correlation metadata and never affects an outcome.
2. **No result-affecting rule may remain ambient.** Any limit, ordering rule, tie-break, rounding or
   accumulation rule that two implementations could choose differently is part of the identified
   interpretation or is an explicitly identified input. One interpretation identity covers a run's
   complete result-affecting interpretation and is fixed when the run is established.
   [Engine Semantics v1](engine-semantics-v1.md) owns the exact rules, their membership test and
   the conformance fixtures that pin them.
3. **Authored facts and interpretation have different owners.** Facts describing the production
   system the designer authored belong to the canonical model and its fingerprint; rules describing
   how Arcogine interprets any such design belong to the Engine interpretation identity. Changing
   interpretation alone never changes the authored model's identity, and authored facts are never
   synthesized to make an interpretation applicable.
4. **An intentional change to result-affecting behavior is a new interpretation identity**, a
   bug fix that observably changes outcomes included. Repairing an implementation so that it
   conforms to the identified interpretation is not such a change. Implementations declare which
   interpretations they execute and refuse unsupported ones rather than silently substituting
   current behavior.
5. **The durability guarantee is attribution plus a verifiable definition, not permanent
   re-execution.** A retired interpretation keeps its identifier, normative specification and
   conformance fixtures, so historical results stay attributable and interpretable after execution
   support ends. Cross-interpretation comparison is explicit and owned by the consumer making the
   claim; identity never authorizes guessing that results are comparable.

Nondeterministic boundaries a consumer needs to replay — clocks, external inputs, human or agent
decisions — are converted into recorded explicit inputs rather than admitted into the
interpretation. Runtime provenance carries the authored model identity and the Engine
interpretation identity, so a consumer can state exactly what produced a result
([runtime contract](runtime-contract.md)).

The current implementation realizes this contract with:

- `java.util.Random` seeded with `rng_seed` from scenario config
- Priority queue orders events by time, with FIFO tie-breaking
- Java strict floating-point semantics; compilation targets the Java 21 compatibility baseline
- No concurrent mutation of simulation state

Given identical scenario TOML and the same seed, the simulation produces identical event logs, KPIs, and final state. Tests comparing semantic outcomes normalize or inject run identity and compare the deterministic stream; a test that depends on run identity is wrong.

This determinism contract is scoped to simulation, replay, and verification contexts, where it is a critical property. It is not a claim that real-world execution itself must be, or will be made, deterministic — production operates in a non-deterministic world of real machines, people, and failures. See the Product Charter's [continuity with current architecture](/docs/product/charter.md#8-continuity-with-current-architecture) section for this distinction.

## Factory Model Identity (current state)

The runtime establishes one fixed `EngineSemanticsVersion` (`engine-semantics:v1`) alongside the
authored `ModelFingerprint` and opaque per-runtime `RunId`. These identities answer different
provenance questions: the semantics identity describes the result-affecting Engine interpretation
([Engine Semantics v1](engine-semantics-v1.md)), while `RunId` is correlation only. Runtime
observation/event field propagation of the semantics identity remains follow-up work, and the
reported constant is not evidence of complete conformance to that specification.

Scenario factory semantics are instantiated through an implemented canonical-model seam: `FactoryModel` (validated) → `FactoryModelVersion` (immutable, published) → `FactoryRuntimeAssembler` (deterministic runtime instantiation). See [Factory Design](factory-design.md#4-canonical-model-boundary) for the boundary this implements.

`FactoryModelVersion.fingerprint()` implements the durable `factory-model:v1` semantic fingerprint contract specified by [Factory Model v1](factory-model-v1.md). The contract uses the typed `ModelFingerprint` value and a language-independent canonical binary encoding with explicit policy versioning and compatibility vectors. Equal canonical semantic content therefore has a durable identity that is independent of process memory and implementation language under the v1 policy.

`FactoryModelVersion.contentHash()` remains a separate legacy implementation surface. It is deterministic for the current Java model but is not the durable fingerprint contract, and bare content hashes must not be reinterpreted as `factory-model:v1` fingerprints. Existing `IntegratedHandler`/`SimResult.modelContentHash` provenance still carries that legacy hash; replacing it with truthful fingerprint/Engine provenance is a bounded later cleanup that must inventory its consumers, not a compatibility obligation. The supported runtime observation/event contract now supplies opaque per-runtime `RunId` on `RuntimeObservation`; that run identity is implemented and is distinct from the remaining broader provenance migration.

`:types` provides the opaque UUIDv4 `ControlledRevisionId` value model, and `:governance` provides the immutable `ControlledRevision`, lineage, and recording-provenance values fixed by the [controlled revision contract](controlled-revisions.md). Governance identity/history capability is complete: `ControlledRevisionAuthority` defines the authoritative acceptance/lookup/resolution boundary, and `accept(...)` returns the immutable accepted record after the authority establishes its `recordedAt` at the commit boundary rather than trusting the candidate's timestamp. The current `FileControlledRevisionAuthority` adapter persists append-only revision records and immutable semantic artifacts across process/reopen boundaries, rejects duplicate/rebound IDs, requires an already-authoritative parent under the current `0..1` lineage policy, verifies the supplied canonical artifact reproduces the revision's `ModelFingerprint`, and atomically installs the revision record under process/filesystem locking. Historical resolution returns the accepted immutable revision together with its exact semantic artifact; missing/corrupt metadata or artifacts and fingerprint mismatches fail explicitly rather than falling back to current model state.

The factory proving ground reuses the exact `factory-model:v1` canonical bytes as its historical semantic artifact. `FactoryModelArtifactV1` strictly decodes and canonical-reencodes those bytes to reconstruct the exact historical `FactoryModelVersion`, while the Governance store remains artifact-policy-agnostic through `SemanticArtifactVerifier`. Distinct revisions may therefore share one `ModelFingerprint` and one immutable artifact — including the `F1 -> F2 -> F1` rollback case — without becoming the same historical occurrence. The current filesystem record layout and locking mechanics are replaceable adapter details, not a selected permanent production persistence architecture. Governance semantic ChangeSet/impact capability's initial slice adds the generic `ChangeSet`/`SemanticChange`/`ImpactScope` contract in `:governance` and the factory-domain `FactoryModelSemanticComparator` (Factory semantic-comparison capability) that implements `SemanticChangeExtractor` against `factory-model:v1` artifacts, keyed on stable domain identity while still attributing a semantically significant top-level list reorder (semantic under [Factory Model v1](factory-model-v1.md)) as a real change. Governance requirements/assertions capability adds the generic `Requirement`/`Assertion`/`RequirementCatalogue` contract in `:governance`, whose `RequirementScope` matches directly against the semantic ChangeSet/impact capability `ImpactScope` seam. Governance conformance evaluation/findings capability's initial slice adds the generic `ConformanceResult`/`ConformanceEvaluation`/`Finding` contract and the deterministic `ConformanceEvaluator` in `com.arcogine.governance.conformance`, which evaluates a requirements/assertions capability `Requirement`/`Assertion` pair against a model fingerprint (and an optional, never-synthesized `ControlledRevisionId`) without introducing evidence, authorization, or deployment concepts. Approval/authorization, evidence, deployment, external change-management relationships, labels/tags/branches, and multi-parent merge semantics remain separate evidence/evidence-use capability+ concerns rather than revision identity.

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
