# Factory-Design Game Challenge Readiness

> **Status:** Proposed  
> **Scope:** Establish the game-owned challenge, admissibility, evaluation, attempt, and comparison capabilities that can progress independently of factory runtime readiness  
> **Authority:** Planning only; this document does not describe current product capability or accepted architecture  
> **Related:** [Factory-Design Game Vertical Slice](factory-design-game-vertical-slice.md), [Factory-Design Game Consumer Initiative](factory-design-game-consumer.md), [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md), [Governance and Conformance Capability](governance-conformance-capability.md), [Factory Design Capability](factory-design-capability.md), [Product Charter](../product/charter.md)

## 1. Purpose

The factory-design game needs a second engineering track alongside [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md).

Engine readiness answers:

> What happened when this published production system executed this workload?

Challenge readiness answers:

> Was this candidate allowed under the challenge, was its resulting outcome good under the exact challenge rules, and how should the attempt be explained and compared?

The tracks are deliberately orthogonal:

```text
Factory Simulation Engine Readiness
    production truth
    orders/work
    dispatch
    session control
    observations
    spatial consequences

Factory-Design Game Challenge Readiness
    challenge definition
    challenge identity/versioning
    game catalogue and budget
    candidate admissibility
    success/failure rules
    deterministic evaluation
    attempts and comparison
    evaluation provenance
```

The challenge layer interprets game-owned draft facts and authoritative Arcogine outcome facts under game rules. It must not simulate production, reconstruct queues, implement dispatch, derive transfer behavior, or depend on mutable runtime internals.

## 2. Parallel-development policy

Challenge readiness should be useful before the final Arcogine observation contract exists.

Early challenge implementation may therefore consume synthetic supported-outcome fixtures or a narrow game-owned port representing only the outcome facts required for evaluation. These fixtures are test inputs, not an alternative production model.

Conceptually:

```text
Synthetic supported-outcome fixture
        |
        v
Challenge evaluator

later

Arcogine supported observation/result
        |
        v
thin consumer adapter
        |
        v
Challenge evaluator
```

The challenge evaluator must not depend on `FactoryRuntime`, scheduler internals, stores, Spring DTOs, or undocumented event reconstruction.

Likewise, challenge admissibility must be testable headlessly from challenge state plus a game-owned candidate draft/attempt snapshot. It must not be implemented only as UI affordances.

This policy lets Engine Readiness change work-item, dispatch, session, and observation internals without forcing the challenge track to follow those refactors.

## 3. Ownership boundary

The challenge track owns game semantics such as:

- challenge identity and content version;
- fixed contract presentation and game-specific constraints;
- equipment catalogue availability and construction prices;
- purchase/resale rules and construction budget;
- candidate admissibility under game rules;
- game success/failure conditions;
- score, rating, medal, or equivalent evaluation;
- attempt history and game-facing comparison;
- challenge/evaluation-policy provenance.

Arcogine remains authoritative for:

- canonical factory-model semantics and executability validation;
- published model identity/provenance;
- production workload and work execution;
- resource eligibility and dispatch;
- queues, processing, and transfers;
- simulation time/event ordering;
- runtime observations and performance facts.

A challenge may say that completion before tick 400 earns a bonus. It may not invent a completion time that Arcogine did not report. A challenge may reject a draft because it uses a disallowed catalogue item or exceeds budget. It may not claim that an admitted draft is an executable factory; Arcogine validation still decides that after projection.

## 4. Delivery sequence

```text
C1  Challenge definition, identity, and validation
    |
    v
C2  Catalogue, construction budget, draft economics, and candidate admissibility
    |
    v
C3  Deterministic challenge evaluation
    |
    v
C4  Attempt provenance and design-to-design comparison
    |
    v
C5  Data-driven challenge content and reference fixtures
```

C1-C5 are game-owned capabilities. None is evidence that an Engine Readiness gate is satisfied.

## 5. C1 — Challenge definition, identity, and validation

### Goal

Represent one immutable, identifiable challenge independently of mutable player state and production runtime state.

A challenge needs concepts equivalent to:

