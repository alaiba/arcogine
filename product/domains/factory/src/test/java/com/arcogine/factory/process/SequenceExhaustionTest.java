package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.types.MachineId;
import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Proves that supported-event and scheduler sequences fail closed at {@code Long.MAX_VALUE}
 * (docs/architecture/runtime-contract.md, docs/architecture/engine-semantics.md §1.2). Each counter
 * is started near its limit through a package-private test seam; reaching the limit by running is
 * an arithmetic argument, not something these tests do.
 *
 * <p>Machine 1 serves the only product (one step, one unit at a time); machine 2 is an idle
 * machine used for single-event availability changes.
 */
class SequenceExhaustionTest {

    private static final ProductId PRODUCT = new ProductId(1);
    private static final MachineId BUSY = new MachineId(1);
    private static final MachineId IDLE = new MachineId(2);
    private static final long MAX = Long.MAX_VALUE;

    private static FactoryModelVersion model() {
        return FactoryModelPublisher.publish(new FactoryModel(
                List.of(
                        new ConfiguredResource(BUSY, "Mill", 1, null, 0),
                        new ConfiguredResource(IDLE, "Spare", 1, null, 0)),
                List.of(new OperationDefinition(
                        1, "Route", List.of(new OperationStepDefinition(1, "Mill", Set.of(BUSY), 5)))),
                List.of(new ProductDefinition(PRODUCT, "Widget", 1))));
    }

    /** Reflection keeps the scheduler ordinal seam out of production code entirely. */
    private static void seedSchedulerOrdinal(FactoryRuntime runtime, long nextOrdinal) throws ReflectiveOperationException {
        java.lang.reflect.Field schedulerField = FactoryRuntime.class.getDeclaredField("scheduler");
        schedulerField.setAccessible(true);
        java.lang.reflect.Field ordinal = com.arcogine.core.queue.Scheduler.class.getDeclaredField("nextSequence");
        ordinal.setAccessible(true);
        ordinal.setLong(schedulerField.get(runtime), nextOrdinal);
    }

    private static List<Long> sequences(List<RuntimeEventEnvelope> events) {
        return events.stream().map(RuntimeEventEnvelope::sequence).toList();
    }

    /** A session whose busy machine holds one job and queues another, seeded at {@code latest}. */
    private static FactoryRuntime loadedSessionAt(long latest) {
        FactoryRuntime runtime = FactoryRuntime.forModel(model());
        runtime.submitWorkload(PRODUCT, 2, 1.0).orElseThrow();
        runtime.drainSupportedEvents();
        runtime.seedEventSequenceForTesting(latest);
        return runtime;
    }

    @Test
    void aSingleEventCommandMayPublishSequenceMaxExactly() {
        FactoryRuntime runtime = FactoryRuntime.forModel(model());
        runtime.seedEventSequenceForTesting(MAX - 1);

        CommandResult<?> result = runtime.setMachineAvailability(IDLE, false);

        assertInstanceOf(CommandResult.Accepted.class, result);
        assertEquals(MAX, runtime.observe().metadata().latestEventSequence());
        assertEquals(List.of(MAX), sequences(runtime.drainSupportedEvents()));
    }

    @Test
    void anExhaustedSessionRefusesMutatingCommandsBeforeAnyMutation() {
        FactoryRuntime runtime = loadedSessionAt(MAX);
        OrderId first = runtime.ordersView().findFirst().orElseThrow().id();
        RuntimeObservation before = runtime.observe();

        CommandResult<OrderId> submit = runtime.submitWorkload(PRODUCT, 1, 1.0);
        CommandResult<?> toggle = runtime.setMachineAvailability(IDLE, false);

        for (CommandResult<?> refused : List.of(submit, toggle)) {
            CommandResult.Rejected<?> rejected = assertInstanceOf(CommandResult.Rejected.class, refused);
            assertInstanceOf(SimError.SequenceExhausted.class, rejected.error());
            assertEquals("SequenceExhausted", refused.code());
            assertTrue(refused.scheduledEvents().isEmpty());
        }
        assertEquals(before, runtime.observe());
        assertTrue(runtime.drainSupportedEvents().isEmpty());

        runtime.seedEventSequenceForTesting(0);
        OrderId next = runtime.submitWorkload(PRODUCT, 1, 1.0).orElseThrow();
        assertEquals(first.value() + 1, next.value(), "the refused submission must not have allocated an id");
    }

    @Test
    void aRedundantAvailabilityRequestStaysAnAcceptedNoOpWhenExhausted() {
        FactoryRuntime runtime = loadedSessionAt(MAX);

        CommandResult<?> result = runtime.setMachineAvailability(IDLE, true);

        assertInstanceOf(CommandResult.Accepted.class, result);
        assertTrue(result.scheduledEvents().isEmpty());
        assertTrue(runtime.drainSupportedEvents().isEmpty());
    }

