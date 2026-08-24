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
 *
 * offerPrice and avgLeadTime are read on demand via {@code DoubleSupplier}s rather
 * than pushed into the model, so tests that need to change them mid-test hold a
 * mutable {@code double[1]} box and supply {@code () -> box[0]}.
 */
class DemandModelTest {

    private static DemandModel makeDemandModel(double[] price, double[] leadTime) {
        return new DemandModel(
                5.0, // baseDemand
                0.5, // priceElasticity
                0.1, // leadTimeSensitivity
                () -> price[0],
                () -> leadTime[0],
                List.of(new ProductId(1)),
                new Random(42));
    }

    private static DemandModel makeModel(double baseDemand, double price) {
        return new DemandModel(
                baseDemand,
                0.5,
                0.0,
                () -> price,
                () -> 0.0,
                List.of(new ProductId(1)),
                new Random(42));
    }

    @Test
    void demandDecreasesWithHigherPrice() {
        DemandModel dmLow = makeDemandModel(new double[] {1.0}, new double[] {0.0});
        DemandModel dmHigh = makeDemandModel(new double[] {8.0}, new double[] {0.0});

        assertTrue(
                dmLow.computeDemand() > dmHigh.computeDemand(),
                "demand should decrease with higher price");
    }

    @Test
    void demandDecreasesWithHigherLeadTime() {
        DemandModel dmFast = makeDemandModel(new double[] {3.0}, new double[] {0.0});
        DemandModel dmSlow = makeDemandModel(new double[] {3.0}, new double[] {50.0});

        assertTrue(
                dmFast.computeDemand() > dmSlow.computeDemand(),
                "demand should decrease with higher lead time");
    }

    @Test
    void demandFloorsAtZero() {
        DemandModel dm = makeDemandModel(new double[] {100.0}, new double[] {1000.0});
        assertEquals(0.0, dm.computeDemand(), "demand should floor at 0");
    }

    @Test
    void demandAtBaseConditions() {
        DemandModel dm = makeDemandModel(new double[] {0.0}, new double[] {0.0});
        assertEquals(
                5.0,
                dm.computeDemand(),
                "demand at zero price and zero lead time should equal baseDemand");
    }

    @Test
    void priceChangeUpdatesDemand() {
        double[] price = {1.0};
        DemandModel dm = makeDemandModel(price, new double[] {0.0});
        double demandBefore = dm.computeDemand();

        price[0] = 8.0;
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
    void generateOrdersCarriesTheCurrentOfferPriceAsOrderPrice() {
        DemandModel model = makeModel(5.0, 7.0);
        Scheduler sched = new Scheduler();
        long count = model.generateOrders(sched);
        assertTrue(count > 0);
        for (long i = 0; i < count; i++) {
            Event evt = sched.nextEvent().orElseThrow();
            var payload = (EventPayload.OrderCreation) evt.payload();
            assertEquals(7.0, payload.unitPrice());
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
    void handleEventIgnoresPriceChangeSinceOfferPriceIsReadOnDemand() {
        // DemandModel no longer has its own price field to update on PriceChange: it reads
        // offerPrice live from its supplier every time computeDemand()/generateOrders() runs.
        DemandModel model = makeModel(5.0, 1.0);
        Scheduler sched = new Scheduler();
        Event event = Event.of(SimTime.of(1), new EventPayload.PriceChange(5.0));
        sched.schedule(event);
        sched.nextEvent();
        model.handleEvent(event, sched);
        assertTrue(sched.isEmpty(), "PriceChange is not relevant to DemandModel directly -- it's a no-op");
    }
}
