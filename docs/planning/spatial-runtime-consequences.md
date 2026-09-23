# PLAN-ENG-5 — Spatial Runtime Consequences Delivery Plan

Status: Active delivery plan with an Engine research prerequisite. PLAN-ENG-5-0, PLAN-ENG-5-A1 (Factory V2 optional-record model/validation proving evidence), and fixed Engine semantics identity are implemented. The Factory composition question is concluded and reconciled; V2 publication identity and spatial runtime activation are dependency-blocked on the READY Engine applicability question.
Owner: Factory Simulation Engine Readiness
Parent plan: [Factory simulation engine readiness](factory-simulation-engine-readiness.md)

## 1. Purpose

PLAN-ENG-5 turns canonical Factory V2 spatial facts into deterministic transfer consequences in the
headless simulation runtime without moving design semantics into consumers or inventing a transport
network capability.

The architecture is fixed by:

- [Factory semantic-evolution contract](../architecture/factory-design.md#111-semantic-evolution);
- [Determinism Contract](../architecture/overview.md#determinism-contract);
- [Engine Semantics v1](../architecture/engine-semantics-v1.md), the normative first-version Engine interpretation;
- Accepted the runtime observation/event contract for supported observation/event state reconstruction and ordering.

### Remaining prerequisite — Engine applicability

The [Factory Model Semantic Composition Research](../research/investigations/factory-model-semantic-composition.md)
is concluded: the [Factory semantic-evolution contract](../architecture/factory-design.md#111-semantic-evolution)
composes policies as closed grammars with explicitly present optional records, and
[Factory Model v2](../architecture/factory-model-v2.md) is the reconciled, still unreleased grammar —
V1 production records plus one optional, complete spatial record. Factory composition no longer
blocks this plan.

Publication validity is not Engine applicability. [Engine Semantics v1](../architecture/engine-semantics-v1.md)
was specified against the same five spatial facts but states no applicability to a
`factory-model:v2` artifact without them, and its fixed definition is not amended by the Factory
reconciliation. Whether `engine-semantics:v1` can be stated as applicable to V2 artifacts, or a
distinguishable Engine identity must execute them, is the READY
[Engine applicability question](../research/investigations/engine-evolution.md#engine-applicability-to-optional-record-factory-policies).
Until it is reconciled:

- do not release the V2 fingerprint policy (PLAN-ENG-5-A2) or make spatial transfer reachable
  (PLAN-ENG-5-C3 onward);
- do not re-point, extend, or reinterpret `engine-semantics:v1` in implementation or documentation
  to accept V2 artifacts;
- keep any spatial substrate built earlier unreachable, and re-confirm it against the Engine identity
  the question selects before activation.

The landed V2 shape/validation and Engine-v1 conformance/identity work are retained as proving
evidence. Work outside this chain may continue where it does not assume an answer to the Engine
applicability question.

The first-release local-admission and shared-backlog-ranking questions in
[Engine Evolution Research](../research/investigations/engine-evolution.md) are concluded after
independent adversarial review, and both retain `engine-semantics:v1` unchanged. That earlier
dispatch gate made PLAN-ENG-5-0 executable; it does not make V2 publication or spatial activation
executable.

## 2. PLAN-ENG-5 semantic boundary

PLAN-ENG-5 preserves the ownership split:

```text
Factory model v2 optional spatial record, when present
    floor dimensions
    resource position + anchored footprint occupancy
    ticksPerCell
    handlingTicks

Engine semantics (v1 rules; executing identity for V2 artifacts still open)
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

PLAN-ENG-5 is v1-specific and preserves v1 selection/ranking and recovery semantics at that insertion
point. The concluded first-release research deliberately retains those rules with qualifications; it
does not claim they are globally optimal. Any future outcome-changing alternative still requires a new
Engine semantics identity and plan reconciliation rather than an in-place change to v1.

## 4. Delivery policy

PLAN-ENG-5 implementation is deliberately split into small semantic increments so a coding agent can
execute a well-bounded change without being asked to rediscover architecture while coding.

Every slice should have:

- one dominant semantic invariant;
- explicit executable evidence that closes that invariant;
- narrow production ownership and explicit non-goals;
- an escalation rule: if implementation evidence conflicts with the Factory semantic-evolution contract, the Determinism Contract, or
  `engine-semantics:v1`, stop and surface the contradiction rather than inventing new semantics.

This decomposition is provider-neutral. Repository architecture and acceptance evidence determine
whether a slice is sufficiently bounded; no particular model, agent, IDE, or hosted service is an
implementation dependency.

The vertical transfer-activation slice is intentionally not decomposed into separate state/event/
observation PRs. Once `TRANSFERRING` becomes reachable, supported state and supported deltas must be
coherent in the same landed change under the runtime observation/event contract.

## 5. Delivery slices

**Engine-applicability rule:** Unimplemented slices below that release `factory-model:v2` publication identity or make spatial runtime behavior reachable are dependency-blocked until the [Engine applicability question](../research/investigations/engine-evolution.md#engine-applicability-to-optional-record-factory-policies) concludes, receives its required adversarial review, and its Engine architecture/specification reconciliation lands.

### PLAN-ENG-5-0 — Pin existing Engine semantics

**Status:** Implemented. The first-release dispatch research gate is cleared: both reviewed questions
retain `engine-semantics:v1` unchanged, with the qualifications recorded in
[Engine Evolution Research](../research/investigations/engine-evolution.md).

**Prerequisites:** the Determinism Contract is adopted architecture, plus conclusion of the two first-release dispatch
questions with `engine-semantics:v1` retained unchanged. Those prerequisites are satisfied. A future
reopening that recommends an outcome-changing alternative does not rewrite this slice or v1 in place;
it requires a new Engine semantics identity through architecture/specification reconciliation and a
new plan decision for the changed interpretation.

**Responsibility**

Add characterization/conformance evidence for the result-affecting behavior that
`engine-semantics:v1` inherits from the pre-Gate-5 Engine:

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

There is a genuine tension here, and it is recorded rather than worked around. Adopted architecture
already makes those values part of `engine-semantics:v1`: §1.1's membership test covers derived-result
arithmetic, §10.1–§10.2 fix their edge cases *and* their accumulation, and §1.1 consequence 4 states
that a rule satisfying the membership test but absent from the specification is a defect in the
specification. Deferring these fixtures would therefore leave v1 knowingly under-specified, which the
accepted contract forbids. Pinning them is the honest action.

What pinning does **not** do is settle ownership. These fixtures characterize current behavior under
the accepted contract; they are not evidence that these values permanently belong in the supported
runtime observation. If the analytics research concludes they should move, that is a change to a
supported contract — requiring supersession or a new `EngineSemanticsVersion` through architecture
reconciliation — not a licence to edit v1 in place, and not something this slice pre-decides.

Two constraints follow for implementers:

1. do not cite these fixtures as evidence that the ownership question is closed;
2. do not harden these specific values into *new* outward contracts or DTOs while the research is
   open — see the KPI ownership note in
   [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md#3-current-implementation-queue).

`combinedQueueDepth` is deliberately listed separately and is **not** in this category: it is derived
but result-affecting, because its exact arithmetic decides assignment. It remains Engine semantics
regardless of the analytics outcome. Every other bullet in this slice — dispatch, acceptance,
scheduler, ranking, materialization, and spatial behavior — is authoritative and unaffected by that
research; none of it, nor Engine semantics identity, nor Factory V2 work, is gated on it.

**Evidence**

Pinned behavioral fixtures fail if any of those results change for identical explicit inputs. The
slice does not freeze incidental DTO or implementation shape.

The reconciled research adds these mandatory dispatch fixtures to the existing v1 conformance set.
Use semantic names rather than research candidate/sample labels.

**Local admission / recovery:**

- one offline concurrency-4 machine with eight queued 10-tick single-eligible jobs completes after
  80 ticks from recovery under v1; this pins the retained one-local-job recovery stage rather than a
  fill-to-capacity interpretation;
- a mixed recovery with M1 concurrency 4, M2 offline, two local M1-only 10-tick jobs and four shared
  `{M1,M2}` 3-tick jobs proves the sequence is local stage first and then shared fixpoint, while also
  proving that one local admission does not reserve all remaining recovery opportunity for local work;
- the routed regression pins a valid case with order completions `[14,29,29]` / makespan 29 under v1,
  against the rejected fill behavior's `[14,40,21]` / makespan 40;
- the objective-conflict case pins v1 at makespan/mean-order completion `93 / 69.75`, against the
  rejected fill behavior's `91 / 71.25`;
- eight jobs routed through `M1 concurrency 4, duration 10 -> M2 unary, duration 100` complete at 810
  under both interpretations, proving upstream capacity fill does not imply terminal improvement;
- preserve same-machine continuation precedence, local-before-shared ordering, FIFO local start
  order, queue-smaller-than-capacity recovery, repeated recovery, and equal-time completion ordering.

**Shared flexible-backlog ranking:**

- canonical overlap: `route-order: M1:5 -> {M1,M2}:1 -> M1:1`, `clock-order: M3:6`,
  `shared-order: {M1,M3}:100`, all machines unary. V1 produces `[106,6,105]`, mean 72.33, makespan 106;
  the rejected local-depth-only interpretation produces `[7,6,106]`, mean 39.67, makespan 106. The
  shared job must remain unbound and later reselect a different machine when the runtime state changes;
- one-variable mirror: use the same case but change only `shared-order` duration from 100 to 1. V1
  mean is 6.33 versus 6.67 under local-depth-only even though the current comparator's decisive input
  projection is unchanged. This prevents the canonical case from being encoded as evidence that local
  depth is a generally superior ranking;
- long-step mirror: `route-order: M1:5 -> {M1,M2}:100`, `clock-order: M3:1000`,
  `shared-order: {M1,M3}:10`, all machines unary. V1 produces `[105,1000,15]`, mean 373.33,
  makespan 1000; local-depth-only produces `[105,1000,115]`, mean 406.67, makespan 1000. Preserve this
  independently shaped reversal alongside the tighter one-variable mirror rather than treating either
  case as a substitute for the other;
- scarce-machine protection: `route-order: M1:5 -> {M1,M2}:60`, `clock-order: M3:500`, and two
  `shared-order` instances each eligible on `{M1,M3}` for 20 ticks. V1 completes `[65,500,25,45]`,
  mean 158.75; local-depth-only completes `[65,500,85,105]`, mean 188.75;
- makespan non-neutrality: use the canonical routes with concurrency 2 on M1/M2 and quantities
  `2 / 1 / 3`. V1 yields `[106,6,106]`, mean 72.67, makespan 106; local-depth-only yields
  `[7,6,107]`, mean 40.00, makespan 107;
- overlap multiplicity: one 100-tick shared job eligible on `{M1,M3,M4,M5}` while M3/M4/M5 are held
  for 500 ticks; preserve unbound waiting and reselection while the shared entry contributes to every
  compatible candidate's exact v1 key;
- recovery magnitude-sensitive corner: construct a ranking call with a recovered concurrency-4
  machine still accepting with local depth 3 under the retained one-local-job recovery stage,
  competing with a just-freed unary machine carrying two compatible pending shared entries. Exact v1
  keys are 3 versus 2 and choose the unary machine; a positive weight of 2 would produce 3 versus 4
  and choose the recovered machine. This pins that exact shared-entry magnitude matters in the
  retained recovery corner and prevents over-generalizing the usual binary-handover interpretation.

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

`PLAN-ENG-5-0` is characterization work with **two deliberate production changes**, both required to make the
normative arithmetic implementable at all:

1. **Mean-lead-time accumulation** currently sums completed-order lead times without an overflow
   check, which `engine-semantics:v1` section 10.2 rule 1 requires to saturate rather than wrap.
2. **`combinedQueueDepth`** currently narrows a 64-bit backlog count to 32 bits and adds it to a
   32-bit queue depth, which section 2 rule 3 forbids. Both terms are structurally bounded by their
   backing collections, so their true sum can exceed the 32-bit range while fitting a 64-bit
   accumulator with wide headroom: widen the sum and the ranking comparison rather than introducing
   any session-wide envelope.

Neither is a semantics change in any regime reachable today — each preserves current results wherever
the current arithmetic is already correct — and both are prerequisites for the exactness and
saturation rules to be implementable. Make them in production code and pin them; do not relax the
specification to describe wrapping or truncation, which would freeze an arithmetic defect into a
durable reproducibility contract and defeat the purpose of `EngineSemanticsVersion`. Every other
behavior in this slice is pinned as-is.

The executable evidence is maintained in `EngineSemanticsV1DispatchConformanceTest`,
`EngineSemanticsV1DerivedResultConformanceTest`, the existing session-control acceptance suite, and
the existing child-materialization acceptance/benchmark tests. These tests preserve retained v1
behavior as compatibility evidence; they do not claim that the dispatch policy is optimal or settle
the Engine-versus-analytics ownership question.

The behaviors above are the ones `engine-semantics:v1` section 1.1 requires to be versioned rather
than left as ambient implementation policy. `PLAN-ENG-5-0` is where that requirement becomes executable
evidence, so a fixture gap here is a semantics gap, not a coverage preference.

**Non-goals**

Engine-semantics identity types, Factory V2, transfer behavior, new scheduling policy.

### PLAN-ENG-5-A1 — Factory V2 spatial model and validation

**Status:** Implemented for the reconciled optional-record shape. `FactoryModelV2`
(`com.arcogine.factory.model.v2`) holds the production records as existing V1 concepts
(`ConfiguredResource`, `OperationDefinition`, `ProductDefinition`) plus an explicitly optional
`SpatialRecord` whose per-resource `ResourceLayout` entries reference resources by `MachineId`, and
`FactoryModelV2Validator` implements every validation predicate below. `FactoryModelV2` shares no
supertype with `FactoryModel`, so it cannot be passed to `FactoryModelPublisher.publish(FactoryModel)`
or used to construct a `FactoryModelVersion` -- a compile-time property, not a runtime guard --
which keeps V2 semantic content, with or without its spatial record, from traveling through the
`factory-model:v1` publication path. `factory-model:v2` canonical bytes, `ModelFingerprint`
derivation, and policy registration are not implemented; PLAN-ENG-5-A2 owns that work.

**Prerequisite:** the Factory semantic-evolution contract is adopted architecture.

**Responsibility**

Add the optional V2 spatial record and its validation, without yet releasing a V2 fingerprint
policy:

- an explicitly absent record that validates production semantics only and synthesizes nothing;
- a present record that is complete: one layout per configured resource, in resource-list order,
  with no unknown or repeated entry;
- floor width/height;
- minimum-coordinate resource reference cell;
- anchored footprint width/height and exact occupied-cell semantics;
- `ticksPerCell`;
- `handlingTicks`;
- floor containment/non-overlap;
- the exact maximum-transfer-duration representability predicate from Factory Model v2.

Do not claim that the publication predicate eliminates the pre-existing extreme-`SimTime.plus(...)`
overflow condition.

**Evidence**

Valid/invalid boundary cases prove absence, present-with-zero distinctness, coverage, anchor,
containment, overlap and arithmetic semantics independently of runtime transfer behavior.

**Non-goals**

Canonical V2 bytes/fingerprints, policy registration, V1→V2 migration, Engine behavior.

### PLAN-ENG-5-A2 — Factory V2 canonical identity

**Status:** Dependency-blocked on the Engine applicability question (see the rule above). The
Factory composition prerequisite is satisfied.

**Prerequisite:** PLAN-ENG-5-A1 plus reconciliation of the
[Engine applicability question](../research/investigations/engine-evolution.md#engine-applicability-to-optional-record-factory-policies);
if its answer changes the relative cost of the reconciled Factory boundary, weigh that composition
reopening trigger before release.

**Responsibility**

Release the `factory-model:v2` canonical policy exactly as specified by
[Factory Model v2 Canonicalization](../architecture/factory-model-v2.md), which is the normative
source of V2 fingerprint bytes:

- deterministic canonical encoding/decoding/verifying for V2 under that byte grammar, with the
  spatial record absent or present;
- `ModelFingerprint` derivation under the V2 policy;
- policy registration while preserving `factory-model:v1`, with a support declaration that states
  whether `factory-model:v1` publication continues for production-only designs;
- the V2 golden vectors that specification requires;
- no automatic V1→V2 lift/default synthesis and no V1 republication of V2 content with its spatial
  record dropped.

**Evidence**

The golden-vector set in that specification, including exact canonical bytes, the pinned
policy-domain prefix, spatial absent versus present with legal zero values, marker framing and
coverage rejection. V1 vectors/fingerprints remain byte-for-byte unchanged; equivalent V2 content
reproduces its fingerprint; every authored V2 fact and the record's presence participate in
identity; moving a resource changes the fingerprint without changing resource identity; a
grammar-valid artifact that violates a V2 publication predicate is rejected on decode.

**Non-goals**

Cross-policy controlled-revision migration/comparison, runtime transfers.

### PLAN-ENG-5-A3 — Multi-policy historical resolution and evolution seam

**Status:** Not started. Implement only when a real controlled transition between
`factory-model:v1` and `factory-model:v2` artifacts needs it.

**Prerequisite:** PLAN-ENG-5-A2. Governance PLAN-GOV-1 historical revision authority is already landed.

**Responsibility**

Make the first V1/V2 coexistence truthful:

- historical artifacts under both released Factory model policies remain resolvable/verifiable;
- controlled-revision lineage may cross policy versions without rewriting either artifact;
- a normal semantic `ChangeSet` does not silently invent V2 spatial facts for V1;
- implement only the narrow migration classification or common-representation seam actually needed
  for the first V1→V2 controlled transition; a common representation invents nothing and preserves
  every V1 distinction, and its equivalence is never fingerprint equality or reattribution.

**Evidence**

Historical V1 resolution remains intact after V2 registration, V2 resolves independently, and the
first cross-policy transition cannot be misreported as an ordinary same-policy empty/equivalent diff.

**Non-goals**

Generic migration/schema framework, transfer runtime behavior.

**Sequencing note:** PLAN-ENG-5-A3 does not block transfer implementation or PLAN-ENG-5 closure merely
because V2 exists. It must land before the first real cross-policy controlled transition.

### PLAN-ENG-5-B1 — Engine semantics identity and runtime establishment

**Status:** Implemented. `EngineSemanticsVersion` is a shared immutable Engine identity with one
supported current value, `engine-semantics:v1`. `FactoryRuntime` establishes and exposes that value
for its lifetime; fresh/reset runtimes receive new `RunId` values without changing the semantics
identity. Unsupported identities fail explicitly through the narrow support check.

**Prerequisites:** the Determinism Contract is adopted architecture and PLAN-ENG-5-0.

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

### PLAN-ENG-5-B2 — Runtime provenance propagation

**Prerequisite:** PLAN-ENG-5-B1.

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

**Convergence note:** outward consumer convergence is retired as a standing objective (see
[Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md#3-current-implementation-queue)); a future
outward adapter should consume this settled runtime provenance shape rather than introduce an envelope that
immediately needs revising.

### PLAN-ENG-5-C1 — Pure transfer arithmetic

**Prerequisite:** PLAN-ENG-5-A1 and the `engine-semantics:v1` transfer rule. The seam stays unreachable, and its
rule is re-confirmed against the Engine identity the applicability question selects before
PLAN-ENG-5-C3.

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
zero authored magnitudes, and overflow-safe behavior consistent with the Factory semantic-evolution contract.

**Non-goals**

Destination selection, reservations, scheduler integration, runtime transfer state.

### PLAN-ENG-5-C2 — Destination admission-reservation substrate

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
release/conversion and no regression of ordinary pre-Gate-5 processing semantics.

**Non-goals**

Public reservation aggregate, transfer timing/state/events, transport capacity.

### PLAN-ENG-5-C3 — Vertical transfer activation

**Prerequisites:** reconciliation of the Engine applicability question, PLAN-ENG-5-A2, PLAN-ENG-5-B2,
PLAN-ENG-5-C1 and PLAN-ENG-5-C2.

**Research recheck before activation:** inbound reservation changes what "can accept" means and was a
named reopening seam in both reviewed first-release dispatch questions. Before making the reservation
substrate reachable, prove that reservation-aware admission does not create a new healthy state where
an online machine has free processing capacity plus residual local work, or where more than one
accepting candidate can carry shared-demand pressure in a way the retained conformance model did not
cover. If it does, stop and reopen the bounded local-admission/ranking research instead of silently
extending the first-release rationale. This check does not reopen the questions merely because V2
model facts exist; it is triggered when reservation-aware runtime execution becomes reachable.

**Responsibility**

Activate the first coherent transfer path at the existing `handleTaskEnd` next-step seam:

- preserve PLAN-ENG-2 v1 selection timing/ranking and post-selection waiting paths;
- bind only when the selected destination is currently admissible;
- for a distinct resource, reserve admission capacity, compute/fix duration once, enter
  `TRANSFERRING`, and schedule completion;
- same-resource consecutive operations retain the no-transfer path;
- publish the minimum supported `TRANSFER_STARTED` / `TRANSFER_COMPLETED` deltas and expose the
  minimum in-flight observation facts in the same slice so no reachable authoritative state is
  invisible to the PLAN-ENG-4 contract;
- use only PLAN-ENG-4 sequence for same-time supported-event ordering.

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

### PLAN-ENG-5-C4 — Transfer edge semantics

**Prerequisite:** PLAN-ENG-5-C3.

**Responsibility**

Close the remaining v1 transfer rules without changing the core design:

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

The multi-eligible arrival-offline case is the load-bearing one: it is the only v1 state where a job
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

Late-join and observation/event-closure tests prove no internal store or scheduler/event-log replay is
required, and existing KPI meanings remain stable.

**Non-goals**

REST/SSE/UI projection, KPI redesign, retained supported-event history.

### PLAN-ENG-5-E — Headless PLAN-ENG-5 closure

**Prerequisites:** PLAN-ENG-5-B2, PLAN-ENG-5-C4 and PLAN-ENG-5-D, plus PLAN-ENG-5-A3 if a real cross-policy
controlled transition exists by then.

**Responsibility**

Add one decisive consumer-neutral proving scenario and extend the immutable fixture set of the Engine
semantics identity that executes V2 artifacts with the landed spatial behavior. Reconcile the parent PLAN-ENG-5
acceptance/status documentation without adding presentation/API dependencies.

**Required proving case**

The scenario demonstrates:

1. two otherwise equivalent V2 designs differing only in canonical placement produce different
   transfer duration/completion under the same Engine semantics identity;
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
8. V1 historical fingerprints/resolution remain unchanged, and neither a V1 design nor a V2 design
   without a spatial record receives synthesized spatial semantics;
9. behavioral conformance fixtures make a future different semantics version independently
   verifiable without requiring the initial runtime to execute two versions.

## 6. Dependency and parallelism map

```text
READY Engine applicability research --reconcile--> gates PLAN-ENG-5-A2 and PLAN-ENG-5-C3

CONCLUDED dispatch research --retain v1--> PLAN-ENG-5-0 v1 fixtures ---> PLAN-ENG-5-B1 semantics identity (implemented) ---> PLAN-ENG-5-B2 provenance ----+
                                                                                                                        |          |
PLAN-ENG-5-A1 V2 model/validation ---> PLAN-ENG-5-A2 V2 identity -------------------------------------------------------------+-> PLAN-ENG-5-C3 activation
       |                         |                                                                                        |
       |                         +--> PLAN-ENG-5-A3 policy evolution ---------------------------------------------------------+   v
       |                                                                                                             | PLAN-ENG-5-C4 edges
       +--> PLAN-ENG-5-C1 transfer arithmetic -----------------------------------------------------------------------------+ |   |
                                                                                                                   | |   v
PLAN-ENG-5-B1 semantics identity ---> PLAN-ENG-5-C2 admission reservation ----------------------------------------------------+ | PLAN-ENG-5-D closure
                                                                                                                     |   |
                                                                                                                     +--> PLAN-ENG-5-E
```

The first-release research gate is cleared. A future reopened investigation that recommends an
outcome-changing alternative does not retroactively mutate v1 or invalidate historical v1 fixtures;
it requires a new `EngineSemanticsVersion` and a separately reconciled implementation path.

Practical parallelism while the Engine applicability question is open:

- `PLAN-ENG-5-0`, `PLAN-ENG-5-A1`, and `PLAN-ENG-5-B1` are implemented and remain proving evidence;
- `PLAN-ENG-5-A2` is dependency-blocked on the Engine applicability question; `PLAN-ENG-5-A3` waits for a
  real cross-policy controlled transition;
- `PLAN-ENG-5-B2` is independent of the Factory boundary and of the applicability outcome: it propagates
  whichever Engine identity a runtime establishes and must not create a new outward compatibility promise;
- `PLAN-ENG-5-C1` and `PLAN-ENG-5-C2` may only build unreachable substrate; `PLAN-ENG-5-C3` through
  `PLAN-ENG-5-E` are not implementation-ready until the applicability question is reconciled;
- work outside this chain may proceed under its own plan when it does not assume an answer to the
  applicability question.

After the Engine applicability question concludes, receives its independent adversarial review, and its
reconciliation lands, re-resolve this dependency map before selecting the next PLAN-ENG-5 implementation
slice.
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

When Challenge attempts consume real Engine-produced results, compatibility must include
`EngineSemanticsVersion` wherever changed Engine semantics can affect compared outcomes. Synthetic
Challenge-only attempts do not block PLAN-ENG-5.

### Governance — REQUIRED WHEN CONSUMER INTEGRATES

Future Arcogine analytical evidence produced from simulation must retain `ModelFingerprint`,
`EngineSemanticsVersion`, and the explicit producing inputs/results required by the Governance evidence contract. Governance
consumes this provenance; it does not own Engine semantics.

### Operational — REQUIRED WHEN CONSUMER INTEGRATES

Future twin/reconciliation analytics retain Engine interpretation provenance independently of the durable operational identity and independently of subject correspondence. These answer different questions: which Engine interpretation produced a result; which accountable operational continuation a record belongs to; and which external and Arcogine subjects are authoritatively related. The Operational continuity contract is adopted and defines that identity's referent and rules, while deliberately deferring its final type name and representation; `EngineSemanticsVersion` and `RunId` remain Engine-owned and must never be derived from it, or it from them.

### A future outward transport migration — SEQUENCE IF AND WHEN ONE IS INTRODUCED

If a future outward adapter migrates supported events and observations outward, it should land after
PLAN-ENG-5-B2's final runtime provenance shape, to avoid immediate wire-contract churn. No such migration is
currently a prerequisite for headless PLAN-ENG-5, since outward consumer convergence is retired as a standing
objective (see [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md#3-current-implementation-queue)).

## 9. Acceptance / readiness

The landed PLAN-ENG-5 conformance, Factory V2 optional-record model/validation, and fixed
Engine-semantics identity work remain valid evidence under the current architecture. The earlier
first-release dispatch gate is closed for those already-landed v1 semantics, and the Factory
composition question is concluded and reconciled.

The remaining V2 publication and spatial-runtime path is **not implementation-ready** while the
Engine applicability question is open. The Factory semantic-evolution contract, the Determinism
Contract and Engine Semantics v1 remain authoritative; none of them makes `engine-semantics:v1`
applicable to `factory-model:v2` artifacts.

Before a held slice resumes, the Engine applicability question must reach a decision-quality
conclusion, receive its independent adversarial review, and its Engine architecture/specification
reconciliation must land. The plan must then be re-resolved against that result. If later activation
still reaches PLAN-ENG-5-C3, perform the explicit reservation-aware dispatch recheck there. Any slice
that encounters contradictory evidence must stop at that boundary rather than silently revising the
accepted contract in product code.