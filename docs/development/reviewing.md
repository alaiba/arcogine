# Review workflow

This document defines Arcogine's review discipline for implementation pull requests, including the AI-assisted workflow used for larger initiatives. It complements [CONTRIBUTING.md](../../.github/CONTRIBUTING.md): contribution mechanics live there; this document describes how a change is independently reviewed and re-reviewed before merge.

For AI execution of this policy, the repository-owned **PR Reviewer** procedure lives in [`.github/agents/pr-reviewer.agent.md`](../../.github/agents/pr-reviewer.agent.md). This document remains the normative review policy; the agent file defines how the specialized reviewer executes it.

The repository is the source of truth. Prior chat/session context, branch descriptions, and implementation-agent explanations are useful leads, but they are never authoritative over current `main`, the live PR head, maintained documentation, ADRs, tests, and CI.

## Role separation for larger initiatives

For multi-slice initiatives, use three distinct responsibilities:

1. **Planning** — defines initiative sequencing, chooses the next coherent slice, and writes the handoff/acceptance criteria.
2. **Implementation** — designs and implements one narrowly scoped slice, opens the PR, and responds to review findings.
3. **Independent review** — inspects the live PR against current repository state, posts actionable findings, re-reviews new heads, and states merge readiness clearly.

For AI-assisted work, a useful session boundary is:

- continue the same implementation session while fixing or completing the **same PR/slice**;
- start a **fresh implementation session for the next slice/branch**, forcing the new agent to re-ground itself from the repository rather than inheriting stale assumptions;
- keep a **persistent reviewer session across PRs** when useful for architectural continuity, but require it to re-read the live repository for every review;
- keep initiative-level planning separate from implementation and review when practical.

These are workflow boundaries, not product architecture. They exist to reduce confirmation bias, stale-context errors, and scope bleed between slices.

## Reviewer posture

The reviewer is not a second implementation agent and should not optimize for finding something wrong.

A good review:

- verifies correctness and architectural fit rather than personal style preference;
- distinguishes blockers from non-blocking improvements and future work;
- prefers the smallest change that satisfies the current slice;
- does not pull later roadmap work into the current PR without a concrete dependency;
- says explicitly when no blocking issues remain.

Unless asked to modify the branch, review work should follow:

```text
inspect
  -> reason
  -> review
  -> comment
  -> re-review
```

not `inspect -> rewrite implementation`.

## Review procedure

### 1. Resolve the live revision

Before reviewing, retrieve and verify:

- PR number, title, and description;
- current head SHA and base SHA;
- mergeability where available;
- changed files and the net diff;
- existing comments/reviews and prior findings;
- CI/workflow/check status.

Never assume the head reviewed previously is still current.

### 2. Re-ground from repository context

Read the relevant current material rather than relying only on the PR description. Depending on scope, this normally includes:

- `docs/product/charter.md` for significant product/architecture changes;
- `docs/architecture/overview.md`;
- the relevant planning document, such as `docs/planning/factory-simulation-engine-readiness.md`;
- applicable accepted ADRs;
- affected domain code and tests;
- `.github/CONTRIBUTING.md`;
- prerequisite/recent PRs when they materially define the current seam.

Repository state overrides prior conversational or agent context.

### 3. Reconstruct the intended slice

Identify the PR's:

- goal;
- acceptance criteria;
- explicit non-goals;
- prerequisite work;
- compatibility expectations.

If a handoff prompt exists, use it as review input, not as authority over the code or maintained docs.

### 4. Review the net change

Review:

```text
current main
vs
current PR head
```

not merely individual commits. This catches stale carry-over, merged work accidentally retained on the branch, and documentation that was correct at branch creation but is wrong against current `main`.

If `main` has moved materially since branch creation, reconcile the branch with current `main` before final review and rerun validation on the reconciled head.

### 5. Evaluate the change

At minimum, evaluate the following where relevant.

#### Functional correctness

- Does the implementation actually establish the stated behavior?
- Are lifecycle and failure cases truthful?
- Are tests proving semantics rather than implementation trivia?

#### Determinism

Arcogine's deterministic contract is non-negotiable. Watch for:

- multiple competing schedulers or clocks over the same mutable state;
- caller-supplied authoritative simulation time where the runtime should own time;
- unordered collections affecting execution semantics;
- unstable ID allocation;
- nondeterministic iteration or tie-breaking;
- asynchronous/event-bus dispatch that weakens explicit handler order;
- external mutation paths that bypass the authoritative runtime sequence.

Identical model, workload, configuration, and seed must produce identical ordered behavior/results.

#### Events, state, observations, and ownership

Preserve the architecture invariant:

```text
Events mutate State.
State produces Observations.
Observations inform Decisions.
Decisions produce Events.
```

Each mutable fact has one authoritative owner. Prefer immutable/read-only projections or explicit commands/events across boundaries rather than sharing mutable handlers, stores, schedulers, or synchronized copies.

#### Domain boundaries

Check that factory, economy, finance, simulation, agents, and interfaces retain their authority boundaries. In particular:

- factory execution must not require economy internals;
- operational facts and financial interpretation remain distinct;
- consumer boundaries should not expose scheduler/time plumbing without a concrete reason;
- mutable internal handlers/stores should not escape ownership boundaries merely for convenience.

#### Production semantics

Keep immutable accepted production/commercial intent distinct from mutable execution state. An `Order` should remain the accepted intent; `Job`/work execution should not become the authoritative source of commercial facts merely because it carries compatibility projections.

#### Canonical model and provenance

Runtime behavior should continue to derive from a published canonical factory model where that boundary applies. Do not weaken provenance or silently promote the current provisional model content hash into a durable cross-process identity guarantee contrary to ADR-0004.

#### Compatibility

Preserve compatibility by default. Inspect especially:

- HTTP/API DTOs;
- UI-facing projections;
- event payloads and correlation semantics;
- SSE/wire shapes;
- scenario behavior and deterministic baselines;
- identity/provenance fields.

An intentional compatibility change must be necessary, explicit, documented, and tested at the appropriate contract boundary.

#### Scope discipline

Check for silent pull-forward of:

- later readiness work;
- generalized frameworks;
- speculative abstractions;
- unrelated cleanup;
- broad public API/event redesign;
- model changes not required by the slice.

Prefer reducing scope when the current requirement does not yet give an abstraction a concrete responsibility.

#### Documentation accuracy

Current-state docs must describe what actually exists. Planning docs must distinguish implemented, partial, deferred, and explicitly out-of-scope capability.

After iterative fixes, re-check the PR title/body as well: a description of an API that no longer exists is a review defect even when the code is correct.

**Durable semantic vocabulary is a mandatory review check.** Initiative-local stage, gate, and slice identifiers are valid in `docs/planning/`, issues, PR descriptions/comments, and implementation handoffs where they help sequence delivery. Markdown documentation under `docs/` outside `docs/planning/` must instead name the capability, contract, identity, invariant, or behavior directly. A durable document may link to a plan, but understanding the document must not require reconstructing a temporary planning coordinate after that plan is completed, condensed, renamed, or removed.

The required CI check catches known coordinate-shaped vocabulary mechanically; review must catch semantic leakage that a regex cannot recognize. Do not waive a durable-document hit merely because the identifier is historically familiar.

#### Semantic propagation

For medium- and high-semantic-risk changes, review by concept as well as by changed file. Identify the small set of concepts whose meaning changed, then search maintained docs, tests, examples, interfaces, and configuration for both the new vocabulary and plausible old assumptions. This is especially important when a semantic change can leave syntactically unrelated prose or tests behind.

When an accepted ADR, readiness criterion, capability status, or other authority-bearing artifact changes state — for example `unresolved -> accepted`, `partial -> implemented`, or `blocked -> ready` — treat that as a propagation trigger. Inspect current architecture, directly related planning/status tables, maintained product concepts, reference surfaces, and implementation/evidence claims that may still describe the prior state.

This is bounded change-impact review. It does not require a repository-wide consistency sweep for every PR.

#### ADR discipline

Request an ADR only for a genuinely hard-to-reverse decision, for example durable identity/canonicalization contracts, persistent revision semantics, public compatibility/event contracts, scheduler/time authority, major domain ownership changes, or an execution decomposition whose semantics would be costly to unwind.

Do not require ADRs for ordinary local refactors.

