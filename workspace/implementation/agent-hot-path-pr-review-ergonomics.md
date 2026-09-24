# Implementation handoff: issues #374 and #376 — agent hot-path consolidation and PR/review ergonomics

Repository: `alaiba/arcogine`

## Entry state

Start by re-resolving live `main`. This handoff was grounded against:

`812eff79ba017c6770633aeab74249cbe7a504d3`

Do not assume that SHA is still current when implementation begins. Live `main` is authoritative for landed repository state.

At handoff time:

- open issues: #374 and #376;
- open PRs: none;
- both issues are ready implementation/process work with no research or product-architecture prerequisite;
- the intended delivery is one coherent implementation PR covering both issues because they modify the same authority boundary: what belongs in always-loaded `AGENTS.md` versus canonical contributor/specialized/tooling authorities.

This prompt is transient delivery scaffolding under `workspace/implementation/`. Remove it from the implementation branch before independent PR review and merge readiness.

## Read first

Read these current files from the implementation branch/live target revision before editing:

1. `AGENTS.md`
2. `.github/CONTRIBUTING.md`
3. `.github/agents/work-planner.agent.md`
4. `.github/agents/continuous-improvement.agent.md`
5. `docs/development/continuous-improvement.md`
6. `docs/development/testing.md`
7. `docs/development/reviewing.md`
8. `.github/scripts/check-delivery-labels.mjs`
9. `.github/scripts/check-delivery-labels.test.mjs`
10. root `README.md`
11. root `arcogine`
12. GitHub issues #374 and #376 in full

Also inspect any other specialized agent contract or development document before moving text into or out of it. Do not move detailed rules into an authority that does not actually own them.

Recent merged changes that materially affect this work:

- PR #397 — prompt persistence is now a preflight/entry condition; executable guards should target mechanically observable invariants rather than pinning policy prose.
- PR #398 — repository tooling was consolidated on Node; the delivery-label checker and tests are now `.mjs`. Do not edit or recreate the removed Python checker/test files.
- PR #399 — repository commits are guarded against GitHub identity/email mismatch; preserve the human-owner commit identity requirements.
- PR #400 — bounded semantic closure is now part of completing implementation when maintained process/current-state semantics fan out; satisfy that obligation before review without creating a closure-set artifact.
- PR #401 — retrospective evidence acquisition/analysis was separated; do not pull retrospective redesign into this slice.

## Quick search before editing

Search the current repository, especially `docs/`, for semantic neighbors using terms such as:

- `AGENTS.md`, `canonical commands`, `validating changes`, `backend test environment`
- `Session-close Kaizen`, `.?`, `Consistency review`, `retrospective`
- `PR description`, `base freshness`, `reconciled with main`, `ahead/behind`, `mergeability`, `validation`
- `REV-NNN`, `REV-<NNN>`, `REV-<digits>`, `review/finding identifiers`
- `delivery-label`, `repository tooling`
- specialized-role routing terms: `Work Planner`, `Dependency Maintainer`, `Continuous Improvement`, `Consistency`, `PR Reviewer`, `Research`

Use search for discovery only. For every current-state claim you rely on, read the owning file at the exact implementation target revision.

## Objective

Close GitHub issues #374 and #376 in one coherent process/tooling change.

The result should make `AGENTS.md` a materially smaller and clearer always-loaded contract whose responsibility is:

- repository/task routing;
- genuinely universal agent invariants;
- compact safety/correctness/provenance guards that must be known before a specialized workflow is entered.

Detailed workflow algorithms, command inventories, validation matrices, mutable toolchain/configuration facts, and specialized decision logic should live in the existing authority that owns them and be loaded only when needed.

At the same time, fix the two ergonomics defects owned by #374:

1. implementation/handoff standard work must clearly distinguish live lifecycle facts such as base freshness from stable PR-description validation content, without duplicating the full CONTRIBUTING policy into `AGENTS.md`;
2. PR-local review/finding identifiers should use ordinary variable-width decimal notation `REV-<N>` / examples such as `REV-1`, `REV-9`, `REV-10`, rather than a fixed-width `REV-<NNN>` convention.

Treat the two issues as one authority-consolidation slice, not as a reason to add detailed #374 prose and then remove it again under #376.

## Ownership and design rules

### 1. Keep PR-description policy canonical in CONTRIBUTING

`.github/CONTRIBUTING.md` already owns the stable PR-description invariant:

- semantic scope/rationale/non-goals and validation actually performed are stable PR-body content;
- current head/base SHA, ahead/behind state, base freshness, mergeability, current CI/check state, and current review/disposition state are live lifecycle facts resolved from GitHub;
- immutable evidence coordinates and clearly historical provenance remain legitimate where materially useful.

Preserve that ownership.

In `AGENTS.md`, retain only the shortest universal guard/reference necessary to prevent implementation agents from serializing current lifecycle state into PR-body validation. Do not copy the complete list or policy if a pointer plus compact invariant is sufficient.

In `.github/agents/work-planner.agent.md`, make implementation-prompt generation explicitly preserve both truths:

