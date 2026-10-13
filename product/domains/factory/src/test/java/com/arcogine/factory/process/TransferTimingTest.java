package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.model.spatial.FactoryFloor;
import com.arcogine.factory.model.spatial.ResourceFootprint;
import com.arcogine.factory.model.spatial.ResourceLayout;
import com.arcogine.factory.model.spatial.ResourcePlacement;
import com.arcogine.factory.model.spatial.SpatialRecord;
import com.arcogine.factory.model.validation.FactoryModelValidator;
import com.arcogine.factory.model.validation.ModelValidationError;
import com.arcogine.factory.model.validation.ModelValidationResult;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Transfer-timing arithmetic over a spatial record (docs/architecture/engine-semantics.md §7),
 * exercised directly: the runtime refuses a model whose spatial record is present, so no transfer
 * is reachable through it. Expected values are literals or come from an independent derivation
 * (counting grid steps, {@link BigInteger} arithmetic), never from a restatement of the production
 * expression. Records the arithmetic is expected to accept are also checked to be publishable, so
 * no case relies on a layout the Factory model would refuse.
 */
class TransferTimingTest {

    private static ResourceLayout layout(long id, long x, long y, long width, long height) {
        return new ResourceLayout(
                new MachineId(id), new ResourcePlacement(x, y), new ResourceFootprint(width, height));
    }

    private static SpatialRecord record(
            long floorWidth,
            long floorHeight,
            long ticksPerCell,
            long handlingTicks,
            ResourceLayout... layouts) {
        return new SpatialRecord(
                new FactoryFloor(floorWidth, floorHeight), ticksPerCell, handlingTicks, List.of(layouts));
    }

    private static long duration(SpatialRecord spatial, long source, long destination) {
        return TransferTiming.transferDuration(
                spatial, new MachineId(source), new MachineId(destination));
    }

    private static long distance(long sourceX, long sourceY, long destinationX, long destinationY) {
        return TransferTiming.manhattanDistance(
                new ResourcePlacement(sourceX, sourceY),
                new ResourcePlacement(destinationX, destinationY));
    }

    /**
     * Validates a minimal model around {@code spatial}: one configured resource per layout, in
     * layout order, and a one-step routing over the first, so only a spatial predicate can fail.
     */
    private static ModelValidationResult validate(SpatialRecord spatial) {
        List<ConfiguredResource> resources = spatial.resourceLayouts().stream()
                .map(layout -> new ConfiguredResource(
                        layout.resourceId(), "Resource " + layout.resourceId().value(), 1, null, 0))
                .toList();
        OperationDefinition routing = new OperationDefinition(
                100,
                "Widget routing",
                List.of(new OperationStepDefinition(1, "Milling", Set.of(resources.get(0).id()), 5)));
        FactoryModel model = new FactoryModel(
                resources,
                List.of(routing),
                List.of(new ProductDefinition(new ProductId(10), "Widget", 100)),
                Optional.of(spatial));
        return FactoryModelValidator.validate(model);
    }

    private static void assertPublishable(SpatialRecord spatial) {
        ModelValidationResult result = validate(spatial);
        assertTrue(result.isValid(), () -> result.errors().toString());
    }

    /** Counts unit moves from source to destination, one axis at a time. */
    private static long gridSteps(ResourcePlacement source, ResourcePlacement destination) {
        long x = source.x();
        long y = source.y();
        long steps = 0;
        while (x != destination.x()) {
            x += x < destination.x() ? 1 : -1;
            steps++;
        }
        while (y != destination.y()) {
            y += y < destination.y() ? 1 : -1;
            steps++;
        }
        return steps;
    }

    // ---- Reference-cell Manhattan distance ------------------------------------------------------

