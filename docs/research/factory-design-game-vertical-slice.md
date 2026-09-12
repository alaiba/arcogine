# Factory-Design Game Vertical-Slice Research

> **Status:** READY  
> **Scope:** Test the product hypothesis for the first playable factory-design game before playable implementation is admitted to delivery planning  
> **Authority:** Research/product evidence only; this document does not authorize game implementation or change Arcogine semantics

## Research question

Can Arcogine's deterministic production-system semantics support a factory-design puzzle in which a player can understand, test, and improve meaningful capacity, layout, and capital trade-offs?

The candidate loop is:

```text
Understand a fixed production requirement
        |
        v
Design a factory
        |
        v
Publish the design
        |
        v
Run production
        |
        v
Diagnose queues, utilization, transfers, and bottlenecks
        |
        v
Revise and compare
```

The intended player role is a production-system designer or industrial engineer, not a live shift operator. The product is successful only if the player can explain **why** a design performs better, not merely discover a hidden score formula.

## Hypotheses to test

1. **Capacity is legible.** Adding compatible capacity at a true bottleneck can improve the fixed production requirement, while adding capacity elsewhere may not.
2. **Spatial arrangement is consequential.** Shorter production flows can compete with additional equipment because transfer consequences are deterministic and observable.
3. **Capital creates a real trade-off.** More equipment is not universally optimal once construction cost is considered.
4. **Diagnosis is possible from supported evidence.** The player can identify the bottleneck and major delay sources without reading raw scheduler/event internals.
5. **Retry behaves like an experiment.** A player can relate a changed draft to changed authoritative outcomes and learn from the difference.
6. **Several credible solutions exist.** The reference challenge does not collapse to one obvious dominant strategy.

## Reference challenge candidate

Use one fixed game-owned production requirement with a simple three-step routing, for example:

```text
Product A
CUT -> ASSEMBLE -> INSPECT
```

Candidate content parameters may begin around:

```text
quantity:       20 units
floor:          12 x 10 cells
budget:         40,000 credits
deadline:       400 simulation ticks
```

These numbers are **research/playtest parameters**, not Arcogine contracts. Tune or replace them when evidence shows that they do not create useful decisions.

The challenge should make at least two approaches credible, such as:

- spend more capital on parallel bottleneck capacity; or
- retain less capacity and reduce transfer loss through layout.

Arcogine's accepted order/work-item semantics remain authoritative: one accepted quantity-bearing requirement is decomposed by Arcogine into independently dispatchable unit work while aggregate progress remains order-level. The game must not manufacture multiple production orders merely to create parallelism.

## Player-facing presentation under test

This is now a **presentation and comprehension** question, not an ownership question. The bounded form is:

> Given supported Arcogine simulation facts and analytics, which presentation lets players correctly identify bottlenecks, major delay sources, and the causal effect of a design change?

The product research should determine which presentation lets a player answer:

- Where is work waiting?
- Which operation or resource is the current bottleneck?
- Which resources are saturated or underused?
- How much time is attributable to processing versus transfer?
- How far is the production requirement from completion?
- What materially changed between this attempt and the previous attempt?

Candidate techniques may include resource overlays, queue indicators, flow/transfer visualization, timeline summaries, bottleneck callouts, and attempt comparison. These are consumer presentation hypotheses. They do not become Engine semantics unless a concrete missing supported observation is proven.

### Boundary with the analytics question

Which *facts and reusable derivations* exist, and who owns them, is no longer decided here. That is [Simulation analytics consumer boundary](simulation-analytics-consumer-boundary.md), an open High-risk investigation. Accordingly:

- overlays, timelines, callouts, wording, tutorial sequencing, and progressive disclosure remain game research;
- **the game must not invent shared KPI or diagnostic formulas while analytics ownership is unresolved.** If a presentation needs a reusable measure, that measure's ownership is an input from the analytics question, not a game decision;
- transfer-dependent presentation remains conditional on landed transfer semantics — there is currently no transfer time, `TRANSFERRING` state, or transfer event in runtime behavior, so the processing-versus-transfer question cannot be asked of the current runtime at all;
- sidecar versus in-process is a packaging/integration question, not a competing runtime semantics choice (ADR-0007, ADR-0011 already establish transport-neutral semantics with sibling adapters).

A prior investigation of the mixed player-facing-evidence question is recorded as **SUPERSEDED** in `docs/research/README.md`. Its player-comprehension half is the open work described here; its technical-ownership half moved to the analytics question.

## Product decisions to resolve before implementation

Research should resolve or deliberately defer these consumer choices:

| Decision | Evidence needed |
|---|---|
| Rendering/input technology | Target platforms, packaging constraints, team capability, and a representative interaction/performance prototype |
| Sidecar versus supported in-process integration | Available Arcogine consumer surfaces plus target runtime/packaging constraints |
| Draft-to-canonical projection UX | Editor usability prototype against the actual canonical model contract |
| Visual interpolation policy | Supported event/observation timing plus desired presentation behavior |
| Game-save wrapper | Available checkpoint/recovery contract plus game-owned persistence needs |
| Scoring formula | Playtests showing understandable trade-offs without an opaque dominant meta |
| Tutorial sequence | First-time-user observation showing which concepts require instruction versus discovery |

