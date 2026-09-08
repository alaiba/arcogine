# Factory Resource Semantics Research Conclusion

> **Status:** CONCLUDED  
> **Research baseline:** `2acee74723aad7a9ca384459f5d44ef35b85b6fd`  
> **Reconciled:** 2026-09-08 against live `main` at `8d208707b808a9dfc41bb9666ee7c1ff4a14b4cc`; intervening changes did not alter Factory resource production semantics  
> **Authority:** Research evidence only. Durable consequences are reconciled into the [Factory Resource Semantics](../architecture/factory-resource-semantics.md) architectural reference and implementation planning.

## Question

> Does Factory Design need a reusable equipment/resource definition distinct from configured resource instances, and what semantic invariant would that distinction carry?

The investigation compared the current Arcogine model with ISA-95 / IEC 62264, industrial engineering, MES/MOM products, discrete-event simulation tools, factory-design software, and adjacent digital-twin/interchange models. It also pressure-tested workers, tools, transport, storage, work centers, replacement, upgrades, cross-design reuse, and MES import.

## Conclusion

**KEEP COLLAPSED FOR NOW.**

Arcogine needs one independently identified **configured productive resource in a factory design**. It does not currently need a second canonical reusable resource-definition identity.

| Question | Conclusion |
|---|---|
| Definition / configured-resource split now? | **No.** Keep one complete configured record per designed productive participant. |
| First-class capability now? | **Defer.** Explicit eligible-resource IDs remain sufficient for current execution. |
| One superclass for machines, workers, tools, transport, and storage? | **No.** Shared allocation/requirement relations may emerge without shared machine semantics. |
| Work centers, pools, hierarchy now? | **Defer.** Introduce only when the grouping owns consequential scheduling, capacity, responsibility, or reporting semantics. |
| Placement versus hierarchy? | Keep separate. Accepted V2 spatial semantics do not create equipment hierarchy. |
| Designed resource versus physical asset? | Keep distinct. External physical-asset identity/correspondence is an Operational concern. |
| Likely future resource-model pressure? | Qualified operation-resource applicability and resource-dependent performance when a concrete consumer must infer or verify feasibility. |

Repeated catalogue/template origin, equal configured values, a shared manufacturer/model label, or installing the same authored item more than once are **not** sufficient reasons to introduce canonical reusable-definition identity.

## Repository evidence

At the research baseline and the reconciliation head:

- at the research baseline, the class was named `ResourceDefinition` and represented one complete configured resource record: identity, name, concurrency, optional volumetric property, and setup time;
- `FactoryModel` contains immutable resources, operations, and products;
- runtime assembly creates one fresh `Machine` state per configured resource;
- `RoutingStep` contains explicit eligible `MachineId` values rather than a generalized capability requirement;
- deterministic dispatch ranks already-eligible resources and ends with `MachineId` as the tie-breaker;
- current step duration belongs to the step, so heterogeneous per-resource processing time for one step is not represented;
- current multi-resource tests already prove repeated equivalent resources and deterministic selection without shared definition identity;
- the Challenge catalogue has reusable offer identity and placed occurrences, but no production specification/capability contract; its reuse is consumer authoring/economics, not proof of a canonical Factory type;
- Governance proving cases can scope requirements to concrete resource identity without equipment-class identity.

At the research baseline, the class name `ResourceDefinition` therefore did not prove that the record was a reusable equipment type. Its semantic referent was the configured productive participant represented in the published design. The implementation was subsequently renamed to `ConfiguredResource` so the production type directly states that concluded referent; the semantic fields and behavior were unchanged.

## External evidence synthesis

The external evidence supports **orthogonal resource relationships** more strongly than one mandatory `definition -> instance` hierarchy.

### ISA-95 / IEC 62264

Relevant evidence was checked against exact source families/editions rather than treating “ISA-95” as timeless shorthand, including IEC 62264-1:2013, IEC 62264-2:2026, ANSI/ISA-95.00.02-2018, IEC 62264-4:2015, OPC 10030, and OPC 10031-4.

Decision-relevant findings:

