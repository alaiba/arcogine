# Governance PLAN-GOV-1 Continuity Notes

> **Status:** PLAN-GOV-1 complete; continuity note retained for downstream Governance work  
> **Scope:** Record the implemented PLAN-GOV-1 identity/history substrate and the interpretation later Governance gates must preserve  
> **Authority:** This note does not supersede ADR-0004, ADR-0006, ADR-0008, the Governance architecture, or the Governance capability plan.  
> **Related:** [Governance and Conformance Capability Plan](governance-conformance-capability.md), [Governance and Conformance Architecture](../architecture/governance-conformance.md), [Standards Alignment](../architecture/standards-alignment.md)

## 1. PLAN-GOV-1 completion state

The repository now has the complete PLAN-GOV-1 substrate:

- PLAN-GOV-1-1: ADR-0006 and `factory-model:v1` provide durable `ModelFingerprint` semantics;
- PLAN-GOV-1-2: `ControlledRevisionId`, immutable `ControlledRevision`, current `0..1` parent lineage, rollback-as-new-revision semantics, and minimum recording provenance are implemented;
- PLAN-GOV-1-3: authoritative controlled-revision acceptance, durable history, repository-level parent integrity, and exact historical semantic-artifact resolution are implemented.

The authoritative chain is now executable across process/reopen boundaries:

```text
ControlledRevisionId
    -> one immutable accepted ControlledRevision
    -> explicit valid parent lineage
    -> one durable ModelFingerprint
    -> immutable recording provenance
    -> exact immutable canonical semantic artifact
```

A `ControlledRevision` value merely constructed in JVM memory is still not authoritative. Authority begins only when `ControlledRevisionAuthority.accept(...)` succeeds.

## 2. PLAN-GOV-1 closure remains a compatibility boundary for downstream records

Later authorization, deployment, result, conformance, evidence, and workflow records may reference `ControlledRevisionId`, but they do not define revision identity and were not circular prerequisites for PLAN-GOV-1 closure.

The dependency direction remains:

```text
PLAN-GOV-1 durable identity + authoritative history
        |
        +--> later ChangeSet / impact records
        +--> later conformance / evidence records
        +--> later authorization / exception records
        +--> later deployment / reconciliation records
```

The immutable controlled-revision core therefore remains limited to historical identity, fingerprint binding, lineage, and recording provenance. External workflow references, labels/tags, `ChangeSet`, authorization, deployment, conformance/evidence, and framework mappings remain separate records.

## 3. Configuration-management and GRC standards relationship

Arcogine's Governance substrate supports disciplined configuration identification and status/history accounting, but it is not itself a certification or compliance claim.

A useful conceptual mapping remains:

```text
configuration identification
        -> ModelFingerprint
        -> ControlledRevisionId

configuration status accounting
        -> authoritative controlled revision history
        -> explicit parent lineage
        -> recording provenance

change control
        -> later ChangeSet / impact / authorization / workflow records

configuration audit / governance verification
        -> later requirements / assertions / evaluations / evidence / findings
```

ISO 10007:2017 remains a useful configuration-management reference; NIST SP 800-53 Rev. 5 CM-3 remains a useful change-control reference; ISO/IEC 27001:2022 remains relevant only as an organizational management-system context. PLAN-GOV-1 does not make Arcogine or any organization conformant, compliant, or certified under those standards.

Git-like history is also not the entire Governance ontology. PLAN-GOV-1 supplies the horizontal immutable configuration-history dimension. PLAN-GOV-2+ add the vertical governance dimensions: change rationale, requirements/conformance, evidence/findings, authorization/exception, and deployment/operational facts.

## 4. PLAN-GOV-1-3 implementation outcome

### 4.1 Identity semantics were not reopened

PLAN-GOV-1-3 preserves ADR-0008 exactly:

- `ControlledRevisionId` remains opaque UUIDv4 historical identity;
- revision identity remains independent of `ModelFingerprint`;
- every revision references exactly one fingerprint;
- distinct revisions may legitimately share one fingerprint;
- current lineage remains zero or one parent;
- rollback/reversion creates a new revision and may reuse an earlier fingerprint;
- accepted ID, fingerprint, lineage, `recordedAt`, and recorder are immutable;
- multi-parent merge semantics, branch/ref/tag semantics, ChangeSets, authorization, deployment, conformance/evidence, and framework mappings remain outside PLAN-GOV-1.

The historical `F1 -> F2 -> F1` case is explicitly covered: the first and third revisions are distinct historical occurrences even though they resolve to the same durable semantic content.

### 4.2 Authoritative acceptance boundary

`:governance` now owns the `ControlledRevisionAuthority` contract. Its acceptance operation is distinct from value construction and exposes supported lookup, deterministic history iteration, and historical resolution.

The first durable adapter is `FileControlledRevisionAuthority`. It is deliberately a narrow JDK-filesystem implementation rather than a production database commitment because the repository had no pre-existing persistence infrastructure to reuse.

Its authority root contains private, versioned storage records conceptually equivalent to:

```text
authority-root/
    authority.lock
    revisions/<ControlledRevisionId>.revision
    artifacts/<internal fingerprint-derived key>.artifact
```

The artifact path key is only a physical adapter detail. The complete `ModelFingerprint` remains the semantic-content identity and is persisted in both the revision/artifact relationship where required.

Revision acceptance is append-only. An existing revision ID is never overwritten, including when the submitted immutable record is otherwise equal. Rebinding the same ID to different immutable content fails deterministically.

### 4.3 Parent and concurrency integrity

A non-root revision may be accepted only after its named parent is already authoritative. A missing parent fails explicitly; self-parenting remains rejected by the PLAN-GOV-1-2 value invariant. Because accepted history is append-only and a parent must pre-exist its child, cycles are structurally unavailable under the current `0..1` model without introducing graph machinery.