These decisions are intentionally absent from executable planning until evidence selects a contract.

## Product success criteria

The hypothesis is supported when evidence shows that:

1. players understand the objective, constraints, and available equipment without Arcogine-specific tooling knowledge;
2. capacity investment creates an understandable conditional trade-off;
3. spatial arrangement creates an understandable performance trade-off;
4. solving one bottleneck can expose another;
5. players can identify bottlenecks and major delay sources from presented evidence;
6. retrying after a design change is perceived as an informative experiment;
7. at least two credible solutions exist for the reference challenge; and
8. scoring or rating rewards useful performance without obscuring the causal explanation.

## Arcogine boundary

Research must not use game pressure to invent shared semantics. In particular:

- game catalogue prices, availability, score, progression, tutorial state, and presentation remain game-owned;
- canonical executability remains Arcogine-owned;
- production workload, dispatch, queues, processing, transfers, time, and performance facts remain Arcogine-owned;
- game attempt snapshots may explain player-authored differences without requiring a new canonical model-diff abstraction;
- unresolved equipment ontology, spatial evolution, and richer design comparison remain separate research questions.

## Playtest protocol

Player comprehension cannot be settled by repository reasoning. Established evidence on dynamic-system learning (Sterman, *Learning in and about complex systems*, System Dynamics Review 10(2–3), 1994) is the specific reason: outcome feedback alone — retry and watch the score move — does not reliably produce understanding, and its misperception effects proved robust to experience and incentives. So the loop's understandability claim requires participant evidence, not argument.

The smallest study that would settle it:

- **Material:** 6–8 pre-recorded traces from the reference challenge, generated headlessly with retained supported runtime events. Ground truth is computed from the trace, not authored. The set must include: one true capacity bottleneck; one capacity-added-at-constraint pair; one capacity-added-away pair with a null outcome delta; one constraint-migration pair; one starvation-versus-surplus case; and one deliberately confounded pair where two variables changed at once.
- **Participants:** 8–12, split between engineering-literate and non-specialist. This is a "does the presentation systematically mislead" question, not an effect-size question — sufficient for a first pass, insufficient for a comparative claim between two designs.
- **Tasks, scored against ground truth rather than preference:**
  1. Name the constraining operation or resource.
  2. For a named idle resource, say whether it was starved or surplus.
  3. Name the largest source of delay.
  4. Given two candidate interventions, predict which improves completion.
  5. Given an attempt pair, explain why the second performed differently.
  6. Given the confounded pair, say what can and cannot be attributed. **"Cannot attribute to one change" is the correct answer**, and scoring it as correct is the point of the item.
- **Pre-registered falsification thresholds** — fix these before running, and treat a miss as falsification rather than as a tuning signal:
  - < 70% correct on task 1 → the constraint presentation fails;
  - < 60% correct on task 2 → the starved/surplus distinction is not landing;
  - < 50% correct on task 6 → the comparison presentation is manufacturing causal confidence, which is worse than showing less;
  - any case where a majority reads the capacity-added-away pair as an improvement → the null-result presentation fails.
- **Do not** ask which visualization participants preferred, or collect self-reported insight as the primary measure.

## Truthful explanation constraints

Whatever presentation the research selects must respect what the evidence actually licenses. These constrain the product, not the Engine.

The game may state, **as measured fact**, things of this shape:

- how many units waited for a given operation step, and for how long in total;
- how long a resource was occupied, and its longest uninterrupted active period;
- that one resource was idle while units waited for another;
- each attempt's completion tick;
- what the player changed between two attempts (known from the game's own draft snapshots).

The game may state, **as a named interpretation**, that under a named detection method a particular resource was the constraint in one attempt and a different one in the next — so the constraint moved. The method must be named and its supporting numbers shown.

The game may state a **causal explanation only when exactly one variable changed**: naming the change and the effect side by side, not asserting a mechanism. Deterministic re-execution makes that controlled comparison genuinely available, and it is the strongest attribution the evidence supports.

The game must **not**:

- attribute an improvement to one change when several changed — state the change set and the outcome delta, and offer a controlled re-run instead;
- make any causal claim about placement while placement has no runtime consequence;
- present instantaneous utilization derived from a completion-credited cumulative busy-time counter;
- present a "blocked" state, which is unreachable while queues are unbounded, or any queue count that includes multi-eligible waiting work more than once;
- show a bare bottleneck badge with no method and no supporting numbers, which presents a consumer heuristic as an authoritative verdict.

The general rule: **prefer the weaker true statement.** Where the evidence supports only association, say association. A null result is a result.

## Exit and promotion

This research is complete when the product team can state a bounded playable requirement set, supported by playtest/prototype evidence meeting the protocol above, that no longer depends on unresolved consumer decisions.

Only then promote the selected requirements into the game consumer implementation plan. If the research shows that the loop is not understandable or engaging, record that conclusion rather than manufacturing implementation work.
