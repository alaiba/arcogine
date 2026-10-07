# Simulation analytics ownership boundary — Phase 2 revision: staged admission of measurement and diagnostic semantics

> **Research status:** `READY`. This report revises Phase 2 of the coupled two-phase packet after an
> independent adversarial review returned **REOPEN**. The registered question stays unresolved until
> this exact revision has its own independent review and a separate reconciliation lands. No register
> change is made or implied.
>
> **Revised research baseline:** live `main` `306205c33ef93f0433a26c92f85cbd82eadd27f8`, resolved
> 2026-10-07. It equals the SHA the handoff recorded.
>
> **Final live-main recheck:** live `main` was still `306205c33ef93f0433a26c92f85cbd82eadd27f8` when this
> report was completed (2026-10-07). No change; every repository-dependent conclusion stands as grounded.
>
> **Workspace branch:** `workspace/simulation-analytics-ownership-phase-1`. This report and its
> custody experiment are committed together, on top of the handoff commit
> `392403935dd64cbd88c8bad8f1c6dc643ee0c667`
> (`workspace/research/handoffs/simulation-analytics-ownership-phase-2-revision-after-game-demand.md`).
> The previously reviewed report and its review are not modified.
>
> **Risk:** High — major ownership, supported observable semantics, deterministic Engine behavior.
>
> **Authority:** Research evidence only. This report changes no architecture, specification, runtime
> contract, Determinism Contract, planning, register, brief, or product code. It does not perform the
> owner-decided `busyTicks` removal.
>
> **Adversarial-review status:** required, not yet performed. Only the author's self-challenge (§17)
> applies to this revision. The REOPEN disposition binds to the original report revision; it does not
> transfer to this one, and nothing here is decision-quality evidence for architecture or planning
> promotion until a new, genuinely independent review of this exact revision exists.

## 1. Exact prior evidence consumed

Every artifact was resolved at the exact commit below, in full, before use.

| Artifact | Workspace | Commit | Path | Role here |
|---|---|---|---|---|
| Phase 1 report (qualified revision) | `workspace/simulation-analytics-ownership-phase-1` | `7e72a46506bf63febcb57d7b7dcc9ce0cd484d64` | `workspace/research/investigations/simulation-analytics-ownership-phase-1-current-boundary.md` | Current-boundary classification; surviving facts in §4 |
| Original Phase 2 report | same | `6a5c043563e3bd89fae6826f830b377b43643d46` | `workspace/research/investigations/simulation-analytics-ownership-phase-2-admission-boundary.md` | Reopened; its surviving conclusions are kept where §5 says so |
| Original Phase 2 custody experiment | same | `6a5c043563e3bd89fae6826f830b377b43643d46` | `workspace/research/experiments/simulation-analytics-ownership/com/arcogine/research/experiment/PerformanceEvidenceSufficiencyExperiment.java` | Rerun at this baseline (§8) |
| Phase 2 adversarial review | same | `2c85fe7c03c6256c9e8a2ce73fde5091b3357e3c` | `workspace/research/investigations/simulation-analytics-ownership-phase-2-adversarial-review.md` | **REOPEN** — binding; every load-bearing reason is closed in §5 |
| Game diagnostic-evidence report, revision 3 | `workspace/factory-design-game-diagnostic-evidence` | `4ad00136441fa29e6b462ac5431b60cf2de1e148` | `workspace/research/investigations/factory-design-game-diagnostic-evidence-report.md` | Admission evidence (§6) |
| Game diagnostic-evidence review | same | `6484795ff4c4ad971e06c70751b03805e54a55f5` | `workspace/research/investigations/factory-design-game-diagnostic-evidence-adversarial-review.md` | **ACCEPT WITH QUALIFICATIONS** — its qualifications are binding (§6) |
| Game research-local definitions | same | `4ad00136441fa29e6b462ac5431b60cf2de1e148` | `workspace/research/experiments/game-diagnostic-evidence/com/arcogine/research/experiment/BakeryInspectionPack.java` | The exact `utilization-over-a-period` and `flow-characterization-of-idle-slots` definitions under test |
| Revision handoff | `workspace/simulation-analytics-ownership-phase-1` | `392403935dd64cbd88c8bad8f1c6dc643ee0c667` | `workspace/research/handoffs/simulation-analytics-ownership-phase-2-revision-after-game-demand.md` | Task definition |

The game report path is untouched between its report commit and its review commit, so the review
binds to the file read here. The game review reran the game experiment at this report's baseline
(156 tests, 0 failures, every output byte-identical), so it was read here, not rerun.

## 2. Baseline and grounding

**Baseline.** `306205c3` is also the game report's final recheck and the game review's baseline. Since
the REOPEN review's baseline (`c304b14a`) `main` gained #455, #457, #458 and #459. They change tests,
plus these main sources: unreachable-guard removal in `FactoryHandler` and `FactoryModelCanonicalForm`,
removal of a redundant `JobId` tie-break in job-observation ordering (ordinals are unique within an
order), removal of an unused mutable queue accessor on `Machine`, one simplification in Governance's
`EvidenceConformanceEvaluator`, Storage changes, and one visibility change in `ExperimentRunner`. No
Engine rule, supported observation or supported event changed. No document under `docs/`, `AGENTS.md`
or the agent contracts changed in that range.

**Read in full at the baseline:**

- `AGENTS.md`, the Researcher contract, `docs/development/researching.md` and the report template;
- the register;
- the briefs for the analytics ownership boundary, evidence/provenance, game diagnostic evidence and
  game strategy space;
- the game consumer and vertical-slice plans;
- the architecture overview and runtime contract;
- Engine semantics §1–§4 and §9–§14;
- Governance evidence §5–§13; Factory Design §7, §10 and §11's opening; Product Charter §6 and §11;
- the Challenge comparison and evaluation sources;
- the whole `research-experiments` main source set.

**Read in the relevant part:**

- `spatial-runtime-consequences.md` (ownership note, KPI acceptance, transfer read model);
- the `FactoryRuntime`/`FactoryHandler` observation, event-payload and crediting code.

**Inspected through search matches:**

- Governance conformance, the ISA-95 mapping and standards alignment (measurement rows);
- the readiness plan's KPI note;
- the adapter-compatibility, cross-revision and engine-evolution briefs.

**Semantic-neighbor search** over `docs/` and `product/` for `RuntimeObservation`,
`RuntimePerformanceObservation`, `busyTicks`, occupancy, utilization, starvation, waiting by step,
bottleneck/constraint, active period, co-binding, excess capacity, `AttemptComparison`, producer
provenance, verification objective, analytical definition, availability/downtime and
transfer/blocking/setup. Every load-bearing match was opened at the baseline. Within that scope:

- no production producer computes any normalized utilization, occupancy, starvation or bottleneck
  measure;
- no implemented consumer evaluates a utilization verification objective;
- Governance analytical evidence is fixture-only (`governance-evidence.md` §12);
- the only executable utilization-like derivations are research-local substrate oracles and the
  game workspace's definitions.

This is a bounded absence claim, not a proof that no future requirement exists.

**History checked for one claim.** `busyTicks` accumulation was added by #241
(`e468e826087b699c5bdca835db362bac706f079e`) as "one minimal production change", so that a headless
acceptance test could assert that "the active production bottleneck is identifiable from
ResourceObservation facts alone, by carried load and by utilization". Before that commit the counter
was never accumulated.

**Not inspectable.** No outward adapter, verification-objective consumer or executable transfer
consumer exists, so those consumers could not be inspected. These are capability absences, not
skipped surfaces.

## 3. Question, decision at stake, scope

**Question (from the handoff).**

> Given the established current Engine/runtime ownership boundary, the concrete game measurement
> demands, and the already-visible near-term diagnostic horizon, what prospective ownership/admission
> rule should Arcogine use for new simulation measurement and diagnostic semantics so that it is
> justified by current demand but does not require ownership restructuring as the next diagnostic
> concepts are admitted?

