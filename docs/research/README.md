# Research

> **Status:** Maintained research register  
> **Scope:** Open, active, and concluded Arcogine questions that require evidence before product, architecture, or executable delivery planning should change  
> **Authority:** Research only; this directory does not describe current capability, accepted architecture, or committed delivery work

This document owns the research **portfolio, lifecycle, and promotion policy**: what questions are open, at what priority and status, and when a conclusion is durable enough to reconcile. It does not define how an individual investigation is conducted, what makes a report decision-quality, or what "independent" and "adversarial" review require — that normative method lives in [`docs/development/researching.md`](../development/researching.md), executed by the repository's [Researcher](../../.github/agents/researcher.agent.md) role. Use [`report-template.md`](report-template.md) as the reusable structure for a decision-quality report.

[`synthesis-seeds.md`](synthesis-seeds.md) is a deliberately weaker surface: a non-authoritative cross-investigation signal index for evidence-bearing, potentially transferable observations whose recurrence may matter later. It is not part of the research portfolio or lifecycle, does not admit work, and must not be treated as accepted semantics or a publication backlog.

## Boundary

`docs/research/` answers:

> **What do we still need to understand or decide?**

`docs/planning/` answers:

> **Given what is already known or decided, what work can an implementer execute and validate?**

Research may confirm, falsify, or narrow a hypothesis; conclude that no change is needed; justify an architecture/product decision; or create evidence for later implementation. A research conclusion becomes durable only after it is reconciled into the appropriate product, architecture, ADR, reference, or implementation plan.

A synthesis seed answers a different and much weaker question:

> **What evidence-bearing signal is worth being able to rediscover if a later independent investigation encounters the same phenomenon?**

A seed therefore does not become an Arcogine research question unless a later deliberate admission decision creates or updates one in this register.

Research documents never receive temporary delivery coordinates.

## Lifecycle

| Status | Meaning |
|---|---|
| **CANDIDATE** | Material uncertainty exists, but the investigation is not yet sufficiently bounded or timely to start |
| **READY** | Question, scope, evidence expectations, and exit criteria are sufficiently clear to start |
| **ACTIVE** | Evidence gathering or synthesis is in progress |
| **CONCLUDED** | A decision-quality result exists and durable consequences have been reconciled, or the result was explicitly no action |
| **SUPERSEDED** | Later evidence/question/decision replaced the investigation before normal conclusion |

Do not use percentage completion. Track evidence, falsified hypotheses, and exit criteria instead.

These lifecycle values apply only to research questions in the register. Synthesis seeds have no lifecycle, priority, owner, delivery commitment, or percentage completion.

## Promotion rule

```text
Research
   |
   v
Decision-quality evidence
   |
   +--> no action
   +--> product clarification
   +--> architecture / ADR
   +--> concrete implementation responsibility
                       |
                       v
                  docs/planning/
```

A topic is ready for implementation planning only when semantic/product meaning, ownership, prerequisites, and acceptance evidence are sufficiently settled. A blocked implementation contract may live in planning; an unresolved question that still determines the contract stays here.

A synthesis seed is outside this promotion path. If recurrence later justifies a bounded cross-investigation question, that question must be admitted into this register through the normal research lifecycle before investigation begins.

## Evidence custody and retirement

Research that must survive a session uses a temporary, semantically named **research-evidence workspace branch** as defined by [`docs/development/researching.md`](../development/researching.md). The normal workspace unit is one bounded research question. An explicitly coupled set of questions may share one workspace only when they are intended to be reviewed/reconciled as one packet. Do not create a separate branch merely because a report and its adversarial review are different artifacts, do not create a fresh branch merely because that workspace has entered durable reconciliation, and do not create one permanent repository-wide research branch.

The same finite workspace normally carries the question from investigation through report/review artifacts and the final durable reconciliation. "Separate reconciliation" means a separate phase and authority transition, not separate Git topology. A new reconciliation branch is justified only by a concrete operational isolation requirement, not by the existence of an immutable report commit.

A workspace may contain checkpoints, drafts, diagnostic notes, completed reports, adversarial-review artifacts, and later reconciliation work. Branch-tip state is never the evidence identity: a completed report or review becomes a handoff artifact only when its exact commit SHA and path are explicitly returned. Those exact revisions are immutable inputs to later review/reconciliation even though later workspace commits may add other material.

