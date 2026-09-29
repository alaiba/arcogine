# Implement Governance-driven product storage reconciliation

## Task and authority

Work in `alaiba/arcogine`. This is an implementation handoff for an explicit owner-directed product and architecture decision. Deliver the semantic reconciliation AND the source-code refactor, tests, build wiring, research briefs, and implementation planning in one coherent candidate. Do not stop after writing documentation, propose retiring Governance, or substitute a test-only storage module.

The owner's decision is:

- Governance is a core Arcogine capability and a legitimate source of requirements pressure. It may drive innovation and early investment in difficult-to-retrofit identity, provenance, historical accountability, acceptance, and integrity capabilities. An outward UI, API, deployment integration, or third-party customer is not the only legitimate source of a concrete product requirement.
- Preserve the existing Governance concepts and storage functionality. The earlier suggestion to delete, quarantine, or freeze this work is superseded by this decision.
- Reframe the current storage implementation as a first-class, product-owned built-in storage capability, not an artifact whose purpose is merely to prove Governance tests. Its purpose is to store Arcogine's authoritative information correctly, with its guarantees under Arcogine's control.
- Extract it into an independent product module implementing an explicit public storage contract. Expose contracts and necessary configuration/value types; keep the concrete persistence implementation private. Separation of meaning from mechanism and enforcement of that boundary are the primary goals. Future substitution of a third-party persistence implementation is possible but secondary.
- Give Storage a truthful place in canonical product/architecture documentation, the research portfolio, and admitted development planning.

This authorizes changing the existing proving-only ownership/purpose restrictions through normal reviewed reconciliation. It does not authorize claiming perfect reliability, promoting every stored semantic definition to a stable compatibility contract, deleting stored data, changing Factory/Engine meaning, or introducing a general-purpose database framework.

Use the owner-decision route in `docs/development/researching.md` and `docs/development/reviewing.md`. Do not require a research report to re-decide whether Governance may drive development or whether Arcogine should own built-in storage. Actual unresolved technical questions remain research questions, not conclusions established by this prompt. Independent PR review remains required; do not perform your own independent review or merge.

## Grounding and branch

Handoff baseline: canonical `main` at `8b59f9d9b8c8715e6b60f305d6975912861fc3fc`, verified against the attached snapshot on 2026-09-29. Snapshot-to-target comparison was identical. The live open-PR search returned no open PRs at handoff creation. No unmerged PR is a prerequisite. Recheck both facts when you start; this is an evidence coordinate, not a claim that `main` will remain there.

Use `feature/built-in-storage-reconciliation`, the branch carrying this prompt, as the implementation branch unless an actual conflict requires a different branch. Fetch it, inspect the worktree and branch before editing, preserve unrelated work, and reconcile any newer `main` without rewriting handed-off history. Do not rebase or force-push away the prompt commit. Remove this prompt and all other tracked `workspace/` artifacts from the final candidate before independent review; their presence is not a reason to publish a prompt-only PR.

Read current `AGENTS.md` and `.github/CONTRIBUTING.md` first. With an attached Repomix, read its embedded `<project_instructions>` and follow the exact-SHA snapshot/delta protocol. With a checkout, establish the actual target revision and read the corresponding source. Repository search is discovery, not target-revision authority. Follow current repository commands, human Git identity, branch, validation, and handoff rules. Do not carry obsolete mechanics from this prompt forward when those rules have legitimately changed.

Recent relevant reconciliation: #405 reset provisional semantic commitments; #407 clarified exact-reference promotion; #408 removed technical development labels and placeholder Engine identity; #410 established the current PR-body and exact-head CI handoff rules. Current source and owning documentation, not their descriptions or prior chat, define implemented behavior.

### Read before designing the change

Product and architecture:

