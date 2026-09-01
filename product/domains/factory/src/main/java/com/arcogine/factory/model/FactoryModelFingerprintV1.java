package com.arcogine.factory.model;

import com.arcogine.types.MachineId;
import com.arcogine.types.ModelFingerprint;
import com.arcogine.types.ProductId;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** The durable, language-independent {@code factory-model:v1} canonicalization policy. */
final class FactoryModelFingerprintV1 {

    private static final byte[] PREFIX = "arcogine.factory-model.v1\0".getBytes(StandardCharsets.US_ASCII);

    private FactoryModelFingerprintV1() {}

    static ModelFingerprint fingerprint(FactoryModel model) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return new ModelFingerprint(
                    "factory-model",
                    "v1",
                    "sha256",
                    HexFormat.of().formatHex(digest.digest(canonicalBytes(model))));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    static ModelFingerprint fingerprint(byte[] canonicalBytes) {
        FactoryModel model = decodeCanonicalBytes(canonicalBytes);
        return fingerprint(model);
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

    static FactoryModel decodeCanonicalBytes(byte[] canonicalBytes) {
        if (canonicalBytes == null) {
            throw new NullPointerException("canonicalBytes");
        }
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(canonicalBytes))) {
            requirePrefix(input);

            List<ResourceDefinition> resources = new ArrayList<>();
            for (int index = 0, count = readCount(input); index < count; index++) {
                long id = input.readLong();
                String name = readText(input);
                long concurrency = input.readLong();
                if (concurrency < Integer.MIN_VALUE || concurrency > Integer.MAX_VALUE) {
                    throw new IOException("resource concurrency is outside the signed 32-bit range");
                }
                Double capacityLiters = readOptionalF64(input);
                long setupTime = input.readLong();
                resources.add(new ResourceDefinition(
                        new MachineId(id), name, (int) concurrency, capacityLiters, setupTime));
            }

            List<OperationDefinition> operations = new ArrayList<>();
            for (int index = 0, count = readCount(input); index < count; index++) {
                long id = input.readLong();
                String name = readText(input);
                List<OperationStepDefinition> steps = new ArrayList<>();
                for (int stepIndex = 0, stepCount = readCount(input);
                        stepIndex < stepCount;
                        stepIndex++) {
                    long stepId = input.readLong();
                    String stepName = readText(input);
                    long duration = input.readLong();
                    Set<MachineId> eligibleResources = new LinkedHashSet<>();
                    for (int resourceIndex = 0, resourceCount = readCount(input);
                            resourceIndex < resourceCount;
                            resourceIndex++) {
                        eligibleResources.add(new MachineId(input.readLong()));
                    }
                    steps.add(new OperationStepDefinition(stepId, stepName, eligibleResources, duration));
                }
                operations.add(new OperationDefinition(id, name, steps));
            }

            List<ProductDefinition> products = new ArrayList<>();
            for (int index = 0, count = readCount(input); index < count; index++) {
                products.add(new ProductDefinition(
                        new ProductId(input.readLong()), readText(input), input.readLong()));
            }
            if (input.read() != -1) {
                throw new IOException("trailing bytes in canonical factory model artifact");
            }

            FactoryModel model = new FactoryModel(resources, operations, products);
            if (!Arrays.equals(canonicalBytes, canonicalBytes(model))) {
                throw new IOException("factory model artifact is decodable but not canonical");
            }
            return model;
        } catch (IOException | RuntimeException e) {
            if (e instanceof IllegalArgumentException illegalArgumentException) {
                throw illegalArgumentException;
            }
            throw new IllegalArgumentException("invalid factory-model:v1 canonical artifact", e);
        }
    }

    private static int readCount(DataInputStream input) throws IOException {
        long count = input.readLong();
        if (count < 0 || count > Integer.MAX_VALUE) {
            throw new IOException("collection count is outside the supported range: " + count);
        }
        return (int) count;
    }

    private static String readText(DataInputStream input) throws IOException {
        int length = readCount(input);
        byte[] bytes = input.readNBytes(length);
        if (bytes.length != length) {
            throw new EOFException("truncated UTF-8 field");
        }
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException e) {
            throw new IOException("invalid UTF-8 field", e);
        }
    }

    private static Double readOptionalF64(DataInputStream input) throws IOException {
        int marker = input.read();
        if (marker == 0) {
            return null;
        }
        if (marker != 1) {
            throw new IOException("invalid optional f64 marker: " + marker);
        }
        return Double.longBitsToDouble(input.readLong());
    }

    private static void requirePrefix(DataInputStream input) throws IOException {
        byte[] prefix = input.readNBytes(PREFIX.length);
        if (!Arrays.equals(prefix, PREFIX)) {
            throw new IOException("unsupported canonical factory model artifact policy");
        }
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
