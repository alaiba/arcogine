# Research-evidence reconciliation handoff — superseded Engine applicability investigation

You are executing a **research-state and research-evidence reconciliation** in the canonical repository `alaiba/arcogine`.

This is a fresh-session delivery task. It is **not** a new research investigation, **not** an architecture reconciliation adopting a new Engine semantics identity, and **not** implementation work.

Your objective is to preserve the useful knowledge produced by the completed Engine-applicability investigation in durable `docs/research/` surfaces, truthfully mark why that investigation no longer answers the current question, establish a clean successor applicability brief, complete the knowledge-transfer audit, and make the old research workspace retirement-safe.

Do **not** resurrect or adopt the failed Engine-v2 reconciliation from closed PR #395.

## Repository and branch rules

Follow current `AGENTS.md`, `.github/agents/work-planner.agent.md`, and `docs/development/researching.md`.

This prompt is transient delivery scaffolding. Before independent PR review and merge readiness, remove this prompt and ensure no tracked `workspace/` path remains in the final candidate tree.

At handoff creation time, live `main` was:

`653c723500eb88888b21af04be40a384bffa76db`

At the start of execution:

1. resolve live `main` again and record its exact SHA;
2. inspect the current branch and verify it was created from a current-enough `main`;
3. if `main` advanced, update this branch by the repository's normal history-preserving process;
4. read `AGENTS.md` in full enough to apply current artifact-lifetime, validation, PR, and review rules;
5. never treat the old research workspace or closed PR #395 as landed truth.

## Quick docs search

Before editing, search `docs/` for at least:

- `Engine applicability`
- `optional-record Factory`
- `transfer lifecycle`
- `spatial record absent`
- `SUPERSEDED`
- `knowledge-transfer audit`
- `retained research artifact`
- `engine-semantics:v2`
- `PR #395`

Fetch every material search hit at the exact current target revision before relying on it.

## Read first

Read the current versions of:

1. `AGENTS.md`
2. `.github/agents/work-planner.agent.md`
3. `docs/development/researching.md`
4. `docs/research/README.md`
5. `docs/research/research-register.md`
6. `docs/research/investigations/engine-evolution.md`
7. `docs/research/investigations/transfer-semantics.md`
8. `docs/research/investigations/factory-model-semantic-composition.md`
9. `docs/architecture/factory-model-v2.md`
10. `docs/architecture/engine-semantics-v1.md`
11. `docs/planning/spatial-runtime-consequences.md`
12. `docs/planning/factory-simulation-engine-readiness.md`
13. `docs/planning/factory-design-capability.md`
14. `docs/history/README.md`

Also inspect closed PR #395 only as failed delivery history.

## Exact prior evidence

The old research workspace is:

`workspace/research-engine-v2-applicability`

Treat it as **read-only evidence custody** during this task. Do not use it as the merge candidate and do not mutate or rewrite its handed-off evidence history.

### Research report

- commit: `c409ecd0bb2941101907a7eb998d8d3ef6b7ce4a`
- path: `workspace/research/investigations/engine-applicability-optional-record-factory-policies.md`
- research baseline recorded by the report: `7e5e6ad2f8a2707d00fff3e2a4e3db96da85852a`

### Independent adversarial review

- commit: `8bae050dfa9fbaea1ada8ba697f46047c3cb0601`
- path: `workspace/research/investigations/engine-applicability-optional-record-factory-policies-adversarial-review.md`
- reviewed report commit: `c409ecd0bb2941101907a7eb998d8d3ef6b7ce4a`
- disposition: `ACCEPT WITH QUALIFICATIONS`
- review baseline: `5787bdd39d8268cf6ad55a7ac083fbb0744ceb04`

Read both exact artifacts in full before curating any durable retained research evidence.

The old workspace may currently contain failed #395 reconciliation edits as well as valid evidence. Those failed durable edits are not authority and must not be copied into current semantic state.

## Why this reconciliation exists

The predecessor investigation asked, in substance:

> Which Engine semantics identity may execute the reconciled `factory-model:v2` policy, with the optional spatial record present or absent, and can `engine-semantics:v1` be stated as applicable without changing its fixed definition?

Its report and independent adversarial review supported a distinguishable Engine identity for the two-form V2 policy, subject to qualifications.

Merged PR #394 later exposed an upstream premise the investigation had inherited rather than independently tested:

> spatial record absent implies no transfer lifecycle.

Current `main` now carries a separate **READY, Critical-path, High-risk** transfer-lifecycle-independence question. Engine applicability is therefore no longer correctly bounded until that upstream question is reconciled.

The predecessor investigation produced useful evidence, but its both-form applicability conclusion must not be promoted as current architecture.

## Lifecycle reconciliation

Apply the research operating model literally.

