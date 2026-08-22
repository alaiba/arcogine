import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

plugins {
    id("me.champeau.jmh") version "0.7.3"
}

dependencies {
    implementation(project(":sim-types"))
    implementation("tools.jackson.core:jackson-databind:3.1.4")
    implementation("tools.jackson.core:jackson-core:3.1.4")
    implementation("tools.jackson.dataformat:jackson-dataformat-toml:3.2.2")
}

// JMH microbenchmarks (ported from crates/sim-core/benches). Run with
// `./gradlew :sim-core:jmh`. ASM is forced to a Java 25-aware version so the
// JMH bytecode generator can read the toolchain's class files.
jmh {
    jmhVersion = "1.37"
    fork = 1
    warmupIterations = 2
    iterations = 3
    timeOnIteration = "500ms"
    warmup = "500ms"
}

configurations.named("jmh") {
    resolutionStrategy.force("org.ow2.asm:asm:9.10.1")
}

// Coverage gate: fails the build if sim-core line coverage drops below the
// floor (e.g. if its ported Rust test suite is deleted). The minimum is set a
// few points below measured actual coverage. See docs/java-rewrite-plan.md.
tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn(tasks.named("test"))
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = "0.82".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.named("jacocoTestCoverageVerification"))
}
