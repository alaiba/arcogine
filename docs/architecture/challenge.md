# Challenge

> **Status:** Implemented headless consumer capability in the `:challenge` module
>
> **Scope:** Game-owned challenge identity and validation, catalogue and draft economics, candidate admissibility, evaluation, attempt history and comparison, and content loading — and the boundary between them and Arcogine's production semantics
>
> **Owning architecture:** [Architecture overview](overview.md)

## Responsibility and boundary

Challenge owns deterministic game rules over game-owned candidate facts and authoritative Arcogine outcome facts. It does not simulate production.

```text
challenge definition / catalogue / budget
        +
candidate draft snapshot
        |
        v
candidate admissibility                      (Challenge)
        |
        v
projection / publication / runtime           (outside Challenge)
        |
        v
authoritative outcome facts
        |
        v
challenge evaluation  ->  attempt  ->  comparison   (Challenge)
```

The `:challenge` module depends on no production simulation or domain module ([dependency graph](overview.md#dependency-graph)). It never reads a Factory model, runtime, scheduler or observation; production facts reach it only as explicitly supplied values. Three validations stay independent, and passing one never implies another:

- **Challenge admissibility** — whether a candidate obeys the challenge's game rules;
- **Factory publication validity** — whether a model is a valid canonical [Factory model](factory-model.md);
- **Engine executability** — whether the current Engine admits a published model ([transfer applicability](transfer-applicability.md)).

Challenge rules may reject a design that Factory would publish, for example on budget or catalogue grounds. The test-only `challenge-factory-integration-test` module demonstrates the first two axes are independent; no production projector from a challenge candidate to a Factory model exists.

## Identities

Each identity is a game-owned value, never derived from a Factory model version, `ModelFingerprint`, `ControlledRevisionId`, `RunId` or object identity, and none of those is derived from it.

| Identity | Identifies |
|---|---|
| `ChallengeIdentity` (id, version) | one challenge and the revision of its content and rules |
| `EvaluationPolicyIdentity` (id, version) | one evaluation policy and the semantic revision of its behavior, versioned independently of any challenge |
| `EquipmentCatalogueIdentity` plus an optional catalogue semantic fingerprint | the catalogue a challenge is bound to, and the exact catalogue content it was authored against |
| `ChallengeAttemptId` | one historical attempt occurrence |

An evaluation-policy version is the identity of evaluation behavior: a result-affecting change to a policy requires a new policy version.

## Definition and validation

`ChallengeDefinition` is an immutable value holding the challenge identity, floor dimensions, starting budget, a fixed workload (a game-owned product reference and required quantity), the available catalogue items, a deadline, the evaluation-policy identity and the catalogue binding. It carries no result or score. The workload's product reference names the contract in game terms; it is not an executable routing, which only the Factory model defines.

Construction establishes a value, not validity. `ChallengeDefinitionValidator` produces deterministic diagnostics with a stable code, path and message. It does not replace Factory model validation.

## Catalogue and draft economics

The equipment catalogue is a game concept. An `EquipmentOffer` carries a game-owned item identity, a non-negative purchase cost and an optional positive quantity limit, and nothing else: it does not assert that an Arcogine configured resource is a reusable equipment type ([Factory resource semantics](factory-resource-semantics.md)). `EquipmentCatalogueValidator` rejects internally invalid catalogues, such as duplicate item identities or negative costs.

`DraftEconomicsCalculator` derives committed construction cost and remaining budget from an exact challenge, catalogue and set of draft occurrences. It fails explicitly, never silently, on an invalid catalogue, an unresolved item or arithmetic overflow, and its result — including which failure it reports — never depends on occurrence order.

## Candidate admissibility

`CandidateAdmissibilityPolicy` decides admissibility for one exact challenge, catalogue and immutable draft snapshot. It reports every violated rule, each with a stable code and path:

- duplicate equipment-occurrence identity;
- a catalogue that is unbound from, or does not match, the challenge's catalogue identity and semantic fingerprint;
- an unknown catalogue item, or one the challenge does not make available;
- a catalogue quantity limit exceeded;
- an economics failure, or committed cost above the starting budget;
- a placement outside the floor, or two placements on the same cell.

A candidate snapshot carries only placed equipment, so it cannot replace the challenge's fixed workload. The result is independent of the order in which the snapshot lists its equipment.

## Evaluation

Evaluation is deterministic over explicit inputs only: the challenge definition, opaque references to the published design and run being evaluated, authoritative outcome facts, and the committed construction cost. `AuthoritativeOutcomeFacts` carries only whether the fixed contract completed and at which tick; it holds no queue, dispatch, transfer or runtime detail, and evaluation never infers production facts the producer did not report.

The current policy, `ReferenceChallengeEvaluationPolicy` (`policy.contract-completion` version `1`), succeeds only when the contract completed on or before the deadline within budget. A successful result scores `1 + deadlineMarginTicks + unusedBudgetCredits` in exact integer arithmetic; an unsuccessful one scores zero and lists its issues. Content naming a policy this build does not support is refused at load time.

## Attempts and comparison

A `ChallengeAttempt` immutably retains a candidate snapshot, the economics used and the evaluation result, which itself carries the challenge identity, policy identity and design/run references. It never recomputes them, and it does not itself check admission: the consumer records the values of an admitted candidate. It records one completed attempt, not a workflow.

`ChallengeAttemptComparator` compares two attempts without re-evaluating either. Attempts are comparable only when they share the exact challenge identity and evaluation-policy identity; otherwise the comparison reports structured incompatibility reasons instead of a result. A compatible comparison reports differences in score, deadline margin, unused budget and construction cost, and introduces no new scoring dimension. Attempt comparison is not Factory semantic comparison.

## Content loading

`ChallengeContentLoader` decodes untrusted JSON challenge and catalogue documents into values, or into deterministic, structured content issues; malformed input never reaches a constructor uncaught. Loading performs no I/O of its own and is deterministic. The combined challenge-with-catalogue entry point also validates both documents, binds and verifies the catalogue identity and semantic fingerprint, and resolves catalogue references. The content schema version is a document-format version, distinct from challenge and policy versions. The content layer is game-owned and is not a general configuration or interchange format.

## Invariants for consumers

Any consumer of the Challenge capability, including a future playable game, must preserve:

- Challenge state and production runtime state remain separate;
- catalogue, economics, score and attempt history never enter the canonical Factory model;
- an admitted candidate implies neither Factory publication validity nor Engine executability;
- production outcome facts come from Arcogine's supported contracts, never from Challenge reconstruction;
- evaluation behavior is versioned by evaluation-policy identity, and attempts stay attributable to the exact challenge, policy, inputs and outcome facts;
- Challenge attempt history is distinct from runtime-event history and from Governance controlled-revision history.

When attempts consume real Engine results, comparing them across Engine definitions is a [cross-revision comparison](../research/investigations/simulation-analytics-cross-revision-comparability.md) that remains a research question; the current comparator checks only challenge and policy identity.
