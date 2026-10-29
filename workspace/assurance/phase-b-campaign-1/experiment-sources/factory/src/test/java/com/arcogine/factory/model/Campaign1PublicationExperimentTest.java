package com.arcogine.factory.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.validation.FactoryModelValidationException;
import com.arcogine.factory.model.validation.FactoryModelValidator;
import com.arcogine.factory.model.validation.ModelValidationError;
import com.arcogine.types.MachineId;
import com.arcogine.types.ModelFingerprint;
import com.arcogine.types.ProductId;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * Transient Campaign 1 slice 1 experiment (not a merge candidate): publication predicates,
 * full-factorial predicate interactions, U2 diagnostic order across fresh JVMs, copy isolation
 * and refusal post-state, each against {@link Campaign1Oracle}.
 */
class Campaign1PublicationExperimentTest {

    static final String HIGH = "\uD83C";
    static final String LOW = "\uDF00";

    static Set<MachineId> ids(long... values) {
        LinkedHashSet<MachineId> set = new LinkedHashSet<>();
        for (long value : values) {
            set.add(new MachineId(value));
        }
        return set;
    }

    /** A valid base model with two resources, two operations (one two-step) and two products. */
    static FactoryModel base() {
        return new FactoryModel(
                List.of(new ConfiguredResource(new MachineId(1), "R1", 1, null, 0),
                        new ConfiguredResource(new MachineId(2), "R2", 2, 3.5, 7)),
                List.of(new OperationDefinition(10, "Op10", List.of(
                                new OperationStepDefinition(1, "S1", ids(1, 2), 4),
                                new OperationStepDefinition(2, "S2", ids(2), 1))),
                        new OperationDefinition(20, "Op20", List.of(
                                new OperationStepDefinition(1, "T1", ids(1), 2)))),
                List.of(new ProductDefinition(new ProductId(100), "P100", 10),
                        new ProductDefinition(new ProductId(200), "P200", 20)));
    }

    // ------------------------------------------------------------------ model surgery helpers

    static FactoryModel withResource(FactoryModel m, int index, UnaryOperator<ConfiguredResource> f) {
        List<ConfiguredResource> resources = new ArrayList<>(m.resources());
        resources.set(index, f.apply(resources.get(index)));
        return new FactoryModel(resources, m.operations(), m.products());
    }

    static FactoryModel withOperation(FactoryModel m, int index, UnaryOperator<OperationDefinition> f) {
        List<OperationDefinition> operations = new ArrayList<>(m.operations());
        operations.set(index, f.apply(operations.get(index)));
        return new FactoryModel(m.resources(), operations, m.products());
    }

    static FactoryModel withStep(FactoryModel m, int op, int step, UnaryOperator<OperationStepDefinition> f) {
        return withOperation(m, op, o -> {
            List<OperationStepDefinition> steps = new ArrayList<>(o.steps());
            steps.set(step, f.apply(steps.get(step)));
            return new OperationDefinition(o.id(), o.name(), steps);
        });
    }

    static FactoryModel withProduct(FactoryModel m, int index, UnaryOperator<ProductDefinition> f) {
        List<ProductDefinition> products = new ArrayList<>(m.products());
        products.set(index, f.apply(products.get(index)));
        return new FactoryModel(m.resources(), m.operations(), products);
    }

    static ConfiguredResource resource(ConfiguredResource r, Long id, String name, Integer concurrency) {
        return new ConfiguredResource(id == null ? r.id() : new MachineId(id), name == null ? r.name() : name,
                concurrency == null ? r.concurrency() : concurrency, r.capacityLiters(), r.setupTime());
    }

    static OperationStepDefinition step(OperationStepDefinition s, String name, Set<MachineId> eligible, Long duration) {
        return new OperationStepDefinition(s.stepId(), name == null ? s.name() : name,
                eligible == null ? s.eligibleResources() : eligible, duration == null ? s.duration() : duration);
    }

    // ------------------------------------------------------------------ product diagnostics -> oracle kinds

