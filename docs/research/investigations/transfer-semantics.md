# Transfer Semantics Boundary Research

> **Status:** Maintained research programme; the lifecycle question is decided but `READY`, because no code implements its design, while non-spatial timing remains a candidate
> **Scope:** Separate the existence of inter-resource transfer as runtime behavior from spatial layout and from the mechanism used to determine transfer duration  
> **Authority:** Research only; the [Factory model](../../architecture/factory-model.md) and the [Engine semantics](../../architecture/engine-semantics.md) own current meaning, and neither has a spatial record or a transfer lifecycle

## Why this programme exists

The decided spatial design couples three concepts:

1. the **transfer lifecycle** (`TRANSFER_STARTED`, `TRANSFERRING`, `TRANSFER_COMPLETED`, destination binding/admission consequences);
2. **transfer timing**; and
3. the optional **spatial record** carrying floor, position, footprint, `ticksPerCell`, and `handlingTicks`.

Absence of that spatial record means the design makes no handling assertion. The [superseded Engine-applicability investigation](engine-applicability-optional-record-factory-policies.md) inherited "spatial absent implies no transfer lifecycle" as a premise; that is why the lifecycle question below was admitted. Its reviewed evidence remains useful, but its recommendation did not constrain the lifecycle candidates. The [Engine-identity question](engine-applicability-after-transfer-boundary.md) that once blocked spatial execution was superseded by the provisional semantic-contract reset.

