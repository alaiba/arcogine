# Game diagnostic evidence: reconciliation drift and knowledge-transfer audit

> **Authority:** Transient reconciliation working record. It is retired with this workspace and is not
> repository authority. The durable destinations named below are the only maintained result.
>
> **Inputs:**
> - Report revision 3: `4ad00136441fa29e6b462ac5431b60cf2de1e148`,
>   `workspace/research/investigations/factory-design-game-diagnostic-evidence-report.md`.
> - Independent review (`ACCEPT WITH QUALIFICATIONS`): `6484795ff4c4ad971e06c70751b03805e54a55f5`,
>   `workspace/research/investigations/factory-design-game-diagnostic-evidence-adversarial-review.md`.
> - Handoff: `bd38e33367438ebf194720ccf67a4efadc5a763e`,
>   `workspace/implementation/factory-design-game-diagnostic-evidence-reconciliation.md`.
>
> Both report and review are byte-identical at the handoff commit.
>
> **Base normalization:** the evidence branch was diverged from `main`. Live `main`
> `5775057aac2bd61dd45afdefed0bd49e1f93a98c` was merged in with an ordinary, non-forced merge commit
> `11cdcde7cc59014068dbef1edd0b8e1f16eb464f` (first parent the handoff commit). No rebase, amend or
> force push. These commits were verified as ancestors of the normalized head:
> - report and review;
> - handoff;
> - first handoff `a6081fba…`;
> - first-pass oracle `010b84a4…`;
> - second-pass oracle `de2c28a8…`;
> - missions oracle `05111092…`.

## 1. Drift matrix (review-era `main` `306205c3` to live `main` `5775057a`)

| Change | What it changed | Effect on the Game evidence | Treatment |
|---|---|---|---|
| #465, inert scheduler markers removed | Factory schedules only authoritative step-completion turns. Positive-event-budget-stopped scripts with interleaved commands can now differ; start-fixed, time-bounded runs keep the same supported events and outcomes | Every corpus, walkthrough and bakery script is one submission at tick 0 and then `ExperimentRunner` advancement with `advanceUntil(t, Long.MAX_VALUE)`, which is time-bounded only. No script interleaves commands after advancement | **Verified by rerun** (section 4): all values reproduce and generated outputs are byte-identical. Durable rule: a requested tick is not an observed time, and a snapshot shown as the state at a later tick needs exhaustion through that tick with no later input taking effect at or before it (consumer plan, claim classes) |
| #466, captured-execution evidence | Distinct claim classes: snapshot fact; interval `[a,b)` with basis, run/model binding, gap-free changes, an independent frontier and finality; future-horizon input exclusion as a controller assertion | The research's interval and idle-tail claims rested on closed constructive scripts | The consumer plan §5 interval/snapshot/future-horizon rules (already reconciled by #466/#471) are kept, and the new subsection points to them instead of restating them. No Engine ledger, validator, closure endpoint or history service is introduced |
| #469, `busyTicks` removed | The resource counter, its Engine accumulation, the utilization/bottleneck claims and the conformance references are gone; Engine §10 states the Engine produces no utilization, occupancy or bottleneck measure | The historical `GameContracts.NaiveDashboard` and `handDerivedBusyTicksDivergenceHolds` no longer compile | Confirmed complete on live `main`: no Java hit for `busyTicks`; `HeadlessClosureAcceptanceTest` and the overview no longer claim busy-tick utilization or a carried-load bottleneck; the Governance verification-objective sentence and the Factory Design "Maximum utilization" illustration are intact. The owner directive is **fulfilled**, not work here. The historical values (4 at tick 9, 48 against 45) stay negative knowledge in the brief and are not rerun |
| #471, analytical-definition ownership | A Game-owned named method is permitted, subject to its own admission and sufficiency/refusal; shared meaning needs one of four concrete triggers; custody is separate from meaning | Settles the former "hand to the analytics research" routing in the report (sections 9, 15) | Game demands recorded as **Game measurement demands** with a method acceptance gate in the consumer plan. No research formula admitted, no analytics module selected, evidence/provenance stays CANDIDATE |
| Other `main` movement since `306205c3` | #461/#462/#464 (research-process docs), #463 (test coverage for `combinedQueueDepth` exactness), #467/#470 (dependency bumps) | None material to the Game evidence | No action. Live `main` did not move during execution (re-resolved before PR) |

