# Semantic Evolution Meta-Architecture Ratification — Workspace Brief

> **Workspace mode:** finite meta-architecture evidence workspace; research-class method without requiring this exercise to enter the maintained research-register lifecycle
> **Authority:** evidence and handoff only. Nothing in this file changes product direction, architecture, ADR status, research state, or implementation admission.
> **Research baseline:** `f4122b5c9fb46847ab8136d24073cbcffb4f8232` (`main` at workspace creation)
> **Primary subject under challenge:** PR #360, `docs(architecture): accept ADR-0017 semantic-contract maturity and scoped support promotion`, reviewed head `4d9d93fed5f4fc28fddfd4280ead3d949bed472a`
> **Existing evidence to inspect, not inherit uncritically:**
> - semantic-maturity report: `d0b2c5f127d340ecb4816d6316f5028190c23a13:workspace/research/investigations/semantic-contract-maturity-durability-report.md`
> - adversarial review of that report: `81f1f702b6548ea2bd5f92fe64743ea8798821df:workspace/research/investigations/semantic-contract-maturity-durability-adversarial-review.md`
> - PR #360 reconciliation candidate and its review history
>
> This workspace is temporary custody. Completed evidence revisions are identified by exact commit SHA + path. Do not rewrite handed-off evidence commits. Before final merge, remove temporary workspace files unless a deliberate decision promotes some artifact to a durable location.

## 1. Why this exercise exists

PR #360 began as reconciliation of the semantic-contract maturity and durability investigation. Its proposed ADR-0017, however, appears to do more than settle one bounded Factory/Engine maturity question. It may establish repository-wide rules for how Arcogine binds historical semantic meaning, distinguishes current authority from future support, creates compatibility obligations, permits semantic evolution, and decides when support commitments become durable.

That is potentially a second-order or "constitutional" architectural decision: not merely what one semantic contract means, but how future semantic contracts acquire permanence and support obligations.

The exercise must therefore test whether the durable result should be decomposed into:

1. **enduring semantic-evolution principles** — expected to survive changes in domains, implementations, and current version labels;
2. **repository lifecycle/governance mechanism** — e.g. proving/promoted states, custody declarations, evidence gates, promotion records, exit conditions;
3. **current-contract adjudications and migration** — what those principles/mechanisms imply for `factory-model:v1`, `factory-model:v2`, `engine-semantics:v1`, Governance history/evidence, runtime/outward contracts, and legacy `contentHash()`; and
4. **implementation/planning consequences** — concrete codec, custody, propagation, retirement, or sequencing work.

The exercise is not obligated to preserve that four-layer hypothesis. It must falsify or refine it if repository/product evidence supports a better boundary.

## 2. Core question

> **What are Arcogine's enduring laws of semantic evolution, if any, and which parts of PR #360 are enduring principles versus changeable governance mechanism versus current-contract reconciliation?**

A useful reduction to test — not assume — is:

> **Semantic identity/historical truth becomes non-rebinding when an exact referent is created or accepted; continuing retention, decoding, execution, migration, or interoperability are separate scoped support commitments rather than automatic consequences of identity.**

The exercise must determine whether that is:
- the actual core decision;
- too broad;
- too narrow;
- missing a distinct product-level principle;
- already fully implied by existing Charter/architecture;
- or merely one implementation of a different underlying rule.

## 3. Decision at stake

The result may change:

- whether the foundational rule belongs in `docs/architecture/overview.md`, an ADR, the Product Charter, development policy, or some combination;
- whether ADR-0017 should remain one large decision, be narrowed, be split, or be replaced by a smaller ADR under an enduring architecture principle;
- whether `proving/promoted` is architectural truth or only a repository mechanism;
- how existing V1/V2/Engine/Governance commitments are classified and grandfathered;
- whether current PR #360 should be substantially rewritten rather than merely patched;
- which planning/research surfaces must be reconciled afterward.

This exercise must not decide an implementation merely because a policy mechanism needs an implementation eventually.

## 4. Scope

### In scope

- Product Charter principles that bear directly on semantic continuity, lifecycle continuity, provenance, causality, reality/authority, and change from design through execution.
- The documentation authority model: Charter, enduring architecture, current architecture, ADR rationale/history, research, planning.
- The research operating model only insofar as it informs evidence quality, independence, reconciliation, and artifact custody.
- PR #360 and proposed ADR-0017 in full.
- The prior semantic-maturity research report and its exact adversarial review.
- ADRs and specifications materially implicated by the proposed meta-rule, especially ADR-0004, ADR-0006, ADR-0008, ADR-0011, ADR-0012, ADR-0013, ADR-0014, ADR-0015, ADR-0016, Factory Model v2, and Engine Semantics v1.
- Current implementation/tests only where they prove or falsify a claimed historical/support obligation.
- Relevant semantic neighbors such as Factory composition and Engine evolution, without deciding their independent open questions.
- Carefully selected external evidence when it discriminates among candidate meta-architecture models rather than merely providing analogies.

### Out of scope unless the analysis proves it cannot be separated

