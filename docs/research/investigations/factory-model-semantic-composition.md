# Factory Model Semantic Composition Research

> **Lifecycle:** READY — see the maintained [research register](../research-register.md)  
> **Scope:** Canonical Factory-model structure, optional semantic concerns, identity, validation, and Engine applicability when not every modeled factory needs every behaviorally relevant concern  
> **Authority:** Research framing only. ADR-0017/ADR-0018 own the reset and surviving Factory constraints; the current V1/V2 implementation is evidence, not a post-reset support commitment.
> **Risk:** High. Independent adversarial review is required before any conclusion is promoted into Factory architecture, fingerprint policy, Engine applicability, or implementation planning.

## Question

> How should Arcogine represent independently applicable authored Factory semantics so that adding a new concern does not automatically turn feature accumulation into a linear whole-model `factory-model:vN` progression?

The investigation must determine whether the current v1/v2 shape is genuine whole-model semantic evolution or evidence that the Factory model contains composable semantic concerns with different applicability.

It must also determine what **absence** of such a concern means. In particular, a model that does not author spatial semantics must not acquire invented coordinates, zero handling time, or another synthesized default merely to satisfy one monolithic schema.

## Decision at stake

The V1/V2 description below records the pre-reset design baseline being tested.
Its support obligations have been withdrawn by ADR-0017/ADR-0018; the retained
authored semantics and validation constraints remain binding until explicitly
changed by an owning decision. The investigation must re-ground against those
successors, not treat ADR-0006/ADR-0014 as still Accepted.

Current `factory-model:v1` is primarily the production-structure substrate:

- products;
- operations and ordered steps;
- complete configured productive resources;
- explicit eligible-resource references; and
- the existing canonical ordering/equality semantics.

[ADR-0014](../../architecture/decisions/0014-factory-model-semantic-policy-evolution.md) defines `factory-model:v2` as V1 plus five mandatory authored additions:

- floor width/height;
- resource reference-cell position;
- resource footprint width/height;
- `ticksPerCell`; and
- `handlingTicks`.

The same ADR deliberately excludes orientation, paths, graph edges, aisles, conveyors, transport resources, obstacles, congestion, floor identity, connection points, and route topology until later capabilities make them behaviorally relevant.

Meanwhile, [Factory Resource Semantics](../../architecture/factory-resource-semantics.md) already treats configured identity, reusable specification, capability/qualification, hierarchy, spatial placement, external asset identity, and runtime state as orthogonal semantic concerns whose independent meaning must be proved rather than collapsed.

That creates a design question: if future storage zones, richer geometry, material-flow topology, capability/qualification, or hierarchy are not universally needed, should each new concern create a new generation of the **entire** Factory contract?

The decision affects:

- canonical model shape;
- publication validation;
- `ModelFingerprint` meaning and canonicalization;
- controlled-revision comparison/history;
- Engine applicability and required authored facts;
- authoring consumers that intentionally model only a subset of concerns;
- future interoperability adapters; and
- whether Factory evolves as a linear schema sequence, a semantic composition, profiles, independently durable components, or another model.

## Current implementation evidence

The investigation must use current implementation as evidence without treating implementation shape as architectural truth.

In particular:

- `FactoryModel` is deliberately narrow and currently contains resources, operations, and products;
- `FactoryModelV2` is implemented as a distinct type that reuses existing V1 concepts and adds spatial/handling facts;
- `FactoryModelV2Validator` exists;
- V2 canonical bytes, fingerprint derivation, policy registration, and V1/V2 coexistence are **not** yet implemented;
- the current planning split intentionally landed V2 shape/validation before releasing a V2 fingerprint policy; and
- current Engine spatial consequences are not yet fully implemented.

This is useful proving evidence because the cost of the current linear policy has started to become concrete while the irreversible coexistence machinery is still incomplete.

## Candidate models to test

Do not assume "composition" already means one specific implementation. Evaluate at least these candidates.

### Candidate A — linear whole-model policies

Retain the current rule:

```text
factory-model:v1
factory-model:v2
factory-model:v3
...
```

Every new behaviorally relevant authored fact that cannot fit the released whole-model grammar creates a new complete Factory policy. Older policies remain separately resolvable.

This candidate must explain how optional concerns and branching feature needs avoid turning the version number into a chronological feature bundle.

### Candidate B — one canonical aggregate with typed optional semantic concerns

One Factory design has a required semantic substrate plus explicitly present authored concerns.

Conceptually, without fixing Java types:

```text
FactoryModel
    production structure        required
    spatial/layout              optional
    storage                     optional
    material-flow topology      optional
    qualification/capability    optional
    hierarchy/scope             optional
    ...
```

The complete authored aggregate has one `ModelFingerprint`. Presence/absence and all authored facts that participate in the model are fingerprinted.

An absent concern means the design makes no claim in that semantic dimension; it does not imply invented defaults.

### Candidate C — independently identified/versioned semantic components

Production, spatial, storage, qualification, hierarchy, or other concerns have independent semantic identities/fingerprints and compose into one Factory definition.

This candidate must justify the additional compatibility-state and provenance complexity with a real independent-evolution requirement rather than assuming modular code requires modular identity.

### Candidate D — supported Factory profiles/capability sets

A small number of named profiles define coherent supported combinations, such as a production-only profile versus a spatial-production profile.

This candidate must explain whether profiles represent real semantic contracts or merely hide the same chronological feature accumulation behind names.

### Candidate E — another hybrid

The researcher may introduce a hybrid if evidence requires it—for example one whole-model identity with internally versioned aspects only after an aspect independently earns durability.

Do not create a generic plugin/extension framework merely because future concerns are imaginable.

## Required proving cases

Apply every serious candidate to all of these cases.

### 1. Core-only deterministic production

A factory needs products, operations, configured resources, eligibility, capacity/concurrency, and durations but no physical layout.

The model must remain a first-class truthful Factory design rather than an obsolete/degraded version.

### 2. Current spatial transfer

A factory additionally authors floor extent, placement, footprint, and handling magnitudes needed by current deterministic transfer semantics.

The candidate must preserve authored-fact versus Engine-interpretation ownership.

### 3. Logical storage without geometry

A consumer needs buffers/storage capacity, occupancy, blocking, or admissible material semantics but does not care where those buffers are physically located.

Test whether storage can exist without forcing spatial layout.

### 4. Spatial storage zones

A design needs storage semantics plus spatial regions/zones.

Test cross-concern relationships without requiring the concerns to collapse into one identity merely because they interact.

### 5. Richer geometry

A design needs non-rectangular floors or machinery, orientation, polygons, composite regions, or another geometry representation.

Test whether geometry evolution should imply a new generation of products/operations/resources or can evolve within a narrower semantic boundary.

### 6. Explicit material-flow topology

A design needs paths, aisles, connection points, conveyors, reachability, adjacency, or route topology.

Test whether topology is spatial layout, a separate authored concern, an Engine interpretation, or some combination with explicit ownership.

### 7. Contended transport resources

A design needs forklifts, conveyors, workers, vehicles, or another independently contended transport participant.

Test whether this belongs in Factory production/resource semantics, material-flow semantics, or another proven relation without forcing every Factory to declare transport resources.

### 8. Qualification and resource-dependent performance

A future consumer needs capability/qualification and potentially resource-dependent duration/cost/consumption.

Test whether these facts compose independently of spatial semantics and whether their existence requires a whole-model generation.

### 9. Hierarchy/work-center/resource-pool scope

A consumer needs grouping that owns consequential scheduling, capacity aggregation, responsibility, reporting, or authorization semantics.

Test independence from spatial proximity and configured-resource identity.

### 10. Cross-consumer partial models

Test at least:

- a simple game/experiment that only needs production flow;
- an optimizer that may need spatial/handling data;
- an industrial authoring/importer case with richer engineering facts;
- Engine execution that requires a declared subset of authored semantics; and
- Governance comparison/history over designs with different concern sets.

