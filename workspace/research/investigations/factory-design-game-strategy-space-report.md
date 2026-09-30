# Factory-design game non-spatial strategy space — research report

## Header / authority

- **Question:** [Factory-design game strategy space](../../../docs/research/investigations/factory-design-game-strategy-space.md)
- **Research status:** `ACTIVE`. A report exists; the question becomes `CONCLUDED` only when its durable
  consequence is reconciled or an explicit no-action result is recorded.
- **Research baseline:** live `main` `3479053f304e43ec57d7a5fd69799ae09b84b7fd` (equal to the handoff
  baseline at research start).
- **Final repository recheck:** live `main` `9fc890ac7895d67f890c82fa5130720824b62536`. The only change is
  the analytics-research split: analytics research briefs, register rows, two planning
  sentences and a `RuntimeObservation` Javadoc sentence. No Engine rule, Factory model, Challenge
  code or research-substrate class changed. The change is **not material** to this conclusion; its only
  effect is that the analytics ownership question this report points to is now the split, READY
  [analytics ownership boundary](../../../docs/research/investigations/simulation-analytics-consumer-boundary.md).
  The workspace was not synchronized with the newer `main`. The change is merged pull request
  alaiba/arcogine#435.
- **Authority:** research evidence only. Nothing here is accepted product direction, architecture,
  Challenge requirement, or implementation commitment until a separate reconciliation promotes it.
- **Risk classification (`docs/development/researching.md` §7):** **medium**. The result shapes a
  consumer's product requirements and how that consumer relies on a normative Engine tie-break; it
  decides no identity, persistence, determinism, compatibility or ownership contract. This is distinct
  from the register's **High portfolio priority**.
- **Adversarial-review status:** `not yet required` under §7 for medium risk. This revision carries a
  self-administered adversarial analysis only. An independent review is **recommended** before the
  requirement set is promoted, because the positive result depends on operational definitions the
  researcher introduced (see *Adversarial analysis*).

## Question

As bounded by the brief: can a deliberately small, fixed **non-spatial** production challenge produce
several materially different, explainable viable designs using only current production/routing/
resource-capacity semantics, one fixed quantity-bearing requirement, a bounded game-owned catalogue and
construction cost, and a game-owned budget and completion target?

**Sharpening (stated before any run, in the pass-1 protocol §9).** Two designs on the (cost, completion)
Pareto frontier never dominate each other by construction, and the brief's constraint-capacity,
irrelevant-capacity, migration and capital-pressure cases can all hold on a frontier that is just "the
same line plus more capacity at the bottleneck" — which is the brief's null. To make the null and the
alternative mutually exclusive, the multiple-viable-structures case was operationalized as **strong
multiplicity**: at least two feasible frontier designs whose capacity provision vectors are not nested,
separated strictly in both dimensions, with a mechanical explanation from supported evidence. The looser
reading (any two frontier designs) is reported alongside. Pass 2 further separated strong-multiplicity
pairs whose incomparability rests on an effect below the protocol's own 5% materiality threshold
("fine-tuning") from pooling and material dedicated pairs, and required survival under a second
projection order.

## Decision at stake

Whether the first factory-design product kernel already has a meaningful capacity/capital design space
before spatial layout, or depends on unresolved semantics to become interesting. A positive result
promotes a bounded non-spatial challenge requirement set; a negative result is evidence against building
a playable shell around current mechanics in anticipation that spatial features will rescue it.

## Scope and non-goals

In scope: one `CUT -> ASSEMBLE -> INSPECT` routing, one workload submitted once, current Factory
publication and non-spatial Engine semantics, a five-offer game-owned catalogue, game-owned cost rules,
budget and target, exhaustive deterministic enumeration, one-variable interventions and constraint
migration. Out of scope and untouched: spatial layout and transfer, presentation, player comprehension,
score/rating formulas, level structure, Engine dispatch changes, manufacturing extra orders, and any
analytics architecture. No production source was modified.

## Executive conclusion

**Qualified positive, with a sharp boundary.** Current non-spatial semantics **do** produce a small,
explainable capacity/capital design problem with at least two — typically three — structurally different,
non-dominated viable designs, **but only when the catalogue includes an offer projecting to a resource
eligible for more than one routing step** (a "flex cell"). With dedicated single-step equipment only, the
kernel largely **collapses to monotone bottleneck purchase**, as the null predicts.

