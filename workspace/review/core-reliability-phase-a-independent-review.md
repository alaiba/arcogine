# Independent adversarial review: core reliability assurance Phase A

## Verdict and scope

**REWORK DESIGN REQUIRED. PHASE B AUTHORIZATION: NO.**

The inventory and most of the assurance design withstand independent challenge. The report correctly returns `INCOMPLETE`; its open prerequisites are not evidence of failed assessment work or a reliable core. Preserve that distinction. Two bounded design revisions are needed: require independent challenge of the eventual Phase D verdict, and extend the research-capture arithmetic family to the last representable cursor. Several suspected concerns can now be stated more precisely from controlled observations. None of this authorizes Phase B, production remediation, a support change, or a reliability certification.

The review treats assurance over identity, persistence, determinism and historical evidence as **high risk**. That warranted independent grounding, complementary fault/oracle analysis across all domains, and bounded falsification probes. It does not promote any architecture or settle an admitted-domain decision.

### Immutable inputs and independence

| Coordinate | Value |
| --- | --- |
| Repository | `alaiba/arcogine` |
| Review commission branch | `handoff/core-reliability-phase-a-independent-review-20261022` |
| Review commission commit | `acf1b8088d17df33651359ce3f080f7182a5643f` |
| Review commission path | `workspace/review/core-reliability-phase-a-independent-review-handoff.md` |
| Assessment branch | `codex/core-reliability-phase-a-assessment` |
| Assessment commit R | `6bc1e1ff5f11f7aff525761a027ec3b71db73f7b` |
| Report at R | `workspace/assurance/core-reliability-phase-a.md` |
| Crosswalk at R | `workspace/assurance/core-reliability-phase-a-crosswalk.json` |
| Report Git blob | `aa7f469431ccf9c5f735d305baa96a651221dd33` |
| Crosswalk Git blob | `d1015aa98b398616981eaba46c9335359b09a6d8` |
| Assessed main T | `da70c929be95b98aba773a9225ec70ba13d50c31` |
| Live main M, start and final recheck | `da70c929be95b98aba773a9225ec70ba13d50c31` |
| Original commission | `3dcdcb4bf883bcf570cba1867e49792be9cb179d`, `workspace/assurance/core-reliability-phase-a-handoff.md` |
| Review branch | `codex/core-reliability-phase-a-independent-review` |
| Review path | `workspace/review/core-reliability-phase-a-independent-review.md` |

This is an independent review execution session, with no responsibility for authoring or defending R. It is not a self-administered continuation. Independence is session-based; no claim of a different model family is made. A separate worktree/branch was created from live main. The assessment branch and artifacts were not edited. The original checkout's pre-existing `.devcontainer/devcontainer-lock.json` modification was left alone.

Before reading the assessment's matrix/recommendation in depth, I read the commission, canonical contracts and selected implementing seams, and recorded an author-agnostic first pass. Its principal failure cases were signed arithmetic and counters; partial mutation mislabeled as rejection; canonical text/framing aliases; binding omissions; shared-artifact cleanup; evidence non-rebinding and exact definitions; compound Challenge diagnostics; lost-tail/frontier/finality errors; shared occupancy helpers; and static-guard bypass. Candidate approaches were examples alone, independently derived finite models/vectors with controls, and score/quota-based testing. Only the second is a plausible comprehensive design. These were reconstructed before relying on U1–U9. The handoff itself disclosed those concerns, so this was author-agnostic grounding, not a claim of complete informational blinding.

The research and reviewing procedures informed adversarial method. This is neither a registered Research lifecycle transition nor a PR review/disposition. No research register, issue, PR, checkpoint, product/test source, architecture, or testing policy was changed.

## Evidence method, custody and inventory

Repository facts below bind to T. Because M=T there is no contract/source/test drift to reconcile. The assessment branch's live tip was independently checked as R. Both packet files and both commissions were read from exact commits, not search snippets. GitHub reads showed PR 510 still open and unmerged, now at `53f374d38febd9c86a0f56bbe13be082fed722a7`; issue 514 remained closed/completed. That changed PR head is delivery context, not landed T evidence. No branch-only assertions were credited to T.

Grounding included `AGENTS.md`, `.github/CONTRIBUTING.md`, `docs/development/testing.md`, `researching.md`, `reviewing.md`, the Researcher contract, architecture overview, Factory model/design/resource semantics, Engine semantics, runtime contract, controlled revisions, Governance conformance/evidence, Storage and external representations. Searches for reliability, coverage, invariants, canonical identity, refusal, determinism, recovery, evidence, parser and diagnostics followed semantic neighbors into the source and retained tests. No external technical premise is load-bearing; no web survey was needed to determine these repository-local contracts.

Evidence labels used here:

- **Static:** source/specification reasoning, including precise proposed mutants. Not a test pass.
- **Probe:** independently authored bounded execution, reproduced in the appendix. Not a campaign.
- **Retained:** inspected assertions already in T; their historical execution is not re-certified.
- **Inference/recommendation:** consequence or requested decision drawn from those observations.

An independent PowerShell check parsed the crosswalk and compared it with `git ls-tree -r T product`, filtering tracked `.java` files. Result: **300 actual paths, 300 crosswalk entries, zero missing paths, zero blob mismatches, zero empty contract assignments, 33 distinct contract IDs**. The nine declarations in `product/settings.gradle.kts` and their relocated project directories match the crosswalk. Module totals were independently recomputed:

| Module | Main | Test | JMH |
| --- | ---: | ---: | ---: |
| types | 11 | 9 | 0 |
| simulation | 4 | 3 | 1 |
| factory | 45 | 37 | 0 |
| governance | 53 | 12 | 0 |
| storage | 3 | 5 | 0 |
| challenge | 49 | 25 | 0 |
| challenge-factory-integration-test | 0 | 1 | 0 |
| research-experiments | 23 | 16 | 0 |
| architecture-conformance-test | 0 | 3 | 0 |
| Total | 188 | 111 | 1 |

