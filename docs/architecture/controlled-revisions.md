# Controlled revisions

Status: Adopted semantic contract; implemented by `:types`/`:governance` value types and `ControlledRevisionAuthority`
Owning architecture: [Governance and Conformance](governance-conformance.md)
Evolution rule: [Semantic identity and evolution](decisions/semantic-identity-and-evolution.md)

## Purpose

Arcogine distinguishes one controlled occurrence of semantic content from another even when the
semantic content is equal. The following history is valid and must remain representable:

```text
Revision A    fingerprint F1
    |
    v
Revision B    fingerprint F2
    |
    v
Revision C    fingerprint F1
```

A and C identify equal semantic content under the same fingerprint policy, but they are not the
same historical revision. C may be a rollback, re-publication, re-application, or another later
controlled occurrence. Reusing A's identity for C would erase the fact that B existed between them
and would make later authorization, conformance, deployment, audit, and evidence records ambiguous.

> **Durable semantic identity is not historical revision identity.**

The revision identifier is independent of human version labels, authorship, timestamps, parents,
external workflow identifiers, approval state, deployment state, and any particular persistence
technology. Those facts may be associated with a revision, but none of them defines which historical
occurrence the revision is.

Controlled revision identity and lineage are a Governance substrate, not a compliance result. They
make exact historical configuration states addressable by later ChangeSets, requirements,
conformance evaluations, evidence uses, authorization records, deployments, exceptions, and audit
projections. A revision's existence does not itself mean that the revision was approved, deployed,
conformant, certified, or compliant with any external framework.

## `ControlledRevisionId` is opaque historical identity

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

## The identifier is a UUID version 4

`ControlledRevisionId` is represented as an RFC 9562 UUID version 4.

UUIDv4 is chosen because it is standardized, cross-language, independently generatable, and carries no model, lineage, actor, workflow, or clock semantics. Canonical textual rendering uses the conventional lowercase hyphenated UUID form.

UUID ordering has no domain meaning. Revision chronology and lineage are explicit facts, not properties inferred from identifier sort order.

Time-ordered identifier schemes are deliberately not used. UUIDv7 and ULID embed timestamp information into the identifier. That would make time partially observable through identity despite the revision already carrying explicit recording provenance, and would conflict with the invariant that revision identity is not derived from time.

A persistence implementation may use an internal sequence, clustered key, or other physical indexing aid for storage locality. Such an internal key is not `ControlledRevisionId` and must not escape as the durable historical identity.

## A controlled revision has one semantic fingerprint

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

There is no generic model/schema-version field on a revision. `ModelFingerprint.policyVersion` already versions the fingerprint semantics. A separate domain model or serialization schema version should be added only when a concrete cross-domain contract requires it; it must not be invented as part of historical revision identity.

## A revision has zero or one parent

A root revision has no parent. A non-root revision has one parent.

The contract is structurally expressed as `parentRevisionIds` so that parent cardinality can be extended later without redefining historical revision identity, but the current capability accepts at most one parent.

```text
root:
    parentRevisionIds = []

current descendant:
    parentRevisionIds = [R1]

not currently supported:
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

The `0..1` parent rule is a capability constraint, not a permanent assertion that controlled history is intrinsically single-parent. Branch refs, tags, multi-parent merge revisions, primary-parent rules, or other source-control-like topology may extend the model through a later reviewed change.

The contract does not define merge semantics, parent ordering for multi-parent revisions, branch names, branch heads, conflict resolution, rebase, or cherry-pick semantics.

## Lineage integrity is explicit

A revision must not name itself as a parent.

When an authoritative revision store accepts a non-root revision, the named parent must already be an accepted revision in that revision authority. Under that append-only acceptance rule, cycles cannot be introduced by ordinary revision creation.

Repository-level parent existence, uniqueness, and cycle integrity belong to the authoritative persistence boundary. A standalone revision value object can enforce local shape invariants such as non-null fields, no self-parent, and parent cardinality, but it cannot by itself prove global graph integrity.

Lineage is independent of fingerprint policy version. A parent and child may reference fingerprints produced under different policy versions when a later migration/evolution contract permits that history; the revision relation itself does not derive from fingerprint equality.

## Rollback is an ordinary new revision

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

## Minimum recording provenance

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

`recordedAt` is explicit provenance and does not participate in `ControlledRevisionId` generation or lineage ordering. The durable persistence capability must preserve its value; exact database/serialization representation and precision are persistence concerns unless a later interoperability contract requires stronger normalization.

## The controlled revision core is immutable

Once an authoritative revision record is accepted, the following facts are immutable:

- `ControlledRevisionId`;
- `ModelFingerprint`;
- parent revision IDs;
- `recordedAt`;
- `recorder`.

The same revision ID must always resolve to the same immutable values.

Corrections must not silently rewrite an accepted revision. If future requirements need correction, supersession, annotation, or administrative repair, those mechanisms must be explicit and separately attributable.

Human labels, tags, descriptions, workflow state, and other presentation or organizational metadata are not part of the immutable identity core. Their own mutability/versioning policy is deferred until required.

## External workflow references are associations, not revision identity/core state

A controlled revision may carry an external change reference, but that relationship is not part of the minimum immutable `ControlledRevision` record.

A later capability may associate a revision with an external authority and identifier, conceptually:

```text
RevisionExternalReference
    revisionId
    externalAuthority
    externalId
```

That relationship may be created after the revision itself and may participate in future governed-change provenance. It never determines model content, fingerprint equality, revision identity, or lineage.

No vendor-specific workflow semantics are introduced.

## Approval, authorization, conformance, deployment, and evidence remain separate records

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

## The revision record does not choose model-artifact persistence

A controlled revision references semantic content through `ModelFingerprint`; the revision record itself does not embed serialized model bytes, an artifact URI, or a content-addressed blob.

Historical change attribution is nevertheless incomplete if Arcogine can identify `R42 -> F1` but cannot recover the exact semantic state represented by F1. The authoritative revision store must therefore resolve an accepted revision to the exact semantic state required for historical reconstruction. The current `FileControlledRevisionAuthority` adapter does so by persisting the immutable canonical artifact whose policy-specific verifier (`SemanticArtifactVerifier`) proves it reproduces the referenced fingerprint; the filesystem layout and locking mechanics are replaceable adapter details, not a selected permanent persistence architecture.

## Authoritative historical identity begins at persistence acceptance

`ControlledRevisionId` is intended to survive process, deployment, and storage boundaries.

Creating a revision-shaped value in memory does not by itself make it an authoritative historical fact. A controlled revision becomes authoritative when its immutable record is accepted by Arcogine's authoritative revision store.

The persistence contract guarantees at least:

- revision-ID uniqueness;
- immutable ID-to-record binding;
- stable revision-to-fingerprint binding;
- stable revision-to-provenance binding;
- parent existence/integrity under the accepted lineage policy;
- durable resolution sufficient for downstream historical use.

These are semantic obligations; the repository API, database, transaction mechanism, retention policy, indexing strategy, and artifact format remain implementation choices.

## Git-like capabilities are not precluded

The contract preserves later paths to concepts analogous to source-control systems without adopting source-control semantics prematurely. A later reviewed change may add:

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
