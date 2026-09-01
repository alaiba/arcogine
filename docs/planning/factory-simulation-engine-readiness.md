# Factory Simulation Engine Readiness

> **Status:** Proposed  
> **Scope:** Prepare Arcogine's factory runtime for external consumers after the canonical factory-model boundary is established  
> **Authority:** Planning only; this document defines runtime-readiness gates, not current capability or accepted architecture  
> **Prerequisite:** the model-seam entry gate (§1.1) — narrower than full D1-D4 in [Factory Design Capability](factory-design-capability.md)  
> **Related:** [Factory Design Architecture](../architecture/factory-design.md), [ADR-0003](../architecture/decisions/0003-canonical-factory-model-boundary.md), [ADR-0004](../architecture/decisions/0004-model-identity-revision-lineage-and-external-change-control.md), [ADR-0006](../architecture/decisions/0006-durable-semantic-fingerprint-contract.md), [ADR-0009](../architecture/decisions/0009-gate-2-closure-and-work-decomposition-boundary.md), [ADR-0010](../architecture/decisions/0010-intra-order-execution-decomposition-and-work-item-identity.md), [ADR-0011](../architecture/decisions/0011-runtime-observation-and-event-contract.md), [Gate 4 Runtime Observation and Event Delivery](gate-4-runtime-observation-event-delivery.md), [Operational Execution and Digital Twin Readiness](operational-execution-digital-twin-readiness.md), [Factory-Design Game Consumer Initiative](factory-design-game-consumer.md), [ISA-95 Semantic Mapping](../architecture/isa-95-semantic-mapping.md), [Architecture Overview](../architecture/overview.md)

## 1. Purpose

Arcogine should become a consumer-ready deterministic production runtime before a factory-design game is used as an integration client.

This plan begins **after** Arcogine can validate, publish, and instantiate an immutable canonical factory model. It does not own the factory-design schema, publication lifecycle, or consumer draft model.

The intended dependency is:

```text
Model-seam entry gate (below)
        |
        v
Published FactoryModelVersion
        |
        v
Factory runtime readiness
        |
        v
Headless capacity/layout acceptance scenarios
        |
        v
Stable external-consumer contract
        |
        v
Factory-design game implementation
```

The game is a downstream proof that Arcogine is usable as an engine. It must not be the place where missing workload, execution, scheduling, session, observation, or transfer semantics are invented under UI pressure.

A CLI command, JUnit harness, or thin reference consumer may prove a readiness gate. That is engine verification, not game implementation.

### 1.1 Model-seam entry gate

Runtime readiness does not wait on the full [Factory Design Capability](factory-design-capability.md) plan — several of D1-D4's own acceptance criteria are deliberately deferred capability (see that plan's §3.1 implementation-status table), not blockers for engine work. The entry requirement for Gate 1 is narrower:

```text
Required before engine Gate 1:
- canonical FactoryModel seam exists
- deterministic structural validation exists
- immutable publication exists
- a model provenance identity policy exists
- runtime instantiates only from the published model
- representative baseline behavior is preserved
```

This gate is satisfied by what had already landed when Gate 1 began. Historically, Gate 1 did **not** depend on the then-unimplemented D3 durable cross-process fingerprint contract; that historical sequencing remains valid and Gate 1 is not retroactively reopened. Since then, D3/G1.1 has landed: [ADR-0006](../architecture/decisions/0006-durable-semantic-fingerprint-contract.md) and `FactoryModelVersion.fingerprint()` establish the durable `factory-model:v1` semantic identity contract. `FactoryModelVersion.contentHash()` remains legacy compatibility only and must not be treated as the durable provenance identity. Gate 4 therefore uses the durable `ModelFingerprint` as mandatory source-model provenance under [ADR-0011](../architecture/decisions/0011-runtime-observation-and-event-contract.md). Run identity and ordered runtime-event provenance are Gate 4 concerns; optional controlled-revision provenance is carried only when authoritatively supplied and does not make Governance G1.3 persistence a prerequisite for Engine readiness.

## 2. Boundary with factory design and operational execution

The [Factory Design Capability](factory-design-capability.md) owns model-side concerns:

```text
Product/operation definitions
Resource definitions and installed instances
Capability or explicit eligibility requirements
Semantic layout
Executability validation
Publication/model identity/provenance
Deterministic instantiation boundary
```

This plan owns deterministic simulation-runtime concerns:

```text
Production orders
Work items
Quantity execution semantics
Resource dispatch
Queues and assignments
Active operations
Transfers in progress
Simulation runtime events/observations
Performance
Simulation session control
```

Where a concern crosses the model/runtime boundary, the published model owns stable input semantics and runtime owns changing simulated consequences. For example:

```text
Model:   resource position and footprint
Runtime: simulated transfer start/progress/completion caused by that layout
```

Runtime may derive indexes or compiled structures from the published model, but it does not author another factory model and never mutates the published version.

The sibling [Operational Execution and Digital Twin Readiness](operational-execution-digital-twin-readiness.md) track consumes stabilized production semantics later and owns the additional boundary created by real-world consequence. This plan therefore does **not** own:

- production execution-context identity or environment separation;
- verified operational actor/source/target trust and authorization;
- external-device command acknowledgement/result lifecycles;
- production deployment targeting/application or applied-artifact provenance;
- telemetry/external-observation ingestion and source provenance;
- modeled-versus-observed twin reconciliation;
- fail-safe physical actuation, credential lifecycle, or live-adapter recovery semantics.