- `docs/product/charter.md`, `docs/product/concepts.md`.
- `docs/architecture/overview.md`, especially identity/durability vocabulary, semantic evolution/support, module dependencies, and the current revision-store description.
- `docs/architecture/controlled-revisions.md`, `governance-conformance.md`, `governance-evidence.md`, and `external-representations.md`.
- Relevant publication/identity sections of `docs/architecture/factory-design.md` and `factory-model.md`; the Engine/runtime contracts only where provenance or support statements are affected.

Planning and development:

- `docs/planning/README.md`, `governance-conformance-capability.md`, `governance-continuity.md`, and relevant Factory/Operational dependency statements.
- `docs/development/semantic-contract-support.md`, `researching.md`, `reviewing.md`, and `testing.md`.
- `docs/research/research-register.md`, `docs/research/brief-template.md`, and `docs/research/investigations/semantic-equivalence-and-reference-boundaries.md`.
- `docs/history/decisions/2026-09-25-provisional-semantic-contract-reset.md` as historical rationale, not a replacement for current authority.

Implementation and tests:

- `product/governance/src/main/java/com/arcogine/governance/{ControlledRevisionAuthority,FileControlledRevisionAuthority,ControlledRevision,HistoricalRevision,SemanticArtifact,SemanticArtifactVerifier,GovernanceHistoryException,RevisionProvenance,RevisionRecorder}.java`.
- Governance `change/ChangeSetFactory.java`, the conformance evaluators, and callers of the revision authority.
- `product/domains/factory/src/main/java/com/arcogine/factory/model/{FactoryModelArtifact,FactoryModelCanonicalForm}.java` and `factory/change/FactoryModelSemanticComparator.java`.
- `FileControlledRevisionAuthorityTest`, `GovernanceModuleBoundaryTest`, `ChangeSetFactoryTest`, `ConformanceEvaluatorTest`, `PreChangeConformanceProvingCaseTest`, and `GovernanceEvidenceTest` under Governance tests; Factory artifact/canonical-form/comparator tests.
- `product/settings.gradle.kts`, `product/build.gradle.kts`, affected module build files, and `product/architecture-conformance-test/`.
- `.github/scripts/classify-changes.sh`, its tests, `.github/scripts/check-delivery-labels.mjs`, relevant tooling tests, and docs indexes.

Search `docs/` and product source for the request's main vocabulary, including `proving store`, `proving-store`, `openProvingStore`, `FileControlledRevisionAuthority`, `disposable`, `retained authority`, `commitment-bearing`, `definitionBinding`, `controlled revision`, `storage`, `persistence`, `concrete consumer`, and `concrete responsibility`. Follow semantic neighbors, not just literal class references. Do not globally replace every use of 'proving': tests can still be proving cases, and physical factory storage is not software persistence.

## Verified starting point and the limits of that evidence

At the handoff baseline:

- `FileControlledRevisionAuthority` is a public production class inside `:governance`. Public `openProvingStore(Path, SemanticArtifactVerifier)` returns that concrete class. There is also a package-private clock-injection overload.
- `ControlledRevisionAuthority` already defines `accept`, `findById`, `resolve`, and `revisions`. Reuse this semantic port rather than create a competing revision identity or authority.
- The implementation owns filesystem directories, marker/record encodings, artifact keys, a JVM-wide process lock, filesystem locking, and temporary-file installation. `writeAtomic` forces the temporary file and uses an atomic move; unsupported atomic moves fail explicitly. Acceptance stores the artifact before the revision record and establishes recording time through the authority clock.
- The root carries a proving-scope marker and `SemanticArtifactVerifier.definitionBinding()`. Factory computes that binding from an explicit set of definition class names/bytecode. It is a conservative exact-build guard, not a semantic-equivalence proof or a replacement for `ModelFingerprint`.
- The implementation rejects unknown/foreign roots, missing ownership/lock metadata, duplicate/rebound revision IDs, unsupported artifacts, and corrupt or mismatched content. It resolves exact accepted artifacts, supports zero-or-one-parent lineage, and permits distinct historical occurrences with equal fingerprints.
- The named reopen test reopens another object in the same JVM. The conflicting-acceptance test uses executor threads. Those tests do not by themselves establish a process-kill, host-power-loss, or independent-process concurrency guarantee. Do not copy earlier conversational claims of such proof into current documentation.
- Production Governance depends only on `:types`; Factory implements narrow Governance artifact/comparison ports. Governance has Factory as a test dependency. No Storage module exists. Durable evaluation/evidence storage and producer integrations are not supplied by the controlled-revision store.

