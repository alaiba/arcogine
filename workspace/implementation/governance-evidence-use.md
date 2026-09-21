# Governance evidence capability implementation handoff

## Mission

Implement the first headless Governance evidence capability described by `PLAN-GOV-5`: an evidence reference/use contract plus immutable evaluation occurrences, composed additively with the landed Requirement / Assertion / ConformanceEvaluation model.

This is an implementation task, not a research task and not an opportunity to redesign the adopted evidence semantics. The durable semantic authority is `docs/architecture/governance-evidence.md`; where this handoff, the planning document, and the architecture could be read differently, the architecture wins.

The implementation should establish executable evidence for the accepted contract while keeping all unadmitted adjacent capabilities out of scope.

## Repository and branch rules

- Repository: `alaiba/arcogine`.
- This branch was created from live `main` at `b2860f0df59556bd8c5c3b489b8e2336d6c6c3c5`.
- Work only on this implementation branch. Re-resolve live `main` before final review and reconcile the branch if `main` has moved materially.
- Read and follow `AGENTS.md` and `.github/CONTRIBUTING.md`.
- This prompt is transient branch-local scaffolding. Before independent PR review, delete `workspace/implementation/governance-evidence-use.md`; the final merge candidate must contain no tracked `workspace/` paths.
- Do not use the delivery coordinate in durable semantic naming. Code types, comments, tests, architecture, reference, and development docs must describe the capability semantically rather than carrying `PLAN-GOV-5`.
- Preserve the repository owner's human Git author/committer identity. Do not add bot/model attribution or session trailers.
- Do not perform the independent PR review yourself. When implementation is complete, hand the resulting PR to the repository PR Reviewer role.

## Re-ground before editing

Read these files first:

1. `AGENTS.md`
2. `.github/CONTRIBUTING.md`
3. `docs/architecture/overview.md`
4. `docs/architecture/governance-evidence.md` — normative evidence semantics
5. `docs/architecture/governance-conformance.md` — Governance ownership and generic conformance model
6. `docs/architecture/controlled-revisions.md` — historical identity / acceptance precedent
7. `docs/planning/governance-conformance-capability.md` — implementation scope and acceptance cases
8. `docs/development/testing.md`
9. Current Governance implementation:
   - `product/governance/src/main/java/com/arcogine/governance/conformance/ConformanceEvaluation.java`
   - `product/governance/src/main/java/com/arcogine/governance/conformance/ConformanceEvaluator.java`
   - `product/governance/src/main/java/com/arcogine/governance/assertion/Assertion.java`
   - `product/governance/src/main/java/com/arcogine/governance/assertion/EvidenceRequirement.java`
   - `product/governance/src/main/java/com/arcogine/governance/requirement/Requirement.java`
   - `product/governance/src/main/java/com/arcogine/governance/ControlledRevisionAuthority.java`
   - `product/governance/src/main/java/com/arcogine/governance/FileControlledRevisionAuthority.java`
   - `product/governance/src/test/java/com/arcogine/governance/GovernanceModuleBoundaryTest.java`
   - `product/governance/src/test/java/com/arcogine/governance/conformance/ConformanceEvaluatorTest.java`

Do a quick repository/docs search for at least:

- `EvidenceUse`
- `evaluation occurrence`
- `evidence reference`
- `definition rebinding`
- `EXTERNAL_EVIDENCE_REQUIRED`
- `ControlledRevisionAuthority`
- `EngineSemanticsVersion`
- `operational continuation`
- `GovernanceModuleBoundaryTest`

Treat search results as discovery only; fetch/read exact files at the branch/current target revision before relying on them.

## Current landed baseline

The implementation starts from these landed facts:

- Governance controlled revision identity/history, acceptance, and exact historical resolution are implemented.
- Semantic ChangeSet / impact is implemented.
- Requirement / Assertion / RequirementCatalogue is implemented.
- The initial deterministic conformance evaluation / finding slice is implemented.
- `ConformanceEvaluation` is currently a deterministic value with no occurrence identity and no acceptance boundary.
- `Requirement` and `Assertion` equality is identity + version only; wording/source/rule are deliberately outside equality.
- Production `:governance` depends only on `:types`; `:factory` is test-only.
- `GovernanceModuleBoundaryTest` currently forbids Evidence / EvidenceUse declarations because the previous slice was intentionally blocked from introducing them. Retire only those evidence-related guard entries in this slice. Keep authorization, deployment, severity, risk-acceptance, and other later-capability guards.
- No production Engine analytics/evidence integration exists.
- No Operational observation ingestion/correspondence/trust integration exists.
- Durable requirement-definition persistence and durable evaluation-history persistence do not exist.
- Open PR #377 is delivery-process documentation and is not a prerequisite for this capability.

