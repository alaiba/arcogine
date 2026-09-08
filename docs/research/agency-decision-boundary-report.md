# Arcogine — Agency and Decision Boundary Investigation

**Research report · read-only architecture analysis · no repository changes made**
Baseline: `alaiba/arcogine` `main` @ `2acee74723aad7a9ca384459f5d44ef35b85b6fd`

Evidence in this report is labelled **REPOSITORY FACT (RF)**, **EXTERNAL EVIDENCE (EE)**,
**ARCHITECTURAL INFERENCE (AI)**, or **RECOMMENDATION**.

---

## 1. Executive conclusion

**Should `Agent` be a platform-level Arcogine concept? No.**

Every proving case is representable without it, and two of them are jointly unrepresentable *with*
it: a human planner is necessarily both the actor and the decision source, while a dispatcher is
necessarily neither-both — it decides for many actors that remain separately accountable. No single
concept that fixes the actor↔controller relation satisfies both. Four unrelated traditions —
provenance (W3C PROV), process and decision modelling (BPMN/DMN), game engines (Unreal), and
identity delegation (RFC 8693) — independently reached the same decomposition and none of them
needed an `Agent` type. The holonic-manufacturing reference architecture, after thirty years,
explicitly "refrains from decision-making" rather than reifying it.

**But the investigation's framing needs one correction.** Arcogine's problem was never a missing
`Agent`. RF: today a human `POST /api/price` and `SalesAgent` produce **structurally identical**
`PriceChange` events; the only way to tell them apart in the event log is the incidental presence of
an adjacent `AgentDecision(String)` narrative. Arcogine already achieved the human/software symmetry
the Charter asks for, with no agent ontology at all. **What is missing is attribution on the
operation** — and the repository has already reinvented it three times as free-form strings
(`RevisionRecorder(source, subject)`, `ChangeProvenance.source`, `AgentDecision(String)`).

**Which concepts should be shared Arcogine semantics.** Seven, of which only two are genuinely new
work:

1. **Actor** — the identity bearing responsibility for a participation. *New: one value type.*
2. **On-behalf-of** — delegation in which the delegator retains responsibility.
3. **Role** — the function served in *one participation*; a property of the participation, not of
   the identity. This is what makes role co-occupancy expressible without duplicate identities.
4. **Decision-source reference** — identity + *resolved, immutable* material version, deliberately
   uninterpreted. *Already implemented three times, per-domain; must NOT be extracted.*
5. **Observation reference** — already available as `(runId, latestEventSequence)`.
6. **Authority determination** — **relational** (actor × subject × operation), not an actor-owned
   permission list. *Already scheduled as PLAN-OPS-2.*
7. **Subject reference** — already implemented as `AffectedEntityRef`.

And one structural correction to the repository's working shape: **a decision and the operation it
requests are one durable fact, not two.** What is durable is an *attributed operation request*;
"decision" names the act that produced it and is carried by its attribution. This is what keeps the
answer small — one durable record and one new shared value type, rather than a seven-concept
framework.

**Which concepts stay decision-source internals.** All of them: goal, belief, memory, plan,
personality, prompt, behaviour-tree state, planner search state, model weights, reasoning trace.
This is not a preference. Mentalistic semantics are unverifiable from outside — the reason FIPA's
message envelope reached Standard while its communicative-act semantics never left Experimental —
and recorded reasoning traces are demonstrably capable of misrepresenting the actual cause, so
promoting them to world semantics would manufacture **false provenance**.

**Is any new module or delivery track justified? No — and the repository's own track-creation test
says so.** The only genuinely cross-cutting new thing is an actor-reference value type, which
belongs in `:types` beside `ControlledRevisionId`, `ModelFingerprint` and `RunId`. Authority already
has a scheduled owner (PLAN-OPS-2). Decision-source versioning must stay per-domain: extracting it
would force `challenge` — which deliberately depends on nothing — to acquire a dependency, breaking
an existing ownership boundary to serve a pattern that is working.

**What should happen now: almost nothing.** Arcogine is single-user, has no authentication, and has
one automated decision-maker; the attribution ambiguity has zero current consequence. The correct
actions are to record this boundary so PLAN-OPS-2 consumes it instead of re-deriving it under
operational pressure, to disambiguate "capability" (five in-repo meanings, three incompatible
external ones) before implementing it anywhere, and to stop adding stringly-typed attribution. No
ADR yet: this investigation introduces no hard-to-reverse decision. The ADR belongs at the moment
PLAN-OPS-2 commits to the actor/authority contract.

---

## 2. Repository baseline

**Exact SHA.** `2acee74723aad7a9ca384459f5d44ef35b85b6fd` (`HEAD` == `origin/main`, clean tree).

### Relevant existing architecture and contracts

- **Events–State–Observations–Decisions** (`overview.md`) is an architectural invariant, not a
  heuristic. Charter §8 keeps it open to evolution but requires its properties.
- **Charter §6**: "Humans and agents participate in the same governance model... neither is a
  special case exempt from the other's rules"; and Arcogine must explain "what happened, why, under
  which model or configuration, based on what observations, because of which decision, and by which
  human, policy, agent, or external authority."
- **`operational-execution-digital-twin.md` §4** already establishes
  `requested operation ≠ accepted command ≠ actual transition ≠ observation of transition ≠
  reconciled interpretation`; **§5** already poses "May actor A perform operation X on subject S
  under the applicable policy?" and states that **ownership of reusable actor/capability semantics
  is open and must not be forced into Operational**; **§10** already separates seek, replay,
  checkpoint/restore and fork.
- **ADR-0011** (Accepted): Arcogine is **not event sourced** — the supported model is fresh
  observation plus ordered deltas; `EventLog` is explicitly not a durable journal; and §4 requires
  that `RunId` "must never participate in simulation decisions... or any other deterministic
  outcome" — the governing precedent for any new identity.
- **ADR-0015** (Accepted): durable reproducibility inputs are `ModelFingerprint +
  EngineSemanticsVersion + explicit workload + seed/random inputs + **ordered external commands**`.
- **ADR-0013** (Proposed, on architecture-review hold): a durable operational-history identity is
  needed but its referent is unresolved; it explicitly must not collapse into **actor identity**.
- **ADR-0016** (Proposed): evidence provenance must distinguish modelled fact, Arcogine-derived
  analytical result, externally observed fact, and reconciliation-derived result — the same instinct
  applied to evidence rather than decisions. §7: "historical evidence validity does not require
  permanent re-executability."
- **PLAN-OPS-2** ("Actor, trust, authority, and capability") is already a scheduled, deliberately
  unowned slice.

### Current implementation evidence

- `SalesAgent` observes a pushed `AgentObservation`, applies a pure `decide()` over
  `SalesAgentConfig`, and schedules `PriceChange` + `AgentDecision(String)`. It has **no identity,
  no version, and no capability**; authority is ambient (`Scheduler.schedule` accepts any payload).
- `RuntimeEventEnvelope` carries four provenance dimensions (`runId`, `sequence`,
  `modelFingerprint`, optional `controlledRevisionId`) and **zero attribution**. `RuntimeEventType`
  is a taxonomy of state changes only — by ADR-0011 §3, intent structurally has no home in the
  supported stream.
- The "policy identity + material version + explicit cross-version incomparability" pattern is
  **already implemented three times independently** — `EngineSemanticsVersion` (Engine, ADR-0015),
  `EvaluationPolicyIdentity(id, version)` with `AttemptCompatibility` refusing incomparable
  comparisons (Challenge), and `RequirementVersion`/`AssertionVersion` (Governance) — and was never
  extracted into a shared type.
- `ChallengeAttempt` is a fourth history record that will need actor attribution as soon as there is
  more than one player, and has none.
- No authentication, principal, or user concept exists in production code; the only "operator"
  strings are governance test fixtures.
- **`EngineSemanticsVersion` is required by Accepted ADR-0015 §10 but has zero occurrences in
  `product/`** — an accepted-but-unimplemented gap, surfaced incidentally.

### Repository hypotheses under test

H1 `Agent` may not be a platform primitive · H2 decision-source internals are not common world
semantics · H3 actor attribution survives controller replacement · H4 replaying a decision is not
re-executing its source · H5 a capability may be temporally extended · H6 agent-specific
communication is not assumed. Verdicts in §12.
## 3. External evidence

Only sources that changed or could have changed the Arcogine decision. Each entry says what it
teaches and which hypothesis it bears on. Access caveats are stated where they exist.

### Attribution and provenance

**W3C PROV-DM / PROV-O** (W3C Recommendation, 30 April 2013) — https://www.w3.org/TR/prov-dm/
*Concept:* Entity / Activity / **Agent**, where "an agent is something that bears some form of
responsibility for an activity taking place, for the existence of an entity, or for another agent's
activity", with subtypes `prov:Person`, `prov:Organization`, `prov:SoftwareAgent`.
`wasAssociatedWith` carries an **optional** plan; `actedOnBehalfOf` is delegation; `hadRole` on a
qualified association names the function served. `prov:Plan` is "a set of actions or steps intended
by one or more agents", and §2.2.1.2 states "There exist no prescriptive requirements on the nature
of plans, their representation, the actions or steps they consist of, or their intended goals."
*Teaches Arcogine:* the world-facing concept it needs is **responsibility, not agency** — and three
kinds cover all six proving cases. `hadPlan` is exactly "which decision source was relied on",
deliberately uninterpreted.
*Bearing:* **H1 refined** (the concept survives; the name should not), **H2 supported**,
**H3 supported**. *Limitation:* PROV is retrospective — it cannot express a requested-but-unrealized
operation, so it cannot be Arcogine's domain model, only a vocabulary donor and projection target.

