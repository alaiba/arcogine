# Handoff: reconstruct Arcogine's ADR baseline from first principles

You are executing an **owner-authorized architectural-governance baseline reconstruction** for Arcogine.

This is an implementation/reconciliation task, not a new research investigation and not an independent PR review.

The repository is only a few months old, has one developer/owner, and has no clients or external compatibility constituency. The owner has explicitly decided to stop carrying the existing ADR corpus as an append-only institutional history and instead reconstruct the maintained ADR baseline around only the few architectural decisions that are worth constraining Arcogine going forward.

The objective is **not to supersede the existing ADRs correctly**.

The objective is to make the maintained repository say, plainly and minimally:

> This is Arcogine's architecture now. These are the few decisions important enough that future Arcogine should understand why they exist. Everything else is Git history.

Git history is the historical record. The maintained ADR directory is the current durable decision set.

## Repository and branch

Repository:

`alaiba/arcogine`

Continue on the existing finite workspace/reconciliation branch:

`research/semantic-evolution-ratification`

Do not create a new research branch.

Do not rebase, reset, force-push, squash away, or otherwise rewrite the branch history. Preserve the already-handed-off research evidence commits exactly.

The exact decision-quality research evidence remains:

- report:
  - commit `c6e67cb5022acae482422336da83a1d5ac80a15c`
  - path at that commit: `workspace/research/investigations/semantic-evolution-meta-architecture-report.md`
- independent adversarial review:
  - disposition: `ACCEPT WITH QUALIFICATIONS`
  - commit `5118a0851b11f88002ad526d77b9a28a95d85307`
  - path at that commit: `workspace/research/investigations/semantic-evolution-meta-architecture-adversarial-review.md`

Those commits are immutable evidence coordinates. Their useful conclusions may inform this reconstruction, but their research chronology must not leak into durable architecture.

The commits after `5118a085...` on this branch constitute the first reconciliation attempt. Preserve them in Git history, but treat their resulting repository state as a **failed reconciliation candidate**, not as authority that must be repaired incrementally.

At prompt creation:

- live `main`: `f4122b5c9fb46847ab8136d24073cbcffb4f8232`
- open PR #369:
  - title: `docs(architecture): establish ground-zero semantic evolution boundary`
  - head branch: `research/semantic-evolution-ratification`
  - head SHA: `4075aa9416e63ee6ae06754ad8b4668c29d6a108`
  - base: `main`

Re-resolve all mutable state at execution start.

## Owner decision governing this task

The repository owner has explicitly chosen a **clean ADR baseline reconstruction**.

That decision means:

- the existing numbered ADR corpus is not entitled to continued presence merely because it was previously Accepted;
- no supersession chain is required merely to preserve repository history;
- Git already preserves the historical documents and their evolution;
- the maintained ADR directory should contain only decisions that intentionally constrain Arcogine's future architecture;
- the current ADR immutability/supersession machinery is itself part of the legacy governance model being retired;
- the one-time reset/reconstruction event is delivery history, not an ADR subject;
- the final architecture must be understandable without knowing about "ground zero", the reset, Q1-Q9, PR #360, PR #369, or the owner declaration that enabled the cleanup.

Do not turn this owner decision into blame/provenance prose inside a durable ADR. It belongs in the delivery context and PR description.

## Mandatory start-of-run grounding

Before changing anything:

1. Resolve current live `main` and record its exact SHA.
2. Resolve the current head/state of `research/semantic-evolution-ratification`.
3. Resolve PR #369 and inspect its current title/body/head/base/review/check state.
4. Read `AGENTS.md`.
5. Read `.github/agents/work-planner.agent.md` for implementation-handoff/lifetime rules.
6. Read `docs/architecture/decisions/README.md`.
7. Read `docs/architecture/decisions/0000-template.md`.
8. Read `.github/scripts/check-adr-immutability.py` and its tests.
9. Search the complete maintained repository for:
   - `ADR-`
   - `Accepted ADR`
   - `Superseded ADR`
   - `semantically immutable`
   - `supersed`
   - `check-adr-immutability`
   - `ground zero`
   - `pre-reset`
   - `proving/promoted`
   - semantic identity / fingerprint / revision / provenance
   - Factory versus Engine ownership
   - Governance ownership
   - Operational identity/authority
   - deterministic simulation / dispatch / event semantics
