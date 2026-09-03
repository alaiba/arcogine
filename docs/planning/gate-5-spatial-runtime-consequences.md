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

## 4. Delivery slices

### G5-A — Factory V2 canonical spatial model

**Prerequisite:** ADR-0014 landed Accepted.

**Production responsibility**

- introduce the `factory-model:v2` canonical artifact policy;
- add required floor dimensions, resource position, footprint, `ticksPerCell`, and `handlingTicks`;
- define position as the footprint's minimum-coordinate reference cell and validate exact occupied
  cells, containment, and non-overlap;
- enforce the exact safe-duration predicate from ADR-0014:
  `maxDistance = (W - 1) + (H - 1)` and
  `handlingTicks + ticksPerCell * maxDistance` must be representable with overflow-safe arithmetic;
- do not claim this publication predicate eliminates the pre-existing possibility that adding a
  valid duration to an extreme current `SimTime` can overflow;
- register policy-aware verification/decoding without weakening V1 resolution;
- define the narrow first V1-to-V2 policy-migration/comparison behavior required by real controlled
  revision use.

**Executable evidence**

- V1 golden vectors/fingerprints remain unchanged;
- V2 canonical round-trip and fingerprint determinism;
- moving a resource or changing any V2 authored field changes `ModelFingerprint`;
- the same `(position, footprint)` pair has one unambiguous occupied-cell set in every conforming
  implementation;
- invalid placement/overlap/bounds and unsafe maximum-transfer arithmetic are rejected;
- V1 and V2 artifacts are both independently resolvable;
- no automatic V1-to-V2 lift/default synthesis exists.

**Non-goals**

- Engine transfer runtime behavior;
- path networks, orientation, conveyors, transport resources;
- generic schema/migration framework.

### G5-B — Engine semantics identity, provenance, and v1 conformance substrate

**Prerequisite:** ADR-0015 landed Accepted. This slice may run in parallel with G5-A.

**Production responsibility**

- add `EngineSemanticsVersion` as a first-class Engine-owned identity;
- establish one current supported value for `engine-semantics:v1`;
- fix the version for each runtime and expose it directly from `FactoryRuntime`;
- add mandatory semantics-version provenance to `RuntimeObservationMetadata` and
  `RuntimeEventEnvelope`;
- add the missing optional `ControlledRevisionId` to `RuntimeObservationMetadata`; ADR-0011's
  provenance contract already requires revision attribution when the runtime is authoritatively
  revision-bound, and Gate 5 must not leave observation metadata asymmetric with the event envelope;
- establish a small support check for the current version without a multi-version resolver;
- pin the actually shipped pre-Gate-5 semantics, including offline filtering/all-offline fallback,
  `canAcceptJob` ranking, `combinedQueueDepth`, `MachineId` tie-breaking, queue-before-
  `pendingMultiEligible` recovery ordering, W1 decomposition, and scheduler ordering;
- extend those fixtures with Gate 5 behavior as later slices land.

**Executable evidence**

- one runtime reports one immutable semantics version;
- semantics version cannot change mid-run;
- a fresh runtime has a fresh `RunId` without changing semantics version;
- observations/events always carry model + Engine semantics provenance;
- authoritatively revision-bound runtimes expose the same optional `ControlledRevisionId` provenance
  in supported observation metadata and event envelopes;
- unsupported semantics identity fails explicitly if the support seam is exercised;
- conformance fixtures describe the current selector/cascade rather than an idealized variant;
- semantics-preserving representation changes do not alter the pinned behavioral fixtures.

**Non-goals**

- caller-selectable historical semantics;
- multi-version resolver;
- retirement process;
- Challenge/Governance/Operational consumer changes.

**Parallel/convergence note:** landing the final runtime provenance shape here removes avoidable
contract churn for the still-deferred Gate 4 API/SSE transport migration (G4-D). G4-D should consume
the settled provenance shape rather than migrate the old envelope and immediately revise it again.

### G5-C — Authoritative transfer timing and state

**Prerequisites:** G5-A + the identity/provenance substrate from G5-B.

**Production responsibility**

- insert Gate 5 at the existing `handleTaskEnd` next-step seam;
- preserve Gate 2 destination selection timing/ranking and its post-selection waiting paths;
- reserve one unit of **destination admission capacity** at binding for an inbound job;
- keep inbound reservation distinct from active processing and queued work: reservation affects
  future `canAcceptJob` capacity, but does not put the job in `activeJobs`, does not make the machine
  `Busy` by itself, and does not accrue `busyTicks`;
- compute/fix transfer duration exactly once using Engine Semantics v1;
- introduce authoritative in-flight state and completion scheduling;
- preserve no-transfer behavior for same-resource consecutive steps;
- preserve immutable destination/no-rerouting behavior;
- allow a destination holding only inbound reservations to be taken offline under the existing
  command contract;
- when an inbound job arrives at an offline destination, complete the transfer at its fixed time,
  then convert/release the reservation into the existing destination waiting path without rerouting.

**Executable evidence**

- transfer begins only once the selected destination can accept the job;
- destination binding is immutable after start;
- duration equals `handlingTicks + ticksPerCell * Manhattan(position)`;
- source processing capacity is released at step completion;
- destination admission capacity is reserved while in flight without treating the job as active
  processing or queued work;
