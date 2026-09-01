package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Headless Gate 4-A evidence for the supported observation boundary. */
class Gate4RuntimeObservationAcceptanceTest {

    private static FactoryModelVersion model() {
        return FactoryModelPublisher.publish(new FactoryModel(
                List.of(
                        new ResourceDefinition(new MachineId(2), "Cutter B", 1, null, 0),
                        new ResourceDefinition(new MachineId(1), "Cutter A", 1, null, 0)),
                List.of(new OperationDefinition(
                        1,
                        "Cut",
                        List.of(new OperationStepDefinition(
                                1, "CUT", Set.of(new MachineId(1), new MachineId(2)), 5)))),
                List.of(new ProductDefinition(new ProductId(1), "Widget", 1))));
    }

    @Test
    void observationCarriesStableRunFingerprintAndInitialSupportedCursor() {
        FactoryModelVersion version = model();
        FactoryRuntime runtime = FactoryRuntime.forModel(version);

        RuntimeObservation first = runtime.observe();
        RuntimeObservation repeated = runtime.observe();

        assertEquals(first, repeated);
        assertFalse(first.metadata().runId().value().toString().isBlank());
        assertEquals(version.fingerprint(), first.metadata().modelFingerprint());
        assertEquals(0, first.metadata().currentTime().value());
        assertEquals(RuntimeRunState.QUIESCENT, first.metadata().runState());
        assertEquals(0, first.metadata().latestEventSequence());
        assertEquals(List.of(new MachineId(1), new MachineId(2)),
                first.resources().stream().map(ResourceObservation::machineId).toList());
    }

    @Test
    void resetCreatesFreshCorrelationIdentityWithoutChangingSemanticOutcome() throws Exception {
        FactoryRuntime original = FactoryRuntime.forModel(model());
        FactoryRuntime reset = original.reset();

        assertNotEquals(original.runId(), reset.runId());
        original.submitWorkload(new ProductId(1), 3, 7.5).orElseThrow();
        reset.submitWorkload(new ProductId(1), 3, 7.5).orElseThrow();
        while (original.advance().isPresent()) {}
        while (reset.advance().isPresent()) {}

        RuntimeObservation originalObservation = original.observe();
        RuntimeObservation resetObservation = reset.observe();
        assertEquals(originalObservation.metadata().modelFingerprint(), resetObservation.metadata().modelFingerprint());
        assertEquals(originalObservation.resources(), resetObservation.resources());
        assertEquals(originalObservation.orders(), resetObservation.orders());
        assertEquals(originalObservation.jobs(), resetObservation.jobs());
        assertEquals(originalObservation.performance(), resetObservation.performance());
        assertEquals(0, resetObservation.metadata().latestEventSequence());
    }

    @Test
    void observationProjectsAuthoritativeOrderJobAndResourceStateWithoutInternalEvents() throws Exception {
        FactoryRuntime runtime = FactoryRuntime.forModel(model());
        OrderId orderId = runtime.submitWorkload(new ProductId(1), 3, 7.5).orElseThrow();

        RuntimeObservation running = runtime.observe();
        assertEquals(orderId, running.orders().getFirst().orderId());
        assertEquals(3, running.orders().getFirst().requestedQuantity());
        assertEquals(3, running.orders().getFirst().releasedQuantity());
        assertEquals(0, running.orders().getFirst().completedQuantity());
        assertEquals(List.of(0L, 1L, 2L), running.jobs().stream().map(JobObservation::ordinalWithinOrder).toList());
        assertTrue(running.jobs().stream().allMatch(job -> job.orderId().equals(orderId)));
        assertEquals(1, running.performance().backlog());
        assertEquals(RuntimeRunState.ACTIVE, running.metadata().runState());
        assertEquals(2, running.resources().stream().mapToInt(resource -> resource.activeJobIds().size()).sum());
        assertEquals(1, running.pendingWork().size());

        while (runtime.advance().isPresent()) {}
        RuntimeObservation completed = runtime.observe();
        assertTrue(completed.orders().getFirst().complete());
        assertEquals(3, completed.orders().getFirst().completedQuantity());
        assertEquals(0, completed.performance().backlog());
        assertEquals(1, completed.performance().completedOrders());
        assertEquals(10, completed.metadata().currentTime().value());
        assertEquals(RuntimeRunState.QUIESCENT, completed.metadata().runState());
    }

    @Test
    void observationCollectionsAreImmutableSnapshots() {
        FactoryRuntime runtime = FactoryRuntime.forModel(model());
        RuntimeObservation observation = runtime.observe();

        assertThrows(UnsupportedOperationException.class, () -> observation.resources().clear());
        assertThrows(UnsupportedOperationException.class, () -> observation.resources().getFirst().activeJobIds().clear());
        assertThrows(UnsupportedOperationException.class, () -> observation.pendingWork().clear());
        assertEquals(observation, runtime.observe());
    }
}
