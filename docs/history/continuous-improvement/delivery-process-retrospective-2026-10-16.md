# Delivery-process retrospective — 2026-10-16

Scope: 368 -> 504

> Dated, non-normative evidence. Window: PR #368 merged 2026-09-20 22:28:50 UTC (exclusive) through PR #504 merged 2026-10-16 12:42:35 UTC (inclusive). Acquisition, analysis and branch inventory ran on `main` at `3fcc4517885464832a34123ccfec96afcc7cefc1`. Current repository contracts take precedence over this report.

## Result

- **Review churn fell; post-merge escapes did not.** Blocking-review checkpoints per merged PR fell from 1.31 to 0.72, and the three-plus tail fell from 9 PRs to 2 (the earlier figures are hand-counted, so read the change as direction only). No PR merged after a blocking disposition. Still, window PRs produced seven high-confidence post-merge escapes, against at least five last time, and Consistency review, not PR review, found six of them.
- **Retire or supersede three controls:** the unenforced Code Owner path (#389), the #353 PR-description model (replaced by #410's intent-only PR body), and connector-based acquisition (#403, replaced by the local adapter in #492). **Retain** the transient-coordinate guard and required tooling CI.
- **The largest waste was premise rework in the Factory/Engine chain.** #408 removed the `:wip` identifiers two days after #405 added them, and #494 deleted the unreachable spatial substrate two days after #474/#475 merged it. Stacked merges #498 and #500 also stranded work off `main` until #501 replayed it. The controls aimed at this (#487, #490, #502) landed in the last two days of the window, so they are *verify next time*.
- **Structured finding analytics are incomplete**, so finding-share comparisons with 2026-09-21 are not possible for this window.
- **No new action issues.** Every in-window escape is either fixed or owned by its Consistency issue.

## Mechanical metrics

These are analyzer values over the exact window. The 2026-09-21 column was hand-counted under the older method and is directional only.

| Measure | 2026-09-21 (#260 → #368) | This window (#368 → #504) |
| --- | ---: | ---: |
| Merged `main` PRs | 81 | 97 |
| Blocking-review checkpoints (`CHANGES REQUIRED`) | 106 | 70 (trusted authors) |
| Checkpoints per merged PR | 1.31 | 0.72 |
| PRs with 0 / 1 / 2 / 3+ checkpoints | 26 / 31 / 15 / 9 | 49 / 30 / 16 / 2 |
| PRs with no blocking checkpoint | 32.1% | 50.5% |
| High-confidence post-merge escapes from window PRs (interpretation) | ≥ 5 | 7, plus 2 medium-confidence |

- **Structured findings are incomplete.** The analyzer parsed 58 distinct findings in 35 PRs from 172 trusted reviews. 23 of the 70 blocking reviews have no parseable finding. All of them are in #372–#411, before the canonical format.
  - After the canonical format, finding blocks still broke in six ways:
    - changed titles or subjects (#432, #435, #490);
    - a non-canonical heading (#466);
    - a missing status (#411);
    - an unknown category, `TEST_COVERAGE` (#499).
  - These observed values are not a distribution. The hand-classified 126-finding baseline is not comparable.
- **Research-to-delivery lead time.** This is the first measurement, so there is no baseline:
  - issue opening to closing merge: median 2.48 days (n = 28);
  - open-issue age at #504's merge: median 2.69 days (n = 6);
  - handoff commit to merge: 0.15 days (n = 1, #504).

  The `Implementation handoff:` line was defined only at the end of the window (#487, #503). The n = 1 reflects how new the method is, not delivery speed.

## Prior changes

| Change | Verdict | Evidence | Action |
| --- | --- | --- | --- |
| Code Owner protection (#313) | retire/supersede | #389 removed the unenforced Code Owner path and reconciled the live ruleset. All 91 non-Dependabot PRs ended on a trusted `READY TO MERGE` before merge. | none |
| Transient-coordinate guard (#322) | retain | Landed in #392; #424 added the related product-source authority check. No in-window Consistency finding fell in this class. | none |
| Required CI for repository tooling | retain | Tooling changes (#398, #399, #401, #403, #491, #492, #503, #504) landed with suites in the required tooling job. No post-merge helper escape was found. | none |
| Stable PR description (#353) | retire/supersede | #410 limited PR bodies to intent and moved validation to the handoff. 0 of 60 later non-Dependabot PR bodies carry a Validation section, against 28 of 31 before. Stale PR prose is no longer a finding by definition, so part of any fall in such findings is definitional. | verify next time |
| Semantic closure before review (#359, #400) | not verified; retain | Propagation escapes: #405/#408→#416, #437→#438, #469/#471→#476, #405→#477, #384→#478; medium-confidence: #465→#479, #473→#480. Every origin except #384 merged after #400. Origins had 0 to 5 blocking reviews. Four of the seven stale claims were in files the origin PR itself edited; #405 even wrote raw control characters into the docs index. Detection took 0–20 days. | verify next time |
| Canonical finding format (#410, #433) | verify next time | Adopted mid-window. The post-adoption slips listed above still break completeness. | verify next time |
| Implemented-only architecture, planning retirement, Implementer handoff (#487, #490, #494, #497, #502) | verify next time | Landed 2026-10-14/15, after the rework they target. | verify next time |
| Checkpoint publication, branch inventory, lead time, shared window (#491, #492, #503, #504) | verify next time | First exercised by this retrospective. | verify next time |

The other two high-confidence escapes:

- #411→#412, a P1 Storage encoding/provenance conflict. It was filed within the hour and fixed the same day by #427.
- The stacked-merge stranding, #498/#500 → #501, which was repaired the same day.

## Research delivery

The inventory covered 110 branch identities: 25 live refs and 85 absent ones. Every linked PR head was fetchable. Seven research trails resolved at exact commits (#395, #402, #405, #443, #466, #471, #472), plus one audit branch with no PR.

- **Factory/Engine semantics and spatial transfer (#394–#408, #474/#475, #494).**
  - #395's reviewed reconciliation, accepted with qualifications, closed unmerged after #394 falsified its premise. #396 kept it as superseded evidence. This was productive falsification, caught before landing.
  - #402 went from REOPEN to an accepted revision within hours.
  - #405's investigation drew REOPEN twice, and its PR body records the reset as an owner decision.
  - The later rework followed premises that were withdrawn: the `:wip` labels (#405 → #408) and the unreachable spatial substrate (#474/#475 → #494). #488 closed as not planned.
- **Game strategy space and diagnostic evidence (#443, #472).**
  - Protocols and oracles were committed before their runs, and #443 registered its pass-2 revision before running pass 2.
  - #472's owner walkthroughs recorded busy-counter findings ahead of the owner's #469 removal.
  - Both were accepted with qualifications. #472 re-verified its transfer on current `main`, but #490 re-homed its target, Game planning, three days later.
- **Simulation analytics ownership and execution account (#435, #451, #454, #461, #466, #471).** The Phase 2 REOPEN was resolved by re-anchoring on the accepted game demand and execution evidence, without repeating the work. Reviewers ran their own reconstructions and probes. The coupled delivery still left one escape (#476).
- **Architecture implementation audit (no PR, 2026-10-14).** Its gaps match the subjects of the next day's PRs (#493, #494, #496, #497, #499, #501, #502). Its only copy is the retained branch.
- **Value of branch history: modest but real.** Only the branches show the pre-registration order and hold the audit. Merged PR bodies already summarize dispositions and supersession (#396, #405), and nothing contradicted merged results. Independent review was useful: three of the seven trails were reopened at least once.
- **Limits.**
  - Research merged in #387, #394, #431, #437, #447, #451, #461 and #462 left no workspace history on its PR branch.
  - Deleted branches without a PR cannot be enumerated.
  - Retained branches are not a random sample.

## Actions

- none. Every in-window escape is fixed or owned by its Consistency issue (#478, #480). The stacked-PR safeguard is the owner's in-flight #505, opened after the window, so it is subsequent context. The other conclusions are measurements for next time.

## Next baseline

- Baseline PR: #504.
- Read the zero-checkpoint share (50.5%) and the three-plus tail (2 of 97) together with attributable post-merge escapes (7 high, 2 medium). None of these is a target.
- Count attributable propagation escapes. Record whether each stale claim was inside the origin diff, and how long detection took.
- Check whether `findingAnalytics` is complete. If post-adoption format slips break it again, consider a reviewer-side format check.
- Count merged product code later removed as unreachable, and merges into stacked bases that never reached `main`.
- Compare research-to-delivery lead time with this first measurement. The handoff-to-merge measure needs n > 1.
- Confirm that every named handoff commit still resolves at acquisition. #504's handoff commit is reachable only from its handoff branch.
