package com.arcogine.research.executionaccount;

import static org.junit.jupiter.api.Assertions.*;

import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.process.CommandResult;
import com.arcogine.factory.process.FactoryRuntime;
import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeEventPayload;
import com.arcogine.factory.process.RuntimeEventType;
import com.arcogine.factory.process.RuntimeRunState;
import com.arcogine.research.experiment.LinearRoutingFamily;
import com.arcogine.types.MachineId;
import com.arcogine.types.SimTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Independent negative probes. Passing tests demonstrate the stated limitations, not repairs. */
class ExecutionAccountAdversarialTest {
    private static final MachineId MACHINE = new MachineId(1);
    private static final LinearRoutingFamily SINGLE = new LinearRoutingFamily(
            List.of(new LinearRoutingFamily.Step("PROCESS", 5)),
            List.of(LinearRoutingFamily.Resource.of("Machine", 1, "PROCESS")));

    private static FactoryRuntime runtime() {
        return FactoryRuntime.forModel(FactoryModelPublisher.publish(SINGLE.model()));
    }

    private static void submit(FactoryRuntime runtime, long quantity) {
        runtime.submitWorkload(LinearRoutingFamily.PRODUCT, quantity, LinearRoutingFamily.UNIT_PRICE).orElseThrow();
    }

    private static void collect(FactoryRuntime runtime, CapturedAccount account) {
        account.retain(runtime.drainSupportedEvents());
        account.observeFrontier(runtime.observe());
    }

    private static CommandResult<?> availability(FactoryRuntime runtime, MachineId machine, boolean online) {
        CommandResult<?> result = runtime.setMachineAvailability(machine, online);
        return result;
    }

    @Test
    void budgetExhaustionAndNoFutureCommandsDoNotEstablishClosure() {
        FactoryRuntime runtime = runtime();
        CapturedAccount account = CapturedAccount.from(runtime.observe());
        submit(runtime, 2);
        runtime.advanceUntil(SimTime.of(9), Long.MAX_VALUE);
        assertEquals(1, runtime.advanceUntil(SimTime.of(100), 1).size());
        collect(runtime, account);
        assertEquals(RuntimeRunState.ACTIVE, runtime.observe().metadata().runState());
        assertEquals(5, account.frontierTime());
        assertFalse(account.intervalDeterminacy(0, 100).determined());
        account.declareClosedThrough(100); // Deliberately false: scheduled completion is still pending.
        assertTrue(account.intervalDeterminacy(0, 100).determined());
        assertEquals(100, IntervalReadings.activeJobTicks(account, MACHINE, 0, 100));
        runtime.advanceUntil(SimTime.of(100), Long.MAX_VALUE); // No new command was needed.
        List<RuntimeEventEnvelope> completion = runtime.drainSupportedEvents();
        assertEquals(10, completion.getFirst().simulationTime().value());
        assertThrows(IllegalStateException.class, () -> account.retain(completion));
        assertEquals(10, runtime.observe().resources().getFirst().busyTicks());
    }

    @Test
    void honestAdvancementDeclarationWithStaleCaptureFrontierLicensesWrongIntegral() {
        FactoryRuntime runtime = runtime();
        CapturedAccount account = CapturedAccount.from(runtime.observe());
        submit(runtime, 1);
        collect(runtime, account); // All events through frontier 2 are retained.
        runtime.advanceUntil(SimTime.of(100), Long.MAX_VALUE);
        // Controller really processed every pending event and promises no more commands.
        // Capture has not yet received the final drain/observation.
        account.declareClosedThrough(100);
        assertTrue(account.missing().isEmpty());
        assertTrue(account.intervalDeterminacy(0, 100).determined());
        assertEquals(100, IntervalReadings.activeJobTicks(account, MACHINE, 0, 100));
        account.observeFrontier(runtime.observe());
        assertFalse(account.missing().isEmpty()); // A fresh frontier finally reveals the tail loss.
        assertFalse(account.intervalDeterminacy(0, 100).determined());
        assertThrows(IllegalStateException.class, () -> account.retain(runtime.drainSupportedEvents()));
        assertEquals(5, runtime.observe().resources().getFirst().busyTicks());
    }

