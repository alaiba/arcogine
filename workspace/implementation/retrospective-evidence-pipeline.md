# Implementation handoff: mechanical retrospective evidence pipeline

## Repository and starting point

Repository: `alaiba/arcogine`

Handoff branch: `workspace/implementation/retrospective-evidence-pipeline`

Handoff baseline when this prompt was created: live `main` at `91996293ba57fe84805fa4ae691053a1c51c1c55`.

Re-resolve live `main` before implementation and follow the current `AGENTS.md` lifecycle/base-freshness rules if it has advanced. Preserve this handoff commit while it is in active custody; do not rebase or force-push it away. Before independent PR review, remove this `workspace/implementation/` prompt from the candidate tree and run the repository transient-workspace check.

## Read first

Read these current-revision authorities before editing:

1. `AGENTS.md`
2. `docs/development/continuous-improvement.md`
3. `.github/agents/pr-reviewer.agent.md`
4. `docs/development/reviewing.md`
5. `docs/development/testing.md`
6. `infra/dev/delivery-retrospective.mjs`
7. `infra/dev/delivery-retrospective.test.mjs`
8. `.github/continuous-improvement/retrospective.json`

Quick-search the repository/docs for at least:

- `delivery-retrospective`
- `Mechanical evidence first`
- `CHANGES REQUIRED`
- `REV-`
- `Category:`
- `Severity:`
- `Confidence:`
- `finding lifecycle`
- `DOCUMENTATION_ACCURACY`
- `retrospective.json`

Use search as discovery only; fetch/read material paths from the exact revision you are implementing against before relying on them.

## Problem

Arcogine already has two useful pieces that are not joined strongly enough:

- the PR Reviewer emits structured actionable findings with `Severity`, `Category`, `Confidence`, reviewed `Head`, `Subject`, and lifecycle continuity via `REV-<NNN>`;
- the delivery retrospective has executable standard work in `infra/dev/delivery-retrospective.mjs`, but today that helper mechanically counts only the retrospective PR window, trusted `CHANGES REQUIRED` review submissions, and 0/1/2/3+ blocking-review checkpoints.

This means retrospective analysis leaves mechanically available reviewer evidence unused and may require hand-reading/hand-counting review bodies to discover patterns such as repeated `P2 DOCUMENTATION_ACCURACY` findings.

At the same time, the current helper combines GitHub acquisition and retrospective analysis in one program. That prevents a future execution environment with a different GitHub transport (for example, a ChatGPT GitHub connector) from reusing the same deterministic retrospective analysis without reimplementing its semantics.

## Goals

Implement both goals as one coherent slice:

1. **Make structured PR-review findings first-class retrospective evidence.**
   Mechanically consume reviewer-produced fields, especially but not limited to `Category`, so the retrospective can reproducibly inspect distributions and per-PR incidence instead of hand-classifying review prose.

2. **Make the retrospective process mechanical standard work.**
   Separate source-specific GitHub acquisition from a pure, deterministic evidence validator/aggregator/output producer. The same source-neutral evidence contract must be usable by the local coding-agent path now and by a future connector-backed acquisition path later.

The end-state architecture is:

```text
GitHub GraphQL / gh-backed local acquisition
                    |
                    v
       Retrospective Evidence v1
        source-neutral JSON contract
                    |
             schema validation
                    |
                    v
          pure retrospective analyzer
                    |
          deterministic JSON/output
                    |
                    v
        retrospective interpretation
```

A future connector adapter should be able to replace only the acquisition step:

```text
GitHub connector calls
        |
        v
Retrospective Evidence v1
        |
        v
same pure analyzer
```

Do **not** implement connector support in this slice. Design the evidence boundary so adding it later does not require changing retrospective semantics.

## Architectural constraints

### 1. Acquisition and analysis are separate authorities

Create an explicit source-specific GitHub acquisition layer and a source-neutral analysis layer.

The acquisition layer may know about:

- GitHub GraphQL/API details;
- `GH_TOKEN` / `GITHUB_TOKEN` / `gh auth token`;
- GitHub pagination and search limits;
- GitHub review-connection limits;
- repository-local baseline selection from `.github/continuous-improvement/retrospective.json`;
- how to prove retrieval completeness from GitHub's transport metadata.

It must **not** know retrospective semantics such as:

- what a closing `CHANGES REQUIRED` means;
- trusted blocking-review counts;
- review-checkpoint buckets;
- `REV-<NNN>` finding parsing;
- severity/category/confidence distributions;
- finding deduplication/lifecycle meaning.

