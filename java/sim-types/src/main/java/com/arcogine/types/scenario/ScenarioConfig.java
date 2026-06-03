package com.arcogine.types.scenario;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ScenarioConfig(
    SimulationParams simulation,
    List<EquipmentConfig> equipment,
    List<MaterialConfig> material,
    @JsonProperty("process_segment") List<ProcessSegmentConfig> processSegment,
    @JsonProperty("operations_definition") List<OperationsDefinitionConfig> operationsDefinition,
    EconomyConfig economy,
    AgentConfig agent
) {
    public ScenarioConfig {
        if (equipment == null) equipment = List.of();
        if (material == null) material = List.of();
        if (processSegment == null) processSegment = List.of();
        if (operationsDefinition == null) operationsDefinition = List.of();
    }
}
