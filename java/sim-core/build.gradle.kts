import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

dependencies {
    implementation(project(":sim-types"))
    implementation("tools.jackson.core:jackson-databind:3.1.4")
    implementation("tools.jackson.dataformat:jackson-dataformat-toml:3.1.4")
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
