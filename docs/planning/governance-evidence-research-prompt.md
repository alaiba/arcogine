# Research execution prompt — Governance Evidence / EvidenceUse boundary

> **Purpose:** Temporary handoff for executing the bounded Governance evidence research question.  
> **Authority:** This prompt is not research evidence, accepted architecture, an ADR, or implementation admission. The repository authorities named below control.  
> **Workspace:** `research/governance-evidence-identity-applicability`, created from live `main` at `d36d9ec5b0591803da2e1803c6ae01467fc0e483`. Treat the branch as temporary research-evidence custody, not as landed repository truth.

Execute the existing **Governance evidence identity and applicability** brief as a standard investigation using Arcogine's repository-owned **Researcher** role. Produce decision-quality evidence for the bounded semantic question; do not implement `Evidence` / `EvidenceUse`, do not accept or rewrite architecture as part of the investigation, and do not move the blocked implementation into delivery.

## Operating contract

At the start of the run:

1. resolve live `main` again and record the exact SHA as the research baseline;
2. distinguish live `main` from this workspace branch — the workspace carries evidence, but does not redefine current Arcogine state;
3. read `AGENTS.md`;
4. read `.github/agents/researcher.agent.md`;
5. read `docs/development/researching.md` **in full** and follow it as the normative research method;
6. read the current research-register entry and the complete bounded brief;
7. inspect the directly relevant current architecture, ADRs, planning, implementation, tests, and semantic neighbors before forming a conclusion;
8. record any material repository or external-evidence surface that cannot be inspected.

If live `main` has moved since this workspace was created, use the new live head as the research baseline. Do not treat the branch's creation SHA as a substitute. Preserve the research-workspace custody rules in `docs/development/researching.md`; once a completed evidence revision has been handed off by exact commit SHA + path, never rebase or force-push it away.

Do **not** use `docs/research/synthesis-seeds.md` as routine start-of-run grounding. Reach the investigation's result independently from the bounded brief and relevant evidence first. Only compare against synthesis seeds afterward; if a seed materially shaped the reasoning, classify any similarity as reuse rather than independent recurrence.

## Read first

Read these current `main` surfaces before substantive analysis:

- `AGENTS.md`
- `.github/agents/researcher.agent.md`
- `docs/development/researching.md`
- `docs/research/research-register.md`
- `docs/research/investigations/governance-evidence-identity-applicability.md`
- `docs/research/report-template.md`
- `docs/architecture/overview.md`
- `docs/architecture/governance-conformance.md`
- `docs/architecture/operational-execution-digital-twin.md`
- `docs/architecture/decisions/0004-model-identity-revision-lineage-and-external-change-control.md`
- `docs/architecture/decisions/0006-durable-semantic-fingerprint-contract.md`
- `docs/architecture/decisions/0008-controlled-revision-identity-and-lineage.md`
- `docs/architecture/decisions/0013-durable-operational-identity.md`
- `docs/architecture/decisions/0015-engine-semantics-identity-and-reproducibility.md`
- `docs/architecture/decisions/0016-governance-evidence-provenance.md`
- `docs/planning/governance-conformance-capability.md`

Treat ADR-0016 as **Proposed**, not established architecture. Its candidate semantics are material to test, not premises that the report may silently assume. Likewise, the proposed Governance architecture's evidence section is a design hypothesis where it goes beyond already accepted/implemented contracts.

Inspect the current implementation and tests that establish the landed boundary, including at minimum:

- `product/governance/src/main/java/com/arcogine/governance/assertion/EvidenceRequirement.java`
- `product/governance/src/main/java/com/arcogine/governance/conformance/ConformanceEvaluation.java`
- `product/governance/src/main/java/com/arcogine/governance/conformance/ConformanceEvaluator.java`
- `product/governance/src/main/java/com/arcogine/governance/conformance/Finding.java`
- the current requirement/assertion, controlled-revision, provenance, and conformance value contracts they compose with;
- `product/governance/src/test/java/com/arcogine/governance/assertion/EvidenceRequirementDeclarationTest.java`
- `product/governance/src/test/java/com/arcogine/governance/GovernanceModuleBoundaryTest.java`
- the directly relevant conformance, controlled-revision, and provenance tests.

Expand beyond this list when repository search reveals semantic neighbors.

## Quick search terms

Search maintained docs, source, and tests for combinations of:

`Evidence`, `EvidenceUse`, `EvidenceRequirement`, `provenance`, `applicability`, `historical attribution`, `UNKNOWN`, `NOT_APPLICABLE`, `ModelFingerprint`, `ControlledRevision`, `ControlledRevisionId`, `EngineSemanticsVersion`, `external observation`, `operational observation`, `runtime observation`, `derived result`, `analytical result`, `verification result`, `correspondence`, `revision-independent`, `effective period`, `source authority`, `evaluation provenance`, `cross-version`, `compatibility`.