Those belong in the pure analyzer.

Conversely, the analyzer must perform no GitHub/network access and must not depend on `gh`, a Git checkout, or mutable repository state. Given identical evidence JSON, it must produce identical analytical output.

### 2. Introduce a versioned source-neutral evidence contract

Define a small, explicit `Retrospective Evidence v1` JSON contract between acquisition and analysis.

Prefer an executable contract/validator owned under `infra/dev/` rather than duplicating a large schema in prose. A module such as `infra/dev/delivery-retrospective-evidence.mjs` is appropriate if it cleanly owns:

- the schema version;
- structural validation;
- completeness invariants;
- normalized field expectations used by both acquisition and analysis.

The exact filenames may differ if a clearer structure emerges, but keep one narrow authority for the evidence contract.

The contract should be **minimally normalized transport data**, not a raw GraphQL response and not pre-analyzed retrospective output. GraphQL and a future connector will expose different transport shapes; both should adapt into this contract.

At minimum the contract must carry enough source data for the analyzer to establish:

- repository identity;
- requested baseline PR and through PR;
- baseline and through endpoint PR facts, including merge timestamp and target branch;
- the complete candidate merged-PR set covering the boundary dates;
- for each candidate PR: number, merge facts/target branch, and a complete submitted-review collection;
- for each review: stable review identity when available, body, author association, submission time, and reviewed commit/head identity when available from GitHub;
- source/provenance metadata sufficient to diagnose which acquisition adapter produced the bundle;
- explicit completeness/count metadata for candidate and review collections.

Do not parse disposition or finding fields in the acquisition layer. Preserve the review body for the analyzer.

### 3. Preserve fail-closed completeness

The current helper intentionally refuses partial GitHub data. Preserve and strengthen that property across the new boundary.

The acquisition adapter is responsible for asserting collection completeness only when its source semantics prove it. The evidence contract should make completeness explicit.

The analyzer must reject internally inconsistent evidence, including cases such as:

- collection marked complete but reported count differs from item count;
- missing required endpoint/candidate/review fields;
- duplicate PR identities in a collection where uniqueness is required;
- an endpoint that is not a merged `main` PR;
- evidence schema version it does not support.

A future connector-backed producer will be required to meet the same contract. Do not bake GraphQL-specific pagination fields into retrospective semantics merely because the first producer uses GraphQL.

### 4. Keep exact-window construction in the analyzer

The GitHub adapter may need the baseline/through merge dates to issue a day-bounded search, but the exact retrospective window remains analysis semantics.

Have acquisition return the complete candidate set covering the relevant boundary dates. The pure analyzer should continue to enforce the exact interval:

`(baseline merge time, through-PR merge time]`

and target branch `main`, just as the current helper does.

### 5. Preserve existing mechanical metrics

Do not regress current helper behavior. The pure analyzer must continue to own and output:

- exact main-target PR window;
- merged PR count;
- trusted-author `CHANGES REQUIRED` submission count;
- per-PR 0 / 1 / 2 / 3+ blocking-review checkpoint distribution;
- exact PR list/order.

Retain the current trusted author-association semantics unless current repository authority says otherwise.

Preserve the distinction between retrospective historical counting and the stricter current-head disposition gate.

## Structured finding analytics

### 6. Parse canonical PR Reviewer findings mechanically

The analyzer should parse reviewer-produced actionable finding blocks, using the current PR Reviewer contract as authority.

For a canonical finding, mechanically capture at least:

- PR number;
- `REV-<NNN>` identifier;
- severity;
- category;
- confidence;
- reviewed head;
- subject;
- first review identity/submission time where available;
- current/final lifecycle status where it can be established from the canonical review lifecycle format.

Do not make the GitHub acquisition adapter parse these fields.

Use exact structured fields, not semantic NLP classification of arbitrary prose.

### 7. Make finding identity stable enough for aggregation

Clarify the PR Reviewer contract as needed so that, within a single PR's review lifecycle:

- a `REV-<NNN>` identifies one semantic defect;
- the same identifier is carried across re-review while that finding is reconciled;
- an identifier is never reused for a different defect;
- genuinely new defects receive new identifiers.

The natural analytical identity should therefore be:

`PR number + REV identifier`

Do not invent a new issue ledger or repository-wide finding database.

If the existing re-review reconciliation syntax needs a small tightening for deterministic lifecycle parsing, make the narrowest compatible contract change and test it. Preserve human readability.

