# Research

> **Status:** Maintained research-area index  
> **Scope:** Navigation across Arcogine research policy, current research state, synthesis signals, investigations/reports, and report structure  
> **Authority:** Index only; normative research rules and maintained research state live in the linked authorities below

Arcogine separates the **research operating model** from its **maintained state** and **investigation artifacts**:

| Surface | Owns |
|---|---|
| [`docs/development/researching.md`](../development/researching.md) | Normative research operating model: question/lifecycle definitions, priority semantics, promotion and reconciliation, investigation/review method, evidence custody, synthesis-seed handling, and register maintenance |
| [`research-register.md`](research-register.md) | Current admitted research questions, priority, lifecycle state, linked evidence artifact, expected destination, and review date |
| [`synthesis-seeds.md`](synthesis-seeds.md) | Current non-authoritative cross-investigation synthesis signals retained under the research method |
| [`investigations/`](investigations/) | Bounded research briefs, investigation write-ups, decision-quality reports, and retained research-history artifacts linked from the register |
| [`report-template.md`](report-template.md) | Reusable structure for a decision-quality research report |

Investigation/report files belong under [`investigations/`](investigations/) rather than beside the maintained registers and reusable template. They are research evidence or investigation framing, not accepted product/architecture semantics merely because they exist.

For AI execution, use the repository-owned [Researcher](../../.github/agents/researcher.agent.md) role together with the normative research operating model.
