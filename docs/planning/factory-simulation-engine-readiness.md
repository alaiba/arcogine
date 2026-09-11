# Factory Simulation Engine Readiness Implementation Plan

> **Status:** Active; workload/dispatch/session/work-decomposition and core observation/event semantics are complete, while outward consumer convergence and spatial runtime consequences remain admitted work  
> **Scope:** Implementation-ready work required to make Arcogine's deterministic factory runtime usable through stable consumer contracts  
> **Authority:** Planning only; result-affecting future policy questions live in research  
> **Related:** [Factory Design Capability](factory-design-capability.md), [ADR-0007](../architecture/decisions/0007-consumer-neutral-session-control-primitives.md), [ADR-0010](../architecture/decisions/0010-intra-order-execution-decomposition-and-work-item-identity.md), [ADR-0011](../architecture/decisions/0011-runtime-observation-and-event-contract.md), [ADR-0015](../architecture/decisions/0015-engine-semantics-identity-and-reproducibility.md), [Runtime Observation/Event Delivery](runtime-observation-event-delivery.md), [Spatial Runtime Consequences](spatial-runtime-consequences.md), [Engine Evolution Research](../research/engine-evolution.md)

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
- deterministic selection, waiting, and queue behavior has a current implementation baseline and an unreleased normative `engine-semantics:v1` contract;
- equivalent compatible resources can execute independent work.

PLAN-ENG-2's **capability boundary** remains complete: independently dispatchable work is selected and queued deterministically. That does not mean every current ranking/admission heuristic is now permanently frozen or was selected as an optimum. The two READY pre-release questions in [Engine Evolution Research](../research/engine-evolution.md) must be resolved before PLAN-ENG-5-0 turns the first Engine-semantics release into executable conformance evidence. No adjacent implementation slice may change those results silently.

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

**Pre-release dispatch gate:** PLAN-ENG-5-0 must not pin/release the current one-local-job-per-trigger rule or exact `combinedQueueDepth` ranking until the two READY questions in [Engine Evolution Research](../research/engine-evolution.md) are concluded and their surviving rules are reconciled into the v1 contract. Those questions do **not** block Factory V2 model/canonicalization work that does not depend on the contested dispatch interpretation. If the research retains current behavior, PLAN-ENG-5-0 pins it deliberately; if it changes either rule, this plan and the normative v1 specification must first be reconciled to the selected pre-release semantics.

No pathfinding, conveyor graph, transport-resource scheduling, congestion, rerouting, or orientation is part of this admitted work.

### PLAN-ENG-6 — Same-semantics shared-backlog performance

The dispatch-policy investigation established an implementation-efficiency problem independently of any scheduling-policy choice: per-submission resource ranking repeatedly scans `pendingMultiEligible`, producing quadratic admission work for a fixed eligible-set size and strongly superlinear diagnostic runtime as the shared backlog grows.

**Prerequisites:**

- the READY shared flexible-backlog ranking question in [Engine Evolution Research](../research/engine-evolution.md) is concluded and reconciled, so optimization targets the selected v1 ranking rather than today's provisional metric by accident;
- PLAN-ENG-5-0 has pinned executable conformance evidence for that selected ranking/waiting behavior.

Required outcome:

- reduce repeated full shared-backlog scans through indexing, cached compatible counts, or another bounded implementation technique without changing authoritative scheduling results;
- preserve exact selected-resource assignments, local FIFO order, shared-pending arrival/reselection behavior, same-time ordering, supported events/observations, and deterministic terminal state for identical explicit inputs;
- maintain exact arithmetic for the selected ranking term;
- add a deterministic result-equivalence fixture that compares representative local/shared waiting workloads before and after the optimization;
- add a reproducible non-functional benchmark over increasing flexible-backlog sizes and record the improvement without turning one machine/environment's elapsed milliseconds into a product contract.

If achieving the performance target requires different assignments, queue order, backlog semantics, or observable results, stop and return to research: that is dispatch-policy evolution, not PLAN-ENG-6.

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

Within PLAN-ENG-5, the two READY pre-release dispatch questions gate **PLAN-ENG-5-0 and the first Engine-semantics release**, not unrelated Factory V2 authored/canonical model work. After the selected dispatch rules are reconciled and PLAN-ENG-5-0 pins them, PLAN-ENG-6 may proceed as an independent same-semantics performance lane.

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

Candidate extensions whose contract is not yet selected remain in [Engine Evolution Research](../research/engine-evolution.md).

## 7. Research boundary

The following are not implementation items in this plan:

- lot/batch/material-lot semantics;
- generalized capability requirements or resource pools/work centers;
- the two READY pre-release dispatch questions: local admission when one trigger exposes several free slots, and flexible-resource ranking under overlapping shared backlog;
- queue sequencing/policy evolution without a concrete supported-consumer objective;
- new advancement/session semantics without a concrete consumer failure case; and
- unselected transport/recovery technology or protocol choices.

`engine-semantics:v1` is not yet a released semantics version. A research-backed result may therefore justify an explicit pre-release v1 reconsideration, but it must be reconciled into architecture/specification before implementation; it is not an ordinary implementation tweak. After a semantics version is released, an outcome-changing revision for identical explicit inputs requires a new `EngineSemanticsVersion` under ADR-0015.

Do not add delivery coordinates for unresolved semantic questions. PLAN-ENG-6 is admitted separately because its implementation contract is exact result equivalence under whichever ranking semantics the pre-release research selects.

## 8. Validation policy

Each admitted slice must:

1. start from current `main` and preserve accepted predecessor behavior;
2. use executable deterministic tests for every result-affecting invariant;
3. avoid using a UI or protocol adapter as evidence for core Engine semantics;
4. keep internal scheduler machinery separate from supported consumer contracts; and
5. reconcile implementation status in this plan and the detailed delivery companion when work lands.
