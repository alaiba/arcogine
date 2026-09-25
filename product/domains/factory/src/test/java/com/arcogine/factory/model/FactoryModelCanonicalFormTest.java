package com.arcogine.factory.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arcogine.factory.model.spatial.FactoryFloor;
import com.arcogine.factory.model.spatial.ResourceFootprint;
import com.arcogine.factory.model.spatial.ResourceLayout;
import com.arcogine.factory.model.spatial.ResourcePlacement;
import com.arcogine.factory.model.spatial.SpatialRecord;
import com.arcogine.factory.model.validation.FactoryModelValidationException;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Golden vectors and sensitivity cases for the current work-in-progress Factory canonical form
 * (docs/architecture/factory-model.md).
 *
 * <p>The literal bytes and digests below were derived from an independent implementation of the
 * specified grammar, not from this codec. They pin the current definition only: a deliberate change
 * to the work-in-progress grammar updates them together with the specification.
 */
class FactoryModelCanonicalFormTest {

    private static final String PRESENT_REPRESENTATIVE_BYTES =
            "6172636f67696e652e666163746f72792d6d6f64656c2e7769700001000000000000000a0000000000000006000000000000000200000000000000030000000000000002000000000000000100000000000000044d696c6c000000000000000201405f60000000000000000000000000030000000000000000000000000000000000000000000000020000000000000003000000000000000200000000000000065061636b657200000000000000010000000000000000000000000000000004000000000000000100000000000000030000000000000002000000000000000100000000000000640000000000000007526f7574696e670000000000000002000000000000000100000000000000074d616368696e650000000000000005000000000000000200000000000000010000000000000002000000000000000200000000000000045061636b0000000000000002000000000000000100000000000000020000000000000001000000000000000a00000000000000065769646765740000000000000064";

    private static FactoryModel representativeModel() {
        return new FactoryModel(
                List.of(
                        new ConfiguredResource(new MachineId(-2), "M|ill", 3, -0.0, -7),
                        new ConfiguredResource(new MachineId(5), "Second", 1, null, 0)),
                List.of(new OperationDefinition(
                        42,
                        "Op:🚀",
                        List.of(new OperationStepDefinition(
                                9,
                                "Step;é",
                                new LinkedHashSet<>(List.of(new MachineId(5), new MachineId(-2))),
                                11)))),
                List.of(new ProductDefinition(new ProductId(8), "P\u0000|𐀀", 42)));
    }

    private static FactoryModel spatialModel(SpatialRecord spatial) {
        return new FactoryModel(
                List.of(
                        new ConfiguredResource(new MachineId(1), "Mill", 2, 125.5, 3),
                        new ConfiguredResource(new MachineId(2), "Packer", 1, null, 0)),
                List.of(new OperationDefinition(100, "Routing", List.of(
                        new OperationStepDefinition(1, "Machine", Set.of(new MachineId(2), new MachineId(1)), 5),
                        new OperationStepDefinition(2, "Pack", Set.of(new MachineId(2)), 2)))),
                List.of(new ProductDefinition(new ProductId(10), "Widget", 100)),
                Optional.of(spatial));
    }

    private static SpatialRecord spatial(
            long floorWidth, long floorHeight, long ticksPerCell, long handlingTicks,
            long x1, long y1, long w1, long h1, long x2, long y2, long w2, long h2) {
        return new SpatialRecord(
                new FactoryFloor(floorWidth, floorHeight),
                ticksPerCell,
                handlingTicks,
                List.of(
                        new ResourceLayout(new MachineId(1), new ResourcePlacement(x1, y1), new ResourceFootprint(w1, h1)),
                        new ResourceLayout(new MachineId(2), new ResourcePlacement(x2, y2), new ResourceFootprint(w2, h2))));
    }

    private static SpatialRecord representativeSpatial() {
        return spatial(10, 6, 2, 3, 0, 0, 2, 3, 4, 1, 3, 2);
    }

    private static FactoryModel minimalModel(Optional<SpatialRecord> spatial) {
        return new FactoryModel(
                List.of(new ConfiguredResource(new MachineId(1), "Mill", 1, null, 0)),
                List.of(new OperationDefinition(
                        100, "Routing", List.of(new OperationStepDefinition(1, "Machine", Set.of(new MachineId(1)), 5)))),
                List.of(new ProductDefinition(new ProductId(10), "Widget", 100)),
                spatial);
    }