### 8. Produce reproducible finding metrics

Add deterministic finding analytics suitable for retrospective interpretation. At minimum include:

- total distinct structured findings;
- number of PRs with at least one structured finding;
- findings by severity;
- findings by category;
- findings by severity × category;
- findings by confidence;
- PR incidence by category;
- PR incidence by severity × category.

Both raw finding count and PR incidence are required: one PR with many findings must not masquerade as broad recurrence.

The JSON output should also preserve a normalized distinct-finding dataset so the retrospective can inspect exactly which PR/finding records produced an aggregate without re-reading every review body.

Do not make any category or severity an optimization target. The helper reports facts; it does not decide that a high count is waste.

### 9. Make parsing quality visible

Do not silently drop malformed or noncanonical reviewer data.

Expose explicit finding-data diagnostics/coverage, including as applicable:

- trusted review count;
- trusted blocking-review count;
- structured findings parsed;
- blocking reviews with no parseable canonical finding;
- malformed finding blocks;
- duplicate/conflicting `PR + REV` identities;
- unknown/noncanonical category values;
- unsupported severity/confidence/status values.

Preserve literal unknown historical category values in evidence/diagnostics; do not silently map them to a current category.

Choose a fail-closed policy that keeps claims truthful. In particular, the analyzer must never report a category/severity distribution as complete if it has evidence that structured finding data for the applicable population is incomplete or contradictory. It is acceptable to keep core window/review metrics available while explicitly marking finding analytics incomplete if that is the smallest truthful compatibility behavior; document and test the chosen rule.

## Local coding-agent workflow

Implement the local GitHub producer first.

A suitable shape is:

```bash
node infra/dev/delivery-retrospective-github.mjs \
  --through-pr <number> \
  --output logs/retrospective-evidence.json

node infra/dev/delivery-retrospective.mjs \
  --input logs/retrospective-evidence.json \
  --json
```

The exact CLI may differ if you find a simpler interface, but preserve these properties:

- the producer can emit the evidence contract without analysis;
- the analyzer can consume a previously produced evidence bundle without any GitHub access;
- the evidence file can live under ignored `logs/` and need not become repository history;
- a future connector agent can produce the same contract and call the same analyzer;
- local standard work remains simple enough to run reliably.

A convenience wrapper is acceptable only if it composes the same clean layers and does not become a second implementation of retrospective semantics. The pure analyzer/input path must remain independently invocable and tested.

## Retrospective operating-model update

Update `docs/development/continuous-improvement.md` so the formal delivery-process retrospective describes the new mechanical standard work.

The maintained process should establish, before interpretation:

1. exact through PR;
2. complete source-neutral evidence bundle produced by an approved acquisition path;
3. successful evidence validation;
4. successful pure retrospective analysis;
5. use of the analyzer's mechanical window/review/finding dataset without hand-reconstructing alternative totals.

Update the current statement that hand-classified finding totals/category percentages are not standard baseline metrics. Preserve the underlying safeguard:

- arbitrary hand classification remains noncanonical;
- mechanically parsed structured reviewer fields may now be used as reproducible retrospective evidence;
- healthy adversarial findings are not automatically waste;
- interpretation and improvement decisions remain human/agent retrospective reasoning.

Do not add a standing cadence, due state, intervention register, or second planning system.

## Tests and proving cases

Use Node's existing repository-tooling test style. Add focused tests around each layer.

At minimum prove:

### Evidence contract / validation

- supported schema version accepted;
- unsupported version rejected;
- complete collection with matching counts accepted;
- `complete=true` with count mismatch rejected;
- malformed/missing endpoint facts rejected;
- duplicate PR identity rejected where applicable;
- incomplete review collections cannot silently produce complete retrospective claims.

### GitHub acquisition

Factor source-specific retrieval enough that tests do not require live GitHub.

Prove:

- GraphQL pagination remains complete/stable;
- >1000 GitHub search result bound still fails closed;
- >review-page/connection limit behavior fails closed rather than truncating;
- endpoint facts and candidate/review transport data are serialized into Evidence v1 without disposition/finding interpretation;
- useful review metadata needed by the analyzer is fetched.

### Existing retrospective semantics

Port/preserve the current tests for:

- merge-timestamp exact-window selection;
- target-branch filtering;
- closing disposition parsing;
- ignoring prose/examples/blockquote/non-closing dispositions;
- trusted author-association filtering;
- current 0/1/2/3+ checkpoint totals.

