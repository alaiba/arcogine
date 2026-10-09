# Engine adaptation review: pre-review independent reconstruction

> **Purpose:** the anchoring-control checkpoint required before the independent adversarial review
> reads the follow-up report's recommendation in depth. It records what this reviewer derived from
> live repository authority alone. It is not the review, it gives no disposition, and it is not
> decision-quality evidence by itself.
>
> **Authority:** research evidence only. Nothing here changes architecture, planning, the register
> or product code.

## 1. Coordinates and chronology

- **Workspace:** `workspace/simulation-execution-account`.
- **Review handoff:** committed by this reviewer at `46f5f1aec491532b1f4446ab5a6976597e198340`,
  `workspace/research/handoffs/simulation-execution-account-engine-adaptation-adversarial-review.md`.
  The owner supplied the text, and it is byte-identical to the supplied file. It was persisted
  because the original handoff could not be committed when it was written.
- **Report under review:** `9551c0cee42bfdeab07f4a55f931eb10ec5fba4e`,
  `workspace/research/investigations/simulation-execution-account-engine-adaptation-report.md`.
  Only the availability check was done: it resolves, has 603 lines and a complete 19-section
  structure, and its header states a research baseline and final recheck of
  `912b2ac85cefab3907fc562a3aaa6507e51975a7`. Its argument has not been read.
- **Live `main` at review start:** `b9902d407e048f731e31e62e85726ed3b9ddbbff`. Material drift since
  `912b2ac8`:
  - a new research-method rule, *Implementability versus architectural suitability*
    (`docs/development/researching.md` §1, with matching researcher-agent and report-template
    text). It landed **after** the report's baseline, so the review applies it as live method, while
    noting that the report could not have cited it;
  - one Factory conformance test (`combinedQueueDepth` exactness), which is immaterial here.
  - No Engine, runtime or planning source changed.

## 2. Independence and prior exposure

**Independence achieved.** This is a fresh, isolated session. I have no responsibility for the source
report, its framing, its probes, the original execution-account report, or its first independent
review, and no part in writing any of them. The model family is Claude (Opus 5.5). The artifacts do
not record which model wrote them, so model-family difference cannot be asserted.

Exposure before this checkpoint:

- **The review handoff.** It summarizes the report's claims. It names the D1/D2 marker/budget
  mechanism and sketches E-guard as "process leading non-authoritative markers then one
  authoritative event". It says D-clock is called "the future shape", says the research runner is
  said to be the only implemented driver, and gives the claimed counts (11 discriminators, 171
  tests).
- **The report's section headings.** They include "6.1 D-clock … (designated future adaptation)"
  and "6.2 E-guard … (preferred correction of §8)".
- **The follow-up's assignment brief** (`f7da1edc`), read as the question. It already states, from
  the first review, that supported observed time may lag internal scheduler time after markers.

The original execution-account report, the first review, and the report's pre-registration and
probes have not been read.

## 3. What a supported consumer can and cannot observe

**Repository fact** (live `main`):

| Quantity | Where it lives | Supported? |
|---|---|---|
| Internal scheduler time `T_int` | `Scheduler.currentTime`, set to the polled event's time for every event (`Scheduler.java:34-41`) | **No** |
| Observed time `T_obs` | `FactoryRuntime.observedTime`, moved only by `emit` (`FactoryRuntime.java:517-526`) | Yes (`observe()`) |
| Time a command executes | `scheduler.currentTime()` = `T_int` (`FactoryRuntime.java:157,246`) | **No.** The contract does not define it; an accepted transition reports it after the fact as its event time |
| Internal event count / budget unit | one polled scheduler event, markers included (`FactoryRuntime.java:437-462,569-582`) | **No.** Only exposed as the internal `Event` values returned by `advance()`/`advanceUntil()`, which `runtime-contract.md` says are not public compatibility types |
| Pending markers | queue contents | **No.** `RuntimeRunState` counts only authoritative payloads (`RecordingScheduler.java:65-81`) |
| Supported changes | `drainSupportedEvents()`, sequence | Yes |
| Run state | ACTIVE iff a `TaskEnd` is queued | Yes |
| Interval finality | not represented | No. Whether a later command can still produce changes with a time `< b` depends on future controller calls |

**Marker mechanics (repository fact):**

- `TaskStart` is scheduled only by queue dispatch and backlog dispatch (`FactoryHandler.java:207,244-245`),
  which includes the online-availability cascade. Immediate dispatch, in `submitOrder` (line 322)
  and in the next-step path of `handleTaskEnd` (lines 391-393), schedules a `TaskEnd` only.
