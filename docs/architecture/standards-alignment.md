# Arcogine — Standards Alignment

Arcogine sits at the intersection of manufacturing systems, digital twins, simulation, industrial data integration, and agent-based decision-making. This document summarizes which standards and reference models influence Arcogine now, which should constrain future design, and which are only relevant to later integrations.

Per the [Product Charter](../product/charter.md), Arcogine's mature product direction spans design, understanding, simulation, verification, operation, monitoring, and improvement over one executable business model. Standards matter where they improve semantic continuity, interoperability, verification, or operational trust. They do not define Arcogine's product identity or require speculative implementation.

[ADR-0012](decisions/0012-external-interchange-and-serialization-boundaries.md) establishes the repository-wide representation rule: Arcogine-owned semantic contracts are authoritative by default; file formats, wire formats, broker envelopes, and industrial schemas are projections or adapters unless a later Accepted ADR explicitly assigns them authority.

The proposed [Operational Execution and Digital Twin Architecture](operational-execution-digital-twin.md) owns the future semantic integration boundary for real operational systems: execution context, verified identity/trust, authority, command/result lifecycle, deployment provenance, independent operational observations, reconciliation, and adapter transformation provenance. Protocol and interchange standards sit behind that boundary rather than defining Arcogine's canonical ontology.

## How to read alignment claims

These claims are distinct:

```text
Reference
    a standard informs design or terminology

Semantic mapping
    Arcogine concepts have documented corresponding meanings

Interchange compatibility
    a defined adapter or exchange profile can translate data

Conformance
    implementation satisfies a stated normative profile and validation process
```

Unless a section explicitly says otherwise, Arcogine claims only **reference** or **semantic mapping**. It currently makes no standards-conformance claim.

When this repository refers to a standards family or shared conceptual model, a family-level label such as `ISA-95 / IEC 62264` is appropriate. When a requirement, conformance claim, interoperability profile, audit record, contractual obligation, or other normative dependency relies on a standard, identify the exact source authority, designation, part, edition or year, source locator when applicable, and applicable adoption/profile. Closely aligned standards families and national adoptions must not be assumed to be textually or normatively identical across editions.

## Alignment strategy

| Tier | Meaning | Action |
|---|---|---|
| **Align now** | Affects current architecture, semantics, naming, validation, identity, or public contracts | Preserve compatible concepts and document exact boundaries |
| **Design for** | Does not require current implementation, but avoid choices that would make later integration unnecessarily destructive | Maintain an explicit mapping, trigger, and extension path |
| **Note for later** | Relevant only when a concrete future domain or external integration appears | Record the trigger; do not build ahead of need |

## Representation-selection policy

Use the narrowest representation that matches the owning semantic boundary:

```text
Human-authored scenario
    -> TOML

Stable HTTP/API representation
    -> JSON + OpenAPI

Factory semantic identity
    -> factory-model:<policy>:<algorithm>:<digest>
       currently factory-model:v1:sha256:<digest>

Historical controlled revision identity
    -> ControlledRevisionId

Current supported simulation state
    -> RuntimeObservation projection

Ordered supported simulation change
    -> RuntimeEvent projection

High-volume analytical export
    -> Parquet once the source schema is stable

Industrial interchange
    -> adapter standard selected by domain boundary and concrete integration need
```

Two rules are especially important:

1. **Representation is not identity.** `factory-model:v1` is defined by ADR-0006's normative binary grammar, not TOML, JSON, a serializer library, or canonical JSON.
2. **Projection is not ontology.** OpenAPI, CloudEvents, B2MML, AutomationML, AASX, FMI, Parquet, IFC, glTF, STEP, OPC UA, MQTT, and similar formats map around Arcogine-owned semantic contracts rather than becoming those contracts automatically.

## Regional adoption context

Romania's national standards body is ASRO. Relevant IEC and ISO standards may be adopted as Romanian standards using designations such as SR EN IEC or SR EN ISO. This makes international standards directly relevant to Romanian industrial interoperability and procurement contexts, but semantic or technical alignment by itself does **not** establish legal, regulatory, contractual, or certification compliance.

