package com.arcogine.factory.model;

import com.arcogine.factory.routing.EligibleMachines;
import com.arcogine.types.MachineId;
import java.util.Set;

/**
 * One step of an {@link OperationDefinition}.
 *
 * <p>{@code eligibleResources} represents which installed resources may perform this step.
 * Runtime dispatch selects one of them deterministically at run time (see {@code
 * FactoryHandler}); the model itself owns only eligibility, not availability, queue state, or
 * selection policy. It is an unordered set -- authored order is not retained -- that iterates in
 * ascending {@link MachineId} order ({@link EligibleMachines}); a null set becomes empty.
 */
public record OperationStepDefinition(
        long stepId, String name, Set<MachineId> eligibleResources, long duration) {

    public OperationStepDefinition {
        if (name == null) {
            throw new NullPointerException("name");
        }
        eligibleResources =
                eligibleResources == null ? Set.of() : EligibleMachines.ascending(eligibleResources);
    }
}
