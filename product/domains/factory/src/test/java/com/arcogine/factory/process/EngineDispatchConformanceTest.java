package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.core.queue.Scheduler;
import com.arcogine.factory.jobs.JobView;
import com.arcogine.factory.machines.Machine;
import com.arcogine.factory.machines.MachineStore;
import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.routing.Routing;
import com.arcogine.factory.routing.RoutingStep;
import com.arcogine.factory.routing.RoutingStore;
import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** Executable characterization of the retained v1 resource-dispatch semantics. */
class EngineSemanticsV1DispatchConformanceTest {

    private record Step(long duration, Set<Integer> machines) {}

    private record Result(List<Long> orderCompletionTimes, double meanLeadTime, long makespan) {}

    @Test
    void onlineFilteringAndImmediateAcceptanceRankBeforeQueueDepth() {
        FactoryRuntime runtime = runtime(
                List.of(1, 1),
                List.of(List.of(new Step(10, Set.of(1, 2)))));

        OrderId first = runtime.submitWorkload(new ProductId(1), 1, 1.0).orElseThrow();
        OrderId second = runtime.submitWorkload(new ProductId(1), 1, 1.0).orElseThrow();

        assertEquals(
                new MachineId(1),
                jobForOrder(runtime, first).currentMachine(),
                "the first equal candidate must use the lowest MachineId");
        assertEquals(
                new MachineId(2),
                jobForOrder(runtime, second).currentMachine(),
                "an accepting machine must outrank a non-accepting machine even when its id is higher");

        FactoryRuntime offline = runtime.reset();
        offline.setMachineAvailability(new MachineId(1), false).orElseThrow();
        OrderId filtered = offline.submitWorkload(new ProductId(1), 1, 1.0).orElseThrow();
        assertEquals(
                new MachineId(2),
                jobForOrder(offline, filtered).currentMachine(),
                "an offline eligible machine must be removed when an online candidate exists");
    }

    @Test
    void allOfflineEligibilityFallsBackToTheCompleteSetAndRecoversSharedWork() {
        FactoryRuntime runtime = runtime(
                List.of(1, 1),
                List.of(List.of(new Step(5, Set.of(1, 2)))));
        runtime.setMachineAvailability(new MachineId(1), false).orElseThrow();
        runtime.setMachineAvailability(new MachineId(2), false).orElseThrow();

        OrderId waiting = runtime.submitWorkload(new ProductId(1), 1, 1.0).orElseThrow();
        assertNull(jobForOrder(runtime, waiting).currentMachine());
        assertEquals(Set.of(new MachineId(1), new MachineId(2)),
                runtime.pendingWorkView().getFirst().eligibleMachines());

        runtime.setMachineAvailability(new MachineId(2), true).orElseThrow();
        assertEquals(
                new MachineId(2),
                jobForOrder(runtime, waiting).currentMachine(),
                "shared waiting work must be reconsidered against the captured complete eligible set");
    }

    @Test
    void localQueueIsFifoAndRecoveryDispatchesOnlyOneLocalJobPerTrigger() {
        FactoryRuntime runtime = runtime(
                List.of(4),
                List.of(List.of(new Step(10, Set.of(1)))));
        runtime.setMachineAvailability(new MachineId(1), false).orElseThrow();

        for (int i = 0; i < 8; i++) {
            runtime.submitWorkload(new ProductId(1), 1, i + 1.0).orElseThrow();
        }

        runtime.setMachineAvailability(new MachineId(1), true).orElseThrow();
        var machine = runtime.machinesView().getFirst();
        assertEquals(1, machine.activeJobs().size(), "recovery must start one local job");
        assertEquals(7, machine.queueDepth(), "the remaining local jobs must stay FIFO-queued");

        Result result = finish(runtime);
        assertEquals(List.of(10L, 20L, 30L, 40L, 50L, 60L, 70L, 80L), result.orderCompletionTimes());
        assertEquals(80L, result.makespan(), "one local admission per recovery/completion trigger is observable");
    }