The predecessor applicability investigation should become a terminal:

`SUPERSEDED`

research-history item because later evidence/question framing replaced it before its proposed architectural consequence was durably reconciled.

The successor post-transfer applicability question is a **separate** question and remains:

`CANDIDATE`

until transfer-lifecycle independence is durably reconciled.

Do not model this as one research question that moved:

`READY -> ACTIVE -> reviewed -> CANDIDATE`

That would couple the old evidence workspace indefinitely to a materially different future investigation.

The durable lineage should read conceptually:

```text
predecessor Engine-applicability investigation
    |
    | completed report + independent review
    | ACCEPT WITH QUALIFICATIONS
    |
    | later upstream premise becomes disputed
    v
SUPERSEDED research evidence
    |
    +--> READY transfer-lifecycle independence
             |
             v
       durable transfer reconciliation
             |
             v
       successor Engine-applicability question
       CANDIDATE -> READY when re-promotion trigger is met
```

## Required durable changes

### 1. Retain the predecessor investigation under docs/research

Create:

`docs/research/investigations/engine-applicability-optional-record-factory-policies.md`

unless current repository conventions reveal a clearly better semantic path.

This is a deliberately retained research-history artifact.

Its header must make status and authority explicit, for example:

- `Status: SUPERSEDED`
- `Risk: High`
- `Authority: Research evidence/history only`

Make clear that current architecture/specifications and the maintained research register remain authoritative for current meaning and state.

Do **not** copy the temporary report and review byte-for-byte. Curate a durable investigation write-up that preserves the material evidence future researchers would otherwise have to rediscover.

At minimum preserve:

- original bounded question and decision at stake;
- relevant research-baseline context;
- Candidate A / Candidate B comparison;
- whole-definition preservation test;
- accepted-input and refusal analysis;
- spatial-present versus spatial-absent distinction;
- historical interpretability/provenance analysis;
- reusable proving cases;
- report conclusion **as the conclusion reached at that time**;
- independent adversarial disposition;
- material qualifications Q1-Q5;
- source/evidence categories or source map where future reasoning materially depends on them;
- the later evidence/question that superseded the conclusion;
- explicit statements of what remains reusable and what no longer survives as current conclusion.

### 2. State what survives as reusable evidence

Preserve, where supported by the predecessor evidence and review:

- whole-definition fixation covers accepted input, interpretation, outputs, refusals, and referenced definitions;
- absence of a failing fixture is not affirmative proof of applicability preservation;
- Factory validity and Engine applicability are distinct predicates;
- absence and authored zero must not be conflated where the owning semantics distinguish them;
- Engine support/refusal boundaries must be explicit;
- historical Engine attribution must not silently change;
- a successor interpretation must preserve truthful Factory fingerprint provenance rather than relabeling or projecting away authored content;
- present-only applicability and both-form applicability are different proof obligations;
- proving cases around supported/unsupported content, refusal, provenance, historical meaning, present/absent content, and future optional concerns remain useful inputs.

### 3. State what is superseded

Explicitly state that the retained predecessor artifact does **not** establish as current Arcogine conclusions:

- adoption of `engine-semantics:v2`;
- that one successor Engine identity should necessarily cover both V2 forms;
- that spatial-record absence means production-only execution with no transfer lifecycle;
- that Engine applicability is concluded;
- that the research blocker on V2 publication or spatial-runtime activation has cleared;
- any failed architecture/planning state from PR #395.

### 4. Preserve adversarial-review qualifications

Carry the review's material qualifications into the retained artifact in durable semantic language.

Preserve at least:

- **Q1 — conditional proof boundary:** failure to prove a whole-definition correction is not a universal theorem that every Factory-policy difference requires a new Engine identity.
- **Q2 — present-only/partition scope:** present-only preservation was unproven, not disproven; a hybrid support partition was coherent in principle but not established.
- **Q3 — exact attribution/dependency support:** historical semantic meaning must remain exactly resolvable and Factory fingerprint provenance truthful.
- **Q4 — research acceptance versus implementation evidence:** research did not prove publication, runtime establishment, provenance propagation, or successor conformance implementation.
- **Q5 — bounded reopening:** later evidence affecting the Factory/Engine boundary can reopen the conclusion.

Record that the newly admitted transfer-lifecycle question is an actual exercise of the reopening condition, not proof that all predecessor analysis was wrong.

### 5. Create a separate successor applicability brief

Create:

`docs/research/investigations/engine-applicability-after-transfer-boundary.md`

unless repository conventions support a materially better semantic name.

Status:

`CANDIDATE`

Frame the question around the **eventual reconciled transfer semantics**, not around the old report's assumed absent-case semantics.

A suitable bounded question is:

> After transfer-lifecycle independence is reconciled, which Engine semantics identity or identities may execute each exact `factory-model:v2` represented-content case, and can any existing fixed Engine definition admit those cases without changing its whole definition?

The successor brief must not assume:

- spatial absence means no transfer;
- every distinct-resource continuation has transfer;
- `engine-semantics:v2` exists;
- one Engine identity must cover both V2 forms;
- v1 can or cannot support the spatial-present form;
- Engine and Factory version numbers must align one-to-one.

### 6. Define the successor re-promotion trigger

Keep the successor brief `CANDIDATE` until transfer-lifecycle independence is durably reconciled.

Require the landed transfer result to make explicit:

- whether distinct-resource transfer exists without spatial facts;
- the semantics of no-transfer versus transfer-with-zero-duration versus refusal;
- what authored fact or Engine rule determines transfer applicability;
- any correction/reopening to the still-unreleased Factory V2 grammar;
- the exact represented-content cases the successor Engine-applicability investigation must evaluate.

Only then can the successor question become `READY`.

### 7. Carry proving dimensions forward without anchoring the answer

The successor brief may cite the superseded artifact as **prior evidence**, but must instruct a future researcher to derive the candidate set from the landed transfer contract before accepting predecessor conclusions.

Relevant proving dimensions include:

- Factory V1 under Engine v1;
- V2 spatial-present;
- V2 spatial-absent, with meaning supplied by the future transfer reconciliation;
- present legal zero versus absence;
- Factory-invalid content;
- Factory-valid but Engine-unsupported policy/content;
- unsupported Engine identity;
- true Factory fingerprint provenance;
- historical interpretation of already-attributed v1 results;
- potential support partitioning where present/absent forms differ semantically;
- a future Factory policy with optional content irrelevant to a given Engine interpretation.

Do not mechanically carry forward Candidate B as the default answer.

## Research-register changes

Update `docs/research/research-register.md` so predecessor and successor are distinct rows.

### Predecessor row

Record:

- area: Engine / Factory Design;
- prior critical-path significance as appropriate to register convention;
- status: `SUPERSEDED`;
- detailed artifact: the durable retained predecessor investigation;
- result/destination: the completed report and independent review remain useful research evidence, but the both-form applicability conclusion was superseded before architectural reconciliation when transfer-lifecycle independence became an admitted upstream question;
- current review date.

Do not state that `engine-semantics:v2` was adopted.

### Successor row

Record:

- area: Engine / Factory Design;
- priority: `Critical-path`;
- status: `CANDIDATE`;
- detailed artifact: the new successor brief;
- expected destination: after transfer-lifecycle reconciliation, re-promote when correctly bounded; then run decision-quality research and required independent adversarial review before any Engine architecture/specification reconciliation;
- keep V2 publication/spatial activation dependency truthful.

The existing READY transfer-lifecycle question remains separate.

## engine-evolution.md reconciliation

Do not leave a second competing full applicability brief embedded in `docs/research/investigations/engine-evolution.md`.

Refactor that section to provide only the necessary Engine-research context and link to:

- the superseded predecessor artifact;
- the successor post-transfer applicability brief;
- the READY transfer-lifecycle brief.

Avoid duplicating lifecycle state, candidate lists, proving cases, or exit criteria after the dedicated brief owns them.

Do not disturb unrelated Engine-evolution research.

## transfer-semantics.md lineage

Modify `docs/research/investigations/transfer-semantics.md` only if useful to make the supersession chain understandable.

If changed, keep it narrow:

- state that the READY lifecycle question was exposed because the predecessor applicability investigation inherited `spatial absent => no transfer lifecycle` as a premise;
- clarify that the superseded predecessor remains prior evidence but does not constrain the transfer candidate outcome.

Do not import the old Engine-v2 recommendation into the transfer brief.

## Architecture and planning non-goals

This task must not create, restore, or retain as current authority:

- `docs/architecture/engine-semantics-v2.md`;
- a v1 applicability restriction derived only from the failed #395 reconciliation;
- a claim that V2 necessarily executes under a new Engine identity;
- planning text clearing transfer/applicability research prerequisites;
- implementation for V2 publication;
- runtime establishment of a successor identity;
- transfer/spatial runtime activation.

Current landed architecture and planning remain authoritative.

Only fix architecture/planning links if the research-document split creates a broken/stale reference that must be updated without changing semantic state.

## Historical-rationale decision

Do **not** create or retain:

`docs/history/decisions/2026-09-24-engine-applicability-optional-record-factory-policies.md`

or an equivalent Engine-v2 decision record.

No Engine-v2 choice was successfully reconciled. Historical decision-rationale records are for significant reconciled choices, not failed proposals.

The superseded research artifact is the correct retention surface.

## Synthesis-seed decision

Evaluate `docs/research/synthesis-seeds.md` under current admission rules.

