# Factory-design game diagnostic evidence: independent adversarial review

> **Disposition:** `ACCEPT WITH QUALIFICATIONS` (section 7).
>
> **Reviewed report:** revision 3 (consolidated), commit `4ad00136441fa29e6b462ac5431b60cf2de1e148`,
> path `workspace/research/investigations/factory-design-game-diagnostic-evidence-report.md`, on the
> workspace branch `workspace/factory-design-game-diagnostic-evidence`. The report's stated research
> baseline is `9e9e4678b18286cea83387b5ffc38741640f0db5`, and its final recheck is
> `306205c33ef93f0433a26c92f85cbd82eadd27f8`. The file is byte-identical at the handoff commit.
>
> **Handoff:** commit `b5dc70e10077e0ac8e8c99d837fad8d8cf4b36ce`, path
> `workspace/research/handoffs/factory-design-game-diagnostic-evidence-adversarial-review.md`.
>
> **Live-`main` review baseline:** `306205c33ef93f0433a26c92f85cbd82eadd27f8`, resolved on 2026-10-07.
> It equals the report's final recheck, so no stale-baseline question arises.
>
> **Authority:** research evidence only. This review changes no brief, register, plan, architecture,
> code, report, oracle or experiment source.

## 1. Independence and anchoring control

**Independence achieved.** This was a fresh, isolated session. I had no part in writing the report,
the oracles, the experiments or the viewers, and no part in the owner sessions. I have no
responsibility for preserving the report's conclusions.

This was **not a different model family**. To my knowledge it is the same model family as the
investigation sessions; the artifacts do not record which model authored them. Sharing the workspace
branch was used only for persistence.

**Anchoring control** followed the handoff's order:

1. Read the brief, its register row, and the owner decisions quoted in the handoff.
2. Re-grounded in live `main`:
   - the Engine semantics, runtime contract and overview;
   - the consumer and vertical-slice plans;
   - the strategy-space record;
   - the analytics-boundary brief;
   - the runtime observation and event types, `FactoryHandler`, `Machine`, and the
     `research-experiments` substrate.
3. Wrote an independent reconstruction of constraints, candidate answers and likely failure cases to
   reviewer scratch. It is summarized in section 2.
4. Only then read the report and its evidence.

**Disclosed exposure:** before step 3, the handoff already named the report's load-bearing claims:
the two-layer model, the reversed analytics conclusion, the busy-counter removal and the bottleneck
deferral. So the reconstruction was blind to the report's reasoning, values and missions, but not to
its framing.

## 2. Independent reconstruction (before reading the report)

**Constraints derived from the authorities:**

- **The supported boundary.** A consumer has the following facts:
  - resource observations: Engine state, concurrency, active jobs, own queue depth, `busyTicks`;
  - job observations, which carry no step start time;
  - pending multi-eligible work, with its eligible set;
  - performance aggregates;
  - supported events: `JOB_DISPATCHED` and `JOB_STEP_COMPLETED`, each with simulation time,
    `JOB_WAITING`, and the others.

  Events are drained, not retained; `FactoryRuntime` documents that a caller needing history keeps
  the drained events itself. Per-step timing is therefore available to a consumer today.
- **Slots and state.** Slots have no Engine identity: slot state is active-job count against
  concurrency. The Engine's machine state `Busy` means at least one active job, so on a multi-slot
  machine it can hide an idle slot.
- **Waiting.** Under always-online semantics, an idle slot should never coexist with queued work
  that is eligible for it. With availability changes it can: Engine semantics §4 rule 7 starts at
  most one own-queue job per trigger, and a machine coming back online is one trigger.
- **The busy counter.** `busyTicks` is completion-credited. That makes it wrong as a mid-run reading,
  and wrong when divided by elapsed time on multi-slot machines. At quiescence, though, it is exactly
  the total processing job-ticks: the counter is misnamed, not wrong.
- **Analytics.** Utilization, occupancy intervals, starved/surplus classification and bottleneck
  inference are already listed as Phase 2 proving classes in the analytics-boundary brief. The
  consumer plan forbids the game from implementing generic analytics locally. Any surface that needs
  them therefore depends on that unresolved question.
- **Machine identity.** Factory canonical content carries authored resource IDs. A landed semantic
  comparator already compares designs by stable `MachineId`.
- **Bottleneck.** The strategy-space record falsified "highest occupancy = constraint". Pre-computed
  controlled interventions are the valid evidence.

