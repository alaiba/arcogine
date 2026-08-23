package com.arcogine.core.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventType;
import com.arcogine.core.handler.EventHandler;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.types.SimError;
import com.arcogine.types.scenario.AgentConfig;
import com.arcogine.types.scenario.ScenarioConfig;
import com.arcogine.types.scenario.SimulationParams;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Ported from crates/sim-core/src/runner.rs #[cfg(test)] module. */
class SimRunnerTest {

    /** Does nothing. */
    private static final class NoopHandler implements EventHandler {
        @Override
        public void handleEvent(Event event, Scheduler scheduler) {
            // no-op
        }
    }

    /** Always fails. */
    private static final class FailingHandler implements EventHandler {
        @Override
        public void handleEvent(Event event, Scheduler scheduler) throws SimError {
            throw new SimError.Other("handler failure");
        }
    }

    private static ScenarioConfig minimalConfig(long maxTicks) {
        return new ScenarioConfig(
                new SimulationParams(1, maxTicks, 10L, 50L),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                null);
    }

    @Test
    void zeroMaxTicksReturnsImmediately() throws SimError {
        SimResult result = SimRunner.runScenario(minimalConfig(0), new NoopHandler());
        assertEquals(0L, result.eventsProcessed());
    }

    @Test
    void seedsDemandEvaluationAtInterval() throws SimError {
        SimResult result = SimRunner.runScenario(minimalConfig(100), new NoopHandler());
        long demandCount = result.eventLog().filterByType(EventType.DemandEvaluation).count();
        assertEquals(10L, demandCount);
    }

    @Test
    void seedsAgentEvaluationWhenEnabled() throws SimError {
        ScenarioConfig base = minimalConfig(100);
        ScenarioConfig config = new ScenarioConfig(
                base.simulation(), base.equipment(), base.material(),
                base.processSegment(), base.operationsDefinition(),
                base.economy(), new AgentConfig(true, "sales"));
        SimResult result = SimRunner.runScenario(config, new NoopHandler());
        long agentCount = result.eventLog().filterByType(EventType.AgentEvaluation).count();
        assertEquals(2L, agentCount);
    }

    @Test
    void noAgentEvaluationWithoutAgentConfig() throws SimError {
        ScenarioConfig config = minimalConfig(100);
        assertNull(config.agent());
        SimResult result = SimRunner.runScenario(config, new NoopHandler());
        long agentCount = result.eventLog().filterByType(EventType.AgentEvaluation).count();
        assertEquals(0L, agentCount);
    }

    @Test
    void noAgentEvaluationWhenDisabled() throws SimError {
        ScenarioConfig base = minimalConfig(100);
        ScenarioConfig config = new ScenarioConfig(
                base.simulation(), base.equipment(), base.material(),
                base.processSegment(), base.operationsDefinition(),
                base.economy(), new AgentConfig(false, "sales"));
        SimResult result = SimRunner.runScenario(config, new NoopHandler());
        long agentCount = result.eventLog().filterByType(EventType.AgentEvaluation).count();
        assertEquals(0L, agentCount);
    }

    @Test
    void handlerErrorPropagates() {
        ScenarioConfig config = minimalConfig(100);
        assertThrows(SimError.class, () -> SimRunner.runScenario(config, new FailingHandler()));
    }
}