| International standard or framework | Common Romanian / EU context |
|---|---|
| IEC 62264 (closely aligned with ISA-95) | SR EN IEC 62264 family |
| ISO 22400 | SR EN ISO 22400 family |
| ISO 9001 | SR EN ISO 9001 |
| ISO 10303 / STEP | SR EN ISO 10303 family |
| OPC UA / IEC 62541 | SR EN IEC 62541 family |
| GDPR / EU 2016/679 | Directly applicable EU regulation when personal data is processed |

---

## Tier 1 — Align now

### ISA-95 / IEC 62264 — Enterprise-control system integration

ISA-95 and IEC 62264 are closely harmonized standards families with shared lineage and semantics for enterprise-control system integration, but individual parts and editions are not automatically identical. Arcogine uses the family as a **semantic reference**, not as an implemented information model or conformance profile.

**Current status:**

- The scenario schema already uses selected ISA-95-oriented terms: `equipment`, `material`, `process_segment`, and `operations_definition`.
- Runtime concepts such as `Machine`, `Routing`, `RoutingStep`, `Order`, and `Job` are mappable to a narrow production-execution subset, but the mappings are approximate.
- unit-work decomposition now keeps one accepted quantity-`N` `Order` as the aggregate request/correlation identity and deterministically materializes `N` independently dispatchable unit-quantity child `Job`s under that `OrderId`.
- The current runtime model still does not consistently separate generalized resource definitions, production schedules, execution records, and performance records.
- Arcogine does not implement the ISA-95 equipment hierarchy, generalized personnel/material/equipment capability models, B2MML exchange profiles, transactions, or conformance validation.

| Arcogine area | ISA-95 relationship | Current assessment |
|---|---|---|
| Scenario `equipment` | Equipment | Good vocabulary mapping |
| Runtime `Machine` | Equipment instance at approximately work-unit granularity | Useful alias; no capability or hierarchy model |
| Scenario `material` / runtime `ProductId` | Material Definition | Partial; product-oriented and minimal |
| `operations_definition` / `Routing` | Operations or Work Definition | Partial; simplified ordered steps |
| `process_segment` / `RoutingStep` | Process Segment or work-step analogue | Partial; explicit eligible resource instances, no generalized capability requirement |
| `Order` | Job Order / production request aggregate | Partial; one accepted quantity-bearing request under stable `OrderId` |
| child `Job` | Independently dispatchable work item / execution state | Partial; unit-quantity child under parent `OrderId`, identified by `JobId` |
| Factory events and observations | Work execution and performance facts | Narrow but useful semantic mapping; supported supported runtime observation/event contract contract is being established separately from internal scheduler events |

**Current commitment:**

- Keep an explicit concept mapping rather than relying on similar-sounding names.
- Preserve definition/request/execution/performance distinctions when concrete features require them.
- Retain approachable Arcogine terminology where it is clearer, with documented aliases.
- Keep equipment/resource hierarchy distinct from spatial factory layout.
- Use `ISA-95 / IEC 62264` for family-level architectural discussion, but identify the exact normative source and edition when a requirement, profile, or conformance claim depends on one.
- Do not claim ISA-95 compatibility or conformance without a defined and tested interchange profile.

See [ISA-95 Semantic Mapping](isa-95-semantic-mapping.md) for the maintained concept register, current structural gaps, naming policy, review checklist, and future adapter path. Its unit-work decomposition current-state wording should remain synchronized with implemented order/child-job behavior.

### Discrete-event simulation methodology

Discrete-event simulation is Arcogine's core execution methodology.

**Current alignment:**

- events occur at explicit simulation times;
- the scheduler advances by processing ordered events;
- same-time ordering is deterministic;
- state transitions remain event-driven;
- seeded randomness and deterministic acceptance tests protect repeatability;
- the simulation layer remains independent of rendering and wall-clock pacing.

Internal scheduler `Event` types remain simulation machinery. [ADR-0011](decisions/0011-runtime-observation-and-event-contract.md) establishes separate supported `RuntimeObservation` and `RuntimeEvent` semantics for outward consumers.

### Queueing theory and Little's Law

Queueing concepts provide the mathematical foundation for bottleneck analysis, work in process, waiting time, utilization, throughput, and lead time.

**Current commitment:**

- keep queue, backlog/WIP, throughput, and lead-time semantics explicit;
- use Little's Law as a reasonableness and scenario-validation relationship where its assumptions apply;
- do not present approximate or transient simulation measurements as exact identity checks without documenting sampling windows and assumptions.

