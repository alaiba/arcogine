# Durable reconciliation handoff: Semantic Contract Maturity and Durability

Operate as the Arcogine reconciliation author for repository `alaiba/arcogine`.

This is **not another research run** and **not an independent adversarial review**. The high-risk research has a reviewed decision-quality report/review pair. Your task is the separate durable reconciliation phase: translate the surviving conclusion and every mandatory qualification into the appropriate authoritative repository surfaces, preserve evidence custody, perform the knowledge-transfer audit, and prepare one independently reviewable reconciliation PR.

Use the existing finite research workspace:

`research/semantic-contract-maturity-durability`

Do not create a new reconciliation branch merely because the phase changed.

## 1. Evidence identity — immutable inputs

The exact research evidence you are reconciling is:

- Report:
  - commit: `d0b2c5f127d340ecb4816d6316f5028190c23a13`
  - path: `workspace/research/investigations/semantic-contract-maturity-durability-report.md`
  - research baseline: `46389bc5d21e0c95ffa9366ce271f9d943d44b35`

- Independent adversarial review:
  - disposition: `ACCEPT WITH QUALIFICATIONS`
  - commit: `81f1f702b6548ea2bd5f92fe64743ea8798821df`
  - path: `workspace/research/investigations/semantic-contract-maturity-durability-adversarial-review.md`
  - review baseline: `46389bc5d21e0c95ffa9366ce271f9d943d44b35`

Those exact SHAs plus paths are the evidence identities. Do not amend, rewrite, rebase away, force-push away, or replace them with branch-tip coordinates.

The review disposition is usable for promotion only for the exact report revision above.

## 2. Later owner-supplied evidence on this workspace

After the review handoff, workspace commit:

`2ba254012f0a1b3f185e961a286ebb5d17168215`

recorded a repository-owner statement that the repository is the complete constituency of every Arcogine contract:

- no external deployments;
- no consumers outside this repository;
- no externally retained revision stores or result histories;
- no published releases, packages, or images;
- every current consumer is in-repository or does not exist.

That commit also added the maintained current-state statement to `docs/architecture/overview.md` and appended a transient addendum to the review file.

Treat this as **later reconciliation-phase evidence/current-state input**, not as part of the immutable review revision `81f1f702...`.

Reverify the owner statement is still current when you start. If repository state now contradicts it, stop and re-scope the reconciliation rather than silently using stale constituency evidence.

The inventory gap identified by the report/review is therefore currently closed. This permits, but does not require, narrowing a grandfathered support promise. Do not narrow an existing promise merely because narrowing is now possible: inspect all in-repository dependents and justify each retained or narrowed promise.

## 3. Required operating documents and quick search

Read first:

1. `AGENTS.md`
2. `.github/CONTRIBUTING.md`
3. `docs/development/researching.md`, especially research/reconciliation separation, evidence custody, knowledge-transfer audit, and retirement
4. `docs/architecture/decisions/README.md`
5. `docs/research/research-register.md`
6. the exact report and review revisions identified above
7. the post-review constituency commit `2ba2540...`

Then perform the repository-required quick search across docs, source, tests, and relevant history for the main reconciliation terms:

- `semantic contract`
- `durability`
- `support promise`
- `proving`
- `provisional`
- `released`
- `contentHash`
- `factory-model:v1`
- `factory-model:v2`
- `engine-semantics:v1`
- `ModelFingerprint`
- `ControlledRevisionId`
- `EngineSemanticsVersion`
- `permanent resolution`
- `golden vectors`
- `historical resolution`
- `custody`
- `retention`
- `compatibility`
- `V1/V2 coexistence`
- `research hold`

Search is discovery only. Read load-bearing material at the exact revision you use.

## 4. Re-ground before editing

Resolve live `main` at start and compare it with the research/review baseline.

At prompt creation time, live `main` still equals:

`46389bc5d21e0c95ffa9366ce271f9d943d44b35`

Do not assume that remains true.

If `main` has advanced:

- inspect the complete delta relevant to this reconciliation;
- preserve the handed-off report and review commits;
- synchronize with `main` only through history-preserving merge-style normalization when needed;
- never rebase or force-push the evidence history;
- if semantic conflicts make a safe history-preserving update impossible, stop with `EVIDENCE PERSISTENCE BLOCKED`.

