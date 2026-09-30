# Simulation analytics cross-adapter compatibility

> **Status:** CANDIDATE
> **Risk:** **High** when promoted because it would affect supported consumer-facing semantics
> **Scope:** Semantic compatibility of one admitted analytics capability across two or more concrete supported consumption boundaries
> **Authority:** Research framing only; Arcogine currently has no required HTTP/SSE/CLI analytics surface and this brief creates none.

## Promotion trigger

Promote only when:

1. the [ownership investigation](simulation-analytics-consumer-boundary.md) has established a
   consumer-neutral analytics responsibility;
2. the applicable [evidence/provenance contract](simulation-analytics-evidence-provenance.md) has
   been settled for the analytical results in scope; and
3. Arcogine has a concrete second supported consumption boundary whose representation/API semantics
   must coexist with the first.

A hypothetical future HTTP/SSE adapter is not sufficient.

## Research question

> For a concrete pair of supported consumption boundaries, what compatibility contract ensures that both expose the same admitted analytics semantics without independently reimplementing analytical formulas?

## Decision at stake

Determine how one owned analytical definition is consumed through multiple supported boundaries while
keeping transport/representation concerns separate from analytical meaning.

This question does not choose whether the second boundary should exist. It starts only after that
product/architecture decision is concrete.

## Candidate models

Compare at least:

1. **Single analytics owner, multiple projections.** Both adapters consume one transport-neutral
   analytical implementation/result contract; each owns only representation.
2. **Shared supported analytics library/client boundary.** Both adapters execute the same owned
   definitions through one supported package contract.
3. **Adapter-local formulas.** Each adapter computes nominally equivalent metrics independently.
   Treat this as a failure-prone candidate that must prove semantic equivalence and change
   coordination rather than assuming duplicated formulas are harmless.

Do not infer that a network service, Java SDK, or serialized result DTO is required.

## Invariants

- Adapter representation never becomes the authority for metric meaning.
- A value with the same display name is not compatible if its analytical definition, interval,
  completeness rule, or producing basis differs.
- Engine facts remain Engine-owned through every adapter.
- Analytics does not regain scheduler/private runtime access through a convenient adapter.
- Representation-specific rounding, omission, pagination, or streaming behavior must not silently
  change admitted analytical meaning.
- Transport compatibility does not establish cross-revision analytical comparability.

## Proving and failure cases

Use the actual supported boundaries selected by the promotion trigger. At minimum test:

1. The same run/evidence basis consumed through both boundaries yields semantically equivalent
   analytical results under one declared analytical definition.
2. Partial/late evidence produces the same refusal or qualification semantics.
3. Provenance and evidence-window metadata survive each representation sufficiently for the result's
   contract.
4. Numeric representation/rounding does not create an undeclared semantic difference.
5. An analytical-definition change cannot update one supported boundary while silently leaving the
   other on an old formula.
6. Adapter-specific failure/retry behavior does not duplicate or reorder analytical evidence in a way
   that changes the result without being detected.

## Exit criteria

Conclude with:

- the concrete consumption boundaries being compared;
- the semantic result/equality boundary they must share;
- the single ownership point for analytical definitions;
- representation-specific freedoms and prohibitions;
- compatibility/versioning behavior when an analytical definition changes;
- proving evidence against duplicated-formula drift; and
- independent adversarial review before any public/support contract is reconciled.

## Destination and exclusions

Reconcile only the supported adapter/analytics compatibility contract justified by the concrete
boundaries. Do not build a speculative HTTP/SSE surface, choose a universal wire format, or reopen the
Engine-versus-analytics ownership decision.
