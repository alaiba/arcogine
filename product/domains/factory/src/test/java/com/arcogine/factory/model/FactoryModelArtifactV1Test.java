package com.arcogine.factory.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.types.MachineId;
import com.arcogine.types.ModelFingerprint;
import com.arcogine.types.ProductId;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FactoryModelArtifactV1Test {

    @Test
    void canonicalArtifactRoundTripsExactSemanticStateAndFingerprint() {
        FactoryModelVersion original = publishedModel();

        byte[] canonicalBytes = FactoryModelArtifactV1.encode(original);
        FactoryModelVersion reconstructed = FactoryModelArtifactV1.decode(canonicalBytes);

        assertEquals(original.model(), reconstructed.model());
        assertEquals(original.fingerprint(), reconstructed.fingerprint());
        assertEquals(original.fingerprint(), FactoryModelArtifactV1.fingerprint(canonicalBytes));
        assertArrayEquals(canonicalBytes, FactoryModelArtifactV1.encode(reconstructed));
        assertTrue(FactoryModelArtifactV1.supports(original.fingerprint()));
        assertFalse(FactoryModelArtifactV1.supports(new ModelFingerprint(
                "other-model", "v1", "sha256", original.fingerprint().digest())));
    }

    @Test
    void malformedOrNoncanonicalArtifactsAreRejected() {
        byte[] canonicalBytes = FactoryModelArtifactV1.encode(publishedModel());

        byte[] wrongPrefix = canonicalBytes.clone();
        wrongPrefix[0] ^= 1;
        assertThrows(IllegalArgumentException.class, () -> FactoryModelArtifactV1.decode(wrongPrefix));

        byte[] trailing = Arrays.copyOf(canonicalBytes, canonicalBytes.length + 1);
        assertThrows(IllegalArgumentException.class, () -> FactoryModelArtifactV1.decode(trailing));

        byte[] truncated = Arrays.copyOf(canonicalBytes, canonicalBytes.length - 1);
        assertThrows(IllegalArgumentException.class, () -> FactoryModelArtifactV1.decode(truncated));

        byte[] invalidUtf8 = canonicalBytes.clone();
        int mill = indexOf(invalidUtf8, "Mill".getBytes(StandardCharsets.UTF_8));
        invalidUtf8[mill] = (byte) 0xc3;
        invalidUtf8[mill + 1] = 0x28;
        assertThrows(IllegalArgumentException.class, () -> FactoryModelArtifactV1.decode(invalidUtf8));

        byte[] invalidOptionalMarker = canonicalBytes.clone();
        byte[] capacity = ByteBuffer.allocate(Long.BYTES).putLong(Double.doubleToRawLongBits(125.5)).array();
        int capacityOffset = indexOf(invalidOptionalMarker, capacity);
        invalidOptionalMarker[capacityOffset - 1] = 2;
        assertThrows(
                IllegalArgumentException.class,
                () -> FactoryModelArtifactV1.decode(invalidOptionalMarker));

        assertThrows(NullPointerException.class, () -> FactoryModelArtifactV1.decode(null));
    }

    @Test
    void decoderRejectsValuesThatCannotBeTheCanonicalFactoryShape() {
        byte[] canonicalBytes = FactoryModelArtifactV1.encode(publishedModel());
        byte[] mill = "Mill".getBytes(StandardCharsets.UTF_8);
        int nameOffset = indexOf(canonicalBytes, mill);
        int concurrencyOffset = nameOffset + mill.length;

        byte[] invalidConcurrency = canonicalBytes.clone();
        ByteBuffer.wrap(invalidConcurrency, concurrencyOffset, Long.BYTES).putLong(Long.MAX_VALUE);
        assertThrows(
                IllegalArgumentException.class,
                () -> FactoryModelArtifactV1.decode(invalidConcurrency));

        byte[] negativeResourceCount = canonicalBytes.clone();
        int prefixLength = "arcogine.factory-model.v1\0".getBytes(StandardCharsets.US_ASCII).length;
        ByteBuffer.wrap(negativeResourceCount, prefixLength, Long.BYTES).putLong(-1L);
        assertThrows(
                IllegalArgumentException.class,
                () -> FactoryModelArtifactV1.decode(negativeResourceCount));
    }

    private static FactoryModelVersion publishedModel() {
        ResourceDefinition mill =
                new ResourceDefinition(new MachineId(1), "Mill", 2, 125.5, 3);
        ResourceDefinition packer =
                new ResourceDefinition(new MachineId(2), "Packer", 1, null, 0);
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
