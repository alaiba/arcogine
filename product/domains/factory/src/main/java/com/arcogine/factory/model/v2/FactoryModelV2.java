package com.arcogine.factory.model.v2;

import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.ProductDefinition;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The canonical {@code factory-model:v2} semantic definition of a designed production system:
 * the required production records -- configured resources, operations, and products, with exactly
 * the {@code factory-model:v1} field meanings -- plus an optional {@link SpatialRecord} carrying
 * the five authored spatial/handling facts docs/architecture/factory-model-v2.md defines.
 *
 * <p>The spatial record is present whole or absent. {@link Optional#empty()} is an explicit
 * authored absence: the design makes no spatial or handling assertion, and nothing is synthesized
 * in its place. A null {@code spatial} component is rejected so absence is never implied.
 *
 * <p>This type is deliberately not {@link FactoryModel} and shares no supertype with it, whether
 * or not its spatial record is present. That is what makes it mechanically impossible -- a compile
 * error, not a runtime check -- to pass a {@code FactoryModelV2} to
 * {@link FactoryModelPublisher#publish(FactoryModel)} or to construct a
 * {@link FactoryModelVersion} from one. {@code factory-model:v2} authored content can therefore
 * never travel through the existing {@code factory-model:v1} publication/fingerprint path.
 *
 * <p>V2 does not yet have its own publication, fingerprint, or canonical-artifact path. This type
 * gives V2's authored semantic content a complete model shape and supports deterministic
 * validation ({@link FactoryModelV2Validator}) ahead of that work.
 */
public record FactoryModelV2(
        List<ConfiguredResource> resources,
        List<OperationDefinition> operations,
        List<ProductDefinition> products,
        Optional<SpatialRecord> spatial) {

    public FactoryModelV2 {
        resources = resources == null ? List.of() : List.copyOf(resources);
        operations = operations == null ? List.of() : List.copyOf(operations);
        products = products == null ? List.of() : List.copyOf(products);
        spatial = Objects.requireNonNull(spatial, "spatial");
    }

    /**
     * Projects this model's production records into a plain {@link FactoryModel}, so
     * {@link FactoryModelV2Validator} can delegate the existing deterministic V1-shaped validation
     * ({@link com.arcogine.factory.model.validation.FactoryModelValidator}) rather than
     * duplicating it.
     *
     * <p>Package-private and validation-only by design: the projection discards the spatial
     * record, so it must never become a public "downgrade to V1" conversion that a caller could
     * publish under {@code factory-model:v1} as though it still represented this V2 design.
     */
    FactoryModel baseModel() {
        return new FactoryModel(resources, operations, products);
    }
}
