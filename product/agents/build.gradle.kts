import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

dependencies {
    implementation(project(":types"))
    implementation(project(":simulation"))
}

// Coverage gate: fails the build if sim-agents line coverage drops below the
// floor (e.g. if its tests are deleted).
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
