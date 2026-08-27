# ADR-0006: Durable semantic fingerprint contract

Status: Accepted
Date: 2026-08-27

## Context

[ADR-0004](0004-model-identity-revision-lineage-and-external-change-control.md) separates two concepts that Arcogine must not collapse:

1. **semantic identity** — whether two canonical model states represent the same semantic content under a defined identity policy; and
2. **controlled revision identity** — which historical governed occurrence of semantic content is being referenced.

ADR-0004 deliberately left the concrete durable fingerprint policy unresolved. The factory domain now provides the first implemented proving ground: `FactoryModelVersion.contentHash()` deterministically computes a SHA-256 digest from canonical factory-model content, and that digest already participates in runtime/result provenance. Its contract is explicitly provisional: it is not a persisted, public, or cross-process compatibility guarantee.

That distinction matters. The current hash is produced by Java implementation details: values are converted with `String.valueOf`, framed using Java `String.length()` semantics, concatenated into one Java string, and only then encoded as UTF-8. Promoting that representation unchanged would make Java UTF-16 code-unit counting and Java scalar lexical rendering part of Arcogine's permanent cross-language identity protocol merely to preserve a compatibility guarantee Arcogine never made.

A durable fingerprint therefore cannot be established by renaming the existing hash. Arcogine must define what semantic content participates, how that content is canonicalized into bytes independently of implementation language, how collection ordering is interpreted, how the policy evolves, and what compatibility future implementations must preserve.

The current factory model is also not semantically neutral with respect to all list ordering. `OperationDefinition.steps()` is explicitly ordered, and the current runtime preserves product ordering into demand generation, where products are selected by deterministic RNG index. Normalizing those collections as unordered during fingerprinting could therefore assign one fingerprint to models that can produce different behavior under the same scenario seed.

This ADR defines the durable semantic fingerprint contract and the first policy, `factory-model:v1`. It intentionally does **not** choose a controlled revision identifier, lineage policy, or revision repository. Those are separate historical-governance decisions and will be addressed by a subsequent ADR when that work is allocated.

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

The policy version identifies the semantic and canonicalization contract. It is independent of the cryptographic algorithm version. Keeping SHA-256 while changing field membership, ordering semantics, normalization, binary encoding, or any other canonicalization rule requires a new fingerprint policy version if the change can alter identity.

### `factory-model:v1` freezes the current semantic field membership

The v1 fingerprint includes the semantic fields currently represented by the factory model:

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

`resources`, `operations`, and `products` are encoded in canonical list order. `OperationDefinition.steps()` is likewise encoded in order.

This is intentionally conservative. The v1 fingerprint reflects the semantics and deterministic-runtime contract of the current canonical model; it does not attempt to define a stronger order-independent equivalence than the implementation presently guarantees.

In particular, product order is behavior-affecting today because runtime assembly preserves product order and deterministic demand generation selects by list index. Treating product order as non-semantic could therefore cause two models capable of different seeded outcomes to share a fingerprint.

If a future canonical model or runtime establishes that one of these collections is semantically unordered, that change does not alter v1. A new fingerprint policy version must encode the new equivalence relation.

### Set-shaped values are canonicalized independently of iteration order

`OperationStepDefinition.eligibleResources` is set-shaped. Its `MachineId` values are sorted by ascending signed numeric ID before encoding.

Equivalent sets must therefore produce the same v1 fingerprint regardless of source collection iteration order.

### The v1 canonical byte grammar is language-independent

The fingerprint digest is SHA-256 over one normative canonical byte stream. The byte stream is defined here independently of Java object/string representation.

All multi-byte integers use network byte order (big-endian). There is no platform-native byte order and no locale-sensitive rendering anywhere in the grammar.

The stream begins with the exact ASCII policy-domain prefix, including the terminal zero byte:

```text
arcogine.factory-model.v1\0
```

The remaining stream is the concatenation of the fields below in the listed order. No field names, separators, whitespace, or serializer metadata are inserted beyond the explicit encodings defined here.

#### Primitive encodings

`U64(n)`
: exactly eight bytes containing unsigned integer `n` in big-endian order. It is used for collection counts and UTF-8 byte lengths. Values outside `0..2^64-1` are not representable.

