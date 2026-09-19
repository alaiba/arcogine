# ADR-0018: Factory semantics after the support reset

Status: Accepted
Date: 2026-09-19
Supersedes: ADR-0014

## Context

[ADR-0017](0017-ground-zero-semantic-evolution.md) records the owner's complete-estate
declaration, reset boundary, and general identity/support rules. Its withdrawal of
pre-reset support replaces ADR-0014's permanent decoder retention, fully supported
V1 execution, automatic V2 introduction, and permanent V1/V2 coexistence. This
changes ADR-0014's applicability and requires supersession. It supplies no evidence
for reopening Factory ownership or inventing historical design facts.

## Decision

### 1. Preserve authored semantics without promoting a policy

The canonical Factory model remains the source of authored production-system facts
under ADR-0003 and ADR-0004. ADR-0017 carries forward ADR-0006's policy-level
canonicalization invariants. Neither V1 nor V2 is a supported post-reset policy
merely because its implementation or specification remains in the repository.

The existing V2 authored-field and validation decisions survive as the current
spatial design constraints. Their owning specification is
[Factory Model v2](../factory-model-v2.md), particularly its authored-facts section,
canonical grammar and publication predicates. This carries forward ADR-0014
decisions 3–6: mandatory floor extent, anchored placement and footprint, authored
handling magnitudes, containment/non-overlap/representability, and the exclusions
of orientation and transport topology. Retaining those constraints does not select
the composition or version shape of a future Factory contract.

Authored facts belong to Factory; result-affecting interpretation belongs to Engine
under ADR-0015. Distance metric, rounding, destination binding, reservation and
transfer lifecycle are not authored Factory fingerprint fields. A footprint remains
an authored validation fact even when an Engine interpretation does not use it for
distance. Changing interpretation alone does not re-fingerprint the same design.

### 2. Preserve historical identity and explicit evolution

Existing identities and recorded bindings never acquire changed meaning.
Fingerprint-policy identity and controlled-revision identity remain distinct. A
controlled revision references exactly one fingerprint; lineage may cross policies
when an explicit owning transition permits it, without rewriting either artifact.

There is no automatic lift from V1 to V2 or another successor. New spatial facts
must be explicitly authored and published; position, footprint and handling
defaults must not be invented as historical truth about a non-spatial artifact.

Cross-policy comparison is explicit. Before an actual cross-policy controlled
transition, provide resolution/verifiers for the policies that transition supports,
and either an explicit migration classification or an explicitly chosen common
semantic representation for fine-grained comparison that claims equivalence.
Implement only the seam the real transition requires, not a generic migration
framework. No pre-reset V1-to-V2 transition is required by the reset.

A changed identity-defining contract requires a distinguishable identity under
ADR-0017; old fingerprints and their definitions are never rewritten. This preserves
ADR-0014 decisions 10–12's no-lift, explicit comparison and non-rebinding invariants
without mandating chronological whole-model `vN+1` composition or perpetual support.
Support/definition retention follows ADR-0017 and the obligations of accepted use,
including ADR-0008's exact historical-resolution requirement. It is not optional
for a use that has already been accepted.

### 3. Withdraw specific inherited commitments

ADR-0014 decisions 2, 8, 9 and the permanent-support/linear-progression parts of 12
no longer require release of V2, eternal V1/V2 decoders, continued V1 execution, or
V1/V2 coexistence. The old grammars remain inspectable definitions, not promises
that post-reset implementations will serve them. Reusing a pre-reset label for a
changed grammar or field contract is forbidden, even before a new release.

Factory composition remains the separate
[Factory semantic-composition investigation](../../research/investigations/factory-model-semantic-composition.md).
Neither this decision nor the reset picks aspects, profiles, component identities,
or another schema. The unimplemented canonical-identity/coexistence plan must be
reconciled with that result before execution; obsolete pre-reset coexistence has no
automatic right to resume. Code cleanup is a later bounded slice.

## Alternatives and consequences

Keeping V1 supported solely to avoid superseding ADR-0006 would contradict the
owner's reset decision. Preserving V2 as the new default solely because its grammar
exists would decide the still-open Factory composition question without evidence.
Discarding its authored semantics as a side effect would confuse support withdrawal
with reopening accepted design constraints. This decision avoids all three.

The V2 specification retains its byte definition and reusable validation cases.
It records its withdrawn support status explicitly. Existing code remains useful
implementation evidence; its presence is not support admission. Future Factory
work must preserve truthful authored identity, explicit transitions and domain
ownership while selecting only the contract its real consumers need.

## Charter alignment

The decision keeps authored production-system meaning continuous across consumers
and preserves historical provenance without requiring an unused compatibility
estate or choosing Factory's future composition prematurely.
