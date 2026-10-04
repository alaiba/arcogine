# Simulation analytics ownership boundary — Phase 2: consumer-neutral analytics admission boundary

> **Research status:** ACTIVE. This report completes the evidence for the coupled two-phase packet.
> The registered umbrella question is **not** `CONCLUDED`: that needs independent adversarial review
> of this exact report revision and a separate reconciliation of the durable consequences.
>
> **Research baseline:** live `main` at `8c2ed14dccde0ca1dbb51e58c94ff19d822856a1`, resolved
> 2026-10-05. Since the Phase 1 baseline (`0b82db0`), `main` gained only #448 (coverage/build, tests,
> Storage). It changed none of `AGENTS.md`, the Researcher contract, `docs/development/researching.md`,
> `docs/research/`, `docs/architecture/`, Factory main sources, or research-experiments main sources.
>
> **Mandatory input:** the Phase 1 report at workspace commit
> `7e72a46506bf63febcb57d7b7dcc9ce0cd484d64`, path
> `workspace/research/investigations/simulation-analytics-ownership-phase-1-current-boundary.md`.
> It is evidence, not architecture. Every load-bearing claim used here was rechecked at the baseline
> (§ Phase 1 recheck).
>
> **In-flight portfolio state:** PR #454, which moves the register row from `READY` to `ACTIVE`, was
> **open, not merged** at investigation start. By the final recheck it had been **closed without
> merging**. On `main` the register still says `READY`; this report does not describe `ACTIVE` as
> landed register truth. Issue #453 (`combinedQueueDepth` exactness fixture) is a separate
> executable-evidence gap and is not absorbed here.
>
> **Final repository recheck:** live `main` advanced during the investigation to
> `4d83f143143bf778881ba842bd4949734ce400c9`. That commit is #452, a Storage test only
> (`BuiltInStorageTest.java`), and does not affect this conclusion.
>
> **Risk:** High (major ownership, supported observable semantics, deterministic Engine behavior).
>
> **Authority:** Research evidence only. This report changes no architecture, specification, runtime
> contract, Determinism Contract, product direction, planning admission, or code.
>
> **Adversarial-review status:** required, not yet performed. Only the author's self-challenge
> (§ Adversarial analysis) has been applied. That is not an independent adversarial review, and this
> report is not decision-quality evidence for architecture promotion until such a review of this
> exact revision exists.

## Question

> Given the Phase 1 classification, should any reusable consumer-neutral analytics responsibility
> exist, and what minimum ownership rule separates it from Engine semantics?

The brief's question is unchanged. "Responsibility" is read as a **semantic ownership** role: who
defines what a measurement means, what evidence it may read, and when it must refuse. It is not a
module, package, or adapter, so the answer can establish a responsibility without admitting any
capability.

## Decision at stake

1. Which current supported performance results stay Engine-owned, move, or stop being Engine results.
2. Whether the Engine's supported boundary may grow to absorb further measurement, such as
   utilization, occupancy, or diagnosis.
3. Whether a consumer-neutral owner exists for reusable measurement, or such measurement is always
   consumer-local.
4. Which downstream questions become promotable.

Several accepted surfaces are waiting on this decision:

- The game consumer plan forbids the game from implementing generic analytics locally until it
  resolves (`docs/planning/factory-design-game-consumer.md:136-138`).
- The playable-slice admission gate requires "any reusable analytics requirement has a settled
  ownership/input contract" (`docs/planning/factory-design-game-vertical-slice.md:22`, `:39`).
- Governance records analytical-definition provenance ownership as unresolved and points here
  (`docs/architecture/governance-evidence.md:292`, `:310-311`).
- Planning holds the ownership of the pinned derived results open
  (`docs/planning/spatial-runtime-consequences.md:147-167`).

## Scope and non-goals

**In scope:**

- the three candidate ownership models;
- the prospective ownership rule;
- the eight prospective responsibility classes as proving classes;
- Phase 1 Q1–Q6;
- contract-change accounting;
- downstream triggers.

**Out of scope, per the brief and handoff:**

- implementing analytics or restoring the KPI package;
- changing runtime behavior, `RuntimePerformanceObservation`, or `FactoryRuntime`;
- fixing wording ahead of the conclusion;
- issue #453;
- selecting formulas for utilization, bottleneck, starvation/surplus, or comparison;
- designing retention, provenance, adapter, or cross-revision contracts;
- designing a module or API migration;
- implementation planning;
- independent adversarial review.

## Executive conclusion

**Surviving model: a refined Model 3, the minimal authoritative boundary, with no reclassification of
current supported results.** The Engine keeps everything it supplies today. The Engine's supported
boundary does not grow to absorb measurement policy. A bounded consumer-neutral analytics
responsibility exists for named measurements over supported evidence. Diagnostic interpretation
stays with the consumer that names its method. Confidence is medium-high overall; per-claim confidence
is given below.

1. **Strict Model 2 (facts-only) fails.** It fails executably, not just by argument.
   - Two runs can produce fresh observations and a published model that are identical except for
     `busyTicks`, or identical except for `completedSalesValue` (experiment C, B).
   - Removing those run-to-date aggregates from the Engine therefore forces one of two things. Either
     complete retained history (a hidden replay requirement, contrary to the runtime contract's
     fresh-observation principle), or new projections that recreate the same accumulators.
   - Facts-only either fails or collapses into Model 3.
2. **Model 1 fails as a growth model.** It survives only as a description of today's fields.
   - Extending the Engine's supported results to normalized or interval measurement puts measurement
     policy into the one indivisible Engine interpretation. That covers capacity basis, interval,
     population, refusal semantics, and named method.
   - That interpretation's own stated reason for bundling ("those rules interact to produce one
     outcome", `engine-semantics.md:38-41`) does not hold for report-only results.
   - Every later measurement refinement would become an Engine definition change. Under §1, that makes
     earlier execution results non-attributable to the current definition even when no execution
     behavior changed.
   - The order-level results do not exhaust useful diagnosis (experiment D1). Constraint identity is
     not even a single-run measurement (strategy-space qualification 2). So "the Engine-rich status quo
     makes analytics unnecessary" is also false.
3. **A consumer-neutral analytics responsibility exists, but it is bounded.**
   - It owns named measurement definitions over supported evidence: exact method, declared inputs,
     evidence-completeness conditions, and refusal.
   - It also owns the qualification rule for comparing measurements, for measurements that must mean
     the same across consumers or for a shared concern.
   - The repository already presupposes such an owner in three places:
     - verification objectives, including "Maximum utilization <= threshold", are a "shared
       verification concern" (`factory-design.md:320-331`);
     - Governance assigns calculation semantics to "Engine and analytics producers"
       (`governance-evidence.md:273-276`);
     - the game is barred from owning generic analytics.
   - No capability is admitted by this conclusion, and no current product code computes such a
     measurement.
4. **None of the eight prospective classes is Engine-necessary.**
   - Occupancy intervals, utilization, waiting-by-step attribution, and non-spatial
     processing/waiting decomposition are plausibly consumer-neutral.
   - Constraint/bottleneck and starved/surplus labels are consumer-local named-method interpretations.
   - Run/attempt scoring is consumer-local, as the existing Challenge comparator already is. The
     qualification for comparing measurement deltas is plausibly consumer-neutral.
   - Transfer decomposition cannot yet be classified.
