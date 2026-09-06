package com.arcogine.factory.change;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelArtifactV1;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.model.ResourceDefinition;
import com.arcogine.governance.SemanticArtifact;
import com.arcogine.governance.change.SemanticChange;
import com.arcogine.governance.change.SemanticChangeKind;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Pure unit tests for the factory-domain semantic comparator, independent of Governance persistence. */
class FactoryModelSemanticComparatorTest {

    private final FactoryModelSemanticComparator comparator = new FactoryModelSemanticComparator();

    @Test
    void reorderingTopLevelResourcesIsAttributedAsEntityModifiedPerAdr0006() {
        // ADR-0006 ("Current list ordering remains semantic in v1") makes resources, operations,
        // and products order-significant in factory-model:v1 -- product order specifically can
        // affect deterministic demand generation. A pure top-level reorder must therefore surface
        // as a real, attributable semantic change, not be absorbed by ID-keyed comparison.
        FactoryModelVersion first = twoResourceModel(List.of(1, 2));
        FactoryModelVersion reordered = twoResourceModel(List.of(2, 1));

        List<SemanticChange> changes = comparator.compare(artifact(first), artifact(reordered));

        assertEquals(2, changes.size(), "both reordered resources must be attributed a position change");
        for (SemanticChange change : changes) {
            assertEquals(SemanticChangeKind.ENTITY_MODIFIED, change.kind());
            assertEquals("factory.resource", change.entity().entityType());
            assertTrue(
                    change.detail().contains("listPosition"),
                    "reorder must be explained via listPosition, not silently dropped");
        }
    }

    @Test
    void reorderingProductsIsAttributedAsEntityModified() {
        // Product order can affect deterministic demand generation (ADR-0006), so it must never be
        // treated as a no-op.
        OperationStepDefinition step =
                new OperationStepDefinition(1, "Step", Set.of(new MachineId(1)), 1);
        OperationDefinition operation = new OperationDefinition(100, "Routing", List.of(step));
        ResourceDefinition machine = new ResourceDefinition(new MachineId(1), "Mill", 1, 10.0, 1);
        ProductDefinition widget = new ProductDefinition(new ProductId(10), "Widget", operation.id());
        ProductDefinition gadget = new ProductDefinition(new ProductId(20), "Gadget", operation.id());

        FactoryModelVersion base =
                FactoryModelPublisher.publish(
                        new FactoryModel(List.of(machine), List.of(operation), List.of(widget, gadget)));
        FactoryModelVersion reordered =
                FactoryModelPublisher.publish(
                        new FactoryModel(List.of(machine), List.of(operation), List.of(gadget, widget)));

        List<SemanticChange> changes = comparator.compare(artifact(base), artifact(reordered));

        assertEquals(2, changes.size());
        for (SemanticChange change : changes) {
            assertEquals(SemanticChangeKind.ENTITY_MODIFIED, change.kind());
            assertEquals("factory.product", change.entity().entityType());
            assertTrue(change.detail().contains("listPosition"));
        }
    }

    @Test
    void identicalModelsWithIdenticalConstructionOrderProduceNoSemanticChanges() {
        FactoryModelVersion first = twoResourceModel(List.of(1, 2));
        FactoryModelVersion same = twoResourceModel(List.of(1, 2));

        List<SemanticChange> changes = comparator.compare(artifact(first), artifact(same));
        assertTrue(changes.isEmpty(), "identical models compared without reordering must not diff");
    }

