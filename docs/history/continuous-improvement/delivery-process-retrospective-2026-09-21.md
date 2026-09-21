# Delivery-process retrospective — 2026-09-21

> **Status:** Dated, non-normative delivery-process evidence  
> **Scope:** All 81 pull requests merged after baseline PR #260 through the current main head produced by PR #368, ordered by merge time rather than PR number  
> **Baseline:** Live main at f4e1b5b259889fcff53300411c8894a1ed43120a on 2026-09-21  
> **Purpose:** Verify the 2026-09-05 delivery-process interventions against later delivery evidence and establish the next empirical baseline. This document does not override AGENTS.md, contributor/review guidance, canonical architecture and specifications, planning, tests, CI, or current GitHub state.

## Executive finding

The delivery loop improved in one important dimension: ordinary work reaches a non-blocking review state with less churn, and the specific stale-base failure class is a smaller share of formal findings. The exact #255 failure pattern — merging an ordinary PR after a canonical blocking disposition without a later READY TO MERGE review — did not recur in the audited review history.

The improvement is not complete. PR-description and justification-prose defects became a larger share of findings, semantic/status propagation still escaped into main, and a live merge-protection assumption turned out not to be enforced. The period also spent substantial review effort stabilizing the delivery system itself. Six process-heavy PRs (#259, #274, #289, #294, #279, and #335) account for 36 of 106 formal CHANGES REQUIRED review submissions.

The strongest result from the 2026-09-05 interventions is therefore selective:

- required validation for repository-owned delivery tooling is working as a pre-merge control;
- base reconciliation is better, but the lifecycle system is not yet fully verified because Code Owner enforcement is not observed to block protected changes;
- semantic-neighbor closure remains mixed;
- the closure-set and final-closeout-summary trials never acquired durable evidence that they were actually exercised and should not remain zombie experiments;
- independent review remains valuable, especially for authority, trust-boundary, evidence-custody, and justification-prose defects.

## Sample and method

Audit date: **2026-09-21**.

Canonical landed baseline at audit time: f4e1b5b259889fcff53300411c8894a1ed43120a.

Previous retrospective baseline: PR #260, merged 2026-09-04 02:09 UTC, merge commit 9e4cc84ac825d7714ef5f89a3007bf40b614a8f9.

The retrospective data file still named #260 / 2026-09-05 as the baseline when this audit began. Live history contains **81 merged PRs after #260**. Because merge chronology is authoritative for this window, lower-numbered PRs that merged later, including #259, #256, and #254, are included. The raw count is so far above the approximate 25-substantive-merge trigger that no borderline classification of small documentation or routine dependency updates can change the trigger result.

Evidence included:

- current repository process contracts and the 2026-09-05 retrospective;
- all merged PRs after #260 through current main;
- formal review submissions and canonical dispositions;
- current issue #295 intervention state;
- issue #313 on observed Code Owner enforcement;
- issue #322 on temporary-workspace commit coordinates leaking into maintained docs;
- issue #323 as explicit retrospective input on justification-prose findings;
- current Consistency findings, using only those whose contradictory transition can be tied confidently to this delivery window;
- current main and the exact files changed by the relevant transition PRs.

For reproducibility, this audit counted **126 unique blocking findings**: 121 numbered P0/P1/P2 review findings plus five earlier-format unnumbered blockers in #332, #333, #334, and #335. Illustrative strings that merely matched the review-finding token shape inside finding prose were not counted as findings.

A formal **CHANGES REQUIRED** review submission is used below as a review-round proxy. This is slightly more mechanical than the 2026-09-05 definition of a remediation round as a feedback checkpoint followed by a meaningful corrective change: current Arcogine review can correct PR metadata without changing the Git head, and a repeated re-review can occasionally restate a still-open finding. The zero-round result is exact; the higher-round buckets should be read as a comparable churn indicator, not precise developer-effort accounting.

Limitations:

- the delivery mix changed substantially: this window intentionally contains many delivery-system, research-process, repository-snapshot, and architecture-governance changes, so a raw tooling-finding rate is not directly comparable to a product-heavy window;
- Dependabot PRs can use the repository's trusted provenance path without a positive reviewer disposition, so absence of a formal review on a genuine bot head is not counted as missing review;
- this audit does not have reliable wall-clock, model-cost, or human-effort data;
- current Consistency findings can expose older defects, so they are counted as post-merge escapes here only when the contradictory status transition is tied to a PR in this window;
- the review format evolved during the sample, which is why finding identity was normalized explicitly above.

## Outcome metrics

### Finding density

| Measure | 2026-09-05 baseline | 2026-09-21 window |
| --- | ---: | ---: |
| Merged PRs | 30 | 81 |
| Unique formal blocking findings | 79 | 126 |
| Findings per merged PR | 2.63 | 1.56 |

The lower density is directionally good, but it is not a target by itself. Several current-window findings are exactly the adversarial review work Arcogine should retain.

### Review-round proxy

| Blocking-review checkpoints | 2026-09-05 remediation-round baseline | 2026-09-21 CHANGES REQUIRED proxy |
| --- | ---: | ---: |
| 0 | 4 / 30 = 13.3% | 26 / 81 = 32.1% |
| 1 | 15 / 30 = 50.0% | 31 / 81 = 38.3% |
| 2 | 3 / 30 = 10.0% | 15 / 81 = 18.5% |
| 3+ | 8 / 30 = 26.7% | 9 / 81 = 11.1% |

The tail improved materially: the share of PRs with no blocking review checkpoint more than doubled, while the 3+ bucket fell by more than half.

### Baseline reconciliation

The directly comparable stale-main / base-reconciliation class produced **11 findings out of 126 (8.7%)**, versus **10 out of 79 (12.7%)** in the previous retrospective.

The absolute count increased only from 10 to 11 while the audited window grew from 30 to 81 PRs. This is evidence that the live-main/base-normalization standard work reduced that specific waste class.

### PR-description and justification drift

A narrow current-window classification identified **17 findings out of 126 (13.5%)** where the material defect was stale or over-strong PR validation, rationale, title, or justification prose. The 2026-09-05 baseline was **6 out of 79 (7.6%)**.

Issue #323 provides a concentrated example: across #316, #317, and #319, review value was entirely in justification prose rather than the diffs, and several findings were claims stronger than the available evidence. Those were healthy pre-merge catches, but they show the waste class did not improve.

PR #353's later stable-description rule — keep semantic scope/rationale/non-goals/validation stable and resolve mutable topology live — is a better targeted response than adding a mandatory final closeout-summary ceremony. It landed only on 2026-09-18 and is too recent for this window to verify.

### Lifecycle and tooling

A simple current-window lifecycle/tooling finding percentage would be misleading because the repository deliberately spent a large part of this interval rebuilding its delivery machinery. The more useful verification is whether the prior failure modes escaped after merge.

Two results matter:

1. **No documented recurrence of the #255 canonical-disposition escape.** In the audited formal review history, every ordinary PR that received CHANGES REQUIRED later received a READY TO MERGE review before merge; genuine Dependabot heads use the separately defined trusted provenance path.
2. **No high-confidence post-merge escape from a newly introduced repository helper being omitted from required CI was found.** Review still caught missing or insufficient validation before merge — notably the disposition evaluator and snapshot tooling — but those omissions were remediated before landing.

That means the “required validation for repository workflow tooling” intervention is doing its job as a fail-closed control, even though delivery-tool changes still consume substantial review effort.

## High-confidence post-merge escapes

This audit identified **at least five** high-confidence escapes attributable to this window:

1. **PR #312 / issue #313 — Code Owner protection was assumed but not observed to enforce independent approval.** PR #312 changed protected .github surfaces and merged without a native APPROVED review even though the ruleset reports Code Owner review enabled. Current review guidance now explicitly states that this activation condition is not satisfied. Under the current severity model, this is a **P1-level lifecycle/security-process escape**: an intended merge-protection invariant was not actually enforced.
2. **PR #314 / issue #322 — maintained synthesis-seed state cited workspace-only commit coordinates.** The coordinates were not reachable from main and survived review/CI unnoticed.
3. **PR #316 / issue #322 — a concluded maintained investigation repeated the same workspace-coordinate defect.** PR #317 removed that instance; #319/#321 corrected the prose rule. The repeat supplies the evidence needed to consider an executable guard rather than another reminder.
4. **PR #341 / consistency issue #356 — a completed Engine-semantics pinning transition left the owning plan internally saying the work was still “ready to pin.”** PR #341 changed the Engine readiness plan itself, so this is a direct status-propagation miss from this window.
5. **PR #347 / consistency issue #362 — Governance evidence admission advanced to READY_NEXT while docs/README.md still published the old blocked state.** #347 changed the Governance plan/research/architecture surfaces but not the top-level docs index, making this another direct propagation escape.

Other current Consistency findings were not automatically counted as escapes here because their originating transition is not as cleanly attributable to this window.

The escape trigger therefore fired independently of the raw merge-count trigger.

## Healthy review catches

Several current-window findings are evidence that independent review is doing the right work rather than compensating for a missing generic checklist:

- #281 caught an incomplete Vitest major migration that omitted the matching coverage provider.
- #305 prevented retirement of a research workspace while unique pre-registered playtest protocol evidence would have been lost.
- #314 caught an authority/rationale statement that reintroduced entitlement semantics rejected by the reviewed evidence.
- #316 caught an unsupported semantic narrowing of ChangeProvenance.source and a false “homeless” claim about an already-planned Engine gap.
- #333 and #334 found fail-closed correctness gaps in connector-side merge-tree reconstruction around truncated Git trees and Git merge attributes/drivers.
- #335 found that stale-PR normalization could bind the wrong base/committer authority.
- #361 and #370 found repository-authority / security-authority problems that ordinary tests could not establish.

These are not reasons to weaken review. They are reasons to keep adversarial review focused on the contracts that are hard to make mechanical.

## Verification of 2026-09-05 interventions

| Intervention | Result | Evidence / decision |
| --- | --- | --- |
| Executable PR lifecycle enforcement | **PARTIALLY VERIFIED** | Base-reconciliation share fell from 12.7% to 8.7%, and the #255 blocking-disposition merge pattern did not recur. Issue #313 proves the protection stack is still incomplete because Code Owner enforcement is not observed to block protected changes. Retain the lifecycle system; close the live enforcement gap rather than adding another parallel gate. |
| Required validation for repository workflow tooling | **VERIFIED — retain** | No post-merge untested-helper escape was found. Missing/insufficient helper validation continued to be caught pre-merge and repaired. |
| Earlier semantic-neighbor / acceptance-evidence closure | **NOT YET VERIFIED** | Direct status-propagation escapes #356 and #362 reached main, and review still found multiple authority/status-neighbor omissions before merge. Current Consistency breadth improvements landed late in the sample, so keep the underlying practice but do not declare it solved. |
| Trial: small closure-set handoff | **INCONCLUSIVE — retire the trial** | Repository and PR search finds the experiment as retrospective/register history, but no durable marker identifies which slices actually exercised it. An uninstrumented experiment cannot support a causal conclusion. Existing concept-fan-out/review and Consistency breadth rules already own the durable behavior. |
| Trial: final PR closeout summary | **NOT VERIFIED — superseded** | PR-description/justification drift rose to 13.5%. Do not add a final approval ritual. PR #353's newer stable-description rule is the narrower replacement and should be verified in the next window. |

## Avoidable recurring waste

### Delivery-system churn is concentrated

The overall review-round tail improved, but process-heavy work remains expensive. Six PRs account for **36 / 106 = 34.0%** of all formal CHANGES REQUIRED submissions in the window:

- #259 — disposition merge gate;
- #274 — durable delivery-label enforcement;
- #289 — developer-tooling contracts;
- #294 — continuous-improvement operating model;
- #279 — PR base reconciliation;
- #335 — stale-PR normalization redesign.

This is partly expected transition cost: the repository was converting process rules into executable infrastructure. The lesson is not to stop improving tooling; it is to avoid repeatedly redesigning the same semantic question through overlapping mechanisms. Prefer one owned lifecycle authority and prove it end-to-end before layering another control.

### Justification prose remains a review hotspot

The old “final closeout summary” experiment did not earn retention. The stronger signal is that mutable topology and over-claimed evidence in PR bodies create churn. The stable-description rule from #353 directly attacks that failure mode without another ceremony.

### Semantic propagation still needs closure

The #341 → #356 and #347 → #362 escapes show that a status transition can still update its owning plan while leaving a neighboring maintained claim stale. Current review/Consistency breadth rules already specify propagation and candidate closure. The next verification should test whether the late-window strengthening in #359 reduces recurrence before adding more process.

## Improvements to retain or pursue

1. **Repair and verify the existing Code Owner protection path (#313).** Treat this as the highest-priority process-control follow-up because it is an observed protection gap, not a hypothetical improvement. Verify the repaired rule with a controlled live PR and keep the evidence in repository/GitHub history.
2. **Implement the evidence-coordinate guard only through the existing #322 follow-up if its cost is acceptable.** Two independent maintained-doc escapes justify moving this rule toward an executable check. The guard must preserve legitimate reachable baseline/provenance SHAs and account deliberately for clone depth and same-PR citations.
3. **Retain required CI coverage for repository-owned tooling.** This intervention is verified; do not reopen it as a process experiment unless a new escape contradicts the evidence.
4. **Use the stable PR-description model from #353 and verify it next time.** Do not revive the closeout-summary trial.
5. **Retire the closure-set-handoff experiment as a separate intervention.** The durable semantic-neighbor/candidate-closure behavior already lives in review and Consistency standard work; maintaining an unobservable duplicate experiment adds no signal.
6. **Do not turn #327's no-op commit incident into a new top-level process layer yet.** It is real waste because exact-head evidence was invalidated without a tree change, but one observed instance is better handled by its existing follow-up than by a new repository-wide ceremony.

## Improvements deliberately not recommended

This window does not support adding:

- another merge-authorization gate beside the existing disposition / GitHub protection stack;
- a mandatory final closeout approval step;
- a new closure-set artifact or checklist;
- a target number of review findings;
- a target of zero CHANGES REQUIRED reviews;
- generic natural-language semantic linting;
- a new process database or retrospective issue ledger;
- model-routing or staffing rules without cost/time evidence.

## Waste interpretation

Using continuous-improvement terminology only as an analytical lens:

- **Muda:** stale/over-strong PR descriptions, repeated review of mutable topology, the #314/#316 evidence-coordinate escape and repair chain, no-op head movement from #327, and duplicated delivery-system semantics during the lifecycle-tooling transition.
- **Mura:** review effort remains highly concentrated — a small number of process PRs generate a large share of blocking rounds while many ordinary PRs now pass without a blocking round.
- **Muri:** reviewers still carry too much burden for proving that delivery infrastructure really enforces its own security/lifecycle assumptions, especially where GitHub platform behavior rather than repository code is the authority.

## Next verification baseline

At the next delivery-process retrospective, compare at least:

- unique formal blocking findings: **126 across 81 merged PRs (1.56 per PR)**;
- baseline-reconciliation findings: **11 / 126 = 8.7%**;
- PR-description / justification findings: **17 / 126 = 13.5%**;
- high-confidence post-merge escapes attributable to the window: **at least 5**;
- no-blocking-review PRs: **26 / 81 = 32.1%**;
- formal CHANGES REQUIRED proxy distribution: **26 zero / 31 one / 15 two / 9 three-plus**;
- delivery-system concentration: **36 / 106 blocking review submissions in the six highest-churn process PRs**;
- repository-tooling validation escape: **none observed post-merge**;
- lifecycle exact-pattern check: **no observed repeat of #255's merge-after-blocking-disposition pattern**.

For the next run, use PR #368 as the merge-history baseline because it is the latest PR merged into the audited main head, even though PR #370 has a higher number and merged slightly earlier.

Run the next formal retrospective after about **25 additional substantive merges after PR #368**, or earlier after **2 high-confidence post-merge process escapes** or **1 P1 lifecycle/process escape**.

## Interpretation rule

This document is evidence, not standard work. Current repository contracts always take precedence.

A control is retained because later evidence shows it prevents or moves a recurring defect earlier, not because a prior retrospective recommended it. A trial with no durable exposure evidence is not “still awaiting verification” forever: retire it or replace it with an observable, narrower mechanism.
