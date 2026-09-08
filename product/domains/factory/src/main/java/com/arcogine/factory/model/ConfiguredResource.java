package com.arcogine.factory.model;

import com.arcogine.types.MachineId;

/**
 * One independently identified, completely configured productive participant in a published
 * factory design.
 *
 * <p>This record is the canonical configured-resource concept. It is not a reusable equipment
 * specification, physical asset, runtime machine, catalogue item, work center, resource pool, or
 * capability. Repeated catalogue/template origin, equal configured values, or installing the same
 * authored item more than once do not imply a second reusable definition identity.
 */
public record ConfiguredResource(
        MachineId id, String name, int concurrency, Double capacityLiters, long setupTime) {

    public ConfiguredResource {
        if (id == null) {
            throw new NullPointerException("id");
        }
        if (name == null) {
            throw new NullPointerException("name");
        }
    }
}
