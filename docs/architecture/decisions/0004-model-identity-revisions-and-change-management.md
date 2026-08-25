# ADR-0004: Model identity, controlled revisions, and change management

Status: Accepted
Date: 2026-08-25

## Context

[ADR-0003](0003-canonical-factory-model-boundary.md) established the canonical factory-model boundary: a validated design is published as an immutable `FactoryModelVersion`, and every runtime or verification context retains provenance identifying the model version it instantiated. ADR-0003 deliberately deferred the identity/version/hash policy itself, along with persistent lineage, authorship, approvals, branches, and change sets, until a concrete requirement justified them.

That deferral is no longer safe to leave unstated, because two different ideas keep getting written as if they were one:

1. **Semantic identity** — whether two published models mean the same production system. This is a content question: given the same canonical facts, Arcogine should be able to derive the same identity deterministically.
2. **Controlled revision identity** — whether a change to the production system was requested, reviewed, approved, and deployed through an accountable process, and how that change relates to prior revisions. This is a governance question, not a content question.

Requirements documents ([`factory-design.md`](../factory-design.md) section 11, [`factory-design-capability.md`](../../planning/factory-design-capability.md) D3) currently bundle "model ID, revision/version, schema version, content hash, publication provenance" into one minimum-identity list. That bundling implies persistent revision lineage, approval state, and an external ticketing key are needed before Arcogine can publish a model at all. They are not: the current implementation need only prove content-derived semantic identity.

Separately, Arcogine is very likely to need an integration with an external organizational change-management system — Jira is the expected first case — once controlled revisions, approvals, and deployment of design changes into real operations become real requirements. That integration is a reference relationship, not a domain dependency: Arcogine must not require a ticketing system's workflow, schema, or terminology to determine what a model *is*.

This ADR draws that boundary explicitly so that fingerprint work, a future revision repository, and any future Jira (or equivalent) integration can be implemented without conflating semantic equality with change accountability.

Related analysis and plans:

- [`../factory-design.md`](../factory-design.md)
- [`../../planning/factory-design-capability.md`](../../planning/factory-design-capability.md)
- [`../../planning/factory-simulation-engine-readiness.md`](../../planning/factory-simulation-engine-readiness.md)

## Decision

Arcogine treats model identity and change management as related but distinct concerns, with the following invariants.

### Semantic identity is a deterministic model fingerprint

A `FactoryModelVersion`'s identity is derived from its canonical semantic content: given equivalent canonical facts, Arcogine derives equivalent identity — a **model fingerprint** — deterministically and without reference to consumer presentation metadata, authorship, timing, or external systems.

Two models with the same fingerprint are the same semantic design. Fingerprint equality is a content-derived fact Arcogine can compute unilaterally; it never requires an external system to establish.

### Controlled revision identity is a separate, future concept

A **controlled revision** — a change that has been requested, reviewed, approved, and deployed through an accountable process, with lineage back to a prior revision — is a distinct concept from a fingerprint. A controlled-revision lifecycle (persistent repository, lineage, approval state, deployment tracking) is deferred capability, to be built only when a concrete requirement (a persistent repository, an external change-management integration, an approval/deployment workflow, an audit requirement, or branching/lineage needs) makes it necessary.

Until that lifecycle exists, Arcogine does not require a model UUID, a revision counter, an approval state, or an external change key to publish a semantically valid model.

### Human `vX` labels are not identity

A human-facing label such as "v3" or "Revision 12" is presentation convenience for a controlled revision, not semantic identity and not a substitute for the fingerprint. Two artifacts sharing a human label are not thereby guaranteed to be the same semantic content, and a fingerprint change does not require incrementing a human label.

### Revision lineage is distinct from semantic equality

Lineage (which revision followed which, who changed what, why) is a change-management fact about the *history* of a design. Semantic equality (whether two designs mean the same thing) is a *content* fact. A later revision may be semantically identical to an earlier one (a revert, a no-op edit); two unrelated designs authored independently may coincidentally share a fingerprint's structure. Neither case collapses one concept into the other.

### Arcogine owns technical configuration-management evidence

Arcogine is authoritative for the model itself, the semantic fingerprint, semantic diff between versions, and the technical assessment/simulation/verification evidence produced about a candidate change. This is Arcogine's configuration-management evidence, and it does not require an external system to exist.

### Organizational change management may live in an external system

The organizational workflow around a change — who requested it, why, who reviewed it, who approved it, and when it is scheduled — may be owned by a system external to Arcogine. Arcogine does not require this workflow to be modeled inside the factory domain, and does not become responsible for request intake, review routing, approval gating, or scheduling merely because a controlled-revision concept exists.

### Jira is the expected first integration, not a domain dependency

Jira (or an equivalent issue tracker) is the anticipated first external change-management system Arcogine integrates with. That expectation must not leak into domain modeling: no domain type, event, or observation may assume Jira's presence, schema, or terminology. The relationship is expressed only as a reference.

### External change references link revisions and evidence to Jira

A controlled model revision, once that concept exists, may carry a stable external reference (for example, a Jira issue key) pointing at the change-management record that authorized it. That reference is metadata attached to the revision; it is not consulted to determine model content, fingerprint, or semantic behavior, and Arcogine's runtime/simulation surfaces do not need to resolve or understand it.

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

External change-management system
    e.g. Jira

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

### Model Jira concepts (issue, transition, approval) directly inside the factory domain

This would make change-management state queryable alongside the model.

It was rejected because it would make Arcogine's domain depend on a specific ticketing product's schema and workflow semantics, contradicting the Product Charter's domain-boundary discipline and locking future integrations (a different tracker, an internal approval tool) out without a domain rewrite.

### Defer this distinction until a revision repository is actually implemented

This would avoid writing an ADR before there is code to constrain.

It was rejected because the ambiguity is already visible in current planning prose (D3's identity list) and in `factory-design.md` section 11, and leaving it unresolved risks the fingerprint implementation and a future revision/Jira integration being designed against conflated requirements.

## Consequences

As a result of this decision:

- fingerprint work can proceed against a precise, minimal target: content-derived semantic identity, nothing else;
- a future controlled-revision capability (persistent repository, lineage, approval, deployment tracking) can be designed and justified independently, triggered by concrete need rather than assumed upfront;
- a future Jira (or equivalent) integration attaches as a stable external reference on a revision, never as a domain dependency;
- planning and architecture documents must stop presenting "model ID/revision/hash/provenance" as one bundled minimum requirement;
- conformance assessments, approvals, simulation runs, and deployments remain distinct, separately referenceable artifacts;
- current-state documentation (`overview.md`) should describe only what exists today: content-derived semantic identity, with no revision repository or change-management integration yet implemented.

The cost is an additional distinction to keep straight when writing about model identity. That cost is intentional: it prevents a ticketing product's workflow from becoming an accidental dependency of what a model *is*.

## Charter alignment

This decision supports the Product Charter's **causality and provenance** principle by keeping provenance (what produced a result) distinct from accountability (who authorized a change), and supports domain-boundary discipline by keeping an anticipated external integration a reference relationship rather than a modeled dependency.
