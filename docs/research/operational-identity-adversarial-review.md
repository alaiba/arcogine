# Operational Identity — Independent Adversarial Review

> **Artifact type:** Independent adversarial research review (`docs/development/researching.md` §9)
> **Research status:** ACTIVE — this review is research evidence for a later, separate ADR-0013 reconciliation pass
> **Risk classification:** HIGH — identity, lineage, continuity, and persisted/public semantics
> **Authority:** Research evidence only. This document is not accepted architecture, product direction, or implementation commitment, and does not amend ADR-0013.
> **Review date:** 2026-09-09

## Baselines

| Baseline | Value |
|---|---|
| Live `main` at review time | `5d010e2aa79e36ca6a9793ebf305ba309e3da5a7` |
| Source report path | `docs/research/operational-execution-digital-twin-identity-report.md` |
| Source report branch | `docs/agency-and-operational-identity-research-reports` |
| Source report commit reviewed | `bf744af171d5a42a3c578d33836e8e45bd33b0fe` |
| Source report's own stated baseline | `2acee74723aad7a9ca384459f5d44ef35b85b6fd` |
| Companion source consulted | `docs/research/agency-and-operational-identity-synthesis.md` at the same commit |

The source report is branch material, not landed repository truth. It is reviewed here as research
evidence.

### Independence statement

```text
Independence: SATISFIED via the fresh-isolated-session condition
```

This review was performed in a fresh isolated session that did not author the source report and has
no responsibility for preserving its conclusion. The repository grounding, constraint
reconstruction, candidate generation, and adversarial-case derivation recorded in §2 were completed
**before** the source report was opened, which is the anchoring control
`docs/development/researching.md` §9 prescribes.

The stronger preference — a different researcher, person, or model family — cannot be verified from
here: the source report records no authoring model or person, so this review cannot claim to satisfy
preference 1. It satisfies preference 2, which `docs/development/researching.md` §9 states as the
acceptable minimum for a high-risk pass. A reconciliation author who considers same-family
authorship a material risk should treat that as the residual limitation of this pass, not as an
unstated assumption.

---

## 1. Bounded question

Two questions were under review, and they are deliberately distinct.

**The underlying research question** (ADR-0013's "Unresolved decision", Proposed):

> What exactly is the independently continuing operational history/partition that needs durable
> identity, and what makes two records belong to the same one versus different ones?

**The question this review answers:**

> Do the source report's proposed durable operational-identity referent, its continuity/equality
> rule, its divergence/fork rule, and its associated qualifications survive an independent attempt
> to falsify them, and is the result decision-quality evidence for an ADR-0013 reconciliation pass?

Out of scope by construction: editing or accepting ADR-0013; implementing any Operational type or
module; selecting representation, encoding, persistence, or coordination technology; admitting
Operational implementation planning; and settling actor/trust/authority semantics except where
needed to demonstrate that they are separable from identity continuity.

---

## 2. Independent pre-report reconstruction

Recorded before the source report's recommendation was read, and preserved here unaltered in
substance so it can be compared with the report rather than retrofitted to it.

### 2.1 Constraints that any answer must preserve — separated by actual authority

This separation turned out to matter more than expected, and is the single most important piece of
re-grounding this review contributes.

**Accepted ADRs — real authority:**

- **ADR-0004** — semantic content identity is not controlled-revision identity; lineage is not
  semantic equality; human labels are not identity.
- **ADR-0008** — historical *occurrence* identity is opaque and non-derived; equal content may
  legitimately recur (`F1 -> F2 -> F1`) without collapsing into one occurrence; rollback is an
  ordinary new occurrence, never a reuse or a rewind; `0..1` parent initially, structurally
  extensible; **divergence is representable without branch objects** (several children may share one
  parent); "authoritative historical identity begins at persistence acceptance"; repository-level
  uniqueness, parent existence, and cycle integrity belong to the authoritative persistence
  boundary, not to a value type; corrections must not silently rewrite an accepted record.
- **ADR-0011** — Arcogine is **not** event sourced; `RunId` is opaque per-runtime correlation
  identity that must not affect outcome; reset creates a new run identity and sequence epoch; a
  bounded journal must expose gap detection because "silent truncation is not acceptable recovery
  behavior"; `ControlledRevisionId` is never synthesized to fill a field; Operational is a sibling
  consumer, never an owner of runtime event semantics.
- **ADR-0015** — released semantics versions are immutable and never reused; **retirement removes
  executability, not provenance**; the durability guarantee is "attribution plus a verifiable
  definition, not permanent exact re-execution"; cross-version comparison must be explicit.
- **ADR-0012** — external representations are projections; identity is not defined by transport.
- **Current architecture** (`docs/architecture/governance-conformance.md` §9) — the
  `Evidence` / `EvidenceUse` split: a source-level fact carries independent provenance and is **not**
  bound to fingerprint/revision at source; binding happens at interpretation/use time; one
  `Evidence` may be referenced by many `EvidenceUse` records.

**Proposed only — must not be leaned on as established:**

- **ADR-0013** itself, including every one of its six "retained requirements" — among them the claim
  that a durable identity is still required at all, and the raw-observation rule.
- **ADR-0016** (evidence provenance classes).
- **`docs/architecture/operational-execution-digital-twin.md` in its entirety** — the document's own
  header reads `Status: Proposed architectural reference` / `Authority: Proposed architecture`.

**Planning:** `docs/planning/operational-execution-digital-twin-readiness.md` is
`NOT ADMITTED`. No Operational implementation slice is admitted, and no `:operational` module exists
in `product/` (verified by inspection).

**Landed implementation evidence bearing directly on "acceptance":**

- `ControlledRevisionAuthority.accept(candidate, artifact)` — "the candidate's `recordedAt` value is
  not authoritative input. The revision authority establishes the accepted record's `recordedAt` at
  its commit boundary."
- `FileControlledRevisionAuthority` — append-only, single filesystem authority, exclusive
  `FileLock`, explicit `DUPLICATE_REVISION_ID` / `MISSING_PARENT` / `STORAGE_INTEGRITY` failures,
  artifact re-verified on historical resolution, and missing or corrupt history fails explicitly
  rather than falling back to current state.
- `EventLog` — in-memory, bounded (1,000,000), with `isTruncated()` exposing loss explicitly.
- `FactoryRuntime.drainSupportedEvents()` — no retained cursor-replayable history exists at all.

Arcogine's only landed notion of an "authority" is therefore a **single-writer, locally exclusive,
append-only store**. Nothing in the repository selects or implies a quorum, lease, consensus, or
fencing mechanism.

### 2.2 Candidate referents generated independently

`N0` defer any durable identity until a concrete consumer exists; `C1` accountable operational
continuation; `C2` twin/correspondence-scoped identity; `C3` durable store/partition (journal)
identity; `C4` two-level stable account plus incarnation/branch identity; `C5` revision-like
occurrence identity plus explicit lineage, with **no** long-lived history identity; `C6`
deployment/binding-scoped identity; `C7` physical installation identity.

`C5` was generated specifically because ADR-0008 already proves Arcogine can represent divergent
history through occurrence identity plus parent links, **without** a branch object or a durable
history identity. It was the strongest independent challenger to any new identity.

### 2.3 Adversarial cases derived before reading

Compaction/archival/retention deletion under a no-loss rule; representation and storage migration
where every byte changes but meaning is preserved; split brain where both sides honestly recorded
one identity; late discovery of that split after externally consequential effects; stale checkpoint
restore; deliberate fork versus accidental stale restore; reconvergence to identical content;
retirement and attempted recreation under the same name; an observation arriving after a fork but
describing pre-fork time; two independent twins of one installation; and an external acknowledgement
followed by no physical transition.

### 2.4 Independent prior on the ADR boundary

Before reading: settle referent, equality, the continuity discriminator, divergence-requires-new-
identity, mandatory lineage, non-reuse, and the existing non-derivation constraints. Defer name,
representation, encoding, canonicalization, persistence mechanics, accepted-record storage model,
divergence-point representation, module ownership, registration authority, and coordination
mechanism. Genuinely uncertain in advance: issuance semantics, and closure/retirement — where the
prior expectation was that **no consumer exists**, so the ADR should leave it explicitly open rather
than foundationally forbid it.

### 2.5 Where the reconstruction and the report converged and diverged

**Converged.** The candidate shortlist, the winner, and the treatment of `C5` (the report's
candidate F) all matched. The report gave `C5` a serious hearing rather than omitting it, which
removes the most likely omitted-candidate finding. Reconvergence, immutability, and the
raw-observation boundary matched.

**Diverged.** The reconstruction expected closure/retirement to be left open; the report forecloses
it. The reconstruction expected the acceptance boundary to need a stated minimum semantics; the
report defers it entirely to a future consumer. The reconstruction's compaction/retention and
storage-migration cases have no counterpart in the report. And the reconstruction did not anticipate
that the report's detectability claim would rest on lineage comparison alone.

---

## 3. Report claims under review

The load-bearing propositions, as the report actually states them:

1. **Referent** — a durable Arcogine-owned identity is required, and it refers to one *accountable
   operational continuation*: a body of Arcogine's own conduct and conclusions maintained as one
   account. It is not the installation, model, runtime, deployment, twin, or a reality
   classification.
2. **Form** — the identity is not a new category but a reference to the record that established the
   account (its "genesis" or "fork" act).
3. **Continuity** — identity is preserved by any continuation that **(i)** loses no record the
   account has already accepted and **(ii)** is alone in extending it.
4. **Loss as discriminator** — record loss, not log prefix, is the continuity test, deliberately so
   that the rule does not presuppose a total order.
5. **Exclusive extension** — a continuation may not fork into independently extending successors
   while both silently claim the same undiverged identity.
6. **Fork lineage** — divergence always establishes a new identity carrying mandatory, immutable
   lineage: parent identity, divergence point, and whether the parent continues.
7. **Convergence** — later convergence of state or content is never identity equality.
8. **Historical immutability** — divergence discovered late is recorded as a finding plus a new
   forward identity; already-written records keep the identity they were written under.
9. **Detectability** — exclusivity cannot be proven at write time, but divergence is "recognisable
   afterwards by comparing recorded ancestry."
10. **Raw observations** — the identity attaches to what Arcogine did or concluded, never to what
    Arcogine was told; association happens later through a relating record, on the
    `Evidence`/`EvidenceUse` pattern.
11. **Identity separation** — the identity stays independent of `RunId`, `ModelFingerprint`,
    `ControlledRevisionId`, `EngineSemanticsVersion`, actor, deployment, target, and external
    subject.
12. **Lifecycle** — the account "never terminates"; it may go dormant and later resume; the
    identifier is never reused.
13. **Entitlement** — entitlement to extend derives from holding the accepted record set, not from
    holding the identifier.
14. **Consumer timing** — resolve the semantics in the ADR now; implement the value type only with
    its first real durable-record consumer.

---

## 4. Repository re-grounding

### 4.1 What changed since the report's baseline

`main` moved from `2acee74` to `5d010e2` — four merged changes: #290 (separate research from
implementation planning), #291 (testing-guide reconciliation), #292 (factory configured-resource
semantics), and #293 (formalize research method and researcher role).

