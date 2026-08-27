package com.arcogine.factory.model;

import com.arcogine.types.MachineId;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * One step of an {@link OperationDefinition}.
 *
 * <p>{@code eligibleResources} represents which installed resources may perform this step.
 * Runtime dispatch selects one of them deterministically at run time (see {@code
 * FactoryHandler}); the model itself owns only eligibility, not availability, queue state, or
 * selection policy.
 */
public record OperationStepDefinition(
        long stepId, String name, Set<MachineId> eligibleResources, long duration) {

    public OperationStepDefinition {
        if (name == null) {
            throw new NullPointerException("name");
        }
        eligibleResources =
                eligibleResources == null
                        ? Set.of()
                        : Set.copyOf(new LinkedHashSet<>(eligibleResources));
    }
}
