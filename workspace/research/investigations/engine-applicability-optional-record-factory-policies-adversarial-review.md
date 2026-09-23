# Independent adversarial review: Engine applicability to optional-record Factory policies

## Review identity and authority

Date: 2026-09-24. Repository: `alaiba/arcogine`. Risk: high.

| Coordinate | Exact value |
| --- | --- |
| Workspace branch | `workspace/research-engine-v2-applicability` |
| Reviewed report commit | `c409ecd0bb2941101907a7eb998d8d3ef6b7ce4a` |
| Reviewed report path | `workspace/research/investigations/engine-applicability-optional-record-factory-policies.md` |
| Reviewed report blob | `0a20427399aa0f598d69e10f6efdb5faede4f5cc` |
| Report-stated baseline | `7e5e6ad2f8a2707d00fff3e2a4e3db96da85852a` |
| Live-main review baseline | `5787bdd39d8268cf6ad55a7ac083fbb0744ceb04` |
| Review handoff commit | `849ba319b69e4824a326de887c76375efbc0b8bc` |
| Review path | `workspace/research/investigations/engine-applicability-optional-record-factory-policies-adversarial-review.md` |

**Disposition: ACCEPT WITH QUALIFICATIONS.** The exact report, together with this review and the carry-forward conditions below, is decision-quality evidence for the bounded Engine architecture/specification reconciliation. It is not an applicability declaration, a support release, permission to publish V2, or permission to activate spatial execution. No concrete successor identifier is allocated here. The maintained research question remains unreconciled; this review does not mark it CONCLUDED.

The report's recommendation survives: leave the fixed `engine-semantics:v1` definition intact and use a distinguishable, explicitly defined interpretation to cover both reconciled V2 forms. Its minimum new case is admission of an actual spatial-absent V2 artifact with truthful V2 provenance and no fabricated transfer semantics. The selection of one successor for the whole V2 domain is a defensible bounded choice, not proof that every present-only implementation or every future Factory policy must acquire a new Engine identity.

## Independence and anchoring control

This review was performed in a fresh isolated assistant session/run with no role in authoring the report and no responsibility for defending its conclusion. A different human researcher or model family was not established and is not claimed. This is an independent review at the fresh-session/run level required by the handoff, not the author's self-challenge.

Initial verification established the exact report header/baseline, access to its complete encoded artifact, and the live-main revision. The recommendation was not read in depth at that stage. The current repository instructions, Researcher contract, complete researching procedure, bounded Engine question, required authorities and relevant executable seams were then read. Independent constraints, four candidate families and thirteen discriminators were recorded in a local pre-report checkpoint before the report's reasoning was read. No synthesis seed framed that reconstruction. The exact report was then read through its final source-map row, using bounded reads to avoid treating display truncation as missing evidence.

This sequence is a method statement, not an assertion that two reviewers must disagree. The original recommendation and several of its qualifications independently survived the reconstruction. References below identify repository evidence, analytical inferences and acceptance qualifications separately. [S1, S2, R]

## Live-main delta: inspected, not dismissed by title

**Repository fact.** Comparing the report baseline with the review baseline gives one later commit and exactly seven changed paths. The complete change was inspected, and each material file was fetched at the exact review baseline. [D]

| Changed path | Inspected effect |
| --- | --- |
| `.github/scripts/check-transient-coordinates.py` | Adds a tracked-text check for a full SHA paired with a concrete workspace path in durable files; allows active planning/workspace custody and narrowly scoped self-fixtures. It does not validate ancestry or infer every semantic dependency. |
| `.github/scripts/check-transient-coordinates.test.py` | Exercises forbidden durable coordinates and allowed historical SHAs, generic discussion, active custody and checker fixtures. |
| `.github/workflows/ci.yml` | Adds explicit checker-test and checker steps to classification/tooling, separately from the existing tracked-workspace rejection. |
| `AGENTS.md` | Generalizes the rule that durable assets must preserve meaning independently of transient coordinates. |
| `docs/development/researching.md` | Clarifies active-custody identity and the fact that copying a coordinate does not preserve its target. |
| `docs/development/reviewing.md` | Requires human review of semantic dependencies that lack the checker's literal syntax. |
| `docs/development/testing.md` | Documents the always-required tooling steps, commands and limits of the checker. |