- selecting the Factory semantic-composition model;
- redesigning Factory V2 fields or canonical bytes;
- selecting new Engine dispatch/transfer/scheduling behavior;
- designing storage engines, databases, registries, codecs, or migration frameworks;
- adopting semantic-versioning syntax or a release calendar;
- redesigning the entire research operating model;
- implementation of the final lifecycle mechanism;
- reopening unrelated Accepted ADRs;
- using this workspace as a permanent evidence archive.

## 5. Candidate meta-architecture models to test

Do not assume PR #360's Candidate C is the right abstraction level. Reconstruct candidates independently.

At minimum test:

### M1 — Eager architectural durability
Acceptance/publication of a named semantic contract inherently commits Arcogine to its continuing historical support obligations. A new semantic meaning therefore requires a new durable identity and historical support from first publication.

### M2 — Identity truth is immediate; support is explicit
Exact semantic/historical referents become non-rebinding immediately, but continuing retention/decoding/execution/interoperability obligations exist only where explicitly scoped. A lifecycle mechanism may operationalize support promotion.

### M3 — Product-context continuity is the primary rule
The foundational obligation is not a generic semantic-contract lifecycle but preservation of truthful semantic continuity across design/simulation/verification/execution. Contract-specific identity and support rules are derived separately from that product principle; no universal proving/promoted mechanism is required.

### M4 — Contract-class-specific rules only
There is no useful repository-wide meta-rule beyond non-rebinding provenance. Fingerprints, Engine interpretations, history stores, outward APIs, Governance definitions, and Operational commitments each define their own lifecycle/support rules.

### M5 — Another materially distinct model
Introduce another model only if evidence exposes a real alternative rather than a vocabulary variation.

For each model, identify which parts are enduring product/architecture truth and which are repository process.

## 6. Required proving/adversarial cases

Apply every serious model to at least these cases.

1. **Factory fingerprint proving correction**
   A new canonical grammar is normative but not yet retained by any supported authority; implementation discovers a missing field or wrong normalization.

2. **Historically accepted artifact**
   A controlled revision already binds to an exact fingerprint/definition revision, then the current contract evolves.

3. **Engine rule not yet exercised**
   A normative Engine section exists but no attributed run could have exercised it; evidence reveals a semantic error before full release.

4. **Engine rule already exercised**
   Attributed runs exist under the rule; later evidence says the rule should change.

5. **Ephemeral proving artifact**
   A test/conformance artifact exists only in explicitly disposable custody.

6. **Retained proving artifact**
   A retained Governance/Factory authority accepts a proving artifact with exact definition provenance.

7. **Outward consumer**
   A supported external API/client or exported artifact creates a compatibility constituency that survives repository-local refactoring.

8. **Operational consequence**
   A real-world action/decision relied on a specific semantic definition and must remain auditable even if execution support later retires.

9. **Representation change without semantic change**
   Storage/API/codec representation changes while the historical semantic referent and interpretation remain the same.

10. **Semantic change without indefinite implementation support**
    Historical meaning must remain truthful while old executability/decoding or physical retention is intentionally retired under a bounded promise.

For every case ask:
- What historical fact must remain true?
- What identity may not be rebound?
- What continuing support, if any, is actually owed?
- What evidence would justify that support?
- What may evolve without a new long-lived support promise?
- Where should the authoritative rule live?
- What would a consumer be entitled to infer?

## 7. First-pass research-class session

Use a fresh high-capability reasoning session (for example Opus, Sol, Astra, or an equivalent) with no responsibility for preserving the conclusions of this brief or PR #360.

### Anchoring control

Before reading the prior report's recommendation in depth:

1. read this brief;
2. ground in current `main`, `AGENTS.md`, Product Charter, Architecture Overview, ADR policy, and research/review policies;
3. inspect PR #360 only enough to identify its claimed authority transition and changed surfaces;
4. independently reconstruct the major constraints;
5. construct candidate meta-architecture models and likely failure cases;
6. only then deeply inspect the prior semantic-maturity report, adversarial review, and proposed ADR-0017.

### Evidence expectations

This is a high-risk, cross-domain, hard-to-reverse meta-architecture question. Internal evidence is primary because the question is Arcogine's own semantic authority model, but actively seek external evidence where it discriminates among models or exposes failure modes. Prefer normative standards/specifications, mature production-system evolution policies, and contrary analogues over generic architecture commentary.

Do not import another project's maturity taxonomy merely because it resembles `proving/promoted`.

### First-pass deliverable

Persist a report under:

`workspace/research/investigations/semantic-evolution-meta-architecture-report.md`

The report should include:
- exact research baseline;
- reconstructed problem and core decision;
- candidate models;
- proving-case matrix;
- evidence actually used and source quality;
- analysis of PR #360's abstraction layering;
- proposed enduring principles, if any, in minimal form;
- proposed subordinate governance mechanism, if any;
- current-contract consequences kept separate from the principles;
- authority-placement recommendation;
- what should happen to PR #360;
- confidence, unknowns, and falsification/reopening conditions;
- explicit statement of whether the report believes new bounded research is required for any proposition before ratification.

