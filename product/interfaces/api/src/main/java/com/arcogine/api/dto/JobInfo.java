package com.arcogine.api.dto;

import com.arcogine.types.JobStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {@code revenue} is a completed job's {@code orderValue()} (quantity x its own OrderPrice), not
 * the accounting concept of recognized revenue -- naming debt inherited from the external
 * {@code revenue} JSON field. Left as-is deliberately (API/JSON compatibility boundary, see
 * docs/architecture.md's "Domain observations vs. API/UI snapshots" and CONTRIBUTING.md guardrail
 * 10/11), but flagged here so it isn't mistaken for a resolved naming decision.
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
