# Durable reconciliation handoff — Engine applicability to optional-record Factory policies

You are executing the **separate durable architecture/specification reconciliation phase** for Arcogine's completed high-risk research on Engine applicability to optional-record Factory policies.

This is not a new research investigation, not an adversarial review, and not yet implementation of V2 publication or spatial runtime behavior. Translate the decision-quality research evidence into current durable repository authority, planning state, and research lifecycle state, while preserving the evidence-custody and knowledge-transfer rules.

## Repository and custody

Repository: `alaiba/arcogine`

Use the existing finite research-evidence workspace:

`workspace/research-engine-v2-applicability`

At handoff creation time:

- live `main`: `5787bdd39d8268cf6ad55a7ac083fbb0744ceb04`
- workspace tip containing the completed adversarial review: `8bae050dfa9fbaea1ada8ba697f46047c3cb0601`

The workspace is intentionally behind current `main` in ancestry. Before making durable reconciliation edits:

1. resolve live `main` again;
2. read current `AGENTS.md`;
3. preserve all handed-off evidence commits;
4. synchronize the workspace with live `main` using a history-preserving merge/update;
5. **do not rebase, amend, force-push, squash away, or otherwise rewrite the handed-off evidence commits**;
6. verify the exact report and review commits remain reachable after synchronization.

If a safe history-preserving update is not possible, stop with `EVIDENCE PERSISTENCE BLOCKED`.

## Exact decision-quality evidence

### Investigation report

- commit: `c409ecd0bb2941101907a7eb998d8d3ef6b7ce4a`
- path: `workspace/research/investigations/engine-applicability-optional-record-factory-policies.md`
- research baseline: `7e5e6ad2f8a2707d00fff3e2a4e3db96da85852a`

### Independent adversarial review

- commit: `8bae050dfa9fbaea1ada8ba697f46047c3cb0601`
- path: `workspace/research/investigations/engine-applicability-optional-record-factory-policies-adversarial-review.md`
- reviewed report commit: `c409ecd0bb2941101907a7eb998d8d3ef6b7ce4a`
- live-main review baseline: `5787bdd39d8268cf6ad55a7ac083fbb0744ceb04`
- disposition: **ACCEPT WITH QUALIFICATIONS (Q1-Q5)**

Read both exact artifacts before drafting the durable reconciliation. The review qualifications are acceptance conditions and must survive in the appropriate durable destinations.

## Current parallel work

At handoff creation time PR #393, **`refactor: close refocus with final consistency audit`**, is open and is **not landed repository truth**.

It overlaps several files that this reconciliation may also need, including:

- `docs/architecture/overview.md`
- `docs/architecture/runtime-contract.md`
- `docs/planning/factory-simulation-engine-readiness.md`
- `docs/research/research-register.md`

Do not base semantic decisions on PR #393's branch state. If it lands before or during this work, merge the new live `main` history-preservingly and reconcile conflicts against the landed content. If it remains open, keep this reconciliation semantically independent and expect ordinary merge conflict handling later.

## Read first

After resolving the current live-main SHA and synchronizing the workspace, read or re-read current versions of:

1. `AGENTS.md`
2. `docs/development/researching.md`, especially research/reconciliation separation, evidence workspace custody, knowledge-transfer audit, historical decision-rationale retention, and retirement
3. `docs/development/semantic-contract-support.md`
4. `docs/research/research-register.md`
5. `docs/research/investigations/engine-evolution.md`, especially **Engine applicability to optional-record Factory policies**
6. `docs/architecture/overview.md`, especially semantic evolution/support, determinism, and current Factory/Engine identity wording
7. `docs/architecture/engine-semantics-v1.md` in full
8. `docs/architecture/factory-design.md`, especially semantic evolution and Engine applicability
9. `docs/architecture/factory-model-v2.md` in full
10. `docs/architecture/runtime-contract.md`
11. `docs/planning/spatial-runtime-consequences.md`
12. `docs/planning/factory-simulation-engine-readiness.md`
13. `docs/history/README.md`
14. the exact report and adversarial review above

Search the current repository for:

- `engine-semantics:v1`
- `engine-semantics:v2`
- `factory-model:v2`
- Engine applicability / applicable / supported / unsupported / refuse
- Engine semantics identity / `EngineSemanticsVersion`
- spatial absent / optional spatial / present zero
- runtime provenance / observation metadata / event envelope
- V2 publication / spatial activation
- current research-register and planning blocker wording

