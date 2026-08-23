package com.arcogine.types.scenario;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SimulationParams(
    @JsonProperty("rng_seed") long rngSeed,
    @JsonProperty("max_ticks") long maxTicks,
    @JsonProperty("demand_eval_interval") Long demandEvalInterval,
    @JsonProperty("agent_eval_interval") Long agentEvalInterval
) {
    public static final long DEFAULT_DEMAND_INTERVAL = 10;
    public static final long DEFAULT_AGENT_INTERVAL = 50;

    public long demandInterval() {
        return demandEvalInterval != null ? demandEvalInterval : DEFAULT_DEMAND_INTERVAL;
    }

    public long agentInterval() {
        return agentEvalInterval != null ? agentEvalInterval : DEFAULT_AGENT_INTERVAL;
    }
}
