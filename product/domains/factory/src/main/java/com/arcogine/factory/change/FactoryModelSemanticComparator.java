package com.arcogine.factory.change;

import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelArtifactV1;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.model.ResourceDefinition;
import com.arcogine.governance.SemanticArtifact;
import com.arcogine.governance.change.ChangedEntityRef;
import com.arcogine.governance.change.SemanticChange;
import com.arcogine.governance.change.SemanticChangeExtractor;
import com.arcogine.governance.change.SemanticChangeKind;
import com.arcogine.types.ModelFingerprint;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The G2 D5 seam: domain-owned semantic comparison for {@code factory-model:v1} artifacts.
 *
 * <p>This is the only place that knows how to interpret {@link FactoryModel} internals for
 * change-attribution purposes. It never depends on Governance's {@code ChangeSet} orchestration
 * (only on the narrow {@link SemanticChangeExtractor} SPI it implements) and Governance never
 * introspects {@link FactoryModel} directly -- keeping factory semantics owned by {@code :factory}
 * and generic transition/impact semantics owned by {@code :governance}.
 *
 * <p>Comparison is by stable domain identity ({@code MachineId}, operation id, {@code ProductId}),
 * never by list position, so reordering a semantically unchanged model produces zero changes.
 */
public final class FactoryModelSemanticComparator implements SemanticChangeExtractor {

    private static final String RESOURCE_TYPE = "factory.resource";
    private static final String OPERATION_TYPE = "factory.operation";
    private static final String PRODUCT_TYPE = "factory.product";

    @Override
    public boolean supports(ModelFingerprint fingerprint) {
        return FactoryModelArtifactV1.supports(fingerprint);
    }

    @Override
    public List<SemanticChange> compare(SemanticArtifact base, SemanticArtifact candidate) {
        FactoryModel baseModel = decode(base);
        FactoryModel candidateModel = decode(candidate);

        List<SemanticChange> changes = new ArrayList<>();
        compareResources(baseModel, candidateModel, changes);
        compareOperations(baseModel, candidateModel, changes);
        compareProducts(baseModel, candidateModel, changes);
        return changes;
    }

    private static FactoryModel decode(SemanticArtifact artifact) {
        FactoryModelVersion version = FactoryModelArtifactV1.decode(artifact.canonicalBytes());
        return version.model();
    }

    private static void compareResources(
            FactoryModel base, FactoryModel candidate, List<SemanticChange> changes) {
        Map<String, ResourceDefinition> baseById =
                indexBy(base.resources(), r -> Long.toString(r.id().value()));
        Map<String, ResourceDefinition> candidateById =
                indexBy(candidate.resources(), r -> Long.toString(r.id().value()));

        for (Map.Entry<String, ResourceDefinition> entry : baseById.entrySet()) {
            String id = entry.getKey();
            ResourceDefinition baseResource = entry.getValue();
            ResourceDefinition candidateResource = candidateById.get(id);
            ChangedEntityRef ref = new ChangedEntityRef(RESOURCE_TYPE, id, baseResource.name());
            if (candidateResource == null) {
                changes.add(
                        new SemanticChange(
                                SemanticChangeKind.ENTITY_REMOVED, ref, "resource removed: " + baseResource.name()));
            } else if (!baseResource.equals(candidateResource)) {
                changes.add(
                        new SemanticChange(
                                SemanticChangeKind.ENTITY_MODIFIED,
                                new ChangedEntityRef(RESOURCE_TYPE, id, candidateResource.name()),
                                describeResourceChange(baseResource, candidateResource)));
            }
        }
        for (Map.Entry<String, ResourceDefinition> entry : candidateById.entrySet()) {
            if (!baseById.containsKey(entry.getKey())) {
                ResourceDefinition added = entry.getValue();
                changes.add(
                        new SemanticChange(
                                SemanticChangeKind.ENTITY_ADDED,
                                new ChangedEntityRef(RESOURCE_TYPE, entry.getKey(), added.name()),
                                "resource added: " + added.name()));
            }
        }
    }

    private static String describeResourceChange(ResourceDefinition before, ResourceDefinition after) {
        StringBuilder detail = new StringBuilder();
        if (!before.name().equals(after.name())) {
            appendField(detail, "name", before.name(), after.name());
        }
        if (before.concurrency() != after.concurrency()) {
            appendField(detail, "concurrency", before.concurrency(), after.concurrency());
        }
        if (!java.util.Objects.equals(before.capacityLiters(), after.capacityLiters())) {
            appendField(detail, "capacityLiters", before.capacityLiters(), after.capacityLiters());
        }
        if (before.setupTime() != after.setupTime()) {
            appendField(detail, "setupTime", before.setupTime(), after.setupTime());
        }
        return detail.toString();
    }

