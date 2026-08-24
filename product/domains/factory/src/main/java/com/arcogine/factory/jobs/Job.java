package com.arcogine.factory.jobs;

import com.arcogine.types.JobId;
import com.arcogine.types.JobStatus;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import java.util.Optional;

public class Job implements JobView {

    private final JobId id;
    private final ProductId productId;
    private final long quantity;
    private JobStatus status;
    private int currentStep;
    private final int totalSteps;
    private MachineId currentMachine;
    private final SimTime createdAt;
    private SimTime completedAt;
    private final double unitPrice;

    public Job(
            JobId id,
            ProductId productId,
            long quantity,
            int totalSteps,
            SimTime createdAt,
            double unitPrice) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.status = JobStatus.Queued;
        this.currentStep = 0;
        this.totalSteps = totalSteps;
        this.currentMachine = null;
        this.createdAt = createdAt;
        this.completedAt = null;
        this.unitPrice = unitPrice;
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

    @Override
    public Optional<Long> leadTime() {
        if (completedAt == null) {
            return Optional.empty();
        }
        return Optional.of(completedAt.minus(createdAt));
    }

    @Override
    public boolean isComplete() {
        return status == JobStatus.Completed;
    }

    @Override
    public JobId id() {
        return id;
    }

    @Override
    public ProductId productId() {
        return productId;
    }

    @Override
    public long quantity() {
        return quantity;
    }

    @Override
    public JobStatus status() {
        return status;
    }

    @Override
    public int currentStep() {
        return currentStep;
    }

    @Override
    public int totalSteps() {
        return totalSteps;
    }

    @Override
    public MachineId currentMachine() {
        return currentMachine;
    }

    @Override
    public SimTime createdAt() {
        return createdAt;
    }

    @Override
    public SimTime completedAt() {
        return completedAt;
    }

    /** The price agreed when this order was created. Immutable for the life of the order. */
    @Override
    public double unitPrice() {
        return unitPrice;
    }

    /** OrderValue: quantity x the order's own agreed unit price. */
    @Override
    public double orderValue() {
        return quantity * unitPrice;
    }
}
