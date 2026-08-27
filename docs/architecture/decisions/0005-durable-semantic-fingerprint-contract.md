# ADR-0005: Durable semantic fingerprint contract

Status: Accepted
Date: 2026-08-27

## Context

[ADR-0004](0004-model-identity-revision-lineage-and-external-change-control.md) separates two concepts that Arcogine must not collapse:

1. **semantic identity** — whether two canonical model states represent the same semantic content under a defined identity policy; and
2. **controlled revision identity** — which historical governed occurrence of semantic content is being referenced.

ADR-0004 deliberately left the concrete durable fingerprint policy unresolved. The factory domain now provides the first implemented proving ground: `FactoryModelVersion.contentHash()` deterministically computes a SHA-256 digest from canonical factory-model content, and that digest already participates in runtime/result provenance. Its contract is nevertheless explicitly provisional: it is not yet a persisted, public, or cross-process compatibility guarantee.

That ambiguity now blocks the first Governance and Conformance delivery slice. A durable fingerprint cannot be established by merely renaming the existing hash. Arcogine must define what semantic content participates, how that content is canonicalized, how collection ordering is interpreted, how the policy evolves, and what compatibility future implementations must preserve.

The current factory model is also not semantically neutral with respect to all list ordering. `OperationDefinition.steps()` is explicitly ordered, and the current runtime preserves product ordering into demand generation, where products are selected by deterministic RNG index. Normalizing those collections as unordered during fingerprinting could therefore assign one fingerprint to models that can produce different behavior under the same scenario seed.

This ADR defines the durable semantic fingerprint contract and the first policy, `factory-model:v1`. It intentionally does **not** choose a controlled revision identifier, lineage policy, or revision repository. Those are separate historical-governance decisions and will be addressed independently.

Related decisions and plans:

- [ADR-0003: Canonical factory model boundary](0003-canonical-factory-model-boundary.md)
- [ADR-0004: Model identity, revision lineage, and external change control](0004-model-identity-revision-lineage-and-external-change-control.md)
- [Governance and Conformance Architecture](../governance-conformance.md)
- [Governance and Conformance Capability Plan](../../planning/governance-conformance-capability.md)
- [Factory Design Capability Plan](../../planning/factory-design-capability.md)

## Decision

### Model fingerprints are namespaced and policy-versioned

A durable `ModelFingerprint` identifies canonical semantic content under an explicit fingerprint policy.

A fingerprint consists conceptually of:

```text
namespace
policy version
algorithm
digest
```

For the first factory-model policy:

```text
namespace      = factory-model
policy version = v1
algorithm      = sha256
digest         = 64 lowercase hexadecimal characters
```

Its canonical textual representation is:

```text
factory-model:v1:sha256:<digest>
```

The policy version identifies the semantic/canonicalization contract. It is independent of the cryptographic algorithm version. Keeping SHA-256 while changing field membership, ordering semantics, normalization, framing, or any other canonicalization rule requires a new fingerprint policy version.

### `factory-model:v1` freezes the current semantic field membership

The v1 fingerprint includes every field currently included by `FactoryModelVersion.contentHash()`.

```text
FactoryModel
    resources, in list order
        ResourceDefinition.id
        ResourceDefinition.name
        ResourceDefinition.concurrency
        ResourceDefinition.capacityLiters
        ResourceDefinition.setupTime

    operations, in list order
        OperationDefinition.id
        OperationDefinition.name
        steps, in list order
            OperationStepDefinition.stepId
            OperationStepDefinition.name
            OperationStepDefinition.duration
            OperationStepDefinition.eligibleResources,
                canonicalized by ascending MachineId

    products, in list order
        ProductDefinition.id
        ProductDefinition.name
        ProductDefinition.operationId
```

Names and allocated IDs therefore participate in `factory-model:v1` semantic identity.

This ADR does not claim that names or current IDs are the ideal permanent ontology for factory semantics. It establishes a durable identity policy over the canonical model Arcogine has today. Reclassifying a field as presentation-only, introducing stable logical identity distinct from current allocated IDs, or changing the canonical factory model is a model-semantics decision and requires a new fingerprint policy version if it changes fingerprint equivalence.

### Current top-level list ordering remains semantic in v1

`resources`, `operations`, and `products` are encoded in their canonical list order. `OperationDefinition.steps()` is likewise encoded in order.

