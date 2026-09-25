package com.arcogine.factory.model.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Publication predicates of the optional spatial record, applied by {@link FactoryModelValidator}
 * (docs/architecture/factory-model.md).
 */
class SpatialRecordValidationTest {

    private static ConfiguredResource resource(long id) {
        return new ConfiguredResource(new MachineId(id), "Resource " + id, 1, null, 0);
    }

    private static ResourceLayout layout(long id, long x, long y, long w, long h) {
        return new ResourceLayout(
                new MachineId(id), new ResourcePlacement(x, y), new ResourceFootprint(w, h));
    }

    private static OperationDefinition routingOver(long... eligibleIds) {
        Set<MachineId> eligible = new java.util.LinkedHashSet<>();
        for (long id : eligibleIds) {
            eligible.add(new MachineId(id));
        }
        return new OperationDefinition(
                100, "Widget routing", List.of(new OperationStepDefinition(1, "Milling", eligible, 5)));
    }

    private static List<ProductDefinition> widget() {
        return List.of(new ProductDefinition(new ProductId(10), "Widget", 100));
    }

    private static FactoryModel withSpatial(List<ConfiguredResource> resources, SpatialRecord spatial) {
        return new FactoryModel(resources, List.of(routingOver(1)), widget(), Optional.of(spatial));
    }

    private static FactoryModel withoutSpatial(List<ConfiguredResource> resources) {
        return new FactoryModel(resources, List.of(routingOver(1)), widget(), Optional.empty());
    }

    /**
     * A model whose spatial record places exactly the resources it declares, in order -- one
     * configured resource per layout id -- so tests of the spatial predicates below never trip an
     * unrelated coverage finding. Every model includes a resource with id 1, so the routing step
     * can reference it without a spurious referential-integrity error.
     */
    private static FactoryModel model(
            long floorW,
            long floorH,
            long ticksPerCell,
            long handlingTicks,
            List<ResourceLayout> layouts) {
        List<ConfiguredResource> resources =
                layouts.stream().map(layout -> resource(layout.resourceId().value())).toList();
        return withSpatial(
                resources,
                new SpatialRecord(new FactoryFloor(floorW, floorH), ticksPerCell, handlingTicks, layouts));
    }

    private static void assertValid(FactoryModel model) {
        ModelValidationResult result = FactoryModelValidator.validate(model);
        assertTrue(result.isValid(), () -> result.errors().toString());
    }

    private static void assertInvalid(FactoryModel model) {
        ModelValidationResult result = FactoryModelValidator.validate(model);
        assertFalse(result.isValid());
    }

    private static void assertCoverageError(FactoryModel model, String message) {
        ModelValidationResult result = FactoryModelValidator.validate(model);
        assertTrue(
                result.errors().contains(new ModelValidationError("spatial.resourceLayouts", message)),
                () -> result.errors().toString());
    }

    // ---- Basic valid model ----------------------------------------------------------------

    @Test
    void validModelWithTwoResourcesHandlingAndOrdinaryProductionContentValidatesDeterministically() {
        FactoryModel model = model(10, 10, 2, 3, List.of(layout(1, 0, 0, 2, 2), layout(2, 5, 5, 1, 1)));

        assertValid(model);
        // Determinism: repeated validation of an equivalent model yields the same result.
        assertValid(model(10, 10, 2, 3, List.of(layout(1, 0, 0, 2, 2), layout(2, 5, 5, 1, 1))));
    }

    @Test
    void requireValidThrowsForInvalidModelAndReturnsSilentlyForValidModel() {
        FactoryModel valid = model(5, 5, 0, 0, List.of(layout(1, 0, 0, 1, 1)));
        FactoryModelValidator.requireValid(valid); // must not throw

        FactoryModel invalid = model(0, 5, 0, 0, List.of(layout(1, 0, 0, 1, 1)));
        assertThrows(FactoryModelValidationException.class, () -> FactoryModelValidator.requireValid(invalid));
    }

    // ---- Absent spatial record ----------------------------------------------------------------

