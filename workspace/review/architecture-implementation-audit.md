# Architecture implementation audit

## Baseline and method

- Audit baseline: live `alaiba/arcogine` `main`, `69cb3e7650b9f0cc6fc9f1cb1d161da001f7d7e3`, resolved on 2026-10-14 (Europe/Bucharest).
- #487 and #490 were both OPEN and unmerged at grounding. Their bodies were read for context; neither branch is implementation evidence. `docs/planning/` is present at the baseline but is not audit authority.
- Custody: `workspace/architecture-implementation-audit`; this report is transient and must never merge to `main`. Only this report is written. Product, tests and maintained documentation are unchanged; no GitHub issues, comments or pull requests are created.
- All seventeen architecture documents and `docs/product/concepts.md` are in scope. Evidence refers to the baseline, not a search index or the mutable branch tip.
- Six read-only cluster audits use the handoff's six classes. The lead reconciles duplicate claims, checks every unbuilt/unreachable gap, and independently samples implemented/test pairs. Tests are inspected for assertions rather than mere execution. No build is run for this report-only change; test evidence means inspected assertions, not a new passing run or mutation-test proof.
- Reachability is from supported Factory runtime/model/publication, Governance, Storage and Challenge APIs. Test callers and research-experiment callers do not make a product capability reachable. Research-experiments is intentionally non-shipped and excluded from unreachable-code findings.
- A row is one independently classifiable rule or closely related rule family; compound rules with different delivery states are split. Counts count per-document inventory rows, not sentences or unique semantic requirements. Cross-document synopses point back to owning evidence; their appearances are included in document coverage counts, while gaps and unreachable capabilities are deduplicated. Examples and reference lists are not extra requirements.
- Classes: **Implemented + pinned**, **Implemented, unpinned**, **Not implemented**, **Unreachable**, **Principle**, **Scope**. A Principle is an ongoing constraint, with its enforcement stated; Scope is a confirmed absence/non-ownership boundary. An explicitly future capability with specified positive behavior is still inventoried as Not implemented, but its disclosed status is preserved: that is an architecture-location consequence under the requested principle, not a claim that the document falsely reports implementation.

## Summary

| Document | Implemented + pinned | Implemented, unpinned | Not implemented | Unreachable | Principle | Scope | Total |
|---|---:|---:|---:|---:|---:|---:|---:|
| engine-semantics.md | 23 | 3 | 10 | 2 | 5 | 6 | 49 |
| runtime-contract.md | 13 | 0 | 0 | 0 | 12 | 7 | 32 |
| transfer-applicability.md | 3 | 0 | 3 | 0 | 2 | 1 | 9 |
| factory-design.md | 13 | 0 | 9 | 0 | 7 | 4 | 33 |
| factory-model.md | 18 | 4 | 0 | 0 | 3 | 3 | 28 |
| factory-resource-semantics.md | 3 | 0 | 0 | 0 | 5 | 1 | 9 |
| challenge.md | 16 | 0 | 1 | 0 | 1 | 3 | 21 |
| governance-conformance.md | 9 | 1 | 8 | 0 | 5 | 4 | 27 |
| governance-evidence.md | 16 | 0 | 1 | 0 | 7 | 3 | 27 |
| controlled-revisions.md | 10 | 0 | 0 | 0 | 2 | 4 | 16 |
| storage.md | 12 | 0 | 0 | 0 | 1 | 3 | 16 |
| operational-execution-digital-twin.md | 2 | 0 | 13 | 0 | 6 | 8 | 29 |
| operational-continuity.md | 0 | 0 | 10 | 0 | 0 | 6 | 16 |
| standards-alignment.md | 5 | 0 | 1 | 0 | 7 | 5 | 18 |
| isa-95-semantic-mapping.md | 5 | 0 | 1 | 1 | 5 | 5 | 17 |
| external-representations.md | 4 | 0 | 0 | 0 | 7 | 5 | 16 |
| overview.md | 13 | 0 | 1 | 1 | 13 | 3 | 31 |
| docs/product/concepts.md | 7 | 0 | 0 | 1 | 1 | 1 | 10 |
| **Per-document total** | **172** | **8** | **58** | **5** | **89** | **72** | **404** |

The delivered nonspatial runtime, canonical model/publication, headless Challenge, revision storage and bounded Governance evidence APIs have substantial specific assertion evidence. Architecture also retains substantial designed-but-unbuilt positive behavior, especially spatial runtime, Operational continuity and Governance decision/audit composition. Negative scopes and explicit no-platform admission principles remain valid. The deduplicated report contains **32 gap families: 21 Not implemented, 3 Unreachable, 6 Implemented-unpinned, 2 review-only enforcement opportunities**. These are family counts, not the 404 per-document coverage groups; the three dormant capabilities are timing, reservations and Finance.

## Gaps

Findings are grouped in the requested order. Proposed outcomes require the owner's decision; none is applied by this audit.

### Not implemented

#### Gap 1 — Spatial runtime execution

`engine-semantics.md:360–671`, `transfer-applicability.md:20–25`, `factory-design.md:114,178–180`, `isa-95-semantic-mapping.md:130,203,206,369`: destination admission/binding, immutable in-flight phase, scheduled arrival, zero-time separate turn, bound offline waiting, transfer events and coherent transfer/admission observations are specified but never executed. The explicit status at engine-semantics:351–356 is truthful about this absence.

Evidence: assembler :36 refuses every present spatial record before creating stores; H:330 advances directly to the next step; RT:446 processes TaskEnd only. Full shipped production searches for TRANSFER/Transferr/TransferTiming/reservation mutators found no flight/arrival orchestration. EX:69,85 pins refusal/absence; timing/reservation tests prove isolated helpers, not runtime behavior. Engine fixture 7–11/13 fulfillment is absent.

**Proposed outcome: move to its research record**, `docs/research/investigations/transfer-semantics.md`, register :36 (transfer lifecycle independence), preserving current absent/no-transfer and present/refusal rules in architecture. Reason: the positive design is decided but unbuilt. **Consequences:** the unexecuted part of CONCLUDED row :36 would return to READY or be split from the delivered discriminator. Composition row :35 remains CONCLUDED. Issue #488 overlaps directly. Factory model/design, runtime contract, transfer applicability and ISA mapping cite the behavior. Relocation changes no canonical bytes/golden vectors; future execution changes admitted runtime states/events and needs all spatial conformance fixtures, but need not change authored spatial encoding.

#### Gap 2 — Revision provenance in observation shape

`engine-semantics.md:627`, `runtime-contract.md:145–165,212`: observation metadata describes optional authoritative controlled-revision provenance. F/process/RuntimeObservationMetadata.java:8 has **no revision field**; RT:694 constructs only run/fingerprint/time/state/cursor. Events have an optional revision field but RT:529 always supplies empty. There is no supported authoritatively bound runtime constructor. Checked OB:40 and EV:88: current run/content provenance is pinned; positive binding is not.

**Proposed outcome: reword as a scope statement** stating the currently unbound runtime and absence of the observation field, retaining authoritative-binding obligation for a future admitted integration. Reason: optional representability and positive binding have not landed. This is not a violation of the “only when authoritatively supplied” condition: no bound runtime exists. **Consequences:** #488 prerequisite 1 and #482 candidate 3 overlap; no research CONCLUDED row needs reversal solely for this field. Future integration needs Governance authority validation, construction and observation/event symmetry tests. No Factory canonical/golden change for rewording.

#### Gap 3 — Framework/control mappings

`governance-conformance.md:53–87,279–301`: controls/framework requirements project onto generic requirements/assertions/findings and allow multi-framework reuse. Full Governance/Storage main declaration/caller inventory has no control/framework/mapping implementation. Current Requirement/source/version and conformance APIs are delivered (GC18/21), not a framework mapper.

**Proposed outcome: move to its research record** as a bounded question in Governance research; existing `governance-evidence-identity-applicability.md` is evidence context, not a concluded mapper design. At baseline no dedicated register row owns this complete mapping question; record that absence rather than inventing one. **Consequences:** conformance §3/§8/§12 and standards/Governance interchange references need truthful future scope. No known CONCLUDED row reverts for mappings alone; no Factory vectors change. No matching open CONS issue was found.

#### Gap 4 — Higher semantic-impact consequences

`governance-conformance.md:183–187,383–389`: affected controls/reviewers, required evidence, evidence invalidation and required authorization/review go beyond changed entities. G/change/ImpactScope.java:25 stores changed entities; inspected main ChangeSet/evaluator callers contain no richer derivation. Existing ChangeSet and intersection tests pin the delivered subset, not these consequences.

**Proposed outcome: move to its research record**, Governance bounded-change research framing; Factory evolution register :45 covers Factory participation and is already CANDIDATE. **Consequences:** pre-change diagrams/checklists and Factory bridge text cite these consequences; no known whole-impact CONCLUDED row to revert. Evidence row :30 stays delivered except Gap 8. Relocation has no canonical/vector effect; future implementation needs named semantic rules and fixtures. No open CONS overlap identified.

#### Gap 5 — Change-policy authorization and external/deployment composition

`governance-conformance.md:214–228,383–389`, `factory-design.md:217–237,454,456,503`: assessment→authorization→apply, policy alternatives and authorize/reject/revise are unbuilt. Full production searches for AuthorizationDecision/ApprovalRecord/DeploymentRecord find no implementation. GT/GovernanceModuleBoundaryTest.java:63 expressly pins current absence. ExternalChangeReference/ChangeProvenance association and stored unauthorized revisions exist (GC16/FD19); they are not a workflow or authorization controller.

**Proposed outcome: move to its research record**, Governance question framing plus `operational-execution-digital-twin-boundaries.md` for applying an authorized state; register :33 is CANDIDATE and Factory :45 CANDIDATE. No dedicated concluded authorization register row exists at baseline. **Consequences:** Governance §6/§11, Factory revision/deployment bridge and Operational §7 must distinguish delivered prerequisites. No canonical/vector change for relocation. No open CONS overlap identified; #490's unmerged Governance research brief is context only and cannot supply baseline implementation.

#### Gap 6 — Finding severity/remediation and exception/risk-acceptance state

`governance-conformance.md:266,268,345–364`: severity/remediation, rationale/owner/approver/effective period/expiry/compensating controls, without rewriting FAIL. G/conformance/Finding.java:30 has immutable affected objects/explanation only. No exception/risk record/lifecycle; module-boundary test :63 guards Severity/RiskAcceptance absence. CE:204 pins narrow FAIL/finding, not lifecycle.

**Proposed outcome: move to its research record** as distinct Governance bounded questions; existing evidence investigation doesn't settle authorization/expiry. **Consequences:** §10/§11/§12, Operational authorization and Governance interchange prose depend on it. No corresponding CONCLUDED register destination identified; no Factory golden change. No open CONS overlap identified. Keeping delivered findings does not justify retaining the unbuilt decision lifecycle as current capability.

#### Gap 7 — Audit snapshots and composed historical explanation

`governance-conformance.md:148–161,396–417`: why/approval/deployment/effective representation and reproducible AuditSnapshot from mappings, accepted occurrences and decisions with gap disclosure. Full Governance/Storage main/test search finds no audit projection/API/reconstruction suite. Revision lookup and in-memory evaluation metadata exist; BS:839 and GE:948 pin their narrower integrity/basis, not the composed audit claim.

**Proposed outcome: move to its research record**, Governance evidence investigation and historical-use framing in `storage-historical-support.md` (register :17 CANDIDATE), until a concrete audit consumer is admitted. **Consequences:** evidence register :30 records §12 as a reconciled destination; separate delivered reference/use/occurrence semantics from unbuilt audit/retained explanation, returning only any implementation-bearing unbuilt portion to READY. Governance §9/§12 and external Governance formats cite the interpretation. No existing Factory vectors change; durable future definition/evaluation representation requires its own compatibility evidence. No open CONS overlap identified.

#### Gap 8 — Fixed executable definition basis is only shallowly captured

`governance-evidence.md:212–217,231–239`: accepted basis must preserve exact executable rule and not reuse an existing definition binding for materially changed meaning. EvaluationOccurrence.java:36–40 stores the original Assertion; Assertion.java:32 holds an arbitrary AssertionRule functional object; AssertionRule.java:16 imposes no immutable representation. A rule can close over mutable state. InMemoryEvaluationOccurrenceAuthority.java:67 checks occurrence/revision/evidence relationships but has no cross-occurrence definition-binding authority. RequirementCatalogue duplicate checks are catalogue-local. GE:440 asserts old wording/description; GE:948 asserts the same Assertion object. Neither catches rule-state mutation or semantic ID/version reuse across accepted occurrences.

**Proposed outcome: implement through a research handoff** for the bounded immutable executable-definition basis, explicitly selecting whether caller purity is a supported obligation or an authoritative representation is required. Reason: another metadata assertion alone cannot enforce arbitrary function semantics. **Consequences:** evidence identity/applicability register :30 needs a partial READY reopening under the requested principle; preserve implemented immutable metadata/results/reference/use semantics. Conformance §7/§9/§12 and evidence §8/§9/§12 cite this basis. No Factory canonical/vector change; any future rule identity/representation has its own vectors. No open CONS overlap identified. **Limit:** source inspection establishes the arbitrary-reference exposure, not an executed mutation proof; generic Java closures cannot be frozen without defining a narrower contract.

#### Gap 9 — Accountable Operational continuity

`operational-continuity.md:23–201` defines genesis/fork acceptance, accumulating identity, loss/compaction, monotonic accepted set, divergence evidence, exclusive extension/forward remediation, immutable bindings and conduct-only attachment. :3/:229 expressly says no type/module/persistence exists. Lead and cluster searches across all shipped declarations/tests found no establishing record/account/frontier/continuation authority. Governance EvidenceUse point targets are explicitly insufficient (GE:497).

**Proposed outcome: move to its research record**, `operational-execution-digital-twin-boundaries.md` durable identity subsection, register :32 **CONCLUDED→READY**. Reason: the adopted positive identity contract has no durable-record consumer to implement/test it. **Consequences:** Operational §2.2/:354/:435/:473, evidence accumulating-identity limit, register actor separation :31, and synthesis-seeds.md:26,29,31 depend on it. Preserve the decision, not a speculative ID type. No Operational canonical format/golden vectors exist; none to migrate. No open CONS overlap identified.

#### Gap 10 — External observation/correspondence/reconciliation/time

`operational-execution-digital-twin.md:124–145,275–326,384,441`: independently sourced durable observations, authoritative correspondence, conflict/unknown/effective intervals and historically attributable reconciliation across distinct time dimensions. All are absent by full module/source/test search. G/evidence/EvidenceProvenance.java:89 and GE:384 prove supplied Governance source values only; they do not ingest, authenticate or reconcile reality.