    private static final Pattern ENTITY = Pattern.compile("(?:Machine|Product)\\((-?\\d+)\\)|(-?\\d+)");

    private static String number(String text) {
        Matcher m = ENTITY.matcher(text);
        if (!m.matches()) {
            throw new AssertionError("unparseable entity coordinate: " + text);
        }
        return m.group(1) != null ? m.group(1) : m.group(2);
    }

    /**
     * Maps one validator diagnostic to the oracle's kind vocabulary. The field/message templates are
     * observed current vocabulary (the specification does not define diagnostic text); an
     * unrecognised diagnostic fails the experiment rather than being ignored.
     */
    static String kind(ModelValidationError e) {
        String f = e.field();
        String msg = e.message();
        Matcher m;
        if ((m = Pattern.compile("resources\\[(.+)]\\.name").matcher(f)).matches()
                && msg.equals("must contain only valid Unicode scalar values")) {
            return "NAME resource " + number(m.group(1));
        }
        if (f.equals("resources") && msg.startsWith("duplicate resource id: ")) {
            return "DUPLICATE resource " + number(msg.substring("duplicate resource id: ".length()));
        }
        if ((m = Pattern.compile("resources\\[(.+)]\\.concurrency").matcher(f)).matches() && msg.equals("must be > 0")) {
            return "CONCURRENCY resource " + number(m.group(1));
        }
        if ((m = Pattern.compile("operations\\[(-?\\d+)]\\.name").matcher(f)).matches()) {
            return "NAME operation " + m.group(1);
        }
        if (f.equals("operations") && msg.startsWith("duplicate operation id: ")) {
            return "DUPLICATE operation " + number(msg.substring("duplicate operation id: ".length()));
        }
        if ((m = Pattern.compile("operations\\[(-?\\d+)]\\.steps").matcher(f)).matches() && msg.equals("must not be empty")) {
            return "EMPTY_STEPS operation " + m.group(1);
        }
        if ((m = Pattern.compile("operations\\[(-?\\d+)]\\.steps\\[(-?\\d+)]\\.name").matcher(f)).matches()) {
            return "NAME step " + m.group(1) + "/" + m.group(2);
        }
        if ((m = Pattern.compile("operations\\[(-?\\d+)]\\.steps\\[(-?\\d+)]\\.duration").matcher(f)).matches()
                && msg.equals("must be > 0")) {
            return "DURATION step " + m.group(1) + "/" + m.group(2);
        }
        if ((m = Pattern.compile("operations\\[(-?\\d+)]\\.steps\\[(-?\\d+)]\\.eligibleResources").matcher(f)).matches()) {
            if (msg.equals("must reference at least one resource")) {
                return "EMPTY_ELIGIBLE step " + m.group(1) + "/" + m.group(2);
            }
            if (msg.startsWith("references nonexistent resource id: ")) {
                return "DANGLING_RESOURCE step " + m.group(1) + "/" + m.group(2) + " -> "
                        + number(msg.substring("references nonexistent resource id: ".length()));
            }
        }
        if ((m = Pattern.compile("products\\[(.+)]\\.name").matcher(f)).matches()) {
            return "NAME product " + number(m.group(1));
        }
        if (f.equals("products") && msg.startsWith("duplicate product id: ")) {
            return "DUPLICATE product " + number(msg.substring("duplicate product id: ".length()));
        }
        if ((m = Pattern.compile("products\\[(.+)]\\.operationId").matcher(f)).matches()
                && msg.startsWith("references nonexistent operation id: ")) {
            return "DANGLING_OPERATION product " + number(m.group(1)) + " -> "
                    + msg.substring("references nonexistent operation id: ".length());
        }
        throw new AssertionError("unrecognised diagnostic: " + e);
    }

    static List<String> productKinds(FactoryModel model) {
        return FactoryModelValidator.validate(model).errors().stream().map(Campaign1PublicationExperimentTest::kind).toList();
    }

