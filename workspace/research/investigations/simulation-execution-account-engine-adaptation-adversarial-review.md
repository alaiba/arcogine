# Engine adaptation follow-up: independent adversarial review

> **Disposition:** `ACCEPT WITH QUALIFICATIONS` (section 9).
>
> **Reviewed report:** commit `9551c0cee42bfdeab07f4a55f931eb10ec5fba4e`, path
> `workspace/research/investigations/simulation-execution-account-engine-adaptation-report.md`, on the
> workspace branch `workspace/simulation-execution-account`.
>
> - The report's research baseline and final recheck are both
>   `912b2ac85cefab3907fc562a3aaa6507e51975a7`.
> - Its pre-registration is `f3975916c925fe6e465203e700e171d0d1a57c7a`.
> - Its probes are `f74c4ad7d9e0aa54743a17bf575699a0a5eeab92`.
>
> **Live-`main` review baseline:** `b9902d407e048f731e31e62e85726ed3b9ddbbff`, at the start of the
> review and again at the final recheck.
>
> **Review custody** (same workspace):
>
> - handoff: `46f5f1aec491532b1f4446ab5a6976597e198340`;
> - pre-review reconstruction: `4d862bfd72a03251c47fa37429c748af671da51b`;
> - review probes: `fe3848ed3cc6c8a9aee3d60248a02b680cef6878`.
>
> Paths are in section 1.
>
> **Scope:** this review covers this report revision only. The original execution-account report
> (`a53778145f3ee9941a4b46c7f04f1ec17e6bff86`) and its independent review
> (`57d87fc4c8ba19927bf50efa4097321208811d5a`, `ACCEPT WITH QUALIFICATIONS`) are preserved
> unchanged and are not reopened. No settled E1–E8 finding was reopened, because no new
> counterexample invalidates one.
>
> **Authority:** research evidence only. This review changes no report, brief, register, plan,
> architecture, code or experiment source of the report.

## 1. Coordinates, independence and chronology

| Artifact | Commit | Path |
|---|---|---|
| Review handoff (owner text; this reviewer committed it because the original commit had failed) | `46f5f1aec491532b1f4446ab5a6976597e198340` | `workspace/research/handoffs/simulation-execution-account-engine-adaptation-adversarial-review.md` |
| Pre-review reconstruction | `4d862bfd72a03251c47fa37429c748af671da51b` | `workspace/research/investigations/simulation-execution-account-engine-adaptation-review-reconstruction.md` |
| Review probes | `fe3848ed3cc6c8a9aee3d60248a02b680cef6878` | `workspace/research/experiments/simulation-execution-account-engine-adaptation-review/com/arcogine/research/executionaccount/adaptationreview/EngineAdaptationReviewProbeTest.java` |
| This review | the commit that adds this file | `workspace/research/investigations/simulation-execution-account-engine-adaptation-adversarial-review.md` |

**Independence achieved.**

- This was a fresh, isolated session with no responsibility for the report, its framing or probes,
  the original report, or the first review.
- Model family: Claude (Opus 5.5). The artifacts do not record their authors' model, so a different
  model family cannot be asserted.
- Committing the owner's handoff text to the shared workspace does not make this a self-review.

**Chronology** (anti-anchoring sequence):

1. Persisted the handoff.
2. Ran the report-availability check: header and section headings only.
3. Grounded in live `main`. The material drift since the report baseline is the
   *implementability versus architectural suitability* method rule
   (`docs/development/researching.md` §1). It landed after the report's baseline, so it is applied
   here as live method, while the report is not faulted for omitting to cite it. The other drift,
   one Factory conformance test, is immaterial.
4. Traced the Engine source and tests, and the Game planning, research and prototype evidence.
5. Committed the reconstruction (`4d862bfd`).
6. Only then read the report, its framing and its probes in full.
7. Executed the probes (section 7).
8. Rechecked live `main` at the end. It had not moved.

**Exposure before the reconstruction:**

- The handoff summarizes the report's claims.
- The section headings name E-guard as "preferred" and D-clock as "designated".
- The assignment brief (`f7da1edc`) was read as the question.

The reconstruction records this exposure. Its counterexamples, remedy space and Game
classification were derived from source and planning, not from the report.

## 2. Independent reconstruction and how it compares

From the reconstruction (`4d862bfd`). The report was read only after this point.

**Repository facts:**