### Structured findings

Prove at least:

- canonical `P2 DOCUMENTATION_ACCURACY` block parses correctly;
- severity/category/confidence/subject/head are captured;
- multiple findings in one review parse separately;
- the same `PR + REV` carried through re-review is one distinct finding, not multiple findings;
- a genuinely new ID is a new finding;
- per-category and severity×category counts are correct;
- PR incidence is deduplicated per PR;
- unknown category values are preserved/reported rather than silently remapped;
- malformed canonical-looking finding data cannot silently disappear;
- conflicting reuse of one `PR + REV` identity is detected;
- lifecycle reconciliation behaves deterministically if included in v1.

### Source neutrality / purity

Add an explicit proving case that two evidence bundles with identical semantic content but different producer metadata (for example `github-graphql` versus a fixture representing a future `github-connector`) produce identical retrospective analytical results.

Also prove that the analyzer can run entirely from an evidence file/fixture with no GitHub token, no `gh`, and no network.

## Validation

Run the narrowest applicable repository-owned validation while iterating, then the relevant complete repository-tooling checks described by current `AGENTS.md` / `docs/development/testing.md`.

At minimum run the affected Node test files directly while developing. Before handoff, run the repository tooling suite that contains these tests and any static/workflow checks affected by changes.

If a required validation command cannot run in the environment, report that precisely; do not substitute a weaker claim.

## Documentation/reconciliation requirements

Keep authority placement clean:

- executable evidence-contract details belong in the contract/validator and tests;
- `docs/development/continuous-improvement.md` owns the retrospective operating model and should point to executable authority rather than duplicate a volatile field-by-field schema;
- `.github/agents/pr-reviewer.agent.md` owns the reviewer finding format/lifecycle invariant;
- do not copy GraphQL query details into maintained process prose;
- do not create a new durable retrospective data store.

Search semantic neighbors before finalizing so no maintained text still says the helper inherently performs GitHub retrieval if the proposed state separates those responsibilities.

## Non-goals

Do not:

- implement ChatGPT/GitHub-connector acquisition in this slice;
- add a database, issue ledger, or persistent archive of retrospective evidence bundles;
- change the trusted current-head PR disposition gate;
- redefine severity semantics or invent a new review taxonomy merely for analytics;
- infer categories from unstructured prose with NLP;
- optimize the process for fewer review findings;
- create a new retrospective cadence/threshold/due-state mechanism;
- expand this into a general analytics framework;
- retain this implementation prompt in the merge candidate.

## Acceptance criteria

The slice is complete when all of the following are true:

1. GitHub acquisition is a distinct source-specific layer that emits a versioned source-neutral evidence bundle and contains no retrospective finding/disposition aggregation semantics.
2. A pure analyzer consumes only that evidence bundle and deterministically produces all existing retrospective metrics.
3. The pure analyzer mechanically extracts the PR Reviewer's structured findings and produces reproducible severity/category/confidence plus PR-incidence analytics with visible parsing/coverage diagnostics.
4. `PR number + REV identifier` is mechanically safe as a finding identity because the reviewer contract explicitly prevents identifier reuse within one PR lifecycle.
5. Incomplete or contradictory source/finding evidence cannot silently produce apparently complete statistics.
6. The local coding-agent path using GitHub credentials/`gh` is documented and executable as standard work.
7. The architecture explicitly permits a future connector acquisition path to produce the same evidence contract and reuse the exact analyzer unchanged.
8. Existing retrospective metrics remain behaviorally compatible unless a current authority requires a deliberate correction.
9. Focused tests cover acquisition, contract validation, existing metrics, structured finding analytics, identity/lifecycle behavior, diagnostics, and source-neutral analyzer purity.
10. `docs/development/continuous-improvement.md` describes the new mechanical-evidence workflow without turning finding count into an improvement target.
11. No tracked `workspace/` artifact remains in the final review candidate.

## Final implementation report

When implementation is complete, report:

- exact branch and current head SHA;
- files changed and the responsibility of each new/refactored module;
- the final Evidence v1 contract boundary in concise terms;
- the local standard-work commands;
- structured finding metrics now produced;
- how incomplete/malformed finding evidence is surfaced;
- tests/validation run and results;
- any compatibility choice made for historical/noncanonical review bodies;
- confirmation that the prompt was removed from the review candidate;
- current PR number if opened, without claiming merge readiness before independent review.
