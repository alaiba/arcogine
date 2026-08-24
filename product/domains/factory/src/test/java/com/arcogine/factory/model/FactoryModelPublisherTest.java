package com.arcogine.factory.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arcogine.factory.model.validation.FactoryModelValidationException;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FactoryModelPublisherTest {

    private static FactoryModel validModel() {
        ResourceDefinition mill = new ResourceDefinition(new MachineId(1), "Mill", 1, null, 0);
        OperationDefinition routing = new OperationDefinition(
                100,
                "Widget routing",
                List.of(new OperationStepDefinition(1, "Rough milling", Set.of(new MachineId(1)), 5)));
        ProductDefinition widget = new ProductDefinition(new ProductId(10), "Widget", 100);
        return new FactoryModel(List.of(mill), List.of(routing), List.of(widget));
    }

    @Test
    void publishRejectsInvalidModel() {
        FactoryModel invalid = new FactoryModel(
                List.of(new ResourceDefinition(new MachineId(1), "Mill", 1, null, 0)),
                List.of(new OperationDefinition(100, "Empty", List.of())),
                List.of());

        assertThrows(FactoryModelValidationException.class, () -> FactoryModelPublisher.publish(invalid));
    }

    @Test
    void equalModelsProduceTheSameContentHash() {
        FactoryModelVersion a = FactoryModelPublisher.publish(validModel());
        FactoryModelVersion b = FactoryModelPublisher.publish(validModel());

        assertEquals(a.contentHash(), b.contentHash());
        assertEquals(a.model(), b.model());
    }

    @Test
    void differentModelsProduceDifferentContentHashes() {
        FactoryModelVersion a = FactoryModelPublisher.publish(validModel());

        ResourceDefinition otherMill = new ResourceDefinition(new MachineId(1), "Other Mill", 1, null, 0);
        OperationDefinition routing = new OperationDefinition(
                100,
                "Widget routing",
                List.of(new OperationStepDefinition(1, "Rough milling", Set.of(new MachineId(1)), 5)));
        ProductDefinition widget = new ProductDefinition(new ProductId(10), "Widget", 100);
        FactoryModelVersion b = FactoryModelPublisher.publish(
                new FactoryModel(List.of(otherMill), List.of(routing), List.of(widget)));

        assertNotEquals(a.contentHash(), b.contentHash());
    }

    @Test
    void publishedModelCarriesTheOriginalModelUnmodified() {
        FactoryModel model = validModel();
        FactoryModelVersion version = FactoryModelPublisher.publish(model);

        assertEquals(model, version.model());
    }
}
