package com.arcogine.factory.process;

import com.arcogine.factory.model.spatial.ResourceLayout;
import com.arcogine.factory.model.spatial.ResourcePlacement;
import com.arcogine.factory.model.spatial.SpatialRecord;
import com.arcogine.types.MachineId;
import java.util.Objects;

/**
 * The Engine's transfer-timing arithmetic over a published {@link SpatialRecord}
 * ({@code docs/architecture/engine-semantics.md} §7):
 *
 * <pre>
 * manhattanDistance = abs(xDestination - xSource) + abs(yDestination - ySource)
 * transferDuration  = handlingTicks + ticksPerCell * manhattanDistance
 * </pre>
 *
 * <p>Distance is measured between the two resources' minimum-coordinate reference cells; footprint
 * extent never enters it. {@code handlingTicks} is applied once per transfer, not once per endpoint
 * or per cell. A transfer exists only between distinct resources: consecutive operations on the
 * same resource perform no transfer and have no duration, so a same-resource request is refused
 * rather than answered with {@code handlingTicks}. Zero magnitudes are legal and give their
 * arithmetic result, so a duration of {@code 0} between distinct resources is a valid transfer
 * duration, not the absence of a transfer.
 *
 * <p>Every step uses {@link Math}'s exact operations: an overflow throws
 * {@link ArithmeticException} instead of wrapping, saturating or going negative. For a published
 * record the Factory model's maximum-transfer-duration predicate already proves every result
 * representable; this class does not re-check that or any other publication predicate (coordinate
 * range, footprint containment, non-overlap, layout coverage). It computes a duration only; adding
 * it to a simulated time remains the runtime's time-addition guard's concern.
 *
 * <p>The functions are pure and stateless. No runtime path calls them: the current runtime refuses
 * a published model whose spatial record is present before any runtime state exists.
 */
final class TransferTiming {

    private TransferTiming() {}

    /** Manhattan distance between two reference cells, with exact arithmetic. */
    static long manhattanDistance(ResourcePlacement source, ResourcePlacement destination) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(destination, "destination");
        long dx = Math.absExact(Math.subtractExact(destination.x(), source.x()));
        long dy = Math.absExact(Math.subtractExact(destination.y(), source.y()));
        return Math.addExact(dx, dy);
    }

    /**
     * The transfer duration, in ticks, from {@code source} to a distinct {@code destination}
     * resource placed in {@code spatial}.
     *
     * @throws IllegalArgumentException if {@code source} equals {@code destination}, or if either
     *     resource has no layout in {@code spatial}
     * @throws ArithmeticException if the result is not representable as a {@code long}
     */
    static long transferDuration(SpatialRecord spatial, MachineId source, MachineId destination) {
        Objects.requireNonNull(spatial, "spatial");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(destination, "destination");
        if (source.equals(destination)) {
            throw new IllegalArgumentException(
                    "no transfer exists between operations on the same resource: " + source);
        }
        long distance = manhattanDistance(
                placementOf(spatial, source, "source"),
                placementOf(spatial, destination, "destination"));
        long travel = Math.multiplyExact(spatial.ticksPerCell(), distance);
        return Math.addExact(spatial.handlingTicks(), travel);
    }

    private static ResourcePlacement placementOf(
            SpatialRecord spatial, MachineId resource, String role) {
        for (ResourceLayout layout : spatial.resourceLayouts()) {
            if (layout.resourceId().equals(resource)) {
                return layout.placement();
            }
        }
        throw new IllegalArgumentException(
                role + " resource " + resource + " has no layout in the spatial record");
    }
}