## 2. Requirement-to-authority and Q1–Q8 transfer matrix

| Source item | Durable destination | Notes |
|---|---|---|
| Report §4/§7 bounded verdict | Brief Conclusion; register row; consumer plan §2; vertical-slice gate; programme doc | Wording narrowed per Q1 |
| Report §7 guardrails (never-as-fact list) | Consumer plan "Forbidden shortcuts" | Adds the Q6 concurrency `Busy` and throughput-period items |
| Report §7 allowed claims | Consumer plan "Claim classes" | Adds the labeled observed time and the requested-tick rule from #465/#466 |
| Report §8 presentation findings | Consumer plan "Presentation requirements" and "Pull-based inspection" | Refusal kept as an internal guardrail; mission count, scripts, keys and client tech excluded |
| Report §9 measurement demand | Consumer plan "Method acceptance gate" | As demands with choices, not formulas (Q4/Q5) |
| Report §10 platform requirements | Consumer plan "No new platform facts"; brief Q7 | Corrected per Q7 |
| Report §11 counter removal | Brief Q8 and one proving case | Already implemented by #469 |
| Report §12 requirement set | Consumer plan "Diagnostic claims and evidence" | Items 1–7 each mapped above; scope as the subsection's opening |
| Report §13 bottleneck deferral | Consumer plan (single-run method optional); brief reopening note | Per Q5 |
| Report §16 concept map and proposed new candidates | Brief reopening section; programme doc promotion paragraph | Deliberately **no new register questions** (no concrete admission need) |
| **Q1** verdict and human evidence | Brief Conclusion and qualification 1; register row; consumer plan §2; vertical-slice gate; programme doc | Single-person smoke test, reduced blinding, concept coverage, bottleneck teaching not established |
| **Q2** what was audited | Brief qualification 2; consumer plan "Acceptance tests" | Audits are regression checks; implementation derives its own tests |
| **Q3** two-layer, always-online | Consumer plan "Two-layer idle classification"; brief qualification 3; proving case | Third classification or refusal; offline-and-queued not impossible; not an Engine invariant |
| **Q4** basis completeness | Consumer plan "Method acceptance gate" (cap and pool items); brief qualification 4 | Not exercised on multi-step-eligible machines or the corpus |
| **Q5** demand framing | Consumer plan gate; brief qualification 5 | Reworded for #471: Game-owned method permitted; reuse not demonstrated; removal not reuse evidence; single-run bottleneck optional |
| **Q6** guardrail additions | Consumer plan "Comparisons" and "Forbidden shortcuts"; brief qualification 6; proving case | Non-composition, `Busy` on concurrency above one, time-only "needed", "worth it" under named Challenge evaluation |
| **Q7** platform corrections | Consumer plan "No new platform facts"; brief qualification 7 | Comparator verified on live `main`: compares by stable `MachineId` and reports `listPosition` changes. A removal also shifts later positions, hence the "no false second change" presentation rule |
| **Q8** removal scope | Brief qualification 8 | Live `main` confirmed repaired; no product change in this PR |
| Review minor notes (events list, "REOPEN" label, `combinedQueueDepth` reconstruction, generator sampling) | Discarded | Report-internal accuracy notes; nothing durable depends on them |

## 3. Negative cases and artifact fates

