# Implement the provisional semantic-contract reset and preserve the useful learning

## Assignment and delivery boundary

You are the implementation owner for one owner-directed, intentionally breaking architecture reset in `alaiba/arcogine`. Implement it; do not run another permission-to-reset investigation, produce report revision 3, or return a plan instead of making the change.

Continue on the existing branch `research/semantic-contract-promotion-boundary-review`. The final PR to `main` must contain all three outcomes together:

1. a truthful, durable account of the superseded research exercise and its useful learning;
2. the explicit owner-directed architecture decision and reconciled current contracts;
3. the actual code, tests, documentation and planning changes that implement that decision.

Multiple commits on this branch are appropriate. Separate research-closure, architecture-only and implementation PRs are not. The task is not complete while only the decision or documentation has changed.

## The owner decision is settled

The owner has repeatedly and explicitly stated that this early-stage project is free to change, rename, delete, replace or discard its existing semantic contracts and their development artifacts. The owner authorizes removing `factory-model:v1`, `factory-model:v2`, `engine-semantics:v1`, their old canonical bytes, fingerprints, fixtures, compatibility assumptions and dependent planning from the active system. Preserving them is not a requirement of this reset.

The selected direction is:

> Factory and Engine semantics remain explicitly WIP and correctable until an explicit owner-approved promotion or release establishes a real, scoped durable-use commitment. Deterministic fingerprinting during development does not itself promote or freeze a contract.

Use `factory-model:wip` and `engine-semantics:wip` as the active provisional markers. Give model types, packages, specification filenames and tests semantic names rather than ordinal-version names. The markers are mutable development labels, not immutable semantic identities spanning development revisions.

This decision supersedes the old preservation/non-reuse assumptions for the pre-reset Factory/Engine estate. Amend the owning current architecture accordingly. Do not make the task conditional on proving that old labels were never committed, that a particular historical interpretation wins, or that every hypothetical outside copy has been located. Do not retain legacy codecs, aliases, tombstones, publication modes or migration machinery merely to protect the old labels. Do not replace the old ladder with `v0`, `draft-1`, `draft-2`, or another mandatory sequence.

This is a new owner-directed decision, not promotion of the unaccepted reports' conclusions. Preserve their actual review outcomes; do not manufacture research acceptance. Normal implementation correctness, validation, independent PR review, branch protections and the owner's manual merge remain required. The authorization concerns this architecture reset, not a general exemption from safeguards or unrelated changes.

## Workspace and grounding

At handoff preparation, live `main` was `b9d6e8b0e3b10f07cbc6777e3bc3abbce2f45c19`; the workspace head before this prompt was `24694a0609f5f8763d879253db44aaf14d2a8e72`. These are starting evidence coordinates, not an instruction to reset a newer branch to either commit. Re-resolve live `main` and the workspace when starting. Preserve any intervening work.

Read `AGENTS.md`, `.github/CONTRIBUTING.md`, `docs/development/reviewing.md`, `docs/development/testing.md`, and the knowledge-transfer/retirement sections of `docs/development/researching.md`. Follow the attached snapshot's retrieval protocol when using a project snapshot. Use current repository evidence, distinguishing main from workspace content.

Read the research packet for bounded knowledge transfer, not to decide whether the reset is permitted:

| Artifact | Exact commit | Path |
| --- | --- | --- |
| Original handoff | `3b1d7928147e9426fd6440f02170fa2c1e6ea020` | `workspace/research/semantic-contract-promotion-boundary-review.md` |
| Original report | `8fa235b5621e3f97fd41c1aa327d6303e464c65d` | `workspace/research/investigations/semantic-contract-promotion-boundary.md` |
| First review, REOPEN | `9586901f9809a17c88ed0239700612eaa1a4c3d2` | `workspace/research/investigations/semantic-contract-promotion-boundary-adversarial-review.md` |
| Revised report | `e7eeedebf2828d467f31ab02a1ce71104ebc4712` | `workspace/research/investigations/semantic-contract-promotion-boundary.md` |
| Revised-report review, scoped REOPEN | `24694a0609f5f8763d879253db44aaf14d2a8e72` | `workspace/research/investigations/semantic-contract-promotion-boundary-revision-2-adversarial-review.md` |

