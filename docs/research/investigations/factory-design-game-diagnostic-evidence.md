# Factory-Design Game Diagnostic Evidence Contract Research

> **Status:** CONCLUDED — see the maintained [research register](../research-register.md)  
> **Scope:** Truthful, inspectable player-facing diagnostic evidence over supported non-spatial production/runtime facts  
> **Authority:** Product research record only. The promoted requirements live in the [consumer plan](../../planning/factory-design-game-consumer.md#diagnostic-claims-and-evidence). This record defines no Engine fact, admitted analytical method or population-level usability claim

## Conclusion

> Within the studied non-spatial scope, a mechanically truthful, inspectable diagnostic surface is achievable from supported simulation facts. It allows direct facts, counts from one labeled observation, supported event intervals, controlled comparisons, and values of named methods with declared evidence and refusal. It never presents a falsified single-run heuristic as fact. One co-designing owner found a pull-based text prototype explorable in a single-person smoke test.

This is a **bounded positive**. It establishes guardrails that held on every falsification case checked. It also establishes concrete Game measurement demands with the choices a method must fix, and presentation requirements that survived the owner sessions. It does **not** establish that the surface is usable, understandable, satisfying or engaging for target players, that learning occurs, that bottleneck can be taught, or that any analytical definition is admitted. The requirement set is in the consumer plan's [Diagnostic claims and evidence](../../planning/factory-design-game-consumer.md#diagnostic-claims-and-evidence); the playable implementation gate stays [blocked](../../planning/factory-design-game-vertical-slice.md).

## Bounded question

What minimum player-facing diagnostic evidence contract can the game build from supported Arcogine simulation facts so that every diagnostic claim is mechanically traceable to authoritative or explicitly derived ground truth, explicit about its method, able to refuse unsupported causal attribution, and inspectable without exposing scheduler or runtime internals?

**Decision at stake.** Whether a first internal or playable slice can expose a truthful diagnostic surface for the non-spatial production problem, and which evidence and explanation requirements to promote into the game consumer plan.

**Framing set by the owner during investigation.** The brief defined *truthful* but not *diagnostic*, so the owner set the player goals:

- understand one recorded run;
- learn the production concepts it exhibits, with vocabulary introduced in context; and
- check hypotheses by exploring design alternatives.

The owner also chose a text interface as the study medium and limited the study to static recorded runs with pre-computed variants. The owner walkthrough became a smoke test of a presentation prototype rather than of a statement list. That third pass had **reduced blinding**. The owner co-designed its scenarios and missions, had seen the first pass's key, and its labeled displays answered most missions directly. The brief's shuffled-identity protocol was not used.

**Studied scope:** non-spatial, one product, linear routing, the whole order released at once, every machine always online, no setup or transfer.

## Evidence basis

- **Deterministic falsification corpus.**
  - **Corpus:** 13 designs and 24 pairs, with ground truth hand-derived and committed before evaluation.
  - **Coverage:** a true constraint; capacity added at and away from it; constraint migration; starvation versus surplus; multi-eligible waiting; a long unfinished step; concurrency above one; confounded pairs, including a pure interaction.
  - **Contracts compared:** six candidate information contracts and one deliberately naive control, under six mechanical audits.
  - **Results:** facts-only and named-interpretation contracts each failed at least one audit. Claim–evidence bundles (facts, named derivations and refusals) passed all six. The naive control failed five.
- **Three owner walkthroughs.**
  1. A blinded statement list partly failed: the owner inferred the constraint from the longest wait and did not understand "starved/surplus".
  2. A scripted walkthrough was paused to resolve scope.
  3. A pull-based text inspection prototype ran over four bakery scenarios and 13 missions. The owner got 17 of 19 checked answer parts right; one mission was answered correctly through an invalid heuristic.
- **Independent adversarial review: `ACCEPT WITH QUALIFICATIONS`.**
  - It reran the experiment on the JDK 21 floor and reproduced every output byte for byte.
  - It hand-derived all 13 missions and the corpus spot checks, and found no wrong official answer.
  - Two new probes found the offline-and-queued state and single-removal non-composition.
  - Every qualification below comes from that review.
- **Reconciliation recheck against the current Engine.** Four relevant changes followed the review:
  - the Engine stopped scheduling inert start and order-completion markers;
  - the captured-execution-evidence and interval-finality rules were reconciled;
  - the completion-credited `busyTicks` counter was removed;
  - analytical-definition ownership was decided.

  Every corpus and mission script is start-fixed (one submission, then time-bounded advancement), so the marker change preserves its supported events. A port of the experiment to the current tree confirmed this. The port changed only the two places that read the removed counter: the naive control now misreads a snapshot instead, and one assertion keeps only its surviving facts. It also added the two review probes, with hand-derived expectations. Every test passes. The regenerated mission data, walkthrough pack, intervention outcomes and candidate-contract statements are byte-identical to the reviewed outputs; only the altered naive control's rows differ. The historical counter values cannot be read on the current tree, so they remain baseline-specific negative evidence and were not rerun.

## Qualifications that constrain the result

These are part of the accepted result, not commentary on it.

1. **Verdict and human evidence.** Promote only the bounded positive above. "Usable", "understandable", "satisfactory" and "learning observed" are not conclusions. The third walkthrough's reduced blinding travels with its result. Independently exercised concepts are: slot state, starvation, utilization, experiment-based "needed", and one co-binding experiment. Bottleneck teaching and external-player comprehension were not established. Owner failures remain valid falsifications; owner success is only a smoke test.
2. **What was mechanically audited.** The six audits ran on the statement contracts. They are keyword and self-declared-facet regression checks, not a truthfulness verifier for new statements. For example, a misleading direct-fact sentence ("assembly held the whole run back") passes them. The later starvation and utilization characterizations were checked only on four bakery scenarios: pre-registered values, plus an internal check that every slot-tick has exactly one state. They were not audited across the 13-design corpus. Any implementation derives its own acceptance tests from the guardrails.
3. **The two-layer model is valid for always-online machines only.** Slot state is observed. Why a slot is idle is a named interpretation. "An idle slot with work queued for it is a contradiction" holds only while machines stay online. After an availability change, Engine semantics §4 rule 7 makes that state reachable, so any method needs a third classification or an explicit refusal. A snapshot's "no currently known eligible work" never promises that no work will arrive.
4. **Measurement basis completeness.** The choices are material because each changes a value: half-open versus inclusive periods, the per-slot denominator, counting running work up to the bound, machine versus step-pool views, and known work only. Two more choices are open:
   - whether starved slots are capped by the remaining eligible work;
   - the pool basis for a machine eligible for several steps.

   The flow characterization was never exercised on such machines or across the 13-design corpus.
5. **Measurement demand framing.** Utilization and starvation are this Game's concrete measurement needs. The [ownership decision](../../architecture/overview.md#domain-authority) lets the Game own a named method for them. Reuse beyond the Game is not demonstrated: utilization has only an illustrative Factory Design verification objective, and idle-reason coding rests on unverified industry background. The counter's removal is not reuse evidence. A single-run bottleneck method is optional, with no demonstrated Game need.
6. **Guardrail additions.**
   - Single-change "needed / not needed" results do not compose into a verdict on a joint change.
   - For concurrency above one, machine-level `Busy` is not "fully working".
   - "Needed" is time-only. "Worth it" belongs to a named Challenge evaluation rule.
7. **No platform gaps.**
   - Per-step timing is available from captured supported events or adequate observations.
   - Machine identity needs no Factory change: stability under edits is the projector's rule.
   - Design comparison has already landed in the Factory semantic comparator and Governance `ChangeSet`.
8. **The counter removal is complete and separate.** The owner's removal decision was carried out by its own reviewed change. That change also repaired the maintained utilization and carried-load bottleneck claims, and kept the Governance verification-objective statement. The counter's history survives only as negative knowledge: it was misnamed and misused as utilization, exact only as total processing time at quiescence, and wrong as a mid-run or elapsed-time reading.

### Claims that did not survive

These must not be reintroduced:

- that the brief's first contract shows no measurement demand (reversed by the owner's educational goal, not by new mechanical evidence);
- that those demands are established as reusable beyond the Game;
- that the game must itself keep event history, machine identity and change sets as game concepts;
- refusal sections as player-facing UI;
- a statement list as the presentation;
- trap questions as walkthrough tasks;
- avoiding industry vocabulary;
- a completion-chain explanation line for players;
- "surplus" from one run;
- annotating every tick range with its length;
- "usable" or "learning observed" as conclusions;
- the idle-with-queued-work contradiction stated as an Engine property;
- "mechanically audited" as general truthfulness;
- per-step timing, machine identity or design comparison as platform blockers;
- the busy counter as utilization.

