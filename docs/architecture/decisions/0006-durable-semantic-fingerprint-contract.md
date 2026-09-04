# ADR-0006: Durable semantic fingerprint contract

Status: Accepted
Date: 2026-08-27
Amendment: 2026-09-03 — replaced transient Governance delivery terminology with semantic terminology; no semantic change

## Context

[ADR-0004](0004-model-identity-revision-lineage-and-external-change-control.md) separates semantic identity from controlled revision identity. Semantic identity answers whether two canonical model states represent the same semantic content under a defined policy; controlled revision identity answers which historical governed occurrence of that content is being referenced.

`FactoryModelVersion.contentHash()` is the current factory proving ground for content-derived identity, but ADR-0004 explicitly treats it as provisional rather than a persisted, public, or cross-process compatibility guarantee. Its current representation also depends on Java-specific behavior (`String.valueOf`, `String.length()`, then UTF-8 encoding), so promoting it unchanged would turn Java implementation details into permanent cross-language protocol semantics.

The first durable Governance fingerprint implementation therefore needs an explicit, language-independent fingerprint contract. It must define field membership, ordering, canonical bytes, versioning, compatibility, and the relationship to the existing legacy hash without conflating semantic identity with controlled revision history.

The current factory model is not neutral to all ordering. Operation steps are explicitly ordered, and product list order currently participates in deterministic demand generation. The fingerprint must not erase distinctions that can still affect behavior.

This ADR defines the durable fingerprint contract and the first policy, `factory-model:v1`. It does not choose a controlled revision identifier, lineage policy, or revision repository; those remain a separate follow-up decision.

Related documents:

- [ADR-0003: Canonical factory model boundary](0003-canonical-factory-model-boundary.md)
- [ADR-0004: Model identity, revision lineage, and external change control](0004-model-identity-revision-lineage-and-external-change-control.md)
- [Governance and Conformance Architecture](../governance-conformance.md)
- [Governance and Conformance Capability Plan](../../planning/governance-conformance-capability.md)
- [Factory Design Capability Plan](../../planning/factory-design-capability.md)

## Decision

### Fingerprints are namespaced and policy-versioned

A durable `ModelFingerprint` identifies canonical semantic content under an explicit fingerprint policy.

For the first factory-model policy:

```text
namespace      = factory-model
policy version = v1
algorithm      = sha256
digest         = 64 lowercase hexadecimal characters
```

The canonical textual representation is:

```text
factory-model:v1:sha256:<digest>
```

The policy version identifies the semantic/canonicalization contract, not merely the cryptographic algorithm. Any change that can alter semantic field membership, ordering, normalization, binary encoding, or digest semantics requires a new policy version.

### `factory-model:v1` freezes current semantic field membership

The v1 fingerprint includes:

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

Names and current allocated IDs therefore participate in `factory-model:v1` identity.

This does not claim that those fields are the ideal permanent ontology. Reclassifying names as presentation-only, introducing logical identity distinct from current IDs, or otherwise changing canonical model equivalence requires a later policy version if fingerprints can change.

### Current list ordering remains semantic in v1

`resources`, `operations`, `products`, and operation `steps` are encoded in list order.

This is intentionally conservative. In particular, current product order can affect deterministic demand generation because runtime preserves product order and selects by RNG index. V1 does not define a stronger order-independent equivalence than the canonical model/runtime currently guarantees.

`eligibleResources` is set-shaped and is therefore sorted by ascending signed `MachineId` before encoding.

### The v1 canonical byte grammar is language-independent

The digest is SHA-256 over one normative canonical byte stream. The stream does not depend on Java object formatting, serializer defaults, locale, or platform byte order.

All multi-byte integers use big-endian byte order.

The stream begins with the exact ASCII bytes for:

```text
arcogine.factory-model.v1\0
```

including the terminal zero byte.

#### Primitive encodings

`U64(n)`
: exactly eight bytes containing unsigned integer `n` in big-endian order. Used for collection counts and UTF-8 byte lengths.

`I64(n)`
: exactly eight bytes containing the signed two's-complement 64-bit representation of `n` in big-endian order. All model IDs and integral numeric fields are encoded as `I64`, including fields currently represented by narrower Java types.

`TEXT(s)`
: `U64(byteLength)` followed by exactly `byteLength` UTF-8 bytes. `byteLength` counts UTF-8 bytes, not UTF-16 code units or Unicode code points. No Unicode normalization, case folding, trimming, locale transformation, or presentation cleanup is applied.