Reinspect these details at your target revision. Preserve the behavior and evidence that actually exist, while correcting any claimed guarantee that exceeds them.

## 1. Reconcile the product principle

Amend the Product Charter at the level of enduring product principles, not class names, technologies, or delivery coordinates:

> Arcogine's core lifecycle capabilities, including Governance and verification, may impose concrete foundational requirements and intentionally drive early investment where deferring identity, provenance, history, or authority would make later correctness costly or impossible to recover. A headless capability can be a legitimate product requirement source before an outward experience exists.

Integrate this with the existing one-model/lifecycle thesis. Do not redefine Arcogine as only a simulator, only a compliance product, or a database product. Do not imply that a future Operations implementation is required before Governance may develop.

Preserve scope discipline: early investment still needs a named requirement, an owning capability, bounded responsibility, and observable acceptance evidence. 'Governance could eventually want it' is not a blanket admission rule for arbitrary frameworks.

Reconcile `.github/CONTRIBUTING.md` and the relevant architecture/planning guidance so 'concrete consumer' cannot be read as 'only an already-shipped UI or external integration'. Use concise clarification and links to the owning principle; do not duplicate it throughout agent contracts or introduce new process gates or prose-matching CI tests.

## 2. Establish the storage boundary and extract the implementation

Use **Arcogine Storage** as the product capability name and `product/storage/` / Gradle `:storage` as the default module placement, absent a material target-revision conflict.

Define a small public contract covering today's supported storage responsibility. A suitable shape is an `ArcogineStorage` interface exposing `ControlledRevisionAuthority controlledRevisions()`, with a minimal public opener/factory returning the contract. This is a design direction, not a mandate to add a redundant facade or several modules. Choose coherent names, document the final API, and keep all callers contract-typed. A publicly accessible concrete implementation renamed from `FileControlledRevisionAuthority` is not sufficient.

Keep these ownership distinctions:

- Governance owns revision meaning, historical occurrence identity, lineage rules, recording provenance, evidence/conformance meaning, and the controlled-revision acceptance/resolution contract.
- Storage owns the built-in persistent realization, opening/lifecycle, concurrency coordination, integrity enforcement at the persistence boundary, and exact retrieval within its declared support scope. It must implement the Governance port's obligations, not create alternate acceptance semantics.
- Factory owns canonical content, fingerprint computation, artifact decoding/verification, and its definition binding. Storage receives the verifier through the existing domain-neutral seam; it must not interpret Factory records or depend on Factory implementation in its main source set.

The default production dependency direction is:

```text
:governance -> :types
:factory   -> :governance, :types, existing Engine dependencies
:storage   -> :governance, :types
```

Do not make Governance or Factory production depend on the concrete storage module to obtain domain meaning. Do not move Governance types into Storage merely because they are persisted. A product composition point may select/open the implementation and pass the interface to a consumer; this task does not require creating an application server or dependency-injection framework.

Move/refactor the existing filesystem implementation and its relevant tests into the Storage boundary. Keep concrete implementation types inaccessible to ordinary external consumers: package-private types/constructors or an equivalently enforced encapsulation mechanism. A package called `internal` containing freely public implementation types is not sufficient. Do not expose filesystem layout, lock objects, record encoders, physical keys, or file manipulation through domain operations. A storage root in bootstrap configuration is acceptable; it does not make record layout public.

