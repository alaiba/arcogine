# Factory Design Evolution Research

> **Status:** Maintained research programme  
> **Scope:** Factory-model and authoring questions that are not admitted to implementation because their durable invariant or cross-consumer need is not yet established  
> **Authority:** Research only; the current Factory model specification and implementation remain authoritative elsewhere

## Purpose

The current Factory Design implementation has a usable canonical publication seam. This document keeps **unresolved extensions** out of executable planning until evidence establishes what they should mean.

## Equipment/resource ontology — CONCLUDED

Research question:

> Does Factory Design need a reusable equipment/resource definition distinct from installed resource instances, and what semantic invariant would that distinction carry?

**Verdict: KEEP COLLAPSED FOR NOW.** The decision-quality evidence is preserved in the [Factory Resource Semantics Research Report](factory-resource-semantics.md), and the durable interpretation is recorded in [Factory Resource Semantics](../../architecture/factory-resource-semantics.md).

The canonical Factory model currently needs one independently identified **configured productive resource** per designed participant. Repeated catalogue/template origin, equal configured values, a manufacturer/model label, or installing the same authored item more than once do not by themselves justify a second reusable definition identity.

A future reusable specification is justified only when its identity carries a checkable cross-consumer technical contract or dependency that complete configured-resource records cannot preserve—for example, conformance to an explicitly versioned specification, admitted configuration constraints/guarantees, or cross-model change-impact dependency on that specification. Equal values alone never establish shared specification identity.

This conclusion also preserves these orthogonal distinctions:

- configured resource identity is not external serialized physical-asset identity; Operational correspondence owns that future relationship;
- capability/qualification is not equipment-type identity and remains deferred until Arcogine must infer or verify applicability;
- hierarchy/work-center/pool membership is not physical placement; and
- runtime queues, availability, setup state, and observations remain outside the immutable design.

No Factory implementation slice is promoted by this conclusion. Current `ConfiguredResource` remains the supported complete configured-resource record.

## Setup and changeover semantics — READY

[Factory Setup and Changeover Semantics Research](factory-setup-changeover-semantics.md) asks what
setup/changeover semantics, if any, Arcogine should represent and whether the current scalar
`ConfiguredResource.setupTime` truthfully represents them.

This is READY because the uncertainty is already present in the canonical model: `setupTime`
participates in authored Factory content and runtime projection while current Engine execution does
not consume it. The investigation must be free to remove/defer the field, retain a precisely bounded
fixed meaning, or replace it with operation-resource or state/sequence-dependent semantics. It must
keep immutable authored setup rules distinct from mutable runtime setup state.

## Resource characteristics and operation requirements — READY

[Factory Resource Characteristics and Operation Requirements Research](factory-resource-characteristics-requirements.md)
asks which configured-resource characteristics, if any, are canonical Factory facts, how operation
requirements should relate to them, and what the current privileged `capacityLiters` field should
become.

This is READY because `capacityLiters` is already canonical, comparable and observable while current
execution does not use it. The investigation must test removal/deferment as the null candidate and
must not jump from one specialized field to an untyped generic property bag. Typed characteristics
or requirements do not by themselves justify reusable equipment/specification identity.

## Qualified operation-resource applicability — CANDIDATE

Current explicit eligible-resource IDs remain sufficient for present execution. They do not express
engineering qualification when Arcogine must discover or verify whether a resource satisfies
material, tooling, dimensional, quality, environmental or other operation requirements.

Promote this when a concrete optimizer, industrial authoring/verification workflow, game mechanic,
MES import, or another consumer needs Arcogine to derive or verify applicability rather than accept
explicit authored eligibility. Keep qualification separate from runtime availability/selection,
resource-dependent performance, and reusable equipment-type identity. The READY resource-
characteristics investigation may supply candidate requirement/provision semantics, but it does not
pre-decide that eligibility must become derived.

## Operation-resource-dependent performance — CANDIDATE

Current step duration belongs to `OperationStepDefinition`, so two resources eligible for the same
step cannot currently differ in processing duration because of which resource is selected. More
generally, Arcogine has no explicit relation for selected-resource-dependent duration, consumption,
yield/quality consequence, or other result-affecting performance.

The factory-design game now provides a concrete motivating use: equipment choices such as
cheap/slow versus expensive/fast or specialized versus flexible-but-less-efficient cannot be
represented merely by changing game catalogue prices. That makes this question materially closer to
promotion, but not yet READY until its own bounded brief states the decision, candidate ownership
models, proving cases, evidence expectations and exit criteria.

When promoted, ask:

> When several resources are eligible for the same operation step, which result-affecting properties
> may legitimately depend on the selected resource, and should those facts belong to the step, the
> resource, or an explicit operation-resource relation?

