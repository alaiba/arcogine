# Game diagnostic evidence — resume checkpoint (not the report)

> **Custody:** temporary continuity note for the factory-design game diagnostic-evidence investigation.
> It is a checkpoint, not a completed report or evidence handoff. **The repository owner must not read
> this note, the oracle note or `walkthrough-key.md` before doing the blinded walkthrough** — it
> contains results.

## Coordinates

- Workspace branch: `workspace/factory-design-game-diagnostic-evidence`
- Handoff prompt: commit `a6081fbac9d901a1d2eb2911ef78eb49ed5cff79`,
  `workspace/research/handoffs/factory-design-game-diagnostic-evidence.md`
- Research baseline (live main at start): `9e9e4678b18286cea83387b5ffc38741640f0db5`
- Main moved during the run to `306205c33ef93f0433a26c92f85cbd82eadd27f8` (#459: research-experiments
  test coverage plus `ExperimentRunner.recordOf` private → package-private; no behaviour change).
  Merged into the workspace; full module suite rerun: 147 tests, 0 failures; regenerated artifacts
  byte-identical. Recheck again at report time.
- Pre-evaluation oracle: commit `010b84a`,
  `workspace/research/investigations/factory-design-game-diagnostic-evidence-oracle.md`
- Experiment + results checkpoint: commit `f8a2be5`; sources under
  `workspace/research/experiments/game-diagnostic-evidence/`, generated results under
  `workspace/research/investigations/game-diagnostic-evidence-results/`.

## Rerun

From `product/` with a JDK 21 (documented generic Docker workflow, repo mounted at `/repo`):

```text
./gradlew :research-experiments:test \
  -PresearchExperimentSources=/repo/workspace/research/experiments/game-diagnostic-evidence
```

The experiment writes `product/research-experiments/build/game-diagnostic-evidence/*.md`; copy them over
the results directory. On Windows Git Bash prefix `docker exec` with `MSYS_NO_PATHCONV=1`.

## State of evidence

- Every hand-derived oracle value is confirmed by runs (completions, single and joint interventions,
  waiting at boundaries, idle resources, busyTicks divergence, completing-unit decompositions, per-step
  waits, completion chains). G6 (S2) interventions are run-derived: +cutter 44, +assembler 45,
  +inspector 45 (all help); G6 resource orders: 46 (8 orders) / 47 (16 orders).
- `ResearchPackageBoundaryTest` covers the workspace classes (explicitly asserted).
- Audit matrix (see `audit-matrix.md`):
  - C1 facts-only: fails refusal only (silent, not explicit, on confounded pairs and single-run surplus).
  - C2a named pool-occupancy "bottleneck": fails counterexample (G5 names CUT; G7 tie neither helps),
    refusal, terminology.
  - C2b named completion chain: fails refusal only.
  - C3a bundle + completion chain: passes all six audits.
  - C3b minimal bundle: passes all six audits.
  - N naive control: fails every audit except reconstruction (proves each audit can fail).

## Disclosed refinements made after the first audit run

1. Traceability caught an unnamed derivation in every candidate: the idle-resource statement combined
   resource state, waiting-by-step and eligibility without a named definition. Fixed by naming it
   (`idle-resources-and-eligible-waiting`); no candidate logic changed.
2. The controlled-mutation sub-check "a verdict must not move when completion does not change" was
   mis-specified: in G7 + assembler the chain moves from tie to `INSPECT` with completion still 68, but
   the chain evidence did change and adding an inspector to that variant helps (68 → 46). Corrected to
   "a moved verdict must be corroborated by intervention on the variant". Both versions were defined
   before results; the correction must be reported.
3. Wording fixes only (grammar, readable missing-range text, tie-break rule names the lower resource
   number, packet shows design per item in production order).

## Conclusions to write up (draft, not yet reviewed)

- **Positive bounded result:** C3b — facts, the completing unit's event intervals and resource activity
  intervals as claims with cited evidence; explicit refusal of single-run limiting step, surplus,
  multi-change attribution, mechanism and interval claims without a complete event window; one-change
  comparisons give change set plus outcome only. Mixed presentation by question; the invariant is that
  every statement is a direct fact, a boundary count, an event interval, a comparison or a refusal, with
  a named derivation and cited evidence.
- **No concrete reusable-measurement demand established.** C3b needs no normalized utilization,
  occupancy, run-total aggregate or constraint verdict. Run-total waits (DispatchProfile) and the
  completion chain appear only in C2/C3a and are not needed. If a later slice wants the completion
  chain (a bottleneck inference) or run-total/occupancy measures, that is the bounded requirement to
  hand to the analytics question; ownership stays unresolved here.
- Requirements to carry: label state with the observation's `currentTime` (the last emitted supported
  event's tick, not the advance target: G2 "17" is observed as 16, G8 "11" as 10); the consumer must
  retain an attempt's supported events from sequence 1 or refuse interval claims; the change set must
  include result-affecting projection identity/order (G9: order alone 11 → 9); multi-eligible waiting
  stays step-first ("whichever of …"), never per-machine queues (combinedQueueDepth is not exposed and
  must not be synthesized); busyTicks is excluded from the minimum contract; terminology lexicon
  (no utilization/%/bottleneck/constraint/starved/surplus/because/due to/variation/"queue at").
- Release-at-once: the largest measured wait is always CUT (198) and is the constraint only in G3/G4,
  so measured delay is never a constraint label.
- Repository tension, not changed: `FactoryHandler` busyTicks comment calls it "real utilization" and
  ties it to identifying the active bottleneck; Factory Design §10.2 names a "Maximum utilization"
  verification objective (a separate potential demand, not the game's).
- External (verified in session): Roser, Nakano, Tanaka, *Throughput Sensitivity Analysis Using a
  Single Simulation*, Proc. 2002 Winter Simulation Conference, pp. 1087–1094 (§2.2 overlap of active
  periods is ambiguous; §1 prediction valid only without significant bottleneck change). Analogy breaks:
  steady-state throughput with finite buffers/blocking versus one finite order, unbounded queues and
  release at once (the cutter is continuously active 0–36 in G1 without limiting completion).
- Analytics dependency (read only after the derivation): review `2c85fe7` REOPEN asks for a bounded
  shared measurement requirement or deferral; this investigation supplies none for the first playable
  slice, so evidence/provenance stays conditional.

## Remaining steps

1. Write `workspace/research/investigations/factory-design-game-diagnostic-evidence-report.md` per
   `docs/research/report-template.md` and the handoff's expected-content list (including the separate
   "Concrete reusable-measurement demand" section and the claim-to-evidence matrix summary).
2. Owner walkthrough: offer `game-diagnostic-evidence-results/walkthrough-packet.md` (blinded); record
   answers truthfully or state the exit criterion as outstanding. Never substitute an AI answer.
3. Final live-main recheck; commit the report; return the handoff fields the prompt lists.
   Risk is High: independent adversarial review is required next. Do not update register, planning or
   architecture in this run.
