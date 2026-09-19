# Handoff: ground-zero semantic-evolution durable reconciliation

You are executing Arcogine's **durable reconciliation phase** for the completed high-risk semantic-evolution meta-architecture investigation.

This is **not** a new research run, **not** a PR review, and **not** implementation cleanup. The research result has already received genuinely independent adversarial review and is decision-quality evidence **only with qualifications Q1-Q9 carried into durable authority**.

Your job is to translate the surviving conclusion and all nine qualifications into Arcogine's authoritative repository surfaces, perform the required knowledge-transfer audit, remove transient research workspace material from the final tree, and open the replacement reconciliation PR. Do not merge it.

## Exact evidence packet

Repository: `alaiba/arcogine`

Finite research/reconciliation workspace branch:

`research/semantic-evolution-ratification`

Exact reviewed report:

- commit: `c6e67cb5022acae482422336da83a1d5ac80a15c`
- path: `workspace/research/investigations/semantic-evolution-meta-architecture-report.md`

Exact independent adversarial review:

- disposition: **ACCEPT WITH QUALIFICATIONS**
- commit: `5118a0851b11f88002ad526d77b9a28a95d85307`
- path: `workspace/research/investigations/semantic-evolution-meta-architecture-adversarial-review.md`

Bounded-question brief:

- commit: `d75b655c6af88fcfeeba3400ec6864477451d11b`
- path: `workspace/research/investigations/semantic-evolution-ratification-brief.md`

Research/review baseline:

`f4122b5c9fb46847ab8136d24073cbcffb4f8232`

At prompt creation, live `main` is still exactly that SHA. Re-resolve live `main` at execution start; do not assume it remains unchanged.

The current abandoned reconciliation candidate is:

- PR #360
- branch: `research/semantic-contract-maturity-durability`
- head: `4d9d93fed5f4fc28fddfd4280ead3d949bed472a`
- current title: `docs(architecture): accept ADR-0017 semantic-contract maturity and scoped support promotion`
- current review disposition on that head: **CHANGES REQUIRED**

PR #360 is evidence/history only for this task. Its proposed ADR-0017 is not landed authority and its monolithic proving/promoted design has been superseded by the reviewed ground-zero result.

## Mandatory start-of-run grounding

Before editing:

1. Resolve live `main` and record its exact SHA.
2. Read `AGENTS.md`.
3. Read `docs/development/researching.md` in full, especially §10 on reconciliation, evidence custody, knowledge transfer, and workspace retirement.
4. Read `docs/architecture/decisions/README.md` in full.
5. Read `docs/development/reviewing.md` far enough to preserve author/reviewer separation and final review gates.
6. Read the exact report and exact adversarial review at the SHAs above. Treat Q1-Q9 as mandatory acceptance constraints, not optional suggestions.
7. Read current Product Charter, Architecture Overview, research register, the maintained semantic-contract-maturity brief, and every Accepted ADR/spec/planning surface materially affected by the reset.
8. Perform a quick `docs/` search for the reconciliation's main concepts: semantic evolution, semantic identity, support/compatibility commitments, non-rebinding, Factory V1/V2, Engine semantics, historical resolution, custody, retained artifacts, legacy `contentHash()`, supersession, and research/planning holds.
9. Re-check live PR #360 metadata/review state so its supersession is handled accurately.
10. Record any material surface you cannot inspect.

Keep these states distinct:

- live `main` = current authority;
- `research/semantic-evolution-ratification` = finite evidence/reconciliation workspace;
- PR #360 = obsolete candidate history, not authority;
- report/review commits = immutable evidence coordinates.

Do not rebase, squash, reset, or force-push away the handed-off report/review commits. If live `main` moved, use only a history-preserving synchronization path allowed by repository policy, then re-check whether the drift materially affects the reconciliation.

## Decision to reconcile

The accepted direction is:

1. Arcogine makes an **explicit ground-zero transition**.
2. The owner declares the repository to be the complete pre-reset estate: no client, retained artifact, external store, release, deployment, or external consumer requires pre-reset support.
3. Pre-reset compatibility/decoding/execution/migration/retention/interoperability promises are withdrawn through explicit durable authority; repository history itself is not rewritten.
4. Post-reset semantic identity is non-rebinding.
5. Continuing support dimensions such as retention, decoding, execution, migration, interoperability, and historical explanation are distinct scoped obligations rather than automatic consequences of naming a contract.
6. Obligations can arise from published support declarations and from accepted/retained use at an authority boundary.
7. A universal whole-contract `proving/promoted` lifecycle is **not** enduring architecture.
8. Universal exercised-section freezing is **not** adopted. A whole-definition freeze trigger applies once a normative contract is first attributed to an accepted/retained record.
9. The reset withdraws support; it does **not** authorize reopening unrelated Accepted semantic decisions.
10. No new bounded research blocks this narrow reconciliation provided Q1-Q9 below are carried.

