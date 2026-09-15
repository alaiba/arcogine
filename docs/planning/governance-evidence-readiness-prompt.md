# Handoff prompt — resolve Governance evidence implementation readiness

Resolve the Governance evidence-readiness gap before any `Evidence` / `EvidenceUse` implementation is started.

This is an architecture/readiness remediation first, not permission to implement PLAN-GOV-5 immediately. Preserve Arcogine's separation between research, durable architecture, implementation planning, product implementation, and independent review.

At the time this prompt was written, live `main` was `b47cdb822c9df3b8c614351efb67f69bca23c86d`. A fresh session must re-resolve live `main` and use the current repository state rather than assuming this baseline is still current.

## Problem to resolve

The maintained Governance plan currently presents PLAN-GOV-5 as the next admitted Governance slice and describes the Governance sequence as implementation-ready. PLAN-GOV-5 requires, among other things:

- stable evidence identity;
- independently attributable evidence provenance;
- reuse of one evidence item through several explicit `EvidenceUse` records;
- exact subject/revision/requirement/assertion/evaluation binding on `EvidenceUse`;
- explicit time/applicability/source-version semantics when material;
- explicit behavior for missing, stale, or inapplicable evidence; and
- historical attribution after later semantic or evidence changes.

At the same time, ADR-0016 is still `Proposed`. It proposes important provenance boundaries, including the distinction between `EvidenceRequirement` and evidence provenance, the distinction between `Evidence` and `EvidenceUse`, revision-independent ingestion of raw external observations, producer-owned analytical semantics, and explicit cross-version applicability. But it also states that detailed evidence identity, persistence, integrity/signature, retention, and transport are not selected by that proposal.

The current implementation reinforces that boundary: `EvidenceRequirement` explicitly says it does not implement `Evidence` / `EvidenceUse`, persistence, or freshness semantics, and `ConformanceEvaluation` deliberately omits the evidence-set/applicability fields until the external-evidence capability exists.

Therefore, unless newer repository authority has resolved the gap, classify PLAN-GOV-5 as `DEPENDENCY_BLOCKED`, not implementation-ready.

## Required grounding

Before editing anything:

1. Resolve current live `main` and record its full SHA.
2. Read `AGENTS.md`.
3. Read `.github/agents/work-planner.agent.md` because this work changes implementation readiness and produces a handoff from planning into architecture/research.
4. Read:
   - `docs/planning/README.md`;
   - `docs/planning/governance-conformance-capability.md`;
   - `docs/architecture/governance-conformance.md`;
   - `docs/architecture/decisions/README.md`;
   - `docs/architecture/decisions/0016-governance-evidence-provenance.md`;
   - the Accepted identity/history/provenance ADRs that materially constrain Governance evidence, especially ADR-0008, ADR-0013, and ADR-0015 where relevant;
   - `docs/development/researching.md`;
   - `docs/research/research-register.md`;
   - `product/governance/src/main/java/com/arcogine/governance/assertion/EvidenceRequirement.java`;
   - `product/governance/src/main/java/com/arcogine/governance/conformance/ConformanceEvaluation.java`;
   - relevant Governance boundary/acceptance tests that currently exclude or defer `Evidence` / `EvidenceUse`.
5. Search current `docs/` and Governance source/tests for at least:
   - `Evidence`;
   - `EvidenceUse`;
   - `EvidenceRequirement`;
   - `stable identity`;
   - `applicability`;
   - `stale`;
   - `historical`;
   - `provenance`;
   - `ADR-0016`.
6. Inspect open PRs that touch Governance evidence/provenance/readiness and recent merged PRs far enough back to see whether the gap has already been resolved.

Do not continue from remembered chat conclusions if current repository authority differs.

## Objective

Produce the narrowest sequence that makes PLAN-GOV-5 truthfully implementable without letting a coding PR invent hard-to-reverse evidence semantics.

The required outcome is:

1. planning truth is correct immediately;
2. the minimum durable semantic prerequisite for `Evidence` / `EvidenceUse` is explicit;
3. unresolved identity/history/applicability questions are routed through the research/architecture process rather than silently decided in Java;
4. implementation is admitted only after the necessary durable decision is authoritative; and
5. the eventual first implementation slice remains small, headless, and Governance-owned.

Do not optimize for finishing all phases in one PR. Respect role separation and stop when the next phase requires a different repository-owned role or an independent review.

## Phase A — repair planning truth now

If current `main` still has the mismatch described above, first make the maintained Governance plan honest.

Update `docs/planning/governance-conformance-capability.md` so that it no longer states or implies that PLAN-GOV-5 is currently ready for implementation while its load-bearing semantic prerequisite remains Proposed or otherwise unresolved.

At minimum:

