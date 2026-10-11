package com.arcogine.research.experiment;

import static com.arcogine.research.experiment.OutcomeAssertions.assertDerived;
import static com.arcogine.research.experiment.OutcomeAssertions.assertUnderdetermined;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeEventPayload;
import com.arcogine.research.experiment.EligibilityPoolOccupancyOracle.EligibilityPool;
import com.arcogine.research.experiment.EligibilityPoolOccupancyOracle.PoolOccupancies;
import com.arcogine.research.experiment.EligibilityPoolOccupancyOracle.PoolOccupancy;
import com.arcogine.research.experiment.ExperimentFixture.WindowIntent;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * The contract of the eligibility-pool occupancy derivation: the inputs it declares, the evidence it
 * refuses, and hand-derived measurements on a dedicated design (one pool per step) and on a design
 * whose shared resource merges two steps into one pool.
 */
class EligibilityPoolOccupancyOracleTest {

    private static final String CUT = ThreeStepRoutingFamily.CUT;
    private static final String ASSEMBLE = ThreeStepRoutingFamily.ASSEMBLE;
    private static final String INSPECT = ThreeStepRoutingFamily.INSPECT;

    private static OracleOutcome<PoolOccupancies> atClosing(ExperimentEvidence evidence) {
        return new EligibilityPoolOccupancyOracle(ExperimentEvidence.CLOSING_LABEL).evaluateOn(evidence);
    }

    @Test
    void itReadsOnlyItsDeclaredInputsAndNamesTheEvidenceThatSupportsIt() {
        ExperimentEvidence evidence = ExperimentRunner.run(CapacityCorpus.sharedResourceAuthored());

        OracleOutcome.Derived<PoolOccupancies> derived = assertDerived(atClosing(evidence));

        assertEquals(
                Set.of(EvidenceInput.PUBLISHED_MODEL, EvidenceInput.OBSERVATIONS, EvidenceInput.SUPPORTED_EVENTS),
                EligibilityPoolOccupancyOracle.DEFINITION.inputs());
        assertEquals(EligibilityPoolOccupancyOracle.DEFINITION, derived.definition());
        long cursor = evidence.observation(ExperimentEvidence.CLOSING_LABEL).metadata().latestEventSequence();
        assertEquals(
                new EvidenceSupport(List.of(ExperimentEvidence.CLOSING_LABEL), Optional.of(new SequenceRange(1, cursor))),
                derived.support());
    }

    @Test
    void aDedicatedDesignHasOnePoolPerStep() {
        LinearRoutingFamily family = CapacityCorpus.DEDICATED_LINE;

        PoolOccupancies occupancy = assertDerived(atClosing(ExperimentRunner.run(CapacityCorpus.waitingVersusOccupancy()))).value();

        // Hand-derived in CapacityCorpus: each resource processes all twelve units over the 67-tick run.
        assertEquals(
                List.of(
                        new PoolOccupancy(CapacityCorpus.pool(family, List.of(CUT), List.of("Cutter")), 36, 67),
                        new PoolOccupancy(CapacityCorpus.pool(family, List.of(ASSEMBLE), List.of("Assembler")), 48, 67),
                        new PoolOccupancy(CapacityCorpus.pool(family, List.of(INSPECT), List.of("Inspector")), 60, 67)),
                occupancy.pools());
    }

    @Test
    void aSharedResourceMergesTheStepsItServesIntoOnePoolWithPoolLevelCapacityOnly() {
        LinearRoutingFamily family = CapacityCorpus.SHARED_RESOURCE;

        PoolOccupancies occupancy = assertDerived(atClosing(ExperimentRunner.run(CapacityCorpus.sharedResourceAuthored()))).value();

        // Hand-derived in CapacityCorpus: 2 x 3 + 2 x 2 job-ticks on Assembler and Shared over 11 ticks,
        // reported for the pool of ASSEMBLE and INSPECT together and never split between the two steps.
        assertEquals(
                List.of(
                        new PoolOccupancy(CapacityCorpus.pool(family, List.of(CUT), List.of("Cutter")), 4, 11),
                        new PoolOccupancy(
                                CapacityCorpus.pool(family, List.of(ASSEMBLE, INSPECT), List.of("Assembler", "Shared")),
                                10,
                                22)),
                occupancy.pools());
    }

