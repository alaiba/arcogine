package com.arcogine.agents;

public record AgentObservation(
        int backlog,
        double avgLeadTime,
        double totalRevenue,
        long completedSales,
        double currentPrice,
        double throughput) {

    public static final AgentObservation DEFAULT = new AgentObservation(0, 0.0, 0.0, 0L, 0.0, 0.0);
}
