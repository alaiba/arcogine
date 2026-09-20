# Factory Model v2 Canonicalization

Status: Normative canonicalization contract; shape/validation implemented, canonical publication not implemented
Fingerprint policy: `factory-model:v2`
Sibling policy: [Factory Model v1 Canonicalization](factory-model-v1.md)
Evolution rule: [Semantic evolution and support](overview.md#semantic-evolution-and-support) and the [Factory semantic-evolution contract](factory-design.md#111-semantic-evolution)

## 1. Purpose

A Factory fingerprint-policy version **is** a canonicalization contract: the policy version
identifies the semantic/canonicalization contract, not merely the cryptographic algorithm. Any
change that can alter semantic field membership, ordering, normalization, binary encoding, or
digest semantics requires a new policy version.

This document is the normative source of `factory-model:v2`: the authored facts it adds to V1
(§1.1), their validation predicates, and the durable byte grammar those facts are digested through,
in exactly the sense that [Factory Model v1](factory-model-v1.md) is the normative source of
`factory-model:v1` bytes. Where this document and any implementation disagree, this document is
authoritative. Its implemented shape/validation slice does not by itself establish a published
fingerprint/artifact path or continuing support; those follow the owning
[support declarations](../development/semantic-contract-support.md).

It deliberately does **not** restate or modify:

- cross-policy compatibility, lift and comparison rules — those are the
  [Factory semantic-evolution contract](factory-design.md#111-semantic-evolution);
- result-affecting Engine interpretation of V2 facts — that is
  [Engine Semantics v1](engine-semantics-v1.md);
- controlled-revision identity or lineage — that is the
  [controlled revision contract](controlled-revisions.md).

### 1.1 Authored facts and publication predicates

V2 is exactly V1 semantics plus these five required authored additions:

| Addition | Meaning | Validation | Zero legal? | Fingerprinted? |
|---|---|---|---|---|
| floor width / height | plant extent in integer cells | each `>= 1`; the exact maximum-transfer predicate below must hold | no | yes |
| resource position `x` / `y` | minimum-coordinate reference cell of the resource footprint | `x >= 0`, `y >= 0`; footprint occupies the exact cells defined below and must fit inside the floor | yes (`0,0` is valid) | yes |
| footprint width / height | integer cells occupied by the resource from its reference cell | each `>= 1`; distinct resource footprints must not overlap | no | yes |
| `ticksPerCell` | authored material-handling rate magnitude | integer `>= 0`; the exact maximum-transfer predicate below must hold | yes | yes |
| `handlingTicks` | authored fixed overhead applied once per inter-resource transfer | integer `>= 0`; the exact maximum-transfer predicate below must hold | yes | yes |

The position anchor is part of V2 model semantics: for a resource at `(x,y)` with footprint width
`w` and height `h`, the footprint occupies exactly the integer cells `x..x+w-1` by `y..y+h-1`. A
footprint fits inside a floor of width `W` and height `H` iff `x >= 0`, `y >= 0`, `w >= 1`,
`h >= 1`, `x + w <= W`, and `y + h <= H`, evaluated with overflow-safe arithmetic. Two resources
overlap iff those occupied-cell sets intersect.

V2 publication must also prove the spatial transfer-duration magnitude is representable for the
farthest possible pair of reference cells in the authored floor. With floor dimensions `W` and `H`:

```text
maxManhattanDistance = (W - 1) + (H - 1)
maxTransferDuration  = handlingTicks + ticksPerCell * maxManhattanDistance
```

Publication accepts the artifact only when every subtraction, addition, and multiplication in that
predicate is representable in the runtime tick-duration type and `maxTransferDuration` is
representable there. This bounds the derived transfer duration itself; it does **not** claim that
adding an otherwise valid duration to an arbitrarily extreme current `SimTime` can never overflow —
the runtime time-addition validation remains responsible for that condition.

All five additions are mandatory in a V2 artifact. Changing any of them changes the authored
Factory design and therefore changes `ModelFingerprint`. They are model facts, not Engine policy:
floor extent, placement, footprint, material-handling rate and fixed handling overhead describe the
production system the designer authored, while Arcogine's choice of distance metric, rounding,
zero-distance behavior, destination binding, reservation and transfer lifecycle belongs to
[Engine Semantics](engine-semantics-v1.md). Footprint remains canonical even though Engine
Semantics v1 does not use it in transfer distance: it is required for publication/layout validation
and future spatial capability, and a later Engine version may interpret the same V2 footprint
differently without re-fingerprinting any V2 design.

Orientation is not part of V2. Neither are paths, graph edges, aisles, conveyors, transport
resources, obstacles, congestion, floor identity, connection points, authoritative animation
coordinates, or route topology. They require a later model policy only when a concrete capability
makes them authored behaviorally relevant semantics.

## 2. Relationship to `factory-model:v1`

V2 is a separate released policy, not a revision of V1. V1's grammar, digests, golden vectors, and
historical fingerprints are permanently unchanged by V2's existence.

Structurally, the V2 stream is the V1 stream with exactly three differences:

1. a different policy-domain prefix (§4);
2. a plant-scope spatial header inserted immediately after the prefix (§6.1);
3. four additional `I64` fields appended to each resource record (§6.2).

Operation, step, and product records are shape-identical to V1's. No V1 field is removed, reordered,
retyped, or renormalized.

Because the policy-domain prefixes differ in their final component, **V1 bytes are never a prefix of
V2 bytes, no V1 artifact decodes under a V2 verifier, and no V2 artifact decodes under a V1
verifier.** Cross-policy artifact confusion is structurally impossible rather than a runtime check.
This is what makes the Factory evolution contract's policy-aware historical resolution and its
prohibition on automatic V1-to-V2 lift mechanically enforceable at the artifact boundary.

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

V2 introduces **no new primitive**. It reuses V1's primitives unchanged, restated here normatively
so this document is self-contained.

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

## 6. The `factory-model:v2` canonical stream

After the §4 policy-domain prefix:

```text
I64(floor.width)
I64(floor.height)
I64(ticksPerCell)
I64(handlingTicks)

U64(resources.size)
for resource in resources, in list order:
    I64(resource.id.value)
    TEXT(resource.name)
    I64(resource.concurrency)
    OPTIONAL_F64(resource.capacityLiters)
    I64(resource.setupTime)
    I64(resource.position.x)
    I64(resource.position.y)
    I64(resource.footprint.width)
    I64(resource.footprint.height)

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

This grammar is the normative source of `factory-model:v2` fingerprint bytes.

### 6.1 Plant-scope spatial header

`floor.width`, `floor.height`, `ticksPerCell`, and `handlingTicks` are plant-scope authored facts:
one value each per Factory model, not per resource. [Engine Semantics v1](engine-semantics-v1.md)
consumes them as plant scalars — `handlingTicks + (ticksPerCell * manhattanDistance)`, with no
per-resource subscript — and the §1.1 maximum-transfer predicate is likewise stated over single
`W`, `H`, `ticksPerCell`, and `handlingTicks` values.

They are encoded as one contiguous block immediately after the prefix, preserving the §1.1
relative table order among plant-scope fields (floor width, floor height, `ticksPerCell`,
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

`position.x`, `position.y`, `footprint.width`, and `footprint.height` are appended after the five V1
resource fields, in the §1.1 table order (position `x`, position `y`, footprint width, footprint
height).

Appending rather than interleaving keeps the V2 resource record a strict extension of the V1 record.
That makes the V1-to-V2 grammar delta auditable field-by-field and keeps one shared encoder core
correct for both policies. It does not create any byte-level compatibility between the two policies;
§2 and §4 remain the operative rule.

## 7. Placement and footprint encoding

1. Placement is encoded as the authored **anchor plus extent** — four separate `I64` values per
   resource — never as a derived occupied-cell set, cell list, bitmap, bounding box, or region
   identifier.

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

4. **V2 introduces no spatial ordering of `resources`.** Row-major, raster, distance-from-origin,
   and any other position-derived ordering are rejected. Sorting resources by position would discard
   the authored list order that V1 keeps semantic, would collapse two authored models with
   different declared resource order into one identity, and would make list order unrecoverable from
   the canonical artifact.

5. The §6.1 plant-scope header is not a collection and carries no count prefix.

## 9. Canonicality and decoding

### 9.1 Grammar rejection

A V2 verifier must reject, explicitly and without partial acceptance:

- an absent, truncated, or non-matching policy-domain prefix;
- a truncated stream at any field boundary;
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
predicates (§1.1, §7.6) during decode. A byte string that satisfies the §6 grammar but violates a V2
publication predicate is **not a valid `factory-model:v2` artifact** and must be rejected.

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
controlled revision, so this grammar may still be corrected by amending this document. Under the
[semantic evolution rules](overview.md#semantic-evolution-and-support), the first retained
attribution freezes the whole definition: from that point every implementation claiming the policy
must produce the same fingerprint for the same V2 semantic content across processes, software
versions, and implementation languages.

Changing any identity-affecting rule while still calling the policy v2 is then forbidden, including:

- semantic field membership or field order;
- placement/footprint field order or anchor interpretation;
- plant-header position, arity, or field order;
- collection ordering or set sorting;
- integer widths or byte order;
- text encoding, byte-length semantics, Unicode-validity requirements, or normalization rules;
- nullable-value tags;
- floating-point canonicalization;
- the policy-domain prefix;
- hash algorithm or digest rendering.

Such a change requires a distinguishable policy identity under the
[Factory evolution contract](factory-design.md#111-semantic-evolution). V1 remains unchanged by
V2's existence, and neither policy's continued decoding, execution or coexistence support follows
from this byte specification; support is scoped by the owning declarations.

## 11. Golden compatibility vectors

The V2 implementation must pin literal expected canonical bytes and expected digest/fingerprint
outputs. Golden vectors supplement this normative grammar; they do not replace it.

At minimum, V2 tests must cover:

**Grammar and identity**

1. one representative full V2 model with exact expected canonical bytes and an exact expected
   fingerprint;
2. the 26-byte policy-domain prefix pinned exactly, proving the `v2` discriminator;
3. repeated/equivalent construction producing the same fingerprint;
4. rejection of a stream with trailing bytes, a truncated field, a bad `OPTIONAL_F64` marker, and a
   decodable-but-non-canonical encoding.

**Spatial field sensitivity**

5. two models identical except for one resource `position.x` producing different fingerprints;
   likewise for `position.y`;
6. an asymmetric position (`x != y`) whose coordinates are swapped producing a different
   fingerprint, proving `x`-before-`y` field order;
7. two models identical except for one resource footprint extent producing different fingerprints,
   proving footprint is fingerprinted even though Engine Semantics v1 ignores it in transfer
   distance;
8. an asymmetric footprint (`w != h`) whose extents are swapped producing a different fingerprint,
   proving width-before-height field order;
9. `ticksPerCell` and `handlingTicks` each independently changing the fingerprint, and a swap of two
   distinct values changing it, proving header field order;
10. `floor.width` and `floor.height` each independently changing the fingerprint, and a swap of two
    distinct values changing it;
11. legal zero-valued spatial content — position `(0,0)`, `ticksPerCell = 0`, `handlingTicks = 0` —
    producing a stable, pinned fingerprint rather than being treated as absent;
12. a whole-design translation by a constant offset producing a different fingerprint, proving no
    translation-invariant normalization.

**Cross-policy separation**

13. the same authored non-spatial content fingerprinted under V1 and under V2 producing different
    fingerprints, with neither derivable from the other;
14. a V2 artifact rejected by the V1 verifier and a V1 artifact rejected by the V2 verifier;
15. every existing `factory-model:v1` golden vector and fingerprint unchanged after V2 is
    registered, as an explicit regression pin.

**Carried-over V1 coverage re-proven under V2**

16. delimiter-bearing text, non-ASCII BMP text, and astral text, proving UTF-8 byte-length framing;
17. publication rejection of unpaired-surrogate / otherwise ill-formed Unicode text in every
    `TEXT`-participating name field;
18. invariance to `eligibleResources` iteration order, and sensitivity to `resources`, `operations`,
    `products`, and `steps` list order;
19. null versus present `capacityLiters`; positive, negative, zero, and signed-zero binary64 values;
    NaN canonicalization if NaN remains publishable.

**Publication-predicate rejection (§9.3)**

20. artifacts that satisfy the grammar but violate a V2 predicate — footprint extending past the
    floor, overlapping distinct footprints, `w` or `h` below `1`, negative `x` or `y`, a `floor`
    dimension below `1`, negative `ticksPerCell` or `handlingTicks`, and a violated
    maximum-transfer-duration predicate — each rejected on decode as well as on publication.

## 12. Non-goals

This document does not define:

- cross-policy compatibility rules (the [Factory evolution contract](factory-design.md#111-semantic-evolution));
- result-affecting interpretation of V2 facts, including the distance metric, handling application,
  destination binding, or transfer lifecycle ([Engine Semantics v1](engine-semantics-v1.md));
- controlled-revision identity, lineage, or repository authority ([controlled revisions](controlled-revisions.md));
- cross-policy `ChangeSet` or migration-classification representation, beyond requiring that V1 and
  V2 artifacts remain structurally distinguishable;
- the external interchange/serialization formats a consumer authors a design in
  ([external representations](external-representations.md));
- a universal canonicalization scheme for non-Factory Arcogine domains.
