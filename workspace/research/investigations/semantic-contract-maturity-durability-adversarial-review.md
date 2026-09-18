# Independent adversarial review: Semantic contract maturity and durability

## Review identity and input integrity

- **Reviewed report:** `d0b2c5f127d340ecb4816d6316f5028190c23a13:workspace/research/investigations/semantic-contract-maturity-durability-report.md`
- **Workspace branch:** `research/semantic-contract-maturity-durability`
- **Report-stated research baseline:** `46389bc5d21e0c95ffa9366ce271f9d943d44b35` (report header and final recheck both name it)
- **Live-`main` review baseline:** `46389bc5d21e0c95ffa9366ce271f9d943d44b35`, resolved 2026-09-18 from `origin/main` and confirmed through the GitHub branch API. **No drift** between report baseline and review baseline; historical and current claims are judged against the same tree.
- **Input integrity:** the report resolves at the exact commit (author Vasile Alaiba, 2026-09-18, 291 inserted lines, one file). It is complete: header/authority, question, executive conclusion, repository evidence, external evidence, candidates and cases, minimum rule and evidence gates, sixteen-question answers, current-contract classification, self-challenge, surviving invariants, confidence/limitations, consequences, and falsification triggers all present, ending with the next-action statement. Not inferred from the handoff prompt.
- **Review method:** `AGENTS.md`, `.github/agents/researcher.agent.md` (adversarial mode), `docs/development/researching.md` §9–§10, the maintained brief, and the report template used only for completeness checks.

## Independence statement

**Independence condition: achieved as a fresh isolated session in a different model family, with no responsibility for defending the report.**

- This review runs in a fresh Claude (Opus 5) session that had no part in producing the report and holds no prior context about it.
- The report does not self-declare its authoring model. Environmental evidence indicates a different model family: the workspace branch's authoring checkout is a Codex worktree (`.codex/worktrees/.../arcogine`, checked out at the report commit `d0b2c5f`). That is circumstantial provenance, not a repository record; the fresh-isolated-session condition holds independently of it.
- Anchoring control was followed: the operating contract, register, and brief were read first; live repository authorities were re-grounded; §2 below was written before the report's reasoning and recommendation were read in depth. Only the report's identity, line count, section headings, header block, and last six lines were consulted before that point.

## 1. Repository grounding performed before reading the report

Surfaces read at the review baseline (all live `main` unless stated): Product Charter (principles §6–§8, authority model); Architecture Overview (principles, determinism contract, "Factory Model Identity (current state)"); ADR-0003 (via ADR-0004 context), ADR-0004, ADR-0006, ADR-0008, ADR-0011 (stabilization/retention passages), ADR-0012, ADR-0013 (§5, §14), ADR-0014, ADR-0015, ADR-0016; `docs/architecture/decisions/README.md` (immutability, amendment, supersession, CI guard); Factory Model v2 canonicalization (whole document, especially §10 lifecycle); Engine Semantics v1 (§1, §13, §14); Governance architecture §4, §13; Operational architecture §15–§16; `docs/planning/spatial-runtime-consequences.md` (research hold, slice statuses 5-0, A1, A2, A3, B1, B2); the sibling Factory composition brief header; the ADR-0016 reconciliation PR #347 as the completed high-risk research → independent review → reconciliation precedent; implementation and tests (`EngineSemanticsVersion`, `FactoryModelFingerprintV1`, `FactoryModelArtifactV1`, `FactoryModelV2` package, `RuntimeObservationMetadata`, `RuntimeEventEnvelope`, `FileControlledRevisionAuthority`, `SemanticArtifactVerifier`, `SimResult`/`IntegratedHandler` legacy hash, `EngineSemanticsV1DispatchConformanceTest`, `EngineSemanticsV1DerivedResultConformanceTest`, `EngineSemanticsIdentityAcceptanceTest`, `FactoryModelFingerprintV1Test`, Challenge `EvaluationPolicyIdentity`); repository history for ADR-0006/0014/0015 status transitions and implementation landing dates; GitHub repository metadata (visibility, forks, stars, releases, tags, CI publication).

Quick search performed across docs, product, tests, and history for: `semantic contract`, `durability`, `durable`, `immutable`, `provisional`, `compatibility`, `provenance`, `factory-model:v1`, `factory-model:v2`, `engine-semantics:v1`, `ModelFingerprint`, `ControlledRevisionId`, `EngineSemanticsVersion`, `golden vectors`, `historical resolution`, `outward compatibility`, `release`, `stabilization`, `support`, `retention`. Search hits were treated as discovery; every load-bearing claim below was read at the review baseline.

Surface that could not be inspected: the GitHub Packages registry for the owner (API returned 403 without `read:packages`). No CI workflow publishes images or packages (grep of `.github/workflows` for registry pushes found none), so the residual risk is a manually published package; recorded as uninspected, not as absent.

### 1.1 Repository facts established independently (before reading the report)

Labelled **Repository fact** unless stated.