- Each `TaskStart` is scheduled at the **end** time, immediately before its own `TaskEnd`, so
  insertion order makes the two adjacent: no other entry can sort between them.
- `OrderCompleted` is scheduled at the completing `TaskEnd`'s own time (line 371). That time has
  already been emitted, and the marker sorts after the other events already queued at that time.
- No handler reachable from `FactoryRuntime` acts on either marker. `FinanceHandler` consumes
  `OrderCompleted`, but `FactoryRuntime` does not wire it. The runtime reads the `OrderCompleted`
  it captures during a `TaskEnd` only to derive `ORDER_COMPLETED` (lines 495-506).

**Consequences (inference):**

- `T_obs ≤ T_int` always.
- `T_obs < T_int` arises only when a `TaskStart` with a positive step duration has been processed
  without its adjacent `TaskEnd`. That can happen only through a single `advance()` call or a count
  budget that runs out between the two. The time guard cannot split them, because they share a
  time.
- An `OrderCompleted` marker can consume a budget unit or an `advance()` call, but it cannot move
  time.
- `advanceUntil(target, Long.MAX_VALUE)` always leaves `T_int == T_obs` whenever it processed
  anything.

## 4. Independently derived counterexamples (hand-derived expectations)

Model **M**: one machine `M1` with concurrency 1, one product with a single step on `M1` taking 10
ticks, and a unit price of 1.

**CE-1: time channel through a lone `TaskStart`.**

Common prefix:

1. `submitWorkload(P, 2)`. Job `j1` is dispatched immediately (`TaskEnd@10`), and `j2` waits in
   `M1`'s own queue. This emits sequences 1-3: `ORDER_ACCEPTED`, `JOB_DISPATCHED j1`,
   `JOB_WAITING j2`.
2. `advanceUntil(MAX, 1)` processes `TaskEnd(j1)@10`. The queue dispatch then schedules
   `TaskStart(j2)@20` and `TaskEnd(j2)@20`, and emits sequences 4-5 at 10. Now `T_obs = T_int = 10`.

The variants:

- **X:** one more `advance()` processes `TaskStart(j2)@20` alone. Now `T_int = 20` and `T_obs = 10`,
  and nothing is emitted.
- **Y:** no extra call.

Before the next command, X and Y observe identically except for `RunId`: time 10, sequence 5, ACTIVE,
the same resources, jobs, orders and performance.

Then both run `submitWorkload(P, 1)` and run to completion:

- **X:** order 2 is created at **20** and its lead time is 10. `avgLeadTime = (20 + 10) / 2 = 15`.
- **Y:** order 2 is created at **10** and its lead time is 20. `avgLeadTime = 20`.

The same holds as a pure budget variant: `advanceUntil(MAX, 2)` against `advanceUntil(MAX, 1)` in
step 2.

**CE-2: budget channel with no time movement (an `OrderCompleted` tail under QUIESCENT).**

Start with `submitWorkload(P, 1)` and `advanceUntil(MAX, 1)`. This processes `TaskEnd@10` and leaves
`OrderCompleted@10` queued. The run state is QUIESCENT and `T_obs = T_int = 10`.

- **P:** next comes `submitWorkload(P, 1)`, so `j2` runs with `TaskEnd@20`. Then
  `advanceUntil(MAX, 1)` consumes the stale marker and leaves no supported change. Then
  `submitWorkload(P, 1)` creates order 3 at **10**.
- **Q:** the same, except the first call is `advanceUntil(MAX, 2)`, which processes the marker. Its
  later `advanceUntil(MAX, 1)` processes `TaskEnd@20`, and order 3 is created at **20**.

The observations before the first shared command are identical except for `RunId`: QUIESCENT,
sequence 2, time 10. The subsequent public scripts are also identical, yet the outcomes differ
because the budget covered different internal events.

**CE-3: cross-implementation form.** Take a conforming implementation that schedules no `TaskStart`.
It processes `TaskEnd(j2)@20` where the current code spends the budget unit on the marker. For
identical model, workload and ordered commands **including the advancement calls**, it therefore
produces a third outcome.

That is exactly the `engine-semantics.md` §1.1 membership test, which §1.2 extends to session
control. **Inference:** marker scheduling, placement and budget counting are result-affecting rules,
and so is the rule that a command runs at internal scheduler time. None of them is recorded in the
specification.

**Not falsified:** reproducibility of an identical **public** script, advancement calls and budgets
included, still holds. What fails is weaker: a supported observation does not determine when the
next command takes effect, and the specification omits rules that decide outcomes.

## 5. Authority analysis (before reading the report's D1/D2)

