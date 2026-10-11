# Pre-registration — reviewer probes for the analytics ownership Phase 2 post-execution-evidence review

> **Status:** research custody only. Committed before any reviewer probe source exists or runs.
> **Review target:** `d2125f39482de3306f2f56e9c7261a3dde160b33`,
> `workspace/research/investigations/simulation-analytics-ownership-phase-2-post-execution-evidence-revision.md`.
> **Probe target:** the author's research-local helper `PostExecutionEvidenceDiscriminatorExperiment.prove(...)`
> and its definitions at `ac0ac2aa603f82426d7d2c0ec88f3485b9927908`, used unchanged.
> **Live-main grounding:** `a7f9856617d99e796d528a44aa8c15677c485f55`.
> **Authority:** none. Expected values are derived by hand from Engine semantics §1.2, §2, §4 rules 3
> and 7, and runtime-contract "Captured execution evidence and interval determinacy". None is copied
> from a run. A later disagreement is reported, not edited back here.

## Purpose

The author's probes show conservative refusals and correct values on same-run, internally consistent
bundles. These probes ask the complementary questions the review handoff raises:

- can the helper **accept** an insufficient claim when the bundle mixes runs or contains evidence
  that contradicts its closure (a false positive), and is that a defect of the helper only or of a
  normative statement in the report;
- is the observation-frontier branch stricter than the contract needs, and by how much;
- is "no currently known work" from one observation a different claim from the P4.3 interval
  classification.

Reviewer-local checks added in the probe (`sameRun`, `closureNotContradicted`,
`observationTimeWitness`, `snapshotFlow`) are research-local illustrations, not proposals for a
production format.

## R1 — basis from another run

Model: one resource `M` (1 slot), one step `WORK` 5.

- **Run X:** `setMachineAvailability(M,false)` → 1 `MACHINE_AVAILABILITY_CHANGED`@0;
  `setMachineAvailability(M,true)` → 2 `MACHINE_AVAILABILITY_CHANGED`@0 (no queued work, no dispatch);
  observation `X2 = (2,0)` with `M` active `[]`; `submitWorkload(1)` → 3 `ORDER_ACCEPTED`@0,
  4 `JOB_DISPATCHED(M)`@0; `advanceUntil(100, unbounded)` → 5 `JOB_STEP_COMPLETED`@5,
  6 `ORDER_COMPLETED`@5; observation `(6,5)`.
- **Run Y:** same model, `submitWorkload(1)` → 1 `ORDER_ACCEPTED`@0, 2 `JOB_DISPATCHED(M)`@0;
  observation `Y2 = (2,0)` with `M` active one job. Different `RunId`, same `ModelFingerprint`.

| Case | Expected |
|---|---|
| R1.1 X changes 1..6 with X's own basis `X2`, `activeJobTicks(M,[0,5))` | 5, producer witness change 5 at time 5 |
| R1.2 the same bundle with `Y2` substituted as basis (bundle `RunId` = X) | **author helper accepts 10** with a producer-witness proof (basis count 1 + dispatch 4 → 2 jobs × 5) |
| R1.3 reviewer same-run check before `prove` | refuses R1.2 (basis run differs); R1.1 still 5 |

## R2 — closure contradicted by held changes

Model as R1. One driver: basis `(0,0)`; `submitWorkload(1)` → 1, 2 @0; stale observation `(2,0)`;
`advanceUntil(100, unbounded)` processes one event → 3 `JOB_STEP_COMPLETED`@5, 4 `ORDER_COMPLETED`@5;
call log `[Call(100, MAX, 1, false)]` (exhaustion through 100 demonstrated); post observation `(4,5)`.
The holder holds all changes 1..4.