Keep these commits reachable throughout implementation and PR review. Append commits; do not amend, rebase, force-push, or recreate the workspace. Incorporate newer main through the repository's history-preserving synchronization procedure when needed. The same branch must carry the research, reset and knowledge transfer.

Read and reconcile the following owners and their semantic neighbors:

- `docs/architecture/overview.md`, especially semantic evolution/support and determinism; `docs/development/semantic-contract-support.md`.
- `docs/architecture/factory-design.md`, `factory-model-v1.md`, `factory-model-v2.md`, `factory-resource-semantics.md`, `engine-semantics-v1.md`, `transfer-applicability.md`, `runtime-contract.md`, `controlled-revisions.md`, `governance-evidence.md` and `external-representations.md`.
- `docs/planning/factory-design-capability.md`, `factory-simulation-engine-readiness.md`, `spatial-runtime-consequences.md`, and affected Governance/Challenge plans.
- `docs/research/research-register.md` and the maturity/durability, Factory-composition and Engine-applicability investigation entries, plus relevant historical rationale.
- Factory model/publication/validation, fingerprint/artifact codecs, semantic comparison, runtime assembly and tests; `EngineSemanticsVersion`, `ModelFingerprint`, runtime metadata/events, evidence provenance, controlled-revision authorities, and their callers/tests.

Search `docs/` and executable source for `factory-model:v1`, `factory-model:v2`, `engine-semantics:v1`, `FactoryModelV2`, `FactoryModelArtifactV1`, `FactoryModelFingerprintV1`, `EngineSemanticsVersion`, `model.v2`, `fixed`, `immutable`, `released`, `attributed`, `promotion`, `cross-policy`, `continued V1 publication`, `WIP`, and `knowledge-transfer`. Expand to meaning-equivalent wording, not just literal suffixes. Inspect any current PR affecting these surfaces; do not import an unrelated open PR as a prerequisite. Reuse an existing PR for this workspace if one now exists, otherwise create one after implementation.

## Required architecture and implementation

### One evolving Factory model, not two renamed generations

Replace the active Factory V1/V2 split with one semantically named Factory model and one WIP canonicalization/publication path. Carry forward the useful current production records and optional spatial/handling record into that single shape. Do not keep two parallel models renamed to `Legacy` and `Wip`.

Preserve the substantive rules unless this reset directly requires changing them: one authoritative set of production records; explicit optional spatial presence; absence distinct from authored zero; complete coverage when spatial content is present; reference validity, bounds, non-overlap and overflow-safe validation. Do not invent absent values or strip authored facts.

Implement coherent WIP canonicalization, fingerprinting and artifact verification for the admitted unified shape, including optional spatial content. A valid spatial-present publication must not acquire a production-only fingerprint that silently drops the spatial record. Use one aggregate current-definition fingerprint. Regenerate byte/digest vectors for the new contract; byte-for-byte compatibility with the discarded policies is explicitly not an acceptance criterion.

Use semantic names for the surviving model, codec, validator, package and specification. An in-process published immutable model snapshot remains a useful concept: changing contract definitions between development revisions does not require making model instances mutable. Historical occurrence/lineage identity remains distinct from content equality; do not delete a useful domain revision concept merely because it contains the word `Version` or `Revision`.

### One WIP Engine interpretation with truthful applicability

Replace the active ordinal Engine identity and version-specific vocabulary with the WIP marker and semantic names. Update actual propagation sites, consumers, tests and explanatory contracts. Remove unconditional rules that every development-time semantic correction must mint another permanent identity.

Keep current deterministic execution behavior, ownership, workload, ordering, observations and runtime control unless a specific reset adaptation requires a change. A running session uses one fixed in-process interpretation and source-model snapshot; WIP does not permit either to change under an already-running session.

