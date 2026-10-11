# Admit analytical-definition ownership per named method, not capture location

> **Date:** 2026-10-11
>
> **Authority:** Historical, non-normative evidence; current ownership and proof meaning live only in the documents into which this decision was reconciled
>
> **Reconciled into:** [Domain authority](../../architecture/overview.md#domain-authority), [Game consumer planning](../../planning/factory-design-game-consumer.md), [Governance evidence](../../architecture/governance-evidence.md) and the [analytics ownership research state](../../research/investigations/simulation-analytics-consumer-boundary.md)

## Decision and context

The coupled analytics investigation selected **staged, kind-specific admission**: no shared
consumer-neutral analytics definition owner or new Engine measurement was needed for current uses.
Existing Engine-declared results remain authoritative under their exact arithmetic. A new named
method is owned by the capability that chooses its otherwise-unfixed meaning, consumer-local by
default; a common definition requires concrete evidence that several uses need that same meaning.
This was an ownership decision, not a decision to build Game diagnostics or change runtime events,
clock semantics, retained history or the Determinism Contract.

The preceding execution-evidence reconciliation had already made run/sequence, basis/frontier and
claim-specific finality common runtime-contract meanings while leaving evidence retention with its
holder and attributable promises with the controller. Phase 1 classified Engine-owned result and
projection fields; subsequent runtime changes removed the completion-credit `busyTicks` counter.
An initial independent Phase 2 review returned `REOPEN` after exposing unsupported capture-owner
coupling. An interim revision was not independently accepted; the final revision received
**ACCEPT WITH QUALIFICATIONS** after independent re-grounding and adversarial probes.

## Serious alternatives and decisive rationale

- **Engine-rich standard measurement** remained viable if a concrete core/public consumer needed
  one Engine-wide definition. It was not selected from a speculative shared analytics need or
  illustrative utilization objective. The five current derived results remained Engine-defined;
  the revision did not enact the opposite “all measurement outside Engine” extreme.
- **Execution-fixed run-to-date accumulator for a late joiner** remained viable but unrequired.
  A late-joining or non-driving holder might need an aggregate whose pre-join interval was never
  captured, competing with bounded retained/shared delivery and its compatibility cost. Such an
  accumulator can replace historical **retention**, never independently seal an arbitrary interval:
  finality still needs the runtime's producer witness/observation or qualified controller closure.
- **Shared-definition owner now** lacked an actual shared named requirement. A Game calculation
  used at two call sites is not two consumers; an illustrative Governance verification objective is
  not yet a concrete admitted use. A single core verification/Governance requirement *could*
  justify common meaning if bounded and supported by acceptance evidence, without waiting for a
  second shipped consumer.
- **Definition custodian owns every evidence bundle** failed. A stateless method can use evidence
  held by one exclusive driver while a second caller uses the same named definition; promotion
  may transfer method custodianship without moving physical capture. The inverse “holder owns
  every definition” also fails: collecting facts does not select a method's denominator, period,
  availability, grouping or causal interpretation.

No alternative was declared impossible merely because present consumers are static or because
the earlier busy-time counter was removed.

## Consequences and accepted trade-offs

This narrower rule avoids prematurely adding a shared module, universal identity scheme, Engine
history store or Game-owned “generic analytics.” It also makes later adoption conditional:
unchanged formulas and sufficient inputs are not enough for durable result portability unless
the original run/model basis, interval, proof frontier/class and applicable controller trust,
fault and input-exclusion provenance survive. The exact producing Engine-definition reference
remains independently required only for admitted cross-revision/reproduction uses.

The independent review found **two false acceptances in a research-only proof helper**, not the
landed runtime contract: mixing an observation basis from the wrong run can report ten ticks
instead of five; and using a stale controller closure while holding a contradictory completion
can report 100 instead of five. Method-level rejection must bind run identity and inspect held
changes against the closure. The separate stale-holder 100-versus-five case and survival of an
earlier proved prefix after a later gap were already captured by the
[execution-evidence contract](../../architecture/runtime-contract.md#captured-execution-evidence-and-interval-determinacy)
and its [earlier rationale](2026-10-09-captured-simulation-evidence.md); copying their research
probes into a production test framework was not justified.

A fresh observation may answer “no currently known work”; an aggregate over an idle tail needs
interval finality; “no work left for the rest of the period” additionally claims excluded future
input. The reviewed Game research used a job-free starting basis, online resources, one product and
constructive closed scripts, not a universal diagnostic method. The Game's own diagnostic
requirements and control-admission checkpoint remained separate, so no playable work was unblocked.

## Reconsider when

An actual shared named method is demanded by two consumers, a bounded core verification/Governance
use, an admitted outward contract or drifting duplicate meanings; a holder cannot capture a needed
execution-fixed run-to-date fact from establishment; a concrete passive/cross-session use requires
different retention, provenance or trust; a Game method with explicit input/evidence refusal is
admitted; or a producing Engine revision comparison exposes a real semantic gap. These are
conditional future inputs, not tasks declared complete by this rationale record.

## Provenance

The [concluded research state](../../research/investigations/simulation-analytics-consumer-boundary.md)
identifies the decision and its bounded reopening conditions. Git and the independently reviewed
reconciliation pull-request history provide delivery provenance; temporary research reports and
probes were deliberately not promoted as maintained artifacts.