**Proposed outcome: move to its research record**, Operational boundary investigation, register :33 remains CANDIDATE. **Consequences:** Governance producer integration, ISA interoperability and manufacturing traceability questions cite these boundaries; row :30 doesn't reopen just because this producer is absent. No Operational vectors/schema exist. No open CONS overlap identified.

#### Gap 11 — External operation realization, command lifecycle, adapters and trust/safety

`operational-execution-digital-twin.md:14,61,151–237,412,449–460`: hybrid semantic realization, separate request/accept/result/physical transition/observation, correlation, retry/compensation, authenticity/credentials/authority/fail-safe/ambiguity. Full shipped declaration/caller searches find no adapter/external command/trust or consequential execution. :460 explicitly disclaims current safety controls; this is disclosed design, not falsely reported deployed capability.

**Proposed outcome: move to its research record**, Operational boundary research; register :33 CANDIDATE, generic actor :31 remains separate CANDIDATE. **Consequences:** standards adapter prerequisite text remains Principle; no protocol becomes canonical and no generic actor implementation is admitted. No existing command schema/golden vectors. No open CONS overlap identified.

#### Gap 12 — Deployment and reality-driven calibration

`operational-execution-digital-twin.md:243–267,360–378`: authorized application/transform/effective artifact/verification/promotion/rollback and governed observed-feedback loop. No deployment/drift/calibration production path. Existing immutable publication, controlled revisions and ChangeSet/conformance APIs are prerequisites only; current absence guard GT/GovernanceModuleBoundaryTest.java:63 confirms no DeploymentRecord.

**Proposed outcome: move to its research record**, Operational boundary research; register :33 remains CANDIDATE. **Consequences:** deduplicate implementation responsibility with Gap 5; Factory/Governance bridge/current identity statements remain principles/prerequisites. No current vectors affected. No open CONS overlap identified.

#### Gap 13 — Operational history/seek/replay/checkpoint/fork/retention

`operational-execution-digital-twin.md:332–354,433–441`: distinct recover/replay/restore/fork and durable conduct/observation/history retention. No such Operational APIs or tests; Engine drain RT:547 does not implement retained history/checkpoint. Runtime-contract:377 correctly defers distribution hardening and is therefore Scope, not an additional false current-implementation claim.

**Proposed outcome: move to its research record**, Operational boundary research, register :33 CANDIDATE; continuation implications handled once through Gap 9/:32 READY. **Consequences:** Operational §10/§15 and continuity loss rules, not the current Engine command contract. No schema/vectors exist. No open CONS overlap identified.

#### Gap 14 — Shared Factory draft/workspace lifecycle

`factory-design.md:31,123–125,259`: mutable invalid drafts, persistence/branching/merge/collaboration. Factory source has immutable models/publication and no FactoryDraft/shared service. Challenge's immutable CandidateDraftSnapshot is consumer-owned, not this capability.

**Proposed outcome: move to its research record**, `factory-design-evolution.md` shared-draft subsection, register :45 remains CANDIDATE. **Consequences:** design diagram/lifecycle/ownership summaries need truthful current scope; delivered model/publication unaffected, no canonical/golden change. No CONCLUDED row or open CONS overlap identified.

#### Gap 15 — Richer Factory facts, hierarchy and shared verification objectives

`factory-design.md:153–157,178–180,268–290,298,325–328`: pools/policies/arbitrary constraints/zones/orientation/connections/reachability/hierarchy and shared throughput/lead-time/utilization/safety objectives. Current closed production/spatial records have none, and no objective definition/evaluator exists. Runtime throughput/lead time values do not implement a reusable objective framework.

**Proposed outcome: move to its research record**, Factory evolution register :45, characteristics :42 READY, qualification/performance :43–44 CANDIDATE, spatial/material-flow :60 CANDIDATE, with consumer-specific verification admission owned by its actual question. **Consequences:** Factory resource and ISA scope should preserve current collapsed explicit eligibility. Composition :35 and resource decision :40 remain CONCLUDED. Relocation changes no vectors; implementing additional authored fields changes canonical grammar/vectors, whereas composing existing transfer fields need not. No open CONS overlap beyond #477's publication wording.

#### Gap 16 — Future scenario composition

`factory-design.md:94`: scenario contains/references model plus workload/settings. No Scenario input schema/loader exists. Explicit absence is correctly repeated in concepts/external representations; Challenge's game content loader is not a generic Factory scenario loader.

**Proposed outcome: reword as a scope statement** that scenario inputs are unselected until a concrete need. Reason: no existing research decision or implementation justifies maintaining a positive format contract. **Consequences:** no CONCLUDED row, canonical/vector or open CONS impact; preserve explicit workload runtime API.

#### Gap 17 — Publication validity wording overstates executability

`factory-design.md:38` says validation reflects what runtime can actually execute. Published valid spatial models are deliberately refused (EX:69). Later §4/§11 and Factory model §2.3 correctly split the authorities.

**Proposed outcome: reword as a scope statement** separating canonical publishability from Engine admission. **Consequences:** no behavioral/vector changes or composition :35 reopening; align transfer applicability and runtime refusal. No open CONS overlap identified.

#### Gap 18 — Explicit validation severity

`factory-design.md:464`: field-addressed diagnostics with explicit severity. ModelValidationError.java:4 stores field/message only; current validation uses rejecting errors. Inspected validator/publication tests pin deterministic errors, not severity.

**Proposed outcome: reword as a scope statement** stating all current publication violations are errors and no severity dimension exists. **Consequences:** no canonical/golden or CONCLUDED register change; future diagnostic compatibility needs concrete use. No open CONS overlap identified.

#### Gap 19 — Attempt admission provenance

`challenge.md:85` calls the retained snapshot admitted. ChallengeAttempt.java:22,45 accepts raw candidate/economics/evaluation with nonnull checks only; no assessment/proof/exact challenge/catalogue binding. Existing attempt tests pin immutable retention, not admission.

**Proposed outcome: reword as a scope statement** that the consumer supplies admission-consistent values. Reason: the record itself cannot establish the stronger claim. **Consequences:** no Factory vectors or CONCLUDED research destination; retain delivered attempt/comparison capability. A mechanically guaranteed admission record would need a focused consumer handoff and binding tests. No open CONS overlap identified.

#### Gap 20 — Resource busy-time claim

`standards-alignment.md:157` says observations expose resource busy time. ResourceObservation.java:9/RuntimePerformanceObservation.java:4 and RT:643 contain no such field/accumulator. Product busy/utilization/metric searches and OB/HC/runtime record tests confirm absence; Engine §10 expressly excludes it.

**Proposed outcome: delete from architecture** the busy-time claim. **Consequences:** reuse CONS #476; analytics ownership register :56 stays CONCLUDED (it admits no new measurement), evidence/provenance :57 remains CANDIDATE. No canonical/golden change. The same sentence's “ownership remains open” is stale status, separately noted below.

#### Gap 21 — Scheduled-event vocabulary overstatement

`overview.md:291` presents order creation/task start/order completion/availability as scheduler-processed transitions. RecordingScheduler only admits TaskEnd; Factory commands apply order/availability directly; Handler ignores TaskStart/OrderCompleted; HC:396 pins actual scheduled events. Overview:501/554 already describes this accurately.

**Proposed outcome: reword as a scope statement** distinguishing internal vocabulary from Factory-session scheduling. **Consequences:** reuse CONS #479; no research reopening or vectors/behavior changes. Do not infer missing scheduled markers need implementation: authoritative-only scheduling is the supported contract.

### Unreachable

#### Gap 22 — TransferTiming

Engine §7's arithmetic has a tested helper but no production caller (EN29). **Proposed outcome: implement through a research handoff** only if spatial execution is admitted; otherwise the owner chooses removal versus retention using the packet below. Reason: arithmetic alone is not a reachable phase. Consequences/research impact are Gap 1, register :36, issue #488; no existing canonical change merely to compose it.

#### Gap 23 — Inbound admission reservations

Engine §6/§9 capacity bookkeeping is implemented/tested but cannot become nonempty from supported runtime (EN33). **Proposed outcome: implement through a research handoff** only with transfer binding/arrival/observation closure, subject to owner choice. Consequences are Gap 1/#488; adding tests of the same direct methods will not establish reachability. Removal/retention facts below.

#### Gap 24 — Finance

Overview:318,335,348,480,484,554; concepts:32; ISA mapping:124,129 describe Finance interpretation/ledger ownership. FinanceHandler.java:25 explicitly says no delivery; RT emits supported ORDER_COMPLETED but no internal OrderCompleted payload or Finance handler chain. Full production caller search finds no supported Finance composition. FinanceHandlerTest.java:123 pins duplicate delivery posting twice.

**Proposed outcome: implement through a research handoff** if the owner admits a real financial consumer; a composition must establish identity/delivery/replay/idempotency, not presume exactly-once from destructive drain. **Consequences:** no Finance CONCLUDED register row identified; no model canonical/golden change simply from composition; Finance ownership/ArchUnit/module references need reconciliation if removed. Owner decision remains open, with costs below. CONS #479 overlaps vocabulary, not Finance delivery itself.

### Implemented, unpinned

#### Gap 25 — Floating completion-value accumulation

Engine:586–590, H:348 uses completion-order `+=`; ordinary exactly representable price tests don't distinguish reassociation or infinity. Checked DR, Handler, Boundary, dispatch and proportional-work tests. **Proposed outcome: add a test** with order-sensitive values, exact result bits and overflow-to-infinity. **Consequences:** closes §14 fixture 16 value case; no expected semantics/vectors/register change; no CONS overlap.

#### Gap 26 — Integral-to-floating boundary

Engine:591–594, H:145,152 uses Java conversion; no >2^53 nearest-even vector in derived/type tests. **Proposed outcome: add a test** for above-mantissa rounding. **Consequences:** no implementation/canonical/register change; no CONS overlap.

#### Gap 27 — Ordinal-discriminating local FIFO fixture

Engine:706–709 requires arrival order that differs from ordinal ordering. D:143 differentiates JobId/duration but all quantities are one; D:88 likewise. Intra-order test:42 checks ordinals/completion totals, not arrival versus ordinal conflict. FIFO implementation is otherwise strongly pinned. **Proposed outcome: add a test** with contradicting arrival/ordinal order. **Consequences:** closes fixture 4, no semantic/vector change; retained admission/ranking register :62–63 remain CONCLUDED; no CONS overlap.

#### Gap 28 — Capacity/setup forwarding and independence

Factory model:78–85, assembler :49,50 and RT:653,654 forward metadata; selector/duration/completion don't read it. All Factory test searches for capacityLiters/setupTime find only comparator detail, no forwarding/observation assertions or differential outcomes. Assembler test :17 asserts concurrency, OB uses null/0. **Proposed outcome: add a test** with nontrivial fields and models differing only in metadata, asserting projected values and equal execution facts. **Consequences:** three IU inventory rows, one test responsibility; characteristics/setup register :42/:41 already READY; no canonical change (fields already canonical), no CONS overlap.

#### Gap 29 — Explicit decoder invalid-predicate fixture coverage

Factory model:392–395 demands decode cases for every listed invalid predicate. ART:147 mutates overlap and zero floor only. Shared validation executes remaining predicates, and publication tests cover them, but no decoder mutation test would catch a future decoder selectively weakening the remaining listed checks. **Proposed outcome: add a test** for out-of-floor/zero footprint/negative position or handling/transfer overflow (including zero-multiplication case)/production-predicate bytes. **Consequences:** test-evidence obligation, not absent rejection implementation; no grammar/golden/register change or CONS overlap.

#### Gap 30 — Divergent sibling revision acceptance

Governance conformance:107 promises divergent children. Storage :387 checks each parent but imposes no one-child constraint, so behavior is implemented. BS:727 accepts one child only; all Governance/Storage tests searched for sibling/divergence cases. **Proposed outcome: add a test** accepting two children of one parent and resolving both unchanged. **Consequences:** no current canonical/UUID/vector/research change; no CONS overlap.

### Review-only principles with feasible bounded mechanical enforcement

#### Gap 31 — Single-owner concurrent mutation boundary

Overview:627 prohibits concurrent simulation mutation. Current runtime owns its stores/scheduler but has no owner-thread/concurrency protocol; A's rules cover dependencies/mutator ownership, not concurrent invocation. Sequential repeatability tests cannot prove arbitrary caller concurrency safe. **Proposed outcome: add a test** after stating the supported single-owner usage contract, or a narrow architecture guard against introducing internal concurrent mutation. **Consequences:** no current vectors/research changes; a structural ban alone is not a thread-safety guarantee. No CONS overlap.

#### Gap 32 — Rendering/wall-clock dependence of simulation

Standards:139 and overview determinism prohibit rendering/wall-clock pacing affecting simulation. Scheduler uses explicit SimTime, but no architecture/API-use rule guards the simulation/domain decision path against future ambient clock/render dependencies. **Proposed outcome: add a test** for the bounded dependency/API prohibition while allowing recording-time metadata in Governance/Storage. **Consequences:** no vectors/register change; avoid falsely forbidding legitimate acceptance timestamps. No CONS overlap.

### Additional documentation corrections and excluded false positives

- CONS #476 also covers standards:157's stale “analytics ownership open” statement. Register :56 is CONCLUDED; correct the status to the existing ownership rule, with no reopening.
- CONS #477 covers ISA mapping:130/400 calling spatial publication proposed. Publication/canonical content is IP; correct that wording while preserving runtime refusal.
- CONS #481 concerns copied preferred JDK, an authority-placement issue, not unimplemented Java21 support. #478/#480 concern removed reference/provider-specific process authority outside this audit's architecture/product scope.
- Runtime evidence :248 explicitly introduces no account/verifier/seal/history owner. Coverage/finality/custody principles are reviewed caller obligations, not missing platform APIs. Register :55/:56 need no blanket reopening.
- Assertion supplied applicability/judgment is a bounded composition seam, not automatic truth/freshness/trust/conflict verification. Preserve Principles and require any real producer/policy to justify its claims; no generic solver is admitted.
- Factory model:269–271 says header layout **can** permit early rejection. The decoder currently validates after decoding. This is a grammar rationale, not a promised early-allocation rejection; the agent's initial NI candidate was rejected by lead review.
- Future representation candidates are Scope/Principle, not unreachable code. Shared actor/Agent/promotion/analytics types are explicitly not admitted; their absence is not a missing implementation.

## Unreachable code

### TransferTiming packet

F/process/TransferTiming.java is **82 physical production lines**; FT/process/TransferTimingTest.java is **377 test lines**. It implements exact Manhattan reference-cell distance, one handling overhead, zero magnitudes, same-resource/missing-layout/null refusal and exact-overflow guards. Direct tests include boundary vectors and small-floor exhaustive pairs. Repository-wide production Java searches find no caller; assembler refuses its required spatial input.

