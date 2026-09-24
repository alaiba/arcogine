# Semantic-contract promotion boundary and premature durability

> **Research status:** ACTIVE (a completed report exists; nothing is reconciled, so the question is
> not `CONCLUDED`)
>
> **Research baseline:** live `main` `b9d6e8b0e3b10f07cbc6777e3bc3abbce2f45c19`. I re-checked it
> just before committing, and `main` had not moved.
>
> **Material under investigation (not landed truth):** workspace branch
> `research/semantic-contract-promotion-boundary-review`, which carries only the research handoff
> prompt on top of the baseline.
>
> **Authority:** Research evidence only. It is not accepted architecture, product direction or
> implementation commitment until a separate, independently reviewed reconciliation promotes it.
>
> **Risk:** High (semantic identity, persistence, historical attribution, determinism, compatibility
> obligations).
>
> **Adversarial-review status:** required, not yet performed. This revision contains only the
> author's own self-challenge (§ Adversarial analysis). It is **not** decision-quality evidence
> for reconciling canonical architecture or specifications until a genuinely independent
> adversarial review of this exact revision has taken place.

## Question

> What exact event or retained-use boundary, if any, legitimately promoted the current Factory and
> Engine semantic definitions from mutable proving definitions into durable immutable identities,
> and has Arcogine applied that boundary too early?

The handoff frames the question the same way. I sharpened it in one respect: I evaluate the
**evidence for fixation** of `factory-model:v1` and `engine-semantics:v1` separately from
**whether the fixation rule itself is sound**. The two turned out to have different answers.

## Decision at stake

- Is the "`factory-model:v1` is immutable because it has attributed records" assertion true? If it
  is not, which reconciled results depend on it (the Factory V1→V2 successor structure, the
  V1/V2-absent/V2-present Engine applicability partition, the READY Engine applicability question)?
- Does Arcogine need a distinguishable provisional/pre-promotion identity regime, or only an
  evidence requirement on the existing rule?
- Should the concluded semantic-contract-maturity and Factory-composition results be reopened, and
  if so, which load-bearing premise fails?

## Scope and non-goals

In scope: the Factory fingerprint policies (primary case), `engine-semantics:v1` (secondary case),
the controlled-revision authority as the only persistence-capable authority, and the architecture
rules that decide fixation.

Out of scope (per the handoff): editing canonical architecture or specifications; renaming
code/types/files; implementing a provisional-identity mechanism; migration machinery; choosing
final tokens. The Engine applicability answer itself (which Engine identity executes which V2 case)
is not answered here. This report only tests the premise that question stands on.

## Executive conclusion

1. **The fixation rule is sound; applying it to V1 was premature.** The current rules say a
   definition becomes fixed at the first retained or accepted attributed record, and that
   disposable proving activity creates no obligation. That is a defensible boundary, and this
   investigation found no evidence against it. What fails is the claim that the boundary has been
   crossed.
2. **The V1 immutability assertion is unsubstantiated, and its justification is partly circular and
   partly equivocal.** Each of its three evidence items refers to proving activity or to an
   in-process domain operation, not to retained or published reliance:
   - "published fingerprints" means Factory's in-process `FactoryModelPublisher.publish`
     validation step, not published reliance;
   - "controlled revisions" and "stored canonical artifacts" exist only inside JUnit `@TempDir`
     stores;
   - no custody declaration, release, tag, outward consumer, or non-test instance of any retained
     authority exists.

   The assertion also has a traceable origin. The original V1 decision (dated 2026-08-27) froze the
   policy "once `factory-model:v1` ships in implementation", and the Factory-evolution decision
   (2026-09-03) then called V1 "released". The 2026-09-21 architecture consolidation introduced the
   attribution-based rule. In the same change it restated V1's freeze as "has attributed records",
   without identifying any record. The freeze was carried over under a new justification that the
   evidence does not meet.
3. **`engine-semantics:v1` is even further from a legitimate freeze.** Several facts point the
   same way:
   - Its identity is a runtime constant; it is not stamped on any observation, event or retained
     record, and propagation is a declared gap.
   - Its spatial rules are not implemented.
   - It was frozen by an earlier "released versions are immutable" rule, and no release exists.
   - Its specification now states an unconditional "any intentional result-affecting change is a
     new version" rule. That rule is stricter than the architecture's pre-attribution correction
     allowance, and nothing justifies the difference.
4. **The Factory V1→V2 successor structure depends on the premature freeze. The Factory
   composition rule does not.**
   - "One closed policy with explicit optional records and one aggregate fingerprint" stands on
     its own merits.
   - The existence of a *separate, coexisting* `factory-model:v2`, with V1 kept unchanged beside
     it, rests on the premise that V1 was attributed. That premise is stated as context in the
     composition decision record. So do several consequences:
     - two identities for identical authored production content (V1 vs V2-absent, pinned as a
       required V2 golden vector);
     - the open "continued V1 publication" support question;
     - the V1 rows in the transfer-applicability boundary and in the READY Engine applicability
       question.
   - If the premise fails, the simplest truthful model is one still-provisional Factory grammar
     (production records plus an optional spatial record), corrected in place, with no V1/V2
     compatibility estate.
