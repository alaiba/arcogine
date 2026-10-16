# Coding-agent handoff: unify research delivery with the retrospective window

## Assignment

Implement one focused process/tooling correction for Arcogine: the **research-delivery analysis inside the formal delivery-process retrospective must use the same exact baseline-to-through-PR merge-time window as the retrospective's canonical PR/review analysis**, rather than a second window measured from a Continuous Improvement checkpoint's opening date.

This is **one retrospective**, not a new practice. Keep its single invocation, dated Git report with a `Research delivery` section, versioned baseline, follow-up issue ownership, and associated checkpoint result publication. The checkpoint is for coordination/publication only; its issue creation time is **not** an analytical cutoff for either retrospective evidence source.

Implement the change in a coherent reviewed PR. Do **not** execute the retrospective or advance its actual baseline during this implementation task. Do **not** delete any Git branches.

## Repository grounding and authorities

- Repository: `alaiba/arcogine`. Start with the exact live `main` and follow `AGENTS.md`, `.github/CONTRIBUTING.md`, and the project snapshot/provenance protocol when present. Search the relevant docs before editing; treat current `main`, not this handoff branch, as the implementation baseline.
- This prompt was composed against `main` at `8b64b71b58558c3ffe9b097ea0000483c1e9cce1`. Recheck live `main` at execution time; this SHA is context, **not** a prescribed implementation target.
- Canonical retrospective method: `docs/development/continuous-improvement.md`, especially **Mechanical evidence first**, **Research delivery and temporary-evidence value**, **Dated report**, and **Retrospective checkpoint publication**.
- Existing branch discovery helper and tests: `infra/dev/delivery-retrospective-branches.mjs` and `infra/dev/delivery-retrospective-branches.test.mjs`.
- Existing authoritative PR/review acquisition and window: `infra/dev/delivery-retrospective-github.mjs`, `infra/dev/delivery-retrospective-evidence.mjs`, and `infra/dev/delivery-retrospective.mjs`. The canonical window is **(baseline PR mergedAt, through PR mergedAt]**, using timestamps rather than PR-number ranges.
- Versioned baseline: `.github/continuous-improvement/retrospective.json`. This is retrospective state; do not replace it with a checkpoint-based baseline.
- Test runner and guidance: `.github/scripts/check-repository-tooling.sh` and `docs/development/testing.md`.
- Research evidence custody and transient merge-tree rules remain governed by `docs/development/researching.md` and `AGENTS.md`.

**Important handoff custody:** this `workspace/implementation/` file is branch-local execution scaffolding. Build the implementation PR from fresh/current `main`, not by merging this handoff branch. No tracked `workspace/` path may appear in the PR's final merge tree. Do not delete this branch without a separate explicit owner request.

## Required outcome

### 1. One exact analytical window

The formal retrospective chooses one merged main-target `throughPr`; the existing source-neutral Retrospective Evidence v1 acquisition resolves it and the persisted baseline PR, including their verified merge timestamps. **Reuse those exact two timestamps for research evidence relevance**:

- Lower bound: baseline PR merge timestamp, **exclusive**.
- Upper bound: selected through-PR merge timestamp, **inclusive**.
- This bound is shared between PR/review metrics and qualitative research delivery.
- The checkpoint number may still identify the issue on which the retrospective publishes its result; **do not obtain the branch-analysis time bounds from a checkpoint issue or its `created_at`**.
- No separate research retrospective, second Git report, second checkpoint publication, special schedule, new baseline state, or due-state register.

The source-neutral Evidence v1 bundle already exposes `repository`, `requested.baselinePr`, `requested.throughPr`, `endpoints.baseline.mergedAt`, and `endpoints.through.mergedAt`. Prefer consuming that **validated local bundle** rather than independently re-fetching or reconstructing the two endpoints or adding a separate checkpoint-based selector.

### 2. Rework branch inventory input and boundaries

Refactor `infra/dev/delivery-retrospective-branches.mjs` and its public CLI to use the verified retrospective window. Recommended call shape:

```bash
node infra/dev/delivery-retrospective-github.mjs \
  --through-pr <merged-pr-number> \
  --output logs/retrospective-evidence.json

node infra/dev/delivery-retrospective-branches.mjs \
  --evidence logs/retrospective-evidence.json \
  --output logs/retrospective-branches.json
```

- Import/use the existing `validateEvidence` contract (or an equivalently authoritative existing validation path) and check repository identity, requested PR identities, valid ordered merge times, and the expected baseline against versioned retrospective state as applicable. Fail closed on missing, contradictory, invalid, or incomplete input; no best-effort date guessing.
- Replace the current required `--checkpoint` / `checkpointNumber` / `checkpoint.created_at` time-bound interface and checkpoint-derived inventory metadata with a clearly named **retrospective window** representation: baseline/through PR identities and exact exclusive/inclusive UTC merge timestamps. Preserve an independent `scannedAt` inventory-observation timestamp, but **do not** treat scanning time as the retrospective analysis cutoff.
- Enumerate all currently live same-repository refs for the eventual **chat-only complete branch table**, including branches with no PR and those older or newer than the analytical window.
- Also discover historical same-repository PR-head branch identities whose **PR lifecycle overlaps the retrospective interval** (including an open or merged PR created before the baseline and closed or still active after it). The overlap rule must respect the exclusive lower and inclusive upper cutoff. Fetch complete paginated all-state PR metadata. Keep fork exclusion, unknown source attribution, missing/deleted-ref handling, duplicate/cursor protection, reused-ref warning, and fail-closed pagination behavior.
- Do not infer the historical time a branch was first opened from its PR's creation timestamp. A PR-lifecycle overlap makes its branch identity a *candidate for inspection*, not proof that each research artifact or commit is inside the window. A branch deleted without a PR may remain undiscoverable; disclose this rather than inventing a complete history.
- Preserve traceability to PR numbers, recorded head SHAs, current ref/tip, and any source ambiguity. Inventory format may evolve, but keep it local/transient under `logs/`, not a new repository ledger or retro baseline.

