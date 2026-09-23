import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

plugins {
    id("me.champeau.jmh") version "0.7.3"
}

dependencies {
    implementation(project(":types"))
    implementation("tools.jackson.core:jackson-databind:3.2.2")
    implementation("tools.jackson.core:jackson-core:3.2.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.22")
    implementation("tools.jackson.dataformat:jackson-dataformat-toml:3.2.2")
}

// JMH microbenchmarks (ported from crates/sim-core/benches). Run with
// `./gradlew :simulation:jmh`. ASM is pinned explicitly for JMH bytecode
// generation; benchmark sources use the same Java 21 release compatibility
// as the rest of the build.
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

// Retained simulation runtime and scenario-loader tests cover 125 of 145 executable lines (86.2%).
// The former 0.88 floor was raised for EventLog tests, whose implementation is now removed.
tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn(tasks.named("test"))
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = "0.86".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.named("jacocoTestCoverageVerification"))
}