### ISO 22400 — Manufacturing operations management KPIs

ISO 22400 is a useful semantic and formula reference for manufacturing KPIs.

**Current status:** Arcogine exposes operational measurements and observations such as throughput, lead time, backlog/work in process, machine activity, order counts, event counts, and simulated time. The exact supported outward KPI set and formulas continue to evolve.

**Current commitment:**

- use precise names and units;
- document each KPI's population, time window, and formula;
- map an Arcogine KPI to an ISO 22400 KPI only after verifying that the semantics and calculation match;
- avoid implying support for the complete ISO 22400 KPI catalogue.

### OpenAPI

OpenAPI is the intended standard description format for stable HTTP contracts.

**Current status:** The HTTP API is documented manually in [`docs/reference/api.md`](../reference/api.md) and consumed through typed frontend code. OpenAPI generation and contract validation are not yet established as the authoritative source.

**Design direction:** Stable HTTP projections should have machine-readable schemas and compatibility tests. OpenAPI follows accepted domain semantics rather than driving them. In particular, supported runtime observation/event contract `RuntimeObservation` / `RuntimeEvent` semantics should stabilize before the legacy API/SSE projection is promoted into a durable external compatibility surface.

### JSON for structured external projections

JSON is the default structured representation for ordinary external HTTP/API projections where no stronger domain-specific format is required.

JSON is **not** Arcogine's semantic identity representation. `factory-model:v1` remains the durable model identity contract from ADR-0006 and must not be redefined through JSON canonicalization or serializer defaults.

---

## Tier 2 — Design for

### B2MML — ISA-95-oriented manufacturing interchange

B2MML is the explicit design-for candidate when Arcogine needs concrete ERP/MES manufacturing-data exchange based on ISA-95 / IEC 62264 concepts.

Likely mapping areas include production/resource definitions, production requests, execution/performance facts, and related enterprise/manufacturing exchanges. Any implemented profile must identify exact source schema/version, mappings, extensions, ignored data, lossy conversions, and validation rules.

B2MML types remain adapter types; they do not become factory or runtime domain types by implication.

### AutomationML / IEC 62714 — Factory and plant engineering interchange

AutomationML is the primary design-for candidate for future cross-tool plant/factory engineering import/export where Arcogine needs to exchange engineering hierarchy, properties, relationships, and associated geometry/logic references.

**Implementation trigger:** the first concrete external engineering-tool or industrial factory-design interchange requirement.

AutomationML maps through Arcogine's canonical factory-design boundary. It does not replace `FactoryModel` as the executable semantic source.

### CloudEvents — Runtime-event integration envelope

CloudEvents is a plausible integration-envelope projection for supported `RuntimeEvent` instances once an external event integration needs a standardized envelope.

It is not the Arcogine runtime-event domain type, does not define simulation ordering or provenance semantics, and is not required by supported runtime observation/event contract. Arcogine-specific responsibilities such as run identity, supported sequence, simulated time, `ModelFingerprint`, optional authoritative `ControlledRevisionId`, affected entities, and semantic payload remain explicit.

### Apache Parquet

Parquet is the preferred design-for candidate for durable high-volume analytical export once supported outward schemas are stable.

Candidate datasets include runtime events, periodic observations, KPI series, experiment batches, challenge/run comparisons, and parameter sweeps. Supported exports must derive from `RuntimeEvent`, `RuntimeObservation`, or another explicit outward contract — not by freezing internal `EventLog` serialization.

### RAMI 4.0

RAMI 4.0 is a useful classification and positioning framework, not a code structure Arcogine must reproduce.

| RAMI concern | Arcogine analogue |
|---|---|
| Asset | Factory resources, products/materials, and future physical-asset models |
| Integration | Scenario loading, model adapters, supported events/observations, external observations |
| Communication | HTTP/SSE today; possible standardized async and operational adapters later |
| Information | Shared types, domain models, observations, and public schemas |
| Functional | Simulation, domain handlers, policies, verification, KPIs |
| Business | Product objectives, economy, finance, agents, and future planning domains |

### Asset Administration Shell / AASX

AAS may become relevant when Arcogine represents or exchanges industrial asset identity, properties, capabilities, and digital-twin metadata. AASX is the explicit package/file representation to consider for such exchange.

