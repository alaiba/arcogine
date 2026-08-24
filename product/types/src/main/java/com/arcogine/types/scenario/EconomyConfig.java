package com.arcogine.types.scenario;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EconomyConfig(
    @JsonProperty("initial_price") double initialPrice,
    @JsonProperty("base_demand") Double baseDemand,
    @JsonProperty("price_elasticity") Double priceElasticity,
    @JsonProperty("lead_time_sensitivity") Double leadTimeSensitivity
) {
    public static final double DEFAULT_BASE_DEMAND = 5.0;
    public static final double DEFAULT_PRICE_ELASTICITY = 0.5;
    public static final double DEFAULT_LEAD_TIME_SENSITIVITY = 0.1;

    public double effectiveBaseDemand() {
        return baseDemand != null ? baseDemand : DEFAULT_BASE_DEMAND;
    }

    public double effectivePriceElasticity() {
        return priceElasticity != null ? priceElasticity : DEFAULT_PRICE_ELASTICITY;
    }

    public double effectiveLeadTimeSensitivity() {
        return leadTimeSensitivity != null ? leadTimeSensitivity : DEFAULT_LEAD_TIME_SENSITIVITY;
    }
}
