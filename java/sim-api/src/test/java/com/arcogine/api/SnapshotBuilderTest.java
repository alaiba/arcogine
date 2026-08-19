package com.arcogine.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
 * Regression coverage for the revenue-consistency bug identified in
 * devel/architecture-assessment-events-state-observations.md: a completed job's
 * revenue must be fixed at the price in effect when it completed, not recomputed
 * from whatever price is live when a snapshot happens to be built.
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
    void completedJobRevenueInSnapshotMatchesPriceAtCompletionNotLivePrice() {
        ScenarioConfig config = ScenarioLoader.loadScenario(SCENARIO);
        IntegratedHandler handler = HandlerFactory.buildFromConfig(config);
        Scheduler scheduler = new Scheduler();

        Event order = Event.of(new SimTime(0), new EventPayload.OrderCreation(new ProductId(1), 3));
        scheduler.schedule(order);
        scheduler.nextEvent();
        handler.handleEvent(order, scheduler);

        Event taskEnd = scheduler.nextEvent().orElseThrow();
        handler.handleEvent(taskEnd, scheduler);

        // Price changes after the job has already completed.
        Event priceChange = Event.of(taskEnd.time(), new EventPayload.PriceChange(999.0));
        scheduler.schedule(priceChange);
        scheduler.nextEvent();
        handler.handleEvent(priceChange, scheduler);

        SimSnapshot snapshot = SnapshotBuilder.buildSnapshot(
                handler, new EventLog(), SimRunState.Running, scheduler.currentTime(), 3, config, null);

        JobInfo job = snapshot.jobs().stream()
                .filter(j -> j.status() == JobStatus.Completed)
                .findFirst()
                .orElseThrow();

        assertEquals(30.0, job.revenue(), "snapshot must report the price actually applied at completion");
        assertNotEquals(999.0 * 3, job.revenue(), "snapshot must not recompute revenue from the live price");
        assertEquals(
                handler.factory().totalRevenue,
                job.revenue(),
                "snapshot revenue must agree with the accumulated totalRevenue for the single completed job");
    }
}
