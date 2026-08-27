package com.arcogine.factory.model;

import com.arcogine.types.MachineId;
import com.arcogine.types.ModelFingerprint;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

/** The durable, language-independent {@code factory-model:v1} canonicalization policy. */
final class FactoryModelFingerprintV1 {

    private static final byte[] PREFIX = "arcogine.factory-model.v1\0".getBytes(StandardCharsets.US_ASCII);

    private FactoryModelFingerprintV1() {}

    static ModelFingerprint fingerprint(FactoryModel model) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return new ModelFingerprint("factory-model", "v1", "sha256", HexFormat.of().formatHex(digest.digest(canonicalBytes(model))));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    static byte[] canonicalBytes(FactoryModel model) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bytes.writeBytes(PREFIX);

        List<ResourceDefinition> resources = model.resources();
        writeU64(bytes, resources.size());
        for (ResourceDefinition resource : resources) {
            writeI64(bytes, resource.id().value());
            writeText(bytes, resource.name());
            writeI64(bytes, resource.concurrency());
            writeOptionalF64(bytes, resource.capacityLiters());
            writeI64(bytes, resource.setupTime());
        }

        List<OperationDefinition> operations = model.operations();
        writeU64(bytes, operations.size());
        for (OperationDefinition operation : operations) {
            writeI64(bytes, operation.id());
            writeText(bytes, operation.name());
            writeU64(bytes, operation.steps().size());
            for (OperationStepDefinition step : operation.steps()) {
                writeI64(bytes, step.stepId());
                writeText(bytes, step.name());
                writeI64(bytes, step.duration());
                List<MachineId> eligibleResources = step.eligibleResources().stream()
                        .sorted()
                        .toList();
                writeU64(bytes, eligibleResources.size());
                for (MachineId resourceId : eligibleResources) {
                    writeI64(bytes, resourceId.value());
                }
            }
        }

        List<ProductDefinition> products = model.products();
        writeU64(bytes, products.size());
        for (ProductDefinition product : products) {
            writeI64(bytes, product.id().value());
            writeText(bytes, product.name());
            writeI64(bytes, product.operationId());
        }
        return bytes.toByteArray();
    }

    private static void writeU64(ByteArrayOutputStream bytes, long value) {
        writeI64(bytes, value);
    }

    private static void writeI64(ByteArrayOutputStream bytes, long value) {
        bytes.writeBytes(ByteBuffer.allocate(Long.BYTES).putLong(value).array());
    }

    private static void writeText(ByteArrayOutputStream bytes, String value) {
        ByteBuffer encoded;
        try {
            encoded = StandardCharsets.UTF_8.newEncoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .encode(CharBuffer.wrap(value));
        } catch (CharacterCodingException e) {
            throw new IllegalArgumentException("text contains an invalid Unicode scalar value", e);
        }
        byte[] text = new byte[encoded.remaining()];
        encoded.get(text);
        writeU64(bytes, text.length);
        bytes.writeBytes(text);
    }

    private static void writeOptionalF64(ByteArrayOutputStream bytes, Double value) {
        if (value == null) {
            bytes.write(0);
            return;
        }
        bytes.write(1);
        long bits = Double.isNaN(value) ? 0x7ff8000000000000L : Double.doubleToRawLongBits(value);
        writeI64(bytes, bits);
    }
}
