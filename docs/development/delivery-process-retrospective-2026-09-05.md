# Delivery-process retrospective — 2026-09-05

> **Status:** Dated, non-normative delivery-process evidence  
> **Scope:** Retrospective of 30 merged pull requests from #221 through #260  
> **Baseline:** Live `main` at `9e4cc84ac825d7714ef5f89a3007bf40b614a8f9` on 2026-09-05  
> **Purpose:** Preserve an empirical baseline for later verification of Arcogine's development standard work. This document does not override `AGENTS.md`, contributor/review guidance, architecture, ADRs, planning documents, tests, CI, or current GitHub state.

## Executive finding

Arcogine was already learning from delivery failures, but the sampled loop was still reviewer-heavy. Of 79 unique formal review findings, 39 (49.4%) were reconciliation failures: stale semantic neighbors, stale base/prerequisite assumptions, or stale PR descriptions after iterative remediation. These were not primarily novel product defects.

The highest-leverage improvement was therefore not more review-policy prose. It was to move recurring lifecycle and reconciliation problems toward repository-owned executable controls, while preserving independent review for subtle product and architecture defects.

The sample also showed that zero review findings is not a useful objective. Several of the most valuable findings were adversarial semantic defects that independent review was the right place to discover. The process question is narrower: **could a recurring class of failure economically have been prevented or detected earlier?**

## Sample and method

Audit date: **2026-09-05**.

Canonical landed baseline at audit time: `9e4cc84ac825d7714ef5f89a3007bf40b614a8f9`.

The local checkout used during the audit was two commits behind live `main`; live GitHub state therefore controlled the analysis.

Sample: all 30 merged PRs from #221, merged 2026-09-01 17:54 UTC, through #260, merged 2026-09-04 02:09 UTC. No PRs in that interval were excluded. #257 was retained because it contained direct post-merge lifecycle-escape evidence from #255.

Evidence included:

- current repository contracts and maintained development guidance;
- PR bodies and net diffs;
- submitted reviews and formal dispositions;
- remediation commits and head revisions;
- CI/check state and merge state;
- consistency-review evidence from issues #204–#209 and #215;
- executable controls such as CI, ArchUnit, ADR immutability checks, link checking, change classifiers, and the PR watcher.

Limitations:

- the same GitHub identity appears on implementation and review activity, so organizational or human independence cannot be proven from GitHub identity alone;
- current-head checks were complete, but attribution of some superseded workflow runs relied partly on contemporaneous PR evidence;
- the audit did not have reliable model-cost or wall-clock data, so it does not support model-routing conclusions;
- the sample is dense: 30 merges over roughly 2.35 days;
- some branches in the sample were grounded before newer standard work landed, so immediate recurrence does not prove that newly introduced guidance is ineffective.

For this audit, a remediation round means a feedback checkpoint followed by a meaningful corrective change.

## Finding distribution

Across the 30 PRs there were **79 unique formal findings**:

- 17 P1;
- 62 P2;
- no P0, P3, or Nit findings in the classified sample.

Primary taxonomy:

| Finding class | Count | Share |
|---|---:|---:|
| Semantic propagation miss | 23 | 29.1% |
| Functional contract miss | 11 | 13.9% |
| Baseline reconciliation miss | 10 | 12.7% |
| PR description drift | 6 | 7.6% |
| Identity/provenance miss | 6 | 7.6% |
| Acceptance-evidence gap | 6 | 7.6% |
| Tooling/workflow friction | 6 | 7.6% |
| Specification gap | 4 | 5.1% |
| Validation gap | 2 | 2.5% |
| Scope/ownership-boundary miss | 2 | 2.5% |
| Compatibility miss | 2 | 2.5% |
| Determinism miss | 1 | 1.3% |

Useful aggregates:

- semantic propagation + baseline reconciliation + PR-description drift: **39/79 = 49.4%**;
- acceptance evidence + validation gaps: **8/79 = 10.1%**;
- core functional/identity/specification/scope/compatibility/determinism defects: **26/79 = 32.9%**;
- lifecycle/tooling findings: **6/79 = 7.6%**.

Formal review first detected all 79 classified findings. No classified semantic/product defect in this sample was first detected by CI. CI still provided important fail-closed enforcement and regression evidence once the relevant executable tests existed.

## Remediation-round distribution

