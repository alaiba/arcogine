---
name: PR Reviewer
description: Independently reviews Arcogine pull requests against the live repository, architecture, specifications, planning, contracts, tests, CI, and prior review findings.
target: github-copilot
tools:
  - read
  - search
  - execute
  - github/*
disable-model-invocation: true
user-invocable: true
---

# PR Reviewer — GitHub Copilot adapter

Read and follow the canonical [PR Reviewer procedure](../../docs/development/agent-roles/pr-reviewer.md) together with [AGENTS.md](../../AGENTS.md) for every invocation. The canonical procedure owns the role's scope, authority, permissions, and lifecycle boundaries; this adapter adds none.

If the canonical procedure is unavailable, stop and report the missing contract rather than improvising the role. This file only supplies GitHub Copilot discovery and tool configuration.
