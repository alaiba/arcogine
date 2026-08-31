package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.factory.jobs.JobView;
import com.arcogine.factory.machines.Machine;
import com.arcogine.factory.machines.MachineStore;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.model.ResourceDefinition;
import com.arcogine.factory.routing.Routing;
import com.arcogine.factory.routing.RoutingStep;
import com.arcogine.factory.routing.RoutingStore;
import com.arcogine.types.JobStatus;
import com.arcogine.types.MachineId;
import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Proves Gate 1's quantity-proportional-work criterion: an order for quantity N consumes N times
 * the routing/machine work of an otherwise identical quantity-1 order, for both the economy-driven
 * {@link EventPayload.OrderCreation} path and {@link FactoryRuntime}'s explicit workload
 * submission, while order completion still fires exactly once per order.
 */
class ProportionalQuantityWorkTest {

    private static final long STEP_DURATION = 5;

    private static FactoryHandler oneMachineOneProduct() {
        MachineStore machines = new MachineStore();
        machines.add(new Machine(new MachineId(1), "Mill", 1, null, 0));

        RoutingStore routings = new RoutingStore();
        routings.addRouting(new Routing(
                1,
                "Widget Route",
                List.of(new RoutingStep(1, "Milling", new MachineId(1), STEP_DURATION))));
        routings.addProductRouting(new ProductId(1), 1);

        return new FactoryHandler(machines, routings, List.of(new ProductId(1)));
    }

    private static FactoryModelVersion publishedModel() {
        FactoryModel model = new FactoryModel(
                List.of(new ResourceDefinition(new MachineId(1), "Mill", 1, null, 0)),
                List.of(new OperationDefinition(
                        1,
                        "Widget Route",
                        List.of(new OperationStepDefinition(
                                1, "Milling", java.util.Set.of(new MachineId(1)), STEP_DURATION)))),
                List.of(new ProductDefinition(new ProductId(1), "Widget", 1)));
        return FactoryModelPublisher.publish(model);
    }

    /** Drives a single order through to completion, returning the completed job's lead time. */
    private static long runOrderToCompletion(FactoryHandler h, long quantity) {
        Scheduler sched = new Scheduler();
        Event order = Event.of(new SimTime(0), new EventPayload.OrderCreation(new ProductId(1), quantity, 10.0));
        sched.schedule(order);
        sched.nextEvent();
        h.handleEvent(order, sched);

        Optional<Event> next;
        while ((next = sched.nextEvent()).isPresent()) {
            h.handleEvent(next.get(), sched);
        }

        return (long) h.avgLeadTime();
    }

    @Test
    void quantityTenConsumesStrictlyMoreProductionWorkThanQuantityOne() {
        long leadTimeOne = runOrderToCompletion(oneMachineOneProduct(), 1);
        long leadTimeTen = runOrderToCompletion(oneMachineOneProduct(), 10);

        assertEquals(STEP_DURATION, leadTimeOne);
        assertEquals(STEP_DURATION * 10, leadTimeTen);
        assertTrue(leadTimeTen > leadTimeOne, "quantity 10 must consume more production work than quantity 1");
    }

    @Test
    void totalStepsScaleLinearlyWithQuantityForAMultiStepRoute() {
        MachineStore machines = new MachineStore();
        machines.add(new Machine(new MachineId(1), "Mill", 1, null, 0));
        machines.add(new Machine(new MachineId(2), "Drill", 1, null, 0));

        RoutingStore routings = new RoutingStore();
        routings.addRouting(new Routing(
                1,
                "Widget Route",
                List.of(
                        new RoutingStep(1, "Milling", new MachineId(1), 5),
                        new RoutingStep(2, "Drilling", new MachineId(2), 3))));
        routings.addProductRouting(new ProductId(1), 1);
        FactoryHandler h = new FactoryHandler(machines, routings, List.of(new ProductId(1)));

        Scheduler sched = new Scheduler();
        Event order = Event.of(new SimTime(0), new EventPayload.OrderCreation(new ProductId(1), 4, 10.0));
        sched.schedule(order);
        sched.nextEvent();
        h.handleEvent(order, sched);

        assertEquals(4, h.jobsView().count());
        assertTrue(h.jobsView().allMatch(job -> job.totalSteps() == 2));
    }

