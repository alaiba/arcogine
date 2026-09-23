# Implementation handoff — durable assets must not depend on transient coordinates

## Task

Implement Arcogine issue #322 using the repository-owner decision that supersedes the issue's original proposed invariant.

The general rule is:

> Durable repository assets must not depend on transient coordinates.

Do **not** implement the issue body's proposed rule that every cited commit SHA must be reachable from `main`. That ancestry rule is unsound for the current repository because legitimate historical provenance SHAs can exist outside the current `main` ancestry. The implementation must preserve legitimate historical commit references.

Issue: https://github.com/alaiba/arcogine/issues/322

This handoff branch was created from live `main` at:

`c6236c9bb02318ae602c59a221cfb12d1ffad685`

At handoff creation, no open implementation PR referenced #322. Re-check live GitHub state before making delivery-state claims.

## Read first

Read and follow, in this order:

1. `AGENTS.md`
2. `.github/CONTRIBUTING.md`
3. issue #322, treating its incident history as useful evidence but its "reachable from main" proposal as superseded by this handoff
4. `docs/development/researching.md`
5. `docs/development/reviewing.md`
6. `docs/development/testing.md`
7. `.github/scripts/check-delivery-labels.py`
8. `.github/scripts/check-delivery-labels.test.py`
9. `.github/scripts/check-transient-workspace.py`
10. `.github/scripts/check-transient-workspace.test.py`
11. `.github/scripts/classify-changes.test.sh`
12. `.github/workflows/ci.yml`

Do a quick repository/docs search for:

- `transient coordinate`
- `workspace`
- `active-custody artifact identity`
- `durable knowledge destinations`
- `delivery-history provenance`
- `PLAN-`
- `REV-`
- `check-transient`
- `commit SHA + path`

Search hits are discovery only; fetch current target-revision content before relying on them.

## Repository model to preserve

Arcogine already has three related lifetime controls:

- `PLAN-*` and `REV-<NNN>` are temporary delivery coordinates and are mechanically kept out of durable semantic naming by `check-delivery-labels.py`.
- tracked files under `workspace/` are transient committed custody artifacts and are mechanically forbidden from merge candidates by `check-transient-workspace.py`.
- research handoffs deliberately use exact `branch + commit SHA + path` identity while an artifact is in active custody, and reconciliation must transfer anything worth retaining into durable knowledge destinations before workspace retirement.

Unify these under one general semantic invariant rather than adding a research-only or Git-ancestry-specific rule:

> Transient coordinates are valid for active custody and delivery context. Durable repository assets must preserve retained meaning through durable semantic state, deliberately promoted durable artifacts, or durable delivery-history provenance rather than depending on a transient coordinate.

"Depend on" is the important boundary. Process/policy material may mention transient-coordinate syntax when the syntax itself is the subject. A durable asset must not require a temporary coordinate to recover, validate, or understand the retained result.

## Objective

Make the repository express and enforce that lifetime rule at the narrowest authoritative surfaces.

The expected implementation shape is:

1. generalize the relevant `AGENTS.md` lifetime/durable-documentation language so the umbrella invariant is explicit;
2. add a deterministic checker for the mechanically recognizable transient-artifact-coordinate leak that remains after the existing guards;
3. add focused checker tests;
4. wire the checker into always-required CI/repository-tooling validation;
5. reconcile `docs/development/reviewing.md`, `docs/development/researching.md`, and `docs/development/testing.md` only as needed so they point to the same rule and executable behavior;
6. leave semantic leakage with no safe syntax signal as an explicit human-review responsibility.

Do not duplicate the full policy across multiple documents. Put the general operating rule in its narrowest existing authority and let downstream docs reference/apply it.

## Mechanical guard

Use the current reserved `workspace/` root to make the transient-artifact case mechanically recognizable.

Add a checker with a semantic name such as:

- `.github/scripts/check-transient-coordinates.py`
- `.github/scripts/check-transient-coordinates.test.py`

The checker should scan tracked repository text, following the same deterministic/offline philosophy as the existing repository checkers.

Its core prohibited case is a **concrete transient workspace artifact identity** leaking into a durable tracked asset: an exact full commit SHA used together with a `workspace/...` path as the artifact coordinate.

Example prohibited durable content:

`0123456789abcdef0123456789abcdef01234567 + workspace/research/example.md`

The exact formatting need not contain a literal plus sign; support the ordinary forms the repository uses for coordinates, including Markdown prose/table/backtick presentation, while keeping the recognition rule narrow enough to avoid treating unrelated SHA and workspace-policy prose as a coordinate.

Do not implement a generic "no SHA in docs" rule.

Do not query GitHub or compute ancestry/reachability.

Do not require the cited commit object to exist locally.

Do not infer transientness from arbitrary historical paths outside the reserved `workspace/` convention.

Do not make the guard depend on network availability.

The checker may exempt its own implementation/test fixtures where necessary, following the precedent in `check-delivery-labels.py`, but avoid broad allowlists of ordinary durable files.

Align scan scope with the existing lifetime model. `workspace/**` is itself transient custody and is already rejected from merge candidates by the separate workspace checker. Active planning/delivery context should retain the allowances already established by `AGENTS.md`; do not accidentally make this checker stricter than the semantic rule it implements.

If the exact safe syntax boundary differs after inspecting the current corpus, keep the invariant above fixed and choose the narrowest deterministic matcher that has no known false positives. Document the intentionally uncatchable semantic cases as review responsibility rather than adding heuristics.

## Required regression evidence

Tests should cover at least:

1. a durable Markdown document containing a full SHA plus a concrete `workspace/...` artifact path fails;
2. a durable non-Markdown text artifact containing the same kind of concrete coordinate fails if it is in checker scope;
3. ordinary historical SHA provenance without a `workspace/` artifact coordinate passes;
4. generic policy prose explaining that handoffs use "commit SHA + workspace path" passes;
5. ordinary mentions of `workspace/` pass when they are not concrete transient artifact coordinates;
6. durable delivery-history references such as merged PR references pass;
7. legitimate active/transient surfaces remain allowed according to the existing repository lifetime model;
8. malformed or partial hex strings do not accidentally become an overbroad SHA ban;
9. checker/test self-fixtures are handled without creating a production loophole.

Also run the checker against the current repository corpus so the implementation demonstrates that existing legitimate historical provenance remains accepted.

## CI integration

Run the checker and its test suite in the always-required repository-tooling path so docs-only changes cannot bypass it.

The current `classify` job already checks out full history, but this implementation should not need that history. Do not retain or introduce `fetch-depth: 0` merely for this checker; its correctness should be based on tracked content and reserved transient syntax, not commit ancestry.

Prefer an explicit, readable CI integration consistent with the existing transient-workspace checker and update `docs/development/testing.md` so the local command list and description match reality.

## Documentation reconciliation

### `AGENTS.md`

Generalize the current temporary-coordinate / durable-documentation rule so it clearly covers transient artifact identities, not only `PLAN-*` / `REV-*`.

Preserve the existing distinctions:

- active/delivery context may use temporary coordinates;
- durable semantic state must name the retained capability, contract, identity, invariant, behavior, artifact, or durable provenance directly;
- policy/process material may discuss the coordinate syntax itself;
- no checker can safely recognize every semantic dependency.

Do not turn `AGENTS.md` into a catalog of incident-specific examples.

### `docs/development/researching.md`

Preserve the current research custody contract:

- exact workspace `commit SHA + path` is required while the artifact is in active custody;
- copying that coordinate into maintained state does not preserve the artifact;
- reconciliation transfers retained knowledge to durable destinations;
- a merged PR may be durable delivery-history provenance;
- an artifact whose exact future readability matters must be deliberately promoted before workspace retirement.

Reconcile wording only enough to make this an instance of the general repository lifetime rule rather than a research-specific exception.

### `docs/development/reviewing.md`

Make the corresponding human-review responsibility explicit: durable assets must not semantically depend on transient coordinates even when no deterministic syntax checker can prove the dependency.

Do not duplicate checker implementation details here beyond what reviewers need.

### `docs/development/testing.md`

Document the new checker/test commands in the repository-tooling suites and state what the checker does and deliberately does not enforce.

## Issue #322 semantics

The original issue is valuable for the defect history, but its proposed invariant is superseded.

The implementation/PR must make the changed acceptance model unambiguous:

- the defect class is transient custody coordinates leaking into durable maintained state;
- current `main` ancestry is **not** the durability test;
- legitimate historical SHAs remain allowed;
- the executable guard covers the deterministic reserved-`workspace/` coordinate case;
- semantic leakage without a safe syntax signal remains a review concern.

Do not erase the historical rationale merely to make the old issue text appear retrospectively correct. In the PR description, explain that the solution intentionally replaces the proposed reachability proxy with the more general lifetime invariant. Use `Fixes #322` if the implemented guard and documentation fully close the issue's intended defect class under the current repository conventions.

## Non-goals

Do not:

- ban commit SHAs from maintained docs;
- require every SHA to be an ancestor of current `main`;
- validate arbitrary cited paths at historical commits;
- fetch deleted branches or PR refs;
- add a network/API dependency to repository-tooling CI;
- create a permanent registry/allowlist of approved historical SHAs;
- invent a new lifecycle/state machine for research or planning;
- archive transient workspace artifacts;
- broaden the task into unrelated documentation cleanup;
- change product Java code.

## Validation

Run the narrowest applicable validation for repository tooling/documentation changes.

At minimum, run the new checker test and checker itself plus every existing repository-tooling suite directly affected by the integration. Because the CI wiring/tooling list changes, also run the relevant always-required tooling validation documented in `docs/development/testing.md`.

Expected core commands will likely include:

```bash
python3 .github/scripts/check-transient-coordinates.test.py
python3 .github/scripts/check-transient-coordinates.py
python3 .github/scripts/check-transient-workspace.test.py
python3 .github/scripts/check-transient-workspace.py
python3 .github/scripts/check-delivery-labels.test.py
python3 .github/scripts/check-delivery-labels.py
bash .github/scripts/classify-changes.test.sh
```

If `.github/workflows/ci.yml` changes, run the repository's workflow-definition validation as required by the testing guide. Report any unavailable validation explicitly; do not summarize partial validation as a full pass.

No Java validation is required unless the implementation unexpectedly changes Java, which it should not.

## Delivery

Work on this existing branch. Before handing the implementation to independent PR review:

1. remove this transient prompt from `workspace/implementation/`;
2. run `check-transient-workspace.py` and confirm no tracked `workspace/` paths remain;
3. verify the repository owner's human Git identity is used for commits and no AI/session attribution trailers are present;
4. create or update a focused PR against `main`;
5. keep the PR description stable and semantic, explaining the #322 re-scope and validation performed;
6. stop after creating/updating the implementation PR and report the current transition; do not perform independent PR review yourself and do not merge.

## Final report

Report:

- the semantic invariant encoded;
- files changed;
- exact mechanical cases the new checker rejects;
- important cases it intentionally allows;
- how #322's original reachability proposal was superseded;
- validation commands and outcomes;
- PR number/URL and current head SHA;
- any residual semantic cases left to human review.
