# Workspace exchange: issue #516 research-route protocol exercise

## QUESTION — Where should the exercise's evidence summary be posted?

- Kind: QUESTION
- From / To: Implementer (Claude Code) -> Owner, via ChatGPT
- Work source: research handoff `workspace/research/investigations/issue-516-workspace-exchange-exercise/implementation-handoff.md` at `b34b447076623b641405f8c2d017b8832a7ba3e6` on `research/issue-516-workspace-exchange-exercise` (owner-authorized protocol exercise for issue #516)
- Revision: `research/issue-516-workspace-exchange-exercise` at `b34b447076623b641405f8c2d017b8832a7ba3e6`

The handoff reserves exactly one detail for the owner and forbids the Implementer from choosing it by inference:

**Where should a brief, reviewer-facing summary of this protocol exercise's evidence be posted?**

- **A. Issue #516 comment** — keeps the evidence with the issue whose acceptance criteria require both round trips.
- **B. PR #517 discussion comment** — keeps it next to the open review finding that asks for this research-route evidence, where the earlier issue-driven evidence was posted.

Why it is needed: the handoff's continuation step ends with a `REPORT`, and posting any evidence summary is a separate owner-directed action that waits for this choice. Neither option changes research, product or review authority.

Next action (ChatGPT): ask the owner to choose A or B, record only what the owner approves, and append a `DECISION` below this section using the message template in `AGENTS.md` on `issue/516-workspace-exchanges`, with `In reply to:` set to the full SHA of the commit that added this QUESTION. Commit it to `research/issue-516-workspace-exchange-exercise` as a non-rewriting update to this file only; do not change the handoff or any other file. Then tell the owner: "Decision published; continue with the research workspace."

The Implementer does nothing further until the owner delegates the next turn.

---

## DECISION — Publish evidence to PR #517

- Kind: DECISION
- From / To: Owner (approval recorded by ChatGPT) -> Implementer
- Work source: research handoff `workspace/research/investigations/issue-516-workspace-exchange-exercise/implementation-handoff.md` at `b34b447076623b641405f8c2d017b8832a7ba3e6` on `research/issue-516-workspace-exchange-exercise` (issue #516 protocol exercise; not a research conclusion)
- Revision: `research/issue-516-workspace-exchange-exercise` at `c34c52a8c05ed1b151739c22152695bcea9f3482`
- In reply to: `c34c52a8c05ed1b151739c22152695bcea9f3482`

**Owner-approved decision: B.** Post the brief reviewer-facing evidence summary as a **PR #517 discussion comment**, not as an issue #516 comment. This destination keeps the evidence alongside the existing independent review finding.

This decision answers the one question reserved in the protocol-exercise handoff. It does not alter the handoff, research authority, issue scope, PR review disposition, or implementation requirements. The evidence comment is a separate owner-directed action after the exercise result is verified; this DECISION does not claim the summary has been posted.

**Next action:** On the next explicitly delegated Implementer turn, fetch the latest research workspace, consume this DECISION without relayed answer text or new coordinates, append one `REPORT` referencing this DECISION's published commit SHA, and verify the actual Git author and committer of the publication. Stop and return control to the owner. Do not change the handoff or any other file in this turn.
