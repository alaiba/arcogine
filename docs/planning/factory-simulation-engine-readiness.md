# Factory Simulation Engine Readiness Implementation Plan

> **Status:** Active; workload/dispatch/session/work-decomposition and core observation/event semantics are complete, while outward consumer convergence and spatial runtime consequences remain admitted work  
> **Scope:** Implementation-ready work required to make Arcogine's deterministic factory runtime usable through stable consumer contracts  
> **Authority:** Planning only; result-affecting future policy questions live in research  
> **Related:** [Factory Design Capability](factory-design-capability.md), [ADR-0007](../architecture/decisions/0007-consumer-neutral-session-control-primitives.md), [ADR-0010](../architecture/decisions/0010-intra-order-execution-decomposition-and-work-item-identity.md), [ADR-0011](../architecture/decisions/0011-runtime-observation-and-event-contract.md), [ADR-0015](../architecture/decisions/0015-engine-semantics-identity-and-reproducibility.md), [Runtime Observation/Event Delivery](runtime-observation-event-delivery.md), [Spatial Runtime Consequences](spatial-runtime-consequences.md), [Engine Evolution Research](../research/investigations/engine-evolution.md)

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
- workload can be submitted without economy/demand/agents;
- quantity consumes proportional production work;
- economy-driven and explicit submission share the same authoritative acceptance path.

### PLAN-ENG-2 — Deterministic resource dispatch

- explicit eligible resource instances define current eligibility;
- operational availability/queue state remain distinct from eligibility;
- deterministic selection, waiting, and queue behavior has a current implementation baseline and a normative `engine-semantics:v1` contract;
- equivalent compatible resources can execute independent work.

PLAN-ENG-2's **capability boundary** remains complete: independently dispatchable work is selected and queued deterministically. The two first-release research questions have now concluded after independent adversarial review, and both retain the existing v1 local-admission and shared-backlog-ranking rules with qualifications. PLAN-ENG-5-0 may therefore pin those rules deliberately; adjacent implementation still must not reinterpret them as optimality claims or change results silently.

### PLAN-ENG-3 — Consumer-neutral simulation session

- runtime owns its scheduler/session state;
- bounded advancement/reset semantics are consumer-neutral;
- callers do not inject authoritative simulated time or scheduler ownership.

### PLAN-ENG-W1 — Intra-order work decomposition

ADR-0010 is implemented:

- one quantity-bearing accepted `Order` remains aggregate intent/correlation;
- quantity creates deterministic unit-quantity sibling `Job`s;
- `JobId` is independently dispatchable work-item identity;
- aggregate progress/completion remains order-level;
- exactly one aggregate completion fact is emitted;
- the supported 100,000-child ceiling has executable benchmark evidence.

### PLAN-ENG-4-A/B/C — Supported observation/event core

The consumer-neutral runtime now provides:

- opaque per-run `RunId` correlation;
- durable source `ModelFingerprint` provenance;
- supported current-state observation;
- ordered supported runtime events distinct from internal scheduler `Event`s;
- post-authoritative event publication;
- stable order/work correlation;
- a fresh observation sufficient to reconstruct supported consumer state without replay.

## 3. Current implementation queue

Only the following Engine work is currently admitted.

### PLAN-ENG-4-D — Outward consumer convergence

Use [Runtime Observation/Event Delivery](runtime-observation-event-delivery.md) as the detailed implementation companion.

Required outcome:

- legacy API/SSE projects supported runtime semantics rather than internal scheduler events;
- outward DTOs remain projections and never re-enter domain decision paths;
- frontend consumption follows the supported envelope/observation boundary;
- CLI/reference/headless paths either consume the supported runtime contract directly or document a deliberate broader orchestration adapter;
- current-state API/reference documentation changes only with shipped behavior.

The API/SSE and CLI/reference changes may land separately when that keeps reviews narrow.

### PLAN-ENG-5 — Spatial runtime consequences

Use [Spatial Runtime Consequences](spatial-runtime-consequences.md) as the detailed implementation plan. ADR-0014, ADR-0015, Factory Model v2 canonicalization, and Engine Semantics v1 define the accepted contract.

The admitted sequence includes:

