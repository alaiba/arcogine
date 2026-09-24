# Continuous-improvement operating model

> **Status:** canonical operating model for when Arcogine's improvement loops run, what each loop records, and where its state lives.

Arcogine has three distinct improvement loops. None substitutes for another.

## Continuous Improvement assessment

The repository's user-invocable [Continuous Improvement agent](../../.github/agents/continuous-improvement.agent.md) is an advisory health check over these practices and the delivery controls around them. It is not a fourth improvement loop.

Invoke it with the repository shorthand `.!` or a prompt such as:

`Assess continuous improvement.`

The assessment re-grounds against current repository and live GitHub evidence, reports the health of the practices, recommends which practice—if any—is worth running, and identifies concrete opportunities to simplify or strengthen standard work. Its status labels are ephemeral report language only. It does not persist due state, completion timestamps, thresholds, or an intervention register, and it does not substitute for a formal Consistency review or delivery-process retrospective.

A repository workflow, [`continuous-improvement-reminder.yml`](../../.github/workflows/continuous-improvement-reminder.yml), provides the only standing reminder. Once per week it creates an issue titled exactly `Continuous improvement checkpoint` only when no open issue with that title exists. The workflow performs no health analysis and derives no due state; the issue simply prompts an explicit Continuous Improvement assessment. Closing the issue acknowledges the reminder, after which a later scheduled run may create a new one.

## Session-close Kaizen

- **Trigger:** `.?` at the close of a meaningful coding-agent session; see `AGENTS.md`.
- **Purpose:** preserve durable lessons as executable safeguards, standard work, or maintained knowledge.
- **Owner:** the coding agent.
- **Cadence:** event-driven only; no recurring due state.

At `.?`, inspect the current session and live repository for anything learned, decided, repeated, or
encountered that should survive deletion of the conversation. Classify each material candidate as one
of:

- **Already encoded** — the repository already captures the lesson or invariant adequately; make no
  duplicate change.
- **Bake in** — the lesson is durable and generally reusable; identify the narrowest authoritative
  repository surface that should encode it.
- **Follow-up** — the improvement is worthwhile but belongs in separate work rather than being
  smuggled into the current PR or slice.
- **Discard** — the observation is situational, transient, or otherwise not worth preserving.

Prefer the strongest durable capture that actually fits the lesson. Use an executable guard/test only
when the invariant is mechanically observable and the guard exercises behavior or repository state
rather than merely asserting that instruction prose still contains particular wording. Otherwise
prefer canonical helper/tooling, agent/contributor standard work, maintained documentation, then
canonical architecture or specification for genuinely architectural or hard-to-reverse constraints.
Generalize incidents into semantic rules rather than preserving session or PR coordinates as durable
concepts. Prefer improving an existing authoritative artifact over creating a new one.

Do not manufacture a lesson merely to produce an output. Default output reports material findings and
actions plus the deletion verdict; it does not narrate adjacent practices that the session gives no
concrete reason to invoke — for example "Consistency review not warranted" or "retrospective not
assessed" are not useful by default (see [Invocation boundaries](#invocation-boundaries)). Finish
every review with an explicit deletion verdict: either the session is safe to delete because nothing
unique remains, or name exactly what still needs to be captured first.

## Consistency review

- **Recommended cadence:** roughly weekly, and after significant architecture/status transitions when useful.
- **Purpose:** deep repository semantic review across implementation, architecture/specifications, planning, docs, examples, config, tests, CI, and prior findings.
- **Owner/runtime:** the Consistency reviewer using the canonical repository-snapshot protocol plus live GitHub state.
- **Durable output:** only evidence-backed `CONS:` finding issues that require resolution.

The cadence is guidance, not persisted scheduler state. Arcogine does not maintain a last-reviewed timestamp, previous-reviewed-head ledger, or `CURRENT`/`DUE`/`OVERDUE` state for Consistency.

A clean review persists nothing after returning its review result in chat. A review with findings creates, updates, reopens, or closes the applicable `CONS:` issues. Those issues are the durable outcome because they affect future work.

Recent Git history may be used as a search-order heuristic, but no previous-review coordinate bounds review scope or is required for correctness.

## Delivery-process retrospective

### Invocation

The retrospective runs only when explicitly requested. Arcogine keeps no standing retrospective cadence, threshold, due state, or automatic reminder, and Session-close Kaizen does not evaluate whether a retrospective is due.

Versioned baseline state for an explicitly invoked retrospective lives in:

`.github/continuous-improvement/retrospective.json`

### Purpose

Determine whether Arcogine's delivery controls are reducing recurring waste and escapes, decide which prior changes should be retained/retired/superseded, and create owned follow-up work only where the evidence justifies it.

The retrospective is **not** a process-history dump, an issue ledger, or a second planning system.

### Mechanical evidence first

Before interpretation:

1. choose one exact main-target `throughPr` that bounds the run;
2. acquire and save a complete Retrospective Evidence v1 bundle:

   ```bash
   node infra/dev/delivery-retrospective-github.mjs \
     --through-pr <number> \
     --output logs/retrospective-evidence.json
   ```

3. run the source-neutral analyzer against that saved bundle:

   ```bash
   node infra/dev/delivery-retrospective.mjs \
     --input logs/retrospective-evidence.json \
     --json
   ```

4. require acquisition, evidence-contract validation, and analysis to complete; then use the analyzer's exact PR window, merged-PR count, trusted-author CHANGES REQUIRED disposition count, review-checkpoint distribution, and structured-finding dataset without manually reconstructing or retyping alternative totals.