- preserve PLAN-GOV-1 through PLAN-GOV-4 as landed/completed according to current evidence;
- classify PLAN-GOV-5 as blocked on an explicit architecture prerequisite;
- replace any blanket `Implementation-ready sequence` wording that overstates current readiness;
- link the blocker to the relevant durable architecture/ADR surface rather than duplicating candidate semantics in planning;
- state concrete promotion criteria for returning PLAN-GOV-5 to `READY_NEXT`;
- keep PLAN-GOV-6 and later work downstream of the unresolved evidence capability where their semantics depend on it;
- do not move candidate evidence identity/equality/lifecycle designs into planning merely to make the blocker look concrete.

A blocked plan is permitted by `docs/planning/README.md` when the implementation contract is settled and the blocker is a concrete prerequisite. If research still has to decide what the implementation means, reduce the planning content to an implementation-admission guard and put the unresolved question in research.

This phase must not add Governance production code.

## Phase B — determine whether a research question is still required

Do not simply change ADR-0016 from `Proposed` to `Accepted` because implementation wants to proceed.

First determine whether current Accepted architecture already settles every hard-to-reverse semantic fact the first `Evidence` / `EvidenceUse` implementation actually needs. Distinguish semantic requirements from mechanism choices.

The minimum semantic questions to account for are:

1. **Evidence referent and identity** — what does one stable evidence identity identify: an immutable Governance evidence record, an external source artifact, a semantic fact, or something else?
2. **Equality and reuse** — what makes two references the same evidence item, and what permits one evidence item to be reused across several uses without rewriting it?
3. **Immutability/history** — whether correction or later interpretation mutates an evidence record or creates a new attributable record/use, and what must remain historically reconstructible.
4. **Evidence versus EvidenceUse ownership** — which provenance is intrinsic to the source fact/result and which model/revision/requirement/assertion/evaluation bindings belong only to its use.
5. **Applicability semantics** — where applicability is recorded and how missing, stale, incompatible, or inapplicable evidence affects conformance without becoming an implicit pass.
6. **Producer provenance** — how Engine-derived analytical results retain `ModelFingerprint`, `EngineSemanticsVersion`, and producer-owned run/result provenance without Governance redefining Engine semantics.
7. **External observation independence** — how future Operational observations remain source/subject/time/trust facts independent of Arcogine model/revision binding at ingestion.
8. **Cross-version use** — how compatibility/applicability is made explicit when evidence or analytical results originate under materially different semantics.

Do **not** require mechanism decisions merely because they are adjacent. Unless the first implementation genuinely needs them, keep storage technology/schema, signatures, retention, transport, telemetry ingestion, connector authentication, and evidence-lake/document-management concerns deferred.

If any load-bearing identity, equality, lifecycle/history, or applicability question above is materially unsettled, do not answer it inside an implementation PR. Follow the Researcher contract in `.github/agents/researcher.agent.md` and the normative method in `docs/development/researching.md`.

A suitable bounded research question, if one is needed, is approximately:

> What minimum durable identity, provenance, applicability, and historical-attribution contract must Governance `Evidence` and `EvidenceUse` expose so that evidence can be reused and interpreted across exact semantic revisions without conflating raw source facts, producer-owned analytical results, or later Governance interpretation?

Treat that wording as a starting point, not authority. Bound the brief from current evidence and derive candidates/proving cases independently.

Because identity/history semantics are hard to reverse, apply the repository's risk-proportionate research rule. If the question is classified high risk under `docs/development/researching.md`, require independent adversarial research review before its conclusion is promoted into an ADR or equivalent durable architecture.

Do not make the open simulation-analytics consumer-boundary research a blanket blocker unless the evidence contract truly depends on that unresolved ownership decision. Generic Governance may require attributable producer provenance without deciding which analytical result belongs to Engine versus a future analytics capability.

## Phase C — reconcile durable architecture only after decision-quality evidence

Once the minimum semantic contract is actually decision-quality, reconcile it into the narrowest durable authority.

ADR-0016 is still Proposed at the prompt baseline, so it may be revised before acceptance. Preserve its useful ownership/provenance separation unless current evidence falsifies it:

- assertion evidence requirement and evidence provenance are different dimensions;
- `Evidence` and `EvidenceUse` are different concepts;
- raw external observations are not rebound to model/revision identity at ingestion;
- producer-specific analytical semantics remain producer-owned;
- historical validity and forward-looking applicability are not the same question;
- cross-version comparison is explicit;
- conformance evaluation remains narrow rather than absorbing producer-specific evidence taxonomy.

Do not overload ADR-0016 with storage/persistence mechanisms merely to make it look complete.

If evidence identity/equality/history constitutes a separate durable decision, prefer a focused companion ADR or focused architecture decision rather than burying it as incidental Java design. If the current repository's accepted authorities already settle it, document the derivation precisely instead of creating a redundant ADR.

