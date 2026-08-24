package com.arcogine.api.state;

import com.arcogine.agents.AgentObservation;
import com.arcogine.economy.pricing.PricingState;
import com.arcogine.factory.process.FactoryHandler;

/**
 * Builds an {@link AgentObservation} from the current state of {@code FactoryHandler} and
 * {@code PricingState}. Lives in sim-api (not colocated with {@code AgentObservation} in
 * sim-agents) because sim-agents deliberately has no compile-time dependency on
 * sim-factory/sim-economy internals -- only {@code IntegratedHandler} sits at the intersection
 * of all three domains.
 */
final class AgentObservationProjector {

    private AgentObservationProjector() {}

    static AgentObservation project(FactoryHandler factory, PricingState pricing, long elapsedTicks) {
        long safeElapsed = Math.max(1, elapsedTicks);
        return new AgentObservation(
                (int) factory.backlog(),
                factory.avgLeadTime(),
                factory.completedSalesValue(),
                factory.completedSales(),
                pricing.offerPrice(),
                factory.throughput(safeElapsed));
    }
}
