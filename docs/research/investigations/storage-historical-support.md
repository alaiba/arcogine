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

## Current-contract baseline

Start from a provider that conforms to the current [Storage](../../architecture/storage.md) and
[controlled-revision](../../architecture/controlled-revisions.md) contracts. A departure from an
already-owned guarantee (acceptance, immutable ID binding, exact-definition refusal, or faithful
resolution of an accepted record) is a correctness defect fixed independently of this question. It is
neither evidence for a stronger claim nor a reason to redefine the baseline, and it does not by itself
block this brief. Concretely, the baseline already requires:

- accepted recorder source and subject resolve exactly as accepted after reopen, and valid Unicode,
  supplementary characters included, survives unchanged;
- text the private UTF-8 record format cannot represent, such as an unpaired surrogate, is refused
  before anything is persisted rather than silently substituted;
- distinct opaque definition bindings never collapse to the same stored marker, so a root created
  under one is refused under any other;
- a repeated or rebound controlled-revision ID is still rejected, and an accepted revision's
  fingerprint, lineage and provenance never change.

These examples add no historical-reader, cross-definition compatibility, migration or
provider-substitution guarantee; stronger resolvability is what this question investigates.

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