10. Read current Architecture Overview and the directly relevant domain architecture/specification documents.
11. Inspect all current numbered ADRs `0001` through `0016` from live `main`.
12. Inspect the first failed reconciliation's proposed ADRs/content at the current branch head, but treat them as candidate material only.
13. Read the exact reviewed report and adversarial review from their immutable commits far enough to extract surviving architectural principles and qualifications.
14. Record any material repository surface you cannot inspect.

Keep four things distinct:

- live `main` = current landed authority;
- the reviewed research evidence = decision-quality input;
- the post-review branch commits / PR #369 = failed reconciliation attempt;
- this task = owner-authorized reconstruction of the maintained decision/governance baseline.

## Core design principle

Do not optimize for preserving old ADR content.

Optimize for preserving the **minimum set of architectural constraints whose loss would make future Arcogine worse**.

For every candidate ADR, apply this admission test:

1. Does this decision intentionally constrain future architecture?
2. Would reversing it be expensive, dangerous, hard to undo, or semantically consequential?
3. Is the rationale not adequately obvious from current architecture, code, tests, or specifications?
4. Will understanding the decision still matter after current implementation details change?

If any answer is materially "no", the subject probably does not belong in the ADR directory.

The default is **not to create an ADR**.

Prefer, in order where appropriate:

1. executable invariant/test;
2. current architecture/specification;
3. development/process guidance;
4. planning/research state;
5. ADR only for genuinely durable architectural rationale.

## Target ADR corpus

The final maintained ADR corpus should be intentionally tiny.

Target: **no more than three durable ADR decision files**.

Fewer than three is completely acceptable.

A fourth ADR is allowed only if the implementation report explains why the decision passes all four admission-test questions and cannot be represented truthfully in current architecture/specification without losing important durable rationale.

The following are candidate shapes, not mandatory filenames or mandatory documents.

### Candidate: semantic identity and evolution

A durable cross-domain ADR may be justified for principles such as:

- semantic identity is non-rebinding;
- a materially changed semantic definition requires distinguishable identity;
- once Arcogine attributes retained/accepted state to a semantic definition, that attributed definition may not silently change;
- historical meaning and continuing support are separate contracts;
- retention, decoding, execution, migration, interoperability, and compatibility are distinct support dimensions rather than automatic consequences of identity;
- retained attribution requires the identity-defining semantics to remain resolvable for as long as the attributed record is retained;
- support obligations are explicitly scoped by the owning contract/authority;
- support withdrawal never makes an old semantic identity reusable for changed meaning;
- Arcogine has no universal whole-contract `proving/promoted` lifecycle;
- Arcogine does not use exercised-section freezing as a universal semantic-evolution model.

The ADR must state architectural invariants, not the research chronology that produced them.

### Candidate: domain authority boundaries

A durable ADR may be justified if the audit shows the rationale is genuinely worth preserving for cross-domain ownership boundaries such as:

- Factory owns authored production/design facts;
- Engine owns deterministic simulation interpretation/execution truth;
- Governance owns controlled revision/change attribution, requirements/assertions, evidence use, conformance/findings, and governed change;
- Operational owns real operational authority/continuation and externally realized operation semantics;
- observations, events, projections, DTOs, serialization, or outward views do not become domain truth merely because they expose it;
- consumer domains do not redefine authoritative production semantics.

Do not create a generic mega-ADR merely to absorb old documents. If current Architecture Overview expresses these boundaries adequately and the rationale is obvious, keep them there instead.

### Candidate: deterministic simulation semantics

Create a separate durable ADR only if the audit establishes that Arcogine has a hard-to-reverse deterministic simulation commitment whose rationale must survive changes to current Engine implementation/specification.

Detailed dispatch algorithms, queue terms, work decomposition mechanics, current `engine-semantics:v1` clauses, or release-specific rules do not deserve an ADR merely because they were once recorded in one.

## What must NOT become a durable ADR

Do not create or retain ADRs whose primary subject is:

- this reset/reconstruction event;
- "ground zero";
- the fact that the owner authorized removal;
- the absence of clients;
- Q1-Q9;
- research report/review coordinates;
- PR #360 or PR #369;
- who made a decision or who is responsible for it;
- migration from Rust to Java when the current codebase/toolchain already makes the choice obvious and no durable architectural rationale remains useful;
- routine implementation decomposition;
- current DTO/serialization details;
- temporary planning gates;
- release-specific V1/V2 compatibility mechanics;
- obsolete compatibility obligations;
- ordinary refactoring;
- a historical choice whose only remaining value is historical.

Do not preserve an ADR because deleting it feels uncomfortable. Git history is sufficient for history that no longer deserves current architectural authority.

