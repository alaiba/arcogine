# Implementation handoff: make merge-style PR base synchronization the default

> Temporary implementation handoff for branch `workflow/merge-style-pr-sync`.
> Treat this file as delivery input, not durable repository authority. Remove it from the final PR tree once the implementation and maintained documentation carry the result.

## Baseline

Start from live `main` and re-resolve it before doing any work. This handoff was created from:

`53979efd5ab7ccc2d5e32de2670ad122e6a26a16`

Repository: `alaiba/arcogine`.

Do not treat this prompt or prior conversation as authority over current repository state.

## Mission

Correct Arcogine's stale-PR synchronization workflow so the normal meaning of **"bring this PR current with `main`"** is:

> incorporate the current base into the PR branch with a history-preserving, merge-style **Update branch** operation.

Do **not** use rebase/history replacement as the default freshness operation for ordinary PRs.

Keep the existing exact-head review/CI lifecycle: synchronization produces a new PR head, so CI and reviewer disposition must be re-resolved for that new head before merge readiness can be established.

The safety rule introduced by PR #326 remains valid for any operation that actually performs a non-fast-forward rewrite: a forced replacement must be conditional on the exact inspected old head through lease/CAS/atomic expected-head protection. The correction here is that routine base synchronization should not require such a rewrite in the first place.

## Read first

Before editing, read current versions of:

- `AGENTS.md`
- `docs/development/reviewing.md`
- `.github/agents/pr-reviewer.agent.md`
- `.github/CONTRIBUTING.md`
- `infra/dev/pr-reconcile.mjs`
- `infra/dev/pr-reconcile.test.mjs`
- `infra/dev/pr-watch.mjs` and its tests where synchronization/lifecycle behavior is referenced
- `docs/development/researching.md`
- `.github/agents/researcher.agent.md`

Also inspect the live GitHub repository settings/ruleset that govern `main`, especially:

- required linear history;
- strict required status checks / requirement that PRs be current with base;
- available merge methods;
- Update branch behavior and `allow_update_branch`.

Do not infer a feature-branch linear-history requirement merely from `main`'s protected-branch linear-history rule.

## Quick repository search

Search current `main` for at least:

- `pr-reconcile`
- `rebase`
- `force-with-lease`
- `CAS`
- `expected-head`
- `Update branch`
- `update-branch`
- `base normalization`
- `stale base`
- `behind_by`
- `linear history`
- `research-evidence workspace`
- `Dependabot`

Update every authoritative semantic neighbor that would otherwise leave contradictory routing.

## Current problem to fix

The high-level review/contributor policy requires a stale PR to be reconciled with current `main` before final review, but the ordinary implementation path currently equates that requirement with a rebase through `infra/dev/pr-reconcile.mjs`.

That creates unnecessary machinery:

1. replay the PR commits onto the new base;
2. create replacement commit SHAs;
3. perform a non-fast-forward branch rewrite;
4. require force-with-lease/CAS to avoid overwriting a concurrent push;
5. maintain a separate merge-style path for research-evidence workspaces solely because their historical evidence SHAs must survive.

The repository's protected `main` branch currently requires linear history, but that is not the same claim as requiring every temporary PR branch to remain linear while it is under review. Verify this against live repository authority rather than preserving the helper's current assertion by inertia.

The normal stale-base operation can instead preserve the existing PR history and merge current `main` into the PR branch. That makes the branch current without replacing its existing commits.

## Required workflow behavior

### 1. Ordinary PRs

For an open same-repository ordinary PR that is behind its base and can be synchronized without semantic conflict:

- prefer GitHub's merge-style Update branch operation;
- preserve the pre-update PR head as an ancestor of the resulting head;
- incorporate the exact live base into the resulting head;
- do not rebase or force-push merely for freshness;
- after the update, require `behind_by == 0` and `ahead_by > 0`;
- verify the PR remains open and mergeable state is re-resolved;
- re-resolve CI/checks/reviews/disposition on the resulting exact head.

