# Player-Facing Diagnostic Evidence for the First Factory-Design Game Slice

> **Research status:** ACTIVE (evidence gathering and synthesis performed; not CONCLUDED — no durable consequence has been reconciled)
> **Research baseline:** live `main` at `56a876208a98dd570d384ed9d8e3535dd86f1335` (original investigation)
> **Later reconciliation baseline:** `24b29418672eaeb74658e04994684bb9fe2e5db4` — re-grounded twice after the original baseline (see § Regrounding log immediately below); the conclusion holds against this later head, with corrections folded into the sections they touch rather than left as an addendum
> **Evidence workspace:** `claude/arcogine-factory-diagnostics-px2bvb` — this question's dedicated temporary research-evidence workspace branch (`docs/development/researching.md` §10); this file's handoff commit SHA and path are the artifact identity, not the branch tip
> **Authority:** Research evidence only. This document is not accepted architecture, product direction, or implementation commitment. Nothing here becomes durable until a separate, independently reviewed reconciliation change promotes it.
> **Adversarial-review status:** **self-administered only.** No independent adversarial pass has been performed. See § Risk classification for why this is sufficient for the surviving conclusion and what would change that.

### Regrounding log

`main` moved twice after the original investigation baseline, and this report was revised in place both times rather than left to go stale silently.

| From → to | Commits | Relevant to this report? |
|---|---|---|
| `56a8762` → `975c163` | `#296` fix, `#297` docs, `#298` docs | Yes — see inline corrections below |
| `975c163` → `24b2941` | `#299` feat, `#300` docs, `#301` docs | Yes — see inline corrections below |

Net effect on the conclusion: **none of the load-bearing findings changed.** Two corrections strengthen the existing recommendation with better evidence than the original investigation had (`#296`, `#300`); one lands a small piece of the deferred spatial model in an inert form that does not yet reach the runtime (`#299`); one sharpens a precondition on required evidence #6 that the original investigation should have caught and did not (the `EngineSemanticsVersion`/`AttemptCompatibility` gap, found only on the second pass). `#297`/`#301` are the research-method changes this persistence itself now follows.

---

## Question

What player-facing evidence best exposes bottlenecks and causal performance differences in the first factory-design game slice, such that a player can correctly explain *why* one design performs better or worse without reading Engine internals or inventing competing production semantics?

Sharpened from the brief in two ways, both forced by repository evidence:

1. The brief lists "how much time is attributable to processing versus transfer" as a required player question. **Transfer does not exist in landed Engine behavior.** The question is retained but split into a landed part (processing versus *waiting*) and a conditional part (processing versus *transfer*) that cannot be asked of the current runtime at all.
2. The brief's candidate set implicitly treats current-state presentation and event-history explanation as alternatives. The evidence shows they answer disjoint question classes, so the real choice is *whether the game retains supported event history*, not which of the two to present.

## Decision at stake

The smallest player-facing diagnostic evidence set that should be required for the first playable slice, and — conditionally — whether any required player inference is impossible to produce truthfully from Arcogine's supported consumer contract without a new supported Engine observation.

## Scope and non-goals

In scope: what evidence exists, who owns each derivation, which indicators mislead, what causal claims are licensed, and what empirical validation remains.

Explicitly out of scope: implementing anything; rendering technology; HUD layout; scoring/progression; Factory ontology; dispatch policy; resource pools; transport networks; exposing internal scheduler `Event`/`EventPayload`/`EventLog`; making Arcogine event sourced; a canonical model-diff abstraction.

---

## Executive conclusion

**Recommended model: Candidate C — supported current-state observation *plus* game-retained supported runtime event history, with a small set of game-owned derivations computed over both.** Candidate B's derivations are not a rival to C; they are the derivations C makes possible, and most of the useful ones are impossible without C's retained history.

**Candidate A (direct supported-state presentation) fails**, and it fails on repository facts rather than on aesthetics. Three landed semantics break it:

- work waiting for a step with more than one eligible resource sits in the cross-machine multi-eligible backlog and is counted in **no machine's** `queueDepth`, so a per-machine queue visualisation shows an empty queue in front of the actual constraint precisely in the parallel-capacity designs the game is built to teach;
- `busyTicks` is credited only **at step completion**, so utilisation derived from a snapshot systematically under-reports, and a machine occupied for the whole run by one long unfinished step reads zero;
- for a single-order challenge the entire `RuntimePerformanceObservation` is degenerate — backlog goes 1→0, completed orders 0→1, mean lead time 0→makespan, throughput 0→1/T — so all four supported "performance facts" carry no within-run diagnostic signal.

**No Engine gap is proven.** Every player inference the validated loop requires for the *landed* runtime is derivable, truthfully and deterministically, from the already-landed supported observation plus the already-landed supported event stream plus the published model the game itself authored. The strongest bottleneck-detection method in the manufacturing literature — Roser, Nakano and Tanaka's active period method — is directly computable from `JOB_DISPATCHED` / `JOB_STEP_COMPLETED` / `JOB_WAITING` without duplicating a single scheduling or dispatch decision.

**Confidence is asymmetric and should be reported that way:**

- *High* that the contract half is right: the evidence set is sufficient, correctly owned, and requires no Engine change. This rests on directly inspected source, tests and Accepted ADRs.
- *Low-to-moderate* that the product half is right: that a player shown this evidence actually forms the correct causal model. No participant testing was performed, and the literature specifically warns that outcome feedback in dynamic systems does not produce understanding. § Prototype/playtest protocol defines the smallest study that would settle it. **The product hypothesis must not be treated as validated by this report.**

Two hard sequencing constraints, neither of which is an Engine gap:

- **Spatial arrangement is not consequential in landed behavior.** There is no transfer time, no `TRANSFERRING` state, no transfer event. The game's second product hypothesis is unfalsifiable until the admitted spatial-transfer implementation work lands. The first slice must either exclude layout as a performance lever or wait.
- **The current HTTP/SSE surface is not a supported consumer contract.** `SseController` still emits internal `Event` objects named by internal `EventType`. A web client consuming it today would be consuming exactly what ADR-0011 forbids. The first slice must consume `FactoryRuntime` directly, or wait for the outward-consumer convergence work.

---

## Repository evidence

All statements in this section are **Repository fact**, verified against the baseline SHA by direct source inspection.

### Landed supported observation

`RuntimeObservation` (`product/domains/factory/src/main/java/com/arcogine/factory/process/`) carries:

| Component | Fields |
|---|---|
| `RuntimeObservationMetadata` | `runId`, `modelFingerprint`, `currentTime`, `runState` ∈ {`ACTIVE`,`QUIESCENT`}, `latestEventSequence` |
| `ResourceObservation` | `machineId`, `name`, `state` ∈ {`Idle`,`Busy`,`Offline`}, `concurrency`, `activeJobIds`, `queueDepth`, `capacityLiters`, `setupTime`, `busyTicks` |
| `OrderObservation` | `orderId`, `productId`, `requestedQuantity`, `releasedQuantity`, `completedQuantity`, `createdAt`, `completedAt`, `complete` |
| `JobObservation` | `jobId`, `orderId`, `ordinalWithinOrder`, `productId`, `status` ∈ {`Queued`,`InProgress`,`Completed`,`Cancelled`}, `currentStep`, `totalSteps`, `currentMachineId`, `createdAt`, `completedAt` |
| `PendingWorkObservation` | `jobId`, `eligibleMachineIds` |
| `RuntimePerformanceObservation` | `backlog`, `completedOrders`, `completedSalesValue`, `averageLeadTime`, `throughputPerTick` |

