package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.core.event.Event;
import com.arcogine.factory.jobs.JobView;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.model.ResourceDefinition;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * End-to-end acceptance evidence for the Gate 3 session-control criteria of the Factory
 * Simulation Engine Readiness plan (see {@code docs/planning/factory-simulation-engine-readiness.md}
 * §7), driven entirely through {@link FactoryRuntime} -- never {@link FactoryHandler}, a store, or
 * a scheduler directly -- matching how {@link Gate1EngineReadinessAcceptanceTest} and {@link
 * Gate2MultiResourceDispatchAcceptanceTest} prove their gates end to end.
 *
 * <p>The published model here has two routing steps on two different machines and a quantity
 * greater than one, so the event stream used to prove {@link FactoryRuntime#advanceUntil}
 * convergence and {@link FactoryRuntime#reset()} reproducibility is more than a one-event
 * coincidence.
 */
class Gate3SessionControlAcceptanceTest {

    private static final long QUANTITY = 3;
    private static final double UNIT_PRICE = 9.0;
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

    private static List<Event> drainAll(FactoryRuntime runtime) {
        List<Event> events = new ArrayList<>();
        Event event;
        while ((event = runtime.advance().orElse(null)) != null) {
            events.add(event);
        }
        return events;
    }

    @Test
    void runtimeRetainsAndExposesItsSourceModelVersionThroughoutTheSession() {
        FactoryModelVersion version = publishedModel();
        FactoryRuntime runtime = FactoryRuntime.forModel(version);

        assertSame(
                version,
                runtime.modelVersion(),
                "the runtime must retain the exact published model version it was instantiated from");

        runtime.submitWorkload(new ProductId(1), QUANTITY, UNIT_PRICE);
        assertSame(
                version, runtime.modelVersion(), "source model version identity must not change as the session advances");

        drainAll(runtime);
        assertSame(version, runtime.modelVersion(), "source model version identity must survive to session completion");
    }

    @Test
    void rejectedSubmissionThrowsAStableStructuredErrorAndLeavesNoPartialMutation() {
        FactoryRuntime runtime = FactoryRuntime.forModel(publishedModel());

        SimError.OutOfRange quantityError = assertThrows(
                SimError.OutOfRange.class,
                () -> runtime.submitWorkload(new ProductId(1), 0, UNIT_PRICE),
                "an invalid quantity must be rejected with a stable, typed SimError subtype");
        assertEquals("quantity", quantityError.field(), "the rejection must identify the offending field");
        assertEquals(0L, runtime.ordersView().count(), "a rejected submission must not create an order");
        assertEquals(0L, runtime.jobsView().count(), "a rejected submission must not create a job");

        SimError.UnknownId productError = assertThrows(
                SimError.UnknownId.class,
                () -> runtime.submitWorkload(new ProductId(999), 1, UNIT_PRICE),
                "a product with no published routing must be rejected with a stable, typed SimError subtype");
        assertEquals(999L, productError.id(), "the rejection must identify the offending entity id");
        assertEquals(0L, runtime.ordersView().count(), "a rejection for an unknown product must not create an order");
        assertEquals(0L, runtime.jobsView().count(), "a rejection for an unknown product must not create a job");
    }

    @Test
    void advanceUntilBoundedToOneEventPerCallConvergesWithLoopingAdvance() {
        FactoryRuntime stepped = FactoryRuntime.forModel(publishedModel());
        stepped.submitWorkload(new ProductId(1), QUANTITY, UNIT_PRICE);
        List<Event> steppedEvents = drainAll(stepped);
        assertFalse(steppedEvents.isEmpty());

        FactoryRuntime bounded = FactoryRuntime.forModel(publishedModel());
        bounded.submitWorkload(new ProductId(1), QUANTITY, UNIT_PRICE);

        List<Event> boundedEvents = new ArrayList<>();
        List<Event> batch;
        do {
            batch = bounded.advanceUntil(SimTime.of(Long.MAX_VALUE), 1);
            assertTrue(batch.size() <= 1, "a maxEvents=1 call must never return more than one event");
            boundedEvents.addAll(batch);
        } while (!batch.isEmpty());

        assertEquals(
                steppedEvents,
                boundedEvents,
                "advanceUntil bounded to one event per call must process the identical ordered event "
                        + "stream that looping advance() one event at a time would");

        JobView steppedJob = stepped.jobsView().findFirst().orElseThrow();
        JobView boundedJob = bounded.jobsView().findFirst().orElseThrow();
        assertTrue(steppedJob.isComplete());
        assertEquals(steppedJob.isComplete(), boundedJob.isComplete());
        assertEquals(steppedJob.leadTime(), boundedJob.leadTime());
        assertEquals(stepped.backlog(), bounded.backlog());
        assertEquals(stepped.completedSales(), bounded.completedSales());
        assertEquals(stepped.completedSalesValue(), bounded.completedSalesValue());
        assertEquals(stepped.avgLeadTime(), bounded.avgLeadTime());
    }

    @Test
    void advanceUntilDrainingInOneUnboundedCallConvergesWithLoopingAdvance() {
        FactoryRuntime stepped = FactoryRuntime.forModel(publishedModel());
        stepped.submitWorkload(new ProductId(1), QUANTITY, UNIT_PRICE);
        List<Event> steppedEvents = drainAll(stepped);

        FactoryRuntime bounded = FactoryRuntime.forModel(publishedModel());
        bounded.submitWorkload(new ProductId(1), QUANTITY, UNIT_PRICE);
        List<Event> boundedEvents = bounded.advanceUntil(SimTime.of(Long.MAX_VALUE), Long.MAX_VALUE);

        assertEquals(
                steppedEvents,
                boundedEvents,
                "one unbounded advanceUntil call must process the identical ordered event stream that "
                        + "looping advance() one event at a time would");
        assertTrue(bounded.jobsView().findFirst().orElseThrow().isComplete());
    }

    @Test
    void advanceUntilStopsAtTheTargetSimulatedTimeWithoutProcessingLaterEvents() {
        FactoryRuntime runtime = FactoryRuntime.forModel(publishedModel());
        runtime.submitWorkload(new ProductId(1), QUANTITY, UNIT_PRICE);

        List<Event> beforeFirstCompletion = runtime.advanceUntil(SimTime.of(STEP_ONE_DURATION - 1), 100);
        assertTrue(
                beforeFirstCompletion.isEmpty(),
                "no event should process before the target time reaches the first scheduled event");

        List<Event> atFirstCompletion = runtime.advanceUntil(SimTime.of(STEP_ONE_DURATION), 100);
        assertFalse(atFirstCompletion.isEmpty(), "events scheduled at or before the target time must process");
        for (Event event : atFirstCompletion) {
            assertTrue(
                    event.time().compareTo(SimTime.of(STEP_ONE_DURATION)) <= 0,
                    "advanceUntil must never process an event scheduled after its target time");
        }
        assertFalse(
                runtime.jobsView().findFirst().orElseThrow().isComplete(),
                "the job must not be complete after only the first routing step has run");
    }

    @Test
    void advanceUntilRejectsANegativeMaxEvents() {
        FactoryRuntime runtime = FactoryRuntime.forModel(publishedModel());
        assertThrows(IllegalArgumentException.class, () -> runtime.advanceUntil(SimTime.ZERO, -1));
    }

    @Test
    void resetSessionReproducesIdenticalResultToTheOriginalSessionWithoutMutatingIt() {
        FactoryModelVersion version = publishedModel();
        FactoryRuntime original = FactoryRuntime.forModel(version);
        original.submitWorkload(new ProductId(1), QUANTITY, UNIT_PRICE);
        List<Event> originalEvents = drainAll(original);

        FactoryRuntime resetRuntime = original.reset();
        assertSame(
                version,
                resetRuntime.modelVersion(),
                "reset must construct a fresh session over the same retained model version");
        assertEquals(
                0L,
                resetRuntime.ordersView().count(),
                "reset must not carry over the original session's submitted workload");
        assertEquals(
                1L,
                original.ordersView().count(),
                "constructing a reset session must not mutate the original session it was reset from");

        resetRuntime.submitWorkload(new ProductId(1), QUANTITY, UNIT_PRICE);
        List<Event> resetEvents = drainAll(resetRuntime);

        assertFalse(resetEvents.isEmpty());
        assertEquals(
                originalEvents,
                resetEvents,
                "a reset session replaying the identical workload must reproduce an identical ordered "
                        + "event stream");

        JobView originalJob = original.jobsView().findFirst().orElseThrow();
        JobView resetJob = resetRuntime.jobsView().findFirst().orElseThrow();
        assertTrue(originalJob.isComplete());
        assertEquals(originalJob.isComplete(), resetJob.isComplete());
        assertEquals(originalJob.leadTime(), resetJob.leadTime());
        assertEquals(original.backlog(), resetRuntime.backlog());
        assertEquals(original.completedSales(), resetRuntime.completedSales());
        assertEquals(original.completedSalesValue(), resetRuntime.completedSalesValue());
        assertEquals(original.avgLeadTime(), resetRuntime.avgLeadTime());
    }
}
