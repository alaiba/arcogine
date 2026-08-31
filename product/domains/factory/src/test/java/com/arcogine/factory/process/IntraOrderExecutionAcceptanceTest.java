package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.model.ResourceDefinition;
import com.arcogine.types.MachineId;
import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** ADR-0010/W1 acceptance proof: one requirement, independently dispatchable unit children. */
class IntraOrderExecutionAcceptanceTest {
    private static FactoryRuntime runtime() {
        FactoryModel model = new FactoryModel(
                List.of(new ResourceDefinition(new MachineId(1), "Cutter A", 1, null, 0),
                        new ResourceDefinition(new MachineId(2), "Cutter B", 1, null, 0),
                        new ResourceDefinition(new MachineId(3), "Assembler", 1, null, 0),
                        new ResourceDefinition(new MachineId(4), "Inspector", 1, null, 0)),
                List.of(new OperationDefinition(1, "Route", List.of(
                        new OperationStepDefinition(1, "CUT", Set.of(new MachineId(1), new MachineId(2)), 5),
                        new OperationStepDefinition(2, "ASSEMBLE", Set.of(new MachineId(3)), 3),
                        new OperationStepDefinition(3, "INSPECT", Set.of(new MachineId(4)), 2)))),
                List.of(new ProductDefinition(new ProductId(1), "Widget", 1)));
        FactoryModelVersion version = FactoryModelPublisher.publish(model);
        return FactoryRuntime.forModel(version);
    }

    @Test
    void quantityTwentyCreatesDeterministicChildrenAndOneAggregateCompletion() throws Exception {
        FactoryRuntime runtime = runtime();
        OrderId orderId = runtime.submitWorkload(new ProductId(1), 20, 12.5).orElseThrow();

        assertEquals(1, runtime.ordersView().count());
        assertEquals(20, runtime.jobsView().count());
        assertEquals(List.of(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L),
                runtime.jobsView().map(job -> job.ordinalWithinOrder()).toList());
        assertTrue(runtime.jobsView().allMatch(job -> job.orderId().equals(orderId) && job.quantity() == 1 && job.totalSteps() == 3));
        assertEquals(Set.of(new MachineId(1), new MachineId(2)), runtime.jobsView().filter(job -> job.currentMachine() != null).map(job -> job.currentMachine()).collect(java.util.stream.Collectors.toSet()));

        List<Long> progress = new ArrayList<>();
        List<EventPayload.OrderCompleted> completions = new ArrayList<>();
        while (runtime.advance().isPresent()) {
            progress.add(runtime.orderExecution(orderId).completedQuantity());
        }
        // Drain produces no duplicate business completion; capture it from deterministic replay.
        FactoryRuntime replay = runtime.reset();
        replay.submitWorkload(new ProductId(1), 20, 12.5).orElseThrow();
        Event event;
        while ((event = replay.advance().orElse(null)) != null) if (event.payload() instanceof EventPayload.OrderCompleted completion) completions.add(completion);

        assertTrue(progress.contains(20L));
        assertEquals(20, runtime.orderExecution(orderId).completedQuantity());
        assertTrue(runtime.orderExecution(orderId).complete());
        assertEquals(1, completions.size());
        assertEquals(orderId, completions.getFirst().orderId());
        assertEquals(20, completions.getFirst().quantity());
        assertEquals(250.0, runtime.completedSalesValue());
        assertEquals(1, runtime.completedSales());
    }

    @Test
    void materializationLimitRejectsBeforeAnyRuntimeMutation() {
        FactoryRuntime runtime = runtime();
        var result = runtime.submitWorkload(new ProductId(1), 100_001, 1.0);
        assertTrue(result instanceof CommandResult.Rejected<?>);
        assertEquals(0, runtime.ordersView().count());
        assertEquals(0, runtime.orderExecutionsView().count());
        assertEquals(0, runtime.jobsView().count());
        assertEquals(0, runtime.machinesView().stream().mapToInt(machine -> machine.queueDepth()).sum());
        assertEquals(0, runtime.pendingWorkView().size());
        assertTrue(runtime.advance().isEmpty());
    }
}
