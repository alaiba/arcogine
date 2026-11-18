# Start-of-review grounding

Part of the [PR Reviewer contract](../pr-reviewer.md#review-procedure); every complete review, re-review and `./` continuation follows all of its routed topics.

At the beginning of every complete review or re-review:

1. Resolve current `main` and record its SHA.
2. Resolve the PR number, title, current base, current head SHA, mergeability, and live base distance where available.
3. **Normalize a stale base before substantive review.**
   - For an open same-repository PR that is behind its live base, apply the base-normalization protocol defined in `docs/development/reviewing.md`: capture `H` (the PR head), `B` (the live base), and `A` (the merge base when needed). Construct exactly one merge commit `M` with first parent `H`, second parent `B`, and a tree formed from the base tree plus the PR-side delta, using ordinary three-way text merges only for supported overlapping text files.
   - Use repository-scoped GitHub Git-data operations. Immediately before publication, re-read the PR head; if it is no longer `H`, abandon without mutation. Otherwise publish `H -> M` with a non-forced ref update (`force=false`). This is a best-effort check, not exact-head atomicity: a reset to an ancestor such as `B` in the tiny post-check interval can still fast-forward to `M`, and that residual race is accepted. Do not use a local `gh` prerequisite, rebase, force push, lease, or separate compare-and-swap protocol.
   - If construction encounters a real conflict or unsupported structural case, make no remote branch mutation and return the PR to its author/implementation owner. Do not resolve conflicts or file a reconciliation finding against the stale head merely to request synchronization.
   - For a stale Dependabot PR that otherwise qualifies for trusted provenance, preserve that provenance during normalization: a maintainer-authored synchronization commit changes the PR's provenance, and the resulting head follows the ordinary review path instead.
4. After successful normalization, resolve the resulting current head and review that candidate. Do not ask the reviewer to orchestrate CI, trusted `disposition`, or final mergeability; those remain independent repository gates. Do not immediately synchronize again solely because `main` advanced after `B` was observed; a later continuation may do so if required.
5. Inspect the PR description, changed files, and net `current main...current normalized PR head` diff.
6. Inspect existing reviews, comments, unresolved threads, and prior findings when available. Treat any active native GitHub `CHANGES_REQUESTED` state as an anomalous platform blocker that must be cleared before merge; Arcogine reviewers do not create it.
7. Record any already-visible CI or gate state separately when useful, but do not make reviewer disposition depend on orchestrating or re-resolving those lifecycle gates.
8. Read `AGENTS.md` and `docs/development/reviewing.md`.
9. Read the relevant current architecture and specifications, the implemented handoff or issue, code, tests, reference docs, and prerequisite/recent PRs indicated by the change.
10. Record any required surface that could not be inspected.

Never assume the head reviewed previously is still current. Never review only the commit list when the net proposed state is available.

If a required repository or PR surface cannot be inspected, state the limitation and do not imply a complete review.