    @Test
    void acceptedNoOpContradictsNoFurtherCommandsWithoutDetectableEvent() {
        FactoryRuntime runtime = runtime();
        CapturedAccount account = CapturedAccount.from(runtime.observe());
        account.declareClosedThrough(100);
        assertInstanceOf(CommandResult.Accepted.class, availability(runtime, MACHINE, true));
        assertTrue(runtime.drainSupportedEvents().isEmpty());
        assertEquals(account.basis(), runtime.observe());
        collect(runtime, account);
        assertTrue(account.intervalDeterminacy(0, 100).determined());
        assertEquals(0, IntervalReadings.activeJobTicks(account, MACHINE, 0, 100));
        // State-interval value remains true; the broader no-command assertion is false and invisible.
    }

    @Test
    void eventExactlyAtUpperBoundaryDoesNotChangeHalfOpenIntegral() {
        FactoryRuntime runtime = runtime();
        CapturedAccount account = CapturedAccount.from(runtime.observe());
        submit(runtime, 1);
        runtime.advanceUntil(SimTime.of(5), Long.MAX_VALUE);
        collect(runtime, account);
        assertTrue(account.intervalDeterminacy(0, 5).determined());
        assertEquals(5, IntervalReadings.activeJobTicks(account, MACHINE, 0, 5));
        CapturedAccount conservative = CapturedAccount.from(account.basis());
        conservative.retain(account.retainedEvents());
        conservative.observeFrontier(account.frontier());
        conservative.declareClosedThrough(5);
        submit(runtime, 1); // Accepted at b=5, so excluded from [0,5).
        List<RuntimeEventEnvelope> boundary = runtime.drainSupportedEvents();
        assertTrue(boundary.stream().allMatch(event -> event.simulationTime().value() == 5));
        account.retain(boundary);
        account.observeFrontier(runtime.observe());
        assertEquals(5, IntervalReadings.activeJobTicks(account, MACHINE, 0, 5));
        assertThrows(IllegalStateException.class, () -> conservative.retain(boundary));
    }

    @Test
    void markerAdvancesInternalTimeButSupportedFrontierConservativelyRefuses() {
        FactoryRuntime runtime = runtime();
        CapturedAccount account = CapturedAccount.from(runtime.observe());
        submit(runtime, 2);
        runtime.advanceUntil(SimTime.of(9), Long.MAX_VALUE);
        collect(runtime, account);
        assertEquals(5, account.frontierTime());
        assertEquals(1, runtime.advanceUntil(SimTime.of(10), 1).size());
        collect(runtime, account);
        assertEquals(5, account.frontierTime());
        assertFalse(account.intervalDeterminacy(5, 10).determined());
        submit(runtime, 1);
        List<RuntimeEventEnvelope> atTen = runtime.drainSupportedEvents();
        assertEquals(10, atTen.getFirst().simulationTime().value());
        account.retain(atTen);
        account.observeFrontier(runtime.observe());
        assertTrue(account.intervalDeterminacy(5, 10).determined());
        assertEquals(5, IntervalReadings.activeJobTicks(account, MACHINE, 5, 10));
    }

    @Test
    void pendingNoOpMarkerDoesNotPreventSupportedIntervalFinality() {
        FactoryRuntime runtime = runtime();
        CapturedAccount account = CapturedAccount.from(runtime.observe());
        submit(runtime, 1);
        runtime.advanceUntil(SimTime.of(5), 1); // Authoritative completion, then a marker remains.
        collect(runtime, account);
        assertEquals(RuntimeRunState.QUIESCENT, runtime.observe().metadata().runState());
        assertTrue(account.intervalDeterminacy(0, 5).determined());
        var before = runtime.observe();
        assertTrue(runtime.advance().isPresent()); // Remaining order-completion marker.
        assertEquals(before, runtime.observe());
        assertTrue(runtime.drainSupportedEvents().isEmpty());
    }

