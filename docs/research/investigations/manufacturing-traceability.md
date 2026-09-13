# Manufacturing Traceability Research

> **Status:** CANDIDATE — material identity/genealogy is High priority; MES/MOM interoperability is Medium priority  
> **Scope:** Determine the minimum manufacturing-traceability semantics Arcogine may need to own, and the boundary between Arcogine canonical semantics and external MES/MOM traceability models  
> **Authority:** Research only; this document defines no current capability, accepted architecture, or implementation commitment

## Purpose

Manufacturing traceability creates useful pressure on Arcogine's existing Factory, Engine, Operational, Governance, and standards boundaries, but it must not silently turn Arcogine into an MES or let an external MES information model define Arcogine's ontology.

Current accepted quantity execution creates independently dispatchable unit work under one aggregate production request. That does not establish physical lot, batch, serial, material-lot, split/merge, or genealogy semantics. Likewise, future Operational Execution already has separate research responsibilities for durable operational identity, command/result lifecycle, deployment provenance, external observations, subject correspondence, and reconciliation. This investigation must reuse those boundaries rather than create parallel trace-specific versions of them.

The two admitted questions below are intentionally separate:

1. what manufacturing material identity and genealogy, if any, Arcogine itself needs to represent; and
2. what semantic boundary Arcogine must preserve when interoperating with MES/MOM traceability models.

The second question is downstream of the first: interoperability must not decide Arcogine's canonical ontology by accident.

## Material identity and production genealogy — CANDIDATE

### Question

> What material, lot, batch, serial, transformation, and genealogy semantics, if any, must Arcogine own to support meaningful production traceability beyond execution work-item identity?

### Decision at stake

Whether Arcogine needs first-class manufacturing-material identity and lineage semantics, where those semantics belong, and what must remain external or deferred.

A positive answer could affect Factory semantics, Operational execution history, durable identity/lineage architecture, and later implementation. A negative or narrower answer could keep material genealogy outside Arcogine while preserving only explicit external correlations.

### Current/simple candidate

Introduce no Arcogine-owned material genealogy. Keep production request/work-item identity, external material identifiers, observations, and correspondence distinct until a concrete supported manufacturing use case proves a canonical lineage invariant is necessary.

### Candidate alternatives

- **External-only material identity.** Arcogine records or maps externally owned lot/serial identifiers where integrations require them but does not define canonical genealogy semantics.
- **Minimal canonical genealogy.** Arcogine owns the smallest stable material identity and consume/produce lineage needed for backward and forward traceability while leaving inventory, quality, personnel, and warehouse semantics outside the model unless separately justified.
- **Richer manufacturing-material model.** Additional batch, serial, split/merge, rework, quantity/unit, state, or related semantics are admitted only if concrete consumers prove that the minimal alternatives cannot preserve the required invariant.

### Proving cases

At minimum, discriminate the candidates against cases where:

- one input lot produces one output lot;
- several input lots contribute to one output batch;
- one material identity is split into several descendants;
- several material identities are merged;
- rework consumes a previously produced item or lot;
- serialized and non-serialized production coexist;
- one production order spans several physical lots or serial units;
- two execution work items contribute to one physical batch, or one execution work item spans more than one physical material identity;
- trace history must remain attributable across process restart, failover, deployment change, or later reconciliation;
- a physical/domain batch must remain distinguishable from an Engine implementation optimization or work chunking choice.

A successful candidate must not infer physical lot/batch/serial identity from `JobId`, order quantity decomposition, queue structure, or runtime object count.

### Evidence expectations

Use current Arcogine Factory/Engine/Operational authority plus external manufacturing-domain evidence only where it can discriminate between execution identity and physical/material identity, establish known genealogy consequences, or expose failure modes Arcogine would otherwise miss.

Standards or MES product concepts are evidence about possible distinctions and interoperability pressure; they do not establish that Arcogine must own the same ontology.

### Falsification / narrowing conditions

A proposed Arcogine-owned concept should be rejected or narrowed when:

- the required query can be answered without a new canonical identity or relation;
- the concept merely restates an external identifier without a stable Arcogine invariant;
- the concept duplicates Operational observation/correspondence or Governance evidence semantics;
- the concept exists only to mirror a standard/vendor schema; or
- its claimed manufacturing identity can be replaced by current Engine work-item decomposition without losing any real physical distinction, demonstrating that no separate material invariant was established.

### Exit criteria

This question is ready to conclude only when the evidence is sufficient to state:

- whether Arcogine needs any canonical material/lot/batch/serial identity;
- the minimum invariant and equality/lineage rules for every admitted identity or relation;
- ownership across Factory, Operational Execution, Governance, and Engine boundaries;
- how backward and forward genealogy are reconstructed;
- what remains external or explicitly deferred;
- what would reopen the decision; and
- whether any hard-to-reverse identity/lineage conclusion requires an ADR and independent adversarial review before promotion.