    @Test
    void reorderingEligibleResourcesWithinAStepDoesNotProduceASemanticChange() {
        // eligibleResources is set-shaped and explicitly order-insignificant under ADR-0006
        // (canonicalized by ascending MachineId), unlike the top-level collections above.
        OperationStepDefinition baseStep =
                new OperationStepDefinition(1, "Step", Set.of(new MachineId(1), new MachineId(2)), 1);
        OperationStepDefinition candidateStep =
                new OperationStepDefinition(1, "Step", Set.of(new MachineId(2), new MachineId(1)), 1);
        OperationDefinition baseOperation = new OperationDefinition(100, "Routing", List.of(baseStep));
        OperationDefinition candidateOperation =
                new OperationDefinition(100, "Routing", List.of(candidateStep));
        ResourceDefinition m1 = new ResourceDefinition(new MachineId(1), "Mill", 1, 10.0, 1);
        ResourceDefinition m2 = new ResourceDefinition(new MachineId(2), "Lathe", 1, 10.0, 1);
        ProductDefinition product =
                new ProductDefinition(new ProductId(10), "Widget", baseOperation.id());

        FactoryModelVersion base =
                FactoryModelPublisher.publish(
                        new FactoryModel(List.of(m1, m2), List.of(baseOperation), List.of(product)));
        FactoryModelVersion candidate =
                FactoryModelPublisher.publish(
                        new FactoryModel(List.of(m1, m2), List.of(candidateOperation), List.of(product)));

        List<SemanticChange> changes = comparator.compare(artifact(base), artifact(candidate));
        assertTrue(changes.isEmpty(), "eligibleResources is set-shaped and order-insignificant");
    }

    @Test
    void addedResourceIsClassifiedAsEntityAddedWithStableIdentity() {
        FactoryModelVersion base = twoResourceModel(List.of(1));
        FactoryModelVersion candidate = twoResourceModel(List.of(1, 2));

        List<SemanticChange> changes = comparator.compare(artifact(base), artifact(candidate));

        assertEquals(1, changes.size());
        SemanticChange change = changes.get(0);
        assertEquals(SemanticChangeKind.ENTITY_ADDED, change.kind());
        assertEquals("factory.resource", change.entity().entityType());
        assertEquals("2", change.entity().entityId());
    }

    @Test
    void removedResourceIsClassifiedAsEntityRemoved() {
        FactoryModelVersion base = twoResourceModel(List.of(1, 2));
        FactoryModelVersion candidate = twoResourceModel(List.of(1));

        List<SemanticChange> changes = comparator.compare(artifact(base), artifact(candidate));

        assertEquals(1, changes.size());
        assertEquals(SemanticChangeKind.ENTITY_REMOVED, changes.get(0).kind());
        assertEquals("2", changes.get(0).entity().entityId());
    }

    @Test
    void modifiedResourceCapacityIsClassifiedAsEntityModifiedByStableId() {
        ResourceDefinition machine = new ResourceDefinition(new MachineId(1), "Mill", 1, 100.0, 2);
        FactoryModelVersion base = model(List.of(machine));
        ResourceDefinition changedMachine =
                new ResourceDefinition(new MachineId(1), "Mill", 1, 150.0, 2);
        FactoryModelVersion candidate = model(List.of(changedMachine));

        List<SemanticChange> changes = comparator.compare(artifact(base), artifact(candidate));

        assertEquals(1, changes.size());
        SemanticChange change = changes.get(0);
        assertEquals(SemanticChangeKind.ENTITY_MODIFIED, change.kind());
        assertEquals("1", change.entity().entityId());
        assertTrue(change.detail().contains("100.0"));
        assertTrue(change.detail().contains("150.0"));
    }

    @Test
    void renamingAnEntityDoesNotChangeWhichEntityIsAffected() {
        // Reordering/relabeling must not be misread as add+remove of a different entity: same
        // stable MachineId, different display name, is one ENTITY_MODIFIED against that id.
        ResourceDefinition machine = new ResourceDefinition(new MachineId(1), "Mill A", 1, 10.0, 2);
        FactoryModelVersion base = model(List.of(machine));
        ResourceDefinition renamed = new ResourceDefinition(new MachineId(1), "Mill B", 1, 10.0, 2);
        FactoryModelVersion candidate = model(List.of(renamed));

        List<SemanticChange> changes = comparator.compare(artifact(base), artifact(candidate));

        assertEquals(1, changes.size());
        assertEquals(SemanticChangeKind.ENTITY_MODIFIED, changes.get(0).kind());
        assertEquals("1", changes.get(0).entity().entityId());
    }

