# Reconciliation prompt — Governance evidence identity and applicability

> **Purpose:** Temporary handoff for the durable reconciliation phase that follows the completed Governance evidence investigation and independent adversarial review.  
> **Authority:** This prompt is planning/delivery material only. It is not research evidence, accepted architecture, an ADR decision, implementation admission, or a review disposition.  
> **Workspace:** `research/governance-evidence-identity-applicability`. Continue on this same finite research-evidence workspace unless an actual operational constraint requires otherwise.  
> **Immutable research report:** commit `23fe823e68ec39d00e10f66d9851fd25a72308cc`, path `docs/research/investigations/governance-evidence-identity-applicability-report.md`.  
> **Immutable adversarial review:** commit `b23bd439eb21cf44819af54ca866eae79a99c092`, path `docs/research/investigations/governance-evidence-identity-applicability-adversarial-review.md`.  
> **Review disposition:** `ACCEPT WITH QUALIFICATIONS`.  
> **Research baseline:** `d36d9ec5b0591803da2e1803c6ae01467fc0e483`.  
> **Report final-main recheck / review-start baseline:** `bad37c837e63f1fe1a5646ba5744d27a89ade9b9`.  
> **Review final-main recheck:** `c9f6bcf04614662b1432acc01489549d1a0c9142`. At prompt creation, live `main` is still this commit.

Perform the **durable architecture reconciliation** for the Governance `Evidence` / `EvidenceUse` identity and applicability contract. The research phase is complete enough to support reconciliation, but the research artifacts themselves do not become architecture by existence. Translate the accepted evidence into durable Arcogine authority, preserve every review qualification, reconcile planning/current-state statements, and prepare the same workspace for an ordinary independently reviewed pull request.

This is **not Researcher mode** and not implementation. Do not run another investigation unless the reconciliation exposes a genuinely unresolved semantic question that cannot be decided from the accepted evidence plus existing authority. Do not add Governance production types, persistence, adapters, or tests for `Evidence` / `EvidenceUse` in this change.

## 1. Start-of-run grounding and custody

Before editing durable authority:

1. resolve current live `main` and record its exact SHA;
2. resolve the workspace branch and verify that both immutable evidence coordinates above are reachable and unchanged;
3. read `AGENTS.md`;
4. read `.github/agents/work-planner.agent.md` for handoff/planning boundaries, but do not re-plan away the named reconciliation slice;
5. read `docs/development/researching.md` in full, especially reconciliation, evidence custody, research lifecycle, and the knowledge-transfer audit;
6. read `docs/architecture/decisions/README.md` for ADR status/immutability rules;
7. read the exact report at `23fe823e68ec39d00e10f66d9851fd25a72308cc` and the exact adversarial review at `b23bd439eb21cf44819af54ca866eae79a99c092` rather than substituting branch-tip versions or summaries;
8. read the current `main` versions of:
   - `docs/architecture/decisions/0016-governance-evidence-provenance.md`;
   - `docs/architecture/governance-conformance.md`;
   - `docs/architecture/overview.md`;
   - `docs/architecture/decisions/0004-model-identity-revision-lineage-and-external-change-control.md`;
   - `docs/architecture/decisions/0006-durable-semantic-fingerprint-contract.md`;
   - `docs/architecture/decisions/0008-controlled-revision-identity-and-lineage.md`;
   - `docs/architecture/decisions/0012-external-interchange-and-serialization-boundaries.md`;
   - `docs/architecture/decisions/0013-durable-operational-identity.md`;
   - `docs/architecture/decisions/0015-engine-semantics-identity-and-reproducibility.md`;
   - `docs/planning/governance-conformance-capability.md`;
   - `docs/research/research-register.md`;
   - `docs/research/investigations/governance-evidence-identity-applicability.md`;
9. search maintained docs, source, and tests for semantic neighbors before editing; and
10. record any required surface that cannot be inspected.

### Workspace synchronization rule

At prompt creation the workspace tip is the review commit `b23bd439eb21cf44819af54ca866eae79a99c092`, while live `main` is `c9f6bcf04614662b1432acc01489549d1a0c9142`.

