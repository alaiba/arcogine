package com.arcogine.api.dto;

import com.arcogine.types.MachineState;
import com.fasterxml.jackson.annotation.JsonProperty;

public record MachineInfo(
        long id,
        String name,
        MachineState state,
        @JsonProperty("queue_depth") int queueDepth,
        @JsonProperty("active_jobs") int activeJobs) {}
