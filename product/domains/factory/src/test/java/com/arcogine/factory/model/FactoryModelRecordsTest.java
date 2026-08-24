package com.arcogine.factory.model;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Covers the null-rejection and null-defaulting branches of the model value types' compact
 * constructors, which the higher-level adapter/validator/publisher tests never happen to exercise
 * because they always build well-formed values.
 */
class FactoryModelRecordsTest {

    @Test
    void resourceDefinitionRejectsNullId() {
        assertThrows(
                NullPointerException.class, () -> new ResourceDefinition(null, "Mill", 1, null, 0));
    }

    @Test
    void resourceDefinitionRejectsNullName() {
        assertThrows(
                NullPointerException.class,
                () -> new ResourceDefinition(new MachineId(1), null, 1, null, 0));
    }

    @Test
    void operationDefinitionRejectsNullName() {
        assertThrows(NullPointerException.class, () -> new OperationDefinition(1, null, List.of()));
    }

    @Test
    void operationDefinitionDefaultsNullStepsToEmpty() {
        OperationDefinition operation = new OperationDefinition(1, "Op", null);

        assertTrue(operation.steps().isEmpty());
    }

    @Test
    void operationStepDefinitionRejectsNullName() {
        assertThrows(
                NullPointerException.class, () -> new OperationStepDefinition(1, null, Set.of(), 5));
    }

    @Test
    void operationStepDefinitionDefaultsNullEligibleResourcesToEmpty() {
        OperationStepDefinition step = new OperationStepDefinition(1, "Step", null, 5);

        assertTrue(step.eligibleResources().isEmpty());
    }

    @Test
    void productDefinitionRejectsNullId() {
        assertThrows(NullPointerException.class, () -> new ProductDefinition(null, "Widget", 1));
    }

    @Test
    void productDefinitionRejectsNullName() {
        assertThrows(
                NullPointerException.class, () -> new ProductDefinition(new ProductId(1), null, 1));
    }

    @Test
    void factoryModelDefaultsNullCollectionsToEmpty() {
        FactoryModel model = new FactoryModel(null, null, null);

        assertTrue(model.resources().isEmpty());
        assertTrue(model.operations().isEmpty());
        assertTrue(model.products().isEmpty());
    }

    @Test
    void factoryModelVersionRejectsNullModel() {
        assertThrows(NullPointerException.class, () -> new FactoryModelVersion(null));
    }
}
