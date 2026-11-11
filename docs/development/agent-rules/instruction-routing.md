# Instruction size and progressive disclosure

When editing agent instructions, keep `AGENTS.md` at most 4,096 UTF-8 bytes and
`CLAUDE.md` as the single `@AGENTS.md` include. Aim below that ceiling: prompt size
affects tool-call reliability on smaller local models.

Keep routine routed files at approximately 8 KB or less. Give every entry-point
link a task trigger and target a small topic file or a specific section. Read the
relevant section, not an entire large manual by default. Reference material is
consulted for the affected contract, not loaded indiscriminately.

Moving a rule changes custody, not meaning. Keep one owning location for each
moved rule, update inbound links and anchors, and retain a concise trigger in
`AGENTS.md`. Role procedures remain mandatory for their selected tasks; routing
does not waive required evidence or authority checks. The role index must remain
reachable from `AGENTS.md` and link every canonical role.

Do not duplicate README product/setup guidance or the contributor workflow in the
agent entry point. Topic and role contracts supplement repository architecture,
contribution, documentation and executable authorities; they do not override them.

When a larger existing authority still requires a full read, preserve that
requirement until its own decomposition is delivered. Track that work as a
bounded issue per file, with a section-to-topic map and proof that no rule was
lost. The large authorities needing this treatment are the architecture overview,
research method, review method, research register, PR Reviewer contract,
continuous-improvement guide, testing guide and contributor guide. Their size is
not permission to omit applicable rules.

For a routing change, inspect byte sizes, run Markdown-link and role-contract
checks, and inspect moved anchors (the Markdown checker validates paths only).
Document the old-section to new-owner mapping in the PR description. Local-model
experiments belong in local diagnostics or transient evidence custody; report
their results and limitations as execution evidence, not durable behavioral rules.
