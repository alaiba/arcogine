package com.arcogine.core.event;

import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;

public sealed interface EventPayload permits
        EventPayload.OrderCreation,
        EventPayload.TaskStart,
        EventPayload.TaskEnd,
        EventPayload.OrderCompleted,
        EventPayload.MachineAvailabilityChange {

    /**
     * unitPrice is the commercial price agreed when the order was created. It is a historical
     * transaction fact: once the order exists, this price is immutable for the life of the order.
     */
    record OrderCreation(ProductId productId, long quantity, double unitPrice) implements EventPayload {}

    record TaskStart(JobId jobId, MachineId machineId, int stepIndex) implements EventPayload {}

    record TaskEnd(JobId jobId, MachineId machineId, int stepIndex) implements EventPayload {}

    /**
     * The operational fact that an accepted order fulfilled its full execution aggregate --
     * distinct from a single {@link TaskEnd}, which only means one production step finished.
     * Carries the authoritative {@code orderId}, the completing child {@code jobId} retained for
     * work-item correlation, and the immutable commercial facts a downstream consumer (e.g.
     * Finance) needs to interpret the transaction. The derived orderValue (quantity x unitPrice)
     * is deliberately omitted to avoid a second consistency invariant for a value that's trivially
     * recomputed.
     */
    record OrderCompleted(OrderId orderId, JobId jobId, ProductId productId, long quantity, double unitPrice)
            implements EventPayload {
        /** Source-compatible construction for historical fixtures; production always supplies OrderId. */
        public OrderCompleted(JobId jobId, ProductId productId, long quantity, double unitPrice) {
            this(new OrderId(jobId.value()), jobId, productId, quantity, unitPrice);
        }
    }

    record MachineAvailabilityChange(MachineId machineId, boolean online) implements EventPayload {}

}
