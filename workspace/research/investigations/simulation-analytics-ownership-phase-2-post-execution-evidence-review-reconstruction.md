# Independent reconstruction checkpoint — analytics ownership Phase 2 post-execution-evidence review

> **Role:** pre-report reconstruction for an adversarial review, written **before** reading the
> reviewed report's executive conclusion, analysis, candidate verdicts or self-challenge.
>
> **Reviewed target (identity verified only):** `workspace/simulation-analytics-ownership-phase-1`,
> commit `d2125f39482de3306f2f56e9c7261a3dde160b33`,
> `workspace/research/investigations/simulation-analytics-ownership-phase-2-post-execution-evidence-revision.md`
> (758 lines; stated research baseline `a7f9856617d99e796d528a44aa8c15677c485f55`). Only its opening
> metadata block (lines 1–30) and its section-heading list were read before this checkpoint.
>
> **Reviewer live-main baseline:** `a7f9856617d99e796d528a44aa8c15677c485f55` (#469), resolved at
> review start; no drift since the report's stated baseline.
>
> **Authority:** research evidence only. Not a disposition, not architecture.

## 0. Anchoring disclosure

This is a fresh, isolated session with no responsibility for the report. It is very probably the
same model family as the report's author (commits carry the owner's human identity, so the author
model cannot be established); that weakens model-family independence and is disclosed in the review.

Unavoidable anchoring before this checkpoint: the review handoff
(`workspace/research/handoffs/simulation-analytics-ownership-phase-2-post-execution-evidence-adversarial-review.md`
at `3248f93b`) states the report's headline (`RECOMMEND STAGED/NO SHARED OWNER NOW`, consumer-local
default, a shared-owner admission test, definition ownership separated from evidence custody,
custody following capture opportunity), names its candidate labels A, A′, B, C, D, E, F, G, and
lists attack surfaces, some of which paraphrase report content (P4.3 closed/open pair, P6 two-call-site
example, P7 throughput 0.2 vs 0.01, `prove()` helper). The candidate set and failure cases below were
derived from live authority; where they coincide with handoff wording, that coincidence is not
independent recurrence.

## 1. The question, restated

Under the adopted runtime evidence contract (basis/coverage/frontier, claim-scoped completeness,
half-open finality with producer witness or attributable controller closure) and the current Engine
(no utilization/busy-time/occupancy/bottleneck measure; `busyTicks` removed by owner decision), who
should own the *definitions* of interval/diagnostic measurements such as occupancy, period
utilization, waiting-by-step, idle/starved classification and run comparison:

- the Engine (a supported derived result);
- a consumer-neutral shared analytics owner, now;
- each consuming capability (Game/Challenge, Governance evaluation, research) locally, possibly with
  a later promotion rule; or
- a split by measurement kind.

The decision at stake is a durable ownership rule (and its admission test), plus what it implies for
the Game consumer plan, the runtime contract's "explicitly named analytical method" sentence and the
downstream evidence/provenance, adapter and comparability candidates.

## 2. Current authority and consumer boundary (verified at `a7f9856`)

Repository facts:

1. **Engine-owned supported derived results** are `backlog`, `completedOrders`, `completedSalesValue`,
   `averageLeadTime`, `throughputPerTick` (Engine semantics §10, §10.1–§10.2; `FactoryRuntime.observe()`
   computes throughput as `completedSales / observedTime.value()` — completed orders per *observed
   supported time since run start*, `0` at time zero). `combinedQueueDepth` is result-affecting
   ranking arithmetic (§2), not a presentable measurement.
2. **No Engine measurement** of utilization, cumulative busy time, occupancy or bottleneck
   (Engine semantics §10; runtime contract "Supported observations…"). `ResourceObservation` carries
   `machineId, name, state, concurrency, activeJobIds, queueDepth, capacityLiters, setupTime` — no
   `busyTicks`; `git grep busyTicks` finds no product or durable architecture/reference/development
   hit.
3. **Supported events** that carry measurement evidence: `JOB_DISPATCHED(job, order, machine, step)`,
   `JOB_WAITING(job, order, eligibleMachines)`, `JOB_STEP_COMPLETED(job, order, machine, step,
   jobComplete)`, `ORDER_ACCEPTED`, `ORDER_COMPLETED`, `MACHINE_AVAILABILITY_CHANGED(machine, online)`.
   Job observations expose `createdAt`/`completedAt` but **no dispatch/start time**: a late joiner
   cannot recover when currently active steps began.
