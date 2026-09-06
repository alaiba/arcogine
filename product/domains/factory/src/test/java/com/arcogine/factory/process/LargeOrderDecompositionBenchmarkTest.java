package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.factory.jobs.JobView;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.model.ResourceDefinition;
import com.arcogine.factory.orders.OrderExecutionView;
import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * ADR-0010 closure evidence for the supported child-materialization ceiling.
 *
 * <p>This is intentionally an acceptance benchmark rather than a wall-clock performance contract:
 * it executes the maximum supported 100,000-unit order, records host-dependent timing/heap
 * diagnostics, and asserts deterministic linear object/event volume plus terminal semantics.
 */
class LargeOrderDecompositionBenchmarkTest {
    private static final long QUANTITY = 100_000L;
    private static final double UNIT_PRICE = 1.25;

    private static FactoryRuntime runtime() {
        FactoryModel model = new FactoryModel(
                List.of(new ResourceDefinition(new MachineId(1), "Machine", 1, null, 0)),
                List.of(new OperationDefinition(
                        1,
                        "Route",
                        List.of(new OperationStepDefinition(
                                1, "PROCESS", Set.of(new MachineId(1)), 1)))),
                List.of(new ProductDefinition(new ProductId(1), "Widget", 1)));
        FactoryModelVersion version = FactoryModelPublisher.publish(model);
        return FactoryRuntime.forModel(version);
    }

    @Test
    void quantityOneHundredThousandMaterializesAndCompletesDeterministically() throws Exception {
        BenchmarkResult first = runBenchmark("first");
        BenchmarkResult replay = runBenchmark("replay");

        assertEquals(first.jobIds(), replay.jobIds());
        assertEquals(first.terminalExecution(), replay.terminalExecution());
        assertEquals(first.completionCount(), replay.completionCount());
        assertEquals(first.completingJobId(), replay.completingJobId());
        assertEquals(first.eventCount(), replay.eventCount());
    }

    private static BenchmarkResult runBenchmark(String label) throws Exception {
        FactoryRuntime runtime = runtime();
        long heapBefore = usedHeapBytes();
        long admissionStarted = System.nanoTime();

        OrderId orderId = runtime.submitWorkload(new ProductId(1), QUANTITY, UNIT_PRICE).orElseThrow();

        long admissionNanos = System.nanoTime() - admissionStarted;
        long heapAfterAdmission = usedHeapBytes();

        assertEquals(1, runtime.ordersView().count());
        assertEquals(QUANTITY, runtime.jobsView().count());
        OrderExecutionView admitted = runtime.orderExecution(orderId);
        assertEquals(QUANTITY, admitted.requestedQuantity());
        assertEquals(QUANTITY, admitted.releasedQuantity());
        assertEquals(0, admitted.completedQuantity());

        List<JobId> jobIds = runtime.jobsView().map(JobView::id).toList();
        assertEquals(QUANTITY, jobIds.size());
        assertEquals(new JobId(1), jobIds.getFirst());
        assertEquals(new JobId(QUANTITY), jobIds.getLast());
        assertTrue(runtime.jobsView().allMatch(job ->
                job.orderId().equals(orderId)
                        && job.quantity() == 1
                        && job.totalSteps() == 1
                        && job.ordinalWithinOrder() == job.id().value() - 1));

        long queued = runtime.machinesView().stream().mapToLong(machine -> machine.queueDepth()).sum();
        assertEquals(QUANTITY - 1, queued);
        assertEquals(0, runtime.pendingWorkView().size());

        long executionStarted = System.nanoTime();
        long eventCount = 0;
        long completionCount = 0;
        JobId completingJobId = null;
        Event event;
        while ((event = runtime.advance().orElse(null)) != null) {
            eventCount++;
            if (event.payload() instanceof EventPayload.OrderCompleted completion) {
                completionCount++;
                completingJobId = completion.jobId();
                assertEquals(orderId, completion.orderId());
                assertEquals(QUANTITY, completion.quantity());
            }
        }
        long executionNanos = System.nanoTime() - executionStarted;
        long heapAfterExecution = usedHeapBytes();

        OrderExecutionView terminal = runtime.orderExecution(orderId);
        assertEquals(QUANTITY, terminal.requestedQuantity());
        assertEquals(QUANTITY, terminal.releasedQuantity());
        assertEquals(QUANTITY, terminal.completedQuantity());
        assertTrue(terminal.complete());
        assertEquals(1, completionCount);
        assertEquals(new JobId(QUANTITY), completingJobId);
        assertEquals(1, runtime.completedSales());
        assertEquals(QUANTITY * UNIT_PRICE, runtime.completedSalesValue());
        assertEquals(0, runtime.backlog());

        // One initial TaskEnd, then each of the remaining N-1 children schedules a TaskStart and
        // TaskEnd, plus exactly one aggregate OrderCompleted event: 2N total events.
        assertEquals(2 * QUANTITY, eventCount);

        System.out.printf(
                Locale.ROOT,
                "Large-order benchmark [%s]: quantity=%d jobs=%d events=%d admissionMs=%.3f executionMs=%.3f heapAdmissionDeltaMiB=%.3f heapTerminalDeltaMiB=%.3f%n",
                label,
                QUANTITY,
                jobIds.size(),
                eventCount,
                admissionNanos / 1_000_000.0,
                executionNanos / 1_000_000.0,
                (heapAfterAdmission - heapBefore) / (1024.0 * 1024.0),
                (heapAfterExecution - heapBefore) / (1024.0 * 1024.0));

        return new BenchmarkResult(jobIds, terminal, completionCount, completingJobId, eventCount);
    }

    private static long usedHeapBytes() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private record BenchmarkResult(
            List<JobId> jobIds,
            OrderExecutionView terminalExecution,
            long completionCount,
            JobId completingJobId,
            long eventCount) {}
}
