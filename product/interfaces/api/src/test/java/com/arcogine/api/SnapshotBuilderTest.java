package com.arcogine.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.arcogine.api.dto.JobInfo;
import com.arcogine.api.dto.SimSnapshot;
import com.arcogine.api.state.HandlerFactory;
import com.arcogine.api.state.IntegratedHandler;
import com.arcogine.api.state.SimRunState;
import com.arcogine.api.state.SnapshotBuilder;
import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.log.EventLog;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.core.scenario.ScenarioLoader;
import com.arcogine.types.JobStatus;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimTime;
import com.arcogine.types.scenario.ScenarioConfig;
import org.junit.jupiter.api.Test;

/**
 * Regression coverage for the pricing/order semantics documented in the Architecture Overview:
 * an order's price is captured once, at OrderCreation, and is immutable for the life of the order.
 * An offer price change while an order is in production must not affect that order's value --
 * including as reported in an API snapshot.
 */
class SnapshotBuilderTest {

    private static final String SCENARIO =
            """
            [simulation]
            rng_seed = 1
            max_ticks = 500
            demand_eval_interval = 1000
            agent_eval_interval = 1000

            [[equipment]]
            id = 1
            name = "Mill"

            [[material]]
            id = 1
            name = "Widget"
            routing_id = 1

            [[process_segment]]
            id = 1
            name = "Milling"
            equipment_id = 1
            duration = 5

            [[operations_definition]]
            id = 1
            name = "Widget routing"
            steps = [1]

            [economy]
            initial_price = 10.0
            base_demand = 0.0
            price_elasticity = 0.0
            lead_time_sensitivity = 0.0
            """;

    @Test
    void completedJobValueInSnapshotMatchesOrderCreationPriceNotLaterOfferPrice() {
        ScenarioConfig config = ScenarioLoader.loadScenario(SCENARIO);
        IntegratedHandler handler = HandlerFactory.buildFromConfig(config);
        Scheduler scheduler = new Scheduler();

        // Order created while the offer price is 10; this locks in the order's own price.
        Event order = Event.of(
                new SimTime(0), new EventPayload.OrderCreation(new ProductId(1), 3, 10.0));
        scheduler.schedule(order);
        scheduler.nextEvent();
        handler.handleEvent(order, scheduler);

        // Offer price changes while the order is still in production (TaskEnd not yet due).
        Event priceChange = Event.of(new SimTime(0), new EventPayload.PriceChange(999.0));
        scheduler.schedule(priceChange);
        scheduler.nextEvent();
        handler.handleEvent(priceChange, scheduler);

        java.util.Optional<Event> pending;
        while ((pending = scheduler.nextEvent()).isPresent()) {
            Event next = pending.orElseThrow();
            handler.handleEvent(next, scheduler);
        }

        SimSnapshot snapshot = SnapshotBuilder.buildSnapshot(
                handler, new EventLog(), SimRunState.Running, scheduler.currentTime(), 3, config, null);

        JobInfo job = snapshot.jobs().stream()
                .filter(j -> j.status() == JobStatus.Completed)
                .findFirst()
                .orElseThrow();

        assertEquals(10.0, job.revenue(), "completed child projections report unit execution value");
        assertEquals(30.0, handler.factory().completedSalesValue());
    }
}