    @Test
    void distanceIsManhattanBetweenReferenceCellsInEitherDirection() {
        SpatialRecord spatial = record(10, 10, 3, 2,
                layout(1, 1, 1, 1, 1),
                layout(2, 6, 1, 1, 1),
                layout(3, 1, 8, 1, 1),
                layout(4, 4, 3, 1, 1),
                layout(5, 9, 0, 1, 1));
        assertPublishable(spatial);

        // Same row: 5 cells.
        assertEquals(5, distance(1, 1, 6, 1));
        assertEquals(5, distance(6, 1, 1, 1));
        assertEquals(17, duration(spatial, 1, 2));
        assertEquals(17, duration(spatial, 2, 1));

        // Same column: 7 cells.
        assertEquals(7, distance(1, 1, 1, 8));
        assertEquals(7, distance(1, 8, 1, 1));
        assertEquals(23, duration(spatial, 1, 3));
        assertEquals(23, duration(spatial, 3, 1));

        // Diagonal, both axes increasing: 3 + 2 cells.
        assertEquals(5, distance(1, 1, 4, 3));
        assertEquals(5, distance(4, 3, 1, 1));
        assertEquals(17, duration(spatial, 1, 4));
        assertEquals(17, duration(spatial, 4, 1));

        // Anti-diagonal: x grows by 8 while y shrinks by 8, so a signed sum would be 0.
        assertEquals(16, distance(1, 8, 9, 0));
        assertEquals(16, distance(9, 0, 1, 8));
        assertEquals(50, duration(spatial, 3, 5));
        assertEquals(50, duration(spatial, 5, 3));
    }

    @Test
    void distanceUsesReferenceCellsNotNearestCellsOrCentres() {
        // A 3x3 footprint at (0,0) occupies (0..2, 0..2); a 1x1 footprint at (3,2) is beside it.
        // Reference cells (0,0) and (3,2) are 5 cells apart. The nearest occupied cells, (2,2) and
        // (3,2), are 1 apart, and the footprint centres, (1,1) and (3,2), are 3 apart, so those
        // readings would give 12 and 16 ticks instead of 20.
        SpatialRecord spatial = record(5, 5, 2, 10, layout(1, 0, 0, 3, 3), layout(2, 3, 2, 1, 1));
        assertPublishable(spatial);

        assertEquals(20, duration(spatial, 1, 2));
        assertEquals(20, duration(spatial, 2, 1));
    }

    @Test
    void footprintExtentChangesNeitherDistanceNorDuration() {
        SpatialRecord unitFootprints =
                record(6, 6, 2, 10, layout(1, 0, 0, 1, 1), layout(2, 3, 2, 1, 1));
        SpatialRecord largeFootprints =
                record(6, 6, 2, 10, layout(1, 0, 0, 3, 2), layout(2, 3, 2, 3, 4));
        assertPublishable(unitFootprints);
        assertPublishable(largeFootprints);

        assertEquals(20, duration(unitFootprints, 1, 2));
        assertEquals(20, duration(largeFootprints, 1, 2));
        assertEquals(20, duration(largeFootprints, 2, 1));
    }

    @Test
    void adjacentResourcesAreOneCellApart() {
        SpatialRecord spatial = record(4, 4, 4, 0,
                layout(1, 1, 1, 1, 1), layout(2, 2, 1, 1, 1), layout(3, 1, 2, 1, 1));
        assertPublishable(spatial);

        assertEquals(4, duration(spatial, 1, 2)); // horizontal neighbours: 1 cell
        assertEquals(4, duration(spatial, 1, 3)); // vertical neighbours: 1 cell
        assertEquals(8, duration(spatial, 2, 3)); // diagonal neighbours: 2 cells
    }

    // ---- Handling --------------------------------------------------------------------------------

    @Test
    void handlingIsAppliedOncePerTransfer() {
        // 4 cells apart: once per transfer gives 10 + 1*4 = 14; once per endpoint would give 24 and
        // once per cell 44.
        SpatialRecord spatial = record(8, 8, 1, 10, layout(1, 0, 0, 1, 1), layout(2, 4, 0, 1, 1));
        assertPublishable(spatial);

        assertEquals(14, duration(spatial, 1, 2));
        assertEquals(14, duration(spatial, 2, 1));
    }

    // ---- Zero magnitudes -------------------------------------------------------------------------

    @Test
    void zeroTicksPerCellLeavesOnlyHandlingWhateverTheDistance() {
        SpatialRecord spatial = record(10, 10, 0, 7,
                layout(1, 0, 0, 1, 1), layout(2, 1, 0, 1, 1), layout(3, 9, 9, 1, 1));
        assertPublishable(spatial);

        assertEquals(7, duration(spatial, 1, 2)); // 1 cell
        assertEquals(7, duration(spatial, 1, 3)); // 18 cells
    }