Keep the initial built-in implementation operational without an external database/service or third-party persistence engine. No SQLite, PostgreSQL, broker, new storage library, or WAL rewrite is required or selected by this reconciliation. Standard runtime and filesystem dependencies are not a claim of independence from the operating system or hardware.

Avoid generic `Storage<K,V>` CRUD, an ORM, universal persisted-object ontology, provider registry, plugin discovery, generic event sourcing, or a new cross-domain transaction framework. Extensibility comes first from a real contract and private implementation, not from implementing a second backend.

Make errors and lifecycle behavior usable through the public contract. Reuse existing semantic failure categories where appropriate; do not force consumers to understand private filenames or backend classes. Add configuration/lifecycle types only for real behavior. Do not imply that closing an object is the commit boundary if acceptance commits per operation.

## 3. Specify guarantees without inventing maturity

Create `docs/architecture/storage.md` as the owning storage specification. Separate normative obligations, implemented support scope, and unmet stronger objectives. Cover at least:

- Opening/creation and ownership of an existing location; refusal behavior that does not adopt, overwrite, repair, or delete unrelated content.
- Acceptance visibility and the exact authoritative commit boundary. The accepted record, its artifact binding, lineage, and authority-owned recording provenance must agree.
- Duplicate ID behavior. Preserve the current rejection of repeated acceptance unless a separately justified and tested contract change deliberately introduces retry/idempotency semantics. Do not silently reinterpret duplicates as independent records.
- Retrieval: absent ID versus corrupt record versus missing artifact versus unsupported definition, and deterministic enumeration behavior where currently provided. Never substitute the current in-memory model or current interpretation for the accepted basis.
- Immutable accepted record bindings and distinct occurrence identity, including `F1 -> F2 -> F1`; artifact sharing must not merge revision occurrences.
- Concurrency and failure assumptions: supported process/access model, limits, interrupted initialization, failure before installation, and failure after installation but before the caller receives confirmation. Do not promise every exception means 'nothing committed'. Do not invent exactly-once semantics.
- Durability/recovery scope: distinguish ordinary reopen, a terminated process, an interrupted operation, OS crash/power loss, disk loss, and hostile modification. Claim only the supported and evidenced failure model. Atomic installation and forcing a file are implementation facts, not a complete proof of every durability property.
- Data ownership and compatibility, as described below. No silent weakening of retained semantic invariants to make the refactor easier.

Product status, physical persistence, exact semantic-definition resolution, cross-version compatibility, and externally supported historical reliance are separate dimensions. A first-class built-in storage capability can exist now while Factory and Engine semantics remain development definitions. Reconcile the old implication that all product storage must therefore be 'disposable proving' machinery. Equally, renaming the store must not manufacture unlimited retention, exact old-definition interpretability, permanent re-execution, release support, or a general stability promise.

State the initial supported scope explicitly, including the same-definition/build restriction if retained. Preserve the current conservative `definitionBinding` refusal until there is a deliberately established replacement. Never replace it with a human development label, infer semantic equivalence from equal build labels, or restore the removed placeholder Engine identifier. Keep storage representation/version identity, semantic content fingerprint, semantic-definition basis, and historical occurrence identity distinct.

When a definition mismatch makes a store unreadable by the current implementation, fail clearly without reinterpreting or deleting it. Explain that refusal preserves information but does not itself supply a compatible reader or exact-definition retention. Research must address stronger historical support where the current boundary cannot provide it. Do not turn future research into a reason to postpone the bounded source extraction and product reframe already decided here.

### Existing data and naming transition

'I do not want to delete anything' means preserve capabilities, concepts, useful acceptance evidence, and user-owned data. Moving implementation files and retiring superseded class/method names as part of the refactor is expected; removing the underlying capability is not.

Inventory every public reference and on-disk marker before renaming. The baseline contains `arcogine-proving-revision-store`, `arcogine-proving-revision`, `arcogine-proving-artifact`, and `proving-store` in private encodings/metadata. Do not globally replace those bytes and silently strand or reinterpret existing roots.

