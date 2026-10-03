import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

dependencies {
    implementation(project(":types"))
}

val governanceTest = tasks.named<Test>("test")

val verifyGovernanceCoverageEvidence = tasks.register("verifyGovernanceCoverageEvidence") {
    dependsOn(governanceTest)

    doLast {
        check(!governanceTest.get().state.noSource) {
            "Governance coverage gate requires test sources; :governance:test had no source."
        }

        val executionData = governanceTest.get()
            .extensions
            .getByType<JacocoTaskExtension>()
            .destinationFile

        check(executionData != null && executionData.isFile && executionData.length() > 0L) {
            "Governance coverage gate requires JaCoCo execution data from :governance:test."
        }
    }
}

// Coverage gate: fails the build if :governance line coverage drops below the
// measured floor, and the evidence guard prevents a no-tests vacuous pass.
tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn(verifyGovernanceCoverageEvidence)
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.named("jacocoTestCoverageVerification"))
}
