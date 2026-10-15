package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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

/** Executable conformance evidence for the current Engine derived-result arithmetic. */
class EngineDerivedResultConformanceTest {

    @Test
    void meanLeadTimeSaturatesAtTheLongBoundary() {
        FactoryRuntime runtime = runtime(2, Long.MAX_VALUE);
        runtime.submitWorkload(new ProductId(1), 1, 2.0).orElseThrow();
        runtime.submitWorkload(new ProductId(1), 1, 3.0).orElseThrow();
        drainToQuiescence(runtime);

        assertEquals(2L, runtime.completedSales(), "each completed order must count exactly once");
        assertEquals(5.0, runtime.completedSalesValue(), "completion-order accumulation must retain both values");
        assertEquals(
                (double) Long.MAX_VALUE / 2.0,
                runtime.avgLeadTime(),
                "the accumulated lead-time register must saturate instead of wrapping");
    }

    @Test
    void completedValueAccumulatesInCompletionOrder() {
        // One machine serialises the orders, so they complete in submission order.
        FactoryRuntime runtime = runtime(1, 7);
        runtime.submitWorkload(new ProductId(1), 1, 1.0).orElseThrow();
        runtime.submitWorkload(new ProductId(1), 1, 1.0).orElseThrow();
        runtime.submitWorkload(new ProductId(1), 1, 1.0e16).orElseThrow();
        drainToQuiescence(runtime);

        double inCompletionOrder = (1.0 + 1.0) + 1.0e16;
        double reassociated = (1.0e16 + 1.0) + 1.0;
        assertNotEquals(inCompletionOrder, reassociated, "the values must make summation order observable");
        assertEquals(
                Double.doubleToRawLongBits(inCompletionOrder),
                Double.doubleToRawLongBits(runtime.completedSalesValue()),
                "completed value must be summed exactly in completion order");
    }

    @Test
    void completedValueOverflowsToInfinityRatherThanWrapping() {
        FactoryRuntime runtime = runtime(1, 7);
        runtime.submitWorkload(new ProductId(1), 1, Double.MAX_VALUE).orElseThrow();
        runtime.submitWorkload(new ProductId(1), 1, Double.MAX_VALUE).orElseThrow();
        drainToQuiescence(runtime);

        assertEquals(Double.POSITIVE_INFINITY, runtime.completedSalesValue());
    }

    @Test
    void meanLeadTimeConversionRoundsToNearestAboveTheMantissaLimit() {
        // 2^53 + 1 ticks has no exact binary64 value; round-to-nearest-even reports 2^53.
        long leadTime = (1L << 53) + 1;
        FactoryRuntime runtime = runtime(1, leadTime);
        runtime.submitWorkload(new ProductId(1), 1, 1.0).orElseThrow();
        drainToQuiescence(runtime);

        assertEquals(1L, runtime.completedSales());
        assertEquals(9_007_199_254_740_992.0, runtime.avgLeadTime());
    }

    @Test
    void meanLeadTimeConversionRoundsUpAtTheUpperEvenTie() {
        // 2^53 + 3 rounds upward to the even significand; truncation would round downward.
        FactoryRuntime runtime = runtime(1, (1L << 53) + 3);
        runtime.submitWorkload(new ProductId(1), 1, 1.0).orElseThrow();
        drainToQuiescence(runtime);

        assertEquals(1L, runtime.completedSales());
        assertEquals(9_007_199_254_740_996.0, runtime.avgLeadTime());
    }

    @Test
    void throughputOverAZeroTickWindowIsZero() {
        FactoryRuntime runtime = runtime(1, 7);
        runtime.submitWorkload(new ProductId(1), 1, 1.0).orElseThrow();
        drainToQuiescence(runtime);

        // A completed order makes the numerator non-zero, so only the zero-window rule keeps the
        // result finite.
        assertEquals(1L, runtime.completedSales());
        assertEquals(0.0, runtime.throughput(0), "zero elapsed ticks must produce zero throughput");
        assertEquals(1.0 / 7.0, runtime.throughput(7));
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
    void ordinaryLeadTimesAccumulatePerCompletedOrder() {
        FactoryRuntime runtime = runtime(1, 7);
        runtime.submitWorkload(new ProductId(1), 1, 1.0).orElseThrow();
        runtime.submitWorkload(new ProductId(1), 1, 1.0).orElseThrow();
        drainToQuiescence(runtime);

        // One machine serialises the two orders: they complete at 7 and 14, both created at 0.
        assertEquals(2L, runtime.completedSales());
        assertEquals(2L, runtime.ordersView().count());
        assertEquals((7.0 + 14.0) / 2.0, runtime.avgLeadTime());
    }

    private static void drainToQuiescence(FactoryRuntime runtime) {
        while (runtime.advance().isPresent()) {
            // Drain internal and supported events alike; only terminal state is relevant here.
            runtime.drainSupportedEvents();
        }
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
