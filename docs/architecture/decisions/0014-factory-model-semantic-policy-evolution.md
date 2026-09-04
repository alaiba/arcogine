# ADR-0014: Factory Model Semantic-Policy Evolution

Status: Accepted
Date: 2026-09-03

## Context

Arcogine's released `factory-model:v1` fingerprint policy is immutable under
[ADR-0006](0006-durable-semantic-fingerprint-contract.md). It identifies the exact authored
Factory semantics used by controlled revision history, historical reconstruction, runtime
provenance, and downstream consumers.

Spatial transfer capability introduces authored spatial facts that change deterministic runtime
consequences. Those facts cannot be added to `factory-model:v1` without changing the meaning and
canonical bytes of an already released policy, and they cannot remain outside `ModelFingerprint`
without allowing two behaviorally different authored designs to share one identity.

This capability also exposes an important ownership boundary: the model identifies **what production
system the designer authored**; result-affecting rules describing **how Arcogine interprets any
design** belong to Engine semantic policy. That sibling decision is recorded by
[ADR-0015](0015-engine-semantics-identity-and-reproducibility.md).

## Decision

1. **`factory-model:v1` remains immutable permanently.** Existing fingerprints, canonical
   artifacts, controlled revisions, and historical resolution are never rewritten or rederived
   under a newer policy.

2. **Authored spatial semantics use `factory-model:v2`.** V2 is a complete canonical Factory
   semantic artifact, not a spatial sidecar fingerprint.

   Because ADR-0006 makes a fingerprint-policy version a canonicalization contract and not merely a
   field-membership label, V2 is not fully specified until its durable byte grammar is fixed.
   **V2 canonicalization is defined normatively in
   [Factory Model v2 Canonicalization](../factory-model-v2.md)**: the policy-domain prefix, exact
   field order, primitive encodings, placement/footprint encoding, collection ordering, digest
   rendering, decode/canonicality obligations, and the required golden compatibility vectors. That
   document is the authoritative source of V2 fingerprint bytes, as ADR-0006's v1 byte-grammar
   section is for V1. This ADR remains the authority for V2's semantic field membership, validation
   predicates, and compatibility rules; it does not restate the grammar.

3. **V2 is exactly V1 semantics plus these five required authored additions:**

   | Addition | Meaning | Validation | Zero legal? | Fingerprinted? |
   |---|---|---|---|---|
   | floor width / height | plant extent in integer cells | each `>= 1`; the exact maximum-transfer predicate below must hold | no | yes |
   | resource position `x` / `y` | minimum-coordinate reference cell of the resource footprint | `x >= 0`, `y >= 0`; footprint occupies the exact cells defined below and must fit inside the floor | yes (`0,0` is valid) | yes |
   | footprint width / height | integer cells occupied by the resource from its reference cell | each `>= 1`; distinct resource footprints must not overlap | no | yes |
   | `ticksPerCell` | authored material-handling rate magnitude | integer `>= 0`; the exact maximum-transfer predicate below must hold | yes | yes |
   | `handlingTicks` | authored fixed overhead applied once per inter-resource transfer | integer `>= 0`; the exact maximum-transfer predicate below must hold | yes | yes |

   The position anchor is part of V2 model semantics: for a resource at `(x,y)` with footprint
   width `w` and height `h`, the footprint occupies exactly the integer cells
   `x..x+w-1` by `y..y+h-1`. A footprint fits inside a floor of width `W` and height `H` iff
   `x >= 0`, `y >= 0`, `w >= 1`, `h >= 1`, `x + w <= W`, and `y + h <= H`, evaluated with
   overflow-safe arithmetic. Two resources overlap iff those occupied-cell sets intersect.

   V2 publication must also prove the spatial transfer-duration magnitude is representable for the
   farthest possible pair of reference cells in the authored floor. With floor dimensions `W` and
   `H`, define:

   ```text
   maxManhattanDistance = (W - 1) + (H - 1)
   maxTransferDuration = handlingTicks + ticksPerCell * maxManhattanDistance
   ```

   Publication accepts the artifact only when every subtraction, addition, and multiplication in
   that predicate is representable in the runtime tick-duration type and `maxTransferDuration` is
   representable there. This bounds the derived transfer duration itself; it does **not** claim that
   adding an otherwise valid duration to an arbitrarily extreme current `SimTime` can never
   overflow. Existing runtime time-addition validation remains responsible for that pre-existing
   condition.

   All five additions are mandatory in a V2 artifact. Changing any of them changes the authored
   Factory design and therefore changes `ModelFingerprint`.