### 3. Research relevance and historical context

For every branch in the complete inventory, classify research-related / non-research / uncertain with available PR, commit, and repository evidence. **Every identified research-related branch remains eligible as research-delivery evidence**, including a surviving no-PR research branch; select a bounded variety of consequential questions only for deeper qualitative reconstruction.

Apply the **shared retrospective interval** to what is attributed to the report's measured period:

- Count or draw period-specific research process conclusions only from work/delivery materially relevant to that exact interval. Relevant in-window delivery can be established by a PR merged inside the interval; in-flight investigation can be established by verifiable evidence occurring inside the interval even without a merged PR.
- Earlier research report revisions, experiments, and adversarial reviews may be read from reachable branch history when necessary to explain *how* a decision delivered during the interval was reached. Label their earlier timestamps explicitly as **context**, not newly completed work in the window.
- Later commits, PR outcomes, or events after the through-PR merge timestamp must likewise be labeled as **subsequent context**, never silently included as period outcomes.
- Do not use current `HEAD`'s clean `workspace/` tree as a claim that reports were never persisted: read prior commits at their exact SHAs and paths when available. Do not treat drafts or rejected hypotheses as settled research conclusions.
- Missing or unverifiable research history yields a documented qualitative coverage limit; it does not alter the mechanically established PR/review denominator. Avoid fabricated branch-creation dates, canonical research rates, new scoring, or selection bias hidden as a complete sample.

### 4. Preserve one report, one checkpoint result, chat-only cleanup advice

The existing formal retrospective has **one** dated report under `docs/history/continuous-improvement/`, including a concise `## Research delivery` section in the *same* document as PR/review mechanical metrics, prior-change verdicts, and owned actions. It advances only the existing versioned baseline when the reviewed report PR actually merges. A single associated Continuous Improvement checkpoint carries the normal retrospective proposed/merged result comments linking this Git report; do not post a separate research-only result.

The agent's final **chat response only** must still list **every scanned branch**, including non-research branches and out-of-window branches, with PR association, research classification, whether evidence was relevant/inspected to the shared window, and grounded `DELETE CANDIDATE` / `KEEP` / `VERIFY FIRST` / `ALREADY ABSENT` recommendation. Mark out-of-window entries as inventory/cleanup context, not retrospective evidence. Do not add the complete branch list to the dated Git report, checkpoint comment, or maintained registry.

**Never delete a branch as part of this work or a retrospective.** Branch deletion requires a separate explicit owner request under `AGENTS.md`. Do not touch the research procedure's evidence persistence / knowledge-transfer contract, which already preserves prior commits behind a retained branch ref despite the final merge tree containing no tracked `workspace/` files.

## Tests, validation, and acceptance criteria

Update the branch-helper tests and any impacted documentation/tests to cover at least:

1. Evidence v1 baseline and through merge timestamps become the branch window (exclusive start, inclusive end), and the former checkpoint timestamp is not referenced as an analysis cutoff.
2. A historical same-repository PR head created **before** the baseline but still overlapping the interval is included; PR history ending exactly at the excluded lower boundary is excluded, and PRs first created **after** the inclusive upper boundary are not historical in-window candidates.
3. All live branches (default/protected, research, non-research, no-PR, out-of-window) remain in the inventory for chat-only disposition, without manufacturing in-window relevance.
4. Deleted PR heads are represented where discoverable; fork PRs are excluded; missing head origin is reported rather than assumed; reused names retain exact-commit provenance.
5. Invalid/incomplete evidence, mismatched repository/PR endpoints, missing merge times, malformed PR facts, incomplete/duplicated pagination and similar uncertainty fail closed.
6. Actual research histories may span the interval: older context and post-through context are allowed for interpretation but not misrepresented as in-window results.
7. The published procedure shows the correct commands and explicitly retains one report, one checkpoint publication, and chat-only branch deletion candidates.
8. No API operation deletes or rewrites refs, and no new due-state, research lifecycle, or independent retrospective process is introduced.

Run the narrow tests, then the repository tooling suite (`node --test infra/dev/delivery-retrospective-branches.test.mjs` and `bash .github/scripts/check-repository-tooling.sh`), plus any more specific checks justified by the changed surface. Retain the existing Evidence v1 producer, pure analyzer, and retrospective baseline schema unless a concrete test proves a necessary minimal change.

Deliver a narrowly scoped PR following the repository's contributor/review contract: clear goal/non-goals, exact-head CI convergence, no tracked `workspace/` files, and independent-review handoff. **Do not merge the PR**; the owner does that manually. Do not run the retrospective, create a dated retrospective report, advance `retrospective.json`, post retrospective checkpoint outcome comments, change issue #486 (research-to-delivery lead-time metrics), or perform branch cleanup. If a required proof cannot be established, surface the precise blocker rather than approximating.

## Completion response

Report the implementation PR URL, branch, exact head SHA, files changed, tests/CI results, and any limitations or open reviewer gate. Do not claim the retrospective itself was executed.
