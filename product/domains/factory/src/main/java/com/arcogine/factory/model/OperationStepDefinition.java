package com.arcogine.factory.model;

import com.arcogine.types.MachineId;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * One step of an {@link OperationDefinition}.
 *
 * <p>{@code eligibleResources} represents which installed resources may perform this step. Today
 * a scenario's {@code process_segment} binds to exactly one {@code equipment_id}, so the set
 * always has a single member; the set shape is used so a future capability-based eligibility
 * representation does not require changing this type's identity, without this milestone actually
 * implementing multi-resource dispatch.
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
