package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.event.EventType;
import com.arcogine.factory.machines.MachineView;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.model.ResourceDefinition;
import com.arcogine.types.MachineId;
import com.arcogine.types.MachineState;
import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimError;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Headless Gate 4-B evidence for the supported {@link RuntimeEventEnvelope} contract: post-
 * authoritative publication, run-scoped strictly monotonic sequencing independent of internal
 * scheduler machinery, durable model provenance, and W1 {@code OrderId}/{@code JobId} correlation.
 * Driven entirely through {@link FactoryRuntime}, matching {@link
 * Gate4RuntimeObservationAcceptanceTest} and {@link Gate3SessionControlAcceptanceTest}'s
 * conventions.
 */
class Gate4BRuntimeEventAcceptanceTest {

    private static final long QUANTITY = 3;
    private static final double UNIT_PRICE = 7.5;
    private static final long STEP_DURATION = 5;

    /** Two machines, one single-step routing, so quantity 3 leaves one unit pending dispatch. */
    private static FactoryModelVersion twoMachineModel() {
        return FactoryModelPublisher.publish(new FactoryModel(
                List.of(
                        new ResourceDefinition(new MachineId(1), "Cutter A", 1, null, 0),
                        new ResourceDefinition(new MachineId(2), "Cutter B", 1, null, 0)),
                List.of(new OperationDefinition(
                        1,
                        "Cut",
                        List.of(new OperationStepDefinition(
                                1, "CUT", Set.of(new MachineId(1), new MachineId(2)), STEP_DURATION)))),
                List.of(new ProductDefinition(new ProductId(1), "Widget", 1))));
    }

    /** Two independent single-machine routes -- reproduces the same fault repro as Gate 3. */
    private static FactoryModelVersion twoIndependentSingleMachineRoutesModel() {
        FactoryModel model = new FactoryModel(
                List.of(
                        new ResourceDefinition(new MachineId(1), "M1", 1, null, 0),
                        new ResourceDefinition(new MachineId(2), "M2", 1, null, 0)),
                List.of(
                        new OperationDefinition(
                                1,
                                "Op A",
                                List.of(new OperationStepDefinition(1, "A", Set.of(new MachineId(1)), 1))),
                        new OperationDefinition(
                                2,
                                "Op B",
                                List.of(new OperationStepDefinition(
                                        2, "B", Set.of(new MachineId(2)), Long.MAX_VALUE)))),
                List.of(
                        new ProductDefinition(new ProductId(1), "Product A", 1),
                        new ProductDefinition(new ProductId(2), "Product B", 2)));
        return FactoryModelPublisher.publish(model);
    }

    private static List<RuntimeEventEnvelope> drainToCompletion(FactoryRuntime runtime, ProductId productId)
            throws SimError {
        runtime.submitWorkload(productId, QUANTITY, UNIT_PRICE).orElseThrow();
        while (runtime.advance().isPresent()) {}
        return runtime.supportedEvents();
    }

    @Test
    void runtimeEventCarriesRunSequenceTimeAndModelProvenance() {
        FactoryModelVersion version = twoMachineModel();
        FactoryRuntime runtime = FactoryRuntime.forModel(version);

        OrderId orderId = runtime.submitWorkload(new ProductId(1), QUANTITY, UNIT_PRICE).orElseThrow();

        List<RuntimeEventEnvelope> events = runtime.supportedEvents();
        assertEquals(1, events.size());
        RuntimeEventEnvelope accepted = events.getFirst();
        assertEquals(runtime.runId(), accepted.runId());
        assertEquals(1, accepted.sequence());
        assertEquals(0, accepted.simulationTime().value());
        assertEquals(RuntimeEventType.ORDER_ACCEPTED, accepted.eventType());
        assertEquals(version.fingerprint(), accepted.modelFingerprint());
        assertTrue(accepted.controlledRevisionId().isEmpty(),
                "G4-B must not synthesize a controlled revision without an established binding contract");
        assertEquals(
                List.of(new AffectedEntityRef.OrderRef(orderId)), accepted.affectedEntityRefs());
        assertEquals(
                new RuntimeEventPayload.OrderAccepted(orderId, new ProductId(1), QUANTITY, UNIT_PRICE),
                accepted.payload());
    }

    @Test
    void sequenceIsStrictlyMonotonicWithinOneRun() throws SimError {
        FactoryRuntime runtime = FactoryRuntime.forModel(twoMachineModel());
        List<RuntimeEventEnvelope> events = drainToCompletion(runtime, new ProductId(1));

        assertTrue(events.size() >= 2, "scenario must produce more than one supported event");
        long previous = 0;
        for (RuntimeEventEnvelope event : events) {
            assertTrue(event.sequence() > previous, "sequence must be strictly increasing");
            previous = event.sequence();
        }
        assertEquals(events.getLast().sequence(), runtime.observe().metadata().latestEventSequence());
    }

