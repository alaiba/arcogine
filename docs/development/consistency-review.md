# Consistency review operations

> **Status:** maintainer guidance around [`.github/agents/consistency.agent.md`](../../.github/agents/consistency.agent.md).

Arcogine's formal Consistency review runs in a ChatGPT chat session. Repository content starts from a mandatory canonical-`main` Repomix baseline and is reconciled to one exact current `main` target through the repository snapshot protocol; the GitHub connector also provides mutable state, history when needed, and finding accounting.

## Operating loop

```text
read Repomix baseline S
      |
      v
compare S -> live main and resolve exact target T
      |
      +--> identical -> snapshot is target corpus
      |
      +--> descendant + complete delta
      |       -> snapshot for unaffected paths
      |       -> affected paths read at immutable T
      |
      +--> unsafe/incomplete reconciliation -> INCOMPLETE; refresh snapshot
      |
      v
load open CONS: findings
      |
      v
use recent Git history as search-order context when useful
      |
      v
deep-search exact target corpus
  start with new/changed material
  run mandatory breadth passes across all target content
      |
      v
reconcile findings idempotently
  closed CONS: searched only for candidate duplicate/regression
      |
      v
recheck live main == reviewed target T
      |
      v
mutate finding ledger as required
      |
      v
recheck live main == reviewed target T
```

Recent Git history is an attention aid, never a scope boundary. A clean earlier review does not establish that older content is correct.

## Repomix/GitHub boundary

The project Repomix is the reviewer's repository-content baseline. Follow `docs/development/repository-snapshot.md` as the sole authority for reconciling that baseline to exact target `T`; Consistency does not duplicate that protocol here. Do not spend connector calls refetching unaffected static content.

GitHub remains authority for live `main`, finding issue state, PR/review/CI state, commit/compare history, and mutations. PR/history queries are evidence-driven, not routine bulk loading.

A snapshot may be older than live `main`; age alone is not a failure. If the canonical snapshot protocol cannot establish a complete exact target view, the review is `INCOMPLETE` and the snapshot must be refreshed.

The reviewer rechecks `main` before finding accounting and again after finding accounting, before reporting completion. Both checks must still equal reviewed target `T`. If `main` moved during a long review, do not report completion against the old target.

## Review depth

A formal review is repository-wide in intent. Recent Git history may supply recency bias independently of the snapshot `S..T` compare used to establish the corpus. New semantic changes are useful starting points, but the reviewer is expected to search broadly across the exact target corpus and chase mildly suspicious evidence into older content. The lifecycle/status, volatile duplicated-fact, cross-authority current-state, and candidate-closure passes in the agent contract are mandatory minimum discovery coverage.

Those breadth passes also test **authority placement**, not only factual equality. Source comments/Javadocs should own current code behavior and limitations, not future delivery sequencing. Maintained explanatory docs should not copy change-prone executable/configuration values merely to restate them; when the exact value is not itself a contract, historical fact, or reproducibility datum, document the purpose/invariant and point to the executable owner instead. A copied claim can therefore be a consistency finding before it becomes stale.

Closed consistency findings are not preloaded. Search them on demand only when a candidate new finding needs duplicate/regression matching.

The generic claim-state taxonomy is closed:

`CURRENT`, `NORMATIVE_DECISION`, `PROPOSED`, `PLANNED`, `IMPLEMENTED_STATUS`, `PARTIAL`, `DEFERRED`, `BLOCKED`, `NON_GOAL`, `HISTORICAL`, `COMPATIBILITY_DEBT`.

Domain-owned lifecycles keep their own vocabulary.

## Finding persistence

GitHub Issues are the durable finding ledger. The issue number is canonical identity; titles use one convention:

```text
CONS: <concise semantic title>
```

Open issues represent unresolved findings. Finding reconciliation is idempotent: a semantic match reuses its existing issue identity; rerunning a review must not create a duplicate issue or duplicate equivalent evidence. Materially stronger evidence for the same unresolved finding may update that issue narrowly. A corrective PR may make a finding `IN_FLIGHT` but not resolved; only authoritative evidence on reviewed `main` establishes resolution. Regression reopens the same issue. Mutable lifecycle state is not duplicated in issue bodies.

A formal review has narrow authority to reconcile its finding issues. Remediation and merge remain separate workflows.

## Completion

A formal review persists only finding state.

1. Do not mutate findings while analyzing.
2. Immediately before finding-accounting mutations, resolve live `main` again. It must still equal reviewed target `T`; otherwise stop `INCOMPLETE` with no review-accounting mutations and restart against the new exact target.
3. Reconcile finding issues idempotently.
4. Resolve live `main` again. It must still equal reviewed target `T`.
5. Report the review result in the session.

A clean review creates no completion issue, comment, timestamp, previous-reviewed-head record, or other bookkeeping artifact. Its value is the analysis itself; only unresolved findings need durable repository work state.

## Report

```text
Consistency review
Head: <sha>
Findings: none | #<number>, #<number>, ...
Coverage:
- recency/concept pass: COMPLETE | NOT_APPLICABLE | INCOMPLETE
- lifecycle/status prose sweep: COMPLETE | INCOMPLETE
- volatile duplicated-fact sweep: COMPLETE | INCOMPLETE
- cross-authority current-state sweep: COMPLETE | INCOMPLETE
- candidate closure checks: COMPLETE | INCOMPLETE
- open finding carry-forward: COMPLETE | NOT_APPLICABLE | INCOMPLETE
Limitations: none | <specific incomplete pass or other limitation>
Overall: CLEAN | FINDINGS | INCOMPLETE
```

A clean report is session output only; it is not persisted as bookkeeping.