    /** Asserts oracle agreement, U2 order and publication/refusal shape for one model. */
    static void check(String label, FactoryModel model) {
        List<String> expected = Campaign1Oracle.violations(Campaign1Oracle.neutral(model));
        List<ModelValidationError> errors = FactoryModelValidator.validate(model).errors();
        List<String> actual = errors.stream().map(Campaign1PublicationExperimentTest::kind).toList();
        assertEquals(Campaign1Oracle.multiset(expected), Campaign1Oracle.multiset(actual), label);
        assertAscendingDanglingPerStep(label, actual);
        if (expected.isEmpty()) {
            FactoryModelVersion version = FactoryModelPublisher.publish(model);
            assertSame(model, version.model(), label);
            assertEquals(Campaign1Oracle.fingerprint(Campaign1Oracle.encode(Campaign1Oracle.neutral(model))),
                    version.fingerprint().toString(), label);
        } else {
            FactoryModelValidationException refused =
                    assertThrows(FactoryModelValidationException.class, () -> FactoryModelPublisher.publish(model), label);
            assertEquals(errors, refused.result().errors(), label);
            assertEquals("factory model failed validation: " + errors, refused.getMessage(), label);
            assertThrows(UnsupportedOperationException.class, () -> refused.result().errors().clear(), label);
            FactoryModelValidationException direct =
                    assertThrows(FactoryModelValidationException.class, () -> new FactoryModelVersion(model), label);
            assertEquals(errors, direct.result().errors(), label);
        }
    }

    /** U2: one step's dangling-resource diagnostics appear in ascending signed MachineId order. */
    static void assertAscendingDanglingPerStep(String label, List<String> kinds) {
        java.util.Map<String, List<Long>> perStep = new java.util.LinkedHashMap<>();
        for (String k : kinds) {
            if (k.startsWith("DANGLING_RESOURCE step ")) {
                String[] parts = k.substring("DANGLING_RESOURCE step ".length()).split(" -> ");
                perStep.computeIfAbsent(parts[0], x -> new ArrayList<>()).add(Long.parseLong(parts[1]));
            }
        }
        perStep.forEach((stepKey, list) -> {
            List<Long> sorted = new ArrayList<>(list);
            sorted.sort(Long::compare);
            assertEquals(sorted, list, label + " step " + stepKey);
        });
    }

    // ------------------------------------------------------------------ experiments

