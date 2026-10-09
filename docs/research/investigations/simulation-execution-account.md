# Simulation execution account semantics

> **Status:** CONCLUDED — see the maintained [research register](../research-register.md)
> **Risk:** High — cross-consumer execution identity, evidence coverage and temporal finality
> **Scope:** Minimum shared meaning of evidence about one simulated execution
> **Authority:** Research state and reopening conditions only; current semantics live in the [runtime observation and event contract](../../architecture/runtime-contract.md#captured-execution-evidence-and-interval-determinacy).

## Concluded question

> What minimum consumer-neutral semantic account, if any, of one simulated execution must Arcogine define so independent capabilities can reason about the same execution without reconstructing Engine behavior, making the Engine event sourced, or independently assembling incompatible notions of execution history?

## Verdict and durable destination

The independently reviewed result was accepted with qualifications and reconciled into the existing
runtime observation/event contract. Its shared evidence rules are sufficient for current confined
and static consumers, with caller-held capture. No separate execution-account entity, identifier,
module, retained-record service, terminal runtime state or physical persistence contract is admitted.

The canonical contract owns run/sequence meaning, observation basis, claim-scoped supported-change
coverage and half-open interval determinacy. Controllers supply admission/advancement provenance
and attributable closure commitments; holders retain and qualify evidence; analytical definitions
specify sufficiency. These responsibilities may coexist in one component. Their separation does
not determine the unresolved analytics owner.

The qualifications are part of the canonical result: finality requires coverage to the exact proof
sequence, not a stale or merely locally contiguous frontier; controller closure retains its trust,
outcome and handover conditions; a valid earlier prefix survives later gaps; and complete supported
changes imply neither fault-free success nor complete commands, analytical applicability or rerun
inputs. Waiting-state residence does not select a readiness, utilization or causal definition.

The separate scheduler-marker correction is an Engine-specification responsibility. This result
neither selects its remedy nor assumes equality between internal and supported clocks. Any later
command-time or stepping change must be checked against these evidence rules and the owning
[Engine session semantics](../../architecture/engine-semantics.md#12-session-and-control-semantics);
it cannot turn a requested advancement deadline into an interval-finality witness implicitly.

Selected rationale for retaining this boundary rather than a new history owner is preserved as
[non-normative historical evidence](../../history/decisions/2026-10-09-captured-simulation-evidence.md).
The canonical contract contains the useful stale-frontier, idle-tail and earlier-prefix
counterexamples and remains sufficient without the original research helpers or reports.

## Downstream continuation

The [analytics ownership question](simulation-analytics-consumer-boundary.md) remains **READY**.
After this reconciliation lands, its prospective Phase 2 can resume using the qualified runtime
contract. Phase 1's current-boundary classification remains evidence. Phase 2 must separate shared
coverage semantics, controller/holder warranties and physical custody from analytical sufficiency,
accumulation and definition ownership, then obtain its own independent adversarial review and
reconciliation. Prior candidate admission rules are not adopted here.

The [analytics evidence/provenance question](simulation-analytics-evidence-provenance.md) remains
**CANDIDATE** until a concrete analytical responsibility/use is established. Its remaining concerns
are additional inputs, method-specific sufficiency, accumulation, definition identity, result
provenance and evidenced retention needs. Execution identity, ordering and coverage are upstream
contract inputs, not questions to decide again.

Game diagnostic reconciliation remains separately owned. It must evaluate control requirements
before playable admission under the [consumer plan](../../planning/factory-design-game-consumer.md#6-playable-implementation-admission)
and [vertical-slice gate](../../planning/factory-design-game-vertical-slice.md). This conclusion does
not complete that research or select a live interactive loop.

## Reopening triggers

| Concrete requirement or new evidence | Boundary to revisit |
|---|---|
| A selected/promoted live-loop requirement includes mid-run commands | Before playable admission, require a landed safe pacing/command-time rule or a truthful bounded protocol under Engine semantics; static recorded or start-fixed playback at any speed does not trigger this |
| Commands must take effect at a selected simulated time while idle | Compare a supported clock witness, scheduled time-stamped inputs and event-boundary interaction; no shape is designated in advance |
| A non-driving party must establish past-interval finality, including control handover | Reassess producer evidence versus scoped controller trust and preserved outcome/attestation provenance; a later clean call cannot establish earlier fault-free success |
| Supported concurrent access to one runtime | Reassess control exclusion, coherent capture and any composite advancement/delivery guarantee |
| An independent live consumer, pre-join history requirement or long-horizon third-party verification | Define only the evidenced shared delivery, retention/recovery or integrity obligation; central custody is conditional |
| A passive consumer needs command/fault outcomes missing from supported changes | Revisit the producer/control provenance seam before making those claims |
| Retained results must be reproduced or compared across producing revisions | Consume the [exact-reference questions](semantic-equivalence-and-reference-boundaries.md) and [cross-revision comparability question](simulation-analytics-cross-revision-comparability.md); run ID and model fingerprint are insufficient |
| Executable spatial transfer behavior lands | Recheck time/order, late-basis and separate residence semantics against executable evidence; the current compatibility argument is specification-derived |
| A use needs durable identity for the inspection/analysis session itself | Revisit that use with the owning Operational/Governance boundary; do not equate simulation run identity with [accountable operational continuation](../../architecture/operational-continuity.md) |

These triggers identify material changed conditions; they create no standing investigation,
implementation admission or new closure API by themselves.
