package com.arcogine.research.experiment;

import static com.arcogine.research.experiment.OutcomeAssertions.assertDerived;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.process.OrderObservation;
import com.arcogine.factory.process.ResourceObservation;
import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeEventPayload;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.factory.process.RuntimePerformanceObservation;
import com.arcogine.research.experiment.ExperimentFixture.WindowIntent;
import com.arcogine.research.experiment.ThreeStepRoutingFamily.Stage;
import com.arcogine.types.MachineId;
import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Research-custody experiment for the simulation analytics ownership question: which supported
 * performance results a consumer could obtain from one fresh observation, which need retained
 * supported events, and whether the order-level results exhaust a useful diagnosis.
 *
 * <p>Every recomputation here is research-local and measures supported evidence only; nothing
 * reconstructs or re-decides which resource the Engine selected.
 */
class PerformanceEvidenceSufficiencyExperiment {

    private static final String LOG = "[analytics-ownership] ";

    // ---- A: order-level results recomputed from the same observation -------------------------

    @Test
    void orderLevelPerformanceResultsAreFunctionsOfTheSameObservation() {
        List<ExperimentFixture> fixtures = new ArrayList<>(StarterCorpus.all());
        fixtures.addAll(CapacityCorpus.all());
        int checked = 0;
        int incompleteBoundaries = 0;
        for (ExperimentFixture fixture : fixtures) {
            ExperimentEvidence evidence = ExperimentRunner.run(fixture);
            for (Map.Entry<String, RuntimeObservation> entry : evidence.observations().entrySet()) {
                RuntimeObservation observation = entry.getValue();
                RuntimePerformanceObservation reported = observation.performance();
                String where = fixture.id() + "/" + entry.getKey();
                long completed = observation.orders().stream().filter(OrderObservation::complete).count();
                long incomplete = observation.orders().size() - completed;
                if (incomplete > 0) {
                    incompleteBoundaries++;
                }
                assertEquals(reported.backlog(), incomplete, where + " backlog");
                assertEquals(reported.completedOrders(), completed, where + " completedOrders");
                assertEquals(reported.averageLeadTime(), meanLeadTime(observation), where + " averageLeadTime");
                assertEquals(
                        reported.throughputPerTick(),
                        throughput(completed, observation.metadata().currentTime().value()),
                        where + " throughputPerTick");
                checked++;
            }
        }
        System.out.println(LOG + "A: recomputed backlog, completedOrders, averageLeadTime and throughputPerTick"
                + " exactly from " + checked + " observations (" + incompleteBoundaries
                + " with incomplete orders) across " + fixtures.size() + " corpus fixtures");
        assertTrue(checked >= fixtures.size());
        assertTrue(incompleteBoundaries > 0, "the corpora must include boundaries with incomplete orders");
    }

    // ---- B: completed sales value needs more than the observation ------------------------------

