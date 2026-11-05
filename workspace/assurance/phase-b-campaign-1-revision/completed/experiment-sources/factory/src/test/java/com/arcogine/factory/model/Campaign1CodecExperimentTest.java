package com.arcogine.factory.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.validation.FactoryModelValidationException;
import com.arcogine.governance.SemanticArtifactVerifier;
import com.arcogine.types.MachineId;
import com.arcogine.types.ModelFingerprint;
import com.arcogine.types.ProductId;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/**
 * Transient Campaign 1 slice 2 experiment (not a merge candidate). Golden hex/fingerprints come
 * from oracles/canonical_vectors.py (independent Python implementation of factory-model.md 4);
 * the differential decoder oracle is {@link Campaign1Oracle}.
 */
class Campaign1CodecExperimentTest {

    static final String EMPTY_HEX =
            "6172636f67696e652e666163746f72792d6d6f64656c00000000000000000000000000000000000000000000000000";
    static final String EMPTY_FP = "factory-model:sha256:d3ab64403c0661912c50db407c998a1af8b7ce86b96d03e5746b4869402363d0";
    static final String REPRESENTATIVE_HEX = "6172636f67696e652e666163746f72792d6d6f64656c000000000000000002000000000000000100000000000000054d697865720000000000000002014097720000000000000000000000001efffffffffffffff9000000000000000d5072c3bc663b7374616e647c4100000000000000010000000000000000000000000000000001000000000000000a0000000000000005426c656e640000000000000002000000000000000100000000000000044c6f616400000000000000050000000000000002fffffffffffffff90000000000000001000000000000000200000000000000084d697820f09f8c800000000000000009000000000000000100000000000000010000000000000001000000000000006400000000000000055061696e74000000000000000a";
    static final String REPRESENTATIVE_FP = "factory-model:sha256:7457e1cac148322b81b07e0f07942a452679e51e8279be15bcf44e20d12faa79";
    static final String EXTREMES_HEX = "6172636f67696e652e666163746f72792d6d6f64656c0000000000000000057fffffffffffffff00000000000000036d6178000000007fffffff0180000000000000008000000000000000800000000000000000000000000000036d696e00000000000000010100000000000000007fffffffffffffff000000000000000000000000000000036e616e0000000000000001017ff8000000000000ffffffffffffffffffffffffffffffff0000000000000003696e660000000000000001017ff00000000000000000000000000000000000000000000200000000000000076e65672d696e66000000000000000101fff0000000000000000000000000000000000000000000018000000000000000000000000000000000000000000000027fffffffffffffff0000000000000005003a7c3b0a7fffffffffffffff00000000000000058000000000000000ffffffffffffffff000000000000000000000000000000027fffffffffffffff7fffffffffffffff000000000000000b6475702d737465702d6964000000000000000100000000000000018000000000000000000000000000000280000000000000000000000000000006e697a5e69cac80000000000000007fffffffffffffff0000000000000004f48fbfbf8000000000000000";
    static final String EXTREMES_FP = "factory-model:sha256:81f6150dc935ee3ca2f46b222d6cd5ab8e4311e76c88597604ac87f1eb685938";

    static Set<MachineId> ids(long... values) {
        return Campaign1PublicationExperimentTest.ids(values);
    }

    static FactoryModel representative() {
        return new FactoryModel(
                List.of(new ConfiguredResource(new MachineId(1), "Mixer", 2, 1500.5, 30),
                        new ConfiguredResource(new MachineId(-7), "Prüf;stand|A", 1, null, 0)),
                List.of(new OperationDefinition(10, "Blend", List.of(
                        new OperationStepDefinition(1, "Load", ids(1, -7), 5),
                        new OperationStepDefinition(2, "Mix 🌀", ids(1), 9)))),
                List.of(new ProductDefinition(new ProductId(100), "Paint", 10)));
    }

    static FactoryModel extremes() {
        long max = Long.MAX_VALUE;
        long min = Long.MIN_VALUE;
        return new FactoryModel(
                List.of(new ConfiguredResource(new MachineId(max), "max", Integer.MAX_VALUE, -0.0, min),
                        new ConfiguredResource(new MachineId(min), "min", 1, 0.0, max),
                        new ConfiguredResource(new MachineId(0), "nan", 1, Double.NaN, -1),
                        new ConfiguredResource(new MachineId(-1), "inf", 1, Double.POSITIVE_INFINITY, 0),
                        new ConfiguredResource(new MachineId(2), "neg-inf", 1, Double.NEGATIVE_INFINITY, 0)),
                List.of(new OperationDefinition(min, "", List.of(
                        new OperationStepDefinition(max, "\u0000:|;\n", ids(max, min, 0, -1, 2), max),
                        new OperationStepDefinition(max, "dup-step-id", ids(min), 1)))),
                List.of(new ProductDefinition(new ProductId(min), "日本", min),
                        new ProductDefinition(new ProductId(max), "􏿿", min)));
    }

