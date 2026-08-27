package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.event.EventType;
import com.arcogine.factory.jobs.JobView;
import com.arcogine.factory.machines.MachineView;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.model.ResourceDefinition;
import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.MachineState;
import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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

        runtime.submitWorkload(new ProductId(1), QUANTITY, UNIT_PRICE).orElseThrow();
        assertSame(
                version, runtime.modelVersion(), "source model version identity must not change as the session advances");

        drainAll(runtime);
        assertSame(version, runtime.modelVersion(), "source model version identity must survive to session completion");
    }

    @Test
    void acceptedSubmissionReturnsAStructuredResultWithProvenanceAndScheduledEvents() {
        FactoryModelVersion version = publishedModel();
        FactoryRuntime runtime = FactoryRuntime.forModel(version);

        CommandResult<OrderId> result = runtime.submitWorkload(new ProductId(1), QUANTITY, UNIT_PRICE);

        assertInstanceOf(CommandResult.Accepted.class, result, "a valid submission must be accepted");
        assertEquals("ACCEPTED", result.code(), "an accepted result must carry the stable ACCEPTED code");
        assertEquals("accepted", result.diagnostic());
        assertSame(version, result.modelVersion(), "the result must carry the session's model provenance");
        assertEquals(
                runtime.jobsView().findFirst().orElseThrow().orderId(),
                result.orElseThrow(),
                "the accepted value must be the new OrderId");
        assertFalse(
                result.scheduledEvents().isEmpty(),
                "submitting workload against an idle machine must dispatch immediately, scheduling at "
                        + "least the first step's TaskEnd as a direct effect of this command");
        for (Event scheduled : result.scheduledEvents()) {
            assertEquals(EventType.TaskEnd, scheduled.eventType());
        }
    }

    @Test
    void rejectedSubmissionReturnsAStructuredResultAndLeavesNoPartialMutation() {
        FactoryRuntime runtime = FactoryRuntime.forModel(publishedModel());

        CommandResult<OrderId> quantityResult = runtime.submitWorkload(new ProductId(1), 0, UNIT_PRICE);
        assertInstanceOf(CommandResult.Rejected.class, quantityResult, "an invalid quantity must be rejected");
        CommandResult.Rejected<OrderId> quantityRejected = (CommandResult.Rejected<OrderId>) quantityResult;
        SimError.OutOfRange quantityThrown = assertThrows(
                SimError.OutOfRange.class,
                quantityResult::orElseThrow,
                "orElseThrow() on a rejected result must rethrow the original, typed SimError");
        assertEquals(
                quantityThrown, quantityRejected.error(), "orElseThrow() must rethrow the exact wrapped error");
        assertEquals(
                "OutOfRange",
                quantityResult.code(),
                "the rejection code must be the stable, typed SimError subtype name");
        assertEquals(quantityThrown.getMessage(), quantityResult.diagnostic());
        assertEquals("quantity", quantityThrown.field(), "the rejection must identify the offending field");
        assertTrue(quantityResult.scheduledEvents().isEmpty(), "a rejection must not schedule any event");
        assertEquals(0L, runtime.ordersView().count(), "a rejected submission must not create an order");
        assertEquals(0L, runtime.jobsView().count(), "a rejected submission must not create a job");

        CommandResult<OrderId> productResult = runtime.submitWorkload(new ProductId(999), 1, UNIT_PRICE);
        assertInstanceOf(CommandResult.Rejected.class, productResult, "an unknown product must be rejected");
        CommandResult.Rejected<OrderId> productRejected = (CommandResult.Rejected<OrderId>) productResult;
        assertInstanceOf(SimError.UnknownId.class, productRejected.error());
        SimError.UnknownId productError = (SimError.UnknownId) productRejected.error();
        assertEquals(999L, productError.id(), "the rejection must identify the offending entity id");
        assertEquals(0L, runtime.ordersView().count(), "a rejection for an unknown product must not create an order");
        assertEquals(0L, runtime.jobsView().count(), "a rejection for an unknown product must not create a job");
    }

    private static FactoryModelVersion oneStepMaxDurationModel() {
        FactoryModel model = new FactoryModel(
                List.of(new ResourceDefinition(new MachineId(1), "Mill", 1, null, 0)),
                List.of(new OperationDefinition(
                        1,
                        "Widget Route",
                        List.of(new OperationStepDefinition(
                                1, "Milling", Set.of(new MachineId(1)), Long.MAX_VALUE)))),
                List.of(new ProductDefinition(new ProductId(1), "Widget", 1)));
        return FactoryModelPublisher.publish(model);
    }

    /**
     * Reproduces the scenario independent review of PR #177 identified: {@code
     * FactoryModelValidator} only requires a step's duration to be positive, so a validly published
     * model can carry {@code Long.MAX_VALUE}. Once simulated time itself reaches a large enough
     * value, dispatching a job against such a step computes an end time that overflows {@code
     * SimTime}'s underlying {@code long} and wraps negative, which {@code Scheduler.schedule} would
     * reject as an ordering violation. Before the {@code FactoryHandler.submitOrder} reorder this
     * test proves, that failure surfaced only after the new {@code Order}/{@code Job} were already
     * created and the machine/job already started -- so a caught-and-wrapped rejection silently
     * misreported "nothing changed" while state had, in fact, already partially mutated.
     */
    @Test
    void rejectedSubmissionFromAPostValidationSchedulingFailureStillLeavesNoPartialMutation() {
        FactoryModelVersion version = oneStepMaxDurationModel();
        FactoryRuntime runtime = FactoryRuntime.forModel(version);

        OrderId firstOrder = runtime.submitWorkload(new ProductId(1), 1, UNIT_PRICE).orElseThrow();
        Event completion = runtime.advance().orElseThrow();
        assertEquals(EventType.TaskEnd, completion.eventType());
        assertTrue(
                runtime.jobsView().findFirst().orElseThrow().isComplete(),
                "the first order must complete, leaving simulated time at Long.MAX_VALUE and the "
                        + "machine idle again");

        CommandResult<OrderId> result = runtime.submitWorkload(new ProductId(1), 1, UNIT_PRICE);

        assertInstanceOf(
                CommandResult.Rejected.class,
                result,
                "a scheduling failure discovered after the quantity/routing preflight must still "
                        + "surface as a rejection, not an uncaught crash");
        CommandResult.Rejected<OrderId> rejected = (CommandResult.Rejected<OrderId>) result;
        assertInstanceOf(SimError.EventOrderingViolation.class, rejected.error());
        assertTrue(result.scheduledEvents().isEmpty());

        assertEquals(
                1L,
                runtime.ordersView().count(),
                "the rejected second submission must not have created a second order");
        assertEquals(
                1L,
                runtime.jobsView().count(),
                "the rejected second submission must not have created a second job");
        assertEquals(
                firstOrder,
                runtime.ordersView().findFirst().orElseThrow().id(),
                "only the first order may exist after the rejection");
        assertTrue(
                runtime.pendingWorkView().isEmpty(),
                "the rejected submission must not have left anything in the pending-work backlog");
        MachineView machine = runtime.machinesView().get(0);
        assertTrue(
                machine.activeJobs().isEmpty(),
                "the rejected submission must not have started a job on the machine");
        assertEquals(0, machine.queueDepth(), "the rejected submission must not have queued a job either");
    }

    @Test
    void machineAvailabilityCommandReturnsAStructuredResultForAcceptanceAndRejection() {
        FactoryModelVersion version = publishedModel();
        FactoryRuntime runtime = FactoryRuntime.forModel(version);

        CommandResult<EventPayload.MachineAvailabilityChange> accepted =
                runtime.setMachineAvailability(new MachineId(1), false);
        assertInstanceOf(CommandResult.Accepted.class, accepted);
        assertEquals("ACCEPTED", accepted.code());
        assertSame(version, accepted.modelVersion());
        assertEquals(new EventPayload.MachineAvailabilityChange(new MachineId(1), false), accepted.orElseThrow());
        assertTrue(
                accepted.scheduledEvents().isEmpty(),
                "taking an idle machine offline dispatches nothing, so no event is scheduled");

        CommandResult<EventPayload.MachineAvailabilityChange> rejected =
                runtime.setMachineAvailability(new MachineId(999), true);
        assertInstanceOf(CommandResult.Rejected.class, rejected, "an unknown machine id must be rejected");
        assertThrows(SimError.UnknownId.class, rejected::orElseThrow);
    }

    @Test
    void takingABusyMachineOfflineIsRejectedBeforeAnyMutation() {
        FactoryModelVersion version = publishedModel();
        FactoryRuntime runtime = FactoryRuntime.forModel(version);
        runtime.submitWorkload(new ProductId(1), QUANTITY, UNIT_PRICE).orElseThrow();
        assertEquals(
                new MachineId(1),
                runtime.jobsView().findFirst().orElseThrow().currentMachine(),
                "the submitted job must be actively running on Mill (MachineId 1)");

        CommandResult<EventPayload.MachineAvailabilityChange> result =
                runtime.setMachineAvailability(new MachineId(1), false);

        assertInstanceOf(
                CommandResult.Rejected.class,
                result,
                "taking a machine with an active job offline must be rejected, matching Machine#setAvailability");
        CommandResult.Rejected<EventPayload.MachineAvailabilityChange> rejected =
                (CommandResult.Rejected<EventPayload.MachineAvailabilityChange>) result;
        assertInstanceOf(SimError.InvalidStateTransition.class, rejected.error());
        assertTrue(result.scheduledEvents().isEmpty());

        MachineView machine = runtime.machinesView().stream()
                .filter(m -> m.id().equals(new MachineId(1)))
                .findFirst()
                .orElseThrow();
        assertEquals(
                MachineState.Busy,
                machine.state(),
                "a rejected offline command must not have changed the machine's operational status");
        assertFalse(
                machine.activeJobs().isEmpty(), "the active job must still be running after the rejected command");
    }

    private static FactoryModelVersion twoIndependentSingleMachineRoutesModel() {
        FactoryModel model = new FactoryModel(
                List.of(
                        new ResourceDefinition(new MachineId(1), "Mill A", 1, null, 0),
                        new ResourceDefinition(new MachineId(2), "Mill B", 1, null, 0)),
                List.of(
                        new OperationDefinition(
                                1,
                                "Route A",
                                List.of(new OperationStepDefinition(1, "Op A", Set.of(new MachineId(1)), 5))),
                        new OperationDefinition(
                                2,
                                "Route B",
                                List.of(new OperationStepDefinition(
                                        2, "Op B", Set.of(new MachineId(2)), Long.MAX_VALUE)))),
                List.of(
                        new ProductDefinition(new ProductId(1), "Product A", 1),
                        new ProductDefinition(new ProductId(2), "Product B", 2)));
        return FactoryModelPublisher.publish(model);
    }

    /**
     * Reproduces the third-round review's repro exactly: a machine coming online can dequeue and
     * start a previously waiting job (mutating machine/job state) before the resulting
     * {@code TaskStart}/{@code TaskEnd} scheduling call discovers, deep in that same dispatch
     * attempt, that simulated time has grown large enough (via an unrelated {@code Long.MAX_VALUE}
     * duration job on a different machine) to overflow {@code SimTime}. Full preflight safety for
     * this cascade was judged out of proportion for this slice (see ADR-0007); instead the command
     * must still return a definite result rather than let the failure escape as a bare exception --
     * proven here as {@link CommandResult.Faulted}, distinct from {@link CommandResult.Rejected}
     * precisely because mutation has already happened by the time it's returned.
     */
    @Test
    void setMachineAvailabilityReportsFaultedRatherThanThrowingWhenTheDispatchCascadeFailsAfterMutation() {
        FactoryRuntime runtime = FactoryRuntime.forModel(twoIndependentSingleMachineRoutesModel());

        runtime.setMachineAvailability(new MachineId(1), false).orElseThrow();
        runtime.submitWorkload(new ProductId(1), 1, UNIT_PRICE).orElseThrow(); // waits in M1's own queue
        runtime.submitWorkload(new ProductId(2), 1, UNIT_PRICE).orElseThrow(); // dispatches to M2 immediately

        Event completion = runtime.advance().orElseThrow(); // M2's TaskEnd fires at t=Long.MAX_VALUE
        assertEquals(EventType.TaskEnd, completion.eventType());

        CommandResult<EventPayload.MachineAvailabilityChange> result =
                runtime.setMachineAvailability(new MachineId(1), true);

        assertInstanceOf(
                CommandResult.Faulted.class,
                result,
                "a scheduling failure deep in the online-machine dispatch cascade must be reported "
                        + "as a definite result, not thrown past the command boundary");
        CommandResult.Faulted<EventPayload.MachineAvailabilityChange> faulted =
                (CommandResult.Faulted<EventPayload.MachineAvailabilityChange>) result;
        assertInstanceOf(SimError.EventOrderingViolation.class, faulted.error());
        assertThrows(SimError.EventOrderingViolation.class, result::orElseThrow);

        // Unlike Rejected, mutation is allowed to have already happened once Faulted is returned:
        // the queued job was dequeued and started on the now-online machine before the scheduling
        // call inside that same dispatch attempt failed.
        MachineView machineOne = runtime.machinesView().stream()
                .filter(m -> m.id().equals(new MachineId(1)))
                .findFirst()
                .orElseThrow();
        assertEquals(
                MachineState.Busy,
                machineOne.state(),
                "the dispatch attempt must have already started the machine before the fault");
        assertFalse(
                machineOne.activeJobs().isEmpty(),
                "the previously queued job must have already been marked active before the fault");
    }

    @Test
    void advanceUntilBoundedToOneEventPerCallConvergesWithLoopingAdvance() {
        FactoryRuntime stepped = FactoryRuntime.forModel(publishedModel());
        stepped.submitWorkload(new ProductId(1), QUANTITY, UNIT_PRICE).orElseThrow();
        List<Event> steppedEvents = drainAll(stepped);
        assertFalse(steppedEvents.isEmpty());

        FactoryRuntime bounded = FactoryRuntime.forModel(publishedModel());
        bounded.submitWorkload(new ProductId(1), QUANTITY, UNIT_PRICE).orElseThrow();

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
        stepped.submitWorkload(new ProductId(1), QUANTITY, UNIT_PRICE).orElseThrow();
        List<Event> steppedEvents = drainAll(stepped);

        FactoryRuntime bounded = FactoryRuntime.forModel(publishedModel());
        bounded.submitWorkload(new ProductId(1), QUANTITY, UNIT_PRICE).orElseThrow();
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
        runtime.submitWorkload(new ProductId(1), QUANTITY, UNIT_PRICE).orElseThrow();

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

    private static final long POOL_STEP_DURATION = 10;

    private static FactoryModelVersion twoEligibleMachinesModel() {
        FactoryModel model = new FactoryModel(
                List.of(
                        new ResourceDefinition(new MachineId(1), "Mill A", 1, null, 0),
                        new ResourceDefinition(new MachineId(2), "Mill B", 1, null, 0)),
                List.of(new OperationDefinition(
                        1,
                        "Widget Route",
                        List.of(new OperationStepDefinition(
                                1, "Milling", Set.of(new MachineId(1), new MachineId(2)), POOL_STEP_DURATION)))),
                List.of(new ProductDefinition(new ProductId(1), "Widget", 1)));
        return FactoryModelPublisher.publish(model);
    }

    @Test
    void pendingWorkViewExposesAMultiEligibleJobWaitingWhileBothEligibleMachinesAreOccupied() {
        FactoryRuntime runtime = FactoryRuntime.forModel(twoEligibleMachinesModel());

        OrderId orderA = runtime.submitWorkload(new ProductId(1), 1, UNIT_PRICE).orElseThrow();
        OrderId orderB = runtime.submitWorkload(new ProductId(1), 1, UNIT_PRICE).orElseThrow();
        assertTrue(
                runtime.pendingWorkView().isEmpty(),
                "the first two orders must dispatch directly onto the two idle eligible machines");

        assertEquals(
                0,
                runtime.machinesView().stream()
                        .mapToInt(MachineView::queueDepth)
                        .sum(),
                "both machines are actively running a job, not queueing one, so per-machine queue "
                        + "depth must be zero even though a third order is about to have to wait");

        OrderId orderC = runtime.submitWorkload(new ProductId(1), 1, UNIT_PRICE).orElseThrow();
        JobId waitingJobId =
                runtime.jobsView().filter(j -> j.orderId().equals(orderC)).findFirst().orElseThrow().id();

        assertEquals(
                0,
                runtime.machinesView().stream()
                        .mapToInt(MachineView::queueDepth)
                        .sum(),
                "the third order waits in the cross-machine multi-eligible backlog, not in either "
                        + "machine's own queue -- queueDepth() alone cannot see it");

        List<PendingWorkView> pending = runtime.pendingWorkView();
        assertEquals(1, pending.size(), "exactly the third order's job must be waiting");
        assertEquals(waitingJobId, pending.get(0).jobId());
        assertEquals(Set.of(new MachineId(1), new MachineId(2)), pending.get(0).eligibleMachines());

        assertEquals(
                null,
                runtime.jobsView().filter(j -> j.orderId().equals(orderC)).findFirst().orElseThrow().currentMachine(),
                "the waiting job must not yet be assigned to a machine");
        assertEquals(
                Set.of(orderA, orderB),
                runtime.jobsView()
                        .filter(j -> !j.orderId().equals(orderC))
                        .map(JobView::orderId)
                        .collect(Collectors.toSet()));

        // Freeing one machine must dispatch the waiting job and clear it from pendingWorkView().
        Event firstCompletion = runtime.advance().orElseThrow();
        assertEquals(EventType.TaskEnd, firstCompletion.eventType());
        assertTrue(
                runtime.pendingWorkView().isEmpty(),
                "once an eligible machine frees up, the waiting job must be dispatched and no longer pending");
        assertTrue(
                runtime.jobsView().filter(j -> j.orderId().equals(orderC)).findFirst().orElseThrow().currentMachine()
                        != null,
                "the previously waiting job must now be dispatched to the freed machine");
    }

    @Test
    void resetSessionReproducesIdenticalResultToTheOriginalSessionWithoutMutatingIt() {
        FactoryModelVersion version = publishedModel();
        FactoryRuntime original = FactoryRuntime.forModel(version);
        original.submitWorkload(new ProductId(1), QUANTITY, UNIT_PRICE).orElseThrow();
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

        resetRuntime.submitWorkload(new ProductId(1), QUANTITY, UNIT_PRICE).orElseThrow();
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
