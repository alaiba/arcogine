package com.arcogine.agents;

public record AgentObservation(
        int backlog,
        double avgLeadTime,
        double completedSalesValue,
        long completedSales,
        double offerPrice,
        double throughput) {

    public static final AgentObservation DEFAULT = new AgentObservation(0, 0.0, 0.0, 0L, 0.0, 0.0);
}