**Inference.** No changed path modifies Engine meaning, Factory predicates/bytes, publication/runtime implementation, or the blocked delivery plans. The later main therefore does not invalidate the report's semantic premise or supply the absent preservation proof. It does make the knowledge-transfer requirement mechanically enforceable. The report already recommends transferring meaning rather than archiving its coordinate; that recommendation survives and must be honored. Live main was rechecked during finalization and remained the review baseline. [D, R]

## Independently reconstructed contract and evidence boundary

**Repository fact.** Five questions must remain separate: whether Factory content is valid; whether an Engine definition applies to its exact policy/content; whether an implementation executes that domain; which identities actually produced a result; and what must be refused before runtime mutation. The Factory policy owns its closed grammar, presence distinctions and validation. Engine owns the result-affecting interpretation and its applicability. Whole-definition preservation includes accepted inputs, refusals, outputs, provenance meaning and referenced semantic dependencies, including unexercised rules. [S2-S5]

The strongest pro-correction text was not overlooked: Engine v1 section 1.1 expressly permits recording a previously unwritten rule when its behavior is unchanged. The issue is whether admission of this V2 absence case is already entailed, not whether correction is prohibited in principle. Conversely, partial implementation is not permission to rewrite unimplemented spatial clauses. [S3, S4]

**Repository fact.** V2 retains the production field meanings and, when spatial content is present, the five spatial fact groups, minimum-coordinate anchor and maximum-transfer predicate. Absence asserts no floor, placement, footprint or handling magnitudes. It is not partial information or a present record of zeros. V2's own publication grammar does not establish Engine applicability. [S5]

**Repository fact.** Current `FactoryModelVersion` validates and fingerprints only `FactoryModel` under V1. The assembler consumes that publication; `FactoryRuntime.forModel` has no Engine-version parameter. V2 is a separate record with a package-private validation-only production projection. The identity helper rejects unsupported versions, but that is not an implemented pre-assembly gate for arbitrary model/Engine pairs. These distinctions are explicit in the inspected identity tests. [S7, S8]

**Repository fact.** `FactoryRuntime` fixes and exposes E1. Actual observation metadata and supported event envelopes still omit the required Engine identity; `CommandResult` retains model provenance but is not an Engine selector. The runtime drains its event buffer rather than promising a durable run ledger. Governance's `EvidenceProvenance` can represent an Engine identity without demanding current execution support; the inspected in-memory evidence authority rejects rebinding but explicitly is not durable storage. These seams support the report's bounded inventory, not a claim to have audited all retained results or external consumers. Missing custody evidence cannot waive the brief's fixed-definition premise. [S6-S9]

**Historical evidence.** The old V2 definition at `1ab991b6e0d9529de35c845cbd0995bb2408af8c` requires all five spatial additions. The comparison to `2e13eb5fcf71823740453b1c80d0e792fa38a746` confirms the composition reconciliation does not change `engine-semantics-v1.md`. This corroborates the report's dependency history. It neither releases that historical Factory grammar nor makes a changed presence byte, by itself, an Engine-version trigger. [H]

## Candidate challenges and their outcomes

### 1. Genuine E1 correction for both V2 forms

**Best case for the alternative.** Production meanings are unchanged. The existing V1 path supplies nonspatial behavior. The present spatial facts still satisfy E1's transfer mathematics. A shared execution representation could retain the actual V2 fingerprint rather than republish the model as V1. Consequently, a faithful implementation is feasible without inventing a new scheduling algorithm.

**Attempted falsification.** Read E1's correction clause, production rules and spatial dependency together with the Factory absence rule as an implicit policy-parametric definition. That would make explicit applicability documentation a clarification rather than an extension.

**Outcome: no whole-definition preservation proof.** The Factory rule explains what absence means; it does not require an arbitrary Engine to execute every valid absent-record artifact. E1 does not establish the policy/content closure needed to admit this V2 case. Combining two familiar computations does not supply the missing applicability predicate. Current architecture requires Engine-owned exact-policy/content support rather than mere recognition of records. The report's conclusion therefore survives under the brief's affirmative-proof burden. [S2-S5, R]

This is not a measured before/after Java rejection of V2: the current API cannot express that request. Nor is it a universal mathematical proof that a preservation argument could never be discovered. The warranted conclusion is that both-form E1 correction is not justified by the inspected fixed definition. Qualification Q1 preserves this distinction.

### 2. Present-only E1 applicability, including a hybrid partition