This is intentionally conservative. The v1 fingerprint reflects the semantics and deterministic-runtime contract of the current canonical model; it does not attempt to define a stronger order-independent equivalence than the implementation presently guarantees.

In particular, product order is behavior-affecting today because runtime assembly preserves product order and deterministic demand generation selects by list index. Treating product order as non-semantic could therefore cause two models capable of different seeded outcomes to share a fingerprint.

If a future canonical model or runtime establishes that one of these collections is semantically unordered, that change does not alter v1. A new fingerprint policy version must encode the new equivalence relation.

### Set-shaped values are canonicalized independently of iteration order

`OperationStepDefinition.eligibleResources` is set-shaped. Its `MachineId` values are sorted into ascending ID order before encoding.

Equivalent sets must therefore produce the same v1 fingerprint regardless of the source collection's iteration order.

### Canonical scalar encoding is explicit and unambiguous

`factory-model:v1` retains the current length-framed scalar representation used by `FactoryModelVersion.contentHash()`.

Each scalar value is converted using the current canonical scalar rendering and framed as:

```text
<string-length>:<string-value>
```

The resulting canonical representation is encoded as UTF-8 bytes and hashed with SHA-256.

The framing is part of the v1 contract. It prevents delimiter-bearing values from creating ambiguous concatenations without introducing dependency on a general-purpose serialization format.

The v1 policy performs no implicit trimming, case folding, Unicode normalization, locale-sensitive transformation, or presentation cleanup. Null and non-null values remain distinct according to the canonical scalar rendering defined by the implementation and pinned by compatibility vectors.

Changing scalar rendering, framing, character encoding, normalization, field ordering, or field membership requires a new policy version.

### SHA-256 collision resistance is sufficient for operational identity

Arcogine treats equality of correctly formed `factory-model:v1` fingerprints as equality of semantic content under the v1 policy for operational identity purposes.

This is a practical cryptographic identity guarantee, not a mathematical assertion that SHA-256 is injective over every possible model representation. Arcogine relies on SHA-256 collision resistance as sufficient for the identity and provenance use cases governed by this ADR.

### Java equality is not the durable semantic-identity contract

`FactoryModel.equals()` remains Java record/representation equality. This ADR does not redefine it as Arcogine's durable semantic identity relation.

For `factory-model:v1`, implementations must preserve the following implication:

```text
FactoryModel a equals FactoryModel b
    =>
fingerprint(a) equals fingerprint(b)
```

The reverse implication is not established as a permanent platform invariant. Consumers that need durable semantic identity must compare fingerprints under the same named fingerprint policy rather than depend on Java object equality.

### Existing `contentHash()` remains a compatibility projection

For compatibility with the current factory/runtime seam:

```text
FactoryModelVersion.contentHash()
```

continues to expose the raw 64-character lowercase SHA-256 digest corresponding to `factory-model:v1`.

The durable fingerprint is the typed/namespaced identity. The existing raw hash is a compatibility projection of its digest component, not a second identity policy.

Existing runtime and result provenance that stores `modelContentHash` may therefore continue to carry the raw digest without changing factory behavior in the first implementation slice. Where a consumer needs durable cross-process identity semantics, the digest must be interpreted under the explicitly documented `factory-model:v1` policy rather than as an unversioned hash with unknown semantics.

### Released fingerprint policies are immutable

Once `factory-model:v1` is released, every supported Arcogine implementation of that policy must produce the same fingerprint for the same v1 canonical semantic content across process boundaries and software versions.

Refactoring the implementation is permitted. Silently changing any of the following while continuing to label the result `factory-model:v1` is not:

- semantic field membership;
- collection ordering rules;
- set canonicalization;
- scalar rendering;
- framing;
- null handling;
- text normalization;
- character encoding;
- hashing algorithm;
- digest rendering.

A change to any of those rules that can change identity requires a new policy version.

### Golden compatibility vectors are part of the contract

The implementation of a durable fingerprint policy must include pinned golden compatibility vectors with literal expected outputs.

At minimum, v1 tests must cover:

- a representative canonical factory model with an exact expected digest/fingerprint;
- equality of repeated computations across independently constructed equal models;
- sensitivity to semantic field changes, including names and IDs;
- sensitivity to resource, operation, product, and operation-step ordering;
- invariance to `eligibleResources` set iteration order;
- delimiter-bearing and Unicode text values;
- null versus non-null optional values where applicable;
- byte-for-byte compatibility between the durable v1 digest and the existing `contentHash()` result.