**RFC 8693, OAuth 2.0 Token Exchange** (IETF Proposed Standard, January 2020)
*Concept:* §1.1 distinguishes **impersonation** ("A ... is indistinguishable from B") from
**delegation** ("principal A still has its own identity separate from B ... any actions taken are
being taken by A representing B"). §4.1's `act` claim identifies the current actor, `sub` the party
on whose behalf; nested `act` expresses a chain; and **"For the purpose of applying access control
policy, the consumer of a token MUST only consider ... the party identified as the current actor ...
Prior actors ... are informational only."**
*Teaches Arcogine:* a deployed, normative rule for the exact question the planning document asks —
**authorization consults the current actor; the delegation chain is provenance.**
*Bearing:* **H3 supported.**

**Hardy, "The Confused Deputy (or why capabilities might have been invented)"**, ACM SIGOPS
Operating Systems Review 22(4), 1988.
*Concept:* a privileged program induced to act on the wrong party's behalf because the authority
exercised was the program's ambient authority, not the requester's.
*Teaches Arcogine:* the actor/controller split is not merely tidy — conflating them makes an entire
bug class structurally available. RF: `Scheduler.schedule(Event)` is ambient authority today.
*Bearing:* **H3 supported, with a security-grounded reason.**

**Dennis & Van Horn**, CACM 9(3), 1966; **Miller, Yee & Shapiro, "Capability Myths Demolished"**, 2003.
*Concept:* a capability is an unforgeable reference that **fuses designation and authority**.
*Teaches Arcogine:* this is a *different relation* from the industrial "what a resource can do" and
from "what an actor may do under policy". *Bearing:* terminology hazard, not a hypothesis.

### Process, decision and industrial standards

**BPMN 2.0** (OMG formal/2011-01-03).
*Concepts:* §9.2 — "A `Pool` is NOT REQUIRED to contain a `Process`, i.e., it can be a 'black box'";
§10.2.3.1 — every Task subtype "inherits the attributes and model associations of `Activity`",
differentiated only by a type/implementation marker; a **User Task** is "a typical 'workflow' Task
where a human performer performs the Task with the assistance of a software application", a
**Manual Task** is "expected to be performed without the aid of any business process execution
engine or any application"; `ResourceRole`/`HumanPerformer` assign resources to activities by
expression; Call Activity/Sub-Process makes one callable activity whose realization is a process.
*Teaches Arcogine:* the mature process standard models human, manual, service, rule-engine and
message work as **the same kind of activity with different realization**, and models an autonomous
external organization by **refusing to model its internals**.
*Bearing:* **H1 supported** (no Agent category anywhere), **H5 supported** (Call Activity),
**H6 supported** (plain Message Flows across organizations).
*Absence worth noting:* BPMN's Performer names a **relationship, not a persistent identity**.

**DMN 1.5** (OMG).
*Concepts:* a **Decision** "denotes the act of determining an output from a number of inputs, using
decision logic"; **Input Data** "denotes information used as an input by one or more Decisions"; a
**Business Knowledge Model** "denotes a function encapsulating business knowledge, e.g., as business
rules, a decision table, or an analytic model"; a **Knowledge Source** "denotes an authority for a
Business Knowledge Model or Decision" — which "might be ... domain experts responsible for defining
or maintaining them, or source documents". The spec adds that "since Knowledge Sources and Authority
Requirements have no execution semantics their interpretation is necessarily vague."
*Teaches Arcogine:* decision / input / reusable decision logic / authority-over-the-logic are four
different things — and the most developed decision-modelling standard has **no actor at all**.
Attribution lives elsewhere. *Bearing:* **H1 supported** (decision modelling and attribution are
separately factored in practice); a terminology warning that DMN's "authority" is *epistemic
authority over the logic*, not *authorization to act*.

**Workflow Resource Patterns** (Russell, van der Aalst, ter Hofstede & Edmond, CAiSE 2005).
*Concept:* the offered/allocated/started work-item lifecycle, and **Pattern 27 Delegation** — "the
ability for a resource to allocate an unstarted work item previously allocated to it ... to another
resource".
*Teaches Arcogine:* accountability can be reassigned while the **work item's identity is unchanged**
— a normative vocabulary for exactly H3, and better than "agent".
*Bearing:* **H3 supported.**

**Industrie 4.0 Capabilities, Skills & Services (CSS)** (Plattform Industrie 4.0, "Information Model
for Capabilities, Skills & Services", November 2022), §8.1/§8.4/§8.8.
*Concept:* **Capability** (implementation-independent ability) / **Skill** (executable
implementation, whose behaviour "corresponds to a standardized state machine") / **Service**
(callable interface).
*Teaches Arcogine:* capability / realization / interface are three things, and the industrial answer
to "what can this participant do" is built **without an Agent concept**. The Skill state machine is
independent corroboration that a temporally extended ability is a *lifecycle*, not a new kind.
*Bearing:* **H1 supported, H5 supported.** *Caveat: a discussion paper, not a ratified standard.*

**OPC UA Part 3**: Method (§5.7.1, atomic call) vs Variable (§5.6.2, state).
*Teaches Arcogine:* calling an operation and reading state are separate primitives in industrial
information modelling — the same split as Arcogine's operations vs observations.

**ISA-95 / IEC 62264 — a partial disconfirmation, reported honestly.**
The expectation was that ISA-95 unifies human operators and machines as one Resource type with
different classes. Checked via the freely accessible OPC UA companion specification (OPC 10030;
the IEC 62264 text itself is paywalled and was **not** read directly), Personnel, Equipment and
Material appear as **three parallel but separate class hierarchies**, not one unified Resource
supertype. *Teaches Arcogine:* even industrial standards do **not** fully unify humans and machines
under one type — which weakens a naive "the standards already prove uniformity" argument while
*strengthening* the conclusion that no universal Agent/Resource supertype is warranted.

**OASIS XACML 3.0** §1.1.1 and RBAC (ANSI INCITS 359).
*Concept:* the (subject, action, resource, environment) decision request.
*Teaches Arcogine:* the classic triple already covers "may this actor request this operation on this
subject" — and carries an **exact terminology inversion**: XACML's *Subject* is the requester
(Arcogine's Actor); XACML's *Resource* is the thing acted upon (Arcogine's Subject).
*Caveat:* the RBAC "user may be a person or an automated agent" claim rests on Sandhu et al. (1996)
and secondary sources; the INCITS PDF could not be read directly.

### Agency, communication and control traditions

**Wooldridge, "Semantic Issues in the Verification of Agent Communication Languages"**, JAAMAS
3(1):9–31, 2000; **Singh, "A Social Semantics for Agent Communication Languages"**.
*Concept:* mentalistic ACL semantics presuppose sincerity conditions unverifiable from outside —
"the mental concepts cannot be verified without access to the internal construction of the agents",
therefore "a purely mentalistic semantics of an ACL cannot be a normative requirement".
*Directly verified from the specifications:* FIPA's *ACL Message Structure Specification* (SC00061G)
reached **Standard** status 2002-12-03, while the *Communicative Act Library* (XC00037H, 2001-08-10)
— which supplies the actual meaning, in modal belief operators such as `B_i(Done(a) → FP(a))` —
remained **Experimental**. The envelope stabilized; the mentalistic semantics did not.
*Bearing:* **H2 supported, H6 supported decisively.** Singh's constructive alternative — public
**commitments** rather than mental states — is the shape to reach for if Arcogine ever needs
cross-participant semantics.

**Valckenaers, "PROSA becomes ARTI"**, *Computers in Industry* 120:103226, 2020 (read directly, open
access). Building on Van Brussel et al., "Reference architecture for holonic manufacturing systems:
PROSA", *Computers in Industry* 37(3), 1998.
*Concept:* a 30-year retrospective by a PROSA co-author. The reference architecture explicitly
**"refrains from decision-making"**, treating decision-making as a pluggable, non-reified layer
rather than a shared base type — and the paper documents the field's own researchers *misclassifying*
their decision mechanism as part of the state/twin layer and later correcting it.
*Teaches Arcogine:* the most mature manufacturing-agent tradition concluded that the decision layer
should not be reified into the shared architecture — a documented instance of exactly the failure
mode Arcogine's Events–State–Observations–Decisions separation guards against.
*Bearing:* **H1 supported.** Useful term borrowed: PROSA's *advisory* (staff) vs *authoritative*
decision roles.

**Unreal Engine — Controller / Pawn / PlayerState.**
*Concept:* the body is an `APawn`; the decider is an `AController`, swappable at runtime between
`APlayerController` and `AAIController` via `Possess`/`UnPossess` and persisting across body
death/respawn; and the durable accountable identity — name, score, team — lives in a **third**,
network-replicated object, `APlayerState`, precisely because controllers are not replicated.
*Teaches Arcogine:* a production engine independently invented the **three-way** split of identity,
decider and body, and needed the identity to be a separate object *because* the decider is
swappable. *Bearing:* **H3 supported (strongest single source), H1 supported** (three independent
types, no unifying Agent class).

**ROS 2 / Nav2 and `ros2_control`.** Layered planner / controller / behaviour servers, plus a
controller-vs-hardware-interface split. Confirms decider/body separation in robotics; no
identity-object analogue was found (recorded as unexplored, not as absent).

**MOISE+ (Hübner, Sichman & Boissier) and OperA (Dignum).** Organizational models that separate
**role specification** from **role occupant** by construction. *Bearing:* **H3 supported.**

### Planning, temporal abstraction and control internals

**Sutton, Precup & Singh, "Between MDPs and semi-MDPs"**, *Artificial Intelligence* 112, 1999.
*Concept:* an option is (initiation set, policy, termination condition) and is **agent-side**; the
base environment receives one primitive action per step. Duration enters the *environment* model
only when its own dynamics are semi-Markov. *Bearing:* **H5 refined** — and the nuance matters,
because Arcogine's changeover/repair examples genuinely are the semi-Markov case (which Arcogine
already models as resource state plus a work-item lifecycle).

**Fox & Long, PDDL 2.1**, JAIR 20, 2003. A durative action reduces to start effects, end effects and
an invariant over the interval — an ordinary process with a lifecycle.
*Disagreement recorded:* Smith's JAIR commentary rejects this reduction, arguing durative actions
need irreducible primitive status. Ghallab, Nau & Traverso (*Automated Planning and Acting*, 2016)
separate planner-internal representation from what the acting layer exposes, which resolves the
dispute for Arcogine's purposes. (Ghallab et al. also independently use **"actor"** for the
plan-and-act entity.)

**Colledanchise & Ögren, "Behavior Trees in Robotics and AI"** (CRC Press 2018; arXiv:1709.00084);
**SHOP2 / HTN planning** (Nau et al., JAIR 20, 2003).
*Concept:* a ticked BT node returns only {Success, Failure, **Running**}; HTN compound-task
decomposition is entirely planner-internal and only primitive operators touch the world.
*Bearing:* **H2 supported, H5 supported.**

**The Sims' smart objects / affordances** (secondary sources).
*Concept:* the available-operation set can be gated jointly by actor **and** subject attributes — an
object may not advertise an interaction to every actor.
*Teaches Arcogine:* **capability must be relational (actor × subject × operation), not an
actor-owned permission list.** This is the one place a proving case genuinely refined the candidate
model, and it agrees with XACML's triple and with the object-capability tradition.

### Nondeterminism, replay and modern autonomy

**Mozilla rr** (O'Callahan et al., USENIX ATC 2017); **Schneider, "Implementing Fault-Tolerant
Services Using the State Machine Approach"**, ACM Computing Surveys 22(4), 1990; **Fowler, Event
Sourcing** (External Updates / External Queries); **Temporal** and **Azure Durable Functions**
determinism-and-replay documentation.
*Concept, reached independently four times:* nondeterminism must be **recorded as input**, so a
non-re-executable source's recorded output becomes a deterministic input on replay. Temporal's
versioning/patching mechanism is the same problem as "decision-source material version".
*Bearing:* **H4 supported** — and RF: ADR-0015 §2's *ordered external commands* is already this
answer.

**PyTorch reproducibility documentation.** "Completely reproducible results are not guaranteed
across PyTorch releases, individual commits, or different platforms", with nondeterministic CUDA
atomics and backend-dependent accumulation order. *Teaches Arcogine:* re-executing a learned source
fails **in principle**, not merely in practice. *Bearing:* **H4 supported.**

**Turpin et al., "Language Models Don't Always Say What They Think"** (NeurIPS 2023); **Lanham et
al., "Measuring Faithfulness in Chain-of-Thought Reasoning"** (2023).
*Concept:* stated reasoning can "systematically misrepresent the true reason for a model's
prediction" and may be post-hoc. *Teaches Arcogine:* recording reasoning traces as world semantics
would create **false provenance**. *Bearing:* **H2 strengthened beyond its own claim.**

**EU AI Act Articles 12 and 14; NIST AI RMF.** Require records of operation and of the human
oversight decision; by omission across both articles, do **not** require internal reasoning.
*Bearing:* **H2 supported** by an independent, binding register.

**Model Cards** (Mitchell et al., FAT* 2019); **MLflow model registry.** The minimum attributable
identity is model identity + version + intended use — and a **mutable alias is not a durable
identifier** (`models:/name@alias` is explicitly reassignable). *Bearing:* refines the H4 provenance
record.

**Model Context Protocol; A2A (Agent2Agent).** MCP is JSON-RPC with typed tools, resources and
prompts and contains no belief/goal/intention primitive; A2A pairs a capability descriptor with an
asynchronous task lifecycle, participants interacting "without needing access to each other's
internal state, memory, or tools." *Bearing:* **H6 supported** — the protocols that succeeded are
ordinary typed operations.

**Santoni de Sio & van den Hoven, "Meaningful Human Control over Autonomous Systems"**, *Frontiers in
Robotics and AI*, 2018. The *tracing* condition requires that some human remain identifiable in the
design/operation chain. *Teaches Arcogine:* a principal distinct from the immediate actor is not
merely convenient. *Bearing:* **H3 supported.** *Note:* their *tracking* condition is a
behavioural/design property, not a logging requirement — it must not be misread as demanding
recorded machine reasoning.

### Evidence excluded

Textbook agent taxonomies (Wooldridge & Jennings' weak/strong agency; Franklin & Graesser's
definitional survey; Russell & Norvig's agent function/program) were examined and **excluded from
the decision**: they classify what counts as an agent, which is not a question Arcogine needs
answered. No invariant emerged from them that resists decomposition into
(actor identity + observation boundary + decision function + capability + operation).
Leitão's agent-based manufacturing survey and the Springer ARTI review chapter are paywalled and
were characterized from abstracts only; nothing in the report rests on them.
## 4. Proving-case analysis

Each case is answered with the same 16 questions. The candidate boundary under test is the
**composition** defined in §10, not a generalized `Agent`.

Notation: **RF** = repository fact, **EE** = external evidence, **AI** = architectural inference.

---

### A. Current `SalesAgent` (REPOSITORY FACT throughout — this case is executable evidence)

1. **Attributable actor** — *None exists today.* The `PriceChange` event `SalesAgent` schedules is
   structurally identical to the one `SimThread` builds for a human `POST /api/price`. Under the
   candidate model the actor would be a `SoftwareAgent`-kind actor, "the automated pricing
   participant", distinct from the class `SalesAgent` and from the rule inside it.
2. **Distinct principal** — Not required today. If the operator who enabled the agent is to remain
   accountable, `AgentEnabledChanged` is already the enabling act and would carry the enabling
   actor; the pricing actor would then act on behalf of that operator.
3. **Subject** — `OfferPrice`, owned by `PricingState`. Already expressible: the supported contract
   has `AffectedEntityRef`; `PriceChange` has no entity ref today because pricing is a singleton.
4. **Observation boundary** — `AgentObservation(backlog, avgLeadTime, completedSalesValue,
   completedSales, offerPrice, throughput)`, *pushed* by `IntegratedHandler` via
   `AgentObservationProjector.project(factory, pricing, currentTicks)`. The agent cannot pull; the
   orchestrator decides what it sees. This is a genuine epistemic boundary, already enforced
   structurally.
5. **What produces the decision** — `SalesAgent.decide()`, a pure function of the last pushed
   observation and `SalesAgentConfig(backlogHigh, backlogLow, adjustmentPct, minPrice, maxPrice)`.
6. **Materially relevant decision-source identity/version** — *Missing.* `AgentConfig.agentType`
   is the string `"sales"`. Two runs with different `SalesAgentConfig` thresholds are not
   distinguishable in the event log. This is the same gap `EngineSemanticsVersion` (ADR-0015) fixes
   for Engine interpretation and `EvaluationPolicyIdentity` fixes for Challenge scoring.
7. **Capability permitting the operation** — *None.* `Scheduler.schedule(Event)` accepts any
   payload from anything holding the scheduler. Authority is ambient. The only real constraint is
   the sealed `EventPayload` permits-list, which bounds the operation *vocabulary* but not *who*
   may use it. EE: this is textbook ambient authority (Hardy 1988).
8. **Primitive or temporally extended** — Primitive. One `PriceChange`, one transition.
9. **Realization** — `scheduler.schedule(...)`; `PricingState.handleEvent` applies it. Note the
   asymmetry: the human path calls `handler.handleEvent(event, scheduler)` **immediately**, the
   agent path **enqueues**. Same operation, different realization — which is exactly the
   operation/realization split (operational architecture §4) already being exercised without
   being named.
10. **Facts that must survive** — actor; policy identity + config version; the observation the
    decision was taken from; the requested new price; the resulting `PriceChange`. Today only the
    last survives, plus `AgentDecision(String)` — a formatted narrative
    (`"SalesAgent: backlog=%d, price %.2f -> %.2f"`), not a semantic fact.
11. **Deterministically re-executable?** Yes, completely. `decide()` is pure; all four replay
    targets coincide.
12. **What replay means** — Nothing special. Case A is the degenerate case.
13. **Communication beyond operations/events?** No.
14. **Can the actor switch controllers without changing identity?** Not testable today — there is
    no actor identity. Under the candidate model, yes: `AgentEnabledChanged` already models
    controller attachment/detachment as a replayable event, which is precisely a controller swap.
15. **Can one controller serve several actors?** Not today (one `SalesAgent` field). Nothing in the
    model prevents it.
16. **Role co-occupancy** — The human operator is simultaneously the actor for manual price changes
    and the principal enabling the automated actor. **AI: Arcogine already has role co-occupancy in
    production and handles it by having no roles at all.**

> **The decisive observation for the whole investigation.** In Arcogine today a human and the
> `SalesAgent` already perform *the same operation* on *the same subject* through *the same
> command vocabulary*, and `docs/product/concepts.md` says so outright ("You (or the agent)").
> No agent-specific ontology was ever needed to achieve that symmetry. What is genuinely missing
> is not an `Agent` concept but **attribution on the operation**.

---

### B. Human planner / supervisor

1. **Actor** — the person (PROV `prov:Person`). EE: PROV-DM §5.3.1 needs no new kind for humans.
2. **Principal** — often yes: a supervisor approving on behalf of an organization or a role.
   EE: RFC 8693 §1.1 delegation — "principal A still has its own identity separate from B... any
   actions taken are being taken by A representing B"; PROV-O: the delegator "retains some
   responsibility for the outcome of the delegated work."
3. **Subject** — for approve/reject, the subject is *the pending request or proposed change*, not
   the machine. This matters (see the Decision/Operation collapse in §6).
4. **Observation** — a purpose-specific projection, exactly the existing `Observation` contract.
   EE: DMN's `Input Data` — "information used as an input by one or more Decisions" — is the same
   role in a normative decision standard.
5. **What produces the decision** — the person's judgement. **Deliberately opaque.**
6. **Decision-source version** — usually *not applicable*. If they applied a documented policy, the
   policy's identity+version is the relevant reference, not the person's reasoning. EE: DMN's
   `Knowledge Source` — "an authority for a Business Knowledge Model or Decision", which "might be
   ... domain experts responsible for defining or maintaining them, or source documents" — is
   exactly this, and DMN explicitly gives Knowledge Sources **no execution semantics**.
7. **Capability** — a genuine authorization question ("may this supervisor stop this line?").
8. **Granularity** — mixed: *approve* is primitive; *reschedule* may be temporally extended.
9. **Realization** — the same command path any other actor uses.
10. **Must survive** — who, when, on what observation, what they requested, under what authority,
    and the outcome. **Not why they thought it.** EE: EU AI Act Art. 12/14 require logging of
    operation records and the human-oversight decision, and by omission do not require internal
    reasoning.
11. **Re-executable?** No. There is no source to re-execute.
12. **Replay means** — replay of the *recorded decision* as an input, or of the resulting
    transition. EE: this is Fowler's Event-Sourcing gateway rule and Temporal's Activity rule —
    the recorded output of a nondeterministic source becomes a deterministic input on replay.
13. **Communication?** No. Approve/reject/stop are ordinary operations.
14. **Controller switch preserving identity?** This is the human→automated→human handoff. Yes —
    and EE: Santoni de Sio & van den Hoven's *tracing* condition requires that some human remain
    identifiable in the chain, which is precisely why the principal must not be erased when an
    automated controller takes over.
15. **One controller, many actors?** Not applicable.
16. **Role co-occupancy** — Yes: here the person is simultaneously actor and decision source. The
    model must permit this **without** collapsing the two concepts, because in case E the same two
    roles are occupied by different entities. EE: PROV solves this by making *role* a property of
    the qualified association, not of the agent.

---

### C. Organization or external system (supplier, ERP, MES)

1. **Actor** — the organization or system (PROV `prov:Organization` / `prov:SoftwareAgent`).
2. **Principal** — possibly: an ERP acting for the company that operates it.
3. **Subject** — an external subject, reached only through an authoritative correspondence
   assertion (operational architecture §8.1). RF: Arcogine already forbids implicit correspondence
   by name, endpoint, or coincident identifier.
4. **Observation** — whatever the participant is given; its own inputs are invisible.
5. **What produces the decision** — **opaque and must stay opaque.**
   EE (decisive): BPMN 2.0 models an external organization as a **black-box Pool** — "A `Pool` is
   NOT REQUIRED to contain a `Process`, i.e., it can be a 'black box.'" You model the interactions,
   never the internals. **AI: this is the strongest normative answer to the whole investigation —
   the mature process standard's treatment of an autonomous external participant is to model its
   interface and refuse to model its autonomy.**
6. **Decision-source version** — not Arcogine's to know. What matters is the interface/contract
   version, which is adapter/profile provenance (operational §14).
7. **Capability** — the contractually offered interface. EE: the Industrie 4.0 CSS distinction
   (Capability = what can be done, implementation-independent; Skill = executable implementation;
   Service = callable interface) maps cleanly, and *only the Service is externally visible*.
8. **Granularity** — a single contractual operation ("fulfil this purchase order") whose internal
   workflow is hidden. This is H5's real motivating case.
   EE: BPMN's **Call Activity/Sub-Process** already provides "one callable activity whose
   realization is a whole process" without requiring the caller to know.
9. **Realization** — an adapter. Request ≠ acceptance ≠ transition ≠ observation ≠ reconciled
   interpretation (operational §4, already accepted direction).
10. **Must survive** — actor identity, correspondence assertion used, requested operation, adapter/
    profile version, acknowledgement, and any later observation. RF: operational §6 already
    enumerates this.
11. **Re-executable?** No — and re-calling would be *harmful*, not merely unreliable.
    EE: Fowler's "External Updates" — re-executing an external side effect on replay is a bug.
12. **Replay means** — replay the recorded exchange, never re-invoke the partner.
13. **Communication?** Ordinary operations plus recorded results suffice. EE: EDI purchase
    order/confirmation is exactly an operation and a result fact, not a mental-state exchange.
14–15. Yes and yes; an organization acts through many systems and people, which is precisely
    `actedOnBehalfOf`.
16. **Role co-occupancy** — the ERP is actor, decision source, and (for its own records) subject
    owner simultaneously. Roles remain distinguishable; identities need not.

---

### D. Embodied NPC / simulated worker

1. **Actor** — the modelled worker/participant identity.
2. **Principal** — optional (a shift supervisor, a squad).
3. **Subject/body** — **a separate identity from the actor.** The body is modelled world state with
   position, capacity, and availability; it exists whether or not anything is controlling it.
   RF: Arcogine already models bodies this way — `MachineId` with state/queue/position — and
   `MachineStore` owns them independently of any decision-maker.
4. **Observation** — deliberately *limited* perception. RF: Arcogine's observation contract already
   supports this — "an agent can only act on what its observation exposes."
5. **What produces the decision** — behaviour tree, GOAP, utility AI, learned policy. **Private.**
6. **Decision-source version** — relevant for reproducibility of a scenario, not for world meaning.
7. **Capability** — what this worker may do to which equipment.
8. **Granularity** — mixed; "perform a changeover" is temporally extended, "move one step" is not.
9. **Realization** — ordinary domain operations.
10. **Must survive** — actor, body, observation boundary, requested operation, transitions.
11. **Re-executable?** Yes if the controller is deterministic; no if stochastic.
12. **Replay** — as in case F when stochastic.
13. **Communication?** "Communicate" is an operation with an addressee subject. No message ontology.
14. **Controller switch?** **This is the case that makes the distinction unavoidable.** A body must
    be able to be driven by a scripted controller, then a player, then an autopilot, without the
    worker's operating history fragmenting into three unrelated histories.
15. **One controller, many actors?** Yes — one behaviour policy instance driving twenty workers is
    the normal case, and each worker's actions must remain separately attributable.
16. **Role co-occupancy** — actor and body may share an identity for a simple NPC; the roles must
    stay distinguishable so that possession/handoff remains expressible.

---

### E. Deterministic planner / optimizer / dispatcher

1. **Actor** — genuinely ambiguous, and the ambiguity is the finding. Either (a) the dispatcher is
   the actor for every dispatch, or (b) each resource is the actor and the dispatcher is its
   decision source. Only (b) preserves per-resource operating history.
2. **Principal** — the operations organization that authorized the dispatcher to act.
3. **Subject** — the resources and work items dispatched.
4. **Observation** — authoritative runtime observation. RF: `RuntimeObservation` already exists and
   already carries `latestEventSequence`, giving a citable input reference for free.
5. **What produces the decision** — the dispatch policy.
6. **Decision-source version** — **materially relevant.** RF: Arcogine already implements exactly
   this for its own dispatch, as `EngineSemanticsVersion` (ADR-0015): "one `EngineSemanticsVersion`
   as the semantic identity of Arcogine's complete result-affecting simulation interpretation for a
   run", covering "resource eligibility, selection ranking, queue-depth interpretation, and final
   tie-breaking".
7. **Capability** — dispatch authority over a resource pool.
8. **Granularity** — usually a set of primitive assignments issued together; the *plan* is one
   decision, the *dispatches* are many operations. Correlation, not a new concept.
9. **Realization** — ordinary dispatch.
10. **Must survive** — policy identity+version, observation cursor, the emitted assignments.
11. **Re-executable?** Yes, and Arcogine already states the exact tuple that makes it so —
    ADR-0015 §2: `ModelFingerprint + EngineSemanticsVersion + explicit workload + seed/random
    inputs + ordered external commands`.
12. **Replay** — all four targets coincide, as in case A.
13. **Communication?** No.
14. **Controller switch?** Yes — swapping dispatch policy must not re-identify the resources.
15. **One controller, many actors?** **This is the defining feature of the case**, and it is the
    single clearest falsifier of `actor == controller`.
16. **Role co-occupancy** — actor and decision source are *necessarily different* here, whereas in
    case B they are *necessarily the same*. **AI: no model that fixes their relationship in either
    direction can satisfy both cases. This is the decisive argument that they are distinct roles
    with an unconstrained cardinality — which is exactly what the planning document already
    forbids collapsing.**

---

### F. Nondeterministic or learned controller

1. **Actor** — the participant the policy decides for (or the service provider, if it is itself a
   responsible party).
2. **Principal** — usually yes, and increasingly required by regulation.
3. **Subject** — as in the deterministic case.
4. **Observation** — the same purpose-specific projection. Unchanged by the controller's nature.
5. **What produces the decision** — a stochastic policy, learned model, LLM, or remote service.
6. **Decision-source version** — **the most important field in the whole model.** EE: it must be a
   *resolved, immutable* version, never a mutable alias — MLflow's `models:/name@alias` is
   explicitly reassignable, so recording an alias records nothing durable.
7. **Capability** — unchanged; authority does not depend on how the choice was produced.
8. **Granularity** — unchanged.
9. **Realization** — unchanged.
10. **Must survive** — actor, principal, decision-source identity + resolved version, input
    observation, emitted decision, authority determination, resulting transitions.
    EE: this matches EU AI Act Art. 12/14 (records of operation, and the human-oversight decision)
    and Model Cards' minimum (model identity + version + intended use).
    **Must NOT survive: reasoning traces treated as ground truth, weights, or internal state.**
11. **Re-executable?** **No — not even in principle.** EE: PyTorch's own reproducibility
    documentation states "Completely reproducible results are not guaranteed across PyTorch
    releases, individual commits, or different platforms", with nondeterministic CUDA atomics and
    backend-dependent floating-point accumulation order. Same weights and same input are not
    sufficient.
12. **What replay means** — the recorded decision becomes a deterministic *input*. EE: this is
    exactly rr's record/replay design, Fowler's Event Sourcing gateway for external queries, and
    Temporal/Durable Functions, where nondeterministic work is confined to Activities whose result
    is recorded in history and returned verbatim on replay.
    **RF: Arcogine has already accepted this answer** — ADR-0015 §2 puts *ordered external commands*
    in the reproducibility tuple. A nondeterministic decision source's output is an external
    command.
13. **Communication?** No. EE: MCP's specification is JSON-RPC with typed tools/resources/prompts —
    no belief, goal or intention primitive anywhere; A2A's agent card plus task lifecycle is a
    capability descriptor plus an ordinary async-job lifecycle, with participants interacting
    "without needing access to each other's internal state, memory, or tools."
14–15. Yes and yes.
16. **Role co-occupancy** — as case E.
    **Critical negative result:** recording a chain-of-thought as the explanation of this decision
    would create *false provenance*. EE: Turpin et al. (NeurIPS 2023) show CoT explanations can
    "systematically misrepresent the true reason for a model's prediction"; Lanham et al. (2023)
    find reasoning may be post-hoc. **This is not merely "internals are unnecessary" — it is
    "internals are affirmatively misleading as world semantics."**
## 5. Comparison matrix

Rows are the six proving cases; columns are the required dimensions. `—` means the dimension is
genuinely not applicable, which is itself evidence.

| Dimension | A. `SalesAgent` | B. Human planner | C. Organization / external system | D. Embodied NPC | E. Deterministic planner | F. Learned / nondeterministic |
|---|---|---|---|---|---|---|
| **Attribution** | none today; would be a software-kind actor | the person | the organization / system | the modelled worker | **ambiguous by design**: dispatcher *or* each resource | the participant, or the service provider |
| **Observation** | `AgentObservation`, pushed by orchestrator | purpose-specific projection | only what the interface exposes | deliberately limited perception | authoritative `RuntimeObservation` | same as E |
| **Decision source** | pure `decide()` rule | human judgement (opaque) | **opaque by contract** | BT / GOAP / utility / RL | dispatch policy | stochastic policy / model / remote service |
| **Source identity + version** | missing; `agentType="sales"` only | usually n/a; the *policy* they applied may have one | not Arcogine's to know; adapter/profile version instead | scenario-relevant only | **material** (cf. `EngineSemanticsVersion`) | **material, and must be a resolved immutable version, never an alias** |
| **Subject / body** | `OfferPrice` (singleton) | the pending request or the resource | external subject via authoritative correspondence | **body is a separate identity from the actor** | resources and work items | as E |
| **Capability** | none — ambient authority | genuine authorization question | the contractually offered interface | what this worker may do to which equipment | dispatch authority over a pool | unchanged by controller nature |
| **Operation granularity** | primitive | mixed (approve primitive; reschedule extended) | **one contractual operation hiding a whole workflow** | mixed | many primitive assignments from one plan | as E |
| **Delegation** | not modelled | common (acting for a role/org) | inherent (org acts through systems) | optional | authorized by the operations org | usually required |
| **Actor : controller : subject cardinality** | 1:1:1 | 1:1:n | 1:1:n | 1:1:1 body, but **n actors : 1 controller** typical | **n actors : 1 controller** | n:1:n |
| **Role co-occupancy** | human is actor *and* principal of the automated actor | actor **is necessarily** the decision source | actor is decision source and subject owner | actor may share identity with body | actor is **necessarily not** the decision source | as E |
| **Determinism** | fully deterministic | none | none | depends on controller | fully deterministic | **not reproducible even in principle** |
| **Replay** | all four targets coincide | recorded decision / transition only | recorded exchange only; re-invoking is harmful | as controller | all four coincide | recorded decision / transition only |
| **Provenance** | today: one `PriceChange` + a narrative string | who / when / on what / requested what / under what authority | correspondence + adapter/profile + acknowledgement | actor + body + observation + operation | policy version + observation cursor + assignments | actor + principal + source id/version + input + output + authority |
| **Communication** | ordinary events | ordinary operations | ordinary operations + recorded results | "communicate" is an operation | ordinary events | ordinary typed operations (MCP/A2A shape) |
| **Consumer-private state** | last observation, intervention count | private reasoning — **must not be recorded** | entire internals | BT state, memory, personality, navigation | search state | weights, sampling state, reasoning trace — **recording it would be false provenance** |

### What the matrix exposes

1. **Rows B and E are mutually contradictory for any model that fixes the actor↔controller
   relation.** In B the actor *is necessarily* the decision source; in E the actor *is necessarily
   not*. No single collapsed concept satisfies both. This is the strongest structural argument in
   the investigation, and it is visible only because the two cases were required to share one
   model.
2. **The "Determinism" and "Replay" rows separate cleanly into exactly two regimes**, and the
   boundary is not simulation-vs-reality but *re-executable source vs not*. Case A and case E sit
   together; B, C and F sit together; D straddles depending on its controller. That is a much
   better fault line than any agent taxonomy.
3. **The "Capability" row is the only one where nothing is currently implemented in any case.** It
   is the genuine gap, and it is already an owned, scheduled question (PLAN-OPS-2).
4. **The "Consumer-private state" column is uniformly private across all six cases.** Not one case
   requires a decision source's internals to become shared semantics. H2 survives every case.
5. **"Operation granularity" is mixed in four of six cases**, but in every one the extension is
   already expressible as an aggregate/child work decomposition with a lifecycle — the exact shape
   ADR-0010 already gives `Order`→`Job`.
6. **Uniformity is created artificially in exactly one place**: forcing case C's opaque participant
   to have a "decision source identity/version" would be inventing knowledge Arcogine does not
   have. The correct value there is *adapter/profile version*, which is a different thing. A model
   that made `decisionSource` mandatory would produce false provenance for organizations.
## 6. Candidate semantic models

### Model 1 — Generalized `Agent` (a platform interface/base type for anything that decides)

**Benefits.** One home for decision-making participants; matches the existing `agents/` module name;
immediately familiar to contributors.

**Failure modes.**
- *Falsified by cases B and E together.* In case B the actor **is necessarily** the decision source
  (a person deciding for themselves); in case E the actor **is necessarily not** (one dispatcher
  deciding for many resources). An `Agent` that is both cannot represent case E without either
  making the dispatcher the sole accountable party for every resource's history, or duplicating an
  `Agent` per resource and losing the fact that one policy drove them all.
- *Falsified by case C.* An ERP or supplier is not autonomous software, and modelling it as an
  `Agent` invents internals Arcogine cannot observe. EE: BPMN 2.0's black-box Pool — "A `Pool` is
  NOT REQUIRED to contain a `Process`, i.e., it can be a 'black box'" — is the mature process
  standard's *explicit refusal* to model an external participant's autonomy.
- *No stopping point.* An `Agent` type attracts `goal`, `belief`, `memory`, `plan` fields over
  time, which is exactly what H2 forbids. EE: Singh's critique of mentalistic ACL semantics is
  decisive here — "the mental concepts cannot be verified without access to the internal
  construction of the agents", therefore "a purely mentalistic semantics ... cannot be a normative
  requirement on agents or their designers."
- *One concrete consumer.* Today Arcogine has exactly one: `SalesAgent`. This fails the
  repository's own evaluation criterion 12.
- *Name hazard.* "Agent" already has at least five distinct meanings in this repository.

**Unnecessary commitments.** Autonomy as an intrinsic property; a perception–decision loop; a
lifecycle; and the identity `actor == decider`.

**Verdict: rejected.** No invariant was found that it expresses and the composition cannot.

---

### Model 2 — Composition of smaller roles (the repository's working hypothesis)

Actor / Observation / Decision / Decision-source identity / Capability / Operation / Subject.

**Benefits.** Satisfies all six proving cases. Every concept is independently useful. Corroborated
independently by four traditions that never talked to each other:
PROV (Agent/Association/Delegation/Plan/Role), BPMN+DMN (Participant/Performer vs Decision vs
Business Knowledge Model vs Knowledge Source), Unreal (PlayerState/Controller/Pawn), and
RFC 8693 (`sub`/`act`).

**Failure mode.** Concept count. Introducing seven-plus shared concepts at once *is* framework
creation by accumulation, which the investigation's own non-goals forbid. The inflation comes from
one specific place: treating **Decision/intent** and **Semantic Operation** as two separate durable
things.

**Verdict: correct in substance, one concept too many.** Refined by Model 3.

---

### Model 3 — Attributed operation request (RECOMMENDED)

The same composition, with one collapse: **a decision and the operation it requests are one durable
fact seen from two sides.** What is durable is the *request*; "decision" names the act that produced
it, and is carried by the request's attribution rather than by a second object.

**Why the collapse holds.**
- The apparent counter-example — case B, where a supervisor *rejects* a proposal — collapses on
  inspection: "reject" is itself an operation, on the approval item as its subject. It is not a
  decision without an operation.
- A decision *rejected by authority* still produced a request; the request is recorded as denied.
  RF: this is exactly `CommandResult.Rejected`, which already exists.
- "Decided not to act" (`SalesAgent.decide()` returning empty) is either decision-source-private
  diagnostics (H2) or, where genuinely needed, an explicit no-op operation recorded by whoever
  needs it — not a platform concept.
- One decision producing several operations (H5) is handled by **correlation**, which the command
  lifecycle already requires ("stable correlation", operational architecture §6).
- EE: DMN models a `Decision` with **no actor at all** — attribution lives outside DMN, in BPMN's
  performer or in PROV. Decision-modelling and attribution are already separate concerns in the
  standards world; Arcogine does not need one object that is both.

**What this reduces the answer to.** One durable record (the attributed operation request) plus one
genuinely new shared value type (an actor reference). Everything else is either already implemented
(`AffectedEntityRef`, `ModelFingerprint`, `RunId`, event `sequence`) or stays domain-owned.

**Failure mode.** It cannot express "a decision was made that never became a request." No proving
case required that, and H2 says such traces are decision-source-private.

**Unnecessary commitments.** None identified.

---

### Model 4 — Adopt W3C PROV directly as the domain model (materially different; discovered in research)

**Benefits.** PROV is a W3C Recommendation with precisely the right shape: `Agent` defined purely
as "something that bears some form of responsibility", exactly the three subtypes the proving cases
need (`Person`, `Organization`, `SoftwareAgent`), `actedOnBehalfOf` with retained responsibility,
`hadRole` on qualified associations, and `hadPlan` deliberately left uninterpreted ("There exist no
prescriptive requirements on the nature of plans"). Free interchange, and no design work.

**Failure modes.**
- *PROV is retrospective only.* It defines provenance as information about what was involved in
  **producing** a thing. It has no construct for a requested-but-unrealized operation, an
  authorization denial, an ambiguous outcome, or forward intent — i.e. it cannot express the
  command lifecycle that operational architecture §6 already requires.
- *Its `Agent` name re-imports the connotations Arcogine is trying to shed.*
- *It inverts ADR-0012*, which makes Arcogine-owned semantic contracts authoritative and external
  representations projections. RF: `standards-alignment.md` already places W3C PROV at Tier 3 as a
  potential **Governance projection**, not a domain model.

**Verdict: rejected as the domain model; adopted as the vocabulary donor and the projection
target.** This is a useful, non-obvious result: Arcogine should borrow PROV's *definitions* (which
are careful and battle-tested) while keeping its own contracts authoritative — exactly the policy
ADR-0012 already sets.

---

### Model 5 — Do nothing now (the null hypothesis, taken seriously)

**Benefits.** Arcogine today is single-user and local-first, has no authentication, and has exactly
one automated decision-maker. The `PriceChange` attribution ambiguity has **zero current
consequence**. Every concept above is speculative relative to today's product.

**Failure modes.** The repository has already reinvented attribution three times as free-form
strings (`RevisionRecorder(source, subject)`, `ChangeProvenance.source`, `AgentDecision(String)`),
and `ChallengeAttempt` is a fourth record that will need it the moment there is more than one
player. PLAN-OPS-2 is already scheduled to invent actor/capability semantics under operational
pressure, and the readiness plan itself warns that it must not be "forced into Operational merely
because the first real-world consumer needs it."

**Verdict: wrong as a permanent answer, approximately right as a near-term implementation answer.**
The correct action now is to record the boundary so the scheduled work does not rediscover it, and
to stop adding new stringly-typed attribution — not to build anything.
## 7. Provenance and replay model

### The four operations are genuinely distinct, and the fault line is not simulation-vs-reality

| Operation | What it means | Requires |
|---|---|---|
| **Re-execute the decision source** | run the mechanism again on the same input and trust it produces the same choice | a deterministic, still-executable source at a known version |
| **Replay the recorded decision** | feed the previously recorded choice back as an *input*, never re-invoking the source | a durable record of the choice + its attribution |
| **Replay the requested operation** | re-apply an already-decided request through a deterministic apply-function | a durable request + a deterministic realization |
| **Replay the resulting transition** | reconstitute the recorded state change without decision or request | a durable transition record |

For a deterministic source **all four coincide** — which is why case A and case E look like they
need no distinction and are misleading as the only evidence.

**Achievability by decision-source kind:**

| Source | Re-execute source | Replay decision | Replay operation | Replay transition |
|---|---|---|---|---|
| Deterministic code (A, E) | yes | yes | yes | yes |
| Learned model (F) | **no — not even in principle** | yes | yes | yes |
| Remote service / external org (C, F) | **no, and re-invoking is harmful** | yes | yes | yes |
| Human (B) | **no source exists to re-execute** | yes | yes | yes |

EE for "not even in principle": PyTorch's own reproducibility documentation states "Completely
reproducible results are not guaranteed across PyTorch releases, individual commits, or different
platforms", with nondeterministic CUDA atomics and backend-dependent floating-point accumulation
order. Same weights plus same input is *not* sufficient.
EE for "harmful": Fowler's Event Sourcing treatment of **External Updates** — re-running an external
side effect during replay is a defect, which is why the gateway records the response instead.

### The unifying rule Arcogine should adopt

> **Convert nondeterminism into recorded input.** The recorded *output* of a non-re-executable
> decision source becomes a deterministic *input* on replay.

This is the same answer reached independently by record/replay debugging (Mozilla rr records
nondeterministic inputs and replays deterministically), state-machine replication (Schneider 1990
requires a deterministic state machine, so nondeterminism must be resolved into the command
stream), event sourcing (the gateway remembers responses), and production workflow engines
(Temporal and Azure Durable Functions confine nondeterminism to Activities whose results are
recorded in history and returned verbatim on replay).

**RF: Arcogine has already accepted this rule.** ADR-0015 §2 puts *ordered external commands* in the
durable reproducibility tuple alongside `ModelFingerprint`, `EngineSemanticsVersion`, explicit
workload, and seed. A nondeterministic decision source's output **is** an ordered external command.
No new mechanism is required — only attribution on those commands.

### Minimum durable record, by decision-source kind

Common to all: **actor**; **subject**; **requested operation**; **time**; **outcome correlation**.

| Additionally | Deterministic controller | Nondeterministic controller | Human | Organization / external system |
|---|---|---|---|---|
| decision-source identity | required | required | usually n/a | **must not be invented** |
| material version | required, resolved & immutable | required, resolved & immutable — **never a mutable alias** | the applied policy's version, if any | adapter/profile version instead |
| observation reference | required | required | required | only what was sent |
| principal / on-behalf-of | optional | usually required | usually required | inherent |
| authority determination | required once authority exists | required | required | required |
| recorded decision output | optional (re-derivable) | **required** | **required** | **required** |

**EE — the "resolved, immutable version" constraint is not pedantry.** A mutable alias records
nothing durable: MLflow's `models:/name@alias` form is explicitly reassignable, so a provenance
record naming an alias can silently come to mean a different model. Arcogine's existing instincts
already match: `ModelFingerprint` is content-derived, and ADR-0015 §12 makes released semantics
versions "immutable and never reused."

### What must NOT be recorded

Reasoning traces treated as ground truth, model weights or internal state, planner search state,
behaviour-tree state, and private human reasoning.

**This is a stronger claim than "unnecessary".** EE: Turpin et al. (NeurIPS 2023) show
chain-of-thought explanations can "systematically misrepresent the true reason for a model's
prediction"; Lanham et al. (2023) find stated reasoning may be post-hoc. Recording such a trace as
*world semantics* would manufacture **false provenance** — a durable, authoritative-looking record
of a cause that was not the cause. For a platform whose Charter promises to explain "what happened,
why, ... because of which decision", that is worse than recording nothing.

**Convergent regulatory evidence.** EU AI Act Article 12 (record-keeping) and Article 14 (human
oversight) require records of the system's operation and of the human oversight decision — and, by
omission across both articles, do **not** require the model's internal reasoning. The regulatory
minimum and the architectural minimum agree.

### Explicitly: what Arcogine needs each operation for

- **Deterministic simulation reproducibility** → re-execute source (already served by ADR-0015's
  tuple).
- **Causal reconstruction** → replay the recorded decision, with attribution.
- **Verification** → replay the requested operation against a chosen semantics version.
- **Audit** → the durable record only; no replay required. RF: ADR-0016 §7 already establishes that
  "historical evidence validity does not require permanent re-executability."
- **Historical inspection** → seek/reconstitution (operational architecture §10).
- **Forked experiments** → fork from a reconciled historical state; explicitly does not require
  replaying every event that produced it (operational §10).

---

## 8. Capability granularity

**Conclusion: Arcogine does not need a new shared "temporally extended capability" concept. It needs
the aggregate/child work-item decomposition it already has, applied beyond orders.**

The distinction under test (H5) is real — `PerformChangeover`, `FulfilPurchaseOrder`,
`RepairMachine`, `RebalanceProduction` genuinely are single requestable things whose realization
spans many lower-level transitions. But three independent lines of evidence say the *world* need
not represent them as a new kind of thing:

1. **The environment sees only primitive actions.** In the options framework (Sutton, Precup &
   Singh, *Artificial Intelligence* 112, 1999) an option — (initiation set, policy, termination
   condition) — is **agent-side**; the base environment receives one primitive action per step.
   A durative action enters the *environment* model only when the process's own dynamics are
   semi-Markov, i.e. have intrinsic holding times.
   **AI: Arcogine's changeover and repair examples are exactly that semi-Markov case** — the machine
   really is unavailable for a duration in the world. But that is *resource state plus a work-item
   lifecycle*, which Arcogine already models (`MachineState.Busy`, setup time, `TaskStart`/`TaskEnd`,
   `Job` status) — not a new capability kind.
2. **A durative action reduces to a lifecycle.** PDDL 2.1 (Fox & Long, JAIR 20, 2003) represents a
   durative action as start conditions/effects, end conditions/effects, and invariants held over
   the interval — i.e. (start event, end event, invariant), an ordinary process with a lifecycle.
   *Disagreement noted:* Smith's JAIR commentary rejects this reduction, arguing durative actions
   need irreducible primitive treatment. The dispute is about **planner-internal representation**,
   not about what the acting layer exposes; Ghallab, Nau & Traverso (2016) separate exactly these
   layers, and all parties agree the execution layer exposes a lifecycle contract.
3. **Decomposition is decider-private.** In HTN planning, compound-task decomposition is entirely
   planner-internal; only primitive operators touch the world. Behaviour trees say the same thing
   from the other direction: a ticked node returns only {Success, Failure, **Running**}, and
   `Running` is the *entire* externally meaningful content of an in-progress durative action.

**RF: Arcogine has already solved this once.** ADR-0010 gives one accepted `Order` (aggregate
intent, `OrderId`) deterministically materializing N unit-quantity child `Job`s (`JobId`), each
traversing routing independently, with the aggregate owning completion and only the final child
emitting `OrderCompleted` carrying both identities. That is precisely "one requested thing, many
realizing transitions, aggregate identity, child identity, correlation, completion rule."

`PerformChangeover` needs those same four things. It does **not** need a fifth concept.

**External corroboration that the distinction belongs at the interface, not in a new type:** BPMN's
Call Activity/Sub-Process already offers one callable activity whose realization is a whole process,
without the caller needing to know. This is also why case C works — an external participant exposes
one contractual operation and hides its workflow.

**Recommendation.** Record the *pattern* — a requestable operation may be realized by a correlated
set of child operations under an aggregate identity with an explicit completion rule — as a rule
domains follow, and let each domain own its own aggregate types. Do not create a shared
`Capability`/`Procedure` type.

---

## 9. Communication conclusion

**No shared agent-specific communication abstraction is justified. H6 survives every proving case.**

- Case C's supplier interaction is a purchase order and a confirmation — an operation on a subject
  reached through an adapter, plus a recorded result fact. Operational architecture §6's command
  lifecycle already covers it exactly.
- Case D's "communicate" is an operation whose subject is the addressee.
- Cases A, E and F need nothing beyond the existing event/observation boundary.

**The decisive negative evidence is that the agent-communication tradition's own critics identified
why an ACL cannot work as a normative contract.** Mentalistic ACL semantics (FIPA ACL and Arcol are
explicitly mentalist) define message meaning in terms of the sender's beliefs and intentions, and:

> "Communication is a public phenomenon, but the mental concepts are private... the mental concepts
> cannot be verified without access to the internal construction of the agents... a purely
> mentalistic semantics of an ACL cannot be a normative requirement on agents or their designers."
> — Singh, *A Social Semantics for Agent Communication Languages*

This is the same conclusion H2 reaches from the provenance side, arrived at independently from the
communication side: **anything defined in terms of a decision source's internals is unverifiable
from outside, and therefore cannot be a contract.**

**Convergent modern evidence.** The most widely adopted current interoperability protocols for
autonomous software are ordinary typed operation interfaces, not ACLs. MCP is JSON-RPC with typed
tools, resources and prompts, containing no belief/goal/intention primitive; A2A pairs a capability
descriptor with an ordinary asynchronous task lifecycle, and its participants interact "without
needing access to each other's internal state, memory, or tools."

**If Arcogine ever does need cross-participant semantics beyond operations**, the verifiable form is
a **public commitment** — an obligation between parties concerning a subject, observable and
challengeable from outside — which is Singh's proposed replacement for mentalistic semantics. RF:
Arcogine already has one such commitment in production: an accepted `Order` is exactly a public,
externally checkable obligation. That is the shape to extend, not a message ontology.

RF: `standards-alignment.md` already places FIPA at Tier 3, "Relevant only if independently
developed autonomous agents need standardized inter-agent communication." This investigation finds
no reason to move it.
## 10. Recommended Arcogine boundary

The working shape in `docs/planning/agency-decision-boundary.md` §2 survives the proving cases in
substance, with **one structural change**: *Decision/intent* and *Semantic Operation* are not two
durable things. They are one attributed request. Observation and decision source become
**references carried by** that request rather than stages preceding it.

```text
        authoritative State
                │
                ▼
          Observation ─────────────────────┐
        (purpose-specific,                 │ input reference
         bounds what can be known)         │
                │                          │
                ▼                          │
        Decision source ───────────────────┤ identity + resolved
   (mechanism; entirely PRIVATE:           │ immutable version
    rule, judgement, planner, policy,      │
    behaviour tree, model, black box)      │
                │                          │
                ▼                          ▼
   Actor ───────────►  ATTRIBUTED OPERATION REQUEST  ◄─── Authority determination
   (+ on-behalf-of chain,       │                          (actor × subject × operation)
      role in this act)         │
                                ▼
                          Realization
              (synthetic apply, adapter, human work —
               request ≠ acceptance ≠ outcome)
                                │
                                ▼
                           Transition
                     (what actually changed)
                                │
                                └──────────► back to State
```

### The surviving shared concepts, defined semantically

1. **Actor** — the identity that bears responsibility for a participation or action. It is *not* the
   thing that decides, and *not* the thing acted upon. Persons, organizations and software are all
   actors, differing only by kind. An actor exists independently of whatever is currently deciding
   for it, which is exactly what makes controller replacement survivable.

2. **On-behalf-of (delegation)** — a relation between actors in which the delegator **retains some
   responsibility** for the outcome. Two rules, both borrowed from deployed practice: *authorization
   consults the current actor only*; *the whole chain is provenance*. Delegation is not
   impersonation — in delegation the acting party keeps its own identity.

3. **Role** — the function an actor served **in one particular participation**. A property of the
   participation, not of the actor. This is what lets one identity occupy several roles (case B,
   case C) without inventing duplicate identities, and lets the roles stay distinguishable when they
   are occupied by different entities (case E).

4. **Decision-source reference** — the identity and *resolved, immutable* material version of the
   mechanism that produced a choice. **Deliberately uninterpreted**: Arcogine records *that* a
   source was relied on and *which version*, never how it works. Optional, and correctly *absent*
   for a direct human act; correctly *refused* for an opaque external participant, which carries
   adapter/profile version instead.

5. **Observation reference** — which purpose-specific observation bounded the decision. Already
   expressible as `(runId, latestEventSequence)`. This is what makes **epistemic containment**
   checkable: a decision should be explainable from the observation its source was given plus that
   source's private state — the property that stops a simulated participant from acting on
   information it could not have had.

6. **Authority determination** — the recorded outcome of *may this actor request this operation on
   this subject*. **Relational, not actor-owned**: the available operation set is jointly determined
   by actor and subject, so a per-actor permission list is insufficient.

7. **Subject reference** — what is acted upon or represented. Already implemented as
   `AffectedEntityRef`; no new concept.

8. **Operation, Realization, Transition** — unchanged from accepted architecture. The operation
   vocabulary stays domain-owned and closed.

### What this costs Arcogine

**One durable record** (the attributed operation request) and **one genuinely new shared value
type** (an actor reference: namespaced identity + kind). Everything else is either already
implemented or stays domain-owned.

### Rules that fall out of the analysis and are worth stating explicitly

- **Actor identity is attribution metadata and must never affect deterministic outcome** — the same
  rule ADR-0011 §4 already imposes on `RunId`. *Subject* identity may affect outcome, because a
  subject is modelled world state. If a requester genuinely should influence dispatch (a priority
  queue), that requester must appear as a modelled field in the operation payload, never be smuggled
  in through attribution metadata.
- **Never record a decision source's internals as world semantics.** Not merely unnecessary —
  affirmatively misleading, because a stated rationale need not be the operative cause.
- **A material version must be a resolved immutable identifier, never a mutable alias.**
- **Advisory and authoritative decision sources are different.** A source whose output is a
  recommendation a human then acts on is not the same as one whose output is applied directly; the
  attributed request records the human as actor in the first case and the automated participant in
  the second. (Terminology borrowed from the holonic tradition's advisory *staff* role.)

---

## 11. Ownership recommendation

**No new module. No new delivery track. No new subsystem.**

| Surviving concept | Narrowest appropriate owner | Rationale |
|---|---|---|
| **Actor reference** (identity + kind) | `:types` | It is the only genuinely cross-cutting new value. `:types` already holds exactly this class of thing — `ControlledRevisionId`, `ModelFingerprint`, `RunId`. Four existing consumers already need it: `RevisionRecorder`, `ChangeProvenance`, a future simulation attribution, and PLAN-OPS-2. |
| **On-behalf-of, role** | with the attributed request, not with the actor | They are properties of a participation, not of an identity. Putting them on the actor would force a relationship model into a value type. |
| **Decision-source reference** | **each domain, separately — do NOT extract** | Engine owns `EngineSemanticsVersion`; Challenge owns `EvaluationPolicyIdentity`; Governance owns `RequirementVersion`/`AssertionVersion`. Extracting a shared type would force `challenge` — which deliberately has **no** `project(...)` dependency on `types`, `simulation`, any domain, `api` or `cli` — to acquire one. **The extraction would violate an existing, deliberate ownership boundary.** Record the *rule*; do not build the type. |
| **Observation reference** | Engine | `RuntimeObservationMetadata.latestEventSequence` already is it. |
| **Authority determination** | PLAN-OPS-2, evaluated per domain | Already scheduled, and the readiness plan already says ownership "must not be forced into Operational merely because it is the first consequential consumer." Nothing here changes that; this investigation supplies the vocabulary PLAN-OPS-2 was going to have to invent. |
| **Attributed operation request** | the existing command boundary | `CommandResult` (Engine) for synthetic realization; operational architecture §6's command lifecycle for external realization. Extend what exists; do not create a parallel record. |
| **Temporally extended operations** | each domain | ADR-0010's `Order`→`Job` aggregate/child pattern already solves it. Record the pattern as a rule. |

### Track-creation test (agency-decision-boundary.md §10) — result: FAILED, correctly

A distinct track requires "a coherent implementation responsibility with durable shared semantics,
multiple real consumers, and work that cannot be owned cleanly by the existing Engine, Governance,
Operational, Factory Design, or Challenge boundaries."

- Durable shared semantics: **one value type.**
- Multiple real consumers: **yes — four.**
- Cannot be owned cleanly by an existing boundary: **no.** `:types` owns cross-cutting identity
  values today; PLAN-OPS-2 owns authority; each domain owns its own policy version.

Two of three conditions fail. The correct outcome is "a small shared contract plus consumer-specific
decision implementations", which is precisely the smaller architecture the planning document says to
preserve rather than manufacture a subsystem around.

### On the existing `agents/` module

RF: `product/agents/` currently contains `SalesAgent`, `SalesAgentConfig`, `AgentObservation`.
Nothing in this investigation justifies growing it into an agent framework, and nothing requires
migrating `SalesAgent`. It is one concrete decision-making component, correctly placed. The
recommendation is that **"agent" remain an informal label for such components and never become a
platform concept name** — the word already carries five distinct meanings in this repository.
## 12. Hypothesis verdicts

### H1 — `Agent` may not be a platform primitive → **SUPPORTED (refined)**

**Decisive evidence.** The structural argument is internal: **cases B and E cannot both be
satisfied by any model that fixes the actor↔controller relation.** In B the actor necessarily *is*
the decision source; in E it necessarily *is not*. Four independent external traditions reached the
same decomposition without contact: PROV (Agent / Association / Delegation / Plan / Role), BPMN+DMN
(Participant and Performer vs Decision vs Business Knowledge Model vs Knowledge Source), Unreal
(PlayerState / Controller / Pawn — three independent framework types, no unifying `Agent` class),
and RFC 8693 (`sub` / `act`). The holonic-manufacturing reference architecture states the point
directly: it "refrains from decision-making", treating decision-making as a pluggable, non-reified
layer rather than a shared base type (Valckenaers 2020, *Computers in Industry* 120:103226).
And Arcogine's own executable evidence closes it: a human and `SalesAgent` already perform the same
operation on the same subject through the same vocabulary, with no agent ontology anywhere.

**Refinement.** A world-facing concept *does* survive — but it is **attribution**, not agency. PROV's
`Agent` is defined purely as "something that bears some form of responsibility", with no autonomy,
cognition, or lifecycle, and needs exactly three kinds (Person, Organization, SoftwareAgent) to
cover all six proving cases. Arcogine should adopt that concept and **not** adopt that name.

**Steelman that did not survive.** An `Agent` primitive would be justified if other participants had
to reason *about* a participant's declared intent for the interaction protocol itself. That reduces
to an optional, provenance-tagged declared-intent record attached to an actor — not a base type.

---

### H2 — decision-source internals are not common world semantics → **SUPPORTED (strengthened)**

**Decisive evidence.** Wooldridge (2000), *JAAMAS* 3(1):9–31, established that agent-communication
semantics grounded in mental state cannot be verified by an outside party; Singh's parallel critique
states it plainly — "the mental concepts cannot be verified without access to the internal
construction of the agents", therefore "a purely mentalistic semantics of an ACL cannot be a
normative requirement." Corroborated across traditions: PROV's `hadPlan` is deliberately
uninterpreted; a behaviour-tree tick exposes only {Success, Failure, Running}; HTN compound-task
decomposition never touches the world; DMN gives Knowledge Sources **no execution semantics** at all.

**Strengthening — the hypothesis understates its own case.** Turpin et al. (NeurIPS 2023) show
chain-of-thought explanations can "systematically misrepresent the true reason for a model's
prediction"; Lanham et al. (2023) find stated reasoning may be post-hoc. Recording internals as
world semantics would not merely be unnecessary — it would manufacture **false provenance**.
EU AI Act Articles 12 and 14 independently confirm the boundary: they require records of operation
and of the human-oversight decision, and by omission do not require internal reasoning.

**No proving case required a decision source's internals to become shared semantics.**

---

### H3 — actor attribution survives controller replacement → **SUPPORTED (refined)**

**Decisive evidence.** Unreal Engine is the sharpest case: a `Pawn` (body) can be possessed and
unpossessed at runtime by a `PlayerController` or an `AIController` (decider), and the durable
"who is accountable" record — name, score, team — lives in a **third** object, `APlayerState`,
precisely because controllers are swappable. That is a three-way split, independently invented.
RFC 8693 §1.1 supplies the normative rule: in delegation "principal A still has its own identity
separate from B", unlike impersonation where A "is indistinguishable from B"; §4.1 adds that
authorization considers only the current actor while prior actors "are informational only". PROV's
`actedOnBehalfOf` adds that the delegator "retains some responsibility for the outcome". MOISE+ and
OperA separate role-specification from role-occupant by construction.

**And a security-grounded reason the split is not optional:** Hardy's confused deputy (ACM SIGOPS
OSR 22(4), 1988) is exactly what happens when the authority exercised is the *running code's*
rather than the *requester's*. RF: Arcogine has textbook ambient authority today —
`Scheduler.schedule(Event)` accepts any payload from anything holding the reference.

**Refinement (important).** Attribution survives controller replacement **when the controller is
modelled as a decision source**. When the controller is itself a bearer of responsibility — a
vendor's autonomous system, an external organization — it becomes a **delegate actor**, and the
principal is preserved by the on-behalf-of relation instead. The platform must express both and
force neither. The test is: *is this mechanism itself a bearer of responsibility?* An in-house
pricing rule is not; a supplier's ERP is.

---

### H4 — replaying a decision is not re-executing its source → **SUPPORTED (refined)**

**Decisive evidence.** The four operations collapse into one only for deterministic sources — which
is why case A alone was never sufficient evidence. For a learned model, re-execution fails **in
principle**: PyTorch's own documentation states "Completely reproducible results are not guaranteed
across PyTorch releases, individual commits, or different platforms." For an external organization,
re-invocation is not merely unreliable but **harmful** (Fowler, Event Sourcing, "External Updates").
For a human there is no source to re-execute at all. The universal resolution — convert
nondeterminism into recorded input — is reached independently by record/replay debugging (rr),
state-machine replication (Schneider 1990), event sourcing, and production workflow engines
(Temporal, Azure Durable Functions), whose Activity results are recorded in history and returned
verbatim on replay.

**Refinement.** RF: **Arcogine already holds this answer.** ADR-0015 §2 places *ordered external
commands* in the durable reproducibility tuple. A non-re-executable decision source's output *is* an
ordered external command. What is missing is not a replay mechanism but **attribution on those
commands**. Temporal's versioning/patching mechanism is the same problem as "decision-source
material version", already solved the same way.

---

### H5 — a capability may be temporally extended → **REFINED**

**The phenomenon is real; the shared-concept question answers "no".**

**Decisive evidence.** In the options framework (Sutton, Precup & Singh, *AI* 112, 1999) an option is
agent-side and the base environment receives only primitive actions; HTN compound-task decomposition
is planner-internal and only primitive operators touch the world; PDDL 2.1 (Fox & Long, JAIR 20,
2003) reduces a durative action to start effects, end effects and an invariant over the interval —
an ordinary process with a lifecycle. A behaviour tree's `Running` status is the *entire* externally
meaningful content of an in-progress durative action.

**Real nuance, not hand-waved.** A durative action *does* enter the environment model when the
process's own dynamics are semi-Markov — which Arcogine's changeover and repair examples genuinely
are. But that is *resource state plus a work-item lifecycle*, which Arcogine already models.

**RF: Arcogine has already solved this once.** ADR-0010 gives one accepted `Order` (aggregate
identity) deterministically materializing N child `Job`s, each independently dispatchable, with the
aggregate owning completion and only the terminal child emitting `OrderCompleted` carrying both
identities. `PerformChangeover` needs the same four things — aggregate identity, child identity,
correlation, completion rule — and no fifth concept.

**Disagreement recorded.** Smith's JAIR commentary rejects PDDL 2.1's reduction, arguing durative
actions need irreducible primitive status. The dispute concerns planner-internal representation, not
what the acting layer exposes; Ghallab, Nau & Traverso (2016) separate exactly those layers. It does
not change Arcogine's answer.

---

### H6 — agent-specific communication is not assumed → **SUPPORTED (decisively)**

**Decisive evidence, verified directly from the specifications.** FIPA's *ACL Message Structure
Specification* (SC00061G) reached status **Standard** on 2002-12-03. Its companion *Communicative
Act Library* (XC00037H, 2001-08-10) — the document that actually gives messages their meaning —
remained **Experimental**, and states that meaning in modal belief operators over feasibility
preconditions and rational effects (`B_i(Done(a) → FP(a))`). **The transport envelope stabilized;
the mentalistic semantics never did.** Wooldridge (2000) explains why: the sincerity conditions such
semantics presuppose are formally unverifiable by an outside party.

**Convergent modern evidence.** The interoperability protocols that actually succeeded for
autonomous software are ordinary typed operation interfaces: MCP is JSON-RPC with typed tools,
resources and prompts, containing no belief/goal/intention primitive; A2A pairs a capability
descriptor with an asynchronous task lifecycle, its participants interacting "without needing access
to each other's internal state, memory, or tools."

**Refinement.** Should cross-participant semantics ever be needed, the verifiable form is a **public
commitment** — an obligation between parties about a subject, observable and challengeable from
outside. RF: Arcogine already runs one in production. An accepted `Order` *is* a public commitment.

---

## 13. Remaining unknowns

Only uncertainties that genuinely need further evidence or a deliberate decision.

1. **Is identity + version sufficient for a *stateful* decision source?** A controller that learns
   online or accumulates memory changes materially without any version changing. Version alone may
   not identify the material source; an instance or epoch identity may be required. No proving case
   forced this. **Needs a concrete consumer before deciding.**

2. **Does actor identity need a lifecycle — merge, alias, supersede, retire?** People change roles,
   organizations are acquired, services are replaced. **AI: this is the same shape as ADR-0013's
   unresolved durable operational-history identity question** ("what makes two records belong to the
   same one versus different ones?"). The two should be answered with one lifecycle/equality
   discipline rather than separately.

3. **Whose authority applies when several mechanisms contribute to one decision?** RFC 8693 answers
   the *chain* case (nested `act`); it does not answer the *ensemble* case (two policies vote, or a
   human overrides a recommendation). The planning document raises this explicitly and no proving
   case resolved it. **Genuinely open.**

4. **Does Challenge adopt a shared actor identity or keep its opaque-reference convention?**
   `ChallengeAttempt` has no player identity today, and `EvaluationProvenance` deliberately uses
   opaque string references to avoid module dependencies. That convention keeps the boundary clean
   but cannot answer a cross-lifecycle question like "everything this person did". **A product
   question (does attributed multiplayer matter?), not an architecture question yet.**

5. **Where does a durable attributed request actually live?** ADR-0011 §2 establishes Arcogine is
   **not** event sourced, and §8 states `EventLog` is "not a durable supported recovery journal".
   The supported runtime stream carries authoritative *transitions* only, by design. So a durable
   record of *requests with attribution* has no home in the current contracts. **This is the one
   genuine implementation gap the investigation found**, and it is a persistence-boundary question,
   not an agency question.

---

## 14. Architecture implications

**None of this is done here. This section says what should eventually change, and what should not.**

### Maintained architecture (`docs/architecture/overview.md`)
- The enduring principle "Agents only use approved command interfaces and never mutate simulation
  state directly" is correct but names the wrong subject. It is a rule about *any* decision source,
  and the Charter already says so. Restating it in actor/operation terms would remove the last place
  where "agent" reads as a platform concept. **Low priority, editorial.**
- The *Decisions* subsection could record that a decision and the operation it requests are one
  attributed fact, not two. **Only once something implements it.**

### The agency investigation plan (`docs/planning/agency-decision-boundary.md`)
- §2's working shape should merge *Decision/intent* and *Semantic Operation* into one attributed
  request, and demote *Observation* and *Decision source* from stages to references carried by it.
- §5 question 15 is answered: **roles are properties of a participation, not of an identity** — so
  one identity may occupy several roles without duplicate identities, and roles stay distinguishable
  when occupied by different entities.
- §9's exit criteria 1–8 are all met by this report.

### Existing ADRs
- **ADR-0013** (Proposed, on architecture-review hold): its unresolved question — what makes two
  records belong to the same continuing history — has the *same shape* as actor-identity lifecycle.
  Worth noting so both are resolved with one discipline. It already lists `actor identity` among
  the identities its durable identity must not collapse into; that remains correct.
- **ADR-0011 §4** (Accepted): its rule that `RunId` "must never participate in simulation
  decisions... or any other deterministic outcome" is the governing precedent for actor identity.
  Worth citing rather than re-deriving.
- **ADR-0015 / ADR-0016**: no change. ADR-0015 §2 already contains the H4 answer; ADR-0016 §3's
  evidence-provenance-class distinction is the same instinct applied to evidence.

### A new ADR
**Not yet, and deliberately.** This investigation introduces no hard-to-reverse decision — it
identifies a boundary and rejects several designs. An ADR becomes appropriate when **PLAN-OPS-2
commits to the actor/authority contract**, because that commitment *is* hard to reverse. Writing one
now would freeze a contract with no implementing consumer, which is the failure mode the repository's
own ADR policy exists to prevent.

### Future implementation planning
- **PLAN-OPS-2 should consume this result rather than re-derive it.** Its own readiness plan already
  warns that actor/capability ownership "must not be forced into Operational merely because it is the
  first consequential consumer" — this report supplies the vocabulary that lets it comply.
- **Disambiguate "capability" before implementing it anywhere.** The word carries five distinct
  meanings inside this repository and three incompatible ones outside it, one of which (the
  object-capability sense: an unforgeable reference fusing designation and authority) is a security
  term that will be read into any authorization work.
- **Stop adding stringly-typed attribution.** Three already exist. A fourth would make the eventual
  consolidation strictly harder, at no benefit.

### One unrelated finding, surfaced because it turned up
**ADR-0015 is Accepted and its §10 requires `EngineSemanticsVersion` to be mandatory on
`RuntimeObservationMetadata` and `RuntimeEventEnvelope`. `grep -rn EngineSemanticsVersion product`
returns zero hits.** This is an accepted-but-unimplemented provenance gap. It has nothing to do with
agency; it was found while establishing the decision-source-version baseline, and is reported here
rather than silently dropped.
