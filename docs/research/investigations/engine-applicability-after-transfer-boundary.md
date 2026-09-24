# Engine applicability after transfer-boundary reconciliation

> **Status:** READY — see the maintained [research register](../research-register.md)
>
> **Priority:** Critical-path
>
> **Risk:** High — whole-definition identity, refusal, deterministic results, and historical attribution
>
> **Authority:** Research brief only; current Factory and Engine specifications remain authoritative

## Question and decision at stake

Under the adopted [transfer-applicability boundary](../../architecture/transfer-applicability.md), **which Engine semantics identity or identities may execute each exact `factory-model:v2` represented-content case, and can any existing fixed Engine definition admit those cases without changing its whole definition?**

The decision determines truthful execution support and result attribution for the still-unreleased V2 policy. Factory publication validity does not establish Engine applicability. The answer may alter the Factory/Engine specification and the held [spatial-runtime plan](../../planning/spatial-runtime-consequences.md); this brief admits no implementation.

## Why this is READY

The separate [transfer-lifecycle question](transfer-semantics.md#concluded--transfer-lifecycle-independence) has been reconciled for current V2 content. The [superseded predecessor](engine-applicability-optional-record-factory-policies.md) investigated Engine identity under an assumed no-transfer absent case and produced substantial reviewed evidence. Its candidate set and both-form recommendation are methods to challenge, not an adopted answer. Current [Factory V2](../../architecture/factory-model-v2.md) remains an unreleased optional-record grammar; [Engine v1](../../architecture/engine-semantics-v1.md) remains fixed, with spatial execution and provenance propagation incomplete.

The landed transfer boundary supplies these inputs to this **separate** investigation:

- for an admitted V2-absent artifact, distinct-resource continuation has no transfer under the selected current design;
- admitted V2-present artifacts can have a full zero-duration or positive-duration transfer lifecycle under an applicable interpretation;
- Factory-valid artifacts outside an Engine's support domain are refused before mutation, including same-resource-only artifacts when its predicate excludes their content;
- the V2 grammar needs no lifecycle-driven correction on current evidence; and
- V1 publication support, execution support for retained V1 artifacts, and new V2 execution support are separate decisions.

V2 publication release and spatial-runtime activation remain blocked until this question concludes and its consequences are reconciled. Research priority does not itself admit those slices.

## Scope for the future investigation

Derive candidates from the **landed transfer contract** before applying predecessor reasoning. Compare preservation of any existing fixed Engine definition with distinguishable interpretation and, where case meanings warrant it, an explicit support partition. Do not assume a new identifier already exists, one identity covers both V2 forms, v1 does or does not support the present form, or Factory and Engine version numbers pair one-to-one. A still-supported V1-only state is a valid interim refusal boundary, not a full answer to V2 applicability. If a candidate refuses V2-absent, apply its exact artifact predicate before examining same-resource or distinct-resource runtime cases; a runtime outcome cannot rescue an excluded artifact.

Apply the whole-definition test to accepted policy/content, referenced definitions, result-affecting rules, output/events, refusals, and provenance, including historical records and unexercised rules. Distinguish Factory-invalid data, Factory-valid but Engine-unsupported data, and unsupported Engine identity. Preserve actual Factory fingerprints and conditional controlled-revision provenance; a representation projection must not relabel the source. An absence of failing fixtures is not affirmative preservation proof. A new implementation of already-defined behavior need not change semantic identity, but implementation feasibility does not prove admission under a fixed definition.

## Proving dimensions to derive into cases

- Factory V1 under Engine v1 and retained historical v1 results;
- V2 spatial-present and V2 spatial-absent under their **reconciled** meanings;
- artifact admission/refusal before same-resource control cases, including same-resource-only V2-absent artifacts and multi-eligible routes whose eventual destination is same-resource;
- present authored zero versus absence, including events, state, ordering, and bounded advancement;
- same-resource and distinct-resource continuation, binding, reservation, arrival, and recovery;
- Factory-invalid content, valid but Engine-unsupported policy/content, and unsupported Engine identity;
- truthful Factory fingerprint and selected Engine attribution on accepted results, reset, observations, and events;
- present-only versus both-form support, if their meanings justify partitioning;
- separate V1 new-publication, retained-V1 execution, and V2-absent execution support declarations; and
- a future optional Factory concern irrelevant to one claimed interpretation, testing explicit support rather than wildcard acceptance.

Evidence should include the exact current definition/dependency inventory, accepted-input and refusal matrix for every candidate, historical-attribution and support obligations, deterministic proving/failure fixtures, and an explicit falsification of unsupported preservation claims. The [retained predecessor evidence](engine-applicability-optional-record-factory-policies.md) supplies methods and cases, not a default answer. Independent adversarial review is required before any Engine architecture/specification reconciliation. Publication and runtime support require their own later executable evidence; research acceptance alone does not release them.
