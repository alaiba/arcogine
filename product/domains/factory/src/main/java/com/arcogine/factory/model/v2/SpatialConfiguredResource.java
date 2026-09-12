package com.arcogine.factory.model.v2;

import com.arcogine.factory.model.ConfiguredResource;

/**
 * One {@code factory-model:v2} configured resource: the existing {@link ConfiguredResource} V1
 * semantic content, plus its authored {@link ResourcePlacement} and {@link ResourceFootprint}.
 *
 * <p>This composes rather than duplicates {@link ConfiguredResource}: {@code id}, {@code name},
 * {@code concurrency}, {@code capacityLiters}, and {@code setupTime} remain owned by the existing
 * V1 record and its own semantics. Placement and footprint are the only facts this type adds, so
 * that V1's resource ontology is never duplicated into a second one.
 */
public record SpatialConfiguredResource(
        ConfiguredResource resource, ResourcePlacement placement, ResourceFootprint footprint) {

    public SpatialConfiguredResource {
        if (resource == null) {
            throw new NullPointerException("resource");
        }
        if (placement == null) {
            throw new NullPointerException("placement");
        }
        if (footprint == null) {
            throw new NullPointerException("footprint");
        }
    }
}