    @Test
    void supportsDelegatesToFactoryModelArtifactV1() {
        FactoryModelVersion version = twoResourceModel(List.of(1));
        assertTrue(comparator.supports(version.fingerprint()));
    }

    @Test
    void addedOperationIsClassifiedAsEntityAdded() {
        OperationStepDefinition step =
                new OperationStepDefinition(1, "Step", Set.of(new MachineId(1)), 1);
        OperationDefinition op1 = new OperationDefinition(100, "Routing", List.of(step));
        OperationDefinition op2 = new OperationDefinition(200, "Packing", List.of(step));
        ResourceDefinition machine = new ResourceDefinition(new MachineId(1), "Mill", 1, 10.0, 1);
        ProductDefinition product = new ProductDefinition(new ProductId(10), "Widget", op1.id());

        FactoryModelVersion base =
                FactoryModelPublisher.publish(
                        new FactoryModel(List.of(machine), List.of(op1), List.of(product)));
        FactoryModelVersion candidate =
                FactoryModelPublisher.publish(
                        new FactoryModel(List.of(machine), List.of(op1, op2), List.of(product)));

        List<SemanticChange> changes = comparator.compare(artifact(base), artifact(candidate));

        assertEquals(1, changes.size());
        assertEquals(SemanticChangeKind.ENTITY_ADDED, changes.get(0).kind());
        assertEquals("factory.operation", changes.get(0).entity().entityType());
        assertEquals("200", changes.get(0).entity().entityId());
    }

    @Test
    void removedOperationIsClassifiedAsEntityRemoved() {
        OperationStepDefinition step =
                new OperationStepDefinition(1, "Step", Set.of(new MachineId(1)), 1);
        OperationDefinition op1 = new OperationDefinition(100, "Routing", List.of(step));
        OperationDefinition op2 = new OperationDefinition(200, "Packing", List.of(step));
        ResourceDefinition machine = new ResourceDefinition(new MachineId(1), "Mill", 1, 10.0, 1);
        ProductDefinition product = new ProductDefinition(new ProductId(10), "Widget", op1.id());

        FactoryModelVersion base =
                FactoryModelPublisher.publish(
                        new FactoryModel(List.of(machine), List.of(op1, op2), List.of(product)));
        FactoryModelVersion candidate =
                FactoryModelPublisher.publish(
                        new FactoryModel(List.of(machine), List.of(op1), List.of(product)));

        List<SemanticChange> changes = comparator.compare(artifact(base), artifact(candidate));

        assertEquals(1, changes.size());
        assertEquals(SemanticChangeKind.ENTITY_REMOVED, changes.get(0).kind());
        assertEquals("200", changes.get(0).entity().entityId());
    }

    @Test
    void operationNameChangeAndStepIdChangeAreDetected() {
        OperationStepDefinition step1 = new OperationStepDefinition(1, "Step", Set.of(new MachineId(1)), 1);
        OperationStepDefinition step2 = new OperationStepDefinition(2, "Step2", Set.of(new MachineId(1)), 1);
        OperationDefinition baseOp = new OperationDefinition(100, "Routing", List.of(step1));
        OperationDefinition renamedOp = new OperationDefinition(100, "Routing2", List.of(step2));
        ResourceDefinition machine = new ResourceDefinition(new MachineId(1), "Mill", 1, 10.0, 1);
        ProductDefinition product = new ProductDefinition(new ProductId(10), "Widget", baseOp.id());

        FactoryModelVersion base =
                FactoryModelPublisher.publish(
                        new FactoryModel(List.of(machine), List.of(baseOp), List.of(product)));
        FactoryModelVersion candidate =
                FactoryModelPublisher.publish(
                        new FactoryModel(List.of(machine), List.of(renamedOp), List.of(product)));

        List<SemanticChange> changes = comparator.compare(artifact(base), artifact(candidate));

        assertEquals(1, changes.size());
        SemanticChange change = changes.get(0);
        assertEquals(SemanticChangeKind.ENTITY_MODIFIED, change.kind());
        assertTrue(change.detail().contains("name"));
        assertTrue(change.detail().contains("stepIds"));
    }

