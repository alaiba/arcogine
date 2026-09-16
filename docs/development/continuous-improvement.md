# Continuous-improvement operating model

> **Status:** canonical operating model for when Arcogine's improvement loops run and how their active state is tracked.

Arcogine has three distinct improvement loops. None substitutes for another.

## Session-close Kaizen

- **Trigger:** `.?` at the close of a meaningful coding-agent session; see `AGENTS.md`.
- **Purpose:** preserve durable lessons as executable safeguards, standard work, or maintained knowledge.
- **Owner:** the coding agent.
- **Cadence:** event-driven only; no recurring due state.

## Weekly Consistency review

- **Trigger/cadence:** weekly, plus additional review after major architecture/status transitions when useful.
- **Purpose:** repository semantic coherence across implementation, architecture, ADRs, planning, docs, examples, config, tests, CI, and prior findings.
- **Owner/runtime:** the Consistency reviewer in a ChatGPT chat session using the GitHub connector. The detailed algorithm is `.github/agents/consistency.agent.md`.
- **Execution:** manual/user-invoked. The workflow tracks due state but never performs the judgment-bearing review.
- **Scope:** selected automatically. With no valid baseline the review is `FULL`; otherwise it is `INCREMENTAL` from the recorded head to current `main`.
- **Finding accounting:** invoking a formal review authorizes only the GitHub issue operations needed to account for that review's consistency findings. It does not authorize remediation or merging.

## Delivery-process retrospective

- **Trigger:** evidence-based, not calendar-driven — about 25 additional substantive merges since the recorded baseline, or 2 high-confidence post-merge process escapes, or 1 P1 lifecycle/process escape. See the latest dated `delivery-process-retrospective-YYYY-MM-DD.md` for the full method.
- **Purpose:** empirical evidence about delivery-process performance and waste.
- **Owner:** a human/agent who judges whether the substantive trigger actually fired.
- **Mechanical guard:** the workflow may count raw merged PRs and surface `CHECK_TRIGGER`; it never turns that raw count into an automatic retrospective decision.

## Authority boundaries

| Concern | Owning authority |
| --- | --- |
| Session-close Kaizen | `AGENTS.md` |
| Consistency review algorithm | `.github/agents/consistency.agent.md` |
| Consistency operating guidance | `docs/development/consistency-review.md` |
| Retrospective method/evidence | dated retrospective documents |
| Current recurring-obligation state | GitHub issue #295 |
| Raw delivery evidence | GitHub PR/review/CI/issue history |

PR review remains governed by `docs/development/reviewing.md`; it is evidence for retrospectives, not a fourth improvement ceremony.

## Continuous improvement register

GitHub issue **#295**, titled `Continuous improvement register`, is the fixed operational register for this repository. It already exists; the workflow does not discover, bootstrap, or replace it. If issue #295 is unavailable or no longer has that title, the workflow fails rather than writing elsewhere.

The register body has two ownership regions separated by HTML markers:

- **Workflow-managed recurring obligations** inside `<!-- continuous-improvement:obligations:start -->` / `...:end -->`.
- **Agent/human-managed active interventions** outside those markers.

The workflow changes only its marker region and preserves the intervention region. The register is active state, not an append-only process database; detailed historical evidence belongs in PRs, issues, commits, and dated retrospectives.

### Consistency completion evidence

A completed formal Consistency review posts one comment to issue #295:

```text
Consistency review completed
head: <full main SHA>
scope: FULL | INCREMENTAL
findings: none | #<number>, #<number>, ...
```

The workflow accepts a comment only when:

- the commenter has trusted GitHub author association (`OWNER`, `MEMBER`, or `COLLABORATOR`);
- `head` is a 40-character SHA;
- `scope` is `FULL` or `INCREMENTAL`;
- `findings` is either `none` or a unique comma-separated list of GitHub issue numbers;
- every cited issue is a persisted Consistency finding.

GitHub's comment `created_at` is the completion timestamp; the comment does not supply its own clock. `findings: none` means clean; a non-empty issue list means findings. An `INCREMENTAL` completion is eligible only after an earlier valid `FULL` completion established the baseline.

Historical completion-comment formats remain ordinary issue history but no longer establish the baseline under this schema. Until a valid current-schema `FULL` completion exists, the weekly obligation remains `DUE` and the next formal review is `FULL`.

### Finding identities

The GitHub issue number is the canonical finding identity.

Historical numeric `CONS-*` titles remain accepted. New findings use `CONS: <semantic title>`. The workflow validates both forms and requires durable diagnostic metadata (`Severity:` and `Category:`) in the issue body; it does not require or parse a duplicated lifecycle `Status:` field.

### Automatic refresh

The register refresh workflow reacts directly to a new completion comment through GitHub's `issue_comment` event. This is the normal post-review path:

```text
ChatGPT reviewer posts completion comment to #295
                    |
                    v
GitHub issue_comment event
                    |
                    v
workflow recomputes and updates register state
```

The reviewer does not invoke `repository_dispatch`, `workflow_dispatch`, `gh api`, or another command, and does not need to wait for or synchronously verify the derived register-body refresh before completing the chat response.

The scheduled workflow remains a backstop and periodic due-state refresh. Pushes to the helper/workflow/operating-model files also refresh the derived state.

## Weekly due state

The workflow derives:

- `CURRENT` when the latest valid completion is at most 7 days old;
- `DUE` when there is no valid completion or it is more than 7 but at most 14 days old;
- `OVERDUE` when it is more than 14 days old.

The register records the latest reviewed head, clean/findings result, cited finding issue numbers, and the due state.

## Retrospective baseline

The retrospective baseline and explicit escape evidence live in `.github/scripts/continuous-improvement-data.json`. A later verified retrospective advances that data as an ordinary repository change; the helper's state-derivation logic does not need to change.

Raw merged-PR count reaching the configured guard threshold produces `CHECK_TRIGGER`, not an automatic `DUE`. Judgment about whether the substantive retrospective trigger fired remains outside the workflow.

## Every-agent reminder

On the first normal repository grounding of a session, Arcogine agents inspect issue #295's workflow-managed obligation state as described in `AGENTS.md`. `CURRENT` is silent; `DUE`/`OVERDUE` and `CHECK_TRIGGER` are mentioned at most once and never derail the requested task.

## Non-goals

This system does not automatically execute a Consistency review or retrospective, require local coding-agent compatibility for the Consistency reviewer, become a general process database, or create one issue per improvement intervention. Its job is to preserve recurring obligation state and durable Consistency finding identities while leaving semantic judgment in the ChatGPT review session.