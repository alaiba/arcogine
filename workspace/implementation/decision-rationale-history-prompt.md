# Introduce selective non-normative decision-rationale history

Work in repository `alaiba/arcogine` on branch:

`process/decision-rationale-history`

Baseline when this handoff was created:

`d765c3e0bd1ba03406b71b857ee7827f8277d267`

This is an implementation/documentation-process change. Re-resolve live `main` before editing and use the repository's normal history-preserving base-normalization process if the branch is stale and the update is mechanical. Do not rewrite handed-off history.

## Goal

Introduce a **selective, durable, explicitly non-normative decision-rationale record** for cases where the reasoning behind an important reconciled decision is worth keeping after temporary research evidence is retired.

The desired distinction is:

- canonical product/architecture/specification/reference documents define **what Arcogine means and requires now**;
- planning defines admitted implementation work;
- research establishes decision-quality evidence before authority changes;
- historical decision-rationale records preserve **why a significant choice was made, what serious alternatives were considered, what trade-offs were accepted, and what changed circumstances would justify revisiting it**;
- historical records are evidence only and can never define, extend, override, or repair current semantics.

The purpose is to preserve useful reasoning **without recreating the old ADR system as a second architecture authority** and without bloating canonical architecture/specifications into dissertations.

## Important historical context

Arcogine deliberately retired its first-generation ADR mechanism in PR #370. That change was correct in eliminating:

- a separate architectural authority layer;
- decision status/lifecycle machinery;
- the need to reconstruct current architecture by reading chronological ADRs;
- duplicated or conflicting current semantics between ADRs and canonical specifications.

Do **not** undo that consolidation.

The gap to address is narrower: after research reconciliation and workspace retirement, some decision rationale can be worth retaining even when it does not belong in canonical architecture/specification and does not qualify as a synthesis seed.

The current repository already provides the right conceptual foundation:

- `docs/history/` is dated, non-normative evidence and never current authority;
- `docs/development/researching.md` already requires a knowledge-transfer audit and permits deliberate promotion of research artifacts or reusable evidence;
- `docs/research/investigations/` can retain deliberately selected research-history artifacts;
- synthesis seeds preserve potentially transferable cross-investigation signals, not ordinary project-specific decision rationale;
- Git/PR history remains useful delivery history, but should not be the only deliberate retrieval mechanism for high-value rationale.

The change should make that distinction explicit and operational.

## Read before editing

Read and follow current versions of:

1. `AGENTS.md`
2. `.github/CONTRIBUTING.md`
3. `docs/README.md`
4. `docs/history/README.md`
5. `docs/development/researching.md` in full
6. `docs/research/README.md`
7. `docs/research/report-template.md`
8. `.github/agents/researcher.agent.md`
9. `docs/development/reviewing.md`
10. PR #370 and the commit that consolidated/retired the old ADR layer

Search for current wording such as:

- "no separate decision-record layer";
- "Git and pull-request history preserve what changed and why";
- research knowledge-transfer/retirement rules;
- historical evidence authority disclaimers;
- any current ADR/decision-history language that would conflict with the new distinction.

Do not mechanically restore deleted ADR wording.

## Desired model

Introduce a concept with a name such as **historical decision rationale**, **decision-rationale record**, or similarly precise repository terminology.

Prefer avoiding the bare term **ADR** in current guidance because it carries the old authority/lifecycle meaning in this repository. Historical discussion of the retired ADR mechanism may of course still use that term.

A durable record should normally live under:

`docs/history/decisions/`

Use semantic, searchable filenames. Prefer a date plus semantic slug when dating materially helps historical ordering, for example:

`docs/history/decisions/2026-09-23-factory-model-optional-concerns.md`

Do not introduce a global numeric ADR sequence.

The directory should be historical evidence, not another authority tree.

## Core authority rule

Make this invariant unmistakable wherever the process needs it:

> A historical decision-rationale record cannot introduce, extend, override, or repair a current requirement. Any constraint, qualification, behavior, identity rule, or support obligation that still governs Arcogine must be present in its owning canonical current document or executable contract.

Corollaries:

- current architecture/specifications must remain sufficient to understand the system without reading history;
- a stale historical record that differs from current architecture is not automatically documentation drift;
- "the newest decision record says so" is never implementation authority;
- changing a historical record does not change current architecture;
- canonical documents may contain concise rationale required to interpret a rule correctly, but should not carry long alternative-analysis narratives merely to preserve history.

## Retention threshold

Do not make a record mandatory for every research result, architecture edit, PR, or design choice.

Define a selective retention test along these lines:

Retain a historical decision-rationale record when losing the rationale would materially increase the chance that a future maintainer would:

- repeat substantial investigation;
- re-open a deliberately rejected alternative without knowing the decisive trade-off;
- mistake a contingent choice for a universal truth;
- accidentally undo a constraint whose motivation is not apparent from the current contract;
- misunderstand why a deliberately narrower solution was chosen over a more general one.

A record is especially useful when several of these are true:

- the decision followed substantial research or independent adversarial review;
- two or more serious alternatives remained viable;
- the selected option intentionally accepted a non-obvious trade-off;
- the decision is foundational or hard to reverse;
- future evidence can reasonably trigger reconsideration;
- the canonical result is intentionally much shorter than the reasoning needed to understand the choice.

A record is usually **not** warranted for:

- ordinary local implementation choices;
- straightforward refactors;
- decisions whose rationale is obvious and fully captured by code/tests/current docs;
- temporary delivery sequencing;
- research material with no surviving decision relevance;
- interesting but non-decisive analysis;
- content already adequately preserved as a reusable proving case, synthesis seed, or other durable artifact.

"No rationale record" must remain a valid knowledge-transfer-audit outcome.

## Relationship to research reconciliation

Update the research reconciliation / knowledge-transfer audit so it explicitly asks:

> Is any decision rationale worth retaining independently of the current contract? If yes, where is it preserved? If no, record/establish that no separate rationale record is needed.

Do not create another research lifecycle state.

The audit should continue to classify material into the existing durable outcomes:

- accepted current semantics/constraints;
- qualifications;
- unresolved research/reopening triggers;
- reusable evidence/know-how;
- synthesis seeds;
- intentionally retained exact artifacts;
- explicit discard.

Add decision rationale as a **retention destination/category**, not as a new authority or backlog.

When a research result is reconciled:

- canonical docs receive the actual current rule and any qualification needed to interpret/apply it;
- a historical rationale record, if justified, receives the compact reasoning behind the choice;
- detailed temporary report/review narration remains disposable unless separately promoted for a clear reason;
- the research workspace can then retire once the full transfer audit is satisfied.

The rationale record should not depend on temporary `workspace/` SHA+path coordinates remaining fetchable after retirement. It may cite the merged reconciliation PR as delivery-history provenance. If a specific research artifact itself must remain readable, continue to use the existing explicit artifact-promotion rule rather than pretending a copied SHA preserves it.

## Relationship to Git and PR history

Do not demote Git/PR history. It remains useful delivery-history evidence.

Change current wording from an absolute idea like:

> Arcogine keeps no separate decision-record layer; Git and PR history preserve why it changed.

to the more exact rule:

> Arcogine keeps no separate decision-record **authority**. Current meaning lives only in canonical current authorities. Git/PR history preserves delivery history, and selected high-value decision rationale may also be retained under non-normative history for deliberate future retrieval.

Use repository-native wording, not necessarily this exact sentence.

A rationale record can point to a merged PR for provenance, but it must be understandable without requiring reconstruction of the entire PR discussion.

## Record content

Keep records concise. They are not archived research reports.

Define a small recommended structure, approximately:

```markdown
# <Decision title>

Date: YYYY-MM-DD
Authority: Historical, non-normative evidence
Current destinations:
- <canonical doc(s) receiving the decision>

## Decision
A short historical statement of what was chosen at the time.

## Context
Only the constraints that materially shaped the choice.

## Serious alternatives
The credible alternatives that mattered, with concise reasons they were not selected.

## Decisive rationale
The small set of facts/trade-offs that actually discriminated the choice.

## Consequences and accepted trade-offs
What the choice made easier/harder or deliberately left unresolved.

## Reconsider when
Concrete evidence or changed conditions that would materially weaken the original rationale.

## Provenance
- merged PR / other durable delivery-history reference
- deliberately retained evidence links, if any
```

Adjust the exact structure if repository style suggests something better.

Do not include:

- a normative "Status: Accepted" field;
- a workflow state machine;
- an approval lifecycle;
- a global sequence number;
- copied full research reports;
- long source dumps;
- duplicated current specification text;
- temporary `PLAN-*` / `REV-*` coordinates as durable semantic naming.

If a later decision changes the earlier one, normally create a new dated record and cross-link them rather than rewriting the old rationale to match the present. Small factual/link corrections are fine if they do not falsify the historical record. Do not introduce enforcement machinery for this unless an existing generic documentation check naturally needs a small update.

## Distinguish rationale from active explanatory text

Clarify this editorial boundary:

Canonical current docs should retain an explanation when removing it would make a current rule ambiguous, unsafe to implement, or easy to misinterpret.

Historical decision rationale is for explanation whose primary purpose is:

- why this option was selected over another;
- which trade-off was knowingly accepted;
- which assumptions were contingent at the time;
- what evidence would justify choosing differently later.

This prevents both extremes:

- canonical docs becoming essays;
- canonical docs becoming opaque rule lists with all useful reasoning removed.

## Distinguish rationale records from other durable research surfaces

Make the boundaries explicit:

### Historical decision rationale
Project-specific explanation of a significant reconciled choice. Non-normative. Searchable later.

### Durable research investigation artifact
Retained analysis/evidence whose exact or substantial content remains independently valuable beyond the decision summary.

### Synthesis seed
Compact potentially transferable signal intended to help recognize recurrence across investigations. Not merely "why Arcogine chose X."

### Research register
Current portfolio/lifecycle state and reopening triggers. Not a rationale archive.

### Canonical architecture/specification
Current requirements and semantics. Normative where designated. Not a chronological decision log.

Avoid storing the same narrative in multiple places.

## Likely files

Inspect current state and make the smallest coherent set of edits. Likely surfaces include:

- `AGENTS.md`
- `docs/README.md`
- `docs/history/README.md`
- a new `docs/history/decisions/` navigation/rules file if useful
- `docs/development/researching.md`
- `docs/research/README.md`
- possibly `.github/CONTRIBUTING.md`, `.github/agents/researcher.agent.md`, `docs/development/reviewing.md`, or report/reconciliation templates if they currently encode conflicting guidance

Do not touch a file merely to mention the concept everywhere. Prefer the narrow owning authorities plus enough navigation/reviewer guidance to prevent ambiguity.

The normative rule for when research reconciliation retains rationale should live in `docs/development/researching.md`, not in `docs/history/`.

The historical directory should describe its evidence/authority boundary and navigation, not become a second process-policy authority.

## Do not retroactively rebuild the old ADR estate

This change is prospective.

Do not:

- restore `docs/architecture/decisions/`;
- recreate old ADRs from Git history;
- copy all prior research conclusions into history;
- create tombstones/mappings for every retired ADR;
- introduce "Accepted / Proposed / Superseded" governance;
- introduce an ADR linter, registry, numbering mechanism, or mandatory record gate;
- make historical records prerequisites for understanding current architecture;
- rewrite current architecture merely to remove all rationale from it.

An older decision may be captured later only when doing so has concrete current value.

## First concrete use

Use the current Factory Model Semantic Composition reconciliation as the first proving case **if and only if its reconciliation is still pending when this process change is implemented**.

Do not reconcile that Factory decision as part of this process slice unless explicitly requested. The purpose here is to introduce the mechanism.

However, validate mentally that the proposed record format could preserve the useful non-normative result from that investigation, for example:

- the closed optional-record direction was selected for the bounded current scope;
- mandatory whole-model policies remained viable rather than disproven;
- open-envelope composition was not selected because current evidence did not justify its extra machinery, not because it was impossible;
- the five-fact spatial bundle was a deliberate coarse scope limit, not a universal theorem;
- future geometry-only or independently governed contract requirements are reconsideration triggers.

Those are exactly the kinds of facts that should not bloat the Factory specification but may be valuable historical reasoning.

## Validation

At minimum:

1. run the repository's Markdown/link checks appropriate to changed docs;
2. run `check-delivery-labels.py` if documentation naming changed;
3. run `check-transient-workspace.py` only after removing this handoff prompt from the final merge candidate;
4. search the final tree for contradictory current wording such as an unconditional "no decision records" rule;
5. verify no new current-authority table accidentally routes implementation/review to historical rationale;
6. verify current architecture remains understandable without history;
7. verify a historical record cannot be mistaken for accepted architecture from its header/index/navigation wording;
8. `git diff --check`.

No product test suite is required solely for documentation/process changes unless the actual diff touches executable code/config or repository gates classify it otherwise.

## Delivery

Implement this as one coherent reviewed change.

Before handoff for PR review:

- remove this `workspace/` prompt from the branch;
- ensure no other transient workspace files remain in the final tree;
- summarize the authority-model change precisely: **non-normative rationale retention added; separate decision authority not restored**;
- call out any wording from PR #370 that was deliberately narrowed and why;
- do not claim that every future decision needs a record.

The desired end state is simple:

> Current docs tell us what Arcogine is. Selected historical rationale tells us why an important choice was made. Neither can be confused for the other.