To become reachable it needs admitted spatial runtime construction, source/destination binding, fixed duration storage and scheduled arrival lifecycle, plus outward facts. Dependencies are spatial model/MachineId/Java exact arithmetic; Engine §7 and transfer research name its semantics. No separate transfer golden file exists; arithmetic vectors live in the suite. Removing it and its suite leaves supported runtime behavior unchanged, but architecture/research references need reconciliation. Keeping costs 82 isolated production and 377 test lines, with lifecycle integration still unproven. No blanket recommendation.

### Machine reservation packet

Machine itself is reachable; **only the reservation capability** is unreachable. Approximately **101 feature-attributed physical production lines** include field/comment/initialization, :64–153 feature block and admission-load/start diagnostic integration; standalone MachineInboundReservationTest.java is **448 lines**. Exact removal count depends on simplifying the shared admission code.

It implements bounded ordered reservations, no active/queued/Busy accounting, offline survival, conversion to active or offline FIFO, and guards. No production calls to reserveInboundAdmission/startReservedJob/enqueueReservedJob exist, so current supported reservation sets stay empty. Reachability needs binding/arrival and supported admission-load projection, not more direct machine tests. MachineView excludes mutators, ArchUnit guards domain ownership, and canAcceptJob already consults reserved load. Removing simplifies central admission and removes its isolated suite without changing supported outcomes; keeping adds dormant states/guards and maintenance cost to a central class. No canonical Factory vectors change merely for removal; source/document/research/ArchUnit reservation references need coordinated cleanup. Owner decides.

### Finance packet

Entire Finance Java surface: **244 physical main lines and 354 test lines**; FinanceHandler alone **76 main/158 test lines**. It supplies immediate DR Cash/CR Sales, exact BigDecimal quantization (two-decimal HALF_UP), balanced immutable JournalEntry, balances and LedgerView. Tests assert invalid/unbalanced refusal, quantization, separate entries and duplicate delivery posting twice.

No supported Factory/Governance/Storage/Challenge entry point calls Finance; only tests construct the handler. Runtime emits supported order completion, never the internal payload Finance consumes. Reachability needs an admitted financial consumer/composition with exactly specified fact delivery and redelivery semantics. Existing dependencies are types/simulation, Finance module/build/classpath wiring, overview/concepts/ISA financial ownership prose and A:52/72 ledger/Finance guardrails. There is no Finance canonical artifact or external golden format.

Removal deletes small tested ownership/monetary evidence, requires module/dependency/ArchUnit/doc cleanup and loses the executable financial boundary example, but changes no supported runtime behavior. Retention keeps 244 isolated main and 354 test lines and a clear ownership example, while not delivering financial interpretation through supported runtime. Connecting it without a use merely to make it callable would not settle its delivery/consumer contract. Decision remains with the owner.

## Implemented + pinned and remaining statement inventory

### Evidence notation

`IP` = Implemented + pinned; `IU` = Implemented, unpinned; `NI` = Not implemented; `U` = Unreachable; `P` = Principle; `S` = Scope. Each inventory row has exactly one class. Document coordinates use the named document under `docs/architecture/`, unless explicitly under `docs/product/`. Evidence aliases are concatenated with their suffix; every suffix and line identifies baseline content.

| Alias | Exact repository path prefix or file |
|---|---|
| F | product/domains/factory/src/main/java/com/arcogine/factory/ |
| FT | product/domains/factory/src/test/java/com/arcogine/factory/ |
| G | product/governance/src/main/java/com/arcogine/governance/ |
| GT | product/governance/src/test/java/com/arcogine/governance/ |
| ST | product/storage/src/test/java/com/arcogine/ |
| SC | product/storage/src/main/java/com/arcogine/storage/ |
| C | product/consumer/challenge/src/main/java/com/arcogine/challenge/ |
| CT | product/consumer/challenge/src/test/java/com/arcogine/challenge/ |
| A | product/architecture-conformance-test/src/test/java/com/arcogine/architecture/ArchitectureTest.java |
| BS | product/storage/src/test/java/com/arcogine/storage/BuiltInStorageTest.java |
| GE | product/governance/src/test/java/com/arcogine/governance/evidence/GovernanceEvidenceTest.java |
| CE | product/governance/src/test/java/com/arcogine/governance/conformance/ConformanceEvaluatorTest.java |
| CR | product/governance/src/test/java/com/arcogine/governance/ControlledRevisionTest.java |
| RT | product/domains/factory/src/main/java/com/arcogine/factory/process/FactoryRuntime.java |
| H | product/domains/factory/src/main/java/com/arcogine/factory/process/FactoryHandler.java |
| D | product/domains/factory/src/test/java/com/arcogine/factory/process/EngineDispatchConformanceTest.java |
| DR | product/domains/factory/src/test/java/com/arcogine/factory/process/EngineDerivedResultConformanceTest.java |
| SE | product/domains/factory/src/test/java/com/arcogine/factory/process/SessionControlAcceptanceTest.java |
| BA | product/domains/factory/src/test/java/com/arcogine/factory/process/BoundedAdvancementConformanceTest.java |
| EV | product/domains/factory/src/test/java/com/arcogine/factory/process/RuntimeEventDeliveryAcceptanceTest.java |
| OB | product/domains/factory/src/test/java/com/arcogine/factory/process/RuntimeObservationAcceptanceTest.java |
| EX | product/domains/factory/src/test/java/com/arcogine/factory/process/FactoryRuntimeExecutabilityAcceptanceTest.java |
| HC | product/domains/factory/src/test/java/com/arcogine/factory/process/HeadlessClosureAcceptanceTest.java |
| CAN | product/domains/factory/src/test/java/com/arcogine/factory/model/FactoryModelCanonicalFormTest.java |
| ART | product/domains/factory/src/test/java/com/arcogine/factory/model/FactoryModelArtifactTest.java |
| SP | product/domains/factory/src/test/java/com/arcogine/factory/model/validation/SpatialRecordValidationTest.java |

### engine-semantics.md

For `spatial-absent` evidence below, the lead checked F/model/FactoryRuntimeAssembler.java:36 (refusal before stores), H:330 (direct next-step dispatch), RT:446 (TaskEnd processing), runtime taxonomy/observations, and all production Java callers of transfer and reservation symbols. No executed transfer path exists. The classes are not downgraded merely because they lack an outward application.

| ID | Document line and rule | Class | Implementation and pin, or enforcement/search |
|---|---|---|---|
| EN01 | :10,22,67–107 Complete result-affecting interpretation and membership test; ambient rules forbidden | P | Review only for completeness/ownership; concrete selection/scheduling/arithmetic pins below |
| EN02 | :14,43,56 No definition identifier/status/support promotion | S | Public runtime fields; EX:61 absence check |
| EN03 | :38 Fixed interpretation/model and fresh run identity | IP | RT:74,96,111,139; SE:80; OB:58 |
| EN04 | :46 Repeated complete inputs produce same supported outcomes | IP | H:161,267,330; RT:285,399; FT/process/FactoryRuntimeBoundaryAcceptanceTest.java:145; EV:433 |
| EN05 | :47–53,97 Definition changes reconcile fixtures; metadata not historical definition basis | P | Review only |
| EN06 | :102 Incidental traversal/thread order cannot select results | P | Review plus D:41,368,401 and EV:433 for current represented cases |
| EN07 | :107,291 Only authoritative TaskEnd work; no ignored start/order markers | IP | F/process/RecordingScheduler.java:111; H:330; RT:446; FT/process/RecordingSchedulerTest.java:111,131; HC:396 |
| EN08 | :123 Bounded advancement composes advance and respects time/count bounds | IP | RT:446,575; SE:363,406,424 |
| EN09 | :131 Command time equals last processed/supported time, guards/budgets don't move it, including faults | IP | RT:156,205,446,521,575; BA:265,278,293,310,324,339 |
| EN10 | :139 Reset fresh runtime/same version; original remains usable | IP | RT:139; SE:526; OB:58 |
| EN11 | :142 Accepted/Rejected/Faulted; rejection unchanged, partial acceptance/fault distinct | IP | RT:156,205; SE:98,122,178,245,312 |
| EN12 | :150–159 Exact version, definite results/value/error/provenance/direct scheduled effects | IP | RT:120,156,205; SE:80,98,122,224,312 |
| EN13 | :160 Scoped capture excludes ordinary advancement history | IP | F/process/RecordingScheduler.java capture window; FT/process/RecordingSchedulerTest.java:35,54,79 |
| EN14 | :162 Shared pending observable outside local queue | IP | RT:434; SE:468 |
| EN15 | :173–195 Online filtering/all-offline fallback, admission first, exact queue+compatible-shared ranking, wide sum, MachineId tie, waiting branch | IP | H:161,169,171,180,301,314,365; D:41,68,328,368 |
| EN16 | :198,220,224,313–331 Local FIFO first/one dispatch; captured eligibility, arrival-order shared fixpoint/reselection/HOL skipping at completion/online only | IP | H:187,225,323,378,387,397; F/machines/Machine.java:173,177; D:88,109,143,161,188,328; BA:265,278,310 |
| EN17 | :203,217 Pre-binding multi-eligible shared only, singleton local only | IP | H:314,365; SE:468; FT/process/FactoryHandlerTest.java:227; D:68,304 |
| EN18 | :209–219 Bound destination collapses eligibility, cannot return to shared pending | NI | spatial-absent evidence; no destination-bound transfer job state |
| EN19 | :230 Transfer begins only when destination admissible | NI | spatial-absent evidence; reservation helper does not compose binding |
| EN20 | :240–258 Immutable order/aggregate identity, N unit children/ordinals/routing/independent dispatch | IP | H:267,311,330; RT:488; FT/process/FactoryRuntimeBoundaryAcceptanceTest.java:75,89,111; FT/process/IntraOrderExecutionAcceptanceTest.java:42 |
| EN21 | :259–265 Atomic release/final child completes aggregate once, no marker, order-level KPIs | IP | H:311,346; RT:488; FT/process/IntraOrderExecutionAcceptanceTest.java:42; FT/process/FactoryHandlerTest.java:293,321 |
| EN22 | :266 No material lots/batches/split/merge | S | Runtime identity/state inventory |
| EN23 | :267–273 Flat 1..100000 envelope/model independent, reject before mutation | IP | H:274,285; FT/process/IntraOrderExecutionAcceptanceTest.java:75,89; FT/process/LargeOrderDecompositionBenchmarkTest.java:53 |
| EN24 | :289–290 Scheduler time then insertion order | IP | product/simulation/src/main/java/com/arcogine/core/queue/Scheduler.java:18,26,33; D:401 |
| EN25 | :310 Same-time supported order solely sequence | IP | RT:521; EV:150 |
| EN26 | :341–349 Factory encoding versus Engine interpretation/runtime/mechanism | P | Review only; concrete grammar in Factory inventory |
| EN27 | :351–356 Present spatial refused before state, absent no transfer | IP | F/model/FactoryRuntimeAssembler.java:36; EX:69,85 |
| EN28 | :360–392 Admissible immutable destination binding/reservation/fixed duration/start fact | NI | spatial-absent evidence |
| EN29 | :399–412 Exact reference-cell Manhattan distance, footprint irrelevant, handling once | U | F/process/TransferTiming.java:41,57; FT/process/TransferTimingTest.java:110,145,158,185,275; no production calls |
| EN30 | :413–422 Maximum-transfer exact publication predicate, distinct footprints, separate time-addition guard | IP | F/model/validation/FactoryModelValidator.java:300,333,349,372; H:303; SP:237,321,397,409,418,431; SE:178 |
| EN31 | :423–427 Spatial same-resource no-transfer; distinct-resource zero retains lifecycle | NI | spatial-absent evidence; helper arithmetic/refusal tests not runtime fulfillment |
| EN32 | :434–471 Transfer state machine/two moments/scheduled arrival/online or bound waiting | NI | spatial-absent evidence |
| EN33 | :477–485 Admission reserved but not active/queued/Busy, ordered bookkeeping | U | F/machines/Machine.java:73,102,122,134,138; FT/machines/MachineInboundReservationTest.java:72,93,162,184,269,321; mutators have no production callers |
| EN34 | :481 No transport resource/congestion capacity model | S | Production model/state inventory |
| EN35 | :486–503 Source offline irrelevant, bound destination offline allowed, no rerouting, exact reservation-to-local-wait conversion | NI | spatial-absent evidence; machine-local offline tests don't prove flight/arrival |
| EN36 | :507–514 No utilization/busy-time/occupancy/bottleneck verdict | S | RT:643; HC:436; absent resource/performance fields |
| EN37 | :520–522 Incomplete-order backlog/completion lead-time/order throughput | IP | H:137,141,148,346; DR:47,80; FT/process/FactoryHandlerTest.java:118 |
| EN38 | :518,524–535 In-flight excluded from queues/active; distinct admission load outward | NI | spatial-absent evidence; RT:643 lacks admission-load field |
| EN39 | :543–585 Floor subtraction, zero-window/empty-mean zero, schedule overflow preflight, saturated lead-time sum, exact completed-order count | IP | product/types/src/main/java/com/arcogine/types/SimTime.java:14,19; H:137,141,148,303,348; DR:32,47,60,80; SE:178; FT/process/FactoryHandlerTest.java:153 |
| EN40 | :586–590 Completion-order binary64 sum/no reassociation/infinity behavior | IU | H:348; checked DR/Handler/Boundary/proportional/dispatch suites, no order-sensitive or infinity vector |
| EN41 | :591–594 Integral conversion rounding above mantissa bound | IU | H:145,152; DR/type-test search, no >2^53 rounding vector |
| EN42 | :603–617 Fresh transfer state/source/destination/start/end and reservation agreement | NI | spatial-absent evidence; current job projection production-only |
| EN43 | :619–621 No coordinates/progress/interpolation as runtime truth | S | Current projections omit them |
| EN44 | :623–628 Existing RunId/fingerprint/cursor/time/state | IP | RT:643; OB:40,85; EV:178 |
| EN45 | :627 Optional authoritative revision in observation metadata | NI | F/process/RuntimeObservationMetadata.java:8 has no revision field; no supported bound constructor; disclosed future shape, not current conditional violation |
| EN46 | :632–671 Transfer started/completed payloads/references/provenance and separate zero-time completion turn | NI | spatial-absent evidence; taxonomy has no transfer variants |
| EN47 | :675–683 Paths/rerouting/congestion/orientation/animation deferred | S | Confirmed field/lifecycle absence |
| EN48 | :690–697,758 Fixtures track semantics; semantic outcomes, no support from fixtures alone | P | Review only; fixture audit below |
| EN49 | :706–709 Ordinal-discriminating FIFO conformance case | IU | FIFO implemented Machine:173,177; D:143 differs JobId/duration but all ordinals zero; D:88 likewise; no adversarial ordinal fixture found |

