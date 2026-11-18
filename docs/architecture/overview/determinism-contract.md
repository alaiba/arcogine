# Determinism Contract

Part of the [architecture overview](../overview.md#routed-topics); it is mandatory whenever the overview routes your change here.


Deterministic simulation is architectural, not an implementation convenience: acceptance tests,
comparison of design candidates, historical explanation of a run, and challenge evaluation are all
meaningless if executions under the same definition and complete inputs can legitimately disagree. At the same
time Arcogine must be able to change how it interprets a design — dispatch ranking, work
decomposition, scheduling — without pretending the designer authored a different
production system, and without claiming that historical results were produced under rules they were
not. The [canonical definition of determinism](determinism-and-durability.md#determinism) supplies the computation and comparison
scope; the following rules specialize it for simulation. Five rules hold that line:

1. **A simulation outcome is a function of explicit inputs and one Engine interpretation.** The
   reproducibility inputs are the authored model content, the Engine interpretation, the explicit
   workload, the seed and other random inputs, the ordered external commands including advancement
   interleavings and budgets, and any other explicitly identified result-affecting input. Nothing
   else may influence acceptance, rejection, assignment, ordering, simulated time, state at the
   declared boundary or derived results; run identity is correlation metadata and never affects
   an outcome.
2. **No result-affecting rule may remain ambient.** Any limit, ordering rule, tie-break, rounding or
   accumulation rule that two implementations could choose differently is part of the
   interpretation or is an explicitly identified input. One interpretation covers a run's complete
   result-affecting behavior and is fixed when the run is established.
   [Engine semantics](../engine-semantics.md) owns the exact rules, their membership test and the
   conformance fixtures that pin them.
3. **Authored facts and interpretation have different owners.** Facts describing the production
   system the designer authored belong to the canonical model and its fingerprint; rules describing
   how Arcogine interprets any such design belong to the Engine interpretation. Changing
   interpretation alone never changes the authored model's fingerprint, and authored facts are never
   synthesized to make an interpretation applicable.
4. **Interpretation changes are explicit.** An intentional change to result-affecting behavior — a
   bug fix that observably changes outcomes included — is a definition change recorded in the
   specification and its fixtures in the same change; it is never unrecorded implementation drift.
   Repairing an implementation so that it conforms to the specification is not such a change. The
   current implementation executes the repository's current Engine definition; no placeholder
   identifier is emitted to imply stronger provenance.
5. **Historical basis and support commitments are separate.** Development results may have
   traceability and reproducibility when their exact basis and capabilities are retained. Neither a
   model fingerprint, a run ID, nor a human development label supplies the exact Engine definition
   basis after it changes. Commitment-bearing admission requires an explicit promotion. A promoted
   commitment states which exact references, definitions, fixtures, and content remain resolvable
   for its accepted uses; historical interpretability does not automatically promise permanent
   re-execution.
   Comparison across interpretations or development revisions is explicit and owned by the
   consumer making the claim. Neither a shared name nor a matching result from one run proves
   behavioral equivalence.

Nondeterministic boundaries a consumer needs to replay — clocks, external inputs, human or agent
decisions — are converted into recorded explicit inputs rather than admitted into the
interpretation. Runtime provenance carries the source model's fingerprint and run correlation. No
dedicated exact Engine-definition reference is currently emitted; a consumer that needs one must use
a separately established exact basis rather than infer it from development status
([runtime contract](../runtime-contract.md)).

The current implementation realizes this contract with:

- Priority queue orders events by time, with FIFO tie-breaking
- Java strict floating-point semantics; compilation targets the Java 21 compatibility baseline
- No concurrent mutation of simulation state: a `FactoryRuntime` is a single-owner session ([runtime contract](../runtime-contract.md#a-runtime-is-a-single-owner-session)), and simulation code creates no threads

Given the same published factory model, current Engine definition, and explicit workload/control script,
fresh `FactoryRuntime` sessions produce identical ordered supported `RuntimeEvent` streams and
`RuntimeObservation` state at the same declared boundary. Tests comparing semantic outcomes account
for the per-run `RunId`; a test that depends on that correlation identity is wrong. Supported change
history alone is not that control script; the [runtime evidence contract](../runtime-contract.md#captured-execution-evidence-and-interval-determinacy)
defines its coverage and finality limits without introducing a terminal runtime state.

This determinism contract is scoped to simulation, replay, and verification contexts, where it is a critical property. It is not a claim that real-world execution itself must be, or will be made, deterministic — production operates in a non-deterministic world of real machines, people, and failures. See the Product Charter's [continuity with current architecture](/docs/product/charter.md#8-continuity-with-current-architecture) section for this distinction.