The GitHub GraphQL adapter is the current local acquisition path. Another approved acquisition path must emit the same source-neutral contract and can then use the same analyzer unchanged. The executable contract and completeness rules live in `infra/dev/delivery-retrospective-evidence.mjs`; acquisition-specific retrieval rules live in `infra/dev/delivery-retrospective-github.mjs`; all retrospective counting and finding analysis live in the pure `infra/dev/delivery-retrospective.mjs` analyzer.

The adapter fails closed when GitHub search/review retrieval cannot prove completeness. The analyzer fails closed on unsupported or internally incomplete evidence. If either fails, the retrospective is `INCOMPLETE`; fix the retrieval/tooling problem rather than estimating the sample. Structurally complete evidence can still contain malformed or noncanonical historical reviewer findings. In that case the analyzer preserves the core window/review metrics, marks finding analytics incomplete, and reports the specific coverage diagnostics; do not treat any incomplete distribution as a complete sample.

Manual analysis begins only after the mechanical window is established. It should focus on:

- whether the previous retrospective's retained controls actually prevented recurrence;
- high-confidence post-merge process/lifecycle escapes;
- repeated waste that can be tied to a concrete mechanism;
- whether an existing experiment/change should be retained, retired, or superseded.

Do not optimize for finding count. Healthy adversarial review findings are not waste merely because they are numerous. Hand-classified finding totals or category percentages remain noncanonical. The analyzer may report mechanically parsed PR Reviewer fields — including severity, category, confidence, distinct findings, and per-PR incidence — when its structured-finding coverage is complete; when it is incomplete, use the diagnostics and do not present the observed values as complete distributions. Interpretation and improvement decisions remain retrospective reasoning, not analyzer output.

### Action ownership

There is no continuous-improvement intervention register.

Every retrospective conclusion must resolve to one of these forms:

- **retain** — existing control is working; no work item;
- **retire/supersede** — stop carrying the old experiment/control as active work;
- **action** — concrete work is required and must be owned by an existing or newly created GitHub issue before the retrospective is complete;
- **verify next time** — an already-landed change needs later measurement only; record the measure in the dated report, not as an unowned intervention.

If nobody is expected to change anything before the next measurement, it is not an active intervention.

### Dated report

Dated retrospective evidence lives under:

`docs/history/continuous-improvement/`

The normative method lives here; dated reports do not restate it. Keep each report concise (normally no more than about 80 lines) and use this structure:

```text
# Delivery-process retrospective — <date>

Scope: <baseline PR exclusive> -> <through PR inclusive>

## Result
<3-5 decision-oriented bullets>

## Mechanical metrics
<small comparison table using helper output>

## Prior changes
| Change | Verdict | Evidence | Action |

## Actions
- #<issue> — <required outcome>
or
- none

## Next baseline
- baseline PR
- specific measures to verify next time
```

Detailed review bodies, PR histories, issue evidence, and calculations remain in their owning GitHub/repository sources; do not duplicate them into the report unless necessary to support a decision.

### Completion

A retrospective is complete only when one reviewed PR:

1. adds the dated report under `docs/history/continuous-improvement/`;
2. advances `.github/continuous-improvement/retrospective.json` to the exact `throughPr`;
3. names every concrete follow-up issue in the report;
4. carries no unowned intervention/trial state.

The versioned state file records only the retrospective baseline and latest report path. It is not a trigger, due-state, reminder, or intervention register.

## Authority boundaries

| Concern | Owning authority |
| --- | --- |
| Session-close Kaizen | `AGENTS.md` |
| Continuous Improvement assessment | `.github/agents/continuous-improvement.agent.md` |
| Assessment reminder delivery | `.github/workflows/continuous-improvement-reminder.yml` |
| Consistency review algorithm | `.github/agents/consistency.agent.md` |
| Consistency corpus generation | `infra/dev/repo-snapshot.mjs` + `infra/dev/repomix.config.json` |
| Consistency operating guidance | `docs/development/consistency-review.md` |
| Consistency finding identity/state | GitHub `CONS:` issues |
| Retrospective method | this document |
| Retrospective baseline state | `.github/continuous-improvement/retrospective.json` |
| Retrospective evidence contract | `infra/dev/delivery-retrospective-evidence.mjs` |
| Retrospective GitHub acquisition | `infra/dev/delivery-retrospective-github.mjs` |
| Retrospective mechanical analysis | `infra/dev/delivery-retrospective.mjs` |
| Dated retrospective evidence | `docs/history/continuous-improvement/` |
| Retrospective actions | their owning GitHub issues |
| Raw delivery evidence | GitHub PR/review/CI/issue history |

PR review remains governed by `docs/development/reviewing.md`; it is evidence for retrospectives, not a fourth improvement ceremony.

## Invocation boundaries

Session-close Kaizen has no standing retrospective reminder. Consistency likewise has no standing due-state mechanism; recommend a fresh Consistency review from Kaizen only when evidence from the current session itself makes a repository-wide sweep materially useful.

Ordinary repository grounding and `.?` do not assess global improvement health. The Continuous Improvement agent is the explicit on-demand boundary for that question and may recommend a specialized practice without performing it. Delivery-process retrospective execution remains explicit and uses the versioned baseline plus the mechanical method above.

The scheduled checkpoint workflow is only an alarm clock for invoking that assessment. It does not establish that any practice is due.

## Non-goals

This system does not automatically execute a review/retrospective, infer practice due state, maintain retrospective due state, maintain a Consistency completion ledger, maintain an intervention database, turn historical reports into normative guidance, create work without issue ownership, or use GitHub as a slow substitute for repository content already established through the snapshot protocol.