**Candidate answers:**

1. facts only;
2. facts plus named characterizations;
3. claim–evidence bundles with refusals;
4. negative: a new Engine fact is needed. I expected this to fail, because events carry dispatch
   and completion times;
5. game-local presentation of every derivation. This conflicts with the planning gate unless a
   derivation counts as presentation.

**Predicted failure cases:**

- the Engine's machine-level `Busy` shown on a multi-slot machine;
- "idle 40%" without saying whether that means any slot or all slots;
- starved versus no-work-left on a machine eligible for two steps;
- shared waiting attributed to one machine;
- per-change attribution and "because";
- identity drift between variants;
- the period convention;
- single-change verdicts that do not compose;
- co-designer walkthrough success counted as usability;
- the busy-counter removal scope.

The report independently reached most of the same constraints. The differences are the findings
below.

## 3. Evidence considered

**Repository facts (live `main`):**

- **Engine semantics:**
  - §2: selection ranks immediate acceptance first; pre-binding waiting paths;
  - §4 rules 6–9: one own-queue dispatch per trigger, also on coming back online;
  - §10, §10.1 and §10.2: busy-tick arithmetic;
  - §14 fixture 15.
- **Runtime contract:** events are drained; Arcogine is not event sourced; retention is defined
  only when a consumer requires it.
- **`FactoryHandler`:** `handleTaskEnd` and `tryDispatchFromQueue` start exactly one job.
- **`Machine`:** `Busy` is set on any start, and `Idle` only when no job is active.
- **Factory model §4:** current allocated IDs are canonical content.
- **`LinearRoutingFamily.resourceId`:** IDs are positional.
- **`FactoryModelSemanticComparator`:** compares by stable `MachineId` and reports order changes.
- **Consumer plan §1 and §4–§5.**
- **Analytics-boundary and evidence-provenance briefs.**
- **Factory Design §10.2:** the "Maximum utilization" verification objective.
- **`governance-evidence.md` line 274.**
- **`HeadlessClosureAcceptanceTest`** and **overview** line 304.
- **History of the busy counter:** commit `e468e826`. The field existed and was never accumulated;
  that 2026-09-02 change added crediting.

**Reruns (executed in this review):**

- **The reviewed experiment.** A `git archive` export of `4ad00136` ran on `gradle:9-jdk21`
  (OpenJDK 21.0.12) with `-PresearchExperimentSources` set to the experiment directory. Result: 156
  tests, 0 failures, 0 errors, 0 skipped. All nine generated files are byte-identical to their
  committed counterparts:
  - the five first-pass results;
  - the second-pass `key.json` and `pack.json`;
  - `inspection.json` and `missions-key.json`.
- **Reviewer probes.** Two reviewer probes ran against the same export (appendix A). They are not
  part of the reviewed experiment and are not committed as sources.

**Hand derivations (this review, before opening the missions oracle):**

- every one of the 13 bakery missions;
- the unasked bakery variants: A + Oven 2 = 23, B + Mixer 2 = 37, B + Packer 2 = 23, and S4's
  single additions and joint addition;
- spot checks of the first-pass corpus: G1 = 67; G4 = 45, with `busyTicks` 4 at tick 9 while two
  slots are busy, and 48 against 45 at completion; G7 = 68, with its single additions at 68 and the
  joint addition at 46; G8 = 67; the 198-tick release backlog; and the order-dependent attribution of
  G1 + A + I (0/−22 or −11/−11).

**Owner-session records:** the first-pass walkthrough record, the discovery notes (including the
cost note added after the report) and the bakery session record.

No external evidence was used. None would discriminate between the candidates beyond what direct
execution of current semantics established.

## 4. Challenged claims

### 4.1 The provisional positive resolution

**Challenged.** Is the surface truthful and usable? Is the evidence sufficient for the decision at
stake?

**Result.**

- **Truthful within the stated scope: survives.** No false fact was found:
  - every official answer is correct (section 4.9);
  - every guardrail counterexample I checked holds;
  - the characterizations are correct for always-online, linear, release-at-once runs
    (section 4.6).