## Required outcome

Implement the narrowest coherent headless capability that satisfies the adopted Governance evidence contract.

### 1. Evidence reference

Provide a representation for one attributable recorded source/result revision or occurrence.

Required semantics:

- Equality means same referenced source/result revision, not equal payload, common subject, truth, applicability, or corroboration.
- A reference is never rebound to different identity-bearing content or intrinsic provenance.
- Producer-owned immutable/versioned handles can directly fulfil this role. Do not allocate a second Governance-global evidence ID merely for ownership symmetry.
- Redelivery of the same source record is the same evidence.
- Equal payloads from independent producers/occurrences may remain distinct evidence.
- Correction/retraction/supersession is a distinguishable attributable relationship/revision, never mutation of an earlier reference.
- Preserve enough intrinsic source meaning/provenance to interpret what was recorded; unknown provenance stays explicitly unknown rather than being synthesized.

Do not freeze a global identifier grammar, URI scheme, canonical serialization, registry, or storage architecture unless the implementation genuinely cannot satisfy the current contract without doing so. Any hard-to-reverse representation decision requires architecture reconciliation.

### 2. Evidence use

Represent how one exact evidence item is considered for one evaluation occurrence and one claim/role.

A use must be able to retain:

- target model fingerprint;
- optional verified controlled revision, never synthesized;
- target semantic scope where material;
- exact requirement definition and exact assertion definition used;
- role, including at least relied-on, considered-but-not-relied-on, and comparator semantics;
- temporal frame where material;
- attributable applicability/reliance determination and explanation.

Preserve these distinctions:

- source/producer-intrinsic provenance versus the Governance use target;
- evidence applicability/reliance versus requirement/assertion scope applicability versus conformance outcome;
- source model versus use target;
- evidence identity versus contextual use.

One evidence item may participate in multiple independent uses. A rollback or equal fingerprint must never copy applicability/reliance from one target use to another.

### 3. Evaluation occurrence

Add an identifiable immutable evaluation occurrence as an additive layer around the existing deterministic `ConformanceEvaluation`; do not change the existing value's equality contract merely to manufacture occurrence identity.

An accepted occurrence must have:

- its own occurrence identity;
- exact requirement and assertion definitions used;
- evaluated subject fingerprint and optional verified revision;
- relied-on uses;
- material considered-but-excluded uses and reasons;
- known material gaps;
- temporal frame / knowledge boundary where material;
- applicability / interpretation rules used;
- final result and explanation;
- a fixed basis after acceptance.

Two accepted occurrences with equal inputs/outcomes are still distinct occurrences.

An unaccepted `ConformanceEvaluation` remains a deterministic result, not a historical occurrence.

Provide a minimal acceptance authority/boundary sufficient to make occurrence acceptance explicit and immutable for this slice. In-memory or fixture-backed authority is acceptable. Do not claim durable evaluation-history persistence.

The acceptance authority may add authority-owned recording metadata, but acceptance must not alter the deterministic evaluation result.

### 4. Exact definition resolution

An occurrence must preserve or authoritatively resolve the exact Requirement / Assertion definitions it used, not merely their id/version keys.

The landed equality contracts intentionally ignore wording/source/rule. Therefore:

- later wording/source/rule changes cannot reinterpret a historical occurrence;
- a materially changed definition must not silently reuse a historical binding;
- if required historical definition material is unavailable, disclose the gap rather than substituting a current definition;
- do not turn this slice into a durable database/registry design.

A bounded in-memory/fixture-backed definition authority or immutable captured definition representation is acceptable if it satisfies the contract for accepted material and makes its limits explicit.

### 5. Conformance composition with evidence

Compose evidence into conformance without turning Governance into a second simulation/analytics/Operational engine.

Required outcome rules:

- Only an applicable assertion with an adequate applicable basis may PASS.
- Missing or unresolved evidence for an applicable assertion ordinarily yields UNKNOWN with an explainable gap.
- Unusable evidence cannot produce PASS.
- Unusable evidence alone cannot make an applicable requirement NOT_APPLICABLE.
- NOT_APPLICABLE remains a scope determination.
- A sufficient alternative applicable basis may still PASS even when another considered item is unusable.
- Absence may support FAIL only when the assertion's own semantics establish a violation from adequate evidence of absence; an incomplete search/feed is not such evidence.
- Malformed/falsely-bound input may be rejected at the boundary rather than converted into a conformance outcome.
- Infrastructure failure is not a real-world conformance violation.