**Best case for the alternative.** For complete present content, the five consumed fact groups, anchor, distance, handling rule, reservation and event obligations survive. A change in Factory framing need not change Engine interpretation. I found no numerical or state-machine counterexample that disproves that fact-level preservation.

**Remaining burden.** Fact-level agreement does not finish the exact-policy/reference and refusal proof. The present-only correction remains unproven, not disproven. It cannot by itself justify V2 absence. [S4, S5, H]

I also tested a partition beyond simply refusing absence: E1 for present V2 after a successful preservation proof, with a successor for absent V2. This is coherent in principle and could execute both forms across two explicitly supported interpretations. It is not presently an established alternative because the present-side proof is unresolved. It would still require a distinguishable interpretation for the missing absence case and explicit establishment/provenance/support behavior.

Thus the partition does not falsify the report's minimum-difference conclusion. It does prevent reading "one successor for both" as a unique logical necessity. The report already preserves the present-only caveat; Q2 carries that limitation forward and makes the hybrid consequence explicit. No new standing framework or research program is required merely because the alternative can be named.

### 3. Distinguishable complete interpretation

A successor can explicitly admit the reconciled policy and its two content cases, retain actual Factory provenance, and reuse the unchanged production and spatial rules. This supplies the missing admission rule without rebinding E1. It can also explicitly support V1; matching Factory and Engine version numbers is neither necessary nor meaningful evidence of compatibility. No automatic migration, per-concern fingerprint, transfer-only identity, wildcard future-policy support or permanent dual executor follows. [S3-S5, R]

Its feasibility alone would not establish necessity. The decisive additional premise is the failed affirmative preservation proof for both-form E1 support under the current authorities. Selecting this bounded route is justified without pretending that every hypothetical alternative has been disproven.

### 4. Refusal, representation reuse and unsafe shortcuts

Continuing V1-only execution and refusing V2 is a safe interim support state, not fulfillment of both-form V2 execution. Internally sharing production structures is a legitimate implementation technique if actual source identity and all distinctions survive; it is not a substitute for applicability authority. Republishing V2 as V1, dropping a present spatial record, or supplying default/zero geometry to absence fails the adopted Factory boundary. Calling an extra result-affecting applicability profile "metadata" also would not avoid the requirement for one identified complete interpretation. [S3-S7]

## Whole-definition preservation assessment

| Dimension | Independently established result |
| --- | --- |
| Accepted domain | Unchanged production fields do not establish admission of V2 absence. E1's correction clause still needs affirmative entailment. |
| Referenced policy/content | Present fact meanings survive; a mutable link cannot silently expand a fixed definition. Changed Factory bytes alone do not decide Engine identity. |
| Interpretation | Shared production and transfer rules are reusable. Selecting successful no-transfer execution for this V2 case still needs an owned applicability rule. |
| Outputs and events | Equal completion time does not erase transfer events, reservations, observation boundaries or extra scheduled turns. |
| Refusal | Factory invalidity, unsupported Engine identity and valid-but-inapplicable content are separate failures. Current type exclusion is not a permanent normative ban. |
| Provenance | V1/absent-V2 production equivalence does not imply fingerprint equality, the same controlled occurrence or historical reattribution. |
| Historical meaning | E1's unexercised clauses and required dependency basis survive retirement and implementation gaps. No discovered store inventory establishes universal absence of attribution. |

This assessment supports the report's central claim without importing a general same-label amendment policy or reopening the concluded Factory composition choice. [S2-S9]

## Independent proving and failure cases

The cases below were selected before substantive report reading. Unless marked as inspected implementation evidence, they are analytical contract witnesses, not new product test results. References are S3-S9.

For an independent numerical witness use one quantity-one order at time zero, two online unary machines, steps `M1:5 -> M2:7`, no contention or external commands. Present spatial content uses floor `10 x 4`, unit footprints at `(0,0)` and `(3,2)`. With `ticksPerCell=2` and `handlingTicks=3`, distance is 5 and transfer duration is 13.