Prefer a repository/platform operation that is conditional on the inspected head where available. GitHub's pull-request Update Branch REST endpoint supports `expected_head_sha`; use that or an equivalent platform primitive so a concurrent head change fails cleanly rather than acting on stale state.

A normal fast-forward ref move is safe from silent overwrite because Git rejects a non-fast-forward update. Do not introduce a forced ref mutation when the platform can perform the merge-style update directly.

### 2. Research-evidence workspaces

Use the same merge-style synchronization mechanism as ordinary PRs, plus the stronger research evidence invariant:

- every handed-off evidence commit SHA must remain reachable and remain an ancestor as required by `docs/development/researching.md`;
- never rewrite handed-off evidence merely for branch freshness;
- verify the pre-update PR head remains an ancestor of the resulting head.

The research path should remain semantically stricter because of immutable evidence identity, but it should not require a wholly separate synchronization mechanism if the ordinary mechanism is already history-preserving.

### 3. Dependabot

Preserve the existing Dependabot exception.

A maintainer-authored synchronization commit can alter the provenance assumptions behind Arcogine's trusted no-positive-review path. Continue to prefer Dependabot's supported rebase/recreate mechanism where the trusted provenance exception applies. Do not collapse this case into the ordinary update path without separately proving the provenance consequences.

### 4. Explicit history rewrites

If a user or an explicitly documented special workflow asks for a rebase/history rewrite, keep the #326 concurrency invariant:

- construct the complete replacement head before mutation;
- make the non-fast-forward remote mutation conditional on the exact inspected old head using force-with-lease/CAS/atomic expected-head semantics;
- never substitute GET/recheck + unconditional force;
- fail closed if the available surface cannot protect the expected old head.

This should be an exceptional rewrite path, not the default definition of "make the PR current".

## Helper/tooling change

Refactor `infra/dev/pr-reconcile.mjs` and its tests so its normal operation performs the repository-approved merge-style Update branch flow instead of local `git rebase` + `force-with-lease`.

Prefer preserving the existing command entry point unless renaming provides a concrete usability benefit; avoiding unnecessary command churn is preferable. If the name `pr-reconcile` remains, update its help/comments so it no longer claims reconciliation is necessarily a rebase.

A good implementation should approximately:

1. resolve the open PR, live base ref, exact current head, head repository, and current base distance;
2. stop successfully if `behind_by == 0`;
3. require an open same-repository PR for the ordinary path;
4. invoke GitHub's merge-style Update branch operation against the exact inspected head, preferably with `expected_head_sha`;
5. treat head movement, base movement, or merge conflict as a failed mechanical synchronization rather than resolving semantics itself;
6. re-fetch the PR and live base after the operation;
7. verify the old head is an ancestor of the new head;
8. verify the live base is incorporated and `behind_by == 0`;
9. verify `ahead_by > 0` and the PR remains open;
10. leave CI/review/disposition to the normal exact-head lifecycle after returning the new head.

Do not construct a merge commit manually if the platform Update branch operation can own conflict detection and branch mutation safely.

## Tests / executable evidence

Update or replace reconciliation-helper tests to prove at least:

- already-current PR is a no-op;
- stale ordinary PR requests a merge-style Update branch, not rebase;
- the update is bound to the inspected head when the platform primitive supports `expected_head_sha`;
- concurrent head movement fails without overwriting the new head;
- an update conflict fails without implementation-side semantic resolution;
- post-update old-head ancestry is verified;
- post-update live-base ancestry / zero-behind state is verified;
- empty/collapsed PR state is rejected where relevant;
- PR-closed or cross-repository unsupported cases still fail safely;
- exact-head lifecycle behavior remains unchanged after synchronization;
- research-evidence synchronization preserves historical evidence/head ancestry;
- no normal-path test expects `git rebase`, `--force`, or `--force-with-lease` anymore.

