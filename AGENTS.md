# AGENTS.md

Repository: `alaiba/arcogine`. Use scoped GitHub operations without rediscovery or asking for its URL. Global discovery only for cross-repository work or operations unresolvable here. Verify search hits at the exact target ref.

## Always

- Never merge PRs. Only the owner merges; branch deletion needs separate explicit owner authorization.
- Read merge gates live for the current head: trusted `disposition` green, `CI / gate` green, head contains live base, GitHub mergeable. CI/comments alone confer no authorization.
- Verify human-owner Git identity before commits. No bot/model attribution, session URLs or tool footers in commits/GitHub text; inspect before posting.
- Do not edit `product/**/build/`; regenerate `product/gradle/wrapper/gradle-wrapper.jar` and `.properties` via `./gradlew wrapper`, and `.devcontainer/devcontainer-lock.json` via Dev Containers CLI.
- Simulation is deterministic; seeded RNG only.

## Read when applicable

- Implementation, research, independent PR review (`./`), dependencies, consistency, or `Assess continuous improvement.`: select the [role contract](docs/development/agent-roles/README.md).
- For shorthand/next work, admission/branches, coordination, prompts/handoffs, artifact custody/cleanup, durable naming, commits/GitHub text, PR continuation (`..`)/validation/completion, commands/toolchains/module boundaries, session close (`.?`), or instruction edits: select the matching [topic rules](docs/development/agent-rules/README.md). These rules are mandatory before acting. Shorthand matches the entire message.
- For orientation read [README](README.md#what-is-arcogine-today); when contributing read [workflow](.github/CONTRIBUTING.md#development-workflow), style and guardrails. Do not duplicate them here.

Canonical Bash entry: `./arcogine --help`, `check`, `check --full`. On Windows inspect running Docker containers first, then follow the environment topic's fallback order.
