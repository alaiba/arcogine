package com.arcogine.factory.model.v2;

import com.arcogine.factory.model.validation.FactoryModelValidationException;
import com.arcogine.factory.model.validation.FactoryModelValidator;
import com.arcogine.factory.model.validation.ModelValidationError;
import com.arcogine.factory.model.validation.ModelValidationResult;
import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic structural validation of a {@link FactoryModelV2}, performed before the model may
 * be published (once V2 publication exists) or used to construct runtime state.
 *
 * <p>This delegates the existing {@code factory-model:v1}-shaped structural checks (duplicate
 * ids, referential integrity, Unicode validity, and so on) to {@link FactoryModelValidator} via
 * {@link FactoryModelV2#baseModel()}, then adds the V2-only spatial/handling predicates required
 * by ADR-0014: floor extent, per-resource placement/footprint range and floor containment,
 * cross-resource footprint non-overlap, handling-value non-negativity, and the maximum
 * transfer-duration representability predicate.
 *
 * <p>Cross-resource predicates such as non-overlap are evaluated here, at the model validation
 * boundary -- never inside an individual resource's value-object constructor, which could never
 * see the rest of the model. This mirrors the existing V1 {@link FactoryModelValidator}'s style:
 * structural/business validation lives at the model boundary, not scattered through record
 * constructors, and every violation is collected into one deterministic, ordered result rather
 * than failing fast on the first problem found.
 */
public final class FactoryModelV2Validator {

    private FactoryModelV2Validator() {}

    public static ModelValidationResult validate(FactoryModelV2 model) {
        List<ModelValidationError> errors =
                new ArrayList<>(FactoryModelValidator.validate(model.baseModel()).errors());

        boolean floorValid = validateFloor(model.floor(), errors);
        boolean handlingValid = validateHandling(model, errors);
        boolean[] footprintValid = validateResources(model, floorValid, errors);
        validateNonOverlap(model.resources(), footprintValid, errors);

        if (floorValid && handlingValid) {
            validateMaxTransferDuration(model, errors);
        }

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

    private static boolean validateFloor(FactoryFloor floor, List<ModelValidationError> errors) {
        boolean valid = true;
        if (floor.width() < 1) {
            errors.add(new ModelValidationError("floor.width", "must be >= 1"));
            valid = false;
        }
        if (floor.height() < 1) {
            errors.add(new ModelValidationError("floor.height", "must be >= 1"));
            valid = false;
        }
        return valid;
    }

    private static boolean validateHandling(FactoryModelV2 model, List<ModelValidationError> errors) {
        boolean valid = true;
        if (model.ticksPerCell() < 0) {
            errors.add(new ModelValidationError("ticksPerCell", "must be >= 0"));
            valid = false;
        }
        if (model.handlingTicks() < 0) {
            errors.add(new ModelValidationError("handlingTicks", "must be >= 0"));
            valid = false;
        }
        return valid;
    }

    /**
     * Validates each resource's own placement/footprint ranges and, when the floor itself is
     * valid, its containment inside the floor. Returns, per resource index, whether that
     * resource's placement/footprint is valid enough to participate in the non-overlap check --
     * an already-invalid footprint (for example negative or zero extent) is excluded there so it
     * cannot produce a spurious or overflow-risking overlap comparison.
     */
    private static boolean[] validateResources(
            FactoryModelV2 model, boolean floorValid, List<ModelValidationError> errors) {
        List<SpatialConfiguredResource> resources = model.resources();
        boolean[] valid = new boolean[resources.size()];
        for (int index = 0; index < resources.size(); index++) {
            SpatialConfiguredResource resource = resources.get(index);
            String field = "resources[" + resource.resource().id() + "]";
            long x = resource.placement().x();
            long y = resource.placement().y();
            long width = resource.footprint().width();
            long height = resource.footprint().height();

            boolean resourceValid = true;
            if (x < 0) {
                errors.add(new ModelValidationError(field + ".position.x", "must be >= 0"));
                resourceValid = false;
            }
            if (y < 0) {
                errors.add(new ModelValidationError(field + ".position.y", "must be >= 0"));
                resourceValid = false;
            }
            if (width < 1) {
                errors.add(new ModelValidationError(field + ".footprint.width", "must be >= 1"));
                resourceValid = false;
            }
            if (height < 1) {
                errors.add(new ModelValidationError(field + ".footprint.height", "must be >= 1"));
                resourceValid = false;
            }

            if (resourceValid && floorValid) {
                // Overflow-safe equivalent of `x + width <= floor.width`: floor.width and width
                // are both already known to be >= 1, so `floor.width - width` -- a difference of
                // two values in [1, Long.MAX_VALUE] -- is always representable as a long, unlike
                // `x + width`, which could overflow before it is ever compared to floor.width.
                if (x > model.floor().width() - width) {
                    errors.add(new ModelValidationError(
                            field + ".footprint",
                            "extends outside floor width " + model.floor().width()));
                    resourceValid = false;
                }
                if (y > model.floor().height() - height) {
                    errors.add(new ModelValidationError(
                            field + ".footprint",
                            "extends outside floor height " + model.floor().height()));
                    resourceValid = false;
                }
            }

            valid[index] = resourceValid;
        }
        return valid;
    }

    /**
     * Checks every distinct pair of resources whose own placement/footprint already validated
     * for pairwise footprint overlap, per ADR-0014's occupied-cell semantics.
     */
    private static void validateNonOverlap(
            List<SpatialConfiguredResource> resources,
            boolean[] footprintValid,
            List<ModelValidationError> errors) {
        for (int i = 0; i < resources.size(); i++) {
            if (!footprintValid[i]) {
                continue;
            }
            for (int j = i + 1; j < resources.size(); j++) {
                if (!footprintValid[j]) {
                    continue;
                }
                SpatialConfiguredResource a = resources.get(i);
                SpatialConfiguredResource b = resources.get(j);
                if (overlaps(a, b)) {
                    errors.add(new ModelValidationError(
                            "resources",
                            "resource " + a.resource().id() + " footprint overlaps resource "
                                    + b.resource().id()));
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
     * {@code >= 0} by {@link #validateResources}, so each difference of two non-negative longs
     * lies within {@code [-Long.MAX_VALUE, Long.MAX_VALUE]} and can never overflow {@code long}.
     */
    private static boolean overlaps(SpatialConfiguredResource a, SpatialConfiguredResource b) {
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
     * Applies ADR-0014's maximum-transfer-duration representability predicate:
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
            FactoryModelV2 model, List<ModelValidationError> errors) {
        try {
            long maxWidthDistance = Math.subtractExact(model.floor().width(), 1);
            long maxHeightDistance = Math.subtractExact(model.floor().height(), 1);
            long maxManhattanDistance = Math.addExact(maxWidthDistance, maxHeightDistance);
            long handlingContribution = Math.multiplyExact(model.ticksPerCell(), maxManhattanDistance);
            Math.addExact(model.handlingTicks(), handlingContribution);
        } catch (ArithmeticException overflow) {
            errors.add(new ModelValidationError(
                    "maxTransferDuration",
                    "maximum transfer duration is not representable in the tick-duration type"));
        }
    }
}
