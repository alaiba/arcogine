package com.arcogine.factory.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.spatial.FactoryFloor;
import com.arcogine.factory.model.spatial.ResourceFootprint;
import com.arcogine.factory.model.spatial.ResourceLayout;
import com.arcogine.factory.model.spatial.ResourcePlacement;
import com.arcogine.factory.model.spatial.SpatialRecord;
import com.arcogine.factory.model.validation.FactoryModelValidationException;
import com.arcogine.factory.model.validation.FactoryModelValidator;
import com.arcogine.factory.model.validation.ModelValidationError;
import com.arcogine.factory.model.validation.ModelValidationResult;
import com.arcogine.governance.SemanticArtifactVerifier;
import com.arcogine.types.MachineId;
import com.arcogine.types.ModelFingerprint;
import com.arcogine.types.ProductId;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * The exact-build binding is conservative build context, not a semantic identity: these tests guard
 * that it covers every repository class whose executable behavior can change whether an artifact is
 * supported, decodes, is valid or verifies, without pinning an incidental build digest.
 */
class FactoryModelDefinitionBindingTest {

    private static final class ResourceProbe {}

    private static final int PRESENCE_OFFSET = "arcogine.factory-model\0".length();

    /**
     * Direct executable dependencies of artifact acceptance. The result type decides whether decoded
     * content is valid, and the concrete verifier is a nested class whose bytes are not those of the
     * enclosing {@link FactoryModelArtifact}; both were once missing from the binding.
     */
    private static final List<Class<?>> REQUIRED_DIRECT_DEPENDENCIES = List.of(
            FactoryModelValidator.class,
            ModelValidationResult.class,
            ModelValidationError.class,
            FactoryModelValidationException.class,
            ModelFingerprint.class,
            FactoryModelCanonicalForm.class,
            FactoryModelVersion.class,
            FactoryModelArtifact.class,
            FactoryModelArtifact.verifier().getClass());

    @Test
    void bindingHasTheExpectedShapeAndIsStableWithinOneBuild() {
        String binding = FactoryModelCanonicalForm.definitionBinding();

        assertTrue(binding.matches("factory-model-definition-build:sha256:[0-9a-f]{64}"), binding);
        assertEquals(binding, FactoryModelCanonicalForm.definitionBinding());
        assertEquals(binding, FactoryModelArtifact.verifier().definitionBinding());
        // The curated classes are the binding's actual input, not a parallel list.
        assertEquals(
                binding,
                FactoryModelCanonicalForm.definitionBindingOver(
                        FactoryModelCanonicalForm.definitionBindingClasses()));
        assertThrows(IllegalStateException.class,
                () -> FactoryModelCanonicalForm.definitionBindingOver(List.of(int.class)));
    }

