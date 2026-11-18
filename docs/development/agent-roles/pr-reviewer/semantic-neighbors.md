# Semantic-neighbor analysis

Part of the [PR Reviewer contract](../pr-reviewer.md#review-procedure); every complete review, re-review and `./` continuation follows all of its routed topics.

When a surface changes, inspect maintained or executable surfaces that encode or expose the same concept. At minimum consider:

| Changed surface | Mandatory neighbors to consider |
| --- | --- |
| `FactoryModel` / model semantics | factory-design architecture, the Factory Model specification, Engine Semantics assumptions, product concepts, provenance/identity contracts, related research-register questions, tests |
| routing/resource eligibility or execution decomposition | `docs/product/concepts.md`, factory-design architecture, Engine Semantics and its conformance fixtures, runtime/dispatch tests, consumer contracts, the issue or handoff the change implements |
| `FactoryRuntime`, handlers, orders, jobs | architecture overview, the runtime and Engine Semantics contracts, acceptance/integration tests, consumer contracts, the issue or handoff the change implements |
| events, scheduler, observations | event/state/observation architecture, supported runtime contract, determinism tests |
| module dependencies | architecture module graph, executable architecture rules, domain/challenge boundaries |
| challenge domain | Challenge contract, Game consumer research, Engine-vs-Challenge boundary |
| governance/conformance | Governance architecture and evidence contract, the identity/revision/fingerprint specifications, evidence/findings semantics, related research-register questions |
| operational/digital-twin work | Operational architecture and continuity contract, Operational boundary research, security/authority concerns, governance boundary |
| outward controllers and projections, if present | runtime contract, relevant adapter references, integration tests |
| scenario/input format (if implemented) | approved input contract, product concepts, format-specific tests |
| `./arcogine` commands | README, CONTRIBUTING, testing guide, AGENTS.md |
| Gradle / Java / Node policy | executable configuration, CI, devcontainer/runtime policy, maintained development docs |
| CI workflows/checks | testing guide, CONTRIBUTING, AGENTS.md where workflow depends on checks |
| architecture/specification changed | the Architecture Overview and every specification the change's semantics reach, the research register, maintained product concepts/reference surfaces, implementation evidence where claimed; if the change carries a conclusion from a `docs/research/` report into architecture, also `docs/development/researching.md` and that report's adversarial-review status |
| research or issue status changed | acceptance criteria, implementation, tests/evidence, architecture/current docs, maintained product concepts/reference surfaces affected by the status claim |

A semantic neighbor is not automatically required to change. Inspect it and determine whether its existing statement remains correct.

## Concept fan-out for semantic changes

For medium- and high-risk semantic changes, do not rely only on file adjacency or changed-symbol references.

1. Identify 2-5 material concepts whose meaning, ownership, lifecycle, cardinality, status, or authority changed.
2. Search maintained repository surfaces for the new vocabulary and for plausible old assumptions or prior-state wording.
3. Inspect hits in current architecture, research state, product concepts, reference docs, examples, tests, interfaces/DTOs, and executable configuration as relevant.
4. Distinguish a semantically stale statement from harmless alternate wording.
5. Record the important semantic neighbors inspected, including surfaces that were checked and correctly required no change.

Examples of useful old-assumption searches include singular/plural cardinality shifts, old API/type names, `unresolved`/`deferred`/`partial` wording after a status transition, and phrases that encode an older ownership boundary.

## Authority/status transition propagation

Treat an authority-bearing transition as a mandatory propagation trigger. Examples include:

- a constraint moving from research or proposal into current architecture;
- readiness/capability `partial/blocked -> implemented/ready`;
- compatibility behavior `legacy/default -> removed/redefined`;
- architecture ownership or identity semantics becoming binding.

When such a transition occurs, explicitly inspect current architecture, directly related status tables, research-register rows, maintained product concepts, reference surfaces, and implementation/evidence claims that may still describe the prior state.

This is bounded PR-impact review. It is not a substitute for the repository-wide Consistency agent.

## Research-backed architecture reconciliation

When a PR carries a conclusion from a `docs/research/` report into canonical architecture or a specification, treat the prerequisite in `docs/development/researching.md` as a review gate, not merely as a fact to note. Identify the exact research-report evidence coordinate whose conclusions are being promoted, including its report commit SHA, and identify the report's risk tier.

For a high-risk reconciliation, verify that a genuinely independent adversarial-review artifact exists, states `ACCEPT` or `ACCEPT WITH QUALIFICATIONS`, and explicitly identifies that same report commit SHA as the reviewed report. A branch name, branch tip, report path, conversational summary, or review link without the reviewed-report SHA is not enough. If the report file was committed again after the reviewed revision, treat the later commit as a distinct report revision: either the promotion must bind to the already-reviewed report SHA, or the later revision must receive its own required adversarial review before its conclusions may be promoted. Do not infer that a prior disposition automatically transfers across a later report commit, including a metadata-only edit that adds review status or a link.

Treat a missing, self-administered-only, `MORE EVIDENCE REQUIRED`, `REOPEN`, or report-SHA-mismatched review as a blocking finding for a high-risk reconciliation. When the disposition is `ACCEPT WITH QUALIFICATIONS`, verify the PR's proposed architecture text actually carries those qualifications forward rather than silently dropping them. This does not make the reviewer perform research or adversarial review itself; it only enforces that the prerequisite was met for the exact evidence revision being promoted.