Fetch material hits at the exact current main/workspace revisions rather than relying on search snippets.

## Reconciliation objective

Translate the reviewed bounded decision into durable authority:

1. **Keep `engine-semantics:v1` semantically fixed.**
2. **Introduce a distinguishable Engine semantics identity for execution of the reconciled `factory-model:v2` domain, covering both:**
   - V2 with the complete spatial record present;
   - V2 with the spatial record absent.
3. Define exact applicability/support and refusal boundaries.
4. Preserve the actual Factory model fingerprint and any truthful controlled-revision provenance.
5. Preserve historical interpretability of `engine-semantics:v1`.
6. Update planning and research lifecycle state so the research prerequisite is cleared only by the landed reconciliation, without pretending V2 publication or spatial execution is already implemented.
7. Complete the research knowledge-transfer audit so the temporary evidence can later be retired safely.

The selected route is the reviewed Candidate B: a distinguishable interpretation for the complete reconciled V2 domain. The reconciliation must allocate a concrete identity under the existing Engine identity scheme. The obvious next sequential name is `engine-semantics:v2`; verify that against current naming/identity authority before using it. Do not invent a parallel identity scheme, capability menu, per-concern Engine identity, or wildcard compatibility framework.

## Mandatory carry-forward qualifications

### Q1 — Conditional proof boundary

Durable wording may state:

- the inspected fixed v1 definition did not establish both-form V2 applicability;
- the affirmative whole-definition correction burden was not met;
- therefore the selected V2 execution contract uses a distinguishable identity.

It must **not** state or imply that:

- every different Factory fingerprint requires a different Engine identity;
- every byte change requires a new Engine identity;
- every newly implemented input requires a new Engine identity;
- every future Factory policy requires a new Engine identity.

Record a bounded reopening condition: if future evidence discovers an Engine-owned, whole-definition-preserving admission rule that already entails this exact V2 absence case, that evidence can justify re-examining the historical conclusion. A new fixture that merely assumes the rule is not proof that v1 historically entailed it.

### Q2 — Present-only and partition scope

The five spatial facts and their result-affecting interpretation remain strongly preserved.

Do not claim:

- that implementing the already-defined spatial mathematics alone necessarily forces a new identity;
- that present-only V2 applicability under v1 was disproven.

The present-only correction remained **unproven, not impossible**. A hybrid partition could be coherent only after a real present-side preservation proof and explicit support/provenance definition.

For this reconciliation, choose one distinguishable successor for both V2 forms as the bounded conservative support contract. Do not create a permanent dual-execution framework or new research gate merely to preserve the hypothetical hybrid alternative.

### Q3 — Exact attribution and dependency support

Define how the exact historical meaning of `engine-semantics:v1` remains resolvable even though the live Factory V2 document now describes a reconciled optional-record grammar.

Do not let a mutable/current Factory reference silently rebind v1's fixed dependency meaning.

At the same time:

- do not resurrect an obsolete historical V2 publication byte grammar as current Factory authority;
- do not republish/relabel a V2 artifact as V1;
- do not invent a new Factory fingerprint merely because the Engine interpretation changes;
- do not silently fall back from an unsupported Engine identity to the successor;
- do not fill missing historical Engine provenance from `CURRENT`.

The successor definition must explicitly identify the Factory policy/content it supports and must preserve real Factory fingerprint provenance.

Use the narrowest durable mechanism that makes the complete interpretation definition resolvable. If a semantics-preserving clarification of the v1 specification is needed to pin its dependency basis, keep that edit strictly non-expansive and reviewable as preservation of existing meaning, not an applicability extension.

### Q4 — Research acceptance is not release evidence

The architecture/specification reconciliation may define the successor interpretation and its obligations. It must **not** claim that current implementation already provides:

- V2 publication/codec/verifier support;
- arbitrary Factory-policy/Engine-identity establishment;
- V2 spatial execution;
- complete Engine identity propagation on supported observations/events;
- successor conformance fixtures.

The current helper rejection, Java type separation, and v1 conformance tests are evidence about current implementation only.

Durable conformance/implementation obligations for later work must include, as applicable:

- reject Factory-valid but Engine-unsupported policy/content before runtime mutation;
- distinguish spatial absence from present zero values;
- preserve the retained non-spatial ordering/dispatch semantics;
- preserve defined transfer arithmetic and reservation/offline-arrival interactions;
- establish the selected Engine identity on the runtime;
- propagate that identity through supported observations/events as required by the runtime contract;
- preserve the selected identity through reset;
- preserve true Factory fingerprint provenance;
- keep Factory invalid-content, canonical-byte, strict-decoding and publication obligations Factory-owned.