4. **Observation time equals the time of the last supported change** (runtime contract; Engine
   semantics §1.2 and §4 rule 3; `emit()` sets `observedTime`). Advancing an idle run moves no
   supported time.
5. **Runtime contract, responsibilities and limits:** the runtime contract owns run/sequence/basis/
   coverage/finality meaning; the controller owns input/advancement and its attributable promises
   and outcome provenance; the holder owns capture/retention/coverage qualification; an
   "analytical-definition owner" owns method-specific sufficiency and computation; "definition
   ownership does not imply physical custody"; roles may share a component. Utilization/occupancy
   require "an explicitly named analytical method" — **owner unnamed**.
6. **Game consumer plan §4/§5:** reusable derived measurement is "**Unresolved — not game-owned by
   default**"; the game "must not implement generic analytics locally as though it owned the
   semantics" until the analytics question resolves. Vertical-slice gate item 4 requires a settled
   ownership/input contract for any reusable analytics requirement.
7. **Governance evidence §11–§13:** "Engine and analytics producers own calculation semantics …
   and analytical definitions. Verification-objective semantics (… utilization …) remain
   domain-owned"; analytical-definition provenance ownership is open (§12–§13). The Governance
   capability plan proves analytical results only through attributed fixtures.
8. **Factory design §10.2:** "Maximum utilization <= threshold" is an *illustrative* shared
   verification objective with no basis (population, denominator, availability treatment) and no
   acceptance evidence.
9. **Charter §6:** core capabilities may drive foundations, but each investment needs "a named
   requirement, an owning capability, bounded responsibility, and observable acceptance evidence; a
   possible future use alone is insufficient."
10. **Consumers today:** no outward adapter; no admitted playable Game slice (diagnostic evidence
    question `READY`); Governance analytical evaluation is fixture-only; `research-experiments` is
    non-shipped. Historical retired consumers (generic KPI, KPI endpoint, web history) show
    possibility of multi-consumer measurement, not a current requirement.
11. **Evidence/provenance brief promotion trigger:** a concrete analytical use needing "additional
    supported inputs, method-specific sufficiency, accumulation, definition identity, result
    provenance or retention" — no requirement that the result cross to a non-holder.

## 3. Candidate ownership models (independently enumerated)

| # | Model | Selects a different responsibility? |
|---|---|---|
| M1 | **Engine measurement** — the Engine defines and publishes a named occupancy/utilization result (Engine-semantics + runtime-contract change) | Yes |
| M1′ | **Engine running accumulator** — the Engine exposes a continuously credited per-resource processing integral (job-ticks to observed time) as a fresh-observation fact; consumers difference it, keeping denominators/conditioning | Yes (numerator owner) |
| M2 | **Facts-only Engine + shared consumer-neutral analytics owner now** (one capability owns named definitions for all consumers) | Yes |
| M3 | **Facts-only Engine + consumer-local definitions, no admission rule** (status quo with the owner named per consumer) | Yes |
| M4 | **Staged admission** — consumer-local by default; a definition is promoted to a shared owner only when an explicit admission test is met | Yes |
| M5 | **Kind-specific split** — current-state projections and result-affecting derivations stay Engine; interval measures, diagnostic interpretation and comparison each get their own owner rule | Yes (may combine with M4) |
| M6 | **Custody-led** — whoever holds the evidence owns the definition | Yes — but contradicted by runtime contract §responsibilities |
| M7 | **Governance-owned verification measures** | Contradicted by governance-evidence §11 |
| M8 | **Shared stateless definitions + any holder** (definitions are pure functions of supplied evidence; physical holder arbitrary) | Mostly an implementation arrangement compatible with M2–M5, not a separate owner verdict |

## 4. Proving and failure cases derived before reading the report

- **R1 — custody ≠ definition.** One exclusive driver/holder computes two independent named methods
  (e.g., a Game period occupancy and a governance-style max-utilization objective) from one capture;
  no custody moves and neither meaning changes. Proves *possibility* only; it does not specify a
  cross-party handover contract.
- **R2 — definition needs controller-only evidence.** A claim needing "no further input" or
  fault-free success needs controller provenance; custody still need not follow the definition, but
  the controller's attestation, trust scope and outcome qualifications must travel with the evidence.
