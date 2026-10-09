# Engine adaptation versus controller-managed closure: pre-analysis framing

> **Status:** research-local checkpoint. It is not the report and carries no conclusion. It fixes the
> decision framing, candidate definitions and hand-derived expected outcomes **before** any new
> discriminator is executed, so the later report can be checked against what was expected rather
> than against what was observed.
>
> **Research baseline:** live `main` `912b2ac85cefab3907fc562a3aaa6507e51975a7`; workspace tip at
> start `f7da1edc2f09919b893a231558c878b6042fe276` (the handoff commit). `product/` is identical at
> both; the only difference is the landed Engine Evolution session/advancement note, which the
> workspace does not contain and which was read at `main`.
>
> **Exposure disclosure:** before writing this framing the investigator read, at their exact
> commits, the original report, the independent review, the review's pre-registration and its
> probes (the handoff orders that reading after grounding and before candidate evaluation). A
> repository-wide grep incidentally surfaced one line of `docs/research/synthesis-seeds.md` (the
> transfer one-turn witness); it is not used as input.

## 1. What `[a,b)` evidence must guarantee

A claim "over simulated interval `[a,b)` the supported state history of run `R` was `H`" needs
four distinct facts. They have different owners and must not be collapsed:

| Fact | Meaning | Who can know it now |
|---|---|---|
| **producer meaning** | each supported change is authoritative, ordered by sequence, time non-decreasing along sequence | Engine (runtime contract) |
| **temporal finality** | no supported change with time `< b` will be emitted after some sequence `S*` | Engine *if* its command clock is already `>= b`; otherwise only a promise about future control |
| **capture coverage** | holder `H` retains every supported change in `(basis, S*]` from a basis observation at time `<= a` | the holder (gap arithmetic is checkable against a genuine frontier) |
| **analytical sufficiency** | the retained facts suffice for the named definition | the definition |

### What the Engine can know at one instant (current implementation)

Repository facts from `FactoryRuntime`, `RecordingScheduler`, `Scheduler`, `FactoryHandler`:

- **Command clock** `T_int` = time of the last processed internal event (`Scheduler.currentTime()`).
  Both commands stamp their effects at `T_int`. `advanceUntil` never moves it to the target.
- **Supported time** `T_obs` = time of the last emitted supported event (or 0). `T_obs <= T_int`.
  They differ only after a non-authoritative marker moved `T_int` without a supported change.
- **Pending set**: `peekTime()` and whether authoritative work is pending (`RuntimeRunState`).
- Every future change has time `>= T_int`; every pending event has time `>= T_int`.

Therefore, **from producer facts alone**, `[a,b)` is final exactly when `T_int >= b`. Supported time
`T_obs >= b` is a sufficient, conservative witness, visible to anyone holding a genuine observation
or a supported event with time `>= b`. A supported event at sequence `k` with time `>= b` proves
every change with time `< b` has sequence `< k` (monotone time along sequence and `T_int` monotone).

What the Engine **cannot** know: whether a later command will be issued. It *does* decide at which
time such a command applies. So "no future input before `b`" is unknowable as a prediction but
**enforceable** as a rule — this is the one place an Engine adaptation could add truth.

### Safety properties and two separate calls

`FactoryRuntime` has no synchronization and no documented concurrent-use contract; it is an
exclusively owned session (its own Javadoc). Under that boundary nothing interleaves between two
calls made by one driver. A composite Engine call therefore protects only against the driver's own
mis-sequencing (for example declaring closure before observing), which a shared library protects
against equally. Concurrent access is outside the current contract for every method, not only for
closure.

### Who needs the guarantee today

| Use | Status | Driver / holder / claimant | How finality is obtained now |
|---|---|---|---|
| Research substrate (`ExperimentRunner`) | implemented | one component, closed script | confinement: the runtime never escapes `run()`; closing observation binds the final cursor; quiescence checked |
| Execution-account research probes | research-local | one test | controller discipline (reviewed procedure) |
| Challenge evaluation | implemented (main) | none: consumes `AuthoritativeOutcomeFacts` supplied synthetically | not applicable yet; a future producer would be a closed-script driver |
| Governance evidence/use | implemented headlessly | no simulation producer wired | not applicable |
| Diagnostic game (static recorded runs) | research `READY`, not implemented | generator | closed script |
| Live interactive pacing | anticipated, not admitted | player-driven loop | not available |
| Passive verifier / outward adapter | none; demand-triggered | another party | not available |

### Missing semantics versus cumbersome API