Do not implement these production changes in this reconciliation unless current repository policy explicitly requires a small executable fixture merely to make the durable specification mechanically valid. The expected slice is architecture/specification/planning/research-state reconciliation; implementation follows separately.

### Q5 — Knowledge transfer and bounded Factory reopening trigger

Perform the research knowledge-transfer audit before the reconciliation is considered complete.

Transfer, classify, or explicitly discard at least:

- accepted Engine applicability conclusion;
- Q1-Q4;
- absent-vs-present-zero discriminator;
- exact support/refusal/provenance obligations;
- retained v1 dependency-basis decision;
- the present-only/hybrid qualification;
- Factory composition reopening trigger;
- reusable proving cases/failure modes that materially constrain later implementation;
- whether any exact research artifact needs durable retention;
- whether any synthesis seed is justified;
- whether a historical decision-rationale record is warranted.

Do not retain the report/review merely by copying their temporary coordinates into durable docs.

The existing Factory composition reopening trigger remains bounded: concrete successor/support cost or later consumer evidence may justify reopening that earlier choice. This research established no universal optimality claim and no measured successor implementation cost.

## Canonical semantic outcome to encode

The durable result should make the following questions answerable without reading the temporary research artifacts.

### Engine v1

Current architecture must clearly establish:

- what `engine-semantics:v1` means;
- the Factory policy/content domain it is actually applicable to;
- how any spatial clauses already in the fixed definition remain exactly interpretable;
- that the reconciled both-form `factory-model:v2` policy is **not** newly declared applicable to v1 by this reconciliation.

Do not erase or weaken existing v1 semantics merely because some spatial behavior remains unimplemented.

### Successor Engine interpretation

Create or reconcile the owning specification for the selected successor identity.

The successor must explicitly define:

- exact Engine semantics identity;
- exact Factory policy/content applicability;
- V2 spatial-present behavior;
- V2 spatial-absent behavior;
- present-zero versus absent distinction;
- inherited/reused result-affecting production semantics;
- inherited/reused transfer rules where spatial content is present;
- refusal of unsupported policy/content;
- provenance/attribution requirements;
- historical interpretation/support boundary;
- conformance obligations sufficient to prevent silent fallback or provenance erasure.

Prefer definition reuse by explicit stable semantic reference where that preserves whole-definition resolvability. Do not duplicate rules gratuitously, but do not make the successor depend on an ambiguous mutable reference.

Use the narrowest support domain justified by the bounded question. Do not silently add support for Factory V1 to the successor unless there is a concrete current reason and the support relation is deliberately specified. The bounded requirement is both forms of reconciled Factory V2.

## Likely durable surfaces

Use ownership, not this list, to decide final edits. Inspect at least these as candidates:

- `docs/architecture/engine-semantics-v1.md`
- a new successor Engine semantics specification under `docs/architecture/` if the repository's existing specification pattern calls for one
- `docs/architecture/overview.md`
- `docs/architecture/factory-model-v2.md` for truthful cross-reference to Engine applicability, without moving Engine authority into Factory
- `docs/architecture/runtime-contract.md` only if the new identity exposes an actual ambiguity not already covered by its general provenance rules
- `docs/research/research-register.md`
- `docs/research/investigations/engine-evolution.md`
- `docs/planning/spatial-runtime-consequences.md`
- `docs/planning/factory-simulation-engine-readiness.md`
- `docs/history/decisions/YYYY-MM-DD-<semantic-slug>.md` if the retention test is met

Do not edit a surface solely because it mentions v1. Search and update only statements whose current meaning becomes stale or incomplete.

## Research lifecycle reconciliation

The live-main question is still unresolved until this durable reconciliation lands.

In the reconciliation candidate:

- update the maintained research register from its current pre-reconciliation state to `CONCLUDED` in the same change that installs the durable semantic consequence;
- record the verdict and durable destination concisely without duplicating the full architecture;
- update the brief's lifecycle/result wording consistently;
- retain material reopening triggers;
- do not describe the question as concluded on live `main` before the reconciliation PR merges.

Do not create a second research ledger or issue solely to track this transition.

## Planning reconciliation

The planning documents currently block V2 publication identity release and spatial-runtime activation on this Engine applicability question.

