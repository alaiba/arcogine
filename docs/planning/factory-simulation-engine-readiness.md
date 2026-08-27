# Factory Simulation Engine Readiness

> **Status:** Proposed  
> **Scope:** Prepare Arcogine's factory runtime for external consumers after the canonical factory-model boundary is established  
> **Authority:** Planning only; this document defines runtime-readiness gates, not current capability or accepted architecture  
> **Prerequisite:** the model-seam entry gate (§1.1) — narrower than full D1-D4 in [Factory Design Capability](factory-design-capability.md)  
> **Related:** [Factory Design Architecture](../architecture/factory-design.md), [ADR-0003](../architecture/decisions/0003-canonical-factory-model-boundary.md), [ADR-0004](../architecture/decisions/0004-model-identity-revision-lineage-and-external-change-control.md), [Operational Execution and Digital Twin Readiness](operational-execution-digital-twin-readiness.md), [Factory-Design Game Consumer Initiative](factory-design-game-consumer.md), [ISA-95 Semantic Mapping](../architecture/isa-95-semantic-mapping.md), [Architecture Overview](../architecture/overview.md)

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
- current provenance identity policy exists (today's provisional content hash; see ADR-0004)
- runtime instantiates only from the published model
- representative baseline behavior is preserved
```

This gate is satisfied by what has already landed. It does not require the D1 definition/instance split, the D2 stable finding taxonomy (codes/severity/entity metadata), or the D3 durable cross-process fingerprint contract — those remain open design-capability work, tracked independently, and are not prerequisites for Gate 1 or any later gate in this plan. Result-level model provenance (`SimResult` carrying the provenance `IntegratedHandler` already has) is implemented; broader run-level provenance (run ID, scenario/input fingerprint, engine build) remains a small, separately tracked follow-up rather than a gate blocker.

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

Game implementation begins only after the design prerequisite and Gates 1-5 below are satisfied by headless evidence.

```text
Prerequisite  Model-seam entry gate satisfied (§1.1) — not full Design D1-D4
        ↓
Gate 1        Explicit workload and execution model
        ↓
Gate 2        Capability-based deterministic dispatch
        ↓
Gate 3        Consumer-neutral simulation session
        ↓
Gate 4        Stable observations and event envelopes
        ↓
Gate 5        Spatial runtime consequences
        ↓
Game consumer may begin
```

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

`FactoryHandler` persists an immutable `Order` before creating its execution `Job`; `Job` no longer owns product, quantity, or price fields. Existing `JobView` product/quantity/price/value getters remain compatibility projections delegated to the referenced order so the current API/UI wire contract stays unchanged.

The second Gate 1 slice added an explicit, consumer-neutral workload-submission entry point: `FactoryRuntime.submitWorkload(productId, quantity, unitPrice)`. `FactoryRuntime` wraps a `FactoryHandler` together with a `Scheduler` it owns internally, so a caller supplies only product/quantity/commercial intent -- never a mutable `Scheduler` or an authoritative simulation time, which stayed internal event-engine plumbing rather than becoming part of the workload boundary. Both `FactoryRuntime.submitWorkload` and the economy-driven `OrderCreation` event resolve to the same package-private `FactoryHandler.submitOrder(productId, quantity, unitPrice, currentTime, scheduler)` acceptance operation -- `FactoryHandler.handleEvent` delegates to it for that event rather than duplicating the logic, and it is not exposed outside the `factory.process` package. `FactoryHandler` already had no compile-time dependency on economy/pricing/demand/agents; this slice makes explicit submission a supported, named entry point instead of something only reachable by hand-constructing an internal event payload and owning a `Scheduler`. `FactoryRuntime` is only constructed from a published model, via `FactoryRuntime.forModel(FactoryModelVersion)` (internally using `FactoryRuntimeAssembler`) -- never wrapped around an already-live `FactoryHandler` some other scheduler might also be driving, so it always owns the exclusive factory/scheduler pair it advances and event ordering stays globally authoritative. It also does not expose the mutable `FactoryHandler` directly; callers observe state through `FactoryRuntime`'s own read-only projections (`ordersView`, `jobsView`, `job`, `machinesView`, `backlog`, `avgLeadTime`, `throughput`, `completedSalesValue`, `completedSales`). A headless test (`ExplicitWorkloadSubmissionTest`) builds a runtime this way and submits workload directly, with no `DemandModel`, `PricingState`, or agent in the loop, and proves repeated identical submissions are deterministic. `FactoryRuntime.advance()` pumps exactly one pending event at a time so a headless caller can drive submitted workload to completion without reaching into scheduler internals; this is deliberately not a general session/advancement API -- that remains Gate 3 work. Economy-driven scenarios continue to work unchanged: `DemandModel.generateOrders` still schedules `OrderCreation` events, which route through the same `submitOrder` logic.

The third Gate 1 slice made order quantity consume proportional production work. `FactoryHandler.submitOrder` now sizes a job's routing to `routing.stepCount() * quantity` instead of `routing.stepCount()`: the job repeats its routing once per unit of quantity, and dispatch/completion resolve each job-global step back to its underlying `RoutingStep` by `stepIndex % routing.stepCount()`. This was chosen over multiplying each step's fixed `duration` by quantity, and over creating one `Job`/`JobId` per unit, because it gives countable progress (`Job.currentStep()` is a job-global counter that advances once per routing step executed, i.e. `routing.stepCount()` times per unit) without multiplying `Order`/`Job` object volume per unit of quantity -- a 100,000-unit order still creates exactly one `Order` and one `Job`, just with a larger `totalSteps` counter -- and it keeps a straightforward seam for Gate 2 to later dispatch remaining repetitions to available capacity in parallel. This deliberately does not bound *event* volume the same way: `TaskStart`/`TaskEnd` events are still scheduled once per routing step per unit (quantity-proportional), because that is the mechanism that makes quantity actually consume proportional machine-occupied time; only object allocation (`Order`/`Job`/`JobId`) stays flat. The externally visible `TaskStart`/`TaskEnd.stepIndex` continues to carry the routing-local index (`stepIndex % routing.stepCount()`), not the job-global counter, so that event field's existing meaning is unchanged. `Job` remains strictly one-to-one with `Order` under this model, so `OrderCompleted`'s existing `jobId` correlation field stays unambiguous and did not need to change; `OrderCompleted` still fires exactly once per order, only after the job's full (quantity-scaled) routing completes. `completedSales`/`backlog`/`avgLeadTime` keep their existing per-order (per-job) counting semantics -- quantity now changes *how long* a job occupies a machine and stays in backlog, not what those counts measure. Both the economy-driven `OrderCreation` path and `FactoryRuntime.submitWorkload` resolve to this same `submitOrder` operation, so they share identical proportional-quantity semantics. See `ProportionalQuantityWorkTest` for the headless proof, including a multi-step, multi-quantity execution that exercises the `stepIndex` modulo wrap-around directly (quantity 10 taking ten times the machine-occupied ticks of quantity 1, determinism, single completion, and economy/explicit-path equivalence).

One accepted order corresponds to exactly one execution job (the routing repeats within that job rather than spawning multiple jobs). This is the deliberate current model, not an unfinished corner: it gives countable per-unit progress and a straightforward Gate 2 dispatch seam (§5.2 above) without multiplying `Order`/`Job` object volume per unit of quantity. Multiple execution objects per order only become necessary if a future quantity/dispatch model requires them (for example, dispatching remaining repetitions across parallel equivalent capacity in Gate 2) -- nothing in Gate 1 requires that shape today.

The existing `OrderCompleted` event retains `jobId`, not an explicit `orderId`, as its correlation field. Under the 1 Order <-> 1 Job invariant this model keeps, that correlation is unambiguous and fully supported: a consumer resolves `OrderCompleted.jobId` through `FactoryRuntime.job(jobId)` to a `JobView`, whose `orderId()` identifies the accepted `Order` the completion belongs to. `Gate1EngineReadinessAcceptanceTest` proves this path end to end through `FactoryRuntime` alone. Adding an explicit `orderId` field to the event payload is a stable-event-contract concern -- it belongs to the later Gate 4 (`§8`) work on event envelopes, not Gate 1: Gate 1 only requires that completion be derivable and observable through a supported observation, which it is.

The canonical-model seam is already complete enough for this runtime work, so definition-model changes do not need to be mixed into the remaining Gate 1 refactor.

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

**Gate 1 is complete.** All eight criteria have identified executable evidence:

1. **Runtime instantiates from a published model.** `FactoryRuntime.forModel(FactoryModelVersion)` is the only constructor; there is no path to a `FactoryRuntime` that was not built from a published model. Proven by every test in `Gate1EngineReadinessAcceptanceTest`, `ExplicitWorkloadSubmissionTest`, and `ProportionalQuantityWorkTest`.
2. **Explicit submission without economy/demand/agents.** `FactoryRuntime.submitWorkload(productId, quantity, unitPrice)` requires no `DemandModel`, `PricingState`, or agent. Proven by `ExplicitWorkloadSubmissionTest` and `Gate1EngineReadinessAcceptanceTest.modelAndRuntimeBoundary`.
3. **Quantity consumes proportional production work.** `FactoryHandler.submitOrder` sizes a job's routing to `routing.stepCount() * quantity`. Proven by `ProportionalQuantityWorkTest` (including the routing-local `stepIndex` modulo wrap-around) and, at the `FactoryRuntime` boundary, by `Gate1EngineReadinessAcceptanceTest.quantityDrivesRepeatedProductionStepCompletionBeforeTheJobIsDone`.
4. **Immutable order intent vs. mutable execution state.** The `Order`/`Job` split: `Order` is an immutable record; `Job` is mutable execution state referencing it. Proven by `OrderIntentSeparationTest` and `Gate1EngineReadinessAcceptanceTest.modelAndRuntimeBoundary`.
5. **One job per order, with job-global progress standing in for "work items."** `Job.currentStep()` is a job-global counter advancing once per routing step executed (`routing.stepCount()` times per unit), from which per-unit progress is derivable (`currentStep / routing.stepCount()`). This is the deliberate current shape of the quantity model (see §5.2), not multiple `Job` objects per `Order`.
6. **Completion derived from executed work, observable via `FactoryRuntime.advance()`.** `OrderCompleted` fires only once `Job.isComplete()` (i.e., every quantity-scaled step has executed), and is observed as the return value of `FactoryRuntime.advance()`. Proven end to end by `Gate1EngineReadinessAcceptanceTest.completionIsObservableThroughFactoryRuntimeAdvance`, which also proves the `OrderCompleted.jobId` -> `FactoryRuntime.job(jobId)` -> `JobView.orderId()` correlation path described above.
7. **Determinism.** The strongest ordered-stream proof reaches the full `FactoryRuntime` contract, not just the `FactoryHandler`/`Scheduler` seam: `Gate1EngineReadinessAcceptanceTest.identicalWorkloadFromTwoFreshRuntimesProducesIdenticalOrderedEventStreamsAndTerminalState` drains the entire ordered event stream from two independently constructed, fresh `FactoryRuntime`s given the same published model and workload and asserts the streams are identical, plus identical terminal state (job completion, lead time, backlog, completed sales count/value, average lead time). `ExplicitWorkloadSubmissionTest.repeatedIdenticalSubmissionsAreDeterministic` and `ProportionalQuantityWorkTest.identicalWorkloadProducesIdenticalOrderedResults` remain as lower-level determinism evidence at the `FactoryHandler`/`Scheduler` seam.
8. **Economy-driven workload shares the same accepted-order/proportional-work path.** Both the economy-driven `OrderCreation` event and `FactoryRuntime.submitWorkload` resolve to the same package-private `FactoryHandler.submitOrder`. Existing regression evidence: `ProportionalQuantityWorkTest.economyDrivenOrderCreationFollowsTheSameProportionalWorkSemanticsAsQuantityOne` and `.explicitAndEconomyPathsProduceIdenticalProportionalWorkForTheSameQuantity` prove the two paths share identical proportional-work semantics; `OrderLifecycleIntegrationTest` (`interfaces/api`) proves the full economy-driven `OrderCreation -> Factory -> OrderCompleted -> Finance` chain end to end through the real wired `IntegratedHandler`; `DemandModelTest` proves the economy demand loop schedules `OrderCreation` events, which route through the identical acceptance path. This evidence was judged sufficient as-is -- no additional economy scenario test was needed to close this criterion.

With Gate 1 closed, **Gate 2 (capability-based deterministic dispatch, §6) is the next active gate.**

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

### 6.2 Current problem (resolved by the first Gate 2 slice)

A routing step used to point to one concrete `MachineId`, so a second equivalent machine could not naturally participate in the route -- even though `OperationStepDefinition.eligibleResources` was already `Set<MachineId>`, `FactoryModelValidator` rejected any step naming more than one, and `FactoryRuntimeAssembler`/`RoutingStep` collapsed to a single machine.

The first Gate 2 slice removed that restriction: `RoutingStep` now carries `Set<MachineId> eligibleMachines`, and `FactoryHandler` selects among them deterministically at dispatch time. See ADR-0005 for the full decision and its alternatives.

### 6.3 Deterministic dispatch

The accepted first-slice policy (ADR-0005), applied in `FactoryHandler.selectMachine`:

1. eligible;
2. online (an eligible machine that is `Offline` is excluded from selection while any eligible machine is online);
3. able to accept work immediately (`Machine.canAcceptJob()`);
4. lowest queue depth;
5. lowest `MachineId` as final tie-breaker.

Projected-completion-time ranking, resource pools, and capability taxonomies were considered and deliberately deferred (see ADR-0005's alternatives) -- none are required by the acceptance criteria below. A future slice that adds load-aware ranking should update or supersede ADR-0005 rather than reinterpreting this policy silently.

### 6.4 Acceptance criteria

Gate 2 is satisfied when:

1. Runtime consumes model-side resource definitions and installed instances rather than redefining them. **Satisfied** (unchanged by this slice -- `FactoryRuntimeAssembler` builds `Machine`s directly from `ResourceDefinition`).
2. Two equivalent eligible resource instances can both execute the same operation. **Satisfied** -- proved at both the `FactoryHandler` seam (`MultiResourceDispatchTest`) and end to end through the published-model boundary (`Gate2MultiResourceDispatchAcceptanceTest`, driven entirely through `FactoryRuntime`).
3. Both resources are used when workload justifies parallel capacity. **Satisfied for independent orders/jobs**: `Gate2MultiResourceDispatchAcceptanceTest.publishedMultiEligibleModelSurvivesAssemblyAndDispatchesBothOrdersConcurrently` proves two independent orders occupy both eligible machines at once, through `FactoryRuntime` alone. **Not yet proved for a single quantity-scaled order.** `submitOrder` still creates exactly one `Job` per order, and `handleTaskEnd` advances that job's repeated routing strictly one step at a time -- a fixed-contract workload expressed as one large-quantity order (rather than many independent orders) cannot yet exploit a second eligible machine's parallel capacity within that one job. Closing that gap would require either intra-job execution parallelism or multiple execution objects per order, both of which the original Gate 2 audit named as non-goals for this slice (see "multiple Jobs per Order" and "per-unit execution objects" in the plan's non-goals). This remains open for a later slice if a concrete workload requires it; it is not claimed satisfied by this slice.
4. Equal candidates resolve reproducibly through a stable tie-break rule. **Satisfied** -- `MultiResourceDispatchTest.equalCandidatesResolveDeterministicallyToTheLowestMachineId` and `Gate2MultiResourceDispatchAcceptanceTest.identicalWorkloadFromTwoFreshRuntimesResolvesToTheSameMachineAssignments`.
5. Adding a second compatible resource changes queueing, utilization, throughput, or completion time in an appropriate workload. **Satisfied for independent orders/jobs**, under the same scope note as criterion 3: both machines' queues stay empty under concurrent independent orders where a single-machine model would have queued the second one. Not yet proved for a single quantity-scaled order, for the same reason as criterion 3.
6. Removing/disabling one instance does not require changing the product definition. **Satisfied**, including the machine-recovery lifecycle: `MultiResourceDispatchTest.machineComingBackOnlineDispatchesWorkThatWasStrandedWaitingOnAnotherMachine` and `Gate2MultiResourceDispatchAcceptanceTest.bringingAnEligibleMachineOnlineDispatchesWorkStrandedWaitingForTheOtherMachine` prove that work waiting because one eligible machine was offline is not pinned to whichever other machine happened to be checked when it started waiting -- `FactoryHandler` reconsiders it against its whole eligible set whenever any eligible machine frees up or comes online (see `pendingMultiEligible`/`tryDispatchPendingMultiEligible`), so a machine that was offline when a job first waited can still pick it up once it recovers. `Gate2MultiResourceDispatchAcceptanceTest.offlineEligibleMachineIsExcludedAndRemovingItDoesNotRequireChangingTheProductDefinition` proves the product/operation definition is untouched, only runtime machine availability changes. `Gate2MultiResourceDispatchAcceptanceTest.disjointPendingPoolDispatchesEvenWhileAnEarlierUnrelatedPoolIsStillFull` proves the pending backlog does not head-of-line block: an undispatchable entry waiting on one still-fully-busy eligible pool does not stop a later entry with a disjoint eligible pool from dispatching the moment its own pool frees up.
7. Resource capability, operational status, and queue state remain distinct concepts. **Satisfied** -- `OperationStepDefinition.eligibleResources` (model eligibility), `MachineState` (operational status), and `Machine`'s per-machine queue plus `FactoryHandler`'s cross-machine pending backlog (runtime queue state) remain separate types; this slice does not merge them.

This closes the first Gate 2 slice as scoped by ADR-0005, for independent-order/job parallelism proved end to end through `FactoryRuntime`. Criteria 3 and 5 remain explicitly open for intra-job (single quantity-scaled order) parallel dispatch -- that is a distinct, larger design question this slice does not resolve or claim to. Also left for a later slice if a concrete need arises: load-aware ranking beyond queue depth, and resource pools/capability taxonomies.

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

**Gate 3 is complete.** All eight criteria have identified executable evidence, all through `FactoryRuntime` alone -- see [ADR-0007](../architecture/decisions/0007-gate-3-session-control-primitives.md) for the full decision record behind the additive shapes below.

1. **Instantiate a published model version.** Unchanged from Gate 1/2: `FactoryRuntime.forModel(FactoryModelVersion)` remains the only constructor.
2. **Structured submit-workload results.** `FactoryRuntime.submitWorkload` and `FactoryRuntime.setMachineAvailability` -- the two externally initiated runtime changes this type exposes -- return a definite `CommandResult<T>` (accepted/rejected status via the sealed `Accepted`/`Rejected` variant; a stable code and diagnostic; `modelVersion()` provenance; every `Event` scheduled as a direct effect of the command) rather than throwing on rejection or returning `void`. Rejection wraps the original, already-structured, sealed `SimError` (`SimError.OutOfRange` for an invalid quantity, `SimError.UnknownId` for a product/machine with no match, `SimError.InvalidStateTransition` for taking a busy machine offline -- all concretely reachable today) rather than re-deriving a parallel shape; see ADR-0007 for the full design, why an earlier revision of this gate over-claimed this criterion by relying on the exception-based contract alone, and why a further revision fixed a real partial-mutation bug independent review found in the first `CommandResult` implementation (a `SimTime` overflow discoverable only after `Order`/`Job`/`Machine` mutation had already started). Proven by `Gate3SessionControlAcceptanceTest.acceptedSubmissionReturnsAStructuredResultWithProvenanceAndScheduledEvents`, `.rejectedSubmissionReturnsAStructuredResultAndLeavesNoPartialMutation`, `.rejectedSubmissionFromAPostValidationSchedulingFailureStillLeavesNoPartialMutation`, `.machineAvailabilityCommandReturnsAStructuredResultForAcceptanceAndRejection`, and `.takingABusyMachineOfflineIsRejectedBeforeAnyMutation` -- asserting the actual result fields and post-rejection state rather than only exception-subtype/no-partial-mutation behavior in the easy cases.
3. **Advance exactly one event.** Unchanged: `FactoryRuntime.advance()`.
4. **Advance to a selected simulated time with a maximum event bound.** New: `FactoryRuntime.advanceUntil(SimTime targetTime, long maxEvents)`, implemented directly in terms of `advance()` so it cannot diverge from single-event dispatch semantics. Proven equivalent to looping `advance()` one event at a time -- both bounded to one event per call and unbounded in a single call -- by `Gate3SessionControlAcceptanceTest.advanceUntilBoundedToOneEventPerCallConvergesWithLoopingAdvance` and `.advanceUntilDrainingInOneUnboundedCallConvergesWithLoopingAdvance`; the time bound is proven independently by `.advanceUntilStopsAtTheTargetSimulatedTimeWithoutProcessingLaterEvents`.
5. **Inspect order, work-item, queue, and resource state.** The existing `ordersView`, `jobsView`, `job`, `machinesView`, `backlog`, `avgLeadTime`, `throughput`, `completedSalesValue`, `completedSales` projections (proven across the Gate 1/Gate 2 acceptance tests) do not, by themselves, expose all authoritative queue state under the current Gate 2 dispatch model: `FactoryHandler.pendingMultiEligible` (ADR-0005) is a second, cross-machine waiting-work structure not reflected in any `MachineView.queueDepth()`. New: `FactoryRuntime.pendingWorkView()` exposes it as read-only `PendingWorkView(JobId, Set<MachineId>)` entries. Proven by `Gate3SessionControlAcceptanceTest.pendingWorkViewExposesAMultiEligibleJobWaitingWhileBothEligibleMachinesAreOccupied`, which occupies both eligible machines, submits a third order, shows every machine's `queueDepth()` at zero while `pendingWorkView()` reports the waiting job and its eligible set, then shows the job dispatched and cleared from `pendingWorkView()` once a machine frees up.
6. **Reset and reproduce the same result.** New: `FactoryRuntime.reset()` returns a fresh `FactoryRuntime.forModel(modelVersion())`, leaving the original session untouched -- fresh construction rather than in-place mutation, since `FactoryHandler`'s stores have no partial-reset subsystem to mutate safely (see ADR-0007). Proven by `Gate3SessionControlAcceptanceTest.resetSessionReproducesIdenticalResultToTheOriginalSessionWithoutMutatingIt`, mirroring `Gate1EngineReadinessAcceptanceTest`'s two-fresh-runtimes determinism pattern: a reset session replaying the identical workload reproduces an identical ordered event stream and terminal state, and the original session's own state is unaffected by the `reset()` call.
7. **Identify the source model version throughout the session.** New: `FactoryRuntime` retains the exact `FactoryModelVersion` passed to `forModel` and exposes it via `modelVersion()`, unchanged for the session's lifetime. Proven by `Gate3SessionControlAcceptanceTest.runtimeRetainsAndExposesItsSourceModelVersionThroughoutTheSession`.
8. **Operate without Spring controllers, frontend DTOs, or mutable domain internals.** Unchanged: `FactoryRuntime` has no dependency on `interfaces/api` or `interfaces/web`, and every new method/type (`modelVersion()`, `advanceUntil`, `reset()`, `CommandResult`, `pendingWorkView()`/`PendingWorkView`) is a plain domain type or read-only projection, following the same shape as the rest of the class.

This slice deliberately does not migrate `interfaces/api`'s `SimThread` or `interfaces/cli`'s `SimRunner`/`HeadlessHandler` onto `FactoryRuntime`/`advanceUntil` -- both still duplicate their own bespoke max-time loop, which ADR-0007 records as accepted, separately tracked follow-up rather than silently left unrecorded. Neither called `submitWorkload`/`setMachineAvailability` either, so this slice's `CommandResult<T>` signature change touches no production code outside the `factory` module's own tests. It also does not add event envelopes/cursors (Gate 4), a new session-identity type, or a general command-result framework beyond the two commands that need one today -- no acceptance criterion above requires more.

With Gate 3 closed, **Gate 4 (stable observations and event envelopes, §8) is the next active gate.**

## 8. Gate 4 — Stable observations and event envelopes

### 8.1 Goal

Expose enough supported runtime information for a consumer to understand the simulation without reaching into internal stores or reconstructing authoritative state from undocumented events.

### 8.2 Minimum observation

The consumer-neutral observation should contain, directly or through purpose-specific projections:

```text
Session
    session/run ID
    model fingerprint
    model revision ID [optional/future]
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
    parent order
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

Not every consumer must receive one universal state dump. Purpose-specific observations remain preferable where they preserve capability boundaries.

### 8.3 Event envelope

Every externally visible runtime event should have an envelope equivalent to:

```text
sequence
simulation time
event type
session ID
model fingerprint
model revision ID [optional/future]
affected entity IDs
payload
```

The sequence is monotonic within one session and makes order explicit independently of event timestamps.

These are **simulation-runtime** events and observations. A future Operational Execution adapter may translate relevant production semantics into its own command/result and external-observation contracts, but Gate 4 does not define production telemetry envelopes, external source authenticity, or digital-twin reconciliation.

### 8.4 Acceptance criteria

Gate 4 is satisfied when:

1. Every externally visible runtime entity has a stable identifier.
2. Observation state and emitted events agree about the same authoritative transition history.
3. Event ordering is explicit and reproducible.
4. A consumer can identify the active bottleneck using supported observations rather than internal stores.
5. Requested, assigned, started, completed, and reported states are distinguishable.
6. Runtime observations include source-model provenance.
7. API/UI DTOs remain outward projections and are not reused as domain decision inputs.
8. A fresh observation can reconstruct current view state without replaying the entire history.

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

The gates are proven before a game client exists. Model variants are published through the design capability and instantiated as separate runtimes.

### 10.1 Capacity benchmark

```text
Product
    CUT -> ASSEMBLE -> INSPECT

Production order
    20 units

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
- model B dispatches work to both cutters;
- cutter queueing, throughput, utilization, or total completion time changes;
- if assembly becomes the new bottleneck, supported observations expose it;
- no product definition is rewritten to name the second cutter.

### 10.2 Layout benchmark

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
3. Make quantity consume proportional production work and allow multiple work items/jobs per order where required. **Implemented as the third Gate 1 slice**: a job's routing repeats once per unit of quantity (`totalSteps = routing.stepCount() * quantity`) rather than spawning multiple `Job`/`JobId` aggregates, giving a job-global executed-step counter (`Job.currentStep()`) from which per-unit progress can be derived, while keeping `Job` one-to-one with `Order`.
4. Capability/eligibility-driven deterministic dispatch.
5. Consumer-neutral session and bounded advancement. **Implemented as the Gate 3 slice** (`FactoryRuntime.modelVersion()`, `advanceUntil(SimTime, long)`, `reset()`, `submitWorkload`/`setMachineAvailability` returning `CommandResult<T>`, `pendingWorkView()`; see [ADR-0007](../architecture/decisions/0007-gate-3-session-control-primitives.md)).
6. Stable runtime observations and event envelopes.
7. Spatial transfer consequences from published layout.
8. Public-contract, recovery, persistence, and packaging hardening.

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
| Final aggregate/type boundaries for order and work execution | Gate 1 implementation |
| Unit work items versus capacity-consuming batches | Quantity prototype and expected scale |
| Capability pools versus explicit eligible-instance sets | Gate 2 scheduling/control requirements |
| Deterministic dispatch policy | First equivalent-resource benchmark |
| Session interface/module ownership | Gate 3 implementation |
| Tick/event-count advancement semantics | Interactive/headless responsiveness tests |
| Observation decomposition | First reference consumer and capability-boundary review |
| Spatial metric/transfer policy | Layout benchmark prototype |
| Public compatibility policy | Before external consumer contract publication |

The canonical model/run/runtime boundary is tracked by [ADR-0003](../architecture/decisions/0003-canonical-factory-model-boundary.md). Operational execution-context/trust/command/deployment/reconciliation decisions belong to the sibling operational track and should receive their own ADRs when hard-to-reverse contracts are selected. Record additional accepted Engine decisions as ADRs rather than expanding this plan into a decision log.

## 15. Documentation lifecycle

While this work is proposed, this file remains under `docs/planning/`.

As gates become implemented:

- update [`../architecture/overview.md`](../architecture/overview.md) with established runtime architecture;
- update [Factory Design Architecture](../architecture/factory-design.md) only as the model boundary becomes accepted/implemented;
- update the [ISA-95 semantic mapping](../architecture/isa-95-semantic-mapping.md) when manufacturing concepts change;
- update [`../reference/api.md`](../reference/api.md) only for implemented public behavior;
- add ADRs for durable execution, scheduling, session, persistence, and compatibility decisions;
- keep headless acceptance scenarios executable and version-controlled;
- keep production connectivity, deployment, external observation, reconciliation, and trust semantics in the sibling [Operational Execution and Digital Twin Readiness](operational-execution-digital-twin-readiness.md) track until they become implemented current architecture.

Once the readiness initiative is complete or abandoned, reduce this file to a concise historical outcome or retire it after durable decisions and current behavior are represented in authoritative locations.