Every `TEXT` value participating in `factory-model:v1` **must be a valid Unicode scalar-value sequence before publication**. The v1 implementation must extend the canonical factory publication/validation boundary so a model containing ill-formed Unicode text cannot become a published v1-fingerprintable model. Fingerprinting must therefore be total for every successfully published model under the v1 publication contract; malformed Unicode is a publication validation error, not a fingerprint-time surprise.

In the current Java implementation this specifically means unpaired UTF-16 surrogates in resource, operation, step, or product names must be rejected before `FactoryModelVersion` publication succeeds.

`OPTIONAL_F64(x)`
: one presence byte followed, when present, by an IEEE 754 binary64 payload:

```text
00                              null
01 <8-byte binary64 payload>    present
```

The payload is big-endian and follows these rules:

- positive and negative zero remain distinct;
- finite values retain their exact binary64 value;
- positive and negative infinity retain their IEEE 754 encodings if admitted by domain validation;
- any NaN value admitted by the domain is canonicalized to `0x7ff8000000000000`.

This byte grammar is total for nullable binary64 values even if domain validation later chooses to reject NaN or infinity.

#### Collection encoding

Every list or set is encoded as:

```text
U64(elementCount)
<element 1 bytes>
...
<element N bytes>
```

List elements retain list order. `eligibleResources` is numerically sorted before collection encoding.

#### Factory-model v1 stream

After the policy-domain prefix:

```text
U64(resources.size)
for resource in resources, in list order:
    I64(resource.id.value)
    TEXT(resource.name)
    I64(resource.concurrency)
    OPTIONAL_F64(resource.capacityLiters)
    I64(resource.setupTime)

U64(operations.size)
for operation in operations, in list order:
    I64(operation.id)
    TEXT(operation.name)
    U64(operation.steps.size)
    for step in operation.steps, in list order:
        I64(step.stepId)
        TEXT(step.name)
        I64(step.duration)
        U64(step.eligibleResources.size)
        for resourceId in step.eligibleResources, sorted ascending:
            I64(resourceId.value)

U64(products.size)
for product in products, in list order:
    I64(product.id.value)
    TEXT(product.name)
    I64(product.operationId)
```

This grammar is the normative source of fingerprint bytes.

### SHA-256 equality is sufficient for operational identity

Arcogine treats equality of correctly formed `factory-model:v1` fingerprints as equality of semantic content under v1 for operational identity purposes. This relies on SHA-256 collision resistance and is not a mathematical claim of injectivity.

### Java equality is not the durable semantic-identity contract

`FactoryModel.equals()` remains representation equality. V1 requires:

```text
FactoryModel a equals FactoryModel b
    =>
fingerprint(a) equals fingerprint(b)
```

The reverse implication is not established as a permanent platform invariant. Durable semantic identity is compared through fingerprints under the same policy.

Because v1 publication validation rejects ill-formed text, the implication applies over the complete set of models that can be successfully published under the v1 contract; no published v1 model lacks a defined fingerprint.

### Existing `contentHash()` remains legacy provisional provenance

`FactoryModelVersion.contentHash()` does **not** become the digest component of `factory-model:v1`.

Its current Java-derived algorithm remains a compatibility surface for existing runtime/result provenance while consumers are deliberately migrated.

```text
contentHash()
    legacy provisional Java-derived hash
    existing raw provenance compatibility

fingerprint()
    durable namespaced semantic identity
    factory-model:v1:sha256:<digest>
    digest from the normative v1 byte grammar
```

Historical bare `modelContentHash` values must not be silently reinterpreted as v1 digests. A migration may retain both identifiers during a compatibility period.

### Released fingerprint policies are immutable

Once `factory-model:v1` ships in implementation, every supported implementation must produce the same fingerprint for the same v1 semantic content across processes, software versions, and implementation languages.

Changing any identity-affecting rule while still calling the policy v1 is forbidden, including:

- semantic field membership or field order;
- collection ordering or set sorting;
- integer widths or byte order;
- text encoding, byte-length semantics, Unicode-validity requirements, or normalization rules;
- nullable-value tags;
- floating-point canonicalization;
- policy-domain prefix;
- hash algorithm or digest rendering.

Such a change requires a new policy version.

### Golden compatibility vectors are part of the contract