The lifecycle investigation and independent adversarial review selected the [applicability boundary](#applicability-boundary) below. The selection is bounded to the spatial-record content it was decided for. The complete design — the authored spatial record, its canonical encoding and the Engine's transfer interpretation — is kept in [Decided design — not implemented](#decided-design--not-implemented). It was specified in the Factory model and Engine semantics while neither implemented it; it moved here when architecture became a description of implemented behavior, and the unreachable spatial record, transfer-timing arithmetic and inbound-reservation bookkeeping were deleted from the code at the same time. The Factory model now has no spatial record, so every model is a no-transfer model.

## READY — transfer lifecycle independence

The question, decision and scope below are retained as asked; they predate the provisional semantic-contract reset, which replaced the Factory V1/V2 policies with one work-in-progress model and made the Engine interpretation work in progress.

The decided result is explicit no transfer for a distinct-resource continuation under an Engine that admits a model with no spatial record. A present complete spatial record can yield a zero or positive transfer interval with the full lifecycle under an Engine that admits it. Factory-valid but Engine-unsupported models are refused before runtime mutation; support is checked before same-resource control cases. This is a design choice for the spatial record as decided, not a universal requirement to condition every future transfer lifecycle on authored timing facts. The later question about exact Engine identities was superseded by the reset. Independent review accepted the no-transfer recommendation with qualifications covering this scope, publication versus execution support, refusal priority, and the corrected bounded-step witness; all four are captured in the decided design.

The question stays `READY` because the positive result — a present spatial record and its lifecycle — has no implementation. It concludes when an implementation handoff lands the decided design together with the conformance fixtures it lists. A consumer that needs spatial layout to be behaviorally consequential is the trigger for that handoff; until then nothing here is current Factory or Engine meaning.

### Question

For work that completes one operation on a configured productive resource and next executes on a **different** configured productive resource, is transfer/hand-off an Engine execution phase whose existence is independent of whether the published Factory model includes spatial layout, or should the transfer lifecycle exist only when the model explicitly represents transfer-affecting facts?

### Decision at stake

Whether Arcogine may continue to equate “no spatial record” with “no transfer lifecycle,” or whether transfer state/events/binding must have a truthful non-spatial interpretation before Factory V2 publication and spatial activation proceed.

The answer can require correction of the still-unreleased Factory V2 boundary and/or a distinguishable Engine semantics identity. It also determines how the separate Engine-applicability question can be correctly re-bounded.

### Scope

In scope:

- distinct-resource continuation after an operation completes;
- same-resource continuation as the control case;
- destination selection/binding timing;
- whether start/in-flight/completion state exists when no spatial facts exist;
- zero-duration versus absent-transfer semantics;
- destination admission/reservation and arrival behavior only insofar as they depend on whether a transfer phase exists;
- model-versus-Engine ownership of the discriminator that makes transfer applicable.

Out of scope:

- selecting Manhattan versus Euclidean/path distance;
- pathfinding, aisles, conveyors, transport resources, buffers, congestion or routing graphs;
- choosing a non-spatial duration representation — the follow-up question below owns that;
- changing dispatch/ranking/queue sequencing;
- game presentation or animation.

### Candidates

At minimum compare:

1. **Current coupling:** transfer lifecycle is present only when the published model represents the current spatial/handling record; absence means next-step execution proceeds with no transfer state/events.
2. **Lifecycle independent, zero-duration fallback:** every distinct-resource continuation has the transfer binding/state/event lifecycle; without authored timing facts its duration is zero, while later timing contracts may make the interval positive. Same-resource continuation still has no transfer.
3. **Lifecycle independent but explicit transfer applicability required:** spatial layout is not the discriminator, but a separate authored transfer/timing contract determines whether a distinct-resource transition has transfer behavior. Absence of both spatial and non-spatial transfer facts has explicitly defined refusal or no-transfer semantics rather than inheriting meaning from geometry absence.

A report may reject or refine these candidates, but it must not collapse lifecycle existence and duration representation back into one unstated choice.

### Proving cases

Any surviving rule must state outcomes for:

- `M1 -> M1` continuation;
- `M1 -> M2` with no spatial record and no separately authored transfer facts;
- `M1 -> M2` with explicit authored zero timing;
- `M1 -> M2` with positive timing facts;
- multi-eligible next step where destination becomes bound before any positive/zero transfer interval;
- destination availability/capacity changes between binding and arrival when a non-zero interval exists;
- deterministic same-time ordering when transfer duration is zero;
- historical V1/production-only artifacts that never authored spatial facts.

The report must distinguish “no transfer exists,” “a transfer exists with zero duration,” and “the artifact is not executable because required transfer facts are absent.” Those are different semantics.

### Evidence expectations

Decision-quality evidence must include:

- a repository inventory of the current Factory/Engine coupling and every downstream contract that relies on it;
- external manufacturing/simulation evidence only where it tests the Arcogine candidates rather than surveying transport modelling generally;
- explicit ownership reasoning for authored plant facts versus result-affecting Engine interpretation;
- before/after result/provenance consequences for identical production records;
- compatibility and semantics-identity consequences under the semantic evolution rules;
- proving/failure cases sufficient to write deterministic conformance tests for the selected rule.

### Falsification

A candidate fails if it cannot truthfully distinguish absence from authored zero, makes a production-only model acquire invented authored facts, causes Engine interpretation to depend on consumer presentation, or leaves two conforming implementations free to disagree about whether a distinct-resource transfer state/event exists.

### Exit criteria used for the decision

The decision was reached only when the report could state, for every proving case:

- whether a transfer phase exists;
- what fact makes that determination;
- which owner defines that fact/rule;
- what happens when the relevant authored fact is absent; and
- what Factory-policy / Engine-semantics reconciliation is required.

Because this question can reopen a recently reconciled unreleased Factory boundary and affects a complete result-affecting Engine interpretation, independent adversarial review was required before reconciliation.

## Decided design — not implemented

Everything in this section is decided design, not current behavior. An implementation handoff that lands it moves each part back to its owner: the applicability boundary and the authored record to the [Factory model](../../architecture/factory-model.md) and [Factory semantic evolution](../../architecture/factory-design.md#111-semantic-evolution), and the transfer interpretation and its fixtures to the [Engine semantics](../../architecture/engine-semantics.md). The design was written while both definitions were work in progress and may be corrected before it lands.

### Applicability boundary

Factory records the production system a designer authored. Engine owns the result-affecting interpretation of those records, including whether an admitted execution enters transfer state, when a destination binds, reservation, timing, events, and refusal. Runtime determines whether a selected destination is the same configured resource as the source. The rule below is a design choice for this Factory model, not a universal requirement that transfer must always depend on authored interval inputs. A later Engine rule over different represented content needs its own explicit semantics and support decision.

The Factory model has either no spatial record or one complete record containing geometry and transfer-timing magnitudes ([authored spatial record](#authored-spatial-record)). It has no geometry-only, handling-only, or non-spatial hand-off record. Absence makes no authored handling assertion. Authored zero is a present fact and remains distinct in the Factory fingerprint. No transfer facts are synthesized from an absent record.

The Engine interpretation declares the represented content it executes. The admission predicate is applied **before** establishing a run or processing any routing step. Factory-invalid content is refused at publication. A valid model outside the executed content is refused before runtime mutation, even if its actual workload would use only same-resource continuations; a later same-resource selection cannot make it admissible. This model-admission rule does not replace workload and command preflight or their explicit reproducibility inputs.

For an admitted execution the lifecycle meanings are:

| Represented content | Same-resource continuation | Distinct-resource continuation |
|---|---|---|
| Spatial record absent | No transfer | No transfer: continue through ordinary destination selection and dispatch, without separate transfer binding, reservation, `TRANSFERRING` state, or `TRANSFER_*` events |
| Spatial record present with legal zero timing magnitudes | No transfer | Full transfer lifecycle under the [Engine transfer interpretation](#engine-transfer-interpretation); duration may be zero, with binding, reservation, state and events still present |
| Spatial record present with positive derived duration | No transfer | Full transfer lifecycle under the Engine transfer interpretation, with its derived positive interval |

Refusal has priority over every row. An Engine that executes only the first row refuses a present spatial record, legal zero included, before any runtime state exists rather than executing it as though the record were absent. Making the second and third rows executable changes the Engine's admitted content, not the meaning of the rows.

A zero-duration transfer and no transfer can differ despite identical isolated completion times. The former has its own binding, reservation, state, event, command-interleaving and same-time ordering consequences. A bounded-step proving case compares two candidate Engine meanings for the same spatial-absent model: the selected no-transfer rule and the unadopted Engine-defined zero-duration lifecycle. Use online unary resources `M1` and `M2`, route `M1:5 -> M2:3`, and one work item. **Before** the pending step-one `TaskEnd` at time 5 is processed, `advanceUntil(SimTime.of(5), 1)` consumes that one turn. Under no transfer the work item is processing on M2; under the zero-duration lifecycle it is `TRANSFERRING` with completion pending at time 5. Calling the bounded advance after already processing `TaskEnd` would consume the pending transfer completion and is not this witness. The distinction also appears in the supported event stream and in commands allowed between same-time turns. In another engine, equivalence must be judged against its actual observations, events, commands and ordering; atomic same-time advancement alone does not establish it.

Factory validity and Engine executability are separate decisions. While both definitions are work in progress, a change to either is reconciled in its owning specification, with the other's dependent text and tests updated in the same change; neither requires a successor identity.

The spatial grammar needs no lifecycle-driven correction. A future consumer may justify an explicit non-spatial hand-off fact, positive non-spatial timing, a geometry-only variant, or an Engine-defined zero-duration lifecycle on absent content. Those are viable alternatives, not interpretations silently admitted by this boundary. Each requires a concrete need and a separate Factory grammar and Engine meaning assessment as applicable. A new timing record does not automatically make transfer applicable. The rule here also does not forbid an explicit future Engine interpretation conditioned on geometry without authored timing magnitudes.

### Authored spatial record

The spatial record is an optional authored record of the Factory model, under the optional-record composition rule kept in the [composition research](factory-model-semantic-composition.md#decided-rules-not-implemented). It is either **absent** or **present and complete**. When present it carries exactly these five authored fact groups, each required within the record:

| Fact | Meaning | Validation | Zero legal? |
|---|---|---|---|
| floor width / height | plant extent in integer cells | each `>= 1`; the maximum-transfer predicate below must hold | no |
| resource position `x` / `y` | minimum-coordinate reference cell of the resource footprint | `x >= 0`, `y >= 0`; the footprint must fit inside the floor | yes (`0,0` is valid) |
| footprint width / height | integer cells occupied by the resource from its reference cell | each `>= 1`; distinct resource footprints must not overlap | no |
| `ticksPerCell` | authored material-handling rate magnitude | integer `>= 0`; the maximum-transfer predicate must hold | yes |
| `handlingTicks` | authored fixed overhead applied once per inter-resource transfer | integer `>= 0`; the maximum-transfer predicate must hold | yes |

Floor extent, `ticksPerCell` and `handlingTicks` are plant-scope: one value each per design. Position and footprint are per resource. A present record gives **every** configured resource exactly one position and one footprint, in resource-list order, and gives none to an identifier that is not a configured resource; a record that omits a resource, places one twice, places an unknown identifier or lists layouts out of resource order is invalid.

When the record is **absent**, the design makes no spatial, layout or handling assertion: no floor, position, footprint, `ticksPerCell` or `handlingTicks` exists for it, and none is synthesized from a default. Absence is not a present record with zero or minimal values — a present record with position `(0,0)`, `ticksPerCell = 0` and `handlingTicks = 0` is a different authored design. Unknown or partly authored facts, such as geometry whose handling magnitudes are unknown, are draft or adapter state: they are not publishable and are never encoded as zero. The record is deliberately coarse; it has no geometry-only or handling-only form.

For a resource at `(x,y)` with footprint width `w` and height `h`, the footprint occupies exactly the integer cells `x..x+w-1` by `y..y+h-1`. It fits inside a floor of width `W` and height `H` iff `x >= 0`, `y >= 0`, `w >= 1`, `h >= 1`, `x + w <= W` and `y + h <= H`, evaluated with overflow-safe arithmetic. Two resources overlap iff their occupied-cell sets intersect.

Publication of a present record also proves that the spatial transfer-duration magnitude is representable for the farthest possible pair of reference cells:

```text
maxManhattanDistance = (W - 1) + (H - 1)
maxTransferDuration  = handlingTicks + ticksPerCell * maxManhattanDistance
```

Every subtraction, addition and multiplication in that predicate, and the result, must be representable in the runtime tick-duration type. This bounds the derived duration itself; it does not claim that adding a valid duration to an arbitrarily extreme current `SimTime` can never overflow — the runtime time-addition guard remains responsible for that.

The spatial record's presence and every present fact participate in the fingerprint. They are authored model facts, not Engine policy: floor extent, placement, footprint, handling rate and fixed overhead describe the production system the designer authored, while the distance metric, rounding, zero-distance behavior, destination binding, reservation and transfer lifecycle belong to the [Engine transfer interpretation](#engine-transfer-interpretation). Footprint is canonical even though no Engine rule reads it in transfer distance: it is required for layout validation and future spatial capability.

Orientation, paths, graph edges, aisles, conveyors, transport resources, obstacles, congestion, floor identity, connection points, authoritative animation coordinates and route topology are not part of the record. Adding them is a definition change justified by a concrete capability that makes them authored, behaviorally relevant semantics.

#### Canonical encoding

The record extends the [Factory canonical byte grammar](../../architecture/factory-model.md#4-canonical-byte-grammar). Every spatial field is an `I64`. A presence byte — `00` absent, `01` present, no other value valid — follows the definition prefix, and a present record adds a fixed-arity header and a per-resource suffix:

```text
PRESENCE(spatial)

if spatial is present:
    I64(spatial.floor.width)
    I64(spatial.floor.height)
    I64(spatial.ticksPerCell)
    I64(spatial.handlingTicks)

U64(resources.size)
for resource in resources, in list order:
    ... production resource fields ...
    if spatial is present:
        I64(spatial.position(resource).x)
        I64(spatial.position(resource).y)
        I64(spatial.footprint(resource).width)
        I64(spatial.footprint(resource).height)

... operations and products unchanged ...
```

Layout rationale:

- **Marker first.** A decoder knows the complete shape of every following record before decoding any of it, so each artifact has exactly one parse, and an absent record occupies one byte.
- **Header before resources.** The maximum-transfer predicate depends only on header fields, so a decoder can reject unrepresentable magnitudes, and establish the floor every footprint is checked against, before it allocates a single resource record. The header is fixed-arity and has no count.
- **Per-resource suffix.** Encoding each resource's spatial fields inside that resource's record makes a present record's coverage structural: the grammar cannot express a resource without spatial fields, spatial fields without a resource, or two layouts for one resource.
- **Anchor plus extent.** Placement is encoded as the authored reference cell and extent, never as a derived occupied-cell set, bitmap, bounding box or region. For every publishable footprint this is bijective with the occupied cells. Positions are encoded exactly as authored: there is no translation invariance, origin normalization or coordinate compaction.
- **Totality.** Every spatial field is an `I64` and the grammar is total over the signed 64-bit range. The range, containment, overlap and maximum-transfer predicates are publication validation, not encoding constraints, which lets a decoder reject a violating artifact explicitly instead of failing to parse it ambiguously.

Per-resource spatial fields follow resource list order; no spatial ordering of resources (row-major, distance-from-origin or similar) is applied, because it would discard authored list order and collapse two authored models into one identity. A decoder additionally rejects a missing or invalid presence marker and a stream truncated inside the spatial header or a resource's spatial fields, and applies the spatial predicates whenever the record is present.

#### Required Factory coverage

Beside the production-record coverage the Factory model already requires, landing the record requires:

1. one representative model with the spatial record present, with exact expected bytes and fingerprint, beside the spatial-absent one;
2. an absent record and a present record with legal zero or minimal values — position `(0,0)`, a 1×1 floor and footprint, `ticksPerCell = 0`, `handlingTicks = 0` — producing different pinned fingerprints;
3. presence-marker framing: an invalid marker, truncation inside the header or a resource's spatial fields, and header bytes appended to a spatial-absent artifact, each rejected;
4. coverage: publication, and canonical encoding, reject a present record that omits a resource, repeats one, places an unknown identifier, places none while resources exist, or lists layouts out of resource order;
5. each position coordinate, each footprint extent, floor width and height, `ticksPerCell` and `handlingTicks` independently changing the fingerprint;
6. swaps of distinct `x`/`y`, width/height, floor width/height and `ticksPerCell`/`handlingTicks` values changing it, proving field order;
7. a whole-design translation producing a different fingerprint;
8. grammar-valid content violating a spatial predicate — overlapping footprints, an out-of-floor footprint, a zero floor or footprint extent, a negative position or handling magnitude, a violated maximum-transfer predicate including an unrepresentable intermediate multiplied by zero — rejected on publication and on decode;
9. accepted boundaries beside those rejections: edge- and corner-adjacent footprints, a footprint on the maximum legal floor edge, a 1×1 floor, and a maximum transfer duration exactly equal to the largest representable value.

### Engine transfer interpretation

#### Spatial model/Engine ownership boundary

Spatial transfer semantics apply this boundary:

- authored plant facts belong to the canonical Factory model and `ModelFingerprint`;
- result-affecting rules for interpreting those facts belong to the Engine interpretation;
- authoritative facts that exist only during one run are runtime state;
- replaceable mechanisms preserving the same semantic outcomes are implementation details.

The spatial-record facts consumed here are floor dimensions, resource position, resource footprint, `ticksPerCell`, and `handlingTicks`, as defined and canonicalized by the [authored spatial record](#authored-spatial-record). The Engine interpretation consumes those facts; it never defines their canonical encoding or identity.

Spatial transfer semantics do not redefine the existing [resource-selection and dispatch behavior](../../architecture/engine-semantics.md#2-resource-selection-and-dispatch-semantics) or its [cascade ordering](../../architecture/engine-semantics.md#4-scheduler-and-dispatch-cascade-ordering). Spatial transfer time is inserted only after a concrete destination is both selected and currently admissible under the same acceptance rule that otherwise permits immediate processing; transfer semantics do not move selection earlier or redefine ranking. Supported runtime-event ordering at the same `SimTime` stays the monotonic supported-event sequence of the [runtime contract](../../architecture/runtime-contract.md); spatial transfer introduces no second event-ordering mechanism.

#### Destination selection and binding

The rule is **transfer begins when the selected destination becomes admissible for binding under the existing acceptance semantics**.

1. On completion of operation `k`, the source resource releases processing capacity exactly as it does without spatial transfer delay.
2. Existing resource selection/ranking runs at the established semantic point, including deterministic selection of a non-accepting machine when that is the current outcome.
3. If the selected machine cannot currently accept the job, the job remains in the existing waiting path (`machine queue` or `pendingMultiEligible`) and the existing recovery cascade controls when selection/placement is retried.
4. When the selected resource can accept the job, that concrete destination is bound.
5. Binding consumes one unit of the destination's **admission capacity** for the inbound job. This reservation contributes to whether subsequent `canAcceptJob` decisions have free capacity, but the inbound job is not yet active processing and is not yet queued work.
6. Transfer duration is computed and fixed at that binding instant.
7. `TRANSFER_STARTED` is emitted for the authoritative transition into the in-flight state.
8. Destination binding is immutable after transfer start. There is no rerouting.
9. **Binding collapses the job's effective eligible set to the bound destination** for every subsequent waiting decision. From binding until the job begins processing there, the bound destination is the only machine the job can be placed on, regardless of how many machines the authored routing step listed as eligible. If the job must wait after arrival, it waits in that destination's own queue — appended at the tail in arrival order like any other queued entry — and never in the shared multi-eligible backlog. [Capacity and availability](#capacity-and-availability) rule 8 fixes the arrival-offline case this exists for.

   This rule is what makes immutable binding implementable. Without it, a multi-eligible step arriving at an offline destination would have no waiting path: the shared backlog would re-select and could reroute, contradicting rule 8, while the destination's own queue would contradict the Engine's waiting-path rule, under which a multi-eligible step never occupies a per-machine queue. Collapsing the effective eligible set resolves both by making the bound job a single-eligible job for waiting purposes.

   The Engine's waiting-path rule therefore applies to the effective eligible set at the moment a job enters the waiting state: before binding it is the routing step's authored eligible-machine set, and after binding it is the bound destination alone. A multi-eligible step never occupies a per-machine queue while waiting pre-binding, and a single-eligible step never occupies the shared backlog; post-binding waiting is the deliberate and only exception. The shared multi-eligible backlog holds pre-binding work only: because its reselection can change the destination, a job that has already bound a destination is never placed in it, since reconsideration there re-runs selection over the authored eligible set and could place the job on a different machine — a reroute this contract forbids.

This changes only the delay between destination binding and next-step processing; it does not change resource-selection/ranking semantics.

#### Transfer timing

For distinct source and destination resources:

```text
manhattanDistance = abs(xDestination - xSource) + abs(yDestination - ySource)
transferDuration = handlingTicks + (ticksPerCell * manhattanDistance)
```

Semantic rules:

1. Resource positions are integer reference cells defined by the Factory model as the minimum-coordinate cells of their footprints.
2. Distance is Manhattan distance between those reference cells.
3. Resource footprint does **not** affect transfer distance. Footprint remains canonical Factory content for publication/layout validation and future spatial semantics.
4. `handlingTicks` is applied once per transfer, not once per endpoint.
5. Arithmetic is integer throughout. There is no floating-point distance and no rounding rule because none is required by the chosen metric.
6. Factory publication validation proves the exact maximum-duration predicate of the [authored spatial record](#authored-spatial-record): `(W - 1) + (H - 1)` and `handlingTicks + ticksPerCell * maxManhattanDistance` must be representable with overflow-safe arithmetic in the runtime duration type. This guarantees representability of the derived transfer duration; it does not guarantee that `currentSimTime + transferDuration` is representable at an arbitrarily extreme current time. The existing runtime time-addition guard remains responsible for that condition.
7. Distinct resource footprints may not overlap. Because each reference cell lies inside its own footprint, distinct valid resources cannot share the same reference cell, so a zero-distance inter-resource transfer is not a supported state.
8. Consecutive operations on the **same resource** perform no transfer at all: no transfer state, no duration, and no transfer events.
9. A present spatial record with `ticksPerCell = 0` and `handlingTicks = 0` therefore preserves the completion timing that would occur without spatial transfer delay while still exposing the authoritative transfer start/completion transitions for distinct resources.

Changing metric, endpoint interpretation, handling application, arithmetic/rounding, zero-distance semantics, or same-resource behavior is a change to the interpretation.

#### Transfer runtime state machine

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

`TRANSFERRING` is a runtime state between two moments, not a third event/moment. There is no separate "destination selected", "transfer pending", "destination queued while in flight", or "next-operation-dispatched" transfer moment beyond the existing authoritative transitions.

#### Capacity and availability

1. The source releases its processing capacity at step completion exactly as it does without spatial transfer delay.
2. A transferring job consumes **destination admission capacity only** from binding until arrival. That reserved inbound capacity counts when deciding whether the destination can admit additional work, but the job is not in the destination's active-processing set and does not make the machine `Busy` merely because it is in flight.
3. The interpretation invents no transport resource/capacity, conveyor scheduler, physical buffer, or congestion model.
4. Admission reservation must be distinguishable from both active processing and queued work. It need not be a new public resource abstraction; it is runtime bookkeeping required to preserve the semantic distinctions already fixed here.
5. Source going offline after departure has no effect on the in-flight transfer.
6. A destination holding only inbound reservations may still be taken offline under the existing command contract because no reserved job has begun active processing. Going offline after transfer start does not change the bound destination or fixed completion time.
7. Another eligible destination becoming preferable after departure does not cause rerouting.
8. If the destination is offline at exact arrival time, `TRANSFER_COMPLETED` still occurs at the fixed time. The inbound admission reservation is then **converted into an entry in that destination's own queue** — the bound-destination wait fixed by [binding](#destination-selection-and-binding) rule 9 — and the job reports `JOB_WAITING`. It remains bound to that destination, never enters the shared multi-eligible backlog, and is not rerouted. The existing recovery cascade releases it when the destination comes back online.

   From arrival onward the entry is ordinary queued work, which resolves its observation consequences unambiguously: it **counts in the destination's queue depth**, it is **no longer held as reserved admission capacity** (the reservation is converted, not additionally retained, so one waiting job never consumes two units of the same destination's capacity), and it is not an active job until processing starts. This is the one case where a job whose authored routing step listed several eligible machines legitimately occupies a per-machine queue.

#### Observation interpretation

Spatial transfer semantics do not redefine existing metric formulas:

- queue depth remains the existing count of queued/waiting work and does not count an in-flight job as a destination queue entry;
- backlog continues to count incomplete accepted orders;
- lead time naturally includes transfer delay because completion occurs later;
- throughput remains completed orders per observed time.

A reserved inbound job also does **not** appear in the destination resource projection's `activeJobIds`, because active jobs are jobs that have actually begun processing. A destination holding only inbound reservations therefore keeps the processing-oriented machine state it would otherwise have (`Idle` when it has no active processing, or `Busy` only because of other active jobs). Supported resource observation must nevertheless expose enough capacity information for a consumer to account for admission capacity consumed by inbound reservations; it must not silently count the in-flight job as processing or queue depth. The exact projection field used for this admission-load fact is an implementation/API-shape choice, not a change to the semantic distinction.

A destination can therefore have constrained admission capacity because of transfer-bound work while none of that work is processing. Any consumer interpretation of resource load must keep reserved admission capacity distinct from processing occupancy rather than count it as processing. Transfer residence stays distinct from waiting-state and processing residence.

A fresh supported observation must reconstruct an in-progress transfer without replay. Spatial transfer support extends the existing per-job projection rather than creating a parallel transfer authority. For an in-flight job it exposes, at minimum:

- job status `TRANSFERRING`;
- `JobId` and existing order/step correlation;
- source resource identity;
- bound/current destination resource identity;
- `transferStartedAt`;
- `transferCompletesAt`.

The job projection and resource projection must agree: the job is `TRANSFERRING`; the destination is bound and has one unit of admission capacity reserved; the job is not yet an active job and is not in destination queue depth until arrival changes it into processing or waiting state.

Authoritative current coordinates, progress fraction, distance, and frame-by-frame movement are not part of the supported runtime state. A consumer may derive visual interpolation from authoritative start/completion times and canonical resource placement.

#### Transfer events

Both events are required because they describe distinct authoritative state changes.

`TRANSFER_STARTED` carries enough supported facts to identify the in-flight relation and its fixed completion:

- `JobId`;
- `OrderId`;
- source resource identity;
- destination resource identity;
- `transferCompletesAt`;
- affected references for job, order, source, and destination.

`TRANSFER_COMPLETED` carries:

- `JobId`;
- `OrderId`;
- destination resource identity;
- affected references for job, order, and destination.

The envelope also carries the existing run/model provenance of the [runtime contract](../../architecture/runtime-contract.md).

At a shared `SimTime`, ordering uses only the supported runtime-event sequence. A zero-duration transfer is still a scheduled completion turn, so the authoritative chain can be:

```text
JOB_STEP_COMPLETED
-> TRANSFER_STARTED
-> TRANSFER_COMPLETED
-> JOB_DISPATCHED
```

or end in `JOB_WAITING` when the destination is offline at arrival. `TRANSFER_STARTED` is published only after the transition into `TRANSFERRING` succeeds; completion occurs on the separately scheduled same-time turn, so supported events continue to describe authoritative changes that have already happened. Scheduling the transfer completion is a new authoritative event kind, so the Engine's scheduling rule must name it beside step completion when the design lands.

#### Deliberately deferred spatial/runtime capabilities

The interpretation does not define:

- authoritative intermediate transfer coordinates/progress;
- rerouting;
- transport-resource capacity or transport scheduling;
- paths, aisles, conveyors, obstacles, graphs, or pathfinding;
- congestion;
- resource orientation as a transfer input;
- animation authority.

Adding them is a deliberate change to the Engine interpretation, the Factory model, or both, made only when their actual ownership and result-affecting meaning become concrete.

#### Required Engine conformance fixtures

Landing the interpretation adds these behavioral fixtures to the [Engine conformance fixtures](../../architecture/engine-semantics.md#6-conformance-fixtures):

1. distinct-resource transfer with nonzero distance;
2. same-resource consecutive steps with no transfer;
3. zero authored transfer magnitudes and the same-time transfer event chain;
4. destination becoming unavailable after binding, arrival while offline, immutable binding, and no rerouting;
5. the bound-destination wait for a **multi-eligible** step: a job whose authored step lists several eligible machines, bound to one destination, arriving while that destination is offline — it waits in the bound destination's own queue, is absent from the shared multi-eligible backlog, is still bound to the same destination after another eligible machine becomes free and the recovery cascade runs, counts in that destination's queue depth, and no longer counts as reserved admission capacity;
6. agreement between job transfer observation and destination resource admission-load observation;
7. refusal of content the Engine does not yet execute, before any runtime state exists, when the record lands ahead of its execution.

## CANDIDATE — non-spatial transfer timing

### Question

If a concrete consumer needs hand-off or positive transfer duration without spatial layout, what minimum authored applicability or timing contract is justified, and how should it compose with spatial derivation?

### Why it is not READY yet

The decided boundary selects no transfer when the spatial record is absent and finds no present need for an additional hand-off assertion or positive non-spatial interval. A future consumer need can reopen that choice; no candidate representation is admitted now.

### Candidate families to test when promoted

Examples to compare if the promotion trigger fires:

- an explicit non-spatial hand-off applicability fact with Engine-defined zero timing;
- an explicit non-spatial hand-off fact with authored timing;
- no positive non-spatial timing: explicit zero remains the only non-spatial timing;
- plant-wide fixed handling delay;
- operation-transition or routing-step transfer duration;
- source/destination pair matrix;
- explicit transfer relationship/profile referenced by the Factory model;
- spatial derivation as one timing source among others rather than the definition of transfer itself.

The investigation must prefer the smallest authored invariant that a concrete consumer/plant model actually needs. Do not introduce transport-resource ontology, graph topology, or generic movement profiles merely to make the representation extensible.

### Promotion trigger

Promote only when a concrete simulation, game, industrial-design or operational consumer needs to assert hand-off without spatial facts, or needs positive non-spatial transfer time. Revisit the no-transfer choice, the Factory grammar and the Engine interpretation together; do not assume that adding a timing record automatically admits a transfer lifecycle.

## Consequences for current work

- every current Factory model has no spatial record, so every distinct-resource continuation is the decided no-transfer case; that is current behavior, not a degraded mode;
- spatial content becomes executable only through an implementation handoff that lands the [decided design](#decided-design--not-implemented), preserving its absence, authored-zero and refusal meanings;
- while both definitions are work in progress, a later transfer contract independent of the optional spatial record is a coordinated correction of the Factory model and the Engine interpretation that revisits the Factory composition result, not a successor policy; after either is promoted, the [semantic evolution rules](../../architecture/overview.md#semantic-evolution-and-support) control instead.
