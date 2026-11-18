# Research operating model

This document is Arcogine's single normative authority for the **research operating model**: what qualifies as research, research lifecycle and portfolio-priority semantics, promotion/reconciliation, investigation and adversarial-review method, evidence custody, synthesis-seed handling, historical decision-rationale retention, and maintenance of the research state registers.

Current maintained state is separate from these rules:

- [`docs/research/research-register.md`](../research/research-register.md) records current admitted research questions and their portfolio state;
- [`docs/research/synthesis-seeds.md`](../research/synthesis-seeds.md) records current non-authoritative synthesis-seed state.

For AI execution of this policy, the repository-owned **Researcher** procedure lives in [`docs/development/agent-roles/researcher.md`](agent-roles/researcher.md). This document remains the normative research policy; the agent file defines how the specialized researcher executes it — the same split `docs/development/reviewing.md` and `docs/development/agent-roles/pr-reviewer.md` already use.

This document must not become a second research backlog, state ledger, architecture authority, or implementation plan. It defines the operating model, not current portfolio content: it does not list Arcogine's open questions (the research register does), does not decide any Arcogine semantic question (the architecture, specification, or product document that owns it does), and does not sequence implementation (implementation handoffs and GitHub issues do).

## Routed topics

Each section below names the topic that owns it and when to read it. A routed topic is mandatory whenever its trigger applies; none is optional reference. An investigation reads every topic its work touches, and an adversarial review reads the adversarial-review topic together with the topics that govern the evidence it challenges.

## Lifecycle, priority, promotion, and maintained state

Owned by [Lifecycle, priority, promotion, and maintained state](researching/lifecycle.md). Read it when changing a question's status or priority, promoting a conclusion, or maintaining the research register.

## 1. What research is for

Owned by [1. What research is for](researching/briefs.md). Read it when starting an investigation or writing or narrowing a research brief.

### Implementability versus architectural suitability

Owned by [Implementability versus architectural suitability](researching/briefs.md#implementability-versus-architectural-suitability). Read it when a question compares what is implementable now with what is architecturally suitable.

## 2. Repository grounding and baseline discipline

Owned by [2. Repository grounding and baseline discipline](researching/grounding-and-internal-evidence.md). Read it when starting any investigation or review, and when choosing or using internal evidence (sections 2-4).

### Executable research substrate

Owned by [Executable research substrate](researching/grounding-and-internal-evidence.md#executable-research-substrate). Read it when running or adding a deterministic research experiment.

## 5. External evidence

Owned by [5. External evidence](researching/external-evidence-and-depth.md). Read it when using external sources or analogies, building proving cases, or deciding depth and when to stop (sections 5-8).

## 9. What "adversarial" means

Owned by [9. What "adversarial" means](researching/adversarial-review.md). Read it when performing or commissioning an independent adversarial review, or judging a review's outcome.

## 10. Research and reconciliation stay separate

Owned by [10. Research and reconciliation stay separate](researching/reconciliation.md). Read it when reconciling a conclusion into durable state or writing an implementation handoff.

### Implementation handoff

Owned by [Implementation handoff](researching/reconciliation.md#implementation-handoff). Read it when writing, citing or implementing a research implementation handoff.

### Evidence workspaces, artifact identity, and retirement

Owned by [Evidence workspaces, artifact identity, and retirement](researching/evidence-workspaces.md). Read it when committing, handing off or synchronizing research artifacts, and then the [feedback, coordinates and retirement](researching/workspace-retirement.md) topic when implementation feedback arrives or before reconciliation retires the workspace.

### Historical decision rationale

Owned by [Historical decision rationale](researching/decision-rationale.md). Read it when deciding whether to retain, write or change a historical decision-rationale record.

### Synthesis-seed custody

Owned by [Synthesis-seed custody](researching/synthesis-seed-custody.md). Read it when recording, reviewing or maintaining a synthesis seed.

## 11. What this operating model intentionally does not decide

This document defines Arcogine's normative research operating model. It does not:

- list Arcogine's current open research questions — see [`docs/research/research-register.md`](../research/research-register.md);
- decide any Arcogine semantic question (agency, operational identity, resource semantics, or otherwise) — each one's state is exactly what the maintained register and the applicable architecture or specification record, and this document neither settles nor reopens any of them;
- create a Research delivery track, a second research roadmap, research delivery coordinates, a research sprint system, a permanent report archive, publication lifecycle/backlog, or new issue ledger;
- create a decision-record authority, decision numbering, or decision status/approval lifecycle.

Research documents remain research evidence only. Synthesis seeds remain non-authoritative recurrence signals only. Historical decision-rationale records remain non-normative history only. None of them becomes accepted architecture simply because it exists.
