# Factory-Design Game Challenge Delivery Plan

> **Status:** COMPLETE through the currently admitted headless challenge capability; no additional Challenge implementation is admitted  
> **Scope:** Record the implemented game-owned challenge substrate and its downstream integration boundary  
> **Authority:** Planning only; product/gameplay hypotheses live in research and current capability remains defined by landed code/tests

## 1. Ownership boundary

The Challenge layer owns deterministic game rules over game-owned candidate facts and authoritative Arcogine outcomes. It does not simulate production.

```text
Challenge definition / catalogue / budget
        +
Candidate draft snapshot
        |
        v
candidate admissibility
        |
        v
Arcogine projection / validation / runtime   [outside Challenge]
        |
        v
supported authoritative outcome facts
        |
        v
challenge evaluation
        |
        v
attempt provenance / comparison
```

Challenge rules may reject a canonically executable factory for budget/catalogue reasons. Challenge admission never proves Arcogine executability.

## 2. Completed implementation sequence

### PLAN-CHAL-1 — Challenge definition, identity, and validation — COMPLETE

Implemented headlessly in `:challenge`:

- immutable challenge identity/content version;
- evaluation-policy identity/version;
- floor, budget, fixed-workload, catalogue-reference, and deadline facts;
- deterministic scalar/structural validation with stable issue code/path/message;
- no dependency on Factory runtime or UI modules.

Definition validation does not replace `FactoryModelValidator`. External content decoding/loading is a separate responsibility from validating an already-constructed definition.

### PLAN-CHAL-2 — Catalogue, draft economics, and candidate admissibility — COMPLETE

Implemented:

- game-owned `EquipmentCatalogue` / `EquipmentOffer` identity and purchase-cost/quantity-limit semantics;
- deterministic `DraftEconomics` calculation with explicit failure for unresolved references/overflow;
- immutable candidate draft/placement values;
- deterministic admissibility for identity, resolution, availability, quantity, affordability, floor bounds, overlap, and duplicate occurrence identity;
- fixed challenge workload cannot be silently replaced by a candidate.

The catalogue is a game concept. It does not assert that Arcogine's current concrete resource representation is a reusable equipment type. The canonical equipment/resource ontology question is tracked in [Factory Design Evolution Research](../research/factory-design-evolution.md).

### PLAN-CHAL-3 — Deterministic challenge evaluation — COMPLETE

Evaluation is deterministic over explicit challenge/evaluation-policy identity, authoritative outcome facts, model/run provenance where supported, and game-owned economics.

The evaluation-policy identity/version is the semantic identity of evaluation behavior: a result-affecting policy change requires a new version.

Evaluation never invents production facts that Arcogine did not report.

### PLAN-CHAL-4 — Attempt provenance and comparison — COMPLETE

Implemented attempt records preserve:

- exact challenge and evaluation-policy identity/version;
- retained candidate/economics facts;
- authoritative outcome/result attribution;
- non-recomputed historical evaluation result; and
- deterministic comparison with explicit incompatibility when challenge/policy versions differ.

Game attempt comparison is not canonical Factory semantic comparison and does not require additional Factory diff semantics.

### PLAN-CHAL-5 — Data-driven challenge content and reference fixtures — COMPLETE

Implemented content loading/validation and reference fixtures prove that challenge definitions/catalogue/evaluation policy can be supplied as data rather than hard-coded test setup, while malformed input fails deterministically.

The content layer remains game-owned and does not become a general configuration/interchange framework.

## 3. Required downstream invariants

Any consumer of the completed Challenge capability must preserve:

- Challenge state and production runtime state remain separate;
- game catalogue/economics do not enter the canonical Factory model;
- admitted candidate does not imply canonically executable factory;
- canonical execution/outcome facts come from Arcogine, not Challenge reconstruction;
- evaluation behavior is versioned by evaluation-policy identity;
- attempts remain attributable to exact challenge/policy/input/outcome facts;
- Challenge history is not RuntimeEvent history or Governance controlled-revision history.

## 4. No further implementation admitted

The current Challenge delivery sequence is closed. New Challenge work requires a concrete promoted requirement from the playable-game/product research or another consumer.

Examples such as campaign progression, leaderboard persistence, richer scoring, or shared Governance/Challenge evaluation abstractions are not roadmap placeholders. Track them in research/product work until a real requirement crosses the planning admission boundary.

The current product investigation is [Factory-Design Game Vertical-Slice Research](../research/factory-design-game-vertical-slice.md).

## 5. Integration milestone

The implemented headless capability can already prove, using game-owned candidate/economics facts plus synthetic authoritative outcome fixtures:

1. load and validate one versioned challenge/catalogue/evaluation policy;
2. admit/reject candidates with deterministic structured reasons;
3. evaluate exact outcome facts deterministically;
4. retain attributable immutable attempts; and
5. compare attempts without runtime internals.

A playable/runtime-integrated milestone belongs to [Factory-Design Game Consumer Implementation Plan](factory-design-game-consumer.md) only after the product research and required Engine/Factory gates are satisfied.
