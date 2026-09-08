# Research

> **Status:** Maintained research register  
> **Scope:** Open, active, and concluded Arcogine research questions that require evidence before architecture, product, or executable delivery planning should change  
> **Authority:** Research only; this directory does not describe current capability, accepted architecture, or committed delivery work

## 1. Purpose

`docs/research/` answers:

> **What do we still need to understand or decide?**

It is deliberately separate from `docs/planning/`, which answers:

> **Given what is currently known or decided, what executable work should be performed, in what order, and under what acceptance criteria?**

Research may confirm a hypothesis, falsify it, narrow it, discover that no action is required, justify an architecture decision, refine product direction, or create evidence for later delivery work. Recording a conclusion here does not by itself make that conclusion authoritative.

Durable outcomes must be reconciled into the narrowest appropriate authority:

```text
Research question
      |
      v
Evidence + conclusion
      |
      +--> no action
      +--> product clarification
      +--> architecture update
      +--> architecture decision
      +--> executable delivery work
                     |
                     v
                docs/planning/
```

Research discovers. Product and architecture establish durable direction. Planning sequences executable work.

## 2. Research lifecycle

Use these statuses consistently:

| Status | Meaning |
|---|---|
| **CANDIDATE** | Material uncertainty has been identified, but the investigation is not yet sufficiently bounded or timely to start |
| **READY** | The research question, scope, evidence expectations, and exit criteria are sufficiently clear to start |
| **ACTIVE** | Evidence gathering or synthesis is currently in progress |
| **CONCLUDED** | The investigation reached a decision-quality result and its durable consequences have been reconciled into the appropriate authority, or explicitly resulted in no action |
| **SUPERSEDED** | A later question, decision, or evidence base replaced the investigation before normal conclusion |

Do not use percentage completion for research. Progress is better represented by evidence gathered, hypotheses falsified, and exit criteria closed.

## 3. Boundary with delivery planning

Research artifacts must not become a shadow roadmap.

- Do not assign temporary delivery coordinates to research questions or research documents.
- Do not treat research priority as an implementation commitment.
- Do not introduce production code merely to make a research hypothesis look complete.
- If an investigation produces executable work, capture that work in `docs/planning/` only after the relevant product or architecture decision is sufficiently established.
- If an investigation changes durable semantics, reconcile the result into architecture or an architecture decision rather than leaving the research report as the only authority.
- If an investigation changes only a product hypothesis, reconcile it into the appropriate product or consumer-planning document.
- A conclusion of **no new abstraction / no implementation** is a successful research result when supported by the evidence.

Research documents may link to planning documents, architecture, implementation, standards, issues, and external sources as evidence. They should remain understandable without relying on transient delivery coordinates.

## 4. Maintenance policy

Maintain this register as research progresses:

1. Add a question when a material uncertainty is discovered and either bound it immediately or leave it `CANDIDATE`.
2. Mark a question `READY` only when its problem, scope, evidence expectations, and exit criteria are clear enough for an independent researcher to execute.
3. Mark a question `ACTIVE` when research actually starts; existence of a prompt or investigation document alone does not mean work is active.
4. When research concludes, record the concise verdict and link the durable destination of any resulting product, architecture, or planning change.
5. Keep concluded entries visible as a lightweight record of investigated uncertainty, but do not duplicate the authoritative conclusion here.
6. During periodic planning/consistency review, prune obsolete candidates, mark superseded work explicitly, and reprioritize based on current repository evidence.

## 5. Research register

Priority is portfolio guidance, not committed sequencing.

