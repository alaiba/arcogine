# Research

> **Status:** Maintained research-area index  
> **Scope:** Navigation across Arcogine research policy, current research state, synthesis signals, investigations/reports, reusable brief/report structures, and the durable strategic rationale for retaining synthesis seeds  
> **Authority:** Research-area context/index only; normative research rules and maintained research state live in the linked authorities below

Arcogine separates the **research operating model** from its **maintained state** and **investigation artifacts**:

| Surface | Owns |
|---|---|
| [`docs/development/researching.md`](../development/researching.md) | Normative research operating model: question/lifecycle definitions, priority semantics, promotion and reconciliation, investigation/review method, evidence custody, synthesis-seed handling, historical decision-rationale retention, and register maintenance |
| [`research-register.md`](research-register.md) | Current admitted research questions, priority, lifecycle state, linked evidence artifact, expected destination, and review date |
| [`synthesis-seeds.md`](synthesis-seeds.md) | Current non-authoritative cross-investigation synthesis signals retained under the research method |
| [Historical decision rationale](../history/README.md#decision-rationale) | Selected dated, non-normative records of why a significant reconciled choice was made, retained under the research method's retention test; history, not research state or current authority |
| [`investigations/`](investigations) | Bounded research briefs, durable investigation write-ups, and deliberately retained or reconciled research-history artifacts linked from the register |
| `workspace/research/` | Temporary branch-local reports, report revisions, adversarial reviews, checkpoints, diagnostic notes, and handoff artifacts; the final merge tree must contain no tracked `workspace/` files, while earlier evidence remains inspectable in a retained branch's Git history. Branch deletion happens only on explicit owner request, not automatically after merge or reconciliation |
| [`brief-template.md`](brief-template.md) | Reusable advisory structure for bounding a research question; it does not add lifecycle or `READY` criteria beyond the normative operating model |
| [`report-template.md`](report-template.md) | Reusable structure for a decision-quality research report |

## Strategic horizon

The long-term reason to preserve synthesis seeds is to keep open a path from repeated Arcogine evidence to knowledge that may eventually prove useful beyond Arcogine. If a signal independently recurs, survives bounded cross-investigation synthesis, and later withstands external comparison, replication, criticism, or other validation at the broader scope, it may contribute to reusable methods, terminology, reference models, tooling, standards work, or other industry practice.

This horizon does not change Arcogine research admission, create publication work, or make a seed a claim about the outside world. Research remains driven by material Arcogine decisions; seeds merely preserve enough evidence-bearing connective tissue that genuinely broader knowledge is not made impossible to recognize if temporary branch history becomes unavailable in the future.

Durable investigation briefs and deliberately retained or reconciled research-history artifacts belong under [`investigations/`](investigations), rather than beside the maintained registers and reusable templates. Temporary decision-quality reports, report revisions, adversarial reviews, checkpoints, and handoff material belong under `workspace/research/` while they are serving evidence custody, and must be removed from the final merge candidate. Exact workspace SHA+path coordinates identify those artifacts during active custody; maintaining a branch makes earlier commits available for inspection, but maintained research state cannot depend on that branch remaining reachable forever. Any result that must survive is reconciled into its durable destination first, and any artifact whose exact future readability matters must be deliberately promoted to a retention surface with a real persistence contract; otherwise the temporary file still leaves the final tree. These artifacts are research evidence or investigation framing, not accepted product/architecture semantics merely because they exist.

For AI execution, use the repository-owned [Researcher](../development/agent-roles/researcher.md) role together with the normative research operating model.