Acceptance is serialized by the authority's process lock and durable filesystem lock. Duplicate-ID validation occurs inside that lock and the revision record is installed with an atomic filesystem move, so concurrent conflicting acceptance attempts cannot produce two authoritative bindings.

### 4.4 Semantic artifact storage and exact reconstruction

PLAN-GOV-1-3 reuses ADR-0006's existing released `factory-model:v1` canonical bytes rather than defining a second model serialization format. `FactoryModelArtifactV1` exposes the factory-domain artifact boundary for:

- encoding a published `FactoryModelVersion` to its canonical bytes;
- strict decoding back to the exact semantic model state;
- recomputing the durable `ModelFingerprint` from stored bytes.

The decoder validates the policy prefix, counts, UTF-8, optional floating-point representation, complete input consumption, and canonical re-encoding. A decodable-but-noncanonical artifact is rejected rather than reinterpreted.

The filesystem authority stores immutable semantic artifacts separately from revision records and naturally deduplicates repeated fingerprints. Therefore `Revision A -> F1`, `Revision B -> F2`, `Revision C -> F1` produces three revision records and two immutable semantic artifacts.

Historical resolution is:

```text
ControlledRevisionId
    -> ControlledRevision
    -> recorded ModelFingerprint
    -> exact canonical semantic artifact
    -> FactoryModelArtifactV1.decode(...), for the current factory proving ground
```

It never consults whichever factory model happens to be current in memory.

### 4.5 Fingerprint verification and consistency strategy

Before authority acceptance commits a revision, the supplied artifact must be supported by the configured semantic-artifact verifier, its declared fingerprint must equal the revision fingerprint, and recomputing the durable fingerprint from the canonical bytes must reproduce that same `ModelFingerprint`.

The adapter installs/verifies the immutable artifact before atomically installing the revision record. The revision-record atomic move is the authoritative commit point. Normal failure removes a newly staged artifact before returning. A process failure between artifact installation and revision commit can leave only an unreferenced immutable artifact; it cannot leave an authoritative revision that precedes its artifact. Unreferenced artifacts are not controlled revisions and do not make history authoritative.

The implementation therefore avoids a generic transaction framework while preserving the PLAN-GOV-1 invariant that no partially accepted revision is exposed.

### 4.6 Restart durability and corruption behavior

Reopening a new `FileControlledRevisionAuthority` over the same authority root reconstructs accepted revision metadata, provenance, lineage, and semantic artifacts from durable files. Reopen tests prove that this is not process-memory persistence.

Integrity failures are typed at the Governance boundary. At minimum the implementation distinguishes:

- duplicate revision ID;
- missing parent;
- missing revision;
- missing artifact;
- fingerprint mismatch;
- unsupported artifact policy;
- storage-integrity failure.

Stored revision metadata that cannot be decoded fails explicitly. Missing artifacts fail explicitly. Stored artifacts are reverified during resolution; malformed/corrupt artifacts or fingerprint disagreement fail instead of silently substituting current state, another revision, or repaired content. The authority also refuses to use acceptance of a later same-fingerprint revision as an implicit repair for an already-accepted revision whose artifact has gone missing.

### 4.7 Persistence technology / ADR decision

No new ADR was required for PLAN-GOV-1-3.

The filesystem authority is the smallest current adapter that proves durable reopen semantics, atomic append-only authority, concurrent uniqueness, corruption handling, and canonical artifact resolution without introducing an external service. Its binary record layout, directory names, lock mechanics, and fingerprint-derived physical artifact key are private implementation details rather than a selected production persistence architecture.

The durable semantic artifact itself is not a new format: it is the already accepted ADR-0006 `factory-model:v1` canonical representation. A future PostgreSQL, document, event-store, object-store, or other adapter can implement the same Governance authority contract without changing `ControlledRevisionId` or `ModelFingerprint` semantics. A future hard-to-reverse production storage/migration/retention contract should receive its own ADR when selected.

## 5. PLAN-GOV-1 completion evidence

PLAN-GOV-1 is complete because executable tests now prove, together with the unchanged PLAN-GOV-1-1/PLAN-GOV-1-2 suites:

- durable root acceptance and reopen/restart lookup;
- immutable ID-to-record binding and deterministic duplicate/rebind rejection;
- existing-parent acceptance, missing-parent rejection, and self-parent rejection;
- three distinct historical occurrences for `F1 -> F2 -> F1` and legitimate same-fingerprint revisions;
- exact historical semantic reconstruction independent of current model state;
- resolved-artifact fingerprint equality with the recorded `ModelFingerprint`;
- mismatch rejection and explicit missing/corrupt-artifact failures;
- exact immutable provenance persistence;
- failure without partially authoritative revision state;
- concurrent conflicting duplicate-ID acceptance with exactly one authoritative winner;
- deterministic exposed history ordering;
- first-run/reopen durability;
- unchanged PLAN-GOV-1-1/PLAN-GOV-1-2 identity/value invariants.

The Java quality gate required by `AGENTS.md` passes with compilation, test compilation, Checkstyle, full tests, JaCoCo reporting, and JaCoCo coverage verification.

## 6. What remains deferred

PLAN-GOV-1 completion deliberately does **not** implement PLAN-GOV-2+ concerns: semantic `ChangeSet`, requirements/assertions, conformance evaluation, Evidence/EvidenceUse/findings, authorization/approval workflow, deployment, external workflow integration, labels/tags/branches, multi-parent merges, generic source control/event sourcing, framework mappings, operational telemetry/reconciliation, simulation event persistence, or challenge attempt history.

Those capabilities can now reference stable durable `ControlledRevisionId` values and resolve exact historical semantic state without expanding the immutable revision core.