Default expectation: no new synthesis seed.

Do not manufacture cross-investigation recurrence from a dependency/framing correction within one research chain.

If no seed is justified, record that explicit decision in the knowledge-transfer audit.

## Knowledge-transfer audit

Before treating the old workspace as retirement-eligible, explicitly account for every material result.

### Promote durably

- predecessor research framing and substantial analysis;
- predecessor conclusion as a superseded historical research result;
- independent adversarial disposition and Q1-Q5;
- reusable proving cases;
- accepted-input/refusal/provenance analysis;
- exact supersession reason;
- successor re-promotion conditions.

### Already durable elsewhere

- READY transfer-lifecycle question;
- current Factory V2 grammar;
- current Engine v1 contract;
- current planning dependency chain.

### Discard as transient or failed delivery scaffolding

- original research handoff prompt;
- adversarial-review prompt;
- reconciliation prompt;
- failed `engine-semantics:v2` canonical draft;
- failed v1 applicability edit;
- failed planning-gate clearance;
- failed historical decision-rationale draft;
- temporary #395 reconciliation wording;
- branch-local mechanics that do not change future reasoning.

### Exact-artifact retention decision

The curated durable predecessor artifact must preserve enough decision-relevant evidence that future research does not require the original workspace report/review files to remain fetchable.

Do not copy exact workspace SHA+path coordinates into durable docs as pseudo-retention.

If a material exact artifact truly needs future readability, deliberately promote its content or artifact into an allowed durable retention surface before retirement.

## Expected durable diff

Prefer a narrow documentation-only diff approximately consisting of:

- add `docs/research/investigations/engine-applicability-optional-record-factory-policies.md`
- add `docs/research/investigations/engine-applicability-after-transfer-boundary.md`
- modify `docs/research/research-register.md`
- modify `docs/research/investigations/engine-evolution.md`
- optionally modify `docs/research/investigations/transfer-semantics.md`
- optionally update `docs/research/README.md` only if navigation genuinely requires it

Avoid architecture/planning/product changes unless a link correction is mechanically necessary because of the research-doc split.

## Validation

Use the current narrowest repository validation for a documentation-only change.

At minimum run the current equivalents of:

- Markdown/link validation;
- delivery-label validation;
- transient-coordinate validation;
- tracked-workspace validation after removing this prompt;
- `git diff --check`.

Also search the final candidate for stale claims that:

- Engine applicability is `CONCLUDED`;
- `engine-semantics:v2` is adopted;
- the applicability research prerequisite is cleared;
- the predecessor and successor questions have been accidentally conflated.

If current `AGENTS.md` names different canonical commands, use those instead.

## Prompt cleanup

Before independent PR review:

1. remove this prompt from `workspace/implementation/`;
2. inspect all branch-added files for transient scaffolding;
3. run the tracked-workspace check;
4. confirm the final candidate contains no tracked `workspace/` path.

## Pull request

Create a focused research-reconciliation PR against current `main`.

The PR should explain:

- the predecessor applicability investigation completed substantial research and independent adversarial review;
- PR #394 later invalidated one upstream premise before architecture reconciliation;
- the predecessor is therefore retained as `SUPERSEDED` research evidence;
- a separate post-transfer applicability question remains `CANDIDATE`;
- this PR adopts no Engine semantics identity;
- it clears no V2 publication or spatial-runtime blocker;
- the durable transfer exists so the old evidence workspace can retire instead of becoming a long-running archive.

Closed PR #395 may be mentioned only as failed delivery history where useful.

Request/use normal independent PR review under Arcogine's PR Reviewer contract.

Do not merge autonomously.

## Workspace retirement

After this research-reconciliation PR:

1. lands on `main`;
2. receives normal independent PR review;
3. durably promotes all material predecessor evidence;
4. marks the predecessor `SUPERSEDED`;
5. leaves the successor separately `CANDIDATE`;
6. completes the knowledge-transfer audit;

the old branch:

`workspace/research-engine-v2-applicability`

is retirement-eligible.

Delete it as immediate post-merge cleanup rather than retaining it until the future successor investigation starts.

Future transfer-lifecycle and successor Engine-applicability investigations must use their own bounded research workspaces.

## Final report

Return:

- live-main baseline used;
- implementation/reconciliation branch;
- durable reconciliation commit SHA;
- changed durable research surfaces;
- predecessor durable artifact path;
- successor brief path;
- predecessor lifecycle status;
- successor lifecycle status;
- knowledge-transfer audit result;
- synthesis-seed decision;
- historical-rationale decision;
- validation performed;
- PR number/URL if created;
- independent PR-review status;
- whether the old workspace is retirement-eligible;
- if not, the exact remaining condition.

Do not claim Engine applicability itself is concluded.
