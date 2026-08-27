package com.arcogine.factory.model.validation;

import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.model.ResourceDefinition;
import com.arcogine.types.MachineId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Deterministic structural validation of a {@link FactoryModel}, performed before the model may
 * be published or used to construct runtime state.
 *
 * <p>This checks only structural/referential invariants (uniqueness, resolvable references,
 * coherent resource/operation relationships) -- it does not partially construct or mutate any
 * runtime state.
 */
public final class FactoryModelValidator {

    private FactoryModelValidator() {}

    public static ModelValidationResult validate(FactoryModel model) {
        List<ModelValidationError> errors = new ArrayList<>();

        Set<MachineId> resourceIds = new HashSet<>();
        for (ResourceDefinition resource : model.resources()) {
            rejectMalformedUnicode(errors, "resources[" + resource.id() + "].name", resource.name());
            if (!resourceIds.add(resource.id())) {
                errors.add(new ModelValidationError(
                        "resources", "duplicate resource id: " + resource.id()));
            }
            if (resource.concurrency() <= 0) {
                errors.add(new ModelValidationError(
                        "resources[" + resource.id() + "].concurrency", "must be > 0"));
            }
        }

        Set<Long> operationIds = new HashSet<>();
        for (OperationDefinition operation : model.operations()) {
            rejectMalformedUnicode(errors, "operations[" + operation.id() + "].name", operation.name());
            if (!operationIds.add(operation.id())) {
                errors.add(new ModelValidationError(
                        "operations", "duplicate operation id: " + operation.id()));
            }
            if (operation.steps().isEmpty()) {
                errors.add(new ModelValidationError(
                        "operations[" + operation.id() + "].steps", "must not be empty"));
            }
            for (OperationStepDefinition step : operation.steps()) {
                rejectMalformedUnicode(
                        errors,
                        "operations[" + operation.id() + "].steps[" + step.stepId() + "].name",
                        step.name());
                if (step.duration() <= 0) {
                    errors.add(new ModelValidationError(
                            "operations[" + operation.id() + "].steps[" + step.stepId() + "].duration",
                            "must be > 0"));
                }
                if (step.eligibleResources().isEmpty()) {
                    errors.add(new ModelValidationError(
                            "operations[" + operation.id() + "].steps[" + step.stepId()
                                    + "].eligibleResources",
                            "must reference at least one resource"));
                    continue;
                }
                for (MachineId resourceId : step.eligibleResources()) {
                    if (!resourceIds.contains(resourceId)) {
                        errors.add(new ModelValidationError(
                                "operations[" + operation.id() + "].steps[" + step.stepId()
                                        + "].eligibleResources",
                                "references nonexistent resource id: " + resourceId));
                    }
                }
            }
        }

        Set<com.arcogine.types.ProductId> productIds = new HashSet<>();
        for (ProductDefinition product : model.products()) {
            rejectMalformedUnicode(errors, "products[" + product.id() + "].name", product.name());
            if (!productIds.add(product.id())) {
                errors.add(new ModelValidationError(
                        "products", "duplicate product id: " + product.id()));
            }
            if (!operationIds.contains(product.operationId())) {
                errors.add(new ModelValidationError(
                        "products[" + product.id() + "].operationId",
                        "references nonexistent operation id: " + product.operationId()));
            }
        }

        return new ModelValidationResult(errors);
    }

    private static void rejectMalformedUnicode(
            List<ModelValidationError> errors, String field, String value) {
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (Character.isHighSurrogate(character)) {
                if (index + 1 >= value.length() || !Character.isLowSurrogate(value.charAt(index + 1))) {
                    errors.add(new ModelValidationError(field, "must contain only valid Unicode scalar values"));
                    return;
                }
                index++;
            } else if (Character.isLowSurrogate(character)) {
                errors.add(new ModelValidationError(field, "must contain only valid Unicode scalar values"));
                return;
            }
        }
    }

    /**
     * Validates {@code model} and throws {@link FactoryModelValidationException} if it is
     * structurally invalid.
     */
    public static void requireValid(FactoryModel model) {
        ModelValidationResult result = validate(model);
        if (!result.isValid()) {
            throw new FactoryModelValidationException(result);
        }
    }
}
