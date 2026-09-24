# Adversarial review: semantic-contract promotion boundary, revision 2

> **Disposition:** REOPEN (scoped; see § Verdict and its scope)
>
> **Date:** 2026-09-25
>
> **Authority:** Research evidence only. This review does not amend architecture or
> specifications, change any identity's meaning, change register state, or admit implementation.
>
> **Risk:** High (semantic identity, historical attribution, persistence, compatibility).
>
> **Evidence workspace:** `research/semantic-contract-promotion-boundary-review`
>
> **Reviewed report:** commit `e7eeedebf2828d467f31ab02a1ce71104ebc4712`,
> `workspace/research/investigations/semantic-contract-promotion-boundary.md`, blob
> `80ba0553563107afc8c7678de46a6e085e610b1b`, 784 lines, self-identified as revision 2.
>
> **Report's stated baseline and independently resolved live-main review baseline:**
> `b9d6e8b0e3b10f07cbc6777e3bc3abbce2f45c19`. Re-checked immediately before persistence; unchanged.
>
> **Prior review in this workspace:** commit `9586901f9809a17c88ed0239700612eaa1a4c3d2`, disposition
> REOPEN, binding to revision 1 (`8fa235b5621e3f97fd41c1aa327d6303e464c65d`) only.
>
> **Independence:** see § Independence and anchoring control. This is a fresh review session with no
> responsibility for authoring or defending either report revision or the revision-1 review. It is
> not the author's self-challenge. A different model family from the author's could not be verified.

## Verdict and its scope

Revision 2 repairs every defect the revision-1 review named. Its attribution audit is correct and
reproducible. Its finding that the Factory V1 "has attributed records" sentence is unsupported, and
contradicted by its own delivery context, holds, and this review strengthens it. The repaired
undeclared-acceptance invariant, the epistemic rule, the rebuilt matrix and the downgrade of token
requirements to options also survive.

The report's central answer does not survive. It says the basis of V1's fixation is an open owner
choice between two "defensible", "balanced" readings: R1 (fixation derived from attribution, so an
authorized narrowing can reopen correction) and R2 (an operative commitment whose justification is
wrong). It says the Engine's unconditional new-version rule is an unexplained "genuine
specification conflict" with rule 2. Both answers rest on evidence the report said was unavailable
but which is retrievable. That evidence is the consolidation's own reviewed research and review,
its abandoned first reconciliation, and the handoff that produced the current V1 sentence. Together
with the part of overview rule 6 the report did not quote, it shows:

1. an omitted, better-evidenced basis for fixation, **carried-label non-reuse across support
   withdrawal** (call it R3); and
2. that R1's correction path conflicts with current repository authority.

That is a viable omitted model that materially changes the answer to the bounded question, so the
disposition is **REOPEN**. The disposition is scoped: it covers executive conclusions 2, 3, 5 and
7, § Factory V1 commitment status, § Engine commitment status, surviving invariant 2(c), durable
consequences 1, 3 and 6, and the related confidence, limitation and follow-up rows. It does not
reopen the attribution audit or the other surviving invariants (§ What survives). It is not a
finding that any retained V1 or Engine record exists. Current specifications stay authoritative
until a separate, reviewed reconciliation changes them.

## Independence and anchoring control

- **Reviewer:** a fresh session, running as Claude Opus 5.5 (Anthropic). The report and both prior
  artifacts are committed under the repository owner's human Git identity and do not record the
  authoring model, so a different model family cannot be verified. This satisfies the "fresh
  isolated session with no responsibility for the original conclusion" condition, not the
  preferred "different model family" condition.