`FactoryRuntime` is a consumer-neutral **simulation runtime**, not a production-control runtime by implication. An Operational Execution prototype may use Engine-owned semantics or synthetic fixtures while this plan is incomplete, but a synthetic operational adapter is not evidence that an Engine gate is complete, and protocol-driven operational types must not become a second production ontology.

## 3. Charter and architecture alignment

This initiative supports the [Product Charter](../product/charter.md) by strengthening one executable model across design, simulation, verification, and future execution contexts.

It follows these constraints:

- Arcogine owns executable production semantics; consumers receive controlled commands, events, and purpose-specific observations.
- Mutable runtime state has one authoritative owner.
- Request, execution, and performance concerns remain distinguishable.
- Simulation behavior remains deterministic for identical published models, seeds, workloads, and ordered commands.
- Runtime contracts are established before transport-specific API shapes become public compatibility obligations.
- The [ISA-95 semantic mapping](../architecture/isa-95-semantic-mapping.md) is a modeling reference, not a requirement for full ISA-95 implementation or conformance.
- Game-specific scoring, rendering, progression, and player-economy concepts do not enter runtime semantics.
- Operational trust, deployment, external-observation, and reconciliation semantics remain outside this simulation-readiness track.

## 4. Readiness policy

Game implementation begins only after the design prerequisite, Gates 1-5, and the currently activated W1 capability below are satisfied by headless evidence.

```text
Prerequisite  Model-seam entry gate satisfied (§1.1) — not full Design D1-D4
        ↓
Gate 1        Explicit workload and execution model
        ↓
Gate 2        Capability-based deterministic dispatch
        ↓
Gate 3        Consumer-neutral simulation session
        ↓
W1            Intra-order execution decomposition
        ↓
Gate 4        Stable observations and event envelopes
        ↓
Gate 5        Spatial runtime consequences
        ↓
Game consumer may begin
```

The current fixed-quantity factory-design reference challenge activates **W1 — intra-order execution decomposition**. ADR-0009 records why W1 is separate from Gate 2 dispatch; [ADR-0010](../architecture/decisions/0010-intra-order-execution-decomposition-and-work-item-identity.md) records the accepted W1 architecture. W1's functional implementation is now in place: one accepted quantity-`N` `Order` decomposes deterministically into `N` independently dispatchable unit-quantity `Job`s, `JobId` is the work-item identity, aggregate progress/completion remains order-level, and exactly one order completion is emitted. W1 remains on the critical path between Gate 3 and Gate 4 until its required large-order benchmark evidence is recorded; that remaining benchmark is the only outstanding W1 completion item. Gate 2 remains complete for dispatch of independently dispatchable work.

Distribution hardening follows the core gates. A first UI experiment may begin after these gates; a distributable client additionally requires the contract, recovery, persistence, and packaging work in Section 11.

## 5. Gate 1 — Explicit production workload and execution model

### 5.1 Goal

Separate immutable production intent from mutable execution and make factory workload independent of the economy demand loop.

The published factory model supplies product and operation definitions. Runtime introduces workload and execution concepts equivalent to:

```text
Production order
    immutable workload intent
    product, quantity, release time

Work item
    mutable execution state
    parent order
    current operation
    assignment
    timing/status

Performance observation
    what actually happened
```

These are conceptual responsibilities, not accepted Java type names.

### 5.2 Current state and remaining problem

The first behavior-preserving Gate 1 slice separated accepted order intent from mutable execution:

```text
Order (immutable)
    OrderId
    product
    quantity
    createdAt
    unitPrice
        |
        v
Job (mutable execution)
    JobId
    referenced immutable Order
    current step
    assignment
    timing/status
```

`FactoryHandler` persists an immutable `Order` before creating execution work; `Job` no longer owns product, quantity, or price fields. Existing `JobView` product/price/value getters remain compatibility projections delegated to the referenced order, while each W1 child projects `quantity() == 1` because it represents one execution unit.

The second Gate 1 slice added an explicit, consumer-neutral workload-submission entry point: `FactoryRuntime.submitWorkload(productId, quantity, unitPrice)`. `FactoryRuntime` wraps a `FactoryHandler` together with a `Scheduler` it owns internally, so a caller supplies only product/quantity/commercial intent -- never a mutable `Scheduler` or an authoritative simulation time, which stayed internal event-engine plumbing rather than becoming part of the workload boundary. Both `FactoryRuntime.submitWorkload` and the economy-driven `OrderCreation` event resolve to the same package-private `FactoryHandler.submitOrder(productId, quantity, unitPrice, currentTime, scheduler)` acceptance operation -- `FactoryHandler.handleEvent` delegates to it for that event rather than duplicating the logic, and it is not exposed outside the `factory.process` package. `FactoryHandler` already had no compile-time dependency on economy/pricing/demand/agents; this slice makes explicit submission a supported, named entry point instead of something only reachable by hand-constructing an internal event payload and owning a `Scheduler`. `FactoryRuntime` is only constructed from a published model, via `FactoryRuntime.forModel(FactoryModelVersion)` (internally using `FactoryRuntimeAssembler`) -- never wrapped around an already-live `FactoryHandler` some other scheduler might also be driving, so it always owns the exclusive factory/scheduler pair it advances and event ordering stays globally authoritative. It also does not expose the mutable `FactoryHandler` directly; callers observe state through `FactoryRuntime`'s own read-only projections (`ordersView`, `jobsView`, `job`, `machinesView`, `backlog`, `avgLeadTime`, `throughput`, `completedSalesValue`, `completedSales`). A headless test (`ExplicitWorkloadSubmissionTest`) builds a runtime this way and submits workload directly, with no `DemandModel`, `PricingState`, or agent in the loop, and proves repeated identical submissions are deterministic. `FactoryRuntime.advance()` pumps exactly one pending event at a time so a headless caller can drive submitted workload to completion without reaching into scheduler internals; this is deliberately not a general session/advancement API -- Gate 3 later completed that broader contract. Economy-driven scenarios continue to work unchanged: `DemandModel.generateOrders` still schedules `OrderCreation` events, which route through the same `submitOrder` logic.

