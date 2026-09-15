# Implementation handoff: add a safe fast-forward fallback for PR base synchronization

> Temporary implementation handoff for branch `workflow/pr-sync-fast-forward-fallback`.
> Treat this file as delivery input, not durable repository authority. Remove it from the final PR tree once the implementation and maintained workflow documentation carry the result.

## Baseline

Start from live `main` and re-resolve it before doing any work. This handoff was created from:

`c0b4f8091be00f70f112d91d1172fbe975f8fe4e`

Repository: `alaiba/arcogine`.

PR #329 (`Use merge-style PR base synchronization`) is landed at this baseline. PR #321 remains an open motivating case: at handoff creation its head is `b79702c67a204280e6ef17746d5250607a1743e7`, while live `main` is the SHA above. Re-resolve both before using them as evidence; do not treat these coordinates as durable semantics.

Do not treat this prompt or prior conversation as authority over current repository state.

## Mission

Correct the operational gap left by PR #329 without undoing its semantic improvement.

Keep **history-preserving merge-style synchronization** as the default meaning of "bring this PR current with its base." Prefer GitHub's native Update branch operation when the current execution surface exposes it. When that endpoint is unavailable but repository-scoped Git data/mutation primitives are available, permit a second, narrowly mechanical implementation:

1. construct the exact merge commit that incorporates the live base into the inspected PR head;
2. move the PR branch to that merge commit with a **non-forced fast-forward ref update**;
3. fail closed if the merge tree cannot be mechanically proven;
4. verify the resulting ancestry, base distance, PR diff, and lifecycle state before continuing.

Do **not** return to rebase/history replacement as the ordinary freshness path.

Preserve the #326/#329 rule for actual history rewrites: any forced/non-fast-forward replacement still requires an exact inspected-head lease/CAS/atomic expected-head guard. The correction in this slice is that a history-preserving merge followed by `force=false` is a different operation and should not be blocked merely because the harness lacks the native Update branch endpoint.

## Read first

Before editing, read current versions of:

- `AGENTS.md`
- `docs/development/reviewing.md`
- `.github/agents/pr-reviewer.agent.md`
- `.github/CONTRIBUTING.md`
- `infra/dev/pr-reconcile.mjs`
- `infra/dev/pr-reconcile.test.mjs`
- `infra/dev/pr-watch.mjs` and its tests where base freshness/lifecycle behavior is referenced
- `docs/development/researching.md`
- `.github/agents/researcher.agent.md`
- PR #329, including its merged diff and review
- open PR #321 only as a live motivating case, not as authority for the general rule

Also inspect the live GitHub connector/mutation surface available in the implementation environment. Confirm whether it exposes:

- native pull-request Update branch;
- commit/tree/blob creation;
- non-forced branch ref update;
- compare/commit/tree reads.

The workflow must be defined by semantic capabilities, not by one harness's tool names.

## Quick repository search

Search current `main` for at least:

- `Update branch`
- `expected_head_sha`
- `base-normalization protocol`
- `pr-reconcile`
- `force-with-lease`
- `CAS`
- `atomic expected-head`
- `manually construct a merge commit`
- `forced ref mutation`
- `fast-forward`
- `behind_by`
- `stale base`
- `research-evidence workspace`
- `Dependabot`

Search under `docs/` as well as executable/agent surfaces. Update every authoritative semantic neighbor that would otherwise leave contradictory routing.

## Current problem to fix

PR #329 correctly changed ordinary stale-PR synchronization from rebase/history replacement to GitHub's history-preserving merge-style Update branch operation.

However, current `AGENTS.md` now makes one implementation mechanism mandatory: it requires the Update branch mutation to be bound to the inspected head with `expected_head_sha` (or equivalent atomic expected-head semantics) and explicitly says not to manually construct a merge commit for routine freshness.

The repository-owned adapter, `infra/dev/pr-reconcile.mjs`, implements that operation by invoking authenticated `gh api` against `PUT /pulls/{number}/update-branch`.

That is safe where `gh`/the native endpoint is available, but it leaves a connector-only runtime unable to normalize a stale PR even when that runtime exposes enough lower-level GitHub primitives to perform the same history-preserving Git operation safely in ordinary mechanical cases:

- read refs/commits/trees/comparisons;
- create blobs/trees/commits;
- move a branch ref with `force=false`.

The resulting workflow is semantically correct but operationally incomplete.

## Correct routing model

### 1. Native Update branch remains preferred

For an ordinary open same-repository PR behind its base, when GitHub's Update branch operation is directly available:

- use it;
- bind it to the inspected head with `expected_head_sha` where supported;
- let GitHub own merge computation/conflict detection;
- verify all existing post-update invariants.

