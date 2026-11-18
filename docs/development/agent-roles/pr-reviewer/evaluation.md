# Evaluation, invariants and false-positive guards

Part of the [PR Reviewer contract](../pr-reviewer.md#review-procedure); every complete review, re-review and `./` continuation follows all of its routed topics.

## Required evaluation

Apply the review dimensions in `docs/development/reviewing.md`, including functional correctness, determinism, events/state/observations/ownership, domain boundaries, production semantics, canonical model/provenance, compatibility, scope discipline, documentation accuracy, architectural reconciliation discipline, tests as design evidence, CI truthfulness, and durable knowledge.

Additionally verify:

- **Acceptance-criterion truth:** identify evidence that actually proves each material completion claim.
- **Forward consistency:** determine whether the proposed change would introduce contradictions across affected current docs, architecture, research state, specification constraints, reference contracts, product concepts, examples, tests, configuration, or consumers. Use semantic concept fan-out for medium/high-risk changes rather than relying only on the changed-file list. This is bounded PR-impact review, not a substitute for the repository-wide Consistency agent.
- **Authority/status propagation:** when a decision or capability changes state, determine whether maintained surfaces that describe that state have been reconciled.
- **PR-description truthfulness:** after fixes, verify the stable title/body claims about semantic scope/rationale, compatibility or migration behavior, non-goals, API names, and completion meaning describe the current head. Transient lifecycle or validation text is not review authority and is never a finding by itself.

## High-value Arcogine invariants

Confirm exact details against current architecture and the owning specifications before filing a finding.

- Keep events/requested occurrences, mutable authoritative state, and read-only observations/projections distinct. DTOs are transport projections, not domain truth.
- Keep scenario/run configuration, consumer-owned draft representation, canonical designed production semantics, immutable/published model-version boundaries, and mutable runtime execution state distinct.
- Keep durable semantic fingerprint/content identity, controlled historical revision identity/lineage, and authorization/change-workflow identity distinct unless a binding decision explicitly relates them.
- Keep resource eligibility, runtime availability/status, and queue/pending-work state distinct unless a current contract defines their relationship.
- Keep requested work, accepted immutable order intent, and mutable job execution/progress distinct.
- Engine owns production simulation truth; Challenge owns candidate admissibility and success/scoring/explanation from authoritative outcomes.
- Governance/Conformance and Challenge may reveal analogous patterns; similarity alone is not evidence for shared generic frameworks or domain-type unification.
- Simulation runtime is not production control runtime; operational authority, external command lifecycle, deployment/effective-artifact provenance, independent observations, reconciliation, and drift/calibration remain distinct concerns.
- Governance owns identity policy, controlled revisions, change attribution, requirements/assertions, conformance/findings, evidence use, governed change, and exceptions; operational execution owns execution context, authority, command lifecycle, deployment application, raw observations, and reconciliation.
- Do not accept a standards family label alone as proof of a normative requirement or conformance claim.
- Do not demand numerical equality among compatibility floors, preferred development environments, runtime images, CI floors, and pinned versions when repository policy assigns them different roles.

## False-positive guards

Do not report a finding solely because:

- a later roadmap gate or future capability is not implemented;
- architecture or specification text the PR neither touches nor makes stale already differs from current implementation; that drift belongs to Consistency review (`docs/development/reviewing.md`);
- an abstraction could theoretically be more generic;
- Challenge and Governance contain similar concepts without sharing types/frameworks;
- intentionally documented compatibility debt remains;
- Git history, rather than current documentation, preserves historical terminology, paths, or migration context;
- a test could be more exhaustive when existing evidence already proves the required invariant;
- unrelated code could be cleaner;
- an internal symbol is not publicly documented;
- a documentation-only PR does not add runtime tests when executable behavior is unchanged;
- a sibling capability uses synthetic fixtures while production integration is explicitly deferred;
- the product charter describes mature direction rather than today's implementation;
- two artifacts use different wording while preserving the same semantics;
- exact tool versions differ across surfaces that intentionally express different compatibility roles;
- a PR body contains stale local-validation or live Git/GitHub lifecycle facts; resolve those facts live and, if useful, mention the stale transient prose only in the review session's chat output rather than creating a finding.

Do not pull future work into the current PR without a concrete dependency on satisfying the current slice.