Keep this separate from qualification: "may perform this operation" and "what happens when this
resource performs it" are different semantic relations.

## Validation finding taxonomy — CANDIDATE

Current validation supplies deterministic executability rejection sufficient for admitted Factory Model work. A richer common diagnostic contract may eventually need stable finding codes, severity, warnings, and structured remediation context.

Promote this only when at least one concrete consumer requires diagnostics that the current validator cannot expose reliably. The research must decide which diagnostics are cross-consumer semantics versus editor presentation.

## Semantic comparison depth — CANDIDATE

The implemented comparison slice identifies add/remove/modify changes to current factory entities through the Governance semantic-change seam. Finer comparison is not admitted merely because more detailed diffs are imaginable.

Research should test concrete needs for:

- routing/operation-detail changes;
- spatial/layout changes;
- capability/constraint changes;
- comparison where one design never authored an optional record the other carries, or across a future promoted definition and its successor;
- explanations useful to game players, industrial reviewers, optimizers, and governed change.

Promote only shared semantics that more than one concrete consumer needs or that Governance requires for an accepted workflow.

## Shared draft lifecycle — CANDIDATE

Consumer-specific drafts, undo/redo, local persistence, branching, collaboration, comments, locks, and autosave do not automatically belong to Arcogine.

Research a shared draft lifecycle only when multiple concrete workflows need common semantic behavior—for example industrial design plus optimizer/game authoring, human/agent co-design, or multi-user controlled design review.

The question is not whether editors need these features; it is whether Arcogine must own a **shared lifecycle invariant** that consumers cannot safely implement independently.

## Factory participation in governed change — CANDIDATE

Governance owns controlled revision identity/history, semantic change, conformance/evidence, and governed-change records. Operational work will own application to independently existing systems once those semantics are accepted.

Research is needed only if a concrete workflow demonstrates a Factory-specific semantic responsibility that is not already expressible through those sibling contracts. Do not create a Factory-only approval repository, deployment mechanism, telemetry path, or reconciliation model.

## Resource pools and work centers — CANDIDATE

A group deserves a canonical concept only if it owns real behavior or interpretation such as scheduling scope, aggregate capacity, responsibility, reporting, or capability aggregation. UI folders and physical proximity are insufficient.

This research should remain coupled to qualified applicability/performance and Engine scheduling evidence rather than introducing an empty hierarchy abstraction.

## Factory semantic composition and spatial/material-flow evolution

The more fundamental boundary question is concluded: [Factory Model Semantic Composition](factory-model-semantic-composition.md) established that the Factory definition is one closed grammar that may admit explicitly present optional authored records under one aggregate fingerprint, reconciled into the [Factory semantic-evolution contract](../../architecture/factory-design.md#111-semantic-evolution). Its reopening triggers name the future concerns below.

The narrower [transfer-lifecycle question](transfer-semantics.md#concluded--transfer-lifecycle-independence) is concluded for current spatial-record content in the [transfer-applicability boundary](../../architecture/transfer-applicability.md). The Factory model and the Engine are mutable current development definitions until an explicit stability/support promotion under the [semantic evolution rules](../../architecture/overview.md#semantic-evolution-and-support); the Factory has one work-in-progress grammar carrying the optional spatial record. The current Engine executes the spatial-absent case and refuses present spatial content until [Spatial Runtime Consequences](../../planning/spatial-runtime-consequences.md) makes the already-specified spatial semantics executable. The successor Engine-identity applicability question was [superseded before investigation](engine-applicability-after-transfer-boundary.md) by the provisional semantic-contract reset and gates nothing. Explicit non-spatial hand-off or timing remains a separate CANDIDATE requiring a concrete consumer need; if one is admitted, correcting the grammar for it is owned by the Factory semantic-evolution contract. It is distinct from the richer spatial/material-flow capabilities below.

The composition investigation does **not** implement the future concerns listed here. Questions such as orientation, paths/aisles, conveyors, connection points, explicit transport resources, buffers, or congestion remain separate capability questions in the research register because current Factory spatial and Engine transfer semantics deliberately stop before them. When those questions are later investigated, they must consume the then-current transfer/composition rules rather than implicitly selecting a new whole-model version or aspect framework themselves; admitting a new or split authored record is a grammar correction owned by the Factory semantic-evolution contract.

## Promotion rule

A research item enters `docs/planning/` only when:

1. a concrete consumer or accepted architecture creates the need;
2. the durable semantic invariant is explicit;
3. ownership is settled;
4. migration/compatibility implications are understood; and
5. executable acceptance evidence can be stated.

Until then, current Factory Model semantics remain truthful and no placeholder abstraction should be introduced.
