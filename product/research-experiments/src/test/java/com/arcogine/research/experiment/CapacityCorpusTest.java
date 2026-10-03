package com.arcogine.research.experiment;

import static com.arcogine.research.experiment.OutcomeAssertions.assertDerived;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeEventPayload;
import com.arcogine.factory.process.RuntimeRunState;
import com.arcogine.research.experiment.EligibilityPoolOccupancyOracle.EligibilityPool;
import com.arcogine.research.experiment.EligibilityPoolOccupancyOracle.PoolOccupancies;
import com.arcogine.research.experiment.EligibilityPoolOccupancyOracle.PoolOccupancy;
import com.arcogine.research.experiment.ExperimentFixture.WindowIntent;
import com.arcogine.research.experiment.WaitingWorkByStepOracle.WaitingAtStep;
import com.arcogine.types.MachineId;
import com.arcogine.types.ModelFingerprint;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Invariants every fixture of the capacity corpus must satisfy, plus the cross-fixture relations the
 * corpus exists to pin. Each relation is stated for the designs proven here under the current Engine
 * interpretation; none of them is a general law about resource order, pooling or diagnostics.
 */
class CapacityCorpusTest {

    private static final String CUT = ThreeStepRoutingFamily.CUT;
    private static final String ASSEMBLE = ThreeStepRoutingFamily.ASSEMBLE;
    private static final String INSPECT = ThreeStepRoutingFamily.INSPECT;

    static Stream<Arguments> corpus() {
        return CapacityCorpus.all().stream().map(fixture -> Arguments.of(fixture.id(), fixture));
    }

    private static PoolOccupancies poolOccupancy(ExperimentEvidence evidence) {
        return assertDerived(new EligibilityPoolOccupancyOracle(ExperimentEvidence.CLOSING_LABEL).evaluateOn(evidence))
                .value();
    }

    private static PoolOccupancy occupancyOf(PoolOccupancies occupancies, EligibilityPool pool) {
        return occupancies.pools().stream()
                .filter(candidate -> candidate.pool().equals(pool))
                .findFirst()
                .orElseThrow();
    }

    /** Resources are the same type when they serve the same steps with the same concurrency. */
    private record ResourceType(int concurrency, Set<String> eligibleSteps) {}

    private record CompletionRange(int orders, long fastest, long slowest) {}

    private static CompletionRange completionRange(LinearRoutingFamily family) {
        List<LinearRoutingFamily> orders = typeInterleavings(family);
        LongSummaryStatistics completions = orders.stream()
                .mapToLong(order -> CapacityCorpus.completionTick(order, CapacityCorpus.LINE_QUANTITY))
                .summaryStatistics();
        return new CompletionRange(orders.size(), completions.getMin(), completions.getMax());
    }

    /**
     * Every distinct way of ordering the family's resource types. Resources of one type keep their
     * authored relative order, since reordering interchangeable resources among themselves is not a
     * different design.
     */
    private static List<LinearRoutingFamily> typeInterleavings(LinearRoutingFamily family) {
        Map<ResourceType, List<LinearRoutingFamily.Resource>> byType = new LinkedHashMap<>();
        for (LinearRoutingFamily.Resource resource : family.resources()) {
            byType.computeIfAbsent(
                            new ResourceType(resource.concurrency(), resource.eligibleSteps()), type -> new ArrayList<>())
                    .add(resource);
        }
        List<LinearRoutingFamily> orders = new ArrayList<>();
        interleave(family, List.copyOf(byType.values()), new int[byType.size()], new ArrayList<>(), orders);
        return orders;
    }

    private static void interleave(
            LinearRoutingFamily family,
            List<List<LinearRoutingFamily.Resource>> groups,
            int[] taken,
            List<LinearRoutingFamily.Resource> prefix,
            List<LinearRoutingFamily> orders) {
        if (prefix.size() == family.resources().size()) {
            orders.add(new LinearRoutingFamily(family.steps(), prefix));
            return;
        }
        for (int group = 0; group < groups.size(); group++) {
            if (taken[group] < groups.get(group).size()) {
                prefix.add(groups.get(group).get(taken[group]++));
                interleave(family, groups, taken, prefix, orders);
                taken[group]--;
                prefix.removeLast();
            }
        }
    }

