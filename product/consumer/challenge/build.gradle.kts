import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

// This module is intentionally headless and game-owned: it must not depend on
// Arcogine's runtime, simulation, finance, or mutable factory implementation
// types, or on any application framework. It is the Challenge boundary and
// stays independent of the factory runtime by construction (no `project(...)`
// dependencies below).

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn(tasks.named("test"))
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = "0.85".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.named("jacocoTestCoverageVerification"))
}