The third Gate 1 slice made order quantity consume proportional production work using a historical pre-W1 representation: one `Job` repeated its routing once per unit of quantity, with `routing.stepCount() * quantity` total steps and job-global progress mapped back to routing-local steps. That representation established proportional machine-occupied work and deterministic quantity semantics without yet decomposing an order into independent execution objects. It is retained here as Gate 1 baseline history only; it is no longer the current runtime representation and must not be used as current W1 guidance.

W1 replaced that historical one-`Order`/one-`Job` representation with the accepted ADR-0010 shape, now implemented:

```text
Order (immutable intent, OrderId)
        |
        v
order-level execution aggregate (same OrderId)
        |
        +---- Job ordinal 0, executionQuantity 1
        +---- Job ordinal 1, executionQuantity 1
        +---- ...
        +---- Job ordinal N-1, executionQuantity 1
```

`JobId` is the independently dispatchable work-item identity; each child traverses the routing exactly once; child creation/ID allocation/initial dispatch are deterministic in ordinal order; aggregate `releasedQuantity` and `completedQuantity` are order-level; backlog/completed-sales/lead-time remain order-level measures; and the final child completion emits exactly one `OrderCompleted` carrying explicit `OrderId` plus the completing child `JobId`.

The current `OrderCompleted` payload therefore carries explicit `orderId` together with the completing child `jobId`, as required by ADR-0010. Gate 4 remains responsible for the later stable event-envelope contract, not for deciding W1's basic execution identities.

The canonical-model seam is already complete enough for this runtime work, so definition-model changes do not need to be mixed into W1.

### 5.3 Required behavior

Arcogine must support explicit production workload independently of pricing, stochastic demand, and sales agents.

Order intent remains immutable after acceptance. Execution progress belongs to work items or an equivalent execution aggregate. Completion/performance is derived from executed work rather than by mutating the original request into a result record.

The economy subsystem may remain one source of production orders, but the factory runtime must not depend on it as the only source.

### 5.4 Acceptance criteria

Gate 1 is satisfied when:

1. Runtime instantiates from a published factory model containing the relevant product/operation definitions.
2. A caller can submit or preload an explicit production order without enabling pricing, stochastic demand, or agents.
3. An order for 10 units consumes more production work than an otherwise identical order for 1 unit.
4. Immutable order intent is distinct from mutable work execution state.
5. Multiple work items can report progress against one production order when the selected quantity model requires it.
6. Order completion is derived from executed work and exposed through a supported observation.
7. The same model version, seed, and workload produce the same ordered result.
8. Existing economy-driven scenario behavior remains covered or is migrated deliberately.

**Gate 1 is complete.** All eight criteria have executable evidence. Historical Gate 1 tests document the pre-W1 proportional-work baseline; current W1 acceptance evidence documents the implemented child-job representation and supersedes that historical representation as current runtime semantics:

1. **Runtime instantiates from a published model.** `FactoryRuntime.forModel(FactoryModelVersion)` is the only constructor; there is no path to a `FactoryRuntime` that was not built from a published model. Proven by every test in `Gate1EngineReadinessAcceptanceTest`, `ExplicitWorkloadSubmissionTest`, and current W1 acceptance coverage.
2. **Explicit submission without economy/demand/agents.** `FactoryRuntime.submitWorkload(productId, quantity, unitPrice)` requires no `DemandModel`, `PricingState`, or agent. Proven by `ExplicitWorkloadSubmissionTest` and `Gate1EngineReadinessAcceptanceTest.modelAndRuntimeBoundary`.
3. **Quantity consumes proportional production work.** Historical Gate 1 evidence established proportional work in the one-job representation; W1 preserves proportional work by materializing one unit-quantity child per requested unit, each traversing the routing once.
4. **Immutable order intent vs. mutable execution state.** The `Order`/`Job` split is established: `Order` is immutable; W1 materializes several mutable sibling `Job`s under one order-level aggregate.
5. **Work-item progress is represented.** Current runtime exposes independently progressing child jobs with stable per-order ordinals and aggregate released/completed quantities.
6. **Completion derived from executed work.** Exactly one aggregate order completion occurs only after the final child completes.
7. **Determinism.** W1 acceptance evidence covers deterministic child ordinals, `JobId` allocation, initial dispatch, assignments, ordered events, and aggregate terminal state.
8. **Economy-driven workload shares the same accepted-order path.** Both economy-driven `OrderCreation` and explicit submission converge on the same acceptance/decomposition semantics.

## 6. Gate 2 — Capability-based deterministic dispatch

### 6.1 Goal

Use the published model's resource definitions, installed instances, and operation eligibility/capability semantics to select runtime capacity deterministically.

The model side owns facts equivalent to:

```text
Resource definition
    capabilities / nominal processing properties

Resource instance
    installed identity and definition

Operation requirement
    capability or explicit eligible-resource semantics
```

Runtime owns:

```text
resource availability
queue state
active work
assignment
dispatch decision
```

### 6.2 Current problem (resolved)

A routing step used to point to one concrete `MachineId`, so a second equivalent machine could not naturally participate in the route -- even though `OperationStepDefinition.eligibleResources` was already `Set<MachineId>`, `FactoryModelValidator` rejected any step naming more than one, and `FactoryRuntimeAssembler`/`RoutingStep` collapsed to a single machine.

Gate 2 removed that restriction: `RoutingStep` now carries `Set<MachineId> eligibleMachines`, and `FactoryHandler` selects among them deterministically at dispatch time. See ADR-0005 for the original dispatch decision; [ADR-0009](../architecture/decisions/0009-gate-2-closure-and-work-decomposition-boundary.md) supersedes its slice-scoping conclusion and records the dispatch-vs-decomposition boundary.

### 6.3 Deterministic dispatch

The dispatch policy established by ADR-0005 and retained by ADR-0009, applied in `FactoryHandler.selectMachine`:

1. eligible;
2. online (an eligible machine that is `Offline` is excluded from selection while any eligible machine is online);
3. able to accept work immediately (`Machine.canAcceptJob()`);
4. lowest queue depth;
5. lowest `MachineId` as final tie-breaker.

Projected-completion-time ranking, resource pools, and capability taxonomies were considered and deliberately deferred -- none are required by the acceptance criteria below. A future slice that changes ranking must record that decision rather than silently reinterpreting the current policy.

Gate 2 is about **dispatch**, not work decomposition: the runtime selects an eligible resource for an independently dispatchable unit of work that already exists. ADR-0010 defines how W1 creates those units within one accepted quantity-scaled order: deterministic unit-quantity sibling `Job`s identified by `JobId`. That decision does not change Gate 2's selector or `pendingMultiEligible` semantics.

### 6.4 Acceptance criteria

Gate 2 is satisfied when:

1. Runtime consumes model-side resource definitions and installed instances rather than redefining them. **Satisfied** (unchanged by this slice -- `FactoryRuntimeAssembler` builds `Machine`s directly from `ResourceDefinition`).
2. Two equivalent eligible resource instances can both execute the same operation. **Satisfied** -- proved at both the `FactoryHandler` seam (`MultiResourceDispatchTest`) and end to end through the published-model boundary (`Gate2MultiResourceDispatchAcceptanceTest`, driven entirely through `FactoryRuntime`).
3. Both resources are used when workload justifies parallel capacity. **Satisfied** for sufficient independently dispatchable work: `Gate2MultiResourceDispatchAcceptanceTest.publishedMultiEligibleModelSurvivesAssemblyAndDispatchesBothOrdersConcurrently` proves two independent orders occupy both eligible machines at once, through `FactoryRuntime` alone. Gate 2 does not require one quantity-scaled order to be decomposed; ADR-0010/W1 provides that separate capability.
4. Equal candidates resolve reproducibly through a stable tie-break rule. **Satisfied** -- `MultiResourceDispatchTest.equalCandidatesResolveDeterministicallyToTheLowestMachineId` and `Gate2MultiResourceDispatchAcceptanceTest.identicalWorkloadFromTwoFreshRuntimesResolvesToTheSameMachineAssignments`.
5. Adding a second compatible resource changes queueing, utilization, throughput, or completion time in an appropriate workload. **Satisfied** under concurrent independent orders. W1 separately proves the same capacity can affect sibling jobs from one order.
6. Removing/disabling one instance does not require changing the product definition. **Satisfied**, including machine recovery and cross-machine pending-work reconsideration.
7. Resource capability, operational status, and queue state remain distinct concepts. **Satisfied** -- `OperationStepDefinition.eligibleResources`, `MachineState`, per-machine queue state, and `pendingMultiEligible` remain separate concepts.

**Gate 2 is complete.** Intra-order parallelism is not an unfinished Gate 2 condition; it is the separately accepted and functionally implemented W1 execution-decomposition contract in ADR-0010. Also deferred unless a concrete need arises: load-aware ranking beyond queue depth and resource pools/capability taxonomies.

## 7. Gate 3 — Consumer-neutral simulation session

### 7.1 Goal

Provide transport-independent runtime-control semantics before treating HTTP, SSE, CLI, or an embedded Java adapter as a stable external contract.

Conceptually, a session needs operations equivalent to:

```text
instantiate/load published model atomically
submit workload/command and receive accepted/rejected result
advance one event
advance until target simulation time
advance with maximum event bound
observe current runtime state
read produced events
reset deterministically
```

Model validation/publication belongs upstream. The session may verify that the supplied published model/version is usable, but it should not become a second model-authoring API.

### 7.2 Required command semantics

Every externally initiated runtime change returns a definite result containing at least:

- accepted/rejected status;
- stable result/rejection code;
- understandable diagnostic;
- affected entity identifiers when applicable;
- session/model provenance;
- events produced by the accepted command, or a cursor identifying them.

A rejected runtime command must not leave partial mutation.

W1 preserves this contract. In particular, rejected workload submission must leave no partial `Order`, order-level execution aggregate, child `Job`, machine assignment/queue mutation, `pendingMultiEligible` entry, or command-produced event.

### 7.3 Bounded advancement

