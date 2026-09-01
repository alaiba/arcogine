package com.arcogine.factory.process;

import com.arcogine.types.JobId;
import com.arcogine.types.JobStatus;
import com.arcogine.types.MachineId;
import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimTime;

/** Immutable current projection of one independently dispatchable W1 child job. */
public record JobObservation(
        JobId jobId,
        OrderId orderId,
        long ordinalWithinOrder,
        ProductId productId,
        JobStatus status,
        int currentStep,
        int totalSteps,
        MachineId currentMachineId,
        SimTime createdAt,
        SimTime completedAt) {}
