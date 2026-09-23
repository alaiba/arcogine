# Factory Design Architecture

> **Status:** Architectural reference; adopted boundaries marked below, remaining capabilities proposed  
> **Scope:** Cross-consumer factory-design semantics and their boundary with future scenario inputs and runtime behavior
> **Authority:** The canonical model/publication/runtime boundary (§3–§6), the publication identity contract (§11) and the semantic-evolution contract (§11.1) are adopted architecture. Broader draft/workspace capabilities remain proposed; the Overview and product/reference docs describe the current implementation  
> **Related:** [Product Charter](../product/charter.md), [Architecture Overview](overview.md), [Factory Model v1](factory-model-v1.md), [Factory Model v2](factory-model-v2.md), [Controlled revisions](controlled-revisions.md), [Governance and Conformance Architecture](governance-conformance.md), [Operational Execution and Digital Twin Architecture](operational-execution-digital-twin.md), [ISA-95 Semantic Mapping](isa-95-semantic-mapping.md), [Factory Design Capability Plan](../planning/factory-design-capability.md), [Factory Simulation Engine Readiness](../planning/factory-simulation-engine-readiness.md), [Operational Execution and Digital Twin Readiness](../planning/operational-execution-digital-twin-readiness.md)

## 1. Architectural position

Factory design is a cross-consumer Arcogine concern, but "factory design" covers three responsibilities that must remain separate:

1. **Designed production system** — products, operations, resources, policies, constraints, and behaviorally relevant geometry.
2. **Design lifecycle** — drafting, validation, publication, versioning, comparison, provenance, and eventual deployment of changes.
3. **Design experience** — UI, interaction model, visualization, undo gestures, camera, palettes, game tutorials, forms, import tools, or agent workflows.

The architectural rule is:

> **Arcogine owns the semantics and publication of production-system designs. Consumers may own how drafts are authored. Runtime contexts instantiate immutable published model versions and never double as the design workspace.**

This follows the Product Charter's lifecycle-continuity thesis without implying one UI, one runtime, or one mutable state store for every mode.

## 2. Orthogonal lifecycle, shared ontology

Factory design is **operationally orthogonal** to simulation/runtime behavior but **semantically inseparable** from it.

It is orthogonal because:

- design can occur without running a simulation;
- runtime can instantiate a published model without an editor;
- design changes are ordered by model revision and provenance, not simulated time;
- drafts may be incomplete or temporarily invalid while executable runtime models may not;
- design review/publication and runtime event processing have different authority and lifecycle rules.

It is semantically inseparable because:

- design and runtime must share product, operation, configured-resource, policy, constraint, and behaviorally relevant layout semantics;
- any future capability or qualification semantics must be independently justified rather than inferred from the current resource record;
- validation must reflect what runtime can actually execute;
- consumers must not manually translate an editor-specific ontology into unrelated runtime semantics;
- every runtime or verification context must identify the exact model version it instantiated.

```text
Consumer-specific design experience
                 |
                 v
            Design draft
      mutable, possibly invalid
                 |
          validate / publish
                 v
       Factory model version
      immutable and identified
                 |
             instantiate
                 v
        Factory runtime state
        mutable in its context
```

## 3. Scenario, model, and runtime are different things

The model boundary distinguishes an experiment from the production system being experimented on and from mutable execution state.

```text
Scenario
    describes a simulation experiment/run input envelope

FactoryModel
    describes the production system

FactoryModelVersion
    immutable published identity of that production system

ExecutableFactoryModel
    validated/resolved representation derived from one model version

FactoryRuntime
    mutable state instantiated from that executable model
```

No scenario/run input envelope is currently implemented. The distinction remains architectural: a future scenario may describe an experiment or reference runtime inputs, while the canonical `FactoryModel` describes the production system itself. No scenario schema or serialization format is selected here.

Conceptually:

```text
Future scenario -------+
Game design -----------|
Industrial design UI --|
Optimizer -------------+--> FactoryModel --> publish --> FactoryModelVersion
Importer / adapter ----|
CLI / test builder ----+
```

A scenario may contain or reference a published model together with runtime inputs such as workload and simulation settings.

### 3.1 Concern classification

