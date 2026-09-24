# Engine applicability to optional-record Factory policies

## Header / authority

- **Date:** 2026-09-24.
- **Research status:** ACTIVE; this is a completed investigation report, not a reconciled decision. The maintained register remains unchanged.
- **Risk:** High: accepted-input identity, refusal, determinism, historical interpretation and cross-consumer provenance.
- **Research baseline:** live `main` at `7e5e6ad2f8a2707d00fff3e2a4e3db96da85852a`.
- **Final repository recheck:** live `main` remained at that exact SHA before handoff; the custody branch was still at the supplied prompt commit before this report was added.
- **Custody branch:** `workspace/research-engine-v2-applicability`.
- **Input:** `b5a4a4fbcf6db3dfe46c23b39378b2ffd226cd36`, `workspace/research/engine-applicability-optional-record-factory-policies-prompt.md`. Its earlier main baseline is not the baseline of this report.
- **Authority:** Research evidence only; no accepted architecture, implementation admission, publication release or spatial activation follows from this report.
- **Adversarial-review status:** **required, not yet performed**. Author self-challenge below is not independent review. This high-risk conclusion is not yet decision-quality evidence for durable architecture reconciliation.

## Question and decision at stake

Can the fixed `engine-semantics:v1` interpretation be stated as applicable to the reconciled `factory-model:v2` policy both with its complete spatial record present and with it absent, by a demonstrably semantics-preserving correction? Otherwise, what minimum difference requires a distinguishable Engine interpretation?

The decision is the identity under which execution of these V2 artifacts, including spatial runtime activation, may be attributed. Factory validity, Engine definition applicability and implemented execution support are separate predicates.

Scope includes publication/assembly, accepted inputs and refusals, the five spatial facts, supported observations/events, retained provenance and compatibility. It excludes implementation, Factory-composition redesign, general same-label amendment policy, scheduling-policy selection, pathfinding, transport resources, and analytics ownership. No production, architecture, research-register or planning file is changed.

## Executive conclusion

**Recommendation: Candidate B, a distinguishable Engine identity, for execution covering both reconciled V2 forms. Confidence: high for that bounded decision.** Keep `engine-semantics:v1` unchanged. Define the successor's policy/content applicability explicitly; reuse the established non-spatial and transfer rules where their meanings are preserved.

The minimum difference is admission of a **V2 spatial-absent artifact as itself**, with its V2 fingerprint, and an explicit production-only interpretation that emits no transfer transitions. It is not a new distance formula. V1's existing non-spatial execution applies to Factory V1, and its spatial definition consumes the five facts that the earlier V2 definition required. Neither establishes an identity-preserving closure rule accepting a different policy/content case merely because its production projection is equivalent. Missing spatial facts are not legal zero values, and a V2 artifact cannot be relabelled as V1.

Candidate A therefore fails its affirmative whole-definition preservation burden for the combined domain. This is stronger than observing a missing test but narrower than claiming that every new Factory policy necessarily requires a new Engine identity. It follows from this fixed definition, this changed input case, and the brief's correction burden.

**Qualification:** spatial-present V2 retains the meanings of the five facts and the specified transfer computation. This report does not establish that implementing those already-defined rules, by itself, requires a new identity. A present-only correction has a materially stronger preservation argument, but would not answer support for the spatial-absent half. Full applicability of the revised artifact policy, including its referenced definition and refusal boundary, cannot be inferred from formula equality. Use one explicitly defined successor for the complete requested V2 domain rather than depending on that narrower unresolved amendment argument.

## Repository evidence

All current facts below refer to the exact research baseline. Source locators at the end identify paths and sections/methods; history is explicitly separated.

### Fixed meaning, implemented coverage, and Factory validity

**Repository fact.** The overview fixes complete attributed definitions, including unexercised rules and refusal behavior. Engine v1's membership test includes acceptance/rejection, assignment, ordering, timing and derived results. Its specification already defines spatial reservation, transfer and observation semantics even though execution is partial. The runtime's implementation limits cannot erase those rules. [Sources: architecture/support; Engine specification.]

**Repository fact.** Factory V2 is a closed grammar with unchanged required V1 production records and an optional complete spatial record. Absence asserts no spatial facts and synthesizes none. Present-with-zero is distinct authored content. V2 adds a presence byte and retains a policy prefix different from V1; even production-only V2 has a different fingerprint domain. No V2 publisher, canonical codec or artifact verifier is implemented. `FactoryModelV2Validator` proves shape/validation only. [Sources: Factory specifications; V2 implementation.]

**Repository fact.** Factory validity is insufficient for execution: the Engine definition must support the exact policy and represented records, values, variants and interactions. Outside that domain the Engine refuses before runtime mutation. The Factory composition reconciliation explicitly leaves this applicability question open. [Sources: Factory design §11.1; V2 §1.2; composition brief.]