| Case | Expected discriminator and review result |
| --- | --- |
| Factory V1 under E1 | Production-only execution; no invented spatial facts. The simple witness completes at 12. Inspected identity tests fix E1 and distinguish fresh run IDs; inspected dispatch tests preserve offline filtering, FIFO and recovery ordering. |
| V2 present, nonzero | Under the defined spatial rules: first step ends at 5, transfer arrives at 18, final completion is 25. Fact-level computation is preserved; these numbers do not establish E1 applicability to the revised policy. |
| V2 present, both magnitudes zero | Simple completion is 12, but distinct-resource transfer start and separately scheduled completion still occur at 5. Intermediate state and ordered transitions remain real. Absence-to-zero normalization fails. |
| V2 spatial absent | Valid production-only content; no floor/rate/overhead exists. A successor can explicitly execute to 12 without transfer events while retaining a V2 fingerprint. Equal V1 numerical output does not prove prior E1 admission. |
| Same-resource continuation | No transfer even with spatial content present. This coincident behavior is a deliberately weak witness: it cannot establish the full applicability domain. |
| Incomplete or invalid present content | Missing resource placement, duplicate/unknown placement, overlap, invalid extent or negative handling is Factory-invalid. Never repair it by dropping the record or synthesizing values. Inspected V2 validator and absence/zero tests support the boundary. |
| Factory-valid but Engine-unsupported | Refuse outside the selected definition's policy/content domain before runtime mutation. Distinguish this from invalid Factory data; the new pair gate itself remains to be implemented. |
| Unsupported Engine identity | The inspected helper and its test explicitly reject `engine-semantics:v2` today. This is not an allocated successor or a test of caller-selected runtime establishment; the current `forModel` has no such parameter. |
| Equivalent production under V1 and absent V2 | Preserve each real artifact/fingerprint. A common representation may support a stated comparison; it must not create full identity equality, controlled-revision equality or substituted provenance. |
| Future allegedly irrelevant optional content | Familiar production fields do not confer wildcard acceptance. Refuse outside the definition unless support/irrelevance is actually established there. A definition already admitting a domain may later gain implementation without a new identity. |
| Bound transfer and offline arrival | Arrival remains at its fixed time; reservation converts to the bound destination's FIFO queue, not the shared backlog. No rerouting or double-counted reservation; transit is neither active processing nor busy time. Reused rules must preserve this interaction. |
| Arithmetic and fault boundaries | Exact maximum-distance arithmetic can overflow even when a later multiplier is zero; invalid publication must still fail. A valid duration plus extreme current time is a distinct runtime condition. Preserve pre-mutation rejection versus a genuinely post-mutation Faulted command. |
| Historical E1 records and unexercised rules | Keep original interpretation and source identities; preserve the required exact definition even after execution retirement. Partial implementation, absent envelope propagation or no discovered durable run ledger cannot unfreeze E1. |

The report's independently shaped `M1:2 -> M2:3` witness was also recomputed: distance 2, rate 4 and handling 1 give duration 9, arrival 11 and completion 14; zero handling magnitudes give simple completion 5 while retaining transfer transitions. Its arithmetic is correct. Neither witness proves that zero-duration transfers preserve all competing-work traces: extra same-time turns can expose ordering and bounded-advancement differences. The report explicitly acknowledges this limitation. [R, S4]

## Findings and precise carry-forward qualifications

No load-bearing factual contradiction or established alternative satisfying both-form E1 preservation was found. Most limitations below are already present in the report; they are acceptance conditions, not manufactured defects. The review adds the explicit hybrid-partition stress test and tightens how failed proof must be expressed.

### Q1. Preserve the conditional proof boundary

**Adversarial qualification.** Reconciliation may state that the inspected fixed E1 definition does not establish both-form V2 applicability and that the affirmative correction burden was not met. It must not turn this into a theorem that different Factory fingerprints, different bytes, any newly implemented input, or any future policy necessarily require a different Engine identity.

Verification: the durable rationale distinguishes implementation coverage from defined domain and includes the reopening condition: discovery of an Engine-owned, whole-definition-preserving admission rule for this exact absence case. A new fixture that merely assumes such a rule is not historical entailment evidence. [S2-S5, R]

### Q2. Preserve present-only and partition scope

**Adversarial qualification.** The five present spatial facts and their interpretation remain strongly preserved. Full present-only policy/reference/refusal preservation is unproven here, not impossible. One successor covering both V2 forms is the selected conservative scope, not a logically unique distribution of execution support. A hybrid still needs its present-only proof and explicit support/provenance responsibilities.