Before final PR-review handoff, the branch must be current with its base under repository lifecycle rules.

## 5. Surviving conclusion to reconcile

The reviewed conclusion is:

**Boundary-specific evidence-gated semantic-contract commitments with shared minimum rules.**

The durable rule must preserve these load-bearing findings:

1. current/normative authority does **not** automatically create every future compatibility/support promise;
2. an exact historical referent is non-rebinding immediately;
3. durability/support is a **scoped promise**, not one undifferentiated guarantee;
4. a durable promotion binds an exact contract revision to explicit promises and supporting evidence;
5. introduction and promotion are distinguishable decisions but need not always happen in different PRs or on different dates;
6. release alone is not sufficient evidence of maturity;
7. existing commitments are not silently relabelled or rewritten;
8. historical content identity, controlled occurrence identity, Engine interpretation identity, release, retention, decoding, execution support, and outward compatibility remain distinct;
9. Factory composition remains a sibling question and must not be decided here;
10. no implementation semantics are changed merely by reconciling this lifecycle rule.

Do not expand the result into a generic versioning framework, semantic-versioning scheme, release calendar, or plugin model.

## 6. Mandatory qualifications Q1–Q7

Every qualification below is binding. The reconciliation is incomplete if any one is omitted, weakened, or left only in the temporary report/review.

### Q1 — Default for silence; obligations arise only from explicit promise or accepted retained custody

Encode a mechanical default:

- A newly introduced contract whose introducing decision neither declares a scoped support promise with its evidence gate satisfied nor explicitly marks the contract as proving is **non-durable but non-rebinding**.
- Silence never implies durability.
- Every new semantic contract must state explicitly whether it is proving or promoted.
- A support obligation arises from:
  1. an explicit published support promise; or
  2. acceptance of an artifact/record into retained, non-disposable custody.
- Unobserved third-party reliance is an inventory risk, not by itself the mechanism by which Arcogine creates a support promise.

The durable wording must make this testable rather than aspirational.

### Q2 — Custody default for provisional persisted artifacts

Encode the custody boundary:

- Disposable versus attributable/retained is determined by the **declared custody of the holding store/authority**, not merely by whether some acceptance API was called.
- Artifacts are disposable only when held in custody explicitly declared ephemeral, such as test fixtures or temporary authorities.
- Anything accepted into a store without an ephemeral-custody declaration is retained for the promised horizon.
- Decide and record whether a retained Factory/Governance authority may accept artifacts produced under a proving semantic contract.
- If it may, the retained record must preserve the exact definition revision and the applicable custody declaration/support horizon.
- If it may not, the authority must reject such artifacts explicitly.

Do not invent a production persistence implementation merely to express this rule. Put the semantic rule in the owning durable authority and admit follow-up implementation/planning only where current executable behavior must later enforce it.

### Q3 — Every proving contract needs an exit condition

Encode:

- Every proving/provisional semantic-contract revision must declare either:
  - a custody horizon; or
  - a concrete retirement/promotion trigger.
- A compatibility period with no end condition is not a valid proving state.

Classify the repository's current legacy `FactoryModelVersion.contentHash()` compatibility/provenance surface under this rule.

Inspect all current in-repository uses before deciding. Choose the smallest truthful result:

- bounded proving custody with a concrete retirement trigger; or
- explicit retirement/deprecation path.

Do not remove code merely because the architecture now has an exit condition. If executable cleanup is required, place it in the correct admitted plan and leave implementation to a later Work Planner/implementation run.

### Q4 — Define "released" for `engine-semantics:v1`; freeze at exercised-section granularity

Encode in the owning Engine authority:

- `engine-semantics:v1` becomes fully released when every required §14 fixture family is pinned; current evidence says that requires the V2/spatial runtime proving path.
- Until full release:
  - any rule/section exercised by an attributed run is frozen exactly under the ADR-0015 non-rebinding principle;
  - only sections not exercised by any attributed run may be corrected;
  - each pre-release correction must be explicitly recorded;
  - once an attributed run exercises a section, later semantic correction requires a new semantics identity rather than reinterpretation.

Do not change Engine behavior in this reconciliation.

