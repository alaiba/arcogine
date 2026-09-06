package com.arcogine.factory.process;

import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;
import java.util.List;
import java.util.Set;

/**
 * The supported, consumer-neutral payload shape for a {@link RuntimeEventEnvelope}. Distinct from
 * the internal scheduler's {@code EventPayload}: a supported payload is derived from the resulting
 * authoritative state after a transition has already succeeded, not a copy of internal scheduler
 * event data. See {@link RuntimeEventType} for the taxonomy each variant belongs to.
 */
public sealed interface RuntimeEventPayload {

    /**
     * {@link RuntimeEventType#ORDER_ACCEPTED}: a new order was accepted and its jobs created.
     * {@code jobIds} carries every created child job's identity, in ordinal order, so a consumer
     * can correlate the {@link #JobDispatched}/{@link #JobWaiting} events that immediately follow
     * this one back to the order that produced them (ADR-0011) without needing an
     * out-of-band job listing.
     */
    record OrderAccepted(OrderId orderId, ProductId productId, long quantity, double unitPrice, List<JobId> jobIds)
            implements RuntimeEventPayload {
        public OrderAccepted {
            jobIds = List.copyOf(jobIds);
        }
    }

    /**
     * {@link RuntimeEventType#JOB_DISPATCHED}: {@code jobId} (child of {@code orderId}) was
     * dispatched to {@code machineId} for the step at {@code stepIndex}.
     */
    record JobDispatched(JobId jobId, OrderId orderId, MachineId machineId, int stepIndex)
            implements RuntimeEventPayload {}

    /**
     * {@link RuntimeEventType#JOB_WAITING}: {@code jobId} (child of {@code orderId}) is not yet
     * dispatched; {@code eligibleMachines} is every machine that could pick it up next -- a
     * singleton when only one machine is eligible for the current step (job waits in that
     * machine's own queue), or several when it is waiting in the cross-machine multi-eligible
     * backlog ({@link PendingWorkView}).
     */
    record JobWaiting(JobId jobId, OrderId orderId, Set<MachineId> eligibleMachines)
            implements RuntimeEventPayload {
        public JobWaiting {
            eligibleMachines = Set.copyOf(eligibleMachines);
        }
    }

    /**
     * {@link RuntimeEventType#JOB_STEP_COMPLETED}: {@code jobId} (with parent {@code orderId}
     * retained for cross-cutting correlation, ADR-0010) finished the step at {@code stepIndex} on {@code
     * machineId}. {@code jobComplete} reports whether that step was the job's last.
     */
    record JobStepCompleted(
            JobId jobId, OrderId orderId, MachineId machineId, int stepIndex, boolean jobComplete)
            implements RuntimeEventPayload {}

    /**
     * {@link RuntimeEventType#ORDER_COMPLETED}: the operational fact that {@code orderId} fulfilled
     * its full execution aggregate, with the completing child {@code jobId} retained for cross-cutting
     * work-item correlation (ADR-0010) alongside the commercial facts a downstream consumer needs.
     */
    record OrderCompleted(OrderId orderId, JobId jobId, ProductId productId, long quantity, double unitPrice)
            implements RuntimeEventPayload {}

    /** {@link RuntimeEventType#MACHINE_AVAILABILITY_CHANGED}: a machine's availability changed. */
    record MachineAvailabilityChanged(MachineId machineId, boolean online) implements RuntimeEventPayload {}
}