| Rounds | PRs | Share |
|---|---:|---:|
| 0 | 4 | 13.3% |
| 1 | 15 | 50.0% |
| 2 | 3 | 10.0% |
| 3+ | 8 | 26.7% |

26 of 30 PRs (86.7%) received at least one substantive formal correction. That is not automatically a process failure: independent review exists to find meaningful defects before merge.

## Healthy review catches

Several findings are examples of review doing the right work rather than compensating for weak standard work:

- #245: lossy JSON-number handling and canonical catalogue binding;
- #239: comparator/order/fingerprint semantics;
- #241: dispatch deltas and observation-cursor semantics;
- #247: caller-asserted provenance and empty applicability;
- #231: long-overflow boundary behavior.

These defects were subtle, context-sensitive, or boundary-adversarial. The useful process response is to add regression evidence after discovery, not to add broad prose attempting to pre-enumerate every future semantic edge case.

## Avoidable recurring waste

The sample also showed repeated work that should move earlier or become executable:

### Reconciliation work

The largest cluster was reviewer discovery of stale repository state rather than new semantics:

- 23 semantic-propagation misses;
- 10 stale-main or prerequisite-baseline findings;
- 6 PR-description drift findings after remediation.

This means reviewers were repeatedly reconstructing authority, current planning state, semantic neighbors, or final PR truth that implementation/planning could often have reconciled earlier.

### Lifecycle escape

#255 was the clearest process escape. It merged despite a blocking disposition, and #257 immediately repaired the missing remediation. This demonstrated that prose stating "do not merge while blockers remain" was insufficient by itself.

### Workflow-test omission

#260 introduced repository-owned PR lifecycle tooling but initially left its test suite outside required CI. #259 repeated the pattern for the disposition evaluator before the suite was wired into the always-running validation path. The durable lesson is stronger than "remember to add the test": workflow tooling needs a reliable required validation path.

### Duplicate lifecycle semantics

#259 initially reimplemented blocker/disposition semantics independently of #260. That created inconsistent interpretations of blocker lifetime and disposition handling. The durable design lesson is not necessarily "one giant lifecycle resolver"; it is **one authority per semantic question, reused by every consumer of that question**.

## High-confidence post-merge escapes

Four cases were treated as high-confidence escapes:

1. #233 claimed/landed RunId work while #238 later found maintained overview prose still describing RunId as outstanding.
2. #233 left busy ticks permanently zero; #241 corrected this during closure work.
3. #239 left D5/G2 planning state unreconciled; #242 repaired it.
4. #255 merged after a blocking review disposition; #257 repaired the missing final remediation.

A broader #233/#237 coherence issue discovered by #241 was not counted as a clear escape because the later slice was already intended to provide integration closure.

## Standard-work assessment at audit time

The audit classified the main recurring controls as follows:

| Practice/control | Assessment on 2026-09-05 |
|---|---|
| Semantic-neighbor / concept-fan-out review | Partially standardized |
| Live-main / prerequisite baseline grounding | Standardized, but too recent to evaluate |
| PR-description truth after remediation | Partially standardized |
| Acceptance criteria mapped to executable evidence | Partially standardized |
| ADR immutability enforcement | Already standardized and effective |
| Module ownership/dependency boundaries | Already standardized and effective |
| Environment-vs-product failure classification | Already standardized and effective |
| Merge blocked while findings/CI remain | Partially standardized; enforcement loop incomplete |
| Workflow-tool suites in required CI | Partially standardized |
| Rare adversarial product semantics | Should not be generalized into broad prose |

The clearest example of standard work already proving effective was ADR immutability. A mutable-header bypass found around #232 led to executable hardening, and the sampled window showed no recurrence of the same class.

## What had already been learned during the sample

The repository was already evolving in response to observed failures:

- consistency-review findings #204–#209/#215 led to stronger concept-fan-out and semantic-neighbor review guidance;
- issue-ledger lifecycle cleanup removed a prior custom-collision problem, with no recurrence observed in the sample;
- ADR mutability problems led to executable immutability checks;
- stale/proposed planning truth led to live-main grounding in the Work Planner and later base/head monitoring;
- #255's lifecycle escape led to #257 remediation, policy simplification, watcher work, and disposition-gate work;
- workflow tests began moving into the always-running CI path.

Because several of these changes landed during or immediately before the sample, recurrence inside the same dense window should not be read as proof that the new controls failed. They needed a later verification window.

## Highest-leverage improvements identified

The retrospective ranked these improvements highest:

1. **Executable PR lifecycle enforcement with clear semantic ownership.** Monitoring, review authorization, base freshness, CI, mergeability, and merge enforcement should not depend on an agent reconstructing mutable GitHub state ad hoc. Shared semantic questions should have shared repository-owned authorities.
2. **Required validation for repository workflow tooling.** Tests for lifecycle/evaluator/classifier/workflow helpers should not exist only as locally runnable scripts that CI can accidentally omit.
3. **Earlier closure of high-risk semantic-neighbor and acceptance-evidence state.** For authority/status-sensitive work, planning/implementation should identify the controlling authority, important semantic neighbors, and the evidence that demonstrates acceptance; independent review still verifies rather than trusting that handoff.

Two experiments were considered worthwhile rather than immediately becoming permanent standard work:

- trial a small closure-set handoff on the next architecture/status-sensitive slices and watch only for recurrence of propagation/base findings;
- generate/reconcile a final PR closeout summary after substantial remediation and retain the practice only if PR-description drift decreases without creating another approval ritual.

A machine-checkable status-sync mechanism was considered appropriate only for exact structured markers with repeated failure evidence, not for generic natural-language semantic linting.

## Improvements deliberately not recommended

The sample did **not** support adding:

- a separate Kaizen agent;
- a mandatory retrospective for every PR;
- a process database;
- a larger finding taxonomy for its own sake;
- review-round targets or a zero-findings objective;
- generic semantic-neighbor linting;
- mandatory Five Whys forms;
- one issue per review finding;
- per-session metrics or vanity KPIs.

These would increase process surface without evidence that they attack the main recurring failure modes.

## Waste interpretation

Using continuous-improvement terminology only as an analytical lens:

- **Muda:** 39/79 reconciliation findings, the immediate #255 → #257 repair, duplicate lifecycle semantics in #259, and workflow-tool tests omitted from required CI.
- **Mura:** large variation in remediation depth (4 zero-round PRs versus 8 with 3+ rounds), uneven semantic-closure quality, and parallel branches grounded against different baselines.
- **Muri:** reviewers repeatedly reconstructing authority/status truth that planning or executable tooling could expose earlier, especially in high-interaction PRs such as #252, #237, and #260.

No evidence in the audit supported a model-strength, model-cost, or agent-assignment recommendation.

## Improvement loop

The process model emerging from the evidence was:

```text
Plan
  intended slice, controlling authority, acceptance evidence,
  important semantic-neighbor closure

Do
  narrow implementation plus focused executable evidence

Check
  required CI plus independent review against current head/current main

Act
  when a defect is recurring/systemic or a severe process escape,
  add the smallest durable control at the lowest effective layer

Verify
  observe later applicable slices and retain, adjust, or remove the new control
  based on recurrence and burden
```

The final **Verify** step is essential. Without it, "continuous improvement" degenerates into accumulation of unverified process rules.

A useful control-strength ordering is:

1. impossible by design;
2. automatically checked;
3. tool-supported workflow;
4. explicit procedure;
5. human reminder.

Use the strongest economically justified layer that fits the recurring defect. Do not turn a single unusual review finding into repository-wide policy by default.

## Verification baseline for the next retrospective

When the next delivery-process retrospective runs, compare at least these values against this baseline:

- reconciliation findings: **39/79 (49.4%)**;
- baseline-reconciliation findings: **10/79 (12.7%)**;
- lifecycle/tooling findings: **6/79 (7.6%)**;
- high-confidence post-merge escapes: **4**;
- remediation-round distribution: **4 zero-round / 15 one-round / 3 two-round / 8 three-plus-round PRs**.

The intent is not to force every number downward. In particular, subtle product-semantic findings may remain healthy evidence that independent review is working. The key question is whether recurring reconciliation and lifecycle waste moves earlier or becomes mechanically prevented.

### Suggested rerun trigger

Run another formal delivery-process retrospective after **about 25 additional substantive merges**, or earlier if either condition occurs:

- **2 high-confidence post-merge process escapes**, or
- **1 P1 lifecycle/process escape**.

This is a trigger for re-evaluation, not a recurring calendar ceremony.

## Interpretation rule

This document is evidence, not standard work. Current repository contracts always take precedence.

When a later control appears to have solved one of the failure classes above, verify it against new delivery evidence before declaring it effective. When a control adds burden without reducing recurrence or severity, simplify or remove it rather than preserving it because it once sounded prudent.
