# Factory-Design Game Diagnostic Evidence Contract Research

> **Status:** READY — see the maintained [research register](../research-register.md)  
> **Scope:** Truthful, inspectable player-facing diagnostic evidence over supported non-spatial production/runtime facts  
> **Authority:** Product research only; this brief does not define Engine facts, reusable analytics, or population-level usability claims

## Question

What minimum player-facing diagnostic evidence contract can the game build from supported Arcogine simulation facts so that every diagnostic claim is:

1. mechanically traceable to authoritative or explicitly derived ground truth;
2. explicit about the method/derivation behind any interpretation;
3. able to refuse unsupported causal attribution; and
4. inspectable without exposing raw scheduler/runtime internals?

## Decision at stake

Whether the first internal/playable slice can expose a **truthful diagnostic surface** for the non-spatial production problem, and which evidence/explanation requirements should be promoted into the game consumer plan.

A positive result does **not** establish that a target player population finds the presentation intuitive, accessible, or engaging. That is a separate empirical question to admit when independent target users exist or when a release objective makes such a claim material.

## Scope and non-goals

In scope:

- supported current-state and ordered runtime evidence;
- resource/operation waiting, activity, completion progress, and attempt outcome facts that exist in the landed non-spatial runtime;
- named interpretations whose method and supporting numbers are exposed;
- controlled versus confounded attempt comparison;
- deterministic ground-truth fixtures and adversarial counterexamples;
- a product-owner walkthrough as a qualitative smoke test.

Out of scope:

- population-level player comprehension, accessibility, onboarding success, or preference;
- reusable analytics ownership — [Simulation analytics consumer boundary](simulation-analytics-consumer-boundary.md) owns that question;
- transfer/processing decomposition until transfer semantics and executable transfer evidence land;
- challenge economics, scoring, strategy diversity, tutorial sequencing, renderer choice, packaging, or persistence;
- inventing new Engine facts to make a presentation convenient;
- treating an AI/model review as a substitute for human-player evidence.

A research-local derivation may establish fixture ground truth. Reusing that derivation in product code is a separate ownership decision and must not be smuggled through this investigation.

## Candidate evidence contracts

Compare a small number of materially different **information contracts**, not polished visual designs.

1. **Facts only** — expose queue/waiting state, resource activity, completion progress, and attempt outcomes without a synthesized diagnostic verdict.
2. **Facts plus named interpretation** — expose the same facts plus a named diagnostic method and the exact numbers/facts on which its interpretation rests.
3. **Claim–evidence bundle** — present a concise diagnosis together with its evidence chain and an explicit refusal state when the available facts do not license the requested conclusion.

A candidate fails if it depends on hidden scheduler state, an unnamed heuristic, or a result that cannot be reconstructed from the supported input evidence selected for the study.

## Deterministic fixture corpus

Use a bounded corpus of deterministic non-spatial traces generated from the strategy-space/reference-challenge family. Every fixture must retain exact model, Engine semantics, workload/command, run/event-range, and any research-local analytical-definition provenance needed to reproduce its ground truth.

The corpus must include at least:

- one true capacity-constraint case;
- one capacity-added-at-constraint pair;
- one capacity-added-away pair with a null or materially smaller outcome effect;
- one constraint-migration pair;
- one starvation-versus-surplus case;
- one multi-eligible waiting case where per-machine queue depth alone is misleading;
- one long unfinished-step case where completion-credited `busyTicks` alone is misleading;
- one `concurrency > 1` case that defeats naïve utilization arithmetic; and
- one deliberately confounded attempt pair where several authored variables change.

Transfer-dependent fixtures belong to a later extension after the transfer/spatial contracts land.

## Ground-truth questions

For every fixture, precompute or independently derive the answer to a fixed diagnostic question set:

1. What work is waiting, and for which operation step?
2. Which resource/operation is the constraint under the **named** diagnostic method used by the candidate, if any?
3. Is a named idle resource starved, surplus, or not decidable from the selected evidence?
4. Which supported interval/category accounts for the largest measured delay that the evidence can actually distinguish?
5. For a controlled one-variable attempt pair, what authored fact changed and what outcome fact changed?
6. For a confounded pair, which causal attribution must be refused?

The oracle must be derived before evaluating the presentation candidate. Do not tune the oracle to make the presentation look correct.

## Evidence method

For each candidate evidence contract:

1. **Traceability audit** — map every displayed claim to exact supporting facts, event intervals, or a named research-local derivation.
2. **Reconstruction test** — prove that the claim can be recomputed from the stated supported input set without scheduler internals or hidden mutable state.
3. **Counterexample test** — run the candidate against the misleading cases above and verify it does not emit a stronger claim than the evidence licenses.
4. **Refusal test** — verify that the confounded attempt pair and every other underdetermined case produce an explicit “not attributable / not decidable from this evidence” outcome rather than a guessed explanation.
5. **Controlled-mutation test** — vary one authored input at a time and verify that the claim/evidence bundle changes only where the authoritative outcome/evidence changes.
6. **Terminology audit** — reject labels whose ordinary meaning contradicts the actual semantics, even if the underlying number is correct.

Where practical, encode the oracle and these invariants as executable fixtures. The research result should not depend on a person repeatedly eyeballing the same traces.

## Product-owner walkthrough

After the mechanical evidence audit, the repository owner may perform a blinded qualitative walkthrough:

- shuffle fixture identities/order;
- hide the oracle/result notes;
- show only the candidate player-facing evidence;
- answer the fixed diagnostic questions;
- reveal the oracle afterward and record ambiguities or misleading wording.

This walkthrough has deliberately asymmetric evidentiary value:

- **failure is useful falsification** — if the product owner cannot interpret a candidate despite knowing the domain and project, do not promote it unchanged;
- **success is only a smoke test** — it shows the candidate is not obviously unusable to the owner, not that independent players will understand it.

Do not compute percentages, confidence intervals, or “player comprehension” claims from one person.

## Truthful explanation constraints

Whatever evidence contract survives must respect what the selected evidence licenses.

The game may state measured facts such as:

- how many units waited for a given operation step and for how long in total;
- how long a resource was occupied when that duration is supported by the selected evidence/analytics boundary;
- that one resource was idle while work waited elsewhere;
- each attempt's completion tick; and
- what authored variable changed between two controlled attempts.

A named bottleneck/constraint interpretation must name its method and expose supporting evidence. A bare authoritative-looking badge is insufficient.

A controlled comparison may place one authored change beside one outcome change. It must not assert an unobserved mechanism merely because deterministic re-execution makes the association reproducible. When several relevant variables changed, present the change set and outcome delta and refuse unique attribution.

Do not present:

- instantaneous utilization from a cumulative completion-credited counter;
- `combinedQueueDepth` as a physical queue count;
- a “blocked” state that the current runtime cannot reach;
- transfer delay or placement causality before the applicable transfer/spatial semantics are landed; or
- “players understand X” as a conclusion of this investigation.

General rule: **prefer the weaker true statement.**

## Falsification conditions

A candidate evidence contract fails when any required fixture shows that it:

- emits a diagnosis that cannot be reconstructed from its declared supported inputs;
- hides a load-bearing analytical rule or presents it as an Engine fact;
- assigns multi-eligible waiting to one physical machine before binding;
- reports a misleading utilization/constraint interpretation in a known counterexample;
- gives a unique causal explanation for a confounded change;
- requires raw scheduler/internal implementation state;
- depends on transfer/spatial facts not yet supported; or
- is not interpretable by the product owner during the blinded smoke test.

## Evidence expectations

A decision-quality report must include:

- exact fixture/model/input provenance;
- the oracle derivation for every diagnostic question;
- candidate evidence-contract definitions;
- a claim-to-evidence matrix;
- reconstruction, counterexample, refusal, and controlled-mutation results;
- any product-owner walkthrough findings clearly labeled as single-person qualitative evidence;
- explicit separation between product presentation findings and reusable-analytics ownership.

## Exit criteria

Conclude when one of these is true:

- at least one bounded evidence contract passes the deterministic traceability/reconstruction/counterexample/refusal criteria and survives the product-owner smoke test, allowing its product-facing evidence requirements to be promoted; or
- no candidate can do so from the supported boundary, in which case record the exact missing fact/interpretation or product constraint rather than manufacturing a UI requirement.

The durable destination is a bounded diagnostic-evidence requirement set in the game consumer planning surface, plus a separate research/architecture question for any newly proven shared semantic gap.

## Deferred external-player validation

Independent player validation is deliberately **not** an exit criterion for this question because no independent participant pool is currently available.

Admit a separate empirical question when either:

- independent target users become available; or
- product/release direction makes a claim such as “new players can diagnose the factory without expert guidance” material enough to require evidence.

That later study may reuse the fixture corpus and diagnostic questions from this investigation, but it must define its own target population, tasks, falsification thresholds, and decision at stake. Until then Arcogine may claim only that the promoted diagnostic surface is mechanically truthful and product-owner inspected, not population-validated.