- **Sequence actually followed:**
  1. Resolved live `main`.
  2. Fetched the workspace and confirmed the report's identity and completeness from its header
     block and its last eight lines.
  3. Read the research handoff (the brief) in full.
  4. Read the Researcher contract and the research operating model.
  5. Independently read the Overview's semantic evolution rules, the support policy, Factory
     Model v1 and v2, Factory Design §11–§11.1, Engine Semantics v1 (§1, §14), controlled
     revisions, the register rows, both concluded investigations, planning, and the V1/Engine
     code and tests.
  6. Traced the V1 sentence's history, predecessor decision record 0006, and PR #370.
  7. Only then read revision 2 in full, then the revision-1 review.
- **Exposures before independent grounding:**
  - revision 2's commit message, a one-sentence change summary;
  - the header block (status, baseline, revision relationship, review status; no conclusions);
  - the source list at the end of the report, which names #370 "including the owner's estate
    declaration".

  That last item pointed at #370. #370 would also have been reached independently, because
  `git log -S` on the V1 sentence leads there. The findings below come from primary sources
  located and read after that point, most of which the report did not cite.
- **Shared workspace:** used for persistence only; it neither weakens nor establishes independence.

## Independent reconstruction (before reading the report)

The authorities were read in this order, before the report's reasoning:

1. **Overview rules.** Rule 2 fixes a definition at the first retained or accepted attributed
   record. Rule 5 creates obligations from published reliance, from declared-custody acceptance,
   and (as a defect) from undeclared acceptance. Rule 6 opens: "Withdrawing support never frees an
   identity for changed meaning."
2. **The V1 specification** justifies fixation by "published fingerprints, controlled revisions
   and stored canonical artifacts".
3. **PR #370's description** records the owner's 2026-09-19 declaration that no retained artifact
   or consumer relied on the estate. It also withdraws pre-reset support commitments.
4. **History of the V1 sentence.** Pre-reset, decision record 0006 fixed V1 "once
   `factory-model:v1` ships in implementation". On `main`, the "has attributed records" sentence
   first appears in #370.

From these the reviewer reconstructed the candidates the brief requires. The reconstruction also
produced the discriminating case that later proved decisive: *a definition that carried a
commitment, whose support is then withdrawn by an authorized transition, with no attributed
records. Is in-place correction under the same label permitted?* Rule 6's first sentence answers
no. A reading that answers yes must explain that sentence away.

## Challenges and results

### 1. Omitted evidence and model: the consolidation fixed carried labels by non-reuse

**Challenged:**

- executive conclusion 2 and its confidence row "Which of R1 and R2 is operative: Open … evidence
  is balanced";
- § Factory V1 commitment status;
- executive conclusions 5 and 7;
- durable consequences 1 and 6;
- the follow-up trigger that would "strengthen the correction side for Factory".

**Evidence considered.** None of these is repository authority. All are delivery-history evidence
that the brief requires for establishing whether a deliberate promotion or acceptance event
occurred.

- **The consolidation's research report.** This is revision `c6e67cb5`, which the #370
  description names as the record of the owner's declaration. It is readable from the PR head
  (`refs/pull/370/head`) and in any clone that has fetched it. Its path is
  `workspace/research/investigations/semantic-evolution-meta-architecture-report.md`.
  - line 60: "The reset is not a license to reuse an identifier within the new estate for changed
    meaning";
  - line 220: V1 cross-language behavior "is promised … the owner-directed reset is the separate
    authorized transition that withdraws the pre-reset promise";
  - line 236: withdraw Factory V1 "as a supported estate".
- **Its independent adversarial review.** This is commit `5118a085`, disposition ACCEPT WITH
  QUALIFICATIONS. Two qualifications bear directly on this question:
  - **Q2:** "Identifier non-reuse across the boundary is a rule. A post-reset identity may bear a
    pre-reset label only if its definition is unchanged rule-for-rule/byte-for-byte; otherwise a
    fresh label or namespace is mandatory." It also states the Factory/Engine asymmetry. Its
    stated owners are the reset rule, "Overview (compact statement)" and the "Engine/Factory specs
    (application)".
  - **Q9:** either supersede decision record 0006 with an explicit carry-forward of its
    policy-level invariants, "or record … that v1 remains a supported policy".