## Promoted requirements

The [consumer plan](../../planning/factory-design-game-consumer.md#diagnostic-claims-and-evidence) owns the requirement set:

- claim classes and the rule that no claim exceeds its proof;
- pull-based inspection;
- forbidden shortcuts;
- comparison truth;
- the two-layer idle classification;
- the method acceptance gate for utilization and starvation;
- no new platform facts;
- presentation requirements;
- acceptance tests derived from the guardrails.

The [implementation gate](../../planning/factory-design-game-vertical-slice.md) records that this research gate is satisfied while playable admission stays blocked.

## Reusable proving cases

Each case falsifies a shortcut the guardrails forbid. Where a maintained research-substrate fixture already pins the phenomenon, it is named. The other parameters are enough to rebuild the case with the substrate's linear routing family.

| Case | What it shows | Parameters and durable home |
|---|---|---|
| Shared multi-eligible waiting | Per-machine counts double-count shared pending work; waiting belongs to the step | The multi-eligible starter fixture and [`WaitingWorkByStepOracleTest`](../../../product/research-experiments/src/test/java/com/arcogine/research/experiment/WaitingWorkByStepOracleTest.java) |
| Work running at a half-open bound; concurrency denominator | Running work counts up to the bound; occupied job-ticks can exceed elapsed time on a multi-slot machine | [`ProcessingOccupancyOracleTest`](../../../product/research-experiments/src/test/java/com/arcogine/research/experiment/ProcessingOccupancyOracleTest.java). Also: cutter, two-slot assembler and two inspectors, on `CUT` 3 → `ASSEMBLE` 4 → `INSPECT` 5 with 12 units. Both assembler slots are working at tick 9, and the run completes at 45 |
| Historical completion-credited counter | A counter credited at step completion is not utilization | Same design: the removed counter read 4 at tick 9 with both slots working, and 48 against 45 elapsed at completion. Historical only |
| Same idle snapshot, different removal outcome | One run cannot show surplus | Cutter, assembler and two inspectors complete at 56; Inspector 2 is idle with no eligible work at tick 17, yet removing it gives 67. Cutter, two assemblers and an inspector complete at 67; Assembler 2 is idle with no eligible work at tick 11, and removing it still gives 67 |
| Capacity at the most-occupied pool to no effect | Busiest is not the bottleneck | The strategy-space occupancy counterexample, pinned by [`CapacityCorpusTest`](../../../product/research-experiments/src/test/java/com/arcogine/research/experiment/CapacityCorpusTest.java) |
| Order-only change | Identity and order are part of a design change | The shared-resource pair completes at 11 authored and 9 reversed, pinned by `CapacityCorpusTest` |
| Confounded co-binding and order-dependent attribution | No unique responsible change; joint effects do not split | One resource per step on `CUT` 3 → `ASSEMBLE` 5 → `INSPECT` 5 with 12 units completes at 68, and at 68 with either an added assembler or an added inspector; adding both gives 46. On the `ASSEMBLE` 4 routing (67), an assembler then an inspector attributes 0 then −22; the reverse order attributes −11 then −11 |
| Single-change non-composition | "Not needed" results do not license a joint removal | `MIX` 2 → `BAKE` 3 → `PACK` 4 with mixer, oven and three packers, one slot each, and 8 units: 30 with all three packers, 30 without Packer 2 alone or Packer 3 alone, and 37 without both |
| Offline-and-queued state | Idle-with-queued-work is reachable; it needs a third classification or refusal | One step `BAKE` 4 and a two-slot oven. Four units are submitted while the oven is offline, then it comes online at time 0. The oven is `Busy` with one slot working and three units in its own queue, and the run completes at 16 instead of 8. Retained Engine behavior, also recorded as local recovery underfill in [Engine evolution](engine-evolution.md) |

## What this does not establish

- Population-level comprehension, accessibility, usability, engagement, learning efficacy or difficulty, or how to teach bottleneck.
- Any result for orders arriving over time, availability changes, several products, branching routing, setup, transfer or spatial layout, or finite buffers and blocking.
- An admitted Game method, a shared analytical definition, a new Engine or Factory fact, a live interaction loop, or a UI or client technology.
- Mechanical enforcement of the guardrails for statements not yet written.

## Reopening triggers

| Concrete condition | Owning next decision |
|---|---|
| An allowed in-scope claim is shown false, or an official mission answer is shown wrong | Reopen this research |
| A selected slice needs a utilization or starvation figure | The consumer plan's method acceptance gate, inside that slice's admission; not a reopening |
| A selected slice needs scope beyond the studied one (arrivals over time, availability changes, several products, pooled multi-step starvation, transfer) | Revalidate the guardrails and idle classification for that scope inside the slice's admission; admit focused research only for a material new uncertainty |
| Another consumer, a core verification or Governance need, an outward contract, or divergent duplicates need the same named meaning | [Domain authority](../../architecture/overview.md#domain-authority) shared-definition triggers |
| An admitted method needs new inputs, sufficiency/refusal, accumulation, provenance or retention | The [evidence/provenance question](simulation-analytics-evidence-provenance.md), still CANDIDATE |
| Live interaction with mid-run commands is selected | The [execution-evidence reopening trigger](simulation-execution-account.md#reopening-triggers) |
| Independent target users exist, or a release requires a comprehension claim | [External-player validation](factory-design-game-vertical-slice.md#external-player-validation--candidate) |

Some proposed follow-ups get no register question now: a concept curriculum (including bottleneck teaching), an order-release policy, finite buffers and blocking, and a live loop. Each becomes a focused question under its real owner only when a selected slice needs it. Controlled retry and scoring keep their existing candidate questions.

## Durable proving assets and knowledge-transfer outcome

The reports, the independent review, the experiment sources and outputs, the owner-session records and the reconciliation's compatibility port were held in one temporary research-evidence workspace. Each material result has a destination below or an explicit discard. No exact artifact must stay readable, and this record does not depend on the workspace remaining fetchable. The workspace is retirement-eligible once this reconciliation lands and its independent review validates the transfer.

No separate historical decision-rationale record is kept. The decisive trade-offs are current constraints on reading the result, so they live in the qualifications and the claims that did not survive. The choice is narrow and easy to revisit. One synthesis-seed occurrence is recorded: the audits' green result was a claim about a control, not evidence of its effect on new statements.

| Result | Fate |
|---|---|
| Bounded verdict, scope and human-evidence limits | Here, the register, the consumer plan and the implementation gate |
| Qualifications 1–8 and the claims that did not survive | Here; their requirement consequences in the consumer plan |
| Guardrails, comparison truth, idle classification, method choices, presentation requirements | The consumer plan |
| Negative cases | Here, with parameters or maintained fixtures; named in the consumer plan's acceptance requirement |
| Corpus, audits, bakery scenarios and missions, viewer, walkthrough packs, generators | Discarded. The proving cases and requirements carry what a future slice needs, and copying scripts or answer keys would wrongly suggest a chosen curriculum |
| Owner-session records and discovery notes | Discarded after transfer into the framing, qualification 1 and the presentation requirements |
| Counter-removal scope and history | Already carried out by its own reviewed change; history kept here as qualification 8 and one proving case |
| Concept map and proposed follow-up questions | Not admitted; recorded under the reopening triggers |
| Reconciliation compatibility port | Discarded after recording its outcome under Evidence basis |

## Deferred external-player validation

Independent player validation was deliberately not an exit criterion, because no independent participant pool exists. The separate [external-player validation candidate](factory-design-game-vertical-slice.md#external-player-validation--candidate) is promoted when independent target users become available or a release objective makes a comprehension claim material. It defines its own population, tasks, thresholds and decision at stake, and may reuse the proving cases above. Until then Arcogine may claim only that the promoted diagnostic requirements are mechanically grounded and were smoke-tested by the owner, not population-validated.
