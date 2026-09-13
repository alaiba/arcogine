package com.arcogine.factory.model.v2;

import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.ProductDefinition;
import java.util.List;

/**
 * The canonical {@code factory-model:v2} semantic definition of a designed production system:
 * exactly the {@code factory-model:v1} semantic content (operations, products, and each
 * resource's V1 fields) plus the five authored spatial/handling facts ADR-0014 requires -- floor
 * extent, per-resource reference-cell position, per-resource footprint, {@code ticksPerCell}, and
 * {@code handlingTicks}.
 *
 * <p>This type is deliberately not {@link FactoryModel} and shares no supertype with it. That is
 * what makes it mechanically impossible -- a compile error, not a runtime check -- to pass a
 * {@code FactoryModelV2} to {@link FactoryModelPublisher#publish(FactoryModel)} or to construct a
 * {@link FactoryModelVersion} from one. {@code factory-model:v2} authored content can therefore
 * never travel through the existing {@code factory-model:v1} publication/fingerprint path.
 *
 * <p>V2 does not yet have its own publication, fingerprint, or canonical-artifact path -- that is
 * a later, separate implementation slice (see {@code docs/architecture/factory-model-v2.md}).
 * This type exists to give V2's authored semantic content a complete model shape and support
 * deterministic validation ({@link FactoryModelV2Validator}) ahead of that work.
 *
 * <p>{@link OperationDefinition} and {@link ProductDefinition} are reused unchanged: V2 adds no
 * new operation or product semantics, only spatial/handling facts about resources and the plant.
 */
public record FactoryModelV2(
        FactoryFloor floor,
        long ticksPerCell,
        long handlingTicks,
        List<SpatialConfiguredResource> resources,
        List<OperationDefinition> operations,
        List<ProductDefinition> products) {

    public FactoryModelV2 {
        if (floor == null) {
            throw new NullPointerException("floor");
        }
        resources = resources == null ? List.of() : List.copyOf(resources);
        operations = operations == null ? List.of() : List.copyOf(operations);
        products = products == null ? List.of() : List.copyOf(products);
    }

    /**
     * Projects this model's V1-shaped semantic content (each resource's base V1 fields,
     * operations, and products) into a plain {@link FactoryModel}, so
     * {@link FactoryModelV2Validator} can delegate the existing deterministic V1/base-semantic
     * validation ({@link com.arcogine.factory.model.validation.FactoryModelValidator}) rather
     * than duplicating it.
     *
     * <p>Package-private and validation-only by design: the projection deliberately discards
     * every V2 spatial/handling fact, so it must never become a public "downgrade to V1"
     * conversion that a caller could publish under {@code factory-model:v1} while believing it
     * still represents this V2 design's full authored content.
     */
    FactoryModel baseModel() {
        List<ConfiguredResource> baseResources =
                resources.stream().map(SpatialConfiguredResource::resource).toList();
        return new FactoryModel(baseResources, operations, products);
    }
}
