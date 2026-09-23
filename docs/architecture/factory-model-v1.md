# Factory Model v1 Canonicalization

Status: Normative canonicalization contract; implemented by `FactoryModelVersion.fingerprint()`
Fingerprint policy: `factory-model:v1`
Evolution rule: [Semantic evolution and support](overview.md#semantic-evolution-and-support) and the [Factory semantic-evolution contract](factory-design.md#111-semantic-evolution)
Successor policy: [Factory Model v2 Canonicalization](factory-model-v2.md)

## Purpose

This document is the normative source of `factory-model:v1` fingerprint bytes. A fingerprint policy
version identifies a semantic/canonicalization contract — field membership, ordering, normalization,
binary encoding and digest rendering — not merely a hash algorithm. The digest is independent of Java
object formatting, serializer defaults, locale, and platform byte order, so promoting an ordinary
serializer representation to identity is never acceptable.

Semantic content identity is distinct from historical occurrence identity: equal fingerprints may
recur in distinct [controlled revisions](controlled-revisions.md). The canonical model whose
content is fingerprinted is defined by the [Factory Design architecture](factory-design.md).

## Fingerprints are namespaced and policy-versioned

A durable `ModelFingerprint` identifies canonical semantic content under an explicit fingerprint policy. For `factory-model:v1`:

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

## Semantic field membership

The v1 fingerprint includes:

```text
FactoryModel
    resources, in list order
        ConfiguredResource.id
        ConfiguredResource.name
        ConfiguredResource.concurrency
        ConfiguredResource.capacityLiters
        ConfiguredResource.setupTime

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

## List ordering is semantic

`resources`, `operations`, `products`, and operation `steps` are encoded in list order.

This is intentionally conservative: these collections are encoded in sequence, so reordering them changes the canonical bytes and therefore the model identity. V1 does not define a stronger order-independent equivalence.

`eligibleResources` is set-shaped and is therefore sorted by ascending signed `MachineId` before encoding.

## The v1 canonical byte grammar is language-independent

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

Every `TEXT` value participating in `factory-model:v1` **must be a valid Unicode scalar-value sequence before publication**. The canonical factory publication/validation boundary rejects a model containing ill-formed Unicode text, so it can never become a published v1-fingerprintable model. Fingerprinting is therefore total for every successfully published model; malformed Unicode is a publication validation error, not a fingerprint-time surprise. In the Java implementation this means unpaired UTF-16 surrogates in resource, operation, step, or product names are rejected before `FactoryModelVersion` publication succeeds.

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

## SHA-256 equality is sufficient for operational identity

Arcogine treats equality of correctly formed `factory-model:v1` fingerprints as equality of semantic content under v1 for operational identity purposes. This relies on SHA-256 collision resistance and is not a mathematical claim of injectivity.

## Java equality is not the durable semantic-identity contract

`FactoryModel.equals()` remains representation equality. V1 requires:

```text
FactoryModel a equals FactoryModel b
    =>
fingerprint(a) equals fingerprint(b)
```

The reverse implication is not established as a permanent platform invariant. Durable semantic identity is compared through fingerprints under the same policy.

Because v1 publication validation rejects ill-formed text, the implication applies over the complete set of models that can be successfully published under the v1 contract; no published v1 model lacks a defined fingerprint.

`FactoryModelVersion.fingerprint()` is the durable namespaced semantic identity:
`factory-model:v1:sha256:<digest>`, computed from the normative v1 byte grammar. No untyped
implementation hash is part of the Factory model identity contract.

## The policy is immutable

`factory-model:v1` has attributed records: published fingerprints, controlled revisions and stored canonical artifacts reference it. Under the [semantic evolution rules](overview.md#semantic-evolution-and-support) its definition is therefore fixed as a whole, and every implementation claiming the policy must produce the same fingerprint for the same v1 semantic content across processes, software versions, and implementation languages.

Changing any identity-affecting rule while still calling the policy v1 is forbidden, including:

- semantic field membership or field order;
- collection ordering or set sorting;
- integer widths or byte order;
- text encoding, byte-length semantics, Unicode-validity requirements, or normalization rules;
- nullable-value tags;
- floating-point canonicalization;
- policy-domain prefix;
- hash algorithm or digest rendering.

Such a change requires a new policy version. Continuing decoding, execution, migration and interoperability support for v1 artifacts is scoped separately by the [Factory semantic-evolution contract](factory-design.md#111-semantic-evolution); this specification fixes the definition, not a support horizon.

## Golden compatibility vectors are part of the contract

The implementation pins literal expected canonical bytes (or equivalent byte-level fixtures) and expected digest/fingerprint outputs. At minimum, v1 tests cover:

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
- typed `ModelFingerprint` rendering under the durable v1 identity contract.

Golden vectors supplement the normative grammar; they do not replace it.