### Expected durable destination

No action, or Factory/Operational architecture with an ADR only if durable material identity/lineage semantics survive. Engine implementation must not change merely because manufacturing genealogy exists.

## MES/MOM traceability interoperability boundary — CANDIDATE

### Question

> Which manufacturing-traceability concepts must Arcogine preserve to interoperate meaningfully with MES/MOM ecosystems without adopting an external information model as Arcogine's canonical production ontology?

### Decision at stake

Where Arcogine canonical semantics end and standards/vendor-specific traceability mappings begin, including whether repeated interoperability pressure exposes a genuinely shared Arcogine invariant or only adapter-local translation.

### Current/simple candidate

Keep Arcogine's canonical semantics unchanged and implement traceability mappings only at integration boundaries when a concrete integration requires them.

### Candidate alternatives

- **Mapping-only interoperability.** External lot, batch, serial, material, operation, resource, quality, or personnel concepts remain adapter/profile concerns unless Arcogine already has an equivalent semantic concept.
- **Small shared traceability subset.** Repeated integrations justify a minimal canonical subset whose invariant is independently useful inside Arcogine.
- **Broader canonical adoption.** A larger external concept family is admitted only when Arcogine otherwise cannot preserve a required production meaning or trace query and the semantics survive independently of any one standard/vendor representation.

### Proving cases

Test at least:

- production-order identity and correlation;
- lot/batch/serial genealogy;
- operation execution status/history;
- equipment/resource identity versus external physical-asset identity;
- process/model/revision context;
- material consumption and production;
- quality or personnel attribution when an external system supplies it but Arcogine has no corresponding canonical domain;
- round-trip mapping where the external representation contains concepts Arcogine deliberately does not own;
- mapping/profile version changes over time;
- two MES/MOM systems that express equivalent manufacturing meaning through materially different schemas.

### Evidence expectations

Use precisely versioned primary standards/specifications and representative official product/platform semantics only when they materially test the boundary. Capture what transfers and where each analogy breaks. A standards family name or common vendor practice is not sufficient evidence by itself.

### Falsification / narrowing conditions

A proposed canonical Arcogine concept should be rejected when it is required only by one external schema, can remain losslessly adapter-local, duplicates an existing Arcogine concept, or has no Arcogine-side decision/query that depends on it.

### Exit criteria

Conclude only when Arcogine can state, for the supported traceability use cases:

- which concepts it must represent canonically;
- which concepts it only maps;
- which concepts it deliberately leaves external;
- what information loss or unsupported round-trip behavior is acceptable and explicit;
- what mapping/profile provenance must be preserved; and
- which concrete integration, if any, is sufficiently bounded to become implementation planning.

### Expected durable destination

`docs/architecture/isa-95-semantic-mapping.md`, `docs/architecture/standards-alignment.md`, and Operational integration architecture as warranted. Implementation planning is downstream of a concrete admitted integration, not of this research item by itself.

## Relationship to Operational boundary research

Manufacturing traceability is a proving pressure on the existing [Operational Execution and Digital-Twin Boundary Research](operational-execution-digital-twin-boundaries.md), not a second Operational ontology.

Operational research must preserve the distinction among requested semantic operation, submitted/accepted external command, actual transition, observation, and reconciled interpretation; preserve independent observation provenance; and establish explicit subject correspondence and historical attribution. This investigation supplies manufacturing-specific queries and failure cases that test those boundaries. It does not redefine durable operational identity, command lifecycle, deployment, observation, correspondence, or reconciliation.

Conversely, Operational research must not invent material lot/batch/serial identity merely to make a trace query convenient. If a trace requires material identity or genealogy that current semantics cannot express, that is evidence for the material-identity question above.

## Explicit non-goals

This investigation does not by itself justify:

- an MES module or complete MOM/MES product scope;
- inventory, warehouse, quality, personnel, maintenance, recipe, recall, or shipment domains without separate concrete requirements;
- treating Engine `JobId` or quantity decomposition as physical material identity;
- choosing an event store, historian, database, broker, or protocol;
- ISA-95/B2MML/OPC UA conformance claims;
- importing an external standard or vendor ontology wholesale; or
- implementation planning before the relevant semantic questions are settled and reconciled.

## Promotion boundary

The material/genealogy question may promote only after its identity, lineage, ownership, and proving cases are decision-quality. The interoperability question may promote only after Arcogine's internal semantic boundary is sufficiently clear that adapters cannot silently define it.

Any later implementation responsibility belongs in `docs/planning/` only after the relevant conclusion has been reconciled into its durable architecture/product authority and has explicit prerequisites plus executable acceptance evidence.
