package com.arcogine.types.scenario;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EquipmentConfig(
    long id,
    String name,
    Integer concurrency,
    @JsonProperty("capacity_liters") Double capacityLiters,
    @JsonProperty("setup_time") Long setupTime
) {
    public static final int DEFAULT_CONCURRENCY = 1;
    public static final long DEFAULT_SETUP_TIME = 0;

    public int effectiveConcurrency() {
        return concurrency != null ? concurrency : DEFAULT_CONCURRENCY;
    }

    public long effectiveSetupTime() {
        return setupTime != null ? setupTime : DEFAULT_SETUP_TIME;
    }
}
