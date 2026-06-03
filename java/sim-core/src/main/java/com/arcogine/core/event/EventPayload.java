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

    record OrderCreation(ProductId productId, long quantity) implements EventPayload {}

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
