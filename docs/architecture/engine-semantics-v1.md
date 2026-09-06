# Engine Semantics v1

Status: Normative design contract; implementation pending
Semantic identity: `engine-semantics:v1`
Decision authority: [ADR-0015](decisions/0015-engine-semantics-identity-and-reproducibility.md)
Model-side counterpart: [ADR-0014](decisions/0014-factory-model-semantic-policy-evolution.md) and
[Factory Model v2 Canonicalization](factory-model-v2.md)

## 1. Purpose

`engine-semantics:v1` defines the complete result-affecting Engine interpretation that Arcogine
must attribute to a simulation run using this version. It records semantic rules, not Java class
shape, DTO serialization, build identity, or replaceable implementation algorithms.

The durable result inputs are:

```text
ModelFingerprint
+ EngineSemanticsVersion (`engine-semantics:v1`)
+ explicit workload
+ seed/random inputs
+ ordered external commands
```

A run fixes its Engine semantics version at establishment. The version never changes mid-run.

### 1.1 Completeness rule

This specification claims to identify the **complete** result-affecting Engine interpretation. That
claim is operational, not aspirational. The membership test is:

> A rule belongs to `engine-semantics:v1` if changing it can change the outcome — acceptance or
> rejection, assignment, ordering, timing, or derived result — for an identical `ModelFingerprint`,
> explicit workload, seed/random inputs, and ordered external commands.

Four consequences follow.

1. **No result-affecting limit may remain ambient implementation policy.** A hard-coded threshold,
   envelope, ceiling, or bound that deterministically decides acceptance, rejection, assignment,
   ordering, or timing is a semantic rule regardless of where it currently sits in the
   implementation. Every such limit is either
   - part of `EngineSemanticsVersion` and recorded in this specification, or
   - an explicitly identified reproducibility input recorded alongside `ModelFingerprint`, explicit
     workload, seed, and ordered commands.

   There is no third category. "It is only a guard", "it is an implementation detail", and "it is
   only reachable at extreme inputs" do not exempt a limit that two conforming implementations could
   choose differently and thereby disagree on a run's outcome. This covers derived-result arithmetic
   as much as acceptance limits: a saturation, clamp, or zero-denominator rule decides a supported
   observation's value and is therefore in scope, and so does the **accumulation** feeding that
   value, not only its edge cases. The currently known rules of this kind are recorded in the section
   that owns the behavior they bound — the child materialization envelope in section 3, the exact
   ranking arithmetic in section 2 rule 3, the derived-result edge cases in section 10.1, and the
   accumulator register in section 10.2.
2. **A rule is in scope even when the capability that motivated this version did not introduce it.**
   Pre-existing behavior that satisfies the membership test is captured here rather than left
   implicit because it predates spatial transfer work. Sections 1.2, 2, 3 and 4 exist for exactly
   that reason.
3. **Recording a previously unwritten rule is a correction, not a semantics change**, provided the
   rule's behavior is unchanged. Changing the behavior requires a new Engine semantics version.
4. **A rule that satisfies the membership test but is absent here is a defect in this document**,
   not a licence to treat the behavior as unversioned. The correct response is to record it, or to
   promote it to an explicitly identified reproducibility input — not to leave it ambient.

### 1.2 Session and control semantics, in scope by reference

The consumer-neutral session/control behavior satisfies the section 1.1 membership test: it decides
how far a session advances and whether an externally initiated change is applied, for an identical
ordered command sequence. It is therefore part of the `engine-semantics:v1` interpretation, and two
implementations may not claim this version while differing on it.

[ADR-0007](decisions/0007-consumer-neutral-session-control-primitives.md) is Accepted and is the
normative authority for those rules. This specification adopts them **by reference** rather than
restating them. In scope:

- `advance()` as the unchanged one-event primitive, and `advanceUntil(targetTime, maxEvents)`
  defined in terms of it — processing events one at a time in `advance()` order and stopping as soon
  as either bound is reached, returning every event actually processed, in order. Because
  `advanceUntil` is defined as a loop over `advance()`, the two cannot diverge in event ordering or
  dispatch behavior.
- `reset()` as a fresh session over the same retained model version: replaying an identical command
  sequence reproduces an identical ordered event stream and identical terminal state, and the
  original session is left untouched.
