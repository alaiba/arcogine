package com.arcogine.factory.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arcogine.types.MachineId;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RoutingStepTest {

    @Test
    void rejectsNullName() {
        assertThrows(
                NullPointerException.class,
                () -> new RoutingStep(1, null, Set.of(new MachineId(1)), 5));
    }

    @Test
    void rejectsNullEligibleMachines() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new RoutingStep(1, "Milling", (Set<MachineId>) null, 5));
    }

    @Test
    void rejectsEmptyEligibleMachines() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new RoutingStep(1, "Milling", Set.of(), 5));
    }

    @Test
    void defensivelyCopiesEligibleMachines() {
        Set<MachineId> source = new java.util.HashSet<>(Set.of(new MachineId(1), new MachineId(2)));
        RoutingStep step = new RoutingStep(1, "Milling", source, 5);

        source.add(new MachineId(3));

        assertEquals(Set.of(new MachineId(1), new MachineId(2)), step.eligibleMachines());
        assertThrows(
                UnsupportedOperationException.class,
                () -> step.eligibleMachines().add(new MachineId(4)));
    }

    @Test
    void singleMachineConvenienceConstructorProducesSingletonEligibleSet() {
        RoutingStep step = new RoutingStep(1, "Milling", new MachineId(1), 5);

        assertEquals(Set.of(new MachineId(1)), step.eligibleMachines());
    }
}
