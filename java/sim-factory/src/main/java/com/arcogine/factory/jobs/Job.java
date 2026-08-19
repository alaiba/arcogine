package com.arcogine.factory.jobs;

import com.arcogine.types.JobId;
import com.arcogine.types.JobStatus;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import java.util.Optional;

public class Job {

    private final JobId id;
    private final ProductId productId;
    private final long quantity;
    private JobStatus status;
    private int currentStep;
    private final int totalSteps;
    private MachineId currentMachine;
    private final SimTime createdAt;
    private SimTime completedAt;
    private Double revenue;

    public Job(JobId id, ProductId productId, long quantity, int totalSteps, SimTime createdAt) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.status = JobStatus.Queued;
        this.currentStep = 0;
        this.totalSteps = totalSteps;
        this.currentMachine = null;
        this.createdAt = createdAt;
        this.completedAt = null;
        this.revenue = null;
    }

    public void start(MachineId machineId) {
        if (status != JobStatus.Queued && status != JobStatus.InProgress) {
            throw new SimError.InvalidStateTransition(
                    "cannot start job " + id + " in state " + status);
        }
        status = JobStatus.InProgress;
        currentMachine = machineId;
    }

    public void completeStep(SimTime time) {
        if (status != JobStatus.InProgress) {
            throw new SimError.InvalidStateTransition(
                    "cannot complete step for job " + id + " in state " + status);
        }
        currentMachine = null;
        currentStep += 1;

        if (currentStep >= totalSteps) {
            status = JobStatus.Completed;
            completedAt = time;
        } else {
            status = JobStatus.Queued;
        }
    }

    public void recordRevenue(double revenue) {
        if (status != JobStatus.Completed) {
            throw new SimError.InvalidStateTransition(
                    "cannot record revenue for job " + id + " in state " + status);
        }
        this.revenue = revenue;
    }

    public Optional<Long> leadTime() {
        if (completedAt == null) {
            return Optional.empty();
        }
        return Optional.of(completedAt.minus(createdAt));
    }

    public boolean isComplete() {
        return status == JobStatus.Completed;
    }

    public JobId id() {
        return id;
    }

    public ProductId productId() {
        return productId;
    }

    public long quantity() {
        return quantity;
    }

    public JobStatus status() {
        return status;
    }

    public int currentStep() {
        return currentStep;
    }

    public int totalSteps() {
        return totalSteps;
    }

    public MachineId currentMachine() {
        return currentMachine;
    }

    public SimTime createdAt() {
        return createdAt;
    }

    public SimTime completedAt() {
        return completedAt;
    }

    /** Revenue recognized for this job, fixed at the price in effect when it completed. */
    public Double revenue() {
        return revenue;
    }
}
