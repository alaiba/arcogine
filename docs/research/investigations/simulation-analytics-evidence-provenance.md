# Simulation analytics evidence, completeness, and provenance

> **Status:** CANDIDATE
> **Risk:** **High** — affects the truthfulness, reconstruction, and possible reproducibility of shared analytical results
> **Scope:** Analytical inputs, accumulation, and analytical-definition/result provenance that remain after the execution-account boundary is settled
> **Authority:** Research framing only; no analytics capability, retention promise, identity scheme, or Engine change is selected here.

## Promotion trigger

The concluded [simulation execution account](simulation-execution-account.md) question supplies
run/reset, ordered evidence, basis/frontier, claim-scoped coverage and qualified interval-finality
meaning through the [runtime contract](../../architecture/runtime-contract.md#captured-execution-evidence-and-interval-determinacy).
These are upstream inputs, not analytical choices. Their reconciliation does not make this question
READY.

The [concluded analytical-definition ownership boundary](simulation-analytics-consumer-boundary.md)
permits Game-local named methods while leaving shared ownership conditional on an evidenced need.
A **concrete admitted analytical use** warrants promotion when it needs **any** of:

1. additional supported producer inputs;
2. definition-specific evidence sufficiency/refusal (including shared-method or custody-crossing use);
3. definition-shaped bounded accumulation instead of retaining raw evidence;
4. definition identity and historical interpretation;
5. result attribution/provenance; or
6. retention, handover or an extended evidence horizon.

Independent or passive verification, Governance reliance, a result consumed by a non-driver or
non-holder, and cross-session reproduction are **examples/additional triggers**, not replacements
for these dimensions. Shared-definition admission requires evaluating these needs, **not**
automatically promoting this question: a stateless named method over already sufficient raw
evidence held by one co-located party may need none of them. A missing producer input is both a
potential producer-support decision and a method-evidence question; it does not automatically
license an Engine accumulator. Physical capture need not belong to the definition owner. Reuse
the runtime's coverage, proof and controller-trust vocabulary rather than defining a second
execution history contract.

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

**Method-specific admission and refusal.** For a retained or promoted interval result,
carry the exact named definition identity, `RunId`, `ModelFingerprint` (`ControlledRevisionId`
only if authoritatively bound), half-open interval/convention, basis `(S0,T0)`, proof sequence
or frontier, and producer-change-witness/producer-observation/controller-closure proof class.
Carry controller attestor, input-exclusion scope, command/fault outcomes and trust assumptions only
when the claim depends on them; carry a resolvable exact producing Engine-definition reference for
concrete cross-revision/reproduction uses only when actually supported; and carry ordered input
commands with advancement interleavings and budgets for reproduction. A result must refuse
basis/observations/changes/closure that do not all bind to the **same run**, or a closure contradicted
by captured changes after its binding but before the claimed upper bound. Locally gap-free events
without a separate authoritative frontier are not interval proof. The completeness of supported
changes never establishes all commands, fault-free success, or reproducible input history.
An analytical-definition owner specifies these method obligations; the holder owns capture and
retention; an attesting controller supplies its qualified promise. Sharing a definition does not
move custody or upgrade trust.

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
4. A long unfinished step at the boundary, where occupancy derived from supported dispatch events
   must count the running step up to the boundary — evidence a completion-credited total would miss —
   and must refuse or qualify when the dispatch that opened it was not captured.
5. `concurrency > 1`, where occupied job-ticks summed across overlapping steps can exceed elapsed
   time, so occupancy needs an explicit capacity denominator rather than elapsed time alone.
6. Equal-time events where supported sequence, not wall-clock equality alone, determines order.
7. Run reset and fresh-run identity.
8. Incremental accumulator state reconstructed from the same declared evidence as batch calculation.
9. Evidence whose model/analytical-definition basis differs from another result, proving the mismatch
   stays visible for later comparability decisions.
10. A mixed-run observation basis combined with another run's changes: refuse the false 10-tick
    acceptance where correctly bound captured evidence proves only five.
11. A controller closure bound before a later held completion within the asserted interval:
    refuse an erroneous 100-tick acceptance when the held completion proves five.
12. A stale holder with dispatch but missing the post-advancement completion/frontier: refuse the
    claimed 100-tick occupancy despite an honest controller's exhaustion; retain an already valid
    earlier prefix even if a later interval has gaps.
13. Current snapshot `no currently known work` versus an interval idle-tail aggregate and future
    `no work left for the period`: distinguish observation sufficiency, interval closure and
    scope of future-effective-input exclusion.
14. After removal of the completion-credit `busyTicks` field, show that a fresh observation
    cannot reconstruct the removed history; a separately named event-derived measurement is not
    an implicit replacement.

## Exit criteria

Conclude only with:

- a supported analytics input set for the admitted analytical uses;
- method-specific sufficiency/refusal using the runtime contract's coverage and finality semantics;
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
