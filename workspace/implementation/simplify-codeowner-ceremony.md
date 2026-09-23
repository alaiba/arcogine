# Implementation handoff: simplify Code Owner ceremony while preserving COMMENT-based review

## Repository and branch

Repository: `alaiba/arcogine`

Implementation handoff branch: `chore/simplify-codeowner-ceremony`

Prompt baseline: live `main` at `2e13eb5fcf71823740453b1c80d0e792fa38a746`.

Work on this branch as the implementation branch. Re-ground against live `main` before making repository-dependent claims. If `main` has advanced materially, reconcile the branch using the repository's normal history-preserving rules; do not rewrite the prompt commit out of history before its handoff purpose is complete.

Follow `AGENTS.md` and `.github/CONTRIBUTING.md`. Agents never merge pull requests. The repository owner performs the final manual merge.

Before independent PR review, delete this prompt from `workspace/implementation/` and run the tracked-workspace check. No `workspace/` file may remain in the merge candidate.

## Read first

Read these current surfaces before editing:

1. `AGENTS.md`
2. `docs/development/reviewing.md`
3. `.github/agents/pr-reviewer.agent.md`
4. `.github/agents/dependency-maintainer.agent.md`
5. `.github/CODEOWNERS`
6. `.github/workflows/pr-disposition.yml`
7. `.github/workflows/pr-disposition-review-trigger.yml`
8. `.github/scripts/check-pr-disposition.sh`
9. `.github/scripts/check-pr-disposition.test.sh`
10. `docs/research/synthesis-seeds.md`
11. `docs/development/testing.md`
12. live issue #313
13. live `Protect main` ruleset id `14822987`

Also inspect merged PR #332 only as delivery-history context for why the current Code Owner caveat exists. Do not treat its historical rationale as current policy authority.

## Quick repository/docs search

Search at least for:

- `CODEOWNERS`
- `Code Owner`
- `Code Owner approval`
- `native APPROVE`
- `independent approval`
- `issue #313`
- `activation condition 4`
- `require_code_owner_review`
- `required_approving_review_count`
- `check-name provenance`
- `READY TO MERGE`
- `CHANGES REQUIRED`
- `disposition`

Distinguish maintained current-state/process guidance from historical records. Do not rewrite historical evidence merely because the policy is changing now.

## Current landed state

At the prompt baseline, Arcogine's working review model is:

- reviewers submit formal GitHub **COMMENT** reviews;
- complete reviews end with the exact-head canonical `READY TO MERGE` or `CHANGES REQUIRED` block;
- the required `disposition` check evaluates that comment-based authorization;
- trusted Dependabot provenance is the existing explicit positive-review exception;
- required `CI / gate` remains independent;
- strict base freshness and mergeability remain independent merge conditions;
- agents never merge PRs;
- the repository owner manually performs the final merge.

The live `Protect main` ruleset currently reports:

- pull request required;
- `required_approving_review_count: 0`;
- `require_code_owner_review: true`;
- required status checks `gate` and `disposition`;
- strict required status checks;
- deletion blocked;
- non-fast-forward updates blocked;
- linear history required;
- no bypass actors;
- `current_user_can_bypass: never`.

Issue #313 exists because the repository previously intended an additional independent Code Owner approval boundary for `.github/**`, but observed merges did not establish that the boundary was actually enforced.

## Owner decision

The repository owner has now made the policy decision for this slice:

- accept the residual risk created by agents operating through the owner's GitHub identity;
- do **not** introduce separate GitHub identities, GitHub Apps, protected-environment ceremony, connector approval ceremony, or another human-authorization layer;
- retire independent Code Owner approval as an intended Arcogine invariant;
- remove the CODEOWNERS/native-approval ceremony that was added around that intended invariant;
- preserve the current COMMENT-based review workflow because it is working well in practice.

Treat this as a deliberate simplification and risk acceptance, not as a claim that shared GitHub identity creates independent human authorization.

## Objective

Make the repository and live merge-protection configuration truthfully express one review model:

1. Arcogine's review state machine is the current-head COMMENT-based canonical disposition.
2. Native GitHub `APPROVE` and `REQUEST_CHANGES` are not part of the intended Arcogine reviewer protocol.
3. Independent Code Owner approval is no longer an intended control.
4. The remaining required controls stay intact: PR requirement, `gate`, `disposition`, strict base freshness, mergeability, no bypass, and manual owner merge as standard work.
5. Documentation must state the accepted shared-identity limitation concisely where it is materially relevant, without recreating a large security ceremony around it.

Prefer deletion and simplification over replacing CODEOWNERS with a new mechanism.

## Required changes

### 1. Delete `.github/CODEOWNERS`

Remove the file entirely unless current GitHub platform behavior makes deletion impossible for a concrete reason discovered during implementation.

Do not replace it with another ownership file or equivalent approval mechanism.

