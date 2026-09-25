# Closed Factory policies with optional authored records

> **Date:** 2026-09-23
>
> **Authority:** Historical, non-normative evidence; current meaning lives only in the documents it was reconciled into
>
> **Reconciled into:** [Factory semantic-evolution contract](../../architecture/factory-design.md#111-semantic-evolution), Factory Model v2 (the specification is now the [Factory model](../../architecture/factory-model.md)), [Determinism Contract](../../architecture/overview.md#determinism-contract)

## Decision

Arcogine chose to evolve Factory fingerprint policies as closed, complete, immutable grammars that
may admit explicitly present optional authored records, keeping one aggregate `ModelFingerprint`
per published design. The unreleased `factory-model:v2` was corrected before any attribution from
"V1 plus five mandatory spatial facts" into the first such policy: the unchanged V1 production
records plus one optional spatial record keeping floor extent, placement, footprint,
`ticksPerCell` and `handlingTicks` together.

## Context

- `factory-model:v1` was attributed and immutable; the V2 draft had never produced or recorded a
  fingerprint, so its grammar could still be corrected in place.
- The draft made the spatial facts mandatory, so a production-only design either stayed on V1 or
  would have had to invent a floor and zero handling, and each later orthogonal concern threatened
  to become another cumulative whole-model generation.
- Only two concern sets had standing: production-only designs in current use, and production plus
  all five spatial facts for specified Engine transfer semantics and a planned game loop. Storage,
  topology, qualification, hierarchy and transport were constructed test cases, not demand.
- The concluded semantic-evolution rules already fixed a whole definition at first attribution,
  forbade rebinding and separated support from meaning.
- Engine Semantics v1 already made absence and authored zero result-affecting: a spatial design
  with zero handling magnitudes still produces transfer events, while a design without spatial facts
  produces none.

## Serious alternatives

- **Mandatory whole-model bundles** — keep the draft and publish production-only designs under V1.
  Viable and not disproven, with the lowest immediate adoption cost for the two current concern
  sets. Not selected because one closed policy represents both sets truthfully without making
  spatial facts the price of the newer policy, whereas cumulative bundles invite every future
  orthogonal concern to become another generation. An early claim that two publication policies
  would be a permanent burden did not survive review: supporting both is a scoped support choice.
- **An open concern envelope** whose admitted concern definitions grow without a new envelope
  identity. Coherent under the non-rebinding rules, and able both to restructure production and to
  carry predeclared internal variants. Not selected because no current requirement justified its
  extra framework — per-concern definition identities and support, an allocation authority for
  them, and ownership of cross-concern rules — not because it cannot work. An early claim that
  openness pays off only through verifier-ignorable concerns or stable republication fingerprints
  was rejected as too strong.
- **Independently identified components or per-concern fingerprints.** No current statement
  needed a second identity; where a design must depend on an independently governed technical
  contract, an exact reference inside the one aggregate preserves that statement.
- **Profiles as identity.** Named supported combinations are useful as support scopes but add
  nothing to content identity, and none was needed.

## Decisive rationale

- Mandatory bundling was a representation choice, not a consequence of whole-policy identity;
  separating the two axes let a closed policy obtain the needed representational composition
  without an open registry.
- Within a closed grammar, positional optional fields and fixed tags are the same semantic model;
  what distinguished the candidates was the admission rule, not the encoding.
- Under whole-definition fixation, admitting a new record needs a distinguishable closed policy;
  that costs one explicit transition when a real transition occurs, and current evidence did not
  show such transitions to be frequent.
- Complete-fingerprint equality for unchanged content republished after a new concern is admitted
  was the benefit most clearly unique to openness, and no inspected consumer relied on it.

## Consequences and accepted trade-offs

- A later identity-defining grammar change needs a new policy identity; equal production content
  republished under it gets a new complete fingerprint, and comparison across policies needs an
  explicit, non-inventive, distinction-preserving representation.
- The five-fact spatial record is deliberately coarse: geometry without handling magnitudes, or
  unknown values, cannot be published under this policy and remain draft state.
- Whether production-only designs keep being published under `factory-model:v1` after V2 is
  released was left a scoped support decision rather than made an architectural rule.
- The investigation's report claimed the `SpatialConfiguredResource` wrapper duplicated
  base-resource authority. It did not: it composed exactly one `ConfiguredResource`. The wrapper
  was replaced by per-resource layouts keyed by resource identifier because an optional record held
  apart from the production records must reference resources rather than embed them; that keyed
  table was one coherent implementation shape, not an architectural necessity.
- Engine applicability was deliberately left open. The Factory decision did not authorize
  re-pointing or amending `engine-semantics:v1`; whether it applies to the corrected policy or a
  distinguishable Engine identity is needed became a separate research question gating V2
  publication and spatial runtime activation.

## Reconsider when

- a current consumer needs complete-fingerprint equality when semantically unchanged content is
  republished, or concern admissions become frequent enough that closed-policy transitions dominate
  cost;
- a represented concern must be safely ignorable by a verifier, or an independently governed
  technical contract must be referenced as such;
- geometry must be authored without handling magnitudes, or resource-dependent performance or
  qualification cannot be stated truthfully with the current production records;
- a concrete consumer needs storage, topology, hierarchy or transport semantics, testing whether
  closed policies with optional records still scale to the concerns actually admitted;
- cross-concern validation becomes unreviewable under one closed policy;
- concrete usage shows the optional record adds no value while bounded dual publication is
  acceptable;
- the Engine applicability answer changes the relative cost of this Factory boundary.

## Provenance

Reconciled in pull request #387, which also carried the investigation's knowledge-transfer audit
and its independent PR review. The high-risk investigation's report
and its independent adversarial review (`ACCEPT WITH QUALIFICATIONS`) were temporary research
evidence and were not retained; the reconciled documents above, the `factory-model:v2` proving-slice
tests, the [concluded investigation brief](../../research/investigations/factory-model-semantic-composition.md)
and its reopening triggers are the durable destinations.
