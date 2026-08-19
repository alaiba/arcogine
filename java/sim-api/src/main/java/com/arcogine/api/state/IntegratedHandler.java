package com.arcogine.api.state;

import com.arcogine.agents.AgentObservation;
import com.arcogine.agents.SalesAgent;
import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.handler.EventHandler;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.economy.demand.DemandModel;
import com.arcogine.economy.pricing.PricingState;
import com.arcogine.factory.process.FactoryHandler;
import com.arcogine.types.SimError;

public class IntegratedHandler implements EventHandler {

    private final FactoryHandler factory;
    private final DemandModel demand;
    private final PricingState pricing;
    private final SalesAgent agent;
    private boolean agentEnabled;

    public IntegratedHandler(
            FactoryHandler factory,
            DemandModel demand,
            PricingState pricing,
            SalesAgent agent,
            boolean agentEnabled) {
        this.factory = factory;
        this.demand = demand;
        this.pricing = pricing;
        this.agent = agent;
        this.agentEnabled = agentEnabled;
    }

    @Override
    public void handleEvent(Event event, Scheduler scheduler) throws SimError {
        pricing.handleEvent(event, scheduler);
        demand.handleEvent(event, scheduler);

        factory.handleEvent(event, scheduler);

        if (event.payload() instanceof EventPayload.AgentEvaluation) {
            if (agentEnabled) {
                long elapsed = Math.max(1, scheduler.currentTime().ticks());
                agent.observe(new AgentObservation(
                        (int) factory.backlog(),
                        factory.avgLeadTime(),
                        factory.completedSalesValue,
                        factory.completedSales,
                        pricing.currentPrice(),
                        factory.throughput(elapsed)));
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

    public SalesAgent agent() {
        return agent;
    }

    public boolean agentEnabled() {
        return agentEnabled;
    }

    public void setAgentEnabled(boolean agentEnabled) {
        this.agentEnabled = agentEnabled;
    }
}
