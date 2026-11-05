package com.arcogine.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Transient Campaign 1 slice 1 experiment (not a merge candidate). Oracles are BigInteger
 * arithmetic and character-level UUID grammar written from the SimTime / ControlledRevisionId
 * contracts, never from the implementation under test.
 */
class Campaign1ValueTimeExperimentTest {

    private static final BigInteger MAX = BigInteger.valueOf(Long.MAX_VALUE);

    private static List<Long> timeBoundaries() {
        List<Long> values = new ArrayList<>();
        for (long v = 0; v <= 4; v++) {
            values.add(v);
        }
        long[] high = {Long.MAX_VALUE / 2, Long.MAX_VALUE / 2 + 1, Long.MAX_VALUE - 4,
            Long.MAX_VALUE - 3, Long.MAX_VALUE - 2, Long.MAX_VALUE - 1, Long.MAX_VALUE};
        for (long v : high) {
            values.add(v);
        }
        return values;
    }

    private static List<Long> deltas() {
        List<Long> deltas = new ArrayList<>();
        long[] base = {Long.MIN_VALUE, Long.MIN_VALUE + 1, -Long.MAX_VALUE, -(Long.MAX_VALUE - 1),
            -(Long.MAX_VALUE / 2) - 1, -(Long.MAX_VALUE / 2), -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5,
            Long.MAX_VALUE / 2, Long.MAX_VALUE / 2 + 1, Long.MAX_VALUE - 1, Long.MAX_VALUE};
        for (long d : base) {
            deltas.add(d);
        }
        // value-relative deltas: exactly -value, -value-1, MAX-value, MAX-value+1
        for (long v : timeBoundaries()) {
            deltas.add(-v);
            if (v != Long.MAX_VALUE) {
                deltas.add(-v - 1);
            }
            deltas.add(Long.MAX_VALUE - v);
            if (v != 0) {
                deltas.add(Long.MAX_VALUE - v + 1);
            }
        }
        return deltas;
    }

    @Test
    void constructionAcceptsExactlyNonNegativeTicks() {
        long[] refused = {Long.MIN_VALUE, Long.MIN_VALUE + 1, -2, -1};
        for (long value : refused) {
            IllegalArgumentException e =
                    assertThrows(IllegalArgumentException.class, () -> new SimTime(value));
            assertEquals("simulated time must be non-negative, got " + value, e.getMessage());
            assertThrows(IllegalArgumentException.class, () -> SimTime.of(value));
        }
        for (long value : timeBoundaries()) {
            assertEquals(value, new SimTime(value).ticks());
            assertEquals(value, SimTime.of(value).value());
        }
        assertEquals(new SimTime(0), SimTime.ZERO);
    }

    @Test
    void plusAndCanAddMatchTheExactIntegerSumOverTheBoundaryCrossProduct() {
        int cases = 0;
        for (long value : timeBoundaries()) {
            SimTime time = new SimTime(value);
            for (long delta : deltas()) {
                cases++;
                BigInteger exact = BigInteger.valueOf(value).add(BigInteger.valueOf(delta));
                boolean representable = exact.signum() >= 0 && exact.compareTo(MAX) <= 0;
                assertEquals(representable, time.canAdd(delta), () -> value + " canAdd " + delta);
                if (representable) {
                    assertEquals(exact.longValueExact(), time.plus(delta).ticks(), () -> value + "+" + delta);
                } else {
                    ArithmeticException e = assertThrows(ArithmeticException.class, () -> time.plus(delta));
                    assertEquals("simulated time " + value + " + " + delta + " is outside [0, Long.MAX_VALUE]",
                            e.getMessage());
                }
            }
        }
        assertTrue(cases > 400, "boundary cross product unexpectedly small: " + cases);
    }

    @Test
    void minusIsExactElapsedOrZeroOverTheBoundaryCrossProduct() {
        for (long a : timeBoundaries()) {
            for (long b : timeBoundaries()) {
                BigInteger exact = BigInteger.valueOf(a).subtract(BigInteger.valueOf(b));
                long expected = exact.signum() < 0 ? 0 : exact.longValueExact();
                assertEquals(expected, new SimTime(a).minus(new SimTime(b)), () -> a + "-" + b);
            }
        }
    }

    @Test
    void comparisonEqualityAndHashAgreeWithIntegerOrder() {
        for (long a : timeBoundaries()) {
            for (long b : timeBoundaries()) {
                int expected = Long.signum(BigInteger.valueOf(a).compareTo(BigInteger.valueOf(b)));
                SimTime x = new SimTime(a);
                SimTime y = new SimTime(b);
                assertEquals(expected, Integer.signum(x.compareTo(y)));
                assertEquals(-expected, Integer.signum(y.compareTo(x)));
                assertEquals(a == b, x.equals(y));
                if (a == b) {
                    assertEquals(x.hashCode(), y.hashCode());
                }
            }
            assertEquals("t=" + a, new SimTime(a).toString());
        }
    }

