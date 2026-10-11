# Simulation analytics ownership — independent adversarial review of the Phase 2 post-execution-evidence revision

> **Disposition: `ACCEPT WITH QUALIFICATIONS`.** The load-bearing recommendation
> `RECOMMEND STAGED/NO SHARED OWNER NOW` survives: no consumer-neutral analytics owner now; staged,
> kind-specific admission (D refined by E); definition ownership separated from evidence custody;
> no Engine, runtime-contract or Determinism Contract change. Six qualifications (§8) must be part of
> any durable reconciliation. Two of them correct the downstream evidence/provenance trigger and the
> Game horizon wording. Neither changes owner placement.
>
> **Reviewed report (sole target):** workspace branch `workspace/simulation-analytics-ownership-phase-1`,
> commit `d2125f39482de3306f2f56e9c7261a3dde160b33`,
> `workspace/research/investigations/simulation-analytics-ownership-phase-2-post-execution-evidence-revision.md`.
> Report-stated baseline and final recheck: `a7f9856617d99e796d528a44aa8c15677c485f55`.
>
> **Review handoff:** commit `3248f93b959c0596de7327eabf0da8dc7ab70b2b`,
> `workspace/research/handoffs/simulation-analytics-ownership-phase-2-post-execution-evidence-adversarial-review.md`.
>
> **Reviewer live-main baseline:** `a7f9856617d99e796d528a44aa8c15677c485f55` (#469), resolved
> 2026-10-11 at review start; rechecked unchanged at review end (§9).
>
> **Independence:** achieved at the **fresh isolated session** level. This session did not write the
> report, its pre-registration, its probes or any earlier phase, and has no responsibility for
> defending them. **Not a different model family**: the author model cannot be determined from the
> artifacts (they carry the owner's human Git identity) and is very probably the same family. This is
> not a self-administered pass. Anchoring disclosure is in §2.
>
> **Authority:** high-risk research review only. No architecture, specification, runtime, planning,
> register, brief, product code, original report, pre-registration or author probe is changed or
> authorized by this review.

## 1. Result in one paragraph

The report correctly finds that nothing in the live repository fixes a shared named measurement. The
Game is the only concrete consumer, the Factory Design utilization objective is illustrative, and
Governance analytical evidence is fixture-only. It also correctly retires the earlier
"custody follows the definition owner" rule against the canonical runtime contract, and it states
interval sufficiency per claim in canonical coverage and finality terms. Every pre-registered value
was reproduced and independently hand-derived. The four reviewer probes found the following, none of
which moves an owner:

- two false acceptances in the **research-only** proof helper, from mixed-run and closure-contradicting
  bundles;
- a one-change conservatism in its observation branch;
- a gap between a one-observation snapshot and the report's "known-work horizon" wording.

The material corrections are to *downstream wording*. The report's restated evidence/provenance
trigger silently drops dimensions the maintained brief keeps (additional supported inputs,
accumulation, admission of a shared definition). Its Game classification folds a snapshot labelling
rule into a controller no-future-input requirement. Both are qualifications, not falsifications.

## 2. Pre-report reconstruction and anchoring

- **Reconstruction checkpoint:** commit `d43251eedac326e4a5ac9572fd5d8e214ec2016d`,
  `workspace/research/investigations/simulation-analytics-ownership-phase-2-post-execution-evidence-review-reconstruction.md`.
  It was committed and pushed before the report body beyond its metadata block and heading list was read.
- **Derived before reading:**
  - nine candidate models: Engine measurement M1; Engine running accumulator M1′; shared owner now M2;
    consumer-local M3; staged admission M4; kind-specific split M5; custody-led M6;
    Governance-owned M7; shared stateless definitions with any holder M8;
  - thirteen failure cases R1–R13: custody ≠ definition; controller-only evidence; promotion of
    retained results; honest controller vs passive party; "so far" finality being free because
    observation time equals last-change time; snapshot vs horizon; reachable idle-with-queued-work;
    stale frontier; helper false positive; late joiner; throughput basis; shared-owner trigger;
    downstream-trigger narrowing;
  - an explicit, falsifiable prior: M4+M5 best supported, M1′ viable-not-now, M2 not now, with five
    survival conditions (a)–(e).
- **Unavoidable anchoring:** the handoff states the report's headline and candidate labels and
  paraphrases some probe content (P4.3, P6, P7, `prove()`). The reconstruction says so. Where its
  candidates coincide with the report's (A≈M1, A′≈M1′, C≈M2, D≈M4, E≈M5, F≈M6, G≈M8), that is not
  independent recurrence.
- **What the reconstruction found that the report does not foreground:**
  - R5, that live "so far" measures need no closure, which the report does reach in §8 row 2;
  - R6, snapshot vs horizon, which the report only partly separates (§8.3 below);
  - R9, helper false positives;
  - R13, trigger narrowing.

## 3. The report's decisive claims, restated by the reviewer

1. **No shared owner now.** A consumer-neutral analytics owner needs one of:
   - two concrete consumers fixing the same named method;
   - one consumer plus a concrete core verification/governance requirement with acceptance evidence;
   - an outward contract;
   - divergent duplicates.

   None exists. Until one does, a measurement's meaning belongs to whoever fixes the choices execution
   does not fix — today, the Game for its two measurements.
2. **Custody is not definition.** Whoever has the capture opportunity for the needed horizon holds the
   evidence (normally the exclusive driver). The definition owner states what evidence suffices and
   what to refuse. Promotion of a definition moves definitional custodianship, not capture.
3. **Sufficiency is per claim.** A fresh observation is enough for current-state claims and for the
   Engine's declared results. An interval claim needs:
   - a basis at or before `a`;
   - gap-free changes through a proof sequence;
   - a finality proof: a producer witness, a producer observation, or attributable controller closure
     with clean outcomes.

   Fault-free, pre-join and reproduction claims need additional classes.
4. **Engine alternatives.**
   - A standardized Engine measure (A) and an execution-fixed run-to-date aggregate (A′) are viable but
     not admitted now.
   - A′ would replace retention for late joiners, not finality.
   - A universal outside-Engine ban (B) is rejected.
5. **Current fields.** `busyTicks` is removed. The five declared results stay Engine-owned with exact
   arithmetic. `throughputPerTick` is a rate over observed supported time, not a period rate.
6. **No Engine, runtime or Determinism Contract change** follows from the ownership result.
7. **Downstream.**
   - Evidence/provenance stays `CANDIDATE`, with a trigger restated as reliance by a non-driving or
     non-holding party, retention beyond the holder's horizon, or reproduction.
   - Adapter compatibility and comparability are unchanged.
   - The Game diagnostic question stays `READY`.

## 4. Grounding and authority verified at `a7f9856`

Read at the live baseline:

- `AGENTS.md`, the researcher contract and `researching.md` (§1 implementability, §2, §7, §9);
- the register;
- the analytics ownership, evidence/provenance and execution-account briefs, and the Game diagnostic
  brief;
- `runtime-contract.md` in full: supported observations; internal scheduler machinery; basis/coverage/
  frontier; half-open finality; responsibilities and limits;
- `engine-semantics.md` §1–§1.2, §4 and §9–§11;
- the overview's domain authority, state ownership/headless closure and Determinism Contract;
- Charter §6; Factory Design §10; Governance evidence §5–§7 and §11–§13;
- the Governance capability plan (analytical fixture rules); the spatial-runtime ownership note;
- the Game consumer plan §4–§5 and the vertical-slice gate;
- `testing.md` §10.

Source read at `a7f9856`: `FactoryRuntime.observe()`/`emit()`, `FactoryHandler` aggregates,
`ResourceObservation`, `JobObservation`, `RuntimePerformanceObservation`,
`RuntimeEventType`/`RuntimeEventPayload` and `JobStatus`.

**Repository facts the review relies on:**

- `ResourceObservation` has no `busyTicks`. `git grep busyTicks` at `a7f9856` finds no `product/` or
  durable architecture/reference/development hit.
- `throughputPerTick = completedSales / observedTime`, where `observedTime` is set only in `emit()`.
  It is cumulative since run start over the last supported change time.
- Observation time equals the time of the change at the observation's cursor.
- Commands apply at that time after every session call (Engine §1.2). A command issued after advancing
  an idle run therefore lands at the last change time (reproduced in P7).
- The Game consumer plan still reads "**Unresolved — not game-owned by default**". The game "must not
  implement generic analytics locally as though it owned the semantics" until this question resolves.
- Governance evidence §11 keeps verification-objective semantics "domain-owned". §12 says
  analytical-definition provenance ownership is unresolved.
- `governance-conformance-capability.md` proves analytical results only through attributed fixtures.
- **Bounded absence search:** `git grep -iE 'utiliz|occupanc'` over `product/governance` and
  `product/consumer` at `a7f9856` returns nothing. `utiliz` over all of `product/` returns only two
  `HeadlessClosureAcceptanceTest` comments, both disclaiming a utilization measure, and one
  research-oracle test; the occupancy oracles live only in the non-shipped `research-experiments`
  module. No outward adapter exists (overview). Open PRs are #467 and #468 (Dependabot), neither
  touching this boundary.

**Concrete consumer verification:**

- The Game diagnostic report (`4ad0013`, §9) identifies exactly two concrete measurement candidates,
  "utilization over a period" and "flow characterization of idle slots". Both are "Ownership:
  Unresolved". Bottleneck is pending a named method.
- No playable live loop exists. The playable slice remains gated.
- The independent Game review (`6484795`, `ACCEPT WITH QUALIFICATIONS`) says reuse beyond the game is
  "not demonstrated". It also says game-local presentation "remains a live candidate".
- Factory Design §10.2's "Maximum utilization <= threshold" is an unevaluated illustration with no
  basis.
- Nothing has changed since the report's baseline.

## 5. Executable verification

**Environment:**

- detached worktree of `a7f9856` under the gitignored `logs/`;
- container OpenJDK 21.0.12.1, repository Gradle wrapper 9.8.0;
- experiments run through `-PresearchExperimentSources` (`testing.md` §10);
- first research-suite attempt failed during dependency resolution with HTTP 429 from Maven Central
  (environmental; no test ran), and the retry succeeded online.

| Run | Command (from `product/`) | Result |
|---|---|---|
| Author's suite reproduction | `./gradlew :research-experiments:test -PresearchExperimentSources=<ac0ac2a export>/simulation-analytics-ownership-post-execution-evidence --rerun-tasks` | **15 classes, 154 tests, 0 failures, 0 errors, 0 skipped**: 137 tracked + 17 custody (`PostExecutionEvidenceDiscriminatorExperiment` 8, `PerformanceEvidenceSufficiencyPostRemovalRerun` 6, `AdmissionBoundaryDiscriminatorPostRemovalRerun` 3). Matches the report |
| Accepted conformance | `./gradlew :factory:test --tests '*BoundedAdvancementConformanceTest' --tests '*EngineDerivedResultConformanceTest' --tests '*HeadlessClosureAcceptanceTest' --tests '*RuntimeEventDeliveryAcceptanceTest' --tests '*SessionControlAcceptanceTest' --rerun-tasks` | **46 tests, 0 failures** (10, 4, 5, 14, 13). Matches the report |
| Reviewer probes | author's three classes, byte-identical to `ac0ac2a` (checked with `cmp`), plus `PostExecutionEvidenceReviewProbe` in one untracked directory; `--tests '…PostExecutionEvidenceReviewProbe' --tests '…ResearchPackageBoundaryTest'` | **4/4 and 4/4 passed on the first run**; no expectation edited after running |

**Chronology.** Pre-registration `07ec402` (12:24:01) → probes `ac0ac2a` (12:31:46) → report `d2125f3`
(12:39:06), each the parent of the next. Git proves *commit* order only. It cannot prove that no probe
source existed locally before `07ec402`. The narrow seven-minute window is noted. The values below
were therefore derived by hand, not taken from the pre-registration.

**Hand derivations agreeing with the pre-registration, the assertions and the rerun output:**

- **P1:** 5; naive stale fold 100; spent budget and target 50 refuse; `[0,50)` 5.
- **P2:** `[0,5)` 5 survives the 6..7 gap; `[0,10)` refuses against cursor 7; complete 10.
- **P3:** joined basis `(5,5)` gives `[5,10)` 5 and refuses `[0,10)`.
- **P4.1:** §4 rule 7 serializes four bakes on one of two Oven slots, giving (16,32) and flow
  16/12/0/0/4. With closure through 20: (16,40), no-work-left 12.
- **P4.2:** Packer slot-ticks over `[0,5)`: 1 working, 8 starved uncapped, 4 capped, 1 no-work-left.
- **P4.3:** closed `[5,9)` gives no-work-left 8; open gives starved 8 (4 capped).
- **P5:** 6 / 0 / 0 / 10 / 4.
- **P7:** 0.2, 5.0, period 1/100, second submission at 5.
- **P8:** a hiding closure gives 95.

**Ports.** Diffs against `6a5c0435` and `75736ddd`:

- Only `busyTicks` reads are removed or restated.
- Prior C is restated from completion-credited durations to dispatch→completion occupancy, as the
  pre-registration says. Its discriminator survives in stronger form: the fresh observations become
  entirely equal.
- The admission port loses the "credited 24/8 vs occupied 12" discriminator, which P5 re-establishes
  (running occupancy 6 vs credited 0).
- No baseline expectation was silently changed.

**Reviewer probes.** Pre-registered at `f8c4ba3a4975b539b3bf60eda4fe2d9c4bcb173b` (path in §9) before
any probe source existed. Source at `cb926538c353bb4c760183cc83846f9b1fc04ed5`. Observed output:

```text
R1: own basis [0,5)=5; foreign-run basis accepted=10 under 'producer witness: change 5 at time 5';
    same-run check refuses (basis observation belongs to another run) and keeps 5
R2: post-bound closure [0,100)=5; stale-bound closure accepted=100 under 'controller closure by driver
    (closed script), exhausted through 100, bound to (2,0)'; contradiction check refuses (closure bound
    to cursor 2 is contradicted by held changes [3, 4] before 100) and keeps 5
R3: holder 1..6 + observation (7,5) refuses (... observation at time 5 has cursor 7 but changes [7] are
    missing ...); observation-time variant Mixer 5, Packer 1; complete holder Mixer 5, Packer 1
R4: snapshot at (6,5) closed = open = {working 0, idle 2, needing 0, no known work 2};
    open after second submission (8,5) = {needing 1, starved uncapped 2, capped 1}
```

**Labels.** All P-results and R-results above are executable current-code evidence. Everything about
A, A′, a clock witness or a shared owner is specification-derived or an architectural preference. None
of those mechanisms exists.

## 6. Adversarial matrix

| # | Challenged assertion | Independent test or source | Observed fact | Assertion disposition | Effect on recommendation |
|---|---|---|---|---|---|
| 1 | No shared owner is justified now (§4.1, §12.3) | Charter §6; Factory Design §10.2; Governance §11–§12; Governance capability plan; bounded `product/` search; Game report §9 and review §4.3 | No core requirement with acceptance evidence fixes a method; one concrete consumer; no adapter | **Survives** | Load-bearing; holds |
| 2 | Rule 4 trigger set, including "(b) one consumer plus a core requirement; no shipped count" | REOPEN D/J; Charter §6 | Honours the "no shipped consumers needed" correction; a single core requirement without the Game would make its verification domain the owner under Rule 3 | **Survives** | None |
| 3 | Old Rule 7 falsified; custody follows capture opportunity (§7) | Runtime contract "definition ownership does not imply physical custody"; P6; reconstruction R1 | Canonical text is decisive. P6 proves *possibility* of one holder feeding a shared stateless call site, not a cross-party handover contract; the runtime contract's handover clause governs that | **Survives**, scoped | None |
| 4 | Promotion preserves meaning under four preconditions; "no-rework" only conditional (§7.1) | Runtime contract handover clause; Governance §5; reconstruction R3 | Conditionality is correct, but the carried-provenance list omits run/model binding and basis/frontier coordinates (§8 Q2) | **Survives with qualification** | Q2 |
| 5 | Six claim-specific sufficiency classes (§8) | Hand derivation of E5/E6; P1–P8; reviewer R3 | Correct and conservative. Current-state claims need only an observation; periods ending at or before the last change need a producer witness; idle tails need closure, because a later command lands at the last change time (P7) | **Survives** | Load-bearing; holds |
| 6 | The research-local `prove()` encodes the contract (§9 limits) | Reviewer R1, R2 | **False acceptances on inconsistent bundles:** a basis from another run gives 10 for a true 5; a closure bound to a pre-advancement observation gives 100 although the holder holds the contradicting completion. No P-result changes: every P-bundle is same-run and holds no post-binding change before `b` | **Helper defect, not a normative falsifier.** The runtime contract already forbids cross-run merging and says contradictory deliveries can expose a broken commitment | Q2 refusal rules; helper must not be reused as a verifier |
| 7 | Observation-frontier branch is "conservative" (§15) | Reviewer R3 | Over-strict by exactly the cursor change when it is alone at its time (the witness branch handles every other case); never a false value. The canonical wording ("captured through the cursor") is equally conservative | **Survives** (characterization exact) | None |
| 8 | Live diagnostics: current state from a fresh observation; idle-flow needs "the known-work horizon as an explicit controller no-future-input requirement" (§8, §12.2) | Game report §9 (flow defined per tick from one observation; "known work only" refusal rule); reviewer R4; P4.3 | The report does **not** make all live diagnostics depend on closure (§8 row 1). But §12.2 folds the Game's "known work only" *labelling* rule into a controller requirement. R4 shows "no currently known work" at `(6,5)` is true in both P4.3 runs from one observation. Only interval ticks in an idle tail, or horizon wording, need closure | **Survives with qualification** | Q3 |
| 9 | P4.3 discriminates the interval classification | P4.3 code and derivation | Identical supported prefixes 1..6 and observations at 5. `[5,9)` differs because the closed run's ticks 6..8 lie in an idle tail where a later command could still land at 5. It is an interval result, correctly not generalized to snapshots in §8 row 1 | **Survives** | None |
| 10 | Flow oracle scope is stated (§9 limits, §12.2) | `flow()` source | Always-online, linear single-product routing and constructive scripts are stated. **Not stated:** the fold refuses any basis that holds jobs, so no late-join flow evidence exists | **Survives with qualification** (scope disclosure) | Q3 |
| 11 | Game qualifications 3–5 (third state, starved cap, pool basis, availability) carried (§12.1–§12.2) | Game review §6; P4.1–P4.2; admission ports 2–3 | Third state reproduced (via §4 rule 7) and capped/uncapped separated. Pool basis for a multi-step-eligible machine is still unexercised, as the Game review said | **Survives** | Carried into Q4 |
| 12 | Game-owned consumer-local default (§12.2, §17.2) | Game plan §4/§5; vertical-slice criterion 4; Game review §4.3 | It is a recommendation that **reverses** the live "not game-owned by default" rule. The report calls it "classification, not product admission" but does not name it as a reversal | **Survives with qualification** | Q4 |
| 13 | A viable but not admitted (§10.2) | Engine §10; ports 1–2; REOPEN A/C | Not falsified; costs stated as costs, not contradictions; "not now" rather than "never" | **Survives** | None |
| 14 | A′ substitutes for retention, not finality; not admitted (§10.2) | Spec derivation; observation time = last change; P3; reconstruction R10 | Differencing ends at observation times, so an idle tail still needs closure; a late joiner gains cumulative occupancy but not pre-basis residence per step. Overflow, observation and conformance obligations are named. Rejection rests on "no present consumer lacks capture", not on the recent removal | **Survives** (specification-derived) | None |
| 15 | B "falsified as a universal rule" | Brief worked examples; overview domain authority; `researching.md` §1 | The result-affecting leg is a real contradiction with accepted ownership. The late-join leg is hypothetical and shows over-restriction, not falsification; the table's conditional wording is right, the verdict word is strong | **Survives with wording qualification** | Q5 |
| 16 | C viable, not admitted; D selected; E selected; F falsified; G an arrangement only | §10.1 table; runtime contract | F falls to canonical text. G changes implementation custody, not owner placement. Conditional custody coupling exists when a definition is computed by bounded accumulation (definition-shaped retained state) | **Survives** | Feeds Q1 |
| 17 | Current fields and `throughputPerTick` (§11) | `observe()` source; Engine §10.1–§10.2; P7; EngineDerivedResult conformance | Exact. P7's 0.2 vs 0.01 is a definition and time-basis difference, not an Engine defect, and the report does not call it one. Phase 1 already flagged the observation clock; P7 adds post-#465 executable proof | **Survives** | None |
| 18 | No Engine, runtime or Determinism Contract change (§4.6) | All of the above | Nothing proposed alters a result-affecting rule or the observation contract | **Survives** | Load-bearing; holds |
| 19 | Evidence/provenance trigger restated in custody terms; "sharing alone is not a trigger" (§4.7, §13) | Maintained brief and register row; the prior revision `75736ddd` §13 (which also triggered on shared-owner admission); reconstruction R13 | The restatement drops three of the brief's dimensions: additional supported inputs (routed only to A′), accumulation, and admission of a shared definition. Excluding a single-owner, co-located stateless definition is a permissible refinement. Dropping accumulation is not: incremental state is definition-shaped custody, which defeats the G premise behind "sharing alone" | **Survives with qualification** (`CANDIDATE` decision holds; trigger narrowed) | Q1 |
| 20 | Cross-revision comparability: a new concrete risk from #465 and #469 (§13) | Engine §4 rule 3 compatibility sentence; Governance §7 | Concrete for budget-stopped scripts and `busyTicks`-reading results. Start-fixed or time-bounded scripts (the Game's recorded generator) keep identical supported events under #465. Provenance hygiene, not presumed incomparability | **Survives with precision note** | Q5 |
| 21 | Drift and stale baseline (§2) | `git log 306205c3..a7f9856`; live recheck | #461–#466 and #469 match; no later drift | **Survives** | None |

## 7. Explicit rulings

### 7.1 Custody versus definition

**Upheld.** The holder/attestor/definition-owner split is canonical, and the report applies it
correctly. The revised Rule 7 restates the runtime contract's "retention belongs to that holder … for
the horizon its use needs". It must not be adopted as a second, independent policy sentence.

Answers to the handoff's questions:

- **Several calculations from one holder.** One exclusive driver/holder can feed several independent
  named calculations without moving custody or either meaning. P6 shows one definition at two call
  sites, and the claim for two distinct definitions follows by the same argument; nothing executes a
  cross-party handover.
- **What travels on promotion.** Identical arithmetic and declared inputs are **not** sufficient for
  retained results. The always-required and claim-specific provenance are set out in Q2.
- **Upgraded commitments.** A shared definition does not upgrade a controller commitment into producer
  evidence, provided the result names its proof class and attestor (P6). It must also refuse a closure
  contradicted by evidence it holds (R2).
- **Intrinsic custody.** No accepted requirement makes custody intrinsic to a definition today. The
  conditional counterexample is bounded accumulation: when a holder keeps definition-shaped state
  instead of raw supported evidence, custody becomes method-coupled.
- **No-rework.** The report's "one-way/no-rework" claim is correctly conditional on proof provenance
  being captured from the start.

### 7.2 E5/E6 proof and refusal

**Upheld** as a normative statement. Independently derived:

- a valid same-run basis at or before `a`;
- gap-free changes from `S0+1`;
- the first change at time `>= b` (prefix through `K-1`); or an observation at time `>= b` with
  coverage through its cursor (conservative by one change, R3); or a bound closure with demonstrated
  exhaustion, input exclusion through `b` and coverage through `S*`.

Specific points:

- `QUIESCENT`, a spent budget, a lower target and a fault each invalidate closure (P1, P8).
- An earlier proved prefix survives a later gap (P2).
- Changes exactly at `b` are excluded (P2.4, P5).
- Equal-time order is by sequence (P4.1).
- Pre-join history is refused (P3).
- The honest-controller/passive-party separation is real (P8): complete supported changes certify
  neither fault-freedom nor complete commands.

The only incorrect acceptances found (R1, R2) are in the research-only helper, on mixed or
contradicted bundles. They are not counterexamples to the report's normative assertions or to the
runtime contract.

### 7.3 Game current-state versus horizon

**Upheld with qualification.** The report keeps current-state claims observation-only (§8 row 1) and
correctly requires finality for period aggregates. Under current Engine semantics, idle-tail ticks
need controller closure, because a later command applies at the last change time (P7). However:

- the Game report defines flow per tick from one observation with a "known work only" refusal rule;
- §12.2 turns that labelling rule into a controller no-future-input requirement;
- R4 shows the snapshot claim "no currently known work" is true from one observation in both P4.3
  runs, with no closure.

Q3 fixes the wording.

On the prior Game review's qualifications:

- the third state, starved cap and availability treatment are honoured;
- the pool basis for a multi-step-eligible machine remains unexercised;
- Game-as-default owner is a **recommendation** awaiting Game-plan reconciliation, not a live product
  rule (Q4).

T1, T2 and T3 stay Game-admission checkpoints, not present Engine or analytics requirements.

### 7.4 A, A′ and the shared-owner alternatives

**Upheld.**

- A and C are "viable, not now". A′ is "viable with cost, not now". Each has a concrete reopening
  trigger.
- None is rejected merely because `busyTicks` was just removed.
- The REOPEN review's demand to grade alternatives as impossible, unsupported or less attractive is met.
- B is correctly rejected as a universal rule, with the wording precision in Q5.
- Nothing here is a permanent prohibition.

### 7.5 Current fields

**Upheld.**

- `busyTicks` is removed (#469).
- `backlog`, `completedOrders`, `completedSalesValue`, `averageLeadTime` and `throughputPerTick` stay
  Engine-owned with §10.1/§10.2 arithmetic; `averageLeadTime` and `throughputPerTick` are a finite
  compatibility exception.
- `throughput(long)` and the other public accessors stay method-level-support open (Q5 of Phase 1).
- `combinedQueueDepth` stays Engine ranking semantics.
- Phase 1's S/D/R-obs/R-hist/M classification survives; R-hist now holds only `completedSalesValue`,
  and Phase 1 finding 3 (utilization ambiguity) is resolved by #469.

### 7.6 Evidence/provenance trigger

**`CANDIDATE` upheld; trigger qualified (Q1).**

- The custody-crossing cases (handover, passive verifier, Governance use), retention beyond the
  holder's horizon and reproduction are genuine triggers.
- They are **additional** to, not a replacement for, the brief's existing dimensions.
- No new investigation is triggered by this review.

## 8. Qualifications that must survive reconciliation

**Q1 — Evidence/provenance trigger: refine, do not narrow.**

- Keep the maintained brief's trigger dimensions:
  - additional supported inputs;
  - method-specific sufficiency for a shared or custody-crossing use;
  - accumulation used in place of retention;
  - definition identity;
  - result provenance;
  - retention.
- Add the report's custody-crossing examples (handover, passive verifier, Governance evidence use)
  and reproduction across sessions.
- Admission of a shared definition owner under Rule 4 remains a trigger for evaluating which of those
  dimensions apply.
- The report's exclusion applies only to a co-located holder supplying raw supported evidence to a
  stateless definition with no additional input, accumulation or retention need.
- A request for an additional supported input stays an evidence/provenance concern as well as a
  possible A′/runtime reopening. It is not routed to A′ alone.

**Q2 — Promotion-ready provenance and refusal rules.**

- **Every retained or promoted interval result carries:**
  - the definition identity;
  - run identity and `ModelFingerprint` (`ControlledRevisionId` only when authoritatively bound);
  - the claim interval and convention;
  - the basis `(S0,T0)`;
  - the proof sequence or frontier;
  - the proof class.
- **Carried only when the claim needs them:**
  - controller attestor, excluded-input scope, trust assumption and command/fault outcome
    qualifications (closure, fault-free claims);
  - exact producing Engine-definition provenance (cross-revision comparison or reproduction; it
    remains a separate obligation and is never synthesized);
  - the full ordered command script with advancement interleavings and budgets (reproduction).
- **Every definition's refusal rules include:**
  - same-run binding of basis, changes, observations and closure binding (R1);
  - refusal of a closure contradicted by a held change after its binding and before `b` (R2).
- The research-local helper at `ac0ac2a` lacks both checks and must not be reused as a verifier.

**Q3 — Snapshot versus interval versus horizon.**

- Per-tick idle-flow classification at an observed state ("no currently known work", or "starved"
  meaning a known unfinished unit needs this resource later) is a current-state claim: one fresh
  observation plus published routing.
- A period aggregate of those classifications needs interval finality. Ticks in an idle tail need
  controller closure, because a later command applies at the last change time.
- "No work left for the rest of the period" is horizon wording that requires a no-future-input
  commitment.
- The Game's "known work only" rule may be met by snapshot labelling instead.
- Record the flow research fold's unstated scope limit: it needs a basis without jobs and is
  always-online, single-product and constructive only.

**Q4 — Game plan default is a reversal, made live only by reconciliation.**

- "Game-owned named definitions" replaces the live "Unresolved — not game-owned by default" row and
  the "must not implement generic analytics locally" sentence in the Game consumer plan §4–§5.
- It also affects vertical-slice criterion 4.
- Until that reconciliation lands, the live rule stands.
- The Game's definitions are adopted by the separately owned Game diagnostic reconciliation, with the
  Game review's qualifications 3–5: third state or refusal, starved cap, pool basis, exercised
  availability treatment.
- Research-local definitions are not promoted.

**Q5 — Wording precision.**

- B is "rejected as a universal rule", because it contradicts the accepted result-affecting ownership
  rule and would pre-empt the comparison `researching.md` §1 requires. It is not empirically falsified
  by a present late-join consumer, which does not exist.
- The cross-revision note is claim-specific. Under Engine §4 rule 3, scripts whose inputs are all
  given before advancement, and calls bounded only by time, keep identical supported events under
  #465. Results needing `busyTicks` cannot be recomputed after #469.

**Q6 — One policy home, no duplication.**

- The revised Rule 7 and the role table restate canonical runtime-contract text.
- Reconciliation adds only the *ownership* rule and its admission triggers (§10). It does not re-state
  custody or finality rules already owned by the runtime contract.

## 9. Final recheck and custody map

**Live `main` at review end:** `a7f9856617d99e796d528a44aa8c15677c485f55`, unchanged from the review
baseline and from the report's baseline. Open PRs: #467 and #468 (Dependabot), unrelated. No material
drift.

| Artifact | Commit | Path | Role |
|---|---|---|---|
| Reviewed report (immutable) | `d2125f39482de3306f2f56e9c7261a3dde160b33` | `workspace/research/investigations/simulation-analytics-ownership-phase-2-post-execution-evidence-revision.md` | Sole target |
| Author pre-registration | `07ec4020e39274e49f690db302376d20c9f0e06b` | `workspace/research/investigations/simulation-analytics-ownership-phase-2-post-execution-evidence-preregistration.md` | Verified chronology and values |
| Author probes and ports | `ac0ac2aa603f82426d7d2c0ec88f3485b9927908` | `workspace/research/experiments/simulation-analytics-ownership-post-execution-evidence/` | Rerun unchanged: 154/0 |
| Review handoff | `3248f93b959c0596de7327eabf0da8dc7ab70b2b` | `workspace/research/handoffs/simulation-analytics-ownership-phase-2-post-execution-evidence-adversarial-review.md` | Assignment |
| Reviewer reconstruction | `d43251eedac326e4a5ac9572fd5d8e214ec2016d` | `workspace/research/investigations/simulation-analytics-ownership-phase-2-post-execution-evidence-review-reconstruction.md` | Pre-report checkpoint |
| Reviewer probe pre-registration | `f8c4ba3a4975b539b3bf60eda4fe2d9c4bcb173b` | `workspace/research/investigations/simulation-analytics-ownership-phase-2-post-execution-evidence-review-probe-preregistration.md` | Committed before probe source |
| Reviewer probes | `cb926538c353bb4c760183cc83846f9b1fc04ed5` | `workspace/research/experiments/simulation-analytics-ownership-post-execution-evidence-review/com/arcogine/research/experiment/PostExecutionEvidenceReviewProbe.java` | R1–R4: 4/4 |
| This review | its containing commit, as returned in the evidence handoff | `workspace/research/investigations/simulation-analytics-ownership-phase-2-post-execution-evidence-adversarial-review.md` | Disposition |

Prior evidence was read at its exact commits:

- original report `6a5c0435`;
- REOPEN review `2c85fe7c`;
- unreviewed revision `75736ddd`;
- Phase 1 `7e72a465`;
- Game report `4ad00136` and Game review `6484795f`.

No earlier artifact was modified, and history was appended only.

**Limitations:**

- The execution-account and Engine-adaptation reports were not re-read; their accepted semantics were
  taken from live canonical text.
- No external source was consulted; none would discriminate an Arcogine ownership question here.
- The bounded absence search covers `docs/` and `product/` at `a7f9856`, not unmerged branches beyond
  open PRs.
- Model-family independence was not achieved.

**What survives and what is repaired:**

- **Phase 1:** the current-boundary classification survives. Q1/Q3 are resolved by #469, Q4 is closed,
  and Q5 stays open.
- **Original REOPEN findings genuinely repaired:**
  - A/C: alternatives are graded and the sentinel/attribution overclaims corrected;
  - B: narrowed;
  - D/J: shared owner deferred and downstream conditional, subject to Q1;
  - E: method-selected versus execution-fixed attribution is now Rule 3/A′;
  - F: finite exception, with `throughput(long)` accounted for;
  - G: extended with three falsified inferences;
  - H and I: re-done against landed state.
- **Unreviewed revision `75736ddd`:** no acceptance is inherited from it. Its Rule 7 is correctly
  withdrawn, and its shared-admission trigger was dropped without a reason that survives Q1.
- **D/E/G recommendation:** nothing materially new forces a change.

## 10. Minimum reconciliation consequences and next action

Only if carried with Q1–Q6:

1. **Owning policy.**
   - Add one ownership rule to the [overview domain authority](../../../docs/architecture/overview.md#domain-authority)
     (not a second copy in the runtime contract). A named analytical method's meaning belongs to the
     capability that fixes choices execution does not fix. It is consumer-local by default, shared only
     on a concrete Rule 4 trigger (two consumers fixing the same method, or one plus a core
     verification/governance requirement with acceptance evidence, or an outward contract, or
     divergent duplicates), and Engine-owned only by an explicit Engine definition change.
   - The runtime contract's "Responsibilities and limits" paragraph already carries custody and
     finality and needs at most a cross-reference.
2. **Engine semantics and Determinism Contract:** no change.
3. **Game consumer plan §4 row, §5 "Visualization and diagnostics", vertical-slice criterion 4.**
   - Replace "not game-owned by default" with game-owned named definitions under the promotion-ready
     discipline (Q2, Q3) and the shared-owner triggers.
   - The definitions themselves are adopted by the separate Game diagnostic reconciliation (Q4).
4. **Governance evidence §12–§13.** Replace "analytical-definition provenance ownership is unresolved"
   with: the definition owner supplies definition identity; holder and attestor supply proof
   provenance. Governance responsibilities are unchanged.
5. **Brief and register.**
   - The analytics ownership question becomes `CONCLUDED` only when that reconciliation lands.
   - Evidence/provenance stays `CANDIDATE`, with the Q1 trigger.
   - Adapter compatibility and cross-revision comparability are unchanged, with the Q5 precision.
   - The Game diagnostic question stays separately `READY`.
6. **Historical decision-rationale record**, as the report suggests. It covers:
   - rejected Rule 7 and the custody-led inverse;
   - A′ "retention, not finality";
   - the B wording;
   - the R1/R2 helper lessons;
   - the snapshot/horizon distinction.

**Next action:** one bounded, independently PR-reviewed reconciliation on this workspace branch,
covering items 1 and 3–6 with Q1–Q6. No further research is required before it. The Game diagnostic
reconciliation stays a separate slice and must evaluate T1–T3 before playable admission.