5. **Current-field consequences: no reclassification is recommended.**
   - `backlog`, `completedOrders`, `busyTicks`, and `completedSalesValue` are Engine-owned on
     principle. They are current-state summaries or run-to-date aggregates of authoritative change.
   - `averageLeadTime` and `throughputPerTick` contain measurement policy (a mean with an empty-set
     sentinel; a rate over the observation clock with a zero-window sentinel). They **remain
     Engine-owned as currently declared results** and are *eligible* for, but not warranted for,
     reclassification.
   - The "utilization facts" wording is resolved as a **textual clarification** with no supported
     semantic change. The Engine supplies processing-time facts; any normalized utilization is an
     analytics measurement.
   - No `FactoryRuntime` operation needs to change. The method-level support of the aggregate
     accessors, especially caller-windowed `throughput(long)`, should be stated explicitly during
     reconciliation.
6. **Required contract revisions are textual clarifications only.** They affect the Engine-semantics
   §10/§10.1/§10.2 utilization and bottleneck wording, the runtime contract's Performance minimum
   wording, and the overview's `busyTicks` description plus a new ownership-principle statement.
   - No Engine definition change, runtime-contract semantic change, or Determinism Contract revision
     is required.
7. **Downstream triggers:**
   - **evidence/completeness/provenance** — this report satisfies its promotion trigger, conditional
     on independent review and reconciliation;
   - **cross-adapter compatibility** — remains `CANDIDATE`;
   - **cross-revision comparability** — remains `CANDIDATE`.

## Minimum ownership rule (proposed, not adopted)

**R1 — The Engine owns:**

- (a) authoritative execution state and authoritative change;
- (b) every rule that can change acceptance, rejection, assignment, ordering, or timing, *whether or
  not its quantity is ever exposed*;
- (c) the supported projections that let a fresh observation represent current state and
  execution-to-date without replay. These are current-state summaries and **run-to-date aggregates
  of authoritative changes**: counts and sums over the run, with no interval, normalization basis,
  statistic, population filter, or attribution beyond the run itself;
- (d) the exact arithmetic of every derived result the Engine declares supported, for as long as it
  remains declared.

**R2 — Measurement policy is not admitted to the Engine.** A value is a *measurement* if it
participates in no Engine decision and its definition requires a choice that execution does not fix:

- an interval other than the run;
- a capacity or normalization basis;
- a statistic or population;
- attribution or classification;
- inference;
- refusal behavior;
- cross-run scope.

New measurements are not added to the Engine interpretation. They are owned by consumer-neutral
analytics when they must mean the same for more than one consumer or for a shared concern, and
otherwise by the consumer that needs them.

**R3 — Analytics measures; it never re-decides.** A consumer-neutral analytical definition must:

- be named;
- state its exact method;
- read only declared supported inputs (published model, observations, gap-free supported-event
  ranges);
- refuse when the evidence does not license the claim;
- never compute which resource, order, time, or admission the Engine would or should have chosen.

Counterfactual questions are answered by Engine re-execution under changed explicit inputs, never by
analytical re-simulation.

**R4 — Interpretation belongs to whoever names the method.** Diagnostic labels — constraint or
bottleneck, starved or surplus, causal attribution — are named-method interpretations owned by the
consumer presenting them, built on measurements. A shared owner for a method is warranted only when
two consumers need the same named method with the same meaning.

**R5 — Placement and factoring never transfer ownership** (Phase 1 invariant).

How the rule handles the brief's required cases:

| Required case | Rule | Result |
|---|---|---|
| `combinedQueueDepth` stays Engine-owned because its exact arithmetic participates in assignment | R1(b) | Pass. It is Engine-owned even though it is never exposed and is recomputable at a boundary |
| A current-state summary can stay Engine-owned even if derived | R1(c) | Pass (`backlog`, `queueDepth`, `completedOrders`) |
| Interval occupancy/utilization can be measurement over outcomes | R2 | Pass. Analytics owns the definition; the Engine supplies the inputs |
| Code factoring does not transfer ownership | R5 | Pass |
| Analytics may measure but must not reconstruct or re-decide Engine choices | R3 | Pass. Step-first waiting attribution and dispatch/completion occupancy read supported facts; neither ranks candidates (PC-Q6) |

**Exceptions.** There is exactly one class of exception, and it is explicit, not ad hoc:
`averageLeadTime` and `throughputPerTick` are retained under R1(d) although R2 would not admit them
today. R2 governs *admission*. It does not compel removal of declared results, because removal is a
separate cost/benefit decision (§ Q2). No other current item needs an exception.

## Phase 1 recheck

Each load-bearing Phase 1 claim was rechecked at the baseline:

| Phase 1 claim | Recheck at `8c2ed14` | Effect |
|---|---|---|
| Current performance results and `busyTicks` are Engine-owned because they are declared supported, not because any decision reads them | Unchanged text and code. No selection path reads them (`FactoryHandler.java:162-186`) | Holds |
| `combinedQueueDepth` is decision-affecting and unexposed | Unchanged | Holds |
| `backlog`, `completedOrders`, `averageLeadTime`, `throughputPerTick` are recomputable from one fresh observation (Phase 1: inference) | **Now executable.** Experiment A recomputes all four bit-exactly from 23 observations (9 with incomplete orders) across all 14 corpus fixtures | Strengthened from inference to executed evidence |
| `busyTicks` and `completedSalesValue` are not generally recomputable from one observation, but are recomputable from complete events (plus model durations) | **Now executable.** Experiments B, C, C′ | Strengthened |
| The "utilization facts" wording is contestable | Unchanged | Closed by Q1 |
| `busyTicks` is completion-credited job-ticks, not occupancy or a normalized ratio | Unchanged; corroborated by D1 | Holds |

**No load-bearing Phase 1 error was found, and Phase 1 does not need to be reopened.** As the handoff
requires, the R-obs/R-hist split was *not* adopted as the boundary. R1/R2 use different criteria:
decision participation, run-to-date aggregate versus measurement policy, and declaration. History
dependence enters only as a **consequence**: it explains why R1(c) aggregates cannot move without an
evidence contract.

## Repository evidence

### Normative and planning text bearing on Phase 2

- **The interpretation is one bundled definition.** "One interpretation covers dispatch,
  decomposition, scheduling, transfer and derived-result rules together, because those rules interact
  to produce one outcome; independently identified sub-definitions would need a concrete
  independent-evolution requirement" (`engine-semantics.md:38-41`).
- **Definition changes make earlier results non-attributable.** A change to a result-affecting rule
  is a definition change; a result from an earlier revision "is not attributable to today's
  definition" (`engine-semantics.md:46-55`).
- **Spatial bottleneck text.** §10 asks resource observation to expose admission-capacity facts "for
  its existing bottleneck/capacity interpretation", and says "Bottleneck interpretation must
  therefore widen" (`engine-semantics.md:482-494`). It names no Engine-owned bottleneck result.
- **Fresh-observation principle and non-retention.** The observation must reconstruct a consumer's
  view without replay (`runtime-contract.md:61-63`). Retained supported events are explicitly not
  part of the boundary (`runtime-contract.md:208-216`; `overview.md:302`).
- **Verification objectives are a shared concern.** They include `Throughput >= target`,
  `Lead time <= limit`, and `Maximum utilization <= threshold`, labelled a "shared verification
  concern", distinct from consumer rules (`factory-design.md:320-343`).