| Concern | Ownership |
|---|---|
| Product definitions | `FactoryModel` |
| Operation definitions | `FactoryModel` |
| Complete configured productive resources | `FactoryModel` |
| Explicit eligible-resource IDs | `FactoryModel` |
| Reusable technical specifications/classification | Separate future concept, only if an independent cross-consumer contract justifies it |
| Capability/qualification relations | Separate future concept, only if behavior must discover or verify applicability |
| Resource grouping or hierarchy | Separate future concept, only if it owns consequential scheduling, capacity, responsibility, or reporting semantics |
| Semantic layout | `FactoryModel` |
| Simulation seed/limits | Scenario/runtime context |
| Production workload | Scenario/runtime input |
| Future consumer-specific policy inputs | Scenario/context, if required |
| Production orders | Runtime |
| Work items | Runtime |
| Queues and assignments | Runtime |
| Transfers in progress | Runtime |
| Simulated time/events | Runtime |
| KPIs/performance observations | Runtime observation |

## 4. Canonical model boundary

The exact Java types are intentionally not fixed yet, but the lifecycle responsibilities are.

```text
FactoryDraft
    mutable consumer authoring state
    may be incomplete or invalid

FactoryModel
    complete semantic production-system definition

FactoryModelVersion
    immutable published semantic snapshot
    deterministic content-derived fingerprint

ExecutableFactoryModel
    validated/resolved runtime-ready derivative

FactoryRuntime
    mutable state instantiated from one model version
```

These need not all become separate persistence entities or modules initially.

Today's `FactoryModelVersion` is an immutable *validated semantic snapshot*: publishing it proves the design is executable and gives it the durable `factory-model:v1` `ModelFingerprint` defined by the [Factory Model v1 specification](factory-model-v1.md). It is still not itself a **controlled revision** entity. Governance identity/history capability now separately provides durable `ControlledRevisionId`, immutable revision lineage/provenance, authoritative persistence, and exact historical semantic-state resolution. Approval state, external workflow references, and deployment remain separate later records. Section 11 draws the identity distinction; see section 11 below for what the fingerprint and controlled revision do and do not carry.

### 4.1 What belongs in the canonical model

A fact belongs in the canonical model when changing it changes the executable meaning of the production system across consumers or lifecycle modes. Examples include:

- product/material definitions;
- operations/work definitions;
- complete configured productive resources;
- explicit eligible-resource IDs;
- resource-pool/work-center membership when it has real scheduling, capacity, responsibility, or reporting semantics;
- processing/setup/transfer/dispatch policies when part of the designed system;
- executable constraints;
- shared verification objectives when modeled explicitly;
- behaviorally relevant floor geometry, position, orientation, footprint, connection points, zones, or transfer relationships.

### 4.2 What does not belong in the canonical model

Consumer presentation and transient authoring state remain consumer-owned unless a concrete cross-consumer requirement justifies promotion. Examples include:

- selection/hover state;
- editor camera;
- drag preview and snapping guides;
- sprites, meshes, animation offsets, sound, colors, themes;
- game tutorial state, unlocks, score, stars, and player currency;
- local undo/redo stacks;
- decorative objects with no production semantics.

## 5. Derived executable representation is not a second model

A runtime may compile or resolve immutable structures for efficiency:

- resolved configured-resource references;
- eligible-resource indexes;
- operation/routing indexes;
- geometry/spatial indexes;
- validated transfer relationships;
- derived scheduling metadata.

These structures are derived from exactly one published semantic model. They are not independently authored sources of truth.

The invariant is:

> **There is no independently authored runtime factory model. Runtime indexes and compiled structures are derivations of one published `FactoryModelVersion`, not another representation that consumers must maintain manually.**

## 6. Design revision is not simulated time

Design changes and simulation events must not share one event stream merely because both are changes.

Simulation events are facts in simulated time:

```text
OrderReleased at tick 10
OperationStarted at tick 14
TransferCompleted at tick 19
```

Design changes belong to a model-revision context:

```text
ResourcePlaced in draft revision 12
OperationRequirementChanged in revision 13
FactoryModelPublished as version 4
```

