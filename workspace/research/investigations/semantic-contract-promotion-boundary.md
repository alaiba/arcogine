# Semantic-contract promotion boundary and premature durability (revision 2)

> **Research status:** ACTIVE. This is a completed report revision. Nothing is reconciled, so the
> question is not `CONCLUDED`.
>
> **Research baseline:** live `main` `b9d6e8b0e3b10f07cbc6777e3bc3abbce2f45c19`. Re-checked
> immediately before persistence; unchanged since revision 1 and since the adversarial review.
>
> **Material under investigation (not landed truth):** workspace branch
> `research/semantic-contract-promotion-boundary-review`. It carries the research handoff, report
> revision 1 (commit `8fa235b5621e3f97fd41c1aa327d6303e464c65d`, this path), and the adversarial
> review of revision 1 (commit `9586901f9809a17c88ed0239700612eaa1a4c3d2`,
> `workspace/research/investigations/semantic-contract-promotion-boundary-adversarial-review.md`,
> disposition **REOPEN**).
>
> **Revision relationship:** this revision supersedes revision 1 as the report's current
> conclusion. Revision 1 keeps its exact commit identity, and the REOPEN disposition binds to it
> only. § Response to the adversarial review maps each of the review's four closing conditions to
> the changes made here.
>
> **Authority:** Research evidence only. This is not accepted architecture, product direction or
> implementation commitment until a separate, independently reviewed reconciliation promotes it.
>
> **Risk:** High (semantic identity, persistence, historical attribution, determinism,
> compatibility obligations).
>
> **Adversarial-review status:** revision 1 was independently reviewed (REOPEN). **This revision
> has not been reviewed.** It was written by a fresh session that read the review before revising
> and is therefore anchored on the review's framing. Before any conclusion here is reconciled into
> canonical architecture or a specification, a new independent adversarial review of this exact
> revision is required.

## Question

> What exact event or retained-use boundary, if any, legitimately promoted the current Factory and
> Engine semantic definitions from mutable proving definitions into durable immutable identities,
> and has Arcogine applied that boundary too early?

Revision 1 treated "was the definition fixed?" as a question about attributed records alone. At
the review's direction, this revision separates four questions that revision 1 conflated. Each
one gets its own evidence scope:

1. **Attribution audit (fact):** has any retained or accepted record actually been attributed to
   `factory-model:v1` or `engine-semantics:v1`, whether the acceptance was declared or undeclared?
2. **Published commitment (interpretation):** does owning text already publish a stability or
   non-rebinding commitment, whatever the attribution facts are? Is that commitment independent,
   or does it only follow from the attribution premise?
3. **Fulfilment evidence:** what evidence shows that an existing promise is met? (A validation
   gap does not erase a promise.)
4. **Desirability:** what should the owner commit to from now on, and at what cost?

## Decision at stake

- What is the true basis, if any, for `factory-model:v1` and `engine-semantics:v1` being fixed?
  Should the owning text be corrected to state that basis, or should an explicit, authorized
  transition narrow the promise?
- What must the fixation rule say so that an actual, undeclared retained acceptance can never be
  treated as permission to change a definition?
- Do the Factory V1/V2 successor structure and the READY Engine applicability framing depend on a
  false premise? Or do they rest on a commitment that survives when attribution is absent?

## Scope and non-goals

In scope: the Factory fingerprint policies (primary case); `engine-semantics:v1` (secondary case);
the controlled-revision authority and the Governance evidence authorities, since they are the only
authorities that could retain attributed records; the cross-domain semantic evolution rules; and
the delivery history of the texts that currently state fixation.

Out of scope, per the handoff: editing canonical architecture or specifications; renaming
code/types/files; implementing any provisional-identity mechanism; migration machinery; choosing
tokens. This report does not answer the Engine applicability question itself, and it does not
choose between the interpretations it leaves open for the owner.

## Executive conclusion

1. **Attribution audit: no retained or accepted attribution was found. For the pre-consolidation
   estate, the owner has positively declared that none existed.** The V1 specification cites three
   kinds of evidence:
   - "published fingerprints" refers to Factory's in-process `FactoryModelPublisher.publish`
     validation step;
   - "controlled revisions" and "stored canonical artifacts" exist only in JUnit `@TempDir`
     stores;
   - no production composition root, release, tag, outward adapter or custody declaration exists.

   The consolidation change that introduced the "has attributed records" sentence also recorded an
   owner declaration (2026-09-19): no retained artifact, external store, release, deployment or
   external consumer relied on the estate. So the sentence is not just unevidenced. Its own
   delivery context contradicts it for the period up to the consolidation. After the consolidation,
   bounded inspection of main code finds no non-test authority instance. This result is **not
   demonstrated anywhere in scope, and declared absent for the pre-consolidation estate**. It is
   not a proof that nothing exists anywhere (§ Absence-claim scope).
2. **Published commitment: V1 has a recorded promotion event, but its current standing is an open
   interpretation question that the owner must settle explicitly.**
   - The accepted fingerprint-contract decision of 2026-08-27 deliberately promoted
     `factory-model:v1` to a "permanent cross-language protocol". It contrasted V1 with the legacy
     hash, which it called "deliberately provisional", and its trigger was "once `factory-model:v1`
     ships in implementation". The implementation shipped. That is a genuine, recorded owner
     commitment, and revision 1 missed it as a commitment.
   - The 2026-09-21 consolidation then withdrew pre-reset support commitments as forward promises
     and replaced them with the scoped model. It restated V1's fixation as a consequence
     ("therefore") of attribution, a premise that does not hold.
   - Two readings are defensible (§ Factory V1 commitment status):
     - **R1:** the current fixation depends on attribution only, so its trigger never occurred;
     - **R2:** the current V1 text re-publishes an operative commitment whose stated justification
       is wrong.
   - Both readings agree on three points:
     - the attribution justification must be replaced;
     - nothing today authorizes changing V1's bytes under the V1 token by inference;
     - either reading can be carried out only by an explicit, owner-authorized statement.
