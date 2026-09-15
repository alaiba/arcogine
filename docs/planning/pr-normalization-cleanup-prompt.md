# Implementation prompt — simplify stale-PR normalization

Implement a focused cleanup of Arcogine's stale-PR normalization model. The current implementation accumulated complexity across several iterations (#333–#335) and now makes a routine mechanical synchronization both slow and, in connector-driven review, frequently impossible to execute. Replace that model with a connector-friendly, history-preserving merge-style synchronization and remove the rebase-specific scaffolding that is no longer justified.

## Design decision

For an ordinary same-repository PR that is behind its current base, **merge the captured live base into the captured PR head mechanically**. Do not rebase the PR branch.

For one normalization attempt capture:

- `H` — current PR head commit;
- `B` — current live base commit;
- `A` — merge base of `H` and `B`, only as needed to determine the two deltas.

Construct exactly one merge commit `M`:

- first parent: `H`;
- second parent: `B`;
- tree: current base tree plus the PR-side delta, with ordinary three-way text merge used only where both sides modify the same supported text file and Git can resolve it mechanically.

Then advance the PR branch `H -> M` with a **non-forced fast-forward ref update (`force=false`)**.

This is the core concurrency guard. Because `M` has `H` as its first parent, a concurrent incompatible branch update makes the ref update non-fast-forward and GitHub rejects it. Do not add a separate exact-head force/CAS/lease protocol around ordinary stale-PR synchronization.

If `main` moves after `B` was captured, do not chase it in the same attempt. The next lifecycle iteration may synchronize again if the PR is still behind.

## Mechanical boundary

Normalization is preparation, not implementation.

The normal path must be:

1. resolve `H`, `B`, and whether the PR is behind;
2. if already current, do nothing;
3. mechanically construct `M`;
4. publish `H -> M` with `force=false`;
5. if construction encounters a real merge conflict or an unsupported case requiring semantic choice, make **no remote mutation** and return the PR to the implementation/author side.

Do not manually resolve conflicts during review.

Keep the supported merge surface deliberately small and practical. Do **not** recreate the previous proof-heavy miniature merge engine.

A suitable connector-oriented implementation can use GitHub Git-data primitives directly:

- obtain the exact `H`, `B`, and merge-base commits/trees;
- start tree construction from `B`'s tree (`base_tree`);
- replay only the PR-side `A -> H` changes;
- use exact source blobs/modes for supported add/modify/delete operations;
- where `A -> H` and `A -> B` both modify the same ordinary text file, use an ordinary deterministic three-way text merge over the exact `A`/`B`/`H` contents;
- if that text merge conflicts, stop;
- reject structural cases that require interpretation rather than adding more policy machinery (for example ambiguous rename/copy, file/directory shape conflicts, submodules, symlinks, incompatible mode changes, binary/content cases that cannot be handled mechanically);
- create `M` with parents `[H, B]`;
- move the PR branch to `M` with `force=false`.

Do not require complete recursive-tree reconstruction when GitHub's `create_tree(base_tree=...)` plus the exact changed entries is sufficient. Do not add `.gitattributes`/custom merge-driver emulation unless the repository actually uses a load-bearing configuration that makes it necessary; prefer rejecting a genuinely unsupported case over rebuilding Git's merge engine.

## Reviewer responsibility

Correct the role boundary introduced by the recent iterations.

The reviewer may perform this **mechanical stale-base normalization before substantive review**. After successful normalization, the reviewer needs only to resolve the resulting current head and review that candidate.

The reviewer is **not** responsible for globally re-resolving CI, trusted disposition, or final mergeability after normalization. Those are lifecycle / repository-gate responsibilities. `pr-watch`, GitHub required checks, and the disposition workflow own merge readiness.

A reviewer must still never review an obsolete head knowingly, and a new head requires review judgment for that new head. But do not turn the reviewer into the overall PR lifecycle orchestrator.

## Research evidence custody

Remove research-evidence custody from ordinary PR-normalization logic and reviewer instructions.

Research evidence custody belongs to the research workspace lifecycle in `docs/development/researching.md` and `.github/agents/researcher.agent.md`: exact handed-off report/review SHA+path coordinates must remain reachable while that temporary workspace is serving as evidence custody.

That concern does **not** alter generic PR synchronization or ordinary PR review. Once a reconciliation PR is the candidate being reviewed for `main`, stale-base normalization is the same mechanical merge-style operation as for any other ordinary PR.

Do not delete or weaken the research workspace's own evidence-custody rules; simply stop importing them into generic PR review/normalization.

## Cleanup required

Inventory current references before editing, then remove the rebase-specific implementation and duplicated policy that are no longer needed.

At minimum evaluate and update these surfaces:

- `infra/dev/pr-reconcile.mjs` — delete the temporary-repository / `gh` / token / rebase / `--force-with-lease` implementation;
- `infra/dev/pr-reconcile.test.mjs` — delete with the implementation;
- `.github/workflows/ci.yml` — remove the obsolete `pr-reconcile` test step;
- `docs/development/testing.md` — remove obsolete rebase-helper test documentation and document only any replacement test surface that remains useful;
- `AGENTS.md` — replace the current route table and lease/rebase protocol with the short merge-style protocol above;
- `.github/agents/pr-reviewer.agent.md` — remove helper/lease/research-custody/lifecycle-orchestration details and keep the reviewer boundary concise;
- `docs/development/reviewing.md` — align with the same concise reviewer rule;
- `infra/dev/git-identity.sh` and `infra/dev/git-identity.test.sh` — remove `arcogine.owner.name` / `arcogine.owner.email` persistence and matching if their only remaining purpose is the deleted guarded-rebase helper; preserve the ordinary rule that actual commits use the human repository owner's Git identity and preserve useful bot/agent identity warnings;
- `.github/scripts/continuous-improvement.mjs` — update any intervention/evidence row that names `pr-reconcile.mjs` as part of the active lifecycle implementation;
- search the repository for `pr-reconcile`, `force-with-lease`, `arcogine.owner`, `base-normalization`, and stale references to the #333/#334/#335 implementation model and reconcile them deliberately.

Do **not** modify the disposition authorization design unless required to remove a false dependency on the deleted helper. `check-pr-disposition.sh` / `pr-disposition.yml` are a separate concern and should remain the trusted review-authorization gate.

Do **not** remove base freshness from `pr-watch`; a behind PR remains a lifecycle state that needs normalization before final merge readiness. Keep lifecycle classification separate from the normalization mechanism.

## Replacement implementation guidance

Prefer the smallest implementation that makes connector-driven synchronization fast.

Do not restore the previous `pr-merge-plan.mjs` architecture wholesale. If a pure helper is useful, it must be substantially smaller and should only encode the minimal mechanical merge-commit plan needed by connector primitives. It must not require agents to prove complete repository-tree equivalence, model arbitrary Git merge drivers, or perform broad post-update lifecycle orchestration.

The common successful path should require only the minimum GitHub reads needed to determine `A/H/B` changes, the minimum blob reads needed for overlapping text files, one tree creation, one merge-commit creation, and one `force=false` ref update.

Optimize for the actual operating environment: repository-scoped GitHub connector operations are available; an authenticated local `gh` CLI must **not** be a prerequisite for reviewer-side normalization.

## Required invariants

Preserve these and no more than necessary:

1. Never mutate `main`; the repository owner still performs the final manual Squash and merge.
2. Never resolve semantic merge conflicts as reviewer.
3. Never use `force=true` for ordinary stale-PR normalization.
4. The synchronization merge commit has first parent `H` and second parent captured base `B`.
5. A failed/unsupported merge attempt causes no remote branch mutation.
6. A concurrent incompatible PR-head update is rejected naturally by the non-forced ref update.
7. `main` moving after captured `B` does not invalidate the completed attempt; later lifecycle iterations handle new staleness.
8. A successful normalization creates a new head that must be reviewed as the current candidate, but CI/disposition/final mergeability remain lifecycle/gate responsibilities rather than reviewer-owned orchestration.
9. Research evidence custody remains a research-workspace concern, not a generic PR normalization concern.
10. Keep the final system materially simpler than the current #335 implementation and materially simpler than the deleted #333/#334 synthetic merge machinery.

## Validation

Add only focused tests that protect the simplified contract. At minimum cover:

- already-current no-op;
- disjoint PR/base changes produce one `[H, B]` merge commit plan;
- clean same-file text merge succeeds;
- real text conflict stops with no ref mutation;
- unsupported structural case stops with no ref mutation;
- branch publication uses `force=false`;
- a simulated concurrent branch movement causes publication failure rather than overwrite;
- base movement after captured `B` does not cause same-attempt retry/chasing;
- `pr-watch` still reports a behind head as needing a pre-merge transition;
- reviewer documentation/contract no longer assigns CI, disposition, or final mergeability orchestration to the reviewer;
- research-workspace evidence rules remain intact in the research lifecycle while disappearing from ordinary PR-normalization instructions.

Run the narrowest relevant repository checks plus `git diff --check`. Keep the change focused on lifecycle/normalization cleanup; do not opportunistically redesign unrelated review or CI behavior.

## Expected outcome

After this change, the routine instruction “bring this PR up to date” should again be a short mechanical operation through the GitHub connector:

- merge captured `main` into the captured PR head mechanically;
- fast-forward the PR ref with `force=false`;
- stop on genuine conflict;
- move on.

No local `gh`, no forced branch rewrite, no lease protocol, no research-evidence detour, no reviewer-owned CI/disposition orchestration, and no miniature general-purpose Git merge engine.