# Simulation execution account: independent reconstruction and pre-registered expectations

> **Status:** research custody, pre-registration. Written **before** reading the simulation-analytics
> Phase 1 report, the original Phase 2 report, its adversarial review, or the paused Phase 2
> revision. Not a conclusion; the report supersedes it.
>
> **Research baseline:** live `main` `79b3399149499f2f337206c909a277f66a5befd5`.
>
> **Read before this note:** the brief and its register row, the handoff, `AGENTS.md`, the
> researcher contract, `researching.md`, `testing.md` §10, the Product Charter, the architecture
> overview, runtime contract, Engine semantics, Operational continuity, Operational architecture
> §1–§2 and §10–§17, Governance evidence, Governance conformance §9–§13, Storage, concepts, the
> Engine-readiness and spatial-runtime plans (history/transfer sections), `FactoryRuntime` and its
> event/observation/command types, `Scheduler`, the session/delivery/closure acceptance tests, the
> Challenge attempt/comparison types, the `research-experiments` substrate, and the accepted game
> diagnostic report plus its independent review.
>
> **Exposure disclosure:** the handoff itself names the paused revision's default (first consumer
> owns evidence-window capture/completeness) and its "Rule 7" (custody follows the analytical
> definition owner). This note is blind to those reports' reasoning, not to that framing.

## 1. Constraints derived from current authorities

1. **Authority.** Engine owns live authoritative runtime state; a fresh observation is sufficient
   for the current view; Arcogine is not event sourced (runtime contract).
2. **Run epoch.** `RunId` is minted at `FactoryRuntime` construction. `reset()` returns a *new*
   runtime (new `RunId`, sequence restarts at 1) and leaves the original runtime untouched and still
   usable. So reset starts an execution; it does not end the old one.
3. **No terminal state.** `RuntimeRunState` is `ACTIVE`/`QUIESCENT`; quiescence covers both a
   fresh runtime and drained work, and a quiescent runtime still accepts commands.
4. **Event-driven clock.** `Scheduler.currentTime` moves only when an internal event is processed;
   `advanceUntil(target)` stops before later events and does **not** move the clock to `target`.
   Commands apply at the scheduler's current time. Observed time moves only with supported emission.
5. **Single drainer.** `drainSupportedEvents()` returns and clears one shared pending list. Two
   in-process drainers of one runtime receive disjoint subsets.
6. **Contiguous sequence.** Supported sequence starts at 1 and increments by one; observation cursor
   equals the latest emitted sequence. Gaps are therefore detectable by arithmetic.
7. **Controller-only facts.** `Rejected` and accepted-no-op commands emit no supported event;
   `Faulted` is reported only through `CommandResult`, while the supported stream carries only the
   changes that actually occurred.
8. **Engine definition.** No Engine-definition identifier exists; a research fixture runs against
   the revision executing it; custody records the revision.
9. **Distinct histories already named.** Engine-readiness planning lists internal scheduler events,
   supported runtime events, model revision history, Governance evidence/decision history,
   challenge attempt history and future Operational history as distinct histories, and assigns
   retention/resume-cursor/gap-detection hardening to later distribution hardening.
10. **Operational contrast.** An accountable operational continuation's identity *is* its accepted
    account (silent loss is a fork). A simulation run's identity is the runtime epoch; captured
    evidence is *about* it.

## 2. Reconstructed semantic hypotheses (to test, not adopted)

- **H-referent.** "One simulated execution" is one runtime epoch identified by `RunId`, from
  establishment (cursor 0, time 0, state determined by the published model) onward. An *account*
  is a body of supported evidence about that epoch with stated coverage. Several accounts of one
  execution can coexist; they agree wherever their coverage overlaps.
- **H-identity.** `RunId` suffices as execution correlation identity for current uses. No separate
  account identity is needed: a coverage statement (run, base cursor, retained sequence range,
  frontier) suffices for downstream provenance.
- **H-end.** No Engine-terminal completion exists. "Complete" can only mean a complete prefix or a
  complete interval up to a frontier; ending an execution's account is a controller act.
- **H-interval.** A claim over simulation interval `[a, b)` needs: a state basis at `a`, every
  supported change from that basis through the last change before `b`, and evidence that no later
  change can land in `[a, b)` — supported time already `>= b`, or an explicit controller closure.
  Advancing "to `b`" is not that evidence, because the clock does not move without events.
