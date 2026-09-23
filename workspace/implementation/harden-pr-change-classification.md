# Implementation handoff: harden PR change classification against redundant network fetches

## Repository and branch rules

Work in `alaiba/arcogine` on this implementation branch. Read and follow `AGENTS.md` before making changes, including its artifact-lifetime, validation, PR-lifecycle, attribution, and branch rules. This prompt is transient delivery scaffolding under `workspace/implementation/`; remove it before independent PR review and merge readiness, then run the tracked-workspace check.

Read `.github/CONTRIBUTING.md` and `docs/development/testing.md` for the current workflow/tooling validation contract. Do not weaken or bypass repository protections to make CI green.

## Read first

Read these files from the current branch before editing:

- `AGENTS.md`
- `.github/CONTRIBUTING.md`
- `docs/development/testing.md`
- `.github/workflows/ci.yml`
- `.github/scripts/classify-changes.sh`
- `.github/scripts/classify-changes.test.sh`
- `.github/scripts/check-actions-workflows.sh`

Quickly search the repository/docs for:

- `Determine changed-file surfaces`
- `fetch-depth`
- `pull_request.base.sha`
- `classify-changes`
- `git diff --name-only`
- `Change-aware execution`
- `fail-safe default`

## Grounded baseline

This handoff was prepared from live `main` at commit `2e13eb5fcf71823740453b1c80d0e792fa38a746`.

At that baseline, the `Classify changes` job in `.github/workflows/ci.yml`:

1. checks out the repository with pinned `actions/checkout` and `fetch-depth: 0`;
2. runs the always-required repository-tooling suites;
3. for a `pull_request` event, performs an additional network operation:

   ```bash
   base="origin/${{ github.event.pull_request.base.ref }}"
   git fetch origin "${{ github.event.pull_request.base.ref }}" --depth=1
   files=$(git diff --name-only "$base"...HEAD)
   ```

The pinned checkout contract says `fetch-depth: 0` fetches all history for all branches and tags. On PR #388, checkout successfully fetched `origin/main`, the PR branch, and the synthetic PR merge ref. The later explicit `git fetch origin main --depth=1` then failed with a hosted-runner TLS certificate-verification error, causing the classifier to fail and the Java jobs to be skipped.

PR #388 is evidence for the failure mode, not an implementation dependency. This CI-hardening slice is independent of #388 and must not modify or continue that PR.

## Objective

Remove the redundant post-checkout network dependency from pull-request changed-file discovery while preserving Arcogine's conservative change-classification semantics.

For `pull_request` events, classify the candidate against the immutable PR-event base commit rather than refreshing a mutable base branch from the network during the classifier step.

The intended coordinate is:

```text
github.event.pull_request.base.sha
```

## Required behavior

For `pull_request` events:

1. Do not run a second `git fetch` in the changed-file-classification step.
2. Use the immutable `github.event.pull_request.base.sha` supplied by the triggering event as the comparison baseline.
3. Verify that the base commit object exists locally before attempting the diff.
4. When it exists, compute changed files locally with three-dot diff semantics equivalent to:

   ```bash
   git diff --name-only "$PR_BASE_SHA"...HEAD
   ```

5. If the event base commit is unexpectedly unavailable locally, fail conservatively for test selection:
   - emit a useful GitHub Actions warning;
   - feed `git ls-files` into the classifier so executable validation is over-selected rather than under-selected;
   - do not perform a fallback network fetch.

Preserve current `push`, `schedule`, and `workflow_dispatch` semantics unless a change is strictly required for correctness.

Prefer passing the event SHA through step `env:` rather than interpolating it directly into shell where practical.

## Invariants

The change must preserve all of these:

- The classifier is an optimization for selecting validation, never a mechanism that may silently under-run required validation.
- Missing or unusable PR comparison state must bias toward more validation, not fewer jobs.
- PR classification is bound to the immutable candidate event it is validating; it must not chase a later `main` tip during the job.
- Base freshness remains a separate repository lifecycle/merge-readiness concern.
- Workflow syntax, action pinning, aggregate `gate`, unconditional secret scanning, the disposition mechanism, and existing trust boundaries remain unchanged unless the narrow fix truly requires otherwise.
- Do not add `sslVerify=false`, TLS retries that mask certificate failures, custom CA workarounds, or any other workaround that weakens transport verification.
- Do not add a generalized CI abstraction merely because changed-file plumbing can be extracted. Reuse the existing split: Git/GitHub context builds a changed-file list; `classify-changes.sh` classifies that list.
- A candidate-controlled value must not be able to cause backend checks to be skipped incorrectly.

## Test and evidence expectations

Do not stop at actionlint syntax validation. Add the narrowest executable regression coverage that proves the changed-file discovery behavior itself.

The tests should prove at least:

1. with a locally available PR base SHA, the intended changed-file list is produced without network access;
2. with an unavailable PR base SHA, classification falls back conservatively rather than under-classifying;
3. existing push/full-sweep behavior remains correct or demonstrably unaffected.

Prefer deterministic tests built from temporary local Git repositories; do not make regression tests depend on live GitHub networking.

Keep `.github/scripts/classify-changes.sh` focused on classifying paths. If directly testing the workflow's inline shell would otherwise require brittle text inspection, a very small repository-owned helper for **changed-file discovery only** is acceptable when it improves executable coverage and remains simpler than duplicating workflow semantics. Do not turn it into a generalized GitHub Actions framework.

Update `docs/development/testing.md` only if the maintained description of the classifier architecture or local test commands changes.

## Scope and non-goals

In scope:

- PR changed-file discovery in the CI classifier;
- narrow deterministic regression coverage for that discovery;
- directly necessary workflow/tooling documentation reconciliation.

Out of scope:

- fixing GitHub-hosted runner TLS/CA behavior;
- changes to product Java code;
- changes to simulation semantics or PR #388;
- reworking unrelated CI jobs;
- changing base-freshness policy;
- changing `disposition`, CODEOWNERS, branch-protection/ruleset semantics, or security scanning;
- retry frameworks, network abstraction layers, or generalized workflow plumbing.

## Validation

Run the narrowest repository-owned validation that exercises the final implementation.

At minimum, after implementing the change, run the affected repository-tooling tests and workflow validation, including:

```bash
bash .github/scripts/classify-changes.test.sh
bash .github/scripts/check-pr-disposition.test.sh
bash .github/scripts/check-actions-workflows.sh
```

Run any new/changed helper test directly as well.

Because a `.github/workflows/` change is deliberately classified as touching every executable subsystem, run the repository-wide Java gate if the environment can support it:

```bash
./arcogine check
```

If a required local validation cannot run because of an actual environment capability limitation, report that precisely; do not replace it with an invented substitute.

Before independent review:

1. remove this prompt from `workspace/implementation/`;
2. inspect the branch for other transient artifacts;
3. run:

   ```bash
   python3 .github/scripts/check-transient-workspace.py
   ```

The final PR candidate must contain no tracked `workspace/` paths.

## PR creation and completion

Keep the implementation as one small, independently reviewable CI/tooling PR from current `main`. If `main` advances before PR creation or review, follow the repository's normal base-freshness/lifecycle rules; do not conflate that with the classifier's immutable event-base behavior.

The PR description should state stable semantic scope, rationale, non-goals, and validation actually performed. It may identify PR #388 as historical evidence for the redundant-fetch failure mode, but do not encode mutable CI/head/base state as prose that must stay synchronized.

Do not claim that this change fixes GitHub-hosted runner TLS behavior. It removes an unnecessary second network operation from classification and makes PR change discovery deterministic against the triggering event's immutable base.

After creating or updating the implementation PR, report:

- PR number;
- exact current head SHA;
- concise summary of the implemented classifier behavior;
- validation actually performed;
- any genuine remaining blocker;
- current lifecycle transition/state under `AGENTS.md`.

Then stop. Do not merge the PR.
