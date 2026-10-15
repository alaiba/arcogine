package com.arcogine.factory.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.validation.FactoryModelValidationException;
import com.arcogine.governance.SemanticArtifactVerifier;
import com.arcogine.types.MachineId;
import com.arcogine.types.ModelFingerprint;
import com.arcogine.types.ProductId;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FactoryModelArtifactTest {

    private static final int PREFIX_LENGTH =
            "arcogine.factory-model\0".getBytes(StandardCharsets.US_ASCII).length;
    private static final int RESOURCE_COUNT_OFFSET = PREFIX_LENGTH;

    @Test
    void canonicalArtifactRoundTripsExactSemanticStateAndFingerprint() {
        FactoryModelVersion original = publishedModel();
        byte[] canonicalBytes = FactoryModelArtifact.encode(original);
        FactoryModelVersion reconstructed = FactoryModelArtifact.decode(canonicalBytes);

        assertEquals(original.model(), reconstructed.model());
        assertEquals(original.fingerprint(), reconstructed.fingerprint());
        assertEquals(original.fingerprint(), FactoryModelArtifact.fingerprint(canonicalBytes));
        assertArrayEquals(canonicalBytes, FactoryModelArtifact.encode(reconstructed));
        assertTrue(FactoryModelArtifact.supports(original.fingerprint()));
    }

    @Test
    void currentFingerprintShapeIsSupportedAndDiscardedPrefixesAreNeverReinterpreted() {
        String digest = publishedModel().fingerprint().digest();

        assertTrue(FactoryModelArtifact.supports(new ModelFingerprint("factory-model", "sha256", digest)));
        assertFalse(FactoryModelArtifact.supports(new ModelFingerprint("other-model", "sha256", digest)));
        assertFalse(FactoryModelArtifact.supports(new ModelFingerprint("factory-model", "sha512", digest)));

        byte[] current = FactoryModelArtifact.encode(publishedModel());
        byte[] discardedPrefix = "arcogine.factory-model.v1\0".getBytes(StandardCharsets.US_ASCII);
        byte[] discardedArtifact = new byte[discardedPrefix.length + current.length - PREFIX_LENGTH];
        System.arraycopy(discardedPrefix, 0, discardedArtifact, 0, discardedPrefix.length);
        System.arraycopy(current, PREFIX_LENGTH, discardedArtifact, discardedPrefix.length, current.length - PREFIX_LENGTH);
        assertThrows(IllegalArgumentException.class, () -> FactoryModelArtifact.decode(discardedArtifact));
    }

    @Test
    void verifierChecksCurrentArtifactsAndNamesTheExactDefinitionBuild() {
        FactoryModelVersion version = publishedModel();
        byte[] canonicalBytes = FactoryModelArtifact.encode(version);
        SemanticArtifactVerifier verifier = FactoryModelArtifact.verifier();

        assertTrue(verifier.supports(version.fingerprint()));
        assertFalse(verifier.supports(new ModelFingerprint(
                "other-model", "sha256", version.fingerprint().digest())));
        assertEquals(version.fingerprint(), verifier.fingerprint(canonicalBytes));

        // The binding is derived from the definition's compiled classes: stable within one build,
        // and deliberately separate from the content fingerprint, which is not an exact definition
        // reference.
        String binding = verifier.definitionBinding();
        assertTrue(binding.matches("factory-model-definition-build:sha256:[0-9a-f]{64}"), binding);
        assertEquals(binding, FactoryModelArtifact.verifier().definitionBinding());
        assertFalse(binding.contains(version.fingerprint().digest()));
    }

    @Test
    void malformedOrNoncanonicalArtifactsAreRejected() {
        byte[] canonicalBytes = FactoryModelArtifact.encode(publishedModel());

        byte[] wrongPrefix = canonicalBytes.clone();
        wrongPrefix[0] ^= 1;
        assertThrows(IllegalArgumentException.class, () -> FactoryModelArtifact.decode(wrongPrefix));

        byte[] trailing = Arrays.copyOf(canonicalBytes, canonicalBytes.length + 1);
        assertThrows(IllegalArgumentException.class, () -> FactoryModelArtifact.decode(trailing));

        byte[] truncated = Arrays.copyOf(canonicalBytes, canonicalBytes.length - 1);
        assertThrows(IllegalArgumentException.class, () -> FactoryModelArtifact.decode(truncated));

        byte[] invalidUtf8 = canonicalBytes.clone();
        int mill = indexOf(invalidUtf8, "Mill".getBytes(StandardCharsets.UTF_8));
        invalidUtf8[mill] = (byte) 0xc3;
        invalidUtf8[mill + 1] = 0x28;
        assertThrows(IllegalArgumentException.class, () -> FactoryModelArtifact.decode(invalidUtf8));

        byte[] invalidOptionalMarker = canonicalBytes.clone();
        byte[] capacity = ByteBuffer.allocate(Long.BYTES).putLong(Double.doubleToRawLongBits(125.5)).array();
        int capacityOffset = indexOf(invalidOptionalMarker, capacity);
        invalidOptionalMarker[capacityOffset - 1] = 2;
        assertThrows(IllegalArgumentException.class, () -> FactoryModelArtifact.decode(invalidOptionalMarker));

        // Eligible resources are canonical only when sorted ascending; the reversed order still
        // parses but does not re-encode to itself.
        byte[] unsortedEligible = canonicalBytes.clone();
        byte[] sortedPair = ByteBuffer.allocate(2 * Long.BYTES).putLong(1).putLong(2).array();
        int eligibleOffset = indexOf(unsortedEligible, sortedPair);
        ByteBuffer.wrap(unsortedEligible, eligibleOffset, 2 * Long.BYTES).putLong(2).putLong(1);
        assertThrows(IllegalArgumentException.class, () -> FactoryModelArtifact.decode(unsortedEligible));

        assertThrows(NullPointerException.class, () -> FactoryModelArtifact.decode(null));
    }

    @Test
    void grammarValidContentThatCouldNeverBePublishedIsRejectedOnDecode() {
        byte[] canonicalBytes = FactoryModelArtifact.encode(publishedModel());

        // A zero concurrency is grammar-valid but violates a publication predicate.
        byte[] zeroConcurrency = canonicalBytes.clone();
        byte[] mill = "Mill".getBytes(StandardCharsets.UTF_8);
        ByteBuffer.wrap(zeroConcurrency, indexOf(zeroConcurrency, mill) + mill.length, Long.BYTES).putLong(0);
        assertThrows(FactoryModelValidationException.class, () -> FactoryModelArtifact.decode(zeroConcurrency));

        // So is a product that references an operation the model does not define.
        byte[] danglingOperation = canonicalBytes.clone();
        ByteBuffer.wrap(danglingOperation, danglingOperation.length - Long.BYTES, Long.BYTES).putLong(999);
        assertThrows(FactoryModelValidationException.class, () -> FactoryModelArtifact.decode(danglingOperation));
    }

    @Test
    void decoderRejectsValuesThatCannotBeTheCanonicalFactoryShape() {
        byte[] canonicalBytes = FactoryModelArtifact.encode(publishedModel());
        byte[] mill = "Mill".getBytes(StandardCharsets.UTF_8);
        int nameOffset = indexOf(canonicalBytes, mill);
        int concurrencyOffset = nameOffset + mill.length;

        byte[] invalidConcurrency = canonicalBytes.clone();
        ByteBuffer.wrap(invalidConcurrency, concurrencyOffset, Long.BYTES).putLong(Long.MAX_VALUE);
        assertThrows(IllegalArgumentException.class, () -> FactoryModelArtifact.decode(invalidConcurrency));

        byte[] tooSmallConcurrency = canonicalBytes.clone();
        ByteBuffer.wrap(tooSmallConcurrency, concurrencyOffset, Long.BYTES).putLong(Long.MIN_VALUE);
        assertThrows(IllegalArgumentException.class, () -> FactoryModelArtifact.decode(tooSmallConcurrency));

        byte[] negativeResourceCount = canonicalBytes.clone();
        ByteBuffer.wrap(negativeResourceCount, RESOURCE_COUNT_OFFSET, Long.BYTES).putLong(-1L);
        assertThrows(IllegalArgumentException.class, () -> FactoryModelArtifact.decode(negativeResourceCount));

        byte[] oversizedResourceCount = canonicalBytes.clone();
        ByteBuffer.wrap(oversizedResourceCount, RESOURCE_COUNT_OFFSET, Long.BYTES)
                .putLong((long) Integer.MAX_VALUE + 1);
        assertThrows(IllegalArgumentException.class, () -> FactoryModelArtifact.decode(oversizedResourceCount));

        byte[] truncatedName = canonicalBytes.clone();
        ByteBuffer.wrap(truncatedName, nameOffset - Long.BYTES, Long.BYTES).putLong(canonicalBytes.length);
        assertThrows(IllegalArgumentException.class, () -> FactoryModelArtifact.decode(truncatedName));
    }

    private static FactoryModelVersion publishedModel() {
        ConfiguredResource mill =
                new ConfiguredResource(new MachineId(1), "Mill", 2, 125.5, 3);
        ConfiguredResource packer =
                new ConfiguredResource(new MachineId(2), "Packer", 1, null, 0);
        OperationStepDefinition machine = new OperationStepDefinition(
                1, "Machine", Set.of(new MachineId(2), new MachineId(1)), 5);
        OperationStepDefinition pack =
                new OperationStepDefinition(2, "Pack", Set.of(new MachineId(2)), 2);
        OperationDefinition operation =
                new OperationDefinition(100, "Routing", List.of(machine, pack));
        ProductDefinition product =
                new ProductDefinition(new ProductId(10), "Widget", operation.id());
        return FactoryModelPublisher.publish(new FactoryModel(
                List.of(mill, packer), List.of(operation), List.of(product)));
    }

    private static int indexOf(byte[] haystack, byte[] needle) {
        for (int index = 0; index <= haystack.length - needle.length; index++) {
            boolean match = true;
            for (int offset = 0; offset < needle.length; offset++) {
                if (haystack[index + offset] != needle[offset]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return index;
            }
        }
        throw new AssertionError("needle not found in canonical artifact");
    }
}