Preserve current structural-evaluation behavior unless a change is strictly required to compose the new evidence capability. Prefer additive APIs/types over destabilizing the existing evaluator.

## Provenance classes this first slice may prove

### Production-backed: structural facts

Use existing `ModelFingerprint` plus authoritative controlled revision/history as the real producer identity/provenance seam. This is the production-backed evidence class for the slice.

### Fixture-backed only: Arcogine-derived analytical results

A fixture may carry explicitly attributed:

- producer-owned model fingerprint;
- `EngineSemanticsVersion`;
- run/result or result-boundary identity;
- explicit material/result-affecting inputs;
- analytical definition/version when supplied.

Do not claim production Engine/analytics integration. Do not invent durable Engine result identity. Do not infer or stamp a missing semantics version.

### Fixture-backed only: external observations

A fixture may retain:

- source identity;
- external subject;
- source/event time and receipt provenance;
- known trust/quality/authenticity material;
- an explicit correspondence assertion supplied at the seam.

Do not create an Operational observation type, ingestion path, correspondence authority, trust model, connector, telemetry adapter, or subject identity system.

## Mandatory executable acceptance cases

Add executable tests covering at least these discriminating cases. It is acceptable to split them across focused test classes; distinguish production-backed structural evidence from fixture-only seam proofs.

1. Revision reuse / rollback: one evidence item used against two controlled revisions sharing a fingerprint, and against a different fingerprint, yields distinct uses with independently determined applicability; no use state is copied.
2. Duplicate delivery vs independent corroboration: redelivery of the same source record resolves to the same evidence and does not increase independent support; equal payloads from independent producers/occurrences remain distinct.
3. Late correction/retraction: correction is a new attributable relationship/revision; an earlier accepted occurrence remains unchanged; a later occurrence can differ while explicitly relating to the earlier material.
4. Unusable evidence: stale/out-of-period/wrong-subject/incompatible material is retained as considered-but-not-relied-on with reason; it cannot PASS or make an applicable requirement NOT_APPLICABLE; an adequate alternative basis may still PASS.
5. Missing evidence vs evidence of absence: missing material yields UNKNOWN with an explainable gap unless assertion semantics plus adequate evidence establish a violation through absence.
6. External observation before correspondence: fixture observation has source provenance and no fabricated Arcogine subject at ingestion; the use owns the explicit correspondence decision.
7. Analytical result provenance: fixture result retains producer provenance unchanged; absent `EngineSemanticsVersion` remains unresolved rather than being filled from current runtime state.
8. Source model vs use target: evidence produced for one model can be used against another only through an explicit comparator role / consumer-owned interpretation; it is not direct proof of the second model.
9. Definition rebinding: later wording/source/rule change under the same id/version cannot alter the exact definition resolved by a historical accepted occurrence.
10. Retired producer: loss of producer executability does not erase attribution; missing material required for later interpretation is disclosed rather than substituted.
11. Context-bound packaging: if evidence and use are embedded/copied together, repeated contextual copies remain traceable to one source identity and do not masquerade as independent corroboration.
12. Accumulating continuation: an accountable operational continuation is rejected or unrepresentable as an evidence-use target; point identities remain the supported target form.
13. Evaluation occurrence: two accepted occurrences with equal deterministic inputs/outcomes have distinct occurrence identities; an unaccepted `ConformanceEvaluation` is not an occurrence; an accepted occurrence's basis cannot be rebound.
14. Boundary rejection: malformed or falsely bound input is rejected explicitly rather than translated into a conformance result.

Also preserve the existing conformance tests for PASS/FAIL/UNKNOWN/NOT_APPLICABLE, model-fingerprint binding, accepted controlled-revision binding, and deterministic repeated evaluation.

## Ownership and architecture invariants

- Governance owns requirement/assertion definitions, evidence use, applicability/reliance interpretation, evaluation occurrences/basis, findings, and later Governance decisions.
- Source producers own their intrinsic identity/provenance and calculation/acquisition semantics.
- Operational Execution owns external-observation acquisition, correspondence, trust/authenticity, and reconciliation.
- Engine/analytics producers own analytical definitions, Engine semantics, result identity, and calculation semantics.
- Governance must not depend on Factory/Engine/Operational production implementations to perform this slice.
- Keep production `:governance` dependent only on `:types`.
- Do not add Spring or interface-layer dependencies to Governance.
- Do not make API/serialization shapes define domain identity.
- Preserve explicit unknown/missing states; never infer provenance.
- Evidence targets are point identities. Do not broaden this slice to accumulating operational continuation identity.
- Do not conflate evidence source authenticity, recording provenance, actor attribution, use applicability, evaluation authorization, or conformance outcome.

