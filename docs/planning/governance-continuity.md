# Governance Identity/History Downstream Compatibility Guard

> **Status:** PLAN-GOV-1 complete; this file exists only to protect downstream implementation from reopening or absorbing the completed identity/history core  
> **Scope:** Implementation invariants for later Governance/Engine/Operational work that references controlled revisions  
> **Authority:** Planning guard only; accepted ADRs and current architecture own the durable semantics

## Completed substrate

PLAN-GOV-1 supplies:

- durable semantic `ModelFingerprint` semantics;
- opaque `ControlledRevisionId` historical identity;
- immutable controlled revision values with current zero-or-one parent lineage;
- recording provenance;
- authoritative append-only acceptance;
- durable reopenable history;
- exact historical semantic-artifact resolution; and
- explicit integrity failures for duplicate/missing/corrupt/mismatched state.

The authoritative chain is:

```text
ControlledRevisionId
    -> immutable accepted ControlledRevision
    -> explicit parent lineage
    -> ModelFingerprint
    -> exact canonical semantic artifact
```

Constructing a value in memory does not create authoritative history. Authority begins only at the accepted Governance authority boundary.

## Invariants downstream implementations must preserve

1. Semantic-content identity and historical-revision identity remain distinct.
2. Equal semantic content may appear in several distinct controlled revisions.
3. Rollback/reversion is a new historical occurrence and may reuse an earlier fingerprint.
4. Accepted revision ID, fingerprint, lineage, and recording provenance are immutable.
5. Authorization, deployment, conformance, evidence, labels, external workflow references, and semantic ChangeSets remain separate records/relationships.
6. Historical resolution uses the recorded immutable artifact, never whichever model happens to be current in memory.
7. A named parent must already be authoritative under the current lineage capability.
8. Revision acceptance must never expose a partially authoritative record.
9. Post-reset retained admission requires the owning authority's declared custody,
   exact definition resolution and support/refusal behavior under
   [ADR-0017](../architecture/decisions/0017-ground-zero-semantic-evolution.md).
   The current file adapter has no declaration enforcement; establishing that
   admission guard is prerequisite to its first retained post-reset use, not a new
   storage project admitted here. An already-accepted undeclared record creates an
   admission defect, never permission to erase the fact or evade its obligations.

## Current adapter boundary

`FileControlledRevisionAuthority` is the current durable adapter proving restart/reopen, atomic append-only acceptance, integrity behavior, and exact artifact resolution.

Its directory layout, binary record format, lock mechanics, and physical artifact key are replaceable implementation details. They are not a selected production database/storage architecture.

The reset withdraws support for pre-reset stores and policies, not the completed
identity/history invariants above. Old V1 verifier/format code may be removed in a
bounded cleanup after its dependents are inventoried; no permanent V1 reader is
required solely because this proving adapter exists.

A hard-to-reverse production persistence/migration/retention/integrity choice requires its own architecture decision when such a concrete implementation is admitted.

## Downstream use

Later Governance evidence/governed-change work, Engine optional revision provenance, and future Operational deployment/reconciliation may reference authoritative `ControlledRevisionId` values. They must consume this substrate rather than create alternate revision identity/history.

This file contains no open lineage/source-control research programme. Branch/ref/tag/multi-parent semantics are not implementation work until a concrete workflow is promoted through the normal research/architecture/planning boundary.