- **The first reconciliation, commit `4075aa94` (PR #369).** It encoded these rules as:
  - ADR-0017 §2: "This pre-attribution allowance does not override the cross-reset non-reuse
    rule";
  - ADR-0018 §3: "Reusing a pre-reset label for a changed grammar or field contract is forbidden,
    even before a new release".

  PR #369 was closed with the comment that "this reconciliation strategy has been abandoned in
  favor of the owner-directed ADR baseline reconstruction". That abandoned the decision-record
  vehicle. It did not reject the substance on review: PR #369 has no reviews. These records are
  therefore candidate material, not landed decisions.
- **The reconstruction handoff, commit `e9dae1ec`.** It required the final architecture to "carry
  the useful principles from the report/review, including the qualifications that actually
  constrain future semantics". It also required removing "pre-reset/post-reset narration" and
  "ground-zero" vocabulary.
- **The origin of the current sentence.** The V1 "has attributed records … therefore fixed"
  sentence first appears in commit `b68b3401`, the reconstruction step (`git log -S` over the
  PR #370 head). Earlier on that branch, the V1 basis was the reset's non-reuse rule.
- **Current landed text.** Overview rule 6 opens with "Withdrawing support never frees an
  identity for changed meaning." This is Q2's "compact statement". Revision 2 quotes only rule 6's
  later narrowing sentence (report line 201), and uses rule 6 only for the explicitness of a
  narrowing.

**Result.** The basis substitution the report correctly detects did happen, but it went from the
non-reuse rule, not only from the 2026-08-27 "ships in implementation" trigger. The reconstruction
had to state V1's fixation without reset narration, and did so through the generic attribution
rule, which is factually false for V1. The operative intent carried into current text is R3:

- V1's pre-reset support, including the reproduction promise, was withdrawn by an authorized
  transition;
- V1's definition stays fixed because withdrawing support never frees an identity for changed
  meaning (rule 6 and rule 1);
- any changed grammar needs a distinguishable identity.

R3 is not R2. It does not treat the cross-version reproduction clause as a live support promise
with a validation gap. It is also not R1: no narrowing transition makes V1 correctable in place.

**Effect on the report's conclusion:**

- The evidence is not balanced. R1, as the report uses it, conflicts with current rule 6 and with
  the only precedent the report cites. The report presents R1 as reachable: "coexistence,
  retirement of V1, or one provisional grammar all become open choices"; the composition result
  "becomes reopenable only if the owner selects R1"; and an in-place change of V1's bytes is
  objectionable only "without that act". But #370, the transition the report calls "precedent",
  withdrew support while expressly *refusing* to free labels for changed meaning.
- Coexistence, retirement of V1, and a new single grammar under a fresh label all remain
  legitimate choices. In-place correction under `factory-model:v1` does not.
- The genuinely open Factory residue is narrower. The reconciliation recorded neither of Q9's
  branches. So the current V1 clause "every implementation claiming the policy must produce the
  same fingerprint … across … software versions, and implementation languages" is ambiguous. It
  may be a definitional property of the identity, or a re-published support promise whose
  cross-language fulfilment is an open validation gap. That residue is a support-scope question,
  not a fixation question.
- Executive conclusion 5's claim that "the Factory V1/V2 structure is not shown to be defective"
  survives and is strengthened. On the R3 basis V2 is a necessary distinguishable successor, and
  the composition result is not reopenable through R1.

### 2. Surviving invariant 2(c) lets narrowing un-fix a definition

**Challenged:** invariant 2(c), "normative stability text that the owner has not narrowed by an
authorized transition".

**Proving case N** (derived in § Independent reconstruction): an owning contract publishes a
stability commitment. No record is attributed. The owner then narrows the promise by an authorized
transition.

- As worded, 2(c) either never fired or has stopped firing, so invariant 3 allows in-place
  correction after an "affirmative determination".
- Rule 6 says the opposite.

**Result:** the invariant conflicts with repository authority, in the same class as the
revision-1 defect the prior review found. The repair is small: once any event in invariant 2 has
occurred, the definition stays non-rebinding. Narrowing or withdrawing a promise changes support
scope and never reverses fixation. The epistemic rule (invariant 3) and the
prevention/discovery/remediation split (invariant 4) are unaffected.

### 3. The Engine analysis misses current text and history

**Challenged:**

- executive conclusion 3;
- § Engine commitment status;
- the statement that "released" is "not defined anywhere current" (report lines 196 and 418);
- durable consequence 3;
- the unresolved unknowns about Engine and "released".

**Evidence considered:**

- **Engine Semantics v1 §14** (current text): "Before `engine-semantics:v1` is considered
  released, pinned behavioral fixtures must prove the normative semantics above". Its required
  fixtures include transfer cases (items 7–11), and the specification's status says spatial
  execution is outstanding. By its own terms, v1 is not yet "considered released". The
  consolidation review recorded the same: executor support and "its fixture obligation (never
  released …)" are withdrawable. "Released" is therefore defined in current text, as a
  fixture-gated Engine event, and overview rule 4's example ("retained conformance fixtures for
  released Engine interpretations") reads naturally against it.
- **The Q2 Engine asymmetry.**
  - Factory identities are structurally discriminated by the policy-domain prefix plus artifact
    verification.
  - Engine identities are not, so "a post-reset Engine identity must not reuse
    `engine-semantics:v1` unless the definition is identical". ADR-0017 §2 has the same rule:
    "a changed post-reset interpretation **must not** use `engine-semantics:v1`".

  That is where Engine §1's and Determinism rule 4's unconditional new-version wording comes
  from. It is not an unexplained collision with rule 2. Rule 2's pre-attribution allowance governs
  definitions still under development. A carried label is closed to changed meaning by non-reuse,
  and ADR-0017 §2 states that precedence explicitly. Current text keeps only the compact form, so
  the textual tension the report sees is real. What is wrong is presenting it as an unguided owner
  choice.
- **The factory-simulation-engine readiness plan** records "fixed Engine semantics identity" as
  complete. The report does not cite this current planning statement, although it bears on the
  "fixed" premise.
- **A proving case the report does not examine.** Since `engine-semantics:v1` was named, its
  normative text has been edited in place under the same label. The consolidation (`c599c908`)
  inlined:
  - the session/control rules previously adopted by reference from decision record 0007, plus
    further session rules;
  - the order/child decomposition rules (new §3 rule 4);
  - a new §1.1 consequence 5: incidental implementation ordering is never a tie-breaker.

  Under R3 this is legitimate only as a rule-for-rule restatement of rules already binding
  through the retired records and the §1.1 membership test. Under a correctable-definition
  reading it would be an ordinary pre-attribution correction. This review did not audit the
  expansion rule by rule. The report did not examine it at all.

**Result:**

- No Engine attribution was demonstrated, and that part of the report stands.
- Its "reads as an independent commitment … genuine specification conflict … owner must resolve"
  analysis omits the §14 release precondition, the Q2 asymmetry and the in-place expansion.
- The best-evidenced reading is that `engine-semantics:v1` is:
  - fixed by non-reuse;
  - not released;
  - not attributed;
  - and its whole specified definition, including the unexecuted spatial rules, is fixed ("unchanged
    rule-for-rule"; rule 7 rejects exercised-section freezing).

  Durable consequence 3's question of whether fixation covers the unexecuted spatial rules is
  answered by that evidence, subject to the in-place-expansion audit.

### 4. The claim that the prior reports are unavailable is partly false

**Challenged:** the limitation "The concluded maturity and composition investigations' full
reports and reviews were not retained, so it cannot be checked whether they examined V1's basis."

**Evidence:**

- **The maturity investigation's artifacts are retained.** They are readable from the GitHub
  pull-request head `refs/pull/370/head`, which this review fetched from `origin`: the research
  report, its independent review, the first reconciliation, and the reconstruction handoff. The
  report itself quotes the #370 description, which names that research report revision. Those are
  the artifacts that examine V1's basis.
- **The composition investigation's artifacts are not in shared custody.** `refs/pull/387/head` is
  a single squashed reconciliation commit on top of `main`, and `origin` has no composition research
  branch. A pre-rebase copy of the composition report survives only on a local, unpublished backup
  ref in the owner's clone (`backup/composition-pre-rebase`, last modified at commit `c6414d8a`).
  Whether that copy exactly matches the independently reviewed revision was not verified. It
  records "**Factory Model v1** is attributed and immutable" and "`factory-model:v1` (released,
  attributed)" as current facts, without auditing them.

**Result:**

- For the maturity investigation, an unavailability claim was made without a stated search, and it
  was wrong. The unavailable material carried the decisive evidence in finding 1.
- For the composition investigation, the limitation is essentially right as far as shared custody
  goes.
- The local copy gives secondary, provenance-limited support to the brief's item E concern:
  composition appears to have inherited the V1-attributed premise without auditing it. No
  conclusion in this review depends on it.
- Under R3, that premise's conclusion (V1 immutable) still holds on a different basis, so the
  composition result survives. Its stated premise needs the same basis correction as the V1
  specification. The dated composition decision record stays historical and unedited, as the
  report already says.

### 5. Minor: an executive-summary quotation

Executive conclusion 2 says decision record 0006 "deliberately promoted `factory-model:v1` to a
'permanent cross-language protocol'". In decision record 0006, that phrase appears only in its
rejection of promoting the legacy `contentHash()` (Context, and the rejected alternative). The
report's evidence section quotes it accurately; the summary turns a contrast into a quotation. The
inference that V1 was designed as a cross-language protocol is fair; the quotation marks are not.
This does not affect the disposition.

## What survives

These results were independently re-derived or checked at the baseline and should be preserved:

- **Attribution audit.** No retained or accepted attribution to `factory-model:v1` or
  `engine-semantics:v1` was found in scope. The review independently reproduced:
  - `FileControlledRevisionAuthority` is constructed only in three test classes, each over a
    temporary directory;
  - there is one literal V1 fingerprint in test code;
  - there are no tracked product resources, no GitHub releases, no remote tags, and no `main`
    entry point under `product/`;
  - main code references `EngineSemanticsVersion` only in the type itself, `FactoryRuntime` and
    `EvidenceProvenance`. Evidence provenance is held only by in-memory authorities and evidence
    values. The only main-code filesystem writer is `FileControlledRevisionAuthority`, which is
    constructed only in tests.

  The pre-consolidation gap rests on the owner's declaration, as the report says.
- **The V1 "has attributed records" sentence is wrong,** and is now traced to its cause: a
  reconstruction step that removed the reset basis.
- **Nothing authorizes changing V1 bytes under the V1 token.** This is strengthened: current rule 6
  forbids it, not merely the lack of an authorizing act.
- **Surviving invariants 1, 2(a), 2(b), 3, 4, 6 and 7.** In particular, undeclared retained
  acceptance is an obligation-creating event, unknown is not mutable, and prevention, discovery and
  remediation are distinct. Invariant 2(c) needs the repair in finding 2.
- **The matrix corrections:**
  - case 14 is policy-relative identity, not a falsifier;
  - case U falsifies unrepaired C;
  - the S/E/M/C dimension split.
- **Token mechanisms T0–T4 stay options.** One addition: the non-reuse rule makes T0's in-place
  correction available only to definitions that have never carried a commitment or support, not to
  carried labels.
- **The composition rule and the V1/V2 structure** are not reopened, and they rest on a sounder
  basis than the report gives them.
- **The transferable failure mode "basis substitution creates standing ambiguity",** now with a
  precise mechanism: a qualification that must be "carried" is re-expressed through a generic rule
  when the narration that grounded it is removed, and the generic rule's factual premise is then
  false.

## Qualifications that must survive any reconciliation

Whatever a revised report concludes, a reconciliation of this question must:

1. state V1's and `engine-semantics:v1`'s fixation basis in current, narration-free terms:
   withdrawn support does not free an identity (rule 6), and the definitions are fixed as carried
   whole. It must remove the false attribution premise from the V1 specification;
2. never treat a support narrowing or withdrawal as reopening in-place correction of a label that
   has carried a commitment or support;
3. separately resolve Q9's unrecorded branch, that is, whether V1's cross-implementation
   reproduction clause is definitional or a live support promise. If it is a live promise, the
   cross-language gap is a validation gap, not a reason to withdraw the promise;
4. record Engine v1's actual status (not released, not attributed, fixed by non-reuse) and check
   the consolidation's in-place expansion of its text for rule-for-rule preservation;
5. keep the attribution-audit technique and the undeclared-acceptance, epistemic and
   prevention/discovery/remediation invariants.

## What would close this review

A revision 3 could reach ACCEPT or ACCEPT WITH QUALIFICATIONS if it:

1. incorporates the #370 provenance chain (the research report and review, Q2 and Q9, the abandoned
   first reconciliation as candidate material, and the reconstruction handoff) and evaluates R3
   against R1 and R2. It must either withdraw R1's in-place-correction path or show how it survives
   rule 6's first sentence;
