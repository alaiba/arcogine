package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Result-equivalence fixture for shared flexible-backlog ranking: a representative workload mixing
 * local-only waiting, shared pending waiting, overlapping eligible sets, an offline machine and a
 * later recovery. The recorded resource-assignment trace (every task end with its time, job,
 * machine and routing step) is the observable result of the ranking and dispatch rules, so a
 * change in how the shared backlog is indexed must leave it byte-for-byte identical.
 */
class SharedBacklogEquivalenceTest {

    private static FactoryRuntime runtime() {
        List<ConfiguredResource> resources = List.of(
                new ConfiguredResource(new MachineId(1), "M1", 2, null, 0),
                new ConfiguredResource(new MachineId(2), "M2", 1, null, 0),
                new ConfiguredResource(new MachineId(3), "M3", 2, null, 0));
        List<OperationDefinition> operations = List.of(
                new OperationDefinition(1, "mixed", List.of(
                        new OperationStepDefinition(1, "flex-all", Set.of(new MachineId(1), new MachineId(2), new MachineId(3)), 3),
                        new OperationStepDefinition(2, "local", Set.of(new MachineId(2)), 2),
                        new OperationStepDefinition(3, "flex-pair", Set.of(new MachineId(1), new MachineId(3)), 4))),
                new OperationDefinition(2, "shared-only", List.of(
                        new OperationStepDefinition(1, "flex-pair", Set.of(new MachineId(2), new MachineId(3)), 5))));
        List<ProductDefinition> products = List.of(
                new ProductDefinition(new ProductId(1), "P1", 1),
                new ProductDefinition(new ProductId(2), "P2", 2));
        return FactoryRuntime.forModel(
                FactoryModelPublisher.publish(new FactoryModel(resources, operations, products)));
    }

    private record Trace(List<String> assignments, long makespan, double meanLeadTime) {}

    private static Trace trace() {
        FactoryRuntime runtime = runtime();
        runtime.setMachineAvailability(new MachineId(1), false).orElseThrow();
        runtime.submitWorkload(new ProductId(1), 7, 1.0).orElseThrow();
        runtime.submitWorkload(new ProductId(2), 6, 1.0).orElseThrow();
        runtime.submitWorkload(new ProductId(1), 4, 1.0).orElseThrow();

        List<String> assignments = new ArrayList<>();
        long makespan = 0;
        int events = 0;
        Event event;
        while ((event = runtime.advance().orElse(null)) != null) {
            if (events++ == 5) {
                runtime.setMachineAvailability(new MachineId(1), true).orElseThrow();
            }
            EventPayload.TaskEnd end = (EventPayload.TaskEnd) event.payload();
            assignments.add(event.time().value() + ":J" + end.jobId().value()
                    + "@M" + end.machineId().value() + "#" + end.stepIndex());
            makespan = event.time().value();
            runtime.drainSupportedEvents();
        }
        assertTrue(runtime.pendingWorkView().isEmpty());
        assertEquals(0, runtime.backlog());
        return new Trace(assignments, makespan, runtime.avgLeadTime());
    }

    @Test
    void mixedLocalAndSharedWaitingKeepsTheRecordedAssignmentTrace() {
        Trace first = trace();
        assertEquals(first, trace(), "identical explicit inputs must replay identically");

        assertEquals(EXPECTED_EVENT_COUNT, first.assignments().size());
        assertEquals(EXPECTED_TRACE_HASH, first.assignments().hashCode());
        assertEquals(EXPECTED_MAKESPAN, first.makespan());
        assertEquals(EXPECTED_MEAN_LEAD_TIME, first.meanLeadTime());
    }

    private static final int EXPECTED_EVENT_COUNT = 39;
    private static final int EXPECTED_TRACE_HASH = 809706600;
    private static final long EXPECTED_MAKESPAN = 32;
    private static final double EXPECTED_MEAN_LEAD_TIME = 28.0;
}
