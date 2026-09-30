# Simulation analytics cross-revision comparability

> **Status:** CANDIDATE
> **Risk:** **High** — touches analytical meaning, Engine behavioral equivalence, exact provenance, and historical interpretation
> **Scope:** When and how analytical results produced under different Engine interpretations or different development revisions may be compared without silently treating differing bases as the same measurement
> **Authority:** Research framing only; this brief defines no Engine identity, equivalence relation, migration, or support promise.

## Promotion trigger

Promote for a concrete use that needs to compare analytical or Engine-supported performance results
across different Engine interpretations, implementations, or development revisions.

Examples include assessing an Engine upgrade, comparing a historical experiment with a current run,
or determining whether an observed metric delta reflects changed workload/design or changed
interpretation.

## Research question

> Under what conditions may analytical results produced under different Engine interpretations or different development revisions of an interpretation be compared, and what provenance, compatibility qualification, recalculation, or explicit refusal is required so differences in basis are never silently treated as differences in the measured phenomenon?

Cross-revision comparison is not prohibited. **Silent direct comparability is.**

## Decision at stake

Define the comparison behavior for one concrete cross-revision use:

- directly comparable;
- comparable under a narrower declared projection;
- comparable only after recalculation/conversion;
- suitable only for qualified side-by-side description; or
- unsupported because the required basis cannot be established.

## Ownership guard

This question owns **analytical comparability and comparison/refusal behavior**. It does not invent
the prerequisite semantic identities or relations.

When the concrete use requires them, route to the existing
[semantic-equivalence and reference questions](semantic-equivalence-and-reference-boundaries.md):

- **Engine behavioral equivalence** owns whether two interpretations/implementations are behaviorally
  equivalent over the relevant input and observation projection.
- **Exact reference boundaries** owns whether the producing Engine basis must be explicitly
  resolvable and what reference can truthfully identify it.
- **Conversion and migration semantics** owns any transformation/recalculation of earlier material
  whose meaning or distinctions may change.

This question consumes those conclusions for the comparison use; it must not duplicate them or
silently settle them.

## Candidate comparison outcomes

Evaluate at least:

1. **Direct comparison.** Same analytical definition, compatible evidence basis, and a relevant
   Engine-behavior basis established as equivalent for the declared comparison projection.
2. **Projection-scoped comparison.** Engine interpretations differ, but evidence establishes that
   the differences are irrelevant to one explicitly bounded analytical projection.
3. **Recalculated comparison.** Retained supported evidence allows both results to be recomputed under
   one analytical definition and compatible basis.
4. **Qualified juxtaposition.** Both results may be shown, but a numeric delta/ordering would imply
   comparability that has not been established.
5. **Refusal.** Required producing definition, evidence, or analytical meaning cannot be resolved
   well enough to support the requested comparison.

No candidate is a universal rule for every cross-revision use.

## Required comparison basis

For the concrete use, determine which of these actually matter:

- Factory/model content and relevant explicit inputs;
- run/result and evidence-window identity;
- analytical definition and version/reference;
- Engine interpretation/implementation basis;
- applicable behavioral-equivalence projection;
- evidence completeness;
- conversion/recalculation provenance and declared losses;
- support horizon, if the use requires future repeatability rather than one-time analysis.

A `ModelFingerprint` alone does not establish Engine comparability. A build identifier alone does
not prove behavioral difference or equivalence. The current absence of a dedicated Engine-definition
identifier must remain visible rather than being patched locally with a placeholder token.

If consumer-neutral analytics exists, consume its
[evidence/provenance contract](simulation-analytics-evidence-provenance.md). Engine-owned supported
performance results may still create a cross-revision comparison use even if no separate analytics
capability is ultimately adopted.

## Proving and failure cases

At minimum for the selected use:

1. Same analytical definition and workload, behavior-preserving implementation change.
2. Same displayed metric name but changed Engine behavior affecting the measurement.
3. Same Engine behavior for the selected projection but unrelated changed behavior elsewhere.
4. Changed analytical definition over otherwise identical retained evidence.
5. Old result with sufficient retained evidence to recalculate under the new analytical definition.
6. Old result whose producing Engine basis or required evidence is unavailable.
7. A numeric result pair that may be shown descriptively but for which a delta would be misleading.
8. A rollback or repeated model fingerprint that must not collapse distinct producing occurrences or
   assumed Engine bases.

## Exit criteria

Conclude only with:

- the concrete cross-revision comparison use and projection;
- the admissible comparison outcomes and their proof obligations;
- required analytical and producing provenance;
- when behavioral-equivalence/exact-reference/conversion results are prerequisites;
- explicit qualification/refusal behavior when comparability is not established;
- treatment of recalculated values versus originally recorded values;
- no silent fallback from missing basis to "same metric name means comparable"; and
- independent adversarial review before any architecture/support reconciliation.

## Destination and exclusions

A surviving comparison rule belongs in the owning analytics/consumer contract, while prerequisite
Engine equivalence, exact-reference, and conversion consequences stay with their existing owning
research/architecture surfaces.

Do not introduce a universal Engine version ladder, registry, compatibility matrix, migration
framework, or permanent historical-execution promise merely to make comparison convenient.