Verification: the reconciliation neither claims that implementing the already-defined spatial mathematics alone forces a new identity nor silently grants E1 present-V2 applicability. It expressly declares which identity is selected for each admitted case. It does not manufacture permanent dual execution, a policy menu or a new open-ended gate just to keep the hypothetical partition alive. [S2, S4, S5, R]

### Q3. Resolve exact attribution and dependency support before promising it

**Adversarial qualification.** Leaving E1's text unchanged is insufficient if its historical meaning is subsequently resolved through an incompatible live Factory reference. The reconciliation must specify the retained E1 dependency basis and how its exact meaning remains resolvable, without resurrecting historical V2 publication bytes as current authority. The successor must identify its own applicable policy/content and preserve actual Factory fingerprints and conditional controlled-revision provenance.

Verification: the owning definition/support statements separate current interpretation, historical definition retention, decoding, execution and retirement. Reset retains the selected interpretation. No request for an unsupported old identity falls back silently to the successor; no missing historical Engine provenance is filled from CURRENT. No new Factory fingerprint is manufactured merely to change Engine interpretation. [S3-S7, S9, H]

### Q4. Do not promote research acceptance or reported tests into release evidence

**Adversarial qualification.** Current helper rejection, type separation and conformance fixtures do not implement a V2 publication path, an arbitrary model/Engine establishment gate, spatial execution, or complete observation/event provenance. The report's 103-test execution remains author-reported evidence; this review did not reproduce that run. Analytical transfer witnesses are not executable successor conformance.

Verification: later implementation proves policy/content refusal before runtime mutation, genuine absent/present-zero separation, retained nonspatial ordering and arithmetic, transfer reservation/offline-arrival interactions, selected identity on runtime and supported observations/events, and version-preserving reset. Factory golden bytes, strict decoding and invalid-content rejection remain Factory-owned work. Existing Faulted semantics must not be disguised as zero-mutation rejection. Publication and activation stay blocked until their own prerequisites and evidence are satisfied. [S5-S8, R]

### Q5. Transfer meaning; keep the composition-cost trigger bounded

**Adversarial qualification.** The current procedural delta requires durable semantic state to survive workspace retirement independently of these coordinates. Transfer the applicability decision, Q1-Q4, the absence/zero discriminator, support/dependency decisions and reopening triggers to their actual owning destinations. Do not retain this review as a substitute for architecture or treat copying its SHA/path as archival preservation.

Verification: the later knowledge-transfer audit identifies those destinations and explicit discard/retention decisions. The research is not marked CONCLUDED by this review. The optional-record Factory decision is not silently reversed; concrete successor/support cost or consumer evidence can activate its existing reopening trigger, but no measured cost or universal optimality claim has been established here. [S1, S2, D, R]

## Effect on the report and remaining work

**Review judgment.** Candidate B survives for the bounded both-form requirement. Confidence is high in rejecting an unsupported assertion of identity-preserving E1 closure on the current evidence, and in accepting the distinguishable-interpretation route subject to Q1-Q5. Confidence is deliberately lower about the outcome of a separately demonstrated present-only correction. The report does not need a new runtime prototype merely to establish that a missing admission premise is missing.

The report plus this review can inform a separate architecture/specification reconciliation now. That phase must resolve its concrete identity, exact applicability/support and retained dependency basis; it must not treat the review as the declaration itself. Tests for promised execution, publication and provenance remain required at their owning implementation boundaries. No production, canonical architecture, planning, research-register or original report file is changed by this review. No PR or merge is performed.

## Validation and limits

**Performed in this review:** exact-commit GitHub reads; report header/blob and complete-content checks; live-main and historical comparisons; inspection of every current procedural-delta path; independent reconstruction before report reasoning; source-level inspection of the implementation and named test cases; arithmetic recomputation of both witness families; local Markdown structure/whitespace and coordinate checks. The authenticated GitHub repository-owner identity was checked, and local drafting Git identity was set and verified to that same human identity before persistence.

**Not performed:** repository Java test execution, Gradle/style/coverage/security gates, V2 codec or runtime execution, successor conformance, or an external retention audit. A container clone failed because `github.com` could not be resolved; repository access succeeded through the GitHub connector. The review therefore does not claim to reproduce the report author's 103 passing tests or its disposable log. Inspected tests are source evidence, not a new passing test run. This limitation does not supply either candidate's missing semantic premise.

**External evidence:** none used. The disputed fact is the meaning and admitted domain of a repository-defined fixed contract. External versioning conventions would not establish that missing premise. No interoperability claim or universal versioning law is inferred.