5. **A narrow boundary invariant survives, and a light distinguishability requirement survives with
   it. A lifecycle does not.** Arcogine does not need a universal `proving`/`promoted` state. It
   does need:
   - (a) an explicit statement that deterministic identity generation, in-process publication,
     implementation landing on `main`, a "normative" document status, golden vectors, and
     acceptance by a test-scoped authority instance are **not** attribution;
   - (b) each owning contract recording its fixation as an evidenced fact (the accepting authority
     or published reliance that fixed it), or recording that no fixation has occurred;
   - (c) a guarantee that the **committed identity token never denoted any other definition**. A
     pre-promotion identity therefore has to be distinguishable from the eventual committed one:
     either a visibly provisional token corrected in place, or abandon-and-reissue. That token must
     also be refusable by a retained authority.

   Rule 2's own boundary is already binary; (b) records it, and adds no new state.
6. **Reopening.**
   - The semantic-contract-maturity conclusion survives. Its rule is what this report applies. Its
     "same-label amendment of an *attributed* Engine definition" open item currently has no
     concrete attributed case.
   - The Factory-composition result should be reopened **only** for its successor-identity
     consequence (premise: "V1 was attributed and immutable"), not for its composition rule.
   - The READY Engine applicability question should be re-scoped before execution, because it
     presumes "fixed" Engine v1 and "retained historical v1 results" that do not exist.

**Confidence:**
- The repository facts are high confidence, within the stated search scope.
- "V1 is not attributed under current rules" is high confidence.
- "The composition rule survives while the successor structure does not" is medium-high.
- The provisional-distinguishability requirement is medium.
- Collapsing to a single provisional Factory grammar is medium: it is the simplest candidate, but
  the reconciliation must still weigh the voluntary-early-commitment alternative (candidate B
  below), which this report does not falsify.

## Repository evidence

All items below are **Repository fact** at the baseline unless labeled otherwise.

### Architecture rules that decide fixation