- branch/base reconciliation is an execution/lifecycle requirement when applicable;
- the resulting current topology must not be instructed into the PR body as validation prose.

The Work Planner should generate handoffs that distinguish those concepts instead of making the implementer infer the distinction.

### 2. Change review-coordinate convention, not its durable-lifetime rule

The executable grammar already accepts digits of any width:

`CANONICAL_REV = /^REV-\d+$/`

Keep that semantic behavior.

Change maintained convention/diagnostic/example wording from fixed-width forms such as `REV-NNN` / `REV-<NNN>` to a variable-width form such as `REV-N` / `REV-<N>` wherever the text is describing the convention.

Normal new examples should use `REV-1`, `REV-2`, `REV-9`, `REV-10`, etc.

Do not rewrite historical PR reviews/comments/commit history. Historical zero-padded identifiers remain valid history.

Do not weaken the rule that any recognizable `REV-<digits>` coordinate is forbidden from durable semantic naming outside its allowed delivery/planning context.

Update the current Node checker diagnostics/comments and tests as needed; do not resurrect the removed Python implementation.

Tests must prove at least:

- single-digit valid identifiers are accepted in allowed planning/delivery-policy context;
- multi-digit valid identifiers are accepted;
- a larger decimal identifier remains accepted;
- malformed `REV-abc` remains rejected;
- well-formed `REV-<digits>` still fails when leaked into durable semantic material;
- policy metavariable examples remain usable without requiring zero padding.

### 3. Reduce AGENTS hot-path duplication evidence-first

Audit at least these `AGENTS.md` surfaces:

- canonical command inventory;
- validating-changes detail and backend/toolchain environment detail;
- mutable tool/version facts;
- Session-close Kaizen mechanics and adjacent Consistency/retrospective decision logic;
- specialized-role entries;
- delivery-coordinate mechanics;
- PR lifecycle/authoring guidance;
- any other detailed workflow algorithm whose canonical owner is elsewhere.

For each block, apply the #376 retention test:

Keep it in `AGENTS.md` only if it is at least one of:

- needed to route to the correct specialized authority;
- a universal invariant across ordinary repository work;
- unsafe to discover only after entering a specialized workflow because doing so creates meaningful safety, correctness, provenance, or irreversibility risk.

Otherwise remove the duplicate detail and point to the existing owner.

Do not simply relocate equivalent prose into a new generic reference/checklist file.

Do not weaken safeguards. Preserve compact early guards when late discovery would be hazardous.

Expected ownership examples:

- root `arcogine --help` / implementation: executable command inventory;
- root `README.md`: normal developer entry/quick-start;
- `docs/development/testing.md`: validation taxonomy, native commands, prerequisites/capability matrix, test/CI detail;
- Gradle/devcontainer/CI config: exact mutable versions/configuration;
- `docs/development/continuous-improvement.md`: continuous-improvement operating model and detailed loop boundaries where it owns them;
- specialized `.github/agents/*.agent.md`: role-specific algorithms and detailed procedure;
- `.github/CONTRIBUTING.md`: contributor workflow, slicing/semantic closure, PR-description stability;
- `AGENTS.md`: routing plus universal/safety-critical agent invariants.

Be careful with Session-close Kaizen ownership: current documents state that `AGENTS.md` owns the trigger/core agent obligation while `docs/development/continuous-improvement.md` owns the broader operating model. Reconcile the two deliberately rather than assuming all Kaizen text must move wholesale.

### 4. Output discipline for Session-close Kaizen

Satisfy #376's output acceptance:

- `.?` must remain unambiguous from the hot path;
- detailed decision mechanics should live with their proper continuous-improvement authority unless an early guard has demonstrated safety value;
- default output should report material findings/actions plus the deletion verdict, not narrate every adjacent workflow that was intentionally not invoked.

Do not add a new ceremony, state ledger, or checklist to accomplish this.

### 5. Mechanical PR-body guard is optional, not required

Issue #374 explicitly asks to evaluate a narrow deterministic guard; it does not require one.

First fix authority placement and implementation/handoff salience.

Only add mechanical enforcement if the bad condition can be recognized with a low-false-positive, semantically justified rule. Do not add a broad natural-language keyword ban over terms such as `main`, `merge`, or `CI`.

PR #397 reinforced that executable guards should protect mechanically observable invariants rather than merely assert prescribed agent/process wording.

If no robust narrow guard is justified, leave it out and make the PR explanation clear that the optional enforcement path was evaluated and intentionally not added.

## Acceptance criteria

### Issue #374

- implementation-facing standard work classifies reconciliation/base freshness as live lifecycle state, not PR-description validation;
- Work Planner handoffs preserve reconciliation requirements without encouraging current topology in PR bodies;
- immutable/historical provenance remains permitted;
- base freshness remains a merge-readiness condition;
- maintained review-coordinate convention is variable-width decimal `REV-<N>`;
- normal examples use unpadded numbering;
- digits-only checker semantics remain;
- malformed identifiers and durable leakage still fail;
- tests cover both single- and multi-digit identifiers;
- no historical review/comment renumbering.

### Issue #376