### Landed supported event contract

`RuntimeEventType` = `ORDER_ACCEPTED`, `JOB_DISPATCHED`, `JOB_WAITING`, `JOB_STEP_COMPLETED`, `ORDER_COMPLETED`, `MACHINE_AVAILABILITY_CHANGED`. `RuntimeEventEnvelope` carries `runId`, monotonic `sequence`, `simulationTime`, `eventType`, `modelFingerprint`, optional `controlledRevisionId`, `affectedEntityRefs`, typed `payload`.

Payloads relevant to diagnosis:

- `JobDispatched(jobId, orderId, machineId, stepIndex)`
- `JobWaiting(jobId, orderId, eligibleMachines)` — the set is the multi-eligible backlog's captured eligible set, or the singleton machine whose own queue holds the job
- `JobStepCompleted(jobId, orderId, machineId, stepIndex, jobComplete)`

`FactoryRuntime.drainSupportedEvents()` is **draining and non-retained** by deliberate design (ADR-0011 §8). The consumer must retain what it wants to keep; nothing can recover a run's history afterwards.

### Landed semantics with direct diagnostic consequences

1. **`busyTicks` accrues at step completion only.** `FactoryHandler.handleTaskEnd` credits the finished step's authored duration after the step ends. In-progress occupancy is therefore invisible in `busyTicks`, which lags true occupancy by up to one full step duration — and reads exactly zero for a machine that has been continuously occupied by a single not-yet-finished step.
2. **`busyTicks` sums per-job durations regardless of concurrency**, so `busyTicks / elapsed` can legitimately exceed 1.0 on a resource with `concurrency > 1`.
3. **A waiting job has no machine.** `Job.completeStep` sets `currentMachine = null`, so every `Queued` `JobObservation` reports `currentMachineId == null`. Waiting work cannot be attributed to a resource by reading the job projection alone.
4. **`queueDepth` covers only the per-machine single-eligible queue.** Work whose current step has more than one eligible resource goes to `FactoryHandler.pendingMultiEligible` and appears solely in `PendingWorkObservation`. It is in no machine's `queueDepth`.
5. **`combinedQueueDepth` is a ranking key, not a presentable count.** It adds a machine's own queue depth to the count of *every* compatible multi-eligible entry, so one waiting unit contributes to several machines' values. `engine-semantics-v1.md` §2 rule 3 fixes it as an exact ranking key precisely because a wrong value would change assignment. Rendering it as "units waiting here" would multiply the visible backlog.
6. **`setupTime` is authored but never used in runtime timing.** It participates in the model fingerprint and semantic comparison; it affects no duration, no dispatch, no result.
7. **`releasedQuantity == requestedQuantity` always.** `OrderExecution` returns `requested` for both; all children materialise at submission. The field carries no signal.
8. **`RuntimePerformanceObservation` is order-scoped.** `backlog` counts incomplete accepted *orders*; `completedOrders` counts completed orders; `avgLeadTime` averages order-level created→completed; `throughput = completedOrders / elapsedTicks`. For the brief's one-order, 20-unit reference challenge these are 1→0, 0→1, 0→makespan, 0→1/T respectively.
9. **Blocking is unreachable.** Machine queues are unbounded `ArrayDeque`s; there is no WIP cap, no finite buffer, no downstream admission limit in landed behavior. `MachineState` has no `Blocked`. The classic starve/block/bottleneck triad reduces to starve/occupied.
10. **`Offline` is unreachable in the game loop.** Availability changes only via `setMachineAvailability`, which the described player loop never issues.
11. **The legacy transport is not a supported contract.** `SseController` sends internal `Event` instances with `event.eventType().name()` as the SSE event name; `docs/reference/api.md` documents `OrderCreation`/`TaskStart`/`TaskEnd` as wire names.
12. **`product/simulation/src/main/java/com/arcogine/core/kpi/` computes from internal `EventLog`.** ADR-0011 §8 excludes `EventLog` from the supported contract; these KPIs are not consumable by the game.
13. **Every authoritative placement change is now provably reported as a supported event, including a path the original investigation missed.** PR #296 (landed after the original baseline) fixed a redundant `setMachineAvailability(machine, true)` against an already-online machine with free capacity and genuinely queued work: the lower-level handler always retried queue/backlog dispatch whenever `online=true` regardless of whether the machine actually transitioned, so a no-op command could silently dispatch queued work while the `if (transitioned)` guard suppressed the corresponding `MACHINE_AVAILABILITY_CHANGED`/`JOB_DISPATCHED` events and the sequence never advanced — two observations at the same `latestEventSequence` could then disagree on authoritative state. This directly strengthens Candidate C's load-bearing premise (every placement change is a supported event), and it is now closed by a regression test. Disclosed as a limitation, not hidden as a strength: the original investigation traced order acceptance, the `TaskEnd` cascade, and genuine availability transitions, and did not find this redundant-command path itself. It never affected this report's conclusion — `setMachineAvailability` is unreachable in the game loop described by the brief (fact 10 above) — but the fix is better evidence for invariant 1 (§ Surviving invariants) than anything this investigation produced on its own.

### Landed acceptance evidence, and its limits

`HeadlessClosureAcceptanceTest.supportedObservationIdentifiesTheActiveBottleneckWithoutInternalAccess` proves that the active bottleneck is identifiable from `ResourceObservation` facts alone — independently by carried load (`activeJobIds.size() + queueDepth`) and by `busyTicks` utilisation — deterministically across a reset.

That is real evidence, and it is narrower than it looks. The fixture is a two-stage routing with **exactly one eligible machine per step** and a 1-tick versus 10-tick imbalance, sampled after the fast stage has run dry. Facts 1 and 4 above are both dormant in that fixture: there is no multi-eligible work to be invisible, and enough steps have completed for `busyTicks` to be informative. The test establishes that Candidate A works in the single-eligible unbalanced case. It does not establish that Candidate A works in the parallel-capacity case the game exists to teach.

### Already-admitted but not landed

`engine-semantics-v1.md` is a normative design contract explicitly marked *implementation pending*; ADR-0014 and ADR-0015 are **Accepted**; the spatial-runtime-consequences delivery plan is Proposed, with only its model-validation slice implemented so far (below). The following remain specified but absent from runtime behavior: job status `TRANSFERRING`; `TRANSFER_STARTED`/`TRANSFER_COMPLETED`; `transferStartedAt`/`transferCompletesAt`; source and bound destination identity for in-flight work; destination admission load; mandatory `EngineSemanticsVersion` on observations and events; optional `ControlledRevisionId` on observation metadata.

