# Transfer applicability for optional spatial models

> **Date:** 2026-09-24
>
> **Authority:** Historical, non-normative evidence; current meaning lives only in the documents it was reconciled into
>
> **Reconciled into:** [Factory/Engine Transfer Applicability](../../architecture/transfer-applicability.md) and [Factory semantic evolution](../../architecture/factory-design.md#111-semantic-evolution)

## Decision

For the currently represented `factory-model:v2` cases, an Engine that admits a spatial-absent artifact is to interpret distinct-resource continuation without a transfer lifecycle. A present complete spatial record can support a full transfer lifecycle even when its derived duration is zero. Exact Engine identities and support scopes were left for a separate investigation.

## Context

The unreleased V2 grammar couples geometry and handling magnitudes in one optional record. Its absent form truthfully represents a production-only design, but earlier Engine-applicability research had inherited the absent-case behavior as a premise. The Engine has a fixed attributed v1 definition and current V1 production execution has no transfer phase. V2 publication and spatial runtime activation were held before this choice could become a released assumption.

## Serious alternatives

- **Engine-defined zero-duration lifecycle on absent content:** coherent without inventing authored Factory facts. It would add binding, reservation, events, state and command-interleaving consequences to production-only runs for which no current consumer need was found. It was set aside, not falsified.
- **Refuse V2-absent artifacts under a given Engine identity:** coherent as an exact support predicate. It preserves a smaller execution domain but does not answer how new production-only designs should be executed if that support is chosen or owed. A V1 publication transition and retained V1 execution are separate matters. Refusal remains an open support option, not the selected absence meaning for an admitted run.
- **Add an authored non-spatial hand-off assertion or timing record:** could make the assertion explicit, including an Engine-defined zero interval or authored positive interval. It changes the Factory grammar and still needs an absence meaning. No current consumer justified it, so it was deferred rather than declared redundant.

## Decisive rationale

The selected meaning preserves current V1 production behavior for the comparable V2-absent case, avoids an unrequested command-sensitive runtime phase, and requires no new Factory grammar. This was a medium-confidence trade-off among viable designs, not a deduction from the rule against synthesizing authored facts. The complete V2 spatial record is currently the only represented transfer-timing carrier, so a rule stated against that record and one stated against its timing inputs select the same artifacts today. That coincidence does not establish a universal ownership rule for future grammars.

## Consequences and accepted trade-offs

The current absent case has a defined no-transfer target if admitted, while exact Engine identity, artifact support and publication support remain open. The choice does not give consumers a uniform transfer event vocabulary for production-only runs. It also does not decide whether a future geometry-only record, explicit hand-off assertion or positive non-spatial interval should trigger transfer. The corrected bounded-step witness and its limits live in the canonical transfer boundary, where they can guide the later Engine investigation.

## Reconsider when

A concrete consumer needs hand-off commitment without authored spatial facts, needs to author hand-off without geometry or timing, or needs positive non-spatial timing; a future Factory grammar separates geometry from timing; or exact Engine support research finds an owed production-only execution contract the current support options cannot satisfy.
