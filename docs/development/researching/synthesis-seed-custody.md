# Synthesis-seed custody

Part of the [research operating model](../researching.md); it is mandatory whenever the entry point routes your task here.


A **synthesis seed** is a compact, non-authoritative record that preserves the ability to recognize a potentially generalizable signal across otherwise independent investigations. It is not an Arcogine research question, accepted product/architecture semantics, implementation commitment, publication candidate, or work item.

The purpose of the seed mechanism is to preserve enough evidence-bearing connective tissue that independently recurring knowledge can be recognized after temporary workspaces are retired. Recurrence can justify later synthesis; it does not itself establish that a result is general beyond Arcogine.

Admit or extend a seed only during reconciliation or another explicit knowledge-transfer review, after the originating investigation has reached its own result. The candidate must satisfy all three conditions:

1. **Evidence-bearing** — it arose from substantive evidence, discriminating proving cases, experiment, counterexample, implementation experience, or another actual investigation result rather than free-form speculation.
2. **Potentially transferable** — after Arcogine-specific class names and implementation details are removed, an intelligible proposition, distinction, failure mode, method, or counterexample remains.
3. **Loss-sensitive** — retiring the workspace without the compact signal would materially reduce the chance that a later independent investigation could recognize recurrence.

Prefer preserving reusable proving cases, counterexamples, measurements, protocols, tests, or other research assets in the durable Arcogine surface that will actually use them. A seed is connective tissue: it points to those assets and, when useful, to reconciliation delivery history rather than duplicating whole reports.

Do not create a seed merely because a finding is interesting, publication is imaginable, or a researcher wants to keep notes. If the item is an unresolved Arcogine decision, track it in the research register. If it has an accepted Arcogine semantic consequence, reconcile that consequence into its authoritative destination. If it is situational and not worth future recovery, discard it explicitly.

Each maintained seed record in [`docs/research/synthesis-seeds.md`](../../research/synthesis-seeds.md) must contain:

- **Signal** — the smallest potentially transferable observation, distinction, counterexample, method, or hypothesis;
- **Origin** — the originating investigation, the durable reconciliation destination when one exists, and optionally a delivery-history provenance reference for where reconciliation occurred;
- **Evidence posture** — what is actually established, without upgrading an Arcogine-specific result into a broader claim;
- **Boundaries / counterevidence** — known conditions where the signal may not hold, contrary examples, or material untested scope;
- **Reusable assets** — durable proving cases, counterexamples, measurements, protocols, tests, source maps, or other research assets;
- **Occurrences** — later independent-occurrence or reuse entries with the materially similar aspect, durable destination where one exists, and an optional delivery-history provenance reference;
- **Revisit when** — a concrete evidence or recurrence condition that would justify considering a bounded synthesis investigation.

A **delivery-history provenance reference** identifies where reconciliation, review, or another relevant delivery event occurred — normally a merged pull request or equivalent record. It may preserve useful historical metadata, but it is not a storage guarantee for temporary workspace commits. Maintained research state must remain intelligible and useful if those temporary report/review commits are no longer fetchable; anything whose exact future readability matters must have been promoted deliberately before retirement.

When reconciling a new candidate, search existing seeds for semantic neighbors **only after** the originating investigation has reached its own result. Extend an existing seed when the underlying signal is genuinely the same. Record a later result as an **independent occurrence** only if the seed was not used as a load-bearing premise or framing input to that investigation; otherwise record **reuse**. Recurrence may justify considering a bounded synthesis question, but it does not establish generality by itself.

Do not use the synthesis-seed state as routine start-of-run grounding for an independent Arcogine investigation. First derive that investigation's candidates, evidence needs, proving/failure cases, and result from its bounded question and current repository authorities. A deliberate seed-informed investigation is permitted, but any later similarity is reuse rather than independent recurrence.

Every seed must state a concrete `Revisit when` condition such as independent recurrence in another domain, materially comparable external evidence, implementation experience that confirms or falsifies the signal, or reuse of the same proving method across distinct questions. When the condition fires, surface that fact for an explicit decision about whether a bounded cross-investigation synthesis question should be admitted to the normal research register. Do not automatically create research work, change priority, or promote the broader claim.

The reconciliation must name the workspace(s) covered by its transfer audit and state whether their evidence-custody obligations have been satisfied. If any material item still lacks a durable destination, qualifying synthesis-seed record, or explicit discard decision, the workspace evidence remains necessary. After reconciliation has landed and independent PR review has validated the transfer, the branch may be assessed as **retirement-eligible**; this describes evidence-custody completeness only, **not a requirement or permission for an agent to delete the ref**. Git branch deletion is never routine research or post-merge cleanup. Only a separate explicit user request may authorize it under the [artifact custody rules](../agent-rules/artifacts.md#artifact-lifetime-and-transient-workspace), after verifying the precise branch, terminal state of every carried research question, successful knowledge transfer, and absence of other unmerged work or evidence obligations. In the absence of such a request, the branch remains intact, and the retrospective may inspect its historical evidence.

These knowledge-transfer and branch-deletion-safety rules do not create another lifecycle state, permanent report archive, research issue ledger, publication backlog, or second research roadmap. [`docs/research/research-register.md`](../../research/research-register.md) is the maintained research portfolio state; [`docs/research/synthesis-seeds.md`](../../research/synthesis-seeds.md) is only maintained low-authority recurrence state. Neither state file defines the research operating rules or can make semantic or delivery decisions by itself. Historical decision-rationale records are dated history, not maintained state, and cannot make such decisions either.
