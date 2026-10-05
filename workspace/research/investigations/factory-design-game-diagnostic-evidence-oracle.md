# Game diagnostic evidence — fixture corpus and oracle (pre-evaluation)

> **Custody:** temporary research evidence for the factory-design game diagnostic-evidence
> investigation. Not maintained state, not a fixture contract, not Engine semantics.
> **Written before any candidate evidence contract was implemented or evaluated.** Every value
> below is hand-derived from `docs/architecture/engine-semantics.md` (§2 selection, §3
> decomposition, §4 cascade ordering) unless it is explicitly marked *pinned* (already a
> hand-derived expectation in the tracked `research-experiments` corpus at the research baseline)
> or *run-derived* (an authoritative completion tick read from a run because hand derivation was
> impractical; such values are used only as intervention ground truth, never as a candidate's
> expected output).

## Shared inputs

- Routing **R** (the strategy-space reference): `CUT` 3 ticks → `ASSEMBLE` 4 → `INSPECT` 5,
  one product, one linear operation, one order of quantity 12 submitted at tick 0, run to
  quiescence, every supported event retained from sequence 1 unless stated otherwise.
- All resources online throughout; `setupTime` 0; `capacityLiters` absent.
- Resource identity = 1-based authored position (`LinearRoutingFamily`). An added resource is
  appended after every existing resource, so existing identities never change.
- `u_k` = the job with `ordinalWithinOrder` `k-1`. The completing job is identified by the
  `ORDER_COMPLETED` payload's `jobId`, never by ordinal arithmetic.

## Fixtures

| Id | Design (authored order) | Completion | Boundary used for Q1/Q3 |
|---|---|---:|---|
| G1 | Cutter, Assembler, Inspector | 67 *pinned* | 33 |
| G2 | Cutter, Assembler, Inspector 1, Inspector 2 (strategy-space S1) | 56 *pinned* | 17 |
| G3 | G2 + Assembler 2 | 45 | — |
| G4 | Cutter, Twin Assembler (`ASSEMBLE`, concurrency 2), Inspector 1, Inspector 2 (S3) | 45 | 9 |
| G5 | Cutter, Assembler 1–6, Shared (`ASSEMBLE`+`INSPECT`) | 67 *pinned* | — |
| G6 | Cutter, Assembler, Inspector, Shared (`ASSEMBLE`+`INSPECT`) (S2, authored order) | 47 *pinned* | 17 |
| G7 | Routing `CUT` 3 → `ASSEMBLE` 5 → `INSPECT` 5; Cutter, Assembler, Inspector | 68 | — |
| G8 | G1 + Assembler 2 | 67 | 11 |
| G9a/G9b | tracked `resource-order/shared-resource-authored` / `-reversed` (routing `CUT` 2, `ASSEMBLE` 3, `INSPECT` 2; quantity 2) | 11 / 9 *pinned* | — |
| G10 | G2 with every supported event through tick 17 drained and discarded (late joiner) | 56 | 17 |
| G11a | tracked `three-step/multi-eligible-waiting` | — | 5 *pinned* |
| G11b | tracked `three-step/long-step-and-parallel-capacity` | 26 *pinned* | 10 *pinned* |

### Derivations not already pinned

**G1** (also pinned): `u_k` cut `3(k-1)`–`3k`; assembly start `max(3k, 4k-1) = 4k-1`, end `4k+3`;
inspection start `max(4k+3, 5k+2) = 5k+2`, end `5k+7`; completion `67`.

**G1 + Cutter 2** → 67. `CUT` becomes multi-eligible; units reach `ASSEMBLE` earlier but assembly
still runs back to back from 3 (end `4k+3`) and inspection from 7 (end `5k+7`).
**G1 + Assembler 2 (= G8)** → 67. Arrivals every 3 against two 4-tick assemblers: each arrival finds
a free assembler (lower identifier when both are free), so assembly ends at `3k+4`; inspection start
`max(3k+4, 5k+2) = 5k+2`, so completion stays 67.
**G1 + Inspector 2 (= G2)** → 56 *pinned*.
**G1 + Assembler 2 + Inspector 2** → 45: assembly ends `3k+4`; two 5-tick inspectors with arrivals
every 3 never make a unit wait (odd units to Inspector 1, even to Inspector 2); `u_12` inspected
40–45.
**G1 + Cutter 2 + Inspector 2** → 56: assembly still ends `4k+3` (51 for `u_12`) and two inspectors
never make a unit wait.