**Repository fact.** `FactoryModelVersion` wraps only `FactoryModel`, validates in its constructor and derives the V1 fingerprint. The publisher and assembler accept that type. `FactoryModelV2` is a separate final record, not a subtype; its package-private `baseModel()` is a validation-only projection. It is not a published V2 execution path. `FactoryHandler.handleTaskEnd` starts the admissible next operation directly, without spatial transfer state. [Sources: publication/assembly; runtime.]

### V1 attribution and support inventory

Search scope: all tracked baseline repository files, with semantic follow-up across `docs/architecture`, `docs/development`, research briefs, planning, and `product`. Searches included the exact identities, their Java types, publisher/assembler, applicability/support/refusal/rejection, spatial presence/absence, handling magnitudes, fingerprints and runtime provenance. Generated build output was not evidence for source-absence claims. The inventory describes inspectable records/contracts, not an audit of external consumers or undisclosed stores.

| Surface or record family | Attribution/dependency found | Support, refusal and custody boundary |
| --- | --- | --- |
| `EngineSemanticsVersion` | Nonblank opaque value; `CURRENT` is `engine-semantics:v1` | `isSupported` accepts only exact equality to CURRENT; `requireSupported` explicitly throws for another identity. Constructing an identity is not support. |
| `FactoryRuntime` session | Final semantics field, fixed on creation; `semanticsVersion()` exposes it; fresh/reset sessions have fresh `RunId` | Only `forModel(FactoryModelVersion)` exists. No caller-selected version or mutable setter. Reset currently selects the same single supported version through fresh construction. No durable run store is implemented here. |
| `FactoryModelVersion`, publisher, assembler | V1 published model retained for session lifetime | Validation and Java type separation exclude V2 before a runtime can be constructed. There is no dynamic V2-policy applicability gate to demonstrate yet. Assembly currently precedes the constructor's check of the hard-coded Engine identity; that is not proof of pre-mutation rejection for a future caller-selected pair. |
| `RuntimeObservation` / `RuntimeObservationMetadata` and contained job/resource/order/pending/metrics views | Results depend on the fixed interpretation; metadata carries run, model, time, state and event cursor | Normative Engine identity propagation is mandatory but absent from the implemented metadata. A snapshot alone must not be assumed to contain it. No spatial observation implementation. |
| `RuntimeEventEnvelope`, payloads and affected references | Model/run identity and sequence; events are produced after authoritative transitions | Normatively require Engine identity; field is currently absent. `drainSupportedEvents()` clears the runtime buffer; consumer retention is outside this implementation. No supported transfer events implemented. |
| `CommandResult.Accepted`, `.Rejected`, `.Faulted` | Model version and command effects, with Engine-defined outcomes | No Engine-version field. Rejected guarantees no command mutation and no scheduled events; Faulted may follow mutation. This command contract is not an implemented model/Engine pair selector. |
| Direct handler results, internal events, jobs/orders and metrics; test/benchmark traces | Depend on semantics but generally do not carry the identity | These are not automatically durable attributed run records or substitutes for supported envelopes. Pinned v1 conformance tests retain executable expectations, not a production result-history service. |
| `EvidenceProvenance` → `EvidenceReference` | Optional Engine identity plus producer model, occurrence and material inputs can represent Engine-derived evidence | Does not call execution support checks, correctly allowing historical/unsupported identities to be described. Missing identity stays missing. Constructor/reference existence is not verified producer integration or declared durable custody. |
| `EvidenceUse` / evaluation basis and occurrence | Transitively depend on source evidence provenance and the consuming interpretation | `InMemoryEvidenceReferenceAuthority.record` rejects rebinding the same source/revision to changed intrinsic provenance. This is a fixture/headless authority, not durable storage. Governance tests explicitly carry CURRENT and missing-version/retired-producer examples. |
| File-backed controlled revisions and semantic artifacts | Retain Factory model fingerprints/artifacts, not an Engine run identity | V1 artifact support checks namespace/policy/algorithm and strict decoding. Generic `SemanticArtifactVerifier` means absence of an in-tree V2 codec does not exclude an outside producer. A controlled model revision is not an Engine result. |
| Owning architecture/support declarations | Published obligations bind Engine meaning, exact definition resolution and historical interpretation | Specification plus conformance fixtures survive execution retirement; no permanent executor follows. Spatial execution, envelope propagation and durable Engine-result integration remain gaps. |

**Limit on absence claims:** no repository-owned durable Engine-result store or universal external acceptance inventory was found. That does not prove that no result has ever been retained. Neither a missing store nor fixture-only examples justify unfreezing v1; the investigation applies the brief's fixed-definition constraint and the published whole-definition rule.

### Historical definition check

