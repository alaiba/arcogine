# Independent adversarial review handoff: Factory Model Semantic Composition

Operate as the Arcogine **Researcher** in **independent adversarial-review mode** for repository `alaiba/arcogine`.

This review is for exactly this completed research report revision:

- Workspace branch: `research/factory-model-semantic-composition`
- Reviewed report commit SHA: `885647023f8945d123cd0c74a8cdff2fb035742e`
- Reviewed report path: `workspace/research/investigations/factory-model-semantic-composition-report.md`
- Report-stated research baseline: `46389bc5d21e0c95ffa9366ce271f9d943d44b35`

Do not review branch tip as a substitute for the exact report revision. Your disposition must bind explicitly to commit `885647023f8945d123cd0c74a8cdff2fb035742e` at the path above.

This review must be performed in a genuinely independent session/model family with no responsibility for defending the original report. If that independence condition is not actually satisfied, state that plainly and do not represent the result as an independent adversarial review.

## 1. Read the operating contract first

Read and follow, in order:

1. `AGENTS.md`
2. `.github/agents/researcher.agent.md`
3. `docs/development/researching.md`, especially the adversarial-review procedure in §9 and evidence-custody rules in §10
4. `docs/research/research-register.md`
5. `docs/research/investigations/factory-model-semantic-composition.md`
6. `docs/research/report-template.md` only where useful for checking report completeness/discipline

Also perform the repository-required quick documentation/source search for the main concepts before deep report reading, including at least:

`FactoryModel`, `FactoryModelV2`, `factory-model:v1`, `factory-model:v2`, `ModelFingerprint`, `canonical bytes`, `optional concern`, `spatial`, `storage`, `topology`, `qualification`, `hierarchy`, `controlled revision`, `Engine applicability`, `V1/V2 coexistence`.

Treat search results as discovery only. Read any load-bearing repository evidence at the exact revision you rely on.

## 2. Confirm the exact review input before substantive work

Resolve the complete report artifact at:

`885647023f8945d123cd0c74a8cdff2fb035742e:workspace/research/investigations/factory-model-semantic-composition-report.md`

Confirm:

- the exact report commit SHA;
- report path;
- workspace branch;
- report-stated research baseline;
- that the report is complete and readable.

If the exact report cannot be resolved, stop with:

`INPUT BLOCKED — ORIGINAL REPORT NOT AVAILABLE`

Do not infer the report from a chat summary, later branch tip, later report revision, or prompt.

Confirming identity and completeness is allowed before anchoring control. Do **not** deeply read or evaluate the report's recommendation yet.

## 3. Enforce brief-first anchoring control

Before reading the report's recommendation in depth:

1. resolve **live `main`** and record the exact review-baseline SHA;
2. read the maintained research brief;
3. re-ground in current repository authorities relevant to the question;
4. independently reconstruct:
   - the major semantic constraints;
   - plausible candidate models, including the simplest/no-new-abstraction candidate;
   - the strongest cases for retaining linear whole-model policies;
   - the strongest cases for compositional representation;
   - plausible cases for independently identified components;
   - plausible cases for profiles/capability sets;
   - likely failure/adversarial cases;
   - what evidence would discriminate among these models;
5. write a compact pre-report reconstruction into the review artifact before deep report evaluation.

Do not use the report's candidate selection, terminology refinements, matrix, or recommendation as the framing source for this reconstruction.

The maintained brief is the authority for required candidates/proving cases/questions, not the report.

## 4. Re-ground current authorities

At minimum inspect the current versions of the authorities named by the brief, including:

- Product Charter;
- Factory Design architecture;
- Factory Resource Semantics;
- Factory Model V2 specification;
- Engine Semantics v1;
- Governance semantic comparison / controlled revision surfaces;
- ISA-95 mapping and standards-alignment material where relevant;
- ADR-0003;
- ADR-0004;
- ADR-0006;
- ADR-0014;
- ADR-0015;
- current Factory v1 canonicalization/fingerprint implementation and tests;
- current `FactoryModelV2` / validator implementation and tests;
- current Engine spatial planning;
- current Factory Design planning/research holds;
- current relevant consumers/adapters.

Inspect history where needed to validate claims about why V2 exists, what is or is not released, and what consumers actually depend on.

If live `main` differs materially from the report baseline, distinguish:

- what was true at the report baseline;
- what is true at the review baseline;
- whether the drift weakens, strengthens, or invalidates any report conclusion.

Do not silently judge the old report against changed facts without making the temporal distinction explicit.

## 5. Then read and attack the exact report

Only after the independent reconstruction above, deeply read the exact report revision.

Challenge its load-bearing claims rather than merely checking prose completeness.

At minimum attack the following:

### A. Candidate B as the surviving structural model

The report concludes that the Factory model should be one canonical aggregate consisting of a required production substrate plus explicitly present typed optional authored concerns.

Try to falsify that conclusion.

Ask whether the report has actually demonstrated:

- that a stable irreducible production substrate exists;
- that future concerns can be cleanly separated without hidden semantic coupling;
- that optional concern presence remains understandable as the concern set grows;
- that cross-concern dependencies do not recreate a global schema matrix;
- that real consumer needs, rather than hypothetical modularity, justify the structure.

### B. Rejection/narrowing of Candidate A

The report says linear whole-model policy evolution fails orthogonal-concern cases structurally.

Test the strongest defense of Candidate A:

- Could whole-policy evolution remain coherent if the number of actual supported concern combinations stays small?
- Is the alleged combinatorial pressure demonstrated by real consumers or only future proving cases?
- Does the no-lift rule necessarily imply bad chronology, or could supported policy lines remain explicit and bounded?
- Is the report over-weighting future optionality relative to current simplicity?
- Does Candidate A have compatibility/provenance advantages the report discounts?

Do not reject A merely because composition is aesthetically cleaner.

### C. One aggregate `ModelFingerprint`

The report says one aggregate fingerprint is both sufficient and required, and rejects independent component identity.

Challenge both "sufficient" and "required".

Look for any current or foreseeable statement that cannot be preserved truthfully by one aggregate fingerprint, such as:

- independent ownership or publication;
- cross-design reuse;
- separately governed concern artifacts;
- independent historical resolution;
- provenance for imported engineering submodels;
- stable references to a concern across aggregate revisions.

Also test whether ADR-0008 or other current decisions truly prohibit future concern identity, or only constrain current controlled-revision semantics.

A component identity should not be admitted merely because code is modular—but it also should not be rejected merely because current examples do not need it.

### D. Canonicalization claim

The report presents concern-tagged, sorted, length-framed segments as a deterministic solution and invokes CBOR-style deterministic ordering as supporting evidence.

Challenge:

- whether the proposed grammar is actually specified enough to prove uniqueness;
- how unknown concern tags are handled across older/newer verifiers;
- whether rejecting unknown tags undermines additive extensibility;
- whether allowing them would undermine semantic verification;
- whether concern-internal grammar evolution simply moves version proliferation inward;
- whether the report's "N concerns, not 2^N policies" claim ignores compatibility combinations and prerequisite constraints;
- whether the external CBOR analogy proves only encoding determinism, not semantic evolvability.

Separate "a deterministic byte format is possible" from "this is the right semantic compatibility model."

### E. Absence semantics

The report treats absence as a first-class authored fact distinct from zero/default/unknown.

Verify that this is consistently true across all proposed concern classes, not only spatial placement.

Challenge cases where absence might mean:

- genuinely not modeled;
- unknown/not supplied;
- inapplicable;
- inherited/externally referenced;
- intentionally empty;
- represented but zero-valued.

Determine whether one generic "segment absent" rule is sufficient or whether some concerns need their own absence-state semantics.

### F. Engine applicability

The report recommends concern-based applicability/dispositions instead of binding Engine semantics directly to a whole-model policy number.

Challenge:

- whether REQUIRED / CONDITIONAL / INERT is sufficient;
- whether "undeclared present concern => inapplicable" is necessary or too strict;
- whether an Engine must understand all present concerns to execute safely;
- whether concern dependencies introduce applicability predicates more complex than policy selection;
- whether applicability belongs in Engine semantics, Factory publication validation, or another boundary;
- whether the current Engine really already demonstrates this model, or the report over-interprets current V1/V2 behavior.

Verify that authoring semantics and Engine interpretation ownership stay distinct.

### G. Concern boundary quality

The report says today's spatial additions form one concern, while future geometry alternatives, topology, storage, qualification, and hierarchy may be distinct.

Challenge the proposed boundary rule:

- Can "predicates close over the concern plus declared prerequisites" be applied mechanically?
- Are handling magnitudes actually part of spatial placement?
- Is geometry separable from placement?
- Can storage be meaningfully non-spatial in Arcogine's actual consumer set?
- Does topology belong to Factory authored semantics, Engine interpretation, or both?
- Are hierarchy/pools semantic Factory facts or consumer/governance views?

Look for hidden coupling that would make the reported boundaries unstable.

### H. Current V2 treatment

The report proposes retaining landed V2 value objects/validator predicates while reinterpreting them as the first optional concern and rewriting the unreleased V2 canonical grammar.

Verify:

- exactly what current V2 implementation is reusable;
- whether any type shape itself encodes monolithic assumptions;
- whether retaining it creates migration debt or accidental authority;
- whether "unreleased canonical bytes/policy" is enough to make this correction low-risk;
- whether any consumer/test already depends on V2 as a complete aggregate.

### I. Evidence breadth and confidence

The report acknowledges that only two concern sets have real standing, the third orthogonal landed consumer does not exist, and spatial behavior has not run through the canonical model.

Challenge whether **High confidence** in B over A/C/D is warranted.

Ask whether the conclusion is:

- proven structurally;
- plausible but under-evidenced;
- dependent on future consumer diversity;
- vulnerable to the absence of runtime evidence.

Do not reduce confidence merely because empirical breadth is limited if the structural proof is genuinely sufficient—but require the report to have earned that claim.

### J. External evidence