    private static SpatialRecord minimalZeroSpatial() {
        return new SpatialRecord(
                new FactoryFloor(1, 1),
                0,
                0,
                List.of(new ResourceLayout(new MachineId(1), new ResourcePlacement(0, 0), new ResourceFootprint(1, 1))));
    }

    // ---- Golden vectors ---------------------------------------------------------------------------

    @Test
    void definitionPrefixIsPinned() {
        byte[] bytes = FactoryModelCanonicalForm.canonicalBytes(representativeModel());
        byte[] expectedPrefix = HexFormat.of().parseHex(
                "6172636f67696e652e666163746f72792d6d6f64656c2e77697000");

        assertArrayEquals(expectedPrefix, Arrays.copyOf(bytes, expectedPrefix.length));
        assertArrayEquals("arcogine.factory-model.wip\0".getBytes(StandardCharsets.US_ASCII), expectedPrefix);
    }

    @Test
    void spatialAbsentRepresentativeVectorPinsCanonicalBytesAndFingerprint() {
        FactoryModelVersion version = FactoryModelPublisher.publish(representativeModel());

        assertArrayEquals(
                HexFormat.of().parseHex(
                        "6172636f67696e652e666163746f72792d6d6f64656c2e77697000000000000000000002fffffffffffffffe00000000000000054d7c696c6c0000000000000003018000000000000000fffffffffffffff9000000000000000500000000000000065365636f6e6400000000000000010000000000000000000000000000000001000000000000002a00000000000000074f703af09f9a80000000000000000100000000000000090000000000000007537465703bc3a9000000000000000b0000000000000002fffffffffffffffe000000000000000500000000000000010000000000000008000000000000000750007cf0908080000000000000002a"),
                FactoryModelCanonicalForm.canonicalBytes(representativeModel()));
        assertEquals(
                "factory-model:wip:sha256:986f52c73e220325fe91f89df425cad31377206bb2f4602dc2f7b7f1f7b4bd0c",
                version.fingerprint().toString());
    }

    @Test
    void spatialPresentRepresentativeVectorPinsCanonicalBytesAndFingerprint() {
        FactoryModel model = spatialModel(representativeSpatial());

        assertArrayEquals(
                HexFormat.of().parseHex(PRESENT_REPRESENTATIVE_BYTES),
                FactoryModelCanonicalForm.canonicalBytes(model));
        assertEquals(
                "factory-model:wip:sha256:a75620400ec8c56dd68de1dcf8e5f0086b9519d57f2f5ad2aee8b08a3e223ba5",
                FactoryModelPublisher.publish(model).fingerprint().toString());
    }

    @Test
    void presentLegalZeroIsAuthoredContentDistinctFromAbsence() {
        FactoryModelVersion absent = FactoryModelPublisher.publish(minimalModel(Optional.empty()));
        FactoryModelVersion presentZero = FactoryModelPublisher.publish(minimalModel(Optional.of(minimalZeroSpatial())));

        assertEquals(
                "factory-model:wip:sha256:105b382bac88a71491342c376f3d5923d09b334b2bc7c731c6744a91c07f728d",
                absent.fingerprint().toString());
        assertEquals(
                "factory-model:wip:sha256:1927aebef92f5955d62a4c830beea0c21751af778b913e5713b657632dc8ebfb",
                presentZero.fingerprint().toString());
        assertNotEquals(absent.fingerprint(), presentZero.fingerprint());
    }

    @Test
    void oneAggregateFingerprintCoversTheSpatialRecord() {
        FactoryModelVersion withSpatial = FactoryModelPublisher.publish(spatialModel(representativeSpatial()));
        FactoryModel production = withSpatial.model();
        FactoryModelVersion productionOnly = FactoryModelPublisher.publish(new FactoryModel(
                production.resources(), production.operations(), production.products()));

        assertNotEquals(productionOnly.fingerprint(), withSpatial.fingerprint());
        assertEquals(withSpatial.fingerprint(), FactoryModelPublisher.publish(spatialModel(representativeSpatial())).fingerprint());
    }

