# Handoff: retire ADRs and consolidate canonical architecture in PR #370

You are continuing the existing Arcogine architecture-reconciliation work on the same branch and **the same open PR #370**.

This is an implementation/reconciliation task. It is not a new research investigation, not a new branch, and not an independent PR review.

## Objective

Complete Arcogine's architectural baseline reset by **retiring the Architecture Decision Record (ADR) / decision-record mechanism entirely** and consolidating all current architectural truth into canonical architecture/specification documents.

The target authority model is:

```text
Product Charter
    ↓
canonical architecture + normative specifications
    ↓
code + executable tests

Research → informs changes to canonical architecture
Planning → sequences implementation against canonical architecture

Git / commits / pull requests → preserve historical decision context and rationale
```

There must be **no separate ADR/current-decision authority layer**.

A contributor should be able to understand Arcogine's current architecture without reading any historical decision log.

## Exact current state

Repository:

`alaiba/arcogine`

Branch:

`research/semantic-evolution-ratification`

Open PR:

- PR #370
- title at handoff creation: `docs(architecture): reconstruct durable decision baseline`
- base: `main`
- base SHA at handoff creation: `f4122b5c9fb46847ab8136d24073cbcffb4f8232`
- head SHA at handoff creation: `8660d28b5e2ef6b4a06fb8e3377da31ed4bd8101`
- mergeable at handoff creation
- no submitted PR reviews / review threads at handoff creation

Re-resolve all mutable state before editing. If PR #370 no longer points to this branch or has acquired material review findings, stop and report rather than assuming the handoff is still current.

The branch already contains valuable extraction work from the former ADR corpus into canonical architecture/specification documents, including:

- `docs/architecture/factory-model-v1.md`
- `docs/architecture/controlled-revisions.md`
- `docs/architecture/runtime-contract.md`
- `docs/architecture/governance-evidence.md`
- `docs/architecture/operational-continuity.md`
- `docs/architecture/external-representations.md`
- reconciled Factory / Engine / Governance / Operational architecture

Preserve that direction unless inspection finds a correctness problem.

The remaining awkward layer is:

- `docs/architecture/decisions/README.md`
- `docs/architecture/decisions/template.md`
- `docs/architecture/decisions/semantic-identity-and-evolution.md`
- `docs/architecture/decisions/deterministic-simulation.md`

PR #370 currently treats these as a special **current durable decision set**. That model is being abandoned.

## Owner decision

Arcogine will not maintain an ADR log or any other separate architecture-decision-record mechanism at this stage.

This is not a permanent prohibition on ever adopting ADRs in the future. It is a statement that Arcogine currently has no demonstrated need for a separate decision-log authority.

Current architecture belongs in canonical architecture/specification documents.

Historical architectural reasoning belongs in Git history, commits, pull requests, and retained research history where applicable.

Do **not** replace ADRs with a differently named equivalent such as:

- Architecture Decisions
- Decision Documents
- Decision Notes
- Architectural Rationale Records
- Current Decisions
- Design Decision Log
- RFC archive

Do not create another parallel authority layer.

## Important terminology boundary

Retire the **ADR / architecture-decision-record mechanism**, not the ordinary concept of a decision.

Arcogine legitimately uses `Decision` as domain language, for example:

`Observation -> Decision -> Event`

and in actor / decision-source / attribution semantics.

Do not mechanically remove ordinary uses of "decision", "decide", "decision source", "product decision", or equivalent domain language.

The cleanup target is specifically repository-governance/documentation concepts such as:

- `ADR`
- `ADR-NNNN`
- `Architecture Decision Record`
- `architecture/decisions/`
- `decision record` when it means the ADR mechanism
- `ADR admission test`
- `Accepted ADR` / `Superseded ADR`
- `promote to an ADR`
- `architecture/ADR`
- `record this in an ADR`
- reviewer/planner/consistency rules treating ADRs as an authority source

Use semantic judgment rather than global text substitution.

## Mandatory start-of-run grounding

Before editing:

1. resolve current `main`, branch head, and PR #370 state;
2. read `AGENTS.md`;
3. read the branch-head `docs/README.md`;
4. read `docs/architecture/overview.md`;
5. read every branch-head file under `docs/architecture/decisions/`;
6. read the current canonical architecture/specification documents touched by PR #370;
7. inspect `.github/CONTRIBUTING.md`;
8. inspect:
   - `.github/agents/pr-reviewer.agent.md`
   - `.github/agents/work-planner.agent.md`
   - `.github/agents/consistency.agent.md`
9. inspect:
   - `docs/development/reviewing.md`
   - `docs/development/researching.md`
   - `docs/development/consistency-review.md`
   - `docs/research/report-template.md`
