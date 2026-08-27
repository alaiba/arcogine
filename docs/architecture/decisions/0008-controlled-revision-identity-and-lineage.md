# ADR-0008: Controlled revision identity and lineage

Status: Accepted
Date: 2026-08-28

## Context

[ADR-0004](0004-model-identity-revision-lineage-and-external-change-control.md) established that semantic identity and controlled revision identity are different concerns. [ADR-0006](0006-durable-semantic-fingerprint-contract.md) then established the first durable semantic fingerprint contract, `factory-model:v1`.

The remaining Governance G1 identity gap is historical identity: Arcogine needs to distinguish one controlled occurrence of semantic content from another even when the semantic content is equal.

For example, the following history is valid and must remain representable:

```text
Revision A    fingerprint F1
    |
    v
Revision B    fingerprint F2
    |
    v
Revision C    fingerprint F1
```

A and C identify equal semantic content under the same fingerprint policy, but they are not the same historical revision. C may be a rollback, re-publication, re-application, or another later controlled occurrence. Reusing A's identity for C would erase the fact that B existed between them and would make later authorization, conformance, deployment, audit, and evidence records ambiguous.

The required invariant is therefore:

> **Durable semantic identity is not historical revision identity.**

The revision identifier must also remain independent of human version labels, authorship, timestamps, parents, external workflow identifiers, approval state, deployment state, and any particular persistence technology. Those facts may be associated with a revision, but none of them defines which historical occurrence the revision is.

At the same time, the first controlled-revision contract should not prematurely implement a source-control system. Arcogine does not yet require branch objects, merge commits, rebase/cherry-pick semantics, generic patching, or a revision DAG with arbitrary parent cardinality. The contract should preserve paths to those capabilities without making them part of G1.2.

Controlled revision identity and lineage are also a Governance and GRC substrate rather than a compliance result. They make exact historical configuration states addressable by later ChangeSets, requirements, conformance evaluations, evidence uses, authorization records, deployments, exceptions, and audit projections. A revision's existence does not itself mean that the revision was approved, deployed, conformant, certified, or compliant with any external framework.

Related documents:

- [ADR-0004: Model identity, revision lineage, and external change control](0004-model-identity-revision-lineage-and-external-change-control.md)
- [ADR-0006: Durable semantic fingerprint contract](0006-durable-semantic-fingerprint-contract.md)
- [Governance and Conformance Architecture](../governance-conformance.md)
- [Governance and Conformance Capability Plan](../../planning/governance-conformance-capability.md)

## Decision

### `ControlledRevisionId` is opaque historical identity

A `ControlledRevisionId` identifies exactly one immutable controlled historical occurrence.

It is not derived from, and must not encode semantic meaning from:

- `ModelFingerprint`;
- a human version/revision label;
- parent revision identity;
- author or recorder identity;
- creation, publication, recording, or deployment time;
- an external issue, ticket, change-request, or workflow identifier;
- approval, authorization, conformance, or deployment state.

Equality of `ControlledRevisionId` means equality of the historical revision record. The same identifier must never be rebound to a different fingerprint, lineage, or required provenance.

### The initial identifier mechanism is UUID version 4

The first `ControlledRevisionId` representation is an RFC 9562 UUID version 4.

UUIDv4 is chosen because it is standardized, cross-language, independently generatable, and carries no model, lineage, actor, workflow, or clock semantics. Canonical textual rendering uses the conventional lowercase hyphenated UUID form.

UUID ordering has no domain meaning. Revision chronology and lineage are explicit facts, not properties inferred from identifier sort order.

Time-ordered identifier schemes are deliberately not used for this contract. In particular, UUIDv7 and ULID embed timestamp information into the identifier. That would make time partially observable through identity despite the revision already carrying explicit recording provenance, and would conflict with the invariant that revision identity is not derived from time.

A persistence implementation may use an internal sequence, clustered key, or other physical indexing aid for storage locality. Such an internal key is not `ControlledRevisionId` and must not escape as the durable historical identity.

### A controlled revision has one semantic fingerprint

The minimum controlled-revision record is conceptually:

```text
ControlledRevision
    id: ControlledRevisionId
    modelFingerprint: ModelFingerprint
    parentRevisionIds: 0..1 ControlledRevisionId
    provenance:
        recordedAt
        recorder
```

Every controlled revision references exactly one `ModelFingerprint`.

The fingerprint answers:

> What canonical semantic content does this revision represent under the named fingerprint policy?

The revision ID answers:

> Which controlled historical occurrence of that semantic content is being referenced?

Therefore:

```text
same fingerprint  does not imply  same revision ID
same revision ID  implies          the same immutable revision record
```

`ControlledRevisionId` generation must not consume or derive from the fingerprint.

A revision does not contain multiple alternate fingerprints for the same semantic state. If Arcogine later needs to relate fingerprints produced under different policies, that relationship must be represented explicitly rather than mutating or multiplying the semantic identity stored in an existing historical revision.

This ADR also does not introduce a generic model/schema-version field. `ModelFingerprint.policyVersion` already versions the fingerprint semantics. A separate domain model or serialization schema version should be added only when a concrete cross-domain contract requires it; it must not be invented as part of historical revision identity.

### G1.2 supports zero or one parent

A root revision has no parent. A non-root revision has one parent.

The contract is structurally expressed as `parentRevisionIds` so that parent cardinality can be extended later without redefining historical revision identity, but the G1.2 capability accepts at most one parent.

```text
root:
    parentRevisionIds = []

current descendant:
    parentRevisionIds = [R1]

not supported in G1.2:
    parentRevisionIds = [R1, R2]
```

Multiple children may reference the same parent, so divergence/branch-shaped history is representable without branch objects:

```text
       R2
      /
R1 --+
      \
       R3
```

The current `0..1` parent rule is a capability constraint, not a permanent assertion that controlled history is intrinsically single-parent. Future branch refs, tags, multi-parent merge revisions, primary-parent rules, or other source-control-like topology may extend the model through a later decision.

G1.2 does not define merge semantics, parent ordering for multi-parent revisions, branch names, branch heads, conflict resolution, rebase, or cherry-pick semantics.

### Lineage integrity is explicit

A revision must not name itself as a parent.

When an authoritative revision store accepts a non-root revision, the named parent must already be an accepted revision in that revision authority. Under that append-only acceptance rule, cycles cannot be introduced by ordinary revision creation.

Repository-level parent existence, uniqueness, and cycle integrity belong to the authoritative persistence boundary. A standalone revision value object can enforce local shape invariants such as non-null fields, no self-parent, and parent cardinality, but it cannot by itself prove global graph integrity.

Lineage is independent of fingerprint policy version. A parent and child may reference fingerprints produced under different policy versions when a later migration/evolution contract permits that history; the revision relation itself does not derive from fingerprint equality.

### Rollback is an ordinary new revision

Rollback does not have a special identity type.

If history moves from semantic content F1 to F2 and later restores F1, Arcogine records a new historical revision:

```text
R1 -> F1
 |
 v
R2 -> F2
 |
 v
R3 -> F1
```

`R3` is distinct from `R1` even though their fingerprints are equal.

Arcogine must not implement rollback by reusing `R1`'s revision ID, mutating `R2`, or moving history backward. Whether a later semantic transition was intentionally a rollback is change rationale and belongs to future ChangeSet/change provenance semantics rather than the minimum revision identity contract.

### Minimum provenance records when Arcogine accepted the revision and who/what recorded it

A controlled revision carries minimum recording provenance:

```text
RevisionProvenance
    recordedAt
    recorder
```

`recordedAt` means:

> the instant at which Arcogine's revision authority accepted the immutable revision record into controlled history.

It is not the time an editor first changed the model, the time a candidate was proposed, an approval time, a deployment time, or an external ticket timestamp.

`recorder` identifies the human, service, agent, import process, or other source that caused Arcogine to record the revision. The minimum contract may represent that identity with a small source/subject value such as:

```text
RevisionRecorder
    source
    subject
```

This is recording provenance, not authorization. A recorder is not thereby an approver, reviewer, owner, or deployer.

`recordedAt` is explicit provenance and does not participate in `ControlledRevisionId` generation or lineage ordering. The durable persistence slice must preserve its value; exact database/serialization representation and precision are persistence concerns unless a later interoperability contract requires stronger normalization.

### The controlled revision core is immutable

Once an authoritative revision record is accepted, the following facts are immutable:

- `ControlledRevisionId`;
- `ModelFingerprint`;
- parent revision IDs;
- `recordedAt`;
- `recorder`.

The same revision ID must always resolve to the same immutable values.

Corrections must not silently rewrite an accepted revision. If future requirements need correction, supersession, annotation, or administrative repair, those mechanisms must be explicit and separately attributable.

Human labels, tags, descriptions, workflow state, and other presentation or organizational metadata are not part of the immutable identity core. Their own mutability/versioning policy is deferred until required.

### External workflow references are associations, not revision identity/core state

This ADR refines ADR-0004's statement that a controlled revision may carry an external change reference: the durable relationship is allowed, but it is not part of the minimum immutable `ControlledRevision` record established by G1.2.