The main count includes the research package descriptor. Nested/anonymous declarations inherit the containing file's boundaries; lexical declaration extraction is not proof of semantic coverage. Proof fixtures, separate-JVM helpers, test-only modules and the throughput-only benchmark are accounted for. Root/module Gradle configuration, source sets, coverage data aggregation and architecture scans remain material non-Java controls. The 90% LINE threshold is not semantic closure; exception handling is not JaCoCo BRANCH coverage.

I independently compiled the 188 main files with native Temurin JDK 21.0.9 using `javac --release 21` solely to run small review probes. Docker inspection failed because the Docker Desktop Linux engine pipe was unavailable. Native `javac` supports this diagnostic directly; no Gradle setup or new dependencies were needed. This is **not** `./arcogine check`, test-source compilation, Checkstyle, JUnit execution, coverage, or CI. None of those is claimed. Gradle/JUnit/JaCoCo campaign execution was deliberately not performed. Probe code/output stayed under ignored `logs/`; the complete minimal reproduction inputs needed for the findings are retained below.

## Material findings

### A1 — BLOCKING: final assurance has no explicit independent verdict challenge

**Provenance:** novel design omission. **Affected groups:** all 33, through Phase D evidence synthesis. **Authority:** this review commission, Pass 5; original campaign purpose and acceptance conditions 3–7. **Packet locations:** §5 domain-campaign endings and shared execution item 6, §7. There is no implicated production defect.

**Static evidence:** R requires a fresh exact-main baseline, repeated campaigns, explicit exclusions and remediation rechecks. Its independent-review requirement is for Phase A, and normal independent PR review applies to future implementation changes. It does not specify an independent challenge of the final Phase D assurance conclusion. These are distinct reviews. A correct Phase A plan and individually reviewed fixes do not independently verify later corpus execution, survivor triage, evidence transfer or the final coverage claim.

**Surviving failure trace:** an executor retains only successful generated cases after a resource frontier, treats the remaining consequential family as covered, and writes a final verdict over a fresh SHA. The freshness and regression rerun conditions can all hold; no specified independent final reviewer must challenge that omitted frontier or the synthesis. This is an assurance-process fault, not a Java mutant. The report's current general warning against truncation does not provide the missing independent check of whether that warning was followed.

**Minimal revision:** add a Phase D entry/exit protocol: exact final main and immutable campaign evidence; a per-family closure/blocker/residual ledger; explicit drift/reuse justification; independent execution context reviewing the verdict and evidence, including common-mode oracles and survivors; unresolved consequential findings precluding an unqualified assurance claim. Review of Phase A or code PRs cannot substitute. No new standing gate/service is needed.

**Effect:** blocks adequacy of the complete A→D design until amended; it is not a demand to execute D now. Highest revision priority because it governs the eventual claim. It independently blocks eventual D closure, even if every current domain prerequisite is resolved.

### A2 — MATERIAL: add last-representable-cursor arithmetic to R-CAPTURE

**Provenance:** novel U10, adjacent to but not identical with U3. **Affected groups:** R-CAPTURE, E-EVENTS; downstream R-DERIVE. **Authority:** `docs/architecture/runtime-contract.md`, “Basis, coverage and frontier”; `EvidenceWindow.missingWithin` Javadoc promises the missing part of `(after,through]`. **Source:** `product/research-experiments/src/main/java/com/arcogine/research/experiment/EvidenceWindow.java:77–95`, especially `runFinalSequence + 1` at line 91. `DeclaredEvidence.completeEvents` consumes this result.

**Probe:** construct a valid-shaped complete `EvidenceWindow` with start=1, end=final=`Long.MAX_VALUE`, no missing sequences, then query `(0,MAX]`. `isComplete()` is true, but `missingWithin` returns `[1,MAX]`. The addition wraps to MIN before `Math.max`. Expected from independent interval algebra: the empty set. Reproduction is in the appendix.

This is a constructed range-control witness, **not** a claim that MAX events were captured in this review. It establishes a helper-domain inconsistency before the counter itself wraps. A policy that permits sequence MAX must address its consumers; refusing only the next increment does not repair this query. A policy restricting the horizon earlier would need explicit authority and consistent range preconditions.

**Why the design misses it:** R-CAPTURE's concrete L recipe explores tiny capture scripts, gap positions and independent interval sets. Its listed mutations target false frontiers, missing capture checks and normalization. T-TIME's signed arithmetic partitions and E-EVENTS' U3 counter reasoning do not explicitly cross into cursor endpoint/successor arithmetic. A helper using signed `final+1` can pass every small gap test, while correct runtime counter handling gives false confidence in downstream evidence.

**Minimal revision:** extend R-CAPTURE's B/L/M family with 0,1,MAX−1,MAX cursors; empty and nonempty queries; inclusive/exclusive endpoint algebra; beyond-frontier ranges; and successor overflow. Use an independent interval relation without `MAX+1` sentinels. Connect its admitted domain to the U3 owner decision. Retain this as a bounded research-substrate finding, not new runtime recovery semantics.

**Effect:** must be explicit before the research-capture campaign closes; blocks use of that helper as a total extreme-cursor oracle and eventual unqualified D assurance. It does not invalidate ordinary small-window capture or independently computed finite interval checks. Add it in the Phase A revision rather than silently accepting U1–U9 as exhaustive.

### A3 — MATERIAL: promote confirmed diagnostic and catalogue observations out of “suspected” status

**Provenance:** confirmed U2 and U8, not novel faults invented by this review. **Affected groups:** F-PUBLISH, C-CATALOGUE, C-CONTENT/C-ADMISSION binding seam. **Authorities:** Factory Design §12 deterministic validation; Factory model §§2,4.4; Challenge “Identities”, “Catalogue and draft economics” and “Candidate admissibility”. **Sources:** `FactoryModelValidator.validate` line 75; `OperationStepDefinition` set copy; `EquipmentCatalogue.writeOffer` lines 76–86; `EquipmentCatalogueValidator.validate`; `CandidateAdmissibilityPolicy.assess` fingerprint check.

**Probe evidence:** eight separate JVM executions of the same invalid Factory model produced three distinct diagnostic orders. Independently, one-offer catalogues with item IDs consisting of lone code units D800 and D801 both passed catalogue validation, their IDs were unequal, and their semantic fingerprints were equal. The latter is representational loss before hashing, not a SHA collision.

