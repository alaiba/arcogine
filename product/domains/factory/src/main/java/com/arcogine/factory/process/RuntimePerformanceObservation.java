package com.arcogine.factory.process;

/** Authoritative factory performance aggregates at one observation boundary. */
public record RuntimePerformanceObservation(
        long backlog,
        long completedOrders,
        double completedSalesValue,
        double averageLeadTime,
        double throughputPerTick) {}
