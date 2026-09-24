# Factory Model v2 Canonicalization

Status: Normative canonicalization contract; not released. Proving shape/validation implemented, canonical publication not implemented
Fingerprint policy: `factory-model:v2`
Predecessor policy: [Factory Model v1 Canonicalization](factory-model-v1.md)
Evolution rule: [Semantic evolution and support](overview.md#semantic-evolution-and-support) and the [Factory semantic-evolution contract](factory-design.md#111-semantic-evolution)

## 1. Purpose

A Factory fingerprint-policy version **is** a canonicalization contract: the policy version
identifies one closed semantic/canonicalization grammar, not merely the cryptographic algorithm.
Any change that can alter which records the policy admits, semantic field membership, ordering,
normalization, binary encoding, validation or rejection behavior, or digest semantics requires a
new policy version once the policy has attributed records (§10).

This document is the normative source of `factory-model:v2`: its required production records and
its optional spatial record (§1.1), their validation predicates, and the durable byte grammar those
facts are digested through, in exactly the sense that [Factory Model v1](factory-model-v1.md) is the
normative source of `factory-model:v1` bytes. Where this document and any implementation disagree,
this document is authoritative. Its implemented shape/validation slice does not by itself establish
a published fingerprint/artifact path or continuing support; those follow the owning
[support declarations](../development/semantic-contract-support.md).

It deliberately does **not** restate or modify:

- cross-policy compatibility, lift and comparison rules, or which policies remain publishable —
  those are the [Factory semantic-evolution contract](factory-design.md#111-semantic-evolution) and
  the support it declares;
- result-affecting Engine interpretation of the spatial facts, or which Engine interpretation
  applies to artifacts of this policy — those belong to Engine semantics (§1.2);
- controlled-revision identity or lineage — that is the
  [controlled revision contract](controlled-revisions.md).

### 1.1 Authored facts and publication predicates

A `factory-model:v2` design consists of required production records and one optional spatial
record.

**Required production records.** Configured resources, operations with their ordered steps, and
products, with exactly the field membership, field meanings, ordering distinctions and publication
predicates that [Factory Model v1](factory-model-v1.md) and the Factory publication boundary define
for them: unique resource, operation and product identifiers; resource concurrency `> 0`; non-empty
operation steps; step duration `> 0`; a non-empty eligible-resource set referencing only configured
resources; every product referencing an existing operation; and well-formed Unicode in every name.
V2 adds no production field and removes, reorders, retypes or renormalizes none.

**Optional spatial record.** The spatial record is either **absent** or **present and complete**.
When present it carries exactly these five authored fact groups, every one of them required within
the record:

| Fact | Meaning | Validation | Zero legal? |
|---|---|---|---|
| floor width / height | plant extent in integer cells | each `>= 1`; the exact maximum-transfer predicate below must hold | no |
| resource position `x` / `y` | minimum-coordinate reference cell of the resource footprint | `x >= 0`, `y >= 0`; footprint occupies the exact cells defined below and must fit inside the floor | yes (`0,0` is valid) |
| footprint width / height | integer cells occupied by the resource from its reference cell | each `>= 1`; distinct resource footprints must not overlap | no |
| `ticksPerCell` | authored material-handling rate magnitude | integer `>= 0`; the exact maximum-transfer predicate below must hold | yes |
| `handlingTicks` | authored fixed overhead applied once per inter-resource transfer | integer `>= 0`; the exact maximum-transfer predicate below must hold | yes |

Floor extent, `ticksPerCell` and `handlingTicks` are plant-scope: one value each per design.
Position and footprint are per resource. A present record gives **every** configured resource
exactly one position and one footprint and gives none to an identifier that is not a configured
resource of the design; a record that omits a resource, places one twice, or places an unknown
identifier is not a valid record.

When the record is **absent**, the design makes no spatial, layout or handling assertion: no floor,
position, footprint, `ticksPerCell` or `handlingTicks` exists for it, and none is synthesized from a
default. Absence is not a present record with zero or minimal values — a present record with
position `(0,0)`, `ticksPerCell = 0` and `handlingTicks = 0` is a different authored design. Facts
that are unknown or only partly authored, such as geometry whose handling magnitudes are unknown,
are draft or adapter state: they are not publishable under this policy and are never encoded as
zero. The record is deliberately coarse; it has no geometry-only or handling-only form.

The position anchor is part of V2 model semantics: for a resource at `(x,y)` with footprint width
`w` and height `h`, the footprint occupies exactly the integer cells `x..x+w-1` by `y..y+h-1`. A
footprint fits inside a floor of width `W` and height `H` iff `x >= 0`, `y >= 0`, `w >= 1`,
`h >= 1`, `x + w <= W`, and `y + h <= H`, evaluated with overflow-safe arithmetic. Two resources
overlap iff those occupied-cell sets intersect.

V2 publication of a present spatial record must also prove the spatial transfer-duration magnitude
is representable for the farthest possible pair of reference cells in the authored floor. With
floor dimensions `W` and `H`:

```text
maxManhattanDistance = (W - 1) + (H - 1)
maxTransferDuration  = handlingTicks + ticksPerCell * maxManhattanDistance
```

Publication accepts the artifact only when every subtraction, addition, and multiplication in that
predicate is representable in the runtime tick-duration type and `maxTransferDuration` is
representable there. This bounds the derived transfer duration itself; it does **not** claim that
adding an otherwise valid duration to an arbitrarily extreme current `SimTime` can never overflow —
the runtime time-addition validation remains responsible for that condition.

The spatial record's presence and every present fact participate in `ModelFingerprint`: adding,
removing or changing any of them changes the authored Factory design. They are model facts, not
Engine policy: floor extent, placement, footprint, material-handling rate and fixed handling
overhead describe the production system the designer authored, while Arcogine's choice of distance
metric, rounding, zero-distance behavior, destination binding, reservation and transfer lifecycle
belongs to Engine semantics. Footprint remains canonical even though
[Engine Semantics v1](engine-semantics-v1.md) does not use it in transfer distance: it is required
for publication/layout validation and future spatial capability, and a later Engine version may
interpret the same V2 footprint differently without re-fingerprinting any V2 design.

Orientation is not part of V2. Neither are paths, graph edges, aisles, conveyors, transport
resources, obstacles, congestion, floor identity, connection points, authoritative animation
coordinates, or route topology. They require a later model policy only when a concrete capability
makes them authored behaviorally relevant semantics.

### 1.2 Publication validity is not Engine applicability

This specification decides only whether an artifact is a valid `factory-model:v2` artifact. A valid
artifact is executable under an Engine interpretation only when that interpretation's own
definition supports this policy and the presence or absence of the spatial record, values and
interactions the artifact represents
([Factory semantic-evolution contract](factory-design.md#111-semantic-evolution)).

[Engine Semantics v1](engine-semantics-v1.md) was specified against the same five spatial facts,
whose meanings, anchor and maximum-transfer predicate §1.1 keeps unchanged. `engine-semantics:v1`
is not applicable to artifacts of this policy, with the spatial record present or absent;
[Engine Semantics v2](engine-semantics-v2.md) is the distinguishable identity that applies to both
forms of this policy, reusing `engine-semantics:v1`'s non-spatial and spatial rules by explicit
reference where their meanings are preserved. This reconciles the previously open
[Engine applicability question](../research/investigations/engine-evolution.md#concluded--engine-applicability-to-optional-record-factory-policies);
execution of `engine-semantics:v2` and release of this policy's canonical identity remain separately
gated by their own implementation prerequisites.

## 2. Relationship to `factory-model:v1`

V2 is a separate policy, not a revision of V1. V1's grammar, digests, golden vectors, and
historical fingerprints are permanently unchanged by V2's existence.

Structurally, the V2 stream is the V1 stream with exactly three differences:

1. a different policy-domain prefix (§4);
2. a spatial-record presence marker immediately after the prefix (§6.1);
3. when, and only when, that marker says the record is present, a plant-scope spatial header after
   the marker (§6.1) and four additional `I64` fields appended to each resource record (§6.2).

A V2 artifact whose spatial record is absent therefore carries V1's production stream unchanged
after its prefix and marker. Operation, step, and product records are shape-identical to V1's in
both cases. No V1 field is removed, reordered, retyped, or renormalized.

Because the policy-domain prefixes differ in their final component, **V1 bytes are never a prefix of
V2 bytes, no V1 artifact decodes under a V2 verifier, and no V2 artifact — with or without its
spatial record — decodes under a V1 verifier.** Cross-policy artifact confusion is structurally
impossible rather than a runtime check. This is what makes the Factory evolution contract's
policy-aware historical resolution and its prohibition on automatic lift mechanically enforceable at
the artifact boundary.

A V2 design is never published under `factory-model:v1` by dropping its spatial record, and no
public projection from V2 content to V1 content exists. Whether new production-only designs
continue to be published under `factory-model:v1` once this policy is released is a support
decision owned by the Factory semantic-evolution contract, not part of this grammar.

## 3. Fingerprint identity and digest rendering

```text
namespace      = factory-model
policy version = v2
algorithm      = sha256
digest         = 64 lowercase hexadecimal characters
```

The canonical textual representation is:

```text
factory-model:v2:sha256:<digest>
```

The digest is SHA-256 over the complete canonical byte stream defined in §6, **including the
policy-domain prefix**. Nothing outside that stream — no length header, no envelope, no framing, no
trailing padding — participates in the digest.

Digest hex is rendered in lowercase. An uppercase or mixed-case rendering is not a valid
`factory-model:v2` fingerprint and must be rejected rather than case-folded.

Arcogine treats equality of correctly formed `factory-model:v2` fingerprints as equality of V2
semantic content for operational identity purposes, on the same SHA-256 collision-resistance basis
as V1. This is not a mathematical claim of injectivity.

## 4. Policy-domain prefix

The stream begins with the exact ASCII bytes for:

```text
arcogine.factory-model.v2\0
```

including the terminal zero byte — 26 bytes total:

```text
61 72 63 6f 67 69 6e 65 2e 66 61 63 74 6f 72 79
2d 6d 6f 64 65 6c 2e 76 32 00
```

The prefix is domain separation, not a version negotiation field. A verifier reads it to decide
which policy's grammar applies and rejects anything else; it never attempts a best-effort decode of
an unrecognized prefix.

## 5. Primitive encodings

V2 introduces **no new numeric or text primitive**. It reuses V1's primitives unchanged, restated
here normatively so this document is self-contained, and adds one structural marker.

All multi-byte integers use big-endian byte order. No platform byte order, locale, serializer
default, or language-runtime formatting behavior participates in the stream.

`U64(n)`
: exactly eight bytes containing unsigned integer `n` in big-endian order. Used for collection
counts and UTF-8 byte lengths.

`I64(n)`
: exactly eight bytes containing the signed two's-complement 64-bit representation of `n` in
big-endian order. All model IDs and integral numeric fields are encoded as `I64`, including fields
currently represented by narrower types in any given implementation, and including every V2 spatial
field.

`TEXT(s)`
: `U64(byteLength)` followed by exactly `byteLength` UTF-8 bytes. `byteLength` counts UTF-8 bytes,
not UTF-16 code units or Unicode code points. No Unicode normalization, case folding, trimming,
locale transformation, or presentation cleanup is applied. Every `TEXT` value participating in V2
must be a valid Unicode scalar-value sequence before publication, on the same terms V1
requires for V1: ill-formed text is a publication validation error, so fingerprinting is total over
published V2 models.

`OPTIONAL_F64(x)`
: one presence byte followed, when present, by an IEEE 754 binary64 payload:

```text
00                              null
01 <8-byte binary64 payload>    present
```

The payload is big-endian. Positive and negative zero remain distinct; finite values retain their
exact binary64 value; positive and negative infinity retain their IEEE 754 encodings if admitted by
domain validation; any NaN value admitted by the domain is canonicalized to `0x7ff8000000000000`.

`PRESENCE`
: exactly one byte selecting whether the spatial record is present, using the same two values as
the `OPTIONAL_F64` presence byte:

```text
00    spatial record absent
01    spatial record present
```

No other value is valid.

## 6. The `factory-model:v2` canonical stream

After the §4 policy-domain prefix:

```text
PRESENCE(spatial)

if spatial is present:
    I64(spatial.floor.width)
    I64(spatial.floor.height)
    I64(spatial.ticksPerCell)
    I64(spatial.handlingTicks)

U64(resources.size)
for resource in resources, in list order:
    I64(resource.id.value)
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
            I64(resourceId.value)

U64(products.size)
for product in products, in list order:
    I64(product.id.value)
    TEXT(product.name)
    I64(product.operationId)
```

`spatial.position(resource)` and `spatial.footprint(resource)` denote that resource's one position
and footprint in the present spatial record (§1.1). This grammar is the normative source of
`factory-model:v2` fingerprint bytes.

### 6.1 Spatial-record marker and plant-scope header

The `PRESENCE` marker immediately follows the prefix. `00` means the spatial record is absent: no
spatial header and no per-resource spatial fields follow, and the rest of the stream is exactly
V1's production stream. `01` means the record is present: the plant-scope header and the §6.2
per-resource fields follow. Placing the marker first lets a verifier know the complete shape of
every following record before decoding any of it, so each artifact has exactly one parse, and an
absent record occupies one byte and carries no spatial payload at all.

`floor.width`, `floor.height`, `ticksPerCell`, and `handlingTicks` are plant-scope authored facts:
one value each per Factory model, not per resource. Engine interpretations such as
[Engine Semantics v1](engine-semantics-v1.md) consume them as plant scalars —
`handlingTicks + (ticksPerCell * manhattanDistance)`, with no per-resource subscript — and the §1.1
maximum-transfer predicate is likewise stated over single `W`, `H`, `ticksPerCell`, and
`handlingTicks` values.

When present they are encoded as one contiguous block immediately after the marker, preserving the
§1.1 relative table order among plant-scope fields (floor width, floor height, `ticksPerCell`,
`handlingTicks`). The header is fixed-arity, so it carries no count prefix.

Header-first placement is a deliberate ordering choice, not an arbitrary one. The §1.1 publication
predicate

```text
maxManhattanDistance = (W - 1) + (H - 1)
maxTransferDuration  = handlingTicks + ticksPerCell * maxManhattanDistance
```

depends only on header fields. Encoding the header before any resource record lets a verifier reject
an artifact whose declared transfer magnitudes are unrepresentable, and lets it establish the floor
bounds every footprint is checked against, before it allocates or validates a single resource
record.

### 6.2 Per-resource spatial suffix

When the record is present, `position.x`, `position.y`, `footprint.width`, and `footprint.height`
are appended after the five V1 resource fields, in the §1.1 table order (position `x`, position `y`,
footprint width, footprint height).

Appending rather than interleaving keeps the V2 resource record a strict extension of the V1 record.
That makes the V1-to-V2 grammar delta auditable field-by-field and keeps one shared encoder core
correct for both policies. It does not create any byte-level compatibility between the two policies;
§2 and §4 remain the operative rule.

Encoding each resource's spatial fields inside that resource's own record also makes a present
record's coverage structural: the grammar has no encoding for a resource without spatial fields, for
spatial fields without a resource, or for two sets of spatial fields for one resource. Publication
therefore rejects, before encoding, any design whose present spatial record does not give every
configured resource exactly one position and footprint (§1.1).

## 7. Placement and footprint encoding

1. Placement is encoded as the authored **anchor plus extent** — four separate `I64` values per
   resource when the spatial record is present — never as a derived occupied-cell set, cell list,
   bitmap, bounding box, or region identifier.

2. §1.1 derives the occupied cells of a resource at `(x,y)` with footprint `w` by `h` as exactly
   `x..x+w-1` by `y..y+h-1`. That derivation is a **validation-time and Engine-time** concept. It is
   never part of the canonical stream. For every publishable footprint (`w >= 1`, `h >= 1`) the
   anchor-plus-extent encoding is bijective with the occupied-cell set, so encoding the anchor loses
   no authored information while keeping the stream fixed-width.

3. Footprint participates in the digest even though Engine Semantics v1 does not use it in transfer
   distance. Per §1.1, footprint is canonical authored content required for
   publication/layout validation and future spatial capability. Two designs differing only in a
   footprint extent are different authored designs and therefore have different V2 fingerprints.

4. The reference cell is the minimum-coordinate cell of the footprint, per §1.1. V2 encodes the
   authored anchor directly; it does not recompute, infer, or normalize it from any other field.

5. Positions are encoded **exactly as authored**. V2 defines no translation invariance, no origin
   normalization, no bounding-box shrink-to-fit, and no coordinate compaction. A design translated
   by a constant offset within the same floor is a different authored design with a different
   fingerprint. Establishing a translation-invariant equivalence would be a new policy version, not
   an implementation optimization.

6. Every V2 spatial field is encoded as `I64` and the grammar is total over the full signed 64-bit
   range. The §1.1 range and layout predicates — `floor.width >= 1`, `floor.height >= 1`,
   `x >= 0`, `y >= 0`, `w >= 1`, `h >= 1`, `x + w <= W`, `y + h <= H`, non-overlap of distinct
   footprints, `ticksPerCell >= 0`, `handlingTicks >= 0`, and the maximum-transfer predicate — are
   **publication validation**, not encoding constraints. Keeping the grammar total is what lets a
   verifier reject a violating artifact explicitly (§9.3) instead of failing to parse it ambiguously.

7. Orientation, rotation, connection points, anchors other than the minimum-coordinate cell, and
   route topology are not encoded, because §1.1 keeps them out of V2 entirely.

## 8. Collection ordering

1. `resources`, `operations`, `products`, and operation `steps` are encoded in **list order**. Order
   remains semantic under V1, and V2 does not weaken that.

2. `eligibleResources` is set-shaped and is sorted by ascending signed `MachineId` value before
   collection encoding, exactly as in V1.

3. Every list and set is encoded as `U64(elementCount)` followed by the element bytes in the order
   fixed above.

4. **V2 introduces no spatial ordering of `resources`.** Per-resource spatial fields follow the
   resource list order. Row-major, raster, distance-from-origin, and any other position-derived
   ordering are rejected. Sorting resources by position would discard the authored list order that
   V1 keeps semantic, would collapse two authored models with different declared resource order into
   one identity, and would make list order unrecoverable from the canonical artifact.

5. The §6.1 marker and plant-scope header are not collections and carry no count prefix.

## 9. Canonicality and decoding

### 9.1 Grammar rejection

A V2 verifier must reject, explicitly and without partial acceptance:

- an absent, truncated, or non-matching policy-domain prefix;
- a missing spatial-record marker, or a marker other than `00` or `01`;
- a truncated stream at any field boundary, including inside the spatial header or inside a
  resource's spatial fields;
- a collection count that is negative or exceeds the supported element range;
- a `TEXT` length that overruns the stream, or `TEXT` bytes that are not well-formed UTF-8;
- an `OPTIONAL_F64` presence byte other than `00` or `01`;
- any trailing byte after the final product record.

### 9.2 Canonicality round-trip

Decoding is only defined for artifacts that are already canonical. A decoder must re-encode the
decoded model under §6 and require byte equality with its input; a byte string that parses but does
not re-encode to itself is *decodable but not canonical* and must be rejected. This closes the gap
where two distinct byte strings could resolve to one model, and therefore to one fingerprint derived
from bytes the artifact does not actually contain.

### 9.3 Publication-predicate rejection

Additionally, and unlike a purely syntactic decoder, a V2 verifier must apply the publication
predicates (§1.1, §7.6) during decode: the production predicates always, and the spatial predicates
whenever the marker says the record is present. A byte string that satisfies the §6 grammar but
violates a V2 publication predicate is **not a valid `factory-model:v2` artifact** and must be
rejected.

This is required by the [Factory evolution contract](factory-design.md#111-semantic-evolution): historical artifact
resolution must remain policy-aware, which means resolving a stored V2 artifact must never yield a model that could not
have been published in the first place. A stored artifact that fails a predicate indicates
corruption or forgery and must fail loudly rather than resolve into an unpublishable model.

### 9.4 Fingerprint derivation from stored artifacts

Deriving a fingerprint from stored canonical bytes must proceed by decode-then-re-encode-then-digest
under §9.1–§9.3, not by digesting untrusted bytes directly. Digesting unvalidated input would let a
malformed or non-canonical artifact acquire a well-formed-looking V2 identity.

## 10. Identity and evolution

No V2 fingerprint has yet been produced by a shipped publication path or recorded against a
controlled revision, so this grammar may still be corrected by amending this document. The policy is
not released: no publication path, verifier, decoder or support declaration exists for it. Under the
[semantic evolution rules](overview.md#semantic-evolution-and-support), the first retained
attribution freezes the whole definition: from that point every implementation claiming the policy
must produce the same fingerprint for the same V2 semantic content across processes, software
versions, and implementation languages.

Changing any identity-affecting rule while still calling the policy v2 is then forbidden, including:

- which records the policy admits, which are required, and which are optional;
- the spatial-record marker, its position, its values, or the rule that a present record is
  complete;
- semantic field membership or field order;
- placement/footprint field order or anchor interpretation;
- plant-header position, arity, or field order;
- collection ordering or set sorting;
- integer widths or byte order;
- text encoding, byte-length semantics, Unicode-validity requirements, or normalization rules;
- nullable-value tags;
- floating-point canonicalization;
- any validation predicate or rejection rule;
- the policy-domain prefix;
- hash algorithm or digest rendering.

Such a change requires a distinguishable policy identity under the
[Factory evolution contract](factory-design.md#111-semantic-evolution). V1 remains unchanged by
V2's existence, and neither policy's continued publication, decoding, execution or coexistence
support follows from this byte specification; support is scoped by the owning declarations.

## 11. Golden compatibility vectors

The V2 implementation must pin literal expected canonical bytes and expected digest/fingerprint
outputs. Golden vectors supplement this normative grammar; they do not replace it.

At minimum, V2 tests must cover:

**Grammar and identity**

1. one representative V2 model with the spatial record absent and one with it present, each with
   exact expected canonical bytes and an exact expected fingerprint;
2. the 26-byte policy-domain prefix pinned exactly, proving the `v2` discriminator;
3. repeated/equivalent construction producing the same fingerprint;
4. each representative artifact decoding to its model and re-encoding to byte-identical output;
5. rejection of a stream with trailing bytes, a truncated field, a bad `OPTIONAL_F64` marker, and a
   decodable-but-non-canonical encoding.

**Spatial-record presence**

6. a spatial-absent model and a model with identical production records whose present spatial
   record holds legal zero or minimal values — position `(0,0)`, a 1×1 floor and footprint,
   `ticksPerCell = 0`, `handlingTicks = 0` — producing different bytes and fingerprints, with the
   present-with-zero fingerprint pinned, proving zero is authored content and not absence;
7. marker framing: marker values other than `00` and `01` rejected; a present-marker stream
   truncated inside the spatial header or inside a resource's spatial fields rejected; and a
   spatial-absent artifact with spatial-header bytes appended after its final product record
   rejected as trailing bytes;
8. coverage: publication rejects a design whose present spatial record omits a configured resource,
   gives a resource two positions/footprints, places an identifier that is not a configured
   resource, or places no resource at all while resources exist.

**Spatial field sensitivity**

9. two models identical except for one resource `position.x` producing different fingerprints;
   likewise for `position.y`;
10. an asymmetric position (`x != y`) whose coordinates are swapped producing a different
    fingerprint, proving `x`-before-`y` field order;
11. two models identical except for one resource footprint extent producing different fingerprints,
    proving footprint is fingerprinted even though Engine Semantics v1 ignores it in transfer
    distance;
12. an asymmetric footprint (`w != h`) whose extents are swapped producing a different fingerprint,
    proving width-before-height field order;
13. `ticksPerCell` and `handlingTicks` each independently changing the fingerprint, and a swap of
    two distinct values changing it, proving header field order;
14. `floor.width` and `floor.height` each independently changing the fingerprint, and a swap of two
    distinct values changing it;
15. a whole-design translation by a constant offset producing a different fingerprint, proving no
    translation-invariant normalization.

**Cross-policy separation**

16. the same authored production content fingerprinted under V1 and under V2 with the spatial record
    absent producing different fingerprints, with neither derivable from the other;
17. V2 artifacts, with the spatial record absent and present, rejected by the V1 verifier, and a V1
    artifact rejected by the V2 verifier;
18. every existing `factory-model:v1` golden vector and fingerprint unchanged after V2 is
    registered, as an explicit regression pin.

**Carried-over V1 coverage re-proven under V2**

19. delimiter-bearing text, non-ASCII BMP text, and astral text, proving UTF-8 byte-length framing;
20. publication rejection of unpaired-surrogate / otherwise ill-formed Unicode text in every
    `TEXT`-participating name field;
21. invariance to `eligibleResources` iteration order, and sensitivity to `resources`, `operations`,
    `products`, and `steps` list order;
22. null versus present `capacityLiters`; positive, negative, zero, and signed-zero binary64 values;
    NaN canonicalization if NaN remains publishable.

**Publication-predicate rejection (§9.3)**

23. artifacts that satisfy the grammar but violate a V2 predicate — footprint extending past the
    floor, overlapping distinct footprints, `w` or `h` below `1`, negative `x` or `y`, a `floor`
    dimension below `1`, negative `ticksPerCell` or `handlingTicks`, a violated
    maximum-transfer-duration predicate including an unrepresentable intermediate multiplied by a
    zero `ticksPerCell`, and a spatial-absent artifact violating a production predicate — each
    rejected on decode as well as on publication;
24. the accepted boundaries beside those rejections: footprints adjacent at an edge or a corner, a
    footprint placed exactly on the maximum legal floor edge, a 1×1 floor, and a maximum-transfer
    duration exactly equal to the largest representable value.

## 12. Non-goals

This document does not define:

- cross-policy compatibility rules or which policies remain publishable (the
  [Factory evolution contract](factory-design.md#111-semantic-evolution));
- result-affecting interpretation of the spatial facts, including the distance metric, handling
  application, destination binding, or transfer lifecycle, or which Engine interpretation applies
  to artifacts of this policy (Engine semantics, §1.2);
- controlled-revision identity, lineage, or repository authority ([controlled revisions](controlled-revisions.md));
- cross-policy `ChangeSet` or migration-classification representation, beyond requiring that V1 and
  V2 artifacts remain structurally distinguishable;
- the external interchange/serialization formats a consumer authors a design in
  ([external representations](external-representations.md));
- a universal canonicalization scheme for non-Factory Arcogine domains.
