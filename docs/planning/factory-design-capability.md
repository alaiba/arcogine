# Factory Design Capability Implementation Plan

> **Status:** Active/partial; current Factory Model v1 capability and the V2 shape/validation proving slice are landed. The semantic-contract maturity question is reconciled in ADR-0017; unimplemented V2 identity/coexistence work remains dependency-blocked on the Factory composition research  
> **Scope:** Implementation-ready Factory Design work over the canonical production-system model  
> **Authority:** Planning only. Accepted Factory ADRs remain current architecture until superseded; this plan deliberately pauses work that would deepen the questioned linear-version commitment while the composition research is open.  
> **Related:** [Factory Design Architecture](../architecture/factory-design.md), [ADR-0003](../architecture/decisions/0003-canonical-factory-model-boundary.md), [ADR-0004](../architecture/decisions/0004-model-identity-revision-lineage-and-external-change-control.md), [ADR-0006](../architecture/decisions/0006-durable-semantic-fingerprint-contract.md), [ADR-0017](../architecture/decisions/0017-semantic-contract-maturity-and-support-promotion.md) (superseding [ADR-0014](../architecture/decisions/0014-factory-model-semantic-policy-evolution.md)), [ADR-0015](../architecture/decisions/0015-engine-semantics-identity-and-reproducibility.md), [Factory Model v2](../architecture/factory-model-v2.md), [Spatial Runtime Consequences](spatial-runtime-consequences.md), [Factory Design Evolution Research](../research/investigations/factory-design-evolution.md), [Factory Resource Semantics](../architecture/factory-resource-semantics.md), [Semantic Contract Maturity and Durability Research](../research/investigations/semantic-contract-maturity-durability.md), [Factory Model Semantic Composition Research](../research/investigations/factory-model-semantic-composition.md)

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

Current `ConfiguredResource` remains the supported complete configured-resource representation. [Factory Resource Semantics](../architecture/factory-resource-semantics.md) records the concluded interpretation and keeps the definition/installed-instance split **out of implementation**: repetition, catalogue reuse, or equal values are not sufficient triggers. Revisit only if an independently identified reusable technical specification must carry a checkable cross-consumer contract or dependency that complete configured-resource records cannot preserve.

## 3. Factory spatial-model work under research hold

[ADR-0017](../architecture/decisions/0017-semantic-contract-maturity-and-support-promotion.md) §8 carries forward the Factory policy rules of the superseded ADR-0014 and therefore still defines `factory-model:v2` as exactly v1 semantic content plus required authored spatial/handling facts. Two high-risk investigations questioned that estate:

- [Semantic Contract Maturity and Durability Research](../research/investigations/semantic-contract-maturity-durability.md) — **concluded and reconciled** in ADR-0017: `factory-model:v2` is a proving contract with an explicit exit condition ([Factory Model v2](../architecture/factory-model-v2.md) §10), the automatic permanent-resolution and automatic `vN+1` triggers are replaced, `factory-model:v1`'s promises are retained, and a V2 fingerprint may not be produced or accepted into retained custody before a promotion record or custody declaration exists; and
- [Factory Model Semantic Composition Research](../research/investigations/factory-model-semantic-composition.md) — **still open**, and still the prerequisite for the slices below, because it decides whether the whole-model V2 shape is the successor V1 should have at all.

This remains an **implementation hold, not an architectural supersession**. The already-landed V2 model/validation slice remains useful proving evidence. Do not start V2 canonical identity, V1/V2 coexistence, or another unimplemented Factory slice whose purpose is to harden the current linear-policy assumption until the composition question has a decision-quality result, its required adversarial review, and any necessary architecture/planning reconciliation has landed. The maturity reconciliation alone does not restart any held slice.

Under the current ADR-0017 §8 rules, V2 consists of:

- floor width and height;
- resource reference-cell position;
- resource footprint width and height;
- `ticksPerCell`;
- `handlingTicks`.

Position/footprint containment, non-overlap, and the exact maximum-transfer-duration representability predicate are publication semantics. Orientation, aisle/path topology, conveyors, explicit transport resources, connection points, congestion, floor identity, and authoritative animation coordinates are not part of v2.

The implementation sequence is owned jointly with [Spatial Runtime Consequences](spatial-runtime-consequences.md):

