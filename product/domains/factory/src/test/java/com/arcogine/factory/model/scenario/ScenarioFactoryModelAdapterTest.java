package com.arcogine.factory.model.scenario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import com.arcogine.factory.model.FactoryModel;
import com.arcogine.types.scenario.AgentConfig;
import com.arcogine.types.scenario.EconomyConfig;
import com.arcogine.types.scenario.EquipmentConfig;
import com.arcogine.types.scenario.MaterialConfig;
import com.arcogine.types.scenario.OperationsDefinitionConfig;
import com.arcogine.types.scenario.ProcessSegmentConfig;
import com.arcogine.types.scenario.ScenarioConfig;
import com.arcogine.types.scenario.SimulationParams;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScenarioFactoryModelAdapterTest {

    @Test
    void adaptsOnlyFactorySemanticsAndPreservesCurrentResourceBinding() {
        ScenarioConfig first = scenario(
                new SimulationParams(42, 100, null, null),
                new EconomyConfig(5.0, 3.0, 0.3, 0.05),
                new AgentConfig(false, "sales"));
        ScenarioConfig differentRunContext = scenario(
                new SimulationParams(99, 999, 1L, 2L),
                new EconomyConfig(50.0, 30.0, 3.0, 0.5),
                new AgentConfig(true, "other"));

        FactoryModel model = ScenarioFactoryModelAdapter.fromScenario(first);
        FactoryModel sameFactory = ScenarioFactoryModelAdapter.fromScenario(differentRunContext);

        assertEquals(sameFactory, model);
        assertEquals(FactoryModel.CURRENT_SCHEMA_VERSION, model.schemaVersion());
        assertEquals(1, model.products().size());
        assertEquals(30L, model.products().getFirst().operationDefinitionId());
        assertEquals(List.of(40L), model.operations().getFirst().stepIds());
        assertEquals(List.of(10L), model.operationSteps().getFirst().eligibleResourceInstanceIds());
        assertEquals(2, model.resourceDefinitions().getFirst().concurrency());
        assertEquals(10L, model.resourceInstances().getFirst().resourceDefinitionId());
    }

    @Test
    void producesAnImmutableSnapshotOfScenarioLists() {
        List<Long> mutableSteps = new ArrayList<>(List.of(40L));
        ScenarioConfig config = new ScenarioConfig(
                new SimulationParams(42, 100, null, null),
                List.of(new EquipmentConfig(10, "Mill", 2, 15.0, 3L)),
                List.of(new MaterialConfig(20, "Widget", 30)),
                List.of(new ProcessSegmentConfig(40, "Milling", 10, 5)),
                List.of(new OperationsDefinitionConfig(30, "Widget routing", mutableSteps)),
                null,
                null);

        FactoryModel model = ScenarioFactoryModelAdapter.fromScenario(config);
        mutableSteps.add(41L);

        assertEquals(List.of(40L), model.operations().getFirst().stepIds());
        assertNotSame(mutableSteps, model.operations().getFirst().stepIds());
    }

    private static ScenarioConfig scenario(
            SimulationParams simulation, EconomyConfig economy, AgentConfig agent) {
        return new ScenarioConfig(
                simulation,
                List.of(new EquipmentConfig(10, "Mill", 2, 15.0, 3L)),
                List.of(new MaterialConfig(20, "Widget", 30)),
                List.of(new ProcessSegmentConfig(40, "Milling", 10, 5)),
                List.of(new OperationsDefinitionConfig(30, "Widget routing", List.of(40L))),
                economy,
                agent);
    }
}
