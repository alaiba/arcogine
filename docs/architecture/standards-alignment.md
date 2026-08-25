# Arcogine — Standards Alignment

Arcogine sits at the intersection of manufacturing systems, digital twins, simulation, industrial data integration, and agent-based decision-making. This document summarizes which standards and reference models influence Arcogine now, which should constrain future design, and which are only relevant to later integrations.

Per the [Product Charter](../product/charter.md), Arcogine's mature product direction spans design, understanding, simulation, verification, operation, monitoring, and improvement over one executable business model. Standards matter where they improve semantic continuity, interoperability, verification, or operational trust. They do not define Arcogine's product identity or require speculative implementation.

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
| **Align now** | Affects current architecture, semantics, naming, validation, or public contracts | Preserve compatible concepts and document exact boundaries |
| **Design for** | Does not require current implementation, but avoid choices that would make later integration unnecessarily destructive | Maintain an explicit mapping and extension path |
| **Note for later** | Relevant only when a concrete future domain or external integration appears | Record the trigger; do not build ahead of need |

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
- Runtime concepts such as `Machine`, `Routing`, `RoutingStep`, and `Job` are mappable to a narrow production-execution subset, but the mappings are approximate.
- The current runtime model does not consistently separate resource definitions, production requests, execution state, and performance records.
- Arcogine does not implement the ISA-95 equipment hierarchy, generalized personnel/material/equipment capability models, B2MML, transactions, exchange profiles, or conformance validation.

| Arcogine area | ISA-95 relationship | Current assessment |
|---|---|---|
| Scenario `equipment` | Equipment | Good vocabulary mapping |
| Runtime `Machine` | Equipment instance at approximately work-unit granularity | Useful alias; no capability or hierarchy model |
| Scenario `material` / runtime `ProductId` | Material Definition | Partial; product-oriented and minimal |
| `operations_definition` / `Routing` | Operations or Work Definition | Partial; simplified ordered steps |
| `process_segment` / `RoutingStep` | Process Segment or work-step analogue | Partial; currently bound to one concrete machine |
| `Job` | Job Order plus execution and result concerns | Collapsed; a refactoring trigger for richer workloads |
| Factory events and observations | Work execution and performance facts | Narrow but useful semantic mapping |

**Current commitment:**

- Keep an explicit concept mapping rather than relying on similar-sounding names.
- Preserve definition/request/execution/performance distinctions when concrete features require them.
- Retain approachable Arcogine terminology where it is clearer, with documented aliases.
- Keep equipment/resource hierarchy distinct from spatial factory layout.
- Use `ISA-95 / IEC 62264` for family-level architectural discussion, but identify the exact normative source and edition when a requirement, profile, or conformance claim depends on one.
- Do not claim ISA-95 compatibility or conformance without a defined and tested interchange profile.

See [ISA-95 Semantic Mapping](isa-95-semantic-mapping.md) for the maintained concept register, current structural gaps, naming policy, review checklist, and future adapter path.

### Discrete-event simulation methodology

Discrete-event simulation is Arcogine's core execution methodology.

**Current alignment:**

- events occur at explicit simulation times;
- the scheduler advances by processing ordered events;
- same-time ordering is deterministic;
- state transitions remain event-driven;
- seeded randomness and deterministic acceptance tests protect repeatability;
- the simulation layer remains independent of rendering and wall-clock pacing.

Primary implementation area: `product/simulation/`, with domain handlers under `product/domains/` and orchestration in `product/interfaces/api/` and `product/interfaces/cli/`.

### Queueing theory and Little's Law

Queueing concepts provide the mathematical foundation for bottleneck analysis, work in process, waiting time, utilization, throughput, and lead time.

**Current commitment:**

- keep queue, backlog/WIP, throughput, and lead-time semantics explicit;
- use Little's Law as a reasonableness and scenario-validation relationship where its assumptions apply;
- do not present approximate or transient simulation measurements as exact identity checks without documenting sampling windows and assumptions.

### ISO 22400 — Manufacturing operations management KPIs

ISO 22400 is a useful semantic and formula reference for manufacturing KPIs.

**Current status:** Arcogine exposes operational measurements and observations such as throughput, lead time, backlog/work in process, machine activity, order counts, event counts, and simulated time. The exact public KPI set and formulas continue to evolve.

**Current commitment:**

- use precise names and units;
- document each KPI's population, time window, and formula;
- map an Arcogine KPI to an ISO 22400 KPI only after verifying that the semantics and calculation match;
- avoid implying support for the complete ISO 22400 KPI catalogue.

Potential later additions include availability, quality, setup, failure, and OEE-related measures once the underlying domains exist.

### OpenAPI

OpenAPI is the intended standard description format for stable HTTP contracts.

**Current status:** The HTTP API is documented manually in [`docs/reference/api.md`](../reference/api.md) and consumed through typed frontend code. OpenAPI generation and contract validation are not yet established as the authoritative source.

**Design direction:** A future versioned external-consumer contract should have a machine-readable schema and compatibility tests. OpenAPI is the natural choice for HTTP surfaces, but the schema must follow accepted domain semantics rather than drive them.

---

## Tier 2 — Design for

### RAMI 4.0

RAMI 4.0 is a useful classification and positioning framework, not a code structure Arcogine must reproduce.