The current report's direction survives. Where the report and adversarial review differ, the **review qualification governs the reconciliation**.

## Q1-Q9: mandatory reconciliation constraints

### Q1 — Record the reset's ground

The reset ADR Context must record the owner's complete-estate declaration as a dated owner fact, not as an inference from missing GitHub evidence:

- no client;
- no retained artifact;
- no external store;
- no release;
- no deployment;
- no external consumer;

requires pre-reset support.

Also record the falsification/reopening condition: if an excluded pre-reset use surfaces, stop the affected transition, inventory it, and explicitly decide support, migration, or exclusion. Do not silently reinterpret the declaration.

### Q2 — Identifier non-reuse across the boundary

Make this a rule, not advice.

A post-reset semantic identity may reuse a pre-reset label only when its definition is unchanged rule-for-rule / byte-for-byte as applicable. Otherwise a fresh identity label, namespace, or equivalent unmistakable discriminator is mandatory.

Carry the reviewed asymmetry:

- Factory identities have structural discrimination through policy-domain prefix plus verification.
- Engine identity does not have the same structural discriminator.
- Therefore a post-reset Engine semantic identity must not reuse `engine-semantics:v1` unless the definition is actually identical.

Do not invent a platform-wide epoch service or registry merely to implement this rule.

### Q3 — Separate enduring invariant from mechanism

In the Architecture Overview, keep only the enduring invariant content:

- preserve original accepted bindings and their declared fixed aspects;
- preserve domain ownership;
- narrowing an in-scope promise requires an explicit authorized transition.

Do **not** put a mutable support checklist into enduring architecture.

The operational checklist — successor meaning, supported scope, compatibility evidence, retained basis, failure behaviour, retirement/change path, etc. — belongs in development policy and/or the owning contract/specification.

### Q4 — Make accepted use enumerable

Define the obligation trigger precisely enough to review:

An obligation arises only from:

1. reliance explicitly published by the owning contract's support declaration; or
2. acceptance by an authority that has declared its custody, at that authority's commit/acceptance boundary.

Tests, scratch stores, drained events, temporary local runs, and equivalent disposable activity are not accepted uses merely because they exist.

Acceptance into an undeclared retained authority/store is an **admission defect**, not a waiver:
- account for the obligations already created;
- block new admission until custody/support is declared;
- never retroactively dispose of the accepted fact to avoid the obligation.

Include failure/refusal behaviour in the declaration information set.

Do not create a central support registry unless a concrete existing authority requires one.

### Q5 — Definition-retention floor

Carry the cross-domain floor:

Every semantic identity stamped on an accepted or retained record requires the exact identity-defining definition revision to remain resolvable for as long as that record is retained.

This does **not** automatically require:
- retaining all original content;
- permanent decoding;
- permanent execution;
- permanent interoperability.

Those remain separately declared obligations.

Ensure the Overview states this compactly without duplicating the detailed owning ADR decisions.

### Q6 — Whole-definition freeze trigger

Adopt the reviewed rule:

A normative semantic contract may be corrected in place only until the first accepted/retained record is attributed to that definition. Once such an attribution exists, any behavioural/identity-affecting change requires a distinguishable new identity/definition.

Do not adopt PR #360's exercised-section freeze.

Resolve the current `docs/research/investigations/engine-evolution.md` blanket pre-release mutation statement to the rule actually ratified.

For the post-reset Engine contract:
- it must not stamp a retained/accepted record with a mutable definition identity; or
- that record must be explicitly disposable/non-retained.

Post-attribution same-label Engine amendment remains a separate bounded research question if Arcogine ever wants it.

### Q7 — Confine reset to support withdrawal and classify every affected contract

The reconciliation must contain an explicit classification for at least:

- Factory V1;
- Factory V2;
- Engine V1;
- controlled history / Governance evidence;
- runtime/outward interfaces;
- Operational history;
- legacy `contentHash()`.

For each, distinguish four things:

1. withdrawn pre-reset support obligations;
2. retained intrinsic invariants;
3. deletable implementation material for later cleanup;
4. unresolved domain decisions that this reconciliation does not settle.

Mandatory retained intrinsic semantics include, where applicable:

- ADR-0006 policy-level invariants:
  - policy version identifies a canonicalization contract;
  - identity-affecting change requires a new label;
  - semantic digest is independent of serializer representation;
  - totality over the published model domain;
- ADR-0004 / ADR-0008 distinction between fingerprint/content identity and controlled revision/history identity;
- ADR-0014 §4 / ADR-0015 §3 authored Factory fact vs Engine interpretation ownership;
- ADR-0014 §10-§12 no automatic lift and explicit cross-policy comparison semantics;
- ADR-0011 / ADR-0012 derivation/projection boundaries;
- ADR-0008 in full;
- ADR-0013 in full;
- ADR-0016 in full.

Do **not** use the reset to "re-evaluate" ADR-0013 or ADR-0016. There is no Operational support estate to withdraw, and the research did not justify reopening those semantics.

Do not make Factory composition, analytics provenance, Operational retirement, or any other open research question part of this decision.

### Q8 — No duplicated authority

Use Arcogine's authority hierarchy cleanly.

Expected placement:

- Product Charter: unchanged unless live evidence shows a real contradiction; the reviewed result found none.
- Architecture Overview: compact enduring doctrine only; cite owning ADRs rather than restating their full decisions.
- Reset/evolution ADR: dated reset decision, owner declaration, support withdrawal, non-reuse rule, significant evolution/support decision, supersession metadata, and current-contract classification required to make the transition authoritative.
- Development policy / owning specs: mutable support-declaration mechanics, evidence/checklist content, concrete domain application.
- Research register/briefs: lifecycle/result state and remaining questions.
- Planning: only admitted follow-on work and holds that genuinely change because the semantic decision landed.

The reset ADR must not duplicate the Overview. The Overview must not become a copy of the ADRs.

### Q9 — Correct supersession inventory, including ADR-0006

Ground-zero means `factory-model:v1` is not retained merely to avoid supersession.

If the reconciliation withdraws V1 as a supported policy — which is the accepted reset direction — ADR-0006's applicability changes. Supersede it while explicitly carrying forward the policy-level invariants listed in Q7.

ADR-0014 must also be superseded. Carry forward its intrinsic surviving invariants, especially authored-fact/Engine-interpretation ownership and no-lift/explicit-comparison rules; withdraw the permanent V1/V2 support/coexistence commitments that depended on the pre-reset support estate.

Do not rewrite the historical bodies of Accepted ADRs. Follow the repository's semantic immutability/supersession policy.

On the reviewed direction:
- ADR-0011: no supersession required;
- ADR-0012: no supersession required;
- ADR-0015: no supersession required merely for the reset;
- ADR-0008: no supersession;
- ADR-0013: no supersession;
- ADR-0016: no supersession.

If live authority has changed since the review, re-evaluate this inventory explicitly rather than assuming it.

Allocate new ADR number(s) against live `main`. PR #360's candidate ADR number is not authority. Prefer the smallest authority split that avoids mixing enduring doctrine with Factory-specific detail. One reset/evolution ADR may supersede both ADR-0006 and ADR-0014 if it can do so precisely without duplicating domain specification; otherwise use a small reset/evolution ADR plus a distinct Factory-specific superseding ADR. Do not create extra ADRs for ceremony.

## Required durable reconciliation surfaces

Determine exact file edits from live authority, but the final result must account for all of these categories.

### Architecture

Reconcile `docs/architecture/overview.md` with the compact post-reset doctrine:
- non-rebinding/fixed-aspect truth;
- accepted-use accountability;
- definition-retention floor;
- separation of meaning from support dimensions;
- explicit transition for narrowing promises;
- identifier non-reuse across a reset/change boundary.

Keep this compact and cite owning ADRs.

### ADRs

Create the necessary new Accepted ADR decision(s) for:
- the ground-zero reset boundary;
- the dated complete-estate owner declaration;
- withdrawal of pre-reset support estate;
- post-reset semantic-evolution/support rules;
- required supersession(s);
- explicit current-contract classification needed to make those withdrawals authoritative.

Apply only metadata changes to superseded Accepted ADR files as permitted by `docs/architecture/decisions/README.md`.

### Factory / Engine / Governance / Operational specifications

Make only the changes needed to eliminate contradictions with the ratified rule.

In particular:
- Factory V2 must not remain misleadingly presented as a supported/promoted post-reset compatibility contract merely because it exists in the repository.
- Engine evolution research must no longer state the old blanket rule that conflicts with Q6.
- Do not adopt section-level freeze.
- Do not reopen Governance or Operational semantics that Q7 preserves.
- Apply identifier non-reuse consequences where a current specification would otherwise imply reuse.