    @Test
    void completedSalesValueIsNotDeterminedByTheRestOfAFreshObservation() {
        ExperimentEvidence cheap = ExperimentRunner.run(pricedRun("price-10", 10.0));
        ExperimentEvidence dear = ExperimentRunner.run(pricedRun("price-20", 20.0));
        assertEquals(cheap.publishedModel(), dear.publishedModel());

        RuntimeObservation a = closing(cheap);
        RuntimeObservation b = closing(dear);
        assertNotEquals(a.performance().completedSalesValue(), b.performance().completedSalesValue());
        assertEquals(withoutCompletedSalesValue(a), withoutCompletedSalesValue(b),
                "apart from completedSalesValue the two fresh observations are identical");

        // Complete ORDER_COMPLETED events, summed in supported-sequence order, reproduce it exactly.
        assertEquals(a.performance().completedSalesValue(), valueFromCompletionEvents(cheap.retainedEvents()));
        assertEquals(b.performance().completedSalesValue(), valueFromCompletionEvents(dear.retainedEvents()));

        // A consumer that joined after the first completion has only part of that history.
        long firstCompletion = cheap.retainedEvents().stream()
                .filter(event -> event.payload() instanceof RuntimeEventPayload.OrderCompleted)
                .mapToLong(event -> event.simulationTime().value())
                .min()
                .orElseThrow();
        ExperimentEvidence lateJoin = ExperimentRunner.run(new ExperimentFixture(
                "price-10/joined-after-first-completion",
                StarterCorpus.BASELINE_FAMILY.model(),
                List.of(
                        ExperimentStep.submit(ThreeStepRoutingFamily.PRODUCT, 2, 10.0),
                        ExperimentStep.submit(ThreeStepRoutingFamily.PRODUCT, 2, 10.0),
                        ExperimentStep.advanceUntil(firstCompletion),
                        ExperimentStep.discardEvents("before-join"),
                        ExperimentStep.advanceToQuiescence(1_000),
                        ExperimentStep.captureEvents("after-join")),
                WindowIntent.PARTIAL,
                List.of()));
        assertFalse(lateJoin.window().isComplete());
        double partial = valueFromCompletionEvents(lateJoin.retainedEvents());
        double supplied = closing(lateJoin).performance().completedSalesValue();
        assertEquals(a.performance().completedSalesValue(), supplied, "the Engine accumulator still supplies the total");
        assertNotEquals(supplied, partial, "the retained events alone no longer reconstruct it");
        System.out.println(LOG + "B: completedSalesValue 10.0-priced=" + a.performance().completedSalesValue()
                + " 20.0-priced=" + b.performance().completedSalesValue()
                + " with otherwise identical fresh observations; complete events reconstruct it exactly; a late join"
                + " (missing " + lateJoin.window().missingSequences() + ") sums " + partial + " against the supplied "
                + supplied);
    }

    // ---- C: busyTicks needs more than the observation and the published model -------------------

    @Test
    void busyTicksIsNotDeterminedByTheRestOfAFreshObservationOrThePublishedModel() {
        LinearRoutingFamily family = new LinearRoutingFamily(
                List.of(new LinearRoutingFamily.Step("ASSEMBLE", 5)),
                List.of(LinearRoutingFamily.Resource.of("A1", 1, "ASSEMBLE"),
                        LinearRoutingFamily.Resource.of("A2", 1, "ASSEMBLE")));
        MachineId a1 = family.resourceId("A1");
        MachineId a2 = family.resourceId("A2");

        ExperimentEvidence onA1 = ExperimentRunner.run(pinnedBy("assigned-a1", family, a2));
        ExperimentEvidence onA2 = ExperimentRunner.run(pinnedBy("assigned-a2", family, a1));
        assertTrue(onA1.allCommandsAccepted());
        assertTrue(onA2.allCommandsAccepted());
        assertEquals(onA1.publishedModel(), onA2.publishedModel());

        RuntimeObservation x = closing(onA1);
        RuntimeObservation y = closing(onA2);
        assertEquals(Map.of(a1, 5L, a2, 0L), busyTicks(x));
        assertEquals(Map.of(a1, 0L, a2, 5L), busyTicks(y));
        assertEquals(withoutBusyTicks(x), withoutBusyTicks(y),
                "apart from busyTicks the two fresh observations are identical, and so is the published model");

        // Complete step-completion events plus published step durations reproduce it exactly.
        assertEquals(busyTicks(x), withZeroes(busyTicksFromEvents(onA1), x));
        assertEquals(busyTicks(y), withZeroes(busyTicksFromEvents(onA2), y));
        System.out.println(LOG + "C: busyTicks " + busyTicks(x) + " vs " + busyTicks(y)
                + " with otherwise identical fresh observations and model; complete events plus model durations"
                + " reconstruct both exactly");
    }

