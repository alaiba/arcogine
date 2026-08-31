# Consistency review operations

> **Status:** maintainer operating guidance as of 2026-08-30. The repository-owned review contract is [`.github/agents/consistency.agent.md`](../../.github/agents/consistency.agent.md); this document records how recurring reviews are operated around that contract.

Arcogine uses a dedicated consistency-review role to detect evidence-backed drift between implementation, architecture, ADRs, planning, public documentation, examples, configuration, tests, CI, and recent pull requests.

The consistency reviewer is diagnostic by default. It identifies and explains inconsistencies; it does not silently remediate them. Accepted findings are fixed through the normal implementation and pull-request review workflow, then re-verified by a later consistency run.

## Current operating model

The current maintainer workflow uses a long-lived Consistency project session for review continuity. At the beginning of each run, that session should re-read the current `.github/agents/consistency.agent.md` from `main` rather than rely on a remembered copy.

A recurring weekly review is currently scheduled outside the repository. That schedule is maintainer automation, not repository authority: changing or removing the external schedule does not change the review contract in this repository.

The normal review sequence is:

```text
previous findings
      |
      v
verify current main
      |
      +--> RESOLVED / STILL_OPEN / IN_FLIGHT / other defined disposition
      |
      v
incremental consistency scan
      |
      v
new evidence-backed findings
      |
      v
triage
      |
      v
normal remediation PRs
      |
      v
later consistency verification
```

A merged PR is not itself evidence that a finding is resolved. Resolution is established by re-evaluating the resulting state on the reviewed `main` head.

For routine recurring runs, use the normal high-scrutiny reasoning setting available in the review environment. Reserve the highest available scrutiny for calibration runs, major architecture transitions, or periods where several capability tracks have changed in parallel. This is operating advice, not a repository requirement, and may need reinterpretation as external tooling evolves.

## Finding persistence

Raw consistency reports are not automatically persisted as repository artifacts.

During calibration and ordinary recurring review, the long-lived Consistency session may retain the report history needed to compare findings across runs. After triage:

- false positives and low-value calibration noise may remain only in the review session;
- confirmed durable work should normally be represented by the GitHub issue or pull request that owns remediation;
- findings that require an architecture/product decision should flow into the appropriate architecture/planning/ADR process rather than being treated as ordinary code defects;
- accepted debt should be explicit in an appropriate durable artifact when forgetting it would create material risk.

Do not create a repository ledger merely to mirror every scanner result. A durable ledger is justified only if recurring review cannot reliably carry unresolved findings and baselines forward without one.

## Baseline discipline

Incremental review is useful only when its baseline is trustworthy.

Until Arcogine implements repository-owned baseline persistence, a recurring review should use the last reliable reviewed head available to the review workflow. If that baseline cannot be established confidently, perform a full review rather than pretending an incremental interval is complete.

The reviewer must carry unresolved findings forward even when the commit that introduced them predates the chosen incremental baseline. Advancing a baseline must never make an unresolved finding disappear by construction.

## Open operational question

The remaining question is whether recurring reviews can maintain sufficient continuity using the current project-session plus external-schedule model, or whether Arcogine needs repository-owned persistence for review state.

Evidence that would justify a durable baseline/finding ledger includes:

- unresolved findings being lost between runs;
- uncertainty about the last fully reviewed `main` head;
- duplicate findings being repeatedly rediscovered because prior disposition is unavailable;
- scheduled runs lacking access to the continuity available to the long-lived review session;
- multiple maintainers or review environments needing to share one authoritative unresolved-finding state.

If those problems appear in practice, define the persistence contract before making consistency review more autonomous. The likely first slice is durable baseline plus unresolved-finding state, not automatic remediation.

## Ownership boundaries

Keep these concerns separate:

- `.github/agents/consistency.agent.md` owns the consistency review procedure, evidence rules, finding format, lifecycle, and resolution policy.
- This document owns human/maintainer operating guidance for recurring reviews.
- Normal implementation sessions own remediation once a finding has been accepted.
- Independent PR review owns acceptance of remediation changes.
- Architecture, ADR, and planning documents remain authoritative for their respective semantic questions; consistency review does not replace them.

The intended loop is therefore:

> diagnose independently, triage deliberately, remediate through normal change control, then verify against the resulting repository state.
