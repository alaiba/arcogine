# Development workflow

Part of the [contributor guide](../../../.github/CONTRIBUTING.md).

The managed devcontainer and Claude Cloud setup install a pre-commit check that confirms `gh` is authenticated as the repository's human owner and that the configured, author, and committer emails belong to that account. It accepts verified account emails, the public profile email, and the account's GitHub noreply addresses; private secondary emails require `gh`'s `user` scope. In other environments, install it once with `bash infra/dev/install-git-hooks.sh`.

1. **Branch** from `main` with a descriptive name (`feature/xyz`, `fix/abc`); every change to `main` starts from an issue (the one owner-approved exception is the Dependabot update path described in [work admission](../agent-rules/work-admission.md#work-admission)) and uses that issue's single branch, named `issue/<n>-<slug>` so it is discoverable from the issue number. Reuse that branch for continuation, never publish a second branch for the same issue, and split separately delivered responsibilities into separate bounded issues ([work admission and branch custody](../agent-rules/work-admission.md#work-admission-branch-custody-and-issue-coordination)).
2. **Make your changes.** Follow the code style enforced by Checkstyle.
3. **Write tests** for new functionality. Java modules use JUnit 6.
4. **Run the checks:**

```bash
./arcogine check         # Java compile, style, tests, and coverage
./arcogine check --full  # check + Java dependency audit + secret scan
```

Use `./arcogine check` before pushing. Use `./arcogine check --full` when the change warrants the Java dependency audit and secret scan. For full local dependency verification, also run the npm lockfile scan in the testing guide. For individual test categories and native subsystem commands, see [`docs/development/testing.md`](../testing.md).

5. **Open a pull request** against `main` using the repository template.

The PR description is stable change intent, not an execution log. Keep it to the semantic summary/rationale, compatibility or migration impact, and material non-goals represented by the template. Do not add a `## Validation` section or copy local test results, unavailable checks, current CI/check state, current `main`/head SHA, ahead/behind or commit counts, base freshness, or mergeability into the body. Those are transient implementation-handoff or live GitHub facts. Exact SHAs may still appear only when they intentionally identify immutable evidence/artifacts or are clearly labeled historical provenance rather than current lifecycle state.

**Temporary artifacts:** use `logs/` for local diagnostics, captures, and session scratch that should never be committed; it is gitignored as a whole. Branch-local material that must be committed for continuity or handoff but must not land on `main` belongs under the unignored `workspace/` root. `workspace/` is transient storage, not an archive: remove its files before final review and do not add a marker file. The repository check rejects any tracked `workspace/` path. Do not redirect canonical tool outputs — Gradle continues to use its configured locations.

For independent PR review, re-review, finding/disposition, CI-language, and AI-assisted session-boundary guidance, follow [`docs/development/reviewing.md`](../reviewing.md).