    /**
     * Proves the modulo wrap-around (stepIndex % routing.stepCount()) that lets a job-global step
     * counter repeat a multi-step routing per unit, and that the externally visible {@link
     * EventPayload.TaskEnd#stepIndex()} continues to carry the routing-local index (0, 1, 0, 1,
     * ...), not the job-global one -- driving a quantity-3 order fully through a two-step route
     * rather than only asserting {@code totalSteps}.
     */
    @Test
    void multiStepRoutingRepeatsCorrectlyAcrossMultipleUnitsOfQuantity() {
        MachineStore machines = new MachineStore();
        machines.add(new Machine(new MachineId(1), "Mill", 1, null, 0));
        machines.add(new Machine(new MachineId(2), "Drill", 1, null, 0));

        RoutingStore routings = new RoutingStore();
        routings.addRouting(new Routing(
                1,
                "Widget Route",
                List.of(
                        new RoutingStep(1, "Milling", new MachineId(1), 5),
                        new RoutingStep(2, "Drilling", new MachineId(2), 3))));
        routings.addProductRouting(new ProductId(1), 1);
        FactoryHandler h = new FactoryHandler(machines, routings, List.of(new ProductId(1)));

        Scheduler sched = new Scheduler();
        Event order = Event.of(new SimTime(0), new EventPayload.OrderCreation(new ProductId(1), 3, 10.0));
        sched.schedule(order);
        sched.nextEvent();
        h.handleEvent(order, sched);

        java.util.List<Integer> stepIndices = new java.util.ArrayList<>();
        java.util.List<MachineId> machineIds = new java.util.ArrayList<>();
        long orderCompletedCount = 0;
        Optional<Event> next;
        while ((next = sched.nextEvent()).isPresent()) {
            h.handleEvent(next.get(), sched);
            if (next.get().payload() instanceof EventPayload.TaskEnd te) {
                stepIndices.add(te.stepIndex());
                machineIds.add(te.machineId());
            } else if (next.get().payload() instanceof EventPayload.OrderCompleted) {
                orderCompletedCount++;
            }
        }

        assertEquals(
                List.of(0, 1, 0, 1, 0, 1),
                stepIndices,
                "TaskEnd.stepIndex must repeat the routing-local index (0, 1) per unit, not a"
                        + " job-global counter (0..5)");
        assertEquals(List.of(new MachineId(1), new MachineId(2), new MachineId(1), new MachineId(2),
                new MachineId(1), new MachineId(2)), machineIds);
        assertEquals(1, orderCompletedCount, "OrderCompleted must fire exactly once for the whole order");
        assertEquals(1, h.completedSales());
    }

    @Test
    void identicalWorkloadProducesIdenticalOrderedResults() {
        FactoryHandler h1 = oneMachineOneProduct();
        FactoryHandler h2 = oneMachineOneProduct();
        Scheduler s1 = new Scheduler();
        Scheduler s2 = new Scheduler();

        Event o1 = Event.of(new SimTime(0), new EventPayload.OrderCreation(new ProductId(1), 10, 12.0));
        Event o2 = Event.of(new SimTime(0), new EventPayload.OrderCreation(new ProductId(1), 10, 12.0));
        s1.schedule(o1);
        s2.schedule(o2);
        s1.nextEvent();
        s2.nextEvent();
        h1.handleEvent(o1, s1);
        h2.handleEvent(o2, s2);

        Optional<Event> n1;
        Optional<Event> n2;
        while ((n1 = s1.nextEvent()).isPresent()) {
            n2 = s2.nextEvent();
            assertEquals(n1.get(), n2.orElseThrow(), "identical workload must produce an identical ordered event stream");
            h1.handleEvent(n1.get(), s1);
            h2.handleEvent(n2.get(), s2);
        }

        assertEquals(h1.completedSalesValue(), h2.completedSalesValue());
        assertEquals(h1.completedSales(), h2.completedSales());
    }

    @Test
    void orderCompletesExactlyOnceOnlyAfterAllRequiredWorkFinishes() {
        FactoryHandler h = oneMachineOneProduct();
        Scheduler sched = new Scheduler();
        Event order = Event.of(new SimTime(0), new EventPayload.OrderCreation(new ProductId(1), 5, 10.0));
        sched.schedule(order);
        sched.nextEvent();
        h.handleEvent(order, sched);

        long orderCompletedCount = 0;
        Optional<Event> next;
        while ((next = sched.nextEvent()).isPresent()) {
            h.handleEvent(next.get(), sched);
            if (next.get().payload() instanceof EventPayload.OrderCompleted) {
                orderCompletedCount++;
                assertEquals(
                        JobStatus.Completed,
                        h.jobsView().findFirst().orElseThrow().status(),
                        "OrderCompleted must only fire once every required unit of work has finished");
            }
        }

        assertEquals(1, orderCompletedCount, "OrderCompleted must fire exactly once for the order");
        assertEquals(1, h.completedSales());
    }

    @Test
    void allWorkForAnOrderReferencesThatOrder() {
        FactoryHandler h = oneMachineOneProduct();
        Scheduler sched = new Scheduler();
        Event order = Event.of(new SimTime(0), new EventPayload.OrderCreation(new ProductId(1), 7, 10.0));
        sched.schedule(order);
        sched.nextEvent();
        h.handleEvent(order, sched);

        OrderId orderId = h.ordersView().findFirst().orElseThrow().id();
        JobView job = h.jobsView().findFirst().orElseThrow();
        assertEquals(orderId, job.orderId());
        assertEquals(7L, h.order(orderId).quantity());
    }

    @Test
    void completedSalesValueIsQuantityTimesUnitPriceWithNoDoubleCounting() {
        FactoryHandler h = oneMachineOneProduct();
        long leadTime = runOrderToCompletion(h, 6);
        assertTrue(leadTime > 0);

        assertEquals(1, h.completedSales(), "one order must produce exactly one completed sale, regardless of quantity");
        assertEquals(6 * 10.0, h.completedSalesValue());
    }