`infra/dev/pr-reconcile.mjs` remains a valid adapter for environments with authenticated `gh`. Do not remove or weaken this path merely to add the connector fallback.

### 2. Mechanical Git-data fallback

When the native Update branch operation is unavailable but repository-scoped Git data and mutation primitives are available, allow a history-preserving fallback **only when the exact merge result can be mechanically proven without semantic judgment**.

Use symbolic names in implementation/review reasoning:

- `H` = inspected PR head;
- `B` = inspected live base head (normally current `main`);
- `A` = merge base of `H` and `B`;
- `T` = mechanically proven merged tree;
- `M` = merge commit with first parent `H`, second parent `B`, tree `T`.

The intended branch movement is:

```text
H -> M
```

with `force=false`.

The fallback is not permission to perform arbitrary server-side merge emulation. It is available only where the exact final tree is provable from repository data.

### 3. Research-evidence workspaces

Research-evidence workspaces use the same history-preserving synchronization routing, plus the stronger evidence-custody invariant already defined by the research workflow:

- every handed-off evidence SHA remains reachable;
- the pre-update PR head remains an ancestor of the resulting head;
- no handed-off evidence commit is rewritten merely for freshness.

If those ancestry guarantees cannot be established, fail closed.

### 4. Dependabot remains special

Preserve the existing Dependabot provenance exception.

A maintainer-authored synchronization commit can revoke the trusted no-positive-review path. Continue to prefer Dependabot's own supported rebase/recreate mechanism while that provenance exception applies. Do not silently route trusted Dependabot PRs through a maintainer-authored connector merge.

### 5. Explicit history rewrites remain CAS/lease protected

For any explicit rebase, replacement commit, force-push, or other non-fast-forward rewrite:

- construct the complete replacement head before mutation;
- require the final remote mutation to be conditional on the exact inspected old head through lease/CAS/atomic expected-head semantics;
- never substitute an immediate GET/recheck plus unconditional force;
- fail closed if the mutation surface cannot protect the expected old head.

This rule from #326 remains correct. Do not weaken it while fixing the ordinary merge-style path.

## Mechanical fallback protocol

The connector fallback should require all of the following.

### A. Resolve the live candidate

1. Re-resolve the PR state, head repository, head branch, exact head `H`, base ref, and live base head `B`.
2. Require an open same-repository PR.
3. Resolve merge base `A` from live Git data; do not use historical PR `base.sha` as freshness evidence.
4. Confirm the PR still has a non-empty intended change against the live base.

### B. Prove that merge construction is mechanical

Determine the exact changes on both sides from the common merge base:

- PR side: `A -> H`;
- base side: `A -> B`.

Only construct `T` when the implementation can prove the same final tree without choosing merge semantics.

A conservative first implementation may accept the common simple case where the two sides modify disjoint paths and every changed entry can be reproduced exactly from Git tree/blob metadata.

Fail closed rather than guessing when any of the following makes the exact merge result uncertain:

- both sides modify the same path;
- file/directory ancestor-descendant collisions occur;
- rename/copy interpretation is required;
- delete/modify interactions are ambiguous;
- file modes cannot be preserved exactly;
- symlink or submodule entries are involved and the available primitives cannot prove their exact result;
- case-folding/path-normalization ambiguity exists;
- any other tree shape requires semantic conflict resolution or unimplemented Git merge behavior.

This fallback may be deliberately narrower than GitHub's native Update branch operation. Safety is more important than maximizing the set of PRs it can normalize.

### C. Construct the exact merged tree and merge commit

For an accepted mechanical case:

1. Start from the live base tree `B` (or otherwise construct the exact same final tree deterministically).
2. Apply the PR-side path states from `H` exactly, including exact blobs, modes, and deletions, only for the mechanically proven branch-only changes.
3. Verify the resulting tree `T` contains both the current base content and the intended PR content.
4. Create merge commit `M` with:
   - first parent = `H`;
   - second parent = `B`;
   - tree = `T`;
   - a concise normal merge-style synchronization message;
   - repository-compliant human author/committer identity and no AI/bot/session attribution.

Do not point the PR branch at `B` as an intermediate state.

### D. Re-resolve immediately before mutation

Immediately before moving the branch ref:

1. re-read the PR head and require it still equals `H`;
2. re-read the live base and require it still equals `B`;
3. if either moved, discard `M` as a candidate and recompute from the new state.

This recheck reduces the race window but is not, by itself, the concurrency guard.

### E. Update with server-enforced fast-forward semantics

Move the PR branch from its current state toward `M` with a **non-forced ref update** (`force=false` or platform-equivalent).