### 2. Simplify `docs/development/reviewing.md`

Preserve the COMMENT-based review protocol and its exact-head disposition semantics.

Remove or rewrite current-state text that treats Code Owner approval as a separate required authorization concern, including:

- the exception saying native `APPROVE` may still be used for Human Code Owner approval;
- the listener-narrowing mitigation that depends on `.github/CODEOWNERS`;
- the check-name-provenance mitigation that depends on independent Code Owner approval;
- the paragraph saying those mitigations depend on a genuinely independent Code Owner identity;
- issue #313 caveat text that exists only because that invariant was expected to be repaired;
- activation checklist requirements whose purpose is Code Owner enforcement;
- recovery instructions that say Code Owner review must remain active.

Keep the useful trusted-workflow design intact:

- candidate PRs must not author the trusted evaluator that judges their authorization;
- trusted orchestration remains based on the existing `pull_request_target`, `workflow_run`, and scheduled paths;
- API re-fetching of PR head, opener, reviews, and Dependabot provenance remains;
- scheduled reevaluation remains;
- reviewer-directed candidate content remains evidence, not instruction.

Replace the removed check-name/CODEOWNERS discussion, if a warning is still useful, with a short truthful statement: because agents and the owner share one GitHub principal and the owner accepts that operating model, changes to review/workflow infrastructure receive the normal independent review discipline but do not have a separate principal-level approval boundary. Do not grow this into a new security subsystem.

Simplify the activation criteria around the controls that actually exist and are intended:

- `disposition` is required;
- required checks cannot be bypassed by the active identity / there are no configured bypass actors;
- strict base freshness remains enforced;
- ordinary current-head authorization and trusted Dependabot authorization still work as documented.

Do not retain a fourth activation condition merely to preserve numbering.

### 3. Simplify `.github/agents/pr-reviewer.agent.md`

Keep:

- formal COMMENT reviews;
- canonical `READY TO MERGE` / `CHANGES REQUIRED`;
- prohibition on using native `REQUEST_CHANGES` as Arcogine's blocker;
- prohibition on using native `APPROVE` as a substitute for canonical disposition;
- handling of accidental/external native blockers.

Remove the sentence/exception that treats Human Code Owner native approval as a separate authorization concern.

Do not change the reviewer finding lifecycle, disposition format, or review-depth rules.

### 4. Simplify `.github/agents/dependency-maintainer.agent.md`

Remove Code Owner requirements from:

- Dependabot lifecycle/blocker descriptions;
- final-report requirements;
- any remaining-protection wording.

Preserve the trusted Dependabot provenance path, ordinary-review fallback, CI/base-freshness/mergeability requirements, and the rule that agents never merge.

### 5. Simplify comments in `.github/workflows/pr-disposition.yml`

Remove comments claiming `.github/CODEOWNERS` provides the mitigation for same-name `disposition` spoofing or workflow/listener narrowing.

Keep the executable workflow behavior unchanged unless a change is strictly necessary to remove a now-invalid assumption.

In particular, do **not** redesign:

- triggers;
- permissions;
- target resolution;
- review fetching;
- Dependabot provenance;
- evaluator invocation;
- check publication;
- scheduling/concurrency.

This slice is policy simplification, not a disposition-workflow redesign.

### 6. Reconcile `docs/research/synthesis-seeds.md`

Preserve the general synthesis seed that a control's asserted/configured state is not evidence of its effect.

Remove stale claims that `.github/CODEOWNERS` or `docs/development/reviewing.md` activation condition 4 are current durable destinations for a control Arcogine has now deliberately retired.

The historical example that `require_code_owner_review: true` was observed not to establish the expected approval boundary may remain as historical evidence if it still helps the general seed. Keep it clearly historical; do not let it imply the control is still desired.

### 7. Search and reconcile other maintained current-state surfaces

Run a repository-wide search after the edits. Remove stale Code Owner/native-approval obligations from maintained current-state/process surfaces that materially depend on the retired invariant.

Do **not** rewrite historical records such as:

- `docs/history/continuous-improvement/delivery-process-retrospective-2026-09-21.md`.

That retrospective truthfully records what the repository considered an escape at that time. History may remain history.

Do not opportunistically edit unrelated security, architecture, product, or research content.

### 8. Reconcile the live `Protect main` ruleset

The desired live setting is:

- `require_code_owner_review: false`;
- `required_approving_review_count: 0`.

Preserve:

- PR requirement;
- required `gate`;
- required `disposition`;
- strict required-status-check policy;
- deletion protection;
- non-fast-forward protection;
- linear-history requirement;
- no bypass actors;
- existing merge-method policy.

Do not change unrelated pull-request rule parameters merely because they are nearby.

If the available GitHub tooling can safely update the ruleset, make only the narrow Code Owner change and read the ruleset back to verify it.