The current design already proposes suitable tests for these faults; the weakness is their still-provisional evidence status and the need for bounded owner resolution. The catalogue probe proves the fingerprint cannot distinguish all currently accepted item IDs. It does **not** prove every swapped catalogue passes admission: later item-resolution/availability checks can still refuse. The public loader preserves escaped code units and imposes no separate scalar-value filter on these IDs; no parser-wide surrogate rejection is inferred.

**Minimal revision:** cite the exact observations, preserve their limits, and identify decisions explicitly: deterministic diagnostic ordering over equal eligible sets; catalogue identity text preservation or refusal at the catalogue boundary. Revalidate the revised ownership/domain statements before the relevant Phase B slices start. No production fix is part of this review.

**Effect:** known domain prerequisites remain open; this is not a new wholesale design blocker. U8 affects exact-content binding, U2 affects diagnostic reproducibility, not canonical bytes or dispatch. Priority is higher for the identity alias because it silently weakens a binding signal.

## Independent disposition of U1–U9

Categories below refer to the precise boundary, not an entire module's reliability.

| Concern | Disposition and independently checked evidence | Required consequence |
| --- | --- | --- |
| U1, signed `SimTime.minus` | **CONFIRMED CONTRADICTION** for the Javadoc's “0 if other > this”: MIN minus 1 returns MAX. Probe and static Java arithmetic agree. **CONTRACT DECISION REQUIRED** for positive mathematical differences larger than MAX and the total value-level domain. `compareTo` uses signed `Long.compare`; construction permits all longs. Engine begins at zero, published durations are positive, and submission checks addition overflow; the negative witness is not a reachable runtime-time defect. | Preserve the known zero-branch contradiction independently of the undecided upper result. Define value-level subtraction or a justified precondition; separately exercise nonnegative Engine elapsed/saturation arithmetic. Unchecked `plus` alone is not a defect claim. |
| U2, invalid eligibility diagnostics | **CONFIRMED CONTRADICTION** to deterministic equal-input diagnostic order across JVMs. `Set.copyOf` is iterated directly for unresolved IDs; the probe returns orders 1,2,3; 1,3,2; and 3,2,1. Canonical encoding sorts signed IDs separately. | A3. Do not report nondeterministic runtime scheduling or fingerprints from this evidence. Choose/document diagnostic ordering at its owner, then prove compound-invalid order. |
| U3, counters | **CONTRACT DECISION REQUIRED**, with a **confirmed static unbounded-horizon contradiction** for supported public runtime sequence. An idle configured resource can alternate offline/online at time zero; drain after each command. Each actual transition emits one event, while neither active work nor retained-event storage grows. Induction reaches the last representable sequence; the next `eventSequence++` wraps. No trillions-of-calls experiment is claimed. Direct Scheduler schedule/dequeue also permits history to grow without retaining the queue history. | Separate the public event horizon, low-level Scheduler contract and runtime-specific internal event bound. A collection-size argument alone does not bound availability events. Choose horizon/refusal/representation and post-state semantics; include A2 consumer arithmetic. Do not demand a practical MAX-operation test. |
| U4, separate reference authority | **CONTRACT DECISION REQUIRED** only for a stronger cross-authority transaction promise; **NOT A GAP** in the report's occurrence-only no-accept oracle. Static order in `InMemoryEvaluationOccurrenceAuthority.accept`: validate, canonicalize/register each reference, read clock, insert occurrence. A later conflicting reference can fail after an earlier one was recorded. H §8 fixes accepted occurrence history; it does not promise rollback of all separate reference registrations. | Observe both authorities and distinguish their guarantees. A clock failure is another pre-occurrence side-effect case, not proof of forbidden behavior. No transactional API or universal zero-side-effect rule is authorized. |
| U5, attempt deltas | **CONTRACT DECISION REQUIRED** for broad constructed records; **confirmed arithmetic behavior** there: costs MIN→MAX yield −1. `DraftEconomics` permits arbitrary triples; comparison only checks challenge/policy identity. This probe does not establish an admitted real-pipeline defect. For one exact challenge with nonnegative costs within budget and fixed nonnegative deadline/completion inputs, differences are more tightly bounded. Equal identity labels alone do not validate fabricated result/economics objects. | Define whether the comparison obligation covers arbitrary constructed records or established admitted-attempt preconditions, and exact/refusal semantics outside representability. BigInteger score subtraction is already distinct from long auxiliary deltas. Do not use arbitrary negative economics as evidence that ordinary admitted attempts overflow. |
| U6, JSON policies | **NOT A GAP** for a presumed universal duplicate-key or scalar-value rejection guarantee: no such guarantee was found. **CONTRACT DECISION REQUIRED / PLAUSIBLE, UNTESTED** for any stronger resource envelope. `parseObject` currently keeps last duplicate; escapes preserve UTF-16 code units; recursive parsing has no arbitrary-depth guarantee. Importantly, integer literals are parsed as Long or BigInteger, not through Double; fraction/exponent tokens are Double and integer schema readers reject them. | Keep current syntax/schema distinctions and characterize duplicate behavior without adopting a general interchange standard. Depth/heap caps are diagnostic frontiers. The catalogue consumer, not the legal escape grammar, owns U8. |
| U7, occupancy representation | **CONTRACT DECISION REQUIRED**, with a **confirmed executed representability failure** on supported evidence: one resource of concurrency 2, two jobs, duration MAX, complete retained trace; `ProcessingOccupancyOracle.evaluateOn` throws `ArithmeticException: long overflow`. Independent occupied and capacity totals are both 18446744073709551614. Shared `ProcessingIntervals` uses unchecked accumulation; capacity uses `multiplyExact`. The probe proves an exception, not a silently returned negative occupancy: capacity checking intercepts this witness. | Define admitted research arithmetic/refusal or exact representation. Test resource and pool totals independently with BigInteger, and validate helper behavior before using the two consumers as Engine judges. Their agreement remains correlated. |
| U8, catalogue alias | **CONFIRMED CONTRADICTION** to exact accepted-content binding: two valid unequal item IDs encode to identical UTF-8 replacement bytes and digest. Probe below. **CONTRACT DECISION REQUIRED** for refusal versus exact encoding at the owning catalogue boundary. | A3. Preserve distinction from cryptographic collision, Factory text rules, and general JSON grammar. Check the actual loader/binding/lookup consumers when implementing the eventual correction. |
| U9, execution/coverage absence | **EVIDENCE UNAVAILABLE** for freshly executed full tests and JaCoCo at T; **NOT A GAP** in an honestly scoped source-only Phase A design. My bounded compile/probes do not replace that missing baseline. Retained tests were examined selectively, not every assertion. | Phase B must acquire matching source/classfile/test-discovery/coverage provenance. No runtime pass, full Java gate, percentage or CI conclusion follows from this review. |