`engine-semantics-v1.md` §10 already anticipates the consumer consequence: "a destination can have constrained admission capacity because of transfer-bound work while not yet accruing processing busy ticks. Consumers must not misdiagnose that state as ordinary processing utilization."

**Regrounding update — the V2 spatial model and its validation have landed, but remain structurally inert for this question.** PR #299 (after the original baseline) added `FactoryModelV2`, `FactoryModelV2Validator`, and the five ADR-0014 spatial/handling fields (floor extent, per-resource reference-cell position, footprint, `ticksPerCell`, `handlingTicks`) under `com.arcogine.factory.model.v2`, plus floor/overflow/overlap/handling validation. This is real landed code, not merely admitted architecture — but `FactoryModelV2` deliberately shares no supertype with `FactoryModel`, so it cannot be passed to `FactoryModelPublisher.publish(FactoryModel)` and cannot produce a `FactoryModelVersion`, a runtime, an observation, or an event. The "spatial arrangement is not consequential in landed behavior" conclusion above is therefore reinforced by a concrete structural reason, not weakened: a V2 model cannot yet reach any surface this report evaluates. The canonical byte encoding, fingerprint derivation, and publication path for the V2 model policy — and everything downstream of it — remain not started.

**Regrounding update — the two Critical-path v1 dispatch questions this report depended on are now `CONCLUDED`, not merely `READY`.** PR #300 (after the original baseline) concluded both "should the first supported release retain `engine-semantics:v1`'s at-most-one-local-job-per-cascade-trigger rule" and "…retain v1's exact `combinedQueueDepth` ranking," each with **Retain v1 unchanged** and each independently adversarially reviewed to **ACCEPT WITH QUALIFICATIONS** (`docs/research/engine-evolution.md`). This does not change this report's recommendation — it was never contingent on the outcome, since the recommendation already treats `combinedQueueDepth` as a ranking key never to be shown to a player regardless of its formula — but it upgrades the evidentiary weight behind that rejection. The concluded research characterizes `combinedQueueDepth`'s shared-work contribution as "best understood conditionally... a handover signal," explicitly **not** "a principled physical-load estimate or an optimal dispatch rule" — durable, adversarially reviewed language that corroborates this report's own "Rejected or misleading indicators" entry for the same field. See the added note under § Failure and misdiagnosis analysis.

### Landed Challenge substrate relevant to attempt comparison

`ChallengeAttempt` retains `CandidateDraftSnapshot` (the exact placed equipment), `DraftEconomics`, and `ChallengeEvaluationResult`. `ChallengeAttemptComparator` produces `AttemptComparison(firstSuccessful, secondSuccessful, scoreDelta, deadlineMarginDeltaTicks, unusedBudgetDeltaCredits, constructionCostDeltaCredits, winner ∈ {FIRST, SECOND, TIE})`, and refuses to compare attempts under different challenge or evaluation-policy versions, returning structured reasons instead. `AuthoritativeOutcomeFacts` deliberately carries only `contractCompleted` and `completionTick` and explicitly "does not describe queues, dispatch, transfers, or runtime state."

**Gap found on regrounding, not in the original pass:** `ChallengeAttemptComparator.checkCompatibility` checks challenge identity and evaluation-policy identity but **does not check `EngineSemanticsVersion`.** That field does not exist on any landed observation or event yet — propagating it onto runtime observation/event metadata is still-admitted, not-yet-landed work — so today this is latent rather than active. But with two dispatch-policy questions now on record as capable of producing a new `EngineSemanticsVersion` in principle (even though both concluded "retain v1"), two attempts straddling a future semantics change would compare as "comparable" while resting on different production semantics, corrupting exactly the attempt-to-attempt comparison this report requires as evidence #6. This is folded into § Recommended first-slice diagnostic contract as an explicit precondition rather than left as a downstream note.

### Search scope for absence claims

