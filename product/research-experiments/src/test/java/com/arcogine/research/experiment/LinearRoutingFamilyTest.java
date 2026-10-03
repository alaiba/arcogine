package com.arcogine.research.experiment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.research.experiment.LinearRoutingFamily.Resource;
import com.arcogine.research.experiment.LinearRoutingFamily.Step;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * The linear family's projection onto the current Factory model: shared eligibility, explicit
 * resource order, nothing invented, and the three-step family as its one-resource-per-stage case.
 */
class LinearRoutingFamilyTest {

    private static final List<Step> STEPS = List.of(new Step("CUT", 2), new Step("ASSEMBLE", 3), new Step("INSPECT", 4));

    private static final LinearRoutingFamily SHARED = new LinearRoutingFamily(
            STEPS,
            List.of(
                    Resource.of("Cutter", 1, "CUT"),
                    Resource.of("Assembler", 1, "ASSEMBLE"),
                    Resource.of("Shared", 2, "ASSEMBLE", "INSPECT")));

    private static Map<String, Set<MachineId>> eligibilityByStep(FactoryModel model) {
        return model.operations().getFirst().steps().stream()
                .collect(Collectors.toMap(OperationStepDefinition::name, OperationStepDefinition::eligibleResources));
    }

    @Test
    void aSharedResourceIsEligibleForEveryStepItListsAndThePublishedModelSaysSo() {
        FactoryModelVersion version = FactoryModelPublisher.publish(SHARED.model());
        MachineId shared = SHARED.resourceId("Shared");

        assertEquals(
                Map.of(
                        "CUT", Set.of(SHARED.resourceId("Cutter")),
                        "ASSEMBLE", Set.of(SHARED.resourceId("Assembler"), shared),
                        "INSPECT", Set.of(shared)),
                eligibilityByStep(version.model()));
        assertEquals(List.of(SHARED.resourceId("Assembler"), shared), SHARED.eligibleResources("ASSEMBLE"));
        assertEquals(List.of(shared), SHARED.eligibleResources("INSPECT"));
    }

    @Test
    void theProjectionCarriesOnlyFactsTheCurrentModelHas() {
        FactoryModel model = SHARED.model();

        // One configured resource per authored resource, numbered in order, with no spatial record, no
        // per-resource speed, and setupTime / capacityLiters left at values the Engine does not read.
        assertTrue(model.spatial().isEmpty());
        assertEquals(
                List.of(
                        new ConfiguredResource(new MachineId(1), "Cutter", 1, null, 0),
                        new ConfiguredResource(new MachineId(2), "Assembler", 1, null, 0),
                        new ConfiguredResource(new MachineId(3), "Shared", 2, null, 0)),
                model.resources());
        // Durations belong to the steps, which are numbered in routing order.
        OperationDefinition operation = model.operations().getFirst();
        assertEquals(
                List.of(1L, 2L, 3L),
                operation.steps().stream().map(OperationStepDefinition::stepId).toList());
        assertEquals(
                List.of(2L, 3L, 4L),
                operation.steps().stream().map(OperationStepDefinition::duration).toList());
        assertEquals(
                List.of(new ProductDefinition(LinearRoutingFamily.PRODUCT, LinearRoutingFamily.PRODUCT_NAME,
                        LinearRoutingFamily.OPERATION_ID)),
                model.products());
    }

    @Test
    void resourceOrderIsAnExplicitInputThatRenumbersTheResources() {
        LinearRoutingFamily reversed = SHARED.withReversedResourceOrder();

        assertEquals(List.of("Shared", "Assembler", "Cutter"), reversed.resources().stream().map(Resource::name).toList());
        assertEquals(new MachineId(3), SHARED.resourceId("Shared"));
        assertEquals(new MachineId(1), reversed.resourceId("Shared"));
        assertEquals(reversed, SHARED.withResourceOrder(List.of("Shared", "Assembler", "Cutter")));
        assertEquals(SHARED, reversed.withReversedResourceOrder());

        // The design is the same apart from its order -- steps, and eligibility by name -- but order and
        // identities are canonical content, so the reordered design is a different published model.
        assertEquals(SHARED.steps(), reversed.steps());
        assertEquals(Set.copyOf(SHARED.resources()), Set.copyOf(reversed.resources()));
        assertNotEquals(
                FactoryModelPublisher.publish(SHARED.model()).fingerprint(),
                FactoryModelPublisher.publish(reversed.model()).fingerprint());
    }

