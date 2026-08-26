package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.model.FactoryRuntimeAssembler;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.model.ResourceDefinition;
import com.arcogine.types.MachineId;
import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Proves Gate 1's explicit-workload-submission slice: a headless caller can instantiate a
 * published factory model's runtime and submit production workload directly through {@link
 * FactoryHandler#submitOrder}, with no economy, pricing, demand, or agent handler anywhere in the
 * loop.
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

    @Test
    void submitsExplicitWorkloadWithoutEconomyDemandOrAgents() {
        FactoryRuntimeAssembler.Assembled assembled = FactoryRuntimeAssembler.assemble(publishedModel());
        FactoryHandler factory = assembled.factory();
        Scheduler scheduler = new Scheduler();

        OrderId orderId = factory.submitOrder(new ProductId(1), 3, 12.0, new SimTime(0), scheduler);

        assertEquals(1L, factory.ordersView().count());
        assertEquals(1L, factory.jobsView().count());
        var job = factory.jobsView().findFirst().orElseThrow();
        assertEquals(orderId, job.orderId());

        Event taskEnd = scheduler.nextEvent().orElseThrow();
        factory.handleEvent(taskEnd, scheduler);

        assertTrue(factory.jobsView().findFirst().orElseThrow().isComplete());
        assertEquals(1L, factory.completedSales());
        Event completed = scheduler.nextEvent().orElseThrow();
        var payload = (EventPayload.OrderCompleted) completed.payload();
        assertEquals(job.id(), payload.jobId());
    }

    @Test
    void repeatedIdenticalSubmissionsAreDeterministic() {
        Event firstCompleted = runToCompletion();
        Event secondCompleted = runToCompletion();

        assertEquals(firstCompleted, secondCompleted);
    }

    private static Event runToCompletion() {
        FactoryRuntimeAssembler.Assembled assembled = FactoryRuntimeAssembler.assemble(publishedModel());
        FactoryHandler factory = assembled.factory();
        Scheduler scheduler = new Scheduler();

        factory.submitOrder(new ProductId(1), 3, 12.0, new SimTime(0), scheduler);
        Event taskEnd = scheduler.nextEvent().orElseThrow();
        factory.handleEvent(taskEnd, scheduler);

        return scheduler.nextEvent().orElseThrow();
    }
}