Prefer recording this in `docs/architecture/engine-semantics-v1.md` or the narrowest owning Engine surface.

Do **not** semantically edit ADR-0015 in place. If the owning ADR truly requires a semantic decision change, use the ADR supersession process. If a change is only an editorial clarification, follow the ADR README's amendment protocol and leave semantic-equivalence judgment to independent PR review.

### Q5 — Enumerate current grandfathered promises explicitly, then deliberately retain or narrow them

At minimum enumerate these promises in durable authority.

For `factory-model:v1`:

- grammar immutability and never-reused identifier from ADR-0006;
- golden vectors as part of the contract;
- the cross-language reproducibility claim;
- permanent verifier/decoder retention currently established by ADR-0014 decision 8;
- full execution support for V1 under non-spatial semantics from ADR-0014 decision 9;
- no automatic V1 -> V2 lift from decision 10.

For `engine-semantics:v1`:

- exact attribution;
- immutable specification;
- fixture definitions that survive retirement;
- the applicable ADR-0015 decisions 14 and 16.

Because the owner-supplied complete constituency inventory now exists, you may evaluate whether any current promise should be narrowed to the needs of the actual in-repository constituency.

For each promise:

1. identify the current authority;
2. identify actual in-repository dependents;
3. choose **retain** or **narrow**;
4. justify the choice;
5. if narrowing an Accepted decision, use explicit ADR supersession — never an editorial amendment.

Do not silently convert a previously durable promise into a proving promise.

If no concrete simplification is justified, retain the promise.

### Q6 — Supersede/narrow the exact automatic-support clauses

The prospective maturity rule conflicts with automatic future-support triggers currently embedded in Factory policy evolution.

At minimum reconcile:

- ADR-0014 decision 8 — permanent resolution of every released policy;
- ADR-0014 decision 12 — automatic `vN+1` progression for an unrepresentable authored fact;
- `docs/architecture/factory-model-v2.md` §10 — the current "recorded against a controlled revision" automatic durability trigger.

The review concluded:

- ADR-0006's ship-gated exact-grammar immutability rule remains valid;
- ADR-0015's identity/non-rebinding rules remain valid;
- the automatic support/promotion trigger is what changes prospectively.

Accepted ADRs are semantically immutable.

Therefore:

- do not edit ADR-0014's semantic decision in place;
- allocate the next unused ADR number at execution time, checking concurrent ADR work;
- use an **Accepted superseding ADR** for the changed semantic decision;
- ensure the replacement is self-contained enough that making ADR-0014 `Superseded` does not accidentally discard still-current Factory constraints;
- explicitly carry forward or restate every ADR-0014 decision that remains current pending the sibling Factory-composition result;
- keep the sibling composition question open: do not use this reconciliation to decide aggregate-versus-concern structure, new component identities, or a replacement Factory schema shape.

If one cross-cutting maturity ADR can cleanly replace the affected ADR-0014 semantics while preserving all unaffected Factory rules, prefer one ADR. If that would make the decision ambiguous, use the minimum additional durable structure needed. Do not create ADRs merely to mirror document boundaries.

Update the unreleased V2 specification's lifecycle trigger to match the accepted rule, without treating that edit as release of a V2 fingerprint policy.

### Q7 — Promotion record location and required fields

Encode where promotion is recorded.

A durable promotion must be recorded in:

- an ADR; or
- for a semantic contract whose governing authority is a normative specification, a dated promotion section in that specification subject to equivalent independent review.

Every promotion record must name:

1. exact contract revision;
2. scoped promises being made;
3. supported scope;
4. evidence relied on;
5. affected consumers/artifacts;
6. evolution obligations;
7. retention/retirement obligations.

Every contract's owning durable document must also carry one compact support-status statement so consumers do not have to reconstruct promises from several historical ADRs.

Make the support statement distinguish at least:

- proving versus promoted;
- exact definition revision/identity;
- historical decoding/resolution obligation;
- execution support, if any;
- outward compatibility, if any;
- retention horizon/retirement rule, where applicable.

Do not make branch names, research SHAs, or temporary workspace coordinates part of the semantic contract.

## 7. Durable destinations to inspect and reconcile

At minimum inspect and change only where the evidence requires it:

- next Accepted ADR implementing the repository-wide maturity/support-promotion rule and the required ADR-0014 supersession;
- ADR-0014 supersession metadata;
- `docs/architecture/factory-model-v2.md`;
- `docs/architecture/engine-semantics-v1.md`;
- `docs/architecture/factory-design.md` and/or the narrowest Factory support-status surface;
- `docs/architecture/overview.md`, including the owner constituency statement already added on this workspace;
- Governance/current authority documentation where Q2's retained/proving-custody rule belongs;
- `docs/development/` only if a repository-wide operational rule belongs there in addition to the ADR — do not duplicate semantic authority unnecessarily;
- `docs/planning/factory-design-capability.md`;
- `docs/planning/spatial-runtime-consequences.md`;
- any plan that currently states both maturity and Factory composition are unresolved;
- `docs/research/research-register.md`.

Use the narrowest authoritative surface for each consequence.

Do not edit unrelated architecture merely to "spread" the new vocabulary.

## 8. Planning consequence

This reconciliation resolves the **maturity/durability lifecycle** question only.

It does **not** resolve Factory Model Semantic Composition.

Therefore:

- update stale planning prose that says the maturity question itself is still unresolved;
- keep any Factory V2 canonical-identity/coexistence/spatial activation work blocked wherever the sibling Factory-composition result is still a prerequisite;
- do not restart held spatial work solely because this maturity reconciliation exists;
- do not choose a compositional Factory representation here;
- do not generate an implementation-agent prompt here; Work Planner owns later implementation sequencing/handoffs.

If Q2/Q3/Q4 require executable enforcement not already present, record the smallest planning admission/dependency and leave implementation for later planning.

## 9. Current constituency statement

The workspace already contains a proposed maintained statement in `docs/architecture/overview.md` that this repository is the complete current contract constituency.

Re-evaluate that wording during reconciliation.

If still true:

- retain it in a durable current-state surface;
- ensure it is clearly a maintained current constraint, not an eternal architecture invariant;
- keep the rule that it must be updated in the same change that creates the first external deployment, release, package/image, externally retained store, or external consumer.

Do not preserve the post-review addendum inside the transient review artifact as durable state; the durable Architecture Overview statement is the maintained destination.

## 10. Research register lifecycle

Update the Semantic Contract Maturity and Durability row so the merge candidate records the durable result.

The target durable state after this reconciliation lands is `CONCLUDED`, with the durable authority pointing to the new/reconciled ADR/specification/architecture surfaces and noting:

- independent adversarial review disposition: `ACCEPT WITH QUALIFICATIONS`;
- Q1–Q7 are encoded;
- current Factory/Engine support promises were explicitly accounted;
- sibling Factory-composition research remains separate.

Do not make temporary report/review SHAs the maintained post-retirement authority.

A merged reconciliation PR may be named as delivery-history provenance, consistent with existing register practice, but current semantics must be understandable without fetching temporary research artifacts.

Before the PR lands, do not tell the user that the research is already `CONCLUDED`. The merge candidate may contain the state transition that becomes authoritative on merge.

## 11. Knowledge-transfer audit — mandatory

Before final PR-review handoff, perform the research knowledge-transfer audit required by `docs/development/researching.md`.

The PR description or another appropriate delivery-history surface must explicitly account for this workspace:

`research/semantic-contract-maturity-durability`

Classify and transfer at minimum:

### Accepted conclusions and invariants

Transfer:

- scoped-promise durability model;
- immediate non-rebinding;
- explicit proving/promoted state;
- explicit promotion record;
- custody rule;
- proving exit condition;
- Engine pre-release/freeze rule;
- current contract support-status decisions.

### Mandatory qualifications

Show where **each of Q1 through Q7** landed. A qualification may not exist only in the PR description; it must be encoded in durable authority.

### Remaining questions

Keep unresolved material with its actual owner:

- Factory semantic composition;
- actual future storage/codec/retention implementation choices;
- future external-consumer support decisions;
- any implementation detail not decided by this lifecycle rule.

Use the maintained research register for genuinely open research questions; do not create a maturity side-ledger.

### Reusable evidence / know-how

Transfer reusable proving cases, migration/refusal rules, and support-status conventions into the ADR/spec/architecture surfaces that future authors/reviewers will actually use.

