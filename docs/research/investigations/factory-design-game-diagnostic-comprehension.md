# Factory-Design Game Diagnostic Comprehension Research

> **Status:** READY — see the maintained [research register](../research-register.md)  
> **Scope:** Player comprehension of supported non-spatial production/runtime evidence for constraint diagnosis and controlled comparison  
> **Authority:** Product research only; this brief does not define Engine facts, reusable analytics, or game implementation

## Question

Which presentation of supported Arcogine simulation evidence lets a player correctly:

1. identify the current constraining operation/resource;
2. distinguish an idle resource that is starved from one that is surplus capacity;
3. identify the largest supported source of delay; and
4. avoid attributing a multi-variable outcome difference to one unisolated change?

## Decision at stake

Whether the first playable product can expose enough trustworthy evidence for a player to diagnose a production problem without reading raw scheduler/runtime internals, and which presentation requirements should be promoted into the game consumer plan.

This question does **not** decide whether the overall game is engaging, whether the reference challenge has a good strategy space, or whether spatial layout is useful.

## Scope and non-goals

In scope:

- presentation of supported current-state and ordered runtime evidence;
- resource/operation waiting, activity, completion progress, and attempt outcome facts that exist in the landed non-spatial runtime;
- named interpretations whose method and supporting numbers are shown;
- controlled versus confounded attempt comparison;
- participant comprehension measured against ground truth.

Out of scope:

- reusable analytics ownership — [Simulation analytics consumer boundary](simulation-analytics-consumer-boundary.md) owns that question;
- transfer/processing decomposition until transfer semantics and executable transfer evidence land;
- challenge economics, scoring, strategy diversity, tutorial sequencing, renderer choice, packaging, or persistence;
- inventing new Engine facts to make a visualization convenient.

A study may compute research-local ground truth from supported traces. Reusing that derivation in product code is a separate ownership decision and must not be smuggled through this investigation.

## Candidate presentation models

The investigation should compare a small number of materially different presentations, for example:

1. **Direct evidence:** queue/waiting state, resource activity, completion progress, and attempt outcome with no bottleneck verdict.
2. **Evidence plus named interpretation:** the same facts plus a named constraint-detection method and the numbers that support it.
3. **Progressive explanation:** direct evidence first, with drill-down showing the interpretation and supporting temporal evidence only when requested.

Exact visual styling is not the independent variable. The candidates must differ in what evidence and interpretation they expose, not in polish.

## Material

Use 6–8 pre-recorded deterministic traces generated headlessly from a bounded non-spatial challenge family. Ground truth is computed from supported facts/events rather than authored by the study facilitator.

The set must include at least:

- one true capacity constraint;
- one capacity-added-at-constraint pair;
- one capacity-added-away pair with a null or materially negligible outcome delta;
- one constraint-migration pair;
- one starvation-versus-surplus case; and
- one deliberately confounded pair where two authored variables changed at once.

Do not require spatial transfer to construct the study. Transfer-dependent cases belong to a later extension after the transfer/spatial contracts land.

## Participants and tasks

Use 8–12 participants split between engineering-literate and non-specialist participants. This is an initial falsification study for systematic misunderstanding, not an effect-size comparison between polished interfaces.

Score these tasks against ground truth:

1. Name the constraining operation or resource.
2. For a named idle resource, state whether it was starved or surplus.
3. Name the largest supported source of delay in the trace.
4. Given two candidate interventions, predict which is expected to improve completion under the evidence shown.
5. Given two attempts differing in one authored variable, state what changed and what outcome changed.
6. Given the confounded pair, state what can and cannot be attributed. **"Cannot attribute to one change" is the correct answer.**

Do not use visualization preference or self-reported insight as the primary measure.

## Pre-registered falsification thresholds

Fix these before running the study:

- < 70% correct on task 1 → the constraint presentation fails;
- < 60% correct on task 2 → the starved/surplus distinction is not landing;
- < 50% correct on task 6 → the comparison presentation is manufacturing causal confidence;
- any case where a majority reads the capacity-added-away pair as a meaningful improvement → the null-result presentation fails.

A miss is falsification of that presentation candidate, not permission to move the threshold after seeing results.

## Truthful explanation constraints

Whatever presentation survives must respect what the evidence licenses.

The game may state measured facts such as:

- how many units waited for a given operation step and for how long in total;
- how long a resource was occupied when that duration is supported by the selected evidence/analytics boundary;
- that one resource was idle while work waited elsewhere;
- each attempt's completion tick; and
- what authored variable the game changed between two controlled attempts.

A named bottleneck/constraint interpretation must name its method and show its supporting evidence. A bare authoritative-looking badge is not sufficient.

A causal explanation is permitted only for a controlled comparison in which exactly one relevant authored variable changed and the statement does not claim an unobserved mechanism. When several variables changed, state the change set and outcome delta and offer a controlled rerun instead.

Do not present:

- instantaneous utilization from a cumulative completion-credited counter;
- `combinedQueueDepth` as a physical queue count;
- a "blocked" state that the current runtime cannot reach; or
- transfer delay or placement causality before the applicable transfer/spatial semantics are landed.

General rule: **prefer the weaker true statement.**

## Evidence expectations

A decision-quality report must include:

- exact trace/model/input provenance for every study case;
- the ground-truth derivation for each scored task;
- the candidate presentation definitions;
- per-task outcomes against the pre-registered thresholds;
- failure/misinterpretation examples, not only aggregate success rates;
- a clear separation between presentation findings and any reusable-analytics question exposed by the study.

## Exit criteria

Conclude when one of these is true:

- at least one bounded presentation model meets the pre-registered thresholds and its required product-facing evidence can be stated without inventing unresolved shared semantics; or
- the tested evidence boundary systematically fails, in which case record the failure and the exact missing fact/interpretation question rather than promoting UI work.

The durable destination is a bounded set of player-facing diagnostic requirements in the game consumer planning surface, plus a separate research/architecture question for any newly proven shared semantic gap.