    @Test
    void completeEventsAndThePublishedModelReconstructBothAccumulatorsAcrossTheCorpora() {
        List<ExperimentFixture> fixtures = new ArrayList<>(StarterCorpus.all());
        fixtures.addAll(CapacityCorpus.all());
        int reconstructed = 0;
        for (ExperimentFixture fixture : fixtures) {
            ExperimentEvidence evidence = ExperimentRunner.run(fixture);
            if (!evidence.window().isComplete()) {
                continue;
            }
            RuntimeObservation closing = evidence.observation(ExperimentEvidence.CLOSING_LABEL);
            assertEquals(busyTicks(closing), withZeroes(busyTicksFromEvents(evidence), closing), fixture.id());
            assertEquals(closing.performance().completedSalesValue(),
                    valueFromCompletionEvents(evidence.retainedEvents()), fixture.id());
            reconstructed++;
        }
        System.out.println(LOG + "C': reconstructed busyTicks and completedSalesValue from complete events and the"
                + " published model for " + reconstructed + " complete-window corpus fixtures");
        assertTrue(reconstructed > 0);
    }

    // ---- D: single-order degeneracy and controlled versus multi-variable comparison -------------

    @Test
    void identicalOrderLevelResultsCanHideDifferentDiagnoses() {
        ThreeStepRoutingFamily cutHeavy =
                new ThreeStepRoutingFamily(Stage.of(8, 1), Stage.of(1, 1), Stage.of(1, 1));
        ThreeStepRoutingFamily assembleHeavy =
                new ThreeStepRoutingFamily(Stage.of(1, 1), Stage.of(8, 1), Stage.of(1, 1));
        ExperimentEvidence first = ExperimentRunner.run(singleOrder("cut-heavy", cutHeavy, 4));
        ExperimentEvidence second = ExperimentRunner.run(singleOrder("assemble-heavy", assembleHeavy, 4));

        // Flow line of four units: completion = sum of durations + 3 x the longest = 10 + 24.
        assertEquals(34L, CompletionTickOracle.completionTick(first));
        assertEquals(34L, CompletionTickOracle.completionTick(second));
        assertEquals(closing(first).performance(), closing(second).performance(),
                "every order-level performance result is identical");

        // Where work waits, and for how long, differs completely.
        List<WaitingWorkByStepOracle.WaitingAtStep> waitingFirst =
                assertDerived(new WaitingWorkByStepOracle("mid-run").evaluateOn(first)).value();
        List<WaitingWorkByStepOracle.WaitingAtStep> waitingSecond =
                assertDerived(new WaitingWorkByStepOracle("mid-run").evaluateOn(second)).value();
        assertEquals(Map.of(ThreeStepRoutingFamily.CUT, 3L), waitingByStep(waitingFirst));
        assertEquals(Map.of(ThreeStepRoutingFamily.ASSEMBLE, 3L), waitingByStep(waitingSecond));

        Map<String, Long> waitsFirst = totalWaitByStep(first);
        Map<String, Long> waitsSecond = totalWaitByStep(second);
        assertEquals(Map.of("CUT", 48L, "ASSEMBLE", 0L, "INSPECT", 0L), waitsFirst);
        assertEquals(Map.of("CUT", 6L, "ASSEMBLE", 42L, "INSPECT", 0L), waitsSecond);

        // Resource-level processing totals differ by location but are the same multiset.
        assertNotEquals(busyTicks(closing(first)), busyTicks(closing(second)));
        assertEquals(sortedValues(busyTicks(closing(first))), sortedValues(busyTicks(closing(second))));
        System.out.println(LOG + "D1: identical performance " + closing(first).performance()
                + "; waiting units at mid-run " + waitingByStep(waitingFirst) + " vs " + waitingByStep(waitingSecond)
                + "; total dispatch waits " + waitsFirst + " vs " + waitsSecond
                + "; busyTicks " + busyTicks(closing(first)) + " vs " + busyTicks(closing(second)));
    }

