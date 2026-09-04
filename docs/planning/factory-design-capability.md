# Factory Design Capability Plan

> **Status:** Proposed  
> **Scope:** Establish a cross-consumer factory-design capability over Arcogine's canonical production-system model  
> **Authority:** Planning only; this document defines delivery slices and readiness criteria, not current capability or accepted architecture  
> **Related:** [Factory Design Architecture](../architecture/factory-design.md), [ADR-0003](../architecture/decisions/0003-canonical-factory-model-boundary.md), [ADR-0004](../architecture/decisions/0004-model-identity-revision-lineage-and-external-change-control.md), [ADR-0006](../architecture/decisions/0006-durable-semantic-fingerprint-contract.md), [ADR-0014](../architecture/decisions/0014-factory-model-semantic-policy-evolution.md), [ADR-0015](../architecture/decisions/0015-engine-semantics-identity-and-reproducibility.md), [Engine Semantics v1](../architecture/engine-semantics-v1.md), [Factory Model v2 Canonicalization](../architecture/factory-model-v2.md), [Gate 5 Spatial Runtime Consequences](gate-5-spatial-runtime-consequences.md), [Governance and Conformance Capability Plan](governance-conformance-capability.md), [Factory Simulation Engine Readiness](factory-simulation-engine-readiness.md), [Operational Execution and Digital Twin Readiness](operational-execution-digital-twin-readiness.md), [Factory-Design Game Consumer Initiative](factory-design-game-consumer.md), [ISA-95 Semantic Mapping](../architecture/isa-95-semantic-mapping.md)

## 1. Purpose

Arcogine should treat factory design as a cross-consumer capability rather than a game-specific editor concern.

The semantic output of design belongs to Arcogine because the same production-system model must remain continuous across design, simulation, verification, and future execution. The authoring experience remains consumer-specific unless multiple concrete consumers justify a shared design workspace.

The delivery shape is:

```text
Consumer-specific draft authoring
              |
              v
     Canonical model validation
              |
              v
       Publish model version
              |
       +------+------+
       |             |
       v             v
   Simulate        Verify
```

This plan establishes the minimal shared design substrate before a game, industrial UI, optimizer, CLI, or integration adapter treats Arcogine as the authority for a published factory design.

## 2. Relationship to engine and operational readiness

This capability is orthogonal to runtime execution, but downstream simulation and operational contexts share one canonical production-system model.

This plan owns model-side semantics and lifecycle:

- canonical factory model definition;
- structured validation;
- immutable publication/version boundary;
- model identity and provenance;
- deterministic runtime instantiation from a published model;
- eventual semantic compare/diff and broader design lifecycle when justified.

The [engine-readiness plan](factory-simulation-engine-readiness.md) starts after this boundary exists. It owns deterministic simulation execution semantics such as production orders, work items, resource dispatch, bounded advancement, simulation observations/events, and transfer progression.

Gate 5 now fixes the hard model/runtime split for spatial consequences:

```text
Factory model v2 (ADR-0014)
    floor dimensions
    resource position + anchored footprint
    ticksPerCell
    handlingTicks
        |
        v
Engine interpretation (ADR-0015 / engine-semantics:v1)
    destination binding
    Manhattan reference-cell distance
    transfer timing
    admission reservation
    availability/no-rerouting rules
        |
        v
Runtime state
    transfer start / in-flight / completion
    observations/events
```

`ModelFingerprint` continues to identify the authored Factory design. `EngineSemanticsVersion` separately identifies Arcogine's result-affecting interpretation. A result-affecting Engine policy change therefore does not masquerade as a Factory design change.

The sibling [Operational Execution and Digital Twin Readiness](operational-execution-digital-twin-readiness.md) track consumes published/governed semantics when real external systems are involved. It owns production execution context, verified operational trust/authority, deployment target/application, command/result lifecycle, independent external observations, reconciliation, and operational recovery.

```text
Factory Design Capability
        |
        v
Published FactoryModelVersion
        |
        +--------------------+
        |                    |
        v                    v
Factory Simulation       Governance
Engine Readiness              |
                              v
                    Operational Execution / Twin
```