3. **Engine: no attribution was found, and the current text reads more like an independent
   commitment than V1's does.** This reverses revision 1's "even further from a freeze" claim as to
   commitment status.
   - Engine Semantics v1 §1 and Determinism Contract rule 4 state, unconditionally, that an
     intentional result-affecting change is a new version. Engine §1 also promises that the
     identifier, specification and fixtures are retained after retirement.
   - Unlike V1's text, neither is premised on attribution.
   - They sit in unresolved tension with semantic-evolution rule 2 and with the support policy's
     explicit allowance for an unfrozen Engine definition whose stamped records are disposable.
   - The owner must reconcile that tension. Missing metadata cannot settle it.
4. **The proposed fixation rule must add undeclared acceptance and published commitment as
   obligation-creating events, and must treat unknown as unknown.** Revision 1's invariants listed
   declared acceptance, declared reliance and recorded owner commitment. They dropped actual
   undeclared acceptance, which overview rule 5 and the support policy expressly make
   obligation-creating. That falsification stands. In the repaired invariant (§ Surviving
   invariants):
   - a definition becomes non-rebinding at the earliest of three events:
     - declared custody acceptance;
     - actual undeclared retained acceptance;
     - a published commitment in the owning contract;
   - in-place correction requires an affirmative, scoped determination that none has occurred;
   - prevention, discovery and remediation are separate obligations.
5. **The Factory V1/V2 structure is not shown to be defective, and it survives or falls with the
   owner's resolution of point 2.**
   - Under R2, V1 is fixed by commitment and V2 is a necessary distinguishable successor.
   - Under R1 plus an explicit narrowing, coexistence, retirement of V1, or one provisional grammar
     all become open choices under the resulting obligations.
   - Pinning different fingerprints for the same authored production content under two policies
     (V2 golden vector 16) is deliberate policy-relative identity, not a defect. Revision 1 was
     wrong to call it one.
   - The composition rule (one closed policy, explicit optional records, one aggregate
     fingerprint, absence distinct from authored zero) is untouched.
6. **Naming and tokens remain options, not conclusions.** The following are all tested options
   (§ Identity-mechanism options):
   - a visibly provisional token;
   - immutable experimental revision identities with bounded support;
   - a reserved final identity;
   - admission-time binding of an exact definition revision.

   None is shown to be necessary. Revision 1's "the committed token never denoted another
   definition, anywhere, including history" requirement is withdrawn as a requirement.
7. **Reopening consequences stay conditional.** The semantic-contract-maturity conclusion survives.
   The Factory-composition result is not reopened by this report. It becomes reopenable only if the
   owner selects R1 and then chooses to revisit the structure. The READY Engine applicability
   brief's retained-history cases stay valid proving dimensions, but its premise that Engine v1 is
   "fixed" depends on the Engine commitment question in point 3.

**Confidence:**

| Finding | Confidence | Basis |
| --- | --- | --- |
| No retained or accepted attribution was demonstrated in scope | High within scope | Absence is not claimed globally |
| No attribution existed pre-consolidation | High | Rests on the owner's positive declaration, not on inspection alone |
| The V1 attribution sentence is wrong | High | |
| A V1 promotion event is recorded (2026-08-27) | High | |
| Which of R1 and R2 is operative | Open | Owner interpretation; evidence is balanced (§ Factory V1 commitment status) |
| Engine's text reads as an independent commitment | Medium | The conflict with rule 2 is real either way |
| The repaired invariant | High | Its elements are already in current text; this report only assembles them |
| The matrix corrections | High | |

## Response to the adversarial review

| Review condition | What this revision changes |
| --- | --- |
| 1. Resolve, or explicitly leave open, whether existing stability language already publishes a commitment; scope each claim; do not equate unknown with absent. | The four questions are separated. The 2026-08-27 promotion event and the #370 withdrawal and owner declaration are added as evidence. The Factory commitment is left explicitly open with the evidence for each reading. The Engine commitment is assessed separately and reads as stronger. Absence claims are scoped, and the owner's positive declaration is distinguished from inspection. The recommendations of an affirmative "no fixation" entry and same-token correction are withdrawn. |
| 2. Preserve actual undeclared retained acceptance; distinguish prevention from remediation. | Undeclared acceptance is an explicit obligation-creating event (invariant 2b). "Unknown ≠ mutable" is invariant 3. Prevention, discovery and remediation are separated (invariant 4). A test label no longer confers exemption; only the absence of admitted retained use does (invariant 1). |
| 3. Separate policy-relative identity, historical meaning, continuing support and convenience cost; keep viable alternatives. | The matrix is rebuilt with separate semantic, epistemic, enforcement and cost dimensions. Case 14 is dropped as a falsifier and kept as a cost. The strongest form of candidate C (C\*: correct before the boundary, then obey the whole-definition rule) is tested and survives. Bounded-support immutable definitions are tested. |
| 4. Correct the V2 source reading; downgrade the universal token conclusion; reassess Engine and Factory afterwards. | V2 §10 is re-read: its absence premise is a sufficient condition for correctability, and it names first retained attribution as the boundary. Revision 1's converse reading is withdrawn. The token requirement becomes a set of options. The Engine and Factory consequences are restated conditionally. |

The review's anchoring qualification applies to it, not to this revision. This revision is
anchored on the review by construction (§ Adversarial analysis).

## Repository evidence

All items are **Repository fact** at the baseline unless labeled **Inference**.

### Architecture rules that decide fixation

