# Factory Design Capability Implementation Plan

> **Status:** Active/partial; the canonical model with its optional spatial record, validation, work-in-progress canonical form and semantic comparison are landed. Spatial execution belongs to the Engine plan.
>
> **Scope:** Implementation-ready Factory Design work over the canonical production-system model  
> **Authority:** Planning only. Current Factory architecture and specifications govern.
>
> **Related:** [Factory Design Architecture](../architecture/factory-design.md), [canonical model boundary](../architecture/factory-design.md#4-canonical-model-boundary), [Factory publication identity contract](../architecture/factory-design.md#11-publication-identity-and-provenance), [Factory model specification](../architecture/factory-model.md), [Factory semantic-evolution contract](../architecture/factory-design.md#111-semantic-evolution), [Determinism Contract](../architecture/overview.md#determinism-contract), [Spatial Runtime Consequences](spatial-runtime-consequences.md), [Factory Design Evolution Research](../research/investigations/factory-design-evolution.md), [Factory Resource Semantics](../architecture/factory-resource-semantics.md), [Factory Model Semantic Composition Research](../research/investigations/factory-model-semantic-composition.md), [Transfer semantics boundary research](../research/investigations/transfer-semantics.md)

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

The following are implemented and must be preserved:

- a canonical `FactoryModel` / immutable `FactoryModelVersion` seam;
- product, operation, and concrete resource identities;
- an explicit optional spatial record (floor, per-resource position and footprint, `ticksPerCell`, `handlingTicks`) that is absent or present and complete;
- deterministic structural validation of the production records and, when present, the spatial record;
- immutable in-process publication;
- the current Factory canonical form and one aggregate `factory-model:sha256:<digest>` fingerprint under the [Factory model specification](../architecture/factory-model.md), deterministic for one definition and carrying no development-status or exact-definition identifier;
- runtime instantiation only from a published model, with present spatial content refused by the current Engine before any runtime state exists;
- runtime/result attribution to the source model's fingerprint;
- Governance-owned controlled revision identity/history independently of Factory fingerprint identity, currently in a disposable proving store;
- semantic comparison of factory resources, operations, and products through the Governance semantic-change seam, with coarse attribution of spatial-record changes.

Current `ConfiguredResource` remains the supported complete configured-resource representation. [Factory Resource Semantics](../architecture/factory-resource-semantics.md) records the concluded interpretation and keeps the definition/installed-instance split **out of implementation**: repetition, catalogue reuse, or equal values are not sufficient triggers. Revisit only if an independently identified reusable technical specification must carry a checkable cross-consumer contract or dependency that complete configured-resource records cannot preserve.

## 3. Factory spatial-model work

The [Factory semantic-evolution contract](../architecture/factory-design.md#111-semantic-evolution) composes the Factory model as one closed definition that may admit explicitly present optional authored records. The spatial record is the first such record. A present record carries, completely and for every configured resource:

- floor width and height;
- resource reference-cell position;
- resource footprint width and height;
- `ticksPerCell`;
- `handlingTicks`.

An absent record asserts nothing spatial, and nothing is synthesized in its place. Position/footprint containment, non-overlap, complete resource coverage, and the exact maximum-transfer-duration representability predicate are publication semantics of a present record. Orientation, aisle/path topology, conveyors, explicit transport resources, connection points, congestion, floor identity, and authoritative animation coordinates are not part of the record.

The model shape, validation and canonical form are complete (PLAN-ENG-5-A1 and PLAN-ENG-5-A2 in [Spatial Runtime Consequences](spatial-runtime-consequences.md), which owns the remaining execution sequence). A later spatial correction changes the current specification, golden vectors and dependents together; no successor identity, migration or dual publication is planned merely for development material. No generic migration or schema-evolution framework is admitted.

## 4. Existing Factory capabilities that remain closed

### PLAN-FD-1 — Canonical model boundary

The canonical seam is implemented, including the optional spatial record.

### PLAN-FD-2 — Executability validation

Current deterministic validation remains the implementation contract for admitted semantics, including the spatial-record predicates required by the [Factory model](../architecture/factory-model.md).

A richer cross-consumer finding taxonomy is **not** an admitted implementation slice; it is tracked in [Factory Design Evolution Research](../research/investigations/factory-design-evolution.md).

### PLAN-FD-3 — Publication, identity, and provenance

Immutable publication and the current content fingerprint are implemented. A stability/support promotion is an explicit owner decision under the [semantic evolution rules](../architecture/overview.md#semantic-evolution-and-support), made when a concrete durable-use need exists; it is not planned work here and does not imply that `ModelFingerprint` itself becomes an exact definition reference.

`ModelFingerprint` remains the Factory canonical-content fingerprint. `ControlledRevisionId` remains Governance-owned historical occurrence identity. Neither approval, deployment, external workflow identity, nor Engine interpretation belongs in the Factory fingerprint.

### PLAN-FD-4 — Deterministic runtime instantiation

Runtime continues to instantiate from one validated published model. Derived indexes/compiled structures are not independently authored models.

Engine result-affecting interpretation remains separately owned by the [Engine semantics](../architecture/engine-semantics.md) contract. No dedicated Engine-definition identifier is currently exposed; exact-reference needs remain separate from runtime instantiation.

### PLAN-FD-5 — Semantic comparison

The comparison slice for current resources, operations, and products is implemented, and a spatial-record addition, removal or change is attributed coarsely to the model's spatial-record entity so it never disappears behind an empty change list. No additional comparison implementation is admitted now.

Finer route/policy/spatial/capability comparison and cross-consumer explanatory depth are tracked in research. A future implementation slice must be promoted from a concrete consumer or Governance requirement rather than remaining as an open-ended extension here.

## 5. Model/runtime ownership invariants

```text
Factory Design owns
    published products / operations / concrete resources
    authored behaviorally relevant constraints
    authored optional spatial/handling record
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
- spatial/material-flow concepts beyond the current spatial record.

They are maintained in [Factory Design Evolution Research](../research/investigations/factory-design-evolution.md) and the research register. Do not assign new Factory delivery coordinates until the relevant question crosses the planning admission boundary.

## 8. Validation and documentation

For every admitted Factory change:

1. keep the specification, golden vectors and dependents synchronized with the current work-in-progress definition, regenerating vectors deliberately when the definition changes;
2. add deterministic boundary/golden tests for new semantic facts;
3. keep runtime construction behind published-model validation;
4. update durable architecture/specification/reference only when actual semantics or shipped behavior changes; and
5. keep this plan synchronized with landed implementation status rather than carrying untriggered future work.
