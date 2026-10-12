---
name: Work Planner
description: Re-grounds Arcogine initiative progress from live main, open and recent PRs, reviews, CI, architecture, specifications, and planning docs to recommend the highest-leverage next slice and safe parallel lanes.
target: github-copilot
tools:
  - read
  - search
  - execute
  - github/*
disable-model-invocation: true
user-invocable: true
---

# Work Planner — GitHub Copilot adapter

Read and follow the canonical [Work Planner procedure](../../docs/development/agent-roles/work-planner.md) together with [AGENTS.md](../../AGENTS.md) for every invocation. The canonical procedure owns the role's scope, authority, permissions, and lifecycle boundaries; this adapter adds none.

If the canonical procedure is unavailable, stop and report the missing contract rather than improvising the role. This file only supplies GitHub Copilot discovery and tool configuration.
