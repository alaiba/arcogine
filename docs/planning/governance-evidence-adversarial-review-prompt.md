# Adversarial research-review prompt — Governance evidence identity and applicability

> **Purpose:** Temporary handoff for the required adversarial review of the completed Governance evidence research report.  
> **Authority:** This prompt is not research evidence, accepted architecture, an ADR, implementation admission, or a review disposition. Repository authorities and the completed review artifact control.  
> **Workspace:** `research/governance-evidence-identity-applicability`. Reuse this same research-evidence workspace for the review unless an actual operational constraint requires isolation.  
> **Reviewed report coordinate:** commit `23fe823e68ec39d00e10f66d9851fd25a72308cc`, path `docs/research/investigations/governance-evidence-identity-applicability-report.md`. This exact commit + path identifies the report revision under review; branch tip is not a substitute.  
> **Report research baseline:** `d36d9ec5b0591803da2e1803c6ae01467fc0e483`.  
> **Report final-main recheck:** `bad37c837e63f1fe1a5646ba5744d27a89ade9b9`, incorporated into the workspace without rewriting evidence history.

Perform the repository-required **adversarial research review** of the exact report revision above using Arcogine's **Researcher** role. The question is high risk, so a genuinely independent pass is required before the report can count as decision-quality evidence for ADR or comparable durable architecture promotion.

Do not implement `Evidence` / `EvidenceUse`, do not edit or accept architecture as part of this review, do not promote planning, and do not turn the review into an implementation or PR-review task.

## Independence and input identity

Before substantive report-specific work:

1. Resolve the workspace branch and verify that the complete report is available at exactly:
   - commit `23fe823e68ec39d00e10f66d9851fd25a72308cc`;
   - path `docs/research/investigations/governance-evidence-identity-applicability-report.md`.
2. Verify that the report states research baseline `d36d9ec5b0591803da2e1803c6ae01467fc0e483` and final live-main recheck `bad37c837e63f1fe1a5646ba5744d27a89ade9b9`.
3. If the exact report cannot be resolved, stop with `INPUT BLOCKED — ORIGINAL REPORT NOT AVAILABLE`. Do not reconstruct it from this prompt, chat history, a branch-tip summary, or another revision.
4. State whether the review is genuinely independent:
   - preferred: a different researcher/person/model family or isolated run with no responsibility for defending the original report;
   - if genuine independence is unavailable, label the pass **self-administered / not independent**. It may still expose defects, but it does not satisfy the high-risk promotion requirement.

The reviewer must not claim independence merely because the review is a later commit or because it uses the same workspace. Independence comes from the reviewer/run and the anchoring-control procedure.

## Operating contract

At the start of the review:

1. resolve current live `main` and record its exact SHA as the review baseline;
2. distinguish live `main` from the research workspace — the workspace contains evidence under review, not landed repository truth;
3. read `AGENTS.md`;
4. read `.github/agents/researcher.agent.md`;
5. read `docs/development/researching.md` **in full**, especially the high-risk, adversarial-review, reconciliation, and evidence-custody rules;
6. read `docs/research/research-register.md` and the complete bounded brief at `docs/research/investigations/governance-evidence-identity-applicability.md`;
7. independently inspect the directly relevant current architecture, Accepted/Proposed ADRs, planning, implementation, tests, and semantic neighbors;
8. record any material repository or external-evidence surface that cannot be inspected.

Do not use this prompt, the original execution prompt, Proposed ADR-0016, proposed architecture, planning acceptance criteria, or the reviewed report as accepted semantic authority.

## Anchoring control — mandatory sequence

Before reading the report's recommendation or reasoning in depth:

1. Read the bounded brief and reconstruct the decision at stake.
2. Re-ground independently in current repository authority.
3. Reconstruct the major semantic constraints and ownership boundaries.
4. Identify plausible candidate answers, including a simple/no-new-shared-contract candidate where viable.
5. Derive failure-oriented proving cases from those candidates and constraints.
6. Identify what external evidence, if any, would materially discriminate the candidates or expose a failure mode.