    @Test
    void sameTimeEventsRemainOrderedBySequence() throws SimError {
        FactoryRuntime runtime = FactoryRuntime.forModel(twoMachineModel());
        List<RuntimeEventEnvelope> events = drainToCompletion(runtime, new ProductId(1));

        // Two JOB_STEP_COMPLETED events share simulationTime=5 (the two immediately-dispatched
        // units both finish their single step at the same instant); a JOB_STEP_COMPLETED and the
        // ORDER_COMPLETED it triggers share simulationTime=10.
        for (int i = 1; i < events.size(); i++) {
            RuntimeEventEnvelope prev = events.get(i - 1);
            RuntimeEventEnvelope curr = events.get(i);
            if (prev.simulationTime().equals(curr.simulationTime())) {
                assertTrue(curr.sequence() > prev.sequence());
            } else {
                assertTrue(curr.simulationTime().value() >= prev.simulationTime().value());
            }
        }
        long countAtFive = events.stream().filter(e -> e.simulationTime().value() == 5).count();
        assertEquals(2, countAtFive, "both immediately-dispatched units finish together at t=5");
    }

    @Test
    void observationLatestSequenceMatchesAppliedRuntimeEvents() throws SimError {
        FactoryRuntime runtime = FactoryRuntime.forModel(twoMachineModel());
        List<RuntimeEventEnvelope> events = drainToCompletion(runtime, new ProductId(1));

        assertEquals(events.getLast().sequence(), runtime.observe().metadata().latestEventSequence());
        assertEquals(events.size(), runtime.observe().metadata().latestEventSequence());
    }

    @Test
    void observationReflectsStateReportedByLastRuntimeEvent() throws SimError {
        FactoryRuntime runtime = FactoryRuntime.forModel(twoMachineModel());
        List<RuntimeEventEnvelope> events = drainToCompletion(runtime, new ProductId(1));

        RuntimeEventEnvelope last = events.getLast();
        assertEquals(RuntimeEventType.ORDER_COMPLETED, last.eventType());
        RuntimeEventPayload.OrderCompleted payload = (RuntimeEventPayload.OrderCompleted) last.payload();

        RuntimeObservation observation = runtime.observe();
        OrderObservation orderObservation = observation.orders().stream()
                .filter(o -> o.orderId().equals(payload.orderId()))
                .findFirst()
                .orElseThrow();
        assertTrue(orderObservation.complete());
        assertEquals(QUANTITY, orderObservation.completedQuantity());
    }

    @Test
    void rejectedTransitionDoesNotEmitSuccessfulStateChange() {
        FactoryRuntime runtime = FactoryRuntime.forModel(twoMachineModel());

        assertInstanceOf(
                CommandResult.Rejected.class, runtime.submitWorkload(new ProductId(1), 0, UNIT_PRICE));

        assertTrue(runtime.supportedEvents().isEmpty());
        assertEquals(0, runtime.observe().metadata().latestEventSequence());
    }

    @Test
    void faultReportsOnlyAuthoritativeChangesThatActuallyOccurred() throws SimError {
        FactoryRuntime runtime = FactoryRuntime.forModel(twoIndependentSingleMachineRoutesModel());

        runtime.setMachineAvailability(new MachineId(1), false).orElseThrow();
        runtime.submitWorkload(new ProductId(1), 1, UNIT_PRICE).orElseThrow();
        runtime.submitWorkload(new ProductId(2), 1, UNIT_PRICE).orElseThrow();
        Event completion = runtime.advance().orElseThrow();
        assertEquals(EventType.TaskEnd, completion.eventType());

        int before = runtime.supportedEvents().size();
        CommandResult<EventPayload.MachineAvailabilityChange> result =
                runtime.setMachineAvailability(new MachineId(1), true);
        assertInstanceOf(CommandResult.Faulted.class, result);

        List<RuntimeEventEnvelope> events = runtime.supportedEvents();
        assertEquals(before + 1, events.size(),
                "the machine coming online genuinely happened before the cascade faulted, so exactly "
                        + "one MACHINE_AVAILABILITY_CHANGED event -- and nothing claiming the failed "
                        + "downstream dispatch succeeded -- must be reported");
        RuntimeEventEnvelope last = events.getLast();
        assertEquals(RuntimeEventType.MACHINE_AVAILABILITY_CHANGED, last.eventType());
        assertEquals(
                new RuntimeEventPayload.MachineAvailabilityChanged(new MachineId(1), true), last.payload());

        MachineView machineOne = runtime.machinesView().stream()
                .filter(m -> m.id().equals(new MachineId(1)))
                .findFirst()
                .orElseThrow();
        assertEquals(MachineState.Busy, machineOne.state());
    }