1. **Current durability trigger is "first shipped implementation / first recorded artifact", not ADR acceptance.** ADR-0006 "Released fingerprint policies are immutable": "Once `factory-model:v1` ships in implementation…"; Factory Model v2 §10: "Until the `factory-model:v2` implementation ships, this grammar is a normative design contract and may be corrected by amending this document. Once a V2 fingerprint is produced by a shipped implementation or recorded against a controlled revision, the grammar is frozen permanently." ADR-0015 decision 12: "Released semantics versions are immutable and never reused"; decision 5/13 speak of "released" versions. Engine Semantics v1 §14: "Before `engine-semantics:v1` is considered released, pinned behavioral fixtures must prove…". So the repository's eager model is already ship-gated in text; what it lacks is any definition of "ship/release" beyond implementation landing on `main`.
2. **ADR-0006 was Accepted at introduction (2026-08-27, #175) and implemented the next day (#181, 2026-08-28).** ADR-0014 and ADR-0015 were introduced Proposed (2026-09-02, #248) and Accepted two days later (2026-09-04, #252). V2 model/validation landed 2026-09-11 (#299); Engine v1 conformance pins 2026-09-16 (#341); `EngineSemanticsVersion` 2026-09-18 (#345). Proving periods between acceptance and freezing were therefore days, and for ADR-0006 effectively nil.
3. **The repository already contains a worked "provisional identity policy" precedent: `FactoryModelVersion.contentHash()`.** ADR-0004 explicitly keeps it provisional, lists what must be specified before promotion (canonicalization, ordering, field membership, format versioning, compatibility guarantee, equality relationship), and ADR-0006 minted the durable contract separately rather than promoting the provisional hash. The legacy hash still flows through `IntegratedHandler`/`SimResult.modelContentHash` today; migration has not happened. This is an existing, repository-native evidence gate for fingerprint promotion.
4. **The outward surfaces do not expose the semantic identities.** `docs/reference/api.md`, the API controllers, CLI, and web contain no fingerprint, `engine-semantics`, or `EngineSemanticsVersion` exposure; the API only carries the legacy content hash internally. `RuntimeEventEnvelope` and `RuntimeObservationMetadata` carry `ModelFingerprint` (and optional `ControlledRevisionId` on the envelope) but **not** `EngineSemanticsVersion`; the semantics version is exposed only from `FactoryRuntime` (plan slice B2 not implemented; Overview says propagation "remains follow-up work").
5. **Constituency evidence:** the repository is public with 0 forks, 0 stars, 1 watcher, 0 releases, 0 tags, and no CI image/package publication. `FileControlledRevisionAuthority` (append-only file store with `arcogine-revision-store-v1` / `arcogine-semantic-artifact-v1` magic) exists in `:governance` but is referenced only by governance tests and `ConformanceEvaluator`/`ChangeSetFactory`; no API/CLI entry point constructs it, so no product path persists revisions outside test temp directories. Exactly one literal `factory-model:v1` fingerprint is pinned, in `FactoryModelFingerprintV1Test`.
6. **V2 status:** `FactoryModelV2`, `FactoryFloor`, `ResourcePlacement`, `ResourceFootprint`, `SpatialConfiguredResource`, `FactoryModelV2Validator` exist; no V2 canonical bytes, fingerprint, verifier, or policy registration exist (`FactoryModelV2` Javadoc and plan slice A2 "dependency-blocked"). `FactoryModelV2` shares no supertype with `FactoryModel`, so V2 content cannot travel the v1 publication path.
7. **Engine v1 status:** spec header "Normative design contract; implementation pending"; §14 lists 18 fixture families; dispatch (1–6), derived-result (15–16), ranking exactness (17), session/control (12), and identity tests exist; spatial/transfer fixtures (7–11, 13) have no implementation to exercise because no V2 runtime exists. `EngineSemanticsVersion.CURRENT = engine-semantics:v1` is emitted for every runtime today.
8. **ADR-0015 already scopes Engine durability narrowly:** decision 14 "attribution plus a verifiable definition, not permanent exact re-execution"; decision 16 "Retirement removes executability, not provenance". By contrast ADR-0014 decision 8 promises **permanent decoder retention** for every released Factory policy ("Factory must retain the verifier/decoder necessary to resolve every released Factory model policy"). The two Accepted contracts therefore already carry different durability promises for different boundaries.
9. **ADR-0012 §4/§14** defers outward HTTP/OpenAPI compatibility until the domain contract stabilizes and lists the evidence a representation needs to become a supported compatibility surface. **ADR-0009 §4** and **ADR-0011** context also sequence "stabilize the supported observation/event contract" after semantic prerequisites — a second internal stabilization-ordering precedent.
10. **ADR-0016** distinguishes accepted logical invariants (non-rebinding, occurrence identity, exact definition resolution) from deliberately deferred representation (§13) and bounds first-implementation claims (§12) to producer identities that actually exist. **ADR-0013** settles acceptance semantics (§5) while deferring representation, storage, and closure/retirement (§14). Both are Accepted decisions whose *representations* are explicitly provisional.
11. **ADR README** makes Accepted ADRs semantically immutable, with semantics-preserving amendments permitted and supersession required for decision changes; CI enforces the mechanical part.
12. **Governance §4/§13 and Overview** describe the file authority's "record layout and locking mechanics" as "replaceable adapter details, not a selected permanent production persistence architecture".
13. **Planning:** the spatial plan carries a research hold; slices A2 (V2 canonical identity), A3 (multi-policy resolution), and spatial activation are dependency-blocked on this research and the composition research.
14. **Precedent:** PR #347 reconciled ADR-0016 from the governance-evidence research after `ACCEPT WITH QUALIFICATIONS`, with the workspace artifacts removed from the tree — the intended prove → independent review → reconcile lifecycle has executed once.

## 2. Pre-report reconstruction (written before reading the report's reasoning)

### 2.1 Distinctions the question requires

1. **Artifact immutability vs. contract-meaning preservation.** An accepted `ControlledRevision`, a published `FactoryModelVersion`, or an accepted evaluation occurrence is immutable as an occurrence (ADR-0008, ADR-0013 §5, ADR-0016 §8). Whether the *policy* that defines such artifacts (`factory-model:v1` grammar, `engine-semantics:v1` rules) keeps its meaning is a separate question.
2. **Current normative authority vs. durable commitment.** "Current" = the one meaning implementations and consumers must agree on now (supersession changes it). "Durable" = Arcogine promises future interpretability and/or support.
3. **Identity/equality contract vs. behavioral contract.** A fingerprint policy defines equality; an Engine semantics version defines outcomes. Equality contracts are referenced by persisted digests and cannot be "re-run"; behavioral contracts can be re-specified and re-executed.
4. **Definition preservation vs. executability vs. outward transport compatibility vs. physical retention.** The repository already separates these: ADR-0015 §14/§16 (definition + fixtures retained; executability retirable); ADR-0014 §8 (decoder retained permanently); ADR-0012 §14 (outward surface needs its own evidence); ADR-0011 (bounded retention with gap detection); ADR-0016 §9 (retention may be bounded, disclosure required).
5. **Repository-controlled proving artifacts vs. externally held artifacts.** Test fixtures, temp-directory stores, and golden vectors are under repository control and can be regenerated with the change recorded; artifacts held outside the repository cannot.
6. **Introduction / ADR acceptance / implementation landing / release / support declaration** are five different events that the current text mostly collapses into "ships".

### 2.2 Candidate lifecycles (independently enumerated)

- **A — eager (current):** durable at first shipped implementation or first recorded artifact. Strongest defense: a digest-bearing identity can only stay truthful if its grammar never changes, so the moment any digest exists the grammar must be fixed; the repository already has namespaced policy versions, so a new version is cheap; "named ⇒ fixed" is the simplest rule to reason about and to audit; ADR-0014 §8 retention cost is already accepted. Weakness: "ships" is undefined (landing on `main` of a repository with no releases counts), the proving window is days, and V2 is being minted under linear whole-model assumptions that the sibling research questions.
- **B — explicit promotion, always separate:** durable only through a later, separate decision after implementation/consumer/conformance/research evidence. Strongest defense: it is a forcing function; the repository's own history shows acceptance and freezing collapsing into days, and a mechanically separate transition creates a record of the evidence relied on. Weakness: if a contract already meets every gate at introduction, a mandatory later ceremony changes no decision; and B does not by itself say what "provisional" persisted artifacts may do.
- **C — boundary-specific triggers with shared minimums:** fingerprint/equality, Engine interpretation, outward transport, Governance evidence, Operational records each earn durability through their own evidence, under repository-wide floor rules (exact historical referents never rebind; durable meaning never rewritten; identifiers never reused). Strongest defense: the Accepted ADRs already carry different promises (fact 8), so a universal lifecycle would either over-promise Engine executability or under-promise fingerprint grammar. Weakness: risk of vague per-boundary gates that two reviewers could apply oppositely.
- **D — release-bound:** freely evolvable until a named supported release. Strongest defense: a release is the first observable event that can create an external constituency, and this repository has none yet. Weakness: a persisted-store adapter or a public repository can create externally held artifacts before any release; and a release date alone is not maturity evidence — it relocates the same decision.
- **E — narrower alternatives worth testing:** (i) *identity-now, support-later*: equality/non-rebinding invariants durable at acceptance, executable/outward support evidence-gated; (ii) *constituency-bound durability*: durability begins at the first artifact that leaves repository control, with an explicit provisional label until then; (iii) *support attached to consumers/release channels* rather than to definitions; (iv) *durable core plus provisional extensions*.

### 2.3 Likely failure / adversarial cases

- An artifact persisted by `FileControlledRevisionAuthority` outside repository control before any "release" — which rule applies?
- `engine-semantics:v1` emitted today by runtimes that can only execute V1 models: what may an external consumer infer, and is later spatial activation under the same identifier a semantics change?
- Amending the Engine spec's transfer sections before any V2 run exists: correction or new version?
- The legacy `contentHash` coexisting with the durable fingerprint for a "compatibility period" that has no end condition.
- Golden vectors/fixtures being mistaken for consumers.
- ADR-0014 §8's permanent decoder promise versus ADR-0015 §14's retirable executability: does any rule treat them consistently?
- Grandfathering: does preserving V1 forever include the decoder forever, and is that truthful given no constituency?
- A contract corrected during proving whose earlier digests exist only in repository tests.

### 2.4 Evidence that separates provenance obligations from support commitments

- Existence of any artifact outside repository control (deployment/consumer inventory) — unavailable here except via public GitHub metadata.
- Whether any code other than the repository's own decodes canonical bytes.
- Whether any Accepted text promises re-execution (ADR-0015: no) or decoder retention (ADR-0014: yes).
- Whether outward surfaces carry the identity (today: no).
- Whether a written correction path exists for provisional artifacts (ADR-0004 for `contentHash`: yes, by minting separately; for V2/Engine: only "amend before shipping").

## 3. Evaluation of the exact report revision

Read in full after §2 was written. The report's load-bearing conclusion is **Candidate C: boundary-specific evidence-gated commitments with shared minimum rules**, with the six subsidiary claims the handoff lists. The report is internally consistent with the repository facts in §1.1; every repository claim I spot-checked (ADR statuses and clauses, V2 §10, Engine §14, implementation/test inventory, absence of outward exposure, absence of releases/tags, PR #181/#235 merge dates, `0.1.0`/`0.0.0` version strings, PR #347 precedent) is accurate at the review baseline. Evidence categories are kept distinct throughout; recommendations are never phrased as fact.

Convergence with the independent reconstruction: §2.1's distinctions, §2.2's candidate set (the report's A–D match; my E-variants are examined in §5 below), and §2.3's failure cases were all reached independently and are all addressed by the report, except for the three gaps recorded as qualifications Q1–Q3 (mechanical default for silence, default custody of undeclared artifacts, provisional contracts without an end condition), which the reconstruction anticipated and the report leaves at principle level.

### 3.1 External evidence re-verification

All three sources were fetched on 2026-09-18.

| Source | Verified | What it establishes | Where the analogy breaks | Possibility vs. necessity |
| --- | --- | --- | --- | --- |
| RFC 6410 (BCP 9, Oct 2011) §2.1–2.3, §3.1 | Yes: §2.2 requires "at least two independent interoperating implementations with widespread deployment and successful operational experience", no interoperability-breaking errata, no unused complexity; Draft Standard level removed; annual review removed. | Evidence-based advancement; removal of an unused ceremony tier. | Internet-scale multi-implementation interoperability; Arcogine has one implementation. | Possibility only; the report says so. |
| Kubernetes Deprecation Policy (live, undated) rules 1, 2, 4a, 4b | Yes: rule 1 "can not be removed from that version or have its behavior significantly changed, regardless of track"; rule 2 lossless round-trip; rule 4a stability-dependent lifetimes (alpha removable without notice); the note that persisted API versions may not be removed and the server "must remain capable of decoding/converting previously persisted data". | Contrary evidence to "provisional permits same-name reinterpretation" and to "retiring service erases persisted-data obligations". | Installed fleet, lossless conversions, storage-version machinery; none exists in Arcogine. | Counterexample to two candidate rules; not a mandate. |
| Rust Compiler Dev Guide, "Request for stabilization" (live, undated) | Yes: report lists design decisions and deviations since RFC acceptance, work done, subteam connections; process includes documentation, feature-gate move from `unstable.rs` to `accepted.rs`, FCP. | Accepted design and stabilization are distinct, visible transitions with an evidence report. | Language exposure and compiler gating, not persisted histories. | Possibility only. |

**Result:** no source is misattributed; none carries the conclusion alone; no external taxonomy is imported. The report's own "possibility and counterexamples, not a theorem" framing is accurate. Challenge survived.

## 4. Challenges, evidence, and results

Format per challenge: what was challenged → evidence considered → result → effect on conclusion. "Repository fact", "External evidence", "Inference", "Challenge", and "Disposition/qualification" are labelled where a reader could otherwise confuse them.

### A. C versus A — eager durability

- **Challenged:** whether exact provenance requires immediate semantic-contract preservation stronger than C allows; whether A's costs are demonstrated; whether "named ⇒ fixed" is simpler and safer.
- **Evidence:** Repository fact — the current trigger is already ship-gated (§1.1 fact 1), not acceptance-gated; ADR-0006 was accepted and implemented within one day (fact 2); ADR-0015 §14/§16 already reject perpetual execution; ADR-0014 §8 promises permanent decoder retention. Report §"Candidates" and case matrix column A; self-challenge row 3 admits no measured maintenance cost.
- **Result:** Survives. The report keeps everything provenance actually needs from A — every exact definition revision is immutable (Q12: "Every semantic change gets a new exact revision even during proving"; Q13 identifiers never reused) — and gates only the *support/retention* promise. Under C, "named ⇒ fixed meaning" still holds for every identifier; what is not automatic is "named ⇒ supported forever". A's costs are indeed anticipated rather than demonstrated, and the report says so plainly ("claims avoidable commitments and concrete dependencies, not a quantified cost saving"). The one place where boundary-specific support could create ambiguity about what must remain interpretable is the undeclared-custody case, handled under D/Q2.
- **Effect:** none on the conclusion; A is correctly retained as V1's existing obligation and as a valid local choice.

### B. C versus B — explicit maturity promotion

- **Challenged:** whether allowing introduction and promotion in one event undermines the distinction; whether a mandatory separate ceremony is a valuable forcing function; whether avoiding ceremony reintroduces automatic durability under another name.
- **Evidence:** Report's promotion event (a decision naming exact revision, promise, scope, evidence, consumers, obligations) and gate 7 (independent adversarial pass for high-risk promises); Q2 "Reliance can expose an already owed obligation even if the team omitted a label"; repository history of days-long proving windows (fact 2); the existing high-risk research standard already requires independent review (researching.md §7/§9).
- **Result:** Survives with one qualification. The distinction is carried by the *content of the decision record*, which is mechanically checkable, so same-date introduction+promotion does not collapse it; and gate 7 supplies the forcing function B wanted without a mandatory second date. However, the report never states the **default for silence**: what a newly introduced contract is when its introducing decision neither declares a promise nor marks it proving. Under current text, silence plus shipping equals durable, which is exactly the automatic trigger the report rejects. Worse, Q2's "reliance can expose an already owed obligation" makes an obligation arise from an unobserved event, which is the non-mechanical outcome the brief's falsification criteria warn against. → **Q1.**
- **Effect:** conclusion holds; Q1 must survive reconciliation.

### C. "Durability is a scoped promise"

- **Challenged:** whether the decomposition (definition preservation, historical decoding, execution support, outward compatibility, physical retention) is necessary, enforceable, consumer-understandable, and bounded; whether the promises can be cleanly separated.
- **Evidence:** Repository fact — the Accepted ADRs already carry *different* promises per boundary (ADR-0014 §8 permanent decoder vs. ADR-0015 §14/§16 retirable executability vs. ADR-0012 §14 outward evidence vs. ADR-0011/ADR-0016 §9 bounded retention with disclosed gaps). Each promise maps to an existing checkable surface (spec + fixtures + ADR CI guard; retained verifier + resolution tests; `EngineSemanticsVersion.requireSupported`; ADR-0012 §14 list; custody declaration).
- **Result:** Survives. The decomposition is *descriptive* of existing Accepted text, so it is necessary to state the current promises truthfully; it is enforceable per promise; and the inseparability cases resolve: historical decoding *is* a retained-executable promise for the codec (the report says V1 permanent resolution is "stronger than a retained identifier", invariant 3); provenance by digest without retained content is a disclosed gap, not a rebinding (ADR-0016 §9); outward compatibility depending on internal semantics is already handled by ADR-0012's projection rule; retention implying support is handled by requiring custody to include the definition ("a hash or copied Git SHA is not a readability guarantee"). The consumer-understandability risk is real but small and is addressed by requiring each contract to state its promises "distinctly near the owning contract"; a one-line support statement per contract would make this concrete (folded into **Q7**).
- **Effect:** none.

### D. Immediate non-rebinding during proving

- **Challenged:** what qualifies as an "accepted historical occurrence"; whether repository-controlled proving artifacts can be rebaselined truthfully; whether every persisted artifact needs retained definition resolution; whether checkpoints/fixtures accidentally become commitments; whether the disposable/attributable boundary is mechanically clear.
- **Evidence:** Repository fact — acceptance boundaries already exist: ADR-0008 ("becomes authoritative when its immutable record is accepted by Arcogine's authoritative revision store"), ADR-0013 §5, ADR-0016 §8. `FileControlledRevisionAuthority` is constructed only in tests with temp directories; it accepts any artifact whose policy has a registered verifier and refuses others (`UNSUPPORTED_ARTIFACT_POLICY`). Report Q9 distinguishes "explicitly disposable fixtures" from "relied-on originals".
- **Result:** Survives as principle; **the usable boundary is incomplete.** "Accepted at an authority's commit boundary" cannot alone separate disposable from attributable: a test creates *accepted* records in a temporary authority. The discriminator the report's own Q9 gestures at is the **custody declaration of the store**, not the acceptance act. The report never states the default for an artifact whose custody was never declared, nor whether a retained authority may accept an artifact under a provisional (proving) policy at all. Without that, two reviewers could classify the same persisted artifact oppositely. → **Q2.**
- **Effect:** conclusion holds; Q2 must survive reconciliation.

### E. Evidence gates

- **Challenged:** sufficiency and minimality; vague terms ("relevant consumer pressure", "independent challenge", "realistic change", "appropriate evidence"); whether two reviewers could reach opposite outcomes without a mechanical disagreement.
- **Evidence:** Report gates 1–7 and the per-class table; ADR-0004's existing pre-promotion list for fingerprints (canonicalization, ordering, membership, versioning, compatibility, equality) — a repository-native precedent the report cites (R2) and which the fingerprint row of the class table generalizes correctly.
- **Result:** Survives with two small gaps. For each boundary the table gives a concrete, checkable minimum (fingerprint: independently derived vectors, strict decode/canonicality, old-artifact transition; Engine: complete supported-scope characterization against the spec's own §14 list, one actual consumer task; transport: boundary-only test consumer; Governance: acceptance-to-later-use path plus correction/drift/unavailable-history; Operational: correspondence, authority, failure handling, reconstructable accepted history). "Independently derived" is defined (a vector or oracle not produced by the implementation under test). The residual vagueness that would let reviewers diverge is (i) *where* the promotion decision is recorded for a contract whose authority is a normative specification rather than an ADR (Engine v1 spec, Factory v2 spec) — the report says "use the existing ADR/architecture/review process where the decision is hard to reverse", and durable promises are hard to reverse by definition, so this should be stated as a rule, not a judgement (**Q7**); (ii) the missing default for silence (**Q1**).
- **Effect:** conclusion holds.

### F. Consumer evidence and the independent-consumer requirement

- **Challenged:** whether the answer is boundary-specific rather than "it depends"; which classes need a second/independent consumer; whether identity/equality contracts can mature on vectors alone.
- **Evidence:** Report class table and Q5/Q6; ADR-0006 promises cross-language reproducibility with only a Java implementation; `Challenge` is the one real consumer task; `FactoryModelSemanticComparator`, `ConformanceEvaluator`, `ChangeSetFactory` are internal dependents of v1 artifacts.
- **Result:** Survives. The answers are boundary-specific and defensible: no external customer for a fingerprint, but a cross-language *claim* needs an independent implementation or another-language harness; a second *task* (not a second wrapper) for an Engine cross-task neutrality claim, second implementation preferred not required; a boundary-only client for a public interchange promise; one domain consumer for a narrow history promise; proportionate independent safety scrutiny for Operational. The report correctly notes (line 63) that V1's vectors "do not prove ... that an independent external implementation interoperates", i.e. V1's cross-language promise is grandfathered rather than proven under the gate — this should be said explicitly in the classification (**Q5**).
- **Effect:** none.

### G. Current `factory-model:v1` classification

- **Challenged:** exact existing obligation; whether permanent resolution means the codec stays executable forever; whether golden vectors are a constituency; whether V1 can be retired; conflation of meaning with implementation; whether "permanent" is supported in every dimension.
- **Evidence:** Repository fact — ADR-0006 (grammar immutability once shipped; golden vectors part of the contract; cross-language reproducibility); ADR-0014 decision 1 (permanent immutability), 8 (permanent verifier/decoder retention), 9 (V1 models "remain fully supported under existing non-spatial semantics"), 10 (no automatic lift); three internal code dependents; one literal pinned fingerprint; no external constituency identified; repository public.
- **Result:** Survives. The report preserves the promise, distinguishes meaning from implementation (invariant 3), correctly treats vectors as evidence not consumers, and correctly withholds retirement absent an explicit superseding support decision with inventory. Repository history supports "permanent" as *promised text*, not as demonstrated need — the report says exactly this. Gap: the report's V1 row does not enumerate the grandfathered promises, and ADR-0014 decision 9 (a standing *execution*-support promise for V1 models, distinct from decoder retention) is only implicit. → **Q5.**
- **Effect:** none.

### H. Current `factory-model:v2` classification

- **Challenged:** that no released codec/policy exists; no persisted V2 revision; no consumer relies on V2 as support; §10 permits correction; planning/tests create no stronger obligation; whether "unreleased" suffices when code/tests and sibling research depend on the grammar.
- **Evidence:** Repository fact — no V2 encoder/decoder/fingerprint/verifier exists (`FactoryModelV2` Javadoc; plan A2 dependency-blocked); a retained authority *cannot* accept a V2 artifact because no verifier is registered (`UNSUPPORTED_ARTIFACT_POLICY`), so no V2 controlled revision can exist anywhere, including outside the repository; `FactoryModelV2` shares no supertype with `FactoryModel`; Factory Model v2 §10 amendment clause; ADR-0014 Accepted (field membership/predicates not freely correctable).
- **Result:** Survives, and is *stronger* than the report states: because the code cannot produce a V2 fingerprint or accept a V2 artifact, V2's "no support commitment" classification is robust even against the missing external inventory (see §6). The report's distinction between grammar-only correction (§10) and ADR-0014 semantic change (supersession) is correct; sibling research treating V2 as *material under investigation* is not reliance.
- **Effect:** none.

### I. `engine-semantics:v1` classification

- **Challenged:** whether a version can be "fixed in meaning but not fully released"; which subset is promised; whether fixtures define the version more strongly than admitted; whether missing spatial fixtures are implementation or contract incompleteness; whether the split creates a partial-version concept unsupported by ADR-0015; what an external consumer may infer from `engine-semantics:v1` today.
- **Evidence:** Repository fact — Engine v1 spec header "implementation pending"; §14 "Before `engine-semantics:v1` is considered released, pinned behavioral fixtures must prove..." (18 families; spatial 7–11, 13 have no executable path); ADR-0015 decisions 5, 8, 12–16 speak of *released* versions, decision 8 says the runtime reports its one supported version; `EngineSemanticsVersion.CURRENT` is emitted for every runtime; only V1 models can run, and V1 models have no spatial facts, so the spec's transfer sections have no reachable behavior; spec §1.1 consequence 3 permits recording an unwritten rule as a correction when behavior is unchanged; `EngineSemanticsVersion` is not yet propagated on observations/events.
- **Result:** Survives with a mandatory sharpening. The split is consistent with ADR-0015's letter: immutability (decision 12) and fixtures (decision 13) attach to *released* versions, and the spec itself says v1 is not yet released, while decision 8 requires the runtime to report its version. What a consumer may infer today is not false: every run so far exercised only the non-spatial rules, and a V1 model has no transfers under the spec. The real risk is a later *correction* of the spatial sections after runs have already been attributed to `v1`. The report says "preserve already attributed semantics" but does not define the mechanism; ADR-0015/Engine v1 have no analogue of Factory v2 §10. The reconciliation must state what "released" means for `engine-semantics:v1` (all §14 families pinned, which requires a V2 runtime) and adopt a section-level rule: rules exercised by any attributed run are frozen; unexercised sections may be corrected only until the first attributed run exercises them, with each correction recorded. → **Q4.**
- **Effect:** conclusion holds; Q4 must survive reconciliation.

### J. ADR acceptance versus durability

- **Challenged:** whether "Accepted logical invariant, provisional representation" is consistently maintainable and rule-shaped rather than ad hoc.
- **Evidence:** Repository fact — ADR-0013 §14 and ADR-0016 §13 explicitly defer representation while their invariants are Accepted; ADR-0004 kept `contentHash()` provisional after acceptance; ADR README's immutability applies to decision text. Report Q11 and case 4.
- **Result:** Survives. The rule is reusable: ADR acceptance freezes the *decision text* (already CI-enforced); durability of a *concrete contract* (grammar, API, format, executable behaviour) requires the promotion record. Invariants defining identity/equality/history (non-rebinding, occurrence identity) are durable at acceptance because they are logical, not representational — the report states this (invariant 1). Examples of Accepted architecture changing representation without superseding identity already exist (file-authority layout declared replaceable; ADR-0006 minting a new fingerprint rather than promoting `contentHash`).
- **Effect:** none.

### K. ADR-0012 precedent

- **Challenged:** whether transport compatibility is fundamentally different from semantic identity; whether the analogy establishes possibility only.
- **Evidence:** ADR-0012 §4/§14; report case 3 (B column: "Does not establish that every other contract needs the same sequence"); additional repository precedents the report does not cite: ADR-0009 §4 and ADR-0011 context deliberately sequence "stabilize the supported observation/event contract" after semantic prerequisites.
- **Result:** Survives. Transport is different (a digest cannot be re-projected; its grammar must be fixed at first retained use), and the report uses ADR-0012 only to show the repository already tolerates a stabilization period, not to prove necessity. The extra precedents strengthen the "repository is not uniformly eager" finding without changing it.
- **Effect:** none.

### L. Operational consequence

- **Challenged:** whether real consequence forces immediate durability at first accepted act; whether an Operational contract can be provisional when acts have consequence; what must be frozen before the act; whether bounded retention suffices; whether Operational needs a stricter gate.
- **Evidence:** ADR-0013 §5 (acceptance monotonic; provisional/rejected material outside the accepted set), §10; Operational architecture §15–§16; ADR-0016 §9 bounded retention with truthful gaps; report case 5, class-table row, Q9.
- **Result:** Survives. The report's answer is the right shape: consequence attaches to the *accepted use*, not to the data shape; the accepted command/authority/result/applied configuration must be frozen and reconstructable before consequential reliance is admitted; representation and retention horizon may mature later provided the promised explanation is retained; the gate is stricter (independent safety scrutiny; "production exposure must not be used to gather missing safety evidence"). This is an admission constraint consistent with ADR-0013 §5(3), where provisional material is simply outside the accepted set.
- **Effect:** none.

### M. Changing-contract proving case

- **Challenged:** whether the report's correction path is operationally precise: history preservation, readability of old artifacts, when an identifier must change, corrected-provisional versus new-durable, controlled revisions referencing artifacts whose provisional contract later changes.
- **Evidence:** Report case 6 and matrix row 6; Q9, Q12; ADR-0008 one-fingerprint-per-revision binding; current authority acceptance behaviour.
- **Result:** Survives at the level the brief asked (compares amend / mint permanent v2 / migrate, and is truthful), with the same gap as D: a controlled revision bound to a provisional-policy fingerprint is resolvable only while that definition revision is retained, and the report does not say whether retained authorities may accept such artifacts or what custody they inherit by default. → **Q2** covers it.
- **Effect:** conclusion holds.

### N. Missing external deployment/consumer inventory

- **Challenged:** which conclusions depend on unseen consumers; whether any recommendation narrows an existing commitment without inventory; whether some classification needs `MORE EVIDENCE REQUIRED`; whether the report limits itself to prospective policy.
- **Evidence:** Report limitations section and "If a later owner decision seeks a broader internal rebaseline"; §1.1 fact 5 (public repo, no forks/releases/tags/CI publication; packages registry uninspected); fact 6 (no V2 codec exists).
- **Result:** Survives. The prospective rule does not depend on the inventory. V1 and Engine v1 are grandfathered, so no existing promise is narrowed. V2's classification is *independent* of the inventory because no implementation can have produced a V2 artifact anywhere. Withdrawal/migration decisions are correctly deferred pending inventory. Therefore the gap blocks only support-removal/reclassification decisions, which the report does not make; it does not block the lifecycle rule. See §6.
- **Effect:** none; treatment is correct and adequately stated.

### O. Grandfathering and correction path

- **Challenged:** whether grandfathering is the smallest truthful correction or too conservative; whether it preserves unnecessary machinery; which clauses need supersession versus clarification.
- **Evidence:** ADR-0014 decisions 1, 8, 9, 12; ADR-0006 immutability clause; ADR-0015 decisions 12–16; Factory v2 §10 triggers; plan A2/A3 blocked (V1/V2 coexistence machinery not yet built); V1 decoder already exists and is small.
- **Result:** Survives. Grandfathering costs one existing small decoder and forbids nothing except silent withdrawal; the machinery the brief worried about (permanent multi-policy estate) is not yet built and the report's route prevents it from being built automatically. The report says a new ADR "must explicitly supersede any conflicting automatic-trigger clauses" but does not name them. From the review's reading: ADR-0014 decisions 8 and 12 (permanent resolution of *every* released policy; automatic `vN+1` progression) are the automatic-trigger clauses that a prospective rule would narrow for future policies; Factory v2 §10's "recorded against a controlled revision" trigger is the same clause at spec level; ADR-0006's ship-gated *immutability* clause is retained as-is; ADR-0015 needs no supersession, only a definition of "released" (**Q4**). → **Q6.**
- **Effect:** conclusion holds.

### P. Reopening/falsification criteria and counterexample search

- **Challenged:** each major claim by attempted counterexample.
- **Results:**
  - *Exact provenance forcing immediate full durability:* a persisted digest whose defining grammar was not retained is a custody failure; a bounded custody horizon plus a disclosed gap (ADR-0016 §9) suffices. No counterexample.
  - *A client that cannot safely consume a provisional authoritative contract:* a client persisting artifacts long-term under a proving policy outside declared custody. Handled by opt-in/support bounds and unknown-version rejection **only if Q1/Q2 are adopted**; otherwise it is the ambiguity case. Conditional survival.
  - *Scoped promises inseparable:* decoder retention is both "historical decoding" and an executable-support promise for the codec; the report already treats V1 resolution as the stronger promise. Not a counterexample.
  - *Ceremony without changed decision:* applying the rule retroactively to ADR-0006 would have required only what ADR-0006 already contained (grammar, vectors, explicit promise). Minimal extra ceremony. Not a counterexample.
  - *Maturity determined solely by release:* none found; the closest, outward HTTP, already requires ADR-0012 §14 evidence beyond a release.
  - *Missed real dependency:* none beyond the report's list. However, the report under-uses one existing repository precedent: `FactoryModelVersion.contentHash()`, provisional under ADR-0004, kept as a "compatibility surface ... while consumers are deliberately migrated" (ADR-0006) — with no end condition, still flowing through `IntegratedHandler`/`SimResult` and asserted by `ScenarioBaselinesTest` three weeks later. It demonstrates the characteristic failure mode of a proving state: without a declared retirement trigger, provisional never ends. This does not falsify C; it shows that the proving state itself needs an exit condition. → **Q3.**

## 5. Omitted-model search

Each alternative was tested for whether it materially changes the answer:

1. **Immutable historical identity plus optional support-grade labels** — this is C's "exact definition revision + provisional marker + explicit promise". Same model.
2. **Per-boundary maturity with no explicit promotion event, only evidence-bound support declarations** — a support declaration naming revision, scope, and evidence *is* the report's promotion record; the difference is vocabulary. No change.
3. **Support attached to consumer/release channels rather than definitions** — collapses into D for release channels (already rejected as sole trigger) and into per-consumer support agreements, for which Arcogine has no consumers; ADR-0015 §17 and ADR-0016 §7 already put compatibility determination on the consumer side. No change.
4. **Durable core identity with provisional extensions** — a model-shape question owned by the sibling Factory composition research; the lifecycle rule is orthogonal to it and the report defers correctly.
5. **Automatic durability for identity/equality invariants, evidence-gated durability for behavioural compatibility** — C already freezes every exact definition revision immediately (Q12/Q13) and gates only support; the report's immediate non-rebinding invariant covers the identity/equality half. Equivalent.

**No omitted viable model materially changes the question.** The report's statement that "No Candidate E is needed" survives.

## 6. Impact of the missing external deployment/consumer inventory

Load-bearing limitation, treated as such:

- **Does not affect:** the prospective lifecycle rule (Candidate C) or the six subsidiary claims; the classification of `factory-model:v2` (no implementation can have produced a V2 fingerprint or accepted a V2 artifact, so no external V2 constituency can exist — this strengthens the report's row); the grandfathering of `factory-model:v1` and `engine-semantics:v1`, which *assumes* unknown consumers may exist and preserves every promise accordingly.
- **Does block:** any withdrawal, narrowing, retirement, or migration of an existing V1/Engine promise, and any reclassification of an existing contract as "provisional". The report makes none of these and explicitly conditions them on inventory; that condition must survive reconciliation (**Q5**, last sentence).
- **Minimum additional evidence before any support-removal decision:** an owner-supplied inventory of installations, retained revision stores, exported artifacts, and downstream packages (including the GitHub Packages registry, uninspected here), plus a demonstrated reconstruction/refusal path for each retained history — as the report's "broader internal rebaseline" section already states.
- **Not converted** anywhere in the report from "no constituency identified" into "no constituency exists"; the review confirms the report's own wording holds this line.

## 7. Verification of the brief's sixteen questions and six proving cases

All sixteen questions are answered with an actionable boundary except as noted: **Q2** (durability event) contains the non-mechanical "reliance can expose an obligation" clause (→ Q1); **Q9** (provisional persisted artifacts) gives categories without a default classification rule (→ Q2); **Q14/Q15** are answered but the enumerated grandfathered promises and clause map are left implicit (→ Q5, Q6). All six proving cases are constructed and every candidate is evaluated against each; the matrix's judgements were checked against the repository facts above and none was found wrong. Exit criteria 1–9 of the brief are met by the report; criterion 10 (independent review) is this artifact.

## 8. Disposition

**ACCEPT WITH QUALIFICATIONS.**

The load-bearing conclusion — Candidate C, boundary-specific evidence-gated commitments with shared minimum rules; immediate non-rebinding of exact historical referents; explicit promotion of a specified revision plus a specified support promise; distinguishable but not necessarily separate introduction and promotion; no retroactive relabelling of existing V1/Engine meanings; prospective improvement rather than identifier recycling — survives the adversarial pass. No claim was falsified; no omitted model materially changes the question; no external source is misused; the baseline has not moved. The qualifications below close gaps where the report states a principle that reconciliation must turn into a mechanical rule, or leaves an enumeration implicit.

### Mandatory qualifications that must survive durable reconciliation

- **Q1 — Default for silence, and obligations arise only from explicit promise or accepted retained custody.** A newly introduced contract whose introducing decision neither declares a scoped support promise (with the evidence gate satisfied) nor marks it proving is **non-durable but non-rebinding**; silence never yields durability. New contracts must state one or the other explicitly. Narrow the report's Q2 clause so that a support obligation arises from (a) an explicit published promise or (b) acceptance of an artifact/record into retained (non-disposable) custody — not from unobserved third-party reliance, which is a risk to be inventoried, not an obligation.
- **Q2 — Custody default for provisional persisted artifacts.** The disposable/attributable boundary is the **declared custody of the holding store/authority**, not the acceptance act: artifacts are disposable only when held in custody declared ephemeral (test fixtures, temporary authorities); anything accepted into a store without such a declaration is retained for the promised horizon. Reconciliation must also decide, and record in the owning Governance/Factory surface, whether a retained authority may accept artifacts under a proving policy at all and, if so, that it must record the custody declaration with them.
- **Q3 — A proving contract must carry an exit condition.** Every provisional/proving contract revision must declare either a custody horizon or a retirement trigger; a "compatibility period" with no end is not a proving state. Reconciliation should classify the existing `contentHash()` legacy provenance under this rule (bounded custody with a retirement trigger, or explicit retirement), since it is the repository's live example of an unbounded provisional contract.
- **Q4 — Define "released" for `engine-semantics:v1` and freeze at section granularity.** State that `engine-semantics:v1` is released when every §14 fixture family is pinned (which requires a V2 runtime); until then, rules already exercised by any attributed run are frozen exactly as ADR-0015 decision 12 requires, and only sections no attributed run has exercised may be corrected, each correction recorded, until the first attributed run exercises them. Record this in the owning Engine surface through the proper process (semantics-preserving amendment if a reviewer can establish equivalence; otherwise supersession) — not by editing behaviour.
- **Q5 — Enumerate the grandfathered promises explicitly.** For `factory-model:v1`: grammar immutability and never-reused identifier (ADR-0006), golden vectors as part of the contract, the cross-language reproducibility claim (a *promise* not yet proven under the report's own gate), permanent verifier/decoder retention (ADR-0014 decision 8), full execution support of V1 models under non-spatial semantics (ADR-0014 decision 9), and no automatic lift (decision 10). For `engine-semantics:v1`: attribution plus immutable specification plus fixtures survive retirement (ADR-0015 decisions 14, 16). None may be narrowed without an explicit superseding support decision preceded by the inventory in §6.
- **Q6 — Name the clauses the prospective rule supersedes or narrows.** ADR-0014 decisions 8 and 12 (permanent resolution of *every* released policy; automatic `vN+1` progression) and Factory Model v2 §10's "recorded against a controlled revision" trigger are the automatic-support clauses to be narrowed for *future* policies by a new Accepted ADR; ADR-0006's ship-gated immutability clause and ADR-0015's identity/definition rules are retained. Any Accepted-ADR change goes through supersession, never editorial amendment.
- **Q7 — Fix where the promotion record lives and what it must contain.** A durable promotion is recorded in an ADR (or, for a contract whose authority is a normative specification, in a dated promotion section of that specification subject to the same independent review), naming: exact contract revision, the promises made from the report's scoped-promise set, supported scope, evidence relied on, affected consumers/artifacts, and evolution/retention/retirement obligations. Each contract's owning document must then carry one compact support statement enumerating its promises so consumers are not left to reconstruct them.

### Effect on the report's conclusion

Unchanged in substance. The qualifications convert four principle-level answers (Q2, Q9, Q14, Q15 of the brief) into mechanical rules and enumerations; they do not alter the candidate ranking, the grandfathering of existing commitments, the V2/Engine classifications, or the planning hold. With Q1–Q7 carried into reconciliation, the exact report revision plus this review constitute decision-quality evidence for a scoped-promotion rule and the targeted clarifications the report recommends. Reconciliation remains a separate phase with its own independent PR review; this review does not mark the research `CONCLUDED`.

## 9. Evidence considered (summary)

- **Repository fact:** the surfaces listed in §1 at `46389bc5d21e0c95ffa9366ce271f9d943d44b35`, including implementation and tests; repository history for ADR status transitions and implementation landing; GitHub metadata (visibility, forks, stars, releases, tags, workflows).
- **External evidence:** RFC 6410, Kubernetes Deprecation Policy, Rust stabilization guide — fetched 2026-09-18, verified as summarized in §3.1.
- **Inference:** §2 reconstruction; challenge results in §4; robustness of the V2 classification to the inventory gap (§6).
- **Uninspected:** GitHub Packages registry (403); any private deployment/consumer inventory.

## 10. Addendum — owner-supplied constituency inventory (2026-09-18)

**Repository-owner statement, supplied after the review disposition above was handed off:** there is nothing outside the repository; the project is at an early stage; every consumer of Arcogine's contracts is either already in the repository or does not exist. This supplies the inventory that §6 named as the minimum missing evidence.

Effect on the review:

- The **disposition is unchanged**: `ACCEPT WITH QUALIFICATIONS`. The prospective rule and the V2/Engine classifications never depended on the inventory.
- **Q5's precondition for narrowing an existing promise is now satisfiable by that statement.** Reconciliation *may* therefore decide, on the report's own evidence rather than on caution about unknown users, whether any grandfathered `factory-model:v1` / `engine-semantics:v1` promise (for example ADR-0014 decision 8's permanent decoder retention, or the cross-language reproducibility claim) should be narrowed to what in-repository dependents actually need. The review does not prescribe that outcome; it records that the "no inventory" reason for refusing to consider it no longer applies. Any narrowing still requires supersession of the Accepted clauses (Q6), never editorial amendment.
- The report's fallback "broader internal rebaseline" section is no longer blocked on inventory, only on the reconstruction/refusal demonstrations it lists.
- The residual public-source-build risk is retired: the owner's statement, not a GitHub search, is the authority.

Durable registration: the statement is recorded as a maintained current-state constraint in `docs/architecture/overview.md` ("Current implementation constraints (MVP)", item 7), with the rule that it is updated in the same change that creates the first external deployment, release, published artifact, or consumer. That edit is a reconciliation-phase change committed on this workspace after the review handoff; it is not part of the reviewed research evidence.
