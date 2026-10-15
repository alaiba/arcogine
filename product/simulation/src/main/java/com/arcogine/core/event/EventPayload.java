package com.arcogine.core.event;

import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;

public sealed interface EventPayload permits
        EventPayload.OrderCreation,
        EventPayload.TaskEnd,
        EventPayload.MachineAvailabilityChange {

    /**
     * unitPrice is the commercial price agreed when the order was created. It is a historical
     * transaction fact: once the order exists, this price is immutable for the life of the order.
     */
    record OrderCreation(ProductId productId, long quantity, double unitPrice) implements EventPayload {}

    record TaskEnd(JobId jobId, MachineId machineId, int stepIndex) implements EventPayload {}

    record MachineAvailabilityChange(MachineId machineId, boolean online) implements EventPayload {}

}