- another job observes the reservation through the acceptance-capacity decision;
- the reserved destination can still be taken offline before arrival;
- same-resource next step creates no transfer;
- repeated identical inputs produce identical assignment/timing/state;
- destination-offline-at-arrival completes transfer on time and then waits without rerouting.

**Non-goals**

- transport capacity/resources;
- intermediate coordinates/progress authority;
- pathfinding/routing graphs;
- KPI redesign;
- a new public resource/reservation domain abstraction.

### G5-D — Supported transfer observations and events

**Prerequisite:** G5-C authoritative state transitions.

**Production responsibility**

- extend the existing per-job observation with `TRANSFERRING` state and the minimum transfer facts;
- keep destination/current-resource identity coherent with the admission-reservation semantics;
- keep resource observation consistent with job state: an inbound reserved job is not reported in
  `activeJobIds`, does not contribute queue depth, and does not make an otherwise idle machine
  `Busy` merely because it is reserved;
- expose enough resource-capacity/admission-load information for the supported bottleneck/capacity
  observation to account for reserved inbound work without reclassifying it as processing;
- add supported `TRANSFER_STARTED` and `TRANSFER_COMPLETED` event types/payloads;
- preserve ADR-0011 late-join reconstruction from one fresh observation plus subsequent events;
- use only Gate 4 sequence for same-time ordering.

**Executable evidence**

- a late-joining consumer reconstructs an in-flight transfer from one fresh observation with no
  replay/journal access;
- the job projection and resource projection agree on one sequence boundary: the job is
  `TRANSFERRING`, the destination has admission capacity consumed, the job is not active processing
  and not queued;
- start/completion events close over the same authoritative state transitions as observations;
- zero-duration transfer completion is a separate same-time scheduler turn, so
  `TRANSFER_STARTED` is published after entering `TRANSFERRING` and before
  `TRANSFER_COMPLETED`/next dispatch;
- no event/observation carries frame-by-frame animation state;
- metadata/envelopes retain `RunId`, `ModelFingerprint`, `EngineSemanticsVersion`, and optional
  authoritative `ControlledRevisionId` as applicable.

**Non-goals**

- REST/SSE DTO migration itself;
- UI interpolation;
- retained event journal.

### G5-E — Headless Gate 5 closure

**Prerequisites:** G5-A through G5-D.

**Production responsibility**

Add one decisive consumer-neutral headless proving scenario and close the parent Gate 5 acceptance
criteria without adding presentation/API dependencies.

**Required proving case**

The scenario must demonstrate, with minimal fixture complexity:

1. two otherwise equivalent V2 designs differing only in canonical placement produce different
   transfer duration/completion under the same `engine-semantics:v1`;
2. moving the resource changes `ModelFingerprint` while keeping stable resource identity;
3. repeated execution with identical model, semantics version, workload, seed, and ordered commands
   produces identical ordered semantic outcomes;
4. an observation taken mid-transfer reconstructs the supported in-flight state without event
   replay and agrees with destination resource admission-load state;
5. transfer start/completion ordering is deterministic and supported;
6. the result retains both design provenance (`ModelFingerprint`) and interpretation provenance
   (`EngineSemanticsVersion`);
7. a V2 model with zero transfer magnitudes preserves pre-Gate-5 completion timing while still
   exposing distinct-resource transfer transitions;
8. a destination can become offline after binding, transfer completes at the fixed time, and the
   job waits on that destination without rerouting;
9. the Engine Semantics v1 conformance fixtures prove that a future different semantics version may
   change an outcome for the same `ModelFingerprint` without redefining the Factory design.

The final item belongs to the semantic-version conformance contract; the initial runtime need not
implement two selectable semantics versions merely to demonstrate the identity distinction.

## 5. KPI acceptance

Gate 5 must preserve existing metric definitions:

- machine `busyTicks` / utilization measure processing time, not inbound reservation time;
- queue depth does not count an in-flight reservation as a queued arrival;
- `activeJobIds` contains processing jobs, not inbound reserved jobs;
- backlog remains incomplete accepted orders;
- lead time naturally increases with transfer delay;
- throughput remains completed orders per observed time.

Bottleneck/capacity interpretation must account for admission capacity held by transfer-bound work
even when processing busy ticks have not yet accrued. This is an interpretation/projection extension,
not a redefinition of processing utilization or queue depth.

## 6. Cross-track consequences

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

Prefer landing G5-B's final runtime provenance shape before G4-D migrates supported events and
observations outward. This prevents immediate wire-contract churn. G4-D is not a prerequisite for
headless Gate 5 implementation.

## 7. Acceptance / readiness

Gate 5 is architecture-ready once ADR-0014 and ADR-0015 are Accepted and this focused plan is
reconciled with the parent Engine/Factory plans.

Implementation order:

```text
G5-A  Factory V2 model policy -----------┐
                                         ├─> G5-C transfer runtime
G5-B  Engine semantics/provenance -------┘          |
      (may run in parallel;                         v
       settles provenance for G4-D)            G5-D observations/events
                                                    |
                                                    v
                                               G5-E headless closure
```

No additional architecture analysis is required before these slices unless implementation evidence
falsifies one of the accepted invariants.
