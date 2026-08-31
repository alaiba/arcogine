package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.event.EventType;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.model.ResourceDefinition;
import com.arcogine.types.MachineId;
import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Proves Gate 1's explicit-workload-submission slice: a headless caller can instantiate a
 * published factory model's runtime and submit production workload through {@link
 * FactoryRuntime#submitWorkload}, supplying only product/quantity/commercial intent -- no
 * economy, pricing, demand, or agent handler in the loop, and no caller-owned {@code Scheduler} or
 * caller-chosen simulation time.
 */
class ExplicitWorkloadSubmissionTest {

    private static FactoryModelVersion publishedModel() {
        FactoryModel model = new FactoryModel(
                List.of(new ResourceDefinition(new MachineId(1), "Mill", 1, null, 0)),
                List.of(new OperationDefinition(
                        1,
                        "Widget Route",
                        List.of(new OperationStepDefinition(
                                1, "Milling", Set.of(new MachineId(1)), 5)))),
                List.of(new ProductDefinition(new ProductId(1), "Widget", 1)));
        return FactoryModelPublisher.publish(model);
    }

    private static FactoryRuntime runtime() {
        return FactoryRuntime.forModel(publishedModel());
    }

    @Test
    void submitsExplicitWorkloadWithoutEconomyDemandOrAgents() {
        FactoryRuntime runtime = runtime();

        OrderId orderId = runtime.submitWorkload(new ProductId(1), 3, 12.0).orElseThrow();

        assertEquals(1L, runtime.ordersView().count());
        assertEquals(3L, runtime.jobsView().count());
        var jobs = runtime.jobsView().toList();
        assertEquals(List.of(0L, 1L, 2L), jobs.stream().map(j -> j.ordinalWithinOrder()).toList());
        assertTrue(jobs.stream().allMatch(job -> job.orderId().equals(orderId)));

        // The job's routing repeats once per unit of quantity, so a quantity-3 order needs three
        // TaskEnd events (one per unit) to complete, not one.
        Event completed = null;
        while (completed == null) {
            Event next = runtime.advance().orElseThrow();
            if (next.eventType() == EventType.OrderCompleted) completed = next;
        }
        assertTrue(runtime.jobsView().allMatch(job -> job.isComplete()));
        assertEquals(1L, runtime.completedSales());
        var payload = (EventPayload.OrderCompleted) completed.payload();
        assertEquals(orderId, payload.orderId());
    }

    @Test
    void exposesReadOnlyProjectionsWithoutAFactoryHandlerAccessor() {
        FactoryRuntime runtime = runtime();
        runtime.submitWorkload(new ProductId(1), 3, 12.0).orElseThrow();
        var queuedJob = runtime.jobsView().findFirst().orElseThrow();

        assertEquals(queuedJob.id(), runtime.job(queuedJob.id()).id());
        assertEquals(1, runtime.machinesView().size());
        assertEquals(1L, runtime.backlog());

        while (runtime.advance().isPresent()) { /* drain child and aggregate events */ }

        assertEquals(0L, runtime.backlog());
        assertTrue(runtime.avgLeadTime() > 0.0);
        assertTrue(runtime.throughput(1) > 0.0);
        assertEquals(36.0, runtime.completedSalesValue());
    }

    @Test
    void repeatedIdenticalSubmissionsAreDeterministic() {
        Event firstCompleted = runToCompletion();
        Event secondCompleted = runToCompletion();

        assertEquals(firstCompleted, secondCompleted);
    }

    private static Event runToCompletion() {
        FactoryRuntime runtime = runtime();
        runtime.submitWorkload(new ProductId(1), 3, 12.0).orElseThrow();
        runtime.advance();
        runtime.advance();
        runtime.advance();
        return runtime.advance().orElseThrow();
    }
}
