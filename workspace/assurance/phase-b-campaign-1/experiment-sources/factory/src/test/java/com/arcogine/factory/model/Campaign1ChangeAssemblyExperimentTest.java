package com.arcogine.factory.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.core.event.Event;
import com.arcogine.factory.change.FactoryModelSemanticComparator;
import com.arcogine.factory.process.FactoryRuntime;
import com.arcogine.factory.process.ResourceObservation;
import com.arcogine.factory.routing.Routing;
import com.arcogine.factory.routing.RoutingStep;
import com.arcogine.governance.SemanticArtifact;
import com.arcogine.governance.change.SemanticChange;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

/**
 * Transient Campaign 1 slice 3 experiment (not a merge candidate): independent semantic edit
 * ledger versus {@link FactoryModelSemanticComparator}, and authored-fact preservation through
 * runtime assembly with session isolation.
 */
class Campaign1ChangeAssemblyExperimentTest {

    static final FactoryModelSemanticComparator COMPARATOR = new FactoryModelSemanticComparator();

    static SemanticArtifact artifact(FactoryModel model) {
        FactoryModelVersion version = FactoryModelPublisher.publish(model);
        return new SemanticArtifact(version.fingerprint(), FactoryModelArtifact.encode(version));
    }

    // -------------------------------------------------------------- independent edit ledger

    record Expected(String kind, String type, String id, Set<String> aspects) {}

    static <T> void ledger(String type, List<T> base, List<T> candidate, java.util.function.Function<T, Long> id,
            java.util.function.BiFunction<T, T, Set<String>> fieldDiff, List<Expected> out) {
        Map<Long, Integer> basePos = new HashMap<>();
        Map<Long, Integer> candPos = new HashMap<>();
        for (int i = 0; i < base.size(); i++) {
            basePos.put(id.apply(base.get(i)), i);
        }
        for (int i = 0; i < candidate.size(); i++) {
            candPos.put(id.apply(candidate.get(i)), i);
        }
        for (T b : base) {
            long key = id.apply(b);
            if (!candPos.containsKey(key)) {
                out.add(new Expected("ENTITY_REMOVED", type, Long.toString(key), Set.of()));
                continue;
            }
            T c = candidate.get(candPos.get(key));
            Set<String> aspects = new TreeSet<>(fieldDiff.apply(b, c));
            if (!basePos.get(key).equals(candPos.get(key))) {
                aspects.add("listPosition");
            }
            if (!aspects.isEmpty()) {
                out.add(new Expected("ENTITY_MODIFIED", type, Long.toString(key), aspects));
            }
        }
        for (T c : candidate) {
            if (!basePos.containsKey(id.apply(c))) {
                out.add(new Expected("ENTITY_ADDED", type, Long.toString(id.apply(c)), Set.of()));
            }
        }
    }

    static List<Expected> expected(FactoryModel baseModel, FactoryModel candidateModel) {
        Campaign1Oracle.NModel b = Campaign1Oracle.setNormalized(Campaign1Oracle.neutral(baseModel));
        Campaign1Oracle.NModel c = Campaign1Oracle.setNormalized(Campaign1Oracle.neutral(candidateModel));
        List<Expected> out = new ArrayList<>();
        ledger("factory.resource", b.resources(), c.resources(), Campaign1Oracle.NResource::id, (x, y) -> {
            Set<String> d = new TreeSet<>();
            if (!x.name().equals(y.name())) d.add("name");
            if (x.concurrency() != y.concurrency()) d.add("concurrency");
            if (!java.util.Objects.equals(x.capacityBits(), y.capacityBits())) d.add("capacityLiters");
            if (x.setupTime() != y.setupTime()) d.add("setupTime");
            return d;
        }, out);
        ledger("factory.operation", b.operations(), c.operations(), Campaign1Oracle.NOperation::id, (x, y) -> {
            Set<String> d = new TreeSet<>();
            if (!x.name().equals(y.name())) d.add("name");
            if (!x.steps().equals(y.steps())) d.add("steps");
            return d;
        }, out);
        ledger("factory.product", b.products(), c.products(), Campaign1Oracle.NProduct::id, (x, y) -> {
            Set<String> d = new TreeSet<>();
            if (!x.name().equals(y.name())) d.add("name");
            if (x.operationId() != y.operationId()) d.add("operationId");
            return d;
        }, out);
        return out;
    }

