package com.arcogine.api.state;

import com.arcogine.agents.SalesAgent;
import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.handler.EventHandler;
import com.arcogine.core.handler.ModelProvenanceSource;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.economy.demand.DemandModel;
import com.arcogine.economy.pricing.PricingState;
import com.arcogine.factory.process.FactoryHandler;
import com.arcogine.finance.process.FinanceHandler;
import com.arcogine.types.SimError;

public class IntegratedHandler implements EventHandler, ModelProvenanceSource {

    private final FactoryHandler factory;
    private final DemandModel demand;
    private final PricingState pricing;
    private final FinanceHandler finance;
    private final SalesAgent agent;
    private boolean agentEnabled;
    private final String factoryModelContentHash;

    public IntegratedHandler(
            FactoryHandler factory,
            DemandModel demand,
            PricingState pricing,
            FinanceHandler finance,
            SalesAgent agent,
            boolean agentEnabled) {
        this(factory, demand, pricing, finance, agent, agentEnabled, null);
    }

    /**
     * @param factoryModelContentHash provenance identifying the {@code FactoryModelVersion} this
     *     handler's factory runtime was instantiated from, or {@code null} when the factory
     *     runtime was not constructed through the canonical model boundary (e.g. hand-built in a
     *     test).
     */
    public IntegratedHandler(
            FactoryHandler factory,
            DemandModel demand,
            PricingState pricing,
            FinanceHandler finance,
            SalesAgent agent,
            boolean agentEnabled,
            String factoryModelContentHash) {
        this.factory = factory;
        this.demand = demand;
        this.pricing = pricing;
        this.finance = finance;
        this.agent = agent;
        this.agentEnabled = agentEnabled;
        this.factoryModelContentHash = factoryModelContentHash;
    }

    @Override
    public void handleEvent(Event event, Scheduler scheduler) throws SimError {
        if (event.payload() instanceof EventPayload.AgentEnabledChanged aec) {
            this.agentEnabled = aec.enabled();
        }

        pricing.handleEvent(event, scheduler);
        demand.handleEvent(event, scheduler);

        factory.handleEvent(event, scheduler);
        finance.handleEvent(event, scheduler);

        if (event.payload() instanceof EventPayload.AgentEvaluation) {
            if (agentEnabled) {
                agent.observe(AgentObservationProjector.project(
                        factory, pricing, scheduler.currentTime().ticks()));
                agent.handleEvent(event, scheduler);
            }
        }
    }

    public FactoryHandler factory() {
        return factory;
    }

    public DemandModel demand() {
        return demand;
    }

    public PricingState pricing() {
        return pricing;
    }

    public FinanceHandler finance() {
        return finance;
    }

    public SalesAgent agent() {
        return agent;
    }

    public boolean agentEnabled() {
        return agentEnabled;
    }

    /**
     * Content hash of the {@code FactoryModelVersion} this handler's factory runtime was
     * instantiated from, or {@code null} if unknown.
     */
    public String factoryModelContentHash() {
        return factoryModelContentHash;
    }

    @Override
    public String modelContentHash() {
        return factoryModelContentHash;
    }
}
