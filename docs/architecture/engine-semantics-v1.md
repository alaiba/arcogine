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

1. A resource that cannot accept the job is not eligible for selection.
2. Among eligible resources, rank by `combinedQueueDepth` as currently defined by the Factory
   Engine, including pending multi-eligible work that could land on that resource.
3. Break a remaining tie deterministically by `MachineId`.
4. Resource selection occurs only at the same semantic points where the pre-Gate-5 Engine performs
   it; Gate 5 inserts transfer time after a concrete destination becomes assignable rather than
   moving selection earlier.

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

## 4. Existing scheduler ordering retained by v1

1. Authoritative scheduled work is ordered first by `SimTime`.
2. Equal-time scheduled work uses the existing deterministic insertion-order tie-break.
3. Internal scheduler markers that do not represent authoritative Factory state changes do not gain
   semantic significance merely because they are present in the implementation.
4. Supported runtime event ordering at the same `SimTime` is represented only by the existing Gate
   4 sequence; Gate 5 introduces no second event-ordering mechanism.

## 5. Gate 5 ownership rule

Gate 5 applies this boundary:

- authored plant facts belong to the canonical Factory model and `ModelFingerprint`;
- result-affecting rules for interpreting those facts belong to `engine-semantics:v1`;
- authoritative facts that exist only during one run are runtime state;
- replaceable mechanisms preserving the same semantic outcomes are implementation details.

The V2 model facts consumed by this specification are floor dimensions, resource position,
resource footprint, `ticksPerCell`, and `handlingTicks`, as fixed by ADR-0014.

## 6. Destination selection and binding

The initial Gate 5 rule is **transfer begins when the destination becomes assignable**.

1. On completion of operation `k`, the source resource releases processing capacity exactly as it
   does before Gate 5.
2. Existing Gate 2 selection/ranking runs at the existing semantic point.
3. If no eligible resource can currently accept the job, the job remains in the existing waiting
   path (`machine queue` or `pendingMultiEligible`) until capacity/availability permits selection.
4. When a resource can accept the job, that concrete destination is selected and bound.
5. Destination binding immediately reserves/occupies one unit of the destination's processing
   capacity.
6. Transfer duration is computed and fixed at that binding instant.
7. `TRANSFER_STARTED` is emitted for the authoritative transition into the in-flight state.
8. Destination binding is immutable after transfer start. V1 does not reroute.

This changes only the delay between destination binding and next-step processing; it does not change
Gate 2 resource-selection semantics.

## 7. Transfer timing

For distinct source and destination resources:

```text
manhattanDistance = abs(xDestination - xSource) + abs(yDestination - ySource)
transferDuration = handlingTicks + (ticksPerCell * manhattanDistance)
```

Semantic rules:

1. Resource positions are integer reference cells.
2. Distance is Manhattan distance between those reference cells.
3. Resource footprint does **not** affect v1 transfer distance. Footprint remains canonical Factory
   content for publication/layout validation and future spatial semantics.
4. `handlingTicks` is applied once per transfer, not once per endpoint.
5. Arithmetic is integer throughout. V1 has no floating-point distance and no rounding rule because
   none is required by the chosen metric.
6. Factory V2 publication validation must bound floor dimensions and authored rate magnitudes so the
   exact derived transfer duration is representable; overflow is rejected before runtime rather
   than treated as a mid-run failure.
7. Distinct resource footprints may not overlap. Under that validation distinct resources cannot
   share the same reference cell, so a zero-distance inter-resource transfer is not a supported V1
   state.
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
  existing destination eligibility/ranking

  if no assignable destination:
      -> existing WAITING state/path
      -> retry under existing dispatch cascade when capacity/availability changes

  if selected destination == source:
      -> no transfer
      -> next processing start under existing dispatch semantics

  if selected destination != source:
      -> reserve destination capacity
      -> TRANSFER_STARTED at current SimTime
      -> TRANSFERRING until fixed completion time
      -> TRANSFER_COMPLETED at start + transferDuration

      if destination online at arrival:
          -> next processing start / JOB_DISPATCHED under existing semantics
      else:
          -> existing destination wait/queue path / JOB_WAITING
```

`TRANSFERRING` is a runtime state between two moments, not a third event/moment. There is no separate
"destination selected", "transfer pending", "destination queued while in flight", or
"next-operation-dispatched" Gate 5 moment beyond the existing authoritative transitions.

## 9. Capacity and availability semantics

1. The source releases its processing capacity at step completion exactly as before Gate 5.
2. A transferring job occupies **destination processing capacity only** from binding until that
   capacity is later released by completion of the next processing step or by the existing
   offline/wait recovery path.
3. V1 invents no transport resource/capacity, conveyor scheduler, physical buffer, or congestion
   model.
4. Destination reservation is represented by the ordinary capacity occupancy created at binding;
   there is no second reservation system.
5. Source going offline after departure has no effect on the in-flight transfer.
6. Destination going offline after transfer start does not change the fixed destination or
   completion time.
7. Another eligible destination becoming preferable after departure does not cause rerouting.
8. If the destination is offline at exact arrival time, `TRANSFER_COMPLETED` still occurs at the
   fixed time and the job enters the existing destination wait/queue path with `JOB_WAITING`.

## 10. KPI interpretation

Gate 5 does not redefine existing metric formulas:

- `busyTicks` / utilization remain processing-time measures; transfer time does not accrue resource
  busy ticks merely because destination capacity is reserved;
- queue depth remains the existing count of queued/waiting work and does not count an in-flight job
  as a destination queue entry;
- backlog continues to count incomplete accepted orders;
- lead time naturally includes transfer delay because completion occurs later;
- throughput remains completed orders per observed time.

Bottleneck interpretation must widen: a destination can be capacity-constrained by transfer-bound
work while not yet accruing processing busy ticks. Consumers must not misdiagnose that state as
ordinary processing utilization.

## 11. Supported observation contract

A fresh supported observation must reconstruct an in-progress transfer without replay.

Gate 5 extends the existing per-job projection rather than creating a parallel transfer authority.
For an in-flight job it exposes, at minimum:

- job status `TRANSFERRING`;
- `JobId` and existing order/step correlation;
- source resource identity;
- bound/current destination resource identity;
- `transferStartedAt`;
- `transferCompletesAt`.

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

At a shared `SimTime`, ordering uses only Gate 4 sequence. A zero-duration transfer can therefore
produce the ordered chain:

```text
JOB_STEP_COMPLETED
-> TRANSFER_STARTED
-> TRANSFER_COMPLETED
-> JOB_DISPATCHED
```

or end in `JOB_WAITING` when the destination is offline at arrival.

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

1. current deterministic dispatch ranking and tie-breaking;
2. current W1 decomposition/release ordering;
3. current scheduler same-time ordering;
4. distinct-resource transfer with nonzero distance;
5. same-resource consecutive steps with no transfer;
6. zero authored transfer magnitudes;
7. destination unavailable at arrival with immutable binding/no rerouting;
8. deterministic event-type/entity-reference ordering and simulated times;
9. terminal state / derived result agreement for repeated identical explicit inputs.

Fixtures pin semantic outcomes, not DTO/JSON bytes, transport representation, or unrelated
non-behavioral observation fields.