Novel U10 is A2. Additional hypotheses rejected or retained as limitations: ID allocator wrap is not automatically reachable in `JobStore`/`OrderStore`, which retain list entries rather than draining them; arbitrary null misuse, VM exhaustion, multi-caller runtime concurrency, hostile filesystem interference, absent production Challenge projector and cross-definition historical readers are not silently promised capabilities. Shared helper agreement, raw downcasts and static-guard incompleteness are already explicitly addressed by R; their mere possibility is not a novel finding.

## Challenge coverage across all 33 contract groups

This is a design/fault-discrimination audit, not a declaration that all listed mutants were executed. Every row below is **Static/Retained** unless linked to a probe. The “attack” is a precise plausible fault that an inadequate experiment could miss. The listed complementary oracle is what makes the proposed design discriminate it, or records the remaining gap. No high-risk contract group was wholly omitted from this review. Inspection of retained assertions is risk-proportionate and selective, not file-by-file certification.

Paths below are under `product/`: **F**=`domains/factory/src/main/java/com/arcogine/factory/`; **G**=`governance/src/main/java/com/arcogine/governance/`; **C**=`consumer/challenge/src/main/java/com/arcogine/challenge/`; **R**=`research-experiments/src/main/java/com/arcogine/research/experiment/`; **S**=`storage/src/main/java/com/arcogine/storage/`. Test names resolve to their same module's `src/test/java` in the exact crosswalk. Authorities name documents under `docs/architecture/`.