    @Test
    void zeroHandlingTicksLeavesOnlyTravel() {
        SpatialRecord spatial = record(10, 10, 3, 0, layout(1, 0, 0, 1, 1), layout(2, 2, 3, 1, 1));
        assertPublishable(spatial);

        assertEquals(15, duration(spatial, 1, 2));
    }

    @Test
    void zeroMagnitudesGiveAZeroTickTransferBetweenDistinctResources() {
        SpatialRecord spatial = record(10, 10, 0, 0, layout(1, 0, 0, 1, 1), layout(2, 9, 9, 1, 1));
        assertPublishable(spatial);

        assertEquals(0, duration(spatial, 1, 2));
        assertEquals(0, duration(spatial, 2, 1));
    }

    // ---- Refusals --------------------------------------------------------------------------------

    @Test
    void sameResourceRequestIsRefusedRatherThanAnsweredWithHandling() {
        SpatialRecord spatial = record(5, 5, 2, 10, layout(1, 0, 0, 1, 1), layout(2, 3, 3, 1, 1));
        SpatialRecord zeroMagnitudes =
                record(5, 5, 0, 0, layout(1, 0, 0, 1, 1), layout(2, 3, 3, 1, 1));

        IllegalArgumentException refused =
                assertThrows(IllegalArgumentException.class, () -> duration(spatial, 1, 1));
        assertTrue(refused.getMessage().contains("same resource"), refused::getMessage);
        assertThrows(IllegalArgumentException.class, () -> duration(zeroMagnitudes, 2, 2));
    }

    @Test
    void resourceWithoutALayoutIsRefusedAsSourceOrDestination() {
        SpatialRecord spatial = record(5, 5, 2, 10, layout(1, 0, 0, 1, 1), layout(2, 3, 3, 1, 1));

        IllegalArgumentException unknownSource =
                assertThrows(IllegalArgumentException.class, () -> duration(spatial, 9, 2));
        assertTrue(
                unknownSource.getMessage().contains("source resource Machine(9)"),
                unknownSource::getMessage);

        IllegalArgumentException unknownDestination =
                assertThrows(IllegalArgumentException.class, () -> duration(spatial, 1, 9));
        assertTrue(
                unknownDestination.getMessage().contains("destination resource Machine(9)"),
                unknownDestination::getMessage);
    }

    @Test
    void missingArgumentsAreRefused() {
        SpatialRecord spatial = record(5, 5, 2, 10, layout(1, 0, 0, 1, 1), layout(2, 3, 3, 1, 1));
        MachineId first = new MachineId(1);
        MachineId second = new MachineId(2);
        ResourcePlacement origin = new ResourcePlacement(0, 0);

        assertThrows(NullPointerException.class,
                () -> TransferTiming.transferDuration(null, first, second));
        assertThrows(NullPointerException.class,
                () -> TransferTiming.transferDuration(spatial, null, second));
        assertThrows(NullPointerException.class,
                () -> TransferTiming.transferDuration(spatial, first, null));
        assertThrows(NullPointerException.class, () -> TransferTiming.manhattanDistance(null, origin));
        assertThrows(NullPointerException.class, () -> TransferTiming.manhattanDistance(origin, null));
    }

    // ---- Overflow and the publication predicate --------------------------------------------------

    @Test
    void largestPublishableDurationIsReturnedExactly() {
        // On a 2x2 floor the predicate's maximum distance is 2, and opposite corners attain it:
        // 1 + 2 * ((2^63 - 2) / 2) = 2^63 - 1.
        long ticksPerCell = (Long.MAX_VALUE - 1) / 2;
        SpatialRecord spatial =
                record(2, 2, ticksPerCell, 1, layout(1, 0, 0, 1, 1), layout(2, 1, 1, 1, 1));
        assertPublishable(spatial);

        assertEquals(Long.MAX_VALUE, duration(spatial, 1, 2));
        assertEquals(Long.MAX_VALUE, duration(spatial, 2, 1));
    }