    // ---- Production-record coverage -------------------------------------------------------------

    @Test
    void setIterationOrderDoesNotChangeFingerprint() {
        FactoryModel first = representativeModel();
        FactoryModel second = new FactoryModel(
                first.resources(),
                List.of(new OperationDefinition(
                        42,
                        "Op:🚀",
                        List.of(new OperationStepDefinition(
                                9, "Step;é", Set.of(new MachineId(-2), new MachineId(5)), 11)))),
                first.products());

        assertEquals(
                FactoryModelPublisher.publish(first).fingerprint(),
                FactoryModelPublisher.publish(second).fingerprint());
    }

    @Test
    void namesIdsStepsAndCapacityParticipateInFingerprint() {
        FactoryModelVersion original = FactoryModelPublisher.publish(representativeModel());

        assertDifferent(original, modelWithResourceName("Other"));
        assertDifferent(original, modelWithOperationName("Other"));
        assertDifferent(original, modelWithStepName("Other"));
        assertDifferent(original, modelWithProductName("Other"));
        assertDifferent(original, modelWithResourceId(7));
        assertDifferent(original, modelWithCapacity(1.0));
    }

    @Test
    void listOrderAndFloatingPointPayloadArePartOfTheContract() {
        FactoryModel model = new FactoryModel(
                List.of(new ConfiguredResource(new MachineId(1), "A", 1, 0.0, 0),
                        new ConfiguredResource(new MachineId(2), "B", 1, null, 0)),
                List.of(new OperationDefinition(1, "One", List.of(
                                new OperationStepDefinition(1, "First", Set.of(new MachineId(1)), 1),
                                new OperationStepDefinition(2, "Second", Set.of(new MachineId(1)), 2))),
                        new OperationDefinition(2, "Two", List.of(
                                new OperationStepDefinition(1, "Only", Set.of(new MachineId(2)), 1)))),
                List.of(new ProductDefinition(new ProductId(1), "One", 1),
                        new ProductDefinition(new ProductId(2), "Two", 2)));

        FactoryModelVersion original = FactoryModelPublisher.publish(model);
        FactoryModelVersion resourceReordered = FactoryModelPublisher.publish(new FactoryModel(
                List.of(model.resources().get(1), model.resources().get(0)), model.operations(), model.products()));
        FactoryModelVersion operationReordered = FactoryModelPublisher.publish(new FactoryModel(
                model.resources(), List.of(model.operations().get(1), model.operations().get(0)), model.products()));
        FactoryModelVersion stepReordered = FactoryModelPublisher.publish(new FactoryModel(
                model.resources(), List.of(new OperationDefinition(1, "One", List.of(
                        model.operations().get(0).steps().get(1), model.operations().get(0).steps().get(0))),
                        model.operations().get(1)), model.products()));
        FactoryModelVersion productReordered = FactoryModelPublisher.publish(new FactoryModel(
                model.resources(), model.operations(), List.of(model.products().get(1), model.products().get(0))));
        FactoryModelVersion negativeZero = FactoryModelPublisher.publish(new FactoryModel(
                List.of(new ConfiguredResource(new MachineId(1), "A", 1, -0.0, 0), model.resources().get(1)),
                model.operations(), model.products()));

        assertNotEquals(original.fingerprint(), resourceReordered.fingerprint());
        assertNotEquals(original.fingerprint(), operationReordered.fingerprint());
        assertNotEquals(original.fingerprint(), stepReordered.fingerprint());
        assertNotEquals(original.fingerprint(), productReordered.fingerprint());
        assertNotEquals(original.fingerprint(), negativeZero.fingerprint());
    }

    @Test
    void negativeFiniteCapacityUsesItsExactBinary64Payload() {
        FactoryModelVersion version = FactoryModelPublisher.publish(modelWithCapacity(-123.5));

        assertNotEquals(
                FactoryModelPublisher.publish(modelWithCapacity(123.5)).fingerprint(),
                version.fingerprint());
    }

