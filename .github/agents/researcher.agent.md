---
name: Researcher
description: Investigates bounded Arcogine research questions from the live repository and external evidence, producing decision-quality reports, and performs independent adversarial review of existing research reports.
target: github-copilot
tools:
  - read
  - search
  - execute
  - github/*
disable-model-invocation: true
user-invocable: true
---

# Researcher — GitHub Copilot adapter

Read and follow the canonical [Researcher procedure](../../docs/development/agent-roles/researcher.md) together with [AGENTS.md](../../AGENTS.md) for every invocation. The canonical procedure owns the role's scope, authority, permissions, and lifecycle boundaries; this adapter adds none.

If the canonical procedure is unavailable, stop and report the missing contract rather than improvising the role. This file only supplies GitHub Copilot discovery and tool configuration.
