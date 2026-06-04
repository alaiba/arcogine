import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

dependencies {
    implementation(project(":sim-types"))
    implementation(project(":sim-core"))
}

// Coverage gate: fails the build if sim-factory line coverage drops below the
// floor (e.g. if its ported tests are deleted). See docs/java-rewrite-plan.md.
tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn(tasks.named("test"))
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = "0.88".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.named("jacocoTestCoverageVerification"))
}