- [Architecture Overview, semantic evolution and support](../../../docs/architecture/overview.md#semantic-evolution-and-support):
  - rule 2 says "A definition may be corrected in place only until the first retained or accepted
    record is attributed to it";
  - rule 3 says "Naming an identity creates none of [the support obligations] automatically";
  - rule 5 says obligations arise when "the owning contract publishes reliance, or when an
    authority that has declared its custody accepts a record at its commit boundary", and that
    "Disposable activity — tests, scratch stores, drained events, local runs — creates no
    obligation by existing";
  - rule 7 says there is no universal lifecycle.

  The section also lists as **open**: "Same-identity amendment of an attributed Engine definition,
  custody mechanics for retained proving artifacts".
- [Semantic contract support](../../../docs/development/semantic-contract-support.md) says that
  "*Before* publishing reliance ... or admitting material attributed to it into a retained
  authority, record a compact declaration in the owning specification or authority contract". The
  declaration covers custody (accepting authority, retained vs disposable scope, horizon, and so
  on). The document also says "a path name, test label or later deletion is not proof of
  disposability", and "An Engine that stamps records with a semantics identity must either use a
  frozen exact definition or keep those records explicitly disposable".
- Both the rules and the support document were introduced in the architecture consolidation of
  2026-09-21 (delivery-history provenance: merged PR #370). `git log -S "Attribution fixes the
  whole definition"` finds that commit as the first appearance.

### The V1 immutability assertion and its origin

- [Factory Model v1](../../../docs/architecture/factory-model-v1.md), "The policy is immutable",
  says: "`factory-model:v1` has attributed records: published fingerprints, controlled revisions
  and stored canonical artifacts reference it. Under the semantic evolution rules its definition is
  therefore fixed as a whole". The sentence first appears in the 2026-09-21 consolidation.
- The predecessor decision record `0006-durable-semantic-fingerprint-contract.md` (accepted
  2026-08-27; readable at the parent of the consolidation commit) had a heading "Released
  fingerprint policies are immutable", with the trigger "Once `factory-model:v1` **ships in
  implementation**, every supported implementation must produce the same fingerprint".
- The predecessor `0014-factory-model-semantic-policy-evolution.md` (accepted 2026-09-03) opened
  with "Arcogine's **released** `factory-model:v1` fingerprint policy is immutable under ADR-0006".
  It derived the need for a separate `factory-model:v2` from it: spatial facts "cannot be added to
  `factory-model:v1` without changing the meaning and canonical bytes of an already released
  policy".
- The predecessor `0015-engine-semantics-identity-and-reproducibility.md` (accepted 2026-09-03)
  said "**Released** semantics versions are immutable and never reused".
- `gh release list --repo alaiba/arcogine` returns nothing, and `git tag --list` is empty. The
  repository is public (created 2026-04-01). No release event exists.

**Inference:** V1's freeze was originally triggered by implementation landing ("ships in
implementation", later called "released"). The current rules explicitly reject that as a
sufficient trigger. The consolidation kept the freeze but restated its reason in the new rule's
vocabulary ("attributed records") without citing an acceptance event.

### What each claimed V1 "attributed record" concretely is

| Claimed evidence | Concrete referent found | Retained or published reliance? |
|---|---|---|
| "published fingerprints" | `FactoryModelPublisher.publish(model)` validates and returns an in-memory `FactoryModelVersion`, whose fingerprint is computed on demand. In Factory's domain language "publication" is this validation boundary ([factory-design.md §11–§12](../../../docs/architecture/factory-design.md#11-publication-identity-and-provenance)). | No. It is an in-process domain operation, not rule 5's "publishes reliance". No outward adapter exists ([overview, Outward Adapters](../../../docs/architecture/overview.md#outward-adapters)). |
| "controlled revisions" | `FileControlledRevisionAuthority` is constructed only in `FileControlledRevisionAuthorityTest`, `ChangeSetFactoryTest` and `PreChangeConformanceProvingCaseTest`, each on a JUnit `@TempDir`. | No. These are test-scoped scratch stores. No production composition root instantiates the authority. |
| "stored canonical artifacts" | Artifacts written by those same temp-dir authorities. No canonical artifact file is committed (`product/**/src/{main,test}/resources` contains no tracked files). | No. |
| (implicit) golden vector | One literal V1 fingerprint pinned in `FactoryModelFingerprintV1Test`. | A committed test vector, i.e. proving evidence; nothing consumes it outside the test. |
| (implicit) documentation examples | `standards-alignment.md` and `factory-model-v1.md` show only the `<digest>` placeholder form. | No concrete digest in durable docs. |

**Absence-claim scope:**
- `git grep` over the baseline tree for `static void main`, for every construction of
  `FileControlledRevisionAuthority`, for `factory-model:v1:sha256` literals, and for main-code uses
  of `ModelFingerprint` and `EngineSemanticsVersion`.
- `git grep` over the parent trees of the commits that retired the legacy API/CLI shell and the web
  consumer (`product/interfaces/{api,cli,web}`), for `fingerprint`, `semanticsVersion` and
  `FileControlledRevision`. There were no matches, so those retired surfaces never carried
  fingerprints or revision authority.
- GitHub releases and git tags.

**Limitation:** this cannot observe contributor machines, forks, or third parties who may have
built the public repository and kept outputs. Under rule 5 and the support policy's
external-consumer case, that kind of undeclared third-party use creates no Arcogine obligation
unless Arcogine published reliance. I found no such publication.

### Other authorities

- `EvidenceReferenceAuthority` and `EvaluationOccurrenceAuthority` have only in-memory
  implementations (`InMemoryEvidenceReferenceAuthority`, `InMemoryEvaluationOccurrenceAuthority`).
- [Governance evidence](../../../docs/architecture/governance-evidence.md) defers retention and
  persistence.
- No custody declaration exists in any owning specification or authority contract. `git grep -i
  custody` over `docs/` (excluding research investigations) finds only the rule, the support
  policy and research-custody prose.
- [Controlled revisions](../../../docs/architecture/controlled-revisions.md) states the semantic
  persistence obligations, but not retained-versus-disposable scope, horizon, or how disposable
  material becomes retained admission.

### Engine

- `EngineSemanticsVersion.CURRENT = "engine-semantics:v1"` was introduced 2026-09-18.
  `FactoryRuntime.semanticsVersion()` exposes it.
- `RuntimeObservationMetadata` carries `runId` and `modelFingerprint`, but no semantics version.
  The overview states that propagation "remains follow-up work".
- The [Engine specification](../../../docs/architecture/engine-semantics-v1.md) status is
  "implementation partial (spatial execution and provenance propagation outstanding)".
- Its §1 says: "An intentional change that can alter outcome for identical explicit inputs —
  including a bug fix that observably changes interpretation — is a new version". This has no
  pre-attribution qualifier.
- No main-code consumer (Challenge included) records an Engine semantics identity.

### V2 and dependent research

- [Factory Model v2 §10](../../../docs/architecture/factory-model-v2.md#10-identity-and-evolution)
  uses the correctability test "No V2 fingerprint has yet been produced by a **shipped publication
  path** or recorded against a controlled revision". That is the de facto boundary the repository
  applies, and it is the old "ships in implementation" trigger, not the current rule 2/5 boundary.
  Under that test, V1 counts as frozen only because its publication path is merged, and V1's
  "controlled revisions" are the temp-dir ones.
- The same document requires golden vector 16: "the same authored production content fingerprinted
  under V1 and under V2 with the spatial record absent producing different fingerprints". It also
  says a V2-absent artifact "carries V1's production stream unchanged". Two identities for
  identical authored content are therefore a pinned consequence.
- [Composition decision record](../../../docs/history/decisions/2026-09-23-factory-model-semantic-composition.md),
  Context: "`factory-model:v1` was attributed and immutable; the V2 draft had never produced or
  recorded a fingerprint". This is stated without evidence.
- The READY [Engine applicability question](../../../docs/research/investigations/engine-applicability-after-transfer-boundary.md)
  asks whether "an existing fixed definition" can admit the V2 cases. It lists "retained historical
  v1 results" and "execution support for retained V1 artifacts" among its cases.
- The concluded [semantic-contract maturity brief](../../../docs/research/investigations/semantic-contract-maturity-durability.md)
  explicitly rejected durability "arising automatically from introduction, decision acceptance,
  internal publication, or the first need for a deterministic identifier". Its full report and
  review were not retained, so I could not check whether it audited V1's actual attribution.

## Candidate models

- **A. Strict current model.** V1 and Engine v1 are already fixed by retained attribution. Any
  semantic correction needs a distinguishable identity.
- **B. Owner-declared early commitment.** V1 (and possibly Engine v1) is fixed by a deliberate
  owner decision, not by attribution. This is legitimate in principle: the support policy says "a
  contract may be introduced and committed to in the same change". It would have to be declared as
  a commitment, with consumers, scope and the reason for choosing it.
- **C. Premature promotion, correct in place, no marker.** Nothing is attributed, so correct V1 and
  Engine v1 in place and keep the `v1` tokens. No other change.
- **D. Evidence gap.** The rule is sound; add the missing declarations and fixation evidence. The
  status of V1 and Engine v1 is whatever that evidence shows.
- **E. Provisional identity regime.** A pre-promotion identity is visibly distinct from any
  committed identity and is corrected in place. The committed token is issued only at promotion.
  Retained authorities refuse provisional identities. There are two variants of the committed-token
  choice: E1 uses a new committed token; E2 promotes the provisional definition under a reserved
  token that no earlier definition used.
- **F. Immutable ephemeral identifiers.** Every proving revision gets a fresh immutable identifier
  (draft-NN style) carrying no support obligations, and the committed token is assigned at
  promotion.
- **G. Scoped mixed.** Factory and Engine have different legitimate boundaries.
- **H. No-new-lifecycle boundary invariant.** Keep rules 1–7. Add: (a) generation, publication,
  implementation, golden vectors and test-scoped acceptance are not attribution; (b) each owning
  contract records fixation as an evidenced fact; (c) the committed token never denoted another
  definition. This is D with enforceable content plus the distinguishability part of E, without a
  lifecycle state.

## External evidence

All sources below were fetched and checked during this investigation (verified 2026-09-25).

| Source | What it establishes | Where the analogy breaks |
|---|---|---|
| Semantic Versioning 2.0.0, semver.org, items 3, 4, 9 | Item 3: "Once a versioned package has been released, the contents of that version MUST NOT be modified". Item 4: 0.y.z is "for initial development. Anything MAY change at any time." Item 9: pre-releases "might not satisfy the intended compatibility requirements". The freeze trigger is **release**, not implementation. | SemVer versions API *compatibility* between packages. Even a 0.x release is immutable content, so "v0" under SemVer would still mint a new number per change and does **not** give in-place correctability. SemVer has no notion of persisted records attributed to a definition. Adopting "v0" by analogy would bring in the proliferation it was meant to avoid. |
| Kubernetes, "API Overview — API versioning", kubernetes.io/docs/reference/using-api/ (page as of 2026-09-25) | Alpha versions (`v1alpha1`) "may change in incompatible ways ... without notice", and support "may be dropped at any time without notice". Beta may change with migration instructions. Stable `vX` remains available. Provisional status is **visible in the identifier**, and the committed name `v1` is reserved for the promoted definition. | Kubernetes versions a wire/storage API with a live store (etcd) and real users; alpha objects are persisted. Arcogine has no store and no users. The page does not say what happens to persisted alpha data, so it does not settle what a retained provisional record would mean. |
| multiformats/multicodec README (GitHub, master, fetched 2026-09-25), status column | "draft - ... may be reassigned if it doesn't gain wide adoption"; "permanent - ... may not [be] reassigned". The NOTE says draft status alone does not make a code reassignable: "Check to see if it ever gained wide adoption". A content-identifier registry uses a provisional status whose rebinding permission depends on **actual adoption**, not on a label. | Multicodec codes are global registry entries with third-party adoption. Arcogine's policies are single-owner. The "adoption" test maps onto Arcogine's "retained or published reliance" test. The status label is a hint, not the authority, which matches Arcogine's "a path name, test label ... is not proof of disposability". |
| IETF RFC 7595 (BCP 35), *Guidelines and Registration Procedures for URI Schemes*, June 2015, §3, §4, §7.1, §7.3 | "Provisional" registrations "can be updated by the original registrant", and transition to "permanent" is requested and approved "in the same manner as a new 'permanent' registration". A standards body keeps provisional and permanent identifiers as distinct statuses, with an explicit promotion act. | This is an identifier-registration process, not content identity. Its provisional names are the same token before and after promotion, so it does not support requirement (c). It shows that an explicit promotion *act* is normal practice, not that tokens must differ. |

**How the external evidence bears on the candidates.** None of the four sources treats
implementation as the freeze trigger. All four freeze on an explicit act (release, stable
graduation, permanent registration) or on observed reliance (adoption). That contradicts the
original "ships in implementation" trigger and supports the current rule 2/5 boundary. The
evidence is mixed on whether the provisional marker must be part of the token itself:
- Kubernetes: yes;
- RFC 7595: no;
- multicodec: status column, not token.

That remains an Arcogine design choice, and I justify it below from an Arcogine-specific property
(the token participates in canonical bytes), not from analogy.

## Proving cases

Legend: ✓ handled truthfully at proportionate cost; ✗ fails (untruthful, or creates unrequired
obligations); ~ coherent but costly or ambiguous.

| # | Case | A strict | B declared | C in-place | D gap | E provisional | F ephemeral | H invariant |
|---|---|---|---|---|---|---|---|---|
| 1 | Golden vector changes before any retained authority accepts it | ✗ forces a new policy for a test-only change | ~ new policy by owner's choice | ✓ | ✓ once D shows no fixation | ✓ | ~ new draft id per change | ✓ |
| 2 | Test creates a `ControlledRevision`, then all state is deleted | ✗ counts it as attribution (V1's current claim) | ✓ irrelevant to a declared freeze | ✓ | ✓ | ✓ authority would refuse or not persist | ✓ | ✓ (a) says so explicitly |
| 3 | Canonical artifact stored only as a Git test fixture | ✗ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| 4 | Durable repository example refers to `factory-model:v1`, no consumer promised | ✗ | ~ | ~ the example silently changes meaning across history | ✓ if the example is declared illustrative | ✓ the example names a provisional token | ✓ | ✓ |
| 5 | Internal retained authority deliberately accepts a fingerprint and must explain it months later, pre-release | ✓ | ✓ | ✗ no marker; nothing tells anyone the freeze happened | ✓ declaration precedes admission | ✓ promotion precedes admission | ✓ | ✓ this is exactly the legitimate boundary |
| 6 | External consumer persists the identity | ✓ | ✓ | ✗ | ✓ | ✓ only committed tokens are published | ✓ | ✓ |
| 7 | Provisional identity accidentally crosses into an authoritative store | n/a | n/a | ✗ undetectable | ~ review-only | ✓ mechanically refusable | ✓ refusable | ✓ with (c) plus authority refusal |
| 8 | Semantic defect found just before promotion | ✗ new identity | ✗ new identity | ✓ | ✓ | ✓ | ✓ new draft id | ✓ |
| 9 | Same defect found just after legitimate promotion | ✓ new identity | ✓ | ✗ in-place edit would rebind | ✓ | ✓ | ✓ | ✓ new identity; the old one keeps its meaning |
| 10 | Spatial semantics discovered while provisional vs after commitment | ✗ provisional case builds a V1/V2 estate | ~ | ✓ provisional, ✗ committed (rebinds) | ✓ | ✓ | ✓ | ✓ correct in place before; new closed policy after |
| 11 | Engine interpretation changes during internal proving vs after retained run attribution | ✗ new version for unimplemented rules | ~ | ✓ proving, ✗ after | ✓ | ✓ | ✓ | ✓ |
| 12 | Support retired while historical attributed records remain | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ unchanged (rule 6) |
| 13 | *(derived)* Repository history: an old commit's test pins `factory-model:v1` bytes that a later in-place correction changed | ✓ | ✓ | ✗ the committed token then denotes two definitions across history | ~ | ✓ provisional token was never the committed one | ✓ | ✓ via (c) |
| 14 | *(derived)* Identical authored production content needs one identity | ✗ V1 vs V2-absent duplication is required | ✗ same | ✓ | ✓ | ✓ | ✓ | ✓ |

Notes on the derived cases:
- **Case 13** is Arcogine-specific. The policy token is inside the canonical bytes (`arcogine.factory-model.v1\0` prefix), so git history permanently associates "v1" with whichever bytes the tests at a given commit pinned. Git history is delivery history, not a retained authority, so this creates no *obligation*. It does create *ambiguity* if the committed identity reuses a token that denoted something else earlier. That is the discriminator against C.
- **Case 14** exposes the concrete cost of A: vector 16 of the V2 specification pins two different fingerprints for the same authored design.

**Results:**
- **A fails** cases 1–4, 8, 10, 11 and 14. Its only support is the unsubstantiated attribution claim.
- **C fails** cases 5, 6, 7 and 13. It removes obligations but gives no signal when a legitimate boundary is later crossed.
- **D** is right about the rule but, left as a review-only declaration, handles case 7 weakly.
- **E, F and H** all pass. **F** adds a new identifier per proving change, and nothing requires that cost while no record escapes.
- **B** is not falsified: an owner can choose to commit early. It is ~/✗ on cases 1, 8, 10 and 14 purely on cost. It is also not what the repository currently claims, since the claimed basis is attribution.
- **G (scoped mixed)** is absorbed. Factory and Engine turn out to be in the *same* state (neither is attributed), but they differ in readiness (below), so their future promotion moments should be decided separately.

## Engine assessed separately

- **Retained attribution:** none. The identity is not stamped on any observation, event or record.
  `EngineSemanticsVersion` is a runtime-exposed constant. The support policy's "An Engine that
  stamps records ... must either use a frozen exact definition or keep those records explicitly
  disposable" is not yet engaged.
- **Promotion:** none. It was frozen as "released" by a decision whose release trigger never
  occurred. The consolidation then restated the new-version rule unconditionally in the
  specification. That rule is stricter than overview rule 2 and is not justified by any recorded
  acceptance.
- **Readiness differs from Factory.** V1's canonicalization is fully implemented and pinned. Engine
  v1's spatial transfer rules, destination binding and transfer events (§5–§8, §12) are specified
  but not executed. Rule 2 fixes a *whole* definition, unexercised rules included. Promoting Engine
  v1 now would therefore freeze behavior that no fixture has run. That is a reason to keep the
  Engine definition provisional until its admitted Factory cases are settled and executed, and not
  a reason to treat it as already fixed.
- **Consequence:** fixing an unreleased Engine semantic mistake does not require minting
  `engine-semantics:v2` under current rules. The READY Engine applicability question presumes "an
  existing fixed definition" and "retained historical v1 results". Its identity-partition framing
  (whether v1 can admit V2 cases "without changing its whole definition") therefore dissolves. What
  remains is the substantive question of which interpretation each admitted Factory case gets,
  under one still-correctable Engine definition.
- **Future Engine boundary:** the first acceptance of a run result, or of evidence attributing
  results to the identity, into a custody-declared retained authority; or a declared
  published-reliance scope such as a supported runtime/interchange contract with a consumer. Under
  either, the retained-conformance-fixture promise in the Determinism Contract rule 5 applies from
  that moment.

## Factory V1 → V2 under both cases

- **Case 1: V1 had legitimately crossed.**
  - A distinguishable successor is needed, and the current V2 grammar is a reasonable one.
  - Ordinal "v2" is acceptable but mildly misleading. The successor does not supersede V1 content;
    it admits V1 production content plus an optional record, and the continued-V1-publication
    question exists precisely because "v2" suggests replacement. A semantically descriptive
    committed name would be clearer, but naming is secondary here.
- **Case 2: V1 had not crossed (the evidence supports this case).**
  - Spatial semantics should have been added by correcting the one provisional Factory grammar.
    Its proving vectors would change in place.
  - That removes: the separate V1 policy; the V1-vs-V2-absent duplicate identity; the cross-policy
    comparison and verifier seam for a V1→V2 transition; the "continued V1 publication" support
    decision; and the V1 row of every Engine applicability matrix.
  - The composition rule itself (closed grammar, explicit optional records, one aggregate
    fingerprint, absence ≠ authored zero) stands on its own merits and does not depend on the
    freeze. The frozen-V1 premise did not decide *whether spatial facts belong in the aggregate*;
    the composition analysis did that independently. It decided only *that the aggregate had to be
    a new coexisting policy*.

## Adversarial analysis (self-administered only)

This is the author's self-challenge, not an independent review.

- **"The public repository is itself published reliance."** Rule 5 requires the *owning contract*
  to publish reliance, and the support policy says "the existence of an API or serializer does not"
  create support. The V1 specification's stability sentence states a consequence of claimed
  attribution; it names no consumer, scope or custody. I treat this as insufficient.

  Residual risk: a reviewer could hold that a normative, public "must produce the same fingerprint
  across software versions" sentence *is* a published promise whatever its stated basis. If so,
  candidate B becomes the operative reading. The conclusion then changes from "V1 is not fixed" to
  "V1 was fixed by an unevidenced, voluntary declaration whose cost should be reconsidered by an
  explicit authorized transition". Under rule 6 that transition cannot free an *attributed*
  identity, but no attributed record exists. This is the most important point for the independent
  reviewer to test.
- **"Temp-dir acceptance by `FileControlledRevisionAuthority` is 'acceptance by an authority that
  never declared custody', which rule 5 calls a defect, not a waiver."** Rule 5 concerns an
  authority that retains what it accepts. A `@TempDir` instance is a scratch store whose accepted
  facts cannot outlive the test. The support policy's "later deletion is not proof of
  disposability" guards against disposing of *retained* use, and nothing here was ever retained
  beyond the test JVM. I checked the three constructing tests; each uses `@TempDir`. Residual risk:
  the adapter has a public constructor, and any future non-test instantiation on a durable path
  would cross the boundary with no declaration. That is exactly case 7, and it motivates the
  refusal part of H.
- **"The prior maturity research already considered this and chose non-rebinding over
  correctability."** It rejected *eager* durability from "internal publication" and "first need for
  a deterministic identifier". That supports this report. This report does not challenge the rule;
  it challenges one application of it.
- **Stale baseline.** I re-checked immediately before persistence; `main` is unchanged.
- **Possibility treated as necessity.** I do not claim a provisional token is *required* by
  external practice. The requirement (c) rests on case 13, which is Arcogine-specific, and it could
  also be met by abandon-and-reissue (F) or by committing an existing definition unchanged (B).
- **Overreach on V2.** I do not claim that collapsing to one Factory grammar is mandatory. B
  remains a legitimate owner choice. My claim is narrower: the *stated* basis for the V1/V2
  structure is false, so the structure has to be justified afresh or removed.

## Surviving invariants

1. Deterministic identity generation, in-process Factory publication, implementation landing on
   `main`, a "normative" document status, golden/test vectors, and acceptance by a test-scoped or
   otherwise undeclared scratch authority instance are not attribution and fix nothing.
2. A definition becomes non-rebinding only through an identifiable event:
   - acceptance at the commit boundary of an authority that has declared custody; or
   - a declared published-reliance scope; or
   - an explicit, recorded owner commitment.

   The owning contract records that event as evidence. It never records only an assertion.
3. A committed identity token never denoted another definition, anywhere, including repository
   history. Pre-promotion definitions therefore use a token distinguishable from the eventual
   committed one, or keep a definition that is later committed unchanged.
4. Retained authorities refuse identities not recorded as committed (declaration precedes
   admission). This extends the existing support-policy rule into an authority-side refusal.
5. Factory and Engine promote independently. Engine promotion should not freeze specified but
   unexecuted rules without a deliberate decision to do so.

## Transferability and reuse

- **Potentially transferable result:** "a durability freeze justified by implementation or
  'release' language can survive a rule change by being re-described in the new rule's vocabulary
  without the new rule's evidence". This is a governance failure mode, not a property of Arcogine
  semantics.
- **Arcogine-specific dependencies:** the 2026-09-21 consolidation, which rewrote decision records
  into current architecture; and the token-in-canonical-bytes property.
- **Evidence level:** one occurrence.
- **Synthesis-seed candidate (for reconciliation to judge, not admitted here):** "freeze claims
  must cite their fixing event; restating an inherited freeze under a new rule is a recurrence
  risk". *Revisit when* a second inherited durability or support claim is found restated without
  its evidence.
- **Reusable asset:** the claimed-evidence-vs-concrete-referent audit table above, as a review
  technique for any "has attributed records" claim.

## What did not survive

- "V1 is fixed because published fingerprints, controlled revisions and stored canonical artifacts
  reference it." Unsubstantiated. The referents are an in-process validation step and `@TempDir`
  test stores.
- "Shipping in implementation" or "released" as the freeze trigger, when no release event exists.
  Contradicted by current rules 3 and 5, and by every external source examined.
- **Reusable negative knowledge:** Factory's domain word "publication" (validated model →
  immutable in-memory version) must not be read as rule 5's "publishes reliance". The equivocation
  is load-bearing in the V1 claim.
- SemVer "v0" as the cure for proliferation. SemVer freezes every *release*, 0.x included, so it
  does not provide in-place pre-promotion correction.
- A universal lifecycle state. Still not needed. Rule 2's binary boundary plus evidence recording
  covers the observed failure.

## Confidence and limitations

- **High:** the repository facts, within the stated search scope; that V1 and Engine v1 have no
  retained attribution; the provenance of the freeze claim.
- **Medium-high:** that the Factory composition rule is independent of the freeze premise while the
  successor structure is not.
- **Medium:**
  - requirement (c) (token distinguishability), which rests on case 13's ambiguity and not on an
    obligation;
  - the recommendation to prefer correction over candidate B, which is a cost judgment for the
    owner.
- **Limitations:**
  - no visibility into forks or third-party builds of the public repository;
  - the full reports and adversarial reviews of the concluded maturity and composition
    investigations were not retained, so I cannot check whether they audited V1's attribution
    evidence;
  - I did not examine every planning document for dependent sequencing beyond those cited;
  - the external sources are analogues, not normative for Arcogine.

## Unresolved unknowns

- Whether the owner *wants* an early voluntary commitment for V1 (candidate B), for example to keep
  proving vectors stable, given its costs.
- The concrete refusal mechanism for retained authorities: a verifier registry scoped to committed
  policies, a composition-root custody declaration, or review only. This is bounded design unless
  equality or acceptance semantics prove unsettled.
- Which token scheme (a provisional marker in the token, or abandon-and-reissue) best fits the
  canonical-prefix property. This is a naming consequence to settle in reconciliation.
- The Engine applicability substance, re-scoped as: which interpretation each admitted Factory case
  receives, under one correctable Engine definition.

## Durable consequences (recommended; not performed)

1. **Architecture Overview, semantic evolution and support:** add invariant 1 (what is not
   attribution) and the evidence requirement of invariant 2. Clarify that rule 2's correction
   allowance applies to every contract whose fixation is not evidenced. No lifecycle state.
2. **Semantic contract support:** require a fixation record (fixing event, or "no fixation") in
   each owning contract. Add the authority-side refusal of invariant 4 to the review cases, and add
   the "publication ≠ published reliance" caution.
3. **Factory Model v1 / v2 and the Factory semantic-evolution contract:**
   - replace the unsubstantiated "has attributed records" sentence;
   - replace V2 §10's "shipped publication path" test with the rule 2/5 boundary;
   - have the owner decide explicitly between committing V1 early (candidate B, recorded as a
     commitment with its cost) and collapsing to one provisional Factory grammar (Case 2);
   - reconsider golden vector 16 and the continued-V1-publication question accordingly.
4. **Engine Semantics v1:** qualify the unconditional new-version rule by rule 2; record "no
   fixation" and the intended promotion boundary. The Determinism Contract rule 4 needs the same
   qualification.
5. **Controlled revisions / governance:** record that no custody-declared retained instance exists
   and what declaration must precede one.
6. **Research register:**
   - mark the Factory-composition result as reopened for its successor-identity consequence only;
   - re-scope the READY Engine applicability question before execution;
   - note that the maturity conclusion's rule survives and that its "attributed Engine definition"
     open item has no current concrete case.
7. **Decision-rationale record:** the composition record's Context bullet is historical and must
   not be edited to change meaning. A new dated record should cross-link it if the reconciliation
   changes the V1/V2 structure.
8. **Knowledge transfer:** preserve the audit table technique and the publication-equivocation
   negative knowledge in the support policy's review cases. The report itself need not be retained
   beyond reconciliation.

## Implementation implication

None from this report. Any code change (identity tokens, V1/V2 collapse, authority refusal,
semantics-version propagation) follows only from a reviewed reconciliation and admitted planning.
Until reconciliation, current code and documents stand.

## Follow-up triggers

- An independent adversarial review is required before any of the above is reconciled.
- Reopen this conclusion if either of the following is found:
  - a non-test, custody-bearing authority instance that accepted records attributed to either
    identity;
  - an outward consumer or release that published reliance.
- Revisit the Engine promotion boundary when semantics-version propagation lands or a retained run
  or evidence authority is introduced.

## Sources

- Semantic Versioning 2.0.0, <https://semver.org/spec/v2.0.0.html>, items 3, 4, 9. Verified
  2026-09-25.
- Kubernetes documentation, "API Overview — API versioning",
  <https://kubernetes.io/docs/reference/using-api/#api-versioning>. Verified 2026-09-25.
- multiformats/multicodec README, status column,
  <https://github.com/multiformats/multicodec/blob/master/README.md>. Verified 2026-09-25.
- IETF RFC 7595 / BCP 35, *Guidelines and Registration Procedures for URI Schemes*, June 2015,
  §3, §4, §7.1, §7.3, <https://www.rfc-editor.org/rfc/rfc7595.html>. Verified 2026-09-25.
- Repository delivery-history provenance: merged PRs #175 (V1 fingerprint contract, 2026-08-27),
  #252 (Engine semantics contract, 2026-09-04), #345 (Engine semantics identity, 2026-09-18), #370
  (architecture consolidation, 2026-09-21), #387 (Factory composition reconciliation, 2026-09-23),
  #380 and #384 (legacy web and API/CLI retirement).