4. **The five additions are model facts, not Engine policy.** Floor extent, placement, footprint,
   material-handling rate, and fixed handling overhead describe the production system the designer
   authored. Arcogine's choice of distance metric, rounding, zero-distance behavior, destination
   binding, reservation, and transfer lifecycle does not belong in the model fingerprint.

5. **Orientation is not part of V2.** Neither are paths, graph edges, aisles, conveyors, transport
   resources, obstacles, congestion, floor identity, connection points, authoritative animation
   coordinates, or route topology. They require a later model-policy revision only when a concrete
   capability makes them authored behaviorally relevant semantics.

6. **Footprint remains canonical even though Engine semantics v1 does not use it in transfer
   distance.** It is required for publication/layout validation and future spatial capability. A
   later Engine semantics version may interpret the same V2 footprint differently without
   requiring every V2 design to be re-fingerprinted.

7. **Fingerprint-policy version and controlled-revision identity remain distinct.** A
   `ControlledRevision` still references exactly one `ModelFingerprint`. Controlled-revision
   lineage may cross Factory model-policy versions; a V2 revision may name a V1 revision as its
   parent without rewriting either artifact.

8. **Historical artifact resolution is policy-aware and permanent.** Factory must retain the
   verifier/decoder necessary to resolve every released Factory model policy. Registering V2 must
   not make historical V1 revisions unreadable or unverifiable.

9. **A V1 model remains fully supported under its existing non-spatial semantics.** V1 has no
   spatial facts, therefore it has no spatial transfer behavior or transfer timing. This is not a
   degraded mode; it is the truthful execution of a design that never authored spatial semantics.

10. **There is no automatic V1-to-V2 lift.** Arcogine never synthesizes position, footprint,
    `ticksPerCell`, or `handlingTicks` for an existing V1 artifact. Spatial behavior requires an
    explicitly published V2 artifact and therefore a new fingerprint.

11. **Cross-policy comparison is explicit.** A normal semantic `ChangeSet` must not silently span
    V1 and V2 by inventing layout facts the V1 model never declared. Before the first V1-to-V2
    controlled transition, Arcogine must provide:
    - multi-policy artifact resolution with a registered V2 verifier/decoder; and
    - either an explicit model-policy migration classification, or an explicitly chosen common
      semantic representation for any fine-grained comparison that claims equivalence.

    The first implementation must choose the narrowest mechanism required by the real transition;
    no generic migration framework is required.

12. **General evolution invariant:** when a future behaviorally relevant authored fact cannot be
    represented without changing the meaning of a released Factory model policy, Arcogine creates
    `factory-model:vN+1`. Old policies remain immutable and historically resolvable; fingerprints
    are never rewritten; lineage may continue across policies; cross-policy comparison remains
    explicit.

## Alternatives considered

### Extend `factory-model:v1` in place

Rejected. It would change the canonical meaning of already released fingerprints and violate
ADR-0006.

### Keep spatial facts outside `ModelFingerprint`

Rejected. Authored designs with different deterministic spatial consequences could then share one
semantic identity.

### Add an independent spatial fingerprint

Rejected for the current capability. Spatial execution needs one authored Factory design. Splitting
identity before independently evolving semantic components are required would add compatibility
complexity without benefit.

### Put the transfer metric in V2

Rejected. A designer placing resources authors their positions and handling magnitudes; Arcogine
chooses how those positions are interpreted for transport. Changing the Engine's distance rule is
an Engine semantic-policy change, not a new authored plant.

### Automatically migrate V1 artifacts to V2 defaults

Rejected. The defaults would be newly invented design semantics, not historical truth about the V1
artifact.

## Consequences

- Spatial execution receives a stable authored spatial substrate without weakening V1 identity.
- The V2 canonicalization contract required by ADR-0006 is a separate normative document rather than
  ADR text, so the durable byte grammar and its golden vectors can be reviewed, cited, and amended
  before `G5-A2` ships without reopening this decision record.
- `ModelFingerprint` continues to mean "which authored Factory design", not "which Engine outcome".
- Existing V1 revisions remain valid and executable under their existing non-spatial semantics.
- Factory must support more than one artifact policy when V2 lands.
- The first V1-to-V2 controlled transition requires an explicit policy boundary rather than a
  misleading ordinary diff.
- Transfer metric, rounding, destination binding, reservation, and lifecycle are intentionally
  absent from V2 and are governed by Engine semantic policy.

## Charter alignment

The decision preserves a single canonical semantic source for authored production-system facts,
keeps historical model identity stable, and prevents Engine interpretation policy from masquerading
as Factory design content.