Only mark an ADR `Accepted` when the decision has actually been made and reviewed according to repository policy. Do not use status promotion as a scheduling shortcut.

## Phase D — return planning to implementation-ready only after prerequisites land

After the required architecture is Accepted/authoritative on live `main`, reconcile the Governance plan again.

PLAN-GOV-5 may become `READY_NEXT` only when a fresh implementer can determine the required domain behavior from landed authority without inventing identity/equality/history/applicability semantics.

The first admitted implementation slice should remain deliberately narrow. It should prove a generic, headless Governance contract such as:

```text
Evidence
    stable opaque identity
    immutable independently attributable source/result provenance
    intrinsic observation/applicability facts only where they truly belong to the source fact/result

EvidenceUse
    reference to Evidence
    exact semantic subject/revision when applicable
    requirement/assertion/evaluation context
    explicit applicability/interpretation decision
```

Do not treat the sketch above as an instruction to freeze exact Java names or fields. Derive the production shape from the accepted semantic contract.

Acceptance evidence for the first slice should cover, at minimum:

- one evidence item can be referenced by multiple uses without mutating the evidence;
- uses against different exact revisions/contexts remain independently attributable;
- missing/stale/inapplicable evidence cannot silently become `PASS`;
- historical evaluation attribution survives later evidence/semantic changes;
- an external observation can be fixture-backed without Governance inventing Operational telemetry types;
- an Arcogine-derived analytical result can preserve producer semantics/version provenance without Governance owning its calculation;
- current module-boundary tests are deliberately updated from `Evidence` / `EvidenceUse` being forbidden/deferred to the exact newly admitted contract.

## Non-goals

Do not pull these into the readiness fix or the first implementation slice unless current authoritative evidence makes one strictly necessary:

- telemetry ingestion;
- PLC/SCADA/MQTT/OPC UA adapters;
- connector trust/authentication;
- a durable evidence database/repository abstraction;
- generic persistence infrastructure;
- signatures/PKI;
- retention/archival policy;
- document management;
- vector search;
- generic evidence lake;
- simulation analytics/KPI redesign;
- Operational correspondence/reconciliation implementation;
- generic cross-domain verification framework;
- framework/control libraries;
- broad audit projection work;
- governed-change/authorization implementation from PLAN-GOV-6;
- changes to `EvidenceRequirement` merely to encode provenance origin.

Do not create a third `EvidenceRequirement` member solely for Arcogine-derived analysis unless new decision-quality evidence explicitly overturns the proposed separation in ADR-0016.

## Documentation discipline

Planning coordinates such as PLAN-GOV-5 may remain in `docs/planning/`, issues, PRs, and handoff material. Do not carry them into durable architecture, ADR titles/filenames, code comments, test names, or semantic type names.

When durable documentation changes, describe the semantic capability directly: evidence identity, provenance, applicability, historical attribution, producer interpretation provenance, and so on.

Current-state architecture must not claim the evidence capability is implemented until code/tests actually land on live `main`.

## Validation

Use the narrowest validation appropriate to each phase and current `AGENTS.md`.

For documentation/planning/ADR-only reconciliation, include at least the repository's applicable documentation/policy checks, normally including:

```text
bash .github/scripts/classify-changes.test.sh
python3 .github/scripts/check-markdown-links.py .
python3 .github/scripts/check-delivery-labels.py
python3 .github/scripts/check-adr-immutability.py --base-ref origin/main
git diff --check
```

Adjust commands if current `AGENTS.md` or repository tooling has changed.

For a later Java implementation PR, run the narrow Governance module tests plus the required repository Java gate from current `AGENTS.md`; include focused acceptance/boundary tests for the new semantics rather than relying only on compilation.

## PR and review discipline

Keep role boundaries explicit:

- planning/readiness correction is not evidence research;
- evidence research is not architecture acceptance;
- architecture acceptance is not product implementation;
- implementation self-validation is not independent PR review.

Use separate PRs when combining phases would make one role certify its own load-bearing conclusion. Every implementation or reconciliation PR still goes through the repository's independent PR Reviewer contract. Do not merge PRs; stop when the repository lifecycle reaches owner-merge responsibility.

If live `main` moves while working, re-ground before making readiness claims. If an open PR changes one of the prerequisites, treat it as in-flight until merged; do not describe PR-head semantics as current repository truth.

## Final report

At the end of each phase, report:

- live `main` SHA used as the authoritative baseline;
- phase completed and files changed;
- whether PLAN-GOV-5 is `DEPENDENCY_BLOCKED` or `READY_NEXT`, with the exact landed prerequisite supporting that classification;
- any research question admitted and its lifecycle/risk classification;
- ADR/architecture status changes and the evidence that justified them;
- validation commands and results;
- open PR/review/CI blockers;
- the single next action, without silently executing a different repository-owned role.