Model validity and Engine executability remain separate. The unified Factory grammar may represent facts the current Engine cannot execute. Refuse unsupported represented content before runtime mutation; never ignore spatial content, execute with invented defaults, or claim spatial execution is implemented just because the Factory shape and fingerprint now admit it. Preserve the meaningful absent-versus-authored-zero transfer distinction in the specifications. Do not implement the entire deferred transfer/scheduling system to finish this reset. Reframe any remaining applicability work by semantic content and actual supported behavior, not by the old V1/V2 identity partition.

### WIP means development-scoped determinism, not durable attribution

State and implement these boundaries coherently:

- For the same current definition and explicit inputs, canonical bytes, fingerprints and supported simulation outcomes remain deterministic.
- Between development revisions, the WIP grammar, bytes, fingerprints, validation and Engine interpretation may change without a new policy version.
- A bare WIP marker is not exact cross-revision provenance. Equality of WIP strings or fingerprints is not a cross-revision compatibility or historical-interpretation guarantee.
- Tests, vectors, in-process model publication, implementation landing, an internal file write, or a normative description of current behavior do not automatically promote the contract.
- No currently unpromoted WIP artifact or result may be represented as a committed durable compatibility record.

Keep useful persistence/reopen proving tests, but make their development-only role explicit. Inspect the existing filesystem revision authority and evidence-acceptance seams rather than relying on a documentation disclaimer alone. Separate disposable proving use from any claimed durable admission with the smallest coherent boundary. Reject WIP at a retained-commitment boundary, or make that boundary explicitly unavailable while all contracts are WIP. Do not silently disable all useful model/revision experiments, and do not introduce a generic registry or promotion framework to simulate a future need.

Old generated development stores may be discarded/reset; there is no old-policy migration obligation. Do not delete arbitrary files on contributor machines. Make stale or unsupported input fail explicitly rather than quietly reinterpreting it as current WIP. Proving stores may require a reset after a definition change; they need not have cross-build readers. Keep any genuinely needed diagnostic build/source context separate from semantic naming, without recreating a permanent policy series for every experiment.

### Explicit future promotion, not an accidental freeze

Reconcile the owning architecture and support guidance so a future promotion is an explicit owner-approved act tied to a concrete durable-use need. It identifies the exact frozen definition, meaningful durable identity, supported uses/consumers, retained basis and actual support obligations. Promotion is not performed by this PR. Factory and Engine need not promote together.

Only the deliberately promoted contract receives the appropriate non-rebinding and scoped compatibility obligations. Preserve sound distinctions between definition, retention, decoding, execution and interoperability where useful; remove their use as an automatic freeze on WIP development. Do not grandfather the discarded pre-reset labels through a special non-reuse exception. Future real misuse or unauthorized retained admission should be diagnosed and handled explicitly, not silently rewritten or used to auto-promote every development contract.

The future promotion mechanism should be a small documented boundary with only the executable checks currently necessary. Do not build a lifecycle platform, global policy registry, migration engine, or new ordinal namespace.

## Durable learning and research closure are implementation deliverables

Do this during the reset, not in a follow-up PR. The owner has stopped the investigation of whether old labels may be erased. Do not seek another research ACCEPT to implement the decision. Do not claim the research proved the selected WIP architecture or that either REOPEN review became an acceptance.

Create a concise dated historical rationale under `docs/history/decisions/`, using the reconciliation date and a semantic slug such as `provisional-semantic-contract-reset`. This case meets the retention test: without an explicit account, another agent could repeat the same lengthy permission debate. The record must cover:

- the original objective: meaningful terminology and freedom to evolve before genuine promotion;
- the investigation's drift into historical permission/fixation interpretation;
- the two reports and REOPEN outcomes, accurately scoped;
- the owner's explicit decision to replace the pre-release estate rather than settle that historical argument;
- the rejected preservation/migration/version-ladder alternatives as choices not adopted, not universally impossible designs;
- useful findings, accepted trade-offs and concrete conditions that would justify future promotion;
- links to the current owning contracts, relevant proving tests/process guidance and this PR as delivery provenance once available.

The historical record is not another normative authority. Current architecture must explain the new rules without requiring history or research archaeology. Do not falsify earlier dated rationale or edit earlier review dispositions to make them agree with the reset; a new record explains supersession.

