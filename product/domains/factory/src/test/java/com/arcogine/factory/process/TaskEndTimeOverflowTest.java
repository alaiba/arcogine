package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.core.event.EventPayload;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Every dispatch site that schedules a task end at {@code currentTime + duration} refuses a sum
 * above {@code Long.MAX_VALUE} as {@link SimError.EventOrderingViolation} -- never an {@link
 * ArithmeticException} or {@link IllegalArgumentException} -- while keeping the site's command
 * outcome (docs/architecture/engine-semantics.md sections 1.2 and 5.1). No wrapped, negative time is
 * ever published or observed.
 */
class TaskEndTimeOverflowTest {

    private static final double UNIT_PRICE = 1.0;
    private static final long MAX = Long.MAX_VALUE;

    private static OperationDefinition route(long id, Set<MachineId> eligible, long... durations) {
        List<OperationStepDefinition> steps = new ArrayList<>();
        for (int i = 0; i < durations.length; i++) {
            steps.add(new OperationStepDefinition(id * 10 + i, "Step " + i, eligible, durations[i]));
        }
        return new OperationDefinition(id, "Route " + id, steps);
    }

    private static ConfiguredResource machine(long id) {
        return new ConfiguredResource(new MachineId(id), "M" + id, 1, null, 0);
    }

    private static FactoryModelVersion publish(List<ConfiguredResource> machines, List<OperationDefinition> routes) {
        List<ProductDefinition> products = routes.stream()
                .map(r -> new ProductDefinition(new ProductId(r.id()), "P" + r.id(), r.id()))
                .toList();
        return FactoryModelPublisher.publish(new FactoryModel(machines, routes, products));
    }

    private static void assertUnrepresentableTaskEnd(SimError error, long currentTicks, long duration) {
        SimError.EventOrderingViolation violation = assertInstanceOf(SimError.EventOrderingViolation.class, error);
        assertEquals(SimTime.of(currentTicks), violation.expectedMin());
        assertTrue(violation.actual().isEmpty(), "an unrepresentable time must never be constructed");
        assertEquals(OptionalLong.of(duration), violation.duration());
    }

    /** No supported event, and no time in the supported observation, is negative. */
    private static void assertNoNegativeTimePublished(FactoryRuntime runtime, List<RuntimeEventEnvelope> events) {
        events.forEach(e -> assertTrue(e.simulationTime().ticks() >= 0, e.toString()));
        RuntimeObservation observation = runtime.observe();
        Stream<SimTime> times = Stream.concat(
                Stream.of(observation.metadata().currentTime()),
                Stream.concat(
                        observation.jobs().stream().flatMap(j -> Stream.of(j.createdAt(), j.completedAt())),
                        observation.orders().stream().flatMap(o -> Stream.of(o.createdAt(), o.completedAt()))));
        times.filter(Objects::nonNull).forEach(t -> assertTrue(t.ticks() >= 0, t.toString()));
    }

    @Test
    void submitPreflightRejectsAnOverflowingFirstStepBeforeMutation() {
        FactoryRuntime runtime = FactoryRuntime.forModel(
                publish(List.of(machine(1)), List.of(route(1, Set.of(new MachineId(1)), MAX))));
        runtime.submitWorkload(new ProductId(1), 1, UNIT_PRICE).orElseThrow();
        runtime.advance().orElseThrow(); // t = MAX, machine idle again
        List<RuntimeEventEnvelope> events = new ArrayList<>(runtime.drainSupportedEvents());

        CommandResult<OrderId> result = runtime.submitWorkload(new ProductId(1), 2, UNIT_PRICE);

        assertInstanceOf(CommandResult.Rejected.class, result);
        CommandResult.Rejected<OrderId> rejected = (CommandResult.Rejected<OrderId>) result;
        assertEquals("EventOrderingViolation", rejected.code());
        assertUnrepresentableTaskEnd(rejected.error(), MAX, MAX);
        assertEquals(1L, runtime.ordersView().count(), "a rejected submission creates no order");
        assertEquals(1L, runtime.jobsView().count(), "a rejected submission creates no job");
        events.addAll(runtime.drainSupportedEvents());
        assertNoNegativeTimePublished(runtime, events);
    }

