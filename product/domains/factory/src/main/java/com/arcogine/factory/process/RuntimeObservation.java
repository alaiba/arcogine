package com.arcogine.factory.process;

import java.util.List;

/**
 * Consumer-neutral immutable snapshot of authoritative runtime state.
 *
 * <p>The cursor is the latest supported runtime-event sequence included in this observation. It is zero only before
 * any supported runtime event has been published in the run. Internal scheduler events are deliberately absent from
 * this contract.
 */
public record RuntimeObservation(
        RuntimeObservationMetadata metadata,
        List<ResourceObservation> resources,
        List<OrderObservation> orders,
        List<JobObservation> jobs,
        List<PendingWorkObservation> pendingWork,
        RuntimePerformanceObservation performance) {

    public RuntimeObservation {
        if (metadata == null || performance == null) {
            throw new NullPointerException("runtime observation metadata and performance must not be null");
        }
        resources = List.copyOf(resources);
        orders = List.copyOf(orders);
        jobs = List.copyOf(jobs);
        pendingWork = List.copyOf(pendingWork);
    }
}