Interactive consumers control presentation speed by requesting bounded simulated progress. Wall-clock sleeping and rendering cadence remain outside the simulation core.

The session should support enough bounded advancement for:

- event stepping;
- pause/resume presentation semantics;
- normal and accelerated presentation speeds;
- deterministic headless runs;
- protection against unbounded work monopolizing a caller.

### 7.4 Acceptance criteria

Gate 3 is satisfied when a non-graphical reference consumer can:

1. instantiate a published factory model version;
2. submit explicit production workload and receive structured results;
3. advance exactly one event;
4. advance to a selected simulated time with a maximum event bound;
5. inspect order, work-item, queue, and resource state;
6. reset and reproduce the same result;
7. identify the source model version throughout the session;
8. operate without Spring controllers, frontend DTOs, or mutable domain internals.

**Gate 3 is complete.** All eight criteria have identified executable evidence through `FactoryRuntime`; see [ADR-0007](../architecture/decisions/0007-gate-3-session-control-primitives.md). W1 preserves those session semantics while changing the work-item representation.

With Gate 3 closed, **W1's functional implementation is now in place (§14.1)**. Its remaining completion item is the required large-order performance/memory benchmark. Gate 4 follows W1 completion so its stable observation/event contract can expose the resulting `OrderId`/`JobId` identities and aggregate progress semantics with the supported-envelope evidence known.

## 8. Gate 4 — Stable observations and event envelopes

### 8.1 Goal

Expose enough supported runtime information for a consumer to understand the simulation without reaching into internal stores or reconstructing authoritative state from undocumented events.

[ADR-0011](../architecture/decisions/0011-runtime-observation-and-event-contract.md) is the accepted architecture for Gate 4 semantics. The focused [Gate 4 Runtime Observation and Event Delivery](gate-4-runtime-observation-event-delivery.md) plan owns implementation slicing and evidence sequencing. In particular, internal scheduler `Event`/`EventType`/`EventPayload` remain transition machinery rather than the supported external compatibility contract; supported runtime events describe authoritative changes after processing, and a fresh observation remains sufficient to reconstruct current consumer state without full event replay.

### 8.2 Minimum observation

The consumer-neutral observation should contain, directly or through purpose-specific projections:

```text
Session
    session/run ID
    model fingerprint
    controlled revision ID [optional when authoritatively bound]
    current simulated time
    run state
    latest event sequence

Resources
    instance ID
    definition ID
    operational status
    queue depth
    active work
    current operation
    expected completion time where available

Orders
    requested quantity
    released quantity
    completed quantity
    status

Work items
    JobId
    parent OrderId
    ordinal within order
    execution quantity
    current operation
    assignment
    execution state
    start/end timing

Performance
    throughput
    lead time
    work in process/backlog
    utilization
```

The minimum order/work-item split above follows ADR-0010. Gate 4 may choose purpose-specific projection types and envelope structure, but it must not collapse aggregate order progress back into one child job or reinterpret `JobId` as an order identity.

Not every consumer must receive one universal state dump. Purpose-specific observations remain preferable where they preserve capability boundaries.

### 8.3 Event envelope

Every externally visible supported runtime event has an envelope equivalent to:

```text
run/session ID
sequence
simulation time
semantic event type
model fingerprint
controlled revision ID [optional when authoritatively bound]
affected entity references
payload
```

The sequence is strictly monotonic within one run/session epoch and makes order explicit independently of event timestamps. A reset creates a new run identity and sequence epoch; run identity is correlation metadata and must not influence deterministic simulation outcomes.

ADR-0010 already requires W1's aggregate completion payload to include explicit `OrderId` while retaining the completing child `JobId`. Gate 4 preserves that correlation in the supported contract.

`ModelFingerprint` is mandatory source-model provenance under ADR-0006/ADR-0011. `ControlledRevisionId` is optional and present only when the runtime was instantiated with an authoritative revision binding; Gate 4 does not synthesize revision identity or take ownership of Governance G1.3 persistence/resolution.

These are **simulation-runtime** events and observations. A future Operational Execution adapter may translate relevant production semantics into its own command/result and external-observation contracts, but Gate 4 does not define production telemetry envelopes, external source authenticity, or digital-twin reconciliation.

### 8.4 Acceptance criteria

Gate 4 is satisfied when:

1. Every externally visible runtime entity has a stable identifier within the session semantics that own it.
2. Observation state and emitted events agree about the same authoritative transition history.
3. Event ordering is explicit and reproducible.
4. A consumer can identify the active bottleneck using supported observations rather than internal stores.
5. Requested, assigned, started, completed, and reported states are distinguishable.
6. Runtime observations and supported runtime events carry the durable source `ModelFingerprint`; a controlled revision ID is carried only when authoritatively bound.
7. API/UI DTOs remain outward projections and are not reused as domain decision inputs.
8. A fresh observation can reconstruct current view state without replaying the entire history.
9. Successful state-change runtime events are published only after the relevant authoritative processing succeeds; rejected changes do not produce successful state-change events.
10. Supported event sequence is strictly monotonic within a run/session and independent of simulation timestamp; same-time events remain explicitly ordered.
11. W1 runtime events preserve `OrderId`/child `JobId` correlation where work-item changes belong to an aggregate order.
12. Reset creates a new run identity/sequence epoch without changing deterministic semantic outcomes for otherwise identical inputs.

## 9. Gate 5 — Spatial runtime consequences

### 9.1 Goal

Apply deterministic production consequences to semantic layout supplied by the published factory model.

