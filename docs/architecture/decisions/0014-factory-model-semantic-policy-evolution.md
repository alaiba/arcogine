# ADR-0014: Factory Model Semantic-Policy Evolution

Status: Proposed
Date: 2026-09-02

## Context

Arcogine's released `factory-model:v1` fingerprint policy is immutable under
[ADR-0006](0006-durable-semantic-fingerprint-contract.md). That policy identifies the
current canonical factory semantics and is already used by controlled revision history,
historical semantic-state reconstruction, runtime provenance, and downstream consumers.

Engine Gate 5 introduces behaviorally relevant spatial semantics. The maintained Factory
Design and Engine plans already place stable layout inputs on the canonical model side and
changing transfer consequences on the simulation-runtime side. In particular, moving a
resource is intended to change transfer behavior without changing the resource's identity.

`factory-model:v1` does not encode the spatial facts required for that contract. Extending
its canonical bytes in place would break the immutability and historical reproducibility
promise of ADR-0006. Keeping those facts outside durable semantic identity would create the
opposite problem: two behaviorally different authored factory designs could share one
`ModelFingerprint`.

The first mature model-policy evolution therefore needs an explicit contract for how a
released canonical semantic artifact gains new behaviorally relevant authored facts without
rewriting historical identity.

This ADR is intentionally limited to authored Factory semantic-policy evolution. It does not
decide the Engine-owned interpretation rules that turn those facts into transfer timing;
that sibling question is proposed separately in
[ADR-0015](0015-engine-semantics-identity-and-reproducibility.md).

## Decision

The proposed decision is:

1. **`factory-model:v1` remains immutable permanently.** Existing fingerprints, canonical
   artifacts, controlled revisions, and historical resolution are never rewritten or
   rederived under a newer policy.

2. **Behaviorally relevant spatial design facts require a new fingerprint policy.** The
   first such evolution is `factory-model:v2`.

3. **`factory-model:v2` remains a complete canonical Factory semantic artifact, not a second
   spatial sidecar fingerprint.** A v2 fingerprint identifies the exact authored Factory
   semantics required by the v2 policy.

4. **The minimum Gate 5 spatial facts proposed for v2 are authored design semantics:**
   - factory-floor dimensions needed to interpret placements;
   - resource position;
   - resource footprint;
   - stable authored transfer-cost magnitudes such as `ticksPerCell` and `handlingTicks`, if
     the final Gate 5 decision confirms those magnitudes are properties of the designed
     production system rather than Engine interpretation policy.

   The exact final v2 field set remains subject to the focused Gate 5 transfer-semantics
   decision. Orientation, route graphs, aisles, conveyors, transport resources, pathfinding,
   congestion, and animation metadata are not pulled into v2 merely because they are spatial.

5. **Fingerprint-policy version and controlled-revision identity remain distinct.** A
   `ControlledRevision` continues to reference exactly one `ModelFingerprint`; lineage may
   connect historical occurrences whose fingerprints use different supported Factory model
   policies. Crossing a policy version does not rewrite or alias either fingerprint.

6. **Cross-policy semantic equality is never assumed implicitly.** Equality of
   `factory-model:v1` and `factory-model:v2` fingerprints has no meaning beyond ordinary
   fingerprint inequality. Any migration/equivalence claim must use an explicit
   Factory-domain comparison or migration contract that states how both artifacts are
   interpreted.

7. **A normal semantic comparison must not silently compare incompatible policy shapes.** For
   an initial `v1 -> v2` transition, Arcogine must either:
   - explicitly classify the transition as a model-policy migration; or
   - resolve both artifacts into an explicitly chosen common semantic representation before
     producing fine-grained semantic differences.

   The first implementation should choose the narrowest of those mechanisms needed by the
   actual v1-to-v2 proving case rather than introduce a generic migration framework.

8. **Historical artifact resolution is policy-aware and permanent.** Factory supplies a
   verifier/decoder for every released Factory model policy that must remain historically
   resolvable. Governance stays domain-neutral: it stores and verifies immutable semantic
   artifacts through the owning domain's policy-aware verification seam.

9. **A v1 model is not silently upgraded into spatial semantics.** Gate 5 behavior requires an
   explicitly published artifact under a policy that contains the required spatial facts.
   Arcogine does not synthesize default positions, footprints, or transfer magnitudes for a
   historical v1 artifact and then pretend the resulting design has the same fingerprint.

10. **General evolution invariant:** when a future behaviorally relevant authored fact cannot
    be represented by the currently released canonical policy without changing its canonical
    meaning, Arcogine introduces a new policy `factory-model:vN+1`; old policies remain
    immutable and historically resolvable; fingerprints are never rewritten; lineage may
    continue across policy versions; cross-policy semantic comparison is explicit.

## Alternatives considered

### Extend `factory-model:v1` in place

Rejected. It would make a previously released fingerprint policy produce different canonical
bytes/meaning and would invalidate ADR-0006's historical identity contract.

### Keep v1 and add an independent spatial fingerprint

Rejected for the current need. Gate 5 requires one authored Factory design whose spatial facts
are behaviorally meaningful. Splitting semantic identity into a base fingerprint plus an
independent spatial fingerprint would complicate revision/change/evidence provenance before a
real independently evolving component boundary has been proven necessary.

### Keep spatial layout outside canonical semantic identity

Rejected. Two authored layouts that produce different deterministic transfer consequences would
then share one `ModelFingerprint`, making the fingerprint an incomplete identity for the design
being executed.

### Introduce a generic component-schema/fingerprint framework now

Rejected. The first policy evolution can establish explicit versioned Factory artifact handling
without generalizing every future domain or semantic component into an independently versioned
framework.

### Automatically migrate every v1 artifact to v2 defaults

Rejected. Spatial defaults would be newly authored semantics, not a historically true
interpretation of the v1 artifact. Migration must be explicit and provenance-preserving.

## Consequences

- Gate 5 model work has a durable evolution path without weakening historical v1 identity.
- Existing controlled revisions remain valid and reconstructable exactly as recorded.
- Factory code must become capable of verifying/decoding more than one released artifact policy
  once v2 lands.
- The first v1-to-v2 controlled change needs an explicit policy-migration/comparison rule.
- Consumers must not equate fingerprint-policy migration with resource identity replacement;
  moving or spatially changing a resource can preserve its resource identity while changing the
  enclosing model fingerprint.
- This ADR does not by itself determine transfer distance, rounding, destination binding, runtime
  transfer state, or Engine provenance. Those result-affecting interpretation rules belong to
  the Engine semantics decision and the final Gate 5 contract.

## Charter alignment

This proposal supports the Charter's single-semantic-source and reproducibility goals: authored
production-system facts remain part of Arcogine's canonical model, while historical semantic
identity remains stable across software and schema evolution. It also avoids coupling consumer
presentation or runtime implementation mechanics to durable model identity.
