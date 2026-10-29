package com.arcogine.factory.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arcogine.factory.model.validation.FactoryModelValidator;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Independent challenge fixtures; workspace only, never a maintained test proposal. */
class IndependentFactoryChallengeTest {
    @Test
    void literalVectorFromGrammarHasExactBytesAndDigest() throws Exception {
        // Each chunk is a literal field from factory-model.md section 4, in stream order.
        byte[] bytes = HexFormat.of().parseHex(
                "6172636f67696e652e666163746f72792d6d6f64656c00"
                + "0000000000000001" // resource count
                + "8000000000000000" // resource ID MIN
                + "0000000000000006c3a9f09f8c80" // name: e-acute, cyclone
                + "000000007fffffff" // concurrency MAX int as I64
                + "018000000000000000" // present -0.0
                + "8000000000000000" // setup MIN
                + "0000000000000001ffffffffffffffff" // one operation, ID -1
                + "00000000000000040065cc81" // NUL, e, combining acute
                + "0000000000000001" // one step
                + "7fffffffffffffff" // step ID MAX
                + "0000000000000004f48fbfbf" // name U+10FFFF
                + "7fffffffffffffff" // duration MAX
                + "00000000000000018000000000000000" // one eligible ID MIN
                + "00000000000000010000000000000000" // one product, ID zero
                + "0000000000000000ffffffffffffffff"); // empty name, operation -1
        FactoryModel model = new FactoryModel(
                List.of(new ConfiguredResource(new MachineId(Long.MIN_VALUE), "é🌀", Integer.MAX_VALUE, -0.0, Long.MIN_VALUE)),
                List.of(new OperationDefinition(-1, "\u0000e\u0301", List.of(
                        new OperationStepDefinition(Long.MAX_VALUE, "\uDBFF\uDFFF", Set.of(new MachineId(Long.MIN_VALUE)), Long.MAX_VALUE)))),
                List.of(new ProductDefinition(new ProductId(0), "", -1)));
        FactoryModelVersion version = FactoryModelPublisher.publish(model);
        assertArrayEquals(bytes, FactoryModelArtifact.encode(version));
        assertEquals(model, FactoryModelArtifact.decode(bytes).model());
        assertEquals(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)), version.fingerprint().digest());
    }

    @Test
    void everySingleByteReplacementAndInsertionInEmptyGrammarIsRefused() {
        byte[] bytes = HexFormat.of().parseHex("6172636f67696e652e666163746f72792d6d6f64656c00" + "00".repeat(24));
        int substitutions = 0;
        int insertions = 0;
        // Empty grammar has fixed length/prefix and all three counts are exactly zero.
        // Any one changed byte is invalid, regardless of product or producer-oracle behavior.
        for (int i = 0; i < bytes.length; i++) {
            for (int value = 0; value < 256; value++) {
                if (value == (bytes[i] & 255)) continue;
                byte[] changed = bytes.clone();
                changed[i] = (byte) value;
                assertEquals(IllegalArgumentException.class,
                        assertThrows(IllegalArgumentException.class, () -> FactoryModelArtifact.decode(changed)).getClass());
                substitutions++;
            }
        }
        for (int i = 0; i <= bytes.length; i++) {
            for (int value = 0; value < 256; value++) {
                byte[] inserted = new byte[48];
                System.arraycopy(bytes, 0, inserted, 0, i);
                inserted[i] = (byte) value;
                System.arraycopy(bytes, i, inserted, i + 1, bytes.length - i);
                assertEquals(IllegalArgumentException.class,
                        assertThrows(IllegalArgumentException.class, () -> FactoryModelArtifact.decode(inserted)).getClass());
                insertions++;
            }
        }
        assertEquals(11985, substitutions);
        assertEquals(12288, insertions);
    }

    @Test
    void fullDiagnosticWitnessPreservesEveryFieldAndMessage() {
        FactoryModel model = new FactoryModel(
                List.of(new ConfiguredResource(new MachineId(2), "r", 0, null, 0)),
                List.of(new OperationDefinition(10, "\uD800", List.of(new OperationStepDefinition(7, "\uDC00",
                        new LinkedHashSet<>(List.of(new MachineId(Long.MAX_VALUE), new MachineId(0), new MachineId(-1))), 0)))),
                List.of(new ProductDefinition(new ProductId(3), "\uD800", 999)));
        // Current observed diagnostic vocabulary/order; only the signed eligible-ID order is a
        // normative ordering assertion. This fixture does not create a universal diagnostic sort.
        assertEquals(List.of(
                "resources[Machine(2)].concurrency: must be > 0",
                "operations[10].name: must contain only valid Unicode scalar values",
                "operations[10].steps[7].name: must contain only valid Unicode scalar values",
                "operations[10].steps[7].duration: must be > 0",
                "operations[10].steps[7].eligibleResources: references nonexistent resource id: Machine(-1)",
                "operations[10].steps[7].eligibleResources: references nonexistent resource id: Machine(0)",
                "operations[10].steps[7].eligibleResources: references nonexistent resource id: Machine(9223372036854775807)",
                "products[Product(3)].name: must contain only valid Unicode scalar values",
                "products[Product(3)].operationId: references nonexistent operation id: 999"),
                FactoryModelValidator.validate(model).errors().stream().map(Object::toString).toList());
    }
}