    @Test
    void anExhaustedSessionRefusesToAdvanceWithoutProcessingAnEvent() {
        FactoryRuntime runtime = loadedSessionAt(MAX);
        RuntimeObservation before = runtime.observe();
        assertEquals(RuntimeRunState.ACTIVE, before.metadata().runState());

        assertThrows(SimError.SequenceExhausted.class, runtime::advance);
        assertThrows(SimError.SequenceExhausted.class, () -> runtime.advanceUntil(SimTime.of(1_000), 10));

        assertEquals(before, runtime.observe());
        assertTrue(runtime.drainSupportedEvents().isEmpty());
    }

    @Test
    void anExhaustedSessionRefusesAdvancementEvenWhenNothingWouldBeProcessed() {
        FactoryRuntime quiescent = FactoryRuntime.forModel(model());
        quiescent.seedEventSequenceForTesting(MAX);
        assertThrows(SimError.SequenceExhausted.class, quiescent::advance);
        assertThrows(SimError.SequenceExhausted.class, () -> quiescent.advanceUntil(SimTime.of(1_000), 10));

        FactoryRuntime loaded = loadedSessionAt(MAX);
        RuntimeObservation before = loaded.observe();
        assertThrows(SimError.SequenceExhausted.class, () -> loaded.advanceUntil(SimTime.ZERO, 10));
        assertThrows(SimError.SequenceExhausted.class, () -> loaded.advanceUntil(SimTime.of(1_000), 0));
        assertEquals(before, loaded.observe());
    }

    @Test
    void anExhaustedSessionStillObservesDrainsAndResets() {
        FactoryRuntime runtime = FactoryRuntime.forModel(model());
        runtime.seedEventSequenceForTesting(MAX - 1);
        runtime.setMachineAvailability(IDLE, false);

        assertEquals(MAX, runtime.observe().metadata().latestEventSequence());
        assertEquals(1, runtime.drainSupportedEvents().size());
        assertTrue(runtime.drainSupportedEvents().isEmpty());

        FactoryRuntime fresh = runtime.reset();
        fresh.setMachineAvailability(IDLE, false);
        assertEquals(List.of(1L), sequences(fresh.drainSupportedEvents()));
    }

    @Test
    void submissionCrossingMaxFaultsWithTheAppliedOrderAndPublishesThroughMax() {
        FactoryRuntime runtime = FactoryRuntime.forModel(model());
        runtime.seedEventSequenceForTesting(MAX - 1);

        CommandResult<OrderId> result = runtime.submitWorkload(PRODUCT, 2, 1.0);

        CommandResult.Faulted<?> faulted = assertInstanceOf(CommandResult.Faulted.class, result);
        assertInstanceOf(SimError.SequenceExhausted.class, faulted.error());
        assertEquals(runtime.ordersView().findFirst().orElseThrow().id(), faulted.value());
        assertEquals(List.of(MAX), sequences(runtime.drainSupportedEvents()));
        assertInstanceOf(CommandResult.Rejected.class, runtime.submitWorkload(PRODUCT, 1, 1.0));
    }

    @Test
    void availabilityCascadeCrossingMaxFaultsInsteadOfThrowing() {
        FactoryRuntime runtime = FactoryRuntime.forModel(model());
        runtime.setMachineAvailability(BUSY, false).orElseThrow();
        runtime.submitWorkload(PRODUCT, 2, 1.0).orElseThrow();
        runtime.drainSupportedEvents();
        runtime.seedEventSequenceForTesting(MAX - 1);

        CommandResult<?> result = runtime.setMachineAvailability(BUSY, true);

        CommandResult.Faulted<?> faulted = assertInstanceOf(CommandResult.Faulted.class, result);
        assertInstanceOf(SimError.SequenceExhausted.class, faulted.error());
        assertEquals(List.of(MAX), sequences(runtime.drainSupportedEvents()));
        assertInstanceOf(CommandResult.Rejected.class, runtime.setMachineAvailability(BUSY, false));
    }

    @Test
    void advanceCrossingMaxThrowsAfterPublishingThroughMax() {
        FactoryRuntime runtime = loadedSessionAt(MAX - 1);

        assertThrows(SimError.SequenceExhausted.class, runtime::advance);

        assertEquals(MAX, runtime.observe().metadata().latestEventSequence());
        assertEquals(List.of(MAX), sequences(runtime.drainSupportedEvents()));
    }

    @Test
    void schedulerOrdinalExhaustionSurfacesAsTheCommandFault() throws ReflectiveOperationException {
        FactoryRuntime runtime = FactoryRuntime.forModel(model());
        seedSchedulerOrdinal(runtime, MAX);

        CommandResult<OrderId> result = runtime.submitWorkload(PRODUCT, 1, 1.0);

        CommandResult.Faulted<?> faulted = assertInstanceOf(CommandResult.Faulted.class, result);
        SimError.SequenceExhausted error = assertInstanceOf(SimError.SequenceExhausted.class, faulted.error());
        assertEquals("scheduler insertion ordinal", error.counter());
        assertEquals(runtime.ordersView().findFirst().orElseThrow().id(), faulted.value());
    }
}