- **"Usable": supported only as "explorable by the owner".** Section 4.8 explains why.
- **Sufficient for the decision at stake: yes, for an internal-slice requirement set**, with two
  limits.
  - Two of the three named characterizations are promoted only "as defined by their eventual owner".
    So the positive verdict depends on the analytics ownership decision, which the report says
    (section 15).
  - The evidence statement "the guardrails passed six mechanical audits" is broader than the
    evidence.
    - The six audits ran on the revision-1 and revision-2 statement contracts (C3b, C3c).
    - As implemented, the terminology audit rejects any non-refusal statement containing `utiliz`,
      `%` or `starv`. Revision 3's surface uses exactly those terms.
    - Revision 3's characterizations were checked only on four bakery scenarios: against
      pre-registered values, plus an internal check that every slot-tick has exactly one state. They
      were not audited across the 13-design corpus.

**Effect.** The conclusion survives as a bounded positive: a truthful surface is achievable within
scope, and the owner found the text prototype explorable. "Usable", "understandable" and
"satisfactory" must be narrowed (qualifications 1 and 2).

### 4.2 Guardrails

**Challenged.** Do the "never present as fact" counterexamples hold? Can a misleading statement pass
the six audits? Does any allowed claim overstate?

**Result.**

- **The counterexamples hold.** Every row I re-derived matches, and the rerun reproduced them.
- **A misleading statement can pass the audits.** The audits are regression checks over each
  contract's own statements: keyword lists plus facets the contract declares about itself.
  - "Assembly held the whole run back", emitted as a direct-fact statement, uses no banned word and
    no constraint-verdict facet. It passes the terminology and counterexample audits.
  - In G4, "The Twin Assembler was Busy from tick 3 to tick 40" is a supported machine-state fact,
    yet it hides the idle second slot during, for example, [3, 6).

  This does not falsify the guardrails, which are rules. It means they are not mechanically
  enforced for new statements: an implementation needs its own acceptance tests derived from them.
  The report anticipated this residual in its section 14.1.