**Negative cases** (brief "Reusable proving cases"; named in the consumer plan's acceptance tests):

| Case | Home |
|---|---|
| Shared multi-eligible waiting | Maintained `StarterCorpus` and `WaitingWorkByStepOracleTest` |
| Half-open bound and concurrency denominator | Maintained `ProcessingOccupancyOracleTest`, plus G4 parameters |
| Historical counter error | G4 parameters, historical only |
| Same-snapshot surplus | G2 and G8 parameters |
| Added capacity at the most-occupied pool | Maintained `CapacityCorpusTest` (G5) |
| Order-only change | Maintained `CapacityCorpusTest` (G9) |
| Confounded co-binding and order-dependent attribution | G7 and G1 parameters |
| Single-change non-composition | Probe 2 parameters |
| Offline-and-queued state | Probe 1 parameters; cross-referenced to Engine evolution's local recovery underfill |

**Artifact fates:**

| Workspace artifact | Fate |
|---|---|
| Report revisions 1–3, review, handoffs, checkpoint, oracles, protocols | Discarded at retirement after transfer; no exact readability needed |
| Discovery notes, walkthrough records | Discarded after transfer into the brief framing, Q1 and presentation requirements |
| `game-diagnostic-evidence` experiment sources, results, walkthrough and inspection packs, viewers | Discarded; proving cases carry their discriminators |
| `game-diagnostic-evidence-current-main` compatibility port (this reconciliation) | Discarded after recording its outcome here and in the brief |
| This audit and the reconciliation handoff | Discarded at retirement |

- **Historical rationale record:** none. The trade-offs are current constraints and live in the brief's qualifications and "claims that did not survive"; the choice is narrow and easy to revisit.
- **Synthesis seed:** one independent occurrence added to "A control's asserted state is not evidence of its effect". The audits passed, yet the review crafted a passing misleading statement. The seed was not used by the investigation. The revisit condition is approached but not treated as fired, because this was a research-local check rather than a maintained control. This is left for an owner decision; no synthesis question is created.

**Retirement:** the only research question carried by the workspace is this one. It is `CONCLUDED` in the merge candidate, and every material item above has a destination or an explicit discard. The workspace is retirement-eligible once the reconciliation lands and its independent PR review validates the transfer. Every tracked `workspace/` path is removed from the final tree.

## 4. Current-main compatibility check

**Purpose:** verify the #465 preserved-scope claim for the Game corpus, and port the two review probes behind Q3 and Q6.

**Port** (`workspace/research/experiments/game-diagnostic-evidence-current-main/`): a copy of the reviewed experiment with exactly two edits:

1. `GameContracts.NaiveDashboard` now misreads a snapshot's active-slot share as "utilization" and ranks by carried load (active plus own queue), instead of reading the removed counter. The audit facet keeps its historical name.
2. `handDerivedBusyTicksDivergenceHolds` keeps only its surviving facts: two active slots at tick 9, completion 45.

It also adds `GameDiagnosticCurrentMainProbes`, with hand-derived expectations written beside each assertion:

- **Offline and queued:** `Busy`, one of two slots working, three in the own queue, none pending; at tick 6, one active and two queued; completion 16 against 8 always online.
- **Packers:** 30 / 30 / 30 / 37.

**Command** (from a `git archive` export of normalized head `11cdcde7`, on `gradle:9-jdk21`, OpenJDK 21.0.12):

```text
docker run --rm -v <scratch>/product:/app -v <scratch>/exp:/exp -v arcogine_gradle_cache:/root/.gradle \
  -w /app gradle:9-jdk21 ./gradlew :research-experiments:test -PresearchExperimentSources=/exp \
  --no-daemon --no-build-cache
```

**Outcome:** BUILD SUCCESSFUL. 158 tests, 0 failures, 0 errors, 0 skipped:

| Test class | Tests |
|---|---:|
| `GameDiagnosticEvidenceExperiment` | 11 |
| `BakeryInspectionPack` | 4 |
| `GameWalkthroughPack` | 4 |
| `GameDiagnosticCurrentMainProbes` | 2 |
| Maintained substrate tests | rest |

Byte comparison with the committed reviewed outputs:

| Output | Result |
|---|---|
| `inspection.json`, `missions-key.json` | identical |
| `game-diagnostic-walkthrough-2/key.json`, `pack.json` | identical |
| `interventions.md`, `walkthrough-key.md`, `walkthrough-packet.md` | identical |
| `claim-evidence-matrix.md` | differs only inside the `N-naive-dashboard` section (from line 2361 on); all six candidate contracts identical |
| `audit-matrix.md` | differs only in the naive row (counterexample audit now FAIL (23), was FAIL (22)) and its finding lines; all candidate rows identical |

**Limitations:**

- The naive control was deliberately altered, so its counts are not comparable with the reviewed ones.
- The historical counter values were not and cannot be rerun.
- The probes cover only the two review cases.
- No expectation was refitted.
- The original report and review sources are untouched.
