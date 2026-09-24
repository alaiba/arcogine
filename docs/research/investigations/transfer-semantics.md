# Transfer Semantics Boundary Research

> **Status:** Maintained research programme; the lifecycle question is concluded through the current [transfer-applicability boundary](../../architecture/transfer-applicability.md), while non-spatial timing remains a candidate
> **Scope:** Separate the existence of inter-resource transfer as runtime behavior from spatial layout and from the mechanism used to determine transfer duration  
> **Authority:** Research only; the [transfer-applicability boundary](../../architecture/transfer-applicability.md), Factory Model v2 and Engine Semantics v1 own current meaning

## Why this programme exists

Current unreleased Factory/Engine architecture couples three concepts:

1. the **transfer lifecycle** (`TRANSFER_STARTED`, `TRANSFERRING`, `TRANSFER_COMPLETED`, destination binding/admission consequences);
2. **transfer timing**; and
3. the optional **spatial record** carrying floor, position, footprint, `ticksPerCell`, and `handlingTicks`.

Absence of that spatial record means the design makes no handling assertion. The [superseded Engine-applicability investigation](engine-applicability-optional-record-factory-policies.md) inherited "spatial absent implies no transfer lifecycle" as a premise; that is why the lifecycle question below was admitted. Its reviewed evidence remains useful, but its recommendation did not constrain the lifecycle candidates. V2 remains unreleased and spatial runtime activation is blocked pending the separate [Engine applicability question](engine-applicability-after-transfer-boundary.md).

The lifecycle investigation and independent adversarial review selected the [current transfer-applicability boundary](../../architecture/transfer-applicability.md). The selection is bounded to currently represented V2 content. This brief retains the original proving cases and the separately deferred timing question as research context; it is not the current Engine definition.

## CONCLUDED — transfer lifecycle independence

The adopted result is explicit no transfer for a distinct-resource continuation under an Engine that admits V2 with no spatial record. A present complete spatial record can yield a zero or positive transfer interval with the full lifecycle under an Engine that admits it. Factory-valid but Engine-unsupported artifacts are refused before runtime mutation; support is checked before same-resource control cases. This is a current design choice, not a universal requirement to condition every future transfer lifecycle on authored timing facts. The exact Engine identity/support partition remains open in the [successor question](engine-applicability-after-transfer-boundary.md). Independent review accepted the no-transfer recommendation with qualifications covering this scope, publication versus execution support, refusal priority, and the corrected bounded-step witness; all four are captured in the adopted boundary. No V2 grammar correction or runtime activation follows from this conclusion alone.

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

### Exit criteria used for the conclusion

Conclude only when the report can state, for every proving case:

- whether a transfer phase exists;
- what fact makes that determination;
- which owner defines that fact/rule;
- what happens when the relevant authored fact is absent; and
- what Factory-policy / Engine-semantics reconciliation is required.

Because this question can reopen a recently reconciled unreleased Factory boundary and affects a complete result-affecting Engine interpretation, independent adversarial review is required before architecture/specification reconciliation.

## CANDIDATE — non-spatial transfer timing

### Question

If a concrete consumer needs hand-off or positive transfer duration without spatial layout, what minimum authored applicability or timing contract is justified, and how should it compose with spatial derivation?

### Why it is not READY yet

The concluded current boundary selects no transfer on V2 absence and finds no present need for an additional hand-off assertion or positive non-spatial interval. A future consumer need can reopen that choice; no candidate representation is admitted now.

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

Promote only when a concrete simulation, game, industrial-design or operational consumer needs to assert hand-off without spatial facts, or needs positive non-spatial transfer time. Revisit the current no-transfer choice, Factory grammar and Engine identity/support together; do not assume that adding a timing record automatically admits a transfer lifecycle.

## Consequences for current work

Until the successor Engine-applicability question is reconciled:

- do not release `factory-model:v2` or activate spatial transfer runtime behavior;
- derive the exact identity/support cases from the adopted [transfer-applicability boundary](../../architecture/transfer-applicability.md), including artifact refusal before runtime cases;
- do not infer that fixed `engine-semantics:v1` admits V2 or that V1 publication and historical V1 execution have one support switch.

If a later need selects a transfer contract independent of the current optional spatial record, revisit the concluded Factory composition result before V2 attribution. Because V2 is unreleased, the appropriate outcome may be a correction to its grammar; if attribution has occurred by then, the semantic evolution rules control instead.