This is the brief's Phase 2 question ("should any reusable consumer-neutral analytics responsibility
exist, and what minimum ownership rule separates it from Engine semantics?"). It is sharpened by a
concrete consumer and an anti-rework test, and its scope is unchanged.

**Decision at stake.**

1. When a new measurement may become Engine-owned.
2. When shared consumer-neutral ownership is justified.
3. Where the two concrete game measurements belong now.
4. What changes, if anything, for current supported fields.
5. Which downstream questions become promotable.

The game plan's generic-analytics restriction and the vertical-slice gate's criterion 4 wait on
this decision. So do Governance's unresolved analytical-definition provenance ownership and the
spatial plan's ownership note on the pinned derived results.

**Out of scope.** Final formulas for utilization, flow characterization or bottleneck methods;
analytics API or module design; retention, provenance, adapter and cross-revision contracts;
performing the `busyTicks` removal; architecture, planning or register edits; independent review.

**Risk classification.** High, per the brief. That required:

- broader semantic-neighbor search;
- explicit falsification analysis of every candidate;
- reruns of existing executable evidence at the baseline;
- one discriminating experiment;
- a mandatory independent review before promotion.

## 4. Surviving Phase 1 facts

These are taken from Phase 1 and the original Phase 2 report, are unchallenged by the REOPEN review
(§H, Q1–Q6), and were rechecked at this baseline (code and text unchanged in substance, §2):

1. Current supported performance results and `busyTicks` are Engine-owned because accepted contracts
   **declare** them supported Engine results (Engine semantics §1.1, §10–§10.2). No dispatch or
   selection path reads them. Placement is not ownership, in either direction.
2. `combinedQueueDepth` is result-affecting (an exact ranking key, §2 rule 3). It is unexposed and is
   Engine-owned under every candidate. It is not a physical queue count.
3. `backlog`, `completedOrders`, `averageLeadTime` and `throughputPerTick` recompute bit-exactly from
   the same fresh observation. Rerun here: experiment A, 23 observations across 14 fixtures.
4. `busyTicks` and `completedSalesValue` are not determined by the rest of a fresh observation (plus
   the model, for `busyTicks`). Complete supported events reconstruct both. Rerun here: B, C and C′.
5. `busyTicks` is completion-credited processing job-ticks, summed across slots and saturating. It is
   not occupancy and not a normalized utilization ratio. It equals total processing job-ticks once
   nothing is running and nothing has saturated.
6. `queueDepth` is a machine's own FIFO queue only. Pre-binding multi-eligible waiting is projected
   separately as `pendingWork`, and is attributed to the operation step first.
7. The order-level results do not exhaust diagnosis (D1). A multi-variable outcome delta is not the
   sum of its single-variable deltas (D3).
8. The runtime contract's "Performance: … utilization facts" and Engine semantics' "`busyTicks` /
   utilization" wording is ambiguous: no normalized ratio is produced.

## 5. The prior REOPEN findings and their resolution

| # | Reopening reason | Resolution in this revision |
|---|---|---|
| 1 | **Omitted candidates.** The staged/demand-driven model and an explicit Engine-owned standardized measurement were not evaluated | Both are evaluated as first-class candidates D and A (§9). A also gets its strongest form, an execution-fixed occupied-slot-ticks integral (A′, §9). The surviving rule is D refined by E (§10) |
| 2 | **Strict facts-only overclaim** | Restated: experiments B/C/C′ falsify removing the history-carrying aggregates **under the current supported evidence contract** (fresh observation plus model, no retained history). They do not show impossibility under a different evidence contract with retained events, more projections or an outside accumulator fed from run start; such a contract trades loss for a capture owner. The owner's `busyTicks` removal chooses that trade for one field (§12) |
| 3 | **Engine-rich characterization** | Each proving class says whether Engine ownership is falsified or merely costly (§9.1). Only comparison is structurally outside Engine. Everything else is a cost/requirement judgment. The §10.1 sentinels bind only the named existing results; a new Engine measurement could define refusal |
| 4 | **Shared analytics prematurity** | The game demand is used as admission evidence (§6). Under the stated shared-owner test it does **not** yet cross the shared threshold: one concrete consumer, an anticipated but unspecified second use (§11.2). Shared ownership is **staged**, not admitted |
| 5 | **R1(c) attribution wording** | Replaced by Rule 2(b) and the attribution distinction in §10. Aggregation along *authoritative* identities and transitions (the machine that performed a step) differs from *analytical* attribution a method chooses (pre-binding waiting assigned to a machine, delay assigned to a cause, outcome assigned to one of several changes). Execution-fixed populations differ from method-selected ones |
| 6 | **Legacy current fields** | `averageLeadTime`, `throughputPerTick` and the caller-windowed `throughput(long)` helper are a deliberate, finite **compatibility exception**: retained declared results, revisitable on stated triggers. This is not a staged migration, and not evidence that the rule needs refining (§12) |
| 7 | **Anti-second-engine rule** | Every horizon concept is checked against an explicit "interpret recorded outcomes / recreate execution logic" boundary (§13) |
| 8 | **Q1–Q6 closure** | Updated only where the game evidence or the owner decision changes them (§14) |
| 9 | **Contract-change accounting** | Current semantic changes, prospective admission policy and implementation factoring are listed separately (§15) |
| 10 | **Downstream triggers** | Evidence/provenance is **not** promoted. The exact trigger is restated per responsibility (§15.3) |

The review's retained qualifications are carried forward:

- the fresh-observation input restriction (B/C hold for that input set only);
- the finite-arithmetic limits: a saturated `busyTicks` loses exact totals, so "busyTicks plus running
  starts recovers occupancy" holds only below saturation;
- floating-point completion-order accumulation needs an explicit policy in any new definition;
- the utilization-support ambiguity (§14 Q1);
- the public-operation support question (§14 Q5);
- the spatial rechecks (§16, §18);
- the non-causal comparison limits (§13).

## 6. Concrete-demand analysis (admission evidence)

### 6.1 What the game evidence admits

The accepted game research, read **with** its review, supplies two concrete forcing use cases:

1. **Utilization over a stated period.** How much of a machine's available slot capacity worked over
   a chosen period?
2. **Idle-flow characterization.** When a slot is idle, is relevant work still to come, is there none
   left, or can the evidence license nothing stronger?

They prove that Arcogine has a concrete consumer that needs analytical measurement beyond the
Engine's declared results, so the ownership question is not speculative. The binding review
qualifications limit what they prove:

- reuse beyond the game is **anticipated, not demonstrated**;
- utilization's second use, Factory Design's "Maximum utilization ≤ threshold" verification
  objective, is an illustrative classification with no consumer;
- flow characterization's reuse rests on unverified background;
- the counter's removal is not reuse evidence;
- game-local presentation remains a live candidate;
- a single-run bottleneck method has no demonstrated game need;
- the research-local definitions are requirements "as defined by their eventual owner", not
  corpus-audited production definitions.

The game diagnostic study stopped for research-management reasons. It did not decide that utilization
and starvation exhaust the desired concepts, so its wider concept map is legitimate **design**
evidence (§11).

### 6.2 Decomposition by classification dimension

Implementation placement is not ownership. The question is which party's requirement fixes each
layer.

| Dimension | Utilization over a period | Idle-flow characterization |
|---|---|---|
| Authoritative fact / state / change | Dispatches, step completions and availability changes, with their times (Engine) | Job status and current step; machine active jobs, concurrency, online state; queue membership (Engine) |
| Current supported projection | `ResourceObservation.activeJobIds`, `concurrency`, `state`; supported events (Engine) | Same, plus `pendingWork` and job projections (Engine); routing eligibility (Factory model) |
| Measurement definition | Period convention (half-open versus inclusive), per-slot versus per-machine-busy denominator, work in progress through the boundary, per machine versus per eligibility pool, offline treatment, later admission load | Aggregation into starved slot-ticks over a period; cap by remaining eligible work; pool basis for a machine eligible for several steps |
| State characterization | — | starved / no work left / **idle with eligible work queued** (reachable after an availability change); "known work" rule once orders arrive over time; "blocked" only when finite buffers exist |
| Inference method | **None.** Never a bottleneck verdict | **None** by itself. "Surplus" or "not needed" is an intervention claim, not a characterization |
| Comparison / experimental method | Across machines or variants, under one named definition | Across variants, under one named definition |
| Consumer interpretation / presentation | Teaching text, labels, period selection (game) | Teaching text, vocabulary (game) |
| Evidence retention / completeness | Occupancy for every tick of the period: per-tick observation, or complete dispatch/completion events from before the period; availability history if offline time is excluded; refusal when uncovered | One observation per state change in the period; refusal for unsupported categories |

