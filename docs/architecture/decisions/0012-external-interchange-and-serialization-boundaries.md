# ADR-0012: External interchange and serialization boundaries

Status: Accepted
Date: 2026-09-01

## Context

Arcogine now has several durable semantic boundaries that are intentionally independent from any one file format, wire format, transport, broker, or external industrial standard:

- [ADR-0003](0003-canonical-factory-model-boundary.md) establishes `FactoryModel` / immutable `FactoryModelVersion` as the canonical factory-design boundary;
- [ADR-0006](0006-durable-semantic-fingerprint-contract.md) establishes the language-independent `factory-model:v1` semantic fingerprint grammar and explicitly rejects serializer-defined canonicalization as the identity contract;
- [ADR-0008](0008-controlled-revision-identity-and-lineage.md) separates opaque historical `ControlledRevisionId` identity from semantic `ModelFingerprint` identity;
- [ADR-0011](0011-runtime-observation-and-event-contract.md) separates internal scheduler events from supported `RuntimeObservation` current truth and ordered supported `RuntimeEvent` change, while making transports projections rather than runtime-domain dependencies;
- Governance, Challenge/Game, and Operational Execution each own additional semantic histories and projections that must not be collapsed into one generic interchange ontology.

The repository also already uses multiple concrete representations for different purposes:

- TOML is the human-oriented scenario authoring format;
- JSON is used by the current HTTP API and SSE payloads;
- current event export serializes the bounded internal `EventLog`;
- future industrial integrations may need B2MML, AutomationML, AAS/AASX, FMI/FMU, OPC UA, MQTT, IFC, STEP, or other domain-specific representations;
- future analytical consumers may need Parquet, Arrow, CSV, or similar tabular/bulk exports.

Without an explicit boundary policy, a convenient serializer or integration standard could accidentally become the de facto domain model, semantic identity source, runtime event taxonomy, or persistence contract. That would reverse Arcogine's dependency direction and make external compatibility concerns harder to evolve safely.

## Decision

### 1. Arcogine semantic contracts are authoritative; external representations are projections or adapters by default

An Arcogine-owned semantic contract remains authoritative unless a later Accepted ADR explicitly delegates authority to an external format or protocol.

The current authoritative examples are:

```text
FactoryModel / FactoryModelVersion
ModelFingerprint
ControlledRevision
RuntimeObservation
RuntimeEvent
```

Future Governance and Operational Execution records follow the same rule in their owning domains.

External representations therefore map around these semantic contracts:

```text
external representation
        |
        v
mapping / validation / projection adapter
        |
        v
Arcogine semantic contract
```

or, for outbound projections, the reverse direction.

A serializer schema, protocol object model, broker envelope, CAD structure, or standards package does not become Arcogine's canonical ontology merely because an adapter uses it.

### 2. TOML remains a human-oriented scenario authoring format

The current scenario loader and API accept TOML as an input envelope for simulation scenarios. TOML remains appropriate for examples, tests, CLI usage, hand-authored scenarios, and small experiments.

TOML is not the canonical published `FactoryModel` artifact by implication, and a future scenario may reference published model/revision identity rather than embedding every authoritative factory-design fact.

Adding YAML or another parallel human-authoring syntax requires a concrete usability or integration need rather than format preference alone.

### 3. JSON is the default structured external representation, not a semantic-identity mechanism

JSON is the preferred default representation for ordinary external HTTP/API projections and other structured interchange where no stronger domain-specific format is required.

JSON serialization must not define or recanonicalize `factory-model:v1` semantic identity.

`factory-model:v1` remains defined by ADR-0006's normative binary grammar and SHA-256 policy. A JSON representation of the same factory model is a projection of semantic content, not the bytes from which the durable fingerprint is derived.

The same principle applies to future semantic-fingerprint policies unless a later Accepted ADR explicitly defines otherwise.

### 4. OpenAPI describes stable HTTP projections after domain contracts stabilize

OpenAPI is the intended machine-readable description format for stable Arcogine HTTP contracts.

OpenAPI schemas follow accepted Arcogine domain semantics; they do not drive those semantics.

The current manually documented API remains current-state authority until a versioned supported HTTP surface is migrated and OpenAPI generation/validation is deliberately established.

In particular, the Gate 4 `RuntimeObservation` / `RuntimeEvent` domain contract must stabilize before the legacy HTTP/SSE surface is promoted into a durable external compatibility contract.

### 5. Runtime transports and integration envelopes project `RuntimeEvent`; they do not define it

ADR-0011 remains authoritative for runtime-event semantics.

SSE, WebSocket, Kafka, NATS, MQTT, CloudEvents, embedded Java, and future transports or integration envelopes are adapters over the supported runtime contract.

A supported runtime event retains Arcogine semantics such as:

```text
run identity
supported-event sequence
simulation time
semantic event type
model fingerprint
optional authoritative controlled revision
entity references
semantic payload
```

A transport may encode or map those responsibilities differently, but it must not silently discard or redefine them.

CloudEvents is a plausible future integration-envelope profile. It is not the Arcogine runtime-event domain type and is not required for Gate 4.

AsyncAPI may later describe supported asynchronous channels once multiple durable async transports justify a shared machine-readable channel contract. It is not required merely because SSE exists.

### 6. Supported runtime exports derive from supported observations/events, not the internal `EventLog`

The current `EventLog` remains internal scheduler/trace machinery as fixed by ADR-0011.

Future supported runtime-history, experiment, or analytical exports must be derived from supported `RuntimeEvent`, supported `RuntimeObservation`, or another explicitly supported outward contract.

They must not establish durable public semantics by serializing internal `Event`, `EventPayload`, or `EventLog` structures directly.

If retained supported runtime events are introduced, retention/gap/recovery semantics remain separate from the bounded internal trace.

### 7. Bulk analytical formats are projections, not domain models

Parquet is the preferred design-for candidate for durable high-volume analytical export of stable runtime events, observations, KPI series, experiment results, challenge/run comparisons, and similar datasets once their semantic schemas are stable.

Arrow IPC may later support high-throughput analytical/application interchange where its performance characteristics justify the dependency.

CSV remains a human/tooling convenience format for small tabular exports. It is not authoritative for typed/nested semantics, units, identity, provenance, or schema evolution.

### 8. Industrial interchange standards map through the owning semantic boundary

Industrial standards are selected by the semantic boundary they serve, not by a goal of finding one universal Arcogine file format.

Current design-for directions include:

```text
B2MML / ISA-95 exchange
    production/resource/request/performance interchange

AutomationML / IEC 62714
    factory/plant engineering interchange

AAS / AASX
    asset identity, properties, capabilities, and digital-twin metadata

FMI / FMU
    external dynamic model exchange and co-simulation
```

Later trigger-driven directions include:

```text
IFC
    facility/building/spatial context

glTF / GLB
    presentation/rendering assets

STEP / AP242
    detailed CAD/PLM engineering data

OPC UA / MQTT
    operational integration behind Operational Execution semantics
```

An adapter must state exact mappings, approximations, extensions, information loss, source/target version/profile, and compatibility guarantees before Arcogine claims interchange compatibility.

### 9. B2MML is the explicit future ISA-95 exchange candidate

Arcogine's ISA-95 / IEC 62264 alignment remains semantic unless a defined interchange profile exists.

If Arcogine later implements ERP/MES manufacturing-data exchange using ISA-95-derived schemas, B2MML is the explicit design-for candidate for the exchange surface.

B2MML types do not become factory-domain or runtime-domain core types by implication.

### 10. AutomationML is the primary design-for factory-engineering interchange candidate

AutomationML / IEC 62714 is a design-for candidate for future cross-tool plant/factory engineering import/export, including engineering hierarchy, properties, relationships, and associated geometry/logic references where applicable.

The implementation trigger is a concrete external engineering-tool or industrial factory-design interchange requirement.

AutomationML remains an adapter representation around Arcogine's canonical factory semantics; it does not replace `FactoryModel` as the executable semantic source.

### 11. AASX is an explicit asset-package representation, not the canonical factory model

A future Asset Administration Shell adapter may use AAS/AASX packages and submodels to exchange asset identity, properties, capabilities, documentation, and semantic identifiers.

External AAS semantic IDs and asset identities remain distinct from Arcogine semantic identity where their meanings differ. AASX packaging does not define Arcogine controlled revision identity, model fingerprinting, runtime execution state, or deployment truth.

### 12. G1.3 owns durable controlled-revision artifact persistence and exact historical resolution

This ADR does not select the physical persistence representation for authoritative controlled revisions or historical semantic artifacts.

Governance G1.3 remains responsible for durable revision persistence, repository-level lineage integrity, and exact controlled-revision-to-semantic-state/artifact resolution.

A later implementation decision may choose JSON, binary storage, a database representation, content-addressed blobs, or another mechanism. That choice must preserve the already-fixed distinction between:

```text
ModelFingerprint
    semantic content identity

ControlledRevisionId
    historical occurrence identity

stored artifact representation
    persistence/resolution mechanism
```

No storage format may silently redefine either identity.

### 13. Governance interchange formats remain downstream projections

Future governance interoperability may justify formats such as ReqIF, SARIF, W3C PROV, OSCAL, BPMN, or other specialized representations.

Those formats remain projections/adapters over Governance-owned requirements, assertions, evaluations, evidence/evidence-use, findings, governed change, or audit snapshots.

They must not cause a premature generic cross-domain evidence/event/evaluation ontology.