    @Test
    void mixedRecoveryRunsOneLocalAdmissionThenSharedBacklogToAFixpoint() {
        FactoryRuntime runtime = runtime(
                List.of(4, 1),
                List.of(
                        List.of(new Step(10, Set.of(1))),
                        List.of(new Step(3, Set.of(1, 2)))));
        runtime.setMachineAvailability(new MachineId(1), false).orElseThrow();
        runtime.setMachineAvailability(new MachineId(2), false).orElseThrow();

        OrderId localA = runtime.submitWorkload(new ProductId(1), 1, 1.0).orElseThrow();
        OrderId localB = runtime.submitWorkload(new ProductId(1), 1, 1.0).orElseThrow();
        List<OrderId> shared = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            shared.add(runtime.submitWorkload(new ProductId(2), 1, 1.0).orElseThrow());
        }

        runtime.setMachineAvailability(new MachineId(1), true).orElseThrow();
        var machine = runtime.machinesView().stream()
                .filter(view -> view.id().equals(new MachineId(1)))
                .findFirst().orElseThrow();
        assertEquals(4, machine.activeJobs().size(), "shared recovery must fill remaining compatible capacity");
        assertEquals(1, machine.queueDepth(), "the second local job must remain behind the first local admission");
        assertEquals(1, runtime.pendingWorkView().size(), "one shared job must remain after three shared placements");
        JobView localBJob = runtime.jobsView().filter(job -> job.orderId().equals(localB)).findFirst().orElseThrow();
        assertEquals(localBJob.id(), machine.queuedJobs().getFirst(),
                "the local queue remains owned by the second local order");
        assertTrue(runtime.jobsView().filter(job -> shared.contains(job.orderId()))
                .filter(job -> job.currentMachine() != null).count() == 3);
        assertEquals(new MachineId(1), jobForOrder(runtime, localA).currentMachine());