Accepted and Superseded ADRs are semantically immutable. If a PR amends one in place under the editorial-amendment policy in `docs/architecture/decisions/README.md`, the reviewer must compare the pre-amendment and post-amendment record and independently establish that the decision, constraints, applicability, alternatives, and consequences have not changed. The required `Amendment: ...; no semantic change` metadata is process evidence, not proof. If semantic equivalence is uncertain or false, require a superseding ADR instead.

## Finding severity

Use severity to communicate merge risk, not rhetorical emphasis:

- **P0** — catastrophic correctness, data, or security issue.
- **P1** — functional or architectural blocker; should not merge.
- **P2** — important issue that should normally be fixed before merge.
- **P3** — non-blocking improvement.
- **Nit** — optional polish only.

Do not inflate severity. A P1 must identify a real invariant or correctness failure, not a preferred design alternative.

Use these calibration examples when the boundary is unclear:

- a semantic regression demonstrated by failing integration/contract tests, or a change that violates a binding architecture invariant, is normally **P1**;
- a PR whose central claimed behavior is still defeated by another maintained execution path is normally **P1**;
- a false completion/status claim or missing completion evidence that can be corrected without changing otherwise safe runtime behavior is normally **P2**, unless that false status itself unlocks a dependent architectural boundary;
- a stale PR title/body or validation description after remediation is normally **P2** when it materially misstates the proposed head;
- optional extra coverage, cleanup, or future hardening that does not affect the current invariant is **P3** or **Nit**.

Each actionable finding should state:

1. what is wrong;
2. why it matters;
3. the invariant/outcome the fix must restore.

Prescribe a specific implementation only when there is effectively one safe solution; otherwise leave room for the implementation agent to choose the smallest valid fix.

## GitHub feedback

When review feedback is intended to be durable, post it on the PR rather than leaving it only in a chat/session.

- Prefer a formal review when the authenticated reviewer can submit one.
- Use `REQUEST_CHANGES` when available for blocking findings.
- If GitHub prevents `REQUEST_CHANGES` because the reviewer owns the PR, submit a `COMMENT` review containing the blocker(s) and an explicit disposition such as `CHANGES REQUIRED`.
- Fall back to a PR conversation comment only if review submission itself is unavailable.
- Keep comments concise enough to act on.
- Do not duplicate already-resolved findings on later heads.

PR comments are useful execution history, but any architectural conclusion that must outlive the PR belongs in maintained docs or an ADR as appropriate.

## Re-review

When a new head is pushed:

1. resolve the new head SHA;
2. verify the original finding against the new implementation;
3. inspect the net diff for regressions introduced by the fix;
4. check whether docs/PR description were kept in sync;
5. check the current CI state;
6. retire resolved findings instead of repeating them mechanically.

A fix is complete when the violated invariant is restored, not merely when the named method/type from the original comment has changed.

## CI and validation language

Distinguish these states precisely:

- **checks passed** — visible CI/checks on the current head are green;
- **author reports local checks passed** — useful evidence, but not independently visible CI;
- **no failing checks visible** — not equivalent to green when no checks have run;
- **no workflow run/status present** — explicitly unresolved validation state.

CI absence alone is not automatically an architectural blocker, but merge readiness must state it accurately.

## Final disposition

Every review/re-review should end with a clear disposition. There are exactly two:

- **READY TO MERGE** — independent review is complete and authorizes merge of this exact PR head. Required validation does not have to be green yet for the review itself to reach this disposition, but merge readiness (see `AGENTS.md`'s PR lifecycle) additionally requires it.
- **CHANGES REQUIRED** — at least one blocking/pre-merge finding remains.

CI is not a reviewer disposition. There is no third disposition for "review is clean but CI is still pending" — required CI is enforced independently by GitHub branch protection, never by review vocabulary. If code/docs inspection is clean but required validation has not finished, report that plainly and do not emit a canonical disposition block for this review; the PR lifecycle stays `AWAITING` until CI is green and a current-head review concludes `READY TO MERGE`. Do not treat green CI alone as authorizing merge, and do not treat an earlier clean inspection as sufficient once the head or required validation has moved — re-resolve and issue a fresh disposition.

Optional, genuinely non-blocking observations belong in review prose or a follow-up issue, not in a formal disposition. If the only remaining items are non-blocking, the disposition is simply `READY TO MERGE`.

For medium- and high-risk reviews, the final report should also identify the material semantic neighbors inspected, including important surfaces inspected that required no change. This coverage note is evidence of review breadth, not a claim that those surfaces are globally consistent.

Do not leave merge readiness implicit.

Before `READY TO MERGE`, explicitly verify two documentation-lifetime conditions when applicable:

1. durable documentation touched or semantically affected by the PR does not depend on temporary planning coordinates; and
2. every in-place Accepted/Superseded ADR amendment is independently proven semantics-preserving, otherwise supersession is required.

### Canonical disposition format

To make merge disposition enforceable, every complete review/re-review that reaches a final verdict must end with a machine-readable canonical disposition block. This block is parsed by Arcogine's PR disposition merge gate (`.github/workflows/pr-disposition.yml`) and must appear exactly once per review, as the final block in the review body, in this format:

```
Reviewed head: <full-PR-head-SHA>
Disposition: **READY TO MERGE**
```

where the disposition value is `READY TO MERGE` or `CHANGES REQUIRED` (in `**...**` markdown bold markers). If review is clean but required CI is still pending, omit the block entirely rather than inventing a third value — see Final disposition above.

**Key semantics:**

1. **Current-head binding:** The reviewed head SHA must match the exact current PR head that was inspected. When a new commit is pushed to the PR (new head SHA), the prior review's disposition becomes stale and does not authorize merge.

2. **Canonical final block only:** The disposition block is the authoritative source. Prose elsewhere in the review (discussion, examples, quoted prior reviews) that mentions disposition names is not authoritative and does not trigger merge-gate evaluation.

3. **Staleness invalidation:** If a new commit is pushed, the PR head SHA changes, and any prior review's disposition (including `READY TO MERGE`) is no longer valid. Merge remains blocked until the current head receives a fresh `READY TO MERGE` disposition.

**When updating or re-reviewing:**

- If the PR head has not changed since your last review, you may edit the existing review to update the disposition value or findings without changing the reviewed head SHA.
- If a new commit has been pushed since your review, treat it as a new head that requires fresh re-review, and update the `Reviewed head:` SHA to the new current head before submitting.

### PR disposition merge gate

The repository enforces the canonical disposition format via `.github/workflows/pr-disposition.yml`, which:

- Runs on PR open, commit push (`synchronize`), and review submission/edit/dismissal
- Fetches the current PR head SHA and all review bodies (paginated)
- Evaluates whether the latest applicable review for the current head contains a canonical `READY TO MERGE` disposition
- Fails closed if:
  - No disposition exists for the current head
  - The latest disposition is `CHANGES REQUIRED`
  - The disposition is malformed or uses an unsupported value (including retired legacy values such as `READY AFTER CI` or `NON-BLOCKING FOLLOW-UPS ONLY`)
  - The disposition applies to a stale (old) PR head

The gate does not evaluate CI, mergeability, or unresolved threads — only the current-head disposition. Required CI is enforced independently by GitHub branch protection alongside this check. Only `READY TO MERGE` for the exact current PR head produces a passing check.

At time of writing, this status check is **not yet required** by the branch protection ruleset on `main` — see the check's own workflow file for the exact context name to add. Enforcement activates only once a maintainer adds it to the ruleset.

## Tests as design evidence

Prefer tests that demonstrate observable semantics and invariants, including as applicable:

- deterministic replay/event ordering;
- ownership/linkage and lifecycle completion;
- explicit workload without economy dependencies;
- compatibility/event contracts;
- model provenance;
- scenario-level regression behavior;
- intentional KPI or timing changes when semantics change.

Do not demand redundant tests when existing integration/baseline coverage already proves the invariant.

## Durable knowledge rule

A chat/session may discover a decision; it must not be the only place that decision exists.

Before considering an initiative slice complete, ask whether deleting the implementation/planning/review conversations would erase anything needed to understand:

- what the system does now;
- why a hard-to-reverse decision was made;
- what remains intentionally deferred;
- how the change is validated.

If yes, move that knowledge into the appropriate repository artifact: code/tests, current-state documentation, planning, an ADR, or the PR record. When promoting knowledge out of planning into a durable document, translate temporary delivery coordinates into semantic terminology.
