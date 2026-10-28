package com.arcogine.factory.model.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/**
 * The validator reports one step's missing eligible resources in ascending resource-ID order, for
 * every authored order and in every JVM. Iteration order of an immutable hash set is salted per
 * JVM, so the cross-JVM case is proved in several fresh child JVMs; a single-JVM check could pass
 * by luck.
 */
class MissingEligibleResourceOrderTest {

    private static final int FRESH_JVMS = 8;

    private static final String EXPECTED = "references nonexistent resource id: Machine(1)"
            + "|references nonexistent resource id: Machine(2)"
            + "|references nonexistent resource id: Machine(3)";

    @Test
    void reportsMissingResourcesInAscendingOrderForEveryAuthoredOrder() {
        for (List<Long> permutation : MissingEligibleResourceOrderProbe.PERMUTATIONS) {
            assertEquals(
                    EXPECTED,
                    MissingEligibleResourceOrderProbe.messages(permutation),
                    () -> "authored " + permutation);
        }
    }

    @Test
    void reportsMissingResourcesInAscendingOrderInEveryFreshJvm() throws Exception {
        List<String> expected =
                Collections.nCopies(MissingEligibleResourceOrderProbe.PERMUTATIONS.size(), EXPECTED);
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        for (int run = 0; run < FRESH_JVMS; run++) {
            Process process = new ProcessBuilder(
                            java,
                            "-cp",
                            System.getProperty("factory.test.classpath"),
                            MissingEligibleResourceOrderProbe.class.getName())
                    .redirectErrorStream(true)
                    .start();
            if (!process.waitFor(30, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new AssertionError("child JVM timed out");
            }
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(0, process.exitValue(), output);
            assertEquals(expected, output.lines().toList(), "fresh JVM " + run);
        }
    }
}
