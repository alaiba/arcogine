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

/** Pure unit tests for the D5 factory-domain comparator, independent of Governance persistence. */
class FactoryModelSemanticComparatorTest {

    private final FactoryModelSemanticComparator comparator = new FactoryModelSemanticComparator();

    @Test
    void identicalModelsProduceNoSemanticChangesRegardlessOfConstructionOrder() {
        // The canonical fingerprint policy (factory-model:v1) is itself list-order sensitive today,
        // so reordered construction can still yield distinct fingerprints -- that is an existing,
        // separate fingerprint-policy decision, not something G2 changes. What G2's comparator must
        // guarantee is that stable-ID-keyed comparison does not manufacture spurious add/remove
        // pairs purely from list position: comparing the same entity set in a different order must
        // still resolve to zero changes.
        FactoryModelVersion first = twoResourceModel(List.of(1, 2));
        FactoryModelVersion reordered = twoResourceModel(List.of(2, 1));

        List<SemanticChange> changes = comparator.compare(artifact(first), artifact(reordered));
        assertTrue(changes.isEmpty(), "semantically equivalent construction order must not diff");
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