### 11. Historical revision and comparison

A controlled revision moves from a core-only design to one that authors a new concern, or removes/changes one.

The candidate must explain:

- fingerprint identity;
- historical resolution;
- comparison semantics;
- absence versus invented defaults; and
- whether cross-concern evolution requires a special migration classification.

### 12. Cross-concern validation

Examples:

- footprint lies inside the floor;
- a storage zone references a represented region;
- a route endpoint references a represented connection point;
- a qualification relation references an existing resource/operation.

Test how validation can express dependencies without turning every possible concern into a mandatory monolith.

## Questions the report must answer

The final report must resolve:

1. What is the minimum required semantic substrate for something to be a canonical Arcogine Factory design?
2. Which current V1 facts belong to that substrate, and which are merely current implementation details?
3. Is "spatial layout" one concern or several distinct authored concerns such as geometry, placement, topology, and handling characteristics?
4. What does absence of an optional concern mean semantically?
5. Which combinations are valid, and how are impossible/incoherent combinations rejected?
6. Does one complete Factory aggregate retain one `ModelFingerprint`, or do any components need independent identity?
7. If a component has independent identity, what invariant and independent evolution requirement justify it?
8. How are canonical bytes/fingerprints defined without creating a combinatorial version matrix?
9. Can canonicalization be deterministic when concerns are optional and extensible?
10. How should Engine semantics state the authored Factory facts it requires—whole-model policy number, semantic capabilities, structural predicates, another mechanism, or no explicit declaration?
11. How does Engine applicability fail when required semantics are absent?
12. How do consumers distinguish "not modeled" from zero/default/unknown?
13. How should richer geometry evolve from rectangles without pretending unrelated production semantics have advanced a generation?
14. Can storage exist without spatial layout? Can spatial layout exist without storage? What evidence decides?
15. How do controlled revisions and semantic comparison cross changes in represented concerns?
16. When, if ever, should one concern become an independently durable/versioned semantic contract?
17. How does the sibling [Semantic Contract Maturity and Durability](semantic-contract-maturity-durability.md) result constrain any version/durability decision?
18. What should happen to the already implemented `FactoryModelV2`/validator if the surviving semantic boundary differs from current V2?
19. What exact accepted ADRs/specifications/plans would need supersession or reconciliation?
20. What evidence would falsify the surviving composition model?

## Identity hypotheses to test explicitly

The investigation must keep these distinct:

```text
Factory design identity
    exact authored aggregate that was published

semantic concern structure
    which authored dimensions the aggregate represents

semantic-contract maturity
    how permanently Arcogine has committed to the meaning of those dimensions

controlled revision identity
    which governed historical occurrence referenced the design
```

Do not infer that structural composition requires multiple fingerprints.

Likewise, do not infer that one whole-model fingerprint requires one monolithic, ever-growing schema generation.

## Engine applicability hypothesis

Test, rather than assume, the following shape:

```text
Engine interpretation
    requires certain authored semantic facts

Factory design
    either supplies those facts or does not

applicable
    only when the required authored semantics are represented and valid
```

Under this hypothesis a production-only Factory is not "old"; it is simply inapplicable to an Engine interpretation that requires spatial transfer facts.

The research must determine whether this is superior to binding Engine applicability directly to `factory-model:vN`.

## Evidence requirements

### Repository evidence

At minimum inspect:

- Product Charter "one model, many views" and semantic-continuity principles;
- Factory Design architecture;
- Factory Resource Semantics;
- ADR-0003, ADR-0004, ADR-0006, ADR-0014, ADR-0015;
- Factory Model v1 implementation/canonicalization and V2 specification/implementation/validator;
- Governance semantic comparison and controlled-revision resolution;
- current Engine spatial plan and Engine Semantics v1;
- Factory Design evolution research, especially deferred capability, pool/hierarchy, spatial/material-flow questions;
- ISA-95 mapping and standards-alignment material where it discriminates ownership; and
- current consumers/adapters sufficient to test whether optional concerns reflect real usage rather than hypothetical modularity.