The parent of the Factory composition reconciliation, `1ab991b6e0d9529de35c845cbd0995bb2408af8c`, defines V2 as V1 semantics plus five **required** spatial additions. Commit `2e13eb5fcf71823740453b1c80d0e792fa38a746` changes that Factory grammar to optional spatial presence, adds the marker and records the open Engine question. It makes **no diff** to `docs/architecture/engine-semantics-v1.md`.

This history corroborates the live brief's description of the dependency change. It is not authority to execute an obsolete V2 byte grammar or overwrite the reconciled Factory policy. In particular, a mutable link from the fixed Engine document to a revised Factory document cannot silently redefine which cases the fixed Engine definition admits.

## Candidate models / hypotheses

**Candidate A — preservation proof and correction (no new abstraction).** Retain v1 and state that it accepts V1 plus both reconciled V2 forms. For spatial-present V2 use the existing five-fact transfer interpretation; for spatial-absent V2 use the existing production-only behavior. Preserve actual model provenance and every existing rejection and interaction. Its burden is proof that all of this is already entailed, not merely implementable.

**Candidate B — distinguishable complete interpretation.** Leave v1's definition intact. A successor, denoted **E-new** here as explanatory notation rather than an allocated identifier, explicitly defines applicability to the reconciled V2 grammar with both presence cases. It interprets absence as production-only execution without transfers, and presence through the retained transfer rules. The successor may also explicitly support V1 without conversion; that is a scoped support choice, not a numerical pairing between Factory and Engine versions. No policy menu, concern registry, new fingerprint or separate transfer-version identity is required.

**Restricted alternative checked:** v1 for spatial-present only, while refusing V2 spatial-absent. This may preserve more of the already-defined spatial domain but does not provide the requested both-form applicability. It is a restriction of A, not evidence that A's missing absent-case entailment exists. Keeping everything blocked is the safe current operational state, not an answer that unlocks the bounded question.

## Before/after accepted-input matrix

“Valid” below means the Factory predicate, not that an implemented V2 publication path exists. “Before” separates current executability from normative meaning. E-new entries are a proposed contract, not observed implementation. All accepted results retain the **actual** Factory fingerprint; no projection is republished to change provenance.

| Factory policy / represented content | Factory-valid? | Before: v1 definition and implementation | Candidate A after | Candidate B after | Rules, refusal and output identity |
| --- | --- | --- | --- | --- | --- |
| V1 production records; spatial content unrepresentable | Yes if V1 predicates hold | Defined production-only behavior; publishable and executable | Same admission and behavior | v1 remains unchanged; E-new can explicitly include this domain | Current dispatch/decomposition/session/metrics; no transfers. V1 fingerprint + actual executing Engine identity; invalid workload still rejected before mutation. |
| Reconciled V2 + complete spatial record, nonzero magnitudes | Yes if production, coverage, layout and arithmetic predicates hold | Five-fact spatial rules defined; applicability to reconciled policy still open; no executable/publication path | Claims existing v1 rules suffice; fact-level preservation supported, full policy-domain proof not complete | Explicitly accept under E-new once implemented | Existing selection then binding/reservation; transfer formula and supported transitions. V2 fingerprint + E-new. v1 remains unchanged, not automatically granted new implemented support. |
| Reconciled V2 + complete spatial record, both magnitudes zero | Yes | Defined zero-duration transfer semantics for the spatial facts; same current applicability/path gap | Must retain scheduled transfer completion and transfer events, not collapse to absence | Explicitly accept under E-new with those same distinctions | Timing can match production-only in an isolated case; event sequence, intermediate state and bounded advancement differ. V2 fingerprint retains present record. |
| Reconciled V2 + absent spatial record | Yes if production predicates hold | No rule establishing v1 admission of this V2 case; no executable/publication path | Adds admission plus a no-transfer branch while calling it already fixed: preservation fails | Explicitly accept under E-new; no floor/position/handling defaults | Ordinary production execution, no transfer state/events. Actual spatial-absent V2 fingerprint + E-new, never a V1 fingerprint. |
| V2 + partial spatial record, overlapping footprints or arithmetic violation | No | Model validation rejects; no valid published artifact | Must keep invalid, never repair by dropping/defaulting content | Same | Factory validation/strict artifact verification rejects before runtime; no successful run/result attribution. |
| Valid artifact outside the selected Engine's declared policy/content domain | Yes under its own policy | Factory validity does not confer Engine support; runtime currently represents only V1 | Must refuse; automatic “known production fields” acceptance would further expand A | Refuse before runtime mutation | Distinguish unsupported Engine applicability from invalid Factory content. Failure diagnostics preserve the requested identities without producing a successful run. |
| V1 artifact, but requested unsupported Engine identity | Yes | `requireSupported` rejects the identity; current runtime does not expose this request parameter | Same refusal obligation | Refuse unsupported identities; explicitly select any genuinely supported pair | No fallback to CURRENT. Historical representation of an identity in Governance is still allowed. |
| Counterfactual future closed Factory policy with an additional irrelevant optional authored record | Assumed valid solely for this test | No automatic forward applicability in v1 | A may not infer acceptance from familiar fields; would need a separate preservation proof | E-new supports only what its exact definition admits; refuse outside it | New Factory label alone neither supplies support nor establishes changed numerical behavior. No silently ignored record or erased source fingerprint. |