- `engine-semantics.md` §1.1, consequence 4: a result-affecting rule missing from the specification
  "is a defect in this document". There is no permitted "internal timing" category; consequence 1
  rules out ambient policy.
- §1.2 places `advance()`, which processes one *scheduler* event, and `advanceUntil` inside the
  interpretation. It does not say at what time an external command executes.
- §4 rule 3 says markers "do not gain semantic significance merely because they are present".
  - Read narrowly, it denies only that presence alone makes a marker significant. The conflict is
    then with §1.1(4): the effects are significant and unrecorded.
  - Read broadly, it is falsified by CE-1 and CE-2.
  - Either way, an owner decision is needed: record the rules or change the behavior.
- `overview.md` ("observed time advances only with emission") and the `observedTime` Javadoc
  deliberately hide marker movement from observations. They do not account for command time
  reading the scheduler cursor. The existing test
  `processingANoOpInternalMarkerLeavesTheSupportedObservationUnchanged` pins observation invariance
  only, and never issues a command after a marker. That omitted variant is CE-1.

**Two separable defect components:**

- **D-time:** the command time is hidden from supported observation.
- **D-count:** the advancement unit counts unspecified internal events.

A remedy may close one without the other.

## 6. Remedy space derived independently

| Candidate | Change | D-time | D-count | Compatibility / risk |
|---|---|---|---|---|
| **R-record** | Specify the current behavior: where `TaskStart` and `OrderCompleted` are placed, that budget units include markers, and that commands run at `T_int`. Rewrite §4.3 | documented, still hidden | documented | Freezes an artifact of the implementation as interpretation. A consumer that wants to predict a budget needs internal events, which pushes `Event` toward the supported surface against `runtime-contract.md` |
| **R-scope** | Declare budget-stopped command interleavings outside the conformance claim, so a count budget is pacing only | excluded | excluded | Narrows §1.2. It is honest, but leaves a trap for interactive drivers |
| **R-floor** | Commands execute at `T_obs`, so markers never move the command clock | closed | open (remaining progress differences become *visible*) | Needs a `Scheduler` change, because `schedule` rejects events earlier than `currentTime` |
| **R-remove** | Stop scheduling no-op markers in runtime sessions, and derive `ORDER_COMPLETED` from handler state | closed | closed (every scheduler event is authoritative) | Changes the returned event stream and per-call progress. Keeps "one event per `advance()`". Needs a forward rule for future markers |
| **R-guard** (E-guard as sketched in the handoff) | A turn is the leading markers plus one authoritative event | closed for current placement | closed | Changes `advance()`'s "one event" and its `Optional<Event>` result, and `advanceUntil`'s "maxEvents events" and returned list. Edge rules needed below |
| **R-expose** | Report command time (or `T_int`) to consumers | made visible | open | Putting it in `observe()` breaks the "one sequence boundary" coherence rule, so it needs another carrier |

**Edges any turn-based remedy must decide:**

- **Marker-only tail.** Either process the tail and return what, or leave it queued. Leaving it
  queued keeps `T_int == T_obs` structurally.
- **Time guard.** Test the authoritative event's time, not the leading marker's.
- **Return values.** A turn processes several internal events, which affects `maxEvents=1 ⇒ ≤ 1
  event` (`SessionControlAcceptanceTest:376`) and the marker counts in `HeadlessClosureAcceptanceTest`.
- **Faults.** A fault after leading markers loses the returned list, as today. Emission in the
  `finally` block keeps `T_obs == T_int` for a `TaskEnd` fault.
- **Equal-time interleaving.** Commands landing between same-tick authoritative events are
  unaffected.
- **Future markers.** The invariant depends on markers never standing alone at a time after the
  last emission. This is a bounded compatibility risk for spatial or transfer machinery, not a
  demonstrated fact.

**Predicted strongest-invariant result for R-guard:** with the current marker vocabulary,
`T_int == T_obs` should hold after every return. That covers a `TaskEnd` turn, an `OrderCompleted`
tail at the already-emitted time, an accepted, rejected, no-op or faulted command, and reset. A
falsifier would need a lone marker at a later time, which current placement cannot produce. So the
invariant is contingent on placement, not structural, unless the remedy forbids lone-marker turns.

**Driver-level emulation.** A driver can reproduce R-guard over today's API without seeing internal
payloads: loop `advanceUntil(MAX, 1)` until the supported sequence advances or nothing is
processed. A driver can avoid D-time entirely by never issuing a command after a count-budget stop.
So the defect is avoidable by protocol. The question is whether the Engine's own specification
should keep a misleading unit, not whether consumers can work around it.

## 7. Closure and Game consumer status, before reading the report

