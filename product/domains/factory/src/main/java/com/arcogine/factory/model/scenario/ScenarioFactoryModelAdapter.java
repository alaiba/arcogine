package com.arcogine.factory.model.scenario;

import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.model.ResourceDefinition;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import com.arcogine.types.scenario.EquipmentConfig;
import com.arcogine.types.scenario.MaterialConfig;
import com.arcogine.types.scenario.OperationsDefinitionConfig;
import com.arcogine.types.scenario.ScenarioConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Adapts a {@link ScenarioConfig} (today's TOML-derived scenario/run input envelope) into a
 * canonical {@link FactoryModel}.
 *
 * <p>This is deliberately a narrow, faithful translation: it preserves today's semantics exactly
 * and does not introduce broader behavior the current scenario shape does not express. In
 * particular, a {@code process_segment} binds to exactly one {@code equipment_id}, so each
 * resulting {@link OperationStepDefinition} has a single-member eligibility set -- this is not
 * capability-based dispatch.
 *
 * <p>Only factory-design concerns are read from the scenario: equipment, material, process
 * segments, and operations definitions. Simulation, economy, and agent configuration are
 * execution/run concerns and are never mapped into the model (see ADR-0003).
 */
public final class ScenarioFactoryModelAdapter {

    private ScenarioFactoryModelAdapter() {}

    public static FactoryModel adapt(ScenarioConfig config) {
        List<ResourceDefinition> resources = new ArrayList<>();
        for (EquipmentConfig eq : config.equipment()) {
            resources.add(new ResourceDefinition(
                    new MachineId(eq.id()),
                    eq.name(),
                    eq.effectiveConcurrency(),
                    eq.capacityLiters(),
                    eq.effectiveSetupTime()));
        }

        List<OperationDefinition> operations = new ArrayList<>();
        for (OperationsDefinitionConfig od : config.operationsDefinition()) {
            List<OperationStepDefinition> steps = new ArrayList<>();
            for (Long segId : od.steps()) {
                config.processSegment().stream()
                        .filter(s -> s.id() == segId)
                        .findFirst()
                        .ifPresent(s -> steps.add(new OperationStepDefinition(
                                s.id(),
                                s.name(),
                                Set.of(new MachineId(s.equipmentId())),
                                s.duration())));
            }
            operations.add(new OperationDefinition(od.id(), od.name(), steps));
        }

        List<ProductDefinition> products = new ArrayList<>();
        for (MaterialConfig mat : config.material()) {
            products.add(new ProductDefinition(new ProductId(mat.id()), mat.name(), mat.routingId()));
        }

        return new FactoryModel(resources, operations, products);
    }
}