## Definition-preservation analysis

The burden is equality of the complete interpretation contract, not equality of a projection or a few observed completion times. Implemented coverage is a subset of defined behavior; adding an implementation for behavior already defined can preserve identity. Adding a newly admitted policy/content case is a different proposition.

| Fixed aspect | Evidence supporting A | Missing or contrary evidence | Verdict |
| --- | --- | --- | --- |
| Accepted input domain | V1 production semantics are unchanged in V2; v1 already names the five V2 spatial facts | The earlier V2 definition required those facts; v1 supplies no policy-parametric rule admitting V2 without them. The present Factory contract explicitly requires Engine-owned applicability, not merely recognizable fields | Preservation fails for both-form admission |
| Result-affecting interpretation | Reuse of the non-spatial algorithm and of §§2–10 spatial rules is feasible | On V2 absence, “skip transfer entirely” versus “refuse unsupported input” is an outcome choice not resolved by v1's existing supported V1 behavior. Supplying zero handling would also violate Factory absence | Feasibility proved; existing entailment not proved |
| Outputs and observations | Same production projections can be produced; spatial-present rules already define transfer observation/event obligations | Absent and zero-duration present differ in events, reservations and advancement turns. Equal timing does not prove full observation equivalence. Current envelopes also lack required Engine provenance | No complete preservation proof |
| Refusal semantics | Existing invalid model/workload and unsupported-version refusals can be retained | Enabling the absent V2 case changes it from outside established applicability to successful execution. No observed Java exception is claimed for a type the API cannot accept. The difference is the defined admission boundary | Cannot call the new admission a demonstrated correction |
| Referenced semantic definitions | Five spatial fact meanings, minimum-coordinate anchor, non-overlap and maximum-duration predicate survive for present content | The referenced Factory grammar's domain, marker and absence case changed. A live document link does not make fixed Engine support follow future grammar changes | Present-fact preservation is real but insufficient for whole-definition preservation |

**Inference:** A is not rescued by composing two individually familiar algorithms. A complete interpretation includes the predicate selecting which artifacts may reach each algorithm and the identities retained in their outputs. A rule selecting production-only behavior for this V2 absence case is exactly the missing addition. Under the brief's “prove correction or use a distinguishable identity” constraint, B is the surviving route for the full domain.

This does **not** prove v1 must reject every future representation forever, or that every decoder improvement changes Engine semantics. An already-fixed definition could explicitly admit multiple policies or an extension rule; an implementation could later cover more of that domain without changing its definition. No such rule establishing this absent V2 case was found here.

## Proving cases

These are contract discriminators. Present-state tests corroborate existing mechanics; proposed spatial outcomes are deductions from the specification, not claims of execution by the current runtime.

Use a simple witness: two unary machines, one quantity-one order at time 0, route `M1:2 -> M2:3`, no competing work or external commands. Where spatial content is present use floor 3×1, non-overlapping 1×1 footprints at `(0,0)` and `(2,0)`. Production identifiers, order and values are identical between variants.