1. pin pre-existing result-affecting Engine semantics and required arithmetic corrections;
2. implement Factory Model v2 spatial facts/validation and canonical identity;
3. establish `EngineSemanticsVersion` and propagate required runtime provenance;
4. implement deterministic transfer arithmetic and inbound admission reservation;
5. activate coherent transfer state/events/observations;
6. close availability/no-rerouting edge semantics and late-join diagnostics; and
7. complete V1/V2 historical coexistence before final closure.

**First-release dispatch gate:** cleared. The two critical-path research questions in [Engine Evolution Research](../research/investigations/engine-evolution.md) are now concluded with `engine-semantics:v1` retained unchanged after adversarial review. PLAN-ENG-5-0 is therefore ready to pin the existing local-admission, shared-backlog-ranking, reselection, ordering, and exact-arithmetic rules together with the reviewed discriminating cases. The research conclusions do not authorize a policy change and do not claim the retained rules are globally optimal.

No pathfinding, conveyor graph, transport-resource scheduling, congestion, rerouting, or orientation is part of this admitted work.

### PLAN-ENG-6 — Same-semantics shared-backlog performance

The dispatch-policy investigation established an implementation-efficiency problem independently of any scheduling-policy choice: per-submission resource ranking repeatedly scans `pendingMultiEligible`, producing quadratic admission work for a fixed eligible-set size and strongly superlinear diagnostic runtime as the shared backlog grows.

**Prerequisites:**

- PLAN-ENG-5-0 has pinned the authoritative v1 ranking/waiting contract and the reviewed local/shared conformance cases;
- the targeted implementation preserves exact results for `engine-semantics:v1`, including exact `combinedQueueDepth` arithmetic and the retained recovery/local-admission coupling.

Required outcome:

- reduce repeated full shared-backlog scans through indexing, cached compatible counts, or another bounded implementation technique without changing authoritative scheduling results for the targeted semantics version;
- preserve exact selected-resource assignments, local FIFO order, shared-pending arrival/reselection behavior, same-time ordering, supported events/observations, and deterministic terminal state for identical explicit inputs;
- maintain exact arithmetic for the targeted version's ranking term;
- add a deterministic result-equivalence fixture that compares representative local/shared waiting workloads before and after the optimization;
- add a reproducible non-functional benchmark over increasing flexible-backlog sizes and record the improvement without turning one machine/environment's elapsed milliseconds into a product contract.

If achieving the performance target requires different assignments, queue order, backlog semantics, or observable results, stop and return to research: that is Engine-semantics evolution, not PLAN-ENG-6.

PLAN-ENG-6 is admitted performance hardening, not a prerequisite for outward convergence or spatial semantics. Schedule it after its semantic/conformance prerequisites when it does not compete with higher-priority Engine work.

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
PLAN-ENG-4 A/B/C complete
    |\
    | +--> PLAN-ENG-4-D outward convergence
    |
    +----> PLAN-ENG-5 spatial consequences
```

Within PLAN-ENG-5, the first-release dispatch research gate is now cleared: both reviewed questions retain v1, so PLAN-ENG-5-0 may pin the existing rules and their coupled recovery/ranking corner. PLAN-ENG-6 may target v1 only after that conformance evidence lands. Factory V2 authored/canonical model work remains independently sequenced as described in the detailed plan.

PLAN-ENG-4 core closure no longer blocks spatial work. Outward convergence should consume settled provenance from the spatial/Engine-semantics work where the detailed delivery plans require it rather than migrating an envelope that is immediately revised.

## 5. Determinism and provenance invariants

For identical explicit inputs, deterministic simulation semantics are scoped to:

- the same published semantic model;
- the same `EngineSemanticsVersion`;
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

Current architecture assigns the retained dispatch rules to `engine-semantics:v1`. The concluded first-release research authorizes only deliberate conformance to those rules. A future outcome-changing revision still requires a new `EngineSemanticsVersion`; planning must not create a pre-release or optimization exception to that architecture contract.

PLAN-ENG-6 is admitted separately because its implementation contract is exact result equivalence for the already-authoritative v1 semantics after PLAN-ENG-5-0 closes the conformance gap.

## 8. Validation policy

Each admitted slice must:

1. start from current `main` and preserve accepted predecessor behavior;
2. use executable deterministic tests for every result-affecting invariant;
3. avoid using a UI or protocol adapter as evidence for core Engine semantics;
4. keep internal scheduler machinery separate from supported consumer contracts; and
5. reconcile implementation status in this plan and the detailed delivery companion when work lands.