    static byte[] hex(String hex) {
        return HexFormat.of().parseHex(hex);
    }

    @Test
    void independentGoldenVectorsMatchProductBytesFingerprintsAndDecoding() {
        Object[][] vectors = {
            {new FactoryModel(List.of(), List.of(), List.of()), EMPTY_HEX, EMPTY_FP},
            {representative(), REPRESENTATIVE_HEX, REPRESENTATIVE_FP},
            {extremes(), EXTREMES_HEX, EXTREMES_FP},
        };
        for (Object[] v : vectors) {
            FactoryModel model = (FactoryModel) v[0];
            byte[] expected = hex((String) v[1]);
            FactoryModelVersion version = FactoryModelPublisher.publish(model);
            assertEquals(v[1], HexFormat.of().formatHex(FactoryModelArtifact.encode(version)));
            assertEquals(v[2], version.fingerprint().toString());
            assertArrayEquals(expected, Campaign1Oracle.encode(Campaign1Oracle.neutral(model)));
            assertEquals(v[2], Campaign1Oracle.fingerprint(expected));
            FactoryModelVersion decoded = FactoryModelArtifact.decode(expected);
            assertEquals(model, decoded.model());
            assertArrayEquals(expected, FactoryModelArtifact.encode(decoded));
            assertEquals(v[2], FactoryModelArtifact.fingerprint(expected).toString());
            assertEquals(v[2], FactoryModelArtifact.verifier().fingerprint(expected).toString());
            assertArrayEquals(Campaign1Oracle.PREFIX, java.util.Arrays.copyOf(expected, 23));
        }
    }

    @Test
    void decodedSignedZeroAndNanAreTheAuthoredValues() {
        FactoryModel decoded = FactoryModelArtifact.decode(hex(EXTREMES_HEX)).model();
        assertEquals(0x8000000000000000L, Double.doubleToRawLongBits(decoded.resources().get(0).capacityLiters()));
        assertEquals(0L, Double.doubleToRawLongBits(decoded.resources().get(1).capacityLiters()));
        assertTrue(Double.isNaN(decoded.resources().get(2).capacityLiters()));
        assertEquals(List.of(Long.MIN_VALUE, -1L, 0L, 2L, Long.MAX_VALUE),
                decoded.operations().get(0).steps().get(0).eligibleResources().stream().map(MachineId::value).toList());
        // Every admitted NaN payload, either sign, canonicalizes to the same bytes and fingerprint.
        long[] nans = {0x7ff8000000000000L, 0x7ff0000000000001L, 0xfff8000000000000L, 0x7fffffffffffffffL, 0xffffffffffffffffL};
        Set<String> fingerprints = new TreeSet<>();
        for (long bits : nans) {
            FactoryModel m = new FactoryModel(
                    List.of(new ConfiguredResource(new MachineId(1), "r", 1, Double.longBitsToDouble(bits), 0)), List.of(), List.of());
            fingerprints.add(FactoryModelPublisher.publish(m).fingerprint().toString());
        }
        assertEquals(1, fingerprints.size());
        FactoryModel positive = new FactoryModel(List.of(new ConfiguredResource(new MachineId(1), "r", 1, 0.0, 0)), List.of(), List.of());
        FactoryModel negative = new FactoryModel(List.of(new ConfiguredResource(new MachineId(1), "r", 1, -0.0, 0)), List.of(), List.of());
        assertNotEquals(FactoryModelPublisher.publish(positive).fingerprint(), FactoryModelPublisher.publish(negative).fingerprint());
    }

