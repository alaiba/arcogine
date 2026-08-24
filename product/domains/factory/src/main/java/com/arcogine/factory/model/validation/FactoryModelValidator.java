package com.arcogine.factory.model.validation;

import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModel.OperationDefinition;
import com.arcogine.factory.model.FactoryModel.OperationStepDefinition;
import com.arcogine.factory.model.FactoryModel.ProductDefinition;
import com.arcogine.factory.model.FactoryModel.ResourceDefinition;
import com.arcogine.factory.model.FactoryModel.ResourceInstance;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Deterministic executability validation for canonical factory models. */
public final class FactoryModelValidator {

    private FactoryModelValidator() {}

    public enum Severity {
        ERROR,
        WARNING
    }

    public record Finding(
            String code,
            Severity severity,
            String message,
            String entityType,
            long entityId,
            String path,
            List<Long> relatedIds) {
        public Finding {
            relatedIds = List.copyOf(relatedIds);
        }
    }

    public record Result(List<Finding> findings) {
        public Result {
            findings = List.copyOf(findings);
        }

        public boolean isValid() {
            return findings.stream().noneMatch(finding -> finding.severity() == Severity.ERROR);
        }
    }

    public static Result validate(FactoryModel model) {
        List<Finding> findings = new ArrayList<>();

        validateProductIds(model.products(), findings);
        validateOperationIds(model.operations(), findings);
        validateOperationStepIds(model.operationSteps(), findings);
        validateResourceDefinitionIds(model.resourceDefinitions(), findings);
        validateResourceInstanceIds(model.resourceInstances(), findings);
        validateResourceValues(model.resourceDefinitions(), findings);
        validateOperationStepValues(model.operationSteps(), findings);
        validateReferences(model, findings);

        return new Result(findings);
    }

    private static void validateProductIds(List<ProductDefinition> products, List<Finding> findings) {
        Set<Long> seen = new HashSet<>();
        for (ProductDefinition product : products) {
            if (!seen.add(product.id())) {
                findings.add(error(
                        "DUPLICATE_PRODUCT_ID",
                        "product",
                        product.id(),
                        "id",
                        "Duplicate product identifier",
                        List.of(product.id())));
            }
        }
    }

    private static void validateOperationIds(List<OperationDefinition> operations, List<Finding> findings) {
        Set<Long> seen = new HashSet<>();
        for (OperationDefinition operation : operations) {
            if (!seen.add(operation.id())) {
                findings.add(error(
                        "DUPLICATE_OPERATION_ID",
                        "operation",
                        operation.id(),
                        "id",
                        "Duplicate operation identifier",
                        List.of(operation.id())));
            }
        }
    }

    private static void validateOperationStepIds(
            List<OperationStepDefinition> operationSteps, List<Finding> findings) {
        Set<Long> seen = new HashSet<>();
        for (OperationStepDefinition step : operationSteps) {
            if (!seen.add(step.id())) {
                findings.add(error(
                        "DUPLICATE_OPERATION_STEP_ID",
                        "operation_step",
                        step.id(),
                        "id",
                        "Duplicate operation-step identifier",
                        List.of(step.id())));
            }
        }
    }

    private static void validateResourceDefinitionIds(
            List<ResourceDefinition> resourceDefinitions, List<Finding> findings) {
        Set<Long> seen = new HashSet<>();
        for (ResourceDefinition definition : resourceDefinitions) {
            if (!seen.add(definition.id())) {
                findings.add(error(
                        "DUPLICATE_RESOURCE_DEFINITION_ID",
                        "resource_definition",
                        definition.id(),
                        "id",
                        "Duplicate resource-definition identifier",
                        List.of(definition.id())));
            }
        }
    }

    private static void validateResourceInstanceIds(
            List<ResourceInstance> resourceInstances, List<Finding> findings) {
        Set<Long> seen = new HashSet<>();
        for (ResourceInstance instance : resourceInstances) {
            if (!seen.add(instance.id())) {
                findings.add(error(
                        "DUPLICATE_RESOURCE_INSTANCE_ID",
                        "resource_instance",
                        instance.id(),
                        "id",
                        "Duplicate resource-instance identifier",
                        List.of(instance.id())));
            }
        }
    }

