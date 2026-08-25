# ADR-0004: Model identity, revision lineage, and external change control

Status: Accepted
Date: 2026-08-25

## Context

[ADR-0003](0003-canonical-factory-model-boundary.md) established the canonical factory-model boundary: a validated design is published as an immutable `FactoryModelVersion`, and every runtime or verification context retains provenance identifying the model version it instantiated. ADR-0003 deliberately deferred the identity/version/hash policy itself, along with persistent lineage, authorship, approvals, branches, and change sets, until a concrete requirement justified them.

That deferral is no longer safe to leave unstated, because two different ideas keep getting written as if they were one:

1. **Semantic identity** — whether two published models mean the same production system. This is a content question: given the same canonical facts, Arcogine should be able to derive the same identity deterministically.
2. **Controlled revision identity** — whether a persisted historical configuration state carries change-control provenance and lineage back to a prior state, independent of whether that state has since been approved or deployed. This is a governance question, not a content question.

Requirements documents ([`factory-design.md`](../factory-design.md) section 11, [`factory-design-capability.md`](../../planning/factory-design-capability.md) D3) currently bundle "model ID, revision/version, schema version, content hash, publication provenance" into one minimum-identity list. That bundling implies persistent revision lineage, approval state, and an external ticketing key are needed before Arcogine can publish a model at all. They are not: the current implementation need only prove content-derived semantic identity.

This distinction is not hypothetical: [`FactoryModelVersion.contentHash()`](../../../product/domains/factory/src/main/java/com/arcogine/factory/model/FactoryModelVersion.java) already exists (introduced alongside the canonical `FactoryModel` in the model-boundary implementation), and its own Javadoc is explicit that it is "an internal, in-memory identity policy... not a persisted, public, or cross-process compatibility guarantee." Meanwhile `IntegratedHandler` already carries that content hash as runtime provenance, but `SimResult` does not — so "runtime results identify their source model" is true at the handler layer today and not yet true at the result layer. Any document claiming D3/D4 are simply "done" or that the content hash is already a durable fingerprint contract would overstate what has actually shipped.

Separately, Arcogine is very likely to need an integration with an external organizational change-management system once controlled revisions, approvals, and deployment of design changes into real operations become real requirements. That integration is a reference relationship, not a domain dependency: Arcogine must not require any particular ticketing system's workflow, schema, or terminology to determine what a model *is*. Several such systems exist (issue trackers, PLM/QMS tools, ITSM platforms); this ADR does not pick one, and no example used below should be read as narrowing the field.

This ADR draws that boundary explicitly so that fingerprint work, a future revision repository, and any future external change-management integration can be implemented without conflating semantic equality with change accountability.

Related analysis and plans:

- [`../factory-design.md`](../factory-design.md)
- [`../../planning/factory-design-capability.md`](../../planning/factory-design-capability.md)
- [`../../planning/factory-simulation-engine-readiness.md`](../../planning/factory-simulation-engine-readiness.md)

## Decision

Arcogine treats model identity and change management as related but distinct concerns, with the following invariants.

### Semantic identity is a deterministic model fingerprint

A `FactoryModelVersion`'s identity is derived from its canonical semantic content: given equivalent canonical facts, Arcogine derives equivalent identity — a **model fingerprint** — deterministically and without reference to consumer presentation metadata, authorship, timing, or external systems.

Under the defined fingerprint policy, equal fingerprints denote the same semantic design. Fingerprint equality is a content-derived fact Arcogine can compute unilaterally; it never requires an external system to establish.

Semantic identity and controlled revision identity are distinct. Arcogine may determine semantic identity from canonical model content. A controlled revision is a historical configuration-management state/artifact and may reference an external change-management record. Equal semantic content may therefore occur in distinct controlled revisions.

**The current `FactoryModelVersion.contentHash()` demonstrates content-derived identity but does not yet constitute Arcogine's durable cross-process fingerprint contract.** It is an in-memory, per-process identity policy sufficient to attribute a runtime or result to the model it was instantiated from. Before any document or consumer may rely on it as a durable, persisted, or cross-process-stable fingerprint, the following must be decided and specified:

- **canonicalization rules** — which representation of the model content is hashed, and why that representation is unambiguous;
- **ordering semantics** — today's canonical encoding hashes `resources`, `operations`, and `products` in list order (only `eligibleResources`, a `Set`, is explicitly sorted before hashing); whether `[A, B]` and `[B, A]` are the same factory design or different ones is a semantic decision that has not yet been made, and the hash must not be treated as a stable identity contract until it has;
- **algorithm/format versioning** — whether the fingerprint format is itself versioned, so a future change to the hashing scheme doesn't silently redefine identity for previously published models;
- **compatibility guarantee** — what, if anything, is promised about a fingerprint computed by one process/version remaining comparable to one computed by another.