- **An allowed claim can mislead by composition.** "Needed" verdicts from single-removal experiments
  do not compose. Probe 2 ran version A plus a third packer:
  - removing Packer 2 alone gives 30;
  - removing Packer 3 alone gives 30;
  - removing both gives 37.

  Two allowed one-change statements ("Packer 2 is not needed", "Packer 3 is not needed") invite a
  false joint conclusion. The report covers the addition-side interaction (G7, S4's co-binding), but
  not this removal-side dual.
- **The Engine's machine-level state misleads for multi-slot machines.** Probe 1 shows `Busy` with
  one of two slots idle. The revision-3 viewer avoids this by showing slot counts. The guardrail
  list, which allows "direct facts", does not exclude it.

**Effect.** The guardrails survive. Two guardrails must be added, and "mechanically audited" must be
reworded (qualifications 2 and 6).

### 4.3 The reversed analytics conclusion

**Challenged.** Is the reuse need demonstrated, or is possibility treated as necessity? Could
utilization and flow characterization be game-local? Are the basis choices material and complete?

**Result.**

- **Routing to the analytics research is correct.** It is required by the consumer plan's "not
  game-owned by default" rule, whether or not reuse is demonstrated.
- **Reuse beyond the game is not demonstrated.**
  - Utilization has a documented, anticipated second use: Factory Design §10.2's "Maximum
    utilization" verification objective, an illustrative classification and not a consumer.
  - Flow characterization rests on unverified background: industry idle reasons.
  - "Removing the busy counter leaves no supported utilization" is a consequence of the owner's
    removal decision, not evidence of reuse.
  - The report itself says the reversal follows the owner's goal, not new mechanical evidence
    (section 14.6).

  This becomes possibility-as-necessity only if reconciliation records "reusable" as established.
  Game-local presentation must remain a live candidate in the analytics research.
- **The basis choices are material.** Each changes a value:
  - half-open periods: mission 8 gives 100%; an inclusive reading gives 21 of 22, about 95%;
  - per slot: the concurrency denominator;
  - work in progress up to the boundary: G4 at tick 9, where `busyTicks` reads 4 while two slots
    are busy;
  - per machine versus per pool: the rankings differ;
  - known work only: orders arriving over time.
- **The basis choices are not complete.**
  1. **An idle slot with eligible queued work has no category.** It is reachable after an
     availability change, and the definition calls it a contradiction (section 4.6).
  2. **Starved slots are not capped by the remaining eligible work.** Take a two-slot machine, or
     two pooled machines, with both slots idle and one remaining unit: this counts two starved
     slot-ticks per tick, although only one slot could ever receive that unit.
  3. **The pool basis for a machine eligible for two steps is unstated.** One choice is the
     connected pool, as `EligibilityPoolOccupancyOracle` merges it; the other is per step.
  4. **Flow characterization was never exercised** on a machine eligible for two steps, or on the
     13-design corpus. It is implemented only in `BakeryInspectionPack`'s four scenarios; the
     corpus used the superseded revision-1 idle derivation.

**Effect.** The handoff survives, with qualifications 4 and 5.

### 4.4 Platform requirements and ownership

**Challenged.** Are per-step timing, machine identity stability and design comparison correctly
assigned to the runtime and Factory Design, rather than the game, and not inverted?

**Result.** No inversion toward the game was found, but each row needs correction.

- **Per-step timing is not a runtime capability gap.** It is available through the supported
  contract today:
  - a consumer retains the drained events, whose simulation times give dispatch and completion;
  - or it observes after each bounded advance.

  The report names the second route and a runtime-retention route. It omits the first, although
  its own bakery generator does both: per-tick observations plus a whole-run event capture, and
  `ProcessingIntervals` already derives occupancy that way.

  The only open item is who owns retained recorded history. For shared measurements that is the
  evidence/provenance question's scope; otherwise it is consumer data. Making the runtime retain
  history would be a separate, independently triggered runtime-contract change. Listing "the
  per-step timing route" under "still blocked" overstates it.
- **Machine identity needs no Factory change.**
  - Factory canonical content already carries authored resource IDs.
  - The renumbering found in the second pass comes from the research substrate's positional
    `LinearRoutingFamily` IDs and the generator.
  - Stability under edits is a projection rule at the consumer integration boundary. The consumer
    plan deliberately leaves that boundary's identity allocation unassigned.

  "Factory Design (model identity)" as owner therefore over-assigns.
- **Design comparison has already landed.**
  - `FactoryModelSemanticComparator` is Factory-owned semantic comparison; Governance owns the
    `ChangeSet` orchestration. It compares by stable `MachineId` and reports order changes.
  - The report's state column ("a related register question is a CANDIDATE") omits it.
  - Reconciliation should evaluate the landed capability before recording a gap.

**Effect.** The section 10 table must be corrected at reconciliation (qualification 7).

### 4.5 Busy-counter removal

**Challenged.** Is the evidence against `busyTicks` as utilization sound? Is the removal scope
complete and correctly risk-classified? Must a replacement exist first?

**Result.**

- **The evidence is sound against reading the counter as utilization**, both mid-run and divided by
  elapsed time (G4, G11b). But the counter is misnamed and misused, not wrong: at quiescence it
  equals the total processing job-ticks. `ProcessingOccupancyOracleTest` pins that equality. So
  `busyTicks ÷ (concurrency × completion)` is exactly whole-run utilization for an always-online
  machine.

  The evidence supports removal or renaming; it does not by itself require removal. The removal is
  an owner decision, and the report labels it so (section 11). Under `researching.md` it is reconciled
  as an ordinary architecture and implementation change under independent PR review, not as the
  adoption of a research conclusion.
- **The scope is incomplete.** At this baseline it also needs:
  - Engine semantics §10 (the KPI bullet) and §14 fixture 15;
  - the overview's headless-closure paragraph (line 304): bottleneck "by busy-tick utilization", and
    `handleTaskEnd` crediting that "reports cumulative utilization";
  - the `setBusyTicks` rule in `ArchitectureTest`;
  - `Machine`, `MachineView` and `FactoryRuntime`, which the discovery notes list but report
    section 11 compresses;
  - both ISA-95 mapping rows: line 108 (the runtime `Machine`, "busy ticks") and line 128
    (utilization observations);
  - `docs/planning/spatial-runtime-consequences.md`, lines 141, 148, 398, 506, 584 and 592;
  - the analytics-boundary brief: its Phase 1 classification item, Phase 1 proving case 2, and the
    truthful-naming invariant. The owner decision pre-empts part of that question's Phase 1;
  - the evidence-provenance brief's proving cases 4–5;
  - the diagnostic brief's own counter text;
  - the busy-tick mention in the engine-evolution research;
  - the tracked substrate: `ProcessingOccupancyOracleTest` (three tests), the `StarterCorpus` Javadoc,
    `WaitingWorkByStepOracleTest`, and the `ProcessingOccupancyOracle` Javadoc.
- **One item is over-included.** "The Governance evidence mention" (`governance-evidence.md` line
  274) says that verification-objective semantics, including utilization, remain domain-owned. It
  does not mention the counter, stays true after removal, and is in fact supporting evidence for the
  utilization reuse argument. It should not be removed.
- **An adjacent claim is left behind.** The same test and overview paragraph also claim that the
  bottleneck is identifiable by carried load (active plus queued work). That is the "busiest =
  bottleneck" heuristic the guardrails falsify: the release-at-once cutter backlog is the largest
  load in every corpus design. Removing only the busy-tick half leaves a maintained architecture
  claim that contradicts the report's own guardrail.
- **Risk: high.** Agreed.
- **No replacement is needed before removal.** No product consumer exists, and the substrate already
  derives occupancy from events. The overview claim the counter supported should be deleted or
  re-scoped, not replaced.

**Effect.** The removal survives as an owner decision with a corrected scope (qualification 8).

### 4.6 The two-layer model

**Challenged.** Are machine slot state and flow characterization correct under current Engine
semantics, including multi-slot machines and multi-eligible steps? Is rejecting "idle while a unit
waits for it" correct?

**Result.**

- **Slot state, read as active jobs against concurrency, is a correct direct reading.** The viewer
  shows counts without inventing slot identity.
- **Flow characterization is correct for always-online scope.** There, an idle slot never coexists
  with queued work eligible for it:
  - a slot frees only at that machine's own step completion, one slot per completion;
  - the completing unit's next placement, the own-queue stage and the multi-eligible fixpoint then
    fill it;
  - selection ranks immediate acceptance first, so work never enters a waiting path while an
    eligible slot is free.

  The bakery pack's assertion of this never fired.
- **"A contradiction under the current Engine" is false as stated.** Engine semantics §4 rules 6–7
  start at most one own-queue job when a machine comes back online.
  - **Probe 1:** a two-slot oven with four queued loaves, brought back online, shows `Busy`, one of
    two slots working and three loaves in its own queue.
  - The run then finishes at **16 instead of 8**: every later trigger frees one slot and starts one
    loaf, so the second slot is never used again.
  - Neither "starved" nor "no work left" fits that state, the definition rejects it, and
    utilization would read 50% while work waits for that very machine.

  The report scopes out offline machines (sections 9 and 14.5), so its in-scope conclusion holds.
  The claim, however, is worded as an Engine property, and it would travel into the analytics
  handoff that way.
- **Multi-eligible steps.** Step-first waiting is correct. For pooled machines, "starved" means
  potential work, not work destined for that machine; the over-count is described in section 4.3.

**Effect.** The model survives within scope (qualification 3).

*Observation outside this question:* under the normative one-dispatch-per-trigger rule, a multi-slot
machine that goes offline and comes back with a queue can run below its capacity for the rest of a
run. Whether that consequence is intended is an Engine-semantics matter, not this review's. It bears
on the "machine downtime" curriculum item and on the strategy-space record's reopening condition for
availability changes.

### 4.7 The decision not to iterate on bottleneck

**Challenged.** Is handing off bottleneck sound, or is the central concept prematurely settled?

**Result.** The deferral is sound: bottleneck is explicitly left open, not settled. Two limits
apply:

- The report gives no game need for a single-run named method. In a static, variant-based game, the
  bottleneck question is answered by experiment, which the report itself says needs no measurement.
  The single-run method is therefore a possible analytics use, not a demonstrated one.
- The positive verdict covers the concepts actually exercised: slot state, starvation, utilization,
  experiment-based "needed", and one co-binding experiment. Teaching bottleneck remains untested.

**Effect.** This is qualification 5, plus a coverage note in qualification 1.

### 4.8 Owner-walkthrough evidence weight

**Challenged.** Is the asymmetry (failure falsifies, success only smoke-tests) respected throughout?

**Result.** It is stated in report section 14.2 and in the session record, but not respected
everywhere.

- **Where success is used as positive evidence:**
  - The executive conclusion cites the third session as evidence that the surface is "usable", that
    the answer is "satisfactory", and that "learning in the moment was observed".
  - The reversals table justifies "pull-based inspection plus missions is [a usable presentation]"
    by the third pass's success.
- **What limits that evidence:**
  - The owner co-designed the scenarios and missions, and had seen the first pass's key.
  - By the session record's own account, the viewer labels states outright and `util` prints the
    breakdown. So the checked parts of the inspect and analyze missions (1–9 and 12) were
    answerable by reading a labeled line.
  - Mission 10 was answered correctly through a heuristic this research shows is invalid.
  - The single "learning" observation is one unprompted adoption of `compare`.
  - The brief's blinding protocol (shuffled identities) was not used in the third pass.
- **The failures stand as valid falsifications:** the statement list, refusals as UI, the "not why"
  line, the two-layer label confusion and the missed joint variant.

**Effect.** The owner-gate result is "explorable; not obviously unusable to the owner"
(qualification 1).

### 4.9 Mission and oracle correctness

**Challenged.** Are the official answers and the pre-registered values right, and is any mission
wrong or ambiguous?

**Result: verified.**

- My hand derivations of all 13 missions and of the unasked variants match the oracle.
- The first-pass spot checks match.
- The JDK 21 rerun asserts every pre-registered value (156 tests, 0 failures) and regenerates all
  outputs byte for byte.
- No official answer is wrong.

Two ambiguities are presentation issues, not oracle errors:

- **Mission 8:** "from tick 4 to tick 14" depends on the half-open convention, which the viewer
  never taught.
- **Mission 13:** the official "only adding both an oven and a packer" is true among the offered
  variants only.

Mission 10 defines "needed" in time terms in its own text, which is correct.

### 4.10 Scope boundaries

**Challenged.** Are the bounds stated where they bound a conclusion?

**Result.**

- **Stated:**
  - non-spatial, one product, linear routing, release at once, all machines online, no setup and no
    transfer (sections 4, 12.7 and 14.5);
  - for no-work-left, the rule for orders arriving over time ("known work only").
- **Not stated where they bound a conclusion:**
  - **Costs.** Revision 3 never says that "needed" is time-only, or that "worth it" belongs to
    Challenge evaluation. That boundary was written into the discovery notes after the report.
  - **Always-online** bounds the two-layer definition (section 4.6), but the report states the
    rejection as an Engine property.

**Effect.** Qualifications 3 and 6.

## 5. Minor accuracy notes (not load-bearing)

- **Supported events.** Report section 6 lists the supported events without `JOB_WAITING` and
  `MACHINE_AVAILABILITY_CHANGED`.
- **Analytics lifecycle state.** Report section 15 labels the analytics question "(REOPEN)". The
  register state is `READY`, and "reopen" is not a lifecycle state. Reconciliation should not imply
  that the question was ever concluded.
- **`combinedQueueDepth`.** It is "not observable" as a field, but it can be reconstructed from own
  queue depth plus the pending entries that name the machine. Immaterial.
- **The bakery generator.** It records one observation per tick through bounded advancement, and
  also retains every supported event. The report's "after every advance" is approximate.

## 6. Qualifications that must survive reconciliation

1. **Verdict wording.**
   - Promote "bounded positive: a truthful diagnostic surface is achievable within scope; the owner
     found the text prototype explorable (single-person smoke test)".
   - Do not promote "usable", "understandable", "satisfactory" or "learning observed" as
     conclusions.
   - When adopting the owner-gate redefinition, also record the third pass's reduced blinding: the
     owner co-designed the scenarios and missions, and labeled displays answered most missions.
   - The concept coverage is slot state, starvation, utilization, experiment-based "needed" and one
     co-binding experiment; bottleneck teaching is not covered.
2. **"Mechanically audited" means the falsified claims and the C3b/C3c contracts.**
   - Revision 3's characterizations were checked only on four bakery scenarios (pre-registered
     values plus an internal slot-state consistency check).
   - The six audits are keyword and self-declared-facet regression checks, and do not enforce
     truthfulness for new statements.
   - Any implementation must derive its own acceptance tests from the guardrails.
