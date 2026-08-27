package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.core.event.Event;
import com.arcogine.factory.jobs.JobView;
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
 * End-to-end Gate 2 acceptance evidence, driven entirely through {@link FactoryRuntime} -- never
 * {@link FactoryHandler}, a store, or a scheduler directly -- proving that a published {@link
 * FactoryModelVersion} naming more than one eligible resource per step survives {@link
 * com.arcogine.factory.model.FactoryRuntimeAssembler} unchanged and produces deterministic
 * multi-resource dispatch through the supported consumer-facing seam. {@link
 * MultiResourceDispatchTest} exercises the same dispatch invariants at the narrower {@link
 * FactoryHandler} seam; this class exists to prove the full model -> assembler -> runtime
 * boundary specifically, matching how {@link Gate1EngineReadinessAcceptanceTest} proves Gate 1
 * end to end.
 */
class Gate2MultiResourceDispatchAcceptanceTest {

    private static final double UNIT_PRICE = 10.0;
    private static final long STEP_DURATION = 5;

    private static FactoryModelVersion twoEligibleMachinesModel() {
        FactoryModel model = new FactoryModel(
                List.of(
                        new ResourceDefinition(new MachineId(1), "Mill A", 1, null, 0),
                        new ResourceDefinition(new MachineId(2), "Mill B", 1, null, 0)),
                List.of(new OperationDefinition(
                        1,
                        "Widget Route",
                        List.of(new OperationStepDefinition(
                                1, "Milling", Set.of(new MachineId(1), new MachineId(2)), STEP_DURATION)))),
                List.of(new ProductDefinition(new ProductId(1), "Widget", 1)));
        return FactoryModelPublisher.publish(model);
    }

    private static FactoryRuntime freshRuntime() {
        return FactoryRuntime.forModel(twoEligibleMachinesModel());
    }

    @Test
    void publishedMultiEligibleModelSurvivesAssemblyAndDispatchesBothOrdersConcurrently() {
        FactoryRuntime runtime = freshRuntime();

        OrderId orderA = runtime.submitWorkload(new ProductId(1), 1, UNIT_PRICE);
        OrderId orderB = runtime.submitWorkload(new ProductId(1), 1, UNIT_PRICE);

        JobView jobA = runtime.jobsView()
                .filter(j -> j.orderId().equals(orderA))
                .findFirst()
                .orElseThrow();
        JobView jobB = runtime.jobsView()
                .filter(j -> j.orderId().equals(orderB))
                .findFirst()
                .orElseThrow();

        assertEquals(
                Set.of(new MachineId(1), new MachineId(2)),
                Set.of(jobA.currentMachine(), jobB.currentMachine()),
                "the published model's two eligible machines must both be dispatched to, through "
                        + "FactoryRuntime alone");
    }

    @Test
    void identicalWorkloadFromTwoFreshRuntimesResolvesToTheSameMachineAssignments() {
        FactoryRuntime runtimeA = freshRuntime();
        FactoryRuntime runtimeB = freshRuntime();

        runtimeA.submitWorkload(new ProductId(1), 1, UNIT_PRICE);
        runtimeB.submitWorkload(new ProductId(1), 1, UNIT_PRICE);

        MachineId assignedA = runtimeA.jobsView().findFirst().orElseThrow().currentMachine();
        MachineId assignedB = runtimeB.jobsView().findFirst().orElseThrow().currentMachine();

        assertEquals(new MachineId(1), assignedA, "a single job among equal idle candidates ties to the lowest MachineId");
        assertEquals(assignedA, assignedB, "two fresh runtimes given the same published model and workload must "
                + "resolve identical machine assignments");
    }

    @Test
    void bringingAnEligibleMachineOnlineDispatchesWorkStrandedWaitingForTheOtherMachine() {
        FactoryRuntime runtime = freshRuntime();

        runtime.setMachineAvailability(new MachineId(1), false);

        // Order A: only Mill B is online, so it starts immediately there.
        OrderId orderA = runtime.submitWorkload(new ProductId(1), 1, UNIT_PRICE);
        JobView jobA = runtime.jobsView().filter(j -> j.orderId().equals(orderA)).findFirst().orElseThrow();
        assertEquals(new MachineId(2), jobA.currentMachine());

        // Order B: Mill A is offline and Mill B is now busy, so it must wait rather than being
        // pinned to one specific machine's queue.
        OrderId orderB = runtime.submitWorkload(new ProductId(1), 1, UNIT_PRICE);
        JobView jobBWaiting = runtime.jobsView().filter(j -> j.orderId().equals(orderB)).findFirst().orElseThrow();
        assertEquals(null, jobBWaiting.currentMachine(), "order B must be waiting, not yet dispatched");

        // Mill A recovers -- even though order B never touched Mill A, this must dispatch it.
        runtime.setMachineAvailability(new MachineId(1), true);

        JobView jobBAfter = runtime.jobsView().filter(j -> j.orderId().equals(orderB)).findFirst().orElseThrow();
        assertEquals(
                new MachineId(1),
                jobBAfter.currentMachine(),
                "recovering one eligible machine must dispatch work that was stranded waiting for "
                        + "either eligible machine to free up, through FactoryRuntime alone");
    }

    @Test
    void offlineEligibleMachineIsExcludedAndRemovingItDoesNotRequireChangingTheProductDefinition() {
        FactoryRuntime runtime = freshRuntime();
        runtime.setMachineAvailability(new MachineId(1), false);

        OrderId orderId = runtime.submitWorkload(new ProductId(1), 1, UNIT_PRICE);

        JobView job = runtime.jobsView().filter(j -> j.orderId().equals(orderId)).findFirst().orElseThrow();
        assertEquals(
                new MachineId(2),
                job.currentMachine(),
                "an offline eligible machine must be excluded from dispatch without any change to "
                        + "the published product/operation definition");

        List<Event> events = new java.util.ArrayList<>();
        Event event;
        while ((event = runtime.advance().orElse(null)) != null) {
            events.add(event);
        }
        assertTrue(!events.isEmpty(), "the order must still complete through the one remaining eligible machine");
    }
}