Retain focused coverage for any explicit rewrite path that still exists, including its lease/CAS invariant.

Inspect `pr-watch` tests/messages as semantic neighbors. `pr-watch` should continue to classify `behind_by > 0` as a transition that must be synchronized before readiness; change only assumptions that unnecessarily equate synchronization with rebase.

## Documentation/agent reconciliation

Reconcile all maintained workflow authority so there is one coherent routing model.

At minimum:

- `AGENTS.md` — ordinary stale PR => merge-style Update branch by default; explicit rewrite => CAS/lease rule; Dependabot remains special; research adds evidence preservation rather than a separate default mechanism.
- `docs/development/reviewing.md` — reviewer mechanical normalization should describe the merge-style operation without implying ordinary reconciliation is a rebase.
- `.github/agents/pr-reviewer.agent.md` — same execution routing.
- `docs/development/researching.md` and `.github/agents/researcher.agent.md` — preserve evidence-identity requirements while aligning the synchronization mechanism with the new common default.
- `.github/CONTRIBUTING.md` — only change if needed to remove ambiguity or a stale linear-feature-branch implication; do not duplicate lower-level mechanics unnecessarily.

Keep policy layered: `AGENTS.md` owns agent operating mechanics, review docs own review discipline, research docs own evidence semantics, executable tooling owns the concrete synchronization implementation.

## Invariants that must not change

- A PR behind its current base is not merge-ready.
- A stale base is not itself a reviewer finding.
- Reviewer synchronization is mechanical only; conflicts or semantic choices return to implementation/author ownership.
- Review always evaluates current `main` versus the current synchronized PR head.
- Any head change invalidates earlier exact-head authorization; CI and reviewer disposition must be re-resolved.
- The trusted `disposition` check remains the ordinary positive-review authorization gate.
- The repository owner still performs the final merge manually; agents do not merge PRs.
- Research evidence commit identity remains immutable once handed off.
- Dependabot provenance behavior remains explicitly special.
- Protected `main` linear-history/required-check rules remain intact unless separate evidence demonstrates a repository-setting change is necessary.

## Explicit non-goals

Do not mix in:

- PR #321's research-policy content;
- the no-op contents-write follow-up tracked separately in issue #327;
- changes to product/runtime behavior;
- changes to review severity/disposition vocabulary;
- weakening the requirement that a PR be current with `main` before final readiness;
- changing the final PR merge strategy into `main` unless current repository rules prove that is required for this fix;
- enabling/disabling repository settings merely for convenience without first proving the workflow requires it;
- opportunistic cleanup of unrelated GitHub tooling.

## Validation

Use the narrowest current repository commands that cover the changed surfaces. At minimum, expect to run the reconciliation-helper tests directly plus the repository's always-required classification/tooling checks and Markdown validation. Resolve the exact commands from current `main` rather than copying stale commands from this prompt.

Also perform live behavioral verification, where safely possible, against a disposable/test PR or a mechanically safe current PR scenario so the implementation is not only a mocked interpretation of GitHub Update Branch semantics.

Before opening the PR, search again for stale statements equating ordinary base synchronization with rebase/force-push.

## PR / lifecycle requirements

When the implementation is complete:

1. remove this temporary handoff file from the branch's final tree;
2. ensure the branch's net diff contains only the durable workflow/tooling changes;
3. open a narrowly scoped PR against `main` with the problem and corrected synchronization model stated clearly;
4. do not self-certify merge readiness;
5. follow the normal `..` lifecycle for implementation-owned transitions and the independent `./` reviewer contract;
6. never merge the PR as an agent.

## Final report checklist

Report:

- live `main` baseline used;
- files changed and why;
- the resulting ordinary / research / Dependabot / explicit-rewrite routing table;
- exact helper behavior before vs after;
- tests added/changed and their results;
- live Update branch behavior verified;
- any repository setting or API limitation discovered;
- remaining follow-up, only if genuinely outside this slice.