        finish(runtime);
    }

    @Test
    void localQueueArrivalOrderingWinsOverJobIdAndDurationOrdering() {
        FactoryRuntime runtime = runtime(
                List.of(1, 1),
                List.of(
                        List.of(new Step(10, Set.of(1))),
                        List.of(new Step(1, Set.of(2)), new Step(1, Set.of(1))),
                        List.of(new Step(5, Set.of(1)))));
        runtime.submitWorkload(new ProductId(1), 1, 1.0).orElseThrow();
        OrderId routed = runtime.submitWorkload(new ProductId(2), 1, 1.0).orElseThrow();
        OrderId queued = runtime.submitWorkload(new ProductId(3), 1, 1.0).orElseThrow();

        Result result = finish(runtime);
        assertEquals(List.of(10L, 16L, 15L), result.orderCompletionTimes());
        assertEquals(15L, runtime.orderExecution(queued).completedAt().value());
        assertEquals(16L, runtime.orderExecution(routed).completedAt().value());
    }

    @Test
    void sharedBacklogPreservesArrivalOrderWithoutHeadOfLineBlockingOrEarlyBinding() {
        FactoryRuntime runtime = runtime(
                List.of(1, 1, 1, 1),
                List.of(
                        List.of(new Step(20, Set.of(1, 2))),
                        List.of(new Step(1, Set.of(3, 4)))));
        runtime.submitWorkload(new ProductId(1), 1, 1.0).orElseThrow();
        runtime.submitWorkload(new ProductId(1), 1, 1.0).orElseThrow();
        runtime.submitWorkload(new ProductId(2), 1, 1.0).orElseThrow();
        runtime.submitWorkload(new ProductId(2), 1, 1.0).orElseThrow();
        OrderId blocked = runtime.submitWorkload(new ProductId(1), 1, 1.0).orElseThrow();
        OrderId later = runtime.submitWorkload(new ProductId(2), 1, 1.0).orElseThrow();

        assertNull(jobForOrder(runtime, blocked).currentMachine());
        assertNull(jobForOrder(runtime, later).currentMachine());
        assertEquals(List.of(blocked, later), runtime.pendingWorkView().stream()
                .map(view -> runtime.job(view.jobId()).orderId()).toList());

        runtime.advance();
        assertEquals(
                new MachineId(3),
                jobForOrder(runtime, later).currentMachine(),
                "a later placeable shared entry must not be blocked by an earlier full pool");
        assertNull(jobForOrder(runtime, blocked).currentMachine());
    }

    @Test
    void sharedWorkCanReselectADifferentEligibleMachineAfterWaiting() {
        FactoryRuntime runtime = runtime(
                List.of(1, 1),
                List.of(List.of(new Step(10, Set.of(1, 2)))));
        runtime.setMachineAvailability(new MachineId(1), false).orElseThrow();
        OrderId first = runtime.submitWorkload(new ProductId(1), 1, 1.0).orElseThrow();
        OrderId waiting = runtime.submitWorkload(new ProductId(1), 1, 1.0).orElseThrow();
        assertEquals(new MachineId(2), jobForOrder(runtime, first).currentMachine());
        assertNull(jobForOrder(runtime, waiting).currentMachine());

        runtime.setMachineAvailability(new MachineId(1), true).orElseThrow();
        assertEquals(new MachineId(1), jobForOrder(runtime, waiting).currentMachine());
    }

    @Test
    void overlapRankingRetainsExactSharedDemandAndUnboundReselection() {
        Result result = run(
                List.of(1, 1, 1),
                List.of(
                        List.of(new Step(5, Set.of(1)), new Step(1, Set.of(1, 2)), new Step(1, Set.of(1))),
                        List.of(new Step(6, Set.of(3))),
                        List.of(new Step(100, Set.of(1, 3)))));
        assertEquals(List.of(106L, 6L, 105L), result.orderCompletionTimes());
        assertEquals(72.33, result.meanLeadTime(), 0.01);
        assertEquals(106L, result.makespan());
    }

    @Test
    void overlapMirrorRetainsTheSameRankingForShortSharedWork() {
        Result result = run(
                List.of(1, 1, 1),
                List.of(
                        List.of(new Step(5, Set.of(1)), new Step(1, Set.of(1, 2)), new Step(1, Set.of(1))),
                        List.of(new Step(6, Set.of(3))),
                        List.of(new Step(1, Set.of(1, 3)))));
        assertEquals(List.of(7L, 6L, 6L), result.orderCompletionTimes());
        assertEquals(6.33, result.meanLeadTime(), 0.01);
    }

    @Test
    void longStepAndScarceMachineRankingFixturesRemainPinned() {
        Result longStep = run(
                List.of(1, 1, 1),
                List.of(
                        List.of(new Step(5, Set.of(1)), new Step(100, Set.of(1, 2))),
                        List.of(new Step(1000, Set.of(3))),
                        List.of(new Step(10, Set.of(1, 3)))));
        assertEquals(List.of(105L, 1000L, 15L), longStep.orderCompletionTimes());
        assertEquals(373.33, longStep.meanLeadTime(), 0.01);

        Result scarce = run(
                List.of(1, 1, 1),
                List.of(
                        List.of(new Step(5, Set.of(1)), new Step(60, Set.of(1, 2))),
                        List.of(new Step(500, Set.of(3))),
                        List.of(new Step(20, Set.of(1, 3))),
                        List.of(new Step(20, Set.of(1, 3)))));
        assertEquals(List.of(65L, 500L, 25L, 45L), scarce.orderCompletionTimes());
    }

    @Test
    void quantityAndConcurrencyOverlapFixtureRetainsV1CompletionVector() {
        FactoryRuntime runtime = runtime(
                List.of(2, 2, 1),
                List.of(
                        List.of(new Step(5, Set.of(1)), new Step(1, Set.of(1, 2)), new Step(1, Set.of(1))),
                        List.of(new Step(6, Set.of(3))),
                        List.of(new Step(100, Set.of(1, 3)))));
        List<OrderId> orders = List.of(
                runtime.submitWorkload(new ProductId(1), 2, 1.0).orElseThrow(),
                runtime.submitWorkload(new ProductId(2), 1, 1.0).orElseThrow(),
                runtime.submitWorkload(new ProductId(3), 3, 1.0).orElseThrow());
        Result result = finish(runtime);
        assertEquals(List.of(106L, 6L, 106L), result.orderCompletionTimes());
        assertEquals(72.67, result.meanLeadTime(), 0.01);
        assertEquals(106L, result.makespan());
        assertTrue(orders.stream().allMatch(id -> runtime.orderExecution(id).complete()));
    }

    @Test
    void routedRecoveryRegressionPreservesOrderCompletionVectorAndMakespan() {
        Result result = runRecovery(
                List.of(4, 1, 1),
                List.of(
                        List.of(new Step(1, Set.of(1)), new Step(9, Set.of(2)), new Step(4, Set.of(3))),
                        List.of(new Step(8, Set.of(1)), new Step(8, Set.of(2)), new Step(11, Set.of(3))),
                        List.of(new Step(1, Set.of(1)), new Step(3, Set.of(3)), new Step(11, Set.of(2)))),
                List.of(1L, 1L, 1L));
        assertEquals(List.of(14L, 29L, 29L), result.orderCompletionTimes());
        assertEquals(29L, result.makespan());
    }

    @Test
    void recoveryObjectiveConflictPreservesV1MakespanAndMeanLeadTime() {
        Result result = runRecovery(
                List.of(2, 1, 1),
                List.of(
                        List.of(new Step(9, Set.of(1)), new Step(11, Set.of(2)), new Step(10, Set.of(3))),
                        List.of(new Step(9, Set.of(1)), new Step(11, Set.of(3)), new Step(5, Set.of(2))),
                        List.of(new Step(7, Set.of(1)), new Step(10, Set.of(2)), new Step(9, Set.of(3))),
                        List.of(new Step(4, Set.of(1)), new Step(6, Set.of(3)), new Step(2, Set.of(1)))),
                List.of(2L, 1L, 2L, 4L));
        assertEquals(93L, result.makespan());
        assertEquals(69.75, result.meanLeadTime(), 0.001);
    }

    @Test
    void downstreamUnaryBottleneckPreservesTheEightJobCompletionAt810() {
        Result result = runRecovery(
                List.of(4, 1),
                List.of(List.of(new Step(10, Set.of(1)), new Step(100, Set.of(2)))),
                List.of(8L));
        assertEquals(810L, result.makespan());
    }

    @Test
    void overlapMultiplicityKeepsOneSharedEntryUnboundUntilAnEligibleMachineFrees() {
        FactoryRuntime runtime = runtime(
                List.of(1, 1, 1, 1),
                List.of(
                        List.of(new Step(500, Set.of(1))),
                        List.of(new Step(500, Set.of(2))),
                        List.of(new Step(500, Set.of(3))),
                        List.of(new Step(500, Set.of(4))),
                        List.of(new Step(100, Set.of(1, 2, 3, 4)))));
        for (int i = 1; i <= 4; i++) {
            runtime.submitWorkload(new ProductId(i), 1, 1.0).orElseThrow();
        }
        OrderId shared = runtime.submitWorkload(new ProductId(5), 1, 1.0).orElseThrow();

        assertNull(jobForOrder(runtime, shared).currentMachine());
        assertEquals(Set.of(1L, 2L, 3L, 4L), runtime.pendingWorkView().getFirst().eligibleMachines().stream()
                .map(MachineId::value).collect(Collectors.toSet()));

        runtime.advance();
        assertTrue(jobForOrder(runtime, shared).currentMachine() != null,
                "the shared entry must remain unbound while waiting and bind only after recovery can place it");
    }

    @Test
    void everyCompatibleSharedEntryContributesToCandidateRankingBeforeReselection() {
        MachineStore machines = new MachineStore();
        Machine m1 = new Machine(new MachineId(1), "M1", 4, null, 0);
        Machine m2 = new Machine(new MachineId(2), "M2", 1, null, 0);
        machines.add(m1);
        machines.add(m2);
        RoutingStore routings = new RoutingStore();
        routings.addRouting(new Routing(
                1,
                "shared-route",
                List.of(new RoutingStep(1, "shared", Set.of(new MachineId(1), new MachineId(2)), 1))));
        routings.addProductRouting(new ProductId(1), 1);
        FactoryHandler handler = new FactoryHandler(machines, routings, List.of(new ProductId(1)));

        // Establish two shared entries while both candidates are full, then leave M1 accepting
        // with a residual local queue and M2 accepting with no local queue. Both pending entries
        // are compatible with both machines, so each candidate's exact key includes both entries.
        m1.startJob(new JobId(900));
        m1.startJob(new JobId(901));
        m1.startJob(new JobId(902));
        m1.startJob(new JobId(903));
        m2.startJob(new JobId(910));
        handler.submitOrder(new ProductId(1), 1, 1.0, SimTime.ZERO, new Scheduler());
        handler.submitOrder(new ProductId(1), 1, 1.0, SimTime.ZERO, new Scheduler());
        m1.completeJob(new JobId(900));
        m1.enqueueJob(new JobId(920));
        m1.enqueueJob(new JobId(921));
        m1.enqueueJob(new JobId(922));
        m2.completeJob(new JobId(910));

        handler.handleMachineAvailability(new MachineId(2), true, new Scheduler(), SimTime.ZERO);

        assertEquals(new MachineId(2), handler.job(new JobId(1)).currentMachine(),
                "the unary candidate with two compatible shared entries must win the exact ranking");
        assertEquals(new MachineId(1), handler.job(new JobId(2)).currentMachine(),
                "the second entry must be reselected after the first placement changes capacity");
        assertTrue(handler.pendingWorkView().isEmpty());
    }

    @Test
    void equalTimeTaskEndsAreProcessedInInsertionOrder() {
        FactoryRuntime runtime = runtime(
                List.of(4),
                List.of(List.of(new Step(10, Set.of(1)))));
        runtime.submitWorkload(new ProductId(1), 4, 1.0).orElseThrow();

        List<Long> completedJobs = new ArrayList<>();
        while (runtime.advance().isPresent()) {
            runtime.drainSupportedEvents();
            runtime.jobsView().filter(JobView::isComplete)
                    .map(JobView::id).map(id -> id.value())
                    .filter(id -> !completedJobs.contains(id))
                    .forEach(completedJobs::add);
        }
        assertEquals(List.of(1L, 2L, 3L, 4L), completedJobs);
    }

    private static FactoryRuntime runtime(List<Integer> capacities, List<List<Step>> routes) {
        List<ConfiguredResource> resources = new ArrayList<>();
        for (int i = 0; i < capacities.size(); i++) {
            resources.add(new ConfiguredResource(new MachineId(i + 1), "M" + (i + 1), capacities.get(i), null, 0));
        }
        List<OperationDefinition> operations = new ArrayList<>();
        List<ProductDefinition> products = new ArrayList<>();
        for (int routeIndex = 0; routeIndex < routes.size(); routeIndex++) {
            List<OperationStepDefinition> steps = new ArrayList<>();
            List<Step> route = routes.get(routeIndex);
            for (int stepIndex = 0; stepIndex < route.size(); stepIndex++) {
                Step step = route.get(stepIndex);
                steps.add(new OperationStepDefinition(
                        stepIndex + 1,
                        "step-" + (stepIndex + 1),
                        step.machines().stream().map(MachineId::new).collect(Collectors.toSet()),
                        step.duration()));
            }
            int productId = routeIndex + 1;
            operations.add(new OperationDefinition(productId, "route-" + productId, steps));
            products.add(new ProductDefinition(new ProductId(productId), "P" + productId, productId));
        }
        FactoryModel model = new FactoryModel(resources, operations, products);
        FactoryModelVersion version = FactoryModelPublisher.publish(model);
        return FactoryRuntime.forModel(version);
    }

    private static Result run(List<Integer> capacities, List<List<Step>> routes) {
        FactoryRuntime runtime = runtime(capacities, routes);
        for (int i = 0; i < routes.size(); i++) {
            runtime.submitWorkload(new ProductId(i + 1), 1, 1.0).orElseThrow();
        }
        return finish(runtime);
    }

    private static Result runRecovery(
            List<Integer> capacities, List<List<Step>> routes, List<Long> quantities) {
        FactoryRuntime runtime = runtime(capacities, routes);
        runtime.setMachineAvailability(new MachineId(1), false).orElseThrow();
        for (int i = 0; i < quantities.size(); i++) {
            runtime.submitWorkload(new ProductId(i + 1), quantities.get(i), 1.0).orElseThrow();
        }
        runtime.setMachineAvailability(new MachineId(1), true).orElseThrow();
        return finish(runtime);
    }

    private static Result finish(FactoryRuntime runtime) {
        while (runtime.advance().isPresent()) {
            runtime.drainSupportedEvents();
        }
        List<Long> completionTimes = runtime.orderExecutionsView()
                .sorted(Comparator.comparingLong(view -> view.orderId().value()))
                .map(view -> view.completedAt().value())
                .toList();
        return new Result(completionTimes, runtime.avgLeadTime(), completionTimes.stream().mapToLong(Long::longValue).max().orElse(0));
    }

    private static JobView jobForOrder(FactoryRuntime runtime, OrderId orderId) {
        return runtime.jobsView().filter(job -> job.orderId().equals(orderId)).findFirst().orElseThrow();
    }
}