- **Cumbersome only:** stop reason (derivable by an exclusive driver: a call returning fewer events
  than its positive budget exhausted everything through the target); binding the post-advancement
  cursor (one `observe()` under exclusive control); fault reporting (advancement throws `SimError`).
- **Missing semantics:** a way to let simulated time pass while idle, so that a later command applies
  at a chosen time and the past interval becomes final by producer monotonicity; and a supported
  witness of that boundary for a party that did not drive the run.
- **Candidate specification defect (to be tested, not assumed):** `maxEvents` counts internal events
  including non-authoritative markers (`TaskStart` before a queue/backlog dispatch completion; the
  `OrderCompleted` marker after an order's final completion). If a budget stop that consumes only a
  marker changes the next command's time, a marker the specification calls insignificant
  (Engine semantics section 4 rule 3) is result-affecting under section 1.1.

### The meanings of "final" kept apart

1. `advancedThrough(b)` at `S`: no pending internal event had time `<= b` when the sequence was `S`.
   Not monotone under future commands while `T_int < b`.
2. `capturedThrough(S)` for holder `H`.
3. `noFutureInputBefore(b)`: no later accepted *effective* change with time `< b`.
4. `finalityOf[a,b)` = all supported changes with time in `[a,b)` have sequence `<= S*`.
5. no future accepted command **at all** (terminal for input).
6. command/outcome completeness (rejected, accepted no-op, faulted results): controller-held only.
7. reproduction completeness: the session-control script (including budgets), model and definition.
8. fault-free processing: separate from all of the above.
9. a globally terminated runtime: does not exist (quiescence is a current state).

### How consumer burden will be compared

Per consumer: semantic invariants it must carry, order-sensitive calls, trust assumptions, repeated
protocol code, failure paths. Not line counts.

## 2. Candidate definitions (none preferred)

- **A — qualified controller protocol, no Engine change.** Exclusive driver; stop admission; loop
  bounded advancement until a call returns fewer events than its positive budget; on `SimError` refuse;
  observe and bind `(RunId, S*, T*)`; attest `closedBefore(b)` with attestor and scope. Holder
  licenses `[a,b)` only with basis `<= a` and gap-free coverage through `S*` (controller branch), or
  through a supported change/observation with time `>= b` (producer branch). Realizable as a shared
  research-local library.
- **B — Engine-produced bounded-advancement result.** `advanceUntil` (or a sibling) returns
  `{stopReason in EXHAUSTED | BUDGET | FAULTED, processed, runId, sequenceAfter, supportedTimeAfter}`.
  Source-authored, but delivered to the caller only.
- **C — Engine-owned protected batch.** One call advances, drains and observes, returning events
  plus a frontier bound to them.
- **D — Engine-enforced temporal closure.** Variants that are *not* assumed equivalent:
  - **D-clock:** a new operation advances through `b` and, on successful exhaustion, sets the
    command clock to `b`, emitting a supported change at `b` when supported time is below `b`.
    Later commands apply at `>= b`.
  - **D-seal:** an input floor `F = b`; commands while `T_int < F` are rejected (zero mutation).
  - **D-terminal:** a closed run rejects every later command.
  - **D-retime:** commands while `T_int < F` are applied at `F`. (Expected to reduce to D-clock.)
- **E — minimal Engine guard.** Count only authoritative turns: markers never end a budgeted call and
  never move the command clock by themselves, so `T_int == T_obs` after every return. Expected to fix
  frontier truthfulness and the marker defect, not idle-tail finality.
- **F — hybrids,** for example B's stop fact plus A's attestation, or A promoted to a supported
  controller-owned capture facility.

Expected (to be tested): D-seal on an idle runtime never admits another command, because the command
clock cannot pass `F` without an event — a terminal state in disguise; D-retime equals D-clock.

## 3. Hand-derived discriminators (new probes)

Models: **S** = `PROCESS 5` on one concurrency-1 machine `M1`. **A2** = `CUT 3 -> ASSEMBLE 5` on Cutter
(1) and Assembler (1), two units at 0 (the original report's model A). **F** = `CUT 5 -> HOLD
Long.MAX_VALUE` on Cutter (1) and Holder (1). Derived from `FactoryHandler`: initial and next-step
dispatch schedule only a `TaskEnd`; queue and backlog dispatch schedule a `TaskStart` marker then a
`TaskEnd` at the completion time; an order's final completion schedules an `OrderCompleted` marker
at the same time.

1. **One budgeted turn, two meanings (S).** X: submit 1; `advanceUntil(100,1)` returns 1 and yields
   supported changes 3 and 4 (`JOB_STEP_COMPLETED`, `ORDER_COMPLETED` at 5), `QUIESCENT`. Y: submit 2;
   advance to 9 (sequence 5, time 5, `ACTIVE`); `advanceUntil(100,1)` returns 1 (the marker at 10)
   and yields **no** supported change; the observation before and after is equal.
2. **A supported-invisible turn changes the next command (S).** Y1 = Y then submit 1: accepted at
   **10**, waits, dispatched at 10, done 15; order lead times 10 and 5, mean **7.5**. Y2 = submit 2,
   advance to 9, submit 1 (no budgeted call): accepted at **5**, mean lead time **10.0**. Same
   12-event shape; only the second order's acceptance time differs. Supported observations before
   the submit agree in time, sequence, run state and job/resource projections.
3. **Budget zero (S).** submit 1; `advanceUntil(100,0)` returns empty while `ACTIVE` at time 0;
   `advanceUntil(100,-1)` throws `IllegalArgumentException`. "Empty result means nothing pending"
   lies; "fewer than a positive budget" never claims exhaustion for budget 0.
4. **Stop reason derivable by an exclusive driver.** A research-local wrapper reports: S two units to
   7 → exhausted, authoritative work remains, supported time 5; S one unit to 100 → exhausted,
   nothing remains; Y's marker call → budget; S one unit with budget 2 at target 5 → **budget**
   (processed the completion and the `OrderCompleted` marker) although nothing remains, resolved by a
   follow-up call → exhausted; F → faulted.
5. **Fault during advancement (F, two units).** submit 2 → sequences 1–3. `advanceUntil(100, max)`
   throws `EventOrderingViolation` (completion time overflows). Supported changes 4
   (`JOB_STEP_COMPLETED` job 1 step 0 at 5) and 5 (`JOB_DISPATCHED` job 1 to Holder) survive. The
   observation is `QUIESCENT` at 5; the Cutter is idle and online with queue depth 1 (job 2 never
   dispatched). A second advancement returns empty: the exhaustion idiom then reports a clean
   exhaustion. No supported fact records the fault; the Engine has no sticky fault state.
6. **Bound attestation prevents the stale-frontier claim (S, one unit).** Holder captures 1–2. A
   library closure performs exhaustion and binds `S* = 4`, `T* = 5`. Licensing `[0,100)` is refused
   (coverage ends at 2). After the holder drains 3–4 it is licensed with **5** active-job-ticks.
7. **Prefix-bound proof survives later loss (S, two units).** Advance to 5; capture 1–5. `[0,5)` is
   producer-final (sequence 4 has time 5); 5 ticks. Then advance to 100 and lose 6–7. `[0,5)` stays
   licensed with 5 ticks; `[0,10)` and `[5,10)` are refused.
8. **Competing drainers.** Holder P drains 1–2, holder Q drains 3–4; neither licenses `[0,100)` under
   the attestation; the union does.
9. **Reset.** An attestation bound to `R1` is refused for the reset run `R2`'s evidence; `R1` still
   accepts commands.
10. **Idle tail and later input (A2).** `advanceUntil(100)` → `QUIESCENT` at 13, sequence 12; submit 1 is
    accepted at **13** (sequences 13–14). Assembler active-job-ticks over `[0,100)`: **10** before,
    **15** after. A library attestation issued before the submit is contradicted by sequence 13
    (time 13 < 100, after `S* = 12`), detectably only because the holder receives it.
11. **Confinement.** `ExperimentEvidence` has no `FactoryRuntime` component, so the substrate's runtime
    cannot receive input after `run()` returns.

Expected D-clock outcomes (specification-derived; cannot be executed without changing the Engine):
for case 10 after `advanceTo(100)`, the run emits one supported clock change at 100 (sequence 13),
the later submit applies at 100, and `[0,100)` stays **10** and is producer-final for any holder
covering sequence 13. For case 6 the stale holder cannot license `[0,100)` by the producer branch
because the witness is a supported change it has not captured.

## 4. Pre-stated falsifiers

- If case 2 shows equal outcomes, the marker-defect hypothesis is false and E loses its main motive.
- If case 4 needs a non-supported fact, B adds truth that A cannot derive.
- If case 6 cannot be prevented by a library rule, a producer-side primitive is necessary for that
  failure.
- If a present consumer needs a party other than the driver to establish finality, A's trust model is
  insufficient now and D-clock (or another D variant) must be weighed for adoption, not deferral.