AAS adapters must map through Arcogine's operational identity/observation/deployment boundary. AAS submodels or semantic IDs may inform mappings, but they do not become the canonical Arcogine model solely because an external asset uses them.

**Not current:** Arcogine does not have an AAS/AASX adapter, AAS submodels, or a generalized equipment-capability model.

### Functional Mock-up Interface / FMU

FMI may be useful for model exchange or co-simulation with other engineering tools.

Arcogine's headless simulation and explicit interfaces make an adapter plausible, but no FMU packaging, FMI lifecycle implementation, or co-simulation timing contract exists today. A future adapter should live under an interface/integration boundary rather than modify the core domain model around FMI types.

### ISO 9001

ISO 9001 is relevant as a quality-management context rather than a direct software-conformance target.

Arcogine's explicit events, deterministic rerun, model versioning, observations, and verification goals are compatible with process traceability and continuous-improvement practices. No ISO 9001 certification or quality-management-system claim follows from those architectural properties.

---

## Tier 3 — Note for later

### AsyncAPI

Potential machine-readable description for supported asynchronous channels once Arcogine has multiple durable async transport surfaces whose shared compatibility contract justifies it. SSE alone does not create an implementation requirement.

### Apache Arrow IPC

Potential high-throughput analytical/application interchange when zero/low-copy process integration or analytical tooling justifies it. Arrow remains an interchange/storage mechanism, not a domain model.

### CSV

Useful convenience export for small tabular datasets and spreadsheet workflows. CSV must not be treated as authoritative for nested semantics, types, units, identity, provenance, or schema evolution.

### IFC

Relevant for building/facility context once spatial work needs interoperable floors, spaces, structures, zones, or related facility geometry. IFC is not the production-process ontology.

### glTF / GLB

Relevant for presentation meshes, materials, textures, and rendering assets in game/web/3D consumers. Arcogine-owned semantic layout, identity, capabilities, and execution state must not become authoritative only inside a rendering asset.

### OPC UA / IEC 62541

Relevant when Arcogine ingests live equipment observations, issues controlled operations to an industrial endpoint, or exposes digital-twin information. The [Operational Execution and Digital Twin Architecture](operational-execution-digital-twin.md) is the semantic owner for such an integration.

A future OPC UA profile must preserve Arcogine semantic identity separately from external node/asset identity; raw observation provenance separately from interpretation; verified peer/source/target trust; command/request/acknowledgement/result separation; adapter/profile/mapping versions; applied-artifact provenance; and modeled, observed, and reconciled twin state as distinct concepts.

### MQTT

Potential transport for distributed observations and commands. Transport choice must not weaken identity verification, authority, integrity/authenticity, ordering, replay, idempotency, command/fact boundaries, or observation provenance. Topic names and payload schemas map into Arcogine semantic contracts rather than define them.

### BPMN

Potential notation for business or approval workflows. It should not replace production-operation semantics merely because both can be drawn as flows.

### ReqIF, SARIF, W3C PROV, and OSCAL

Potential Governance projections when concrete requirement interchange, findings integration, provenance interchange, or security/control-framework use cases appear.

These remain downstream of Governance-owned requirements/assertions, conformance evaluation, evidence/evidence-use, findings, governed change, and audit projections. They must not create a generic cross-domain evidence/event/evaluation ontology prematurely.

### SCOR

Relevant if Arcogine expands from production into supply-chain planning, sourcing, delivery, and returns.

### FIPA

Relevant only if independently developed autonomous agents need standardized inter-agent communication. Current agents act through Arcogine's controlled observation and event boundaries.

### ISO 8000

Relevant when external master or operational data becomes authoritative input. Data provenance, quality rules, identity, and reconciliation will then require explicit models.

### ISO 10303 / STEP / AP242

Relevant for CAD/PLM and detailed product/equipment-engineering integration. It is later than AutomationML for factory-engineering interchange and later than IFC for facility context.

### Industrial fieldbus protocols

Modbus, PROFINET, EtherCAT, and similar protocols are relevant only through later shop-floor gateways or adapters. Low-level protocol support should not enter the simulation core.

### GDPR

Relevant when Arcogine processes personal data, such as identifiable operator actions or workforce information. Synthetic scenarios alone do not create a GDPR implementation requirement. Real deployments would need purpose limitation, data minimization, access control, retention, and data-subject handling appropriate to the processing context.