3. **The two-layer model is valid for always-online machines.**
   - Restate "an idle slot with a queued unit waiting for it is a contradiction" as an always-online
     property.
   - After availability changes, Engine semantics §4 rule 7 makes that state reachable (probe 1).
     Any definition handed to the analytics research must add a third category or an explicit
     refusal for it.
4. **Measurement basis completeness.** In addition to the report's choices, state:
   - whether starved slots are capped by the remaining eligible work;
   - the pool basis for a machine eligible for several steps.

   Also record that flow characterization was not exercised on such machines, or on the
   13-design corpus.
5. **Analytics handoff framing.**
   - Utilization and flow characterization are the game's concrete measurement needs.
   - Reuse beyond the game is not demonstrated: utilization has a documented, anticipated
     verification-objective use; flow characterization rests on unverified background.
   - Game-local presentation remains a live candidate.
   - The counter's removal is not reuse evidence.
   - A single-run bottleneck method is optional, with no demonstrated game need.
6. **Guardrail additions:**
   - single-change "needed / not needed" verdicts do not compose, and must never be presented as
     licensing a joint change (probe 2);
   - for concurrency above 1, never present the Engine's machine-level `Busy` as fully working; use
     slot-level state;
   - "needed" is time-only; "finishes sooner" is not "worth it"; and "worth it" may be presented
     only under a named Challenge evaluation method.
