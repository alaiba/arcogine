# Factory Model Semantic Composition Research

> **Status:** READY — decided, with its optional-record rules not implemented; see the maintained [research register](../research-register.md)
>
> **Scope:** How authored Factory semantics compose when not every design represents every behaviorally relevant concern: canonical structure, optional concerns, identity, validation and Engine applicability
>
> **Authority:** Research only. The implemented part of the result lives in the [Factory semantic-evolution contract](../../architecture/factory-design.md#111-semantic-evolution) and the [Factory model](../../architecture/factory-model.md); the rules no code implements are kept [below](#decided-rules-not-implemented). This brief decides nothing.

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
- the spatial record keeps the five spatial/handling facts together as a deliberately coarse
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

The spatial record was the only optional record, and no Engine executed it. It was deleted from the
code, and its specification moved to the [transfer research](transfer-semantics.md#authored-spatial-record),
when architecture became a description of implemented behavior. The question is `READY` again:
the closed grammar and its one aggregate fingerprint are implemented, while the optional-record and
executability rules below have no implementation. It concludes when an implementation handoff lands
the first optional record together with these rules.

Destinations:

- [Factory semantic-evolution contract](../../architecture/factory-design.md#111-semantic-evolution) —
  the implemented composition, reference, comparison and support rules;
- [Factory model](../../architecture/factory-model.md) — the closed production-record grammar and its
  one aggregate fingerprint;
- [Decided rules not implemented](#decided-rules-not-implemented) — the optional-record and
  executability rules, until a first optional record lands them;
- the non-normative
  [decision rationale](../../history/decisions/2026-09-23-factory-model-semantic-composition.md) —
  why this boundary was chosen over the serious alternatives at the time.

## Decided rules not implemented

These rules were part of the [Factory semantic-evolution contract](../../architecture/factory-design.md#111-semantic-evolution)
while the spatial record existed in code. They return there with the first optional record.

**Optional authored records.** The definition may admit explicitly present optional authored
records beside its required ones, and the presence of each is canonical content. An absent record
means the design makes no assertion in that semantic dimension; nothing is synthesized for it from
a default. A present record carries authored values, and a legal zero or default-like value is an
authored value distinct from absence in both meaning and canonical bytes. Unknown or partly
authored facts are draft or adapter state, never published content; the definition states what makes
a present record complete and rejects anything else. Which combinations of records are valid is
decided by the definition's own closed predicate, not by a separate combination registry. A design
without an optional record has no facts in that dimension and therefore no behavior from it — the
truthful execution of a design that never authored those semantics, not a degraded mode.

**Publication validity is not Engine executability.** A valid published model is executable only
when the Engine interpretation's own definition supports the records, values, variants and
interactions it represents; recognizing which records are present is not sufficient. An Engine
refuses a model outside that domain before runtime mutation rather than ignoring represented content
or supplying absent content. The decided [transfer applicability boundary](transfer-semantics.md#applicability-boundary)
selects no transfer for a model without spatial content and distinguishes it from authored zero and
from refusal. That selection is specific to the spatial grammar, not a general rule that every future
transfer lifecycle must be conditioned on authored duration inputs.

## What remains open

- **The first optional record.** The decided spatial record and its transfer lifecycle are kept in
  the [transfer research](transfer-semantics.md#decided-design--not-implemented); landing them, or
  any other optional record, lands the rules above with it.
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