    @Test
    void onlineMachineDispatchFaultsOnAnOverflowingQueuedTask() {
        FactoryRuntime runtime = FactoryRuntime.forModel(publish(
                List.of(machine(1), machine(2)),
                List.of(route(1, Set.of(new MachineId(1)), MAX), route(2, Set.of(new MachineId(2)), 1))));
        runtime.setMachineAvailability(new MachineId(1), false).orElseThrow();
        runtime.submitWorkload(new ProductId(1), 1, UNIT_PRICE).orElseThrow(); // waits in M1's queue
        runtime.submitWorkload(new ProductId(2), 1, UNIT_PRICE).orElseThrow();
        runtime.advance().orElseThrow(); // t = 1

        CommandResult<EventPayload.MachineAvailabilityChange> result =
                runtime.setMachineAvailability(new MachineId(1), true);

        assertInstanceOf(CommandResult.Faulted.class, result);
        CommandResult.Faulted<EventPayload.MachineAvailabilityChange> faulted =
                (CommandResult.Faulted<EventPayload.MachineAvailabilityChange>) result;
        assertEquals("EventOrderingViolation", faulted.code());
        assertUnrepresentableTaskEnd(faulted.error(), 1, MAX);
        assertNoNegativeTimePublished(runtime, runtime.drainSupportedEvents());
    }

    @Test
    void pendingMultiEligibleDispatchFaultsOnAnOverflowingTask() {
        Set<MachineId> shared = Set.of(new MachineId(1), new MachineId(2));
        FactoryRuntime runtime = FactoryRuntime.forModel(publish(
                List.of(machine(1), machine(2), machine(3)),
                List.of(route(1, shared, MAX), route(2, Set.of(new MachineId(3)), 1))));
        runtime.setMachineAvailability(new MachineId(1), false).orElseThrow();
        runtime.setMachineAvailability(new MachineId(2), false).orElseThrow();
        runtime.submitWorkload(new ProductId(1), 1, UNIT_PRICE).orElseThrow(); // pending on {M1, M2}
        assertEquals(1, runtime.pendingWorkView().size());
        runtime.submitWorkload(new ProductId(2), 1, UNIT_PRICE).orElseThrow();
        runtime.advance().orElseThrow(); // t = 1

        CommandResult<EventPayload.MachineAvailabilityChange> result =
                runtime.setMachineAvailability(new MachineId(2), true);

        assertInstanceOf(CommandResult.Faulted.class, result);
        CommandResult.Faulted<EventPayload.MachineAvailabilityChange> faulted =
                (CommandResult.Faulted<EventPayload.MachineAvailabilityChange>) result;
        assertEquals("EventOrderingViolation", faulted.code());
        assertUnrepresentableTaskEnd(faulted.error(), 1, MAX);
        assertNoNegativeTimePublished(runtime, runtime.drainSupportedEvents());
    }

    @Test
    void nextStepDispatchOnTaskEndThrowsFromAdvance() {
        FactoryRuntime runtime = FactoryRuntime.forModel(publish(
                List.of(machine(1), machine(2)),
                List.of(new OperationDefinition(1, "Two step", List.of(
                        new OperationStepDefinition(1, "First", Set.of(new MachineId(1)), 1),
                        new OperationStepDefinition(2, "Second", Set.of(new MachineId(2)), MAX))))));
        runtime.submitWorkload(new ProductId(1), 1, UNIT_PRICE).orElseThrow();

        SimError.EventOrderingViolation violation =
                assertThrows(SimError.EventOrderingViolation.class, runtime::advance);

        assertUnrepresentableTaskEnd(violation, 1, MAX);
        assertNoNegativeTimePublished(runtime, runtime.drainSupportedEvents());
    }
}