7. **Platform-requirement corrections:**
   - **Per-step timing** is available through the supported contract (retained drained events, or
     per-tick observation), so there is no runtime gap. Ownership of retained recorded history
     follows the evidence/provenance path for shared measures, and is consumer data otherwise.
   - **Machine identity** needs no Factory change. Stability under edits is a projection rule at the
     integration boundary, whose ownership the consumer plan leaves unresolved.
   - **Design comparison** must be evaluated against the landed `FactoryModelSemanticComparator` and
     Governance `ChangeSet`, which report order changes by stable identity, before any gap is
     recorded.
8. **Busy-counter removal scope.**
   - Use an exhaustive repository search at reconciliation time. At this baseline it covers the
     items in section 4.5 as well as the report's own list.
   - Exclude the `governance-evidence.md` verification-objective sentence.
   - Reconcile the carried-load half of the bottleneck claim (the overview and
     `HeadlessClosureAcceptanceTest`) with the guardrails.
   - Update the analytics-boundary and evidence-provenance briefs that name the counter.
   - Present the removal as an owner decision. Characterize its evidence accurately: the counter is
     misnamed and misused as utilization, and exact as total processing time at quiescence.

The mission-presentation fixes the report already queues stay presentation work and are not
qualifications: teaching the half-open convention before the first period question, and scoping
"only X helps" to the offered variants.

