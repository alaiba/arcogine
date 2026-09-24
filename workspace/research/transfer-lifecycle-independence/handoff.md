# Research handoff — transfer-lifecycle independence

You are operating as Arcogine's **Researcher** for one bounded, high-risk investigation.

## Repository and workspace

Repository: `alaiba/arcogine`

This handoff was created from live `main` at:

`397d1e989cb0e9d9a3167887294b410c1836cee3`

Workspace branch:

`research/transfer-lifecycle-independence`

This branch is temporary research-evidence custody, not repository authority. Do **not** treat its branch tip as landed truth. At the start of the investigation, resolve live `main` again and record its exact SHA as the research baseline. If `main` has advanced, ground the investigation against the new live `main`; preserve this handoff commit and use only history-preserving workspace synchronization if needed. Do not rebase or force-push away handed-off evidence history.

Read and follow, before substantive investigation:

1. `AGENTS.md`
2. `.github/agents/researcher.agent.md`
3. `docs/development/researching.md` **in full**
4. `docs/research/report-template.md`

Research is diagnostic and evidentiary. Do not modify production code, current architecture/specifications, or implementation planning as part of the investigation. Do not mark the question `CONCLUDED` merely because a report exists.

Persist the completed report in this same workspace under:

`workspace/research/investigations/transfer-lifecycle-independence-report.md`

When the report is complete, commit it and hand off:

- workspace branch;
- exact report commit SHA;
- report path; and
- research-baseline SHA.

Do not perform the required independent adversarial review in the same research run. Hand the completed report to a fresh independent Researcher session for that review, preferably using this same workspace branch.

## Bounded research question

Execute the existing `READY` question in:

`docs/research/investigations/transfer-semantics.md#ready--transfer-lifecycle-independence`

Question:

> For work that completes one operation on a configured productive resource and next executes on a **different** configured productive resource, is transfer/hand-off an Engine execution phase whose existence is independent of whether the published Factory model includes spatial layout, or should the transfer lifecycle exist only when the model explicitly represents transfer-affecting facts?

The decision at stake is whether Arcogine may continue to equate **“no spatial record” with “no transfer lifecycle”**, or whether transfer binding/state/events must have a truthful non-spatial interpretation before Factory V2 publication and spatial runtime activation proceed.

This investigation is **high risk** because it can reopen the still-unreleased Factory V2 boundary and affects a complete result-affecting Engine interpretation. Independent adversarial review is therefore required before any conclusion may become decision-quality evidence for durable architecture/specification reconciliation.

## Current repository state to verify, not assume

At handoff creation, current planning and research state says:

- `factory-model:v2` is still unreleased.
- The V2 optional spatial-record shape and validation proving slice is landed.
- V2 canonical identity/publication remains blocked.
- Spatial/transfer runtime activation remains blocked.
- `engine-semantics:v1` is fixed as the current Engine interpretation identity, with pre-spatial conformance evidence landed.
- The previous Engine-applicability investigation is `SUPERSEDED` because it inherited the premise that spatial absence means no transfer lifecycle.
- The successor Engine-applicability question remains `CANDIDATE` until this transfer-lifecycle question is reconciled.
- The separate non-spatial transfer-timing question remains `CANDIDATE`.

Verify all of those claims against the live research register, current architecture/specifications, planning, source and tests before relying on them.

## Start-of-run repository grounding

After resolving live `main`, read at minimum:

### Research state and method

- `docs/research/research-register.md`
- `docs/research/investigations/transfer-semantics.md`
- `docs/research/investigations/engine-applicability-optional-record-factory-policies.md`
- `docs/research/investigations/engine-applicability-after-transfer-boundary.md`
- `docs/research/investigations/factory-model-semantic-composition.md`
- `docs/research/investigations/engine-evolution.md`

Treat the superseded applicability report as retained evidence, **not** as a recommendation that constrains this investigation.

### Current durable authorities

- `docs/architecture/overview.md` — especially the Determinism Contract and semantic-evolution/support rules
- `docs/architecture/factory-design.md` — especially the Factory semantic-evolution and authored-model ownership boundaries
- `docs/architecture/factory-model-v2.md`
- `docs/architecture/engine-semantics-v1.md`
- `docs/architecture/runtime-contract.md`
- `docs/history/decisions/2026-09-23-factory-model-semantic-composition.md` as non-normative retained rationale, where relevant

### Planning consequences

- `docs/planning/factory-design-capability.md`
- `docs/planning/factory-simulation-engine-readiness.md`
- `docs/planning/spatial-runtime-consequences.md`

### Implementation and executable evidence

