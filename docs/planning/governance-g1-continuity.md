# Governance G1 Continuity Notes

> **Status:** Planning/continuity note  
> **Scope:** Preserve the remaining non-ADR conclusions needed to continue Governance G1 after G1.2  
> **Authority:** This note does not supersede ADR-0004, ADR-0006, ADR-0008, the Governance architecture, or the Governance capability plan. It clarifies delivery interpretation and records the starting constraints for G1.3 without choosing an implementation technology.  
> **Related:** [Governance and Conformance Capability Plan](governance-conformance-capability.md), [Governance and Conformance Architecture](../architecture/governance-conformance.md), [Standards Alignment](../architecture/standards-alignment.md)

## 1. Why this note exists

The durable architecture is already established in the repository:

- ADR-0004 separates semantic identity, historical revision identity, and external workflow/change-control identity;
- ADR-0006 defines the durable `ModelFingerprint` contract;
- ADR-0008 defines controlled revision identity, current lineage semantics, rollback, minimum recording provenance, immutability, and the persistence boundary;
- G1.1 implements durable semantic fingerprints;
- G1.2 implements `ControlledRevisionId` and the immutable controlled-revision value contract.

The remaining G1 work is G1.3: authoritative persistence and exact historical semantic-state resolution.

This note preserves three conclusions that are useful for that work but are not themselves new architecture decisions:

1. how to interpret G1 acceptance criteria that mention downstream authorization/deployment/result integrations;
2. how the Governance substrate relates to configuration-management and GRC standards without claiming conformance;
3. which G1.3 constraints are already fixed and which implementation choices remain intentionally open.

## 2. G1 closure: downstream integration is a compatibility boundary, not a circular prerequisite

The Governance capability plan includes G1 acceptance criteria stating that:

- authorization and deployment records can independently reference a controlled revision without becoming revision identity; and
- downstream results can retain the semantic fingerprint and, when applicable, the controlled revision ID.

These criteria must be interpreted as **contract-level compatibility requirements**, not as requirements that all downstream capabilities be implemented before G1 can close.

In particular:

```text
G1 must make this possible:

AuthorizationDecision ----> ControlledRevisionId
DeploymentRecord ---------> ControlledRevisionId
Simulation/other result --> ModelFingerprint
                         +-> ControlledRevisionId, when applicable

without moving authorization, deployment, result, or workflow state
into ControlledRevision identity or its immutable core.
```

G1 does **not** require the concrete G6 authorization/change-control workflow, Operational deployment integration, or every downstream producer to be implemented first. Those capabilities depend on G1 and therefore cannot themselves be prerequisites for the existence of the G1 identity/persistence substrate.

For G1 closure, the relevant requirement is that the G1 contracts and authoritative persistence model provide stable, durable identities that later records can reference independently.

Concrete integration acceptance belongs to the capability that owns the integration.

This keeps the dependency direction acyclic:

```text
G1 identity + authoritative history
        |
        +--> later authorization/change-control records
        +--> later deployment records
        +--> later run/result provenance
```

not:

```text
G1
 ^
 |
G6/downstream integration required to complete G1
```

## 3. Configuration-management and GRC standards relationship

Arcogine's Governance substrate is intended to support disciplined configuration history, governed change, conformance, evidence, and auditability. It is **not** itself a certification, compliance framework, or claim of conformance with any external standard.

The useful conceptual mapping is:

```text
configuration identification
        |
        v
ModelFingerprint
ControlledRevisionId

configuration status accounting
        |
        v
controlled revision history
explicit parent lineage
recording provenance

change control
        |
        v
ChangeSet
impact analysis
review / authorization decision
external workflow association
exceptions

configuration audit / governance verification
        |
        v
requirements
assertions
conformance evaluations
Evidence / EvidenceUse
findings
```

### 3.1 ISO 10007

ISO 10007:2017, *Quality management — Guidelines for configuration management*, is a useful configuration-management reference. Its configuration-management process includes configuration identification, change control, configuration status accounting, and configuration audit.

Arcogine's mapping is intentionally architectural rather than a conformance claim:

- `ModelFingerprint` and `ControlledRevisionId` support configuration identification at semantic-content and historical-occurrence levels;
- controlled revision lineage and recording provenance support configuration status/history accounting;
- later `ChangeSet`, authorization, exception, and external-workflow relationships support change-control workflows;
- later requirement/conformance/evidence/finding records support configuration-audit and verification use cases.

The standard does not require Arcogine to collapse those concerns into one object. Keeping revision identity, change rationale, authorization, deployment, and evidence separate is useful precisely because the governance questions are distinct.

### 3.2 NIST SP 800-53 CM-3

NIST SP 800-53 Rev. 5 control CM-3, **Configuration Change Control**, is a useful security/control reference for managed configuration changes. Its change-control concerns include documenting proposed and completed changes, defined approval/authorization, preventing unauthorized changes where policy requires it, notification, and auditing changes before and after implementation.

Arcogine should support those concerns through composable records rather than by turning a `ControlledRevision` into an approval object:

```text
ControlledRevision
        |
        +--> ChangeSet
        +--> ConformanceEvaluation / Finding
        +--> AuthorizationDecision
        +--> DeploymentRecord
        +--> ExternalWorkflowReference
```

A revision can therefore exist as an immutable historical occurrence without implying that it has been approved, authorized, deployed, or found conformant.

### 3.3 ISO/IEC 27001

