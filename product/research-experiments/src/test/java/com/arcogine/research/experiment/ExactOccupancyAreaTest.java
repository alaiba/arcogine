package com.arcogine.research.experiment;

import static com.arcogine.research.experiment.OutcomeAssertions.assertDerived;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.research.experiment.EligibilityPoolOccupancyOracle.EligibilityPool;
import com.arcogine.research.experiment.EligibilityPoolOccupancyOracle.PoolOccupancies;
import com.arcogine.research.experiment.EligibilityPoolOccupancyOracle.PoolOccupancy;
import com.arcogine.research.experiment.ExperimentFixture.WindowIntent;
import com.arcogine.research.experiment.ProcessingOccupancyOracle.ResourceOccupancy;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Occupancy areas are the exact sums and products their definitions state, of any size: evidence
 * that licenses a derivation yields a derived value even where those areas do not fit in a {@code
 * long}. Every expected area is integrated by hand from the fixture's own constants.
 */
class ExactOccupancyAreaTest {

    private static final long MAX = Long.MAX_VALUE;
    private static final MachineId RESOURCE = new MachineId(1);
    private static final ProductId PRODUCT = new ProductId(1);

    /**
     * One online resource of {@code concurrency}, one step of duration {@link Long#MAX_VALUE} that only
     * it serves, and as many jobs as it has slots: every job is dispatched at tick 0 and completes at
     * {@code MAX}, which is therefore the closing boundary's time.
     */
    private static ExperimentEvidence saturatedForMaxTicks(int concurrency) {
        FactoryModel model = new FactoryModel(
                List.of(new ConfiguredResource(RESOURCE, "Resource", concurrency, null, 0)),
                List.of(new OperationDefinition(
                        1, "longest", List.of(new OperationStepDefinition(1, "longest", Set.of(RESOURCE), MAX)))),
                List.of(new ProductDefinition(PRODUCT, "product", 1)));
        ExperimentEvidence evidence = ExperimentRunner.run(new ExperimentFixture(
                "extreme/concurrency-" + concurrency + "-for-max-ticks",
                model,
                List.of(
                        ExperimentStep.submit(PRODUCT, concurrency, 1.0),
                        ExperimentStep.advanceToQuiescence(MAX),
                        ExperimentStep.captureEvents("everything")),
                WindowIntent.COMPLETE_RUN,
                List.of()));
        assertTrue(evidence.allCommandsAccepted());
        assertEquals(MAX, evidence.observation(ExperimentEvidence.CLOSING_LABEL).metadata().currentTime().value());
        return evidence;
    }

    /** {@code jobs} steps of {@code MAX} ticks each, and {@code MAX} ticks times {@code concurrency}. */
    private static void assertBothOraclesDerive(int concurrency, int jobs, BigInteger expectedArea) {
        BigInteger occupied = BigInteger.valueOf(MAX).multiply(BigInteger.valueOf(jobs));
        BigInteger capacity = BigInteger.valueOf(MAX).multiply(BigInteger.valueOf(concurrency));
        assertEquals(expectedArea, occupied);
        assertEquals(expectedArea, capacity);
        ExperimentEvidence evidence = saturatedForMaxTicks(concurrency);

        List<ResourceOccupancy> perResource = assertDerived(
                new ProcessingOccupancyOracle(ExperimentEvidence.CLOSING_LABEL).evaluateOn(evidence)).value();
        assertEquals(List.of(new ResourceOccupancy(RESOURCE, occupied, capacity)), perResource);

        PoolOccupancies perPool = assertDerived(
                new EligibilityPoolOccupancyOracle(ExperimentEvidence.CLOSING_LABEL).evaluateOn(evidence)).value();
        EligibilityPool pool = new EligibilityPool(List.of(new OperationStep(1, 1, "longest")), Set.of(RESOURCE));
        assertEquals(List.of(new PoolOccupancy(pool, occupied, capacity)), perPool.pools());
    }

    @Test
    void twoSlotsBusyForMaxTicksDeriveTwiceMaxJobTicks() {
        assertBothOraclesDerive(2, 2, new BigInteger("18446744073709551614"));
    }

    @Test
    void threeSlotsBusyForMaxTicksDeriveThreeTimesMaxJobTicks() {
        assertBothOraclesDerive(3, 3, BigInteger.valueOf(MAX).multiply(BigInteger.valueOf(3)));
    }

    @Test
    void oneSlotBusyForMaxTicksDerivesMaxJobTicksAsALongDid() {
        assertBothOraclesDerive(1, 1, BigInteger.valueOf(MAX));
    }

    @Test
    void poolsWhoseCrossProductsExceedALongAreOrderedExactly() {
        EligibilityPool a = new EligibilityPool(List.of(new OperationStep(1, 1, "A")), Set.of(new MachineId(1)));
        EligibilityPool b = new EligibilityPool(List.of(new OperationStep(1, 2, "B")), Set.of(new MachineId(2)));
        BigInteger max = BigInteger.valueOf(MAX);
        // 1 - 1/(3 MAX) against 1 - 1/(2 MAX): the first is larger, and both cross-products pass a long.
        BigInteger aOccupied = max.multiply(BigInteger.valueOf(3)).subtract(BigInteger.ONE);
        BigInteger aCapacity = max.multiply(BigInteger.valueOf(3));
        BigInteger bOccupied = max.multiply(BigInteger.TWO).subtract(BigInteger.ONE);
        BigInteger bCapacity = max.multiply(BigInteger.TWO);
        PoolOccupancy first = new PoolOccupancy(a, aOccupied, aCapacity);
        PoolOccupancy second = new PoolOccupancy(b, bOccupied, bCapacity);

        int expected = aOccupied.multiply(bCapacity).compareTo(bOccupied.multiply(aCapacity));
        assertEquals(1, expected);
        assertEquals(expected, Integer.signum(first.compareOccupancy(second)));
        assertEquals(-expected, Integer.signum(second.compareOccupancy(first)));
        assertEquals(0, first.compareOccupancy(new PoolOccupancy(b, aOccupied, aCapacity)));
        assertEquals(Set.of(a), new PoolOccupancies(List.of(second, first)).maximumOccupancyPools());
    }
}
