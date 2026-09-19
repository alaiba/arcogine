# Semantic Contract Maturity and Durability Research

> **Lifecycle:** CONCLUDED — see the maintained [research register](../research-register.md)
> **Scope:** Semantic identity, support commitments and the ground-zero transition
> **Authority:** Research provenance only; the Accepted decisions below own the result.
> **Risk:** High; independent adversarial review concluded ACCEPT WITH QUALIFICATIONS before reconciliation.

## Conclusion and durable destinations

The qualified result is reconciled into
[ADR-0017](../../architecture/decisions/0017-ground-zero-semantic-evolution.md)
(reset/evolution, superseding ADR-0006),
[ADR-0018](../../architecture/decisions/0018-factory-semantics-after-support-reset.md)
(Factory consequences, superseding ADR-0014), the
[Architecture Overview](../../architecture/overview.md#semantic-evolution-and-support)
and [support policy](../../development/semantic-contract-support.md).
Those authorities carry the decision; this brief preserves framing and transfer history.

Universal whole-contract promotion and exercised-section freezing were not adopted.
The result instead separates non-rebinding identity from scoped support, with a
whole-definition attribution boundary and an explicit owner-directed support reset.
The reviewed qualifications are all carried: dated complete-estate declaration and
reopening condition; strict label non-reuse; enduring doctrine separated from the
checklist; enumerable acceptance/custody and defect handling; definition-retention
floor; whole-definition freeze; seven-contract withdrawal/invariant/cleanup/open-question
classification; distinct authority placement; and supersession of both Factory ADRs.

### Remaining questions and reopening triggers

Factory composition remains [its own investigation](factory-model-semantic-composition.md).
[Engine evolution](engine-evolution.md) owns possible post-attribution same-label
amendment; [simulation analytics](simulation-analytics-consumer-boundary.md) owns
analytical provenance; [Operational boundaries](operational-execution-digital-twin-boundaries.md)
and the register retain closure/retirement, audit horizon, trust and correspondence.
Concrete retained-custody mechanics are bounded implementation design unless new
equality/acceptance/accountability uncertainty requires research. ADR-0016 retains
its separate representation/retention deferrals. None is settled by the reset.

An excluded pre-reset use surfacing invokes ADR-0017's stop/inventory/explicit-decision
rule. Reopen the general model only if a concrete accepted use cannot satisfy its
accountability through scoped support, or an evolving identity cannot preserve its
declared fixed aspects. A proven Engine amendment need could reopen the bounded
Engine question; process cost alone calls for simplifying the support checklist.

### Knowledge transfer

The finite semantic-evolution reconciliation covers the reviewed ground-zero result
and the earlier maturity candidate it replaces. Accepted principles, every review
qualification and current-contract consequences have the authorities above as their
destinations. Reusable proving/failure cases, declaration information, the
obligation-versus-fulfilment distinction, and the limits of in-tree absence evidence
live in the support policy. ADR-0017 retains the rejected whole-contract lifecycle,
section-freeze and unnecessary epoch-service alternatives. Existing Engine dispatch
evidence remains in its owning brief.

No exact report, review or source artifact requires post-retirement readability:
their decision-relevant knowledge has been transferred. No synthesis seed is admitted;
the candidate signals are evidence-bearing and potentially transferable, but none
is loss-sensitive once the direct destinations above exist. Existing seeds are not
new independent observations merely because this work reuses their distinctions.
Draft/search chronology, prior superseded recommendations, prompts and temporary
report/review files are explicitly discarded after transfer. The reconciliation PR
records the workspace coverage and exact active-custody evidence coordinates for
independent review; maintained semantics do not depend on their future fetchability.
Workspace retirement requires the reconciliation to land and independent PR review
to validate the transfer. No research artifact is promoted wholesale into `docs/`.

The original brief follows as historical investigation framing, not current authority.

## Question

> When should Arcogine promote a currently authoritative semantic contract into a durable immutable contract, what evidence must precede that promotion, and what compatibility/provenance obligations apply before versus after promotion?

The investigation must distinguish at least these two questions:

1. **Is a particular published semantic artifact immutable?**
2. **Has Arcogine committed to preserving the meaning of the semantic contract that defines such artifacts?**

The first may need to be true immediately for provenance and reproducibility. The second is the maturity/durability question this investigation must resolve.

## Decision at stake

Arcogine currently has several places where introduction, normative authority, version naming, and durability are close enough that a newly accepted contract can become permanently immutable before its implementation and consumers have had much opportunity to challenge its shape.

That is especially visible in:

- [ADR-0006](../../architecture/decisions/0006-durable-semantic-fingerprint-contract.md), which established `factory-model:v1` as a durable fingerprint contract;
- [ADR-0014](../../architecture/decisions/0014-factory-model-semantic-policy-evolution.md), which treats released Factory model policies as permanently immutable and therefore introduces `factory-model:v2` for spatial authored facts;
- [ADR-0015](../../architecture/decisions/0015-engine-semantics-identity-and-reproducibility.md), which makes outcome-affecting Engine interpretation changes require a new `EngineSemanticsVersion`;
- [Engine Semantics v1](../../architecture/engine-semantics-v1.md), which pins the first named Engine interpretation before Arcogine has an external supported product release; and
- the current [Spatial Runtime Consequences](../../planning/spatial-runtime-consequences.md) plan, which would deepen V1/V2 coexistence and permanent historical-policy support if completed unchanged.

The repository also contains a materially different precedent: [ADR-0012](../../architecture/decisions/0012-external-interchange-and-serialization-boundaries.md) says stable outward HTTP/OpenAPI compatibility should be promoted only **after** the underlying supported domain contract stabilizes. The research operating model itself similarly promotes only conclusions that survive bounded proving evidence and, for high-risk questions, independent adversarial review.

The decision is whether semantic-contract durability should itself have an explicit evidence-gated promotion lifecycle rather than arise automatically from introduction, ADR acceptance, internal publication, or the first need for a deterministic identifier.

## Current repository facts to preserve during the investigation

The investigation starts from, and must not silently rewrite, these current facts:

- Accepted ADRs remain current architecture until superseded through the ADR process.
- A published `FactoryModelVersion` is immutable as a particular authored design; changing the design produces another identified model artifact.
- `ModelFingerprint` identifies authored semantic content, while `ControlledRevisionId` identifies a historical controlled occurrence.
- `EngineSemanticsVersion` exists to distinguish authored Factory facts from result-affecting Arcogine interpretation.
- Provenance/reproducibility require exact attribution even while a contract is still being proved.
- The repository has no authority to infer external compatibility obligations merely from the existence of internal test/proving artifacts. The investigation must verify whether any real durable constituency exists for each contract it evaluates.
- Once a contract genuinely becomes durable, historical meaning must not be silently rewritten merely for implementation convenience.

## Candidate lifecycle models to test

Do not assume the session hypothesis is correct. Evaluate at least the following candidates.

### Candidate A — current eager durability

A semantic contract becomes durable when its defining ADR/specification is accepted or when Arcogine first publishes/persists artifacts under it.

This is closest to the current Factory/Engine approach.

### Candidate B — explicit maturity promotion

A semantic contract may be normative/current while still provisional. It becomes durable only through a later, separate promotion after implementation, consumer use, conformance evidence, and dedicated research demonstrate sufficient maturity.

The introduction decision and the durability-promotion decision are never the same step.

Possible labels such as `PROVISIONAL`, `PROVING`, and `DURABLE` are hypotheses only; the research must determine whether explicit states are useful and what they mean.

### Candidate C — boundary-specific durability triggers

Different kinds of contracts earn durability through different evidence. Persisted semantic identity, supported outward compatibility, execution semantics, and governance evidence may need distinct promotion criteria rather than one universal lifecycle.

This candidate may still share repository-wide minimum rules.

### Candidate D — release-bound durability

Contracts remain freely evolvable until a named supported product/release boundary, after which versions become durable.

The investigation must test whether a release milestone alone is sufficient evidence or whether this merely moves the same premature decision to another administrative event.

### Candidate E — another narrower model

The researcher may introduce another candidate if repository evidence exposes a materially different answer. Do not manufacture extra states or frameworks merely for symmetry.

## Required proving cases

Apply each candidate to at least these cases.

### 1. Factory semantic fingerprint policy

Use `factory-model:v1`, the current V2 design, `ModelFingerprint`, controlled revisions, exact historical artifact resolution, and golden vectors.

Test the difference between:

- immutable individual published artifacts;
- reproducible development/proving artifacts;
- a permanently supported fingerprint-policy meaning; and
- a real compatibility constituency that must survive upgrades.

### 2. Engine semantics

Use `engine-semantics:v1`, its conformance fixtures, current dispatch/ranking behavior, and the fact that the implementation now exposes one fixed semantics identity.

Test whether current behavior can be authoritatively characterized during proving without already committing Arcogine to preserve that interpretation forever.

### 3. Supported runtime contract versus outward transport compatibility

Use ADR-0011/ADR-0012 and current API/SSE convergence planning.

This case must explain why the repository already tolerates a stabilization period before promoting an outward compatibility contract and whether that principle is transferable to semantic identity/version contracts.

### 4. Governance evidence and other newly accepted durable concepts

Use ADR-0016 and at least one other recently accepted identity/provenance contract.

Test whether an Accepted architectural decision necessarily implies the represented contract is already a permanent compatibility commitment, or whether ADR acceptance and contract durability are orthogonal states.

### 5. Future Operational execution

Use the Operational architecture's stronger audit/trust/continuity needs.

Test whether real external consequence creates a stronger durability trigger than simulation-only proving artifacts and whether the lifecycle still preserves exact provenance before promotion.

### 6. A contract that changes during proving

Construct a realistic case where implementation and multiple clients reveal a missing field, wrong equality rule, or incorrectly owned responsibility before external release.

Compare the cost and truthfulness of:

- amending the provisional contract;
- minting a permanent v2 solely because v1 was named too early; and
- migrating/rebaselining internal proving artifacts.

## Questions the report must answer

The final report must resolve, with evidence:

1. What exactly does **normative/current** mean before durability?
2. What event makes a semantic contract **durable**, if any?
3. Must introduction and durability promotion be separate decisions?
4. What minimum implementation evidence is required before promotion?
5. What consumer evidence is required, and does one originating consumer suffice?
6. Is an independent consumer required, preferred, or irrelevant for different contract classes?
7. What conformance/fixture evidence is necessary?
8. What dedicated research question must be answered before promotion — specifically, how Arcogine decides whether the contract has proved mature enough to freeze?
9. What can happen to provisional persisted artifacts when the contract changes: invalidation, migration, rebaselining, retention, or something else?
10. How are exact historical facts preserved without converting every development artifact into a permanent compatibility promise?
11. What relationship should exist among ADR status, semantic-contract maturity, implementation release, and external compatibility support?
12. How should identifiers be named before durability so a provisional contract is not accidentally represented as a permanent `v1`?
13. Once durability is earned, which immutability rules remain as strong as today?
14. Which current Arcogine contracts, if any, were promoted prematurely under the resulting rule?
15. If current `factory-model:v1/v2` or `engine-semantics:v1` are classified as pre-durable proving contracts, what is the truthful correction path given the current repository-controlled ecosystem?
16. What concrete evidence would falsify the conclusion and justify reopening it?

## Evidence requirements

### Repository evidence

At minimum inspect:

- Product Charter lifecycle-continuity and provenance principles;
- ADR-0003, ADR-0004, ADR-0006, ADR-0011, ADR-0012, ADR-0014, ADR-0015, ADR-0016;
- Factory Design architecture and Factory Model v2 specification;
- Engine Semantics v1 specification and current conformance fixtures;
- Factory/Engine/Governance implementation and tests that persist or consume versioned identities;
- current planning that requires V1/V2 coexistence or semantics-version propagation;
- research operating policy and at least one concluded high-risk investigation showing the intended prove/review/reconcile lifecycle; and
- repository/history evidence sufficient to determine whether a compatibility constituency exists beyond repository-controlled development/proving artifacts.

### External evidence

External evidence is optional, not a quota. Use it only if it materially discriminates among candidates, for example:

- standards or protocol processes that distinguish draft/provisional status from stable compatibility;
- schema or language ecosystems with explicit stabilization/promote/deprecate rules; or
- evidence about costs/failure modes of freezing a semantic contract before real client use.

Do not import another project's release taxonomy merely because it is familiar.

## Falsification criteria

The explicit-maturity hypothesis should be rejected or narrowed if evidence shows, for example, that:

- exact provenance cannot remain truthful without immediate permanent contract immutability;
- pre-durable migration necessarily destroys a historical fact Arcogine must preserve;
- the supposed distinction between current authority and durable compatibility cannot be made mechanically or operationally clear;
- clients cannot safely consume a provisional authoritative contract under any bounded rule; or
- the lifecycle adds ceremony without changing any real compatibility or design decision.

Conversely, eager durability should be rejected or narrowed if it repeatedly creates permanent compatibility machinery around contracts that have not yet survived real implementation/client pressure.

## Exit criteria

The investigation is complete only when it:

1. compares all serious candidates against every proving case;
2. defines the semantic difference among artifact immutability, current authority, durability, release, and compatibility;
3. states concrete evidence gates for durability, including the required maturity research/review step if one survives;
4. states what can change before durability and what becomes immutable afterwards;
5. classifies the current Factory and Engine version contracts under the proposed rule without silently changing them;
6. identifies migration/rebaselining obligations if current contracts were frozen prematurely;
7. states the smallest repository-wide policy/architecture/ADR consequences that would follow;
8. identifies any planning that must remain blocked until reconciliation;
9. records reopening triggers and unresolved questions; and
10. receives independent adversarial review before any conclusion is promoted.

## Non-goals

This investigation does **not**:

- decide the internal composition of the Factory Model; that is the sibling Factory composition investigation;
- choose new Engine dispatch, transfer, scheduling, or analytics semantics;
- implement migrations or rename any current identity;
- rewrite Accepted ADRs in place;
- create a product release calendar;
- require semantic-versioning syntax such as MAJOR/MINOR/PATCH;
- assume that every domain contract needs the same maturity states; or
- weaken the rule that genuinely durable contracts must preserve their historical meaning.

## Coordination with Factory composition research

[Factory Model Semantic Composition](factory-model-semantic-composition.md) may investigate its candidate structures in parallel because the shape question is independently material.

However, the Factory investigation must not promote any candidate aspect/core/profile contract as independently durable, assign permanent version identities, or decide a migration commitment until this lifecycle investigation has concluded or the two reports explicitly reconcile that dependency.

## Expected durable destination

No durable destination is preselected.

Depending on evidence, reconciliation may require:

- no change;
- a repository-wide semantic-contract maturity rule in development/architecture guidance;
- a new ADR defining durability promotion and superseding conflicting portions of existing decisions;
- targeted supersession/reclassification of Factory or Engine version decisions;
- planning changes for migration/rebaselining; or
- a narrower boundary-specific rule rather than one universal lifecycle.

The research report itself must not perform those promotions.
