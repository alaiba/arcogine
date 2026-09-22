package com.arcogine.governance.conformance;

import java.util.Objects;
import java.util.UUID;

/** Opaque identity for one accepted act of evaluating a requirement/assertion pair. */
public record EvaluationOccurrenceId(UUID value) {

    public EvaluationOccurrenceId {
        Objects.requireNonNull(value, "value");
    }

    public static EvaluationOccurrenceId generate() {
        return new EvaluationOccurrenceId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