    @Test
    void everySinglePredicateAndItsAdmittedBoundaryNeighbour() {
        List<Object[]> cases = new ArrayList<>();
        FactoryModel b = base();
        cases.add(new Object[] {"base", b});
        for (int concurrency : new int[] {Integer.MIN_VALUE, -1, 0, 1, 2, Integer.MAX_VALUE}) {
            cases.add(new Object[] {"concurrency " + concurrency, withResource(b, 0, r -> resource(r, null, null, concurrency))});
        }
        for (long duration : new long[] {Long.MIN_VALUE, -1, 0, 1, Long.MAX_VALUE}) {
            cases.add(new Object[] {"duration " + duration, withStep(b, 0, 1, s -> step(s, null, null, duration))});
        }
        for (long id : new long[] {Long.MIN_VALUE, -1, 0, Long.MAX_VALUE}) {
            // admitted: signed IDs carry no positivity requirement; references updated consistently
            FactoryModel renamed = new FactoryModel(
                    List.of(new ConfiguredResource(new MachineId(id), "R", 1, null, Long.MIN_VALUE)),
                    List.of(new OperationDefinition(id, "O", List.of(new OperationStepDefinition(id, "S", ids(id), 1)))),
                    List.of(new ProductDefinition(new ProductId(id), "P", id)));
            cases.add(new Object[] {"signed id " + id, renamed});
        }
        for (Double capacity : new Double[] {null, -1.0, -0.0, 0.0, Double.NaN, Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY, Double.MIN_VALUE, Double.MAX_VALUE}) {
            cases.add(new Object[] {"capacity " + capacity, withResource(b, 1,
                    r -> new ConfiguredResource(r.id(), r.name(), r.concurrency(), capacity, r.setupTime()))});
        }
        for (long setup : new long[] {Long.MIN_VALUE, -1, 0, Long.MAX_VALUE}) {
            cases.add(new Object[] {"setup " + setup, withResource(b, 1,
                    r -> new ConfiguredResource(r.id(), r.name(), r.concurrency(), r.capacityLiters(), setup))});
        }
        cases.add(new Object[] {"duplicate resource", withResource(b, 1, r -> resource(r, 1L, null, null))});
        cases.add(new Object[] {"duplicate operation", withOperation(b, 1, o -> new OperationDefinition(10, o.name(), o.steps()))});
        cases.add(new Object[] {"duplicate product", withProduct(b, 1, p -> new ProductDefinition(new ProductId(100), p.name(), p.operationId()))});
        cases.add(new Object[] {"empty steps", withOperation(b, 1, o -> new OperationDefinition(o.id(), o.name(), List.of()))});
        cases.add(new Object[] {"null steps", withOperation(b, 1, o -> new OperationDefinition(o.id(), o.name(), null))});
        cases.add(new Object[] {"empty eligible", withStep(b, 0, 0, s -> step(s, null, Set.of(), null))});
        cases.add(new Object[] {"null eligible", withStep(b, 0, 0, s -> new OperationStepDefinition(s.stepId(), s.name(), null, s.duration()))});
        cases.add(new Object[] {"dangling resource", withStep(b, 0, 0, s -> step(s, null, ids(1, 2, 3), null))});
        cases.add(new Object[] {"all dangling", withStep(b, 0, 0, s -> step(s, null, ids(Long.MAX_VALUE, -5, 3), null))});
        cases.add(new Object[] {"dangling operation", withProduct(b, 0, p -> new ProductDefinition(p.id(), p.name(), 11))});
        cases.add(new Object[] {"empty model", new FactoryModel(null, null, null)});
        cases.add(new Object[] {"resources only", new FactoryModel(b.resources(), List.of(), List.of())});
        cases.add(new Object[] {"operation without product", new FactoryModel(b.resources(), b.operations(), List.of())});
        cases.add(new Object[] {"two products one operation", withProduct(b, 1, p -> new ProductDefinition(p.id(), p.name(), 10))});
        cases.add(new Object[] {"duplicate step ids admitted", withStep(b, 0, 1, s -> new OperationStepDefinition(1, s.name(), s.eligibleResources(), s.duration()))});
        List<String> badNames = List.of(HIGH, LOW, "a" + HIGH, LOW + "a", LOW + HIGH, HIGH + HIGH + LOW, "x" + HIGH + "y");
        List<String> goodNames = List.of("", "\u0000", "🌀", "􏿿", "a:b|c;d", "￿", "﻿");
        for (String name : concat(badNames, goodNames)) {
            cases.add(new Object[] {"resource name " + escape(name), withResource(b, 0, r -> resource(r, null, name, null))});
            cases.add(new Object[] {"operation name " + escape(name), withOperation(b, 0, o -> new OperationDefinition(o.id(), name, o.steps()))});
            cases.add(new Object[] {"step name " + escape(name), withStep(b, 0, 1, s -> step(s, name, null, null))});
            cases.add(new Object[] {"product name " + escape(name), withProduct(b, 1, p -> new ProductDefinition(p.id(), name, p.operationId()))});
        }
        int refused = 0;
        for (Object[] c : cases) {
            check((String) c[0], (FactoryModel) c[1]);
            if (!Campaign1Oracle.violations(Campaign1Oracle.neutral((FactoryModel) c[1])).isEmpty()) {
                refused++;
            }
        }
        assertEquals(100, cases.size());
        assertEquals(44, refused);
    }

    static <T> List<T> concat(List<T> a, List<T> b) {
        List<T> out = new ArrayList<>(a);
        out.addAll(b);
        return out;
    }