    @Test
    void gapAfterRequestedIntervalDoesNotEraseEarlierIntervalKnowledge() {
        FactoryRuntime runtime = runtime();
        CapturedAccount account = CapturedAccount.from(runtime.observe());
        submit(runtime, 2);
        runtime.advanceUntil(SimTime.of(5), Long.MAX_VALUE);
        collect(runtime, account);
        assertTrue(account.intervalDeterminacy(0, 5).determined());
        assertEquals(5, IntervalReadings.activeJobTicks(account, MACHINE, 0, 5));
        CapturedAccount frozen = CapturedAccount.from(account.basis());
        frozen.retain(account.retainedEvents());
        frozen.observeFrontier(account.frontier());
        runtime.advanceUntil(SimTime.of(100), Long.MAX_VALUE);
        runtime.drainSupportedEvents(); // Deliberately lose only changes at time 10.
        account.observeFrontier(runtime.observe());
        assertFalse(account.intervalDeterminacy(0, 5).determined());
        assertTrue(account.retainedEvents().stream().allMatch(event -> event.simulationTime().value() <= 5));
        assertEquals(5, IntervalReadings.activeJobTicks(frozen, MACHINE, 0, 5));
        assertFalse(CapturedAccount.from(runtime.observe()).intervalDeterminacy(0, 5).determined());
        // Earlier evidence is unchanged: blanket gap refusal is safe but not minimum/monotone.
    }

    @Test
    void serializedControllerCanCloseWithExhaustionEvidenceAndFinalCaptureWithoutNewApi() {
        FactoryRuntime runtime = runtime();
        CapturedAccount account = CapturedAccount.from(runtime.observe());
        submit(runtime, 2);
        // The exclusive driver stops issuing commands, then proves the time bound was reached
        // by the loop's stopping condition, not by assuming a budget-exhausted call drained work.
        int processed;
        do {
            processed = runtime.advanceUntil(SimTime.of(100), 1).size();
        } while (processed == 1);
        collect(runtime, account); // Drain, then observe while no caller can mutate the runtime.
        assertEquals(RuntimeRunState.QUIESCENT, runtime.observe().metadata().runState());
        assertEquals(10, account.frontierTime());
        assertTrue(account.missing().isEmpty());
        assertFalse(account.intervalDeterminacy(0, 100).determined());
        account.declareClosedThrough(100);
        assertEquals(10, IntervalReadings.activeJobTicks(account, MACHINE, 0, 100));
    }

    @Test
    void waitingResidenceAndResourceReadyWaitingAreDifferentLegitimateIntervals() {
        LinearRoutingFamily family = new LinearRoutingFamily(
                List.of(new LinearRoutingFamily.Step("CUT", 3), new LinearRoutingFamily.Step("BAKE", 4)),
                List.of(LinearRoutingFamily.Resource.of("Cutter", 1, "CUT"),
                        LinearRoutingFamily.Resource.of("Oven", 2, "BAKE")));
        FactoryRuntime runtime = FactoryRuntime.forModel(FactoryModelPublisher.publish(family.model()));
        MachineId oven = new MachineId(2);
        availability(runtime, oven, false).orElseThrow();
        submit(runtime, 2);
        runtime.advanceUntil(SimTime.of(6), Long.MAX_VALUE);
        var firstJob = runtime.observe().jobs().getFirst().jobId();
        assertTrue(runtime.observe().resources().get(1).activeJobIds().isEmpty());
        availability(runtime, oven, true).orElseThrow();
        List<RuntimeEventEnvelope> events = runtime.drainSupportedEvents();
        long waitingAt = events.stream().filter(event -> event.eventType() == RuntimeEventType.JOB_WAITING)
                .filter(event -> event.payload() instanceof RuntimeEventPayload.JobWaiting w
                        && w.jobId().equals(firstJob) && w.eligibleMachines().contains(oven))
                .findFirst().orElseThrow().simulationTime().value();
        long dispatchedAt = events.stream()
                .filter(event -> event.payload() instanceof RuntimeEventPayload.JobDispatched d
                        && d.jobId().equals(firstJob) && d.stepIndex() == 1)
                .findFirst().orElseThrow().simulationTime().value();
        assertEquals(3, waitingAt);
        assertEquals(6, dispatchedAt);
        assertEquals(3, dispatchedAt - waitingAt); // Queue/step-ready residence includes offline time.
        assertEquals(0, dispatchedAt - 6); // Waiting with an online eligible resource excludes it.
        assertEquals(1, runtime.observe().resources().get(1).activeJobIds().size());
        assertEquals(1, runtime.observe().resources().get(1).queueDepth());
        // Also one free slot with eligible waiting work: no forced starvation/causal verdict follows.
    }
}