| Design lifecycle | Runtime lifecycle |
|---|---|
| model/draft revision | event sequence |
| author or decision source | event source |
| wall-clock provenance | simulated time |
| validation/publication state | runtime fact |
| branch/review context | run/session context |
| published model version | instantiated source model version |

A future real-operations workflow connects design to governance and deployment explicitly:

```text
Candidate change
        ↓
Controlled revision (Governance; persisted, with lineage)
        ↓
Technical assessment (validation / simulation / verification)
        ↓
Authorization
        ↓
Operational deployment plan / target application
        ↓
Observed result / reconciliation
```

The revision is persisted before authorization, not after: an unauthorized revision, or one authorized but never deployed, must remain representable. Authorization and deployment are separate records that reference the revision, not steps a revision passes through to come into existence.

Factory Design owns the semantic design and publication boundary. Governance owns controlled revision/change-control and authorization interpretation. [Operational Execution and Digital Twin](operational-execution-digital-twin.md) owns deployment targeting/application, effective transformed/applied-artifact provenance, resulting operational facts, and reconciliation. That bridge preserves provenance and authority; it does not turn editor operations into simulation or production-control events.

Authorization may be owned externally: Arcogine can produce the technical assessment evidence a candidate needs (validation results, semantic diff, simulation/verification outcomes) without itself hosting the request/review/approval workflow. See section 11.2 below and the operational architecture.

## 7. Cross-consumer ownership

| Concern | Ownership |
|---|---|
| Product and operation definitions | Arcogine canonical model |
| Complete configured productive resources | Arcogine canonical model |
| Explicit eligible-resource IDs | Arcogine canonical model |
| Reusable technical specifications/classification | Future orthogonal concept; not part of the current implementation slice |
| Capability/qualification relations | Future orthogonal concept; not inferred from explicit eligibility |
| Resource grouping or hierarchy | Future orthogonal concept; admitted only when it owns consequential behavior |
| Semantic position/footprint when behavior depends on them | Arcogine canonical model |
| Structured executability validation | Shared Arcogine model/design capability |
| Semantic model identity (fingerprint) | Shared Arcogine model infrastructure; durable fingerprint policy completed by Governance identity/history capability |
| Controlled revision lifecycle and lineage | Cross-domain Governance and Conformance capability (Governance identity/history capability) — see the [controlled revision contract](controlled-revisions.md) and the [Governance and Conformance Capability Plan](../planning/governance-conformance-capability.md) |
| Change request/review/authorization workflow | Cross-domain Governance and Conformance capability (governed-change and external-workflow integration), or an external change-management system referenced not depended on |
| Operational deployment target/application and effective applied-artifact provenance | [Operational Execution and Digital Twin](operational-execution-digital-twin.md) |
| External operational observations and modeled-versus-observed reconciliation | [Operational Execution and Digital Twin](operational-execution-digital-twin.md) |
| Model publication and runtime instantiation | Shared Arcogine boundary |
| Runtime dispatch/queues/work/transfers | Arcogine runtime |
| Semantic model comparison/diff | Cross-consumer candidate; share when justified |
| Draft persistence/branching/merge/collaboration | Cross-consumer candidate; defer until justified |
| Editor selection/camera/drag/drop/palette | Consumer |
| Visual assets and presentation | Consumer |
| Game budget/unlocks/score/progression | Game consumer |
| Industrial engineering constraints | Arcogine model or verification capability when semantically supported |
| Throughput/lead-time objectives | Shared verification capability when supported |

## 8. Spatial layout and resource hierarchy are independent

If a future grouping contract warrants it, resource/organizational scope may eventually resemble:

```text
Factory
  Work Center
    Resource Pool
      Resource Instance
```

Spatial layout is a separate dimension:

```text
Factory Floor
  Position
  Orientation
  Footprint
  Connection points
  Transfer relationships
```

Hierarchy supports containment, responsibility, capacity aggregation, scheduling scope, reporting, or authorization. Spatial layout supports physical placement and transport consequences. This possible future scope model does not require splitting the current `ConfiguredResource` into a reusable definition and an installed instance.

A resource may move without changing identity or hierarchy membership. A resource may change resource-pool membership without moving physically.