    @Test
    void identityIsInvariantToEligibleIterationAndSensitiveToListOrderAndEveryField() {
        FactoryModel extremes = extremes();
        ModelFingerprint reference = FactoryModelPublisher.publish(extremes).fingerprint();
        List<Long> eligible = List.of(Long.MAX_VALUE, Long.MIN_VALUE, 0L, -1L, 2L);
        int permutations = 0;
        for (List<Long> p : Campaign1PublicationExperimentTest.permutations(eligible)) {
            LinkedHashSet<MachineId> authored = new LinkedHashSet<>();
            p.forEach(id -> authored.add(new MachineId(id)));
            FactoryModel permuted = Campaign1PublicationExperimentTest.withStep(extremes, 0, 0,
                    s -> new OperationStepDefinition(s.stepId(), s.name(), authored, s.duration()));
            assertEquals(reference, FactoryModelPublisher.publish(permuted).fingerprint(), p.toString());
            assertEquals(extremes, permuted);
            permutations++;
        }
        assertEquals(120, permutations);

        FactoryModel r = representative();
        ModelFingerprint base = FactoryModelPublisher.publish(r).fingerprint();
        List<FactoryModel> variants = new ArrayList<>();
        variants.add(new FactoryModel(List.of(r.resources().get(1), r.resources().get(0)), r.operations(), r.products()));
        variants.add(Campaign1PublicationExperimentTest.withOperation(r, 0,
                o -> new OperationDefinition(o.id(), o.name(), List.of(o.steps().get(1), o.steps().get(0)))));
        FactoryModel twoOps = new FactoryModel(r.resources(), List.of(r.operations().get(0),
                new OperationDefinition(11, "Other", List.of(new OperationStepDefinition(1, "x", ids(1), 1)))), r.products());
        variants.add(twoOps);
        variants.add(new FactoryModel(twoOps.resources(), List.of(twoOps.operations().get(1), twoOps.operations().get(0)), twoOps.products()));
        FactoryModel twoProducts = new FactoryModel(r.resources(), r.operations(),
                List.of(r.products().get(0), new ProductDefinition(new ProductId(101), "Other", 10)));
        variants.add(twoProducts);
        variants.add(new FactoryModel(r.resources(), r.operations(), List.of(twoProducts.products().get(1), twoProducts.products().get(0))));
        // one change of every canonical field
        variants.add(new FactoryModel(List.of(new ConfiguredResource(new MachineId(3), "Mixer", 2, 1500.5, 30), r.resources().get(1)),
                List.of(new OperationDefinition(10, "Blend", List.of(new OperationStepDefinition(1, "Load", ids(3, -7), 5),
                        new OperationStepDefinition(2, "Mix 🌀", ids(3), 9)))), r.products()));
        variants.add(Campaign1PublicationExperimentTest.withResource(r, 0, x -> new ConfiguredResource(x.id(), "Mixer ", x.concurrency(), x.capacityLiters(), x.setupTime())));
        variants.add(Campaign1PublicationExperimentTest.withResource(r, 0, x -> new ConfiguredResource(x.id(), x.name(), 3, x.capacityLiters(), x.setupTime())));
        variants.add(Campaign1PublicationExperimentTest.withResource(r, 0, x -> new ConfiguredResource(x.id(), x.name(), x.concurrency(), null, x.setupTime())));
        variants.add(Campaign1PublicationExperimentTest.withResource(r, 0, x -> new ConfiguredResource(x.id(), x.name(), x.concurrency(), Math.nextUp(1500.5), x.setupTime())));
        variants.add(Campaign1PublicationExperimentTest.withResource(r, 0, x -> new ConfiguredResource(x.id(), x.name(), x.concurrency(), x.capacityLiters(), 31)));
        variants.add(new FactoryModel(r.resources(), List.of(new OperationDefinition(12, "Blend", r.operations().get(0).steps())),
                List.of(new ProductDefinition(new ProductId(100), "Paint", 12))));
        variants.add(Campaign1PublicationExperimentTest.withOperation(r, 0, o -> new OperationDefinition(o.id(), "blend", o.steps())));
        variants.add(Campaign1PublicationExperimentTest.withStep(r, 0, 0, s -> new OperationStepDefinition(3, s.name(), s.eligibleResources(), s.duration())));
        variants.add(Campaign1PublicationExperimentTest.withStep(r, 0, 0, s -> new OperationStepDefinition(s.stepId(), "Load́", s.eligibleResources(), s.duration())));
        variants.add(Campaign1PublicationExperimentTest.withStep(r, 0, 0, s -> new OperationStepDefinition(s.stepId(), s.name(), ids(1), s.duration())));
        variants.add(Campaign1PublicationExperimentTest.withStep(r, 0, 0, s -> new OperationStepDefinition(s.stepId(), s.name(), s.eligibleResources(), 6)));
        variants.add(Campaign1PublicationExperimentTest.withProduct(r, 0, p -> new ProductDefinition(new ProductId(-100), p.name(), p.operationId())));
        variants.add(Campaign1PublicationExperimentTest.withProduct(r, 0, p -> new ProductDefinition(p.id(), "Paint ", p.operationId())));
        Set<String> all = new TreeSet<>();
        all.add(base.toString());
        for (FactoryModel variant : variants) {
            FactoryModelVersion version = FactoryModelPublisher.publish(variant);
            assertNotEquals(base, version.fingerprint(), variant.toString());
            assertEquals(Campaign1Oracle.fingerprint(Campaign1Oracle.encode(Campaign1Oracle.neutral(variant))), version.fingerprint().toString());
            all.add(version.fingerprint().toString());
        }
        // pairwise distinct as well
        assertEquals(variants.size() + 1, all.size());
    }

    // -------------------------------------------------------------- differential corpus

    record Outcome(String product, String oracle) {}

