package com.arcogine.core.event;

import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;

public sealed interface EventPayload permits
        EventPayload.OrderCreation,
        EventPayload.TaskStart,
        EventPayload.TaskEnd,
        EventPayload.OrderCompleted,
        EventPayload.MachineAvailabilityChange,
        EventPayload.PriceChange,
        EventPayload.AgentEnabledChanged,
        EventPayload.AgentDecision,
        EventPayload.DemandEvaluation,
        EventPayload.AgentEvaluation {

    /**
     * unitPrice is the OfferPrice in effect at the instant the order was created. It is a
     * historical transaction fact: once the order exists, this price is immutable for the life
     * of the order and must not be re-derived from current pricing state later.
     */
    record OrderCreation(ProductId productId, long quantity, double unitPrice) implements EventPayload {}

    record TaskStart(JobId jobId, MachineId machineId, int stepIndex) implements EventPayload {}

    record TaskEnd(JobId jobId, MachineId machineId, int stepIndex) implements EventPayload {}

    /**
     * The operational fact that an order fulfilled its full routing -- distinct from a single
     * {@link TaskEnd}, which only means one production step finished. Carries the immutable
     * commercial facts a downstream consumer (e.g. a future Finance domain) needs to interpret
     * the transaction; deliberately does not carry the derived orderValue (quantity x unitPrice)
     * to avoid a second consistency invariant for a value that's trivially recomputed.
     */
    record OrderCompleted(JobId orderId, ProductId productId, long quantity, double unitPrice)
            implements EventPayload {}

    record MachineAvailabilityChange(MachineId machineId, boolean online) implements EventPayload {}

    record PriceChange(double newPrice) implements EventPayload {}

    /**
     * Whether the SalesAgent is enabled. This is orchestration config, not a domain state
     * transition owned by any single domain handler -- it is modeled as an event (rather than a
     * direct setter call, as it once was) purely for consistency with every other simulation
     * command (PriceChange, MachineAvailabilityChange): it becomes part of the deterministic,
     * replayable event stream instead of being an out-of-band mutation.
     */
    record AgentEnabledChanged(boolean enabled) implements EventPayload {}

    record AgentDecision(String description) implements EventPayload {}

    record DemandEvaluation() implements EventPayload {
        public static final DemandEvaluation INSTANCE = new DemandEvaluation();
    }

    record AgentEvaluation() implements EventPayload {
        public static final AgentEvaluation INSTANCE = new AgentEvaluation();
    }
}