For material absence claims, record the actual search scope rather than turning failure to find a reference into proof that none exists.

## Bounded question

Answer exactly this question:

> What minimum durable identity, provenance, applicability, and historical-attribution contract must Governance `Evidence` and `EvidenceUse` expose so that evidence can be reused and interpreted across exact semantic revisions without conflating raw source facts, producer-owned analytical results, or later Governance interpretation?

The decision at stake is whether the future Governance evidence capability has sufficiently settled semantics to admit its first headless implementation, and which durable authority would need reconciliation before implementation can begin.

Do not expand this into a general GRC platform design, telemetry architecture, persistence project, analytics redesign, or generic verification framework.

## Current landed boundary to preserve while investigating

Keep these repository distinctions explicit and verify them against current authority rather than merely repeating this prompt:

- semantic content identity (`ModelFingerprint`) is distinct from controlled historical revision identity;
- recording provenance is not approval, actor attribution, authorization, deployment, or source-observation identity;
- Governance owns requirements, assertions, conformance interpretation, findings, evidence use, and later governance decisions, but it does not become telemetry ingestion or a generic producer-specific verification engine;
- Operational owns acquisition/trust/authenticity provenance and raw external observations; Governance must not fabricate model/revision binding at ingestion merely to consume those observations later;
- Engine owns its result-affecting simulation semantics; Governance may consume attributable analytical results without redefining how those results were calculated;
- `EvidenceRequirement` currently states whether model state alone is sufficient for an assertion; it is not an implemented evidence identity/provenance model;
- current `ConformanceEvaluation` deliberately omits the future evidence/evidence-use capability;
- missing evidence or genuinely unobserved facts must not silently become success;
- historical meaning must not be rewritten by later requirement, assertion, evidence, model, or interpretation changes.

The investigation may falsify or qualify a proposed formulation above where the binding repository authorities permit it. Do not protect ADR-0016 or the proposed evidence architecture from falsification.

## Candidate models

Compare at least the candidate set already defined by the brief:

1. **Source-record identity with contextual uses** — one immutable attributable fact/result, referenced by separate evaluation-specific uses.
2. **Context-bound evidence identity** — evidence identity is primarily the evaluation/revision context, with reuse represented through repeated/linked contextual records.
3. **Artifact/fact identity with interpretation layer** — identity points to an immutable external artifact or semantic fact, while Governance interpretations/usages provide contextual binding.
4. **No new shared evidence contract yet** — preserve producer/Operational facts and existing Governance records separately until a concrete consumer proves a narrower shared boundary; keep implementation blocked.

These are starting hypotheses, not a forced four-way answer. Merge candidates only if analysis proves they are semantically equivalent under the discriminating cases; add an omitted viable candidate if evidence requires it. Prefer the simplest surviving contract.

## Required proving cases

Derive the final proving-case set from the candidates, but include the brief's minimum discriminators:

1. One independently attributed evidence item is reused by different evaluations for different exact semantic revisions without mutation of its source meaning or prior use context.
2. A later correction or reinterpretation creates an attributable historical distinction instead of silently rewriting earlier evidence or an earlier evaluation.
3. Missing, stale, out-of-period, inapplicable, or cross-version-incompatible evidence cannot silently produce `PASS`; the resulting use remains explainable.
4. An external observation can retain source/subject/time/trust provenance without Governance inventing Operational telemetry types or revision-binding at ingestion.
5. An Arcogine-derived analytical result retains producer-owned model, engine-semantics, run/result, and interpretation provenance without Governance taking ownership of the calculation.
6. A historical evaluation remains attributable after the requirement/assertion version, evidence set, or semantic revision changes later.

For every proving case, show which candidate survives, fails, or remains underdetermined, and identify the evidence needed to justify that judgment.

Add failure-oriented cases when needed, especially around identity/equality, correction, reuse, stale applicability, source authenticity versus interpretation, cross-version compatibility, history/replay, and domain ownership.

## External evidence discipline

This is a **high-risk** identity/provenance/history question. Seek external evidence where it materially discriminates candidates, reveals failure modes, establishes interoperability consequences, or tests whether a proposed abstraction is actually necessary.

Do not collect sources to satisfy a quota. Prefer primary/normative sources where applicable and triangulate across genuinely different evidence traditions when useful. For standards/specifications, record issuing authority, designation, part, edition/version, year/date, and useful locator. For evolving products/platforms, record version/date context.

For each external source that materially carries the conclusion, state what it proves for this Arcogine question **and where the analogy stops**. Industry precedent establishes possibility and tradeoffs, not necessity. Mark anything not actually verified in the run as unverified background and do not let it support a load-bearing conclusion.

## Evidence classification

Keep these categories visibly distinct in the report:

- **Repository fact**
- **External evidence**
- **Inference**
- **Recommendation / proposed decision**

In particular, never present Proposed ADR-0016, proposed architecture, this prompt, or a planning acceptance condition as though it were already an accepted semantic fact.

## Non-goals and scope guards

Unless the research proves one is semantically indispensable, do **not** select or design:

- database/storage schema;
- generic persistence infrastructure;
- signatures/PKI;
- retention/archive implementation;
- transport or connector authentication;
- telemetry ingestion;
- document-management/vector-search/evidence-lake systems;
- a generic cross-domain verification framework;
- a simulation analytics redesign;
- production adapters;
- new framework-specific compliance taxonomies.

Do not add production Java types, fields, persistence, module-boundary exceptions, or tests for a future `Evidence` / `EvidenceUse` implementation. The implementation plan remains dependency-blocked until research, independent adversarial review, and durable reconciliation have all landed.

## ADR / architecture rule

Research may recommend that a semantic contract be reconciled into ADR-0016, a superseding/new ADR, Governance architecture, or another durable authority. Research must **not perform that promotion itself**.

If the surviving conclusion would commit Arcogine to a hard-to-reverse identity/equality/history/applicability/provenance/public-compatibility contract, explicitly identify the durable decision that would require ADR/architecture reconciliation. Keep mechanism choices out unless they are necessary to state the semantic invariant.

Do not edit Accepted ADR history to fit the result. Do not change ADR-0016 from Proposed to Accepted during the investigation.

## Report deliverable

Create a completed research report as a **new semantically named file** under `docs/research/investigations/`; do not overwrite the existing brief. A suitable path is:

`docs/research/investigations/governance-evidence-identity-applicability-report.md`

Use `docs/research/report-template.md` as a structure, omitting irrelevant sections rather than padding them. At minimum the report must contain:

- exact research baseline SHA;
- the bounded question and decision at stake;
- high-risk classification and resulting depth;
- current repository facts and authority/status distinctions;
- candidate models, including any omitted candidate discovered during research;
- a candidate × proving-case evaluation;
- external evidence actually used, with verification/provenance status;
- the surviving semantic contract, or an explicit conclusion that no shared contract is justified yet;
- confidence and what would change it;
- unresolved unknowns and reopening triggers;
- mechanism choices deliberately deferred;
- recommended durable destination without self-promotion;
- explicit statement that genuinely independent adversarial review is still required before architecture promotion unless such a review has actually occurred in a separate qualifying run.

The report must not mark the research question `CONCLUDED` merely because the report exists. Under the research operating model, `CONCLUDED` requires the durable consequence to be reconciled or an explicit durable no-action result.

## Persistence / handoff

Persist the completed report in this same research-evidence workspace. Commit it as a new revision and return:

- workspace branch;
- exact completed-report commit SHA;
- report path;
- research-baseline SHA.

That exact commit SHA + path is the evidence identity for the report. Branch tip is not a substitute.

After the report is handed off, do not amend, rebase, force-push, or otherwise rewrite that report revision. Any change creates a new report revision requiring its own review binding.

Because this question is high risk, the next phase is a **genuinely independent adversarial research review**. Prefer a different researcher/person/model family; otherwise use a fresh isolated run and state the independence limitation truthfully. The reviewer must reconstruct constraints/candidates/failure cases from the brief and live authorities before deeply consuming the report recommendation.

The same workspace should normally carry that later review and, after accepted evidence exists, the separate durable reconciliation. Do not create one branch per research artifact or phase without an operational reason.

## Final current-state check

Before presenting the investigation as complete:

1. re-resolve live `main`;
2. determine whether changes since the recorded research baseline materially affect the conclusion;
3. verify the status of every ADR used as authority (`Accepted`, `Proposed`, `Superseded`, etc.);
4. verify that material repository claims still match current implementation/tests;
5. verify all load-bearing external-source provenance actually cited;
6. state any unavailable surface or unresolved evidence gap;
7. confirm no production/architecture/planning semantics were silently promoted by the research branch.

## Final response checklist

Report back with:

- research baseline and final live-main recheck SHA;
- report path and exact report commit SHA;
- workspace branch;
- candidate models considered and headline proving-case result;
- conclusion + confidence;
- unresolved unknowns/reopening triggers;
- external sources actually verified versus unverified background;
- recommended durable destination;
- independent adversarial-review status;
- explicit confirmation that no production implementation or architecture adoption was performed.

## Workspace cleanup note

This handoff prompt is temporary delivery/process material, not decision-quality evidence. Before a final durable reconciliation PR is ready to merge, follow `docs/development/researching.md`'s knowledge-transfer audit and ensure temporary report/review/handoff files that are not deliberately promoted into maintained repository authority are absent from the final reconciliation tree. Preserve their exact historical coordinates in the durable delivery-history reference required by the research operating model before workspace retirement.