    @Test
    void poolsAreConnectedComponentsOfEligibilityAcrossOperationsAndSkipResourcesThatServeNothing() {
        // Operation 1: P on {1}, Q on {2}; operation 2: R on {2, 3}, S on {4}. Resource 5 serves no step.
        FactoryModel model = new FactoryModel(
                List.of(resource(1), resource(2), resource(3), resource(4), resource(5)),
                List.of(
                        new OperationDefinition(1, "first", List.of(step(1, "P", 1), step(2, "Q", 2))),
                        new OperationDefinition(2, "second", List.of(step(1, "R", 2, 3), step(2, "S", 4)))),
                List.of(new ProductDefinition(new ProductId(1), "one", 1), new ProductDefinition(new ProductId(2), "two", 2)));

        assertEquals(
                List.of(
                        new EligibilityPool(List.of(new OperationStep(1, 1, "P")), Set.of(new MachineId(1))),
                        new EligibilityPool(
                                List.of(new OperationStep(1, 2, "Q"), new OperationStep(2, 1, "R")),
                                Set.of(new MachineId(2), new MachineId(3))),
                        new EligibilityPool(List.of(new OperationStep(2, 2, "S")), Set.of(new MachineId(4)))),
                EligibilityPoolOccupancyOracle.poolsOf(model));
    }

    @Test
    void aPartialEventWindowIsRefused() {
        ExperimentEvidence joinedLate = ExperimentRunner.run(new ExperimentFixture(
                "partial/shared-resource-joined-late",
                CapacityCorpus.SHARED_RESOURCE.model(),
                List.of(
                        ExperimentStep.submit(LinearRoutingFamily.PRODUCT, CapacityCorpus.PAIR_QUANTITY, LinearRoutingFamily.UNIT_PRICE),
                        ExperimentStep.discardEvents("joined-late"),
                        ExperimentStep.advanceToQuiescence(1_000),
                        ExperimentStep.captureEvents("after-join")),
                WindowIntent.PARTIAL,
                List.of()));

        OracleOutcome.Underdetermined<PoolOccupancies> refused = assertUnderdetermined(atClosing(joinedLate));

        assertTrue(refused.reasons().getFirst().contains("not all retained"), refused.toString());
    }

    @Test
    void noElapsedTimeIsRefused() {
        ExperimentEvidence evidence = ExperimentRunner.run(StarterCorpus.longStepAndParallelCapacity());

        OracleOutcome.Underdetermined<PoolOccupancies> refused =
                assertUnderdetermined(new EligibilityPoolOccupancyOracle("after-submission").evaluateOn(evidence));

        assertTrue(refused.reasons().getFirst().contains("no supported time has elapsed"), refused.toString());
    }

    @Test
    void anAvailabilityChangeOfAPooledResourceIsRefusedButOneOfAResourceServingNothingIsNot() {
        // The dedicated line plus a spare resource that no step lists.
        FactoryModel line = CapacityCorpus.DEDICATED_LINE.model();
        FactoryModel withSpare = new FactoryModel(
                List.of(line.resources().get(0), line.resources().get(1), line.resources().get(2), resource(4)),
                line.operations(),
                line.products());
        MachineId inspector = CapacityCorpus.DEDICATED_LINE.resourceId("Inspector");
        MachineId spare = new MachineId(4);

        ExperimentEvidence spareToggled = runWithAvailabilityToggled(withSpare, spare);
        ExperimentEvidence inspectorToggled = runWithAvailabilityToggled(withSpare, inspector);

        // The spare is in no pool, so its availability does not bear on any pool's capacity ...
        assertEquals(3, assertDerived(atClosing(spareToggled)).value().pools().size());
        // ... while per-resource occupancy, which reports the spare too, refuses.
        assertUnderdetermined(new ProcessingOccupancyOracle(ExperimentEvidence.CLOSING_LABEL).evaluateOn(spareToggled));
        // A pooled resource that went offline and came back refuses the pool measurement.
        OracleOutcome.Underdetermined<PoolOccupancies> refused = assertUnderdetermined(atClosing(inspectorToggled));
        assertTrue(refused.reasons().getFirst().contains("resource " + inspector + " changed availability"), refused.toString());
    }

