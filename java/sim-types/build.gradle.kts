import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

dependencies {
    implementation("tools.jackson.core:jackson-databind:3.2.2")
    implementation("tools.jackson.core:jackson-core:3.2.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.22")
    implementation("tools.jackson.dataformat:jackson-dataformat-toml:3.2.2")
}

// Coverage gate: fails the build if sim-types line coverage drops below the
// floor (e.g. if its tests are deleted). Other modules adopt their own gate
// as their Rust test suites are ported. See docs/java-rewrite-plan.md.
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
