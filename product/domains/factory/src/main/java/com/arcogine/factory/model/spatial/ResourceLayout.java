package com.arcogine.factory.model.spatial;

import com.arcogine.types.MachineId;
import java.util.Objects;

/**
 * The authored placement and footprint of one configured resource inside a {@link SpatialRecord},
 * keyed by that resource's {@link MachineId}.
 *
 * <p>This entry holds only spatial facts. The resource's own facts -- name, concurrency, capacity,
 * setup time -- stay owned by its {@link com.arcogine.factory.model.ConfiguredResource} in
 * {@link com.arcogine.factory.model.FactoryModel#resources()}; a layout never carries a second
 * copy of them.
 */
public record ResourceLayout(
        MachineId resourceId, ResourcePlacement placement, ResourceFootprint footprint) {

    public ResourceLayout {
        Objects.requireNonNull(resourceId, "resourceId");
        Objects.requireNonNull(placement, "placement");
        Objects.requireNonNull(footprint, "footprint");
    }
}