The implementation must pin literal expected canonical bytes (or equivalent byte-level fixtures) and expected digest/fingerprint outputs.

At minimum, v1 tests must cover:

- one representative full factory model with an exact expected fingerprint;
- repeated/equivalent construction producing the same fingerprint;
- sensitivity to names, IDs, and ordered collection changes;
- invariance to `eligibleResources` iteration order;
- delimiter-bearing text;
- non-ASCII BMP and astral Unicode text, proving UTF-8 byte-length framing;
- publication rejection of unpaired surrogate / otherwise ill-formed Unicode text in every `TEXT`-participating name field;
- null versus present `capacityLiters`;
- positive, negative, zero, and signed-zero floating-point values;
- NaN canonicalization if NaN remains publishable;
- explicit evidence that legacy `contentHash()` behavior remains unchanged while its value is allowed to differ from the durable v1 digest.

Golden vectors supplement the normative grammar; they do not replace it.

## Alternatives considered

### Promote the existing bare SHA-256 string

Rejected because a bare digest does not identify the policy that produced it and would allow later canonicalization changes to silently redefine identity.

### Promote the current Java canonical string as v1

Rejected because it depends on Java `String.length()` and `String.valueOf()` behavior. Those are acceptable for a deliberately provisional hash, not a permanent cross-language protocol.

### Canonicalize through JSON

Rejected because it introduces serializer, property-order, escaping, numeric-rendering, and library-version dependencies without improving the semantic contract.

### Permit ill-formed Java strings and reject them only during fingerprinting

Rejected because that makes the identity function partial over the canonical models Arcogine can publish. V1 instead makes Unicode scalar validity part of the publication contract so every published v1 model has a defined fingerprint.

### Define an encoding for isolated UTF-16 surrogate code units

Rejected because it would standardize a Java representation artifact as cross-language domain content. Valid Unicode scalar values are the durable text domain instead.

### Sort all top-level model collections

Rejected because current semantics do not justify that equivalence: step ordering is explicit and product order can affect seeded runtime behavior.

### Exclude names or allocated IDs

Rejected for v1 because both are currently canonical model fields/references. Reclassifying them belongs to future canonical-model evolution and, if identity changes, a later fingerprint policy.

### Combine fingerprint and controlled-revision decisions

Rejected because ADR-0004 deliberately separates semantic content identity from historical revision identity. Controlled revision identifiers and lineage will be decided separately when that work is allocated.

## Consequences

- Arcogine gains a named, versioned, language-independent semantic identity contract.
- `factory-model:v1` preserves current field membership and ordering semantics rather than redesigning factory equivalence inside governance work.
- The durable fingerprint does not inherit Java string-length or scalar-formatting behavior.
- The factory publication boundary must reject ill-formed Unicode in all v1 `TEXT` fields before a model can be published; fingerprinting is therefore total over published v1 models.
- Existing `contentHash()` and `modelContentHash` consumers can remain compatible as legacy provenance while durable fingerprint support is introduced additively.
- Historical bare hashes are not reinterpreted as v1 fingerprints.
- Future canonical-model evolution can introduce a new policy without changing the meaning of historical v1 identities.
- Semantic identity remains independent of authorship, time, approval, deployment, workflow state, and controlled revision history.
- Controlled revision identity and lineage remain unresolved by this ADR.

The main costs are an additive migration from the legacy content hash, an explicit binary grammar, and a new publication-validation rule for malformed Unicode. Those costs are preferable to a partial identity function or a durable protocol coupled to one language runtime.

## Non-goals

This ADR does not decide:

- `ControlledRevisionId` representation;
- lineage, branching, merge, or rollback policy;
- revision persistence or repository authority;
- model artifact serialization/retention;
- schema evolution/migration policy;
- semantic `ChangeSet` representation;
- whether future names become presentation-only;
- whether future logical identity separates from current IDs;
- a universal canonicalization scheme for all Arcogine domains;
- Challenge Readiness identity/versioning;
- approval, authorization, deployment, or external workflow semantics.

A subsequent ADR should address controlled revision identity and lineage independently when that decision is allocated.

## Charter alignment

This decision supports determinism and provenance by making semantic identity reproducible across implementations and complete over the published v1 model domain. It also preserves domain ownership: the factory domain defines the canonical semantic content and publication validity of its proving-ground model, while Governance requires durable identities to be explicit, versioned, and reproducible.