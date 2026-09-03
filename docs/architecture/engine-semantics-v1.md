# Engine Semantics v1

Status: Normative design contract; implementation pending Gate 5 delivery
Semantic identity: `engine-semantics:v1`
Decision authority: [ADR-0015](decisions/0015-engine-semantics-identity-and-reproducibility.md)

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

## 2. Existing dispatch semantics retained by v1

Gate 5 does not redefine Gate 2 dispatch. The current semantics remain part of v1:

1. Start from the routing step's authored eligible-machine set. Prefer online machines by filtering
   out `Offline` resources when at least one eligible machine is online. If every eligible machine
   is offline, retain the full eligible set so selection remains deterministic and the job enters
   the existing waiting/recovery path rather than becoming unselectable.
2. Within the resulting candidate set, `canAcceptJob()` is the primary **ranking key**, not an
   eligibility filter: a machine that can accept immediately ranks ahead of one that cannot.
3. Among candidates tied on immediate acceptance, rank by `combinedQueueDepth` as currently defined
   by the Factory Engine, including pending multi-eligible work that could land on that resource.
4. Break a remaining tie deterministically by `MachineId`.
5. Selection runs at the existing semantic points even when the selected machine cannot yet accept
   the job. The post-selection branch determines immediate dispatch versus `pendingMultiEligible`
   versus the selected single-machine queue.
6. Recovery after a machine/step completion preserves the current cascade order: drain that
   machine's own queue first, then reconsider pending multi-eligible work. Within the multi-eligible
   backlog, the earliest queued entry that can actually be placed is dispatched first, with repeated
   re-selection after each placement.
7. Gate 5 inserts transfer time only after a concrete destination is both selected and currently
   admissible under the same acceptance rule that would permit immediate pre-Gate-5 processing; it
   does not move selection earlier or redefine ranking.

Changing any of those result-affecting rules requires a new Engine semantics version.

## 3. Existing work-decomposition semantics retained by v1

W1 child-work semantics remain part of v1:

1. Workload quantity `N` decomposes into `N` independently dispatchable `JobId` children.
2. Child creation/release and initial dispatch use the deterministic ordinal ordering established by
   W1.
3. Aggregate completion continues to correlate explicit `OrderId` with the completing child
   `JobId`.

A change that can alter assignment, ordering, or completion outcome for identical explicit inputs
requires a new Engine semantics version.

## 4. Existing scheduler and dispatch-cascade ordering retained by v1

1. Authoritative scheduled work is ordered first by `SimTime`.
2. Equal-time scheduled work uses the existing deterministic insertion-order tie-break.
3. Internal scheduler markers that do not represent authoritative Factory state changes do not gain
   semantic significance merely because they are present in the implementation.
4. Supported runtime event ordering at the same `SimTime` is represented only by the existing Gate
   4 sequence; Gate 5 introduces no second event-ordering mechanism.
5. The intra-handler recovery cascade is also semantic ordering where it changes assignment:
   after a completed step releases a machine, the Engine first attempts to dispatch that machine's
   own queued work and only then reconsiders `pendingMultiEligible` work. A change to that ordering
   can change assignments and therefore requires a new Engine semantics version.

## 5. Gate 5 ownership rule

Gate 5 applies this boundary:

- authored plant facts belong to the canonical Factory model and `ModelFingerprint`;
- result-affecting rules for interpreting those facts belong to `engine-semantics:v1`;
- authoritative facts that exist only during one run are runtime state;
- replaceable mechanisms preserving the same semantic outcomes are implementation details.

The V2 model facts consumed by this specification are floor dimensions, resource position,
resource footprint, `ticksPerCell`, and `handlingTicks`, as fixed by ADR-0014.

## 6. Destination selection and binding

The initial Gate 5 rule is **transfer begins when the selected destination becomes admissible for
binding under the existing acceptance semantics**.

1. On completion of operation `k`, the source resource releases processing capacity exactly as it
   does before Gate 5.
2. Existing Gate 2 selection/ranking runs at the existing semantic point, including deterministic
   selection of a non-accepting machine when that is the current outcome.
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
Gate 2 selection/ranking semantics.

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
   arithmetic in the runtime duration type. This guarantees representability of Gate 5's derived
   duration; it does not guarantee that `currentSimTime + transferDuration` is representable at an
   arbitrarily extreme current time. The existing runtime time-addition guard remains responsible
   for that condition.
7. Distinct resource footprints may not overlap. Because each reference cell lies inside its own
   footprint under ADR-0014, distinct valid resources cannot share the same reference cell, so a
   zero-distance inter-resource transfer is not a supported v1 state.
8. Consecutive operations on the **same resource** perform no transfer at all: no transfer state,
   no duration, and no transfer events.
9. A V2 design with `ticksPerCell = 0` and `handlingTicks = 0` therefore preserves pre-Gate-5
   completion timing while still exposing the authoritative transfer start/completion transitions
   for distinct resources.

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
"next-operation-dispatched" Gate 5 moment beyond the existing authoritative transitions.

## 9. Capacity and availability semantics

1. The source releases its processing capacity at step completion exactly as before Gate 5.
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

Gate 5 does not redefine existing metric formulas:

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
projection field used for this admission-load fact is an implementation/API-shape choice for G5-D,
not a change to the semantic distinction.

Bottleneck interpretation must therefore widen: a destination can have constrained admission
capacity because of transfer-bound work while not yet accruing processing busy ticks. Consumers must
not misdiagnose that state as ordinary processing utilization.

## 11. Supported job and runtime observation contract

A fresh supported observation must reconstruct an in-progress transfer without replay.

Gate 5 extends the existing per-job projection rather than creating a parallel transfer authority.
For an in-flight job it exposes, at minimum:

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
- existing Gate 4 sequence/time/run-state metadata.

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

At a shared `SimTime`, ordering uses only Gate 4 sequence. A zero-duration transfer is still a
scheduled completion turn, so the authoritative chain can be:

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

1. current deterministic dispatch behavior, including offline filtering/all-offline fallback,
   `canAcceptJob` ranking, `combinedQueueDepth`, `MachineId` tie-breaking, and the queue-before-
   `pendingMultiEligible` recovery cascade;
2. current W1 decomposition/release ordering;
3. current scheduler same-time ordering;
4. distinct-resource transfer with nonzero distance;
5. same-resource consecutive steps with no transfer;
6. zero authored transfer magnitudes and the same-time transfer event chain;
7. destination becoming unavailable after binding, arrival while offline, immutable binding, and no
   rerouting;
8. agreement between job transfer observation and destination resource admission-load observation;
9. deterministic event-type/entity-reference ordering and simulated times;
10. terminal state / derived result agreement for repeated identical explicit inputs.

Fixtures pin semantic outcomes, not DTO/JSON bytes, transport representation, or unrelated
non-behavioral observation fields.
