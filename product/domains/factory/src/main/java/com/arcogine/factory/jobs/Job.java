package com.arcogine.factory.jobs;

import com.arcogine.factory.orders.Order;
import com.arcogine.types.JobId;
import com.arcogine.types.JobStatus;
import com.arcogine.types.MachineId;
import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import java.util.Optional;

/** Mutable production execution state for one accepted {@link Order}. */
public class Job implements JobView {

    private final JobId id;
    private final Order order;
    private final long ordinalWithinOrder;
    private JobStatus status;
    private int currentStep;
    private final int totalSteps;
    private MachineId currentMachine;
    private final SimTime createdAt;
    private SimTime completedAt;

    public Job(JobId id, Order order, long ordinalWithinOrder, int totalSteps, SimTime createdAt) {
        this.id = id;
        this.order = order;
        this.ordinalWithinOrder = ordinalWithinOrder;
        this.status = JobStatus.Queued;
        this.currentStep = 0;
        this.totalSteps = totalSteps;
        this.currentMachine = null;
        this.createdAt = createdAt;
        this.completedAt = null;
    }

    /** Compatibility constructor for focused lifecycle tests; production supplies an ordinal. */
    public Job(JobId id, Order order, int totalSteps, SimTime createdAt) {
        this(id, order, 0, totalSteps, createdAt);
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
    public OrderId orderId() {
        return order.id();
    }

    @Override
    public long ordinalWithinOrder() { return ordinalWithinOrder; }

    /** Compatibility projection from immutable order intent; Job does not own this fact. */
    @Override
    public ProductId productId() {
        return order.productId();
    }

    /** Compatibility projection from immutable order intent; Job does not own this fact. */
    @Override
    public long quantity() {
        return 1;
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

    /** Compatibility projection from immutable order intent; Job does not own this fact. */
    @Override
    public double unitPrice() {
        return order.unitPrice();
    }

    /** Compatibility projection from immutable order intent; Job does not own this fact. */
    @Override
    public double orderValue() {
        return unitPrice();
    }
}