### runtime-contract.md

| ID | Document line and rule | Class | Implementation and pin, or enforcement/search |
|---|---|---|---|
| RC01 | :49–57 Separate supported types/taxonomy, not internal Event wrapper | IP | RT:488,521; EV:88,386; HC:396 |
| RC02 | :61 Domains own mutable state; supported stream not event-sourcing truth | P | Runtime owns Handler; review only for architectural ownership |
| RC03 | :63–73 Fresh observation sufficient; subsequent deltas agree | IP | RT:643; HC:230,281,349 |
| RC04 | :75 No checkpoint/durable-history/recovery hardening delivered | S | Public runtime API inventory |
| RC05 | :79–85 Publish after mutation, no success on rejection; report actual partial changes on fault | IP | RT:205,446,488,521; EV:187,205,216,278,305,344 |
| RC06 | :87 Future adapter must not forward attempted change early | P | Review only; no adapter |
| RC07 | :91–101 Opaque run/sequence epoch, reset fresh and original usable, quiescence nonterminal | IP | RT:96,139,156,643; OB:58; EV:456; SE:526; BA:293,324 |
| RC08 | :103 Run identity cannot affect outcome | IP | RT:96,521 metadata-only; EV:470 |
| RC09 | :107–116 Contiguous monotonic sequence/initial zero/first one/nondecreasing time, distinguishes same-time and cross-run facts | IP | RT:521,643; EV:88,136,150,178,456,470; OB:40 |
| RC10 | :121–141 Neutral envelope, semantic taxonomy and typed order/child references | IP | F/process/RuntimeEventEnvelope.java:36; RT:488,521; EV:88,386 |
| RC11 | :145–190 Authoritative resource/order/child/pending/performance projections | IP | RT:643; OB:85; HC:436; DR:32,47,80 |
| RC12 | :192–195 No utilization/occupancy/bottleneck fact | S | ResourceObservation/RuntimePerformanceObservation field inventory |
| RC13 | :197 Single coherent observation cursor boundary | IP | RT:521,643; EV:178,187; HC:281 |
| RC14 | :199 Outward DTOs never domain decision inputs | P | Review only; no outward DTO surface to guard |
| RC15 | :203–210 Source fingerprint mandatory; no invented definition ID | IP | RT:529,643; EV:88; EX:61 |
| RC16 | :212–227 Revision only when authoritatively bound; no synthesis/ownership of persistence | S | RT:529 emits empty revision optional; observation metadata has no revision field; no supported bound runtime. Conditional positive integration unbuilt (EN45), not current misattribution |
| RC17 | :235 Destructive drain returns/clears current batch | IP | RT:547; EV:225,230,284,288,300,350,365 |
| RC18 | :236–244 Two drainers cannot each claim complete history; holder retention/shared delivery/gap qualification explicit | P | Drain mechanically pinned RC17; custody/completeness claims review only |
| RC19 | :248–251 No account/verifier/seal/history service or mandatory full history | S | Explicit nonclaim; supported APIs confirm absence |
| RC20 | :255–276 Coverage basis/run/model/history/independent frontier, no invented pre-join history or silent missing tail | P | Review only; no verifier promised |
| RC21 | :278–285 Changes don't prove command/fault/input/definition/reproduction completeness | P | Controller assertion/custody review only; actual publication pinned RC05 |
| RC22 | :289–308 Half-open finality needs basis/prefix plus supported temporal witness or exclusive-controller closure | P | Review only; no interval-seal API claimed |
| RC23 | :310–317 Advance budget/deadline not time/seal/exhaustion; quiescence permits input | IP | RT:575,643; SE:424; BA:293,324,339 |
| RC24 | :319–328 Closure trusted attributable assertion, violations/handover preserve basis/history/frontier/trust | P | Review only |
| RC25 | :339–351 Controller/holder/method/Governance duties; residence/causality/utilization meaning separate | P | Review only; methods not producer facts |
| RC26 | :353–356 No closure API now; concrete future need reopens research | S | API inventory |
| RC27 | :360–366 Future transports/envelopes, stable transport name/sequence semantic meaning | P | Review only; current absence covered RC04 |
| RC28 | :370–375 Current recovery prerequisites run/sequence/snapshot cursor/envelope | IP | RT:125,521,643; OB:40; EV:88,136,178 |
| RC29 | :377–395 Intended recovery diagram; retention/resume/reconnect/checkpoint deferred | S | No journal/resume/checkpoint API; destructive drain only |
| RC30 | :397 Future bounded journal must reveal cursor gaps | P | Review only; journal absent |
| RC31 | :401 Challenge cannot redefine Engine events/dispatch/scoring ownership | P | Current neutral types; semantic boundary review only |
| RC32 | :403–405 No production telemetry/trust/actuation/reconciliation meaning | S | Simulation-runtime field/API inventory |

### transfer-applicability.md

| ID | Document line and rule | Class | Implementation and pin, or enforcement/search |
|---|---|---|---|
| TA01 | :9 Factory authored facts versus Engine transfer/admission and runtime identity | P | Review only |
| TA02 | :11 Absent/complete spatial only, absence distinct from legal zero/presence fingerprinted | IP | F/model/validation/FactoryModelValidator.java:143; SP:144,168,178; CAN:168 |
| TA03 | :13,23 Factory validates; Engine refuses before runtime; workload cannot rescue present record, including legal zero | IP | F/model/FactoryRuntimeAssembler.java:36; SP:110; EX:69 |
| TA04 | :19 Absent record ordinary direct dispatch, no transfer/reservations/events | IP | Assembler:36; H:330; EX:85 |
| TA05 | :20 Legal zero spatial has complete distinct-resource lifecycle | NI | spatial-absent evidence |
| TA06 | :21 Positive spatial has derived positive interval | NI | spatial-absent evidence |
| TA07 | :25 Zero duration still separate completion turn/observable transfer/interleaving | NI | spatial-absent evidence; no-transfer path is distinct |
| TA08 | :29 Validity/executability separate; grammar/interpretation evolution reconciled | P | Concrete admission pins above; future coordinated change review only |
| TA09 | :31 Geometry-only/nonspatial handoff variants not silently admitted | S | Current grammar/runtime admission inventory |

### governance-conformance.md

| ID | Document line and rule | Class | Implementation and pin, or enforcement/search |
|---|---|---|---|
| GC01 | :12 Generic conformance over explicit authoritative state | IP | G/conformance/ConformanceEvaluator.java:101; CE:150,204 |
| GC02 | :14 Frameworks do not shape business objects; no monolithic BusinessModel (:463) | P | GT/GovernanceModuleBoundaryTest.java:23,46 guards dependencies; semantic vocabulary review only |
| GC03 | :18,47 Modeled and externally observed facts have distinct authority | P | G/evidence/EvidenceProvenance.java:19; GE:384; source/trust truth review only |
| GC04 | :49 Governance does not ingest telemetry or reconcile twins | S | Full Governance production declaration/API inventory |
| GC05 | :53–87,279–301 Controls/framework projections and reuse across mappings | NI | No framework/control/mapping implementation in Governance/Storage main sources |
| GC06 | :94–104,123 Content identity distinct from historical occurrence, one fingerprint, zero/one parent | IP | G/ControlledRevision.java:9; CR:25,34,75; BS:767 |
| GC07 | :107 Divergent children of one parent can be accepted | IU | SC/FileArcogineStorage.java:387; BS:727 checks one child only; all Governance/Storage tests searched for siblings/divergence |
| GC08 | :109,125 Revision core excludes approval/deployment/framework/model artifact | S | G/ControlledRevision.java:9; CR:111 |
| GC09 | :127 Recorder provenance is not authorization or actor identity | P | BS:695,1203 pins retained values; meaning/additive future attribution review only |
| GC10 | :146 Authority timestamp, immutable acceptance and exact artifact resolution | IP | SC/FileArcogineStorage.java:368,426,442; BS:635,839,874 |
| GC11 | :148–161 Historical why/approval/deployment/effective-artifact/evidence queries | NI | Revision lookup exists; no composed historical query/audit/authorization service |
| GC12 | :166–187 ChangeSet base/candidate/semantic changes and changed-entity impact | IP | G/change/ChangeSet.java:37; G/change/ChangeSetFactory.java:31,75; ST/governance/change/ChangeSetFactoryTest.java:50,95 |
| GC13 | :183–187 Affected controls/reviewers/required and invalidated evidence | NI | G/change/ImpactScope.java:25 contains changed entities only; main callers have no derivation |
| GC14 | :190 Design history distinct from runtime history | P | GT/GovernanceModuleBoundaryTest.java:23; semantic separation review only |
| GC15 | :196,226 External organizational workflow remains external | S | External references only, no Jira workflow-state implementation |
| GC16 | :224 External workflow reference is association, not revision identity | IP | G/change/ExternalChangeReference.java:10; G/change/ChangeProvenance.java:10; GT/change/ChangeSetTest.java:147 |
| GC17 | :214–228 Change-policy authorization and deployment relationship | NI | No ApprovalRecord/AuthorizationDecision/DeploymentRecord; GT/GovernanceModuleBoundaryTest.java:63 guards current absence |
| GC18 | :235–262 Requirements/assertions and external source identity/edition/locator | IP | G/requirement/Requirement.java:25; G/requirement/ExternalRequirementSource.java:21; GT/requirement/RequirementSourceProvenanceTest.java:29,88 |
| GC19 | :265–268 Finding retains affected objects and explanation | IP | G/conformance/Finding.java:30; CE:204,280,301 |
| GC20 | :266,268 Finding severity and remediation lifecycle | NI | Finding has no severity/remediation fields; absence guard GT/GovernanceModuleBoundaryTest.java:63 |
| GC21 | :271–277 Independent versions, missing evidence UNKNOWN, result distinct from occurrence | IP | G/conformance/ConformanceEvaluator.java:101; CE:333; GE:461 |
| GC22 | :345–364 Exceptions/risk acceptance, rationale/owner/period/expiry/compensation, no rewriting FAIL | NI | No such state/API in main sources; same absence guard |
| GC23 | :368–390 Bounded pre-change impact and conformance evaluation | IP | G/change/ChangeSetFactory.java:75; G/conformance/ConformanceEvaluator.java:101; CE:301,441; ST/governance/conformance/PreChangeConformanceProvingCaseTest.java |
| GC24 | :383–389 Invalidation/required review/authorize-reject-revise loop | NI | Values/evaluations exist; no authorization controller/review requirement/invalidation engine |
| GC25 | :396–417 Reproducible AuditSnapshot, historical basis and explicit missing-history gaps | NI | Full main/test inventory: no audit projection/API or audit reconstruction test |
| GC26 | :436,467–477 Headless evidence is in-memory; no producer integration/GRC/certification/actuation | S | G/conformance/InMemoryEvaluationOccurrenceAuthority.java:27; Storage only persists revisions |
| GC27 | :479–520 Architecture review and reconciliation triggers | P | Review only; not an executable governance policy |

### governance-evidence.md

| ID | Document line and rule | Class | Implementation and pin, or enforcement/search |
|---|---|---|---|
| GE01 | :9–14 Evidence-need declaration; absent external evidence UNKNOWN | IP | G/assertion/EvidenceRequirement.java; G/conformance/ConformanceEvaluator.java:101; CE:333 |
| GE02 | :46–50 Origins stay distinguishable | P | G/evidence/EvidenceProvenance.java:19,49,67,89; GE:384,395; source semantics review only |
| GE03 | :54–76 Source/revision reference, intrinsic subject/statement/units/time/unknowns | IP | G/evidence/EvidenceReference.java:21; G/evidence/EvidenceProvenance.java:19; GE:59,882,1042 |
| GE04 | :59–63 Producer handle can suffice without global evidence ID | IP | G/evidence/EvidenceReference.java:58; GE:503 |
| GE05 | :64–72 Mutable aliases/digests insufficient for reliance | P | Required nonblank reference fields pinned GE:882; source sufficiency review only |
| GE06 | :80–84 Source-revision equality and no rebinding | IP | G/evidence/EvidenceReference.java:86; G/evidence/InMemoryEvidenceReferenceAuthority.java:21; GE:59,74,219 |
| GE07 | :85–88 Correction/retraction/supersession/reinterpretation adds distinguishable relation | IP | G/evidence/EvidenceRelation.java:6; GE:74,90,106,243 |
| GE08 | :89–92 Redelivery not independent evidence; distinct producers may stay distinct | IP | G/evidence/InMemoryEvidenceReferenceAuthority.java:21; GE:59,423 |
| GE09 | :93–95 No required physical append-only/perpetual storage | S | In-memory authority explicitly supports bounded headless contract |
| GE10 | :99–110 Source distinct from contextual target/definition/role/time/applicability; reuse independent | IP | G/evidence/EvidenceUse.java:18; GE:287,948; CE:441 |
| GE11 | :111–118 Packaging/copying cannot manufacture corroboration or erase recorded basis | IP | G/evidence/InMemoryEvidenceReferenceAuthority.java:21; G/conformance/EvaluationOccurrence.java:36; GE:423,948; bounded recorded-value guarantee |
| GE12 | :119–121 Use identified by occurrence and position | IP | G/conformance/EvaluationOccurrenceDraft.java:100; GE:308 |
| GE13 | :128–150 Producer intrinsic provenance separate from later use; unknowns not fabricated | IP | G/evidence/EvidenceProvenance.java:67,89; GE:384,395,406,982,1042; explicit headless inputs, not integration |
| GE14 | :144–147 Cross-model comparator use explicit | IP | G/conformance/EvidenceConformanceEvaluator.java:101; GE:287,680 |
| GE15 | :151–154 Domain ownership not acquired by evidence consumption | P | GT/GovernanceModuleBoundaryTest.java:23,46; detailed semantics review only |
| GE16 | :158–179 Declared adequacy separate from scope/outcome; missing basis UNKNOWN; evidence of absence may FAIL; alternatives/exclusions/gaps | IP | G/evidence/EvidenceUse.java:31; G/conformance/EvidenceConformanceEvaluator.java:33; GE:343,365,742,835,912,948; supplied judgment only |
| GE17 | :180–187 Truthful conflict/applicability/trust determination | P | Caller supplies EvidenceApplicability and EvidenceJudgment; no automatic conflict/freshness/trust solver; review only |
| GE18 | :183–185 Invalid bindings refused; infrastructure failure not real-world FAIL | IP | G/conformance/EvidenceConformanceEvaluator.java:113; GE:659,680,807 |
| GE19 | :191–198 Claim-specific compatibility, no guessed Engine definition | P | Explicit producer metadata GE:395,406; compatibility assertion review only |
| GE20 | :205–211 Equal results distinct immutable occurrence identity | IP | G/conformance/InMemoryEvaluationOccurrenceAuthority.java:67; GE:461 |
| GE21 | :212–222 Fixed metadata/output/use lists and authority recording time | IP | G/conformance/EvaluationOccurrence.java:36; G/conformance/InMemoryEvaluationOccurrenceAuthority.java:67; GE:440,948; executable rule excluded below |
| GE22 | :212–217,231–239 Immutable exact executable rule; changed meaning cannot reuse definition binding | NI | Assertion.java:32 retains arbitrary AssertionRule; EvaluationOccurrence.java:40 captures reference; no freezing or cross-occurrence definition-binding authority |
| GE23 | :240–247 New facts don't replace recorded outcomes/metadata; absent lookup disclosed | IP | G/conformance/EvaluationOccurrence.java:36; authority :92; GE:106,440,948; no durable corruption/recovery claim |
| GE24 | :248–254 Support retirement doesn't fabricate meaning or authorize another use | P | Review only; no historical producer-definition service |
| GE25 | :258–263 Point targets only, accumulating continuation excluded | S | G/evidence/EvidenceUse.java:18; GE:497 |
| GE26 | :267–290 Governance/Operational/Engine/controller/holder roles; reference not authenticity | P | Dependency guards; role/trust/custody review only |
| GE27 | :294–329 Explicit fixtures/in-memory bounds; no durable definition/evaluation history or Operational producer integration; representation/security/policies open | S | G/conformance/InMemoryEvaluationOccurrenceAuthority.java:27; full Storage main API inventory |