Before substantive reconciliation, update the workspace from current live `main` **history-preservingly** if it is behind. Preserve the handed-off report and review commits as reachable immutable ancestors. Do not rebase, amend, squash, force-push, or otherwise rewrite `23fe823...` or `b23bd43...`. If a safe history-preserving synchronization cannot be performed without a semantic conflict, stop and report the concrete conflict instead of rewriting evidence history or guessing.

If `main` has moved beyond `c9f6bcf...`, inspect the intervening changes and determine whether any materially affect the evidence conclusion or reconciliation target. If they do, reconcile deliberately; if they reopen a load-bearing research question, stop the architecture promotion and route that question back through the research lifecycle rather than silently deciding it in implementation prose.

## 2. Quick search terms

Search at least across `docs/architecture`, `docs/planning`, `docs/research`, and relevant Governance/Factory production/tests for combinations of:

`Evidence`, `EvidenceUse`, `EvidenceRequirement`, `evidence identity`, `non-rebinding`, `provenance`, `producer provenance`, `applicability`, `historical attribution`, `historical explanation`, `evaluation occurrence`, `definition resolution`, `RequirementVersion`, `AssertionVersion`, `ModelFingerprint`, `ControlledRevision`, `EngineSemanticsVersion`, `RunId`, `operational continuation`, `external observation`, `correspondence`, `cross-version`, `compatibility`, `UNKNOWN`, `NOT_APPLICABLE`, `audit snapshot`, `PLAN-GOV-5`.

Treat absence claims as bounded searches, not universal proof.

## 3. Objective

Reconcile the independently reviewed research conclusion into durable Governance architecture so a later implementation session does not have to invent identity, equality, history, applicability, provenance ownership, or historical-definition rules.

The reconciliation should normally:

1. revise **ADR-0016** from its current Proposed form into the durable decision that actually survived research/review, and set it to `Accepted` only if the completed change states the semantic decision completely enough to be accepted;
2. reconcile the relevant Governance architecture sections, especially the generic evaluation/evidence model, evidence provenance/applicability, audit reconstruction, and current/downstream boundary text;
3. reconcile `docs/planning/governance-conformance-capability.md` so PLAN-GOV-5's blocker/readiness description reflects the now-durable architecture truth after this change lands;
4. reconcile the research register/lifecycle only when the durable consequence is actually carried by this reconciliation; and
5. complete the research knowledge-transfer audit so temporary evidence/handoff artifacts do not accidentally become permanent architecture authority.

Do not implement the future Java/storage/API shape in the same change.

## 4. Required semantic decision

The durable decision must preserve the report's load-bearing conclusion and the adversarial review's qualifications.

### 4.1 Evidence reference / referent / equality

State the semantic contract, not a storage schema:

- an evidence reference identifies one particular attributable recorded assertion, observation, artifact revision, or analytical result revision/occurrence;
- identity equality means the **same referenced source/result revision**, not byte equality, logical equivalence, common subject, current truth, independent corroboration, trust, or applicability;
- the same complete evidence reference must not later resolve to different identity-bearing content or intrinsic provenance;
- correction, retraction, supersession, or producer reinterpretation creates a distinguishable attributable relationship/revision rather than rebinding an existing reference;
- a source-owned stable logical ID may participate only when the exact relied-on revision/capture remains identifiable;
- a bare digest is content identity, not necessarily source-occurrence identity;
- a producer-owned immutable/versioned handle can satisfy the evidence-reference role; do **not** require a new global Governance evidence ID when the producer identity is already semantically sufficient.

Do not choose UUID, URI grammar, database key shape, canonical serialization, evidence registry, alias/deduplication algorithm, or capture store unless a semantic invariant genuinely requires it. The research did not establish such a mechanism choice.

### 4.2 Evidence versus contextual use

Keep the roles distinguishable even if a future representation embeds them together:

- evidence preserves the independently attributable source/result and its intrinsic provenance;
- an `EvidenceUse`-equivalent relationship records how that exact material was considered for one evaluation/claim;
- one evidence item may have multiple uses with independent subject/revision/claim/time/applicability determinations;
- a use must not rewrite the evidence's source meaning or intrinsic provenance;
- repeated contextual copies are acceptable only if they preserve the same independent source identity and do not masquerade as independent corroboration.