Prefer retaining readable existing encoding privately where that does not misstate custody; otherwise define and test an explicit non-destructive refusal or narrowly scoped, authorized transition. Never infer a stronger historical support commitment from accepting an old marker. Do not automatically adopt a legacy development store into a stronger custody declaration, migrate meanings, or delete/reset a root on open. Existing source API compatibility and existing-data compatibility must be discussed separately in the PR.

Current public names, documentation, examples, and diagnostics should describe product storage rather than a proving-only artifact. Historical decision records and deliberately retained legacy encoding fixtures may keep the old terminology, with a clear historical/compatibility role. Do not rewrite history to make the capability appear to have always had the new purpose.

## 4. Reconcile all affected canonical surfaces

This is semantic closure, not a new parallel documentation estate. At minimum inspect and update where materially affected:

- `docs/product/charter.md`: the owner-approved requirements-pressure principle.
- `docs/architecture/overview.md`: Storage's product role, implemented boundary, module graph, and separation of storage guarantees from semantic promotion.
- New `docs/architecture/storage.md`: the storage contract and initial supported realization.
- `docs/architecture/controlled-revisions.md` and `governance-conformance.md`: Governance retains semantic authority while Storage supplies persistent implementation. Do not promote every broader proposed Governance capability as implemented.
- `docs/architecture/governance-evidence.md`: preserve structural-evidence use of a real revision authority through the new public storage boundary; do not replace integration with fake evidence or claim durable evaluation/evidence persistence has appeared.
- `docs/architecture/external-representations.md`: physical storage is mechanism; neither storage encoding nor an external database defines semantic identity.
- `docs/development/semantic-contract-support.md` and relevant overview/Factory text: remove false coupling between product-owned storage and maturity of payload definitions, while preserving truthful refusal and scoped commitments.
- `docs/planning/governance-conformance-capability.md`, `governance-continuity.md`, and affected Factory/Operational dependencies: separate completed Governance semantics from the relocated implementation and its actual support scope. Do not automatically restart or freeze the Governance roadmap.
- `docs/product/concepts.md`, root README, `docs/README.md`, planning/research indexes, and any maintained references or examples actually affected.
- `AGENTS.md`, CONTRIBUTING layout guidance, build declarations, architecture checks, coverage, and change-classification behavior for the new module/track.

Keep class comments about current behavior and local invariants; roadmap state belongs in planning. Keep transient delivery labels out of durable filenames, code/test names, and architecture prose. Preserve meaningful historical rationale; an added historical decision record is optional, not a substitute for updating canonical authorities.

## 5. Put Storage in research and development planning

### Research portfolio

Add focused research briefs and register entries for material unanswered questions; do not reopen the owner's product decision. Read and reuse the existing exact-reference, scoped-commitment, and conversion/migration questions in `semantic-equivalence-and-reference-boundaries.md` rather than duplicate their authority.

Useful bounded questions are:

1. What failure model and minimum durability/recovery/concurrency guarantees should Arcogine Storage support beyond its initial evidenced local contract, and what changes to the existing implementation are required? Distinguish process termination from host/power failure, acknowledgment ambiguity, atomic publication, interrupted initialization, and corruption detection from repair/authenticity.
2. How can stored authoritative material remain truthfully resolvable as storage format and domain definitions evolve independently? Distinguish preservation of bytes, exact-definition availability, compatibility, semantics-preserving conversion, migration, and scoped historical reliance. Start from the existing build-bound refusal rather than assuming a registry or migration framework.

Separate performance/capacity, backup/restore, and possible alternate providers into later candidate questions only where a concrete decision and trigger exist. Do not write an omnibus storage encyclopedia or research whether 'WAL is better' without a failure-model requirement.