    /** The comparator's reported aspects, read from its detail text (observed vocabulary). */
    static Set<String> aspects(SemanticChange change) {
        Set<String> out = new TreeSet<>();
        for (String part : change.detail().split("; ")) {
            if (part.startsWith("stepIds: ") || part.equals("step routing/duration changed")) {
                out.add("steps");
            } else if (!part.isEmpty() && part.contains(": ")) {
                out.add(part.substring(0, part.indexOf(": ")));
            }
        }
        return out;
    }

    static void checkPair(String label, FactoryModel base, FactoryModel candidate) {
        SemanticArtifact a = artifact(base);
        SemanticArtifact b = artifact(candidate);
        List<SemanticChange> changes = COMPARATOR.compare(a, b);
        Map<String, Expected> expected = new TreeMap<>();
        for (Expected e : expected(base, candidate)) {
            expected.put(e.type() + "#" + e.id(), e);
        }
        Map<String, Expected> actual = new TreeMap<>();
        for (SemanticChange change : changes) {
            String key = change.entity().entityType() + "#" + change.entity().entityId();
            Expected previous = actual.put(key, new Expected(change.kind().name(), change.entity().entityType(),
                    change.entity().entityId(), change.kind().name().equals("ENTITY_MODIFIED") ? aspects(change) : Set.of()));
            assertEquals(null, previous, label + " duplicate change for " + key);
        }
        assertEquals(expected, actual, label);
        // Identity relation: no reported change exactly when canonical bytes are equal.
        assertEquals(Arrays.equals(a.canonicalBytes(), b.canonicalBytes()), changes.isEmpty(), label);
        // Swap relation: additions and removals exchange; modified identities are preserved.
        List<SemanticChange> reverse = COMPARATOR.compare(b, a);
        Map<String, String> forwardKinds = new TreeMap<>();
        changes.forEach(c -> forwardKinds.put(c.entity().entityType() + "#" + c.entity().entityId(), c.kind().name()));
        Map<String, String> reverseKinds = new TreeMap<>();
        reverse.forEach(c -> reverseKinds.put(c.entity().entityType() + "#" + c.entity().entityId(),
                c.kind().name().replace("ADDED", "TMP").replace("REMOVED", "ADDED").replace("TMP", "REMOVED")));
        assertEquals(forwardKinds, reverseKinds, label + " swap");
    }

    @Test
    void seededPairsAgreeWithTheIndependentEditLedger() {
        Random random = new Random(Campaign1CodecExperimentTest.SEED ^ 0x5EED);
        int pairs = 0;
        int noOps = 0;
        for (int i = 0; i < 1500; i++) {
            FactoryModel a = Campaign1CodecExperimentTest.generate(random);
            FactoryModel b = Campaign1CodecExperimentTest.generate(random);
            checkPair("independent " + i, a, b);
            checkPair("self " + i, a, a);
            FactoryModel edited = edit(a, random);
            checkPair("edit " + i, a, edited);
            if (Arrays.equals(artifact(a).canonicalBytes(), artifact(edited).canonicalBytes())) {
                noOps++;
            }
            pairs += 3;
        }
        assertEquals(4500, pairs);
        assertTrue(noOps > 0, "edits never produced an equivalent model");
    }

