# Factory-design game diagnostic evidence contract — investigation report

> **Research status:** `READY` (the question stays admitted and unresolved until its consequence is
> reconciled; this report does not change the register).
>
> **Research baseline:** live `main` `9e9e4678b18286cea83387b5ffc38741640f0db5` at the start of the
> investigation.
>
> **Final repository recheck:** live `main` `306205c33ef93f0433a26c92f85cbd82eadd27f8`. The only change
> since the baseline is test coverage in `research-experiments` plus a private → package-private
> visibility change to `ExperimentRunner.recordOf`; no Engine, Factory or substrate behavior changed.
> `main` was merged into the workspace, and the experiment was rerun twice against that content: once in
> the earlier session, and again for this report on the JDK 21 compatibility floor (`gradle:9-jdk21`
> image, `./gradlew :research-experiments:test` with the workspace experiment sources): 147 tests,
> 0 failures, 0 skipped, and all five generated result files byte-identical to the committed results.
> The conclusion is unchanged.
>
> **Authority:** research evidence only. This report is not accepted architecture, product direction, a
> game requirement, or implementation commitment. It decides no analytics ownership.
>
> **Adversarial-review status:** required, not yet performed (High risk in the register). The
> self-challenge below is self-administered and has no independent weight.
>
> **Owner walkthrough:** **not performed.** The blinded packet is persisted (see
> [Product-owner walkthrough](#product-owner-walkthrough)); the brief's owner smoke-test exit criterion
> is still open. **The repository owner should not read this report before doing that walkthrough.**

## Question

What minimum player-facing diagnostic evidence contract can the game build from supported non-spatial
simulation facts, so that every displayed diagnosis is mechanically traceable, states its method, and
can refuse unsupported causal attribution?

The question is unchanged from the brief. One refinement emerged from the evidence and is used
throughout: the brief's three candidates were run as five variants (two named methods for candidate 2;
candidate 3 with and without a single-run pacing verdict), plus a deliberately naive control.

## Decision at stake

1. Whether the smallest non-spatial playable path can show a mechanically truthful, inspectable
   diagnostic surface built from current supported evidence.
2. Which bounded player-facing evidence requirements should later be promoted into the game consumer
   plan.
3. Separately, for the reopened simulation-analytics ownership research: whether that surface needs a
   concrete named measurement whose owner cannot be left local or undefined.

## Scope and non-goals

In scope: one product with a linear three-step routing, one fixed order released at once, all
resources online, no setup and no transfer. The evidence is supported observations, supported events
and the published model. The scope also covers candidate information contracts (not visual designs),
the six audits from the brief, and a blinded owner packet.

Not in scope, and not done: game, analytics or Engine implementation; analytics ownership; changes to
`combinedQueueDepth`, dispatch or ranking; a universal utilization formula or bottleneck algorithm;
transfer or spatial diagnostics; scoring, levels or tutorials; player-comprehension claims; updates to
the register, planning or architecture; durable reconciliation.

## Executive conclusion

**Mechanical result: positive and bounded.** One candidate contract passes all six audits on every
corpus fixture and needs no reusable measurement. That candidate is **C3b, the minimal claim–evidence
bundle**. It shows only:

- direct facts;
- boundary counts;
- the event intervals of the completing unit and of each resource;
- one-change comparisons stated as change plus outcome;
- explicit refusals.

Every derived statement names its derivation and cites its evidence. C3b gives no single-run
limiting-step, bottleneck, surplus or mechanism verdict. It refuses those, and it refuses per-change
attribution for multi-change comparisons. It is best described as **the factual basis plus explicit,
evidence-cited refusal**, not as a diagnosis engine. The decisive difference from facts-only (C1) is
that C3b refuses **explicitly** where C1 stays silent.

**Exit-criterion status: incomplete only because the owner walkthrough remains.** The brief needs
both the deterministic audits and the product-owner smoke test. Only the audits are done.

**No concrete reusable-measurement demand was established.**

> The first non-spatial playable diagnostic contract does not currently establish a concrete reusable
> analytics requirement.

C3b needs no normalized utilization, occupancy, run-total aggregate or constraint verdict. Its derived
statements are counts or groupings of direct facts at one observation, or exact intervals between
supported events of one job-step occurrence. These definitions are fully determined by current
supported semantics and have no interval, population or normalization choices.

**Confidence:**

- **High** for the falsifications and for the mechanical pass within the stated corpus scope.
- **Medium** that the requirement set is sufficient for a first internal playable slice. That depends
  on the owner walkthrough and on a refusal-heavy surface being usable at all.
- **Low to medium** for anything beyond one product, one order, a linear routing and release at once.

Answers to the handoff's questions:

| # | Question | Answer |
|---|---|---|
| 1 | What must the player be shown? | Waiting by step at a boundary, step first. Slots in use per resource. Idle resources and whether any waiting work could use them. Progress and completion. The completing unit's per-step wait and processing intervals. One-change comparisons. Explicit refusals where single-run or confounded evidence licenses no answer. See [Requirement set](#result-the-playable-diagnostic-requirement-set). |
| 2 | Which items are direct facts, research-local derivations or named interpretations? | See [Concrete reusable-measurement demand](#concrete-reusable-measurement-demand). Direct facts and boundary counts dominate. Event intervals are exact readings of supported events. **No named interpretation is required.** |
| 3 | What evidence licenses each derived claim? | The [claim-to-evidence matrix](#claim-to-evidence-matrix) cites the observation boundary, event sequences, design facts and fields for every statement. |
| 4 | Which diagnoses must refuse? | The single-run limiting step or constraint; surplus or "starved"; per-change attribution for more than one change; the mechanism of a one-change outcome; and any interval statement when the event window is incomplete. |
| 5 | Is a concrete named measurement required? | **No.** |
| 6 | If yes, specify it. | Not applicable. Conditional, *not established* candidates that a later slice could raise are listed for the analytics research, with their known material basis choices. |
| 7 | If no, what weaker contract suffices? | C3b's invariant: every statement is a direct fact, a boundary count, an event interval, a comparison or a refusal; each derived statement names its derivation and cites its evidence. |

## Repository evidence

Each item below is a repository fact at the final recheck, unless labeled otherwise.

**Supported runtime surface** (`docs/architecture/runtime-contract.md`; `product/domains/factory/.../process/`):

- `RuntimeObservation` carries `metadata`, `resources`, `orders`, `jobs`, `pendingWork` and
  `performance`.
- `ResourceObservation` exposes `state`, `concurrency`, `activeJobIds`, its own `queueDepth`,
  `capacityLiters`, `setupTime` and `busyTicks`.
- `PendingWorkObservation` exposes the pending job and its eligible machine ids.
- `RuntimeObservationMetadata` exposes `currentTime` and `latestEventSequence`.
- The supported events used here are `ORDER_ACCEPTED` (with `jobIds`), `JOB_DISPATCHED` (job, order,
  machine, step index), `JOB_STEP_COMPLETED` and `ORDER_COMPLETED` (naming the completing `jobId`).
- Runtime event delivery drains rather than retains. The runtime contract defines no retained history
  and requires recovery to detect dropped events, not treat an incomplete sequence as complete.

**Selection and waiting semantics** (`docs/architecture/engine-semantics.md`):

- §2 rules 2–4 rank candidates by immediate acceptance, then `combinedQueueDepth`, then `MachineId`.
- §2 rule 7: before binding, a multi-eligible step never occupies a per-machine queue, and a
  single-eligible step never occupies the shared backlog.
- `combinedQueueDepth` is an internal ranking key in `FactoryHandler`. **No supported observation
  exposes it.**

**`busyTicks` is completion-credited** (§10.1 rule 1; `FactoryHandler.handleTaskEnd`). A finished
step's duration is added at completion, so a running step contributes nothing until it ends.

**Repository tension (not changed by this investigation).** Several durable surfaces describe that
counter as utilization or as a bottleneck basis:

- A `FactoryHandler` comment says crediting at completion makes `busyTicks` express "real utilization"
  and ties it to identifying the active bottleneck from supported observations.
- Engine semantics §10 and the §10.2 register name the result "`busyTicks` / utilization" and refer to
  an "existing bottleneck/capacity interpretation".
- The runtime contract lists "utilization facts" among performance facts.
- Factory Design §10.2 names a "Maximum utilization" verification objective. That is a separate
  potential Governance/verification demand, not the game's.

The corpus shows the counter is not an elapsed-normalized or instantaneous utilization (G4, G11b
below). This is a reconciliation candidate, not a research result to enforce here.

**Product and planning context:**

- `docs/planning/factory-design-game-consumer.md` §4–§5 makes player-facing presentation and
  explanation of supported facts game-owned.
- It also leaves "reusable derived measurement (longitudinal aggregation, utilization/occupancy
  intervals, diagnosis, run comparison)" **unresolved and not game-owned by default**, and forbids the
  game from implementing generic analytics locally as though it owned the semantics.
- Attempt history and comparison belong to the Challenge layer.
- `docs/planning/factory-design-game-vertical-slice.md` names this question as the remaining product
  blocker and admits no implementation.

**Concluded strategy-space reference** (`docs/research/investigations/factory-design-game-strategy-space.md`).
It supplies the S1/S2/S3 designs, the intervention sequence and the occupancy counterexample. Its
accepted qualifications are preserved here:

- shared eligibility is constructive, not necessary;
- occupancy is descriptive, not a constraint definition;
- the most-occupied pool can be the wrong constraint;
- result-affecting resource identity and order must be explicit;
- mechanical truth is not comprehension;
- the reference is a bounded case.

**Research substrate** (`product/research-experiments`). These tracked research-local derivations are
used or audited here:

- `WaitingWorkByStepOracle`
- `DispatchProfileOracle`
- `EligibilityPoolOccupancyOracle`
- `ProcessingOccupancyOracle`
- `CompletionTickOracle`

The tracked fixtures used are `CapacityCorpus` and `StarterCorpus`. A substrate derivation proves
feasibility and ground truth only; it is not an Engine fact or game-owned analytics (researching.md
§4).

**Surfaces not inspected:** no Challenge production code path was exercised. Comparisons here are
built from game-owned design facts plus completion facts, which matches the Challenge layer's
ownership; the Challenge substrate's own comparison output was not audited.

## Fixture, model and input provenance

The repository revision is the final recheck commit above; the runtime exposes no Engine-definition
identifier (runtime contract).

- **Fixture corpus:** the experiment class `GameDiagnosticCorpus`.
- **Hand-derived oracle:** committed in the oracle note before any candidate was implemented or
  evaluated.

| Id | Design (authored resource order) | Source |
|---|---|---|
| G1 | Cutter, Assembler, Inspector | tracked `CapacityCorpus.DEDICATED_LINE` |
| G2 | Cutter, Assembler, Inspector 1, Inspector 2 (S1) | tracked `TWO_INSPECTORS` |
| G3 | G2 + Assembler 2 | appended |
| G4 | Cutter, Twin Assembler (`ASSEMBLE`, concurrency 2), Inspector 1, Inspector 2 (S3) | workspace |
| G5 | Cutter, Assembler 1–6, Shared (`ASSEMBLE`+`INSPECT`) | tracked `SHARED_ONLY_INSPECTION` (occupancy counterexample) |
| G6 | Cutter, Assembler, Inspector, Shared (S2, authored order) | tracked `SHARED_ASSEMBLE_INSPECT` |
| G7 | routing `CUT` 3, `ASSEMBLE` 5, `INSPECT` 5; one resource each | workspace (co-binding) |
| G8 | G1 + Assembler 2 | appended |
| G9a / G9b | tracked shared-resource pair, authored and reversed order (routing 2/3/2, quantity 2) | tracked `SHARED_RESOURCE` |
| G10 | G2 observed by a late joiner (events through the mid-run boundary drained and discarded) | workspace |
| G11a / G11b | tracked `three-step/multi-eligible-waiting` and `three-step/long-step-and-parallel-capacity` | tracked `StarterCorpus` |

- **Routing:** `CUT` 3 → `ASSEMBLE` 4 → `INSPECT` 5, unless the row says otherwise.
- **Workload:** one order of 12 submitted at tick 0, run to quiescence. Every supported event is
  retained from sequence 1, except in G10.
- **Resource identity:** the 1-based authored position. Added resources are appended, so existing
  identities never change.
- **Script:** each fixture has an explicit ordered script (submit; optional advance to a mid-run tick,
  observe, capture or discard events; advance to quiescence; capture events). The runner checks that
  the declared event window matches the actual capture.

**Comparison pairs (24).** Every corpus pair is listed below; the counts are grouped by the oracle's
fixed questions.

- **18 single additions:** one cutter, assembler or inspector appended to each of G1, G2, G3, G4, G5
  and G7.
- **Q5 table (controlled pairs).** These 8 named pairs (three single additions, two removals, two
  migration pairs and one reorder) are the ones the oracle table lists:
  - G1 + Inspector 2: 67 → 56.
  - G1 + Cutter 2: 67 → 67.
  - G1 + Assembler 2: 67 → 67.
  - G2 → G3 (one assembler added; constraint migration from `INSPECT` to `ASSEMBLE`): 56 → 45.
  - G3 + Cutter 2 (migration to `CUT`): 45 → 37.
  - G2 − Inspector 2: 56 → 67.
  - G8 − Assembler 2: 67 → 67.
  - G9a → G9b (order only): 11 → 9.
- **Q6 (confounded pairs), 3 pairs:**
  - G1 + Assembler + Inspector: 67 → 45.
  - G1 + Cutter + Inspector: 67 → 56.
  - G7 + Assembler + Inspector: 68 → 46.

Some named Q5 pairs are also single additions. The corpus therefore holds 24 pairs in total: 18 single
additions, 2 removals, 1 reorder and 3 confounded pairs.

**Interventions.** All hand-derived completions and interventions were confirmed by runs. G6's
single-addition interventions were read from runs and used only as intervention ground truth: + cutter
44, + assembler 45, + inspector 45. So were G6's completions across all 24 resource orders: 46 for 8
orders and 47 for 16.

## Brief-required case coverage

| Required case | Fixture(s) |
|---|---|
| True capacity constraint | G1 (`INSPECT`), G2 (`ASSEMBLE`), G3/G4 (`CUT`), by single-addition intervention |
| Capacity added at the constraint | G1 + Inspector 2 (67 → 56); G2 + Assembler 2 (56 → 45); G3 + Cutter 2 (45 → 37) |
| Capacity added away from it | G1 + Cutter 2 and G1 + Assembler 2 (67 → 67); G2 + Cutter 2 and G2 + Inspector 3 (56 → 56) |
| Constraint migration after relief | G1 → G2 → G3 → G3 + Cutter 2 (`INSPECT` → `ASSEMBLE` → `CUT`) |
| Starvation versus surplus | G2 Inspector 2 at the mid-run boundary (removing it costs 11 ticks) versus G8 Assembler 2 (removing it costs 0): the same single-run evidence, opposite truths |
| Multi-eligible waiting where per-machine depth misleads | G6 (one `INSPECT` unit pending for {Inspector, Shared}, every own `queueDepth` except the cutter's is 0); G11a (3 shared units, all own queues 0) |
| Long unfinished step where `busyTicks` misleads | G4 at tick 9 (twin assembler 2 of 2 slots active, `busyTicks` 4); G11b (2 of 2 slots active at tick 3, `busyTicks` 0) |
| `concurrency > 1` defeats naïve utilization | G4 at completion (`busyTicks` 48 against 45 elapsed); G11b (36 against 26) |
| Deliberately confounded pair | the three Q6 pairs, including a pure interaction (G7: each single change 0, together −22) |

## Candidate evidence contracts

All candidates share one factual basis. At the mid-run boundary it holds: waiting by step, slots in use
per resource, idle resources against eligible waiting, and progress. At closing it holds: outcome,
idleness, the completing unit's decomposition and each resource's activity intervals.

| Id | Contract |
|---|---|
| **C1** facts only | The shared basis; plain comparisons (change set + completion delta); no verdict, aggregate or refusal |
| **C2a** facts + named pool occupancy | C1 + run-total waits by step + "Bottleneck (method: most occupied eligibility pool)" with job-tick numbers |
| **C2b** facts + named completion chain | C1 + run-total waits by step + "Pacing step (method: completion chain)": trace the completing job back through readiness and resource releases; refuse at a tie or when capacity waits fall at more than one step |
| **C3a** bundle with completion chain | C2b + explicit refusals: surplus, the counterfactual "would adding capacity help", per-change attribution, mechanism; tie-break rule stated on an order change |
| **C3b** minimal bundle | C1 + explicit refusals: single-run limiting step, surplus, per-change attribution, mechanism, interval claims without a complete event window; tie-break rule stated on an order change |
| **N** naive control | `busyTicks` ÷ elapsed as "utilization %"; own queue + pending entries naming a resource as that resource's "queue"; highest utilization = "bottleneck"; idle = "surplus"; comparison attributed to the first change (or to "variation" for order-only changes) |

N is not a candidate. It exists to prove that every audit can fail.

The named derivations behind derived statements are:

- `waiting-work-by-operation-step` (tracked substrate);
- `idle-resources-and-eligible-waiting`;
- `job-step-occurrences-from-supported-events`;
- `completing-unit-lead-time-decomposition`;
- `completion-chain-from-supported-events`;
- `dispatch-profile` and `eligibility-pool-occupancy` (tracked substrate, C2/C3a only).

Each workspace definition declares its inputs (published model, observations, supported events) and
its refusal conditions. None re-decides a dispatch: which resource took which step is read from
`JOB_DISPATCHED`.

## Oracle derivations

The complete oracle note was fixed before evaluation. It derives every value by hand from Engine
semantics §2–§4, except values marked *pinned* (existing tracked expectations) or *run-derived*
(G6 interventions only). Summary:

- **Q1 — waiting work by step, step first.** Resources are named only for a single-member eligible
  set.

  | Fixture | `CUT` | `ASSEMBLE` | `INSPECT` |
  |---|---|---|---|
  | G1 @ 33 | 0 | 3 (Assembler) | 1 (Inspector) |
  | G2 @ 17 | 6 (Cutter) | 1 (Assembler) | 0 |
  | G6 @ 17 | 6 (Cutter) | 0 | 1, shared by {Inspector, Shared} and in neither resource's queue |
  | G8 @ 11 | 8 (Cutter) | 0 | 1 (Inspector) |
  | G11a | 0 | 3, shared (every own `queueDepth` is 0) | 0 |

- **Q2 — the constraint.** Ground truth for this bounded reference is controlled marginal
  intervention: the steps where appending one comparable resource reduces completion.
  - G1: `INSPECT`.
  - G2: `ASSEMBLE`.
  - G3 and G4: `CUT`.
  - G5: `INSPECT`.
  - G7: **none**. Only the joint addition helps (68 → 46).

  Method-relative expectations fixed in advance:
  - Pool occupancy names `CUT` in G5 (36 of 67 job-ticks, against 108 of 469). Falsified.
  - Resource occupancy ties G7's assembler and inspector at 60 of 68 while neither single addition
    helps. Falsified.
  - `busyTicks` ÷ elapsed ranks G4's twin assembler first at 48/45. Falsified.
  - The largest total wait is always `CUT` (198, the release-at-once backlog). Falsified in G1, G2, G5
    and G7.
  - The completion chain gives G1 `INSPECT`, G2 `ASSEMBLE`, G3 and G4 `CUT`, G5 `INSPECT`, and ties
    in G6 and G7.
- **Q3 — idle resources.** G2's Inspector 2 and G8's Assembler 2 are each idle, with nothing waiting
  for a step they serve. Removing them costs 11 ticks and 0 ticks respectively. A single-run "surplus"
  claim is therefore a guess. "Starved" is defensible only as "idle with no eligible work waiting", and
  is false at quiescence.
- **Q4 — largest measured delay.** The completing unit's lead time splits into per-step wait and
  processing from supported events:
  - G1: 67 = 33+3 · 11+4 · 11+5.
  - G7: 68 = 33+3 · 22+5 · 0+5.

  The largest measured wait is waiting for `CUT` in every fixture, yet `CUT` is the
  intervention-truth constraint only in G3 and G4. A measured delay is never licensed as a constraint
  label.
- **Q5 — controlled pairs.** The truthful statement is the authored change beside the outcome change;
  no mechanism is licensed.
- **Q6 — confounded pairs.**
  - G1 + A + I: the singles give 0 and −11, together −22. Sequential attribution depends on the order
    of attribution.
  - G1 + C + I: the pair alone cannot separate the two changes.
  - G7 + A + I: a pure interaction (each single change gives 0).

## Audit results

These are generated by the experiment (`audit-matrix.md`) and were reproduced byte for byte at the
final recheck:

| Contract | Traceability | Reconstruction | Counterexample | Refusal | Controlled mutation | Terminology |
|---|---|---|---|---|---|---|
| C1 facts only | PASS | PASS | PASS | **FAIL (5)** | PASS | PASS |
| C2a named pool occupancy | PASS | PASS | **FAIL (2)** | **FAIL (7)** | PASS | **FAIL (12)** |
| C2b named completion chain | PASS | PASS | PASS | **FAIL (5)** | PASS | PASS |
| C3a bundle + completion chain | PASS | PASS | PASS | PASS | PASS | PASS |
| **C3b minimal bundle** | **PASS** | **PASS** | **PASS** | **PASS** | **PASS** | **PASS** |
| N naive control | FAIL (104) | PASS | FAIL (22) | FAIL (13) | FAIL (15) | FAIL (305) |

The audits run over 37 rendered items per contract: 13 attempts and 24 pairs.

### Claim-to-evidence matrix

The matrix is generated as `claim-evidence-matrix.md` and lists every statement of every contract on
every item. Each row gives the question, evidence kind, named method and cited evidence (observation
label, event sequences, design facts, fields). Statement counts:

| Contract | Direct fact | Boundary count | Event interval | Aggregate | Named interpretation | Comparison | Refusal | Total |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| C1 | 51 | 35 | 60 | 0 | 0 | 24 | 2 | 172 |
| C2a | 51 | 35 | 60 | 12 | 12 | 24 | 4 | 198 |
| C2b | 51 | 35 | 60 | 12 | 8 | 24 | 8 | 198 |
| C3a | 52 | 35 | 60 | 12 | 8 | 24 | 49 | 240 |
| **C3b** | 52 | 35 | 60 | 0 | **0** | 24 | 48 | 219 |
| N | 95 | 82 | 0 | 0 | 22 | 24 | 0 | 223 |

C3b emits no aggregate and no named interpretation. Its only non-substrate derivations are boundary
counts and event intervals. C1's two refusals are G10's interval refusals, which the shared derivations
emit by themselves.

### Traceability

The audit checks that every non-refusal statement cites existing evidence, that every derived statement
names one of the declared definitions, and that every cited event was actually retained. All five
candidates pass. N fails on 104 unnamed derivations, for example "Queue at Assembler", "Bottleneck:
Inspector (highest utilization)" and "Inspector 2 is surplus".

**Disclosed refinement:** the first audit run caught an unnamed derivation in **every** candidate. The
idle-resource statement combined resource state, waiting by step and eligibility without a named
definition. The fix was to name it (`idle-resources-and-eligible-waiting`); no candidate's logic
changed.

### Reconstruction

Every attempt was rebuilt from a fresh run of the same explicit fixture (model, script, window), and
each contract had to render identical statements. All contracts pass, including N; reconstruction alone
does not separate truthful from misleading.

The late-joiner check (G10 against the complete-window G2) requires that:

- current-state statements match the complete-window run;
- no event-based statement is emitted without the events before the join.

Every candidate passes. In G10 the completing-unit and activity statements become explicit refusals
("supported events 1..96 are not all retained; missing [1-39]"), and the current-state statements are
unchanged.

No derivation reads scheduler, handler or store internals, or re-runs ranking. The package boundary
test (`ResearchPackageBoundaryTest`) holds every class to the supported runtime contract, and the
experiment asserts that the workspace classes fall inside its scope.

### Counterexample

| Probe | C1 | C2a | C2b | C3a | C3b | N |
|---|---|---|---|---|---|---|
| Multi-eligible waiting not assigned to one machine; per-machine sums never exceed waiting units (G6, G11a) | ok | ok | ok | ok | ok | G6: 8 counted for 7 units; G11a: 6 for 3 (own queues 0) |
| `busyTicks` never shown as utilization (G4, G11b) | ok | ok | ok | ok | ok | 16 findings, e.g. "Twin Assembler 107%" at completion, "Assembler 138%" (G11b) |
| A single-run verdict never names a step whose single addition does not help (six intervention rows) | — | **G5 names `CUT`; G7 names `ASSEMBLE` and `INSPECT`** | ok | ok | — (no verdict) | G4, G5, G7 wrong |
| A resource-order change is reported as a change (G9) | ok | ok | ok | ok | ok | "Same equipment … (run-to-run variation)" |

### Refusal

The audit requires explicit refusal in four places:

- per-change attribution for each Q6 pair;
- surplus for G2 Inspector 2 and G8 Assembler 2;
- no Q2 verdict where the evidence ties (G6, G7);
- "measurement unavailable" for the incomplete window (G10).

Results:

- **C1 and C2b** fail by **silence**, not by a wrong claim: there is no attribution refusal on the
  three Q6 pairs and no surplus refusal for the two idle resources.
- **C2a** additionally emits verdicts on G6's and G7's ties.
- **C3a and C3b** pass.
- **N** guesses: "Completion improved by 22 ticks due to added 1 x ASSEMBLE" for G7 + A + I.

### Controlled mutation

The audit covers the 21 one-change pairs and requires that:

- the comparison reports exactly one change and the authoritative delta;
- a zero delta carries no effect words ("improv", "worsen", "due to", "because");
- the outcome statement changes when completion changes;
- a single-run verdict that moves while completion stays put is corroborated by intervention on the
  variant.

All candidates pass. N claims "improved by 0 ticks due to added 1 x CUT" and reports an order-only
change as zero changes.

**Disclosed correction** (made after the first run; both versions were defined before results were
seen):

- **Original sub-check:** "a verdict must not move when completion does not change".
- **Why it was wrong:** in G7 + assembler the completion chain moves from a tie to `INSPECT` while
  completion stays at 68. The chain evidence did change, and adding an inspector to that variant helps
  (68 → 46).
- **Corrected sub-check:** "a moved verdict must be corroborated by intervention on the variant".
- **Effect:** C3b emits no verdict, so **its pass does not depend on this correction**. C2a, C2b and
  C3a do depend on it.

### Terminology

Non-refusal statements are checked against a fixed lexicon of words whose ordinary meaning overstates
what they can license:

- utiliz-, %, efficien-: imply a normalized rate or a target the evidence does not define;
- bottleneck, constraint: imply that relieving it improves the outcome;
- blocked: the non-spatial runtime has no such state;
- starv-, surplus: imply an upstream cause or decide a counterfactual;
- because, due to, caused: assert a mechanism;
- variation: a deterministic run has no run-to-run variation;
- "queue at": multi-eligible waiting is not one machine's queue.

C2a fails on "Bottleneck" (12). N fails 305 times. The lexicon is a floor, not a semantic proof; see
the self-challenge.

## Product-owner walkthrough

**Not performed in this run.** No owner answers exist, and none were substituted.

- **Packet:** `game-diagnostic-evidence-results/walkthrough-packet.md`, generated by the experiment.
  It has 16 items shuffled with a fixed seed: 8 attempts and 8 comparisons.
- **Content:** each item shows only C3b's statements and the named derivations. Attempt items add an
  optional completion-chain line (C3a's verdict), to test whether a pacing verdict clarifies or
  misleads.
- **Questions:** the fixed diagnostic questions, plus a request to note any ambiguous or overstating
  wording.
- **Key:** `walkthrough-key.md`, kept separate and not to be read first.

The brief's exit criterion therefore remains open. A failure of interpretation by the owner would
falsify C3b unchanged, per the brief. A success would be only a single-person smoke test. Items whose
wording is most at risk, flagged here so a later revision can compare (the key is not needed for this
list):

- W6: the completing unit's "waited 33 for CUT" sits beside a refusal of the limiting step.
- W5: the tie-break rule sits next to a one-change order comparison.
- W13: "whichever of Inspector or Shared can take it first" describes multi-eligible waiting.
- W9: interval refusals appear for the late joiner.

## Concrete reusable-measurement demand

Each player-facing requirement is classified below (Direct fact, Game-local presentation/
interpretation, Reusable measurement candidate, Unsupported/must refuse):

| Requirement | Classification | Why |
|---|---|---|
| Order completion tick; completed / requested quantity | **Direct supported fact** | `OrderObservation` fields |
| Boundary label | **Direct supported fact** | `RuntimeObservationMetadata.currentTime`: the tick of the last emitted supported event, which is not the advance target. G2's advance to 17 is observed at 16, G8's 11 at 10, G11b's 10 at 3; the state is identical in between |
| Slots in use of concurrency; offline state | **Direct supported fact** | `ResourceObservation.activeJobIds`, `concurrency`, `state` |
| Waiting work by step, step first; resource named only for a single-member eligible set; multi-eligible work shown unassigned to its eligible set | **Game-local presentation** (a count/grouping of direct facts) | `JobObservation` status and current step, `PendingWorkObservation`, own `queueDepth`, grouped by the published routing. The material rule (pre-binding multi-eligible work is never one machine's queue) is already Engine semantics §2 rule 7, so no new definition is needed |
| Idle resource, and whether waiting work exists for a step it can serve | **Game-local presentation** (boundary count) | Resource state and active jobs, against waiting by step and published eligibility |
| Completing unit: per-step wait (dispatch − readiness) and processing (completion − dispatch) | **Game-local presentation of supported event intervals** | Exact pairing of `ORDER_ACCEPTED` / `JOB_STEP_COMPLETED` → `JOB_DISPATCHED` → `JOB_STEP_COMPLETED` for the job `ORDER_COMPLETED` names. No interval, population or normalization choice. Needs a complete event window from sequence 1; otherwise refuse |
| Per-resource activity intervals and max slots in use at once | **Game-local presentation of event intervals**, *optional* | The union of a resource's [dispatch, completion] intervals with no normalization. It answers no fixed question (see the self-challenge on "occupancy intervals") |
| One-change comparison: change set + completion before/after | **Game/Challenge-owned attempt comparison of direct facts** | Change set from game-owned draft facts, *including* result-affecting projection identity and order (G9: order alone 11 → 9). Outcome from two direct completion facts. Not a derived-measurement comparison |
| Tie-break rule on an order change | **Direct Engine-semantics fact** presented by the game | Engine semantics §2 rule 4 (`MachineId`). Correct only while the projection maps authored position to identity |
| Refusals (limiting step, surplus, attribution, mechanism, incomplete window) | **Game-local presentation** | Fixed refusal statements, each conditioned on one of the above |
| Single-run limiting step / constraint / bottleneck | **Unsupported — must refuse** in the minimal contract | Pool occupancy (G5), resource occupancy (G7), `busyTicks` (G4) and largest wait (G1, G2, G5, G7) are falsified. The completion chain survives the corpus but is a bottleneck inference (see below) |
| "Surplus" / "starved" | **Unsupported — must refuse** | G2 against G8: the same evidence, opposite truths. "Starved" is false at quiescence |
| Utilization, occupancy or any `busyTicks`-based figure | **Unsupported as utilization; not required** | Completion-credited (G4 at tick 9: 4 credited with 2 slots active; 48 against 45 elapsed) |
| Per-machine queue for multi-eligible work; `combinedQueueDepth` | **Unsupported — must not present or synthesize** | Not exposed; would double-count (G6, G11a) |
| Per-change attribution (more than one change); mechanism (one change) | **Unsupported — must refuse** | G1 + A + I is order-dependent; G7 + A + I is a pure interaction |

**No row meets the "concrete reusable measurement candidate" bar.** No requirement needs a measurement
whose definition involves a material interval, population, grouping, normalization or concurrency
choice that someone must own.

**Conditional items, *not* established demands.** The analytics research must not read these as game
requirements. They become concrete only if a later slice explicitly wants them:

1. **A single-run pacing or limiting-step explanation**, such as the completion chain:
   - It is a bottleneck inference, which the consumer plan places in the unresolved reusable scope.
   - Material choices: how to link a step to the release that started it; how to treat a tie between
     job readiness and resource release; what to do when capacity waits fall at several steps; the
     completeness of the event window.
   - Refusal conditions: a tie (G6, G7); capacity waits at more than one step; an incomplete window.
   - Evidence: it matched intervention truth on every unique case in this corpus. That is no general
     guarantee, because an equal-length path off the chain is not detected.
2. **Run-total waits by step, or occupancy/utilization over an interval.** Material choices exposed
   here:
   - completion-credited numerator versus occupancy-interval numerator;
   - capacity basis (concurrency) for the denominator;
   - running work at the boundary;
   - grouping by resource versus by eligibility pool (G5);
   - elapsed interval;
   - an incomplete window;
   - under release-at-once, run totals always rank `CUT` first (198) regardless of the constraint.

   The required refusal is: never present the value as a constraint.

## Result: the playable diagnostic requirement set

This is the bounded requirement set recommended for later promotion into the game consumer plan. It
must not be promoted until independent adversarial review and the owner walkthrough are done.

1. **Statement invariant.** Every player-visible diagnostic statement is one of five kinds:
   - a direct supported fact;
   - a boundary count;
   - an event interval;
   - a comparison;
   - an explicit refusal.

   Every derived statement names its derivation and can cite its evidence: the observation boundary,
   the supported-event sequences and the authored design facts. A minimal set of statement kinds can
   differ by question; the invariant cannot.
2. **Time labeling.** State is labeled with the observation's `currentTime`, not with the requested
   advance target.
3. **Waiting is step-first.** A resource is named only when the step has exactly one eligible resource.
   Multi-eligible waiting is shown as unassigned work for the eligible set. It is never shown as
   per-machine queues and is never derived from `combinedQueueDepth`.
4. **Resource activity at a boundary** is shown as slots in use of concurrency, or offline. It never
   uses `busyTicks`, utilization or percentages.
5. **Idle facts, never verdicts.**
   - Wording is "idle, and no unit is waiting for a step it can serve", or "idle while units wait for
     STEP".
   - The words "starved" and "surplus" are not used.
   - An explicit surplus refusal points to the one-change retry that can decide it.
6. **Event intervals need a complete window.** The completing unit's per-step wait and processing, and
   any optional per-resource activity intervals, are shown only when the attempt's supported events
   from sequence 1 through the boundary are retained, for one accepted order. Otherwise they are
   explicitly refused, while current-state facts stay available. This makes **event retention for an
   attempt a game-side requirement**. The runtime contract does not retain events.
7. **Limiting step: refuse in a single run.** No bottleneck or constraint label is shown. The evidence
   path is a one-change comparison.
8. **Measured delay is not a cause.** The largest measured wait is shown as a measurement of one unit.
   Under release-at-once it is the `CUT` backlog in every tested design.
9. **Comparisons.**
   - The change set comes from game-owned design facts, including result-affecting projection identity
     and order. The outcome is the two completion ticks and their difference.
   - One change: state change and outcome only, and say it "does not show why". Zero difference: "no
     change".
   - An order change carries the tie-break rule as a stated fact.
   - More than one change: explicitly refuse per-change attribution.
10. **Terminology.** Outside refusals, do not use:
    - utilization, %, efficien-;
    - bottleneck, constraint;
    - starved, surplus, blocked;
    - because, due to, caused;
    - variation;
    - "queue at".
11. **Scope.** All of the above is established for one product, a linear three-step routing, one order
    released at once, all resources online, no setup and no transfer. Any other workload or Engine
    feature needs revalidation before reuse.

### Separation

| Layer | Content |
|---|---|
| Engine/runtime facts | Observation fields and supported events listed under Repository evidence; Engine semantics §2 rules 4 and 7 |
| Game-owned presentation | Requirements 1–10: counts, groupings and event intervals over those facts; refusals; wording; attempt event retention |
| Game-owned named interpretation | **None required.** The completion chain is the surviving optional candidate, but it is a bottleneck inference whose ownership the consumer plan leaves unresolved |
| Challenge-owned | Attempt comparison of outcome facts beside game design facts |
| Reusable measurement candidates (ownership unresolved) | **None established**; the two conditional items above are handed over as context only |

## Downstream consequence for the analytics ownership research

The game question was derived first, from its own brief and evidence. Only then was the analytics
adversarial review read. That review's disposition is REOPEN, and it asks for a bounded shared
measurement use, or deferral of a common owner.

This investigation **supplies no such use for the first playable slice**. The analytics research should
therefore:

- not count the first non-spatial playable slice as a forcing consumer of utilization, occupancy,
  bottleneck inference, longitudinal aggregation or run comparison;
- keep evidence and provenance work conditional, as the review already concluded;
- treat the two conditional items above as the shapes a future game demand would take, with their
  material basis choices;
- reuse the corpus's misleading-measure proving cases if it defines any such measurement:
  - G4 and G11b: completion-credited counter against slots in use and elapsed time;
  - G5: most-occupied pool is not the constraint;
  - G7: occupancy tie with no helpful single addition;
  - G1 to G8: release-at-once wait totals.

The game-side consequence holds whatever ownership the analytics research chooses: the minimal contract
avoids every category the consumer plan leaves unresolved.

## External evidence

**Roser, C., Nakano, M., Tanaka, M., *Throughput Sensitivity Analysis Using a Single Simulation*.**
Proceedings of the 2002 Winter Simulation Conference, pp. 1087–1094 (DOI 10.1109/WSC.2002.1166361).

- **Verification status:**
  - In this session, the bibliographic record was confirmed (search index and the first author's
    publication page), and so was the abstract's validity condition: a single-simulation throughput
    prediction holds only "provided that the system change does not significantly change the
    bottleneck".
  - The §2.2 point that overlapping active periods make bottleneck attribution ambiguous was read in
    the earlier session of this investigation. It was **not re-read here** (the PDF could not be
    text-extracted in this session), so treat it as background, verify before citing.
- **What it establishes:** a recognized single-run method infers bottlenecks from machine activity
  durations, and its own authors bound the counterfactual prediction by bottleneck stability. This
  supports refusing a single-run "adding capacity here helps" claim (requirement 7). It also supports
  treating the corpus's tie and migration cases as exactly the conditions under which single-run
  inference is not licensed.
- **Where the analogy breaks:**
  - That method targets steady-state throughput of systems with finite buffers and blocking. This
    corpus is one finite order with unbounded queues, no blocking and release at once.
  - Here a continuously active resource is often not limiting: the cutter is active 0–36 in G1 without
    pacing completion.
  - Nothing in the source is used to define Arcogine semantics.

No other external evidence materially discriminated between the candidates. Specifically,
presentation research would not change the mechanical audits, and population comprehension is out of
scope.

## Adversarial analysis (self-administered)

This is the author's own challenge. It is not independent review and carries no independent weight.

1. **Audit design bias.**
   - The oracle (expected values and the method falsifications) was committed before the candidates
     existed. The audit *code* was committed together with the candidates and refined twice after a
     first run (both refinements are disclosed above).
   - Mitigations:
     - N fails five of six audits.
     - C3b's pass does not depend on the controlled-mutation correction.
     - The first refinement changed no candidate's logic.
   - Residual risk: the audits share authorship with C3b. An independent reviewer should try to write
     a misleading C3b-shaped statement that the audits pass.
2. **The terminology audit is a word list.**
   - It cannot catch overstatement in other words: "pacing" (C2b/C3a), "held it up" (the tie text),
     "limits" (inside C3b's refusal, which the audit skips).
   - So C3b's non-refusal templates were reviewed by hand: "waiting to start STEP on R", "each goes to
     whichever of A or B can take it first and is not assigned to either yet", "has k of n slots in
     use", "is idle, and no unit is waiting for a step it can serve", "took L ticks from order
     acceptance: waited w for STEP, STEP p on R", "processed n STEP steps; in use during ticks …", the
     comparison texts, and the tie-break rule.
   - None asserts a mechanism, rate or counterfactual. Two carry reading risk: "waited 33 for CUT" may
     be read as blame, and the tie-break rule beside an order comparison may be read as the
     explanation for that pair. Both are walkthrough questions, not audit failures.
3. **The refusal wording overstates slightly.**
   - "Which step limits this design is not decidable from one run" and "whether R is surplus … is not
     decidable from this run" are *negative* claims about all single-run analysis.
   - The evidence establishes less:
     - for surplus, the selected single-run evidence cannot decide it (G2 against G8);
     - for the limiting step, the tested single-run measures are falsified, and the one surviving
       method (the completion chain) carries no general guarantee.
   - Qualification for any promotion: word these refusals relative to the evidence shown ("not decided
     by this evidence"), as the brief's own examples do. This is a wording qualification only. It
     changes no audit result, because refusals are excluded from the terminology and traceability
     checks.
4. **C3a also passes every audit.**
   - C3b is preferred by minimality and ownership, not by falsification of C3a:
     - C3b earns no additional definition;
     - C3a's pacing verdict is a bottleneck inference the consumer plan leaves unresolved.
   - C3a's corpus pass should not be generalized; see conditional item 1.
5. **Is C3b a "bundle" or facts-only plus refusal?** It is the latter in substance, and the report says
   so. The candidate the brief calls "claim–evidence bundle" survives only in the form "claims =
   evidence-cited facts". No synthesized concise diagnosis is necessary.
6. **Activity intervals against the planning wording.**
   - The consumer plan lists "utilization or occupancy intervals" as unresolved reusable computation.
     C3b's per-resource activity statements are unions of event intervals with no normalization, which
     is arguably below that bar.
   - If reconciliation disagrees, the statements can be dropped. They answer none of the six fixed
     questions.
   - Every audit is monotone under removing them: each audit either checks for the absence of a bad
     statement or for a refusal or comparison that does not come from the activity statements. This
     is an inference, **not a separate run**.
7. **The corpus is narrow.**
   - Scope: one product, one order released at once, a linear routing, no availability changes, no
     setup and no transfer.
   - The event-interval derivations refuse when more than one order is accepted, so multi-order
     workloads would *refuse* rather than mislead. That is safe, but the useful surface shrinks.
   - Readiness defined as "previous step completion or order acceptance" holds only without transfer.
     When transfer lands, wait intervals need new semantics.
8. **Unit numbering.**
   - Observations number units by `ordinalWithinOrder + 1`; the event derivation numbers them by
     position in `ORDER_ACCEPTED.jobIds`.
   - They agree in every run, consistent with Engine semantics §3 (ordinal orders creation and
     `JobId` allocation).
   - The agreement is not separately pinned as a supported-event ordering guarantee. A game should
     correlate units by `JobId` and show the ordinal.
9. **Possibility versus necessity.**
   - The negative demand result is that the slice does not *need* a reusable measurement. It is not a
     claim that no slice ever will.
   - Requirement 7 makes controlled retries the only route to "what limits this design". That is a
     product consequence (see follow-up triggers), not a defect in the evidence.

## Surviving invariants

- A player-visible diagnostic statement is a direct fact, a boundary count, an event interval, a
  comparison or a refusal. Each derived statement names its derivation and cites its evidence.
- Multi-eligible waiting before binding is step work for an eligible set, not any machine's queue.
- Completion-credited `busyTicks` is neither instantaneous nor elapsed-normalized utilization.
- In this reference, no single-run descriptive load measure (pool occupancy, resource occupancy,
  credited busy time, wait totals) identifies the constraint.
- Under release-at-once, the largest measured wait is the first step's backlog, independent of the
  constraint.
- One authored change licenses "change and outcome", not "why". Several changes license no per-change
  attribution.
- Projection identity and order are authored changes whenever they differ.

## Transferability and reuse

- **Arcogine-specific dependency:** the current non-spatial Engine semantics, release-at-once order
  submission and the strategy-space reference.
- **Potentially transferable result (hypothesis level):** a truthful player-facing diagnostic surface
  over a deterministic simulation can be built from facts plus explicit refusal, and can push causal
  questions to one-change comparisons, without any named single-run diagnostic method.
- **Boundary conditions:** stochastic simulation; steady-state systems where single-run sensitivity
  methods are designed to work (Roser et al.); multi-order or continuous-arrival workloads.
- **Reusable research assets:** the G-corpus with hand-derived interventions; the misleading-measure
  cases (G4, G5, G7, G11b, release-at-once totals); the starvation-versus-surplus pair (G2 against G8);
  the confounded pairs, including pure interaction (G7 + A + I); the six audit implementations; and the
  N control as an audit-sensitivity check.
- **Synthesis-seed candidate:** none nominated. The signal is not yet independent of this one consumer
  and corpus.

## What did not survive

- **Pool occupancy as a constraint label:** falsified by G5. Resource occupancy: falsified by the G7
  tie. `busyTicks` ÷ elapsed: falsified by G4 and G11b.
- **Largest total or per-unit wait as a constraint label:** falsified in G1, G2, G5 and G7 (it is
  always `CUT`).
- **Facts-only with silent non-answers (C1, C2b):** silence is not refusal. A player cannot tell "not
  shown" from "not decidable".
- **Per-machine queue counts for shared work:** double-counting (G6, G11a).
- **Any single-run surplus or "starved" label:** G2 and G8 show the same evidence with opposite truths.
- **"Variation" as an explanation:** order-only changes are deterministic (G9).
- **The original controlled-mutation sub-check:** mis-specified and corrected, as disclosed above.

## Confidence and limitations

**High:**

- the falsifications above (hand-derived, then confirmed by runs and reproduced at the final recheck);
- C3b's mechanical pass in this corpus.

**Medium:**

- that requirements 1–11 suffice for a first internal playable slice;
- that a refusal-heavy surface is usable. **Untested:** the owner walkthrough was not performed.

**Low to medium:** any scope beyond requirement 11.

**Not inspected:**

- the Challenge substrate's own attempt-comparison output;
- multi-order, availability-change and setup workloads.

## Unresolved unknowns

- Whether the owner can interpret C3b's surface, and whether the optional completion-chain line helps
  or misleads (walkthrough outstanding).
- Whether a refusal-first single-run view is acceptable product-wise when limiting steps can be learned
  only through retries. This depends on the controlled-retry question.
- How event intervals and waiting change once transfer semantics land (readiness ≠ previous
  completion).
- Whether the durable-surface wording that treats `busyTicks` as utilization should be reconciled.

## Durable consequences

None are performed here.

- **Game consumer planning:** after independent review and the owner walkthrough, promote requirements
  1–11 with qualifications 3, 6 and 8 from the self-challenge, and the event-retention requirement.
- **Research register:** after reconciliation, record this question's verdict. Note that it supplies
  the evidence-contract precondition of the controlled one-variable retry question.
- **Analytics ownership research:** consume "no demand from the first playable slice", plus the
  conditional items and proving cases.
- **Consistency / reconciliation candidate:** the `busyTicks` "utilization" and bottleneck wording in
  the `FactoryHandler` comment, Engine semantics §10 / §10.2 and the runtime contract.
- **Reusable assets to preserve in a durable test surface,** if a later slice implements diagnostics:
  the G-corpus misleading-measure cases and the starvation-versus-surplus and confounded pairs.
  Otherwise discard them explicitly at the knowledge-transfer audit.
- **Report itself:** it does not need to remain readable after reconciliation.

## Implementation implication

No implementation. No game, analytics or Engine change is admitted by this report.

## Follow-up triggers

- **Owner walkthrough completed:** record the answers (as a new report revision or a linked walkthrough
  record). A failure falsifies C3b unchanged.
- **The controlled-retry question:** this contract makes retries the evidence path for limiting steps,
  which raises that question's materiality.
- **Transfer or spatial semantics land:** revalidate requirements 3, 6 and 8 before any spatial slice
  uses them.
- **Multi-order or continuous-arrival workloads in a slice:** revalidate requirement 11 and the
  event-interval refusals.
- **A slice explicitly wants a single-run pacing explanation, or run-total or occupancy figures:** that
  becomes a concrete measurement requirement for the analytics research, using the conditional items
  above.

## Sources

- Roser, C., Nakano, M., Tanaka, M. *Throughput Sensitivity Analysis Using a Single Simulation.* In:
  Proceedings of the 2002 Winter Simulation Conference, pp. 1087–1094. DOI 10.1109/WSC.2002.1166361.
  - Author preprint page, with the abstract checked in this session:
    https://www.allaboutlean.com/publications/2002_wsc-throughput-sensitivityanalysis-preprint/
  - A proceedings PDF was located by search at https://informs-sim.org/wsc02papers/146.pdf. Its
    content could not be text-extracted in this session, so that copy is unconfirmed.
  - The page range comes from the earlier session's reading of the paper.

## Evidence coordinates (active custody)

- **Workspace branch:** `workspace/factory-design-game-diagnostic-evidence`
- **Handoff prompt:** commit `a6081fbac9d901a1d2eb2911ef78eb49ed5cff79`,
  `workspace/research/handoffs/factory-design-game-diagnostic-evidence.md`
- **Pre-evaluation oracle:** commit `010b84a4e38113766af2cd594135825296a0ef3a`,
  `workspace/research/investigations/factory-design-game-diagnostic-evidence-oracle.md`
- **Experiment sources and generated results:** commit `f8a2be5bd2d5dfe0e2e9ffb0bfd7e9d8dc950374`, under:
  - `workspace/research/experiments/game-diagnostic-evidence/`
  - `workspace/research/investigations/game-diagnostic-evidence-results/`

  Both are unchanged since that commit and were reproduced byte for byte at the final recheck.
- **Rerun:**

  ```text
  cd product && ./gradlew :research-experiments:test \
    -PresearchExperimentSources=<repo>/workspace/research/experiments/game-diagnostic-evidence
  ```

  It writes `product/research-experiments/build/game-diagnostic-evidence/*.md`.
- **Report:** this file. Its exact commit is the one that adds it.
