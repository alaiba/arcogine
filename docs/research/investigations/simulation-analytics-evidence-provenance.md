# Simulation analytics evidence, completeness, and provenance

> **Status:** CANDIDATE
> **Risk:** **High** — affects the truthfulness, reconstruction, and possible reproducibility of shared analytical results
> **Scope:** Analytical inputs, accumulation, and analytical-definition/result provenance that remain after the execution-account boundary is settled
> **Authority:** Research framing only; no analytics capability, retention promise, identity scheme, or Engine change is selected here.

## Promotion trigger

Keep this question downstream of both:

1. the [simulation execution account](simulation-execution-account.md) investigation, which decides
   whether run/reset identity, ordered execution evidence, interval completeness, late join, and
   retained execution-window semantics are already sufficient or belong to a separate
   consumer-neutral execution responsibility; and
2. the [analytics ownership investigation](simulation-analytics-consumer-boundary.md), which must
   identify a concrete analytical responsibility/use.

Promote only for the analytical evidence/provenance obligations that remain after those boundaries
are known. Do not duplicate execution-account semantics here merely because analytics consumes them.

## Research question

> Given the accepted execution-account boundary, what additional supported inputs, analytical accumulation responsibility, and analytical-definition/result provenance must an admitted analytical responsibility preserve so its results are truthfully reconstructable and, where a use requires it, reproducible without becoming authoritative runtime history?

## Decision at stake

Choose the minimum evidence contract needed for shared analytics without making Engine event sourced,
inventing unbounded retention, or allowing analytical code to re-decide simulation behavior.

Arcogine distinguishes determinism from reproducibility. Determinism says a fixed defined computation
over complete result-affecting inputs has one specified observable result. Reproducibility additionally
requires the basis and capabilities needed to repeat that computation to remain recoverable. This
question owns that distinction for consumer-neutral analytical computations; it does not redefine the
Engine Determinism Contract.

## Current baseline

- Current observations provide supported current-state projections.
- Supported runtime events are ordered authoritative deltas but are drained rather than retained
  replay history.
- The runtime need not retain an entire event history merely because an analytical use needs an
  interval.
- The non-shipped `research-experiments` module can capture explicit evidence windows and
  research-local oracles; it is proving infrastructure, not a production analytics API or retention
  contract.
- `ModelFingerprint` identifies Factory content under its owning rules, not an exact Engine
  definition.
- Current Engine exposes no placeholder exact-definition identifier.

## Candidate models

Compare at least:

1. **Caller-owned complete capture.** Analytics operates only on an explicitly complete evidence
   window supplied by its caller and refuses claims whose required interval is incomplete.
2. **Analytics-owned incremental state.** A consumer-neutral analytics owner consumes supported
   observations/events and maintains only the accumulators needed by declared analytical definitions.
3. **Bounded retained supported-event capability.** Introduce retained event access only if a
   concrete analytics use cannot be served truthfully by complete capture or bounded accumulators.
   Do not presume this candidate wins or that retention belongs to Engine.

A hybrid may survive if different analytical definitions genuinely need different evidence shapes.

## Required contract dimensions

For every supported analytical result considered, identify:

- exact supported model/observation/event inputs;
- the event or observation interval actually used;
- whether that interval is complete for the claim;
- run/session identity and reset boundaries;
- the named analytical definition and any version/reference required to reproduce it;
- the producing model/content basis;
- any exact Engine-definition basis the use actually requires, without inventing one by assumption;
- accumulator state, if any, and who owns it;
- missing/late/partial evidence behavior;
- reconstruction versus later-reproduction guarantees; and
- enough provenance to notice a basis mismatch rather than silently assert comparability.

That last rule does **not** decide whether cross-revision results are comparable. The
[cross-revision comparability question](simulation-analytics-cross-revision-comparability.md) owns
that decision.

## Invariants

- Analytics reads supported facts; it never reads scheduler/private handler state as a substitute.
- Analytics may measure outcomes but must not reconstruct or re-decide Engine scheduling/dispatch.
- A partial event window is never silently presented as a complete historical interval.
- Run reset creates a new run basis; analytical state may not silently flow across it.
- Research-local formulas do not become supported analytical definitions merely because they are
  executable.
- Missing exact Engine provenance remains missing until a concrete exact-reference use is resolved.
- Retaining analytical state does not make it authoritative runtime state.

## Proving and failure cases

At minimum:

1. Late collection after earlier supported events have already drained.
2. A deliberately incomplete interval where a duration/occupancy claim must refuse or qualify.
3. Multi-eligible waiting, proving that resource-local queue depth is not sufficient evidence.
4. A long unfinished step where completion-credited `busyTicks` is insufficient for instantaneous
   occupancy.
5. `concurrency > 1`, defeating naive busy-tick utilization.
6. Equal-time events where supported sequence, not wall-clock equality alone, determines order.
7. Run reset and fresh-run identity.
8. Incremental accumulator state reconstructed from the same declared evidence as batch calculation.
9. Evidence whose model/analytical-definition basis differs from another result, proving the mismatch
   stays visible for later comparability decisions.

## Exit criteria

Conclude only with:

- a supported analytics input set for the admitted analytical uses;
- explicit complete/partial evidence-window semantics;
- retention versus accumulator ownership and failure behavior;
- reconstruction and, where actually required, reproduction obligations;
- analytical-definition and producing-basis provenance requirements;
- refusal/qualification behavior for insufficient evidence;
- proof that no selected mechanism becomes a second simulation authority; and
- independent adversarial review before any architecture promotion.

## Destination and exclusions

If accepted, reconcile only the evidenced analytics-input/provenance responsibility into the owning
runtime/analytics/Governance contracts, then admit a bounded implementation slice.

Do not choose a remote transport, public Java package, cross-adapter representation, universal
definition identity, event-sourcing architecture, unbounded history store, or cross-revision
comparability rule here.
