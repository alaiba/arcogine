# Factory Design Capability Implementation Plan

> **Status:** Active/partial; Factory V1 and V2 shape/validation code is landed. Fresh Factory identity is held for composition/reconciliation; mandatory pre-reset coexistence is superseded.
> **Scope:** Implementation-ready Factory Design work over the canonical production-system model  
> **Authority:** Planning only. ADR-0017/ADR-0018 withdraw pre-reset support; remaining Factory composition work must be reconciled before a fresh identity contract is admitted.
> **Related:** [Factory Design Architecture](../architecture/factory-design.md), [ADR-0003](../architecture/decisions/0003-canonical-factory-model-boundary.md), [ADR-0004](../architecture/decisions/0004-model-identity-revision-lineage-and-external-change-control.md), [ADR-0006](../architecture/decisions/0006-durable-semantic-fingerprint-contract.md), [ADR-0014](../architecture/decisions/0014-factory-model-semantic-policy-evolution.md), [ADR-0015](../architecture/decisions/0015-engine-semantics-identity-and-reproducibility.md), [Factory Model v2](../architecture/factory-model-v2.md), [Spatial Runtime Consequences](spatial-runtime-consequences.md), [Factory Design Evolution Research](../research/investigations/factory-design-evolution.md), [Factory Resource Semantics](../architecture/factory-resource-semantics.md), [Semantic Contract Maturity and Durability Research](../research/investigations/semantic-contract-maturity-durability.md), [Factory Model Semantic Composition Research](../research/investigations/factory-model-semantic-composition.md)

## 1. Implementation boundary

Factory Design owns the immutable semantic description that consumers publish and downstream contexts instantiate:

```text
consumer-owned draft/input
        |
        v
canonical FactoryModel
        |
        v
validate / publish
        |
        v
FactoryModelVersion + ModelFingerprint
        |
        v
validated/resolved runtime instantiation
```

Runtime workload, queues, assignments, transfers in progress, operational observations, deployments, and twin reconciliation are not Factory Design state.

The canonical model is not an editor state tree, a scenario envelope, a transport DTO, or a mutable runtime object graph.

## 2. Current landed baseline

The following are implemented. Preserve their intrinsic boundaries; pre-reset policy-specific code and compatibility fixtures are removable only in a later bounded cleanup under ADR-0017/ADR-0018:

- a canonical `FactoryModel` / immutable `FactoryModelVersion` seam;
- product, operation, and concrete resource identities under the pre-reset v1 policy;
- deterministic structural executability validation;
- immutable publication;
- the pre-reset `factory-model:v1` fingerprint implementation;
- runtime instantiation only from a published model;
- runtime/result attribution to source model identity;
- Governance-owned controlled revision identity/history independently of Factory fingerprint identity;
- initial semantic comparison of factory resources, operations, and products through the Governance semantic-change seam.

Current `ConfiguredResource` remains the supported complete configured-resource representation. [Factory Resource Semantics](../architecture/factory-resource-semantics.md) records the concluded interpretation and keeps the definition/installed-instance split **out of implementation**: repetition, catalogue reuse, or equal values are not sufficient triggers. Revisit only if an independently identified reusable technical specification must carry a checkable cross-consumer contract or dependency that complete configured-resource records cannot preserve.

## 3. Factory spatial-model work under research hold

[ADR-0017](../architecture/decisions/0017-ground-zero-semantic-evolution.md) resolves the universal maturity question and [ADR-0018](../architecture/decisions/0018-factory-semantics-after-support-reset.md) supersedes
ADR-0014. The remaining hold belongs to
[Factory semantic composition](../research/investigations/factory-model-semantic-composition.md)
and reconciliation of its independently reviewed result into a fresh owning contract.
The landed V2 shape/validation slice remains proving evidence. No V2 release or
permanent V1/V2 coexistence follows from retaining that evidence.

The retained authored spatial design constraints under ADR-0018 consist of:

- floor width and height;
- resource reference-cell position;
- resource footprint width and height;
- `ticksPerCell`;
- `handlingTicks`.

Position/footprint containment, non-overlap, and the exact maximum-transfer-duration representability predicate are publication semantics. Orientation, aisle/path topology, conveyors, explicit transport resources, connection points, congestion, floor identity, and authoritative animation coordinates are not part of v2.

The implementation sequence is owned jointly with [Spatial Runtime Consequences](spatial-runtime-consequences.md):

### PLAN-ENG-5-A1 — V2 model and validation

**Status:** Implemented as proving evidence. `factory-model:v2` canonical bytes/fingerprint policy are not released by this slice. PLAN-ENG-5-A2 was the next V2 Factory-model slice and is now dependency-blocked by the research hold above.

Implement the five authored additions and deterministic validation required by ADR-0014.

Acceptance evidence must prove:

- exact anchored footprint occupancy;
- floor containment;
- non-overlap;
- accepted zero/boundary values where the ADR permits them;
- overflow-safe maximum transfer-duration validation; and
- no change to v1 behavior or identity.