The decision must not mandate separate physical records merely because the semantic roles are separate.

### 4.3 Producer-intrinsic provenance versus use-target binding

Carry the report's first qualification explicitly:

- raw external observations remain independent of Arcogine model/revision identity unless the source itself intrinsically owns such provenance;
- later correspondence to an Arcogine subject/revision is a use/reconciliation determination and must not be fabricated at ingestion;
- Arcogine-derived analytical results **may and sometimes must retain producer-intrinsic** `ModelFingerprint`, controlled revision, `EngineSemanticsVersion`, run/result identity, analytical definition/version, and other result-affecting provenance owned by the producer;
- the target model/revision/claim of a later Governance use is a separate relationship and may differ from the source model that produced the evidence;
- Governance does not take ownership of Operational acquisition/correspondence/trust semantics or Engine/analytics calculation semantics merely by consuming their attributable results.

This must replace/qualify the current Governance-architecture statement that `Evidence` categorically "does not carry a model fingerprint or revision". That statement is too broad. The durable distinction is **intrinsic producer provenance vs. contextual use-target binding**, not simply "fingerprint belongs only on use".

### 4.4 Applicability, requirement scope, sufficiency, and outcome

Carry the report's second qualification explicitly:

- evidence applicability/reliance is distinct from requirement/assertion applicability;
- both are distinct from conformance outcome;
- stale, out-of-period, wrong-subject, untrusted, semantically incompatible, incomplete, or otherwise unusable evidence does not silently produce `PASS`;
- unusable evidence alone does not make an applicable requirement `NOT_APPLICABLE`;
- missing or unresolved evidence for an applicable assertion remains explainable and ordinarily supports `UNKNOWN` unless the assertion's own semantics establish a violation from adequate evidence of absence;
- known considered-but-not-relied-on material and material evidence gaps may need to remain part of the historical evaluation basis;
- cross-version compatibility is explicit, claim/metric/context specific, and consumer-owned; equal or different Engine semantics versions alone never establish comparability.

Do not invent one universal freshness/admissibility/compatibility policy or enum where the research deliberately leaves consumer-specific policy open.

### 4.5 Historical evaluation basis and exact definition resolution

Carry the report's third qualification explicitly:

- a completed evaluation must preserve or authoritatively resolve the exact requirement/assertion definitions actually used, not only labels that could later resolve differently;
- current Java equality of requirement/assertion values by ID + version is not proof that historical definitions are durably resolvable;
- historical explanation must preserve the actual evidence/use basis, material exclusions/gaps, subject fingerprint/revision where applicable, temporal frame, interpretation/applicability rules, and outcome;
- later requirement/assertion wording/rule changes, evidence corrections, trust changes, or policy changes create new historical facts/evaluations rather than rewriting the old basis;
- if necessary historical material is missing or corrupt, the system must disclose the gap rather than substitute current definitions or re-execute under current semantics.

Do not select a requirement-definition registry or persistence mechanism merely to state this obligation.

## 5. Adversarial-review qualifications that must survive

The review disposition is **ACCEPT WITH QUALIFICATIONS**. The reconciliation is incomplete unless every qualification below is either encoded durably or explicitly deferred to a maintained unresolved destination without pretending it is settled.

### Q-A — Evaluation-occurrence identity must be explicit

Decide one of these explicitly in ADR-0016/reconciled architecture:

- the first evidence-capable Governance contract requires an identifiable immutable evaluation occurrence whose historical basis can be referenced; or
- evaluation-occurrence identity remains intentionally deferred and the accepted contract states the exact limit this creates.

Do not leave this implicit. The adversarial reviewer judged the report's rules narrow enough to decide during reconciliation with normal independent PR review; if the reconciliation cannot decide it without new research, add/route the bounded question through the maintained research register rather than smuggling an answer into implementation.

Avoid over-design: this decision is about the semantic identity/referent and historical non-rebinding obligation, not UUID/storage/API selection.

### Q-B — Use targets are point identities; accumulating continuations are out of scope

Do not generalize this contract into binding evidence to an accumulating Operational continuation as though it were an immutable point identity. ADR-0013 already identifies that semantic limit.

For this reconciliation:

- `EvidenceUse`-style target binding is to exact point-in-time semantic/evaluation subjects where the meaning can be made immutable and attributable;
- durable accumulating Operational continuation semantics remain outside this evidence-identity decision;
- a future integration that needs "evidence about a continuation over time" must use the owning Operational contract or obtain a later explicit decision rather than reinterpreting the point-identity rule.

### Q-C — First-implementation evidence must be bounded by producer identities that actually exist

Do not turn the accepted architecture into acceptance criteria that require unavailable production provenance.

The first headless implementation may prove the generic reference/use contract using producer identities/provenance that actually exist or explicit fixtures at the owning seam. It must not claim production support for:

- Operational correspondence/trust identity not yet implemented;
- Engine result identity/`EngineSemanticsVersion` propagation that current product contracts do not yet expose;
- unresolved analytics-definition ownership;
- durable requirement-definition storage that is not yet implemented.

Any later implementation plan must distinguish a fixture/proving seam from a real production integration and must not fabricate missing producer provenance.

## 6. ADR-0016 reconciliation

`docs/architecture/decisions/0016-governance-evidence-provenance.md` is currently `Status: Proposed` and may be edited freely until accepted.

Rework it into a self-contained durable semantic decision. It should cover, as necessary:

- evidence-reference referent and equality/non-rebinding;
- evidence versus use roles without prescribing physical record separation;
- correction/retraction/supersession semantics;
- intrinsic source/producer provenance and contextual use-target binding;
- external-observation revision independence;
- producer-owned analytical provenance including Engine semantics and explicit result-affecting inputs where applicable;
- evidence applicability versus requirement applicability versus outcome;
- cross-version compatibility ownership;
- historical evaluation basis and exact definition resolution;
- evaluation-occurrence identity or its explicit bounded deferral (Q-A);
- point-identity scope / accumulating-continuation exclusion (Q-B);
- first-implementation producer-provenance bounds (Q-C) as an implementation consequence/non-goal rather than a mechanism choice;
- authority boundaries: Governance interpretation/use versus Operational acquisition/correspondence/trust versus Engine/analytics calculation;
- mechanism choices deliberately deferred.

Retain useful existing ADR-0016 decisions where they survived research. Replace proposal text that the evidence disproved or narrowed. Do not mechanically append the report; produce one coherent architectural decision.

If the reconciliation completely states the survived decision and no unresolved load-bearing semantic question remains, set ADR-0016 to `Status: Accepted` in this change. If a load-bearing semantic question remains unresolved, keep it Proposed and do **not** mark the research CONCLUDED or PLAN-GOV-5 ready merely to close the task.

Do not modify Accepted ADRs semantically. If an Accepted ADR appears to conflict with the research conclusion, treat the Accepted ADR as authority and stop to resolve the discrepancy rather than editing it in place.

## 7. Governance architecture reconciliation

Reconcile `docs/architecture/governance-conformance.md` so it is consistent with accepted ADR-0016 and does not preserve stale hypotheses as though they were the durable rule.

At minimum inspect and deliberately reconcile:

- §7 generic conformance/evaluation model;
- §9 evidence attribution/temporality and the `Evidence` / `EvidenceUse` sketch;
- §12 audit reconstruction invariant;
- §13 current/downstream relationship text; and
- any other evidence/provenance/applicability statement found by search.

Required corrections include:

- replace the blanket rule that `Evidence` has no fingerprint/revision with producer-intrinsic versus use-target provenance;
- ensure source-qualified reference/non-rebinding semantics are visible;
- make historical evaluation basis and exact definition resolution explicit enough that the architecture does not imply `id + version` labels alone are sufficient;
- keep evidence applicability separate from requirement applicability and result;
- preserve Operational and Engine/analytics ownership boundaries;
- preserve Q-A/Q-B/Q-C limits without turning them into speculative mechanism design.

The overall architecture document may remain a Proposed architectural reference where appropriate; accepted semantic rules should be clearly grounded in Accepted ADR-0016 rather than being presented as unsupported proposal.

## 8. Planning reconciliation / PLAN-GOV-5

Re-evaluate `docs/planning/governance-conformance-capability.md` against its existing `READY_NEXT` promotion criteria. Do not promote from memory or because research exists.