[Architecture Overview, semantic evolution and support](../../../docs/architecture/overview.md#semantic-evolution-and-support):

- **Rule 1** is unconditional: "An identity denotes exactly one definition". A materially changed
  definition "requires a distinguishable identity".
- **Rule 2:** "A definition may be corrected in place only until the first retained or accepted
  record is attributed to it".
- **Rule 3:** "Naming an identity creates none of [the support obligations] automatically".
- **Rule 4** gives as an example of an owning contract promising more "retained conformance
  fixtures for **released** Engine interpretations". "Released" is not defined anywhere current.
- **Rule 5** says obligations arise "when the owning contract publishes reliance, or when an
  authority that has declared its custody accepts a record at its commit boundary". It also says:
  "Acceptance by an authority that never declared custody is a defect to account for, not a
  waiver: the accepted fact is never disposed of to escape the obligation it created".
- **Rule 6:** "Narrowing an in-scope promise requires an explicit, authorized transition under the
  owning contract".

[Semantic contract support](../../../docs/development/semantic-contract-support.md):

- "An unproved promise is a validation gap to close, not a promise erased by missing evidence."
- "Discovering undeclared retained acceptance blocks new admission until custody/support is
  declared and requires accounting for the facts already accepted."
- "Tests, scratch stores, drained events and local runs are disposable only while no retained use
  has been admitted against them; a path name, test label or later deletion is not proof of
  disposability."
- "An Engine that stamps records with a semantics identity must either use a frozen exact
  definition or keep those records explicitly disposable".
- "A contract may be introduced and committed to in the same change."

**Inference (rule 1 vs rule 2):** read literally, rule 2's in-place correction changes what an
already-named identity denotes, which is exactly what rule 1 forbids. The two can be reconciled in
one of two ways:

- rule 1 applies from fixation onward; or
- in-place correction changes an editable draft *document*, not an identity any retained record
  resolves.

The current text does not say which. This matters for the token options below.

### The Factory V1 fixation texts and their lineage

- **Current [Factory Model v1](../../../docs/architecture/factory-model-v1.md), "The policy is
  immutable":**
  > `factory-model:v1` has attributed records: published fingerprints, controlled revisions and
  > stored canonical artifacts reference it. Under the semantic evolution rules its definition is
  > therefore fixed as a whole, and every implementation claiming the policy must produce the same
  > fingerprint for the same v1 semantic content across processes, software versions, and
  > implementation languages.

  A separate paragraph follows: "Changing any identity-affecting rule while still calling the
  policy v1 is forbidden", with a list. The next section is headed "Golden compatibility vectors
  are part of the contract". The header names Factory Model v2 as "Successor policy".
- **2026-08-27, accepted decision `0006-durable-semantic-fingerprint-contract`** (readable at the
  parent of the consolidation commit `c599c908`):
  - its Context calls the legacy `contentHash()` "provisional rather than a persisted, public, or
    cross-process compatibility guarantee";
  - it rejects promoting that hash because it "would turn Java implementation details into
    permanent cross-language protocol semantics";
  - it is headed "Released fingerprint policies are immutable", and its rule reads: "Once
    `factory-model:v1` ships in implementation, every supported implementation must produce the
    same fingerprint ... across processes, software versions, and implementation languages";
  - the same prohibition list and "Golden compatibility vectors are part of the contract" follow.
- **2026-09-21, consolidation (merged PR #370):**
  - the description records an owner declaration supplied 2026-09-19: "everything presently in the
    repository is the complete pre-reset estate — no client, retained artifact, external store,
    release, deployment, or external consumer relies on it — and no pre-reset support commitment
    needs to continue as a promise of the post-consolidation system";
  - it adds: "This consolidation replaces the prior support promises with the scoped support
    model" and "Pre-reset commitments remain historical facts ... Withdrawing them as forward
    promises does not rewrite what they meant";
  - it records a falsification trigger for any use the declaration excluded;
  - the same change wrote the current V1 sentence citing attributed records (first appearance per
    `git log -S`).
- **2026-09-23, Factory composition reconciliation (merged PR #387):**
  - the [composition decision record](../../../docs/history/decisions/2026-09-23-factory-model-semantic-composition.md)
    Context states: "`factory-model:v1` was attributed and immutable";
  - [Factory Model v2 §2](../../../docs/architecture/factory-model-v2.md#2-relationship-to-factory-modelv1)
    states: "V1's grammar, digests, golden vectors, and historical fingerprints are permanently
    unchanged by V2's existence".

  The decision record is historical, not current authority.
- `gh release list` returns nothing, and `git ls-remote --tags origin` returns nothing.

### Concrete referents of the claimed V1 records

| Claimed evidence | Concrete referent found | Retained or accepted attribution? |
|---|---|---|
| "published fingerprints" | `FactoryModelPublisher.publish(model)` validates and returns an immutable in-memory `FactoryModelVersion`. Factory "publication" is this validation boundary ([factory-design.md §11](../../../docs/architecture/factory-design.md#11-publication-identity-and-provenance)). | Not demonstrated. It is an in-process domain operation, not rule 5's "publishes reliance". No outward adapter exists. |
| "controlled revisions" | `FileControlledRevisionAuthority` (public constructor) is constructed only in `FileControlledRevisionAuthorityTest`, `ChangeSetFactoryTest` and `PreChangeConformanceProvingCaseTest`, each on a JUnit `@TempDir`. | Not demonstrated. These instances demonstrate a capability. No retained use is known to have been admitted against them. |
| "stored canonical artifacts" | Written only by those temp-dir instances. No tracked files exist under `product/**/src/{main,test}/resources`. | Not demonstrated. |
| golden vector | One literal V1 fingerprint in `FactoryModelFingerprintV1Test`, plus a synthetic `"a".repeat(64)` digest in `ModelFingerprintTest`. | Fulfilment evidence for reproduction (question 3), not attribution. |

### Engine

- `EngineSemanticsVersion.CURRENT = "engine-semantics:v1"` is exposed by
  `FactoryRuntime.semanticsVersion()`.
- `RuntimeObservationMetadata` carries no semantics identity, a declared propagation gap.
- **Correction to revision 1:** the main-code Governance type `EvidenceProvenance` has an
  `Optional<EngineSemanticsVersion>` field, so evidence references *can* carry an Engine identity.
  Its only main-code holders are `InMemoryEvidenceReferenceAuthority` and
  `InMemoryEvaluationOccurrenceAuthority`. Its main-code construction sites are its own static
  factories; other callers are tests. No retaining authority exists, so this does not change the
  attribution result. But revision 1's claim that the identity "is not stamped on any record" was
  imprecise: the capability exists.
- [Engine Semantics v1 §1](../../../docs/architecture/engine-semantics-v1.md#1-purpose):
  - "An intentional change that can alter outcome for identical explicit inputs — including a bug
    fix that observably changes interpretation — is a new version";
  - "When a version is retired from execution, its identifier, this specification and the
    conformance fixtures of section 14 remain".

  Status: "implementation partial (spatial execution and provenance propagation outstanding)".
- [Determinism Contract](../../../docs/architecture/overview.md#determinism-contract):
  - rule 4: "An intentional change to result-affecting behavior is a new interpretation identity";
  - rule 5: "A retired interpretation keeps its identifier, normative specification and
    conformance fixtures".

  Neither rule is qualified by attribution.
- **2026-09-03, accepted decision `0015-engine-semantics-identity-and-reproducibility`:**
  "**Released** semantics versions are immutable and never reused"; "For every released version
  Arcogine retains its identifier, immutable normative specification, and conformance fixtures".
  No Engine release event is defined or recorded.

### V2 and dependent research

- [Factory Model v2 §10](../../../docs/architecture/factory-model-v2.md#10-identity-and-evolution):
  - "No V2 fingerprint has yet been produced by a shipped publication path or recorded against a
    controlled revision, so this grammar may still be corrected ... the first retained attribution
    freezes the whole definition".
  - Golden vector 16 pins different fingerprints for the same authored production content under V1
    and V2 with the spatial record absent.
- The [Factory semantic-evolution contract](../../../docs/architecture/factory-design.md#111-semantic-evolution)
  distinguishes cross-policy semantic comparison from full-fingerprint equality ("Such equivalence
  is never full-fingerprint equality"). It says: "nothing mandates eternal readers for every policy
  or permanent coexistence of any two."
- The composition decision record adds two points:
  - an earlier argument that dual-policy support "would be a permanent burden did not survive
    review";
  - "no inspected consumer relied" on unchanged complete fingerprints.

  These are historical qualifications; they support the current contract but do not replace it.
- The READY [Engine applicability brief](../../../docs/research/investigations/engine-applicability-after-transfer-boundary.md)
  states "Engine v1 remains fixed". It lists "Factory V1 under Engine v1 and retained historical
  v1 results" among its cases. These are proving dimensions, not an inventory claim that such
  records exist.

### Absence-claim scope

**Search scope:** `git grep` over the baseline tree for `static void main` (none under `product/`);
for constructions of `FileControlledRevisionAuthority` (tests only); for users of
`EvidenceProvenance` and `EngineSemanticsVersion`; and for `factory-model:v1:sha256` literals
(test code and `<digest>` placeholders in docs). It also covers tracked resources (none), GitHub
releases (none) and remote tags (none). Revision 1's additional searches of the parent trees of the
retired interface modules are carried forward; this revision did not repeat them.

**Out of scope:** contributor machines, forks, third-party builds, and ad-hoc local code that might
have instantiated the public authority constructor outside tests.

- For the **pre-consolidation** estate, the owner's positive declaration covers the gap. This
  report relies on that declaration's authority, not on search results.
- For the **post-consolidation** period, only the bounded search applies.
- Third-party retention without an Arcogine publication creates no Arcogine obligation under rule
  5. An *Arcogine* authority accepting and retaining without declaration would. Nothing found
  indicates one, and the scope cannot exclude one absolutely.

## Factory V1 commitment status

This is review condition 1, the central open question. The question is whether the current V1
stability language is an independently published commitment, or a derived consequence of the
attribution premise.

**Evidence for R1 (derived; its trigger never occurred):**

- The sentence's grammar makes fixation and cross-version reproduction a consequence ("therefore")
  of attribution under the semantic evolution rules.
- The consolidation's stated intent was to withdraw pre-reset support promises and re-base all
  obligations on the scoped model. In that model, fixation follows from attribution or published
  reliance, and implementation landing is not a trigger (rules 3 and 5).
- The owner's own declaration in that change says no retained artifact existed. Keeping an
  implementation-triggered promise alive while declaring that nothing relied on it would have been
  pointless, which suggests the attribution sentence was an intended re-basing that misstated the
  facts.
- Every post-consolidation statement of V1's fixation (the V1 specification, the composition
  record) cites attribution, not an independent commitment.

**Evidence for R2 (operative commitment; wrong justification):**

- The current V1 text still publishes normative, unconditional-sounding content:
  - cross-software-version reproduction;
  - a separate prohibition paragraph;
  - "golden *compatibility* vectors are part of the contract".
- The support policy says a contract may be committed to in the same change it is introduced.
- The consolidation did not remove the promise text. It re-published it in the post-consolidation
  specification. Arguably that is a fresh publication under current rules, independent of whether
  the pre-reset promise was withdrawn.
- Post-consolidation owner conduct treated V1 as fixed:
  - V2 was built as a separate successor;
  - V2 §2 says V1 is "permanently unchanged";
  - V2 golden vector 18 pins V1 as a regression.
- Rule 6 requires an explicit, authorized transition to narrow a promise, and none has occurred.

**Inference:**

- The evidence is balanced. Research cannot decide what the owner's published text commits to.
  The owner can.
- The reconciliation does not depend on picking the "right" reading after the fact. Both readings
  require the same explicit act: the owner states the operative basis in the V1 specification.
  - Either V1 is committed, with the actual basis stated (the promotion decision and its
    re-publication), not attribution.
  - Or the promise is narrowed by an authorized transition that records an estate declaration and
    a falsification trigger, in the form #370 already used as precedent.
- An in-place change of V1's bytes under the V1 token without that act would violate R2 if R2
  holds, and would violate rule 6's explicitness requirement under either reading.
- Revision 1's "no fixation; correct in place" recommendation is therefore withdrawn.

**Scope of any commitment:**

- The V1 text binds "every implementation claiming the policy": the definition's reproducibility
  across implementations and software versions.
- It names no consumer, and it makes no decoding, execution or support-horizon promise. The text
  itself disclaims that ("this specification fixes the definition, not a support horizon").
- So even under R2, the commitment is about non-rebinding of the definition, not a compatibility
  estate.
- Cross-language reproduction is a validation gap, because only Java fixtures exist. That gap is a
  question-3 matter and does not dissolve the promise.

## Engine commitment status

- **Attribution (question 1):** not demonstrated, for the reasons in § Engine above.
- **Commitment (question 2):**
  - The current Engine §1 new-version rule and retention promise, and Determinism Contract rules 4
    and 5, are stated without an attribution premise.
  - The predecessor "released" qualifier survives only as the undefined word "released" in overview
    rule 4's example.
  - **Inference:** as written, the Engine text reads more like an independent commitment than V1's
    does. That commitment conflicts with rule 2's pre-attribution correction allowance, and with
    the support policy's permission for an Engine to keep stamped records "explicitly disposable"
    instead of using "a frozen exact definition", which presupposes that an unfrozen Engine
    definition can exist.
  - It is a genuine specification conflict. The resolution belongs to the owner, and missing
    runtime metadata is not evidence of which side prevails.
- **Whole-definition consequence:** if the Engine commitment is operative, then rule 2's whole-
  definition fixation already covers the specified-but-unexecuted spatial rules (§5–§8, §12). That
  may be intended, or it may not. The reconciliation should say which. Revision 1's argument that
  the definition should *stay* provisional because those rules are unexecuted is downgraded to a
  consideration for question 4. Overview rule 7 expressly rejects freezing only exercised
  sections, so it is not a reason by itself.
- **Consequence for the READY Engine applicability brief:** its "Engine v1 remains fixed" premise
  is a commitment-status claim whose basis the brief does not state. Its retained-history cases
  remain valid failure dimensions whether or not such records exist. This report does not
  recommend collapsing the question to "one correctable Engine definition". It recommends that the
  Engine commitment basis be made explicit before or during that investigation, so the brief's
  whole-definition test runs against a stated basis. Factory and Engine may resolve differently.

## Candidate models

- **A. Fixed by attribution (current text taken literally).** Rests on a factual premise that is
  not demonstrated, and for the pre-consolidation estate is declared false.
- **B. Fixed by published commitment.** For Factory this is R2; the Engine text supports B more
  directly. Legitimate under the support policy.
- **C\*. Boundary-obeying correction.** Correct in place before any established fixation event;
  after one, obey the whole-definition rule and mint a distinguishable identity. No new marker is
  required. This is the strongest form of revision 1's candidate C.
- **D. Evidence gap.** The rules are sound and the owning contracts lack stated bases. This is
  compatible with B and C\*: it is the reconciliation work both need.
- **G. Scoped mixed.** Factory and Engine resolve differently, for example Factory under R1 and
  narrowed while Engine keeps its operative commitment, or the reverse.
- **H′. Repaired boundary invariant.** Keep rules 1–7. State the fixation events (including
  undeclared acceptance and published commitment), the epistemic rule (unknown ≠ mutable) and the
  prevention/discovery/remediation split. No lifecycle state.
- **I. Immutable, bounded-support definition identities.** Every materially changed definition
  gets a distinguishable identity, including during proving. Each carries only the support its
  owning contract declares, possibly none beyond definition resolvability. Distinct identities do
  not by themselves create a support estate (rule 3; Factory §11.1).

Revision 1's candidates E and F are now treated as identity *mechanisms* (next section), not as
semantic models. They answer "how is a provisional definition kept distinguishable?", not "when is
a definition fixed?".

## Identity-mechanism options

These options are evaluated, not selected.

| Option | What it provides | Main limitation |
|---|---|---|
| T0. One token throughout; in-place correction before fixation (current) | No new machinery | Relies on C\*/H′ discipline. The history ambiguity in case 13 remains. |
| T1. Visibly provisional token corrected in place; a committed token issued at promotion | A signal at a glance; retained authorities can refuse by token | Still rebinds the provisional token across its own history. A moving provisional name is not an acceptable sole retained identity (per the review). |
| T2. Immutable experimental revision identities (draft-NN) with bounded support | No rebinding at all; experimental artifacts can be retained with declared horizons | Mints an identity per change; still needs a support declaration if retained |
| T3. Reserved final identity used only at promotion | The committed token never denoted another definition | Does not by itself make pre-promotion identities refusable |
| T4. Admission-time binding of an exact definition revision (for example a digest of the definition document) behind a mutable alias | Retained records resolve an exact definition even if the alias moves | New machinery. The alias must never be the retained identity. |

**Inference:**

- Case 13's history ambiguity is a real clarity cost when the identity token is inside canonical
  bytes.
- It creates no obligation, because Git history is delivery history, not a retained authority.
- T1–T4 each reduce the ambiguity differently. None is shown to be necessary.
- The support policy already permits retained experimental artifacts with declared definition,
  basis, horizon and refusal. So refusing *every* experimental identity for lacking a promotion
  label would be a stronger mechanism that needs its own justification.
- Refusing identities whose exact definition cannot be resolved *is* justified by rule 4.

## External evidence

These sources were verified by revision 1 on 2026-09-25, and independently by the adversarial
review on the same date. This revision did not re-fetch them. They are design precedents, not
Arcogine authority.

| Source | What it establishes | Where the analogy breaks |
|---|---|---|
| Semantic Versioning 2.0.0, items 1, 3, 4, 9 | A public API may be declared by documentation. A released version's contents must not be modified. 0.y.z is for initial development, where anything may change *between versions*; each release is still immutable. | SemVer versions package compatibility, not persisted records attributed to a definition. An outside trigger ("release") cannot invalidate an Arcogine owner's choice of a different trigger, such as the 2026-08-27 "ships in implementation". SemVer "v0" still mints a new number per change, so it does not give in-place correction. |
| Kubernetes API Overview, API versioning | Alpha/beta/stable names expose different stability expectations in the identifier. | A live store and real users. It shows visible provisional naming is useful, not that Arcogine needs it or needs the same storage lifecycle. |
| IETF RFC 7595 / BCP 35 (June 2015), §7.2–§7.3 | A registration's status can change under a change-control process, with the same name before and after. | Registration permanence is not immutability of canonical semantic bytes. It does not support requiring distinct provisional and final tokens. |

**Removed:** the multicodec source. The review could not re-verify it, and it was not load-bearing.
Revision 1's claim that "all four analogues contradict" an implementation-linked trigger is
withdrawn as too strong: every source shows only that some systems choose release or adoption
triggers.

## Proving cases

The matrix is rebuilt so that each cell's result names its dimension:

- **S:** semantic contradiction (untruthful identity or history);
- **E:** epistemic mishandling (unknown treated as known);
- **M:** enforcement weakness (relies on review, not mechanism);
- **C:** discretionary cost only.

A ✓ means handled truthfully. Only S and E are falsifying.

| # | Case | A attribution | B commitment | C\* boundary-obeying | H′ repaired | I bounded-support immutable |
|---|---|---|---|---|---|---|
| 1 | Golden vector changes before any fixation event | E: treats temp stores as attribution | C: new identity required by commitment | ✓ | ✓ after affirmative determination | ✓ (new identity; no support by default) |
| 2 | Test creates a `ControlledRevision`, then all state is deleted | E | ✓ | ✓ if no retained use was admitted | ✓ (invariant 1) | ✓ |
| 3 | Canonical artifact kept only as a Git test fixture | E | ✓ | ✓ | ✓ | ✓ |
| 4 | Durable example refers to the identity; no consumer is promised | ✓ | ✓ | M: the example's meaning may drift across history | ✓ if the example is declared illustrative | ✓ |
| 5 | Internal authority deliberately accepts under a declaration and must explain months later | ✓ | ✓ | ✓ | ✓ (2a) | ✓ |
| 6 | External consumer persists the identity under published reliance | ✓ | ✓ | ✓ (fixation event) | ✓ (2c) | ✓ |
| 7 | Provisional identity accidentally reaches an authoritative store | ✓ | ✓ | M: detection by review only | M/✓ depending on the refusal mechanism | ✓ (every identity already resolves exactly) |
| 8 | Semantic defect found just before a fixation event | C | C | ✓ | ✓ | C: new identity |
| 9 | Same defect found just after a legitimate fixation event | ✓ new identity | ✓ | ✓ new identity (C\* obeys the rule) | ✓ | ✓ |
| 10 | Spatial semantics discovered while unfixed vs after fixation | E if the fixation is misattributed | C: successor required | ✓ both | ✓ both | C: new identity either way |
| 11 | Engine interpretation changes during proving vs after retained run attribution | E | ✓ (Engine's current text) | ✓ | ✓ | ✓ |
| 12 | Support retired while attributed records remain | ✓ | ✓ | ✓ | ✓ (rule 6) | ✓ |
| 13 | Old commit's test pins identity bytes that a later correction changes | ✓ | ✓ | M/C: history ambiguity, no obligation | M/C (mechanism-dependent; see T1–T4) | ✓ |
| 14 | Same authored production content under two policies | ✓ policy-relative by design | ✓ | ✓ | ✓ | ✓ |
| U | *(new, from the review)* An authority actually retains a record without declaring custody. Later, someone changes the definition because no fixation event is recorded. | ✓ | ✓ | **S** unless C\* counts undeclared acceptance as the boundary | ✓ (2b; invariants 3 and 4) | ✓ |
| P | *(new)* Normative owning text publishes a stability promise with a basis that turns out wrong, and nothing has been attributed | E | ✓ | **S/E** if it corrects in place without an explicit transition | ✓ (2c; rule 6 transition) | ✓ |

**Results:**

- **A fails on E** (cases 1, 2, 3, 10, 11, P). Its only support is the factual premise, which is
  not demonstrated.
- **B is not falsified.** Its costs (1, 8, 10) are discretionary. It is the operative model
  wherever the owner confirms a commitment.
- **C\* survives** only when its "established boundary" includes undeclared acceptance and
  published commitment. With that repair, C\* and H′ are essentially the same semantic model, and
  H′ is C\* stated as invariants. Revision 1's unrepaired C (and revision 1's H) fails U. That was
  the review's falsification, and it stands.
- **I survives.** It is truthful everywhere. Its cost is identity proliferation, which is a support
  burden only if each identity is also given support. Rule 3 and Factory §11.1 say it need not be.
- **Case 14 falsifies nothing.** Policy-relative fingerprints are the specified design (Factory
  §11.1; V2 vector 16). A single-grammar Factory remains an option, not a demonstrated consequence.
- **G is not a separate semantic model.** Every surviving model may resolve differently for
  Factory and Engine, and the current evidence suggests they will.

## Adversarial analysis

This is the author's self-challenge only, not an independent review.

- **Anchoring.** This revision was written after reading the review, and it adopts all four of the
  review's challenges. A next reviewer should specifically test whether it over-corrects toward
  commitment. Two examples to test:
  - whether "re-publication after consolidation" (an R2 argument) is really a fresh commitment, or
    the carried-over text of a promise the owner withdrew;
  - whether the owner declaration in #370 legitimately closes the pre-consolidation attribution
    question, or covers only "support commitments".
- **Is #370's declaration within its own scope?** It speaks of retained artifacts, stores, releases
  and consumers relying on the estate. That is the same subject as attribution. I treat it as
  covering attribution for the pre-reset estate. If a reviewer reads it as covering only support
  promises, question 1's pre-consolidation result falls back to "not demonstrated within the
  bounded search". The rest of the report is unaffected, because it does not rely on a global
  absence claim.
- **"R1 plus narrowing is just C by another name."** No. C inferred mutability from missing
  paperwork. R1 plus narrowing requires the explicit, authorized, recorded act that rule 6 demands,
  with an estate declaration and a falsification trigger. The difference is who decides and whether
  a record exists.
- **"The Engine conflict is editorial: rule 4 plainly means post-attribution."** Possibly. But the
  Engine text also carries a retention promise unconditionally, and the Determinism Contract is
  the owning architectural statement. Calling it editorial would be possibility treated as
  necessity. It stays an owner question.
- **Stale baseline.** Re-checked immediately before persistence. `main` is unchanged at
  `b9d6e8b0`.
- **Conclusion stronger than evidence.** The two open questions (Factory R1/R2, Engine conflict)
  are left open rather than resolved by lean. The only high-confidence claims are the attribution
  audit, the wrong V1 justification, the recorded 2026-08-27 promotion event, the invariant repair
  and the matrix corrections.

## Surviving invariants

1. **Not by itself a fixation event:**
   - deterministic identity generation;
   - in-process Factory publication;
   - implementation landing on `main`;
   - a "normative" document status;
   - golden or test vectors;
   - acceptance by a test-scoped authority instance whose accepted facts cannot outlive the test.

   Each qualifies only *while no retained use has been admitted against it*. A path name, test
   label or later deletion is not proof of disposability.
2. **A definition becomes non-rebinding at the earliest of:**
   - (a) acceptance at the commit boundary of an authority that has declared custody;
   - (b) actual acceptance into an authority that retains the record, without a custody
     declaration: a defect that nonetheless creates the obligation;
   - (c) a published commitment in the owning contract, whether as declared reliance, an explicit
     owner commitment, or normative stability text that the owner has not narrowed by an
     authorized transition.
3. **Unknown is not mutable.** In-place correction requires an affirmative, scoped determination
   that no event in 2 has occurred. Where inspection cannot close the scope, the owner may make a
   positive declaration with a falsification trigger (as in #370). Missing evidence of fixation is
   never, by itself, permission to correct.
4. **Prevention, discovery and remediation are distinct.**
   - **Prevention:** declare custody before admission, and optionally have authorities refuse
     identities that are undeclared or cannot be resolved exactly.
   - **Discovery:** undeclared acceptance blocks further admission until custody and support are
     declared.
   - **Remediation:** preserve the original attribution and the exact definition in force at
     acceptance, and never un-accept.
   - A definition changed in place after an undiscovered acceptance has in effect rebound. The
     retained record keeps the earlier definition's meaning, and the changed definition needs a
     distinguishable identity.
5. **Each owning contract states its fixation basis as a fact or an explicit commitment, never as an
   unevidenced assertion.** Narrowing a stated commitment is an authorized transition under rule 6.
6. **Factory and Engine determine fixation independently.**
7. **Policy-relative identity is not duplication.** Distinct policies may give distinct fingerprints
   to the same authored content. Cross-policy equivalence is explicit comparison, never
   full-fingerprint equality, and distinct identities create no support obligation by existing.

Invariants 1–5 and 7 are assembled from current text: overview rules 2, 3, 5 and 6, the support
policy, and Factory §11.1. What is new is the explicit enumeration and the epistemic rule.

## Transferability and reuse

- **Potentially transferable failure mode (governance, not Arcogine semantics):** "A commitment
  carried across a rule change can keep its text while its stated basis changes, leaving the
  commitment's standing ambiguous." Here the 2026-08-27 promise text survived the consolidation,
  but its trigger was re-described as attribution, which the facts do not support. Revision 1
  described this as "a freeze restated under new vocabulary without evidence". The more accurate
  lesson is that **basis substitution creates standing ambiguity**, not that it creates no
  commitment.
- **Evidence level:** one occurrence, with a possible second in the Engine "released" →
  unconditional change. The reconciliation should judge whether that is the same pattern.
- **Synthesis-seed candidate (for reconciliation to judge, not admitted here):** "when a rule change
  re-bases existing commitments, each carried commitment must state whether it continues, on what
  basis, or is withdrawn". *Revisit when* another carried-over commitment is found with a
  substituted basis.
- **Reusable assets:**
  - the claimed-evidence-vs-concrete-referent audit table;
  - the four-question split (attribution / commitment / fulfilment / desirability);
  - the #370 pattern of an owner's positive estate declaration plus a falsification trigger, as the
    mechanism for closing an absence question that inspection cannot close.

## What did not survive

From revision 1, withdrawn or corrected:

- "V1 is not fixed; record no fixation; correct in place." Withdrawn, because it inferred the
  absence of commitment from the absence of attribution.
- Invariant 2's three-event list. It omitted undeclared acceptance (falsified by case U).
- Invariant 3's universal token requirement ("never denoted another definition, anywhere, including
  history"). Downgraded to mechanism options T1–T4.
- Case 14 as a falsifier of A and B. Policy-relative identity is by design.
- The reading of V2 §10 as adopting a "shipped publication path" freeze trigger. Its absence
  premise is sufficient for correctability, not a converse trigger, and it names first retained
  attribution as the boundary.
- "Engine v1 is even further from a legitimate freeze" and the recommendation to re-scope the Engine
  applicability brief around one correctable Engine definition. Replaced by § Engine commitment
  status.
- "Engine identity is not stamped on any record." The capability exists in `EvidenceProvenance`,
  although no retaining authority exists.
- "All four external analogues contradict an implementation-linked trigger." Too strong; multicodec
  removed.

Still negative knowledge:

- Factory's domain word "publication" (validated model → immutable in-memory version) is not rule
  5's "publishes reliance".
- Naming an identity creates no support obligation.
- A universal `proving`/`promoted` lifecycle is still not needed.

## Confidence and limitations

- **High:**
  - the attribution audit within scope;
  - the V1 attribution sentence being wrong (its own delivery context contradicts it);
  - the 2026-08-27 promotion event;
  - the invariant repair and matrix corrections.
- **Open, with no lean asserted:** Factory R1 vs R2.
- **Medium:** that the Engine text reads as an independent commitment.
- **Limitations:**
  - Contributor machines, forks and third-party builds are not visible. The pre-consolidation gap
    is covered by the owner's declaration, not by inspection.
  - The concluded maturity and composition investigations' full reports and reviews were not
    retained, so it cannot be checked whether they examined V1's basis.
  - This revision did not re-run revision 1's retired-interface searches or re-fetch the external
    sources.
  - This revision is anchored on the review.

## Unresolved unknowns

- Factory: is the current V1 stability text an operative commitment (R2), or a derived consequence
  whose trigger never occurred (R1)? Only the owner's explicit statement resolves this.
- Engine: does the unconditional new-version rule and retention promise override rule 2's
  pre-attribution correction allowance for `engine-semantics:v1`? If so, is freezing its
  unexecuted spatial rules intended?
- Did #370's withdrawal of "pre-reset support commitments" cover the 2026-08-27 fixation promise,
  given that the same change re-published the promise text?
- Which identity mechanism (T0–T4), if any, is worth its cost. This is bounded design unless
  equality or acceptance semantics prove unsettled.
- What "released" means in overview rule 4's example, if anything, now.

## Durable consequences (recommended; not performed)

1. **Factory Model v1:** replace the "has attributed records" sentence with the basis the owner
   selects:
   - **R2:** a commitment basis, citing the actual promotion and publication rather than
     attribution; or
   - **R1:** an explicit, authorized narrowing transition with an estate declaration and a
     falsification trigger.

   Until then, current text stands, and nothing authorizes changing V1 bytes under the V1 token.
2. **Architecture Overview / Semantic contract support:**
   - enumerate the fixation events (invariant 2), including undeclared acceptance and published
     commitment;
   - add the epistemic rule (invariant 3) and the prevention/discovery/remediation split
     (invariant 4);
   - state how rule 1 and rule 2 relate (from fixation onward, or document vs identity);
   - resolve or remove the undefined "released" in rule 4's example;
   - add the publication-equivocation caution and the four-question split to the review cases.
3. **Engine Semantics v1 / Determinism Contract:** the owner resolves the conflict between the
   unconditional new-version rule and rule 2 for `engine-semantics:v1`, and records the basis
   (commitment, or correctable until a stated event). The owner also records whether fixation
   covers the unexecuted spatial rules.
4. **Factory Model v2 §10:** optionally clarify the introductory absence sentence so that it cannot
   be read as a converse "shipped path" trigger. No substantive change.
5. **Controlled revisions / Governance evidence:** record that no custody-declared retained
   instance exists. Also record that the public authority constructor and `EvidenceProvenance`'s
   Engine field are capabilities whose first retained use needs a prior declaration.
6. **Research register:**
   - no change to the maturity or composition verdicts from this report;
   - the Engine applicability brief should state its "Engine v1 remains fixed" basis once item 3 is
     resolved, or name that as a prerequisite;
   - the composition result becomes reopenable only if the owner takes the R1 path and chooses to
     revisit the V1/V2 structure.
7. **Decision-rationale records:** the composition record's Context bullet is historical and must
   not be edited to change meaning. If reconciliation changes V1's basis or the V1/V2 structure, a
   new dated record may cross-link it.
8. **Knowledge transfer:** preserve the referent-audit technique, the four-question split, the
   basis-substitution failure mode, and the estate-declaration pattern in the support policy's
   review guidance. The report itself need not be retained beyond reconciliation.

## Implementation implication

None from this report. Any code change (identity mechanism, V1/V2 restructuring, authority refusal,
semantics-version propagation) follows only from a reviewed reconciliation and admitted planning.
Until then, current code and documents stand.

## Follow-up triggers

- An independent adversarial review of **this revision** is required before reconciliation.
- Strengthen the obligation side if any of these is found:
  - a non-test Arcogine authority instance that retained records attributed to either identity,
    declared or not;
  - an outward consumer or release that published reliance.
- Strengthen the correction side for Factory if the owner states expressly that the post-
  consolidation V1 text was meant only as a consequence of attribution and published no
  independent promise.
- Revisit the Engine question when semantics-version propagation lands, or when a retaining
  evidence or run authority is introduced.

## Sources

- Semantic Versioning 2.0.0, <https://semver.org/spec/v2.0.0.html>, items 1, 3, 4, 9. Verified
  2026-09-25 (revision 1 and the review).
- Kubernetes documentation, "API Overview — API versioning",
  <https://kubernetes.io/docs/reference/using-api/#api-versioning>. Verified 2026-09-25 (revision 1
  and the review).
- IETF RFC 7595 / BCP 35, *Guidelines and Registration Procedures for URI Schemes*, June 2015,
  §7.2–§7.3, <https://www.rfc-editor.org/rfc/rfc7595.html>. Verified 2026-09-25 (revision 1 and the
  review).
- Repository delivery-history provenance:
  - merged PR #175 (V1 fingerprint contract, 2026-08-27);
  - #252 (Engine semantics contract);
  - #345 (Engine semantics identity, 2026-09-18);
  - #370 (architecture consolidation, 2026-09-21, including the owner's estate declaration);
  - #387 (Factory composition reconciliation, 2026-09-23);
  - #380 and #384 (legacy interface retirement).

  Predecessor decision records `0006` and `0015` were read at `c599c908^`.