```text
Challenge
    stable identity
    content/rules version
    floor/game constraints
    available catalogue
    construction budget
    production contract reference/input
    deadline
    evaluation policy identity/version
```

Exact type names and serialization format remain implementation decisions.

### Validation boundary

C1's `ChallengeDefinitionValidator` answers only whether an already-constructed
`ChallengeDefinition`'s own scalar/structural content is internally coherent:

- Is the budget non-negative?
- Is the deadline positive?
- Are floor dimensions positive?
- Is the workload reference present and its quantity positive?
- Are available catalogue-item identities present and free of duplicates?
- Are identity/evaluation-policy id and version present?

It does **not** replace `FactoryModelValidator`, which answers whether projected production
semantics are executable by Arcogine.

Questions that require resolving a `ChallengeDefinition`'s references against other state --
whether referenced catalogue entries exist in an actual catalogue, whether the workload's success
condition is unambiguous once evaluation semantics exist, whether rating/score thresholds are
coherent -- belong to later gates (C2's catalogue seam and C3's evaluation-policy work,
respectively) once that state exists to validate against. C1 does not claim to answer them.

C1 also does not define a content-loading layer. `ChallengeDefinition` and its nested value
records reject structurally absent required fields (a null identity, a null workload, a null
nested id/version/reference, a null equipment element) at construction time via ordinary
constructor invariants (`NullPointerException`), not via `ChallengeDefinitionIssue` diagnostics.
Converting an untrusted external representation (e.g. loaded JSON/TOML) into either a valid
`ChallengeDefinition` or a diagnostic explaining why it couldn't be constructed is the
responsibility of the content-loading layer introduced in C5; C1 supplies the validator that
layer will run once a definition exists, not a replacement for it.

### Acceptance criteria

C1 is ready when:

1. A challenge can be loaded independently of an active simulation session.
2. Challenge identity and content/rules version are explicit.
3. A structurally-constructed but scalar/content-invalid `ChallengeDefinition` (blank ids,
   non-positive dimensions/budget/deadline/quantity, duplicate catalogue ids, etc.) produces
   deterministic, actionable `ChallengeDefinitionIssue` diagnostics. This criterion covers content
   validity of a constructed definition -- it does not cover translating absent/malformed
   external input into diagnostics; that is C5's content-loading responsibility (see "Validation
   boundary" above).
4. Challenge-definition validation does not duplicate Arcogine factory-model validation.
5. Game-only fields do not enter the canonical factory model merely to support challenge loading.

### Implementation status (C1)

C1 is implemented as a new headless Gradle module, `:challenge` (`product/consumer/challenge`,
package `com.arcogine.challenge`), with no `project(...)` dependency on `:types`, `:simulation`,
`:factory`, `:economy`, `:finance`, `:api`, or `:cli`.

Implemented:

- Immutable value records: `ChallengeIdentity`, `EvaluationPolicyIdentity`,
  `EquipmentCatalogueItemId`, `ChallengeWorkload`, `FactoryFloorConstraint`, and the aggregate
  `ChallengeDefinition`. Construction defensively copies the available-equipment collection so
  caller mutation cannot affect an already-constructed definition.
- `com.arcogine.challenge.validation.ChallengeDefinitionValidator`, a deterministic, side-effect-free
  static validator producing a `ChallengeDefinitionValidationResult` of `ChallengeDefinitionIssue`
  (stable code, field path, message), covering the scalar/structural rules listed under
  "Validation boundary" above (presence/positivity of identity, floor, budget, workload, deadline,
  evaluation-policy fields, plus duplicate/blank available-equipment-id rejection). It does not
  call, extend, or share a result type with `FactoryModelValidator`.

Not implemented, and not claimed by this validator (see "Validation boundary" above for why each
belongs to a later gate rather than C1): catalogue-item resolution against real equipment offers
and their existence/pricing (C2), construction-cost calculation, candidate placement/overlap/
admissibility, affordability (C2), evaluation-policy implementation lookup and score/rating
threshold coherence (C3), and any persistence/content-loading format that would translate absent
or malformed external input into diagnostics (C5).

This module and its no-runtime-dependency boundary are also recorded in `docs/architecture/overview.md`.

