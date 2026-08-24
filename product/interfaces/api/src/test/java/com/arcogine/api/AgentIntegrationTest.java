package com.arcogine.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.agents.SalesAgent;
import com.arcogine.agents.SalesAgentConfig;
import com.arcogine.api.state.IntegratedHandler;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.event.EventType;
import com.arcogine.core.runner.SimResult;
import com.arcogine.core.runner.SimRunner;
import com.arcogine.core.scenario.ScenarioLoader;
import com.arcogine.economy.demand.DemandModel;
import com.arcogine.economy.pricing.PricingState;
import com.arcogine.factory.machines.Machine;
import com.arcogine.factory.machines.MachineStore;
import com.arcogine.factory.process.FactoryHandler;
import com.arcogine.factory.routing.Routing;
import com.arcogine.factory.routing.RoutingStep;
import com.arcogine.factory.routing.RoutingStore;
import com.arcogine.finance.process.FinanceHandler;
import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimError;
import com.arcogine.types.scenario.EquipmentConfig;
import com.arcogine.types.scenario.MaterialConfig;
import com.arcogine.types.scenario.OperationsDefinitionConfig;
import com.arcogine.types.scenario.ScenarioConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * Ported from crates/sim-api/tests/agent_integration.rs. Verifies that the
 * SalesAgent intervenes under an overload scenario and reduces backlog growth
 * versus a fixed-price baseline. Builds the factory/demand/pricing pipeline the
 * same way {@code HandlerFactory} does, but with a test-configured agent (whose
 * intervention thresholds match the Rust suite) instead of the default one
 * {@code HandlerFactory.buildFromConfig} hardcodes -- so this test constructs the
 * real {@link IntegratedHandler} directly rather than duplicating its wiring
 * logic in a local test handler.
 */
class AgentIntegrationTest {

    private static final String OVERLOAD_TOML =
            """
            [simulation]
            rng_seed = 42
            max_ticks = 500
            demand_eval_interval = 10
            agent_eval_interval = 25

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
            duration = 10

            [[operations_definition]]
            id = 1
            name = "Widget routing"
            steps = [1]

            [economy]
            initial_price = 2.0
            base_demand = 8.0
            price_elasticity = 0.5
            lead_time_sensitivity = 0.0

            [agent]
            enabled = true
            agent_type = "sales"
            """;

    private static IntegratedHandler buildHandler(String toml, SalesAgent agent, boolean agentEnabled) {
        ScenarioConfig config = ScenarioLoader.loadScenario(toml);

        MachineStore machines = new MachineStore();
        for (EquipmentConfig eq : config.equipment()) {
            machines.add(new Machine(
                    new MachineId(eq.id()),
                    eq.name(),
                    eq.effectiveConcurrency(),
                    eq.capacityLiters(),
                    eq.effectiveSetupTime()));
        }

        RoutingStore routings = new RoutingStore();
        for (OperationsDefinitionConfig od : config.operationsDefinition()) {
            List<RoutingStep> steps = new ArrayList<>();
            for (Long segId : od.steps()) {
                config.processSegment().stream()
                        .filter(s -> s.id() == segId)
                        .findFirst()
                        .ifPresent(s -> steps.add(new RoutingStep(
                                s.id(), s.name(), new MachineId(s.equipmentId()), s.duration())));
            }
            routings.addRouting(new Routing(od.id(), od.name(), steps));
        }

        List<ProductId> productIds =
                config.material().stream().map(m -> new ProductId(m.id())).toList();
        for (MaterialConfig mat : config.material()) {
            routings.addProductRouting(new ProductId(mat.id()), mat.routingId());
        }

        FactoryHandler factory = new FactoryHandler(machines, routings, productIds);

        double initialPrice = config.economy().initialPrice();
        PricingState pricing = new PricingState(initialPrice);
        DemandModel demand = new DemandModel(
                config.economy().effectiveBaseDemand(),
                config.economy().effectivePriceElasticity(),
                config.economy().effectiveLeadTimeSensitivity(),
                pricing::offerPrice,
                factory::avgLeadTime,
                productIds,
                new Random(config.simulation().rngSeed()));

        return new IntegratedHandler(factory, demand, pricing, new FinanceHandler(), agent, agentEnabled);
    }