    @Test
    void theThreeStepFamilyStillProjectsTheModelsTheStarterCorpusWasAuthoredWith() {
        // Each starter fixture's model, written out as the three-step family authored it before it became a
        // special case of the linear family: resources numbered in stage order, named by stage.
        Map<String, FactoryModel> authored = Map.of(
                StarterCorpus.BASELINE_ID,
                widget(
                        List.of(resource(1, "Cutter", 1), resource(2, "Assembler", 1), resource(3, "Inspector", 1)),
                        List.of(step(1, "CUT", 2, 1), step(2, "ASSEMBLE", 6, 2), step(3, "INSPECT", 3, 3))),
                StarterCorpus.ASSEMBLE_VARIANT_ID,
                widget(
                        List.of(resource(1, "Cutter", 1), resource(2, "Assembler", 2), resource(3, "Inspector", 1)),
                        List.of(step(1, "CUT", 2, 1), step(2, "ASSEMBLE", 6, 2), step(3, "INSPECT", 3, 3))),
                StarterCorpus.MULTI_ELIGIBLE_ID,
                widget(
                        List.of(
                                resource(1, "Cutter", 1),
                                resource(2, "Assembler 1", 1),
                                resource(3, "Assembler 2", 1),
                                resource(4, "Inspector", 1)),
                        List.of(step(1, "CUT", 1, 1), step(2, "ASSEMBLE", 8, 2, 3), step(3, "INSPECT", 1, 4))),
                StarterCorpus.LONG_STEP_PARALLEL_ID,
                widget(
                        List.of(resource(1, "Cutter", 1), resource(2, "Assembler", 2), resource(3, "Inspector", 1)),
                        List.of(step(1, "CUT", 1, 1), step(2, "ASSEMBLE", 12, 2), step(3, "INSPECT", 1, 3))));

        for (ExperimentFixture fixture : StarterCorpus.all()) {
            FactoryModel expected = authored.get(fixture.id());
            assertEquals(expected, fixture.authoredModel(), fixture.id());
            assertEquals(FactoryModelPublisher.publish(expected).fingerprint(), fixture.publishedModel().fingerprint(),
                    fixture.id());
        }
    }

    @Test
    void malformedFamiliesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Step(" ", 1));
        assertThrows(IllegalArgumentException.class, () -> new Step("CUT", 0));
        assertThrows(IllegalArgumentException.class, () -> Resource.of("Cutter", 0, "CUT"));
        assertThrows(IllegalArgumentException.class, () -> Resource.of("Cutter", 1));
        assertThrows(IllegalArgumentException.class, () -> new LinearRoutingFamily(List.of(), List.of(Resource.of("A", 1, "X"))));
        assertThrows(
                IllegalArgumentException.class,
                () -> new LinearRoutingFamily(List.of(new Step("X", 1), new Step("X", 2)), List.of(Resource.of("A", 1, "X"))),
                "step names identify steps");
        assertThrows(
                IllegalArgumentException.class,
                () -> new LinearRoutingFamily(List.of(new Step("X", 1)), List.of(Resource.of("A", 1, "X"), Resource.of("A", 1, "X"))),
                "resource names identify resources");
        assertThrows(
                IllegalArgumentException.class,
                () -> new LinearRoutingFamily(List.of(new Step("X", 1)), List.of(Resource.of("A", 1, "Y"))),
                "a resource may only list known steps");
        assertThrows(
                IllegalArgumentException.class,
                () -> new LinearRoutingFamily(List.of(new Step("X", 1), new Step("Y", 1)), List.of(Resource.of("A", 1, "X"))),
                "every step needs an eligible resource");
        assertThrows(IllegalArgumentException.class, () -> SHARED.withResourceOrder(List.of("Shared", "Cutter")));
        assertThrows(IllegalArgumentException.class, () -> SHARED.withResourceOrder(List.of("Shared", "Shared", "Cutter")));
        assertThrows(IllegalArgumentException.class, () -> SHARED.resourceId("Painter"));
        assertThrows(IllegalArgumentException.class, () -> SHARED.eligibleResources("PAINT"));
    }

    private static FactoryModel widget(List<ConfiguredResource> resources, List<OperationStepDefinition> steps) {
        return new FactoryModel(
                resources,
                List.of(new OperationDefinition(1, "Widget routing", steps)),
                List.of(new ProductDefinition(new ProductId(1), "Widget", 1)));
    }

    private static ConfiguredResource resource(long id, String name, int concurrency) {
        return new ConfiguredResource(new MachineId(id), name, concurrency, null, 0);
    }

    private static OperationStepDefinition step(long id, String name, long duration, long... eligible) {
        return new OperationStepDefinition(
                id, name, Arrays.stream(eligible).mapToObj(MachineId::new).collect(Collectors.toSet()), duration);
    }
}
