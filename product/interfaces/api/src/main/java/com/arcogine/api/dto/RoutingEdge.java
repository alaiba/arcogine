package com.arcogine.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RoutingEdge(
        @JsonProperty("from_machine_id") long fromMachineId,
        @JsonProperty("to_machine_id") long toMachineId,
        @JsonProperty("routing_name") String routingName) {}
