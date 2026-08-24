package com.arcogine.types.scenario;

import java.util.List;

public record OperationsDefinitionConfig(
    long id,
    String name,
    List<Long> steps
) {}
