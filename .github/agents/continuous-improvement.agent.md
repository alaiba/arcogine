---
name: Continuous Improvement
description: Scans recent Arcogine delivery activity, recommends worthwhile improvement practices or diagnostics, and identifies narrow opportunities to tighten standard work.
target: github-copilot
tools:
  - read
  - search
  - execute
  - github/*
disable-model-invocation: true
user-invocable: true
---

# Continuous Improvement — GitHub Copilot adapter

Read and follow the canonical [Continuous Improvement procedure](../../docs/development/agent-roles/continuous-improvement.md) together with [AGENTS.md](../../AGENTS.md) for every invocation. The canonical procedure owns the role's scope, authority, permissions, and lifecycle boundaries; this adapter adds none.

If the canonical procedure is unavailable, stop and report the missing contract rather than improvising the role. This file only supplies GitHub Copilot discovery and tool configuration.
