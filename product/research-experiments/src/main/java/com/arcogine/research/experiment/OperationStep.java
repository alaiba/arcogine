package com.arcogine.research.experiment;

import java.util.Objects;

/**
 * One operation step of a published model, identified as the model identifies it: its operation,
 * its step identifier within that operation, and its authored name.
 */
public record OperationStep(long operationId, long stepId, String name) {

    public OperationStep {
        Objects.requireNonNull(name, "name");
    }
}
