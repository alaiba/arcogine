import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

dependencies {
    implementation(project(":types"))
    implementation(project(":simulation"))
}

// Coverage gate: fails the build if sim-economy line coverage drops below the
// floor (e.g. if its tests are deleted). Mirrors the gate in sim-types.
tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn(tasks.named("test"))
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = "0.70".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.named("jacocoTestCoverageVerification"))
}