Do not edit durable architecture during this first pass.

## 8. Independent adversarial analysis and testing

A different model family/person is preferred. Otherwise use a fresh isolated high-capability session with no responsibility for defending the first-pass report.

The reviewer must bind to the exact report commit SHA + path and begin from this brief plus live repository authority before deeply consuming the report recommendation.

Challenge at least:

- whether the proposed "enduring principles" are genuinely enduring or merely the current lifecycle mechanism generalized upward;
- whether the result is actually product-level rather than architectural;
- whether historical truth can be separated cleanly from retention/decoding/execution support;
- whether "support promise" is itself too narrow for Operational consequence;
- whether immediate non-rebinding plus bounded custody is sufficient for audit/provenance;
- whether two-state `proving/promoted` creates false precision or is the minimal enforceable mechanism;
- whether a contract can be simultaneously promoted for one promise and proving for another without making the model incoherent;
- whether section-level freeze is a special Engine rule masquerading as a universal principle;
- whether current constituency assumptions are being elevated into enduring architecture;
- whether grandfathering V1/Engine promises is logically required or merely conservative migration;
- whether the proposed authority placement matches Arcogine's Charter/architecture/ADR hierarchy;
- whether any candidate creates irreversible compatibility machinery before real consumer pressure;
- whether any candidate permits rewriting historical meaning under the guise of "proving."

Persist the adversarial artifact under:

`workspace/research/investigations/semantic-evolution-meta-architecture-adversarial-review.md`

Use the standard dispositions if they fit (`ACCEPT`, `ACCEPT WITH QUALIFICATIONS`, `MORE EVIDENCE REQUIRED`, `REOPEN`), but the substance matters more than ritual vocabulary.

## 9. Reconciliation phase

Only after an exact report revision and exact adversarial review revision exist.

Reconciliation is a distinct authority transition but remains on this same finite workspace branch unless evidence preservation requires otherwise.

Start by producing an explicit classification table for every normative proposition being promoted:

| Proposition | Product principle | Enduring architecture | Lifecycle/governance mechanism | Current-contract decision | Planning/implementation |
|---|---:|---:|---:|---:|---:|

Then reconcile only claims that survived the adversarial review.

Expected shape to test, not assume:

1. **Enduring semantic-evolution doctrine** — compact enough for `docs/architecture/overview.md` if genuinely enduring and charter-derived.
2. **Lifecycle/support-promotion policy** — ADR/development/architecture mechanism beneath that doctrine.
3. **Current-contract reconciliation** — V1, V2, Engine v1, Governance/history, runtime/outward compatibility, legacy hash.
4. **Planning/research propagation** — only after semantic authority is settled.

Do not let the migration of existing contracts determine the wording of the enduring principle unless the principle genuinely depends on it.

### Knowledge-transfer audit

Before opening the final reconciliation PR:
- every surviving principle has a durable destination;
- every surviving qualification has a durable destination or explicit discard rationale;
- current-contract consequences are represented in their owning surfaces;
- remaining open questions are explicitly retained without being silently settled;
- temporary report/review/checkpoint files are removed from the branch's final tree unless deliberately promoted;
- the exact report/review commits remain reachable through the review period.

## 10. Standard PR review and merge

The final reconciliation is reviewed as an ordinary high-semantic-impact PR under `docs/development/reviewing.md`.

The PR reviewer should independently verify:
- authority placement;
- semantic-neighbor propagation;
- that the reconciliation does not claim more than the evidence supports;
- that any Accepted/Superseded ADR transitions obey ADR immutability policy;
- exact report/review evidence identity while the workspace is active;
- removal of transient `workspace/` files from the final tree;
- CI and current-base freshness.

Do not treat the prior research/adversarial pass as a substitute for the standard PR review.

## 11. Initial known concerns from PR #360 review

These are leads, not conclusions the new first-pass session must preserve:

1. The proposed V2 retained-custody rule is internally contradictory between unconditional pre-promotion prohibition and conditional retained acceptance with exact revision/custody metadata.
2. The maintained Engine-evolution research still states the older blanket pre-release mutation rule while PR #360 proposes a section-level exercised/unexercised distinction.
3. More broadly, ADR-0017 may mix enduring principle, lifecycle mechanism, current contract classification, and implementation/planning rules in one authority surface.
4. The current two-state model may need explicit analysis of scoped/mixed promises: a contract may have an already-promoted attribution guarantee while other support dimensions remain unproved.

The first-pass session must independently decide whether these are material.

## 12. Stopping rule

Stop when:
- the core meta-decision can be stated without relying on current Factory/Engine names;
- serious alternative meta-models have been tested against the required cases;
- the authority-level boundary is explicit;
- the adversarial pass either validates the conclusion or identifies the specific evidence gap;
- reconciliation can be performed without using research prose itself as authority.

If the analysis cannot separate principle from mechanism without losing correctness, say so and treat that as a substantive result rather than forcing the decomposition.

