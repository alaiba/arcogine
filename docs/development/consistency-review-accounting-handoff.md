# Consistency Review Accounting Repair — Implementation Handoff

> **Temporary branch-local handoff.** This file is implementation guidance, not a new process authority. Remove it before the final PR merges unless a specific part is deliberately promoted into an existing authoritative process document.
>
> **Branch:** `fix/consistency-review-accounting`
> **Starting baseline:** `main@2b672f4f980164a1aaee2d9a926d0cae0ba0a0e9`

## Mission

Repair Arcogine's recurring Consistency-review process so a reviewed baseline cannot advance while material findings from that review remain only in conversational/session state.

The defect to close is concrete:

- the FULL Consistency review recorded on 2026-09-11 reviewed `975c1630884c7d2b8725d8b8c6f6815c876fdc30`;
- its completion comment on the `Continuous improvement register` carried only reviewed head, completion time, and mode;
- that review found material inconsistencies, but those findings were not synchronized into the durable `CONS-*` GitHub Issue ledger;
- the continuous-improvement workflow later accepted the completion comment and made `975c163...` the current weekly reviewed baseline;
- therefore the repository could advance its incremental comparison baseline while unresolved findings at that baseline had no durable identity and could disappear from future incremental review by construction.

This PR must make that state impossible under the repaired contract.

Do **not** remediate the repository inconsistencies found by the September 11 review in this PR. They are intentionally retained as real acceptance pressure for the first FULL Consistency review after this process repair lands.

## Read and re-ground first

Before editing anything:

1. Read `AGENTS.md` from the branch and obey its repository, GitHub-attribution, validation, PR-lifecycle, and review rules.
2. Resolve live `main` again. Treat the branch baseline above as historical starting context, not permission to ignore a newer `main`.
3. Read in full:
   - `.github/agents/consistency.agent.md`
   - `docs/development/consistency-review.md`
   - `docs/development/continuous-improvement.md`
   - `.github/scripts/continuous-improvement.mjs`
   - `.github/scripts/continuous-improvement.test.mjs`
   - `.github/workflows/continuous-improvement.yml`
   - `docs/development/reviewing.md` for P0/P1/P2/P3/Nit semantics
   - `.github/CONTRIBUTING.md` and `docs/development/testing.md` for validation/process requirements.
4. Inspect the live GitHub issue titled exactly `Continuous improvement register`, including its completion-evidence comments.
5. Inspect open and closed `CONS-*` issues and any `[CONSISTENCY-UNBOUND]` issues to confirm the current finding-ledger contract.
6. Quick-search `docs/` and `.github/` for at least:
   - `Consistency review completed`
   - `reviewed head`
   - `baseline persistence`
   - `finding persistence`
   - `UNPERSISTED`
   - `issue-ledger synchronization`
   - `CURRENT`, `DUE`, `OVERDUE`
   - `PR_FORWARD`
   - `diagnostic-only`.

If live `main` has moved, reconcile this branch with it using the repository-approved normal branch reconciliation path before relying on line-level assumptions.

## Required invariant

Make the following invariant true in both normative/procedural text and executable register evidence handling:

> **A recurring Consistency review may advance the durable reviewed baseline only after every unresolved finding from that review that needs lifecycle continuity has a durable disposition.**

For a normal baseline-advancing FULL or INCREMENTAL review, an unresolved finding must not remain merely `UNPERSISTED` in chat/session output. The review must synchronize the finding ledger before recording completion, then verify that the durable identities exist.

A user may still explicitly request a read-only/diagnostic Consistency run. Such a run may report `UNPERSISTED` findings, but it must **not** establish or advance the weekly recurring-review baseline.

Preserve the useful distinction in the current finding policy between durable finding continuity and unnecessary issue ceremony. However, do not let the existing immediate-remediation P3/Nit exception recreate the baseline hole. If a newly discovered finding is still unresolved on the reviewed `main` head when a recurring baseline is recorded, it needs a durable identity. A low-risk finding can avoid issue churn only when the applicable workflow leaves no unresolved finding to carry across the baseline (for example, an explicitly non-baseline diagnostic/remediation flow, or later re-verification after the fix has actually landed on `main`).

## Review-mode semantics

Do not let every Consistency mode satisfy the weekly repository-wide obligation.

At minimum:

- `FULL` — may establish/advance the weekly reviewed baseline when the accounting invariant is satisfied;
- `INCREMENTAL` — may establish/advance it when its prior baseline is valid and the accounting invariant is satisfied;
- `PR_FORWARD` — must **not** satisfy the weekly repository-wide review obligation or replace the main reviewed baseline;
- explicit diagnostic/read-only mode — must not satisfy the weekly obligation or advance the baseline.

If the current contract has no explicit syntax for diagnostic-only invocation, introduce the narrowest clear procedural distinction necessary; do not invent a parallel review framework.

## Finding-ledger authorization

The current contract requires a second explicit authorization before issue-ledger synchronization even when synchronization is necessary to complete a normal recurring review. That separation caused the continuity failure.

