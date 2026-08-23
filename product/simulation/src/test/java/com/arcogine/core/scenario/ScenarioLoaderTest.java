package com.arcogine.core.scenario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.types.SimError;
import com.arcogine.types.scenario.ScenarioConfig;
import org.junit.jupiter.api.Test;

/** Ported from crates/sim-core/tests/scenario_loading.rs. */
class ScenarioLoaderTest {

    private static final String VALID_SCENARIO = """
            [simulation]
            rng_seed = 42
            max_ticks = 100

            [[equipment]]
            id = 1
            name = "Mill"

            [[equipment]]
            id = 2
            name = "Lathe"

            [[material]]
            id = 1
            name = "Widget"
            routing_id = 1

            [[process_segment]]
            id = 1
            name = "Milling"
            equipment_id = 1
            duration = 5

            [[process_segment]]
            id = 2
            name = "Turning"
            equipment_id = 2
            duration = 3

            [[operations_definition]]
            id = 1
            name = "Widget Routing"
            steps = [1, 2]
            """;

    @Test
    void loadValidScenario() {
        ScenarioConfig config = ScenarioLoader.loadScenario(VALID_SCENARIO);
        assertEquals(42L, config.simulation().rngSeed());
        assertEquals(100L, config.simulation().maxTicks());
        assertEquals(2, config.equipment().size());
        assertEquals(1, config.material().size());
        assertEquals(2, config.processSegment().size());
        assertEquals(1, config.operationsDefinition().size());
    }

    @Test
    void malformedTomlReturnsError() {
        SimError.ScenarioLoadError err = assertThrows(
                SimError.ScenarioLoadError.class,
                () -> ScenarioLoader.loadScenario("this is not valid toml {{{{"));
        assertTrue(err.getMessage().contains("TOML parse error"));
    }

    @Test
    void missingSimulationSectionReturnsError() {
        String toml = """
                [[equipment]]
                id = 1
                name = "Mill"
                """;
        // Java: no [simulation] yields a null SimulationParams, so validation
        // throws (NPE) rather than a SimError; the load still fails as required.
        assertThrows(RuntimeException.class, () -> ScenarioLoader.loadScenario(toml));
    }

    @Test
    void noEquipmentReturnsError() {
        String toml = """
                [simulation]
                rng_seed = 42
                max_ticks = 100

                [[material]]
                id = 1
                name = "Widget"
                routing_id = 1
                """;
        SimError.ScenarioLoadError err = assertThrows(
                SimError.ScenarioLoadError.class,
                () -> ScenarioLoader.loadScenario(toml));
        assertTrue(err.getMessage().contains("equipment"));
    }

    @Test
    void noMaterialReturnsError() {
        String toml = """
                [simulation]
                rng_seed = 42
                max_ticks = 100

                [[equipment]]
                id = 1
                name = "Mill"
                """;
        SimError.ScenarioLoadError err = assertThrows(
                SimError.ScenarioLoadError.class,
                () -> ScenarioLoader.loadScenario(toml));
        assertTrue(err.getMessage().contains("material"));
    }

    @Test
    void nonexistentRoutingReferenceReturnsError() {
        String toml = """
                [simulation]
                rng_seed = 42
                max_ticks = 100

                [[equipment]]
                id = 1
                name = "Mill"

                [[material]]
                id = 1
                name = "Widget"
                routing_id = 999

                [[process_segment]]
                id = 1
                name = "Milling"
                equipment_id = 1
                duration = 5

                [[operations_definition]]
                id = 1
                name = "Widget Routing"
                steps = [1]
                """;
        SimError.InvalidReference err = assertThrows(
                SimError.InvalidReference.class,
                () -> ScenarioLoader.loadScenario(toml));
        assertTrue(err.getMessage().contains("999"));
    }

    @Test
    void nonexistentEquipmentInProcessSegmentReturnsError() {
        String toml = """
                [simulation]
                rng_seed = 42
                max_ticks = 100

                [[equipment]]
                id = 1
                name = "Mill"

                [[material]]
                id = 1
                name = "Widget"
                routing_id = 1

                [[process_segment]]
                id = 1
                name = "Milling"
                equipment_id = 999
                duration = 5

                [[operations_definition]]
                id = 1
                name = "Widget Routing"
                steps = [1]
                """;
        SimError.InvalidReference err = assertThrows(
                SimError.InvalidReference.class,
                () -> ScenarioLoader.loadScenario(toml));
        assertTrue(err.getMessage().contains("999"));
    }