## Explicit non-goals

Do not implement or introduce:

- telemetry ingestion;
- connectors;
- source trust/authentication infrastructure;
- Operational correspondence authority;
- Operational observation domain types;
- document-management or vector-search systems;
- evidence lake/store architecture;
- production Engine analytics integration;
- Engine analytics redesign;
- durable Engine result identity;
- generic verification framework;
- signature/PKI infrastructure;
- database/event-store selection;
- durable Governance evaluation-history persistence;
- universal evidence source taxonomy;
- universal subject reference;
- generic actor identity;
- authorization / governed change / external workflow association;
- exceptions / risk acceptance;
- framework/control mappings;
- audit/compliance projections;
- severity taxonomy unless independently required by already-landed semantics (the current boundary guard keeps it out);
- any other GOV-6+ capability.

Do not solve open research questions in code. In particular, do not decide the simulation analytics ownership boundary while constructing an analytical-result fixture.

## Likely implementation surfaces

Choose the smallest representation that satisfies the semantic contract; these are navigation hints, not mandated class names:

- `product/governance/src/main/java/com/arcogine/governance/`
- `product/governance/src/main/java/com/arcogine/governance/conformance/`
- `product/governance/src/main/java/com/arcogine/governance/assertion/`
- `product/governance/src/main/java/com/arcogine/governance/requirement/`
- corresponding `product/governance/src/test/java/...` tests
- `GovernanceModuleBoundaryTest` to remove only the Evidence/EvidenceUse blocked-state declarations and retain all downstream guards.

Prefer a small cohesive package structure over a generic framework. Introduce interfaces/authorities only where they have a concrete responsibility in this slice.

## Documentation reconciliation

When implementation establishes current behavior:

- update `docs/architecture/overview.md` current-implementation text if the repository's implemented architecture materially changes;
- update `docs/architecture/governance-conformance.md` only if needed to describe the now-implemented current boundary without changing the already-adopted semantic contract;
- update `docs/architecture/governance-evidence.md` status/current-implementation wording as needed, but do not rewrite settled semantics merely to match an implementation convenience;
- update `docs/planning/governance-conformance-capability.md` so the implementation queue truthfully reflects what landed/remains;
- update `docs/README.md` only if its current-state planning/architecture summary becomes stale.

Durable docs must use semantic vocabulary, not delivery coordinates.

If implementation uncovers a hard-to-reverse identity, taxonomy, persistence, serialization, or public-contract choice not already settled by the evidence contract, stop that decision from becoming accidental implementation policy. Reconcile the choice into the architecture/specification that owns it before treating the slice as complete.

## Validation

During implementation, run the narrowest relevant Governance checks, for example:

```bash
cd product
./gradlew :governance:compileJava :governance:compileTestJava
./gradlew :governance:checkstyleMain :governance:checkstyleTest
./gradlew :governance:test
./gradlew :governance:jacocoTestCoverageVerification
```

Run focused tests while iterating when useful.

The committed `workspace/` prompt intentionally makes the branch non-mergeable under the repository transient-workspace invariant. Before final review:

1. delete this prompt from the branch;
2. verify no tracked `workspace/` paths remain;
3. run the repository-owned transient-workspace check;
4. run the appropriate complete gate, normally:

```bash
./arcogine check
```

If the environment cannot execute a required validation surface, report that limitation accurately; do not convert absent validation into a pass.

## Completion / PR handoff

A complete implementation candidate should:

- satisfy the 14 acceptance cases with executable evidence;
- preserve existing Governance behavior and module boundaries;
- make its provenance-class limits explicit;
- contain no tracked `workspace/` material;
- contain no GOV-6+ scope;
- reconcile current-state architecture/planning/documentation with shipped behavior;
- use a stable PR description covering semantic scope, rationale, non-goals, and reproducible validation performed;
- target `main`;
- be reconciled with current `main` before final review if necessary;
- be handed to an independent PR Reviewer for the repository's current-head disposition process.

In the implementation session's final report, state:

- branch and PR;
- exact implementation head SHA;
- semantic capability implemented;
- material production/fixture provenance classes proved;
- tests/validation actually run and results;
- documentation reconciled;
- anything intentionally deferred or blocked;
- confirmation that the transient workspace prompt was removed before review.
