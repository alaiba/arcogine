package com.arcogine.api.dto;

import com.arcogine.types.JobStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

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
