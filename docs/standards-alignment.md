# Arcogine — Standards Alignment

Arcogine sits at the intersection of manufacturing systems, digital twins, simulation, industrial data integration, and agent-based decision making. This document maps relevant industry standards to Arcogine's architecture and defines the alignment strategy at each project phase.

## Alignment Strategy

Standards are categorized by when they influence design:

| Tier | Meaning | Action |
|------|---------|--------|
| **Align now** | Affects MVP data model, naming, or API surface | Use standard-compatible naming and structure from the start |
| **Design for** | Does not change MVP code, but the architecture must not preclude it | Document the mapping; avoid design choices that block future adoption |
| **Note for later** | Relevant only in post-MVP expansions | Record in Future Directions |

## Regional Adoption Context (Romania / EU)

Romania's national standards body is **ASRO (Asociația de Standardizare din România)**. Romanian standards are published as **SR** (Standard Român) and are predominantly transpositions of European (EN) and international (ISO/IEC) standards:

- **SR EN** = European standard adopted in Romania
- **SR ISO** = ISO standard adopted in Romania
- **SR EN ISO** = ISO standard adopted via EU harmonization (e.g., SR EN ISO 9001 = ISO 9001)

**Practical consequence:** Arcogine does not need Romania-specific implementations. Alignment with ISO, EN, and IEC standards automatically satisfies Romanian requirements. The standards referenced in this document are applicable in Romania via their SR EN / SR ISO transpositions.

Romania follows **EU Industry 4.0 frameworks**, **RAMI 4.0**, and EU-wide regulations (including GDPR). Romanian industrial and academic practice uses DES simulation, operations research, and queueing theory — all of which are Arcogine's core methodology.

| Standard referenced below | Romanian transposition |
|---------------------------|----------------------|
| IEC 62264 (ISA-95) | SR EN IEC 62264 |
| ISO 22400 | SR EN ISO 22400 |
| ISO 9001 | SR EN ISO 9001 |
| ISO 10303 (STEP) | SR EN ISO 10303 |
| OPC UA (IEC 62541) | SR EN IEC 62541 |
| GDPR (EU 2016/679) | Directly applicable EU regulation |

---

## Tier 1 — Align Now

### ISA-95 / IEC 62264 (Enterprise–Manufacturing Integration)

ISA-95 defines how ERP, MES, and shop-floor systems interact. It standardizes equipment models, production schedules, material flows, and operations definitions.

**Mapping to Arcogine MVP:**

| ISA-95 Concept | Arcogine Equivalent | Crate |
|----------------|---------------------|-------|
| Equipment (Work Unit) | Machine | `sim-factory` |
| Material Definition | Product / SKU | `sim-factory` |
| Process Segment | Routing Step | `sim-factory` |
| Operations Definition | Product Routing | `sim-factory` |
| Operations Schedule | Job Queue / Scenario | `sim-core` |
| Production Performance | KPIs (throughput, lead time, utilization) | `sim-core` |

**MVP commitment:** Arcogine's `sim-types` and `sim-factory` use domain concepts that map cleanly to ISA-95. Typed IDs, routing definitions, and equipment models are designed so that ISA-95-conformant data import/export can be added without restructuring the core model. The TOML scenario schema uses field names that correspond to ISA-95 terminology where practical (e.g., `equipment`, `material`, `process_segment`).

**Not in MVP:** Full ISA-95 XML/B2MML serialization, hierarchical equipment modeling (enterprise > site > area > work center > work unit), personnel models.

### DES Methodology (Discrete-Event Simulation)

DES is the foundational simulation methodology — not a formal ISO standard, but the industry-standard approach for manufacturing simulation, logistics, and operations research.

**MVP commitment:** Arcogine is fundamentally DES-based. The architecture documents this explicitly. Event types, priority-queue scheduling, monotonic time progression, and event causality are all standard DES patterns. This alignment ensures comparability with established tools (AnyLogic, Simio, Arena).

### Queueing Theory / Little's Law

Core mathematical foundations for manufacturing performance analysis.

**MVP commitment:** The KPI system (Phase 3) computes throughput, WIP, lead time, and utilization — the quantities related by Little's Law (L = λW). Scenario acceptance tests validate these relationships empirically. The architecture doc should reference Little's Law as a validation invariant.

### ISO 22400 (Manufacturing Operations Management KPIs)