    @Test
    void productionOnlyModelWithAbsentSpatialRecordIsValid() {
        assertValid(withoutSpatial(List.of(resource(1), resource(2))));
    }

    @Test
    void absentSpatialRecordValidatesProductionSemanticsOnly() {
        // An absent record asserts nothing, so no spatial predicate is evaluated against
        // synthesized values; the production records are still fully validated.
        FactoryModel badProduction = new FactoryModel(
                List.of(new ConfiguredResource(new MachineId(1), "Resource 1", 0, null, 0)),
                List.of(routingOver(999)),
                widget(),
                Optional.empty());

        ModelValidationResult result = FactoryModelValidator.validate(badProduction);

        assertFalse(result.isValid());
        assertTrue(
                result.errors().stream().noneMatch(error -> error.field().startsWith("spatial")),
                () -> result.errors().toString());
    }

    @Test
    void absentSpatialRecordIsDistinctFromPresentRecordWithLegalZeroMagnitudes() {
        List<ConfiguredResource> resources = List.of(resource(1));
        FactoryModel absent = withoutSpatial(resources);
        FactoryModel presentWithZero = withSpatial(
                resources,
                new SpatialRecord(new FactoryFloor(1, 1), 0, 0, List.of(layout(1, 0, 0, 1, 1))));

        // Both are publishable designs, and they share their production records...
        assertValid(absent);
        assertValid(presentWithZero);
        assertEquals(absent.resources(), presentWithZero.resources());
        assertEquals(absent.operations(), presentWithZero.operations());
        assertEquals(absent.products(), presentWithZero.products());
        // ...but zero magnitudes at the origin are authored facts, not a spelling of "no spatial
        // record": the two are different designs.
        assertNotEquals(absent, presentWithZero);
        assertTrue(absent.spatial().isEmpty());
        assertEquals(0, presentWithZero.spatial().orElseThrow().ticksPerCell());
        assertEquals(0, presentWithZero.spatial().orElseThrow().handlingTicks());
    }

    // ---- Present spatial record must be complete ----------------------------------------------

    @Test
    void presentRecordMustPlaceEveryConfiguredResource() {
        FactoryModel missing = withSpatial(
                List.of(resource(1), resource(2)),
                new SpatialRecord(new FactoryFloor(5, 5), 0, 0, List.of(layout(1, 0, 0, 1, 1))));

        assertInvalid(missing);
        assertCoverageError(missing, "missing layout for resource id: " + new MachineId(2));
    }

    @Test
    void presentRecordWithoutAnyLayoutIsPartialAndInvalid() {
        FactoryModel headerOnly = withSpatial(
                List.of(resource(1)), new SpatialRecord(new FactoryFloor(5, 5), 0, 0, List.of()));

        assertInvalid(headerOnly);
        assertCoverageError(headerOnly, "missing layout for resource id: " + new MachineId(1));
    }

    @Test
    void presentRecordMustNotPlaceAnUnknownResource() {
        FactoryModel unknown = withSpatial(
                List.of(resource(1)),
                new SpatialRecord(
                        new FactoryFloor(5, 5), 0, 0, List.of(layout(1, 0, 0, 1, 1), layout(9, 3, 3, 1, 1))));

        assertInvalid(unknown);
        assertCoverageError(unknown, "references nonexistent resource id: " + new MachineId(9));
    }

    @Test
    void presentRecordMustNotPlaceAResourceTwice() {
        // The repeated entry is identical to the first, so a duplicate is caught as a coverage
        // defect rather than hidden as, or confused with, a self-overlap.
        FactoryModel duplicate = withSpatial(
                List.of(resource(1), resource(2)),
                new SpatialRecord(
                        new FactoryFloor(5, 5),
                        0,
                        0,
                        List.of(layout(1, 0, 0, 1, 1), layout(1, 0, 0, 1, 1), layout(2, 2, 2, 1, 1))));

        ModelValidationResult result = FactoryModelValidator.validate(duplicate);

        assertFalse(result.isValid());
        assertEquals(
                List.of(new ModelValidationError(
                        "spatial.resourceLayouts", "duplicate layout for resource id: " + new MachineId(1))),
                result.errors());
    }