- **Clock.** `Scheduler.nextEvent()` moves the scheduler clock (`T_int`) for every polled event
  (`Scheduler.java:34-41`). Commands stamp their effects at `T_int` (`FactoryRuntime.java:157,246`).
- **Observed time.** Observed time (`T_obs`) moves only on emission (`FactoryRuntime.java:517-526`).
- **`TaskStart`.** It is scheduled only by queue dispatch and backlog dispatch, at the step's end
  time, immediately before its own `TaskEnd` (`FactoryHandler.java:207-208,244-247`).
- **`OrderCompleted`.** It is scheduled at an already-emitted time (`FactoryHandler.java:371`).
- **No positive-duration exemption.** The Factory validator rejects non-positive step durations
  (`FactoryModelValidator.java:74`). Every lone `TaskStart` therefore opens a gap between `T_int`
  and `T_obs`.
- **No hidden marker semantics.** No handler that `FactoryRuntime` wires acts on either marker.

**Counterexamples derived independently:**

- **CE-1, the time channel.** A lone `TaskStart` moves `T_int` while every supported observation is
  unchanged. The next submission is then stamped later, and the lead times change.
- **CE-2, the budget channel.** A queued `OrderCompleted` tail under `QUIESCENT` makes the
  identical next `advanceUntil(…, 1)` consume a no-op marker in one run and complete a job in the
  other. The two runs are observationally identical, and there is no time split.
- **CE-3, the cross-implementation form.** A conforming implementation without markers gives a
  third outcome for the same script.

The two defect components are therefore separable:

- **D-time:** the command clock is hidden from supported observation.
- **D-count:** the advancement unit counts unspecified internal events.

**Comparison with the report:**

| Reconstruction | Report | Assessment |
|---|---|---|
| CE-1 | D2 (§7, §8): the same mechanism, independently reached | Agreement |
| CE-2, budget-only divergence | Specification-derived only ("the same holds for … an `OrderCompleted` marker", §8). Not executed. Not separated from the time channel | New executed evidence (R1). It discriminates between remedies (section 4) |
| Remedies: R-record, R-scope, R-floor, R-remove, R-guard, R-expose | E-guard and E-record. R-scope appears only as a confidence condition (§18) | Omitted remedy candidates (section 4) |
| Game control: tested, admitted, planned, possible | Static runs as research; live pacing as hypothetical (§10) | Agreement on status. The report understates proximity (section 6) |
| Triggers fire at requirement selection, before admission | Trigger 1 conditioned on "an admitted consumer" (§12) | Qualification (section 6) |
| Time-floor candidates include scheduled inputs | D-clock "designated" (§3, §12) | Omitted future candidate (section 6) |

## 3. Finding A: the marker / command-time defect exists, and its scope is narrower than "E6"

**Claims challenged:** report §3 (second paragraph) and §8: a count-budget stop on a marker changes
the next command's time and the outcome, which conflicts with Engine semantics §4 rule 3 and §1.1.

**Evidence considered:**

- source lines (section 2);
- `docs/architecture/engine-semantics.md` §1.1 (lines 65-106; consequence 4 at 99-101), §1.2
  (lines 108-150; the one-event primitive at 118-122) and §4 rule 3 (273-274);
- `runtime-contract.md` lines 49-57 and 105;
- `overview.md` line 302;
- the report's D1 and D2, rerun;
- this review's R1 and R2, new.

**Result: upheld, and sharpened.**

1. **The mechanism reproduces.**
   - The report's D2 rerun is green.
   - CE-1 was derived independently and has the same structure.
   - R2's raw profile pins the gap at exactly the lone start markers: for one machine with three
     units, `[0, 0, 5, 0, 5, 0, 0, 0]`.
2. **What is actually inconsistent.**
   - §1.2 makes session control part of the interpretation: "for an identical ordered command
     sequence", `advance()` processes one *scheduler* event, and `advanceUntil` bounds by that
     count.
   - §1.1's membership test and consequence 4 then make two rules part of the interpretation:
     - where `FactoryHandler` schedules `TaskStart` and `OrderCompleted`;
     - the rule that a command applies at the scheduler clock.
   - Both satisfy the membership test, and neither is recorded.
   - §4 rule 3 says markers "do not gain semantic significance merely because they are present".
     - Read narrowly, it disclaims significance from presence alone. The defect is then an
       omission under §1.1(4).
     - Read broadly, it is false of current behavior.
   - Under either reading this is a genuine specification defect, not permitted internal timing.
     §1.1 has no third category: consequences 1 and 4 apply.
   - The `overview.md` sentence "observed time advances only with emission" is true. It is
     incomplete about command time.