ISO 22400 defines standardized key performance indicators for manufacturing operations, including formulas, units, and timing semantics. Adopted in Romania as SR EN ISO 22400.

**Mapping to Arcogine MVP KPIs:**

| ISO 22400 KPI | Definition | Arcogine KPI (Phase 3) |
|---------------|------------|----------------------|
| Throughput rate | Good units produced per unit time | Throughput |
| Utilization efficiency | Actual production time / planned busy time | Utilization |
| Production lead time | Time from order release to completion | Lead time |
| Work-in-process inventory | Units started but not yet completed | Backlog / WIP |
| Allocation ratio | Time equipment is allocated / planned busy time | (Derivable from utilization) |

**MVP commitment:** Phase 3 KPI computations (throughput, utilization, lead time, backlog) use definitions and naming consistent with ISO 22400 Part 2. KPI doc-comments reference the ISO 22400 KPI identifier where applicable (e.g., throughput rate is ISO 22400 KPI 1200). This costs nothing and ensures that KPI output is directly comparable with industry-standard reporting.

**Not in MVP:** Full ISO 22400 KPI set (~35 KPIs including OEE, quality ratio, mean time between failure, setup ratio). OEE (Overall Equipment Effectiveness) is a natural post-MVP addition.

### OpenAPI (REST API Specification)

The standard way to define and document HTTP APIs.

**MVP commitment:** **Planned.** API surface documentation currently uses typed client modules and route-level definitions in code, with OpenAPI generation on the roadmap.

---

## Tier 2 — Design For

### RAMI 4.0 (Reference Architecture Model Industry 4.0)

RAMI 4.0 defines six layers for industrial digital systems: asset, integration, communication, information, functional, and business.

**Mapping:**

| RAMI 4.0 Layer | Arcogine Layer |
|----------------|----------------|
| Asset | Physical machine/product models in `sim-factory` |
| Integration | Scenario loader, event log, data stores |
| Communication | REST API (`sim-api`), future OPC UA / MQTT |
| Information | `sim-types` (typed IDs, shared data structures) |
| Functional | Simulation core (`sim-core`), KPIs, event engine |
| Business | Agents (`sim-agents`), economy (`sim-economy`) |

**MVP commitment:** No RAMI 4.0-specific code. The layered crate architecture naturally maps to RAMI 4.0 layers. This mapping is documented here for positioning purposes.

### Asset Administration Shell (AAS)

The standard digital twin representation for industrial assets — describes machines, products, capabilities, and properties in a structured format.

**MVP commitment:** No AAS-specific code. Arcogine's typed machine and product models (`MachineId`, `ProductId`, capabilities, processing times) are designed to be exportable as AAS submodels in a future integration phase. Avoid design choices that would prevent machines from having extensible property sets.

### ISO 9001 (Quality Management Systems)

ISO 9001 defines requirements for quality management: process management, performance tracking, auditability, and continuous improvement. Adopted in Romania as SR EN ISO 9001.

**MVP commitment:** No ISO 9001-specific code. Arcogine's architecture naturally supports ISO 9001 thinking: the append-only event log provides process traceability, KPIs enable performance tracking, deterministic replay supports auditability, and the agent feedback loop models continuous improvement. These alignment points are documented here for positioning — no additional MVP work required.

### FMI (Functional Mock-up Interface)

Standard for co-simulation and model exchange between simulation tools.

**MVP commitment:** No FMI-specific code. Arcogine's headless simulation core (no UI dependency, deterministic execution, scenario-driven) is architecturally compatible with FMI wrapping. The clean separation between the simulation engine and its API surface means an FMU adapter could be built as an additional crate without modifying `sim-core`.

---

## Tier 3 — Note for Later

### OPC UA (Industrial Communication)

Standard protocol for machine data, telemetry, and real-time signals. Critical for connecting a digital twin to real factory equipment.

**Post-MVP relevance:** When Arcogine becomes a digital twin, OPC UA is the primary protocol for ingesting real machine data and mapping simulation state to live systems.

### MQTT (Lightweight Messaging)

Common in IoT and industrial event streaming.

**Post-MVP relevance:** Useful for distributed simulation, real-time event streaming to external consumers, and agent communication in multi-node deployments.

### BPMN (Business Process Model and Notation)

Standard for describing workflows and process flows.