### Development policy

Place the support-declaration/checklist mechanism in the smallest appropriate development-policy surface. Do not force it into `docs/development/researching.md` merely because the evidence originated in research; research policy owns research, not all semantic-contract support.

The mechanism must be proportionate and must distinguish:
- an obligation from evidence that implementation fulfils it;
- published support from accepted retained use;
- retained basis from decoding/execution/interoperability;
- explicit failure/refusal behaviour;
- change/retirement behaviour.

Do not introduce a universal two-state lifecycle.

### Research state

Reconcile the maintained semantic-contract-maturity question that the reset now settles:
- update `docs/research/research-register.md` consistently with the durable result;
- update its maintained brief/result surface as required by repository convention;
- mark it terminal only if the same PR actually lands the authoritative consequence, which this reconciliation is intended to do;
- keep Factory composition, analytics provenance, Operational closure/retirement, post-attribution same-label Engine amendment, and any other surviving questions separate.

The finite meta-architecture workspace itself does not need a new permanent research-register entry.

Do not copy temporary report/review coordinates into maintained research state merely to simulate durable preservation.

### Planning

Update only planning statements invalidated by the reset:
- remove holds whose only prerequisite was the now-resolved universal maturity question;
- preserve holds that still depend on Factory composition or other unresolved semantics;
- admit cleanup only as future bounded work if planning authority requires it;
- do not perform that cleanup in this reconciliation.

Do not silently turn a research conclusion into a broad implementation program.

## Current-contract classification expectation

The exact wording is yours to reconcile against live authority, but the result must preserve this distinction.

### Factory V1
Withdraw pre-reset support/compatibility obligations. Do not migrate old artifacts. Retain only intrinsic identity/canonicalization invariants justified independently of V1 support. Because V1 is no longer supported, handle ADR-0006 supersession per Q9.

### Factory V2
Do not promote/preserve it merely because the document exists. Keep only independently justified authored-field/validation/identity invariants; leave Factory composition and the shape of a future fresh Factory semantic contract unresolved.

### Engine V1
Withdraw it as the post-reset supported semantics label unless the definition is preserved identically. Retain independently justified determinism, authored-fact/interpretation ownership, and other intrinsic Engine rules only where current Accepted authority supports them. Do not reuse `engine-semantics:v1` for changed meaning.

### Controlled history / Governance evidence
Withdraw any pre-reset reader/compatibility obligation, but do not reopen ADR-0008 or ADR-0016 semantics. Accepted occurrence, provenance, correction, and evidence-use invariants survive.

### Runtime / outward interfaces
Treat current adapters/interfaces as implementation material, not as automatic post-reset compatibility commitments. A future supported external boundary must create its own explicit semantic/support contract.

### Operational
Do not claim the reset reopens ADR-0013. Its continuation semantics remain Accepted. The reset changes no Operational implementation admission.

### Legacy `contentHash()`
Classify as removable post-reset implementation compatibility material. Do not delete it in this PR. Admit/bound later cleanup only if required by planning.

## Knowledge-transfer audit — mandatory before final PR candidate

Before opening the replacement PR for independent review, produce and encode a complete transfer audit covering this workspace.

Account explicitly for:

1. surviving architectural principles and their durable destinations;
2. every qualification Q1-Q9 and its durable destination;
3. current-contract consequences;
4. remaining research questions and their owning maintained surfaces;
5. reusable evidence/know-how that needs durable preservation;
6. any synthesis-seed candidate under the repository's actual seed criteria;
7. explicit discard decisions for transient chronology, prompts, reports/reviews after transfer;
8. workspace retirement eligibility after merge and independent PR review.

Do not preserve a synthesis seed merely because the research was interesting. If every useful signal has a direct durable destination and nothing loss-sensitive remains, state that no seed is admitted.

## Transient workspace cleanup

The branch currently contains finite research evidence and prompts under `workspace/`.

Those commits must remain reachable during reconciliation/review, but **the final merge candidate must contain no tracked `workspace/` files**.

Before handing the replacement PR to independent review:

- verify the report commit `c6e67cb...` and review commit `5118a085...` remain reachable in branch history;
- delete the brief/report/review prompts/review and this reconciliation prompt from the branch's final tree after the knowledge-transfer audit has accounted for them;
- remove any other transient workspace/checkpoint files;
- run `.github/scripts/check-transient-workspace.py`.

Do not move completed reports/reviews into `docs/` merely to preserve them.

## PR #360 and replacement delivery

Do **not** retrofit PR #360.

The intended delivery is a fresh replacement PR from:

`research/semantic-evolution-ratification` → `main`

Reason: this branch is the repository-prescribed continuation of the reviewed evidence workspace, while PR #360 is tied to the older semantic-maturity branch and an obsolete monolithic design.

Once the reconciliation branch:
- is synchronized to current `main` as required;
- has durable reconciliation edits complete;
- has the knowledge-transfer audit complete;
- has removed all tracked `workspace/` material;
- passes required validation;

open a new PR to `main`.

Suggested title shape:

`docs(architecture): establish ground-zero semantic evolution boundary`

Use the PR body to summarize:
- ground-zero owner decision;
- exact report/review evidence coordinates as delivery-history provenance;
- review disposition `ACCEPT WITH QUALIFICATIONS`;
- Q1-Q9 transfer map;
- ADR supersession map;
- research/planning propagation;
- knowledge-transfer audit;
- validation;
- explicit non-goals.

The PR body may use active evidence coordinates for review provenance. Durable architecture/research state must not depend on those temporary coordinates.

After the replacement PR exists, close PR #360 as **superseded by the replacement PR**, preserving its review/history. Do not attempt to make the old head green first.

Do not merge either PR.

## Validation

This should remain a documentation/architecture/research/planning reconciliation unless live authority proves a tiny non-production script/doc fixture must change. Do not perform product cleanup.

At minimum run the repository-prescribed checks relevant to the touched surfaces, including:

- ADR immutability/supersession checker against current `origin/main`;
- Markdown-link checker;
- delivery-label/state checker where applicable;
- transient-workspace checker;
- checker self-tests required by repository convention if checker-owned inputs are touched;
- `git diff --check`.

Run any additional documentation/ADR validation named by current `AGENTS.md` or touched files.

Do not run Java/frontend product suites merely for ceremony if no product code or product test changed. If you do touch executable/product behavior, stop and reassess scope: that is likely implementation cleanup and belongs after the reconciliation lands.

## Scope prohibitions

Do not:

- perform a new research investigation unless live drift reveals a genuinely new unresolved semantic question;
- revise the reviewed report or adversarial review;
- adopt universal `proving/promoted` as architecture;
- adopt section-level freeze;
- reopen ADR-0008, ADR-0013, or ADR-0016 semantics;
- settle Factory semantic composition;
- settle simulation analytics provenance;
- settle Operational closure/retirement;
- define a same-label post-attribution Engine amendment mechanism;
- introduce a central semantic-contract registry or epoch service without a concrete existing consumer;
- delete V1/V2/Engine/legacy code in this PR;
- merge any PR;
- perform the independent PR review yourself.

## Stop / escalation conditions

Stop reconciliation and report the exact blocker if:

1. live `main` has materially changed in a way that invalidates the reviewed conclusion;
2. an excluded pre-reset client/artifact/store/release/deployment/consumer is discovered;
3. carrying Q1-Q9 requires reopening an Accepted semantic decision that the review explicitly kept out of scope;
4. the necessary supersession cannot be expressed without contradicting ADR immutability policy;
5. history-preserving synchronization cannot retain the handed-off evidence commits;
6. repository persistence or PR mutation capabilities needed for the required handoff are unavailable.

Do not silently weaken Q1-Q9 to get a PR open.

## Completion criteria

This reconciliation task is complete only when all of the following are true:

- live-base drift was re-checked;
- Q1-Q9 each has a durable destination;
- ground-zero reset is represented in authoritative architecture/ADR surfaces;
- ADR-0006 and ADR-0014 are handled correctly under the chosen supported-policy outcome;
- no unrelated Accepted semantics were reopened;
- maintained research state reflects the landed-intended decision without collapsing remaining questions;
- planning propagation is bounded;
- knowledge-transfer audit is complete;
- final branch tree contains no tracked `workspace/` material;
- required checks pass;
- replacement PR is open from `research/semantic-evolution-ratification` to `main`;
- PR #360 is closed as superseded by the replacement PR;
- the replacement PR has **not** been merged;
- independent PR review remains the next gate.

## Final handoff

After pushing the completed reconciliation and opening the replacement PR, return compactly:

- live-main baseline used;
- reconciliation branch;
- exact reconciliation head SHA;
- replacement PR number + URL;
- PR #360 supersession/closure status;
- new ADR number(s) and supersession mapping;
- one-line Q1-Q9 transfer status;
- validation results;
- whether the workspace is retirement-eligible **after merge + independent PR review**;
- the next required action: independent PR Reviewer run on the exact replacement-PR head.

Do not paste this full prompt back into chat after executing it.