Once the durable reconciliation is part of the same merge candidate:

- clear the **research prerequisite**;
- name the selected Engine interpretation that later implementation must establish;
- keep V2 publication and spatial activation gated by their own unimplemented production/conformance prerequisites;
- do not describe spatial execution as already reachable;
- do not claim observation/event Engine provenance propagation is already implemented;
- identify the next implementation responsibility cleanly enough for a later Work Planner/implementation handoff.

Do not expand PLAN-ENG-5 into unrelated transport, pathfinding, congestion, orientation, scheduler-policy, analytics, or resource-model work.

## Historical rationale retention

Apply the retention test in `docs/development/researching.md`.

This decision is a strong candidate for one concise non-normative historical rationale record because it:

- followed substantial high-risk research and independent adversarial review;
- retained a serious present-only/hybrid alternative as unproven rather than impossible;
- makes a hard-to-reverse semantic-identity choice;
- has concrete reconsideration triggers;
- has more reasoning than belongs in the canonical specification.

If current re-grounding confirms the retention test, create one concise record under `docs/history/decisions/` dated by the reconciliation date.

That record must:

- be explicitly non-normative;
- point to the canonical documents where current meaning lives;
- summarize the serious alternatives and decisive bounded rationale;
- state accepted trade-offs and concrete reconsideration conditions;
- not copy the report/review;
- not include temporary workspace `commit SHA + path` pairs as if they were durable preservation;
- not become the only place any current rule or qualification is stated.

If no record is warranted, the knowledge-transfer audit must explicitly say so.

## Synthesis seeds

Do not automatically create or modify a synthesis seed.

Only add/update `docs/research/synthesis-seeds.md` if this investigation independently satisfies the repository's recurrence/admission rules. A shared theme with earlier semantic-identity research is not sufficient by itself; distinguish genuine independent recurrence from reuse of established semantic-contract rules.

## Validation

Use the narrowest current repository validation that applies to the final durable diff.

At minimum for a documentation-only reconciliation, expect to run the current equivalents of:

- Markdown/link validation;
- delivery-label validation;
- transient-coordinate validation;
- `git diff --check`.

Before the branch is a merge/review candidate, complete the knowledge-transfer audit and remove the transient workspace artifacts from the final tree, including:

- research handoff prompts;
- investigation report;
- adversarial review;
- this reconciliation prompt;
- other branch-local research scratch/checkpoints not deliberately promoted.

Then run the tracked-workspace check and confirm no `workspace/` path remains in the final candidate tree.

If the reconciliation touches executable code/configuration, run the additional repository-required gates from current `AGENTS.md`/testing guidance; do not infer those commands from this prompt if the repository has changed.

## Pull request and review boundary

After durable reconciliation, knowledge-transfer audit, workspace cleanup, and applicable validation:

1. re-resolve live `main`;
2. merge/update from it history-preservingly if needed;
3. verify the handed-off report/review commits remain reachable on the active workspace branch;
4. verify no transient workspace paths remain in the final candidate tree;
5. create/update a reconciliation PR against `main`;
6. describe the durable semantic outcome and validation, not the temporary artifact coordinates as preservation;
7. request/use normal **independent PR review** under Arcogine's PR Reviewer contract.

The adversarial research review does not substitute for PR review.

Do not merge merely because the research evidence was accepted.

## Workspace retirement

The workspace is not retirement-eligible merely because this reconciliation prompt or candidate exists.

It becomes retirement-eligible only after:

- the durable reconciliation lands;
- independent PR review validates the change;
- the knowledge-transfer audit accounts for all material research outputs;
- the research question is durably `CONCLUDED` or otherwise terminal;
- no material exact artifact has been designated for separate durable retention.

After merge, deleting the retirement-eligible workspace branch is immediate operational cleanup. Do not keep it as a permanent research archive.

## Final handoff

Return a concise final report containing:

- live-main baseline used for reconciliation;
- workspace branch;
- durable reconciliation commit SHA;
- changed durable surfaces;
- selected concrete Engine identity and exact Factory applicability;
- how Q1-Q5 were carried forward;
- research-register lifecycle update included in the candidate;
- planning blocker transition;
- knowledge-transfer audit result, including rationale-record/synthesis-seed/artifact-retention decisions;
- validation performed;
- PR number/URL if created;
- independent PR-review status;
- whether the workspace is retirement-eligible yet.

Do not paste the temporary research artifacts into the PR or durable documentation.
