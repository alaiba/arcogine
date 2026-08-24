package com.arcogine.api.state;

import com.arcogine.agents.SalesAgent;
import com.arcogine.economy.demand.DemandModel;
import com.arcogine.economy.pricing.PricingState;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.model.FactoryRuntimeAssembler;
import com.arcogine.factory.model.scenario.ScenarioFactoryModelAdapter;
import com.arcogine.factory.process.FactoryHandler;
import com.arcogine.finance.process.FinanceHandler;
import com.arcogine.types.ProductId;
import com.arcogine.types.scenario.ScenarioConfig;
import java.util.List;
import java.util.Random;

public final class HandlerFactory {

    private HandlerFactory() {}

    public static IntegratedHandler buildFromConfig(ScenarioConfig config) {
        FactoryModel model = ScenarioFactoryModelAdapter.adapt(config);
        FactoryModelVersion modelVersion = FactoryModelPublisher.publish(model);
        FactoryRuntimeAssembler.Assembled assembled = FactoryRuntimeAssembler.assemble(modelVersion);
        FactoryHandler factory = assembled.factory();
        List<ProductId> productIds = assembled.productIds();

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
                pricing::offerPrice,
                factory::avgLeadTime,
                productIds,
                rng);

        boolean agentEnabled = config.agent() != null && config.agent().enabled();
        SalesAgent agent = SalesAgent.withDefaultConfig();
        FinanceHandler finance = new FinanceHandler();

        return new IntegratedHandler(
                factory, demand, pricing, finance, agent, agentEnabled, modelVersion.contentHash());
    }
}