### controlled-revisions.md

| ID | Document line and rule | Class | Implementation and pin, or enforcement/search |
|---|---|---|---|
| CR01 | :9–27 Content equality distinct from occurrence, including rollback | IP | G/ControlledRevision.java:9; CR:75; BS:767 |
| CR02 | :29–60 Opaque RFC9562 UUIDv4, canonical lowercase text, independent identity | IP | product/types/src/main/java/com/arcogine/types/ControlledRevisionId.java:7,16,20; product/types/src/test/java/com/arcogine/types/ControlledRevisionIdTest.java:12,20,28; CR:111 |
| CR03 | :54 Non-rebinding ID | IP | SC/FileArcogineStorage.java:368; BS:666,695 |
| CR04 | :62 Ordering is not domain chronology | P | UUID generation; lexical enumeration :364; chronology interpretation review only |
| CR05 | :64–66 No timestamp ID or physical key leaking | S | Public ID contains only UUIDv4 |
| CR06 | :82–109 Exactly one fingerprint, generation independent, no schema ID | IP | G/ControlledRevision.java:9; CR:59,111 |
| CR07 | :113–138,144–148 Root/one parent, immutable lineage, no self-parent, accepted parent, acyclic acceptance | IP | G/ControlledRevision.java:15,23; SC/FileArcogineStorage.java:387; CR:25,34; BS:727; sibling acceptance weakness GC07 |
| CR08 | :140 Undefined merge/branch/ref/rebase/cherry-pick semantics | S | No such APIs |
| CR09 | :150 Conditional cross-definition lineage depends on admitted evolution | P | Review only; current provider deliberately accepts one build binding |
| CR10 | :154–170 Rollback new revision, no ID reuse/history rewrite | IP | SC/FileArcogineStorage.java:368; BS:666,767 |
| CR11 | :174–212 Fixed accepted time/recorder/ID/fingerprint/parents, not authorization | IP | G/RevisionProvenance.java; SC/FileArcogineStorage.java:426; CR:46; BS:635,695,1203 |
| CR12 | :214–266 Labels/workflow/authorization/artifact bytes/URI not revision core/prerequisite | S | G/ControlledRevision.java:9; CR:111 |
| CR13 | :268–273 Authority exact artifact; Factory verifier, Storage realization | IP | G/ControlledRevisionAuthority.java:15; SC/FileArcogineStorage.java:442; BS:799,839; A:34,43 |
| CR14 | :275–281 Exact build binding; earlier encoding/mismatch roots refused untouched | IP | SC/FileArcogineStorage.java:234,259; BS:1121,1288 |
| CR15 | :285–302 Authority at persistent acceptance; uniqueness/parent integrity/restart resolution | IP | SC/FileArcogineStorage.java:368,426,442; BS:80,635,666,727 |
| CR16 | :291–325 No broader retained support, Git-like/crypto features, or content commitment from UUID | S | Current binding and public API surface; future support conditions review governed |

### storage.md

| ID | Document line and rule | Class | Implementation and pin, or enforcement/search |
|---|---|---|---|
| SS01 | :9–18 Governance owns meaning; domain-neutral verifier; Storage private mechanism | P | A:34,43 mechanically guards dependencies; interpretation ownership review only |
| SS02 | :20–23 Public open/storage/authority, private backend, per-call acceptance | IP | SC/BuiltInStorage.java; SC/ArcogineStorage.java; SC/FileArcogineStorage.java:209,368; BS:104,635 |
| SS03 | :27–32 Exact owned root only, no foreign/incomplete adoption/repair; initialization locking | IP | SC/FileArcogineStorage.java:180,214,234,254; BS:295,420,435,984,1031 |
| SS04 | :34–42 Legacy private naming/strict discriminator; prior encoding/mismatch refused untouched | IP | SC/FileArcogineStorage.java:83,87,234,259; BS:1121,1288 |
| SS05 | :39–42 Binding not semantic equivalence/archive/compatible reader | S | Exact string binding; no archive/migration service |
| SS06 | :44–49 Verified candidate/accepted parent/artifact-before-record/time/duplicate rejection | IP | SC/FileArcogineStorage.java:368,426; BS:635,666,695,727,820 |
| SS07 | :50–55 Exact Unicode; bad recorder before lock/artifact, bad binding before root | IP | SC/FileArcogineStorage.java:259,306,316,696; BS:1203,1221,1229,1237,1253,1271 |
| SS08 | :56–57 Shared artifact for distinct occurrences/rollback | IP | SC/FileArcogineStorage.java:398; BS:767 |
| SS09 | :59 Absent ID only empty lookup | IP | SC/FileArcogineStorage.java:325; BS:401,597 |
| SS10 | :59–63 Missing/unsupported/corrupt artifact explicit; no current reconstruction/repair | IP | SC/FileArcogineStorage.java:442,472,496,526; BS:401,498,526,799,839,874,1322,1348 |
| SS11 | :62 Deterministic lexical ID enumeration | IP | SC/FileArcogineStorage.java:344,364; BS:936 |
| SS12 | :67–69 Locks serialize normal conflicting acceptance, threads and separate-JVM reopening | IP | SC/FileArcogineStorage.java:647; BS:80,892,1031; not independent-process contention |
| SS13 | :70–71 Unfinished root may refuse; no network/hostile-write/process-contention support | S | Stated bounded support; tests inspect threads/reopening |
| SS14 | :73–74 Forced temporary file/atomic install; unsupported move fails | IP | SC/FileArcogineStorage.java:590; BS:148,213 |
| SS15 | :74–78 Record install authoritative, pre-install failure unaccepted, post-install ambiguity/query, safe cleanup | IP | SC/FileArcogineStorage.java:410; BS:116,245,267,666 |
| SS16 | :79–93 No kill/power/disk-loss/repair/backup/authenticity/permanent cross-version support | S | Failure-seam scope, exact definition-binding refusal; BS:1121 |

### factory-design.md

| ID | Document line and rule | Class | Implementation and pin, or enforcement/search |
|---|---|---|---|
| FD01 | :10–18,30,32,36–40 Semantic/lifecycle/experience ownership; revision not simulation time; no second consumer ontology | P | Review only; current publication/assembler derivative supplies bounded implementation |
| FD02 | :18,28–29 Publication without simulation and runtime without editor | IP | F/model/FactoryModelPublisher.java:17; FT/model/FactoryModelPublisherTest.java:36; RT:111; FT/model/FactoryModelImmutabilityTest.java:72 |
| FD03 | :31,123–125,259 Mutable invalid draft/workspace, persistence/branch/merge/collaboration | NI | Full Factory main sources: no FactoryDraft/shared draft service; Challenge owns immutable candidate only |
| FD04 | :37,104–106 No inferred competence/reusable specification/qualification/hierarchy | S | ConfiguredResource/explicit eligible-ID fields only |
| FD05 | :38 Validation reflects everything executable | NI | Overbroad wording: publisher accepts valid spatial but assembler :36 refuses it; publication and execution separately pinned EX:69 |
| FD06 | :40,354,358 Runtime provenance exact source content | IP | RT:529,695; OB:40 |
| FD07 | :81 No scenario schema/envelope selected | S | Production input API/record inventory |
| FD08 | :94 Future scenario model/workload/settings composition | NI | No Scenario implementation; status explicitly future |
| FD09 | :100–103,107,127,157 Production records/explicit eligibility and current authored floor/layout/handling facts | IP | F/model/FactoryModel.java; F/model/OperationStepDefinition.java:15; F/model/spatial/SpatialRecord.java:22; CAN:182,212; SP:101 |
| FD10 | :108–116 Seeds/workload/limits/context versus mutable runtime ownership | P | Review only; A:80,90 guards lifecycle writes; current workload API RT:156 |
| FD11 | :114,178–180 Transfers/runtime spatial indexes/relationships/scheduling metadata | NI | Assembler :36 refuses spatial; no transfer state/index/composition |
| FD12 | :130–143 Validated immutable publication/fingerprint, validity distinct from execution | IP | F/model/FactoryModelVersion.java:30,37; FT/model/FactoryModelPublisherTest.java:26; EX:69 |
| FD13 | :143 Publication not controlled persistence/approval/deployment | S | Publisher creates in-process version only |
| FD14 | :147–157,298 Rich policies/constraints/pools/geometry/orientation/zones/connection/reachability | NI | Closed Factory records have none; current spatial record only floor/rate/overhead/layout |
| FD15 | :161–169,260–264,294–304,331–343 Semantic/presentation and executability/objective/consumer rule ownership | P | Review only; current Challenge/Factory axes independently pinned |
| FD16 | :173–177,182–186 Runtime resource/routing derivatives from one version | IP | F/model/FactoryRuntimeAssembler.java:34; FT/model/FactoryRuntimeAssemblerTest.java:17 |
| FD17 | :190–215 Design events/revision history not simulation stream | P | Review only; no design event stream delivered |
| FD18 | :217–237,454,456,503 Assessment→authorization→deployment/reconciliation/external policy provenance | NI | Publication/revision/ChangeSet prerequisites exist; no authorization/deployment/reconciler; see central Governance/Operational gaps |
| FD19 | :233 Persisted revision does not require authorization | IP | SC/FileArcogineStorage.java:368; BS:635; revision core excludes approval CR:111 |
| FD20 | :235,243–257,456 Factory/Governance/Operational/consumer ownership, artifacts distinct | P | Review only; existing narrow owners covered in their inventories |
| FD21 | :258,351–365 Comparison, content versus occurrence, ChangeSet/impact integration | IP | F/change/FactoryModelSemanticComparator.java:60; FT/change/FactoryModelSemanticComparatorTest.java:86,149,193,209; BS:767 |
| FD22 | :268–290 Consequential resource hierarchy/membership versus movement behavior | NI | Hypothetical disclosed; no hierarchy identity/membership execution |
| FD23 | :311–317 Reference/ID/steps/eligibility/footprint/nonoverlap/max-transfer publication predicates | IP | F/model/validation/FactoryModelValidator.java:42,241,302,367; FT/model/validation/FactoryModelValidatorTest.java:41,51,86,95,148; SP:311,321,409,418,431 |
| FD24 | :325–328 Shared throughput/lead-time/utilization/safety verification objectives | NI | Runtime produces bounded facts, not shared named objective definitions/evaluator; full main inventory |
| FD25 | :360,438–452 Broader workflow/branch/ref/history/migration/registry support omitted | S | Explicit exclusions; current public records/ports |
| FD26 | :366–386 Semantic identity/one closed grammar/one aggregate fingerprint | IP | F/model/FactoryModelCanonicalForm.java:170; CAN:182 |
| FD27 | :374–375,397–419,419–425 Future evolution/exact references/promotion/comparison admission | P | Review only; current spatial difference detection CAN:182/comparator test :209 |
| FD28 | :388–395 Complete optional record, absence distinct zero/no substituted defaults | IP | F/model/spatial/SpatialRecord.java:25; FT/model/spatial/SpatialRecordTest.java:61; CAN:168 |
| FD29 | :401–403 Current discarded-prefix decoder/build-binding refusal | IP | F/model/FactoryModelArtifact.java:33,41; ART:49; BS:1121 |
| FD30 | :415–419,427–436 Unsupported spatial not stripped/synthesized; refuse before state | IP | Assembler :36; EX:69,85 |
| FD31 | :460–474 Shared field-addressed deterministic validation/atomic publication/instantiation/provenance | IP | Validator :42; F/model/validation/ModelValidationError.java:4; SP:453; publisher test :26; immutability test :72; OB:40,58 |
| FD32 | :464 Explicit validation severity | NI | ModelValidationError.java:4 has field/message only, no severity |
| FD33 | :474,476–540 Consumer drafts and UI independence/module admission/review/reconciliation triggers | P | A guards selected dependency/mutation rules; remaining design/process clauses review only |

### factory-model.md