    /** One random semantic or equivalent edit of a valid model, keeping it valid. */
    static FactoryModel edit(FactoryModel m, Random random) {
        List<ConfiguredResource> resources = new ArrayList<>(m.resources());
        List<OperationDefinition> operations = new ArrayList<>(m.operations());
        List<ProductDefinition> products = new ArrayList<>(m.products());
        int r = random.nextInt(resources.size());
        ConfiguredResource res = resources.get(r);
        switch (random.nextInt(12)) {
            case 0 -> resources.set(r, new ConfiguredResource(res.id(), res.name() + "x", res.concurrency(), res.capacityLiters(), res.setupTime()));
            case 1 -> resources.set(r, new ConfiguredResource(res.id(), res.name(), res.concurrency() == 1 ? 2 : 1, res.capacityLiters(), res.setupTime()));
            case 2 -> resources.set(r, new ConfiguredResource(res.id(), res.name(), res.concurrency(),
                    new Double[] {null, 0.0, -0.0, Double.NaN, 7.0}[random.nextInt(5)], res.setupTime()));
            case 3 -> resources.set(r, new ConfiguredResource(res.id(), res.name(), res.concurrency(), res.capacityLiters(), res.setupTime() ^ 1));
            case 4 -> java.util.Collections.reverse(resources);
            case 5 -> java.util.Collections.reverse(operations);
            case 6 -> java.util.Collections.reverse(products);
            case 7 -> {
                OperationDefinition o = operations.get(0);
                List<OperationStepDefinition> steps = new ArrayList<>(o.steps());
                java.util.Collections.reverse(steps);
                operations.set(0, new OperationDefinition(o.id(), o.name(), steps));
            }
            case 8 -> {
                OperationDefinition o = operations.get(0);
                OperationStepDefinition s = o.steps().get(0);
                List<OperationStepDefinition> steps = new ArrayList<>(o.steps());
                steps.set(0, new OperationStepDefinition(s.stepId(), s.name() + "y", s.eligibleResources(), s.duration()));
                operations.set(0, new OperationDefinition(o.id(), o.name(), steps));
            }
            case 9 -> {
                OperationDefinition o = operations.get(0);
                operations.set(0, new OperationDefinition(o.id(), o.name() + "z", o.steps()));
            }
            case 10 -> {
                // equivalent: eligible set rebuilt in reverse authored order
                OperationDefinition o = operations.get(0);
                OperationStepDefinition s = o.steps().get(0);
                List<MachineId> reversed = new ArrayList<>(s.eligibleResources());
                java.util.Collections.reverse(reversed);
                List<OperationStepDefinition> steps = new ArrayList<>(o.steps());
                steps.set(0, new OperationStepDefinition(s.stepId(), s.name(), new java.util.LinkedHashSet<>(reversed), s.duration()));
                operations.set(0, new OperationDefinition(o.id(), o.name(), steps));
            }
            default -> {
                long fresh = 1_000_000L + random.nextInt(1000);
                resources.add(random.nextInt(resources.size() + 1), new ConfiguredResource(new MachineId(fresh), "new", 1, null, 0));
            }
        }
        return new FactoryModel(resources, operations, products);
    }

    @Test
    void stepOnlyEditsAreReportedAtOperationGranularity() {
        FactoryModel base = Campaign1CodecExperimentTest.representative();
        FactoryModel renamedStep = Campaign1PublicationExperimentTest.withStep(base, 0, 0,
                s -> new OperationStepDefinition(s.stepId(), "Unload", s.eligibleResources(), s.duration()));
        List<SemanticChange> changes = COMPARATOR.compare(artifact(base), artifact(renamedStep));
        assertEquals(1, changes.size());
        // Observed vocabulary: a step-name-only edit is described with the routing/duration phrase.
        assertEquals("step routing/duration changed", changes.get(0).detail());
        FactoryModel reorderedSteps = Campaign1PublicationExperimentTest.withOperation(base, 0,
                o -> new OperationDefinition(o.id(), o.name(), List.of(o.steps().get(1), o.steps().get(0))));
        assertEquals("step routing/duration changed", COMPARATOR.compare(artifact(base), artifact(reorderedSteps)).get(0).detail());
    }

    // -------------------------------------------------------------- assembly preservation

    record ResourceFact(long id, String name, int concurrency, Long capacityBits, long setupTime) {}

    static Long bits(Double value) {
        return value == null ? null : Double.isNaN(value) ? Campaign1Oracle.CANONICAL_NAN : Double.doubleToRawLongBits(value);
    }

    @Test
    void canonicalRecordComponentsAreExactlyTheSpecifiedAuthoredFacts() {
        Map<Class<?>, List<String>> specified = Map.of(
                FactoryModel.class, List.of("resources", "operations", "products"),
                ConfiguredResource.class, List.of("id", "name", "concurrency", "capacityLiters", "setupTime"),
                OperationDefinition.class, List.of("id", "name", "steps"),
                OperationStepDefinition.class, List.of("stepId", "name", "eligibleResources", "duration"),
                ProductDefinition.class, List.of("id", "name", "operationId"));
        specified.forEach((type, components) -> assertEquals(components,
                Arrays.stream(type.getRecordComponents()).map(RecordComponent::getName).toList(), type.getName()));
    }

