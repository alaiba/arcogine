package com.arcogine.economy.demand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.event.EventType;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * Ported from crates/sim-economy/tests/demand_model.rs and the inline
 * #[cfg(test)] module in crates/sim-economy/src/demand.rs.
 *
 * The Rust suite seeds a ChaCha8Rng with 42; here we use {@link Random} with
 * the same seed. compute_demand() is deterministic and independent of the RNG,
 * so the demand-response tests are exact; the generate_orders tests only assert
 * on counts/event types, which hold regardless of RNG implementation.
 */
class DemandModelTest {

    private static DemandModel makeDemandModel(double price, double leadTime) {
        DemandModel dm = new DemandModel(
                5.0, // baseDemand
                0.5, // priceElasticity
                0.1, // leadTimeSensitivity
                price,
                List.of(new ProductId(1)),
                new Random(42));
        dm.setAvgLeadTime(leadTime);
        return dm;
    }

    private static DemandModel makeModel(double baseDemand, double price) {
        return new DemandModel(
                baseDemand,
                0.5,
                0.0,
                price,
                List.of(new ProductId(1)),
                new Random(42));
    }

    @Test
    void demandDecreasesWithHigherPrice() {
        DemandModel dmLow = makeDemandModel(1.0, 0.0);
        DemandModel dmHigh = makeDemandModel(8.0, 0.0);

        assertTrue(
                dmLow.computeDemand() > dmHigh.computeDemand(),
                "demand should decrease with higher price");
    }

    @Test
    void demandDecreasesWithHigherLeadTime() {
        DemandModel dmFast = makeDemandModel(3.0, 0.0);
        DemandModel dmSlow = makeDemandModel(3.0, 50.0);

        assertTrue(
                dmFast.computeDemand() > dmSlow.computeDemand(),
                "demand should decrease with higher lead time");
    }

    @Test
    void demandFloorsAtZero() {
        DemandModel dm = makeDemandModel(100.0, 1000.0);
        assertEquals(0.0, dm.computeDemand(), "demand should floor at 0");
    }

    @Test
    void demandAtBaseConditions() {
        DemandModel dm = makeDemandModel(0.0, 0.0);
        assertEquals(
                5.0,
                dm.computeDemand(),
                "demand at zero price and zero lead time should equal baseDemand");
    }

    @Test
    void priceChangeUpdatesDemand() {
        DemandModel dm = makeDemandModel(1.0, 0.0);
        double demandBefore = dm.computeDemand();

        dm.setPrice(8.0);
        double demandAfter = dm.computeDemand();

        assertTrue(demandAfter < demandBefore);
    }

    @Test
    void generateOrdersWithZeroDemandProducesNone() {
        DemandModel model = makeModel(0.0, 0.0);
        Scheduler sched = new Scheduler();
        long count = model.generateOrders(sched);
        assertEquals(0L, count);
    }

    @Test
    void generateOrdersSchedulesOrderCreationEvents() {
        DemandModel model = makeModel(5.0, 1.0);
        Scheduler sched = new Scheduler();
        long count = model.generateOrders(sched);
        assertTrue(count > 0);
        for (long i = 0; i < count; i++) {
            Optional<Event> evt = sched.nextEvent();
            assertTrue(evt.isPresent());
            assertEquals(EventType.OrderCreation, evt.get().eventType());
        }
    }

    @Test
    void handleEventIgnoresNonRelevantEvents() {
        DemandModel model = makeModel(5.0, 1.0);
        Scheduler sched = new Scheduler();
        Event event = Event.of(SimTime.of(1), EventPayload.AgentEvaluation.INSTANCE);
        sched.schedule(event);
        sched.nextEvent();
        model.handleEvent(event, sched);
        assertTrue(sched.isEmpty());
    }

    @Test
    void handleEventForDemandEvaluationGeneratesOrders() {
        DemandModel model = makeModel(5.0, 1.0);
        Scheduler sched = new Scheduler();
        Event event = Event.of(SimTime.of(10), EventPayload.DemandEvaluation.INSTANCE);
        sched.schedule(event);
        sched.nextEvent();
        model.handleEvent(event, sched);
        assertFalse(sched.isEmpty());
    }

    @Test
    void handleEventForPriceChangeUpdatesPrice() {
        DemandModel model = makeModel(5.0, 1.0);
        Scheduler sched = new Scheduler();
        Event event = Event.of(SimTime.of(1), new EventPayload.PriceChange(5.0));
        sched.schedule(event);
        sched.nextEvent();
        model.handleEvent(event, sched);
        // currentPrice is private; verify it became 5.0 indirectly via
        // computeDemand: baseDemand=5.0, elasticity=0.5, leadTime=0
        // -> demand = 5 - 0.5 * 5 = 2.5.
        assertEquals(2.5, model.computeDemand());
    }
}