## 7. Disposition

**`ACCEPT WITH QUALIFICATIONS`.**

**What survived:**

- No load-bearing conclusion was falsified.
- The truth constraints hold on every counterexample checked.
- Every official answer is correct.
- The rerun is exact.
- The characterizations are correct within the stated scope.
- The busy-counter removal is correctly framed as an owner decision with high risk.

**What changes:**

- Several claims are stronger than their evidence: "mechanically audited", "usable", and the
  "contradiction under the current Engine".
- The removal scope and the platform-requirements table need correction.
- Two guardrails, plus the cost boundary, are missing.

None of these reopens the question. Each must be carried into reconciliation as listed in section 6.

**What would change this disposition:**

- **`REOPEN`:** an allowed in-scope claim shown false, or an official answer shown wrong.
- **`MORE EVIDENCE REQUIRED`:** a reconciliation that promotes the research-local definitions
  themselves, rather than requirements "as defined by their eventual owner". That would need the
  corpus-wide audit that section 4.1 found missing.

## Appendix A. Reviewer probes (scratch; reproducible)

Both probes ran as an extra experiment-source directory against the `4ad00136` export, on
`gradle:9-jdk21`. Both passed.

**Probe 1: an availability change leaves a slot idle while work waits for it.**

- **Design:** one step, BAKE 4; one Oven with concurrency 2; four loaves, built with
  `LinearRoutingFamily`.
- **Script:** set the Oven offline, submit four loaves, set the Oven online, observe, advance to
  quiescence, and capture events (`ExperimentRunner`, complete window).
- **Observed after coming online:**
  - Oven state `Busy`, concurrency 2;
  - active jobs: 1;
  - own `queueDepth`: 3;
  - pending multi-eligible work: 0.
- **Completion: 16.** The same design always online completes at 8.

**Probe 2: single-removal verdicts do not compose.**

- **Design:** MIX 2 → BAKE 3 → PACK 4; Mixer, Oven and Packer 1, plus extra packers; eight loaves,
  submitted directly to `FactoryRuntime`.

| Design | Completion |
|---|---:|
| Three packers | 30 |
| Without Packer 3 | 30 |
| Without Packer 2 | 30 |
| Without both | 37 |
