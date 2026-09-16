# Consistency review operations

> **Status:** maintainer guidance around the review contract in [`.github/agents/consistency.agent.md`](../../.github/agents/consistency.agent.md).

Arcogine's formal Consistency review runs in a ChatGPT chat session using the GitHub connector. It is not required to run in a local checkout or remain compatible with local coding-agent runtimes. Repository reads and finding/register writes therefore happen through GitHub; the review does not depend on `git`, `gh`, shell commands, or locally executing repository scripts.

## Operating loop

```text
read live main + register #295 + open CONS findings
        |
        v
choose scope automatically
  no baseline -> FULL
     baseline -> INCREMENTAL
        |
        v
inspect semantic changes + repository-search neighbors
        |
        v
reconcile open findings
        |
        +--> candidate new/regression finding
        |       -> search relevant closed CONS findings on demand
        |
        v
create/reopen/close finding issues as required
        |
        v
update the weekly Consistency section of #295
        |
        v
done
```

The reviewer does not run a second PR-review mode. Open PRs are inspected only when they explain history or show that an existing finding is plausibly `IN_FLIGHT`; independent PR acceptance remains owned by the PR Reviewer.

Ad-hoc questions such as “is this architecture claim consistent with the implementation?” are ordinary read-only chat analysis. They do not need a formal diagnostic mode and do not advance the weekly baseline.

## Grounding and scope

Every formal review re-reads current `main`, `AGENTS.md`, the live consistency contract, register issue #295, and the currently open `CONS:` findings. Uploaded snapshots and conversation memory are not current repository authority.

Closed findings are **not** preloaded. When a new candidate finding appears, search closed `CONS:` issues using its semantic subject/terminology/evidence to determine whether it is a regression or duplicate. This makes history lookup proportional to actual candidates rather than to the lifetime size of the ledger.

The reviewer selects scope from the register rather than asking the user:

- no accounted reviewed head -> `FULL`;
- accounted reviewed head -> `INCREMENTAL` from that head to current `main`;
- unusable/missing baseline -> fall back to `FULL`.

A `FULL` review broadly samples all maintained semantic families and executable evidence needed to establish a repository-wide baseline. An `INCREMENTAL` review reconstructs the semantic changes since the baseline and follows their neighboring authorities. Open findings are always carried forward even when they predate the incremental range.

## Claim-state taxonomy

The generic Consistency-review taxonomy is closed and explicit:

`CURRENT`, `NORMATIVE_DECISION`, `PROPOSED`, `PLANNED`, `IMPLEMENTED_STATUS`, `PARTIAL`, `DEFERRED`, `BLOCKED`, `NON_GOAL`, `HISTORICAL`, `COMPATIBILITY_DEBT`.

A reviewer does not invent another generic state during a run. If the taxonomy proves insufficient, change the contract explicitly. Domain-owned lifecycles such as research statuses remain their own vocabulary and are interpreted through their owning documents rather than being folded into this list.

## Search-driven semantic neighbors

The reviewer should not maintain a second architecture map inside its own instructions. For each material concept, symbol, status, or contract, search the repository and inspect the authoritative current/planning/ADR/test/interface surfaces that encode the same semantics.

Typical examples:

- model semantics -> factory architecture, ADRs, planning, Engine assumptions;
- controllers/DTOs -> API/reference docs, consumers, integration tests;
- toolchain/CI changes -> workflow/config plus testing/contribution policy;
- planning status changes -> acceptance criteria plus executable evidence.

These are examples, not a fixed matrix. Repository evidence determines the actual neighbor set.

When a consistency claim depends on executable evidence, inspect source, tests, configuration, and existing GitHub CI/check evidence through the connector. The review runtime has no local-check requirement.

## Finding persistence

GitHub Issues are the durable finding ledger. The GitHub issue number is the canonical identity; all findings use one title convention:

```text
CONS: <concise semantic title>
```

The earlier numeric `CONS-*` titles were normalized to this form rather than preserved as a compatibility branch.

Finding bodies persist durable diagnostic evidence: severity/category/confidence, conflicting claims/evidence, and authority analysis. Mutable lifecycle state is not duplicated in the body:

- unresolved -> issue open;
- plausible corrective PR -> still open, reported as `IN_FLIGHT`;
- verified fixed on `main` -> close completed;
- duplicate/false positive/superseded -> close with explanation;
- regression -> reopen the same issue.

A merged PR, closed issue, or green CI result is not itself proof of resolution. Current authoritative evidence on the reviewed `main` head is.

Invoking a formal recurring review gives the narrow issue authority required to account for its findings. It does not authorize remediation, unrelated issue edits, or merging.

## Completion and baseline

Completion is recorded directly in issue #295. The reviewer replaces only the `### Weekly Consistency review` section and preserves the rest of the issue body:

```text
### Weekly Consistency review

- last verified: <UTC YYYY-MM-DD>
- reviewed head: <full main SHA>
- accounted result: CLEAN | FINDINGS
- finding issues: none | #<number>, #<number>, ...
- next due / interval: every 7 days
- state: **CURRENT**
```

That edit is the complete recording operation. There is no completion-comment ledger, comment parser, event-driven completion workflow, manual dispatch, or synchronous refresh check.

The scheduled continuous-improvement workflow is independent maintenance. It reads the current weekly record from the body, ages `CURRENT` to `DUE`/`OVERDUE` as time passes, refreshes retrospective counters, and preserves the reviewer-owned accounting fields. If the workflow is delayed, the recorded review head/date/result remain authoritative; only the derived display state may be stale until the next maintenance run.

## Review-quality audit attribution

Finding attribution for a later delivery/review-quality retrospective remains separate post-processing guidance. When evidence establishes it, an audit may record:

- first known bad commit;
- likely introducing PR;
- attribution confidence (`HIGH`, `MEDIUM`, `LOW`);
- origin class (`REVIEW_ESCAPE`, `LEGACY_DRIFT`, `UNRELATED_DRIFT`, `REGRESSION`, `UNKNOWN`).

Use `REVIEW_ESCAPE` only when the reviewed PR's proposed post-merge state already contained the inconsistency and the PR review did not identify it. Do not manufacture attribution to improve process metrics. This audit metadata is not part of the normal Consistency finding schema or identity.

## Ownership

- `.github/agents/consistency.agent.md` owns the review algorithm, evidence rules, issue accounting, and completion protocol.
- GitHub issues own durable finding identity/lifecycle continuity.
- Register issue #295 owns current recurring review state.
- `.github/workflows/continuous-improvement.yml` only refreshes time/count-derived display state; it is not part of review completion.
- This document records maintainer operating guidance and audit attribution conventions.
- Architecture, ADRs, planning, source, tests, config, and public/reference docs remain authoritative for their respective semantic questions.

The intended loop is: diagnose from live evidence, durably account real findings, record the reviewed baseline directly, remediate through ordinary change control, then verify against a later `main` head.