| RAMI concern | Arcogine analogue |
|---|---|
| Asset | Factory resources, products/materials, and future physical-asset models |
| Integration | Scenario loading, model adapters, event history, external observations |
| Communication | HTTP/SSE today; possible OPC UA or MQTT adapters later |
| Information | Shared types, domain models, observations, and public schemas |
| Functional | Simulation, domain handlers, policies, verification, KPIs |
| Business | Product objectives, economy, finance, agents, and future planning domains |

Primary paths now use the repository's current layout: `product/types/`, `product/simulation/`, `product/domains/`, `product/agents/`, and `product/interfaces/`.

### Asset Administration Shell

AAS may become relevant when Arcogine represents or exchanges industrial asset identity, properties, capabilities, and digital-twin metadata.

**Design-for rule:** Do not assume every machine property belongs in one fixed Java record forever. Preserve stable identity and a path to extensible, typed asset metadata when a real integration requires it.

**Not current:** Arcogine does not have an AAS adapter, AAS submodels, or a generalized equipment-capability model.

### ISO 9001

ISO 9001 is relevant as a quality-management context rather than a direct software-conformance target.

Arcogine's explicit events, deterministic replay, model versioning direction, observations, and verification goals are compatible with process traceability and continuous-improvement practices. No ISO 9001 certification or quality-management-system claim follows from those architectural properties.

### Functional Mock-up Interface

FMI may be useful for model exchange or co-simulation with other engineering tools.

Arcogine's headless simulation and explicit interfaces make an adapter plausible, but no FMU packaging, FMI lifecycle implementation, or co-simulation timing contract exists today. A future adapter should live under an interface/integration boundary rather than modify the core domain model around FMI types.

---

## Tier 3 — Note for later

### OPC UA / IEC 62541

Relevant when Arcogine ingests live equipment observations or exposes digital-twin information. A future integration must distinguish modeled state, observed external state, and any reconciled twin state.

### MQTT

Potential transport for distributed observations and commands. Transport choice must not weaken authority, ordering, replay, or command/fact boundaries.

### BPMN

Potential notation for business or approval workflows. It should not replace production-operation semantics merely because both can be drawn as flows.

### SCOR

Relevant if Arcogine expands from production into supply-chain planning, sourcing, delivery, and returns.

### FIPA

Relevant only if independently developed autonomous agents need standardized inter-agent communication. Current agents act through Arcogine's controlled observation and event boundaries.

### ISO 8000

Relevant when external master or operational data becomes authoritative input. Data provenance, quality rules, identity, and reconciliation will then require explicit models.

### ISO 10303 / STEP

Relevant for CAD/PLM and detailed product-engineering integration. The current material/product model is intentionally much simpler.

### Industrial fieldbus protocols

Modbus, PROFINET, EtherCAT, and similar protocols are relevant only through later shop-floor gateways or adapters. OPC UA is the more likely first semantic integration boundary; low-level protocol support should not enter the simulation core.

### GDPR

Relevant when Arcogine processes personal data, such as identifiable operator actions or workforce information. Synthetic scenarios alone do not create a GDPR implementation requirement. Real deployments would need purpose limitation, data minimization, access control, retention, and data-subject handling appropriate to the processing context.

### Apache Arrow and Parquet

Potential analytical formats for large event histories, KPI series, experiment results, and comparison datasets. They are storage/interchange choices, not domain models.

---

## Conceptual foundations

These are not standards conformance targets but remain central to Arcogine's credibility:

| Foundation | Relevance |
|---|---|
| Queueing theory | Bottlenecks, waiting, utilization, and capacity analysis |
| Little's Law | Relationship among WIP, throughput, and lead time under stated assumptions |
| System dynamics | Feedback among demand, capacity, policy, and operational outcomes |
| Operations research | Scheduling, optimization, resource allocation, and decision evaluation |
| Discrete-event simulation | Deterministic event ordering and state transitions |

## Summary

| Standard or framework | Tier | Arcogine position |
|---|---|---|
| ISA-95 / IEC 62264 | Align now | Maintained semantic mapping; exact normative source required for profiles or conformance claims |
| ISO 22400 | Align now | KPI terminology/formula reference; map only verified equivalents |
| DES methodology | Align now | Core simulation architecture |
| Queueing theory / Little's Law | Align now | Operational analysis and validation foundation |
| OpenAPI | Align now | Intended machine-readable HTTP contract format; not authoritative yet |
| RAMI 4.0 | Design for | Classification and positioning reference |
| Asset Administration Shell | Design for | Future asset/digital-twin integration path |
| ISO 9001 | Design for | Quality-management context, not certification claim |
| FMI | Design for | Potential interface adapter for model exchange/co-simulation |
| OPC UA / IEC 62541 | Note for later | Live industrial observation and twin integration |
| MQTT | Note for later | Distributed messaging transport |
| BPMN | Note for later | Business/approval workflow notation |
| SCOR | Note for later | Supply-chain expansion |
| FIPA | Note for later | External multi-agent interoperability |
| ISO 8000 | Note for later | External data quality and governance |
| ISO 10303 / STEP | Note for later | CAD/PLM product-data exchange |
| Industrial fieldbus protocols | Note for later | Gateway-level shop-floor connectivity |
| GDPR | Note for later | Personal-data obligations in real deployments |
| Arrow / Parquet | Note for later | Analytical storage and interchange |
