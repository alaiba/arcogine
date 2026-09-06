# Coding-agent evaluation for Arcogine

> **Status:** non-normative maintainer research, last reviewed 2026-09-06. Provider plans, quotas, models, and prices change frequently; re-verify before making purchasing or workflow decisions.

This document records the current Arcogine-specific evaluation of coding-agent capacity, especially free and low-cost plans and model tiers that can be used for repository work. It is developer guidance, not repository policy and not an architectural constraint.

The objective is not to identify one permanent "best" coding agent. Arcogine already carries substantial repository-owned guidance in `AGENTS.md`, architecture/planning documents, specialized agent contracts, tests, and CI. That allows routine implementation work to be routed to inexpensive agents while reserving stronger reasoning capacity for architecture, adjudication, and difficult remediation.

## Evaluation principle

Compare agents by the cost of producing an acceptable Arcogine change, not by token price alone.

Useful measurements are:

- cost per accepted or mergeable PR;
- number of human interventions;
- first-pass test and quality-gate success;
- semantic correctness under Arcogine's architecture and ADRs;
- number and severity of independent-review findings;
- wall-clock time;
- reliability of Git/PR iteration and remediation;
- ability to follow `AGENTS.md` and the repository's specialized agent contracts.

A cheap model that repeatedly chooses the wrong abstraction or requires extensive cleanup is not cheap in practice. Conversely, repository-owned contracts and executable acceptance evidence can make less expensive models viable for well-bounded implementation work.

## Current maintainer access

The current working assumption for experimentation is that the maintainer already has access to:

| Tool / plan | Marginal cost for current use | Likely Arcogine role |
|---|---:|---|
| Cursor Free / Hobby | $0 until included allowance is exhausted | Interactive local implementation, multi-file changes |
| GitHub Copilot Free | $0 until included allowance is exhausted | IDE/GitHub-oriented implementation and routine coding |
| Claude Pro | Already subscribed | Claude Code implementation/reasoning; use cheaper model tiers for routine work and stronger tiers when semantic risk justifies them |
| ChatGPT / Codex access | Already available | Bounded implementation, reasoning, and repository work; see the separate Codex Cloud empirical note |

These are sunk or zero-marginal-cost resources for the current evaluation. They should generally be used before purchasing another overlapping subscription unless a new tool demonstrates materially better throughput or quality.

## Economical model tiers inside existing access

The harness and the model should be evaluated separately. Two inexpensive model tiers are especially relevant because they can carry a large share of well-specified Arcogine implementation work while preserving stronger models for ambiguity and architecture.

| Model | Current provider positioning / price snapshot | Arcogine hypothesis |
|---|---|---|
| GPT-5.6 Luna | OpenAI positions Luna for cost-sensitive, high-volume workloads. API list price as of 2026-09-03: $0.20/M input, $0.02/M cached input, $1.20/M output; 1.05M context. Inputs above 272K tokens incur long-context pricing. | Strong first-line worker for tests, coverage, bounded Java/TypeScript slices, mechanical refactors, documentation reconciliation, and broad repository archaeology with explicit acceptance criteria. |
| Claude Haiku 4.5 | Anthropic positions Haiku as the fastest/cheapest Claude Code option for quick lookups, simple edits, and high-volume scripted runs. API list price as of 2026-09-03: $1/M input and $5/M output; Claude Code availability is account/model-menu dependent. | Strong first-line worker inside the already-paid Claude Pro/Claude Code workflow for narrow edits, test work, scripted or repetitive changes, and well-bounded implementation. |

Current first-party references:

- GPT-5.6 Luna: <https://developers.openai.com/api/docs/models/gpt-5.6-luna>
- OpenAI model comparison: <https://developers.openai.com/api/docs/models/compare>
- Claude Haiku 4.5: <https://www.anthropic.com/claude/haiku>
- Claude Code model selection and usage guidance: <https://support.claude.com/en/articles/14552983-models-usage-and-limits-in-claude-code>
- Claude Code model configuration: <https://support.claude.com/en/articles/11940350-claude-code-model-configuration>