Every layer above the "measurement definition" row is already supplied by the supported runtime
contract. Every layer from that row down is a **choice execution does not fix**. Experiment 1 (§8)
shows it on one run: the same evidence gives several legitimate values, and even different rankings
of which machine is "most utilized".

## 7. Design evidence: the horizon used for falsification

As the handoff requires, the horizon is used only to falsify brittle rules:

- **evidenced by the game corpus:** waiting by step, occupancy/timelines, excess capacity or
  "needed", run comparison;
- **near-term possible:** a bottleneck named method, a shifting bottleneck / active period,
  co-binding;
- **future semantic horizon (compatibility only):** availability-aware capacity, release
  policies/arrivals, multiple products, finite buffers/blocking, setup/changeover, transfer/layout
  decomposition, stochastic behavior.

No formula is designed for any of them.

## 8. Executable evidence

### 8.1 Rerun of existing evidence

The documented `gradle:9-jdk21` Docker workflow (OpenJDK 21.0.12, repository wrapper Gradle 9.8.0)
ran against a `git archive` export of the baseline's `product/` tree, with `--no-build-cache`:

- the prior Phase 2 custody experiment: **6 tests, 0 failures**. A, B, C, C′, D1 and D3 reproduced
  their recorded values exactly: A 23 observations / 14 fixtures; B 40.0 vs 80.0 and late-join 20.0
  vs 40.0; C `{5,0}` vs `{0,5}`; D1 identical performance with waits `{CUT 48}` vs
  `{CUT 6, ASSEMBLE 42}`; D3 −16, +5, −5;
- the whole tracked `:research-experiments:test` suite plus both custody experiments, with
  `--rerun-tasks`: **146 tests, 0 failures, 0 errors, 0 skipped** (137 tracked, 9 custody).

That suite includes `ProcessingOccupancyOracleTest`, `EligibilityPoolOccupancyOracleTest`,
`WaitingWorkByStepOracleTest`, `DispatchProfileOracleTest` and `CapacityCorpusTest`. They pin:

- occupancy from events, and its refusals;
- per-resource versus per-pool rankings;
- step-first waiting;
- per-step waits;
- the occupancy-is-not-constraint counterexample.

This is a research-evidence run, not a full quality or coverage gate.

### 8.2 New custody experiment

**Source:** `workspace/research/experiments/simulation-analytics-ownership-revision/com/arcogine/research/experiment/AdmissionBoundaryDiscriminatorExperiment.java`,
committed with this report. Run with:

```text
cd product && ./gradlew :research-experiments:test \
  -PresearchExperimentSources=<that directory> \
  --tests 'com.arcogine.research.experiment.AdmissionBoundaryDiscriminatorExperiment'
```

**Result: 3 tests, 0 failures**, on the first run and again in the full-suite run. Every expected
value was hand-derived from Engine semantics §2 and §4 before the first run; no assertion was changed
after running. Every derivation reads only supported observations, supported events and the
published model, and none ranks candidates. It defines no production measurement.

| Test | Discriminates | Result |
|---|---|---|
| 1 `oneEvidenceSetFeedsSeveralLegitimateUtilizationDefinitions` | A (Engine fixes one utilization) vs outside named definitions over Engine evidence | MIX 2 / BAKE 4 on a 2-slot Oven / PACK 1, six units, finish 17. Per-tick observation and dispatch/completion intervals agree on Oven occupied slots per tick: `0 0 1 1 2 2 2 2 2 2 2 2 2 2 1 1 0`. That quantity is execution-fixed. The definitions over it disagree: half-open [4,14) 20/20, inclusive [4,14] 21/22; whole-run per-slot 24/34, machine-state-`Busy` fraction 14/17. **Per-slot ties the Mixer (12/17) with the Oven (24/34); `Busy` ranks the Oven above the Mixer.** `busyTicks` is 8 at tick 9 against 12 occupied, and 24 = occupied at quiescence |
| 2 `afterAnAvailabilityChangeAnIdleSlotCoexistsWithWorkQueuedForIt` | Whether a third idle state needs Engine re-decision; whether completeness/refusal belongs to the definition | Oven taken offline, four units submitted, Oven back online at tick 0. It shows `Busy`, 1 of 2 slots active, own `queueDepth` 3, no pending work. It finishes at **16 against 8** always online (§4 rule 7 starts one queued job per trigger). From observation plus model alone, over [0,16): working 16, **idle-with-eligible-work-queued 12**, no-work-left 4 slot-ticks; no dispatch rule was consulted. The research-local continuously-online occupancy **refuses** (availability changed), while an offline-excluding per-slot basis reports 16/32 although work waited throughout. This reproduces the game review's probe 1 executably |
| 3 `starvedSlotCountingDependsOnADeclaredCap` | Whether starved accounting is fixed by execution | MIX 4 → PACK 1 on a 2-slot Packer, one unit. Over [0,4) uncapped starved slot-ticks 8; capped by units still needing the machine, 4; no-work-left 1 |

**Range limits.** These are finite constructive cases on always-online or single-toggle,
single-product, linear, release-at-once runs. They show that alternatives exist and that the
evidence suffices. They select no definition.

## 9. Candidate ownership models

The candidate set was reconstructed from the handoff, the REOPEN review's independent
reconstruction, and the evidence above. A′ is added because it changes an ownership decision: it is
the only Engine-side form that serves arbitrary-period utilization without moving policy into
Engine.