10. search the complete tracked branch tree for the ADR-mechanism vocabulary listed above;
11. search code/tests/comments for old `ADR-NNNN` references;
12. identify every current file that treats ADRs/decision records as normative or required authority;
13. record any material surface you cannot inspect.

Do a quick docs search for these concepts specifically:

- ADR
- Architecture Decision Record
- decision record
- architecture/decisions
- architectural rationale
- canonical architecture
- normative architecture
- semantic identity
- deterministic simulation
- research promotion
- planning authority

## Canonical authority model

After this change, repository guidance should consistently express:

### Product Charter

`docs/product/charter.md` owns enduring product direction and principles.

### Canonical architecture and specifications

`docs/architecture/` owns Arcogine's current architecture and normative semantic contracts.

The Architecture Overview owns cross-cutting principles and domain boundaries.

Focused architecture/specification documents own exact domain contracts, identities, semantics, algorithms, supported boundaries, and other detailed normative material.

These documents must be sufficient to answer:

> What is Arcogine's architecture now?

without consulting Git history or research artifacts.

### Code and executable tests

Code implements the canonical architecture.

Tests/guards prove mechanically checkable invariants.

Where architecture and implementation disagree, that is a consistency problem to resolve; a historical record does not override current canonical architecture.

### Research

Research discovers and validates possible changes.

A research conclusion becomes architecture only when reconciled into the canonical architecture/specification that owns the affected semantics.

Do not say "promoted into an ADR".

### Planning

Planning sequences admitted implementation work against current canonical architecture.

Planning must cite the canonical architecture/specification that defines a prerequisite or invariant, not a historical ADR.

### Git / pull requests

Git and PR history preserve:

- earlier architectural states;
- historical rationale;
- alternatives considered during a change;
- why a canonical document changed;
- unusual transition context.

They are historical evidence, not current normative architecture.

## Remove the ADR layer completely

Delete the entire maintained directory:

`docs/architecture/decisions/`

including:

- `README.md`
- `template.md`
- `semantic-identity-and-evolution.md`
- `deterministic-simulation.md`

Do not leave a tombstone README explaining that ADRs were retired.

Do not add an ADR-history index elsewhere.

Git already preserves the deleted material.

The final tracked tree should contain no `docs/architecture/decisions/` directory.

## Consolidate semantic identity and evolution

The surviving architectural content in `decisions/semantic-identity-and-evolution.md` is valuable, but it must move into the canonical authority that owns it.

Reconcile it rather than copy-pasting a duplicate essay.

Expected placement:

### Architecture Overview

The cross-domain architectural principles should live compactly in `docs/architecture/overview.md`, especially the existing **Semantic evolution and support** area.

It should be sufficient to establish at the cross-domain level that:

- semantic identity is non-rebinding;
- materially changed semantics require distinguishable identity;
- retained/accepted attribution fixes the relevant semantic definition;
- historical meaning and continuing support are different concerns;
- retained attribution requires the defining semantics to remain resolvable for the required lifetime;
- decoding, execution, migration, interoperability and other support dimensions are explicit/scoped rather than implied by identity;
- withdrawing support does not allow rebinding historical identity;
- Arcogine has no universal `proving/promoted` maturity lifecycle;
- Arcogine has no universal exercised-section-freeze rule.

Keep this at architectural-principle level.

### Owning domain specifications

Exact equality rules, fixed aspects, version/identity labels, retention basis, compatibility behavior, failure behavior, and supported semantics belong in their owning Factory / Engine / Governance / Operational documents.

Do not duplicate them all in the Overview.

### Development policy

`docs/development/semantic-contract-support.md` may own contributor/review mechanics for declaring and evidencing support.

It must not become a second architecture authority.

## Consolidate deterministic simulation

The surviving architectural content in `decisions/deterministic-simulation.md` must likewise be absorbed into canonical architecture.

Expected placement:

### Architecture Overview

The enduring principle and scope of deterministic simulation belongs in the existing determinism material in `docs/architecture/overview.md`.

The Overview should make clear that:

- simulation/replay/verification determinism is architectural;
- outcomes depend only on explicit inputs plus an identified Engine interpretation;
- result-affecting ambiguity cannot remain ambient;
- Factory-authored facts and Engine interpretation have distinct ownership;
- real-world Operational execution is not claimed to be deterministic.

### Engine specification

Exact normative behavior belongs in `docs/architecture/engine-semantics-v1.md` and any future Engine semantic specification:

- current interpretation identity;
- result-affecting rules;
- tie-breaking;
- dispatch/decomposition/scheduling semantics;
- explicit inputs;
- conformance fixtures;
- refusal of unsupported interpretations;
- attribution/provenance requirements where Engine owns them.

### Runtime/provenance contracts

Where deterministic attribution interacts with outward runtime facts, place the exact contract in `runtime-contract.md` or the actual owning specification.

Do not leave a second prose definition elsewhere merely because the deleted decision record had one.

