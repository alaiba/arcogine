package com.arcogine.factory.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arcogine.factory.model.validation.FactoryModelValidationException;
import com.arcogine.types.MachineId;
import com.arcogine.types.ModelFingerprint;
import com.arcogine.types.ProductId;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FactoryModelFingerprintV1Test {

    private static FactoryModel representativeModel() {
        return new FactoryModel(
                List.of(
                        new ResourceDefinition(new MachineId(-2), "M|ill", 3, -0.0, -7),
                        new ResourceDefinition(new MachineId(5), "Second", 1, null, 0)),
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

    @Test
    void representativeVectorPinsCanonicalBytesAndFingerprint() {
        FactoryModelVersion version = FactoryModelPublisher.publish(representativeModel());

        assertArrayEquals(
                HexFormat.of().parseHex(
                        "6172636f67696e652e666163746f72792d6d6f64656c2e7631000000000000000002fffffffffffffffe00000000000000054d7c696c6c0000000000000003018000000000000000fffffffffffffff9000000000000000500000000000000065365636f6e6400000000000000010000000000000000000000000000000001000000000000002a00000000000000074f703af09f9a80000000000000000100000000000000090000000000000007537465703bc3a9000000000000000b0000000000000002fffffffffffffffe000000000000000500000000000000010000000000000008000000000000000750007cf0908080000000000000002a"),
                FactoryModelFingerprintV1.canonicalBytes(representativeModel()));
        assertEquals(
                "factory-model:v1:sha256:5b08fbe9a8db08f229ed6161444a05834b306cb4d54425ae06b5f99cef9acd01",
                version.fingerprint().toString());
    }

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
    void meaningfulFieldAndListChangesChangeFingerprint() {
        FactoryModelVersion original = FactoryModelPublisher.publish(representativeModel());
        FactoryModel reordered = new FactoryModel(
                List.of(
                        new ResourceDefinition(new MachineId(5), "Second", 1, null, 0),
                        new ResourceDefinition(new MachineId(-2), "M|ill", 3, -0.0, -7)),
                List.of(new OperationDefinition(
                        42, "Op:🚀", List.of(new OperationStepDefinition(
                                9, "Step;é", Set.of(new MachineId(-2), new MachineId(5)), 11)))),
                List.of(new ProductDefinition(new ProductId(8), "P\u0000|𐀀", 42)));

        assertNotEquals(original.fingerprint(), FactoryModelPublisher.publish(reordered).fingerprint());
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
                List.of(new ResourceDefinition(new MachineId(1), "A", 1, 0.0, 0),
                        new ResourceDefinition(new MachineId(2), "B", 1, null, 0)),
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
                List.of(new ResourceDefinition(new MachineId(1), "A", 1, -0.0, 0), model.resources().get(1)),
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
        void allTextFieldsRejectMalformedUnicodeAtPublication() {
                assertMalformedResourceNameIsRejected();
                assertMalformedOperationNameIsRejected();
                assertMalformedStepNameIsRejected();
                assertMalformedProductNameIsRejected();
        }

        @Test
        void differentNaNPayloadsHaveTheSameFingerprint() {
                assertEquals(
                                FactoryModelPublisher.publish(modelWithCapacity(Double.longBitsToDouble(0x7ff0000000000001L))).fingerprint(),
                                FactoryModelPublisher.publish(modelWithCapacity(Double.longBitsToDouble(0x7fffffffffffffffL))).fingerprint());
        }

        @Test
        void legacyContentHashCompatibilityValueRemainsPinned() {
                assertEquals(
                        "3fbe9d181b8d982150d85a1ef422ad06142fc79a573ae43687fdedbe15cc569c",
                        FactoryModelPublisher.publish(validLegacyFixture()).contentHash());
        }

        private static FactoryModel validLegacyFixture() {
                return new FactoryModel(
                                List.of(new ResourceDefinition(new MachineId(1), "Mill", 1, null, 0)),
                                List.of(new OperationDefinition(100, "Widget routing",
                                List.of(new OperationStepDefinition(1, "Rough milling", Set.of(new MachineId(1)), 5)))),
                                List.of(new ProductDefinition(new ProductId(10), "Widget", 100)));
        }

        private static void assertMalformedResourceNameIsRejected() {
                FactoryModel malformed = new FactoryModel(
                                List.of(new ResourceDefinition(new MachineId(1), "bad\uD800", 1, null, 0)),
                                representativeModel().operations(), representativeModel().products());
                assertThrows(FactoryModelValidationException.class, () -> FactoryModelPublisher.publish(malformed));
        }

        private static void assertMalformedOperationNameIsRejected() {
                OperationDefinition operation = representativeModel().operations().get(0);
                FactoryModel malformed = new FactoryModel(representativeModel().resources(),
                                List.of(new OperationDefinition(operation.id(), "bad\uD800", operation.steps())), representativeModel().products());
                assertThrows(FactoryModelValidationException.class, () -> FactoryModelPublisher.publish(malformed));
        }

        private static void assertMalformedStepNameIsRejected() {
                OperationDefinition operation = representativeModel().operations().get(0);
                OperationStepDefinition step = operation.steps().get(0);
                FactoryModel malformed = new FactoryModel(representativeModel().resources(),
                                List.of(new OperationDefinition(operation.id(), operation.name(),
                                                List.of(new OperationStepDefinition(step.stepId(), "bad\uDC00", step.eligibleResources(), step.duration())))),
                                representativeModel().products());
                assertThrows(FactoryModelValidationException.class, () -> FactoryModelPublisher.publish(malformed));
        }

        private static void assertMalformedProductNameIsRejected() {
                FactoryModel malformed = new FactoryModel(representativeModel().resources(), representativeModel().operations(),
                                List.of(new ProductDefinition(new ProductId(8), "bad\uDC00", 42)));
                assertThrows(FactoryModelValidationException.class, () -> FactoryModelPublisher.publish(malformed));
        }

    private static void assertDifferent(FactoryModelVersion original, FactoryModel changed) {
        assertNotEquals(original.fingerprint(), FactoryModelPublisher.publish(changed).fingerprint());
    }

    private static FactoryModel modelWithResourceName(String name) {
        return new FactoryModel(
                List.of(new ResourceDefinition(new MachineId(-2), name, 3, -0.0, -7),
                        new ResourceDefinition(new MachineId(5), "Second", 1, null, 0)),
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
                List.of(new ResourceDefinition(new MachineId(id), "M|ill", 3, -0.0, -7),
                        new ResourceDefinition(new MachineId(5), "Second", 1, null, 0)),
                List.of(new OperationDefinition(operation.id(), operation.name(),
                        List.of(new OperationStepDefinition(operation.steps().get(0).stepId(),
                                operation.steps().get(0).name(), Set.of(new MachineId(id), new MachineId(5)),
                                operation.steps().get(0).duration())))),
                representativeModel().products());
    }

    private static FactoryModel modelWithCapacity(double capacity) {
        return new FactoryModel(
                List.of(new ResourceDefinition(new MachineId(-2), "M|ill", 3, capacity, -7),
                        new ResourceDefinition(new MachineId(5), "Second", 1, null, 0)),
                representativeModel().operations(), representativeModel().products());
    }

    @Test
    void malformedUnicodeIsRejectedAtPublication() {
        FactoryModel malformed = new FactoryModel(
                List.of(new ResourceDefinition(new MachineId(1), "bad\uD800", 1, null, 0)),
                List.of(),
                List.of());

        FactoryModelValidationException exception = assertThrows(
                FactoryModelValidationException.class, () -> FactoryModelPublisher.publish(malformed));

        assertEquals("resources[Machine(1)].name", exception.result().errors().get(0).field());
    }

    @Test
    void durableFingerprintRemainsSeparateFromLegacyHash() {
        FactoryModelVersion version = FactoryModelPublisher.publish(representativeModel());

        assertNotEquals(version.contentHash(), version.fingerprint().digest());
        assertEquals(new ModelFingerprint("factory-model", "v1", "sha256", version.fingerprint().digest()), version.fingerprint());
    }
}