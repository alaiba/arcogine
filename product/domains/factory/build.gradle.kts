import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

dependencies {
    implementation(project(":types"))
    implementation(project(":simulation"))
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.22")
}

// Coverage gate: fails the build if sim-factory line coverage drops below the
// floor (e.g. if its tests are deleted).
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