Change the authorization model narrowly:

- invoking a normal baseline-advancing FULL/INCREMENTAL Consistency review authorizes the **finding-ledger bookkeeping required to durably record that review's findings**;
- this authority is limited to Consistency finding identity/lifecycle synchronization required by the review;
- it does not authorize remediation, product/architecture/planning mutation, arbitrary issue edits, or merging;
- user-requested diagnostic/read-only mode remains mutation-free and therefore cannot advance the baseline.

Keep the existing collision-safe issue-number-derived `CONS-*` identity model. Do not introduce a second finding database, sequential allocator, or register-embedded detailed finding ledger.

## Completion evidence

Replace the current lossy completion evidence (`reviewed head`, `completed at`, `mode`) with a compact accounted-review format.

The exact field names may be refined during implementation, but the accepted evidence must be sufficient to distinguish all of these states:

1. a clean qualifying FULL/INCREMENTAL review with zero findings;
2. a qualifying FULL/INCREMENTAL review with one or more durably persisted findings;
3. malformed or internally inconsistent accounting evidence;
4. a legacy pre-repair completion marker;
5. a PR-forward or diagnostic-only review that must not advance the weekly baseline.

A suitable shape is along these lines:

```text
Consistency review completed
reviewed head: <40-char main SHA>
completed at: <UTC timestamp>
mode: FULL | INCREMENTAL
result: CLEAN | FINDINGS
finding issues: none | #<n>, #<n>, ...
```

Optional severity counts are useful if they remain compact and mechanically validated, but do not duplicate entire finding bodies in the register comment. GitHub Issues remain the detailed durable finding ledger.

Validation should fail closed. For example:

- `result: CLEAN` with non-empty finding issues is invalid;
- `result: FINDINGS` with no finding issues is invalid;
- duplicate issue references are invalid or normalized deterministically;
- malformed issue references are invalid;
- modes that do not qualify for the recurring baseline are ignored as baseline evidence;
- referenced finding issues should be verified as real persisted Consistency findings, not accepted purely because a trusted commenter typed `#123`.

Preserve the existing trusted-author-association and future-date defenses.

## Legacy completion evidence migration

Do not rewrite or delete historical register comments.

The old September 11 completion marker is historical evidence that a review occurred, but after this repair it must **not** count as proof of an accounted recurring baseline because it contains no durable finding accounting.

Choose a clear fail-safe migration behavior. Preferred outcome:

- old-format markers remain visible as history;
- they do not qualify as the current accounted baseline under the new contract;
- after this process repair lands, the register becomes `DUE` (or an equally explicit not-accounted state) until a new qualifying FULL review is completed under the repaired rules;
- the first repaired FULL review then establishes the new trustworthy baseline.

Do not silently grandfather `975c163...` as fully accounted merely to keep the register green.

## Register refresh

The current workflow already supports a trusted `repository_dispatch` trigger. Prefer reusing that mechanism rather than adding a new privileged event path unless implementation evidence shows a workflow change is necessary.

After a qualifying review successfully posts completion evidence, the Consistency procedure should trigger the existing register refresh mechanism when the execution environment permits it, then verify the register reflects the new state. If refresh cannot be triggered, report that operational failure explicitly; do not pretend the register is already updated.

Do not broaden `.github/workflows/continuous-improvement.yml` without a concrete need. If it must change, preserve its existing default-branch trust boundary and least-privilege model.

## Primary landing surfaces

Expect the coherent change set to center on:

- `.github/agents/consistency.agent.md`
  - normal vs diagnostic review mutation semantics;
  - finding-accounting-before-completion ordering;
  - qualifying review modes;
  - completion-evidence shape;
  - register-refresh step;
  - run-output wording as needed.

- `docs/development/consistency-review.md`
  - remove the stale claim that Arcogine has not implemented repository-owned baseline persistence;
  - document the actual baseline/finding-accounting invariant;
  - reconcile recurring review, finding persistence, remediation, and later verification;
  - keep GitHub Issues as finding identity/lifecycle continuity, not product/architecture authority.

- `docs/development/continuous-improvement.md`
  - define what `last verified` / `reviewed head` mean under the accounted-review contract;
  - document qualifying versus non-qualifying Consistency modes;
  - document legacy evidence behavior and refresh semantics without duplicating the entire Consistency procedure.

- `.github/scripts/continuous-improvement.mjs`
  - parse and validate new completion evidence;
  - reject/ignore legacy evidence as an accounted baseline under the new contract;
  - validate referenced finding identities against GitHub issue evidence;
  - render the managed register state needed to make the obligation operationally clear.

- `.github/scripts/continuous-improvement.test.mjs`
  - executable regression coverage for the continuity failure and migration behavior.

Change `.github/workflows/continuous-improvement.yml` only if required by the chosen implementation. Change `AGENTS.md` only if a genuinely repository-wide agent rule is missing; do not duplicate specialized Consistency procedure there.

