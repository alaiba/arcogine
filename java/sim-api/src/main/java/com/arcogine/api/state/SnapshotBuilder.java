package com.arcogine.api.state;

import com.arcogine.api.dto.JobInfo;
import com.arcogine.api.dto.MachineInfo;
import com.arcogine.api.dto.RoutingEdge;
import com.arcogine.api.dto.SimSnapshot;
import com.arcogine.api.dto.TopologySnapshot;
import com.arcogine.core.kpi.EventCount;
import com.arcogine.core.kpi.KpiValue;
import com.arcogine.core.kpi.OrderCount;
import com.arcogine.core.kpi.ThroughputRate;
import com.arcogine.core.kpi.TotalSimulatedTime;
import com.arcogine.core.log.EventLog;
import com.arcogine.types.JobStatus;
import com.arcogine.types.SimTime;
import com.arcogine.types.scenario.OperationsDefinitionConfig;
import com.arcogine.types.scenario.ProcessSegmentConfig;
import com.arcogine.types.scenario.ScenarioConfig;
import java.util.ArrayList;
import java.util.List;

public final class SnapshotBuilder {

    private static final TotalSimulatedTime TOTAL_SIMULATED_TIME = new TotalSimulatedTime();
    private static final EventCount EVENT_COUNT = new EventCount();
    private static final ThroughputRate THROUGHPUT_RATE = new ThroughputRate();
    private static final OrderCount ORDER_COUNT = new OrderCount();

    private SnapshotBuilder() {}

    public static SimSnapshot buildSnapshot(
            IntegratedHandler handler,
            EventLog eventLog,
            SimRunState runState,
            SimTime currentTime,
            long eventsProcessed,
            ScenarioConfig config,
            String lastError) {
        List<KpiValue> kpis = List.of(
                TOTAL_SIMULATED_TIME.compute(eventLog, currentTime),
                EVENT_COUNT.compute(eventLog, currentTime),
                THROUGHPUT_RATE.compute(eventLog, currentTime),
                ORDER_COUNT.compute(eventLog, currentTime));

        List<MachineInfo> machines = handler.factory().machines.machines().stream()
                .map(m -> new MachineInfo(
                        m.id().value(),
                        m.name(),
                        m.state(),
                        m.queueDepth(),
                        m.activeJobs().size()))
                .toList();

        List<RoutingEdge> edges = new ArrayList<>();
        if (config != null) {
            for (OperationsDefinitionConfig od : config.operationsDefinition()) {
                List<Long> stepMachineIds = new ArrayList<>();
                for (Long segId : od.steps()) {
                    config.processSegment().stream()
                            .filter(s -> s.id() == segId)
                            .map(ProcessSegmentConfig::equipmentId)
                            .findFirst()
                            .ifPresent(stepMachineIds::add);
                }
                for (int i = 0; i + 1 < stepMachineIds.size(); i++) {
                    edges.add(new RoutingEdge(stepMachineIds.get(i), stepMachineIds.get(i + 1), od.name()));
                }
            }
        }

        List<JobInfo> jobs = handler.factory().jobs.allJobs()
                .map(j -> {
                    Double revenue = j.status() == JobStatus.Completed
                            ? handler.pricing().currentPrice() * j.quantity()
                            : null;
                    Long completedAt = j.completedAt() != null ? j.completedAt().ticks() : null;
                    return new JobInfo(
                            j.id().value(),
                            j.productId().value(),
                            j.quantity(),
                            j.status(),
                            j.currentStep(),
                            j.totalSteps(),
                            j.createdAt().ticks(),
                            completedAt,
                            revenue);
                })
                .toList();

        return new SimSnapshot(
                runState,
                currentTime.ticks(),
                eventsProcessed,
                kpis,
                new TopologySnapshot(machines, edges),
                jobs,
                handler.factory().totalRevenue,
                handler.factory().completedSales,
                handler.factory().backlog(),
                handler.pricing().currentPrice(),
                handler.agentEnabled(),
                config != null,
                lastError);
    }
}