Only after that independent reconstruction should you read the reviewed report's detailed reasoning and recommendation.

Confirming the report's exact identity and completeness before this sequence is allowed; consuming its recommendation deeply before independent reconstruction is not.

## Bounded question

Review the report against the existing research question, not a broader evidence-platform design:

> What minimum durable identity, provenance, applicability, and historical-attribution contract must Governance `Evidence` and `EvidenceUse` expose so that evidence can be reused and interpreted across exact semantic revisions without conflating raw source facts, producer-owned analytical results, or later Governance interpretation?

The decision at stake is whether the evidence semantics are strong enough to support later architecture reconciliation and, only after that reconciliation, possible admission of a first headless implementation.

Do not expand the review into storage/database design, PKI/signatures, retention implementation, telemetry ingestion, document management, a generic verification framework, analytics redesign, or production adapters unless the report improperly makes one of those mechanisms load-bearing.

## Current authority distinctions to verify independently

Verify rather than assume these categories and their current status:

- Accepted identity/history/provenance constraints in the applicable ADRs;
- Proposed ADR-0016 and proposed Governance architecture as hypotheses, not established evidence semantics;
- the current landed Governance conformance/evidence-requirement boundary and its tests;
- Operational ownership of acquisition/correspondence/trust-related concerns where currently defined;
- Engine ownership of result-affecting simulation semantics and producer provenance;
- research-register and planning state as workflow/delivery evidence, not semantic authority.

Search semantic neighbors beyond the files named in the brief. In particular, challenge any argument that depends on equality, historical resolution, cross-version compatibility, applicability, missing evidence, correspondence, provenance, correction, or producer-owned analytical results.

## Required adversarial challenges

Attempt to falsify the report using at least these failure classes from the research operating model:

- an omitted viable candidate or a simpler contract the report did not fairly evaluate;
- a proving case that breaks the recommended model;
- a hidden assumption presented as repository fact;
- domain or authority ownership inversion;
- stale repository baseline or status drift;
- a source whose identity/version/provenance is misidentified;
- an external analogy whose transferable part is overstated;
- possibility or industry precedent treated as necessity;
- an abstraction generalized beyond the consumers actually evidenced;
- a question settled prematurely despite unresolved semantic dependence;
- a conclusion materially stronger than its repository/external evidence supports.

Also test these question-specific adversarial cases independently:

1. one source item reused across different exact semantic revisions without historical context loss;
2. duplicate delivery versus genuinely independent corroborating production;
3. late correction or reinterpretation without rewriting an earlier evaluation;
4. missing, stale, out-of-period, inapplicable, incompatible, or conflicting material without silently producing `PASS`;
5. external observations that lack Arcogine model/revision correspondence at ingestion time;
6. producer-owned analytical results whose model/Engine/run/result provenance is incomplete or changes across versions;
7. source model versus use target where evidence is comparative rather than direct proof of the target;
8. later requirement/assertion definition changes that make IDs/version labels insufficient for historical explanation;
9. retired or unavailable producer execution where attribution survives but replay may not;
10. a context-bound representation that may preserve every required semantic distinction without a separately represented source object.

Do not treat this list as proof of the report. Use it to try to break the report, add stronger cases when appropriate, and drop cases that are not actually discriminating once analyzed.

## External-evidence discipline

Re-verify any external source that materially carries the report's conclusion. Do not trust the report's citation description merely because it looks precise.

For every load-bearing external source:

- verify source identity, issuing authority, version/edition/date, and relevant locator;
- state the narrow proposition it establishes;
- state where the analogy to Arcogine breaks;
- distinguish representability/precedent from semantic necessity.

External evidence may support possibility, tradeoffs, interoperability consequences, or known failure modes. It does not override Arcogine's Accepted repository authority or prove that Arcogine must adopt an external model.

## Baseline re-verification

The original report used research baseline `d36d9ec5b0591803da2e1803c6ae01467fc0e483` and rechecked `main` at `bad37c837e63f1fe1a5646ba5744d27a89ade9b9`.