| Case | Discriminating evidence / expected consequence | A | B |
| --- | --- | --- | --- |
| V1 baseline | Current no-transfer execution completes at tick 5; session/version/model provenance stays fixed and reset uses a new run ID | Survives; already covered domain | Survives; preserve v1, and preserve numerical behavior if E-new includes V1 |
| V2 spatial-present | With `ticksPerCell=4`, `handlingTicks=1`, duration is 9: step one completes at 2, transfer arrives at 11, order completes at 14 | Transfer calculation preserved; complete reconciled-policy applicability not thereby proved | Survives with explicit admission and retained rules |
| V2 spatial-absent | Factory-valid production records, no position/rate/overhead exists. Proposed production-only outcome is tick 5, with no transfer events | Fails affirmative preservation: reusing V1's projection is not proof of admitting this exact policy/content pair | Survives by explicitly defining this additional domain and behavior |
| Present legal zero versus absence | Set both handling magnitudes to 0: spatial transfer still starts and completes on a separate scheduled turn at 2, then processing completes at 5. Absence has no such turns/events | Any normalization of absence to zero fails; faithful A still has its applicability gap | Survives only if it preserves separate branches, state and provenance |
| Incomplete/invalid representation | Remove M2's layout from a present record; or overlap footprints; or choose floor/rates that overflow the maximum-duration predicate | Both must reject as Factory-invalid, not as a supported approximation | Same |
| Valid but unsupported artifact | A valid absent V2 cannot enter today's V1-only publication/assembly path. More generally, a verified artifact outside an Engine's definition must be refused before runtime mutation | A's new absent-case admission is the semantic difference; no inherited-support assertion repairs it | Explicit gate rejects outside domain; valid V2 acceptance is under E-new |
| Equivalent production under two Factory policies | V1 and absent V2 can have identical production records/outcomes yet distinct prefixes/fingerprints | Must retain actual fingerprint; republishing V2 projection as V1 would fail | Survives; same Engine may explicitly admit both, comparisons do not collapse identities |
| Refusal layering | Unknown Engine value, wrong-policy bytes, invalid content and valid-but-inapplicable content are distinct boundaries | Existing support helper proves only the first, V1 codec the second; neither proves new applicability | Requires explicit pair/content gate as well as validation; no silent CURRENT fallback |
| Five-fact interactions | Footprint/floor constrain validity but footprint is not distance; anchor positions and handling scalars drive time. Same-machine consecutive steps have no transfer. Distinct same-reference resources are invalid | Spatial facts/rules survive at their existing scope; no justification for dropping checks in absent/present dispatch | Survives if copied precisely, including checked duration versus runtime time-addition distinction |
| Binding and recovery | Multi-eligible destination becomes offline after departure: fixed arrival time, convert reservation to bound destination FIFO, no shared-backlog rerouting; exclude transit from busyTicks and activeJobIds | Existing spatial obligations cannot be edited away just because unimplemented | Survives by retaining interactions and proving them later |
| Historical v1 interpretation | Retained v1 definition includes spatial rules as well as tested production behavior; current lack of a durable run store does not make them mutable | Broad same-label amendment fails; historical definition would acquire an unproved new admission branch | Survives with v1 definition/attribution retained and a separate successor |
| Future irrelevant optional content | A hypothetical new closed policy adds authored content irrelevant to the retained production formula | Familiar projection alone must not confer v1 support; otherwise A silently invents forward compatibility | Survives as explicit applicability, not matched version numbers or a wildcard promise |

The numerical witness distinguishes reusable computation from admission, and absence from zero. It is not a performance experiment or a universal assertion that zero-transfer scheduling preserves every competing-work trace. Equal-time interleavings need their own future execution fixtures.

## External evidence

No external source is load-bearing or cited. External versioning conventions, schema-evolution precedents and optional-field analogies cannot establish what this fixed Arcogine definition already admits; that is the actual disputed premise. Repository specification, historical dependency delta, implementation/refusal boundaries and executable fixtures supply distinct relevant evidence here. No interoperability or broader universal amendment claim is made. This is a deliberate sufficiency decision, not a claim that external practice uniformly requires new identities.

## Adversarial analysis — author self-challenge only

1. **“No spatial facts means no spatial behavior, universally.”** Factory establishes absence of assertion, and explicitly establishes non-spatial V1 execution. It does not require every Engine to accept every valid absent-record artifact. Refusal is the counterexample. The Engine must own the supported case before no-transfer execution follows.
2. **“The two algorithms already exist in the definition.”** True at their existing input boundaries, and a strong argument against gratuitously redesigning the algorithms. It does not establish the union's revised policy applicability or identity-preserving admission rule.
3. **“Nothing attributed to v1 could have exercised spatial rules.”** Even if true of every repository test, whole-definition fixation expressly includes unexercised sections. No store search can waive that constraint. Governance has representation seams carrying the identity as well.
4. **“Any current refusal changing to acceptance demands a version.”** Too broad: an incomplete implementation can acquire already-defined behavior without changing the semantics identity. The conclusion relies on the absent V2 case missing from the fixed definition, not merely its absence from today's Java API.
5. **“A new prefix or presence byte forces a new Engine.”** Too broad: encoding belongs to Factory and the Engine consumes semantic facts. The changed accepted domain and unresolved referenced-definition applicability are decisive, not the byte change alone.
6. **“Preserve v1 for present-only and do nothing for absence.”** This is a coherent restricted support proposal to assess separately if product scope changes. It does not establish A for both cases. No proof here declares all present-only correction arguments impossible.
7. **“E-new means a new scheduling policy or simultaneous permanent executors.”** Neither follows. Preserve existing rule meanings; explicitly scope actual execution and retirement support. Retain v1 meaning and required evidence without promising eternal execution.
8. **“The successor proves arbitrary future optional concerns are safe to ignore.”** It does not. The future-policy case fails that generalization. Any applicability extension still needs the owning contract and relevant interactions.

## Surviving invariants and what did not survive

- Factory-valid does not imply Engine-applicable, and Engine-applicable does not imply implemented today.
- Exact policy/content admission is part of the complete interpretation; a shared production projection is not an admission proof.
- Absent spatial content is never synthesized; present zero remains spatial content with distinct transition semantics.
- An actual source fingerprint survives execution and comparison. Model equivalence for a specific claim never authorizes reattribution.
- Fixed v1 includes its defined unimplemented rules. A successor may share them without changing their historical meaning.

