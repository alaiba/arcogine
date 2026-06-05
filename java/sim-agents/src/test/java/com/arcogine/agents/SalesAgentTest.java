package com.arcogine.agents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.event.EventType;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Ported from crates/sim-agents/tests/sales_agent.rs. */
class SalesAgentTest {

    private static SalesAgent makeAgent(int backlogHigh, int backlogLow) {
        return new SalesAgent(new SalesAgentConfig(backlogHigh, backlogLow, 0.10, 1.0, 50.0));
    }

    /** Mirrors the Rust `AgentObservation { backlog, current_price, ..Default::default() }`. */
    private static AgentObservation observation(int backlog, double currentPrice) {
        return new AgentObservation(backlog, 0.0, 0.0, 0L, currentPrice, 0.0);
    }

    @Test
    void raisesPriceWhenBacklogHigh() {
        SalesAgent agent = makeAgent(10, 3);
        agent.observe(observation(15, 10.0));
        Optional<Double> decision = agent.decide();
        assertTrue(decision.isPresent());
        double newPrice = decision.get();
        assertTrue(newPrice > 10.0, "price should increase: got " + newPrice);
    }

    @Test
    void lowersPriceWhenBacklogLow() {
        SalesAgent agent = makeAgent(10, 3);
        agent.observe(observation(1, 10.0));
        Optional<Double> decision = agent.decide();
        assertTrue(decision.isPresent());
        double newPrice = decision.get();
        assertTrue(newPrice < 10.0, "price should decrease: got " + newPrice);
    }

    @Test
    void holdsPriceWhenBacklogNormal() {
        SalesAgent agent = makeAgent(10, 3);
        agent.observe(observation(5, 10.0));
        Optional<Double> decision = agent.decide();
        assertTrue(decision.isEmpty(), "agent should hold price in normal range");
    }

    @Test
    void respectsMaxPrice() {
        SalesAgent agent = makeAgent(10, 3);
        agent.observe(observation(100, 49.5));
        Optional<Double> decision = agent.decide();
        assertTrue(decision.isPresent());
        double newPrice = decision.get();
        assertTrue(newPrice <= 50.0, "price should not exceed max: got " + newPrice);
    }

    @Test
    void respectsMinPrice() {
        SalesAgent agent = makeAgent(10, 3);
        agent.observe(observation(0, 1.05));
        Optional<Double> decision = agent.decide();
        assertTrue(decision.isPresent());
        double newPrice = decision.get();
        assertTrue(newPrice >= 1.0, "price should not go below min: got " + newPrice);
    }

    @Test
    void handleEventSchedulesPriceChange() throws SimError {
        SalesAgent agent = makeAgent(10, 3);
        agent.observe(observation(20, 10.0));

        Scheduler scheduler = new Scheduler();
        scheduler.schedule(Event.of(SimTime.of(100), EventPayload.AgentEvaluation.INSTANCE));
        Event event = scheduler.nextEvent().orElseThrow();

        agent.handleEvent(event, scheduler);

        assertEquals(1L, agent.interventions);
        assertFalse(scheduler.isEmpty(), "should have scheduled PriceChange + AgentDecision");

        Event ev1 = scheduler.nextEvent().orElseThrow();
        Event ev2 = scheduler.nextEvent().orElseThrow();

        List<EventType> types = new ArrayList<>();
        types.add(ev1.eventType());
        types.add(ev2.eventType());
        assertTrue(types.contains(EventType.PriceChange));
        assertTrue(types.contains(EventType.AgentDecision));
    }

    @Test
    void handleEventIgnoresNonAgentEvents() throws SimError {
        SalesAgent agent = makeAgent(10, 3);
        agent.observe(observation(20, 10.0));

        Scheduler scheduler = new Scheduler();
        scheduler.schedule(Event.of(SimTime.of(100), EventPayload.DemandEvaluation.INSTANCE));
        Event event = scheduler.nextEvent().orElseThrow();

        agent.handleEvent(event, scheduler);
        assertEquals(0L, agent.interventions);
        assertTrue(scheduler.isEmpty());
    }

    @Test
    void tracksInterventionCount() throws SimError {
        SalesAgent agent = makeAgent(5, 2);
        Scheduler scheduler = new Scheduler();

        for (int tick : new int[] {100, 200, 300}) {
            agent.observe(observation(10, 10.0));
            scheduler.schedule(Event.of(SimTime.of(tick), EventPayload.AgentEvaluation.INSTANCE));
            Event event = scheduler.nextEvent().orElseThrow();
            agent.handleEvent(event, scheduler);
            // Drain scheduled events.
            while (scheduler.nextEvent().isPresent()) {
                // discard
            }
        }

        assertEquals(3L, agent.interventions);
    }

    @Test
    void defaultConfigIsSensible() {
        SalesAgent agent = SalesAgent.withDefaultConfig();
        assertTrue(agent.config.backlogHigh() > agent.config.backlogLow());
        assertTrue(agent.config.minPrice() > 0.0);
        assertTrue(agent.config.maxPrice() > agent.config.minPrice());
        assertTrue(agent.config.adjustmentPct() > 0.0 && agent.config.adjustmentPct() < 1.0);
    }
}