| | Shape | Current fields | New measurement default |
|---|---|---|---|
| **A** Engine-rich / standardized | Engine owns current results and grows standardized measurements, such as normalized utilization or occupancy, when a cross-consumer or runtime-contract requirement justifies one fixed definition | All retained | Engine, when justified |
| **A′** Engine-supplied execution-fixed aggregates | A restricted to aggregates whose definition execution fixes, for example occupied slot-ticks to date including running work, or online slot-ticks to date. Consumers difference two observations to get any period; normalization, period, grouping and refusal stay outside | All retained | Aggregate in Engine; policy outside |
| **B** Universal outside-Engine | Engine owns execution facts, result-affecting interpretation and justified current projections; **all** report-only measurement policy lives outside, and no new Engine measurement is ever admitted (the original report's R2) | Retained | Outside, unconditionally |
| **C** Shared now | A consumer-neutral semantic owner (not necessarily a module) defines utilization, characterization and similar measurements now; Engine supplies facts; the game owns presentation | Retained | Shared owner |
| **D** Staged / demand-driven | New measurement stays consumer-local until it must join the Engine contract or several consumers need one definition | Retained | Consumer-local |
| **E** Hybrid by semantic kind | Different classes have different destinations: execution facts → Engine; one consumer's characterization → consumer; a reusable named measurement → shared; a contractual Engine-wide result → Engine; cross-run interpretation → consumer or named method | Retained | Depends on kind and trigger |

### 9.1 Evaluation against the proving classes

"Falsified" means a proving case contradicts the model's semantics. "Cost" means the model is viable
but carries a stated price that the present requirements do not justify.

| Proving class | A | A′ | B (universal) | C | D | E |
|---|---|---|---|---|---|---|
| Utilization over a stated period (concrete) | **Cost.** Arbitrary past periods need Engine-retained occupancy history, a runtime-contract retention change, or a caller-parameterized query, which has no precedent beyond the ambiguous `throughput(long)`. It also fixes at least five basis choices that test 1 shows are not fixed by execution and that legitimately differ, so it suppresses alternatives or turns Engine into a measurement library. No requirement demands an Engine-wide meaning | Viable; serves every definition by differencing at observation boundaries. **Not needed now**: the game captures complete evidence itself. Admissible on a concrete late-join requirement | Viable for this case | Viable; one consumer | Viable: game-owned named definition | Viable |
| Idle-flow characterization (concrete) | **Cost.** Engine would classify its own idle slots with routing lookahead plus cap, pool and known-work choices. The third state (test 2) is reachable state, not a reason to put the classification in Engine | Not applicable; it is a state classification, not an aggregate | Viable | Viable; one consumer | Viable | Viable |
| Bottleneck / shifting / co-binding methods | **Cost, plus one falsified instance.** No method is required. Methods are plural, and the overview's claim that the bottleneck is identifiable "by carried load and by busy-tick utilization" — an inference leaked into the Engine-contract description — is falsified (strategy-space qualification 2; game G5/G7; review 4.5) | Not applicable | Viable | Premature: no method has a second consumer | Viable | Viable |
| Excess capacity / "needed" | **Falsified as an Engine measurement**: needed-ness is not a single-run quantity (G2 vs G8; review probe 2 shows single-removal verdicts do not compose). Engine supplies re-execution, not the verdict | n/a | Viable | Viable | Viable | Viable |
| Run comparison | **Structurally outside Engine.** One run is one evaluation of the interpretation, and comparison across runs is consumer-owned (Determinism Contract rule 5; Governance evidence §7). Change identification is already Factory/Governance-owned | n/a | Viable | Viable for qualification only | Viable | Viable |
| Late-join run-to-date aggregate need (Phase 2's PC-5, at the current contract) | Viable | **Viable — its natural home** | **Fails at the current evidence contract**: a requirement for history-carrying aggregates in the fresh observation cannot be met outside Engine without a retention/capture contract (B/C). B forbids the Engine answer categorically | Viable, with capture contract | Viable if the rule includes an Engine-admission test | Viable |
| A measurement that starts to drive dispatch (a future policy) | Viable | Viable | Viable (result-affecting stays Engine) | Must move to Engine | Must move to Engine | Viable |

**Verdicts.**

- **A** is *not falsified as a model*. It is rejected as the **default** destination, on present need
  and on concrete costs: history retention, and suppression of definitions shown here to differ
  legitimately. It survives as a destination whose admission is triggered (Rule 2(c)).
- **A′** survives as the Engine-side form of a triggered admission (Rule 2(b)). No current
  requirement triggers it.
- **B as a default** coincides with the surviving rule for every current decision. **B as a
  universal prohibition** is not supported: it upgrades the coupling cost into a ban, and it has no
  Engine answer for a concrete late-join requirement at the current evidence contract.
- **C** is *not falsified*. It is **unsupported by present need**: one concrete consumer, and an
  unimplemented second use whose basis choices are unspecified (§11.2). Creating the owner now would
  violate the Charter §6 requirement of a named requirement with acceptance evidence.
- **D** survives as the admission **mechanism**. Unrefined, it lacks an Engine-admission test, a
  kind-specific shared threshold, and the definition discipline that makes promotion rework-free.
- **E** survives as the **classification**. Unstaged, it would create shared owners ahead of
  demand.

The surviving rule is therefore **D refined by E** (§10). It differs from the reopened report's
package in three ways:

1. it admits Engine measurement by trigger rather than banning it;
2. it withholds the shared owner;
3. it assigns the concrete game measurements to the game now, under a promotion-ready discipline.

No further candidate changes an ownership decision. In particular:

- "a Governance or verification capability owns utilization" is a possible **destination** for a
  triggered shared owner (§11.2), not a different rule;
- "analytics as a Factory sub-definition" is a placement choice.

## 10. The surviving rule: staged admission by semantic kind

Proposed, not adopted.

**Rule 1 — Engine owns execution truth and the evidence to measure it.** That is:

- authoritative execution state and change;
- every rule that can change acceptance, rejection, assignment, ordering or timing, whether or not
  its quantity is exposed;
- the supported projections and events that let a consumer observe current state and ordered
  change;
- the exact arithmetic of every derived result it declares supported, while it is declared.

When new execution semantics land, Engine owns the new **facts** they create, because no other party
can supply them.

**Rule 2 — Engine-owned measurement is admitted by requirement, not by possibility.** A new
report-only derived result becomes Engine-owned only when one of these holds:

- **(a) It participates in authoritative execution.** Then it is Rule 1 semantics, and Engine
  ownership is mandatory.
- **(b) It is an execution-fixed aggregate** along authoritative identities and transitions:
  current or run-to-date, with no other interval, normalization basis, statistic, method-selected
  population or analytical attribution. In addition, a concrete requirement must need it in the
  fresh observation because a consumer cannot otherwise obtain it without retained history.
- **(c) A concrete requirement demands one Engine-wide definition** in the supported runtime
  contract for every consumer. In addition:
  - its completeness/refusal semantics must fit the observation contract;
  - its coupling cost is accepted explicitly: every change becomes an Engine definition change,
    with specification and fixtures, under §1 of Engine semantics;
  - differently defined measurements stay admissible elsewhere under other names.

This is a trigger, not a ban. Recomputability neither requires nor forbids it.

**Rule 3 — Other measurement, characterization and inference belong to whoever fixes their
definition.** A value whose definition needs a choice execution does not fix belongs to the party
whose requirement fixes that choice. Such choices are: period or interval, capacity or normalization
basis, statistic, population, grouping or pool, analytical attribution, classification rule,
inference method, refusal rule, cross-run scope. The default owner is the consumer that needs it
(**consumer-local**). Two components of one consumer — for example the game's diagnostics and its
Challenge scoring — are one consumer.

**Rule 4 — Shared consumer-neutral ownership is admitted by trigger.** A definition moves to, or
starts in, a consumer-neutral semantic owner when any of these holds:

- (a) two concrete consumers require the **same named definition with the same meaning**;
- (b) one concrete consumer and one concrete core verification/governance requirement must share it
  — for example a verification objective that is actually evaluated, or a Governance assertion that
  relies on the result — with the named requirement, owning capability, bounded responsibility and
  acceptance evidence the Charter §6 asks for;
- (c) a supported outward contract must promise the same definition across consumption boundaries;
- (d) two existing implementations of one meaning have demonstrably diverged, or create a concrete
  correctness or compatibility risk.

Anticipated reuse, a category name, a consumer *count* (overview §Attribution: "a consumer count is
not a threshold") and removal of an Engine field are not triggers. The shared owner is a semantic
role; its placement is chosen with the requirement that triggers it.

**Rule 5 — Every non-Engine definition is promotion-ready from the start.** Consumer-local or
shared, every such definition:

- is **named**;
- states its exact method and **every basis choice**;
- declares only supported inputs: the published model, supported observations, and gap-free
  supported-event ranges;
- states its evidence-completeness condition, and refuses or applies a stated basis when it is
  unmet;
- is never presented as an Engine fact, and never reuses the name of an Engine-declared result for a
  different meaning;
- is separable from its wording and presentation.

A change of meaning is a new definition under a new name. Promotion then transfers custody of an
unchanged definition; it changes neither the definition, the Engine boundary, nor this rule.

**Rule 6 — Measure outcomes; never re-decide execution** (§13).

**Rule 7 — Evidence custody follows the definition owner.** The owner captures the complete
evidence window its definition needs, by retaining drained supported events or by observing at the
boundaries it needs, and carries the provenance its uses require. Engine retains no history by
default. A requirement that Engine retain history is a separate runtime-contract change. A reusable
evidence/provenance contract is needed only when a definition is shared (Rule 4), or when its results
cross a boundary its producer does not control (§15.3).

**Rule 8 — Placement and factoring never transfer ownership** (unchanged from Phase 1).

**Attribution distinction (closing reopen 5).** Engine-owned aggregates may aggregate only along
**authoritative attribution**: facts execution fixed, such as which machine performed a step, which
order a job belongs to, which step a job waits for with its captured eligible set, and which
destination a bound job waits at. **Analytical attribution** — assigning pre-binding shared waiting
to a machine, delay to a cause, capacity to one step of a shared resource, or an outcome change to
one of several changes — belongs to a named method under Rule 3. Likewise, a population fixed by
execution (completed orders) differs from a method-selected one ("orders in period", "after
warm-up").

**Checks against the brief's worked examples.**

- `combinedQueueDepth`: Rule 1. ✓
- Interval utilization: Rule 3, and test 1. ✓
- A fresh current-state summary may stay Engine-owned: Rule 1 or 2(b). ✓
- Factoring keeps ownership: Rule 8. ✓
- No reconstructing or re-deciding: Rule 6. ✓

## 11. Admission tests and the classification of the concrete requirements

### 11.1 Engine-owned admission test, applied

| Test | Utilization over a period | Flow characterization | Run-to-date occupied slot-ticks (A′) |
|---|---|---|---|
| Participates in execution? | No | No | No |
| Required for all Engine consumers by a supported contract? | No such requirement found (§2) | No | Not now. It would be under a late-join requirement |
| Is one fixed definition integral to the Engine interpretation? | No: five or more basis choices, none fixed by execution (test 1) | No: cap, pool and known-work choices (tests 2–3, review 4.3) | Yes: the integral of active jobs over time is fixed by execution, and two evidence routes agree (test 1) |
| Would legitimate alternatives be suppressed? | Yes (test 1: values and rankings differ) | Yes | No; all definitions derive from it |
| Do completeness and refusal fit the observation contract? | Arbitrary past periods do not without retained history | Partially: per observation, yes; aggregation over a period needs history | Yes; current-state cumulative |
| Is the coupling justified by a concrete requirement? | No | No | Only when a late-join requirement exists |
| **Verdict** | **Not Engine now** | **Not Engine now** | **Admissible on trigger; not admitted now** |

### 11.2 Shared-owner admission test, applied

| Trigger | Utilization over a period | Flow characterization |
|---|---|---|
| (a) Two concrete consumers, same named definition | **Not met.** Only the game is concrete. Historical KPI consumers were retired and are not evidence of a present requirement | **Not met** |
| (b) Consumer plus core verification/governance requirement | **Not met yet, and the most likely trigger.** Factory Design §10.2 lists "Maximum utilization ≤ threshold" as a shared verification concern, and Governance §11 keeps verification-objective semantics domain-owned. But no objective is modeled or evaluated, and its basis choices are unspecified, so whether it would want the game's definition is unknown | **Not met**; no core requirement names it |
| (c) Outward contract | **Not met**; no outward adapter | **Not met** |
| (d) Divergent duplicates | **Not met**; one implementation | **Not met** |
| Null hypothesis: one concrete consumer is insufficient | **Holds** | **Holds** |

**Thresholds by kind.**

- **Measurement and characterization** promote on (a)–(d). A characterization's dependence on Engine
  state semantics, such as the always-online property behind the two-way classification, must be
  stated, and rechecked whenever Engine semantics change.
- **Inference methods** promote only on (a)–(c) **for the same named method**. One consumer's method
  choice never becomes "the" bottleneck.
- **Comparison qualification** stays with the consuming use (Determinism Contract rule 5; Governance
  §7) unless (a) applies.

### 11.3 Classification of the concrete game requirements

**Shared consumer-neutral ownership: staged. Not admitted now, not rejected.**

1. **Utilization over a stated period** is a **game-owned, consumer-local, named measurement**
   (Rule 3).
   - It is subject to Rule 5: every basis choice in §6.2 is stated, including offline treatment
     before any availability-changing scenario is used.
   - Under Rule 7, the game owns its evidence capture: per-tick observation, or retained complete
     events.
   - Engine supplies the evidence unchanged.
   - It promotes to a shared owner on Rule 4(b) if a utilization verification objective is actually
     modeled and evaluated with the same meaning. If that objective chooses a different basis, the
     two definitions coexist under different names.
2. **Idle-flow characterization** is a **game-owned, consumer-local, named characterization**,
   subject to Rule 5 with the review's corrections:
   - a third category, **idle with eligible work queued**, or an explicit refusal for that state;
   - a declared starved-slot cap;
   - a declared pool basis for machines eligible for several steps;
   - a "known work" rule before "no work left" is claimed once orders can arrive over time;
   - the always-online scope of the two-way version stated as a scope, not an Engine property.

   It must not claim *why* the Engine left a slot idle (§13).

Neither requirement is promoted to production semantics by this report. The research-local
definitions remain feasibility evidence.

## 12. Current-field consequence matrix

| Item | Kind under the rule | Consequence | Change category |
|---|---|---|---|
| `combinedQueueDepth` | Result-affecting interpretation (Rule 1) | Unchanged; never presentable as a physical queue | None |
| `queueDepth`, `activeJobIds`, `state`, `concurrency`, `pendingWork`, order/job projections, metadata | Engine projections (Rule 1) | Unchanged | None |
| `backlog`, `completedOrders` | Execution-fixed current-state count and run-to-date count (Rule 1/2(b) shape) | Retained | None |
| `completedSalesValue` | Execution-fixed aggregate along authoritative completions and accepted terms; an overview "operational measure" | Retained | None |
| `averageLeadTime` | Policy-bearing: mean over the completed population, empty-set sentinel | **Compatibility exception**: retained as declared, with exact current arithmetic | None now. Revisit on any proposal to change its formula or sentinel, on a consumer needing a different lead-time statistic, or at the `busyTicks` reconciliation |
| `throughputPerTick` | Policy-bearing: rate over the observation clock, zero-window sentinel | **Compatibility exception**, as above | None now |
| `FactoryRuntime.throughput(long)` | Caller-windowed Engine arithmetic | Inside the exception accounting; method-level support stays open (Q5) | None now |
| `busyTicks` | Execution-fixed aggregate (completion-credited along the performing machine) | **Owner decision: remove.** Consistent with the rule: Rule 2(b) would admit such an aggregate only on a concrete fresh-observation requirement, and none exists. The rule does not need refining | Owner-decision change, outside this research: Engine semantics (§10, §10.1 rule 1, §10.2 row, §14 item 15) **and** runtime contract (projection field; "utilization facts") narrowing, with the exhaustive scope of game-review qualification 8 |
| Runtime contract "utilization facts"; Engine semantics "`busyTicks` / utilization" | Ambiguous wording (§4.8) | Resolved by the removal. If removal were not reconciled, the obligation must be resolved explicitly, not by an "editorial" pass | Part of the owner-decision change |
| Engine semantics §10: "existing bottleneck/capacity interpretation", "Bottleneck interpretation must therefore widen" | An inference described as Engine-owned | Restate: Engine exposes capacity and admission-load facts; any bottleneck interpretation is a named method outside Engine (Rules 3/6). The spatial plan's "visible enough for supported bottleneck diagnosis" already reads that way | Textual clarification of an unexecuted spatial clause. No value, rule or fixture changes |
| Overview headless-closure paragraph: bottleneck identifiable "by carried load and by busy-tick utilization" | A fixture's finding stated as a general capability; falsified as general | Scope it to its fixture, or remove it, in both halves | Correction of an over-general architecture claim |

**How the owner decision affects the prospective rule.** It needs no refinement of the rule. It
applies Rule 2(b) to an existing field, finds no requirement, and knowingly accepts the information
loss experiment C shows: a late joiner can no longer recover processing totals.

The counter's own history is also Arcogine evidence for the rule's central clause. It was added to
the Engine contract to support an interpretation claim, not a requirement (§2), and that interpretation
was later falsified — exactly the rework that admission by requirement prevents.

The removal itself is reconciled on the owner-decision route of `researching.md`, not as adoption of
this report.

## 13. Anti-second-engine analysis

**Boundary.**

Outside-Engine definitions **may**:

- read supported facts;
- aggregate along authoritative identities;
- apply declared methods to **recorded** outcomes;
- compare **actual** executions;
- request Engine re-execution under changed explicit inputs for any counterfactual.

They **must not**:

- evaluate the Engine's selection, ranking, admission or cascade rules to determine what Engine
  would or should have done;
- reconstruct unexposed intermediate cascade state;
- simulate forward from an observed state;
- assert an Engine-internal cause for an observed state.

They may describe the state, and may cite the specification as context.

| Concept | Interprets recorded outcomes (permitted) | Would recreate execution logic (forbidden) |
|---|---|---|
| Utilization, occupancy, timelines | Intervals from actual dispatch/completion, or per-tick active jobs (test 1) | Inferring which slot "should" have been busy |
| Waiting by step | Queued jobs grouped by current step; resource named only for a singular or bound eligible set; waits from actual readiness and dispatch times | Assigning pre-binding shared waiting to the machine ranking would pick |
| Idle-flow characterization | Idle slot plus queued eligible work, read from the observation (test 2); remaining routing steps from the model | Explaining the idle slot by replaying the one-dispatch-per-trigger cascade |
| Bottleneck named methods | Active periods measured from actual intervals; intervention outcomes from actual reruns | Ranking candidates to predict which machine would constrain |
| Static model analysis (e.g. authored capacity per step) | A separately named method over **model** facts, never presented as runtime outcome | Allocating shared capacity by Engine dispatch rules |
| Shifting bottleneck, co-binding | Time-resolved measurements; joint versus single interventions actually executed | Composing single-change verdicts into a joint claim (review probe 2) |
| Excess capacity / "needed" | Direct comparison of actual variant outcomes, time-only; "worth it" only under a named Challenge evaluation policy | A single-run "surplus" label |
| Run comparison | Factory `FactoryModelSemanticComparator` plus Governance `ChangeSet` for what changed (stable `MachineId`, order changes reported); outcome facts per run; unique attribution refused for multi-variable change sets | Attributing a joint delta by "subtracting" single-change deltas |

The near-term horizon stays on the measurement/interpretation side. No near-term concept needs a
fact the supported contract lacks, or a rule outside the Engine's.

## 14. Phase 1 Q1–Q6, updated only where changed

| Question | Original Phase 2 answer as qualified by the review | Change in this revision |
|---|---|---|
| Q1 utilization wording | Processing-time facts, not a normalized result; obligation ambiguity to resolve explicitly | **Changed.** The owner decision removes `busyTicks` and the "utilization facts" wording, so the ambiguity is resolved by removal. Prospectively, normalized utilization is **not admitted to Engine now** (Rule 2 unmet), not "never" |
| Q2 four recomputable fields | Retain | Unchanged. `averageLeadTime`/`throughputPerTick` are an explicit compatibility exception (§12) |
| Q3 history-carrying aggregates | Retain | **Changed for `busyTicks` only**: owner-decided removal, consistent with Rule 2(b). `completedSalesValue` retained |
| Q4 observation clock | Closed for the retained field | Unchanged |
| Q5 public accessors | Method-level support open | Unchanged; the removal must also account for `Machine`/`MachineView`/`FactoryRuntime` projection of the field (review qualification 8) |
| Q6 rule tests | `combinedQueueDepth` Engine-owned; step-first waiting and occupancy derivable outside | Unchanged. The attribution distinction (§10) now repairs the R1(c) ambiguity the review identified |

## 15. Contract-change accounting and downstream triggers

### 15.1 Current semantic changes

**None is required by this rule.** Separately:

- the owner-decided `busyTicks` removal is an Engine-semantics plus runtime-contract narrowing, on the
  owner-decision route (§12);
- the overview bottleneck claim and the Engine §10 bottleneck sentences need textual correction.
  The overview correction is needed whether or not the removal proceeds.

The Determinism Contract needs no revision. Outside-Engine definitions own their own determinism and
reproducibility, and Engine's output boundary is unchanged.

### 15.2 Prospective admission policy

Recording Rules 1–8 is an **architectural policy decision**: an ownership boundary in the overview's
domain-authority area or a focused section it links. It is not an editorial correction, and it needs
its own independent PR review at reconciliation.

### 15.3 Downstream questions

| Question | Decision | Exact trigger |
|---|---|---|
| Evidence, completeness and provenance | **Not promoted; stays `CANDIDATE`** | Its brief requires an established consumer-neutral responsibility; none is admitted. Truthfulness-dependent needs exist (complete windows, period coverage, running work at boundaries, availability history), but Rule 7 assigns them to the game's own definitions, and the game controls its capture. Promote when **either** (1) a definition is admitted to a shared owner under Rule 4, **or** (2) an analytical result must be reconstructable by a party that did not capture its evidence — first plausibly a Governance evidence use of a verification-objective result, which also needs analytical-definition identity and producer provenance (Governance §5, §12) |
| Cross-adapter compatibility | Stays `CANDIDATE` | Unchanged: a shared analytics contract plus a second supported consumption boundary |
| Cross-revision comparability | Stays `CANDIDATE` | Unchanged. Newly visible candidate trigger: pre-computed recorded runs (the game's static method) retained across Engine development revisions and compared with fresh runs |
| One dispatch per trigger after a machine returns online (test 2: 16 vs 8) | **Not a new question** | Already a retained behavior with stated reopening triggers in the engine-evolution research (a concrete case at its line 110; "empirical recovery behavior from a supported consumer"). A game downtime curriculum would be such a consumer: route it there |

No other downstream question is newly triggered.

## 16. Near-term extension stress test

| Concept | Demand status | Semantic kind | Natural owner under the rule | New owner or architecture required? | Trigger / qualification |
|---|---|---|---|---|---|
| Utilization over a period | concrete | measurement | Game, named definition (Rules 3, 5); evidence from Engine; capture by game (Rule 7) | **No** | Shared on Rule 4(b) when a utilization verification objective is evaluated with the same meaning; Engine only on Rule 2(b)/(c). State offline treatment before using availability changes; never a bottleneck verdict |
| Idle-flow / starvation | concrete | characterization over observation and model, aggregated into a measurement | Game, named characterization | **No** | Add a third state or refusal (test 2), a declared cap (test 3), a pool basis and a known-work rule; recheck when Engine availability or release semantics change |
| Waiting by step | evidenced | aggregation along authoritative identities (step first) | Consumer-local; durations from events | **No** | Resource attribution only for singular or bound eligible sets. Post-binding waiting under transfer is resource-attributable by the Engine's binding fact (§6 rule 9, §9 rule 8); recheck when transfer executes |
| Occupancy / timeline | evidenced | temporal measurement | Consumer-local; complete window captured by the owner | **No** | Engine retains nothing. A late-join requirement triggers Rule 2(b) (A′, or a current-step start time) or a separate retention change |
| Bottleneck, named method | near-term possible | inference | Consumer-local named method over measurements and actual reruns | **No** | Never Engine unless result-affecting (Rule 2(a)); shared only when two consumers need the same named method. No method is required now |
| Shifting / active period | near-term possible | temporal inference | Consumer-local named method | **No** | Several methods may coexist; method identity is part of the claim. Under blocking or setup, "active" gains Engine-supplied states (§18) |
| Co-binding | near-term possible | inference returning a set | Consumer-local; established by joint versus single interventions | **No** | Must be able to return several constraints, or refuse |
| Excess capacity / "needed" | evidenced | intervention plus comparison plus decision interpretation | Experiment design and verdict: consumer. Outcomes: Engine re-execution. "Worth it": Challenge evaluation | **No** | Time-only; single-change verdicts do not compose (review probe 2) |
| Run comparison | evidenced | comparison | Change set: Factory comparator plus Governance `ChangeSet`. Outcome comparison and attribution refusal: consumer. Scoring: Challenge | **No** | Same-revision only. Across revisions: the comparability question. Stable identity at projection: the integration boundary (consumer plan §1) |
| (Brief class) KPI history / baseline comparison | historical | retained measurement plus comparison | Consumer capture and comparison | **No** | Shared only under Rule 4 |
| (Brief class) processing/waiting/transfer decomposition | non-spatial evidenced; transfer unexecuted | measurement | Consumer-local | **No** for the non-spatial part | Engine supplies `TRANSFER_*` facts and admission load when spatial execution lands; recheck then |

**Why admitting the next two or three game concepts would not restructure ownership.**

Take waiting by step, occupancy timelines, and a bottleneck named method such as an active-period
method:

- Each lands in the destination the rule already assigns: a game-owned named definition over
  evidence the game already captures for utilization.
- Each is derivable from today's supported evidence, so none needs a new Engine fact. Tests 1–2,
  `WaitingWorkByStepOracle`, `DispatchProfileOracle` and `ProcessingIntervals` show this.
- None needs Engine-retained history, because Rule 7 gives capture to the owner.
- None needs a shared owner, because no second consumer exists.

The only pressures that could move responsibility are each a pre-declared clause:

- a measurement used by dispatch (Rule 2(a));
- a late-join requirement (Rule 2(b) or a retention change);
- a shared verification objective or a second consumer (Rule 4).

When one fires, it changes one definition's custodian along a one-way path and leaves the principle
unchanged. Under Rule 5 the definition's meaning is unchanged too.

**The stress test passes** for the near-term horizon under current semantics, with the Rule 5
qualification from §17.

## 17. Adversarial analysis (self-administered only)

This is not an independent review.

| Challenge | Result |
|---|---|
| The rule is unfalsifiable: everything goes to "whoever names it" | **Rejected, with qualification.** It makes checkable predictions: no near-term concept needs a new Engine fact or Engine-retained history; the concrete measurements' basis choices are not fixed by execution; the third idle state needs no dispatch rule. Tests 1–3 and the substrate oracles check all four for the current semantics. Any failure would force an Engine-side change the rule treats as a trigger, not a default |
| The anti-rework test passes by construction because promotion is "allowed" | **Partly upheld.** Promotion moves custody once, game to shared. It is not a back-and-forth. Rework is avoided only if Rule 5 holds from day one; a game implementation that hard-codes undefined choices would need refactoring at promotion. That is an implementation risk, so the discipline must be in the reconciled rule and the game requirement set |
| Consumer-local game ownership contradicts the game plan ("not game-owned by default"; no local generic analytics) | **Upheld as a reconciliation consequence, not a conflict.** That restriction explicitly awaits this question. Under the rule it becomes "game-owned named definitions under Rule 5, never presented as Engine facts or Arcogine-wide measures". The plan's ban on reproducing *Engine-declared* KPI semantics stays |
| A′ shows Engine-rich is cheap; Engine should own an occupancy integral now | **Rejected for now.** No concrete late-join consumer exists, and the game captures complete evidence. The owner just decided to remove the existing processing aggregate for exactly that reason. A′ stays the triggered Engine form |
| Shared ownership is withheld only because the verification objective is unimplemented, which is a technicality | **Rejected.** The objective's basis choices are unspecified, so even a shared owner created now could not know the meaning to share. That is the semantic gap, not a technicality. Charter §6 requires a named requirement with acceptance evidence |
| The compatibility exception for two legacy fields undermines the rule | **Qualified.** It is finite, named, and revisitable, and the rule governs admission, not removal (review F). An owner who prefers consolidation may remove them as an explicit Engine-semantics plus runtime-contract change. No proving case requires either choice |
| Test 1 is contrived; definitions "obviously" differ | **Partly upheld.** The point is not surprise but evidence: an Engine-fixed utilization would have to choose among definitions that change values *and* rankings on an ordinary two-slot run |
| Test 2 depends on an artificial toggle at tick 0 | **Rejected.** Any availability return with a queue reaches the state (§4 rule 7). Engine-evolution research records the same behavior for a concurrency-4 machine |
| Stale baseline | Live `main` equals the handoff SHA. Changes since the reviewed evidence are guard and test cleanups only (§2). Final recheck in the header |
| Synthesis seeds | **Not consulted**, consistent with `researching.md` §2 |

**Transferability.** The narrow candidate signal: "the owner of a measurement is whoever fixes the
choices execution does not fix, and a definition carried from the start in promotion-ready form
moves between owners without changing meaning". It is recoverable from first principles and is
retained in the rule if reconciled, so no synthesis seed is nominated.

## 18. Future semantic horizon: compatibility and reopening conditions

Nothing here is designed. Each item lists the obvious extension and what would reopen the rule.

| Future semantics | Extension under the rule | Reopen when |
|---|---|---|
| Downtime / availability-aware capacity | Engine owns availability facts: commands, `MACHINE_AVAILABILITY_CHANGED`, and any authored schedule if modeled. Measurements add an available-time basis choice | Interruption of active work becomes possible (it is rejected today), creating a preemption state outside the supported facts; or a consumer needs availability *interpretation* inside Engine |
| Orders arriving over time / release policies | Release is Factory or Engine semantics. "Known work" becomes an explicit basis choice | Engine holds unreleased work that observations do not expose (a projection is then required), or a dynamic release policy consumes a measurement (Rule 2(a)) |
| Multiple products | Aggregation keys extend by product and operation step; definitions unchanged | A cross-product statistic needs weights only a governing requirement can fix (Rule 4(b)) |
| Finite buffers / blocking | Engine owns blocked states and buffer occupancy as new facts. "Blocked" joins the characterization. Utilization gains a working-versus-occupied choice | A blocked state is unexposed, or bottleneck methods need Engine-internal states |
| Setup / changeover | If it becomes executable, Engine owns setup states and durations as facts. Measurements choose whether setup counts as working | Setup becomes sequence-dependent with information only Engine has |
| Transfer / layout decomposition | Engine owns the transfer lifecycle and admission-load facts (§9–§12). Decomposition and bottleneck interpretation are outside | Admission load cannot be projected truthfully, or Engine §10's "bottleneck interpretation" is retained as an Engine obligation |
| Stochastic behavior | Engine owns random streams and seed consumption (Determinism Contract). Replication statistics and confidence statements are named outside methods | Several consumers need one statistical method (Rule 4(a)), or controlled comparison needs Engine-supported common random numbers or seed control — an Engine-fact extension |

None of these depends on the permanent truth of always-online, single-product, release-at-once
semantics.

## 19. Surviving invariants

1. Engine owns execution truth, result-affecting interpretation and the evidence to measure it;
   declared results keep their exact arithmetic while declared.
2. A report-only measurement enters Engine only on a concrete requirement: execution participation,
   a fresh-observation need for an execution-fixed aggregate, or an Engine-wide contractual
   definition. Possibility, recomputability and convenience are not reasons in either direction.
3. A measurement's owner is whoever fixes the choices execution does not fix. The default is the
   consumer that needs it.
4. Shared consumer-neutral ownership needs the same meaning demanded by more than one concrete
   requirement. A count, a category or anticipated reuse is not enough.
5. Every non-Engine definition is named, basis-explicit, input-declared, completeness-aware and
   presentation-separable. Then promotion changes custody, not meaning.
6. Measurement and interpretation read recorded outcomes. Counterfactuals use Engine re-execution.
   No definition replays Engine decisions.
7. Evidence custody follows the definition owner. Engine retains no history by default.

## 20. What did not survive

- The original R2 as a **universal ban** on Engine measurement (replaced by Rule 2's trigger).
- "A consumer-neutral analytics responsibility exists **now**", and the resulting promotion of
  evidence/provenance (unsupported by present need).
- "Strict facts-only fails" stated without its evidence-contract scope (narrowed, §5).
- "Coupling makes earlier results non-attributable": Engine §1 says only that they are not
  attributable *merely from* fingerprint, run ID or label.
- R1(c)'s attribution-blind wording (repaired, §10).
- "An idle slot with work queued for it is a contradiction under the current Engine": an
  always-online property only (test 2).
- "The bottleneck is identifiable by carried load and by busy-tick utilization" as a general claim.
- "The `busyTicks` removal is reuse evidence for shared utilization".

Reusable negative knowledge:

- One supported evidence set yields legitimately different utilization values *and rankings*
  (test 1).
- An availability return leaves an idle slot beside eligible queued work under current semantics
  (test 2).
- Starved-slot totals depend on a declared cap (test 3).

## 21. Confidence by conclusion class

| Conclusion | Confidence | Basis |
|---|---|---|
| Engine keeps execution facts, `combinedQueueDepth`, projections and declared arithmetic | High | Normative text, code, conformance and reruns |
| Every near-term horizon concept is derivable from today's supported evidence, without new Engine facts or re-decision | High | Tests 1–3, the substrate oracles and the game corpus, all rerun or re-verified at this baseline |
| Utilization and characterization basis choices are not fixed by execution | High | Test 1, tests 2–3, game review 4.3 |
| Comparison is not an Engine fact; "needed" is not a single-run measure | High | D3, review probe 2, Determinism Contract rule 5 |
| Rule 2 is the right Engine-admission formulation (trigger, not ban) | Medium-high | It closes the REOPEN finding. A′ shows the Engine-side form; no executable discriminator selects it over a slightly different wording |
| Shared ownership staged, not admitted now | Medium-high | Review qualification 5, Charter §6, bounded absence search. Would change if a verification objective is specified |
| Consumer-local default plus Rule 5 passes the anti-rework test | Medium | Holds for the near-term horizon under current semantics; depends on Rule 5 actually binding the game's implementation |
| Kind-specific shared thresholds | Medium | Judgment calibrated by repository precedent (overview §Attribution; Factory Design §7 "share when justified"; Charter §6); not executable |
| Compatibility exception for `averageLeadTime`/`throughputPerTick` | Medium | An owner preference; no proving case forces either choice |
| Future-horizon extensions | Low-medium | The semantics do not exist yet; only reopening conditions are claimed |

**What would change the conclusion:**

- a concrete requirement for one Engine-wide utilization or occupancy definition;
- a specified and evaluated utilization verification objective (it promotes the shared owner);
- a late-joining consumer that needs in-progress occupancy (it admits A′);
- a dispatch policy that consumes a measurement;
- spatial execution showing that admission load cannot be interpreted outside Engine.

## 22. Unresolved unknowns

- The final game definitions of utilization and flow characterization. Their choices are enumerated
  in §6.2; the game owns them.
- Whether and how Factory Design's utilization verification objective will be specified — the
  likeliest shared trigger.
- Where a triggered shared owner would live: a verification capability or a separate analytics
  role.
- Method-level support of the `FactoryRuntime` aggregate accessors (Q5).
- How a future live interactive game loop captures its evidence. Rule 7 assigns the responsibility,
  not the mechanism.
- Whether one-dispatch-per-trigger after an availability return is intended Engine behavior for the
  game's purposes; this belongs to engine-evolution research.

## 23. Durable consequences if accepted

None is performed here. All follow a new independent review of this revision.

1. **Architecture.**
   - Record Rules 1–8 and the attribution distinction as the simulation-measurement ownership
     boundary (overview domain authority, or a focused section it links). This is a policy decision
     needing its own review.
   - Restate Engine semantics §10's bottleneck sentences as capacity/admission-load facts plus
     outside interpretation.
   - Scope or remove both halves of the overview's headless bottleneck claim.
2. **Owner-decision change** (separate route): remove `busyTicks` with game-review qualification 8's
   exhaustive scope. That resolves the runtime contract's "utilization facts" and the §10.2 row. The
   change also updates the analytics-boundary brief items and the evidence-provenance brief's cases
   4–5 that name the counter.
3. **Planning.**
   - Game consumer plan §4 ownership row and §5 diagnostics text: game-owned named definitions under
     Rules 5 and 7; shared on Rule 4; no reproduction of Engine-declared results.
   - Vertical-slice gate criterion 4: satisfiable for consumer-local definitions by their owner's
     input/capture contract.
   - Spatial-runtime ownership note: the pinned results stay Engine-owned, except as the owner
     decision changes `busyTicks`.
   - The readiness KPI note.
4. **Governance evidence §12–§13.** Analytical-definition provenance follows the definition owner;
   it stays open for any future shared owner. Governance responsibilities are unchanged.
5. **Register** (on reconciliation only).
   - Umbrella question → `CONCLUDED` once the above lands.
   - Evidence/provenance stays `CANDIDATE` with the §15.3 trigger.
   - Adapter compatibility and cross-revision comparability unchanged, with the newly visible
     cross-revision trigger noted.
6. **Game requirement set.** The game diagnostic reconciliation promotes utilization and flow
   characterization as game-owned named definitions, with §11.3's corrections, not as "defined by
   their eventual owner".
7. **Reusable assets.**
   - Tests 2 and 1 are worth promoting into the tracked research corpus as proving cases: the
     availability third state, and one evidence set with several definitions. Alternatively, they
     move into the game's own acceptance tests when diagnostics are implemented.
   - Test 3 is recoverable from first principles and may be discarded.
   - Prior experiments B/C/C′/D1/D3 keep the original report's disposition; A is discardable.
8. **Historical decision-rationale record: recommended.** Several serious alternatives remained
   viable (A, B, C, D), the choice knowingly trades Engine coupling against rework, it follows a
   REOPEN and independent review, and concrete triggers can justify revisiting it. That meets the
   retention test in `researching.md` §10. The record should hold the rejected universal ban, the
   withheld shared owner and the A′ trigger.
9. **No synthesis seed** (§17). This report itself need not stay readable after reconciliation.

## 24. Implementation implication

**No implementation.** No analytics module, API or capability is admitted. No game consumer is
admitted. The only product change in view is the owner-decided `busyTicks` removal, which is
separately reconciled.

## 25. Follow-up triggers

- A new, genuinely independent adversarial review of **this exact revision** (next, required).
- A modeled or evaluated utilization verification objective (Rule 4(b); the evidence/provenance
  trigger).
- A late-joining consumer that needs in-progress occupancy (Rule 2(b)).
- A measurement consumed by a dispatch or release policy (Rule 2(a)).
- Spatial execution landing (Engine §10 rechecks; transfer decomposition; post-binding waiting).
- Availability-schedule, blocking, setup or stochastic semantics becoming supported (§18).

Track these through the existing register entries. No new register question is proposed.

## 26. Sources

**Repository, at baseline `306205c3`:** the documents and code in §2, in particular:

- `docs/architecture/engine-semantics.md` §1, §1.1, §2, §4, §9–§10.2, §14;
- `docs/architecture/runtime-contract.md`;
- `docs/architecture/overview.md` (Domain authority, Attribution, module-admission rule, headless
  closure paragraph, Determinism Contract);
- `docs/architecture/factory-design.md` §7, §10;
- `docs/architecture/governance-evidence.md` §5–§7, §11–§13;
- `docs/product/charter.md` §6;
- `docs/planning/factory-design-game-consumer.md`, `factory-design-game-vertical-slice.md`,
  `spatial-runtime-consequences.md`;
- `docs/research/investigations/engine-evolution.md`;
- `product/domains/factory/.../process/{FactoryRuntime,FactoryHandler,ResourceObservation,JobObservation,RuntimePerformanceObservation,RuntimeEventPayload}.java`;
- `product/consumer/challenge/.../{comparison,evaluation}/`;
- `product/research-experiments/src/main/...` (all);
- history: #241 `e468e826087b699c5bdca835db362bac706f079e`.

**Prior evidence:** §1 coordinates.

**Custody experiment:** §8.2 path, committed with this report.

**External:** none newly used. The original report and the REOPEN review verified Roser, Nakano and
Tanaka, *Throughput Sensitivity Analysis Using a Single Simulation*, WSC 2002, pp. 1087–1094, §2,
for the point that a bottleneck claim is a named temporal method valid only while the bottleneck does
not shift. It was not re-fetched here and is not load-bearing: the repository's own counterexamples
carry every claim above. Simulation-tool precedents (AnyLogic built-in utilization, SimPy
user-defined monitoring) establish possibility only, as both prior artifacts state, and decide
nothing here.