Inspect the current implementation and tests around the actual seams rather than inferring implementation from architecture prose. At minimum search/read the current equivalents of:

- `product/domains/factory/src/main/java/com/arcogine/factory/model/v2/FactoryModelV2.java`
- `product/domains/factory/src/main/java/com/arcogine/factory/model/v2/SpatialRecord.java`
- `product/domains/factory/src/main/java/com/arcogine/factory/model/v2/FactoryModelV2Validator.java`
- corresponding V2 model/validator tests;
- `product/types/src/main/java/com/arcogine/types/EngineSemanticsVersion.java`;
- `product/domains/factory/src/main/java/com/arcogine/factory/process/FactoryHandler.java`;
- `product/domains/factory/src/main/java/com/arcogine/factory/process/FactoryRuntime.java`;
- current runtime observation/event/provenance types and focused Engine conformance/identity/session tests named by the superseded applicability investigation.

Verify whether transfer runtime state/events are implemented, partially implemented, or still specification/planning-only. Do not infer implementation merely because `TRANSFER_STARTED`, `TRANSFERRING`, or `TRANSFER_COMPLETED` appears in a normative document.

## Required quick searches

Perform repository searches under `docs/` and then source/tests for at least:

- `transfer lifecycle`
- `spatial absent`
- `no transfer`
- `zero duration`
- `TRANSFER_STARTED`
- `TRANSFERRING`
- `TRANSFER_COMPLETED`
- `destination binding`
- `admission reservation`
- `handlingTicks`
- `ticksPerCell`
- `EngineSemanticsVersion`
- `factory-model:v2`
- `transfer duration`

Search semantic neighbors, not just files named by the brief. Record any material surface that cannot be inspected.

## Scope discipline

### In scope

Analyze:

- distinct-resource continuation after an operation completes;
- same-resource continuation as the control case;
- destination selection/binding timing;
- whether transfer start/in-flight/completion state exists when no spatial facts exist;
- the semantic difference between **no transfer**, **zero-duration transfer**, and **execution refusal because required facts are absent**;
- destination admission/reservation and arrival behavior only insofar as the existence of a transfer phase changes those semantics;
- whether the discriminator for transfer applicability is authored Factory fact, Engine interpretation rule, or a combination;
- result/provenance consequences for otherwise identical production records;
- semantic-identity and compatibility consequences under Arcogine's semantic-evolution rules;
- what exact facts the later Engine-applicability investigation will need in order to be re-promoted correctly.

### Explicitly out of scope

Do **not** decide:

- Manhattan versus Euclidean/path distance;
- pathfinding, aisles, conveyors, explicit transport resources, buffers, congestion, or routing graphs;
- the concrete representation for positive non-spatial transfer duration;
- a general transfer profile or transport ontology;
- dispatch/ranking/queue-sequencing changes;
- game animation or presentation;
- the successor Engine-applicability question itself.

In particular, do not allow “what is the duration?” to silently decide “does a transfer phase exist?”. Lifecycle existence and duration representation are separate decisions.

## Candidate models

At minimum evaluate these three candidates from the maintained brief. Refine them only if evidence requires it, and add a candidate if a materially distinct viable rule is missing.

### Candidate 1 — current coupling

Transfer lifecycle exists only when the published model represents the current spatial/handling record. Spatial absence means distinct-resource continuation proceeds with no transfer state/events.

### Candidate 2 — lifecycle independent, zero-duration fallback

Every distinct-resource continuation has transfer binding/state/event lifecycle. Without authored timing facts, the transfer duration is zero; later timing contracts may make the interval positive. Same-resource continuation has no transfer.

### Candidate 3 — lifecycle independent, explicit applicability required

Spatial layout is not the transfer discriminator, but a separate authored transfer/timing contract determines whether distinct-resource continuation has transfer behavior. Absence of both spatial and non-spatial transfer facts has explicitly defined refusal or no-transfer semantics rather than inheriting meaning from geometry absence.

Do not let a candidate survive by leaving absence semantics implicit.

## Mandatory proving cases

Produce an explicit candidate-by-case evaluation matrix. For every surviving candidate, state the semantic result for all of these:

1. `M1 -> M1` continuation.
2. `M1 -> M2` with no spatial record and no separately authored transfer facts.
3. `M1 -> M2` with explicitly authored zero timing.
4. `M1 -> M2` with positive timing facts.
5. Multi-eligible next step where destination becomes bound before any positive or zero transfer interval.
6. Destination availability/capacity changes between binding and arrival when a non-zero interval exists.
7. Deterministic same-time ordering when transfer duration is zero.
8. Historical V1 / production-only artifacts that never authored spatial facts.