- The `CommandResult` outcome contract for the two externally initiated commands, `submitWorkload`
  and `setMachineAvailability` — `Accepted`; `Rejected` with its zero-mutation guarantee and
  structurally empty scheduled-event list; and `Faulted` for a command whose requested change was
  genuinely applied before the runtime failed while carrying out the resulting work. Acceptance and
  execution outcome are independent facts, so `Faulted` is not a variant of `Rejected`.

Adoption by reference is deliberate. ADR-0007 is already normative and its rules are proven by
`SessionControlAcceptanceTest`; restating them here would create two independently editable
statements of one contract, which is exactly the drift this document exists to prevent. The
consequence is unchanged: a change to any rule above is a change to `engine-semantics:v1` and
requires a new Engine semantics version, even though its text lives in ADR-0007.

## 2. Resource-selection and dispatch semantics

Spatial transfer semantics do not redefine the existing resource-selection and dispatch behavior.
The following rules are part of v1:

1. Start from the routing step's authored eligible-machine set. Prefer online machines by filtering
   out `Offline` resources when at least one eligible machine is online. If every eligible machine
   is offline, retain the full eligible set so selection remains deterministic and the job enters
   the existing waiting/recovery path rather than becoming unselectable.
2. Within the resulting candidate set, `canAcceptJob()` is the primary **ranking key**, not an
   eligibility filter: a machine that can accept immediately ranks ahead of one that cannot.
3. Among candidates tied on immediate acceptance, rank by `combinedQueueDepth` as currently defined
   by the Factory Engine, including pending multi-eligible work that could land on that resource.
   `combinedQueueDepth` is an **exact** sum of that machine's own queue depth and the count of
   compatible multi-eligible entries. Unlike the derived-result accumulators of section 10.2 it may
   not saturate, narrow, or approximate: it is a ranking key, so a wrapped or truncated value would
   change which machine is selected and therefore change assignment, not merely a reported number.

   Exactness here is **total, and requires no session-wide envelope**. Each term is structurally
   bounded by the size of the collection holding it, so their true sum cannot exceed twice that
   bound. The obligation is therefore only that the sum be accumulated and compared at a width that
   holds it — never that a run be restricted to keep it representable. The concrete failures this
   rule forbids are a narrowing conversion of either term and a same-width addition of two
   independently bounded terms. Section 3's per-submission materialization envelope is unrelated: it
   bounds one submission's children, not a session's cumulative waiting set, and must not be cited
   as the reason this sum is representable.
4. Break a remaining tie deterministically by `MachineId`.
5. Selection runs at the established semantic points even when the selected machine cannot yet
   accept the job. The post-selection branch determines immediate dispatch versus
   `pendingMultiEligible` versus the selected single-machine queue.
6. Recovery after a machine/step completion preserves the current cascade order: attempt that
   machine's own queue first, then reconsider pending multi-eligible work. The per-machine attempt
   starts at most one job (see section 4); it does not drain the queue to capacity. Within the
   multi-eligible backlog, the earliest queued entry that can actually be placed is dispatched
   first, with repeated re-selection after each placement.
7. **Which waiting path a job enters is determined by its effective eligible set at the moment it
   enters the waiting state.** Before any destination is bound, the effective eligible set is the
   routing step's authored eligible-machine set: when the selected machine cannot currently accept
   the job, a step with more than one eligible machine enters the shared multi-eligible backlog, and
   a step with exactly one eligible machine enters that single machine's own queue.

   Destination binding collapses the effective eligible set to the single bound destination (section
   6 rule 9). A **bound** job that must wait therefore has an effective eligible set of size one and
   waits in the bound destination's own queue, whatever the authored step's eligible-set size was.
   That is what keeps this rule consistent with immutable binding: a bound job must never re-enter
   the shared multi-eligible backlog, because reconsideration there re-runs selection over the
   authored eligible set and could place the job on a different machine — a reroute the transfer
   contract forbids.

   So a multi-eligible step never occupies a per-machine queue **while waiting pre-binding**, and a
   single-eligible step never occupies the shared backlog. Post-binding waiting is the deliberate
   and only exception, and it is governed by section 6 rule 9 and section 9 rule 8.
