package com.arcogine.factory.model;

import java.util.List;

/** Immutable semantic definition of a designed production system. */
public record FactoryModel(
        int schemaVersion,
        List<ProductDefinition> products,
        List<OperationDefinition> operations,
        List<OperationStepDefinition> operationSteps,
        List<ResourceDefinition> resourceDefinitions,
        List<ResourceInstance> resourceInstances) {

    public static final int CURRENT_SCHEMA_VERSION = 1;

    public FactoryModel {
        products = List.copyOf(products);
        operations = List.copyOf(operations);
        operationSteps = List.copyOf(operationSteps);
        resourceDefinitions = List.copyOf(resourceDefinitions);
        resourceInstances = List.copyOf(resourceInstances);
    }

    public record ProductDefinition(long id, String name, long operationDefinitionId) {}

    public record OperationDefinition(long id, String name, List<Long> stepIds) {
        public OperationDefinition {
            stepIds = List.copyOf(stepIds);
        }
    }

    public record OperationStepDefinition(
            long id,
            String name,
            long duration,
            List<Long> eligibleResourceInstanceIds) {
        public OperationStepDefinition {
            eligibleResourceInstanceIds = List.copyOf(eligibleResourceInstanceIds);
        }
    }

    public record ResourceDefinition(
            long id,
            String name,
            int concurrency,
            Double capacityLiters,
            long setupTime) {}

    public record ResourceInstance(long id, long resourceDefinitionId) {}
}
