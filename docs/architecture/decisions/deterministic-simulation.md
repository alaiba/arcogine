# Deterministic simulation

## Context

Arcogine's simulation, replay and verification contexts depend on reproducing the
same outcome from the same inputs: acceptance tests, comparison of design candidates,
historical explanation of a run, and challenge evaluation are all meaningless if two
executions of the same explicit inputs can legitimately disagree. At the same time,
Arcogine must be able to change how it interprets a design — dispatch ranking, work
decomposition, scheduling, transfer timing — without pretending the designer authored
a different production system, and without claiming that historical results were
produced under rules they were not.

Real operation is not deterministic, and this decision makes no claim about it. It
constrains simulation, replay and verification only.

## Decision

1. **A simulation outcome is a function of explicit inputs and an identified
   interpretation.** The reproducibility inputs are the authored model identity, the
   Engine interpretation identity, the explicit workload, the seed and other random
   inputs, the ordered external commands, and any other explicitly identified
   result-affecting input. Nothing else may influence acceptance, rejection,
   assignment, ordering, simulated time, terminal state or derived results. Run
   identity is correlation metadata and must never affect the outcome.
2. **No result-affecting rule may remain ambient.** Any limit, ordering rule,
   tie-break, rounding or accumulation rule that two implementations could choose
   differently is part of the identified interpretation or an explicitly identified
   input. Hash or set iteration order is never a semantic tie-breaker; same-time
   events are ordered by an explicit rule.
3. **Authored facts and interpretation have different owners.** Facts describing the
   production system the designer authored belong to the canonical model and its
   fingerprint. Rules describing how Arcogine interprets any such design belong to the
   Engine interpretation identity. Changing interpretation alone does not change the
   authored model's identity, and authored facts are never synthesized to make an
   interpretation applicable.
4. **One interpretation identity covers the complete result-affecting
   interpretation of a run.** It is fixed when the run is established and cannot
   change mid-run. Dispatch, decomposition, scheduling and transfer rules interact to
   produce one outcome, so they are not versioned independently unless a concrete need
   proves two concerns must evolve separately.
5. **An intentional change to result-affecting behavior is a new interpretation
   identity**, including a bug fix that observably changes outcomes. Repairing an
   implementation so that it conforms to the identified interpretation is not a
   change of interpretation. Implementations declare which interpretations they
   execute and refuse unsupported ones rather than silently substituting current
   behavior.
6. **The durability guarantee is attribution plus a verifiable definition, not
   permanent re-execution.** Every interpretation that has attributed results keeps
   its identifier, normative specification and conformance fixtures, so historical
   results remain attributable and interpretable after execution support is retired.
   Cross-interpretation comparison is explicit and owned by the consumer making the
   claim; identity never authorizes guessing that results are comparable.

## Consequences

- Runtime provenance carries the authored model identity and the Engine
  interpretation identity on every supported observation and event; consumers can
  state exactly what produced a result.
- Every released interpretation has a normative specification enumerating its
  result-affecting rules and behavioral fixtures pinning semantic outcomes, not
  transport bytes or projections. [Engine Semantics v1](../engine-semantics-v1.md)
  is the current one.
- Nondeterministic boundaries a consumer needs to replay — clocks, external inputs,
  human or agent decisions — are converted into recorded explicit inputs rather than
  admitted into the interpretation.
- Tests comparing semantic outcomes normalize or inject run identity and compare the
  deterministic stream; a test that depends on run identity is wrong.
- Retiring an interpretation removes executability, not provenance; a change to this
  decision would have to reconcile every consumer that relies on historical
  attribution.

## Alternatives

- **Treat the authored model identity as sufficient provenance.** Rejected: the
  same design legitimately produces different outcomes under different accepted
  interpretations, so results would be attributed to a design that did not determine
  them.
- **Use build or release identity as the interpretation identity.** Rejected: builds
  change for many semantics-preserving reasons and carry no stable meaning; build
  identity remains diagnostic only.
- **Guarantee permanent exact re-execution of every interpretation.** Rejected: an
  unbounded commitment to keep every historical implementation runnable, when an
  immutable specification plus fixtures already gives durable verification.