ISO/IEC 27001:2022 defines requirements for an information security management system. Arcogine's controlled-revision and governed-change machinery may support an organization's implementation and evidence around managed configuration/change controls, but Arcogine must not describe the G1 substrate as an "ISO 27001 implementation" or treat the existence of revision history as evidence that an organization is compliant or certified.

The relationship is therefore:

```text
Arcogine governance primitives
        |
        v
possible control implementation / evidence substrate
        |
        v
organization-specific ISMS/control interpretation
        |
        v
possible external conformity assessment
```

not:

```text
ControlledRevision exists
        -> organization is ISO/IEC 27001 compliant
```

### 3.4 Git history is not the whole GRC model

Git-like history and Governance answer overlapping but different questions.

A source-control history primarily helps answer what changed in versioned objects and how historical states relate. Governance additionally needs to answer questions such as:

- was the change permitted under the applicable policy?
- what semantic/business impact did it have?
- what requirements applied?
- what evidence was evaluated?
- what findings existed?
- who or what authorized progression when authorization was required?
- what was actually deployed and where?

Arcogine may borrow useful historical topology concepts without making a source-control graph the entire governance ontology.

A useful mental model is:

```text
horizontal dimension
    immutable configuration history

vertical dimension
    change rationale
    requirements / conformance
    evidence / findings
    authorization / exception
    deployment / operational facts
```

## 4. G1.3 starting constraints

G1.3 starts from decisions that are already fixed by ADR-0008 and the G1.2 implementation. It should not reopen them merely because persistence is now being designed.

### 4.1 Already fixed

G1.3 must preserve these invariants:

- `ControlledRevisionId` is opaque UUIDv4 historical identity;
- revision identity is independent of `ModelFingerprint`;
- every controlled revision references exactly one `ModelFingerprint`;
- equal fingerprints may occur in distinct controlled revisions;
- current lineage supports zero or one parent, while single-parent lineage is not declared a permanent architectural invariant;
- rollback/reversion creates a new revision occurrence and may reuse an earlier fingerprint;
- revision ID, fingerprint relationship, lineage, `recordedAt`, and recorder are immutable once the authoritative revision record is accepted;
- external workflow references, labels/tags, `ChangeSet`, authorization, deployment, conformance/evidence, and framework mappings remain outside the minimum immutable revision core;
- branch refs, tags, multi-parent merge semantics, rebase/cherry-pick semantics, and cryptographic revision-record integrity remain deferred unless a concrete requirement activates them.

### 4.2 What makes a revision authoritative

A `ControlledRevision` value existing in process memory is not yet an authoritative historical fact.

G1.3 must establish a boundary equivalent to:

> A controlled revision becomes authoritative when Arcogine's revision authority accepts its immutable record into the authoritative revision store.

Acceptance must establish at least:

- durable revision-ID uniqueness;
- stable `ControlledRevisionId -> immutable revision record` binding;
- durable revision-to-fingerprint binding;
- durable recording-provenance binding;
- parent existence under the current lineage policy;
- repository-level lineage integrity;
- behavior that prevents silent mutation/rebinding of an accepted historical ID.

Under the ADR-0008 acceptance contract, G1.3 must require a named parent to have already been accepted before a descendant can be accepted. With append-only acceptance and the current `0..1` parent policy, that rule can make cycles structurally impossible; global graph checks should be introduced only if the chosen persistence semantics require them.

### 4.3 Exact historical semantic-state resolution is part of G1.3

Persisting only this mapping is insufficient:

```text
ControlledRevisionId -> ModelFingerprint
```

Later `ChangeSet`, conformance, evidence attribution, audit reconstruction, and operational reconciliation need to answer:

```text
ControlledRevision
        |
        v
exact semantic state/artifact represented by its ModelFingerprint
```

G1.3 must therefore establish an authoritative resolution path from a controlled revision to the exact semantic state/artifact needed for historical reconstruction.

Possible mechanisms include, but are not yet selected:

- a content-addressed semantic artifact store;
- an authoritative canonical-model repository with fingerprint lookup;
- retained immutable published model artifacts plus a resolver;
- another storage/resolution mechanism that proves the same invariants.

The implementation must ensure that historical semantic reconstruction does not depend on whatever mutable "current model" happens to exist later.

### 4.4 Hard-to-reverse choices that remain open

G1.3 has **not** yet decided:

- PostgreSQL vs. document database vs. event store vs. another persistence technology;
- repository API shape;
- transaction mechanics;
- physical/internal indexing keys;
- model-artifact serialization format;
- artifact/blob storage technology;
- retention and archival policy;
- migration strategy;
- canonical revision-record digest/signature policy;
- multi-parent merge semantics;
- branch/ref/tag semantics.

An internal database sequence or clustered key may be used later for physical efficiency without changing the public `ControlledRevisionId` contract.

A follow-up ADR is appropriate if the G1.3 implementation commits Arcogine to a hard-to-reverse persistence, artifact-resolution, integrity, or migration contract.

## 5. Completion boundary

After G1.3, G1 can be considered complete when the repository can prove the following without relying on process memory:

```text
revision ID
    -> one immutable accepted historical record
    -> explicit valid parent lineage
    -> one durable semantic fingerprint
    -> immutable recording provenance
    -> exact historical semantic state/artifact
```

At that point later capabilities can safely build on durable history:

```text
G1 complete
    |
    v
G2 semantic ChangeSet / impact
    |
    v
G3-G5 requirement, conformance, evidence
    |
    v
G6+ governed change / authorization / integrations
```

Downstream authorization, deployment, and result-provenance integrations then validate their own end-to-end use of G1 identities in their owning capability slices; they do not retroactively define what a controlled revision is.