Each executable brief needs a bounded question, current/simple candidate, alternatives, discriminating cases, expected evidence, exit criteria, risks, and owning durable destination. Use `READY` only if independently executable; otherwise `CANDIDATE`. Research priority is not implementation admission. Do not mark unknowns `CONCLUDED` because a brief exists. High-risk research conclusions later promoted to architecture still require the repository's independent adversarial-review process. This implementation task writes the briefs and register reconciliation; it does not self-certify those investigations.

### Development planning

Create `docs/planning/storage-capability.md`. Register Storage under the existing `PLAN-<TRACK>-<LOCAL-ID>` discipline, using `STO` unless already allocated differently on the current target; reconcile actual track enumerations and checkers only where necessary.

The first admitted slice is this coherent owner-directed reconciliation: product purpose, the bounded current public contract, extraction of the retained built-in implementation, boundary tests and acceptance integration, documentation, and research/planning placement. Mark completion only to the extent actually implemented and validated; preserve future support gaps explicitly.

Do not copy an imagined sequence of WAL, replication, migrations, multiple backends, or universal evidence storage into the plan as admitted work. Stronger behavior whose meaning remains unsettled stays in research. A future implementation slice may be admitted only once its contract, prerequisites, and acceptance criteria are settled; planning may link to an actual blocker without selecting its unresolved answer.

Governance's next work may consume the extracted current contract without waiting for every Storage research item. Conversely, a Governance use requiring stronger retained custody cannot claim that guarantee merely because Storage is now first-class. Make dependencies requirement-specific, not a blanket gate on either track.

## 6. Source changes and acceptance evidence

Update Gradle settings, module dependencies, compilation/style/coverage wiring, tests, architecture checks, and affected tooling/docs as one change. The Storage implementation is production code and must receive the ordinary production quality gates; it is not a proof-only coverage exemption.

Keep Governance's main-source dependency boundary and domain-neutrality. Relocate persistence-coupled integration tests to Storage tests, or a small test-only integration module only if actually necessary, to avoid production cycles and duplicate fixtures. Pure Governance unit tests may retain narrow test doubles. Acceptance tests spanning Factory, Storage, and Governance must still exercise the real built-in implementation through public contracts.

Required evidence:

1. An external-package consumer can open the built-in provider, obtain contract-typed access, accept a real Factory artifact/revision, and resolve it without importing or casting to the concrete implementation.
2. Encapsulation is executable: implementation visibility and architecture/dependency tests prohibit ordinary consumer dependency on backend internals. A mere package name or documentation assertion is insufficient.
3. Preserve the existing revision-store coverage: authority-owned recording time; duplicate/rebound ID rejection; parent and self-parent constraints; rollback with equal content but distinct revisions; exact historical artifact recovery independent of current model; deterministic enumeration; defensive copying; malformed/unsupported/mismatched input; missing/corrupt metadata/artifacts; no silent repair from a newly supplied artifact; ownership/lock-file refusal; definition mismatch; and concurrent conflicting acceptance.
4. Add a real separate-JVM persist/reopen test for the initial process-boundary claim. Keep tests deterministic, bounded, with timeouts and cleanup confined to test-owned temporary locations. Add independent-process concurrency or controlled interruption tests where the public contract claims those behaviors. Do not call normal reopen a power-loss test.
5. Preserve the existing ChangeSet and pre-change conformance end-to-end path against a real accepted base revision and real Factory comparator, using the new public storage boundary. Preserve honest evidence provenance and existing fixture-versus-production distinctions.
6. Test the chosen legacy-root/naming transition, exact-definition mismatch, foreign-location refusal, and absence of implicit data deletion/adoption. A refused root must not be silently rewritten to make the new name fit.
7. Exercise failure/cleanup cases supporting the acceptance boundary. Distinguish pre-commit rejection from potentially committed-but-unacknowledged outcomes; do not erase committed history during cleanup. Where stronger crash guarantees remain unimplemented, documentation and research must say so instead of weakening existing invariants or claiming them passed.
8. Verify canonical Factory bytes/fingerprints, Engine runtime behavior, and unrelated Governance identity/equality contracts are unchanged by moving storage. Changes to the verifier's compiled build binding can still occur and must be handled truthfully, not disguised as changed model content.

