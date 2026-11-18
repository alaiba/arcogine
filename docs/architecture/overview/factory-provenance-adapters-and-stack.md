# Factory model and runtime provenance, outward adapters, and technology stack

Part of the [architecture overview](../overview.md#routed-topics); it is mandatory whenever the overview routes your change here.

## Factory model and runtime provenance (current state)

The runtime retains the source `ModelFingerprint` and opaque per-runtime `RunId`. The fingerprint
identifies canonical Factory content in the current producing context; `RunId` is correlation only.
The runtime executes the current result-affecting [Engine definition](../engine-semantics.md), but no
dedicated Engine-definition identifier is exposed today. That omission is intentional until a
concrete boundary requires an exact resolvable reference.

Factory runtime semantics are instantiated through the canonical-model seam: `FactoryModel` (validated) → `FactoryModelVersion` (immutable, published) → `FactoryRuntimeAssembler` (deterministic runtime instantiation). See [Factory Design](../factory-design.md#4-canonical-model-boundary) for the boundary this implements. `FactoryModel` holds the production records, and `FactoryRuntimeAssembler` instantiates every published model.

`FactoryModelVersion.fingerprint()` implements the current canonical form specified by the [Factory model](../factory-model.md): one aggregate `ModelFingerprint` rendered as `factory-model:sha256:<digest>` over a language-independent binary encoding of the production records, pinned by golden vectors for the current definition. Equal canonical content has an equal fingerprint independent of process memory and implementation language under the same definition; the fingerprint does not identify the exact definition revision that produced it.

The supported runtime observation/event contract supplies opaque per-runtime `RunId` and the
source-model `ModelFingerprint` on `RuntimeObservation`. It does not currently invent an Engine
definition identifier; exact Engine-definition provenance remains a separately triggered concern.

`:types` provides opaque UUIDv4 `ControlledRevisionId`; `:governance` owns the immutable
`ControlledRevision`, lineage and recording-provenance values and the
`ControlledRevisionAuthority` acceptance/resolution port. `:storage` implements that port through
the public `ArcogineStorage` contract and `BuiltInStorage.open` composition point. Its concrete
filesystem implementation is private. Successful acceptance fixes the ID, fingerprint, lineage,
artifact and authority-owned recording time; duplicate IDs fail. Resolution recovers the exact
accepted artifact rather than current model state. [Storage](../storage.md) owns the implemented
opening, integrity, concurrency and durability scope; [controlled revisions](../controlled-revisions.md)
owns historical meaning.

The current built-in provider keeps its legacy on-disk naming privately and refuses foreign,
incomplete, differently definition-bound or pre-strict-text-encoding roots without adopting or
deleting them. The
Factory `SemanticArtifactVerifier` supplies a conservative compiled-definition binding and checks
canonical bytes and fingerprints. That binding is not an exact semantic-definition archive or a
proof of cross-build equivalence. Factory and Engine meanings remain development definitions;
product-owned physical storage does not promote them or promise historical interpretation after a
definition change. Distinct revisions can share one fingerprint and artifact, including the
`F1 -> F2 -> F1` rollback case, without becoming the same occurrence.

The Governance semantic change/impact capability provides `ChangeSet`/`SemanticChange`/`ImpactScope`
in `:governance`; Factory's `FactoryModelSemanticComparator` implements the domain comparison port.
Governance also owns requirements/assertions, deterministic conformance evaluation, evidence
references and evidence use. The headless evidence acceptance boundary remains in memory; Storage
does not yet persist evaluation or evidence history. Approval, authorization, deployment, external
change-management relationships, labels/tags/branches and multi-parent merge semantics remain
later Governance concerns.

## Outward Adapters

Arcogine's one outward adapter is the Last Light Foundry game consumer: a JSON-over-HTTP adapter and bundled browser UI, documented in [Last Light Foundry integration](../last-light-foundry.md). There is no other HTTP API, CLI, or UI. See [Event Dispatch Architecture](simulation-modules-and-types.md#event-dispatch-architecture) for the current internal handler boundary and [Domain observations vs. outward projections/DTOs](events-state-observations.md#domain-observations-vs-outward-projectionsdtos) for the boundary it must maintain. A further consumer is introduced from the supported [runtime contract](../runtime-contract.md) when a concrete product need exists, not spun up preemptively.

## Technology Stack

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Language | Java (release 21 compatibility baseline) | Records, sealed types, pattern matching |
| Build | Gradle (Kotlin DSL, repository wrapper) | Multi-module build; exact version pinned by `product/gradle/wrapper/gradle-wrapper.properties` |
| Testing | JUnit 6 | Unit and acceptance tests |
| Coverage | JaCoCo | Code coverage reporting |
