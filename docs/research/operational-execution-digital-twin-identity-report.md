# The Accountable Continuation

**Architecture research — alaiba/arcogine — ADR-0013 / PLAN-OPS-1**

What ADR-0013's durable operational identity actually refers to, and exactly when two operational records belong to one continuing history.

- **main:** `2acee74723aad7a9ca384459f5d44ef35b85b6fd`
- **Verdict:** Revise substantially — referent resolved
- **Read-only:** no repository state changed

---

## 1. Executive conclusion

**RECOMMENDATION — Yes, a durable Arcogine-owned identity is required**, but it is not a new *kind* of thing. It is a reference to one specific operational record: the act that established an account. The concept survives every required proving case; the withdrawn kind-bound model does not need reviving in any form.

### What it identifies

One **accountable operational continuation**: the unit of Arcogine's own accountability for the operational facts it records and the interpretations it forms. It is a body of Arcogine's *conduct and conclusions* — commands issued, deployments applied, correspondences asserted, reconciliations reached, drift analysed — maintained as one account that may be extended over time.

It is **not** the physical installation, not the modelled system, not the runtime, not the deployment, not the twin-as-such, and not a classification of how real anything is.

### The continuity rule, in one sentence

> Identity is preserved by any continuation that loses no record the account has already accepted, and that is alone in extending it; a new identity is required exactly when either of those fails.

### The fork rule, in one sentence

Divergence — beginning from a selected earlier point, or from a set missing records the account had accepted — always establishes a **new** identity, carrying mandatory lineage naming the parent identity and the divergence point. Deliberate forks and stale restores are the same rule; only the annotation differs. Convergence is never identity equality.

### What it must not be confused with

`RunId` (a runtime incarnation), `ModelFingerprint` (semantic content), `ControlledRevisionId` (a historical revision *point*), `EngineSemanticsVersion` (interpretation rules), deployment identity (infrastructure realisation), external subject identity (the thing in the world), and observation identity (what Arcogine was *told* rather than what it *did*).

### Is this enough to revise ADR-0013 toward acceptance?

**RECOMMENDATION — Yes, for the referent and the lifecycle rule.** Three qualifications, each surfaced by adversarial review and folded into §6 and §10, must be written into the ADR rather than papered over: continuity is defined by *record loss*, not by prefix of a log; exclusivity can be *claimed and detected* but not *proven at write time*; and a divergence violation is **recorded, never repaired by relabelling** already-written records. With those stated, the remaining open items are representation, issuance and sequencing — which the ADR itself already defers.

---

## 2. Repository baseline