    @Test
    void aMultiVariableDeltaIsNotTheSumOfItsSingleVariableDeltas() {
        long base = completion("base-1-8-1", 1, 8, 1);
        long assembleFaster = completion("assemble-4", 1, 4, 1);
        long cutSlower = completion("cut-6", 6, 8, 1);
        long both = completion("cut-6-assemble-4", 6, 4, 1);
        assertEquals(34L, base);
        assertEquals(18L, assembleFaster);
        assertEquals(39L, cutSlower);
        assertEquals(29L, both);
        long singleSum = (assembleFaster - base) + (cutSlower - base);
        long joint = both - base;
        assertNotEquals(singleSum, joint);
        System.out.println(LOG + "D3: base " + base + "; assemble 8->4 gives " + (assembleFaster - base)
                + "; cut 1->6 gives " + (cutSlower - base) + "; both give " + joint + " (single deltas sum to "
                + singleSum + ")");
    }

    // ---- helpers ---------------------------------------------------------------------------------

    private static double meanLeadTime(RuntimeObservation observation) {
        long count = 0;
        long sum = 0;
        for (OrderObservation order : observation.orders()) {
            if (!order.complete()) {
                continue;
            }
            count++;
            long leadTime = order.completedAt().minus(order.createdAt());
            sum = leadTime > Long.MAX_VALUE - sum ? Long.MAX_VALUE : sum + leadTime;
        }
        return count == 0 ? 0.0 : (double) sum / count;
    }

    private static double throughput(long completed, long elapsedTicks) {
        return elapsedTicks == 0 ? 0.0 : (double) completed / elapsedTicks;
    }

    private static RuntimeObservation closing(ExperimentEvidence evidence) {
        return evidence.withNormalizedRunIdentity().observation(ExperimentEvidence.CLOSING_LABEL);
    }

    private static ExperimentFixture pricedRun(String id, double unitPrice) {
        return new ExperimentFixture(
                id,
                StarterCorpus.BASELINE_FAMILY.model(),
                List.of(
                        ExperimentStep.submit(ThreeStepRoutingFamily.PRODUCT, 2, unitPrice),
                        ExperimentStep.submit(ThreeStepRoutingFamily.PRODUCT, 2, unitPrice),
                        ExperimentStep.advanceToQuiescence(1_000),
                        ExperimentStep.captureEvents("everything")),
                WindowIntent.COMPLETE_RUN,
                List.of());
    }

    private static ExperimentFixture pinnedBy(String id, LinearRoutingFamily family, MachineId heldOffline) {
        return new ExperimentFixture(
                id,
                family.model(),
                List.of(
                        ExperimentStep.setAvailability(heldOffline, false),
                        ExperimentStep.submit(LinearRoutingFamily.PRODUCT, 1, LinearRoutingFamily.UNIT_PRICE),
                        ExperimentStep.advanceToQuiescence(100),
                        ExperimentStep.setAvailability(heldOffline, true),
                        ExperimentStep.captureEvents("everything")),
                WindowIntent.COMPLETE_RUN,
                List.of());
    }

    private static ExperimentFixture singleOrder(String id, ThreeStepRoutingFamily family, long quantity) {
        return new ExperimentFixture(
                id,
                family.model(),
                List.of(
                        ExperimentStep.submit(ThreeStepRoutingFamily.PRODUCT, quantity, ThreeStepRoutingFamily.UNIT_PRICE),
                        ExperimentStep.advanceUntil(6),
                        ExperimentStep.observe("mid-run"),
                        ExperimentStep.advanceToQuiescence(1_000),
                        ExperimentStep.captureEvents("everything")),
                WindowIntent.COMPLETE_RUN,
                List.of());
    }

    private static long completion(String id, long cut, long assemble, long inspect) {
        ThreeStepRoutingFamily family =
                new ThreeStepRoutingFamily(Stage.of(cut, 1), Stage.of(assemble, 1), Stage.of(inspect, 1));
        return CompletionTickOracle.completionTick(ExperimentRunner.run(singleOrder(id, family, 4)));
    }