For subscription-backed use, API list price is not the marginal cash cost. The practical constraint is how much included plan quota a model consumes and whether the account actually exposes that model. Treat `/model`, the provider account UI, and observed usage as authoritative for the maintainer's current plan.

### Model-tier routing hypothesis

A useful starting rule is to route by **semantic risk**, not by provider prestige:

| Arcogine task shape | First model tier | Escalate when |
|---|---|---|
| File/reference lookup, repository search | Luna / Haiku | Search results require architectural adjudication |
| Tests, coverage, fixtures | Luna / Haiku | Tests reveal unclear or disputed product semantics |
| Small bug with a clear reproducer | Luna / Haiku | Root cause crosses domain boundaries or the obvious fix violates an invariant |
| Well-specified Java/TypeScript implementation slice | Luna / Haiku | Acceptance criteria are incomplete, implementation exposes a contract gap, or repeated validation fails |
| Mechanical refactor / documentation reconciliation | Luna / Haiku | The change alters compatibility, architecture status, or normative semantics |
| Medium cross-module feature | Mid/frontier model as needed | Use repository evidence to decide whether the cheaper worker is still converging |
| Difficult debugging / ambiguous remediation | Stronger reasoning model | — |
| ADR, identity/provenance, compatibility, persistence, cross-domain ownership | Strong reasoning model plus independent review | Do not optimize primarily for inference cost |
| Architecture/freeze review | Strong reasoning model plus a different-model second opinion | — |

A particularly attractive pattern is **cheap-model implementation plus independent model-family review**: for example, Luna implements and Claude reviews, or Haiku implements and GPT reviews. This reduces the chance that the implementer and reviewer reproduce the same model-family blind spot.

The core economic hypothesis is therefore:

> Use stronger reasoning to define and freeze the contract; use Luna or Haiku to execute sufficiently explicit implementation slices; escalate only when evidence shows that the cheaper worker is no longer converging safely.

Gate-style Arcogine work is a good proving ground for this pattern: once an ADR, normative semantics, acceptance criteria, and executable validation are settled, implementation is materially different from inventing those semantics in the first place.

## Additional free and low-cost capacity worth testing

The table below is intentionally a snapshot. Follow the linked provider documentation and re-check current account-specific limits before relying on any quota.

| Provider / tool | Current free or low-cost opportunity | Arcogine hypothesis |
|---|---|---|
| OpenCode + Muse Spark Contributor | OpenCode has offered free Contributor-model access; Contributor traffic may be used to improve Meta models | Strong candidate for high-volume public-repository implementation and repository archaeology in a clean checkout |
| Muse Code | Low fixed-price subscription tiers are available in some regions; current observed Everyday price is about RON 23/month | Worth paying for only if its integrated harness materially outperforms free pools on real Arcogine tasks |
| Google Antigravity Individual | Free individual agent tier with a rotating/current model set and quota | Strong candidate for another independent implementation/review pool |
| Amazon Q Developer Free | Free developer allowance with agentic requests | Useful independent monthly implementation pool, especially for bounded tasks |
| Kiro Free | Free credit allowance | Candidate for spec-oriented or bounded implementation work; measure actual task cost rather than credit count |
| Mistral Vibe Free | Free coding access plus provider-specific free/credit allowances where available | Useful additional CLI-agent pool and model diversity |
| Windsurf Free | Free editor/agent allowance | Secondary interactive implementation pool if its current quota is useful |
| OpenRouter free models | Free-model pool with rate/request limits | Useful backstop when paired with OpenCode/Cline/Roo; model quality varies |
| Cline promotional/free models | Rotating free-model promotions | Opportunistic capacity; do not make Arcogine workflow depend on a temporary promotion |

### Current source references

Re-verify from first-party sources when practical:

- Cursor pricing: <https://cursor.com/pricing>
- GitHub Copilot plans: <https://github.com/features/copilot/plans>
- Claude plans / Claude Code access: <https://claude.com/pricing>
- OpenAI Codex documentation: <https://openai.com/codex/>
- OpenCode Zen/model documentation: <https://opencode.ai/docs/zen/>
- Google Antigravity pricing: <https://antigravity.google/pricing>
- Amazon Q Developer pricing: <https://aws.amazon.com/q/developer/pricing/>
- Kiro pricing: <https://kiro.dev/pricing/>
- Mistral pricing: <https://mistral.ai/pricing/>
- OpenRouter pricing/free models: <https://openrouter.ai/pricing>

Provider marketing pages and account dashboards can disagree during migrations or regional rollouts. Prefer the actual account UI for the maintainer's available quota and price.

## Arcogine-specific routing hypothesis

The repository should not prescribe a provider. For experiments, route work by task shape and preserve independent review.

| Arcogine work | Economical first choice | Escalation principle |
|---|---|---|
| Architecture/readiness synthesis | Strong reasoning environment already available | Use an independent second opinion before freezing hard-to-reverse semantics |
| ADR adjudication | Strong Claude/GPT reasoning plus repository authority | Do not optimize for inference cost when a wrong decision creates downstream rework |
| Straight Java implementation | Luna / Haiku / Cursor Free / Copilot Free / free external-agent pool | Escalate only after concrete failure or semantic ambiguity |
| Straight React/TypeScript implementation | Luna / Haiku / Cursor Free / Copilot Free / free external-agent pool | Escalate on cross-boundary/API semantics rather than routine UI code |
| Tests and coverage | Luna / Haiku / cheapest capable free agent | Strong models are usually unnecessary unless tests expose a semantic defect |
| Repository archaeology / broad search | Luna or another very-low-cost large-context/free pool | Use stronger reasoning to adjudicate conclusions, not necessarily to perform every search |
| Documentation reconciliation | Luna / Haiku / cheap free agent with repository search | Independent review for architecture/status transitions |
| Iterative PR remediation | Cheap capable model in a harness with a reliable authenticated Git/PR loop | Prefer continuity and review-state awareness over raw model price; escalate disputed findings |
| Independent PR review | Different model family from the implementer where practical | Semantic independence is more valuable than using the same strongest model twice |

## Why Arcogine can use cheaper workers effectively

Arcogine deliberately keeps important correctness constraints in durable repository authority instead of relying on conversational context:

- `AGENTS.md` defines repository identity, branch handling, validation, and PR-monitoring rules;
- specialized Work Planner, PR Reviewer, and Consistency contracts define repository-specific operating procedures;
- architecture and ADRs define semantic boundaries;
- maintained planning documents define acceptance criteria and sequencing;
- tests and quality gates provide executable evidence;
- CI provides an independent repository-level validation surface.

This lowers the amount of project knowledge that must be inferred correctly from scratch by every coding model.

It does **not** eliminate the need for strong reasoning on high-semantic-risk changes. In particular, identity, provenance, compatibility, persistence, runtime semantics, cross-domain ownership, and accepted-ADR transitions should not be delegated solely because a model is inexpensive.

## Public-repository and data-use considerations

Arcogine is public, which makes Contributor/data-improvement tiers more attractive than they would be for proprietary source. However, a coding agent can see more than committed public files.

For providers whose terms permit using prompts/completions for model or product improvement, use a deliberately clean Arcogine checkout or worktree and avoid exposing:

- unrelated private repositories;
- `.env` files and secrets;
- personal or company credentials;
- private patches or notes that are not intended to become public;
- proprietary dependency source;
- shell history or adjacent filesystem content that the agent does not need.

"Public repository" is not equivalent to "every byte in the developer environment is public."

## Existing Codex-specific evidence

Detailed empirical observations about Arcogine's Codex Cloud environment, validation capability, draft-PR publication, and existing-PR iteration limitations are maintained separately in [`codex-cloud.md`](codex-cloud.md).

