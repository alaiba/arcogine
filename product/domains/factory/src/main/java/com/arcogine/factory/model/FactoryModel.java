package com.arcogine.factory.model;

import java.util.List;

/**
 * The canonical, consumer-neutral semantic definition of a designed production system: its
 * resources, operations, and products.
 *
 * <p>This is deliberately narrow. Simulation configuration, RNG seed, economy configuration,
 * agents, workload, and any other execution/run concern are never part of this type -- see
 * ADR-0003 (canonical factory model boundary). {@link FactoryModel} is a plain, immutable value;
 * it has no dependency on Spring, scenario parsing, or any runtime/mutable state.
 */
public record FactoryModel(
        List<ResourceDefinition> resources,
        List<OperationDefinition> operations,
        List<ProductDefinition> products) {

    public FactoryModel {
        resources = resources == null ? List.of() : List.copyOf(resources);
        operations = operations == null ? List.of() : List.copyOf(operations);
        products = products == null ? List.of() : List.copyOf(products);
    }
}
