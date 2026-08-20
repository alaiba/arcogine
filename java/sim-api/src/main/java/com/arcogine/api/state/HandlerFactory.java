package com.arcogine.api.state;

import com.arcogine.agents.SalesAgent;
import com.arcogine.economy.demand.DemandModel;
import com.arcogine.economy.pricing.PricingState;
import com.arcogine.factory.machines.Machine;
import com.arcogine.factory.machines.MachineStore;
import com.arcogine.factory.process.FactoryHandler;
import com.arcogine.factory.routing.Routing;
import com.arcogine.factory.routing.RoutingStep;
import com.arcogine.factory.routing.RoutingStore;
import com.arcogine.finance.process.FinanceHandler;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import com.arcogine.types.scenario.EquipmentConfig;
import com.arcogine.types.scenario.MaterialConfig;
import com.arcogine.types.scenario.OperationsDefinitionConfig;
import com.arcogine.types.scenario.ScenarioConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class HandlerFactory {

    private HandlerFactory() {}

    public static IntegratedHandler buildFromConfig(ScenarioConfig config) {
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
                                s.id(),
                                s.name(),
                                new MachineId(s.equipmentId()),
                                s.duration())));
            }
            routings.addRouting(new Routing(od.id(), od.name(), steps));
        }

        List<ProductId> productIds =
                config.material().stream().map(m -> new ProductId(m.id())).toList();
        for (MaterialConfig mat : config.material()) {
            routings.addProductRouting(new ProductId(mat.id()), mat.routingId());
        }

        FactoryHandler factory = new FactoryHandler(machines, routings, productIds);

        double initialPrice = 10.0;
        double baseDemand = 5.0;
        double priceElasticity = 0.5;
        double leadTimeSensitivity = 0.1;
        if (config.economy() != null) {
            initialPrice = config.economy().initialPrice();
            baseDemand = config.economy().effectiveBaseDemand();
            priceElasticity = config.economy().effectivePriceElasticity();
            leadTimeSensitivity = config.economy().effectiveLeadTimeSensitivity();
        }

        Random rng = new Random(config.simulation().rngSeed());
        PricingState pricing = new PricingState(initialPrice);
        DemandModel demand = new DemandModel(
                baseDemand,
                priceElasticity,
                leadTimeSensitivity,
                pricing::currentPrice,
                factory::avgLeadTime,
                productIds,
                rng);

        boolean agentEnabled = config.agent() != null && config.agent().enabled();
        SalesAgent agent = SalesAgent.withDefaultConfig();
        FinanceHandler finance = new FinanceHandler();

        return new IntegratedHandler(factory, demand, pricing, finance, agent, agentEnabled);
    }
}