**Post-MVP relevance:** Could model production processes, decision workflows, and agent policies in a standardized, visual notation. Relevant for serious-game and training scenarios.

### SCOR (Supply Chain Operations Reference)

Defines plan, source, make, deliver, and return as supply chain process categories.

**Post-MVP relevance:** Relevant if Arcogine expands into logistics, supply chain simulation, or multi-factory systems.

### FIPA (Foundation for Intelligent Physical Agents)

Defines agent interaction protocols and communication languages.

**Post-MVP relevance:** Relevant for multi-agent negotiation systems where agents from different vendors or frameworks need to interoperate.

### ISO 8000 (Data Quality)

Standard for data correctness and governance.

**Post-MVP relevance:** Critical for digital twin credibility when real operational data enters the system.

### ISO 10303 / STEP (Product Data Representation)

Standard for product data exchange across CAD, PLM, and engineering systems. Adopted in Romania as SR EN ISO 10303.

**Post-MVP relevance:** Relevant if Arcogine simulates product configurations, engineering change workflows, or integrates with PLM systems. Not applicable to the MVP's simplified product/SKU model.

### Industrial Fieldbus Protocols (Modbus, PROFINET, EtherCAT)

Common industrial communication protocols used in Romanian and EU factory environments alongside OPC UA.

**Post-MVP relevance:** Relevant only when connecting to real shop-floor equipment. OPC UA is the primary integration path; fieldbus protocols may be needed for legacy equipment connectivity.

### GDPR (EU General Data Protection Regulation)

EU-wide regulation (directly applicable in Romania, not transposed via SR) governing personal data processing.

**Post-MVP relevance:** Relevant when Arcogine handles real operational data tied to identifiable individuals (employee performance, user behavior logs, operator actions). The MVP operates on synthetic simulation data with no personal data — GDPR does not apply. When real data integration begins: anonymize or pseudonymize personal identifiers, implement access controls, and provide audit trails. Arcogine's append-only event log and command-based mutation model support GDPR-compatible auditability by design.

### Apache Arrow / Parquet

De facto standards for analytical data formats.

**Post-MVP relevance:** Efficient storage and interchange format for simulation results, KPI time series, and scenario comparison data at scale.

---

## Conceptual Foundations

These are not formal standards but are essential to Arcogine's credibility:

| Foundation | Relevance |
|------------|-----------|
| **Queueing Theory** | Bottleneck analysis, waiting time, utilization |
| **Little's Law** | WIP = Throughput × Lead Time — core validation invariant |
| **System Dynamics** | Higher-level feedback modeling (pricing → demand → capacity) |
| **Operations Research** | Scheduling, optimization, resource allocation |

---

## Summary Table

| Standard | Tier | MVP Impact | RO Transposition |
|----------|------|------------|-----------------|
| ISA-95 / IEC 62264 | Align now | Data model naming, scenario schema field names | SR EN IEC 62264 |
| ISO 22400 | Align now | KPI definitions and naming in Phase 3 | SR EN ISO 22400 |
| DES methodology | Align now | Core architecture (already aligned) | — |
| Queueing theory / Little's Law | Align now | KPI validation, documentation | — |
| OpenAPI | Align now | Planned API contract generation; route contracts are manually maintained in code | — |
| RAMI 4.0 | Design for | Documented layer mapping, no code change | EU framework |
| AAS | Design for | Extensible asset model, no code change | EU framework |
| ISO 9001 | Design for | Traceability, auditability (already aligned) | SR EN ISO 9001 |
| FMI | Design for | Headless core architecture, no code change | — |
| OPC UA / IEC 62541 | Note for later | Digital twin data integration | SR EN IEC 62541 |
| MQTT | Note for later | Distributed event streaming | — |
| BPMN | Note for later | Process workflow modeling | — |
| SCOR | Note for later | Supply chain expansion | — |
| FIPA | Note for later | Multi-agent interoperability | — |
| ISO 8000 | Note for later | Data quality for real data | SR EN ISO 8000 |
| ISO 10303 / STEP | Note for later | Product data for PLM integration | SR EN ISO 10303 |
| Modbus / PROFINET / EtherCAT | Note for later | Legacy equipment connectivity | — |
| GDPR (EU 2016/679) | Note for later | Personal data when using real operational data | Directly applicable |
| Arrow / Parquet | Note for later | Analytical data storage | — |
