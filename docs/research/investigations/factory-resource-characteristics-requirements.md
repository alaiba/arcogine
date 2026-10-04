# Factory Resource Characteristics and Operation Requirements Research

> **Status:** READY  
> **Scope:** Decide which configured-resource characteristics, if any, belong in canonical Factory semantics, how operation requirements relate to them, and what the current privileged `capacityLiters` field should become  
> **Authority:** Research only; current Factory resource semantics remain authoritative until reconciliation

## Question

> Which configured-resource characteristics, if any, are canonical Factory facts, how should operation requirements relate to them, and what should happen to the current `ConfiguredResource.capacityLiters` field?

## Decision at stake

Whether Arcogine should:

- remove `capacityLiters` and continue to rely on explicitly authored eligible-resource IDs;
- retain a deliberately narrow volumetric fact with a precise invariant;
- introduce typed resource provisions/limits and typed operation requirements with explicit satisfaction semantics, while leaving operation-resource eligibility authored explicitly; or
- adopt another bounded representation justified by concrete Factory semantics.

The investigation must not assume that a generalized characteristic system is needed merely because one specialized field exists today.

## Current evidence boundary

Current repository behavior establishes:

- `ConfiguredResource.capacityLiters` is optional canonical Factory content;
- it participates in model identity, semantic comparison, runtime resource projection and observations;
- current Engine eligibility and processing do not use it;
- current operation applicability is represented by explicit eligible `MachineId` values; and
- [Factory Resource Semantics](../../architecture/factory-resource-semantics.md) deliberately keeps qualification separate from reusable equipment-type identity.

The single privileged volumetric property therefore needs a semantic justification independent of its implementation history.

## Candidate models

At minimum compare:

1. **Remove/defer.** No canonical resource-characteristic relation is needed yet; explicit eligible IDs remain sufficient.
2. **Narrow volumetric fact.** `capacityLiters` survives because a concrete Factory invariant specifically requires machine volume, without generalizing to arbitrary attributes.
3. **Typed provisions and requirements.** Resources expose explicitly typed provisions/limits and operations expose typed requirements with a defined satisfaction relation; explicit eligible IDs remain the authoritative applicability decision.
4. **Typed resource facts without canonical operation requirements.** Characteristics are canonical and inspectable, but operation requirement semantics remain outside the Factory model and explicit eligible IDs continue to carry applicability.
5. **Another bounded typed model** only if evidence establishes a different invariant.

An untyped generic property bag such as `Map<String,Object>` is not a default candidate: it would move semantic ownership into keys, conventions or consumers rather than resolving it.

## Scope

In scope:

- resource provisions, limits and characteristics that affect production meaning;
- corresponding operation/step requirements;
- units, dimensions and comparison rules;
- requirement-satisfaction semantics, while explicit eligible-resource IDs remain authoritative for applicability;
- canonicalization and semantic comparison consequences;
- interaction with Governance structural evidence;
- whether characteristics imply any operation-resource performance fact.

Out of scope:

- reusable equipment/specification identity unless a checkable cross-consumer dependency independently requires it;
- presentation/catalogue attributes such as cosmetic appearance or marketing copy;
- mutable runtime availability, queues or setup state;
- spatial placement/footprint except as a comparison case for already-owned typed Factory facts;
- deriving or verifying operation-resource eligibility from represented characteristics/requirements; that remains the separate [qualified operation-resource applicability](factory-design-evolution.md#qualified-operation-resource-applicability--candidate) research question until its promotion trigger is met;
- generic metadata extensibility.

## Proving cases

A decision-quality answer must cover at least:

1. **Volumetric requirement satisfaction.** A step requiring a minimum working volume and resources above/below that threshold, proving the comparison semantics without treating the result as an eligibility decision.
2. **Second independent dimension.** A non-volume characteristic such as tooling, dimensional envelope, material compatibility or temperature range, to test whether a volume-specific field generalizes truthfully.
3. **Irrelevant descriptive attribute.** A fact that should remain consumer/authoring metadata and must not become canonical merely because it can be attached to equipment.
4. **Explicit-eligibility control.** A case where represented requirement satisfaction and authored eligible IDs can be considered independently, so this investigation does not silently turn the former into derived or verified applicability.
5. **Unit safety.** Equivalent or incompatible units must not depend on string conventions or presentation labels.
6. **Identity boundary.** Two configured resources with equal characteristics must not acquire shared reusable-specification identity merely from equal values.
7. **Change semantics.** A material characteristic/requirement change must have an explainable canonical and comparison consequence if the candidate claims it is Factory truth.

## Evidence expectations

Use current Factory model, validation, semantic comparison, Governance evidence use, resource architecture and ISA-95 mapping as repository evidence. External standards/domain evidence is warranted where it distinguishes characteristics, requirements, capability/qualification and reusable specification identity.

For each candidate, identify:

- the fact owner;
- equality and units;
- how requirement satisfaction is represented or evaluated, while applicability remains explicitly authored;
- validation behavior;
- canonical/fingerprint consequences;
- runtime/observation consequences;
- what remains consumer metadata.

## Falsification and rejection conditions

Reject a generalized characteristic model when:

- it is only a renamed metadata bag;
- no current or evidenced consumer decision changes;
- it collapses reusable specification identity into equal values;
- it silently turns requirement satisfaction into derived or verified applicability before the separate qualification question is promoted;
- it makes characteristic or requirement meaning depend on ambient consumer conventions;
- units or comparison semantics are undefined;
- the current explicit eligibility relation already answers the proven need more truthfully.

## Exit criteria

Conclude when the investigation can state whether `capacityLiters` should be removed, retained narrowly, or replaced by typed characteristic/requirement semantics with explicit ownership and proving evidence, without deciding whether those semantics derive or verify operation-resource eligibility.

The durable destination is [Factory model](../../architecture/factory-model.md) and [Factory Resource Semantics](../../architecture/factory-resource-semantics.md), with Engine consequences only if the surviving semantics affect execution. Implementation planning follows only after reconciliation.