The safety property is:

- if another actor advances or rewrites the PR branch to a head that is not an ancestor of `M`, the `H/current -> M` move is no longer fast-forward and GitHub rejects it;
- never retry that rejection with `force=true`;
- re-resolve the new head and restart normalization from current state.

Treat server-enforced fast-forward rejection as sufficient concurrency protection for this history-preserving fallback against ordinary concurrent forward/divergent head movement.

Do **not** claim this is identical to exact-head CAS. Document the narrow residual race explicitly: if another actor deliberately force-resets the branch backward to an ancestor of `M` after the final recheck but before the non-forced update, `ancestor -> M` can still be a fast-forward. Native Update branch with `expected_head_sha` therefore remains the stronger/preferred route when available.

The protocol deliberately accepts that narrow residual race for the connector fallback rather than making ordinary synchronization impossible in connector-only environments. Do not generalize that acceptance to forced/history-rewriting mutations.

### F. Post-update verification

After the ref update, re-resolve repository state and require all of the following:

- PR is still open;
- PR head is the expected merge commit `M` (unless a subsequent safe concurrent advancement is detected and separately resolved);
- old head `H` is an ancestor of the resulting head;
- incorporated base `B` is an ancestor of the resulting head;
- the **current** live base is an ancestor of the resulting head;
- `behind_by == 0` against the current live base;
- `ahead_by > 0`;
- the net live-base -> PR-head diff remains non-empty and contains the intended PR change without unrelated semantic edits;
- merge commit parents/tree match the plan that was verified before mutation.

If `main` advanced from `B` to `B2` after the branch update, the update itself need not be rolled back. Post-verification will show the PR behind again; repeat normalization against the new live base.

After any successful synchronization, re-resolve CI/checks, submitted reviews, trusted `disposition`, and mergeability. Exact-head lifecycle rules remain unchanged.

## Policy/documentation changes

Reconcile the maintained workflow authority so it describes one coherent capability-based routing model.

At minimum:

- `AGENTS.md`
  - keep merge-style synchronization as the canonical behavior;
  - native Update branch + `expected_head_sha` is preferred when exposed;
  - add the mechanical Git-data fallback described above;
  - replace the current blanket prohibition on manually constructing a merge commit with a fail-closed rule: construction is allowed only when the exact tree is mechanically provable and the branch update is non-forced;
  - keep CAS/lease mandatory for true history rewrites;
  - document the non-forced fallback's concurrency guarantee and narrow backward-reset residual race accurately.
- `docs/development/reviewing.md`
  - describe synchronization by capability/invariant rather than assuming every reviewer environment can run `pr-reconcile.mjs`/native Update branch;
  - keep reviewer normalization purely mechanical; any conflict/semantic choice returns to implementation ownership.
- `.github/agents/pr-reviewer.agent.md`
  - mirror the canonical routing from `AGENTS.md` without duplicating low-level implementation detail unnecessarily.
- `docs/development/researching.md` and `.github/agents/researcher.agent.md`
  - change only if needed to remove wording that incorrectly implies the native endpoint is the sole permitted history-preserving mechanism; preserve evidence-identity semantics exactly.
- `.github/CONTRIBUTING.md`
  - change only if current wording would otherwise contradict the corrected routing.

Keep policy layered: `AGENTS.md` owns agent operating mechanics; review docs own review discipline; research docs own evidence semantics; `pr-reconcile.mjs` remains the native endpoint adapter.

## Tooling guidance

Do not contort `infra/dev/pr-reconcile.mjs` into pretending it can invoke connector primitives from a normal Node process. Its existing native Update branch implementation is useful and should remain the preferred local/`gh` adapter.

Change `pr-reconcile.mjs` only where necessary to keep comments/help/contracts truthful after the protocol gains a second implementation route.

If a small pure helper can encode mechanical-merge eligibility or verification invariants without duplicating connector-specific orchestration, adding it with focused tests is reasonable. Do not build a broad Git merge engine inside Arcogine merely to support this fallback.

## Tests / executable evidence

Add the narrowest executable evidence that can prove the corrected invariants. At minimum, cover the semantics of the fallback or any extracted pure planning/verification logic for:

