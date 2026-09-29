# Historical storage support across evolving definitions

Status: READY brief; no conclusion

## Question and decision

For a concrete retained historical use, what minimum basis must Arcogine preserve so an accepted
record remains truthfully resolvable as storage representation and domain definitions evolve
independently? This determines scoped historical support, not whether built-in Storage exists.
Start from the current definition-bound refusal. Coordinate with the existing
[exact-reference, scoped-commitment, and conversion questions](semantic-equivalence-and-reference-boundaries.md)
and the [Factory canonical-artifact-boundary question](factory-canonical-artifact-boundary.md)
rather than allocating a new universal definition identity or assuming the current public codec must
remain the persistence boundary.

## Scope and candidates

Compare (1) preservation and refusal with a retained compatible reader/build, (2) a scoped exact
definition package and reader, and (3) a separately declared conversion or migration that records
source and target meaning, preserved distinctions and losses. The current/simple candidate is
continued same-binding support with explicit refusal outside it. Do not presume a registry,
universal attestation, broad migration framework, or permanent re-execution promise.

## Discriminating cases and evidence

- A private storage format changes while Factory meaning does not. Determine whether the accepted
  semantic basis and occurrence provenance remain recoverable without making format identity semantic
  or presuming that today's public Factory canonical bytes must remain Storage's persistent representation.
- Factory meaning changes while the old bytes still parse under a new decoder. Determine how each
  candidate avoids falsely attributing current meaning to an old accepted occurrence.
- Factory bytes are preserved but the old verifier/definition is unavailable. Separate byte custody,
  exact interpretation, compatibility, and a support promise; identify what can still be answered.
- A conversion preserves some distinctions but loses another, or an `F1 -> F2 -> F1` lineage spans
  definitions. Determine whether the proposed history keeps occurrence identity, source basis and
  losses explicit.
- A new Governance use requires a fixed horizon or independently verifiable explanation. Test
  whether the candidate supplies every transitive basis that use needs.

Inspect actual storage and Factory verifier behavior, current semantic definitions, current
support declarations, and any concrete consuming use. A candidate fails if it infers equivalence
from equal fingerprints/build labels, silently rewrites old records, or claims interpretation
without the needed definition. Unknown retention needs remain a scoped gap rather than an invented
platform-wide promise.

## Exit, review and destination

Stop with a specific retained use and horizon, preserved and unavailable bases, a selected
resolution/refusal contract, conversion losses if any, evidence by case, and explicit exclusions.
This is high risk identity and history work; independent adversarial review precedes promotion.
Reconcile any accepted result into [Storage](../../architecture/storage.md), the owning semantic
specification and [semantic support policy](../../development/semantic-contract-support.md), then
admit only a bounded implementation slice. Reopen when a real historical reliance, upgrade, or
definition change exceeds the current build-bound scope.
