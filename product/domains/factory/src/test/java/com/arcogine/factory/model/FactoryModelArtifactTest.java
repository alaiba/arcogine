package com.arcogine.factory.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.spatial.FactoryFloor;
import com.arcogine.factory.model.spatial.ResourceFootprint;
import com.arcogine.factory.model.spatial.ResourceLayout;
import com.arcogine.factory.model.spatial.ResourcePlacement;
import com.arcogine.factory.model.spatial.SpatialRecord;
import com.arcogine.factory.model.validation.FactoryModelValidationException;
import com.arcogine.governance.SemanticArtifactVerifier;
import com.arcogine.types.MachineId;
import com.arcogine.types.ModelFingerprint;
import com.arcogine.types.ProductId;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FactoryModelArtifactTest {

    private static final int PREFIX_LENGTH =
            "arcogine.factory-model.wip\0".getBytes(StandardCharsets.US_ASCII).length;
    private static final int PRESENCE_OFFSET = PREFIX_LENGTH;
    private static final int HEADER_LENGTH = 4 * Long.BYTES;

    @Test
    void canonicalArtifactRoundTripsExactSemanticStateAndFingerprint() {
        for (FactoryModelVersion original : List.of(publishedModel(Optional.empty()), publishedModel(Optional.of(spatial(0))))) {
            byte[] canonicalBytes = FactoryModelArtifact.encode(original);
            FactoryModelVersion reconstructed = FactoryModelArtifact.decode(canonicalBytes);

            assertEquals(original.model(), reconstructed.model());
            assertEquals(original.fingerprint(), reconstructed.fingerprint());
            assertEquals(original.fingerprint(), FactoryModelArtifact.fingerprint(canonicalBytes));
            assertArrayEquals(canonicalBytes, FactoryModelArtifact.encode(reconstructed));
            assertTrue(FactoryModelArtifact.supports(original.fingerprint()));
        }
    }

    @Test
    void onlyTheCurrentDefinitionIsSupportedAndDiscardedPoliciesAreNeverReinterpreted() {
        String digest = publishedModel(Optional.empty()).fingerprint().digest();

        assertTrue(FactoryModelArtifact.supports(new ModelFingerprint("factory-model", "wip", "sha256", digest)));
        assertFalse(FactoryModelArtifact.supports(new ModelFingerprint("factory-model", "v1", "sha256", digest)));
        assertFalse(FactoryModelArtifact.supports(new ModelFingerprint("factory-model", "v2", "sha256", digest)));
        assertFalse(FactoryModelArtifact.supports(new ModelFingerprint("other-model", "wip", "sha256", digest)));

        byte[] current = FactoryModelArtifact.encode(publishedModel(Optional.empty()));
        byte[] discardedPrefix = "arcogine.factory-model.v1\0".getBytes(StandardCharsets.US_ASCII);
        byte[] discardedArtifact = new byte[discardedPrefix.length + current.length - PREFIX_LENGTH - 1];
        System.arraycopy(discardedPrefix, 0, discardedArtifact, 0, discardedPrefix.length);
        System.arraycopy(current, PREFIX_LENGTH + 1, discardedArtifact, discardedPrefix.length, current.length - PREFIX_LENGTH - 1);
        assertThrows(IllegalArgumentException.class, () -> FactoryModelArtifact.decode(discardedArtifact));
    }

    @Test
    void verifierChecksCurrentArtifactsAndNamesTheExactDefinitionBuild() {
        FactoryModelVersion version = publishedModel(Optional.of(spatial(0)));
        byte[] canonicalBytes = FactoryModelArtifact.encode(version);
        SemanticArtifactVerifier verifier = FactoryModelArtifact.verifier();

        assertTrue(verifier.supports(version.fingerprint()));
        assertFalse(verifier.supports(new ModelFingerprint(
                "factory-model", "v1", "sha256", version.fingerprint().digest())));
        assertEquals(version.fingerprint(), verifier.fingerprint(canonicalBytes));

        // The binding is derived from the definition's compiled classes: stable within one build,
        // and deliberately not the public marker, which does not change between revisions.
        String binding = verifier.definitionBinding();
        assertTrue(binding.matches("factory-model-definition-build:sha256:[0-9a-f]{64}"), binding);
        assertEquals(binding, FactoryModelArtifact.verifier().definitionBinding());
        assertFalse(binding.contains(version.fingerprint().digest()));
    }

    @Test
    void malformedOrNoncanonicalArtifactsAreRejected() {
        byte[] canonicalBytes = FactoryModelArtifact.encode(publishedModel(Optional.empty()));

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
    void spatialPresenceMarkerFramingIsStrict() {
        byte[] absent = FactoryModelArtifact.encode(publishedModel(Optional.empty()));
        byte[] present = FactoryModelArtifact.encode(publishedModel(Optional.of(spatial(0))));
        assertEquals(0, absent[PRESENCE_OFFSET]);
        assertEquals(1, present[PRESENCE_OFFSET]);

        byte[] invalidMarker = absent.clone();
        invalidMarker[PRESENCE_OFFSET] = 2;
        assertThrows(IllegalArgumentException.class, () -> FactoryModelArtifact.decode(invalidMarker));

        byte[] truncatedInHeader = Arrays.copyOf(present, PRESENCE_OFFSET + 1 + HEADER_LENGTH - 3);
        assertThrows(IllegalArgumentException.class, () -> FactoryModelArtifact.decode(truncatedInHeader));

        byte[] millName = "Mill".getBytes(StandardCharsets.UTF_8);
        int resourceSuffix = indexOf(present, millName) + millName.length + Long.BYTES + 1 + Long.BYTES + Long.BYTES;
        byte[] truncatedInResourceSuffix = Arrays.copyOf(present, resourceSuffix + Long.BYTES + 2);
        assertThrows(IllegalArgumentException.class, () -> FactoryModelArtifact.decode(truncatedInResourceSuffix));

        byte[] header = Arrays.copyOfRange(present, PRESENCE_OFFSET + 1, PRESENCE_OFFSET + 1 + HEADER_LENGTH);
        byte[] absentWithAppendedHeader = Arrays.copyOf(absent, absent.length + header.length);
        System.arraycopy(header, 0, absentWithAppendedHeader, absent.length, header.length);
        assertThrows(IllegalArgumentException.class, () -> FactoryModelArtifact.decode(absentWithAppendedHeader));
    }

    @Test
    void grammarValidContentThatCouldNeverBePublishedIsRejectedOnDecode() {
        byte[] present = FactoryModelArtifact.encode(publishedModel(Optional.of(spatial(0))));

        // Move the second footprint onto the first: well-formed bytes, but overlapping footprints.
        byte[] overlapping = present.clone();
        byte[] packer = "Packer".getBytes(StandardCharsets.UTF_8);
        int packerPosition = indexOf(overlapping, packer) + packer.length + Long.BYTES + 1 + Long.BYTES;
        ByteBuffer.wrap(overlapping, packerPosition, 2 * Long.BYTES).putLong(0).putLong(0);
        assertThrows(FactoryModelValidationException.class, () -> FactoryModelArtifact.decode(overlapping));

        // A zero floor width is grammar-valid but violates a publication predicate.
        byte[] zeroFloor = present.clone();
        ByteBuffer.wrap(zeroFloor, PRESENCE_OFFSET + 1, Long.BYTES).putLong(0);
        assertThrows(FactoryModelValidationException.class, () -> FactoryModelArtifact.decode(zeroFloor));
    }

    @Test
    void decoderRejectsValuesThatCannotBeTheCanonicalFactoryShape() {
        byte[] canonicalBytes = FactoryModelArtifact.encode(publishedModel(Optional.empty()));
        byte[] mill = "Mill".getBytes(StandardCharsets.UTF_8);
        int nameOffset = indexOf(canonicalBytes, mill);
        int concurrencyOffset = nameOffset + mill.length;

        byte[] invalidConcurrency = canonicalBytes.clone();
        ByteBuffer.wrap(invalidConcurrency, concurrencyOffset, Long.BYTES).putLong(Long.MAX_VALUE);
        assertThrows(IllegalArgumentException.class, () -> FactoryModelArtifact.decode(invalidConcurrency));

        byte[] negativeResourceCount = canonicalBytes.clone();
        ByteBuffer.wrap(negativeResourceCount, PRESENCE_OFFSET + 1, Long.BYTES).putLong(-1L);
        assertThrows(IllegalArgumentException.class, () -> FactoryModelArtifact.decode(negativeResourceCount));
    }

    private static SpatialRecord spatial(long ticksPerCell) {
        return new SpatialRecord(
                new FactoryFloor(8, 4),
                ticksPerCell,
                1,
                List.of(
                        new ResourceLayout(new MachineId(1), new ResourcePlacement(0, 0), new ResourceFootprint(2, 2)),
                        new ResourceLayout(new MachineId(2), new ResourcePlacement(5, 1), new ResourceFootprint(2, 2))));
    }

    private static FactoryModelVersion publishedModel(Optional<SpatialRecord> spatial) {
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
                List.of(mill, packer), List.of(operation), List.of(product), spatial));
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