    /** The resource that received the first {@code ASSEMBLE} dispatch of the run. */
    private static MachineId firstAssembler(ExperimentEvidence evidence) {
        return evidence.retainedEvents().stream()
                .map(RuntimeEventEnvelope::payload)
                .filter(payload -> payload instanceof RuntimeEventPayload.JobDispatched dispatched
                        && dispatched.stepIndex() == 1)
                .map(payload -> ((RuntimeEventPayload.JobDispatched) payload).machineId())
                .findFirst()
                .orElseThrow();
    }

    @Test
    void theCorpusHoldsItsFixturesUnderDistinctIdsAndDistinctModels() {
        List<ExperimentFixture> fixtures = CapacityCorpus.all();

        assertEquals(fixtures.size(), fixtures.stream().map(ExperimentFixture::id).distinct().count());
        Set<ModelFingerprint> fingerprints = fixtures.stream()
                .map(fixture -> fixture.publishedModel().fingerprint())
                .collect(Collectors.toSet());
        assertEquals(fixtures.size(), fingerprints.size(), "each fixture authors a different design");
        assertTrue(fixtures.stream().allMatch(fixture -> fixture.authoredModel().spatial().isEmpty()),
                "the capacity corpus is non-spatial");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("corpus")
    void everyFixtureRunsToQuiescenceWithAcceptedCommandsAndItsDeclaredWindow(String id, ExperimentFixture fixture) {
        ExperimentEvidence evidence = ExperimentRunner.run(fixture);

        assertEquals(id, evidence.fixtureId());
        assertTrue(evidence.allCommandsAccepted());
        assertEquals(RuntimeRunState.QUIESCENT, evidence.observation(ExperimentEvidence.CLOSING_LABEL).metadata().runState());
        assertEquals(fixture.windowIntent() == WindowIntent.COMPLETE_RUN, evidence.window().isComplete());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("corpus")
    void everyFixtureReplaysDeterministically(String id, ExperimentFixture fixture) {
        assertEquals(id, ExperimentRunner.runAndReplay(fixture).fixtureId());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("corpus")
    void everyExpectedClaimHoldsOnFreshEvidence(String id, ExperimentFixture fixture) {
        ExperimentEvidence evidence = ExperimentRunner.run(fixture);

        assertFalse(fixture.expectedClaims().isEmpty(), "a corpus fixture states the ground truth it is for");
        for (ExpectedClaim<?> claim : fixture.expectedClaims()) {
            ExpectedClaim.ClaimCheck<?> check = claim.check(evidence);
            assertTrue(check.holds(), () -> "claim '" + check.name() + "' expected " + check.expected() + " but was "
                    + check.actual());
        }
    }

    @Test
    void reversingTheDedicatedDesignsOrderChangesWhichAssemblerWinsTheTieButNotCompletion() {
        LinearRoutingFamily authored = CapacityCorpus.DEDICATED_ASSEMBLERS;
        LinearRoutingFamily reversed = authored.withReversedResourceOrder();
        ExperimentEvidence authoredRun = ExperimentRunner.run(CapacityCorpus.dedicatedAssemblersAuthored());
        ExperimentEvidence reversedRun = ExperimentRunner.run(CapacityCorpus.dedicatedAssemblersReversed());

        // The two fixtures author the same resources and routing; only the order, and so the identities, differ.
        assertEquals(authored.resources().reversed(), reversed.resources());
        assertEquals(authored.steps(), reversed.steps());
        assertEquals(CapacityCorpus.dedicatedAssemblersAuthored().script(), CapacityCorpus.dedicatedAssemblersReversed().script());

        // The final MachineId tie-break is reached: the first assembly goes to whichever assembler is
        // numbered lower, which is a different resource in each order.
        assertEquals(authored.resourceId("Assembler 1"), firstAssembler(authoredRun));
        assertEquals(reversed.resourceId("Assembler 2"), firstAssembler(reversedRun));

        // In this dedicated design that choice is between interchangeable resources, so completion is the same.
        assertEquals(9, CompletionTickOracle.completionTick(authoredRun));
        assertEquals(9, CompletionTickOracle.completionTick(reversedRun));
    }

    @Test
    void reversingTheSharedResourceDesignsOrderChangesCompletion() {
        LinearRoutingFamily authored = CapacityCorpus.SHARED_RESOURCE;
        LinearRoutingFamily reversed = authored.withReversedResourceOrder();
        ExperimentEvidence authoredRun = ExperimentRunner.run(CapacityCorpus.sharedResourceAuthored());
        ExperimentEvidence reversedRun = ExperimentRunner.run(CapacityCorpus.sharedResourceReversed());

        assertEquals(authored.resources().reversed(), reversed.resources());
        assertEquals(authored.steps(), reversed.steps());

        // The same tie at the first assembly is decided for the dedicated assembler in authored order
        // and for the shared resource in reversed order, and here that choice reaches completion.
        assertEquals(authored.resourceId("Assembler"), firstAssembler(authoredRun));
        assertEquals(reversed.resourceId("Shared"), firstAssembler(reversedRun));
        assertEquals(11, CompletionTickOracle.completionTick(authoredRun));
        assertEquals(9, CompletionTickOracle.completionTick(reversedRun));
    }

    @Test
    void waitingWorkAndOccupancyRankTheSameStepsDifferently() {
        LinearRoutingFamily family = CapacityCorpus.DEDICATED_LINE;
        ExperimentEvidence evidence = ExperimentRunner.run(CapacityCorpus.waitingVersusOccupancy());

        List<WaitingAtStep> waiting =
                assertDerived(new WaitingWorkByStepOracle("mid-run").evaluateOn(evidence)).value();
        long waitingAtAssemble = waiting.stream().filter(step -> step.stepName().equals(ASSEMBLE)).findFirst().orElseThrow().waitingJobs();
        long waitingAtInspect = waiting.stream().filter(step -> step.stepName().equals(INSPECT)).findFirst().orElseThrow().waitingJobs();

        PoolOccupancies occupancy = poolOccupancy(evidence);
        EligibilityPool assemble = CapacityCorpus.pool(family, List.of(ASSEMBLE), List.of("Assembler"));
        EligibilityPool inspect = CapacityCorpus.pool(family, List.of(INSPECT), List.of("Inspector"));

        // More work waits at ASSEMBLE than at INSPECT ...
        assertTrue(waitingAtAssemble > waitingAtInspect, waiting.toString());
        // ... while INSPECT's capacity is the more occupied, and the most occupied of all.
        assertTrue(occupancyOf(occupancy, inspect).compareOccupancy(occupancyOf(occupancy, assemble)) > 0);
        assertEquals(Set.of(inspect), occupancy.maximumOccupancyPools());
    }

    @Test
    void aSharedResourceMergesAssemblyAndInspectionIntoOnePoolAndServesBothSteps() {
        LinearRoutingFamily twoInspectors = CapacityCorpus.TWO_INSPECTORS;
        LinearRoutingFamily shared = CapacityCorpus.SHARED_ASSEMBLE_INSPECT;
        ExperimentEvidence dedicatedRun = ExperimentRunner.run(CapacityCorpus.twoDedicatedInspectors());
        ExperimentEvidence sharedRun = ExperimentRunner.run(CapacityCorpus.sharedAssembleInspectResource());

        // With dedicated inspectors each step is its own pool, and ASSEMBLE's is the most occupied.
        assertEquals(
                Set.of(CapacityCorpus.pool(twoInspectors, List.of(ASSEMBLE), List.of("Assembler"))),
                poolOccupancy(dedicatedRun).maximumOccupancyPools());

        // With the shared resource, assembly and inspection form one pool, the shared resource is
        // dispatched for both steps, and the order completes earlier.
        EligibilityPool merged =
                CapacityCorpus.pool(shared, List.of(ASSEMBLE, INSPECT), List.of("Assembler", "Inspector", "Shared"));
        EligibilityPool cut = CapacityCorpus.pool(shared, List.of(CUT), List.of("Cutter"));
        PoolOccupancies occupancy = poolOccupancy(sharedRun);
        assertEquals(List.of(cut, merged), occupancy.pools().stream().map(PoolOccupancy::pool).toList());
        DispatchProfileOracle.DispatchProfile profile =
                assertDerived(new DispatchProfileOracle(ExperimentEvidence.CLOSING_LABEL).evaluateOn(sharedRun)).value();
        assertEquals(
                Set.of(ASSEMBLE, INSPECT),
                profile.dispatches().stream()
                        .filter(dispatches -> dispatches.machineId().equals(shared.resourceId("Shared")))
                        .map(dispatches -> dispatches.step().name())
                        .collect(Collectors.toSet()));
        assertTrue(CompletionTickOracle.completionTick(sharedRun) < CompletionTickOracle.completionTick(dedicatedRun));

        // The cutter and the merged pool are exactly equally occupied (36/47 = 108/141): both are reported.
        assertEquals(0, occupancyOf(occupancy, cut).compareOccupancy(occupancyOf(occupancy, merged)));
        assertEquals(Set.of(cut, merged), occupancy.maximumOccupancyPools());
        assertNotEquals(occupancyOf(occupancy, cut), occupancyOf(occupancy, merged));
    }

    @Test
    void theMostOccupiedPoolIsNotTheOneWhoseAddedCapacityShortensTheRun() {
        LinearRoutingFamily family = CapacityCorpus.SHARED_ONLY_INSPECTION;
        ExperimentEvidence baseRun = ExperimentRunner.run(CapacityCorpus.sharedOnlyInspection());
        long base = CompletionTickOracle.completionTick(baseRun);

        // Cutting is measured as the most occupied pool, by a wide margin ...
        EligibilityPool cut = CapacityCorpus.pool(family, List.of(CUT), List.of("Cutter"));
        EligibilityPool merged = CapacityCorpus.pool(
                family,
                List.of(ASSEMBLE, INSPECT),
                List.of("Assembler 1", "Assembler 2", "Assembler 3", "Assembler 4", "Assembler 5", "Assembler 6", "Shared"));
        PoolOccupancies occupancy = poolOccupancy(baseRun);
        assertEquals(Set.of(cut), occupancy.maximumOccupancyPools());
        assertTrue(occupancyOf(occupancy, cut).compareOccupancy(occupancyOf(occupancy, merged)) > 0);

        // ... yet another cutter leaves the completion tick unchanged, and another inspector shortens the run.
        assertEquals(base, CompletionTickOracle.completionTick(
                ExperimentRunner.run(CapacityCorpus.sharedOnlyInspectionPlusCutter())));
        assertTrue(CompletionTickOracle.completionTick(
                        ExperimentRunner.run(CapacityCorpus.sharedOnlyInspectionPlusInspector()))
                < base);
    }

    @Test
    void resourceOrderCanDelayTheInspectionLimitedDesignButAnotherInspectorStillBeatsEveryOrder() {
        CompletionRange base = completionRange(CapacityCorpus.SHARED_ONLY_INSPECTION);
        CompletionRange plusCutter = completionRange(CapacityCorpus.SHARED_ONLY_INSPECTION_PLUS_CUTTER);
        CompletionRange plusInspector = completionRange(CapacityCorpus.SHARED_ONLY_INSPECTION_PLUS_INSPECTOR);

        // Every distinct interleaving of resource types was run: 8!/6!, 9!/(2! 6!) and 9!/6!.
        assertEquals(56, base.orders());
        assertEquals(252, plusCutter.orders());
        assertEquals(504, plusInspector.orders());

        // With a single inspector no unit reaches inspection before 7 and the twelve inspections take 60
        // ticks, so no order beats 67, and the authored order, Shared last, attains it ...
        assertEquals(67, base.fastest());
        assertEquals(67, plusCutter.fastest());
        // ... while numbering exactly one assembler before Shared sends it to assemble unit 2, which
        // delays the first inspection to 10.
        assertEquals(70, base.slowest());

        // So under every order another cutter cannot beat the base design's best, whereas another
        // inspector beats it under every order.
        assertTrue(plusInspector.slowest() < base.fastest());
    }
}
