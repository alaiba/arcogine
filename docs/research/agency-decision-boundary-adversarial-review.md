# Agency and Decision Boundary — Independent Adversarial Review

> **Research status:** ACTIVE
> **Review baseline (live `main`):** `5d010e2aa79e36ca6a9793ebf305ba309e3da5a7`
> **Reviewed artifact:** `docs/research/agency-decision-boundary-report.md` @ `bf744af171d5a42a3c578d33836e8e45bd33b0fe` (branch `docs/agency-and-operational-identity-research-reports`)
> **Also consulted:** `docs/research/agency-and-operational-identity-synthesis.md` @ the same commit
> **Reviewed report's own baseline:** `2acee74723aad7a9ca384459f5d44ef35b85b6fd`
> **Review date:** 2026-09-09
> **Risk classification:** HIGH — cross-domain identity/equality, responsibility, provenance, authority, and prospectively persisted/public contracts
> **Authority:** Research evidence only. This document is not accepted architecture, not an ADR, not product direction, and not implementation commitment. Nothing here is promoted by having been written.
> **Disposition:** `ACCEPT WITH QUALIFICATIONS` (§15)

**Independence statement.** This review was executed in a fresh, isolated session with no
authorship of, and no responsibility for preserving, the reviewed report's conclusion. It is a
different run from the authoring run and from the run that produced the accompanying synthesis. It
is *not* verified to be a different model family from the authoring run, and this reviewer cannot
establish that from repository evidence. Under `docs/development/researching.md` §9's preference
order this satisfies condition 2 ("at minimum, a fresh isolated session with no responsibility for
preserving the original conclusion") but not condition 1. The pre-report reconstruction in §2 was
produced and fixed before the reviewed report was read, as that section's anchoring-control
procedure requires.

Evidence categories are labelled **RF** (repository fact), **EE** (external evidence),
**INF** (inference), **REC** (recommendation).

---

## 1. Bounded question

> Which Agency/Decision Boundary conclusions survive an independent attempt to falsify them, and
> what is the narrowest durable semantic contract Arcogine may safely reconcile into architecture
> later?

Reviewed against the brief at `docs/research/agency-decision-boundary.md` (live `main`), which is
semantically unchanged since the reviewed report's baseline — the only diff is its lifecycle status
(`Proposed bounded architecture investigation` → `READY`), its relocation from `docs/planning/` to
`docs/research/`, and one delivery-coordinate cleanup.

Out of scope, per the review's own mandate and the brief's non-goals: designing any authorization
model, choosing identity technology, creating any production type, and performing architecture or
ADR reconciliation.

---

## 2. Independent pre-report reconstruction

Produced from current repository authority before the reviewed report's recommendation was read.
Preserved here unedited in substance so it can be compared against what the report concluded.

### A. What problem actually exists?

Not "Arcogine lacks an `Actor` type." The defect visible in current `main` is narrower:

> **Arcogine has no stated rule distinguishing (i) who is answerable for an action, (ii) what
> mechanism produced or recorded it, and (iii) where a datum came from.**

**RF.** Six "source"-shaped concepts exist with six different meanings and nothing saying they
differ: `RevisionRecorder.source`, `RevisionRecorder.subject`, `ChangeProvenance.source`,
`RequirementSource` (sealed native/external *governing publication authority*),
`ExternalChangeReference` (workflow association), and `EventPayload.AgentDecision(String)`
(narrative). Meanwhile the Accepted runtime contract (`RuntimeEventEnvelope`, ADR-0011) carries
**zero** attribution, and `Event`/`EventPayload` carry none either.

**INF.** The cost of this gap is deferred, not present. It becomes a safety problem only once
execution can have external consequence — Charter "Safety scales with consequence"; Proposed
Operational architecture §16.1 "Absence of authority is denial, not implicit permission."

### B. Candidate semantic models (generated independently)

- **M1** No new shared semantics yet; everything consumer-local.
- **M2** Platform `Agent`.
- **M3** Attribution vocabulary *rule* only, no types.
- **M4** Shared responsibility-bearing actor identity; roles/delegation/authority owned where consumed.
- **M5** Full actor ontology (reference + kind + role + delegation + decision-source + authority).
- **M6** Attributed operation request as the primary shared durable causal boundary.
- **M7** Distinct durable decision and operation records.
- **M8 — discovered independently, not in the brief's or the report's candidate set.**
  *Provenance-slot rule with per-owner types*: any durable record answering "who is answerable /
  why does this exist" must carry a **typed, discriminated** provenance value, but the type is
  owned by the recording domain. The shared contract is the *discrimination requirement*, not a
  shared type. **RF:** the repository already demonstrates exactly this shape — `RequirementSource`
  is a sealed native/external discrimination owned by Governance; `AffectedEntityRef` (Factory) and
  `ChangedEntityRef` (Governance) are two domain-owned subject references coexisting deliberately
  with no shared `SubjectRef`; `EvaluationProvenance` (Challenge) references Engine identities by
  opaque string specifically to avoid a module dependency.

### C. Which semantic roles need to be distinguishable?

Reasoned case-by-case rather than assumed:

- responsible party vs. immediate actor — **yes** (an import process records; an engineer is answerable);
- principal/delegator — **suspected over-split**; may collapse into responsible party;
- decision source/controller — **yes** (`EvaluationPolicyIdentity`, `AssertionVersion` exist precisely because "which rules produced this" is separately versioned);
- subject/body — **yes**, but already solved twice, domain-locally; distinguishable ≠ shared type;
- operation / realization / transition — **yes**, and this is *already repository authority* (Charter §7; Proposed Operational §6), not a new Agency finding;
- observation source — **yes**, and *already* ADR-0016 (Proposed) evidence-provenance classes.

**INF (recorded pre-report, and it turned out to matter).** A large share of the "surviving
distinctions" any Agency report would produce are pre-existing repository authority. Agency's
genuinely *new* contribution is therefore narrower than the role list implies, and a report that
lists them all as its findings will read as broader than its actual evidence.

### D. Proving cases derived independently as discriminators

Import-process vs. responsible engineer; external telemetry source; rejected approval with no
operation; advisory optimizer with human override; Challenge attempt (a module with **zero**
project dependencies by construction); `SalesAgent` (no identity today, `EventPayload` sealed); one
controller serving many actors; actor whose kind is unknown or changes.

### E. Durable-decision threshold (independent)

- A prose **rule** is cheap to state and cheap to revise → architecture doc at most, **no ADR**.
- A **type in `:types`** is hard to reverse (every module, ADR-0012 serialization, persisted
  governance history, public API) → needs an ADR → therefore needs a real consumer first.
- A **closed enum** is the hardest of all to reverse (serialization compatibility, ADR-0012) →
  highest bar of anything in this question.
- **Negative claims** ("no platform `Agent`", "no new track") constrain nothing that anyone is
  building → **no ADR is justified for them at all.**

---

## 3. Source-report load-bearing claims

Only the claims whose failure would change the answer:

1. A platform-level `Agent` concept is unnecessary and harmful (independent cardinalities).
2. A responsibility-bearing **Actor** concept survives across cases.
3. Actor, decision source/controller, and subject/body are distinct **roles**; one identity may occupy several.
4. **On-behalf-of** delegation preserves responsibility; "authorization consults the current actor only."
5. **Role** is a property of a participation, not of an identity.
6. **Decision-source reference** = identity + resolved immutable material version, internals private.
7. Decision-source internals never become common world semantics.
8. **Authority** is relational (actor × subject × operation), not an actor-owned permission list.
9. **A decision and the operation it requests are one durable fact** — an *attributed operation request*.
10. Re-executing the source ≠ replaying the decision ≠ replaying the operation ≠ replaying the transition.
11. No new capability/procedure type for temporally extended behaviour.
12. No agent-communication ontology.
13. No new Agency module or delivery track.
14. **An actor-reference value type (namespaced identity + kind) is the one genuinely cross-cutting new type**, belonging in `:types`, with "four existing consumers."
15. **Subject reference is already implemented as `AffectedEntityRef`.**
16. **Observation reference is already available as `(runId, latestEventSequence)`.**
17. Actor identity is attribution metadata and must never affect deterministic outcome.

---

## 4. Repository re-grounding

### 4.1 Current facts established for this review

**RF — attribution surface.**
- `SalesAgent` has no identity, no version, no capability. `decide()` is a pure function over
  `AgentObservation`. On acting it schedules **two** events at the same instant:
  `EventPayload.PriceChange(double)` and `EventPayload.AgentDecision(String)`, the latter a
  formatted narrative (`"SalesAgent: backlog=%d, price %.2f -> %.2f"`).
- `EventPayload` is a **sealed** interface; `Event` is `(SimTime, EventType, EventPayload)` with no
  attribution field.
- `RuntimeEventEnvelope` (ADR-0011, Accepted) carries `runId`, `sequence`, `simulationTime`,
  `eventType`, `modelFingerprint`, optional `controlledRevisionId`, `affectedEntityRefs`, `payload`
  — and **no actor**. It is explicitly post-authoritative publication.
- `AgentObservation` carries **no provenance whatsoever** (six domain scalars).

**RF — the "source" fields, from their actual call sites.**
- `RevisionRecorder("governance-test", "operator-17")`, `RevisionRecorder("test", "operator")`:
  the **`source`** slot holds a *recording mechanism/channel*; the **`subject`** slot holds the
  person-like identifier.
- `ChangeProvenance.of("engineer", "add second machine")`: `source` holds a **role label**, not an identity.
- `RevisionRecorder` is **persisted** (`FileControlledRevisionAuthority` writes both fields into the
  durable binary revision record and reads them back) and **participates in idempotency equality**
  (`existing.provenance().recorder().equals(candidate.provenance().recorder())`).

**RF — subject references.** Two exist, deliberately: `AffectedEntityRef` (`com.arcogine.factory.process`,
**sealed** to `OrderRef`/`JobRef`/`MachineRef`) and `ChangedEntityRef` (`com.arcogine.governance.change`,
`(entityType, entityId, label)` with label excluded from equality).

**RF — module boundaries.** `:agents` depends only on `:types` and `:simulation`. An executable
ArchUnit guard, `sim_agents_must_not_depend_on_sim_factory`, forbids `com.arcogine.agents..` from
depending on `com.arcogine.factory..`. `consumer/challenge` has **no `project(...)` dependency at
all** — not even `:types` — enforced by construction and documented in its build file.
`:types` currently holds entity ids, `ControlledRevisionId`, `ModelFingerprint`, `Quantity`,
`RunId`, `SimError`, `SimTime`, and scenario types.

**RF — mechanism identity/version patterns.** `RequirementId`/`RequirementVersion(int)`,
`AssertionId`/`AssertionVersion(int)`, `EvaluationPolicyIdentity(String id, String version)` —
three shapes, three namespaces, and their Javadoc *explicitly* declares each independent of the
others and of `ModelFingerprint`/`ControlledRevisionId`. `EvaluationProvenance(publishedModelReference,
runReference)` crosses a module boundary by **opaque string** on purpose.

**RF — `EngineSemanticsVersion` has zero occurrences in `product/`** on live `main`, while
ADR-0015 (Accepted) requires it. The reviewed report surfaced this; it is re-verified here and
still true.

### 4.2 Accepted vs. Proposed

**Accepted:** ADR-0008 (controlled revision identity/lineage), ADR-0010, ADR-0011, ADR-0012
(external interchange/serialization boundaries), ADR-0015. **Proposed:** ADR-0013 (durable
operational identity, on explicit architecture-review hold), ADR-0016 (governance evidence
provenance). **Proposed architectural reference, not accepted architecture:**
`docs/architecture/operational-execution-digital-twin.md` and
`docs/architecture/governance-conformance.md` — both carry `Status: Proposed architectural reference`.

This matters: the authority-question shape "May actor A perform operation X on subject S under the
applicable policy?" is **Proposed**, not settled, and the document states it "approximately."

### 4.3 What changed since `2acee747`

Five commits. Materially:

- **`docs/development/researching.md`, `.github/agents/researcher.agent.md`,
  `docs/research/report-template.md`, and `docs/research/README.md` did not exist when the reviewed
  report was written.** The report therefore predates the repository's normative research method,
  its report template, and its lifecycle/promotion register.
- The Operational readiness plan was rewritten from ~424 lines to 48 and is now
  **`Status: NOT ADMITTED — no Operational implementation slice is currently safe to execute`**.
  Every Operational planning coordinate it contained was **removed**, including the
  "Actor, trust, authority, and capability boundary" slice the reviewed report repeatedly cites as
  already scheduled and already owned.
- Research briefs moved `docs/planning/` → `docs/research/`; the Agency brief's substance is unchanged.
- Factory `ResourceDefinition` collapsed into `ConfiguredResource` (unrelated to this question).

### 4.4 Stale report statements

| Report statement | Status on live `main` | Affects the semantic conclusion? |
|---|---|---|
| An Operational actor/trust/authority slice is "already scheduled" and owns Authority | **Void.** The slice and its coordinate were deleted; the plan is NOT ADMITTED and names actor/capability ownership as a thing that must be resolved *before* any slice | **Yes** — it is one of the two reasons the report gives for rejecting "do nothing now", and the sole occupant of the Authority row in its ownership table |
| That slice is one of "four existing consumers" for an actor type | **Void** | **Yes** — see §7.2 |
| Cites `docs/planning/agency-decision-boundary.md` | Relocated to `docs/research/` | No |
| `EngineSemanticsVersion` is one of three implementations of the policy-version pattern | **False.** Zero occurrences in `product/` — the report's own §14 says so without correcting §2 | **Partly** — the pattern is implemented twice, not three times |
| Charter, ADR-0011/0012/0013/0015/0016, Operational and Governance architecture claims | Re-verified; unchanged | No |
| `RevisionRecorder`/`ChangeProvenance` are bare strings; `ChallengeAttempt` has no player identity | Re-verified; still true | No |

**Additional consequence.** The reviewed report carries a `PLAN-*` delivery coordinate on 12
separate lines (13 occurrences), and the synthesis carries more. Under the rule now in `AGENTS.md` and enforced by
`.github/scripts/check-delivery-labels.py`, any such token outside `docs/planning/` is a
durable-naming leak, and `docs/research/README.md` states research documents never receive
temporary delivery coordinates. Those tokens must not be carried into any durable artifact derived
from the report. This review deliberately contains none.

---

## 5. Candidate models

The independent set is §2.B (M1–M8). After reading the report, one model was added to the
comparison:

- **M9 — Adopt W3C PROV directly as the domain model.** The report generated this independently
  and rejected it. This review agrees with the rejection and with its reasoning (PROV is purely
  retrospective; ADR-0012 makes Arcogine contracts authoritative and external representations
  projections; `standards-alignment.md` already places PROV as a Governance *projection* candidate).
  Verified independently in §9.

The report did **not** consider **M8** (typed, discriminated, domain-owned provenance slots). This
matters: M8 delivers the report's own "stop adding stringly-typed attribution" recommendation
*without* committing to a shared value type, a namespace, an equality rule, or a kind taxonomy —
i.e. without the four things §7.1, §7.2 and §7.13 show are unsettled. It is the model the
repository has already converged on four times independently. Its omission is a real gap in the
report's candidate set, though it does not reverse the report's direction — M8 is *more*
conservative than the report's recommendation, not less.

---

## 6. Proving-case matrix

Cases 1–6 are the brief's; 7–18 are the mandated adversarial variants. Columns show which
candidates survive. `M2` (platform Agent) fails so uniformly it is shown only where it fails
distinctively.

| # | Case | Actor | Principal | Decision source(s) | Subject | Separate durable decision? | Candidates that fail |
|---|---|---|---|---|---|---|---|
| 1 | `SalesAgent` | **none today**; the component is not an identity | — | the pure `decide()` rule + `SalesAgentConfig` | pricing state (**not expressible as `AffectedEntityRef`**) | no — `AgentDecision(String)` is narrative, not a record | M5 (nothing consumes kind/role/delegation); claim 15; claim 16 |
| 2 | Human planner/supervisor | the person | often (role/organization) | the person's judgement, opaque | the pending request or the resource | for approve/reject: see #10 | M2 (actor *is* the source) |
| 3 | Organization / external system | the organization or system | possibly | **opaque; must not be invented** | external subject via explicit correspondence | no | M2 (invents unobservable internals); M9 |
| 4 | Embodied NPC / simulated worker | the participant | rarely | any game-AI technique, private | the body, distinct from the actor | no | M2 (fixes actor↔body) |
| 5 | Deterministic planner/dispatcher | the many resources it acts for, separately | the operator | the planner + resolved version | each dispatched resource | no | **M2 decisively** (one Agent cannot be both #2's identity and #5's non-identity) |
| 6 | Nondeterministic / learned controller | the deploying party | usually | model identity + immutable digest | modelled subject | **yes — recorded output is required**, since it is not re-derivable | M1 (loses replay input) |
| 7 | Human using optimizer advice, deciding | the human | possibly | **two**: recommender (advisory) + human (authoritative) | the resource | **the recommendation is durable and is not the human's request** | claim 9 (universal form); claim 6 (singular source) |
| 8 | One decision → many operations | one | — | one | many | no — correlation under an aggregate suffices (ADR-0010 pattern) | none |
| 9 | Many sources → one operation | one | possibly | recommender + approver + policy check | one | **contested** — the *approval* is durable in Proposed Governance architecture | claim 6 (singular source); claim 9 |
| 10 | Denied / no-op decision | the decider | — | one | — | **yes for standing authorization and risk acceptance** (see §7.4) | **claim 9 in its universal form** |
| 11 | Service acting for an organization | the service | the organization | the service's policy + version | modelled subject | no | M2 |
| 12 | Import process; responsible party is elsewhere | the human/organization who authored the change | — | possibly none | the imported artifact | no | **any model that reads `RevisionRecorder.source` as the actor** |
| 13 | External telemetry / data source | **none — it is not an actor** | — | — | — | no | any model that promotes a data source to actor (ADR-0016 already forbids) |
| 14 | Controller changes, identity does not | unchanged | unchanged | changes | unchanged | no | M2 (fuses them) |
| 15 | Opaque external org, no source version | the organization | — | **must not be invented**; adapter/profile version instead | external subject | no | claim 6 as an unconditional rule |
| 16 | Online-learning / stateful controller | the deploying party | usually | **identity + version does not identify the material source** | modelled subject | recorded output required | **claim 6 as written** |
| 17 | One controller, many actors | many, separately accountable | — | one, shared | many | no | **M2 decisively** |
| 18 | One identity in several roles | same identity | — | same identity | possibly same identity | no | any model requiring distinct identities per role |

**What the matrix separates.** Cases 2 + 5 + 17 jointly kill **M2** — no single concept can fix the
actor↔controller relation both ways. Cases 7, 9, 10 break **claim 9's universal form** while
leaving its common-case form intact. Cases 15 and 16 break **claim 6 as written**. Cases 1 and 13
show attribution is not always present and must not be manufactured. Case 1 alone falsifies
**claims 15 and 16**.

---

## 7. Mandatory challenge results

### 7.1 Is an `ActorKind` taxonomy required?

**Challenge:** what Arcogine invariant requires actor *kind* in foundational identity rather than
in separate classification/trust/directory/participation metadata?

**Evidence.** **EE (verified in session, W3C PROV-DM, `https://www.w3.org/TR/prov-dm/`):** the
three agent types are explicitly non-exhaustive — "It is useful to define some basic categories of
agents *from an interoperability perspective*. There are three types of agents that are common
across most anticipated domains of use; **it is acknowledged that these types do not cover all
kinds of agent.**" **EE (verified, OMG BPMN 2.0 PDF, §"Pool"):** a BPMN Participant "can be a
specific partner entity (e.g. a company) or can be a more general partner role (e.g. a buyer,
seller, or manufacturer)" — a mature process standard permits the participant to be an *unresolved
role*, not a typed entity. **RF:** no current Arcogine consumer reads a kind. Kind participates in
none of equality (nothing compares actors), authorization (none exists), provenance
(`RevisionRecorder` persists two untyped strings), trust (no trust model), or serialization
(no actor is serialized). Only display might use it, and display is not identity. **RF:** ADR-0012
(Accepted) makes serialization boundaries an explicit compatibility concern; a closed enum in a
persisted contract is the hardest thing in this question to reverse.

**Result:** **`ActorKind` is not established.** Deriving a closed Arcogine enum from PROV's
subclasses inverts the source's own disclaimer. Among the three formulations tested,
`ActorId = opaque identity only` is the only one current evidence supports;
`identity + optional extensible classification metadata` is a defensible later step;
`identity + closed kind` is not supportable now.

**Effect on source report:** narrows claim 14. The report's actor reference is specified twice as
"namespaced identity + kind"; the "+ kind" half must be removed.

**Qualification required:** no reconciliation may introduce an actor kind, and none may cite PROV's
`Person`/`Organization`/`SoftwareAgent` as evidence that one is needed.

---

### 7.2 Is a shared Actor value type justified **now**?

**Challenge:** test the consumer evidence rather than accepting the type because it is useful.

**Evidence.** The report claims "four existing consumers": `RevisionRecorder`, `ChangeProvenance`,
"a future simulation attribution", and the Operational actor/authority slice. **RF:** the fourth
was deleted from `main` (§4.3); the third is explicitly hypothetical. The remaining two are **two
fields in one module** (`:governance`) — and per §7.3 one of them (`RevisionRecorder.source`) is
not an actor at all. Two fields in a single module is evidence of one module's local need, not of a
cross-cutting type.

**RF — decisive structural finding.** The report refuses to extract a shared decision-source type
with the argument that doing so "would force `challenge` — which deliberately has **no**
`project(...)` dependency — to acquire one. **The extraction would violate an existing, deliberate
ownership boundary.**" That argument is correct. It applies **identically** to an actor type in
`:types`, because the report simultaneously asserts that `ChallengeAttempt` "will need actor
attribution as soon as there is more than one player." The report does not notice that it has
applied its own ownership test to one type and exempted the other. **RF:** Challenge's existing
answer to exactly this problem is `EvaluationProvenance(publishedModelReference, runReference)` —
opaque strings, no dependency. There is no evidence Challenge would adopt a `:types` actor type;
there is direct evidence it would not.

**Do the claimed consumers share one semantic identity contract?** No. Governance's recorder is
persisted and equality-bearing in immutable history; simulation attribution would be transient and
governed by ADR-0011's non-event-sourced contract; Challenge's would be a game-owned player
identity with its own namespace; a future Operational actor would need verified identity, trust
roots, credential lifecycle, and revocation (Proposed Operational §5) that none of the others need.
Their equality, namespace, lifecycle, federation, and trust semantics differ materially.

**Result:** **a shared Actor value type is not justified now.** The *concept* survives (§7.12,
§11); the *type* does not.

**Effect on source report:** claim 14 does not survive as written. This is the review's second most
consequential finding.

**Qualification required:** the concrete trigger for implementation is *two consumers in different
modules that demonstrably require the same equality and namespace rule, at least one of which is
committed implementation work* — not two fields in one module plus two hypotheticals. Until then,
the surviving instruction is the negative one the report also gives: **stop adding new untyped
attribution**, which M8 satisfies without a shared type.

---

### 7.3 `source` is not automatically `actor`

**Challenge:** are the repository's "stringly attribution" examples three manifestations of one
missing concept?

**Evidence — RF, from actual call sites, not field names.**

| Existing field / concept | Current semantics (from usage) | Responsibility-bearing actor? | Decision source? | External/data source? | Safe future migration |
|---|---|---|---|---|---|
| `RevisionRecorder.source` | recording **mechanism/channel** (`"test"`, `"governance-test"`) | **No** | No | No | **None.** Persisted + equality-bearing. Additive only |
| `RevisionRecorder.subject` | the **person-like identifier** (`"operator-17"`) — the closest thing to an actor in the repository, and it is named `subject` | **Partly — yes in intent** | No | No | **None without a rename that breaks a persisted format.** Name collides with "subject = thing acted upon" |
| `ChangeProvenance.source` | a **role label** (`"engineer"`), not an identity | **No — a role, not a party** | No | No | Additive; could later carry a typed value beside the role |
| `ChangeProvenance.reason` | human narrative | No | No | No | None |
| `ExternalChangeReference(system, identifier)` | association to an external workflow record; "never revision or ChangeSet identity" | No | No | Governing workflow record | None — already correctly bounded |
| `RequirementSource` (sealed) | **governing publication authority** — `ExternalRequirementSource(authority, designation, edition, locator, adoptionProfile)`; `authority` means *publishing body* (IEC, ANSI/ISA) | No | No | Normative external source | None — already typed and correct |
| `EventPayload.AgentDecision(String)` | formatted **narrative** of a price move | No | No | No | None — it is a log line, not attribution |
| `EvaluationProvenance` | opaque cross-module references to model/run | No | No | No | None — deliberate |

**Result:** **the three examples are not semantically equivalent, and one of them is the inverse of
what the report implies.** In `RevisionRecorder`, the *actor-ish* value lives in the field named
`subject` and the field named `source` holds the mechanism. `ChangeProvenance.source` holds a
*role*, not a party. `AgentDecision` is narrative. A mechanical migration of `source` fields to an
actor type would actively corrupt provenance by recording a test harness as the responsible party.

**RF — reversibility.** `RevisionRecorder` is written into and read from the durable binary
controlled-revision record and participates in idempotent re-acceptance equality. Changing its
shape is a persisted-format migration on immutable governance history governed by ADR-0008.

**Note also** the terminology collision this exposes: Arcogine already uses `subject` to mean
roughly *who*, while the reviewed report and the Proposed Operational architecture use `subject` to
mean *what is acted upon*. Any reconciliation that adopts the report's vocabulary without resolving
this will make the existing persisted field unreadable.

**Effect on source report:** materially narrows its framing claim ("the repository has already
reinvented attribution three times as free-form strings"). Three of the report's own strongest
motivating data points are three *different* things. The direction — stop adding untyped
attribution — survives; the premise that a single missing concept explains them does not.

**Qualification required:** no reconciliation may describe these fields as instances of one
concept, may recommend migrating them, or may treat `RevisionRecorder.source` as an actor.

---

### 7.4 Does "decision + operation request = one durable fact" survive?

**Challenge:** attack the report's most consequential structural simplification.

**Evidence.** The report's counter-example handling is: reject *is* an operation on the approval
item; a denied request is `CommandResult.Rejected`; "decided not to act" is source-private; one
decision → many operations is correlation. Cases 8 and the rejection case do survive that
treatment, and the DMN observation (a `Decision` with no actor; attribution lives outside it) is
correct and useful.

**But the report did not consult `docs/architecture/governance-conformance.md`, which already
specifies durable decisions that request no operation.** **RF (Proposed architecture, §9 and §10):**

```text
Authorization decision/evidence required by applicable change-control policy
(e.g. ApprovalRecord, standing authorization, pre-approved standard change,
 emergency justification, automated policy)

Exception / RiskAcceptance
    finding or control
    rationale
    owner/approver
    effective period
    expiration
    compensating controls where applicable
```

and: "An exception does not rewrite the underlying assertion into success. It records an
**authorized governance decision** about a known non-conformance."

Three of these are not operation requests under any reading:

- a **standing authorization** authorizes a *class of future operations*; it precedes and outlives
  every operation it permits, and there is no single request it could be folded into;
- a **RiskAcceptance** with an effective period, an expiration, and compensating controls is a
  durable decision that a non-conforming *state may persist* — nothing is being requested;
- **case 7's advisory recommendation**: when a human reads an optimizer's proposal and issues a
  *different* request, the recommendation is a real, attributable, durable fact that the human's
  attributed request does not contain. Folding it in loses the fact that advice was given and not
  taken — which is precisely what the Charter's "because of which decision" is for.

**A second, internal problem.** The report's collapse mechanism for approve/reject requires "the
pending request" to be a durable, identifiable subject. Its own §13 unknown 5 states that a durable
record of requests-with-attribution **has no home in current contracts** (ADR-0011 §2/§8: Arcogine
is not event sourced; `EventLog` is not a durable journal). The collapse therefore rests on an
artefact the report itself says does not exist — a hidden infrastructure assumption of exactly the
kind `docs/development/researching.md` §9 directs a reviewer to find.

**Result:** the **universal** claim is **falsified**. The **narrow** claim survives and is valuable.
Precisely:

> **Survives:** Arcogine does not need a universal `Decision` object, and an attributed operation
> request is the preferred *first and common* durable boundary for a decision that results in a
> requested change.
>
> **Does not survive:** that a decision and the operation it requests are *universally* the same
> durable fact. A domain may own a distinct durable decision/recommendation/approval record where a
> real consumer requires one — and Proposed Governance architecture already names three such records.

**Effect on source report:** claim 9 must be restated in the narrow form. This is the review's most
consequential finding, and the reason the disposition is not a clean `ACCEPT`.

**Qualification required:** any reconciliation must state the narrow form, and must not assert that
Governance's approval/exception/risk-acceptance records are operation requests.

---

### 7.5 Does actor identity really never affect deterministic outcome?

**Challenge:** the report asserts actor identity must never affect deterministic outcome while also
proposing authority as a relation over actor × subject × operation. Construct: A may operate machine
M, B may not; otherwise identical requests; authority accepts one and rejects the other. Actor
identity has now changed system behaviour.

**Evidence.** The report resolves the *dispatch* variant well — a requester that should influence
scheduling must appear as a modelled payload field, never be smuggled in through attribution
metadata. It does **not** address the authority variant, which is not smuggling: it is the intended
behaviour of the very mechanism the report proposes.

**RF — the transplant is a category error.** ADR-0011 §4's rule governs `RunId`, which names an
execution epoch and has no policy role; it exists so that *which run this is* cannot leak into
results. Actor identity is a modelled participant with an intended policy role. The rules are not
the same rule, and the report's "same rule ADR-0011 §4 already imposes" over-reaches.

**Result:** the claim survives only in a restated, two-part form:

> 1. **Incidental attribution metadata must never affect outcome.** Attribution attached to a
>    request for provenance must not reach any domain, dispatch, pricing, or simulation computation.
> 2. **Explicit modelled authority is a legitimate exception.** Actor identity may affect behaviour
>    *only* through an authority determination that is explicitly modelled, whose outcome is
>    recorded, and which is itself a reproducible input on replay — never through an implicit read
>    of attribution by domain logic.

Part 2 is what makes authorization possible at all; without it the report's own claim 8 is
unimplementable.

**Effect on source report:** claim 17 narrowed, and its ADR-0011 precedent citation withdrawn.

---

### 7.6 Is authority really only actor × subject × operation?

**Challenge:** treat the triple as a candidate, not an ontology.

**Evidence.** **RF:** the triple is already in the repository, as *Proposed* architecture, and the
document itself hedges — "The reusable semantic question is **approximately**: May actor A perform
operation X on subject S under the applicable policy?" The same section then lists further
requirements for real consequence (claimed vs. verified identity, trust roots, credential
lifecycle, least privilege, revocation/expiry, physical safety, fail-safe behaviour). Proposed
Governance architecture adds change-control policy, standing authorization, emergency
justification, and effective periods. Proposed Operational §16 adds fail-safe-under-uncertainty.
Delegation, participation role, organization, controlled revision, time, system state, purpose,
trust level, operation parameters, policy version, and obligations are all plausibly in scope and
none is excluded by evidence.

**On "authorization consults the current actor only" — EE, verified in session (RFC 8693,
`https://www.rfc-editor.org/rfc/rfc8693.html`):** the actual text is *"For the purpose of applying
access control policy, **the consumer of a token** MUST only consider the token's top-level claims
and the party identified as the current actor by the `act` claim,"* with prior nested actors
"informational only." **But the same RFC defines `may_act`**, which "makes a statement that one
party is authorized to become the actor and act on behalf of another party" and which the
*authorization server* evaluates when issuing the token. So the delegation relation **is**
authorized — at a different point in the protocol. The report imported one half of a two-part
mechanism and generalized it into a universal rule.

**Result:** the triple survives as **the useful question shape**, not as an input schema. "Current
actor only" is an **RFC-specific token-consumer rule** and at most a useful default for a
downstream enforcement point; it is **not** a universal Arcogine authority invariant, and stating
it as one would erase the delegation-authorization step that the same RFC requires.

**Effect on source report:** claim 8's question shape survives; the borrowed rule inside claim 4
does not.

**Qualification required:** reconciliation may record the question shape and must record that it is
a question shape. It must not record "authorization consults the current actor only."

---

### 7.7 What does delegation mean for responsibility?

**Challenge:** does "on behalf of" necessarily mean the delegator retains responsibility?

**Evidence.** **EE (verified, PROV-DM):** "Delegation is the assignment of authority and
responsibility to an agent ... to carry out a specific activity as a delegate or representative,
**while the agent it acts on behalf of retains some responsibility for the outcome** of the
delegated work." The report's quotation is accurate. **But** PROV is *defining its own term*: this
is what PROV's `actedOnBehalfOf` means, not a discovered truth about responsibility. **EE (verified,
RFC 8693):** distinguishes delegation (A keeps its own identity while representing B) from
impersonation (A is indistinguishable from B) — a genuinely different relation the report mentions
but does not carry into its concept list.

Separating the strands the cases actually require: **causal chain** (who invoked what),
**acting party** (whose identity performed it), **represented principal** (for whom),
**accountability** (who answers for it), **authorization** (whose permission was evaluated), and
**provenance** (what is recorded). PROV fuses accountability into the delegation relation by
definition; that is a modelling choice appropriate to a provenance interchange vocabulary, and it
is **not** self-evidently right for transfer-of-responsibility, impersonation, or
approval-then-execution-by-another, all of which are legitimate cases (18, 11, 9).

**Result:** what survives is narrow and non-legal:

> An **acting party** and a **represented principal** may differ, and where they do, both must
> remain recoverable from the record. Whether the principal retains accountability is a
> **policy question owned by whichever domain models the relationship** — not a property of a
> generic identity or delegation type.

**Effect on source report:** claim 4 narrowed. Retained-responsibility must be labelled as PROV's
definition, not as an Arcogine invariant, or a legal/organizational responsibility model becomes
an accidental part of a generic identity concept.

---

### 7.8 Can one decision-source reference represent ensembles?

**Challenge:** the report speaks of *the* decision source; test ensembles and composites.

**Evidence.** Cases 7, 9 and 18 all produce more than one contributing source (recommender +
approver; policy engine + learned model; tools invoked by a controller; quorum). The report's own
§13 unknown 3 concedes this: "RFC 8693 answers the *chain* case (nested `act`); it does not answer
the *ensemble* case ... **Genuinely open.**" Yet §10 concept 4 and §11's ownership table state a
singular decision-source reference without that qualification, and §7's minimum-record table lists
"decision-source identity" as a single required value.

**Result:** the durable requirement is the weaker one:

> **Preserve sufficient provenance about the materially relevant decision-production context when a
> consumer requires it** — not "one decision-source reference."

**Effect on source report:** internal inconsistency. The unknown is correct; the confident
formulation in §10/§11 is not. The qualification must be promoted into the contract rather than
left in a late unknowns section where a reconciliation author may not carry it forward.

**Separate research question?** **Not yet** (see §12). The Agency question *can* responsibly settle
this by declining to commit to a singular reference — a refusal is a settlement. No generic
provenance graph is warranted.

---

### 7.9 Does every material decision source need a durable version?

**Challenge:** test "identity + resolved immutable material version."

**Evidence.** The report handles more of this than the challenge presumes, and that should be
recorded rather than re-litigated: its minimum-record table already says decision-source identity
is "usually n/a" for a direct human act and **"must not be invented"** for an opaque external
participant (adapter/profile version instead), and its immutability argument (a mutable model alias
can silently come to mean a different model) is sound and matches `ModelFingerprint`'s
content-derived design and ADR-0015 §12's never-reused released versions.

The unhandled case is **case 16**: an online-learning or otherwise stateful controller has *no*
immutable material version — it changes materially with no version changing. The report's own §13
unknown 1 identifies this and says an instance or epoch identity may be required; its §7 table
nevertheless demands "required, resolved & immutable" for nondeterministic controllers, which such
a source cannot satisfy.

**Result:** the shared invariant is **consumer-specific, with one universal negative**:

> Arcogine must be able to explain **which mechanism was relied on**. The *exact immutable version*
> is required only where the consumer's own reproducibility contract requires it, and **must never
> be fabricated** where the participant cannot supply one. Stateful/online-learning sources remain
> explicitly unresolved.

**Effect on source report:** claim 6 narrowed; the mandatory-version formulation does not survive as
a universal rule.

---

### 7.10 Is `(RunId, latestEventSequence)` a shared observation reference?

**Challenge:** the report says an observation reference is "already available" in this form.

**Evidence — RF, decisive and executable.**

- `AgentObservation` — the decision input in the report's **own primary proving case** — carries no
  `runId`, no sequence, no provenance of any kind. `AgentObservationProjector` builds it from six
  scalars.
- `RuntimeObservationMetadata(runId, modelFingerprint, currentTime, runState, latestEventSequence)`
  lives in `com.arcogine.factory.process` and is the ADR-0011 **consumer-neutral external**
  observation boundary — a different boundary from the in-tick decision input, which
  `docs/architecture/overview.md` explicitly distinguishes ("domain observation vs. API/UI snapshot").
- The ArchUnit rule `sim_agents_must_not_depend_on_sim_factory` **forbids** `com.arcogine.agents..`
  from depending on `com.arcogine.factory..`. The claimed reference is therefore not merely absent
  from the decision boundary — it is **unreachable from it by an enforced guard**.
- It also cannot serve a human on an API view, Governance evidence (ADR-0016 keeps external
  observations independently provenanced), raw operational telemetry, an external ERP request, or
  an asynchronous external service. Proposed Operational architecture explicitly requires raw
  external observations to retain **independent** source and time provenance and *not* invent
  Arcogine run identity.

**Result:** **falsified.** Arcogine has **several domain-owned provenance references satisfying a
common rule** — "a decision record should identify the input boundary that bounded it" — not a
universal observation-reference semantic. The tuple is an Engine cursor and must not become a
cross-cutting Agency abstraction because it happens to fit one case that cannot even use it.

**Effect on source report:** claim 16 does not survive. The *epistemic-containment* idea it supports
(a decision should be explainable from the observation its source was given) survives as a rule.

---

### 7.11 Is `AffectedEntityRef` the universal subject reference?

**Challenge:** verify its owner, scope, namespace, and consumers.

**Evidence — RF.** `AffectedEntityRef` is a **sealed** interface in `com.arcogine.factory.process`
permitting exactly `OrderRef`, `JobRef`, `MachineRef`. Its Javadoc scopes it to correlation of
entities affected by a `RuntimeEventEnvelope` — i.e. entities affected by an **already-succeeded
transition**, which is not the subject of a *request*. Testing it against the required cases:
pricing singleton — **not representable** (pricing is `:economy`; `RuntimeEventType` has no pricing
member at all); machine/resource — yes; purchase order — yes; external physical asset —
**no** (Proposed Operational requires explicit correspondence, never identity inference);
organization — **no**; modelled worker body — **no**; governed semantic artifact — **no**, and
Governance already has its own `ChangedEntityRef(entityType, entityId, label)` for exactly this.

So: the report's **own canonical proving case cannot express its own subject** with the type the
report says already implements subject reference; the report's approve/reject collapse needs "the
pending request" as a subject, which this sealed type also cannot express; a second subject
reference already exists in another module; and the ArchUnit guard of §7.10 makes it unreachable
from `:agents` regardless.

**Result:** **falsified.** `AffectedEntityRef` is a Factory-owned, transition-scoped correlation
type that *illustrates* the required distinction. Declaring it universal would invert dependency
direction (Governance and Challenge depending on Factory) for no invariant.

**What survives:** the semantic rule **"subject is distinct from actor"** — which needs no universal
`SubjectRef` type, and which the repository already honours twice.

---

### 7.12 Distinct identities, or only distinct roles?

**Challenge:** state the two claims separately.

**Evidence.** Cases 2 and 18 (one identity as actor and decision source), 4 (NPC as actor and body),
5 and 17 (dispatcher deciding for many separately accountable actors), 14 (controller changes,
identity does not), 11 (service for organization). No case required an extra identity object; every
case required the roles to stay tellable apart. **EE (verified, PROV-DM):** role attaches to the
qualified association, not to the agent — the same answer. **EE (verified, BPMN 2.0):** a
Participant may be a partner *role* rather than a partner entity, which is the same separation from
the process side.

**Result, stated separately as required:**

> **Roles must remain distinguishable — universal.** Actor, decision source/controller, and
> subject/body are distinct semantic roles in every case examined.
>
> **Identities must be distinct — case-specific.** One identity may occupy several roles. Nothing
> in the evidence requires manufacturing separate identity objects to keep roles apart.

**Effect on source report:** claims 3 and 5 **survive intact**. This is the strongest surviving part
of the report, and it answers the brief's §5 question 15 correctly.

---

### 7.13 Does actor identity have a known lifecycle/equality rule?

**Challenge:** before recommending any shared actor value type, is enough settled?

**Evidence — RF.** Nothing in the repository answers who issues actor identities, what defines
equality, whether they may be federated or namespaced, whether renaming preserves identity, whether
an organization's identity survives personnel change, what happens when an external identity
provider rotates identifiers, whether aliases are distinct from identity, what retirement means, or
whether identities are local or global. The one place a person-like value is persisted
(`RevisionRecorder.subject`) is an unconstrained non-blank string that participates in equality by
raw string comparison. ADR-0013 (Proposed) has the structurally identical unresolved question for
operational-history identity and is **on explicit architecture-review hold** until it has "a precise
lifecycle/equality rule" — the repository's own precedent for what this bar looks like.

**Result:** **not settled, and not settleable by the Agency question.** Since a value type is
exactly a commitment to an equality rule, and ADR-0008/ADR-0013 show this repository treats
identity equality as ADR-grade, `ActorId` cannot responsibly be created now. This independently
reinforces §7.2 by a different route: even if consumers existed, the equality rule does not.

**Effect on source report:** confirms and *strengthens* the report's own §13 unknown 2, and
contradicts its §10/§11 recommendation to place an actor reference in `:types` now.

---

## 8. Existing `source` semantics audit

The classification table is in **§7.3** and is the required explicit deliverable. Its three
headline results, restated so a reconciliation author cannot miss them:

1. **`RevisionRecorder.source` is a recording mechanism, not an actor.** The actor-ish value in that
   record lives in the field named `subject`.
2. **`ChangeProvenance.source` holds a role label, not a party identity.**
3. **`RequirementSource.authority` means *publishing body*, not authorization authority** — a direct
   collision with the authorization sense of "authority" used throughout the Agency material.

`AgentDecision(String)` is narrative and is not attribution at all. No field in this table should be
migrated; `RevisionRecorder` in particular is persisted and equality-bearing in immutable governance
history under ADR-0008.

---

## 9. External-evidence verification

Re-verified in this session, not inherited:

| Source | Verified? | What it establishes | What transfers | Where the analogy breaks | What it does **not** establish |
|---|---|---|---|---|---|
| **W3C PROV-DM** (`w3.org/TR/prov-dm/`) | **Fetched and checked** | Agent = "something that bears some form of responsibility"; delegation retains responsibility *by PROV's definition*; retrospective scope | The responsibility-bearing definition; role on the qualified association | PROV is purely retrospective — no requested-but-unrealized operation, no authorization denial, no forward intent | **That Arcogine needs an actor kind.** PROV states its three types "do not cover all kinds of agent" and frames them as interoperability conveniences |
| **RFC 8693** (`rfc-editor.org/rfc/rfc8693.html`) | **Fetched and checked** | Token consumers apply access control using top-level claims + current actor; nested actors informational; `may_act` authorizes becoming an actor | Delegation ≠ impersonation; a delegation chain is provenance | It is an OAuth token-exchange protocol rule scoped to *the consumer of a token*; `may_act` shows the delegation relation is itself authorized elsewhere | **That "authorization consults the current actor only" is a universal authority invariant** |
| **OMG BPMN 2.0** (spec PDF) | **Fetched; text extracted and checked** | "A Pool is NOT REQUIRED to contain a Process, i.e., it can be a 'black box'"; a Participant may be a partner entity *or* a partner role | External participants are modelled by interaction, never by internals — the strongest support for case C and against a platform `Agent` | Notation for collaboration, not a domain ontology | That Arcogine needs BPMN constructs; also, by permitting a *role* as participant, it weakens rather than supports a required resolvable actor identity |
| **Singh, social semantics for ACLs**; FIPA status | **Background, not re-verified in session** | Mentalistic ACL semantics are unverifiable from outside | The verifiability argument, which the repository already reaches independently | — | Report's use is consistent with `standards-alignment.md`, which already places FIPA at "relevant only if…" |
| Sutton/Precup/Singh options; PDDL 2.1; HTN; behaviour trees | **Background, not re-verified in session** | Durative behaviour reduces to a lifecycle at the acting layer | Supports the no-new-capability-type conclusion | Planner-internal vs. exposed-interface distinction | Not load-bearing beyond a conclusion ADR-0010 already supports from repository evidence |
| Turpin et al. 2023; Lanham et al. 2023; EU AI Act Arts. 12/14 | **Background, not re-verified in session** | Stated reasoning can misrepresent actual cause; record-keeping obligations do not require internal reasoning | Supports the false-provenance argument | Regulatory scope ≠ architectural necessity | The conclusion does not depend on them — the repository reaches it from ADR-0016 and the Charter |
| MCP / A2A as typed-operation precedent | **Background, not re-verified in session** | Current interop protocols are typed operations, not ACLs | Corroborates the communication conclusion | Protocol adoption is not semantic necessity | Nothing load-bearing rests on it alone |

**Contrary evidence actively sought and found:** PROV's own non-exhaustiveness disclaimer (against
`ActorKind`); RFC 8693's `may_act` (against the universal current-actor rule); BPMN's role-as-participant
(against a required resolvable actor identity). All three cut against the report, and all three were
found in sources the report itself cited — the citations are accurate; the inferences drawn from
them over-reach.

**Absence-claim scope.** The searches behind "no current consumer reads an actor kind" and "no
equality rule exists" were: `git grep` over all tracked files for the semantic-neighbour term set in
the review mandate, plus direct reading of `:types`, `:governance`, `:agents`, `consumer/challenge`,
`domains/factory` process types, and the ArchUnit suite. This does not prove absence elsewhere in
the wider ecosystem; it does establish it for this repository at `5d010e2`.

---

## 10. Surviving invariants

Only what survived independent falsification. Every item below is a **semantic rule**; none is a type.

1. **No platform `Agent` concept.** Cases 2, 5 and 17 jointly make it unrepresentable: a human is
   necessarily both actor and decision source, while a dispatcher is necessarily neither-both. No
   invariant was found that the composition cannot express.
2. **Actor, decision source/controller, and subject/body are distinct semantic roles**, and role is a
   property of a *participation*, not of an identity.
3. **Roles must remain distinguishable; identities need not be distinct.** One identity may occupy
   several roles without duplication.
4. **A responsibility-bearing actor *concept* survives** across humans, organizations, software,
   planners, NPCs and external systems — as a concept, not as a value type, and not always present
   (an external data source is not an actor).
5. **An acting party and a represented principal may differ**, and where they do, both must remain
   recoverable. Whether the principal retains accountability is a domain policy question.
6. **Attribution, recording mechanism, decision source, and external data source are four distinct
   kinds of provenance** and must not be collapsed into one field or one concept. (Independently
   corroborated by ADR-0016's evidence-provenance classes.)
7. **Subject is distinct from actor** — without a universal subject type.
8. **Decision-source internals never become common world semantics.** Goals, beliefs, memory, plans,
   prompts, behaviour-tree state, search state, weights, and reasoning traces stay private. Recording
   a reasoning trace as authoritative provenance would manufacture **false provenance**. Legitimate
   exceptions are voluntarily recorded *public* statements — a declared rationale, a public
   commitment, an audit explanation, a policy identifier — which are records of what a party
   *asserted*, not of hidden internal state, and must never be presented as the operative cause.
9. **Arcogine must be able to explain which mechanism was relied on**, where materially relevant; an
   exact immutable version is required only by a consumer's own reproducibility contract and must
   never be fabricated.
10. **An attributed operation request is the preferred first and common durable boundary** for a
    decision that results in a requested change, and **no universal `Decision` type is needed** — but
    a domain may own a distinct durable decision/recommendation/approval record where a real consumer
    requires one.
11. **The four replay operations are distinct** — re-execute the source ≠ replay the recorded
    decision ≠ replay the requested operation ≠ replay the resulting transition — and the unifying
    rule is **convert nondeterminism into recorded input**, which ADR-0015 §2 already carries via
    ordered external commands.
12. **Incidental attribution must never affect outcome; explicitly modelled, recorded authority may.**
13. **The authority *question shape*** — may this actor perform this operation on this subject under
    the applicable policy — is a useful question shape and not an input schema.
14. **No shared temporally-extended capability type.** ADR-0010's aggregate/child pattern already
    provides aggregate intent, child identity, correlation, and a completion rule.
15. **No agent-communication ontology.** Ordinary typed operations, events, observations, results,
    and public commitments suffice; an accepted `Order` is already exactly such a commitment.
16. **No new Agency module, subsystem, or delivery track**, and "agent" should remain an informal
    label rather than becoming a platform concept name.
17. **W3C PROV is a vocabulary donor and projection target, not a domain model** — which is what
    ADR-0012 and `standards-alignment.md` already say.

---

## 11. Claims that do not survive as written

| Claim | Verdict | Action |
|---|---|---|
| Actor reference is the one genuinely cross-cutting new type, belonging in `:types` now, with four existing consumers | **Falsified as stated** — two fields in one module, one of which is not an actor; two "consumers" are void or hypothetical; Challenge cannot consume `:types` by construction | **Defer.** Keep the concept (§10.4); drop the type until the §7.2 trigger is met |
| Actor reference carries a **kind** | **Not established** — PROV disclaims exhaustiveness; no consumer reads it; ADR-0012 makes a closed enum hardest to reverse | **Remove** |
| Subject reference is already implemented as `AffectedEntityRef` | **Falsified** — Factory-owned, sealed, transition-scoped, cannot express the report's own case A subject or its approve/reject subject, unreachable from `:agents` by an executable guard, and duplicated by `ChangedEntityRef` | **Narrow** to the rule "subject is distinct from actor"; no universal type |
| Observation reference is already available as `(runId, latestEventSequence)` | **Falsified** — the actual decision input carries no provenance and is forbidden from reaching that type | **Narrow** to "a decision record should identify the input boundary that bounded it"; the reference stays domain-owned |
| A decision and the operation it requests are universally one durable fact | **Falsified in its universal form** — standing authorization, risk acceptance, and unfollowed advice are durable decisions that are not operation requests; Proposed Governance architecture already names them | **Narrow** to the first/common-boundary form in §10.10 |
| Decision-source reference is identity + resolved immutable version | **Falsified as a universal rule** — opaque external participants (which the report itself excepts) and stateful/online-learning controllers | **Narrow** per §10.9; keep the never-a-mutable-alias rule where a version exists |
| One decision-source reference | **Does not survive** for ensembles/composites | **Split**: preserve sufficient provenance about the materially relevant decision-production context; do not commit to a singular reference |
| "Authorization consults the current actor only" | **Over-generalized** — an RFC-specific token-consumer rule whose companion `may_act` authorizes the delegation itself | **Remove** from any durable statement |
| Delegation means the delegator retains responsibility | **Narrow** — that is PROV's definition, not an Arcogine invariant; impersonation and transfer-of-responsibility are different relations | **Narrow** per §10.5 |
| Actor identity must never affect deterministic outcome (citing ADR-0011's `RunId` rule) | **Narrow** — the precedent is a category error, and the unqualified rule makes authorization impossible | **Split** into the two-part rule in §7.5 |
| The policy identity + version pattern is implemented three times | **False** — `EngineSemanticsVersion` has zero occurrences in `product/` | **Correct to twice** |
| The repository reinvented attribution three times as free-form strings | **Materially narrowed** — the three are a mechanism, a role label, and a narrative | **Narrow** per §7.3/§8 |
| An Operational actor/authority slice is already scheduled and owns Authority | **Void** — deleted from `main`; the plan is NOT ADMITTED | **Remove.** Authority currently has **no** owner, which strengthens rather than weakens the case for doing nothing yet |

**Not a finding against the report, recorded for completeness:** its incidental
`EngineSemanticsVersion` gap (ADR-0015 Accepted, zero implementation) is **re-verified as true on
live `main`**. It is unrelated to Agency and belongs to whoever owns Engine provenance, not here.

---

## 12. Research questions exposed

**Actor identity lifecycle, equality, and federation — a separate bounded question is warranted.**
§7.13 demonstrates the Agency question cannot responsibly settle it: who issues identities, what
defines equality, whether they may be federated, and what rename/merge/retire mean are outside "what
is the minimum durable boundary among attribution, decision production, subject, capability,
operation, realization, and transition," yet a value type *is* a commitment to an equality rule, so
the type cannot be created until they are answered. **REC:** add one entry to
`docs/research/README.md`, and state in it that it should be answered with **one** identity
lifecycle/equality discipline shared with ADR-0013's unresolved durable operational-history identity
— not independently, which would risk two divergent disciplines for the same shape. This review does
not create that entry; adding a research question is a register change for the repository owner, and
this review's persistence mandate covers only the truthful lifecycle transition of the existing
question.

**Ensemble / multi-source decision provenance and authority — no separate entry yet.** §7.8 shows
the Agency question *can* responsibly settle this, by declining to commit to a singular
decision-source reference. A refusal is a settlement. There is no consumer, and opening an entry now
would be symmetry rather than need. The requirement is instead a **qualification** (§14): the narrow
contract must be written so it does not foreclose multi-source provenance later.

---

## 13. Durable consequence

**Ready for later architecture reconciliation — as semantic rules only, in
`docs/architecture/overview.md` and/or the Proposed Operational and Governance architecture
documents:** surviving invariants §10.1–§10.17, each in its narrowed form, plus the §8 audit result
so that no future author reads `RevisionRecorder.source` as an actor.

**Not ready for anything:** every type. `ActorId`, `ActorKind`, a shared `SubjectRef`, a shared
`DecisionSourceRef`, a universal `Decision`, and a shared `Capability` are all either unjustified by
consumers, unsettled in equality, or falsified above.

**No ADR is warranted now**, and this review reaches that independently of the report. An ADR records
a decision that constrains future implementation; §10's surviving content is a set of *refusals to
build* plus vocabulary discipline, which constrains nothing anyone is building. The threshold for an
ADR is the moment a real consumer commits to an actor/authority contract — and per §4.3 there is
currently no such committed consumer, because the Operational plan is NOT ADMITTED.

This review performs none of the above. Reconciliation is a separate change under its own
independent PR review.

---

## 14. Qualifications that must survive reconciliation

Binding contract for a later reconciliation author.

1. **Do not introduce any type.** No `ActorId`, `ActorKind`, `Decision`, `DecisionSource`,
   `Capability`, `SubjectRef`, or delegation type. Record rules; build nothing.
2. **Do not attach a kind to actor identity**, and do not cite PROV's agent subclasses as evidence
   for one — PROV states they do not cover all kinds of agent.
3. **State the decision/operation rule in its narrow form only** (§10.10). Never write that a
   decision and the operation it requests are universally one durable fact. Explicitly preserve that
   a domain may own a distinct durable decision/recommendation/approval record, and note that
   Proposed Governance architecture already names approval, standing authorization, and
   exception/risk-acceptance records.
4. **Do not claim `AffectedEntityRef` is a universal subject reference.** Record only "subject is
   distinct from actor," and note that two domain-owned subject references already coexist
   deliberately.
5. **Do not claim `(runId, latestEventSequence)` is a shared observation reference.** Record the
   input-boundary rule; leave the reference domain-owned.
6. **Do not state "authorization consults the current actor only."** If the authority question shape
   is recorded, record it as a question shape, hedged as the Proposed Operational architecture
   already hedges it, and not as an input schema.
7. **Do not make retained delegator responsibility an Arcogine invariant.** Record only that acting
   party and represented principal may differ and both must remain recoverable.
8. **Do not require a decision-source version universally.** Preserve "must not be invented" for
   opaque external participants, and record stateful/online-learning sources as explicitly
   unresolved.
9. **Do not commit to a singular decision-source reference.** Write the provenance rule so
   multi-source and ensemble provenance remain expressible later.
10. **State the actor-identity rule in two parts** (§7.5), and do not cite ADR-0011's `RunId` rule as
    its precedent.
11. **Do not migrate, rename, or reinterpret `RevisionRecorder.source`, `RevisionRecorder.subject`,
    `ChangeProvenance.source`, or `AgentDecision`.** `RevisionRecorder` is persisted and
    equality-bearing in immutable governance history. Any future attribution must be additive.
    Resolve the `subject` terminology collision explicitly before adopting the report's vocabulary.
12. **Do not describe an Operational actor/authority slice as scheduled or owned.** It was removed;
    the Operational plan is NOT ADMITTED; actor/capability ownership remains open and must not be
    forced into Operational.
13. **Do not carry any temporary delivery coordinate** from the source report or synthesis into a
    durable artifact.
14. **Do not mark this research `CONCLUDED`** on the strength of this review. `CONCLUDED` requires
    reconciliation to have actually happened, or a recorded no-action outcome.

---

## 15. Disposition

```text
ACCEPT WITH QUALIFICATIONS
```

The reviewed report's central conclusion survives: **no platform `Agent` is warranted; attribution
rather than agency is what Arcogine actually lacks; the smallest boundary is a set of semantic roles
plus consumer-owned mechanisms; no module, track, or ADR is justified now.** No omitted candidate
model reverses it — the one candidate the report missed (§5, M8) is *more* conservative than its
recommendation, not less.

It is not a clean `ACCEPT` because five load-bearing sub-claims are falsified as written (the
universal decision/operation collapse; the shared actor value type with its "four consumers"; the
actor kind; `AffectedEntityRef` as universal subject; `(runId, latestEventSequence)` as shared
observation reference) and four more require narrowing. It is not `REOPEN` because none of these
falsifies the report's answer to the bounded question — each narrows a sub-claim while the
conclusion and its direction hold. It is not `MORE EVIDENCE REQUIRED` because the surviving
invariants in §10 are supported by current repository evidence and re-verified external sources; the
evidence gap is confined to the questions §12 correctly defers.

**Independence, restated for the record:** a fresh isolated session with no responsibility for
preserving the reviewed conclusion, satisfying `docs/development/researching.md` §9's second
preference but not its first (a different model family was not established). A reconciliation author
who considers that insufficient for HIGH-risk promotion should obtain a different-model-family pass
before promoting anything in §13.

---

## 16. Reconciliation handoff

### Safe to promote

The seventeen surviving invariants in **§10**, each in the narrowed form stated there, as
**semantic rules in architecture prose** — plus the §8 `source` audit result, which prevents a
predictable future misreading. Nothing else.

### Must not be promoted

Every type (`ActorId`, `ActorKind`, `Decision`, `DecisionSource`, `Capability`, `SubjectRef`,
delegation types); any actor taxonomy; any authorization model, policy engine, RBAC/ABAC/XACML
structure, or delegation token; any provenance-graph framework; any agent communication bus or
conversation ontology; `AffectedEntityRef` as a universal subject reference; an Engine observation
cursor as a cross-domain reference; reasoning traces as authoritative provenance; any new module or
delivery track; any ADR; and any implementation planning admission.

### Mandatory qualifications

**§14**, items 1–14, in full. They are written to be checkable against a reconciliation diff.

### Next action

```text
architecture reconciliation without ADR
```

A single, narrowly scoped, independently reviewed documentation change recording the §10 invariants
and the §8 audit result in `docs/architecture/overview.md` and the Proposed Operational/Governance
architecture documents, preserving every §14 qualification — and nothing else.

Two follow-ups sit beside it and are **not** part of it: the recommended **actor-identity
lifecycle/equality/federation** research entry (§12), which should share one discipline with
ADR-0013's unresolved identity question; and the unrelated re-verified `EngineSemanticsVersion`
implementation gap, which belongs to Engine provenance ownership.

This review does not execute any of them.
