package com.arcogine.factory.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.validation.FactoryModelValidationException;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.nio.charset.StandardCharsets;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Golden vectors and sensitivity cases for the current Factory canonical form
 * (docs/architecture/factory-model.md).
 *
 * <p>The literal bytes and digests below were derived from an independent implementation of the
 * specified grammar, not from this codec. They pin the current definition only: a deliberate
 * definition change updates them together with the specification.
 */
class FactoryModelCanonicalFormTest {

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

    private static FactoryModel minimalModel() {
        return new FactoryModel(
                List.of(new ConfiguredResource(new MachineId(1), "Mill", 1, null, 0)),
                List.of(new OperationDefinition(
                        100, "Routing", List.of(new OperationStepDefinition(1, "Machine", Set.of(new MachineId(1)), 5)))),
                List.of(new ProductDefinition(new ProductId(10), "Widget", 100)));
    }

    // ---- Golden vectors ---------------------------------------------------------------------------

    @Test
    void definitionPrefixIsPinned() {
        byte[] bytes = FactoryModelCanonicalForm.canonicalBytes(representativeModel());
        byte[] expectedPrefix = HexFormat.of().parseHex(
                "6172636f67696e652e666163746f72792d6d6f64656c00");

        assertArrayEquals(expectedPrefix, Arrays.copyOf(bytes, expectedPrefix.length));
        assertArrayEquals("arcogine.factory-model\0".getBytes(StandardCharsets.US_ASCII), expectedPrefix);
    }

    @Test
    void missingDigestProviderFailsClearlyAndRestoresProviders() {
        // Keep the definition class initialized before changing the process provider registry.
        FactoryModelCanonicalForm.definitionBinding();
        synchronized (Security.class) {
            Provider[] original = Security.getProviders();
            Provider[] shaProviders = Security.getProviders("MessageDigest.SHA-256");
            assertTrue(shaProviders.length > 0);
            for (Provider provider : shaProviders) {
                Security.removeProvider(provider.getName());
            }
            try {
                assertThrows(IllegalStateException.class,
                        () -> FactoryModelCanonicalForm.fingerprint(representativeModel()));
                assertThrows(IllegalStateException.class,
                        () -> FactoryModelCanonicalForm.definitionBindingOver(List.of(String.class)));
            } finally {
                for (int index = 0; index < original.length; index++) {
                    Provider provider = original[index];
                    if (Arrays.asList(shaProviders).contains(provider)) {
                        Security.insertProviderAt(provider, index + 1);
                    }
                }
            }
            assertArrayEquals(original, Security.getProviders());
        }
    }

    @Test
    void representativeVectorPinsCanonicalBytesAndFingerprint() {
        FactoryModelVersion version = FactoryModelPublisher.publish(representativeModel());

        assertArrayEquals(
                HexFormat.of().parseHex(
                        "6172636f67696e652e666163746f72792d6d6f64656c000000000000000002fffffffffffffffe00000000000000054d7c696c6c0000000000000003018000000000000000fffffffffffffff9000000000000000500000000000000065365636f6e6400000000000000010000000000000000000000000000000001000000000000002a00000000000000074f703af09f9a80000000000000000100000000000000090000000000000007537465703bc3a9000000000000000b0000000000000002fffffffffffffffe000000000000000500000000000000010000000000000008000000000000000750007cf0908080000000000000002a"),
                FactoryModelCanonicalForm.canonicalBytes(representativeModel()));
        assertEquals(
                "factory-model:sha256:b6d39eb7880db14cec0982cfaec623e95018a7ca78fa5674deffe17f8328b9d0",
                version.fingerprint().toString());
    }

    @Test
    void minimalVectorPinsFingerprint() {
        assertEquals(
                "factory-model:sha256:f36253ac70d8a454a8a4670e81d7815c8da92d97e526f40d351bbae5921f9486",
                FactoryModelPublisher.publish(minimalModel()).fingerprint().toString());
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

        // The low-level encoder must refuse malformed text even when publication is bypassed.
        assertThrows(IllegalArgumentException.class,
                () -> FactoryModelCanonicalForm.canonicalBytes(malformed));
        assertThrows(FactoryModelValidationException.class,
                () -> FactoryModelPublisher.publish(modelWithResourceName("bad\uD800x")));
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