- equipment classification is not the same concept as a prototype or manufacturer-model definition;
- logical equipment identity can remain stable while a physical asset is replaced;
- requirements may name classes or specific equipment, while execution records actual resources;
- personnel carry distinct qualification semantics even when they participate in similar requirement/allocation relations;
- work centers are scoped equipment groupings, not automatically substitution pools or physical rectangles.

Sources: [IEC 62264-1](https://webstore.iec.ch/en/publication/6675), [IEC 62264-2](https://webstore.iec.ch/en/publication/75127), [OPC 10030](https://reference.opcfoundation.org/specs/OPC-10030), [OPC 10031-4](https://reference.opcfoundation.org/specs/OPC-10031-4), and the [ISA standards list](https://www.isa.org/standards-and-publications/isa-standards/find-isa-standards-by-topic).

### Industrial engineering and MES/MOM

Industrial engineering evidence treats useful manufacturing capability as configuration- and context-dependent: machine, tooling/workholding, material/process constraints, human factors, and available capacity can all affect feasibility. A common operation label or manufacturer model therefore does not prove substitutability.

MES/MOM products demonstrate multiple valid decompositions:

- SAP resources may have several resource types while work-center membership remains a different relationship;
- Dynamics assigns multiple capabilities with validity/proficiency and separately models capacity/calendars/groups;
- Oracle exposes shared resource masters plus named instances, a strong positive example for a split when that master identity has planning/lifecycle meaning;
- Critical Manufacturing separates templates from resource model/revision, role, serial identity, area, and process services.

The evidence shows that a definition/instance split can be useful, but **only when the definition has an independent contract/lifecycle**. Industry practice does not establish one universal split Arcogine must adopt now.

Sources include [NIST manufacturing capability research](https://www.nist.gov/programs-projects/model-based-manufacturing-capability-definition), [SAP resource configuration](https://learning.sap.com/courses/configuring-sap-digital-manufacturing-for-execution-basic-data-and-configuration/creating-resources), [Dynamics resource capabilities](https://learn.microsoft.com/en-us/dynamics365/supply-chain/production-control/resource-capabilities), and [Oracle Manufacturing resources](https://docs.oracle.com/en/cloud/saas/supply-chain-and-manufacturing/25d/faims/how-you-manage-resources.html).

### Simulation, factory design, and adjacent digital-twin models

AnyLogic, FlexSim, Autodesk Factory Design Utilities, and Visual Components show that reusable editor/simulation components can package behavior, geometry, defaults, and animation while placed/stateful resource units remain separate. That reuse mechanism is not itself evidence for a canonical production specification.

AAS and AutomationML also keep classification, templates/types, assets/instances, roles, and information-model instances separable. Their distinctions are useful adapter/reference evidence, not an instruction to copy their aggregate shapes internally.

## Proving cases

The cases that determined the conclusion are:

1. **Two identical cutters:** require two independently assignable resource identities; shared defaults add no necessary invariant.
2. **Same manufacturer model, different configuration:** configured facts may differ while product/model label remains equal; model equality does not determine qualification.
3. **Same capability, different equipment types:** the missing semantics are qualification and resource-dependent performance, not shared definition identity.
4. **Multi-capability CNC:** one resource identity can participate in several explicitly eligible steps today; capability becomes useful only when assignments must be discovered/verified.
5. **Tool-dependent capability:** useful ability often belongs to a machine-tool-fixture configuration and may require joint allocation if tooling is independently scarce.
6. **Worker plus machine:** shared participation/allocation does not justify treating personnel as machines.
7. **Work center / pool:** grouping needs its own identity only when it owns real scheduling, capacity, responsibility, or reporting behavior.
8. **Station:** may be one scheduling unit, a composition, a location, or consumer vocabulary; no universal Station type follows.
9. **Transport:** V2 transfer delay can remain policy-level until vehicle/conveyor scarcity, movement, failures, or allocation affect outcomes.
10. **Buffer/storage:** needs explicit occupancy/material/blocking semantics before becoming a canonical participant.
11. **Equipment replacement:** can preserve designed resource identity while physical-asset identity changes; this proves design/asset correspondence, not definition/instance splitting.
12. **Equipment upgrade:** can preserve resource identity while configured facts change; a specification revision changes only when its own contract changes.
13. **One equipment product across designs:** complete published snapshots are sufficient unless cross-design qualification or change-impact needs shared specification identity.
14. **MES import:** external class IDs should survive only to the degree their semantics are required for fidelity; an external noun does not automatically become a canonical Factory definition.

## Identity and future specification invariant

Current resource semantics should be read as:

> A configured productive resource is one identified productive participant in a published factory design, independently assignable/configurable and distinct from its mutable runtime state and from any external serialized physical asset.

A future reusable resource specification is justified only if this invariant can be stated and tested:

> **Resource specification identity denotes an explicitly identified technical contract. Its revision fixes the admitted configuration space, property meanings, and explicit guarantees against which configured resources can be checked.**

Two configured resources share that specification revision only because both explicitly reference and satisfy it. Equal values do not infer shared identity. Sharing the specification implies only guarantees written into that contract; it does not automatically imply identical configured capability, geometry, runtime interchangeability, availability, or satisfaction of every operation requirement.

The decisive promotion test is:

> After expanding a proposed reusable definition into complete configured-resource records, which necessary cross-consumer statement becomes impossible to answer?

If the lost statement is only “which catalogue/template supplied these defaults?”, keep it in authoring provenance. If it is “does this resource conform to the approved specification?” or “which resources depend on this specification revision?”, canonical specification identity may be warranted.

## Capability conclusion

First-class capability remains deferred. Current explicit eligibility is authored permission, not inferred competence.

Promote capability/qualification only when Arcogine must discover or verify operation applicability from represented facts. The future model must distinguish:

- operation requirement meaning and units;
- configured-resource provision/limits;
- tooling/fixture or other joint requirements;
- resource-dependent duration/consumption/cost where needed;
- mutable availability/current setup from persistent qualification.

Qualification happens before runtime availability and deterministic selection. A bare capability tag is not sufficient evidence of feasibility.

## Resource-category conclusion

| Candidate | Disposition |
|---|---|
| Configured machine/equipment resource | **Current shared canonical concept** |
| Reusable technical specification/class | **Deferred** until independent contract/dependency exists |
| Capability/qualification | **Deferred** until inference/verification consumer exists |
| Station | Composition when independently meaningful; otherwise consumer vocabulary |
| Worker/personnel | **Deferred typed participant** |
| Tool/fixture | **Deferred typed participant/configuration relation** |
| Transport resource | **Deferred** until independent contention/behavior matters |
| Storage/buffer | **Deferred** until occupancy/material/blocking semantics matter |
| Work center | **Deferred** until consequential scope/aggregation exists |
| Resource pool | Relationship/set today; named identity deferred |
| Location | Spatial relationship/facts; independent location identity deferred |
| External physical asset | **Operational correspondence concern**, not Factory resource equality |

## Durable consequences

This conclusion does **not** introduce a production-model migration or new Factory implementation slice.

Durable reconciliation should preserve these rules:

- [Factory Resource Semantics](../architecture/factory-resource-semantics.md) starts from configured productive-resource identity, not an assumed reusable definition plus installed instance, and treats specification/classification, configured identity, capability, hierarchy, location, physical asset, and runtime state as orthogonal concepts;
- `ConfiguredResource` documentation and current code state directly that the canonical record is one configured resource; the production rename from the research-baseline `ResourceDefinition` was terminology-only;
- implementation planning records the split as **not admitted** and requires a future checkable technical-specification/dependency invariant before reopening it;
- ADR-0011's resource `definition identity` wording does not imply two distinct identifiers under the current collapsed model: the same canonical `MachineId` identifies the configured model record and runtime correlation target.

No new ADR is needed for keeping the existing model. A new/superseding ADR becomes appropriate only when Arcogine actually accepts an independently versioned specification identity, generalized qualification/allocation rules with hard compatibility impact, consequential pool/work-center aggregation, physical-asset correspondence semantics, or another hard-to-reverse resource contract.

## Follow-up research

The definition/instance question is closed. The next resource-semantic unknown is separate:

> When does Arcogine need qualified operation-resource applicability and resource-dependent performance beyond explicit eligible IDs and one step-level duration?

Keep that question in research until a concrete optimizer, industrial authoring/verification workflow, game mechanic, MES adapter, or other consumer proves the gap.
