# Pre-registration — analytics ownership Phase 2 post-execution-evidence probes

> **Status:** research custody only; committed before any new probe source exists or runs.
> **Question:** the already-admitted Phase 2 continuation of the simulation analytics ownership
> question (handoff `workspace/research/handoffs/simulation-analytics-ownership-phase-2-post-execution-evidence-revision.md`).
> **Live-main grounding:** `a7f9856617d99e796d528a44aa8c15677c485f55` (#469, `busyTicks` removed), on
> top of #465 (no inert scheduler markers) and #466 (captured execution evidence).
> **Authority:** none. Expected values are derived by hand from Engine semantics §1.2, §2, §3 and
> §4 rules 3 and 5–9, and from runtime-contract "Captured execution evidence and interval
> determinacy". No value here was copied from a run. A disagreement found later is reported and
> investigated; it is never edited back into this file.

## Shared research-local vocabulary (not production semantics)

- **Holder bundle:** run identity; a basis observation `(S0,T0)`; held supported changes; held
  observations; optionally a controller closure.
- **Controller closure:** attestor and scope; inclusive exhaustion target `t` demonstrated by a call
  log whose last call to a target `>= t` processed fewer events than its positive budget, with no
  faulted call since the basis; a commitment against effective input up to `t`; binding to a held
  post-advancement observation `(RunId,S*,T*)`.
- **Interval proof for `[a,b)`** (one of): **P1** the lowest-sequence held change `K` with time
  `>= b`, plus gap-free held changes `S0+1..K-1`; **P2** a held observation with time `>= b` and
  cursor `S`, plus gap-free `S0+1..S`; **C** a valid closure with `t >= b`, bound to a held
  observation, plus gap-free `S0+1..S*`. Basis requires `T0 <= a`. Otherwise refuse, naming why.
- **`active-job-ticks[M,a,b)`:** integral over `[a,b)` of the number of jobs active on resource `M`,
  folded from the basis' active set plus `JOB_DISPATCHED`/`JOB_STEP_COMPLETED` on `M` within the
  proved prefix; changes at time `>= b` contribute nothing.
- **Per-slot utilization, offline excluded:** `(active-job-ticks, concurrency × online ticks in [a,b))`
  as an exact pair.
- **Idle-slot flow characterization per slot-tick:** working; *idle with eligible work queued* (a
  waiting job whose current step `M` serves); *starved* (otherwise, an unfinished job has a later or
  current remaining step `M` serves; reported uncapped and capped by the number of such jobs);
  *no work left*. Step position comes only from supported `JOB_DISPATCHED`/`JOB_STEP_COMPLETED`/
  `JOB_WAITING` facts and the published routing; no dispatch rule is evaluated.

## Port of prior evidence, unchanged-input scope

The two prior custody experiments and the Game experiment do not compile at live main: they read the
removed `ResourceObservation.busyTicks`. A port removes only those reads and keeps every other
assertion and expected value as originally registered.

| Port | Expected |
|---|---|
| Prior A | `backlog`, `completedOrders`, `averageLeadTime`, `throughputPerTick` recompute exactly from the same observation; 23 observations across 14 corpus fixtures, some with incomplete orders |
| Prior B | `completedSalesValue` 40.0 versus 80.0 with otherwise identical fresh observations; complete completion events reproduce each; late join retains 20.0 against the supplied 40.0 |
| Prior C, restated | The two pinned runs' normalized closing observations are now **entirely equal**; complete-event occupancy over `[0,5)`: run X `{A1:5, A2:0}`, run Y `{A1:0, A2:5}` |
| Prior C′, restated | `completedSalesValue` reconstructs from complete events on every complete-window corpus fixture |
| Prior D1 | Identical performance for cut-heavy and assemble-heavy single orders (completion 34); mid-run waiting `{CUT:3}` vs `{ASSEMBLE:3}`; total waits `{CUT 48, ASSEMBLE 0, INSPECT 0}` vs `{CUT 6, ASSEMBLE 42, INSPECT 0}` |
| Prior D3 | 34, 18, 39, 29; single deltas −16 and +5 sum to −11, joint −5 |
| Prior admission 1 | Oven per-tick `0 0 1 1 2 2 2 2 2 2 2 2 2 2 1 1 0` by observation and by events; `[4,14)` 20/20; inclusive 21/22; whole run 24/34; `Busy` 14/17; Mixer 12/17; occupied `[0,9)` 12 |
| Prior admission 2 | Oven back online: `Busy`, 1 of 2 active, `queueDepth` 3, completes 16 vs 8; slot-ticks working 16, idle-with-eligible-work-queued 12, no-work-left 4; continuously-online occupancy refuses |
| Prior admission 3 | Packer starved uncapped 8, capped 4, no-work-left 1 |

## New discriminating probes

### P1 — stale frontier (five versus 100)

One resource `M` (1 slot), one step `WORK` 5, one unit. The basis is the initial observation
`(0,0)`. Submission publishes 1 `ORDER_ACCEPTED`@0 and 2 `JOB_DISPATCHED`(M)@0; a stale holder
drains here and observes `(2,0)`. The driver calls `advanceUntil(100,1)` (processes 1, so the budget
is spent) and again (processes 0 < 1, demonstrating exhaustion through 100): 3
`JOB_STEP_COMPLETED`@5, 4 `ORDER_COMPLETED`@5, post-advancement observation `(4,5)` `QUIESCENT`.

| Case | Expected |
|---|---|
| P1.1 complete holder, closure bound to `(4,5)`, `[0,100)` | 5 (proof C) |
| P1.2 stale holder `{1,2}`, closure bound to `(4,5)` | refuse: 3..4 missing |
| P1.3 stale holder, closure with no binding | refuse: closure not bound |
| P1.4 stale holder, no closure | refuse: no proof for `b=100` |
| P1.5 naive fold on stale holder (unfinished dispatch runs to `b`) | 100 |
| P1.6 complete holder, no closure, `[0,100)` | refuse (quiescence is not finality) |
| P1.7 complete holder, no closure, `[0,5)` | 5 (proof P1, witness 3@5, prefix through 2) |
| P1.8 stale holder, no closure, `[0,5)` | refuse |
| P1.9 one budget-1 call only (budget spent) | closure invalid; `[0,100)` refuse |
| P1.10 one unbounded call to target 50 | `[0,100)` refuse; `[0,50)` 5 (proof C) |

### P2 — earlier prefix survives a later gap

`M` (1 slot), `WORK` 5, two units: 1 `ORDER_ACCEPTED`@0, 2 `JOB_DISPATCHED`(u0)@0, 3 `JOB_WAITING`(u1,{M})@0;
`advanceUntil(5)`: 4 `JOB_STEP_COMPLETED`(u0)@5, 5 `JOB_DISPATCHED`(u1)@5; the holder keeps 1..5 and
observes `(5,5)`; `advanceUntil(100)`: 6 `JOB_STEP_COMPLETED`(u1)@10, 7 `ORDER_COMPLETED`@10, which the
gap holder loses, then observes `(7,10)`.

| Case | Expected |
|---|---|
| P2.1 gap holder `[0,5)` | 5 (witness 4@5) |
| P2.2 gap holder `[0,10)` | refuse: 6..7 missing against observation cursor 7 |
| P2.3 complete holder `[0,10)` | 10 (witness 6@10) |
| P2.4 holder of `{1..4}` only, `[0,5)` | 5 (witness 4@5, the dispatch at 5 is not needed) |
| P2.5 gap holder `[5,10)` | refuse; complete holder 5 |

### P3 — late join

| Case | Expected |
|---|---|
| P3.1 | prior C restated (above) |
| P3.2 | `JobObservation` components exactly `jobId, orderId, ordinalWithinOrder, productId, status, currentStep, totalSteps, currentMachineId, createdAt, completedAt`; `ResourceObservation` exactly `machineId, name, state, concurrency, activeJobIds, queueDepth, capacityLiters, setupTime`; `RuntimePerformanceObservation` exactly `backlog, completedOrders, completedSalesValue, averageLeadTime, throughputPerTick` |
| P3.3 | P2's run joined after `advanceUntil(7)`: basis `(5,5)` shows u1 active on `M`; with later changes 6..7, `[5,10)` = 5; `[0,10)` refuse (basis after `a`) |

### P4 — Game-defined period, availability treatment and flow

**P4.1** `BAKE` 4 on a 2-slot Oven; offline, submit 4, online, all at time 0. Changes: 1 offline,
2 accepted, 3..6 waiting, 7 online, 8 dispatch u0@0, 9/10 complete u0 + dispatch u1@4, 11/12@8,
13/14@12, 15 complete u3@16, 16 order completed@16.

| Case | Expected |
|---|---|
| per-slot utilization `[0,16)`, offline excluded | (16, 32) |
| `[0,20)` without closure | refuse |
| `[0,20)` with closure through 20 bound to `(16,16)` | (16, 40) |
| continuously-online occupancy oracle | refuses (availability changed) |
| flow `[0,16)` | working 16, idle-with-queued 12, no-work-left 4, starved 0 |
| flow `[0,20)` with closure | working 16, idle-with-queued 12, no-work-left 12, starved 0 |

**P4.2** `MIX` 4 on Mixer (1 slot) then `PACK` 1 on Packer (2 slots), one unit: 1 accepted, 2 dispatch
Mixer@0, 3 complete step 0@4, 4 dispatch Packer@4, 5 complete@5, 6 order completed@5. Packer flow over
`[0,5)`: starved uncapped 8, capped 4, no-work-left 1, working 1.

**P4.3 known-work horizon.** Closed run = P4.2. Open run = P4.2 plus, after quiescence at 5, one more
unit: 7 accepted@5, 8 dispatch Mixer@5, 9 complete step 0@9, 10 dispatch Packer@9, 11 complete@10,
12 order completed@10.

| Case | Expected |
|---|---|
| normalized changes 1..6 and the observation at 5 | identical in both runs; `QUIESCENT` |
| closed, closure through 9, Packer flow `[5,9)` | no-work-left 8, starved 0 |
| closed, no closure | refuse |
| open, complete capture | starved uncapped 8, capped 4, no-work-left 0 |

### P5 — unfinished work crossing the upper boundary

`WORK` 10 on `M`, one unit: 1 accepted@0, 2 dispatch@0, 3 complete@10, 4 order completed@10.

| Case | Expected |
|---|---|
| running occupancy `[0,6)` via witness 3@10 | 6 |
| same via a driver that stops at `advanceUntil(6)` (0 events), closure bound to `(2,0)` | 6 |
| completion-credited processing within `[0,6)` and within `[0,10)` | 0 and 0 |
| running occupancy `[0,10)` | 10 |
| `[6,10)` | 4 |
| `[6,12)` without closure / with closure through 12 bound to `(4,10)` | refuse / 4 |

### P6 — method promotion without custody transfer

The P4.1 bundle with closure (attestor `driver`, scope `closed script`) is passed unchanged to two
call sites of one stateless definition. Both give (16,32) for `[0,16)` and (16,40) for `[0,20)`; the
`[0,20)` proof names the attestor and scope. With the closure stripped: `[0,16)` (16,32), `[0,20)`
refuse, idle-slot no-work-left over `[16,20)` refuse, current slot state at `(16,16)` = 0 working,
2 idle. Given only the observation `(16,16)`: slot state as above; `[0,16)` refuse (basis after `a`).

### P7 — command time and observation clock

P1's model: one unit, unbounded `advanceUntil(100)`: observation `(4,5)`, `throughputPerTick` 0.2,
`averageLeadTime` 5.0. Period rate `[0,100)` under a named definition: 1/100 with closure, refused
without. A second unit submitted then is accepted at time 5 (5 `ORDER_ACCEPTED`@5, 6 dispatch@5),
completes at 10 (7, 8); final observation `(8,10)`, completed 2, mean lead 5.0, throughput 0.2. In P1's
budget-1 run, the first call that processed an event moved the cursor from 2 to 4.

### P8 — fault provenance

`CUT` 5 on Cutter, then `HOLD` `Long.MAX_VALUE` on Holder; one unit. `advanceUntil(100)` throws
`EventOrderingViolation` after publishing 3 `JOB_STEP_COMPLETED`(step 0)@5 and 4 `JOB_DISPATCHED`(Holder,
step 1)@5.

| Case | Expected |
|---|---|
| held changes | 1..4 gap-free; `RuntimeEventType` has exactly six values, none naming a fault |
| observation after the fault | `(4,5)`, Holder active `[u0]`, run state `QUIESCENT` |
| Holder `[0,100)`, closure recording the fault | refuse |
| Holder `[0,100)`, closure that hides the fault | 95 |
| `[0,5)` via witness 3@5 | Cutter 5, Holder 0 |

## Execution plan

Run each probe with the documented `-PresearchExperimentSources` mechanism, together with
`ResearchPackageBoundaryTest`, at live main; then rerun the whole tracked `:research-experiments:test`
suite plus the probes. Report counts, failures and any disagreement exactly.
