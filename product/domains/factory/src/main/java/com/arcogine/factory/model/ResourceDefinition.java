package com.arcogine.factory.model;

import com.arcogine.types.MachineId;

/**
 * A resource (piece of equipment) that can be installed in a factory design.
 *
 * <p>Current scenarios do not distinguish a reusable resource "type" from an installed
 * instance -- each configured resource is a single concrete, uniquely identified unit. This
 * type therefore represents both facts at once; splitting definition from instance is deferred
 * until a scenario/consumer actually needs a resource type installed more than once.
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