8. **Each machine's own queue is strict FIFO in arrival order.** Entries are appended at the tail on
   enqueue and taken from the head on dispatch. Queue position is not re-derived from job identity,
   ordinal, order identity, step duration, remaining steps, waiting time, or priority; v1 has no
   priority, aging, or reordering rule for per-machine queues.
9. **The shared multi-eligible backlog holds pre-binding work only**, and retains arrival order. An
   entry's eligible set is the one captured when it was enqueued. Reconsideration re-runs selection
   over that captured set against current machine state, so an entry may be placed on a machine
   other than the one selected when it first waited; see section 4 for the scan order. Because that
   reselection can change the destination, a job that has already bound a destination is never
   placed in this backlog.
10. Spatial transfer time is inserted only after a concrete destination is both selected and
    currently admissible under the same acceptance rule that otherwise permits immediate processing;
    transfer semantics do not move selection earlier or redefine ranking.

Changing any of those result-affecting rules requires a new Engine semantics version.

## 3. Unit-work decomposition semantics

The existing unit-work decomposition rules remain part of v1:

1. Workload quantity `N` decomposes into `N` independently dispatchable `JobId` children.
2. Child creation/release and initial dispatch use deterministic ordinal ordering.
3. Aggregate completion continues to correlate explicit `OrderId` with the completing child
   `JobId`.
4. **The supported child-materialization envelope is part of `engine-semantics:v1`.** Workload
   submission accepts `1 <= N <= 100000` and rejects anything outside that closed interval as an
   out-of-range explicit input. The bound is a flat count of children; it does not vary with routing
   step count, resource count, or any other model content.
5. Rejection under rule 4 is total and occurs **before any runtime mutation**: no `Order`, no child
   `Job`, no queue entry, and no scheduled event exists after a rejected submission. A rejected
   submission is therefore not a partial run with a smaller workload.

Rule 4 is recorded here rather than left as an implementation guard because it satisfies the section
1.1 membership test directly: for an identical model, seed, and command sequence, a workload of
`100001` is deterministically rejected and a workload of `100000` is deterministically accepted and
executed. Two implementations choosing different envelopes would disagree about whether a run
happens at all, which is the strongest possible outcome difference. The envelope is consequently
neither an ambient policy nor a free implementation choice; changing the accepted interval requires
a new Engine semantics version, and a deployment that needs a different interval must expose it as
an explicitly identified reproducibility input rather than silently widening or narrowing v1.

A change that can alter assignment, ordering, or completion outcome for identical explicit inputs
requires a new Engine semantics version.

## 4. Scheduler and dispatch-cascade ordering

1. Authoritative scheduled work is ordered first by `SimTime`.
2. Equal-time scheduled work uses the existing deterministic insertion-order tie-break.
3. Internal scheduler markers that do not represent authoritative Factory state changes do not gain
   semantic significance merely because they are present in the implementation.
4. Supported runtime-event ordering at the same `SimTime` is represented only by the monotonic
   supported-event sequence established by ADR-0011; spatial transfer semantics introduce no second
   event-ordering mechanism.
5. The intra-handler recovery cascade is also semantic ordering where it changes assignment:
   after a completed step releases a machine, the Engine first attempts to dispatch that machine's
   own queued work and only then reconsiders `pendingMultiEligible` work. A change to that ordering
   can change assignments and therefore requires a new Engine semantics version.
6. The cascade runs at the same two trigger points and in the same shape for both of them: a step
   completion that releases a machine, and a machine coming back online. Taking a machine offline
   runs no cascade.
7. **The per-machine stage of the cascade dispatches at most one queued job per trigger.** If the
   released machine can accept work, exactly one head-of-queue entry is started; the machine is not
   drained to capacity in one pass. Remaining queued work waits for the next trigger.
8. **The multi-eligible stage runs to fixpoint, scanning from the head each pass.** One pass walks
   the backlog in arrival order, re-running selection over each entry's captured eligible set, and
   dispatches the first entry whose selected machine can currently accept it. After a placement the
   scan restarts from the head, because that placement changes queue and active state and can change
   subsequent selections. The stage ends when a complete pass places nothing.