    private static void validateResourceValues(
            List<ResourceDefinition> resourceDefinitions, List<Finding> findings) {
        for (ResourceDefinition definition : resourceDefinitions) {
            if (definition.concurrency() <= 0) {
                findings.add(error(
                        "INVALID_RESOURCE_CONCURRENCY",
                        "resource_definition",
                        definition.id(),
                        "concurrency",
                        "Resource concurrency must be greater than zero",
                        List.of()));
            }
            if (definition.capacityLiters() != null && definition.capacityLiters() <= 0.0) {
                findings.add(error(
                        "INVALID_RESOURCE_CAPACITY",
                        "resource_definition",
                        definition.id(),
                        "capacityLiters",
                        "Resource capacity must be greater than zero when specified",
                        List.of()));
            }
            if (definition.setupTime() < 0) {
                findings.add(error(
                        "INVALID_RESOURCE_SETUP_TIME",
                        "resource_definition",
                        definition.id(),
                        "setupTime",
                        "Resource setup time must not be negative",
                        List.of()));
            }
        }
    }

    private static void validateOperationStepValues(
            List<OperationStepDefinition> operationSteps, List<Finding> findings) {
        for (OperationStepDefinition step : operationSteps) {
            if (step.duration() <= 0) {
                findings.add(error(
                        "INVALID_OPERATION_DURATION",
                        "operation_step",
                        step.id(),
                        "duration",
                        "Operation-step duration must be greater than zero",
                        List.of()));
            }
            if (step.eligibleResourceInstanceIds().isEmpty()) {
                findings.add(error(
                        "NO_ELIGIBLE_RESOURCE",
                        "operation_step",
                        step.id(),
                        "eligibleResourceInstanceIds",
                        "Operation step must have at least one eligible resource instance",
                        List.of()));
            }
        }
    }

    private static void validateReferences(FactoryModel model, List<Finding> findings) {
        Set<Long> operationIds = idsOfOperations(model.operations());
        Set<Long> operationStepIds = idsOfOperationSteps(model.operationSteps());
        Set<Long> resourceDefinitionIds = idsOfResourceDefinitions(model.resourceDefinitions());
        Set<Long> resourceInstanceIds = idsOfResourceInstances(model.resourceInstances());

        for (ProductDefinition product : model.products()) {
            if (!operationIds.contains(product.operationDefinitionId())) {
                findings.add(error(
                        "UNKNOWN_OPERATION",
                        "product",
                        product.id(),
                        "operationDefinitionId",
                        "Product references an unknown operation definition",
                        List.of(product.operationDefinitionId())));
            }
        }

        for (OperationDefinition operation : model.operations()) {
            for (Long stepId : operation.stepIds()) {
                if (!operationStepIds.contains(stepId)) {
                    findings.add(error(
                            "UNKNOWN_OPERATION_STEP",
                            "operation",
                            operation.id(),
                            "stepIds",
                            "Operation references an unknown operation step",
                            List.of(stepId)));
                }
            }
        }

        for (ResourceInstance instance : model.resourceInstances()) {
            if (!resourceDefinitionIds.contains(instance.resourceDefinitionId())) {
                findings.add(error(
                        "UNKNOWN_RESOURCE_DEFINITION",
                        "resource_instance",
                        instance.id(),
                        "resourceDefinitionId",
                        "Resource instance references an unknown resource definition",
                        List.of(instance.resourceDefinitionId())));
            }
        }

        for (OperationStepDefinition step : model.operationSteps()) {
            for (Long resourceId : step.eligibleResourceInstanceIds()) {
                if (!resourceInstanceIds.contains(resourceId)) {
                    findings.add(error(
                            "UNKNOWN_ELIGIBLE_RESOURCE",
                            "operation_step",
                            step.id(),
                            "eligibleResourceInstanceIds",
                            "Operation step references an unknown eligible resource instance",
                            List.of(resourceId)));
                }
            }
        }
    }

    private static Set<Long> idsOfOperations(List<OperationDefinition> operations) {
        Set<Long> ids = new HashSet<>();
        operations.forEach(operation -> ids.add(operation.id()));
        return ids;
    }

    private static Set<Long> idsOfOperationSteps(List<OperationStepDefinition> steps) {
        Set<Long> ids = new HashSet<>();
        steps.forEach(step -> ids.add(step.id()));
        return ids;
    }

    private static Set<Long> idsOfResourceDefinitions(List<ResourceDefinition> definitions) {
        Set<Long> ids = new HashSet<>();
        definitions.forEach(definition -> ids.add(definition.id()));
        return ids;
    }

    private static Set<Long> idsOfResourceInstances(List<ResourceInstance> instances) {
        Set<Long> ids = new HashSet<>();
        instances.forEach(instance -> ids.add(instance.id()));
        return ids;
    }

    private static Finding error(
            String code,
            String entityType,
            long entityId,
            String path,
            String message,
            List<Long> relatedIds) {
        return new Finding(code, Severity.ERROR, message, entityType, entityId, path, relatedIds);
    }
}