    private static RuntimeObservation withoutCompletedSalesValue(RuntimeObservation observation) {
        RuntimePerformanceObservation p = observation.performance();
        return new RuntimeObservation(observation.metadata(), observation.resources(), observation.orders(),
                observation.jobs(), observation.pendingWork(), new RuntimePerformanceObservation(
                        p.backlog(), p.completedOrders(), 0.0, p.averageLeadTime(), p.throughputPerTick()));
    }

    private static RuntimeObservation withoutBusyTicks(RuntimeObservation observation) {
        List<ResourceObservation> resources = observation.resources().stream()
                .map(r -> new ResourceObservation(r.machineId(), r.name(), r.state(), r.concurrency(),
                        r.activeJobIds(), r.queueDepth(), r.capacityLiters(), r.setupTime(), 0L))
                .toList();
        return new RuntimeObservation(observation.metadata(), resources, observation.orders(), observation.jobs(),
                observation.pendingWork(), observation.performance());
    }

    private static Map<MachineId, Long> busyTicks(RuntimeObservation observation) {
        return observation.resources().stream()
                .collect(Collectors.toMap(ResourceObservation::machineId, ResourceObservation::busyTicks));
    }

    private static Map<MachineId, Long> withZeroes(Map<MachineId, Long> credited, RuntimeObservation observation) {
        Map<MachineId, Long> all = new HashMap<>();
        observation.resources().forEach(r -> all.put(r.machineId(), credited.getOrDefault(r.machineId(), 0L)));
        return all;
    }

    private static List<Long> sortedValues(Map<MachineId, Long> values) {
        return values.values().stream().sorted().toList();
    }

    private static double valueFromCompletionEvents(List<RuntimeEventEnvelope> events) {
        double total = 0.0;
        for (RuntimeEventEnvelope event : events) {
            if (event.payload() instanceof RuntimeEventPayload.OrderCompleted completed) {
                total += completed.quantity() * completed.unitPrice();
            }
        }
        return total;
    }

    /** Credits each completed step's published duration to the resource the completion names. */
    private static Map<MachineId, Long> busyTicksFromEvents(ExperimentEvidence evidence) {
        FactoryModel model = evidence.publishedModel();
        Map<OrderId, ProductId> productOf = new HashMap<>();
        Map<MachineId, Long> credited = new HashMap<>();
        for (RuntimeEventEnvelope event : evidence.retainedEvents()) {
            switch (event.payload()) {
                case RuntimeEventPayload.OrderAccepted accepted -> productOf.put(accepted.orderId(), accepted.productId());
                case RuntimeEventPayload.JobStepCompleted completed -> {
                    long duration = stepDuration(model, productOf.get(completed.orderId()), completed.stepIndex());
                    credited.merge(completed.machineId(), duration,
                            (left, right) -> left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right);
                }
                default -> { }
            }
        }
        return credited;
    }

    private static long stepDuration(FactoryModel model, ProductId productId, int stepIndex) {
        ProductDefinition product = model.products().stream()
                .filter(candidate -> candidate.id().equals(productId))
                .findFirst()
                .orElseThrow();
        OperationDefinition operation = model.operations().stream()
                .filter(candidate -> candidate.id() == product.operationId())
                .findFirst()
                .orElseThrow();
        return operation.steps().get(stepIndex).duration();
    }

    private static Map<String, Long> waitingByStep(List<WaitingWorkByStepOracle.WaitingAtStep> waiting) {
        return waiting.stream().collect(Collectors.toMap(
                WaitingWorkByStepOracle.WaitingAtStep::stepName, WaitingWorkByStepOracle.WaitingAtStep::waitingJobs));
    }

    private static Map<String, Long> totalWaitByStep(ExperimentEvidence evidence) {
        DispatchProfileOracle.DispatchProfile profile = assertDerived(
                new DispatchProfileOracle(ExperimentEvidence.CLOSING_LABEL).evaluateOn(evidence)).value();
        Map<String, Long> waits = new TreeMap<>();
        profile.waits().forEach(wait -> waits.put(wait.step().name(), wait.totalWaitTicks()));
        return waits;
    }
}
