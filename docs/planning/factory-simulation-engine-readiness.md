# Factory Simulation Engine Readiness Implementation Plan

> **Status:** Active. The runtime core (PLAN-ENG-1 through PLAN-ENG-4) is landed. Admitted: the remaining PLAN-ENG-5 spatial-runtime slices, tracked in [Spatial Runtime Consequences](spatial-runtime-consequences.md), and PLAN-ENG-6, not started. Outward consumer convergence is retired as an objective — a future outward consumer is demand-triggered, not standing backlog.  
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

The following Engine capability is landed and must not be reopened by adjacent work. Its durable meaning lives in the owning contracts, and its executable evidence in the Engine conformance and acceptance suites.

| Slice | Landed capability | Owning contract |
| --- | --- | --- |
| PLAN-ENG-1 | immutable `Order` intent separate from mutable execution; explicit workload submission through one authoritative acceptance path | [Engine semantics §3](../architecture/engine-semantics.md#3-unit-work-decomposition-semantics) |
| PLAN-ENG-2 | deterministic selection, waiting and queue behavior over explicit eligible resource instances | [Engine semantics §2 and §4](../architecture/engine-semantics.md#2-resource-selection-and-dispatch-semantics) |
| PLAN-ENG-3 | consumer-neutral session: runtime-owned scheduler and bounded advancement/reset | [Engine semantics §1.2](../architecture/engine-semantics.md#12-session-and-control-semantics) |
| PLAN-ENG-W1 | unit-work decomposition into sibling `Job`s with one aggregate completion and a 100,000-child envelope | [Engine semantics §3](../architecture/engine-semantics.md#3-unit-work-decomposition-semantics) |
| PLAN-ENG-4-A/B/C | `RunId` correlation, `ModelFingerprint` provenance, supported observation and ordered supported runtime events | [Runtime contract](../architecture/runtime-contract.md) |

The concluded dispatch research retains the local-admission and shared-backlog-ranking rules; PLAN-ENG-5-0 pins them as behavior, not as an optimality claim. The landed PLAN-ENG-5 slices are listed in [Spatial Runtime Consequences](spatial-runtime-consequences.md#implemented-slices).

## 3. Current implementation queue

Only the following Engine work is currently admitted.

Outward consumer convergence is no longer an outstanding Engine objective. The legacy API/SSE/CLI adapters were retired rather than migrated to the supported runtime contract; a future outward consumer will be introduced from that contract when a concrete product need exists, scoped at that time rather than carried here as standing backlog.

The [concluded analytics ownership boundary](../research/investigations/simulation-analytics-consumer-boundary.md) admits no shared analytics capability, new Engine metric or runtime work: existing Engine-declared results remain Engine-owned, named methods belong to their choosing consumer by default, and no analytics path may derive supported semantics from scheduler internals.

### PLAN-ENG-5 — Spatial runtime consequences

**Status:** Admitted; partially landed.

Use [Spatial Runtime Consequences](spatial-runtime-consequences.md) as the detailed implementation plan. The Factory semantic-evolution contract, the Determinism Contract, the [Factory model](../architecture/factory-model.md), [Engine semantics](../architecture/engine-semantics.md), and the [current transfer-applicability boundary](../architecture/transfer-applicability.md) remain governing contracts. The current Engine refuses present spatial content until spatial execution is implemented; the remaining slices make it executable without any successor Factory policy or Engine identity.

The admitted sequence is:

1. pin pre-existing result-affecting Engine semantics and required arithmetic corrections — complete;
2. implement Factory spatial facts, validation and canonical form — complete;
3. establish the Engine interpretation per runtime — complete; revision-provenance symmetry remains;
4. implement deterministic transfer arithmetic and inbound admission reservation — complete;
5. activate coherent transfer state/events/observations;
6. close availability/no-rerouting edge semantics and late-join diagnostics.

No pathfinding, conveyor graph, transport-resource scheduling, congestion, rerouting, or orientation is part of this admitted work.

### PLAN-ENG-6 — Same-semantics shared-backlog performance

**Status:** Admitted; not started. Its prerequisites are landed.

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
PLAN-ENG-1 .. PLAN-ENG-4 (landed)
    |
    v
PLAN-ENG-5 spatial consequences (partially landed; see the detailed plan)

PLAN-ENG-6 shared-backlog performance (independent; prerequisites landed)
```

Within PLAN-ENG-5, the remaining transfer slices proceed in the dependency order of the detailed plan, with the reservation-aware dispatch recheck before activation.

## 5. Determinism and provenance invariants

For identical explicit inputs, deterministic simulation semantics are scoped to:

- the same published semantic model;
- the same current Engine definition;
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

Current architecture assigns the retained dispatch rules to the [Engine semantics](../architecture/engine-semantics.md). The concluded dispatch research authorizes deliberate conformance to those rules, and no implementation task may silently change them; an intended change is an explicit specification and fixture change. The Engine interpretation remains a current development definition under the [semantic evolution and support rules](../architecture/overview.md#semantic-evolution-and-support); planning itself must not create an implicit identity, support commitment, or exception to those rules.

PLAN-ENG-6 is admitted separately because its implementation contract is exact result equivalence for the current, already-pinned semantics.

## 8. Validation policy

Each admitted slice must:

1. start from current `main` and preserve accepted predecessor behavior;
2. use executable deterministic tests for every result-affecting invariant;
3. avoid using a UI or protocol adapter as evidence for core Engine semantics;
4. keep internal scheduler machinery separate from supported consumer contracts; and
5. reconcile implementation status in this plan and the detailed delivery companion when work lands.
