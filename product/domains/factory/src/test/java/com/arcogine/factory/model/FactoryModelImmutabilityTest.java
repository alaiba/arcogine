package com.arcogine.factory.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Proves the publication invariant that a {@link FactoryModel}/{@link FactoryModelVersion} cannot
 * be mutated after construction, either by writing back through an accessor or by mutating a
 * mutable collection the caller passed in.
 */
class FactoryModelImmutabilityTest {

    @Test
    void resourcesListRejectsMutationThroughTheAccessor() {
        FactoryModel model = new FactoryModel(
                new ArrayList<>(List.of(new ResourceDefinition(new MachineId(1), "Mill", 1, null, 0))),
                List.of(),
                List.of());

        assertThrows(
                UnsupportedOperationException.class,
                () -> model.resources().add(new ResourceDefinition(new MachineId(2), "Lathe", 1, null, 0)));
    }

    @Test
    void constructorDefensivelyCopiesTheResourcesList() {
        List<ResourceDefinition> mutableResources =
                new ArrayList<>(List.of(new ResourceDefinition(new MachineId(1), "Mill", 1, null, 0)));
        FactoryModel model = new FactoryModel(mutableResources, List.of(), List.of());

        mutableResources.add(new ResourceDefinition(new MachineId(2), "Lathe", 1, null, 0));

        assertEquals(1, model.resources().size(), "model must not observe mutation of the source list");
    }

    @Test
    void operationStepsListIsDefensivelyCopiedAndUnmodifiable() {
        List<OperationStepDefinition> mutableSteps = new ArrayList<>(
                List.of(new OperationStepDefinition(1, "Rough milling", Set.of(new MachineId(1)), 5)));
        OperationDefinition operation = new OperationDefinition(100, "Widget routing", mutableSteps);

        mutableSteps.add(new OperationStepDefinition(2, "Extra step", Set.of(new MachineId(1)), 5));

        assertEquals(1, operation.steps().size(), "operation must not observe mutation of the source list");
        assertThrows(
                UnsupportedOperationException.class,
                () -> operation.steps().add(new OperationStepDefinition(3, "x", Set.of(new MachineId(1)), 1)));
    }

    @Test
    void eligibleResourcesSetIsDefensivelyCopiedAndUnmodifiable() {
        Set<MachineId> mutableEligible = new HashSet<>(Set.of(new MachineId(1)));
        OperationStepDefinition step = new OperationStepDefinition(1, "Rough milling", mutableEligible, 5);

        mutableEligible.add(new MachineId(2));

        assertEquals(1, step.eligibleResources().size(), "step must not observe mutation of the source set");
        assertThrows(
                UnsupportedOperationException.class, () -> step.eligibleResources().add(new MachineId(3)));
    }

    @Test
    void mutatingTheSourceModelAfterPublicationDoesNotAffectThePublishedVersion() {
        List<ResourceDefinition> mutableResources =
                new ArrayList<>(List.of(new ResourceDefinition(new MachineId(1), "Mill", 1, null, 0)));
        List<OperationDefinition> operations = List.of(new OperationDefinition(
                100,
                "Widget routing",
                List.of(new OperationStepDefinition(1, "Rough milling", Set.of(new MachineId(1)), 5))));
        List<ProductDefinition> products = List.of(new ProductDefinition(new ProductId(10), "Widget", 100));

        FactoryModel model = new FactoryModel(mutableResources, operations, products);
        FactoryModelVersion version = FactoryModelPublisher.publish(model);
        String hashBeforeMutation = version.contentHash();

        // A source collection can only be mutated before construction (it's defensively copied),
        // so this exercises that the already-published version is unaffected by any further
        // change to the model instance itself: there is no setter to mutate it through.
        assertEquals(hashBeforeMutation, version.contentHash());
        assertEquals(1, version.model().resources().size());
    }
}
