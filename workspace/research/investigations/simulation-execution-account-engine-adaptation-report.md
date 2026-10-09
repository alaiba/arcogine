# Engine adaptation versus controller-managed interval closure: follow-up report

> **Research status:** `READY`. The simulation execution-account question stays admitted and
> unresolved until its independently reviewed durable reconciliation lands. This report changes no
> register entry and does not mark anything `CONCLUDED`.
>
> **Research baseline:** live `main` `912b2ac85cefab3907fc562a3aaa6507e51975a7`, resolved at the start
> of the run (it equals the handoff's preparation baseline).
>
> **Final live-main recheck:** `912b2ac85cefab3907fc562a3aaa6507e51975a7`, re-resolved from the
> remote immediately before this report was committed. `main` did not move during the run.
>
> **Workspace:** `workspace/simulation-execution-account`. Assignment:
> `f7da1edc2f09919b893a231558c878b6042fe276`,
> `workspace/research/handoffs/simulation-execution-account-engine-adaptation.md`.
>
> **Risk:** High. The question concerns Engine session-control meaning, temporal finality,
> determinism/reproduction and cross-consumer evidence ownership.
>
> **Authority:** research evidence only. Nothing here is accepted architecture, an Engine-semantics
> or runtime-contract change, a public API, planning admission or implementation commitment.
> Every proposed Engine operation below is **proposed behavior**, specified but not implemented and
> not executed.
>
> **Adversarial-review status:** required, **not yet performed** for this revision. The prior
> independent review (`ACCEPT WITH QUALIFICATIONS`) bound to the original report revision only and
> confers no approval on anything new here. §16 is a self-challenge, not independent review.

## 1. Baselines and custody

| Item | Coordinate |
|---|---|
| Live `main` at start and at every recheck | `912b2ac85cefab3907fc562a3aaa6507e51975a7` |
| Workspace tip at start | `f7da1edc2f09919b893a231558c878b6042fe276` |
| Original report (immutable) | `a53778145f3ee9941a4b46c7f04f1ec17e6bff86`, `workspace/research/investigations/simulation-execution-account-report.md`; baseline `79b3399149499f2f337206c909a277f66a5befd5` |
| Independent review (immutable) | `57d87fc4c8ba19927bf50efa4097321208811d5a`, `workspace/research/investigations/simulation-execution-account-adversarial-review.md`; `ACCEPT WITH QUALIFICATIONS` |
| Review pre-registration | `ba6db2e35f25150624a53ae900c48d15757b78fb`, `workspace/research/investigations/simulation-execution-account-review-reconstruction.md` |
| Review probes | `928fb86665016bc94d5979e18f1ce5b2ebef3b57`, `workspace/research/experiments/simulation-execution-account-review/com/arcogine/research/executionaccount/ExecutionAccountAdversarialTest.java` |
| **This run's pre-registration** | `f3975916c925fe6e465203e700e171d0d1a57c7a`, `workspace/research/investigations/simulation-execution-account-engine-adaptation-framing.md` |
| **This run's probes** | `f74c4ad7d9e0aa54743a17bf575699a0a5eeab92`, `workspace/research/experiments/simulation-execution-account-engine-adaptation/com/arcogine/research/executionaccount/adaptation/` (four files) |

All four handed-off evidence commits were verified as ancestors of the workspace head before any
commit was added. Nothing was rebased, amended or force-pushed; this run only appended commits.

**Material difference between `main` and the workspace.** `product/` is byte-identical. The only
difference is the landed Engine Evolution session/advancement note (`main` has it; the workspace,
branched from the original baseline, does not). It was read at `main`. It changes no runtime
behavior and explicitly denies that advancement establishes closure. No stale-baseline effect.

**Exposure.** The handoff orders reading the prior report, review, reconstruction and probes
after grounding and before candidate evaluation; that was done. The decision framing,
candidates and every expected value below were then committed before any new probe ran. A
repository-wide grep incidentally showed one line of `docs/research/synthesis-seeds.md` (the
transfer one-turn witness). It was not used.

## 2. Question and why it is reopened at all

> For trustworthy completion of supported execution evidence over a simulated-time interval,
> should Arcogine retain the independently reviewed controller-managed closure protocol, or change
> the Engine's supported session-control/observation boundary to offer a simpler or stronger
> guarantee? If an Engine adaptation is justified, what is the minimum semantic operation,
> observable evidence, ownership and compatibility consequence?

The review showed that a disciplined controller *can* close an interval with today's API. That
shows the protocol is feasible. It does not show the API is the right boundary. This run tests the
further claim: is the boundary architecturally suitable, or does a modest Engine change make the
system safer, simpler or better for consumers that are planned or foreseeable?

## 3. Executive conclusion

**Recommendation: do not change the Engine's closure boundary now (outcome 1).** Keep
controller-managed closure, with the exact bound-attestation invariants in §11, for comparative
reasons, not merely because today's API can express it. Designate one Engine adaptation, an
**explicit clock-advancement operation that emits a supported change** (D-clock, §6.1), as the
minimum change to adopt when a named trigger fires (§12). Reject the other Engine alternatives on
evidence.

Separately, this run found an **Engine-semantics completeness defect** that the pending E6
reconciliation must resolve, whichever closure decision is taken (§8):

- `maxEvents` counts internal scheduler events, including non-authoritative markers.
- A budget-limited call can therefore consume only a marker. No supported fact changes, yet the
  *next command* is stamped at a different time and the run's outcome changes (executed).
- Engine semantics section 4 rule 3 says such markers have no semantic significance. The original
  report proposed recording "commands apply at the runtime's internal current time". Recorded
  as-is, that rule would silently make marker placement part of the interpretation.

The preferred correction is **authoritative-turn counting** (E-guard, §6.2). It makes the latest
supported time always equal the time at which the next command applies. This is an Engine
definition change and needs its own independent review.

Why each candidate was or was not selected:

1. **No present consumer needs Engine-enforced finality.**
   - The only implemented driver, the research substrate, gets "no later input" by
     **confinement**: the runtime never escapes `ExperimentRunner.run()` (executed check). It
     binds its final cursor with a closing observation.
   - The Challenge evaluation consumes supplied outcome facts and drives nothing.
   - Governance has no wired simulation producer.
   - Live interactive pacing and passive verification are anticipated but not admitted. Planning
     explicitly keeps "new advancement/session semantics without a concrete consumer failure
     case" out of implementation.
2. **Every stop-condition and binding fact an Engine result type (B) or protected batch (C) would
   carry is already derivable by an exclusive driver** (executed with a research-local surrogate).
   `FactoryRuntime` has no concurrent-use contract, so atomic composites guard only against the
   driver's own mis-sequencing, and a shared library guards against that equally.
3. **The review's stale-frontier counterexample is prevented, not merely detected, by a library
   rule:** a licence must have coverage through the attested bound sequence (executed). A later
   capture gap never erases an earlier, prefix-bound proof (executed).
4. **Only D-clock adds a truth the current API lacks.** It lets simulated time pass while the run
   is idle, and makes past-interval finality a producer fact visible in the captured stream, so no
   controller trust is needed. It is coherent and conventional (SimPy 4.1.1 `run(until)` moves the
   clock to the target). But it is result-affecting session semantics, and its benefit falls
   entirely on consumers that do not exist yet.
5. **The input-watermark seal is falsified as an interval seal.** On an idle runtime the command
   clock cannot pass the floor without an event. A reject-only seal therefore never admits another
   command: a terminal state in disguise. Re-timing reduces to D-clock without its witness. A
   global terminal state duplicates what confinement already gives and forecloses continuation.

**Confidence:**

- High: the executed facts and the falsification of D-seal.
- Medium-high: "no present consumer needs it" (bounded search, §10) and the marker defect's
  classification.
- Medium: D-clock being the right future shape (specification-derived, unexecuted) and preferring
  E-guard to explicit recording.

**Independent review:** a new, genuinely independent adversarial review of **this exact report
revision** is required before any of its new conclusions inform reconciliation (§15).

## 4. Reconstructed invariants: who knows what

Repository facts (current `FactoryRuntime`, `RecordingScheduler`, `Scheduler`, `FactoryHandler`):

- **Command clock** `T_int` is `Scheduler.currentTime()`, the time of the last processed internal
  event. `submitWorkload` and `setMachineAvailability` stamp their effects at `T_int`.
  `advanceUntil` processes events up to and including its target, and never moves `T_int` to the
  target.
- **Supported time** `T_obs` is the time of the last emitted supported event, or 0.
  `T_obs <= T_int`. The two differ only after a non-authoritative marker moved `T_int`.
- **Monotonicity.** Every future supported change and every pending event has time `>= T_int`.
  Supported time is non-decreasing along sequence.
- **Internal markers.**
  - Dispatch from a machine queue or from the shared backlog schedules a no-op `TaskStart` and
    then a `TaskEnd`, both at the completion time.
  - Initial and next-step dispatch schedule only the `TaskEnd`.
  - An order's final completion schedules a same-time `OrderCompleted` marker.
  - `RecordingScheduler` classifies all markers as non-authoritative for run state, but `maxEvents`
    counts them.
- **Faults.** A `SimError` from advancement propagates out of `advanceUntil`, after the
  `finally`-path publication of the changes that already happened. No run-level fault state is
  retained.
- **Concurrency.** There is no synchronization and no concurrent-use contract. `FactoryRuntime`
  is documented as the exclusive owner of its handler and scheduler.

Derived invariant (inference, checked by probes):

- **Producer-final condition.** `[a,b)` is final, meaning no supported change with time `< b`
  will follow, whenever `T_int >= b`. The converse holds whenever some command would still be
  effective: while `T_int < b`, a submission or a genuine availability transition produces a
  change inside the interval. Edge models in which every command is rejected or a no-op are
  final earlier, but nothing in the supported contract proves that.
- **Supported witness.** Any party holding gap-free coverage can recognize finality from either
  of two witnesses:
  - the first captured supported change with time `>= b`, which proves every change below `b`
    precedes it;
  - an observation with supported time `>= b`.
- **What the witness cannot see.** It is conservative: it cannot see a finality established only
  by `T_int`.

Owners and roles are kept separate:

| Role | Holds | Can attest |
|---|---|---|
| Engine (producer) | meaning, order, time, epoch; current pending set; command clock | facts about now; monotonicity; nothing about future control |
| Controller (driver) | command and advancement outcomes, budgets, faults, its own future intent | exhaustion it demonstrated; its commitment about later input |
| Capture holder | the events and observations it received | possession and coverage |
| Analytical definition | the reading | sufficiency for that reading |

## 5. Alternatives tested

| | Candidate | Shape |
|---|---|---|
| **A** | Qualified controller protocol, no Engine change | `BoundedAdvance` + `ControllerClosure` + `HeldAccount` (research-local library, §7) |
| **B** | Engine-produced advancement result | `{stop, processed, runId, sequenceAfter, supportedTimeAfter}` returned to the caller |
| **C** | Engine-owned protected batch | one call advances, drains and observes |
| **D-clock** | Explicit clock advancement with a supported witness | §6.1 |
| D-silent | `advanceUntil` moves `T_int` to the target without emitting anything | variant of D-clock |
| **D-seal** | Input floor; commands below it rejected | §9 |
| D-retime | Commands below the floor applied at the floor | §9 |
| **D-terminal** | Run closed to all further commands | §9 |
| **E-guard** | Authoritative-turn counting | §6.2 |
| E-record | Record marker placement and counting as interpretation | §8 |
| **F1** | B's stop fact plus A's attestation | hybrid |
| **F2** | A promoted to a supported controller-owned capture facility | hybrid, non-Engine |

## 6. Contract sketches for the surviving Engine alternatives

These are specifications for comparison, **not landed or tested Engine behavior**.

### 6.1 D-clock: explicit clock advancement (designated future adaptation)

```text
AdvanceOutcome advanceTo(SimTime b, long maxTurns)   // maxTurns >= 1; unit per §6.2
  1. Process turns with time <= b, in the current order, until none remains, maxTurns is used,
     or a turn faults.
  2. EXHAUSTED (no pending internal event at or before b):
       if T_obs < b: set T_int := b and emit CLOCK_ADVANCED{to: b} at time b, next sequence.
       (If a processed change was already at b, nothing is emitted.)
  3. BUDGET: T_int is not moved and nothing is emitted.
  4. FAULTED(SimError): the existing finally-path publication applies; T_int is not moved and
     nothing is emitted; the outcome is returned, not thrown.
  returns {stop, turnsProcessed, runId, sequenceAfter, supportedTimeAfter}
```

**Invariants:**

- After `EXHAUSTED`, supported time is `>= b`, so `[a,b)` is producer-final for every holder whose
  coverage reaches the witness sequence.
- Commands then apply at `>= b`.
- Changes at exactly `b` may still follow (commands at `b`), so the operation proves half-open
  finality, not an inclusive `closedThrough(b)`.
- Equal-time order: changes processed at `b` precede the clock change; later commands at `b`
  follow it.
- The operation never reorders, skips or re-times a scheduled event, and has no effect on runs that
  never call it.

**Reset:** a fresh runtime starts at 0; old references are unaffected.

**What it does not guarantee:**

- capture coverage, which stays the holder's job;
- command-history completeness;
- fault-free success. A later `advanceTo` on a faulted run reports `EXHAUSTED`, which is truthful
  about finality, not about success.

**Compatibility:**

- Additive: a new operation, `RuntimeEventType` constant and payload record. Exhaustive pattern
  matches over the sealed payload stop compiling until they handle it, which is intended.
- Existing scripts are unchanged.
- Throughput is defined over observed time, so it changes after a clock change. That is consistent
  with Engine semantics section 10 ("per observed time").
- Required changes: an Engine semantics section 1.2 rule, a runtime-contract taxonomy entry and
  conformance fixtures.
- Spatial work must decide how in-flight transfers straddle `b` (they stay pending; specified,
  not checked).

**Acceptance-test contract:**

1. Model A2, two units, `advanceTo(100)`: sequence 13 is `CLOCK_ADVANCED` at 100, observation
   time 100. A following submit is accepted at 100. Assembler `[0,100)` reads 10, licensed by a
   holder covering 1–13 with no attestation.
2. A holder covering only 1–12 is refused.
3. An event exactly at `b`: no clock change is emitted, and the command after it is at `b`.
4. A budget stop leaves time and sequence unchanged.
5. A fault leaves time and sequence unchanged and returns `FAULTED`.
6. Two identical scripts produce identical streams with only `RunId` normalized.
7. A script that never calls the operation produces a byte-identical stream to today's.

### 6.2 E-guard: authoritative-turn advancement (preferred correction of §8)

```text
advance(): process any leading non-authoritative markers, then exactly one authoritative
           event; if no authoritative event remains, process nothing.
advanceUntil(t, maxTurns): count authoritative turns; the time bound applies to the
           authoritative event (a TaskStart marker always shares its completion's time).
```

**Invariants:**

- After every return, `T_int == T_obs`. Every authoritative event emits at least one supported
  change at its own time, and no marker is processed on its own.
- The latest supported observation therefore determines the time at which the next command
  applies.
- Markers become removable without changing any outcome.
- Trailing same-time `OrderCompleted` markers may stay queued. They do not move time and are
  already excluded from run state.

**Compatibility:**

- This is a **definition change** for budget-limited scripts whose budget ended after a marker.
  No consumer uses small budgets.
- Tracked tests with budget 1 compare bounded against looping advancement, which still converge
  under one shared unit. Their internal per-call event lists change.
- The review's and the original report's marker probes would describe the earlier definition.

**Acceptance tests:**

1. Probe D2's script: the second order is accepted at 10, *after* job 2's completion, with 11
   supported events (specification-derived below).
2. `advanceUntil(t, 1)` on the queue-dispatch path yields a supported completion.
3. `T_obs == T_int` property-tested over random budgets.
4. Bounded and unbounded advancement converge.

## 7. Proving cases: method, commands and results

**Method:**

- Expected values were hand-derived from `FactoryHandler`/`FactoryRuntime` and committed in the
  pre-registration before execution.
- The research-local library is built only on the supported surface:
  - `BoundedAdvance` derives the stop reason;
  - `ControllerClosure` performs exhaustion and binds `(RunId, b, S*, T*, attestor)`;
  - `HeldAccount` licenses `[a,b)` only via a producer witness or a covering, uncontradicted
    attestation whose bound sequence its coverage reaches.

**Models:**

- **S:** `PROCESS 5` on one concurrency-1 machine.
- **A2:** `CUT 3 -> ASSEMBLE 5` on Cutter (1) and Assembler (1).
- **F:** `CUT 5 -> HOLD Long.MAX_VALUE` on Cutter (1) and Holder (1).

**Environment:**

- No devcontainer was running, so the documented generic Docker route was used.
- `gradle:9-jdk21` image, Temurin `21.0.12+8-LTS`, wrapper Gradle `9.8.0`.
- The run used a `git archive` export of `product/` at `f3975916` (byte-identical to `main`'s),
  plus the working-tree experiment sources. Those were verified byte-identical, by blob hash, to
  `f74c4ad7` after the run.

```text
./gradlew :research-experiments:test -PresearchExperimentSources=/repo/workspace/research/experiments --no-daemon --no-build-cache
```

**Result:**

- **171 tests, 0 failures, 0 errors, 0 skips**, counted from JUnit XML:
  - 137 tracked tests, including `ResearchPackageBoundaryTest`, which now also polices the new
    package;
  - 14 original proving cases and 9 review probes, rerun;
  - **11 new discriminators**.
- **Every new expected value matched on the first execution.**
- **Not run:** Checkstyle, coverage, the full Java gate, security scans and CI. This is a
  research-evidence run.
- **Not executable:** spatial execution, and every Engine alternative (no production change).

| # | Discriminator | Executed result | Hand-derived match |
|---|---|---|---|
| D1 | One budgeted event | Initial-dispatch path: `advanceUntil(100,1)` yields `JOB_STEP_COMPLETED`, `ORDER_COMPLETED` (seq 3–4), `QUIESCENT`. Queue-dispatch path: yields **no** supported change; the observation is equal before and after | yes |
| D2 | Supported-invisible turn changes the next command | Y1 (budgeted call consumed the start marker, then submit): order 2 accepted at **10**, mean lead time **7.5**. Y2 (no budgeted call): accepted at **5**, mean **10.0**. The pre-submit observations agree in every field but `RunId`; same 12-event type sequence; only two times differ | yes |
| D3 | Budget zero | `advanceUntil(100,0)` returns empty while `ACTIVE` at time 0; negative budget throws; the surrogate refuses budget 0 | yes |
| D4 | Stop reason derivable (B) | Pending beyond target, so exhausted with work remaining (time 5, seq 5); idle, so exhausted with none remaining (seq 4); marker call: budget; `advanceThrough(5,2)`: **budget** although nothing remains, and a follow-up call gives exhausted; F: faulted with `EventOrderingViolation` | yes |
| D5 | Fault during advancement | `advanceUntil` throws; seq 4 (`JOB_STEP_COMPLETED`, step 0, not complete) and seq 5 (`JOB_DISPATCHED` to Holder) survive; `QUIESCENT` at 5; Cutter **idle, online, queue depth 1**, job 2 `Queued`. A later exhaustion is clean, and a successor's closure **attests** (bound seq 5) | yes |
| D6 | Stale frontier (A) | Attestation bound to seq 4 / time 5; a holder at seq 2 is **refused** and the reading throws; after draining it is licensed with **5** ticks over `[0,100)`, attestor recorded; `[0,5)` is producer-licensed with no attestation | yes |
| D7 | Prefix-bound proof | `[0,5)` licensed with proof sequence 3 (seq 4 is the first change at 5); after losing 6–7, still licensed with 5 ticks; `[0,10)` and `[5,10)` refused | yes |
| D8 | Competing drainers | Holders with 1–2 or 3–4 are refused under the attestation; the union gives 5 | yes |
| D9 | Reset | Attestation refused for the reset run; original run still accepts a submit | yes |
| D10 | Idle tail (A2) | `QUIESCENT` at 13, seq 12; `[0,100)` refused without attestation, 10 with it; a later submit is stamped **13** (seq 13–14), both changes are recorded as contradictions, and the licence is refused; after re-closure, **15** | yes |
| D11 | Confinement | `ExperimentEvidence` has no `FactoryRuntime` component | yes |

Prior evidence reused as baseline facts (rerun green):

- inclusive `advanceUntil`;
- a supported-time conservative refusal after a marker;
- a pending marker that does not block supported finality;
- half-open edge changes at `b`;
- invisible accepted no-ops;
- the sufficient serialized procedure;
- the waiting-definition probe;
- the original 14 cases: late join, gaps, equal-time order, the faulted command, reproduction
  granularity and drainers.

## 8. The marker / count-budget defect

**Repository facts:**

- Engine semantics section 1.2 defines `advanceUntil` as a loop over the one-event `advance()`,
  bounded by `maxEvents`.
- Section 4 rule 3: internal markers "do not gain semantic significance merely because they are
  present in the implementation."
- Section 1.1: a rule belongs to the interpretation if changing it can change outcome for an
  identical script. Consequence 4: an omitted rule of that kind is a documentation defect.
- The overview says markers "change nothing `observe()` reports". That is true. But they change
  `T_int`, which `observe()` does not report.

**Executed:**

- D1: the same session-control call, `advanceUntil(b, 1)`, sometimes completes authoritative
  work and sometimes completes nothing supported. Which happens depends on which internal dispatch
  path scheduled the work.
- D2: after a marker-only call, no supported fact distinguishes the two moments, yet the next
  command lands at 10 instead of 5. Mean lead time moves from 10.0 to 7.5.

**Specification-derived:** suppose a conforming implementation did not schedule the `TaskStart`
marker. D2's Y1 budgeted call would then process job 2's completion. The submit at 10 would
dispatch immediately: 11 supported events, with no `JOB_WAITING`. The same holds for consuming an
`OrderCompleted` marker instead of the next authoritative event. Marker placement is therefore
result-affecting for any script that issues a command after a budget-limited stop. Section 4 rule 3
and section 1.1 cannot both hold as written.

**Scope of effect:** only scripts that both stop on a positive finite budget and then command
before advancing again.

- Target-only and unbounded advancement are unaffected: a marker and its completion share a time.
- The research corpus uses unbounded budgets.
- A bounded scan of `domains/factory` tests found no command on a runtime after `advanceUntil`.

**Why it matters now:**

- The original report proposed recording the command-time rule in Engine semantics section 1.2.
  As phrased, that recording would quietly make marker scheduling normative.
- The admitted same-semantics shared-backlog performance work touches the backlog dispatch path
  that schedules these markers. Its result-equivalence fixture would not detect a marker change
  unless it includes budget-limited command scripts.
- The review reported correcting its own fixture assumptions about these markers, which is direct
  evidence of a hand-derivation hazard.

**Resolutions:**

- **E-guard (preferred):** the Engine already classifies markers as non-authoritative, and the
  invariant `T_obs == T_int` makes the observation sufficient to predict the next command's time.
- **E-record:** record marker placement and counting as interpretation. Behavior-preserving, but it
  freezes an implementation artifact and turns marker refactors into definition changes.

Either resolution is an Engine-semantics decision for its owner, with independent review. This
report does not adopt either.

## 9. Candidate × case matrix

Legend:

- **P** — guaranteed by producer facts.
- **C** — attested by the controller or holder, with trust recorded.
- **✖** — fails or lies.
- **—** — unchanged from A.
- **ex.** — executed; **spec** — specification-derived.

| Case | A (library) | B | C | D-clock | D-seal | D-terminal | E-guard |
|---|---|---|---|---|---|---|---|
| 1 idle tail, later input | C only; input at 13 breaks it, detectable if delivered (ex. D10) | — | — | **P**: input applies at 100; `[0,100)` stays 10 (spec) | P final; all later input rejected forever (spec) | P final; no continuation (spec) | — |
| 2 count stop, budget 0, marker | Derivable, never claims exhaustion falsely (ex. D3, D4) | Same facts; must fix the counted unit or "processed 1" misleads (ex. D1) | — | Budget stop does not move time (spec) | — | — | Unit becomes authoritative turns (spec) |
| 3 stale frontier | **Prevented** by bound licensing rule (ex. D6); a non-conforming claimant can still err | Same rule, atomic return | Prevented for the batch caller only | **Prevented by construction**: the witness is a change the stale holder lacks (spec) | — | — | — |
| 4 earlier subinterval | Preserved, prefix proof (ex. D7) | — | — | Preserved (spec) | — | — | — |
| 5 half-open, equal time | Producer witness excludes events at `b`; inclusive not promised (ex. review, D7) | — | — | Clock change after events at `b`; commands at `b` after it (spec) | Floor at `b` must not reject equal-time commands | — | — |
| 6 observed versus internal time | Supported time conservative, never false; next command time invisible (ex. D2) | Exposing `T_int` leaks an internal fact | — | Consistent after success; silent variant creates `T_int > T_obs` | Floor invisible without a witness | — | **Split removed** (spec) |
| 7 promise versus guarantee | No-op/rejected invisible; effective contradiction only if delivered (ex. review, D10) | — | — | Past-interval promise unnecessary; command history still controller-held | Enforced by rejection | Enforced | — |
| 8 delivery, shared holders | Refuses partial drainers, union licensed (ex. D8); late join refused | Result delivered to caller only | **✖** reshapes delivery: competes with or duplicates drain | Holder coverage still required | — | — | — |
| 9 reset, identity | Run-bound attestation (ex. D9) | — | — | Per-run clock (spec) | Per-run floor | Old run closed, new run open | — |
| 10 fault in advancement | Refuses on the faulting call; **not sticky**, a successor attests (ex. D5) | Definite `FAULTED`; still not sticky | — | No clock move on fault; not sticky (spec) | — | — | — |
| 11 provenance, replay | Exact budgets are part of the script (ex. D2) | — | — | Command times become explicit; equal-time ambiguity remains | — | — | Marker consumption cannot change outcome (spec) |
| 12 two-consumer cost | §10 | §10 | §10 | §10 | — | — | — |

**Falsifiers found:**

- **D-seal.** On an idle runtime `T_int` never reaches the floor, so the seal is terminal for
  input.
- **D-retime.** It equals D-clock without a witness: the first re-timed command is the first
  evidence that time passed, so an idle tail gives a passive holder nothing.
- **D-silent.** It reintroduces `T_int > T_obs`, the defect E-guard removes. It is result-affecting
  for every script that commands after an exhausted advancement, with no observable trace.
- **C.** It moves the single-drainer problem into the batch call rather than removing it.
- **B, standalone.** It carries no fact an exclusive driver lacks (D4). Its only addition, a
  thrown fault returned as a value, is not sticky either.
- **Every alternative, on fault stickiness.** None makes a fault sticky without new run-level fault
  state. Finality and successful processing stay separate facts in every design.

## 10. Who carries the burden

Present uses (bounded search scope: `product/` main and consumer source sets, `docs/planning/`, the
research register and the analytics briefs):

- research substrate — implemented;
- Challenge evaluation — implemented, outcome facts only;
- Governance — implemented, no simulation producer;
- static diagnostic runs — research, closed script.

Hypothetical consumers, each separately triggered:

- live interactive pacing with mid-run commands;
- a passive verifier or outward adapter.

| Obligation | Research runner, A today | Live session L, under A | Live session L, under D-clock + E-guard | Passive verifier V, under A | V under D-clock |
|---|---|---|---|---|---|
| exclusive driver | structural (confinement) | required | required | n/a | n/a |
| exhaustion proof | not needed (quiescence check exists) | loop until a call returns fewer than its budget | one result value | trust | — |
| bind post-advancement cursor | closing observation | `observe()` inside the closure call | witness sequence | check bound coverage | check witness coverage |
| "no later input" | confinement | promise, attested | not needed for past intervals | **trust attestor** | **not needed** |
| past interval during a continuing session | n/a | provisional until supported time passes it (idle tail: never) | final at each pause | — | — |
| command time | not used | last event time, not pause time; must be displayed | pause time | — | — |
| fault record | test failure | must carry it; not sticky | `FAULTED` result; not sticky | trust | trust |
| order-sensitive calls | 0 new | advance loop, then observe, attest, drain, license | `advanceTo`, drain, license | — | — |

**Engine burden of each change:**

- **A:** none.
- **B:** a result type plus a unit decision.
- **C:** a composite operation plus a delivery-semantics change.
- **D-clock:** an operation, clock state, an event type and payload, a section 1.2 rule, a
  taxonomy entry, fixtures, and a throughput consequence.
- **E-guard:** counting semantics, a section 1.2 and 4 text change, and fixtures. No new public
  type.
- **D-terminal:** a run-state value, an error, an operation, a witness event, and reset semantics.

**Reading:**

- D-clock with E-guard removes four of L's obligations and the verifier's trust in the attestor.
  Those are genuine simplifications, but L and V are not admitted.
- For the only implemented driver, A costs nothing new.
- A shared library (F2) removes repeated protocol code. It does not remove the trust assumption,
  which only D-clock removes.

## 11. Separating the "final" facts, and the E6 consequence

| Fact | Definition | Established by (today) | Under D-clock |
|---|---|---|---|
| `advancedThrough(b)@S` | no pending internal event `<= b` when the sequence was `S` | driver, by the stop idiom; not monotone while `T_int < b` | operation result |
| `supportedTime(S)` | `T_obs` at `S` | producer | producer |
| command clock | `T_int` | internal; equals `T_obs` only under E-guard | equals `T_obs` |
| `noFutureInputBefore(b)` | no later effective change with time `< b` | producer iff `T_int >= b`; otherwise controller promise or confinement | producer after `advanceTo(b)` |
| `capturedThrough(S)` | holder retains `(basis, S]` with basis time `<= a` | holder, gap arithmetic | holder |
| `finalityOf[a,b)` | all changes in `[a,b)` precede a known sequence | `noFutureInputBefore(b)` plus exhaustion below `b` | producer witness |
| account determinacy | finality plus coverage to the proof sequence plus basis | holder licence | holder licence |
| command/outcome completeness | all requests and results, including rejected, no-op and faulted | controller only | controller only |
| reproduction completeness | script at session-control granularity, **including budgets**, plus model and definition | custody | custody; budgets matter less under E-guard |
| fault-free success | no fault since the basis | the controller that saw every outcome | same |

**Recommended E6 shape for reconciliation** (new wording; review required). A claim over `[a,b)`
of run `R` is licensed for a holder that has a basis observation at time `<= a` and gap-free
coverage through a proof sequence `P`. `P` is one of:

1. **Producer-final.**
   - The sequence before the first captured supported change with time `>= b`; or
   - the cursor of an observation whose supported time is `>= b`.
2. **Controller-attested.** The bound sequence of an attestation `closedBefore(b' >= b)` that:
   - is bound to `(RunId, S*, T*)`, observed immediately after demonstrated exhaustion through
     `b'`;
   - was issued by the driver that observed every advancement and command outcome since the
     basis;
   - records the attestor;
   - is uncontradicted by any delivered change after `S*` with time `< b'`.

**Confinement of the runtime is a legitimate structural basis for the controller's commitment.**

**Never infer finality from:**

- an advancement request or target;
- a used-up budget;
- quiescence alone;
- an internal marker;
- an exhaustion observed by a driver that did not see earlier outcomes.

## 12. Recommendation on the Engine

**Do not change the Engine's closure boundary now.**

- The decisive facts: no implemented consumer has a failure the qualified protocol cannot avoid,
  and B and C add no truth for exclusive drivers (D4, D6).
- The only truth-adding adaptation, D-clock, changes command timing semantics with no consumer to
  justify it, against an explicit planning boundary.
- Moving the protocol into a shared library (F2) is optional. Promote it only when a second
  concrete driver needs interval closure.

**Rejected Engine alternatives, and why:**

| Alternative | Why rejected |
|---|---|
| B (standalone) | Derivable; it would also need the unit decision of §8 |
| C | Reshapes destructive delivery |
| D-seal | Terminal in disguise on an idle runtime |
| D-retime | No witness |
| D-silent | Hidden result-affecting change; reintroduces the observed/internal split |
| D-terminal | Safe but overbuilt; confinement already gives it; forecloses continuation; must not be conflated with the Operational closure question left open there |

**What stays on consumers:**

- exclusive control;
- the exhaustion loop;
- binding and attestation;
- fault custody;
- a recorded trust assumption for every downstream party;
- provisional labelling of open intervals;
- awareness that commands apply at the last event time.

**Signals that reverse the choice and trigger D-clock** (adopt it, with E-guard as a prerequisite,
rather than wrapper protocols):

1. an admitted consumer interleaves inputs with advancement in a continuing session and must
   present past intervals as final before the session ends, or must apply a command at a chosen
   simulated time while idle;
2. a party other than the driver must establish interval finality: a passive verifier, an outward
   adapter, a Governance producer that did not drive the run, or a handover of control between
   drivers;
3. concurrent or multi-component access to one runtime becomes supported. That trigger also
   reopens C.

**The marker defect is not deferred.** It must be resolved (E-guard preferred) before or within the
E6 reconciliation that records command-time semantics. The resolution has its own conformance
tests (§6.2) and needs independent review as an Engine definition change.

## 13. Implications for E5/E6 and the pending reconciliation

**Already accepted in the independent review** (unchanged here):

- bind closure to `RunId` and a post-advancement frontier;
- exhaustion, not budget;
- narrowed detection;
- half-open versus inclusive;
- prefix preservation;
- scoped "complete";
- no promotion of `CapturedAccount`;
- waiting and ownership qualifications.

This run **executed** those qualifications as a working library (D6–D9). That is reinforcement, not
new authority.

**New in this report** (each needs the new independent review):

1. **The exact producer-final condition (`T_int >= b`) and its event-witness form.** This
   sharpens E6's "supported time `>= b`" branch into a per-claim, prefix-bound proof.
2. **Fault knowledge is not sticky.** An attestation is sound only from a driver that saw every
   outcome since the basis. A successor's clean exhaustion over a faulted run is a real hazard
   (D5).
3. **Confinement as a structural basis** for the no-later-input commitment.
4. **The marker defect and its required resolution (§8).** It blocks recording the command-time
   rule as the original report proposed.
5. **The comparative decision.** No Engine change now, D-clock designated, the other alternatives
   rejected on evidence.

## 14. Impact on adjacent work

- **Paused analytics Phase 2:** not resumed. It consumes the coverage vocabulary unchanged. It must
  not assume a producer-final deadline witness exists. An idle-tail interval is final only by a
  controller attestation or confinement.
- **Delivery custody:** unchanged. The holder captures. No central store is shown to be needed
  (D8).
- **Governance:** an attestation is an attributable assertion whose author must be recorded. Under
  a future D-clock, finality becomes producer evidence. Neither changes the Governance evidence
  contract.
- **Operational continuity:** unaffected. D-terminal must not be read as simulation precedent for
  the open Operational closure question.
- **Engine Evolution:** its session/advancement revisit condition is consistent with this result.
  If reconciliation chooses to record a design hint there, it should cover the command-time
  observability invariant (`T_obs == T_int`) and D-clock as the designated shape for the §12
  triggers. The marker correction belongs in Engine semantics section 1.2 and section 4, not in
  Engine Evolution.

## 15. Independent review required

A **genuinely separate independent adversarial review of this exact report revision** (the commit
returned with this report) is required before reconciliation:

- the report changes no Engine meaning itself;
- but it introduces new load-bearing inferences (§13 items 1–4);
- and it recommends an Engine definition change (E-guard) on high-risk session-control semantics.

The prior review's disposition does not transfer. The reviewer should attack in particular:

- the producer-final iff-claim;
- the §8 classification and whether E-record should be preferred;
- whether D-clock's witness is truly necessary;
- whether any present consumer was missed by the bounded search.

## 16. Self-challenge (self-administered; not independent)

1. **"You preferred no change because nothing consumes it: a tautology?"** No. The test was whether
   any failure the review found survives the qualified library. Stale frontier, count exhaustion,
   drainers and prefix loss do not survive it (executed). The truth the library cannot supply, a
   non-trusted past-interval finality, has no present claimant. That is a consumer fact, and it has
   named reversal triggers.
2. **"D-clock is conventional; Arcogine is the odd one."** It is conventional (SimPy). Arcogine's
   inclusive, non-moving `advanceUntil` is a defensible pacing primitive. What is odd is only that
   no operation lets idle time pass. The cost of adding one is result-affecting semantics. Possible
   is not necessary.
3. **"The marker finding is out of scope."** It decides cases 2, 6 and 11. And the pending
   reconciliation would otherwise write a rule that contradicts section 4. It is reported with its
   own destination, not folded into the closure decision.
4. **"E-guard changes results."** Only for budget-limited scripts that command after a marker.
   E-record avoids the change at the price of freezing an artifact. The choice is left to the
   owner, with a stated preference.
5. **"The library just moves the obligations."** Partly true. It removes re-derivation and makes
   prevention a rule. It cannot remove the trust in the "no later input" promise. §10 states that
   residue.

## 17. External evidence

- **SimPy 4.1.1, `Environment.run(until=...)`.**
  - Sources: [API reference](https://simpy.readthedocs.io/en/4.1.1/api_reference/simpy.core.html)
    and [source](https://gitlab.com/team-simpy/simpy/-/raw/4.1.1/src/simpy/core.py). Verified in
    session.
  - What it shows: a numeric `until` schedules an `URGENT` stop event at the target, "before all
    regular timeouts"; the clock ends at `until` even when idle.
  - **Transfers:** clock-moving, half-open advancement is an established discrete-event design
    (supports D-clock's coherence).
  - **Breaks:** SimPy inputs are processes scheduled at chosen times; it has no separate command
    boundary stamped "now" and no supported sequence contract. It shows possibility, not
    necessity.
- **Apache Flink 1.20 (page v1.20.3), "Timely Stream Processing", watermarks.**
  - Source: [Flink docs](https://nightlies.apache.org/flink/flink-docs-release-1.20/docs/concepts/time/).
    Verified in session.
  - What it shows: a watermark asserts no more elements with timestamp `<= t`, and "it is possible
    that certain elements will violate the watermark condition"; late elements are handled by
    policy.
  - **Transfers:** an input watermark from outside the admission point is an assertion, not a
    guarantee. That is the shape of A's attestation and its contradiction detection.
  - **Breaks:** Flink cannot control its sources; Arcogine's Engine *is* the admission point. That
    is exactly why Arcogine could enforce what Flink can only assert (D-clock or D-seal). No owner
    is chosen by analogy.

## 18. Unknowns, confidence and reopening

| Conclusion | Confidence | Would change it |
|---|---|---|
| Executed facts (D1–D11) | High | — |
| D-seal falsified as an interval seal | High | An Engine clock that advances without events |
| B and C add no truth for exclusive drivers | High | A supported concurrent-access contract |
| No present consumer needs Engine finality | Medium-high | Any §12 trigger, or a consumer missed by the bounded search |
| §8 defect classification | Medium-high | An owner reading that budget-limited scripts are outside the conformance domain |
| D-clock is the right future shape | Medium | Spatial in-flight interaction; a consumer needing inclusive closure |
| E-guard preferable to E-record | Medium | Evidence that marker structure is needed elsewhere |

**Unknowns:**

- transfer and spatial interaction with clock advancement;
- whether any future consumer needs inclusive `closedThrough(b)`;
- whether sticky fault state will be needed by a non-controlling consumer, which is the original
  report's existing fault trigger.

## 19. Artifacts

**Reusable in a knowledge-transfer audit:**

- the pre-registered discriminator set;
- **D2**, a candidate conformance fixture for whichever §8 resolution is chosen;
- **D5**, the fault-not-sticky witness;
- the D6/D7 licensing rules, as specification text for E6;
- the §6.1 and §6.2 acceptance contracts;
- the external analogues, with their break points.

**Must not be promoted:**

- `BoundedAdvance`, `ControllerClosure` and `HeldAccount`. They are research-local statements of a
  protocol, not production types or a supported library.
- The original `CapturedAccount` (per the review).
- The specification-derived Engine outcomes, which are not executed evidence.

No production code, tracked test, architecture, specification, planning or register document was
changed.
