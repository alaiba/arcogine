package com.arcogine.agents;

public record SalesAgentConfig(
        int backlogHigh,
        int backlogLow,
        double adjustmentPct,
        double minPrice,
        double maxPrice) {

    public static final SalesAgentConfig DEFAULT = new SalesAgentConfig(10, 3, 0.10, 0.5, 100.0);
}