Until those are specified, `contentHash()` remains a provisional, internal policy — useful for the invariants it already satisfies (attributing one process's runtime/result to the model it came from), not yet a promise of durable semantic identity.

### Controlled revision identity is a separate, future concept

A **controlled revision** is a persisted, controlled historical configuration state: it carries change-control provenance and lineage back to a prior revision. A controlled revision must exist before it can be approved, and must be approved before it can be deployed — approval and deployment are separate lifecycle/evidence records referencing the revision, not properties required for the revision to exist. This must represent both `candidate → approved revision → scheduled → deployed` and `approved revision → never deployed` without contradiction; a revision that is never approved, or approved but never deployed, is still a controlled revision. A controlled-revision lifecycle (persistent repository, lineage, approval state, deployment tracking) is deferred capability, to be built only when a concrete requirement (a persistent repository, an external change-management integration, an approval/deployment workflow, an audit requirement, or branching/lineage needs) makes it necessary.

Until that lifecycle exists, Arcogine does not require a model UUID, a revision counter, an approval state, or an external change key to publish a semantically valid model.

### Human `vX` labels are not identity

A human-facing label such as "v3" or "Revision 12" is presentation convenience for a controlled revision, not semantic identity and not a substitute for the fingerprint. Two artifacts sharing a human label are not thereby guaranteed to be the same semantic content, and a fingerprint change does not require incrementing a human label.

### Revision lineage is distinct from semantic equality

Lineage (which revision followed which, who changed what, why) is a change-management fact about the *history* of a design. Semantic equality (whether two designs mean the same thing) is a *content* fact. A later revision may be semantically identical to an earlier one (a revert, a no-op edit); two unrelated designs authored independently may coincidentally share a fingerprint's structure. Neither case collapses one concept into the other.

### Arcogine owns technical configuration-management evidence

Arcogine is authoritative for the model itself, the semantic fingerprint, semantic diff between versions, and the technical assessment/simulation/verification evidence produced about a candidate change. This is Arcogine's configuration-management evidence, and it does not require an external system to exist.

### Organizational change management may live in an external system

The organizational workflow around a change — who requested it, why, who reviewed it, who approved it, and when it is scheduled — may be owned by a system external to Arcogine. Arcogine does not require this workflow to be modeled inside the factory domain, and does not become responsible for request intake, review routing, approval gating, or scheduling merely because a controlled-revision concept exists.

### External change references link revisions and evidence to an external system

A controlled model revision, once that concept exists, may carry a stable external reference (for example, an issue-tracker key) pointing at the change-management record that tracks/governs the change — whether that record is still proposed, under review, approved, or closed. That reference is metadata attached to the revision; it is not consulted to determine model content, fingerprint, or semantic behavior, and Arcogine's runtime/simulation surfaces do not need to resolve or understand it. Arcogine does not adopt any particular external system's schema, workflow states, or terminology into its own domain model to support this reference — an issue tracker, a PLM/QMS system, or an ITSM platform are all equally valid on the other side of it.

### Conformance, approvals, runs, and deployments remain separate artifacts

Conformance/verification assessments, approval decisions, simulation runs, and deployment records are separate artifacts from one another and from the model itself. They may reference a model fingerprint and, once it exists, a controlled revision, but none of them is the model, and the model does not depend on any of them existing.

Conceptually:

```text
Arcogine
    semantic model
    fingerprint
    revision lineage
    validation
    simulation/verification evidence
    conformance evidence

                 ↕ stable references

External change-management authority
    e.g. an issue tracker, PLM/QMS system, or ITSM platform

    request
    rationale
    review
    approval
    scheduling
    accountability
```

## Alternatives considered

### Bundle model ID, revision, schema version, hash, and provenance into one required identity from the start

This mirrors what the current planning documents literally say and would be simplest to write down.

It was rejected because it forces persistent lineage, approval state, and an implied external-system key onto the very first publication of a model, long before any concrete revision-lifecycle or change-management requirement exists — repeating the mixing ADR-0003 already warned against, one level down.

### Model an external change-management system's concepts (issue, transition, approval) directly inside the factory domain

This would make change-management state queryable alongside the model.

It was rejected because it would make Arcogine's domain depend on one specific external product's schema and workflow semantics, contradicting the Product Charter's domain-boundary discipline and locking future integrations (a different tracker, an internal approval tool) out without a domain rewrite.

### Defer this distinction until a revision repository is actually implemented

This would avoid writing an ADR before there is code to constrain.

It was rejected because the ambiguity is already visible in current planning prose (D3's identity list) and in `factory-design.md` section 11, and leaving it unresolved risks the fingerprint implementation and a future revision/change-management integration being designed against conflated requirements.

## Consequences

As a result of this decision:

- fingerprint work can proceed against a precise, minimal target: content-derived semantic identity, nothing else — with canonicalization, ordering, versioning, and compatibility guarantees specified before `contentHash()` (or a successor) is promoted to a durable fingerprint contract;
- a future controlled-revision capability (persistent repository, lineage, approval, deployment tracking) can be designed and justified independently, triggered by concrete need rather than assumed upfront;
- a future external change-management integration attaches as a stable external reference on a revision, never as a domain dependency, and is not tied to any one vendor or product;
- planning and architecture documents must stop presenting "model ID/revision/hash/provenance" as one bundled minimum requirement, and must not describe D3/D4 as uniformly complete when result-level provenance (e.g. `SimResult`) does not yet carry it;
- conformance assessments, approvals, simulation runs, and deployments remain distinct, separately referenceable artifacts;
- current-state documentation (`overview.md`) should describe only what exists today: content-derived semantic identity with an explicitly provisional durability policy, and no revision repository or change-management integration yet implemented.

The cost is an additional distinction to keep straight when writing about model identity. That cost is intentional: it prevents an external change-management product from becoming an accidental dependency of what a model *is*, and prevents an in-memory identity policy from being read as a stronger guarantee than it currently makes.

## Charter alignment

This decision supports the Product Charter's **causality and provenance** principle by keeping provenance (what produced a result) distinct from accountability (who authorized a change), and supports domain-boundary discipline by keeping an anticipated external integration a reference relationship rather than a modeled dependency.
