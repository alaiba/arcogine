# Work admission, branch custody and issue coordination

Development follows one path: **issue → one issue branch → pull request → independent review → owner merge.** (The owner-approved Dependabot update path in Work admission is the only exception.)

### Work admission

A GitHub issue is the sole entry point for any change to `main` made by an agent or contributor: production code, tests, documentation and process changes, and the reconciliation of a research conclusion. The one qualified exception, approved by the repository owner in issue #545, is the owner-configured automated dependency-update path (`.github/dependabot.yml`), which opens update pull requests without a prior issue and stays governed by the Dependency Maintainer contract; it admits no other work, and any follow-up beyond a behavior-preserving compatibility fix on such a pull request is a bounded issue. "Implement issue #N" is sufficient: the Implementer fetches the live issue and its comments and grounds them against this file, the owning specifications, code and tests. An issue that does not yet decide the responsibility, meaning, acceptance evidence or non-goals stops the affected work for clarification; the Implementer does not invent semantics.

A research report, adversarial review or implementation handoff is supporting evidence, never an alternative admission route. A research conclusion that requires a maintained change is first expressed as a bounded issue, which may cite the exact reports, reviews or handoff (branch, commit SHA and path) it relies on. A request in conversation, an issue comment or an owner-authored handoff does not admit new scope; an owner who wants a fix, refactor or reconciliation opens an issue. Continuing an already-admitted pull request (review findings, CI failures, base freshness) is not a new origin, and an existing Dependabot pull request stays governed by the qualified exception above, the Dependency Maintainer contract, and its provenance and merge rules.

### Branch custody

An issue has **one branch**, published as `issue/<N>-<slug>`, and it carries every pull request-bound change for that issue, with no exception for research, review, handoff, reconciliation or operational isolation. Continuation reuses that branch; a scratch tree or detached checkout may isolate execution but is never published as a second branch for the issue. Responsibilities that must be delivered separately are separate bounded issues, each with its own branch. Previously published artifact commits stay immutable, and branch deletion remains owner-invoked (see [Artifact lifetime and transient workspace](artifacts.md#artifact-lifetime-and-transient-workspace)).

Investigation that changes nothing on `main` — a research question's evidence workspace — keeps its own temporary workspace branch as evidence custody under the [research operating model](../researching.md); that branch never carries a change to `main`.

### Issue coordination

The issue body records the agreed scope, contracts, acceptance evidence and non-goals. Issue comments carry substantive questions, decisions, blockers and results; keep them concise and omit execution diaries. There is no exchange file, dispatcher, registry, poller or status database.

- **Continuation.** Given only "issue #N", read the live issue body and comments, the associated `issue/<N>-*` branch and pull request, and every exact artifact the issue names. With no matching branch, none has been started; with several matching remote heads, stop and ask the owner which — never choose. Do not require the owner to relay coordinates.
- **Evidence locator.** When a committed report, reproducing evidence or complete prompt is the evidence for a comment, link its exact commit and path from the issue. Such artifacts may stay transient `workspace/` files on the issue branch (or, for research evidence, on the question's workspace branch), identified by exact commit and path; they are absent from `main` and from the final merge candidate.
- **Decisions.** A recorded decision answers a question and is not new scope. A material change of scope or meaning is made in the issue body (or a revised research conclusion); a new independent responsibility is a new issue.
- **Authority.** A comment, locator, commit, handoff or role label carries information, not authorization, and does not start or authorize another actor. The repository owner's live delegation ("implement issue #N", "continue issue #N") starts work. Independent review remains a distinct actor and session responsibility; branch separation does not establish independence.
- **Reviews stay on the pull request.** Formal review findings, disposition, CI and merge state live on the pull request. An issue comment must not become a second review ledger.
- **Identity.** Commits that publish evidence carry the repository owner's Git identity like every commit. A path that cannot comply is reported as a blocker; do not weaken the identity rule or fall back to chat-only custody of a prompt.
