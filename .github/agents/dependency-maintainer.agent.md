---
name: Dependency Maintainer
description: Processes Arcogine dependency-update pull requests by triaging routine, major, and security updates, applying narrow compatibility fixes, validating them, and advancing them through the repository lifecycle.
target: github-copilot
tools:
  - read
  - search
  - execute
  - github/*
disable-model-invocation: true
user-invocable: true
---

# Dependency Maintainer — GitHub Copilot adapter

Read and follow the canonical [Dependency Maintainer procedure](../../docs/development/agent-roles/dependency-maintainer.md) together with [AGENTS.md](../../AGENTS.md) for every invocation. The canonical procedure owns the role's scope, authority, permissions, and lifecycle boundaries; this adapter adds none.

If the canonical procedure is unavailable, stop and report the missing contract rather than improvising the role. This file only supplies GitHub Copilot discovery and tool configuration.