| Group; authority; source/member | Adversarial fault and discrimination result |
| --- | --- |
| T-VALUE; controlled-revisions UUID heading, factory-model §3; `types/.../ControlledRevisionId`, `ModelFingerprint` | Accept non-v4/variant or noncanonical text; drop version in equality. Independent version/variant/spelling partitions and equality/hash laws discriminate. Retained typed-ID tests are local examples, not uniqueness/cryptographic proofs. High rather than full state exploration is appropriate for immutable values. |
| T-TIME; engine-semantics §5.1; `types/.../SimTime.minus/compareTo` | Comparator subtraction and floor-after-wrap survive small positive examples. BigInteger and signed partitions address them once domain is settled; U1 confirmed. Value-level unchecked addition must be tested through its consumer guard, not presumed checked. |
| F-PUBLISH; factory-model §2, factory-design §12; F `model/validation/FactoryModelValidator.validate` | Skip one predicate or nesting copy, or vary compound diagnostics. Independent expected predicate table plus alias attacks and removed-guard controls suffice in principle. `FactoryModelImmutabilityTest` asserts actual copies; a publication test name alone does not. U2 now confirmed. |
| F-CODEC; factory-model §§3–6; F `model/FactoryModelCanonicalForm`, `FactoryModelArtifact` | Encode/decode both omit a field or share wrong signed sorting; round-trip passes. Independent byte grammar/literal vectors plus per-field mutants defeat that fault. Required cases include signed-zero/NaN, strict UTF-8, count widths, predicates and rejection of noncanonical encodings. Binding list includes separate concrete verifier class and validator/result/value types; no present missing executable dependency was established. Future binding changes still require independent constituent review. |
| F-ASSEMBLE; factory-design §§4–5, resource semantics; F `model/FactoryRuntimeAssembler.assemble` | Swap product routing or share mutable stores between sessions; replay alone can agree. Explicit authored→observed field table, two-session isolation and metadata-only execution relation discriminate. Capacity/setup changes must alter identity/configuration but not current execution. |
| F-CHANGE; factory-model list ordering, governance-conformance §5; F `change/FactoryModelSemanticComparator.compare` | ID-keyed maps silently erase list reorder or eligible/step edits. Comparator separately checks positions and operation step lists. Independent edit ledger, every canonical field and reverse add/remove cases cover this. Coarse operation-level reporting is legitimate; per-field change objects are not required. |
| E-SCHEDULE; engine-semantics §4; `simulation/.../Scheduler.schedule/nextEvent` | Enqueue then throw on past input; count/monotonic-time tests still pass. Unique payload tags and exact ordered list model plus refusal post-state catch it. Retained FIFO assertions are meaningful; T post-refusal assertion gap is not cured by unmerged PR 510. U3 remains. |
| E-DISPATCH; engine-semantics §§2,4; F `process/FactoryHandler.selectMachine/tryDispatchFromQueue/tryDispatchPendingMultiEligible` | Reorder ranking keys, drain local queue, stop at blocked shared head, retain initial machine or omit fixpoint rescan. Same makespan can hide each. Planned intermediate assignment/queue/ordered-trace oracle distinguishes them. Retained `EngineDispatchConformanceTest` explicitly separates arrival from ID/ordinal/duration and local-one from shared-fill. Exact-width ranking needs synthetic arithmetic evidence separate from public reachability. |
| E-WORK; engine-semantics §3; F `process/FactoryHandler.submitOrder`, `orders/OrderExecution.completeChild` | Suppress/repeat child completion or multiply sales per child; quantity-only terminal checks miss attribution. Per-job causality, ordinal release and parent transition ledger plus boundary 100000/100001 refusal post-state cover this. Large-work test is not a performance SLA. |
| E-CONTROL; engine-semantics §1.2; F `process/FactoryRuntime.submitWorkload/setMachineAvailability/advanceUntil/reset` | Apply command then report Rejected, cascade on no-op, or advance time to target without event. Joint results/state/effect/stream assertions and real late-overflow traces discriminate. Retained session tests exercise actual arithmetic faults; fault prefix must be compared, not just caught. Null/VM failures do not extend definite-result semantics by implication. |
| E-ARITH; engine-semantics §§5.1–5.2; F `process/FactoryHandler.avgLeadTime/throughput` and completion accumulation | Wrap lead-time sum or reorder floating sales sum; replay can repeat the same wrong result. BigInteger saturation and literal bit-pattern/order witnesses are independent. Retained derived-result tests include nonassociativity and 2^53 rounding distinctions. Exact real arithmetic is not the binary64 sales oracle. |
| E-EVENTS; runtime-contract envelope/sequence headings; F `process/FactoryRuntime.emit/recordSupportedEventsFor` | Drop order-complete on a fault, emit nonexistent placement, consume cursor on no-op or reset cursor on drain. Independent transition ledger and reducer versus observations plus fault/success paired traces cover these. Reducer must not reuse runtime projection helpers; public sequence horizon remains unresolved U3/A2. |
| E-OBSERVE; runtime-contract observation, basis/finality headings; F `process/FactoryRuntime.observe` | Mutable snapshots or facts from different sequence boundaries can agree at terminal state. Copy attacks and per-boundary event/state comparison cover this. A final empty queue or QUIESCENT alone is not interval closure. Retained headless closure tests are stronger than repeated terminal equality. |
| G-REVISION; controlled-revisions identity/lineage/provenance/acceptance; G `ControlledRevision`, `SemanticArtifact`, authority port | Reuse F1 occurrence after F2 or trust candidate timestamp. Independent append-only record graph, exact reopened provenance/bytes and alias tests distinguish. Persistence acceptance, not constructor success, establishes historical authority. |
| G-CHANGE; governance-conformance §§5–6; G `change/ChangeSetFactory.fromCandidateSnapshot`, `ImpactScope` | Trust forged fingerprint, omit impacted entity or concatenate delimiter-bearing tuple keys. Real historical artifacts plus independent tuple/edit ledger discriminate. Calling the same scope helper twice is not independent evidence. Retained downstream `ChangeSetFactoryTest` crosses actual Factory/Storage seam. |
| G-DEFINITION; governance-conformance §7, governance-evidence §9; G requirement/assertion/catalogue values | Equality hides changed wording/rule under same id/version; falsely conclude historical definitions are identical. R explicitly distinguishes equality from exact retained definition objects and mutable executable closures. Copy/source-field truth tables are adequate for current values; purity/archive claims are excluded. |
| G-CONFORMANCE; governance-evidence §6; G `conformance/ConformanceEvaluator.evaluate`, `EvidenceConformanceEvaluator.evaluate` | UNKNOWN→PASS, missing evidence→NOT_APPLICABLE or whole-scope findings. Independent scope×basis×binding×outcome table and exact attribution distinguish. Evidence judgement is consumer supplied; framework checks cannot prove external truth. Qualification: subject/assertion/revision checks precede scope shortcut; `validateUses` in the evidence evaluator runs after it. Do not generalize R's “bindings before shortcut” sentence to every evidence-use check or manufacture a stronger unconditional-refusal contract. |
| G-EVIDENCE; governance-evidence §§1–7,10–12; G `evidence/InMemoryEvidenceReferenceAuthority.record`, `EvidenceUse` | Treat redelivery as corroboration, permit same-key provenance replacement, transfer applicability through equal fingerprints. Independent reference/use ledger and correction history address them. Retained tests check canonical redelivery and prior history; fixture provenance is not authenticity evidence. No universal freshness/conflict algorithm is silently added. |
| G-OCCURRENCE; governance-evidence §§8–9; G `conformance/InMemoryEvaluationOccurrenceAuthority.accept` | Late conflicting reference after earlier registration, or equal-ID definition substitution. Snapshot both authorities, exact definition objects and accepted occurrence basis. R correctly separates occurrence-only atomicity from unpromised cross-authority rollback. U4 is not evidence for making a transaction abstraction mandatory. |
| S-OPEN; storage Initial supported scope; S `FileArcogineStorage` initialization/marker validation | Adopt foreign/incomplete roots, recreate missing metadata or substitute text. Stage fault injection and full tree bytes before/after refusal discriminate. Strict text-binding checks happen before root mutation. Interrupted initialization is allowed to remain refused; automatic repair is not expected. |
| S-ACCEPT; storage commit boundary, controlled-revisions acceptance; S `FileArcogineStorage.acceptLocked` | Duplicate overwrite, fake artifact binding, candidate time, merge same-content occurrences. Independent accepted-record ledger, literal verified artifact and exact fields/reopen checks discriminate. Real Factory verifier is an integration seam; independent codec vectors must validate it first. |
| S-RESOLVE; storage resolution paragraphs; S `findById/resolveLocked/revisionsLocked` | Mask corruption as absence, return current model, silently repair or enumerate nondeterministically. Private-record corruption corpus, exact error codes/bytes and unchanged manifest address this. `findById` returns empty for absent path, not missing artifact during resolve; keep operations' promises distinct. |
| S-FAILURE; storage Concurrency/failure bounds; S atomic install/cleanup and lock paths | Install record then throw; cleanup removes an artifact still referenced by accepted history. Exception-only tests miss it. Accepted-ID query, shared-artifact stage matrix, reopen and latch-controlled conflicting threads discriminate. Retained Storage assertions target commit ambiguity/shared preservation, not power-loss durability. |
| C-DEFINITION; challenge Identities/Definition; C definition values/validator | Skip invalid field or copy; equal version labels hide distinct policy identity. Exact independent diagnostic table and nested input-copy tests address this. Invalid constructed drafts remain allowed; validation is separate. Immutable values justify thinner examination than stateful authorities. |
| C-CATALOGUE; challenge catalogue/identity; C `catalogue/EquipmentCatalogue.semanticFingerprint/validate/findByItemId` | Omit cost/limit/text distinction or trust first duplicate. Independent offer ledger, valid-offer permutations, duplicate validation and U8 probe discriminate. Fingerprint agreement with itself is insufficient. Catalogue identity is separately bound; no Factory grammar is imposed. |
| C-ECONOMICS; challenge catalogue/economics; C `economics/DraftEconomicsCalculator.calculate` | Encounter-order unknown-item versus overflow failure, unchecked sum/subtraction. Independent BigInteger result-or-refusal plus mixed unknown/overflow permutations address both numeric value and failure precedence. Expected remaining budget cannot be derived only from implementation cost. |
| C-ADMISSION; challenge Candidate admissibility; C `admissibility/CandidateAdmissibilityPolicy.assess` | Early return hides duplicate+overlap+cost violations, bounds changed to inclusive upper edge, wrong catalogue binding. Independent grid/count/cost truth tables and compound permutations cover them. Per-field rejection Boolean alone does not. U8 can weaken the binding check without proving all subsequent checks pass. |
| C-EVALUATION; challenge Evaluation; C `evaluation/ReferenceChallengeEvaluationPolicy.evaluate` | Strict deadline/budget rather than inclusive, missing one-point bonus, narrowed score, swapped provenance. Independently computed literals/truth table and MAX cases distinguish. Javadoc exact score uses BigInteger; nonnegative input guards bound component subtractions. External production facts' truth is supplied, not computed here. |
| C-ATTEMPT; challenge Attempts/comparison; C `comparison/ChallengeAttemptComparator.compare/buildComparison` | Reverse delta/winner or omit one incompatibility. Exact independent differences, swap/self/absence relations and all mismatch reasons cover them once U5 domain is settled. Production-policy-built test fixtures cannot be the sole expected-result oracle. |
| C-CONTENT; challenge Content loading; C `content/Json`, `ChallengeContentLoader` | Broaden hex/whitespace/number grammar, narrow integer before range check, bypass unsupported policy or catalogue binding. Independent grammar classes plus exact public structured issues cover this. Retained escape fix asserts all positions and legal UTF-16 code units. BigInteger integer literals and Double schema rejection were verified; no new precision defect is alleged. |
| R-CAPTURE; runtime-contract basis/frontier, testing category 10; R `ExperimentRunner`, `DeclaredEvidence`, `EvidenceWindow` | Lost tail appears contiguous, forged proof frontier, undeclared reads or semantic replay normalization. Independent interval ledger and actual closing observation are suitable. New A2 range arithmetic survives small-script-only exploration. Correct capture does not establish truthful controller promises or complete command history from events alone. |
| R-DERIVE; named `ResearchDefinition`s, runtime responsibilities; R `ProcessingIntervals`, occupancy/pool oracles and other derivations | Shared wrong intervals, endpoint rule, ambiguous step mapping, capacity overflow or wrong connectivity. Independent hand-integrated/BigInteger tables and graph closure cover these once U7 resolved. Probe confirms extreme arithmetic exception. Treat shared helper as one oracle, not corroboration; occupancy does not prove a bottleneck. Supporting exposure warrants selected per-oracle contracts, while common-mode use makes occupancy critical. |
| X-BOUNDARY; overview ownership/dependency/determinism, testing categories 10–11; architecture/package boundary tests, Gradle graph | Disable a prohibition or put a mutator/ambient dependency outside scanned source set. Deliberately violating and allowed fixtures, mutator inventory and source-set inspection discriminate. Current rules enumerate lifecycle mutations and prohibit clocks/concurrency for Factory/core. Reflection or arbitrary environment reads are acknowledged limits; no blanket static proof is claimed. Factory publication versus Challenge admission is two independent validity axes, not a shipped projector. |

