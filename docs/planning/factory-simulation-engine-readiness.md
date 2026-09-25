# Factory Simulation Engine Readiness Implementation Plan

> **Status:** Active; workload/dispatch/session/work-decomposition, core observation/event semantics, PLAN-ENG-5-0 Engine conformance, and a runtime that fixes one work-in-progress Engine interpretation are complete. Outward consumer convergence is retired as an objective — a future consumer is demand-triggered, not standing backlog. The Factory spatial record and its canonical form are implemented; spatial execution is not, and a model with a present spatial record is refused before any runtime state exists. The remaining transfer slices proceed in dependency order.
> **Scope:** Implementation-ready work required to make Arcogine's deterministic factory runtime usable through stable consumer contracts  
> **Authority:** Planning only; result-affecting future policy questions live in research  
> **Related:** [Factory Design Capability](factory-design-capability.md), [session-control semantics](../architecture/engine-semantics.md#12-session-and-control-semantics), [unit-work decomposition semantics](../architecture/engine-semantics.md#3-unit-work-decomposition-semantics), [runtime observation/event contract](../architecture/runtime-contract.md), [Determinism Contract](../architecture/overview.md#determinism-contract), [Spatial Runtime Consequences](spatial-runtime-consequences.md), [Transfer Semantics Boundary Research](../research/investigations/transfer-semantics.md), [Engine Evolution Research](../research/investigations/engine-evolution.md), [Semantic Contract Maturity and Durability Research](../research/investigations/semantic-contract-maturity-durability.md), [Factory Model Semantic Composition Research](../research/investigations/factory-model-semantic-composition.md)

## 1. Runtime boundary

The runtime consumes one published factory model and owns deterministic execution consequences:

```text
Published FactoryModelVersion
        |
        v
FactoryRuntime
        |
        +--> explicit workload
        +--> child work items
        +--> deterministic resource selection
        +--> queues / processing / transfers
        +--> bounded advancement
        +--> supported observations/events
```

The runtime does not author Factory semantics, Governance history, game scoring, or production-control trust/reconciliation semantics.

## 2. Current landed baseline

The following Engine capability is complete and must not be reopened by adjacent work:

### PLAN-ENG-1 — Explicit workload and execution

- immutable `Order` intent is separate from mutable execution;
- workload is submitted explicitly, independent of any demand or pricing model;
- quantity consumes proportional production work;
- internal `OrderCreation` events and explicit workload submission share the same authoritative acceptance path.

### PLAN-ENG-2 — Deterministic resource dispatch

- explicit eligible resource instances define current eligibility;
- operational availability/queue state remain distinct from eligibility;
- deterministic selection, waiting, and queue behavior has a current implementation baseline and a normative [Engine semantics](../architecture/engine-semantics.md) contract;
- equivalent compatible resources can execute independent work.

PLAN-ENG-2's **capability boundary** remains complete: independently dispatchable work is selected and queued deterministically. The two first-release research questions have now concluded after independent adversarial review, and both retain the existing local-admission and shared-backlog-ranking rules with qualifications. PLAN-ENG-5-0 has pinned those rules deliberately; adjacent implementation still must not reinterpret them as optimality claims or change results silently.

### PLAN-ENG-3 — Consumer-neutral simulation session

- runtime owns its scheduler/session state;
- bounded advancement/reset semantics are consumer-neutral;
- callers do not inject authoritative simulated time or scheduler ownership.

### PLAN-ENG-W1 — Intra-order work decomposition

The unit-work decomposition semantics is implemented:

- one quantity-bearing accepted `Order` remains aggregate intent/correlation;
- quantity creates deterministic unit-quantity sibling `Job`s;
- `JobId` is independently dispatchable work-item identity;
- aggregate progress/completion remains order-level;
- exactly one aggregate completion fact is emitted;
- the supported 100,000-child ceiling has executable benchmark evidence.

### PLAN-ENG-4-A/B/C — Supported observation/event core

The consumer-neutral runtime now provides:

- opaque per-run `RunId` correlation;
- source `ModelFingerprint` provenance;
- supported current-state observation;
- ordered supported runtime events distinct from internal scheduler `Event`s;
- post-authoritative event publication;
- stable order/work correlation;
- a fresh observation sufficient to reconstruct supported consumer state without replay.

### PLAN-ENG-5-0 — Engine conformance

The retained pre-spatial Engine interpretation is now explicitly pinned by deterministic factory
conformance evidence. This includes resource selection, local/shared waiting and recovery ordering,
shared-backlog ranking and reselection, child materialization boundaries, scheduler ordering, and
the accepted derived-result arithmetic. The implementation also uses a wide exact
`combinedQueueDepth` ranking quantity and saturating mean-lead-time accumulation as the
[Engine semantics](../architecture/engine-semantics.md) require.

### Engine interpretation fixed per runtime

The runtime establishes one `EngineSemantics` for every fresh and reset session. The supported
value is the work-in-progress marker `engine-semantics:wip`; it is distinct from `ModelFingerprint`,
`RunId`, and build identity, it names the current build's definition rather than an identity spanning
development revisions, and any other name fails explicitly. Runtime observation/event provenance
propagation remains separate admitted work and is not implied by this capability.

## 3. Current implementation queue

Only the following Engine work is currently admitted.

Outward consumer convergence is no longer an outstanding Engine objective. PLAN-ENG-4-A/B/C (runtime identity/supported observation, the supported `RuntimeEvent` contract, and headless acceptance closure) are the completed supported runtime observation/event capability — see [runtime observation/event contract](../architecture/runtime-contract.md) and [Architecture Overview](../architecture/overview.md#event-dispatch-architecture). The product decision is no longer to migrate the retired legacy API/SSE/CLI toward this contract; those adapters were retired outright rather than migrated. A future outward consumer will be introduced from this supported contract when a concrete product need exists, scoped at that time rather than carried here as standing backlog.

The former generic KPI implementation derived values from internal scheduler events and simulation time, outside the supported runtime contract; that implementation has been removed. Any future analytical capability remains unresolved and must follow [Simulation analytics consumer boundary](../research/investigations/simulation-analytics-consumer-boundary.md), rather than deriving supported semantics from scheduler internals.

### PLAN-ENG-5 — Spatial runtime consequences

Use [Spatial Runtime Consequences](spatial-runtime-consequences.md) as the detailed implementation plan. The Factory semantic-evolution contract, the Determinism Contract, the [Factory model](../architecture/factory-model.md), [Engine semantics](../architecture/engine-semantics.md), and the [current transfer-applicability boundary](../architecture/transfer-applicability.md) remain governing contracts. The work-in-progress Factory model carries an optional spatial record under one canonical form; the transfer boundary selects no transfer for a model without that record while preserving authored zero and refusal as distinct. The current Engine refuses present spatial content until spatial execution is implemented; the remaining slices make it executable without any successor Factory policy or Engine identity.

The admitted sequence includes:

1. pin pre-existing result-affecting Engine semantics and required arithmetic corrections — complete;
2. implement Factory spatial facts, validation and canonical form — complete;
3. establish the Engine interpretation per runtime — complete; provenance propagation remains;
4. implement deterministic transfer arithmetic and inbound admission reservation;
5. activate coherent transfer state/events/observations;
6. close availability/no-rerouting edge semantics and late-join diagnostics.

**First-release dispatch gate:** cleared. The two critical-path research questions in [Engine Evolution Research](../research/investigations/engine-evolution.md) are now concluded with the current rules retained unchanged after adversarial review. PLAN-ENG-5-0 has pinned the existing local-admission, shared-backlog-ranking, reselection, ordering, and exact-arithmetic rules together with the reviewed discriminating cases. The research conclusions do not authorize a policy change and do not claim the retained rules are globally optimal.

No pathfinding, conveyor graph, transport-resource scheduling, congestion, rerouting, or orientation is part of this admitted work.

### PLAN-ENG-6 — Same-semantics shared-backlog performance

The dispatch-policy investigation established an implementation-efficiency problem independently of any scheduling-policy choice: per-submission resource ranking repeatedly scans `pendingMultiEligible`, producing quadratic admission work for a fixed eligible-set size and strongly superlinear diagnostic runtime as the shared backlog grows.

**Prerequisites:**

- PLAN-ENG-5-0 has pinned the authoritative ranking/waiting contract and the reviewed local/shared conformance cases;
- the targeted implementation preserves exact results for the current interpretation, including exact `combinedQueueDepth` arithmetic and the retained recovery/local-admission coupling.

Required outcome:

- reduce repeated full shared-backlog scans through indexing, cached compatible counts, or another bounded implementation technique without changing authoritative scheduling results for the current interpretation;
- preserve exact selected-resource assignments, local FIFO order, shared-pending arrival/reselection behavior, same-time ordering, supported events/observations, and deterministic terminal state for identical explicit inputs;
- maintain exact arithmetic for the ranking term;
- add a deterministic result-equivalence fixture that compares representative local/shared waiting workloads before and after the optimization;
- add a reproducible non-functional benchmark over increasing flexible-backlog sizes and record the improvement without turning one machine/environment's elapsed milliseconds into a product contract.

If achieving the performance target requires different assignments, queue order, backlog semantics, or observable results, stop and return to research: that is Engine-semantics evolution, not PLAN-ENG-6.

PLAN-ENG-6 is admitted performance hardening, not a prerequisite for spatial semantics or for a future outward consumer. Schedule it after its semantic/conformance prerequisites when it does not compete with higher-priority Engine work.

## 4. Dependency order

```text
completed model seam
    |
    v
PLAN-ENG-1 complete
    |
PLAN-ENG-2 capability complete
    |
PLAN-ENG-3 complete
    |
PLAN-ENG-W1 complete
    |
PLAN-ENG-4 A/B/C complete (outward consumer convergence retired as an objective)
    |
PLAN-ENG-5 spatial consequences
```

Within PLAN-ENG-5, the earlier first-release dispatch research gate is cleared and PLAN-ENG-5-0 is implemented: the existing rules and their coupled recovery/ranking corner are pinned executably, and the Engine interpretation is fixed per runtime. The Factory spatial record, its validation and its canonical form are implemented. The remaining transfer slices proceed in the dependency order of the detailed plan, with the reservation-aware dispatch recheck before activation. PLAN-ENG-6 may proceed independently.

PLAN-ENG-4 core closure is no longer a prerequisite blocker in its own right.

## 5. Determinism and provenance invariants

For identical explicit inputs, deterministic simulation semantics are scoped to:

- the same published semantic model;
- the same Engine interpretation (`EngineSemantics`, under one definition);
- the same workload/commands;
- the same seed/random inputs; and
- any other explicit result-affecting input introduced by accepted semantics.

`RunId` is correlation only and must not affect outcomes. `ControlledRevisionId` is optional provenance only when the runtime has an authoritative upstream revision binding; Engine never synthesizes one.

Internal scheduler events, supported runtime events, model revision history, Governance evidence/decision history, challenge attempt history, and future Operational command/observation history remain distinct histories.

## 6. Distribution hardening

Recovery/versioning/checkpoint/packaging work is not a core blocker for spatial implementation. It may be admitted after the supported outward contract is stable and its concrete compatibility requirements are selected.

The currently established recovery invariant is:

- supported event sequence is monotonic within a run;
- a fresh observation is authoritative current state;
- silent history truncation must never be reported as successful resume;
- exact checkpoint/restore, when implemented, must preserve the source model, Engine semantics, simulated time, runtime state, scheduler/random state, and event position required for exact continuation.

Candidate extensions whose contract is not yet selected remain in [Engine Evolution Research](../research/investigations/engine-evolution.md).

## 7. Research boundary

The following are not implementation items in this plan:

- lot/batch/material-lot semantics;
- generalized capability requirements or resource pools/work centers;
- any future outcome-changing revision of the now-retained local-admission or shared-backlog-ranking rules, including a reopening caused by a new scheduling objective, changed admission/reservation mechanics, or representative consumer evidence;
- queue sequencing/policy evolution without a concrete supported-consumer objective;
- new advancement/session semantics without a concrete consumer failure case; and
- unselected transport/recovery technology or protocol choices.

Current architecture assigns the retained dispatch rules to the [Engine semantics](../architecture/engine-semantics.md). The concluded first-release research authorizes deliberate conformance to those rules, and no implementation task may silently change them; an intended change is an explicit specification and fixture change. The Engine interpretation is work in progress under the [semantic evolution and support rules](../architecture/overview.md#semantic-evolution-and-support); planning itself must not create an exception to them in either direction.

PLAN-ENG-6 is admitted separately because its implementation contract is exact result equivalence for the current, already-pinned semantics.

## 8. Validation policy

Each admitted slice must:

1. start from current `main` and preserve accepted predecessor behavior;
2. use executable deterministic tests for every result-affecting invariant;
3. avoid using a UI or protocol adapter as evidence for core Engine semantics;
4. keep internal scheduler machinery separate from supported consumer contracts; and
5. reconcile implementation status in this plan and the detailed delivery companion when work lands.
