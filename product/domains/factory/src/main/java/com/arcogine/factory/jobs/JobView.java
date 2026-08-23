package com.arcogine.factory.jobs;

import com.arcogine.types.JobId;
import com.arcogine.types.JobStatus;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimTime;
import java.util.Optional;

/**
 * Read-only view of a {@link Job} -- deliberately excludes {@code start(MachineId)} and
 * {@code completeStep(SimTime)}. Only {@code FactoryHandler} (via its own package-private
 * {@code JobStore}) drives a job's production lifecycle; everything else, including anything
 * obtaining a reference via {@code FactoryHandler.jobsView()}/{@code job(JobId)}, is structurally
 * limited to reading, not just conventionally expected to.
 */
public interface JobView {

    JobId id();

    ProductId productId();

    long quantity();

    JobStatus status();

    int currentStep();

    int totalSteps();

    MachineId currentMachine();

    SimTime createdAt();

    SimTime completedAt();

    Optional<Long> leadTime();

    boolean isComplete();

    /** The price agreed when this order was created. Immutable for the life of the order. */
    double unitPrice();

    /** OrderValue: quantity x the order's own agreed unit price. */
    double orderValue();
}