    @Test
    void presentRecordLayoutsFollowResourceListOrder() {
        List<ConfiguredResource> resources = List.of(resource(1), resource(2));
        FactoryModel reordered = withSpatial(
                resources,
                new SpatialRecord(
                        new FactoryFloor(5, 5), 0, 0, List.of(layout(2, 2, 2, 1, 1), layout(1, 0, 0, 1, 1))));

        assertInvalid(reordered);
        assertCoverageError(reordered, "must follow resource list order");
        assertValid(withSpatial(
                resources,
                new SpatialRecord(
                        new FactoryFloor(5, 5), 0, 0, List.of(layout(1, 0, 0, 1, 1), layout(2, 2, 2, 1, 1)))));
    }

    // ---- Anchor semantics -------------------------------------------------------------------

    @Test
    void referenceCellIsTheMinimumCoordinateAnchorOfALargerFootprint() {
        // A 3x2 footprint anchored at (2,1) occupies exactly x in [2,4], y in [1,2]: the anchor
        // is the minimum-coordinate corner, not a center point.
        ResourceLayout wide = layout(1, 2, 1, 3, 2);

        // Adjacent on the immediately-outside boundary cells must not overlap...
        ResourceLayout justLeft = layout(2, 1, 1, 1, 1); // occupies x=1, outside [2,4]
        ResourceLayout justAbove = layout(3, 2, 0, 1, 1); // occupies y=0, outside [1,2]
        ResourceLayout justRight = layout(4, 5, 1, 1, 1); // occupies x=5, outside [2,4]
        ResourceLayout justBelow = layout(5, 2, 3, 1, 1); // occupies y=3, outside [1,2]
        assertValid(model(10, 10, 0, 0, List.of(wide, justLeft)));
        assertValid(model(10, 10, 0, 0, List.of(wide, justAbove)));
        assertValid(model(10, 10, 0, 0, List.of(wide, justRight)));
        assertValid(model(10, 10, 0, 0, List.of(wide, justBelow)));

        // ...but a 1x1 footprint landing on any actually-occupied boundary cell of the 3x2
        // footprint does overlap, proving the occupied set is exactly [2,4]x[1,2].
        ResourceLayout onMinCorner = layout(6, 2, 1, 1, 1);
        ResourceLayout onMaxCorner = layout(7, 4, 2, 1, 1);
        assertInvalid(model(10, 10, 0, 0, List.of(wide, onMinCorner)));
        assertInvalid(model(10, 10, 0, 0, List.of(wide, onMaxCorner)));
    }

    // ---- Floor boundaries -------------------------------------------------------------------

    @Test
    void minimalOneByOneFloorIsValid() {
        assertValid(model(1, 1, 0, 0, List.of(layout(1, 0, 0, 1, 1))));
    }

    @Test
    void zeroFloorWidthIsInvalid() {
        assertInvalid(model(0, 5, 0, 0, List.of(layout(1, 0, 0, 1, 1))));
    }

    @Test
    void zeroFloorHeightIsInvalid() {
        assertInvalid(model(5, 0, 0, 0, List.of(layout(1, 0, 0, 1, 1))));
    }

    @Test
    void originPositionIsValid() {
        assertValid(model(5, 5, 0, 0, List.of(layout(1, 0, 0, 1, 1))));
    }

    @Test
    void negativePositionIsInvalid() {
        assertInvalid(model(5, 5, 0, 0, List.of(layout(1, -1, 0, 1, 1))));
        assertInvalid(model(5, 5, 0, 0, List.of(layout(1, 0, -1, 1, 1))));
    }

    @Test
    void oneByOneFootprintIsValid() {
        assertValid(model(5, 5, 0, 0, List.of(layout(1, 2, 2, 1, 1))));
    }

