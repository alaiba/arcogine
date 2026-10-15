package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * {@code capacityLiters} and {@code setupTime} are authored resource facts that runtime assembly
 * forwards to resource observations but that the current Engine never reads: two models that
 * differ only in those fields execute identically.
 */
class ConfiguredResourceMetadataTest {

    /** A supported event without its run identity and model fingerprint, which differ by design. */
    private record SemanticEvent(
            long sequence, long time, RuntimeEventType type, List<AffectedEntityRef> refs, RuntimeEventPayload payload) {
        static SemanticEvent of(RuntimeEventEnvelope event) {
            return new SemanticEvent(
                    event.sequence(),
                    event.simulationTime().value(),
                    event.eventType(),
                    event.affectedEntityRefs(),
                    event.payload());
        }
    }

    @Test
    void capacityAndSetupTimeAreObservedButNeverChangeExecution() {
        FactoryRuntime plain = runtime(null, 0, null, 0);
        // A large setup time and a small capacity on the first-ranked machine would delay or
        // divert work if the Engine read either field.
        FactoryRuntime annotated = runtime(1.0, 1_000, 250.0, 7);

        List<SemanticEvent> plainEvents = run(plain);
        List<SemanticEvent> annotatedEvents = run(annotated);

        assertNotEquals(plain.modelVersion().fingerprint(), annotated.modelVersion().fingerprint(),
                "the fields are canonical content, so the two designs have different identities");
        assertEquals(plainEvents, annotatedEvents);
        assertEquals(plain.observe().performance(), annotated.observe().performance());

        List<ResourceObservation> resources = annotated.observe().resources();
        assertEquals(1.0, resources.get(0).capacityLiters());
        assertEquals(1_000L, resources.get(0).setupTime());
        assertEquals(250.0, resources.get(1).capacityLiters());
        assertEquals(7L, resources.get(1).setupTime());
        assertNull(plain.observe().resources().get(0).capacityLiters());
        assertEquals(0L, plain.observe().resources().get(0).setupTime());
    }

    private static List<SemanticEvent> run(FactoryRuntime runtime) {
        List<SemanticEvent> events = new ArrayList<>();
        runtime.submitWorkload(new ProductId(1), 4, 10.0).orElseThrow();
        runtime.drainSupportedEvents().forEach(event -> events.add(SemanticEvent.of(event)));
        while (runtime.advance().isPresent()) {
            runtime.drainSupportedEvents().forEach(event -> events.add(SemanticEvent.of(event)));
        }
        return events;
    }

    private static FactoryRuntime runtime(
            Double firstCapacity, long firstSetup, Double secondCapacity, long secondSetup) {
        FactoryModel model = new FactoryModel(
                List.of(
                        new ConfiguredResource(new MachineId(1), "M1", 1, firstCapacity, firstSetup),
                        new ConfiguredResource(new MachineId(2), "M2", 1, secondCapacity, secondSetup)),
                List.of(new OperationDefinition(
                        1,
                        "route",
                        List.of(
                                new OperationStepDefinition(1, "shared", Set.of(new MachineId(1), new MachineId(2)), 5),
                                new OperationStepDefinition(2, "finish", Set.of(new MachineId(1)), 3)))),
                List.of(new ProductDefinition(new ProductId(1), "P1", 1)));
        return FactoryRuntime.forModel(FactoryModelPublisher.publish(model));
    }
}