    @Test
    void operationStepRoutingChangeWithSameStepIdsIsDetected() {
        OperationStepDefinition beforeStep =
                new OperationStepDefinition(1, "Step", Set.of(new MachineId(1)), 1);
        OperationStepDefinition afterStep =
                new OperationStepDefinition(1, "Step", Set.of(new MachineId(1)), 5);
        OperationDefinition baseOp = new OperationDefinition(100, "Routing", List.of(beforeStep));
        OperationDefinition candidateOp = new OperationDefinition(100, "Routing", List.of(afterStep));
        ResourceDefinition machine = new ResourceDefinition(new MachineId(1), "Mill", 1, 10.0, 1);
        ProductDefinition product = new ProductDefinition(new ProductId(10), "Widget", baseOp.id());

        FactoryModelVersion base =
                FactoryModelPublisher.publish(
                        new FactoryModel(List.of(machine), List.of(baseOp), List.of(product)));
        FactoryModelVersion candidate =
                FactoryModelPublisher.publish(
                        new FactoryModel(List.of(machine), List.of(candidateOp), List.of(product)));

        List<SemanticChange> changes = comparator.compare(artifact(base), artifact(candidate));

        assertEquals(1, changes.size());
        assertTrue(changes.get(0).detail().contains("step routing/duration changed"));
    }

    @Test
    void addedProductIsClassifiedAsEntityAdded() {
        OperationStepDefinition step =
                new OperationStepDefinition(1, "Step", Set.of(new MachineId(1)), 1);
        OperationDefinition operation = new OperationDefinition(100, "Routing", List.of(step));
        ResourceDefinition machine = new ResourceDefinition(new MachineId(1), "Mill", 1, 10.0, 1);
        ProductDefinition product1 = new ProductDefinition(new ProductId(10), "Widget", operation.id());
        ProductDefinition product2 = new ProductDefinition(new ProductId(20), "Gadget", operation.id());

        FactoryModelVersion base =
                FactoryModelPublisher.publish(
                        new FactoryModel(List.of(machine), List.of(operation), List.of(product1)));
        FactoryModelVersion candidate =
                FactoryModelPublisher.publish(
                        new FactoryModel(
                                List.of(machine), List.of(operation), List.of(product1, product2)));

        List<SemanticChange> changes = comparator.compare(artifact(base), artifact(candidate));

        assertEquals(1, changes.size());
        assertEquals(SemanticChangeKind.ENTITY_ADDED, changes.get(0).kind());
        assertEquals("factory.product", changes.get(0).entity().entityType());
        assertEquals("20", changes.get(0).entity().entityId());
    }

    @Test
    void removedProductIsClassifiedAsEntityRemoved() {
        OperationStepDefinition step =
                new OperationStepDefinition(1, "Step", Set.of(new MachineId(1)), 1);
        OperationDefinition operation = new OperationDefinition(100, "Routing", List.of(step));
        ResourceDefinition machine = new ResourceDefinition(new MachineId(1), "Mill", 1, 10.0, 1);
        ProductDefinition product1 = new ProductDefinition(new ProductId(10), "Widget", operation.id());
        ProductDefinition product2 = new ProductDefinition(new ProductId(20), "Gadget", operation.id());

        FactoryModelVersion base =
                FactoryModelPublisher.publish(
                        new FactoryModel(
                                List.of(machine), List.of(operation), List.of(product1, product2)));
        FactoryModelVersion candidate =
                FactoryModelPublisher.publish(
                        new FactoryModel(List.of(machine), List.of(operation), List.of(product1)));

        List<SemanticChange> changes = comparator.compare(artifact(base), artifact(candidate));

        assertEquals(1, changes.size());
        assertEquals(SemanticChangeKind.ENTITY_REMOVED, changes.get(0).kind());
        assertEquals("20", changes.get(0).entity().entityId());
    }

