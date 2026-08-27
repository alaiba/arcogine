package com.arcogine.factory.routing;

import com.arcogine.types.MachineId;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * One step of a {@link Routing}.
 *
 * <p>{@code eligibleMachines} carries every machine the published model allows to perform this
 * step; dispatch (see {@code FactoryHandler}) selects one of them deterministically at run time.
 * It is never empty -- {@code FactoryModelValidator} rejects a step with no eligible resources
 * before a model can be published.
 */
public record RoutingStep(long stepId, String name, Set<MachineId> eligibleMachines, long duration) {

    public RoutingStep {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (eligibleMachines == null || eligibleMachines.isEmpty()) {
            throw new IllegalArgumentException("eligibleMachines must not be empty");
        }
        eligibleMachines = Set.copyOf(new LinkedHashSet<>(eligibleMachines));
    }

    /** Convenience constructor for the common single-eligible-machine case. */
    public RoutingStep(long stepId, String name, MachineId machineId, long duration) {
        this(stepId, name, Set.of(machineId), duration);
    }
}
