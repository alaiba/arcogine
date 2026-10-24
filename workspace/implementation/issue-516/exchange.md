# Workspace exchange: issue #516

## QUESTION — Publication-route preflight and owner-approved answer

- Kind: QUESTION
- From / To: Implementer (Claude Code) -> ChatGPT
- Work source: issue #516
- Revision: `issue/516-workspace-exchanges` at `bc8fb1b7d4a93a9759361f1555b6d93949dc9b7c` (PR #517 head before this exchange)

PR #517's review finding requires a real issue-driven round trip, including a ChatGPT-side write whose actual Git author and committer are inspected against the `AGENTS.md` rule that every commit uses the repository owner's human Git identity.

You found this file from the issue number alone, by resolving live remote heads matching `issue/516-*` per `AGENTS.md` § Workspace exchanges on this branch. Please:

1. Ask the owner for the answer to this question, and record only what the owner approves: **Should the issue-driven exchange stay on the implementation branch (`issue/<N>-<slug>`, removed before independent review), as PR #517 specifies, rather than on a separate coordination branch?**
2. Append a `DECISION` section below this one using the message template in `AGENTS.md`, with `In reply to:` set to the commit SHA that added this QUESTION. In its body, state the branch and path you resolved, how you found them (the `issue/516-*` lookup), and the write mechanism you are using (for example the GitHub connector's file-update operation).
3. Commit it to `issue/516-workspace-exchanges` as a non-rewriting update to this file only. Do not change any other file.
4. Tell the owner: "Decision published; check the workspace for issue #516."

On its next delegated turn the Implementer will fetch this branch, read your `DECISION`, inspect the actual author and committer of your commit, publish a `REPORT` with the result, and then remove this file from the PR tree before re-review.
