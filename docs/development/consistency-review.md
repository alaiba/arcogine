# Consistency review operations

> **Status:** maintainer operating guidance as of 2026-09-01. The repository-owned review contract is [`.github/agents/consistency.agent.md`](../../.github/agents/consistency.agent.md); this document records how recurring reviews are operated around that contract.

Arcogine uses a dedicated consistency-review role to detect evidence-backed drift between implementation, architecture, ADRs, planning, public documentation, examples, configuration, tests, CI, recent pull requests, and prior consistency findings.

The consistency reviewer is diagnostic by default. It identifies and explains inconsistencies; it does not silently remediate them or mutate the finding ledger. A normal baseline-advancing `FULL` or `INCREMENTAL` run has the narrow accounting authority described below so its unresolved findings cannot be lost. Confirmed findings are fixed through the normal implementation and pull-request review workflow, then re-verified against a later `main` head.

## Current operating model

GitHub Issues are the durable continuity mechanism for consistency findings. A long-lived Consistency project session may still be useful for working context, but deleting or replacing that session must not erase the durable identity or lifecycle of a persisted finding.

At the beginning of every review, the reviewer must re-read the current `.github/agents/consistency.agent.md` from `main`, resolve the current repository head, and load the open and closed consistency-finding issues before comparing current evidence. Repository and issue state override remembered session state.

The weekly Consistency-review cadence is a repository-owned obligation, not maintainer automation outside this repository. Its scheduling, due-state tracking, and the continuous-improvement register that carries that state are defined in [`docs/development/continuous-improvement.md`](continuous-improvement.md); this document (and `.github/agents/consistency.agent.md`) continues to own how a review is actually performed once invoked.

The normal review sequence is:

```text
GitHub consistency issues
      |
      v
verify current main
      |
      +--> RESOLVED / OPEN / IN_FLIGHT / SUPERSEDED / WITHDRAWN
      |
      v
incremental or full consistency scan
      |
      v
new evidence-backed findings
      |
      v
triage
      |
      +--> required finding-ledger accounting for a qualifying FULL/INCREMENTAL run
      |
      v
normal remediation PRs
      |
      v
later consistency verification on main
```

A merged PR, closed issue, or green CI result is not itself evidence that a finding is resolved. Resolution is established by re-evaluating authoritative evidence on the reviewed `main` head.

For routine recurring runs, use the normal high-scrutiny reasoning setting available in the review environment. Reserve the highest available scrutiny for calibration runs, major architecture transitions, or periods where several capability tracks have changed in parallel. This is operating advice, not a repository requirement, and may need reinterpretation as external tooling evolves.

## Documentation-lifetime consistency

Recurring consistency review must treat documentation lifetime as a first-class consistency boundary. Initiative-local stage, gate, and slice identifiers, and PR-local review/finding identifiers (see `AGENTS.md`), are useful in `docs/planning/`, PRs/reviews, and delivery history (including commit messages) while work is active, but durable semantic naming — Markdown outside `docs/planning/`, and non-Markdown durable artifacts such as code comments, workflow definitions, and test names — must name the semantic capability, contract, identity, invariant, or behavior directly.

A full or incremental consistency scan should therefore check two things:

- whether temporary delivery coordinates have leaked into durable filenames, prose, comments, or test/workflow names; and
- whether a durable document still depends on an obsolete planning artifact for its meaning even when no machine-detectable coordinate remains.

The repository vocabulary checker provides a fail-closed syntactic baseline. It is not sufficient evidence of semantic self-containment: reviewers still need to recognize prose such as “the next stage” or “the previous slice” when those phrases only make sense in a plan that may later disappear.

Accepted and Superseded ADRs may be clarified under the semantics-preserving amendment policy in [`../architecture/decisions/README.md`](../architecture/decisions/README.md). During consistency review, such an amendment is valid only when the historical decision, applicability, constraints, alternatives, consequences, and impact remain unchanged. If an edit changed the architecture rather than its presentation, the inconsistency is the use of an editorial amendment where a superseding ADR was required.

## Finding persistence

GitHub Issues persist **finding identity and lifecycle**, not product/architecture truth. The continuous-improvement register carries the accounted reviewed-head evidence for the recurring baseline.

Persisted findings use the identity rules in the consistency-agent contract:

- the GitHub issue number is the canonical, immutable storage identity;
- the human-readable `CONS-*` alias in the issue title is immutable once bound to that issue;
- the six calibration findings migrated as `CONS-001` through `CONS-006` retain those aliases for continuity;
- new persisted findings derive their alias from the GitHub issue number (for example, issue `#211` becomes `CONS-211`), so no independent counter or reservation protocol is required;
- a diagnostic-only finding that has not been persisted has no durable `CONS-*` identity yet.

The review remains read-only for `DIAGNOSTIC_ONLY` and `PR_FORWARD` runs unless separate synchronization authority is given. Invoking a normal baseline-advancing `FULL` or `INCREMENTAL` review authorizes only the finding-ledger bookkeeping required to account for that review; it does not authorize remediation, arbitrary issue edits, or merging.

When synchronization is authorized, it follows repository truth rather than issue state:

- create an issue only for a genuinely new durable finding after duplicate/regression matching;
- complete that accounting before posting recurring completion evidence;
- keep `OPEN` and `IN_FLIGHT` findings open;
- close a finding only after current `main` verifies it as `RESOLVED`, or when it is explicitly `SUPERSEDED` or `WITHDRAWN`;
- never close a finding merely because a remediation PR exists or merged;
- retain closed issues so regression detection can reuse the same semantic finding identity.

This deliberately does **not** mean that every raw observation or exploratory suspicion becomes an issue. A finding must meet the evidence rules in the reviewer contract before it qualifies for persistence. For a qualifying recurring run, however, every unresolved finding that needs lifecycle continuity must have a durable disposition before the reviewed head can advance the baseline; the old immediate-remediation P3/Nit exception cannot leave such a finding only in session output.

## Finding attribution

Consistency findings may later be used as evidence in a separate review-quality audit. Attribution for that audit must be evidence-backed so the audit distinguishes a PR-review escape from unrelated or inherited repository drift.

This section is **maintainer audit/post-processing guidance**, not an extension of the executable Consistency agent's required finding or run-report schema. Ordinary Consistency-agent runs remain governed by `.github/agents/consistency.agent.md` and are not required to populate the attribution fields below. If Arcogine later wants every consistency run to produce this metadata automatically, the executable agent contract must be changed explicitly in a separate coherent update.

When conducting a review-quality audit, derive or preserve the following attribution facts from repository history where they can be established:

- **first known bad commit** — earliest verified commit in the inspected history where the inconsistency is present;
- **likely introducing PR** — PR whose merged semantic transition introduced or should have reconciled the stale neighbor;
- **attribution confidence** — `HIGH`, `MEDIUM`, or `LOW` based on how directly history establishes causation;
- **origin class** — `REVIEW_ESCAPE`, `LEGACY_DRIFT`, `UNRELATED_DRIFT`, `REGRESSION`, or `UNKNOWN`.

Use `REVIEW_ESCAPE` only when the evidence supports all of these: the relevant semantic change was in a reviewed PR, the stale or contradictory neighbor already existed in that PR's proposed post-merge state, and the PR review did not identify it before merge. A later consistency finding is not automatically a reviewer failure merely because it was discovered after a PR.

Use `LEGACY_DRIFT` when the inconsistency predates the inspected review window or cannot reasonably be tied to the semantic transition under review. Use `UNRELATED_DRIFT` when a recent PR is nearby in time but did not change the concept or authority involved. Use `REGRESSION` when a previously resolved semantic inconsistency reappears.

Do not manufacture attribution to improve metrics. If the introducing point cannot be established from repository history, record `UNKNOWN`. Attribution is diagnostic audit metadata, not product/architecture authority, not part of the finding's durable semantic identity, and not required to be persisted in a consistency issue.

## Calibration migration

The original calibration findings were migrated to GitHub Issues #204-#209. Their legacy aliases remain stable:

- `CONS-001` -> #204
- `CONS-002` -> #205
- `CONS-003` -> #206
- `CONS-004` -> #207
- `CONS-005` -> #208
- `CONS-006` -> #209

Those aliases are grandfathered; new findings use the issue-number-derived alias rule instead of continuing a separate sequential counter.

## Baseline discipline

Finding persistence and comparison-baseline persistence remain separate concerns, but the recurring baseline now has an explicit accounting contract. The continuous-improvement register may treat a completion as a reviewed baseline only when the comment is trusted, non-future, structurally complete, and accounted:

- `FULL` or `INCREMENTAL` is the mode;
- `CLEAN` cites `finding issues: none`;
- `FINDINGS` cites one or more verified persisted Consistency issue numbers;
- every unresolved finding needing lifecycle continuity has been durably identified before the comment is posted.

`PR_FORWARD`, `DIAGNOSTIC_ONLY`, legacy four-line comments, malformed comments, and completion claims containing `UNPERSISTED` findings never establish or advance the weekly `main` baseline. If no accounted baseline exists, the obligation is `DUE` and the next qualifying run must be `FULL`; do not pretend an incremental interval is complete.

The reviewer must carry unresolved findings forward even when the commit that introduced them predates the chosen incremental baseline. Advancing a baseline must never make an unresolved finding disappear by construction. After posting a qualifying completion comment, trigger the existing `repository_dispatch` register refresh when possible and verify the managed register state; report dispatch or verification failure explicitly.

The old September 11 completion marker remains visible as historical evidence, but its missing result and finding-accounting fields mean it is not an accounted baseline. Until a new qualifying completion is recorded, the register must therefore show `DUE` (or an equally explicit not-accounted state).


## Ownership boundaries

Keep these concerns separate:

- `.github/agents/consistency.agent.md` owns the consistency review procedure, evidence rules, finding format, identity/lifecycle rules, and resolution policy.
- GitHub Issues own durable finding identity and lifecycle continuity only.
- This document owns human/maintainer operating guidance for recurring reviews and separate review-quality audit attribution conventions; it does not add fields to the executable Consistency agent's finding/report contract.
- Normal implementation sessions own remediation once a finding has been accepted.
- Independent PR review owns acceptance of remediation changes.
- Architecture, ADR, planning, source, tests, and executable configuration remain authoritative for their respective semantic questions; consistency issues do not replace them.

The intended loop is therefore:

> diagnose independently, persist durable findings deliberately, triage deliberately, remediate through normal change control, then verify against the resulting repository state.