    @Test
    void productNameAndOperationIdChangeIsDetected() {
        OperationStepDefinition step =
                new OperationStepDefinition(1, "Step", Set.of(new MachineId(1)), 1);
        OperationDefinition op1 = new OperationDefinition(100, "Routing", List.of(step));
        OperationDefinition op2 = new OperationDefinition(200, "Packing", List.of(step));
        ResourceDefinition machine = new ResourceDefinition(new MachineId(1), "Mill", 1, 10.0, 1);
        ProductDefinition baseProduct = new ProductDefinition(new ProductId(10), "Widget", op1.id());
        ProductDefinition changedProduct = new ProductDefinition(new ProductId(10), "Widget2", op2.id());

        FactoryModelVersion base =
                FactoryModelPublisher.publish(
                        new FactoryModel(List.of(machine), List.of(op1, op2), List.of(baseProduct)));
        FactoryModelVersion candidate =
                FactoryModelPublisher.publish(
                        new FactoryModel(List.of(machine), List.of(op1, op2), List.of(changedProduct)));

        List<SemanticChange> changes = comparator.compare(artifact(base), artifact(candidate));

        assertEquals(1, changes.size());
        SemanticChange change = changes.get(0);
        assertEquals(SemanticChangeKind.ENTITY_MODIFIED, change.kind());
        assertTrue(change.detail().contains("name"));
        assertTrue(change.detail().contains("operationId"));
    }

    @Test
    void resourceConcurrencyAndSetupTimeChangesAreDetected() {
        ResourceDefinition before = new ResourceDefinition(new MachineId(1), "Mill", 1, 10.0, 2);
        ResourceDefinition after = new ResourceDefinition(new MachineId(1), "Mill", 3, 10.0, 9);
        FactoryModelVersion base = model(List.of(before));
        FactoryModelVersion candidate = model(List.of(after));

        List<SemanticChange> changes = comparator.compare(artifact(base), artifact(candidate));

        assertEquals(1, changes.size());
        SemanticChange change = changes.get(0);
        assertTrue(change.detail().contains("concurrency"));
        assertTrue(change.detail().contains("setupTime"));
    }

    private static FactoryModelVersion twoResourceModel(List<Integer> resourceIds) {
        List<ResourceDefinition> resources =
                resourceIds.stream()
                        .map(id -> new ResourceDefinition(new MachineId(id), "Machine " + id, 1, 10.0, 1))
                        .toList();
        return model(resources);
    }

    private static FactoryModelVersion model(List<ResourceDefinition> resources) {
        // Eligibility is pinned to machine 1 (present in every fixture variant below) so that
        // adding/removing/reordering *other* resources does not also perturb the operation's own
        // semantics -- keeping each test's expected change set attributable to one entity kind.
        OperationStepDefinition step =
                new OperationStepDefinition(1, "Step", Set.of(new MachineId(1)), 1);
        OperationDefinition operation = new OperationDefinition(100, "Routing", List.of(step));
        ProductDefinition product = new ProductDefinition(new ProductId(10), "Widget", operation.id());
        return FactoryModelPublisher.publish(
                new FactoryModel(resources, List.of(operation), List.of(product)));
    }

    private static SemanticArtifact artifact(FactoryModelVersion version) {
        return new SemanticArtifact(version.fingerprint(), FactoryModelArtifactV1.encode(version));
    }
}