    static String escape(String s) {
        StringBuilder out = new StringBuilder();
        for (char c : s.toCharArray()) {
            out.append(c < 0x20 || c > 0x7e ? String.format("\\u%04X", (int) c) : String.valueOf(c));
        }
        return out.toString();
    }

    /** Full factorial over twelve interacting predicate toggles (4096 models), not pairwise. */
    static FactoryModel toggled(int mask) {
        FactoryModel m = base();
        if ((mask & 1) != 0) {
            m = withResource(m, 1, r -> resource(r, 1L, null, null)); // duplicate resource id; step refs to 2 dangle
        }
        if ((mask & 2) != 0) {
            m = withResource(m, 1, r -> resource(r, null, null, 0));
        }
        if ((mask & 4) != 0) {
            m = withResource(m, 0, r -> resource(r, null, "R" + HIGH, null));
        }
        if ((mask & 8) != 0) {
            m = withOperation(m, 1, o -> new OperationDefinition(10, o.name(), o.steps())); // duplicate operation
        }
        if ((mask & 16) != 0) {
            m = withOperation(m, 1, o -> new OperationDefinition(o.id(), o.name(), List.of()));
        }
        if ((mask & 32) != 0) {
            m = withStep(m, 0, 1, s -> step(s, null, null, 0L));
        }
        if ((mask & 64) != 0) {
            m = withStep(m, 0, 0, s -> step(s, null, Set.of(), null));
        }
        if ((mask & 128) != 0) {
            m = withStep(m, 0, 1, s -> step(s, null, ids(9, 2, -9), null));
        }
        if ((mask & 256) != 0) {
            m = withProduct(m, 1, p -> new ProductDefinition(new ProductId(100), p.name(), p.operationId()));
        }
        if ((mask & 512) != 0) {
            m = withProduct(m, 0, p -> new ProductDefinition(p.id(), p.name(), 99));
        }
        if ((mask & 1024) != 0) {
            m = withStep(m, 0, 0, s -> step(s, LOW + "S", null, null));
        }
        if ((mask & 2048) != 0) {
            m = withProduct(m, 1, p -> new ProductDefinition(p.id(), p.name() + HIGH, p.operationId()));
        }
        return m;
    }

    @Test
    void fullFactorialPredicateInteractionsAgreeWithTheOracle() {
        int valid = 0;
        for (int mask = 0; mask < 4096; mask++) {
            FactoryModel model = toggled(mask);
            check("mask " + Integer.toBinaryString(mask), model);
            if (Campaign1Oracle.violations(Campaign1Oracle.neutral(model)).isEmpty()) {
                valid++;
            }
        }
        // Every toggle violates a predicate on its own and none cancels another, so only mask 0 is valid.
        assertEquals(1, valid);
    }

    /** U2 witness over signed IDs: every one of the 720 authored orders yields ascending diagnostics. */
    static String signedWitnessMessages(List<Long> authored) {
        long[] values = authored.stream().mapToLong(Long::longValue).toArray();
        FactoryModel model = new FactoryModel(List.of(),
                List.of(new OperationDefinition(1, "Op", List.of(new OperationStepDefinition(1, "S", ids(values), 1)))),
                List.of());
        return String.join("|", FactoryModelValidator.validate(model).errors().stream().map(ModelValidationError::toString).toList());
    }

    static final List<Long> SIGNED = List.of(Long.MAX_VALUE, -1L, 0L, Long.MIN_VALUE, 7L, -7L);
    static final String SIGNED_EXPECTED = String.join("|", List.of(Long.MIN_VALUE, -7L, -1L, 0L, 7L, Long.MAX_VALUE).stream()
            .map(v -> "operations[1].steps[1].eligibleResources: references nonexistent resource id: Machine(" + v + ")").toList());

