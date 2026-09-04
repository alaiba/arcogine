# Gate 5 — Spatial Runtime Consequences Delivery Plan

Status: Proposed delivery plan; architecture fixed by ADR-0014 / ADR-0015, implementation not yet started
Owner: Factory Simulation Engine Readiness
Parent plan: [Factory simulation engine readiness](factory-simulation-engine-readiness.md)

## 1. Purpose

Gate 5 turns canonical Factory V2 spatial facts into deterministic transfer consequences in the
headless simulation runtime without moving design semantics into consumers or inventing a transport
network capability.

The architecture is fixed by:

- [ADR-0014 — Factory Model Semantic-Policy Evolution](../architecture/decisions/0014-factory-model-semantic-policy-evolution.md);
- [ADR-0015 — Engine Semantics Identity and Reproducibility](../architecture/decisions/0015-engine-semantics-identity-and-reproducibility.md);
- [Engine Semantics v1](../architecture/engine-semantics-v1.md), the normative first-version Engine interpretation;
- Accepted ADR-0011 for supported observation/event state reconstruction and ordering.

Implementation must not begin from this plan until ADR-0014 and ADR-0015 are landed as Accepted.

## 2. Gate 5 semantic boundary

Gate 5 preserves the ownership split:

```text
Factory model v2
    floor dimensions
    resource position + anchored footprint occupancy
    ticksPerCell
    handlingTicks

Engine semantics v1
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
authoritative animation coordinates, or resource orientation is part of Gate 5.

## 3. Existing insertion seam

The implementation seam is the current next-step branch in `FactoryHandler.handleTaskEnd`.
Today that branch already:

1. determines the next routing step;
2. runs existing Gate 2 selection at the established semantic point;
3. may select a machine that cannot currently accept the job because `canAcceptJob` is a ranking
   key rather than an eligibility filter;
4. starts immediately only when the selected resource can accept the job;
5. otherwise routes the job into the existing single-machine queue or `pendingMultiEligible` path.

Gate 5 preserves those selection/ranking and recovery semantics. It inserts a deterministic transfer
interval only after a concrete destination is selected and currently admissible for binding.

## 4. Delivery policy

Gate 5 implementation is deliberately split into small semantic increments so a coding agent can
execute a well-bounded change without being asked to rediscover architecture while coding.

Every slice should have:

- one dominant semantic invariant;
- explicit executable evidence that closes that invariant;
- narrow production ownership and explicit non-goals;
- an escalation rule: if implementation evidence conflicts with ADR-0014, ADR-0015, or
  `engine-semantics:v1`, stop and surface the contradiction rather than inventing new semantics.

This decomposition is provider-neutral. Repository architecture and acceptance evidence determine
whether a slice is sufficiently bounded; no particular model, agent, IDE, or hosted service is an
implementation dependency.

The vertical transfer-activation slice is intentionally not decomposed into separate state/event/
observation PRs. Once `TRANSFERRING` becomes reachable, supported state and supported deltas must be
coherent in the same landed change under ADR-0011.

## 5. Delivery slices

### G5-0 — Pin existing Engine semantics

**Prerequisite:** ADR-0015 landed Accepted.

**Responsibility**

Add characterization/conformance evidence for the result-affecting behavior that
`engine-semantics:v1` inherits from the pre-Gate-5 Engine:

- Gate 2 offline filtering with all-offline fallback;
- `canAcceptJob` as the primary ranking key rather than an eligibility filter;
- `combinedQueueDepth`, including compatible `pendingMultiEligible` work;
- deterministic `MachineId` tie-breaking;
- queue-before-`pendingMultiEligible` recovery cascade ordering, including one dispatch per trigger
  from a machine's own queue and the fixpoint rescan of the multi-eligible backlog;
- per-machine queue FIFO arrival order, and waiting-path selection by eligible-set size;
- multi-eligible backlog arrival order, captured eligible sets, and non-head-of-line-blocking;
- W1 child creation/release/dispatch ordering;
- the W1 child-materialization envelope (`1 <= N <= 100000`) and its no-partial-mutation rejection;
- scheduler equal-time insertion ordering where it is semantically observable.

**Evidence**

Pinned behavioral fixtures fail if any of those results change for identical explicit inputs. The
slice changes no production behavior and does not freeze incidental DTO or implementation shape.

The behaviors above are the ones `engine-semantics:v1` section 1.1 requires to be versioned rather
than left as ambient implementation policy. `G5-0` is where that requirement becomes executable
evidence, so a fixture gap here is a semantics gap, not a coverage preference.

**Non-goals**

Engine-semantics identity types, Factory V2, transfer behavior, new scheduling policy.

### G5-A1 — Factory V2 spatial model and validation

**Prerequisite:** ADR-0014 landed Accepted.

**Responsibility**

Add the five V2 authored facts and their validation, without yet releasing a V2 fingerprint policy:

- floor width/height;
- minimum-coordinate resource reference cell;
- anchored footprint width/height and exact occupied-cell semantics;
- `ticksPerCell`;
- `handlingTicks`;
- floor containment/non-overlap;
- the exact maximum-transfer-duration representability predicate from ADR-0014.

Do not claim that the publication predicate eliminates the pre-existing extreme-`SimTime.plus(...)`
overflow condition.

**Evidence**

Valid/invalid boundary cases prove anchor, containment, overlap and arithmetic semantics
independently of runtime transfer behavior.

**Non-goals**

Canonical V2 bytes/fingerprints, policy registration, V1→V2 migration, Engine behavior.

### G5-A2 — Factory V2 canonical identity

**Prerequisite:** G5-A1.

**Responsibility**

Release the `factory-model:v2` canonical policy exactly as specified by
[Factory Model v2 Canonicalization](../architecture/factory-model-v2.md), which is the normative
source of V2 fingerprint bytes:

- deterministic canonical encoding/decoding/verifying for V2 under that byte grammar;
- `ModelFingerprint` derivation under the V2 policy;
- policy registration while preserving `factory-model:v1`;
- the V2 golden vectors that specification requires;
- no automatic V1→V2 lift/default synthesis.

**Evidence**

The golden-vector set in that specification, including exact canonical bytes and the pinned
policy-domain prefix. V1 vectors/fingerprints remain byte-for-byte unchanged; equivalent V2 content
reproduces its fingerprint; every authored V2 field participates in identity; moving a resource
changes the fingerprint without changing resource identity; a grammar-valid artifact that violates a
V2 publication predicate is rejected on decode.

**Non-goals**

Cross-policy controlled-revision migration/comparison, runtime transfers.

### G5-A3 — Multi-policy historical resolution and evolution seam

**Prerequisite:** G5-A2. Governance G1 historical revision authority is already landed.

**Responsibility**

Make the first V1/V2 coexistence truthful:

- historical artifacts under both released Factory model policies remain resolvable/verifiable;
- controlled-revision lineage may cross policy versions without rewriting either artifact;
- a normal semantic `ChangeSet` does not silently invent V2 spatial facts for V1;
- implement only the narrow migration classification or common-representation seam actually needed
  for the first V1→V2 controlled transition.

**Evidence**

Historical V1 resolution remains intact after V2 registration, V2 resolves independently, and the
first cross-policy transition cannot be misreported as an ordinary same-policy empty/equivalent diff.

**Non-goals**

Generic migration/schema framework, transfer runtime behavior.

**Sequencing note:** G5-A3 does not have to block transfer implementation merely because V2 exists.
It must land before the first real cross-policy controlled transition and before final Gate 5
closure.

### G5-B1 — Engine semantics identity and runtime establishment

**Prerequisites:** ADR-0015 landed Accepted and G5-0.

**Responsibility**

- add first-class `EngineSemanticsVersion`;
- establish the one supported current value `engine-semantics:v1`;
- fix it when a `FactoryRuntime` is established;
- expose it directly from the runtime;
- provide a narrow support check that fails explicitly for an unsupported identity;
- do not add caller-selectable historical versions or a multi-version resolver.

**Evidence**

One runtime reports one immutable semantics version; reset/fresh runtime identity changes do not
change the semantics version; unsupported identity fails explicitly if the support seam is
exercised.

**Non-goals**

Observation/event field propagation, transfer behavior, retirement/multi-version support.

### G5-B2 — Runtime provenance propagation

**Prerequisite:** G5-B1.

**Responsibility**

- add mandatory `EngineSemanticsVersion` to `RuntimeObservationMetadata` and
  `RuntimeEventEnvelope`;
- add the missing optional `ControlledRevisionId` to observation metadata when authoritatively
  revision-bound, preserving symmetry with event provenance;
- keep `RunId`, `ModelFingerprint`, revision provenance and Engine semantics identity distinct.

**Evidence**

Fresh observations and every supported event carry the same model + Engine interpretation identity;
revision-bound runtimes expose the same optional authoritative revision in observations and events;
reset creates a new `RunId` without changing semantic interpretation.

**Non-goals**

REST/SSE migration, transfer state/events, generic provenance framework.

**Convergence note:** G4-D outward API/SSE migration should consume this settled runtime provenance
shape rather than migrate the old envelope and immediately revise it.

### G5-C1 — Pure transfer arithmetic

**Prerequisite:** G5-A1 and the accepted `engine-semantics:v1` rule.

**Responsibility**

Implement a small, deterministic calculation seam for:

```text
manhattanDistance = abs(xDestination - xSource) + abs(yDestination - ySource)
transferDuration = handlingTicks + ticksPerCell * manhattanDistance
```

using exact integer arithmetic and the already-validated V2 bounds. Footprint does not participate
in v1 distance.

**Evidence**

Boundary/property cases cover reference-cell Manhattan distance, handling applied exactly once,
zero authored magnitudes, and overflow-safe behavior consistent with ADR-0014.

**Non-goals**

Destination selection, reservations, scheduler integration, runtime transfer state.

### G5-C2 — Destination admission-reservation substrate

**Prerequisite:** G5-B1.

**Responsibility**

Add the minimum internal state needed to reserve inbound admission capacity before processing starts:

- one inbound reservation consumes capacity used by future acceptance decisions;
- reserved inbound work is not `activeJobs`, is not queue depth, does not make a machine `Busy` by
  itself, and accrues no `busyTicks`;
- a destination holding only inbound reservations may still be taken offline;
- reservation can be released/converted deterministically for arrival and waiting paths.

Keep this substrate unreachable from ordinary runtime execution until G5-C3 activates transfers.

**Evidence**

Direct tests prove capacity admission, active/queue separation, offline behavior, deterministic
release/conversion and no regression of ordinary pre-Gate-5 processing semantics.

**Non-goals**

Public reservation aggregate, transfer timing/state/events, transport capacity.

### G5-C3 — Vertical transfer activation

**Prerequisites:** G5-A2, G5-B2, G5-C1 and G5-C2.

**Responsibility**

Activate the first coherent transfer path at the existing `handleTaskEnd` next-step seam:

- preserve Gate 2 selection timing/ranking and post-selection waiting paths;
- bind only when the selected destination is currently admissible;
- for a distinct resource, reserve admission capacity, compute/fix duration once, enter
  `TRANSFERRING`, and schedule completion;
- same-resource consecutive operations retain the no-transfer path;
- publish the minimum supported `TRANSFER_STARTED` / `TRANSFER_COMPLETED` deltas and expose the
  minimum in-flight observation facts in the same slice so no reachable authoritative state is
  invisible to the Gate 4 contract;
- use only Gate 4 sequence for same-time supported-event ordering.

Minimum in-flight observation facts are job/order correlation, source, bound destination,
`transferStartedAt`, `transferCompletesAt`, and resource admission load sufficient to keep resource
and job projections coherent.

**Evidence**

A happy-path distinct-resource job visibly and deterministically transitions from step completion to
in-flight state to arrival/next processing; source capacity releases; destination admission capacity
is held without active processing; a late observation during the interval is self-consistent; the
same-resource path creates no transfer.

**Non-goals**

Every offline/zero-duration edge case, outward API DTO migration, KPI closure beyond minimum
projection coherence.

### G5-C4 — Transfer edge semantics

**Prerequisite:** G5-C3.

**Responsibility**

Close the remaining v1 transfer rules without changing the core design:

- zero authored transfer magnitudes and zero-duration same-time ordering;
- immutable destination/no rerouting after transfer start;
- source going offline after departure has no effect;
- destination going offline in flight or at exact arrival does not change fixed completion time;
- arrival at an offline destination converts/releases reservation into the existing waiting path;
- another eligible destination becoming preferable does not cause rerouting.

**Evidence**

Focused tests prove the exact same-time sequence and all availability/no-rerouting cases, including
that the bound destination can actually become offline while only inbound-reserved work exists.

**Non-goals**

Transport resources, routing graphs, buffers, congestion, intermediate coordinates.

### G5-D — Observation, KPI and late-join closure

**Prerequisites:** G5-B2 and G5-C4.

**Responsibility**

Complete the supported read model around already-authoritative transfers:

- fresh observation reconstructs in-flight state without replay;
- job and destination resource projections agree at one sequence boundary;
- admission load/capacity is visible enough for supported bottleneck diagnosis;
- inbound reservation is not `activeJobIds`, queue depth, processing `Busy`, or processing
  `busyTicks`;
- backlog remains incomplete accepted orders;
- lead time naturally includes transfer delay;
- throughput remains completed orders per observed time;
- supported start/completion events and observations close over the same authoritative transitions.

**Evidence**

Late-join and observation/event-closure tests prove no internal store or scheduler/event-log replay is
required, and existing KPI meanings remain stable.

**Non-goals**

REST/SSE/UI projection, KPI redesign, retained supported-event history.

### G5-E — Headless Gate 5 closure

**Prerequisites:** G5-A3, G5-B2, G5-C4 and G5-D.

**Responsibility**

Add one decisive consumer-neutral proving scenario and extend the immutable
`engine-semantics:v1` fixture set with the landed spatial behavior. Reconcile the parent Gate 5
acceptance/status documentation without adding presentation/API dependencies.

**Required proving case**

The scenario demonstrates:

1. two otherwise equivalent V2 designs differing only in canonical placement produce different
   transfer duration/completion under the same `engine-semantics:v1`;
2. moving the resource changes `ModelFingerprint` while keeping stable resource identity;
3. repeated execution with identical model, semantics version, workload, seed and ordered commands
   produces identical ordered semantic outcomes;
4. a mid-transfer fresh observation reconstructs supported in-flight state and agrees with resource
   admission load without replay;
5. transfer start/completion ordering is deterministic, including zero-duration behavior;
6. the result retains both design provenance (`ModelFingerprint`) and interpretation provenance
   (`EngineSemanticsVersion`);
7. a destination can become offline after binding, transfer completes at the fixed time, and the
   job waits on that bound destination without rerouting;
8. V1 historical fingerprints/resolution remain unchanged and V1 receives no synthesized spatial
   semantics;
9. behavioral conformance fixtures make a future different semantics version independently
   verifiable without requiring the initial runtime to execute two versions.

## 6. Dependency and parallelism map

```text
G5-0 existing-semantics fixtures ---> G5-B1 semantics identity ---> G5-B2 provenance ----+
                                                                                |          |