- **Governance.**
  - "Engine and analytics producers own calculation semantics ... and analytical definitions"
    (`governance-evidence.md:273-276`).
  - Derived results carry "the analytical definition/version needed to interpret the result"
    (`:134-143`).
  - A comparator role exists "only through an explicit use-owned role and the owning domain's
    comparison semantics" (`:144-147`).
  - Analytical-definition provenance ownership is unresolved and deferred here (`:292`, `:310-311`).
- **Game consumer plan.**
  - Reusable derived measurement is "Unresolved — not game-owned by default" (`:114`).
  - "Do not reproduce ... KPI semantics inside the game" (`:120`).
  - Consumer summaries "must not become a competing authoritative computation" (`:134`).
  - The game must not implement generic analytics locally; scoring and evaluation are deliberately
    separate from generic analytics (`:136-138`).
- **Concluded strategy-space research** (independently reviewed; it is in the "Promoted" section).
  "Occupancy is measurement, not a causal constraint definition." In its counterexample, cutting has
  the highest pool occupancy, yet adding a cutter leaves completion at 67 while adding an inspector
  brings it to 45. Controlled marginal interventions, not occupancy, evidenced the effective
  constraint; no repository-wide bottleneck algorithm was defined
  (`factory-design-game-strategy-space.md:83`). The counterexample is preserved as executable
  `CapacityCorpus` fixtures (`:136`, `:142`). Pooling advantage needs concurrent work (`:89`).
