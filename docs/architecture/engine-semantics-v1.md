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
   choose differently and thereby disagree on a run's outcome. The currently known limits of this
   kind are recorded in the section that owns the behavior they bound — see the child
   materialization envelope in section 3.
2. **A rule is in scope even when the capability that motivated this version did not introduce it.**
   Pre-existing behavior that satisfies the membership test is captured here rather than left
   implicit because it predates spatial transfer work. Sections 2, 3 and 4 exist for exactly that
   reason.
3. **Recording a previously unwritten rule is a correction, not a semantics change**, provided the
   rule's behavior is unchanged. Changing the behavior requires a new Engine semantics version.
4. **A rule that satisfies the membership test but is absent here is a defect in this document**,
   not a licence to treat the behavior as unversioned. The correct response is to record it, or to
   promote it to an explicitly identified reproducibility input — not to leave it ambient.

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
4. Break a remaining tie deterministically by `MachineId`.
5. Selection runs at the established semantic points even when the selected machine cannot yet
   accept the job. The post-selection branch determines immediate dispatch versus
   `pendingMultiEligible` versus the selected single-machine queue.
6. Recovery after a machine/step completion preserves the current cascade order: attempt that
   machine's own queue first, then reconsider pending multi-eligible work. The per-machine attempt
   starts at most one job (see section 4); it does not drain the queue to capacity. Within the
   multi-eligible backlog, the earliest queued entry that can actually be placed is dispatched
   first, with repeated re-selection after each placement.
7. **Which waiting path a job enters is determined by the eligible-set size of the step it is
   waiting for, not by why it is waiting.** When the selected machine cannot currently accept the
   job, a step with more than one eligible machine enters the shared multi-eligible backlog, and a
   step with exactly one eligible machine enters that single machine's own queue. A multi-eligible
   step therefore never occupies a per-machine queue while waiting, and a single-eligible step never
   occupies the shared backlog.
8. **Each machine's own queue is strict FIFO in arrival order.** Entries are appended at the tail on
   enqueue and taken from the head on dispatch. Queue position is not re-derived from job identity,
   ordinal, order identity, step duration, remaining steps, waiting time, or priority; v1 has no
   priority, aging, or reordering rule for per-machine queues.
9. **The shared multi-eligible backlog also retains arrival order**, and an entry's eligible set is
   the one captured when it was enqueued. Reconsideration re-runs selection over that captured set
   against current machine state, so an entry may be placed on a machine other than the one selected
   when it first waited; see section 4 for the scan order.
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
          -> release/convert the inbound reservation as required by the existing wait path
          -> existing destination wait/queue path / JOB_WAITING
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
   fixed time. The inbound reservation is then converted/released into the existing destination
   wait/queue path, and the job reports `JOB_WAITING`; it remains bound to that destination and is
   not rerouted.

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
5. waiting-path selection by eligible-set size: a multi-eligible step waiting in the shared backlog
   and never in a per-machine queue, and a single-eligible step waiting in that machine's queue and
   never in the backlog;
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
11. agreement between job transfer observation and destination resource admission-load observation;
12. deterministic event-type/entity-reference ordering and simulated times;
13. terminal state / derived result agreement for repeated identical explicit inputs.

Fixtures pin semantic outcomes, not DTO/JSON bytes, transport representation, or unrelated
non-behavioral observation fields.