The runtime must not mutate the published model in place. The design capability must not reproduce queues, assignments, transfers in progress, production deployment records, external telemetry, or twin reconciliation state.

## 3. Delivery policy

The first implementation target is deliberately narrow and behavior-preserving.

```text
D1  Canonical factory model contract
    ↓
D2  Structured validation
    ↓
D3  Publication, identity, and provenance
    ↓
D4  Deterministic runtime instantiation
    ↓
D5  Semantic comparison (after concrete need)
    ↓
D6  Shared draft lifecycle (deferred until justified)
    ↓
D7  Factory adoption of governed change (deferred until justified; owned cross-domain by Governance and Conformance G1/G2/G6, with Operational Execution owning deployment application)
```

D1-D4 form the immediate implementation sequence. D5 has an implemented initial slice covering factory semantic comparison for resources, operations, and products through the Governance `SemanticChangeExtractor` seam (see §9); finer-grained D5 comparison remains future work triggered by concrete consumer need. D6 and D7 remain deferred. D5 is not a prerequisite for engine runtime work or a first game consumer unless a specific consumer requirement depends on its semantic-diff capability.

The initial spike established the model seam without simultaneously redesigning order execution, dispatch policy, spatial behavior, operational deployment, or the public HTTP contract. Gate 5 now extends that model seam deliberately through a new immutable policy rather than changing `factory-model:v1` in place.

### 3.1 Implementation status

The canonical `FactoryModel`/`FactoryModelVersion` seam has landed, so D1-D4 are no longer wholly proposed, but they are not uniformly complete. Gate 5 adds an **accepted but not yet implemented** Factory V2 contract:

```text
D1 Canonical model                  PARTIAL
    canonical semantic seam         implemented
    product/operation/resources     implemented
    definition-instance split       deferred
    factory-model:v1 semantics      implemented/released
    factory-model:v2 semantics      accepted by ADR-0014; implementation pending G5-A1/A2

D2 Executability validation         PARTIAL
    deterministic structural errors implemented
    stable finding taxonomy         deferred
    warnings/severity/codes         deferred
    V2 spatial validation           accepted by ADR-0014; implementation pending G5-A1

D3 Publication / semantic identity  PARTIAL
    immutable publication           implemented
    content hash                    implemented, legacy compatibility
    durable fingerprint v1          implemented — see ADR-0006
    durable fingerprint v2          accepted by ADR-0014, canonicalization fixed by
                                    factory-model-v2.md; implementation pending G5-A2
    multi-policy historical resolve accepted; implementation pending G5-A3
    controlled revision identity    implemented cross-domain by Governance G1

D4 Runtime instantiation            PARTIAL
    runtime from published model    implemented for current V1
    handler provenance              implemented (IntegratedHandler)
    result model provenance         implemented (SimResult.modelContentHash compatibility surface)
    consumer-neutral runtime ID     implemented for FactoryRuntime / Gate 4 G4-A
    EngineSemanticsVersion          accepted by ADR-0015; implementation pending G5-B1/B2
```

D1's original acceptance criteria also call for resource definitions and installed instances to be distinguishable; today's `ResourceDefinition` still deliberately represents both a resource type and its installed instance, so that richer split remains deferred. D2's original stable finding taxonomy is likewise broader than today's minimal `ModelValidationError`. Neither deferred enrichment is a prerequisite for the accepted Gate 5 V2 contract.

Important current identity rules:

- `factory-model:v1` remains permanently immutable under ADR-0006 and ADR-0014.
- `factory-model:v2` is exactly V1 semantics plus the required Gate 5 spatial/handling facts; it is not a sidecar fingerprint.
- V2 implementation must preserve V1 golden vectors/fingerprints unchanged.
- there is no automatic V1→V2 lift and no synthesized spatial defaults for historical V1 artifacts.
- Governance G1 provides controlled revision authority/history independently of Factory fingerprint policy; lineage may cross V1/V2 without rewriting either artifact.

## 4. Current-model migration strategy

The existing scenario factory semantics adapt into the canonical model and instantiate the runtime through that model boundary:

```text
Current ScenarioConfig / TOML
            |
            | extract/adapt factory semantics
            v
       FactoryModel
            |
         validate
            v
   FactoryModelVersion
            |
      resolve/compile
            v
       FactoryRuntime
```

`ScenarioConfig` remains a scenario/run input envelope rather than becoming the canonical factory model. Simulation parameters, economy configuration, agent configuration, and workload concerns remain outside the factory design.

For current features that are narrower than the intended future model, the adapter preserves existing semantics explicitly. A process step that targets concrete eligible resources remains explicit eligibility; the seam must not manufacture new runtime behavior merely to appear more general.

V1 remains a truthful supported design policy after Gate 5: because it authors no spatial facts, it receives no Gate 5 transfer behavior. Spatial behavior requires explicit V2 publication and therefore a new `ModelFingerprint`.

### 4.1 Migration-spike acceptance criteria

The original behavior-preserving model-seam spike established that:

1. an existing scenario can produce a canonical `FactoryModel`;
2. simulation, economy, and agent configuration stay outside it;
3. model validation is independent of mutable runtime construction;
4. a valid model can publish with semantic identity/provenance;
5. runtime instantiates from the published model;
6. representative deterministic behavior remains covered;
7. runtime observations/results can identify source model identity;
8. no game UI or operational deployment semantics are needed to prove the seam.

Gate 5 builds on, rather than reopens, that seam through the focused G5-A1/A2/A3 implementation sequence.

## 5. D1 — Canonical factory model contract

### 5.1 Goal

Define one semantic representation of a designed production system that can be authored by multiple consumers and instantiated by multiple lifecycle contexts.

The model covers behaviorally relevant facts needed by current and near-term factory work:

```text
FactoryModel
    product definitions
    operation definitions
    resources / installed identities
    capability or explicit eligibility requirements
    relevant canonical constraints
    semantic spatial layout where behavior depends on it
```

The exact Java and wire types remain implementation details. The model must not be shaped around one editor state tree, TOML layout, transport format, or mutable runtime object graph.

### 5.2 Required separations

```text
MODEL SIDE
    definitions
    installed structure
    validated published model

RUNTIME SIDE
    production orders
    work items
    queues
    assignments
    transfers in progress
    performance
```

Runtime workload/state does not belong in a published factory design.

### 5.3 Gate 5 V2 addition

ADR-0014 establishes the next released Factory fingerprint policy. `factory-model:v2` is exactly V1 plus five required authored additions:

| Addition | Canonical meaning |
|---|---|
| floor width / height | plant extent in integer cells |
| resource position `x` / `y` | minimum-coordinate reference cell of the footprint |
| footprint width / height | exact occupied `w × h` cells extending from the reference cell |
| `ticksPerCell` | authored material-handling rate magnitude |
| `handlingTicks` | authored fixed overhead applied once per inter-resource transfer |

All are mandatory and fingerprinted in V2. The exact bytes those additions are digested through — the `arcogine.factory-model.v2\0` policy-domain prefix, the plant-scope header, the per-resource spatial suffix, collection ordering and digest rendering — are fixed normatively by [Factory Model v2 Canonicalization](../architecture/factory-model-v2.md), because ADR-0006 makes a fingerprint-policy version a canonicalization contract rather than a field-membership label.

Position/footprint containment and non-overlap are publication semantics. V2 publication also proves with overflow-safe arithmetic that

```text
maxManhattanDistance = (floorWidth - 1) + (floorHeight - 1)
maxTransferDuration = handlingTicks + ticksPerCell * maxManhattanDistance
```

is representable in the runtime duration type. That bounds the derived transfer duration, not arbitrary future addition to an extreme current `SimTime`.

Orientation, path/aisle/conveyor topology, transport resources, connection points, congestion, floor identity and authoritative animation coordinates are not V2.

### 5.4 Acceptance criteria

D1 is satisfied incrementally when:

1. a complete supported factory can be represented without frontend DTOs or mutable runtime classes;
2. product/operation/resource concepts have stable semantic identities;
3. operation requirements can express explicit eligible-resource semantics without mutable runtime objects;
4. semantic layout facts are present only where they are authored behaviorally relevant content;
5. consumer-only state such as selection, camera, undo, score, and artwork is absent;
6. canonical content is normalized deterministically for identity/provenance;
7. for V2, every `(position, footprint)` pair has one unambiguous occupied-cell set and invalid containment/overlap/arithmetic is rejected.

The richer resource-definition/installed-instance split remains separately deferred; it is not silently claimed complete merely because V2 adds placement to current resource identities.

## 6. D2 — Structured validation

### 6.1 Goal

Make Arcogine authoritative for whether a factory design can be published and instantiated.

Consumers may perform optimistic local checks, but publication/runtime instantiation relies on Arcogine validation.

Validation should eventually distinguish at least errors from warnings through a stable structured finding shape. Today's implemented validation remains narrower, and Gate 5 adds only the V2 executability predicates required by ADR-0014.

### 6.2 Initial executability checks

Examples include:

- duplicate/missing identifiers;
- dangling product/operation/resource references;
- invalid operation order/graph;
- operation with no resolvable eligible resource under represented semantics;
- unsupported policy/configuration values;
- V2 floor/position/footprint violations;
- V2 unsafe maximum-transfer-duration arithmetic.

Game budget, unlocks, scores, and tutorials are not Arcogine executability checks.

### 6.3 Acceptance criteria

D2 is satisfied incrementally when invalid models return deterministic findings, validation does not mutate runtime state, publication/instantiation is atomic, and headless/consumer paths rely on the same authoritative validation. Stable codes/severity remain a separate planned enrichment where not yet implemented.

## 7. D3 — Publication, identity, and provenance

### 7.1 Goal

Create an explicit boundary between mutable authoring state and an immutable semantic snapshot that downstream contexts can instantiate.

```text
FactoryModel
    ↓ validate/publish
immutable semantic snapshot
    ↓
ModelFingerprint
```

### 7.2 Minimum identity

Per ADR-0004, publication requires deterministic content-derived semantic identity and enough provenance to attribute downstream execution to the model. Governance G1 separately provides controlled historical revision identity and authoritative history.

A model UUID, revision counter, Jira key, approval state or Engine semantics version is not part of the Factory model fingerprint.

### 7.3 Immutable policy evolution

ADR-0006 fixes the released V1 contract; ADR-0014 generalizes the evolution rule:

- a released Factory fingerprint policy is immutable and never reinterpreted;
- a new authored behaviorally relevant fact that cannot fit without changing a released policy creates `factory-model:vN+1`;
- old policies remain permanently historically resolvable/verifiable;
- controlled-revision lineage may cross policies;
- a normal semantic `ChangeSet` must not silently span policies by inventing facts the older model never authored;
- the first real V1→V2 controlled transition gets only the narrow migration classification/common-representation seam it actually needs, not a generic migration framework.

G5-A2 implements the V2 policy; G5-A3 implements this first coexistence/evolution seam.

### 7.4 Acceptance criteria

D3 requires immutable publication, stable deterministic fingerprints within each released policy, draft/editor metadata excluded from identity, downstream attribution to the exact model fingerprint, and permanent policy-aware historical resolution.

## 8. D4 — Deterministic runtime instantiation

### 8.1 Goal

Make the published model the only semantic bridge from design into runtime contexts.

```text
FactoryModelVersion
       |
 validate/resolve
       v
ExecutableFactoryModel
       |
 instantiate
       v
FactoryRuntime
```

### 8.2 Derived structures

Runtime preparation may derive resolved references, eligibility indexes, immutable routing indexes, spatial indexes when supported, and scheduling metadata. These are derived from one published semantic model and are not independently authored truth.

### 8.3 Runtime provenance

Every run/session identifies the source `ModelFingerprint`. ADR-0015 adds a second, Engine-owned provenance dimension:

```text
RunId                     correlation/runtime epoch
ModelFingerprint          which authored Factory design
EngineSemanticsVersion    which result-affecting Engine interpretation
```

The current runtime has not implemented the last field yet; focused Gate 5 slice G5-B1/B2 adds it. `ControlledRevisionId` remains additional optional provenance only when an authoritative revision binding exists; runtime code must not synthesize one from the fingerprint.

