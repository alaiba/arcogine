package com.arcogine.factory.model;

import com.arcogine.factory.model.spatial.FactoryFloor;
import com.arcogine.factory.model.spatial.ResourceFootprint;
import com.arcogine.factory.model.spatial.ResourceLayout;
import com.arcogine.factory.model.spatial.ResourcePlacement;
import com.arcogine.factory.model.spatial.SpatialRecord;
import com.arcogine.factory.model.validation.FactoryModelValidator;
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
import java.util.Optional;
import java.util.Set;

/**
 * The language-independent canonical form of the current Factory model definition, specified by
 * docs/architecture/factory-model.md.
 *
 * <p>For one definition, equal canonical content always produces the same bytes and fingerprint.
 * The definition itself may change between development revisions, so the fingerprint identifies
 * canonical content in the producing context; it is not an exact reference to the definition that
 * produced it.
 */
final class FactoryModelCanonicalForm {

    static final String NAMESPACE = "factory-model";
    static final String ALGORITHM = "sha256";

    private static final byte[] PREFIX =
            ("arcogine." + NAMESPACE + "\0").getBytes(StandardCharsets.US_ASCII);
    private static final int ABSENT = 0;
    private static final int PRESENT = 1;

    private FactoryModelCanonicalForm() {}

    static ModelFingerprint fingerprint(FactoryModel model) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return new ModelFingerprint(
                    NAMESPACE,
                    ALGORITHM,
                    HexFormat.of().formatHex(digest.digest(canonicalBytes(model))));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    static boolean identifies(ModelFingerprint fingerprint) {
        return NAMESPACE.equals(fingerprint.namespace())
                && ALGORITHM.equals(fingerprint.algorithm());
    }

    /**
     * Names the exact build of the current definition: a digest of the compiled classes that define
     * the model's records, validation and canonical form. It changes whenever that code changes --
     * behavior-preserving refactors and a different compiler included -- so persisted proving
     * material never outlives the definition build that wrote it. It is build context, not a
     * semantic identity or version, and never participates in a fingerprint.
     */
    static String definitionBinding() {
        return DefinitionBinding.VALUE;
    }

    private static final class DefinitionBinding {

        private static final List<Class<?>> DEFINITION_CLASSES = List.of(
                FactoryModel.class,
                ConfiguredResource.class,
                OperationDefinition.class,
                OperationStepDefinition.class,
                ProductDefinition.class,
                SpatialRecord.class,
                FactoryFloor.class,
                ResourceLayout.class,
                ResourcePlacement.class,
                ResourceFootprint.class,
                MachineId.class,
                ProductId.class,
                FactoryModelValidator.class,
                FactoryModelVersion.class,
                FactoryModelCanonicalForm.class,
                FactoryModelArtifact.class);

        private static final String VALUE = compute();

        private static String compute() {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                for (Class<?> type : DEFINITION_CLASSES) {
                    byte[] name = type.getName().getBytes(StandardCharsets.UTF_8);
                    byte[] bytecode = classBytes(type);
                    digest.update(ByteBuffer.allocate(Long.BYTES).putLong(name.length).array());
                    digest.update(name);
                    digest.update(ByteBuffer.allocate(Long.BYTES).putLong(bytecode.length).array());
                    digest.update(bytecode);
                }
                return "factory-model-definition-build:sha256:" + HexFormat.of().formatHex(digest.digest());
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("SHA-256 not available", e);
            }
        }

