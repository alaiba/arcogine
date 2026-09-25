# Factory model

Status: Normative work-in-progress definition, `factory-model:wip`; implemented by `FactoryModelVersion.fingerprint()` and `FactoryModelArtifact`. Not promoted.
Evolution rule: [Semantic evolution and support](overview.md#semantic-evolution-and-support); composition rules: [Factory semantic evolution](factory-design.md#111-semantic-evolution)

## 1. Purpose and status

This document is the normative source of the current Factory model: its required production
records and optional spatial record, their publication predicates, and the canonical byte grammar
the one aggregate `ModelFingerprint` is digested from. Where this document and an implementation
disagree, this document is authoritative. The canonical model boundary it serves is defined by the
[Factory Design architecture](factory-design.md).

The definition is **work in progress**. `factory-model:wip` is a mutable development marker, not a
durable semantic identity:

- for the same current definition and the same authored content, canonical bytes and fingerprints
  are deterministic across processes and implementation languages;
- between development revisions the records, predicates, grammar, bytes and fingerprints may change
  without a new marker; such a change updates this document, its golden vectors and every dependent
  in the same change;
- a `factory-model:wip` fingerprint therefore means "equal content under the definition current when
  it was computed". Equal WIP strings or fingerprints from different development revisions establish
  no compatibility, and no current reader interprets an artifact produced under an earlier revision;
- tests, golden vectors, in-process publication, persistence in a proving store and this normative
  description do not promote the definition. Promotion is an explicit owner decision under the
  [semantic evolution rules](overview.md#semantic-evolution-and-support).

Semantic content identity is distinct from historical occurrence identity: equal fingerprints may
recur in distinct [controlled revisions](controlled-revisions.md).

## 2. Authored facts and publication predicates

A Factory model consists of required production records and one optional spatial record. Publication
rejects content for which canonicalization is undefined, so fingerprinting is total over published
models.

### 2.1 Production records

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

Names and current allocated IDs participate in identity. That does not claim they are the ideal
ontology; reclassifying names as presentation-only or introducing a logical identity distinct from
current IDs is an ordinary definition change while the model is work in progress.

### 2.2 Optional spatial record

The spatial record is either **absent** or **present and complete**. When present it carries exactly
these five authored fact groups, each required within the record:

| Fact | Meaning | Validation | Zero legal? |
|---|---|---|---|
| floor width / height | plant extent in integer cells | each `>= 1`; the maximum-transfer predicate below must hold | no |
| resource position `x` / `y` | minimum-coordinate reference cell of the resource footprint | `x >= 0`, `y >= 0`; the footprint must fit inside the floor | yes (`0,0` is valid) |
| footprint width / height | integer cells occupied by the resource from its reference cell | each `>= 1`; distinct resource footprints must not overlap | no |
| `ticksPerCell` | authored material-handling rate magnitude | integer `>= 0`; the maximum-transfer predicate must hold | yes |
| `handlingTicks` | authored fixed overhead applied once per inter-resource transfer | integer `>= 0`; the maximum-transfer predicate must hold | yes |

Floor extent, `ticksPerCell` and `handlingTicks` are plant-scope: one value each per design.
Position and footprint are per resource. A present record gives **every** configured resource exactly
one position and one footprint, in resource-list order, and gives none to an identifier that is not
a configured resource; a record that omits a resource, places one twice, places an unknown
identifier or lists layouts out of resource order is invalid.

When the record is **absent**, the design makes no spatial, layout or handling assertion: no floor,
position, footprint, `ticksPerCell` or `handlingTicks` exists for it, and none is synthesized from a
default. Absence is not a present record with zero or minimal values — a present record with position
`(0,0)`, `ticksPerCell = 0` and `handlingTicks = 0` is a different authored design. Unknown or partly
authored facts, such as geometry whose handling magnitudes are unknown, are draft or adapter state:
they are not publishable and are never encoded as zero. The record is deliberately coarse; it has no
geometry-only or handling-only form.

For a resource at `(x,y)` with footprint width `w` and height `h`, the footprint occupies exactly the
integer cells `x..x+w-1` by `y..y+h-1`. It fits inside a floor of width `W` and height `H` iff
`x >= 0`, `y >= 0`, `w >= 1`, `h >= 1`, `x + w <= W` and `y + h <= H`, evaluated with overflow-safe
arithmetic. Two resources overlap iff their occupied-cell sets intersect.

Publication of a present record also proves that the spatial transfer-duration magnitude is
representable for the farthest possible pair of reference cells:

```text
maxManhattanDistance = (W - 1) + (H - 1)
maxTransferDuration  = handlingTicks + ticksPerCell * maxManhattanDistance
```

Every subtraction, addition and multiplication in that predicate, and the result, must be
representable in the runtime tick-duration type. This bounds the derived duration itself; it does not
claim that adding a valid duration to an arbitrarily extreme current `SimTime` can never overflow —
the runtime time-addition guard remains responsible for that.

The spatial record's presence and every present fact participate in the fingerprint. They are
authored model facts, not Engine policy: floor extent, placement, footprint, handling rate and fixed
overhead describe the production system the designer authored, while the distance metric, rounding,
zero-distance behavior, destination binding, reservation and transfer lifecycle belong to
[Engine semantics](engine-semantics.md). Footprint is canonical even though no current Engine rule
reads it in transfer distance: it is required for layout validation and future spatial capability.

Orientation, paths, graph edges, aisles, conveyors, transport resources, obstacles, congestion, floor
identity, connection points, authoritative animation coordinates and route topology are not part of
the record. Adding them is a definition change justified by a concrete capability that makes them
authored, behaviorally relevant semantics.

### 2.3 Publication validity is not Engine executability

This document decides only whether content is a valid published Factory model. Whether the Engine
executes it is decided by the Engine interpretation's own supported content; the current
interpretation executes production records only and refuses a present spatial record before any
runtime state exists ([transfer applicability](transfer-applicability.md)).

## 3. Fingerprint rendering

```text
namespace      = factory-model
policy         = wip
algorithm      = sha256
digest         = 64 lowercase hexadecimal characters

factory-model:wip:sha256:<digest>
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

The stream begins with the exact ASCII bytes of `arcogine.factory-model.wip` followed by a zero byte —
27 bytes:

```text
61 72 63 6f 67 69 6e 65 2e 66 61 63 74 6f 72 79
2d 6d 6f 64 65 6c 2e 77 69 70 00
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
fields are encoded as `I64`, including fields currently held in narrower types and every spatial
field.

`TEXT(s)`
: `U64(byteLength)` followed by exactly `byteLength` UTF-8 bytes. No Unicode normalization, case
folding, trimming or presentation cleanup is applied.

`OPTIONAL_F64(x)`
: one presence byte — `00` null, `01` present — followed, when present, by the big-endian IEEE 754
binary64 payload. Positive and negative zero stay distinct, finite values keep their exact value,
infinities keep their encodings if admitted, and any admitted NaN is canonicalized to
`0x7ff8000000000000`.

`PRESENCE`
: one byte selecting whether the spatial record is present: `00` absent, `01` present. No other
value is valid.

Every list or set is `U64(elementCount)` followed by its elements.

### 4.3 Stream

After the prefix:

```text
PRESENCE(spatial)

if spatial is present:
    I64(spatial.floor.width)
    I64(spatial.floor.height)
    I64(spatial.ticksPerCell)
    I64(spatial.handlingTicks)

U64(resources.size)
for resource in resources, in list order:
    I64(resource.id)
    TEXT(resource.name)
    I64(resource.concurrency)
    OPTIONAL_F64(resource.capacityLiters)
    I64(resource.setupTime)
    if spatial is present:
        I64(spatial.position(resource).x)
        I64(spatial.position(resource).y)
        I64(spatial.footprint(resource).width)
        I64(spatial.footprint(resource).height)

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

### 4.4 Layout rationale

- **Marker first.** A decoder knows the complete shape of every following record before decoding any
  of it, so each artifact has exactly one parse, and an absent record occupies one byte.
- **Header before resources.** The maximum-transfer predicate depends only on header fields, so a
  decoder can reject unrepresentable magnitudes, and establish the floor every footprint is checked
  against, before it allocates a single resource record. The header is fixed-arity and has no count.
- **Per-resource suffix.** Encoding each resource's spatial fields inside that resource's record makes
  a present record's coverage structural: the grammar cannot express a resource without spatial fields,
  spatial fields without a resource, or two layouts for one resource.
- **Anchor plus extent.** Placement is encoded as the authored reference cell and extent, never as a
  derived occupied-cell set, bitmap, bounding box or region. For every publishable footprint this is
  bijective with the occupied cells. Positions are encoded exactly as authored: there is no translation
  invariance, origin normalization or coordinate compaction.
- **Totality.** Every spatial field is an `I64` and the grammar is total over the signed 64-bit range.
  The range, containment, overlap and maximum-transfer predicates are publication validation, not
  encoding constraints, which lets a decoder reject a violating artifact explicitly (§5.3) instead of
  failing to parse it ambiguously.

### 4.5 Collection ordering

`resources`, `operations`, `products` and operation `steps` are encoded in **list order**, so
reordering them changes identity. `eligibleResources` is set-shaped and sorted by ascending signed
`MachineId` value. Per-resource spatial fields follow resource list order; no spatial ordering of
resources (row-major, distance-from-origin or similar) is applied, because it would discard authored
list order and collapse two authored models into one identity.

## 5. Canonicality and decoding

### 5.1 Grammar rejection

A decoder rejects, explicitly and without partial acceptance:

- an absent, truncated or non-matching definition prefix, including the prefixes of discarded
  definitions;
- a missing presence marker or a marker other than `00` or `01`;
- a stream truncated at any field boundary, including inside the spatial header or a resource's
  spatial fields;
- a collection count that is negative or exceeds the supported element range;
- a `TEXT` length that overruns the stream, or `TEXT` bytes that are not well-formed UTF-8;
- an `OPTIONAL_F64` presence byte other than `00` or `01`;
- any trailing byte after the final product record.

### 5.2 Canonicality round-trip

Decoding is defined only for artifacts that are already canonical. A decoder re-encodes the decoded
model and requires byte equality with its input; a byte string that parses but does not re-encode to
itself is rejected. This closes the gap where two byte strings could resolve to one model.

### 5.3 Publication-predicate rejection

A decoder also applies the publication predicates of §2 — the production predicates always, and the
spatial predicates whenever the record is present. A byte string that satisfies the grammar but
violates a predicate is not a valid artifact and is rejected: resolving stored content must never
yield a model that could not have been published.

### 5.4 Fingerprint derivation from stored artifacts

Deriving a fingerprint from stored bytes proceeds by decode, re-encode and digest under §5.1–§5.3,
never by digesting untrusted bytes directly.

### 5.5 Other definitions are refused, not reinterpreted

Only the current definition is understood. A fingerprint under any other namespace or policy —
including the discarded ordinal `factory-model` policies — is unsupported, and bytes laid out under
another definition fail at the prefix. Because the marker does not change between development
revisions, bytes written under an earlier development revision are verified only against the current
definition; such material is disposable proving evidence and is reset rather than migrated
([controlled revisions](controlled-revisions.md#the-revision-record-does-not-choose-model-artifact-persistence)).

## 6. Required test coverage

Golden vectors pin literal canonical bytes and fingerprints for the current definition and supplement,
never replace, this grammar. Vectors are derived from an independent implementation of §4 and are
regenerated deliberately when the definition changes; byte compatibility with earlier development
revisions is not a requirement. Tests must cover at least:

**Grammar and identity**

1. one representative model with the spatial record absent and one with it present, each with exact
   expected bytes and fingerprint;
2. the 27-byte definition prefix;
3. repeated or equivalent construction producing the same fingerprint;
4. each representative artifact decoding to its model and re-encoding to identical bytes;
5. rejection of trailing bytes, a truncated field, a bad `OPTIONAL_F64` marker, invalid UTF-8, an
   out-of-range count or concurrency, and a decodable-but-non-canonical encoding;
6. refusal of discarded-definition fingerprints and byte layouts.

**Spatial-record presence**

7. an absent record and a present record with legal zero or minimal values — position `(0,0)`, a
   1×1 floor and footprint, `ticksPerCell = 0`, `handlingTicks = 0` — producing different pinned
   fingerprints;
8. presence-marker framing: an invalid marker, truncation inside the header or a resource's spatial
   fields, and header bytes appended to a spatial-absent artifact, each rejected;
9. coverage: publication, and canonical encoding, reject a present record that omits a resource,
   repeats one, places an unknown identifier, places none while resources exist, or lists layouts out
   of resource order.

**Spatial field sensitivity**

10. each position coordinate, each footprint extent, floor width and height, `ticksPerCell` and
    `handlingTicks` independently changing the fingerprint;
11. swaps of distinct `x`/`y`, width/height, floor width/height and `ticksPerCell`/`handlingTicks`
    values changing it, proving field order;
12. a whole-design translation producing a different fingerprint.

**Production-record coverage**

13. delimiter-bearing, non-ASCII BMP and astral text, proving UTF-8 byte-length framing;
14. publication rejection of ill-formed Unicode in every name field;
15. invariance to `eligibleResources` iteration order and sensitivity to `resources`, `operations`,
    `products` and `steps` order;
16. null versus present `capacityLiters`; positive, negative, zero and signed-zero binary64 values;
    NaN canonicalization while NaN remains publishable.

**Publication-predicate rejection**

17. grammar-valid content violating a predicate — overlapping footprints, an out-of-floor footprint,
    a zero floor or footprint extent, a negative position or handling magnitude, a violated
    maximum-transfer predicate including an unrepresentable intermediate multiplied by zero, and a
    production predicate — rejected on publication and on decode;
18. accepted boundaries beside those rejections: edge- and corner-adjacent footprints, a footprint on
    the maximum legal floor edge, a 1×1 floor, and a maximum transfer duration exactly equal to the
    largest representable value.

## 7. Non-goals

This document does not define result-affecting interpretation of the spatial facts ([Engine
semantics](engine-semantics.md)), controlled-revision identity or authority ([controlled
revisions](controlled-revisions.md)), external interchange formats ([external
representations](external-representations.md)), a universal canonicalization scheme for non-Factory
domains, or the promoted durable identity a future promotion would introduce.
