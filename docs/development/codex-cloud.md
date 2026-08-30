# Codex Cloud development

> **Status:** observed behavior as of 2026-08-30. Codex Cloud is an external product and its behavior may change; re-verify this guidance when the product evolves.

This document records how Arcogine behaves in ChatGPT Codex Cloud based on direct repository experiments. It is developer guidance for people who choose to use the cloud environment, not a repository-wide requirement and not an architectural constraint.

## What the cloud environment provides

A cloud task started from `alaiba/arcogine` is provisioned as an isolated checkout. In the observed environment:

- the working branch inside the container is a synthetic `work` branch;
- no Git remote is configured;
- `gh` is installed but not authenticated;
- no `GH_TOKEN`, `GITHUB_TOKEN`, or equivalent shell credential is exposed;
- public GitHub HTTPS and REST reads work;
- the checkout can start at the exact selected repository revision even though remote metadata has been removed.

These properties appear to be intentional sandboxing. Do not add a PAT, GitHub CLI authentication, or a Git remote merely to make the container resemble a normal local clone.

## Toolchain and validation

The initial inspected image had Java 21 and Node 20.20.2. Java 21 satisfies Arcogine's compatibility baseline, while Node 20 is below the frontend's supported engine range.

A supported Node version can be provisioned inside the task with the available version managers. Both of the following were exercised successfully in separate tasks:

- `nvm use 22.22.2`;
- installing and selecting Node 24.15.0 with `mise`.

After selecting a supported Node version, the cloud environment successfully ran:

- `npm ci`;
- frontend linting;
- frontend unit tests (82 tests in the observed run);
- the production frontend build;
- `./arcogine check`, including Java compilation, Checkstyle, tests, coverage gates, frontend lint/type-check/tests, and the frontend build.

Docker was not available inside the inspected container, so Docker-dependent validation should remain a CI responsibility when the cloud environment does not expose a Docker daemon/CLI.

## Verified GitHub publication workflow

The container itself has no demonstrated GitHub write authority. Publication happens through the Codex product UI rather than through shell-level Git credentials.

The verified workflow is:

1. Start a cloud task from an Arcogine repository revision.
2. Implement, validate, and commit inside the isolated `work` checkout.
3. Review the resulting task diff in the Codex UI.
4. Use **Create draft PR** in the Codex UI.
5. Codex publishes a GitHub branch and draft pull request through a platform-managed integration.

PR #197 was created this way from a container that still had no remote and no authenticated `gh` session. This demonstrates that shell-level GitHub credentials are not required for initial PR publication.

## Observed limitations

The following behaviors were observed during the 2026-08-30 experiments and should be treated as product limitations or unproven flows, not as Arcogine policy:

- Continuing the original cloud task after creating a PR can create additional local commits, but no reliable **Update PR** action was exposed to publish those commits back to the existing PR.
- Opening **Chat** from an existing PR did not produce a PR-associated checkout in the container.
- Selecting the PR's GitHub branch as the source revision still yielded a synthetic `work` branch with no remote and no PR identity visible to the agent.
- The agent could therefore not infer from Git state alone that its checkout corresponded to an existing pull request.
- The local handoff controls were not fully reliable in testing: **Apply** skipped both changed files in one run, and **Undo** reported that it required a Git repository even though the cloud sandbox itself was a Git repository.

These observations may become obsolete as Codex evolves. Re-test before depending on them.

## Recommended Arcogine usage for now

Treat Codex Cloud as a strong, disposable implementation worker for bounded changes:

```text
main (or another selected source revision)
  -> Codex Cloud task
  -> implementation
  -> available validation
  -> local cloud commit
  -> Create draft PR
  -> stop
```

For iterative work on an existing PR (review fixes, repeated commits, rebases, conflict resolution, or review-comment follow-up), prefer an environment with a normal Git remote and authenticated GitHub workflow until Codex Cloud's existing-PR update path is proven reliable.

This recommendation is deliberately operational rather than normative. Developers may choose other environments as appropriate.

## Evidence from the experiment

- Dependabot PR #194 provided the small reference change: `@types/react-dom` 19.2.4 -> 19.2.5.
- A fresh Codex Cloud task reproduced the same two-file dependency update, ran `./arcogine check` successfully, and committed it in the synthetic `work` checkout.
- The Codex UI then created draft PR #197 on branch `codex/update-@types/react-dom-to-19.2.5` despite the container having no remote and no authenticated GitHub CLI.
- Follow-up commits in the same cloud task were intentionally created and reverted while testing existing-PR iteration; they were not published to PR #197.