- **R3 — promotion of retained results.** Identical arithmetic and declared inputs preserve meaning
  for *future* evaluations. *Retained* consumer-local results promoted to a shared definition also
  need method identity, basis, proof frontier, proof class and trust scope recorded at production
  time; otherwise promotion requires recomputation or qualification. Any "no-rework" promise is
  conditional on that capture.
- **R4 — honest controller vs passive party.** A `Faulted` `setMachineAvailability` emits
  `MACHINE_AVAILABILITY_CHANGED` plus any dispatches that occurred — indistinguishable on the
  change stream from success. A rejected command or accepted no-op emits nothing. Complete supported
  changes therefore do not certify fault-free or complete-command history; occupancy over a final
  interval is still passively computable, because occupancy depends only on authoritative changes.
- **R5 — "so far" finality is free.** Because observation time equals last-change time, a fresh
  observation at `(Sobs,Tobs)` with gap-free capture from a basis `<= a` through `Sobs` makes
  `[a,Tobs)` final by producer evidence (no later change can have time `< Tobs`). Live "so far"
  period measures therefore need no controller closure; only `[a,b)` with `b > Tobs` (idle tail)
  needs closure.
- **R6 — current-state vs future-horizon.** "M has no currently eligible work" is a one-observation
  snapshot claim; "M will receive no work before `b`" is a horizon claim needing controller input
  exclusion and must not be produced by predicting routing flow (second-Engine risk).
- **R7 — reachable idle-with-queued-work.** Engine semantics §4 rule 7 (one queued dispatch per
  trigger) suggests a `concurrency > 1` machine can hold free capacity with eligible queued work
  until its next trigger; a binary idle/busy or starved/surplus classifier must handle a third state
  or refuse. (Hypothesis from the specification; to verify.)
- **R8 — stale frontier and earlier prefix.** The runtime contract's five-tick example: a holder with
  only the time-zero dispatch and its stale cursor must refuse 100 active-job-ticks; a proved `[0,5)`
  survives a later gap.
- **R9 — helper false positive.** A research helper that checks local sequence contiguity but not run
  identity, basis/frontier consistency, monotone time or frontier coverage could accept mixed or
  tampered evidence. Distinguish a defect in a research-only helper from one in the report's
  normative claim.
- **R10 — late joiner.** A basis at `T0 > 0` supports occupancy over `[a,b)` with `a >= T0` (active
  jobs at the basis suffice) but not pre-join occupancy or per-step processing residence of steps
  active at the basis. An Engine accumulator (M1′) would recover cumulative occupancy *as of an
  observation*, not interval finality, command provenance or arbitrary windows.
- **R11 — throughput basis.** `throughputPerTick` is over observed supported time from run start; a
  period rate over a requested horizon differs by definition, not by Engine error.
- **R12 — shared-owner trigger.** A single core verification/governance requirement with acceptance
  evidence that fixes a named method could justify a shared owner without two shipped consumers.
  An illustrative objective or two call sites in one consumer does not.
- **R13 — downstream trigger narrowing.** A same-holder Game use needing method-specific sufficiency
  or retained attempt results with definition identity would satisfy the evidence/provenance brief's
  literal trigger even without a non-holder; restating the trigger as "reliance by a non-holder"
  would narrow it.

## 5. What evidence would discriminate

- **Supports consumer-local/staged (M3/M4/M5):** only one concrete forcing consumer; basis choices
  (population, denominator, availability, capped vs uncapped slot-ticks) are still consumer-specific
  and unfixed; no cross-boundary reliance on the same named method; definitions are pure functions of
  supported evidence, so promotion needs no Engine change; Charter §6 threshold not met for M2.
- **Supports Engine/shared (M1/M1′/M2):** a core requirement with acceptance evidence fixing one
  method; two concrete consumers needing identical semantics; a late-join/passive consumer needing
  pre-basis cumulative occupancy; repeated, error-prone interval protocols that only the producer can
  make impossible rather than detectable.
- **Expected reviewer prior (falsifiable):** M4 combined with M5 is the best-supported current answer,
  with M1′ viable-but-not-now and M2 not now. It survives only if (a) the admission test is not so
  narrow that it can never fire (one core requirement must suffice); (b) temporal sufficiency is
  claim-scoped and does not make live current-state or "so far" diagnostics depend on controller
  closure; (c) the conflict with the Game plan's "not game-owned by default" is named as a required
  reconciliation, not treated as live; (d) the evidence/provenance trigger is not narrowed; and (e) no
  universal prohibition on future Engine measurement is asserted.