    @Test
    void aCompletionWithoutItsDispatchIsRefused() {
        ExperimentEvidence evidence = ExperimentRunner.run(CapacityCorpus.sharedResourceAuthored());
        List<RuntimeEventEnvelope> withoutFirstDispatch = evidence.retainedEvents().stream()
                .filter(event -> !(event.payload() instanceof RuntimeEventPayload.JobDispatched dispatched
                        && dispatched.jobId().value() == 1 && dispatched.stepIndex() == 0))
                .toList();

        OracleOutcome.Underdetermined<PoolOccupancies> refused =
                assertUnderdetermined(atClosing(TamperedEvidence.withEvents(evidence, withoutFirstDispatch)));

        assertTrue(refused.reasons().getFirst().contains("has no matching dispatch"), refused.toString());
    }

    @Test
    void maximumOccupancyComparesRatiosExactlyAndReturnsEveryTie() {
        EligibilityPool a = new EligibilityPool(List.of(new OperationStep(1, 1, "A")), Set.of(new MachineId(1)));
        EligibilityPool b = new EligibilityPool(List.of(new OperationStep(1, 2, "B")), Set.of(new MachineId(2)));
        EligibilityPool c = new EligibilityPool(List.of(new OperationStep(1, 3, "C")), Set.of(new MachineId(3)));

        // 1/2 and 3/6 are the same ratio, so both are maximal.
        assertEquals(
                Set.of(a, b),
                new PoolOccupancies(List.of(
                        new PoolOccupancy(a, 1, 2), new PoolOccupancy(b, 3, 6), new PoolOccupancy(c, 1, 3)))
                        .maximumOccupancyPools());

        // (2^53 - 1) / 2^53 and (2^53 - 2) / (2^53 - 1) round to the same double, but the first is larger.
        long twoTo53 = 1L << 53;
        assertEquals(
                Set.of(a),
                new PoolOccupancies(List.of(
                        new PoolOccupancy(a, twoTo53 - 1, twoTo53), new PoolOccupancy(b, twoTo53 - 2, twoTo53 - 1)))
                        .maximumOccupancyPools());
        assertEquals((double) (twoTo53 - 1) / twoTo53, (double) (twoTo53 - 2) / (twoTo53 - 1));

        assertEquals(Set.of(), new PoolOccupancies(List.of()).maximumOccupancyPools());
        assertThrows(IllegalArgumentException.class, () -> new PoolOccupancy(a, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new PoolOccupancy(a, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> new EligibilityPool(List.of(), Set.of(new MachineId(1))));
        assertThrows(IllegalArgumentException.class, () -> new EligibilityPool(List.of(new OperationStep(1, 1, "A")), Set.of()));
    }

    private static ExperimentEvidence runWithAvailabilityToggled(FactoryModel model, MachineId machineId) {
        return ExperimentRunner.run(new ExperimentFixture(
                "availability/toggled-" + machineId.value(),
                model,
                List.of(
                        ExperimentStep.setAvailability(machineId, false),
                        ExperimentStep.setAvailability(machineId, true),
                        ExperimentStep.submit(LinearRoutingFamily.PRODUCT, 2, LinearRoutingFamily.UNIT_PRICE),
                        ExperimentStep.advanceToQuiescence(1_000),
                        ExperimentStep.captureEvents("everything")),
                WindowIntent.COMPLETE_RUN,
                List.of()));
    }

    private static ConfiguredResource resource(long id) {
        return new ConfiguredResource(new MachineId(id), "R" + id, 1, null, 0);
    }

    private static OperationStepDefinition step(long id, String name, long... eligible) {
        Set<MachineId> resources = Arrays.stream(eligible).mapToObj(MachineId::new).collect(Collectors.toSet());
        return new OperationStepDefinition(id, name, resources, 1);
    }
}
