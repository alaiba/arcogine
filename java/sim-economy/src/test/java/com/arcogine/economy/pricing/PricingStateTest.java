package com.arcogine.economy.pricing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.economy.pricing.PricingState.PricePoint;
import com.arcogine.types.SimTime;
import org.junit.jupiter.api.Test;

/**
 * Ported from crates/sim-economy/tests/pricing.rs and the inline #[cfg(test)]
 * module in crates/sim-economy/src/pricing.rs.
 */
class PricingStateTest {

    @Test
    void initialPriceSetCorrectly() {
        PricingState ps = new PricingState(10.0);
        assertEquals(10.0, ps.currentPrice());
        assertEquals(1, ps.priceHistory().size());
    }

    @Test
    void priceChangeRecorded() {
        PricingState ps = new PricingState(10.0);
        ps.setPrice(15.0, 50);

        assertEquals(15.0, ps.currentPrice());
        assertEquals(2, ps.priceHistory().size());
        assertEquals(new PricePoint(50, 15.0), ps.priceHistory().get(1));
    }

    @Test
    void multiplePriceChangesTracked() {
        PricingState ps = new PricingState(10.0);
        ps.setPrice(12.0, 10);
        ps.setPrice(8.0, 20);
        ps.setPrice(15.0, 30);

        assertEquals(15.0, ps.currentPrice());
        assertEquals(4, ps.priceHistory().size());
    }

    @Test
    void priceChangeUpdatesPriceAndHistory() {
        PricingState ps = new PricingState(10.0);
        Scheduler sched = new Scheduler();
        Event event = Event.of(SimTime.of(5), new EventPayload.PriceChange(15.0));
        sched.schedule(event);
        sched.nextEvent();
        ps.handleEvent(event, sched);
        assertEquals(15.0, ps.currentPrice());
        assertEquals(2, ps.priceHistory().size());
        assertEquals(new PricePoint(5, 15.0), ps.priceHistory().get(1));
    }

    @Test
    void ignoresNonPriceChangeEvents() {
        PricingState ps = new PricingState(10.0);
        Scheduler sched = new Scheduler();
        Event event = Event.of(SimTime.of(1), EventPayload.DemandEvaluation.INSTANCE);
        sched.schedule(event);
        sched.nextEvent();
        ps.handleEvent(event, sched);
        assertEquals(10.0, ps.currentPrice());
        assertEquals(1, ps.priceHistory().size());
    }
}
