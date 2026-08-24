package com.arcogine.factory.model.scenario;

import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModel.OperationDefinition;
import com.arcogine.factory.model.FactoryModel.OperationStepDefinition;
import com.arcogine.factory.model.FactoryModel.ProductDefinition;
import com.arcogine.factory.model.FactoryModel.ResourceDefinition;
import com.arcogine.factory.model.FactoryModel.ResourceInstance;
import com.arcogine.types.scenario.ScenarioConfig;

/** Adapts factory semantics out of the broader scenario/run input envelope. */
public final class ScenarioFactoryModelAdapter {

    private ScenarioFactoryModelAdapter() {}

    public static FactoryModel fromScenario(ScenarioConfig config) {
        var products = config.material().stream()
                .map(material -> new ProductDefinition(
                        material.id(), material.name(), material.routingId()))
                .toList();

        var operations = config.operationsDefinition().stream()
                .map(operation -> new OperationDefinition(
                        operation.id(), operation.name(), operation.steps()))
                .toList();

        var operationSteps = config.processSegment().stream()
                .map(segment -> new OperationStepDefinition(
                        segment.id(),
                        segment.name(),
                        segment.duration(),
                        java.util.List.of(segment.equipmentId())))
                .toList();

        var resourceDefinitions = config.equipment().stream()
                .map(equipment -> new ResourceDefinition(
                        equipment.id(),
                        equipment.name(),
                        equipment.effectiveConcurrency(),
                        equipment.capacityLiters(),
                        equipment.effectiveSetupTime()))
                .toList();

        var resourceInstances = config.equipment().stream()
                .map(equipment -> new ResourceInstance(equipment.id(), equipment.id()))
                .toList();

        return new FactoryModel(
                FactoryModel.CURRENT_SCHEMA_VERSION,
                products,
                operations,
                operationSteps,
                resourceDefinitions,
                resourceInstances);
    }
}