    @Test
    void differentNaNPayloadsHaveTheSameFingerprint() {
        assertEquals(
                FactoryModelPublisher.publish(modelWithCapacity(Double.longBitsToDouble(0x7ff0000000000001L))).fingerprint(),
                FactoryModelPublisher.publish(modelWithCapacity(Double.longBitsToDouble(0x7fffffffffffffffL))).fingerprint());
    }

    @Test
    void allTextFieldsRejectMalformedUnicodeAtPublication() {
        OperationDefinition operation = representativeModel().operations().get(0);
        OperationStepDefinition step = operation.steps().get(0);
        List<FactoryModel> malformed = List.of(
                new FactoryModel(
                        List.of(new ConfiguredResource(new MachineId(1), "bad\uD800", 1, null, 0)),
                        representativeModel().operations(), representativeModel().products()),
                new FactoryModel(representativeModel().resources(),
                        List.of(new OperationDefinition(operation.id(), "bad\uD800", operation.steps())),
                        representativeModel().products()),
                new FactoryModel(representativeModel().resources(),
                        List.of(new OperationDefinition(operation.id(), operation.name(), List.of(
                                new OperationStepDefinition(step.stepId(), "bad\uDC00", step.eligibleResources(), step.duration())))),
                        representativeModel().products()),
                new FactoryModel(representativeModel().resources(), representativeModel().operations(),
                        List.of(new ProductDefinition(new ProductId(8), "bad\uDC00", 42))));

        for (FactoryModel model : malformed) {
            assertThrows(FactoryModelValidationException.class, () -> FactoryModelPublisher.publish(model));
        }
    }

    @Test
    void malformedUnicodeIsRejectedAtPublicationWithTheOffendingField() {
        FactoryModel malformed = new FactoryModel(
                List.of(new ConfiguredResource(new MachineId(1), "bad\uD800", 1, null, 0)),
                List.of(),
                List.of());

        FactoryModelValidationException exception = assertThrows(
                FactoryModelValidationException.class, () -> FactoryModelPublisher.publish(malformed));

        assertEquals("resources[Machine(1)].name", exception.result().errors().get(0).field());
    }

    // ---- Spatial-record coverage ----------------------------------------------------------------

    @Test
    void everySpatialFieldAndItsOrderParticipatesInTheFingerprint() {
        FactoryModelVersion original = FactoryModelPublisher.publish(spatialModel(representativeSpatial()));

        // Plant-scope header: each value, and swaps of distinct values, change identity.
        assertDifferent(original, spatialModel(spatial(11, 6, 2, 3, 0, 0, 2, 3, 4, 1, 3, 2)));
        assertDifferent(original, spatialModel(spatial(10, 7, 2, 3, 0, 0, 2, 3, 4, 1, 3, 2)));
        assertDifferent(
                FactoryModelPublisher.publish(spatialModel(spatial(8, 7, 2, 3, 0, 0, 2, 3, 4, 1, 3, 2))),
                spatialModel(spatial(7, 8, 2, 3, 0, 0, 2, 3, 4, 1, 3, 2)));
        assertDifferent(original, spatialModel(spatial(10, 6, 5, 3, 0, 0, 2, 3, 4, 1, 3, 2)));
        assertDifferent(original, spatialModel(spatial(10, 6, 2, 7, 0, 0, 2, 3, 4, 1, 3, 2)));
        assertDifferent(original, spatialModel(spatial(10, 6, 3, 2, 0, 0, 2, 3, 4, 1, 3, 2)));
        // Per-resource placement: x, y and an x/y swap.
        assertDifferent(original, spatialModel(spatial(10, 6, 2, 3, 0, 0, 2, 3, 5, 1, 3, 2)));
        assertDifferent(original, spatialModel(spatial(10, 6, 2, 3, 0, 0, 2, 3, 4, 2, 3, 2)));
        assertDifferent(original, spatialModel(spatial(10, 6, 2, 3, 0, 0, 2, 3, 1, 4, 3, 2)));
        // Per-resource footprint: an extent change and a width/height swap, even though no current
        // Engine rule reads footprint.
        assertDifferent(original, spatialModel(spatial(10, 6, 2, 3, 0, 0, 2, 3, 4, 1, 2, 2)));
        assertDifferent(original, spatialModel(spatial(10, 6, 2, 3, 0, 0, 2, 3, 4, 1, 2, 3)));
    }