This comparison document should not duplicate those environment-specific findings. It should record only comparative conclusions that matter when allocating Arcogine work across available agents.

## Experiment protocol

Rather than choosing a permanent provider from marketing benchmarks, use real Arcogine slices as the benchmark.

For each meaningful experiment, record:

| Field | Record |
|---|---|
| Date | When the experiment was run |
| Agent/harness | Cursor, Copilot, Claude Code, Codex, OpenCode, Antigravity, Q, Kiro, etc. |
| Model / effort configuration | Exact model, including tier/version and reasoning/effort setting where exposed, such as GPT-5.6 Luna — High, GPT-5.6 Terra — Medium, Claude Sonnet 5 — Low, or Claude Haiku 4.5 |
| Plan / cost pool | Free quota, existing subscription, Contributor, paid API, etc. |
| Arcogine task | Issue/PR/slice and semantic-risk class |
| Human interventions | Clarifications, corrections, manual edits |
| Validation | Commands run and result |
| Independent review | Findings by severity and whether valid |
| Outcome | Abandoned, acceptable patch, PR opened, merged |
| Approximate cost | Cash cost or quota consumed when observable |
| Notes | Harness strengths, failures, context issues, Git/PR limitations |

The primary score is not benchmark percentage. It is **cost and human effort per acceptable Arcogine change**.

When comparing models inside one harness, keep the task shape and instructions as similar as practical. The goal is to distinguish model capability from harness quality rather than accidentally benchmarking two unrelated workflows.

For cross-provider comparisons, compare roughly equivalent model roles rather than arbitrary models: economical worker tiers such as Luna and Haiku; balanced implementation tiers such as Terra and Sonnet; and strongest reasoning tiers such as Sol and Opus. These are comparison buckets, not claims of capability equivalence, and provider reasoning/effort labels are not assumed to be equivalent. One useful Arcogine experiment is to compare a cheaper model at higher reasoning effort with a stronger model at lower effort on the same well-specified slice, measuring cost and human effort per acceptable change.

## Initial experiment order

Given the current access described above, the economical sequence is:

1. Use GPT-5.6 Luna and Claude Haiku 4.5 deliberately on real, well-specified Arcogine implementation tasks instead of reserving them for trivial edits. Record where each succeeds without escalation.
2. Keep using Cursor Free and GitHub Copilot Free for bounded implementation work where their local/IDE harnesses are advantageous.
3. Use the already-paid Claude Pro capacity with stronger Claude models for high-semantic-risk reasoning, difficult coding, and independent review rather than routine mechanical work; use Haiku inside Claude Code when the task shape fits.
4. Keep using existing ChatGPT/Codex capacity where its workflow fits; use Luna for cost-sensitive/high-volume execution when available and retain the separate empirical Codex Cloud guidance.
5. Test OpenCode with a free Muse Contributor-class model on a real, bounded Arcogine implementation or repository-archaeology task.
6. Test Google Antigravity and Amazon Q Developer as independent free implementation pools.
7. Test Kiro and Mistral Vibe if the first pools become quota-constrained or if their harnesses offer a distinct advantage.
8. Purchase another subscription such as Muse Code Everyday only after an experiment demonstrates a concrete throughput or quality advantage over the already-available capacity.

## Evidence ledger

No comparative Arcogine bake-off has yet been recorded in this document. Add rows here only after real repository tasks have been attempted; do not convert provider claims into Arcogine evidence.

| Date | Agent / model | Task | Cost pool | Outcome | Review findings | Notes |
|---|---|---|---|---|---|---|
| _pending_ | | | | | | |

## Maintenance rule

This document is intentionally volatile developer research.

When updating it:

- date material pricing/quota claims;
- prefer first-party sources;
- distinguish advertised quota from observed account quota;
- distinguish model quality from agent-harness quality;
- retain failed experiments as evidence rather than silently deleting them;
- do not promote a provider preference into `AGENTS.md` without a separate repository-level reason;
- do not treat this document as architecture, product direction, or an implementation prerequisite.