The design/model side owns facts equivalent to:

```text
Factory floor dimensions
Resource position
Resource orientation
Resource footprint
Semantic transfer/connection inputs
```

Runtime owns consequences such as:

```text
transfer duration
transfer start/completion
work-item location/progress
layout-dependent completion time
```

A sufficient first transfer policy may be:

```text
transfer time = fixed handling time
              + Manhattan distance * ticks per cell
```

The exact metric remains an open runtime/model-policy decision until validated by a prototype.

### 9.2 Hierarchy is not layout

Resource scope and physical placement remain separate dimensions:

```text
Resource scope
    Factory -> Work Center / Resource Pool -> Resource Instance

Spatial layout
    Floor -> Position -> Footprint -> Transfer distance
```

Moving a resource changes spatial consequences without changing its identity or hierarchy membership. Changing pool/work-center membership does not implicitly move it.

### 9.3 Transfer state

Transfers should be represented through explicit runtime state and/or events such as:

```text
TransferStarted
TransferCompleted
```

The runtime owns transfer timing. A consumer may interpolate visual movement between authoritative times.

### 9.4 Acceptance criteria

Gate 5 is satisfied when:

1. Runtime consumes validated semantic layout from a published model rather than accepting editor-specific geometry directly.
2. Two published models with identical resources/workload/processing durations but different semantic positions produce different completion times when transfer distance differs.
3. Both results are deterministic.
4. Transfer time is observable and attributable through supported events/observations.
5. Moving a resource creates a new model version and changes transfer behavior without changing resource identity.
6. Resource-pool/hierarchy changes do not implicitly change spatial placement.
7. No pathfinding, worker, vehicle, aisle, or congestion model is required to satisfy the gate.

## 10. Headless engine acceptance scenarios

The gates and active W1 capability are proven before a game client exists. Model variants are published through the design capability and instantiated as separate runtimes.

### 10.1 Gate 2 capacity benchmark — independently dispatchable work

```text
Product
    CUT -> ASSEMBLE -> INSPECT

Production workload
    two or more independent orders/jobs totaling 20 units

Published model A
    one cutter
    one assembler
    one inspector

Published model B
    two equivalent cutters
    one assembler
    one inspector
```

Expected evidence:

- both runs are deterministic;
- model B dispatches independently dispatchable work to both cutters;
- cutter queueing, throughput, utilization, or total completion time changes;
- if assembly becomes the new bottleneck, supported observations expose it;
- no product definition is rewritten to name the second cutter.

This benchmark intentionally proves Gate 2 only: it does not require one quantity-scaled order to split across both cutters. The current reference consumer does require that separate behavior, so W1 has its own fixed-contract benchmark below.

### 10.2 W1 fixed-contract work-decomposition benchmark

```text
Product
    CUT -> ASSEMBLE -> INSPECT

Production requirement
    one accepted Order for 20 units

Published model A
    one cutter
    one assembler
    one inspector

Published model B
    two equivalent cutters
    one assembler
    one inspector
```

Expected evidence under ADR-0010:

- exactly one accepted parent `Order` exists for the 20-unit requirement;
- exactly 20 sibling unit-quantity `Job`s exist under that order;
- child ordinals, `JobId` allocation, and initial dispatch order are deterministic;
- each child independently traverses `CUT -> ASSEMBLE -> INSPECT` exactly once and preserves its own operation precedence;
- model B can execute different sibling jobs from the same order concurrently on both eligible cutters;
- aggregate `releasedQuantity` is 20 after successful acceptance and aggregate `completedQuantity` advances exactly once per completed child from 0 through 20;
- exactly one `OrderCompleted` is emitted, carrying explicit parent `OrderId` and the child `JobId` whose completion caused the aggregate transition;
- backlog, completed-sales count/value, and order lead-time semantics remain order-level rather than multiplying by child count;
- adding the second cutter changes queueing, utilization, throughput, or completion time for this fixed-contract workload;
- repeated fresh runs reproduce child identities/order, assignments, ordered events, progress transitions, and terminal state;
- quantity 1 remains the degenerate one-order/one-child case;
- the proof uses Arcogine-owned execution semantics rather than game-owned splitting logic.

The W1 acceptance suite must also preserve existing Gate 2 independent-order behavior, offline/recovery behavior, Gate 3 reset/replay and bounded advancement, and rejected-submission atomicity.

A non-functional large-order benchmark (for example quantity 100,000) must record the memory/execution impact of unit decomposition. W1 accepts quantity-proportional resident work-item count because arbitrary chunking would invent batch semantics; measured evidence should determine whether a later representation-efficiency design is required.

### 10.3 Layout benchmark

```text
Same product definition
Same production order
Same resource definitions/instances
Same processing durations

Published model A
    resources close together

Published model B
    resources far apart
```

Expected evidence:

- processing work is identical;
- transfer duration differs;
- total completion time differs;
- transfer events/observations explain the difference;
- repeated runs reproduce each model's results;
- each run identifies the model version used.

## 11. Distribution hardening after the core gates

These capabilities are required before treating an external client as distributable, but they do not block the first headless readiness proof.

| Capability | Requirement |
|---|---|
| Public contract versioning | Version model, command, event, observation, and protocol schemas |
| Reliable event recovery | Support monotonic event IDs and a defined reconnect/resynchronization strategy |
| Exact checkpoint and restore | Persist source model identity, simulated time, runtime state, queues, active work, scheduler contents, random state, and event position |
| Sidecar lifecycle and packaging | Start, health-check, version-check, communicate with, and stop a bundled local runtime without requiring a separate Java installation |
| Compatibility tests | Keep consumer-contract fixtures that fail on accidental breaking changes |