Deterministic comparison across builds is therefore scoped to the same model fingerprint, Engine semantics version, workload, seed/random inputs and ordered commands rather than treating model identity alone as outcome identity.

### 8.4 Acceptance criteria

D4 is satisfied incrementally when a published model version instantiates a fresh deterministic runtime, runtime cannot mutate the model, supported runtime facts retain source-model provenance, and no consumer draft representation is required. Gate 5 extends the provenance surface additively without changing those ownership rules.

## 9. D5 — Semantic comparison and design alternatives

**Status: initial slice implemented**, as the direct D5 implementation feeding Governance G2:
`com.arcogine.factory.change.FactoryModelSemanticComparator` compares resources, operations, and
products by stable domain identity and classifies add/remove/modify through Governance's semantic-change seam.

Finer route/policy/constraint/spatial change taxonomy remains consumer-pulled work. Moving a V2 resource is a real Factory design change because position is canonical content, but cross-policy V1→V2 comparison must obey ADR-0014 rather than pretending a V1 design authored missing spatial fields.

A change-management workflow is one concrete future consumer: reviewers need a domain-level semantic diff between a candidate revision and its predecessor, not a generic text/JSON diff.

Do not implement arbitrary text/JSON diff, generic patch/merge, or collaborative editing merely to satisfy D5.

## 10. D6 — Shared draft lifecycle and collaboration

Promote drafts into an Arcogine-owned shared lifecycle only when a second concrete workflow requires common persistence, branching, collaboration, or collaborative draft review/comments.

Possible triggers include industrial design plus optimizer/game authoring, human/agent co-design, branching real production changes, or multi-user design sessions.

D6 is scoped to shared *authoring* mechanics and does not include authorization or organizational change-management workflow. Until a trigger applies, Arcogine does not need generic undo/redo, draft branching, merge, collaboration cursors, edit locks, comments, workspace permissions, or autosave semantics.

## 11. D7 — Factory adoption of governed change

Controlled revision lineage, external change references, technical evidence packages for review, and authorization hand-off are cross-domain concerns owned by Governance, not factory-specific ones. Governance G1 already owns durable revision identity/history; G2 owns semantic `ChangeSet`/impact; G4/G5 own conformance/evidence semantics; G6 will own external workflow/change-control integration.

Operational Execution owns what happens after authorization to a real execution target: execution-context identity, deployment application/provenance, command/result lifecycle, external observations and reconciliation.

D7 remains only the factory-specific contribution of Factory semantics/evidence into those cross-domain capabilities. Building a factory-only revision repository, authorization workflow, production deployment runtime, telemetry ingestion or reconciliation layer would duplicate sibling ownership.

## 12. Constraint classification

Every design rule must be classified before implementation.

| Class | Meaning | Owner |
|---|---|---|
| Executability constraint | Required for the model to publish/instantiate coherently | Arcogine model/design boundary |
| Verification objective/constraint | Tests whether an executable design meets a target | Shared verification capability when supported |
| Consumer rule | Applies only to one experience/workflow | Consumer |

Examples:

```text
Resource outside floor               -> executability
Operation has no eligible resource   -> executability
Throughput must exceed target        -> verification
Player construction budget           -> game consumer
Machine unlock level                 -> game consumer
```

## 13. Interaction with engine and operational readiness

The design capability owns:

```text
Product/operation definitions
Resource identities and authored structure
Capability/eligibility requirements
Factory V2 floor/position/footprint/ticksPerCell/handlingTicks
Validation
Publication/model provenance
```

Engine readiness owns:

```text
Production orders and work items
Quantity execution semantics
Dispatch and queues
EngineSemanticsVersion and result-affecting interpretation
Active operations/transfers
Simulation runtime events and observations
Performance
```

Operational Execution owns:

```text
Execution-context identity / verified trust / authority
Deployment target application and applied-artifact provenance
External command/result lifecycle
External operational observations
Twin reconciliation and drift/calibration feedback
```

