package com.arcogine.factory.research;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.process.FactoryRuntime;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Structural evidence that the research experiment package is test-only and reads only the supported
 * runtime contract: it creates no production dependency or API, and nothing in it consumes
 * scheduler, handler, or store internals as research truth.
 */
class ResearchPackageBoundaryTest {

    private static final String RESEARCH_PACKAGE = "com.arcogine.factory.research";

    /**
     * Tokens that would put implementation internals into research evidence: the internal scheduler
     * and its events, mutable machine/job/order/routing state, the handler that owns it, and the
     * internal events a command result lists. Supported observations and the draining supported-event
     * stream are the evidence boundary.
     */
    private static final List<String> INTERNALS = List.of(
            "com.arcogine.core.event.",
            "com.arcogine.core.queue.",
            "com.arcogine.factory.machines.",
            "com.arcogine.factory.jobs.",
            "com.arcogine.factory.orders.",
            "com.arcogine.factory.routing.",
            "FactoryHandler",
            "RecordingScheduler",
            "scheduledEvents");

    @Test
    void researchClassesLiveOnlyOnTheTestClasspathEntry() throws IOException {
        URL production = FactoryRuntime.class.getProtectionDomain().getCodeSource().getLocation();
        URL research = ExperimentRunner.class.getProtectionDomain().getCodeSource().getLocation();
        assertNotEquals(production, research, "research support must not share a classpath entry with production classes");

        // A loader that can see only the production entry finds no class of the research package.
        try (URLClassLoader productionOnly = new URLClassLoader(new URL[] {production}, null)) {
            for (Class<?> researchClass :
                    List.of(ExperimentRunner.class, ExperimentFixture.class, StarterCorpus.class, Oracle.class)) {
                assertNull(
                        productionOnly.getResource(classResource(researchClass)),
                        researchClass + " is present in production output");
            }
        }

        // Control: the same probe does find them on the entry where they live, so the check above is
        // not passing vacuously.
        try (URLClassLoader testOnly = new URLClassLoader(new URL[] {research}, null)) {
            assertNotNull(testOnly.getResource(classResource(ExperimentRunner.class)));
        }
    }

    private static String classResource(Class<?> type) {
        return type.getName().replace('.', '/') + ".class";
    }

    @Test
    void productionSourcesNeitherContainNorReferenceTheResearchPackage() throws IOException {
        Path mainSourceRoot = moduleRoot().resolve("src/main/java");
        assertFalse(
                Files.exists(mainSourceRoot.resolve(RESEARCH_PACKAGE.replace('.', '/'))),
                "the research package must not exist in production sources");
        try (var stream = Files.walk(mainSourceRoot)) {
            List<Path> javaFiles = stream.filter(p -> p.toString().endsWith(".java")).toList();
            assertTrue(javaFiles.size() > 10, "expected the factory production sources to be present");
            for (Path file : javaFiles) {
                assertFalse(Files.readString(file).contains(RESEARCH_PACKAGE), file + " references the research package");
            }
        }
    }

    @Test
    void researchSupportSourcesTouchOnlyTheSupportedRuntimeContract() throws IOException {
        Path researchRoot = moduleRoot().resolve("src/test/java").resolve(RESEARCH_PACKAGE.replace('.', '/'));
        try (var stream = Files.list(researchRoot)) {
            List<Path> supportSources = stream.filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> !p.getFileName().toString().endsWith("Test.java"))
                    .toList();
            assertTrue(supportSources.size() > 10, "expected the research support sources to be present");
            for (Path file : supportSources) {
                String content = Files.readString(file);
                for (String internal : INTERNALS) {
                    assertFalse(
                            content.contains(internal),
                            file.getFileName() + " reaches for implementation internals: " + internal);
                }
            }
        }
    }

    /** Gradle runs each module's tests with that module's own directory as the working directory. */
    private static Path moduleRoot() {
        return Path.of("").toAbsolutePath();
    }
}