The failed hypotheses worth preserving are: fixture silence makes sections mutable; current type exclusion proves a permanent normative exclusion; equal completion time proves full semantic equivalence; and identical production fields authorize acceptance across policies. The first and last would incorrectly authorize same-label expansion; the second would incorrectly forbid ordinary conformance implementation; the third loses events, reservations and advancement semantics.

## Fixture requirements

### Evidence needed for this research conclusion

The conclusion needs the exact before/after definition comparison, the current type/refusal boundary, the presence/absence validation distinctions, the identity/provenance inventory and the explicit failed entailment in the preservation table. Existing identity, V2 shape/validation, v1 dispatch/derived-result/session and Governance evidence fixtures corroborate those observations. No invented E-new implementation can prove that E-new's admission rule was already part of v1.

The future transfer traces above are specification-derived witnesses. Their absence from current executable tests is disclosed and is not the reason A fails. Test execution and limitations are recorded below.

### Later reconciliation and implementation evidence

Before publishing execution support, the selected identity needs:

1. **Supported-input fixtures:** policy/content matrix, V1 if included, V2 with complete spatial record and V2 with absence, empty/minimal valid designs, supported value boundaries and publication/decoder identity checks. V2 golden bytes/digests and cross-policy decoder rejection belong to Factory publication work, not this report.
2. **Non-spatial behavior fixtures:** equivalent production V1/absent-V2 numerical and ordered semantic observations, no fabricated spatial values/events, immutable original fingerprints, fresh run IDs, version-preserving reset and bounded advancement. This is explicit comparison evidence, not fingerprint equality.
3. **Spatial interaction fixtures:** nonzero transfer, same-resource no transfer, zero-duration scheduled completion, deterministic equal-time interleavings and bounded advancement; selection before binding; admission reservation affecting later selection; destination offline at arrival; bound FIFO recovery rather than shared reselection; coherent job/resource observation, active work, queue depth and processing-only busyTicks. Retain decomposition, quantity limits, dispatch order, exact ranking, accumulator and command fault/rejection rules.
4. **Rejection fixtures:** unknown Engine identity; supported identity with unsupported Factory policy or represented variant; incomplete layout; unknown/duplicate/missing resource placement; overlap; out-of-bounds layout; negative magnitudes; duration overflow, including overflowing intermediate distance multiplied by zero; valid duration added to extreme current time. Identify the Factory-versus-Engine failure boundary and assert no runtime mutation for pre-run applicability refusal. Do not change post-mutation command Faulted into Rejected.
5. **Identity/provenance fixtures:** actual selected Engine identity on runtime, observation and each supported event; actual policy fingerprint; reset retains the selected interpretation; unknown historical provenance remains unknown; historical v1 evidence keeps its exact attribution; Governance admission does not demand that a historical version remain executable.
6. **Cross-policy/support fixtures:** reject unsupported future content rather than project/drop it; compare only stated semantics under stated inputs; demonstrate explicit refusal after an execution-support transition without rebinding historical identity.

These are requirements for later promised support, not implementation admission performed by this report. The full v1 §14 fixture obligations remain relevant to any assertion of full v1 conformance; recommending E-new does not erase outstanding v1 promises.

## Compatibility and provenance consequences

| Concern | Candidate A | Candidate B |
| --- | --- | --- |
| Historical v1 results | Safe only if the entire correction proof succeeds; unchanged observed old traces alone are insufficient | Keep original definition, actual fingerprint, Engine identity and explicit inputs; do not relabel old records |
| Replay / interpretability | A missing retained definition cannot be replaced by whichever Factory page the link now resolves to | Retain exact v1 semantic basis for required horizons; executor retirement is explicit and does not erase meaning |
| Consumer comparison | Equal version still does not prove equal workload or experiments; cross-policy model equivalence remains explicit | Different Engine identities do not prohibit a claim-specific equivalence proof. Shared production rules can justify bounded comparisons; spatial-versus-absent equivalence cannot be assumed |
| Support / retirement | A would still need declared scopes, codecs, fixtures and provenance propagation | Same obligations plus explicit successor applicability; no requirement for permanent parallel engines, generic migrations or continued V1 publication forever |
| V2 release / spatial activation | Cannot proceed on the failed both-form correction claim | Remain blocked until independent review and Engine reconciliation land; then publication/execution implementation and promised-support evidence are still necessary |

A proposed E-new could accept both V1 and V2, so the recommendation does not couple Factory and Engine version numbers. Whether to execute V1 under E-new, keep a v1 executor, or retire a particular implementation's v1 support is a later scoped declaration accounting for accepted uses. It must not change a running session's version or silently execute a requested old version using the successor.

## Confidence, limitations and unresolved unknowns

