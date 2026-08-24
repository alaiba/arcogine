package com.arcogine.api.dto;

import java.util.List;

public record TopologySnapshot(List<MachineInfo> machines, List<RoutingEdge> edges) {}
