package com.arcogine.factory.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arcogine.factory.model.validation.FactoryModelValidationException;
import com.arcogine.factory.process.FactoryHandler;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FactoryRuntimeAssemblerTest {

    @Test
    void assemblesFactoryHandlerMatchingThePublishedModel() {
        ResourceDefinition mill = new ResourceDefinition(new MachineId(1), "Mill", 2, 100.0, 3);
        OperationDefinition routing = new OperationDefinition(
                100,
                "Widget routing",
                List.of(new OperationStepDefinition(1, "Rough milling", Set.of(new MachineId(1)), 5)));
        ProductDefinition widget = new ProductDefinition(new ProductId(10), "Widget", 100);
        FactoryModelVersion version =
                FactoryModelPublisher.publish(new FactoryModel(List.of(mill), List.of(routing), List.of(widget)));

        FactoryRuntimeAssembler.Assembled assembled = FactoryRuntimeAssembler.assemble(version);

        FactoryHandler factory = assembled.factory();
        assertEquals(List.of(new ProductId(10)), assembled.productIds());
        assertEquals(
                2,
                factory.machinesView().stream()
                        .filter(m -> m.id().equals(new MachineId(1)))
                        .findFirst()
                        .orElseThrow()
                        .concurrency());
    }

    @Test
    void cannotConstructAFactoryModelVersionWrappingAnInvalidModelEvenBypassingThePublisher() {
        // FactoryModelVersion's own canonical constructor validates, not just
        // FactoryModelPublisher.publish -- so there is no construction path, direct or otherwise,
        // that lets assemble() ever see an invalid model.
        FactoryModel invalid = new FactoryModel(
                List.of(new ResourceDefinition(new MachineId(1), "Mill", 1, null, 0)),
                List.of(new OperationDefinition(100, "Empty", List.of())),
                List.of());

        assertThrows(FactoryModelValidationException.class, () -> new FactoryModelVersion(invalid));
    }
}
