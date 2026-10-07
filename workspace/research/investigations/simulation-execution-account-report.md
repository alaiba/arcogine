# Simulation execution account semantics: investigation report

> **Research status:** `READY`. The registered question stays admitted and unresolved until a
> separate, independently reviewed reconciliation lands. This report changes no register entry.
>
> **Research baseline:** live `main` `79b3399149499f2f337206c909a277f66a5befd5`, resolved
> 2026-10-08 at the start of the run. It equals the handoff's recorded admission commit.
>
> **Final live-main recheck:** `79b3399149499f2f337206c909a277f66a5befd5`, re-resolved before this
> report was committed. `main` did not move during the investigation.
>
> **Workspace branch:** `workspace/simulation-execution-account`. Handoff commit
> `5a433fc5de3fea4ff935a659ebc548676b6ee1fa`, path
> `workspace/research/handoffs/simulation-execution-account.md`.
>
> **Risk:** High, as the brief classifies it. It touches identity, history, completeness,
> determinism/reproducibility and major ownership.
>
> **Authority:** research evidence only. Nothing here is accepted architecture, a runtime-contract
> change, an Engine-semantics change, product direction, planning admission, or implementation
> commitment. It does not decide simulation-analytics ownership.
>
> **Adversarial-review status:** required (High risk), **not yet performed**. The only adversarial
> pass on this revision is the author's self-challenge (§21). This report is not decision-quality
> evidence for any architecture or runtime-contract change until a genuinely independent adversarial
> review binds to this exact revision.

## Non-goals honored

No product code, production type, module, database, Storage support, event sourcing, unbounded
retention, transport schema, replay/seek/checkpoint/restore/fork capability, Operational-identity
unification, execution-context registry, or final utilization/starvation/bottleneck/comparison
formula is proposed. No architecture, planning or register document is edited. The paused analytics
Phase 2 revision is consumed as candidate evidence; it is neither resumed nor reviewed here.

## 1. Question, decision at stake, scope

**Question (unchanged from the brief):**

> What minimum consumer-neutral semantic account, if any, of one simulated execution must Arcogine
> define so independent capabilities can reason about the same execution without reconstructing
> Engine behavior, making the Engine event sourced, or independently assembling incompatible notions
> of execution history?

**Decision at stake.** Whether a cross-consumer execution-account responsibility exists; if so, its
minimum boundary and owner; what stays Engine-owned; what belongs only to analytical definitions or
consumers; which analytics evidence/provenance concerns are really upstream; and whether the paused
analytics Phase 2 premise ("the first consumer owns evidence-window capture/completeness") survives.

**Decomposed per the handoff** into referent, identity, boundary, basis, record semantics, ordering,
completeness, live/completed continuity, retention, authority, downstream reasoning and cross-domain
relation (§8 and §10–§14). These are not collapsed into one "event log" question.

## 2. Executive conclusion

**Result class: the conservative outcome, strengthened — no additional execution-account
responsibility (owner, entity, identity, terminal status or custody) is justified, but the "caller
declares its own completeness" form of the current boundary fails. A bounded set of consumer-neutral
execution-evidence coverage rules is necessary; those rules are entailed by Engine-owned facts and
belong in the existing Engine-owned runtime observation/event contract (with session-time rules in
Engine semantics §1.2). Physical capture stays with whichever party holds the runtime's delivery.
Analytical definitions only state which coverage they need.** This is the brief's exit criterion 1 in
ownership terms, with the qualification that the rules are not caller conventions: callers obey them,
the runtime contract defines them. Among the handoff's candidates it is Candidate 5 (hybrid) in the
specific form "one cross-consumer coverage contract upstream, caller-held custody, analytics layered
above" (§7).

**Why "consumer declares its own completeness" fails** (all executed, §9): from identical supported
evidence, independent consumers following locally reasonable rules reach incompatible histories:

- a consumer that treats `advanceUntil(100)` as closing `[0, 100)` reports Assembler occupancy 10;
  a later command — accepted at simulated time 13, not 100 — makes the true value 15 (case 6b);
- two in-process drainers of one live run each hold a gap-free-looking partial account; one cannot
  see its own tail loss without a frontier cursor (case 13);
- a gap-blind consumer reads Cutter occupancy 13 where the truth is 6 (case 3);
- a late joiner treating its capture as the run reads the Cutter as never used (truth 6) (case 2);
- the same supported events with equal-time groups reordered produce an impossible state (two jobs on
  a concurrency-1 machine) (case 5).

None of these is an analytical-definition choice: each wrong reading contradicts Engine semantics.
That is the discriminator for ownership: **rules with no legitimate alternative, entailed by the
producer's ordering, time and epoch semantics, belong with the producer's contract; rules with
legitimate alternatives belong to named definitions** (case 8 shows the latter: one unchanged account,
slot utilization 1/2, machine-busy fraction 1, and the research substrate's continuously-online
occupancy refusing outright).

**The minimum consumer-neutral account semantics (E1–E8), as rules, not types:**