**Who drives runs today (repository fact).**

- `ExperimentRunner` is the only main-source driver. It always calls
  `advanceUntil(target, Long.MAX_VALUE)` (`ExperimentRunner.java:219-221`).
- Tests are the other callers.
- No production code issues a command after a count-budget stop, so D-time and D-count are latent
  for every implemented driver.

**Game evidence:**

- **Tested** (Game diagnostic report `4ad00136` and its prototype on
  `workspace/factory-design-game-diagnostic-evidence`):
  - precomputed runs, "static only", with the live loop declared future work (report §1);
  - workload submitted at t0, availability toggles only before advancement, and per-tick
    `advanceUntil(tick)` with an unlimited budget (`BakeryInspectionPack.java:141-147`);
  - a text viewer over recorded JSON.
  - No mid-run runtime command and no finite budget. The prototype labels its observations by the
    tick it requested, not by `T_obs`.
- **Admitted:** nothing playable. The consumer and vertical-slice plans are BLOCKED. The diagnostic
  question is READY, with an independent review (`6484795f`, ACCEPT WITH QUALIFICATIONS).
- **Planned, not admitted** (`factory-design-game-consumer.md`):
  - §5 "Simulation control": bounded advancement/reset; presentation speed changes how the client
    advances and "must not … make outcomes depend on wall-clock timing";
  - §7 items 4 and 6: control through supported primitives, and identical inputs giving identical
    outcomes;
  - §3: "consumer-neutral bounded advancement" is a prerequisite, treated as landed.
- **Possible or future:**
  - a live interactive loop. The Game report §16 says "the owner has stated it is necessary soon",
    as a new research or planning item;
  - order release over time;
  - a machine-downtime curriculum.
  - The programme's player role is "designer …, not a live shift operator".

**Coexistence (inference).** The static study and the planned control model are consistent.
Presentation stepping of a run whose inputs are all given at start needs only bounded advancement.
With no command after a partial advancement, advancement granularity cannot change outcomes. The
marker defect makes some frames show no visible progress, but that is cosmetic.

The planning rule against wall-clock dependence conflicts with current Engine behavior only if a
selected loop admits **mid-run commands**:

- with count-budget pacing, CE-1 and CE-2 make outcomes depend on frame budgets;
- with time-guarded pacing, a command lands at the last event time rather than at the displayed or
  requested tick, which is retroactive relative to what the player sees.

**Closure (inference).** For the confined consumers (the runner, and the static prototype's
precomputed runs), the exclusive driver knows its own future script. Controller-managed closure is
therefore adequate. Today's Engine cannot know that no future command will arrive: that is a promise
about the controller, not a runtime fact.

The cheap Engine-side improvements are:

- a stop reason or outcome for an advancement call (B);
- a bound cursor (C).

Each would make an existing check unnecessary, but neither removes a failure that the confined
consumers actually exhibit. A time floor (D-clock) or seal would make "no future change with time
`< b`" Engine-knowable. It is coherent only with a defined command-time rule, and only a live loop
that selects commands at chosen ticks would need it.

## 8. Candidate reopening triggers (before reading the report)

Decide the Engine session-control questions when the Game live-loop interaction set is **selected
for admission** (the consumer plan §6 criterion 2 check), not after implementation, if it includes
any of:

- **T1:** any runtime command after a partial advancement. The marker remedy must then have **landed**
  as behavior, or the slice must restrict pacing to unlimited-budget, time-guarded calls and accept
  last-event command time.
- **T2:** commands that must take effect at a displayed or requested tick while the run is idle. This
  is the time-floor or clock-class decision.
- **T3:** a consumer that must verify interval finality without trusting the driver. This is the
  seal or attestation decision.

**Not triggers:** precomputed static runs, and presentation stepping (pause, resume, speed) of runs
whose inputs are all given at start.

**Independent of any consumer:** the marker defect itself, because §1.1 is normative today.

## 9. Planned review probes (to run after reading the report, under a distinct review path)

1. CE-1, in both the `advance()` form and the budget form: observation equality except `RunId`,
   different `ORDER_ACCEPTED` time and lead time.
2. CE-2: a QUIESCENT marker tail, budget consumption, and divergent command time.
3. An emulated authoritative-turn driver over public calls only, with a twin-session command-time
   oracle. It attacks `T_int == T_obs` after every return, across a backlog, the availability
   cascade, equal-time completions, zero-duration steps, budgets of zero and of exact exhaustion, an
   exhausted queue, a marker-only tail and reset.
4. Static-presentation neutrality: with all inputs at t0, per-frame count budgets of any size give
   identical final supported results. This is the Game no-trigger case.