    @Test
    void numericTypedIdentifiersCompareSignedValuesAndStayDomainSeparated() {
        long[] values = {Long.MIN_VALUE, Long.MIN_VALUE + 1, -1, 0, 1, Long.MAX_VALUE - 1, Long.MAX_VALUE};
        for (long a : values) {
            for (long b : values) {
                int expected = Integer.signum(Long.compare(a, b));
                assertEquals(expected, Integer.signum(new MachineId(a).compareTo(new MachineId(b))));
                assertEquals(expected, Integer.signum(new JobId(a).compareTo(new JobId(b))));
                assertEquals(a == b, new MachineId(a).equals(new MachineId(b)));
                assertEquals(a == b, new OrderId(a).equals(new OrderId(b)));
                assertEquals(a == b, new ProductId(a).equals(new ProductId(b)));
            }
            Object[] domains = {new MachineId(a), new JobId(a), new OrderId(a), new ProductId(a)};
            for (int i = 0; i < domains.length; i++) {
                for (int j = 0; j < domains.length; j++) {
                    assertEquals(i == j, domains[i].equals(domains[j]), domains[i] + " vs " + domains[j]);
                }
            }
        }
    }

    /** Independent RFC 9562 reading: version is hex digit 14, variant 10xx is hex digit 19 in 8..b. */
    private static boolean oracleAccepts(String spelling) {
        if (spelling.length() != 36) {
            return false;
        }
        for (int i = 0; i < 36; i++) {
            char c = spelling.charAt(i);
            boolean dash = i == 8 || i == 13 || i == 18 || i == 23;
            if (dash ? c != '-' : !((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f'))) {
                return false;
            }
        }
        char variant = spelling.charAt(19);
        return spelling.charAt(14) == '4' && (variant == '8' || variant == '9' || variant == 'a' || variant == 'b');
    }

    @Test
    void controlledRevisionIdAcceptsExactlyCanonicalLowercaseRfcVersion4() {
        String hexDigits = "0123456789abcdef";
        int accepted = 0;
        for (int version = 0; version < 16; version++) {
            for (int variant = 0; variant < 16; variant++) {
                String spelling = "3f2504e0-4f89-" + hexDigits.charAt(version) + "1d3-"
                        + hexDigits.charAt(variant) + "a0c-0305e82c3301";
                boolean expected = oracleAccepts(spelling);
                try {
                    ControlledRevisionId id = ControlledRevisionId.parse(spelling);
                    assertTrue(expected, () -> "accepted " + spelling);
                    assertEquals(spelling, id.toString());
                    assertEquals(id, ControlledRevisionId.parse(id.toString()));
                    assertEquals(id, new ControlledRevisionId(UUID.fromString(spelling)));
                    accepted++;
                } catch (IllegalArgumentException e) {
                    assertFalse(expected, () -> "refused " + spelling + ": " + e);
                    // Non-v4/variant spellings are canonical, so the refusal must be the v4 rule.
                    assertEquals("controlled revision ID must be an RFC UUIDv4", e.getMessage());
                }
            }
        }
        assertEquals(4, accepted);
    }

    @Test
    void controlledRevisionIdRefusesEveryNonCanonicalSpellingOfAnAcceptableValue() {
        String canonical = "3f2504e0-4f89-41d3-9a0c-0305e82c3301";
        assertEquals(canonical, ControlledRevisionId.parse(canonical).toString());
        List<String> spellings = List.of(
                canonical.toUpperCase(),
                "3F2504e0-4f89-41d3-9a0c-0305e82c3301",
                "3f2504e0-4f89-41d3-9A0c-0305e82c3301",
                "3f2504e0-4f89-41d3-9a0c-0305e82c330",
                "3f2504e0-4f89-41d3-9a0c-0305e82c33011",
                "3f2504e04f8941d39a0c0305e82c3301",
                "{3f2504e0-4f89-41d3-9a0c-0305e82c3301}",
                " 3f2504e0-4f89-41d3-9a0c-0305e82c3301",
                "3f2504e0-4f89-41d3-9a0c-0305e82c3301 ",
                "urn:uuid:3f2504e0-4f89-41d3-9a0c-0305e82c3301",
                "3f2504e0-4f89-41d3-9a0c-00000305e82c3301",
                "3f2504e0-4f89-41d3-9a0c-305e82c3301",
                "0003f2504e0-4f89-41d3-9a0c-0305e82c3301",
                "0-0-4000-8000-0",
                "3f2504e0-4f89-41d3-9a0c+0305e82c3301",
                "3f2504e0_4f89_41d3_9a0c_0305e82c3301",
                "",
                "3f2504e0-4f89-41d3-9a0c-0305e82c33١١");
        for (String spelling : spellings) {
            assertFalse(oracleAccepts(spelling), spelling);
            IllegalArgumentException e = assertThrows(
                    IllegalArgumentException.class, () -> ControlledRevisionId.parse(spelling), spelling);
            assertNotEquals(null, e.getMessage(), spelling);
        }
        assertThrows(NullPointerException.class, () -> ControlledRevisionId.parse(null));
        assertThrows(NullPointerException.class, () -> new ControlledRevisionId(null));
    }

    @Test
    void controlledRevisionIdConstructorAppliesTheSameVersionAndVariantRule() {
        for (int version = 0; version < 16; version++) {
            for (int variantBits = 0; variantBits < 8; variantBits++) {
                long msb = 0x3f2504e04f8941d3L & ~0xF000L | ((long) version << 12);
                long lsb = 0x9a0c0305e82c3301L & ~(0x7L << 61) & ~(1L << 63) | ((long) variantBits << 61);
                UUID uuid = new UUID(msb, lsb);
                boolean expected = version == 4 && (variantBits >>> 1) == 0b10;
                if (expected) {
                    assertEquals(uuid, new ControlledRevisionId(uuid).value());
                } else {
                    assertThrows(IllegalArgumentException.class, () -> new ControlledRevisionId(uuid),
                            () -> "uuid " + uuid);
                }
            }
        }
        for (int i = 0; i < 1_000; i++) {
            ControlledRevisionId generated = ControlledRevisionId.generate();
            assertTrue(oracleAccepts(generated.toString()), generated.toString());
        }
    }

    @Test
    void modelFingerprintGrammarAndExactEquality() {
        String digest = "0123456789abcdef".repeat(4);
        ModelFingerprint fingerprint = new ModelFingerprint("factory-model", "sha256", digest);
        assertEquals("factory-model:sha256:" + digest, fingerprint.toString());
        assertEquals(fingerprint, new ModelFingerprint("factory-model", "sha256", digest));
        assertNotEquals(fingerprint, new ModelFingerprint("other", "sha256", digest));
        assertNotEquals(fingerprint, new ModelFingerprint("factory-model", "sha512", digest));

        List<String> badDigests = List.of(digest.toUpperCase(), "A" + digest.substring(1), digest.substring(1),
                digest + "0", digest.substring(1) + "g", digest.substring(1) + " ", " " + digest.substring(1),
                digest.substring(2) + "١٢");
        for (String bad : badDigests) {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> new ModelFingerprint("factory-model", "sha256", bad), bad);
            assertEquals("sha256 digest must be 64 lowercase hexadecimal characters", e.getMessage());
        }
        for (String blank : List.of("", " ", "\t", "\n")) {
            assertEquals("namespace must not be blank", assertThrows(IllegalArgumentException.class,
                    () -> new ModelFingerprint(blank, "sha256", digest)).getMessage());
            assertEquals("algorithm must not be blank", assertThrows(IllegalArgumentException.class,
                    () -> new ModelFingerprint("factory-model", blank, digest)).getMessage());
            assertEquals("digest must not be blank", assertThrows(IllegalArgumentException.class,
                    () -> new ModelFingerprint("factory-model", "sha256", blank)).getMessage());
        }
        assertThrows(NullPointerException.class, () -> new ModelFingerprint(null, "sha256", digest));
        assertThrows(NullPointerException.class, () -> new ModelFingerprint("factory-model", null, digest));
        assertThrows(NullPointerException.class, () -> new ModelFingerprint("factory-model", "sha256", null));
    }

    @Test
    void runIdIsAnOpaqueNonNullCorrelationValue() {
        UUID uuid = UUID.randomUUID();
        assertEquals(new RunId(uuid), new RunId(uuid));
        assertNotEquals(RunId.create(), RunId.create());
        assertThrows(NullPointerException.class, () -> new RunId(null));
    }

    @Test
    void structuredSimErrorsRetainTheirFacts() {
        SimError.EventOrderingViolation unrepresentable =
                new SimError.EventOrderingViolation(new SimTime(Long.MAX_VALUE), 1);
        assertEquals(new SimTime(Long.MAX_VALUE), unrepresentable.expectedMin());
        assertTrue(unrepresentable.actual().isEmpty());
        assertEquals(1, unrepresentable.duration().getAsLong());
        SimError.EventOrderingViolation earlier =
                new SimError.EventOrderingViolation(new SimTime(5), new SimTime(4));
        assertEquals(new SimTime(4), earlier.actual().orElseThrow());
        assertTrue(earlier.duration().isEmpty());
        assertEquals("eventSequence", new SimError.SequenceExhausted("eventSequence").counter());
    }
}