## 6. C2 — Catalogue, construction budget, draft economics, and candidate admissibility

### Goal

Make capital efficiency and challenge-specific design constraints deterministic game rules without extending Arcogine production or finance semantics.

### 6.1 Game-owned catalogue seam

The challenge catalogue must not assume that today's canonical `ResourceDefinition` already represents a reusable equipment type. The current factory model still defers a full definition/installed-instance split in [Factory Design Capability](factory-design-capability.md).

Challenge Readiness therefore uses a game-owned catalogue identity and keeps canonical projection in the consumer authoring layer:

```text
EquipmentOffer
    catalogue item ID
    availability
    purchase cost
    resale policy
    projection key / authoring reference

DraftEconomics
    starting budget
    committed construction cost
    remaining budget
```

The challenge evaluator and budget logic reason about catalogue item occurrences, not about pretending a canonical `ResourceDefinition` is a reusable equipment type.

The game draft-authoring/projection layer maps one catalogue item occurrence into whatever supported canonical resource semantics the current model requires. That projection is an authoring convenience only; once published, Arcogine's canonical model is authoritative for production semantics.

If the canonical definition/instance split later lands, this projection seam may simplify. C2 does not require that split as a prerequisite and must not duplicate canonical dispatch, capability, or execution semantics in the game.

### 6.2 Candidate admissibility

Before publication/run, the challenge layer needs a headless admissibility decision over one exact candidate draft or attempt input.

Conceptually:

```text
Challenge version
        +
Candidate draft snapshot
        +
Draft economics
        +
Challenge-bound workload/input
        |
        v
Candidate admissibility
        |
        +--> ADMITTED
        |
        +--> REJECTED with structured game-rule reasons
```

Admissibility may check game-owned constraints such as:

- only allowed catalogue items are used;
- per-item quantity/availability limits are respected;
- construction cost is within budget;
- game-owned floor/placement constraints are satisfied where they are intentionally outside canonical executability semantics;
- the attempt uses the challenge's fixed workload/contract rather than a player-substituted input;
- any challenge-specific prerequisite is satisfied.

Admissibility does **not** prove canonical executability. An admitted candidate is still projected and passed to Arcogine validation/publication. Conversely, an executable canonical factory may still be inadmissible for a particular challenge.

### Acceptance criteria

C2 is ready when:

1. Placing/removing game catalogue items updates construction cost deterministically.
2. Unaffordable drafts can be rejected by game rules without mutating Arcogine runtime state.
3. Purchase/resale rules are versioned challenge semantics when they affect reproducibility.
4. Arcogine does not acquire player currency, purchase price, score, or catalogue-availability fields.
5. Equivalent canonical designs can be evaluated under different challenge economies without changing production semantics.
6. Catalogue logic does not depend on treating the current canonical `ResourceDefinition` as a reusable equipment type.
7. Candidate admissibility is callable headlessly and returns deterministic structured reasons.
8. An executable canonical factory may still be rejected for violating challenge rules, and an admitted candidate must still pass Arcogine executability validation before publication/run.
9. The fixed challenge workload/input cannot be silently replaced by the candidate attempt.

## 7. C3 — Deterministic challenge evaluation

### Goal

Evaluate exact outcome facts under an exact challenge/evaluation policy and produce an explainable immutable result.

A useful conceptual shape is:

```text
Challenge evaluation input
    challenge identity/version
    evaluation-policy identity/version
    model provenance
    workload/run provenance where supported
    authoritative outcome facts
    game-owned construction cost

Challenge evaluation result
    success/failure
    reasons
    deadline margin
    unused budget
    score/rating
```

The evaluator should be a pure or equivalently deterministic operation over explicit inputs.

### Evaluation-policy identity contract

For the first implementation, the **evaluation-policy identity/version is the complete semantic identity of challenge evaluation behavior**.

Any change that can alter an evaluation result for the same explicit inputs must create a new evaluation-policy version. Pure implementation refactors may retain the version only when they preserve observable evaluation semantics.

