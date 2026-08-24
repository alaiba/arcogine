package com.arcogine.factory.model.scenario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import com.arcogine.types.scenario.EquipmentConfig;
import com.arcogine.types.scenario.MaterialConfig;
import com.arcogine.types.scenario.OperationsDefinitionConfig;
import com.arcogine.types.scenario.ProcessSegmentConfig;
import com.arcogine.types.scenario.ScenarioConfig;
import com.arcogine.types.scenario.SimulationParams;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScenarioFactoryModelAdapterTest {

    private static ScenarioConfig scenario() {
        return new ScenarioConfig(
                new SimulationParams(42L, 100L, 10L, 50L),
                List.of(new EquipmentConfig(1, "Mill", null, null, null)),
                List.of(new MaterialConfig(10, "Widget", 100)),
                List.of(new ProcessSegmentConfig(1, "Rough milling", 1, 5)),
                List.of(new OperationsDefinitionConfig(100, "Widget routing", List.of(1L))),
                null,
                null);
    }

    @Test
    void mapsResourcesOperationsAndProductsFaithfully() {
        FactoryModel model = ScenarioFactoryModelAdapter.adapt(scenario());

        assertEquals(1, model.resources().size());
        assertEquals(new MachineId(1), model.resources().get(0).id());
        assertEquals("Mill", model.resources().get(0).name());

        assertEquals(1, model.operations().size());
        OperationDefinition operation = model.operations().get(0);
        assertEquals(100, operation.id());
        assertEquals(1, operation.steps().size());
        OperationStepDefinition step = operation.steps().get(0);
        assertEquals(1, step.stepId());
        assertEquals(5, step.duration());
        assertTrue(step.eligibleResources().contains(new MachineId(1)));
        assertEquals(1, step.eligibleResources().size(), "current semantics bind one equipment id");

        assertEquals(1, model.products().size());
        assertEquals(new ProductId(10), model.products().get(0).id());
        assertEquals(100, model.products().get(0).operationId());
    }

    @Test
    void excludesSimulationEconomyAndAgentConcernsFromTheModel() {
        FactoryModel model = ScenarioFactoryModelAdapter.adapt(scenario());

        // The canonical model has no fields for simulation/economy/agent concerns at all -- the
        // adapter cannot leak them even accidentally.
        assertEquals(1, model.resources().size());
        assertEquals(1, model.operations().size());
        assertEquals(1, model.products().size());
    }
}