    @Test
    void zeroFootprintWidthIsInvalid() {
        assertInvalid(model(5, 5, 0, 0, List.of(layout(1, 0, 0, 0, 1))));
    }

    @Test
    void zeroFootprintHeightIsInvalid() {
        assertInvalid(model(5, 5, 0, 0, List.of(layout(1, 0, 0, 1, 0))));
    }

    @Test
    void placementExactlyOnMaxLegalEdgeIsValid() {
        long floorW = 5;
        long floorH = 5;
        assertValid(model(floorW, floorH, 0, 0, List.of(layout(1, floorW - 1, floorH - 1, 1, 1))));
    }

    @Test
    void footprintExtendingOneCellOutsideFloorIsInvalid() {
        long floorW = 5;
        long floorH = 5;
        assertInvalid(model(floorW, floorH, 0, 0, List.of(layout(1, floorW, floorH - 1, 1, 1))));
        assertInvalid(model(floorW, floorH, 0, 0, List.of(layout(1, floorW - 1, floorH, 1, 1))));
    }

    // ---- Overlap ------------------------------------------------------------------------------

    @Test
    void identicalOverlapIsInvalid() {
        assertInvalid(model(10, 10, 0, 0, List.of(layout(1, 2, 2, 2, 2), layout(2, 2, 2, 2, 2))));
    }

    @Test
    void partialHorizontalOverlapIsInvalid() {
        // resource1 occupies x=[0,1]; resource2 occupies x=[1,2]: cell x=1 shared.
        assertInvalid(model(10, 10, 0, 0, List.of(layout(1, 0, 0, 2, 1), layout(2, 1, 0, 2, 1))));
    }

    @Test
    void partialVerticalOverlapIsInvalid() {
        // resource1 occupies y=[0,1]; resource2 occupies y=[1,2]: cell y=1 shared.
        assertInvalid(model(10, 10, 0, 0, List.of(layout(1, 0, 0, 1, 2), layout(2, 0, 1, 1, 2))));
    }

    @Test
    void containmentOverlapIsInvalid() {
        // resource2's single cell lies entirely inside resource1's larger footprint.
        assertInvalid(model(10, 10, 0, 0, List.of(layout(1, 0, 0, 5, 5), layout(2, 2, 2, 1, 1))));
    }

    @Test
    void edgeAdjacentFootprintsDoNotOverlap() {
        // resource1 x=0 width=1 (cell 0); resource2 x=1 width=1 (cell 1): adjacent, not
        // overlapping.
        assertValid(model(10, 10, 0, 0, List.of(layout(1, 0, 0, 1, 1), layout(2, 1, 0, 1, 1))));
        // Same, vertically.
        assertValid(model(10, 10, 0, 0, List.of(layout(1, 0, 0, 1, 1), layout(2, 0, 1, 1, 1))));
    }

    @Test
    void cornerAdjacentFootprintsDoNotOverlap() {
        assertValid(model(10, 10, 0, 0, List.of(layout(1, 0, 0, 1, 1), layout(2, 1, 1, 1, 1))));
    }

    // ---- Handling zeros -----------------------------------------------------------------------

    @Test
    void ticksPerCellZeroAndHandlingTicksZeroAreBothLegal() {
        assertValid(model(5, 5, 0, 0, List.of(layout(1, 0, 0, 1, 1))));
    }

    @Test
    void ticksPerCellZeroIndependentlyIsLegal() {
        assertValid(model(5, 5, 0, 7, List.of(layout(1, 0, 0, 1, 1))));
    }

    @Test
    void handlingTicksZeroIndependentlyIsLegal() {
        assertValid(model(5, 5, 4, 0, List.of(layout(1, 0, 0, 1, 1))));
    }

    @Test
    void negativeTicksPerCellIsInvalid() {
        assertInvalid(model(5, 5, -1, 0, List.of(layout(1, 0, 0, 1, 1))));
    }

    @Test
    void negativeHandlingTicksIsInvalid() {
        assertInvalid(model(5, 5, 0, -1, List.of(layout(1, 0, 0, 1, 1))));
    }