The Factory model owns authored input semantics; the Engine owns deterministic interpretation and mutable consequences. The same V2 `ModelFingerprint` may legitimately produce a different result under a future different `EngineSemanticsVersion` without redefining the authored plant.

## 14. Factory-design game integration

The game may own an editor-specific `FactoryDraft`, undo history, camera, palettes, previews, and game rules.

```text
Game-owned draft
      |
 project canonical semantics
      v
Arcogine validate/publish
      |
      v
FactoryModelVersion
      |
      v
Arcogine runtime
```

The game must not implement a parallel scheduler, transfer rule, distance metric, or semantic-policy layer and must not treat its draft as the authoritative executable model.

## 15. Headless acceptance path

### 15.1 Behavior-preserving publication test

1. Load an existing representative scenario.
2. Adapt only its factory semantics into a canonical model.
3. Validate the model.
4. Publish a model version.
5. Instantiate runtime through the model boundary.
6. Run the same inputs/seed as the existing path.
7. Assert deterministic behavior/results are unchanged.
8. Assert results identify the source model's semantic fingerprint.

### 15.2 Variant tests after richer semantics exist

For a **Factory design variant**:

1. publish model A;
2. publish model B with one authored semantic design change;
3. instantiate independent runtimes under the same `EngineSemanticsVersion`;
4. apply the same seed/workload/commands;
5. verify deterministic but appropriately different outcomes.

Gate 5 examples include moving one V2 resource or changing an authored handling magnitude. A comparison that keeps the same `ModelFingerprint` and changes only `EngineSemanticsVersion` is an Engine-semantics experiment, not a D5 Factory design variant.

## 16. First implementation milestone

> **Take an existing Arcogine scenario, derive a validated immutable canonical factory model from it, instantiate the existing simulation from that model, and prove the simulation result has not changed.**

That original milestone established the current model seam. Gate 5 does not retroactively reopen it; it uses ADR-0014's explicit V2 policy and the focused G5-A1/A2/A3 slices to add spatial authored semantics without weakening V1.

## 17. ADR triggers

[ADR-0003](../architecture/decisions/0003-canonical-factory-model-boundary.md) establishes the model/run/runtime boundary. [ADR-0004](../architecture/decisions/0004-model-identity-revision-lineage-and-external-change-control.md) separates semantic fingerprint from controlled-revision/change-management identity. ADR-0006 fixes the durable V1 fingerprint contract.

Gate 5's hard-to-reverse decisions are now accepted rather than open triggers:

- [ADR-0014](../architecture/decisions/0014-factory-model-semantic-policy-evolution.md) fixes Factory fingerprint-policy evolution and `factory-model:v2`;
- [ADR-0015](../architecture/decisions/0015-engine-semantics-identity-and-reproducibility.md) fixes design-vs-Engine interpretation ownership and `EngineSemanticsVersion`;
- [Engine Semantics v1](../architecture/engine-semantics-v1.md) fixes the first concrete result-affecting interpretation;
- [Factory Model v2 Canonicalization](../architecture/factory-model-v2.md) fixes the durable `factory-model:v2` byte grammar that ADR-0014's policy version contractually implies.

Future ADRs remain warranted for genuinely new hard-to-reverse model aggregate boundaries, work-center/resource-pool semantics, shared draft lifecycle, or a new released Factory/Engine semantics contract. Do not create ADRs for consumer-local editor gestures or temporary UI structure.

## 18. Documentation lifecycle

While this work is proposed, this file remains under `docs/planning/`.

As capabilities are implemented:

- reconcile established behavior into [`../architecture/overview.md`](../architecture/overview.md);
- update [`../product/concepts.md`](../product/concepts.md) only for capabilities that actually ship;
- update [`../reference/api.md`](../reference/api.md) only when a public contract exists;
- update the ISA-95 mapping when implemented manufacturing concepts change;
- keep migration/regression/golden fixtures executable and version-controlled;
- keep governed-change integration aligned with Governance and real deployment/reconciliation aligned with Operational Execution.

Once the initiative is complete or abandoned, reduce this file to a concise historical outcome or retire it after durable decisions and current behavior are represented in authoritative locations.