Absence claims above rest on: `git ls-files`-scoped `grep` for `transfer`/`TRANSFERRING`/`bottleneck`/`diagnos`/`utilization`/`busyTicks`/`pendingMultiEligible`/`combinedQueueDepth` across `product/`, `docs/` and `.github/`; direct reading of every `*Observation.java` and `RuntimeEvent*.java` in `factory/process`; `Machine`, `MachineView`, `Job`, `JobView`, `OrderExecution`, `FactoryHandler`, `FactoryRuntime`, `FactoryRuntimeAssembler`, `Routing`, `OperationStepDefinition`; ADR status scan across all 17 decision records; open PR listing (none, original pass); open issue listing (four, original pass, none bearing on this question's diagnostics). The original pass's branch-listing claim ("only `main` and this session's branch exist") was **wrong** — corrected on first regrounding: `origin` in fact carries 15 branches. Every one was checked (`git diff --name-only origin/main...<branch>`) for changes under `factory/process`, `*Observation*`, or `RuntimeEvent*`; none touch those paths, so the substantive absence claim holds, but the original search-scope statement did not accurately describe how it was established.

---

## Candidate models

**A — direct supported-state presentation.** Visualise `ResourceObservation`, `JobObservation`, `OrderObservation`, `PendingWorkObservation`, `RuntimePerformanceObservation`. Little or no derivation.

**B — game-owned diagnostic derivation.** Compute explanatory summaries (waiting-time totals, saturation summaries, deltas) presentation-locally. Not independently viable: without retained history most of these derivations have no inputs. Evaluated as a component of C.

**C — state plus consumer-retained supported-event history.** Observations establish what is true now; the game drains and retains `RuntimeEventEnvelope`s to reconstruct ordered change and compute interval-based summaries. Not event sourcing: authoritative state still comes from observations, and a fresh observation still reconstructs current state without replay.

**D — new supported Engine diagnostic facts.** Engine exposes additional supported observations because a required inference is otherwise impossible. Treated as requiring proof of necessity.

---

## Diagnostic-question matrix

"Current support" is against the landed baseline. "Owner" of each derivation is stated in § Ownership classification.

| Player question | Required authoritative facts | Current Arcogine support | Consumer derivation | Candidate presentation | Misleading case | Gap? |
|---|---|---|---|---|---|---|
| **1. Where is work waiting?** | Job status + current step; per-step eligible resources; multi-eligible backlog membership | **Full.** `JobObservation.status`/`currentStep`, `PendingWorkObservation`, `ResourceObservation.queueDepth`; eligible sets from the published model the game authored | Bucket `Queued` jobs by `currentStep`; attribute to a resource when the step has one eligible resource, to the eligible *set* when it has several | Waiting count per **operation step**, with resource attribution shown only where the step is single-eligible | Per-machine queue badges alone: multi-eligible waiting work appears in no machine's `queueDepth`, so the constraint shows an empty queue | **No** |
| **2. Which operation or resource is constraining performance?** | Ordered occupancy intervals per resource; ordered waiting intervals per job/step | **Full via events.** `JOB_DISPATCHED` → `JOB_STEP_COMPLETED` gives exact occupancy intervals; `JOB_WAITING` → `JOB_DISPATCHED` gives exact wait intervals | Active period method: longest uninterrupted non-waiting period per resource over the run; corroborated by total waiting time accumulated per step | One named constraint per run, with the two supporting numbers (longest active period; waiting time attributable to that step) | Snapshot `busyTicks` utilisation: under-reports in-progress work, reads 0 for a machine mid-long-step, exceeds 1.0 at `concurrency > 1` | **No** |
| **3. Which resources are saturated, starved or materially underused?** | Occupancy intervals; idle intervals; whether eligible work existed during each idle interval | **Full via events + model.** Idle intervals are the gaps between occupancy intervals; concurrent waiting work is reconstructible at any event boundary | Classify each idle interval as *starved* (units still need this step later in the run) or *surplus* (no contention for this resource at any point) | Per-resource occupied / starved / surplus time split | Presenting `Idle` as a state: `MachineState.Idle` conflates starved with surplus. Presenting "blocked": **unreachable** in v1 — queues are unbounded | **No** |
| **4a. Processing versus waiting time?** | Per-unit dispatch/completion and waiting intervals | **Full via events** | Sum per-unit processing intervals and waiting intervals; attribute waiting to the step waited for | Per-unit or per-order stacked processing/waiting bar, split by step | Comparing against `averageLeadTime`: that is order-level and reads 0 until the whole order completes | **No** |
| **4b. Processing versus *transfer* time?** | Transfer intervals | **None landed.** Specified in `engine-semantics-v1.md`, Accepted under ADR-0014/0015, implementation not started | n/a | Deferred | Any transfer/travel visualisation today would be pure animation with no authoritative referent | **No — admitted, unlanded** |
| **5. How far is the requirement from completion?** | Requested/completed quantity; elapsed simulated time | **Full.** `OrderObservation.requestedQuantity`/`completedQuantity`; `metadata.currentTime` | Completion fraction; observed rate over a stated window | Units completed of N, plus deadline margin | `throughputPerTick` and `backlog`: both order-scoped, degenerate for one order. `releasedQuantity` always equals requested | **No** |
| **6. Why did this design perform differently?** | Both attempts' outcome facts; both attempts' diagnostic evidence; the player-authored change set | **Full.** `ChallengeEvaluationResult` + `CandidateDraftSnapshot` per attempt; game retains each run's derived diagnostics | Outcome delta; constraint identity before/after; waiting-time redistribution by step | Side-by-side: what you changed, what the outcome did, where the constraint is now | Any single-cause sentence when the player changed more than one thing | **No** |
| **7. Did the bottleneck disappear, move or remain?** | Constraint identification per attempt | **Full via events, per attempt** | Re-run constraint identification independently for each attempt and compare identities, never just magnitudes | Explicit "the constraint moved from FINISH to CUT" statement | Reporting only "FINISH improved": true and useless — it hides that CUT now limits the system | **No** |
| **8. Did a change actually matter?** | Authoritative outcome facts for both attempts | **Full.** `completionTick`, `contractCompleted`; landed `AttemptComparison` already supports a `TIE` verdict | Compare authoritative outcomes first; report "no material outcome change" as a first-class result | A plain "no material difference" result that suppresses diagnostic storytelling | Manufacturing an explanation for a null result; treating a visual change (a moved machine) as a performance change when landed semantics make placement inert | **No** |

---

## Proving cases

Derived from this question's own candidates and from the failure modes the external evidence names. "A" is direct-state presentation; "C" is state plus retained events with game-owned derivations.

| Case | What happens | A | C |
|---|---|---|---|
| **True capacity bottleneck** (single-eligible, unbalanced) | Slow stage accumulates queue and busy ticks | **Survives.** This is exactly the landed acceptance fixture | Survives |
| **Capacity added at the true bottleneck** | Second FINISH machine; makespan drops materially | **Fails.** FINISH step is now multi-eligible, so its waiting work leaves `queueDepth` entirely and both FINISH machines show empty queues. The improvement is visible, the reason is not | Survives. Waiting time at the FINISH step falls; both FINISH resources show high occupancy; the outcome delta is attributable because only one thing changed |
| **Capacity added away from the bottleneck** | Second CUT machine; makespan unchanged | **Fails, and misleads actively.** CUT's queue visibly empties (its waiting work moves to the invisible multi-eligible backlog), which reads as improvement while the outcome is unchanged | Survives. Constraint identity is still FINISH; waiting time at FINISH is unchanged; outcome delta is null; comparison says so |
| **Bottleneck migration** | Capacity added at FINISH until CUT becomes limiting | **Fails.** Nothing in A re-identifies the constraint; the player sees a former problem improve | Survives, *provided the presentation restates constraint identity per attempt* rather than tracking the old constraint's metric |
| **Starvation versus bottleneck** | CUT idles while units queue for FINISH | **Ambiguous.** `MachineState.Idle` and `activeJobIds` empty are equally consistent with "starved" and "wasted capital" | Survives. Idle intervals are classifiable by whether units still needed that step later in the run |
| **Downstream constraint, upstream waiting** | Units pile up before FINISH | Partially survives — the symptom is visible where it accumulates, single-eligible only | Survives. Waiting attributed to the *step waited for*, not to the resource that last touched the unit |
| **Parallel eligible resources** | Several compatible machines for one step | **Fails decisively.** Per-machine queue semantics do not describe the system; the only faithful count lives in `PendingWorkObservation`. Rendering `combinedQueueDepth` instead would over-count, since one entry counts once per eligible machine | Survives, if and only if waiting is presented **per operation step** rather than per machine |
| **Transient versus persistent congestion** | Momentary queue spike that is not the run's constraint | **Fails.** A snapshot cannot distinguish them; the literature identifies exactly this as queue-length methods' known weakness | Survives. Longest-active-period and cumulative waiting are run-scoped, not instantaneous |
| **Transfer-dominated loss / layout improvement** | Spatial arrangement changes flow | **Not applicable.** No transfer semantics are landed; placement has no runtime consequence | **Not applicable.** Same reason. Neither candidate can be tested until the admitted spatial work lands |
| **Equivalent outcome** | Change produces no material difference | Fails softly — visual deltas invite a narrative | Survives. Landed comparison already returns `TIE`; the rule is to report null results as results |
| **Multiple simultaneous changes** | Player moves a machine *and* adds capacity | Fails | **Survives only with an explicit limit.** C can state the change set and the outcome delta truthfully; it cannot attribute the delta to one change. Attribution requires a controlled re-run |
| **Late/current-state inspection** | Fresh observation without replay | Survives for current state — this is ADR-0011's own guarantee, proven by the landed closure test | Survives. History loss on late join is by design, not a defect; the game controls its own run from tick zero and always retains |
| **Run completion / after-action** | Post-run explanation of delay sources | **Fails.** At quiescence, queues are empty, no job is active, and the performance facts collapse to a single completed order | Survives. The retained event history is the after-action record |

**Candidate B** alone: fails every case whose inputs are intervals rather than instantaneous state — which is most of them. It is a component of C, not an alternative.

**Candidate D**: no proving case in this set requires a fact that C cannot produce. See § Engine gaps.

---

## External evidence

Only sources that changed a hypothesis, a proving case, the recommended evidence set, or the causal-language rules.

**1. Skoogh, Thürer, Subramaniyan, Matta & Roser (2023).** *Throughput bottleneck detection in manufacturing: a systematic review of the literature on methods and operationalization modes.* Production & Manufacturing Research 11(1), article 2283031. DOI 10.1080/21693277.2023.2283031. **Verified in session** via the Chalmers open-access full text (`research.chalmers.se/publication/538741`).

- *Establishes:* 14 detection methods classified by the information they consume — queue states, process states, or combined; queue-state methods suffer transient fluctuation so momentary spikes do not identify the true constraint; high utilisation is not sufficient for a bottleneck, and being period-averaged it detects only an average constraint rather than the one operating at a given moment; shifting bottlenecks are a first-class contingency; no single method is universally recommended.
- *Transfers:* directly justifies rejecting snapshot queue depth and snapshot utilisation as first-slice bottleneck indicators, and justifies making constraint *migration* an explicit presented outcome rather than an edge case.
- *Breaks:* the review's systems have stochastic failures, repair, changeover and finite buffers. Arcogine v1 has none of these — durations are deterministic, queues unbounded, blocking unreachable. Method *selection* transfers; the review's empirical severity rankings do not.

**2. Roser, Nakano & Tanaka — the active period method.** Original papers: "A practical bottleneck detection method," Winter Simulation Conference 2001; "Shifting Bottleneck Detection," Winter Simulation Conference 2002; "Detecting Shifting Bottlenecks," International Symposium on Scheduling 2002; "Monitoring Bottlenecks in Dynamic Discrete Event Systems," European Simulation Multiconference 2004. **Method definition verified in session** from Roser's own exposition at `allaboutlean.com/active-period-method/`; the conference papers themselves are **background, not verified** (the WSC preprint PDF would not parse).

- *Establishes:* the bottleneck is the process with the longest uninterrupted **active** period, where active means *not waiting* — any uninterrupted run of working, repair, changeover, and so on. Frequent interruption by starvation or blocking indicates a process is *not* the constraint. Sole bottleneck when one process holds the longest active period outright; shifting bottleneck during overlap.
- *Transfers, and this is the load-bearing transfer in this report:* the method's inputs are exactly what Arcogine's landed supported event stream provides. `JOB_DISPATCHED(machineId, t)` and `JOB_STEP_COMPLETED(machineId, t')` bound each occupancy interval exactly, because `FactoryHandler` schedules `TaskEnd` at `start + duration`. Active periods are the maximal unions of overlapping occupancy intervals; interruptions are the gaps. This makes the literature's most robust detector a pure consumer-side measurement over supported facts — it observes outcomes and never re-decides an assignment.
- *Breaks:* the method's discriminating power comes from starvation *and* blocking. Arcogine v1 has no blocking, so one of its two interruption sources is absent. In a deterministic, unbuffered, single-product line this weakens the method's ability to separate near-tied resources. Corroborate with accumulated waiting time per step rather than relying on active periods alone.

**3. Sterman, J. D. (1994).** *Learning in and about complex systems.* System Dynamics Review 10(2–3), 291–330. **Verified by publication metadata and abstract; full text not retrieved** (the MIT DSpace PDF returned HTTP 405).

- *Establishes:* barriers to learning in dynamic systems include inadequate and ambiguous outcome feedback and systematic misperceptions of feedback; subjects failed to improve across repeated trials, and the misperceptions proved robust to experience, incentives, and opportunities for learning.
- *Transfers:* this is the single strongest reason the first slice must present *diagnostic* evidence rather than only a score and a completion time. "Retry and see if the number improves" is precisely the outcome-feedback regime this work shows does not teach. It also means the product hypothesis cannot be settled by reasoning — hence § Prototype/playtest protocol.
- *Breaks:* Sterman's tasks involve stochastic feedback loops and time delays across many decision periods. The reference challenge is a short, deterministic, single-decision-per-attempt puzzle where the player sees the whole system. The barrier is weaker here, so this establishes a *risk to test*, not a predicted failure.

**4. Wang, Borland & Gotz (2025).** *Beyond Correlation: Incorporating Counterfactual Guidance to Better Support Exploratory Visual Analysis.* IEEE TVCG 31(1); arXiv:2408.16078. **Verified in session** via the arXiv abstract page.

- *Establishes:* correlation-led visual exploration leads users toward false positives when interpreting causal relations; counterfactual guidance measurably improved causal-inference accuracy against synthetic data with known ground truth.
- *Transfers:* supports making the game's controlled comparison an explicit affordance — "re-run with only this change" — rather than trusting players to infer causation from a before/after pair. It also supports the study design in § Prototype/playtest protocol: synthetic traces with known ground truth, scored against it.
- *Breaks:* the domain is high-dimensional statistical exploration; Arcogine's runs are deterministic with a small, player-authored change set, so the false-positive risk is narrower and concentrated in the multi-change case.

**5. Sarma, Pu, Cui, Brown, Correll & Kay (2024).** *Odds and Insights: Decision Quality in Visual Analytics*, CHI 2024 — reported finding that over 60% of user-generated insights on a mostly-noise dataset were false. **Background, not verified in session.** Cited only as motivation for scoring comprehension against ground truth rather than collecting self-reported insight. Do not let it carry a load-bearing conclusion.

**On Little's Law.** `docs/architecture/standards-alignment.md` already commits to using it "as a reasonableness and scenario-validation relationship *where its assumptions apply*." That condition is not satisfied here, and the argument is an **inference from repository facts**, not from an external source: the reference challenge is one instantaneous batch of 20 units with no ongoing arrivals, so there is no arrival rate, no steady state, and `backlog` takes only the values 1 and 0. Separately, `docs/product/concepts.md` currently presents "Backlog ≈ Throughput × Lead Time" and "Utilization: fraction of time machines are actively working" to users. That framing is sound for the legacy economy dashboard and would mislead a game player. Flagged in § Durable consequences; not remediated here.

---

## Failure and misdiagnosis analysis

Where common indicators give the wrong answer in *this* runtime, with the repository fact that causes it and the companion evidence that prevents it.

| Indicator | Arcogine fact it depends on | Authoritative or interpretation? | Safe inference | How it misleads here | Companion evidence |
|---|---|---|---|---|---|
| Per-machine queue depth | `ResourceObservation.queueDepth` | Authoritative | "This many units are queued on this specific resource *right now*" | Zero in front of every resource whose step has parallel capacity; fluctuates transiently | Waiting counts bucketed **by operation step**, including `PendingWorkObservation` |
| `combinedQueueDepth` | Engine-internal ranking key | Authoritative, but as a *ranking key* | Nothing player-facing | One multi-eligible unit counts once per eligible machine; presenting it inflates visible backlog. Now-`CONCLUDED`, adversarially reviewed research independently characterizes this term as a conditional "handover signal," explicitly not a physical-load estimate — corroboration this report did not originally have | Do not present it |
| Utilisation from `busyTicks` | `busyTicks`, `concurrency`, elapsed | **Interpretation**, not a supported field — `RuntimePerformanceObservation` has no utilisation | "Cumulative *completed* processing time as of the last completion" | Lags by up to one step duration; reads 0 for a machine occupied by one long unfinished step; exceeds 1.0 at `concurrency > 1` | Event-derived occupancy intervals, which converge exactly to `busyTicks` at quiescence |
| `MachineState.Idle` | `ResourceObservation.state` | Authoritative | "Not processing at this instant" | Conflates starved with surplus — opposite design conclusions | Idle-interval classification against whether units still needed that step |
| A "blocked" indicator | — | Would be **invented** | None | Blocking is unreachable: queues are unbounded, no WIP cap exists | Do not present it |
| `throughputPerTick` | `completedOrders / elapsed` | Authoritative | "Completed *orders* per tick" | 0 for the entire run, then 1/T, for a one-order challenge | Units completed of N over time |
| `averageLeadTime` | Order created→completed | Authoritative | "Mean order-level lead time" | 0 until the order completes; then equals makespan | Per-unit processing/waiting decomposition from events |
| `backlog` | Incomplete accepted orders | Authoritative | "How many orders are unfinished" | 1 then 0 | Units remaining |
| `releasedQuantity` | Always `== requested` | Authoritative | None | Reads as release progress; is constant | Omit |
| `setupTime` | Authored resource fact | Authoritative **model** fact | "This resource declares this setup time" | Never used in any duration or dispatch decision; presenting it as a performance fact is false | Omit from diagnostics |
| Bottleneck callout | Derived | **Interpretation** | "Under this named method, this resource was constraining" | Presented as an Arcogine verdict, it silently promotes a consumer heuristic to authoritative truth | Name the method and show its two supporting numbers |
| Attempt score delta | `ChallengeEvaluationResult` | Challenge-owned fact | "This attempt scored better under this policy version" | Reads as causal when several variables changed | The explicit change set, plus a controlled re-run affordance |
| Transfer/travel animation | — | Would be **invented** | None today | No authoritative referent exists in landed behavior | Defer until the admitted spatial work lands |

---

## Recommended first-slice diagnostic contract

### Required first-slice evidence

1. **Production progress** — units completed of N, and deadline margin, from `OrderObservation` and `metadata.currentTime`.
2. **Waiting work by operation step** — count of `Queued` jobs bucketed by `currentStep`, resolved against the published model's step names and eligible sets. Resource attribution shown only where the step is single-eligible. This replaces per-machine queue badges as the primary "where is work waiting" evidence.
3. **Per-resource time split: occupied / starved / surplus** — derived from retained `JOB_DISPATCHED` → `JOB_STEP_COMPLETED` intervals and their gaps, classified against whether units still required that step.
4. **One named constraint per run**, identified by the active period method and corroborated by total waiting time attributed to that step, with both numbers shown and the method named.
5. **Per-unit processing versus waiting decomposition**, with waiting attributed to the step waited for.
6. **Attempt comparison** carrying, as separate rows: the player-authored change set (from the game's own draft snapshots); the authoritative outcome delta; the constraint identity **in each attempt independently**; and the redistribution of waiting time across steps. A null outcome delta is reported as a result, not explained away. **Precondition, found on regrounding:** `ChallengeAttemptComparator` does not yet check `EngineSemanticsVersion` compatibility (that field is not yet landed on any observation/event). Before the game consumes real attempts, comparison must either wait for that field to land and be checked, or the game must independently record and check it itself — two attempts under different Engine semantics must never be presented as a same-semantics comparison.
7. **A controlled-comparison affordance** — "re-run changing only this" — as the only path from association to attribution.

### Useful optional evidence

- A step-level flow diagram annotated with the required facts, as an *arrangement* of the above rather than new information.
- A run timeline of occupancy and waiting per resource, as progressive disclosure behind the summary.
- Interactive inspection of one unit's history from the retained event stream.

### Defer

- Anything transfer- or layout-dependent, until the admitted spatial-transfer work lands: transfer time, `TRANSFERRING` visualisation, admission-load evidence, and the "processing versus transfer" question.
- Retained supported-event history *inside Arcogine*, replay-by-cursor and reconnect/resume. The game controls its run from tick zero and retains what it needs; nothing here requires Engine-side retention.
- `EngineSemanticsVersion` surfacing in the player-facing UI, though attempt records should carry it once it lands, so cross-attempt comparability stays truthful across semantics changes.

### Rejected or misleading indicators

Per-machine queue depth as the primary waiting evidence; `combinedQueueDepth` in any player-facing form; snapshot utilisation from `busyTicks`; `Idle` presented without starved/surplus classification; any "blocked" state; `throughputPerTick`, `averageLeadTime`, `backlog` and `releasedQuantity` as within-run diagnostics; `setupTime` as a performance fact; an unattributed "bottleneck" badge with no method named; transfer or travel animation.

---

## Ownership classification

| Element | Classification |
|---|---|
| Job status, current step, order progress, `activeJobIds`, `queueDepth`, multi-eligible backlog membership, `busyTicks`, run metadata | **Existing supported Engine fact** |
| Ordered `JOB_DISPATCHED` / `JOB_WAITING` / `JOB_STEP_COMPLETED` / `ORDER_COMPLETED` with time and sequence | **Existing supported Engine fact** |
| Step names, per-step eligible resource sets, authored durations | **Existing supported Factory model fact** (the game authored and published the model) |
| Transfer state, transfer events, transfer timing, destination admission load; `EngineSemanticsVersion` provenance | **Already-admitted but not-yet-landed Engine fact** (ADR-0014/0015 Accepted; `engine-semantics-v1.md` normative, implementation pending) |
| Waiting work bucketed by operation step; per-unit processing/waiting split; occupancy intervals; idle-interval starved/surplus classification; active-period constraint identification | **Game-owned diagnostic derivation** — measurement over supported outcomes, never re-deciding an assignment |
| Overlays, timelines, callouts, progressive disclosure, wording | **Game-owned presentation** |
| Attempt identity, admitted candidate snapshot, economics, evaluation result, score, comparability, outcome delta, `TIE` | **Challenge/attempt fact** (landed) |
| Player-authored change set between attempts | **Challenge/attempt fact** — game-owned draft snapshots, no canonical model-diff abstraction required |
| — | **Proven missing supported Engine fact: none** |

The boundary test each derivation passes: it consumes only post-hoc supported outcomes and never reconstructs *why* the Engine chose a particular resource. Reconstructing `selectMachine`'s ranking — offline filtering, `canAcceptJob` as primary key, `combinedQueueDepth`, `MachineId` tie-break — would be duplicating authoritative dispatch semantics. The game must not do it, and no required player inference needs it.

---

## Causal-language rules

The game may state, as measured fact:

- "12 units waited for FINISH, totalling 340 ticks."
- "FINISH was occupied for 380 of 400 ticks; its longest uninterrupted active period was 210 ticks."
- "CUT was idle for 190 ticks while units were waiting for FINISH."
- "Attempt B completed at tick 310; attempt A completed at tick 380."
- "Between A and B you added one FINISH machine and moved the CUT machine."

The game may state, as a named interpretation:

- "Under the active period method, FINISH was the constraint in attempt A and CUT is the constraint in attempt B — the constraint moved."

The game may state, when exactly one variable changed:

- "You added FINISH capacity and nothing else. Completion improved by 70 ticks, and waiting at FINISH fell by 300 ticks."

That is a controlled comparison in a deterministic system, which is the strongest attribution the evidence supports. Even here the honest form names the change and the effect side by side rather than asserting a mechanism.

The game must **not** state:

- "Adding the FINISH machine caused the improvement" when more than one variable changed. State the change set and the outcome delta; offer the controlled re-run.
- Any causal claim about placement while placement has no runtime consequence.
- "Utilisation was 92%" from a snapshot of `busyTicks`.
- "M3 is blocked", or any queue count that includes multi-eligible work more than once.
- A bare "Bottleneck: FINISH" badge with no method and no supporting numbers — that presents a consumer heuristic as an Arcogine verdict.

The general rule: **prefer the weaker true statement.** Where the evidence supports only association, say association. A null result is a result.

---

## Prototype/playtest protocol

The contract conclusion is settled by repository evidence. The product conclusion is not, and Sterman's work is the specific reason it cannot be settled by reasoning. **No participant testing was performed in this investigation, and none is fabricated here.**

Smallest study that would settle it:

- **Material:** 6–8 pre-recorded Arcogine traces from the reference challenge, generated headlessly through `FactoryRuntime` with retained supported events. Ground truth is computed from the trace, not authored. The set must include: one true capacity bottleneck; one capacity-added-at-constraint pair; one capacity-added-away pair with a null outcome delta; one constraint-migration pair; one starvation-versus-surplus case; and one deliberately confounded pair where two variables changed at once.
- **Participants:** 8–12, split between engineering-literate and non-specialist. This is a "does the presentation systematically mislead" question, not an effect-size question, so this is sufficient for a first pass and insufficient for a comparative claim between two designs.
- **Tasks, scored against ground truth, not preference:**
  1. Name the constraining operation or resource.
  2. For a named idle resource, say whether it was starved or surplus.
  3. Name the largest source of delay.
  4. Given two candidate interventions, predict which improves completion.
  5. Given an attempt pair, explain why the second performed differently.
  6. Given the confounded pair, say what can and cannot be attributed. **"Cannot attribute to one change" is the correct answer**, and scoring it as correct is the point of the item.
- **Pre-registered falsification thresholds** — fix these before running, and treat a miss as falsification rather than as a tuning signal:
  - < 70% correct on task 1 → the constraint presentation fails;
  - < 60% correct on task 2 → the starved/surplus distinction is not landing;
  - < 50% correct on task 6 → the comparison presentation is manufacturing causal confidence, which is worse than showing less;
  - any case where a majority reads the capacity-added-away pair as an improvement → the null-result presentation fails.
- **Do not** ask which visualisation participants preferred, or collect self-reported insights as the primary measure.

---

## Engine gaps

**No supported-Engine observation gap is proven by this investigation.**

Applying the Engine-gap decision rule to every candidate gap considered:

| Candidate gap | Verdict |
|---|---|
| A first-class "bottleneck" fact | Not required. Constraint identification is a *method*, and the literature explicitly declines to name one universally correct method. Making it an Engine fact would freeze a consumer heuristic into a durable semantic contract. Fails at "can it be derived transparently by the game" — it can |
| Waiting-time or waiting-since facts on the job projection | Not required. Wait intervals are exactly derivable from `JOB_WAITING` → `JOB_DISPATCHED`. Fails at the same step |
| Instantaneous occupancy on `ResourceObservation` (to fix `busyTicks`' completion-time accrual) | Not required. Occupancy intervals are derivable from events and converge exactly to `busyTicks` at quiescence. This is presentation inconvenience, which the decision rule explicitly excludes |
| Queued job identities on `ResourceObservation` | Not required. `PendingWorkObservation` plus job `currentStep` plus the published model's eligible sets already attribute every waiting unit exactly |
| A starved/blocked distinction on `MachineState` | Not required, and partly meaningless: blocking is unreachable in v1. The starved/surplus distinction is a consumer classification over derivable intervals |
| A reason-for-dispatch fact ("why M3 and not M4") | Not required by any player inference in the validated loop, and exposing it would push authoritative ranking semantics into the consumer contract. Correctly out of scope |
| A canonical model-diff abstraction for attempt comparison | Not required. The game authored both drafts and already retains `CandidateDraftSnapshot` |

**Evidence that would falsify "no gap":** a required player inference emerging from playtest that depends on a fact not present in a fresh observation and not derivable from retained supported events — most plausibly around admission load once transfer lands, where `engine-semantics-v1.md` §10 already anticipates the interpretation problem and states that supported resource observation must expose enough capacity information for bottleneck interpretation. That obligation is already inside the accepted spatial contract; it is not a new gap this report discovers, and it should be re-examined against the landed implementation rather than pre-designed now.

---

## Surviving invariants

1. Current-state observation answers *where* and *how much now*; ordered supported events answer *how long* and *in what order*. Any question containing a duration requires retained events.
2. Waiting work in Arcogine belongs to an **operation step**, not to a resource. Resource attribution is a special case that holds only when the step has one eligible resource.
3. A consumer derivation is safe when it measures outcomes and unsafe when it re-decides them. Measuring how long a resource was occupied is safe; reconstructing why it was chosen is not.
4. Every landed `RuntimePerformanceObservation` field is order-scoped and therefore diagnostically degenerate for a single-order challenge.
5. Deterministic re-execution makes controlled single-variable comparison a genuinely available causal instrument — and makes multi-variable attribution no more licensed than it would be in a noisy system.
6. Presenting an authoritative fact under a name it does not carry — `busyTicks` as instantaneous utilisation, `combinedQueueDepth` as a queue length, `setupTime` as a performance fact — is a truthfulness failure, not a design preference.

## What did not survive

- **Candidate A**, on the parallel-capacity, capacity-added-away, transient-congestion and after-action cases.
- **Candidate B as an independent model.** Its useful derivations require C's retained history.
- **Candidate D**, on necessity. Every candidate gap fell to an existing fact or a safe derivation.
- **The brief's assumption that the reference challenge's diagnostics can be built on the supported performance facts.** They are order-scoped and degenerate for one order.
- **The brief's spatial hypothesis, for the first slice.** Not falsified — untestable, because no transfer semantics are landed.
- **The reading that the landed bottleneck acceptance test generalises.** It is single-eligible only, and the multi-eligible case is where the game lives.

---

## Risk classification

Classified **medium**: cross-consumer product/contract question, shared ownership boundary, likely to influence architecture. That drove broad semantic-neighbor inspection (all 17 ADRs' statuses, four planning documents, the normative Engine semantics contract, the full observation/event source and its acceptance tests, the Challenge consumer module, the legacy transport, branches, PRs and issues) plus external evidence chosen to discriminate rather than to fill a quota.

**Reclassification test applied:** the surviving recommendation introduces no new public or persisted Engine semantics, alters no domain ownership, and establishes no new hard-to-reverse contract. It stays medium, so a self-administered adversarial pass is proportionate and no independent review is required before it is used as evidence for a *product* requirement set.

**If a later pass concludes the opposite** — that a supported Engine observation is needed — that conclusion would be high-risk (it would add durable public Engine semantics) and would require a genuinely independent adversarial review, in a separate session, before it counted as decision-quality evidence for an ADR.

### Self-administered adversarial pass

What I tried to falsify in my own conclusion, and the result:

- *Omitted candidate:* considered a fifth model — Engine-side retained supported-event history serving diagnostics. Rejected: ADR-0011 §8 and §10 deliberately keep retention as later distribution hardening, and the game controls its run from tick zero, so it has no need Engine retention would meet.
- *Proving case that breaks the model:* the strongest attempt was late-join, where C loses history. It does not break C, because ADR-0011 §2 already scopes fresh-observation sufficiency to *current state*, and the game is never a late joiner to its own run.
- *Hidden assumption:* C assumes the consumer reliably drains `drainSupportedEvents()`. Verified this is a real obligation — `advanceUntil` returns internal `Event`s, not supported ones, so a consumer that only reads its return value silently accumulates and then discards supported events. Recorded as an integration requirement, not a gap.
- *Ownership inversion:* checked whether event-derived occupancy competes with `busyTicks` as a second authoritative computation. It does not — it converges to `busyTicks` exactly at quiescence — but to avoid presenting two numbers for one concept, the recommendation is that the first slice present only the derived one and label it as an observed-occupancy summary.
- *Conclusion stronger than evidence:* the original draft conclusion read "the player can diagnose correctly." Weakened to a contract claim plus an explicitly untested product claim, because no participant evidence exists.
- *Stale baseline, checked twice more after the original pass:* `main` moved `56a8762` → `975c163` → `24b2941` across two later regroundings. Both times the load-bearing conclusion held; both times at least one correction was found that the prior pass should have caught (see § Regrounding log). The pattern across three passes is that this report's *contract* half has been stable under real repository movement, while its *search-scope honesty* and *precondition completeness* were where repeated regrounding actually found something — which is itself evidence for treating regrounding as substantive verification, not a formality to restate the same SHA.

This is a self-challenge, performed three times across the investigation and two later regroundings. It is not an independent adversarial review and must not be reported as one.

## Confidence and limitations

High confidence in the repository-derived half: it rests on directly inspected source, executable tests, and Accepted ADRs at a pinned SHA. High confidence that no Engine gap exists for the landed scope, moderated by the fact that the conditional transfer-dependent evidence has not been exercised against a real implementation.

Low-to-moderate confidence in the product half. What would change it: playtest results at or below the pre-registered thresholds; or a proving case from real play in which the active period method and accumulated waiting time disagree and neither corroborates the other.

Not inspected: no runtime execution of the Gradle test suite (this was a read-only investigation, and no source was modified); no prototype built; no participants. The WSC conference PDFs for the active period method could not be parsed, so that method's definition rests on its author's own later exposition rather than the original papers. Sterman (1994) is verified by publication metadata and abstract only.

## Unresolved unknowns

- Whether players form the correct causal model from this evidence set. Empirical, untested.
- Whether the active period method retains discriminating power in a deterministic, unbuffered, single-product line where blocking is unreachable and near-ties are likely — this may need the waiting-time corroborator to carry more weight than the literature assumes.
- What admission-load evidence actually turns out to be needed once transfer lands. Deliberately not pre-designed.
- Whether the first playable slice integrates in-process against `FactoryRuntime` or waits for outward-consumer convergence. That is a consumer-integration decision this report scopes but does not make.

## Durable consequences

Recommended destinations, narrowest first. **This report performs none of them.**

1. **Game product/UX requirement set** (primary): the § Recommended first-slice diagnostic contract, the § Causal-language rules, and the § Prototype/playtest protocol belong in the vertical-slice research brief's requirement set, and move into the game-consumer implementation plan only after the playtest gate is met and the research question is genuinely `CONCLUDED`.
2. **No shared change** to Arcogine semantics. No ADR, no architecture change, no Engine observation.
3. **Two narrow reconciliation candidates, for separate independently reviewed changes:**
   - `docs/product/concepts.md`'s KPI table presents utilisation as "fraction of time machines are actively working" and offers Little's Law as a user-facing relationship. Both are sound for the legacy economy dashboard and would mislead a game consumer, given that no utilisation field exists in the supported observation and the challenge is a single finite batch. Worth a scoped clarification.
   - The sequencing fact that the first playable slice cannot consume the current SSE surface is implied by existing planning but not stated where a game implementer would find it.

## Implementation implication

**No implementation.** Not of the game, not of Engine changes, not of a prototype. The next step is the playtest gate, not code. This report also does not perform the promotion into implementation planning, and the vertical-slice research question remains `ACTIVE` — a report existing is not a conclusion.

## Follow-up triggers

Reopen or extend this question when: the admitted spatial-transfer work lands (the transfer-dependent half of the matrix becomes testable, and admission-load evidence needs a real answer); playtest results arrive, in either direction; outward-consumer convergence lands (the integration-surface question becomes decidable); or a second product consumer needs the same diagnostics, which would be the first genuine evidence that any derivation here is cross-consumer rather than game-local. Track in `docs/research/README.md` against the existing register entry.

## Sources

- Skoogh, A., Thürer, M., Subramaniyan, M., Matta, A., & Roser, C. (2023). Throughput bottleneck detection in manufacturing: a systematic review of the literature on methods and operationalization modes. *Production & Manufacturing Research*, 11(1), 2283031. DOI 10.1080/21693277.2023.2283031. Open-access full text: `research.chalmers.se/publication/538741`. **Verified in session.**
- Roser, C., Nakano, M., & Tanaka, M. Active period method for bottleneck detection — original papers: WSC 2001 ("A practical bottleneck detection method"), WSC 2002 ("Shifting Bottleneck Detection"), ISS 2002 ("Detecting Shifting Bottlenecks"), ESM 2004 ("Monitoring Bottlenecks in Dynamic Discrete Event Systems"). Method definition verified in session via the author's exposition at `allaboutlean.com/active-period-method/`; **the conference papers themselves are background, not verified.**
- Sterman, J. D. (1994). Learning in and about complex systems. *System Dynamics Review*, 10(2–3), 291–330. **Verified by publication metadata and abstract; full text not retrieved.**
- Wang, A. Z., Borland, D., & Gotz, D. (2025). Beyond Correlation: Incorporating Counterfactual Guidance to Better Support Exploratory Visual Analysis. *IEEE TVCG* 31(1); arXiv:2408.16078. **Verified in session (abstract).**
- Sarma, A., Pu, X., Cui, Y., Brown, E. T., Correll, M., & Kay, M. (2024). Odds and Insights: Decision Quality in Visual Analytics. CHI 2024. **Background, not verified in session.** Not load-bearing.

Repository sources are cited inline by path. Original investigation baseline `56a876208a98dd570d384ed9d8e3535dd86f1335`; re-verified against later baseline `24b29418672eaeb74658e04994684bb9fe2e5db4` (§ Regrounding log). All inline paths and facts were re-checked, not merely carried forward.