Perform a bounded knowledge-transfer audit of the whole packet. For each material result, select a durable destination, mark it already encoded, retain it as a concrete unresolved issue only if still material, or explicitly discard it. Put a compact learning/disposition table in the reset rationale or another existing appropriate owner; do not create a parallel register. Merely retaining temporary SHAs in a PR is not preservation.

Assess these candidate learnings, retaining only what the evidence actually supports:

| Candidate learning | Likely durable destination |
| --- | --- |
| An explicit owner-directed change of architecture is a decision input, not a hypothesis that must be proven compatible with the very rules being replaced. Separate that route from promotion of a research conclusion. | Narrow clarification in existing contributor/research/review guidance; preserve ordinary independent PR review. |
| Deterministic canonicalization and in-process Factory publication do not themselves establish a durable-use promise. | Current Factory/WIP/support contract and behavioral tests. |
| Names should express semantics; maturity and support are explicit properties, not inferred from `v1`, an implementation milestone or a golden vector. | Existing naming/agent guidance and semantically named source/specifications. |
| Audit the concrete referent behind claims such as attributed, published or retained; distinguish a capability, an actual accepted use, a promise, evidence of fulfilment and a preference about cost. | Support/review guidance with a small concrete example. |
| Scope negative evidence; lack of a discovered record is not universal proof of absence. A test path alone is not a universal custody rule. | Existing research evidence guidance, only if the current wording needs improvement. This must not restart the settled pre-reset preservation audit. |
| When compressing architecture or removing historical narration, preserve the real condition and rationale of a rule rather than substituting a convenient but false factual premise. | Existing documentation/review guidance and this historical rationale. |
| Policy-relative identity, semantic comparison and support costs are different questions; a convenient preferred design is not a theorem invalidating all alternatives. | Existing research guidance, preserving the distinction without retaining the old policy estate. |
| Useful domain rules survive bad lifecycle assumptions: explicit absence versus zero, model versus interpretation, and content identity versus occurrence identity. | Current semantic contracts and executable tests rather than duplicated prose. |

Do not preserve obsolete permission arguments, entire external-literature summaries, R1/R2/R3 taxonomies, or historical fixed-label obligations as new current architecture. Do not manufacture a learning quota, a general theory or an extra research project. A synthesis seed is optional and must meet the existing admission test; prefer the actual reusable rule/test in its owner.

Update the research register and relevant briefs to show the promotion-boundary permission investigation as `SUPERSEDED` by the explicit reset decision, with durable links. Reconcile prior maturity/composition/applicability entries by scope: replace obsolete lifecycle/preservation conclusions, preserve independently useful composition findings, and retain genuine open behavior questions without old-version framing. Do not label abandoned research successful, mark unfinished capabilities implemented, or claim branch-only changes have landed.

If any old process wording would misclassify this owner-directed implementation as research promotion, clarify that distinction narrowly in this same PR. Do not weaken independent review of research conclusions that really are being promoted, invent a new review disposition, or give an implementation author self-approval.

## Cross-repository semantic closure

Remove the old ladder from active code, paths, imports, constants, codecs, validators, runtime guards, tests, architecture, product explanations, planning, reference material and research-status claims reached by the change. Reconcile Governance fingerprint/revision/evidence usage and Challenge consumers; do not leave a downstream comparator or provenance field assuming the old durable identity.

Rename the old Factory/Engine specification files to semantic filenames and update inbound links. Eliminate the V1-versus-V2-absent identity split, dual-publication question, frozen-V1 preservation gates and historical-transition implementation work that exists only for the discarded estate. Preserve real unresolved behavior questions and legitimate domain boundaries.

Old tokens may appear in narrowly justified historical narrative or tests that explicitly reject obsolete input. They must not remain operative compatibility contracts or accepted aliases. Do not indiscriminately remove unrelated dependency versions, protocol versions, quantities, document dates or legitimate domain revisions.