The pre-report checkpoint and local validation diagnostics are disposable working material, not new normative artifacts. This file is active workspace custody and is not a merge-ready durable document.

## Source map

All S and D references below are at live-main review baseline `5787bdd39d8268cf6ad55a7ac083fbb0744ceb04`. Paths name actual files; sections/methods bound the evidence inspected. R and H have their separate exact revisions. Search hits were used to locate files, not substituted for exact-revision reads.

| Reference | Exact source and relevant boundary |
| --- | --- |
| R | The report commit/path/blob in the header; executive conclusion, input and preservation matrices, proving cases, self-challenge, qualifications, validation and source map. |
| S1 | `AGENTS.md`; `.github/agents/researcher.agent.md`; `docs/development/researching.md` (read in full); `.github/CONTRIBUTING.md`: independence, authority, custody and reconciliation. |
| S2 | `docs/research/research-register.md`; `docs/research/investigations/engine-evolution.md`, Engine applicability question; `docs/research/investigations/factory-model-semantic-composition.md`, conclusion and reopening triggers; `docs/planning/spatial-runtime-consequences.md`, remaining prerequisite; `docs/planning/factory-simulation-engine-readiness.md`, landed baseline and current queue. |
| S3 | `docs/architecture/overview.md`, semantic evolution/support, determinism and model identity; `docs/development/semantic-contract-support.md`; `docs/product/charter.md`, semantic continuity and provenance. |
| S4 | `docs/architecture/engine-semantics-v1.md`, read in full: identity/completeness and correction clause; session/dispatch/decomposition; spatial rules; results/provenance; conformance. |
| S5 | `docs/architecture/factory-design.md`, section 11.1; `docs/architecture/factory-model-v2.md`, sections 1-10 and relevant golden-vector requirements: absence, policy identity, applicability, bytes, predicates and support. |
| S6 | `docs/architecture/runtime-contract.md`, mandatory model/Engine provenance, observation/event distinction and history/retention boundaries. |
| S7 | `product/domains/factory/src/main/java/com/arcogine/factory/model/FactoryModelVersion.java`; `FactoryRuntimeAssembler.java` in the same directory; `product/domains/factory/src/main/java/com/arcogine/factory/model/v2/FactoryModelV2.java` and `FactoryModelV2Validator.java`; `product/domains/factory/src/main/java/com/arcogine/factory/process/FactoryRuntime.java`, construction/reset and command boundary; `RuntimeObservationMetadata.java`, `RuntimeEventEnvelope.java` and `CommandResult.java` in the same process directory; `product/types/src/main/java/com/arcogine/types/EngineSemanticsVersion.java`. |
| S8 | `product/types/src/test/java/com/arcogine/types/EngineSemanticsVersionTest.java`; `product/domains/factory/src/test/java/com/arcogine/factory/process/EngineSemanticsIdentityAcceptanceTest.java`; `EngineSemanticsV1DerivedResultConformanceTest.java` in that directory; initial filtering/FIFO/recovery cases in `EngineSemanticsV1DispatchConformanceTest.java`; absence/zero/production-validation cases in `product/domains/factory/src/test/java/com/arcogine/factory/model/v2/FactoryModelV2ValidatorTest.java`. Tests inspected, not executed. |
| S9 | `product/governance/src/main/java/com/arcogine/governance/evidence/EvidenceProvenance.java` and `InMemoryEvidenceReferenceAuthority.java`: explicit optional provenance, historical identity representation, rebinding refusal and non-durable scope. |
| D | Baseline comparison `7e5e6ad2f8a2707d00fff3e2a4e3db96da85852a` to `5787bdd39d8268cf6ad55a7ac083fbb0744ceb04`; complete PR #392 diff and exact-baseline reads of all seven paths listed in the delta table. |
| H | `docs/architecture/factory-model-v2.md`, section 1.1, at `1ab991b6e0d9529de35c845cbd0995bb2408af8c`; comparison to `2e13eb5fcf71823740453b1c80d0e792fa38a746`, including absence of an Engine-specification change. Historical evidence, not current Factory publication authority. |

**Final disposition: ACCEPT WITH QUALIFICATIONS (Q1-Q5).** Independent fresh isolated review session/run; no authorship or defense responsibility for the original report. Decision-quality evidence for later bounded reconciliation, not release or implementation authorization.
