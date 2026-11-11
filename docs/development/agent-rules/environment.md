# Layout

- `product/` — all executable product source.
  - Gradle multi-module Java backend (compatibility baseline in [`docs/development/testing.md`](../testing.md#prerequisites-and-version-policy)) rooted here: `types`, `governance`, `storage`, `simulation`, `domains/factory`, `consumer/challenge`, `consumer/challenge-factory-integration-test`, `research-experiments`, `architecture-conformance-test`. `research-experiments` is non-shipped research infrastructure: the substrate for deterministic research experiments over the supported runtime contract, which no production or consumer source set depends on (see `docs/development/researching.md`). There is currently no application server, HTTP API, or CLI product surface — retained executable evidence is tests, conformance checks, and benchmarks; a future outward consumer is introduced from the supported runtime contract (`docs/architecture/runtime-contract.md`) when a concrete product need exists.
- `docs/` — architecture, product, development, research, and history docs. Read `docs/architecture/overview.md` before touching cross-module boundaries.
- `infra/` — dev-environment infrastructure: `infra/dev/claude-cloud.sh` (Claude Cloud environment provisioning) and related repository tooling.

## Canonical commands

`./arcogine` (run from the repo root) is the canonical developer entry point; run `./arcogine --help`
or see [`docs/development/testing.md`](../testing.md) for the exact command list,
native subsystem commands (`cd product && ./gradlew <task>`, `trivy`, `gitleaks`), and the JDK-21
Docker workflow that backend validation needs when the host JDK is older.

`./arcogine` is a Bash script — it works in the dev container, on Linux/macOS, and via WSL/Git Bash
on Windows, but not directly in PowerShell/cmd. On a Windows host, or when the current JDK is below
21, prefer execution environments in this order when available: (1) the devcontainer, (2) a generic
ad hoc Docker container, (3) WSL/Git Bash, and (4) native Windows tooling. Before running shell- or
toolchain-dependent commands on Windows, inspect the running Docker containers first and identify the
container that mounts this repository; do not assume a container name. If no suitable devcontainer is
running, try the documented generic Docker workflow, then WSL/Git Bash, and finally native Windows
tooling when the command supports it. Classify a host Gradle failure as environmental only when it is
attributable to the unsupported or missing JVM; otherwise investigate it as a build or product
failure.

## Conventions worth knowing

- **Gradle** has one true source: `product/gradle/wrapper/gradle-wrapper.properties`. Both `gradlew` and `gradlew.bat` read it, and no Gradle is installed via the devcontainer feature — don't add one back.
- **Trivy and Gitleaks** are environment/security tools pinned independently in the devcontainer and CI. When intentionally changing either tool version, grep the repository for the old version and keep the relevant devcontainer/CI install sites aligned.
- Architecture guardrails (module dependency direction, event/state/observation boundaries) are documented in [.github/CONTRIBUTING.md](../../../.github/CONTRIBUTING.md#architecture-guardrails) and enforced by `architecture-conformance-test`'s ArchUnit `ArchitectureTest`. Read that section before adding a new domain.
- The simulation must stay deterministic (seeded RNG only) — see `docs/architecture/overview.md`.