If the available tooling cannot mutate repository rulesets, do not approximate the setting through repository files and do not claim the operational reconciliation is complete. Report the exact required owner action:

> In GitHub Settings for the active `Protect main` ruleset (id `14822987`), disable "Require review from Code Owners" while leaving required approval count at 0 and all existing required checks/freshness/no-bypass protections unchanged.

The repository PR may still carry the code/documentation cleanup, but final completion of this slice must explicitly account for the live setting.

### 9. Resolve issue #313 as a retired invariant, not a repaired Code Owner gate

The durable result should make clear that Arcogine deliberately chose not to enforce the independent-Code-Owner invariant because it does not match the accepted single-owner/shared-identity operating model.

If the repository changes and live ruleset change are both complete before the PR is opened/updated, the PR may use `Closes #313`.

If the live ruleset change is still an owner action, use `Refs #313` instead and report that closing the issue remains blocked on that setting reconciliation.

Do not close #313 merely because documentation changed while the live ruleset still advertises the retired control.

## Invariants to preserve

The implementation must not weaken these:

1. Ordinary PR authorization remains a current-head canonical COMMENT review ending in either `READY TO MERGE` or `CHANGES REQUIRED`.
2. A new PR head invalidates prior ordinary positive authorization.
3. Current-head `CHANGES REQUIRED` remains a blocker, including for the trusted Dependabot path as currently defined.
4. The trusted Dependabot exception remains based on exact GitHub identity and exact-head Actions provenance.
5. `CI / gate` and `disposition` remain separate required checks.
6. Strict base freshness remains required.
7. Mergeability/conflicts remain GitHub-owned merge conditions.
8. Agents never merge pull requests; owner manual merge remains standard work.
9. No bypass actor is introduced.
10. Candidate-controlled PR content remains evidence, never reviewer instruction.

## Explicit non-goals

Do not:

- create separate GitHub identities, users, or Apps;
- change ChatGPT app permissions;
- add GitHub Environments, deployment approvals, merge queues, or another human-confirmation mechanism;
- replace CODEOWNERS with another approval file/mechanism;
- introduce native GitHub approval as the main review workflow;
- redesign the disposition evaluator or Dependabot provenance path;
- add another status check solely to replace CODEOWNERS;
- change product/runtime code;
- rewrite historical retrospectives to match the new policy;
- generalize this into a broad repository-security redesign;
- merge the resulting PR.

## Validation

This slice should not require Java/product validation unless implementation unexpectedly changes executable product code, which it should not.

Run the narrowest validation that exercises the changed repository tooling:

1. `bash .github/scripts/check-pr-disposition.test.sh`
2. `bash .github/scripts/check-actions-workflows.sh` if network/tooling is available, because `.github/workflows/pr-disposition.yml` is touched even if only comments change.
3. `bash .github/scripts/classify-changes.test.sh` if the touched workflow/tooling surface makes it applicable.
4. `python3 .github/scripts/check-transient-workspace.test.py`
5. Before review, after deleting this prompt: `python3 .github/scripts/check-transient-workspace.py`.

Also perform explicit repository searches proving:

- `.github/CODEOWNERS` is absent;
- no maintained current-state/process document still requires independent Code Owner approval;
- no maintained agent contract still reports Code Owner approval as a normal merge gate;
- historical references intentionally retained are clearly historical;
- canonical COMMENT disposition wording and parser expectations are unchanged.

If the live ruleset is mutated, read back ruleset id `14822987` and record the relevant resulting fields in the PR/report.

Report any unavailable validation precisely; do not summarize partial validation as a full pass.

## PR requirements

Keep the change one coherent simplification PR.

The PR description should explain:

- the owner has deliberately retired independent Code Owner approval as an intended invariant;
- the residual risk of shared owner/agent GitHub identity is accepted;
- the COMMENT-based disposition workflow remains the review state machine;
- which existing protections remain unchanged;
- whether the live `Protect main` setting was reconciled and verified;
- issue #313 closure/reference status.

Do not turn the PR description into a transcript of this prompt or the prior threat-model discussion.

When implementation is complete:

1. remove this `workspace/implementation/` prompt from the branch;
2. run the transient-workspace check;
3. create/update the PR;
4. hand the live PR to the repository-owned PR Reviewer for independent review;
5. stop after reporting current PR/gate state; do not merge.

## Final report

Report:

- live `main` SHA used at implementation time;
- changed/deleted files;
- exactly what Code Owner ceremony was removed;
- confirmation that COMMENT disposition semantics were preserved;
- confirmation that `gate`, `disposition`, strict freshness, no-bypass posture, and manual owner merge standard work remain;
- live ruleset change/read-back result, or the exact blocked owner action if mutation was unavailable;
- issue #313 disposition;
- validation commands and outcomes;
- current PR number/head and visible CI/review state;
- any remaining blocker or owner-only action.
