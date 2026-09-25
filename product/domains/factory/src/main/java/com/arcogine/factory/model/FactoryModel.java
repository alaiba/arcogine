package com.arcogine.factory.model;

import com.arcogine.factory.model.spatial.SpatialRecord;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The canonical, consumer-neutral semantic definition of a designed production system: its
 * resources, operations, and products, plus an optional authored {@link SpatialRecord}.
 *
 * <p>This is deliberately narrow. Simulation configuration, RNG seed, commercial policy,
 * decision-makers, workload, and any other execution/run concern are never part of this type -- see
 * the canonical model boundary (docs/architecture/factory-design.md). {@link FactoryModel} is a
 * plain, immutable value;
 * it has no dependency on any input format, application framework, or runtime/mutable state.
 *
 * <p>The spatial record is present whole or absent. {@link Optional#empty()} is an authored
 * absence: the design makes no spatial or handling assertion, and nothing is synthesized in its
 * place. A null {@code spatial} component is rejected so absence is never implied by omission; the
 * three-argument constructor is the explicit spelling of a production-only design.
 */
public record FactoryModel(
        List<ConfiguredResource> resources,
        List<OperationDefinition> operations,
        List<ProductDefinition> products,
        Optional<SpatialRecord> spatial) {

    public FactoryModel {
        resources = resources == null ? List.of() : List.copyOf(resources);
        operations = operations == null ? List.of() : List.copyOf(operations);
        products = products == null ? List.of() : List.copyOf(products);
        spatial = Objects.requireNonNull(spatial, "spatial");
    }

    /** A production-only design whose spatial record is explicitly absent. */
    public FactoryModel(
            List<ConfiguredResource> resources,
            List<OperationDefinition> operations,
            List<ProductDefinition> products) {
        this(resources, operations, products, Optional.empty());
    }
}
