package com.arcogine.api.dto;

import com.arcogine.types.JobStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {@code revenue} is a completed child job's unit execution value (its immutable accepted unit
 * price), not recognized revenue. The legacy JSON field name remains for wire compatibility;
 * consumers must use snapshot-level completed-sales KPIs for commercial revenue.
 */
public record JobInfo(
        @JsonProperty("job_id") long jobId,
        @JsonProperty("product_id") long productId,
        long quantity,
        JobStatus status,
        @JsonProperty("current_step") int currentStep,
        @JsonProperty("total_steps") int totalSteps,
        @JsonProperty("created_at") long createdAt,
        @JsonProperty("completed_at") Long completedAt,
        Double revenue) {}