- **Diagnostic-evidence brief** (READY, game-owned). It requires named methods, refusal ("prefer the
  weaker true statement"), and controlled-versus-confounded comparison. It explicitly leaves reusable
  derivation ownership to this question
  (`factory-design-game-diagnostic-evidence.md`, § Scope and non-goals, § Truthful explanation
  constraints).

### Implementation and executable surfaces

- **Challenge consumer.**
  - Evaluation receives only `AuthoritativeOutcomeFacts(contractCompleted, completionTick)`. That
    record holds "no queue, dispatch, transfer, or runtime-state detail"
    (`product/consumer/challenge/.../evaluation/AuthoritativeOutcomeFacts.java`).
  - Scoring is a versioned policy: "Changing any of these observable rules requires a new policy
    version" (`ReferenceChallengeEvaluationPolicy.java`).
  - `ChallengeAttemptComparator` refuses comparison across challenge or evaluation-policy versions
    with structured reasons. It "never re-evaluates either attempt" and "introduces no new scoring
    dimension" (`comparison/ChallengeAttemptComparator.java`).
  - So consumer-local comparison already exists with the qualification discipline R3 asks for, and it
    consumes one Engine outcome fact.
- **Research substrate** (non-shipped, research-local).
  - Definitions are named, with declared inputs. "Changing what a definition computes is a new
    definition with a new name" (`ResearchDefinition.java`).
  - "Refusal is a first-class outcome" (`OracleOutcome.java`).
  - Oracles receive only declared evidence and "no way to re-decide scheduling or dispatch"
    (`Oracle.java`).
  - `ProcessingIntervals` measures occupancy from `JOB_DISPATCHED` and `JOB_STEP_COMPLETED`. It
    refuses on any event gap, zero elapsed time, availability change, or offline boundary.
  - `EligibilityPoolOccupancyOracle` defines a *second* occupancy measurement, for a different
    question. `DispatchProfileOracle` measures per-step waits "never reconstruct[ing] or re-decid[ing]"
    assignment.
  - These derivations are not candidate product definitions. They show that truthful measurement
    over supported evidence is feasible without re-deciding Engine choices, and that several
    legitimate definitions coexist per question.
- **Projection shapes.**
  - `JobObservation` carries `createdAt`/`completedAt` but no current-step start time.
  - A completed job keeps no machine reference (`Job.completeStep` sets `currentMachine = null`,
    `product/domains/factory/.../jobs/Job.java:47-61`).
  - `OrderObservation` carries no price.
- **Semantic ChangeSets exist.** "What authored fact changed" between two model versions is already
  a supported Factory/Governance capability (`FactoryModelSemanticComparator`, `ChangeSet`;
  `factory-design.md:360`). It is not an analytics concern.

### Executable evidence produced in this investigation

**Source.** `workspace/research/experiments/simulation-analytics-ownership/com/arcogine/research/experiment/PerformanceEvidenceSufficiencyExperiment.java`.
It is held in research custody and run against the baseline product tree with the documented
custody-source mechanism (`docs/development/testing.md:165-171`):

```text
cd product && ./gradlew :research-experiments:test \
  -PresearchExperimentSources=<that directory> \
  --tests 'com.arcogine.research.experiment.PerformanceEvidenceSufficiencyExperiment'
```

It ran in the documented `gradle:9-jdk21` Docker workflow at `8c2ed14`: **6 tests, 0 failures,
0 errors, 0 skipped.** The first run failed on one assertion because of a harness defect: the event
reconstruction omitted zero-credit resources. The defect was corrected and the test re-run; no
evidence assertion changed. Every expected value is hand-derived from current specifications before
running.

| Id | Test | Result |
|---|---|---|
| A | `orderLevelPerformanceResultsAreFunctionsOfTheSameObservation` | For every observation of all 14 `StarterCorpus` + `CapacityCorpus` fixtures (23 observations, 9 with incomplete orders), `backlog`, `completedOrders`, `averageLeadTime` (saturating sum ÷ count, empty → 0) and `throughputPerTick` (count ÷ observation `currentTime`, zero → 0) recompute **bit-exactly** from the same observation |
| B | `completedSalesValueIsNotDeterminedByTheRestOfAFreshObservation` | Identical model and script except unit price: closing observations are identical except `completedSalesValue` (40.0 vs 80.0). Complete `ORDER_COMPLETED` events summed in sequence order reproduce it exactly. A consumer joining after the first completion (events 1–26 missing) sums 20.0, while the Engine still supplies 40.0 |
| C | `busyTicksIsNotDeterminedByTheRestOfAFreshObservationOrThePublishedModel` | One 5-tick step eligible on A1/A2, with availability toggles forcing the assignment. Closing observations and published model are identical except `busyTicks` ({A1=5, A2=0} vs {A1=0, A2=5}). Complete `JOB_STEP_COMPLETED` events plus published durations reconstruct both exactly |
| C′ | `completeEventsAndThePublishedModelReconstructBothAccumulatorsAcrossTheCorpora` | Both accumulators reconstruct exactly from complete events and model for all 14 complete-window corpus fixtures |
| D1 | `identicalOrderLevelResultsCanHideDifferentDiagnoses` | Single 4-unit order on flow lines (8,1,1) and (1,8,1): completion 34 in both and **identical `RuntimePerformanceObservation`** (`backlog=0, completedOrders=1, completedSalesValue=40.0, averageLeadTime=34.0, throughputPerTick=1/34`). Waiting at the mid-run boundary is {CUT: 3} vs {ASSEMBLE: 3}. Total dispatch waits are {CUT 48, ASSEMBLE 0, INSPECT 0} vs {CUT 6, ASSEMBLE 42, INSPECT 0}. `busyTicks` are the same multiset {32, 4, 4} in different places |
| D3 | `aMultiVariableDeltaIsNotTheSumOfItsSingleVariableDeltas` | From base (1,8,1) = 34: ASSEMBLE 8→4 gives 18 (Δ −16); CUT 1→6 gives 39 (Δ +5); both together give 29 (Δ −5). The single deltas sum to −11: non-additive, because the constraint moves |

## Candidate models

Each candidate was assessed on six points. Their "evidence a late joiner needs" assumes the
analytical information *currently available* stays available.

| | **Model 1 — Engine-rich** | **Model 2 — facts-only** | **Model 3 — minimal authoritative (refined)** |
|---|---|---|---|
| Engine responsible for | State and change, decision rules, current performance results, **and growth into further performance measurement** (utilization, occupancy, possibly diagnosis) | State and change, decision rules, current state only. Recomputable performance measurement leaves | R1: state and change, decision rules, current-state summaries and run-to-date aggregates, and declared results' arithmetic |
| Analytics responsible for | At most additive extras | All performance measurement, including today's fields | R2–R3: named measurements over supported evidence and comparison qualification. Interpretation is consumer-owned (R4) |
| Current fields | All stay; more are added | All five performance fields and `busyTicks` move | All stay. `averageLeadTime` and `throughputPerTick` are eligible to move but are not moved |
| Late joiner needs | One observation | One observation for the four R-obs fields; **complete retained events from run start** for `busyTicks` and `completedSalesValue` (B, C) | One observation for today's information; complete event windows only for new interval measurements, which no current field provides |
| Duplicated formulas / hidden replay / second-engine risk | No replay. Measurement policy is coupled to Engine identity, and its sentinel conventions conflict with refusal-based measurement | **Hidden replay requirement**, or new projections that recreate the accumulators | No replay for current information. A shared definition owner prevents duplicated formulas; R3 prevents a second engine |
| Contract change | Engine semantics and runtime-contract *additions* for every new measurement | Both, for six fields; overview tables; accessors; and an evidence/retention contract | Textual clarifications only (§ Contract-change accounting) |

An omitted-candidate check found no materially distinct fourth model. Its variants either collapse
into one of the three or change no decision:

- **"Model 3-strict: move `averageLeadTime` and `throughputPerTick` now."** This changes the
  current-field decision only, and is evaluated under Q2.
- **"Analytics inside Factory as a sub-definition."** This is a placement choice (R5); its semantic
  content is Model 1 or Model 3.

## External evidence

External evidence is used only where it tests a candidate. None of it carries a conclusion on its
own.

| Source | Establishes | Analogy limits | Verification |
|---|---|---|---|
| C. Roser, M. Nakano, M. Tanaka, "Throughput Sensitivity Analysis Using a Single Simulation", *Proceedings of the 2002 Winter Simulation Conference*, eds. E. Yücesan, C.-H. Chen, J. L. Snowdon, J. M. Charnes, pp. 1087ff., §2 "Shifting Bottleneck Detection" (§2.1–§2.2), `informs-sim.org/wsc02papers/146.pdf` | A bottleneck method defines its own active/inactive state classification per machine type. It takes the *momentary* bottleneck as the machine with the longest uninterrupted active period at time *t*, so the bottleneck shifts over time and needs time-resolved per-machine state intervals. The paper's sensitivity predictions hold only "as long as there is no significant change in the bottleneck." This supports two points: a bottleneck claim is a **named-method, temporal interpretation**, not a cumulative counter or current-state fact (R2/R4); and single-run deltas are conditional on the constraint not moving (consistent with D3) | Their systems include blocking, repairs, tool changes, and AGVs. Arcogine's non-spatial runtime has unbounded queues (no blocking) and availability toggles. The state classification does not transfer, and no method is selected for Arcogine | **Verified in session**: pp. 1087–1089 read from the WSC archive PDF |
| C. Roser, M. Nakano, M. Tanaka, "A Practical Bottleneck Detection Method", *Proc. 2001 WSC*, pp. 949–953 | Claims that utilization-based detection can misidentify the primary bottleneck | Same as above | **Background, unverified** (search-result summary only). Not load-bearing: the repository's own strategy-space counterexample carries this point |
| AnyLogic `ResourcePool` reference (utilization statistics collected automatically; a [0..1] fraction of time busy, restricted to operating hours under a schedule) | Contrary analogue: mainstream DES tools *do* put utilization inside the simulator, so Model 1 is industry-normal. It also shows that utilization definitions embed an availability/operating-hours policy | Product documentation of one commercial tool, not an ownership requirement. It shows possibility, not necessity | **Background, unverified**: `anylogic.help` returned HTTP 403; content known only from a search summary |
| SimPy "Monitoring" topical guide (`simpy.readthedocs.io/en/latest/topical_guides/monitoring.html`) | The opposite placement: the guide frames monitoring as something users implement for their own use cases | The page does not state the policy explicitly; this is an implication. No version was shown | Fetched in session; **weak** (implication only) |

**Net external effect.** Industry precedent shows both placements exist. It therefore establishes
neither Model 1 nor Model 3 as necessary. The decisive considerations are Arcogine-specific: the
Determinism Contract's single-interpretation coupling, the fresh-observation principle, and the
repository's refusal-based truthfulness requirements.

## Proving cases

### PC-1 — Single-order diagnostic degeneracy (experiment D1)

**Facts.** Two designs produce identical `RuntimePerformanceObservation` values. Their diagnoses
differ entirely: work waits at CUT versus ASSEMBLE, total waits are 48 at CUT versus 42 at ASSEMBLE
plus 6 at CUT, and the dominant processing load sits on different resources. `busyTicks` locates the
load, but not the waiting or its timing. The concluded strategy-space counterexample adds that even
the most-occupied pool is not the constraint. Constraint evidence came from controlled marginal
interventions, which are multi-run comparisons.

**Result.**

- **Model 1:** survives as a supplier, but fails the claim that the Engine-rich status quo exhausts
  useful analysis. The Engine's results are *inputs*, not the diagnosis.
- **Model 2:** survives this case.
- **Model 3:** survives.
- Diagnosis needs measurement over intervals (waits, occupancy) plus named-method interpretation.
  Neither participates in Engine behavior.

### PC-2 — Temporal occupancy/utilization derivation (Phase 1 PC2/PC3; `ProcessingOccupancyOracleTest`; experiments C, C′)

**Facts.**

- Occupancy over `[0, boundary]` is derivable from dispatch and completion events plus the
  observation, without ranking candidates.
- Truthfulness needs four conditions:
  - a gap-free supported-event range from run start;
  - no availability change in the interval (or an availability-aware capacity basis);
  - an online resource at the boundary;
  - elapsed time greater than zero, or refusal.
- A late joiner has three ways to get occupancy-to-date:
  - complete events;
  - the Engine's `busyTicks` plus the start times of currently running steps — not projected today,
    because `JobObservation` has no step start;
  - an analytics-owned accumulator fed from the start.
- Utilization additionally needs a capacity basis: concurrency, online time, and admission load once
  spatial semantics execute (§10). It also needs a decision on whether running steps count.
  `EligibilityPoolOccupancyOracle` shows a second legitimate grouping.

**Result.**

- **Model 3:** survives. The measurement lives outside the Engine truthfully, under declared
  completeness, and the Engine supplies the inputs.
- **Model 1 (growth):** would make the Engine choose capacity basis, interval, running-step
  treatment, and grouping. Those choices are not needed for execution, and several legitimate answers
  exist. Its total-function sentinels (§10.1: 0 for undefined) contradict the refusal semantics this
  measurement needs. It fails on coupling, not impossibility.
- **Model 2:** survives for new measurements. For the existing accumulator it needs the replay
  covered in PC-5.

### PC-3 — Controlled versus multi-variable comparison (experiment D3; Challenge comparator)

**Facts.**

- A one-variable rerun (ASSEMBLE 8→4) supports a bounded statement. Under the same Engine revision,
  model otherwise unchanged, and the same workload, completion moved from 34 to 18.
- The two-variable change (29, Δ −5) is not the sum of the single deltas (−11), because the
  constraint moves. Unique attribution must be refused.
- The Engine executes one run and holds no cross-run scope. Each run's outcome is an Engine fact; the
  comparison is not.
- "What changed" between designs is a supported Factory/Governance ChangeSet.
- Scoring deltas are a consumer-local Challenge policy, refused across policy versions.

**Result.** Comparison is never an Engine fact (all models agree), and scoring is consumer-local. The
**qualification rule** is consumer-neutral analytics responsibility within one revision: when a
measurement delta may be described, when attribution must be refused, and which definitions and
bases must match. Across revisions it belongs to the separate cross-revision question.

### PC-4 — Decision-affecting counterexample (`combinedQueueDepth`)

**Facts.** It is derived, its value can be recomputed at an observation boundary from `queueDepth`
plus `pendingWork`, and its exact arithmetic decides assignment (Phase 1 PC4). A rule that moved it
because it is recomputable would fail.

**Result.** All three models keep it: facts-only exempts derivations that participate in Engine
behavior. Under R1(b) it is Engine-owned even though it is unexposed. Under R3, analytics may count
waiting work at a boundary, but must neither present that count as the Engine's ranking state or a
physical queue (Phase 1 PC1: 3 per assembler, 6 summed, 3 real units), nor use it to predict
selection.

### PC-5 (added) — Late-joiner sufficiency for run-to-date aggregates (experiments B, C, C′)

This case discriminates Model 2.

**Facts.** The two pairs of runs are indistinguishable from their fresh observations plus the
published model, except for the accumulator itself. A consumer can rebuild the accumulator only from
complete events from run start. The late join reconstructs 20.0 of 40.0.

**Result.** **Model 2 fails** unless it keeps these aggregates, which is Model 3's R1(c), or imposes
a retention/replay contract that the runtime contract explicitly excludes from the supported
boundary. Models 1 and 3 survive.

### PC-6 (added) — Observation-clock coupling of throughput (experiment A; Q4)

**Facts.** `throughputPerTick` equals `completedOrders ÷ metadata.currentTime` (zero → 0) bit-exactly
at every corpus observation, including boundaries where advancement went past the last supported
event.

**Result.** Any owner can preserve the current relationship by consuming the same supported clock.
This case does not discriminate the models, but it closes Q4.

### Candidate × case summary

| | PC-1 | PC-2 | PC-3 | PC-4 | PC-5 | PC-6 |
|---|---|---|---|---|---|---|
| Model 1 (Engine-rich growth) | Inputs yes; "exhausts analysis" fails | **Fails on coupling** | Neutral | Pass | Pass | Pass |
| Model 2 (facts-only) | Pass | Pass for new measures | Neutral | Pass | **Fails / collapses** | Pass |
| Model 3 (refined) | Pass | Pass | Pass (qualification consumer-neutral) | Pass | Pass | Pass |

## Prospective responsibility classes

"Engine-necessary" means the class must be Engine-owned under R1.

| Class | Classification | Reason | Engine-supplied inputs; evidence dependency |
|---|---|---|---|
| KPI/metric history and baseline comparison | **Plausibly consumer-neutral** for the comparison qualification. History *retention* is consumer-owned capture unless evidence/provenance decides otherwise | Values at each boundary are Engine results or analytics measurements. The Engine retains no history (`runtime-contract.md:208-216`). Baseline deltas are comparisons (PC-3) | Observation snapshots; same-definition basis; cross-revision is a separate question |
| Occupancy intervals | **Plausibly consumer-neutral** | Measurement over outcomes (R2). Several legitimate groupings (resource, eligibility pool) | Dispatch, completion, and availability events gap-free from run start, or `busyTicks` plus running-step start times (not projected today) |
| Waiting-by-operation-step attribution | **Plausibly consumer-neutral** | Current waiting needs only observation plus model; waiting durations need events. Must attribute multi-eligible waiting to the shared eligible set, per the meaning of Engine §2 rule 7 | `pendingWork`, job status, published routing; dispatch and completion events for durations |
| Processing/waiting/transfer decomposition | Non-spatial part: **plausibly consumer-neutral**. Transfer part: **currently impossible to classify** | Transfer semantics are specified but not executed, and `TRANSFER_*` events are not emitted | Order acceptance, dispatch, and completion events |
| Utilization | **Plausibly consumer-neutral** (needed by a shared verification objective); **not Engine** (R2) | Capacity basis, interval, running-step treatment, availability, and admission load are measurement policy. No formula is selected | Processing-time facts (`busyTicks`, concurrency, state, `activeJobIds`); admission load once spatial executes |
| Starved/surplus classification | The label is **consumer-local** named-method interpretation (R4). The underlying predicate ("idle while no eligible work waits") is plausibly consumer-neutral measurement | "Surplus" is counterfactual: it needs Engine re-execution, not analytics | Resource state intervals, waiting attribution |
| Active-period and other bottleneck inference | **Consumer-local** named-method interpretation; **unnecessary as a shared responsibility** until two consumers need the same method | Method-relative and temporal (Roser et al. 2002). Occupancy ≠ constraint (strategy-space). The repository deliberately defines no universal algorithm | Interval measurements; controlled reruns for effective-constraint claims |
| Run/attempt comparison | Scoring: **consumer-local** (exists in Challenge). Measurement-delta qualification: **plausibly consumer-neutral** | PC-3 | One Engine outcome per run; ChangeSet for the change set |

**No class is Engine-necessary.** The consumer-neutral responsibility that survives is therefore
**named measurement plus comparison qualification**. It does not include interpretation, retention,
or presentation.

## Closing Phase 1 Q1–Q6

### Q1 — Utilization wording and responsibility

**Decision.** "Utilization facts" in the supported minimum means the **processing-time facts the
Engine already supplies**: `busyTicks` (completion-credited job-ticks), `concurrency`, `state`, and
`activeJobIds`. No normalized utilization is an Engine result, now or prospectively (R2). A
normalized utilization, including the one a "Maximum utilization <= threshold" objective needs, is a
consumer-neutral analytics measurement. Its definition must fix capacity basis, interval, running-step
treatment, availability, and, once spatial executes, admission load. It must refuse on insufficient
evidence. No formula is selected here.

**Change category, per the four distinctions the handoff requires:**

- **Clarification preserving existing supported facts.** Yes, for:
  - `runtime-contract.md:173`;
  - `engine-semantics.md` §10 bullet (`:474`), §10.1 rule 1's utilization sentence (`:505-506`),
    and §10.2 register row label (`:534`);
  - the §10 "existing bottleneck/capacity interpretation" and "bottleneck interpretation must widen"
    sentences (`:482-494`). Restate them as: the Engine exposes admission-load facts, and
    interpretation is consumer or analytics;
  - `overview.md:304`.

  These are textual clarifications with **no supported semantic change**: no implemented value,
  rule, or fixture changes.
- **Addition of a new supported result.** None in the Engine. A future utilization definition is an
  *analytics* addition.
- **Removal or narrowing of an existing obligation.** None. Qualification for review: a reader who
  takes the runtime contract's minimum as having promised a *normalized* result would call the
  clarification a narrowing of an unimplemented promise. Either way no produced value changes (Phase 1
  found no producer).
- **Future consumer-neutral measurement.** Yes, as above.

### Q2 — The four fresh-observation-recomputable fields

**Recomputability is not an ownership reason.** It is an *enabling condition*: moving a recomputable
field creates no evidence dependency (experiment A). It neither requires nor justifies moving it
(K2 from Phase 1 already fails).

- **`backlog` and `completedOrders`.** Current-state summary and run-to-date count, under R1(c).
  **Remain Engine-owned. No change.**
- **`averageLeadTime` and `throughputPerTick`.** Each contains measurement policy:
  - `averageLeadTime` is an arithmetic mean over the *completed* population, with an empty-set
    sentinel of 0. Mid-run it ignores incomplete orders, and with no completions it reports a
    misleading 0.
  - `throughputPerTick` is a rate whose interval is the observation clock from tick 0, with a
    zero-window sentinel of 0.

  They **remain Engine-owned as declared results (R1(d)); eligible, not warranted, for
  reclassification.** The reasons not to move them now:
  1. No proving case shows consumer harm, an evidence gap, or second-engine risk.
  2. Removing them is a both-contract change with no information gain (Phase 1).
  3. No analytics owner exists to receive them, so reclassification would orphan the definitions.
  4. Their coupling cost is theoretical while no change to their formulas is proposed.
- **Change category if later reclassified:** **both** Engine semantics (§10.1 rules 2–4; §10.2 rows
  and rules 1–2) and the runtime contract (Performance minimum and implemented record), plus the
  `FactoryRuntime` accessors (Q5) and the overview's operational-measure tables (`:446`, `:465`).
  Truthful consumers must already read `completedOrders` alongside them to interpret the 0 sentinels;
  reconciliation should state that.

### Q3 — The history-dependent accumulators

**`busyTicks` and `completedSalesValue` remain Engine-owned run-to-date aggregates of authoritative
change (R1(c)). No change.** Experiments B and C show neither is determined by the rest of a fresh
observation (plus the model, for `busyTicks`).

Moving them out would require one of these explicit input contracts:

- For `completedSalesValue`:
  - complete `ORDER_COMPLETED` events from run start, summed in supported-sequence order (sequence
    order matters for floating-point reproducibility); or
  - adding per-order unit price *and* completion order to the order projection.
- For `busyTicks`:
  - complete `JOB_STEP_COMPLETED` events plus published step durations; or
  - a per-resource credited ledger, which *is* the accumulator.

Both alternatives either impose retention/replay or recreate the field. Required textual
clarification: `busyTicks` is credited processing job-ticks of completed steps. It is not utilization
and not occupancy (Q1). The overview's commercial/operational/financial ownership tables stay
unchanged.

### Q4 — `throughputPerTick` and observation-clock coherence

Because it is retained, the relationship is preserved unchanged. If it is ever reclassified, the
analytics definition must take the **same observation's** `currentTime` and `completedOrders` as
inputs. Experiment A proves this reproduces the value bit-exactly. The observation-clock rule stays
owned by the runtime contract (`overview.md:302-304`) and is consumed, not redefined, so formula and
clock are not split. A throughput over any other clock, such as an advancement target, is a different
measurement and needs a different name.

### Q5 — `FactoryRuntime` aggregate accessors

The surviving model changes no public operation.

- **`backlog()`, `avgLeadTime()`, `completedSalesValue()`, `completedSales()`.** They expose the
  Engine-owned results above.
- **`throughput(long)`.** It evaluates the Engine's zero-window rule over a **caller-chosen window**.
  Under R2 that window is caller-supplied measurement policy: the value is an Engine-arithmetic helper,
  not an Engine-supported observation result. It is coherent with an observation only if the caller
  passes that observation's `currentTime`.
- **Reconciliation consequence.** State the method-level support of these accessors explicitly; do
  not infer "unsupported" from documentation silence.
- **Change category.** If a later decision withdrew `throughput(long)`, that would be a
  **public-operation change only**: no Engine-semantics change, because §10.1 rule 3 still governs the
  observation field, and no runtime-contract change, because the observation is unchanged.
  Withdrawing the others follows Q2/Q3 if their results are also reclassified; otherwise it is a
  public-operation change.

### Q6 — Boundary-rule proving cases

The rule keeps `combinedQueueDepth` in the Engine (R1(b), PC-4). It permits consumer-side
measurement over supported evidence:

- step-first waiting attribution reads observation plus model and attributes shared waiting to the
  eligible set;
- occupancy reads dispatch and completion facts.

It forbids reconstructing Engine choices: ranking, "which machine would this job get", and
mid-cascade queue state. Counterfactual "what if" claims require Engine re-execution (R3).

## Contract-change accounting

### Per current field and operation

| Item | Consequence |
|---|---|
| `backlog`, `completedOrders` | No change |
| `averageLeadTime`, `throughputPerTick` | No change now; eligible for a later **both** reclassification |
| `busyTicks` | Textual clarification (description), no supported semantic change |
| `completedSalesValue` | No change |
| "Utilization facts" (runtime contract), §10/§10.1/§10.2 utilization and bottleneck wording | Textual clarification, no supported semantic change |
| `combinedQueueDepth` | No change; Engine-owned (R1(b)) |
| `FactoryRuntime` accessors, including `throughput(long)` | No change; method-level support to be stated (reconciliation) |
| `FactoryHandler` aggregates | No change; factoring stays implementation-only (R5) |

### Per durable surface

| Surface | Requires revision? |
|---|---|
| Runtime observation/event contract | **Textual clarification only**: the Performance minimum wording. No event or observation semantic change |
| Determinism Contract / determinism architecture | **No revision required.** Analytics derived values are outside the Engine interpretation; their determinism and reproducibility are owned by the evidence/provenance question. An optional clarifying sentence ("derived results" in Determinism rule 1 means Engine-supported derived results) is reconciliation's choice |
| Engine semantics | **Textual clarification only**: §10, §10.1 rule 1, §10.2 row label. Optional: a sentence noting that report-only derived results are included because they are declared supported, not because they interact. No definition change and no fixture change |
| Architecture Overview ownership text | **Yes, textual.** Record the R1–R5 ownership principle in the cross-cutting principles (`overview.md:20-29` area), correct `:304`, and add analytics/consumer rows where ownership tables list measurement. The commercial/operational/financial tables are unchanged |
| Factory/runtime public operations | **No change required.** Method-level support statement recommended (Q5) |

## Adversarial analysis

**Self-administered only.** This is not an independent adversarial review.

| Challenge | Result |
|---|---|
| Model 3 was predetermined; the report just re-labels the status quo | Partly upheld as a framing risk. The *current-field* outcome equals Model 1's, and this is stated plainly. The selection rests on PC-2 (Model 1 growth fails on coupling) and PC-5 (Model 2 fails executably), not on preference. A reviewer could call the surviving model "Model 1 with a growth freeze"; the semantic content is identical |
| The R1(d) retention of `averageLeadTime`/`throughputPerTick` is an ad hoc exception | Qualified, not falsified. It is one explicit class with a stated cost/benefit reason and a revisit trigger, and R2 still constrains all admissions. If the reviewer judges two retained policy-laden fields to undermine R2, the narrower fallback is Model 3-strict (move them once an analytics owner exists). Removal remains a both-contract change |
| "Coupling" against Model 1 is a cost, not a falsification | Accepted as a characterization. Model 1 is rejected as a *growth* model because the coupling has no offsetting necessity: report-only results do not interact with execution (Phase 1), several legitimate definitions exist (PC-2), and the needed refusal semantics conflict with §10.1 sentinels. Evidence that two consumers need one Engine-fixed utilization with total-function semantics would weaken this |
| The consumer-neutral responsibility is speculative: no current product code computes a shared measurement | Partly upheld; confidence is **medium**. The responsibility is grounded in accepted architecture (shared verification objectives, Governance producer ownership) and planning (the game is barred from owning generic analytics), not in an implemented consumer. No capability is admitted; R2's "otherwise consumer-local" branch handles the no-shared-use case |
| Experiment A only covers corpora without saturation or huge values | Accepted as a limitation. Saturation arithmetic is replicated in the recomputation but not exercised; Engine saturation itself is pinned by `EngineDerivedResultConformanceTest` |
| Experiment C forces assignment with availability toggles, which a reviewer might call artificial | The point is only that the observation and model do not *determine* `busyTicks`. Any two runs differing in historical assignment suffice, and a completed job's machine is cleared by construction (`Job.completeStep`) |
| A research-local oracle was smuggled in as an analytics definition | No. Oracles are cited only as feasibility and counterexample evidence; no definition is adopted |
| An external analogy is used as necessity | No. Industry precedent is recorded as showing both placements; the decisive reasons are Arcogine-specific |
| Staleness | `main` moved once (#448) relative to Phase 1, with no relevant change. Final recheck recorded below |

## Surviving invariants

1. Decision-affecting interpretation is Engine-owned whether or not it is exposed or recomputable.
2. Run-to-date aggregates of authoritative change that a fresh observation cannot otherwise express
   must be supplied by the Engine, or else a retained-evidence contract must exist. The Engine's
   order-level results do not exhaust useful diagnosis.
3. Measurement policy — interval, normalization basis, statistic, attribution, classification,
   inference, refusal, cross-run scope — does not enter the single Engine interpretation.
4. Analytics measures from supported evidence, declares its inputs, refuses when the evidence is
   insufficient, and never re-decides Engine choices. Counterfactuals require Engine re-execution.
5. Diagnostic interpretation is named-method and consumer-owned. Comparison scoring is consumer-local.
   Comparison qualification is consumer-neutral within one revision.
6. Recomputability is an enabling condition for reclassification, never a reason for it.

## What did not survive

- **Model 2 (facts-only) in strict form** — PC-5. Executable reusable negative knowledge: two runs
  indistinguishable by observation and model except an accumulator.
- **Model 1 as a growth model**, and the claim that the Engine-rich status quo exhausts analysis —
  PC-1, PC-2.
- **"Recomputable, therefore analytics"** — PC-4 and Q2.
- **"Utilization facts" read as an implied Engine-normalized ratio** — Q1.
- **"Comparison is an Engine fact because execution is deterministic"** — PC-3. A non-additive
  multi-variable delta is the reusable counterexample.
- **The R-obs/R-hist split as an ownership boundary.** It remains a consequence axis only.

## Confidence and limitations

- **High:**
  - strict facts-only fails (PC-5, executed);
  - `combinedQueueDepth` stays Engine-owned;
  - no prospective class is Engine-necessary;
  - Engine order-level results do not exhaust diagnosis (D1 plus the concluded strategy-space
    counterexample);
  - comparison is not an Engine fact (D3 plus Challenge code);
  - the Q3 and Q4 closures.
- **Medium-high:**
  - Model 1 rejected as a growth model (a coupling argument grounded in §1 text, not an
    impossibility);
  - Q1 as clarification rather than narrowing.
- **Medium:**
  - existence of the consumer-neutral responsibility as a category (accepted architecture and
    planning, not implemented use);
  - retaining rather than moving `averageLeadTime`/`throughputPerTick` (a cost/benefit judgment an
    owner could reasonably make differently without contradicting R1–R5).
- **What would change the conclusion:**
  - a concrete requirement that a normalized utilization be Engine-fixed and identical for all
    consumers with total-function semantics (strengthens Model 1);
  - evidence that every measurement need is consumer-specific, for example verification objectives
    re-owned as game or Challenge content (removes the shared responsibility; R2 then routes
    everything consumer-local);
  - a decision to make the Engine event-retaining (weakens PC-5);
  - spatial execution showing that admission-load facts must be interpreted inside the Engine
    (rechecks Q1 and PC-2).
- **Not inspected or not available:**
  - no outward adapter exists;
  - spatial/transfer execution is not implemented;
  - no verification-objective consumer is implemented;
  - two of four external sources are unverified, and none is load-bearing;
  - the custody experiment did not exercise saturation;
  - PR #454 was closed without merging, so register state on `main` is `READY`.
  - All required repository surfaces in the handoff's grounding list were inspected. Searches covered
    `RuntimePerformanceObservation`, `RuntimeObservation`, `busyTicks`, `combinedQueueDepth`,
    `averageLeadTime`, `throughputPerTick`, `completedSalesValue`, `utilization`, `occupancy`,
    `bottleneck`, `waiting`, `AttemptComparison`, `AuthoritativeOutcomeFacts`, and historical `kpi`
    across `docs/` and `product/`.
- **Validation**, all in the `gradle:9-jdk21` Docker workflow at `8c2ed14`:
  - Custody experiment: 6/6 passed.
  - A freshly executed baseline selection (no up-to-date reuse) passed 198/198 with 0 failures:
    - Factory: `EngineDerivedResultConformanceTest` 3, `EngineDispatchConformanceTest` 17,
      `FactoryHandlerTest` 17, `HeadlessClosureAcceptanceTest` 5, `RuntimeObservationAcceptanceTest`
      5, `JobLeadTimeTest` 1;
    - the complete `:research-experiments:test` suite (129, including the corpus, oracle, and
      package-boundary tests);
    - Challenge: `ChallengeAttemptComparatorTest` 9, `ReferenceChallengeEvaluationPolicyTest` 12.
  - No full quality/coverage gate was run for this research-document change.
- **Final repository recheck:** live `main` was `4d83f143143bf778881ba842bd4949734ce400c9` when this
  report was completed. Its only change since the baseline is a Storage test (#452), so there is no
  effect.

## Unresolved unknowns

- **The first concrete admitted analytics use.** Likely candidates are the diagnostic-evidence
  study's occupancy and waiting requirements, or a utilization verification objective.
- **Late-joiner inputs for interval measurement.** Options include projecting running-step start
  times, caller-owned complete capture, or an analytics-owned accumulator; online-time history is
  needed for an availability-aware capacity basis. This is the evidence/provenance question.
- **Analytical-definition identity and provenance.** What Governance's producer-provenance rule
  needs from analytics. This is the evidence/provenance question plus Governance.
- **Consolidation of the retained Engine measures.** Whether `averageLeadTime`/`throughputPerTick`
  should eventually move to analytics. This is an owner choice under R1(d)/R2.
- **Method-level support of the `FactoryRuntime` accessors.** This is for reconciliation.

## Durable consequences

None of these is performed here. All follow independent adversarial review.

1. **Architecture (overview).** Record R1–R5 as the Engine / analytics / consumer ownership
   principle. Correct the `busyTicks` description at `:304`.
2. **Runtime contract.** Clarify the Performance minimum: order-level results plus resource
   processing-time facts; no Engine-normalized utilization.
3. **Engine semantics.** Clarify the §10 utilization and bottleneck sentences, §10.1 rule 1, and the
   §10.2 row label. Optionally note why report-only results are bundled. No definition change.
4. **Governance evidence §12–§13.** Analytical-definition ownership is resolved toward analytics
   producers; provenance detail goes to the evidence/provenance question.
5. **Planning.**
   - Update the game consumer plan's ownership row (`:114`) and §Visualization text (`:136-138`).
   - The slice gate (`vertical-slice.md:39`) can then be satisfied for reusable measurement once
     evidence/provenance resolves.
   - Close the open-ownership note in `spatial-runtime-consequences.md:147-167`: the pinned results
     stay Engine-owned.
   - Update `factory-simulation-engine-readiness.md:100`.
6. **Implementation responsibility, small and textual.** Correct the `FactoryHandler` comment
   ("real utilization"). Optionally rename the consumer-side "utilization" comparator in
   `HeadlessClosureAcceptanceTest`. Neither changes behavior.
7. **Register.**
   - Umbrella question → `CONCLUDED` only after reconciliation lands.
   - Evidence/provenance → `READY` on acceptance (see triggers).
   - Adapter compatibility and cross-revision comparability unchanged.
8. **Reusable assets.** Experiments B, C/C′, D1, and D3 are durable proving cases for the
   evidence/provenance and diagnostic-evidence questions. The knowledge-transfer audit should promote
   them into the tracked research-experiments corpus rather than retain this report. Experiment A is
   recoverable from first principles and may be discarded once Q2/Q4 are reconciled.
9. **No synthesis seed** is nominated: the negative results are recoverable from first principles and
   retained executably.
10. **No historical decision-rationale record** is required unless reconciliation adopts the R1(d)
    retention trade-off. If it does, a short record of why `averageLeadTime`/`throughputPerTick` were
    retained, and when to revisit, meets the retention test.

## Implementation implication

No implementation now. The ownership conclusion requires no code change. Reconciliation may correct
one code comment and optionally one test's naming. No analytics module or capability is admitted.

## Downstream research triggers

| Question | Decision | Why |
|---|---|---|
| Evidence, completeness, and provenance | **Trigger satisfied conditionally: promote to `READY` when this conclusion is accepted** after independent review and reconciliation; not before | This report establishes a consumer-neutral responsibility (named measurement plus qualification) and identifies a result whose truthfulness depends on a reusable evidence contract: processing occupancy over an interval, and utilization built on it. That result needs gap-free events from run start, availability history, and running-step start times (PC-2; `ProcessingOccupancyOracleTest` refusals; experiment B's late join). Suggested scope: the occupancy/utilization measurement class |
| Cross-adapter compatibility | **Remains `CANDIDATE`** | No evidence contract settled; no second concrete supported consumption boundary |
| Cross-revision comparability | **Remains `CANDIDATE`** | This result creates no cross-revision comparison use; within-revision qualification is covered by R3/PC-3 |
| Factory-design game diagnostic evidence (existing, READY) | Status unchanged | It receives its ownership input: the game owns presentation and named interpretations, generic measurements are analytics-owned definitions, and none may be defined locally as game semantics |

## Follow-up triggers

- Independent adversarial review of this exact report revision (next).
- Spatial execution landing: recheck Q1/PC-2 for admission load, and classify transfer decomposition.
- The first admitted analytics use (feeds evidence/provenance).
- Any proposal to change `averageLeadTime`/`throughputPerTick` formulas or sentinels: decide between an
  Engine definition change and reclassification.
- Introduction of an outward adapter (DTO hardening constraint; adapter question).

Track these through the existing register entries. No new register question is proposed.

## Sources

**Repository, at the baseline:**

- `AGENTS.md`; `.github/agents/researcher.agent.md`; `docs/development/researching.md`;
  `docs/development/testing.md`
- `docs/research/research-register.md` and the briefs:
  - `simulation-analytics-consumer-boundary.md`
  - `simulation-analytics-evidence-provenance.md`
  - `simulation-analytics-adapter-compatibility.md`
  - `simulation-analytics-cross-revision-comparability.md`
  - `factory-design-game-diagnostic-evidence.md`
  - `factory-design-game-strategy-space.md`
- `docs/architecture/overview.md`, `engine-semantics.md`, `runtime-contract.md`, `factory-design.md`,
  `governance-evidence.md`, `governance-conformance.md`
- `docs/planning/factory-design-game-consumer.md`, `factory-design-game-vertical-slice.md`,
  `spatial-runtime-consequences.md`, `factory-simulation-engine-readiness.md`
- `product/domains/factory/.../process/{FactoryRuntime,FactoryHandler,RuntimeObservation,RuntimePerformanceObservation,ResourceObservation,OrderObservation,JobObservation,RuntimeEventPayload}.java`;
  `.../jobs/Job.java`; `.../orders/Order.java`; `.../model/{FactoryModel,OperationDefinition,OperationStepDefinition,ProductDefinition}.java`
- `product/consumer/challenge/.../evaluation/{AuthoritativeOutcomeFacts,ChallengeEvaluationInput,EvaluationProvenance,ReferenceChallengeEvaluationPolicy}.java`;
  `.../comparison/{AttemptComparison,AttemptComparisonResult,AttemptComparisonWinner,ChallengeAttemptComparator}.java`
- `product/research-experiments/src/main/.../{package-info,Oracle,OracleOutcome,ResearchDefinition,EvidenceInput,ProcessingOccupancyOracle,ProcessingIntervals,EligibilityPoolOccupancyOracle,DispatchProfileOracle,CompletionTickOracle,WaitingWorkByStepOracle,ExperimentRunner,ExperimentEvidence,ExperimentFixture,ExperimentStep,LinearRoutingFamily,ThreeStepRoutingFamily}.java`;
  test corpora `StarterCorpus`, `CapacityCorpus`
- Phase 1 report at workspace commit `7e72a46506bf63febcb57d7b7dcc9ce0cd484d64` (path above)
- Custody experiment `workspace/research/experiments/simulation-analytics-ownership/com/arcogine/research/experiment/PerformanceEvidenceSufficiencyExperiment.java`
  (committed with this report)

**External** (verification status in § External evidence):

- Roser, Nakano, Tanaka (2002), *Proc. 2002 WSC*, pp. 1087ff., §2 — verified in session.
- Roser, Nakano, Tanaka (2001), *Proc. 2001 WSC*, pp. 949–953 — background, unverified.
- AnyLogic `ResourcePool` reference — background, unverified (HTTP 403).
- SimPy "Monitoring" topical guide — fetched; implication only.