| ID | Document line and rule | Class | Implementation and pin, or enforcement/search |
|---|---|---|---|
| FM01 | :8–29,163–175 Grammar authority, coordinated evolution/promotion, semantic equivalence/hash/collision limits | P | Review only; concrete identity grammar pinned below |
| FM02 | :17–18 Deterministic canonical bytes/fingerprint for same content/definition | IP | F/model/FactoryModelCanonicalForm.java:56,170; CAN:143,156,195 |
| FM03 | :21–26 No fingerprint/raw-decoder exact historical-definition guarantee | S | Namespace unversioned, compiled binding separate |
| FM04 | :31–38 Distinct occurrences may share fingerprint; validated production+optional spatial publication | IP | F/model/FactoryModelVersion.java:30; publisher test :26; CAN:143,156; BS:767 |
| FM05 | :44–64,72–77 All declared production fields canonical, list shape/set semantics | IP | Canonical encoder :170; CAN:212,224,259,268; comparator test :454 |
| FM06 | :66–70 Unique resource/operation/product IDs, positive concurrency/duration, nonempty steps/eligibility, valid references, scalar Unicode | IP | Validator :42,115; validator tests :41,51,86,95,105,114,127,138,148; CAN:275,298 |
| FM07 | :78 Capacity/setup survive assembly | IU | Assembler :49,50; assembler test :17 asserts concurrency only; all Factory tests searched for both accessors |
| FM08 | :78 Capacity/setup exposed through resource observations | IU | RT:653,654; OB:40,85 uses null/0, no assertion of those fields |
| FM09 | :79–85 Metadata does not affect eligibility/ranking/queue/duration/completion | IU | Routing duration passed at assembler :59, no reads in selector/completion; no differential metadata outcome test found |
| FM10 | :89–112 Complete spatial record/ranges/zeros/order/exact coverage/absence distinct from zero/partial rejection | IP | Validator :143,164,208,221,241; SP:144,168,178,187,198,219,263–380; spatial record test :61; CAN:168 |
| FM11 | :114–130 Anchor/extent, overflow-safe containment/nonoverlap, max-transfer intermediate arithmetic, not arbitrary current-time guarantee | IP | Validator :241,333,367; SP:237,304,311,321–353,387,397,409,418,431 |
| FM12 | :132–137 Every spatial distinction/presence fingerprinted, no Engine interpretation in encoding | IP | Encoder :170; CAN:182,319 |
| FM13 | :139–142 No orientation/path/topology/transport/animation facts | S | SpatialRecord.java:22 fields |
| FM14 | :146–149 Production executable, spatial refused before state | IP | Assembler :36; EX:69,85 |
| FM15 | :154–165 SHA256/domain rendering/lowercase/prefix included | IP | Canonical :56; product/types/src/main/java/com/arcogine/types/ModelFingerprint.java:18; corresponding ModelFingerprintTest.java:11,25; CAN:143,156 |
| FM16 | :179–201 Exact prefix, big-endian count/I64/signed IDs | IP | Canonical :49,380,387,391; CAN:105,143; ART:49,85 |
| FM17 | :203–211 Length-framed raw UTF8, no normalization, nullable binary64/exact finite/signed zero/canonical NaN | IP | Canonical :395,411; CAN:143,212,224,259,268 |
| FM18 | :213–268 Exact presence bytes/count/aggregate field order/absent single byte | IP | Canonical :170,245,333,344; CAN:143,156; ART:122,164 |
| FM19 | :269–271 Header-before-resource grammar permits early predicate rejection | P | Layout rationale says decoder **can** reject, not that current decoder does. Current :245–313 decodes records before publication predicates; no implemented early-rejection claim counted |
| FM20 | :272–290 Spatial suffix structural coverage/anchor/extent/no translation normalization/list identity/set sorted signed | IP | Encoder/decoder :170,245; Validator :241; CAN:195,224,342,351; ART:122,147 |
| FM21 | :296–319 Strict prefix/marker/count/truncation/UTF8/trailing/noncanonical rejection and publication predicates reapplied | IP | Canonical readers :245,333,344,352,369,380; Artifact :33; ART:35,85,122,147,164 |
| FM22 | :323–334 Stored fingerprint decode/reencode; refuse unsupported namespace/algorithm/discarded prefixes | IP | Artifact :37,41; ART:35,49,85 |
| FM23 | :336–342 Compiled-definition binding and Storage mismatch refusal | IP | Canonical :109,134; FT/model/FactoryModelDefinitionBindingTest.java:137,157,171; BS:1121 |
| FM24 | :346–349 Deliberate independent golden regeneration, not serializer-generated authority | P | CAN:31 vectors literal; regeneration process review only |
| FM25 | :353–388 Required grammar/presence/order/translation/Unicode/binary64 conformance cases | IP | CAN:105,143,156,168,195,224,259,268,275,319,342,351; ART:35,49,85,122,164; SP:168–219 |
| FM26 | :392–398 Invalid predicates and accepted edge boundaries on publication | IP | SP:263,268–431; validator tests :41–148 |
| FM27 | :392–395 Explicit every-listed-invalid-predicate decode fixture obligation | IU | Shared validator implemented; ART:147 directly mutates overlap/zero-floor only; no decode mutation pins remaining listed spatial/production predicates |
| FM28 | :402–406 No Engine/controlled/interchange/universal canonicalization/stable-support ownership | S | Field/codec/public API inventory |

### factory-resource-semantics.md

| ID | Document line and rule | Class | Implementation and pin, or enforcement/search |
|---|---|---|---|
| FR01 | :10–12,88–90 Complete independently identified configured participant; same MachineId into runtime/correlation | IP | F/model/ConfiguredResource.java:15; assembler :43,45; RT:651; assembler test :17; OB:40; CAN:212 |
| FR02 | :14,18–31 Equal values not reusable identity; specification/qualification/hierarchy/location/asset/state/economics roles separate | P | Review only |
| FR03 | :23–25,28–29 Future specification/qualification/hierarchy/physical asset/economics not Factory equality | S | Current record/eligibility/canonical grammar contain none |
| FR04 | :26 Authored layout independent of hierarchy | IP | SpatialRecord :22; CAN:182 |
| FR05 | :27 Mutable runtime never immutable design identity | P | A:80,90 and immutability test :72; general equality boundary review only |
| FR06 | :35–47 Conditional reusable-specification admission/equality | P | Review only; no future shared identity claimed delivered |
| FR07 | :51 Eligibility authored permission; duration step-owned | IP | OperationStepDefinition :15; validator :42; validator tests :51,65,127; assembler :59; D:41,143 |
| FR08 | :53–84 Qualification/pools/typed resources/physical correspondence admission; don't infer from collapsed fields | P | Review only; current absences FR03 |
| FR09 | :90–92 New specification/qualification/pool/correspondence requires owning reconciliation | P | Review only |

### challenge.md

| ID | Document line and rule | Class | Implementation and pin, or enforcement/search |
|---|---|---|---|
| CH01 | :11 Explicit deterministic game/outcome evaluation | IP | C/evaluation/ReferenceChallengeEvaluationPolicy.java:30; CT/evaluation/ReferenceChallengeEvaluationPolicyTest.java:19 |
| CH02 | :11,31,37 Challenge does not simulate/read Factory; no candidate→Factory projector | S | Full Challenge public input/import/dependency inventory; CT/catalogue/EquipmentCatalogueValidatorDependencyBoundaryTest.java:15 |
| CH03 | :31–37,99 Admission, publication and executability independent | IP | product/consumer/challenge-factory-integration-test/src/test/java/com/arcogine/challenge/factoryintegration/FactoryPublicationValidButChallengeInadmissibleTest.java:56; EX:69 |
| CH04 | :41–48 Game identities separate from model/revision/run/object identity | IP | C/ChallengeDefinition.java:22; CT/ChallengeDefinitionTest.java:22,32,42; CT/attempt/ChallengeAttemptTest.java:35,80 |
| CH05 | :50,97–102 Policy version/economics/attribution/state/history ownership discipline | P | Current dependency/retention tests pin bounded values; future semantic admission/composition review only |
| CH06 | :54 Immutable definition/no score/workload game reference not routing | IP | C/ChallengeDefinition.java:22,59; CT/ChallengeDefinitionTest.java:58,77 |
| CH07 | :56 Construction not validity; complete deterministic code/path/message issues | IP | C/validation/ChallengeDefinitionValidator.java:35; CT/validation/ChallengeDefinitionValidatorTest.java:29,188,220 |
| CH08 | :60 Offer item/cost/optional limit; invalid catalogue refused | IP | C/catalogue/EquipmentOffer.java; C/catalogue/EquipmentCatalogueValidator.java; CT/catalogue/EquipmentCatalogueValidatorTest.java:26,47,58 |
| CH09 | :62 Exact economics/remaining budget; unresolved/overflow failures; order independent success/failure | IP | C/economics/DraftEconomicsCalculator.java:37,73; CT/economics/DraftEconomicsCalculatorTest.java:31,48,64,102,115,129,141,172,191,215,264 |
| CH10 | :66–75 Complete deterministic admission issues, duplicate IDs/catalogue binding/items/limits/budget/bounds/overlap; list order independent | IP | C/admissibility/CandidateAdmissibilityPolicy.java:29,43,47,71,93,110,123,136,149; CT/admissibility/CandidateAdmissibilityPolicyTest.java:26,40,63,81,95,108,132,147,158,172,184 |
| CH11 | :75 Immutable placement-only candidate cannot replace workload | IP | C/admissibility/CandidateDraftSnapshot.java; admission test :53; ChallengeDefinition workload |
| CH12 | :79 Explicit definition/design/run/outcome/cost; completion/tick facts only | IP | C/evaluation/ChallengeEvaluationInput.java; C/evaluation/AuthoritativeOutcomeFacts.java; evaluation policy :30; policy test :36,69 |
| CH13 | :81 Completion/deadline/budget success; exact score margin+budget+1, zero/issues on failure | IP | Reference policy :30,54; policy test :19,48,59,75,85 |
| CH14 | :81 Unsupported policy refused on load | IP | C/content/ChallengeContentLoader.java:164; CT/content/ChallengeContentLoaderTest.java:257 |
| CH15 | :85 Immutable attempt retains supplied candidate/economics/evaluation/provenance without recomputation | IP | C/attempt/ChallengeAttempt.java:22; CT/attempt/ChallengeAttemptTest.java:24,45,59,71,80 |
| CH16 | :85 Retained candidate is admitted | NI | Attempt :22,45 checks nonnull only; no proof/assessment/challenge/catalogue binding; direct test construction |
| CH17 | :85,87,104 Completed record not workflow; comparison not Factory/Engine-equivalence history | S | No workflow/Factory dependency; comparator only game identity/policy |
| CH18 | :87 Exact challenge/policy compatibility, structured refusal, no reevaluation; score/margin/budget/cost deltas | IP | C/comparison/ChallengeAttemptComparator.java:26,44,62; CT/comparison/ChallengeAttemptComparatorTest.java:42,76,92,108,146 |
| CH19 | :91 Untrusted JSON structured deterministic failure, no uncaught malformed construction; schema separate identities | IP | Loader :60,63,90,211,413; loader test :84,91,132,216,236,407,708,736,759 |
| CH20 | :91 Pure loader no I/O | S | String-based production entrypoints, no I/O |
| CH21 | :91 Combined document validation/catalogue binding/reference resolution | IP | Loader :274; loader test :460,470,494,514,539,558,572 |

### operational-execution-digital-twin.md

Operational absence search: inspected `product/settings.gradle.kts:3`, all shipped Java declarations and tests, and callers for Operational/Continuation/Reconciliation/Correspondence/Deployment/ExternalCommand/ExternalObservation/ExecutionContext/DigitalTwin/Genesis/Divergence. Excluded build output and research-experiments. No Operational module, establishing record, durable continuation, external realization or twin implementation exists. Governance reference values are explicitly distinguished below.

| ID | Document line and rule | Class | Implementation and pin, or enforcement/search |
|---|---|---|---|
| OT01 | :14,19,61,67–69 Same semantics across hybrid realization, independently representable concerns, relational twins/predictions | NI | Operational absence search; no external realization/correspondence/reconciler |
| OT02 | :18,24–26 Existing Factory/Engine/Governance ownership, no substitutes | P | Existing owner/ArchUnit evidence above; future Operational consumption review only |
| OT03 | :22,29 No real-world commands or assumed retained/distribution capability | S | Production public APIs and absence search |
| OT04 | :49,65 No global execution taxonomy/DigitalTwinId | S | No declarations; future admission also review constraint |
| OT05 | :75,94,106 Adopted accountable continuation/continuity/loss/divergence/identity attachment and non-inference | NI | No operational identity; detailed owning contract below, same gap counted once |
| OT06 | :100,169 No production type/mechanics/registry/closure/ownership or required conceptual operation types selected | S | Explicit deferred choices/absence |
| OT07 | :124–145 Distinct modeled/external/observed/reconciled/predicted truth and explicit uncertainty reconciliation | NI | No twin state model/reconciler |
| OT08 | :151–173,217 Shared operation meaning/external lifecycle, request≠acceptance≠physical transition≠observation≠interpretation; correlation/retry/partial outcome/compensation | NI | No external operation/adapter/command lifecycle |
| OT09 | :191–199 Actor/source/subject/continuation separation; policy question not generic public schema | P | Overview role principles; review only |
| OT10 | :201,449–460 Consequential authenticity/credentials/least privilege/revocation/safety/fail-safe/idempotency/ambiguity/visible rejection/physical compensation | NI | No consequential execution or trust/authority implementation; :460 discloses this |
| OT11 | :211 Generic actor/security mechanisms/ownership unresolved | S | No choices selected |
| OT12 | :243–267 Authorized deployment/transform/effective artifact/apply/verify/promote/rollback with independent provenance | NI | No DeploymentRecord; GT/GovernanceModuleBoundaryTest.java:63 guards current absence |
| OT13 | :269 Governance history does not implement deployment | S | Revision authority only |
| OT14 | :275 Durable external observation source/subject/value/units/times/quality/trust/raw reference | NI | Governance fixture/reference values don't implement durable ingestion |
| OT15 | :277 Source provenance need not contain Arcogine model/revision/continuation | IP | Bounded Governance value only: G/evidence/EvidenceProvenance.java:89; GE:384 |
| OT16 | :287–310 Qualified authoritative correspondence/interval/history/unknown/conflict, no implicit name match, mapping responsibilities separate | NI | No correspondence authority or mapping adapter |
| OT17 | :314,410 Industrial adapters don't define identity/domain truth | P | Review only; no industrial adapters |
| OT18 | :318–326 Reconciliation states/historical source/policy/trust/temporal attribution and distinct time dimensions | NI | No reconciler/result/temporal twin record |
| OT19 | :330,352 Physical world not replayed/no unbounded durable history/permanent reexecution | S | Current Engine scope and no Operational history service |
| OT20 | :332,354 Seek/reconstitution/replay/checkpoint/fork, identity effects | NI | No Operational history/restore/fork; continuation consequences in owning contract |
| OT21 | :356 Additional identity requires lifecycle/equality need | P | Review only |
| OT22 | :360–378 Governed drift/calibration→candidate→verification→revision→optional deployment | NI | No observed-feedback composition; existing model immutability not that loop |
| OT23 | :380,394 Governance/Factory/Engine retain meaning and existing responsibilities | P | Existing owner enforcement; future feedback/integration review only |
| OT24 | :384 Operational acquisition/reconciliation supplying Governance use | NI | Operational producer absent |
| OT25 | :390 Point evidence targets not accumulating identity | IP | G/evidence/EvidenceUse.java:18; GE:497; bounded exclusion, no continuation semantics |
| OT26 | :396–406 FactoryRuntime not production control; shared operation ownership open; no implied transport/history | S | API and module inventory |
| OT27 | :412–441 Separate adapter relations/transformation/trust/loss/retry/provenance/compatibility, durable record/retention/migration/temporal capability | NI | No adapters/durable Operational artifacts |
| OT28 | :439,443,466–473 Registry/storage/bitemporal/primitives/coordination/sequencing choices open | S | Explicit nonchoices; absence search |
| OT29 | :475 Hard-to-reverse decisions reconciled with consumers | P | Review only |