1. **Dedicated-only kernel — null largely holds.** Step duration belongs to the step, so "faster
   equipment" is inexpressible; `setupTime` and `capacityLiters` are inert; one 2-slot machine and two
   1-slot machines perform identically (0 of 13,680 controlled pairs differ). The frontier is a chain of
   bottleneck purchases. Non-nested frontier pairs occur in 37 of 576 cells. After classification only 19
   remain material, and all of them rest on 1–3-tick pipeline-alignment gains from extra non-bottleneck
   capacity, at or one tick over the 5% materiality threshold. 9 of 576 cells (1.6%) pass every proving
   case.
2. **Flex-capable kernel — alternative survives in a bounded region.** A flex cell creates non-nested
   frontier structures by **pooling** spare capacity of one step into another. Across the declared window,
   735 of 2,304 flexible-catalogue cells have order-robust material strong multiplicity (694 of them
   through pooling), and 271 (11.8%) pass constraint capacity, irrelevant capacity, migration, capital
   pressure and strong multiplicity under both projection orders. A positive reference family exists and
   is robust on every declared perturbation axis.
3. **Qualifications that must survive reconciliation.**
   - Flex-design outcomes depend on resource identity order: `MachineId` is the Engine's final
     selection tie-break, and reversing projection order changes completion for 10–33% of flex designs
     by up to 6 ticks. No flex-free design is order-sensitive. A challenge must therefore fix a
     deterministic projection order as challenge-owned content.
   - The pooling trade-off exists only within a price band: flex priced at roughly 1.0–1.5× the dearer
     dedicated machine it can substitute. At 2.0× it nearly vanishes (4 and 19 of 288 cells per cost rule).
   - It needs budget slack: none at 0% above the cheapest target-meeting cost, increasingly more at
     10/25/50%.
   - Cost-minimal target-meeting designs are frequently balanced (co-constrained). From such a base a
     one-unit capacity addition often yields nothing, so the constraint-capacity and migration cases are
     demonstrable only for targets whose cheapest feasible design has a single active constraint (positive
     cells concentrate at `f = 0.8` and `0.4`; `f = 0.6` has one).
   - The **pre-selected** reference cell (`P1`, `N = 12`) is monotone in both families; the positive
     reference family was located by the pre-registered window search, not by the first guess.

**Confidence:** high that the dedicated-only kernel collapses (exhaustive within the window, invariant
under both projection orders); **medium** for the flex-capable positive (constructive, replayed and robust
within the window, but conditional on one serial routing, the researcher's operational definitions, two
projection orders and a price band). Overall **medium**.

## Repository evidence

Repository facts at the research baseline:

- **Step-level duration.** `OperationStepDefinition.duration` is fixed per step; `FactoryHandler`
  schedules completion at `currentTime + step.duration()` for whichever resource is selected
  ([Factory Resource Semantics](../../../docs/architecture/factory-resource-semantics.md): "step
  duration also belongs to the step rather than the selected resource").
- **Inert resource fields.** `ConfiguredResource.setupTime` and `capacityLiters` are canonical,
  fingerprinted and observed, but no Engine decision path reads them.
- **Capacity levers.** Resource count, `concurrency` (`canAcceptJob` = online and active `<`
  concurrency) and each step's authored eligible set, which may contain one resource for several steps
  ([Factory model](../../../docs/architecture/factory-model.md) §2.1).
- **Dispatch.** Selection ranks immediate acceptance, then `combinedQueueDepth`, then `MachineId`;
  per-machine queues are FIFO; multi-eligible waiting uses a shared backlog; the recovery cascade serves a
  machine's own queue first ([Engine semantics](../../../docs/architecture/engine-semantics.md) §2, §4).
  Quantity `N` becomes `N` child jobs released at acceptance (§3). Present spatial content is refused.
- **Challenge layer.** Landed game-owned facts are a starting budget, a deadline, purchase costs and
  quantity limits; `EquipmentOffer` carries no capability and does not project to Factory facts. The
  landed reference evaluation policy is pass/fail with an additive score
  `1 + deadlineMarginTicks + unusedBudgetCredits`
  ([Challenge readiness](../../../docs/planning/factory-design-game-challenge-readiness.md)).
- **Research substrate.** `ExperimentFixture`, `ExperimentRunner`, `DeclaredEvidence`, `Oracle`,
  `WaitingWorkByStepOracle` and `StarterCorpus` run fixtures through supported control and evidence only.
  The `P1` durations reuse the `StarterCorpus` baseline profile.
- **Open research neighbors.** The READY [diagnostic evidence contract](../../../docs/research/investigations/factory-design-game-diagnostic-evidence.md),
  the analytics ownership boundary, and the CANDIDATE
  [qualified applicability and resource-dependent performance](../../../docs/research/investigations/factory-design-evolution.md)
  question. None is settled; none was relied on as fact.

Not inspected: no required surface was unavailable. The only other open pull request,
alaiba/arcogine#433 (review-finding wording), is unrelated.

## Candidate models / hypotheses

- **H0 — monotone collapse (brief's null).** The frontier is a nested chain of bottleneck purchases, or
  apparent multiplicity is fragile or price-arbitrary.
- **H1-D — dedicated capacity suffices.** Offers differing only in served step and concurrency already
  produce non-nested viable structures.
- **H1-F — shared eligibility suffices.** Adding an offer eligible for several steps produces
  non-nested, explainable viable structures.
- **H-artifact — multiplicity is an artifact** of projection order, of sub-threshold effects, or of a
  narrow price point.

A-priori expectations were committed before any run: E1, granularity is price-only; E2, Family D is
monotone everywhere; E3, Family F's direction is unknown.

## Method

Pass 1 was pre-registered before any run (`factory-design-game-strategy-space-protocol.md`), and the
harness's own operational choices were committed before its first run. Pass 2 was registered after
pass 1 was inspected and before pass 2 ran (`factory-design-game-strategy-space-protocol-pass-2.md`); it
changed no window value and only added controls that can make a positive harder to claim.

| Element | Value (game-owned research parameters unless noted) |
|---|---|
| Routing profiles | `P1` CUT 2 / ASSEMBLE 6 / INSPECT 3 (StarterCorpus); `P2` 3 / 4 / 5 |
| Quantity | `N ∈ {6, 12, 24}`, one order, submitted once |
| Catalogue | `CUTTER` {CUT} c1 ≤4; `ASSEMBLER` {ASSEMBLE} c1 ≤6; `TWIN_ASSEMBLER` {ASSEMBLE} c2 ≤3; `INSPECTOR` {INSPECT} c1 ≤4; `FLEX_CELL` {ASSEMBLE, INSPECT} c1 ≤3; `setupTime = 0`, `capacityLiters = null` |
| Families | D: no flex; F: all five offers |
| Cost rules | `UNIFORM` 100 per dedicated machine; `WORK` 50 × served-step duration; twin `δ × 2 × assembler`, `δ ∈ {0.75, 0.9, 1.0}`; flex `φ × max(assembler, inspector)`, `φ ∈ {1.0, 1.25, 1.5, 2.0}` |
| Target | `τ = T_floor + ⌊f × (T_starter − T_floor)⌋`, `f ∈ {0.2, 0.4, 0.6, 0.8}` |
| Budget | `B = ⌊C*(τ) × (1 + β)⌋`, `β ∈ {0, 0.1, 0.25, 0.5}`, `C*` = cheapest target-meeting cost |
| Dimensions | construction cost and completion tick only; Pareto dominance; no weighted score |
| Enumeration | all 2,112 projectable designs per (profile, N), published through `FactoryModelPublisher`, run by `ExperimentRunner`, complete event window |
| Cells | 2,880 per order (576 D, 2,304 F) |
| Projection orders | pass 1: C (offers in table order, flex last); pass 2 adds R (reverse, flex first) |

Research-local derivations, each reading only declared supported evidence: a **provision vector**
`(cut, assemble-only, inspect-only, flex)` slots; **step-pool occupancy**, where pools are connected
components of the step–resource eligibility graph and the active constraint is the pool with the maximum
occupied/capacity job-ticks from `JOB_DISPATCHED`/`JOB_STEP_COMPLETED` (a flex cell merges ASSEMBLE and
INSPECT into one pool; per-step attribution of shared capacity is refused); a **dispatch profile**
(dispatch counts per resource and step, mean wait per step); and the landed `WaitingWorkByStepOracle` at
`⌊T/2⌋`. Completion is the closing observation's `completedAt`, cross-checked against the single
`ORDER_COMPLETED` event.

Replay: each of these was executed twice and compared as a whole evidence bundle after run-identity
normalization, and none differed:

- every design key of the reference `(P1, N = 12)` context, with its probes and controls — 2,148 in
  pass 1, and 2,127 (order C) plus 2,132 (order R) in pass 2;
- all 24 candidate load-bearing runs (12 designs × 2 orders). The refactored harness reproduced every pass-1 output file
byte-for-byte.

## Proving cases

### Controls

| Control | Result |
|---|---|
| Granularity: one twin versus two assemblers, all else equal | 0 of 6,840 pairs differ under order C, 0 of 6,840 under R — E1 **confirmed**; twin versus singles is a price choice only |
| Projection order C versus R | 167–554 of 2,112 designs per (profile, N) change completion, **all** of them containing a flex cell, by at most 4–6 ticks; every flex-free design is order-invariant |

### Cell map

| | D (576) | F (2,304) |
|---|---:|---:|
| Pass 1: weak multiplicity (≥2 feasible frontier designs = capital-pressure trade-off) | 280 | 1,269 |
| Pass 1: capital pressure binding (fastest design over budget) | 550 | 2,302 |
| Pass 1: strong multiplicity (MVS-S) | 37 | 796 |
| Pass 1: all proving cases | 15 | 338 |
| Pass 2: MVS-S cells whose pairs are all FINE-TUNING (order C) | 18 | 16 |
| Pass 2: material MVS under C / under R | 19 / 19 | 780 / 771 |
| Pass 2: order-robust material MVS | 19 | 735 (694 via POOLING in both orders) |
| Pass 2: CC, IC, CM and CP under both orders | 111 | 422 |
| **Pass 2: all proving cases, both orders** | **9** | **271** |

Family F by flex price (order-robust material / pass-2 positive, of 288 cells each):

| φ | 1.0 | 1.25 | 1.5 | 2.0 |
|---|---|---|---|---|
| `UNIFORM` | 98 / 21 | 144 / 62 | 129 / 35 | 4 / 0 |
| `WORK` | 138 / 44 | 142 / 61 | 61 / 33 | 19 / 15 |

Family F by budget slack β = 0 / 0.1 / 0.25 / 0.5: order-robust material 0 / 111 / 246 / 378; positive
0 / 52 / 101 / 118. By target fraction f = 0.2 / 0.4 / 0.6 / 0.8: material 183 / 219 / 142 / 191;
positive 18 / 88 / 1 / 164.

### Pre-selected reference cell — null signature

`P1`, `N = 12`, `WORK`, twin δ 0.9, flex φ 1.25, `f = 0.6`, `β = 0.25`. Prices: `CUTTER` 100, `ASSEMBLER`
300, `TWIN_ASSEMBLER` 540, `INSPECTOR` 150, `FLEX_CELL` 375. Starter completion 77. Target 53 (D) / 52 (F),
budget 987.

The feasible frontier is identical in both families and both orders: `1 cutter, 1 twin, 1 inspector`
(790 credits, 44 ticks, co-constrained ASSEMBLE|INSPECT) followed by the same design plus one inspector
(940, 43, ASSEMBLE). The pair is nested: the only choice is whether to buy one tick for 150 credits.
From the 790 base, `+INSPECTOR` gains 1 tick and `+ASSEMBLER` gains 0, so constraint capacity fails from
a balanced base.

Global frontiers, order C (cost/ticks, provision `(cut, asm, insp, flex)`, active constraint):

- **D:** 550/77 (1,1,1,0) ASM · 790/44 (1,2,1,0) ASM|INSP · 940/43 (1,2,2,0) ASM · 1040/41 (2,2,2,0) ASM ·
  1240/33 (1,3,2,0) CUT|ASM · 1340/32 (2,3,2,0) ASM · 1490/31 (2,3,3,0) ASM · 1580/26 (2,4,2,0) ASM|INSP ·
  1880/25 (2,4,4,0) ASM · 2030/23 (2,5,3,0) ASM · 2270/22 (2,6,3,0) CUT|ASM|INSP · 2370/20 (3,6,3,0) ASM|INSP ·
  3120/19 (3,8,4,0) ASM|INSP · 3220/17 (4,8,4,0) ASM|INSP.
- **F:** the D frontier through 2370/20, interleaved with pooled designs that also displace D's two
  most expensive points: 475/110 (1,0,0,1) · 775/59 (1,1,0,1) · 1165/35 (1,2,1,1) · 1955/24 (2,4,2,1) ·
  2880/19 (3,5,3,2) · 3205/17 (4,5,2,3) · 4945/16 (4,10,4,3). Under order R five of these complete 1–3 ticks
  later.

The starter design demonstrates the classic cases here. `+ASSEMBLER` gains 33 ticks (77 → 44). A
cost-matched away addition gains nothing (`+1` or `+3 CUTTER`, `+1` or `+2 INSPECTOR`). Relieving
ASSEMBLE makes INSPECT co-limiting. The 43-tick base `(1,2,2,0)` behaves the same way: `+ASSEMBLER` gains
10; `+CUTTER` gains 2, which is materially smaller at 0.2 of the at-constraint gain; and one assembler
migrates the constraint to CUT|ASSEMBLE. For these three designs, waiting-work corroboration at `⌊T/2⌋`
puts most waiting work at the identified step. The candidate family below shows that this is not
guaranteed.

### Positive reference family (the pre-registered candidate)

Selected by the pre-registered rule (the positive cell with the most positive neighbours) in pass 1, and
still the pass-2 candidate.

- **Routing:** `P2` — CUT 3, ASSEMBLE 4, INSPECT 5 ticks. **Quantity:** 12, one order.
- **Catalogue and prices (`WORK`, δ 0.9, φ 1.25):** `CUTTER` 150, `ASSEMBLER` 200, `TWIN_ASSEMBLER` 360,
  `INSPECTOR` 250, `FLEX_CELL` 313; limits as in *Method*.
- **Target:** 57 ticks (`f = 0.8`; starter 67, floor 20). **Budget:** 1,062 credits (`β = 0.25` over
  `C*(57) = 850`).

**Feasible frontier:**

| Design | Provision | Cost | T (C) | T (R) | Active constraint (pool occupancy) |
|---|---|---:|---:|---:|---|
| **S1** 1 cutter, 1 assembler, 2 inspectors | (1,1,2,0) | 850 | 56 | 56 | ASSEMBLE 48/56 (INSPECT 60/112) |
| **S2** 1 cutter, 1 assembler, 1 inspector, 1 flex | (1,1,1,1) | 913 | 47 | 46 | CUT 36/47 = ASSEMBLE+INSPECT pool 108/141 (co-constraint) |
| S2′ 1 cutter, 1 assembler, 2 flex | (1,1,0,2) | 976 | 46 | 46 (dominated) | CUT 36/46 = pool 108/138 (co-constraint) |
| **S3** 1 cutter, 1 twin, 2 inspectors | (1,2,2,0) | 1010 | 45 | 45 | CUT 36/45 |

**Complete global frontier, order C:** 463/111 (1,0,0,1) · 600/67 (1,1,1,0) · 776/60 (1,0,0,2) · 850/56 ·
913/47 · 976/46 · 1010/45 · 1063/44 (2,1,1,1) · 1160/37 (2,2,2,0) · 1376/35 (2,1,1,2) · 1410/33 (2,2,3,0) ·
1473/32 (2,2,2,1) · 1599/31 (2,2,0,3) · 1610/30 (2,3,3,0) · 1723/29 (2,2,3,1) · 1760/27 (3,3,3,0) ·
2010/26 (3,3,4,0) · 2170/25 (3,4,4,0) · 2186/24 (3,2,3,2) · 2320/22 (4,4,4,0) · 2849/21 (4,3,3,3) ·
2946/20 (4,4,4,2).

**Order R** differs as follows: S2 completes at 46; six classes leave the frontier — 976/46 (S2′),
1599/31, 2170/25, 2186/24, 2849/21 and 2946/20; three join — 2123/25 (3,2,4,1), 2699/21 (3,3,3,3) and
3209/20 (4,5,3,3). That leaves 19 classes against 22 under C. Dedicated and pooled structures alternate
along the whole frontier under both orders.

**Proving cases (identical under both orders):**

- **Constraint capacity:** from the starter (600, 67, INSPECT), `+INSPECTOR` gains 11 ticks (to S1). From
  S1 (ASSEMBLE), `+ASSEMBLER` gains 11 ticks.
- **Irrelevant capacity:** from the starter, `+1` or `+2 CUTTER` and `+1 ASSEMBLER` gain 0. From S1,
  `+1 CUTTER` and `+1 INSPECTOR` (cost-matched) gain 0.
- **Constraint migration:** INSPECT → ASSEMBLE after one inspector, then ASSEMBLE → CUT after one
  assembler, by pool occupancy (the pre-registered constraint evidence). Waiting work at `⌊T/2⌋` does
  **not** always point at that constraint. At the starter, 3 jobs wait at ASSEMBLE and 1 at INSPECT,
  although INSPECT has the higher occupancy (60/67 vs 48/67), because a faster upstream step fills the
  queue ahead of a merely slower one. At S1, waiting sits at CUT (2) and ASSEMBLE (2); at S3, at CUT (4).
- **Capital pressure:** the fastest design (20 ticks) costs 2,946 (C) or 3,209 (R) against a 1,062
  budget. Inside the budget, every tick bought costs capital.
- **Multiple viable structures:** S1 vs S2 and S2 vs S3 are POOLING pairs in both orders, provision
  non-nested, and strictly separated (S1→S2: +63 credits for −9/−10 ticks; S2→S3: +97 credits for
  −2/−1 ticks).

**Mechanical explanation (dispatch profile, supported events only):**

- **S1:** the assembler performs all 12 assemblies; two inspectors split 6/6; mean assembly wait
  66/12 ticks, inspection wait 0. The second inspector is half idle.
- **S2:** the flex cell performs 4 assemblies and 5 inspections, the assembler 8 and the inspector 7; mean
  assembly wait falls to 3/12 (C) or 4/12 (R), inspection wait rises to 11/12. The flex cell converts S1's
  spare inspection capacity into assembly capacity for 63 credits.
- **S3:** relieves ASSEMBLE with dedicated twin capacity instead, which makes CUT the limit. It costs 97
  credits more than S2 and finishes 1–2 ticks sooner.

These are three different answers to one production question: how to relieve the assembly bottleneck —
don't, pool, or duplicate.

**Robustness (pre-registered neighbour rule; passes in pass 1 and pass 2):** on every axis — adjacent
target, budget, quantity, twin price, flex price, and the other cost rule — every neighbour keeps
order-robust material strong multiplicity, and none collapses to a single design. Every neighbour except
the adjacent target also passes all proving cases. At `f = 0.6` the cheapest feasible design is S2,
co-constrained CUT|pool, so migration cannot be shown from that base. The load-bearing pairs involve no
exact ties.

### Candidate models against the proving cases

| Case | H0 | H1-D | H1-F | H-artifact |
|---|---|---|---|---|
| Constraint capacity / irrelevant capacity / migration | compatible (all hold on chains) | holds, mainly from unbalanced bases | holds in the candidate family | n/a |
| Capital pressure | compatible | holds (550/576 binding) | holds | n/a |
| Strong, material, order-robust multiplicity | **predicts absence** | fails in 557/576 cells; the remaining 19 are threshold-edge pipeline tweaks | **holds** in 735/2,304 cells, 694 via pooling | order: refuted for pooling (pairs survive reversal); sub-threshold: refuted for pooling (no threshold involved); price: **partly supported** (vanishes at φ = 2.0) |

**Surviving:** H0 for dedicated-only catalogues; H1-F within a price and budget band; H-artifact survives
only as a price-band qualification.

## Adversarial analysis (self-administered only)

This is the author's own attempt to falsify the result, not an independent review.

- **Omitted candidate / catalogue choice.** The researcher put the flex offer in the catalogue, so the
  positive might be an artifact of that choice. It is disclosed, pre-registered, and analyzed separately
  as Family D. The conclusion is conditional on it, and states that condition as the load-bearing
  requirement.
- **Is a flex cell "current semantics"?** Yes: one `ConfiguredResource` in two steps' authored eligible
  sets, executed by the existing selection, backlog and cascade rules. Nothing new is inferred. The game
  must author that eligibility explicitly, and no Arcogine qualification or discovery is involved.
- **Order artifact.** It is real for individual flex completions (up to 6 ticks). Only the two extreme
  tie-break priorities (flex last, flex first) were tested; interleaved orders were not. Of the 780
  order-C material cells, 735 (94%) stay material under R, and the candidate's pairs survive both orders. Residual
  risk: some other order could reshape specific frontiers.
- **Operational definitions.** Strong multiplicity, the 5% materiality threshold and the pool-level
  active-constraint derivation are the researcher's. Family D's verdict is threshold-sensitive: its 19
  material cells sit at or one tick over the threshold. The pooling verdict does not depend on the
  threshold.
- **Weighted-score smuggling.** No score was used. As a side check only (a research-local computation
  over a landed policy, not a proposal), the landed reference policy's additive score would rank S1
  first (214 vs 160 vs 65), because its 1 credit = 1 tick exchange lets budget dominate. Any fixed
  scalarization picks one design, which belongs to the scoring and level-structure question.
- **Possibility versus necessity.** This shows that a non-spatial design space exists with flex offers.
  It does not show that a dedicated-only kernel needs spatial or per-resource-speed semantics to become
  interesting; neither was tested.
- **Window reasonableness.** One serial three-step routing, two duration profiles, `N ≤ 24`, one order.
  Pooling positives persist at `N = 24` (104 cells). Dedicated fine-tuning effects are pipeline
  fill/drain effects and are expected to shrink as `N` grows, but that was not tested beyond 24.
- **Conclusion stronger than evidence?** The claim is limited to "exists, is explainable and is robust
  within the declared window, subject to four qualifications." No general game-design or cross-routing
  claim is made.

## Surviving invariants

Within the declared window and current semantics:

1. For resources that each serve one step, completion depends only on per-step slot provision: it is
   invariant to machine granularity and to projection order.
2. For a resource eligible for several steps, completion also depends on resource identity order through
   the `MachineId` tie-break.
3. With dedicated offers only, the non-dominated set is a bottleneck-purchase chain apart from small
   non-bottleneck pipeline-alignment effects.
4. A multi-step-eligible offer can make non-nested structures non-dominated by pooling capacity, when
   priced so the pooling benefit exceeds its premium.
5. Cost-minimal target-meeting designs tend to be balanced (co-constrained), where one-unit additions do
   not improve completion.

## What did not survive

- **E2 as stated** (Family D monotone everywhere): 37 cells showed non-nested pairs. It survives in
  substance, since they are fine-tuning or threshold-edge pipeline effects.
- **Pass-1 strong multiplicity as a discriminator.** Counting any explained non-nested pair let
  sub-threshold effects pass as strategy. Pass 2's classification is needed to read it.
- **The pre-selected reference cell as a positive case.** It is monotone. A reader who saw only that cell
  would wrongly conclude a full falsification.
- **"Faster" or "better" equipment in the non-spatial kernel.** It is inexpressible; granularity and inert
  fields cannot substitute for it.
- **Constraint capacity from the cheapest design as a universal demonstration.** It fails from balanced
  bases.
- **Mid-run queue length as a bottleneck indicator.** A faster upstream step can build the largest queue
  ahead of a step that is not the limit, so occupancy over the run, not a queue snapshot, identified the
  constraint.

Reusable negative knowledge: *Pareto non-dominance on (cost, time) is not evidence of a strategy space.
Nestedness and sub-threshold incomparability must be tested; otherwise any capacity-for-money chain looks
strategic.*

## Transferability and reuse

- **Arcogine-specific dependencies:** step-level duration, the `MachineId` tie-break, and the FIFO/backlog
  cascade.
- **Potentially transferable signal (hypothesis only):** "non-dominance ≠ strategy space" — nestedness plus
  a materiality test is a cheap, reusable discriminator for capacity/capital design problems.
- **Reusable assets:** the pre-registered protocols, the enumeration/frontier/classification harness, the
  S1/S2/S3 pooling family (a compact proving case that pooling is visible from supported dispatch events),
  and the granularity-invariance control.
- **Synthesis-seed candidate for reconciliation to consider:** the non-dominance signal above, revisited
  if a second consumer or domain uses a Pareto-frontier argument for "interesting choice". Nominating it
  here does not admit it.

## Confidence and limitations

Medium overall; see *Executive conclusion*. The result would change if:

- a different projection order materially removes pooling pairs;
- the product rejects multi-step-eligible offers;
- the dispatch rules (tie-break, cascade, backlog) change, since flex outcomes depend on them; or
- larger or branching routings behave differently.

Limitations: one routing topology; two projection orders; research-local constraint derivation; cost
computed research-locally as a sum of prices (the landed `DraftEconomicsCalculator` sums purchase costs
the same way, but it lives in `:challenge` and was not invoked). The Challenge catalogue does not currently
project to Factory facts; the projection used here is research-local.

## Unresolved unknowns

- Whether players can perceive and explain pooling, and order-dependent flex outcomes, from supported
  evidence. This belongs to the diagnostic-evidence question and, for reusable derivations, the analytics
  ownership boundary.
- Whether any resource order other than the two tested extremes changes frontier structure.
- Whether the dedicated-only collapse persists for branching or longer routings, larger `N`, or
  multi-product workloads (out of scope).
- Which scalar objective, if any, keeps several structures competitive (the scoring/level-structure
  question).

## Durable consequences

Recommendations for a separate reconciliation; none is performed here.

1. **Research register:** record the verdict "qualified positive — a non-spatial capacity/capital design
   space exists only with multi-step-eligible offers; the dedicated-only kernel collapses to monotone
   bottleneck purchase", with this report's qualifications. Mark `CONCLUDED` only when the requirement set
   lands.
2. **Product/challenge requirement set (bounded, for the first non-spatial reference challenge):**
   - include at least one catalogue offer that projects to a resource eligible for two or more routing
     steps, priced within a band where pooling beats its premium (evidence: about 1.0–1.5× the dearer
     dedicated machine it substitutes);
   - make the draft-to-Factory projection's resource ordering deterministic, challenge-owned content;
     outcomes of shared resources depend on it, and incidental build order must not silently change
     results;
   - never present twin/single granularity, `setupTime` or `capacityLiters` as performance differences;
   - set the budget with slack above the cheapest target-meeting cost, and choose targets knowing that
     cost-minimal designs are often balanced;
   - use the S1/S2/S3 family as an illustrative reference, not as a contract.

   No scoring formula, spatial mechanic, UI or Engine change follows from this.
3. **Hand-offs:**
   - **Diagnostic-evidence question:** flex designs need dispatch-level evidence (who served which step),
     co-constraint sets, and a refusal where per-step attribution of shared capacity is unlicensed.
     Mid-run queue length alone can point at the wrong step — the candidate's starter queues at ASSEMBLE
     while INSPECT limits.
   - **Controlled-retry candidate:** one-variable retries from balanced designs often show no effect.
   - **Scoring/level structure:** any fixed scalarization, including the landed reference policy, selects
     one frontier design.
4. **Factory/Engine:** no change indicated. The `MachineId` tie-break is already normative. This
   investigation provides no trigger for the qualified-applicability/resource-dependent-performance
   candidate, because the positive needs no per-resource performance and the dedicated-only collapse
   was not tested against it.
5. **Knowledge transfer:** consider preserving the S1/S2/S3 pooling family and the granularity/order
   controls as a durable research-corpus proving case if the diagnostic-evidence work needs them. Such a
   fixture must describe itself semantically and must not reference `workspace/` or `docs/research/`.
   Otherwise the harness and raw outputs can be explicitly discarded at retirement. No exact report
   artifact needs to stay readable after reconciliation.

## Implementation implication

No implementation. No planning admission follows from this report. Headless Challenge capability is
unchanged.

## Follow-up triggers

- Product decides the first slice will not include shared-eligibility equipment: treat this result as
  negative for that slice.
- An Engine dispatch-rule change: re-run the flex-family experiment.
- Per-resource performance semantics are proposed: re-examine the dedicated-only kernel.
- A second routing topology is wanted: open a new bounded question.

All of these are tracked through the research register.

## Sources

Repository only; no external evidence was used, because none would discriminate between the candidates
beyond what exhaustive execution of current semantics established.

Workspace artifacts on branch `research/factory-design-game-strategy-space`:

- `workspace/research/investigations/factory-design-game-strategy-space-protocol.md` — pass-1
  pre-registration, commit `218d2b9a`;
- `workspace/research/investigations/factory-design-game-strategy-space-protocol-pass-2.md` — pass-2
  revision, commit `2efc10f4`;
- `workspace/research/investigations/factory-design-game-strategy-space-experiment/StrategySpaceExperimentTest.java`
  — harness: pass-1 run revision `29682e7e`; pass-2 revision `efc365f0`; candidate replay `ebda9e54`;
- `.../results/pass-1/` — pass-1 raw outputs (`dbfd673b`), reproduced byte-for-byte by later revisions;
- `.../results/pass-2/` — order-R outputs, classified pairs, cell verdicts, summary and candidate replay
  (`efc365f0`, `ebda9e54`).

These coordinates are active-custody identifiers only.