### PLAN-ENG-5-A1 — V2 model and validation

**Status:** Implemented as proving evidence. `factory-model:v2` canonical bytes/fingerprint policy are not released by this slice. PLAN-ENG-5-A2 was the next V2 Factory-model slice and is now dependency-blocked by the research hold above.

Implement the five authored additions and deterministic validation required by ADR-0017 §8 rule 3.

Acceptance evidence must prove:

- exact anchored footprint occupancy;
- floor containment;
- non-overlap;
- accepted zero/boundary values where the ADR permits them;
- overflow-safe maximum transfer-duration validation; and
- no change to v1 behavior or identity.

### PLAN-ENG-5-A2 — V2 canonical identity

**Status:** Dependency-blocked. Do not implement while the Factory composition investigation is open.

Implement the exact V2 canonical bytes and fingerprint policy from [Factory Model v2](../architecture/factory-model-v2.md).

Acceptance evidence must prove:

- required golden vectors;
- deterministic equivalent-content fingerprints;
- every authored V2 field participates in identity;
- V1 vectors/fingerprints remain byte-for-byte unchanged;
- no automatic v1-to-v2 lift or synthesized historical spatial defaults; and
- malformed or semantically invalid V2 artifacts fail explicitly.

Under ADR-0017 this slice implements a **proving** policy, not a released one: it must not register a V2 verifier with a retained authority unless that authority records the exact grammar revision and custody declaration with each accepted V2 artifact (invariant 9 of the [Identity/History Compatibility Guard](governance-continuity.md)), and it must either deliver V2's promotion record per ADR-0017 §6 and [Factory Model v2](../architecture/factory-model-v2.md) §10.3 or leave V2 explicitly proving with that record as a named later step.

### PLAN-ENG-5-A3 — V1/V2 historical coexistence

**Status:** Dependency-blocked. Do not implement while the Factory composition investigation is open.

Use the landed Governance revision authority to keep both policies historically resolvable and verifiable for the horizon each one promises: permanently for `factory-model:v1`, and for V2 per its custody declaration or promotion record (ADR-0017 §8 rule 8).

Acceptance evidence must prove:

- historical v1 resolution remains intact after V2 registration;
- v2 resolution is independent;
- controlled revision lineage may cross policy versions without rewriting either artifact;
- the first cross-policy transition is not falsely represented as an ordinary same-policy empty/equivalent diff; and
- a V2 artifact accepted while V2 is proving carries its exact grammar revision and custody declaration, and is rejected explicitly when the authority cannot record them.

This slice implements only the first coexistence/migration seam actually required by V1/V2. It does not create a generic schema-migration framework, and it does not make V2's resolution permanent unless V2's promotion record says so.

## 4. Existing Factory capabilities that remain closed

### PLAN-FD-1 — Canonical model boundary

The behavior-preserving canonical seam is implemented for current semantics. A1 is landed proving evidence; A2/A3 are no longer implementation-ready while the research hold is active.

### PLAN-FD-2 — Executability validation

Current deterministic validation remains the implementation contract for admitted semantics. V2 adds only the accepted spatial/arithmetic predicates required by ADR-0017 §8 rule 3.

A richer cross-consumer finding taxonomy is **not** an admitted implementation slice; it is tracked in [Factory Design Evolution Research](../research/investigations/factory-design-evolution.md).

### PLAN-FD-3 — Publication, identity, and provenance

Immutable publication and the currently accepted v1 semantic-identity contract are implemented. V2 identity/coexistence was previously admitted through A2/A3 and is now dependency-blocked pending research and reconciliation.

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
- spatial/material-flow concepts beyond accepted v2.

They are maintained in [Factory Design Evolution Research](../research/investigations/factory-design-evolution.md) and the research register. Do not assign new Factory delivery coordinates until the relevant question crosses the planning admission boundary.

## 8. Validation and documentation

For every admitted Factory change:

1. preserve released v1 golden identity behavior;
2. add deterministic boundary/golden tests for new semantic facts;
3. keep runtime construction behind published-model validation;
4. update durable architecture/ADR/reference only when actual semantics or shipped behavior changes; and
5. keep this plan synchronized with landed implementation status rather than carrying untriggered future work.