    @Test
    void positionsAreEncodedExactlyAsAuthoredWithoutTranslationNormalization() {
        FactoryModelVersion original = FactoryModelPublisher.publish(spatialModel(representativeSpatial()));
        FactoryModelVersion translated = FactoryModelPublisher.publish(
                spatialModel(spatial(10, 6, 2, 3, 1, 1, 2, 3, 5, 2, 3, 2)));

        assertNotEquals(original.fingerprint(), translated.fingerprint());
    }

    @Test
    void canonicalEncodingRefusesSpatialContentThatCannotBePublished() {
        FactoryModel production = spatialModel(representativeSpatial());
        SpatialRecord missingLayout = new SpatialRecord(
                new FactoryFloor(10, 6), 2, 3, List.of(representativeSpatial().resourceLayouts().get(0)));
        SpatialRecord outOfOrder = new SpatialRecord(
                new FactoryFloor(10, 6), 2, 3, List.of(
                        representativeSpatial().resourceLayouts().get(1), representativeSpatial().resourceLayouts().get(0)));

        for (SpatialRecord invalid : List.of(missingLayout, outOfOrder)) {
            FactoryModel model = new FactoryModel(
                    production.resources(), production.operations(), production.products(), Optional.of(invalid));
            assertThrows(IllegalArgumentException.class, () -> FactoryModelCanonicalForm.canonicalBytes(model));
            assertThrows(FactoryModelValidationException.class, () -> FactoryModelPublisher.publish(model));
        }
    }

    // ---- Helpers ----------------------------------------------------------------------------------

    private static void assertDifferent(FactoryModelVersion original, FactoryModel changed) {
        assertNotEquals(original.fingerprint(), FactoryModelPublisher.publish(changed).fingerprint());
    }

    private static FactoryModel modelWithResourceName(String name) {
        return new FactoryModel(
                List.of(new ConfiguredResource(new MachineId(-2), name, 3, -0.0, -7),
                        new ConfiguredResource(new MachineId(5), "Second", 1, null, 0)),
                representativeModel().operations(), representativeModel().products());
    }

    private static FactoryModel modelWithOperationName(String name) {
        OperationDefinition operation = representativeModel().operations().get(0);
        return new FactoryModel(representativeModel().resources(),
                List.of(new OperationDefinition(operation.id(), name, operation.steps())), representativeModel().products());
    }

    private static FactoryModel modelWithStepName(String name) {
        OperationDefinition operation = representativeModel().operations().get(0);
        OperationStepDefinition step = operation.steps().get(0);
        return new FactoryModel(representativeModel().resources(),
                List.of(new OperationDefinition(operation.id(), operation.name(),
                        List.of(new OperationStepDefinition(step.stepId(), name, step.eligibleResources(), step.duration())))),
                representativeModel().products());
    }

    private static FactoryModel modelWithProductName(String name) {
        return new FactoryModel(representativeModel().resources(), representativeModel().operations(),
                List.of(new ProductDefinition(new ProductId(8), name, 42)));
    }

    private static FactoryModel modelWithResourceId(long id) {
        OperationDefinition operation = representativeModel().operations().get(0);
        return new FactoryModel(
                List.of(new ConfiguredResource(new MachineId(id), "M|ill", 3, -0.0, -7),
                        new ConfiguredResource(new MachineId(5), "Second", 1, null, 0)),
                List.of(new OperationDefinition(operation.id(), operation.name(),
                        List.of(new OperationStepDefinition(operation.steps().get(0).stepId(),
                                operation.steps().get(0).name(), Set.of(new MachineId(id), new MachineId(5)),
                                operation.steps().get(0).duration())))),
                representativeModel().products());
    }

    private static FactoryModel modelWithCapacity(double capacity) {
        return new FactoryModel(
                List.of(new ConfiguredResource(new MachineId(-2), "M|ill", 3, capacity, -7),
                        new ConfiguredResource(new MachineId(5), "Second", 1, null, 0)),
                representativeModel().operations(), representativeModel().products());
    }
}
