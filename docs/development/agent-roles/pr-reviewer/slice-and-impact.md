# Slice, artifact lifecycle, semantic impact and depth

Part of the [PR Reviewer contract](../pr-reviewer.md#review-procedure); every complete review, re-review and `./` continuation follows all of its routed topics.

## Reconstruct the intended slice

Before judging implementation, identify:

- goal;
- acceptance criteria;
- explicit non-goals;
- prerequisite work;
- compatibility expectations;
- research or issue status affected, if any.

A handoff prompt or implementation explanation is useful evidence of intent but is never authority over the live repository.

### Artifact-lifecycle pass

For every newly added artifact, determine its owning authority and intended post-merge lifetime, whether it is durable state or delivery/research/session scaffolding, whether its containing directory has admission/maintenance/retirement rules, whether its durable meaning is already captured elsewhere, and whether retention would create a stale duplicate, archive dump, frozen prompt, or historical note with no active downstream role. Any tracked `workspace/` file is an immediate merge-blocking `CHANGES REQUIRED` finding, independent of its name or contents. Temporary material accidentally placed outside `workspace/` is also a review defect when its post-merge lifetime is unjustified. Handoff prompts and implementation explanations are intent evidence only and do not override live repository state.

Do not convert an explicit non-goal into a review requirement unless the PR cannot satisfy its actual contract without it.

## Classify semantic impact

Classify material changes before deciding review depth. Use any applicable categories:

- behavioral/runtime semantics;
- public API, DTO, event, wire, CLI, or consumer contract;
- domain model or ownership;
- scheduler, time, determinism, or ordering;
- canonical model, identity, revision, lineage, provenance, or persistence;
- architecture/module dependency boundary;
- challenge/game semantics;
- governance/conformance semantics;
- operational/digital-twin semantics;
- research or issue status;
- current-state architecture/documentation;
- toolchain, build, dependency, CI, or packaging;
- tests/evidence only;
- local mechanical/refactoring change.

Use classification to drive semantic-neighbor inspection, not to force irrelevant review work.

## Risk-proportionate depth

Use semantic risk to control review breadth. PR-local findings do not carry severity; every open finding is pre-merge work.

- **Low:** isolated refactors, narrow tests, typo/link corrections, mechanical changes with no contract effect. Inspect direct code/docs, tests/checks, and immediate contracts.
- **Medium:** domain behavior, research or issue status, maintained current-state docs, internal interfaces with meaningful consumers. Inspect architecture and research state, semantic neighbors, compatibility, and executable evidence; perform concept fan-out for changed semantics.
- **High:** durable identity/canonicalization, revision semantics, persistence contracts, scheduler/time authority, determinism, major ownership changes, public compatibility/event contracts, security/authority, or hard-to-reverse architecture. Inspect the applicable architecture and specifications, planning, integration/compatibility evidence, consumers, and relevant prerequisite/recent PRs; perform concept fan-out and authority-transition propagation where applicable.