Research workspaces provide reproducible cross-session custody only. Their contents are not current repository authority and are not merged to `main` merely because research exists.

When a workspace with handed-off evidence must catch up to `main`, preserve the exact evidence SHAs. Use a history-preserving merge-style update rather than rebasing or force-pushing away completed artifact commits. Before the reconciliation PR lands, temporary evidence/handoff files that are not deliberately promoted should be absent from the final tree; the PR should carry the durable transfer rather than an evidence archive.

A temporary research-evidence workspace may be retired only after every research question it carries is `CONCLUDED` or `SUPERSEDED` and the knowledge-transfer audit has accounted for every material result that should survive: accepted conclusions and qualifications in their durable authority, remaining unknowns/reopening triggers/new questions back in research, implementation responsibilities in admitted planning when ready, reusable proving cases/counterexamples/measurements/know-how in the surface that will need them, qualifying cross-investigation synthesis seeds in [`synthesis-seeds.md`](synthesis-seeds.md), and explicit discard decisions for findings that no longer merit retention. If any material item remains unaccounted for, keep the workspace available.

Synthesis-seed retention is intentionally stricter than "interesting." The signal must be evidence-bearing, remain intelligible outside its immediate Arcogine implementation context, and be loss-sensitive enough that retiring the workspace would materially reduce the chance of recognizing recurrence later. The seed carries only the compact signal, evidence posture, boundaries, durable reusable assets, provenance, and a concrete revisit trigger; it does not preserve the whole report.

The reconciliation that completes that audit owns the retirement decision. Once the reconciliation has landed, deleting the listed temporary workspace branch is immediate post-merge cleanup; branch deletion is not itself part of repository authority and must not happen before the reviewed transfer is durable.

This custody rule does not add a lifecycle status, permanent report archive, second research roadmap, or second research ledger. The research register continues to track questions and lifecycle; `synthesis-seeds.md` records only low-authority recurrence signals and cannot by itself create work.

## Research register

Priority is portfolio guidance, not delivery commitment.

