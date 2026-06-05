package com.arcogine.factory.routing;

import com.arcogine.types.MachineId;

public record RoutingStep(long stepId, String name, MachineId machineId, long duration) {}
