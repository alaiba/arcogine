# PLAN-ENG-5 — Spatial Runtime Consequences Delivery Plan

Status: Active delivery plan. Implemented: PLAN-ENG-5-0, PLAN-ENG-5-A1, PLAN-ENG-5-A2, PLAN-ENG-5-B1, PLAN-ENG-5-C1 and PLAN-ENG-5-C2. Not started: PLAN-ENG-5-B2, PLAN-ENG-5-C3, PLAN-ENG-5-C4, PLAN-ENG-5-D and PLAN-ENG-5-E. Spatial execution is not implemented: a published model with a present spatial record is refused before any runtime state exists, and the transfer arithmetic and admission-reservation substrate stay unreachable from runtime execution until PLAN-ENG-5-C3.
Owner: Factory Simulation Engine Readiness
Parent plan: [Factory simulation engine readiness](factory-simulation-engine-readiness.md)

## 1. Purpose

PLAN-ENG-5 turns the Factory model's optional spatial record into deterministic transfer
consequences in the headless simulation runtime without moving design semantics into consumers or
inventing a transport network capability.

The architecture is fixed by:

- [Factory semantic evolution](../architecture/factory-design.md#111-semantic-evolution) and the
  [Factory model](../architecture/factory-model.md);
- [Determinism Contract](../architecture/overview.md#determinism-contract);
- [Engine semantics](../architecture/engine-semantics.md), whose §5–§13 specify how the current
  interpretation reads a present spatial record;
- the [current transfer-applicability boundary](../architecture/transfer-applicability.md);
- the runtime observation/event contract for supported observation/event state reconstruction and
  ordering.

### Current state

The [transfer-applicability boundary](../architecture/transfer-applicability.md) fixes the meaning of
each represented-content case: a model without a spatial record has no transfer lifecycle on
distinct-resource continuation, while present content — legal zero included — has the full lifecycle.
The current Engine executes only the first case and refuses present spatial content before runtime
mutation. Making the other cases executable is the remaining work of this plan; it changes which
content the Engine admits, not what the cases mean.

The Factory model and Engine interpretation are current development definitions under the
[semantic evolution rules](../architecture/overview.md#semantic-evolution-and-support). Development
status is not encoded as a Factory policy or Engine identity. Activating spatial execution therefore
implements already-specified rules of the current interpretation; any correction those rules need
while implementing is made in the Engine specification and its fixtures in the same change.

The concluded local-admission and shared-backlog-ranking research in
[Engine Evolution Research](../research/investigations/engine-evolution.md) retains the current
dispatch rules; PLAN-ENG-5-0 pins them, and their reservation-aware recheck applies at PLAN-ENG-5-C3.

## 2. PLAN-ENG-5 semantic boundary

PLAN-ENG-5 preserves the ownership split:

```text
Factory model optional spatial record, when present
    floor dimensions
    resource position + anchored footprint occupancy
    ticksPerCell
    handlingTicks

Engine semantics
    existing dispatch/decomposition/scheduler rules
    destination binding timing
    Manhattan distance on position reference cells
    transfer timing arithmetic
    same-resource / zero-distance behavior
    destination admission reservation and in-flight availability behavior

Runtime state
    transfer start / in-flight / completion
    supported transfer observations and events
```

No pathfinding, aisle/conveyor graph, transport-resource scheduling, congestion, rerouting,
authoritative animation coordinates, or resource orientation is part of PLAN-ENG-5.

## 3. Existing insertion seam

The implementation seam is the current next-step branch in `FactoryHandler.handleTaskEnd`.
Today that branch already:

1. determines the next routing step;
2. runs existing PLAN-ENG-2 selection at the established semantic point;
3. may select a machine that cannot currently accept the job because `canAcceptJob` is a ranking
   key rather than an eligibility filter;
4. starts immediately only when the selected resource can accept the job;
5. otherwise routes the job into the existing single-machine queue or `pendingMultiEligible` path.

PLAN-ENG-5 preserves the current selection/ranking and recovery semantics at that insertion point.
The concluded dispatch research deliberately retains those rules with qualifications; it does not
claim they are globally optimal. Any outcome-changing alternative is an explicit change to the
Engine specification and its fixtures, reconciled with this plan, never a silent implementation
drift.

## 4. Delivery policy

PLAN-ENG-5 implementation is deliberately split into small semantic increments so a coding agent can
execute a well-bounded change without being asked to rediscover architecture while coding.

Every slice should have:

- one dominant semantic invariant;
- explicit executable evidence that closes that invariant;
- narrow production ownership and explicit non-goals;
- an escalation rule: if implementation evidence conflicts with the Factory model, the Determinism
  Contract, or the Engine specification, stop and surface the contradiction rather than inventing new
  semantics.

This decomposition is provider-neutral. Repository architecture and acceptance evidence determine
whether a slice is sufficiently bounded; no particular model, agent, IDE, or hosted service is an
implementation dependency.

The vertical transfer-activation slice is intentionally not decomposed into separate state/event/
observation PRs. Once `TRANSFERRING` becomes reachable, supported state and supported deltas must be
coherent in the same landed change under the runtime observation/event contract.

## 5. Delivery slices

### Implemented slices

Their durable meaning lives in the owning specifications and their executable evidence in the named
tests; this plan keeps only what later slices need.

**PLAN-ENG-5-0 — Pin existing Engine semantics.** Implemented. The result-affecting behavior the
Engine interpretation inherits from the pre-spatial Engine — resource selection, local/shared
waiting and recovery ordering, shared-backlog ranking and reselection, child materialization,
scheduler ordering, and the derived-result arithmetic — is pinned by `EngineDispatchConformanceTest`,
`EngineDerivedResultConformanceTest`, the session-control acceptance suite, and the
child-materialization acceptance and benchmark tests, covering the fixture set in [Engine semantics
§14](../architecture/engine-semantics.md#14-conformance-fixtures). The reviewed dispatch proving
cases, their qualifications and reopening triggers live in [Engine Evolution
Research](../research/investigations/engine-evolution.md). The fixtures pin behavior, not a claim
that the retained rules are optimal; an outcome-changing alternative is an explicit specification
and fixture change, never an implementation edit.

**PLAN-ENG-5-A1 — Factory spatial record and validation.** Implemented. `FactoryModel` carries an
explicitly optional `SpatialRecord` with the publication predicates of the [Factory
model](../architecture/factory-model.md). Evidence: `SpatialRecordValidationTest` and
`SpatialRecordTest`.

**PLAN-ENG-5-A2 — Spatial content in the canonical form.** Implemented. The canonical form and its
one `factory-model:sha256:<digest>` fingerprint cover the spatial record, present or absent. Evidence:
`FactoryModelCanonicalFormTest` and `FactoryModelArtifactTest`.

**PLAN-ENG-5-B1 — Engine interpretation fixed per runtime.** Implemented. A `FactoryRuntime`
executes the current Engine definition for its lifetime without an Engine-definition identifier, and
refuses a published model with a present spatial record with `UnsupportedModelContentException`
before any runtime state exists. Evidence: `FactoryRuntimeExecutabilityAcceptanceTest`.

**PLAN-ENG-5-C1 — Pure transfer arithmetic.** Implemented. The package-private `TransferTiming`
computes the reference-cell Manhattan distance and transfer duration of [Engine semantics
§7](../architecture/engine-semantics.md#7-transfer-timing) with exact arithmetic that throws instead
of wrapping, and refuses a same-resource request. Nothing in the runtime calls it. Evidence:
`TransferTimingTest`.

**PLAN-ENG-5-C2 — Destination admission-reservation substrate.** Implemented. `Machine` keeps
inbound admission reservations in reservation order. A reservation counts toward `canAcceptJob()`
and the `startJob` capacity check, but is not an active job, not queue depth and not `Busy`, and it
converts either into active processing at an online destination or onto the tail of the
destination's own queue at an offline one. Nothing in the runtime calls it. Evidence:
`MachineInboundReservationTest`; the `only_factory_may_mutate_machine_state` rule in
`ArchitectureTest` covers the reservation mutators.

### PLAN-ENG-5-B2 — Runtime revision-provenance symmetry

**Status:** Not started.

**Prerequisite:** none beyond the existing supported runtime metadata contract.

**Responsibility**

- add the missing optional `ControlledRevisionId` to observation metadata when authoritatively
  revision-bound, preserving symmetry with event provenance;
- keep `RunId`, `ModelFingerprint` and revision provenance distinct;
- do not add a placeholder Engine-definition identifier. If a concrete supported consumer later
  requires exact Engine-definition provenance, that is admitted through the exact-reference boundary,
  not this spatial-plan slice.

**Evidence**

Fresh observations and every supported event carry the same model fingerprint and Engine
interpretation; revision-bound runtimes expose the same optional authoritative revision in
observations and events; reset creates a new `RunId` without changing the interpretation.

**Non-goals**

REST/SSE migration, transfer state/events, generic provenance framework, or any claim that current
development provenance is automatically durable across definition revisions.

**Convergence note:** outward consumer convergence is retired as a standing objective (see
[Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md#3-current-implementation-queue)); a future
outward adapter should consume this settled runtime provenance shape rather than introduce an envelope that
immediately needs revising.

### PLAN-ENG-5-C3 — Vertical transfer activation

**Status:** Not started.

**Prerequisites:** PLAN-ENG-5-B2, PLAN-ENG-5-C1 and PLAN-ENG-5-C2.

**Research recheck before activation:** inbound reservation changes what "can accept" means and was a
named reopening seam in both reviewed dispatch questions. Before making the reservation
substrate reachable, prove that reservation-aware admission does not create a new healthy state where
an online machine has free processing capacity plus residual local work, or where more than one
accepting candidate can carry shared-demand pressure in a way the retained conformance model did not
cover. If it does, stop and reopen the bounded local-admission/ranking research instead of silently
extending the existing rationale. This check is triggered when reservation-aware runtime
execution becomes reachable, not merely because spatial model facts exist.

**Responsibility**

Admit present spatial content in the Engine and activate the first coherent transfer path at the
existing `handleTaskEnd` next-step seam:

- replace the pre-mutation refusal of present spatial content with admission, in the same change
  that makes every admitted case coherent;
- preserve PLAN-ENG-2 selection timing/ranking and post-selection waiting paths;
- bind only when the selected destination is currently admissible;
- for a distinct resource under present spatial content, reserve admission capacity, compute/fix
  duration once, enter `TRANSFERRING`, and schedule completion; a model without a spatial record
  keeps the ordinary no-transfer dispatch path selected by the
  [current transfer boundary](../architecture/transfer-applicability.md);
- same-resource consecutive operations retain the no-transfer path;
- publish the minimum supported `TRANSFER_STARTED` / `TRANSFER_COMPLETED` deltas and expose the
  minimum in-flight observation facts in the same slice so no reachable authoritative state is
  invisible to the PLAN-ENG-4 contract;
- use only PLAN-ENG-4 sequence for same-time supported-event ordering.

Minimum in-flight observation facts are job/order correlation, source, bound destination,
`transferStartedAt`, `transferCompletesAt`, and resource admission load sufficient to keep resource
and job projections coherent.

**Evidence**

A happy-path distinct-resource job under present spatial content visibly and deterministically
transitions from step completion to in-flight state to arrival/next processing; source capacity
releases; destination admission capacity is held without active processing; a late observation during
the interval is self-consistent. Same-resource continuation and a spatial-absent distinct-resource
continuation create no transfer.

**Non-goals**

Every offline/zero-duration edge case, outward API DTO migration, KPI closure beyond minimum
projection coherence.

### PLAN-ENG-5-C4 — Transfer edge semantics

**Status:** Not started.

**Prerequisite:** PLAN-ENG-5-C3.

**Responsibility**

Close the remaining specified transfer rules without changing the core design:

- zero authored transfer magnitudes and zero-duration same-time ordering;
- immutable destination/no rerouting after transfer start;
- source going offline after departure has no effect;
- destination going offline in flight or at exact arrival does not change fixed completion time;
- arrival at an offline destination converts the inbound reservation into a queue entry on the bound
  destination, including for a multi-eligible authored step;
- another eligible destination becoming preferable does not cause rerouting.

**Evidence**

Focused tests prove the exact same-time sequence and all availability/no-rerouting cases, including
that the bound destination can actually become offline while only inbound-reserved work exists.

The multi-eligible arrival-offline case is the load-bearing one: it is the only state where a job
whose authored step listed several eligible machines waits in a per-machine queue, and it is what
makes immutable binding implementable rather than contradictory. A test must prove the job stays
bound after another eligible machine frees up and the recovery cascade runs, that it never appears
in `pendingMultiEligible`, and that it counts once — in destination queue depth, not additionally as
reserved admission capacity.

**Non-goals**

Transport resources, routing graphs, buffers, congestion, intermediate coordinates.

### PLAN-ENG-5-D — Observation, KPI and late-join closure

**Status:** Not started.

**Prerequisites:** PLAN-ENG-5-B2 and PLAN-ENG-5-C4.

**Responsibility**

Complete the supported read model around already-authoritative transfers:

- fresh observation reconstructs in-flight state without replay;
- job and destination resource projections agree at one sequence boundary;
- admission load/capacity is visible enough for a consumer to account for capacity held by inbound
  reservations;
- inbound reservation is not `activeJobIds`, queue depth, or processing `Busy`;
- backlog remains incomplete accepted orders;
- lead time naturally includes transfer delay;
- throughput remains completed orders per observed time;
- supported start/completion events and observations close over the same authoritative transitions.

**Evidence**

Late-join and observation/event-closure tests prove no internal store or scheduler-event replay is
required, and existing KPI meanings remain stable.

**Non-goals**

REST/SSE/UI projection, KPI redesign, retained supported-event history.

### PLAN-ENG-5-E — Headless PLAN-ENG-5 closure

**Status:** Not started.

**Prerequisites:** PLAN-ENG-5-B2, PLAN-ENG-5-C4 and PLAN-ENG-5-D.

**Responsibility**

Add one decisive consumer-neutral proving scenario and extend the Engine conformance fixtures with the
landed spatial behavior. Reconcile the parent PLAN-ENG-5 acceptance/status documentation without
adding presentation/API dependencies.

**Required proving case**

The scenario demonstrates:

1. two otherwise equivalent designs differing only in canonical placement produce different
   transfer duration/completion under the same Engine interpretation;
2. moving the resource changes `ModelFingerprint` while keeping stable resource identity;
3. repeated execution with identical model, interpretation, workload, seed and ordered commands
   produces identical ordered semantic outcomes;
4. a mid-transfer fresh observation reconstructs supported in-flight state and agrees with resource
   admission load without replay;
5. transfer start/completion ordering is deterministic, including zero-duration behavior;
6. the result carries source-design provenance (`ModelFingerprint`) without synthesizing an
   Engine-definition identifier; any future use that requires exact interpretation provenance must
   carry that basis explicitly;
7. a destination can become offline after binding, transfer completes at the fixed time, and the
   job waits on that bound destination without rerouting;
8. a design without a spatial record receives no synthesized spatial semantics;
9. the spatial conformance fixtures pin Engine semantics §14 items 7–11 and 13 as the executed scope.

## 6. Dependency and parallelism map

```text
Implemented: PLAN-ENG-5-0, PLAN-ENG-5-A1, PLAN-ENG-5-A2, PLAN-ENG-5-B1, PLAN-ENG-5-C1, PLAN-ENG-5-C2

PLAN-ENG-5-B2 revision-provenance symmetry ---------+
PLAN-ENG-5-C1 + PLAN-ENG-5-C2 (implemented) --------+--> PLAN-ENG-5-C3 activation
                                                                  |
                                                                  v
                                                          PLAN-ENG-5-C4 edges
                                                                  |
                                                                  v
                                                          PLAN-ENG-5-D closure   (also requires PLAN-ENG-5-B2)
                                                                  |
                                                                  v
                                                          PLAN-ENG-5-E
```

Practical sequencing:

- `PLAN-ENG-5-B2` is now the only unlanded slice prerequisite of `PLAN-ENG-5-C3`;
- `PLAN-ENG-5-C3` performs the reservation-aware dispatch recheck before activation;
- `PLAN-ENG-5-C1` and `PLAN-ENG-5-C2` stay unreachable from ordinary execution until `PLAN-ENG-5-C3`;
- work outside this chain may proceed under its own plan.

## 7. KPI acceptance

PLAN-ENG-5 preserves existing metric definitions:

- the Engine introduces no utilization or busy-time measure, and inbound reservation time is not
  reported as processing;
- queue depth does not count an in-flight reservation as a queued arrival;
- `activeJobIds` contains processing jobs, not inbound reserved jobs;
- backlog remains incomplete accepted orders;
- lead time naturally increases with transfer delay;
- throughput remains completed orders per observed time.

Supported resource observation must let a consumer account for admission capacity held by
transfer-bound work even while none of that work is processing. This is a projection extension, not a
redefinition of processing state or queue depth, and it makes neither admission load nor any occupancy
derivation a bottleneck verdict.

## 8. Cross-track consequences

### Challenge — REQUIRED WHEN CONSUMER INTEGRATES

When Challenge attempts consume real Engine-produced results, compatibility must account for the
producing Engine definition wherever changed semantics can affect compared outcomes. No current
Engine identifier establishes that compatibility. A concrete cross-revision comparison use must
follow [simulation analytics cross-revision comparability](../research/investigations/simulation-analytics-cross-revision-comparability.md), which in turn routes any required exact Engine reference or behavioral-equivalence claim to its existing research owner. Synthetic Challenge-only attempts do not block PLAN-ENG-5.

### Governance — REQUIRED WHEN CONSUMER INTEGRATES

Future Arcogine analytical evidence produced from simulation must retain `ModelFingerprint` and
the explicit producing inputs/results required by the Governance evidence contract. Governance
consumes this provenance; it does not own Engine semantics and must not synthesize an Engine
definition identifier. A future use that requires the exact producing Engine definition must carry
an explicitly owned/resolvable basis.

### Operational — REQUIRED WHEN CONSUMER INTEGRATES

Future twin/reconciliation analytics retain whatever exact Engine interpretation provenance their
own producer contract actually requires, independently of the durable operational identity and
subject correspondence. These answer different questions: which Engine basis produced a result;
which accountable operational continuation a record belongs to; and which external and Arcogine
subjects are authoritatively related. The Operational continuity contract is adopted and defines
that identity's referent and rules, while deliberately deferring its final type name and
representation; `RunId` remains Engine-owned and must never be derived from operational identity,
or operational identity from it.

### A future outward transport migration — SEQUENCE IF AND WHEN ONE IS INTRODUCED

If a future outward adapter migrates supported events and observations outward, it should land after
PLAN-ENG-5-B2's final runtime provenance shape, to avoid immediate wire-contract churn. No such migration is
currently a prerequisite for headless PLAN-ENG-5, since outward consumer convergence is retired as a standing
objective (see [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md#3-current-implementation-queue)).

## 9. Acceptance / readiness

The landed PLAN-ENG-5 conformance, Factory spatial record/validation/canonical-form, Engine
interpretation, transfer-arithmetic and reservation-substrate work are valid evidence under the
current architecture. The remaining transfer path is ready to proceed in dependency order; no
separate identity or applicability research gate remains. Until PLAN-ENG-5-C3 lands, present spatial
content stays refused rather than executed without its transfer semantics. Any slice that encounters
evidence contradicting the Factory model, the Engine specification or the transfer boundary must stop
and reconcile the owning specification rather than silently changing behavior in product code.