Cross-boundary checks traced typed values→scheduler/runtime; model→validation→canonical decode/verifier→Storage→revision; published routing→assignment→runtime events/observations→research; historical comparison→ChangeSet→scope→evaluation; reference→use→occurrence; JSON→catalogue/admission/evaluation/comparison; and architecture/module boundaries. No missing implemented subsystem requiring a 34th group was established. Physical file completeness still does not prove that every possible semantic fault has been enumerated.

## Oracle feasibility, exploration and campaign audit

### Independent expected results and reduction rules

The proposed minimal tools are operationally credible: JUnit loops, specification-derived byte encoder, sorted-list Scheduler, tuple-set/interval-set algebra, BigInteger arithmetic and small explicit transition systems fit Java 21 and existing source sets. Optional PIT, fuzzers and formal model checkers have not been installed, compiled or timed. Their feasibility remains unknown. A large copied production algorithm would defeat independence even if placed in a test source set.

| Technique | Independent relation / expected result | Required controls and limits |
| --- | --- | --- |
| Boundary/enumeration | Exact guard truth tables; mathematical integer result or defined refusal; literal binary64 expected bits | Illegal combinations separated from admitted domains. Shrink to adjacent boundary while preserving the interacting predicate and expected diagnostic class. U1/U5/U7 decisions precede total-oracle claims. |
| Grammar/differential | F §4 independently framed bytes, signed order and canonical F64; JSON ASCII token partitions plus schema rules | Encoder/decoder agreement alone misses shared errors. Goldens must be checked independently before generated use. Minimize tree/field then bytes without repairing the malformed class. Parser comparisons restricted to an agreed semantic intersection. |
| State/model | Independent list queue and explicit Engine transition/authority ledgers; compare every observable boundary | Preserve scheduler insertion ordinal, signed ID order, local/shared arrival order, routing position, active placements, clock/cursor, fault and command outcomes, held/discarded evidence and reset epoch. Equal terminal occupancy/makespan is not state equivalence. |
| Metamorphic | Eligible-set order, valid catalogue/occurrence permutations, repeated script with RunId normalization; split advancement only with identical effective command times | No top-level Factory list permutation, arbitrary ID renaming or capacity-improves-makespan law. Paired shrinking must preserve both preconditions and relation. Configuration-only metadata relation must allow expected fingerprint/configuration differences. |
| Fault sensitivity | One semantic mutation changes independently predicted value/refusal/post-state | Baseline nonzero discovery, successful compilation, same selection/environment, identifiable failed behavioral assertion. Setup-only failures, compile errors, timeouts and resource caps are not semantic kills. Equivalent/unreachable survivors need explicit reasoning. |
| Injection/static | Independent commit-point ledger and tree manifest; allowed/forbidden architecture fixtures | Before/after-install ambiguity and shared artifacts are mandatory. Latches/join bounds replace sleeps. Unsupported FS capability and hostile races cannot become product failures or new durability claims. |

