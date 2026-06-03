package com.arcogine.core.scenario;

import com.arcogine.types.SimError;
import com.arcogine.types.scenario.EconomyConfig;
import com.arcogine.types.scenario.EquipmentConfig;
import com.arcogine.types.scenario.MaterialConfig;
import com.arcogine.types.scenario.OperationsDefinitionConfig;
import com.arcogine.types.scenario.ProcessSegmentConfig;
import com.arcogine.types.scenario.ScenarioConfig;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ScenarioLoader {

    private static final double MAX_ECON_VALUE = 1_000_000.0;
    private static final TomlMapper TOML_MAPPER = new TomlMapper();

    private ScenarioLoader() {}

    public static ScenarioConfig loadScenario(String toml) {
        ScenarioConfig config;
        try {
            config = TOML_MAPPER.readValue(toml, ScenarioConfig.class);
        } catch (IOException e) {
            throw new SimError.ScenarioLoadError("TOML parse error: " + e.getMessage());
        }
        validateScenario(config);
        return config;
    }

    public static ScenarioConfig loadScenarioFile(String path) {
        String contents;
        try {
            contents = Files.readString(Path.of(path));
        } catch (IOException e) {
            throw new SimError.ScenarioLoadError("cannot read file '" + path + "': " + e.getMessage());
        }
        return loadScenario(contents);
    }

    private static void validateScenario(ScenarioConfig config) {
        if (config.simulation().maxTicks() == 0) {
            throw new SimError.OutOfRange("simulation.max_ticks", "must be > 0");
        }

        List<EquipmentConfig> equipment = config.equipment();
        if (equipment.isEmpty()) {
            throw new SimError.ScenarioLoadError("at least one [[equipment]] entry is required");
        }

        List<MaterialConfig> material = config.material();
        if (material.isEmpty()) {
            throw new SimError.ScenarioLoadError("at least one [[material]] entry is required");
        }

        Set<Long> equipIds = new HashSet<>();
        for (EquipmentConfig eq : equipment) {
            if (!equipIds.add(eq.id())) {
                throw new SimError.InvalidReference("duplicate equipment id: " + eq.id());
            }
            if (eq.effectiveConcurrency() == 0) {
                throw new SimError.OutOfRange(
                        "equipment[" + eq.id() + "].concurrency", "must be > 0");
            }
        }

        Set<Long> opsIds = new HashSet<>();
        for (OperationsDefinitionConfig od : config.operationsDefinition()) {
            opsIds.add(od.id());
        }

        Set<Long> materialIds = new HashSet<>();
        for (MaterialConfig mat : material) {
            if (!materialIds.add(mat.id())) {
                throw new SimError.InvalidReference("duplicate material id: " + mat.id());
            }
            if (!opsIds.contains(mat.routingId())) {
                throw new SimError.InvalidReference(
                        "material '" + mat.name() + "' references nonexistent routing id: " + mat.routingId());
            }
        }

        Set<Long> segIds = new HashSet<>();
        for (ProcessSegmentConfig seg : config.processSegment()) {
            if (!segIds.add(seg.id())) {
                throw new SimError.InvalidReference("duplicate process_segment id: " + seg.id());
            }
            if (!equipIds.contains(seg.equipmentId())) {
                throw new SimError.InvalidReference(
                        "process_segment '" + seg.name() + "' references nonexistent equipment id: "
                                + seg.equipmentId());
            }
            if (seg.duration() == 0) {
                throw new SimError.OutOfRange(
                        "process_segment[" + seg.id() + "].duration", "must be > 0");
            }
        }

        for (OperationsDefinitionConfig od : config.operationsDefinition()) {
            if (od.steps() == null || od.steps().isEmpty()) {
                throw new SimError.ScenarioLoadError(
                        "operations_definition '" + od.name() + "' has no steps");
            }
            for (Long stepId : od.steps()) {
                if (!segIds.contains(stepId)) {
                    throw new SimError.InvalidReference(
                            "operations_definition '" + od.name()
                                    + "' references nonexistent process_segment id: " + stepId);
                }
            }
        }

        EconomyConfig economy = config.economy();
        if (economy != null) {
            validateEconField("economy.initial_price", economy.initialPrice(), true);
            validateEconField("economy.base_demand", economy.effectiveBaseDemand(), false);
            validateEconField("economy.price_elasticity", economy.effectivePriceElasticity(), false);
            validateEconField("economy.lead_time_sensitivity", economy.effectiveLeadTimeSensitivity(), false);
        }
    }

    private static void validateEconField(String field, double value, boolean strictlyPositive) {
        if (!Double.isFinite(value) || value > MAX_ECON_VALUE) {
            String msg = strictlyPositive
                    ? "must be a finite number > 0 and <= 1,000,000"
                    : "must be a finite number >= 0 and <= 1,000,000";
            throw new SimError.OutOfRange(field, msg);
        }
        if (strictlyPositive && value <= 0.0) {
            throw new SimError.OutOfRange(field, "must be a finite number > 0 and <= 1,000,000");
        }
        if (!strictlyPositive && value < 0.0) {
            throw new SimError.OutOfRange(field, "must be a finite number >= 0 and <= 1,000,000");
        }
    }
}