Gate 4 establishes the recovery primitives—run identity, monotonic supported-event sequence, observation cursor, and transport-neutral event semantics—but not the complete recovery mechanism. Distribution hardening later owns retained supported-event history, reconnect/resume cursors, explicit gap detection/resynchronization, contract versioning, and exact checkpoint/restore. Recovery uses a fresh observation plus ordered deltas when history cannot be resumed; it does not require event sourcing or full-history replay.

A game save may wrap an Arcogine checkpoint with game-owned state. Arcogine does not own campaign progress, score, camera state, or user preferences.

This distribution hardening is for simulation consumers. Production connectivity additionally depends on the Operational Execution/Digital Twin readiness gates and must not infer production safety from simulation packaging/recovery maturity.

## 12. Explicit non-goals

Engine readiness does not require:

- defining or persisting consumer drafts;
- a complete ISA-95 object model/hierarchy/transaction set/B2MML profile/conformance claim;
- Enterprise or Site entities without a concrete use case;
- personnel, worker skills, fatigue, or pathfinding;
- raw-material procurement/suppliers/BOM merely for this initiative;
- maintenance, failure, quality, or shift-management domains;
- live production connectivity or operational execution;
- production identity/trust/authorization or credential lifecycle;
- production deployment application or effective applied-artifact provenance;
- external telemetry ingestion or digital-twin reconciliation;
- fail-safe physical actuation or production adapter recovery;
- dynamic mutation of published factory structure while work is in flight;
- generalized material-lot identity/genealogy, configurable production batch sizes, transfer batches, or split/merge semantics as part of W1;
- setup-family optimization, priority/due-date scheduling, or generalized scheduler semantics merely to implement W1;
- a generic plugin framework;
- multiplayer/distributed simulation/remote hosting;
- game rendering, scoring, progression, narrative, tutorials, or player economy.

The objective is a consumer-ready deterministic production **simulation** runtime over a published factory model, not a universal MES/digital-twin/game framework.

## 13. Delivery order and first runtime milestone

### 13.1 Prerequisite

Complete the behavior-preserving canonical-model seam from [Factory Design Capability](factory-design-capability.md) — this is the model-seam entry gate defined in §1.1, not the full D1-D4 acceptance criteria:

```text
Scenario factory semantics
      -> FactoryModel
      -> validate/publish
      -> instantiate existing runtime
      -> preserve deterministic results
```

### 13.2 Runtime delivery order

1. Separate accepted immutable order intent from mutable job execution. **Implemented as the first behavior-preserving Gate 1 slice.**
2. Add explicit workload submission independent of the economy/pricing loop. **Implemented as the second Gate 1 slice** (`FactoryRuntime.submitWorkload`, backed by package-private `FactoryHandler.submitOrder`).
3. Make quantity consume proportional production work. **Implemented as the third Gate 1 slice** using repeated routing inside one `Job`; retained as historical pre-W1 evidence, not current runtime behavior.
4. Capability/eligibility-driven deterministic dispatch. **Implemented as Gate 2**; see ADR-0009 for the dispatch/decomposition boundary.
5. Consumer-neutral session and bounded advancement. **Implemented as Gate 3**; see ADR-0007.
6. **W1 — intra-order execution decomposition. Functional implementation in place.** Architecture resolved by [ADR-0010](../architecture/decisions/0010-intra-order-execution-decomposition-and-work-item-identity.md): quantity `N` -> `N` unit-quantity sibling `Job`s; `JobId` is work-item identity; order-level execution progress uses the parent `OrderId`; Gate 2 dispatch is reused unchanged; exactly one order completion; deterministic ordering/replay. The required large-order benchmark remains outstanding before W1 is fully complete.
7. Stable runtime observations and ordered authoritative runtime events under [ADR-0011](../architecture/decisions/0011-runtime-observation-and-event-contract.md), delivered headlessly according to [Gate 4 Runtime Observation and Event Delivery](gate-4-runtime-observation-event-delivery.md).
8. Spatial transfer consequences from published layout.
9. Public-contract, recovery, persistence, and packaging hardening.

### 13.3 First runtime milestone

> **Run a deterministic explicit production order through separate order intent and work execution without using the economy demand loop.**

Definition of done:

```text
Published model supplies product/operation definitions
Production order exists as runtime workload
Quantity creates proportional work
Work execution is distinct from order intent
Order progress is observable
Completion is deterministic
Economy and sales agent are optional workload sources
Existing scenario behavior remains covered
```

This milestone deliberately excludes changing the canonical-model boundary, layout consequences, public HTTP versioning, checkpointing, sidecar packaging, and game UI.

## 14. Open decisions and ADR triggers