| | Rule |
|---|---|
| **E1 Referent and identity** | One simulated execution is one runtime epoch, identified by `RunId`, from establishment (cursor 0, time 0, state fixed by the published model) onward. Captured evidence is an *account of* that epoch, not the execution; several accounts of one epoch can coexist and agree wherever their coverage overlaps. A supported fact is identified by `(RunId, sequence)`; evidence of different runs never merges. No account identity is needed. |
| **E2 Boundary** | Establishment starts an execution. `reset()` establishes a new execution and ends nothing: the original runtime stays usable. Quiescence is a current state, not an end. The Engine has no terminal completion; ending an account is a declaration by the party driving the session. |
| **E3 Basis and record membership** | The account is bound to its `ModelFingerprint` (and `ControlledRevisionId` when authoritatively bound). Effective accepted commands appear as supported changes. Rejected, accepted-no-op and faulted command outcomes are controller-held results, not supported facts. The producing repository revision (hence Engine definition) is held by custody, not by the account. Reproduction needs the controller's own script at session-control granularity; supported history alone does not determine it. |
| **E4 Ordering** | Supported sequence is the semantic order. Simulation time is a non-decreasing domain coordinate along sequence; equal times never reorder. |
| **E5 Coverage** | An account states its state basis (cursor and time of an observation; cursor 0 for capture from establishment), the supported events it retained after that cursor, and a frontier (the latest observation's cursor and supported time). Missing sequences between basis and frontier are gaps, detected by arithmetic; tail loss is detectable only against a frontier cursor. A claim spanning a gap is refused. |
| **E6 Interval determinacy** | A claim over simulation interval `[a, b)` is licensed only with a basis at or before `a`, gap-free changes through the frontier, and either supported time `>= b` or a closure declaration by the driving party covering `b`. Advancement requests are not closure. |
| **E7 Authority** | An account is never authoritative current state. Current state is a fresh observation; folding history yields consumer views only. |
| **E8 Delivery** | Drained delivery is single-consumer. Sharing one live execution among independent consumers needs one capture holder that fans out, or the runtime contract's separately triggered retained cursor-addressable delivery. Competing drainers' partial captures are detected under E5, never trusted. |

**Retained custody: unnecessary centrally now; optional and held by the capture holder** (§13). No
current consumer needs a party other than the session driver to hold history. The only structural
pressure for shared custody — several independent live consumers of one execution, or a late joiner
needing pre-join history — maps onto the runtime contract's already-anticipated retained delivery
hardening, not onto a new execution-record capability.

**Implementable by caller capture today: yes.** A ~200-line research-local `CapturedAccount` built
only on the supported contract implements E1–E8 and passes all proving cases and the research
package-boundary rules (§9, §24). Two items need controller-held facts: command outcomes (E3) and
closure (E6).

**Missing supported facts found (recorded, not recommended for addition now):** a fault is invisible
to any non-controlling consumer (case 10); a run's advanced-through horizon is not representable in a
supported observation (case 6); and a command applies at the runtime's internal current time, which
can be later than the latest observed supported time (case 12). Each has a reopening trigger (§19).

**Exact Engine-definition identity: not needed now.** The first claim that would become untruthful
without it is a comparison or reproduction claim that spans a retained account produced under one
repository revision and a run under another (§14.5). Every proving case here runs within one revision
under one custody.

**Consequence for paused analytics Phase 2:** its Rule 7 ("evidence custody follows the definition
owner") conflates three separable things — execution-level coverage semantics (upstream, fixed),
definition-level evidence sufficiency (analytical), and physical custody (whoever holds delivery). As
stated it fails whenever the definition owner is not the session driver, which is exactly the
promotion case its anti-rework argument relies on (§15).

**Confidence:** high for every executable fact and for the failure of consumer-declared completeness;
medium-high for placing E1–E8 in the runtime contract rather than a new owner; medium for "no current
consumer needs central custody" (bounded absence). Detail in §20.

## 3. Current runtime-contract reconstruction

Repository facts at the baseline (`docs/architecture/runtime-contract.md`, `engine-semantics.md`,
`overview.md`):

1. **Three event concepts are distinct.** Internal scheduler events drive simulation; supported
   observations expose current authoritative state; supported runtime events expose ordered
   authoritative change after it succeeds. Transports only project these.
2. **Not event sourced.** Authoritative state is runtime-owned; "fresh observation at sequence S +
   events after S = current consumer view". Deterministic rerun, checkpoint/restore, durable history
   and transport recovery are separate capabilities.
3. **Post-authoritative publication.** Rejected changes emit nothing; a fault after partial mutation
   reports only changes that occurred; "fault/result reporting remains distinct from state-change
   event publication."
4. **Run identity and epoch.** Each runtime has an opaque `runId`; a reset creating a fresh runtime
   has a new identity and sequence epoch; run identity never affects outcome. Sequence is strictly
   monotonic, independent of time, starts at 1; the no-event observation reports cursor 0.
5. **Provenance.** Every event/observation carries `ModelFingerprint`; `ControlledRevisionId` only when
   authoritatively bound; no Engine-definition identifier is emitted or synthesized.
6. **No retained history.** "If a future consumer requires retained supported events, its ownership,
   retention, and recovery semantics must be defined explicitly. A bounded history is not a durable
   audit ledger, and recovery must detect dropped events rather than silently treating an incomplete
   sequence as complete." Distribution hardening owns retention, resume cursors, reconnect, gap
   detection and checkpoint/restore (also `docs/planning/factory-simulation-engine-readiness.md` §6).
7. **Consumers.** Challenge/Game consume but never define runtime-event semantics or reconstruct
   dispatch from replay; Operational is a sibling, not an owner.
8. **Session control** (Engine semantics §1.2): `advance()` is the one-event primitive;
   `advanceUntil(target, maxEvents)` loops over it; `reset()` is a fresh session over the retained
   model version that leaves the original untouched; `submitWorkload`/`setMachineAvailability` return
   `Accepted`/`Rejected`/`Faulted`.
9. **Distinct histories already named** (Engine-readiness plan §5): internal scheduler events,
   supported runtime events, model revision history, Governance evidence/decision history, challenge
   attempt history and future Operational history.

**What the contract does not state** (bounded search of `runtime-contract.md` and
`engine-semantics.md` for "current time", "monoton", "clock", "non-decreasing", "terminal"):

- that supported-event simulation time is non-decreasing along sequence;
- that supported time advances only with a supported change, so `advanceUntil(target)` does not move
  time to `target`;
- at which simulated time a command applies;
- any coverage vocabulary beyond "detect dropped events";
- that drained delivery is single-consumer;
- that quiescence is not an end, and that reset leaves the original run usable *as an execution*.

## 4. Current implementation evidence

Repository facts from source at the baseline:

- `FactoryRuntime` mints `RunId.create()` (`UUID.randomUUID()`) at construction; `reset()` returns
  `forModel(modelVersion)`. No method ends a runtime.
- `drainSupportedEvents()` returns and clears one shared pending list; `eventSequence` advances in
  lockstep with emission, independent of draining.
- `observe()` reports `observedTime`, the time of the last *emitted* supported event, not the
  scheduler cursor; `RuntimeRunState` is `ACTIVE`/`QUIESCENT` by pending *authoritative* work, and its
  Javadoc says quiescence "deliberately covers both a fresh runtime and one whose submitted work has
  drained rather than inventing a terminal state".
- `Scheduler.currentTime` moves only in `nextEvent()`; commands use `scheduler.currentTime()`.
  `FactoryHandler` schedules an internal start marker together with the completion at the
  completion time for cascade dispatches, so processing that marker alone moves the internal clock
  without any supported change.
- `setMachineAvailability`: an accepted no-op emits nothing; `Faulted` emits only
  `MACHINE_AVAILABILITY_CHANGED` and genuinely occurred dispatches. Rejections never reach the
  supported stream.
- The supported taxonomy is exactly `ORDER_ACCEPTED`, `JOB_DISPATCHED`, `JOB_WAITING`,
  `JOB_STEP_COMPLETED`, `ORDER_COMPLETED`, `MACHINE_AVAILABILITY_CHANGED`.
- Challenge: `ChallengeAttempt` keeps opaque `EvaluationProvenance(publishedModelReference,
  runReference)` and `AuthoritativeOutcomeFacts(contractCompleted, completionTick)`;
  `ChallengeAttemptComparator` checks challenge and evaluation-policy identity only, and compares
  outcome facts, not event histories.
- Research substrate: `ExperimentRunner` records command outcomes, labeled observations and retained
  events; `EvidenceWindow` states coverage `1..runFinalSequence` against a closing observation and
  missing ranges; `DeclaredEvidence.completeEvents` refuses gapped ranges. It is research-local and
  already embodies part of E5 — but only for whole-prefix windows, with no basis-at-`a` or interval
  closure rule (`ProcessingIntervals` measures `[0, boundary]` only).
- Acceptance evidence already pinning parts of E1–E7: `RuntimeEventDeliveryAcceptanceTest`
  (`sameTimeEventsRemainOrderedBySequence`, `resetCreatesNewRunAndSequenceEpoch`,
  `faultReportsOnlyAuthoritativeChangesThatActuallyOccurred`, `acceptedNoOpAvailabilityRequestEmitsNothing`),
  `HeadlessClosureAcceptanceTest` (`freshObservationReconstructsCurrentConsumerViewWithoutReplay`,
  observation/event closure), `SessionControlAcceptanceTest` (reset, `advanceUntil`, `Faulted`).
  None pins time monotonicity along sequence, clock non-advance under `advanceUntil`, command
  application time, single-drainer delivery or interval closure.

## 5. Exact prior workspace evidence consumed

| Artifact | Commit | Path | Use |
|---|---|---|---|
| Game diagnostic report r3 | `4ad00136441fa29e6b462ac5431b60cf2de1e148` | `workspace/research/investigations/factory-design-game-diagnostic-evidence-report.md` | Concrete downstream reasoning needs; read before reconstruction |
| Game independent review (ACCEPT WITH QUALIFICATIONS) | `6484795ff4c4ad971e06c70751b03805e54a55f5` | `workspace/research/investigations/factory-design-game-diagnostic-evidence-adversarial-review.md` | Binding qualifications (probe 1 → case 7; per-step timing available; custody of recorded history open) |
| Analytics Phase 1 | `7e72a46506bf63febcb57d7b7dcc9ce0cd484d64` | `workspace/research/investigations/simulation-analytics-ownership-phase-1-current-boundary.md` | Inherited current-boundary classification; read after reconstruction |
| Analytics Phase 2 (original) | `6a5c043563e3bd89fae6826f830b377b43643d46` | `workspace/research/investigations/simulation-analytics-ownership-phase-2-admission-boundary.md` | Candidate evidence; read after reconstruction |
| Phase 2 independent review (REOPEN) | `2c85fe7c03c6256c9e8a2ce73fde5091b3357e3c` | `workspace/research/investigations/simulation-analytics-ownership-phase-2-adversarial-review.md` | Warning: information-loss observations are not ownership theorems |
| Paused Phase 2 revision | `75736dddfa3fe1ae89c5089720c702f4ca595ab5` | `workspace/research/investigations/simulation-analytics-ownership-phase-2-admission-boundary-revision.md` | Candidate analysis; Rule 7 tested in §15 |

Each was extracted at its exact commit with `git show <commit>:<path>` and read in full or in every
section bearing on custody, completeness, late join and provenance.

**Phase 1 recheck.** Its classification is not contradicted at this baseline: no runtime source or
contract changed between its baseline and `79b3399`. Two Phase 1 findings corroborate this
investigation independently: the `throughputPerTick` window is "the runtime contract's observation
clock, not the time a consumer advanced to" (the same clock fact as E6), and history-carrying
accumulators (`busyTicks`, `completedSalesValue`) are not recoverable by a late joiner (the same fact
as E5's basis rule).

## 6. Independent reconstruction before reading the paused Phase 2

**Order followed.** Brief → Charter → runtime/Engine architecture and implementation → Governance and
Operational boundaries → accepted game report and review → **pre-registration committed** → executable
experiment committed → only then the analytics reports.

- Pre-registration: commit `6c1640b60297c1044c860a66752e5cca82969e80`,
  `workspace/research/investigations/simulation-execution-account-independent-reconstruction.md`.
  It records constraints, hypotheses (H-referent, H-identity, H-end, H-interval, H-ordering, H-basis,
  H-custody, H-owner), predicted candidate outcomes, and hand-derived expected event streams for every
  probe.
- Experiment: commit `1fe4d43acb8ac902e8368a42aa4eeb58eedfb52c` (§24). Every pre-registered value held
  on first execution; only compile fixes were needed.
- **Exposure disclosure.** The handoff itself named the paused revision's default and its Rule 7, so
  the reconstruction was blind to those reports' reasoning, not to that framing.
- **Pre-registration imprecision (disclosed).** For Model B the note said every tick of `[0, 16)` has
  an idle slot "while eligible work waits". Over `[12, 16)` no work waits. The experiment asserts the
  precise statement (queued work 3, 2, 1, 0 after each change), which the run confirmed.

**What reading the analytics material changed.** Nothing in the conclusion; it sharpened §15. The
REOPEN review's warning — "information sufficiency constrains an evidence contract; it does not alone
choose its semantic owner" — is answered here by a different discriminator than information loss:
whether a rule admits a legitimate alternative (§7, §21 challenge 1).

## 7. Candidate models

| | Shape | Owner of coverage meaning | Custody |
|---|---|---|---|
| **C1** Current boundary + caller capture (literal) | Consumers capture what they need and declare their own complete/partial windows | each consumer | each consumer |
| **C2** Bounded execution-account semantics, representation deferred | Arcogine defines identity, basis, ordered inputs/changes, completeness and live/terminal status of one execution as a semantic responsibility distinct from Engine and analytics | a new consumer-neutral execution-account owner | open |
| **C3** Retained execution-record capability | A shared owner retains supported execution evidence for consumers | the record owner | central |
| **C4** Analysis/inspection-owned evidence session | An analysis capability captures a run and supplies completeness/provenance | analytics | analytics |
| **C5** Hybrid (surviving) | One cross-consumer coverage contract (E1–E8) **in the existing runtime contract**; capture held by whoever holds delivery; analytical definitions state only their sufficiency | Engine-owned runtime contract | capture holder |

C5 changes semantic decisions relative to every other candidate: against C1, coverage meaning is
common and upstream; against C2, there is no new owner, account identity or terminal status; against
C3, no central custody; against C4, analytics owns neither coverage nor custody. It is not a hybrid
"to avoid choosing".

## 8. Semantic-dimension matrix

| Dimension | Finding (evidence) | C1 | C2 | C3 | C4 | C5 |
|---|---|---|---|---|---|---|
| Referent | Runtime epoch; accounts are evidence about it (cases 1, 2, 13) | implicit | separate account referent | record as referent | session as referent | E1 |
| Identity | `RunId` suffices; facts are `(RunId, sequence)` (cases 4, 9) | ✓ | adds account id unnecessarily | adds record id | adds session id | E1 |
| Boundary | Start = establishment; reset = new execution, original usable; no terminal (cases 1, 4, 6b) | inconsistent | "terminal status" has no Engine referent | same | same | E2 |
| Basis | Fingerprint on every fact; revision by custody; command outcomes controller-held; reproduction needs script (cases 10, 12) | inconsistent | ✓ | partial (no controller facts) | partial | E3 |
| Record semantics | Effective accepted commands are supported changes; rejected/no-op/fault results are not (§4, case 10) | inconsistent | ✓ | ✓ | ✓ | E3 |
| Ordering | Sequence semantic; time non-decreasing (case 5) | ✓ if obeyed | ✓ | ✓ | ✓ | E4 |
| Completeness | Basis/frontier/gap arithmetic; tail loss needs frontier (cases 2, 3, 13) | **fails** | ✓ | ✓ | ✓ for analytics only | E5 |
| Interval closure | Supported time or controller closure, not advancement (cases 6, 6b) | **fails** | ✓ if stated | ✓ if stated | ✓ if stated | E6 |
| Live/completed | Same account frozen; determinacy only grows (cases 6, 6b) | ✓ | ✓ | ✓ | ✓ | E5/E6 |
| Retention | Not needed centrally now (§13) | ✓ | open | **unjustified** | ✓ | capture holder |
| Authority | Account never current state (cases 1, 2) | ✓ | ✓ | risk of second authority | ✓ | E7 |
| Delivery | Single drainer (case 13) | silent partials | — | ✓ | — | E8 |
| Downstream reasoning | Coverage fixed; definitions differ (case 8) | ✓ | ✓ | ✓ | **mixes the two** | sufficiency only |
| Cross-domain | Distinct from Governance and Operational (§14) | ✓ | risk of new identity | risk of Storage/Governance overlap | ✓ | ✓ |

## 9. Proving-case results

All cases executed in `ExecutionAccountProvingCasesTest` on OpenJDK 21.0.12 (`gradle:9-jdk21`
image, repository wrapper), against a `git archive` export of the baseline's `product/` tree, using
the documented `-PresearchExperimentSources` mechanism. **14 tests, 0 failures.** The whole
`:research-experiments:test` suite with these sources: **151 tests, 0 failures, 0 errors, 0 skipped**
(137 tracked, 14 experiment), including `ResearchPackageBoundaryTest`, which now also holds the
experiment package to the supported runtime contract.

Models: **A** `CUT 3 → ASSEMBLE 5`, Cutter (concurrency 1), Assembler (1), two units at `t = 0`;
**A'** A plus Assembler 2; **B** `BAKE 4` on a concurrency-2 Oven; **C** `PROCESS 5` on one machine;
**F** two independent routes, one with a `Long.MAX_VALUE` step.

### Case 1 — Full-run capture from establishment

- Establishment observation: cursor 0, time 0, `QUIESCENT`, no orders/jobs, all resources idle.
- Model A capture: sequences 1..12 exactly as pre-registered; every event carries the run's `RunId`
  and the published fingerprint; final observation cursor 12, time 13, `QUIESCENT`; folding the 12
  events onto the establishment observation reproduces the final placement exactly.
- **Not terminal:** a later submit in the same run yields sequence 13 at time 13 under the same `RunId`.
- **Establishes:** one run (one `RunId`), no missing sequence (`1..cursor` contiguous against a
  frontier observation), final-observation agreement, model basis. "Terminally complete" does not
  exist; "complete from establishment through cursor N" does.

### Case 2 — Late join

- Join after cursor 6 (time 3). Join observation + events 7..12 reproduce the final view (current
  state is sufficient without history).
- The late account cannot claim the run: coverage from establishment is false; `[0, 13)` is refused
  ("no state basis at 0").
- `[3, 13)` is licensed: Assembler 10 and Cutter 3 active-job-ticks, identical from the late and full
  accounts. The explicit basis is the join observation.
- A late joiner that pairs dispatches and completions it happens to hold reads the Cutter as never
  used (0), against a true 6 over `[0, 13)`.

### Case 3 — Explicit gap

- Dropping sequence 7 is detected as missing `7..7`; every claim spanning it is refused.
- A gap-blind pairing reads Cutter occupancy 13 against a true 6.

### Case 4 — Reset / fresh run

- New `RunId`, cursor 0, time 0, no orders or jobs, same fingerprint and model version.
- The same commands reproduce the same stream once run identity is normalized.
- The original run's account refuses the new run's events (no silent merge).
- **The original run is not ended:** it accepts more work and continues at sequence 13 under its own
  `RunId` while the new run stays at 12. Old and new runs have no execution-account relation; any
  relation is a consumer-owned comparison.

### Case 5 — Equal simulation time

- Model A has equal-time groups of sizes 3, 3, 2, 2, 2 (times 0, 3, 6, 8, 13); simulation time never
  decreases along sequence.
- Reversing only the two changes at `t = 8` (completion of J1, then dispatch of J2 onto the Assembler)
  puts both jobs on the concurrency-1 Assembler — an impossible state. Sequence order never does.

### Case 6 — Running work crossing an interval boundary

- Window `[4, 7)`; J1 runs on the Assembler over `[3, 8)`, starting before and ending after it.
- The window account starts at the cursor-6 observation (time 3), which shows J1 active. It holds
  neither J1's dispatch (sequence 5) nor uses J1's completion (sequence 9, time 8). Assembler
  active-job-ticks over `[4, 7)` = 3.
- **Evidence needed:** a state basis at or before `a` plus closure at `b`. Neither the prior start
  event nor the later completion event is needed for this interval; those are needed only by other
  questions (for example "how long has this step run", a timeline that starts before `a`).
- After `advanceUntil(7)`, supported time is 6, not 7, and `[4, 7)` is correctly **not** yet
  determined: every internal event up to 7 was processed, yet a command issued now would apply at 6,
  inside the window. It becomes determined when supported time reaches 8.
- Completion-credited `busyTicks` deltas over the same window read 0 (separate from any utilization
  formula).

### Case 6b — Closure needs supported time or a commitment (the closure trap)

- After `advanceUntil(100)` the run is `QUIESCENT` at time 13. `[0, 13)` is determined (Assembler 10);
  `[0, 100)` is refused.
- The driver then submits one unit: it is accepted at **time 13**. After the driver commits to nothing
  further, `[0, 100)` is Assembler **15**, not 10.
- A premature closure through 100, followed by a command, is detected as a contradiction.

### Case 7 — Availability change and idle slot

- Oven offline, four units submitted, Oven online: `Busy`, 1 of 2 active, own queue depth 3, no
  pending multi-eligible work; 16 events as pre-registered; completion 16 (8 when always online).
- After each time's last change: (active, queued) = (1, 3), (1, 2), (1, 1), (1, 0), (0, 0) at
  times 0, 4, 8, 12, 16. The account preserves "one idle slot beside queued eligible work" over
  `[0, 12)` and "one idle slot, nothing waiting" over `[12, 16)` as facts.
- It encodes no characterization: "starved", "no work left" or a third category is a definition laid
  over these facts, which the Engine's two-way classification cannot represent (game review probe 1).

### Case 8 — Two legitimate definitions over one unchanged account

- Model B run through the existing substrate (`ExperimentRunner.runAndReplay`, complete window).
- Slot utilization over `[0, 16)`: 16 / 32. Machine busy fraction (at least one job): 16 / 16. The
  substrate's `processing-occupancy-of-continuously-online-resources` **refuses** ("changed
  availability"). The account's events and basis are byte-for-byte unchanged before and after.
- **Discriminator:** analytical semantics are not execution semantics. Execution coverage is the same
  under every definition; sufficiency differs per definition.

### Case 9 — Controlled comparison of two runs

- A and A' complete accounts: distinct `RunId`s, both from sequence 1; completion 13 vs 11; 12 vs 11
  events. Sequence 8 is `JOB_WAITING` in A and `JOB_DISPATCHED` to Assembler 2 in A', so a sequence
  number names different facts in different runs: identity is `(RunId, sequence)`.
- The authored change comes from `FactoryModelSemanticComparator`: resource 3 added, and the routing
  step changed. No stream is merged, and no super-run exists. Each account keeps its own coverage and
  basis.

### Case 10 — Faulted command

- Reproduced through the supported API alone (`Long.MAX_VALUE` step, availability toggle).
- The controller receives `Faulted:EventOrderingViolation`. The supported stream ends with
  `MACHINE_AVAILABILITY_CHANGED` and `JOB_DISPATCHED` at `Long.MAX_VALUE`; the account is gap-free.
- Afterwards the run is `QUIESCENT` while job A is `InProgress` on a `Busy` M1 indefinitely. The
  supported taxonomy has no fault member and the observation no fault field, so **a non-controlling
  account looks complete and cannot know a fault occurred**.
- The fault is deterministic: two runs give the same outcome and the same normalized stream.
- **Account representation:** the request is controller-held; the fault result is controller-held;
  the partial authoritative changes are supported facts; "fault may follow partial mutation" is a
  property of the result, not a rollback. No transactional semantics are invented.

### Case 11 — Spatial compatibility

- A valid published model with a present spatial record is refused (`UnsupportedModelContentException`)
  before any runtime exists: no `RunId`, no account. The execution referent begins at establishment.
- Analysis of the specified transfer semantics (Engine semantics §6–§12, the spatial plan's transfer-activation and observation/late-join slices):
  `TRANSFER_STARTED`/`TRANSFER_COMPLETED` become further supported changes in the same sequence; zero
  duration produces an equal-time chain that E4 already orders; a fresh mid-transfer observation is
  required to expose in-flight state (`transferStartedAt`, `transferCompletesAt`, admission load), so
  E6's basis rule holds unchanged; `JOB_DISPATCHED` keeps meaning processing start after arrival.
  **No E-rule needs restructuring**; only the set of supported facts grows. Anti-rework test passes.

### Case 12 (added) — Reproduction granularity

- Script A stops after the internal no-op start marker at 10 (`advanceUntil(10, 1)`), then submits.
  The observation still says **time 5**, yet the order is accepted at **10**, before the running job
  completes at 10, so the new unit waits and is dispatched by the completion cascade.
- Script B advances through 10, then submits: accepted after the completion, dispatched at once.
- Both are legitimate, distinct executions with different supported histories (12 vs 11 events).
- **Re-driving A's accepted commands at their recorded times through supported control reproduces B,
  not A.** Supported history records effects; it is not a reproduction script.

### Case 13 (added; the brief's "two presentations") — Drainers and fan-out

- Two drainers of one live run receive disjoint sequences (1..6 and 7..12); the union is complete.
- The first drainer's capture looks gap-free up to its own frontier and cannot see what it will miss;
  against the final frontier it is missing 7..12. The second is missing 1..6.
- One capture fanned out to a CLI-style list and a web-style summary gives both the same identity,
  order and coverage by construction. This proves representation plurality, not two semantic
  consumers.

### Candidate × case summary

| Case | C1 literal | C2 | C3 | C4 | C5 |
|---|---|---|---|---|---|
| 1 full capture | ✓ | ✓ (but "terminal" undefined) | ✓ | ✓ | ✓ |
| 2 late join | **fails** if consumer-declared | ✓ | ✓ (only with retention) | ✓ | ✓ |
| 3 gap | **fails** if blind | ✓ | ✓ | ✓ | ✓ |
| 4 reset | inconsistent | ✓ | ✓ | ✓ | ✓ |
| 5 equal time | ✓ if obeyed | ✓ | ✓ | ✓ | ✓ |
| 6/6b closure | **fails** | ✓ if rule stated | ✓ if rule stated | analytics-only | ✓ |
| 7 idle slot | ✓ | ✓ | ✓ | **risk** of encoding characterization | ✓ |
| 8 two definitions | ✓ | ✓ | ✓ | **risk**: definitions shape the session | ✓ |
| 9 comparison | ✓ | ✓ | ✓ | ✓ | ✓ |
| 10 fault | controller-only | needs controller record | same | same | E3 |
| 12 reproduction | needs script | same | same | same | E3 |
| 13 drainers | **fails silently** | ✓ | ✓ (custody) | too narrow | E5/E8 |
| owner placement | — | new owner unjustified (§21.1) | custody unjustified (§13) | ownership inversion | ✓ |

## 10. Identity and boundary conclusion

- **Referent.** Beyond a live `FactoryRuntime` and its projections, the semantic thing is the runtime
  epoch plus its ordered supported changes. An account is evidence of it. No separate execution entity
  is needed (Inference from cases 1, 2, 13).
- **Identity.** `RunId` identifies exactly one runtime epoch, opaque, never result-affecting, fresh
  per construction or reset. It does not identify a model+command basis (reruns differ), an account,
  or a result. A time-varying result needs `(RunId, coverage)` — consistent with Governance evidence
  §5 ("a run identifier alone does not identify a time-varying result"). **`RunId` is sufficient for
  the execution referent**; it is not a durable accepted identity and needs none for current uses.
- **Boundary.** Establishment starts an execution; reset starts another and ends neither; there is no
  Engine end. Terminal completion is meaningful only as a declaration by the session driver (for
  example a Challenge attempt closing its run at quiescence or a deadline); its meaning (E6) is
  common, the act is the driver's.

## 11. Ordering and completeness conclusion

- **Ordering (E4).** Sequence is semantic and total within a run; simulation time is a domain
  coordinate, non-decreasing along sequence (executed; not yet stated by the contract). Command
  acceptance order is visible only for effective commands, as their position in sequence.
- **Completeness (E5–E6).** The consumer-neutral statuses are:
  - complete from establishment through cursor N (basis cursor 0, no gap, frontier N);
  - complete over `(S, N]` from a basis observation at S (late join);
  - complete over simulation interval `[a, b)` (basis time `<= a`, no gap, supported time `>= b` or
    closure covering `b`);
  - gap detected (named missing ranges);
  - unknown (no basis, or no frontier — tail loss cannot be excluded).
- **Execution completeness is not analytical sufficiency** (case 8): a complete account may still be
  refused by a definition (the continuously-online occupancy refuses an availability change), and a
  partial account can fully serve a bounded interval (case 2).

## 12. Live versus completed conclusion

One semantic execution can be consumed incrementally and later as a completed account without
changing identity or coordinates: the completed account is the live capture frozen at a frontier.
Determinacy is monotone: an interval determined while live stays determined (case 6); an open
interval must be presented as provisional until supported time or closure covers it (cases 6, 6b);
a premature closure is detectable rather than silently rewritten (case 6b). A completed account handed
to another party keeps `(RunId, sequence)` coordinates and its coverage statement, so its guarantees
do not change with custody.

## 13. Retention and custody conclusion

Separating the three layers the handoff requires:

1. **Semantic ownership** of what coverage means: the runtime contract (E1–E8).
2. **Custody/retention responsibility:** the party holding the runtime's delivery — the session driver
   or its delegate — because drained delivery is single-consumer (case 13). It retains what its uses
   need for their horizon. This is **not** necessarily an analytical-definition owner: a stateless
   definition cannot drain a runtime it does not drive.
3. **Physical persistence:** unspecified and unnecessary now.

**Is central custody required now? No.** Concrete current uses:

| Use | Who drives | Custody need | Central custody needed? |
|---|---|---|---|
| Game static recorded runs (game r3 §1) | generator | complete account per run | No |
| Research experiments | runner | complete or declared-partial windows | No |
| Challenge evaluation | evaluator | outcome facts only | No |
| Governance evaluation of an analytical result | producer | producer keeps the account its result provenance cites | No (producer custody) |
| Two presentations of one consumer (CLI/web) | one driver | one capture, fanned out | No |

**Triggers that would make custody shared or central** (none present): several *independent* live
consumers of one execution; a late joiner needing pre-join history; a result consumed and re-verified
by a party that neither drove nor captured the run beyond the producer's retention horizon. The first
two map onto the runtime contract's anticipated retained, cursor-addressable delivery with truncation
detection (distribution hardening) — an Engine/runtime-contract extension, not Engine event sourcing,
not Governance history, not Storage authority. The third is producer custody plus a retention promise
under semantic-contract support rules.

## 14. Engine, Governance, Operational and product boundary analysis

### 14.1 Authority (handoff distinction A)

Engine/runtime domains own current authoritative state (Repository fact). The account is never needed
to reconstruct it (case 2: a fresh observation suffices) and never consulted by the runtime (E7).

### 14.2 Internal versus supported events (B) and request/result/transition/observation (C)

Only supported events enter an account. Membership:

| Fact | Account membership |
|---|---|
| Caller request | Controller-held; not a supported fact |
| Accepted/rejected/faulted result | Controller-held; related record, not a supported fact |
| Accepted authoritative transition | Represented by the supported change(s) it caused |
| Supported runtime event | Member |
| Supported observation | Basis or frontier evidence |
| Analytical or consumer conclusion | Never a member; derived elsewhere |
| Internal scheduler event | Excluded |

Missing supported facts are recorded explicitly (§19), not filled from internals.

### 14.3 Governance

An account is neither a governed occurrence (no acceptance authority, no commit boundary), nor
controlled history, nor Governance evidence by itself. It is producer-side material. When a result
derived from it is used as Governance evidence, the producer's intrinsic provenance (Governance
evidence §5) carries `RunId` plus the coverage statement (basis, range, frontier or closure), the model
fingerprint (and controlled revision when bound), the analytical definition, and the still-missing
exact Engine definition; Governance owns use, applicability and outcome. Nothing here moves controlled
revision, evidence use, conformance or governed change out of Governance.

### 14.4 Operational continuity

| Dimension | Simulation execution | Accountable operational continuation |
|---|---|---|
| Identity attaches to | the runtime epoch; accounts are evidence about it | the accepted account itself |
| Establishment | runtime construction (correlation, no acceptance) | explicit genesis/fork accepted into durable history; never inferred from runtime |
| Lifetime | open-ended while the runtime lives; no Engine end; driver ends the account | independently continuing; closure deliberately open |
| Authority | deterministic simulated state, Engine-owned | Arcogine's accountable conduct amid external authority |
| Raw external observations | none | independent facts, never carrying identity at ingestion |
| Loss | lost capture = partial account; the execution is unaffected | silent loss = fork; declared loss = fork with a recorded gap |
| Reset | new execution; original untouched | not equivalent; stale restore = new identity with lineage |
| Fork/lineage | none today; a rerun is a new, unrelated execution | mandatory lineage with a divergence boundary |
| Durability | none promised | load-bearing |
| Replay | deterministic rerun from basis and driver script | the physical world does not replay |
| Ordering | total, by sequence | set-based, not sequence-based |

**Transferable pattern only:** loss must be detectable or declared, never silent. No shared type,
identity or lifecycle follows; simulation `RunId` stays distinct from operational continuation
identity (Operational continuity §2, §14 "whether an inspection or analysis session needs its own
durable identity" remains open and is not answered by this report).

### 14.5 Exact Engine-definition provenance

No proving case needs it: every run here, and in the game and analytics evidence, is produced under one
repository revision whose custody records it. **The first claim that would become untruthful without
it:** a claim that spans a retained account produced under one revision and a run under another —
for example the game's pre-computed recorded runs shipped as data and compared with a variant run
by a later build, attributing the difference to the authored change; or a claim that a retained
account is reproducible by rerunning its basis. Leave it unadmitted; the trigger coincides with the
cross-revision comparability question's.

### 14.6 Charter and representation

- Charter lifecycle modes legitimately imply several purpose-built consumers of executions. What
  follows semantically is decision test 5 — views must not invent competing truths — hence common
  coverage rules. It does not follow that one application, transport, database, module, retention
  policy or analytical definition exists.
- "Reality is explicit": a rerun is a different execution with equal outcomes; E1 keeps them distinct.
- CLI/web presentations of one Challenge consumer are representation plurality (case 13): one capture,
  two views. They are not evidence of cross-domain semantic reuse.
- Industry notions (utilization, occupancy, throughput, lead time, WIP, waiting, bottleneck) are
  legitimate concept families; each named definition still needs its basis (case 8). No external
  standard is imported as Arcogine's execution ontology.

## 15. Consequence for simulation analytics

### 15.1 Classification

| Concern | Classification |
|---|---|
| `RunId` | Engine execution semantics (epoch correlation) |
| Model fingerprint | Engine execution semantics as provenance projection; meaning Factory-owned |
| Engine definition identity | Not admitted. If triggered: Engine-owned basis; until then revision is held by custody |
| Supported event sequence | Engine execution semantics |
| Observation cursor | Engine execution semantics |
| Full-run completeness | Execution-account semantics (E5), realized in the runtime contract |
| Bounded-interval completeness | Execution-account semantics (E6) |
| Late-join state | Execution-account semantics (E5 basis rule) |
| Retained event custody | Meaning: execution-account semantics. Holding: the capture holder (consumer), not analytics by default |
| Processing intervals (job/step on resource from dispatch to completion) | Execution-account semantics: an unambiguous reading of paired supported changes. Presentation is consumer |
| Waiting intervals (readiness to dispatch, per job and step) | Execution-account semantics as a reading. Grouping waiting by step or resource is analytical |
| Occupancy over a period or grouping | Analytical definition/provenance |
| Utilization | Analytical definition/provenance |
| Starvation / idle-flow characterization | Analytical definition (characterization); presentation consumer |
| Bottleneck method identity | Analytical definition/provenance (named method); verdict presentation consumer |
| Cross-run comparison | Consumer interpretation (claims, scoring); change identification Factory/Governance; never a merged account |
| Analytical-definition identity | Analytical definition/provenance |
| Consumer wording/pedagogy | Consumer interpretation/presentation |
| Closure declaration | The driving consumer's act; meaning execution-account semantics |
| Command outcome records | Controller-held; part of producer provenance |
| Evidence applicability/use | Governance evidence/use |
| Accountable continuation, external observations | Operational-only |

### 15.2 What the paused Phase 2 revision must reconsider

1. **Rule 7 ("evidence custody follows the definition owner") fails as stated.** Split it into:
   - coverage meaning is execution-level and governed by the runtime contract (E1–E8);
   - each definition states only its *sufficiency* in that vocabulary (which basis, interval and
     closure it needs) and refuses otherwise;
   - physical custody sits with the party holding delivery, which supplies accounts to definition
     owners.

   Rule 7 holds only when the definition owner also drives the session (the consumer-local default),
   and fails exactly at its own Rule 4 promotion, where a shared definition owner does not drive the
   runtime (case 13).
2. **§6.2 "Evidence retention / completeness" row** sits below the "choices execution does not fix"
   line. Split it: coverage/closure/gap semantics are fixed by execution (cases 3, 5, 6b, 13);
   definition-specific evidence needs are choices.
3. **Rule 5's "evidence-completeness condition"** must be expressed in the common E5–E6 vocabulary and
   may not redefine coverage (for example treat an advancement target as closure, or quiescence as an
   end). With that change its rework-free promotion claim becomes stronger: promotion moves a
   definition, not its evidence semantics.
4. **§16/§17 anti-rework argument** ("promotion moves custody once") must change its mechanism: only
   definition custody moves; evidence custody stays with the capture holder.
5. **§15.3 evidence/provenance trigger (2)** ("a result must be reconstructable by a party that did not
   capture its evidence") stays valid; the handed-over account's coverage meaning now comes from the
   runtime contract, so the remaining obligation is analytical (definition identity, result
   provenance).
6. **Late-join rows (A′ trigger).** Unchanged as Engine-side aggregates, with one addition: pre-join
   *interval* history is unobtainable without someone's retention; A′ serves run-to-date aggregates
   only.
7. **Utilization over a period** must use E6 for period closure; a live loop must mark open periods
   provisional (case 6b).
8. **Occupancy/timeline decomposition** (§6.2/§16): the per-job/step interval reading is
   execution-level; periods and groupings remain definition-level.
9. **Idle-flow characterization and its third state** are unaffected; the account preserves the facts
   (case 7).

The ownership decision itself (staged admission, Engine admission triggers, shared-owner triggers) is
not decided here.

## 16. Consequence for analytics evidence/provenance

| Concern in that brief | Destination after this investigation |
|---|---|
| Run/reset basis | **Upstream** (E1, E2) |
| Event ordering, equal time | **Upstream** (E4) |
| Complete/partial evidence-window semantics | **Upstream** (E5, E6) |
| Late join | **Upstream** (E5 basis rule) |
| Retained-event custody | Meaning upstream; holding by the capture holder; analytics only when it is that holder |
| Accumulator ownership (incremental vs batch equivalence) | **Remains analytical** |
| Analytical-definition identity | **Remains analytical** |
| Result provenance | Analytical, composed from execution-level parts: `RunId` + coverage statement + fingerprint (+ controlled revision) + definition + controller outcomes; missing Engine definition stays missing |
| Reproduction/compatibility | Reproduction needs the driver's script, the model and the producing revision (E3, case 12); cross-revision compatibility stays with its own question |

**Outcome:** the execution account absorbs the execution-level concerns and narrows the analytics
question to definition identity, sufficiency, accumulation and result provenance. Its promotion
trigger stands; a prerequisite is added — reconciled coverage rules it can reference.

## 17. Architecture/support changes required if accepted

None is performed here. All need independent adversarial review first, then independent PR review.

1. **Runtime observation/event contract** — add a section on execution evidence and coverage stating
   E1–E8 as consumer-neutral rules (no type, store or API), plus explicit guarantees of current
   behavior: supported-event time non-decreasing along sequence; supported time advances only with a
   supported change; no terminal session state (quiescence is not an end); reset leaves the original
   run usable; drained delivery is single-consumer; rejected/no-op/fault outcomes are not supported
   facts. **Category:** additive clarification of current behavior plus a new normative consumer rule
   set. No produced value changes.
2. **Engine semantics §1.2** — record, as current interpretation, at which time a command applies (the
   runtime's internal current time, which the latest observation may not show) and that `advanceUntil`
   does not move simulated time. **Category:** recording a previously unwritten result-affecting rule
   (§1.1 consequence 3: a correction, not a behavior change), with conformance fixtures from cases 6,
   6b and 12.
3. **Determinism Contract** (optional clarification) — "ordered external commands" means the driver's
   script at session-control granularity, interleaving included; supported history alone does not
   determine it (case 12).
4. **No change** to Governance evidence, Operational continuity, Storage, Factory, Challenge, the
   planning documents, or the Engine definition's behavior.

## 18. External evidence

External evidence shows possibility and vocabulary only; the conclusion rests on executed repository
evidence.

- **Apache Kafka 4.3.X documentation, consumer configuration `auto.offset.reset`** — verified in
  session (2026-10-08) at `kafka.apache.org/43/configuration/consumer-configs/`. The setting decides
  "what to do when there is no initial offset in Kafka or if the current offset does not exist any
  more on the server (e.g. because that data has been deleted)", with `earliest`/`latest`/
  `by_duration`/`none` (exception). **Transfers:** in a mature multi-consumer log, offsets are
  assigned by the log, each consumer holds its own position, retention can truncate, and truncation is
  surfaced to the consumer rather than hidden — the shape of E5/E8 and of the runtime contract's
  anticipated retained delivery. **Breaks:** Kafka is a durable shared log by design; Arcogine's
  current delivery drains to one in-process caller, and nothing here makes central retention necessary.
- **OpenTelemetry Trace SDK specification (Stable), Span Limits** — verified in session at
  `opentelemetry.io/docs/specs/otel/trace/sdk/`: counts of attributes, events and links dropped due to
  limits "MUST be" available to exporters. **Transfers:** the producer declares loss so consumers do
  not mistake partial for complete. **Breaks:** telemetry is lossy and sampled by design.
- **OpenTelemetry Trace API specification (Stable)** — verified in session at
  `opentelemetry.io/docs/specs/otel/trace/api/`: each span is ended separately, and ending a parent
  "MUST NOT have any effects on child spans". No trace-completion marker is defined (bounded reading
  of that page). **Transfers:** completeness of an open-ended record is not intrinsic; closure needs a
  declaration or policy — the shape of E2/E6. **Breaks:** distributed traces lack a total order.
- **IEEE 1849 (XES) event-log standard** — partially verified: the IEEE record shows 1849-2016
  superseded by a 2023 revision; the normative lifecycle clause text was **not** verified (only a
  third-party implementation listing `start`/`complete` transitions). Used only as background for
  "one log, many analyses"; it carries no conclusion.

No source was used to choose an owner. Absence claims about these systems are limited to the pages
read.

## 19. Unresolved unknowns and reopening triggers

| Unknown / gap | Trigger | Where it would be tracked |
|---|---|---|
| Fault invisible to non-controlling consumers (case 10) | A non-controlling consumer of runs that can fault, or a Governance use of results whose producer did not drive the run | Runtime contract reopening note; register row |
| Advanced-through horizon not representable (cases 6, 6b) | A passive consumer needing interval claims ending after the last supported change (for example deadline intervals in a live view) | Same |
| Command applies at internal time later than observed time (case 12) | A live interactive loop where a player acts "at" a time; any supported-only re-driver | Engine semantics §1.2 (clarification now); Engine evolution research if behavior should change |
| Shared/central custody | Independent live consumers of one execution; late joiner needing pre-join history; long-horizon third-party verification | Runtime contract distribution hardening; semantic-contract support |
| Exact Engine-definition identity | Retained accounts or results compared or reproduced across development revisions | Cross-revision comparability and exact-reference questions |
| Account handover format and integrity | First cross-party handover of an account | Analytics evidence/provenance question |
| Whether an inspection/analysis session needs durable identity (Operational continuity §14) | A consumer needing to cite the session itself, not the run | Operational boundary research |
| Spatial execution | Transfer execution lands | Recheck case 11 analysis executably |

## 20. Confidence by conclusion class

| Conclusion | Confidence | Basis |
|---|---|---|
| Executable facts (clock, drain, reset, fault, ordering, closure trap, reproduction granularity) | High | 14 pre-registered cases, all matched on first run, JDK 21 |
| Consumer-declared completeness (C1 literal) fails | High | Cases 2, 3, 5, 6b, 13 |
| E1–E8 are entailed by Engine facts, with no legitimate alternative | High for E1, E4, E5, E7, E8; medium-high for E6's closure-declaration form and E3's reproduction clause | Execution plus specification text |
| Owner is the runtime contract, not a new owner | Medium-high | Placement argument (§21.1); no executable discriminator can choose a document |
| No central custody needed now | Medium | Bounded search of current uses; triggers named |
| Missing supported facts are not needed now | Medium | No non-controlling consumer exists |
| Spatial anti-rework | Medium | Specified, not executed |

**What would change the conclusion:** a consumer that cannot obtain the controller's facts (fault,
closure, script) and must still reason about runs; a concrete requirement for several independent live
consumers of one execution; a case where a coverage rule admits two legitimate meanings (that would
move it to definitions); or Engine time semantics changing so that the clock advances to advancement
targets.

## 21. Adversarial self-challenge (self-administered; not independent)

1. **"Information sufficiency does not choose an owner" (the REOPEN warning).** Upheld as a warning;
   answered with a different discriminator. E1–E8 are placed in the runtime contract not because
   information is lost elsewhere but because no consumer can legitimately define them differently: a
   consumer that does is wrong against Engine semantics (cases 3, 5, 6b), unlike case 8 where
   different definitions are all correct. Residual: a reviewer could argue for a separate
   "execution-evidence contract" document owned by Engine. That is a document-placement choice with
   the same owner and is not a different semantic decision.
2. **"This is C2 under another name."** Partly upheld. The semantic content is C2's minimum. What
   differs is decisive: no new owner, no account identity, no terminal status, no custody. The brief's
   C2 also asked for "terminal/live status"; that is rejected (E2).
3. **"The closure trap is an artifact of a misbehaving driver."** Rejected. The driver did nothing
   unsupported: it advanced to 100 and later submitted. The trap exists because the clock is
   event-driven, which is current Engine behavior. A consumer that is not the driver cannot even know
   the advanced-through horizon.
4. **"Case 12 exploits an internal marker."** Partly upheld. It uses only `advanceUntil(target, 1)`, a
   supported call, but the outcome depends on the internal event structure. That is exactly the point:
   reproduction granularity is internal-event granularity. The finding is narrowed to "supported
   history is not a reproduction script", not "reproduction is impossible".
5. **"The fault case is an extreme overflow, irrelevant."** Partly upheld for frequency; rejected for
   semantics. It is the only current `Faulted` path, and the contract defines `Faulted`. The gap is
   recorded with a trigger, not as a required change.
6. **"No central custody is a bounded-absence claim."** Upheld: §13 lists the searched uses and the
   triggers.
7. **"Shared authorship of probes and expectations."** Mitigated by pre-registration at a committed SHA
   before the run; residual: the same author chose which cases to probe. An independent reviewer
   should try to construct a coverage rule with two legitimate meanings, and a consumer that needs
   central custody now.
8. **"Spatial anti-rework is argued, not executed."** Upheld; confidence medium.
9. **Stale baseline.** `main` did not move.
10. **Synthesis seeds** were not consulted, consistent with `researching.md` §2.

**Transferability (candidate only).** Narrowest surviving signal: *for a producer with a total change
order and an event-driven clock, the coverage semantics of captured history (gap, basis, closure) are
entailed by the producer's ordering and time semantics and belong with the producer's contract;
physical custody follows whoever holds delivery; analytical definitions express only sufficiency.*
Counter-contexts: set-ordered records (Operational continuity), lossy sampled telemetry, durable shared
logs where custody is the product. Evidence level: established for Arcogine's current runtime only;
analogues show possibility.

## 22. Surviving invariants and what did not survive

**Surviving invariants:**

1. An execution is a runtime epoch; an account is evidence about it; facts are `(RunId, sequence)`.
2. Reset starts an execution and ends none; quiescence is not an end; there is no Engine terminal state.
3. Sequence is the semantic order; simulation time never decreases along it.
4. Coverage is stated as basis, retained range and frontier; gaps are arithmetic; tail loss needs a
   frontier.
5. An interval is closed by supported time or a driver's commitment, never by an advancement request.
6. Coverage is fixed by execution; sufficiency is chosen by definitions.
7. Custody follows delivery, not definition ownership.
8. Supported history records effects; reproduction needs the driver's script.

**Did not survive:**

- "Caller capture plus consumer-declared completeness is enough" (C1 literal).
- "An execution account needs its own identity or terminal status" (C2 as framed).
- "A retained execution record is needed now" (C3).
- "Analysis owns the evidence session" (C4).
- "Evidence custody follows the definition owner" (paused revision Rule 7, as stated).
- "`advanceUntil(b)` makes `[a, b)` complete."
- "A gap-free retained range is complete" (without a frontier).
- "Reset ends the old run."
- "The supported event stream is a reproduction script."
- "A complete account suffices for every analytical question."

**Reusable negative knowledge:** the closure trap (case 6b), single-drainer silent partials (case 13),
reproduction granularity (case 12) and fault invisibility (case 10) are each easy to rediscover only
after a wrong claim ships.

## 23. Durable consequences, reusable assets, implementation implication

**Durable consequences (if accepted after independent review):** §17 items 1–3; register update on
reconciliation (this question `CONCLUDED` with destination; analytics ownership resumes Phase 2 with
§15.2; evidence/provenance narrowed per §16); §19 triggers recorded with their destinations.

**Reusable assets:**

- Cases 6b, 12 and 13 and the time-monotonicity check are worth promoting as conformance fixtures for
  the clarified runtime contract and §1.2 (factory acceptance tests). Case 10's fault-invisibility
  assertion belongs with them as a pinned limitation.
- The substrate's `EvidenceWindow` could gain the basis/interval-closure determinacy of
  `CapturedAccount` if research needs bounded-interval claims; otherwise discard `CapturedAccount`,
  `PlacementFold` and `IntervalReadings` explicitly at the knowledge-transfer audit.
- **Historical decision-rationale record: recommended** — high-risk, three serious alternatives (C2,
  C3, C4), a non-obvious owner placement, concrete reopening triggers.
- **Synthesis seed: nominate for consideration** (§21 transferability), with *Revisit when*: a second
  Arcogine producer (for example Operational records or a future analytics stream) faces the same
  coverage-versus-custody split.
- This report need not remain readable after reconciliation.

**Implementation implication:** no implementation. No module, type, API, retention or capability is
admitted. The only code-adjacent consequence is conformance fixtures accompanying the contract
clarifications, at reconciliation.

## 24. Evidence coordinates and rerun

| Artifact | Commit | Path |
|---|---|---|
| Handoff | `5a433fc5de3fea4ff935a659ebc548676b6ee1fa` | `workspace/research/handoffs/simulation-execution-account.md` |
| Pre-registration (independent reconstruction) | `6c1640b60297c1044c860a66752e5cca82969e80` | `workspace/research/investigations/simulation-execution-account-independent-reconstruction.md` |
| Experiment sources | `1fe4d43acb8ac902e8368a42aa4eeb58eedfb52c` | `workspace/research/experiments/simulation-execution-account/com/arcogine/research/executionaccount/` (`CapturedAccount`, `PlacementFold`, `IntervalReadings`, `ExecutionAccountProvingCasesTest`) |
| This report | the commit that adds it | `workspace/research/investigations/simulation-execution-account-report.md` |

**Rerun** (JDK 21 floor, from a `git archive` export of `product/` so no root-owned build output
lands in a worktree):

```text
docker run --rm -v <export>/product:/app \
  -v <workspace>/research/experiments/simulation-execution-account:/experiment:ro \
  -v arcogine_gradle_cache:/root/.gradle -w /app gradle:9-jdk21 \
  sh ./gradlew :research-experiments:test -PresearchExperimentSources=/experiment --no-daemon
```

Expected: 151 tests, 0 failures (137 tracked, 14 experiment); the experiment alone, 14 tests.

**Independent adversarial review is required next**, by a genuinely independent session, bound to this
exact report revision, recording its own live-main baseline, and using the dispositions ACCEPT,
ACCEPT WITH QUALIFICATIONS, MORE EVIDENCE REQUIRED or REOPEN.

## Sources

**Repository, at `79b3399149499f2f337206c909a277f66a5befd5`:** `AGENTS.md`;
`.github/agents/researcher.agent.md`; `docs/development/researching.md`, `testing.md` §10;
`docs/product/charter.md`, `concepts.md`; `docs/architecture/overview.md`, `runtime-contract.md`,
`engine-semantics.md`, `operational-continuity.md`, `operational-execution-digital-twin.md`,
`governance-evidence.md`, `governance-conformance.md` §9–§13, `storage.md`;
`docs/planning/factory-simulation-engine-readiness.md` §5–§6, `spatial-runtime-consequences.md`
(transfer activation through headless closure); `docs/research/research-register.md`; the execution-account, analytics-boundary
and evidence-provenance briefs; `product/domains/factory/src/main/java/com/arcogine/factory/process/`
(`FactoryRuntime`, `FactoryHandler`, `RecordingScheduler`, `CommandResult`, `RuntimeEventEnvelope`,
`RuntimeEventType`, `RuntimeEventPayload`, `RuntimeObservation*`, `RuntimeRunState`);
`product/simulation/src/main/java/com/arcogine/core/queue/Scheduler.java`;
`product/types/src/main/java/com/arcogine/types/RunId.java`; Challenge `attempt/`, `comparison/`,
`evaluation/`; `product/research-experiments/` (main and tests); `SessionControlAcceptanceTest`,
`RuntimeEventDeliveryAcceptanceTest`, `HeadlessClosureAcceptanceTest`,
`FactoryRuntimeExecutabilityAcceptanceTest`.

**Prior workspace evidence:** §5 coordinates.

**External:** Apache Kafka 4.3.X consumer configuration (verified 2026-10-08); OpenTelemetry Trace SDK
and Trace API specifications, Stable (verified 2026-10-08); IEEE 1849 record (edition status verified;
clause text not verified).
