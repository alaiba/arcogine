package com.arcogine.factory.research;

import java.util.Objects;
import java.util.Optional;

/**
 * Ground truth for one research-local derivation on one fixture, stated independently of the run.
 *
 * <p>The expected value belongs to the fixture and is never stored in, or derived from, the
 * evidence bundle a run produces. An empty expectation means the evidence must <em>not</em> license
 * the claim: the oracle is expected to refuse.
 */
public record ExpectedClaim<T>(String name, Oracle<T> oracle, Optional<T> expected) {

    public ExpectedClaim {
        if (Objects.requireNonNull(name, "name").isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        Objects.requireNonNull(oracle, "oracle");
        Objects.requireNonNull(expected, "expected");
    }

    /** The oracle is expected to derive exactly {@code expected}. */
    public static <T> ExpectedClaim<T> derives(String name, Oracle<T> oracle, T expected) {
        return new ExpectedClaim<>(name, oracle, Optional.of(expected));
    }

    /** The supported evidence does not license the claim, so the oracle is expected to refuse. */
    public static <T> ExpectedClaim<T> refuses(String name, Oracle<T> oracle) {
        return new ExpectedClaim<>(name, oracle, Optional.empty());
    }

    /** Applies the oracle to {@code evidence} and pairs the outcome with this expectation. */
    public ClaimCheck<T> check(ExperimentEvidence evidence) {
        return new ClaimCheck<>(name, expected, oracle.evaluateOn(evidence));
    }

    /** An expectation paired with what the oracle actually produced. */
    public record ClaimCheck<V>(String name, Optional<V> expected, OracleOutcome<V> actual) {

        /** True when a derived value equals the expectation, or a refusal was expected and given. */
        public boolean holds() {
            return switch (actual) {
                case OracleOutcome.Derived<V> derived ->
                    expected.isPresent() && expected.get().equals(derived.value());
                case OracleOutcome.Underdetermined<V> refused -> expected.isEmpty();
            };
        }
    }
}
