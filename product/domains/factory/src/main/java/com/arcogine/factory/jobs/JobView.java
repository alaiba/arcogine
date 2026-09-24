package com.arcogine.factory.jobs;

import com.arcogine.types.JobId;
import com.arcogine.types.JobStatus;
import com.arcogine.types.MachineId;
import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimTime;
import java.util.Optional;

/**
 * Read-only view of a {@link Job} -- deliberately excludes {@code start(MachineId)} and
 * {@code completeStep(SimTime)}. Only {@code FactoryHandler} (via its own package-private
 * {@code JobStore}) drives a job's production lifecycle; everything else, including anything
 * obtaining a reference via {@code FactoryHandler.jobsView()}/{@code job(JobId)}, is structurally
 * limited to reading, not just conventionally expected to.
 *
 * <p>{@code productId()} projects the immutable accepted order referenced by this job; it is not
 * mutable job-owned state. Commercial facts such as price and order value belong to the order.
 */
public interface JobView {

    JobId id();

    OrderId orderId();

    long ordinalWithinOrder();

    ProductId productId();

    JobStatus status();

    int currentStep();

    int totalSteps();

    MachineId currentMachine();

    SimTime createdAt();

    SimTime completedAt();

    Optional<Long> leadTime();

    boolean isComplete();
}
