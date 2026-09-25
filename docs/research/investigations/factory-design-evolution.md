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

## Qualified operation-resource applicability and performance — CANDIDATE

Current explicit eligible-resource IDs are sufficient for present execution. They do not, however, express engineering qualification or resource-dependent performance when heterogeneous resources can perform the same operation under different material, tooling, dimensional, quality, duration, consumption, or cost constraints.

Research this only when a concrete optimizer, industrial authoring/verification workflow, game mechanic, MES import, or other consumer needs Arcogine to **discover or verify** applicability rather than accept explicitly authored eligibility. The investigation must keep qualification separate from runtime availability/selection and must test whether operation-resource performance needs its own relation rather than being hidden inside a reusable equipment type.

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

The more fundamental boundary question is concluded: [Factory Model Semantic Composition](factory-model-semantic-composition.md) established that a Factory policy is one closed grammar that may admit explicitly present optional authored records under one aggregate fingerprint, reconciled into the [Factory semantic-evolution contract](../../architecture/factory-design.md#111-semantic-evolution). Its reopening triggers name the future concerns below.

The narrower [transfer-lifecycle question](transfer-semantics.md#concluded--transfer-lifecycle-independence) is concluded for current spatial-record content in the [transfer-applicability boundary](../../architecture/transfer-applicability.md). Explicit non-spatial hand-off or timing remains a separate CANDIDATE requiring a concrete consumer need, and could reopen the still-unreleased V2 grammar before attribution. It is distinct from the richer spatial/material-flow capabilities below. Exact Engine identity/support is the READY [successor applicability question](engine-applicability-after-transfer-boundary.md).

The composition investigation does **not** implement the future concerns listed here. Questions such as orientation, paths/aisles, conveyors, connection points, explicit transport resources, buffers, or congestion remain separate capability questions in the research register because current Factory spatial and Engine transfer semantics deliberately stop before them. When those questions are later investigated, they must consume the then-current transfer/composition rules rather than implicitly selecting a new whole-model version or aspect framework themselves; admitting a new or split authored record is a new Factory policy.

## Promotion rule

A research item enters `docs/planning/` only when:

1. a concrete consumer or accepted architecture creates the need;
2. the durable semantic invariant is explicit;
3. ownership is settled;
4. migration/compatibility implications are understood; and
5. executable acceptance evidence can be stated.

Until then, current Factory Model semantics remain truthful and no placeholder abstraction should be introduced.
