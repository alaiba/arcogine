# Factory Design Capability Implementation Plan

> **Status:** Active/partial; current Factory Model v1 capability and the V2 optional-record shape/validation proving slice are landed; V2 canonical identity is dependency-blocked first on transfer-lifecycle research and then on re-bounded Engine applicability, and cross-policy historical resolution waits for a real transition
>
> **Scope:** Implementation-ready Factory Design work over the canonical production-system model  
> **Authority:** Planning only. Current Factory architecture and specifications govern.
>
> **Related:** [Factory Design Architecture](../architecture/factory-design.md), [canonical model boundary](../architecture/factory-design.md#4-canonical-model-boundary), [Factory publication identity contract](../architecture/factory-design.md#11-publication-identity-and-provenance), [Factory Model v1 specification](../architecture/factory-model-v1.md), [Factory semantic-evolution contract](../architecture/factory-design.md#111-semantic-evolution), [Determinism Contract](../architecture/overview.md#determinism-contract), [Factory Model v2](../architecture/factory-model-v2.md), [Spatial Runtime Consequences](spatial-runtime-consequences.md), [Factory Design Evolution Research](../research/investigations/factory-design-evolution.md), [Factory Resource Semantics](../architecture/factory-resource-semantics.md), [Factory Model Semantic Composition Research](../research/investigations/factory-model-semantic-composition.md), [Transfer semantics boundary research](../research/investigations/transfer-semantics.md), [Successor Engine applicability research](../research/investigations/engine-applicability-after-transfer-boundary.md)

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
- durable `factory-model:v1` fingerprint semantics under the Factory Model v1 specification;
- runtime instantiation only from a published model;
- runtime/result attribution to source model identity;
- Governance-owned controlled revision identity/history independently of Factory fingerprint identity;
- initial semantic comparison of factory resources, operations, and products through the Governance semantic-change seam.

Current `ConfiguredResource` remains the supported complete configured-resource representation. [Factory Resource Semantics](../architecture/factory-resource-semantics.md) records the concluded interpretation and keeps the definition/installed-instance split **out of implementation**: repetition, catalogue reuse, or equal values are not sufficient triggers. Revisit only if an independently identified reusable technical specification must carry a checkable cross-consumer contract or dependency that complete configured-resource records cannot preserve.

## 3. Factory spatial-model work

The [Factory semantic-evolution contract](../architecture/factory-design.md#111-semantic-evolution) composes Factory policies as closed grammars that may admit explicitly present optional authored records. [Factory Model v2](../architecture/factory-model-v2.md) is the first such policy and is not released: the unchanged V1 production records plus one optional spatial record. A present record carries, completely and for every configured resource:

- floor width and height;
- resource reference-cell position;
- resource footprint width and height;
- `ticksPerCell`;
- `handlingTicks`.

An absent record asserts nothing spatial, and nothing is synthesized in its place. Position/footprint containment, non-overlap, complete resource coverage, and the exact maximum-transfer-duration representability predicate are publication semantics of a present record. Orientation, aisle/path topology, conveyors, explicit transport resources, connection points, congestion, floor identity, and authoritative animation coordinates are not part of v2.

The implementation sequence is owned jointly with [Spatial Runtime Consequences](spatial-runtime-consequences.md). No generic migration or schema-evolution framework is admitted.

### PLAN-ENG-5-A1 — V2 model and validation

**Status:** Implemented as proving evidence for the reconciled optional-record shape. `factory-model:v2` canonical bytes/fingerprint policy are not released by this slice.

Acceptance evidence proves:

- the production records keep one authoritative representation and V1-shaped validation;
- an absent spatial record validates production semantics only and synthesizes nothing;
- a present record with legal zero values is a different design from an absent record;
- a present record places every configured resource exactly once, in resource-list order, and no unknown identifier;
- exact anchored footprint occupancy, floor containment, non-overlap, accepted zero/boundary values, and overflow-safe maximum transfer-duration validation;
- no public projection through which V2 content could be published under V1, and no change to v1 behavior or identity.

### PLAN-ENG-5-A2 — V2 canonical identity

**Status:** Dependency-blocked on the READY [transfer-lifecycle question](../research/investigations/transfer-semantics.md#ready--transfer-lifecycle-independence) and then on the [successor Engine applicability question](../research/investigations/engine-applicability-after-transfer-boundary.md). The Factory-composition result remains current authority, but the transfer investigation may reopen the still-unreleased V2 grammar if spatial presence proves to be the wrong discriminator for transfer behavior. Releasing V2 publication identity would fix that grammar, so publication must wait until the transfer boundary and executing Engine identity are settled.

Implement the exact V2 canonicalizer, verifier, and fingerprint policy from [Factory Model v2](../architecture/factory-model-v2.md), with the support declaration that release requires, including whether `factory-model:v1` publication continues for production-only designs.

Acceptance evidence must prove:

- the specification's required golden vectors, including spatial absent, spatial present with legal zero values, the absent-versus-zero distinction, marker framing, and coverage rejection;
- canonical decode/re-encode and deterministic equivalent-content fingerprints;
- every authored V2 fact, and the spatial record's presence, participates in identity;
- V1 vectors/fingerprints remain byte-for-byte unchanged;
- no automatic v1-to-v2 lift, no synthesized spatial defaults, and no V1 republication of V2 content with its spatial record dropped; and
- malformed or semantically invalid V2 artifacts fail explicitly.

### PLAN-ENG-5-A3 — Historical resolution for the first cross-policy transition

**Status:** Not started. Implement only when a real controlled transition between `factory-model:v1` and `factory-model:v2` artifacts needs it; prerequisite PLAN-ENG-5-A2.

Use the landed Governance revision authority to keep the policies in scope historically resolvable and verifiable.

Acceptance evidence must prove:

- historical v1 resolution remains intact after V2 registration;
- v2 resolution is independent;
- controlled revision lineage may cross policy versions without rewriting either artifact; and
- the transition is not falsely represented as an ordinary same-policy empty/equivalent diff, and any common comparison representation it uses invents nothing and preserves every V1 distinction.

This slice implements only the seam that transition requires. It does not create a generic schema-migration framework.
## 4. Existing Factory capabilities that remain closed

### PLAN-FD-1 — Canonical model boundary

The behavior-preserving canonical seam is implemented for current semantics. A1 is landed proving evidence; A2 and A3 prerequisites are stated in §3.

### PLAN-FD-2 — Executability validation

Current deterministic validation remains the implementation contract for admitted semantics. V2 adds only the spatial-record predicates required by [Factory Model v2](../architecture/factory-model-v2.md).

A richer cross-consumer finding taxonomy is **not** an admitted implementation slice; it is tracked in [Factory Design Evolution Research](../research/investigations/factory-design-evolution.md).

### PLAN-FD-3 — Publication, identity, and provenance

Immutable publication and the currently accepted v1 semantic-identity contract are implemented. V2 identity is admitted through A2 and dependency-blocked as §3 states; A3 follows only a real cross-policy transition.

`ModelFingerprint` remains semantic-content identity. `ControlledRevisionId` remains Governance-owned historical occurrence identity. Neither approval, deployment, external workflow identity, nor Engine interpretation belongs in the Factory fingerprint.

### PLAN-FD-4 — Deterministic runtime instantiation

Runtime continues to instantiate from one validated published model. Derived indexes/compiled structures are not independently authored models.

Engine result-affecting interpretation is separately identified by `EngineSemanticsVersion` under the Determinism Contract.

### PLAN-FD-5 — Semantic comparison

The initial comparison slice for current resources, operations, and products is implemented. No additional comparison implementation is admitted now.

Finer route/policy/spatial/capability comparison and cross-consumer explanatory depth are tracked in research. A future implementation slice must be promoted from a concrete consumer or Governance requirement rather than remaining as an open-ended extension here.

## 5. Model/runtime ownership invariants

```text
Factory Design owns
    published products / operations / concrete resources
    authored behaviorally relevant constraints
    authored optional V2 spatial/handling record
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
- spatial/material-flow concepts beyond the v2 spatial record.

They are maintained in [Factory Design Evolution Research](../research/investigations/factory-design-evolution.md) and the research register. Do not assign new Factory delivery coordinates until the relevant question crosses the planning admission boundary.

## 8. Validation and documentation

For every admitted Factory change:

1. preserve released v1 golden identity behavior;
2. add deterministic boundary/golden tests for new semantic facts;
3. keep runtime construction behind published-model validation;
4. update durable architecture/specification/reference only when actual semantics or shipped behavior changes; and
5. keep this plan synchronized with landed implementation status rather than carrying untriggered future work.
