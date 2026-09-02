package com.arcogine.governance.assertion;

import java.util.Objects;

/**
 * Stable, Arcogine-owned identity for one {@link Assertion} lineage.
 *
 * <p>Deliberately never derived from a Java class name or other evaluator implementation detail
 * -- an assertion's identity must survive re-implementation (e.g. rewriting the evaluator in a
 * different language or moving it to another package) exactly as a requirement's identity must
 * survive rewording.
 */
public record AssertionId(String value) {

    public AssertionId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
