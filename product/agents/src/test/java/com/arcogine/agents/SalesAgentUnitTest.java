package com.arcogine.agents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Ported from the inline #[cfg(test)] module in crates/sim-agents/src/sales_agent.rs. */
class SalesAgentUnitTest {

    @Test
    void observeUpdatesInternalState() {
        SalesAgent agent = SalesAgent.withDefaultConfig();
        AgentObservation obs = new AgentObservation(42, 5.5, 100.0, 10L, 8.0, 0.5);
        agent.observe(obs);
        assertEquals(42, agent.observation().backlog());
        assertEquals(8.0, agent.observation().offerPrice());
    }

    @Test
    void decideReturnsNoneWhenBacklogNormal() {
        SalesAgent agent = new SalesAgent(new SalesAgentConfig(10, 3, 0.10, 0.5, 100.0));
        agent.observe(new AgentObservation(5, 0.0, 0.0, 0L, 10.0, 0.0));
        assertTrue(agent.decide().isEmpty());
    }

    @Test
    void decideRaisesPriceWhenBacklogHigh() {
        SalesAgent agent = new SalesAgent(new SalesAgentConfig(5, 2, 0.10, 0.5, 100.0));
        agent.observe(new AgentObservation(10, 0.0, 0.0, 0L, 10.0, 0.0));
        Optional<Double> decision = agent.decide();
        assertTrue(decision.isPresent());
        assertTrue(decision.get() > 10.0);
    }

    @Test
    void decideLowersPriceWhenBacklogLow() {
        SalesAgent agent = new SalesAgent(new SalesAgentConfig(10, 5, 0.10, 0.5, 100.0));
        agent.observe(new AgentObservation(1, 0.0, 0.0, 0L, 10.0, 0.0));
        Optional<Double> decision = agent.decide();
        assertTrue(decision.isPresent());
        assertTrue(decision.get() < 10.0);
    }

    @Test
    void defaultConfigValues() {
        SalesAgentConfig cfg = SalesAgentConfig.DEFAULT;
        assertEquals(10, cfg.backlogHigh());
        assertEquals(3, cfg.backlogLow());
        assertEquals(0.10, cfg.adjustmentPct());
        assertEquals(0.5, cfg.minPrice());
        assertEquals(100.0, cfg.maxPrice());
    }
}