    static List<List<Long>> permutations(List<Long> values) {
        if (values.isEmpty()) {
            return List.of(List.of());
        }
        List<List<Long>> out = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            List<Long> rest = new ArrayList<>(values);
            Long head = rest.remove(i);
            for (List<Long> tail : permutations(rest)) {
                List<Long> p = new ArrayList<>();
                p.add(head);
                p.addAll(tail);
                out.add(p);
            }
        }
        return out;
    }

    @Test
    void signedEligibleDiagnosticsAreAscendingForEveryAuthoredOrder() {
        List<List<Long>> all = permutations(SIGNED);
        assertEquals(720, all.size());
        for (List<Long> p : all) {
            assertEquals(SIGNED_EXPECTED, signedWitnessMessages(p), p.toString());
        }
        // Mutable HashSet source with a deliberately colliding-bucket spread.
        HashSet<MachineId> hashed = new HashSet<>();
        SIGNED.forEach(v -> hashed.add(new MachineId(v)));
        assertEquals(List.of(Long.MIN_VALUE, -7L, -1L, 0L, 7L, Long.MAX_VALUE),
                new OperationStepDefinition(1, "S", hashed, 1).eligibleResources().stream().map(MachineId::value).toList());
    }

    /** Child-JVM entry point: prints the signed witness and every compound-invalid toggle model's diagnostics. */
    public static void main(String[] args) {
        System.out.println(signedWitnessMessages(SIGNED));
        for (int mask = 0; mask < 4096; mask += 37) {
            System.out.println(mask + " " + FactoryModelValidator.validate(toggled(mask)).errors());
        }
    }

    @Test
    void completeDiagnosticSequencesAreIdenticalAcrossFreshJvms() throws Exception {
        List<String> inProcess = new ArrayList<>();
        inProcess.add(signedWitnessMessages(SIGNED));
        for (int mask = 0; mask < 4096; mask += 37) {
            inProcess.add(mask + " " + FactoryModelValidator.validate(toggled(mask)).errors());
        }
        assertEquals(SIGNED_EXPECTED, inProcess.get(0));
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        List<String> witnesses = new ArrayList<>();
        for (int run = 0; run < 8; run++) {
            Process process = new ProcessBuilder(java, "-cp", System.getProperty("factory.test.classpath"),
                    Campaign1PublicationExperimentTest.class.getName()).redirectErrorStream(true).start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (!process.waitFor(60, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new AssertionError("child JVM timed out");
            }
            assertEquals(0, process.exitValue(), output);
            assertEquals(inProcess, output.lines().toList(), "fresh JVM " + run);
            witnesses.add(Integer.toHexString(output.hashCode()));
        }
        String dir = System.getenv("CAMPAIGN1_RESULTS_DIR");
        if (dir != null) {
            Files.createDirectories(Path.of(dir));
            Files.write(Path.of(dir, "fresh-jvm-diagnostics.txt"), inProcess, StandardCharsets.UTF_8);
        }
        assertEquals(1, new HashSet<>(witnesses).size(), witnesses.toString());
    }

    @Test
    void sourceAndAccessorCollectionsAreIsolatedAtEveryNestingDepth() {
        HashSet<MachineId> eligible = new HashSet<>(ids(2, 1));
        ArrayList<OperationStepDefinition> steps = new ArrayList<>(List.of(new OperationStepDefinition(1, "S", eligible, 3)));
        ArrayList<OperationDefinition> operations = new ArrayList<>(List.of(new OperationDefinition(10, "O", steps)));
        ArrayList<ConfiguredResource> resources = new ArrayList<>(List.of(
                new ConfiguredResource(new MachineId(1), "A", 1, null, 0), new ConfiguredResource(new MachineId(2), "B", 1, null, 0)));
        ArrayList<ProductDefinition> products = new ArrayList<>(List.of(new ProductDefinition(new ProductId(5), "P", 10)));
        FactoryModel model = new FactoryModel(resources, operations, products);
        FactoryModelVersion version = FactoryModelPublisher.publish(model);
        ModelFingerprint before = version.fingerprint();
        byte[] bytesBefore = FactoryModelArtifact.encode(version);

        eligible.add(new MachineId(99));
        eligible.remove(new MachineId(1));
        steps.add(new OperationStepDefinition(2, "X", ids(1), 1));
        steps.set(0, new OperationStepDefinition(1, "Y", ids(1), 9));
        operations.add(new OperationDefinition(11, "O2", List.of()));
        resources.remove(0);
        resources.add(new ConfiguredResource(new MachineId(3), "C", 0, null, 0));
        products.clear();

        assertEquals(before, version.fingerprint());
        assertArrayEquals(bytesBefore, FactoryModelArtifact.encode(version));
        assertEquals(Set.of(new MachineId(1), new MachineId(2)), version.model().operations().get(0).steps().get(0).eligibleResources());
        assertEquals(1, version.model().operations().get(0).steps().size());
        assertTrue(FactoryModelValidator.validate(version.model()).isValid());

        FactoryModel published = version.model();
        assertThrows(UnsupportedOperationException.class, () -> published.resources().clear());
        assertThrows(UnsupportedOperationException.class, () -> published.operations().clear());
        assertThrows(UnsupportedOperationException.class, () -> published.products().clear());
        assertThrows(UnsupportedOperationException.class, () -> published.operations().get(0).steps().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> published.operations().get(0).steps().get(0).eligibleResources().add(new MachineId(7)));
        assertThrows(UnsupportedOperationException.class,
                () -> published.operations().get(0).steps().get(0).eligibleResources().iterator().remove());
        assertEquals(before, version.fingerprint());
    }

    @Test
    void refusedPublicationLeavesNoPublishedStateAndARetryCanSucceed() {
        ArrayList<ConfiguredResource> resources = new ArrayList<>(List.of(new ConfiguredResource(new MachineId(1), "A", 0, null, 0)));
        FactoryModel invalid = new FactoryModel(resources, List.of(), List.of());
        FactoryModelValidationException e = assertThrows(FactoryModelValidationException.class,
                () -> FactoryModelPublisher.publish(invalid));
        assertEquals(List.of(new ModelValidationError("resources[Machine(1)].concurrency", "must be > 0")), e.result().errors());
        assertFalse(e.result().isValid());
        // The refused model is untouched and still reports the same violation; publishing a corrected copy succeeds.
        assertEquals(e.result(), FactoryModelValidator.validate(invalid));
        resources.set(0, new ConfiguredResource(new MachineId(1), "A", 1, null, 0));
        assertEquals(e.result(), FactoryModelValidator.validate(invalid));
        FactoryModelVersion version = FactoryModelPublisher.publish(new FactoryModel(resources, List.of(), List.of()));
        assertEquals(1, version.model().resources().get(0).concurrency());
    }

    @Test
    void constructionRefusalsPrecedePublicationAndAreNotPublicationPredicates() {
        assertThrows(NullPointerException.class, () -> new ConfiguredResource(null, "a", 1, null, 0));
        assertThrows(NullPointerException.class, () -> new ConfiguredResource(new MachineId(1), null, 1, null, 0));
        assertThrows(NullPointerException.class, () -> new OperationDefinition(1, null, List.of()));
        assertThrows(NullPointerException.class, () -> new OperationStepDefinition(1, null, Set.of(), 1));
        assertThrows(NullPointerException.class, () -> new ProductDefinition(null, "p", 1));
        assertThrows(NullPointerException.class, () -> new ProductDefinition(new ProductId(1), null, 1));
        HashSet<MachineId> withNull = new HashSet<>();
        withNull.add(null);
        assertThrows(NullPointerException.class, () -> new OperationStepDefinition(1, "s", withNull, 1));
        List<ConfiguredResource> nullElement = new ArrayList<>();
        nullElement.add(null);
        assertThrows(NullPointerException.class, () -> new FactoryModel(nullElement, List.of(), List.of()));
        assertThrows(NullPointerException.class, () -> FactoryModelPublisher.publish(null));
        assertThrows(NullPointerException.class, () -> new FactoryModelVersion(null));
    }
}