When this reconciliation is complete, assess whether all four existing criteria will be true on merge:

1. decision-quality research exists;
2. independent adversarial review for the high-risk question exists;
3. the surviving semantic contract is reconciled into Accepted durable authority; and
4. the plan/current-state architecture is reconciled so a fresh implementer can derive behavior and acceptance evidence without inventing evidence identity/lifecycle rules.

If all four are satisfied by this same change, update PLAN-GOV-5 from `DEPENDENCY_BLOCKED` to `READY_NEXT` and replace stale blocker prose with the authoritative contract and bounded first-implementation acceptance direction. If any criterion remains unsatisfied, keep it blocked and state the remaining concrete prerequisite.

Do **not** implement PLAN-GOV-5 in this reconciliation PR. Readiness is admission for a later implementation slice, not permission to mix architecture and production code here.

Any implementation acceptance guidance added to the plan must preserve Q-C and the research non-goals: no telemetry ingestion, connector trust/authentication, generic evidence lake/document system, broad analytics redesign, generic verification framework, or invented producer provenance.

## 9. Research lifecycle and register reconciliation

Research is `CONCLUDED` only when a decision-quality result exists **and** its durable consequence is actually reconciled.

If ADR-0016 becomes Accepted and the corresponding Governance architecture/planning reconciliation is complete in this change, update the maintained research-register entry for this question to `CONCLUDED` and make the durable destination/result explicit.

Keep the research status separate from planning status:

- `CONCLUDED` means the research question's durable consequence is reconciled;
- `READY_NEXT` means the implementation slice has satisfied its planning prerequisites.

Do not use one as a synonym for the other.

If reconciliation intentionally leaves a load-bearing question open, keep the research item `ACTIVE`/appropriate non-concluded state and record the specific unresolved question/destination. Do not claim conclusion merely because ADR prose changed.

## 10. Knowledge-transfer audit and temporary evidence cleanup

Before the reconciliation PR is considered ready for review, perform the knowledge-transfer audit required by `docs/development/researching.md`.

Account explicitly for:

- the accepted conclusion;
- all report qualifications;
- Q-A, Q-B, Q-C;
- reusable proving cases/counterexamples that future implementation/review needs;
- remaining open questions and their maintained destinations;
- reusable source/semantic knowledge that must survive;
- transient search/drafting/process material that can be discarded.

The audit must ensure that everything material has moved into durable authority, maintained planning/research state, or explicit PR delivery history.

Temporary report/review/handoff artifacts are custody material, not architecture authority. Before the final reconciliation tree is ready to merge, remove temporary files that are no longer deliberately maintained, including the execution/review/reconciliation handoff prompts and completed report/review artifacts **when** their material content has been transferred and their exact historical coordinates are recorded in the reconciliation PR description/history.

Do not delete/retire the workspace before the reconciliation lands. The repository may retire the workspace only after the merge and after exact report/review coordinates survive in durable delivery history.

Do not delete the maintained research brief/register entry merely because the research concluded; reconcile them according to the repository's normal research-register compaction/maintenance policy.

## 11. Proving cases to preserve through reconciliation

The durable decision and later implementation acceptance evidence must remain capable of explaining at least these cases. Do not necessarily copy this list verbatim into an ADR; transfer the semantic lesson where it belongs.

1. **Revision reuse / rollback:** one source item is used against distinct controlled revisions with equal fingerprint without copying historical applicability/approval.
2. **Duplicate delivery vs. independent corroboration:** redelivery of the same source record does not become a second independent observation; equal payload from distinct producers/occurrences may remain distinct evidence.
3. **Late correction:** a source correction or retraction does not rewrite the basis of an earlier evaluation; a later reassessment can differ.
4. **Unusable evidence:** stale/wrong-period/wrong-subject/incompatible evidence is explainably excluded and cannot silently produce `PASS` or `NOT_APPLICABLE`.
5. **External observation before correspondence:** the observation retains source subject/time/trust provenance while a later use carries the Arcogine correspondence decision.
6. **Analytical result:** producer-owned model/Engine/run/result provenance is retained without Governance owning the calculation or fabricating missing `EngineSemanticsVersion`.
7. **Source-model vs. use-target:** evidence produced for one model may be used as a comparator for another only through explicit use-owned semantics.
8. **Definition rebinding:** later wording/rule changes cannot change the exact requirement/assertion meaning used by a historical evaluation.
9. **Retired producer:** attribution can survive loss of executability/replay; missing required material is disclosed rather than substituted.
10. **Context-bound packaging:** one physical/contextual record is acceptable if source identity and use context remain semantically distinguishable.
11. **Accumulating continuation:** point-bound `EvidenceUse` semantics must not silently generalize to an accumulating Operational continuation (Q-B).
12. **Evaluation occurrence:** two completed evaluations with equal inputs/outcomes must remain distinguishable if historical attribution requires distinct occurrences (Q-A decision).