## Preserve one source of truth

For every rule moved out of the two decision files, decide its single normative owner.

Do not produce the same detailed semantic rule in:

- Overview;
- Engine spec;
- Factory spec;
- support policy;
- research brief;
- planning;

unless one is clearly a concise summary linking to the actual owner.

The goal is not to maximize prose retention.

The goal is to make authority obvious.

## Sweep repository governance

Remove the ADR concept from repository process/guidance.

At minimum reconcile:

- `AGENTS.md`
- `.github/CONTRIBUTING.md`
- `.github/agents/pr-reviewer.agent.md`
- `.github/agents/work-planner.agent.md`
- `.github/agents/consistency.agent.md`
- `docs/development/reviewing.md`
- `docs/development/researching.md`
- `docs/development/consistency-review.md`
- `docs/research/report-template.md`
- `docs/planning/README.md`
- `docs/README.md`
- Product Charter wording if it currently treats ADRs as part of authority

Replace ADR-specific rules with the simpler principle:

> Significant architectural changes must be reconciled into the canonical architecture/specification that owns the affected semantics, with code/tests and dependent planning updated consistently. Git/PR history preserves what changed and why.

Do not invent a replacement "hard-to-reverse decision document" requirement.

A hard-to-reverse architectural change still deserves deeper research/review where risk requires it, but the durable result is the changed canonical architecture.

## Reviewer authority map

The repository's reviewer/consistency/planner authority tables should no longer contain a row like:

`Why does a significant architectural constraint exist? -> ADRs`

Instead, use a model like:

- product direction → Product Charter;
- current architecture / semantic constraints → canonical architecture/specification;
- implementation truth → code/tests, reconciled with architecture;
- research evidence/state → research surfaces;
- planned work → planning;
- historical rationale for a change → Git/PR history when actually needed.

Do not make historical rationale a mandatory input for understanding current architecture.

## Research reconciliation

Update maintained research surfaces that currently point at ADRs or decision files as their durable destination.

For concluded research:

- the durable destination should be the canonical architecture/specification section that now owns the conclusion;
- the research artifact may remain as provenance/evidence according to the repository's normal research lifecycle;
- do not create a special decision-record destination.

For open research:

- expected destinations should say architecture/specification, process policy, planning, implementation, or no change as appropriate;
- remove formulaic "architecture/ADR" language.

The semantic-contract maturity/evolution investigation remains concluded if its reviewed conclusions are fully represented in canonical architecture and owning contracts.

Do not reopen research solely because the ADR mechanism is being removed.

## Planning reconciliation

Planning should depend on semantic/current authorities, for example:

- `runtime-contract.md`
- `factory-design.md`
- `engine-semantics-v1.md`
- `governance-conformance.md`

not on ADR identifiers or decision-record filenames.

Do not reprioritize work or pull deferred implementation into scope.

## Source and test comments

PR #370 already replaces many `ADR-NNNN` source/test comments.

Complete that sweep.

A source comment should either:

- state the invariant directly;
- cite the current owning architecture/specification when useful;
- or say nothing if the comment adds no value.

Do not replace `ADR-0014` with a vague phrase like "the architecture decision". Point to the actual current contract.

No behavior change is intended by this comment/reference cleanup.

## Do not erase useful rationale from canonical architecture

Retiring ADRs does **not** mean canonical docs should become unexplained rule lists.

When rationale is necessary to understand a constraint, prevent misuse, or explain a non-obvious trade-off, keep concise rationale next to the canonical rule.

What should disappear is **chronological decision-log narration**, not useful explanatory context.

For example, canonical architecture may say why semantic identity cannot rebind.

It should not narrate which old ADR first introduced the rule, what it superseded, or which PR debated it.

## Do not recreate an archive elsewhere

Do not:

- create `docs/architecture/history/`;
- create `docs/architecture/rationale/`;
- create a decision index;
- move deleted ADRs into `docs/archive/`;
- add tombstone files;
- preserve old ADR prose as "historical notes";
- add a "former ADR mapping" document to `main`.

The PR description and Git history are enough for this one-time consolidation.

## PR #370 itself

Continue using PR #370.

Do **not** close it and open another PR.

After the architecture consolidation is complete, update PR #370's title and body to describe the actual result.

A suitable title shape is:

`docs(architecture): consolidate canonical architecture`

or another concise title that reflects the final diff.

The PR body should explain the one-time cleanup:

- canonical architecture/specifications are now the sole architectural authority;
- the experimental ADR corpus and ADR governance mechanism were retired;
- useful current semantics were moved into canonical owners;
- Git/PR history preserves historical rationale;
- no product behavior was intentionally changed;
- open research remains open.

This explanation belongs in the PR, not in canonical architecture.

## Existing research evidence custody