The bounded Engine dimensions in R (1–3 resources/children/steps, capacity 1–2, short durations plus symbolic extremes) are plausible starting bounds, not a measured execution budget. Higher-order overlap, offline/online recovery, same-time completions, command budgets and faults cannot be pruned by pairwise coverage claims. Before enabling visited-state deduplication, the executor must record its full key and demonstrate that states differing in the listed ordering/identity/evidence facts remain distinct or are proven equivalent. A key using only sets of pending jobs would erase the local/shared FIFO mutants. R's explicit no-ID-renaming/no-false-permutation constraints already oppose that reduction; this is an execution prerequisite, not an additional established defect. Exact explored/pruned/frontier counts and successful distinguishing controls must accompany results. No universal state-space claim is available from a finite run.

The declared `-PresearchExperimentSources` facility is for supported-only research experiments; package-local parser/Storage diagnostic tests need appropriate test-package/classpath treatment, not a new production API or an illicit research dependency. The direct javac probes here validate narrow semantics only and do not prove the proposed future Gradle test discovery path. Every future slice must verify discovery and source-set membership itself.

### Campaigns, dependencies and exit meaning

| Campaign | Dependency and end-to-end audit | Closure and remaining work |
| --- | --- | --- |
| Values/publication/identity/change | Independent primitive/byte oracles first. Actual historical comparison later consumes verified Storage artifacts; this does not prevent independent codec work or create a campaign cycle. Covers all six T/F families and the G-CHANGE consumer. | Per predicate/field/encoding/edit family, credible oracle + distinguishing controls + executed cases + triaged survivors/discrepancies + explicit residual frontier. U1/U2 resolution; binding and session-isolation checks. No one-golden-vector exit. |
| Scheduler/dispatch/control/runtime | Uses published independently checked fixtures and time semantics. Graph must include local-one/shared-fixpoint, equal-time/final-child/fault interactions, order arithmetic, result/state/event coherence and drain/reset. | U3 and consumer cursor ranges explicit; every numbered Engine §6 obligation mapped. Re-run changed family and adjacent consumer/evidence oracle after C, not only a minimized new unit test. Performance ownership does not invent an SLA. |
| Governance/Storage | Independent tuples/records can start separately; real Factory verifier/semantic comparison closes historical attribution. Scope, evidence role, reference, exact definition and occurrence are separate checks. Initialization/acceptance/read/refusal stages and shared cleanup include reopen and supported threads. | U4 guarantee boundary retained. Exact manifests/ID queries/provenance and error codes, not exception-only evidence. Stronger archive, independent-process contention and crash/power-loss guarantees stay excluded. |
| Challenge/research/architecture | Pure Challenge cases independent of Engine. Research measurement truth needs an independently checked Engine trace and separately tested intervals/pool graph. Architecture sees all actual source sets; Factory/admission axes remain separate. | U5–U8 plus A2 and declared-input/finality sufficiency. Record exact JSON/code units, policy results, capture ledger and guard controls. Research helpers must be validated before they judge Engine. No nonexistent gameplay pipeline can be an exit condition. |

All four campaigns specify useful native focused test seams, seeds plus primary input bytes, minimization, temporary-tool custody, domain-owned regression retention and bounded implementation slices. R properly requires family closure rather than a pilot count, score, coverage percentage, mutation threshold or fuzz quota. Actual runtime estimates remain **unknown**, as does installation/compatibility of optional tools; small-loop alternatives are credible but not measured campaign completion promises.

Phase C's repeat-failure/family/neighbor-consumer/independent-oracle-control loop is adequate in design. Product/spec fixes still require ordinary implementation ownership and independent PR review. Diagnostic tooling does not become a permanent gate by default. Phase D's fresh exact main and repeated baseline evidence requirements are sound but incomplete without A1. Earlier branch-only passes, stale classfiles, missing test discovery or changed generating/oracle semantics cannot silently transfer. Preserve all current exclusions and residuals; no support, architecture, product identity or release-readiness commitment follows.

## Controlled reproduction appendix

These probes used unmodified T main sources in an isolated native Java classpath. They are intentionally small discriminating observations. They did not run Phase B. Exact preparation from a clean T checkout in PowerShell:

```powershell
New-Item -ItemType Directory -Force logs/probe-classes | Out-Null
git ls-tree -r --name-only da70c929be95b98aba773a9225ec70ba13d50c31 product |
  Where-Object { $_ -match '/src/main/.*\.java$' } |
  Set-Content logs/main-sources.txt
javac --release 21 -d logs/probe-classes '@logs/main-sources.txt'
```

Save the following literal source as `logs/ReviewProbe.java` (ASCII escapes intentionally preserve code units). Compile/run with:

```powershell
javac --release 21 -cp logs/probe-classes -d logs/probe-classes logs/ReviewProbe.java
java -cp logs/probe-classes ReviewProbe
1..8 | ForEach-Object { java -cp logs/probe-classes ReviewProbe | Select-String '^U2=' }
```

