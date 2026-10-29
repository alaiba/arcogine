package com.arcogine.factory.model;

import com.arcogine.types.MachineId;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Transient Campaign 1 oracle (not a merge candidate): an independent implementation of
 * docs/architecture/factory-model.md sections 2, 4 and 5 over a neutral model representation.
 * Integers are framed by explicit shifts, UTF-8 is encoded and decoded from code points by hand,
 * and nothing calls FactoryModelCanonicalForm, FactoryModelValidator or a JDK charset codec.
 */
final class Campaign1Oracle {

    static final byte[] PREFIX = {0x61, 0x72, 0x63, 0x6f, 0x67, 0x69, 0x6e, 0x65, 0x2e, 0x66, 0x61, 0x63,
        0x74, 0x6f, 0x72, 0x79, 0x2d, 0x6d, 0x6f, 0x64, 0x65, 0x6c, 0x00};
    static final long CANONICAL_NAN = 0x7ff8000000000000L;

    private Campaign1Oracle() {}

    record NResource(long id, String name, long concurrency, Long capacityBits, long setupTime) {}

    /** {@code eligible} keeps the order and multiplicity found in bytes (or the authored set). */
    record NStep(long stepId, String name, long duration, List<Long> eligible) {}

    record NOperation(long id, String name, List<NStep> steps) {}

    record NProduct(long id, String name, long operationId) {}

    record NModel(List<NResource> resources, List<NOperation> operations, List<NProduct> products) {}

    /** Outcome of the oracle decoder: exactly one of model / refusal. */
    record Decoded(NModel model, String refusal) {
        boolean accepted() {
            return model != null;
        }
    }

    static NModel neutral(FactoryModel model) {
        List<NResource> resources = new ArrayList<>();
        for (ConfiguredResource r : model.resources()) {
            Double capacity = r.capacityLiters();
            Long bits = capacity == null ? null : Double.isNaN(capacity) ? CANONICAL_NAN : Double.doubleToRawLongBits(capacity);
            resources.add(new NResource(r.id().value(), r.name(), r.concurrency(), bits, r.setupTime()));
        }
        List<NOperation> operations = new ArrayList<>();
        for (OperationDefinition o : model.operations()) {
            List<NStep> steps = new ArrayList<>();
            for (OperationStepDefinition s : o.steps()) {
                List<Long> eligible = new ArrayList<>();
                for (MachineId id : s.eligibleResources()) {
                    eligible.add(id.value());
                }
                steps.add(new NStep(s.stepId(), s.name(), s.duration(), eligible));
            }
            operations.add(new NOperation(o.id(), o.name(), steps));
        }
        List<NProduct> products = new ArrayList<>();
        for (ProductDefinition p : model.products()) {
            products.add(new NProduct(p.id().value(), p.name(), p.operationId()));
        }
        return new NModel(resources, operations, products);
    }

    /** Neutral model with each eligible list replaced by its ascending distinct set (set semantics). */
    static NModel setNormalized(NModel model) {
        List<NOperation> operations = new ArrayList<>();
        for (NOperation o : model.operations()) {
            List<NStep> steps = new ArrayList<>();
            for (NStep s : o.steps()) {
                steps.add(new NStep(s.stepId(), s.name(), s.duration(), new ArrayList<>(new TreeSet<>(s.eligible()))));
            }
            operations.add(new NOperation(o.id(), o.name(), steps));
        }
        return new NModel(model.resources(), operations, model.products());
    }

    // ---------------------------------------------------------------- encoding (section 4)

    static byte[] encode(NModel model) {
        Out out = new Out();
        out.raw(PREFIX);
        out.u64(model.resources().size());
        for (NResource r : model.resources()) {
            out.i64(r.id());
            out.text(r.name());
            out.i64(r.concurrency());
            if (r.capacityBits() == null) {
                out.b(0);
            } else {
                out.b(1);
                long bits = r.capacityBits();
                boolean nan = (bits & 0x7ff0000000000000L) == 0x7ff0000000000000L && (bits & 0x000fffffffffffffL) != 0;
                out.i64(nan ? CANONICAL_NAN : bits);
            }
            out.i64(r.setupTime());
        }
        out.u64(model.operations().size());
        for (NOperation o : model.operations()) {
            out.i64(o.id());
            out.text(o.name());
            out.u64(o.steps().size());
            for (NStep s : o.steps()) {
                out.i64(s.stepId());
                out.text(s.name());
                out.i64(s.duration());
                TreeSet<Long> sorted = new TreeSet<>(s.eligible());
                out.u64(sorted.size());
                for (long id : sorted) {
                    out.i64(id);
                }
            }
        }
        out.u64(model.products().size());
        for (NProduct p : model.products()) {
            out.i64(p.id());
            out.text(p.name());
            out.i64(p.operationId());
        }
        return out.bytes();
    }

