# External representations and interchange boundaries

Status: Adopted repository-wide representation policy
Related: [Standards Alignment](standards-alignment.md), [Factory Model v1](factory-model-v1.md), [Controlled revisions](controlled-revisions.md), [Runtime contract](runtime-contract.md)

## Purpose

Arcogine has several durable semantic boundaries that are intentionally independent from any one
file format, wire format, transport, broker, or external industrial standard: the canonical
`FactoryModel`/`FactoryModelVersion`, the language-independent fingerprint grammars, opaque
historical `ControlledRevisionId` identity, and the supported `RuntimeObservation`/`RuntimeEvent`
contracts. Governance, Challenge/Game, and Operational Execution each own additional semantic
histories and projections that must not be collapsed into one generic interchange ontology.

Arcogine currently has no scenario loader or human-authored scenario format, and no HTTP API, SSE,
or CLI product surface (see
[Architecture Overview — Outward Adapters](overview.md#outward-adapters)); JSON was used for HTTP
API and SSE payloads in that retired adapter and remains the default structured representation for
a future one (see below), alongside possible future industrial (B2MML, AutomationML, AAS/AASX,
FMI/FMU, OPC UA, MQTT, IFC, STEP) or analytical (Parquet, Arrow, CSV) representations. Without an
explicit boundary policy, a convenient serializer or integration standard could accidentally become
the de facto domain model, semantic identity source, runtime event taxonomy, or persistence
contract.

## Arcogine semantic contracts are authoritative; external representations are projections or adapters by default

An Arcogine-owned semantic contract remains authoritative unless a reviewed architectural decision explicitly delegates authority to an external format or protocol.

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

## Future scenario formats remain open

No current scenario loader or input format is selected. If a future product need requires scenario inputs, choose the format at that time and keep its input envelope distinct from the canonical published `FactoryModel`.

TOML is not a current Arcogine input contract. A future scenario may reference published model/revision identity rather than embedding every authoritative factory-design fact.

Any selected format requires a concrete usability or integration need rather than format preference alone.

## JSON is the default structured external representation, not a semantic-identity mechanism

JSON is the preferred default representation for a future ordinary external HTTP/API projection and other structured interchange where no stronger domain-specific format is required. No such projection currently exists.

JSON serialization must not define or recanonicalize `factory-model:v1` semantic identity.

`factory-model:v1` is defined by the [Factory Model v1 specification](factory-model-v1.md)'s normative binary grammar and SHA-256 policy. A JSON representation of the same factory model is a projection of semantic content, not the bytes from which the durable fingerprint is derived.

The same principle applies to every semantic-fingerprint policy unless a reviewed architectural decision explicitly defines otherwise.

## OpenAPI describes stable HTTP projections after domain contracts stabilize

OpenAPI is the intended machine-readable description format for stable Arcogine HTTP contracts.

OpenAPI schemas follow accepted Arcogine domain semantics; they do not drive those semantics.

Arcogine currently has no HTTP surface to document; a previous manually documented API was retired along with the adapter it described. In particular, the supported `RuntimeObservation` / `RuntimeEvent` domain contract must stabilize before any future HTTP/SSE surface is promoted into a durable external compatibility contract, and OpenAPI generation/validation should be established when that surface is introduced rather than documented by hand again.

## Runtime transports and integration envelopes project `RuntimeEvent`; they do not define it

The [runtime observation/event contract](runtime-contract.md) is authoritative for runtime-event semantics.

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

CloudEvents is a plausible future integration-envelope profile. It is not the Arcogine runtime-event domain type and is not required for the supported runtime observation/event contract.

AsyncAPI may later describe supported asynchronous channels once multiple durable async transports justify a shared machine-readable channel contract. It is not required merely because SSE exists.

## Supported runtime exports derive from supported observations and events

Future supported runtime-history, experiment, or analytical exports must be derived from supported
`RuntimeEvent`, supported `RuntimeObservation`, or another explicitly supported outward contract.
They must not establish durable public semantics by serializing internal `Event` or `EventPayload`
structures directly.

Any future retained supported-event capability needs explicit ownership, retention, and
gap/recovery semantics; internal scheduler processing does not provide those contracts.

## Bulk analytical formats are projections, not domain models

Parquet is the preferred design-for candidate for durable high-volume analytical export of stable runtime events, observations, KPI series, experiment results, challenge/run comparisons, and similar datasets once their semantic schemas are stable.

Arrow IPC may later support high-throughput analytical/application interchange where its performance characteristics justify the dependency.

CSV remains a human/tooling convenience format for small tabular exports. It is not authoritative for typed/nested semantics, units, identity, provenance, or schema evolution.

## Industrial interchange standards map through the owning semantic boundary

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

## B2MML is the explicit future ISA-95 exchange candidate

Arcogine's ISA-95 / IEC 62264 alignment remains semantic unless a defined interchange profile exists.

If Arcogine later implements ERP/MES manufacturing-data exchange using ISA-95-derived schemas, B2MML is the explicit design-for candidate for the exchange surface.

B2MML types do not become factory-domain or runtime-domain core types by implication.

## AutomationML is the primary design-for factory-engineering interchange candidate

AutomationML / IEC 62714 is a design-for candidate for future cross-tool plant/factory engineering import/export, including engineering hierarchy, properties, relationships, and associated geometry/logic references where applicable.

The implementation trigger is a concrete external engineering-tool or industrial factory-design interchange requirement.

AutomationML remains an adapter representation around Arcogine's canonical factory semantics; it does not replace `FactoryModel` as the executable semantic source.

## AASX is an explicit asset-package representation, not the canonical factory model

A future Asset Administration Shell adapter may use AAS/AASX packages and submodels to exchange asset identity, properties, capabilities, documentation, and semantic identifiers.

External AAS semantic IDs and asset identities remain distinct from Arcogine semantic identity where their meanings differ. AASX packaging does not define Arcogine controlled revision identity, model fingerprinting, runtime execution state, or deployment truth.

## Authoritative revision persistence owns durable controlled-revision artifact storage and exact historical resolution

This policy does not select the physical persistence representation for authoritative controlled revisions or historical semantic artifacts.

The Governance-owned authoritative revision persistence capability remains responsible for durable revision persistence, repository-level lineage integrity, and exact controlled-revision-to-semantic-state/artifact resolution.

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

## Governance interchange formats remain downstream projections

Future governance interoperability may justify formats such as ReqIF, SARIF, W3C PROV, OSCAL, BPMN, or other specialized representations.

Those formats remain projections/adapters over Governance-owned requirements, assertions, evaluations, evidence/evidence-use, findings, governed change, or audit snapshots.

They must not cause a premature generic cross-domain evidence/event/evaluation ontology.

## Stable external representations require explicit versioning and compatibility evidence

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

## Avoid a generic interchange ontology or module until real adapters justify shared infrastructure

This policy does not create a universal `ExternalModel`, `InteropEvent`, `InterchangeRecord`, or generic cross-domain mapping ontology.

Adapters should initially live at the owning interface/integration boundary. Shared infrastructure may be extracted later only when several implemented adapters prove a stable common responsibility.

The same rule applies to repository structure: no top-level `interop` or `formats` module is required.

## Format-selection guide

Use the narrowest representation that matches the owning semantic boundary:

```text
Future human-authored scenario
    -> format selected from a concrete product need

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
