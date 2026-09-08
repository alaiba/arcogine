# Agency and Decision Boundary Investigation

> **Status:** Proposed bounded architecture investigation  
> **Scope:** Determine the smallest durable semantic boundary between observation, decision production, actor attribution, capability, semantic operation, and resulting state change  
> **Authority:** Research only; this document does not establish a new platform abstraction, module owner, delivery track, or current product capability  
> **Related:** [Product Charter](../product/charter.md), [Architecture Overview](../architecture/overview.md), [Operational Execution and Digital Twin Architecture](../architecture/operational-execution-digital-twin.md), [Standards Alignment](../architecture/standards-alignment.md)

## 1. Why this investigation exists

Arcogine already has several relevant architectural constraints:

- authoritative state is mutated only through its owning subsystem and explicit transition mechanisms;
- purpose-specific observations are the supported input boundary for decision-making code;
- current simulation decision-making follows `Observation -> Decision -> Event`;
- humans and autonomous decision-makers should ultimately participate through explicit capabilities, authority, constraints, and accountable decisions;
- actor/action/capability semantics may be needed in synthetic execution as well as externally consequential execution;
- a requested semantic operation, its realization, the resulting transition, an observation of that transition, and later reconciliation are distinct facts.

What is not yet established is the semantic boundary between **who or what is attributable for an action** and **how a choice was produced**.

The current `SalesAgent` is one concrete decision-maker. It observes an `AgentObservation`, applies a deterministic pricing rule, and schedules events. That implementation is useful evidence, but it is not sufficient evidence for a generalized `Agent` abstraction.

This investigation asks whether Arcogine needs a platform-level agent concept at all, or whether agency is better represented as a composition of smaller concepts that also fit humans, organizations, external systems, NPCs, optimizers, planners, learned policies, and other decision sources.

## 2. Working semantic shape

The investigation should test a conceptual shape equivalent to:

```text
Observation
    |
    v
Decision source / controller
    |
    v
Decision / intent
    |
    v
Actor exercises Capability
    |
    v
Semantic Operation
    |
    v
Realization
    |
    v
Transition
```

These names are hypotheses, not proposed Java types.

The important distinctions under test are:

```text
Actor
    who or what is attributable for participation/action

Decision source / controller
    how a choice is produced

Subject / body
    the modeled or external thing acted on, represented, or controlled

Capability
    what semantic operations an actor may exercise under applicable policy

Operation
    the semantic requested change
```

Actor, decision source/controller, and subject/body must remain independently representable **semantic roles**. The investigation must not collapse them into one concept or assume identity equality between them. It also must not require their identities to be unequal: one entity may legitimately occupy more than one role in a concrete case.

The proving cases therefore need to establish when separate identities and explicit relationships are required versus when one identity may occupy several roles without losing the semantic distinctions. A human acting directly may be both actor and decision source; an autonomous service may act for itself; an embodied simulated participant may combine some roles while still requiring the roles to be distinguishable for reasoning and provenance.

A human supervisor, organization, autonomous controller, NPC, service, or external system may all be legitimate actors. A decision source may be a human, deterministic rule, process engine, optimizer, planner, behavior controller, learned policy, or another mechanism. One decision source may serve many actors; one actor may change controllers over time or combine several decision sources.

## 3. Hypotheses to falsify

### H1. `Agent` may not be a platform primitive

Do not introduce a generalized `Agent` interface, base class, ontology, or module merely because the current implementation contains `SalesAgent`.

Test whether the proving cases can instead be represented through a smaller composition such as:

```text
Actor
Observation
Decision
Decision-source identity/provenance
Capability
Operation
Subject
```

If a common `Agent` concept remains necessary after the proving cases, the investigation must state exactly which invariant cannot be expressed through the smaller concepts.

### H2. Decision-source internals are not common world semantics

Arcogine should care about the externally meaningful boundary of a decision, not automatically about the mechanism's internal cognitive or algorithmic representation.

Unless a proving case demonstrates otherwise, concepts such as the following remain decision-source implementation details rather than shared platform ontology:

```text
goal
belief
memory
plan
personality
prompt
behavior-tree state
planner search state
model weights
hidden reasoning trace
```

Different decision architectures may use these concepts internally without requiring other domains to understand them.

### H3. Actor attribution survives controller replacement

An actor's attributable identity should not automatically change merely because its decision source or policy implementation changes.

The investigation should test:

- one policy/controller serving many actor instances;
- one actor switching policies or controllers;
- human -> automated -> human handoff;
- delegation from one actor or principal to another;
- an organization acting through several people and systems;
- several decision mechanisms contributing to one attributable decision;
- one identity legitimately occupying more than one semantic role without erasing the role distinctions.

The outcome should clarify which identities belong in causal provenance when these relationships are not one-to-one and when role co-occupancy does or does not require separate identity concepts.

### H4. Replay of a decision is not the same as re-executing its source

Deterministic decision sources may be reproducible from the same observation and policy version. Humans, external organizations, stochastic learned policies, remote services, and model-based controllers may not be.

The investigation must preserve these distinct operations:

```text
re-execute the decision source
!= replay the recorded decision
!= replay the requested operation
!= replay the resulting transition
```

It should determine which of these Arcogine needs for simulation reproducibility, causal reconstruction, verification, audit, and forked experimentation.

A working provenance hypothesis is that accountable decisions may need enough durable information to identify, when applicable:

- actor identity;
- decision-source or policy identity and material version;
- input observation/provenance boundary;
- selected decision or requested operation;
- capability/authority evaluation relevant to exercising it;
- resulting operation/transition/outcome correlation.

This does **not** imply recording private human cognition, hidden model reasoning, planner search state, or other implementation internals.

### H5. A capability may be temporally extended

Do not assume every capability maps one-to-one to one primitive transition.

Test whether a decision-maker may legitimately exercise a higher-level capability such as:

```text
PerformChangeover
FulfilPurchaseOrder
RepairMachine
RebalanceProduction
```

whose realization expands into several lower-level operations and transitions over time.

The investigation should determine whether Arcogine needs to distinguish a primitive operation from a procedure/process/skill exposed as one callable capability, or whether existing process/domain composition is sufficient.

This question matters for organizations and external systems as much as for autonomous software: an external participant may expose one contractual operation while hiding its internal workflow.

### H6. Agent-specific communication is not assumed

Do not introduce a generic agent message bus, conversation ontology, or standardized inter-agent protocol unless a proving case demonstrates that ordinary domain operations, events, observations, and process interactions cannot represent the required interaction cleanly.

Existing standards remain adapter/reference candidates when concrete interoperability requires them; they do not define Arcogine's domain model by default.

## 4. Required proving cases

The investigation is not complete until one candidate boundary is tested against all of these cases.

### A. Current `SalesAgent`

Preserve current behavior without forcing the existing implementation to migrate merely to prove an abstraction.

Test whether the candidate semantics can describe:

- `AgentObservation` as its decision input;
- the deterministic pricing rule as its decision source/policy;
- the attributable actor, if one is needed;
- the resulting price-change decision/operation;
- current event-based realization.

### B. Human participant

Use a planner or supervisor who receives a purpose-specific observation and may approve, reject, reschedule, stop, or otherwise request an operation.

The model must not require Arcogine to represent the human's internal beliefs or reasoning. It should allow the same human identity to occupy actor and decision-source roles when that accurately describes the case, without collapsing those roles into one semantic concept.

### C. Organization or external-system participant

Use a BPMN-like participant boundary: an organization, ERP/MES, supplier system, or other external participant offers and consumes defined interactions while its internal implementation remains opaque.

Test whether actor identity, interface/capability, operation, and subject semantics are sufficient without pretending the participant is autonomous software.

### D. Embodied NPC or simulated worker

Use an actor with limited perception and an embodied modeled subject capable of operations such as move, pick/place, communicate, or perform work.

Its controller may use any game-AI technique internally. Navigation, animation, personality, memory, and controller-specific state must remain outside the common ontology unless the scenario proves otherwise.

This case specifically tests when actor, controller, and body/subject require separate identities and relationships versus when one entity may legitimately occupy multiple roles while those roles remain semantically distinguishable.

### E. Deterministic planner or optimizer

Use a dispatcher/planner that selects operations from authoritative observations and constraints.

Test deterministic re-execution, policy/version provenance, one-controller-to-many-actor relationships, and whether planning internals need any platform representation beyond the emitted decision.

### F. Nondeterministic or learned controller

Use a stochastic learned policy, model-based controller, or external nondeterministic decision service.

Test whether Arcogine can preserve attribution and replay useful system history without requiring the decision source to reproduce the same internal reasoning or output on demand.

## 5. Questions each proving case must answer

