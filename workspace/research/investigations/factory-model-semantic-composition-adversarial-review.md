# Factory Model Semantic Composition — Independent Adversarial Review

## Review identity and method

- Reviewed workspace: `research/factory-model-semantic-composition`.
- Exact reviewed report: `885647023f8945d123cd0c74a8cdff2fb035742e:workspace/research/investigations/factory-model-semantic-composition-report.md`.
- Report-stated baseline and live-main review baseline: `46389bc5d21e0c95ffa9366ce271f9d943d44b35`.
- Review date: 2026-09-18.
- Risk: high (identity, deterministic encoding, historical interpretation, and cross-domain authority).
- Independence: achieved through a fresh isolated session/run with no authorship of or responsibility for defending the report. A different model family is not established. The task prompt itself disclosed the recommendation and requested attacks; this unavoidable advance exposure is disclosed. The reconstruction below was written from the maintained brief and repository authorities before deep reading of the report.
- Input integrity: the complete pinned report Git blob is readable (66,650 bytes); its headings, baseline, source section and closing evidence-coordinate section were checked before substantive review. Branch tip is not substituted for that revision.

## Pre-report reconstruction

This section was written before deeply reading the report. It records independent hypotheses, not conclusions about the report.

**Repository fact.** ADR-0003 separates immutable authored production semantics from scenario inputs, mutable execution, and consumer drafts. ADR-0004 separates semantic identity from historical occurrence. Accepted ADR-0006 fixes V1 field membership and byte semantics; Accepted ADR-0014 already preserves fully supported non-spatial execution and forbids synthesized spatial defaults. Accepted ADR-0015 owns result-affecting interpretation in Engine. Factory Resource Semantics expressly admits qualification, hierarchy and reusable technical contracts only when a consequential need proves them. Factory Design architecture has a Proposed header; its broader examples are not all accepted capability.

**Repository fact.** Current FactoryModel has resources, operations and products. Empty collections are allowed; represented operations require steps and explicit eligible resources. V2 adds compulsory floor/handling fields and wraps every productive resource in placement/footprint. Its validator delegates production checks and adds containment, non-overlap and checked maximum-transfer arithmetic. V2 publication/canonical identity/runtime are unimplemented and the remaining spatial plan is on research hold. The current comparator understands only V1; generic Governance orchestration first verifies artifacts and delegates domain comparison.

**Inference: constraints.** A truthful model must identify all authored semantics, distinguish omitted representation from a represented zero, retain exact historical meaning, and make execution applicability explicit. Neither the Charter's one-model rule nor one controlled-revision fingerprint proves that every future technical sub-artifact must lack identity. Similarly, independent semantic dimensions do not prove independently versioned contracts.

**Candidates reconstructed.** (1) Keep the two supported complete policies until an actual third requirement discriminates. (2) Define a complete policy whose grammar contains a closed set of optional typed records, and evolve the whole policy when that grammar changes. (3) Use typed concern composition under an aggregate identity with explicitly specified dependencies and applicability. (4) Admit independently governed/referenceable technical components only when cross-design reuse or independent historical resolution needs identity. (5) Use supported profiles as validation/consumer promises atop either complete-policy or compositional representation. Options 2 and 3 may be the same structure with different evolution rhetoric; representation and versioning are separate axes.

**Strongest linear-policy defense.** For the two actual sets, V1/V2 offer simple fail-closed decoding, fixed historical semantics, no compatibility negotiation, and truthful no-lift behavior. A future whole policy with optional spatial and storage records can represent all four presence combinations without four policy versions. New whole-policy identity need not assert that every field's meaning changed. Any claimed impossibility/combinatorial proof must distinguish mandatory cumulative bundles from complete versioned grammars that contain optional fields.

**Strongest composition case.** A logical finite buffer can constrain flow independently of coordinates; a spatial layout can exist without finite storage. Conflating these would force invented facts. Authored topology and qualification relations can refer to the same configured resource while retaining separate meaning. Typed representation can make those omissions and dependencies explicit, but does not erase cross-concern validation or Engine interaction complexity.

**Independent-component/profile cases.** An approved technical specification shared by two factories can need identity beyond both aggregates; flattening values loses an intentional dependency. Conversely, an imported CAD document's original identifier can be provenance without being a second canonical executable source. A safety or interchange profile can be a real promise about completeness and accepted behavior, not merely a UI preset; whether that promise belongs to Factory, Engine, Governance or an adapter depends on its owner.

