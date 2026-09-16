# Consistency review operations

> **Status:** maintainer guidance around [`.github/agents/consistency.agent.md`](../../.github/agents/consistency.agent.md).

Arcogine's formal Consistency review runs in a ChatGPT chat session. Repository content comes from a mandatory Repomix attachment generated from the exact current canonical `main`; the GitHub connector is reserved for mutable state, history when needed, and issue/register writes.

## Operating loop

```text
resolve live main
      |
      v
require Repomix commit == live main
      |
      +--> missing/stale/malformed -> INCOMPLETE; update and retry
      |
      v
read #295 + open CONS: findings
      |
      v
use previous reviewed head as recency anchor, if present
      |
      v
deep-search Repomix corpus
  start with new/changed material
  follow semantic evidence anywhere
      |
      v
reconcile findings
  closed CONS: searched only for candidate duplicate/regression
      |
      v
recheck live main == Repomix commit
      |
      v
mutate finding ledger as required + update #295
```

The previous reviewed head is an attention aid, never a scope boundary. A clean earlier review does not establish that older content is correct.

## Repomix/GitHub boundary

The project Repomix is the reviewer's repository-content data plane. Once its header is proven to match live `main`, use it for `AGENTS.md`, the Consistency contract, docs, source, tests, workflows, configuration, examples, and cross-repository text search. Do not spend connector calls refetching the same static content.

GitHub remains authority for live `main`, issue #295, finding issue state, PR/review/CI state, commit/compare history, and mutations. PR/history queries are evidence-driven, not routine bulk loading.

There is no stale-snapshot fallback. Missing, malformed, or non-current Repomix means the formal review is `INCOMPLETE`; generate/upload the current-main snapshot and retry.

The reviewer rechecks `main` immediately before accounting mutations. If `main` moved during a long review, do not record completion against the old corpus.

## Review depth

A formal review is repository-wide in intent. If #295 has a previous reviewed head, one GitHub comparison to current `main` supplies the recency bias. New semantic changes are useful starting points, but the reviewer is expected to search broadly in the local corpus and chase mildly suspicious evidence into older content.

Closed consistency findings are not preloaded. Search them on demand only when a candidate new finding needs duplicate/regression matching.

The generic claim-state taxonomy is closed:

`CURRENT`, `NORMATIVE_DECISION`, `PROPOSED`, `PLANNED`, `IMPLEMENTED_STATUS`, `PARTIAL`, `DEFERRED`, `BLOCKED`, `NON_GOAL`, `HISTORICAL`, `COMPATIBILITY_DEBT`.

Domain-owned lifecycles keep their own vocabulary.

## Finding persistence

GitHub Issues are the durable finding ledger. The issue number is canonical identity; titles use one convention:

```text
CONS: <concise semantic title>
```

Open issues represent unresolved findings. A corrective PR may make a finding `IN_FLIGHT` but not resolved; only authoritative evidence on reviewed `main` establishes resolution. Regression reopens the same issue. Mutable lifecycle state is not duplicated in issue bodies.

A formal review has narrow authority to reconcile its finding issues and update the weekly section of #295. Remediation and merge remain separate workflows.

## Completion

Issue #295 is fixed mandatory repository state. It is never discovered, bootstrapped, recreated, or substituted. If it is missing, inaccessible, wrongly titled, or malformed, the review is `INCOMPLETE`.

A completed review directly writes the weekly section:

```text
### Weekly Consistency review

- last verified: <UTC YYYY-MM-DD>
- reviewed head: <full main SHA>
- accounted result: CLEAN | FINDINGS
- finding issues: none | #<number>, #<number>, ...
- next due / interval: every 7 days
- state: **CURRENT**
```

The recorded head is a recency anchor for the next review, not a certificate that older content was exhaustively cleared. There is no completion-comment ledger or event-driven completion workflow.

The scheduled continuous-improvement workflow independently ages due state and refreshes retrospective counters; it is not part of review completion.