9. **An unplaceable entry does not head-of-line block the backlog.** A pass skips entries whose
   selected machine cannot accept and continues to later entries, so an entry waiting on a disjoint
   eligible set is still reachable. Arrival order is therefore decisive only among entries that are
   simultaneously placeable, which is exactly the case where it can change assignment.

Rules 7, 8 and 9 satisfy the section 1.1 membership test — each of them can change which job lands on
which machine at which time for identical explicit inputs — and are recorded here for that reason,
not because spatial transfer introduced them.

## 5. Spatial model/Engine ownership boundary

Spatial transfer semantics apply this boundary:

- authored plant facts belong to the canonical Factory model and `ModelFingerprint`;
- result-affecting rules for interpreting those facts belong to `engine-semantics:v1`;
- authoritative facts that exist only during one run are runtime state;
- replaceable mechanisms preserving the same semantic outcomes are implementation details.

The V2 model facts consumed by this specification are floor dimensions, resource position,
resource footprint, `ticksPerCell`, and `handlingTicks`, as fixed by ADR-0014 and canonicalized by
[Factory Model v2 Canonicalization](factory-model-v2.md). This specification consumes those facts;
it never defines their canonical encoding or identity.

## 6. Destination selection and binding

The v1 rule is **transfer begins when the selected destination becomes admissible for binding under
the existing acceptance semantics**.

1. On completion of operation `k`, the source resource releases processing capacity exactly as it
   does without spatial transfer delay.
2. Existing resource selection/ranking runs at the established semantic point, including
   deterministic selection of a non-accepting machine when that is the current outcome.
3. If the selected machine cannot currently accept the job, the job remains in the existing waiting
   path (`machine queue` or `pendingMultiEligible`) and the existing recovery cascade controls when
   selection/placement is retried.
4. When the selected resource can accept the job, that concrete destination is bound.
5. Binding consumes one unit of the destination's **admission capacity** for the inbound job. This
   reservation contributes to whether subsequent `canAcceptJob` decisions have free capacity, but
   the inbound job is not yet active processing and is not yet queued work.
6. Transfer duration is computed and fixed at that binding instant.
7. `TRANSFER_STARTED` is emitted for the authoritative transition into the in-flight state.
8. Destination binding is immutable after transfer start. V1 does not reroute.
9. **Binding collapses the job's effective eligible set to the bound destination** for every
   subsequent waiting decision. From binding until the job begins processing there, the bound
   destination is the only machine the job can be placed on, regardless of how many machines the
   authored routing step listed as eligible. If the job must wait after arrival, it waits in that
   destination's own queue — appended at the tail in arrival order like any other queued entry —
   and never in the shared multi-eligible backlog. Section 9 rule 8 fixes the arrival-offline case
   this exists for.

   This rule is what makes immutable binding implementable. Without it, a multi-eligible step
   arriving at an offline destination would have no waiting path: the shared backlog would re-select
   and could reroute, contradicting rule 8, while the destination's own queue would contradict the
   pre-binding form of section 2 rule 7. Collapsing the effective eligible set resolves both by
   making the bound job a single-eligible job for waiting purposes.

This changes only the delay between destination binding and next-step processing; it does not change
resource-selection/ranking semantics.

## 7. Transfer timing

For distinct source and destination resources:

```text
manhattanDistance = abs(xDestination - xSource) + abs(yDestination - ySource)
transferDuration = handlingTicks + (ticksPerCell * manhattanDistance)
```

Semantic rules:

1. Resource positions are integer reference cells defined by ADR-0014 as the minimum-coordinate
   cells of their footprints.
2. Distance is Manhattan distance between those reference cells.
3. Resource footprint does **not** affect v1 transfer distance. Footprint remains canonical Factory
   content for publication/layout validation and future spatial semantics.
4. `handlingTicks` is applied once per transfer, not once per endpoint.
5. Arithmetic is integer throughout. V1 has no floating-point distance and no rounding rule because
   none is required by the chosen metric.
