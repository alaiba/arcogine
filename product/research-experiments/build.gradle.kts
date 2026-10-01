import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

// Non-shipped research infrastructure: the reusable substrate for deterministic research
// experiments over the supported Factory runtime contract (docs/development/researching.md).
// Its main sources depend only on :factory and :types, so the compiler confines them to what
// those modules expose; ResearchPackageBoundaryTest narrows that to the supported runtime
// contract. Production and consumer source sets never depend on this module: the root build
// refuses such a declaration and architecture-conformance-test rejects any reference.
dependencies {
    implementation(project(":factory"))
    implementation(project(":types"))

    // The corpus tests compare authored designs through Factory's semantic comparator, whose
    // result types belong to Governance; the reusable substrate itself never needs them.
    testImplementation(project(":governance"))
    testImplementation("com.tngtech.archunit:archunit-junit5:1.5.0")
}

// Coverage gate: fails the build if line coverage of the reusable substrate drops below the
// floor (e.g. if its corpus and contract tests are deleted).
tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn(tasks.named("test"))
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = "0.94".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.named("jacocoTestCoverageVerification"))
}

// Opt-in: compile and run experiment sources kept outside the tracked tree as additional tests of
// this module, for one invocation. The caller supplies the directory (absolute, or relative to the
// directory Gradle is invoked from); when the property is unset the build is unchanged.
//   ./gradlew :research-experiments:test -PresearchExperimentSources=<dir> --tests '<experiment>'
providers.gradleProperty("researchExperimentSources").orNull?.let { directory ->
    val requested = File(directory)
    val sources = if (requested.isAbsolute) requested else gradle.startParameter.currentDir.resolve(requested)
    if (!sources.isDirectory) {
        throw GradleException("researchExperimentSources is not a directory: $sources")
    }
    sourceSets.named("test") { java.srcDir(sources) }
}