### 14. Stable external representations require explicit versioning and compatibility evidence

A representation becomes a supported compatibility surface only when its owning capability defines:

- schema/profile identity and version;
- semantic source contract;
- required identity/provenance fields;
- field meaning and units;
- ordering rules where relevant;
- compatibility and evolution policy;
- validation behavior;
- golden fixtures, contract tests, or equivalent compatibility evidence.

The existence of a serializer library, generated schema, exported file, or successful protocol connection is not sufficient evidence of a supported interchange contract.

### 15. Avoid a generic interchange ontology or module until real adapters justify shared infrastructure

This decision does not create a universal `ExternalModel`, `InteropEvent`, `InterchangeRecord`, or generic cross-domain mapping ontology.

Adapters should initially live at the owning interface/integration boundary. Shared infrastructure may be extracted later only when several implemented adapters prove a stable common responsibility.

The same rule applies to repository structure: this ADR does not require a new top-level `interop` or `formats` module.

## Format-selection guide

Use the narrowest representation that matches the owning semantic boundary:

```text
Human-authored scenario
    -> TOML

Stable HTTP/API representation
    -> JSON + OpenAPI

Factory semantic identity
    -> ModelFingerprint / factory-model:<policy>:<algorithm>:<digest>

Historical controlled revision identity
    -> ControlledRevisionId

Current supported simulation state
    -> RuntimeObservation projection

Ordered supported simulation change
    -> RuntimeEvent projection

High-volume analytical export
    -> Parquet when the source schema is stable

Industrial interchange
    -> adapter standard selected by domain boundary and concrete integration need
```

## Consequences

Positive consequences:

- serializers and external standards cannot silently become Arcogine's domain authority;
- `factory-model:v1` identity remains independent of JSON/TOML/library behavior;
- Gate 4 runtime-event semantics remain independent of SSE, CloudEvents, brokers, and other transports;
- future supported exports will use outward runtime contracts rather than internal scheduler traces;
- G1.3 can choose artifact persistence independently without reopening semantic or revision identity;
- B2MML, AutomationML, AASX, FMI, Parquet, IFC, glTF, STEP, OPC UA, and other formats have explicit architectural homes and implementation triggers;
- adapters can evolve independently while preserving Arcogine-owned semantic continuity.

Costs and constraints:

- some integrations require explicit mapping layers rather than direct serialization of domain objects;
- a single semantic object may have several legitimate projections for different consumers;
- compatibility requires deliberate versioning and fixtures rather than relying on serializer defaults;
- future adapter work must document information loss and identity/provenance mapping explicitly.

## Alternatives considered

### Make JSON the universal canonical representation

Rejected. Arcogine already has a normative language-independent binary semantic fingerprint contract, and several domains require semantics that should remain independent of one wire representation. JSON remains a useful default projection.

### Define semantic fingerprints by canonical JSON

Rejected. ADR-0006 already fixes the `factory-model:v1` byte grammar and explicitly rejects canonicalization through JSON for the durable fingerprint contract.

### Make CloudEvents the runtime event type

Rejected. CloudEvents is an integration-envelope standard. ADR-0011 establishes Arcogine-owned runtime event semantics that must also support embedded/headless consumers and other transports.

### Promote `EventLog` export into the supported runtime-history contract

Rejected. `EventLog` is a bounded internal scheduler trace and records a different semantic layer from post-authoritative `RuntimeEvent` changes.

### Standardize one industrial format for all factory, asset, runtime, and operational concerns

Rejected. B2MML, AutomationML, AAS, FMI, IFC, STEP, OPC UA, and similar standards address different concerns. A universal external format would collapse domain ownership and produce lossy or misleading mappings.

### Create a generic interchange module immediately

Rejected. There are not yet enough implemented adapters to prove a stable common abstraction, and such a module would encourage premature ontology unification.

## Non-goals

This ADR does not:

- implement any new serializer, adapter, broker, protocol, or export path;
- change current HTTP/SSE behavior or `docs/reference/api.md`;
- choose G1.3 persistence, artifact-retention, database, or repository technology;
- define a new factory-model fingerprint policy;
- add YAML support;
- implement B2MML, AutomationML, AASX, FMI/FMU, Parquet, Arrow, IFC, glTF, STEP, OPC UA, MQTT, ReqIF, SARIF, PROV, OSCAL, or AsyncAPI;
- claim standards conformance or interchange compatibility;
- create a generic event/evidence/evaluation/interchange framework;
- make simulation runtime events production telemetry by implication.

## Charter alignment

This decision supports semantic continuity, determinism, interoperability, verification, and operational trust by keeping Arcogine's executable semantic contracts authoritative while allowing multiple standards-based projections at explicit boundaries. It preserves the Charter's one-model direction without requiring one physical serialization format to own every consumer surface.