## 12. Non-goals

Do not in this reconciliation:

- add/modify production Java types for `Evidence`, `EvidenceUse`, evaluation persistence, or evidence storage;
- select a database, event store, object store, document store, vector store, or evidence lake;
- design signature/PKI/authentication infrastructure;
- implement Operational telemetry ingestion, source correspondence, trust scoring, or twin reconciliation;
- implement/ redesign Engine analytics or settle the separate simulation-analytics consumer-boundary research question;
- invent a universal source taxonomy, subject reference, actor model, verification framework, or compatibility engine;
- choose concrete public serialization/API field shapes unless required to state an existing semantic invariant;
- rewrite Accepted ADRs to fit the new decision;
- claim that a future implementation can provide producer provenance that current producers do not expose;
- merge the research/reconciliation workspace merely because the architecture edit exists without normal independent PR review.

## 13. Validation

Run the narrowest repository-owned validation that exercises the changed documentation/ADR surfaces, following current `AGENTS.md` and `docs/development/testing.md`.

At minimum:

- run Markdown/link/delivery-label/ADR-immutability validation through the repository-owned checks applicable to documentation changes;
- run `git diff --check` or the repository-equivalent whitespace check;
- search the final tree for stale contradictory evidence statements (especially the blanket no-fingerprint rule, stale `DEPENDENCY_BLOCKED` rationale if promoted, stale research status, and temporary evidence/handoff files that should have been retired);
- verify the final changed-file set contains no product/runtime implementation unless a separate, explicitly justified task expanded scope (default: none);
- before final handoff, re-resolve live `main` and ensure any movement is reconciled history-preservingly without rewriting evidence commits.

Do not describe pending/absent CI as passed. Once a PR exists, current-head CI and independent PR Reviewer disposition are separate required evidence.

## 14. Pull request and independent review

After the reconciliation is complete and locally/repository-valid:

1. keep using this same workspace branch;
2. open one ordinary PR to `main` for the durable reconciliation if no PR already exists;
3. in the PR description, record the exact research evidence coordinates:
   - report `23fe823e68ec39d00e10f66d9851fd25a72308cc` + report path;
   - adversarial review `b23bd439eb21cf44819af54ca866eae79a99c092` + review path;
   - disposition `ACCEPT WITH QUALIFICATIONS`;
   - the live-main reconciliation baseline;
4. state how every material qualification/Q-A/Q-B/Q-C was transferred or deliberately deferred;
5. state the knowledge-transfer audit/temporary-file disposition;
6. request/hand off to the repository's ordinary independent **PR Reviewer** workflow; and
7. stop at `READY TO MERGE`; the repository owner performs the final merge.

The researcher's adversarial review is not a substitute for independent PR review of the actual reconciliation diff.

## 15. Final response checklist

Report back with:

- starting and final live-`main` SHAs;
- workspace branch and final reconciliation head SHA;
- evidence report/review coordinates confirmed unchanged;
- files changed and why;
- final ADR-0016 status and the semantic decisions added/changed;
- how the Governance architecture was reconciled;
- explicit disposition of Q-A, Q-B, Q-C;
- PLAN-GOV-5 status and why its promotion criteria are or are not satisfied;
- research-register status and why `CONCLUDED` is or is not justified;
- knowledge-transfer audit outcome and which temporary evidence/handoff files remain or were removed;
- validation executed and any unavailable gate;
- PR number/current lifecycle state if opened;
- confirmation that no production implementation or unreviewed architecture adoption outside this reconciliation was performed.