    @Test
    void recordPastThePublicationPredicateThrowsInsteadOfWrapping() {
        long ticksPerCell = (Long.MAX_VALUE - 1) / 2;
        SpatialRecord handlingOverflows =
                record(2, 2, ticksPerCell, 2, layout(1, 0, 0, 1, 1), layout(2, 1, 1, 1, 1));
        SpatialRecord travelOverflows =
                record(2, 2, Long.MAX_VALUE, 0, layout(1, 0, 0, 1, 1), layout(2, 1, 1, 1, 1));

        for (SpatialRecord spatial : List.of(handlingOverflows, travelOverflows)) {
            assertEquals(
                    List.of("spatial.maxTransferDuration"),
                    validate(spatial).errors().stream().map(ModelValidationError::field).toList());
            assertThrows(ArithmeticException.class, () -> duration(spatial, 1, 2));
            assertThrows(ArithmeticException.class, () -> duration(spatial, 2, 1));
        }
    }

    @Test
    void distanceOverflowOnAnUnvalidatedRecordThrowsInsteadOfWrapping() {
        // The extremes that are representable are returned exactly.
        assertEquals(Long.MAX_VALUE, distance(0, 0, Long.MAX_VALUE, 0));
        assertEquals(Long.MAX_VALUE, distance(Long.MIN_VALUE + 1, 0, 0, 0));

        // An axis difference that is not representable.
        assertThrows(ArithmeticException.class, () -> distance(Long.MIN_VALUE, 0, Long.MAX_VALUE, 0));
        assertThrows(ArithmeticException.class, () -> distance(0, Long.MAX_VALUE, 0, Long.MIN_VALUE));
        // A representable difference whose absolute value is not: Math.abs would stay negative.
        assertThrows(ArithmeticException.class, () -> distance(0, 0, Long.MIN_VALUE, 0));
        assertThrows(ArithmeticException.class, () -> distance(0, 0, 0, Long.MIN_VALUE));
        // Representable axis distances whose sum is not.
        assertThrows(ArithmeticException.class, () -> distance(0, 0, Long.MAX_VALUE, 1));

        // The same refusal surfaces through a record, even when ticksPerCell would zero the travel.
        SpatialRecord unvalidated = record(1, 1, 0, 0,
                layout(1, Long.MIN_VALUE, 0, 1, 1), layout(2, Long.MAX_VALUE, 0, 1, 1));
        assertThrows(ArithmeticException.class, () -> duration(unvalidated, 1, 2));
    }

    // ---- Enumerated oracle -----------------------------------------------------------------------

    @Test
    void everyOrderedPairOnASmallFloorMatchesAnIndependentDerivationWithinThePredicateMaximum() {
        int width = 4;
        int height = 4;
        List<ResourceLayout> layouts = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                layouts.add(layout(layouts.size() + 1, x, y, 1, 1));
            }
        }
        long[][] magnitudes = {
            {0, 0}, {0, 9}, {5, 0}, {1, 1}, {3, 7}, {(Long.MAX_VALUE - 5) / 6, 5},
        };

        int pairs = 0;
        for (long[] magnitude : magnitudes) {
            BigInteger ticksPerCell = BigInteger.valueOf(magnitude[0]);
            BigInteger handlingTicks = BigInteger.valueOf(magnitude[1]);
            SpatialRecord spatial = new SpatialRecord(
                    new FactoryFloor(width, height), magnitude[0], magnitude[1], layouts);
            assertPublishable(spatial);
            BigInteger maximum = handlingTicks.add(
                    ticksPerCell.multiply(BigInteger.valueOf((width - 1) + (height - 1))));

            BigInteger largest = BigInteger.ZERO;
            for (ResourceLayout source : layouts) {
                for (ResourceLayout destination : layouts) {
                    if (source.resourceId().equals(destination.resourceId())) {
                        continue;
                    }
                    long steps = gridSteps(source.placement(), destination.placement());
                    BigInteger expected = handlingTicks.add(ticksPerCell.multiply(BigInteger.valueOf(steps)));
                    BigInteger actual = BigInteger.valueOf(TransferTiming.transferDuration(
                            spatial, source.resourceId(), destination.resourceId()));
                    String pair = source + " -> " + destination + " in " + spatial;

                    assertEquals(steps,
                            TransferTiming.manhattanDistance(source.placement(), destination.placement()),
                            pair);
                    assertEquals(expected, actual, pair);
                    assertTrue(actual.compareTo(maximum) <= 0, pair);
                    largest = largest.max(actual);
                    pairs++;
                }
            }
            // Opposite corners attain the predicate's maximum distance.
            assertEquals(maximum, largest);
        }
        assertEquals(magnitudes.length * 16 * 15, pairs);
    }
}
