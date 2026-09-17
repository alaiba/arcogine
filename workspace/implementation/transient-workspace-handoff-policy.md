# Implementation prompt — transient repository workspace and coordinate-only handoffs

Implement a repository-wide transient-artifact convention that makes temporary but commit-worthy working material structurally distinct from durable repository state, and makes handoffs reproducible through immutable repository coordinates instead of duplicated chat text.

This is a process/tooling change. Do not change Arcogine product semantics.

## Starting point

This prompt was created from live `main` at:

`c9f6bcf04614662b1432acc01489549d1a0c9142`

Work on the branch carrying this prompt. Re-resolve live `main` and relevant open PR state before finalizing; do not assume the recorded SHA remains current.

## Read first

Before editing, read:

1. `AGENTS.md`
2. `.github/agents/work-planner.agent.md`
3. `.github/agents/researcher.agent.md`
4. `.github/agents/pr-reviewer.agent.md`
5. `docs/development/researching.md`
6. `docs/development/reviewing.md`
7. `docs/planning/README.md`
8. `.github/CONTRIBUTING.md`
9. current CI workflow(s) and repository-owned validation/check scripts relevant to documentation/process invariants

Then do a focused repository search for at least:

- `implementation prompt`
- `handoff prompt`
- `research-evidence workspace`
- `temporary artifacts`
- `logs/`
- `report commit SHA`
- `exact commit SHA`
- `docs/planning/*prompt*` or equivalent prompt artifacts
- checks invoked by CI that enforce repository-wide documentation/process invariants

Use the search to find every current authority that needs to agree with the new lifecycle model. Do not duplicate policy text unnecessarily when one canonical rule plus role-specific references will suffice.

## Objective

Introduce one reserved repository root for **transient but commit-worthy artifacts**:

```text
workspace/
```

The invariant is:

- working/research/implementation branches may temporarily contain tracked files under `workspace/`;
- those files exist to provide persistence, handoff, exact artifact identity, or cross-session continuity;
- `main` must never contain tracked files under `workspace/`;
- a PR candidate is not merge-ready while its head contains any tracked file under `workspace/`;
- the directory should therefore normally be absent from `main` entirely because Git does not track empty directories;
- do not add `.gitkeep`, a README, or any other permanent file under `workspace/` merely to preserve the directory;
- document the convention in durable repository policy outside `workspace/`.

This is distinct from `logs/`:

- `logs/` remains gitignored local/session scratch and diagnostic output that should never be committed;
- `workspace/` is for transient material that **does need to be committed on a non-main branch** because another session, agent, reviewer, or later phase must be able to retrieve the exact artifact;
- durable code, tests, architecture, ADRs, maintained planning, reference material, and maintained research state remain in their existing canonical locations.

A useful conceptual structure is:

```text
workspace/
  implementation/
  research/
  review/
```

Do not require those exact subdirectories if repository evidence supports a simpler structure, but preserve clear semantic separation of implementation handoffs and research evidence. Filenames must be semantic rather than temporary delivery-coordinate-derived.

## 1. Canonical repository rule

Add the narrowest durable policy needed to `AGENTS.md` (and contributor documentation only where humans also need the same rule) so agents can classify artifacts by lifecycle:

### Local ephemeral material

Use `logs/` for ad hoc diagnostics, local captures, and session scratch that should not be committed.

### Transient committed material

Use `workspace/` for branch-local artifacts that must survive the current chat/session or be handed to another actor but are **not intended to survive on `main`**.

Examples include:

- implementation/coding-agent prompts;
- prompts intended for a fresh ChatGPT/session handoff;
- research report drafts and completed report revisions whose identity is branch + exact commit SHA + path;
- adversarial research-review artifacts;
- research checkpoints/diagnostic notes when they need repository persistence;
- review/handoff packets that are intentionally branch-transient.

### Durable material

Keep only maintained repository state in durable locations (`docs/`, product code/tests, scripts, workflows, etc.). A transient artifact may inform durable reconciliation but does not become durable merely because it was useful during delivery.

State explicitly that `workspace/` is **not** an archive and must be empty/absent from merge candidates and `main`.

## 2. Coordinate-only handoff rule

Make this a hard rule for any agent producing a complete prompt for a new session, coding agent, reviewer, researcher, or other fresh execution context when repository persistence is available.

The producer must:

1. write the complete prompt to an appropriate semantic path under `workspace/` on the relevant working branch;
2. commit that artifact using the repository owner's human Git identity;
3. treat `branch + exact commit SHA + path` as the handoff coordinate;
4. return only those handoff coordinates in chat, plus an issue/PR/planning identifier only when it materially helps locate the work;
5. **never duplicate the complete prompt verbatim into chat** once the repository artifact has been created;
6. never treat branch tip alone as immutable artifact identity when an exact commit exists;
7. if the prompt changes, commit a new revision and hand off the new exact coordinates instead of silently mutating the old handoff;
8. require the receiving session/agent to retrieve the prompt from that exact commit and path before acting.

A normal handoff should therefore be compact, for example conceptually:

