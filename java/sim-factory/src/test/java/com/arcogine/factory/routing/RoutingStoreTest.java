package com.arcogine.factory.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimError;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Ported from the inline #[cfg(test)] module in crates/sim-factory/src/routing.rs. */
class RoutingStoreTest {

    private static Routing sampleRouting() {
        return new Routing(
                1,
                "Widget Route",
                List.of(
                        new RoutingStep(1, "Step A", new MachineId(1), 5),
                        new RoutingStep(2, "Step B", new MachineId(2), 3)));
    }

    @Test
    void stepCountCorrect() {
        Routing r = sampleRouting();
        assertEquals(2, r.stepCount());
    }

    @Test
    void getStepOutOfBoundsIsNone() {
        Routing r = sampleRouting();
        assertFalse(r.getStep(5).isPresent());
    }

    @Test
    void getStepValidIndex() {
        Routing r = sampleRouting();
        RoutingStep step = r.getStep(0).orElseThrow();
        assertEquals(new MachineId(1), step.machineId());
    }

    @Test
    void routingStoreProductRoundtrip() {
        RoutingStore store = new RoutingStore();
        store.addRouting(sampleRouting());
        store.addProductRouting(new ProductId(10), 1);
        Routing r = store.getRoutingForProduct(new ProductId(10));
        assertEquals("Widget Route", r.name());
    }

    @Test
    void routingStoreUnknownProductErrors() {
        RoutingStore store = new RoutingStore();
        assertThrows(
                SimError.UnknownId.class, () -> store.getRoutingForProduct(new ProductId(99)));
    }

    @Test
    void getRoutingById() {
        RoutingStore store = new RoutingStore();
        store.addRouting(sampleRouting());
        Routing r = store.getRouting(1);
        assertEquals("Widget Route", r.name());
    }

    @Test
    void getRoutingUnknownIdErrors() {
        RoutingStore store = new RoutingStore();
        assertThrows(SimError.UnknownId.class, () -> store.getRouting(42));
    }

    @Test
    void productRoutingWithMissingRoutingIdErrors() {
        RoutingStore store = new RoutingStore();
        store.addProductRouting(new ProductId(1), 999);
        assertThrows(
                SimError.UnknownId.class, () -> store.getRoutingForProduct(new ProductId(1)));
    }
}