## Required executable acceptance cases

Add deterministic tests that prove at least the following:

### Qualifying completion evidence

- FULL + CLEAN + zero finding issues qualifies;
- INCREMENTAL + CLEAN + zero finding issues qualifies;
- FULL/INCREMENTAL + FINDINGS + one or more valid persisted Consistency issue identities qualifies;
- latest qualifying accounted completion is selected correctly.

### Finding accounting failures

- FINDINGS with no issue identities does not advance the baseline;
- CLEAN with issue identities is rejected as inconsistent;
- malformed/duplicate/bogus issue references do not silently qualify;
- a trusted commenter cannot establish a baseline by citing non-Consistency issues as finding accounting;
- unresolved findings cannot be represented only as conversational `UNPERSISTED` output and still produce qualifying recurring completion evidence.

### Mode boundaries

- PR_FORWARD completion cannot satisfy or refresh the weekly repository-wide obligation;
- diagnostic/read-only review cannot satisfy or refresh it;
- a malformed or unknown mode fails closed.

### Legacy migration

- the exact old-format style currently present on issue #295 does not qualify as an accounted baseline after the new contract lands;
- historical old-format evidence does not prevent the obligation from becoming DUE when no new qualifying completion exists;
- a later new-format qualifying completion supersedes legacy evidence normally.

### Existing security/integrity behavior

Preserve tests for:

- trusted GitHub author association;
- rejection of future-dated completion claims;
- exact register-title discovery and ambiguity failure;
- marker-region integrity and preservation of the human-managed intervention region;
- idempotent updates;
- retrospective `CHECK_TRIGGER` semantics.

If GitHub-I/O-dependent validation is separated from pure parsing, keep the pure functions deterministic and testable with fixtures rather than introducing network calls into unit tests.

## Acceptance behavior after merge

The process PR itself is not the final proof.

After it lands on `main`, the intended acceptance exercise is:

1. the old `975c163...` marker no longer counts as an accounted baseline;
2. the weekly Consistency obligation becomes DUE/not-accounted;
3. run a fresh **FULL** Consistency review of then-current `main` without seeding it from the known September findings;
4. independently detect whatever inconsistencies actually remain;
5. persist every unresolved finding requiring continuity through the repaired `CONS-*` issue-ledger path;
6. only then post qualifying completion evidence;
7. refresh and verify the register;
8. use later remediation and a subsequent Consistency run to prove finding verification/closure lifecycle.

The known September findings are an external acceptance oracle for this exercise, not implementation input. Do not encode them into the scanner, helper, tests, or process docs.

## Explicit non-goals

Do **not** in this PR:

- remediate `docs/reference/api.md`, KPI/product-concepts drift, Challenge Javadocs, the stale Netty version prose, or standards-alignment wording;
- manually create the five known September finding issues merely to patch the current ledger;
- change product/runtime behavior;
- change architecture or Accepted ADR semantics;
- redesign the general PR review process;
- run or complete the delivery-process retrospective;
- invent a second Consistency findings store in the register;
- auto-remediate findings;
- auto-merge any PR;
- turn the continuous-improvement workflow into an LLM/agent executor.

## Validation

At minimum run the narrow process/tooling checks required by the final diff, including:

```text
node --test .github/scripts/continuous-improvement.test.mjs
bash .github/scripts/classify-changes.test.sh
python3 .github/scripts/check-markdown-links.py .
python3 .github/scripts/check-delivery-labels.py
bash .github/scripts/check-actions-workflows.sh   # if workflow YAML changes
```

Also run `git diff --check` or the repository-equivalent whitespace validation available in the environment.

Do not claim success from unit tests alone. Before opening the PR, inspect the final text across the Consistency agent, both development docs, helper, tests, and any workflow change for one coherent authority story.

## PR and review requirements

- Keep this branch scoped to the process repair above.
- Remove this temporary handoff file before the final PR merges.
- Reconcile with live `main` before substantive independent review if the branch falls behind, using the normal repository-approved conflict-free reconciliation path.
- Open a PR with a precise description of the continuity failure, the new invariant, migration semantics for old completion evidence, test evidence, and explicit non-goals.
- Do not self-certify merge readiness. Request normal independent review using `.github/agents/pr-reviewer.agent.md`.
- The implementation/reviewer agents do not merge; stop at the repository's `READY TO MERGE` boundary.

## Completion report

When implementation is ready for independent review, report:

- current branch head and live `main` SHA;
- files changed;
- exact completion-evidence schema adopted;
- how FULL/INCREMENTAL, PR_FORWARD, and diagnostic-only modes differ;
- how required finding persistence is authorized and verified;
- how legacy `975c163...` evidence is treated;
- whether the workflow file changed and why;
- deterministic test cases added for the original continuity failure;
- validation commands/results;
- confirmation that none of the known repository findings was remediated or manually persisted as part of this process PR;
- confirmation that this temporary handoff file is removed from the final merge candidate.