```text
Branch: <branch>
Prompt commit: <full SHA>
Prompt: workspace/implementation/<semantic-name>.md
```

Do not prescribe that exact prose if existing response conventions suggest a better compact form; preserve the information invariant.

### No chat fallback

If a repository handoff artifact is required but cannot be persisted, do **not** fall back to pasting the complete prompt into chat and calling the handoff complete. Report that repository handoff persistence is blocked and state what failed. The goal is one authoritative prompt copy, not two potentially divergent copies.

This rule applies prospectively after this process change lands. While implementing this very change, preserve this prompt as the working artifact until the implementation has consumed it, then remove it before final review/merge readiness.

## 3. Work Planner changes

Update `.github/agents/work-planner.agent.md` so that when Work Planner generates an implementation prompt or prompt intended for another fresh session/agent:

- the full prompt is stored under `workspace/implementation/` (or the canonical implementation subpath chosen by this change);
- the file is committed before handoff;
- the response contains only immutable handoff coordinates rather than the prompt body;
- the prompt is temporary delivery scaffolding, not maintained planning authority;
- the final implementation branch must delete the prompt before it is eligible for independent PR review / merge readiness;
- durable implementation state remains represented by maintained planning, code/tests, architecture/ADRs/reference, and PR history rather than by preserving the prompt on `main`.

Preserve the existing rule that planning coordinates may be used to locate work in prompts but must not leak into durable semantic naming.

## 4. Research workflow changes

Reconcile `docs/development/researching.md` and `.github/agents/researcher.agent.md` with the structural workspace convention.

The current research model already requires temporary research-evidence workspaces, exact report commit SHA + path identity, immutable handed-off report/review revisions, durable reconciliation, and removal of temporary report/review/handoff files before merge. Preserve those semantics.

Make their repository location explicit:

- temporary research reports, report revisions, adversarial reviews, checkpoints, diagnostic notes, and research handoff artifacts that need persistence belong under `workspace/research/...` (or the canonical research subpath selected by this implementation);
- their evidence identity remains the exact workspace branch + commit SHA + path;
- later revisions get new commits rather than rewriting handed-off evidence;
- durable conclusions, qualifications, unknowns/reopening triggers, reusable proving assets, register state, and synthesis seeds still reconcile into their existing maintained destinations;
- before final reconciliation becomes merge-ready, temporary research artifacts are removed from the final tree;
- the research workspace branch is retired only under the existing knowledge-transfer/retirement rules.

Do not turn `workspace/research/` into a permanent research archive. Do not weaken the current evidence-custody guarantees.

## 5. PR review: artifact-lifecycle pass

Update the normative review process and PR Reviewer procedure so review explicitly considers **artifact lifetime**, not only artifact correctness.

For every newly added repository artifact, the reviewer should determine, proportionate to risk:

- what authority owns the artifact;
- whether it is intended to exist on `main` after merge;
- whether it is durable repository state or delivery/research/session scaffolding;
- whether the containing directory has admission/maintenance/retirement rules;
- whether its durable meaning is already captured in maintained authorities;
- whether retaining it would create a stale duplicate, archive dump, frozen prompt, or historical note with no active downstream role.

Add an explicit reviewer rule:

- any tracked `workspace/` file in the current candidate head is merge-blocking / `CHANGES REQUIRED`;
- independently of that mechanical rule, temporary material accidentally placed outside `workspace/` is still a review defect when its post-merge lifetime is unjustified;
- handoff prompts and implementation explanations are evidence of intent only and are not authority over live repository state.

This lifecycle check should have caught a completed implementation prompt added under `docs/planning/` even if its contents were internally correct.

## 6. Mechanical enforcement

Add a repository-owned executable check that fails when the checked tree contains any tracked file under `workspace/`.

Prefer the simplest deterministic implementation consistent with existing repository scripts. It should conceptually enforce:

```text
git ls-files workspace/
```

must return no tracked paths for a final validation/CI candidate.

Requirements:

- no filename heuristics such as only rejecting `*-prompt.md`;
- classify by reserved path/lifecycle, not by prose/content patterns;
- do not reject untracked local files that are outside Git's candidate tree merely because a developer happens to have local scratch;
- do not gitignore `workspace/`, because transient artifacts need to be commit-able on working branches;
- wire the check into the normal CI/final validation path that protects `main`;
- add focused automated tests for the checker consistent with existing script-test conventions;
- test at least: no workspace path passes; one tracked workspace file fails; nested files fail; similarly named paths outside the reserved root do not falsely fail.

If the existing CI architecture has a more appropriate central repository-hygiene checker than a new standalone script, integrate there instead of creating unnecessary tooling.

## 7. Implementation-side completion gate

Add a completion/handoff rule for implementation work:

Before a branch is handed to independent PR review as merge candidate, inspect files added by the branch and remove transient execution/handoff artifacts that are not intended as maintained repository state.

At minimum, ensure the final candidate head has no tracked `workspace/` files.