## Reconstruct the ADR directory itself

Rebuild `docs/architecture/decisions/` around the new philosophy rather than layering another supersession regime over the old one.

The expected end state is approximately:

```
docs/architecture/decisions/
    README.md
    template.md
    <one to three semantic ADR files>
```

Semantic filenames are preferred over sequence numbers unless a concrete current need justifies numbering.

Do not reuse old numbered identifiers for new semantic decisions.

Delete the existing numbered ADR corpus from the final maintained tree unless an individual decision independently passes the new admission test and is intentionally reconstructed in the new format.

Do not preserve `0001-use-architecture-decision-records.md` merely to justify having ADRs. The ADR README can own the mechanism.

Do not preserve `0000-template.md` by inertia; replace it with a simpler `template.md` if a template remains useful.

## New ADR philosophy and README

Rewrite `docs/architecture/decisions/README.md` from first principles.

It should establish a simple model approximately like this:

- ADRs record the **small current set of durable architectural decisions whose rationale must constrain future changes**.
- ADRs are not an append-only institutional archive.
- Git history preserves old/replaced/removed decisions.
- Only decisions that pass the admission test belong here.
- Current-state behavior belongs in architecture/specification/code/tests, not in ADR history.
- Proposed/unresolved architecture normally belongs in research, planning, a branch/PR discussion, or another appropriate pre-adoption surface; `main` should not accumulate speculative ADRs.
- When architecture changes, the same reviewed change updates/removes the old current ADR and adds/updates the new durable decision as needed.
- A removed ADR remains discoverable through Git history; it does not require a tombstone or permanent supersession chain.
- ADR prose should be self-contained and semantic: Context, Decision, Consequences, and only useful Alternatives/Rationale.
- ADRs must not require reconstructing delivery chronology, PR numbers, research coordinates, owner declarations, or obsolete planning state.

Do not rebuild status bureaucracy under new names.

Prefer no lifecycle/status field unless it demonstrably adds current value. If every ADR on `main` is by definition an accepted current decision, say that plainly.

## Retire the old ADR immutability machinery

The current governance rule that Accepted/Superseded ADRs can never be deleted is intentionally being retired.

Update repository process/tooling atomically so the final repository does not contradict the new philosophy.

At minimum inspect and reconcile:

- `.github/scripts/check-adr-immutability.py`
- `.github/scripts/check-adr-immutability.test.py`
- `.github/scripts/check-adr-rename.test.py`
- workflow/script invocations of that checker;
- `AGENTS.md`;
- `.github/agents/pr-reviewer.agent.md`;
- `.github/agents/consistency.agent.md`;
- `.github/agents/work-planner.agent.md`;
- `docs/development/reviewing.md`;
- `docs/development/consistency-review.md`;
- any other development/contributor documentation that assumes Accepted/Superseded ADR semantic immutability or mandatory supersession chains.

Default outcome: **remove the old immutability checker and its dedicated tests/invocations** rather than replacing it with a comparably complex mechanism.

Keep a mechanical ADR checker only if it enforces a small, clear invariant that is still valuable under the new model. Do not create governance machinery merely because governance machinery existed before.

Do not weaken unrelated security, branch-protection, review, workspace, delivery-label, or CI safeguards.

## Audit every old ADR for durable signal

Before deleting the old corpus, audit each old ADR for signal worth carrying forward.

For every old ADR, classify its durable content into one of:

- **ADMIT TO NEW ADR** — genuinely durable rationale passes the admission test;
- **CURRENT ARCHITECTURE/SPEC** — useful current truth belongs in an owning architecture/specification document rather than an ADR;
- **EXECUTABLE INVARIANT** — code/test already owns it or should own it;
- **PROCESS/POLICY** — belongs in development/review/contributor guidance;
- **RESEARCH/PLANNING** — remains open, proposed, or sequencing-specific;
- **GIT HISTORY ONLY** — no maintained replacement needed.

Do not create a permanent migration matrix merely to document this audit. Use the implementation notes / PR description as the delivery-history record.

Before deleting an old ADR, make sure any genuinely current invariant that otherwise exists only there has an appropriate maintained destination.

Do not preserve prose whose only purpose is to explain how the project arrived at today's architecture.

## Preserve the reviewed semantic-evolution result without preserving its story

The decision-quality research still matters, but only its durable architectural content should survive.

The final architecture must carry the useful principles from the report/review, including the qualifications that actually constrain future semantics, without preserving:

