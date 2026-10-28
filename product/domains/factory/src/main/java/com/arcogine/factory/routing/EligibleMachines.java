package com.arcogine.factory.routing;

import com.arcogine.types.MachineId;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * The one representation of every engine-owned eligible-machine set.
 *
 * <p>Eligibility is an unordered set: authored order is not retained, duplicates collapse, and
 * equality is {@link Set} equality. Its iteration order is nevertheless fixed to ascending signed
 * {@link MachineId}, a property of the set's contents alone, so no consumer observes an order that
 * depends on authoring or on the JVM's hash salt (docs/architecture/engine-semantics.md section 2).
 */
public final class EligibleMachines {

    private EligibleMachines() {}

    /**
     * Returns an unmodifiable copy of {@code machines} that iterates in ascending {@link
     * MachineId} order.
     *
     * @throws NullPointerException if {@code machines} is null or contains a null element
     */
    public static Set<MachineId> ascending(Collection<MachineId> machines) {
        return Collections.unmodifiableSet(new TreeSet<>(machines));
    }
}
