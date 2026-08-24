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

    @Test
    void namesContainingDelimiterCharactersDoNotCauseHashCollisions() {
        // Regression for a naive ':'/';'-delimited canonical representation: a two-product model
        // and a one-product model whose single product name happens to contain those delimiter
        // characters could otherwise serialize to the identical string and hash identically, even
        // though they are different designs (different product counts, different operation
        // references). The canonical representation must use unambiguous length-framing instead.
        ResourceDefinition mill = new ResourceDefinition(new MachineId(1), "Mill", 1, null, 0);
        OperationDefinition op10 = new OperationDefinition(
                10, "Op10", List.of(new OperationStepDefinition(1, "Step", Set.of(new MachineId(1)), 5)));
        OperationDefinition op20 = new OperationDefinition(
                20, "Op20", List.of(new OperationStepDefinition(2, "Step", Set.of(new MachineId(1)), 5)));

        FactoryModel twoProducts = new FactoryModel(
                List.of(mill),
                List.of(op10, op20),
                List.of(
                        new ProductDefinition(new ProductId(1), "A", 10),
                        new ProductDefinition(new ProductId(2), "B", 20)));
        FactoryModel oneProductWithDelimiterLadenName = new FactoryModel(
                List.of(mill),
                List.of(op10, op20),
                List.of(new ProductDefinition(new ProductId(1), "A:10;2:B", 20)));

        FactoryModelVersion a = FactoryModelPublisher.publish(twoProducts);
        FactoryModelVersion b = FactoryModelPublisher.publish(oneProductWithDelimiterLadenName);

        assertNotEquals(a.contentHash(), b.contentHash());
    }
}