    @Test
    void economyDrivenOrderCreationFollowsTheSameProportionalWorkSemanticsAsQuantityOne() {
        FactoryHandler h1 = oneMachineOneProduct();
        FactoryHandler h10 = oneMachineOneProduct();

        long leadTimeQty1 = runOrderToCompletion(h1, 1);
        long leadTimeQty10 = runOrderToCompletion(h10, 10);

        assertEquals(STEP_DURATION * 10, leadTimeQty10);
        assertTrue(leadTimeQty10 > leadTimeQty1);
    }

    @Test
    void explicitWorkloadSubmissionFollowsTheSameProportionalWorkSemantics() {
        FactoryRuntime runtimeQty1 = FactoryRuntime.forModel(publishedModel());
        FactoryRuntime runtimeQty10 = FactoryRuntime.forModel(publishedModel());

        runtimeQty1.submitWorkload(new ProductId(1), 1, 10.0).orElseThrow();
        runtimeQty10.submitWorkload(new ProductId(1), 10, 10.0).orElseThrow();

        while (runtimeQty1.advance().isPresent()) {
            // drain
        }
        while (runtimeQty10.advance().isPresent()) {
            // drain
        }

        assertEquals(STEP_DURATION, runtimeQty1.avgLeadTime());
        assertEquals(STEP_DURATION * 10, runtimeQty10.avgLeadTime());
        assertEquals(1L, runtimeQty1.completedSales());
        assertEquals(1L, runtimeQty10.completedSales());
    }

    @Test
    void explicitAndEconomyPathsProduceIdenticalProportionalWorkForTheSameQuantity() {
        FactoryHandler economyHandler = oneMachineOneProduct();
        long economyLeadTime = runOrderToCompletion(economyHandler, 5);

        FactoryRuntime explicitRuntime = FactoryRuntime.forModel(publishedModel());
        explicitRuntime.submitWorkload(new ProductId(1), 5, 10.0).orElseThrow();
        while (explicitRuntime.advance().isPresent()) {
            // drain
        }
        long explicitLeadTime = (long) explicitRuntime.avgLeadTime();

        assertEquals(economyLeadTime, explicitLeadTime);
    }

    @Test
    void zeroOrNegativeQuantityIsRejected() {
        FactoryHandler h = oneMachineOneProduct();
        Scheduler sched = new Scheduler();
        assertThrows(
                SimError.OutOfRange.class,
                () -> h.submitOrder(new ProductId(1), 0, 10.0, new SimTime(0), sched));
    }

    /**
     * A quantity whose {@code routing.stepCount() * quantity} exceeds the {@code int} execution
     * step counter must be rejected as a domain {@link SimError.OutOfRange}, not escape as a raw
     * {@code ArithmeticException} from overflowing arithmetic.
     */
    @Test
    void quantityThatWouldOverflowTheExecutionStepCounterIsRejectedAsADomainError() {
        FactoryHandler h = oneMachineOneProduct();
        Scheduler sched = new Scheduler();
        long unrepresentableQuantity = Integer.MAX_VALUE + 1L;

        SimError.OutOfRange error = assertThrows(
                SimError.OutOfRange.class,
                () -> h.submitOrder(new ProductId(1), unrepresentableQuantity, 10.0, new SimTime(0), sched));
        assertEquals("quantity", error.field());
    }

    /**
     * The overflow guard itself must not overflow: computing {@code stepsPerUnit * quantity} (even
     * widened to {@code long}) before comparing against {@code Integer.MAX_VALUE} can wrap for a
     * sufficiently large {@code long} quantity, silently bypassing the guard. A two-step routing
     * with {@code quantity = Long.MAX_VALUE} is the case that exposes that: {@code 2 *
     * Long.MAX_VALUE} overflows a signed long.
     */
    @Test
    void veryLargeLongQuantityOnAMultiStepRoutingIsRejectedWithoutInternalOverflow() {
        MachineStore machines = new MachineStore();
        machines.add(new Machine(new MachineId(1), "Mill", 1, null, 0));
        machines.add(new Machine(new MachineId(2), "Drill", 1, null, 0));

        RoutingStore routings = new RoutingStore();
        routings.addRouting(new Routing(
                1,
                "Widget Route",
                List.of(
                        new RoutingStep(1, "Milling", new MachineId(1), 5),
                        new RoutingStep(2, "Drilling", new MachineId(2), 3))));
        routings.addProductRouting(new ProductId(1), 1);
        FactoryHandler h = new FactoryHandler(machines, routings, List.of(new ProductId(1)));
        Scheduler sched = new Scheduler();

        SimError.OutOfRange error = assertThrows(
                SimError.OutOfRange.class,
                () -> h.submitOrder(new ProductId(1), Long.MAX_VALUE, 10.0, new SimTime(0), sched));
        assertEquals("quantity", error.field());
        assertEquals(0, h.jobsView().count(), "no job may be created for a rejected quantity");
    }
}