6. Factory V2 publication validation must prove the exact maximum-duration predicate defined by
   ADR-0014: `(W - 1) + (H - 1)` and
   `handlingTicks + ticksPerCell * maxManhattanDistance` must be representable with overflow-safe
   arithmetic in the runtime duration type. This guarantees representability of the derived
   transfer duration; it does not guarantee that `currentSimTime + transferDuration` is
   representable at an arbitrarily extreme current time. The existing runtime time-addition guard
   remains responsible for that condition.
7. Distinct resource footprints may not overlap. Because each reference cell lies inside its own
   footprint under ADR-0014, distinct valid resources cannot share the same reference cell, so a
   zero-distance inter-resource transfer is not a supported v1 state.
8. Consecutive operations on the **same resource** perform no transfer at all: no transfer state,
   no duration, and no transfer events.
9. A V2 design with `ticksPerCell = 0` and `handlingTicks = 0` therefore preserves the completion
   timing that would occur without spatial transfer delay while still exposing the authoritative
   transfer start/completion transitions for distinct resources.

Changing metric, endpoint interpretation, handling application, arithmetic/rounding, zero-distance
semantics, or same-resource behavior requires a new Engine semantics version.

## 8. Transfer runtime state machine

Only two new authoritative semantic moments are introduced.

```text
PROCESSING step k
  -> JOB_STEP_COMPLETED

if order complete:
  -> ORDER completion path
else:
  existing destination selection/ranking

  if selected destination cannot currently accept:
      -> existing WAITING state/path
      -> retry under existing dispatch cascade when capacity/availability changes

  if selected destination == source and can accept:
      -> no transfer
      -> next processing start under existing dispatch semantics

  if selected destination != source and can accept:
      -> reserve one unit of destination admission capacity
      -> TRANSFER_STARTED at current SimTime
      -> TRANSFERRING until fixed completion time
      -> TRANSFER_COMPLETED at start + transferDuration

      if destination online at arrival:
          -> convert reservation into active processing
          -> next processing start / JOB_DISPATCHED under existing semantics
      else:
          -> convert the inbound reservation into a queue entry on the BOUND destination
             (tail, arrival order) -- never the shared multi-eligible backlog
          -> JOB_WAITING, still bound, no rerouting
          -> released by the existing recovery cascade when that destination is online
```

`TRANSFERRING` is a runtime state between two moments, not a third event/moment. There is no separate
"destination selected", "transfer pending", "destination queued while in flight", or
"next-operation-dispatched" transfer moment beyond the existing authoritative transitions.

## 9. Capacity and availability semantics

1. The source releases its processing capacity at step completion exactly as it does without
   spatial transfer delay.
2. A transferring job consumes **destination admission capacity only** from binding until arrival.
   That reserved inbound capacity counts when deciding whether the destination can admit additional
   work, but the job is not in the destination's active-processing set and does not make the machine
   `Busy` merely because it is in flight.
3. V1 invents no transport resource/capacity, conveyor scheduler, physical buffer, or congestion
   model.
4. Admission reservation must be distinguishable from both active processing and queued work. It
   need not be a new public resource abstraction; it is runtime bookkeeping required to preserve
   the semantic distinctions already fixed here.
5. Source going offline after departure has no effect on the in-flight transfer.
6. A destination holding only inbound reservations may still be taken offline under the existing
   command contract because no reserved job has begun active processing. Going offline after
   transfer start does not change the bound destination or fixed completion time.