    static String productOutcome(byte[] bytes, Campaign1Oracle.Decoded oracle) {
        try {
            FactoryModelVersion decoded = FactoryModelArtifact.decode(bytes);
            if (!oracle.accepted()) {
                return "MISMATCH accepted-by-product refused-by-oracle(" + oracle.refusal() + ")";
            }
            if (!Campaign1Oracle.setNormalized(Campaign1Oracle.neutral(decoded.model())).equals(oracle.model())) {
                return "MISMATCH different model";
            }
            if (!FactoryModelArtifact.fingerprint(bytes).toString().equals(Campaign1Oracle.fingerprint(bytes))) {
                return "MISMATCH fingerprint not digest of accepted bytes";
            }
            return "accepted";
        } catch (FactoryModelValidationException e) {
            if (oracle.accepted() || !oracle.refusal().startsWith("publication predicates")) {
                return "MISMATCH refused-predicate where oracle " + (oracle.accepted() ? "accepted" : "refused grammar");
            }
            return "refused:" + e.getClass().getSimpleName();
        } catch (IllegalArgumentException e) {
            if (oracle.accepted() || oracle.refusal().startsWith("publication predicates")) {
                return "MISMATCH refused-grammar " + e.getCause() + " where oracle " + (oracle.accepted() ? "accepted" : oracle.refusal());
            }
            return "refused:" + e.getClass().getSimpleName();
        } catch (RuntimeException e) {
            return "MISMATCH unexpected " + e;
        }
    }

    @Test
    void exhaustiveSingleEditCorpusAgreesWithTheIndependentDecoder() throws Exception {
        Map<String, Integer> ledger = new TreeMap<>();
        List<String> mismatches = new ArrayList<>();
        int total = 0;
        for (String[] vector : new String[][] {{"empty", EMPTY_HEX}, {"representative", REPRESENTATIVE_HEX}, {"signed-extremes", EXTREMES_HEX}}) {
            byte[] golden = hex(vector[1]);
            List<byte[]> corpus = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            for (int length = 0; length < golden.length; length++) {
                corpus.add(java.util.Arrays.copyOf(golden, length));
                labels.add("truncate " + length);
            }
            for (int position = 0; position < golden.length; position++) {
                int original = golden[position] & 0xff;
                for (int replacement : new int[] {original ^ 0x01, original ^ 0x80, 0x00, 0xff, (original + 1) & 0xff, original ^ 0x40}) {
                    if (replacement == original) {
                        continue;
                    }
                    byte[] edited = golden.clone();
                    edited[position] = (byte) replacement;
                    corpus.add(edited);
                    labels.add("substitute " + position + "=" + replacement);
                }
                byte[] deleted = new byte[golden.length - 1];
                System.arraycopy(golden, 0, deleted, 0, position);
                System.arraycopy(golden, position + 1, deleted, position, golden.length - position - 1);
                corpus.add(deleted);
                labels.add("delete " + position);
            }
            for (int position = 0; position <= golden.length; position++) {
                for (int inserted : new int[] {0x00, 0x01, 0xff}) {
                    byte[] longer = new byte[golden.length + 1];
                    System.arraycopy(golden, 0, longer, 0, position);
                    longer[position] = (byte) inserted;
                    System.arraycopy(golden, position, longer, position + 1, golden.length - position);
                    corpus.add(longer);
                    labels.add("insert " + position + "=" + inserted);
                }
            }
            for (int i = 0; i < corpus.size(); i++) {
                Campaign1Oracle.Decoded oracle = Campaign1Oracle.decode(corpus.get(i));
                String outcome = productOutcome(corpus.get(i), oracle);
                String key = vector[0] + " " + outcome + " / oracle " + (oracle.accepted() ? "accepted" : oracle.refusal().replaceAll(":.*", ""));
                ledger.merge(key, 1, Integer::sum);
                if (outcome.startsWith("MISMATCH")) {
                    mismatches.add(vector[0] + " " + labels.get(i) + ": " + outcome);
                }
                total++;
            }
        }
        writeResult("codec-differential-ledger.txt", ledger.entrySet().stream().map(e -> e.getValue() + "\t" + e.getKey()).toList());
        writeResult("codec-differential-mismatches.txt", mismatches);
        assertTrue(total > 8000, "corpus " + total);
        assertEquals(List.of(), mismatches);
    }

    static void writeResult(String name, List<String> lines) throws Exception {
        String dir = System.getenv("CAMPAIGN1_RESULTS_DIR");
        if (dir != null) {
            Files.createDirectories(Path.of(dir));
            Files.write(Path.of(dir, name), lines, StandardCharsets.UTF_8);
        }
    }

    // -------------------------------------------------------------- targeted malformed corpus

    /** Builds a single-resource, single-op, single-product stream with raw overrides for fields. */
    static final class Stream {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        Stream raw(int... values) {
            for (int v : values) {
                out.write(v);
            }
            return this;
        }

        Stream raw(byte[] values) {
            out.writeBytes(values);
            return this;
        }

        Stream i64(long v) {
            for (int s = 56; s >= 0; s -= 8) {
                out.write((int) (v >>> s) & 0xff);
            }
            return this;
        }

