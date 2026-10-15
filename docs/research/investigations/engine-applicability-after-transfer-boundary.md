# Engine applicability after transfer-boundary reconciliation

> **Status:** SUPERSEDED — see the maintained [research register](../research-register.md)
>
> **Risk:** High — whole-definition identity, refusal, deterministic results, and historical attribution
>
> **Authority:** Research brief only; current Factory and Engine specifications are authoritative

## Question as admitted

Under the then-adopted [transfer-applicability boundary](transfer-semantics.md#applicability-boundary),
the brief asked which Engine semantics identity or identities may execute each exact
`factory-model:v2` represented-content case — V1, V2 with the spatial record absent, present with
zero timing, present with positive timing — and whether the then-fixed `engine-semantics:v1`
definition could admit those cases without changing its whole definition. It was `READY` but never
investigated.

## Why it is superseded

The question existed because the Factory V1/V2 policies and `engine-semantics:v1` were treated as
fixed identities whose definitions could not change in place. The 2026-09-25 owner-directed
[provisional semantic-contract reset](../../history/decisions/2026-09-25-provisional-semantic-contract-reset.md)
replaced that estate: one work-in-progress Factory model replaced the policy pair, and the Engine
interpretation is a mutable current development definition whose status is not encoded as an Engine
identity. There is no longer a fixed Engine definition whose admission domain must be preserved, no
V1/V2 identity partition to allocate, and no publication-release decision gated on the answer. No investigation or review was
performed, and nothing here was accepted as research.

## What survives, and where

The brief's behavioral proving dimensions remain valid independent of identity and now live in
current contracts and tests, or in the decided spatial design that no code implements yet:

| Proving dimension | Current home |
| --- | --- |
| Model refusal before runtime mutation, ahead of same-resource or distinct-resource runtime cases | Decided [applicability boundary](transfer-semantics.md#applicability-boundary) and its refusal fixture in the [decided Engine fixtures](transfer-semantics.md#required-engine-conformance-fixtures) |
| Present authored zero versus absence, including events, state, ordering and bounded advancement | Decided [applicability boundary](transfer-semantics.md#applicability-boundary) proving case and [decided Engine fixtures](transfer-semantics.md#required-engine-conformance-fixtures) |
| Factory-invalid content and valid but Engine-unsupported content as distinct failures | `FactoryModelValidationException` and its tests; Engine-unsupported refusal is part of the [decided design](transfer-semantics.md#decided-design--not-implemented) |
| Truthful Factory fingerprint and explicit treatment of any Engine-definition provenance a consumer actually requires | [Runtime contract](../../architecture/runtime-contract.md) |
| Same-resource and distinct-resource continuation, binding, reservation, arrival and recovery | Decided [Engine transfer interpretation](transfer-semantics.md#engine-transfer-interpretation) |
| A future optional Factory concern irrelevant to one interpretation needs explicit support, not wildcard acceptance | Decided [composition rules](factory-model-semantic-composition.md#decided-rules-not-implemented) ("Publication validity is not Engine executability") |

The identity-specific dimensions — V1 publication versus retained V1 execution, present-only versus
both-form support partitions, and the whole-definition test against fixed `engine-semantics:v1` — are
discarded with the estate they concerned.
