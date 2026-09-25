package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.factory.machines.Machine;
import com.arcogine.factory.machines.MachineStore;
import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.routing.Routing;
import com.arcogine.factory.routing.RoutingStep;
import com.arcogine.factory.routing.RoutingStore;
import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Executable characterization of the retained v1 derived-result arithmetic. */
class EngineSemanticsV1DerivedResultConformanceTest {

    @Test
    void meanLeadTimeAndBusyTicksSaturateAtTheLongBoundary() {
        FactoryRuntime runtime = runtime(2, Long.MAX_VALUE);
        runtime.submitWorkload(new ProductId(1), 1, 2.0).orElseThrow();
        runtime.submitWorkload(new ProductId(1), 1, 3.0).orElseThrow();

        while (runtime.advance().isPresent()) {
            // Drain internal and supported events alike; only terminal state is relevant here.
            runtime.drainSupportedEvents();
        }

        assertEquals(2L, runtime.completedSales(), "each completed order must count exactly once");
        assertEquals(5.0, runtime.completedSalesValue(), "completion-order accumulation must retain both values");
        assertEquals(
                (double) Long.MAX_VALUE / 2.0,
                runtime.avgLeadTime(),
                "the accumulated lead-time register must saturate instead of wrapping");
        assertEquals(Long.MAX_VALUE, runtime.machinesView().getFirst().busyTicks());
        assertEquals(0.0, runtime.throughput(0), "zero elapsed ticks must produce zero throughput");
    }

    @Test
    void emptyMeanLeadTimeIsZeroAndLeadTimeSubtractionFloorsAtZero() {
        FactoryHandler handler = oneMachineHandler();
        assertEquals(0.0, handler.avgLeadTime());

        Scheduler scheduler = new Scheduler();
        Event order = Event.of(
                SimTime.of(10),
                new EventPayload.OrderCreation(new ProductId(1), 1, 1.0));
        handler.handleEvent(order, scheduler);
        JobId jobId = handler.jobsView().findFirst().orElseThrow().id();

        // The direct handler contract accepts the explicit event time supplied by the event. This
        // boundary case proves SimTime.minus floors a completion earlier than creation at zero.
        handler.handleEvent(
                Event.of(SimTime.ZERO, new EventPayload.TaskEnd(jobId, new MachineId(1), 0)),
                scheduler);
        assertEquals(0.0, handler.avgLeadTime());
    }

    @Test
    void ordinaryBusyTicksAccumulatePerCompletedStep() {
        FactoryRuntime runtime = runtime(1, 7);
        runtime.submitWorkload(new ProductId(1), 1, 1.0).orElseThrow();
        while (runtime.advance().isPresent()) {
            runtime.drainSupportedEvents();
        }
        assertEquals(7L, runtime.machinesView().getFirst().busyTicks());
        assertEquals(1L, runtime.completedSales());
        assertEquals(1L, runtime.ordersView().count());
    }

    private static FactoryRuntime runtime(int concurrency, long duration) {
        FactoryModel model = new FactoryModel(
                List.of(new ConfiguredResource(new MachineId(1), "M1", concurrency, null, 0)),
                List.of(new OperationDefinition(
                        1,
                        "route",
                        List.of(new OperationStepDefinition(1, "step", Set.of(new MachineId(1)), duration)))),
                List.of(new ProductDefinition(new ProductId(1), "P1", 1)));
        FactoryModelVersion version = FactoryModelPublisher.publish(model);
        return FactoryRuntime.forModel(version);
    }

    private static FactoryHandler oneMachineHandler() {
        MachineStore machines = new MachineStore();
        machines.add(new Machine(new MachineId(1), "M1", 1, null, 0));
        RoutingStore routings = new RoutingStore();
        routings.addRouting(new Routing(
                1,
                "route",
                List.of(new RoutingStep(1, "step", new MachineId(1), 1))));
        routings.addProductRouting(new ProductId(1), 1);
        return new FactoryHandler(machines, routings, List.of(new ProductId(1)));
    }
}
