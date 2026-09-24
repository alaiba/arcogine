# Research handoff — semantic-contract promotion and premature durability

You are the Arcogine Researcher. Execute this as a **fresh, bounded, high-risk standard investigation** under `.github/agents/researcher.agent.md` and `docs/development/researching.md`.

Do not treat the framing below as a conclusion to defend. The purpose of the investigation is to test whether Arcogine has prematurely converted proving/development identities into durable semantic contracts, and whether that assumption is now forcing unnecessary whole-contract version proliferation.

## Bounded question

Arcogine currently states that semantic identity never rebinds and that a definition becomes fixed once a retained or accepted record is attributed to it. In particular, `factory-model:v1` says it is immutable because "published fingerprints, controlled revisions and stored canonical artifacts reference it", while the support policy separately says disposable proving activity creates no durability obligation and that tests, scratch stores, drained events and local runs remain disposable until retained use is deliberately admitted.

Investigate:

> **What exact event or retained-use boundary, if any, legitimately promoted the current Factory and Engine semantic definitions from mutable proving definitions into durable immutable identities, and has Arcogine applied that boundary too early?**

The investigation must determine whether `factory-model:v1` and, secondarily, `engine-semantics:v1` are already entitled to immutable-contract treatment, or whether some or all current "versioning" is development history that should still be correctable before an explicit commitment boundary.

## Decision at stake

The result may require one of several materially different conclusions:

1. the current durability interpretation is justified because a concrete retained authority or published reliance already crossed the freeze boundary;
2. the architectural rule is sound but the repository lacks evidence/declaration for the claimed boundary and therefore needs reconciliation;
3. `factory-model:v1` was prematurely treated as durable because proving artifacts, internal fingerprints, fixtures, or implementation milestones were mistaken for committed reliance;
4. the project needs an explicit pre-promotion/provisional identity regime so deterministic fingerprinting can be exercised without creating a compatibility estate;
5. some narrower mixed result, for example Factory and Engine having different legitimate commitment boundaries.

Do not assume external release is the only possible commitment boundary. An intentional internal authority that retains attributed records for historical/accountability purposes may legitimately freeze an identity even before an external release. Conversely, merely implementing a canonicalizer, generating a digest, storing a fixture, creating a test controlled revision, or naming something `v1` must not be treated as sufficient unless the owning contracts actually establish retained accepted use.

## Risk classification

Treat this as **high risk** because it affects semantic identity, persistence, historical attribution, determinism, compatibility obligations, and potentially foundational architecture.

A completed report is not sufficient for durable reconciliation until it has received a genuinely independent adversarial research review under the repository research protocol.

## Independence / anchoring discipline

Before consuming the conclusions of the existing concluded durability/composition investigations in depth:

1. resolve live `main` and record the exact SHA;
2. read `AGENTS.md`, `docs/development/researching.md`, and `docs/research/research-register.md`;
3. independently reconstruct the current semantic-identity and support rules from authoritative architecture/specification and implementation;
4. inspect concrete retention/acceptance behavior and persistence boundaries;
5. derive candidate answers and likely failure cases;
6. only then compare your reconstruction with the prior research conclusions.

Prior research is evidence/provenance, not authority over the answer.

## Required repository evidence

At minimum inspect and reconcile the current versions of:

- `docs/architecture/overview.md` — semantic evolution and support;
- `docs/development/semantic-contract-support.md`;
- `docs/architecture/factory-design.md`, especially publication identity and semantic evolution;
- `docs/architecture/factory-model-v1.md`;
- `docs/architecture/factory-model-v2.md`;
- `docs/architecture/engine-semantics-v1.md`;
- `docs/planning/factory-design-capability.md`;
- `docs/research/investigations/semantic-contract-maturity-durability.md`;
- `docs/research/investigations/factory-model-semantic-composition.md`;
- the relevant research-register entries;
- Factory fingerprint/artifact implementation and tests;
- Governance controlled-revision implementation and tests;
- Engine semantics identity/runtime attribution implementation and tests;
- any persistence/store/repository implementation that could make an attributed record actually retained beyond disposable proving activity;
- release/tag/publication history where relevant;
- merged PR/commit history where it is needed to establish whether a deliberate promotion or acceptance event actually occurred.

Search semantic neighbors rather than stopping at these paths.

When making an absence claim such as "no retained authority exists" or "no promotion event is recorded", state the exact search scope and limitation.

## Core challenges to test

### A. What does "attributed retained record" concretely mean in Arcogine today?

Do not accept the phrase at face value. Identify the actual authority and custody boundary.

Distinguish at least:

- deterministic fingerprints produced during tests or local runs;
- canonical artifacts created only for fixtures or proving;
- in-memory records that disappear with a process/test;
- committed test vectors or sample artifacts;
- controlled revisions used only as executable evidence;
- records deliberately accepted into an authority intended to preserve historical meaning;
- outward/public reliance;
- a release or promotion declaration;
- repository history itself.

For each relevant category, decide whether current architecture says it creates a non-rebinding obligation and whether the implementation/repository actually contains such a case.

### B. Audit the V1 immutability claim

`factory-model:v1` currently states that it "has attributed records: published fingerprints, controlled revisions and stored canonical artifacts reference it."

Treat that as a claim requiring evidence, not as evidence for itself.

Determine:

- what "published fingerprints" refers to concretely;
- what "stored canonical artifacts" refers to concretely;
- which controlled revisions, if any, constitute retained authoritative use rather than disposable proving evidence;
- where custody, retention horizon, support obligation and exact-definition resolution are declared;
- whether an explicit promotion/acceptance event can be identified;
- whether any claimed commitment predates the policy that now explains why it is immutable;
- whether the reasoning is circular: "V1 is immutable because V1 artifacts exist, and those artifacts are durable because V1 is immutable."

If a legitimate retained boundary exists, identify it precisely and explain why it satisfies the current support rules.

### C. Separate deterministic identity machinery from durable contract commitment

Test whether Arcogine needs two concepts:

1. a **provisional deterministic identity mechanism** used to prove canonicalization, equality and reproducibility while semantics are still mutable; and
2. a **committed semantic identity** whose meaning cannot later rebind because Arcogine has deliberately accepted obligations against it.

Assess whether the current architecture already supports this distinction implicitly, whether it contradicts the prior rejection of a universal `proving/promoted` lifecycle, and whether a narrow rule can preserve both conclusions.

Do not assume a repository-wide maturity state is required. Consider whether the necessary rule can instead be a boundary invariant such as:

> deterministic identity generation does not itself create retained attribution; only deliberate acceptance into a declared retained authority or published reliance freezes the definition.

Evaluate how such a rule would be enforced or evidenced.

### D. Evaluate the proposed "WIP/unreleased" idea without assuming it is correct

The motivating concern is that calling the first implemented policy `v1` encourages developers to read it as already-final and makes every correction look like a `v2`.

Test alternatives, including but not limited to:

- one mutable provisional identity such as `wip`/`draft`/`unreleased`, forbidden from retained authoritative use;
- a numeric pre-release identity such as `v0`;
- no externally meaningful policy identity before promotion, using test-local deterministic bytes only;
- immutable ephemeral identifiers for proving runs, without long-lived compatibility obligations;
- the current model, where named policy identity can exist before broad support but freezes on first retained attribution;
- another model justified by evidence.

Do not choose terminology until the lifecycle/commitment semantics are settled. The question is not primarily whether the token should literally be `wip`; it is whether provisional deterministic identity must be visibly and operationally distinct from a committed durable semantic contract.

### E. Re-test Factory V1 → V2 under the corrected durability boundary

The existing Factory composition research concluded that unreleased `factory-model:v2` should preserve V1 unchanged and add an optional spatial record under a new closed policy.

Re-evaluate that reasoning under both cases:

- **Case 1: V1 had legitimately crossed the commitment boundary.**
  Determine whether a distinguishable successor identity is actually necessary and whether ordinal `v2` is semantically appropriate.
- **Case 2: V1 had not crossed the commitment boundary.**
  Determine whether spatial semantics should instead have been incorporated by correcting the still-provisional Factory grammar, with proving vectors/fingerprints changing in place and no V2 compatibility estate created.

Do not automatically conclude that spatial semantics belong in the same aggregate. The prior semantic-composition candidates remain relevant; the question here is specifically whether "V1 is already frozen" improperly constrained that choice.

### F. Re-test Engine semantics separately

Apply the same analysis to `engine-semantics:v1`, but keep Factory as the primary case.

Determine:

- whether any retained simulation result is actually attributed to the current Engine semantics identity under a declared authority;
- whether current tests/runs are disposable proving evidence;
- whether the current complete-result-affecting-interpretation contract was deliberately promoted or merely named;
- whether fixing an unreleased semantic mistake must really mint `engine-semantics:v2`;
- what boundary should make future Engine semantics non-rebinding.

Do not assume Factory and Engine must share one promotion rule or moment.

## Candidate models

Derive your own candidate set, but ensure the analysis includes serious versions of these positions:

- **Current strict non-rebinding model:** current V1 is already legitimately attributed/fixed.
- **Premature-promotion model:** current V1 attribution is proving activity and should not yet freeze semantics.
- **Evidence-gap model:** the intended rule is correct, but the repository has failed to document or implement the authority/custody event that would justify the claimed freeze.
- **Scoped mixed model:** some existing identities or records are legitimately retained while others are disposable; the correction should be scoped rather than global.
- **No-new-lifecycle model:** preserve the rejection of a universal maturity state while defining a precise acceptance boundary that prevents accidental promotion.

Add omitted candidates if repository or external evidence exposes them.

## Required proving/failure cases

Derive additional cases from the candidates, but explicitly test at least:

1. A golden fingerprint vector changes before any retained authority accepts it.
2. An internal test creates a `ControlledRevision` and then deletes all runtime state.
3. A developer stores a canonical artifact only as a test fixture in Git.
4. A repository-owned durable example or fixture refers to `factory-model:v1`, but no consumer promises compatibility with it.
5. An internal retained authority deliberately accepts a model fingerprint and must explain it months later, before any public release.
6. A public/external consumer persists the identity.
7. A WIP/provisional identity accidentally crosses into an authoritative store.
8. A semantic defect is discovered immediately before promotion.
9. The same defect is discovered immediately after a legitimate promotion.
10. Spatial semantics are discovered while Factory identity is still provisional versus after it is committed.
11. Engine interpretation changes during internal proving versus after retained result attribution.
12. A support capability is retired while historical attributed records remain.

For each candidate model, show how the case is handled and what obligation is created.

## External evidence

Because this is a high-risk identity/persistence question, seek external evidence only where it materially discriminates between candidates or exposes failure modes.

Useful evidence may include mature practices around provisional versus committed protocol/schema/semantic identities, draft specifications, pre-release compatibility promises, content-addressed identity, migration boundaries, or persisted event/schema evolution. Prefer primary/official sources and record exact provenance.

Do not import SemVer or any other versioning scheme by analogy without demonstrating that its equality, consumer, release and persistence assumptions actually transfer to Arcogine. Explicitly state where each analogy breaks.

## Naming question

Treat naming as a consequence, not the starting point.

The report should answer separately:

1. Does Arcogine need distinguishable durable semantic identities?
2. When does an identity become non-rebinding?
3. Does that imply ordinal versioning?
4. Before promotion, should provisional deterministic identities be named in a way that makes non-durability explicit?
5. After promotion, would semantic names be preferable to `v1`/`v2` where the policies represent materially different semantics?

Do not recommend renames until the identity/lifecycle analysis supports them.

## Expected report outcome

Produce a decision-quality report using `docs/research/report-template.md` that:

- records the live-main baseline;
- states the exact bounded question and decision at stake;
- classifies the question as high risk;
- separates repository fact, external evidence, inference and recommendation;
- identifies the concrete evidence for or against current V1 durability;
- states whether the current V1 immutability assertion is substantiated, unsubstantiated, circular, or only partially substantiated;
- evaluates the candidate models against proving cases;
- explicitly assesses whether current Factory V1/V2 proliferation depends on a premature freeze assumption;
- assesses Engine separately;
- recommends the narrowest durable destination if a change is warranted;
- identifies what current architecture/research would need reopening or reconciliation, without performing that reconciliation;
- states confidence, limitations, unresolved unknowns and reopening triggers;
- states that a genuinely independent adversarial review is required before any high-risk conclusion is reconciled into canonical architecture.

If the evidence supports the current architecture, say so plainly and identify the exact promotion/retained-use evidence that defeats the premature-durability concern.

If the evidence supports reopening the concluded semantic-contract-maturity or Factory-composition result, say exactly which load-bearing premise fails and which durable surfaces would need later reconciliation.

## Non-goals

Do not:

- edit canonical architecture/specification as part of the investigation;
- rename code/types/files;
- implement a WIP identity mechanism;
- create a migration framework;
- assume all pre-release identities are disposable;
- assume all internal data is non-authoritative;
- assume an external release is required to create obligations;
- assume `v0`, `wip`, or semantic naming is the answer;
- collapse Factory and Engine promotion semantics without evidence;
- treat prior research conclusions as repository authority;
- mark the question `CONCLUDED` merely because this report exists.

## Persistence and handoff

Follow the Researcher custody contract. Persist the completed report under a semantically named path in `workspace/research/investigations/` on this same research workspace branch, record its exact commit SHA and research-baseline SHA, and return those coordinates.

Because this is high risk, hand the completed report to a genuinely independent adversarial research review before any architecture/specification reconciliation is treated as decision-quality.
