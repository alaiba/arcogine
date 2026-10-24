# Implementation handoff — issue #516 research-workspace protocol exercise

> **Status: owner-authorized protocol exercise, not a research conclusion.**
>
> This is an isolated fixture to exercise the *research-backed* communication route required by issue #516 and the open review of PR #517. It does **not** represent an investigation, accepted research finding, adversarial review, change to architecture, or admission of new product/implementation scope. The repository owner expressly authorized creating this fixture. Any lasting change still derives from issue #516 and normal PR review.

## Work source and authority

- **Source of responsibility:** [issue #516](https://github.com/alaiba/arcogine/issues/516), whose acceptance criteria require demonstrating both issue-driven and research-backed question/answer round trips. The issue-driven exchange has already been demonstrated; this fixture tests the missing research-workspace path only.
- **Relevant delivery:** [PR #517](https://github.com/alaiba/arcogine/pull/517), an unmerged candidate implementing the protocol contract. Do not treat its contents as landed on `main`.
- **Live-main grounding baseline when this fixture was prepared:** `e44097a76f6e181d7c15c2754794d57371d191f0`.
- **Candidate protocol baseline:** `issue/516-workspace-exchanges` at `8a634a7e547106a890a1856f7733aee41314c72f`. This isolated research workspace was branched from that revision solely to make the proposed protocol available for the exercise. Re-resolve live `main` and source PR before relying on current-state claims.
- **Owning contracts for the exercise:** `AGENTS.md` §§ Work admission and workspace exchanges, Artifact lifetime and transient workspace, Prompt handoff preflight; `docs/development/agent-roles/implementer.md` §§ Inputs, Branch, Workspace exchange; `docs/development/researching.md` §10.
- **Research status:** no admitted research-register question, report, decision-quality conclusion, or independent adversarial review is asserted or required for this *test fixture*. Do not cite this document as evidence of accepted research semantics.

## Bounded responsibility

Demonstrate that an Implementer can consume this **once-addressed immutable handoff**, publish a substantive `QUESTION` into this same research workspace, stop for an owner decision, then (only when delegated another turn) consume an owner-approved `DECISION` and publish a `REPORT`. The owner should have to relay only short turn notifications, not substantive text or fresh coordinates.

The exchange belongs at:

`workspace/research/investigations/issue-516-workspace-exchange-exercise/exchange.md`

on **this research workspace branch**, not on PR #517's implementation branch. Append messages following the existing `QUESTION` / `DECISION` / `REPORT` contract; use the handed-off revision as input identity. Never edit this handoff after its exact SHA has been given to the Implementer.

## Exactly one detail reserved for the owner

**Where should a brief, reviewer-facing summary of this protocol exercise's eventual evidence be posted: as an issue #516 comment, or as a PR #517 discussion comment?**

Both options concern delivery-history evidence, not research or product authority. The owner decides; the Implementer must not choose by inference. Do not add new issue scope, alter the authoritative review disposition, or post an evidence summary until that decision is received.

## Initial Implementer turn

1. Verify the branch, exact handoff commit SHA and this path before using the content. Ground against the above contract files, the live issue, and the current PR state. No new implementation or research conclusion is authorized.
2. Create `exchange.md` on this same research workspace branch containing **one `QUESTION` addressed to the Owner via ChatGPT**, asking the single undecided detail above. Include the issue and handoff source, relevant revision, the two alternatives, why the choice is needed, and the explicit next action: owner approval via the trusted conversation.
3. Commit and push the question in a fast-forward, history-preserving update; verify that the pushed message can be read. Do not change the handoff, production code, `main`, PR #517, issue #516, or any maintained repository artifact during this exercise turn.
4. Return control with a short notice that the research workspace has a question. Do not paste the question text or request that the owner relay its content.

**Stop here until the owner explicitly delegates the next turn.** A new workspace commit by itself is not an invitation to act.

## Small continuation step once the owner answers

On a subsequent, explicitly delegated turn, fetch the *current* research workspace, read the owner-approved `DECISION`, and append exactly one `REPORT` that references the decision's full commit SHA and states whether the question/answer was consumed without owner-relayed content. Verify the published commit's actual author and committer against Arcogine's human-owner identity rule, record the result, and return control. If the decision is missing, ambiguous, or cannot be published with compliant identity, report the concrete blocker and stop.

Any actual comment on issue #516 or PR #517 to present evidence to the reviewer is a **separate owner-directed action** after the owner selects the destination; it is not part of the above continuation step.

## Acceptance evidence and limits

- This completed handoff resolves by branch + exact SHA + path; its revision is preserved across all subsequent exchange commits.
- A `QUESTION`, owner-approved `DECISION` (in reply to the question commit), and `REPORT` (in reply to the decision commit) are readable in a single bounded research workspace; their Git history shows chronology without reusing mutable branch tips as artifact identity.
- On a follow-up instruction of “Continue; read the research workspace,” the Implementer retrieves the answer without another copied SHA or answer text from the owner.
- The workspace is transient research custody; no workspace file is introduced into PR #517's final merge tree. Retrospective accessibility lasts only while the relevant Git objects/ref remain reachable; branch deletion is owner-only.
- The exercise is **not** a production implementation, a research approval, a new deliverable PR, a change to issue #516's admitted scope, or a substitute for PR #517's existing independent review and CI gates.

## Validation

Inspect the exact commit/file and any later exchange revisions with Git or repository-connected reads. Check the message schema, reply commit identities, non-rewriting ancestry, and actual commit author/committer metadata; separately inspect the live PR for its own review/CI state if making claims about delivery. A message heading or GitHub account mapping alone is not evidence of owner approval: the decision must come from the owner's trusted instruction.

**Handoff outcome:** protocol-fixture input only. Do not interpret this artifact as a completed research report or as authorization to implement new product behavior.
