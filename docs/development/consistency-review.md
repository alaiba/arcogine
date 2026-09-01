# Consistency review operations

> **Status:** maintainer operating guidance as of 2026-09-01. The repository-owned review contract is [`.github/agents/consistency.agent.md`](../../.github/agents/consistency.agent.md); this document records how recurring reviews are operated around that contract.

Arcogine uses a dedicated consistency-review role to detect evidence-backed drift between implementation, architecture, ADRs, planning, public documentation, examples, configuration, tests, CI, recent pull requests, and prior consistency findings.

The consistency reviewer is diagnostic by default. It identifies and explains inconsistencies; it does not silently remediate them or mutate the finding ledger. Confirmed findings are fixed through the normal implementation and pull-request review workflow, then re-verified against a later `main` head.

## Current operating model

GitHub Issues are the durable continuity mechanism for consistency findings. A long-lived Consistency project session may still be useful for working context, but deleting or replacing that session must not erase the durable identity or lifecycle of a persisted finding.

At the beginning of every review, the reviewer must re-read the current `.github/agents/consistency.agent.md` from `main`, resolve the current repository head, and load the open and closed consistency-finding issues before comparing current evidence. Repository and issue state override remembered session state.

A recurring weekly review may be scheduled outside the repository. That schedule is maintainer automation, not repository authority: changing or removing the external schedule does not change the review contract in this repository.

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
      +--> optional explicitly authorized issue-ledger synchronization
      |
      v
normal remediation PRs
      |
      v
later consistency verification on main
```

A merged PR, closed issue, or green CI result is not itself evidence that a finding is resolved. Resolution is established by re-evaluating authoritative evidence on the reviewed `main` head.

For routine recurring runs, use the normal high-scrutiny reasoning setting available in the review environment. Reserve the highest available scrutiny for calibration runs, major architecture transitions, or periods where several capability tracks have changed in parallel. This is operating advice, not a repository requirement, and may need reinterpretation as external tooling evolves.

## Finding persistence

GitHub Issues persist **finding identity and lifecycle**, not product/architecture truth and not the repository comparison baseline.

Persisted findings use the identity rules in the consistency-agent contract:

- the GitHub issue number is the canonical, immutable storage identity;
- the human-readable `CONS-*` alias in the issue title is immutable once bound to that issue;
- the six calibration findings migrated as `CONS-001` through `CONS-006` retain those aliases for continuity;
- new persisted findings derive their alias from the GitHub issue number (for example, issue `#211` becomes `CONS-211`), so no independent counter or reservation protocol is required;
- a diagnostic-only finding that has not been persisted has no durable `CONS-*` identity yet.

The review itself remains read-only by default. Reading/searching the issue ledger is mandatory grounding; creating, editing, commenting on, closing, reopening, assigning, or relabeling finding issues requires explicit authorization for issue-ledger synchronization.

When synchronization is authorized, it follows repository truth rather than issue state:

- create an issue only for a genuinely new durable finding after duplicate/regression matching;
- keep `OPEN` and `IN_FLIGHT` findings open;
- close a finding only after current `main` verifies it as `RESOLVED`, or when it is explicitly `SUPERSEDED` or `WITHDRAWN`;
- never close a finding merely because a remediation PR exists or merged;
- retain closed issues so regression detection can reuse the same semantic finding identity.

This deliberately does **not** mean that every raw observation or exploratory suspicion becomes an issue. A finding must meet the evidence rules in the reviewer contract before it qualifies for persistence.

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

Finding persistence and comparison-baseline persistence are separate problems.

Until Arcogine implements repository-owned baseline persistence, a recurring review should use the last reliable reviewed head available to the review workflow. If that baseline cannot be established confidently, perform a full review rather than pretending an incremental interval is complete.

The reviewer must carry unresolved findings forward even when the commit that introduced them predates the chosen incremental baseline. Advancing a baseline must never make an unresolved finding disappear by construction.

The GitHub Issue ledger therefore solves cross-session finding continuity but does not claim to answer "what was the last fully reviewed `main` SHA?" A future baseline mechanism may be added separately if recurring review needs one.

## Remaining operational question

The finding-ledger question is now decided: durable findings live in GitHub Issues.

The remaining persistence question is narrower: whether Arcogine needs repository-owned **review-baseline** state in addition to the issue ledger and whatever trusted reviewed-head context the recurring workflow already maintains.

Evidence that would justify baseline persistence includes:

- uncertainty about the last fully reviewed `main` head;
- scheduled runs being unable to establish a trustworthy incremental range;
- multiple maintainers or review environments repeatedly falling back to full scans solely because the reviewed-head baseline is unavailable.

If those problems appear in practice, define the baseline contract separately. Do not overload finding issues with baseline semantics.

## Ownership boundaries

Keep these concerns separate:

- `.github/agents/consistency.agent.md` owns the consistency review procedure, evidence rules, finding format, identity/lifecycle rules, and resolution policy.
- GitHub Issues own durable finding identity and lifecycle continuity only.
- This document owns human/maintainer operating guidance for recurring reviews.
- Normal implementation sessions own remediation once a finding has been accepted.
- Independent PR review owns acceptance of remediation changes.
- Architecture, ADR, planning, source, tests, and executable configuration remain authoritative for their respective semantic questions; consistency issues do not replace them.

The intended loop is therefore:

> diagnose independently, persist durable findings deliberately, triage deliberately, remediate through normal change control, then verify against the resulting repository state.