Do not solve unrelated research, add outward interfaces, rewrite the scheduler, or widen into a general schema/composition framework. A concrete difficulty implementing the chosen shape is an engineering problem to solve or report precisely, not a reason to reopen whether the owner is allowed to reset it.

## Acceptance evidence

The final PR must establish all of the following:

1. **One active model and interpretation.** Factory and Engine use WIP markers; types/packages/specifications describe semantics, with no active V1/V2 parallel model, legacy decoder or automatic successor ladder.
2. **Correct canonicalization.** Current-definition repeated/equivalent construction, deterministic encoding, full authored-field coverage, round-trip verification, malformed-input rejection and meaningful ordering/normalization cases are tested. Spatial absence differs from present legal zero. Update old golden vectors deliberately; preserve substantive coverage, not discarded byte values.
3. **Executable coherence.** Published model immutability, current headless production behavior, deterministic ordering and supported observations remain correct. Unsupported represented content is refused before mutation. No new claim of implemented spatial execution or provenance support exceeds the code/tests.
4. **No accidental durability.** Tests exercise the chosen proving-versus-committed admission boundary. WIP does not enter a claimed durable authority or acquire promotion merely by being persisted in a proving test. Unsupported/stale/old-policy input is not silently accepted as current meaning.
5. **Knowledge survives.** The durable reset rationale, actual reusable learnings, explicit discards and scoped research-status changes are in the same diff as the code. Current architecture stands without reading the temporary reports. Research outcomes and the owner decision remain distinguishable.
6. **Full reconciliation.** A semantic-neighbor sweep finds no contradictory active old-policy obligations, broken links or bogus implemented/readiness claims. Remaining behavior work is stated without the removed policy partition.
7. **One reviewable PR.** The existing workspace history is preserved through review, the final net tree contains no tracked `workspace/` files, and ordinary independent PR review is still required.

Run the repository-owned Java gates because this changes Java across modules: `./arcogine check` (or the exact equivalent specified in the current testing guide). Use focused model, runtime, Governance and Challenge tests while developing; do not substitute them for the required full Java gate. Run relevant repository-tooling checks for Markdown links, delivery labels, transient coordinates and tracked workspace content; use the current scripts/helper named in `docs/development/testing.md`. Run `git diff --check`. Add targeted Node/tooling tests only where tooling changes require them. Do not invent brittle tests that merely assert prose contains a preferred sentence.

Record commands, actual outcomes and unavailable validation precisely. Do not claim absent, pending or unrun CI passed. Keep unrelated security/tooling installation out of the critical path unless the changed surface requires it.

## Final-tree cleanup and PR handoff

After the knowledge-transfer audit and scoped research supersession are encoded, delete temporary research reports/reviews/handoffs and this implementation prompt from the branch's final tree with ordinary new commits. Their exact earlier revisions must remain reachable while the PR is being reviewed. Removing them from the tree is not permission to rewrite history or retire the branch before the reconciliation lands.

The final PR must retain a useful account of the research failure, the decision and its learning in durable files; a chain of soon-to-be-deleted workspace links alone does not satisfy the owner request. Full raw reports need not be archived. If an exact research artifact independently merits permanent readability, deliberately retain that specific artifact in an appropriate durable location with an honest non-normative status; do not dump the workspace under `docs/`.

Open or update one PR from this same branch to `main`, describing the owner-authorized reset, intentionally broken compatibility, substantive implementation, research supersession, durable-learning destinations, acceptance evidence and non-goals. Keep volatile head/base distances, mergeability and CI lifecycle state out of the maintained PR description, per contributor policy; report current state separately. Do not describe the work as adoption of an accepted report.

Do not self-review, emit a reviewer-authored merge authorization, merge, or launch autonomous follow-up work. Hand the candidate to the independent PR Reviewer and stop according to the implementation continuation contract.

In the final implementation report, identify the PR, summarize the actual reset and removed obligations, point to durable knowledge destinations, state validation and any remaining implementation-owned blockers, confirm final-tree workspace cleanup and preservation of the evidence commits, and distinguish remaining substantive behavior work from the discarded version-preservation work. Completion means the reset and learning transfer are both implemented in the same PR, not another proposed research cycle.