- `AGENTS.md` has a clearly narrowed routing + universal/safety-critical responsibility;
- it no longer duplicates the full `./arcogine` command inventory or detailed subsystem validation matrices;
- mutable tool/version/config facts move out when another executable/canonical authority owns them, unless an early agent-safety reason justifies retention;
- specialized workflow algorithms are routed to their specialized authority instead of living on the hot path;
- `.?` stays unambiguous while detailed continuous-improvement mechanics are owned appropriately;
- Session-close output defaults to material findings/actions plus deletion verdict;
- README, testing guide, executable help, CONTRIBUTING, specialized agent contracts, and tooling/configuration have explicit non-overlapping responsibilities;
- a general coding agent can still determine what authority to load and what validation class applies;
- documentation-only work remains distinguishable from executable validation work;
- `AGENTS.md` has a material net reduction;
- no new generic command/workflow/checklist document is introduced.

## Semantic-closure requirement

This is a process/current-state documentation and repository-tooling change, so PR #400's bounded semantic-closure rule applies.

Before handing the candidate to review:

1. identify the concepts changed: hot-path authority, PR lifecycle-vs-description state, review-coordinate convention, validation authority, Kaizen output/ownership;
2. search maintained docs, agent contracts, tooling diagnostics/tests, and contributor guidance for both new terminology and plausible old assumptions;
3. reconcile stale semantic neighbors in the same PR;
4. keep the search bounded to these concepts; do not turn this into a repository-wide Consistency review.

Do not create a closure-set artifact.

## Explicit non-goals

Do not:

- change product/runtime behavior;
- change architecture/domain semantics;
- redesign the PR disposition protocol;
- remove base freshness from merge readiness;
- prevent live topology from being reported in chat when the user explicitly asks for repository state;
- ban immutable SHA evidence or clearly historical provenance;
- rewrite historical PR bodies/reviews/comments;
- make review IDs globally unique;
- change the reserved `REV-` namespace or its delivery-vs-durable lifetime semantics;
- add a generic natural-language policy linter;
- create a new command/workflow/checklist document where an existing authority suffices;
- redesign the retrospective pipeline landed in #401;
- broaden into unrelated research/planning/product work.

## Validation

Select the narrowest validation for the actual diff.

If `.github/scripts/check-delivery-labels.mjs` or its test changes, run at minimum:

```bash
node --test .github/scripts/check-delivery-labels.test.mjs
node .github/scripts/check-delivery-labels.mjs
```

Because this slice is likely to touch repository-owned tooling and multiple maintained docs, also run the canonical repository-tooling suite:

```bash
bash .github/scripts/check-repository-tooling.sh
```

Run:

```bash
git diff --check
```

Use `./arcogine check` only if the final change reaches Java/backend surfaces; documentation/process/repository-tooling changes do not require Java validation merely for ceremony. Use `./arcogine check --full` only if dependency-audit or secret-scan behavior is actually in scope.

Report every validation command/tool actually used, its outcome, and anything unavailable/skipped. Never call partial validation a full pass.

## Branch, commit, and PR requirements

- Re-resolve live `main` before implementation and branch from/reconcile to it as required.
- Use the repository owner's human Git identity for commits. Do not commit with an agent/model/provider/bot identity.
- Do not add AI attribution, session URLs, `Co-Authored-By` model footers, or generated-by trailers.
- Keep commits narrowly about #374/#376.
- Before final review, reconcile materially moved `main` and consider the net diff against current `main`.
- Open one PR against `main` that clearly states it closes/addresses #374 and #376.
- Keep the PR body stable under branch evolution. Describe semantic scope, rationale, non-goals, and validation commands/checks performed.
- Do not put current head/base SHA, ahead/behind count, base freshness, mergeability, current CI state, or current review/disposition state into the PR body as validation facts.
- Exact SHAs are fine only for deliberately immutable evidence or clearly historical provenance.
- Do not merge the PR; independent PR review belongs to the PR Reviewer contract and final merge is owner-only.

## Transient-artifact cleanup

This handoff file must not survive into the final merge candidate.

Before independent review:

- delete this `workspace/implementation/` prompt from the branch;
- inspect branch-added files for other transient execution/handoff artifacts;
- run the repository transient-workspace check through the repository-tooling suite (or the focused checker when appropriate);
- confirm there are no tracked `workspace/` paths in the candidate.

## Final implementation report

At completion, report concisely:

- PR number/title and current implementation head;
- the current `main` baseline used for the final candidate;
- files changed and the authority consolidation performed;
- how #374's PR-body distinction is encoded without duplicating CONTRIBUTING;
- how the `REV-<N>` convention and Node checker/tests were updated;
- how #376 reduced `AGENTS.md` and where removed details are now owned;
- the disposition of the optional PR-body mechanical guard and why;
- semantic-neighbor/closure surfaces checked;
- validation commands and outcomes;
- confirmation that the transient prompt/workspace files were removed before review;
- any remaining blocker, if one exists.

Do not claim merge readiness from implementation alone. Hand the completed candidate to the independent PR Reviewer when implementation and bounded semantic closure are complete.