The [ISA-95 semantic mapping](isa-95-semantic-mapping.md) remains the reference for whether hierarchy concepts should be adopted, aliased, extended, or deferred.

## 9. Semantic geometry versus presentation geometry

Arcogine owns geometry only when it changes executable behavior or shared validation.

Semantic geometry may include floor dimensions, resource position/orientation/footprint, connection points, zones/restrictions, adjacency/reachability constraints, and transfer-distance inputs.

Consumers own meshes/sprites, animation anchors, camera framing, selection outlines, label placement, decorative composition, and visual interpolation between authoritative engine states.

## 10. Constraint ownership

Every design rule should be classified before implementation.

### 10.1 Executability constraints — Arcogine

These answer whether the production system can be published/instantiated coherently:

```text
Referenced products, operations, and configured resources exist
Identifiers are unique
Operation graph is valid
Each operation can resolve eligible capacity
Semantic footprint lies inside the floor
Forbidden semantic footprints do not overlap
Required transfers are representable
```

### 10.2 Verification objectives — shared verification concern

These test whether an executable design meets a target:

```text
Throughput >= target
Lead time <= limit
Maximum utilization <= threshold
Safety separation >= required distance
```

An objective is not automatically an executability invariant.

### 10.3 Consumer rules — consumer

These apply only to one experience:

```text
Player construction budget
Unlock level
Star-rating threshold
Tutorial restriction
Aesthetic preference
```

## 11. Publication, identity, and provenance

A published model version is the bridge between design and downstream contexts.

Publication identity must not bundle two concepts together:

- **Semantic fingerprint** — a deterministic identity derived from canonical model content under a durable versioned canonicalization policy ([Factory Model v1](factory-model-v1.md) today). Equivalent canonical facts produce equivalent fingerprints, independent of consumer presentation metadata, authorship, or timing. This is the publication identity carried by `FactoryModelVersion`.
- **Controlled revision** — a persisted, controlled historical configuration occurrence with separate identity, semantic-fingerprint binding, lineage, and recording provenance. [controlled revision contract](controlled-revisions.md) defines it, implemented through `ControlledRevisionId`, `ControlledRevision`, and `ControlledRevisionAuthority`. Authorization, external workflow linkage, conformance, and deployment remain separate records that may reference a revision; a revision need not be authorized or deployed to exist.

Every runtime or verification result must retain the semantic fingerprint of the model version it instantiated. A `ControlledRevisionId` is additional historical provenance only when an authoritative revision binding actually exists; it must not be synthesized from the fingerprint.

The desired invariant is:

> Given a published model version, the relevant runtime inputs (including the seed for simulation), and ordered commands, Arcogine can identify exactly which semantic design produced the resulting events and observations.