A later capability may associate a revision with an external authority and identifier, conceptually:

```text
RevisionExternalReference
    revisionId
    externalAuthority
    externalId
```

That relationship may be created after the revision itself and may participate in future governed-change provenance. It never determines model content, fingerprint equality, revision identity, or lineage.

No Jira-specific or other vendor-specific workflow semantics are introduced by this ADR.

### Approval, authorization, conformance, deployment, and evidence remain separate records

A controlled revision exists whether or not it is later:

- evaluated for conformance;
- approved or rejected;
- covered by another form of authorization;
- deployed;
- linked to external workflow;
- used by a simulation or runtime;
- cited as evidence.

Those records may reference `ControlledRevisionId` and, where useful, `ModelFingerprint`. None of them is part of revision identity and none is required for the revision to exist.

Controlled revision identity and lineage therefore provide **configuration-history and evidence-addressability primitives**. They do not themselves represent authorization, conformance, certification, deployment state, or compliance with an external framework.

This separation allows a later governance graph such as:

```text
                  Requirement / Assertion
                           |
                           v
R40 -> R41 -> R42 -> R43   Evaluation / Finding
             |             EvidenceUse
             +-----------> Authorization
             +-----------> Deployment
             +-----------> External change reference
```

The horizontal dimension is configuration history. The attached records are governance, evidence, and operational facts about that history.

### The revision record does not choose model-artifact persistence

A controlled revision references semantic content through `ModelFingerprint`; this ADR does not require serialized model bytes, an artifact URI, or a content-addressed blob to be embedded in the revision record.

However, downstream historical change attribution cannot be considered complete if Arcogine can identify `R42 -> F1` but cannot recover the exact semantic state represented by F1. A subsequent G1 persistence/artifact-resolution slice must define how an authoritative controlled revision can resolve to the exact semantic state required for historical reconstruction.

That later work may choose a repository, artifact store, content-addressed store, event store, or another persistence mechanism. ADR-0008 does not select the technology.

### Authoritative historical identity begins at persistence acceptance

`ControlledRevisionId` is intended to survive process, deployment, and storage boundaries.

Creating a revision-shaped value in memory does not by itself make it an authoritative historical fact. A controlled revision becomes authoritative when its immutable record is accepted by Arcogine's authoritative revision store.

The persistence contract must eventually guarantee at least:

- revision-ID uniqueness;
- immutable ID-to-record binding;
- stable revision-to-fingerprint binding;
- stable revision-to-provenance binding;
- parent existence/integrity under the accepted lineage policy;
- durable resolution sufficient for downstream historical use.

This ADR defines those semantic obligations but does not select the repository API, database, transaction mechanism, retention policy, indexing strategy, or artifact format.

### Future Git-like capabilities are not precluded

This decision intentionally preserves later paths to concepts analogous to source-control systems without adopting source-control semantics prematurely.

A future decision may add:

- mutable branch/ref pointers to immutable controlled revisions;
- tags/labels referencing revisions;
- multi-parent merge revisions;
- domain-specific merge/conflict semantics;
- cherry-pick/reapplication semantics through ChangeSets;
- revision-record integrity digests or signatures;
- import/migration mechanisms for externally established history.

Such mechanisms must remain separate from the distinction established here:

```text
ModelFingerprint       = semantic content identity
ControlledRevisionId   = historical occurrence identity
```

The UUID revision ID itself is not a cryptographic commitment to revision contents. If stronger tamper-evident history is required, Arcogine may later hash/sign a canonical revision record while retaining `ControlledRevisionId` as the stable referential identity.

## Alternatives considered

### Derive the revision ID from `ModelFingerprint`

Rejected because equal semantic content may legitimately occur at multiple points in controlled history. A fingerprint-derived revision ID would collapse `F1 -> F2 -> F1` into one historical identity for the two F1 occurrences.

### Use a deterministic/name-based UUID

Rejected because the identifier would necessarily be derived from some combination of content, lineage, actor, time, labels, or external data. Those facts must remain independently modelable rather than becoming revision identity.

### Use UUIDv7

Rejected for the initial durable contract because UUIDv7 embeds a timestamp. Database locality is useful but insufficient reason to make time part of the durable public identifier. Physical storage can add locality independently.

### Use ULID

Rejected for the same semantic reason as UUIDv7: the identifier embeds time. It also provides less platform-native support than UUID across Arcogine's likely implementation environments.

### Use a database sequence as the public revision identity

Rejected because it couples identity allocation to one persistence authority/technology and makes distributed/offline creation harder. A database sequence remains permissible as an internal physical key.