```java
import com.arcogine.types.*;
import com.arcogine.factory.model.*;
import com.arcogine.factory.model.validation.*;
import com.arcogine.challenge.*;
import com.arcogine.challenge.catalogue.*;
import com.arcogine.challenge.admissibility.*;
import com.arcogine.challenge.attempt.*;
import com.arcogine.challenge.economics.*;
import com.arcogine.challenge.evaluation.*;
import com.arcogine.challenge.comparison.*;
import com.arcogine.research.experiment.*;
import java.util.*;
import java.math.*;
public class ReviewProbe {
 public static void main(String[] args) {
  System.out.println("U1=" + SimTime.of(Long.MIN_VALUE).minus(SimTime.of(1)));
  var a = new EquipmentCatalogue(List.of(EquipmentOffer.of(new EquipmentCatalogueItemId("\uD800"), 1)));
  var b = new EquipmentCatalogue(List.of(EquipmentOffer.of(new EquipmentCatalogueItemId("\uD801"), 1)));
  System.out.println("U8 valid=" + EquipmentCatalogueValidator.validate(a).isValid() + "," + EquipmentCatalogueValidator.validate(b).isValid() + " unequalIds=" + !a.offers().getFirst().itemId().equals(b.offers().getFirst().itemId()) + " equalFingerprint=" + a.semanticFingerprint().equals(b.semanticFingerprint()));
  var invalid = new FactoryModel(List.of(), List.of(new OperationDefinition(1,"op",List.of(new OperationStepDefinition(1,"step",Set.of(new MachineId(1),new MachineId(2),new MachineId(3)),1)))),List.of());
  System.out.println("U2=" + FactoryModelValidator.validate(invalid).errors());
  var p = new ProductId(1); var m = new MachineId(1);
  var model = new FactoryModel(List.of(new ConfiguredResource(m,"m",2,null,0)),List.of(new OperationDefinition(1,"op",List.of(new OperationStepDefinition(1,"s",Set.of(m),Long.MAX_VALUE)))),List.of(new ProductDefinition(p,"p",1)));
  var fixture = new ExperimentFixture("review-extreme", model,List.of(ExperimentStep.submit(p,2,1),ExperimentStep.advanceToQuiescence(Long.MAX_VALUE),ExperimentStep.captureEvents("all")),ExperimentFixture.WindowIntent.COMPLETE_RUN,List.of());
  var evidence = ExperimentRunner.run(fixture);
  try { System.out.println("U7=" + new ProcessingOccupancyOracle("closing").evaluateOn(evidence)); }
  catch (Exception ex) { System.out.println("U7=" + ex.getClass().getSimpleName() + ":" + ex.getMessage()); }
  var id = new ChallengeIdentity("same","1"); var policy = new EvaluationPolicyIdentity("p","1");
  var prov = new EvaluationProvenance("model","run");
  var r1 = new ChallengeEvaluationResult(id,policy,prov,true,List.of(),0L,0,BigInteger.ONE);
  var r2 = new ChallengeEvaluationResult(id,policy,prov,true,List.of(),0L,0,BigInteger.ONE);
  var x = ChallengeAttempt.record(new CandidateDraftSnapshot(List.of()),new DraftEconomics(0,Long.MIN_VALUE,0),r1);
  var y = ChallengeAttempt.record(new CandidateDraftSnapshot(List.of()),new DraftEconomics(0,Long.MAX_VALUE,0),r2);
  System.out.println("U5=" + ChallengeAttemptComparator.compare(x,y));
 }
}
```

Observed: U1=`9223372036854775807`; U8=`valid=true,true unequalIds=true equalFingerprint=true`; U7=`ArithmeticException:long overflow`; U5 compatibility true, construction-cost delta −1, score delta 0 and winner TIE. Random attempt IDs are incidental and not expected to match between runs. U2 outputs varied across the eight JVMs as reported above. The expected U1 lower branch is zero; exact U5 mathematical difference is 18446744073709551615, outside long. U5's inputs intentionally expose constructed-record scope, not established admission.

For A2, save as `logs/RangeProbe.java`, compile/run with the same classpath:

```java
import java.util.*;
import com.arcogine.research.experiment.*;
import com.arcogine.types.*;
public class RangeProbe {
 public static void main(String[] args) {
  var w = new EvidenceWindow(RunId.create(),ExperimentFixture.WindowIntent.COMPLETE_RUN,1,Long.MAX_VALUE,Long.MAX_VALUE,List.of(),List.of());
  System.out.println("complete=" + w.isComplete() + "; missing(0,MAX]=" + w.missingWithin(0,Long.MAX_VALUE));
 }
}
```

Observed: `complete=true; missing(0,MAX]=[SequenceRange[from=1, through=9223372036854775807]]`. Independent expected missing list is empty. This record does not allocate or fabricate MAX event objects; it is a boundary calculation control only.

Inventory reproduction: parse crosswalk JSON at R; use a path→blob map from `git ls-tree -r T product`; require equal path counts, no duplicate crosswalk paths, every listed blob equal, nonempty contract lists, and no actual Java path absent from the crosswalk. Group `files` by `module,sourceSet` and compare the table above; compare contract union with the 33 matrix IDs. This verifies custody, not semantic correctness.

## Requested revision and next owner action

Ask the assessment owner to revise the packet separately, preserving R: add A1's independent Phase D challenge, add U10/A2 to research-capture numeric coverage and its U3 dependency, update confirmed U1/U2/U7/U8 observations and the constructed-domain limits of U5, and narrow the G-CONFORMANCE pre-shortcut wording as described in the coverage table. Keep the `INCOMPLETE` distinction and all current unsupported boundaries.

Then resolve the smallest admitted-domain/ordering/text/representability decisions at their owners and independently review the revised packet before commissioning Phase B. The amount of implementation and campaign execution work is unmeasured. These revisions are bounded; they do not require replacing the inventory, a new framework, another generic repository sweep or a permanent dashboard.

**Examination limits:** no full test/coverage/CI run; no exhaustive audit of every retained assertion; no executed mutation campaign, full finite Engine model, fuzz campaign, Storage failure/concurrency campaign or independent golden regeneration; no claim about unsupported operating systems/filesystems, external evidence truth or arbitrary user-rule purity. These are explicit execution limits, not hidden passes or missing input artifacts. All 33 contract groups and consequential seams received a design-level challenge; optional tool performance remains unmeasured.

Final source recheck retained the exact R packet blobs and M=T. Human Git identity matched authenticated owner `alaiba`, Vasile Alaiba, `vasile@alaiba.ro`. This branch is a transient review artifact, not a merge candidate; no workspace merge-gate pass is expected. No PR was opened, no branch deleted, no merge performed, and Phase B was not begun.