| Research question | Area | Priority | Status | Why now / what it unlocks | Detailed artifact or authority | Expected conclusion destination | Last reviewed |
|---|---|---:|---|---|---|---|---|
| What is the minimum durable semantic boundary among actor attribution, decision production, subject, capability, semantic operation, realization, and transition? | Cross-cutting | High | **READY** | Tests whether Arcogine needs any platform-level `Agent` meaning before generalized agent abstractions or ownership are introduced | [Agency and decision boundary](agency-decision-boundary.md) | Architecture and possibly an architecture decision; executable planning only if shared implementation responsibility survives | 2026-09-08 |
| What independently continuing operational history or partition needs durable identity, and what makes two records belong to the same one versus different ones? | Operational Execution / Digital Twin | **Critical-path** | **READY** | Resolves the architecture hold that blocks durable operational identity and downstream operational-history attribution | [ADR-0013](../architecture/decisions/0013-execution-context-identity.md) and [Operational readiness](../planning/operational-execution-digital-twin-readiness.md) | Revised architecture decision, then operational planning | 2026-09-08 |
| Does Factory Design need a reusable equipment/resource definition distinct from installed resource instances, and what semantic invariant would that distinction carry? | Factory Design | High | **READY** | Current `ResourceDefinition` intentionally collapses type and installed unit; the game catalogue, future industrial consumers, and capability modeling provide concrete pressure to test the deferred split | [Factory design capability](../planning/factory-design-capability.md), [Factory design architecture](../architecture/factory-design.md), and [ISA-95 mapping](../architecture/isa-95-semantic-mapping.md) | Factory Design architecture; planning only if the split or another decomposition is justified | 2026-09-08 |
| Does the proposed factory-design game loop make production-system optimization understandable, experimentally useful, and engaging? | Factory-Design Challenge / Game | High | **READY** | The first playable slice is explicitly a product hypothesis; challenge infrastructure can exist without proving the player loop | [Factory-design game vertical slice](../planning/factory-design-game-vertical-slice.md) | Consumer/product hypothesis and playtest plan; implementation changes only after evidence | 2026-09-08 |
| What player-facing evidence best exposes bottlenecks, queues, utilization, processing versus transfer loss, and attempt-to-attempt causal differences without exposing raw runtime internals? | Factory-Design Challenge / Game | High | **READY** | The vertical slice depends on players being able to explain why one design performs better, not merely observe a score | [Factory-design game vertical slice](../planning/factory-design-game-vertical-slice.md) and [runtime observation/event delivery](../planning/runtime-observation-event-delivery.md) | Consumer UX/product evidence; runtime planning only if a real supported-observation gap is demonstrated | 2026-09-08 |
| Which spatial and material-flow semantics, if any, should follow the current factory-model spatial policy: orientation, paths/aisles, conveyors, transport resources, connection points, congestion, or other concepts? | Factory Design / Engine | High | **CANDIDATE** | Current spatial semantics intentionally stop at authored placement/footprint and deterministic transfer interpretation; the next expansion should be consumer-driven rather than assumed | [Factory Model v2](../architecture/factory-model-v2.md), [Engine Semantics v1](../architecture/engine-semantics-v1.md), and [Factory design capability](../planning/factory-design-capability.md) | Factory/Engine architecture and only then delivery planning | 2026-09-08 |
| What design-authoring lifecycle is genuinely cross-consumer rather than editor-specific: incomplete drafts, alternatives, templates, undo/history, branching, collaboration, optimizer-generated candidates, and publication handoff? | Factory Design | Medium | **CANDIDATE** | Factory Design deliberately keeps authoring consumer-specific until multiple consumers justify shared lifecycle semantics | [Factory design architecture](../architecture/factory-design.md) and [Factory design capability](../planning/factory-design-capability.md) | Factory Design architecture or no shared abstraction | 2026-09-08 |
| What semantic comparison of factory-design alternatives is useful across consumers beyond the current initial comparison slice? | Factory Design / Governance | Medium | **CANDIDATE** | Finer comparison is explicitly consumer-triggered; game and industrial design may eventually require different explanatory depth | [Factory design capability](../planning/factory-design-capability.md) and [Governance capability](../planning/governance-conformance-capability.md) | Factory/Governance architecture and planning only for demonstrated shared needs | 2026-09-08 |
| What challenge, scoring, and level-construction structures create multiple understandable viable factory strategies without a dominant opaque meta or reward-function exploitation? | Factory-Design Challenge / Game | Medium | **CANDIDATE** | The reference challenge requires meaningful capacity/layout/cost trade-offs and at least two credible solutions; exact content parameters are intentionally subject to tuning | [Factory-design game vertical slice](../planning/factory-design-game-vertical-slice.md) | Consumer product/content design; no Arcogine semantic change unless evidence demands one | 2026-09-08 |
| What minimum actor, trust, authority, and capability semantics are shared between synthetic decision-makers and externally consequential operational actors? | Operational Execution / Cross-cutting | High | **CANDIDATE** | Operational architecture identifies the need, but the agency investigation should first clarify actor/controller/subject and capability boundaries so ownership is not duplicated | [Agency investigation](agency-decision-boundary.md) and [Operational readiness](../planning/operational-execution-digital-twin-readiness.md) | Cross-cutting/Operational architecture, then planning if a concrete shared contract survives | 2026-09-08 |

## 6. Current research focus

The strongest currently ready investigations are intentionally from different lanes:

1. **Durable operational identity** — actual architecture blocker for Operational Execution.
2. **Agency and decision boundary** — bounded cross-cutting investigation intended to prevent premature generalized agent abstractions.
3. **Factory equipment/resource ontology** — Factory Design domain-model investigation testing the deferred definition/installed-instance split.
4. **Factory-design game core loop and diagnostics** — product/game research testing whether the existing deterministic semantics become understandable and engaging to a player.

These may proceed independently where their evidence does not assume conclusions from another investigation. Research priority should be re-grounded against current repository state before starting a new investigation.