| Research question | Area | Priority | Status | Detailed artifact / authority | Expected destination | Last reviewed |
|---|---|---:|---|---|---|---|
| What is the minimum durable boundary among actor attribution, decision production, subject, capability, semantic operation, realization, and transition? | Cross-cutting | High | **READY** | [Agency and decision boundary](agency-decision-boundary.md) | Architecture/ADR if shared semantics survive; planning only for concrete implementation | 2026-09-08 |
| What independently continuing operational history, if any, needs durable identity, and what equality/continuity/fork rules define it? | Operational / Digital Twin | **Critical-path** | **READY** | [Operational boundary research](operational-execution-digital-twin-boundaries.md) and ADR-0013 | Revised ADR/architecture, then the first narrow Operational implementation slice | 2026-09-08 |
| What other Operational boundaries are required for actor/trust/authority, external operation realization, correspondence, reconciliation, drift, and resilience? | Operational / Cross-cutting | High | **CANDIDATE** | [Operational boundary research](operational-execution-digital-twin-boundaries.md) | Architecture and then implementation planning only as needed | 2026-09-08 |
| Does Factory Design need a reusable equipment/resource definition distinct from installed resource instances, and what invariant would that distinction carry? | Factory Design | High | **CONCLUDED** | [Factory resource semantics report](factory-resource-semantics.md) | **Keep collapsed for now**; [Factory Resource Semantics](../architecture/factory-resource-semantics.md) records the durable interpretation, no implementation slice | 2026-09-08 |
| When does Arcogine need qualified operation-resource applicability and resource-dependent performance beyond explicit eligible IDs and step-level duration? | Factory Design / Engine | High | **CANDIDATE** | [Factory Design evolution research](factory-design-evolution.md) | Factory/Engine architecture and implementation only after a concrete heterogeneous-resource consumer proves the gap | 2026-09-08 |
| Which richer validation, comparison, resource-pool, shared-draft, or governed-change semantics are genuinely cross-consumer? | Factory Design | Medium | **CANDIDATE** | [Factory Design evolution research](factory-design-evolution.md) | Factory/Governance architecture or no shared abstraction | 2026-09-08 |
| Does the proposed factory-design game loop make production-system optimization understandable, experimentally useful, and engaging? | Game / Challenge consumer | High | **READY** | [Factory-design game vertical-slice research](factory-design-game-vertical-slice.md) | Bounded product requirement set, then game consumer implementation planning | 2026-09-08 |
| What player-facing evidence best exposes bottlenecks and causal performance differences? | Game / Engine consumer | High | **READY** | [Factory-design game vertical-slice research](factory-design-game-vertical-slice.md) | Game UX; Engine change only if a real supported-observation gap is proven | 2026-09-08 |
| What scoring, challenge, and level structures create several understandable viable strategies without an opaque dominant meta? | Game | Medium | **CANDIDATE** | [Factory-design game vertical-slice research](factory-design-game-vertical-slice.md) | Product/content decision | 2026-09-08 |
| Which spatial/material-flow semantics should follow current placement/footprint and deterministic transfer semantics? | Factory Design / Engine | High | **CANDIDATE** | [Factory Design evolution research](factory-design-evolution.md) plus current spatial architecture | Factory/Engine architecture | 2026-09-08 |
| When should Arcogine evolve beyond its current deterministic dispatch policy? | Engine | Medium | **CONCLUDED** | [Engine evolution research](engine-evolution.md) | **Do not add a general policy menu/scheduler now**; keep current versioned behavior as the implementation baseline while narrower first-release questions and same-semantics performance work are separated | 2026-09-11 |
| Should the first supported Engine release retain `engine-semantics:v1`'s at-most-one-local-job-per-cascade-trigger rule, or does the evidence justify a differently versioned bounded admission rule? | Engine | **Critical-path** | **CONCLUDED** | [Engine evolution research](engine-evolution.md) | **Retain v1 unchanged**; [Spatial Runtime Consequences](../planning/spatial-runtime-consequences.md) owns executable conformance for the retained recovery/local-admission behavior; reopen only at the documented recovery/spatial triggers | 2026-09-12 |
| Should the first supported Engine release retain v1's exact `combinedQueueDepth` ranking when shared pending jobs overlap several candidate machines, or does the evidence justify a differently versioned ranking term? | Engine | **Critical-path** | **CONCLUDED** | [Engine evolution research](engine-evolution.md) | **Retain v1 unchanged**; [Spatial Runtime Consequences](../planning/spatial-runtime-consequences.md) owns executable conformance for exact ranking/reselection behavior; reopen only at the documented objective/spatial/local-admission triggers | 2026-09-12 |
| What concrete supported-consumer objective would justify queue sequencing beyond FIFO, and what fairness/information-horizon constraints must accompany it? | Engine | Medium | **CANDIDATE** | [Engine evolution research](engine-evolution.md) | Engine semantics only after a consumer defines what "better" means | 2026-09-11 |
| When are lot/batch/material-lot semantics or capability/resource-pool semantics justified beyond current unit-work decomposition and explicit eligible instances? | Engine | Medium | **CANDIDATE** | [Engine evolution research](engine-evolution.md) | Engine/Factory architecture and versioned implementation only after a concrete domain/consumer requirement | 2026-09-11 |

## Maintenance

- Add a material unknown instead of hiding it in an implementation plan.
- Mark research `READY` only when an independent researcher can execute it from the stated evidence/exit criteria — see `docs/development/researching.md` for what a sufficiently bounded brief and decision-quality report require.
- When research concludes, record the verdict here and link the durable destination; do not duplicate the authoritative conclusion.
- Before retiring a temporary research-evidence workspace, perform the knowledge-transfer audit above; branch deletion is not a substitute for deciding what should survive.
- During reconciliation, admit a synthesis seed only under `docs/development/researching.md`'s evidence-bearing / transferable / loss-sensitive criteria; compare with existing seeds after the originating investigation has reached its own result, so similarity is not manufactured by anchoring.
- During planning/consistency review, flag exploratory content that has leaked back into `docs/planning/` and relocate it here.
- Keep concluded research visible when it provides useful history, but prefer current architecture/reference for durable semantics.