For this review:

1. resolve live `main` again;
2. inspect changes since the report's relevant baseline/recheck as needed;
3. determine whether any current architecture, ADR status, planning state, product code, tests, research contract, or semantic neighbor materially changes the reviewed conclusion;
4. if `main` moved, do not rewrite the report revision — evaluate whether the exact reviewed revision still survives under current authority.

A stale factual statement may qualify or reopen the report even when its core model remains plausible.

## Review artifact

Persist the completed review as a **new semantically named file** in the same research-evidence workspace. Do not edit the reviewed report revision.

Use:

`docs/research/investigations/governance-evidence-identity-applicability-adversarial-review.md`

The review artifact must include at minimum:

- independence statement;
- exact reviewed-report coordinate: branch, commit, and path;
- reviewed report's stated research baseline and final-main recheck;
- exact live-main SHA used by the review;
- independently reconstructed constraints, candidates, and failure cases before report-specific analysis;
- report claims challenged and evidence used;
- any external source re-verification performed;
- effect of each material challenge on the report's conclusion;
- qualifications that must survive any later reconciliation;
- unresolved questions or missing evidence;
- one final adversarial disposition.

Use exactly one of these dispositions:

- `ACCEPT`
- `ACCEPT WITH QUALIFICATIONS`
- `MORE EVIDENCE REQUIRED`
- `REOPEN`

Do not invent a finding merely to avoid a clean `ACCEPT`, and do not soften a material falsification merely to preserve the original recommendation.

## Disposition meaning

Apply the research operating model rather than treating the labels as sentiment:

- **ACCEPT** — the load-bearing conclusion survives the independent falsification attempt without material qualification beyond what the reviewed report already carries.
- **ACCEPT WITH QUALIFICATIONS** — the load-bearing conclusion survives, but additional qualifications must be preserved explicitly in any reconciliation.
- **MORE EVIDENCE REQUIRED** — the conclusion is not falsified, but a material evidentiary gap prevents it from being decision-quality for promotion.
- **REOPEN** — a load-bearing part of the conclusion fails or the candidate/question needs substantive reconsideration.

If the pass is not genuinely independent, make that limitation explicit regardless of the analytical disposition; a self-administered `ACCEPT` does not satisfy the high-risk independent-review prerequisite.

## Persistence and custody

Commit the review artifact to `research/governance-evidence-identity-applicability` and return:

- workspace branch;
- exact review commit SHA;
- review path;
- reviewed-report commit SHA `23fe823e68ec39d00e10f66d9851fd25a72308cc`;
- live-main baseline SHA used by the review;
- final disposition and independence status.

Do not amend, rebase, force-push, squash away, or otherwise rewrite the handed-off report commit. The new review commit must preserve `23fe823e68ec39d00e10f66d9851fd25a72308cc` in history. Later workspace refreshes must likewise be history-preserving.

The review artifact is evidence, not authority. Even an independent `ACCEPT` or `ACCEPT WITH QUALIFICATIONS` does not itself accept ADR-0016 or change current architecture. Durable promotion remains a separate reconciliation change followed by normal independent PR review.

## Final response checklist

Report back with:

- independence status;
- exact reviewed-report coordinate;
- live-main review baseline;
- strongest attempts to falsify the report;
- which challenges failed or succeeded and why;
- any qualifications/missing evidence that must survive;
- final adversarial disposition;
- workspace branch, exact review commit SHA, and review path;
- explicit confirmation that the report revision was not rewritten and that no implementation or architecture adoption was performed.

## Workspace cleanup note

This handoff prompt is temporary delivery/process material, not research evidence. Keep the workspace until the later reconciliation and knowledge-transfer audit have accounted for the report, review, conclusions, qualifications, reusable proving cases/source maps, remaining questions, and explicit discards. The final durable reconciliation tree should not accidentally retain temporary prompt/report/review artifacts as architecture authority unless a deliberate maintained destination is chosen.