| Case | Expected |
|---|---|
| R2.1 closure bound to the post-advancement observation `(4,5)`, input excluded through 100, `[0,100)` | 5 (proof: controller closure bound to `(4,5)`) |
| R2.2 the same call log and scope, closure bound to the stale observation `(2,0)` | **author helper accepts 100** (prefix 1..2 only; the held completion 3@5 is ignored) |
| R2.3 reviewer check: refuse when a held change with sequence `> S*` has time `< b` | refuses R2.2, naming changes 3 and 4; R2.1 still 5 |

The runtime contract requires a *post-advancement* binding and says delivered contradictory changes
can expose a broken commitment. R2.2 is therefore an attestation error the holder could detect,
not a new contract falsifier.

## R3 — observation-frontier strictness

Model: `MIX` 4 on Mixer (1 slot), then `PACK` 1 on Packer (2 slots). Two units at time 0:
1 `ORDER_ACCEPTED`@0, 2 `JOB_DISPATCHED(u0, Mixer, step 0)`@0, 3 `JOB_WAITING(u1, {Mixer})`@0.
`advanceUntil(4, unbounded)`: 4 `JOB_STEP_COMPLETED(u0, Mixer, step 0)`@4, then the two placements
`JOB_DISPATCHED(u0, Packer, step 1)` and `JOB_DISPATCHED(u1, Mixer, step 0)` as 5 and 6, both @4 (their
relative order is not asserted). The holder drains 1..6. `advanceUntil(5, unbounded)`:
7 `JOB_STEP_COMPLETED(u0, Packer, step 1, jobComplete)`@5 **alone** (the order is incomplete and
Packer has nothing queued); next event at 8 > 5. Observation `(7,5)`. The holder does **not** hold 7.

| Case | Expected |
|---|---|
| R3.1 holder 1..6 + observation `(7,5)`, Mixer `[0,5)` | author helper refuses: cursor 7, change 7 missing |
| R3.2 the same for Packer | refuses |
| R3.3 reviewer variant: an observation's time is the time of the change at its cursor, so held `S0+1..S-1` all `< b` plus an observation `(S, T >= b)` proves `[a,b)` | Mixer 5 (u0 `[0,4)`, u1 `[4,5)`), Packer 1 (u0 `[4,5)`) |
| R3.4 complete holder 1..7, author helper | Mixer 5, Packer 1 by producer witness 7@5 — equal to R3.3 |

Expected reading: the author's branch is conservative by exactly the cursor change in this narrow
case and never yields a false value.

## R4 — current-state snapshot versus interval/horizon classification

P4.3's closed and open runs (one unit on the R3 model; the open run submits one more unit after the
observation at 5).

- Snapshot rule (reviewer-local, from one observation plus published routing): working slots =
  Packer active jobs; idle = concurrency − working; *needing* = unfinished jobs with a remaining step
  eligible on Packer (`Queued` from `currentStep`, `InProgress` from `currentStep + 1`). Idle slots
  are "starved" when needing > 0 (capped by needing), otherwise "no currently known work".

| Case | Expected |
|---|---|
| R4.1 closed `(6,5)` and open `(6,5)` | identical normalized observations; Packer working 0, idle 2, needing 0 → no currently known work 2 in both |
| R4.2 open after the second submission, observation `(8,5)` (7 `ORDER_ACCEPTED`@5, 8 `JOB_DISPATCHED(u1, Mixer)`@5) | u1 `InProgress` step 0 on Mixer; Packer starved uncapped 2, capped 1 |
| R4.3 interpretation | R4.1 is true in both runs as a statement about state at sequence 6 and needs no closure; P4.3's `[5,9)` classifications differ because they are interval claims whose closed-run ticks lie in an idle tail |

## Execution plan

Copy the author's three classes from `ac0ac2aa` unchanged together with the reviewer probe into one
untracked directory and run, at live main:

```bash
cd product && ./gradlew :research-experiments:test -PresearchExperimentSources=<dir> \
  --tests 'com.arcogine.research.experiment.PostExecutionEvidenceReviewProbe' \
  --tests 'com.arcogine.research.experiment.ResearchPackageBoundaryTest'
```

Report counts and any disagreement exactly.