**G2** (S1): assembly as in G1 (end `4k+3`); Inspector 1 takes odd units and Inspector 2 even units,
no inspection waits; completion 56 *pinned*. **G2 + Cutter 2** → 56 (assembly unchanged).
**G2 + Assembler 2 (= G3)** → 45 (assembly ends `3k+4`; inspection never waits).
**G2 + Inspector 3** → 56 (assembly unchanged). **G2 − Inspector 2 (= G1)** → 67.

**G3**: no assembly or inspection waits; `u_12` cut 33–36, assembled 36–40, inspected 40–45.
**G3 + Cutter 2** → 37: pairs `(u_{2m-1}, u_{2m})` are cut by tick `3m`; both assemblers are busy
until `3m+1`, so pair `m` is assembled `4m-1`–`4m+3` and inspected `5m+2`–`5m+7`; pair 6 ends at 37.
**G3 + Assembler 3** → 45, **G3 + Inspector 3** → 45 (cutting paces the run).

**G4** (S3): two assembly slots on one resource, arrivals every 3, 4-tick steps: no assembly wait;
inspection as in G3; completion 45. **G4 + Cutter 2** → 37 (pairs wait for the twin until `4m-1`;
each slot release is its own cascade trigger, so both units of a pair start together);
**G4 + Assembler** → 45; **G4 + Inspector 3** → 45. At tick 9 the twin assembler has two active
jobs (`u_2` 6–10 and `u_3` 9–13) while its completion-credited `busyTicks` is 4 (only `u_1`
completed); at completion `busyTicks` is 48 against 45 elapsed ticks.

**G5** (occupancy counterexample, pinned): completion 67; **+ Cutter 2** → 67 *pinned*;
**+ Inspector** → 45 *pinned*; **+ Assembler 7** → 67 (an idle lower-numbered assembler always
exists, so Assembler 7 never assembles and nothing else changes).

**G6** (S2): completion 47 *pinned*; schedule table pinned in `CapacityCorpus`. Single-addition
interventions (+Cutter 2, +Assembler 2, +Inspector 2) and the alternative-order completion (46)
are *run-derived* and used only as intervention ground truth.

**G7** (co-binding): cut `3(k-1)`–`3k`; assembly start `max(3k, 5k-2) = 5k-2`, end `5k+3`;
inspection start `max(5k+3, 5(k-1)+8) = 5k+3`, end `5k+8`. Every inspection after the first starts
at the tick at which the unit becomes ready **and** the inspector is released by the previous unit;
completion `68`.
**+ Cutter 2** → 68 (assembly already back to back from 3).
**+ Assembler 2** → 68 (assembly ends `3k+5`, but the single inspector still starts `5k+3`).
**+ Inspector 2** → 68 (inspection never waits, but assembly still ends `5k+3`).
**+ Assembler 2 + Inspector 2** → 46 (assembly ends `3k+5`; two inspectors never make a unit
wait; `u_12` inspected 41–46).
**+ Cutter 2 + Inspector 2** → 68.

**G8** (G1 + Assembler 2): completion 67. At tick 11: `u_4` is being cut (9–12), `u_5`…`u_12`
wait for the cutter (8); Assembler has `u_3` (9–13); Assembler 2 finished `u_2` at 10 and is idle;
no unit waits for `ASSEMBLE`; the inspector has `u_1` (7–12) and `u_2` (ready at 10) waits for it.
**G8 − Assembler 2 (= G1)** → 67.

