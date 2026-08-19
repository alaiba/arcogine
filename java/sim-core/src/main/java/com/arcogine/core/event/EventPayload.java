package com.arcogine.core.event;

import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;

public sealed interface EventPayload permits
        EventPayload.OrderCreation,
        EventPayload.TaskStart,
        EventPayload.TaskEnd,
        EventPayload.MachineAvailabilityChange,
        EventPayload.PriceChange,
        EventPayload.AgentDecision,
        EventPayload.DemandEvaluation,
        EventPayload.AgentEvaluation {

    /**
     * unitPrice is the MarketPrice in effect at the instant the order was created. It is a
     * historical transaction fact: once the order exists, this price is immutable for the life
     * of the order and must not be re-derived from current pricing state later.
     */
    record OrderCreation(ProductId productId, long quantity, double unitPrice) implements EventPayload {}

    record TaskStart(JobId jobId, MachineId machineId, int stepIndex) implements EventPayload {}

    record TaskEnd(JobId jobId, MachineId machineId, int stepIndex) implements EventPayload {}

    record MachineAvailabilityChange(MachineId machineId, boolean online) implements EventPayload {}

    record PriceChange(double newPrice) implements EventPayload {}

    record AgentDecision(String description) implements EventPayload {}

    record DemandEvaluation() implements EventPayload {
        public static final DemandEvaluation INSTANCE = new DemandEvaluation();
    }

    record AgentEvaluation() implements EventPayload {
        public static final AgentEvaluation INSTANCE = new AgentEvaluation();
    }
}
