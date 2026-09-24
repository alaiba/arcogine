# Factory Model Semantic Composition Research

> **Status:** CONCLUDED — see the maintained [research register](../research-register.md)
>
> **Scope:** How authored Factory semantics compose when not every design represents every behaviorally relevant concern: canonical structure, optional concerns, identity, validation and Engine applicability
>
> **Authority:** Research provenance only. The durable result lives in the [Factory semantic-evolution contract](../../architecture/factory-design.md#111-semantic-evolution), [Factory Model v2](../../architecture/factory-model-v2.md) and the [Determinism Contract](../../architecture/overview.md#determinism-contract); this brief decides nothing.

## Question

> How should Arcogine represent independently applicable authored Factory semantics so that adding a
> new concern does not automatically turn feature accumulation into a linear whole-model
> `factory-model:vN` progression?

## Conclusion

The question was high-risk and received an independent adversarial review with disposition
**ACCEPT WITH QUALIFICATIONS**. The result is a bounded choice for current evidence, not a universal
theorem:

- a Factory fingerprint policy is one closed, complete, immutable grammar, and that grammar may admit
  explicitly present optional authored records; absence asserts nothing in that dimension and is
  never synthesized, and a present legal zero is authored content distinct from absence;
- one aggregate `ModelFingerprint` per published design remains the identity model; an exact
  immutable reference to an independently governed technical contract is available when a concrete
  semantic dependency requires one;
- after first attribution, any identity-defining grammar change needs a distinguishable policy
  identity; cross-policy comparison stays explicit, may use only a non-inventive
  distinction-preserving common representation, and never implies full-fingerprint equality,
  occurrence identity, evidence applicability or reattribution; continued publication and other
  support stay separate from identity;
- unreleased `factory-model:v2` becomes the first such policy: required production records with
  unchanged `factory-model:v1` meaning, plus one optional spatial record that keeps the five current
  spatial/handling facts together as a deliberately coarse scope. `factory-model:v1` bytes,
  meaning, artifacts and controlled-revision bindings are untouched;
- mandatory whole-model bundles were not disproven, and more open structures — concern envelopes,
  per-concern identities, profiles as content identity, generic migration machinery — were not
  adopted because no current requirement justified their extra framework, not because they cannot
  work.

Durable destinations:

- [Factory semantic-evolution contract](../../architecture/factory-design.md#111-semantic-evolution) —
  composition, attribution, reference, comparison, applicability and support rules;
- [Factory Model v2](../../architecture/factory-model-v2.md) — the corrected grammar, presence and
  coverage semantics, and required future golden vectors;
- [Determinism Contract](../../architecture/overview.md#determinism-contract) — publication validity
  never establishes Engine applicability;
- the `factory-model:v2` proving shape/validation and its tests in the Factory module — absence,
  present legal zero, complete coverage, and every retained spatial predicate;
- [Factory Design capability](../../planning/factory-design-capability.md) and
  [Spatial Runtime Consequences](../../planning/spatial-runtime-consequences.md) — re-scoped held
  slices and their remaining prerequisites;
- the non-normative
  [decision rationale](../../history/decisions/2026-09-23-factory-model-semantic-composition.md) —
  why this boundary was chosen over the serious alternatives.

## What remains open

- **Engine applicability to the corrected policy — resolved.** The
  [Engine applicability question](engine-evolution.md#concluded--engine-applicability-to-optional-record-factory-policies)
  has concluded: `engine-semantics:v1` is unchanged and not applicable to `factory-model:v2`, and
  [Engine Semantics v2](../../architecture/engine-semantics-v2.md) is the distinguishable identity
  that applies to both its spatial-present and spatial-absent forms. V2 publication release and
  spatial runtime activation now stay blocked only on their own implementation prerequisites.
- **Continued `factory-model:v1` publication.** Whether new production-only designs are still
  published under `factory-model:v1` once V2 is released is a scoped support decision for the V2
  publication work, not an architectural rule.
- **Transport-participant ownership and resource-dependent performance** remain with their own
  register questions; this result neither resolves them nor prebuilds representation for them.

## Reopening triggers

Reopen the composition choice — not the non-rebinding and support rules it consumes — when:

1. a current consumer requires complete-fingerprint equality when semantically unchanged content is
   republished;
2. concern admissions become frequent enough that closed-policy transitions and their comparison
   seams dominate cost;
3. some represented Factory semantic concern must be safely ignorable by a verifier;
4. an independently governed technical contract must be referenced as such;
5. geometry must be authored without handling magnitudes;
6. resource-dependent performance or qualification needs a representation the current production
   records cannot state truthfully;
7. cross-concern validation becomes unreviewable under one closed policy;
8. concrete usage shows the optional spatial record provides no value while bounded dual
   publication is acceptable;
9. Engine identity/applicability evidence changes the cost enough to challenge the selected Factory
   boundary.

Storage, topology, hierarchy, qualification and transport concerns are reopening triggers only when
a concrete requirement appears; a trigger reopens research and does not by itself admit
implementation work.
