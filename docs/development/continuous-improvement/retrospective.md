# Delivery-process retrospective

Part of the [continuous-improvement operating model](../continuous-improvement.md#routed-topics). This topic opens the retrospective method; a formal retrospective then reads every other retrospective topic the entry point routes.

## Invocation

The retrospective runs only when explicitly requested. Arcogine keeps no standing retrospective cadence, threshold, due state, or automatic reminder, and Session-close Kaizen does not evaluate whether a retrospective is due.

Versioned baseline state for an explicitly invoked retrospective lives in:

`.github/continuous-improvement/retrospective.json`

## Purpose

Determine whether Arcogine's delivery controls are reducing recurring waste and escapes, decide which prior changes should be retained/retired/superseded, and create owned follow-up work only where the evidence justifies it.

The retrospective is **not** a process-history dump, an issue ledger, or a second planning system.

## Mechanical evidence first

Run the formal retrospective in a coding-agent repository checkout using the repository-owned Node tooling and authenticated GitHub access. Keep the versioned retrospective baseline, acquisition adapter, evidence validator, and analyzer on the same exact current `main` revision. Authenticate through `GH_TOKEN`, `GITHUB_TOKEN`, or an authenticated `gh` CLI, as supported by the adapter. Do not substitute hand-reconstructed PR/review counts or a chat/connector-side acquisition run if this environment is unavailable.

Before interpretation:

1. choose one exact main-target `throughPr` that bounds the run;
2. acquire and save a complete Retrospective Evidence bundle:

   ```bash
   node infra/dev/delivery-retrospective-github.mjs \
     --through-pr <number> \
     --output logs/retrospective-evidence.json
   ```

3. run the source-neutral analyzer against that saved bundle:

   ```bash
   node infra/dev/delivery-retrospective.mjs \
     --input logs/retrospective-evidence.json \
     --json
   ```

4. require acquisition, evidence-contract validation, and analysis to complete; then use the analyzer's exact PR window, merged-PR count, trusted-author CHANGES REQUIRED disposition count, review-checkpoint distribution, structured-finding dataset, and research-to-delivery lead time without manually reconstructing or retyping alternative totals.

The **supported Retrospective Evidence acquisition path** is `infra/dev/delivery-retrospective-github.mjs`, which fetches evidence through authenticated GitHub GraphQL and validates the complete Retrospective Evidence bundle before writing it. The adapter proves merged `main` baseline/through endpoints, complete candidate search pagination, exact merge-time window membership, complete submitted-review payloads preserving GitHub-provided author association and reviewed-head fields, each named handoff's commit, file and commit date as GitHub records them, and the issues open during the window, each with the pull request GitHub records as its closer, taken from one complete listing of the repository's issues regardless of state, so an issue that closes or reopens during acquisition is not lost. It fails closed on GitHub search limits, truncated review connections, a malformed, repeated or unconfirmed handoff line, an issue listing whose size changes during pagination, or unverifiable evidence. Do not replace it with manual issue/PR enumeration, connector-only acquisition outside the repository scripts, inferred review associations, or hand-built evidence JSON.

The **evidence contract remains source-neutral** in `infra/dev/delivery-retrospective-evidence.mjs`, and the analyzer in `infra/dev/delivery-retrospective.mjs` remains pure and source-independent. This does not authorize an alternative acquisition path: any new adapter must be introduced by a separately reviewed repository change that preserves the same completeness, provenance, and validation guarantees. The existing local GraphQL adapter is the only supported Retrospective Evidence acquisition entrypoint; research branch inventory is a separate supporting evidence pass over the same saved bundle.

The adapter fails closed when GitHub search/review retrieval cannot prove completeness. The analyzer fails closed on unsupported or internally incomplete evidence. If either fails, the retrospective is `INCOMPLETE`; fix the retrieval/tooling problem rather than estimating the sample. Structurally complete evidence can still contain malformed or noncanonical historical reviewer findings. In that case the analyzer preserves the core window/review metrics, marks finding analytics incomplete, and reports the specific coverage diagnostics; do not treat any incomplete distribution as a complete sample.

Manual analysis begins only after the mechanical window is established. It should focus on:

- whether the previous retrospective's retained controls actually prevented recurrence;
- high-confidence post-merge process/lifecycle escapes;
- repeated waste that can be tied to a concrete mechanism;
- whether an existing experiment/change should be retained, retired, or superseded.

**Control-effectiveness exposure.** For each prior control marked `verify next time`, identify its landing PR and merge time, the failure mechanism it was intended to prevent, and the later changes actually exposed to that mechanism after the control became applicable. Use those eligible changes and any attributable escapes or avoided rework to assess effectiveness; earlier failures explain why the control was adopted, not whether it worked. If eligible work, follow-up time, or trustworthy evidence is insufficient, retain `verify next time` and label the control **not yet verified** rather than claiming success or failure. Compare windows only where the sample and definitions permit it, and do not infer causation from lower review-churn counts alone. No exposure quota or success target is imposed.

**Post-merge escape mechanisms.** For every material attributable escape, record the origin PR and violated contract, whether the contradiction was in the origin diff or an omitted semantic neighbor, the detection source and date, the delay from merge to detection where available, and the existing corrective owner. Distinguish preventable semantic propagation misses, premature implementation premises, and branch-integration/stacking failures from valid research falsification or deliberate replacement. Keep **origin window**, **discovery date**, and the evidence observation cutoff distinct: a later-discovered escape from a window PR may inform that origin window but its detection is subsequent context; an escape whose origin predates this window is not a new in-window-origin escape. State whether detection lag and differing observation periods limit trend comparisons. These classifications are evidence-backed interpretation, not new canonical analyzer metrics.

Do not optimize for finding count. Healthy adversarial review findings are not waste merely because they are numerous. Hand-classified finding totals or category percentages remain noncanonical. The analyzer may report mechanically parsed PR Reviewer fields — including category, confidence, distinct findings, and per-PR incidence — when its structured-finding coverage is complete. Historical review bodies may still contain a legacy `Severity:` field; the analyzer preserves that value on the parsed historical finding when present, but severity is not part of current finding identity or aggregate analytics. When structured-finding coverage is incomplete, use the diagnostics and do not present the observed values as complete distributions. Interpretation and improvement decisions remain retrospective reasoning, not analyzer output.
