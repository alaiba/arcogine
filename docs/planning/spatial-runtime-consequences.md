# PLAN-ENG-5 — Spatial Runtime Consequences Delivery Plan

Status: Active delivery plan. PLAN-ENG-5-0 (pinned pre-spatial Engine behavior), the Factory spatial record with its validation and canonical form (PLAN-ENG-5-A1/A2), and a runtime that fixes one Engine interpretation (PLAN-ENG-5-B1) are implemented. Spatial execution is not: a published model with a present spatial record is refused before any runtime state exists. The transfer slices below are unimplemented and ordered by ordinary dependencies.
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
- [Engine semantics](../architecture/engine-semantics.md), whose §5–§13 specify how the work-in-progress
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

Both the Factory model and the Engine interpretation are work in progress under the
[semantic evolution rules](../architecture/overview.md#semantic-evolution-and-support). Activating
spatial execution therefore needs no successor Factory policy or Engine identity: it implements
already-specified rules of the current interpretation, and any correction those rules need while
implementing is made in the Engine specification and its fixtures in the same change.

The first-release local-admission and shared-backlog-ranking questions in
[Engine Evolution Research](../research/investigations/engine-evolution.md) are concluded after
independent adversarial review and retain the current rules. PLAN-ENG-5-0 pins them. Their
reservation-aware recheck applies at PLAN-ENG-5-C3.

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
The concluded first-release research deliberately retains those rules with qualifications; it does
not claim they are globally optimal. Any outcome-changing alternative is an explicit change to the
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

### PLAN-ENG-5-0 — Pin existing Engine semantics

**Status:** Implemented. The first-release dispatch research gate is cleared: both reviewed questions
retain the current rules, with the qualifications recorded in
[Engine Evolution Research](../research/investigations/engine-evolution.md).

**Prerequisites:** the Determinism Contract is adopted architecture, plus conclusion of the two
first-release dispatch questions with the current rules retained. Those prerequisites are satisfied. A
future reopening that recommends an outcome-changing alternative does not rewrite this slice silently;
it is an explicit Engine specification and fixture change with a new plan decision.

**Responsibility**

Add characterization/conformance evidence for the result-affecting behavior the Engine interpretation
inherits from the pre-spatial Engine:

- PLAN-ENG-2 offline filtering with all-offline fallback;
- `canAcceptJob` as the primary ranking key rather than an eligibility filter;
- `combinedQueueDepth`, including compatible `pendingMultiEligible` work;
- deterministic `MachineId` tie-breaking;
- queue-before-`pendingMultiEligible` recovery cascade ordering, including one dispatch per trigger
  from a machine's own queue and the fixpoint rescan of the multi-eligible backlog;
- per-machine queue FIFO arrival order, and waiting-path selection by eligible-set size;
- multi-eligible backlog arrival order, captured eligible sets, and non-head-of-line-blocking;
- PLAN-ENG-W1 child creation/release/dispatch ordering;
- the PLAN-ENG-W1 child-materialization envelope (`1 <= N <= 100000`) and its no-partial-mutation rejection;
- exact `combinedQueueDepth` ranking arithmetic (ranking semantics, not a reported result — see the
  ownership note below);
- the derived-result arithmetic: `busyTicks` overflow saturation, elapsed-time subtraction flooring
  at zero, and the zero-denominator throughput / empty-set mean-lead-time results;
- the derived-result accumulators: exact completed-order counting and completion-ordered value
  accumulation;
- scheduler equal-time insertion ordering where it is semantically observable.

**Ownership note — reported derived results.** The last two bullets pin *reported* derived results
(`busyTicks`, throughput, mean lead time, completed-order counting and value accumulation) whose
placement on the Engine/analytics boundary is an open question under
[Simulation analytics consumer boundary](../research/investigations/simulation-analytics-consumer-boundary.md).

Adopted architecture makes those values part of the Engine interpretation: the §1.1 membership test
covers derived-result arithmetic, §10.1–§10.2 fix their edge cases *and* their accumulation, and §1.1
consequence 4 states that a rule satisfying the membership test but absent from the specification is a
defect in the specification. Pinning them is therefore the honest action.

What pinning does **not** do is settle ownership. These fixtures characterize current behavior under
the accepted contract; they are not evidence that these values permanently belong in the supported
runtime observation. If the analytics research concludes they should move, that is an explicit change
to a supported contract reconciled through architecture, not something this slice pre-decides.

Two constraints follow for implementers:

1. do not cite these fixtures as evidence that the ownership question is closed;
2. do not harden these specific values into *new* outward contracts or DTOs while the research is
   open — see the KPI ownership note in
   [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md#3-current-implementation-queue).

`combinedQueueDepth` is deliberately listed separately and is **not** in this category: it is derived
but result-affecting, because its exact arithmetic decides assignment. It remains Engine semantics
regardless of the analytics outcome. Every other bullet in this slice — dispatch, acceptance,
scheduler, ranking, materialization, and spatial behavior — is authoritative and unaffected by that
research.

**Evidence**

Pinned behavioral fixtures fail if any of those results change for identical explicit inputs. The
slice does not freeze incidental DTO or implementation shape.

The reconciled research adds these mandatory dispatch fixtures to the conformance set. Use semantic
names rather than research candidate/sample labels.

**Local admission / recovery:**

- one offline concurrency-4 machine with eight queued 10-tick single-eligible jobs completes after
  80 ticks from recovery; this pins the retained one-local-job recovery stage rather than a
  fill-to-capacity interpretation;
- a mixed recovery with M1 concurrency 4, M2 offline, two local M1-only 10-tick jobs and four shared
  `{M1,M2}` 3-tick jobs proves the sequence is local stage first and then shared fixpoint, while also
  proving that one local admission does not reserve all remaining recovery opportunity for local work;
- the routed regression pins a valid case with order completions `[14,29,29]` / makespan 29,
  against the rejected fill behavior's `[14,40,21]` / makespan 40;
- the objective-conflict case pins makespan/mean-order completion `93 / 69.75`, against the
  rejected fill behavior's `91 / 71.25`;
- eight jobs routed through `M1 concurrency 4, duration 10 -> M2 unary, duration 100` complete at 810
  under both interpretations, proving upstream capacity fill does not imply terminal improvement;
- preserve same-machine continuation precedence, local-before-shared ordering, FIFO local start
  order, queue-smaller-than-capacity recovery, repeated recovery, and equal-time completion ordering.

**Shared flexible-backlog ranking:**

- canonical overlap: `route-order: M1:5 -> {M1,M2}:1 -> M1:1`, `clock-order: M3:6`,
  `shared-order: {M1,M3}:100`, all machines unary. The current rule produces `[106,6,105]`, mean
  72.33, makespan 106; the rejected local-depth-only interpretation produces `[7,6,106]`, mean 39.67,
  makespan 106. The shared job must remain unbound and later reselect a different machine when the
  runtime state changes;
- one-variable mirror: use the same case but change only `shared-order` duration from 100 to 1. The
  current mean is 6.33 versus 6.67 under local-depth-only even though the comparator's decisive input
  projection is unchanged. This prevents the canonical case from being encoded as evidence that local
  depth is a generally superior ranking;
- long-step mirror: `route-order: M1:5 -> {M1,M2}:100`, `clock-order: M3:1000`,
  `shared-order: {M1,M3}:10`, all machines unary. The current rule produces `[105,1000,15]`, mean
  373.33, makespan 1000; local-depth-only produces `[105,1000,115]`, mean 406.67, makespan 1000.
  Preserve this independently shaped reversal alongside the tighter one-variable mirror rather than
  treating either case as a substitute for the other;
- scarce-machine protection: `route-order: M1:5 -> {M1,M2}:60`, `clock-order: M3:500`, and two
  `shared-order` instances each eligible on `{M1,M3}` for 20 ticks. The current rule completes
  `[65,500,25,45]`, mean 158.75; local-depth-only completes `[65,500,85,105]`, mean 188.75;
- makespan non-neutrality: use the canonical routes with concurrency 2 on M1/M2 and quantities
  `2 / 1 / 3`. The current rule yields `[106,6,106]`, mean 72.67, makespan 106; local-depth-only
  yields `[7,6,107]`, mean 40.00, makespan 107;
- overlap multiplicity: one 100-tick shared job eligible on `{M1,M3,M4,M5}` while M3/M4/M5 are held
  for 500 ticks; preserve unbound waiting and reselection while the shared entry contributes to every
  compatible candidate's exact key;
- recovery magnitude-sensitive corner: construct a ranking call with a recovered concurrency-4
  machine still accepting with local depth 3 under the retained one-local-job recovery stage,
  competing with a just-freed unary machine carrying two compatible pending shared entries. Exact keys
  are 3 versus 2 and choose the unary machine; a positive weight of 2 would produce 3 versus 4 and
  choose the recovered machine. This pins that exact shared-entry magnitude matters in the retained
  recovery corner and prevents over-generalizing the usual binary-handover interpretation.

**Interpretation boundary from reconciliation:**

- conformance pins **behavior**, not a claim that the retained local-admission or ranking rules are
  optimal;
- one-local-job admission is an inherited asymmetry of the local queue stage, not a general
  trigger-budget invariant: initial admission and the shared fixpoint already fill capacity;
- `combinedQueueDepth` acts like a binary handover signal only on the sub-domain where competing
  accepting candidates do not carry residual local queues; the recovery fixture above pins the
  reachable magnitude-sensitive exception;
- generated research win ratios are not workload probabilities and must not appear as conformance
  requirements;
- the partial-fault history where recovery-only fill and fill-on-every-local-stage diverge belongs to
  the fault/recovery contract, not to healthy scheduling-policy selection. Preserve it only where the
  existing partial-mutation fault contract is being pinned;
- if implementation or later spatial admission mechanics create a new healthy state that violates the
  reviewed enqueue/free-capacity/ranking assumptions, stop and reopen the bounded research question
  instead of silently extending the rationale.

PLAN-ENG-5-0 included **two deliberate production changes**, both required to make the normative
arithmetic implementable: mean-lead-time accumulation saturates rather than wrapping, as Engine
semantics §10.2 rule 1 requires, and `combinedQueueDepth` is summed and compared at 64-bit width
rather than narrowing a 64-bit backlog count, as §2 rule 3 requires. Neither changed results in any
regime that was reachable before; each preserves current results wherever the earlier arithmetic was
already correct.

The executable evidence is maintained in `EngineDispatchConformanceTest`,
`EngineDerivedResultConformanceTest`, the session-control acceptance suite, and the
child-materialization acceptance/benchmark tests. These tests pin current behavior; they do not claim
that the dispatch policy is optimal or settle the Engine-versus-analytics ownership question.

**Non-goals**

Engine interpretation naming, Factory spatial content, transfer behavior, new scheduling policy.

### PLAN-ENG-5-A1 — Factory spatial record and validation

**Status:** Implemented. `FactoryModel` carries the required production records and an explicitly
optional `SpatialRecord` (`com.arcogine.factory.model.spatial`) whose per-resource `ResourceLayout`
entries reference resources by `MachineId`; `FactoryModelValidator` implements every predicate below.

**Responsibility**

- an explicitly absent record that validates production semantics only and synthesizes nothing;
- a present record that is complete: one layout per configured resource, in resource-list order,
  with no unknown or repeated entry;
- floor width/height;
- minimum-coordinate resource reference cell;
- anchored footprint width/height and exact occupied-cell semantics;
- `ticksPerCell`;
- `handlingTicks`;
- floor containment/non-overlap;
- the exact maximum-transfer-duration representability predicate from the Factory model.

Do not claim that the publication predicate eliminates the pre-existing extreme-`SimTime.plus(...)`
overflow condition.

**Evidence**

`SpatialRecordValidationTest` and `SpatialRecordTest` prove absence, present-with-zero distinctness,
coverage, anchor, containment, overlap and arithmetic semantics independently of runtime transfer
behavior.

### PLAN-ENG-5-A2 — Spatial content in the canonical form

**Status:** Implemented. The work-in-progress `factory-model:wip` canonical form and its one
aggregate fingerprint cover the production records and the spatial record, with or without the record
present, as specified by the [Factory model](../architecture/factory-model.md).

**Evidence**

`FactoryModelCanonicalFormTest` and `FactoryModelArtifactTest` pin golden bytes and fingerprints for
spatial-absent and spatial-present models, the absent-versus-legal-zero distinction, presence-marker
framing, coverage refusal, field and order sensitivity, strict decoding and publication-predicate
rejection on decode, and refusal of discarded definitions.

**Non-goals**

A separate policy release, migration from earlier development definitions, Engine behavior.

### PLAN-ENG-5-B1 — Engine interpretation fixed per runtime

**Status:** Implemented. `EngineSemantics` names the Engine interpretation, with the single supported
work-in-progress value `engine-semantics:wip`. `FactoryRuntime` fixes and exposes it for its lifetime;
fresh/reset runtimes receive new `RunId` values without changing it. Any other name, the discarded
ordinal names included, fails explicitly through the narrow support check. A published model with a
present spatial record is refused with `UnsupportedModelContentException` before any runtime state
exists.

**Evidence**

`EngineSemanticsTest` and `EngineSemanticsAcceptanceTest`.

**Non-goals**

Observation/event field propagation, transfer behavior, caller-selectable or multiple interpretations.

### PLAN-ENG-5-B2 — Runtime provenance propagation

**Prerequisite:** PLAN-ENG-5-B1.

**Responsibility**

- add the mandatory `EngineSemantics` to `RuntimeObservationMetadata` and `RuntimeEventEnvelope`;
- add the missing optional `ControlledRevisionId` to observation metadata when authoritatively
  revision-bound, preserving symmetry with event provenance;
- keep `RunId`, `ModelFingerprint`, revision provenance and the Engine interpretation distinct.

**Evidence**

Fresh observations and every supported event carry the same model fingerprint and Engine
interpretation; revision-bound runtimes expose the same optional authoritative revision in
observations and events; reset creates a new `RunId` without changing the interpretation.

**Non-goals**

REST/SSE migration, transfer state/events, generic provenance framework, any claim that
work-in-progress provenance is durable across development revisions.

**Convergence note:** outward consumer convergence is retired as a standing objective (see
[Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md#3-current-implementation-queue)); a future
outward adapter should consume this settled runtime provenance shape rather than introduce an envelope that
immediately needs revising.

### PLAN-ENG-5-C1 — Pure transfer arithmetic

**Status:** Not started.

**Prerequisite:** PLAN-ENG-5-A1.

**Responsibility**

Implement a small, deterministic calculation seam for:

```text
manhattanDistance = abs(xDestination - xSource) + abs(yDestination - ySource)
transferDuration = handlingTicks + ticksPerCell * manhattanDistance
```

using exact integer arithmetic and the already-validated spatial bounds. Footprint does not participate
in distance.

**Evidence**

Boundary/property cases cover reference-cell Manhattan distance, handling applied exactly once,
zero authored magnitudes, and overflow-safe behavior consistent with the Factory model's
maximum-transfer predicate.

**Non-goals**

Destination selection, reservations, scheduler integration, runtime transfer state.

### PLAN-ENG-5-C2 — Destination admission-reservation substrate

**Status:** Not started.

**Prerequisite:** PLAN-ENG-5-B1.

**Responsibility**

Add the minimum internal state needed to reserve inbound admission capacity before processing starts:

- one inbound reservation consumes capacity used by future acceptance decisions;
- reserved inbound work is not `activeJobs`, is not queue depth, does not make a machine `Busy` by
  itself, and accrues no `busyTicks`;
- a destination holding only inbound reservations may still be taken offline;
- reservation can be released/converted deterministically for arrival and waiting paths.

Keep this substrate unreachable from ordinary runtime execution until PLAN-ENG-5-C3 activates transfers.

**Evidence**

Direct tests prove capacity admission, active/queue separation, offline behavior, deterministic
release/conversion and no regression of ordinary pre-spatial processing semantics.

**Non-goals**

Public reservation aggregate, transfer timing/state/events, transport capacity.

### PLAN-ENG-5-C3 — Vertical transfer activation

**Prerequisites:** PLAN-ENG-5-B2, PLAN-ENG-5-C1 and PLAN-ENG-5-C2.

**Research recheck before activation:** inbound reservation changes what "can accept" means and was a
named reopening seam in both reviewed first-release dispatch questions. Before making the reservation
substrate reachable, prove that reservation-aware admission does not create a new healthy state where
an online machine has free processing capacity plus residual local work, or where more than one
accepting candidate can carry shared-demand pressure in a way the retained conformance model did not
cover. If it does, stop and reopen the bounded local-admission/ranking research instead of silently
extending the first-release rationale. This check is triggered when reservation-aware runtime
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

**Prerequisites:** PLAN-ENG-5-B2 and PLAN-ENG-5-C4.

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

Late-join and observation/event-closure tests prove no internal store or scheduler-event replay is
required, and existing KPI meanings remain stable.

**Non-goals**

REST/SSE/UI projection, KPI redesign, retained supported-event history.

### PLAN-ENG-5-E — Headless PLAN-ENG-5 closure

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
6. the result carries both design provenance (`ModelFingerprint`) and interpretation provenance
   (`EngineSemantics`);
7. a destination can become offline after binding, transfer completes at the fixed time, and the
   job waits on that bound destination without rerouting;
8. a design without a spatial record receives no synthesized spatial semantics;
9. the spatial conformance fixtures pin Engine semantics §14 items 7–11 and 13 as the executed scope.

## 6. Dependency and parallelism map

```text
CONCLUDED dispatch research --retain current rules--> PLAN-ENG-5-0 fixtures (implemented)

PLAN-ENG-5-A1 spatial record/validation (implemented) ---> PLAN-ENG-5-A2 canonical form (implemented)
       |
       +--> PLAN-ENG-5-C1 transfer arithmetic ---------------------------------+
                                                                               |
PLAN-ENG-5-B1 interpretation per runtime (implemented) ---> PLAN-ENG-5-B2 provenance ---+--> PLAN-ENG-5-C3 activation
       |                                                                                |          |
       +--> PLAN-ENG-5-C2 admission reservation ---------------------------------------+          v
                                                                                        PLAN-ENG-5-C4 edges
                                                                                                   |
                                                                                                   v
                                                                                        PLAN-ENG-5-D closure
                                                                                                   |
                                                                                                   v
                                                                                        PLAN-ENG-5-E
```

Practical parallelism:

- `PLAN-ENG-5-B2`, `PLAN-ENG-5-C1` and `PLAN-ENG-5-C2` are independent of each other;
- `PLAN-ENG-5-C2` stays unreachable from ordinary execution until `PLAN-ENG-5-C3`;
- `PLAN-ENG-5-C3` performs the reservation-aware dispatch recheck before activation;
- work outside this chain may proceed under its own plan.

## 7. KPI acceptance

PLAN-ENG-5 preserves existing metric definitions:

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

When Challenge attempts consume real Engine-produced results, compatibility must include the Engine
interpretation wherever changed Engine semantics can affect compared outcomes; while the interpretation
is work in progress, results from different development revisions are not comparable by name alone.
Synthetic Challenge-only attempts do not block PLAN-ENG-5.

### Governance — REQUIRED WHEN CONSUMER INTEGRATES

Future Arcogine analytical evidence produced from simulation must retain `ModelFingerprint`,
`EngineSemantics`, and the explicit producing inputs/results required by the Governance evidence
contract. Governance consumes this provenance; it does not own Engine semantics, and it treats
work-in-progress provenance as development evidence rather than a durable cross-revision reference.

### Operational — REQUIRED WHEN CONSUMER INTEGRATES

Future twin/reconciliation analytics retain Engine interpretation provenance independently of the durable operational identity and independently of subject correspondence. These answer different questions: which Engine interpretation produced a result; which accountable operational continuation a record belongs to; and which external and Arcogine subjects are authoritatively related. The Operational continuity contract is adopted and defines that identity's referent and rules, while deliberately deferring its final type name and representation; `EngineSemantics` and `RunId` remain Engine-owned and must never be derived from it, or it from them.

### A future outward transport migration — SEQUENCE IF AND WHEN ONE IS INTRODUCED

If a future outward adapter migrates supported events and observations outward, it should land after
PLAN-ENG-5-B2's final runtime provenance shape, to avoid immediate wire-contract churn. No such migration is
currently a prerequisite for headless PLAN-ENG-5, since outward consumer convergence is retired as a standing
objective (see [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md#3-current-implementation-queue)).

## 9. Acceptance / readiness

The landed PLAN-ENG-5 conformance, Factory spatial record/validation/canonical-form and Engine
interpretation work are valid evidence under the current architecture. The remaining transfer path is
ready to proceed in dependency order; no separate identity or applicability research gate remains.
Until PLAN-ENG-5-C3 lands, present spatial content stays refused rather than executed without its
transfer semantics. Any slice that encounters evidence contradicting the Factory model, the Engine
specification or the transfer boundary must stop and reconcile the owning specification rather than
silently changing behavior in product code.