Re-verify any external source that materially supports the conclusion.

In particular, test whether:

- RFC 8949 supports only deterministic serialization, not semantic composition;
- Protocol Buffers field-presence behavior is analogous enough to support Arcogine absence semantics;
- AutomationML/standards material is actually load-bearing or merely illustrative.

Downgrade or remove analogies that do not discriminate candidates.

### K. Sibling maturity dependency

The report explicitly defers permanent durability/version/historical-support decisions to Semantic Contract Maturity and Durability.

Verify that it actually stays within that boundary.

Challenge any statement that implicitly decides:

- whether a concern set may grow under the same permanent policy label;
- whether a concern earns an independently durable identity;
- whether old verifiers must remain supported;
- whether `factory-model:v1` can be extended;
- whether current V1/V2 contracts were prematurely frozen.

If the sibling has since produced a report/review by the review baseline, classify that evidence correctly. Do not treat an unreviewed high-risk sibling report as decision-quality.

## 6. Brief-completeness challenge

Verify the report against **every** mandatory proving case and **every** required question in the maintained brief.

Do not accept a question-to-answer map at face value. Follow the references and determine whether each answer is actually supported.

Look especially for proving cases that may have been resolved by assertion rather than discrimination:

- logical storage without geometry;
- spatial storage zones;
- richer geometry;
- explicit topology;
- contended transport resources;
- qualification/resource-dependent performance;
- hierarchy/pools;
- cross-consumer partial models;
- historical concern-set changes;
- cross-concern validation.

If a required case is only hypothetical, determine whether that is acceptable for a structural conclusion or whether additional evidence is needed.

## 7. Falsification and omitted-model search

Actively search for:

- a viable omitted candidate or hybrid;
- a counterexample where Candidate B creates worse compatibility state than A;
- a case where component identity becomes necessary;
- a case where a profile is a real semantic contract rather than presentation;
- a concern dependency cycle;
- a canonicalization ambiguity;
- a controlled-revision comparison ambiguity;
- an Engine applicability case not expressible by concern predicates;
- a consumer for whom partial representation is misleading or unsafe.

Do not manufacture a finding. A clean `ACCEPT` is valid if the load-bearing conclusion survives.

## 8. Evidence discipline

In the review artifact, keep these distinct:

- **Repository fact**
- **External evidence**
- **Inference**
- **Challenge**
- **Disposition / qualification**

For each material challenge, state:

1. what was challenged;
2. what evidence was considered;
3. whether the claim survived, failed, or requires qualification;
4. effect on the report's overall conclusion.

Do not turn reviewer preference into evidence.

## 9. Required disposition

Reach exactly one of:

- `ACCEPT`
- `ACCEPT WITH QUALIFICATIONS`
- `MORE EVIDENCE REQUIRED`
- `REOPEN`

Use the repository definitions from `docs/development/researching.md`.

If `ACCEPT WITH QUALIFICATIONS`, state the exact qualifications that **must survive durable reconciliation**.

If `MORE EVIDENCE REQUIRED`, state the minimum discriminating evidence needed and which conclusions remain unpromotable.

If `REOPEN`, identify the falsified load-bearing conclusion or viable omitted model that materially changes the question.

The review must state explicitly:

- independence condition achieved or not;
- reviewed report SHA;
- reviewed report path;
- report research baseline;
- live-main review baseline;
- what was challenged;
- evidence considered;
- disposition;
- effect on the report's conclusion;
- mandatory reconciliation qualifications, if any.

## 10. Persistence

Use the **same** finite research-evidence workspace:

`research/factory-model-semantic-composition`

Do not create a new review branch merely for phase separation.

Persist the completed review at:

`workspace/research/investigations/factory-model-semantic-composition-adversarial-review.md`

Use another semantic filename only if current repository state provides a concrete reason.

Commit and push the completed review.

The review artifact identity is:

`exact review commit SHA + review path`

The review must explicitly identify the reviewed report as:

`885647023f8945d123cd0c74a8cdff2fb035742e:workspace/research/investigations/factory-model-semantic-composition-report.md`

Do not amend, rewrite, rebase away, or force-push away the handed-off report commit or the completed review commit.

If `main` has advanced and the workspace must be synchronized, preserve the exact report SHA through history-preserving synchronization. If that cannot be done safely, return:

`EVIDENCE PERSISTENCE BLOCKED`

Do not mark the research `CONCLUDED` merely because the review is complete. Durable reconciliation is a separate authority transition.

## 11. Final handoff

After persistence succeeds, return only a compact handoff containing:

- independence status;
- disposition;
- live-main review baseline SHA;
- workspace branch;
- exact review commit SHA;
- review path;
- reviewed report commit SHA;
- mandatory qualifications or evidence gaps, if any;
- next required action:
  - durable reconciliation if the disposition is `ACCEPT` or `ACCEPT WITH QUALIFICATIONS`;
  - evidence gathering / report revision if `MORE EVIDENCE REQUIRED`;
  - reopened research if `REOPEN`.

Do not paste the complete review into chat after it has been persisted.
