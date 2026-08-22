# Arcogine Product Charter

> Product vision, system thesis, and enduring principles for Arcogine.

## What this document is

This is the single normative source for Arcogine's enduring product direction. It sits above UX decisions, domain design, architecture, ADRs, API and UI design, implementation plans, and current-state documentation — those all operate under it, and a proposal that contradicts this charter should be treated as the thing that needs to change, not the charter.

This document is **not**:

- a roadmap;
- an implementation plan;
- a list of promised features;
- a description of current functionality;
- marketing copy;
- an architecture specification.

It defines what Arcogine is ultimately intended to become and the principles against which future initiatives — features, refactors, architecture proposals, ADRs — should be evaluated. It does not promise dates, phases, or that any specific capability will ship. For what Arcogine implements today, see [`docs/architecture.md`](docs/architecture.md), [`docs/concepts.md`](docs/concepts.md), and [`docs/api.md`](docs/api.md).

## 1. Purpose

**Arcogine provides purpose-built ways to design, understand, simulate, verify, operate, monitor, and improve a production system, all grounded in the same executable model of the business.**

Arcogine should ultimately support the full lifecycle of a production business: designing the system, understanding it, simulating it, verifying it, implementing and deploying changes, executing real operations, monitoring actual behavior, and improving the system continuously.

Simulation is a major Arcogine capability, but **Arcogine is not fundamentally a simulation product**. The mature platform may operate as a modeling and design environment, a simulation engine, a digital twin, a verification environment, a decision-support system, an execution engine, an operational control surface, and a monitoring and analysis environment. These are modes of engagement with the same underlying business model, not necessarily separate applications.

## 2. Central system thesis

> **The system you design is the system you simulate is the system you verify is the system you execute is the system you monitor.**

This does not mean one runtime instance, one database, one UI, one deployment, one copy of mutable state, or Arcogine being the authoritative source of every datum in the business. Simulation, replay, staging, and production may have different state, clocks, authority, permissions, side effects, safety constraints, and external integrations.

What must remain continuous is the **semantic model of the business** — its lineage, policies, objectives, constraints, and executable meaning. External systems such as ERP, MES, CRM, financial systems, equipment, and sensors may continue to own authoritative external facts while participating in the Arcogine model; Arcogine does not need to become the source of truth for everything it touches.

## 3. Business-change thesis

> **A change to the business should be designable, testable against a faithful digital twin, verifiable against explicit objectives and constraints, and deployable to reality without translating it into an entirely different representation along the way.**

Arcogine should resist architectures where one representation is used for design, another is manually recreated for simulation, another is used for operations, and yet another is used for monitoring. The value proposition is **semantic and executable continuity across the lifecycle** — not four disconnected tools that happen to share a brand.

## 4. Product lifecycle

The mature Arcogine lifecycle, conceptually:

```text
Design
  ↓
Understand
  ↓
Simulate
  ↓
Verify
  ↓
Deploy
  ↓
Execute
  ↓
Monitor
  ↓
Improve
  └──────────────→ Design
```

This is a conceptual lifecycle, not a roadmap or a prescribed sequence of product releases. Users may enter at different points and move between stages as needed. An operating production system may be cloned into simulation to investigate a problem; validated changes may later be applied to production; actual outcomes may feed back into model improvement. No stage requires having built all the others first, and nothing here commits Arcogine to shipping these stages in this order or on any schedule.

## 5. Modes of engagement, not personas

Arcogine will serve many users with different responsibilities. The product is not built around one persona or one universal dashboard — it is built around **modes of engagement with the same system**.

| Mode | Primary activity | Example users |
|---|---|---|
| **Design** | Define products, processes, resources, policies, constraints, and objectives | Industrial engineers, process designers, business architects |
| **Understand** | Inspect behavior, dependencies, causes, current state, or history | Analysts, managers, auditors |
| **Simulate** | Explore scenarios, interventions, uncertainty, and alternative policies | Analysts, researchers, planners, agent developers |
| **Verify** | Establish whether a model, configuration, policy, or proposed change satisfies explicit requirements | Engineers, risk owners, compliance users, testers |
| **Operate** | Execute and coordinate actual production and business processes | Operators, planners, supervisors, autonomous agents |
| **Improve** | Compare intended and actual behavior and develop validated improvements | Engineers, managers, continuous-improvement teams |

These modes are not required to become six applications.

> **Roles do not receive separate versions of the business. They receive purpose-built projections and capabilities over the same model.**

UX complexity, permissions, information density, and available actions should follow a user's task and authority, not a fork in the underlying model.

## 6. Enduring product principles

**One model, many views.** Purpose-built experiences should be projections over a coherent business model rather than independently maintained representations.

**Lifecycle continuity.** Design, simulation, verification, execution, monitoring, and improvement should remain connected stages of one system lifecycle, not separately-maintained tools that happen to share a name.

**Reality is explicit.** Simulation, replay, staging, hypothetical branches, and live production must never be ambiguously presented or confused. A user or agent should always be able to tell which one they are looking at.

**Semantics survive deployment.** A validated model or policy should not need to be manually translated into an unrelated representation before it can govern reality.

**Causality and provenance.** Arcogine should make it possible to understand what happened, why, under which model or configuration, based on what observations, because of which decision, and by which human, policy, agent, or external authority.

**Humans and agents participate in the same governance model.** Human users and autonomous decision-makers should ultimately act through explicit capabilities, authority, constraints, and accountable decisions — neither is a special case exempt from the other's rules.

