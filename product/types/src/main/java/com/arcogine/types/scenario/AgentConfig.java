package com.arcogine.types.scenario;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AgentConfig(
    boolean enabled,
    @JsonProperty("agent_type") String agentType
) {
    public static final String DEFAULT_AGENT_TYPE = "sales";

    public String agentType() {
        return agentType != null ? agentType : DEFAULT_AGENT_TYPE;
    }
}
