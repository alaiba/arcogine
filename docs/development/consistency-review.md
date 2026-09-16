# Consistency review operations

> **Status:** maintainer guidance around the executable review contract in [`.github/agents/consistency.agent.md`](../../.github/agents/consistency.agent.md).

Arcogine's formal Consistency review runs in a ChatGPT chat session using the GitHub connector. It is not required to run in a local checkout or remain compatible with local coding-agent runtimes. The review contract therefore assumes repository reads/writes happen through GitHub and must not depend on `git`, `gh`, shell commands, or locally executing repository scripts.

## Operating loop

The recurring review is deliberately small:

```text
read live main + register #295
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
reconcile open/closed consistency findings
        |
        v
create/reopen/close finding issues as required
        |
        v
post one completion comment to #295
        |
        v
workflow refreshes the derived register state
```

The reviewer does not run a second PR-review mode. Open PRs are inspected only when they explain history or show that an existing finding is plausibly `IN_FLIGHT`; independent PR acceptance remains owned by the PR Reviewer.

Ad-hoc questions such as “is this architecture claim consistent with the implementation?” are ordinary read-only chat analysis. They do not need a formal `DIAGNOSTIC_ONLY` mode and do not advance the weekly baseline.

## Grounding and scope

Every formal review re-reads current `main`, `AGENTS.md`, the live consistency contract, register issue #295, and the open/closed consistency-finding ledger. Uploaded snapshots and conversation memory are not current repository authority.

The reviewer selects scope from the register rather than asking the user:

- no accounted reviewed head -> `FULL`;
- accounted reviewed head -> `INCREMENTAL` from that head to current `main`;
- unusable/missing baseline -> fall back to `FULL`.

A `FULL` review broadly samples all maintained semantic families and executable evidence needed to establish a repository-wide baseline. An `INCREMENTAL` review reconstructs the semantic changes since the baseline and follows their neighboring authorities. Unresolved findings are always carried forward even when they predate the incremental range.

## Search-driven semantic neighbors

The reviewer should not maintain a second architecture map inside its own instructions. For each material concept, symbol, status, or contract, search the repository and inspect the authoritative current/planning/ADR/test/interface surfaces that encode the same semantics.

Typical examples:

- model semantics -> factory architecture, ADRs, planning, Engine assumptions;
- controllers/DTOs -> API/reference docs, consumers, integration tests;
- toolchain/CI changes -> workflow/config plus testing/contribution policy;
- planning status changes -> acceptance criteria plus executable evidence.

These are examples, not a fixed matrix. Repository evidence determines the actual neighbor set.

## Finding persistence

GitHub Issues are the durable finding ledger. The GitHub issue number is the sole canonical identity.

Historical findings with `CONS-001`, `CONS-002`, and later numeric `CONS-*` titles remain valid and searchable. New findings use:

```text
CONS: <concise semantic title>
```

No new alias counter, issue-number-derived alias, reservation protocol, or `[CONSISTENCY-UNBOUND]` staging issue is needed.

Before creating a finding, search open and closed current/historical consistency issues by semantic subject and evidence. Regressions reuse and reopen the original issue.

Finding bodies persist durable diagnostic evidence (severity/category/confidence, conflicting claims/evidence, and authority analysis). Mutable lifecycle state is not duplicated in the body:

- unresolved -> issue open;
- plausible corrective PR -> still open, reported as `IN_FLIGHT`;
- verified fixed on `main` -> close completed;
- duplicate/false positive/superseded -> close with explanation;
- regression -> reopen the same issue.

A merged PR, closed issue, or green CI result is not itself proof of resolution. Current authoritative evidence on the reviewed `main` head is.

Invoking a formal recurring review gives the narrow issue authority required to account for its findings. It does not authorize remediation, unrelated issue edits, or merging.

## Completion and baseline

Completion evidence is an append-only comment on register issue #295:

```text
Consistency review completed
head: <full main SHA>
scope: FULL | INCREMENTAL
findings: none | #<number>, #<number>, ...
```

GitHub supplies the comment author association and creation timestamp, so the comment does not restate them. `findings: none` is the clean result; otherwise every cited number must resolve to a persisted consistency-finding issue.

The register workflow reacts to the completion comment through GitHub's `issue_comment` event and updates its derived state. The reviewer does not need `repository_dispatch`, `workflow_dispatch`, `gh api`, or a synchronous post-comment refresh check. The scheduled workflow remains a backstop if an event-driven refresh is delayed or fails.

The weekly baseline advances only from a trusted, structurally valid, fully accounted `FULL` or `INCREMENTAL` completion. An isolated `INCREMENTAL` comment cannot establish the first baseline.

## Mechanical evidence

The ChatGPT reviewer does not execute repository checks locally. When a consistency claim depends on CI or a repository-owned checker, inspect its source/tests and existing GitHub check/workflow evidence. Lack of a local shell is not an `INCOMPLETE` condition.

`INCOMPLETE` is reserved for missing repository/ledger evidence necessary to judge the requested scope or for failure to durably account for findings.

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
- Register issue #295 plus `.github/workflows/continuous-improvement.yml` own recurring due-state display.
- This document records maintainer operating guidance and audit attribution conventions.
- Architecture, ADRs, planning, source, tests, config, and public/reference docs remain authoritative for their respective semantic questions.

The intended loop is: diagnose from live evidence, durably account real findings, remediate through ordinary change control, then verify against a later `main` head.