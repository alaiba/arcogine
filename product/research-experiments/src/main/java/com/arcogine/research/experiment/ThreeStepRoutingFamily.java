package com.arcogine.research.experiment;

import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
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
 * <p>This is the special case of a {@link LinearRoutingFamily} in which every resource serves exactly
 * one stage: each stage is one routing step with one duration, and each resource eligible for a stage
 * is one {@link ConfiguredResource} with its own concurrency. It invents nothing the model does not
 * carry, in particular no per-resource speed, and it authors no spatial record. Costs, budgets and any
 * other game-side parameters are not part of it; a research fixture that needs them keeps them outside
 * the projected model. A design in which one resource serves several stages is authored as a {@link
 * LinearRoutingFamily}.
 *
 * <p>Resources are numbered from 1 in stage order (cut, assemble, inspect), so identities are
 * stable for a given parameterization.
 */
public record ThreeStepRoutingFamily(Stage cut, Stage assemble, Stage inspect) {

    public static final ProductId PRODUCT = LinearRoutingFamily.PRODUCT;
    public static final long OPERATION_ID = LinearRoutingFamily.OPERATION_ID;
    public static final long CUT_STEP_ID = 1;
    public static final long ASSEMBLE_STEP_ID = 2;
    public static final long INSPECT_STEP_ID = 3;
    public static final String CUT = "CUT";
    public static final String ASSEMBLE = "ASSEMBLE";
    public static final String INSPECT = "INSPECT";
    public static final double UNIT_PRICE = LinearRoutingFamily.UNIT_PRICE;

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
        return asLinearFamily().eligibleResources(CUT);
    }

    public List<MachineId> assembleResources() {
        return asLinearFamily().eligibleResources(ASSEMBLE);
    }

    public List<MachineId> inspectResources() {
        return asLinearFamily().eligibleResources(INSPECT);
    }

    /**
     * This design as a {@link LinearRoutingFamily}: the three stages in routing order, and one
     * single-stage resource per stage entry, named {@code Cutter}, {@code Assembler} and {@code
     * Inspector} (numbered when a stage has more than one), in stage order.
     */
    public LinearRoutingFamily asLinearFamily() {
        List<LinearRoutingFamily.Resource> resources = new ArrayList<>();
        appendResources(resources, "Cutter", CUT, cut);
        appendResources(resources, "Assembler", ASSEMBLE, assemble);
        appendResources(resources, "Inspector", INSPECT, inspect);
        return new LinearRoutingFamily(
                List.of(
                        new LinearRoutingFamily.Step(CUT, cut.duration()),
                        new LinearRoutingFamily.Step(ASSEMBLE, assemble.duration()),
                        new LinearRoutingFamily.Step(INSPECT, inspect.duration())),
                resources);
    }

    /** Builds the authored, spatial-record-free Factory model for this parameterization. */
    public FactoryModel model() {
        return asLinearFamily().model();
    }

    private static void appendResources(
            List<LinearRoutingFamily.Resource> resources, String name, String step, Stage stage) {
        int count = stage.resourceConcurrencies().size();
        for (int i = 0; i < count; i++) {
            String resourceName = count == 1 ? name : name + " " + (i + 1);
            resources.add(new LinearRoutingFamily.Resource(resourceName, stage.resourceConcurrencies().get(i), Set.of(step)));
        }
    }
}