**High confidence** that the full V2-domain preservation proof fails and a distinguishable identity is the bounded route under the stated rules. **Moderate confidence only** about the ultimate outcome of a separately scoped spatial-present-only correction: fact-level preservation is strong, while complete artifact-policy/refusal/reference preservation has not been established. The recommendation does not depend on declaring that narrower route impossible.

No external consumer stores or unpublished retention commitments were audited. There is no proven production Engine-result integration in the inspected Governance implementation. Current provenance propagation and spatial execution are incomplete. Factory V2 canonical artifact operations cannot be tested because they do not exist. These gaps limit release/readiness claims, not the specific failed entailment.

Open decisions for later work:

- the successor's concrete identity, exact applicability declaration, and whether its execution scope includes V1;
- support transition for existing v1 callers and the separate continuation of V1 publication;
- a precise preserved dependency basis for v1's historical spatial reference, without resurrecting obsolete Factory publication bytes;
- runtime establishment refusal/API shape and completed provenance propagation;
- any measured consumer/maintenance cost sufficient to activate the Factory-composition reopening trigger.

A newly discovered fixed Engine-owned rule explicitly admitting this V2 absence case, with unchanged acceptance/refusal and referenced-definition semantics, would reopen the conclusion. A new test written to assume that rule would not be such evidence.

## Durable consequences and implementation implication

**Proposed destination:** a separate Engine architecture/specification reconciliation on this same workspace after independent adversarial review. It should leave v1's definition intact, define a distinguishable complete interpretation with explicit policy/content applicability, preserve actual Factory provenance, and reconcile dependent runtime/support/planning statements. It must resolve the dependency basis and declared support before admitting retained successor records. No code or canonical text is changed in this investigation.

The Factory-composition reopening trigger must be considered, not silently dismissed: a successor identity adds specification, provenance and support-transition work. However, the repository already requires fixed identity, refusal, propagation and conformance machinery; this conclusion does not itself require multiple permanent executors or a generic migration system. No inspected consumer requirement or cost evidence demonstrates that these incremental obligations overturn the optional-record composition choice. Reopen that choice before V2 release if concrete cost/value evidence does demonstrate it; do not revert to mandatory spatial records as an unreviewed workaround here.

**Knowledge-transfer recommendation:** preserve the applicability matrix and absence-versus-zero event/advancement discriminator in the owning specification and later fixtures; retain the definition-preservation argument and its present-only qualification in concise decision rationale if reconciliation confirms them. Keep unresolved support decisions/reopening triggers in their owning surfaces. Exact report readability after retirement is not independently required if that transfer is complete. Logs, search inventories and test-download diagnostics are disposable. No synthesis-seed claim is needed; this is a local application of established identity constraints. This workspace is not retirement-eligible until reconciliation and its transfer audit land.

**Next phase:** a genuinely independent adversarial researcher reviews this exact committed report, reconstructing constraints and alternatives before reading its recommendation deeply. The current run performs no independent review and no architecture reconciliation.

## Validation and source map

**Executed:** exported the exact baseline with `git archive` into an ignored `logs/research-baseline` tree and used the documented generic Docker/JDK 21 workflow. The running devcontainer and existing build container mounted another checkout, so they were not used as evidence for this revision. A one-off `gradle:9-jdk21` container mounted the baseline at `/repo`; from `/repo/product` it invoked the repository wrapper with:

```text
sh ./gradlew :types:test --tests com.arcogine.types.EngineSemanticsVersionTest
  :factory:test --tests 'com.arcogine.factory.model.v2.FactoryModelV2*Test'
  --tests com.arcogine.factory.process.EngineSemanticsIdentityAcceptanceTest
  --tests 'com.arcogine.factory.process.EngineSemanticsV1*Test'
  --tests com.arcogine.factory.process.SessionControlAcceptanceTest
  :governance:test --tests com.arcogine.governance.evidence.GovernanceEvidenceTest
  --no-daemon --console=plain
```

The displayed command is line-wrapped for readability; arguments were passed in one invocation. **Result: BUILD SUCCESSFUL, 14 tasks executed; 103 tests, zero failures/errors/skips**, verified from the generated JUnit XML across eight suites: identity type 3, V2 shape 4, V2 validation 40, runtime identity 3, v1 dispatch 17, v1 derived results 3, session control 13, Governance evidence 20. The tests executed against the exact baseline, not the older custody checkout. The log is local disposable material at `logs/engine-applicability-tests.log`; the commands, revision and totals here carry the reproducible result.

**Document verification:** checked report scope, source locators, the candidate/case matrix and whitespace with `git diff --check`. Git searches and the historical diff confirmed the evidence revision and dependency change.

**Not run / not claimed:** repository-wide Java style/coverage/security gates (this change is a research Markdown artifact), a V2 codec/runtime test (no such implementation exists), E-new conformance, or independent adversarial review. Passing the selected baseline suites proves none of those missing capabilities. No production source or test was edited.