- already-current PR => no synchronization needed;
- native Update branch remains preferred when available;
- disjoint mechanical changes => an exact merge plan can be constructed;
- overlapping/ambiguous changes => fallback refuses rather than resolving semantics;
- merge commit uses `H` as first parent and `B` as second parent;
- branch mutation is non-forced;
- concurrent forward push `H -> H2` makes `H2 -> M` non-fast-forward and must be treated as rejection/retry, never force;
- concurrent divergent/rebased head likewise fails safely;
- base advance `B -> B2` is detected by post-update freshness verification and triggers another normalization pass;
- successful fallback preserves old-head and base ancestry, produces `behind_by == 0`, keeps `ahead_by > 0`, and preserves the intended non-empty PR diff;
- research evidence ancestry requirements remain stronger and intact;
- explicit rewrite paths still require CAS/lease/atomic expected-head semantics;
- Dependabot provenance routing remains unchanged.

Where repository CI cannot literally exercise the ChatGPT connector, do not fake that claim. Test pure invariants in repository code and perform a live integration validation from a connector-capable session against a disposable/safe PR scenario before declaring the workflow operationally proven.

## Live integration acceptance

This slice exists because #329 passed unit/review validation while the intended connector-only execution path remained unusable. Therefore live integration evidence is part of acceptance, not an optional nice-to-have.

Before merge readiness:

1. use an environment exposing the same class of repository-scoped primitives as the connector-only runtime;
2. choose a disposable/test PR or another explicitly safe same-repository stale PR with mechanically disjoint changes;
3. execute the fallback end-to-end without `gh`, without native Update branch, and without `force=true`;
4. prove the PR becomes 0-behind and retains its intended diff;
5. demonstrate that a simulated or controlled concurrent forward/divergent head movement is rejected by non-fast-forward semantics or is covered by executable evidence if a live race cannot safely be staged;
6. record the exact validation evidence in the PR body.

Do not use PR #321 as a test target unless the repository owner explicitly chooses it for the validation run. Its existence demonstrates the operational gap; it should not be mutated opportunistically by an implementation experiment.

## Invariants that must not change

- A PR behind its current base is not merge-ready.
- A stale base is not itself a reviewer finding.
- Reviewer synchronization is mechanical only; conflicts or semantic choices return to implementation/author ownership.
- Ordinary freshness remains history-preserving merge-style synchronization, not rebase by default.
- Review always evaluates current `main` versus the current synchronized PR head.
- Any head change invalidates earlier exact-head authorization; CI and reviewer disposition must be re-resolved.
- The trusted `disposition` check remains the ordinary positive-review authorization gate.
- The repository owner still performs the final merge manually; agents do not merge PRs into `main`.
- Research evidence commit identity remains immutable once handed off.
- Dependabot provenance behavior remains explicitly special.
- Forced/non-fast-forward history rewrites remain protected by exact-head CAS/lease semantics.
- The connector fallback must never escalate a failed non-forced ref update into `force=true`.

## Explicit non-goals

Do not mix in:

- PR #321's research-policy content;
- issue #327's no-op contents-write follow-up;
- product/runtime changes;
- review severity/disposition vocabulary changes;
- weakening the requirement that a PR be current with `main` before final readiness;
- changing the final merge strategy into `main`;
- a generic Git merge implementation;
- arbitrary conflict resolution through low-level Git data APIs;
- a GitHub Actions/comment-command bridge merely to work around the missing endpoint when the connector already has sufficient Git primitives for the mechanical fallback;
- weakening #326's CAS rule for actual non-fast-forward rewrites.

## Validation

Use the narrowest current repository commands covering the changed surfaces. Resolve exact commands from current `main`, not from this prompt.

At minimum expect focused tests for any changed/extracted PR reconciliation logic plus the repository's required tooling/classification and Markdown validation relevant to the files touched.

Before opening the PR, search again for stale statements that:

- make native Update branch the sole legal ordinary synchronization mechanism;
- claim `force=false` fast-forward protection is identical to CAS;
- incorrectly require CAS for a non-forced history-preserving merge advance;
- accidentally permit forced ref updates for routine freshness.

## PR / lifecycle requirements

When implementation is complete:

1. remove this temporary handoff file from the branch's final tree;
2. ensure the branch's net diff contains only durable workflow/tooling changes;
3. open a narrowly scoped PR against `main` describing #329 as semantically correct but operationally incomplete;
4. include the live connector-only integration evidence in the PR body;
5. do not self-certify merge readiness;
6. follow the normal `..` lifecycle for implementation-owned transitions and the independent `./` reviewer contract;
7. never merge the PR into `main` as an agent.

## Final report checklist

Report:

- live `main` baseline used;
- files changed and why;
- final routing table for native Update branch / mechanical connector fallback / research evidence / Dependabot / explicit rewrites;
- exact mechanical-merge eligibility boundary;
- concurrency behavior, including the difference between non-forced fast-forward protection and exact-head CAS;
- tests added/changed and results;
- live connector-only integration evidence;
- any residual limitations;
- remaining follow-up only if genuinely outside this slice.