        Stream text(byte[] utf8) {
            return i64(utf8.length).raw(utf8);
        }

        Stream text(String ascii) {
            return text(ascii.getBytes(StandardCharsets.US_ASCII));
        }

        byte[] bytes() {
            return out.toByteArray();
        }
    }

    record Fields(byte[] prefix, long resourceCount, byte[] resourceName, long concurrency, int marker, long capacityBits,
            long[] eligible, long duration, long stepCount, long productOperation, byte[] trailing) {}

    static Fields valid() {
        return new Fields(Campaign1Oracle.PREFIX, 1, "r".getBytes(StandardCharsets.US_ASCII), 1, 0, 0,
                new long[] {1}, 1, 1, 10, new byte[0]);
    }

    static byte[] build(Fields f) {
        Stream s = new Stream().raw(f.prefix()).i64(f.resourceCount());
        if (f.resourceCount() != 0) {
            s.i64(1).text(f.resourceName()).i64(f.concurrency()).raw(f.marker());
            if (f.marker() == 1) {
                s.i64(f.capacityBits());
            }
            s.i64(0);
        }
        s.i64(1).i64(10).text("op").i64(f.stepCount());
        if (f.stepCount() == 1) {
            s.i64(1).text("s").i64(f.duration()).i64(f.eligible().length);
            for (long id : f.eligible()) {
                s.i64(id);
            }
        }
        s.i64(1).i64(100).text("p").i64(f.productOperation());
        return s.raw(f.trailing()).bytes();
    }

    static Fields with(Fields f, String field, Object value) {
        return new Fields(
                field.equals("prefix") ? (byte[]) value : f.prefix(),
                field.equals("resourceCount") ? (Long) value : f.resourceCount(),
                field.equals("resourceName") ? (byte[]) value : f.resourceName(),
                field.equals("concurrency") ? (Long) value : f.concurrency(),
                field.equals("marker") ? (Integer) value : f.marker(),
                field.equals("capacityBits") ? (Long) value : f.capacityBits(),
                field.equals("eligible") ? (long[]) value : f.eligible(),
                field.equals("duration") ? (Long) value : f.duration(),
                field.equals("stepCount") ? (Long) value : f.stepCount(),
                field.equals("productOperation") ? (Long) value : f.productOperation(),
                field.equals("trailing") ? (byte[]) value : f.trailing());
    }