All following repository paths are scoped to baseline `7e5e6ad2f8a2707d00fff3e2a4e3db96da85852a`, except the explicitly named historical comparison. They can be retrieved with `git show <baseline>:<path>`. The branch checkout is evidence custody based on an older main, so its unqualified working-tree documents are not substitutes for these baseline sources.

| Source group | Exact repository paths and load-bearing locators |
| --- | --- |
| Architecture/support | `docs/architecture/overview.md`: Semantic evolution and support, Determinism Contract, Factory Model Identity; `docs/development/semantic-contract-support.md`: declarations and Engine discriminating cases |
| Engine specification | `docs/architecture/engine-semantics-v1.md`: §§1–1.2 identity/completeness/session, §§2–4 existing rules, §§5–9 five-fact consumption/transfer, §§10–12 results and provenance, §14 conformance |
| Factory specifications | `docs/architecture/factory-design.md` §11.1; `docs/architecture/factory-model-v2.md` §§1.1–2, 6, 9–11; `docs/architecture/factory-model-v1.md` as the unchanged production/fingerprint counterpart |
| Runtime/Governance contracts | `docs/architecture/runtime-contract.md`: mandatory provenance and retained history boundaries; `docs/architecture/governance-evidence.md` §§5, 7, 9, 11–13 |
| Research and gates | `docs/research/research-register.md`; `docs/research/investigations/engine-evolution.md`: Engine applicability; `docs/research/investigations/factory-model-semantic-composition.md`: conclusion/reopening triggers; `docs/research/investigations/semantic-contract-maturity-durability.md`; `docs/planning/spatial-runtime-consequences.md`: remaining prerequisite; `docs/planning/factory-simulation-engine-readiness.md`: fixed identity/current queue |
| Product and method | `docs/product/charter.md` §§2–6; `AGENTS.md`; `.github/CONTRIBUTING.md`; `.github/agents/researcher.agent.md`; `docs/development/researching.md`; `docs/research/report-template.md`; `docs/development/testing.md` |
| Identity types | `product/types/src/main/java/com/arcogine/types/EngineSemanticsVersion.java`; `product/types/src/main/java/com/arcogine/types/ModelFingerprint.java`; `product/types/src/test/java/com/arcogine/types/EngineSemanticsVersionTest.java` |
| Publication/assembly | `product/domains/factory/src/main/java/com/arcogine/factory/model/FactoryModelVersion.java`, `FactoryModelPublisher.java`, `FactoryRuntimeAssembler.java`, `FactoryModelArtifactV1.java` in that same directory |
| V2 implementation | `product/domains/factory/src/main/java/com/arcogine/factory/model/v2/FactoryModelV2.java`: constructor/baseModel; `FactoryModelV2Validator.java` in the same directory: validate/coverage/layout/max-duration; corresponding `product/domains/factory/src/test/java/com/arcogine/factory/model/v2/FactoryModelV2Test.java` and `FactoryModelV2ValidatorTest.java` |
| Runtime implementation | `product/domains/factory/src/main/java/com/arcogine/factory/process/FactoryRuntime.java`: constructor/forModel/reset/observe/emit/drainSupportedEvents; `FactoryHandler.java`: handleTaskEnd; `CommandResult.java`, `RuntimeObservationMetadata.java`, `RuntimeEventEnvelope.java` in the same directory |
| Runtime executable evidence | `product/domains/factory/src/test/java/com/arcogine/factory/process/EngineSemanticsIdentityAcceptanceTest.java`, `EngineSemanticsV1DispatchConformanceTest.java`, `EngineSemanticsV1DerivedResultConformanceTest.java`, `SessionControlAcceptanceTest.java` |
| Governance executable evidence | `product/governance/src/main/java/com/arcogine/governance/evidence/EvidenceProvenance.java`, `EvidenceReference.java`, `InMemoryEvidenceReferenceAuthority.java`; `product/governance/src/test/java/com/arcogine/governance/evidence/GovernanceEvidenceTest.java`: missing Engine attribution, retired producer, rebinding and contextual use fixtures |
| Transitive evidence and artifact custody | `product/governance/src/main/java/com/arcogine/governance/evidence/EvidenceUse.java`; `product/governance/src/main/java/com/arcogine/governance/conformance/EvaluationOccurrence.java`; `product/governance/src/main/java/com/arcogine/governance/FileControlledRevisionAuthority.java`: artifact validation and persisted revision fields; `product/governance/src/main/java/com/arcogine/governance/SemanticArtifactVerifier.java` |
| Historical dependency delta | `git diff 1ab991b6e0d9529de35c845cbd0995bb2408af8c 2e13eb5fcf71823740453b1c80d0e792fa38a746 -- docs/architecture/factory-model-v2.md docs/architecture/engine-semantics-v1.md`; old V2 §1.1 inspected directly at the parent |
