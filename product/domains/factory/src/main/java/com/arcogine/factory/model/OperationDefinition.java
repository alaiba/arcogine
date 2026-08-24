package com.arcogine.factory.model;

import java.util.List;

/** An ordered sequence of {@link OperationStepDefinition}s a product may be routed through. */
public record OperationDefinition(long id, String name, List<OperationStepDefinition> steps) {

    public OperationDefinition {
        if (name == null) {
            throw new NullPointerException("name");
        }
        steps = steps == null ? List.of() : List.copyOf(steps);
    }
}
