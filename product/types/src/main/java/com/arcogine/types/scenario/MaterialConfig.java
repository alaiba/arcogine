package com.arcogine.types.scenario;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MaterialConfig(
    long id,
    String name,
    @JsonProperty("routing_id") long routingId
) {}