This should be a semantic cleanup step as well as the mechanical check: the absence of `workspace/` does not excuse a temporary prompt or one-off report accidentally stored in a durable directory.

## 8. Existing repository cleanup

Audit current `main` for already-landed files that violate the new lifecycle distinction.

Known candidate:

- `docs/planning/pr-normalization-cleanup-prompt.md`

Determine from current authoritative state whether it is completed transient implementation scaffolding. If its durable meaning is already captured in maintained review/base-normalization policy and it no longer protects active downstream implementation, remove it as part of this process cleanup.

Search for other clearly analogous implementation prompts or temporary research/review artifacts that have accidentally survived on `main`. Keep the cleanup narrow: do not delete maintained research briefs, registers, synthesis seeds, accepted architecture, or intentionally durable historical/decision material merely because a filename contains words like `report`, `review`, or `prompt`. Apply the lifecycle rule, not a string match.

## 9. Documentation placement

Prefer one canonical general rule plus role-specific operational references:

- `AGENTS.md`: lifecycle classification and repository-wide `workspace/` invariant, plus coordinate-only handoff principle;
- `.github/agents/work-planner.agent.md`: implementation prompt creation/handoff/removal procedure;
- `docs/development/researching.md` + researcher agent: research-evidence placement while preserving existing custody/retirement model;
- `docs/development/reviewing.md` + PR Reviewer agent: artifact-lifecycle review and merge-blocking `workspace/` rule;
- contributor docs only if human contributor behavior materially needs the same convention;
- executable script/tests/CI: deterministic final-tree enforcement.

Avoid copying a long identical policy paragraph into every file. Keep normative ownership obvious.

## 10. Acceptance evidence

The change is complete only when the repository demonstrates all of the following:

1. `logs/` remains the documented never-commit scratch/output location.
2. `workspace/` is documented as transient-but-commit-worthy branch-local storage.
3. `workspace/` is not gitignored and contains no permanent marker file.
4. `main`/merge-candidate invariant is explicit: no tracked `workspace/` paths may survive.
5. implementation prompts for new sessions/coding agents must be committed under the transient workspace and handed off only by branch + exact commit SHA + path.
6. complete prompt text is not duplicated into chat after repository persistence succeeds.
7. inability to persist a required handoff is reported as blocked rather than silently degrading to chat-only prompt custody.
8. research report/review/checkpoint placement is reconciled with the existing exact-SHA evidence model and knowledge-transfer retirement rules.
9. PR review contains an explicit artifact-lifecycle pass and treats tracked `workspace/` files as merge-blocking.
10. an executable CI/final-validation check deterministically rejects a candidate tree containing tracked `workspace/` files.
11. focused tests prove the checker and avoid path false positives.
12. known completed implementation prompt artifacts on current `main` are audited and removed when they no longer have an active durable role.
13. this very implementation prompt is removed from the branch before final independent PR review so the resulting PR demonstrates the invariant it introduces.

## 11. Validation

Run the repository-owned focused tests for the new/changed checker first.

Then run all documentation/process/script checks required by `AGENTS.md` and current CI for the files changed. At minimum, include the applicable repository hygiene/documentation checks and `git diff --check`.

If the change touches executable scripts, run their focused test suite and any broader required test command defined by current repository policy.

Report exact commands and outcomes. Do not claim a full pass for validation that could not run.

Because the prompt file itself intentionally violates the final-tree invariant while serving as the active handoff, perform validation in the correct order: consume/implement from the prompt, delete this prompt before final candidate validation, then run the final no-`workspace/` check and full required gates on the candidate head.

## 12. PR lifecycle

Keep implementation and independent review separate.

Before opening or handing off the final PR candidate:

1. re-resolve live `main`;
2. inspect the complete `main...HEAD` diff for scope drift;
3. perform the transient-artifact cleanup, including deletion of this prompt;
4. verify there are no tracked `workspace/` paths in the candidate tree;
5. commit/push with the human repository owner's Git identity;
6. open or update the PR with a concise description of the invariant, enforcement, migration/cleanup, and validation;
7. hand the current head to independent PR review;
8. do not merge — final merge remains the repository owner's responsibility.

Do not add bot/model/provider attribution or session URLs to commits or GitHub bodies.

## Completion report

When finished, report only the normal implementation/PR outcome and current lifecycle state. Do not reproduce deleted transient prompt text into chat.

Include:

- live `main` SHA used for final comparison;
- branch and final implementation commit SHA(s);
- PR number/link;
- durable files changed;
- chosen `workspace/` subpath convention;
- executable checker and focused-test locations;
- existing transient artifacts removed from durable paths;
- exact validation commands/outcomes;
- confirmation that final candidate head contains no tracked `workspace/` files;
- current PR lifecycle state and next owner/action.

The implementation succeeds when Arcogine has one structural lifecycle distinction that is easy for agents, humans, reviewers, and CI to enforce: local scratch stays uncommitted in `logs/`, transient cross-session evidence/handoffs may be committed only under `workspace/`, handoffs use immutable repository coordinates instead of duplicated chat bodies, and only durable repository state is eligible to land on `main`.