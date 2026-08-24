package com.arcogine.api.dto;

import com.arcogine.api.state.SimRunState;
import com.arcogine.core.kpi.KpiValue;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SimSnapshot(
        @JsonProperty("run_state") SimRunState runState,
        @JsonProperty("current_time") long currentTime,
        @JsonProperty("events_processed") long eventsProcessed,
        List<KpiValue> kpis,
        TopologySnapshot topology,
        List<JobInfo> jobs,
        @JsonProperty("total_revenue") double totalRevenue,
        @JsonProperty("completed_sales") long completedSales,
        long backlog,
        @JsonProperty("current_price") double currentPrice,
        @JsonProperty("agent_enabled") boolean agentEnabled,
        @JsonProperty("scenario_loaded") boolean scenarioLoaded,
        @JsonProperty("last_error") String lastError) {

    public static SimSnapshot empty() {
        return new SimSnapshot(
                SimRunState.Idle,
                0,
                0,
                List.of(),
                new TopologySnapshot(List.of(), List.of()),
                List.of(),
                0.0,
                0,
                0,
                0.0,
                false,
                false,
                null);
    }
}
