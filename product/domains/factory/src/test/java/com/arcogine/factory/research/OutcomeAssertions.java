package com.arcogine.factory.research;

/** Test helpers that state, in one place, what an oracle outcome must be. */
final class OutcomeAssertions {

    private OutcomeAssertions() {}

    static <T> OracleOutcome.Derived<T> assertDerived(OracleOutcome<T> outcome) {
        if (outcome instanceof OracleOutcome.Derived<T> derived) {
            return derived;
        }
        throw new AssertionError("expected a derived value but the oracle refused: " + outcome);
    }

    static <T> OracleOutcome.Underdetermined<T> assertUnderdetermined(OracleOutcome<T> outcome) {
        if (outcome instanceof OracleOutcome.Underdetermined<T> refused) {
            return refused;
        }
        throw new AssertionError("expected a refusal but the oracle derived: " + outcome);
    }
}