### External evidence

Use external manufacturing/digital-twin/schema literature only where it discriminates among candidates.

Potentially useful evidence includes information models that separate process structure, spatial geometry, hierarchy, transport topology, storage, or qualification. Treat those as evidence about possible boundaries, not as authority to import an external ontology wholesale.

## Falsification criteria

The compositional hypothesis should be rejected or narrowed if evidence shows, for example, that:

- every behaviorally relevant Factory concern necessarily changes one inseparable canonical meaning;
- optional concern presence creates ambiguous identity or non-deterministic canonicalization;
- Engine applicability becomes less truthful or substantially more complex than whole-policy selection;
- cross-concern validation requires so much coupling that the proposed separation is nominal only;
- independent concerns cannot evolve without a compatibility matrix worse than linear policies; or
- no real consumer can use a partial concern set.

The linear-policy candidate should be rejected or narrowed if distinct real consumers repeatedly need different subsets and each new orthogonal concern forces unrelated existing semantics into another permanent generation.

Independent component identities should be rejected unless they preserve a necessary statement or evolution boundary that one aggregate fingerprint cannot preserve.

## Exit criteria

The investigation is complete only when it:

1. compares all serious candidates against every proving case;
2. defines the irreducible Factory substrate;
3. defines presence/absence semantics for additional authored concerns;
4. settles whole-model versus component identity at the semantic level;
5. defines deterministic publication/canonicalization requirements sufficiently to assess feasibility;
6. defines Engine applicability to partially represented Factory concerns;
7. explains cross-concern validation and controlled-revision comparison;
8. tests geometry/storage/topology/qualification/hierarchy evolution without implementing those future capabilities;
9. states how current V1/V2 artifacts and the landed V2 model/validator would be treated under the surviving model;
10. states the smallest ADR/architecture/planning consequences without performing them;
11. records unresolved questions and reopening triggers; and
12. receives independent adversarial review before any conclusion is promoted.

## Non-goals

This investigation does **not**:

- implement storage, richer geometry, pathfinding, conveyors, transport resources, capability inference, or hierarchy;
- select Engine distance, routing, reservation, congestion, or scheduling policy;
- create a generic extension/plugin framework;
- introduce independently versioned subcontracts merely because code can be modularized;
- automatically preserve or discard current `factory-model:v1/v2`;
- invent defaults for a concern the authored design did not represent;
- decide semantic-contract durability independently of the sibling maturity investigation; or
- rewrite Accepted ADRs in place.

## Coordination with semantic-contract maturity research

The [maturity question](semantic-contract-maturity-durability.md) is concluded through
[ADR-0017](../../architecture/decisions/0017-ground-zero-semantic-evolution.md) and
[ADR-0018](../../architecture/decisions/0018-factory-semantics-after-support-reset.md).
This investigation must incorporate their reset, non-reuse, accepted-use,
definition-retention and whole-definition freeze constraints. It chooses no support
estate merely by selecting a composition; owning declarations must specify that scope.

The V1/V2 descriptions and linear-policy candidates above remain baseline evidence.
ADR-0006 and ADR-0014 are now superseded: preserving their pre-reset support estate
is no longer required. Authored facts, validation, no-lift, explicit comparison and
domain ownership survive through ADR-0018. The composition question remains READY;
no core/aspect/profile answer is selected here.

## Expected durable destination

No durable destination is preselected.

Depending on evidence, reconciliation may:

- retain the current linear Factory policy;
- supersede ADR-0018 if a compositional result changes its retained Factory decisions;
- define a fresh Factory identity contract within ADR-0017's evolution/support rules;
- retain one `ModelFingerprint` while changing canonical model composition;
- introduce a narrowly justified independent semantic component only where evidence proves it;
- reframe Engine applicability away from whole-model policy numbers;
- replan or remove V1/V2 coexistence work; or
- conclude that the current V2 design is the correct first durable whole-model contract.

The research report itself must not perform those promotions.
