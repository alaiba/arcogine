package com.arcogine.research.experiment;

import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A parameterized, non-spatial {@code CUT -> ASSEMBLE -> INSPECT} production family, so controlled
 * variants of one design differ only in what a fixture deliberately changes.
 *
 * <p>The family projects onto the current Factory model exactly as authored: each stage is one
 * routing step with one duration, and each resource eligible for a stage is one {@link
 * ConfiguredResource} with its own concurrency. It invents nothing the model does not carry, in
 * particular no per-resource speed, and it authors no spatial record. Costs, budgets and any other
 * game-side parameters are not part of it; a research fixture that needs them keeps them outside
 * the projected model.
 *
 * <p>Resources are numbered from 1 in stage order (cut, assemble, inspect), so identities are
 * stable for a given parameterization.
 */
public record ThreeStepRoutingFamily(Stage cut, Stage assemble, Stage inspect) {

    public static final ProductId PRODUCT = new ProductId(1);
    public static final long OPERATION_ID = 1;
    public static final long CUT_STEP_ID = 1;
    public static final long ASSEMBLE_STEP_ID = 2;
    public static final long INSPECT_STEP_ID = 3;
    public static final String CUT = "CUT";
    public static final String ASSEMBLE = "ASSEMBLE";
    public static final String INSPECT = "INSPECT";
    public static final double UNIT_PRICE = 10.0;

    /**
     * One routing step: its duration in ticks, and one entry per resource eligible for it giving
     * that resource's concurrency.
     */
    public record Stage(long duration, List<Integer> resourceConcurrencies) {

        public Stage {
            if (duration < 1) {
                throw new IllegalArgumentException("a stage duration must be at least one tick, got " + duration);
            }
            resourceConcurrencies = List.copyOf(Objects.requireNonNull(resourceConcurrencies, "resourceConcurrencies"));
            if (resourceConcurrencies.isEmpty()) {
                throw new IllegalArgumentException("a stage needs at least one eligible resource");
            }
        }

        public static Stage of(long duration, int... concurrencies) {
            return new Stage(duration, Arrays.stream(concurrencies).boxed().toList());
        }
    }

    public ThreeStepRoutingFamily {
        Objects.requireNonNull(cut, "cut");
        Objects.requireNonNull(assemble, "assemble");
        Objects.requireNonNull(inspect, "inspect");
    }

    /** The same family with only the assemble stage replaced. */
    public ThreeStepRoutingFamily withAssemble(Stage replacement) {
        return new ThreeStepRoutingFamily(cut, replacement, inspect);
    }

    public List<MachineId> cutResources() {
        return resourceIds(0, cut);
    }

    public List<MachineId> assembleResources() {
        return resourceIds(cut.resourceConcurrencies().size(), assemble);
    }

    public List<MachineId> inspectResources() {
        return resourceIds(cut.resourceConcurrencies().size() + assemble.resourceConcurrencies().size(), inspect);
    }

    /** Builds the authored, spatial-record-free Factory model for this parameterization. */
    public FactoryModel model() {
        List<ConfiguredResource> resources = new ArrayList<>();
        appendResources(resources, "Cutter", cut, cutResources());
        appendResources(resources, "Assembler", assemble, assembleResources());
        appendResources(resources, "Inspector", inspect, inspectResources());
        List<OperationStepDefinition> steps = List.of(
                step(CUT_STEP_ID, CUT, cut, cutResources()),
                step(ASSEMBLE_STEP_ID, ASSEMBLE, assemble, assembleResources()),
                step(INSPECT_STEP_ID, INSPECT, inspect, inspectResources()));
        return new FactoryModel(
                resources,
                List.of(new OperationDefinition(OPERATION_ID, "Widget routing", steps)),
                List.of(new ProductDefinition(PRODUCT, "Widget", OPERATION_ID)));
    }

    private static List<MachineId> resourceIds(int precedingResources, Stage stage) {
        List<MachineId> ids = new ArrayList<>();
        for (int i = 0; i < stage.resourceConcurrencies().size(); i++) {
            ids.add(new MachineId(precedingResources + i + 1L));
        }
        return List.copyOf(ids);
    }

    private static void appendResources(
            List<ConfiguredResource> resources, String name, Stage stage, List<MachineId> ids) {
        for (int i = 0; i < ids.size(); i++) {
            String resourceName = ids.size() == 1 ? name : name + " " + (i + 1);
            resources.add(new ConfiguredResource(ids.get(i), resourceName, stage.resourceConcurrencies().get(i), null, 0));
        }
    }

    private static OperationStepDefinition step(long stepId, String name, Stage stage, List<MachineId> ids) {
        return new OperationStepDefinition(stepId, name, Set.copyOf(ids), stage.duration());
    }
}