        private static byte[] classBytes(Class<?> type) {
            String resource = type.getName().substring(type.getName().lastIndexOf('.') + 1) + ".class";
            try (var input = type.getResourceAsStream(resource)) {
                if (input == null) {
                    throw new IllegalStateException("definition class bytes are unavailable: " + type.getName());
                }
                return input.readAllBytes();
            } catch (IOException e) {
                throw new IllegalStateException("definition class bytes are unreadable: " + type.getName(), e);
            }
        }
    }

    /**
     * Encodes {@code model} canonically. Canonical encoding is only defined for publishable
     * content; a present spatial record that does not give every configured resource exactly one
     * layout in resource-list order is rejected rather than silently dropped or reordered.
     */
    static byte[] canonicalBytes(FactoryModel model) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bytes.writeBytes(PREFIX);

        List<ConfiguredResource> resources = model.resources();
        Optional<SpatialRecord> spatial = model.spatial();
        List<ResourceLayout> layouts = spatial.map(SpatialRecord::resourceLayouts).orElse(List.of());
        if (spatial.isPresent()) {
            requireStructuralCoverage(resources, layouts);
            SpatialRecord record = spatial.orElseThrow();
            bytes.write(PRESENT);
            writeI64(bytes, record.floor().width());
            writeI64(bytes, record.floor().height());
            writeI64(bytes, record.ticksPerCell());
            writeI64(bytes, record.handlingTicks());
        } else {
            bytes.write(ABSENT);
        }

        writeU64(bytes, resources.size());
        for (int index = 0; index < resources.size(); index++) {
            ConfiguredResource resource = resources.get(index);
            writeI64(bytes, resource.id().value());
            writeText(bytes, resource.name());
            writeI64(bytes, resource.concurrency());
            writeOptionalF64(bytes, resource.capacityLiters());
            writeI64(bytes, resource.setupTime());
            if (spatial.isPresent()) {
                ResourceLayout layout = layouts.get(index);
                writeI64(bytes, layout.placement().x());
                writeI64(bytes, layout.placement().y());
                writeI64(bytes, layout.footprint().width());
                writeI64(bytes, layout.footprint().height());
            }
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

    /**
     * Strictly decodes canonical bytes into model content. Grammar violations and byte strings
     * that parse but do not re-encode to themselves are rejected; publication predicates are
     * applied by the caller when the content is published as a {@link FactoryModelVersion}.
     */
    static FactoryModel decode(byte[] canonicalBytes) {
        if (canonicalBytes == null) {
            throw new NullPointerException("canonicalBytes");
        }
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(canonicalBytes))) {
            requirePrefix(input);
            boolean spatialPresent = readPresence(input);
            FactoryFloor floor = null;
            long ticksPerCell = 0;
            long handlingTicks = 0;
            if (spatialPresent) {
                floor = new FactoryFloor(input.readLong(), input.readLong());
                ticksPerCell = input.readLong();
                handlingTicks = input.readLong();
            }

            List<ConfiguredResource> resources = new ArrayList<>();
            List<ResourceLayout> layouts = new ArrayList<>();
            for (int index = 0, count = readCount(input); index < count; index++) {
                long id = input.readLong();
                String name = readText(input);
                long concurrency = input.readLong();
                if (concurrency < Integer.MIN_VALUE || concurrency > Integer.MAX_VALUE) {
                    throw new IOException("resource concurrency is outside the signed 32-bit range");
                }
                Double capacityLiters = readOptionalF64(input);
                long setupTime = input.readLong();
                resources.add(new ConfiguredResource(
                        new MachineId(id), name, (int) concurrency, capacityLiters, setupTime));
                if (spatialPresent) {
                    ResourcePlacement placement = new ResourcePlacement(input.readLong(), input.readLong());
                    ResourceFootprint footprint = new ResourceFootprint(input.readLong(), input.readLong());
                    layouts.add(new ResourceLayout(new MachineId(id), placement, footprint));
                }
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

            Optional<SpatialRecord> spatial = spatialPresent
                    ? Optional.of(new SpatialRecord(floor, ticksPerCell, handlingTicks, layouts))
                    : Optional.empty();
            FactoryModel model = new FactoryModel(resources, operations, products, spatial);
            if (!Arrays.equals(canonicalBytes, canonicalBytes(model))) {
                throw new IOException("factory model artifact is decodable but not canonical");
            }
            return model;
        } catch (IOException | RuntimeException e) {
            if (e instanceof IllegalArgumentException illegalArgumentException) {
                throw illegalArgumentException;
            }
            throw new IllegalArgumentException(
                    "invalid " + NAMESPACE + " canonical artifact", e);
        }
    }

    private static void requireStructuralCoverage(
            List<ConfiguredResource> resources, List<ResourceLayout> layouts) {
        boolean covered = layouts.size() == resources.size();
        for (int index = 0; covered && index < resources.size(); index++) {
            covered = layouts.get(index).resourceId().equals(resources.get(index).id());
        }
        if (!covered) {
            throw new IllegalArgumentException(
                    "canonical encoding requires exactly one layout per resource in resource-list order");
        }
    }

    private static boolean readPresence(DataInputStream input) throws IOException {
        int marker = input.read();
        if (marker == ABSENT) {
            return false;
        }
        if (marker == PRESENT) {
            return true;
        }
        throw new IOException("invalid spatial-record presence marker: " + marker);
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
        if (marker == ABSENT) {
            return null;
        }
        if (marker != PRESENT) {
            throw new IOException("invalid optional f64 marker: " + marker);
        }
        return Double.longBitsToDouble(input.readLong());
    }

    private static void requirePrefix(DataInputStream input) throws IOException {
        byte[] prefix = input.readNBytes(PREFIX.length);
        if (!Arrays.equals(prefix, PREFIX)) {
            throw new IOException("unsupported canonical factory model artifact definition");
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
            bytes.write(ABSENT);
            return;
        }
        bytes.write(PRESENT);
        long bits = Double.isNaN(value) ? 0x7ff8000000000000L : Double.doubleToRawLongBits(value);
        writeI64(bytes, bits);
    }
}
