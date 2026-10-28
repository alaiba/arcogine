package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.routing.EligibleMachines;
import com.arcogine.factory.routing.RoutingStep;
import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Every engine-owned eligible-machine set is unordered and iterates in ascending signed {@link
 * MachineId} order, whatever order it was built from (docs/architecture/engine-semantics.md section
 * 2).
 */
class EligibleMachineOrderTest {

    private static final MachineId MINUS_ONE = new MachineId(-1);
    private static final MachineId ZERO = new MachineId(0);
    private static final MachineId FIVE = new MachineId(5);
    private static final List<MachineId> ASCENDING = List.of(MINUS_ONE, ZERO, FIVE);

    /** One engine-owned site that stores an eligible-machine set. */
    private record Site(String name, Function<Set<MachineId>, Set<MachineId>> store) {
        @Override
        public String toString() {
            return name;
        }
    }

    static Stream<Site> sites() {
        return Stream.of(
                new Site("EligibleMachines", EligibleMachines::ascending),
                new Site(
                        "OperationStepDefinition",
                        s -> new OperationStepDefinition(1, "Step", s, 1).eligibleResources()),
                new Site("RoutingStep", s -> new RoutingStep(1, "Step", s, 1).eligibleMachines()),
                new Site("PendingWorkView", s -> new PendingWorkView(new JobId(1), s).eligibleMachines()),
                new Site(
                        "JobWaiting",
                        s -> new RuntimeEventPayload.JobWaiting(new JobId(1), new OrderId(1), s)
                                .eligibleMachines()));
    }

    private static Set<MachineId> descending() {
        return new LinkedHashSet<>(List.of(FIVE, ZERO, MINUS_ONE));
    }

    @ParameterizedTest
    @MethodSource("sites")
    void iteratesInAscendingSignedOrderWhateverTheInputOrder(Site site) {
        assertEquals(ASCENDING, new ArrayList<>(site.store().apply(descending())));
        for (List<MachineId> permutation : permutations(ASCENDING)) {
            assertEquals(
                    ASCENDING,
                    new ArrayList<>(site.store().apply(new LinkedHashSet<>(permutation))),
                    () -> "built from " + permutation);
        }
    }

    @ParameterizedTest
    @MethodSource("sites")
    void isUnmodifiable(Site site) {
        Set<MachineId> stored = site.store().apply(descending());

        assertThrows(UnsupportedOperationException.class, () -> stored.add(new MachineId(7)));
        assertThrows(UnsupportedOperationException.class, () -> stored.remove(ZERO));
        assertThrows(UnsupportedOperationException.class, stored::clear);
    }

    @ParameterizedTest
    @MethodSource("sites")
    void keepsSetEqualityWithAnEqualImmutableSet(Site site) {
        Set<MachineId> stored = site.store().apply(descending());
        Set<MachineId> equal = Set.of(MINUS_ONE, ZERO, FIVE);

        assertEquals(equal, stored);
        assertEquals(stored, equal);
        assertEquals(equal.hashCode(), stored.hashCode());
    }

    @ParameterizedTest
    @MethodSource("sites")
    void isACopyOfItsInput(Site site) {
        Set<MachineId> source = descending();
        Set<MachineId> stored = site.store().apply(source);

        source.add(new MachineId(7));

        assertEquals(ASCENDING, new ArrayList<>(stored));
    }

    @ParameterizedTest
    @MethodSource("sites")
    void refusesANullElement(Site site) {
        Set<MachineId> withNull = new LinkedHashSet<>(Arrays.asList(FIVE, null));

        assertThrows(NullPointerException.class, () -> site.store().apply(withNull));
    }

    @Test
    void nullSetHandlingIsUnchangedAtEverySite() {
        assertThrows(NullPointerException.class, () -> EligibleMachines.ascending(null));
        assertTrue(new OperationStepDefinition(1, "Step", null, 1).eligibleResources().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> new RoutingStep(1, "Step", (Set<MachineId>) null, 1));
        assertThrows(NullPointerException.class, () -> new PendingWorkView(new JobId(1), null));
        assertThrows(
                NullPointerException.class,
                () -> new RuntimeEventPayload.JobWaiting(new JobId(1), new OrderId(1), null));
    }

    @Test
    void publishedWaitingEventAndPendingWorkViewIterateInAscendingOrder() {
        FactoryRuntime runtime = FactoryRuntime.forModel(FactoryModelPublisher.publish(new FactoryModel(
                List.of(
                        new ConfiguredResource(FIVE, "Five", 1, null, 0),
                        new ConfiguredResource(ZERO, "Zero", 1, null, 0),
                        new ConfiguredResource(MINUS_ONE, "Minus one", 1, null, 0)),
                List.of(new OperationDefinition(
                        1, "Cut", List.of(new OperationStepDefinition(1, "CUT", descending(), 5)))),
                List.of(new ProductDefinition(new ProductId(1), "Widget", 1)))));

        // Three idle eligible machines take three units; the fourth waits in the shared backlog.
        runtime.submitWorkload(new ProductId(1), 4, 1.0).orElseThrow();

        List<RuntimeEventPayload.JobWaiting> waiting = runtime.drainSupportedEvents().stream()
                .filter(e -> e.eventType() == RuntimeEventType.JOB_WAITING)
                .map(e -> (RuntimeEventPayload.JobWaiting) e.payload())
                .toList();
        assertEquals(1, waiting.size());
        assertEquals(ASCENDING, new ArrayList<>(waiting.getFirst().eligibleMachines()));

        List<PendingWorkView> pending = runtime.pendingWorkView();
        assertEquals(1, pending.size());
        assertEquals(waiting.getFirst().jobId(), pending.getFirst().jobId());
        assertEquals(ASCENDING, new ArrayList<>(pending.getFirst().eligibleMachines()));
    }

    private static List<List<MachineId>> permutations(List<MachineId> items) {
        if (items.isEmpty()) {
            return List.of(List.of());
        }
        List<List<MachineId>> result = new ArrayList<>();
        for (MachineId head : items) {
            List<MachineId> rest = new ArrayList<>(items);
            rest.remove(head);
            for (List<MachineId> tail : permutations(rest)) {
                List<MachineId> permutation = new ArrayList<>();
                permutation.add(head);
                permutation.addAll(tail);
                result.add(Collections.unmodifiableList(permutation));
            }
        }
        return result;
    }
}