- "ground-zero" vocabulary;
- owner attribution;
- pre-reset/post-reset narration;
- Q1-Q9 labels;
- evidence SHAs;
- review disposition text;
- support-estate inventories;
- transition exception procedures;
- implementation cleanup lists.

The durable result should read as architecture that makes sense to a future contributor with no knowledge of this investigation.

## Reconcile current architecture and specifications

After reconstructing the ADR set, reconcile maintained documentation so it no longer depends on deleted ADR identifiers or obsolete reset vocabulary.

At minimum inspect:

- `docs/architecture/overview.md`;
- Factory architecture/specification documents;
- Engine semantics architecture/specification;
- Governance architecture;
- Operational architecture;
- product concepts where architectural references appear;
- development policy;
- planning documents;
- research register and maintained investigation briefs;
- docs index/readmes;
- source/test comments only where they cite ADRs or encode obsolete architectural claims.

For every reference to a deleted ADR, choose deliberately:

- point to a new durable ADR when the rationale truly moved there;
- point to current architecture/specification when that is the real owner;
- remove the reference when no maintained replacement is needed.

Do not turn current architecture into a historical archive just because ADR history was deleted.

## Factory / Engine / Governance / Operational constraints

The ADR reset is not permission to destabilize domain semantics accidentally.

Preserve current architectural invariants that remain independently valuable, but place them in the right authority.

Examples that require deliberate treatment include:

- semantic/content identity versus controlled revision/history identity;
- Factory-authored facts versus Engine interpretation;
- deterministic Engine behavior where current architecture still intends it;
- explicit cross-policy/cross-definition comparison instead of implicit semantic lift;
- runtime observation/event/projection boundaries;
- Governance controlled revision/provenance/evidence boundaries;
- Operational continuation/authority semantics that remain current;
- consumer-owned semantics versus authoritative production semantics.

Do not preserve old compatibility promises merely to preserve them.

Do not settle unrelated open research such as Factory semantic composition, simulation analytics provenance, Operational closure/retirement, or a same-label Engine amendment mechanism.

## PR #369 handling

PR #369 represents the failed first reconciliation and must not be remodeled into the final candidate.

After grounding and before opening the replacement PR:

1. close PR #369 with a concise explanation that the reconciliation strategy was abandoned in favor of an owner-directed ADR baseline reconstruction;
2. preserve its GitHub history;
3. continue forward on the same branch;
4. do not force-push or erase its commits.

A fresh replacement PR from `research/semantic-evolution-ratification` to `main` can be opened after #369 is closed and the reconstruction is complete.

Do not reopen or repair PR #360.

## Delivery-history versus durable-authority rule

The replacement PR body should contain enough transition history to make the unusual change understandable:

- owner-authorized reconstruction of the ADR baseline;
- why Git history, rather than retained superseded ADRs, is now the historical record;
- broad classification of what was retained versus moved versus discarded;
- removal of the old immutability/supersession machinery;
- the final admitted ADR set and why each passes the admission test;
- validation performed;
- explicit non-goals.

But do **not** copy that transitional narrative into the ADRs themselves.

The PR is allowed to explain the reset.

The ADRs are not.

## Research state

The semantic-contract maturity/evolution research may remain `CONCLUDED` if the reconstructed architecture actually carries its durable result.

Rewrite maintained research-state references away from failed ADR numbers and reset chronology.

The durable conclusion should be expressed semantically, for example:

- semantic identity is non-rebinding;
- identity and support obligations are distinct;
- retained attribution requires resolvable defining semantics;
- support dimensions are contract-scoped rather than universally promoted.

Do not preserve exact temporary report/review coordinates in maintained research state merely for posterity.

Do not reopen the investigation unless implementation uncovers a genuinely new semantic uncertainty that blocks the reconstruction.

## Planning

Update planning only where a deleted ADR reference or failed reset vocabulary would make the plan misleading.

Do not use this cleanup to reprioritize the roadmap or pull future implementation work into the PR.

No production compatibility cleanup belongs in this slice.

## Product/code scope

This is primarily architecture/documentation/governance cleanup.

Do not delete V1/V2 implementation, legacy hash code, readers, runtime behavior, or other production compatibility machinery merely because the ADR baseline is being reconstructed.

That cleanup, if still useful, is separate implementation work after the architecture baseline lands.

Touch product code/tests only when necessary to repair a direct broken reference or executable architectural guard, and keep such changes minimal.

## Validation

Because this PR changes repository governance/tooling, validation must cover both documentation and the changed process.

At minimum:

1. search the final tree for stale references to deleted ADR identifiers;
2. search for stale `Accepted/Superseded` immutability/supersession instructions;
3. search for stale `ground zero`, `pre-reset`, Q1-Q9, PR #360/#369, and other transition-only vocabulary in durable architecture;
4. run Markdown-link validation;
5. run delivery-label validation;
6. run transient-workspace validation;
7. run relevant workflow/script tests for every checker/tool changed or removed;
8. run action/workflow validation if workflow files change;
9. run `git diff --check`;
10. run the repository's narrow documentation/process quality gates required by current `AGENTS.md`.

Do not run product suites merely for ceremony if product behavior is unchanged.

If a formerly required CI check references the removed ADR checker, update the workflow/gate coherently in the same change so CI evaluates the new repository policy rather than the retired one.

## Transient prompt/workspace cleanup

This prompt is delivery scaffolding.

Before handing the replacement PR to independent review:

- remove this prompt from the branch's final tree;
- remove any other tracked `workspace/` content;
- run `.github/scripts/check-transient-workspace.py`;
- verify the handed-off research commits `c6e67cb...` and `5118a085...` remain reachable in branch history.

Do not move the prompt or research artifacts into `docs/`.

## Independent review

This implementation session must not perform the final independent PR review.

After the replacement PR is open on its final candidate head, hand it to the repository PR Reviewer.

The reviewer should evaluate:

- whether the retained ADR set is genuinely minimal and durable;
- whether any important current architectural invariant was accidentally lost;
- whether old ADR references were fully reconciled;
- whether the new ADR governance model is coherent and simpler;
- whether the old immutability machinery was completely removed or consistently redesigned;
- whether the current architecture/specifications remain internally truthful;
- whether the change stays out of unrelated product/research scope.

Do not ask the reviewer to enforce supersession chains that this PR explicitly retires as repository policy.

## Stop/escalation conditions

Stop and report the exact blocker rather than improvising if:

1. live `main` moved in a way that materially changes the architectural corpus being reconstructed;
2. a real client/release/external retained artifact or other external compatibility constituency is discovered;
3. an old ADR contains a current invariant that cannot be placed confidently in ADR/current architecture/specification/code/test/process;
4. removing the old ADR checker would accidentally weaken an unrelated repository safety property;
5. GitHub prevents safe closure of #369 / continued use of the same branch / opening of a fresh replacement PR;
6. preserving the immutable research evidence commits would require history rewrite.

Do not fall back to another supersession-chain exercise merely because the old process objects to being replaced.

## Completion criteria

The task is complete only when:

- live baseline was re-grounded;
- PR #369 is closed as an abandoned reconciliation attempt;
- the old numbered ADR corpus has been audited;
- the final ADR directory contains only the minimal admitted durable decision set, target <= 3 ADRs;
- each retained ADR passes the admission test;
- reset/research/owner/provenance narrative is absent from durable ADR prose;
- `docs/architecture/decisions/README.md` expresses the new current-decision philosophy;
- the old Accepted/Superseded append-only governance regime is removed;
- `check-adr-immutability.py` and related machinery are removed or deliberately replaced with something materially simpler;
- all maintained references to deleted ADRs are reconciled to their actual owning authority or removed;
- current architecture/specifications still carry all intentionally preserved invariants;
- semantic-evolution research state points to durable semantic outcomes, not failed reset ADR numbers;
- no unrelated research question was silently settled;
- no production compatibility cleanup was bundled;
- all relevant validation passes;
- no tracked `workspace/` material remains;
- report/review evidence commits remain reachable;
- a fresh replacement PR is open from `research/semantic-evolution-ratification` to `main`;
- the replacement PR has not been merged;
- independent PR review is the next gate.

## Suggested replacement PR title

Prefer a title describing the durable outcome rather than the reset chronology, for example:

`docs(architecture): reconstruct durable decision baseline`

or a clearer equivalent based on the actual final diff.

## Final report

After pushing the completed reconstruction and opening the replacement PR, report compactly:

- live-main baseline used;
- branch;
- exact final head SHA;
- PR #369 closure status;
- replacement PR number + URL;
- final ADR files retained/created;
- old ADR corpus disposition;
- ADR governance/checker disposition;
- key architectural signals moved to current architecture/specification instead of ADRs;
- validation results;
- confirmation that `workspace/` is absent from the final tree;
- confirmation that `c6e67cb...` and `5118a085...` remain reachable;
- next required action: independent PR review on the exact replacement-PR head.

Do not merge the replacement PR.
