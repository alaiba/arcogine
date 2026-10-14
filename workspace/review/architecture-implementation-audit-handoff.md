# Architecture implementation audit — handoff

You are running a read-only audit of Arcogine's architecture documentation against its implementation and tests. Work in a fresh session, re-ground from the live repository, and use parallel read-only subagents as described below. Produce one report, committed on this branch; change nothing else.

## Why this audit exists

The repository owner adopted a documentation principle: **architecture and specifications describe what the code implements.**

- Every rule, contract and described capability in `docs/architecture/` must be implemented and pinned by tests.
- Cross-cutting principles stay in architecture, but each must name its enforcement: a test, an architecture-conformance (ArchUnit) rule, a repository checker, or review only.
- Scope statements of what Arcogine does *not* do are true today and stay.
- A decided rule that the code does not implement belongs in its research record, with its question `READY`, until an implementation handoff lands it together with code and tests.

Some architecture content predates this principle and describes designed-but-unbuilt behavior. The owner wants every such gap found and flagged before deciding, per gap, whether to implement it, move it back into research, or delete it. **Your job is to find and evidence the gaps, not to fix them.**

A second question is still open with the owner: what to do with **built but unreachable code** — code that is implemented and tested but that no supported production path calls. Classify it separately and give the owner the facts each decision needs. Do not recommend a blanket rule.

## Repository rules

- Follow `AGENTS.md`. This run is read-only for product source, tests and maintained documentation.
- The only file you write is the report, `workspace/review/architecture-implementation-audit.md`, committed on this branch (`workspace/architecture-implementation-audit`) with the repository owner's human Git identity. Do not open a pull request, create or edit issues, or edit any other file.
- This branch is transient custody: it is never merged.

## Baseline

1. Resolve live `main` and record its exact SHA as the audit baseline. Audit `main`, not this branch's base.
2. Two pull requests change process documentation and may or may not have merged when you start: #487 (Implementer role; the "architecture describes what is implemented" rule in `docs/development/researching.md` and `docs/development/reviewing.md`) and #490 (retires `docs/planning/` and the Work Planner). Read whichever are still open for context, but audit only what is on `main`. If `docs/planning/` still exists on `main`, it is not an authority for this audit.
3. Read the open `CONS:` issues (`gh issue list --repo alaiba/arcogine --state open`). Several already flag architecture drift; cross-reference rather than duplicate them.

## Scope

Every document in `docs/architecture/`:

`overview.md`, `engine-semantics.md`, `runtime-contract.md`, `transfer-applicability.md`, `factory-design.md`, `factory-model.md`, `factory-resource-semantics.md`, `challenge.md`, `governance-conformance.md`, `governance-evidence.md`, `controlled-revisions.md`, `storage.md`, `operational-execution-digital-twin.md`, `operational-continuity.md`, `standards-alignment.md`, `isa-95-semantic-mapping.md`, `external-representations.md`

plus `docs/product/concepts.md`, which describes current capability. If a new architecture document appears on `main`, include it.

## Classification

Inventory each normative statement — every rule, contract, invariant, identity or equality rule, ordering rule, ownership statement and described capability — and give it exactly one class:

| Class | Meaning | Evidence required |
|---|---|---|
| **Implemented + pinned** | Production code implements it, reachable as defined below, and at least one test would fail if the behavior changed | The implementing class/method and the pinning test, as `path:line` |
| **Implemented, unpinned** | Implemented and reachable, but no test would catch a change | The implementing code, and the tests you checked |
| **Not implemented** | Describes behavior, a capability or a contract the code does not provide | The section, and what you searched for |
| **Unreachable** | Implemented, and possibly tested, but no production path from a supported entry point calls it, or nothing composes it | The code, its tests, and the absence of callers (search scope stated) |
| **Principle** | Cross-cutting constraint that current code is held to | Its enforcement — test, ArchUnit rule, checker, or "review only" |
| **Scope** | A true statement of what Arcogine does not do or does not own | None beyond confirming it is true |

**Reachable** means invoked on a production code path from a supported entry point: `FactoryRuntime` and the supported runtime contract (commands, observation, runtime events); Factory model construction, validation and publication; the Governance and Storage public APIs; the Challenge public API. Calls from tests or from `product/research-experiments` do not count. The research-experiments module is intentionally non-shipped research infrastructure and is out of scope for the unreachable class.

**Pinned** means a test asserts the specific behavior, so changing it fails the test. A test that merely exercises the code without asserting the rule does not pin it. Engine semantics §14 maps conformance fixtures to tests; verify that mapping rather than trusting it.