    private static SalesAgent overloadAgent() {
        return new SalesAgent(new SalesAgentConfig(5, 2, 0.15, 0.5, 50.0));
    }

    private static ScenarioConfig configOf(String toml) {
        return ScenarioLoader.loadScenario(toml);
    }

    @Test
    void agentProducesAtLeastOneIntervention() throws SimError {
        IntegratedHandler handler = buildHandler(OVERLOAD_TOML, overloadAgent(), true);
        SimResult result = SimRunner.runScenario(configOf(OVERLOAD_TOML), handler);

        assertTrue(handler.agent().interventions() > 0,
                "agent should have intervened at least once, got " + handler.agent().interventions());

        long agentDecisions = result.eventLog().filterByType(EventType.AgentDecision).count();
        assertTrue(agentDecisions > 0, "should have at least one AgentDecision event logged");
    }

    @Test
    void agentReducesBacklogVsFixedPriceBaseline() throws SimError {
        IntegratedHandler noAgent = buildHandler(OVERLOAD_TOML, overloadAgent(), false);
        SimRunner.runScenario(configOf(OVERLOAD_TOML), noAgent);
        long backlogNoAgent = noAgent.factory().backlog();

        IntegratedHandler withAgent = buildHandler(OVERLOAD_TOML, overloadAgent(), true);
        SimRunner.runScenario(configOf(OVERLOAD_TOML), withAgent);
        long backlogAgent = withAgent.factory().backlog();

        assertTrue(backlogAgent <= backlogNoAgent,
                "agent should reduce backlog: agent=" + backlogAgent + ", no_agent=" + backlogNoAgent);
    }

    @Test
    void agentPriceInterventionsNeverAlterAlreadyCreatedOrders() throws SimError {
        IntegratedHandler handler = buildHandler(OVERLOAD_TOML, overloadAgent(), true);
        SimResult result = SimRunner.runScenario(configOf(OVERLOAD_TOML), handler);

        List<EventPayload.OrderCreation> orders = result.eventLog().filterByType(EventType.OrderCreation)
                .map(e -> (EventPayload.OrderCreation) e.payload())
                .toList();

        assertTrue(orders.size() > 1, "need multiple orders to meaningfully exercise this invariant");
        assertTrue(handler.agent().interventions() > 0,
                "agent must actually change price during the run for this test to be meaningful");

        // JobStore assigns ids 1..N in the same order OrderCreation events are dispatched (now
        // deterministic thanks to Scheduler's same-tick FIFO ordering), so the nth OrderCreation
        // event corresponds to the job with id n.
        for (int i = 0; i < orders.size(); i++) {
            JobId jobId = new JobId(i + 1L);
            var job = handler.factory().job(jobId);
            assertEquals(
                    orders.get(i).unitPrice(),
                    job.unitPrice(),
                    "job " + jobId + "'s price must match the OfferPrice at its own OrderCreation -- "
                            + "an agent PriceChange issued after this order existed must never have altered it");
        }
    }

    @Test
    void agentDoesNotInterveneWhenDisabled() throws SimError {
        IntegratedHandler handler = buildHandler(OVERLOAD_TOML, overloadAgent(), false);
        SimResult result = SimRunner.runScenario(configOf(OVERLOAD_TOML), handler);

        assertEquals(0, handler.agent().interventions(), "disabled agent should not intervene");

        long agentDecisions = result.eventLog().filterByType(EventType.AgentDecision).count();
        assertEquals(0, agentDecisions, "no AgentDecision events when disabled");
    }
}