`I64(n)`
: exactly eight bytes containing the two's-complement signed 64-bit representation of `n` in big-endian order. All model IDs and integral numeric fields are encoded as `I64`, including fields whose current Java representation is narrower than 64 bits.

`TEXT(s)`
: `U64(byteLength)` followed by exactly `byteLength` bytes of the Unicode string encoded as UTF-8. `byteLength` is the number of UTF-8 bytes, not UTF-16 code units and not Unicode code points. No Unicode normalization, case folding, trimming, locale transformation, or presentation cleanup is applied. A value must be a valid Unicode scalar-value sequence; an implementation must reject ill-formed text rather than silently replace malformed code units while fingerprinting.

`OPTIONAL_F64(x)`
: one presence byte followed, when present, by an IEEE 754 binary64 payload:

```text
00                              null
01 <8-byte binary64 payload>    present
```

The binary64 payload is the exact 64-bit IEEE 754 encoding in big-endian byte order, with these canonicalization rules:

- positive and negative zero remain distinct;
- finite values retain their exact binary64 value;
- positive and negative infinity retain their IEEE 754 encodings if admitted by the canonical model;
- every NaN representation, if admitted by the canonical model, is canonicalized to the quiet-NaN bit pattern `0x7ff8000000000000`.

This policy does not assert that NaN or infinity are valid factory-domain values. Domain validation may reject them. The byte grammar is nevertheless total for every nullable binary64 value so fingerprint compatibility does not depend on one language's numeric-to-text formatter.

#### Collection encoding

Every list or set is encoded as:

```text
U64(elementCount)
<element 1 bytes>
...
<element N bytes>
```

List elements retain list order. `eligibleResources` is sorted numerically before this collection encoding is applied.

#### Factory-model v1 stream

After the policy-domain prefix, encode:

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

This grammar, rather than any particular serializer or Java implementation, is the normative source of fingerprint bytes.

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

### Existing `contentHash()` remains legacy provisional provenance

`FactoryModelVersion.contentHash()` does **not** become the digest component of `factory-model:v1`.

Its existing algorithm remains a compatibility surface for current runtime/result provenance until consumers are deliberately migrated. It may remain unchanged so existing behavior and recorded `modelContentHash` values are not silently rewritten.

The distinction is explicit:

```text
contentHash()
    legacy provisional Java-derived content hash
    existing raw provenance compatibility

fingerprint()
    durable namespaced semantic identity
    factory-model:v1:sha256:<digest>
    digest derived from the normative v1 byte grammar
```

A consumer must not reinterpret a historical bare `modelContentHash` value as the digest of `factory-model:v1` unless that value was explicitly produced by the durable fingerprint implementation. Migration may retain both identifiers during a compatibility period.

This separation is intentional. ADR-0004 expressly declined to promise cross-process durability for the old hash, so G1 is the correct point to replace accidental Java encoding semantics with an intentional language-independent protocol.

### Released fingerprint policies are immutable

Once `factory-model:v1` is released in implementation, every supported Arcogine implementation of that policy must produce the same fingerprint for the same v1 canonical semantic content across process boundaries, software versions, and implementation languages.

Refactoring the implementation is permitted. Silently changing any of the following while continuing to label the result `factory-model:v1` is not:

- semantic field membership;
- field order;
- collection ordering rules;
- set canonicalization;
- integer width or byte order;
- string encoding or length unit;
- Unicode validity or normalization rules;
- nullable-value tagging;
- floating-point canonicalization;
- policy-domain prefix;
- hashing algorithm;
- digest rendering.

A change to any of those rules that can change identity requires a new policy version.

### Golden compatibility vectors are part of the contract

The implementation of a durable fingerprint policy must include pinned golden compatibility vectors with literal expected canonical bytes or byte encodings and literal expected digest/fingerprint outputs.

At minimum, v1 tests must cover:

- a representative canonical factory model with an exact expected digest/fingerprint;
- equality of repeated computations across independently constructed equal models;
- sensitivity to semantic field changes, including names and IDs;
- sensitivity to resource, operation, product, and operation-step ordering;
- invariance to `eligibleResources` set iteration order;
- delimiter-bearing text;
- non-ASCII BMP text and at least one astral Unicode character, proving UTF-8 byte-length framing;
- null versus present `capacityLiters`;
- representative positive/negative/zero floating-point values, including signed zero;
- NaN canonicalization if NaN remains constructible by the canonical model;
- a compatibility assertion that the legacy `contentHash()` remains unchanged for its existing fixture, while explicitly allowing its value to differ from the durable v1 digest.

Golden vectors are additional executable evidence for the grammar, not a substitute for it.

## Alternatives considered

### Promote the existing bare SHA-256 string without policy metadata

This would require the least code and preserve current consumers unchanged.

It was rejected because a bare digest does not identify the canonicalization policy that produced it. Future changes to field membership, ordering, or normalization could silently redefine identity while retaining the same apparent format.

### Promote the current Java canonical string as `factory-model:v1`

This would make the durable v1 digest byte-for-byte equal to today's `contentHash()`.

It was rejected because the current representation depends on Java-specific behavior, including UTF-16 code-unit length through `String.length()` and Java lexical rendering through `String.valueOf`. Those were acceptable for a deliberately provisional in-process hash but are poor foundations for a permanent cross-language protocol. ADR-0004 made no durability promise for the old hash, so preserving it as legacy provenance is preferable to turning its implementation accidents into architecture.

### Canonicalize through JSON

A canonical JSON representation could be human-inspectable and reuse existing serializers.

It was rejected for v1 because it would introduce additional compatibility dependencies — serializer behavior, property ordering, escaping, numeric rendering, and library-version behavior — without improving the semantic contract. A small explicit binary grammar is easier to specify exactly.

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

It was rejected because ADR-0004's principal architectural distinction is that semantic content identity and historical revision identity are different concerns. Recombining their concrete mechanisms in the next ADR would weaken that boundary. Controlled revision identifiers, lineage rules, rollback/branch semantics, and provenance minimums will be decided separately when that ADR is allocated.

## Consequences

As a result of this decision:

- Arcogine gains a named, versioned, language-independent, cross-process semantic identity contract;
- `factory-model:v1` intentionally preserves current field membership and ordering semantics rather than redesigning factory semantic equivalence inside governance work;
- the durable fingerprint no longer inherits Java `String.length()` or `String.valueOf()` behavior;
- existing `contentHash()` and `modelContentHash` consumers can remain behaviorally compatible as legacy provenance while the durable fingerprint is introduced additively;
- migration must not silently reinterpret historical bare hashes as v1 fingerprints;
- future canonical-model evolution can introduce a new fingerprint policy without rewriting the meaning of historical v1 identifiers;
- semantic identity remains independent of authorship, time, workflow state, approval, deployment, and external change-management systems;
- controlled revision identity and lineage remain unresolved by this ADR and must not be inferred from the fingerprint;
- compatibility tests become part of the architecture contract, so accidental canonicalization changes fail visibly rather than silently changing persisted identity.

The principal costs are an additive migration from the legacy content hash and a deliberately explicit binary grammar. Those costs are preferable to permanently coupling durable identity to one language runtime or retroactively declaring current distinguishable models equivalent.

## Non-goals

This ADR does not decide:

- `ControlledRevisionId` representation;
- revision lineage, branching, merge, or rollback policy;
- controlled revision persistence or repository authority;
- model artifact serialization/retention;
- schema evolution or migration policy beyond fingerprint coexistence;
- semantic `ChangeSet` representation;
- whether future factory names become presentation-only metadata;
- whether future logical identity is separated from current allocated IDs;
- a universal canonicalization scheme shared by all Arcogine domains;
- Challenge Readiness identity/versioning;
- approval, authorization, deployment, or external workflow semantics.

A subsequent ADR should address controlled revision identity and lineage independently when that decision is allocated. This ADR does not reserve its number.

## Charter alignment

This decision supports the Product Charter's determinism and provenance principles by making semantic identity reproducible and historically interpretable across processes, versions, and implementation languages. It also preserves domain ownership: the factory domain defines the canonical semantic content of its proving-ground model, while Governance supplies the architectural requirement that durable identities be explicit, versioned, and reproducible rather than accidental properties of an implementation hash.
