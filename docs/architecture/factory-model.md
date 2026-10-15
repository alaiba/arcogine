# Factory model

Status: Normative current development definition; implemented by `FactoryModelVersion.fingerprint()` and `FactoryModelArtifact`. No stability/support promotion has been declared.
Evolution rule: [Semantic evolution and support](overview.md#semantic-evolution-and-support); composition rules: [Factory semantic evolution](factory-design.md#111-semantic-evolution)

## 1. Purpose and status

This document is the normative source of the current Factory model: its production records, their
publication predicates, and the canonical byte grammar
the one aggregate `ModelFingerprint` is digested from. Where this document and an implementation
disagree, this document is authoritative. The canonical model boundary it serves is defined by the
[Factory Design architecture](factory-design.md).

The definition is under active development. That status is human-facing repository state, not part
of the canonical bytes, fingerprint, decoder dispatch, or provenance:

- for the same current definition and the same authored content, canonical bytes and fingerprints
  are deterministic across processes and implementation languages;
- between development revisions the records, predicates, grammar, bytes and fingerprints may change;
  such a change updates this document, its golden vectors and every dependent in the same change;
- a `ModelFingerprint` identifies canonical Factory content in the producing definition context. It
  does not identify the exact definition revision that produced it, establish compatibility across
  development revisions, or select a support/maturity state;
- the fingerprint alone does not establish which development revision produced bytes. A raw decoder
  can establish only that bytes satisfy the current grammar, predicates, and canonical round-trip;
  any use that needs historical definition provenance must obtain that basis separately;
- tests, golden vectors, in-process publication, physical persistence and this normative
  description do not establish a stability/support commitment. Such a commitment is an explicit
  owner decision under the [semantic evolution rules](overview.md#semantic-evolution-and-support).

Semantic content identity is distinct from historical occurrence identity: equal fingerprints may
recur in distinct [controlled revisions](controlled-revisions.md).

## 2. Authored facts and publication predicates

A Factory model consists of production records. Publication rejects content for which
canonicalization is undefined, so fingerprinting is total over published models.

```text
FactoryModel
    resources, in list order
        ConfiguredResource.id
        ConfiguredResource.name
        ConfiguredResource.concurrency
        ConfiguredResource.capacityLiters      nullable binary64
        ConfiguredResource.setupTime

    operations, in list order
        OperationDefinition.id
        OperationDefinition.name
        steps, in list order
            OperationStepDefinition.stepId
            OperationStepDefinition.name
            OperationStepDefinition.duration
            OperationStepDefinition.eligibleResources   set-shaped

    products, in list order
        ProductDefinition.id
        ProductDefinition.name
        ProductDefinition.operationId
```

Publication predicates: unique resource, operation and product identifiers; resource
concurrency `> 0`; non-empty operation steps; step duration `> 0`; a non-empty eligible-resource set
referencing only configured resources; every product referencing an existing operation; and every
name a well-formed Unicode scalar-value sequence (in Java, an unpaired UTF-16 surrogate is rejected
before publication succeeds).

Names and current allocated IDs participate in canonical content. That does not claim they are the
ideal ontology; reclassifying names as presentation-only or introducing a logical identity distinct
from current IDs is an ordinary change to the current development definition.

`capacityLiters` and `setupTime` are likewise authored canonical resource facts under the current
development definition. They participate in canonical bytes and the model fingerprint, survive
runtime assembly as configured machine facts, and are exposed by current resource observations.
The current Engine does **not** use either field to determine operation eligibility, resource
selection, queueing, step duration, or processing completion; processing duration remains the
authored `OperationStepDefinition.duration`. Consequently, changing either resource field can change
Factory content identity and observed configuration without changing the current Engine execution
outcome for an otherwise identical model and workload. This describes current behavior only: it does
not claim that either field is the final resource ontology or that executable setup, volumetric
qualification, or resource-dependent performance semantics already exist.

## 3. Fingerprint rendering

```text
namespace      = factory-model
algorithm      = sha256
digest         = 64 lowercase hexadecimal characters

factory-model:sha256:<digest>
```

The digest is SHA-256 over the complete canonical byte stream of §4, **including the definition
prefix**. Nothing outside that stream participates. An uppercase or mixed-case digest is not a valid
fingerprint and is rejected rather than case-folded. Equality of correctly formed fingerprints under
the current definition is treated as equality of canonical content for operational identity purposes,
on SHA-256 collision resistance; this is not a claim of injectivity.

`FactoryModel.equals()` remains representation equality, and equal models always have equal
fingerprints. The reverse implication is not established as a platform invariant; semantic content
is compared through fingerprints under one definition.

## 4. Canonical byte grammar

The grammar is language-independent: it does not depend on object formatting, serializer defaults,
locale or platform byte order. Promoting an ordinary serializer representation to identity is never
acceptable ([external representations](external-representations.md)).

### 4.1 Definition prefix

The stream begins with the exact ASCII bytes of `arcogine.factory-model` followed by a zero byte —
23 bytes:

```text
61 72 63 6f 67 69 6e 65 2e 66 61 63 74 6f 72 79
2d 6d 6f 64 65 6c 00
```

The prefix is domain separation, not a negotiation field. A decoder reads it to decide whether the
current grammar applies and rejects anything else; it never attempts a best-effort decode of another
prefix.

### 4.2 Primitive encodings

All multi-byte integers are big-endian.

`U64(n)`
: eight bytes containing unsigned `n`. Used for collection counts and UTF-8 byte lengths.

`I64(n)`
: eight bytes containing the signed two's-complement 64-bit `n`. All model IDs and integral numeric
fields are encoded as `I64`, including fields currently held in narrower types.

`TEXT(s)`
: `U64(byteLength)` followed by exactly `byteLength` UTF-8 bytes. No Unicode normalization, case
folding, trimming or presentation cleanup is applied.

`OPTIONAL_F64(x)`
: one presence byte — `00` null, `01` present — followed, when present, by the big-endian IEEE 754
binary64 payload. Positive and negative zero stay distinct, finite values keep their exact value,
infinities keep their encodings if admitted, and any admitted NaN is canonicalized to
`0x7ff8000000000000`.

Every list or set is `U64(elementCount)` followed by its elements.

### 4.3 Stream

After the prefix:

```text
U64(resources.size)
for resource in resources, in list order:
    I64(resource.id)
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
            I64(resourceId)

U64(products.size)
for product in products, in list order:
    I64(product.id)
    TEXT(product.name)
    I64(product.operationId)
```

### 4.4 Collection ordering

`resources`, `operations`, `products` and operation `steps` are encoded in **list order**, so
reordering them changes identity. `eligibleResources` is set-shaped and sorted by ascending signed
`MachineId` value.

## 5. Canonicality and decoding

### 5.1 Grammar rejection

A decoder rejects, explicitly and without partial acceptance:

- an absent, truncated or non-matching definition prefix, including the prefixes of discarded
  definitions;
- a stream truncated at any field boundary;
- a collection count that is negative or exceeds the supported element range;
- a `TEXT` length that overruns the stream, or `TEXT` bytes that are not well-formed UTF-8;
- an `OPTIONAL_F64` presence byte other than `00` or `01`;
- any trailing byte after the final product record.

### 5.2 Canonicality round-trip

Decoding is defined only for artifacts that are already canonical. A decoder re-encodes the decoded
model and requires byte equality with its input; a byte string that parses but does not re-encode to
itself is rejected. This closes the gap where two byte strings could resolve to one model.

### 5.3 Publication-predicate rejection

A decoder also applies the publication predicates of §2. A byte string that satisfies the grammar but
violates a predicate is not a valid artifact and is rejected: resolving stored content must never
yield a model that could not have been published.

### 5.4 Fingerprint derivation from stored artifacts

Deriving a fingerprint from stored bytes proceeds by decode, re-encode and digest under §5.1–§5.3,
never by digesting untrusted bytes directly.

### 5.5 Other definitions are refused, not reinterpreted

Only the current definition is understood. A fingerprint with another namespace or algorithm is
unsupported, and bytes carrying the former ordinal `factory-model:v1`/`v2` prefixes fail at the
canonical prefix boundary. The current unversioned prefix is a domain separator, not an exact
definition reference: an older development artifact whose bytes also satisfy today's grammar,
predicates, and canonical round-trip is indistinguishable to the raw decoder from bytes authored
under today's definition. The decoder therefore establishes current canonical validity, not
historical provenance.

Persisted development material that requires cross-revision protection is bound separately to the
exact build of the definition that wrote it: the Factory artifact verifier's definition binding is a
digest of the compiled classes that define the records, validation, canonical form and the
verification path that decides whether an artifact is supported, decodes, is valid and verifies. Built-in
[Storage](storage.md) refuses to reopen under a different binding without reinterpreting or deleting
the accepted material. This does not supply a compatible historical reader or promote the Factory
definition ([controlled revisions](controlled-revisions.md#the-revision-record-does-not-choose-model-artifact-persistence)).

## 6. Required test coverage

Golden vectors pin literal canonical bytes and fingerprints for the current definition and supplement,
never replace, this grammar. Vectors are derived from an independent implementation of §4 and are
regenerated deliberately when the definition changes; byte compatibility with earlier development
revisions is not a requirement. Tests must cover at least:

**Grammar and identity**

1. one representative model with exact expected bytes and fingerprint;
2. the 23-byte definition prefix;
3. repeated or equivalent construction producing the same fingerprint;
4. each representative artifact decoding to its model and re-encoding to identical bytes;
5. rejection of trailing bytes, a truncated field, a bad `OPTIONAL_F64` marker, invalid UTF-8, an
   out-of-range count or concurrency, and a decodable-but-non-canonical encoding;
6. refusal of discarded-definition fingerprints and byte layouts.

**Production-record coverage**

7. delimiter-bearing, non-ASCII BMP and astral text, proving UTF-8 byte-length framing;
8. publication rejection of ill-formed Unicode in every name field;
9. invariance to `eligibleResources` iteration order and sensitivity to `resources`, `operations`,
   `products` and `steps` order;
10. null versus present `capacityLiters`; positive, negative, zero and signed-zero binary64 values;
    NaN canonicalization while NaN remains publishable.

**Publication-predicate rejection**

11. grammar-valid content violating a predicate — a zero concurrency and a product referencing a
    missing operation — rejected on publication and on decode.

## 7. Non-goals

This document does not define result-affecting interpretation of the model ([Engine
semantics](engine-semantics.md)), controlled-revision identity or authority ([controlled
revisions](controlled-revisions.md)), external interchange formats ([external
representations](external-representations.md)), a universal canonicalization scheme for non-Factory
domains, or any stable-reference or support declaration a future promotion may require.