    static byte[] utf8(int... values) {
        byte[] out = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            out[i] = (byte) values[i];
        }
        return out;
    }

    @Test
    void targetedMalformedAndPredicateInvalidBytesAreRefusedWithTheRightClass() throws Exception {
        Fields v = valid();
        assertTrue(Campaign1Oracle.decode(build(v)).accepted());
        FactoryModelArtifact.decode(build(v));

        Map<String, byte[]> grammar = new java.util.LinkedHashMap<>();
        grammar.put("empty input", new byte[0]);
        grammar.put("prefix only", Campaign1Oracle.PREFIX);
        grammar.put("prefix without NUL", build(with(v, "prefix", "arcogine.factory-model".getBytes(StandardCharsets.US_ASCII))));
        grammar.put("discarded v1 prefix", build(with(v, "prefix", "arcogine.factory-model:v1\0".getBytes(StandardCharsets.US_ASCII))));
        grammar.put("discarded v2 prefix", build(with(v, "prefix", "arcogine.factory-model:v2\0".getBytes(StandardCharsets.US_ASCII))));
        grammar.put("uppercase prefix", build(with(v, "prefix", "ARCOGINE.factory-model\0".getBytes(StandardCharsets.US_ASCII))));
        grammar.put("trailing 00", build(with(v, "trailing", new byte[] {0})));
        grammar.put("trailing record", build(with(v, "trailing", build(v))));
        grammar.put("negative count", build(with(v, "resourceCount", -1L)));
        grammar.put("count 2^31", build(with(v, "resourceCount", 1L << 31)));
        grammar.put("count Long.MAX", build(with(v, "resourceCount", Long.MAX_VALUE)));
        grammar.put("count Integer.MAX (overrun)", build(with(v, "resourceCount", (long) Integer.MAX_VALUE)));
        grammar.put("count 2 with one record", build(with(v, "resourceCount", 2L)));
        grammar.put("text length overruns", new Stream().raw(Campaign1Oracle.PREFIX).i64(1).i64(1).i64(Integer.MAX_VALUE).bytes());
        grammar.put("text length negative", new Stream().raw(Campaign1Oracle.PREFIX).i64(1).i64(1).i64(-1).bytes());
        grammar.put("concurrency 2^31", build(with(v, "concurrency", 1L << 31)));
        grammar.put("concurrency 2^32+1 (wraps to 1)", build(with(v, "concurrency", (1L << 32) + 1)));
        grammar.put("concurrency -2^31-1", build(with(v, "concurrency", -(1L << 31) - 1)));
        grammar.put("marker 02", build(with(v, "marker", 2)));
        grammar.put("marker ff", build(with(v, "marker", 0xff)));
        grammar.put("noncanonical NaN 7ff0..01", build(with(with(v, "marker", 1), "capacityBits", 0x7ff0000000000001L)));
        grammar.put("noncanonical NaN fff8..00", build(with(with(v, "marker", 1), "capacityBits", 0xfff8000000000000L)));
        grammar.put("eligible unsorted", build(with(v, "eligible", new long[] {2, 1})));
        grammar.put("eligible duplicate", build(with(v, "eligible", new long[] {1, 1})));
        grammar.put("utf8 overlong NUL", build(with(v, "resourceName", utf8(0xC0, 0x80))));
        grammar.put("utf8 overlong 3-byte", build(with(v, "resourceName", utf8(0xE0, 0x80, 0xAF))));
        grammar.put("utf8 overlong 4-byte", build(with(v, "resourceName", utf8(0xF0, 0x80, 0x80, 0xAF))));
        grammar.put("utf8 encoded high surrogate", build(with(v, "resourceName", utf8(0xED, 0xA0, 0x80))));
        grammar.put("utf8 encoded low surrogate", build(with(v, "resourceName", utf8(0xED, 0xB0, 0x80))));
        grammar.put("utf8 CESU pair", build(with(v, "resourceName", utf8(0xED, 0xA0, 0xBC, 0xED, 0xBC, 0x80))));
        grammar.put("utf8 above U+10FFFF", build(with(v, "resourceName", utf8(0xF4, 0x90, 0x80, 0x80))));
        grammar.put("utf8 F5 lead", build(with(v, "resourceName", utf8(0xF5, 0x80, 0x80, 0x80))));
        grammar.put("utf8 5-byte F8", build(with(v, "resourceName", utf8(0xF8, 0x88, 0x80, 0x80, 0x80))));
        grammar.put("utf8 lone continuation", build(with(v, "resourceName", utf8(0x80))));
        grammar.put("utf8 truncated 2-byte", build(with(v, "resourceName", utf8(0xC3))));
        grammar.put("utf8 truncated 4-byte", build(with(v, "resourceName", utf8(0xF0, 0x9F, 0x8C))));
        grammar.put("utf8 FE byte", build(with(v, "resourceName", utf8(0xFE))));

        Map<String, byte[]> predicates = new java.util.LinkedHashMap<>();
        predicates.put("zero concurrency", build(with(v, "concurrency", 0L)));
        predicates.put("negative concurrency", build(with(v, "concurrency", -1L)));
        predicates.put("Integer.MIN concurrency", build(with(v, "concurrency", (long) Integer.MIN_VALUE)));
        predicates.put("missing operation", build(with(v, "productOperation", 11L)));
        predicates.put("zero duration", build(with(v, "duration", 0L)));
        predicates.put("Long.MIN duration", build(with(v, "duration", Long.MIN_VALUE)));
        predicates.put("empty eligible", build(with(v, "eligible", new long[0])));
        predicates.put("dangling resource", build(with(v, "eligible", new long[] {1, 2})));
        predicates.put("no resources, dangling", build(with(v, "resourceCount", 0L)));
        predicates.put("empty steps", build(with(v, "stepCount", 0L)));

        List<String> ledger = new ArrayList<>();
        for (Map.Entry<String, byte[]> c : grammar.entrySet()) {
            Campaign1Oracle.Decoded oracle = Campaign1Oracle.decode(c.getValue());
            assertFalse(oracle.accepted(), c.getKey());
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> FactoryModelArtifact.decode(c.getValue()), c.getKey());
            assertEquals("invalid factory-model canonical artifact", e.getMessage(), c.getKey());
            assertThrows(IllegalArgumentException.class, () -> FactoryModelArtifact.fingerprint(c.getValue()), c.getKey());
            assertThrows(IllegalArgumentException.class, () -> FactoryModelArtifact.verifier().fingerprint(c.getValue()), c.getKey());
            ledger.add("grammar\t" + c.getKey() + "\toracle=" + oracle.refusal() + "\tproduct=IllegalArgumentException cause="
                    + (e.getCause() == null ? "none" : e.getCause().getClass().getSimpleName() + ": " + e.getCause().getMessage()));
        }
        for (Map.Entry<String, byte[]> c : predicates.entrySet()) {
            Campaign1Oracle.Decoded oracle = Campaign1Oracle.decode(c.getValue());
            assertFalse(oracle.accepted(), c.getKey());
            assertTrue(oracle.refusal().startsWith("publication predicates"), c.getKey() + " " + oracle.refusal());
            FactoryModelValidationException e = assertThrows(FactoryModelValidationException.class,
                    () -> FactoryModelArtifact.decode(c.getValue()), c.getKey());
            ledger.add("predicate\t" + c.getKey() + "\toracle=" + oracle.refusal() + "\tproduct=" + e.result().errors());
        }
        writeResult("codec-targeted-ledger.txt", ledger);
    }

    // -------------------------------------------------------------- seeded structured generator

    static final long SEED = 0xC1A55EEDL;

    static long pick(Random random, long[] pool) {
        return pool[random.nextInt(pool.length)];
    }

    static String name(Random random) {
        String[] pool = {"", "a", "\u0000", "é", "日", "🌀", "􏿿", ":", "|", "\n", "￿", "é"};
        StringBuilder s = new StringBuilder();
        for (int i = random.nextInt(4); i > 0; i--) {
            s.append(pool[random.nextInt(pool.length)]);
        }
        return s.toString();
    }

    static FactoryModel generate(Random random) {
        long[] idPool = {Long.MIN_VALUE, -2, -1, 0, 1, 2, 3, 40, Long.MAX_VALUE};
        int resourceCount = 1 + random.nextInt(4);
        Set<Long> used = new LinkedHashSet<>();
        List<ConfiguredResource> resources = new ArrayList<>();
        while (resources.size() < resourceCount) {
            long id = pick(random, idPool);
            if (used.add(id)) {
                Double[] capacities = {null, 0.0, -0.0, 1.5, Double.NaN, Double.POSITIVE_INFINITY, -2.25, Double.MIN_VALUE};
                resources.add(new ConfiguredResource(new MachineId(id), name(random),
                        new int[] {1, 2, Integer.MAX_VALUE}[random.nextInt(3)], capacities[random.nextInt(capacities.length)],
                        pick(random, new long[] {Long.MIN_VALUE, -1, 0, 5, Long.MAX_VALUE})));
            }
        }
        List<Long> resourceIds = new ArrayList<>(used);
        List<OperationDefinition> operations = new ArrayList<>();
        Set<Long> opIds = new LinkedHashSet<>();
        for (int o = random.nextInt(3); o >= 0; o--) {
            long id = pick(random, idPool);
            if (!opIds.add(id)) {
                continue;
            }
            List<OperationStepDefinition> steps = new ArrayList<>();
            for (int s = random.nextInt(3); s >= 0; s--) {
                List<Long> shuffled = new ArrayList<>(resourceIds);
                java.util.Collections.shuffle(shuffled, random);
                LinkedHashSet<MachineId> eligible = new LinkedHashSet<>();
                shuffled.subList(0, 1 + random.nextInt(shuffled.size())).forEach(x -> eligible.add(new MachineId(x)));
                steps.add(new OperationStepDefinition(pick(random, idPool), name(random), eligible,
                        pick(random, new long[] {1, 2, 1000, Long.MAX_VALUE})));
            }
            operations.add(new OperationDefinition(id, name(random), steps));
        }
        List<Long> opList = new ArrayList<>(opIds);
        List<ProductDefinition> products = new ArrayList<>();
        Set<Long> productIds = new LinkedHashSet<>();
        for (int p = random.nextInt(3); p > 0; p--) {
            long id = pick(random, idPool);
            if (productIds.add(id)) {
                products.add(new ProductDefinition(new ProductId(id), name(random), opList.get(random.nextInt(opList.size()))));
            }
        }
        return new FactoryModel(resources, operations, products);
    }

    @Test
    void seededGeneratedModelsAgreeWithTheIndependentEncoderAndRoundTrip() throws Exception {
        Random random = new Random(SEED);
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        int models = 3000;
        for (int i = 0; i < models; i++) {
            FactoryModel model = generate(random);
            assertEquals(List.of(), Campaign1Oracle.violations(Campaign1Oracle.neutral(model)), "generator produced invalid model " + i);
            FactoryModelVersion version = FactoryModelPublisher.publish(model);
            byte[] bytes = FactoryModelArtifact.encode(version);
            assertArrayEquals(Campaign1Oracle.encode(Campaign1Oracle.neutral(model)), bytes, "model " + i);
            assertEquals("factory-model:sha256:" + HexFormat.of().formatHex(sha.digest(bytes)), version.fingerprint().toString());
            FactoryModelVersion decoded = FactoryModelArtifact.decode(bytes);
            assertEquals(model, decoded.model(), "model " + i);
            Campaign1Oracle.Decoded oracle = Campaign1Oracle.decode(bytes);
            assertTrue(oracle.accepted(), "model " + i + " " + oracle.refusal());
            assertEquals(oracle.model(), Campaign1Oracle.setNormalized(Campaign1Oracle.neutral(model)));
        }
    }

    // -------------------------------------------------------------- support and definition binding

    @Test
    void supportIsExactlyTheCurrentNamespaceAndAlgorithm() {
        String digest = "0".repeat(64);
        assertTrue(FactoryModelArtifact.supports(new ModelFingerprint("factory-model", "sha256", digest)));
        SemanticArtifactVerifier verifier = FactoryModelArtifact.verifier();
        for (String[] unsupported : new String[][] {{"factory-model-v1", "sha256"}, {"Factory-model", "sha256"},
            {"factory-model", "sha512"}, {"factory-model", "SHA256"}, {"other", "sha256"}, {"factory-model ", "sha256"}}) {
            ModelFingerprint fingerprint = new ModelFingerprint(unsupported[0], unsupported[1],
                    "sha256".equals(unsupported[1]) ? digest : "x");
            assertFalse(FactoryModelArtifact.supports(fingerprint), String.join(":", unsupported));
            assertFalse(verifier.supports(fingerprint), String.join(":", unsupported));
        }
        assertThrows(NullPointerException.class, () -> FactoryModelArtifact.supports(null));
    }

    @Test
    void definitionBindingIsTheDocumentedDigestOverTheListedClassBytes() throws Exception {
        List<Class<?>> classes = FactoryModelCanonicalForm.definitionBindingClasses();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        for (Class<?> type : classes) {
            byte[] name = type.getName().getBytes(StandardCharsets.UTF_8);
            String resource = "/" + type.getName().replace('.', '/') + ".class";
            byte[] code;
            try (var in = Campaign1CodecExperimentTest.class.getResourceAsStream(resource)) {
                code = in.readAllBytes();
            }
            digest.update(new Stream().i64(name.length).bytes());
            digest.update(name);
            digest.update(new Stream().i64(code.length).bytes());
            digest.update(code);
        }
        String expected = "factory-model-definition-build:sha256:" + HexFormat.of().formatHex(digest.digest());
        assertEquals(expected, FactoryModelArtifact.verifier().definitionBinding());
        assertEquals(expected, FactoryModelCanonicalForm.definitionBinding());
    }

    /** Child-JVM entry point for the binding coverage probe: exercises only the verification path. */
    public static void main(String[] args) {
        SemanticArtifactVerifier verifier = FactoryModelArtifact.verifier();
        byte[] bytes = hex(EXTREMES_HEX);
        ModelFingerprint fingerprint = verifier.fingerprint(bytes);
        if (!verifier.supports(fingerprint)) {
            throw new AssertionError("unsupported");
        }
        try {
            verifier.fingerprint(build(with(valid(), "concurrency", 0L)));
            throw new AssertionError("accepted invalid");
        } catch (FactoryModelValidationException expected) {
            // refusal path exercised too
        }
        try {
            verifier.fingerprint(new byte[] {1});
            throw new AssertionError("accepted malformed");
        } catch (IllegalArgumentException expected) {
            // grammar refusal path exercised too
        }
        System.out.println("PROBE-DONE " + fingerprint);
    }

    @Test
    void everyArcogineClassExecutedByVerificationIsBoundOrJustified() throws Exception {
        String javaBin = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        Process process = new ProcessBuilder(javaBin, "-Xlog:class+load=info:stdout", "-cp",
                System.getProperty("factory.test.classpath"), Campaign1CodecExperimentTest.class.getName())
                .redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (!process.waitFor(60, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new AssertionError("child JVM timed out");
        }
        assertEquals(0, process.exitValue(), output);
        assertTrue(output.contains("PROBE-DONE " + EXTREMES_FP), output);
        Set<String> loaded = new TreeSet<>();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\] (com\\.arcogine\\.[\\w.$]+) source:").matcher(output);
        while (m.find()) {
            loaded.add(m.group(1));
        }
        Set<String> bound = new TreeSet<>();
        FactoryModelCanonicalForm.definitionBindingClasses().forEach(c -> bound.add(c.getName()));
        Set<String> loadedNotBound = new TreeSet<>(loaded);
        loadedNotBound.removeAll(bound);
        Set<String> boundNotLoaded = new TreeSet<>(bound);
        boundNotLoaded.removeAll(loaded);
        writeResult("definition-binding-coverage.txt", List.of("loaded=" + loaded, "bound=" + bound,
                "loadedNotBound=" + loadedNotBound, "boundNotLoaded=" + boundNotLoaded));
        // Justified: the probe itself (and its nested records), the binding computation (not decode/verify
        // behaviour), and the Governance verifier interface (no behaviour of its own).
        Set<String> unjustified = new TreeSet<>(loadedNotBound);
        unjustified.removeIf(n -> n.startsWith(Campaign1CodecExperimentTest.class.getName())
                || n.startsWith(Campaign1Oracle.class.getName())
                || n.startsWith(Campaign1PublicationExperimentTest.class.getName())
                || n.equals("com.arcogine.factory.model.FactoryModelCanonicalForm$DefinitionBinding")
                || n.equals("com.arcogine.governance.SemanticArtifactVerifier"));
        assertEquals(Set.of(), unjustified);
        assertEquals(Set.of(), boundNotLoaded);
    }
}
