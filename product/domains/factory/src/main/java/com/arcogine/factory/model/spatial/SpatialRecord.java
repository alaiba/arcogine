package com.arcogine.factory.model.v2;

import java.util.List;
import java.util.Objects;

/**
 * The optional {@code factory-model:v2} spatial record: the authored plant floor extent, the
 * material-handling magnitudes {@code ticksPerCell} and {@code handlingTicks}, and one
 * {@link ResourceLayout} per configured resource.
 *
 * <p>A {@link FactoryModelV2} carries this record whole or not at all. Its presence is an authored
 * assertion about space and handling; its absence asserts nothing in that dimension, so no floor,
 * placement, footprint, or zero magnitude is ever synthesized for a model without it. A present
 * record whose magnitudes are zero is therefore a different authored design from an absent record.
 *
 * <p>Layouts reference configured resources by {@link com.arcogine.types.MachineId} instead of
 * embedding them, so {@link FactoryModelV2#resources()} stays the single authority for resource
 * facts. Exactly one layout per resource, in resource-list order, is a publication predicate
 * checked by {@link FactoryModelV2Validator}; range validity is likewise left to the validator.
 */
public record SpatialRecord(
        FactoryFloor floor, long ticksPerCell, long handlingTicks, List<ResourceLayout> resourceLayouts) {

    public SpatialRecord {
        Objects.requireNonNull(floor, "floor");
        resourceLayouts = resourceLayouts == null ? List.of() : List.copyOf(resourceLayouts);
    }
}