Preserve the already-reviewed semantic-evolution evidence in branch history:

- report commit: `c6e67cb5022acae482422336da83a1d5ac80a15c`
- adversarial review commit: `5118a0851b11f88002ad526d77b9a28a95d85307`

Do not rewrite history to remove them.

Do not expose those coordinates as required current architecture.

## Product/code scope

This remains primarily documentation/governance consolidation.

Do not remove or redesign:

- Factory V1/V2 implementation;
- Engine runtime behavior;
- legacy `contentHash()`;
- runtime readers/adapters;
- Governance implementation;
- Operational implementation boundaries;
- API behavior.

Code/test edits should remain reference/comment-only unless a genuine inconsistency uncovered by the documentation consolidation requires a separately justified correction.

If behavior change becomes necessary, stop and report why rather than smuggling it into this cleanup.

## Validation

At minimum validate:

1. final tracked tree contains no `docs/architecture/decisions/`;
2. no `ADR-NNNN` references remain in tracked current content;
3. no `Architecture Decision Record` / ADR-governance references remain except where a historical research artifact genuinely discusses the historical mechanism and retaining that historical wording is necessary;
4. no current guidance requires creating, updating, promoting to, superseding, or reviewing an ADR;
5. no current planning item depends on an ADR as normative authority;
6. no current source/test comment cites an ADR;
7. Markdown links pass;
8. delivery-label checks pass;
9. transient-workspace checks pass;
10. changed helper/checker tests pass;
11. `git diff --check`;
12. repository-prescribed documentation/process validation required by current `AGENTS.md`.

Be careful with research artifacts: they may legitimately describe historical repository state. Do not falsify historical research just to remove the token `ADR`. The required sweep is for **current normative references and active process assumptions**. Historical prose may remain when changing it would corrupt the record, but maintained summaries/register rows should point to current canonical destinations.

## Transient workspace cleanup

This handoff prompt is temporary.

Before handing PR #370 to independent review:

- remove this prompt and all other tracked `workspace/` material;
- run `.github/scripts/check-transient-workspace.py`;
- verify the report/review evidence commits above remain reachable in history.

## Independent review

Do not perform the final PR review yourself.

Once the branch reaches its final candidate head, hand PR #370 to the repository's independent PR Reviewer.

The reviewer should focus on:

- whether canonical architecture is sufficient and internally consistent without ADRs;
- whether any architectural invariant was accidentally lost during consolidation;
- whether the two surviving decision files were fully and correctly absorbed;
- whether normative ownership is unambiguous;
- whether active ADR/process references were completely removed;
- whether historical research was preserved truthfully where appropriate;
- whether the PR remains documentation/governance consolidation rather than hidden product change.

## Stop conditions

Stop and report rather than improvising if:

1. PR #370 has materially changed from the stated head before execution;
2. live `main` has moved in a way that changes the authority model materially;
3. a current architectural invariant exists only in a deleted decision file and no clear canonical owner can be determined;
4. removing an ADR-related rule would weaken an unrelated safety/review/security control;
5. a code behavior change appears necessary to make the canonical docs truthful;
6. the research evidence commits cannot remain reachable without history rewrite.

Do not respond to uncertainty by recreating an ADR layer.

## Completion criteria

This task is complete only when:

- PR #370 remains the delivery vehicle;
- `docs/architecture/decisions/` is absent from the final tree;
- semantic identity/evolution principles are fully represented in canonical architecture and owning specifications;
- deterministic simulation principles are fully represented in canonical architecture and Engine/runtime specifications;
- canonical architecture/specifications are sufficient to understand current architecture without historical records;
- repository authority/process docs contain no ADR role or decision-record authority layer;
- current planning cites canonical architecture/specifications rather than ADRs;
- current source/test comments contain no ADR references;
- maintained research destinations point to canonical architecture/specifications;
- historical research wording is not falsified merely for token cleanup;
- no replacement decision-log mechanism was introduced;
- no unrelated product behavior was changed;
- all relevant validation passes;
- no tracked `workspace/` material remains;
- `c6e67cb...` and `5118a085...` remain reachable in Git history;
- PR #370 title/body accurately describe canonical-architecture consolidation;
- the PR is not merged;
- independent PR review is the next gate.

## Final report

After pushing the completed changes to PR #370, report compactly:

- live-main baseline used;
- final branch head SHA;
- PR #370 title/state;
- confirmation that `docs/architecture/decisions/` is gone;
- canonical destinations for semantic identity/evolution and deterministic simulation;
- major process/guidance surfaces cleaned of ADR assumptions;
- any intentionally retained historical ADR mentions and why they remain historical rather than normative;
- validation results;
- confirmation that `workspace/` is absent;
- confirmation that the reviewed evidence commits remain reachable;
- next action: independent PR review on the exact final head.

Do not merge PR #370.