    private static void compareOperations(
            FactoryModel base, FactoryModel candidate, List<SemanticChange> changes) {
        Map<String, OperationDefinition> baseById = indexBy(base.operations(), o -> Long.toString(o.id()));
        Map<String, OperationDefinition> candidateById =
                indexBy(candidate.operations(), o -> Long.toString(o.id()));

        for (Map.Entry<String, OperationDefinition> entry : baseById.entrySet()) {
            String id = entry.getKey();
            OperationDefinition baseOperation = entry.getValue();
            OperationDefinition candidateOperation = candidateById.get(id);
            if (candidateOperation == null) {
                changes.add(
                        new SemanticChange(
                                SemanticChangeKind.ENTITY_REMOVED,
                                new ChangedEntityRef(OPERATION_TYPE, id, baseOperation.name()),
                                "operation removed: " + baseOperation.name()));
            } else if (!operationsEqual(baseOperation, candidateOperation)) {
                changes.add(
                        new SemanticChange(
                                SemanticChangeKind.ENTITY_MODIFIED,
                                new ChangedEntityRef(OPERATION_TYPE, id, candidateOperation.name()),
                                describeOperationChange(baseOperation, candidateOperation)));
            }
        }
        for (Map.Entry<String, OperationDefinition> entry : candidateById.entrySet()) {
            if (!baseById.containsKey(entry.getKey())) {
                OperationDefinition added = entry.getValue();
                changes.add(
                        new SemanticChange(
                                SemanticChangeKind.ENTITY_ADDED,
                                new ChangedEntityRef(OPERATION_TYPE, entry.getKey(), added.name()),
                                "operation added: " + added.name()));
            }
        }
    }

    private static boolean operationsEqual(OperationDefinition a, OperationDefinition b) {
        return a.name().equals(b.name()) && a.steps().equals(b.steps());
    }

    private static String describeOperationChange(OperationDefinition before, OperationDefinition after) {
        StringBuilder detail = new StringBuilder();
        if (!before.name().equals(after.name())) {
            appendField(detail, "name", before.name(), after.name());
        }
        Set<Long> beforeSteps = stepIds(before);
        Set<Long> afterSteps = stepIds(after);
        if (!beforeSteps.equals(afterSteps)) {
            appendField(detail, "stepIds", beforeSteps, afterSteps);
        } else if (!before.steps().equals(after.steps())) {
            detail.append(detail.isEmpty() ? "" : "; ").append("step routing/duration changed");
        }
        return detail.toString();
    }

    private static Set<Long> stepIds(OperationDefinition operation) {
        Set<Long> ids = new TreeSet<>();
        for (OperationStepDefinition step : operation.steps()) {
            ids.add(step.stepId());
        }
        return ids;
    }

    private static void compareProducts(
            FactoryModel base, FactoryModel candidate, List<SemanticChange> changes) {
        Map<String, ProductDefinition> baseById =
                indexBy(base.products(), p -> Long.toString(p.id().value()));
        Map<String, ProductDefinition> candidateById =
                indexBy(candidate.products(), p -> Long.toString(p.id().value()));

        for (Map.Entry<String, ProductDefinition> entry : baseById.entrySet()) {
            String id = entry.getKey();
            ProductDefinition baseProduct = entry.getValue();
            ProductDefinition candidateProduct = candidateById.get(id);
            if (candidateProduct == null) {
                changes.add(
                        new SemanticChange(
                                SemanticChangeKind.ENTITY_REMOVED,
                                new ChangedEntityRef(PRODUCT_TYPE, id, baseProduct.name()),
                                "product removed: " + baseProduct.name()));
            } else if (!baseProduct.equals(candidateProduct)) {
                changes.add(
                        new SemanticChange(
                                SemanticChangeKind.ENTITY_MODIFIED,
                                new ChangedEntityRef(PRODUCT_TYPE, id, candidateProduct.name()),
                                describeProductChange(baseProduct, candidateProduct)));
            }
        }
        for (Map.Entry<String, ProductDefinition> entry : candidateById.entrySet()) {
            if (!baseById.containsKey(entry.getKey())) {
                ProductDefinition added = entry.getValue();
                changes.add(
                        new SemanticChange(
                                SemanticChangeKind.ENTITY_ADDED,
                                new ChangedEntityRef(PRODUCT_TYPE, entry.getKey(), added.name()),
                                "product added: " + added.name()));
            }
        }
    }

    private static String describeProductChange(ProductDefinition before, ProductDefinition after) {
        StringBuilder detail = new StringBuilder();
        if (!before.name().equals(after.name())) {
            appendField(detail, "name", before.name(), after.name());
        }
        if (before.operationId() != after.operationId()) {
            appendField(detail, "operationId", before.operationId(), after.operationId());
        }
        return detail.toString();
    }

    private static void appendField(StringBuilder detail, String field, Object before, Object after) {
        if (!detail.isEmpty()) {
            detail.append("; ");
        }
        detail.append(field).append(": ").append(before).append(" -> ").append(after);
    }

    private static <T> Map<String, T> indexBy(
            List<T> items, java.util.function.Function<T, String> idExtractor) {
        Map<String, T> byId = new LinkedHashMap<>();
        for (T item : items) {
            byId.put(idExtractor.apply(item), item);
        }
        return byId;
    }
}
