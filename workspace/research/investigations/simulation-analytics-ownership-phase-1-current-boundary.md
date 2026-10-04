# Simulation analytics ownership boundary — Phase 1: current Engine/runtime ownership classification

> **Research status:** ACTIVE. The registered umbrella question remains open for Phase 2. This report
> is the Phase 1 evidence checkpoint of the coupled packet, not a conclusion of the registered
> question.
>
> **Research baseline:** live `main` at `0b82db0a49be483af3fed2415a0ad52e39a45f27`, resolved
> 2026-10-04.
>
> **Task definition:** phased research-definition commit `22188d4bf5fedfa44795b4e2890b03d11266edb0`.
> It is landed: `main` received it by squash merge (#451), and the baseline tree is identical to the
> definition commit's tree (empty `git diff`). The brief and register entry read here are therefore
> landed repository truth. The workspace handoff commit `5bbff8dfd81125aae1fb608087894341f1aa8369`
> added only the handoff prompt. It is material under investigation, not repository truth.
>
> **Final repository recheck:** research continuation on 2026-10-05 resolved live `main` to
> `8c2ed14dccde0ca1dbb51e58c94ff19d822856a1`. The intervening coverage/build, test, and Storage changes
> leave the cited Factory runtime implementation, Engine/runtime contracts, planning, and proving
> cases unchanged. The classifications therefore still hold. This is a new report revision;
> the earlier completed revision remains preserved in Git history.
>
> **Risk:** High, as the brief classifies it: major ownership, supported observable semantics, and
> deterministic Engine behavior.
>
> **Authority:** Research evidence only. This report changes no architecture, specification, runtime
> contract, product direction, or implementation commitment. Current Engine semantics, the runtime
> observation/event contract, and the Determinism Contract stay exactly as accepted.
>
> **Adversarial-review status:** required, not yet performed. The only adversarial pass applied to
> this revision is the author's self-challenge (§ Adversarial analysis). Phase 1 makes no final
> ownership recommendation. Any later high-risk ownership recommendation that depends on these
> classifications needs genuinely independent adversarial review before architecture promotion.

## Question

> Which currently supported runtime facts and derived results are semantically Engine-owned under the
> accepted contracts, and why?

The question is the brief's Phase 1 question, unchanged. The investigation adds two descriptive
axes. Neither is used as an ownership rule. Both separate cases the brief's four ownership reasons
otherwise blur:

- **Decision-affecting versus report-only.** Engine semantics §1.1 counts a change to a *derived
  result* as a change of outcome. So "result-affecting" covers two different things. Some
  arithmetic changes acceptance, assignment, ordering, or timing; this report calls that
  *decision-affecting*. Other arithmetic changes nothing except the reported value itself; this
  report calls that *report-only*.
- **Fresh-observation sufficiency.** This asks whether a value is a function of other facts in the
  same fresh `RuntimeObservation`, or whether it also needs supported event history and published
  model facts. The runtime contract requires a fresh observation to suffice without replay
  (`runtime-contract.md:63`). That makes this a real consequence axis for any reclassification.

## Decision at stake

Phase 1 decides nothing on its own. It fixes the description of the current supported boundary that
Phase 2 must reason from. For each plausible reclassification it records what kind of change would
be needed: Engine-semantics change, runtime-contract change, both, or implementation-only
reorganization. Phase 2 can then weigh the cost of reclassification without first re-deriving it.

## Scope and non-goals

In scope: `RuntimeObservation` state projections; every `RuntimePerformanceObservation` field;
`busyTicks` and its accumulator; the `FactoryHandler` aggregates and the `FactoryRuntime`
accessors that expose them; `combinedQueueDepth`; the removed `com.arcogine.core.kpi.*` package as
historical evidence.

Out of scope, per the brief and handoff:

- choosing an Engine-rich, facts-only, or mixed model;
- deciding whether a consumer-neutral analytics capability should exist;
- the prospective ownership rule;
- formulas for utilization, occupancy, bottleneck, starvation, or comparison;
- retention, provenance, adapter compatibility, and cross-revision comparability;
- module/API migration;
- any edit to code or accepted architecture.

## Executive conclusion

**1. The in-scope execution facts and supported performance results are currently owned by the
Engine / Factory runtime.** Authored model facts and model fingerprints retain Factory ownership
when projected; the runtime owns their faithful projection and observation coherence. The removed
KPI package is historical only. No current in-scope supported performance result is owned by a
consumer or by an analytics capability. Confidence: high. The basis is direct
normative text (`engine-semantics.md` §1.1, §2 rule 3, §10, §10.1, §10.2; `runtime-contract.md`
observation minimum; `overview.md` ownership tables), matching implementation, and 113 relevant
passing test results, with execution and Gradle reuse distinguished below.

**2. The ownership reasons differ, and only one in-scope derived quantity is decision-affecting.**

| Class | Members | Why Engine-owned today |
|---|---|---|
| **S** — projection of authoritative state/change | resource state, `activeJobIds`, `queueDepth`; order and job projections; pending work; observation metadata | They project authoritative runtime state, and the runtime contract requires them |
| **D** — decision-affecting interpretation | `combinedQueueDepth` | Its exact arithmetic decides assignment. It is not exposed in any observation. Its ownership is not contestable under any candidate the brief names |
| **R-obs** — report-only supported result, recomputable from the same fresh observation | `backlog`, `completedOrders`, `averageLeadTime`, `throughputPerTick` | Only because accepted contracts declare them supported Engine results whose arithmetic is part of the interpretation. None of them participates in any Engine decision |
| **R-hist** — report-only supported accumulator carrying history a fresh observation cannot otherwise express | `busyTicks`, `completedSalesValue` | Same reason as R-obs. Removing them while retaining other projection shapes would lose general fresh-observation reconstruction unless additional evidence were supplied |
| **M** — authored model facts echoed into the resource projection | `name`, `concurrency`, `capacityLiters`, `setupTime` | Owned by the Factory model, not the runtime. `setupTime` is not used by the runtime at all |

For R-obs and R-hist, ownership follows from **support status, not decision participation**. That
conditionality is the hinge Phase 2 must decide. The repository flags the same hinge itself
(`docs/planning/spatial-runtime-consequences.md:147-173`).

**3. Current contract text is ambiguous about "utilization."** "`busyTicks` /
utilization" appears as a supported derived result in `engine-semantics.md:474`, `:505` and `:534`.
"Utilization facts" appear in the runtime contract's Performance minimum
(`runtime-contract.md:173`). Overview says `busyTicks` "reports cumulative utilization"
(`overview.md:304`). **No normalized utilization ratio is produced by the current runtime.**

- The normalized ratio used by the headless bottleneck acceptance case is consumer-side, inside a test:
  `busyTicks / elapsed` in `HeadlessClosureAcceptanceTest.java:461-469`.
- That formula is falsified for `concurrency > 1` by the pinned research oracle: it gives
  36 / 26 ≈ 1.38 (`ProcessingOccupancyOracleTest.java:69-85`).
- It also reads zero while a resource is fully occupied (`ProcessingOccupancyOracleTest.java:52-67`).

This leaves a **genuinely contestable interpretation of the supported phrase "utilization facts"**:
it may refer to processing-time inputs such as `busyTicks`, or suggest a further normalized result
whose definition is absent. The accumulator's current owner and formula are clear. Phase 1 does
not resolve the phrase by withdrawing its declared support or inventing a ratio.

**4. Several further tensions do not change today's owner but do affect Phase 2:**

- The `throughputPerTick` window is the runtime contract's observation clock, not the time a
  consumer advanced to. Its definition therefore spans two authorities.
- The public `FactoryRuntime` KPI accessors sit outside `observe()`. Their individual method-level
  support is not separately specified, although runtime documentation broadly promises read-only
  projections. The underlying result semantics are clear; omission does not license removal.
- No executable fixture was found for `combinedQueueDepth` exactness above the 32-bit range
  (`engine-semantics.md` §14 item 17). Exactness currently rests on the `long`-typed implementation.

**5. Reclassifying any R-obs or R-hist item is an Engine-semantics *and* runtime-contract change.**
Reclassifying `completedSalesValue` also touches the overview's operational/financial ownership
tables. Moving the code while keeping formula, arithmetic, accumulation, and conformance unchanged
is implementation-only. `combinedQueueDepth` could change only through an Engine-semantics
definition change. It is not a candidate for analytics.

## Repository evidence

All paths and line numbers are at the research baseline unless marked historical.

### Normative ownership text

- **Engine owns derived-result rules.** The Engine owns deterministic interpretation including
  "derived-result rules under one identified interpretation." Consumer domains, including
  analytics, "never redefine authoritative production semantics" (`overview.md:20-29`).
- **Membership test.** A rule belongs to the interpretation if changing it can change "acceptance or
  rejection, assignment, ordering, timing, or derived result" for identical explicit inputs
  (`engine-semantics.md:70-72`). This covers derived-result arithmetic "as much as acceptance
  limits," including the **accumulation** feeding a value (`engine-semantics.md:86-92`).
- **§10 interpretation.** `busyTicks`/utilization are processing-time measures. Queue depth is the
  existing queued/waiting count. Backlog counts incomplete accepted orders. Throughput is completed
  orders per observed time (`engine-semantics.md:470-480`).
- **§10.1.** Saturating `busyTicks`, elapsed-time subtraction floored at zero, zero-window
  throughput = 0, and empty-set mean lead time = 0 are part of the interpretation
  (`engine-semantics.md:496-523`).
- **§10.2 accumulator register.** It covers `busyTicks`/utilization, mean lead time, throughput,
  backlog, and completed sales value, with saturating, exact-counting, and completion-ordered rules
  (`engine-semantics.md:525-564`).
- **§2 rule 3.** `combinedQueueDepth` is an exact ranking key "unlike the derived-result
  accumulators of section 10.2" (`engine-semantics.md:163-177`).
- **§1.2.** Waiting work not queued on any single machine must be observable separately from
  per-machine queue depth (`engine-semantics.md:146-148`).
- **§3 rule 4.** Backlog, completed sales and value, lead time, and order throughput are order-level
  facts (`engine-semantics.md:246-247`).
- **Runtime contract.**
  - The observation minimum includes Resources (queue depth, active work, status) and "Performance:
    supported throughput/lead-time/backlog/utilization facts" (`runtime-contract.md:135-176`).
  - A fresh observation must suffice without replay (`runtime-contract.md:63`).
  - Challenge/Game may consume Engine facts but does not define them (`runtime-contract.md:261`).
- **Overview ownership tables.**
  - Factory owns "production metrics" (`overview.md:269`).
  - The order aggregate "is the sole source for order completion, backlog, sales KPIs, and lead
    time" (`overview.md:271`).
  - `CompletedSalesValue` is listed as a fact owned by `FactoryHandler` and mutated by `TaskEnd`
    (`overview.md:290`).
  - Completed sales value, backlog, throughput, and lead time are Factory "operational measures"
    (`overview.md:446`, `:465`).
  - A computed value over another domain's state belongs "as a method/projection on the existing
    owner ... or in a separately supported consumer projection, not a new module"
    (`overview.md:483`).
- **Repository's own open-ownership flag.** The planning note pins the reported derived results
  while stating that pinning "does not settle ownership." It forbids hardening them into new outward
  contracts while research is open, and carves `combinedQueueDepth` out as Engine semantics
  "regardless of the analytics outcome" (`docs/planning/spatial-runtime-consequences.md:147-173`).
  The readiness plan says the same about the removed KPI path and no scheduler-internal analytics
  (`docs/planning/factory-simulation-engine-readiness.md:100`).
- **Semantic neighbours that presuppose utilization.**
  - Verification objectives include "Maximum utilization <= threshold"
    (`docs/architecture/factory-design.md:320-331`).
  - Verification-objective semantics, including utilization, remain domain-owned, and "Engine and
    analytics producers own calculation semantics" (`docs/architecture/governance-evidence.md:273-276`).
  - The ISA-95 mapping lists utilization among derived operational measures (Partial)
    (`docs/architecture/isa-95-semantic-mapping.md:128`).

### Implementation

- **Observation assembly.** `FactoryRuntime.observe()` (`FactoryRuntime.java:638-704`) builds:
  - resource projections from `MachineView` (`:639-651`);
  - order, job, and pending-work projections (`:652-687`);
  - the performance record from the handler aggregates, with throughput over `observedTime`
    (`:688`, `:702-703`).
- **Observation clock.** `observedTime` advances only when a supported event is emitted
  (`FactoryRuntime.java:79-92`, `:517-526`).
- **Handler aggregates** (`FactoryHandler.java`):
  - fields `completedSalesValue`, `completedSales`, `completedLeadTimeTicks` (`:42-44`);
  - `backlog()` counts incomplete order aggregates on demand (`:138-140`);
  - `avgLeadTime()` is `completedLeadTimeTicks / completedSales`, with an empty set giving 0
    (`:142-147`);
  - `throughput(elapsedTicks)` gives 0 for a zero window (`:149-154`);
  - on final order completion, value is added in completion order, the count is incremented, and
    lead time is added with saturation (`:369-381`).
- **`busyTicks` crediting.** At `TaskEnd` the finished step's authored duration is added to the
  performing machine, saturating at `Long.MAX_VALUE` (`FactoryHandler.java:353-365`). The comment
  claims this "expresses real utilization."
- **Not decision inputs.** No selection or dispatch path reads `busyTicks` or any performance
  aggregate. `selectMachine` uses only `canAcceptJob`, `combinedQueueDepth`, and `MachineId`
  (`FactoryHandler.java:162-174`). Repository search found `busyTicks` used only at crediting, the
  `Machine`/`MachineView` accessors, and `observe()`. The aggregates are used only by
  `FactoryRuntime` pass-throughs, `observe()`, and tests.
- **`combinedQueueDepth`** is `long`: the own queue size plus the count of pending multi-eligible
  entries whose eligible set contains the machine (`FactoryHandler.java:181-186`). It appears in no
  observation record.
- **Machine state.**
  - `canAcceptJob` means not offline and active count below concurrency (`Machine.java:36-38`).
  - The machine is `Busy` once any job starts and returns to `Idle` when none is active
    (`Machine.java:50`, `:67`). `Busy` therefore does not mean saturated.
  - Taking a machine offline while it has active jobs is rejected (`FactoryRuntime.java:211-214`).
    So a started step is never interrupted in the current supported execution. Before saturation,
    its completion credit equals its processing duration; a saturated accumulator loses exact totals.
- **Shared time arithmetic.** `SimTime.minus` floors at zero and `SimTime.plus` is unchecked
  (`product/types/.../SimTime.java:14-21`). Both implement Engine §10.1 rules 2 and 5 from the
  shared `types` module.
- **Projection shapes.**
  - `OrderObservation` carries quantities, `createdAt`, `completedAt`, and `complete`, but **no
    price**.
  - Price appears in the `ORDER_ACCEPTED` and `ORDER_COMPLETED` event payloads
    (`RuntimeEventPayload.java`).
  - `JOB_STEP_COMPLETED` carries machine and step index but no duration.

### Executable evidence exercised at the baseline

Verification used the documented `gradle:9-jdk21` Docker workflow and the repository's Gradle
9.8.0 wrapper. The executed `C:/work/arcogine/product` tree at
`e4f98a642ebf29cedf99307574a9ddfd920f6567` is byte-identical in Git to the report baseline's product
tree (`git diff e4f98a6 0b82db0 -- product` is empty).

Across two targeted invocations, **96 Factory tests executed and passed**. Gradle reported
`:research-experiments:test` **UP-TO-DATE**; its existing XML results confirm **17 passing oracle
tests**, zero failures/errors/skips. Thus 113 relevant results were validated, not 113 freshly
executed tests. Both commands ended `BUILD SUCCESSFUL`; the only build notice was Gradle deprecation
guidance. No full Java quality/coverage gate, new experimental harness, CI workflow, or independent
adversarial review is claimed for this research-document change.

| Test class | Tests | What it pins for this report |
|---|---|---|
| `EngineDerivedResultConformanceTest` | 3 | Saturating `busyTicks` and lead-time register; zero-window throughput; empty mean lead time; lead-time floor; ordinary per-step crediting |
| `EngineDispatchConformanceTest` | 17 | `combinedQueueDepth` ranking, including every compatible shared entry counting toward each candidate (`:327-365`), and assignment- and lead-time-changing overlap fixtures (`:203-213`) |
| `FactoryHandlerTest` | 17 | Handler aggregates, including `throughput(100) = 0.1` and `throughput(0) = 0` (`:146-153`) |
| `HeadlessClosureAcceptanceTest` | 5 | Fresh-observation sufficiency; marker no-op leaves observation, time, and throughput unchanged; consumer-side bottleneck by load and by `busyTicks / elapsed` (`:450-497`) |
| `RuntimeObservationAcceptanceTest` | 5 | Projection of authoritative state; `backlog` and `completedOrders` in `performance()` |
| `ProportionalQuantityWorkTest` | 13 | Order-level completion count, value, and lead time under quantity decomposition |
| `ProcessingOccupancyOracleTest` | 10 | Proving cases 2 and 3 (below) |
| `WaitingWorkByStepOracleTest` | 7 | Proving case 1 (below) |
| `MachineStateTest` | 10 | Concurrency, state transitions, availability rejection and machine queue behavior |
| `RuntimeEventDeliveryAcceptanceTest` | 13 | Observation/event cursor agreement, deterministic semantic events, rejection and applied-change boundaries |
| `SessionControlAcceptanceTest` | 13 | Supported session controls and independently observable multi-eligible waiting |

The exact Gradle task selections were:

```text
./gradlew :factory:test
  --tests com.arcogine.factory.process.EngineDerivedResultConformanceTest
  --tests com.arcogine.factory.process.EngineDispatchConformanceTest
  --tests com.arcogine.factory.process.RuntimeObservationAcceptanceTest
  --tests com.arcogine.factory.process.HeadlessClosureAcceptanceTest
  --tests com.arcogine.factory.process.RuntimeEventDeliveryAcceptanceTest
  --tests com.arcogine.factory.process.FactoryHandlerTest
  --tests com.arcogine.factory.machines.MachineStateTest --no-daemon

./gradlew :factory:test
  --tests com.arcogine.factory.process.ProportionalQuantityWorkTest
  --tests com.arcogine.factory.process.SessionControlAcceptanceTest
  :research-experiments:test
  --tests com.arcogine.research.experiment.ProcessingOccupancyOracleTest
  --tests com.arcogine.research.experiment.WaitingWorkByStepOracleTest --no-daemon
```

Each selection was passed as one command via `docker run --rm`, mounting `product` at `/app`,
using `/app` as working directory and `arcogine_gradle_cache:/root/.gradle` as documented.

The research-experiments oracles are research-local derivations (`researching.md` §4,
`overview.md:523`). Here they serve only as **counterexample evidence about the meaning of Engine
facts**. They are not candidate analytics definitions.

### Historical evidence

The removed `com.arcogine.core.kpi` package was read at exact historical commit
`2e13eb5fcf71823740453b1c80d0e792fa38a746`, parent of removal commit
`cf67df8f28c5bf466960215e3f4552d509db58a4` (#388, "prune legacy simulation substrate," 2026-09-24).

- **Shape.** `Kpi.compute(EventLog, SimTime)` returned `KpiValue(name, value, unit)` over the
  internal scheduler-event log.
- **Members:** `ThroughputRate`, `OrderCount`, `EventCount`, `TotalSimulatedTime`. No utilization
  KPI existed in that package at that revision.
- **`ThroughputRate`** counted internal `TaskEnd` events — **step completions** — over
  `max(1, currentTime)` ticks, with unit `task_completions/tick`. At the same revision,
  `FactoryHandler.throughput(elapsedTicks)` already computed **order** completions per tick, with a
  zero window giving 0. Distinct step- and order-throughput formulas coexisted in different modules;
  the legacy KPI explicitly named itself `throughput_rate` with unit `task_completions/tick`.
  This is evidence against assuming equivalent meaning from similar terminology, not proof of
  ownerlessness or a conflicting shared contract.
- **Removal.** #388 removed the package together with `EventLog` and `SimRunner`.

`busyTicks` crediting landed on 2026-09-02 as headless-acceptance work (#241,
`e468e826087b699c5bdca835db362bac706f079e`), before the KPI removal; its comment records that
`busyTicks` had previously been a constant zero. The §10.2 accumulator register was added on
2026-09-26 with the provisional Engine-semantics reset (#405,
`0f89224b612ae564b411204957376303b16472f5`).

## Candidate classifications

Phase 1 does not compare the brief's Phase 2 models. It tests rival *readings of the current
boundary*:

| Reading | Statement | Result |
|---|---|---|
| **K1 Placement** | Engine owns what Engine code computes | **Fails.** `SimTime` in `types` implements Engine rules. A consumer-side test computes a normalized ratio while the contract's "utilization facts" wording does not define that ratio. The historical generic `simulation`-module package computed a *different* throughput from the Factory one. Placement alone supplies no ownership rule |
| **K2 Derivability** | A derived value is analytics, not Engine | **Fails.** `combinedQueueDepth` is derived and decision-affecting. The five performance fields are derived and Engine-owned by normative text |
| **K3 Supported-boundary** | A derived value's current owner follows from whether accepted contracts declare it a supported Engine result (§1.1 + §10.x + runtime contract), independent of decision participation | **Survives** as the description of the current boundary |
| **K4 Decision-participation** | Engine owns only what affects acceptance, assignment, ordering, or timing | **Fails as a description of today**, because §10.1–§10.2 explicitly own report-only arithmetic. It is not falsified as a *prospective* rule. That is Phase 2 territory, and Phase 1 takes no position on it |

## Proving cases

### PC1 — Multi-eligible waiting while every per-machine `queueDepth` is zero

Fixture: `StarterCorpus.multiEligibleWaiting()`. Five units; the ASSEMBLE step lasts 8 ticks on
two concurrency-1 assemblers (`StarterCorpus.java:45-46`, `:53`).

**Pinned facts at the mid-run boundary** (`WaitingWorkByStepOracleTest.java:43-76`):

- every resource's `queueDepth` is 0;
- three jobs (3, 4, 5) appear in `pendingWork`, each eligible for both assemblers;
- the same three jobs are `Queued` in the job projection;
- their `JOB_WAITING` events carry the shared eligible set and name no single machine.

**Derived by this report** (inference: §2 rule 3 applied to those pinned facts): each assembler's
`combinedQueueDepth` is 0 + 3 = **3**. Summed across both assemblers that is **6**, against **3**
physical waiting units, none of which is in either machine's own queue.

**Classification consequences:**

- `queueDepth` is an S-projection of the machine's own FIFO queue (§2 rules 7–8), not "all work
  waiting at this machine."
- `pendingWork` is a separately required S-projection (§1.2). Its existence is what keeps the
  observation truthful in this case.
- `combinedQueueDepth` is a D-quantity. It counts a shared entry against every compatible
  candidate, so it is exact as a ranking key. Presented as physical waiting it is wrong in both the
  per-machine sense and the summed sense.
- An observation-boundary recomputation gives the key's value only *at that boundary*. Ranking
  happens at intermediate cascade states (§4 rules 7–9), so a consumer cannot reproduce Engine
  choices this way. The brief's invariant forbids it from trying.
- Operation-step-first attribution over the same facts is derivable from the observation plus
  published model facts (`WaitingWorkByStepOracleTest.java:78-117`). Phase 1 records that it is
  possible. It does not assign an owner.

### PC2 — A long unfinished step while completion-credited `busyTicks` is zero

Fixture: `StarterCorpus.longStepAndParallelCapacity()`. A 12-tick ASSEMBLE step on one
concurrency-2 resource (`StarterCorpus.java:49-50`).

**Pinned facts at the mid-run boundary** (observed time 3; `ProcessingOccupancyOracleTest.java:52-67`,
`:107-119`):

- the assembler is `Busy` with 2 of 2 slots active;
- its `busyTicks` is **0**;
- the occupied job-ticks derived from dispatch and completion events are **3**.

**Classification consequence.** The Engine-owned accumulator is *cumulative processing job-ticks
credited at step completion*. It says nothing about current occupancy. The current-occupancy facts
are the S-projections `state` and `activeJobIds`. Because offline-with-active-work is rejected, the
credited total equals processing job-ticks once no step on that resource is unfinished **and the
accumulator has not saturated**. That bounded equality is pinned for every resource at this fixture's
closing boundary, and for its cutter mid-run (`ProcessingOccupancyOracleTest.java:87-105`).
`EngineDerivedResultConformanceTest.meanLeadTimeAndBusyTicksSaturateAtTheLongBoundary` supplies
the contrasting case: two completed `Long.MAX_VALUE`-duration steps credit only `Long.MAX_VALUE`,
not their mathematical sum. Neither case establishes a general utilization definition.

### PC3 — `concurrency > 1`, where cumulative processing ticks exceed elapsed time

Same fixture.

**Pinned facts at closing** (`ProcessingOccupancyOracleTest.java:69-85`):

- `busyTicks` is **36** and elapsed observed time is **26**, so `busyTicks / elapsed` ≈ 1.38;
- concurrency-weighted capacity is **52** job-ticks;
- derived occupancy is 36 / 52.

**Classification consequences:**

- What the Engine owns is the accumulation rule only: saturating, completion-credited, summed
  across concurrent slots.
- No Engine-produced normalized ratio or denominator is defined here. The cited research oracle
  uses concurrency times elapsed time for its continuously-online fixture; that research-local
  denominator is not adopted as a general utilization formula.
- The overview and handler wording can be misread as promising a normalized result. The concrete
  accumulator rule, rather than that shorthand, determines what the runtime actually supplies.
- The consumer ratio in `HeadlessClosureAcceptanceTest.java:467-469` fails as a bounded capacity
  fraction in this concurrent case. It does not falsify that test's bottleneck assertion, which
  compares resources ordinally in its particular fixture.
- The name `busyTicks` is also imprecise for `concurrency > 1`. It counts job-ticks, not ticks
  during which the resource was `Busy`.

### PC4 — `combinedQueueDepth`: exact as ranking, unsafe as a physical queue

**Pinned facts:**

- The exact key decides assignment: a unary candidate with two compatible shared entries beats a
  four-slot candidate carrying a residual local queue (`EngineDispatchConformanceTest.java:327-365`).
- The overlap fixtures pin completion vectors and mean lead time that depend on it, for example a
  mean of 72.33 (`EngineDispatchConformanceTest.java:203-213`). Prior research measured the same
  overlap against a 39.67 local-depth counterfactual (`engine-evolution.md:56`).

Changing the arithmetic changes assignment, so the quantity is D under every reading. PC1 shows it
is unsafe as a presentable queue. Its ownership is **clear and not contestable** under the current
contracts and under every candidate model in the brief (the brief's own worked example, and
`spatial-runtime-consequences.md:169-173`).

### PC5 (added) — Fresh-observation sufficiency separates R-obs from R-hist

This case is inference from record shapes and the pinned arithmetic. It was not exercised as a
dedicated experiment.

**Recomputable from one fresh observation:**

| Field | Recomputation |
|---|---|
| `backlog` | count of `!complete` orders |
| `completedOrders` | count of `complete` orders |
| `averageLeadTime` | Σ(`completedAt` − `createdAt`) ÷ count, with floor-at-zero subtraction and a saturating sum. The saturating sum of non-negative terms is order-independent |
| `throughputPerTick` | `completedOrders ÷ metadata.currentTime`, with a zero window giving 0 |

**Not generally recomputable from one fresh observation with the current record shapes:**

- `completedSalesValue`. `OrderObservation` carries no unit price. Floating-point accumulation also
  depends on completion order, which `completedAt` cannot break when two completions share a time.
  It is reconstructible from `ORDER_COMPLETED` events in sequence order, but only if every one of
  them was retained.
- `busyTicks`. A fresh job projection does not generally retain each completed step's machine
  assignment. Complete `JOB_STEP_COMPLETED` events plus published step durations can reconstruct
  the completion credits under the same saturating rule. The occupancy oracle's missing-history
  refusal (`ProcessingOccupancyOracleTest.java:129-159`) corroborates the separate history limitation,
  but is not a dedicated test of recomputing this accumulator.

**Consequence.** Removing an R-obs field loses no information a consumer could not recompute.
Removing an R-hist field while leaving the other projections unchanged would prevent general
reconstruction by a late-joining consumer from that observation alone. Continuing to supply the
result elsewhere would need additional evidence, such as retained supported events; its input and
retention contract belongs to the separate evidence/provenance question, not to Phase 1.

### PC6 (added) — The "Engine computes it" argument fails in both directions

This is the K1 evidence from the candidate table, stated as a case:

- An Engine rule implemented outside the Engine module: `SimTime.minus` and `SimTime.plus`.
- A consumer-computed ratio not defined by the contract's generic "utilization facts" wording.
- A generic-module "throughput" that measured something else: historical `ThroughputRate`.

None of these placements tells you the owner.

## Classification matrix

Status labels:

- **Clear** — the current owner is unambiguous and not under question.
- **Clear, held open** — the current owner is unambiguous, but the repository explicitly holds
  future ownership open.
- **Contested** — current support wording or scope admits materially different readings.

Change category means what reclassifying the item would require: moving its defining arithmetic out
of the Engine and/or removing it from the supported observation. Keeping formula, arithmetic,
accumulation, and conformance unchanged while moving code is always implementation-only.

| # | Item | Current owner | Class | Status | Change category if reclassified |
|---|---|---|---|---|---|
| 1 | Resource `machineId`, `state`, `activeJobIds`, `queueDepth` | Factory runtime | S | Clear | Not a plausible candidate. A change to `queueDepth` meaning would be **both** (§10 bullet, runtime-contract Resources) |
| 2 | Order and job projections | Factory runtime | S | Clear | Not a plausible candidate; runtime-contract |
| 3 | `pendingWork` | Factory runtime | S | Clear | Not a plausible candidate; **both** (§1.2 + runtime contract) |
| 4 | Metadata: `runId`, `modelFingerprint`, `currentTime`, `runState`, `latestEventSequence` | Runtime owns correlation, time/state/cursor and projection coherence; Factory retains model-fingerprint meaning | S / provenance | Clear | A projection/support-only change is runtime-contract; changing time, run-state or result semantics also requires Engine-semantics change |
| 5 | Resource `name`, `concurrency`, `capacityLiters`, `setupTime` | Factory model (echoed) | M | Clear | Removal would be a runtime-contract projection-shape change only. `setupTime` is not a runtime performance fact |
| 6 | `backlog` | Engine | R-obs | Clear, held open | **Both**: §10 bullet and §10.2 row; runtime-contract Performance minimum |
| 7 | `completedOrders` | Engine | R-obs | Clear, held open | **Both**: §10.2 rule 2; the implemented observation record and the overview description. It is not named in the contract's prose minimum |
| 8 | `completedSalesValue` | Engine (Factory operational measure) | R-hist | Clear, held open | **Both**, plus the overview ownership tables (`:290`, `:323-328`, `:446`, `:465`) |
| 9 | `averageLeadTime` | Engine | R-obs | Clear, held open | **Both**: §10.1 rules 2 and 4, §10.2 rule 1; runtime-contract lead-time |
| 10 | `throughputPerTick` | Engine arithmetic over the runtime contract's observation clock | R-obs | Clear, held open; window spans two authorities | **Both**: §10.1 rule 3, §10.2 rule 2; runtime-contract throughput and observation-boundary coherence |
| 11 | `busyTicks` | Engine | R-hist | Clear as an accumulator; **Contested** as described ("utilization") | **Both**: §10 bullet, §10.1 rule 1, §10.2 row; implemented resource projection and overview `:304`. A wording-only correction is a separate matter (Q1/Q2) |
| 12 | "Utilization facts" as named by the contracts | Engine supplies processing-time facts; no normalized ratio producer is defined | — | **Contested** interpretation of support wording | Phase 2 must distinguish clarification of existing facts from adding/removing a supported normalized result (Q1) |
| 13 | `FactoryHandler.backlog()`, `avgLeadTime()`, `throughput(long)`, `completedSalesValue()`, `completedSales()` | Implementation of Engine-owned R rules | — | Clear; placement matches | Implementation-only to factor while preserving ownership; reclassifying the supported results or their arithmetic follows rows 6–10 (**both**) |
| 14 | `FactoryRuntime` public pass-throughs of row 13 | Runtime operations over Engine-owned results | — | Method-level support not separately specified; underlying ownership clear | Code motion preserving behavior is implementation-only; removal/alteration needs API-support determination, and changed result meaning still requires semantic reconciliation (Q5) |
| 15 | `combinedQueueDepth` | Engine | D | Clear | **Engine-semantics definition change only.** It is in no supported observation. Not an analytics candidate |
| 16 | Former `com.arcogine.core.kpi.*` | None (removed) | Historical | n/a | Restoring it is a non-goal |

## Per-item detail

Each subsection gives: (1) owner; (2) reason; (3) textual authority; (4) implementation and whether
it matches; (5) executable evidence; (6) clear or contestable; (7) change category.

### `RuntimeObservation` state projections (rows 1–5)

1. **Owner.** Rows 1–3 project Factory runtime execution under the Engine interpretation, including
   references to Factory-owned product/resource identities. The runtime owns row 4's correlation,
   time/state/cursor and coherent projection; the copied fingerprint retains Factory provenance
   meaning. Row 5 is authored Factory-model content. A contract document defines these obligations;
   it is not itself a semantic owner.
2. **Reason.** Required current-state projection of authoritative runtime state (S).
   - `queueDepth` is the machine's own strict-FIFO queue only (§2 rules 7–8).
   - `pendingWork` exists because §1.2 forbids a view in which every queue is empty while work is
     waiting.
   - `state == Busy` means at least one active job, not saturation.
   - `currentTime` is the observation clock: the time of the latest supported event. It is not the
     scheduler cursor and not the time a consumer advanced to (`FactoryRuntime.java:79-92`;
     `ProcessingOccupancyOracleTest.java:107-119`).
   - `runId` is correlation metadata and never result-affecting (`runtime-contract.md:95`).
   - `setupTime` is echoed authored content that the runtime does not consume. The brief already
     states it is not a runtime performance fact.
3. **Authority.** `runtime-contract.md:61-63`, `:131-178`; `engine-semantics.md:146-148`, `:187-208`,
   `:474-477`; `overview.md:300`, `:308-310`.
4. **Implementation.** `FactoryRuntime.observe()` plus the read-only views. Matches.
5. **Evidence.** `RuntimeObservationAcceptanceTest`, `HeadlessClosureAcceptanceTest`,
   `WaitingWorkByStepOracleTest` (PC1).
6. **Status.** Clear.
7. **Change category.** For these state projections, removing or altering the supported projection
   requires a runtime-contract change; changing authoritative transition, dispatch, time, or
   aggregate meaning also changes Engine semantics. Factoring projection code while preserving
   those responsibilities is implementation-only. Echoed authored facts do not transfer Factory
   authority. These are not prospective analytics candidates in this phase.

### `backlog` (row 6)

1. **Owner.** Engine.
2. **Reason.** Report-only supported derived result (R-obs). It is a current-state count, "not a
   running accumulator" (§10.2), and no decision reads it.
3. **Authority.** `engine-semantics.md:478`, `:537`; `runtime-contract.md:173`; `overview.md:271`,
   `:446`.
4. **Implementation.** `FactoryHandler.java:138-140`, computed on demand from the order aggregates.
   Matches.
5. **Evidence.** `RuntimeObservationAcceptanceTest:96`, `:105`; `FactoryHandlerTest`;
   `FactoryRuntimeBoundaryAcceptanceTest`.
6. **Status.** Clear, held open. It is currently assigned without ambiguity, but its only claim to
   Engine ownership is its support status.
7. **Change category.** Both.

### `completedOrders` (row 7)

1. **Owner.** Engine.
2. **Reason.** R-obs: an exact counting accumulator. Naming differs across surfaces:
   `completedSales` in the handler and runtime accessor, `completedOrders` in the observation, and
   "completed sales" in §3 rule 4. All three mean the count of completed orders, not sales units.
3. **Authority.** `engine-semantics.md:545-550`, `:246-247`; `overview.md:300`.
4. **Implementation.** `FactoryHandler.java:43`, `:89-91`, `:373`. Matches.
5. **Evidence.** `EngineDerivedResultConformanceTest:42`, `:80`; `ProportionalQuantityWorkTest`;
   `RuntimeObservationAcceptanceTest:106`.
6. **Status.** Clear, held open.
7. **Change category.** Both.

### `completedSalesValue` (row 8)

1. **Owner.** Engine, as Factory's operational measure. Finance separately owns the *financial*
   interpretation through `OrderCompleted`.
2. **Reason.** R-hist: a report-only accumulator, deterministic by completion order, saturating
   toward infinity. The overview lists it as owned mutable state. Nothing in Engine decisions or in
   Finance reads it.
3. **Authority.** `engine-semantics.md:538`, `:551-559`; `overview.md:290`, `:321-328`, `:446`,
   `:465`.
4. **Implementation.** `FactoryHandler.java:42`, `:85-87`, `:372`. Matches.
5. **Evidence.** `EngineDerivedResultConformanceTest:43`; `FactoryHandlerTest:322-392`;
   `OrderIntentSeparationTest:80`; `ProportionalQuantityWorkTest:200-252`.
6. **Status.** Clear, held open.
7. **Change category.** Both, plus the overview's commercial/operational/financial ownership
   tables. Because it is R-hist, reclassification also bears on event retention (PC5).

### `averageLeadTime` (row 9)

1. **Owner.** Engine.
2. **Reason.** R-obs. A saturating sum of floor-at-zero order lead times divided by the completed
   count, with an empty set giving 0.
3. **Authority.** `engine-semantics.md:507-513`, `:535`, `:540-544`; `runtime-contract.md:173`.
4. **Implementation.** `FactoryHandler.java:142-147`, `:374-379`; `SimTime.java:19-21`. Matches.
   The floor-at-zero part sits in `types`, which is still Engine semantics (PC6).
5. **Evidence.** `EngineDerivedResultConformanceTest:31-70`; `EngineDispatchConformanceTest`
   mean-lead-time fixtures.
6. **Status.** Clear, held open.
7. **Change category.** Both.

### `throughputPerTick` (row 10)

1. **Owner.** Engine arithmetic over the runtime contract's observation clock.
2. **Reason.** R-obs. The window is `[0, observedTime]`, where `observedTime` advances only with
   supported events. That rule exists to keep every observation fact coherent with one
   `latestEventSequence` (`overview.md:302-304`). A consumer that advances to tick 10 with no
   supported event after tick 3 sees throughput over 3 ticks.
3. **Authority.** `engine-semantics.md:480`, `:510-511`, `:536`; `runtime-contract.md:173-176`;
   `overview.md:302-304`.
4. **Implementation.** `FactoryRuntime.java:688`, `:702-703`; `FactoryHandler.java:149-154`.
   Matches.
5. **Evidence.** `FactoryHandlerTest:146-153`; `EngineDerivedResultConformanceTest:49`;
   `HeadlessClosureAcceptanceTest` marker-no-op invariance (`:422-448`).
6. **Status.** Clear, held open. The definition spans two authorities.
7. **Change category.** Both.

### `busyTicks` (rows 11–12)

1. **Owner.** Engine.
2. **Reason.** R-hist. It is a saturating, cumulative sum of the authored durations of steps
   *completed* on the resource. It is summed across concurrent slots, so it is job-ticks. It is held
   as `Machine` runtime state because no single observation can recompute it, and no decision reads
   it.
3. **Authority.** `engine-semantics.md:474-475`, `:502-506`, `:534`, `:561-564`;
   `overview.md:304`. The runtime contract does not name `busyTicks`. It names only "utilization
   facts."
4. **Implementation.** `FactoryHandler.java:353-365`; `Machine.java:144-149`; `ResourceObservation`.
   The accumulator matches the spec. The descriptions "real utilization" in the handler comment
   and "reports cumulative utilization" in `overview.md:304` need qualification: PC2 and PC3
   disprove interpreting this counter as instantaneous occupancy or a normalized capacity fraction.
5. **Evidence.** `EngineDerivedResultConformanceTest:31-50`, `:72-82`;
   `ProcessingOccupancyOracleTest` (PC2, PC3).
6. **Status.** Clear as an accumulator and processing-time fact. **Contested** only where the
   generic "utilization facts" language is read to require a normalized result; no such producer
   is defined, and the headless test's ratio is not general for concurrent resources.
7. **Change category.** Removing `busyTicks` or moving its accumulation out of the Engine is both.
   Correcting *descriptions* without touching the value is a text correction (§1.1 consequence 3),
   if it clarifies the existing processing-time facts. Removing an actual supported obligation
   would instead narrow the runtime contract even without an implemented value change. Phase 2
   must distinguish those actions explicitly rather than assume either reading (Q1).

### `FactoryHandler` aggregates and `FactoryRuntime` accessors (rows 13–14)

1. **Owner.** The handler methods implement Engine-owned R rules. The `FactoryRuntime` public
   pass-throughs (`backlog()`, `avgLeadTime()`, `throughput(long)`, `completedSalesValue()`,
   `completedSales()`; `FactoryRuntime.java:606-624`) are public Java API outside `observe()`.
2. **Reason.** Implementation of supported derived results. One accessor differs:
   `throughput(long)` takes a **caller-chosen** window, so it is a formula exposed as an operation
   rather than a projection.
3. **Authority.** The runtime contract names `observe()` as the current-state boundary
   (`runtime-contract.md:20-22`). The overview (`:296`, `:308`) and `FactoryRuntime` class Javadoc
   broadly describe supported runtime access and read-only projections without separately naming
   these aggregate accessors. That omission does not establish they are unsupported.
4. **Implementation.** Matches the Engine-owned arithmetic. Individual method-level support is not
   separately specified, including the caller-selected denominator's relationship to observation time.
5. **Evidence.** Conformance tests pin the arithmetic *through these accessors*, not through
   `observe()` (`EngineDerivedResultConformanceTest`, `FactoryHandlerTest`,
   `SessionControlAcceptanceTest:391-394`).
6. **Status.** Underlying ownership is clear for both. Method-level support needs clarification only
   if a prospective change would remove or alter the public accessors.
7. **Change category.** Code motion preserving ownership and behavior is implementation-only.
   Transferring ownership of the supported aggregate semantics follows rows 6–10. Removing/altering
   a public accessor requires a separate API-support determination; documentation silence alone
   is not permission to treat it as a private implementation detail.

### `combinedQueueDepth` (row 15)

1. **Owner.** Engine.
2. **Reason.** Decision-affecting interpretation (D). Its exact arithmetic decides assignment.
3. **Authority.** `engine-semantics.md:163-177`, `:664-666`, `:710-713`;
   `spatial-runtime-consequences.md:169-173`; `engine-evolution.md:133`.
4. **Implementation.** `FactoryHandler.java:171`, `:181-186`. Matches. It is not exposed in any
   observation.
5. **Evidence.** `EngineDispatchConformanceTest` (PC4). Gap: no fixture for §14 item 17 (a sum above
   the 32-bit range) was found. See Confidence and limitations.
6. **Status.** Clear.
7. **Change category.** Engine-semantics definition change only. Exposing it as a presentable value
   would be a runtime-contract *addition*, and PC1 shows it would have to be named as a ranking
   quantity.

### Former `com.arcogine.core.kpi.*` (row 16)

Historical evidence only (see Repository evidence). It shows three things:

1. a consumer-neutral-shaped derivation was attempted;
2. it was derived from internal scheduler events, which the current runtime contract excludes as a
   supported source;
3. related throughput terminology denoted different declared units and formulas. That does not
   establish a single ambiguous contract or missing ownership.

It establishes nothing about the current boundary and is not a component to restore.

## Adversarial analysis

**Self-administered only.** Author-side checks, including evidence verification by assisting agents,
challenged these classifications. This is not an independent adversarial review.

| Challenge | Result |
|---|---|
| R-obs/R-hist ownership is circular: Engine-owned because §10 says so | Partly upheld as a *qualification*, not a falsification. Phase 1 asks for the current owner under accepted contracts, and normative text is the authority. The ownership is real but conditional on support status. That is now stated explicitly, and it is the main Phase 2 input |
| Some performance value secretly feeds a decision | Not found. Repository search shows `busyTicks` and the aggregates used only at crediting, the accessors, `observe()`, and tests. `selectMachine` reads none of them. Finance consumes `OrderCompleted`, not `completedSalesValue` |
| The PC5 recomputability claims are wrong | Checked against record shapes and the pinned arithmetic. The `averageLeadTime` and `completedSalesValue` claims rest on field presence (`OrderObservation` has no price) and IEEE non-associativity. They are inference, not executed experiments; confidence is lowered accordingly |
| "Contested" overstates a wording issue | Qualified. The minimum names utilization **facts**, which can mean processing-time inputs rather than a ratio. The accumulator is clear; the normalized-result interpretation remains unresolved. Absence of a ratio producer does not withdraw declared support |
| A supported derived result was missed | Re-swept the observation records. `completedQuantity` and `complete` are aggregate state (S). `runState` is a run-state projection (S). `observedTime` is covered under row 4 and row 10. Nothing else |
| Baseline staleness | The baseline `0b82db0` differs from initial `e4f98a6` only in the landed brief/register. Continuation rechecked `8c2ed14`: relevant implementation, contracts and proving cases are unchanged; the new job-lead-time test corroborates existing behavior |
| Completed `busyTicks` always equals processing job-ticks | Qualified. Equality in the ordinary occupancy fixture requires no unfinished step and no saturation; the long-boundary conformance fixture disproves the unconditional claim |
| Phase 1 smuggles a Phase 2 answer through K4 or the R-obs/R-hist split | K4 is rejected only *descriptively*. The split is offered as a change-consequence axis, and no item's future owner is chosen |

## Surviving invariants

1. In-scope execution facts and supported derived performance results are currently Engine /
   Factory-runtime owned. Authored facts and fingerprint meaning remain Factory-owned when copied
   into observations. No in-scope supported analytics-owned result exists today.
2. Current contracts use "result-affecting" in two senses. In scope, only `combinedQueueDepth` is
   decision-affecting. The performance arithmetic is report-only.
3. Report-only supported results are Engine-owned *because they are declared supported*. Their
   ownership is conditional on that support status.
4. Implementation placement does not establish semantic ownership, in either direction.
5. `busyTicks` is a saturating, completion-credited cumulative of authored step durations across
   concurrent slots (job-ticks). Exact equality to processing job-ticks also requires no unfinished
   step and no saturation. It is neither instantaneous occupancy nor a normalized utilization ratio,
   and it can exceed elapsed time.
6. `queueDepth` covers a resource's own queue only. Multi-eligible waiting is projected separately
   through `pendingWork`, with job status and events providing corroborating facts.
   `combinedQueueDepth` counts each shared entry against every compatible candidate
   and is not a physical-queue measure.

## What did not survive

- **K1 "Engine owns what Engine computes"** and **K2 "derived means analytics"** (PC4, PC6).
- **K4 as a description of the current boundary.** It remains open as a prospective rule for
  Phase 2.
- **"`busyTicks` itself is instantaneous occupancy or a normalized utilization ratio."** PC2 and
  PC3 falsify those readings of the overview and handler shorthand.
- **"A normalized utilization ratio is already defined and produced by Engine."** No such producer
  was found in the runtime surface; the headless test ratio is not general for `concurrency > 1`.
  This does not falsify the contract's declared support for utilization facts.
- **"The removed generic KPI package was the same measurement in another place."** Its throughput
  counted step completions with a `max(1, t)` floor.

Reusable negative knowledge worth preserving: *a cumulative, completion-credited, concurrency-summed
counter divided by elapsed time is not a utilization.* It is already durable as
`ProcessingOccupancyOracleTest`. The reconciliation should keep that test rather than this report.

## Confidence and limitations

- **High:** current owner for every row; `busyTicks`, `combinedQueueDepth`, and `queueDepth`
  semantics; the absence of a normalized runtime ratio. These rest on normative text, code, and the
  113 passing results above, including the explicitly identified 17 reused results.
- **Medium-high:** the R-obs/R-hist split and the change categories. PC5 is inference from record
  shapes and arithmetic, partly corroborated by the research oracles. No dedicated experiment
  recomputed each performance field from one observation.
- **Required-surface coverage:** all required current in-scope repository surfaces were inspected;
  no missing access blocked Phase 1. The following limits concern absent capability or bounded
  executable evidence, not silently omitted required files:
  - No outward adapter exists, so there is no outward DTO to classify.
  - Spatial execution is not implemented, so the transfer-dependent clauses of §10 (admission load,
    in-flight jobs) are specified but unexecuted. They are a recheck trigger.
  - The §14 item 17 fixture (`combinedQueueDepth` above 32 bits) was not found by bounded search
    over `product/` for `combinedQueueDepth`, `Integer.MAX_VALUE`, "32-bit", and the dispatch
    conformance test. The spec requires the fixture. This bounded search establishes an evidence gap,
    not that the fixture is infeasible or necessarily requires direct materialization of billions
    of entries. It does not change any ownership classification.
  - No external evidence was used. No external source can change which Arcogine contract currently
    owns a value (stopping rule, `researching.md` §8). External bottleneck and utilization
    literature belongs to Phase 2.

## Unresolved unknowns — the bounded Phase 2 set

Phase 2 must decide at least the following. Phase 1 does not resolve any of them.

- **Q1 — Utilization wording and responsibility.** Does the current phrase mean the supplied
  processing-time facts, or is a separately normalized result intended? Clarification that preserves
  existing facts differs from adding or removing a supported result. Phase 2 must classify that
  consequence and any future owner; Phase 1 selects no formula or normalization.
- **Q2 — R-obs fields** (`backlog`, `completedOrders`, `averageLeadTime`, `throughputPerTick`).
  Do they remain Engine-owned supported results? Reclassification is both kinds of change, with no
  information loss to consumers.
- **Q3 — R-hist accumulators** (`busyTicks`, `completedSalesValue`). Do they remain Engine-owned
  supported results?
  - Reclassification is both kinds of change. Removing the fields without replacement evidence
    loses their current fresh-observation sufficiency.
  - `completedSalesValue` also touches the overview's ownership tables.
  - If either moved, the evidence/provenance question's trigger would fire.
- **Q4 — The `throughputPerTick` window.** How would any future ownership change preserve or
  deliberately change its connection to the supported observation clock? Its current window and
  owner are clear; formula and boundary-coherence responsibilities must not be split accidentally.
- **Q5 — `FactoryRuntime` KPI accessors.** If a prospective reclassification would alter public
  accessors outside `observe()`, what method-level support applies, including to caller-windowed
  `throughput(long)`? Their underlying Engine-owned arithmetic is already established.
- **Q6 — Ownership rule test.** Whatever ownership rule Phase 2 adopts must classify
  `combinedQueueDepth` as Engine-owned (D), and must classify PC1's step-first waiting attribution
  and PC3's occupancy derivation without requiring reconstruction of Engine choices. These are
  fixed tests for the rule, not open questions about those items.

## Durable consequences

No reconciliation follows from Phase 1 alone. The following are candidates. They are subject to
Phase 2 and independent adversarial review, and are not performed here:

- correct the "utilization" descriptions attached to `busyTicks` (`overview.md:304`, the
  `FactoryHandler` comment, `engine-semantics.md` §10, §10.1 rule 1, §10.2 row) and the runtime
  contract's Performance minimum, consistent with the Q1 outcome;
- state the support status of the `FactoryRuntime` KPI accessors (Q5);
- resolve whether `engine-semantics.md` §14 item 17 is executable as worded, or restate the
  obligation;
- harmonize the completed-order count naming (`completedSales` / `completedOrders` /
  "completed sales") if the field survives.

Reusable assets are already durable as tests: `WaitingWorkByStepOracleTest` (PC1) and
`ProcessingOccupancyOracleTest` (PC2, PC3). This report does not itself need promotion.

## Implementation implication

No implementation.

## Follow-up triggers

- Phase 2 of this coupled packet (next).
- Spatial execution landing. Recheck the `busyTicks`, `queueDepth`, and admission-load
  classifications against §9–§10.
- A concrete consumer that requires utilization (feeds Q1).
- Introduction of an outward adapter. The planning constraint against hardening these values into
  new DTOs while research is open applies.

Track all of these through the existing register entry. No new register question is proposed.

## Sources

Repository only, at the baseline unless stated:

- `docs/architecture/engine-semantics.md`, `runtime-contract.md`, `overview.md`,
  `factory-design.md`, `governance-evidence.md`, `isa-95-semantic-mapping.md`
- `docs/planning/spatial-runtime-consequences.md`, `factory-simulation-engine-readiness.md`,
  `factory-design-game-consumer.md`
- `docs/research/investigations/simulation-analytics-consumer-boundary.md`,
  `engine-evolution.md`; `docs/research/research-register.md`
- `product/domains/factory/src/main/java/com/arcogine/factory/process/{FactoryRuntime,FactoryHandler,RuntimeObservation,RuntimePerformanceObservation,ResourceObservation,OrderObservation,RuntimeEventPayload}.java`;
  `.../factory/machines/{Machine,MachineView}.java`; `product/types/.../SimTime.java`
- The tests listed under Executable evidence, plus `StarterCorpus.java` and
  `ThreeStepRoutingFamily.java`
- Historical: `product/simulation/src/main/java/com/arcogine/core/kpi/*.java` at
  `2e13eb5fcf71823740453b1c80d0e792fa38a746`, before removal in
  `cf67df8f28c5bf466960215e3f4552d509db58a4` (#388); `busyTicks` crediting in
  `e468e826087b699c5bdca835db362bac706f079e` (#241); accumulator register in
  `0f89224b612ae564b411204957376303b16472f5` (#405)

No external sources.