- **H-ordering.** Sequence is the semantic order; simulation time is a non-decreasing domain
  coordinate (implementation property; not yet stated by the contract).
- **H-basis.** Effective accepted commands are visible in supported events; rejected/no-op/fault
  outcomes are controller-held. Reproducing an execution needs the controller's script at the
  session-control granularity, which supported history alone may not determine.
- **H-custody.** No current consumer needs central retained custody; capture sits with whoever
  holds the drain (the controller or its delegate), not necessarily with an analytical-definition
  owner.
- **H-owner.** The coverage/closure rules derive entirely from Engine-owned facts, so their natural
  authority is the Engine-owned runtime contract, not a new execution-account owner or analytics.

**Predicted candidate outcomes:** C1 as literally stated ("each consumer declares its own
completeness") fails on the closure trap, the single-drainer case and silent gaps; C2's semantic
content survives but probably without a new owner/entity; C3 fails for lack of a consumer needing
central custody (only a future distribution-hardening trigger); C4 fails as too narrow and as an
ownership inversion. A hybrid must be tested, not assumed.

## 3. Pre-registered proving-case expectations (hand-derived from specifications)

Notation: `Jn` is the order's child with ordinal `n-1`; sequences are supported-event sequences.

### Model A — two-stage line

`CUT 3 -> ASSEMBLE 5`; Cutter (concurrency 1, CUT), Assembler (concurrency 1, ASSEMBLE); submit 2
units at `t = 0`.

| Seq | Time | Event |
|---:|---:|---|
| 1 | 0 | `ORDER_ACCEPTED` (J1, J2) |
| 2 | 0 | `JOB_DISPATCHED` J1 Cutter step 0 |
| 3 | 0 | `JOB_WAITING` J2 {Cutter} |
| 4 | 3 | `JOB_STEP_COMPLETED` J1 step 0 |
| 5 | 3 | `JOB_DISPATCHED` J1 Assembler step 1 |
| 6 | 3 | `JOB_DISPATCHED` J2 Cutter step 0 |
| 7 | 6 | `JOB_STEP_COMPLETED` J2 step 0 |
| 8 | 6 | `JOB_WAITING` J2 {Assembler} |
| 9 | 8 | `JOB_STEP_COMPLETED` J1 step 1 (job complete) |
| 10 | 8 | `JOB_DISPATCHED` J2 Assembler step 1 |
| 11 | 13 | `JOB_STEP_COMPLETED` J2 step 1 (job complete) |
| 12 | 13 | `ORDER_COMPLETED` (J2) |

Final: cursor 12, time 13, `QUIESCENT`. Assembler processes J1 `[3, 8)` and J2 `[8, 13)`; Cutter
`[0, 3)` and `[3, 6)`.

- **Full capture:** sequences 1..12 contiguous, all one `RunId`, all the published fingerprint;
  folding them onto the model's initial state reproduces the final observation's placement.
  Quiescence is not terminal: a later submit in the same run yields sequence 13 at time 13.
- **Late join at cursor 6 (time 3):** observation@6 plus events 7..12 reproduce the final view;
  whole-run coverage is refused (1..6 missing); Assembler occupancy over `[3, 13)` = 10 of 10
  from the late account and from the full account alike; an interval starting before 3 is refused.
- **Gap:** dropping sequence 7 must be detected. A gap-blind fold leaves J2 running on the Cutter,
  so Cutter occupancy over `[0, 13)` reads 13 instead of 6.
- **Equal time:** at `t = 3` and `t = 8` several events share a time. Folding in sequence order
  never exceeds concurrency; reversing order within a time (e.g. seq 10 before 9) puts two jobs on
  the concurrency-1 Assembler.
- **Interval crossing `[4, 7)`:** the state basis at 4 is the cursor-6 observation (Assembler
  active with J1); no change before 7 touches the Assembler; supported time reaches 8 (seq 9), so
  the interval is closed. Assembler occupancy = 3 of 3 without J1's dispatch event or completion
  event. Completion-credited `busyTicks` deltas over the same window read 0.
- **Closure trap:** after quiescence at 13, `advanceUntil(100)` leaves observed time 13. A later
  submit is accepted at time 13, and Assembler occupancy over `[0, 100)` changes from 10 to 15.
- **Reset:** the reset runtime has a new `RunId`, cursor 0, time 0, no orders, the same fingerprint;
  rerunning the same commands reproduces the same stream after run-identity normalization; the
  original runtime remains usable and continues its own sequence (13) under its own `RunId`.
- **Two drainers on one live run:** a drainer at `t = 4` gets 1..6 and another at the end gets
  7..12; each alone has a detectable gap; one capture fanned out to two views gives both views the
  same identity, order and coverage.

### Model A' — controlled change

Model A plus a second Assembler (concurrency 1, ASSEMBLE). Expected completion 11 (J1 on
Assembler 1 `[3, 8)`, J2 on Assembler 2 `[6, 11)`); 11 events; distinct `RunId`; both accounts
complete and both start at sequence 1, so merging by sequence is meaningless. The authored change
is the added resource and the widened ASSEMBLE eligibility, identified by Factory's semantic
comparator.

### Model B — availability change and idle slot

One step BAKE 4; Oven concurrency 2. Script: Oven offline, submit 4, Oven online.

| Seq | Time | Event |
|---:|---:|---|
| 1 | 0 | `MACHINE_AVAILABILITY_CHANGED` offline |
| 2 | 0 | `ORDER_ACCEPTED` |
| 3–6 | 0 | `JOB_WAITING` J1..J4 {Oven} |
| 7 | 0 | `MACHINE_AVAILABILITY_CHANGED` online |
| 8 | 0 | `JOB_DISPATCHED` J1 |
| 9, 10 | 4 | J1 completes; J2 dispatched |
| 11, 12 | 8 | J2 completes; J3 dispatched |
| 13, 14 | 12 | J3 completes; J4 dispatched |
| 15, 16 | 16 | J4 completes; `ORDER_COMPLETED` |

After coming online: Oven `Busy`, 1 active of 2, own queue depth 3. Completion 16. Every tick of
`[0, 16)` has one working and one idle slot while eligible work waits. Two legitimate definitions
over this one account: slot utilization `16 / (2 x 16) = 0.5`; machine busy fraction (at least one
active job) `16 / 16 = 1.0`. The account is identical under both.

### Model C — reproduction granularity

One step PROCESS 5; machine concurrency 1; submit 2 at `t = 0`. J2 is dispatched by the cascade at
5, which schedules an internal no-op start marker and the completion both at 10.

- **Script A:** `advanceUntil(9)`, then `advanceUntil(10, maxEvents = 1)` (processes only the marker),
  then submit 1. Expected: observation before the submit still says time 5, yet the order is
  accepted at time 10, *before* J2's completion at 10, so the new unit waits (`JOB_WAITING`) and is
  dispatched by the completion cascade at 10.
- **Script B:** `advanceUntil(10)`, then submit 1. Expected: accepted at 10 after J2's completion;
  dispatched immediately; no `JOB_WAITING`.
- Both are legitimate distinct executions with different supported histories. Re-driving A's
  commands at their recorded simulation times through supported control reproduces B, not A.

### Model F — faulted command

Two independent single-machine routes (Op A 5 on M1; Op B `Long.MAX_VALUE` on M2). Script: M1
offline; submit A (waits); submit B (dispatched); advance through `Long.MAX_VALUE`; M1 online.
Expected: `Faulted` returned to the controller; supported stream ends with
`MACHINE_AVAILABILITY_CHANGED` online and `JOB_DISPATCHED` A on M1; no supported event or
observation field marks a fault; afterwards the run reports `QUIESCENT` while job A is
`InProgress` on M1 forever. A non-controlling account is "complete" yet cannot know a fault
occurred.

### Spatial

A published model with a present spatial record is refused before any runtime exists: no `RunId`,
no account. Transfer events, when executed, become further supported facts in the same sequence.

## 4. Discriminators

| Case | C1 literal (consumer-declared) | C2 semantics, representation deferred | C3 retained record | C4 analysis-owned session |
|---|---|---|---|---|
| Closure trap | fails unless a common rule exists | survives if closure rule is part of it | survives only via the same rule | survives for analytics, not for presentation/verification |
| Two drainers | fails silently unless gaps are checked | survives (coverage rule) | survives (custody) — but no consumer needs it | too narrow |
| Gap | fails if blind | survives | survives | survives for analytics only |
| Two definitions over one account | survives | survives | survives | **risk:** definitions shape the "session" |
| Fault | controller-only fact | needs the controller's outcome record | same | same |
| Reproduction | needs controller script | needs controller script | same | same |