Historical attempts therefore retain the evaluation-policy identity/version they used. A runtime/application build identifier may additionally be recorded for diagnostics, but it is not a substitute for semantic policy versioning.

This rule avoids requiring a second independent evaluator-version concept while still making old results reproducible and attributable.

### Acceptance criteria

C3 is ready when:

1. Identical explicit evaluation inputs under the same evaluation-policy identity/version produce identical results.
2. Success/failure can be explained through structured reasons rather than only a scalar score.
3. The evaluator can operate against synthetic supported-outcome fixtures without an active Arcogine runtime.
4. The evaluator consumes authoritative outcome facts rather than reconstructing production semantics.
5. Every result-affecting evaluator change creates a new evaluation-policy version.
6. An old attempt retains the exact challenge and evaluation-policy identities/versions that produced its result.
7. Reference fixtures can reproduce historical evaluation results under their recorded policy versions.

## 8. C4 — Attempt provenance and design-to-design comparison

### Goal

Make design iteration historically attributable and comparable.

An attempt should eventually retain enough identity to answer:

```text
Which challenge/rules version?
Which admitted game-owned draft snapshot?
Which published factory design?
Which workload/run?
Which evaluation-policy identity/version?
Which authoritative result facts?
Which game evaluation resulted?
```

A comparison may then explain changes such as:

```text
completion time       391 -> 348
construction cost     28k -> 34k
transfer contribution  92 -> 41
score                  740 -> 812
```

Only supported facts may be compared as authoritative production results. Game presentation may derive deltas from those facts and may compare its own immutable draft snapshots. This does not require Arcogine to provide a canonical semantic diff between published models.

### Acceptance criteria

C4 is ready when:

1. An attempt references the exact challenge version and admitted game-owned draft snapshot used.
2. An attempt references the published model identity when publication succeeds.
3. Evaluation provenance identifies the exact evaluation-policy identity/version.
4. Historical attempts are immutable or historically attributable.
5. Two synthetic attempts can be compared without Arcogine runtime internals.
6. Later integration can add supported run/session provenance without changing the meaning of old game-owned records.
7. Comparison distinguishes authoritative production facts from game-owned score/economics/draft facts.
8. No comparison requirement implies Arcogine D5 semantic comparison unless a future product requirement explicitly asks Arcogine to explain semantic differences between published model versions.

## 9. C5 — Data-driven challenges and reference fixtures

### Goal

Make challenge content independently authorable and use representative fixtures to stabilize challenge semantics before game UI implementation.

Initial content should stay small. Useful challenge archetypes include:

- identify and relieve a processing bottleneck;
- trade additional capacity against construction cost;
- trade capacity against shorter transfers once spatial consequences exist;
- meet the same fixed contract under tighter budget or deadline constraints.

### Acceptance criteria

C5 is ready when:

1. Multiple challenge definitions can be loaded through one supported content path.
2. Content validation is independent of rendering technology.
3. Reference fixtures cover challenge-definition failures, candidate-admissibility failures, pass/fail boundaries, and score/rating thresholds.
4. Synthetic outcome fixtures prove evaluation determinism.
5. Content does not require engine behavior absent from the applicable readiness gates.
6. At least one fixture proves a candidate can be canonically executable yet challenge-inadmissible.

## 10. Relationship to Engine Readiness

The delivery tracks should converge only through supported contracts:

```text
Game-owned draft
    |
    +--> challenge admissibility
    |        |
    |        +--> rejected by game rules
    |
    v
project canonical semantics
    |
    v
Arcogine validate/publish
    |
    v
published FactoryModelVersion
    |
    v
Arcogine runtime
[Engine Readiness]
    |
    v
supported outcome facts
    |
    +-------------------------+
                              |
Challenge definition ---------+
[Challenge Readiness]         |
                              v
                     Challenge evaluation
                              |
                              v
                       Game presentation
```

Challenge Readiness may proceed against synthetic fixtures. Engine Readiness does not depend on challenge/scoring semantics. Challenge code must not be used as evidence that runtime gates are correct.

## 11. Relationship to governance/conformance

Challenge Readiness and [Governance and Conformance Capability](governance-conformance-capability.md) are sibling proving grounds for several architectural qualities:

| Concern | Challenge domain | Governance domain |
|---|---|---|
| Evaluation subject | Admitted attempt/design/run outcome | Model/revision/change/observation scope |
| Rules | Challenge/evaluation-policy version | Requirement/assertion versions |
| Result | Success/failure/score/rating | Conformance result |
| Explanation | Challenge reasons | Findings |
| Provenance | Challenge/policy/model/run | Requirement/assertion/evidence/model/revision |
| History | Attempt comparison | Revision/ChangeSet/audit history |

This similarity is intentional evidence to observe, **not permission to unify the domains prematurely**.

In particular, this plan does not introduce concepts such as:

```text
ChallengeRule extends Requirement
ChallengeReason extends governance Finding
AttemptEvidence extends governance Evidence
GenericEvaluationEngine<S, R, E>
```

Reuse architectural principles immediately; extract shared concrete abstractions only after both domains independently demonstrate stable common semantics.

### 11.1 Cross-track architecture reviews

The tracks should compare lessons at three non-blocking checkpoints.

**Identity review — after Challenge C1 and Governance G1**

Compare semantic/content identity, historical identity, ruleset versioning, persistence needs, and the role of human labels versus durable identifiers.

**Evaluation review — after Challenge C3 and Governance G4**

Compare the stable semantics of subject, exact rule/policy version, evaluator semantic identity, result, explanation/findings, and deterministic reproduction. Ask whether a genuinely domain-neutral evaluation envelope exists; do not assume that it does.

**Provenance review — after Challenge C4 and Governance G5**

Compare what exact subject, rules, evaluator policy, evidence/input, and result must be retained to reconstruct a historical decision. Evaluation provenance is the most plausible eventual convergence point, but remains an evidence-driven architecture decision.

These reviews are architecture-learning checkpoints, not delivery dependencies. Either track may proceed if the other has not reached the corresponding checkpoint.

## 12. Explicit non-goals

This initiative does not introduce:

- a generic Arcogine rules engine;
- a generic evaluation framework;
- governance requirements as game rules;
- game scoring concepts in governance;
- production simulation in the challenge layer;
- a replacement for factory-model validation;
- a fake reusable canonical resource type in the game merely to compensate for today's deferred definition/instance split;
- UI/editor implementation;
- campaign/progression architecture beyond the minimum challenge semantics;
- a shared persistence system merely because attempts and governance records both require history.

If stable common abstractions later emerge, they should be justified by concrete implementations in both domains and recorded through the normal architecture/ADR process.

## 13. First end-to-end milestone

Using only game-owned draft/economics facts plus synthetic authoritative outcome facts:

1. load one versioned challenge with a catalogue, budget, fixed contract, deadline, and evaluation policy;
2. admit one candidate and reject another with structured challenge-rule reasons;
3. project the admitted candidate through the supported game-to-canonical seam without relying on reusable-type semantics absent from today's model;
4. evaluate at least two attributable attempts under an exact evaluation-policy version;
5. reproduce the same evaluations deterministically; and
6. compare why the second attempt performed differently without depending on Arcogine runtime internals or D5 semantic comparison.

That milestone proves the challenge/admissibility/evaluation boundary while Engine Readiness continues independently.

## 14. Documentation lifecycle

While exploratory, this file remains under `docs/planning/`.

As work becomes established:

- keep production truth and external-consumer runtime contracts in [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md);
- keep game/Arcogine ownership constraints in [Factory-Design Game Consumer Initiative](factory-design-game-consumer.md);
- keep the concrete product hypothesis in [Factory-Design Game Vertical Slice](factory-design-game-vertical-slice.md);
- keep canonical model evolution, including any eventual definition/instance split, in [Factory Design Capability](factory-design-capability.md);
- keep generic governed evaluation, evidence, and change semantics in [Governance and Conformance Capability](governance-conformance-capability.md);
- record genuinely shared, hard-to-reverse abstractions as architecture/ADRs only after evidence from both sibling tracks exists.

Once the challenge layer is implemented and its durable boundaries have moved into current architecture/reference documentation, reduce or retire this planning file.
