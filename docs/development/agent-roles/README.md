# Specialized agent role contracts

The Markdown procedures in this directory are the **canonical, provider-neutral** definitions of Arcogine's specialized roles. `AGENTS.md` selects the appropriate procedure by task and invocation. Every execution environment follows the same procedure, authority boundaries, permissions, validation, and handoff rules; repository state and GitHub evidence are authoritative regardless of provider.

## Specialized agent roles

Some repository tasks have additional repository-owned operating contracts. Their canonical
procedures live in [`docs/development/agent-roles/`](README.md)
and apply regardless of the model or execution provider. Runtime-specific profiles,
including `.github/agents/` GitHub Copilot adapters, only load those procedures; they
do not replace or override them.

- **Dependency maintenance:** when asked to process, apply, remediate, or sweep dependency updates or Dependabot pull requests, read and follow [`docs/development/agent-roles/dependency-maintainer.md`](dependency-maintainer.md) in addition to this file.
- **Continuous improvement:** when the user says `Assess continuous improvement.`, read and follow [`docs/development/agent-roles/continuous-improvement.md`](continuous-improvement.md) in addition to this file.
- **Consistency review:** when asked to perform a repository consistency
  review, documentation/architecture reconciliation, periodic consistency
  sweep, or to operate as the consistency agent, read and follow
  [`docs/development/agent-roles/consistency.md`](consistency.md)
  in addition to this file.
- **PR review:** when asked to independently review, re-review, or assess merge
  readiness of a pull request, read and follow
  [`docs/development/agent-roles/pr-reviewer.md`](pr-reviewer.md)
  in addition to this file. This is the repository's dedicated **PR Reviewer**
  role; do not substitute a generic coding-agent review when the contract is
  available.
- **Research:** when asked to investigate a bounded Arcogine research question,
  execute a research brief from `docs/research/`, produce a decision-quality
  research report, or perform an independent adversarial review of an existing
  research report, or write the supporting implementation handoff for a concluded question,
  read and follow
  [`docs/development/agent-roles/researcher.md`](researcher.md)
  in addition to this file. That contract defers to
  [`docs/development/researching.md`](../researching.md) for the
  normative research method. Research investigation and adversarial research
  review belong to this role, not to Consistency. Research never
  changes production code.
- **Implementation:** when asked to implement an issue, or to continue an
  implementation pull request with `..`, read and follow
  [`docs/development/agent-roles/implementer.md`](implementer.md)
  in addition to this file.

There is no separate planning role. Known work waits in GitHub issues and open questions in the
research register; the repository owner chooses what to start. A request to recommend next work is
answered from open issues, `READY` research questions, open pull requests and their live
review/CI state.

Specialized agent contracts supplement `AGENTS.md`; they do not override
repository architecture, contribution, documentation, or executable
authorities.

## Optional runtime adapters

A provider-specific adapter may handle discovery, model/tool configuration, and loading of one canonical procedure. It must **link to and read that procedure**, fail clearly if it is unavailable, and must not copy, narrow, supersede, or change any repository-owned role behavior. Provider-specific metadata belongs only in such adapters, never in canonical role contracts.

The existing GitHub Copilot profiles in `.github/agents/` remain as thin adapters for the four roles they already exposed. Consistency has no Copilot adapter: its formal run still requires the complete snapshot-backed evidence view specified by its canonical procedure. A new adapter is optional and is valid only if the runtime actually provides that role's required capabilities. Do not treat a missing adapter as a missing role.
