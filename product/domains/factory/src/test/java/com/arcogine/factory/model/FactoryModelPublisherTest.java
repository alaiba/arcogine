package com.arcogine.factory.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arcogine.factory.model.validation.FactoryModelValidationException;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FactoryModelPublisherTest {

    private static FactoryModel validModel() {
        ConfiguredResource mill = new ConfiguredResource(new MachineId(1), "Mill", 1, null, 0);
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
                List.of(new ConfiguredResource(new MachineId(1), "Mill", 1, null, 0)),
                List.of(new OperationDefinition(100, "Empty", List.of())),
                List.of());

        assertThrows(FactoryModelValidationException.class, () -> FactoryModelPublisher.publish(invalid));
    }

    @Test
    void publishedModelCarriesTheOriginalModelUnmodified() {
        FactoryModel model = validModel();
        FactoryModelVersion version = FactoryModelPublisher.publish(model);

        assertEquals(model, version.model());
    }
}