### operational-continuity.md

Every positive rule below is expressly adopted but unbuilt (:3,229), confirmed by the Operational absence search. Clauses are grouped only within the same identity/continuity contract; their document summaries do not create additional implementation gaps.

| ID | Document line and rule | Class | Implementation and pin, or enforcement/search |
|---|---|---|---|
| OC01 | :23–25 Accumulating accountable conduct/conclusions, reference to accepted establishing record | NI | No establishing/continuation record or authority |
| OC02 | :27–37 Not installation/model/subject/run/deployment/twin/global kind | S | No alternate global execution-kind implementation |
| OC03 | :41–80 Neighbour identities/correlations distinct; genesis or parent-boundary fork only; authoritative at acceptance, never inferred from infrastructure | NI | No identity-establishment/acceptance capability |
| OC04 | :84–100 Answerable accepted facts preserve identity, representation/bounded compaction independent; loss forks/gaps, summary exception, set-based frontier, unproven continuity forks | NI | No accepted account/frontier/loss protocol |
| OC05 | :106–110 Authority commit-boundary acceptance monotonic; provisional/rejected excluded; projections derive status; determinable accepted set | NI | No durable operational record acceptance |
| OC06 | :112 No storage/arrival/transaction semantics selected | S | Explicit scope/nonchoice |
| OC07 | :118–132 Earlier/unproven state forks, head fork permitted, identity at first acceptance, mandatory immutable 0..1-parent/boundary lineage, inspection creates nothing | NI | No fork/history authority |
| OC08 | :136–140 Durable per-fact relationship to extended frontier; undeclared divergence remains provable | NI | No divergence evidence/frontier representation |
| OC09 | :138 No prescribed physical evidence layout | S | Explicit scope/nonchoice |
| OC10 | :144–150 Exclusive extension obligation; visible detectable divergence finding, forward remedy, at most one entitled continuation | NI | No finding/remediation/extension-authority protocol |
| OC11 | :154–162 State convergence never identity equality; immutable historical binding/no identity reissue | NI | No account identity/equality/binding authority |
| OC12 | :166 Closure/retirement unsettled | S | Explicit deferred capability |
| OC13 | :176–193 Conduct/conclusions carry identity; external facts don't; source preserved, later relating record, independent and late observations reusable | NI | No identity-bearing Operational conduct/relating records or ingestion |
| OC14 | :195 Governance point-use precedent does not settle accumulation | S | G/evidence/EvidenceUse.java:18; GE:497 |
| OC15 | :199–201 Identifier and continuity evidence confer no extension/action authority | NI | No implemented account/entitlement distinction; adopted positive contract |
| OC16 | :205–229 Second grouping/type/encoding/storage/order/evidence/coordination/closure/module/multiparent/session/sequencing open until concrete consumer | S | Explicit nonchoice; no speculative ID type demanded |

### overview.md

| ID | Document line and rule | Class | Implementation and pin, or enforcement/search |
|---|---|---|---|
| OV01 | :9–18 Reproducibility/modularity/testing, command/owner authority and no authority transfer | P | A:34,43,52,72,80,90 bounded enforcement; broader adequacy/ownership review only |
| OV02 | :31–49 Engine results versus named methods; four shared-owner admission cases, separate measurement responsibility/custody, no hidden-cause inference | P | Review only; no new analytics owner admitted |
| OV03 | :60–180 Determinism/input/output boundary, distinct durability/support/equivalence/canonical/hash/provenance/codec/naming responsibilities and non-rebinding labels | P | Concrete domain vectors/runtime assertions above; conceptual guarantees and admission review only |
| OV04 | :180 Development status not semantic data, no dedicated Engine identifier | IP | RT:75,643; EX:61; canonical form current prefix/CAN:105 |
| OV05 | :197–248 Evolution/explicit scoped promotion/no accidental commitments/stable identity rules | P | Review only for future support admission; existing non-rebinding pins in owning docs |
| OV06 | :202 Fixed session model/interpretation | IP | RT:96,111,139; SE:80,526 |
| OV07 | :206 Storage separate build binding/refusal | IP | SC/FileArcogineStorage.java:259; BS:1121 |
| OV08 | :256 Java21 floor/compiler distinct | P | product/build.gradle.kts:38 and .github/workflows/ci.yml:90,94 mechanical build/CI enforcement; toolchain contract, not a behavioral pin |
| OV09 | :257,273 Headless, no outward/rendering layer; local-first defaults | S | Public APIs/module dependencies; hardening future policy reviewed |
| OV10 | :262 Charter implications conceptual, no current real-operation capability | P | Review only; no missing platform inferred |
| OV11 | :274 Repeated explicit inputs deterministic supported outcomes | IP | RT:521,643; FT/process/FactoryRuntimeBoundaryAcceptanceTest.java:145; EV:433 |
| OV12 | :291,501 Internal event vocabulary versus actual scheduled transitions | NI | First paragraph overstates scheduled starts/order completion; F/process/RecordingScheduler.java:111 only TaskEnd; HC:396; CONS #479 |
| OV13 | :293,329 Successful supported publication/order/no-op/fault/drain cursor semantics | IP | RT:488,521,548; EV:136,150,178,205,216,278,305 |
| OV14 | :298,313 Immutable order intent, deterministic unit jobs/aggregate completion/order KPIs and Factory stores | IP | F/orders/Order.java:13; FT/process/OrderIntentSeparationTest.java:37,60; FT/process/FactoryRuntimeBoundaryAcceptanceTest.java:89,111 |
| OV15 | :302,344 Exclusive mutation/observations/command boundary and architectural-review triggers | P | A:72,80,90 bounded mutation rules; duplicate-state/provenance/synchronization/general agent behavior review only |
| OV16 | :318,335,348,468–495,554 Finance interpretation/ledger/view/quantization/balance capability | U | product/domains/finance/src/main/java/com/arcogine/finance/process/FinanceHandler.java:40,43,48,55,72; tests :34,46,77,94,123; ledger JournalEntry.java:15/test :28; no supported composition |
| OV17 | :323 Exclusive forModel handler/scheduler and command/internal acceptance convergence | IP | RT:96,111; FT/process/FactoryRuntimeBoundaryAcceptanceTest.java:75 |
| OV18 | :325 Exact model/bounded advance/reset/definite command outcomes | IP | RT:139,156,205,575; SE:80,98,122,178,245,312,363,406,424,526 |
| OV19 | :327 Immutable ordered state/provenance/cursor, no mutable stores/internal events | IP | RT:643; F/process/RuntimeObservation.java:24; OB:40,85,112 |
| OV20 | :331 Fresh view/delta closure/full cascade/raw resource deterministic facts | IP | RT:399,643; HC:230,281,349,396,436 |
| OV21 | :335,341 Purpose-specific views/no duplicate truth or DTO reentry | P | OB:112 pins current snapshots; no outward adapter rule yet; future responsibility review only |
| OV22 | :341,458 No outward/internal Finance observation/platform Agent/capability/bus/universal references | S | Full module/type inventory; admitted principles don't demand new types |
| OV23 | :381–464 Attribution roles/recording versus actor/source provenance/private mechanisms/request/knowledge/replay/explicit authority boundaries | P | Explicit rules/no implemented attribution type; review only. No blanket research reopening |
| OV24 | :498 Scheduler time/FIFO/only TaskEnd, idle interval skipping | IP | product/simulation/src/main/java/com/arcogine/core/queue/Scheduler.java:19,34; SchedulerTest.java:17,31,82,100; HC:396 |
| OV25 | :504 Domain/module admission requires state/invariant/lifecycle need | P | Review only |
| OV26 | :514 Dependency directions and non-shipped research boundary | P | A:34,43,52,62 mechanical named rules; complete dependency graph reviewed |
| OV27 | :578 Determinism: complete explicit inputs/fixed interpretation/ownership/explicit changes/history separate support | P | Behavioral repeatability above; completeness/evolution/claims review only |
| OV28 | :625–626 FIFO/strict Java arithmetic realization | IP | Scheduler.java:19; SchedulerTest.java:82; D:401; DR:32,47; Java21 strict arithmetic |
| OV29 | :627 No concurrent simulation mutation | P | Review only; no thread-owner/concurrency guard; sequential tests cannot pin arbitrary callers |
| OV30 | :640–680 Source/run provenance/model seam/spatial refusal/canonical identity/revision/storage/comparison/evidence summary | IP | RT:100,111,643; OB:40; EX:61,69,85; CAN:143; BS:635,767,1121; GE:440; detailed owning rows govern limits |
| OV31 | :680–684 No approval/deployment/durable evidence history/outward adapters | S | Explicit bounded API/module absence |

### docs/product/concepts.md

| ID | Document line and rule | Class | Implementation and pin, or enforcement/search |
|---|---|---|---|
| PC01 | :7,36 Headless/no outward scenario/demand/experiment/agent/application surface | S | Current module/public API inventory |
| PC02 | :9 Controlled occurrence/exact artifact/build-bound persistent recovery | IP | SC/FileArcogineStorage.java:368,442,259; BS:635,767,1121 |
| PC03 | :11 Published immutable validated fingerprinted model; spatial authored not executable | IP | Factory version/publisher; CAN:143,156; EX:69 |
| PC04 | :11 Definitions development until explicit promotion | P | Review only |
| PC05 | :17–19 Resource/product/ordered operation/routing | IP | Factory records/assembler :43,53; assembler test :17; CAN:212; D:41 |
| PC06 | :20,32 Immutable accepted commercial terms/price | IP | F/orders/Order.java:13; FT/orders/OrderTest.java:16; FT/process/OrderIntentSeparationTest.java:37,60 |
| PC07 | :21–22,26 Unit children/stable ordinals/aggregate completion/KPIs over one version | IP | H:311,346; FT/process/FactoryRuntimeBoundaryAcceptanceTest.java:89,111; SE:80 |
| PC08 | :23–24 Read-only view/ordered events after authoritative change | IP | RT:521,643; OB:85,112; EV:205 |
| PC09 | :28 Explicit workload and repeated semantic outcomes | IP | RT:156; FT/process/FactoryRuntimeBoundaryAcceptanceTest.java:145 |
| PC10 | :32 Finance independently interprets completion/balanced postings | U | Same isolated Finance capability OV16, no supported runtime delivery |

### standards-alignment.md

| ID | Document line and rule | Class | Implementation and pin, or enforcement/search |
|---|---|---|---|
| SA01 | :5–7,31–69 Owner authority, exact standards attribution, admission tiers/narrow formats/compatibility principles | P | Review only; no external standards metadata verification in this audit |
| SA02 | :7,53–75,178 Canonical binary/SHA256 identity distinct from historical occurrence/serializer | IP | Canonical form :55,169; CAN:143,156,168,182; CR:75; UUID tests :12,20,28 |
| SA03 | :9,15,29,79,96 Operational/interchange/conformance/regulatory scope, no complete standards-profile claim | S | No shipped adapters/profile/conformance implementation |
| SA04 | :60,63,115,141 Supported observations/events distinct from scheduler events | IP | RT:488,521,643; OB:85; EV:88,136,150,386 |
| SA05 | :100–104 No scenario/hierarchy/general capability/B2MML profile | S | Production fields/module inventory |
| SA06 | :101–114 Configured resources/operations/orders/unit children narrow mappings | IP | Assembler :43,53; H:267,311; FT/process/IntraOrderExecutionAcceptanceTest.java:42; D:88,143; machine-state tests :21–116 |
| SA07 | :119–126 Current mapping synchronization, aliases/layout distinctions/tested profile prerequisite | P | Review only |
| SA08 | :130–138 DES/event-time/FIFO/deterministic acceptance | IP | Scheduler.java:10,26,33; SchedulerTest.java:17,31,82,100; EV:433 |
| SA09 | :138–151 Seed/explicit randomness constraint, rendering/wall-clock independence, qualified Little's Law/KPI definitions | P | No production RNG exists; rule review only; scheduler time pins narrow current behavior |
| SA10 | :157 Current throughput/lead-time/backlog/completed count/value/queue depth/time | IP | H:84,88,141,148; RT:643; OB:85 |
| SA11 | :157 Resource busy time exposed | NI | F/process/ResourceObservation.java:9 and RuntimePerformanceObservation.java:4 omit it; full product busy/utilization search; CONS #476 |
| SA12 | :157 No outward KPI surface | S | Public API/adapter inventory |
| SA13 | :157 Reusable analytics ownership open | P | Stale status, not missing capability: register :56 CONCLUDED; owning overview :31–49; CONS #476 |
| SA14 | :161–164 Precise units/population/formula/verified ISO mapping, no catalogue-wide claim | P | Review only |
| SA15 | :168–311 Future JSON/OpenAPI/B2MML/AutomationML/CloudEvents/Parquet/RAMI/AAS/FMI/later formats/protocols, no certification | S | Explicit candidates/analogies/triggers; production search finds no delivered adapter |
| SA16 | :188–210,229,237,259–311 Future mappings/version/loss/preservation/domain truth/security/agent boundary | P | Review only |
| SA17 | :317–349 Operational adapter prerequisites and compatibility evidence; no speculative interop ontology | P | Review only; no adapter exists to pin |
| SA18 | :351–397 Summary/nondecisions and Governance/Storage authority | S | Summary duplicates candidate/current scope; dependency enforcement A:34,43 belongs owning principles |

### isa-95-semantic-mapping.md

