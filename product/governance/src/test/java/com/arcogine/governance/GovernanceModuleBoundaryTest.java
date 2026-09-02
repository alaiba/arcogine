package com.arcogine.governance;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Structural evidence that {@code :governance} (and the G3 requirement/assertion/catalogue
 * packages added by this slice) stays domain-neutral: it may only depend on {@code :types} in its
 * main source set. {@code :factory} may only be a {@code testImplementation} dependency (used to
 * prove the G2/G3 seam against a real domain, not to let production Governance code depend on
 * factory implementation classes).
 */
class GovernanceModuleBoundaryTest {

    @Test
    void mainSourceSetDependsOnlyOnTypesModule() {
        Path buildFile = findGovernanceBuildFile();
        List<String> lines = readLines(buildFile);

        boolean sawMainImplementationOnTypes = false;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("//")) {
                continue;
            }
            if (trimmed.startsWith("implementation(project(")) {
                assertTrue(trimmed.contains(":types"), "unexpected main dependency: " + trimmed);
                sawMainImplementationOnTypes = true;
            }
            assertFalse(trimmed.startsWith("implementation(project(\":factory\""), trimmed);
            assertFalse(trimmed.startsWith("implementation(project(\":domains"), trimmed);
            assertFalse(trimmed.contains("spring"), trimmed);
        }
        assertTrue(sawMainImplementationOnTypes, "expected an implementation(project(\":types\")) dependency");
    }

    @Test
    void productionGovernanceCodeNeverReferencesFactoryPackage() throws IOException {
        Path mainSourceRoot = moduleRoot().resolve("src/main/java");
        try (var stream = Files.walk(mainSourceRoot)) {
            List<Path> javaFiles = stream.filter(p -> p.toString().endsWith(".java")).toList();
            assertTrue(javaFiles.size() > 10, "expected the G1/G2/G3 governance sources to be present");
            for (Path file : javaFiles) {
                String content = Files.readString(file);
                assertFalse(content.contains("com.arcogine.factory"), file + " references com.arcogine.factory");
                assertFalse(content.contains("org.springframework"), file + " references Spring");
            }
        }
    }

    private static Path findGovernanceBuildFile() {
        Path candidate = moduleRoot().resolve("build.gradle.kts");
        if (!Files.exists(candidate)) {
            fail("governance build.gradle.kts not found at " + candidate);
        }
        return candidate;
    }

    /** Gradle runs each module's tests with that module's own directory as the working directory. */
    private static Path moduleRoot() {
        return Path.of("").toAbsolutePath();
    }

    private static List<String> readLines(Path path) {
        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