**Adversarial cases and discriminators.** Test logical storage without layout; geometry without handling; a non-spatial transport participant; qualification-derived performance conflicting with mandatory step duration; topology/geometry dependency cycles; aggregate evolution that retains a shared sub-contract; absent versus unknown/inapplicable/inherited/explicitly empty; an older verifier receiving a new tag; a known concern with an unknown internal revision; a known concern interaction unsupported by an Engine; addition/removal of a concern in historical comparison; and a consumer that presents partial semantics as complete operational truth. Discriminate by explicit valid/invalid artifacts and interpretation promises, not by counting fields or repeating an ontology analogy. Encoding determinism alone cannot prove semantic compatibility or universal extensibility.

**Evidence needed.** Current implementation/tests establish the two supported shapes and boundaries. Future cases may establish representational feasibility by construction, but cannot establish deployed demand or settled future domain contracts. Compare a closed optional-record whole policy against composition on the same artifacts, failure rules and evolution sequence. Inspect imported technical-contract identity and Engine prerequisites before treating extra identity as either required or forbidden.

## Result and effect on the report

**Disposition: REOPEN.** This binds only to the exact report revision identified above.

Two load-bearing claims are falsified: whole-policy evolution does not necessarily force cumulative mandatory concerns, and an unchanged digest/prefix does not preserve a `ModelFingerprint` when its policy version changes. A viable hybrid — optional authored records inside an explicitly versioned complete policy — materially changes the comparison. The report treats that hybrid as either impossible Candidate A or Candidate B with a failed invariant, instead of evaluating it on its own merits. Its high-confidence ranking and prescribed narrowing of ADR-0006/0014 are therefore not decision-quality evidence.

This does **not** establish that typed concern composition is wrong or that the currently specified mandatory V2 shape should ship unchanged. Aggregate identity, explicit absence, truthful applicability, and reuse of existing validation are promising surviving ideas. The reopened investigation must separate representation, equality, grammar evolution, support promises, and consumer applicability before choosing among them. No architecture, production code, planning hold, or maintained research lifecycle is changed by this review.

## Evidence and temporal scope

**Repository fact.** Authoritative files inspected locally resolve to the review baseline: the research branch differs from that baseline only under `workspace/`. Search was scoped to `docs/architecture`, `docs/planning`, `docs/research`, Factory/Governance/types implementation and tests, Challenge consumers, web presentation, and relevant Git history. Searches covered the prompt's concepts, followed by source reads of the material hits. Absence claims below concern this tree, not all deployments or future consumers.

The load-bearing source map is:

| Surface at baseline | Evidence actually used |
| --- | --- |
| Product Charter §§2–7; Architecture Overview model/runtime and provenance passages | Semantic continuity and purpose-specific views; no universal schema or publisher-count theorem |
| ADR-0003, ADR-0004 (Accepted) | Model/run/draft boundary; immutable publication; content identity versus occurrence |
| ADR-0006 (Accepted), especially policy identity, primitive grammar, ordering and released-policy immutability | Policy version participates in identity; existing ordered V1 content cannot be silently reclassified |
| ADR-0008 (Accepted), “A controlled revision has one semantic fingerprint” | One aggregate reference in the revision; prohibition concerns alternate fingerprints for the same state |
| ADR-0014 (Accepted), decisions 2–12 | Mandatory additions for V2, no lift, first-class V1, exact historical resolution, explicit cross-policy comparison; no rule that every later policy must inherit all V2 fields as mandatory |
| ADR-0015 (Accepted), decisions 2–5, 12–17 | Complete interpretation identity and reproducibility; execution support and historical attribution are separate |
| Factory Design architecture §§3–5, 7–9 (Proposed reference); Factory Resource Semantics (maintained) | Current resource referent and future identity/qualification/grouping admission criteria; proposed direction is not implemented authority |
| Factory Model v2 §§2–10; Engine Semantics v1 §§5–9 and deferred scope | Exact current grammar; pre-ship amendment boundary; zero handling still yields transfer events; rectangular reference-cell rules and publication arithmetic |
| Governance architecture identity/history/comparison and evidence-applicability passages | Historical verification, producer/consumer provenance boundaries, domain-owned comparison |
| ISA-95 mapping §8 and Standards Alignment manufacturing/interchange passages | Hierarchy versus spatial scope; AutomationML is a design-for adapter candidate, not an adopted canonical ontology |
| Factory Design capability and Spatial Runtime Consequences plans, especially research hold and V2 model/identity slices | Shape/validator landed; canonical identity, coexistence and spatial activation held |
| Factory Design Evolution research; composition and maturity briefs/register; game consumer/readiness plans | Future storage/qualification/pool/topology semantics remain research; gameplay spatial dependency is planned |
| `FactoryModel`, `ConfiguredResource`, `OperationStepDefinition`, `FactoryModelValidator`, `FactoryRuntimeAssembler` | Current data shape, validation, explicit eligible resources and duration; optional capacity; projection into runtime |
| `FactoryModelFingerprintV1`, `FactoryModelArtifactV1`, their tests | Exact bytes/digest, canonical round-trip, malformed/trailing-byte rejection, policy-aware support |
| V2 records, validator, `FactoryModelV2ValidatorTest`, `V1V2IdentitySeparationTest` | Mandatory fields, zero-valued handling, overflow/overlap constraints, compile-time separation and validation-only base projection |
| `ModelFingerprint`, `EngineSemanticsVersion`, `FactoryRuntime` | Record equality includes policy version; current Engine support helper checks a version value, not model semantics; runtime accepts V1 publication type |
| `SemanticArtifact`, `SemanticArtifactVerifier`, `ChangeSetFactory`, `FactoryModelSemanticComparator` | Verification versus storage; both-policy comparison guard; V1 domain comparison uses entity identity and order |
| Challenge `CandidateAdmissibilityPolicy` and related placement/floor records; `FactoryFlow.tsx`; scenario adapter | Consumer-local cell checks differ from V2 footprint validation; web positions are presentation; current canonical authoring is non-spatial |
| Git history: `c41ebf1` (#252), `85cbf3e` (#299), current research-hold documents | Normative V2 contract preceded shape/validation implementation; no released V2 identity implementation in inspected tree |

**Temporal check.** Report and review main baselines are identical. There is no landed drift to excuse or invalidate a report claim. The sibling research workspace advanced separately during this review: the report is at `d0b2c5f127d340ecb4816d6316f5028190c23a13`, path `workspace/research/investigations/semantic-contract-maturity-durability-report.md`; its independent review is at `81f1f702b6548ea2bd5f92fe64743ea8798821df`, path `workspace/research/investigations/semantic-contract-maturity-durability-adversarial-review.md`, with disposition **ACCEPT WITH QUALIFICATIONS**. The sibling review's identity, result and mandatory qualifications were inspected after this review's reconstruction and report evaluation began. This review does not independently re-review that sibling.

The original report's “no sibling result” header is a historical statement about its run, not a continuing fact. The sibling result is now reviewed research evidence conditional on its qualifications, not accepted architecture. In particular, its qualifications preserve existing promises pending explicit superseding decisions and inventory; it does not authorize recycling the V1 label. A later sibling-branch overview edit at `2ba254012f0a1b3f185e961a286ebb5d17168215` is unmerged material, not live-main authority. No conclusion here relies on that edit.

## Material challenges

### Whole-policy evolution versus mandatory accumulation — failed

**Challenge.** Report executive finding 2, matrix cases 3/5/6/7/8/9/10, and the self-challenge assert that A must use `v3 = v2 + X`, inheriting mandatory spatial facts forever.

**Repository fact.** ADR-0014 decision 3 makes those additions mandatory in **V2**. Decision 12 requires a new policy when new meaning cannot fit a released policy; it does not constrain the new policy to a mandatory superset. Decision 10 prohibits inventing spatial facts during a V1-to-V2 lift. Neither prohibits a newly specified V3 containing optional spatial and storage records. V1 and V2 can remain untouched and resolvable.

**Counterexample / inference.** Define a hypothetical complete V3 grammar:

```text
V3 domain prefix
unchanged production-record semantics
spatial-present: 0 | 1
if 1: exact spatial record
storage-present: 0 | 1
if 1: exact logical-storage record
```

There are four design forms under **one** policy: core, core+spatial, core+storage, and core+both. Each flag is explicitly encoded; a present payload has a fixed or length-framed grammar; no omitted coordinates become zero. Storage-zone references are validated only when authored and must resolve. Existing V1 artifacts stay V1; authoring storage can publish a V3 artifact with spatial absent. Cross-policy comparison remains explicit. This is a constructed candidate, not an implementation proposal or adopted storage semantics.

That candidate uses linear **policy** evolution and compositional **representation**. Calling it B does not rescue the report's ranking: it concedes that B can coexist with A's complete-policy contract and that the asserted necessity to relax field-membership versioning has not been established. An aggregate policy number identifies the meaning of the whole encoding; it need not claim every contained product or operation changed meaning. A new version may share encoder/validator routines; a new policy does not mechanically require duplicating all implementations.

**Result/effect.** The mandatory-accretion interpretation is a narrower candidate that fails the orthogonal cases. A complete versioned policy with optional records remains viable. The report's structural rejection of linear policy evolution and its `2^N policies` comparison are not valid as stated. This alone meets the repository definition of REOPEN. Today, retaining two closed policies also has a credible simplicity/provenance case; future demand affects the timing of a change, not just its urgency after an allegedly completed proof.

### Byte stability, identity stability and the sibling boundary — failed

**Challenge.** Executive finding 8 and the self-challenge claim unchanged bytes/fingerprint under either an amendment or a new policy version, even suggesting that keeping the prefix while changing the label preserves the invariant.

**Repository fact.** `ModelFingerprint` is a Java record of `(namespace, policyVersion, algorithm, digest)`; the rendered identifier contains all four. ADR-0006 defines that same semantic contract. Therefore for any fixed digest `d`:

```text
factory-model:policy-a:sha256:d != factory-model:policy-b:sha256:d
```

**Inference.** Keeping bytes and SHA-256 digest fixed while changing `policyVersion` cannot preserve the complete fingerprint. Keeping an existing design under its **old** policy preserves its fingerprint, but A can do that too, and historical artifacts are already immutable. Preserving the fingerprint when newly publishing the same concern-free design under an expanded **same** policy is possible only if that extension discipline is authorized. This is a semantic-contract decision, not merely a formatting detail.

**Result/effect.** The invariant is not independent of the sibling result. The report must distinguish retaining an old artifact, republishing under a new policy, extending the admitted set under one policy, and evolving a concern grammar under an existing tag. Its “no migration after the first seam” result is conditional on those choices, not established by composition. This is a second falsified load-bearing assertion. No support promise or same-label extension can be promoted from this review.

### Aggregate identity, component identity and profiles — survives only when narrowed

**Challenge.** Executive findings 3–4, matrix case 11 and proposed negative knowledge reject C via ADR-0008 and reduce profiles to presentation or closed bundles.

**Repository fact.** ADR-0008's one-fingerprint rule addresses the controlled revision's identity of one semantic state. It does not forbid that aggregate's content from referencing independently identified technical contracts. Factory Resource Semantics explicitly permits such an identity when expansion into configured values loses a necessary conformance/dependency statement. One canonical publication authority is not evidence that all referenced facts share one ownership lifecycle.

**Constructed case.** Two designs reference approved specification `S@r`. Equal copied property values cannot answer whether either intentionally depends on that approved revision, nor identify all dependent designs for change-impact review. An aggregate can contain an exact immutable reference plus whatever resolution/content binding is required, while its controlled revision still references one aggregate fingerprint. This is compatible with ADR-0008. A stable concern across aggregate revisions can similarly use an admitted semantic key/reference; a hash of extracted bytes is not automatically a governed identity. Imported CAD source identity may remain provenance if no executable conformance/dependency claim requires it.

**Inference/result.** One aggregate identity is sufficient for current demonstrated publication/run attribution and should not be multiplied merely for modular code. The report's later reopening test correctly recognizes independently governed references. Preserve that narrow result; remove the inference that ADR-0008 prohibits C or that a single publisher proves C unnecessary in every foreseeable case.

A hypothetical “bounded-buffer execution” profile can require finite capacities and a specified blocking interpretation, excluding otherwise structurally valid partial models. That is a semantic applicability promise, not just a friendly name. A profile need not define an alternative Factory fingerprint, cover every imaginable combination, or force profile proliferation. Current Engine and consumer contracts already own analogous support scopes. D can complement A/B; neither profiles nor components are shown to be needed now, but their blanket rejection is unsupported.

### Canonical encoding and compatibility — feasibility survives; sufficiency fails

**Challenge.** Report canonicalization says compatibility reduces to recognized-tag inclusion and that N tag definitions remove the compatibility matrix.

**Evidence.** The existing V1 canonicalizer/test pair establishes that fixed primitives, explicit counts, total canonicalization and strict decode/re-encode are feasible. The proposed segment framing is a credible envelope. RFC 8949 supports deterministic keyed encoding, with important limits recorded below. No actual full compositional Factory grammar or implementation was supplied.

**Counterexample.** Verifier Old recognizes `spatial-placement` containing rectangles. Verifier New recognizes the **same tag** with the report's later polygon alternative. Both recognize the artifact's tag set, but Old cannot validate a polygon payload. Tag-set inclusion is necessary, not sufficient. If a new tag denotes the new grammar instead, support still includes variant identity, prerequisites and interactions. A validator can recognize both `storage` and `topology` yet reject an unresolved storage-zone/route endpoint or an incompatible pair of grammars. Recognition, canonicality, publication validity and Engine applicability are four separate checks.

**Result/effect.** Rejecting unknown tags is a coherent fail-closed publication rule; it preserves safety but means old verifiers are not forward-compatible with new concerns. An opaque transport may retain unknown bytes without attesting to valid Factory semantics; that is not permission for a semantic verifier to accept them. No ambiguity in the envelope has been demonstrated when per-tag grammars and canonical ordering are fully fixed. However, it establishes encoding possibility, not chosen evolution semantics or an O(N) compatibility proof.

The future grammar must fix exact tag equality/ordering (the report's `TEXT(tag)` sort is length-framed encoded-key ordering, not raw textual order), limits, trailing-data rejection, within-segment variant discrimination and a unique concern-free encoding. B-ii's promise of exactly V1 bytes cannot use the displayed mandatory `U64(concernCount)` for zero concerns: it needs an explicit distinct empty-tail rule and rejection of alternate empty encodings. These are feasible specification tasks, not proof that composition is impossible. A first-release shape discriminator is one option; a new concern grammar/tag/policy is another. The report's “only possible” shape-tag requirement is too strong.

### Absence and the supposed irreducible substrate — qualified, not settled

**Challenge.** Invariants 2–3 say the substrate is irreducible and published consumers never see unknown/default, while absence is a universal authored no-claim fact.

**Repository fact.** Current execution requires explicit step duration and eligibility. Current V1 also canonically includes names, ordered collections, capacity and setup fields. `capacityLiters` may be null; this review does not assign that null an undocumented unknown/inapplicable meaning. The validator permits empty product/operation/resource collections. Current field membership is a compatibility fact, not a proof of the minimum ontology for all Charter modes.

**Inference.** “Absent concern = no represented assertion in this dimension” is a useful envelope rule. It does not distinguish why information is absent (unknown to an importer, irrelevant to this use, or intentionally omitted). A concern can require richer internal states: explicitly empty membership, unknown measured property, inherited value, or a pinned external reference. If these states matter they must be represented distinctly or publication must reject that use case; silently collapsing them or declaring them universally impossible is not a complete answer to brief Q12. An absent storage segment does not prove no real-world buffers exist. It only bounds the model's assertions and the interpretation's supported scope.

**Discriminator.** A qualification-based design can know durations only for each operation/resource pair, with no meaningful resource-independent step duration. Requiring a dummy base duration and then overriding it violates the report's own ban on invented facts. An optional concern with explicit precedence works only when both authored values have truthful meaning; it does not prove mandatory base duration irreducible. Likewise an industrial geometry import may have no authored handling magnitudes. The report's first concern still requires them, even though it lists that importer in case 10. Keeping that importer as a draft could be valid, but must be an explicit scope restriction rather than a claimed B pass.

**Result/effect.** The current production substrate is a reasonable conservative starting point, not a universal theorem. No current V1 field may be silently removed under this review. Resolve or narrow the scope of the qualified-performance and geometry-only cases before declaring the minimum substrate and all partial models settled.

### Engine applicability, dependency complexity and ownership — narrowed

**Challenge.** A per-tag REQUIRED/CONDITIONAL/INERT table is said to be sufficient and no more complex than `requireSupported`, with undeclared concerns always inapplicable.

**Repository fact.** `EngineSemanticsVersion.requireSupported` checks equality with the current version; `FactoryRuntime.forModel` accepts `FactoryModelVersion`, and V2 cannot enter that publication path. The spec describes spatial interpretation, but current runtime has not demonstrated V1/V2 concern dispatch. The report acknowledges this limitation later; its “today” wording must consistently mean the specified contract, not executed capability.

**Constructed case.** An Engine knows rectangular `spatial-placement` but receives a valid newer polygon variant under that tag. Or it understands qualification and storage separately but lacks a rule for their joint admission/performance consequences. A tag-only table accepts too much. Applicability must include the exact supported semantic grammar/variants, required facts, value constraints and interaction rules. These can live in Engine-owned predicates; the table is a useful summary, not the full algorithm. No case here proves an explicitly defined predicate over the complete model cannot express applicability.

**Inference/result.** Rejecting unknown or undeclared consequential content is a sensible conservative default. A specifically reviewed INERT declaration can permit known facts that do not affect this interpretation. An Engine need not simulate every authored concern, but must not silently claim full fidelity while omitting consequential restrictions. Adding an INERT disposition for newly admitted content changes accepted scope and requires review under the identity/support contract; it is not automatically an editorial amendment.

Factory owns syntactic/semantic publication validity and reference integrity. Engine owns interpretation applicability and result-affecting rules. Governance/consumer profiles can impose additional completeness or use constraints. Cross-concern presence is neither automatic Factory validity nor automatic Engine applicability. Existing maximum-transfer validation is required by the current Factory contract, even though its formula is aligned with one Engine interpretation; whether richer geometry permits replacing that bound is a future ownership decision.

Dependencies do not guarantee linear validation effort. With N concerns there may be up to N(N−1)/2 distinct pairwise relationships even in an acyclic graph. Example: route clearance against resource footprints and storage zones, plus transport admission against qualification and pool capacity, requires interaction rules as well as tag recognition. Cyclic **references** are not necessarily cyclic **prerequisites** or undecidable validation: aggregate reference checking can be simultaneous. No actual required cycle is established by current consumers; the report's universal DAG and linear-scaling claims nevertheless do not follow from one base-plus-spatial validator. This is not a reason to invent a generic framework.

### Concern boundaries and current V2 reuse — useful evidence, bounded conclusion

**Challenge.** Current five additions are one concern; future geometry/topology/storage/qualification/pools are separable; retaining V2 is low-risk.

**Repository fact.** Current accepted predicates couple floor, placements, footprints and handling bounds. Geometry does not mathematically require handling overhead; Engine v1's transfer formula couples the values for one interpretation. Factory Resource Semantics distinguishes productive resources, transport, workers and buffers without prescribing one universal resource superclass. The report's forklift-as-substrate assertion settles more than that authority currently establishes.

**Result/effect.** Keep today's five-fact spatial package as a proven current unit, not a forever boundary. “Predicates close over declared prerequisites” is a design test requiring semantic judgment, not a mechanical unique decomposition. Logical topology can be coordinate-free; spatial path validation needs geometry; route selection belongs to Engine. Consequential hierarchy can be authored Factory scope, while authorization/organizational governance and UI grouping do not automatically become Factory content. Non-spatial finite buffers are representationally coherent, but no current Arcogine storage consumer validates their proposed execution contract.

The small value records and overflow/containment/non-overlap predicates are reusable. `FactoryModelV2` itself is a complete aggregate, and `SpatialConfiguredResource` embeds a full `ConfiguredResource`; neither is already an optional concern keyed against an independently supplied substrate. Reuse must prevent two authoritative copies of resource facts and validate placement coverage, duplicate/orphan references and ordering. The validation-only `baseModel()` projection must not become a public lossy downgrade. Current tests intentionally enforce the V1/V2 type barrier; a replacement needs equivalent full-content publication protection.

No V2 canonical identity implementation or production construction site was found in the inspected tree, so the historical-identity migration burden is small. This is a scoped repository finding, not an inventory of private artifacts or a waiver of Accepted ADR-0014. Rewriting its byte specification can use the pre-ship process; changing mandatory membership/compatibility decisions still requires the proper supersession/reconciliation. Retaining useful value objects is reasonable, but “retain V2 unchanged as the concern implementation” would be inaccurate.

## External-source re-verification

All three load-bearing sources were opened during this review on 2026-09-18. External evidence remains distinct from Arcogine authority.

- **IETF RFC 8949 (December 2020), §§4.2.1–4.2.3.** It specifies deterministic CBOR forms and encoded-key ordering. This supports feasibility of deterministic keyed envelopes. It does not choose Arcogine's concern boundaries, tag admission, historical support, or compatible semantic evolution. Arcogine's fixed-width primitives are not literally CBOR. The report states some of these limits correctly, then exceeds them in its executive “solved” compatibility claim. [Normative RFC](https://www.rfc-editor.org/rfc/rfc8949.html#section-4.2.1).
- **Protocol Buffers, Application Note: Field Presence, current online documentation, “Semantic Differences” and “Considerations for Merging.”** Explicit presence preserves a distinction that implicit presence loses for default-valued fields; patch behavior illustrates its importance. It does not prescribe why an Arcogine concern is absent or prohibit explicit unknown/inapplicable states. It discriminates absence-preserving designs from designs that erase presence, not A from B when both preserve it. [Official documentation](https://protobuf.dev/programming-guides/field_presence/).
- **AutomationML e.V., “What is AutomationML?”, Basic architecture.** The association describes a distributed architecture connecting topology, geometry/kinematics and behavior through referenced formats, naming IEC 62714-1:2018, CAEX/IEC 62424, COLLADA and PLCopen XML. This verifies a real engineering decomposition and a reference-based counter-context. The page does not prove that a single canonical publisher prevents independent component identity, nor establish that all AutomationML variants lack content-derived identity. The full IEC standard was not read and no conformance claim is made. [Association overview](https://www.automationml.org/about-automationml/automationml/).

The unavailable AAS material was background in the report and is unnecessary to the disposition. It was not promoted into verified evidence here. More analogy collection would not repair the internal counterexamples.

## Audit of all twelve mandatory proving cases

“Constructed” below means a reasoned hypothetical, not an observed consumer or runtime experiment. C denotes referenceable components; D denotes meaningful support profiles; H denotes the complete-policy/optional-record hybrid above. No candidate is admitted merely because it can serialize a happy path.

| Case | Adversarial result and effect on candidate comparison |
| --- | --- |
| 1. Core-only deterministic production | Landed V1 proves this. A/B/C/D/H can represent it; C adds no demonstrated benefit. A does not force existing V1 designs to migrate. Irreducibility beyond today's execution is unproved. |
| 2. Current spatial transfer | Shape/validation and normative timing are evidenced; canonical spatial runtime is not. A's V2 and B/H can carry the same facts. C is unnecessary today; D can state supported scope. This case does not discriminate the general evolution rule. |
| 3. Logical storage without geometry | Constructed capacity/buffer relations can omit coordinates in B/H, in a new optional-record A policy, in C, or in a bounded D profile. The report's A/D failures assume mandatory bundling or all-combination profiles. No current storage behavior is demonstrated. |
| 4. Spatial storage zones | Constructed zone references require both storage and geometry and joint validity. Every candidate can express them; B needs an actual interaction predicate, C exact reference binding. One aggregate hash does not force one indivisible concern. |
| 5. Richer geometry | Add a tagged alternative, a new concern grammar/tag, or a new complete policy; all can preserve old artifacts. B's same-tag polygon exposes tag-inclusion insufficiency. A changes policy identity, not necessarily product meaning; this is a tradeoff, not representational failure. |
| 6. Explicit topology | Constructed logical adjacency needs no coordinates; geometric routes do. Authored connectivity and Engine path interpretation remain separate for every candidate. H/A optional fields refute required spatial inheritance. |
| 7. Contended transport | Constructed resource/route allocation is possible, but transport ownership/lifecycle and admission are unresolved. B does not prove a forklift is a current productive-resource record. Optional transport under A/H does not force universal declaration; C/D depend on actual ownership/support need. |
| 8. Qualification/performance | Constructed requirement/provision relations can be non-spatial under all candidates. Resource-only durations challenge B's mandatory base duration. Declared overrides cover only the case where a truthful base value exists. The report's qualified pass needs this additional discriminator. |
| 9. Hierarchy/pools | Maintained resource semantics and mapping support independence from placement. Consequential scope must be specified; UI folders alone do not count. B/H/A/C can represent it, D can restrict supported scopes. A's claimed impossibility is unsupported. |
| 10. Partial consumers | Production-flow use is real; spatial optimizer/game integration is planned; richer importer is hypothetical. A/H/B can support finite valid subsets. B's initial spatial concern cannot represent geometry with unknown handling without further decomposition or an explicit publication restriction. Engine and Governance need exact supported-subset checks. Partial operational models must disclose excluded constraints. |
| 11. Historical comparison | One aggregate fingerprint per revision survives all candidates, including C with aggregate references. Within one fixed grammar, concern add/remove can be compared without invented values. Cross-policy changes still require explicit support under A/H and potentially B. A is not forced to reclassify every revision; C does not violate ADR-0008. |
| 12. Cross-concern validation | V2 proves one dependency layer. All candidates need real reference/interaction validation. B's DAG can be a chosen discipline but neither global acyclicity nor linear complexity is proven. Conditional references versus mandatory prerequisites must be distinguished. |

The brief permits non-implemented future cases, so lack of a third landed consumer is not itself a failure. The defect is treating constructive representability as proof of superiority, and declaring every B case passed while reserving the decisive semantics for later work.

## Audit of all twenty required questions

| Question | Review outcome after following the report's answer references |
| --- | --- |
| 1. Minimum substrate | Current executable substrate identified; universal irreducibility not established (resource-dependent performance and non-executing partial authoring). |
| 2. V1 facts versus implementation detail | Correctly distinguishes unused current fields from execution needs, but all current canonical fields remain identity-bearing. The new minimal substrate and retained V1-shaped stream need an explicit boundary/migration account. |
| 3. Spatial concern boundaries | Current five-fact bundle supported; geometry/handling separability and future concern closure remain conditional. |
| 4. Absence | Envelope-level no-represented-claim rule survives; it does not exhaust concern-internal state meanings. |
| 5. Valid combinations | Predicate conjunction is plausible; universal DAG/linear-cost claim is unsupported. |
| 6. Aggregate versus component identity | One aggregate reference retained; no current need for another identity. Future prohibition is not proved by ADR-0008. |
| 7. Justification for component identity | Independent conformance/dependency/reference requirement is a sound trigger; publisher count is insufficient evidence either way. |
| 8. Bytes without version matrix | Deterministic envelope possible; version/support complexity and full fingerprint invariance are unresolved or misstated. |
| 9. Optional/extensible determinism | Feasible subject to complete per-variant grammars, canonical absence, tag rules and rejection obligations. Not a semantic compatibility proof. |
| 10. Engine requirement declaration | Concern dispositions useful as a summary; exact grammar/value/interaction predicates are needed. |
| 11. Missing requirements | Explicit pre-runtime refusal survives. Existing `requireSupported` is only a version check, not evidence of implemented general applicability. |
| 12. Not modeled/zero/default/unknown | Missing: explicit account of unknown, inapplicable, inherited and deliberately empty states where material. No defaults remains valid. |
| 13. Geometry evolution | Multiple legitimate representation/evolution choices remain; first-release shape tag is not the only possibility. |
| 14. Storage/spatial independence | Coherent constructive examples; not current storage consumer evidence or settled blocking semantics. |
| 15. Revision/comparison | Within-policy presence comparison survives. Cross-policy handling and comparator support remain necessary; no invented values. |
| 16. Independent durable concern | Identity trigger partly answered; durability properly belongs with sibling evidence and explicit support decision. |
| 17. Sibling constraint | Header correctly defers, but invariant and permanent same-policy growth pre-empt the answer. Incorporate now-reviewed sibling qualifications explicitly. |
| 18. Existing V2 | Value objects/predicates reusable; complete aggregate and embedded-resource shape require redesign, exact publication protection retained. |
| 19. Reconciliation targets | Appropriate surfaces identified, but supersession prescription is premature and optional-policy countercandidate may require a different scope. |
| 20. Falsification | Useful triggers exist; the viable hybrid and policy-label identity counterexample actually fire the review's reopening criteria now. |

## Minimum work to resolve the reopened question

1. Compare **mandatory cumulative policies**, **closed complete policies with optional records**, and **same-policy growing tagged concerns** as distinct evolution choices. Keep aggregate identity, component references and profiles as separate axes. Re-score all twelve cases without treating them as mutually exclusive categories.
2. Provide a small explicit artifact sequence: core; core+logical storage; core+spatial; both; rectangle-to-polygon; known tag with unsupported variant. For each, state exact policy/grammar identity, old/new verifier behavior, Engine applicability, and comparison. A paper construction plus byte examples is sufficient for feasibility; no product implementation is required.
3. Specify which stability claim is intended: old artifact retained, same bytes, same digest, same complete fingerprint, or cross-policy semantic equivalence. Reconcile it with the reviewed sibling result and current promises. Do not change V1 meaning or support as a side effect of structural analysis.
4. Test geometry-without-handling and resource-dependent-duration-without-a-truthful-base-duration. Either demonstrate their representation/ownership/applicability or explicitly narrow canonical publication scope and weaken the universal substrate claim.
5. Replace tag-only compatibility and O(N) validation assertions with exact variant/dependency/interaction obligations. Give at least one unsupported combination and show where refusal occurs. Keep unknown/inapplicable/inherited/empty states distinct when consequential, and preserve the conditional admission test for component identity and profiles.
6. Produce a new report revision in this same workspace and obtain independent review of its exact commit/path. Keep the reviewed report and this review reachable. Only surviving conclusions, with all qualifications, can later enter separately reviewed durable reconciliation; current holds remain.

## Verification and custody

Validation for this research artifact is source inspection, exact-revision checks, the counterexamples above, external-source re-verification, Markdown/diff checks and artifact reachability. Product behavior was not changed. Java/frontend test suites were not run; source tests were inspected as evidence and are not reported as having passed in this session. No spatial runtime experiment or new Factory serializer was performed. Full IEC text, external deployment inventory, and all future consumer contracts remain outside verified scope; none is required for the two decisive repository-internal counterexamples.

Completed checks: `git diff --cached --check` passed; the repository Markdown-link parser, invoked with Python in the discovered running development container against the review text supplied on stdin, found three external links and no local link targets; fence-balance validation passed. `git merge-base --is-ancestor` verified both the report and prompt commits remain ancestors of this workspace. A final `git ls-remote origin refs/heads/main` still returned `46389bc5d21e0c95ffa9366ce271f9d943d44b35`. The human owner identity was verified before commit. The tracked-workspace merge-candidate check is intentionally not a completion gate for an active research-evidence branch; this artifact must be removed or deliberately transferred before any later merge candidate.

The completed review is persisted on `research/factory-model-semantic-composition` at `workspace/research/investigations/factory-model-semantic-composition-adversarial-review.md`; its exact commit is supplied in the handoff after commit. The disposition remains bound to `885647023f8945d123cd0c74a8cdff2fb035742e:workspace/research/investigations/factory-model-semantic-composition-report.md`. This workspace is not retirement-eligible and must not be merged as a research archive.

**Independence:** fresh isolated review session achieved; different model family not asserted. **Final disposition: REOPEN.**