3. **The two components are independent (R1, new).**
   - A run that consumes a stale `OrderCompleted` marker, and one that does not, observe
     identically: `QUIESCENT`, sequence 4, time 5, and later `ACTIVE`, sequence 6, time 5.
   - Neither run has any time split.
   - Yet the same next call `advanceUntil(100, 1)` completes a job in one run and nothing in the
     other. The next submission is then accepted at 10 versus 5, and the mean lead time is 5.0
     versus 20/3.
   - **Consequence:** a remedy that closes only D-time cannot restore observational sufficiency.
     That covers both applying commands at observed time and exposing the command clock. A remedy
     must also fix the counted unit (section 4).
4. **What is not broken.**
   - An identical *public* script reproduces exactly, advancement budgets included.
   - D2 compares scripts that differ by one supported call. The defect is that observationally
     indistinguishable scripts diverge, and that the specification omits the rules that decide how.
   - The report's executive wording ("the run's outcome changes") is accurate for D2 as stated, but
     should not be read as a determinism failure.
5. **Scope.**
   - Affected: scripts that stop on a positive finite budget, including a single `advance()`, and
     then issue a command or take further budgeted progress.
   - Immune: time-guarded unbounded calls, because a `TaskStart` and its `TaskEnd` are adjacent and
     share a time.
   - Every implemented driver is immune. `ExperimentRunner.java:219-221` always passes
     `Long.MAX_VALUE`, and the Game prototype uses per-tick unbounded calls.
   - **Correction to report §8's scan:**
     - The scan covered only `advanceUntil`. Tracked tests do issue commands after a single
       `advance()`: `SessionControlAcceptanceTest:183-190,319-323` and
       `RuntimeEventDeliveryAcceptanceTest:220-225`.
     - Each of those tests asserts that the processed event is an authoritative `TaskEnd` from
       immediate dispatch, so none of them is affected by the defect or by any remedy.
     - Not load-bearing.
