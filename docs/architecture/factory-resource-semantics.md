# Factory Resource Semantics

> **Status:** Maintained architectural reference  
> **Scope:** Current canonical meaning of Factory productive-resource identity and the admission rules for reusable specifications, qualification, grouping, and external-asset correspondence  
> **Authority:** Architectural interpretation of current Factory resource semantics. It does not change released Factory Model fingerprint policy, runtime behavior, or Accepted ADR decisions.  
> **Related:** [Architecture Overview](overview.md), [Factory Design Architecture](factory-design.md), [ISA-95 Semantic Mapping](isa-95-semantic-mapping.md), [ADR-0003](decisions/0003-canonical-factory-model-boundary.md), [ADR-0011](decisions/0011-runtime-observation-and-event-contract.md), [Factory Design Capability Plan](../planning/factory-design-capability.md), [Factory Resource Semantics Research Conclusion](../research/factory-resource-semantics.md)

## Current resource referent

The canonical Factory resource is one independently identified **configured productive resource in a published factory design**.

Current `ResourceDefinition` is the complete configured-resource record despite its historical type name. It is not a reusable equipment-type registry entry. Its `MachineId` identifies the designed productive participant and is the stable resource correlation identity used when runtime state is instantiated from that design.

Repeated catalogue/template origin, equal configured values, a common manufacturer/model label, or installing the same authored item more than once do not create a second canonical reusable-definition identity. Those facts remain authoring/provenance data unless a cross-consumer production semantic requires them.

## Orthogonal distinctions

Do not collapse these concerns into one generic type/instance hierarchy:

| Concern | Current Arcogine position |
|---|---|
| Configured productive resource identity | Canonical Factory concept |
| Reusable technical specification/classification | Separate future concept only when it carries an independently meaningful contract/dependency |
| Capability/qualification | Separate future relation; explicit eligible-resource IDs are sufficient today |
| Work center/resource pool/hierarchy | Separate future relation/identity only when it owns consequential scheduling, capacity, responsibility, or reporting semantics |
| Spatial placement | Authored Factory facts when behaviorally relevant; not equipment hierarchy |
| Mutable queue/availability/setup/execution state | Runtime/Operational state, never immutable design identity |
| External serialized physical asset | Operational correspondence concern, not Factory resource equality |
| Catalogue/template identity and purchase economics | Consumer authoring/economics unless promoted by a proven cross-consumer semantic need |

This interpretation is ISA-95-informed without requiring Arcogine to copy one external information-model decomposition. Classification, specification, configured identity, qualification, hierarchy, location, physical asset, and execution state may all be independently meaningful and therefore need separate equality/cardinality rules when introduced.

## Reusable specification promotion rule

A reusable resource specification becomes canonical only when its identity preserves a necessary cross-consumer statement that complete configured-resource records cannot preserve.

A viable invariant is:

> **Resource specification identity denotes an explicitly identified technical contract. Its revision fixes the admitted configuration space, property meanings, and explicit guarantees against which configured resources can be checked.**

Two configured resources share such a specification revision because both intentionally reference and satisfy that contract. Equal values alone do not infer shared identity. Sharing the specification implies only the guarantees written into it; it does not automatically imply identical configured capability, geometry, runtime interchangeability, availability, or satisfaction of every operation requirement.

Use this proving question before introducing the split:

> After expanding a proposed reusable definition into complete configured-resource records, which necessary cross-consumer statement becomes impossible to answer?

If the lost statement is only “which catalogue/template supplied these defaults?”, keep it in authoring provenance. If it is “does this resource conform to the approved specification?” or “which resources depend on this specification revision?”, a canonical specification identity may be warranted.

## Qualification and resource-dependent performance

Current explicit eligible-resource IDs mean authored permission to perform a step; they are not an inferred engineering-competence model. Current step duration also belongs to the step rather than the selected resource.

Do not introduce first-class capability merely to rename explicit eligibility. Promote qualification only when Arcogine must **discover or verify** applicability from represented production facts. At that point preserve separate concepts for:

- operation requirements and units;
- configured-resource provisions/limits;
- tooling/fixture or other joint requirements;
- resource-dependent duration, consumption, or cost where needed; and
- mutable availability/current setup versus persistent qualification.

Qualification precedes runtime availability and deterministic selection. A bare capability tag is insufficient evidence of feasibility.

## Grouping and heterogeneous participants

A work center, resource pool, or hierarchy deserves canonical identity only when the grouping owns behavior or interpretation such as scheduling scope, aggregate capacity, responsibility, reporting, or capability aggregation. UI folders and physical proximity are insufficient.

Workers, tools/fixtures, transport resources, and storage/buffers may eventually participate in shared requirement/allocation relations, but shared participation does not justify one universal machine/resource superclass. Promote typed concepts when their own lifecycle, qualification, occupancy, contention, or behavior becomes consequential.

## Designed resource versus external physical asset

A designed resource can remain the same while its serialized physical realization is replaced. Conversely, changing represented productive configuration can create a new Factory model version without implying a new external asset.

Therefore designed resource identity and external physical-asset identity/correspondence are distinct. The latter belongs to Operational Execution / Digital Twin semantics once that track is admitted.

## Existing ADR interpretation

[ADR-0003](decisions/0003-canonical-factory-model-boundary.md) establishes the canonical design/runtime boundary and lists resource definitions/instances as examples while explicitly leaving concrete type decomposition to implementation. The current collapsed configured-resource representation satisfies that decision.

[ADR-0011](decisions/0011-runtime-observation-and-event-contract.md) requires stable resource-instance identity and definition identity in supported observations. Under the current collapsed model those roles do not require two distinct identifiers: the canonical `MachineId` identifies the configured model resource and the runtime observation correlation target. A future independently versioned reusable specification would be a separate semantic decision rather than something implied by ADR-0011 wording.

No Accepted ADR is changed by keeping the current model. A new or superseding ADR becomes appropriate only when Arcogine accepts a hard-to-reverse resource contract such as independently versioned reusable specifications, generalized qualification/allocation semantics with compatibility consequences, consequential pool/work-center aggregation, or Operational physical-asset correspondence identity.
