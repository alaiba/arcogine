package com.arcogine.factory.model.v2;

import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.validation.FactoryModelValidationException;
import com.arcogine.factory.model.validation.FactoryModelValidator;
import com.arcogine.factory.model.validation.ModelValidationError;
import com.arcogine.factory.model.validation.ModelValidationResult;
import com.arcogine.types.MachineId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Deterministic structural validation of a {@link FactoryModelV2}, performed before the model may
 * be published (once V2 publication exists) or used to construct runtime state.
 *
 * <p>This delegates the V1-shaped checks on the production records (duplicate ids, referential
 * integrity, Unicode validity, and so on) to {@link FactoryModelValidator} via
 * {@link FactoryModelV2#baseModel()}. When the spatial record is absent that is the whole
 * validation: an absent record asserts nothing, so no spatial predicate applies and none is
 * evaluated against synthesized values. When the record is present, this adds the predicates
 * docs/architecture/factory-model-v2.md requires of it: exactly one layout per configured
 * resource in resource-list order, floor extent, handling-value non-negativity, per-resource
 * placement/footprint range and floor containment, cross-resource footprint non-overlap, and the
 * maximum transfer-duration representability predicate.
 *
 * <p>Cross-resource predicates such as coverage and non-overlap are evaluated here, at the model
 * validation boundary -- never inside an individual value-object constructor, which could never
 * see the rest of the model. This mirrors the existing V1 {@link FactoryModelValidator}'s style:
 * structural/business validation lives at the model boundary, not scattered through record
 * constructors, and every violation is collected into one deterministic, ordered result rather
 * than failing fast on the first problem found.
 */
public final class FactoryModelV2Validator {

    private static final String LAYOUTS = "spatial.resourceLayouts";

    private FactoryModelV2Validator() {}

    public static ModelValidationResult validate(FactoryModelV2 model) {
        List<ModelValidationError> errors =
                new ArrayList<>(FactoryModelValidator.validate(model.baseModel()).errors());
        model.spatial().ifPresent(spatial -> validateSpatial(model.resources(), spatial, errors));
        return new ModelValidationResult(errors);
    }

    /**
     * Validates {@code model} and throws {@link FactoryModelValidationException} if it is
     * structurally invalid.
     */
    public static void requireValid(FactoryModelV2 model) {
        ModelValidationResult result = validate(model);
        if (!result.isValid()) {
            throw new FactoryModelValidationException(result);
        }
    }

    private static void validateSpatial(
            List<ConfiguredResource> resources, SpatialRecord spatial, List<ModelValidationError> errors) {
        boolean[] covered = validateCoverage(resources, spatial.resourceLayouts(), errors);
        boolean floorValid = validateFloor(spatial.floor(), errors);
        boolean handlingValid = validateHandling(spatial, errors);
        boolean[] footprintValid = validateLayouts(spatial, covered, floorValid, errors);
        validateNonOverlap(spatial.resourceLayouts(), footprintValid, errors);

        if (floorValid && handlingValid) {
            validateMaxTransferDuration(spatial, errors);
        }
    }

    /**
     * Checks that a present spatial record places every configured resource exactly once, places
     * no resource the model does not contain, and lists its layouts in resource-list order, so a
     * present record is always complete rather than partial. Returns, per layout index, whether
     * that layout is the one coverage-valid entry for an existing resource and may therefore take
     * part in the pairwise non-overlap check; an unknown or repeated entry is excluded there so it
     * cannot produce a spurious overlap finding.
     */
    private static boolean[] validateCoverage(
            List<ConfiguredResource> resources,
            List<ResourceLayout> layouts,
            List<ModelValidationError> errors) {
        Set<MachineId> resourceIds = new HashSet<>();
        for (ConfiguredResource resource : resources) {
            resourceIds.add(resource.id());
        }

        Set<MachineId> placed = new HashSet<>();
        boolean[] covered = new boolean[layouts.size()];
        for (int index = 0; index < layouts.size(); index++) {
            MachineId resourceId = layouts.get(index).resourceId();
            if (!resourceIds.contains(resourceId)) {
                errors.add(new ModelValidationError(
                        LAYOUTS, "references nonexistent resource id: " + resourceId));
            } else if (!placed.add(resourceId)) {
                errors.add(new ModelValidationError(
                        LAYOUTS, "duplicate layout for resource id: " + resourceId));
            } else {
                covered[index] = true;
            }
        }

        boolean complete = true;
        for (ConfiguredResource resource : resources) {
            if (!placed.contains(resource.id())) {
                errors.add(new ModelValidationError(
                        LAYOUTS, "missing layout for resource id: " + resource.id()));
                complete = false;
            }
        }

        if (complete && layouts.size() == resources.size()) {
            for (int index = 0; index < resources.size(); index++) {
                if (!layouts.get(index).resourceId().equals(resources.get(index).id())) {
                    errors.add(new ModelValidationError(LAYOUTS, "must follow resource list order"));
                    break;
                }
            }
        }
        return covered;
    }

    private static boolean validateFloor(FactoryFloor floor, List<ModelValidationError> errors) {
        boolean valid = true;
        if (floor.width() < 1) {
            errors.add(new ModelValidationError("spatial.floor.width", "must be >= 1"));
            valid = false;
        }
        if (floor.height() < 1) {
            errors.add(new ModelValidationError("spatial.floor.height", "must be >= 1"));
            valid = false;
        }
        return valid;
    }

    private static boolean validateHandling(SpatialRecord spatial, List<ModelValidationError> errors) {
        boolean valid = true;
        if (spatial.ticksPerCell() < 0) {
            errors.add(new ModelValidationError("spatial.ticksPerCell", "must be >= 0"));
            valid = false;
        }
        if (spatial.handlingTicks() < 0) {
            errors.add(new ModelValidationError("spatial.handlingTicks", "must be >= 0"));
            valid = false;
        }
        return valid;
    }

    /**
     * Validates each layout's own placement/footprint ranges and, when the floor itself is valid,
     * its containment inside the floor. Returns, per layout index, whether that layout is
     * coverage-valid and its placement/footprint valid enough to participate in the non-overlap
     * check -- an already-invalid footprint (for example negative or zero extent) is excluded
     * there so it cannot produce a spurious or overflow-risking overlap comparison.
     */
    private static boolean[] validateLayouts(
            SpatialRecord spatial,
            boolean[] covered,
            boolean floorValid,
            List<ModelValidationError> errors) {
        List<ResourceLayout> layouts = spatial.resourceLayouts();
        boolean[] valid = new boolean[layouts.size()];
        for (int index = 0; index < layouts.size(); index++) {
            ResourceLayout layout = layouts.get(index);
            String field = LAYOUTS + "[" + layout.resourceId() + "]";
            long x = layout.placement().x();
            long y = layout.placement().y();
            long width = layout.footprint().width();
            long height = layout.footprint().height();

            boolean layoutValid = true;
            if (x < 0) {
                errors.add(new ModelValidationError(field + ".position.x", "must be >= 0"));
                layoutValid = false;
            }
            if (y < 0) {
                errors.add(new ModelValidationError(field + ".position.y", "must be >= 0"));
                layoutValid = false;
            }
            if (width < 1) {
                errors.add(new ModelValidationError(field + ".footprint.width", "must be >= 1"));
                layoutValid = false;
            }
            if (height < 1) {
                errors.add(new ModelValidationError(field + ".footprint.height", "must be >= 1"));
                layoutValid = false;
            }

            if (layoutValid && floorValid) {
                // Overflow-safe equivalent of `x + width <= floor.width`: floor.width and width
                // are both already known to be >= 1, so `floor.width - width` -- a difference of
                // two values in [1, Long.MAX_VALUE] -- is always representable as a long, unlike
                // `x + width`, which could overflow before it is ever compared to floor.width.
                if (x > spatial.floor().width() - width) {
                    errors.add(new ModelValidationError(
                            field + ".footprint",
                            "extends outside floor width " + spatial.floor().width()));
                    layoutValid = false;
                }
                if (y > spatial.floor().height() - height) {
                    errors.add(new ModelValidationError(
                            field + ".footprint",
                            "extends outside floor height " + spatial.floor().height()));
                    layoutValid = false;
                }
            }

            valid[index] = covered[index] && layoutValid;
        }
        return valid;
    }

    /**
     * Checks every distinct pair of layouts whose own placement/footprint already validated for
     * pairwise footprint overlap, per the Factory Model v2 specification's occupied-cell semantics.
     */
    private static void validateNonOverlap(
            List<ResourceLayout> layouts, boolean[] footprintValid, List<ModelValidationError> errors) {
        for (int i = 0; i < layouts.size(); i++) {
            if (!footprintValid[i]) {
                continue;
            }
            for (int j = i + 1; j < layouts.size(); j++) {
                if (!footprintValid[j]) {
                    continue;
                }
                ResourceLayout a = layouts.get(i);
                ResourceLayout b = layouts.get(j);
                if (overlaps(a, b)) {
                    errors.add(new ModelValidationError(
                            LAYOUTS,
                            "resource " + a.resourceId() + " footprint overlaps resource "
                                    + b.resourceId()));
                }
            }
        }
    }

    /**
     * Two rectangular footprints {@code [x1, x1+w1-1] x [y1, y1+h1-1]} and
     * {@code [x2, x2+w2-1] x [y2, y2+h2-1]} overlap iff their projections onto both axes overlap.
     * The textbook interval-overlap test compares sums (for example {@code x1 < x2 + w2}), which
     * risks overflow; this instead compares the algebraically equivalent difference form
     * ({@code x1 - x2 < w2}). Every position reaching this method has already been validated
     * {@code >= 0} by {@link #validateLayouts}, so each difference of two non-negative longs lies
     * within {@code [-Long.MAX_VALUE, Long.MAX_VALUE]} and can never overflow {@code long}.
     */
    private static boolean overlaps(ResourceLayout a, ResourceLayout b) {
        long ax = a.placement().x();
        long ay = a.placement().y();
        long aw = a.footprint().width();
        long ah = a.footprint().height();
        long bx = b.placement().x();
        long by = b.placement().y();
        long bw = b.footprint().width();
        long bh = b.footprint().height();

        boolean overlapsHorizontally = (ax - bx) < bw && (bx - ax) < aw;
        boolean overlapsVertically = (ay - by) < bh && (by - ay) < ah;
        return overlapsHorizontally && overlapsVertically;
    }

    /**
     * Applies the Factory Model v2 specification's maximum-transfer-duration representability
     * predicate:
     *
     * <pre>
     * maxManhattanDistance = (W - 1) + (H - 1)
     * maxTransferDuration  = handlingTicks + ticksPerCell * maxManhattanDistance
     * </pre>
     *
     * <p>Every subtraction, addition, and multiplication is evaluated with {@link Math}'s
     * checked/exact arithmetic, so an overflow throws {@link ArithmeticException} rather than
     * silently wrapping; the predicate is then reported as failed instead of being evaluated
     * against a wrapped or otherwise incorrect magnitude. Passing this predicate proves the
     * derived transfer-duration magnitude is representable in {@code long} -- the runtime
     * tick-duration type used by durations and simulated time elsewhere in this model and the
     * runtime. It does not, and is not claimed to, prove that adding an arbitrarily extreme
     * current simulated time to an otherwise valid duration can never overflow; that pre-existing
     * condition remains the runtime's own responsibility.
     */
    private static void validateMaxTransferDuration(
            SpatialRecord spatial, List<ModelValidationError> errors) {
        try {
            long maxWidthDistance = Math.subtractExact(spatial.floor().width(), 1);
            long maxHeightDistance = Math.subtractExact(spatial.floor().height(), 1);
            long maxManhattanDistance = Math.addExact(maxWidthDistance, maxHeightDistance);
            long handlingContribution = Math.multiplyExact(spatial.ticksPerCell(), maxManhattanDistance);
            Math.addExact(spatial.handlingTicks(), handlingContribution);
        } catch (ArithmeticException overflow) {
            errors.add(new ModelValidationError(
                    "spatial.maxTransferDuration",
                    "maximum transfer duration is not representable in the tick-duration type"));
        }
    }
}
