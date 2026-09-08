# Factory Design Capability Implementation Plan

> **Status:** Active/partial; current implementation is complete for Factory Model v1, while the admitted Factory Model v2 work is delivered through the spatial-runtime implementation sequence  
> **Scope:** Implementation-ready Factory Design work over the canonical production-system model  
> **Authority:** Planning only; unresolved ontology, diagnostics, comparison, and authoring questions live in research  
> **Related:** [Factory Design Architecture](../architecture/factory-design.md), [ADR-0003](../architecture/decisions/0003-canonical-factory-model-boundary.md), [ADR-0004](../architecture/decisions/0004-model-identity-revision-lineage-and-external-change-control.md), [ADR-0006](../architecture/decisions/0006-durable-semantic-fingerprint-contract.md), [ADR-0014](../architecture/decisions/0014-factory-model-semantic-policy-evolution.md), [ADR-0015](../architecture/decisions/0015-engine-semantics-identity-and-reproducibility.md), [Factory Model v2](../architecture/factory-model-v2.md), [Spatial Runtime Consequences](spatial-runtime-consequences.md), [Factory Design Evolution Research](../research/factory-design-evolution.md)

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
- product, operation, and concrete resource identities under the released v1 policy;
- deterministic structural executability validation;
- immutable publication;
- durable `factory-model:v1` fingerprint semantics under ADR-0006;
- runtime instantiation only from a published model;
- runtime/result attribution to source model identity;
- Governance-owned controlled revision identity/history independently of Factory fingerprint identity;
- initial semantic comparison of factory resources, operations, and products through the Governance semantic-change seam.

Current `ResourceDefinition` remains the supported concrete resource representation. This plan does not reinterpret it as a reusable equipment type.

## 3. Admitted Factory Model v2 work

ADR-0014 establishes `factory-model:v2` as exactly v1 semantic content plus required authored spatial/handling facts:

- floor width and height;
- resource reference-cell position;
- resource footprint width and height;
- `ticksPerCell`;
- `handlingTicks`.

Position/footprint containment, non-overlap, and the exact maximum-transfer-duration representability predicate are publication semantics. Orientation, aisle/path topology, conveyors, explicit transport resources, connection points, congestion, floor identity, and authoritative animation coordinates are not part of v2.

The implementation sequence is owned jointly with [Spatial Runtime Consequences](spatial-runtime-consequences.md):

### PLAN-ENG-5-A1 — V2 model and validation

Implement the five authored additions and deterministic validation required by ADR-0014.

Acceptance evidence must prove:

- exact anchored footprint occupancy;
- floor containment;
- non-overlap;
- accepted zero/boundary values where the ADR permits them;
- overflow-safe maximum transfer-duration validation; and
- no change to v1 behavior or identity.

### PLAN-ENG-5-A2 — V2 canonical identity

Implement the exact V2 canonical bytes and fingerprint policy from [Factory Model v2](../architecture/factory-model-v2.md).

Acceptance evidence must prove:

- required golden vectors;
- deterministic equivalent-content fingerprints;
- every authored V2 field participates in identity;
- V1 vectors/fingerprints remain byte-for-byte unchanged;
- no automatic v1-to-v2 lift or synthesized historical spatial defaults; and
- malformed or semantically invalid V2 artifacts fail explicitly.

### PLAN-ENG-5-A3 — V1/V2 historical coexistence

Use the landed Governance revision authority to keep both released policies historically resolvable and verifiable.

Acceptance evidence must prove:

- historical v1 resolution remains intact after V2 registration;
- v2 resolution is independent;
- controlled revision lineage may cross policy versions without rewriting either artifact; and
- the first cross-policy transition is not falsely represented as an ordinary same-policy empty/equivalent diff.

This slice implements only the first coexistence/migration seam actually required by V1/V2. It does not create a generic schema-migration framework.

## 4. Existing Factory capabilities that remain closed

### PLAN-FD-1 — Canonical model boundary

The behavior-preserving canonical seam is implemented for current semantics. V2 additions are admitted only through the concrete A1/A2/A3 work above.

### PLAN-FD-2 — Executability validation

Current deterministic validation remains the implementation contract for admitted semantics. V2 adds only the accepted spatial/arithmetic predicates required by ADR-0014.

A richer cross-consumer finding taxonomy is **not** an admitted implementation slice; it is tracked in [Factory Design Evolution Research](../research/factory-design-evolution.md).

### PLAN-FD-3 — Publication, identity, and provenance

Immutable publication and v1 durable semantic identity are implemented. V2 identity/coexistence is admitted through A2/A3 above.

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

The game must not force unresolved equipment-type, shared-editor, or comparison abstractions into Factory Design. Those questions remain in research until promoted.

## 7. Research boundary

The following are intentionally **not implementation work** in this plan:

- reusable equipment definition versus installed resource ontology;
- generalized capability/resource-pool/work-center semantics;
- richer stable validation findings/warnings/severity;
- finer semantic comparison beyond the implemented slice;
- shared draft lifecycle/collaboration;
- Factory-specific additions to governed-change workflow; and
- spatial/material-flow concepts beyond accepted v2.

They are maintained in [Factory Design Evolution Research](../research/factory-design-evolution.md) and the research register. Do not assign new Factory delivery coordinates until the relevant question crosses the planning admission boundary.

## 8. Validation and documentation

For every admitted Factory change:

1. preserve released v1 golden identity behavior;
2. add deterministic boundary/golden tests for new semantic facts;
3. keep runtime construction behind published-model validation;
4. update durable architecture/ADR/reference only when actual semantics or shipped behavior changes; and
5. keep this plan synchronized with landed implementation status rather than carrying untriggered future work.