2. narrows the Factory open question to the Q9 support-scope residue;
3. repairs invariant 2(c) so that fixation is not reversible by narrowing;
4. redoes the Engine assessment with §14, the Q2 asymmetry and the readiness plan's "fixed" record,
   and either audits the consolidation's in-place expansion of the Engine text or names it as an
   explicit unknown with a concrete check;
5. corrects the unavailability limitation (the maturity artifacts are on the #370 pull-request
   head; the composition artifacts are not in shared custody) and states the retrieval coordinates
   it used.

None of this requires new external evidence.

## External sources

Revision 2's three external sources (Semantic Versioning 2.0.0; the Kubernetes API versioning
overview; RFC 7595 / BCP 35) were not re-fetched in this review. No finding here depends on them,
and the report does not treat them as load-bearing.

## Evidence retrieved in this review

- **Live `main`:** `b9d6e8b0e3b10f07cbc6777e3bc3abbce2f45c19`, including:
  - Overview semantic evolution rules 1–7;
  - the support policy;
  - Factory Model v1 and v2;
  - Factory Design §11–§11.1;
  - Engine Semantics v1 §1 and §14;
  - controlled revisions;
  - the register rows for maturity, composition and Engine applicability;
  - both concluded investigation briefs;
  - the Factory design capability, spatial runtime consequences and engine readiness plans;
  - `FactoryModelArtifactV1`, `EngineSemanticsVersion`, and all constructions of
    `FileControlledRevisionAuthority`.
- **Parent of `c599c908`:** decision records 0006 and 0015.
- **GitHub pull-request head `refs/pull/370/head`:** commits `c6e67cb5` (research report),
  `5118a085` (review), `4075aa94` (ADR-0017/0018), `e9dae1ec` (reconstruction handoff) and
  `b68b3401` (first appearance of the V1 sentence). All were verified to be reachable from that
  head.
- **Local-only, secondary:** the pre-rebase composition report at `c6414d8a`, on the unpublished
  local ref `backup/composition-pre-rebase`. `refs/pull/387/head` does not contain it.
- **GitHub metadata:** the PR #370 description, and PR #369's closing comment and its (empty)
  review list.
- **Not done:**
  - no build or test execution;
  - no rule-by-rule audit of the consolidation's Engine-text expansion;
  - no inspection of contributor machines or third-party stores.
