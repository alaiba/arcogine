package com.arcogine.factory.process;

import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;

/**
 * The supported, consumer-neutral payload shape for a {@link RuntimeEventEnvelope}. Distinct from
 * the internal scheduler's {@code EventPayload}: a supported payload is derived from the resulting
 * authoritative state after a transition has already succeeded, not a copy of internal scheduler
 * event data. See {@link RuntimeEventType} for the taxonomy each variant belongs to.
 */
public sealed interface RuntimeEventPayload {

    /** {@link RuntimeEventType#ORDER_ACCEPTED}: a new order was accepted and its jobs created. */
    record OrderAccepted(OrderId orderId, ProductId productId, long quantity, double unitPrice)
            implements RuntimeEventPayload {}

    /**
     * {@link RuntimeEventType#JOB_STEP_COMPLETED}: {@code jobId} (with parent {@code orderId}
     * retained for W1 correlation, ADR-0010) finished the step at {@code stepIndex} on {@code
     * machineId}. {@code jobComplete} reports whether that step was the job's last.
     */
    record JobStepCompleted(
            JobId jobId, OrderId orderId, MachineId machineId, int stepIndex, boolean jobComplete)
            implements RuntimeEventPayload {}

    /**
     * {@link RuntimeEventType#ORDER_COMPLETED}: the operational fact that {@code orderId} fulfilled
     * its full execution aggregate, with the completing child {@code jobId} retained for W1
     * work-item correlation (ADR-0010) alongside the commercial facts a downstream consumer needs.
     */
    record OrderCompleted(OrderId orderId, JobId jobId, ProductId productId, long quantity, double unitPrice)
            implements RuntimeEventPayload {}

    /** {@link RuntimeEventType#MACHINE_AVAILABILITY_CHANGED}: a machine's availability changed. */
    record MachineAvailabilityChanged(MachineId machineId, boolean online) implements RuntimeEventPayload {}
}