    // ---- Maximum-transfer-duration arithmetic --------------------------------------------------

    @Test
    void zeroDistanceOnAOneByOneFloorIsRepresentable() {
        assertValid(model(1, 1, Long.MAX_VALUE, 5, List.of(layout(1, 0, 0, 1, 1))));
    }

    @Test
    void multiplicationByZeroManhattanDistanceIsRepresentableEvenWithLargeTicksPerCell() {
        assertValid(model(1, 1, Long.MAX_VALUE, Long.MAX_VALUE, List.of(layout(1, 0, 0, 1, 1))));
    }

    @Test
    void exactlyRepresentableLargeResultIsValid() {
        // floor 1000x1000 -> maxManhattanDistance = 999 + 999 = 1998.
        // ticksPerCell * 1998 + handlingTicks chosen to land exactly at Long.MAX_VALUE.
        long maxManhattanDistance = 999 + 999;
        long ticksPerCell = 1_000_000L;
        long handlingContribution = ticksPerCell * maxManhattanDistance;
        long handlingTicks = Long.MAX_VALUE - handlingContribution;

        assertValid(model(1000, 1000, ticksPerCell, handlingTicks, List.of(layout(1, 0, 0, 1, 1))));
    }

    @Test
    void multiplicationOverflowIsRejected() {
        // A floor large enough that maxManhattanDistance itself is huge, so multiplying it by a
        // large ticksPerCell overflows.
        long floorW = 4_000_000_000L;
        long floorH = 4_000_000_000L; // maxManhattanDistance ~= 8e9
        assertInvalid(model(floorW, floorH, Long.MAX_VALUE / 2, 0, List.of(layout(1, 0, 0, 1, 1))));
    }

    @Test
    void finalAdditionOverflowIsRejected() {
        // A nonzero distance with a contribution just under Long.MAX_VALUE and a handlingTicks that
        // pushes the final addExact(handlingTicks, contribution) past it.
        long floorW = 3; // maxManhattanDistance contribution from width = 2
        long floorH = 1; // contribution from height = 0 -> maxManhattanDistance = 2
        long ticksPerCell = Long.MAX_VALUE / 2; // *2 stays just within range
        long handlingContribution = ticksPerCell * 2;
        long handlingTicks = Long.MAX_VALUE - handlingContribution + 1; // one past representable

        assertInvalid(model(floorW, floorH, ticksPerCell, handlingTicks, List.of(layout(1, 0, 0, 1, 1))));
    }

    @Test
    void maxManhattanDistanceCalculationOverflowIsRejectedEvenWithZeroTicksPerCell() {
        // (W-1) + (H-1) overflows long when both W and H are near Long.MAX_VALUE. The predicate
        // rejects that intermediate even though multiplying it by ticksPerCell = 0 would yield 0.
        long floorW = Long.MAX_VALUE;
        long floorH = Long.MAX_VALUE;
        assertInvalid(model(floorW, floorH, 0, 0, List.of(layout(1, 0, 0, 1, 1))));
    }

    // ---- Spatial validation does not weaken production-record validation ------------------------

    @Test
    void spatialValidationStillCatchesProductionRecordErrors() {
        FactoryModel badModel = new FactoryModel(
                List.of(resource(1)),
                List.of(routingOver(999)),
                widget(),
                Optional.of(new SpatialRecord(new FactoryFloor(5, 5), 0, 0, List.of(layout(1, 0, 0, 1, 1)))));

        assertInvalid(badModel);
    }

    @Test
    void invalidModelReportsMultipleDeterministicErrorsRatherThanFailingFast() {
        FactoryModel badModel = model(0, 0, -1, -1, List.of(layout(1, -1, -1, 0, 0)));

        ModelValidationResult first = FactoryModelValidator.validate(badModel);
        ModelValidationResult second = FactoryModelValidator.validate(badModel);

        assertFalse(first.isValid());
        assertTrue(first.errors().size() > 1);
        assertEquals(first.errors(), second.errors());
    }
}
