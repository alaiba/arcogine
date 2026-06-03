package com.arcogine.types.scenario;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcessSegmentConfig(
    long id,
    String name,
    @JsonProperty("equipment_id") long equipmentId,
    long duration
) {}
