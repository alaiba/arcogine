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

## Player-facing evidence under test

The product research should determine which presentation lets a player answer:

- Where is work waiting?
- Which operation or resource is the current bottleneck?
- Which resources are saturated or underused?
- How much time is attributable to processing versus transfer?
- How far is the production requirement from completion?
- What materially changed between this attempt and the previous attempt?

Candidate techniques may include resource overlays, queue indicators, flow/transfer visualization, timeline summaries, bottleneck callouts, and attempt comparison. These are consumer presentation hypotheses. They do not become Engine semantics unless a concrete missing supported observation is proven.

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

## Exit and promotion

This research is complete when the product team can state a bounded playable requirement set, supported by playtest/prototype evidence, that no longer depends on unresolved consumer decisions.

Only then promote the selected requirements into the game consumer implementation plan. If the research shows that the loop is not understandable or engaging, record that conclusion rather than manufacturing implementation work.
