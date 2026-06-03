package com.arcogine.types.scenario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Ported from crates/sim-types/src/scenario.rs default_* and toml tests. */
class ScenarioConfigTest {

    private static TomlMapper tomlMapper() {
        TomlMapper mapper = TomlMapper.builder().build();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    // --- default value tests (Rust default_*_value) ---

    @Test
    void demandIntervalDefault() {
        assertEquals(10L, new SimulationParams(1, 100, null, null).demandInterval());
    }

    @Test
    void agentIntervalDefault() {
        assertEquals(50L, new SimulationParams(1, 100, null, null).agentInterval());
    }

    @Test
    void baseDemandDefault() {
        assertEquals(5.0, new EconomyConfig(10.0, null, null, null).effectiveBaseDemand());
    }

    @Test
    void priceElasticityDefault() {
        assertEquals(0.5, new EconomyConfig(10.0, null, null, null).effectivePriceElasticity());
    }

    @Test
    void leadTimeSensitivityDefault() {
        assertEquals(0.1, new EconomyConfig(10.0, null, null, null).effectiveLeadTimeSensitivity());
    }

    @Test
    void concurrencyDefault() {
        assertEquals(1, new EquipmentConfig(1, "x", null, null, null).effectiveConcurrency());
    }

    @Test
    void agentTypeDefault() {
        assertEquals("sales", new AgentConfig(true, null).agentType());
    }

    // --- TOML (de)serialization tests ---

    @Test
    void scenarioConfigRoundtrip() throws Exception {
        ScenarioConfig config = new ScenarioConfig(
            new SimulationParams(42, 1000, 10L, 50L),
            List.of(new EquipmentConfig(1, "Lathe", 2, null, 0L)),
            List.of(new MaterialConfig(1, "Widget", 1)),
            List.of(new ProcessSegmentConfig(1, "Turn", 1, 5)),
            List.of(new OperationsDefinitionConfig(1, "Widget Route", List.of(1L))),
            new EconomyConfig(10.0, 5.0, 0.5, 0.1),
            new AgentConfig(true, "sales"));

        TomlMapper mapper = tomlMapper();
        String toml = mapper.writeValueAsString(config);
        ScenarioConfig back = mapper.readValue(toml, ScenarioConfig.class);

        assertEquals(config, back);
    }

    @Test
    void partialTomlFillsDefaults() throws Exception {
        String toml = """
            [simulation]
            rng_seed = 1
            max_ticks = 100
            """;
        ScenarioConfig config = tomlMapper().readValue(toml, ScenarioConfig.class);

        assertEquals(10L, config.simulation().demandInterval());
        assertEquals(50L, config.simulation().agentInterval());
        assertTrue(config.equipment().isEmpty());
        assertTrue(config.material().isEmpty());
        assertTrue(config.processSegment().isEmpty());
        assertTrue(config.operationsDefinition().isEmpty());
        assertNull(config.economy());
        assertNull(config.agent());
    }

    @Test
    void equipmentDefaults() throws Exception {
        String toml = """
            [simulation]
            rng_seed = 1
            max_ticks = 100

            [[equipment]]
            id = 1
            name = "Mill"
            """;
        ScenarioConfig config = tomlMapper().readValue(toml, ScenarioConfig.class);
        EquipmentConfig eq = config.equipment().get(0);

        assertEquals(1, eq.effectiveConcurrency());
        assertNull(eq.capacityLiters());
        assertEquals(0L, eq.effectiveSetupTime());
    }

    @Test
    void economyDefaults() throws Exception {
        String toml = """
            [simulation]
            rng_seed = 1
            max_ticks = 100

            [economy]
            initial_price = 20.0
            """;
        ScenarioConfig config = tomlMapper().readValue(toml, ScenarioConfig.class);
        EconomyConfig econ = config.economy();

        assertNotNull(econ);
        assertEquals(5.0, econ.effectiveBaseDemand());
        assertEquals(0.5, econ.effectivePriceElasticity());
        assertEquals(0.1, econ.effectiveLeadTimeSensitivity());
    }
}
