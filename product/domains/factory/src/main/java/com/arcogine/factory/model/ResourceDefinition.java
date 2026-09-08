package com.arcogine.factory.model;

import com.arcogine.types.MachineId;

/**
 * A complete description of one identified productive resource in a factory design.
 *
 * <p>Despite the historical type name, this record is not a reusable equipment type. The
 * canonical model currently represents each configured resource directly. Repeated
 * catalogue/template origin, equal configured values, or installing the same authored item more
 * than once do not imply a second reusable definition identity. Introduce such a specification
 * only if it carries a checkable cross-consumer technical contract or dependency that complete
 * configured-resource records cannot preserve.
 */
public record ResourceDefinition(
        MachineId id, String name, int concurrency, Double capacityLiters, long setupTime) {

    public ResourceDefinition {
        if (id == null) {
            throw new NullPointerException("id");
        }
        if (name == null) {
            throw new NullPointerException("name");
        }
    }
}