    @Test
    void assemblyPreservesEveryAuthoredFactForSeededModels() {
        Random random = new Random(Campaign1CodecExperimentTest.SEED ^ 0xA55E);
        for (int i = 0; i < 500; i++) {
            FactoryModel model = Campaign1CodecExperimentTest.generate(random);
            FactoryModelVersion version = FactoryModelPublisher.publish(model);
            FactoryRuntime runtime = FactoryRuntime.forModel(version);
            assertEquals(version, runtime.modelVersion());
            List<ResourceFact> authored = model.resources().stream().map(r -> new ResourceFact(r.id().value(), r.name(),
                    r.concurrency(), bits(r.capacityLiters()), r.setupTime())).toList();
            List<ResourceFact> observed = runtime.observe().resources().stream().map((ResourceObservation r) -> new ResourceFact(
                    r.machineId().value(), r.name(), r.concurrency(), bits(r.capacityLiters()), r.setupTime())).toList();
            // The observation projection orders resources by ascending ID (oracle correction: an
            // earlier revision of this experiment wrongly expected authored list order here).
            List<ResourceFact> authoredById = new ArrayList<>(authored);
            authoredById.sort(java.util.Comparator.comparingLong(ResourceFact::id));
            assertEquals(authoredById, observed, "model " + i);
            FactoryRuntimeAssembler.Assembled assembled = FactoryRuntimeAssembler.assemble(version);
            // The assembled machine store itself retains authored list order.
            assertEquals(authored, assembled.factory().machinesView().stream().map(r -> new ResourceFact(r.id().value(),
                    r.name(), r.concurrency(), bits(r.capacityLiters()), r.setupTime())).toList(), "store " + i);
            assertEquals(model.products().stream().map(ProductDefinition::id).toList(), assembled.productIds());
            for (ProductDefinition product : model.products()) {
                Routing routing = assembled.factory().routings.getRoutingForProduct(product.id());
                OperationDefinition operation = model.operations().stream().filter(o -> o.id() == product.operationId()).findFirst().orElseThrow();
                assertEquals(operation.id(), routing.id());
                assertEquals(operation.name(), routing.name());
                List<RoutingStep> steps = routing.steps();
                assertEquals(operation.steps().size(), steps.size());
                for (int s = 0; s < steps.size(); s++) {
                    OperationStepDefinition authoredStep = operation.steps().get(s);
                    assertEquals(authoredStep.stepId(), steps.get(s).stepId());
                    assertEquals(authoredStep.name(), steps.get(s).name());
                    assertEquals(authoredStep.duration(), steps.get(s).duration());
                    assertEquals(new ArrayList<>(authoredStep.eligibleResources()), new ArrayList<>(steps.get(s).eligibleMachines()));
                }
            }
        }
    }

    static List<Event> run(FactoryRuntime runtime, ProductId product, long quantity) {
        runtime.submitWorkload(product, quantity, 2.5).orElseThrow();
        List<Event> events = new ArrayList<>();
        for (int guard = 0; guard < 10_000; guard++) {
            Optional<Event> event = runtime.advance();
            if (event.isEmpty()) {
                return events;
            }
            events.add(event.get());
        }
        throw new AssertionError("run did not drain");
    }

    @Test
    void sessionsAreIsolatedAndResourceMetadataChangesIdentityButNotExecution() {
        FactoryModel model = Campaign1CodecExperimentTest.representative();
        FactoryModelVersion version = FactoryModelPublisher.publish(model);
        FactoryRuntime first = FactoryRuntime.forModel(version);
        FactoryRuntime second = FactoryRuntime.forModel(version);
        var freshResources = FactoryRuntime.forModel(version).observe().resources();
        List<Event> trace = run(first, new ProductId(100), 4);
        assertTrue(trace.size() > 4, "trace " + trace);
        assertEquals(freshResources, second.observe().resources());
        assertEquals(0, second.observe().orders().size());
        assertNotEquals(first.runId(), second.runId());
        assertEquals(trace, run(second, new ProductId(100), 4));

        FactoryModel metadataOnly = new FactoryModel(List.of(
                new ConfiguredResource(new MachineId(1), "Mixer", 2, -0.0, Long.MIN_VALUE),
                new ConfiguredResource(new MachineId(-7), "Prüf;stand|A", 1, Double.NaN, Long.MAX_VALUE)),
                model.operations(), model.products());
        FactoryModelVersion changed = FactoryModelPublisher.publish(metadataOnly);
        assertNotEquals(version.fingerprint(), changed.fingerprint());
        assertEquals(trace, run(FactoryRuntime.forModel(changed), new ProductId(100), 4));
        assertEquals(Set.of("factory.resource#-7", "factory.resource#1"), new TreeSet<>(COMPARATOR.compare(artifact(model), artifact(metadataOnly))
                .stream().map(c -> c.entity().entityType() + "#" + c.entity().entityId()).toList()));
    }
}