| ID | Document line and rule | Class | Implementation and pin, or enforcement/search |
|---|---|---|---|
| IS01 | :3–32,88–99 Narrow semantic mapping, not ISA model/interchange/hierarchy/rename/conformance | S | Production API/module inventory |
| IS02 | :34,51–82 Exact source attribution and mapping/alignment vocabulary | P | Review only |
| IS03 | :107–117,150–158,274–287 Configured participant/runtime machine/Idle-Busy-Offline/FIFO/concurrency, canonical capacity/setup, ordered steps/explicit eligibility | IP | Assembler :43,53; Machine.java:49,155,173,186; MachineStateTest.java:21–116; CAN:212,224; D:41,88,143 |
| IS04 | :108–120,157–160,178–189,289–304 Future classification/qualification/material/hierarchy/pool/external schedule/version semantics absent | S | Current fields and explicit deferred clauses |
| IS05 | :118–122,176,206,225–271 Immutable order/unit jobs/identity/ordinal/progression/aggregate progress | IP | Order.java:13,21; Job.java:14,26,87; order separation test :37,60; intra-order test :42; OB:85 |
| IS06 | :123–128,223 TaskEnd/supported completion/order correlation, scheduler/KPIs | IP | RT:488,521,643; EV:278,386; H:84,88,141; D:401; OB:85 |
| IS07 | :123,126,128,223 No active TaskStart/retained internal history/Engine utilization | S | Handler :69; current projections; HC:436 |
| IS08 | :124,129 Finance interprets operational completion separately | U | OV16 Finance item; no production delivery; Factory sales remain independently pinned |
| IS09 | :130,354–368,370,400 Published spatial floor/placement/footprint | IP | CAN:156,168,182; EX:69 proves publishable spatial; “Proposed” wording stale CONS #477 |
| IS10 | :130,203,206,369 Executed transfer state/consequences | NI | JobStatus.java:3 has no transfer state; assembler :36 refusal; EX:69,85 |
| IS11 | :189,302–304,336–378 Hierarchy/layout/qualification distinct, no inferred reusable ontology; scheduling consequence owns due-time meaning | P | Review only |
| IS12 | :330 Deterministic MachineId final ranking tie | IP | H:161–171; D:41 |
| IS13 | :382–400 Partial manufacturing coverage only; absent inventory/personnel/maintenance/hierarchy/enterprise integration | S | Coverage summary duplicates delivered groups; current module inventory |
| IS14 | :402–479 Clear names/aliases/no false MOM or conformance claims, checklist/semantic reconciliation | P | Review only; A enforces selected ownership constraints |
| IS15 | :485–511 Future mapping/validation adapter needs exact approximation/loss/profile/authority/test evidence | P | Review only |
| IS16 | :515–525 Complete hierarchy/serialization/ERP-MES/certification/lot genealogy excluded | S | Explicit scope and production absence |
| IS17 | :528–537 Current-state maintenance/reconciliation, no chronology-as-authority | P | Review only; existing allowance for adopted unbuilt architecture conflicts with requested new principle and needs owner-selected reconciliation |

### external-representations.md

| ID | Document line and rule | Class | Implementation and pin, or enforcement/search |
|---|---|---|---|
| ER01 | :8–11,28–36 Current model/version/fingerprint/revision/observation/event semantic boundaries | IP | CAN:143,156; CR:75; UUID tests :12,20,28; RT:488,521,643; OB:85; EV:88 |
| ER02 | :12–13,26,38–54 Owner semantics authoritative, no generic domain collapse or serializer/protocol ontology | P | A protects bounded existing dependencies; general semantic authority review only |
| ER03 | :15–21,58,66,80 No scenario/HTTP/SSE/product CLI/ordinary external projection | S | Full current API/module inventory |
| ER04 | :58–62 Future scenario distinct/referenced model selected on need | P | Review only |
| ER05 | :66–80 Future JSON/OpenAPI/generation choice, no delivered HTTP surface | S | Explicit future direction |
| ER06 | :68–72 Factory fingerprint from canonical binary SHA256 not JSON | IP | Canonical :55,169; CAN:143,156 |
| ER07 | :72,84–86 Runtime semantics authoritative; other fingerprint policies/transport preservation | P | Review only |
| ER08 | :88–99 Event run/sequence/time/type/fingerprint/conditional revision/entity/payload fields | IP | F/process/RuntimeEventEnvelope.java:36; RT:521,529; EV:88,386; current revision optional always empty |
| ER09 | :101,109–115 Future transport/export cannot discard meaning or expose internal events as supported semantics; retention/gaps explicit | P | Review only; no shipped remote/export adapter |
| ER10 | :103–105,119–123,127–169,181 Future CloudEvents/AsyncAPI/Parquet/Arrow/CSV/B2MML/AutomationML/AAS/FMI/other industrial candidates | S | Explicit design-for/later possibilities; no implemented-but-unused adapter |
| ER11 | :123,161,173–183 CSV not semantic authority, exact mapping/loss/version required, external asset identity distinct | P | Review only |
| ER12 | :187 Policy selects no physical persistence format | S | Storage owns current private representation |
| ER13 | :189 Governance meaning/Storage mechanism | P | A:34,43; general ownership review |
| ER14 | :191–204 Content/occurrence/storage identities distinct, no format redefinition | IP | CAN:143,156; CR:75; BS:767 |
| ER15 | :208,231 Future Governance formats/no universal interchange types | S | Full main-module/type search |
| ER16 | :210–227,233–264 Downstream Governance projections/compatibility evidence/narrow selection/shared infrastructure only after actual adapters | P | Review only |

## Research-register impact

These are proposed lifecycle consequences under the owner's requested principle, not register edits. The whole register was read; the following table separates implementation-bearing unbuilt destinations from concluded scope/ownership decisions. Coordinates refer to `docs/research/research-register.md` at the baseline.

| Register row / question and investigation | Current status | Architecture destination and audit consequence |
|---|---|---|
| :30 Evidence identity/applicability — governance-evidence-identity-applicability.md | CONCLUDED | Evidence §8/§9 fixed executable basis remains shallow (Gap 8), and conformance §12 audit destination is unbuilt (Gap 7). Reopen/split the implementation-bearing obligations to READY; preserve delivered source/reference/use/occurrence metadata/output contract. Open representation/consumer policies are already deferred, not automatic failures |
| :32 Accountable operational continuation — operational-execution-digital-twin-boundaries.md / operational-continuity.md | CONCLUDED | Entire positive accepted identity/continuity/divergence/attachment destination unbuilt (Gap 9): move back to research and READY until a concrete durable consumer implements/tests it |
| :36 Transfer lifecycle independence — transfer-semantics.md | CONCLUDED | Absent/no-transfer and present/refusal implemented; positive present/zero lifecycle unbuilt (Gap 1). Split/reopen only that implementation-bearing destination to READY; retain delivered admission decision |
| :22 Semantic support/maturity | CONCLUDED | Distinctions/explicit support admission are Principles, not missing promotion machinery. No status reversal justified |
| :29 Agency/decision boundary — agency-decision-boundary.md | CONCLUDED | Explicit semantic-role/no-platform decision, review-enforced. No shared actor implementation required; retain |
| :35 Factory semantic composition — factory-model-semantic-composition.md | CONCLUDED | Closed optional-record grammar and aggregate fingerprint delivered/pinned; retain despite spatial runtime refusal |
| :40 Resource semantics — factory-resource-semantics.md investigation | CONCLUDED | Keep collapsed configured participant, no implementation slice; present representation fulfills it; retain |
| :55 Execution account — simulation-execution-account.md | CONCLUDED | Runtime evidence principles expressly admit no new account/verifier/history owner; existing time/cursor/control mechanics pinned. No reversal solely because consumer custody/verifier absent |
| :56 Analytics ownership — simulation-analytics-consumer-boundary.md | CONCLUDED | Admission/ownership/no-new-owner rule implemented as architecture Principle and current field scopes; retain. CONS #476 stale status is corrected to this outcome |
| :61–63 General menu refusal/local admission/exact shared ranking — engine-evolution.md | CONCLUDED | Current dispatch behavior pinned, including retention of one-local-trigger and exact ranking. Missing ordinal fixture is narrower test obligation, not grounds for undoing the conclusion |
| :47–48 Game diagnostic/strategy evidence | CONCLUDED | Empirical/mechanical conclusions expressly admit no playable implementation and their destination is research/consumer requirements, not a claimed built architecture capability. No reversal inferred here |
| :16 Storage failure model | READY | Stronger failure/process-contention evidence already open; current documented local limits are truthful Scope |
| :17–21 Historical storage/codec boundary/backup/workload/alternate provider | CANDIDATE | No claimed stronger current implementation; statuses remain open |
| :24–28 Equivalence/exact-reference/conversion/attestation, :31 actor identity | CANDIDATE | No generic service/types admitted; preserve questions |
| :33–34 Operational boundaries/closure | CANDIDATE | Positive future Operational mechanisms move into :33 framing; :34 remains unresolved closure scope |
| :41–45 Setup/characteristics/qualification/performance/broader design | READY or CANDIDATE as recorded | Metadata-test and broader-draft/geometry gaps do not create a new conclusion; retain recorded status and question ownership |
| :57–60 Analytics evidence/adapter compatibility/cross-revision comparison/spatial evolution | CANDIDATE | No generic analytic/format/geometry implementation presumed; concrete use still required |
| Manufacturing-traceability questions at register end | CANDIDATE | Operational/ISA future mapping remains research, not a new current material domain |

No CONCLUDED register destination was identified for the whole framework/authorization/exception graph, shared Factory draft service, scenario loader, validation severity, attempt admission guarantee or Finance composition. Those are missing question/admission ownership or wording problems, not grounds to invent a concluded row. Relocation must preserve explicit investigation decisions and proving cases rather than deleting research evidence.

## Verification and limitations

### Lead reconciliation

Every NI/U gap family was rechecked against the owning document, actual production boundary and search inventory; in particular the lead read assembler/refusal, runtime metadata/envelope/emit, task completion/selection/cascade, timing and Machine reservation implementation, Finance handler/ledger and all shipped caller searches, Governance assertion/occurrence/authority/ImpactScope/Finding, ChallengeAttempt, model validation errors/decoder boundary, and current observation/performance records. Operational and external adapter absences were checked across module declarations, shipped main sources and test inventories. Full open CONS issue bodies were read; related open #488/#482 and #487/#490 bodies were read for context, with no mutation.

The following independently reread code/assertion pairs cover at least one fifth of the final IP rows in each cluster; repeated synopsis rows were checked against the same owning pair. This is bounded source review, not a claim that tests mathematically prove every universal quantifier.

| Cluster | Final IP coverage groups | Lead sample groups (minimum ceiling 20%) |
|---|---:|---|
| Engine | 39 | EN07,EN09,EN11,EN15,EN16,EN20,EN21,EN24,EN27,EN37,EN39,RC03,RC09,RC11,RC13,RC15,RC17,TA02–04; TaskEnd-only/cursor/dispatch/decomposition/derived assertions read |
| Factory/Challenge | 50 | FD02,FD12,FD16,FD30,FM02,FM04,FM05,FM14,FM18,FM21,CH03,CH09,CH13,CH15,CH18; source copies/golden/publication/refusal/score/deltas/economics assertions read |
| Governance/Storage | 47 | GC06,GC10,GC19,GC21,GE06,GE08,GE10,GE16,GE21,GE23,CR01,CR03,CR07,CR10,CR14,SS04,SS06,SS08; acceptance/rebinding/rollback/UNKNOWN/exclusion/basis/binding assertions read |
| Operational | 2 | OT15,OT25; GE:384 and GE:497 reread, preserving bounded Governance slice only |
| Standards/representations | 14 | SA02,SA04,SA06,SA08,SA10,IS09,ER08; canonical/units/events/metrics/scheduler evidence checked |
| Cross-cutting/product | 20 | OV11,OV13,OV14,OV19,OV20,OV24,PC02,PC03,PC06,PC07,PC08; immutable observation/actual completion/sequence/ledger/owner facts checked |

Corrections made during lead review: sibling revision acceptance became IU because the cited test accepts only one child; observation revision metadata does not even have an optional field; header early rejection is an affordance/rationale rather than guaranteed decoder behavior; explicit decoder-fixture coverage is IU rather than absent validation; headless public Governance/Challenge/spatial publication are reachable; future caller-owned evidence principles are not a missing central platform. Manual subagent totals were discarded and final table classes counted mechanically.

### Engine semantics §14 fixture mapping

| Fixture | Inspected proving evidence / limitation |
|---|---|
| 1 Ranking/filter/tie/cascade | D:41,68,109,328 |
| 2 Unit decomposition/envelope/pre-mutation refusal | FT/process/IntraOrderExecutionAcceptanceTest.java:42,75,89; LargeOrderDecompositionBenchmarkTest.java:53 |
| 3 Same-time scheduling | D:401; SchedulerTest.java:82 |
| 4 FIFO/one local dispatch | D:88,143; ordinal-contradicting case absent (Gap 27) |
| 5 Pre-binding waiting | SE:468; FactoryHandlerTest.java:227; D:68 |
| 6 Shared arrival/HOL/reselection/fixpoint/cascade | D:109,161,188,328 |
| 7 Distinct-resource spatial transfer | Unbuilt; timing arithmetic tests not runtime fulfillment |
| 8 Present-spatial same-resource continuation | Unbuilt; absent-spatial direct path/helper refusal insufficient |
| 9 Zero timing full event chain | Unbuilt |
| 10 Offline bound destination/no reroute | Unbuilt; machine-local survival insufficient |
| 11 Bound multi-eligible arrival waiting | Unbuilt |
| 12 Session/reset/outcome convergence | SE:98,122,178,312,363,406,424,526 |
| 13 Transfer observation/admission agreement | Unbuilt |
| 14 Taxonomy/entities/time/order determinism | EV:88,150,386,433 |
| 15 Arithmetic zero/floor/overflow | DR:47,60; SE:178 |
| 16 Saturation/exact count/value accumulation | DR:32; FactoryHandlerTest.java:153; DR:80; order-sensitive/infinity sum pin absent (Gap 25) |
| 17 Wide exact shared ranking | D:368 |
| 18 Repeated explicit input terminal/derived equality | FactoryRuntimeBoundaryAcceptanceTest.java:145; EV:433 |
| 19 Authoritative schedule/observed command time under budgets/cascades/faults | RecordingSchedulerTest.java:111,131; HC:396; BA:219,265,278,293,310,324,339,372,436 |

### Validation and inspected-surface limits

- `git fetch origin main`, immutable Git reads, `gh pr view`/`gh issue list`/`gh api user`, repository `rg`/source/test reads and physical line counts grounded the report. Final `git ls-remote` still showed baseline main and the initial handoff branch commit before report commit. A later main change would require a new audit, not silently change this baseline.
- The report's 18-document/404-row/class counts and 32 unique gap headings were mechanically counted; Markdown/Git whitespace and sole-file diff were checked before committing. Owner Git configuration matches authenticated human `alaiba` / Vasile Alaiba / vasile@alaiba.ro.
- No product tests/build/CI were executed: only a transient diagnostic Markdown report changes, no implementation PR or merge candidate exists. The tracked-workspace merge-candidate checker is intentionally inapplicable to this never-merged custody branch. This is not an implementation exact-head CI handoff.
- No requested architecture/product document was omitted. External standards editions/legal adoption metadata, real external integrations, process-kill/power-loss experiments, mutation testing, arbitrary stateful-rule executions and hypothetical adapter implementations were not inspected/proven. Their absence is not converted into fabricated test evidence.
- “No test found” and caller absence are bounded to tracked shipped source/test inventories at the exact baseline, not a universal claim about external clients. Public Governance/Storage/Challenge/model APIs count as supported reachability even without a UI/application. Finance differs because the handoff does not designate its isolated handler as a supported entry point.
- Model/vector and register consequences are proposals. No source, test, maintained documentation, research row, GitHub finding or PR was changed. Approximate reservation feature-size attribution is distinguished from exact whole-file counts.
