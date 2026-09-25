# Factory Model Semantic Composition Research

> **Status:** CONCLUDED — see the maintained [research register](../research-register.md)
>
> **Scope:** How authored Factory semantics compose when not every design represents every behaviorally relevant concern: canonical structure, optional concerns, identity, validation and Engine applicability
>
> **Authority:** Research provenance only. The durable result lives in the [Factory semantic-evolution contract](../../architecture/factory-design.md#111-semantic-evolution), the [Factory model](../../architecture/factory-model.md) and the [Determinism Contract](../../architecture/overview.md#determinism-contract); this brief decides nothing.

## Question

> How should Arcogine represent independently applicable authored Factory semantics so that adding a
> new concern does not automatically turn feature accumulation into a linear whole-model
> `factory-model:vN` progression?

## Conclusion

The question was high-risk and received an independent adversarial review with disposition
**ACCEPT WITH QUALIFICATIONS**. The composition result is a bounded choice for current evidence, not a
universal theorem:

- the Factory canonical form is one closed, complete grammar, and that grammar may admit explicitly
  present optional authored records; absence asserts nothing in that dimension and is never
  synthesized, and a present legal zero is authored content distinct from absence;
- one aggregate `ModelFingerprint` per published design remains the identity model; an exact
  immutable reference to an independently governed technical contract is available when a concrete
  semantic dependency requires one;
- the spatial record keeps the five current spatial/handling facts together as a deliberately coarse
  optional record;
- mandatory whole-model bundles were not disproven, and more open structures — concern envelopes,
  per-concern identities, profiles as content identity, generic migration machinery — were not
  adopted because no current requirement justified their extra framework, not because they cannot
  work.

When it was reconciled, this result was expressed as a successor policy: an unreleased
`factory-model:v2` beside an unchanged `factory-model:v1`, with cross-policy comparison and continued
V1 publication as open support questions. That framing rested on treating V1 as already fixed. The
2026-09-25 owner-directed [provisional semantic-contract
reset](../../history/decisions/2026-09-25-provisional-semantic-contract-reset.md) replaced the policy
pair with one work-in-progress Factory model carrying the same optional spatial record, so the
successor-policy, cross-policy comparison and continued-V1-publication consequences no longer apply.
The composition rule itself is unchanged by the reset.

Durable destinations:

- [Factory semantic-evolution contract](../../architecture/factory-design.md#111-semantic-evolution) —
  composition, reference, comparison, applicability and support rules;
- [Factory model](../../architecture/factory-model.md) — the spatial record's presence and coverage
  semantics, predicates and canonical grammar;
- [Determinism Contract](../../architecture/overview.md#determinism-contract) — publication validity
  never establishes Engine executability;
- the Factory module's spatial-record validation and canonical-form tests — absence, present legal
  zero, complete coverage, and every spatial predicate;
- the non-normative
  [decision rationale](../../history/decisions/2026-09-23-factory-model-semantic-composition.md) —
  why this boundary was chosen over the serious alternatives at the time.

## What remains open

- **Spatial execution.** The current Engine executes production records only and refuses a present
  spatial record; making it executable is delivery in
  [Spatial Runtime Consequences](../../planning/spatial-runtime-consequences.md), not research.
- **Transport-participant ownership and resource-dependent performance** remain with their own
  register questions; this result neither resolves them nor prebuilds representation for them.

## Reopening triggers

Reopen the composition choice — not the evolution and support rules it consumes — when:

1. a current consumer requires complete-fingerprint equality when semantically unchanged content is
   republished;
2. concern admissions become frequent enough that corrections to one closed grammar and their
   comparison seams dominate cost;
3. some represented Factory semantic concern must be safely ignorable by a verifier;
4. an independently governed technical contract must be referenced as such;
5. geometry must be authored without handling magnitudes;
6. resource-dependent performance or qualification needs a representation the current production
   records cannot state truthfully;
7. cross-concern validation becomes unreviewable under one closed grammar;
8. concrete usage shows the optional spatial record provides no value;
9. Engine execution evidence changes the cost enough to challenge the selected Factory boundary.

Storage, topology, hierarchy, qualification and transport concerns are reopening triggers only when
a concrete requirement appears; a trigger reopens research and does not by itself admit
implementation work.
