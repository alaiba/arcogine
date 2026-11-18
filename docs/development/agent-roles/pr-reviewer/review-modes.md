# Review modes and continuation

Part of the [PR Reviewer contract](../pr-reviewer.md#review-procedure); every complete review, re-review and `./` continuation follows all of its routed topics.

## Initial review

Normalize the base if needed, then review current `main` against the resulting current PR head. Perform full change-impact, evidence, compatibility, and semantic-neighbor analysis appropriate to the PR's risk.

## Re-review

Normalize the base if needed, resolve the resulting head, re-evaluate every prior finding, inspect changes since the previously reviewed head, and scan the full current-main-to-current-head net diff for regressions or newly exposed issues.

Carry every prior finding forward under the same `REV-<N>` identity and set its status to `OPEN`, `RESOLVED`, or `OBSOLETE` after checking the current head. If a resolved defect recurs, reopen the same identity as `OPEN` and describe the recurrence as a regression in review prose; `REGRESSION` is not a finding status. Do not omit resolved or obsolete identities from the lifecycle record.

## Concise re-review after mechanical base synchronization

Use the [verified base-synchronization re-review](../../reviewing.md#verified-base-synchronization-re-review) path only when a previous complete `READY TO MERGE` review and **every** intervening mechanical merge can be verified from live GitHub evidence. Compare the old reviewed head, incoming base changes, and the entire current-base-to-head net diff; independently inspect semantic, test and toolchain interactions, and re-evaluate prior findings. Do not infer eligibility merely from disjoint filenames or a merge commit message.

If the eligible change preserves the earlier review's conclusions, write a **short but complete** formal `COMMENT` review referring to that earlier review, carrying the finding lifecycle, CI, limitations and new exact-head canonical disposition. Escalate to ordinary full re-review for unverified history, changed PR implementation, affected evidence, unresolved findings or material interaction uncertainty. No old disposition is inherited and no trusted gate is bypassed.

## Final review

Before recommending merge, re-resolve current `main` and the PR head. If the branch is stale again, normalize it before continuing. Review the current net diff and reviewer-owned criteria; record externally visible mergeability or validation facts without orchestrating lifecycle gates. Do not rely on an earlier clean review if normalization or another commit changed the head.

## Targeted review

When the user requests only a specific concern, review that concern thoroughly but label the result targeted. Do not turn a targeted architecture, API, security, or test inspection into an implicit full-PR approval.

A Dependabot PR for which the trusted base-side workflow verifies both the exact GitHub Dependabot account as PR opener and as the actor of the `CI` pull-request workflow run for the exact current head does not need a reviewer-authored `READY TO MERGE` to satisfy the required `disposition` check. If the user explicitly asks you to review such a PR, still perform the requested review normally; the provenance exception removes a positive merge-gate requirement, not the ability to request independent analysis. If that review finds a blocker, a current-head canonical `CHANGES REQUIRED` revokes the default authorization.

## Continuation shorthand

When the user's entire message is `./` treat it as an instruction to continue the independent PR-review workflow without asking for clarification. `./` is review-only shorthand; repository-wide remediation shorthand `..` and Session-close Kaizen shorthand `.?` are defined in `AGENTS.md` and are not instructions to perform reviewer-side remediation beyond the permitted pre-review base normalization.

- If a PR is currently being reviewed and remains open, re-resolve current `main`, the live PR head, base freshness, reviews, unresolved threads, and CI; normalize a stale base if possible; then perform a re-review of that PR.
- If the current PR has been merged or closed, or no PR is currently active, find an open PR and perform a complete review of it.
- Prefer an open non-draft PR that has not already reached a completed disposition in the current reviewer workflow. When several qualify, review the most recently updated one.
- If only draft PRs are available, review the most recently updated draft and identify the result as a draft review.
- If no open PR exists, report that there is currently nothing to review.
- Do not ask what `./` means or ask the user to select a PR.
- Do not mechanically post a duplicate GitHub review when the PR head and relevant review state have not changed. Re-check the live state and report that the previous disposition remains valid when appropriate.
