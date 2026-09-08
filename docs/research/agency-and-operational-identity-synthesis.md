# Research Synthesis — Operational Identity and Agency/Decision Boundary

**Read-only architecture synthesis · alaiba/arcogine · no repository changes made**

Reports synthesized:

1. *Arcogine — Agency and Decision Boundary Investigation* (uploaded report), baseline `2acee74723aad7a9ca384459f5d44ef35b85b6fd`
2. *The Accountable Continuation* — Operational Execution durable-identity research (produced earlier in this session), baseline `2acee74723aad7a9ca384459f5d44ef35b85b6fd`

Current repository baseline for this synthesis: `origin/main` @ `d7b55b08e53e54c4190097912c787ab8d7231c62` (4 commits ahead of both reports' baseline).

Evidence is labelled **REPOSITORY FACT (RF)**, **REPORT FINDING (RPF)**, **ARCHITECTURAL INFERENCE (AI)**, or **RECOMMENDATION (REC)**. Nothing is silently promoted between these categories.

---

## A. Executive verdict

**Accept, with qualification, both reports' central conclusions.** Neither requires reopening any Accepted ADR. Neither is contradicted by the four commits main gained since their shared baseline — those commits restructured *where* unresolved questions live (`docs/planning/` → `docs/research/` split, commit `dad40c7`) without touching any ADR or the operational-execution architecture document. Both reports' repository-fact claims were independently re-verified against current `main` and hold (`EngineSemanticsVersion` still has zero occurrences in `product/`; `RevisionRecorder`/`ChangeProvenance` are still bare strings; `ChallengeAttempt` still carries no player identity; ADR-0013 and the operational-execution architecture document are byte-for-byte unchanged).

**What is now settled, at decision quality:**

- Arcogine does not need a platform `Agent` type. Attribution — actor, decision-source version, observation reference, authority determination — is what was actually missing, not agency. A decision and the operation it requests collapse into one durable *attributed operation request*.
- ADR-0013's actual question has an answer with a precise referent, equality rule, continuity rule, and fork rule: the identity names one **accountable operational continuation**, preserved exactly while no record it has already accepted is lost and while exactly one continuation is extending it, replaced by a new identity (with mandatory lineage) whenever either condition fails.
- Both investigations independently converge on the same architectural pattern for any new identity: opaque, declared by an explicit act, bound immutably to its record, carrying explicit lineage, never reused, never inferred from infrastructure — i.e., ADR-0008's `ControlledRevisionId` discipline generalizes, and both reports reach for it independently without having read each other.
- Neither report's ownership recommendation creates a new module. Both place their one genuinely new value type in `:types`, beside `RunId`/`ModelFingerprint`/`ControlledRevisionId`.
- Raw external observations carry neither report's new identity — full agreement, and both reinforce ADR-0013's already-retained requirement rather than relaxing it.

**What remains genuinely unresolved** (neither report closes these, and this synthesis will not invent closure):

- Whether a future actor-identity lifecycle (merge, alias, supersede, retire) should be governed by the *same* discipline as the resolved operational-history identity, or an independently derived one. The agency report raises this explicitly as its own remaining unknown and calls it "the same shape" as ADR-0013's question; the operational-identity report never examines actor-identity lifecycle at all, and its neighbor table quietly assumes actor identity survives organizational transfer without asking what makes it the same actor. This is a real gap between the two reports, not a contradiction — see Conflict Matrix, item 3.
- Ensemble/multi-source authority (two policies vote; a human overrides a recommendation) — flagged and left open by the agency report, untouched by the other.
- Where a durable attributed-operation-request record physically lives — the agency report's self-identified "one genuine implementation gap" (ADR-0011 §2/§8 give the supported runtime stream no home for durable requests-with-attribution). This is an implementation-design question, not a semantic one, and both reports agree it should wait for a first real consumer.

**Highest-leverage repository action:** neither implementation nor a new ADR. Both research questions currently sit at register status `READY` despite decision-quality evidence now existing for both — that evidence has simply never been reconciled into the repository's own documents, which is precisely the gap `docs/research/README.md`'s promotion rule exists to close. The single highest-leverage action is to **revise ADR-0013 in place** using the resolved rule (§H below), after one independent adversarial re-review, since the operational-identity report's own coverage note discloses that its adversarial pass was self-administered — its four independent critique lanes failed on a session rate limit and were never run.

---

## B. Report-by-report assessment

| Report | Baseline status | Verdict | Core accepted conclusion | Main reservation |
|---|---|---|---|---|
| **Operational Execution / Digital-Twin durable identity** ("The Accountable Continuation") | **CURRENT** — baselined `2acee74`; zero diff to `HEAD` in ADR-0013, the operational-execution architecture doc, or any cited ADR (0004/0008/0011/0012/0015/0016 all `0` diff lines). The only drift is structural relocation (`docs/planning/operational-execution-digital-twin-readiness.md` was rewritten into a NOT-ADMITTED gate citing `docs/research/operational-execution-digital-twin-boundaries.md`), which does not contradict the report — it is the exact research artifact that new gate now points to. | **ACCEPT WITH QUALIFICATION** | Durable operational identity = one *accountable operational continuation*; established by an explicit act (genesis or fork); preserved iff no accepted record is lost and extension is exclusive; a new identity (mandatory lineage) is required on loss or on an exclusivity violation; convergence never restores equality; the identity attaches to Arcogine's own conduct/conclusions, never to raw observations. | Three necessary qualifications were only reached through the report's own self-administered adversarial pass (continuity is defined by record *loss*, not log prefix; exclusivity is *detectable*, never *provable at write time*; a divergence is *recorded*, never *relabelled*). The report is explicit that its four planned independent research/adversarial lanes never ran (rate-limited). Recommend one genuinely independent adversarial re-review of §6 before ADR-0013 text is finalized. |
| **Agency and Decision Boundary Investigation** | **NEEDS RECHECK** on two citations, **CURRENT** on substance. Baselined `2acee74`; every code-level and ADR-level repository fact it asserts was independently re-verified and still holds at `d7b55b0`. But it repeatedly cites **`PLAN-OPS-2`** as an already-named, scheduled delivery coordinate ("already scheduled", "the readiness plan already warns...") and repeatedly cites `docs/planning/agency-decision-boundary.md`. Commit `dad40c7` (after the report's baseline) removed all `PLAN-OPS-*` coordinates from the readiness plan, rewriting it into a NOT-ADMITTED implementation-admission gate, and relocated the agency document to `docs/research/agency-decision-boundary.md` (content preserved; §§1–10 numbering and text intact). | **ACCEPT WITH QUALIFICATION** | No platform `Agent` type; attribution (actor + decision-source identity/version + observation reference + relational authority) is the missing concept; one durable *attributed operation request* replaces the separate "decision" and "operation" objects; the only genuinely new shared value is an actor-reference type, owned by `:types`; no new module, track, or ADR is justified yet. | Fix the two stale citations before this becomes the register's authoritative reconciliation source (substance is unaffected — the "actor/trust/authority" question it points to still exists, now as an unnamed `CANDIDATE` item in `docs/research/operational-execution-digital-twin-boundaries.md`). Two of its own "remaining unknowns" (actor-identity lifecycle, durable-request persistence home) are genuinely open and should become tracked follow-ups rather than reservations buried in prose. |

---

## C. Cross-report semantic model

### Combined conceptual diagram

The two reports describe adjacent, non-overlapping halves of one picture. The agency report defines the *internal structure* of a durable record Arcogine produces when it acts; the operational-identity report defines *which durable account* that record belongs to. Composed:

```text
                    authoritative State
                            │
                            ▼
                      Observation ──────────────────────┐
                (purpose-specific;                       │ input reference
                 independently provenanced;               │ (runId, latestEventSequence)
                 carries NEITHER report's new identity)   │
                            │                             │
                            ▼                             │
                  Decision source ────────────────────────┤ identity + resolved
             (mechanism; PRIVATE —                        │ immutable version
              rule, judgement, planner,                   │ (per-domain: EngineSemanticsVersion,
              policy, model, black box)                   │  EvaluationPolicyIdentity, ...)
                            │                              │
                            ▼                              ▼
                Actor ───────────►  ATTRIBUTED OPERATION REQUEST  ◄─── Authority determination
        (+ on-behalf-of chain,           │        ▲                    (actor × subject × operation;
           role in THIS act)             │        │                     relational, not actor-owned)
                                          │        │
                                          │        └── carries, when accepted: durable operational-
                                          │            history identity — this request is Arcogine's
                                          │            own CONDUCT, so it belongs to exactly one
                                          │            accountable operational continuation
                                          ▼
                                    Realization
                        (synthetic apply / adapter / human work —
                         request ≠ acceptance ≠ outcome)
                                          │
                                          ▼
                                     Transition
                                          │
                    ┌─────────────────────┴──────────────────────┐
                    ▼                                             ▼
           back to State                          Correspondence assertion / Reconciliation
                                                   (Arcogine's own CONCLUSION — also belongs
                                                    to exactly one accountable continuation)
```

**The payoff of doing this synthesis is the join at "ATTRIBUTED OPERATION REQUEST."** Neither report names this join itself. The agency report supplies what the request *contains*; the operational-identity report supplies what the request *belongs to*. Both were reached independently, from unrelated evidence bases (PROV/BPMN/DMN/Unreal/RFC 8693 for one, PostgreSQL/Oracle/SQL-Server/DRBD/Temporal/Kafka/AAS/IEC-81346 for the other), and neither contradicts the other at the join.

### Concept-by-concept table

| Concept | Proposed owner | Agreement between reports | Relationship |
|---|---|---|---|
| **Identity/referent (operational)** | Operational Execution (concept); `:types` (value type, once implemented) | Only the operational-identity report addresses this | n/a |
| **Equality (operational)** | — | Only the operational-identity report | Same establishment act, no intervening fork |
| **Continuity (operational)** | — | Only the operational-identity report | No accepted record lost + exclusive extension |
| **Lineage/fork (operational)** | — | Only the operational-identity report | Mandatory parent + divergence point; 0..1 parent initially (mirrors ADR-0008) |
| **Actor/principal** | `:types` (value type) | Only the agency report | Bears responsibility for a participation; independent of the decider |
| **Decision source/controller** | Each domain, separately — deliberately not extracted | Only the agency report | Identity + resolved immutable version; private mechanism |
| **Capability** | Actor/trust/authority research (unowned) | Both reports treat as unresolved and correctly out of scope | Agency report: authority is relational (actor × subject × operation), not actor-owned |
| **Authority/authorization** | Actor/trust/authority research (`CANDIDATE`, formerly framed as `PLAN-OPS-2`) | Both reports agree it is a distinct, not-yet-owned concern | Operational-identity report lists it as "related but not dependent" on operational identity; agency report supplies its vocabulary |
| **Operation** | Existing command boundary (Engine `CommandResult`; operational architecture §6) | Both — full agreement, no change proposed | Requested operation ≠ accepted command ≠ actual transition, already accepted architecture |
| **Command/result** | Existing command boundary | Both — **terminology risk found here**, see Conflict Matrix item 4 | Agency: nondeterministic decision output *is* an "ordered external command" per ADR-0015 §2 (Engine/run-scoped). Operational-identity: "command identity" is one-to-many, belongs to exactly one operational history (real-world-consequence-scoped). Same word, two different scopes. |
| **Deployment** | Operational Execution (unimplemented) | Only the operational-identity report addresses it | Many-to-many with operational identity in both directions; never derived from either |
| **External subject** | Operational Execution (unimplemented) | Only the operational-identity report addresses it directly; agency report's case C references it via "authoritative correspondence" | Decisively not the operational-identity referent (multiple twins share a subject, not an identity) |
| **Raw observation** | Independently provenanced; no owner needed | **Full agreement** | Carries neither report's new identity; associated to a history/decision later, at interpretation time, never at ingestion |
| **Subject correspondence** | Operational Execution research (`CANDIDATE`) | Only the operational-identity report | A correspondence assertion is Arcogine's own conclusion → carries operational identity |
| **Reconciliation** | Operational Execution research (`CANDIDATE`) | Only the operational-identity report | Same: Arcogine's own conclusion → carries operational identity |
| **Temporal truth** | Operational Execution research (`CANDIDATE`, valid/effective vs. knowledge/recorded time) | Only the operational-identity report touches this, lightly | Left open in both; explicitly deferred in the operational-identity report's remaining unknowns |
| **Model/revision identity** (`ModelFingerprint`, `ControlledRevisionId`) | Governance (unchanged) | Both — full agreement, no change | Many-to-many with operational identity; a revision change is a fact recorded *within* a history, per the operational-identity report; agency report separately reuses ADR-0008's exact discipline as the template for actor-reference identity |
| **Runtime/run identity** (`RunId`) | Engine (unchanged) | Both — full agreement, no change | One-to-many/conditional with operational identity (operational-identity report); "must never affect deterministic outcome" is echoed independently for *actor* identity by the agency report, citing the same ADR-0011 §4 rule |

**What I checked and found no conflict on:** neither report assigns the same concept to two different owners (Governance vs. Operational, or Operational vs. Engine); neither collapses deployment identity into external-subject identity; neither lets `RunId`, replay, or fork semantics leak into the durable operational identity or into actor identity — both are explicit and careful about this exact boundary, which is reassuring given how heavily ADR-0011 and ADR-0013 warn against it.

---

## D. Conflict matrix

| # | Conflict/tension | Evidence | Impact | Recommended resolution |
|---|---|---|---|---|
| 1 | Agency report cites `PLAN-OPS-2` as an existing, named, scheduled coordinate. | RF: current `docs/planning/operational-execution-digital-twin-readiness.md` contains zero `PLAN-OPS-*` strings (verified by grep); it was rewritten by commit `dad40c7` into a NOT-ADMITTED gate. The substance the report was pointing at now lives, unnamed, as the "Actor, trust, authority, and capability — `CANDIDATE`" section of `docs/research/operational-execution-digital-twin-boundaries.md`. | Low. Substance intact; a reader following the citation into the repo today finds no such coordinate. | When reconciling the agency report into the research register, replace every `PLAN-OPS-2` reference with a pointer to the actor/trust/authority `CANDIDATE` research item. |
| 2 | Agency report cites `docs/planning/agency-decision-boundary.md` throughout. | RF: the document moved to `docs/research/agency-decision-boundary.md` in the same commit; §§1–10 and their content are intact (confirmed by direct read), only the path and a few status/link lines changed. | Low. | Update the path in any reconciled version of the report; no content is affected. |
| 3 | **Genuine open gap, not a contradiction.** Does actor-identity lifecycle (merge/alias/supersede/retire) share the *same* discipline as the resolved operational-history identity, or an independently derived one? | RPF: the agency report's own remaining unknown #2 calls this "the same shape as ADR-0013's unresolved durable operational-history identity question" and recommends "one lifecycle/equality discipline rather than separately." RPF: the operational-identity report never examines actor-identity lifecycle — its neighbor table (§8) states actor identity is "many-to-many" relative to operational history and that "organisational transfer... [is] recorded within it," which quietly presumes actor identity itself persists stably through such a transfer without ever asking what makes it the same actor. | Medium, deferred. No actor identity is implemented yet, so nothing is broken today. But if a future actor/authority ADR is drafted before this is resolved, it risks either re-deriving the discipline from scratch or silently borrowing operational-history identity's specific rule where a different one might be warranted (a revision is a *point*; an actor's participation history *accumulates*, closer in shape to the operational continuation than to a revision). | **Do not resolve here** — record it as an explicit open research item (see §F) rather than force a ruling neither report supports. |
| 4 | **Terminology collision risk.** "Command" is used for two different concepts across the two evidentiary bases the reports draw on. | RF: ADR-0015 §2 places "ordered external commands" in the Engine's durable *reproducibility* tuple (`ModelFingerprint + EngineSemanticsVersion + explicit workload + seed/random inputs + ordered external commands`) — this is `RunId`-scoped, about reproducing one simulation run. RF: `docs/architecture/operational-execution-digital-twin.md` §6, "External commands are not facts about reality," defines a *different* command lifecycle (`Operation requested → Validated → Authorized → Submitted → Accepted/Rejected → Executing → Succeeded/Failed/Unknown → Observed and reconciled`) for real-world consequence, which is what the operational-identity report's "command identity" (belongs to exactly one operational history) actually describes. The agency report uses the ADR-0015 sense when it says a nondeterministic decision source's output "*is* an ordered external command"; the operational-identity report uses the operational-execution sense. | Medium. Not a semantic error in either report — each is correct within its own scope — but a future engineer skimming both could conflate a run-scoped reproducibility input with a real-world command carrying operational-history identity. | A short maintained-architecture clarification (not a new ADR) distinguishing "ordered external command" (ADR-0015, `RunId`-scoped, reproducibility input) from the operational external-command lifecycle (operational-execution architecture §6, operational-history-scoped) before either concept is implemented. |
| 5 | Checked, no conflict found: **Governance vs. Operational ownership.** | Both reports place their one new value type in `:types` (neutral, cross-cutting), not in Governance or Operational. Neither assigns decision-source-version ownership to a shared module — the agency report explicitly keeps it per-domain (`EngineSemanticsVersion`, `EvaluationPolicyIdentity`, `RequirementVersion`) to avoid forcing `challenge`'s deliberate zero-dependency boundary to acquire one. | None. | No action needed. |
| 6 | Checked, no conflict found: **deployment identity vs. external-subject identity.** | Operational-identity report §8 keeps these explicitly separate and many-to-many in both directions; the agency report does not touch deployment identity at all. | None. | No action needed. |
| 7 | Checked, no conflict found: **run/replay/fork leaking into either durable identity.** | Both reports independently and explicitly forbid deriving their new identity from `RunId`, replay, or infrastructure; both cite ADR-0011 §4's "must never affect deterministic outcome" rule as governing precedent, applied to two different identities (operational-history identity in one report, actor identity in the other) without either report needing to see the other's argument. | None — this convergence is a *strength*, recorded as a durable invariant in §H. | No action needed. |

---

## E. ADR consequences

| ADR | Current status | Conclusion affecting it | Disposition | Why |
|---|---|---|---|---|
| **ADR-0013** (Durable operational identity) | Proposed, architecture-review hold | The operational-identity report resolves the exact "Unresolved decision" the ADR poses, with the three qualifications from §A/§B | **REVISE IN PLACE** | ADR-0013 was never Accepted, so per the repository's own ADR policy (`docs/architecture/decisions/README.md`) revision-in-place is correct — no supersession is needed. The revision must retain every currently-withdrawn item (no `ExecutionContextKind`, no replacement enum) and every retained requirement, replacing only the "Unresolved decision" section with the resolved referent/equality/continuity/fork rule, the record-loss (not log-prefix) formulation, the detectability-not-provability limitation on exclusivity, and the record-don't-relabel rule for divergence. |
| **ADR-0011** (Runtime observation and event contract) | Accepted | Both reports independently reaffirm and *extend* its §4 rule ("`RunId` must never participate in... any deterministic outcome") to a second identity each (operational-history identity; actor identity) | **LEAVE UNCHANGED** | Nothing in either report requires reopening an Accepted ADR; the convergence is worth stating as a standalone invariant in maintained architecture (§H), not as an ADR edit. |
| **ADR-0015** (Engine semantics identity and reproducibility) | Accepted | Its §2 reproducibility tuple is confirmed as already containing the agency report's H4 answer ("ordered external commands" = recorded nondeterministic decision output); Conflict Matrix item 4 flags a terminology-scope risk against it | **LEAVE UNCHANGED** | No semantic content is contested. The terminology clarification recommended in item 4 belongs in maintained architecture text (`operational-execution-digital-twin.md`), not in this ADR. |
| **ADR-0016** (Governance evidence provenance) | Proposed | Both reports note it applies "the same instinct" (distinguishing modelled/derived/observed/reconciled) to evidence that the operational-identity report applies to operational history | **LEAVE UNCHANGED** | Informative parallel only; neither report proposes an edit. |
| **ADR-0004 / ADR-0008 / ADR-0012** | Accepted | Both reports use ADR-0008's opaque/declared/immutable-binding/explicit-lineage/no-reuse discipline as the explicit template for their own new identity (operational-history identity; implicitly, actor-reference identity) | **LEAVE UNCHANGED** | Used as precedent/pattern donors only; no edit proposed by either report. |
| *(No new ADR for agency)* | — | Confirmed correct independently: no hard-to-reverse decision currently exists — no actor/authority contract has been committed to any implementation yet | **NO ADR YET** | Writing one now would freeze a contract with no implementing consumer, which is exactly the failure mode the repository's ADR-creation guidance (`docs/architecture/decisions/README.md`, "write one for decisions that meaningfully constrain future implementation") exists to prevent. The correct trigger is the moment a future actor/authority research item commits to a concrete contract. |
| *(No superseding ADR anywhere)* | — | Both revisions are to Proposed or to nothing | **CONFIRMED** | A superseding ADR is only appropriate for a later Accepted ADR replacing an earlier Accepted decision; ADR-0013 was never Accepted, so revision-in-place is the only correct mechanism per repository policy. |

---

## F. Research-register changes

Per `docs/research/README.md`'s lifecycle table, `CONCLUDED` requires both a decision-quality result **and** that durable consequences have been reconciled. Neither has yet been reconciled into any committed document (ADR-0013 text is unchanged; no verdict has been recorded against the agency document). Marking either `CONCLUDED` today would overstate what has actually landed in the repository, so this synthesis recommends the intermediate, honest status:

| Research item | Current register status | Recommended status | Reasoning |
|---|---|---|---|
| Durable operational identity (`operational-execution-digital-twin-boundaries.md`) | `READY` | **`ACTIVE`**, with `CONCLUDED` to follow immediately once ADR-0013 is revised | A decision-quality result now exists (this synthesis's §H); the sole remaining step — revising ADR-0013's text — is a concrete, well-defined act, not further research. Per the register's own precedent (Factory Resource Semantics: `CONCLUDED` only once its durable interpretation actually landed as a new architecture document), `CONCLUDED` should wait for that landing. |
| Agency and decision boundary (`agency-decision-boundary.md`) | `READY` | **`ACTIVE`**, with `CONCLUDED` to follow once a verdict is recorded in the register and (optionally, low-priority) `overview.md`'s wording is corrected | Same reasoning. The reconciliation cost here is smaller than ADR-0013's (a register entry plus an optional editorial fix, no ADR), but it has likewise not yet happened. |

**New follow-up items to add** (these are newly revealed by the reports, not part of either report's original question, and should not be used to keep the original questions open):

1. **Actor-identity lifecycle discipline** (merge, alias, supersede, retire) — `CANDIDATE`. Origin: agency report's remaining unknown #2. Cross-references the resolved operational-identity discipline as a candidate template, without presupposing they must be the same rule (Conflict Matrix item 3).
2. **Ensemble/multi-source authority attribution** (two policies vote; a human overrides a recommendation) — `CANDIDATE`. Origin: agency report's remaining unknown #3, explicitly left open by RFC 8693 (which only answers the delegation-chain case).

**Not recommended as new register items** (correctly out of scope per both reports and the planning/research admission rules):

- Persistence home for the durable attributed-operation-request / durable operational record — an implementation-design question, deferred to the first real consumer by both reports' own ownership sections.
- The "command" terminology clarification (Conflict Matrix item 4) — a documentation edit, not a research question.
- Stateful/online-learning decision-source identity, Challenge multiplayer actor adoption — both explicitly flagged by the agency report as needing a concrete consumer or product decision before they are even research-ready.

---

## G. Planning implications

**No implementation slice is admissible.** Checked directly against `docs/planning/operational-execution-digital-twin-readiness.md`'s six promotion criteria:

| # | Criterion | Status |
|---|---|---|
| 1 | Exact semantic referent of the proposed slice is settled | Nearly — the operational-identity referent now has a decision-quality answer, but it has not yet been written into any authoritative document. |
| 2 | Any required ADR is Accepted or otherwise no longer a blocker | **Not met.** ADR-0013 is still Proposed and textually unrevised. |
| 3 | Module/track ownership is known, no duplication | Reasonably settled in substance (`:types` for the value type, Operational Execution as conceptual owner, no new module) — but unratified in any authoritative document. |
| 4 | Prerequisites explicit and landed or fixture-backed | **Not met.** No durable operational record capability exists yet to test the rule against (both reports agree the type should wait for its first real consumer). |
| 5 | Failure/safety semantics explicit for consequential behavior | Partially — the operational-identity report's §10 gives semantic (not implementation-acceptance) treatment of split brain, stale restore, ambiguous failover. |
| 6 | Executable acceptance evidence defined | **Not met.** |

Two of six are unmet outright, and the readiness document's own status line ("NOT ADMITTED — no Operational implementation slice is currently safe to execute") is unaffected by either report. The narrowest next step, per both the readiness document and the operational-identity report's own ownership section, is the ADR-0013 revision itself — a documentation action with zero implementation content.

For the agency question: the actor/trust/authority research item is still `CANDIDATE`, not even `READY`, so it sits further from implementation admission than the operational-identity question does. The agency report's own conclusion — "almost nothing" should happen now beyond recording the boundary — is correct and is not disturbed by anything in this synthesis.

**No implementation roadmap is proposed here**, consistent with the constraint against inventing one from unresolved research.

---

## H. Proposed durable invariants

The smallest set of normative sentences that should survive into architecture/ADR material, stated without implementation mechanics:

1. Arcogine's durable operational-history identity names one **accountable operational continuation** — a body of Arcogine's own conduct and conclusions — and is preserved exactly while no record it has already accepted is lost and while exactly one continuation is extending it; any other continuation establishes a new identity carrying mandatory lineage to its source and divergence point.
2. A durable operational-history identity attaches only to what Arcogine did or concluded (commands, deployment records, correspondence assertions, reconciliations, drift analyses); it never attaches to a raw external observation, which retains independent source, time, and trust provenance.
3. Convergence of two divergent operational continuations is never identity equality; a merge, if ever supported, is recorded as a new identity whose lineage names both parents.
4. A violation of exclusive extension (two continuations extending one identity concurrently) is detected retrospectively by comparing recorded ancestry, never proven synchronously at the moment of writing, and is resolved by recording a divergence finding and assigning at most one continuation the right to extend the identity going forward — never by relabelling records already written.
5. No Arcogine identity that attributes responsibility for an action — actor identity included — may participate in a deterministic simulation outcome, extending the rule already established for `RunId` (ADR-0011 §4) to attribution generally.
6. Attribution (who or what is responsible for a requested operation, through what decision source and version, on what observation, under what authority) is a property Arcogine records about an operation request. It is not evidence for a general-purpose `Agent` concept, and a decision source's internal reasoning, beliefs, or state are never recorded as world semantics.
7. One entity may occupy more than one attribution role (actor, decision source, subject) within a single participation. Role is a property of the participation, not of the identity; the platform must neither assume these roles are always distinct nor that they are always equal.

---

## I. Remaining unknowns

**1. Architecture-blocking** (must be resolved before the relevant ADR/architecture text can be finalized):

- Whether actor-identity lifecycle shares ADR-0013's discipline or needs its own (Conflict Matrix item 3).
- Ensemble/multi-source authority attribution — blocks a future actor/authority architecture decision.
- An independent adversarial re-review of the operational-identity report's §6 rule, which the report's own coverage note discloses was never performed by anyone but the report's own author.

**2. Implementation-design** (do not block ADR/architecture acceptance, but must be settled before building):

- Where the durable attributed-operation-request / durable operational record physically lives (module, persistence boundary) — deferred to the first real consumer by both reports.
- How a fork's divergence point is represented (checkpoint reference, temporal boundary, designated record).
- Whether multi-parent lineage will ever be needed (deferred at 0..1 parent initially, mirroring ADR-0008's own initial contract).

**3. Representation/technology choices** (explicitly out of scope for research, per ADR-0013 itself and both reports):

- Final type name (`OperationalHistoryId` or otherwise) — both ADR-0013 and the operational-identity report agree naming should follow, not precede, the resolved semantics.
- UUID or other representation, persistence technology, canonicalization rules.

**4. Deferred optimization/convenience** (safe to leave alone indefinitely, or until a concrete trigger):

- Stateful/online-learning decision-source identity (needs a concrete consumer per the agency report).
- Whether Challenge adopts a shared actor identity for multiplayer attempts (a product decision, not an architecture one).
- The "command" terminology clarification (Conflict Matrix item 4) — worth doing, low urgency, no semantic risk until Operational's command lifecycle is actually specified.

---

## J. Recommended repository changes

Ordered, minimal, read-only recommendation — **none of these were performed**:

1. **`docs/architecture/decisions/0013-execution-context-identity.md`** — revise in place: replace the "Unresolved decision" section with the resolved referent/equality/continuity/fork rule and its three qualifications (§A/§H); retain every currently-withdrawn item and every retained requirement unchanged; keep `Status: Proposed` (acceptance remains the repository owner's decision, not this synthesis's).
2. **`docs/research/operational-execution-digital-twin-boundaries.md`** — update the "Durable operational identity" section's status line to `ACTIVE` (→ `CONCLUDED` once #1 lands), linking to the revised ADR-0013.
3. **`docs/research/README.md`** — update both register rows' `Status` column (`READY` → `ACTIVE`) and `Last reviewed` date; add the two new `CANDIDATE` rows from §F.
4. **`docs/research/agency-decision-boundary.md`** — append a verdict/result pointer per the research README's own maintenance rule ("record the verdict here and link the durable destination"); correct the stale `PLAN-OPS-2` and `docs/planning/agency-decision-boundary.md` citations in the reconciled report text.
5. **`docs/architecture/overview.md`** — optional, low-priority editorial fix (the agency report's own suggestion): reword "Agents only use approved command interfaces and never mutate simulation state directly" to name the actual subject (any decision source/actor), removing the last place "agent" reads as a platform concept.
6. **`docs/architecture/operational-execution-digital-twin.md`** — small clarifying addition disambiguating ADR-0015's run-scoped "ordered external commands" from the operational external-command lifecycle in §6 (Conflict Matrix item 4).
7. **`docs/planning/operational-execution-digital-twin-readiness.md`** — no change needed yet; its promotion criteria already correctly gate on ADR-0013 acceptance, which action #1 alone does not satisfy.

---

## Closing

**1. Recommended next repository action:** Revise ADR-0013 in place using the resolved rule in §H (with the three qualifications), routed through one independent adversarial re-review before the repository owner is asked to accept it.

**2. Research questions now closed** (decision-quality answer reached; reconciliation into durable documents still pending, see §F):
   - ADR-0013's core question: referent, equality, continuity, and fork rule for the durable operational identity.
   - All six agency hypotheses (H1–H6): no platform `Agent`; decision-source internals stay private; actor attribution survives controller replacement; replaying a decision differs from re-executing its source; capability extension needs no new shared concept; no agent-specific communication abstraction is needed.

**3. Research questions still blocking implementation:** none of the *research* is blocking — but nothing is implementation-admissible yet, because ADR-0013 remains textually unrevised and unaccepted, and the actor/trust/authority question remains `CANDIDATE` (not even `READY`).

**4. Questions safe to defer until implementation design:** divergence-point representation; persistence home for durable operational/attributed-request records; final type name/representation/UUID format; stateful decision-source identity; Challenge multiplayer actor adoption.