**Safety scales with consequence.** Exploration in simulation can be permissive. Actions affecting real production require proportionate controls around authority, validation, approval, failure handling, auditability, and reversibility. This charter does not prescribe the concrete mechanisms — that is architecture and implementation work, done later, under this principle.

**Complexity follows the task.** An operator should not need to understand the simulation kernel. A model designer may need deep access to system semantics. Expose complexity according to task and authority, not uniformly to everyone.

**Reality improves the model.** Monitoring is not merely dashboarding. Observed behavior should make it possible to compare reality with modeled expectations, detect divergence, validate assumptions, and improve the model.

## 7. Architectural implications

These are consequences that follow from the thesis above, stated at the conceptual level only — none of this is a module design, schema, API, or class, and none of it is an implementation commitment:

- Arcogine should not evolve separate simulation-only and production-only domain semantics.
- Multiple execution contexts (simulation, replay, staging, production) must be distinguishable without losing model continuity.
- Model, version, and provenance concepts become fundamental once changes can move from design to reality.
- Once Arcogine can affect real systems, requested actions, accepted actions, resulting facts, failures, and authority boundaries cannot be conceptually collapsed into one another.
- Purpose-specific observations and capabilities are preferable to exposing unrestricted mutable state.
- Modeled state, observed external reality, and any reconciled digital-twin state are conceptually distinct.
- Real execution introduces safety, authorization, auditability, failure, and operational consequence as architectural concerns, not optional add-ons.
- Integrating external systems must not require pretending Arcogine is the physical source of truth for every domain it touches.

## 8. Continuity with current architecture

Arcogine's existing architectural philosophy —

```text
Events mutate State.
State produces Observations.
Observations inform Decisions.
Decisions produce Events.
```

— is broadly compatible with this charter and is expected to remain a durable pattern as the system grows: explicit events, single-owner state, purpose-specific observations, and accountable decisions are exactly the shape a system needs to keep design, simulation, verification, and execution semantically continuous. Existing ideas the current implementation already applies — authoritative state ownership per subsystem, immutable observations, explicit decisions, deterministic simulation, provenance via an event log, the commercial/operational/financial truth distinction, and controlled agent capabilities — remain valuable and are not discarded by this charter. See [`docs/architecture.md`](docs/architecture.md) for how they work today; this charter does not freeze their current implementation as permanent, only affirms the direction they point in.

One distinction matters enough to state explicitly: **determinism is a critical property of simulation, replay, and verification contexts — it is not a requirement that real-world execution itself somehow become deterministic.** Production operates in a non-deterministic world with real machines, real people, and real failures. What must stay continuous across contexts is the semantic model, not a claim that reality is repeatable the way a seeded simulation is. Language that treats simulation semantics as synonymous with Arcogine's entire future runtime model should be corrected wherever it appears.

## 9. What Arcogine is not

Arcogine is not fundamentally: a factory dashboard; a generic BI tool; only a discrete-event simulator; a game; an ERP clone; an MES clone; a collection of unrelated digital-twin integrations; or a separate model maintained per lifecycle stage.

Arcogine is also not defined by: its current UI; Java; Spring; React; TOML scenarios; its current API; its current module names; or its current single-user, local-first deployment model. Those are implementation and current-state choices, evaluated and possibly changed over time — not product identity.

Older ambitions such as "serious games" for training or "MMO-scale economic simulations" may remain possible applications built on the underlying engine, but they do not compete with the production-system/business-lifecycle thesis in Sections 1–3 as Arcogine's primary product identity.

## 10. Documentation authority model

```text
PRODUCT_CHARTER.md
Normative product destination and enduring principles
        │
        ▼
docs/architecture.md
Current architecture and enduring architectural principles
        │
        ├── docs/decisions/
        │   Historical rationale for significant decisions
        │
        ├── docs/concepts.md / docs/api.md / UI docs
        │   Current capability/reference documentation
        │
        └── devel/ and historical plans
            Temporary analysis, proposals, assessments, and past plans
```

Repository documentation carries one of four statuses, applied where it materially helps a reader avoid confusing them:

- **Normative** — enduring direction and principles. This charter, and the enduring-principle portions of `docs/architecture.md`.
- **Current state** — what Arcogine implements now. `docs/concepts.md`, `docs/api.md`, `ui/README.md`, the current-implementation portions of `docs/architecture.md`.
- **Proposed** — under consideration; not established. Proposed ADRs, `devel/` analyses.
- **Historical** — retained for context but no longer authoritative. Superseded ADRs, past plans such as `docs/java-rewrite-plan.md`.

A reader should never be left guessing whether a statement is mature product ambition, current implementation, an open proposal, or a historical artifact.

## 11. Decision test

A significant initiative — a feature proposal, an architecture change, an ADR — can be evaluated against this charter by asking:

1. Does this strengthen or fragment the common executable model?
2. Does it preserve continuity between design, simulation, verification, and execution?
3. Does it make the distinction between hypothetical and real state clearer, or blur it?
4. Does it preserve causality and provenance?
5. Can multiple user roles interact through purpose-specific views without inventing competing business truths?
6. Does it preserve a path from validated change to actual execution without manual semantic translation?
7. Does it introduce authority or operational consequences that require explicit governance?
8. Is it solving a real product need, or merely expanding Arcogine into an adjacent category?

An initiative does not need a "yes" on every question to proceed — some are in tension by nature (e.g. exposing more capability vs. minimizing authority surface). The test is meant to surface the tradeoff explicitly, not to produce an automatic verdict.
