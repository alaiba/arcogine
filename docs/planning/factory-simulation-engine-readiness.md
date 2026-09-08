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
- deterministic ranking/tie-breaking is fixed by current Engine semantics;
- equivalent compatible resources can execute independent work.

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

No pathfinding, conveyor graph, transport-resource scheduling, congestion, rerouting, or orientation is part of this admitted work.

## 4. Dependency order

```text
completed model seam
    |
    v
PLAN-ENG-1 complete
    |
PLAN-ENG-2 complete
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
- dispatch policy evolution beyond the accepted current Engine semantics;
- new advancement/session semantics without a concrete consumer failure case; and
- unselected transport/recovery technology or protocol choices.

Do not add delivery coordinates for these questions until research produces a concrete semantic contract and deterministic acceptance evidence.

## 8. Validation policy

Each admitted slice must:

1. start from current `main` and preserve accepted predecessor behavior;
2. use executable deterministic tests for every result-affecting invariant;
3. avoid using a UI or protocol adapter as evidence for core Engine semantics;
4. keep internal scheduler machinery separate from supported consumer contracts; and
5. reconcile implementation status in this plan and the detailed delivery companion when work lands.