---

## Operational integration policy

The first industrial adapter should be selected for learning value rather than breadth. Before a protocol-backed adapter can be considered production-consequential, the operational track must already define the protocol-independent contracts for:

```text
execution context
verified identity / trust
capability and authorization
command lifecycle
external observation provenance
temporal + unit semantics
mapping/transformation version
applied-artifact provenance
reconciliation
recovery / ambiguous outcomes
```

A protocol test server may prove adapter mechanics before physical integration. Connecting successfully to a broker/server, implementing a vendor SDK, or reproducing a protocol object model does not by itself establish Operational Execution readiness or standards conformance.

## Compatibility evidence policy

A representation becomes a supported compatibility surface only when its owning capability defines enough of the following to make compatibility reproducible:

- schema/profile identity and version;
- semantic source contract;
- required identity/provenance fields;
- field meaning and units;
- ordering rules where relevant;
- evolution/compatibility policy;
- validation behavior;
- golden fixtures, contract tests, or equivalent compatibility evidence.

The existence of a serializer library, generated schema, exported file, or successful protocol connection is not sufficient evidence of a supported interchange contract.

Do not create a generic `interop`, `formats`, `ExternalModel`, or cross-domain interchange ontology merely to host future possibilities. Let concrete adapters prove shared infrastructure before extracting it.

## Summary

| Standard or format | Tier | Arcogine position |
|---|---|---|
| ISA-95 / IEC 62264 | Align now | Maintained semantic mapping; exact normative source required for profiles or conformance claims |
| DES methodology | Align now | Core simulation architecture; outward runtime contract remains separate from internal scheduler events |
| Queueing theory / Little's Law | Align now | Operational analysis and validation foundation |
| ISO 22400 | Align now | KPI terminology/formula reference; map only verified equivalents |
| JSON | Align now | Default structured external projection; not semantic identity |
| OpenAPI | Align now | Intended machine-readable HTTP contract after domain surfaces stabilize |
| B2MML | Design for | Explicit future ISA-95-oriented ERP/MES exchange candidate |
| AutomationML / IEC 62714 | Design for | Primary future factory/plant engineering interchange candidate |
| CloudEvents | Design for | Possible RuntimeEvent integration-envelope projection; not core event type |
| Parquet | Design for | Preferred bulk analytical export once outward schemas stabilize |
| RAMI 4.0 | Design for | Classification and positioning reference |
| AAS / AASX | Design for | Future asset/digital-twin metadata/package integration path |
| FMI / FMU | Design for | Potential interface adapter for model exchange/co-simulation |
| ISO 9001 | Design for | Quality-management context, not certification claim |
| AsyncAPI | Note for later | Async channel description when multiple stable async surfaces justify it |
| Arrow IPC | Note for later | High-throughput analytical/application interchange |
| CSV | Note for later | Human/spreadsheet convenience only |
| IFC | Note for later | Facility/building/spatial context |
| glTF / GLB | Note for later | Consumer presentation assets, not canonical semantics |
| OPC UA / IEC 62541 | Note for later | Candidate live industrial adapter behind Operational Execution semantics |
| MQTT | Note for later | Distributed messaging transport behind the same operational semantics |
| ReqIF / SARIF / PROV / OSCAL | Note for later | Governance interchange/projection candidates |
| BPMN | Note for later | Business/approval workflow notation |
| SCOR | Note for later | Supply-chain expansion |
| FIPA | Note for later | External multi-agent interoperability |
| ISO 8000 | Note for later | External data quality and governance |
| ISO 10303 / STEP / AP242 | Note for later | Detailed CAD/PLM engineering data |
| Industrial fieldbus protocols | Note for later | Gateway-level shop-floor connectivity |
| GDPR | Note for later | Personal-data obligations in real deployments |

## Current non-decisions

This standards register deliberately does **not** choose:

- Governance authoritative controlled-revision persistence and historical resolution persistence/database/artifact format;
- a canonical JSON representation for `FactoryModel`;
- CloudEvents, Kafka, NATS, MQTT, WebSocket, or another runtime transport as the Engine domain contract;
- one universal industrial interchange format;
- a generic cross-domain interchange module or ontology.

Those choices remain with the capability that owns the semantic boundary and are made only when implementation pressure makes the decision concrete.
