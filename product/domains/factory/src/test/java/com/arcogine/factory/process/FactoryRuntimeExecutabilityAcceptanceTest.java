package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.model.FactoryRuntimeAssembler;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.model.spatial.FactoryFloor;
import com.arcogine.factory.model.spatial.ResourceFootprint;
import com.arcogine.factory.model.spatial.ResourceLayout;
import com.arcogine.factory.model.spatial.ResourcePlacement;
import com.arcogine.factory.model.spatial.SpatialRecord;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Acceptance evidence for the boundary between Factory validity and current Engine executability. */
class FactoryRuntimeExecutabilityAcceptanceTest {

    private static FactoryModel productionRecords(Optional<SpatialRecord> spatial) {
        return new FactoryModel(
                List.of(
                        new ConfiguredResource(new MachineId(1), "Mill", 1, null, 0),
                        new ConfiguredResource(new MachineId(2), "Packer", 1, null, 0)),
                List.of(new OperationDefinition(
                        1,
                        "Widget Route",
                        List.of(
                                new OperationStepDefinition(1, "Milling", Set.of(new MachineId(1)), 5),
                                new OperationStepDefinition(2, "Packing", Set.of(new MachineId(2)), 3)))),
                List.of(new ProductDefinition(new ProductId(1), "Widget", 1)),
                spatial);
    }

    private static FactoryModelVersion model() {
        return FactoryModelPublisher.publish(productionRecords(Optional.empty()));
    }

    private static SpatialRecord spatial(long ticksPerCell, long handlingTicks) {
        return new SpatialRecord(
                new FactoryFloor(4, 1),
                ticksPerCell,
                handlingTicks,
                List.of(
                        new ResourceLayout(new MachineId(1), new ResourcePlacement(0, 0), new ResourceFootprint(1, 1)),
                        new ResourceLayout(new MachineId(2), new ResourcePlacement(3, 0), new ResourceFootprint(1, 1))));
    }

    @Test
    void runtimeDoesNotExposeAPlaceholderEngineSemanticsIdentifier() {
        assertTrue(Arrays.stream(FactoryRuntime.class.getMethods())
                .noneMatch(method -> method.getName().equals("engineSemantics")));
        assertTrue(Arrays.stream(FactoryRuntime.class.getMethods())
                .noneMatch(method -> method.getName().equals("setEngineSemantics")));
    }

    @Test
    void validSpatialContentIsRefusedBeforeAnyRuntimeStateExists() {
        // Present legal zero and positive magnitudes are both valid published designs, but the
        // current Engine executes production records only. Neither is run as though its spatial
        // record were absent, and no runtime is assembled.
        for (SpatialRecord spatial : List.of(spatial(0, 0), spatial(2, 1))) {
            FactoryModelVersion published = FactoryModelPublisher.publish(productionRecords(Optional.of(spatial)));

            UnsupportedModelContentException refusal = assertThrows(
                    UnsupportedModelContentException.class, () -> FactoryRuntime.forModel(published));
            assertEquals("spatial", refusal.content());
            assertThrows(
                    UnsupportedModelContentException.class, () -> FactoryRuntimeAssembler.assemble(published));
        }
    }

    @Test
    void productionOnlyContentStillExecutesWithoutTransferBehavior() {
        FactoryRuntime runtime = FactoryRuntime.forModel(model());
        assertTrue(runtime.submitWorkload(new ProductId(1), 1, 10.0) instanceof CommandResult.Accepted<?>);

        runtime.advanceUntil(com.arcogine.types.SimTime.of(1_000), 1_000);

        assertEquals(1, runtime.observe().performance().completedOrders());
        assertEquals(8, runtime.observe().metadata().currentTime().value());
    }
}