G5-A1 V2 model/validation ---> G5-A2 V2 identity -------------------------------+-> G5-C3 activation
       |                         |                                                        |
       |                         +--> G5-A3 policy evolution -------------------------+   v
       |                                                                             | G5-C4 edges
       +--> G5-C1 transfer arithmetic ---------------------------------------------+ |   |
                                                                                   | |   v
G5-B1 semantics identity ---> G5-C2 admission reservation ------------------------+ | G5-D closure
                                                                                     |   |
                                                                                     +--> G5-E
```

Practical parallelism after the ADRs land:

- `G5-0` and `G5-A1` are independent first slices;
- after `G5-0`, `G5-B1` can proceed while `G5-A1/A2` advances;
- after `G5-A1`/`G5-B1`, `G5-C1` and `G5-C2` can proceed independently;
- `G5-A3` is compatibility/history work and need not block `G5-C3`, but it must close before
  `G5-E` and before a real V1→V2 controlled transition;
- `G5-C3` is the deliberate convergence point and should receive correspondingly strong review.

## 7. KPI acceptance

Gate 5 preserves existing metric definitions:

- machine `busyTicks` / utilization measure processing time, not inbound reservation time;
- queue depth does not count an in-flight reservation as a queued arrival;
- `activeJobIds` contains processing jobs, not inbound reserved jobs;
- backlog remains incomplete accepted orders;
- lead time naturally increases with transfer delay;
- throughput remains completed orders per observed time.

Bottleneck/capacity interpretation must account for admission capacity held by transfer-bound work
even when processing busy ticks have not yet accrued. This is an interpretation/projection extension,
not a redefinition of processing utilization or queue depth.

## 8. Cross-track consequences

### Challenge — REQUIRED WHEN CONSUMER INTEGRATES

When Challenge attempts consume real Engine-produced results, compatibility must include
`EngineSemanticsVersion` wherever changed Engine semantics can affect compared outcomes. Synthetic
Challenge-only attempts do not block Gate 5.

### Governance — REQUIRED WHEN CONSUMER INTEGRATES

Future Arcogine analytical evidence produced from simulation must retain `ModelFingerprint`,
`EngineSemanticsVersion`, and the explicit producing inputs/results required by ADR-0016. Governance
consumes this provenance; it does not own Engine semantics.

### Operational — REQUIRED WHEN CONSUMER INTEGRATES

Future twin/reconciliation analytics retain Engine interpretation provenance independently of
`ExecutionContextId`. The two answer different questions: which Arcogine interpretation produced a
result versus which operational consequence context an interpretation belongs to.

### API/SSE Gate 4 transport migration — REQUIRED BEFORE THAT MIGRATION, NOT BEFORE HEADLESS G5

Land G5-B2's final runtime provenance shape before G4-D migrates supported events and observations
outward. This avoids immediate wire-contract churn. G4-D is not a prerequisite for headless Gate 5.

## 9. Acceptance / readiness

Gate 5 is architecture-ready once ADR-0014 and ADR-0015 are Accepted and this focused plan is
reconciled with the parent Engine/Factory plans. Implementation remains pending until the slices
above land with executable evidence.

No additional architecture analysis is required before these slices unless implementation evidence
falsifies one of the accepted invariants. A slice that encounters such evidence must stop at that
boundary rather than silently revising the accepted contract in product code.
