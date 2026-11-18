# 10. Research and reconciliation stay separate

Part of the [research operating model](../researching.md); it is mandatory whenever the entry point routes your task here.


The full lifecycle is:

```text
bounded research question / explicitly coupled research packet
        |
        v
temporary research-evidence workspace
        |
        +--> checkpoints / drafts / diagnostic notes (optional, non-authoritative)
        |
        v
completed report revision handed off by exact commit + path
        |
        v
independent adversarial review (when required by risk, §7/§9)
        |
        v
completed review revision handed off by exact commit + path
        |
        v
decision-quality evidence
        |
        v
durable reconciliation, admitted by a bounded issue
        |
        +--> no action
        +--> product
        +--> architecture / specification
        +--> implementation handoff --> bounded issue --> Implementer --> one issue branch --> PR
        |
        v
knowledge-transfer audit
        |
        +--> reusable evidence / know-how
        +--> historical decision rationale (non-normative, only when retained)
        +--> qualifying synthesis seed (non-authoritative)
        +--> explicit discard
        |
        v
temporary workspace files removed from final merge tree
        |
        v
reconciliation lands
        |
        v
branch history remains reachable while the ref is retained
```

A researcher may recommend durable consequences. A researcher does not make architecture authoritative merely by publishing a report; the [promotion rule](lifecycle.md) requires deliberate reconciliation into the appropriate durable authority.

The report's author must not silently rewrite current architecture, specifications, product semantics, or production code while "doing research." A separate reconciliation pass deliberately translates surviving research conclusions into the appropriate authoritative surface — an architecture or specification PR, or an implementation PR that the [Implementer](../agent-roles/implementer.md) produces — each admitted by a bounded issue that may cite the research report, review and handoff as supporting evidence, and each delivered on that issue's branch through normal independent PR review exactly as any other change would. Research itself never changes production code: anything durable leaves research only through an issue-admitted reconciliation.

Architecture and specification text describes what the code implements. A reconciliation without code may add only what current code already does: an ownership boundary, a refusal, a constraint already enforced, or a scope exclusion. A decided rule that current code does not implement stays in its research record, and its question stays `READY`, until an issue-admitted implementation lands the rule together with the code and tests that make it true. Architecture or specification text found describing unbuilt behavior is drift, and it is corrected or moved out of architecture. Where a research conclusion produced it, the text moves to that research record and the question returns to `READY` until an issue-admitted implementation lands it. Where it came from an explicit owner decision, or from research an owner decision superseded, the text is corrected or relocated without creating or reopening a research question.

**Separate reconciliation means a separate phase and authority transition, admitted by its own issue.** Neither continued reconciliation on the research workspace branch nor an implementation started from a handoff alone admits a change to `main`. The reconciliation issue and its single `issue/<N>-<slug>` branch govern the change; the research workspace stays evidence custody and never carries or merges it. The issue cites the completed report, review and handoff by exact commit SHA + path, which stay immutable on the workspace branch, while the issue branch carries the reconciliation edits and the knowledge-transfer audit.

Research is never `CONCLUDED` merely because a report was written. `CONCLUDED` requires the durable consequence to actually be reconciled, or an explicit, recorded no-action result. For an implementation handoff, the merged pull requests of the issues that cite it are that reconciliation.

## Implementation handoff

When a conclusion's durable consequence includes production code or tests, research delegates it; the researcher never implements it. A conclusion is **implementation-ready** only when:

1. the question is answered, and any independent adversarial review that its risk requires (§7, §9) has accepted the conclusion;
2. the owning module, contract or integration boundary is known;
3. the change's prerequisites have landed, or are explicitly unnecessary;
4. the implementation responsibility is bounded;
5. acceptance evidence can be stated before coding begins; and
6. non-goals are explicit enough to keep adjacent unsettled work out.

A conclusion that fails any of these stays in research: narrow it, or record what blocks it in the register.

For an implementation-ready conclusion, the researcher writes an **implementation handoff** in the question's workspace, under the [prompt-handoff preflight](../agent-rules/handoffs.md#prompt-handoff-preflight), with the [contents the Implementer needs](../agent-roles/implementer.md#implementation-handoff-contents). The handoff includes the knowledge-transfer audit in [workspace retirement](workspace-retirement.md), so the implementing pull request can carry its outcomes. The researcher returns the workspace branch, the exact handoff commit SHA and its path. The handoff is supporting evidence: it does not start or admit implementation. The owner opens the bounded issue(s) that cite its exact coordinates and delegates them; later questions and answers are issue comments, not re-supplied coordinates.

A handoff may split the work into several slices. Each slice is its own bounded issue with its own branch and pull request, and the handoff assigns each durable update — specification change, register row, knowledge-transfer outcome — to the slice whose pull request makes it true. Implementation of a slice starts only when the owner delegates its issue, never merely because the handoff exists. Until the last slice lands, the question stays `READY` and its workspace stays active as the handoff's custody; the question becomes `CONCLUDED` when the last slice's pull request merges.
