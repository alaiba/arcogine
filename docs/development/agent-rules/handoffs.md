# Prompt handoff preflight

When asked to write, draft, generate, or prepare a complete prompt or handoff for another session,
agent, reviewer, researcher, or execution context, persist it before composing the complete body —
see [Artifact lifetime and transient workspace](artifacts.md#artifact-lifetime-and-transient-workspace) for where
and how. Do not draft the complete prompt in chat and persist a copy afterward: compose it directly
into the persisted `workspace/` artifact, commit it, and hand it off only as `branch + exact commit
SHA + path` (plus an issue/PR identifier only when useful). After persistence succeeds, do
not duplicate the prompt body in chat. If repository persistence is required but unavailable, report
the handoff as blocked rather than falling back to chat-only prompt custody.

A complete prompt or handoff is a persisted instruction/evidence package. An ordinary short
issue-based continuation notice (for example "Continue issue #N") is not one: it carries no
operational instructions beyond naming the issue and is not persisted.

Before committing a prompt, run a bounded content preflight:

1. verify the output destination and, for issue-origin work, the intended issue and its existing
   `issue/<N>-*` branch (see
   [Work admission, branch custody and issue coordination](work-admission.md#work-admission-branch-custody-and-issue-coordination)).
   A research evidence handoff is committed to its question's workspace branch and legitimately
   precedes any issue that will cite it; the issue-branch check applies once the owner has opened
   the admitting issue, and no preflight step may invent an issue or issue branch;
2. reconcile every operational instruction in the prompt with current repository and role authority;
   in particular, a prompt that asks for another branch for an issue that already has one, a
   workspace exchange file, or admission of work without an issue conflicts with those authorities
   and is corrected before commit, not carried forward.

Persistence establishes artifact identity; it does not confer authority on conflicting instructions.