Persistent controlled-revision lineage is available through the Governance revision authority, and semantic `ChangeSet`/impact comparison (Governance semantic ChangeSet/impact capability's initial slice) is now available through the Governance-owned `ChangeSet`/`ImpactScope` contracts and the factory-domain `FactoryModelSemanticComparator`. Authorship beyond recording provenance, approvals/authorization, external workflow relationships, and branch/ref semantics remain separate, unimplemented capabilities. Semantic ChangeSets remain a separate capability owned by Governance and must not be folded into Factory Design publication identity even though they are now implemented.

Fingerprint equality is a content-derived fact Arcogine computes unilaterally under a named policy;
it never requires an external system to establish. Equal semantic content may occur in distinct
controlled revisions; a later revision may be semantically identical to an earlier one (a revert or
no-op edit) without the two collapsing into one. A human-facing label such as "v3" is presentation
convenience for a controlled revision, not semantic identity. Arcogine is authoritative for the
model, the fingerprint, semantic diff between versions, and the technical assessment/simulation/
verification evidence about a candidate change; none of that requires an external system to exist.

### 11.1 Semantic evolution

Factory owns authored production-system facts; Engine owns the rules that interpret them
(distance, rounding, destination binding, reservation, transfer lifecycle). Changing Engine
interpretation alone never changes a model's fingerprint, and authored facts are never synthesized
to make an interpretation applicable. [Factory Model v1](factory-model-v1.md) and
[Factory Model v2](factory-model-v2.md) own their records, field membership, validation predicates
and canonical bytes; this section owns how a policy is composed and how policies relate.

**One closed policy, one aggregate identity.** A fingerprint policy identifies one complete, closed
semantic/canonicalization grammar — the records it admits, the validation predicates within and
across them, its canonical bytes and its rejection behavior — not merely a hash algorithm. Ordinary
serializer bytes never define identity, and publication rejects inputs for which canonicalization
is undefined, so fingerprinting is total over published models. One published Factory semantic
artifact has exactly one aggregate `ModelFingerprint`; Factory defines no per-concern fingerprints.

**Optional authored records.** A closed policy may admit explicitly present optional authored
records beside its required ones, and the presence of each is canonical content. An absent record
means the design makes no assertion in that semantic dimension; nothing is synthesized for it from
a default. A present record carries authored values, and a legal zero or default-like value is an
authored value distinct from absence in both meaning and canonical bytes. Unknown or partly
authored facts are draft or adapter state, never published content; the policy states what makes a
present record complete and rejects anything else. Which combinations of records are valid is
decided by the policy's own closed predicate, not by a separate combination registry.

**Attribution fixes the grammar.** Until a policy has attributed records its definition may be
corrected in place. After the first retained or accepted attribution its definition is fixed as a
whole under the [semantic evolution rules](overview.md#semantic-evolution-and-support): admitting a
new record or variant, changing an accepted value domain, or changing a predicate, rejection rule
or canonical byte requires a distinguishable policy identity; old fingerprints are never rewritten or rederived; and a
controlled revision still binds exactly one fingerprint while lineage may cross policies without
rewriting either artifact. A new policy is warranted by an identity-defining grammar change, not by
a new combination of records a policy already admits.

**Exact references.** An aggregate may contain an exact immutable reference to an independently
governed technical contract only when a concrete semantic dependency requires the reference itself
— a statement, such as dependence on one approved specification revision, that inlined values
cannot preserve ([Factory Resource Semantics](factory-resource-semantics.md)). The reference is
aggregate content covered by the one fingerprint, not a second identity for the design.

**No automatic lift; explicit comparison.** There is no automatic lift between policies. A V1 model
has no spatial facts and therefore no spatial behavior — the truthful execution of a design that
never authored spatial semantics, not a degraded mode. Position, footprint and handling values
must be explicitly authored and published; historical facts are never invented as defaults, and a
design is never stripped of content a policy cannot represent in order to publish it under that
policy. Cross-policy comparison is explicit: a semantic `ChangeSet` must not silently span
policies by inventing facts one model never declared. Before an actual cross-policy controlled
transition, Arcogine provides artifact resolution with a registered verifier/decoder for each
policy in scope and either an explicit migration classification or an explicitly chosen common
semantic representation for any fine-grained comparison that claims equivalence, implementing only
the seam that transition requires. A common representation may map one policy's content into
another's form only when the mapping invents nothing — records absent in the source stay absent —
and preserves every semantic distinction of the source policy, including names, identifiers,
order and nullability; where that cannot be shown, the comparison states its limitation. Such
equivalence is never full-fingerprint equality, the same controlled occurrence, evidence
applicability or reattribution of either artifact.

**Publication validity is not Engine applicability.** A valid published artifact is executable
under an Engine interpretation only when that interpretation's own definition supports the
artifact's exact policy and the records, values, variants and interactions the artifact represents;
recognizing which records are present is not sufficient. An Engine refuses an artifact outside that
domain before runtime mutation rather than ignoring represented content or supplying absent content.

**Support is separate from identity.** Retained attribution requires the exact definition of every
referenced policy to remain resolvable; continuing publication, decoding, execution, migration and
interoperability are separately scoped support obligations declared by the owning contract, not
consequences of a policy existing. Defining a successor policy neither retires publication under an
earlier one nor promises it indefinitely, and nothing mandates eternal readers for every policy or
permanent coexistence of any two. A named support scope, such as the combinations of records one
consumer or Engine accepts, may be declared where useful but never participates in content
identity. Factory currently defines no open extension envelope, concern registry, per-concern
fingerprint or generic migration framework; one would need a concrete requirement that closed
policies with optional records and exact references cannot meet.

### 11.2 External change-management and deployment integration

Arcogine does not require organizational change-management workflow to live inside the factory domain.

A controlled model revision may reference an external change record, such as an issue-tracker key. Arcogine remains authoritative for the model and its domain-specific semantic facts; Governance owns the durable revision/change interpretation, while the external system remains authoritative for request/review/authorization workflow unless that responsibility is explicitly brought into Arcogine. That reference is metadata attached to the revision: it is never consulted to determine model content, fingerprint, or semantic behavior, and Arcogine adopts no external system's schema, workflow states, or terminology into its own domain model to support it. What authorization a deployment requires is a matter of the applicable change-control policy, which Arcogine does not own; the invariant is narrower — a deployment must be attributable to the authorization that policy required for it.

Conformance/verification assessments, authorization decisions, simulation runs, and operational deployments remain separate artifacts from the model and from each other. They may reference a fingerprint and authoritative controlled revision when applicable, but none of them is the model. The operational deployment record additionally owns target, execution context, adapter/profile/transformation, effective applied-artifact/version, apply/verification result, and reconciliation provenance; Factory Design must not duplicate those mechanics.

## 12. Shared validation and publication boundary

Validation belongs at the shared model/design boundary, not independently in each consumer.

A consumer may perform optimistic local checks for responsiveness, but Arcogine remains authoritative for shared executability semantics.

Validation should be deterministic, structured, attributable to fields/entities when possible, explicit about severity, and atomic with respect to publication/instantiation.

The initial shared substrate should include:

1. a canonical factory model contract;
2. structured validation;
3. an explicit publication boundary producing an immutable model version;
4. deterministic runtime instantiation from that version;
5. runtime provenance linking runs to model versions.

The first consumer may still own mutable drafts, undo history, and editor persistence.

## 13. Architectural dependency direction

```text
Consumer authoring sources
          |
          v
Canonical factory model
 definitions + validation
          |
     publish version
          |
     +------------+-------------+
     |            |             |
     v            v             v
Simulation    Verification   Governance
runtime       contexts       revision/change
                                |
                                v
                         Operational deployment
                                |
                                v
                         External observations
                                |
                                v
                           Reconciliation
```

The canonical model must not depend on a specific UI, simulation transport, industrial protocol, or deployment target. Runtime must not mutate the published model. Operational deployment references an authorized governed revision rather than becoming part of model publication. Consumers must not depend on mutable runtime internals to author a design.

This may initially be implemented inside existing modules/packages. A new Gradle module is warranted only when dependency direction or ownership invariants justify one.

## 14. Review checklist

When factory-design semantics change, ask:

1. Is this production-system design, design lifecycle, runtime state, verification state, operational deployment/reconciliation state, or one consumer's experience?
2. Does changing it alter executable behavior across consumers?
3. Is it part of the scenario/run context rather than the factory itself?
4. Is the fact ordered by model revision or simulated time?
5. Can a draft be invalid while a published model must reject that state?
6. Is this configured-resource identity, a future reusable specification/classification, resource hierarchy, or spatial layout?
7. Is the rule executability, verification, or consumer-specific?
8. Can a runtime result identify the exact published model version that produced it?
9. Are runtime structures derived from that model, or are we creating a second authored representation?
10. Is a new shared design abstraction justified by a concrete cross-consumer workflow?
11. Are deployment targeting/application, external observation ingestion, or reconciliation semantics being placed in Factory Design even though they belong to Operational Execution?

## 15. Triggers for revisiting this proposal

Revisit this document when Arcogine introduces or materially changes:

- a canonical `FactoryModel` or equivalent;
- any future scenario-to-model adapter boundary;
- model versioning/hashes/lineage;
- shared draft/design services;
- semantic model diff/compare;
- design branching/merge/collaboration;
- deployment of model changes to real operations;
- behaviorally relevant spatial geometry;
- work-center/resource-pool hierarchy;
- cross-consumer validation rules;
- another independent design consumer;
- ISA-95/AAS/other model import/export.

As further capabilities are implemented and accepted, authoritative current-state portions should move into or be reconciled with [`overview.md`](overview.md) rather than leaving proposed behavior presented as current fact.