7. Another eligible destination becoming preferable after departure does not cause rerouting.
8. If the destination is offline at exact arrival time, `TRANSFER_COMPLETED` still occurs at the
   fixed time. The inbound admission reservation is then **converted into an entry in that
   destination's own queue** — the bound-destination wait fixed by section 6 rule 9 — and the job
   reports `JOB_WAITING`. It remains bound to that destination, never enters the shared
   multi-eligible backlog, and is not rerouted. The existing recovery cascade releases it when the
   destination comes back online.

   From arrival onward the entry is ordinary queued work, which resolves its observation
   consequences unambiguously: it **counts in the destination's queue depth**, it is **no longer
   held as reserved admission capacity** (the reservation is converted, not additionally retained,
   so one waiting job never consumes two units of the same destination's capacity), and it is not an
   active job until processing starts. This is the one case in v1 where a job whose authored routing
   step listed several eligible machines legitimately occupies a per-machine queue.

## 10. KPI and resource-observation interpretation

Spatial transfer semantics do not redefine existing metric formulas:

- `busyTicks` / utilization remain processing-time measures; transfer time does not accrue resource
  busy ticks merely because destination admission capacity is reserved;
- queue depth remains the existing count of queued/waiting work and does not count an in-flight job
  as a destination queue entry;
- backlog continues to count incomplete accepted orders;
- lead time naturally includes transfer delay because completion occurs later;
- throughput remains completed orders per observed time.

A reserved inbound job also does **not** appear in the destination resource projection's
`activeJobIds`, because active jobs are jobs that have actually begun processing. A destination
holding only inbound reservations therefore keeps the processing-oriented machine state it would
otherwise have (`Idle` when it has no active processing, or `Busy` only because of other active
jobs). Supported resource observation must nevertheless expose enough capacity information for its
existing bottleneck/capacity interpretation to account for admission capacity consumed by inbound
reservations; it must not silently count the in-flight job as processing or queue depth. The exact
projection field used for this admission-load fact is an implementation/API-shape choice, not a
change to the semantic distinction.

Bottleneck interpretation must therefore widen: a destination can have constrained admission
capacity because of transfer-bound work while not yet accruing processing busy ticks. Consumers must
not misdiagnose that state as ordinary processing utilization.

### 10.1 Derived-result arithmetic

The arithmetic that produces supported derived results is itself result-affecting under section 1.1:
two implementations computing these differently would report different supported observations for an
identical model, workload, seed and ordered command sequence. These rules are part of v1.

1. **Cumulative `busyTicks` saturates.** A resource's cumulative busy time is credited at step
   completion by adding the finished step's duration. Because durations are non-negative, a negative
   sum can only be signed 64-bit overflow, so the accumulator saturates at the maximum representable
   tick value rather than wrapping. Utilization derived from a saturated `busyTicks` is pinned at
   its maximum rather than becoming negative or nonsensical.
2. **Elapsed-time subtraction saturates at zero.** Subtracting one simulated time from another
   yields zero when the subtrahend is the greater, never a negative interval. Lead time and every
   other interval-derived measure therefore has a floor of zero by construction.
3. **Throughput over a zero-tick window is zero.** Throughput is completed orders per observed tick;
   an observation window of zero ticks yields `0`, not a division by zero, an infinity, or a NaN.
4. **Mean lead time over an empty completed set is zero.** With no completed orders the mean is `0`,
   not a NaN from a zero denominator.
5. Simulated-time **addition is not saturating.** It is unchecked, and the pre-existing runtime
   time-addition guard rejects an overflowing schedule at the command boundary instead of silently
   producing a wrapped time — see section 7 rule 6 and ADR-0007's zero-mutation rejection contract
   adopted by section 1.2.

Rules 1 to 4 are deliberate total-function choices at domain edges, not incidental defensive coding:
each replaces an undefined, wrapped, or non-finite value with a defined one a consumer can interpret.
Rule 5 is the deliberate exception, and it is a rejection rule rather than a value rule. Changing any
of them changes supported derived results for identical explicit inputs and therefore requires a new
Engine semantics version.

### 10.2 Derived-result accumulator register

Section 10.1 fixes edge-case *values*. This register fixes the **accumulation** behind each supported
derived result, because an accumulator's overflow policy changes the reported value for identical
explicit inputs exactly as a zero-denominator rule does. Sentinel cases and accumulators are two
halves of one contract; specifying only the first leaves the second ambient.

| Supported derived result | Accumulator | v1 accumulation rule |
|---|---|---|
| resource `busyTicks` / utilization | per-resource cumulative tick sum | saturating (rule 1 below) |
| mean lead time | sum of completed-order lead times ÷ completed count | saturating (rule 1 below) |
| throughput | completed-order counter ÷ elapsed ticks | exact counting (rule 2 below) |
| backlog | count of incomplete accepted orders | current-state count, not a running accumulator |
| completed sales value | running total of completed order value | deterministic by completion order (rule 3 below) |

1. **Every tick-valued accumulator saturates; none wraps.** This extends section 10.1 rule 1 beyond
   `busyTicks` to mean-lead-time accumulation: summing completed-order lead times saturates at the
   maximum representable tick value and never wraps to a negative or small positive total. A wrapping
   accumulator would not merely lose precision — it would report a qualitatively wrong mean, and two
   implementations differing only in accumulator width would disagree on a supported observation.
2. **Counting accumulators are exact.** The completed-order counter increments once per completing
   order and is exact for every run that can exist: each increment corresponds to a distinct
   materialized child job, so the count stays far below the 64-bit range however many
   `submitWorkload` commands a session issues. Its exactness rests on the counter's width, not on
   section 3's per-submission envelope — that envelope bounds one submission's children and says
   nothing about a session's cumulative completions, so it must not be cited as the justification.
3. **Value accumulation is deterministic by completion order.** Completed order value accumulates
   once per completing order, in the deterministic completion order this specification already fixes.
   Because floating-point addition is not associative, that fixed order is what makes the running
   total reproducible; an implementation may not reorder, batch, or parallelise the accumulation. It
   saturates toward infinity rather than wrapping.
4. **Conversion to the reported representation is IEEE 754 round-to-nearest.** Converting an integral
   accumulator to the reported floating-point value is exact below the mantissa limit and
   round-to-nearest above it — deterministic across conforming implementations either way, so no
   further rounding rule is required.

**Implementation obligation.** Rule 1's saturation requirement is satisfied today for `busyTicks` but
**not** for mean-lead-time accumulation, which currently sums lead times without an overflow check.
That is a real gap between this contract and shipped behavior. The implementation slice that pins
existing semantics must make the accumulation saturating and prove it, rather than relaxing this
specification to describe wrapping. Specifying wrap-around would freeze an arithmetic defect into a
durable reproducibility contract, which is the opposite of what `EngineSemanticsVersion` exists to
guarantee.

## 11. Supported job and runtime observation contract

A fresh supported observation must reconstruct an in-progress transfer without replay.

Spatial transfer support extends the existing per-job projection rather than creating a parallel
transfer authority. For an in-flight job it exposes, at minimum:

- job status `TRANSFERRING`;
- `JobId` and existing order/step correlation;
- source resource identity;
- bound/current destination resource identity;
- `transferStartedAt`;
- `transferCompletesAt`.

The job projection and resource projection must agree: the job is `TRANSFERRING`; the destination is
bound and has one unit of admission capacity reserved; the job is not yet an active job and is not
in destination queue depth until arrival changes it into processing or waiting state.

Authoritative current coordinates, progress fraction, distance, and frame-by-frame movement are not
part of the supported runtime state. A consumer may derive visual interpolation from authoritative
start/completion times and canonical resource placement.

Runtime observation metadata carries once per observation:

- `RunId`;
- `ModelFingerprint`;
- mandatory `EngineSemanticsVersion`;
- optional `ControlledRevisionId` only when authoritatively bound;
- the existing supported-event sequence, time, and run-state metadata.

## 12. Supported transfer events

Both events are required because they describe distinct authoritative state changes.

### `TRANSFER_STARTED`

Carries enough supported facts to identify the in-flight relation and its fixed completion:

- `JobId`;
- `OrderId`;
- source resource identity;
- destination resource identity;
- `transferCompletesAt`;
- affected references for job, order, source, and destination.

### `TRANSFER_COMPLETED`

Carries:

- `JobId`;
- `OrderId`;
- destination resource identity;
- affected references for job, order, and destination.

The envelope also carries existing run/model/revision provenance plus mandatory
`EngineSemanticsVersion`.

At a shared `SimTime`, ordering uses only the supported runtime-event sequence. A zero-duration
transfer is still a scheduled completion turn, so the authoritative chain can be:

```text
JOB_STEP_COMPLETED
-> TRANSFER_STARTED
-> TRANSFER_COMPLETED
-> JOB_DISPATCHED
```

or end in `JOB_WAITING` when the destination is offline at arrival. `TRANSFER_STARTED` is published
only after the transition into `TRANSFERRING` succeeds; completion occurs on the separately
scheduled same-time turn, so supported events continue to describe authoritative changes that have
already happened.

## 13. Deliberately deferred spatial/runtime capabilities

V1 does not define:

- authoritative intermediate transfer coordinates/progress;
- rerouting;
- transport-resource capacity or transport scheduling;
- paths, aisles, conveyors, obstacles, graphs, or pathfinding;
- congestion;
- resource orientation as a transfer input;
- animation authority.

Those require a new Engine semantics version, a new Factory model policy, or both only when their
actual ownership and result-affecting meaning become concrete.

## 14. Conformance fixtures

Before `engine-semantics:v1` is considered released, pinned behavioral fixtures must prove the
normative semantics above using representative explicit inputs. The fixtures must cover at least:

1. current deterministic resource-selection behavior, including offline filtering/all-offline
   fallback, `canAcceptJob` ranking, `combinedQueueDepth`, `MachineId` tie-breaking, and the
   queue-before-`pendingMultiEligible` recovery cascade;
2. current unit-work decomposition/release ordering, plus the child-materialization envelope: `1`
   and `100000` accepted, `0` and `100001` rejected, and a rejected submission leaving no `Order`,
   child `Job`, queue entry, or scheduled event behind;
3. current scheduler same-time ordering;
4. per-machine queue FIFO order, proved by a dispatch sequence whose FIFO result differs from every
   plausible alternative ordering — arrival order must be distinguishable from `JobId` order,
   ordinal order, and step-duration order — plus the one-dispatch-per-trigger rule for a machine
   that has free capacity and more than one queued job;
5. pre-binding waiting-path selection by eligible-set size: a multi-eligible step waiting in the
   shared backlog and never in a per-machine queue, and a single-eligible step waiting in that
   machine's queue and never in the backlog;
6. multi-eligible backlog ordering: two simultaneously placeable entries competing for one machine
   resolving in arrival order; an unplaceable head entry not blocking a later placeable entry with a
   disjoint eligible set; an entry placed on a machine other than the one selected when it first
   waited; the fixpoint rescan placing more than one entry per trigger; and the
   queue-before-backlog cascade order at both trigger points, step completion and machine coming
   back online;
7. distinct-resource transfer with nonzero distance;
8. same-resource consecutive steps with no transfer;
9. zero authored transfer magnitudes and the same-time transfer event chain;
10. destination becoming unavailable after binding, arrival while offline, immutable binding, and no
    rerouting;
11. the bound-destination wait for a **multi-eligible** step: a job whose authored step lists several
    eligible machines, bound to one destination, arriving while that destination is offline — it
    waits in the bound destination's own queue, is absent from the shared multi-eligible backlog,
    is still bound to the same destination after another eligible machine becomes free and the
    recovery cascade runs, counts in that destination's queue depth, and no longer counts as
    reserved admission capacity;
12. session/control rules adopted by section 1.2: `advanceUntil` converging with looping `advance()`
    under both bounds, reset-session reproduction, and the `Accepted`/`Rejected`/`Faulted` outcome
    shapes with `Rejected`'s zero-mutation guarantee — satisfied by ADR-0007's existing
    `SessionControlAcceptanceTest` rather than duplicated here;
13. agreement between job transfer observation and destination resource admission-load observation;
14. deterministic event-type/entity-reference ordering and simulated times;
15. the section 10.1 derived-result arithmetic: `busyTicks` saturating at the maximum representable
    tick value instead of wrapping negative, elapsed-time subtraction flooring at zero, zero-tick
    throughput and empty-set mean lead time each yielding `0` rather than a non-finite value, and
    simulated-time addition overflow being rejected at the command boundary with no partial
    mutation rather than saturating;
16. the section 10.2 accumulator rules: **non-empty** mean-lead-time accumulation saturating rather
    than wrapping when the summed lead times exceed the representable tick range, exact
    completed-order counting across many submissions rather than one large one, and completed-value
    accumulation reproducing bit-identically under the fixed completion order;
17. section 2 rule 3 ranking exactness: a `combinedQueueDepth` whose two terms sum beyond the 32-bit
    range selects the same machine an exact sum would, proving neither the narrowing conversion nor
    a same-width addition survives — and proving it without appealing to section 3's per-submission
    envelope, which does not bound the cumulative waiting set;
18. terminal state / derived result agreement for repeated identical explicit inputs.

Fixtures pin semantic outcomes, not DTO/JSON bytes, transport representation, or unrelated
non-behavioral observation fields.