Private encoding/lock tests may inspect private layout from implementation-owned tests; that must not turn the layout into a public API or make semantic acceptance depend on direct file edits by consumers. Do not add tests that only assert the charter contains a sentence or that a roadmap uses preferred wording.

## 7. Explicit non-goals

Do not delete/quarantine the Storage capability or reduce Governance to documentation. Do not claim this completes all Storage excellence, audit readiness, or production hardening. Do not implement evidence/evaluation persistence, authorization, exceptions, framework mappings, Operational actuation, an outward application, distributed storage, replication, encryption/signatures, a generic database/ORM, a provider plugin system, or a third-party backend solely for this extraction. Do not freeze Factory/Engine definitions, restore removed technical maturity labels, invent exact-definition provenance, or add a universal identity/attestation registry.

WAL, SQLite transactions, and atomic file installation were illustrative mechanisms in discussion, not selected architecture or algorithms. Preserve/improve the existing realization to satisfy the bounded contract; put genuinely undecided stronger mechanisms behind the appropriate research question.

## 8. Validation and delivery

Use current `docs/development/testing.md` and `AGENTS.md`. With `:storage` introduced, run targeted tests during development, for example:

```bash
cd product
./gradlew :storage:test :governance:test :factory:test :architecture-conformance-test:test
```

Include any integration module actually introduced. Then run the repository-wide product gate from the root:

```bash
./arcogine check
```

Because this changes module wiring and canonical documentation, run the applicable repository-tooling suites and real Markdown-link, delivery-label, transient-coordinate, and transient-workspace checks. The current canonical runner is:

```bash
bash .github/scripts/check-repository-tooling.sh
```

Remove the tracked handoff before the final workspace/static checks. Run `./arcogine check --full` when required by the change/current repository policy and available tooling; disclose unavailable checks accurately. Never weaken coverage, dependencies, scanners, architecture guards, or failure assertions merely to obtain green results. Use the supported JDK/environment workflow; do not hand-edit generated wrapper or lock artifacts.

Before handoff, perform bounded semantic closure over storage ownership/purpose, public/private contracts, custody/support scope, and roadmap status. Inspect old and new wording across maintained documents and source; account for meaningful historical/legacy exceptions. No tracked `workspace/` files or disguised temporary artifacts may remain in the candidate.

Commit with the repository owner's verified human identity and no agent/provider attribution trailers. Open one coherent implementation PR against `main` using `.github/pull_request_template.md`. Its body records stable summary/rationale, compatibility/migration impact, and non-goals only. Do not paste validation logs, a Validation section, current head/base coordinates, CI/review state, or branch topology into the body.

Follow the current implementation-owned exact-head CI convergence rule: resolve `CI / gate` for the final head, synchronously address implementation-owned failures and material base/conflict changes, rerun after head changes, and hand off truthfully. Do not manufacture or wait for your own independent reviewer disposition; never merge the PR. If a required tool or external blocker prevents completion, report the exact missing gate and retain the useful work rather than claim completion.

Final implementation report in chat: PR and branch; exact final head and checked main; resulting public storage API/module boundary; semantic authorities reconciled; preserved behaviors and compatibility decisions; research/planning entries and their actual states; validation commands/results and exact-head CI; remaining limits/blockers. State explicitly that the work preserves the capability, makes it first-class, and leaves only genuinely stronger unimplemented guarantees in research/planning.

The finished change must tell one consistent story: Governance is a legitimate driver of Arcogine's foundational requirements; Arcogine owns a built-in storage capability; Governance owns historical meaning and Storage supplies its persistent realization; consumers see contracts rather than backend mechanics; and the documented guarantees match the implementation and evidence actually delivered.