### Synthesis seed

The report concluded that no new synthesis seed was warranted because the material has direct durable destinations. Re-check this after reconciliation; do not create a seed unless the repository's seed criteria are genuinely met.

### Explicit discard

Explicitly discard temporary drafting/search chronology and other session-local material that has no durable use.

### Exact research artifacts

The report/review themselves do not require permanent readability after successful transfer. Do not promote them to `docs/` merely to preserve them.

### Retirement eligibility

State whether the workspace is retirement-eligible **after**:

1. the reconciliation PR lands;
2. independent PR review verifies the transfer;
3. all temporary workspace files are absent from the merge candidate.

If any material result lacks a durable destination or explicit discard, the workspace is not retirement-eligible.

## 12. Transient workspace cleanup

The final reconciliation merge candidate must contain **no tracked `workspace/` content**.

Before independent PR review:

1. verify all durable conclusions and Q1–Q7 are transferred;
2. delete all tracked transient artifacts on this branch, including:
   - original research handoff prompt;
   - completed report;
   - adversarial-review prompt;
   - completed review and its later addendum revision;
   - this reconciliation prompt;
   - any checkpoints or diagnostics;
3. keep the exact report and review commits reachable in branch history;
4. do not squash/rebase/force-push them away;
5. run the repository's tracked-workspace check.

The final PR diff must be durable reconciliation only, not a research archive.

## 13. ADR discipline

Follow `docs/architecture/decisions/README.md` exactly.

In particular:

- allocate the next unused ADR number at execution time, including concurrent work;
- use durable semantic naming, never delivery/research coordinates;
- an Accepted semantic change requires supersession;
- do not use an editorial amendment to change an Accepted decision;
- if an existing ADR is only clarified editorially, add the required amendment metadata and explain semantic equivalence in the PR;
- if semantic equivalence is doubtful, supersede instead.

The independent PR reviewer, not this reconciliation author, decides whether any claimed editorial amendment is genuinely semantics-preserving.

## 14. Validation

Run repository-authoritative validation appropriate to the changed surfaces.

At minimum ensure:

- markdown links and document references are valid;
- ADR immutability/supersession rules pass;
- delivery-coordinate leakage checks pass;
- tracked-workspace final-tree check passes;
- `git diff --check` or the repository equivalent passes;
- any tests/checks implicated by semantic support-status or validation documentation changes are run when the repository standard requires them.

Do not claim product validation you did not run.

If the reconciliation changes only docs/ADR/planning, do not invent unrelated runtime work merely to increase validation volume.

## 15. Pull request and independent review

Prepare one reconciliation PR from:

`research/semantic-contract-maturity-durability`

to `main`.

Follow `.github/CONTRIBUTING.md`'s PR-description stability rule.

The PR description must include a compact evidence/provenance section identifying:

- exact research report commit:
  `d0b2c5f127d340ecb4816d6316f5028190c23a13`
- exact independent review commit:
  `81f1f702b6548ea2bd5f92fe64743ea8798821df`
- disposition:
  `ACCEPT WITH QUALIFICATIONS`
- Q1–Q7 transfer map;
- post-review owner constituency evidence and its durable destination;
- knowledge-transfer audit;
- workspace retirement eligibility.

These coordinates are delivery-history provenance, not maintained semantic authority.

Do not review your own PR under the PR Reviewer role.

After creating/updating the PR, stop for the repository's normal independent PR review. Do not merge it yourself.

## 16. Final response

Return only a compact reconciliation handoff containing:

- live-main reconciliation baseline SHA;
- workspace branch;
- reconciliation PR number/link;
- new/superseding ADR number and title;
- concise list of durable surfaces changed;
- confirmation that Q1–Q7 are all encoded;
- exact report SHA;
- exact review SHA;
- validation summary;
- whether final tree contains any tracked `workspace/` content (must be no);
- research lifecycle wording:
  - `reconciliation prepared; research becomes CONCLUDED only when this PR lands`;
- workspace retirement status:
  - `retirement-eligible after merge and independent PR-review validation`, or the exact blocking transfer item;
- next action:
  `independent PR review`.

Do not paste the reconciliation prompt, report, or review into chat.
