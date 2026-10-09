# Independent pre-review reconstruction

Date: 2026-10-09. High-risk adversarial review, fresh isolated Codex session with no
authorship or obligation to defend the investigator. No different model-family claim.

Target confirmed available in full (67,265 bytes): report commit
`a53778145f3ee9941a4b46c7f04f1ec17e6bff86`,
`workspace/research/investigations/simulation-execution-account-report.md`, branch
`workspace/simulation-execution-account`. Only its first 16 metadata lines have been read.
Report baseline: `79b3399149499f2f337206c909a277f66a5befd5`.
Live main independently resolved: `912b2ac85cefab3907fc562a3aaa6507e51975a7`.
The only baseline drift is the Engine evolution session/advancement research note;
runtime code and normative architecture are identical. The assigned handoff (including
its suggested attacks) and registered brief have been read, so the attack framing is
not blind; candidates and expected outcomes below precede report consumption.

## Current constraints reconstructed from authorities

The Product Charter calls for common business semantics without requiring one store,
runtime, or UI. Runtime-contract observations identify RunId, model fingerprint and
latest supported sequence. New runtime/reset means fresh RunId; reset leaves the old
runtime operational. Current facts and supported changes belong to Engine; the stream
is destructively drained and does not reconstruct authoritative runtime state.

Engine semantics section 1.2 and FactoryRuntime establish two advancement bounds:
the next scheduled time and the internal-event count. Neither guarantees time reaches
the target. FactoryRuntime.observe uses last-supported-change time, not scheduler time.
Internal no-op markers consume internal advancement counts without supported events.
QUIESCENT concerns pending authoritative work, not terminal lifecycle, absence of
waiting jobs, or a prohibition on subsequent commands. CommandResult distinguishes
rejection, accepted effects, and applied changes followed by fault. An accepted
redundant availability request emits nothing.

Governance evidence distinguishes recorded assertion from truth and producer evidence
from contextual applicability. A run ID alone does not identify a time-varying result.
Operational continuation is durable and set-based; simulation's runtime epoch and
total event order are insufficient reasons to adopt its identity or retention contract.

## Serious candidates

1. Existing producer semantics plus a shared caller-capture specification. Physical
   custody can remain local while coverage predicates have one definition.
2. An additional execution-account semantic authority with a distinct responsibility.
   Must prove a distinction the current producer/capture split cannot express.
3. A retained common record service. Might solve fan-out/retrieval, but must show a
   present requirement rather than infer necessity from multiple consumers.
4. An analytics evidence session. Viable for definition-specific requirements; suspect
   as sole authority for verification and other non-analytical completeness claims.
5. A hybrid: Engine defines the meaning of supported boundaries; controller attests
   admission/advancement; holder warrants capture; analytical method defines sufficiency.
   These are separable responsibilities even when one implementation performs all four.

## Hand-derived discriminators

- Idle tail: work ends at 13, requested advancement bound 100, later workload accepted
  at 13. An unconditional final [0,100) occupancy claim is unsound. A promise of no
  more inputs is an attributed controller statement, not an observation fact.
- Budget exhaustion: stop after an internal start marker with completion pending before
  the requested bound. Even an honest promise never to submit again cannot establish
  interval completion. Pending work must execute, or a separately justified forecast
  must be named as prediction.
- Exact boundary: equal-time changes at b may affect the state at b but have zero
  duration in [a,b). Closing all changes <= b is stronger than needed for a pure
  duration integral. Distinguish a conservative sufficient rule from necessity.
- Two drainers: each receives a contiguous chunk but neither has complete history.
  A later authoritative frontier and initial basis expose missing prefix/tail. A
  frozen event list without that basis cannot carry the same coverage warranty.
- Closure violation without event: submit a redundant accepted command after declaring
  no further inputs. No event-sequence check can detect the broken input attestation.
  This need not invalidate state coverage, but does invalidate a no-input claim.
- A queued job while all eligible resources are offline versus an online free slot
  left unused by the bounded recovery rule admits different meaningful waiting
  definitions. History facts cannot choose an analytical readiness convention.
- Reset and equal-time order: combining epochs must fail; ordering by time alone loses
  genuine changes. A retained supported trace cannot recover rejected/no-op inputs or
  internal-event-count interleavings and therefore is not a reproduction script.

## Initial evidence boundary

Read current charter, runtime contract, session/dispatch semantics, relevant ownership
and identity authorities, FactoryRuntime, Scheduler and headless marker/closure tests.
Searched docs and product for run/advancement, gap/frontier, retained-history and
analytical-provenance neighbors. Deeper adjacent research and investigator artifacts
are intentionally deferred until this reconstruction is committed. No disposition is
assigned yet, and no experiment has yet run.
