# Adversarial review: semantic-contract promotion boundary

> **Disposition:** REOPEN
>
> **Date:** 2026-09-25
>
> **Authority:** Research evidence only. This review does not amend architecture, change identity meaning, reopen the maintained research register, or admit implementation.
>
> **Risk:** High: identity, historical attribution, persistence and compatibility.
>
> **Evidence workspace:** `research/semantic-contract-promotion-boundary-review`
>
> **Reviewed report:** commit `8fa235b5621e3f97fd41c1aa327d6303e464c65d`, `workspace/research/investigations/semantic-contract-promotion-boundary.md`; report blob `d1ba7e2ceb7dafe435756ac9b3d856c15f6722e3`, 572 lines.
>
> **Report baseline and independently resolved live-main review baseline:** `b9d6e8b0e3b10f07cbc6777e3bc3abbce2f45c19`. Main was rechecked before persistence and remained unchanged. The workspace still pointed to the reviewed report commit before this review was added.
>
> **Independence:** This is a fresh review session with no responsibility for authoring or defending the original report, not the author's self-challenge. A different originating model family was not verified. Anchoring qualification: initial report retrieval exposed its executive conclusion before the specialized review procedure and original handoff were read. Primary authorities were then examined before the candidate matrix and detailed recommendation were evaluated. This was not a fully blinded reading sequence; the qualification must accompany the review.

## Verdict and its scope

The report successfully exposes a gap between the Factory V1 specification's attributed-record assertion and the concrete evidence offered for it. Factory publication code, a filesystem authority's persistence capability, and temporary test records are not interchangeable with demonstrated retained reliance. That diagnostic result should be preserved.

The stronger conclusion does not survive as written. Missing fixation evidence does not establish that every relevant promise or accepted use is absent. More decisively, the proposed replacement invariant excludes an actual undeclared retained acceptance that current architecture explicitly requires the owner to account for. The proving matrix also introduces a cross-policy fingerprint-equality requirement and counts discretionary costs as semantic falsifications.

**REOPEN applies to this report revision's conclusions and proposed invariants, not to the entire semantic-contract-maturity result or Factory composition rule.** It is not a finding that an actual retained V1 record has been discovered. Neither a universal freeze nor a universal permission to correct in place has been established by this review. Current specifications remain authoritative pending a separate reviewed reconciliation.

## Question, method and scope

The original handoff asks what actual retained-use or commitment boundary, if any, fixed Factory and Engine identities, and whether implementation/proving activity has been mistaken for that boundary. I checked the handed-off report's identity, its original handoff, the Researcher procedure, the research operating model, relevant register entries, primary semantic contracts, and selected executable surfaces at the exact baseline.

The governing distinction reconstructed from primary authority is between:

1. an identity's meaning and definition;
2. a published promise about that meaning or its supported use;
3. an actual acceptance of attributed material, including defective undeclared acceptance;
4. evidence that a promise has been fulfilled; and
5. continuing publication, retention, decoding, execution and migration capabilities.

An empty result for one category does not establish emptiness in the others. In particular, a lack of observed historical runs does not establish that a specification never published a stability promise.

The serious alternatives are therefore: actual retained attribution; an already-expressed owner commitment with incomplete declaration or fulfilment evidence; genuinely disposable proving with no commitment; and a scoped mixture differing between Factory, Engine and individual promises. Immutable definition identities with bounded support remain an alternative to changing a provisional definition in place. These alternatives are not ordered by how many version labels they produce.

### Evidence inspected

Repository facts below are grounded in these baseline surfaces:

- [Architecture Overview, semantic evolution and support](../../../docs/architecture/overview.md#semantic-evolution-and-support), especially rules 1-7, and [Semantic contract support](../../../docs/development/semantic-contract-support.md), including undeclared acceptance and retained experimental artifacts.
- [Factory V1](../../../docs/architecture/factory-model-v1.md), [Factory V2](../../../docs/architecture/factory-model-v2.md) sections 6 and 9-11, [Factory semantic evolution](../../../docs/architecture/factory-design.md#111-semantic-evolution), and [Engine v1](../../../docs/architecture/engine-semantics-v1.md) sections 1-1.2.
- [Controlled revisions](../../../docs/architecture/controlled-revisions.md), including the authoritative persistence-acceptance boundary.
- [FactoryModelPublisher](../../../product/domains/factory/src/main/java/com/arcogine/factory/model/FactoryModelPublisher.java), [RuntimeObservationMetadata](../../../product/domains/factory/src/main/java/com/arcogine/factory/process/RuntimeObservationMetadata.java), and the constructor/acceptance path of [FileControlledRevisionAuthority](../../../product/governance/src/main/java/com/arcogine/governance/FileControlledRevisionAuthority.java).
- The temporary-directory setup and reopen/resolution cases in [FileControlledRevisionAuthorityTest](../../../product/governance/src/test/java/com/arcogine/governance/FileControlledRevisionAuthorityTest.java).
- The [research register](../../../docs/research/research-register.md), [maturity conclusion](../../../docs/research/investigations/semantic-contract-maturity-durability.md), [Engine applicability brief](../../../docs/research/investigations/engine-applicability-after-transfer-boundary.md), and [composition decision history](../../../docs/history/decisions/2026-09-23-factory-model-semantic-composition.md). The last is historical evidence, not present authority.

Connector searches were discovery aids; source claims used exact-baseline fetches. This was a source-and-contract review, not a build or test execution. It did not reproduce the report's exhaustive local grep, retired-interface audit, release/tag enumeration, or full historical decision-record genealogy. It does not inspect contributor machines, third-party consumers, or every planning document. Consequently, its absence conclusion is bounded: **no concrete retained attribution was demonstrated in the inspected evidence**, not proof that none exists anywhere. The findings below do not require a global absence claim.

## Challenges and results

### Published commitments are not erased by missing attribution evidence

**Challenged:** Executive conclusions 1-3 and 6; candidate B's treatment; the Engine assessment; and the proposed instructions to record no fixation and correct existing definitions in place.

**Repository facts:** Factory V1 does more than display a normative status badge. Its immutability section requires consistent fingerprints across implementations, software versions and languages and prohibits identity-affecting changes under V1. Engine v1 likewise states a new-version rule for intentional changes to interpretation and a historical specification/fixture retention promise. The support policy separately states that an unproved promise remains a validation gap, rather than disappearing because its evidence is missing. A contract may be introduced and committed to in the same change.

**Result / inference:** The report is entitled to challenge whether the stated attributed records actually justify the Factory claim. It is not entitled to infer from that evidential failure alone that the stability commitment was never made. Its own self-challenge recognizes this alternative but leaves it unresolved while maintaining a high-confidence no-fixation conclusion. Candidate B is a potentially operative reading of existing text, not merely a future owner preference.

This does not establish that every public normative sentence automatically fixes a contract. The necessary analysis is narrower: is the specific stability language an independently published commitment, or solely a conditional consequence of the disputed attribution premise? What exact use and definition does it cover? A missing named consumer or custody checklist cannot by itself answer that, particularly when the promise concerns all implementations claiming the policy rather than a particular stored record.

**Required revision:** Separate the factual attribution audit, interpretation of already-published commitments, fulfilment evidence, and desirability of future commitments. Preserve the evidence-gap finding. Do not recommend an affirmative no-fixation entry, same-token correction, or removal of historical cases until the commitment question is resolved. Any narrowing of an actual promise must be an explicit authorized transition preserving existing obligations, not a retrospective inference from missing paperwork.

### The proposed event rule drops the undeclared-acceptance safeguard

**Challenged:** Surviving invariants 1-2 and 4, candidate H's claim to preserve rules 1-7, and durable consequence 1's permission to correct any contract whose fixation is not evidenced.

**Repository facts:** Overview rule 5 and the support policy expressly cover acceptance without a custody declaration. Such acceptance is a defect, not a waiver. Further admission must stop while custody/support is established, and already accepted facts must be accounted for. Test scope or later deletion alone does not prove disposability.

**Counterexample:** An authority actually admits a model artifact for retained historical explanation but omits the required declaration. Before the omission is audited, someone changes the policy definition, treating the absence of a recorded fixation event as permission. The report's exhaustive event list names declared acceptance, declared reliance and recorded owner commitment; it does not preserve this fourth, defective-but-real case.

**Result:** H does not preserve the existing rule as written. A pre-admission refusal requirement is useful prevention, but cannot repair the definition of what happened after that prevention failed. Likewise, a test-created object remains disposable only while no retained use has actually been admitted against it. The inspected temp-directory test demonstrates capability, not external retained use, but its label cannot become a blanket exemption.

**Required revision:** Make actual undeclared retained acceptance an explicit obligation-creating case. Treat unknown fixation as unknown, not mutable. Distinguish prevention, discovery and remediation. Preserve original attribution and exact defining basis; block further admission until the defect is resolved. This is a concrete failure of a proposed surviving invariant, not merely a request for more evidence.

### The matrix conflates equality, identity and support cost

**Challenged:** Proving case 14, the failure labels for A and B, the post-attribution failures assigned to C, and the claimed necessity of removing the V1/V2 structure.

**Repository facts:** Factory fingerprints identify content under a specific policy. Factory semantic evolution explicitly distinguishes cross-policy semantic comparison from full-fingerprint equality. V2 golden vector 16 deliberately tests that separation. The same owning contract also makes continued publication, decoding, execution and coexistence separate scoped support decisions; distinct policy identities do not require permanent dual support.

The composition history records that an earlier permanent-dual-policy-burden argument did not survive review. It also records that no inspected consumer depended on unchanged complete fingerprints across concern admission. That historical qualification supports, but does not replace, the current contract.

**Result / inference:** Identical authored production content does not entail one full fingerprint across different canonicalization grammars. Case 14 assumes the disputed cross-policy equality requirement rather than deriving it from a consumer. Keeping two distinguishable definitions may be less convenient, but is not a truthfulness failure. The report's combined failure category masks this difference.

Candidate C should also be tested in its strongest coherent form: correction before an established boundary, followed by compliance with the existing whole-definition rule after actual acceptance. Lack of a new visual marker does not by itself entail continued rebinding after acceptance. Evidence for mechanical enforcement can discriminate implementation proposals, but is not a proof that this semantic alternative cannot exist.

**Required revision:** Separate semantic contradiction, evidential uncertainty, enforcement weakness and discretionary cost. Test bounded-support immutable definitions and a boundary-obeying correction candidate. Identify an actual consumer or measured transition cost before turning duplicate policy-relative fingerprints into a decisive defect. A single provisional Factory grammar can remain an option, not a demonstrated consequence of the current audit.

### Source interpretation and token distinguishability are overstated

**Challenged:** The claimed V2 shipped-path freeze trigger, repository-history proving case 13, and the mandatory final-token requirement.

**Repository fact:** V2 section 10 reports that no fingerprint has been produced by a shipped publication path or recorded against a controlled revision, then explicitly identifies first retained attribution as the freeze boundary. Its absence premise does not establish the converse proposition that any shipped fingerprint would freeze the definition. Clarifying the introductory wording may help, but the report should not present this as an independently established replacement of the current rule by the older trigger.

**Inference:** A token embedded in canonical bytes creates a real ambiguity risk when the same apparent identity can select incompatible definitions. However, that observation does not alone prove the only solution is a committed token never used anywhere in repository history. The revision must reconcile overview rule 1's non-rebinding requirement with rule 2's pre-attribution correctability: distinguish an editable draft document or alias from the exact semantic identity a retained record resolves. Test immutable experimental revision identities, reserved final identities, and any proposed admission-time binding explicitly. A moving draft alias is not an acceptable sole retained identity.

The support policy permits retained experimental artifacts when exact definitions, retained basis, horizons and refusal behavior are accounted for. Refusing every mutable unresolved identity is justified; refusing every experimental identity merely because it lacks a promotion label is a stronger mechanism that needs its own justification. This review selects no naming scheme or registry.

## Engine and Factory consequences must remain conditional

The publisher implementation validates and returns an immutable in-memory version; that call does not itself demonstrate outward published reliance. The inspected runtime observation metadata lacks Engine semantics identity, consistent with the specification's acknowledged propagation gap. Neither fact demonstrates that a previous specification-level commitment is absent.

The Engine applicability brief really does carry the current fixed-definition premise, so the fixation audit is relevant to its framing. Its retained-history cases are proving dimensions, however, not an inventory claiming that particular historical run records were found. Their usefulness as failure cases does not depend on already having such records. A result affecting historical attribution must still survive first acceptance followed by a later interpretation change.

Do not collapse that question to one correctable Engine definition by inference from missing metadata. First resolve the commitment status, then compare applicable interpretations, supported Factory content and refusals. Factory and Engine can reach different answers. The existing publication and spatial-runtime activation blocks remain unchanged by this review.

The closed-policy composition rule, explicit optional records, one aggregate identity, and absence distinct from authored zero are not falsified here. Whether V1/V2 should coexist, one should be retired, or a new provisional grammar should replace the development arrangement remains a separate explicit choice under the resulting obligations.

## External-source verification and limits

The following original-report analogues were checked against primary sources on 2026-09-25. They provide counterexamples and design precedents, not Arcogine authority.

- [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html), items 1, 3, 4 and 9: a public API may be documented rather than implemented as a separate endpoint; instability of a development API does not permit mutating an already released package version. This supports separating publication, content immutability and compatibility. It does not decide what constituted an Arcogine promise.
- [Kubernetes API Overview, API versioning](https://kubernetes.io/docs/reference/using-api/#api-versioning), live documentation: alpha and stable names expose different support/stability expectations. This is evidence that visible provisional naming can be useful, not proof that Arcogine requires the same naming or storage lifecycle.
- [IETF RFC 7595 / BCP 35](https://www.rfc-editor.org/rfc/rfc7595.html), June 2015, sections 7.2-7.3: an existing registration can change status, and registrations have a change-control mechanism. Registration permanence is not a proof of immutable canonical semantic bytes. The analogy cannot establish that every final semantic token must differ from every provisional token.

The multicodec source was not successfully reverified in this review and is not load-bearing here. The historical claim that all four analogues contradict any deliberate implementation-linked commitment is too strong: an outside system's different trigger cannot invalidate an Arcogine owner decision. No analogy substitutes for the actual owning contract and its accepted uses.

## What survives and what would close the review

Preserve the concrete-referent audit of the attributed-record assertion, the distinction between domain publication and publishing reliance, independent Factory/Engine analysis, and the rejection of automatic support obligations merely from naming an identity. No universal lifecycle has been justified.

A revised report should satisfy four conditions before it can support reconciliation:

1. Resolve, or explicitly leave open, whether the existing Factory and Engine stability language already publishes a commitment; give each claim an evidence scope and avoid equating unknown with absent.
2. Repair the proposed invariants to preserve actual undeclared retained acceptance and to distinguish preventive refusal from remedial accounting.
3. Rework the discriminatory cases so policy-relative identity, historical meaning, continuing support and convenience costs are evaluated separately; retain viable alternatives rather than rejecting them for not having the preferred marker.
4. Correct the V2 source reading and downgrade the universal token/naming conclusion to a tested option unless a stronger necessity argument is established. Reassess Engine and Factory consequences only after those corrections.

Confidence is high in the identified conflicts with the inspected normative text and the matrix's unestablished equality premise. Confidence is intentionally lower about the true scope of historical or external reliance, which this review did not exhaustively audit. Discovering an actual retained record would strengthen the obligation side; documenting that a statement was expressly provisional and created no promise could strengthen the correction side. Neither outcome would rescue an invariant that erases an already accepted fact merely because its declaration was missing.

This review changes no canonical document, code, register status or implementation gate. The evidence workspace remains active; no retirement or merge is authorized. A revision is needed before the report's strong de-fixation and identity-collapse conclusions become decision-quality evidence. A later reconciliation still requires its own independent PR review.

**Final disposition: REOPEN.**
