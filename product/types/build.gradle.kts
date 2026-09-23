import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

// Coverage gate: fails the build if :types line coverage drops below the
// floor (e.g. if its tests are deleted). Other modules adopt their own gate.
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
