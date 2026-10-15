# Specialized agent role contracts

The Markdown procedures in this directory are the **canonical, provider-neutral** definitions of Arcogine's specialized roles. `AGENTS.md` selects the appropriate procedure by task and invocation. Every execution environment follows the same procedure, authority boundaries, permissions, validation, and handoff rules; repository state and GitHub evidence are authoritative regardless of provider.

| Role | Canonical procedure |
| --- | --- |
| Consistency | [consistency.md](consistency.md) |
| Continuous Improvement | [continuous-improvement.md](continuous-improvement.md) |
| Dependency Maintainer | [dependency-maintainer.md](dependency-maintainer.md) |
| Implementer | [implementer.md](implementer.md) |
| PR Reviewer | [pr-reviewer.md](pr-reviewer.md) |
| Researcher | [researcher.md](researcher.md) |

## Optional runtime adapters

A provider-specific adapter may handle discovery, model/tool configuration, and loading of one canonical procedure. It must **link to and read that procedure**, fail clearly if it is unavailable, and must not copy, narrow, supersede, or change any repository-owned role behavior. Provider-specific metadata belongs only in such adapters, never in canonical role contracts.

The existing GitHub Copilot profiles in `.github/agents/` remain as thin adapters for the four roles they already exposed. Consistency has no Copilot adapter: its formal run still requires the complete snapshot-backed evidence view specified by its canonical procedure. A new adapter is optional and is valid only if the runtime actually provides that role's required capabilities. Do not treat a missing adapter as a missing role.