**REPOSITORY FACT** Investigation SHA: `2acee74723aad7a9ca384459f5d44ef35b85b6fd` (`origin/main`, tip *docs(planning): capture agency decision boundary investigation (#288)*). Working tree clean; no repository state was modified.

### Current Arcogine identities

| Identity | Referent | Evidence |
|---|---|---|
| `ModelFingerprint` | Deterministic semantic content identity under a named, versioned fingerprint policy. Equality means equality of canonical semantic content. | `product/types/…/ModelFingerprint.java`; ADR-0004, ADR-0006 |
| `ControlledRevisionId` | One immutable controlled historical *occurrence*. Opaque UUIDv4; never derived from fingerprint, label, parent, author, time or state. `parentRevisionIds` 0..1. Rollback is a new revision, never a reused one. | `product/types/…/ControlledRevisionId.java`; ADR-0008 (Accepted) |
| `RunId` | Opaque correlation identity for one fresh runtime session. New on every `reset()`. Must never affect deterministic outcome. | `product/types/…/RunId.java`; ADR-0011 §4; ADR-0007 |
| `EngineSemanticsVersion` | Complete result-affecting Engine interpretation for a run. Fixed for one run; independent dimension from `RunId`. | ADR-0015 (Accepted) |
| Actor / target / external subject | Not yet implemented. Actor semantics deliberately unowned (PLAN-OPS-2); external subject correspondence must be *asserted by an authority*, never inferred. | `operational-execution-digital-twin.md` §5, §8.1 |
| Deployment / command / observation | Not yet implemented. Architecturally required to remain separate from each other and from the open identity. | `operational-execution-digital-twin.md` §6, §7, §8 |

### Current Operational architecture assumptions

**REPOSITORY FACT** Operational Execution is **unimplemented**. Governance fingerprint/revision identity, semantic change, requirements/assertions and initial conformance are implemented and authoritative. Engine runtime observation/event headless closure is complete. Simulation-first remains the current-state claim.

Four architectural positions constrain any answer. Synthetic versus operational is **not** a global execution kind (§2). Digital twin is **relational**, and no top-level `DigitalTwinId` is justified by the concept alone (§2.1). Seek, replay, checkpoint/restore and fork are **four distinct capabilities**, not synonyms (§10). No higher-rank execution identity above `RunId` may be introduced until a concrete capability proves its lifecycle rule (§10).

### ADR-0013's withdrawn model and retained requirements

Withdrawn from the implementation contract: `ExecutionContextKind`, the `PRODUCTION / STAGING / SIMULATION` taxonomy, immutable `{id, kind}` values, permanent ID-to-kind binding, and any use of a global environment classification as an authorization or safety input. No replacement enum was introduced.

Retained: (1) a durable Arcogine-owned identity is still required; (2) neighbouring identities stay distinct; (3) identity is established explicitly, never inferred from infrastructure; (4) no central registry is required to state the semantics; (5) raw external observations remain independently provenanced; (6) external representations remain projections under ADR-0012.

### The exact unresolved question

> What exactly is the independently continuing operational history/partition that needs durable identity, and what makes two records belong to the same one versus different ones?

### Two repository facts the ADR revision must handle explicitly

**REPOSITORY FACT** **Charter §7 lists the withdrawn vocabulary.** `docs/product/charter.md` §7 states that "Multiple execution contexts (simulation, replay, staging, production) must be distinguishable without losing model continuity," and §6 requires that "Simulation, replay, staging, hypothetical branches, and live production must never be ambiguously presented or confused."

**ARCHITECTURAL INFERENCE** This is Charter-level normative text naming those as *examples of things a reader must be able to tell apart*, not as a mandated enumeration on one identifier. Distinguishability is satisfiable relationally — per subject, per realisation, per command — and the fork rule recommended here actively serves Charter §6's "hypothetical branches" clause by giving a hypothetical continuation its own identity with recorded lineage. The revised ADR should say so, because §7 read carelessly is the most likely route by which the withdrawn taxonomy gets revived.

**REPOSITORY FACT** **Arcogine is explicitly not event sourced** (ADR-0011 §2), and `EventLog` is an internal scheduler trace, not a supported journal (§8). Any continuity rule phrased as "a prefix of the log" would contradict this. §6 below is phrased to avoid it.

---

## 3. External evidence

Only sources that materially bear on the *referent*, *equality rule*, *continuation rule* or *fork rule* appear here. Each entry marks whether it was fetched and verified during this investigation or is stated from background knowledge and still needs checking.

### PostgreSQL timelines — *Verified in session*

**Source:** PostgreSQL documentation, *Continuous Archiving and Point-in-Time Recovery* §"Timelines" — postgresql.org/docs/current/continuous-archiving.html

**Semantics.** "Whenever an archive recovery completes, a new timeline is created to identify the series of WAL records generated after that recovery." The documented motivation is explicitly the divergence problem: recovery creates "complexities that are akin to science-fiction stories about time travel and parallel universes… you need to distinguish the series of WAL records generated after you've done a point-in-time recovery from those that were generated in the original database history." Lineage is mandatory and durable: "Every time a new timeline is created, PostgreSQL creates a 'timeline history' file that shows which timeline it branched off from and when." Crucially, the abandoned branch is *not* destroyed — "you can recover to *any* prior state, including states in timeline branches that you abandoned earlier."

**Analogy.** This is the closest available analogue to the recommended rule. A plain crash restart preserves the timeline; a recovery that resumes from an earlier point creates a new one with recorded ancestry. That is precisely "loss of accepted records ⇒ new identity, with lineage."

**Where it breaks.** A timeline is defined over a strictly ordered WAL. Arcogine's operational records are heterogeneous and concurrently produced, and Arcogine is not event sourced. The *rule* transfers; the *linear-log mechanism* must not.

**Implication.** Divergent continuation gets a new identity plus mandatory lineage, and the abandoned branch remains addressable rather than being overwritten. Strong external support for both the fork rule and for refusing to rewrite history.

### Oracle database incarnations and DBID — *Verified in session*

**Source:** Oracle Database Backup and Recovery User's Guide, *RMAN Data Repair Concepts* — docs.oracle.com/en/database/oracle/oracle-database/19/bradv/rman-data-repair-concepts.html

**Semantics.** A new incarnation is created by `OPEN RESETLOGS`. The lineage vocabulary is explicit: *current* (generating redo now), *parent* (the incarnation the current one branched from at `RESETLOGS`), *ancestor* (transitively), and *orphan* — "a noncurrent incarnation that is not a direct ancestor of the current incarnation." The DBID remains consistent across all incarnations of the same database.

**Analogy.** Two-level identity: a stable identifier surviving recovery, plus a branch identifier that changes on divergent continuation. "Orphan" names precisely the abandoned tail that a stale restore leaves behind.

**Where it breaks.** DBID's job is to stop backup pieces from different databases being mixed — a physical-file-set safety mechanism, not a semantic history identity. It is not evidence that Arcogine needs a second super-identity above its own; Arcogine's equivalent of "same database across all incarnations" is *transitive lineage*, which ADR-0008 already chose over branch objects.

**Implication.** One identifier plus mandatory lineage is sufficient; resist a second identifier unless a concrete Arcogine capability demands one. Also supplies the *orphan* concept for §10.

### SQL Server recovery forks — *Verified in session*

**Source:** Microsoft Learn, *Recovery Paths*; `backupset` (Transact-SQL) columns `first_recovery_fork_guid`, `last_recovery_fork_guid`, `fork_point_lsn`; `RESTORE HEADERONLY`

**Semantics.** A recovery path is "a specific set of transformations that have evolved the database over time while maintaining consistency," described as a range from a start `(LSN, GUID)` to an end `(LSN, GUID)`. The continuity check is mechanical: for each log backup in a restore sequence, `first_recovery_fork_guid` must equal the `last_recovery_fork_guid` of the prior backup. Where they differ, `fork_point_lsn` records the divergence point; otherwise it is null.

**Analogy.** A mature system expresses "is this a legitimate continuation?" as an explicit identity-and-point check against recorded lineage, not as an inference from infrastructure or intent. That is the shape §6 recommends.

**Where it breaks.** LSN ordering again presumes a single log. Arcogine needs the same check expressed over an unordered accepted set.

**Implication.** Continuity is a checkable relation between a continuation and a recorded ancestry, and "fork point" is a first-class recorded fact, not a narrative.

### DRBD generation identifiers and split brain — *Verified in session*

**Source:** LINBIT, *The DRBD User's Guide* 9.0, §"Generation Identifiers" and §"Split brain notification and auto-recovery"

**Semantics.** Each node carries a current UUID plus history UUIDs recording data lineage. Split brain — both nodes promoted while disconnected — is *detected* on reconnection by comparing generation identifiers: divergence is the case where two nodes' current UUIDs do not appear in each other's history, meaning independent modification without replication.

**Analogy.** This is the direct answer to the strongest objection against the recommended rule ("detectable by *what*?"). Divergence is detected *retrospectively by ancestry comparison*, using recorded lineage — a semantic property — rather than by consensus at write time, which is infrastructure.

**Where it breaks.** DRBD then *discards* one side's changes under a configured victim policy. Arcogine must not: its records are accountability records, and discarding them would be falsification. Arcogine keeps both and records the divergence.

**Implication.** Split brain is representable as divergent ancestry, and detection does not require the model to specify any coordination mechanism.

### Temporal: Workflow Id versus Run Id — *Verified in session*

**Source:** Temporal documentation, *Workflow Id and Run Id*; *Workflow Execution*

**Semantics.** Workflow Id is "a customizable, application-level identifier… unique to an Open Workflow Execution within a Namespace" — **user-supplied**, carrying business meaning. Run Id is "a globally unique, platform-level identifier," system-generated, and explicitly mutable across the chain: "You shouldn't rely on storing the current Run Id… because a Workflow Retry changes the Run Id." A Workflow Execution Chain is "a sequence of Workflow Executions that share the same Workflow Id," and lineage is recorded through `first_execution_run_id` and `original_execution_run_id`.

**Analogy.** A mature durable-execution platform separates the declared, durable, business-meaningful identity from the system-generated incarnation — and puts the durable one on the *declared* side. That is direct support for "established by an explicit act, not derived."

**Where it breaks, importantly.** A workflow execution *terminates*; the Workflow Id Reuse Policy exists precisely because a Workflow Id is reusable after close. An Arcogine operational history is open-ended, may lie dormant for years and be resumed, and must never have its identity reused. Workflow-instance identity is therefore a good analogy for the *split* and a bad analogy for the *lifecycle*.

**Implication.** Adopt the declared-identity/generated-incarnation split; explicitly reject reuse semantics and any notion of a terminating history.

### Kafka topic IDs (KIP-516) — *Verified in session*

**Source:** Apache Kafka, *KIP-516: Topic Identifiers*, Motivation

**Semantics.** Names are not identity. Deleting and recreating a topic with the same name leaves stale replicas indistinguishable from current ones; the documented failure is that "it is possible for partitions to be reassigned away from brokers while they are down and there are no guarantees that this state will ever be cleaned up." Topic IDs give "true uniqueness across the topic's lifecycle, not merely across concurrent topics."

**Analogy.** Direct evidence for opacity and non-reuse: a human-meaningful name for a plant or line is not an operational history identity, and reusing a retired identity produces exactly this class of silent corruption.

**Where it breaks.** Kafka's identity has no fork semantics at all — a topic never branches. It settles reuse, not divergence.

**Implication.** The identity must be opaque and never reused, even after a history is retired. Do not permit a site code, plant name or line designation to serve as it.

### Asset Administration Shell: many shells, one asset — *Verified in session*

**Source:** IDTA-01001-3-0-1, *Specification of the Asset Administration Shell, Part 1: Metamodel*; IDTA questions-and-answers

**Semantics.** The AAS carries its own identifier (`id`) distinct from the asset's (`AssetInformation/globalAssetId`). Multiple shells for one asset are the normal case — the canonical illustration is two AAS for a single asset, one managed by the manufacturer and one by the operator — and the digital twin is described as the aggregation of all AAS sharing a global asset ID. Discovery is explicitly many-to-one: `GetAllAssetAdministrationShellIdsByAssetLink` resolves an asset ID to *multiple* shell IDs.

**Analogy.** Normative support for the decisive proving case: several independent interpretations of one physical installation carry **different** identities, related to one another only through the shared external subject.

**Where it breaks.** An AAS is a *description* of an asset, not a record of conduct, and the spec provides no continuity or fork semantics for a shell across restart, migration or divergence. It settles the multiplicity question and nothing else.

**Implication.** The referent cannot be the physical installation. Two twins of one plant have two identities; the plant is a shared *relatum*, not the identity.

### IEC 81346 aspects: function, product, location — *Verified in session*

**Source:** IEC/ISO 81346 series, *Industrial systems, installations and equipment and industrial products — Structuring principles and reference designations*

**Semantics.** Three orthogonal aspects, each with its own prefix sign: the function aspect (`=`) is what the object does or is intended to do; the location aspect (`+`) is where it can be found; the product aspect (`-`) is how it is constructed. A designation such as `=H10+UCB01-CF001` names one object through all three lenses, and the aspects vary independently.

**Analogy.** Answers the hardware-replacement case in the external world: replacing a device changes the product aspect while the function designation persists. Industrial practice already separates "the role being filled" from "the thing filling it."

**Where it breaks.** All three aspects designate things in the plant. None of them designates a *record of operating* the plant. 81346 confirms that Arcogine must not identify its history by equipment; it does not supply the referent.

**Implication.** Equipment replacement is an event recorded *within* a history, never a change of history. Correspondence should be able to target a functional designation, not only a serialised device.

### Consensus terms, epochs and fencing tokens — *Background, verify before citing in the ADR*

**Source:** Ongaro & Ousterhout, "In Search of an Understandable Consensus Algorithm (Raft)", USENIX ATC 2014; Burrows, "The Chubby lock service", OSDI 2006 (sequencers); Kleppmann, "How to do distributed locking", 2016 (fencing tokens); Das, Gupta & Motivala, "SWIM", DSN 2002 (incarnation numbers).

**Semantics.** Across this tradition, a monotonically increasing term/epoch/ballot/incarnation number establishes *which claim of authority is newer* so that stale claims can be rejected. The number orders claims; it does not name the entity whose authority is being claimed — that entity is identified separately.

**Where it breaks.** This is the sharpest available warning for Arcogine: an epoch is *ordering and authority*, not *identity*. Importing a fencing token as the durable identity would reproduce exactly the `RunId` conflation ADR-0013 forbids. These mechanisms are also infrastructure, which ADR-0013 §3 excludes from the semantics.

**Implication.** Keep incarnation strictly separate from history. If Arcogine ever needs to arbitrate concurrent claims, that is an authority mechanism layered over the identity, never a redefinition of it.

### Event sourcing, stream identity and snapshots — *Background, verify before citing in the ADR*

**Source:** Fowler, "Event Sourcing" (martinfowler.com, 2005); Vernon, *Implementing Domain-Driven Design* (2013), on aggregate identity; EventStoreDB stream semantics including tombstoned streams whose names cannot be reused.

**Semantics.** Stream identity equals aggregate identity, assigned at creation and never derived from content; a snapshot is a derived optimisation and never a separate identity; rebuilding from a snapshot is continuation, not a new stream.

**Where it breaks, decisively.** An event-sourced stream *is* the authoritative state — replaying it reconstitutes truth. Arcogine's operational history is *not* the authoritative state of the world: the world is, and the history is Arcogine's account of what it did and concluded about it. ADR-0011 §2 states Arcogine is not event sourced. The superficial resemblance is real and must be resisted at the level of mechanism.

**Implication.** Borrow "identity assigned at creation, never derived, never reused"; reject "the log is the truth," total ordering, and replay-as-reconstitution.

### Where sources disagree

On divergence, the traditions split cleanly by what their records are *for*. Database recovery systems (PostgreSQL, Oracle, SQL Server) treat divergence as **two branches that both continue to exist**, with lineage recorded and the abandoned branch still addressable. Replication systems (DRBD, and unclean leader election in general) treat divergence as **an error to be resolved by discarding one side**. **ARCHITECTURAL INFERENCE** Arcogine belongs firmly in the first camp: its operational records are accountability records, and a recovery policy that deletes what the system actually did is falsification, not repair. The disagreement is therefore not a genuine conflict of evidence — it reflects whether the records are *state* (discardable) or *history* (not).

---

## 4. Candidate referents

| Candidate | Definition | Equality / continuity | Creation & termination | Restart / failover | Fork & lineage | Strength | Failure case |
|---|---|---|---|---|---|---|---|
| **A — Operational history** *(sharpened; recommended)* | One accountable continuation of Arcogine's own operational conduct and conclusions. | Same establishment act, extended without record loss and without a rival extender. | Created by an explicit authoritative act (genesis or fork). **Never terminates** — a history may go dormant and later resume. | Preserved when the whole accepted set is carried; new when records are lost. | New identity; parent + divergence point mandatory. Convergence ≠ equality. | Only candidate that answers all fourteen cases without appealing to infrastructure or to a reality classification. | Exclusivity is claimable but not provable at write time (see §10). |
| **B — Operational partition** | An independently continuing partition of operational authority. | Same as A in intent. | Same as A. | Same as A. | Same as A. | Captures the "independence" intuition ADR-0013 reaches for. | **The word is the problem.** "Partition" imports data-sharding and network-partition connotations, inviting exactly the infrastructure derivation ADR-0013 §3 forbids. Fold the intuition into A; drop the term. |
| **C — Logical execution** | One long-lived logical execution above runtime incarnations. | "Same logical execution" — undefined without further rules. | Starts when execution starts; unclear when it ends. | Intended to survive both. | Unclear; forking an execution is not a natural notion. | Matches the durable-execution literature. | **Fails.** "Execution" implies running: a dormant, decommissioned, or purely record-keeping history is not executing, and historical inspection has no execution at all. It also sits one word away from `RunId` and will be conflated with it. This is the phrase the success condition explicitly bans. |
| **D — Digital-twin instance** | One twin's continuing interpretation of reality. | Same twin. | Created when a twin is established. | Survives both. | A predictive fork is a new twin. | Handles multiple twins of one plant well, corroborated by AAS. | **Fails twice.** §2.1 states twin-ness is relational and no top-level twin identity is justified by the concept. And a history may contain zero external subjects — a purely synthetic operations record is still an accountable history — so it cannot be a twin identity. |
| **E — Operational system instance** | One logical operational realisation of the production system. | Same realisation. | Created when the realisation is stood up. | Intended to survive both. | Awkward: a fork is not a second realisation. | Intuitive to operators. | **Fails.** "Realisation" and "instance" drift straight toward deployment, which ADR-0013 §3 forbids. It cannot explain two twins of one plant — are they two realisations of one system, or one? The model gives no answer. |
| **F — No new identity** | Existing identities plus explicit succession relations and provenance. | Transitive closure of a declared `continues:` relation, not crossing a fork. | Implicit in the first record. | Expressible. | Expressible as a relation. | **Semantically equivalent to A** and adds no new concept. Deserves the serious hearing §6 gives it. | Three practical defeats: a writer cannot decide entitlement without traversal; heterogeneous concurrent records have no single chain to belong to; and an exported record (ADR-0012) would have to carry the whole graph to be self-describing. |
| **G — Establishment-act reference** *(the reconciliation)* | Not a new kind of thing: the identity *is* a reference to the record that established the account. | Same establishment act. | Created by that act; the act is itself a durable operational record. | Unchanged by either. | A fork is a new establishment act naming a parent and a point. | Gives A its referent while conceding F's point that no new *category* is needed. Fewest new shared concepts. | Requires the establishment record to be durable before the identity is authoritative — the same rule ADR-0008 already applies to revisions. |

**ARCHITECTURAL INFERENCE** A and G are not competitors. **A supplies the referent; G supplies its form.** The recommendation is A-as-referent, materialised as G — which is also the honest resolution of F, because the label is a reference to a record rather than a new species of identifier.

---

## 5. Proving-case matrix

Fourteen required cases against five candidate models. A = recommended referent; C, D, E, F as defined in §4.

| Case | A — Operational history | C — Logical execution | D — Twin instance | E — System instance | F — No new identity |
|---|---|---|---|---|---|
| Process restart | **PRESERVED** — no accepted record lost; one extender. Infrastructure is not mentioned by the rule. | AMBIGUOUS — is a restarted process the same execution? Undefined. | PRESERVED | PRESERVED | PRESERVED — succession relation declared. |
| Active/passive failover — full replication | **PRESERVED** — standby holds every accepted record; old primary stops extending. | AMBIGUOUS | PRESERVED | AMBIGUOUS — two realisations or one? | PRESERVED |
| Active/passive failover — lagging | **NEW** — standby is missing accepted records. Fork from the last common point; the lost tail becomes an orphan branch. | AMBIGUOUS | AMBIGUOUS — the twin still mirrors one plant. | AMBIGUOUS | NEW — only if the relation is declared honestly. |
| DR failover | **PRESERVED or NEW** — preserved if the DR site holds the full accepted set; otherwise a fork. The choice is an explicit, recorded act, not an inference. | AMBIGUOUS | AMBIGUOUS | AMBIGUOUS | PRESERVED or NEW |
| Commissioning → production | **PRESERVED** — the rule never mentions consequence, authority or lifecycle stage. The commissioning record and the production record are one auditable account. | PRESERVED | PRESERVED | AMBIGUOUS — tempts a "staging → production" re-derivation. | PRESERVED |
| Gain / loss of telemetry | **PRESERVED** — reality correspondence and history identity are different concepts; observations never carried the identity anyway. | PRESERVED | AMBIGUOUS — does a twin with no telemetry remain a twin? | PRESERVED | PRESERVED |
| Gain / loss of control capability | **PRESERVED** — capability and authority are relationships, not identity. | PRESERVED | PRESERVED | AMBIGUOUS | PRESERVED |
| Hybrid physical/synthetic composition | **NOT APPLICABLE** — the rule is orthogonal to subject composition by construction — it never inspects what a subject is. | NOT APPLICABLE | FAILS MODEL — forces a whole-environment twin judgement. | FAILS MODEL | NOT APPLICABLE |
| Multiple twins of one installation | **DISTINCT** — each was separately established; neither adopts the other's records. They share an external subject, not an identity. Corroborated by AAS. | AMBIGUOUS | DISTINCT — right answer, wrong reason, fails elsewhere. | FAILS MODEL — one installation, one realisation? | DISTINCT |
| Historical inspection only | **NO NEW IDENTITY** — reading is not extending. An inspection may want its own session identity; that is a different concept. | AMBIGUOUS — is loading a snapshot an execution? | NO NEW IDENTITY | AMBIGUOUS | NO NEW IDENTITY |
| Deliberate divergent fork | **NEW** — established at the first accepted record of the continuation, with mandatory parent and divergence point. | AMBIGUOUS | NEW | AMBIGUOUS | NEW |
| Split brain | **UNDECLARED FORK** — an exclusivity violation, detectable by ancestry comparison. Both branches are retained; entitlement to future extension is decided, records are never relabelled. See §10. | FAILS MODEL | FAILS MODEL | FAILS MODEL | UNDECLARED FORK — same answer; harder to detect without a label. |
| Stale checkpoint restore + resume | **NEW** — continuing from a set missing accepted records. Same rule as a deliberate fork; the annotation records that an authoritative tail was abandoned. | AMBIGUOUS | AMBIGUOUS | AMBIGUOUS | NEW |
| Model / revision change mid-operation | **PRESERVED** — the revision change is recorded *within* the history. Semantic revision history and operational history stay orthogonal. | PRESERVED | PRESERVED | AMBIGUOUS | PRESERVED |
| Deployment replacement | **PRESERVED** — many deployments may extend one history; one deployment may host many. Identity is never derived from either. | PRESERVED | PRESERVED | FAILS MODEL — realisation and deployment collapse. | PRESERVED |

### Additional cases raised in adversarial review

- **Merging two histories.** Never identity convergence — that would falsify which account did what. Either record a correlation, or establish a *new* history whose lineage names both parents. Keep the initial contract at 0..1 parent, exactly as ADR-0008 does, and reserve multi-parent as an extension.
- **A history with no records yet.** Valid — established but empty, the direct analogue of a root revision.
- **Federated live operation across two Arcogine instances.** Permitted: many deployments, one history. They carry a joint obligation not to lose each other's accepted records; the mechanism is out of scope.
- **Durable state lost but the identifier remembered.** The accepted extent is unknown, so continuity cannot be proven: a new identity with lineage to the lost one and a recorded discontinuity. **Holding the identifier never confers entitlement to extend the history.**
- **Decommissioning, then resumption years later.** Preserved if the record set is intact. There is no termination event — only "no longer extended." This is precisely where the workflow-instance analogy breaks.
- **Inspection that becomes a continuation mid-session.** Sharply decidable: the continuation begins at the first accepted record, and not before.
- **Shadow / dry-run history mirroring a live one.** Separate identity, same shape as multiple twins. It may consume the same observations; its conclusions are its own.
- **A different software generation operating one history.** Preserved. The identity is not software identity — which is the test for remaining meaningful if Arcogine's implementation technology changes completely.

---

## 6. Equality and lifecycle rule

**RECOMMENDATION** The rule below is stated in semantic terms only. It names no type, representation, storage or mechanism.

**Represents** — One **accountable operational continuation**: a body of Arcogine's own operational conduct and conclusions, maintained as one account for which Arcogine is answerable, and which may be extended over time.

**Established** — By one explicit authoritative act, of exactly one of two forms. **Genesis** — an account begins with no predecessor. **Fork** — an account begins by naming a parent identity and a divergence point in it, adopting the parent's state as of that point and becoming separately answerable thereafter. The identity is a reference to that act. It becomes authoritative when the act's record is accepted into durable operational history, mirroring ADR-0008's rule that historical identity begins at persistence acceptance.

**Preserved** — Across any continuation that satisfies both conditions:

- **(i) No loss.** Every record the account has already accepted remains part of the continuation's prior state. Nothing accepted is dropped, disowned or forgotten.
- **(ii) Exclusivity.** No other continuation is concurrently extending the same account.

Nothing else bears on it. Not hostname, process, container, namespace, profile, URL, storage, database, deployment, actor, target, subject, model, revision, engine semantics, telemetry, control capability, consequence, authority, organisation or lifecycle stage. Any of those may change, repeatedly, while the identity holds.

**Replaced** — A new identity must be established exactly when (i) or (ii) fails: a continuation begins from a selected earlier point or from a set missing accepted records, or a second continuation extends an account already being extended. A fork is required in both cases; only the recorded reason differs.

**Undetermined** — If it cannot be established that no accepted record was lost, continuity is **not proven**. The model fails safe to fork, consistent with the architecture's safety principle that absence of authority is denial and that ambiguity remains representable as ambiguity.

**Lineage** — Every fork records its parent identity, the divergence point, and whether the parent continues. Lineage is mandatory and immutable. Ancestry is carried by these links alone — no branch objects, no second super-identity, exactly as ADR-0008 chose for revisions.

**Equality** — Two records carry the same identity if and only if each was accepted into a continuation descending from the *same establishment act* without an intervening fork. Equality of the identity means "written under one account," and nothing more.

### Three amendments the adversarial pass forced

Each of these corrects a genuine defect in the first formulation, and each is load-bearing.

**1 · Loss, not prefix.** The rule was first written as "adopts the entire accepted *extent*," which presupposes a linear order and would have smuggled event sourcing in through the back door — contradicting ADR-0011 §2. Operational records are heterogeneous and concurrently produced: a deployment record, a correspondence assertion and a reconciliation have no single predecessor chain. **ARCHITECTURAL INFERENCE** The condition is therefore **set-based, not sequence-based**: no accepted record is lost. That is well defined over a partially ordered, multi-writer set, and requires only that acceptance be monotonic — which durable accountability records need anyway.

**2 · Exclusivity is claimed and detected, not proven.** Condition (ii) is not locally checkable. During a split brain both sides sincerely believe they satisfy it, and any actual enforcement — leases, quorum, fencing — is infrastructure, which ADR-0013 §3 puts outside the semantics. **ARCHITECTURAL INFERENCE** What the semantic model owes is **detectability**, not prevention: because every continuation records its predecessor and what it carried forward, divergence is recognisable afterwards by comparing recorded ancestry. DRBD does exactly this. The ADR must state the limitation plainly: a set of records bearing one identity is not *guaranteed* free of divergence; divergence is guaranteed *representable and detectable*.

**3 · Violations are recorded, never relabelled.** Records already written — and possibly already exported under ADR-0012 — carry the identity they were written under. Retroactively relabelling them would be identity mutation, which every other Arcogine identity forbids. **RECOMMENDATION** On discovering divergence, record a divergence finding naming both continuations and the divergence point, and decide which continuation is entitled to *extend* the identity from that decision onward; the other establishes a new identity going forward. History is added to, never rewritten — the same discipline as ADR-0008's "rollback is an ordinary new revision."

### The membership heuristic, correctly demoted

An earlier formulation offered a membership test: two records belong to one history if a contradiction between them would be *an error to resolve* rather than *a difference to compare*. **ARCHITECTURAL INFERENCE** That test is genuinely useful — it separates two twins disagreeing (a difference) from pre- and post-restart records disagreeing (an error) — but it is **a design heuristic for whoever establishes an account, not an equality rule**. It appeals to how records will be used, which two competent architects can read differently. Equality is settled by the succession rule above; the heuristic only helps decide *when to establish a new account in the first place*. The ADR should present it in that subordinate role and never as the definition.

### Why a durable identity is genuinely required

Candidate F deserves the serious hearing the brief demands, and it wins more of the argument than expected: a per-continuation `continues:` relation is **semantically equivalent** to the recommended model. It simply declines to name the equivalence class. Three arguments defeat it, and none of them is "restart survival" — a relation handles that fine.

1. **Entitlement is decided at write time.** A writer must know whether it may extend an existing account *before* writing. Under F that requires traversing the graph; under the recommended model it is a single reference already in hand.
2. **Concurrent heterogeneous records have no chain to belong to.** Succession is a relation between *continuations*, not between records. Naming the continuation lineage is therefore unavoidable — and a name for it is the identity.
3. **Exported records must be self-describing.** ADR-0012 makes external representations projections of semantic contracts. A traversal-only model forces an exported record to carry its whole ancestry graph to state which account it belongs to.

**RECOMMENDATION** The honest conclusion is a reconciliation rather than a defeat: **a durable identity is required, but it is not a new category**. It is a reference to one operational record — the establishment act — used as a membership label. That is the fewest new shared concepts consistent with the requirement.

### What the model can and cannot enforce

Because the identity is declared, a careless establisher can draw the boundary badly, and the model cannot say they were wrong. **ARCHITECTURAL INFERENCE** This is not a defect peculiar to this proposal: nothing stops a nonsense controlled revision being recorded either, and Arcogine already requires external-subject correspondence to be *asserted by an authority* precisely because it cannot be inferred. Enforcement belongs to the authority boundary, not to the identity. What the identity *can* enforce is real and worth stating in the ADR: lineage acyclicity and parent existence; immutability of the record-to-identity binding; non-reuse of a retired identity; detectable divergence; and the record-attachment boundary of §9.

One asymmetry with ADR-0008 is genuine and must be acknowledged rather than glossed. A revision is a *point*, so it carries no exclusivity obligation. An operational history *accumulates*, so it does — and condition (ii) is exactly the extra rule that difference demands.

---

## 7. Fork and lineage semantics

```
source identity      the parent account, which may or may not continue
divergence point     the state of the parent that the child adopted
child identity       established at the child's FIRST ACCEPTED RECORD,
                      not at the moment a snapshot is selected or loaded
lineage               parent identity + divergence point + does-parent-continue
                      mandatory, immutable, single-parent in the initial contract
```

### When the child identity is established

At the first accepted record of the new continuation. Selecting a historical state, loading it, and inspecting it create nothing. This is what makes historical inspection and deliberate fork sharply decidable rather than a matter of intent, and it makes "an inspection that turns into a continuation" a precisely locatable event.

### Is lineage mandatory?

**RECOMMENDATION** Yes, on every fork, without exception. ADR-0013's retained requirement already says a deliberately divergent continuation must not *silently* share one history identity with its source; mandatory lineage is what makes it non-silent. PostgreSQL's timeline history file and SQL Server's fork-point LSN are the same commitment. A fork whose parent is unrecorded is indistinguishable from a genesis, and the difference is exactly what the requirement exists to preserve.

### Does the divergence point need its own identity?

**ARCHITECTURAL INFERENCE** Not a new identity — but it must be **referenceable**. It needs to designate a determinate state of the parent account, precisely enough that "what the child adopted" is answerable later. Whether that is a checkpoint reference, a temporal boundary, or a designated record is a representation question the ADR should defer alongside issuance and persistence.

### May branches later converge?

**Never into identity equality.** Two accounts that separately recorded conduct cannot retroactively become one account without falsifying which of them did what. What is available: a correlation between them, or a *new* account whose lineage names both parents. **RECOMMENDATION** Keep the initial contract at 0..1 parent — exactly ADR-0008's choice — and reserve multi-parent lineage as a later extension, so that the initial decision does not prematurely import merge semantics.

### Stale restore versus deliberate fork

The identity rule is *identical*: both begin from something other than the full accepted set, so both establish a new identity with lineage. Deliberately keeping one rule for both is the point — it removes any incentive to classify a restore as "really a continuation" to avoid an inconvenient new identity. What differs is only annotation, and the difference is worth recording: a deliberate fork typically leaves the parent continuing, whereas a stale restore abandons an authoritative tail. Oracle's *orphan incarnation* — a non-current incarnation that is not an ancestor of the current one — is the established name for what the abandoned tail becomes.

### Replay, reconstitution, checkpoint restore, resume, fork, inspection

The rule keeps all six distinct without needing to name them, because it asks only one question: *was anything accepted lost, and is anyone else extending?*

| Capability | Does it extend the account? | Identity effect |
|---|---|---|
| Historical inspection | No | None. May warrant a separate inspection/session identity, which is a different concept. |
| Seek / reconstitution | No | None — recovering a view of a past state accepts no new record. |
| Replay | Not by itself | None. Derived results belong to whatever account retains them — typically an analysis, not an operational history. |
| Checkpoint restore (current) | Yes, losing nothing | PRESERVED |
| Checkpoint restore (stale) | Yes, losing accepted records | NEW + lineage + abandoned-tail annotation |
| Resume | Yes, losing nothing | PRESERVED |
| Fork | Yes, from a selected point | NEW + lineage |

### Where uncertainty remains

Whether a fork ever needs to be re-parented — for example when a divergence is discovered long after the fact and the recorded parent turns out to be wrong — is unresolved. The immutability discipline says no, and the recording-not-rewriting rule of §6 says a correction is a new record rather than an edit. That is the recommended reading, but it has not been tested against a concrete Arcogine capability.

---

## 8. Relationship to neighbouring identities

Cardinality is stated from the durable operational identity's side.

| Identity / concept | Cardinality | Relationship |
|---|---|---|
| `RunId` | one-to-many, conditional | A run is a runtime incarnation; a history may span many, and many runs belong to no history at all (all current simulation runs do). The two must never be derived from one another — this is the conflation the architecture most explicitly forbids. A run may be *correlated* to a history when it produced records the history accepted. |
| `ModelFingerprint` | many-to-many | Semantic content identity. One history operates under many fingerprints over time; one fingerprint is operated under by many histories. A change of fingerprint is a fact recorded *within* a history. |
| `ControlledRevisionId` | many-to-many | Historical revision *points*; the operational identity names an *accumulating account*. Structurally the closest existing analogue — opaque, declared, explicit lineage, never reused — and the pattern to copy, but a different referent. Revision history is semantic; operational history is conduct. |
| `EngineSemanticsVersion` | independent | Engine-owned interpretation provenance for simulation. Fixed for one run; touches operational identity only when a history accepts records that a run under some semantics version produced. |
| Actor identity | many-to-many | Who acted. Actors change constantly within one history; one actor participates in many. Organisational transfer of an account changes actors and authority without changing the identity — the transfer is recorded within it. |
| External subject identity | many-to-many | The thing in the world. Decisively **not** the referent: several twins of one installation carry different identities and share only the subject. Equipment replacement changes the product aspect of a subject while the history continues unaffected. |
| Deployment identity | many-to-many | Many deployments may extend one history (blue/green, rolling, replicas, distributed processing); one deployment may host many histories (multi-tenant, several plants, parallel experiments). Neither direction is derivable from the other. |
| Command identity | one-to-many | A command is Arcogine's own conduct, so it belongs to exactly one history. Its acceptance, execution and outcome remain separate facts from the transition it requested. |
| Observation identity | independent at ingestion; many-to-many through interpretation | An observation is what Arcogine was *told*, not what it did. It carries no operational identity. One observation may be interpreted by many histories; one history interprets many observations. See §9. |
| Reconciliation / evidence-use relationship | one-to-many | A reconciliation is Arcogine's own conclusion and therefore belongs to exactly one history. Governance `EvidenceUse` may later reference it without altering its operational provenance. |

---

## 9. Raw observation boundary

**RECOMMENDATION** **Raw external observation ingestion does not require the identity, and must not be made to carry it.** ADR-0013's retained requirement 5 is confirmed, and the reason is now principled rather than pragmatic.

> The identity attaches to what Arcogine did or concluded — never to what Arcogine was told.

That single line partitions the future durable operational record types cleanly:

- **Carries the identity** — commands and their lifecycle, deployment records, subject-correspondence assertions, reconciliation results, drift and calibration proposals. Each is an act or a conclusion for which Arcogine is answerable.
- **Does not carry it** — raw external observations, which retain source identity, external subject identity, source/event time, receipt time, quality, trust and authenticity provenance, and raw-source reference, exactly as the architecture already requires.

### How an observation becomes associated with a history

**REPOSITORY FACT** Arcogine has already solved a structurally identical problem. `docs/architecture/governance-conformance.md` separates `Evidence` — "the source-level fact: what was observed, by what authority, when, and over what period it applies… It does not carry a model fingerprint or revision" — from `EvidenceUse`, "the binding: which evaluation consumed that evidence, against which fingerprint/revision, and why it was judged applicable to that scope at that time," noting that "One `Evidence` record may be referenced by many `EvidenceUse` records."

**RECOMMENDATION** Use the same shape. The association is made **at interpretation time by a separate relating record**, which itself carries the identity. The observation's original provenance is never rewritten, never annotated, never duplicated per history. Two twins of one plant consume the same observation feed and reach different conclusions without conflict, because each records its own interpretation in its own account.

Reusing an established Arcogine pattern rather than inventing a parallel one is also the cheaper answer against the evaluation criterion of fewest new shared concepts.

### The case that proves the boundary

**ARCHITECTURAL INFERENCE** An observation arrives late, describing a period *before* a fork, after the fork has already occurred. Under this boundary the answer is immediate and needs no special rule: the observation belongs to no history, and **both** the parent and the child may legitimately interpret it, each recording its own interpretation in its own account. A model that forced the identity onto the observation at ingestion would have had to choose, and would have been wrong either way.

---

## 10. Failure semantics

Semantic consequences only. No infrastructure is designed here, and none is required to state these.

### Split brain

Two continuations extend one account concurrently. This violates condition (ii) of §6 and is therefore a **well-formedness violation of the identity claim**, not a redefinition of identity. It is detectable retrospectively by comparing recorded ancestry — the DRBD mechanism — and the semantic obligations are: both branches are retained; the divergence and its point are recorded as a finding; entitlement to *extend* the identity from the decision onward is assigned to at most one branch; the other establishes a new identity going forward; and **already-written records keep the identity they were written under**. Nothing is relabelled and nothing is deleted.

> **State the limitation in the ADR.** A set of records bearing one identity is not guaranteed to be free of divergence. What is guaranteed is that divergence is representable and detectable. Anyone reading a history must be able to learn that a divergence finding exists against it.

### Ambiguous failover

Leadership is briefly unclear, or it is unknown whether the standby holds everything. The undetermined rule applies: continuity is not proven, so the safe resolution is a fork with lineage. If it is later established that nothing was lost, that is recorded as a finding about the fork — *not* as a retroactive merge of the two identities into one.

### Stale checkpoint restore

Records accepted after the checkpoint are lost, so this is a fork. The abandoned tail remains an addressable branch — PostgreSQL's "states in timeline branches that you abandoned earlier," Oracle's orphan incarnation. **RECOMMENDATION** Arcogine must not adopt DRBD-style victim discard: deleting records of what the system actually did is falsification, not repair. This is the one point where the two external traditions genuinely disagree, and Arcogine belongs with the recovery systems, not the replication systems.

### Duplicate claims of continuity

Two parties each assert entitlement to extend one identity. Because entitlement derives from *holding the accepted record set* and not from *holding the identifier*, the claim is adjudicable in principle by comparing what each carries forward. **Possession of the identifier confers nothing.** Where neither claim can be substantiated, the undetermined rule applies to both.

### Loss of durable state

If the accepted extent cannot be determined, continuity cannot be proven. A new identity is established with lineage to the lost one and an explicit record of the discontinuity. This is the case that most clearly shows the identity is about the *records*, not about the label.

### Recovery after partial history loss

Same rule, with an added obligation: the fork must record what is known to be missing. **ARCHITECTURAL INFERENCE** ADR-0011 §8's principle that "silent truncation is not acceptable recovery behavior" and that recovery must detect a gap rather than pretend replay is complete applies here with equal force. A history that quietly lost records and continued under the same identity is exactly the falsification this whole rule exists to prevent.

### Loss of correspondence, telemetry, control or trust

None of these touch identity. They are changes in relationships, recorded within the history — which is the same relational treatment the architecture already requires and the reason the withdrawn kind-bound model failed.

---

## 11. Candidate name

Assessed only after the referent was settled. The name follows the referent.

| Candidate | Assessment |
|---|---|
| `OperationalHistoryId` | **Best of the offered set.** "History" in the sense of *an account or record* rather than *the past* — the same sense as ADR-0008's "historical occurrence," so it reads consistently with the existing vocabulary. Its one weakness is that it can be misread as past-only, when the referent is a forward-continuing account. |
| `OperationalContinuityId` | Rejected. Names the *property* (continuity) rather than the *thing*. Continuity is what the rule decides *about* the identity, not what the identity is. |
| `OperationalPartitionId` | Rejected. "Partition" imports data-sharding and network-partition connotations and invites exactly the infrastructure derivation ADR-0013 §3 forbids. |
| `ExecutionId` | Rejected. One word from `RunId` and certain to be conflated with it. "Execution" also implies running, which a dormant or purely record-keeping account is not. |
| `RealizationId` | Rejected. Realisation is already a distinct architectural concept — the mechanism by which an operation produces a transition — and reusing the word would collide with §4 of the Operational architecture. |
| `OperationalLedgerId` / `…AccountId` | Considered because they carry the accountability sense best. Rejected on collision: `Ledger` is already a Finance type owned by `FinanceHandler`, and "account" reads as financial in a system that models a business. |

**RECOMMENDATION** Carry `OperationalHistoryId` as the working name, and **defer the final choice to the ADR revision** — which ADR-0013 already sequences correctly by settling the type name only after the equality rule is explicit. The name is now the smallest remaining question, and no name should be introduced into code before a durable operational record exists for it to label.

---

## 12. Ownership recommendation

**RECOMMENDATION** **Operational Execution owns the concept; no new module is created; the value type belongs in `:types` when it is finally implemented.**

**Not Governance.** Governance owns durable *semantic* identity, controlled revision history, requirements, conformance, evidence use and governed change. An operational history is Arcogine's record of its own *conduct*, not of model change. Placing it in Governance would conflate the two histories that this entire investigation exists to keep apart. Governance nevertheless supplies the *pattern* — opaque identity, authoritative acceptance, explicit immutable lineage, no reuse — and the ADR should cite ADR-0008 as precedent rather than reinventing it.

**Not a smaller shared boundary, yet.** There is currently exactly one consumer. Extracting a shared "accountable record continuation" abstraction across Governance and Operational would generalise from a single instance — the pattern the repository repeatedly warns against.

**Not a new module.** ADR-0013's consequences already forbid introducing an `:operational` module merely to materialise identity types, and the requirement has not changed. `RunId`, `ControlledRevisionId` and `ModelFingerprint` all live in `product/types/`; the operational identity belongs beside them when its time comes.

> **Sequencing matters more than ownership here.** Until a durable operational record capability actually exists — PLAN-OPS-3 commands, PLAN-OPS-4 deployment records, or PLAN-OPS-5 correspondence — the identity has nothing to label. Implementing it earlier would create an identifier with no records, no acceptance boundary, and no way to test the continuity rule. Resolve the ADR now; implement the type with its first real consumer.

---

## 13. ADR-0013 recommendation

> **Verdict: Revise substantially**
>
> The referent is resolved and the retained requirements survive. What the ADR currently says is not wrong — it is incomplete in exactly the place it identifies. Keep the hold's withdrawals, keep all six retained requirements, replace the "Unresolved decision" section with the rule in §6, and rewrite the surrounding text around the resolved referent. **Do not revive the kind-bound model in any form.** No compatibility migration is needed, because the original proposal was never Accepted.

### Semantic decisions the revised ADR must make

1. **Name the referent** as an accountable operational continuation, and state explicitly that it is not the installation, the model, the runtime, the deployment or the twin.
2. **State the two-condition continuity rule** — no accepted record lost, and exclusive extension — and enumerate the long list of things that do *not* affect it, since that list is what makes the rule usable.
3. **Define continuity by record loss, not by log prefix**, and say why: ADR-0011 §2 means the rule must not presuppose a total order.
4. **Make lineage mandatory on every fork**, with parent identity, divergence point, and whether the parent continues.
5. **Fix parent cardinality at 0..1** for the initial contract, reserving multi-parent as a later extension, exactly as ADR-0008 did.
6. **State that convergence is never identity equality.**
7. **Adopt one rule for stale restore and deliberate fork**, with the difference carried by annotation rather than by a second rule.
8. **Declare the exclusivity limitation openly** — claimable and detectable, not provable at write time — and require that divergence be recorded rather than repaired by relabelling.
9. **Fix the undetermined rule**: unproven continuity fails safe to fork.
10. **State the record-attachment boundary** — the identity attaches to conduct and conclusions, never to raw observations — and point at the `Evidence`/`EvidenceUse` shape for later association.
11. **Establish non-reuse and immutability**: a retired identity is never reissued; the record-to-identity binding never changes.
12. **State that holding the identifier confers no entitlement to extend**; entitlement follows from holding the accepted record set.
13. **Address Charter §7 explicitly**, so that its "(simulation, replay, staging, production)" parenthetical is not later read as mandating the withdrawn taxonomy, and note that the fork rule actively serves Charter §6's "hypothetical branches" requirement.
14. **Defer, on the record**: the final type name, representation and canonicalisation, issuance boundary, persistence and acceptance mechanics, divergence-point representation, and whether any registration authority is ever needed.

---

## 14. Downstream implications

### True dependencies

- **Operational readiness sequencing.** PLAN-OPS-1 is unblocked as an *architecture* item, but should not be implemented first. The identity has nothing to label until a durable operational record capability exists; the narrowest safe slice is to resolve the ADR now and introduce the type with its first real consumer.
- **External command lifecycle (PLAN-OPS-3).** Commands are conduct and therefore carry the identity. The command contract must reference it without deriving anything from it, and correlation to a `RunId` must stay separate.
- **Deployment records (PLAN-OPS-4).** Deployment identity stays independent in both directions — many deployments per history, many histories per deployment. The deployment record carries the identity as provenance; the identity is never inferred from the deployment.
- **External observation ingestion (PLAN-OPS-5).** Confirmed unaffected at ingestion. The relating record that binds an observation to a history is a distinct future contract, and the `Evidence`/`EvidenceUse` shape is the recommended precedent.
- **Subject correspondence (PLAN-OPS-5).** A correspondence assertion is Arcogine's own assertion and therefore carries the identity — which is what allows two twins of one plant to assert different correspondences without conflict.
- **Reconciliation (PLAN-OPS-6).** Reconciliation results are conclusions and carry the identity. This is what makes "two twins disagree" a comparison rather than a conflict.
- **Checkpoint/recovery (PLAN-OPS-8).** Most directly affected. Recovery semantics must implement the loss test and the undetermined rule, must not silently truncate, and must be able to record an abandoned branch.
- **Future persistence.** Requires monotonic acceptance (nothing accepted is later un-accepted), an immutable record-to-identity binding, durable lineage, and enough information to answer "was anything accepted lost?" after a failure. Storage technology remains unspecified.

### Related but not dependent

- **Actor, trust and authority (PLAN-OPS-2).** Independent by construction. Authority changes — including organisational transfer — are recorded within a history and never alter its identity. PLAN-OPS-2 need not wait on this ADR, and this ADR must not be used to place actor semantics in Operational.
- **Drift and calibration (PLAN-OPS-7).** Consumes histories; imposes no requirement on the identity beyond attribution.
- **Engine distribution hardening.** Genuinely separate. Retained runtime-event history, replay-by-cursor and reconnect are `RunId`-scoped concerns; conflating them with operational-history continuity is exactly the error the architecture warns against.
- **The agency and decision-boundary investigation.** Its H3 (actor attribution survives controller replacement) and H4 (replaying a decision differs from re-executing its source) are structurally the same shape as this result — durable attribution surviving mechanism change — and the two investigations should be read together, but neither blocks the other.

---

## 15. Remaining unknowns

**What exactly constitutes an "accepted" operational record?**
→ The continuity rule depends on it, and Arcogine has no durable operational record capability yet. ADR-0008's persistence-acceptance boundary is the obvious precedent, but it has not been tested against records that arrive late, out of order, or from several writers. **Resolved by:** the first durable operational record capability — PLAN-OPS-3, -4 or -5 — defining its acceptance boundary.

**How is the divergence point designated?**
→ It must reference a determinate state of the parent account. Whether that is a checkpoint reference, a temporal boundary or a designated record cannot be settled without a concrete checkpoint or recovery capability. **Resolved by:** PLAN-OPS-8, or the first fork-capable implementation.

**Can a fork ever be re-parented after the fact?**
→ Immutability says no and §6's recording-not-rewriting rule agrees, but this has not been tested against a real late-discovery scenario. **Resolved by:** a proving case in which a divergence is found long after it occurred and the recorded parent proves wrong.

**Does federated live operation need more than a coordination obligation?**
→ Several Arcogine instances jointly extending one history are permitted by the rule and owe each other non-loss, but whether the semantics need to say more than that is untested. **Resolved by:** an actual distributed operational deployment, which is well beyond current scope.

**Will multi-parent lineage eventually be required?**
→ Deferred here on ADR-0008's precedent. Whether a real Arcogine capability ever needs to record that a new account descends from two is unknown. **Resolved by:** a concrete case where a correlation between two accounts is demonstrably insufficient.

**Is a registration or uniqueness authority ever needed?**
→ ADR-0013's retained requirement 4 says no registry is needed to *state* the semantics, and nothing here contradicts that. Whether independent issuance suffices once several parties can claim continuity of one account is a genuinely open question. **Resolved by:** the first case with more than one candidate extender outside a single administrative boundary.

**Does an inspection or analysis session need its own identity?**
→ §5 concludes that inspection creates no operational identity, which raises but does not answer whether analyses need their own durable identity. Deliberately out of scope. **Resolved by:** a capability that must attribute analytical results durably — possibly Governance `EvidenceUse`, possibly ADR-0016's derived-analytical-result provenance.

---

## 16. Method and coverage

Repository grounding was done directly against the recorded SHA: `AGENTS.md`, `docs/README.md`, the Charter, the Architecture Overview, the Operational Execution and Digital Twin architecture, the Operational readiness plan, ADRs 0004, 0006, 0007, 0008, 0011, 0012, 0013, 0015 and 0016, the Governance and Conformance architecture, the ISA-95 semantic mapping, the standards alignment register, the agency decision-boundary investigation, and the landed identity types under `product/types/` and `product/governance/`. Keyword sweeps covered every term the brief listed.

> **Coverage limitation, stated plainly.** Four parallel research lanes — distributed systems/HA/DR, event sourcing and durable execution, digital twin and industrial standards, and an independent adversarial reviewer — were dispatched and all four terminated on a session-wide rate limit before returning results. The external research was therefore carried out directly, and the adversarial pass was performed in-line rather than by an independent reviewer.
>
> The practical consequence is **narrower external breadth than planned, at unchanged depth on the sources that decide the question**. Eight primary sources were fetched and verified in session; two entries in §3 are marked as background knowledge requiring verification before they are cited in an ADR.

Areas the brief listed that received thin or no coverage, and which a follow-up should close if the ADR needs them: ISA-88/IEC 61512 batch identity (searched; the accessible material did not reach the batch-identity semantics the question needed, so it is reported as inconclusive rather than assumed); ISO 23247 digital-twin framework; process historian point identity across system migration; regulated-records continuity under GAMP 5, EU GMP Annex 11 and 21 CFR Part 11; Camunda process-instance migration across definition versions; AWS Step Functions and Azure Durable Functions; virtual-commissioning record continuity; and bitemporal modelling literature. **ARCHITECTURAL INFERENCE** None of these is load-bearing for the recommended rule — the rule is decided by the recovery-lineage, declared-identity and multiple-shell evidence, all of which was verified — but several would strengthen or complicate §7 and §14, and the regulated-records question in particular could impose additional obligations on §10.

Since the adversarial critique was self-administered, its findings should be treated as a first pass rather than as independent review. The three amendments in §6 and the demotion of the membership heuristic are its results; an independent reviewer given §6 to attack would be a worthwhile next step before the ADR is rewritten.

---

*Read-only investigation against alaiba/arcogine at `2acee74723aad7a9ca384459f5d44ef35b85b6fd`. No source files, documentation, ADRs, planning status, issues, branches, commits or pull requests were modified. Claims are labelled REPOSITORY FACT, EXTERNAL EVIDENCE, ARCHITECTURAL INFERENCE or RECOMMENDATION; external sources marked "background" were not fetched during this investigation and must be verified before citation in an ADR.*