For each case state, separately:

- whether a transfer phase exists;
- what fact/rule determines that;
- which owner defines the fact/rule;
- whether destination binding occurs, and at what semantic point;
- whether admission/reservation exists and why;
- what happens when relevant authored facts are absent;
- what runtime state/events are required or forbidden;
- what provenance / Engine-semantics consequence follows.

The report must explicitly distinguish:

- **no transfer exists**;
- **transfer exists with zero duration**; and
- **artifact is not executable because required transfer facts are absent**.

## Evidence requirements

### Repository inventory

Build a concrete inventory of every current Factory/Engine contract that depends on or assumes the present coupling, including:

- V2 optional spatial-record semantics;
- transfer-duration formula and publication representability checks;
- Engine v1 transfer/binding/reservation semantics;
- supported runtime state/event/observation expectations;
- planning gates and blocked slices;
- V1/V2 identity and historical compatibility consequences;
- any source/test evidence that currently encodes or deliberately does **not** encode transfer behavior.

Do not conflate current durable authority, planning intent, research evidence, and implementation status.

### Ownership analysis

Apply Arcogine's existing ownership split rigorously:

- Factory owns authored production-system facts and their model identity.
- Engine owns result-affecting interpretation and runtime execution consequences.

For every candidate, identify exactly which side owns the discriminator for transfer existence. Reject any answer that invents authored Factory facts merely so the Engine has something to execute, or that hides a result-affecting Engine rule as ambient behavior.

### Determinism and identity

For otherwise identical explicit production records, analyze what changes when the spatial record is absent, present with authored zero timing, or present with positive timing.

Determine:

- whether two conforming implementations could disagree about transfer state/event existence;
- whether the rule is part of the identified Engine interpretation;
- whether selecting a candidate would require correcting the still-unreleased Factory V2 grammar;
- whether it necessarily implies a new Engine semantics identity, merely re-bounds the successor applicability question, or leaves that identity question genuinely open.

Do **not** prematurely answer the successor applicability investigation. This report should provide the exact represented-content meanings and applicability facts that successor research needs.

### External evidence

Use external manufacturing/simulation evidence only if it materially discriminates between candidates, falsifies a rule, exposes a hidden lifecycle/ownership assumption, or establishes a compatibility consequence.

Prefer primary or authoritative sources. Verify provenance/version. Keep external evidence visibly separate from Arcogine repository facts and from inference.

Do not produce a generic survey of material handling or transport modelling.

## Falsification rules

A candidate fails if it:

- cannot truthfully distinguish absence from authored zero;
- makes a production-only model acquire invented authored facts;
- causes Engine interpretation to depend on consumer presentation;
- leaves two conforming implementations free to disagree about whether a distinct-resource transfer state/event exists;
- contradicts current durable ownership/determinism rules without explicitly identifying the required architecture change;
- cannot give deterministic semantics for destination binding/reservation and zero-duration same-time ordering;
- makes historical V1/production-only artifacts silently change meaning without an explicit compatibility/semantics consequence.

Add additional falsification criteria if evidence exposes them.

## Successor-research handoff requirement

The report must end with a precise handoff for the separate successor Engine-applicability question. Without performing that investigation, state the exact facts it will be allowed to take as settled **if** this report later survives independent adversarial review and durable reconciliation.

At minimum clarify:

- whether distinct-resource transfer exists without spatial facts;
- the meaning of no-transfer versus zero-duration versus refusal;
- which semantic owner controls transfer applicability;
- any required correction to the unreleased V2 represented-content grammar;
- the exact spatial-present/spatial-absent represented-content cases that successor Engine applicability must evaluate.

## Conclusion and durable consequence

Conclude only when every mandatory proving case has an explicit result and the ownership/identity consequences are stated.

Report:

- surviving candidate or bounded alternative;
- rejected candidates and the evidence/failure case that rejects each;
- confidence;
- material limitations and unresolved unknowns;
- what evidence would change the conclusion;
- recommended durable destination.

The likely durable destination is Factory/Engine architecture/specification reconciliation, but the report must derive that from its actual conclusion rather than assuming a preferred outcome.

Do **not** perform that reconciliation in the research report. Because this is high-risk research, state prominently that:

**independent adversarial review is required before architecture/specification reconciliation.**

## Stopping rule

Do not broaden the study once the bounded decision can be made. Stop when the evidence is sufficient to discriminate the candidates, satisfy the proving cases, explain the ownership/determinism/identity consequences, and state the successor-research handoff.

The report should be decision-quality, not exhaustive.