| Decision | Trigger for resolution |
|---|---|
| Final aggregate/type boundaries for order and work execution | Gate 1 baseline implemented; W1 parent/child shape further resolved by ADR-0010 |
| Intra-order execution decomposition and work-item identity | **Resolved by ADR-0010; functional implementation in place; large-order benchmark evidence outstanding** |
| Future lot/batch sizing or material-lot identity | A concrete domain requirement supplies real lot/batch semantics beyond W1 unit decomposition |
| Capability pools versus explicit eligible-instance sets | A concrete scheduling/control use case cannot be expressed cleanly by explicit eligibility |
| Deterministic dispatch policy | Resolved by Gate 2 / ADR-0009; revisit only if a concrete workload requires a different ranking policy |
| Session interface/module ownership | Resolved by Gate 3 / ADR-0007; revisit only if a concrete consumer proves `FactoryRuntime` is no longer the right boundary |
| Tick/event-count advancement semantics | Interactive/headless responsiveness tests |
| Observation/event contract semantics | **Resolved by ADR-0011; implementation decomposition remains in the focused Gate 4 delivery plan** |
| Spatial metric/transfer policy | Layout benchmark prototype |
| Public compatibility policy | Before external consumer contract publication |

### 14.1 W1 — active execution capability: intra-order execution decomposition

Intra-order parallelism is an **active W1 Engine execution capability with its functional implementation in place**, not unfinished Gate 2 work. ADR-0010 records the child-job identity and aggregate-completion shape. Large-order benchmark evidence remains required before W1 can be marked fully implemented.

The accepted W1 model is implemented: Arcogine materializes deterministic unit-quantity child jobs under one authoritative order-execution aggregate; the game supplies only one production requirement.

```text
Order
    immutable production intent
    OrderId
    requested quantity N
        |
        v
order-level execution aggregate
    identity: same OrderId
    requested/released/completed quantity
        |
        +---- Job ordinal 0, executionQuantity 1, JobId
        +---- Job ordinal 1, executionQuantity 1, JobId
        +---- ...
        +---- Job ordinal N-1, executionQuantity 1, JobId
```

W1 therefore does **not** need another design round to choose whether one unit becomes one execution object: for this first capability, it does. This is a deliberately minimal unit-decomposition policy, not a generalized lot/batch framework.

Required implementation semantics are fixed by ADR-0010:

- accepting quantity `N` creates exactly `N` unit-quantity sibling `Job`s under one `Order`;
- `JobId` is the independently dispatchable work-item identity already used by machine queues, active work, pending work, and task events;
- no new `ExecutionUnitId`, `LotId`, or `BatchId` is introduced;
- child ordinal is immutable deterministic ordering metadata, not identity;
- all W1 children are released atomically at acceptance, so `releasedQuantity == requestedQuantity` after successful submission;
- each child traverses the routing exactly once and has no sibling precedence beyond its own routing order;
- Gate 2's selector and `pendingMultiEligible` semantics are reused unchanged;
- aggregate `completedQuantity` increments exactly once per child final completion;
- the `N-1 -> N` aggregate transition emits exactly one `OrderCompleted`, carrying explicit `OrderId` and the completing child `JobId`;
- backlog, completed-sales count/value, lead time, and existing order-throughput measures remain order-level;
- `FactoryRuntime.submitWorkload` continues to return one `OrderId` and rejected submission remains zero-partial-mutation across the entire parent/child creation operation;
- runtime observation must expose aggregate order progress rather than forcing consumers to infer it by counting child jobs;
- child creation, `JobId` allocation, initial dispatch, queueing, pending-work reconsideration, machine tie-breaking, and same-time event ordering remain deterministic;
- implementation must include the fixed-contract acceptance benchmark in §10.2 and a large-order performance/memory benchmark.

This decision intentionally leaves material-lot genealogy, configurable batch sizes, transfer batches, split/merge, setup optimization, inventory allocation, generalized scheduling, and durable cross-session work-item identity out of W1. Those require separate accepted semantics when a concrete use case justifies them.

W1 remains placed **before Gate 4** deliberately. Gate 4 must stabilize observations and event envelopes around the execution identities and aggregate progress model W1 establishes rather than around the obsolete one-Job-per-Order runtime shape.

`IntraOrderExecutionAcceptanceTest` proves the fixed quantity-20 workload: one order, twenty deterministic children, concurrent use of two eligible cutters, aggregate completion, one business completion, and pre-mutation rejection above the provisional 100,000-child materialization limit. The required large-order benchmark will determine the evidence-backed supported envelope.

The canonical model/run/runtime boundary is tracked by [ADR-0003](../architecture/decisions/0003-canonical-factory-model-boundary.md). Operational execution-context/trust/command/deployment/reconciliation decisions belong to the sibling operational track and should receive their own ADRs when hard-to-reverse contracts are selected. Record additional accepted Engine decisions as ADRs rather than expanding this plan into a decision log.

## 15. Documentation lifecycle

While this work is proposed, this file remains under `docs/planning/`.

As gates become implemented:

- update [`../architecture/overview.md`](../architecture/overview.md) with established runtime architecture;
- update [Factory Design Architecture](../architecture/factory-design.md) only as the model boundary becomes accepted/implemented;
- update the [ISA-95 semantic mapping](../architecture/isa-95-semantic-mapping.md) when manufacturing concepts change, while distinguishing accepted target architecture from implementation status;
- update [`../reference/api.md`](../reference/api.md) only for implemented public behavior;
- add ADRs for durable execution, scheduling, session, persistence, and compatibility decisions;
- keep headless acceptance scenarios executable and version-controlled;
- keep production connectivity, deployment, external observation, reconciliation, and trust semantics in the sibling [Operational Execution and Digital Twin Readiness](operational-execution-digital-twin-readiness.md) track until they become implemented current architecture.

Once the readiness initiative is complete or abandoned, reduce this file to a concise historical outcome or retire it after durable decisions and current behavior are represented in authoritative locations.