For every case, record answers to the same questions:

1. What is the attributable actor?
2. Is there a distinct principal or delegating actor?
3. What modeled or external subject is acted on or represented, if any?
4. What observation/input boundary is visible to the decision source?
5. What produces the decision?
6. Which identity/version of that decision source is materially relevant?
7. What capability permits the requested operation?
8. Is the operation primitive or temporally extended?
9. What realization turns the request into one or more transitions?
10. Which facts must be recorded to explain what happened and why?
11. Can the source be deterministically re-executed? If not, what should replay mean?
12. Does communication require anything beyond normal domain/process interactions?
13. Can the same actor change controllers without changing attributable identity?
14. Can one controller serve several actors without conflating their identities?
15. Can one identity occupy multiple semantic roles here, or do the roles require separate identities and explicit relationships?

## 6. Evaluation matrix

The final investigation result should compare the proving cases on at least these dimensions:

| Dimension | Question |
|---|---|
| Attribution | Who or what is accountable for the decision/action? |
| Observation | What information is available, and under whose authority? |
| Decision source | How is the choice produced, and is its identity/version material? |
| Subject | What modeled/external entity is represented or affected? |
| Capability | What may the actor do, to which subject, under which policy? |
| Operation granularity | Primitive request or temporally extended procedure? |
| Delegation | Can authority/controller responsibility move between actors? |
| Cardinality / role co-occupancy | Which relationships are one-to-one, one-to-many, or many-to-many, and may one identity occupy several roles? |
| Determinism | Can the decision source be re-executed reproducibly? |
| Replay | Re-run source, replay decision, replay operation, replay transition, or some combination? |
| Provenance | What must survive for causal reconstruction and verification? |
| Communication | Are ordinary operations/events sufficient? |
| Consumer-specific state | Which concepts must remain private to this decision architecture? |

## 7. Research inputs without repository knowledge duplication

External research may use agent and decision-system traditions as adversarial examples and implementation evidence, including deliberative agents, goal-oriented planning, behavior controllers, utility policies, optimization, multi-agent/holonic manufacturing, learned policies, tool-using model-based agents, process orchestration, and game AI.

The repository should not accumulate textbook summaries of those fields. Research output belongs here only when it changes or falsifies an Arcogine-specific hypothesis, boundary, proving case, or ownership decision.

## 8. Explicit non-goals

This investigation does **not** authorize:

- a new delivery track or track code;
- a generalized `Agent` framework or superclass;
- BDI, GOAP, behavior-tree, RL, LLM, or game-AI concepts in the common domain model;
- generic memory, belief, goal, plan, personality, prompt, or reasoning-trace semantics;
- an agent message bus or conversation protocol;
- a new authorization model competing with the actor/capability work already identified by Operational architecture;
- assuming `actor == controller`, `actor == subject`, or `controller == subject`;
- assuming actor, controller, and subject must always have different identities;
- moving shared actor/capability concepts into Operational solely because Operational has an early concrete consumer;
- production implementation of NPCs, learned agents, model-based agents, or external autonomous control.

## 9. Exit criteria

The investigation is complete when it produces a concise recommendation that:

1. applies one candidate semantic boundary to all six proving cases;
2. identifies which concepts are genuinely shared Arcogine semantics and which remain consumer/decision-source internals;
3. resolves whether `Agent` deserves a platform-level meaning or remains an application composition/label;
4. defines the minimum provenance/replay distinction required for deterministic and nondeterministic decision sources;
5. resolves whether temporally extended capabilities require a shared concept;
6. states whether any communication abstraction is actually needed;
7. identifies the narrowest appropriate ownership for any surviving shared concepts;
8. states whether the result warrants a distinct delivery track, belongs to existing owners, or requires no new implementation work.

Only surviving, cross-case invariants should be promoted into maintained architecture. Use an ADR only if the result introduces a genuinely architectural or hard-to-reverse decision.

## 10. Track-creation test

Do not create an Agency/Agentic delivery track merely because the investigation is cross-cutting.

A distinct track is justified only if the proving cases reveal a coherent implementation responsibility with durable shared semantics, multiple real consumers, and work that cannot be owned cleanly by the existing Engine, Governance, Operational, Factory Design, or Challenge boundaries without duplication or dependency inversion.

If the result is instead a small shared contract plus consumer-specific decision implementations, preserve that smaller architecture rather than manufacturing a new platform subsystem.
