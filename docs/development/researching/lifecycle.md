# Lifecycle, priority, promotion, and maintained state

Part of the [research operating model](../researching.md); it is mandatory whenever the entry point routes your task here.


Arcogine research exists to resolve material uncertainty before product, architecture, or implementation silently commits to an answer.

The research-question lifecycle is:

| Status | Meaning |
|---|---|
| **CANDIDATE** | Material uncertainty exists, but the investigation is not yet sufficiently bounded or timely to start |
| **READY** | Question, scope, evidence expectations, and exit criteria are sufficiently clear to investigate, and the question remains unresolved. This includes admitted research before investigation, while evidence/review/reconciliation is in progress, and after a report exists but before the normative `CONCLUDED` conditions are satisfied |
| **CONCLUDED** | A decision-quality result exists and durable consequences have been reconciled, or the result was explicitly no action |
| **SUPERSEDED** | Later evidence, question, or decision replaced the investigation before normal conclusion |

These lifecycle values apply only to research questions in the maintained research register. Synthesis seeds have no research lifecycle, priority, owner, delivery commitment, or percentage completion. Do not use percentage completion for research questions; track evidence, falsified hypotheses, and exit criteria instead.

Research priority is **portfolio guidance, not delivery commitment**. A `READY` research question is admitted and sufficiently bounded to investigate, but remains unresolved; it may be idle, actively investigated, awaiting required review, or awaiting durable reconciliation. `READY` does not mean implementation is ready or admitted.

The promotion path is:

```text
Research
   |
   v
Decision-quality evidence
   |
   +--> no action
   +--> product clarification
   +--> architecture / specification
   +--> implementation responsibility
                       |
                       +--> bounded issue (citing the handoff as evidence) --> Implementer --> one issue branch --> PR
```

A conclusion is ready for implementation only when it passes the implementation-ready test in [Implementation handoff](reconciliation.md#implementation-handoff). An unresolved question that still determines the change stays in research. Research may recommend a durable consequence, but the conclusion becomes authoritative only through reconciliation into the appropriate product, architecture, specification, reference or research surface, or through the implementation pull request of a bounded issue that cites the conclusion and its handoff as supporting evidence. A research report or handoff never admits a change to `main` by itself; an issue does ([work admission](../agent-rules/work-admission.md#work-admission-branch-custody-and-issue-coordination)).

`docs/research/` therefore tracks what Arcogine still needs to understand or decide. Research documents never receive temporary delivery coordinates.

An explicit owner decision is a separate route, not a research conclusion. When the owner decides a question directly — for example by choosing to replace an architecture rather than settle an open argument about what the existing one permits — that decision is recorded in an issue and reconciled as an ordinary architecture and implementation change on that issue's branch under independent PR review. It needs no research acceptance, is not tested for compatibility with the rules it deliberately replaces, and must not be presented as the adoption of an unaccepted report. Research made moot by the decision is marked `SUPERSEDED`, and the knowledge-transfer audit in [workspace retirement](workspace-retirement.md) still applies to its evidence.

A synthesis seed is outside the promotion path. It preserves only recurrence-detection value under the rules in §10; if recurrence later justifies a bounded Arcogine research question, that question must be deliberately admitted to the normal research register and lifecycle.

## Research-register maintenance

Maintain [`docs/research/research-register.md`](../../research/research-register.md) as state, not as another policy surface:

- add a material unknown instead of hiding it in an implementation plan;
- mark research `READY` only when an independent researcher can execute it from the stated evidence and exit criteria;
- do not update the maintained register merely because evidence gathering starts, pauses, resumes, or a report/review is in flight; temporary execution state belongs to the research workspace and exact evidence handoffs, while the register changes only when the question's admitted/unresolved/terminal state or another decision-relevant portfolio fact changes;
- when research concludes, record the verdict and durable destination in the register without duplicating the authoritative conclusion;
- during consistency review or issue triage, move unresolved exploratory content back to research rather than allowing an issue or handoff to settle it implicitly;
- terminal questions (`CONCLUDED` or `SUPERSEDED`) normally remain for lifecycle and provenance history; compact or remove a terminal row only when its continued presence no longer helps explain a material conclusion, supersession chain, reopening trigger, or active decision context, all durable consequences have already been reconciled, and removal is not being used as a substitute for the knowledge-transfer audit in §10;
- prefer current architecture/reference for durable semantics; the register records portfolio state and provenance, not a duplicate semantic authority.