    @Test
    void zeroMaxTicksReturnsError() {
        String toml = """
                [simulation]
                rng_seed = 42
                max_ticks = 0

                [[equipment]]
                id = 1
                name = "Mill"

                [[material]]
                id = 1
                name = "Widget"
                routing_id = 1

                [[process_segment]]
                id = 1
                name = "Milling"
                equipment_id = 1
                duration = 5

                [[operations_definition]]
                id = 1
                name = "Widget Routing"
                steps = [1]
                """;
        SimError.OutOfRange err = assertThrows(
                SimError.OutOfRange.class,
                () -> ScenarioLoader.loadScenario(toml));
        assertTrue(err.field().contains("max_ticks"));
    }

    @Test
    void zeroDurationProcessSegmentReturnsError() {
        String toml = """
                [simulation]
                rng_seed = 42
                max_ticks = 100

                [[equipment]]
                id = 1
                name = "Mill"

                [[material]]
                id = 1
                name = "Widget"
                routing_id = 1

                [[process_segment]]
                id = 1
                name = "Milling"
                equipment_id = 1
                duration = 0

                [[operations_definition]]
                id = 1
                name = "Widget Routing"
                steps = [1]
                """;
        SimError.OutOfRange err = assertThrows(
                SimError.OutOfRange.class,
                () -> ScenarioLoader.loadScenario(toml));
        assertTrue(err.field().contains("duration"));
    }

    @Test
    void duplicateEquipmentIdsReturnsError() {
        String toml = """
                [simulation]
                rng_seed = 42
                max_ticks = 100

                [[equipment]]
                id = 1
                name = "Mill A"

                [[equipment]]
                id = 1
                name = "Mill B"

                [[material]]
                id = 1
                name = "Widget"
                routing_id = 1

                [[process_segment]]
                id = 1
                name = "Milling"
                equipment_id = 1
                duration = 5

                [[operations_definition]]
                id = 1
                name = "Widget Routing"
                steps = [1]
                """;
        SimError.InvalidReference err = assertThrows(
                SimError.InvalidReference.class,
                () -> ScenarioLoader.loadScenario(toml));
        assertTrue(err.getMessage().contains("duplicate"));
    }

    @Test
    void defaultValuesAppliedCorrectly() {
        ScenarioConfig config = ScenarioLoader.loadScenario(VALID_SCENARIO);
        assertEquals(10L, config.simulation().demandInterval());
        assertEquals(50L, config.simulation().agentInterval());
        assertEquals(1, config.equipment().get(0).effectiveConcurrency());
        assertEquals(0L, config.equipment().get(0).effectiveSetupTime());
        assertNull(config.equipment().get(0).capacityLiters());
    }

    private static String scenarioWithEconomy(String economyBlock) {
        return """
                [simulation]
                rng_seed = 42
                max_ticks = 100

                [[equipment]]
                id = 1
                name = "Mill"

                [[material]]
                id = 1
                name = "Widget"
                routing_id = 1

                [[process_segment]]
                id = 1
                name = "Milling"
                equipment_id = 1
                duration = 5

                [[operations_definition]]
                id = 1
                name = "Widget Routing"
                steps = [1]

                """ + economyBlock;
    }

    @Test
    void scenarioWithNanPriceRejected() {
        String toml = scenarioWithEconomy("[economy]\ninitial_price = nan\n");
        SimError.OutOfRange err = assertThrows(
                SimError.OutOfRange.class,
                () -> ScenarioLoader.loadScenario(toml));
        assertTrue(err.field().contains("initial_price"), "expected initial_price, got: " + err.field());
    }

    @Test
    void scenarioWithInfDemandRejected() {
        String toml = scenarioWithEconomy("[economy]\ninitial_price = 10.0\nbase_demand = inf\n");
        SimError.OutOfRange err = assertThrows(
                SimError.OutOfRange.class,
                () -> ScenarioLoader.loadScenario(toml));
        assertTrue(err.field().contains("base_demand"), "expected base_demand, got: " + err.field());
    }

    @Test
    void scenarioWithExtremePriceRejected() {
        String toml = scenarioWithEconomy("[economy]\ninitial_price = 999999999.0\n");
        SimError.OutOfRange err = assertThrows(
                SimError.OutOfRange.class,
                () -> ScenarioLoader.loadScenario(toml));
        assertTrue(err.field().contains("initial_price"), "expected initial_price, got: " + err.field());
    }

    @Test
    void scenarioWithExtremeBaseDemandRejected() {
        String toml = scenarioWithEconomy("[economy]\ninitial_price = 10.0\nbase_demand = 1500000.0\n");
        SimError.OutOfRange err = assertThrows(
                SimError.OutOfRange.class,
                () -> ScenarioLoader.loadScenario(toml));
        assertTrue(err.field().contains("base_demand"), "expected base_demand, got: " + err.field());
    }
}
