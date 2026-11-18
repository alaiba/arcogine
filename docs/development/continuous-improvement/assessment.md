# Continuous Improvement assessment

Part of the [continuous-improvement operating model](../continuous-improvement.md#routed-topics).

The repository's user-invocable [Continuous Improvement agent](../agent-roles/continuous-improvement.md) is a light advisory checkpoint over recent delivery activity. It is not a fourth improvement loop.

Invoke it with exactly:

`Assess continuous improvement.`

The assessment is intentionally light and broad. It re-grounds against current repository and live GitHub evidence, scans activity since the preceding checkpoint opened, and recommends the smallest worthwhile next practice or diagnostic instead of performing deep analysis itself. The scan centers on pull requests merged into `main`, non-PR issues closed in that window, and current open work needed only to avoid duplicating ownership.

A repository workflow, [`continuous-improvement-reminder.yml`](../../../.github/workflows/continuous-improvement-reminder.yml), provides the only standing reminder. Once per week it creates an issue titled exactly `Continuous improvement checkpoint` only when no open issue with that title exists. The workflow performs no health analysis and derives no due state; the issue simply prompts an explicit Continuous Improvement assessment. When an open checkpoint triggers the assessment, the previous checkpoint's opening time is the normal lower bound; otherwise the latest checkpoint's opening time is used. Closing the reminder acknowledges it, after which a later scheduled run may create another.

The assessment may recommend Consistency, a delivery-process retrospective, a focused test-evidence diagnostic, or a narrow process-tightening action. It does not substitute for those deeper practices, and every actionable recommendation includes a fresh-session invocation: a short inline invocation of an existing role, or, when a complete prompt is needed, its persisted branch + exact commit SHA + path under the [prompt handoff preflight](../agent-rules/handoffs.md#prompt-handoff-preflight). It persists no due state, completion timestamps, thresholds, intervention register, or diagnostic score.
