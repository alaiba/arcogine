package com.arcogine.research.experiment;

/**
 * A research-local derivation over supported evidence.
 *
 * <p>An oracle receives only {@link DeclaredEvidence}: a view of a finished evidence bundle limited
 * to the inputs its {@link ResearchDefinition} declares. It is never given a {@code FactoryRuntime},
 * so nothing in its contract lets it modify runtime truth, and it has no route to scheduler or
 * handler internals, so it has no way to re-decide scheduling or dispatch. It may only measure what
 * the supported evidence licenses, or refuse.
 */
public interface Oracle<T> {

    /** The named definition this oracle implements, including the inputs it may read. */
    ResearchDefinition definition();

    OracleOutcome<T> evaluate(DeclaredEvidence evidence);

    /** Applies this oracle to {@code evidence}, restricted to the inputs the definition declares. */
    default OracleOutcome<T> evaluateOn(ExperimentEvidence evidence) {
        return evaluate(new DeclaredEvidence(evidence, definition()));
    }
}
