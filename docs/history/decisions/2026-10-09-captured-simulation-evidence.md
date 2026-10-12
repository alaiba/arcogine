# Keep captured simulation evidence at the runtime contract boundary

> **Date:** 2026-10-09
>
> **Authority:** Historical, non-normative evidence; current meaning lives only in the documents it was reconciled into
>
> **Reconciled into:** [Runtime observation and event contract](../../architecture/runtime-contract.md#captured-execution-evidence-and-interval-determinacy)

## Decision and context

The execution-account investigation selected common evidence semantics in the existing runtime
contract, with caller-held capture, rather than a new execution entity or retained-history owner.
The runtime already provided an opaque run epoch, coherent current observations, ordered supported
changes and destructive delivery. Present research and static inspection consumers drove confined
scripts; Challenge consumed outcome facts, and Governance had no simulation producer integration.

## Serious alternatives and decisive rationale

- A disciplined caller-capture contract could preserve the same producer/holder split. It was a
  viable equivalent arrangement, not disproved by examples of bad local completeness rules.
  The existing shared runtime contract was the smallest home for common producer-derived meanings.
- A separate semantic account owner could also express the rules without allocating an entity.
  No inspected use required an additional referent or responsibility beyond producer meaning,
  controller provenance, evidence custody and analytical sufficiency.
- A retained execution-record service could serve independent consumers or pre-join history, but
  those requirements were not established for current uses. It was unjustified then, not universally
  falsified. An analysis-owned evidence session would likewise have conflated definition ownership
  with possession of runtime delivery.
- Stronger producer closure operations could reduce controller trust for idle tails. A stop-result
  API or composite drain added no demonstrated guarantee for the exclusive drivers inspected;
  a clock witness would add temporal evidence while changing command timing and observed-time
  derived results. Those costs were not justified by the current confined consumers.

Independent adversarial reviews exposed why capture and closure had to remain distinct: even an
honest driver exhausting a five-tick job through 100 could leave a holder with stale dispatch-only
evidence and an incorrect 100-tick claim. The remedy bound coverage to the post-advancement proof
cursor, rather than treating an attestation as self-validating. A later gap also failed to erase
an already proved earlier interval.

## Consequences and accepted trade-offs

Shared meaning remained independent of physical custody and analytical formulas. Callers retained
the evidence their uses needed. Idle-tail finality could still depend on an attributable driver
commitment whose honesty a frozen capture could not verify; rejected and no-op requests remained
outside the supported change stream. This narrower choice avoided speculative history machinery
while preserving explicit refusal and qualification for insufficient evidence.

The scheduler-marker defect was a separate Engine correction, not evidence against conservative
interval finality and not a reason to select authoritative-turn advancement here.

## Reconsider when

Selected live-loop requirements need mid-run commands, selected-time input while idle, or finality
for a non-driving party; concurrent runtime access becomes supported; independent consumers need
previous history; or retained results require longer-lived verification or exact producing-definition
provenance. These changed conditions could justify producer evidence or shared delivery. A clock
witness, scheduled inputs and event-boundary interaction remained candidates rather than a
designated future shape. The maintained [research state](../../research/investigations/simulation-execution-account.md#reopening-triggers)
records the active reopening boundaries.