### PLAN-ENG-5-A2 — Factory canonical identity after composition

**Status:** Dependency-blocked pending Factory composition and its reconciliation.
The previous V2 release mandate is superseded. Re-scope this slice against the
resulting owning Factory contract before execution; do not implement the retained
V2 grammar simply because this coordinate exists. Required evidence will include
total canonicalization, strict rejection, exact definition attribution, no invented
historical facts, and the declared support/custody scope.

### PLAN-ENG-5-A3 — Historical policy transition

**Status:** Previous mandatory V1/V2 coexistence scope superseded by the reset;
no implementation currently admitted. A real post-reset cross-policy controlled
transition may justify a newly bounded slice under ADR-0018, with verifiers for the
policies in scope and an explicit migration/comparison contract. No pre-reset reader
or migration is owed merely to complete this former sequence.

## 4. Existing Factory capabilities that remain closed

### PLAN-FD-1 — Canonical model boundary

The behavior-preserving canonical seam is implemented for current semantics. A1 is landed proving evidence; A2/A3 are no longer implementation-ready while the research hold is active.

### PLAN-FD-2 — Executability validation

Current deterministic validation remains the implementation contract for admitted semantics. ADR-0018 preserves V2 authored spatial/arithmetic constraints without admitting V2 support.

A richer cross-consumer finding taxonomy is **not** an admitted implementation slice; it is tracked in [Factory Design Evolution Research](../research/investigations/factory-design-evolution.md).

### PLAN-FD-3 — Publication, identity, and provenance

Immutable publication and the pre-reset v1 implementation are present. V2 identity requires Factory composition reconciliation; the old V1/V2 coexistence requirement is superseded by the reset.

`ModelFingerprint` remains semantic-content identity. `ControlledRevisionId` remains Governance-owned historical occurrence identity. Neither approval, deployment, external workflow identity, nor Engine interpretation belongs in the Factory fingerprint.

### PLAN-FD-4 — Deterministic runtime instantiation

Runtime continues to instantiate from one validated published model. Derived indexes/compiled structures are not independently authored models.

Engine result-affecting interpretation is separately identified by `EngineSemanticsVersion` under ADR-0015.

### PLAN-FD-5 — Semantic comparison

The initial comparison slice for current resources, operations, and products is implemented. No additional comparison implementation is admitted now.

Finer route/policy/spatial/capability comparison and cross-consumer explanatory depth are tracked in research. A future implementation slice must be promoted from a concrete consumer or Governance requirement rather than remaining as an open-ended extension here.

## 5. Model/runtime ownership invariants

```text
Factory Design owns
    published products / operations / concrete resources
    authored behaviorally relevant constraints
    authored V2 spatial/handling facts
    validation / publication
    ModelFingerprint

Engine owns
    workload and work items
    dispatch / queues / assignments
    result-affecting interpretation policy
    transfers in progress
    runtime observations/events/performance

Governance owns
    ControlledRevisionId and authoritative revision history
    semantic ChangeSet / impact
    requirements / conformance / evidence-use / governed change

Operational work, once admitted, owns
    independently existing subject correspondence
    consequential trust/authority
    external realization / command-result facts
    deployment application
    external observations / reconciliation
```

No downstream consumer may create a second authored production model merely because it needs a convenient runtime/editor representation.

## 6. Game integration boundary

The game may own a mutable draft, catalogue/economics, score, attempt history, UI state, and other consumer-only data. It projects only supported canonical semantic facts before Arcogine validation/publication.

The game must not force unadmitted reusable-specification/capability, shared-editor, or comparison abstractions into Factory Design. Those questions remain in research until promoted.

## 7. Research boundary

The reusable definition versus installed-resource investigation is **concluded** with no implementation slice: keep one complete configured resource per designed participant unless a future technical-specification/dependency invariant proves a separate canonical identity necessary.

The following remain intentionally **not implementation work** in this plan:

- generalized capability/qualification/resource-pool/work-center semantics;
- richer stable validation findings/warnings/severity;
- finer semantic comparison beyond the implemented slice;
- shared draft lifecycle/collaboration;
- Factory-specific additions to governed-change workflow; and
- spatial/material-flow concepts beyond the retained authored constraints.

They are maintained in [Factory Design Evolution Research](../research/investigations/factory-design-evolution.md) and the research register. Do not assign new Factory delivery coordinates until the relevant question crosses the planning admission boundary.

## 8. Validation and documentation

For every admitted Factory change:

1. preserve truthful identities and the support scope actually declared under ADR-0017; old-label reuse must preserve exact meaning/bytes;
2. add deterministic boundary/golden tests for new semantic facts;
3. keep runtime construction behind published-model validation;
4. update durable architecture/ADR/reference only when actual semantics or shipped behavior changes; and
5. keep this plan synchronized with landed implementation status rather than carrying untriggered future work.