**G2 at tick 17**: `u_6` is being cut (15–18), `u_7`…`u_12` wait for the cutter (6); the assembler
has `u_4` (15–19) and `u_5` (ready at 15) waits for it; Inspector 1 has `u_3` (15–20); Inspector 2
finished `u_2` at 16 and is idle; no unit waits for `INSPECT`.

**G6 at tick 17**: `u_6` is being cut, `u_7`…`u_12` wait for the cutter (6); `u_4` finished
assembly at 17 and waits for `INSPECT`, whose eligible set is {Inspector, Shared}: Inspector has
`u_3` (13–18) and Shared is assembling `u_5` (15–19). Every resource's own `queueDepth` except the
cutter's is 0; the single waiting `INSPECT` unit is pending multi-eligible work naming both
eligible resources.

**G10**: same run as G2; events 1..(cursor at tick 17) are discarded, later events retained.

## Oracle for the fixed diagnostic questions

### Q1 — what work is waiting, and for which operation step?

Step-first, at the named boundary; resource attribution only for a single-member eligible set.

| Fixture @ tick | `CUT` | `ASSEMBLE` | `INSPECT` |
|---|---|---|---|
| G1 @ 33 | 0 | 3 (Assembler) *pinned* | 1 (Inspector) *pinned* |
| G2 @ 17 | 6 (Cutter) | 1 (Assembler) | 0 |
| G6 @ 17 | 6 (Cutter) | 0 | 1 (shared: {Inspector, Shared}; not either resource's queue) |
| G8 @ 11 | 8 (Cutter) | 0 | 1 (Inspector) |
| G11a @ 5 | 0 | 3 (shared: both assemblers; every own `queueDepth` 0) *pinned* | 0 |

### Q2 — the constraint, under a **named** method

Ground truth for the bounded reference is **controlled marginal intervention**: the steps where
appending one comparable resource reduces completion.

| Base | + cutter | + assembler | + inspector | Steps whose single addition helps |
|---|---:|---:|---:|---|
| G1 (67) | 67 | 67 | 56 | `INSPECT` |
| G2 (56) | 56 | 45 | 56 | `ASSEMBLE` |
| G3 (45) | 37 | 45 | 45 | `CUT` |
| G4 (45) | 37 | 45 | 45 | `CUT` |
| G5 (67) | 67 | 67 | 45 | `INSPECT` |
| G7 (68) | 68 | 68 | 68 | **none** — only the joint addition (46) helps |

Method-relative expectations for candidates that use a named single-run method:

- **Most-occupied eligibility pool** (`EligibilityPoolOccupancyOracle.maximumOccupancyPools`):
  G5 names the `CUT` pool (36/67 against 108/469) *pinned*; intervention truth is `INSPECT`.
  A candidate that labels this measurement a constraint/bottleneck is falsified by G5.
- **Most-occupied resource** (`ProcessingOccupancyOracle`, occupied/capacity job-ticks): G7 ties
  Assembler and Inspector at 60/68; neither single addition helps. Any "constraint" label is
  falsified by G7.
- **Completion-credited `busyTicks` ÷ elapsed ticks** (naïve utilization): G4 at completion gives the
  twin assembler 48/45 > 1 and ranks it first; intervention truth is `CUT`. Falsified by G4.
- **Largest total wait before a step** (`DispatchProfileOracle` waits): every fixture's largest is
  `CUT` (198, the release-at-once backlog) — falsified by G1, G2, G5, G7; excluding `CUT`, G7 names
  `ASSEMBLE` (132 against 0) — falsified by G7.
- **Binding-chain trace** (defined in the experiment; walk back from the `ORDER_COMPLETED` job:
  a step that started later than its job became ready links to the step whose completion released
  the dispatched resource at that tick; a step that started when its job became ready links to the
  job's previous step; a step that started exactly when its job became ready **and** its resource
  was released at that tick by a different job is a tie). Expected: G1 `INSPECT` (Inspector, 7–67);
  G2 `ASSEMBLE` (Assembler, 3–51); G3 `CUT`; G4 `CUT`; G5 `INSPECT` (Shared); G6 tie at tick 33
  (`u_11` became ready for `ASSEMBLE` when Shared was released by `u_8`'s inspection) → no unique
  step; G7 tie at every inspection start → no unique step.

### Q3 — is a named idle resource starved, surplus, or not decidable?

| Case | Single-run evidence at the boundary | Surplus decidable from one run? | Controlled removal | Truth |
|---|---|---|---|---|
| G2 @ 17, Inspector 2 | idle, 0 of 1 active, no unit waits for a step it serves | **No** | G2 − Inspector 2 → 67 (+11) | not surplus for completion |
| G8 @ 11, Assembler 2 | idle, 0 of 1 active, no unit waits for a step it serves | **No** | G8 − Assembler 2 → 67 (0) | surplus for completion under this workload |
| any fixture at quiescence | every resource idle, nothing waits, order complete | — | — | "idle, order complete"; not starvation |

The two single-run evidence rows are indistinguishable in kind, so any single-run "surplus" claim
is a guess; "starved" is defensible only as "idle with no eligible work waiting" and is wrong at
quiescence.

### Q4 — the largest measured delay the evidence can distinguish

Waiting = dispatch tick − readiness tick (previous step's completion, or order acceptance for the
first step); processing = completion − dispatch. Both from supported events.

| Fixture | Completing-unit lead time = wait/process by step | Per-step total wait `CUT`/`ASSEMBLE`/`INSPECT` |
|---|---|---|
| G1 | 67 = 33+3 · 11+4 · 11+5 | 198 / 66 / 66 |
| G2 | 56 = 33+3 · 11+4 · 0+5 | 198 / 66 / 0 *pinned* |
| G3 | 45 = 33+3 · 0+4 · 0+5 | 198 / 0 / 0 |
| G4 | 45 = 33+3 · 0+4 · 0+5 | 198 / 0 / 0 |
| G5 | 67 = 33+3 · 0+4 · 22+5 | 198 / 0 / 132 *pinned* |
| G7 | 68 = 33+3 · 22+5 · 0+5 | 198 / 132 / 0 |
| G8 | 67 = 33+3 · 0+4 · 22+5 | 198 / 0 / 132 |

The largest measured delay is waiting for `CUT` in every fixture — the one-order, release-at-once
backlog — while `CUT` is the intervention-truth constraint only in G3/G4. A measured delay is
therefore never licensed as a constraint label.

### Q5 — controlled one-variable pairs

| Pair | Authored change | Completion |
|---|---|---|
| G1 → G1 + Inspector 2 | one inspector appended | 67 → 56 |
| G1 → G1 + Cutter 2 | one cutter appended | 67 → 67 |
| G1 → G1 + Assembler 2 | one assembler appended | 67 → 67 |
| G2 → G3 | one assembler appended | 56 → 45 |
| G3 → G3 + Cutter 2 | one cutter appended | 45 → 37 |
| G2 → G1 | Inspector 2 removed | 56 → 67 |
| G8 → G1 | Assembler 2 removed | 67 → 67 |
| G9a → G9b | resource order reversed; no resource added or removed | 11 → 9 *pinned* |

The truthful statement is the authored change beside the outcome change; a mechanism is not
licensed by the pair.

### Q6 — confounded pairs: which attribution must be refused?

| Pair | Changes | Completion | Single-change truth | Why unique attribution must be refused |
|---|---|---|---|---|
| G1 → G1 + Assembler 2 + Inspector 2 | 2 | 67 → 45 | +A: 0; +I: −11 | Sequential attribution depends on order: (A 0, I −22) or (I −11, A −11) |
| G1 → G1 + Cutter 2 + Inspector 2 | 2 | 67 → 56 | +C: 0; +I: −11 | The pair alone cannot separate the two changes; only the single-change runs can |
| G7 → G7 + Assembler 2 + Inspector 2 | 2 | 68 → 46 | +A: 0; +I: 0 | Pure interaction: neither change alone changes completion |