6. **What it blocks, and what it does not.**
   - E6's interval-determinacy rule (original report) is conservative under the defect: `T_obs <=
     T_int`, so its supported-time branch never over-claims. The defect does **not** block E6
     itself.
   - It blocks the original report's planned §1.2 clarification that "a command applies at the
     runtime's internal current time" (original report reconciliation item 2 and its case-12
     trigger row). Recorded verbatim, that clarification would make marker placement normative.
   - It also blocks leaving §4 rule 3 as written.
   - **Routing:** the defect can be handled as a **separate bounded owner decision**, sequenced
     before the execution-account reconciliation writes any command-time rule.
   - The report's "before or within the E6 reconciliation that records command-time semantics"
     (§12) survives, read with that narrowing.
7. **Adjacent risk (upheld from report §8).**
   - The admitted same-semantics shared-backlog performance work touches the backlog dispatch path,
     which schedules start markers.
   - Under the current §4 rule 3 wording, removing or moving a marker there would pass as "same
     semantics" while changing budget-stopped outcomes.

## 4. Finding B: E-guard is valid as an invariant, but its compatibility is understated and its selection is not forced

**Claims challenged:**

- report §6.2 (contract and invariants);
- §8 (resolutions);
- §10 ("No new public type");
- §12 ("The marker defect is not deferred … E-guard preferred");
- §16 item 4.

**Evidence considered:**

- R2, the authoritative-turn unit *emulated* by a driver over public calls (`turn()` in the probe
  file). Each turn advances single events until the supported sequence moves, and does nothing when
  `QUIESCENT`;
- static analysis of tracked tests;
- `factory-simulation-engine-readiness.md` §7 (line 198);
- `RecordingScheduler.java:74-81`;
- `FactoryHandler.java:72-82`;
- `FactoryRuntime.java:218-236,480-486`.

**B1. The strongest invariant survives every reachable path tested (R2).**

Under the turn unit, the command clock equals observed time after every return on each of these
paths:

- queue dispatch;
- shared-backlog dispatch with a dedicated-queue second step;
- equal-time completions;
- a time guard whose next authoritative event lies beyond the target (targets 3, 5 and 9);
- the availability cascade that schedules a start marker *inside a command*;
- commands interleaved between turns;
- a fault mid-turn (model F);
- a marker-only tail left queued.

Today's unit opens the gap on the same scripts (R2b). It does not open the gap on the fault path,
which schedules no start marker.

The guarantee is structural, given two premises:

1. a marker is processed only in a turn that also processes a later authoritative event;
2. every event classified authoritative publishes at its own time.

Premise 2 holds today:

- a `TaskEnd` always publishes `JOB_STEP_COMPLETED` in the `finally` path;
- `FactoryRuntime` never queues `OrderCreation` or `MachineAvailabilityChange`.

**Bounded risk.** The classification is by payload type (`RecordingScheduler.java:74-81`), not by
effect.

- A future queued input that is classified authoritative but can be a no-op would move `T_int`
  silently and break the invariant.
- An example is a scheduled redundant `MachineAvailabilityChange`. `FactoryHandler.handleEvent`
  would process it, whereas the command path suppresses redundant transitions
  (`FactoryRuntime.java:218-236`).
- A spatial event that publishes nothing would do the same.
- Any E-guard text must therefore state premise 2 as an invariant rather than inherit it from
  payload classification.

**B2. API and contract consistency: E-guard is not compatibility-neutral.**

The sketch does not decide what `advance()` and `advanceUntil()` return. Either choice contradicts
a tracked assertion:

- **If returned lists include absorbed markers:** `SessionControlAcceptanceTest:376` (a
  `maxEvents = 1` call returns `<= 1` event) fails. That test's model queues work on the Mill, so
  its turns contain a `TaskStart`.
- **If returns are authoritative-only:** `HeadlessClosureAcceptanceTest:410-413` fails. It expects
  `TaskStart` and `OrderCompleted` markers to be returned by `advance()`, and under E-guard a
  trailing marker is never processed at all.

E-guard also changes:

- the text of §1.2 ("unchanged one-event primitive");
- the `advance()` Javadoc ("Processes exactly one pending event");
- the `advanceUntil()` contract ("returning every event actually processed");
- the research substrate's count-based stop derivation. `BoundedAdvance` counts internal events, and
  `ResearchPackageBoundaryTest` admits `advance`/`advanceUntil` as the supported surface.

"No new public type" (§10) is literally true. Yet the observable return values and the progression
step both change. The owner decision must therefore include:

- the return contract (a list-valued turn result, or authoritative-only returns);
- migration of those two tests and of the §1.2 text.

The report's "their internal per-call event lists change" (§6.2) acknowledges only part of this.

**B3. The turn unit is implementable without an Engine change.**

- R2's driver uses only supported run state, the sequence cursor and call counts. A careful driver
  can therefore avoid both components today.
- Under the implementability-versus-suitability rule, that does not settle where the rule belongs.
  The counted unit is defined in §1.2, an Engine-owned interpretation. The inconsistency is the
  Engine specification's, so the Engine owner decides it.
- A consumer workaround is not a substitute for a truthful specification.

**B4. Credible competing corrections. None is falsified, and the choice belongs to the owner.**

| Remedy | Closes D-time | Closes D-count | Behavior change | Main cost |
|---|---|---|---|---|
| **E-guard** (authoritative turns) | yes (R2) | yes | budget-stopped scripts | return contract (B2); premise 2 (B1). Robust to future marker refactors |
| **R-remove** (stop scheduling no-op markers in runtime sessions; derive `ORDER_COMPLETED` from handler state) | yes | yes, only if `OrderCompleted` is also removed or never counted | the same scripts | keeps one event per `advance()` and the `<= 1` assertion. Needs a forward rule for future markers. `TaskStart` has no consumer; `OrderCompleted` is consumed only as a capture signal in runtime sessions, and by `FinanceHandler`, which no runtime wires |
| **E-record** (specify placement, counting and the command clock) | documented, still hidden | documented | none | freezes an implementation artifact. Predicting a budget then requires internal-event knowledge, which pushes `Event` composition toward the supported surface against `runtime-contract.md` lines 49-57 |
| **R-scope** (count budgets are pacing-only; commands after a budget-stopped call are outside the conformance claim) | excluded | excluded | none | narrows §1.2 honestly, but leaves an interactive trap (R4) |
| **R-floor / R-expose** (commands at observed time, or report the clock) | yes / visible | **no** (R1) | small / none | insufficient alone |

The report's preference for E-guard is reasonable: it decouples outcomes from marker structure
("markers become removable"). It is not forced. R-remove is the smaller contract change.

**B5. Urgency against the planning boundary.**

- `factory-simulation-engine-readiness.md:198` excludes "new advancement/session semantics without
  a concrete consumer failure case" from implementation.
- The report invokes this boundary against D-clock (§3 item 1, §12), but calls E-guard "not
  deferred". No present consumer fails: every implemented driver is immune.
- The consistent reading:
  - the **decision** is not deferrable, because the specification must become truthful before
    anything records command time;
  - a **behavior-changing** remedy now needs an explicit owner decision that treats it as a
    specification-defect correction. That is permitted, because the interpretation is a current
    development definition.
  - Otherwise the owner takes E-record or R-scope now, and defers behavior change to trigger T1
    (section 6).

## 5. Finding C: "no Engine closure adaptation now" holds for confined consumers and needs a Game-gate qualification

**Claims challenged:** report §3 items 1–5, §9, §10 and §12. Specifically: B and C add no truth for
exclusive drivers; the library prevents the stale-frontier claim; D-seal is falsified; D-clock is
the only truth-adding adaptation; and no present consumer needs Engine finality.

**C1. The library's limited positive claims are verified.**

- **Executed.** D3–D11 were rerun green on live `main`.
- **Source.** `HeldAccount.license` binds coverage to the attested sequence or to the first witness.
  Coverage is evaluated per claim. Contradiction checks run on retain and on accept.
- **Edge checked: basis taken after the attestation.**
  - The licence's proof sequence can then precede the basis cursor, with coverage trivially
    satisfied.
  - With a truthful attestation this cannot misread:
    - a basis cursor `K > S*` implies changes after `S*` exist;
    - those changes have time `>= b` (the attestation's bound);
    - so the basis time is `>= b > a`, and `license` refuses.
  - With a broken promise it falls into the existing trust class: detectable only if delivered
    (D10).
- **No new counterexample within the library's stated trust model.**
- The library stays research-local (report §19 is upheld).

**C2. The comparative reasons meet the live method rule in substance.**

- **B** would add a non-mutating stop fact. D4's "budget although nothing remains, then a follow-up
  call" shows the derivation can need one more, possibly advancing, call. Closure loops anyway, so
  no exhibited failure is removed.
- **C** reshapes destructive delivery.
- **D-seal** is terminal on an idle runtime. Reasoning checked: with no event, the command clock
  cannot pass the floor.
- **D-terminal** duplicates confinement.
- **D-clock** adds producer finality. But it changes command timing, and it makes throughput, which
  is defined over observed time, depend on the controller's clock calls (report §6.1).
- **Weakness.** §12 also cites the planning boundary as a reason. Planning admission sequences work;
  it does not establish suitability. The affirmative reasons above stand without it.

**C3. Scope of the "no change now" decision.**

- **Supported for every present consumer:**
  - **the research runner:** confinement verified. Its runtime is local to `run()`, and oracles
    receive only `DeclaredEvidence`;
  - **the static Game prototype:** closed scripts;
  - **Challenge:** it drives no runtime, verified by `ReferenceChallengeEvaluationPolicyTest:36-44`;
  - **Governance:** it has no simulation producer.
- **Not invalidated.** No admitted consumer interleaves inputs: the consumer plan and the
  vertical-slice gate are BLOCKED.
- **It does require a near-term Game qualification.** The Game diagnostic report revision
  (`4ad00136`, §15–§16) lists "the live interactive loop decision" as a playable blocker, and says
  that "the owner has stated it is necessary soon".
  - The report's bounded search covered `main`, planning, the register and the analytics briefs. It
    did not include that workspace evidence.
  - "Not admitted" is accurate. Proximity is understated.

**C4. The three questions stay separate.**

| Question | Answer |
|---|---|
| **(a)** A present specification inconsistency | Finding A. Needs an owner decision now, independent of any consumer |
| **(b)** A workable closure protocol | Yes, for confined consumers (C1–C3) |
| **(c)** A future, product-triggered stronger guarantee | Finding D |

They are not one vote on "changing the Engine".

## 6. Finding D: Game admission and the reopening trigger

**Claims challenged:** report §3 item 4, §10 and §12 triggers 1–3, and §18 ("D-clock is the right
future shape", medium confidence).

| Status | Game control evidence |
|---|---|
| **Tested** | Game diagnostic report `4ad00136` §1: "static only", with live loop future work. Prototype (`BakeryInspectionPack.java:141-147`, branch `workspace/factory-design-game-diagnostic-evidence`): submit at t0, availability toggles only before advancement, per-tick `advanceUntil(tick)` with an unbounded budget, and whole-run capture. The text viewer works over recorded JSON. No mid-run runtime command, no finite budget |
| **Admitted** | Nothing playable (`factory-design-game-consumer.md` and `factory-design-game-vertical-slice.md` are BLOCKED). The diagnostic question is READY, with independent review `6484795f` (`ACCEPT WITH QUALIFICATIONS`) |
| **Planned (not admitted)** | Consumer plan §5 "Simulation control": bounded advancement and reset; presentation speed "must not … make outcomes depend on wall-clock timing". §7 items 4 and 6. §6 criterion 2: the required Engine gates must be **landed** before admission |
| **Possible** | A live interactive loop, which the Game report §16 proposes as a new item. Orders released over time, proposed as a register candidate. Machine downtime as a curriculum item. The programme's player role is "designer …, not a live shift operator" |

**D1. The static study and the planned control model coexist (R3, executed).**

- Fixed-input runs paced with frame budgets of 1, 2, 3 or 7, or per tick, produce supported streams
  identical to an unbounded run's.
- The only effect is cosmetic: frames with no visible progress. For one machine and three units
  there are three such frames.
- A first slice that presents precomputed runs, or start-fixed runs at any presentation speed,
  triggers nothing.

**D2. Report trigger 1 is late and too narrow.**

- **Late.** It is conditioned on "an admitted consumer". Consumer plan §6 criterion 2 requires the
  Engine gates for the selected requirements to be landed *before* admission. The trigger must
  therefore fire when a live-loop requirement set is **selected or promoted**, before criterion 2 is
  evaluated. The report does not defer to "after implementation", but "admitted" is one checkpoint
  too late.
- **Narrow.** R4 (executed) shows that *any* mid-run command under count-budget pacing breaks
  consumer plan §5, whether or not past intervals must be presented as final:
  - a player who pauses at the identical supported state (sequence 5, time 5) and submits gets
    acceptance at 5 when the frame budget is 1, and at 10 when it is 2;
  - the mean lead time is 10.0 versus 7.5;
  - under the emulated turn unit, the speed difference instead becomes a *visible* state difference
    (time 5 versus 10), and the command lands at the observed time.

**D3. D-clock should not be "designated".**

- Its sketch (§6.1) is coherent: a supported witness, half-open finality, and equal-time order
  (clock change after the `b` events, commands after it).
- The trigger-2 needs, however, have an omitted candidate: **scheduled (time-stamped) inputs**.
  - The internal `OrderCreation` and `MachineAvailabilityChange` payloads already exist, and
    `FactoryHandler.handleEvent` processes them at event time (`FactoryHandler.java:74-79`).
  - A supported operation that applies a command at a chosen `t >=` the command clock would place an
    input at a chosen simulated time while idle, without a clock-change event type.
  - It directly serves the Game report's release-over-time concept inside a closed script.
- **Specification-derived costs** (not executed):
  - `FactoryRuntime.java:480-486` currently publishes nothing for queued inputs;
  - a queued redundant availability change is exactly B1's silent-authoritative risk;
  - it gives no passive finality witness.
- Event-boundary interaction (commands only between turns) needs neither D-clock nor scheduled
  inputs.
- **At trigger 2**, compare D-clock, scheduled inputs and event-boundary interaction. The report's
  medium confidence (§18) is right. "Designated … the minimum change to adopt" (§3, §12) is
  stronger than the evidence.

**D4. Exact triggers.** None has fired. Each is observable from the selected live-loop requirement
set, before consumer plan §6 criterion 2:

- **T1.** The set includes any runtime command after the run starts (a player or controller input
  after partial advancement). Then either:
  - the Finding A remedy is **landed as behavior** (E-guard or R-remove), or
  - the slice restricts pacing to time-guarded unbounded calls and states that commands apply at the
    last event time.
- **T2.** The set requires a command to take effect at a displayed or requested simulated time while
  the run is idle. This is the time-floor decision: D-clock, scheduled inputs, or event-boundary
  interaction.
- **T3.** A party other than the driver must establish interval finality: a passive verifier, an
  outward adapter, a non-driving Governance producer, or a handover of control. This is the
  witness/attestation decision. Report trigger 2 is upheld.
- **Report trigger 3** (supported concurrent access, which also reopens C) is upheld unchanged.
- **Not triggers:**
  - precomputed or start-fixed runs at any presentation speed (R3);
  - static recorded inspection.

## 7. Probes, results and limitations

**Environment and command:**

- `gradle:9-jdk21`, Temurin `21.0.12+8-LTS`, Gradle `9.8.0`, generic Docker route (no devcontainer
  was running).
- `product/` was exported from live `main` `b9902d40` with `git archive`.
- The experiment sources are byte-identical, by blob hash, to `f74c4ad7` and `fe3848ed`.

```text
./gradlew :research-experiments:test -PresearchExperimentSources=/repo/workspace/research/experiments --no-daemon --no-build-cache
```

**Result: 179 tests, 0 failures, 0 errors, 0 skipped** (counted from JUnit XML):

| Suite | Tests |
|---|---|
| Tracked, including `ResearchPackageBoundaryTest`, which also polices the new package | 137 |
| Original proving cases | 14 |
| First-review probes | 9 |
| Report discriminators | 11 |
| This review | 8 |

The report's "171 tests" and its 11 discriminators reproduce on the live baseline.

Every new review expectation was hand-derived before execution and matched on the first run:

| Probe | Discriminates | Hand-derived expectation (matched) |
|---|---|---|
| R1 `aQuiescentMarkerTail…` | D-count without D-time | Identical observations before the same call. Results: no change versus completion. Acceptance at 5 versus 10. Mean lead time 20/3 versus 5.0 |
| R2a `todaysUnitSplits…ALoneStartMarker` | Today's unit | Clock-minus-observed profile `[0,0,5,0,5,0,0,0]` |
| R2b `theEmulatedTurnUnitNeverSplits…` | The turn unit's strongest invariant | All zero across queue, backlog, equal-time, time-guard, cascade, interleaved and fault paths |
| R2c `todaysUnitSplitsTheClockOnTheSameMixedScripts…` | Today's unit on the same scripts | A positive gap on the mixed, backlog and cascade scripts (5 after the cascade's lone marker). No gap on the fault path |
| R3a `presentationPacingCannotChange…` | Game no-trigger | Identical supported streams for frame budgets 1, 2, 3, 7 and per tick |
| R3b `markerOnlyFrames…` | The cosmetic effect | 3 silent frames |
| R4a `underTodaysUnitPresentationSpeedChanges…` | Game trigger T1 | Identical pause states. Acceptance at 5 versus 10. Mean lead time 10.0 versus 7.5 |
| R4b `underTheTurnUnit…` | Turn unit at a pause | Observed time 5 versus 10. Each command lands at its observed time |

**Limitations:**

- E-guard was **emulated** by a supported-only driver. It was not executed as Engine behavior, and
  it would choose its own return contract (B2).
- R-remove, R-scope, scheduled inputs, D-clock and D-seal are specification-derived only. No
  production change was made.
- Not run: Checkstyle, coverage, the full Java gate, security scans and CI.
- No spatial or transfer execution.

**Falsification outcome.**

- None of the report's load-bearing conclusions was falsified.
- R1 falsifies any remedy that closes only D-time.
- R4 falsifies the narrower wording of report trigger 1.

## 8. Qualifications, owner choices and consolidated reconciliation sequence

**Qualifications that must survive reconciliation:**

1. **The defect has two components.** The marker defect is a §1.1-completeness defect with two
   independent components: the hidden command clock, and the unspecified advancement unit. A
   resolution must address both, or explicitly scope the unit out of conformance (R1).
2. **The defect blocks a specific edit, not E6.** It blocks the original report's planned §1.2
   "internal current time" clarification and the current §4 rule 3 wording. E6's conservative
   interval-determinacy rule is not blocked. Treat the defect as a separate bounded owner decision,
   sequenced before any command-time rule is recorded.
3. **E-guard is preferred, not mandated.** R-remove, E-record and R-scope remain credible. Any
   behavior-changing remedy must:
   - state its return contract, and migrate `SessionControlAcceptanceTest:376`,
     `HeadlessClosureAcceptanceTest:410-413` and the §1.2 text;
   - state "every authoritative turn publishes at its own time" as an invariant, not as a
     consequence of payload classification.
4. **A behavior-changing remedy now needs an explicit owner decision.** Without a consumer failure
   case, it must be classified as a specification-defect correction, because planning keeps new
   advancement semantics out of implementation (`factory-simulation-engine-readiness.md:198`).
   Otherwise, record or scope the defect now and land behavior at T1.
5. **The closure decision is scoped.** "No Engine closure change now" holds for confined and static
   consumers. It is qualified by the Game gate T1–T3, which must be evaluated when a live-loop
   requirement set is selected, before admission criterion 2.
6. **D-clock is one candidate at T2, not a designated adaptation.** The others are scheduled inputs
   and event-boundary interaction. The planning-boundary citation is sequencing, not a suitability
   reason.
7. **The library stays research-local.** Its licensing rules may inform E6 text, but only as already
   qualified by the first review.

**Minimal owner choices:**

- **Marker resolution:** E-guard, R-remove, E-record or R-scope, plus the return contract if
  behavior changes.
- **Timing:** correct behavior now as a defect correction, or record or scope now and change
  behavior at T1.
- **At T2 only:** the time-floor shape.

**Consolidated reconciliation sequence.** This needs no new research question and no standing
follow-up.

1. **Engine semantics owner decision** on the marker/command-time defect.
   - Destinations: `engine-semantics.md` §1.2 and §4 rule 3, plus `runtime-contract.md` only if the
     command clock becomes a supported fact.
   - Independent PR review.
   - Conformance fixtures: report D2, review R1, and the R2 profiles.
2. **Execution-account reconciliation**, on the same workspace.
   - E6 as qualified by the first review; this report's producer-final witness and prefix-bound
     proof may sharpen it.
   - Record a command-time rule only as step 1 decides. The verbatim "internal current time" text is
     acceptable only under E-record, together with the marker rules.
3. **Shared-backlog performance work.**
   - Until step 1 lands, it must not change marker scheduling.
   - Otherwise, its result-equivalence fixture must include budget-stopped command scripts.
4. **Game consumer plan and vertical-slice gate.**
   - Add the T1–T3 checkpoint (§5 Simulation control, §6 admission) at the Game diagnostic
     reconciliation that creates the live-loop item, before any live loop is selected for
     admission.
   - Precomputed and start-fixed slices remain untriggered.
5. **`engine-evolution.md` session/advancement.**
   - Keep the existing reopening condition.
   - The reconciliation may add T1–T3 and the T2 candidate set: no designated shape.
   - The marker correction itself belongs in Engine semantics, not here (report §14 is upheld).
6. **Analytics and Game-local code** must not define their own command-time or finality semantics,
   such as treating a requested tick as the time a command took effect.

## 9. Disposition

**`ACCEPT WITH QUALIFICATIONS`.**

| Decision | Result |
|---|---|
| **Marker defect** | **Upheld and sharpened.** It is a genuine §1.1/§4.3 specification defect with independent time and count components. Not consumer-triggered. A separate bounded owner decision, sequenced before any command-time rule is recorded; it does not block E6 itself |
| **E-guard remedy** | **The core invariant survives** every reachable path tested (emulated). E-guard is not compatibility-neutral: the return contract and two tracked assertions are affected, and premise 2 must become explicit. Not forced: R-remove, E-record and R-scope stay open. Behavior change now needs an explicit owner decision under the planning boundary |
| **Closure now / Game trigger** | **No Engine closure change now is upheld** for every present, confined consumer, and invalidated by none. It is qualified by a Game checkpoint that fires at live-loop requirement selection, before admission. That checkpoint has three split triggers: mid-run commands, commands at requested ticks while idle, and finality without driver trust. D-clock is a candidate, not the designated shape |

**Why not `REOPEN`:**

- No load-bearing conclusion was falsified.
- The omitted scheduled-input candidate and the late trigger wording change only the future-shape
  designation and the checkpoint timing. They do not change the present decisions.

**Why not `MORE EVIDENCE REQUIRED`:**

- The present decisions are sufficiently evidenced, by executed and source-verified evidence.
- What remains open is product-triggered or an owner choice, not missing evidence.
