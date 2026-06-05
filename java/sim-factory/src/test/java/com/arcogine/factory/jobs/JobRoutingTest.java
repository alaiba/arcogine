package com.arcogine.factory.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.routing.Routing;
import com.arcogine.factory.routing.RoutingStep;
import com.arcogine.factory.routing.RoutingStore;
import com.arcogine.types.JobId;
import com.arcogine.types.JobStatus;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Ported from crates/sim-factory/tests/job_routing.rs. */
class JobRoutingTest {

    private static Routing sampleRouting() {
        return new Routing(
                1,
                "Widget Routing",
                List.of(
                        new RoutingStep(1, "Milling", new MachineId(1), 5),
                        new RoutingStep(2, "Turning", new MachineId(2), 3)));
    }

    @Test
    void newJobIsQueued() {
        Job job = new Job(new JobId(1), new ProductId(1), 10, 2, new SimTime(0));
        assertEquals(JobStatus.Queued, job.status());
        assertEquals(0, job.currentStep());
        assertNull(job.currentMachine());
        assertNull(job.completedAt());
    }

    @Test
    void jobAdvancesThroughSteps() {
        Job job = new Job(new JobId(1), new ProductId(1), 10, 2, new SimTime(0));

        // Start first step
        job.start(new MachineId(1));
        assertEquals(JobStatus.InProgress, job.status());
        assertEquals(new MachineId(1), job.currentMachine());

        // Complete first step — not final, so returns to Queued
        job.completeStep(new SimTime(5));
        assertEquals(JobStatus.Queued, job.status());
        assertEquals(1, job.currentStep());
        assertNull(job.currentMachine());

        // Start second step
        job.start(new MachineId(2));
        assertEquals(JobStatus.InProgress, job.status());

        // Complete second step — final step
        job.completeStep(new SimTime(8));
        assertEquals(JobStatus.Completed, job.status());
        assertEquals(2, job.currentStep());
        assertTrue(job.isComplete());
    }

    @Test
    void completedJobHasLeadTime() {
        Job job = new Job(new JobId(1), new ProductId(1), 10, 1, new SimTime(10));
        job.start(new MachineId(1));
        job.completeStep(new SimTime(25));

        assertEquals(15L, job.leadTime().orElseThrow());
    }

    @Test
    void cannotStartCompletedJob() {
        Job job = new Job(new JobId(1), new ProductId(1), 10, 1, new SimTime(0));
        job.start(new MachineId(1));
        job.completeStep(new SimTime(5));
        assertEquals(JobStatus.Completed, job.status());

        assertThrows(SimError.InvalidStateTransition.class, () -> job.start(new MachineId(2)));
    }

    @Test
    void cannotCompleteStepWhenQueued() {
        Job job = new Job(new JobId(1), new ProductId(1), 10, 2, new SimTime(0));
        assertEquals(JobStatus.Queued, job.status());

        // Completing a step without starting should fail
        assertThrows(
                SimError.InvalidStateTransition.class, () -> job.completeStep(new SimTime(5)));
    }

    @Test
    void jobStoreCreatesUniqueIds() {
        JobStore store = new JobStore();
        JobId id1 = store.createJob(new ProductId(1), 10, 2, new SimTime(0));
        JobId id2 = store.createJob(new ProductId(1), 5, 2, new SimTime(1));
        assertNotEquals(id1, id2);
    }

    @Test
    void jobStoreUnknownIdReturnsError() {
        JobStore store = new JobStore();
        SimError.UnknownId error =
                assertThrows(SimError.UnknownId.class, () -> store.get(new JobId(999)));
        assertEquals("job", error.kind());
        assertEquals(999L, error.id());
    }

    @Test
    void routingStoreLookup() {
        RoutingStore store = new RoutingStore();
        store.addRouting(sampleRouting());
        store.addProductRouting(new ProductId(1), 1);

        Routing routing = store.getRoutingForProduct(new ProductId(1));
        assertEquals(2, routing.stepCount());
        assertEquals("Milling", routing.getStep(0).orElseThrow().name());
        assertEquals("Turning", routing.getStep(1).orElseThrow().name());
    }

    @Test
    void routingStoreUnknownProductReturnsError() {
        RoutingStore store = new RoutingStore();
        assertThrows(
                SimError.UnknownId.class, () -> store.getRoutingForProduct(new ProductId(999)));
    }

    @Test
    void routingStepsInCorrectOrder() {
        Routing routing = sampleRouting();
        RoutingStep step0 = routing.getStep(0).orElseThrow();
        RoutingStep step1 = routing.getStep(1).orElseThrow();

        assertEquals(new MachineId(1), step0.machineId());
        assertEquals(new MachineId(2), step1.machineId());
        assertFalse(routing.getStep(2).isPresent());
    }
}
