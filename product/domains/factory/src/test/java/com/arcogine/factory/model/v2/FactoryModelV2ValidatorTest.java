package com.arcogine.factory.model.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.model.validation.FactoryModelValidationException;
import com.arcogine.factory.model.validation.ModelValidationResult;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FactoryModelV2ValidatorTest {

    private static ConfiguredResource resource(long id) {
        return new ConfiguredResource(new MachineId(id), "Resource " + id, 1, null, 0);
    }

    private static SpatialConfiguredResource spatial(long id, long x, long y, long w, long h) {
        return new SpatialConfiguredResource(
                resource(id), new ResourcePlacement(x, y), new ResourceFootprint(w, h));
    }

    private static OperationDefinition routingOver(long... eligibleIds) {
        Set<MachineId> eligible = new java.util.LinkedHashSet<>();
        for (long id : eligibleIds) {
            eligible.add(new MachineId(id));
        }
        return new OperationDefinition(
                100, "Widget routing", List.of(new OperationStepDefinition(1, "Milling", eligible, 5)));
    }

    private static FactoryModelV2 model(
            long floorW,
            long floorH,
            long ticksPerCell,
            long handlingTicks,
            List<SpatialConfiguredResource> resources) {
        // Every test model below includes a resource with id 1, so the routing step can always
        // reference exactly that one resource without accidentally asserting a spurious
        // referential-integrity error unrelated to the spatial predicate under test.
        return new FactoryModelV2(
                new FactoryFloor(floorW, floorH),
                ticksPerCell,
                handlingTicks,
                resources,
                List.of(routingOver(1)),
                List.of(new ProductDefinition(new ProductId(10), "Widget", 100)));
    }

    private static void assertValid(FactoryModelV2 model) {
        ModelValidationResult result = FactoryModelV2Validator.validate(model);
        assertTrue(result.isValid(), () -> result.errors().toString());
    }

    private static void assertInvalid(FactoryModelV2 model) {
        ModelValidationResult result = FactoryModelV2Validator.validate(model);
        assertFalse(result.isValid());
    }

    // ---- Basic valid model ----------------------------------------------------------------

    @Test
    void validModelWithTwoResourcesHandlingAndOrdinaryV1ContentValidatesDeterministically() {
        FactoryModelV2 model = model(
                10,
                10,
                2,
                3,
                List.of(spatial(1, 0, 0, 2, 2), spatial(2, 5, 5, 1, 1)));

        assertValid(model);
        // Determinism: repeated validation of an equivalent model yields the same result.
        assertValid(model(10, 10, 2, 3, List.of(spatial(1, 0, 0, 2, 2), spatial(2, 5, 5, 1, 1))));
    }

    @Test
    void requireValidThrowsForInvalidModelAndReturnsSilentlyForValidModel() {
        FactoryModelV2 valid = model(5, 5, 0, 0, List.of(spatial(1, 0, 0, 1, 1)));
        FactoryModelV2Validator.requireValid(valid); // must not throw

        FactoryModelV2 invalid = model(0, 5, 0, 0, List.of(spatial(1, 0, 0, 1, 1)));
        assertThrows(FactoryModelValidationException.class, () -> FactoryModelV2Validator.requireValid(invalid));
    }

    // ---- Anchor semantics -------------------------------------------------------------------

    @Test
    void referenceCellIsTheMinimumCoordinateAnchorOfALargerFootprint() {
        // A 3x2 footprint anchored at (2,1) occupies exactly x in [2,4], y in [1,2]: the anchor
        // is the minimum-coordinate corner, not a center point.
        SpatialConfiguredResource wide = spatial(1, 2, 1, 3, 2);

        // Adjacent on the immediately-outside boundary cells must not overlap...
        SpatialConfiguredResource justLeft = spatial(2, 1, 1, 1, 1); // occupies x=1, outside [2,4]
        SpatialConfiguredResource justAbove = spatial(3, 2, 0, 1, 1); // occupies y=0, outside [1,2]
        SpatialConfiguredResource justRight = spatial(4, 5, 1, 1, 1); // occupies x=5, outside [2,4]
        SpatialConfiguredResource justBelow = spatial(5, 2, 3, 1, 1); // occupies y=3, outside [1,2]
        assertValid(model(10, 10, 0, 0, List.of(wide, justLeft)));
        assertValid(model(10, 10, 0, 0, List.of(wide, justAbove)));
        assertValid(model(10, 10, 0, 0, List.of(wide, justRight)));
        assertValid(model(10, 10, 0, 0, List.of(wide, justBelow)));

        // ...but a 1x1 footprint landing on any actually-occupied boundary cell of the 3x2
        // footprint does overlap, proving the occupied set is exactly [2,4]x[1,2].
        SpatialConfiguredResource onMinCorner = spatial(6, 2, 1, 1, 1);
        SpatialConfiguredResource onMaxCorner = spatial(7, 4, 2, 1, 1);
        assertInvalid(model(10, 10, 0, 0, List.of(wide, onMinCorner)));
        assertInvalid(model(10, 10, 0, 0, List.of(wide, onMaxCorner)));
    }

    // ---- Floor boundaries -------------------------------------------------------------------

    @Test
    void minimalOneByOneFloorIsValid() {
        assertValid(model(1, 1, 0, 0, List.of(spatial(1, 0, 0, 1, 1))));
    }

    @Test
    void zeroFloorWidthIsInvalid() {
        assertInvalid(model(0, 5, 0, 0, List.of(spatial(1, 0, 0, 1, 1))));
    }

    @Test
    void zeroFloorHeightIsInvalid() {
        assertInvalid(model(5, 0, 0, 0, List.of(spatial(1, 0, 0, 1, 1))));
    }

    @Test
    void originPositionIsValid() {
        assertValid(model(5, 5, 0, 0, List.of(spatial(1, 0, 0, 1, 1))));
    }

    @Test
    void negativePositionIsInvalid() {
        assertInvalid(model(5, 5, 0, 0, List.of(spatial(1, -1, 0, 1, 1))));
        assertInvalid(model(5, 5, 0, 0, List.of(spatial(1, 0, -1, 1, 1))));
    }

    @Test
    void oneByOneFootprintIsValid() {
        assertValid(model(5, 5, 0, 0, List.of(spatial(1, 2, 2, 1, 1))));
    }

    @Test
    void zeroFootprintWidthIsInvalid() {
        assertInvalid(model(5, 5, 0, 0, List.of(spatial(1, 0, 0, 0, 1))));
    }

    @Test
    void zeroFootprintHeightIsInvalid() {
        assertInvalid(model(5, 5, 0, 0, List.of(spatial(1, 0, 0, 1, 0))));
    }

    @Test
    void placementExactlyOnMaxLegalEdgeIsValid() {
        long floorW = 5;
        long floorH = 5;
        assertValid(model(floorW, floorH, 0, 0, List.of(spatial(1, floorW - 1, floorH - 1, 1, 1))));
    }

    @Test
    void footprintExtendingOneCellOutsideFloorIsInvalid() {
        long floorW = 5;
        long floorH = 5;
        assertInvalid(model(floorW, floorH, 0, 0, List.of(spatial(1, floorW, floorH - 1, 1, 1))));
        assertInvalid(model(floorW, floorH, 0, 0, List.of(spatial(1, floorW - 1, floorH, 1, 1))));
    }

    // ---- Overlap ------------------------------------------------------------------------------

    @Test
    void identicalOverlapIsInvalid() {
        assertInvalid(model(10, 10, 0, 0, List.of(spatial(1, 2, 2, 2, 2), spatial(2, 2, 2, 2, 2))));
    }

    @Test
    void partialHorizontalOverlapIsInvalid() {
        // resource1 occupies x=[0,1]; resource2 occupies x=[1,2]: cell x=1 shared.
        assertInvalid(model(10, 10, 0, 0, List.of(spatial(1, 0, 0, 2, 1), spatial(2, 1, 0, 2, 1))));
    }

    @Test
    void partialVerticalOverlapIsInvalid() {
        // resource1 occupies y=[0,1]; resource2 occupies y=[1,2]: cell y=1 shared.
        assertInvalid(model(10, 10, 0, 0, List.of(spatial(1, 0, 0, 1, 2), spatial(2, 0, 1, 1, 2))));
    }

    @Test
    void containmentOverlapIsInvalid() {
        // resource2's single cell lies entirely inside resource1's larger footprint.
        assertInvalid(model(10, 10, 0, 0, List.of(spatial(1, 0, 0, 5, 5), spatial(2, 2, 2, 1, 1))));
    }

    @Test
    void edgeAdjacentFootprintsDoNotOverlap() {
        // resource1 x=0 width=1 (cell 0); resource2 x=1 width=1 (cell 1): adjacent, not
        // overlapping.
        assertValid(model(10, 10, 0, 0, List.of(spatial(1, 0, 0, 1, 1), spatial(2, 1, 0, 1, 1))));
        // Same, vertically.
        assertValid(model(10, 10, 0, 0, List.of(spatial(1, 0, 0, 1, 1), spatial(2, 0, 1, 1, 1))));
    }

    @Test
    void cornerAdjacentFootprintsDoNotOverlap() {
        assertValid(model(10, 10, 0, 0, List.of(spatial(1, 0, 0, 1, 1), spatial(2, 1, 1, 1, 1))));
    }

    // ---- Handling zeros -----------------------------------------------------------------------

    @Test
    void ticksPerCellZeroAndHandlingTicksZeroAreBothLegal() {
        assertValid(model(5, 5, 0, 0, List.of(spatial(1, 0, 0, 1, 1))));
    }

    @Test
    void ticksPerCellZeroIndependentlyIsLegal() {
        assertValid(model(5, 5, 0, 7, List.of(spatial(1, 0, 0, 1, 1))));
    }

    @Test
    void handlingTicksZeroIndependentlyIsLegal() {
        assertValid(model(5, 5, 4, 0, List.of(spatial(1, 0, 0, 1, 1))));
    }

    @Test
    void negativeTicksPerCellIsInvalid() {
        assertInvalid(model(5, 5, -1, 0, List.of(spatial(1, 0, 0, 1, 1))));
    }

    @Test
    void negativeHandlingTicksIsInvalid() {
        assertInvalid(model(5, 5, 0, -1, List.of(spatial(1, 0, 0, 1, 1))));
    }

    // ---- Maximum-transfer-duration arithmetic --------------------------------------------------

    @Test
    void zeroDistanceOnAOneByOneFloorIsRepresentable() {
        assertValid(model(1, 1, Long.MAX_VALUE, 5, List.of(spatial(1, 0, 0, 1, 1))));
    }

    @Test
    void multiplicationByZeroManhattanDistanceIsRepresentableEvenWithLargeTicksPerCell() {
        assertValid(model(1, 1, Long.MAX_VALUE, Long.MAX_VALUE, List.of(spatial(1, 0, 0, 1, 1))));
    }

    @Test
    void exactlyRepresentableLargeResultIsValid() {
        // floor 1000x1000 -> maxManhattanDistance = 999 + 999 = 1998.
        // ticksPerCell * 1998 + handlingTicks chosen to land exactly at Long.MAX_VALUE.
        long maxManhattanDistance = 999 + 999;
        long ticksPerCell = 1_000_000L;
        long handlingContribution = ticksPerCell * maxManhattanDistance;
        long handlingTicks = Long.MAX_VALUE - handlingContribution;

        assertValid(model(1000, 1000, ticksPerCell, handlingTicks, List.of(spatial(1, 0, 0, 1, 1))));
    }

    @Test
    void multiplicationOverflowIsRejected() {
        // maxManhattanDistance = 1 -> ticksPerCell * 1 does not overflow by itself, so use a
        // floor large enough that maxManhattanDistance itself is huge and the multiplication by a
        // large ticksPerCell overflows.
        long floorW = 4_000_000_000L;
        long floorH = 4_000_000_000L; // maxManhattanDistance ~= 8e9
        assertInvalid(model(floorW, floorH, Long.MAX_VALUE / 2, 0, List.of(spatial(1, 0, 0, 1, 1))));
    }

    @Test
    void finalAdditionOverflowIsRejected() {
        // maxManhattanDistance = 0 (1x1 floor) so the multiplication cannot overflow, but adding
        // handlingTicks to a maximal ticksPerCell*0=0 contribution still can't overflow here --
        // instead force the final addExact(handlingTicks, contribution) to overflow by using a
        // nonzero distance with a contribution just under Long.MAX_VALUE and a handlingTicks that
        // pushes the sum past it.
        long floorW = 3; // maxManhattanDistance contribution from width = 2
        long floorH = 1; // contribution from height = 0 -> maxManhattanDistance = 2
        long ticksPerCell = Long.MAX_VALUE / 2; // *2 stays just within range
        long handlingContribution = ticksPerCell * 2;
        long handlingTicks = Long.MAX_VALUE - handlingContribution + 1; // one past representable

        assertInvalid(model(floorW, floorH, ticksPerCell, handlingTicks, List.of(spatial(1, 0, 0, 1, 1))));
    }

    @Test
    void maxManhattanDistanceCalculationOverflowIsRejected() {
        // (W-1) + (H-1) overflows long when both W and H are near Long.MAX_VALUE.
        long floorW = Long.MAX_VALUE;
        long floorH = Long.MAX_VALUE;
        assertInvalid(model(floorW, floorH, 0, 0, List.of(spatial(1, 0, 0, 1, 1))));
    }

    // ---- Mandatory V2 data ----------------------------------------------------------------------

    @Test
    void everyMandatoryV2SemanticFactIsRequiredByConstruction() {
        // floor, and each resource's placement/footprint, are non-null object fields: a
        // FactoryModelV2 or SpatialConfiguredResource literally cannot be constructed without
        // supplying them.
        assertThrows(NullPointerException.class,
                () -> new FactoryModelV2(null, 0, 0, List.of(), List.of(), List.of()));
        assertThrows(NullPointerException.class,
                () -> new SpatialConfiguredResource(resource(1), null, new ResourceFootprint(1, 1)));
        assertThrows(NullPointerException.class,
                () -> new SpatialConfiguredResource(resource(1), new ResourcePlacement(0, 0), null));
        assertThrows(NullPointerException.class,
                () -> new SpatialConfiguredResource(null, new ResourcePlacement(0, 0), new ResourceFootprint(1, 1)));

        // ticksPerCell/handlingTicks are primitive longs: every construction site must supply an
        // explicit value (zero is a legal explicit value, not an absent one).
        assertValid(model(5, 5, 0, 0, List.of(spatial(1, 0, 0, 1, 1))));
    }

    // ---- V2 does not weaken underlying V1-shaped structural validation -------------------------

    @Test
    void v2ValidationStillCatchesV1ShapedStructuralErrors() {
        OperationDefinition badRouting = new OperationDefinition(
                100,
                "Widget routing",
                List.of(new OperationStepDefinition(1, "Milling", Set.of(new MachineId(999)), 5)));
        FactoryModelV2 badModel = new FactoryModelV2(
                new FactoryFloor(5, 5),
                0,
                0,
                List.of(spatial(1, 0, 0, 1, 1)),
                List.of(badRouting),
                List.of(new ProductDefinition(new ProductId(10), "Widget", 100)));

        assertInvalid(badModel);
    }

    @Test
    void invalidModelReportsMultipleDeterministicErrorsRatherThanFailingFast() {
        FactoryModelV2 badModel = model(0, 0, -1, -1, List.of(spatial(1, -1, -1, 0, 0)));

        ModelValidationResult first = FactoryModelV2Validator.validate(badModel);
        ModelValidationResult second = FactoryModelV2Validator.validate(badModel);

        assertFalse(first.isValid());
        assertTrue(first.errors().size() > 1);
        assertEquals(first.errors(), second.errors());
    }
}