    @Test
    void unreadableDefinitionClassBytesFailClosed() throws IOException, ClassNotFoundException {
        byte[] bytes;
        try (InputStream input = ResourceProbe.class.getResourceAsStream("FactoryModelDefinitionBindingTest$ResourceProbe.class")) {
            bytes = input.readAllBytes();
        }
        ClassLoader loader = new ClassLoader(ResourceProbe.class.getClassLoader()) {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                if (name.equals(ResourceProbe.class.getName())) {
                    return defineClass(name, bytes, 0, bytes.length);
                }
                return super.loadClass(name);
            }

            @Override
            public InputStream getResourceAsStream(String name) {
                if (name.endsWith("FactoryModelDefinitionBindingTest$ResourceProbe.class")) {
                    return new InputStream() {
                        @Override
                        public int read() throws IOException {
                            throw new IOException("test resource read failure");
                        }
                    };
                }
                return super.getResourceAsStream(name);
            }
        };
        Class<?> unreadable = loader.loadClass(ResourceProbe.class.getName());

        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> FactoryModelCanonicalForm.definitionBindingOver(List.of(unreadable)));
        assertEquals(IOException.class, failure.getCause().getClass());
    }

    @Test
    void bindingIsDistinctFromAndIndependentOfAModelContentFingerprint() {
        FactoryModelVersion first = version("Widget");
        FactoryModelVersion second = version("Other widget");
        String binding = FactoryModelArtifact.verifier().definitionBinding();

        assertNotEquals(first.fingerprint(), second.fingerprint());
        // The fingerprint is exactly the digest of the canonical bytes: no build context enters it,
        // and the binding is neither derived from nor embedded in any content.
        assertEquals(sha256(FactoryModelArtifact.encode(first)), first.fingerprint().digest());
        assertEquals(sha256(FactoryModelArtifact.encode(second)), second.fingerprint().digest());
        assertFalse(binding.contains(first.fingerprint().digest()));
        assertFalse(first.fingerprint().toString().contains(binding));
        assertFalse(new String(FactoryModelArtifact.encode(first), StandardCharsets.ISO_8859_1)
                .contains(binding));
    }

    @Test
    void bindingNamesEveryDirectDependencyOfArtifactVerification() {
        List<Class<?>> covered = FactoryModelCanonicalForm.definitionBindingClasses();

        for (Class<?> required : REQUIRED_DIRECT_DEPENDENCIES) {
            assertTrue(covered.contains(required), "binding omits " + required.getName());
        }
        assertEquals(covered.size(), Set.copyOf(covered).size(), "a class is hashed at most once");

        // Every covered class contributes to the digest, so dropping any one changes the binding.
        String binding = FactoryModelCanonicalForm.definitionBinding();
        for (Class<?> type : covered) {
            List<Class<?>> without = covered.stream().filter(other -> other != type).toList();
            assertNotEquals(
                    binding,
                    FactoryModelCanonicalForm.definitionBindingOver(without),
                    type.getName() + " does not contribute to the binding");
        }
    }

    @Test
    void everyRepositoryClassExecutedByArtifactVerificationIsCovered() throws Exception {
        Set<String> executed = repositoryClassesLoadedWhileVerifyingArtifacts();

        // The trace must really observe what it is meant to guard, or the check below is vacuous.
        for (Class<?> required : REQUIRED_DIRECT_DEPENDENCIES) {
            assertTrue(executed.contains(required.getName()), "trace never reached " + required.getName());
        }
        assertEquals(
                Set.of(),
                uncovered(executed, FactoryModelCanonicalForm.definitionBindingClasses()),
                "classes executed during artifact verification but absent from the binding");
    }

    @Test
    void removingARequiredDependencyFromTheBindingWouldBeDetected() throws Exception {
        Set<String> executed = repositoryClassesLoadedWhileVerifyingArtifacts();
        List<Class<?>> covered = FactoryModelCanonicalForm.definitionBindingClasses();

        for (Class<?> required : REQUIRED_DIRECT_DEPENDENCIES) {
            List<Class<?>> without =
                    covered.stream().filter(type -> !type.getName().equals(required.getName())).toList();
            assertEquals(
                    Set.of(required.getName()),
                    uncovered(executed, without),
                    "omitting " + required.getName() + " must be reported");
        }
    }

    /**
     * Repository classes the trace reached that the binding does not hash. The Governance verifier
     * port is pure interface with no executable behavior: it names what the concrete verifier
     * implements but contributes no code of its own.
     */
    private static Set<String> uncovered(Set<String> executed, List<Class<?>> covered) {
        Set<String> missing = new TreeSet<>(executed);
        covered.forEach(type -> missing.remove(type.getName()));
        missing.remove(SemanticArtifactVerifier.class.getName());
        return missing;
    }

    /**
     * Runs the supported, fingerprint, decode, validation-refusal and malformed-input paths of the
     * public verifier in a fresh class loader and records every {@code com.arcogine} class that
     * loader is asked to resolve. A fresh loader is used because classes this test JVM has already
     * loaded would otherwise never be observed loading.
     */
    private static Set<String> repositoryClassesLoadedWhileVerifyingArtifacts() throws Exception {
        Set<String> requested = new TreeSet<>();
        URL[] locations = Stream.of(
                        FactoryModelArtifact.class, ModelFingerprint.class, SemanticArtifactVerifier.class)
                .map(type -> type.getProtectionDomain().getCodeSource().getLocation())
                .distinct()
                .toArray(URL[]::new);
        try (URLClassLoader isolated = new URLClassLoader(locations, ClassLoader.getPlatformClassLoader()) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.startsWith("com.arcogine.")) {
                    requested.add(name);
                }
                return super.loadClass(name, resolve);
            }
        }) {
            Object verifier = isolated.loadClass(FactoryModelArtifact.class.getName())
                    .getMethod("verifier")
                    .invoke(null);
            Class<?> port = isolated.loadClass(SemanticArtifactVerifier.class.getName());
            Method fingerprint = port.getMethod("fingerprint", byte[].class);
            Method supports = port.getMethod("supports", isolated.loadClass(ModelFingerprint.class.getName()));
            for (byte[] artifact : verificationInputs()) {
                try {
                    supports.invoke(verifier, fingerprint.invoke(verifier, (Object) artifact));
                } catch (InvocationTargetException refused) {
                    // Refusal is part of the path under trace, not a failure of the trace.
                }
            }
        }
        return requested;
    }

    private static List<byte[]> verificationInputs() {
        byte[] absent = FactoryModelArtifact.encode(version("Widget"));
        byte[] present = FactoryModelArtifact.encode(publish(Optional.of(spatial())));

        // Grammar-valid bytes that violate a publication predicate: refused by the validator.
        byte[] zeroFloor = present.clone();
        ByteBuffer.wrap(zeroFloor, PRESENCE_OFFSET + 1, Long.BYTES).putLong(0);

        byte[] truncated = Arrays.copyOf(absent, absent.length - 1);
        return List.of(absent, present, zeroFloor, truncated);
    }

    private static SpatialRecord spatial() {
        return new SpatialRecord(
                new FactoryFloor(8, 4),
                0,
                1,
                List.of(
                        new ResourceLayout(new MachineId(1), new ResourcePlacement(0, 0), new ResourceFootprint(2, 2)),
                        new ResourceLayout(new MachineId(2), new ResourcePlacement(5, 1), new ResourceFootprint(2, 2))));
    }

    private static FactoryModelVersion version(String productName) {
        return publish(Optional.empty(), productName);
    }

    private static FactoryModelVersion publish(Optional<SpatialRecord> spatial) {
        return publish(spatial, "Widget");
    }

    private static FactoryModelVersion publish(Optional<SpatialRecord> spatial, String productName) {
        ConfiguredResource mill = new ConfiguredResource(new MachineId(1), "Mill", 2, 125.5, 3);
        ConfiguredResource packer = new ConfiguredResource(new MachineId(2), "Packer", 1, null, 0);
        OperationStepDefinition machine = new OperationStepDefinition(
                1, "Machine", Set.of(new MachineId(2), new MachineId(1)), 5);
        OperationStepDefinition pack = new OperationStepDefinition(2, "Pack", Set.of(new MachineId(2)), 2);
        OperationDefinition operation = new OperationDefinition(100, "Routing", List.of(machine, pack));
        ProductDefinition product = new ProductDefinition(new ProductId(10), productName, operation.id());
        return FactoryModelPublisher.publish(
                new FactoryModel(List.of(mill, packer), List.of(operation), List.of(product), spatial));
    }

    private static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
