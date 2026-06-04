package com.arcogine.cli;

import com.arcogine.core.event.Event;
import com.arcogine.core.handler.EventHandler;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.economy.demand.DemandModel;
import com.arcogine.economy.pricing.PricingState;
import com.arcogine.factory.machines.Machine;
import com.arcogine.factory.machines.MachineStore;
import com.arcogine.factory.process.FactoryHandler;
import com.arcogine.factory.routing.Routing;
import com.arcogine.factory.routing.RoutingStep;
import com.arcogine.factory.routing.RoutingStore;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimError;
import com.arcogine.types.scenario.EconomyConfig;
import com.arcogine.types.scenario.EquipmentConfig;
import com.arcogine.types.scenario.MaterialConfig;
import com.arcogine.types.scenario.OperationsDefinitionConfig;
import com.arcogine.types.scenario.ProcessSegmentConfig;
import com.arcogine.types.scenario.ScenarioConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HeadlessHandler implements EventHandler {

    public final FactoryHandler factory;
    private final PricingState pricing;
    private final DemandModel demand;

    private HeadlessHandler(FactoryHandler factory, PricingState pricing, DemandModel demand) {
        this.factory = factory;
        this.pricing = pricing;
        this.demand = demand;
    }

    public double currentPrice() {
        return pricing.currentPrice();
    }

    public static HeadlessHandler fromConfig(ScenarioConfig config) {
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

        List<ProductId> productIds = config.material().stream()
                .map(m -> new ProductId(m.id()))
                .toList();
        for (MaterialConfig mat : config.material()) {
            routings.addProductRouting(new ProductId(mat.id()), mat.routingId());
        }

        FactoryHandler factory = new FactoryHandler(machines, routings, productIds);

        EconomyConfig econ = config.economy();
        double initialPrice = econ != null ? econ.initialPrice() : 10.0;
        double baseDemand = econ != null
                ? econ.effectiveBaseDemand()
                : EconomyConfig.DEFAULT_BASE_DEMAND;
        double priceElasticity = econ != null
                ? econ.effectivePriceElasticity()
                : EconomyConfig.DEFAULT_PRICE_ELASTICITY;
        double leadTimeSensitivity = econ != null
                ? econ.effectiveLeadTimeSensitivity()
                : EconomyConfig.DEFAULT_LEAD_TIME_SENSITIVITY;

        Random rng = new Random(config.simulation().rngSeed());
        DemandModel demand = new DemandModel(
                baseDemand,
                priceElasticity,
                leadTimeSensitivity,
                initialPrice,
                productIds,
                rng);
        PricingState pricing = new PricingState(initialPrice);

        return new HeadlessHandler(factory, pricing, demand);
    }

    @Override
    public void handleEvent(Event event, Scheduler scheduler) throws SimError {
        pricing.handleEvent(event, scheduler);
        demand.setPrice(pricing.currentPrice());
        demand.setAvgLeadTime(factory.avgLeadTime());
        demand.handleEvent(event, scheduler);
        factory.setCurrentPrice(pricing.currentPrice());
        factory.handleEvent(event, scheduler);
    }
}
