# Factory Design Evolution Research

> **Status:** Maintained research programme  
> **Scope:** Factory-model and authoring questions that are not admitted to implementation because their durable invariant or cross-consumer need is not yet established  
> **Authority:** Research only; accepted Factory Model v1/v2 semantics and current implementation remain authoritative elsewhere

## Purpose

The current Factory Design implementation has a usable canonical publication seam. This document keeps **unresolved extensions** out of executable planning until evidence establishes what they should mean.

## Equipment/resource ontology — READY

Research question:

> Does Factory Design need a reusable equipment/resource definition distinct from installed resource instances, and what semantic invariant would that distinction carry?

Current implementation deliberately allows one resource record to represent one concrete installed unit. Reuse in a game catalogue or editor is not by itself sufficient reason to introduce canonical type/instance semantics.

The investigation must compare at least:

- reusable equipment definition/class/model;
- installed resource identity;
- capability and operation requirement;
- station/work-center/resource-pool semantics;
- physical placement versus equipment hierarchy;
- tool, worker, transport, and storage roles;
- external physical-asset identity versus canonical design identity.

A split should be promoted only if sharing a definition implies a durable semantic invariant beyond authoring convenience.

## Validation finding taxonomy — CANDIDATE

Current validation supplies deterministic executability rejection sufficient for admitted Factory Model work. A richer common diagnostic contract may eventually need stable finding codes, severity, warnings, and structured remediation context.

Promote this only when at least one concrete consumer requires diagnostics that the current validator cannot expose reliably. The research must decide which diagnostics are cross-consumer semantics versus editor presentation.

## Semantic comparison depth — CANDIDATE

The implemented comparison slice identifies add/remove/modify changes to current factory entities through the Governance semantic-change seam. Finer comparison is not admitted merely because more detailed diffs are imaginable.

Research should test concrete needs for:

- routing/operation-detail changes;
- spatial/layout changes;
- capability/constraint changes;
- policy-version transitions;
- cross-policy comparison where an older model never authored the newer facts;
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

This research should remain coupled to the equipment ontology and Engine scheduling evidence rather than introducing an empty hierarchy abstraction.

## Spatial/material-flow evolution

Questions such as orientation, paths/aisles, conveyors, connection points, explicit transport resources, buffers, or congestion are tracked separately in the research register because current Factory Model v2 and Engine Semantics v1 deliberately stop before those capabilities.

## Promotion rule

A research item enters `docs/planning/` only when:

1. a concrete consumer or accepted architecture creates the need;
2. the durable semantic invariant is explicit;
3. ownership is settled;
4. migration/compatibility implications are understood; and
5. executable acceptance evidence can be stated.

Until then, current Factory Model semantics remain truthful and no placeholder abstraction should be introduced.