    static String fingerprint(byte[] bytes) {
        try {
            return "factory-model:sha256:" + HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final class Out {
        private byte[] data = new byte[256];
        private int size;

        void b(int value) {
            if (size == data.length) {
                data = java.util.Arrays.copyOf(data, size * 2);
            }
            data[size++] = (byte) value;
        }

        void raw(byte[] bytes) {
            for (byte value : bytes) {
                b(value);
            }
        }

        void i64(long value) {
            for (int shift = 56; shift >= 0; shift -= 8) {
                b((int) (value >>> shift) & 0xff);
            }
        }

        void u64(long value) {
            i64(value);
        }

        void text(String value) {
            List<Integer> utf8 = new ArrayList<>();
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                int cp;
                if (c >= 0xD800 && c <= 0xDBFF && i + 1 < value.length()
                        && value.charAt(i + 1) >= 0xDC00 && value.charAt(i + 1) <= 0xDFFF) {
                    cp = 0x10000 + ((c - 0xD800) << 10) + (value.charAt(i + 1) - 0xDC00);
                    i++;
                } else if (c >= 0xD800 && c <= 0xDFFF) {
                    throw new IllegalArgumentException("oracle: unpaired surrogate");
                } else {
                    cp = c;
                }
                if (cp < 0x80) {
                    utf8.add(cp);
                } else if (cp < 0x800) {
                    utf8.add(0xC0 | (cp >> 6));
                    utf8.add(0x80 | (cp & 0x3F));
                } else if (cp < 0x10000) {
                    utf8.add(0xE0 | (cp >> 12));
                    utf8.add(0x80 | ((cp >> 6) & 0x3F));
                    utf8.add(0x80 | (cp & 0x3F));
                } else {
                    utf8.add(0xF0 | (cp >> 18));
                    utf8.add(0x80 | ((cp >> 12) & 0x3F));
                    utf8.add(0x80 | ((cp >> 6) & 0x3F));
                    utf8.add(0x80 | (cp & 0x3F));
                }
            }
            u64(utf8.size());
            for (int value8 : utf8) {
                b(value8);
            }
        }

        byte[] bytes() {
            return java.util.Arrays.copyOf(data, size);
        }
    }

    // ---------------------------------------------------------------- decoding (section 5)

    private static final class Refused extends Exception {
        private static final long serialVersionUID = 1L;

        Refused(String reason) {
            super(reason, null, false, false);
        }
    }

    private static final class In {
        private final byte[] data;
        private int position;

        In(byte[] data) {
            this.data = data;
        }

        int u8() throws Refused {
            if (position >= data.length) {
                throw new Refused("truncated");
            }
            return data[position++] & 0xff;
        }

        long i64() throws Refused {
            long value = 0;
            for (int i = 0; i < 8; i++) {
                value = (value << 8) | u8();
            }
            return value;
        }

        int count() throws Refused {
            long value = i64();
            if (value < 0 || value > Integer.MAX_VALUE) {
                throw new Refused("count out of range");
            }
            return (int) value;
        }

        String text() throws Refused {
            int length = count();
            if ((long) position + length > data.length) {
                throw new Refused("text overruns stream");
            }
            int end = position + length;
            StringBuilder s = new StringBuilder();
            while (position < end) {
                int lead = data[position++] & 0xff;
                int extra;
                int cp;
                int min;
                if (lead < 0x80) {
                    s.append((char) lead);
                    continue;
                } else if (lead >= 0xC2 && lead <= 0xDF) {
                    extra = 1;
                    cp = lead & 0x1F;
                    min = 0x80;
                } else if (lead >= 0xE0 && lead <= 0xEF) {
                    extra = 2;
                    cp = lead & 0x0F;
                    min = 0x800;
                } else if (lead >= 0xF0 && lead <= 0xF4) {
                    extra = 3;
                    cp = lead & 0x07;
                    min = 0x10000;
                } else {
                    throw new Refused("invalid UTF-8 lead byte");
                }
                for (int k = 0; k < extra; k++) {
                    if (position >= end) {
                        throw new Refused("truncated UTF-8 sequence");
                    }
                    int cont = data[position++] & 0xff;
                    if ((cont & 0xC0) != 0x80) {
                        throw new Refused("invalid UTF-8 continuation");
                    }
                    cp = (cp << 6) | (cont & 0x3F);
                }
                if (cp < min || cp > 0x10FFFF || (cp >= 0xD800 && cp <= 0xDFFF)) {
                    throw new Refused("non-scalar or overlong UTF-8");
                }
                s.appendCodePoint(cp);
            }
            return s.toString();
        }
    }

    static Decoded decode(byte[] bytes) {
        try {
            In in = new In(bytes);
            for (byte expected : PREFIX) {
                if (in.u8() != (expected & 0xff)) {
                    throw new Refused("prefix");
                }
            }
            List<NResource> resources = new ArrayList<>();
            for (int i = 0, n = in.count(); i < n; i++) {
                long id = in.i64();
                String name = in.text();
                long concurrency = in.i64();
                if (concurrency < Integer.MIN_VALUE || concurrency > Integer.MAX_VALUE) {
                    // factory-model.md 6 item 5: an out-of-range concurrency is a decode rejection
                    throw new Refused("concurrency out of range");
                }
                int marker = in.u8();
                Long bits;
                if (marker == 0) {
                    bits = null;
                } else if (marker == 1) {
                    bits = in.i64();
                } else {
                    throw new Refused("optional f64 marker");
                }
                resources.add(new NResource(id, name, concurrency, bits, in.i64()));
            }
            List<NOperation> operations = new ArrayList<>();
            for (int i = 0, n = in.count(); i < n; i++) {
                long id = in.i64();
                String name = in.text();
                List<NStep> steps = new ArrayList<>();
                for (int j = 0, m = in.count(); j < m; j++) {
                    long stepId = in.i64();
                    String stepName = in.text();
                    long duration = in.i64();
                    List<Long> eligible = new ArrayList<>();
                    for (int k = 0, e = in.count(); k < e; k++) {
                        eligible.add(in.i64());
                    }
                    steps.add(new NStep(stepId, stepName, duration, eligible));
                }
                operations.add(new NOperation(id, name, steps));
            }
            List<NProduct> products = new ArrayList<>();
            for (int i = 0, n = in.count(); i < n; i++) {
                products.add(new NProduct(in.i64(), in.text(), in.i64()));
            }
            if (in.position != bytes.length) {
                throw new Refused("trailing bytes");
            }
            NModel model = new NModel(resources, operations, products);
            if (!java.util.Arrays.equals(bytes, encode(model))) {
                throw new Refused("not canonical");
            }
            List<String> violations = violations(model);
            if (!violations.isEmpty()) {
                throw new Refused("publication predicates: " + violations);
            }
            return new Decoded(setNormalized(model), null);
        } catch (Refused e) {
            return new Decoded(null, e.getMessage());
        }
    }

    // ---------------------------------------------------------------- predicates (section 2)

    static boolean wellFormed(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 0xD800 && c <= 0xDBFF) {
                if (i + 1 < s.length() && s.charAt(i + 1) >= 0xDC00 && s.charAt(i + 1) <= 0xDFFF) {
                    i++;
                    continue;
                }
                return false;
            }
            if (c >= 0xDC00 && c <= 0xDFFF) {
                return false;
            }
        }
        return true;
    }

    /**
     * Violation kinds with entity coordinates. Granularity choices the specification leaves open are
     * explicit assumptions: one violation per offending record occurrence, one per extra occurrence
     * of a duplicate identifier, one per dangling eligible-resource member, one per ill-formed name.
     */
    static List<String> violations(NModel model) {
        List<String> out = new ArrayList<>();
        Set<Long> resourceIds = new HashSet<>();
        for (NResource r : model.resources()) {
            if (!wellFormed(r.name())) {
                out.add("NAME resource " + r.id());
            }
            if (!resourceIds.add(r.id())) {
                out.add("DUPLICATE resource " + r.id());
            }
            if (r.concurrency() <= 0) {
                out.add("CONCURRENCY resource " + r.id());
            }
            if (r.concurrency() > Integer.MAX_VALUE) {
                out.add("CONCURRENCY_RANGE resource " + r.id());
            }
        }
        Set<Long> operationIds = new HashSet<>();
        for (NOperation o : model.operations()) {
            operationIds.add(o.id());
        }
        Set<Long> seenOperations = new HashSet<>();
        for (NOperation o : model.operations()) {
            if (!wellFormed(o.name())) {
                out.add("NAME operation " + o.id());
            }
            if (!seenOperations.add(o.id())) {
                out.add("DUPLICATE operation " + o.id());
            }
            if (o.steps().isEmpty()) {
                out.add("EMPTY_STEPS operation " + o.id());
            }
            for (NStep s : o.steps()) {
                if (!wellFormed(s.name())) {
                    out.add("NAME step " + o.id() + "/" + s.stepId());
                }
                if (s.duration() <= 0) {
                    out.add("DURATION step " + o.id() + "/" + s.stepId());
                }
                if (s.eligible().isEmpty()) {
                    out.add("EMPTY_ELIGIBLE step " + o.id() + "/" + s.stepId());
                }
                for (long id : new TreeSet<>(s.eligible())) {
                    if (!resourceIds.contains(id)) {
                        out.add("DANGLING_RESOURCE step " + o.id() + "/" + s.stepId() + " -> " + id);
                    }
                }
            }
        }
        Set<Long> productIds = new HashSet<>();
        for (NProduct p : model.products()) {
            if (!wellFormed(p.name())) {
                out.add("NAME product " + p.id());
            }
            if (!productIds.add(p.id())) {
                out.add("DUPLICATE product " + p.id());
            }
            if (!operationIds.contains(p.operationId())) {
                out.add("DANGLING_OPERATION product " + p.id() + " -> " + p.operationId());
            }
        }
        return out;
    }

    /** Multiset view, so unrelated error kinds are compared without imposing a universal order. */
    static Map<String, Integer> multiset(List<String> values) {
        Map<String, Integer> counts = new TreeMap<>();
        for (String value : values) {
            counts.merge(value, 1, Integer::sum);
        }
        return counts;
    }
}