### Make human version labels the revision identity

Rejected because labels are presentation/workflow conveniences, may be renamed or scoped, and do not reliably identify historical occurrences across systems.

### Make arbitrary multi-parent lineage part of G1.2

Rejected because Arcogine does not yet have a concrete merge workflow or semantic merge contract. Supporting arbitrary DAG topology now would force decisions about parent ordering, merge meaning, conflict handling, and ChangeSet semantics before they are required.

The current schema remains structurally extensible through `parentRevisionIds`, while G1.2 enforces `0..1` parents.

### Model rollback as a special revision type

Rejected because rollback does not change what historical identity means. It is an ordinary new occurrence whose semantic content happens to equal an earlier state. Rollback intent belongs to change provenance when that capability exists.

### Put ChangeSet or external workflow references in the minimum immutable revision record

Rejected because those relationships may not exist when a revision is recorded and may be established or corrected later. Making them immutable core fields would either force premature data or require mutation/new revisions for metadata linkage.

### Embed the full semantic model/artifact in `ControlledRevision`

Rejected because historical identity does not require choosing a storage/serialization mechanism. Artifact retention and exact-state resolution are necessary follow-up persistence concerns, but not identity fields.

### Use the revision ID itself as a cryptographic hash of the revision record

Rejected for G1.2 because referential identity and cryptographic integrity are distinct requirements. UUIDv4 provides stable opaque identity; a later integrity digest/signature can commit to canonical revision metadata without making every revision-reference contract content-addressed.

## Consequences

- Arcogine can represent repeated semantic states at distinct points in controlled history without ambiguity.
- `ModelFingerprint` and `ControlledRevisionId` have non-overlapping meanings that downstream governance, runtime, and audit records can retain together.
- The initial revision ID is simple, opaque, standardized, and independent of content, lineage, time, actors, and external workflow.
- Revision records are immutable historical facts once accepted by the authoritative revision store.
- G1.2 can implement the value contracts and local invariants without pretending that an in-memory collection is durable persistence.
- G1.2 supports roots, linear history, and divergence, while merge/multi-parent semantics remain deferred but not structurally foreclosed.
- Rollback/reversion preserves history rather than rewinding or mutating it.
- External change workflow, approval/authorization, conformance, deployment, evidence, labels, and ChangeSets remain separate relationships/artifacts.
- Controlled revision history can serve as an addressable substrate for later GRC and standards mappings without claiming compliance by virtue of revision existence.
- A later persistence decision remains required to establish the authoritative revision store and exact historical semantic-state/artifact resolution.
- A later integrity decision may add canonical revision-record digests/signatures without changing public revision identity.

The costs are an additional identifier alongside the semantic fingerprint, explicit lineage/provenance records, and UUIDv4's lack of natural database ordering. Those costs are preferable to conflating semantic equality with governed history or encoding storage/workflow assumptions into durable identity.

## Non-goals

This ADR does not define:

- a controlled revision repository implementation;
- database, event-store, or artifact-store technology;
- serialized model retention or content-addressed artifact storage;
- generic semantic `ChangeSet` representation;
- branch names, mutable refs, tags, or head selection;
- merge, rebase, cherry-pick, or conflict-resolution semantics;
- approval or authorization policy;
- deployment lifecycle/application mechanics;
- conformance evaluation, evidence, findings, or exceptions;
- external workflow schemas or vendor-specific integrations;
- human revision-label policy;
- cryptographic revision-record digests or signatures;
- Challenge Readiness versioning/identity;
- a shared all-domain evaluation/evidence framework.

## G1 delivery boundary

With this ADR accepted, the G1 work can be split without reopening identity semantics:

```text
G1.1  Durable semantic fingerprint
      established by ADR-0006 and implementation

G1.2  Controlled revision value contract
      ControlledRevisionId
      immutable ControlledRevision
      one ModelFingerprint
      current 0..1 parent lineage
      recording provenance
      rollback-as-new-revision semantics

G1.3  Authoritative persistence and historical resolution
      durable revision repository
      repository-level lineage integrity
      exact revision -> semantic state/artifact resolution
      downstream provenance integration
```

G1 remains incomplete until the G1.3 persistence/resolution obligations required by downstream historical reconstruction are satisfied.

## Charter alignment

This decision supports the Product Charter's causality and provenance direction by making exact historical configuration occurrences independently addressable from their semantic content. It also preserves domain boundaries: semantic content remains owned by the authoritative domain/fingerprint policy, Governance owns controlled revision history and its interpretation, external workflow systems may own organizational process state, and Operational Execution remains authoritative for deployment/application/runtime facts.