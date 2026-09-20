# Operational Execution and Digital-Twin Implementation Admission

> **Status:** NOT ADMITTED — no Operational implementation slice is currently safe to execute  
> **Scope:** Record the concrete admission conditions for future Operational work without embedding unresolved architecture in delivery planning  
> **Authority:** Planning only; unresolved semantics live in research, not in this plan, and adopted semantics live in canonical architecture/specifications

## Current landed dependencies

The following reusable substrate is available and must be consumed rather than duplicated:

- canonical published production-system semantics from Factory Design;
- durable semantic fingerprint and controlled-revision identity/history from Governance;
- Governance semantic change/impact, requirement/assertion, and initial conformance/finding contracts;
- Engine runtime observation/event core and deterministic simulation semantics.

Operational Execution itself remains unimplemented.

## Why no implementation slice is admitted

The durable operational identity is no longer a blocker: the [Operational continuity contract](../architecture/operational-continuity.md) is adopted and defines the accountable-continuation referent together with its continuity, divergence, lineage, acceptance, and record-attachment rules.

That closes one critical-path semantic question; it does not admit a slice. The remaining shared boundaries — actor/capability ownership, external operation realization, authoritative subject correspondence, and temporal reconciliation — are still unresolved, and each must be settled at least to the extent required to define a concrete safe slice. Slice-specific safety/failure semantics, explicit prerequisites, and executable acceptance evidence remain independently required by the promotion criteria below.

Those open questions are tracked in [Operational Execution and Digital-Twin Boundary Research](../research/investigations/operational-execution-digital-twin-boundaries.md), not here. The [Agency and decision boundary](../research/investigations/agency-decision-boundary.md) question is now concluded: its durable role, attribution, and provenance rules in [Architecture Overview — Attribution and decision boundaries](../architecture/overview.md#attribution-and-decision-boundaries) constrain any future slice, but they do not settle actor/capability ownership and do not admit one.

## Constraints any future implementation must preserve

Any promoted Operational slice must preserve:

- one shared semantic production model rather than a simulation-only/production-only fork;
- no global `PRODUCTION / STAGING / SIMULATION` or equivalent whole-execution taxonomy;
- the accepted durable operational identity's referent, continuity, divergence, lineage, acceptance, and record-attachment rules, including its explicitly deferred representation, persistence, coordination, registry, closure, and ownership questions;
- independently provenanced raw external observations;
- explicit external-subject to Arcogine-subject correspondence rather than identity inference from names/endpoints/configuration;
- requested operation, external command/result, actual transition, observation, and reconciled interpretation as distinct facts;
- verified identity/trust/authority and fail-safe behavior where external consequence requires them;
- Governance-owned revision/change/conformance/evidence-use contracts where applicable;
- Engine-owned simulation semantics and provenance rather than a parallel Operational simulator.

## Promotion criteria

The first Operational delivery coordinate may be created only after:

1. the exact semantic referent of the proposed slice is settled;
2. any required architectural decision is adopted or otherwise no longer an implementation blocker;
3. module/track ownership is known and does not duplicate Factory, Engine, Governance, or shared actor semantics;
4. prerequisites are explicit and landed or deliberately fixture-backed;
5. failure/safety semantics are explicit for consequential behavior; and
6. executable acceptance evidence is defined.

When those conditions are met, add the narrowest concrete implementation slice here. Do not restore the previous speculative multi-step Operational sequence merely as a roadmap placeholder.