Be precise about partial cases. When a section mixes implemented and unbuilt parts, split it into separate rows. When a capability is implemented only as a fixture or in-memory variant while the document describes more, record the delivered part and the gap separately.

## Method: parallel read-only subagents

Fan out one read-only subagent per cluster. Each subagent reads its documents completely, searches the production code and tests, classifies every statement, and returns rows in the report format below. Subagents must not edit anything.

| Cluster | Documents |
|---|---|
| Engine | `engine-semantics.md`, `runtime-contract.md`, `transfer-applicability.md` |
| Factory and Challenge | `factory-design.md`, `factory-model.md`, `factory-resource-semantics.md`, `challenge.md` |
| Governance and Storage | `governance-conformance.md`, `governance-evidence.md`, `controlled-revisions.md`, `storage.md` |
| Operational | `operational-execution-digital-twin.md`, `operational-continuity.md` |
| Standards and representations | `standards-alignment.md`, `isa-95-semantic-mapping.md`, `external-representations.md` |
| Cross-cutting | `overview.md` (including Finance, the Determinism Contract, domain authority and the architecture guardrails) and `docs/product/concepts.md` |

Give each subagent the classification table, the reachability and pinning definitions, the evidence rules and the row format verbatim, so every cluster applies the same standard.

Then, in the lead session:

1. Merge the rows, and remove duplicates where two documents state the same rule.
2. Verify every **Not implemented** and **Unreachable** row yourself, and spot-check at least a fifth of the **Implemented + pinned** rows per cluster, more where a subagent's evidence looks thin. Correct any row that does not survive.
3. Resolve contradictions between clusters by reading the code.
4. Record any surface you could not inspect.

## Leads to verify (not findings)

These came from reading the documents during a planning review. They are unverified leads, not conclusions; confirm, correct or discard each one:

- Engine semantics §5–§13 and `transfer-applicability.md` specify spatial transfer execution. The Engine refuses a present spatial record. `TransferTiming` (§7 arithmetic) and the inbound admission-reservation substrate on `Machine` (§9) are implemented and tested but appear to have no runtime caller.
- `governance-conformance.md` §6, §8, §10, §11 and §12 (governed change and authorization, framework mappings, exceptions and risk acceptance, the pre-change authorization loop, audit snapshots) appear to be unbuilt. Governance evidence is implemented headlessly and in memory.
- `operational-execution-digital-twin.md` is indexed as "proposed", and `operational-continuity.md` is an adopted contract with no implementation.
- `runtime-contract.md` describes distribution hardening — retention, resume cursors, gap detection, checkpoint/restore — that may be unbuilt.
- The overview states that nothing delivers order completions to `FinanceHandler`, so the Finance domain may be uncomposed.
- The Factory model's optional spatial record is published, validated and fingerprinted, but no runtime can execute a model that carries it.
- The ISA-95 mapping, standards alignment and external representations documents may describe intended mappings or formats as current. Open `CONS:` issues already flag some drift there.

## Report

Write `workspace/review/architecture-implementation-audit.md`:

1. **Baseline:** the `main` SHA, the date, which of #487 and #490 had merged, and any surface not inspected.
2. **Summary:** a table of statement counts by document and class, and a short paragraph on the overall state.
3. **Gaps,** numbered `Gap 1`, `Gap 2`, … and grouped by class in this order: Not implemented, Unreachable, Implemented-unpinned, Principle with review-only enforcement where a mechanical check looks feasible. For each gap:
   - document and section (`path:line`);
   - the statement, quoted briefly or paraphrased precisely;
   - evidence: the code, the tests, and what was searched;
   - **proposed outcome:** one of implement through a research handoff; move to its research record (name the investigation and register row); delete from architecture; add a test; or reword as a scope statement. Add a one-line reason;
   - **consequences** of that outcome: research-register rows whose `CONCLUDED` status rests on this text and would revert to `READY`; canonical-form or golden-vector changes; other documents that cite the section; overlapping `CONS:` issues.
4. **Unreachable code:** one section per item, giving its size in production and test lines, what it implements, what would need to change to make it reachable, what depends on it (code, golden vectors, documents), and the cost of removing it versus keeping it. Leave the decision to the owner.
5. **Implemented + pinned:** a compact per-document table of rule, implementing code and pinning test, so the owner can see what is solid.
6. **Research-register impact:** every register row whose recorded destination is architecture text found unbuilt.

Keep the report factual. It proposes outcomes; it does not decide them, and it states uncertainty where the evidence is incomplete.

## Completion

Commit the report on this branch with the owner's human Git identity and push it. Return the branch, the exact report commit SHA, the report path and the audit baseline SHA, plus a short chat summary of the gap counts by class and the three gaps you consider most consequential. Do not paste the full report into chat.