These vectors are compatibility fixtures, not implementation suggestions. A future refactor that changes a v1 golden result is a contract break unless the fingerprint policy version also changes.

## Alternatives considered

### Promote the existing bare SHA-256 string without policy metadata

This would require the least code and preserve current consumers unchanged.

It was rejected because a bare digest does not identify the canonicalization policy that produced it. Future changes to field membership, ordering, or normalization could silently redefine identity while retaining the same apparent format.

### Canonicalize through JSON

A canonical JSON representation could be human-inspectable and reuse existing serializers.

It was rejected for v1 because it would introduce additional compatibility dependencies — serializer behavior, property ordering, escaping, null rendering, numeric rendering, and library-version behavior — without improving the semantic contract. The existing length-framed representation is small, deterministic, and already avoids delimiter ambiguity.

### Sort all top-level model collections

This would make fingerprints insensitive to source ordering and might appear closer to a mathematical semantic model.

It was rejected because current model/runtime semantics do not justify that equivalence. Operation-step order is explicitly meaningful, and current product ordering can affect deterministic demand generation. The fingerprint must not erase behaviorally relevant distinctions merely to produce a tidier canonical form.

### Exclude names from the fingerprint

Names could be treated as presentation metadata so renaming an object would not change semantic identity.

It was rejected for v1 because names are currently canonical domain fields rather than a separately modeled presentation layer. Reclassifying them is a model-semantics decision that should be made explicitly and, if adopted, expressed in a later fingerprint policy.

### Exclude allocated IDs from the fingerprint

This could allow independently imported models with newly allocated IDs to reproduce a fingerprint.

It was rejected for v1 because current IDs participate in canonical references and Arcogine has not yet defined a stable logical-identity layer independent of those IDs. Treating them as non-semantic now would assert an equivalence the current domain model does not establish.

### Combine fingerprint and controlled-revision decisions in this ADR

This would keep all G1 identity choices in one place.

It was rejected because ADR-0004's principal architectural distinction is that semantic content identity and historical revision identity are different concerns. Recombining their concrete mechanisms in the next ADR would weaken that boundary. Controlled revision identifiers, lineage rules, rollback/branch semantics, and provenance minimums will be decided separately.

## Consequences

As a result of this decision:

- Arcogine can promote factory semantic identity from a provisional process-local hash convention to a named, versioned, cross-process compatibility contract;
- `factory-model:v1` intentionally preserves the current field membership and ordering semantics rather than redesigning factory semantic equivalence inside governance work;
- existing `contentHash()` and `modelContentHash` consumers can remain behaviorally compatible while a typed durable fingerprint is introduced additively;
- future canonical-model evolution can introduce a new fingerprint policy without rewriting the meaning of historical v1 identifiers;
- semantic identity remains independent of authorship, time, workflow state, approval, deployment, and external change-management systems;
- controlled revision identity and lineage remain unresolved by this ADR and must not be inferred from the fingerprint;
- compatibility tests become part of the architecture contract, so accidental canonicalization changes fail visibly rather than silently changing persisted identity.

The principal cost is that Arcogine is deliberately committing to some current representation-sensitive semantics — notably names, current IDs, and top-level list ordering — for `factory-model:v1`. That cost is preferable to retroactively declaring current distinguishable models equivalent. A future, better-factored canonical model may define a cleaner v2 equivalence relation without invalidating v1 history.

## Non-goals

This ADR does not decide:

- `ControlledRevisionId` representation;
- revision lineage, branching, merge, or rollback policy;
- controlled revision persistence or repository authority;
- model artifact serialization/retention;
- schema evolution or migration policy;
- semantic `ChangeSet` representation;
- whether future factory names become presentation-only metadata;
- whether future logical identity is separated from current allocated IDs;
- a universal canonicalization scheme shared by all Arcogine domains;
- Challenge Readiness identity/versioning;
- approval, authorization, deployment, or external workflow semantics.

A subsequent ADR should address controlled revision identity and lineage independently.

## Charter alignment

This decision supports the Product Charter's determinism and provenance principles by making semantic identity reproducible and historically interpretable across processes and versions. It also preserves domain ownership: the factory domain defines the canonical semantic content of its proving-ground model, while Governance supplies the architectural requirement that durable identities be explicit, versioned, and reproducible rather than accidental properties of an implementation hash.
