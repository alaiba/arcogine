# Repository layout

Part of the [contributor guide](../../../.github/CONTRIBUTING.md).

| Directory | Purpose |
|-----------|---------|
| `product/types/` | Shared types, typed IDs, error definitions |
| `product/governance/` | Controlled revisions, semantic change, requirements, conformance, and evidence use |
| `product/storage/` | Arcogine-owned built-in persistence behind public storage and Governance contracts |
| `product/simulation/` | Event engine and scheduler |
| `product/domains/factory/` | Canonical factory model and publication, `FactoryRuntime`, machines, jobs, routing, queues |
| `product/consumer/challenge/` | Headless, game-owned challenge definitions, catalogue/economics, admissibility, evaluation, and attempt comparison — no dependency on any module above |
| `product/consumer/challenge-factory-integration-test/` | Test-only proof module: Factory publication validity and challenge admissibility are independent axes |
| `product/consumer/last-light-foundry/` | Outward game consumer: JDK HTTP adapter and browser client over the Challenge, Factory and runtime APIs; nothing depends on it |
| `product/research-experiments/` | Non-shipped research infrastructure: the deterministic research-experiment substrate over the supported runtime contract; never a production dependency |
| `product/architecture-conformance-test/` | Test-only module: durable cross-domain ArchUnit guardrails |
| `docs/` | Project documentation |
| `infra/` | Dev-environment infrastructure |

Keep the repository root limited to primary entry points, standard discovery/configuration files, and files whose tools conventionally expect them there. CI, test-support, and security-support files should prefer `.github/` when their paths are explicitly controlled. Do not move conventionally discovered files merely to reduce visual clutter. For example, `.trivyignore` stays at the root because Trivy conventionally discovers it there, while the Gitleaks config can live under `.github/security/` because Arcogine invokes it with an explicit `--config` path.

See [`docs/architecture/overview.md`](../../architecture/overview.md) for the full module dependency graph and design rationale.
