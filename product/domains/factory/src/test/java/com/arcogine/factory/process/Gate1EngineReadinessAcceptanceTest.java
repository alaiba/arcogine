package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.event.EventType;
import com.arcogine.factory.jobs.JobView;
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

/**
 * End-to-end acceptance evidence for Gate 1 of the Factory Simulation Engine Readiness plan (see
 * {@code docs/planning/factory-simulation-engine-readiness.md} §5). Unlike the narrower slice
 * tests it deliberately does not duplicate ({@link ExplicitWorkloadSubmissionTest}, {@link
 * ProportionalQuantityWorkTest}, {@link OrderIntentSeparationTest}), this test drives everything
 * through {@link FactoryRuntime} alone -- never {@link FactoryHandler}, a {@code Scheduler}, or
 * any store directly -- to prove the eight Gate 1 acceptance criteria hold together as a single
 * externally observable contract, from a published model through to deterministic completion.
 *
 * <p>The published model here has two routing steps and a quantity greater than one, so
 * completion genuinely depends on the full quantity-scaled routing executing, not on a
 * single-step coincidence.
 */
class Gate1EngineReadinessAcceptanceTest {

    private static final long QUANTITY = 4;
    private static final double UNIT_PRICE = 12.5;
    private static final long STEP_ONE_DURATION = 5;
    private static final long STEP_TWO_DURATION = 3;

    private static FactoryModelVersion publishedModel() {
        FactoryModel model = new FactoryModel(
                List.of(
                        new ResourceDefinition(new MachineId(1), "Mill", 1, null, 0),
                        new ResourceDefinition(new MachineId(2), "Drill", 1, null, 0)),
                List.of(new OperationDefinition(
                        1,
                        "Widget Route",
                        List.of(
                                new OperationStepDefinition(
                                        1, "Milling", Set.of(new MachineId(1)), STEP_ONE_DURATION),
                                new OperationStepDefinition(
                                        2, "Drilling", Set.of(new MachineId(2)), STEP_TWO_DURATION)))),
                List.of(new ProductDefinition(new ProductId(1), "Widget", 1)));
        return FactoryModelPublisher.publish(model);
    }

    private static FactoryRuntime freshRuntime() {
        return FactoryRuntime.forModel(publishedModel());
    }

    @Test
    void modelAndRuntimeBoundary() {
        FactoryRuntime runtime = freshRuntime();

        OrderId orderId = runtime.submitWorkload(new ProductId(1), QUANTITY, UNIT_PRICE);

        assertEquals(1L, runtime.ordersView().count(), "exactly one accepted order");
        assertEquals(1L, runtime.jobsView().count(), "exactly one job under the quantity model");
        JobView job = runtime.jobsView().findFirst().orElseThrow();
        assertEquals(orderId, job.orderId(), "the one job must reference the submitted order");
        assertFalse(job.isComplete(), "a freshly submitted job must not already be complete");
    }

    @Test
    void quantityDrivesRepeatedProductionStepCompletionBeforeTheJobIsDone() {
        FactoryRuntime runtime = freshRuntime();
        runtime.submitWorkload(new ProductId(1), QUANTITY, UNIT_PRICE);

        // Two routing steps repeated QUANTITY times means QUANTITY * 2 TaskEnd events are
        // required before the job can be complete -- not one, and not just QUANTITY.
        long requiredTaskEnds = QUANTITY * 2;
        for (long i = 0; i < requiredTaskEnds - 1; i++) {
            Event event = runtime.advance().orElseThrow();
            assertEquals(EventType.TaskEnd, event.eventType());
            assertFalse(
                    runtime.jobsView().findFirst().orElseThrow().isComplete(),
                    "job must not be complete before all quantity-scaled routing work has executed");
        }

        Event finalStep = runtime.advance().orElseThrow();
        assertEquals(EventType.TaskEnd, finalStep.eventType());
        assertTrue(
                runtime.jobsView().findFirst().orElseThrow().isComplete(),
                "job must be complete only after the full quantity-scaled routing has executed");
    }

    @Test
    void completionIsObservableThroughFactoryRuntimeAdvance() {
        FactoryRuntime runtime = freshRuntime();
        OrderId orderId = runtime.submitWorkload(new ProductId(1), QUANTITY, UNIT_PRICE);

        long requiredTaskEnds = QUANTITY * 2;
        Event lastEvent = null;
        int orderCompletedCount = 0;
        Event event;
        while ((event = runtime.advance().orElse(null)) != null) {
            lastEvent = event;
            if (event.payload() instanceof EventPayload.OrderCompleted) {
                orderCompletedCount++;
            }
        }

        assertEquals(1, orderCompletedCount, "exactly one OrderCompleted event must be observed");
        var completed = (EventPayload.OrderCompleted) lastEvent.payload();

        // OrderCompleted correlates back to the submitted order via jobId -> FactoryRuntime.job ->
        // JobView.orderId, under the 1 Order <-> 1 Job invariant this model deliberately keeps.
        JobView resolvedJob = runtime.job(completed.jobId());
        assertEquals(orderId, resolvedJob.orderId(), "OrderCompleted.jobId must resolve to the submitted order");
        assertTrue(resolvedJob.isComplete(), "the resolved job must be complete at the completion observation");

        assertEquals(0L, runtime.backlog(), "backlog must reach zero once the order completes");
        assertEquals(1L, runtime.completedSales(), "completed sales count must be exactly one");
        assertEquals(
                QUANTITY * UNIT_PRICE,
                runtime.completedSalesValue(),
                "completed sales value must equal quantity * unitPrice");
    }

    @Test
    void identicalWorkloadFromTwoFreshRuntimesProducesIdenticalOrderedEventStreamsAndTerminalState() {
        FactoryRuntime runtimeA = freshRuntime();
        FactoryRuntime runtimeB = freshRuntime();

        runtimeA.submitWorkload(new ProductId(1), QUANTITY, UNIT_PRICE);
        runtimeB.submitWorkload(new ProductId(1), QUANTITY, UNIT_PRICE);

        List<Event> eventsA = drainAll(runtimeA);
        List<Event> eventsB = drainAll(runtimeB);

        assertFalse(eventsA.isEmpty());
        assertEquals(eventsA, eventsB, "two fresh runtimes given the same model/workload must produce an "
                + "identical ordered event stream");

        JobView jobA = runtimeA.jobsView().findFirst().orElseThrow();
        JobView jobB = runtimeB.jobsView().findFirst().orElseThrow();
        assertEquals(jobA.isComplete(), jobB.isComplete());
        assertTrue(jobA.isComplete());
        assertEquals(jobA.leadTime(), jobB.leadTime());
        assertEquals(runtimeA.backlog(), runtimeB.backlog());
        assertEquals(runtimeA.completedSales(), runtimeB.completedSales());
        assertEquals(runtimeA.completedSalesValue(), runtimeB.completedSalesValue());
        assertEquals(runtimeA.avgLeadTime(), runtimeB.avgLeadTime());
    }

    private static List<Event> drainAll(FactoryRuntime runtime) {
        List<Event> events = new ArrayList<>();
        Event event;
        while ((event = runtime.advance().orElse(null)) != null) {
            events.add(event);
        }
        return events;
    }
}