    @Test
    void w1EventsPreserveOrderIdAndJobIdCorrelation() throws SimError {
        FactoryRuntime runtime = FactoryRuntime.forModel(twoMachineModel());
        OrderId orderId = runtime.submitWorkload(new ProductId(1), QUANTITY, UNIT_PRICE).orElseThrow();
        while (runtime.advance().isPresent()) {}

        List<RuntimeEventEnvelope> stepEvents = runtime.supportedEvents().stream()
                .filter(e -> e.eventType() == RuntimeEventType.JOB_STEP_COMPLETED)
                .toList();
        assertEquals(QUANTITY, stepEvents.size());
        for (RuntimeEventEnvelope event : stepEvents) {
            RuntimeEventPayload.JobStepCompleted payload = (RuntimeEventPayload.JobStepCompleted) event.payload();
            assertEquals(orderId, payload.orderId());
            assertTrue(event.affectedEntityRefs().contains(new AffectedEntityRef.JobRef(payload.jobId())));
            assertTrue(event.affectedEntityRefs().contains(new AffectedEntityRef.OrderRef(orderId)));
        }

        RuntimeEventEnvelope orderCompleted = runtime.supportedEvents().stream()
                .filter(e -> e.eventType() == RuntimeEventType.ORDER_COMPLETED)
                .findFirst()
                .orElseThrow();
        RuntimeEventPayload.OrderCompleted completedPayload =
                (RuntimeEventPayload.OrderCompleted) orderCompleted.payload();
        assertEquals(orderId, completedPayload.orderId());
        assertTrue(orderCompleted.affectedEntityRefs().contains(new AffectedEntityRef.JobRef(completedPayload.jobId())));
        assertTrue(stepEvents.stream()
                .map(e -> ((RuntimeEventPayload.JobStepCompleted) e.payload()).jobId())
                .anyMatch(jobId -> jobId.equals(completedPayload.jobId())));
    }

    @Test
    void identicalInputsProduceIdenticalSemanticEventStreams() throws SimError {
        FactoryModelVersion version = twoMachineModel();
        FactoryRuntime first = FactoryRuntime.forModel(version);
        FactoryRuntime second = FactoryRuntime.forModel(version);

        List<RuntimeEventEnvelope> firstEvents = drainToCompletion(first, new ProductId(1));
        List<RuntimeEventEnvelope> secondEvents = drainToCompletion(second, new ProductId(1));

        assertNotEquals(first.runId(), second.runId());
        assertEquals(firstEvents.size(), secondEvents.size());
        for (int i = 0; i < firstEvents.size(); i++) {
            RuntimeEventEnvelope a = firstEvents.get(i);
            RuntimeEventEnvelope b = secondEvents.get(i);
            assertEquals(a.sequence(), b.sequence());
            assertEquals(a.simulationTime(), b.simulationTime());
            assertEquals(a.eventType(), b.eventType());
            assertEquals(a.modelFingerprint(), b.modelFingerprint());
            assertEquals(a.affectedEntityRefs(), b.affectedEntityRefs());
            assertEquals(a.payload(), b.payload());
        }
    }

    @Test
    void resetCreatesNewRunAndSequenceEpoch() throws SimError {
        FactoryModelVersion version = twoMachineModel();
        FactoryRuntime original = FactoryRuntime.forModel(version);
        drainToCompletion(original, new ProductId(1));
        assertTrue(original.observe().metadata().latestEventSequence() > 0);

        FactoryRuntime reset = original.reset();

        assertNotEquals(original.runId(), reset.runId());
        assertTrue(reset.supportedEvents().isEmpty());
        assertEquals(0, reset.observe().metadata().latestEventSequence());
    }

    @Test
    void runIdentityDoesNotInfluenceSimulationOutcome() throws SimError {
        FactoryModelVersion version = twoMachineModel();
        FactoryRuntime first = FactoryRuntime.forModel(version);
        FactoryRuntime second = first.reset();
        assertNotEquals(first.runId(), second.runId());

        List<RuntimeEventEnvelope> firstEvents = drainToCompletion(first, new ProductId(1));
        List<RuntimeEventEnvelope> secondEvents = drainToCompletion(second, new ProductId(1));

        assertEquals(
                firstEvents.stream().map(RuntimeEventEnvelope::eventType).toList(),
                secondEvents.stream().map(RuntimeEventEnvelope::eventType).toList());
        assertEquals(first.observe().performance(), second.observe().performance());
        assertEquals(first.observe().orders(), second.observe().orders());
    }
}