**Material consequence.** #293 landed `docs/development/researching.md`,
`.github/agents/researcher.agent.md`, and `docs/research/report-template.md` — none of which existed
when the report was written. Two effects follow:

- The report's self-administered adversarial pass is now explicitly non-compliant with
  `docs/development/researching.md` §9 for a high-risk question. The report itself says so in
  substance ("its findings should be treated as a first pass rather than as independent review"),
  and this review is the pass §9 requires.
- The report's structure predates `docs/research/report-template.md`. That is a persistence-format
  question for a later reconciliation or publication step, not a semantic defect.

**No in-flight competing work.** One PR is open (#294, continuous-improvement operating model). It
does not touch ADR-0013, operational identity, the research register's Operational entry, or the
research method's substance. Nothing in flight changes this question.

**No semantic drift.** None of the four merged changes altered ADR-0004, ADR-0008, ADR-0011,
ADR-0012, ADR-0013, ADR-0015, ADR-0016, `docs/architecture/operational-execution-digital-twin.md`,
`docs/architecture/governance-conformance.md`, or the Operational readiness plan. **The report's
semantic conclusions are not stale.**

### 4.2 Stale or incorrect report claims, and whether they matter

| Claim in the report | Status on live `main` | Materially affects the conclusion? |
|---|---|---|
| Baseline `2acee74` | Superseded by `5d010e2`; no relevant semantic surface changed | **No** |
| Cites eight `OPS`-track planning coordinates (an `OPS` variant of the reserved `PLAN-` namespace) as real slices — for commands, deployment, correspondence, reconciliation, checkpoint/recovery, actor/trust, drift | **These coordinates do not exist anywhere on live `main`.** The Operational readiness plan is `NOT ADMITTED` and states that the first Operational delivery coordinate "may be created only after" its promotion criteria are met — none has been created | **Partly.** Not semantically, but §14's "unblocked" framing invites a reconciliation author to treat an unadmitted plan as sequenced work. Every such reference must be read as *anticipated future capability*, never as an admitted slice |
| Title and body carry a reserved planning coordinate | `docs/research/README.md`: "Research documents never receive temporary delivery coordinates." `.github/scripts/check-delivery-labels.py` treats any reserved-namespace token outside `docs/planning/` as a durable-naming leak and fails | **No** semantic effect. It is a real persistence-hygiene defect that must be resolved before the report is landed on `main`, and is **not** corrected here — the source report must remain the artifact under review |
| Treats ADR-0013's retained requirements and the Operational architecture document as constraints to preserve | Correct as *Proposed* material, but the report does not consistently flag that the entire Operational architecture document is Proposed | **Yes, mildly.** A reconciliation author could mistake Proposed constraints for Accepted ones. §9 of this review restates which authority each surviving invariant actually rests on |
| Attributes the `Evidence`/`EvidenceUse` precedent to `governance-conformance.md` | Correct — that is current architecture prose, not merely Proposed ADR-0016 | **No** |

### 4.3 A repository statement the report under-weights

`docs/architecture/operational-execution-digital-twin.md` §10 states:

> "No higher-rank execution identity above `RunId` should be introduced until checkpoint/recovery or
> another concrete capability proves its lifecycle/equality semantics."

The report quotes this in its baseline section but does not test its recommendation against it. Read
strictly, it is an argument for deferral of the *decision*, not only of the *type*. This review
treats it as the strongest repository-internal support for candidate `N0` and evaluates it in §5 and
§7.12 rather than letting it pass unexamined.

---

## 5. Candidate models

Carried forward for evaluation. `A` is the report's model **as qualified by this review**, not as
originally written.

| Model | Shape | Equality/continuity |
|---|---|---|
| **A — Accountable operational continuation** | One account of Arcogine's own conduct and conclusions, established by an explicit act, extended over time | Same establishment act, extended without semantic loss of accepted accountable fact; divergence establishes a new identity with lineage |
| **F — No durable identity; succession relation only** | Records carry occurrence identity plus a declared `continues:` relation; "history" is the derived transitive closure | Transitive closure of the relation, not crossing a fork |
| **T — Two-level: stable account + incarnation** | A stable identifier surviving all recovery, plus an incarnation/branch identifier that changes on divergent continuation | Account equality by issuance; continuation equality by (account, incarnation) |
| **S — Subject/realisation-anchored** | Identity of the twin, the operational system instance, or the physical installation | Same subject or same realisation |
| **N0 — Defer the decision** | Not a referent. Leave ADR-0013's referent unresolved until a concrete durable operational record capability exists | n/a |

`C3` (store/partition identity) and `C6` (deployment-scoped identity) were dropped before the matrix:
both are falsified outright by ADR-0013's own retained requirement that the identity survive storage
migration and deployment change, and neither survives a single proving case the others do not.

**One model discovered only after reading the report** and not present in the reconstruction: the
report's candidate G, "the identity *is* a reference to the establishment record." This is not a
rival referent — it is a claim about the identity's *form*. It is a genuine contribution: it answers
`F`'s strongest objection (that naming an equivalence class invents a new category) by making the
identifier a reference to an ordinary durable record rather than a new species of identifier. It
survives this review and is folded into `A`.

---

## 6. Adversarial and proving-case matrix

Twenty-two cases. `PRESERVED` = same durable identity; `NEW` = a new identity is required; `NONE` =
no operational identity is created or changed; `FAILS` = the model gives no determinate or no
truthful answer.

| # | Case | A (as qualified) | F | T | S |
|---|---|---|---|---|---|
| 1 | Process restart | PRESERVED — nothing accepted lost | PRESERVED | PRESERVED | PRESERVED |
| 2 | Runtime replacement (new `RunId`, new build) | PRESERVED — run identity is correlation only (ADR-0011 §4) | PRESERVED | PRESERVED | PRESERVED |
| 3 | Active/passive failover, standby fully caught up | PRESERVED | PRESERVED | PRESERVED — incarnation may or may not advance | FAILS — no answer about who is "the" instance |
| 4 | Disaster-recovery standby, full accepted set | PRESERVED | PRESERVED | PRESERVED | FAILS |
| 5 | Failover to a lagging standby | NEW + lineage; abandoned tail retained | NEW only if declared honestly | NEW incarnation, account preserved | FAILS |
| 6 | Deliberate branch/fork from a selected point | NEW + lineage + parent-continues annotation | NEW | NEW incarnation | FAILS — a fork is not a second installation |
| 7 | Split brain, both sides honestly extending | Undeclared fork; **detectable only if per-record carried-forward evidence exists** (§7.4) | Same, and harder to detect without a label | Detectable if incarnations were rotated independently | FAILS |
| 8 | Split brain discovered late, after external consequence | Divergence finding + forward-only new identity; nothing relabelled, nothing deleted | Same | Same | FAILS |
| 9 | Commissioning to production lifecycle change | PRESERVED — the rule never mentions lifecycle stage | PRESERVED | PRESERVED | FAILS — invites a staging/production re-derivation |
| 10 | Gain or loss of telemetry | PRESERVED — a relationship change recorded within the account | PRESERVED | PRESERVED | FAILS — is a twin without telemetry still a twin? |
| 11 | Gain or loss of control capability | PRESERVED | PRESERVED | PRESERVED | FAILS |
| 12 | Gain or loss of trust/authority; organisational transfer | PRESERVED — authority is separable (§7.11) | PRESERVED | PRESERVED | FAILS |
| 13 | Hybrid physical/synthetic composition | NONE — the rule never inspects what a subject is | NONE | NONE | FAILS — forces a whole-environment judgement |
| 14 | Several independent twins of one installation | DISTINCT identities sharing an external subject | DISTINCT | DISTINCT | FAILS — one installation, how many identities? |
| 15 | Historical inspection only | NONE — reading is not extending | NONE | NONE | AMBIGUOUS |
| 16 | Checkpoint restore, current | PRESERVED | PRESERVED | PRESERVED | AMBIGUOUS |
| 17 | Storage migration, every byte rewritten | PRESERVED — **only under the narrowed loss rule** (§7.1); the report's literal wording is at risk here | PRESERVED | PRESERVED | PRESERVED |
| 18 | Retention policy, compaction, archival, summarisation | PRESERVED **only under the narrowed rule**; the report's literal "nothing forgotten" wording would fork | PRESERVED — relation survives compaction | PRESERVED | PRESERVED |
| 19 | Late or out-of-order external observation, describing pre-fork time | NONE — the observation belongs to no account; parent and child may each interpret it | NONE | NONE | FAILS — must choose one twin |
| 20 | External acknowledgement, then no physical transition | Arcogine's request/submission and its later interpretation carry the identity; the target's acknowledgement does not (§7.7) | Same | Same | FAILS — collapses request and reality |
| 21 | Closure/retirement, then attempted recreation under the same name | Non-reuse holds. **Whether closure exists is unresolved** (§7.6) — the report's "never terminates" is not established | Same | Same, and `T` makes closure easier to express | FAILS |
| 22 | Two divergent branches later reaching identical state | Never identity equality (ADR-0008 `F1 -> F2 -> F1` precedent) | Never equality | Never equality | FAILS — same state, same subject, one identity? |

**Reading of the matrix.** `S` is falsified decisively and repeatedly, and is not revivable. `F` is
**not** falsified by any proving case — it is semantically equivalent to `A` and survives the matrix.
It loses on the three practical grounds the report gives (write-time entitlement, concurrent
heterogeneous records with no single chain, and self-describing exports under ADR-0012), which this
review independently accepts as sound; the second is the strongest and rests on repository fact
rather than preference. `T` also survives every case and is *never* falsified — it is rejected on
Arcogine-internal consistency grounds (ADR-0008 already chose transitive lineage over a second
super-identity), not by evidence. §7.12 and §8 treat that distinction carefully, because the report
presents it as an evidence result.

---

## 7. Mandatory challenge results

### 7.1 Does "no accepted record lost" imply permanent physical retention?

**Challenge.** The report's condition (i) reads: "Every record the account has already accepted
remains part of the continuation's prior state. Nothing accepted is dropped, disowned or forgotten."
Applied literally, legitimate compaction, archival, retention-policy deletion, representation
migration, or replacement of a record by an equivalent summarised form all "drop" or "forget" an
accepted record and would therefore force an identity fork.

**Evidence.** The report never addresses compaction, retention, archival, summarisation, or
representation migration — the words do not appear. Yet ADR-0013's own retained requirement 1
demands the identity survive **storage migration**, and the report's proving-case table asserts
storage-independence without testing it against a rule that talks about records "remaining."
Arcogine already answers this question elsewhere, twice: ADR-0015 fixes the durability guarantee as
"attribution plus a verifiable definition, **not** permanent exact re-execution," and holds that
"retirement removes executability, not provenance"; ADR-0011 §8 and `EventLog.isTruncated()`
establish that bounded retention is acceptable **provided loss is explicitly detectable** rather than
silent. External evidence points the same way: PostgreSQL retains abandoned timeline branches but
"you cannot recover into timelines that branched off earlier than the base backup" — even the
report's flagship analogue bounds addressability by retained material; and Temporal's strictest
non-reuse policy is scoped "within the retention period."

**Result.** **Defect confirmed.** Continuity must not depend on retaining every original physical
record forever.

**Effect on report.** Condition (i) is over-broad and representation-dependent as written. The
underlying discriminator is sound; the wording is not.

**Qualification required.** State the rule representation-independently, as: *continuity is broken
when the account can no longer answer for an accountable fact it already accepted — because that
fact was semantically disowned, or because it was lost in a way the account cannot declare and
bound. It is not broken by a change of representation, storage, encoding, or physical location that
preserves attributability, nor by a declared, recorded, bounded retention or compaction under which
what is no longer retrievable remains explicitly known to have existed.* Three consequences follow
and must be stated: silent loss is a fork; **declared** loss is a fork *with a recorded gap*, per
ADR-0011 §8's rule that recovery must detect the gap rather than pretend completeness; and a lossy
summarisation is not a fork only when its lossiness is itself an accepted record. No persistence
technology is selected by any of this.

---

### 7.2 Is "accepted record" sufficiently defined?

**Challenge.** The entire continuity rule turns on the word *accepted*, and the report's §15 lists
"What exactly constitutes an 'accepted' operational record?" as an unresolved unknown deferred to a
future capability. If the term is undefined, condition (i) is unevaluable and the rule is not
decision-complete.

**Evidence.** The report distinguishes none of: received-but-not-accepted, provisional, rejected,
superseded interpretation, correction, retraction, derived projection, archived representation,
late-arriving fact, or concurrent writers. It asserts only that acceptance must be "monotonic."
Against that, Arcogine already has a landed, executable acceptance boundary: ADR-0008's "authoritative
historical identity begins at persistence acceptance", implemented as
`ControlledRevisionAuthority.accept(...)`, where the **authority** — not the candidate — establishes
the accepted record at its commit boundary, and where corrections "must not silently rewrite an
accepted revision."

**Result.** **Partially confirmed — and repairable now, not deferrable in full.** The report is right
that the *concrete* acceptance boundary (storage model, transaction semantics, arrival ordering)
belongs to the first durable operational record capability. It is wrong to leave *acceptance* wholly
undefined, because the continuity rule cannot be evaluated without a minimum semantics.

**Effect on report.** Claim 3 is not decision-complete as written. It becomes decision-complete when
the minimum below is added, all of which is already available from Accepted ADRs.

**Qualification required.** ADR-0013 must state the smallest semantic meaning of acceptance:

1. Acceptance is an act of an **authority for the account**, at that authority's commit boundary; a
   record-shaped value that has not been accepted is not part of the accepted set (ADR-0008).
2. Acceptance is **monotonic**: an accepted record is never un-accepted. Correction, retraction,
   supersession, and reinterpretation are **new accepted records about** an earlier one, never a
   removal of it (ADR-0008's corrections rule; ADR-0011's rollback-as-new-record discipline).
3. Received-but-not-accepted, provisional, and rejected material is **not** in the accepted set, so
   losing it never breaks continuity.
4. A derived projection is not independently accepted material; it inherits the status of what it
   derives from.
5. The accepted set must be **determinable by the account** — otherwise condition (i) is
   unevaluable, and the report's own "undetermined" rule (unproven continuity fails safe to fork)
   correctly applies.

This is a semantic contract on any future record capability. It selects no storage model.

---

### 7.3 Does exclusivity require an unstated distributed-systems mechanism?

**Challenge.** Condition (ii) — "no other continuation is concurrently extending the same account" —
is not locally checkable. Does stating it smuggle in leases, quorum, fencing, consensus, or
leader election?

**Evidence.** The report anticipates this and concedes it explicitly (its amendment 2): exclusivity
is "claimed and detected, not proven," and enforcement mechanisms are infrastructure that ADR-0013 §3
places outside the semantics. The three-way separation the challenge demands is genuinely maintained:
the semantic requirement, the ability to prove it synchronously, and the mechanism enforcing it are
kept apart. Independently: Arcogine's only landed authority is a single-writer exclusive-lock local
store, and nothing in the repository implies a distributed mechanism. The consensus/epoch literature
the report cites as background is used correctly and defensively — as a warning that an epoch is
*ordering and authority*, never identity.

**Result.** **Challenge does not succeed against the report's stated position** — but it exposes a
different, real defect, below.

**Effect on report.** No mechanism is required to state the invariant. However, condition (ii) is
mis-typed: the report states it as a **condition on identity preservation**, then states in §10 that
during a split brain "already-written records keep the identity they were written under." Both cannot
hold. If (ii) were a truth-condition for identity continuity, the records written on the losing
branch were never under a preserved identity — yet the report (rightly) says they keep it.

**Qualification required.** Restate the two conditions as different *kinds* of rule. Condition (i) is
a **factual continuity condition**: it determines whether identity is in fact preserved. Condition
(ii) is a **normative exclusivity obligation** on legitimate extension: its violation is a
well-formedness defect of the *claim*, producing a recorded divergence finding and a forward-only
remediation, never a retroactive change to identity already written. Only this typing is consistent
with claim 8, with ADR-0008's immutability, and with ADR-0012's projections already leaving Arcogine's
control.

---

### 7.4 Is retrospective divergence detection actually possible under the proposed model?

**Challenge.** "Detectable later" is empty unless the model says detectable *by what surviving
evidence*. The report answers: "because every continuation records its predecessor and what it
carried forward, divergence is recognisable afterwards by comparing recorded ancestry."

**Evidence.** This answer does not work for the case it is offered for. In a genuine split brain,
**neither side declares a fork** — that is what makes it a split brain. Both extend the same
identity, and neither writes a new lineage entry. Their recorded ancestries are therefore
*identical*, and ancestry comparison cannot distinguish them. The problem is sharpened by two of the
report's own commitments: it permits legitimate concurrent multi-writer extension ("Federated live
operation... Permitted: many deployments, one history"), and it insists the accepted set is
partially ordered with no total order. A model that permits concurrent legitimate extension and
forbids concurrent illegitimate extension **must** supply a discriminator between them. The report
supplies none.

Re-verification of the report's own analogues shows precisely what it dropped:

- **DRBD** detects split brain because each node **rotates its own current UUID at the moment it
  begins modifying data independently** (notably on being promoted to Primary while disconnected)
  and retains history UUIDs; detection is ancestry comparison *plus* that rotation precondition. The
  LINBIT 9.0 guide's accessible text states that detection "depends on whether each node
  independently rotated its current UUID when beginning modifications — the system cannot reliably
  distinguish concurrent changes if UUID rotation didn't occur."
- **SQL Server** stores `first_recovery_fork_guid` and `last_recovery_fork_guid` on **every**
  `backupset` row, not only at a fork, with `fork_point_lsn` non-NULL exactly when they differ. The
  continuity check is possible because *each accepted unit carries its own fork lineage*.

So both cited systems make divergence detectable by putting carried-forward evidence **on every
record**, which is a semantic obligation, not a coordination mechanism.

**Result.** **Defect confirmed, and it is the most load-bearing finding of this review.** Claim 9 is
unsupported as stated, and the DRBD analogy is used to support a conclusion the analogy does not
support once its missing precondition is restored.

**Effect on report.** The exclusivity and divergence claims stand only if the model adds an evidence
obligation. Without it, the reassurance that "divergence is guaranteed representable and detectable"
is false, and the honest weaker statement would be that divergence is detectable *only where a record
capability happens to have retained enough evidence*.

**Qualification required.** ADR-0013 must state the minimum divergence-evidence obligation in
semantic terms: *every accepted record must carry evidence sufficient to place it relative to the
accepted material its writer extended* — enough that two continuations can later establish common
ancestry, that they diverged, an identifiable divergence boundary, and which accepted material each
carried forward. Whether that evidence is a reference to prior accepted material, a per-account
monotonic acceptance marker, a digest of carried-forward state, or a per-writer continuation marker
is a **representation** question that may defer. What may **not** defer is the obligation itself,
because a record capability designed without it makes divergence permanently undetectable, and that
is not retrofittable onto records already written. This obligation names no mechanism: it constrains
what evidence must survive, not who coordinates.

---

### 7.5 Does late split-brain discovery require rewriting history?

**Challenge.** Test the case where both branches honestly recorded the same identity before anyone
knew. Is "already-written identity bindings never change" actually justified, or imported from
neighbouring identity designs?

**Evidence.** Four resolutions were considered: retroactively relabel one branch; delete a branch;
treat old records as malformed; or record a divergence finding and establish a distinct continuation
going forward. Relabelling and deletion both falsify what the system actually did, which is
disqualifying for accountability records; treating honest records as malformed is untrue on its
face. The fourth is independently supported: ADR-0008 makes the record-to-identity binding immutable
and requires corrections to be "explicit and separately attributable"; ADR-0011 forbids pretending a
gap did not occur; ADR-0012 means representations may already have been exported beyond Arcogine's
reach, so a retroactive relabel cannot even be made globally true; and the Charter's causality and
provenance principle requires that what happened remain recoverable. The external traditions split
exactly here — recovery systems (PostgreSQL, Oracle, SQL Server) retain the abandoned branch, while
replication systems (DRBD) discard a victim — and the report's reading that Arcogine belongs with
the recovery systems because its records are *history* rather than *state* is sound.

**Result.** **Challenge does not succeed. Claim 8 survives, and is justified rather than imported.**

**Effect on report.** None adverse. The justification is currently implicit and should be made
explicit in the ADR, since "identity bindings are immutable" is exactly the kind of inherited rule a
future reader would otherwise be entitled to question.

**Qualification required.** None beyond stating the justification.

---

### 7.6 Has the report overcommitted to an endless history lifecycle?

**Challenge.** Separate "an identity is never reused" from "the continuation can never close,
retire, terminate, or become non-extendable." The report asserts the account "**Never terminates**."

**Evidence.** The non-reuse half is well supported: Kafka KIP-516 (re-verified) shows that
name-based identity reuse after deletion produces stale replicas indistinguishable from current
ones, and topic IDs give "true uniqueness across the topic's lifecycle, not merely across concurrent
topics"; ADR-0008 and ADR-0015 both forbid identifier reuse. The termination half is not supported by
anything. The report derives it from the Temporal analogy *breaking* on reuse — but re-verification
confirms Temporal workflow executions do close, and Workflow Ids are reusable under an explicit
policy whose strictest setting is bounded "within the retention period." "Do not reuse the
identifier" and "the thing never ends" are different propositions, and the second does not follow
from the first. Kafka's own lesson is likewise "do not reuse the name," not "never delete the
topic." The report is also internally inconsistent: §4 says the account never terminates, while §6
and §11 speak of "non-reuse of a **retired** identity" — which presupposes retirement exists. And no
consumer for closure exists: Operational implementation is `NOT ADMITTED`.

**Result.** **Defect confirmed.** This is evidence of possibility treated as necessity, plus an
internal contradiction.

**Effect on report.** Claim 12 splits. Non-reuse survives intact. "Never terminates" does not.

**Qualification required.** ADR-0013 must assert non-reuse and immutability of the record-to-identity
binding, and must **not** assert that a continuation can never be closed, retired, or made
non-extendable. Leave closure/retirement explicitly open, recorded as awaiting a concrete consumer.
Foreclosing it now would be an unevidenced, hard-to-reverse restriction on a durable contract — and
ADR-0015 already supplies the shape a future answer would likely take: retirement removing
extendability while preserving provenance and attributability.

---

### 7.7 Does "Arcogine conduct carries the identity" over-own external facts?

**Challenge.** The report places "commands **and their lifecycle**" on the Arcogine-owned,
identity-bearing side. The command lifecycle includes target acknowledgement, external
acceptance/rejection, and external result telemetry — which are not Arcogine's conduct.

**Evidence.** Walking the sequence — Arcogine decides and requests; Arcogine submits; the external
target acknowledges receipt; the external controller accepts or rejects; a physical transition may or
may not occur; telemetry reports something; Arcogine interprets and reconciles — the boundary falls
cleanly:

| Fact | Owner | Carries the identity? |
|---|---|---|
| Arcogine's decision/request to command | Arcogine conduct | **Yes** |
| Arcogine's submission act (what it sent, when, under what authority) | Arcogine conduct | **Yes** |
| Target-produced acknowledgement of receipt | Externally sourced fact | **No** — linked to the identity-bearing submission record, independently provenanced |
| External controller's acceptance/rejection | Externally sourced fact | **No** — same treatment |
| Actual physical transition | Reality; not an Arcogine record at all | **No** |
| Telemetry reporting the outcome | Raw observation | **No** — independently provenanced |
| Arcogine's interpretation/reconciliation of all of the above | Arcogine conclusion | **Yes** |

This is the boundary the architecture already requires: `requested semantic operation != external
command/result != actual transition != observation != reconciled interpretation`. Treating a
target's acknowledgement as Arcogine conduct because it arrived in a command context would smuggle
externally sourced facts onto the identity-bearing side through a side door — defeating the very
raw-observation rule the report defends in its §9. The architecture is explicit that "an accepted
command is not proof that reality changed."

**Result.** **Defect confirmed** — an ownership inversion, narrow but real.

**Effect on report.** Claim 10's partition is right in principle and over-drawn in wording. "Commands
and their lifecycle" must not be a single identity-bearing unit.

**Qualification required.** State the boundary at the granularity above: **Arcogine's request,
submission, and interpretation carry the identity; the target's acknowledgement, the external
accept/reject, the physical transition, and the resulting telemetry do not** — they are
independently provenanced facts *linked to* the identity-bearing records. Do not collapse the five
distinct facts merely to make attribution convenient.

---

### 7.8 Does the raw-observation rule survive difficult temporal cases?

**Challenge.** Test observations arriving after a fork but describing pre-fork time; two twins
interpreting one observation differently; later correction; a source later found untrustworthy;
correspondence changing after ingestion; and no correspondence at ingestion. Then check whether the
Governance evidence analogy actually transfers.

**Evidence.** The rule handles all six, and handles them by *not* needing a special case. An
observation carries no account identity, so a post-fork arrival describing pre-fork time belongs to
no account and both parent and child may legitimately interpret it, each recording its own
interpretation in its own account — the case that would have forced a wrong choice under any model
binding identity at ingestion. Two twins disagreeing is a comparison of two accounts' conclusions,
not a conflict. A correction is a new independently provenanced source fact; interpretations that
used the earlier one remain historically truthful. A source later judged untrustworthy is a trust
re-assessment recorded within the interpreting account. A correspondence change is a new
identity-bearing assertion within the account. No correspondence at ingestion is the normal case the
architecture already anticipates.

On the analogy: `governance-conformance.md` §9 is current architecture, correctly cited, and it
transfers well — including a useful collapse case the report does not mention, where structural
evidence drawn from Arcogine's own authoritative state legitimately binds at source, whose Operational
analogue is exactly Arcogine's own conduct records.

**Where the analogy breaks — and the report does not say.** `EvidenceUse` binds evidence to a
**point** identity (a fingerprint or a revision). An operational account is an **accumulating**
identity, so an interpretation record binds a source fact to something that keeps changing after the
binding is made. Nothing in the Governance pattern tells Arcogine what that means for later
re-interpretation, supersession of an interpretation, or an interpretation made under an account
state later found divergent. The pattern transfers for *where the binding lives*; it does not
transfer for *what a binding to an accumulating identity means over time*. Additionally, ADR-0016 —
which the report lists among its consulted sources — is **Proposed**, and must not be cited as
settled.

**Result.** **Challenge does not succeed. Claim 10's observation half survives**, strengthened.

**Effect on report.** Sound, with one unstated analogy limit.

**Qualification required.** Record the point-versus-accumulating asymmetry as a known limit of the
Governance precedent, and flag ADR-0016's Proposed status wherever it is relied on.

---

### 7.9 Does deliberate fork really share one rule with stale restore?

**Challenge.** These differ in authorization, reason, severity, safety response, parent status, and
recovery behaviour. Do they nevertheless share one *identity* rule?

**Evidence.** Both begin a continuation from something other than the parent's full accepted set, so
both trigger the same condition and both require a new identity plus lineage. Everything that
differs — whether the act was authorized, why, how severe, whether the parent continues, what safety
response follows — is annotation on the fork record, not a different identity semantics. Keeping one
rule is actively protective: it removes any incentive to reclassify an inconvenient stale restore as
"really a continuation." The one substantive difference worth recording is whether the parent
continues, which the report already requires in lineage.

**Result.** **Challenge does not succeed. Claim 6 survives.**

**Effect on report.** None adverse.

**Qualification required.** One precision defect, discovered while testing this case. The report says
a new identity must be established "**exactly when**" (i) or (ii) fails. That forbids a *voluntary*
declared fork taken from the parent's complete current accepted set — branching at the head with
intent to diverge — which is a legitimate and expected operation (the Charter's "hypothetical
branches"). Restate as: a new identity is **required** at least whenever the continuity condition
fails, and is **permitted at any time** by an explicit declared fork. Otherwise a deliberate branch
at head is indistinguishable from an undeclared split brain.

---

### 7.10 What happens when branches later reconverge?

**Challenge.** Do independently identical state, a common deployment, content-equivalent records, a
selection of one branch as authoritative, or a reconciliation concluding both describe one external
subject justify identity equality or merge?

**Evidence.** None of them do, and Arcogine has already decided this in the closest available
precedent: ADR-0008's `F1 -> F2 -> F1` case, where equal semantic content at two points in history
must not collapse into one occurrence, because collapsing "would erase the fact that B existed
between them." The operational case is stronger, not weaker: two accounts that separately recorded
conduct cannot become one without falsifying which of them did what. Selecting one branch as
authoritative is a decision recorded *about* both, not a merge. Sharing an external subject is
precisely the many-twins case (case 14) where distinct identities sharing a relatum is the correct
answer, corroborated by the AAS metamodel's normative many-shells-per-asset position, re-verified in
this session.

On multi-parent lineage: the report defers it on ADR-0008's precedent, correctly. ADR-0008 keeps
`parentRevisionIds` structurally extensible while enforcing `0..1`, and explicitly declines to
import merge semantics before a merge workflow exists. The same reasoning applies here, and the
initial ADR is not damaged by the deferral because the structure remains extensible.

**Result.** **Challenge does not succeed. Claims 7 and the `0..1` deferral survive.**

**Effect on report.** None adverse; this is the best-supported part of the report.

**Qualification required.** None.

---

### 7.11 Does "holding the accepted record set" define entitlement, identity, or both?

**Challenge.** The report states that "entitlement derives from *holding the accepted record set* and
not from *holding the identifier*," and that "possession of the identifier confers nothing." Is that
a continuity condition, an authorization rule, an operational-authority rule, or merely evidence?

**Evidence.** As an authorization rule it is unsound: an unauthorized party in possession of a
complete copy of the accepted record set would, under that rule, be *entitled* to extend the account.
Holding the accepted set is **necessary** for continuity and nowhere near **sufficient** for
authority. The two are separable, and the repository requires them to be kept separate: actor,
trust, and authority is a distinct `CANDIDATE` research question; the Operational architecture states
that "the exact module ownership of reusable actor/capability semantics is still open and must not be
forced into Operational"; and its safety principles already place "absence of authority is denial"
on the authority side, not the identity side. The report's own §12 warns that this ADR "must not be
used to place actor semantics in Operational" — and then its §13 recommendation 12 does exactly that
by making entitlement an ADR-0013 output.

**Result.** **Defect confirmed** — an unresolved question silently settled, and outside this
question's scope.

**Effect on report.** Claim 13 is two claims. The negative half ("possession of the identifier
confers nothing") is sound and worth keeping: it prevents the identifier from becoming a bearer
token, and it is what makes the identity semantic rather than capability-like. The positive half
("entitlement derives from holding the record set") is an authority claim this research did not
establish and should not make.

**Qualification required.** Split them. ADR-0013 should state that **holding the complete accepted
record set is a necessary condition for continuity, and is evidence relevant to a later entitlement
decision**, while **entitlement and authority to extend an account remain a separate, unresolved
question** owned by the actor/trust/authority research. Keep the negative claim; drop the positive
one.

---

### 7.12 Consumer timing, and the deferral candidate `N0`

Not a numbered mandatory challenge, but required by §11's instruction not to omit a viable model.

**Challenge.** `docs/architecture/operational-execution-digital-twin.md` §10 says no higher-rank
identity above `RunId` "should be introduced until checkpoint/recovery or another concrete capability
proves its lifecycle/equality semantics." Read strictly, that is an argument for deferring the
*decision*, not just the type — candidate `N0`.

**Evidence.** The sentence forbids **introducing** an identity, which is a statement about the type
and its use, not about resolving semantics on paper. ADR-0013's own hold says work "may continue on
architecture analysis and requirements," and its consequences forbid materialising an `:operational`
module for identity types — again a statement about introduction. Deferring the *decision* also has a
real cost that deferring the *type* does not: the divergence-evidence obligation established in §7.4
must be known **before** the first durable operational record capability is designed, because records
written without carried-forward evidence make divergence permanently undetectable. That is precisely
the hard-to-reverse consequence an ADR exists to prevent.

**Result.** `N0` is **rejected for the semantic core and accepted for the type.** The report's claim
14 survives, and this review strengthens its justification: the semantics must be settled now
*because* a future record capability needs the acceptance and evidence obligations as design inputs,
not merely because the referent is now known.

**Qualification required.** None, beyond stating the justification so that "resolve now, implement
later" does not read as an arbitrary split.

---

## 8. External-evidence verification

The source report's "Verified in session" labels refer to its authoring run and are **not** inherited
here. The following were independently fetched and checked during **this** review.

| Source | Re-verified | What transfers | Where it breaks | What it actually supports |
|---|---|---|---|---|
| PostgreSQL, *Continuous Archiving and PITR* — "Timelines" | **Yes** | New timeline on completed archive recovery; mandatory timeline-history file recording which timeline it branched from and when; abandoned branches remain recoverable; **a plain crash restart creates no new timeline** | Defined over a strictly ordered WAL; Arcogine has no total order and is not event sourced | The fork rule and the refusal to destroy an abandoned branch. **Contrary detail the report omits:** "You cannot recover into timelines that branched off earlier than the base backup" — addressability is bounded by retained material, which supports §7.1 |
| SQL Server, `backupset` (T-SQL) | **Yes** | `first_recovery_fork_guid` / `last_recovery_fork_guid` are stored on **every** backup row; `fork_point_lsn` is non-NULL exactly when they differ | LSN ordering presumes a single log | **Strongly supports the §7.4 obligation**: continuity is checkable because every accepted unit carries its own fork lineage. **Contrary detail the report omits:** `family_guid` ("remains the same when the database is restored, even to a different name") plus `database_guid` ("a new value is assigned" on restore) is a **two-level** identity in the report's own cited source |
| DRBD 9.0 User Guide — generation identifiers, split brain | **Partially.** The accessible guide text and search-surfaced material confirm the mechanism; the internals section's exact wording could not be retrieved in this session | Divergence is detected retrospectively by comparing generation identifiers, without write-time consensus | **Detection requires each node to rotate its own current UUID when it begins modifying data independently** — a precondition the report drops; and DRBD then discards a victim, which Arcogine must not | Supports "detection without a coordination mechanism"; **does not** support "ancestry comparison alone suffices" (§7.4) |
| Temporal — Workflow Id and Run Id | **Yes** | Declared, business-meaningful identity separated from a system-generated incarnation; chain lineage via `first_execution_run_id` / `original_execution_run_id` | Workflow executions **close**, and Workflow Ids are reusable under an explicit policy whose strictest form is bounded "within the retention period" | Supports the declared-identity/generated-incarnation split. **Contrary to the report's "never terminates"** (§7.6) |
| Apache Kafka KIP-516 — Topic Identifiers | **Yes** | Names are not identity; reuse of a name after deletion leaves stale replicas indistinguishable from current ones; IDs give "true uniqueness across the topic's lifecycle" | No fork semantics whatsoever | Opacity and non-reuse. Its lesson is "do not reuse the name," **not** "never delete the topic" (§7.6) |
| Asset Administration Shell — IDTA metamodel and discovery API | **Yes** | Multiple AAS per asset is normal; discovery resolves one asset ID to **multiple** shell IDs | An AAS describes an asset; it is not a record of conduct, and defines no continuity or fork semantics | The decisive many-twins case (matrix case 14): the referent cannot be the physical installation |
| Oracle incarnations / DBID; IEC 81346 aspects | **No — not re-verified in this session** | — | — | Not relied on by any conclusion in this review. The report's own labels for them are not inherited |
| Consensus/epoch literature; event-sourcing/stream identity | **No** — the report itself marked these "background, verify before citing" | — | — | Not relied on here. They remain unverified background and must be verified before any ADR cites them |

**Contrary evidence actively sought and found.** Three items above cut against the report: PostgreSQL's
retention-bounded branch addressability, Temporal's closing executions and retention-bounded reuse
policy, and SQL Server's `family_guid`/`database_guid` two-level identity. The first two are folded
into §7.1 and §7.6 as qualifications. The third is treated in §7.13 below.

### 7.13 The two-level model was rejected on the wrong grounds

The report rejects a stable-account-plus-incarnation model (its candidate `T`, reached via Oracle
DBID) with: "One identifier plus mandatory lineage is sufficient; resist a second identifier unless a
concrete Arcogine capability demands one." Re-verification shows that **three** of the report's own
database analogues use two-level identity — Oracle (DBID plus incarnation), SQL Server
(`family_guid` plus `database_guid`), and DRBD (current UUID plus history UUIDs). The external
evidence does not support single-identifier-plus-lineage; if anything it leans the other way.

**Result.** The **conclusion** still stands, but its **justification** does not. Single identifier
plus transitive lineage is defensible on Arcogine-internal grounds — ADR-0008 already made exactly
this choice for controlled revisions, deliberately preferring transitive lineage over branch objects,
and consistency with a landed Accepted decision is a legitimate and strong reason. It is not an
external-evidence result.

**Qualification required.** ADR-0013 must present the single-identifier choice as an **Arcogine
consistency decision grounded in ADR-0008**, and must record that mature external systems commonly
use two-level identity — so that a future reader who encounters a case favouring two levels finds the
tradeoff recorded rather than believing it was foreclosed by evidence. `T` was never falsified by any
proving case in §6, and the ADR should not imply that it was.

---

## 9. Surviving invariants

Only these survived independent falsification. Each names the authority it actually rests on.

1. **Referent.** The durable identity refers to one **accountable operational continuation** — a body
   of Arcogine's own conduct and conclusions maintained as one account. It is not the physical
   installation, the external subject, a deployment, an Engine run, a model fingerprint, a governed
   revision, a twin-as-object, or a global simulation/staging/production classification.
   *(Survives every proving case; the only rival never falsified is `F`, which is semantically
   equivalent and loses on practical grounds, and `T`, which differs only in shape.)*
2. **Form.** The identity is a reference to the durable record that established the account, not a new
   category of identifier. It becomes authoritative when that record is accepted.
   *(ADR-0008's persistence-acceptance precedent.)*
3. **Continuity.** Identity is preserved across any continuation that does not lose the account's
   ability to answer for an accountable fact it already accepted — **stated
   representation-independently**, per §7.1. Storage, encoding, location, deployment, runtime, actor,
   target, subject, model, revision, engine semantics, telemetry, control capability, consequence,
   authority, organisation, and lifecycle stage may all change while identity holds.
4. **Minimum acceptance semantics.** Acceptance is an authority's act at its commit boundary;
   acceptance is monotonic; correction/retraction/supersession are new accepted records, never
   un-acceptance; non-accepted material is outside the set; the accepted set must be determinable by
   the account. *(ADR-0008 and its landed implementation.)*
5. **Divergence requires a new identity.** A continuation beginning from a selected earlier point, or
   from a set missing accepted material, establishes a new identity — one rule for deliberate fork and
   stale restore alike, with the difference carried by annotation. A declared fork is additionally
   permitted at any time.
6. **Lineage is mandatory and immutable.** Every fork records parent identity, a referenceable
   divergence point, and whether the parent continues. Ancestry is carried by these links alone;
   `0..1` parent initially, structurally extensible. *(ADR-0008 precedent.)*
7. **Divergence evidence.** Every accepted record must carry evidence sufficient to place it relative
   to the accepted material its writer extended, so that common ancestry, divergence, a divergence
   boundary, and what each side carried forward remain establishable later. *(Established by this
   review, §7.4.)*
8. **Exclusivity is an obligation, not a truth-condition.** Sole legitimate extension is required;
   its violation is a recorded divergence finding with forward-only remediation. It is claimable and
   detectable, never provable at write time, and requires no coordination mechanism to state.
9. **Convergence is never identity equality.** Independently arrived-at identical state, content
   equivalence, a shared deployment, a selection of one branch as authoritative, or a shared external
   subject none of them merge identities. *(ADR-0008's `F1 -> F2 -> F1` precedent.)*
10. **Historical immutability.** Already-written records keep the identity they were written under.
    Late-discovered divergence is represented by additional findings and lineage, never by rewriting
    recorded identity. *(ADR-0008 immutability; ADR-0011 gap honesty; ADR-0012 exported projections.)*
11. **Undetermined fails safe to fork.** Where continuity cannot be established, it is not proven, and
    a new identity with lineage and a recorded discontinuity is the safe resolution.
12. **Record-attachment boundary.** Arcogine's request, submission, interpretation, reconciliation,
    correspondence assertion, deployment record, and drift proposal carry the identity. A target
    acknowledgement, an external accept/reject, an actual transition, and raw telemetry do not — they
    are independently provenanced and linked. *(Narrowed from the report, §7.7.)*
13. **Raw observations stay outside.** Observations are never made to carry the identity at ingestion;
    association is a later, identity-bearing relating record on the `Evidence`/`EvidenceUse` pattern,
    with the point-versus-accumulating asymmetry recorded as a known limit.
14. **Identity separation.** Independent of `RunId`, `ModelFingerprint`, `ControlledRevisionId`,
    `EngineSemanticsVersion`, actor, deployment, target, and external-subject identity; never inferred
    from infrastructure. *(ADR-0013 retained requirements 2 and 3, unchanged.)*
15. **Non-reuse.** A retired or superseded identity is never reissued; the record-to-identity binding
    never changes. *(KIP-516; ADR-0008; ADR-0015.)*
16. **Timing.** The semantic core is settleable now and must be, because a future record capability
    needs the acceptance and divergence-evidence obligations as design inputs. The value type waits
    for its first real durable-record consumer.

---

## 10. Claims that do not survive as written

| # | Claim | Disposition | What must change |
|---|---|---|---|
| 1 | Continuity condition (i): "nothing accepted is dropped, disowned or forgotten" | **Narrow** | Restate representation-independently as loss of answerability, not loss of bytes. Declared, bounded, recorded retention/compaction/migration is not a fork; silent loss is; declared loss is a fork with a recorded gap (§7.1) |
| 2 | Continuity condition (ii) stated as a condition on identity preservation | **Rephrase** | Re-type as a normative exclusivity obligation whose violation is recorded, not as a truth-condition. Required for internal consistency with claim 8 (§7.3) |
| 3 | "Divergence is recognisable afterwards by comparing recorded ancestry" | **Narrow, and add the missing obligation** | Ancestry comparison alone cannot detect an undeclared fork. Add the per-record carried-forward evidence obligation, without which the detectability guarantee is false (§7.4) |
| 4 | "Accepted" left as an unresolved unknown | **Narrow the deferral** | The concrete boundary may defer; the minimum semantics (§7.2) may not, because the continuity rule is unevaluable without it |
| 5 | The account "never terminates" | **Remove and reopen** | Unsupported, internally inconsistent with the report's own "retired identity," and consumer-less. Keep non-reuse; leave closure/retirement explicitly open (§7.6) |
| 6 | "Commands **and their lifecycle**" carry the identity | **Narrow** | Split the lifecycle: request, submission, and interpretation carry it; acknowledgement, external accept/reject, transition, and telemetry do not (§7.7) |
| 7 | "Entitlement derives from holding the accepted record set" | **Narrow and defer** | Keep "possession of the identifier confers nothing." Recast record-set possession as a necessary continuity condition and as evidence; leave entitlement/authority to the separate actor/trust/authority question (§7.11) |
| 8 | "A new identity must be established **exactly when** (i) or (ii) fails" | **Rephrase** | "At least when… and permitted at any time by explicit declared fork," so a deliberate branch at head stays expressible (§7.9) |
| 9 | Two-level identity rejected as an evidence result | **Rephrase** | Present as an Arcogine consistency decision grounded in ADR-0008, and record that mature external systems commonly use two levels (§7.13) |
| 10 | Eight `OPS`-track planning coordinates cited as real slices; a reserved coordinate in the title | **Remove before persistence** | These do not exist; Operational implementation is `NOT ADMITTED`. Separate persistence/cleanup work on the source report — deliberately **not** performed by this review |

None of these falsifies the referent. Nine of the ten are repairable using authority the repository
already carries; the tenth is document hygiene.

---

## 11. Minimum ADR-0013 decision boundary

The criterion below is hard-to-reverse semantic consequence — specifically, whether getting the
answer wrong would invalidate durable records already written, or would merely cost a refactor.

| Question | Must ADR-0013 settle before acceptance? | May defer to first consumer? | Why |
|---|---|---|---|
| referent | **Yes** | No | It is the ADR's stated unresolved decision; every later record's meaning depends on it, and no record can be reinterpreted afterwards |
| equality | **Yes** | No | Equality *is* the durable contract; changing it later reclassifies records already written |
| continuity | **Yes** (as narrowed, §7.1) | No | Determines whether records written across a change are truthfully one account |
| divergence/fork | **Yes** | No | ADR-0013 already retains "must not silently share"; a wrong answer makes records affirmatively false |
| lineage | **Yes** — that it is mandatory and immutable | Representation only | A fork whose parent was never recorded is indistinguishable from a genesis, and unrecoverable later |
| non-reuse | **Yes** | No | Trivial to state now, catastrophic and unfixable to retrofit (KIP-516) |
| closure/retirement | **No — and must not be foreclosed** | Yes | No consumer exists; the report's "never terminates" is unevidenced. Record it as explicitly open |
| identity establishment/issuance semantics | **Split.** Settle: established by explicit act, never inferred; authoritative on acceptance of the establishing record | Defer: who issues, how, and how uniqueness is enforced | The first half is already ADR-0013 retained requirement 3 plus ADR-0008; the second is mechanism |
| final type name | No | **Yes** | Nothing is persisted; renaming costs a refactor. ADR-0013 already sequences the name after the rule |
| representation | No | **Yes** | ADR-0012 already governs; representations are projections |
| UUID/other encoding | No | **Yes** | Same. ADR-0008 shows an encoding can be chosen at implementation without disturbing semantics |
| parsing/canonicalization | No | **Yes** | Adapter-level concern, explicitly separated by ADR-0013 retained requirement 6 |
| persistence mechanics | No | **Yes** | ADR-0008 fixed identity semantics without selecting storage; the same split applies |
| exact accepted-record storage model | No — **but the minimum acceptance semantics must be settled** (§7.2) | Storage model only | The continuity rule is unevaluable without the semantics; the storage model is genuinely reversible |
| divergence-point representation | No — **but settle that it must be determinate and referenceable** | Representation only | "What the child adopted" must be answerable later; how it is designated is not hard to reverse |
| module/type ownership | No | **Yes** | The binding constraints already exist in the hold's consequences (no `:operational` module merely to materialise identity; not Governance-owned). Placement is reversible |
| registration/uniqueness authority | **Only the negative**: the semantics require no central registry | Yes, whether one is ever needed | ADR-0013 retained requirement 4 already states this; the positive question has no consumer |
| coordination mechanism | **No — and must not be settled** | Yes | Choosing one would violate the infrastructure-independence constraint. **But** the divergence-evidence obligation (§7.4) *must* be settled: it is semantic, not mechanism |

### What ADR-0013 must actually decide before moving from Proposed toward Accepted

The referent; the equality rule; the continuity rule stated representation-independently; the minimum
semantics of acceptance; that divergence requires a new identity under one rule for deliberate and
accidental cases; that lineage is mandatory and immutable with a determinate divergence point; the
per-record divergence-evidence obligation; that exclusivity is an obligation whose violation is
recorded rather than repaired; that convergence is never equality; that already-written identity
bindings are immutable; that unproven continuity fails safe to fork; the record-attachment boundary
at the granularity of §7.7; that raw observations never carry the identity at ingestion; non-reuse;
and the existing non-derivation and non-inference constraints.

Everything else — name, representation, encoding, canonicalization, persistence, storage model,
divergence-point representation, module placement, registration authority, and any coordination
mechanism — may and should be deferred **on the record**. Closure and retirement must be left
explicitly open rather than decided in either direction.

---

## 12. Qualifications that must survive reconciliation

**This section is a contract for the reconciliation pass.** Each item is a change the reconciliation
author must make; none may be dropped as stylistic. Cross-references point at the analysis.

**Q1 — Restate continuity representation-independently.** (§7.1) Continuity is broken by loss of the
account's ability to answer for an already-accepted accountable fact, not by loss of original bytes.
Declared, bounded, recorded retention, compaction, summarisation, and storage or representation
migration that preserve attributability are **not** forks. Silent loss is a fork. Declared loss is a
fork carrying a recorded gap. Ground this in ADR-0015's "attribution plus a verifiable definition,
not permanent exact re-execution" and ADR-0011 §8's gap-detection rule. Select no persistence
technology.

**Q2 — State the minimum semantics of acceptance.** (§7.2) Authority act at a commit boundary;
monotonic; corrections and retractions are new accepted records, never un-acceptance; non-accepted
material is outside the set; the accepted set must be determinable by the account. Defer only the
concrete storage/arrival model.

**Q3 — Add the divergence-evidence obligation.** (§7.4) Every accepted record must carry evidence
sufficient to place it relative to the accepted material its writer extended. Without this, the
claim that divergence is detectable is **false**, and undetectability is not retrofittable onto
records already written. State it semantically; name no mechanism. This is the single qualification
whose omission would do the most durable damage.

**Q4 — Re-type exclusivity.** (§7.3) Continuity condition = factual; exclusivity = normative
obligation whose violation is recorded and remediated forward. Required for consistency with
"already-written records keep the identity they were written under."

**Q5 — Do not foreclose closure or retirement.** (§7.6) Assert non-reuse and binding immutability.
Do **not** assert that a continuation can never close, retire, or become non-extendable. Record it as
open pending a concrete consumer.

**Q6 — Narrow the record-attachment boundary.** (§7.7) Arcogine's request, submission, and
interpretation carry the identity. Target acknowledgement, external accept/reject, physical
transition, and telemetry do not. Preserve the five-way distinction the architecture already
requires.

**Q7 — Separate entitlement from continuity.** (§7.11) Keep "possession of the identifier confers
nothing." Recast holding the accepted record set as a necessary continuity condition and as evidence
for a later decision. Leave entitlement and authority to the actor/trust/authority research; do not
settle them in ADR-0013.

**Q8 — Fix the fork trigger's precision.** (§7.9) "At least when the continuity condition fails, and
permitted at any time by explicit declared fork."

**Q9 — Re-ground the single-identifier choice.** (§7.13) Present it as an ADR-0008 consistency
decision, and record that mature external systems commonly use two-level identity. Do not present
the two-level model as falsified — no proving case falsified it.

**Q10 — Carry authority status honestly.** (§4.2) The Operational architecture document is
**Proposed in its entirety**, ADR-0016 is **Proposed**, and ADR-0013's own retained requirements are
Proposed. Only ADR-0004, 0006, 0008, 0011, 0012, and 0015 and current architecture prose are
Accepted authority. A reconciliation that cites Proposed material as established would repeat the
error this review had to correct.

**Q11 — Do not import non-existent planning coordinates.** (§4.2) The `OPS`-track coordinates the
report cites do not exist; Operational implementation is `NOT ADMITTED`. Reconciliation must not
create them, cite them, or treat them as sequenced work, and no reserved delivery coordinate may
enter ADR-0013 or any durable document.

**Q12 — State the Charter reading explicitly.** The Charter §7 parenthetical "(simulation, replay,
staging, production)" and §6's "hypothetical branches" clause must be addressed in the ADR as
requiring *distinguishability*, satisfiable relationally, and not as mandating the withdrawn global
taxonomy. This review independently confirms the report's reading: §7 is stated as a conceptual
consequence, and §6 requires that a user can tell them apart, which per-subject relationships and the
fork rule satisfy. Left unstated, §7 is the most likely route by which the withdrawn enum returns.

---

## 13. Remaining unknowns

**Genuine blockers to ADR-0013 reconciliation** — must be resolved *within* the reconciliation, not
after it:

- Nothing that requires further research. Every qualification in §12 is statable from authority the
  repository already carries. The blocker is that Q1–Q4 and Q6–Q7 are **not optional edits**: an
  ADR reconciled without them would carry a rule that is unevaluable (Q2), unenforceable in its own
  terms (Q3), internally inconsistent (Q4), or overreaching (Q6, Q7).

**Legitimate deferred implementation details** — dormant until a first durable operational record
capability exists:

- The concrete acceptance boundary: arrival ordering, late arrival, multi-writer commit semantics.
- The representation of the divergence point and of carried-forward evidence.
- Type name, representation, encoding, canonicalization, persistence, module placement.
- Whether any registration or uniqueness authority is ever needed.

**Separate future research topics** — not this question, and not blockers:

- **Actor, trust, and authority** (already `CANDIDATE`). §7.11 confirms it is genuinely separable
  from identity continuity; entitlement to extend belongs there.
- **Closure/retirement semantics** (§7.6). Should be recorded as a follow-up trigger rather than
  decided now.
- **Whether multi-parent lineage is ever required** (§7.10). Deferred on ADR-0008 precedent;
  structurally extensible.
- **What binding to an accumulating identity means over time** (§7.8) — the point-versus-accumulating
  asymmetry the Governance `Evidence`/`EvidenceUse` precedent does not cover.
- **Whether an inspection or analysis session needs its own durable identity** — raised by the report,
  correctly out of scope.
- **Federated multi-instance extension of one account** — permitted by the rule, untested, and now
  additionally dependent on Q3's evidence obligation to remain meaningful.

**Consumer-triggered questions that should remain dormant:** coordination and enforcement mechanisms;
distributed operational deployment; and the regulated-records continuity obligations (GxP-style) the
report lists as uncovered — none is load-bearing for the surviving rule, though the last could add
obligations to failure semantics if Arcogine ever operates under such a regime.

**Surfaces this review could not inspect:** the DRBD internals section's exact wording (§8), and the
external sources listed there as not re-verified. No conclusion in this review depends on them.

---

## 14. Disposition

```text
ACCEPT WITH QUALIFICATIONS
```

**Why.** The load-bearing conclusion survives. The referent — one accountable operational
continuation — was independently reconstructed as the leading candidate before the report was read,
survived all twenty-two proving cases, and was not falsified by any of them. No viable candidate was
omitted: the strongest independent challenger (`F`, no new identity) was already given a serious
hearing and defeated on sound practical grounds, and the second (`T`, two-level) survives the matrix
but is legitimately set aside for consistency with a landed Accepted decision. The fork rule,
mandatory lineage, non-convergence, historical immutability, the raw-observation boundary, identity
separation, and the resolve-now/implement-later timing all survive, several of them strengthened by
independent re-verification of the report's sources.

**Why not ACCEPT.** Eight substantive claims do not survive as written, and two of them are
load-bearing rather than cosmetic: the continuity condition is representation-dependent and would
classify legitimate compaction and storage migration as identity forks (§7.1), and the detectability
guarantee is unsupported because ancestry comparison alone cannot detect an undeclared fork (§7.4).
A third — the acceptance boundary — is deferred in full when its minimum semantics cannot be (§7.2).
Two further claims overreach outside this question's scope (§7.6, §7.11), and one inverts an
ownership boundary the architecture explicitly requires (§7.7).

**Why not MORE EVIDENCE REQUIRED.** Every defect is repairable now, from authority the repository
already carries — ADR-0008's acceptance and immutability rules, ADR-0015's attribution-not-
re-execution guarantee, ADR-0011's gap-detection discipline, and the current Governance
`Evidence`/`EvidenceUse` architecture. No further external research is needed to state the corrected
rule, and no unresolved external question changes the referent.

**Why not REOPEN.** No load-bearing conclusion was falsified, and no omitted model materially changes
the question. The defects narrow, re-type, and re-ground the rule; none of them touches the referent.

**Independence caveat, restated so it is not lost:** this pass satisfies the fresh-isolated-session
condition and not the different-researcher/model-family preference, which could not be verified.

---

## 15. Reconciliation handoff

### Safe to promote into ADR-0013

The sixteen surviving invariants in §9, **as worded there** — which is the report's model with
qualifications Q1–Q4 and Q6–Q9 already applied. In particular: the referent; the establishment-act
form; the representation-independent continuity rule; the minimum acceptance semantics; one
divergence rule for deliberate fork and stale restore; mandatory immutable lineage with a determinate
divergence point; the per-record divergence-evidence obligation; exclusivity as a recorded
obligation; convergence never being equality; historical immutability; fail-safe-to-fork;
the narrowed record-attachment boundary; the raw-observation boundary; identity separation and
non-inference; non-reuse; and settling the semantics now while the type waits for its first consumer.

### Must not be promoted yet

- The report's literal wording of continuity condition (i) — over-broad (§7.1).
- Exclusivity stated as a condition on identity preservation (§7.3).
- "Divergence is detectable by comparing recorded ancestry" as an unqualified guarantee (§7.4).
- "The account never terminates" — in either direction; leave closure open (§7.6).
- "Commands and their lifecycle" as one identity-bearing unit (§7.7).
- "Entitlement derives from holding the accepted record set" as an authority rule (§7.11).
- Any claim that the two-level identity model was falsified by evidence (§7.13).
- Any `OPS`-track planning coordinate, or any suggestion that Operational implementation is
  sequenced (§4.2).
- Any Proposed material — the Operational architecture document, ADR-0016, or ADR-0013's own retained
  requirements — cited as established (§4.2, Q10).

### Must be preserved as qualifications

Q1 through Q12 in §12, in full. Q3 is the one whose loss would be least visible and most damaging: a
durable operational record capability designed without the carried-forward evidence obligation makes
divergence permanently undetectable, and no later ADR can repair records already written.

### Next action

**The next correct slice is ADR-0013 reconciliation** — a separate, independently reviewed change
that rewrites the Proposed ADR around the §9 invariants under the §12 qualifications, and nothing
else.

It is not further bounded research: no unknown blocks the reconciliation, and every qualification is
statable from existing repository authority. It is not another adversarial pass on the source report:
the report's load-bearing conclusion has now been independently tested, and a second pass on the same
text would not change the candidate set, a proving case, confidence, or the durable consequence
(`docs/development/researching.md` §8).

Two constraints on that slice. First, it must remain ADR-only: it must not admit Operational
implementation planning, create planning coordinates, introduce an Operational type or module, or
select representation, persistence, or coordination technology. Second, the source report's own
persistence — its reserved delivery coordinates, its references to non-existent planning
coordinates, and its pre-`report-template.md` structure — is separate cleanup work, deliberately not
performed here so that the artifact under review remained unmodified.

This review performs none of that work. It is research evidence only.

---

## Sources

Re-verified during this review (see §8 for what each supports and where it breaks):

- PostgreSQL Documentation, *Continuous Archiving and Point-in-Time Recovery* — "Timelines".
  <https://www.postgresql.org/docs/current/continuous-archiving.html>
- Microsoft Learn, *backupset (Transact-SQL)* — `first_recovery_fork_guid`, `last_recovery_fork_guid`,
  `fork_point_lsn`, `database_guid`, `family_guid`. Documentation dated 2024-02-29, retrieved
  2026-09-09. <https://learn.microsoft.com/en-us/sql/relational-databases/system-tables/backupset-transact-sql>
- LINBIT, *The DRBD User's Guide*, version 9.0 — generation identifiers; split brain notification and
  auto-recovery. **Partially verified**: the internals section's exact wording could not be retrieved
  in this session. <https://linbit.com/drbd-user-guide/drbd-guide-9_0-en/>
- Temporal Documentation, *Workflow Id and Run Id* — Workflow Id Reuse Policy, Workflow Execution
  Chain, `first_execution_run_id`, `original_execution_run_id`.
  <https://docs.temporal.io/workflow-execution/workflowid-runid>
- Apache Kafka, *KIP-516: Topic Identifiers* — Motivation.
  <https://cwiki.apache.org/confluence/display/KAFKA/KIP-516%3A+Topic+Identifiers>
- IDTA, *Specification of the Asset Administration Shell* — Part 1 Metamodel and Part 2 API
  (`GetAllAssetAdministrationShellIdsByAssetLink`); multiple shells per asset.
  <https://industrialdigitaltwin.org/en/content-hub/aasspecifications>

Not re-verified in this review and not relied upon by any conclusion here: Oracle Database Backup and
Recovery User's Guide (incarnations, DBID, orphan incarnations); IEC/ISO 81346 aspect structuring;
the consensus/epoch literature and the event-sourcing/stream-identity material, both of which the
source report itself labelled background requiring verification before citation.

Repository material is cited inline by path and is authoritative as of live `main`
`5d010e2aa79e36ca6a9793ebf305ba309e3da5a7`.
