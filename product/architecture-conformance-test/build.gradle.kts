// This module is test-only proof scaffolding: it has no production sources
// and exists solely to run the durable cross-domain module-boundary rules
// (see ArchitectureTest) against every production and consumer module's main
// sources in one place. It depends directly on that module set so the
// guardrails execute as CI-checked rules rather than review discipline alone.
// It also sees the non-shipped research-experiments module, solely to enforce
// that nothing production- or consumer-owned depends on it.
dependencies {
    testImplementation(project(":types"))
    testImplementation(project(":factory"))
    testImplementation(project(":storage"))
    testImplementation(project(":finance"))
    testImplementation(project(":challenge"))
    testImplementation(project(":research-experiments"))

    testImplementation("com.tngtech.archunit:archunit-junit5:1.5.1")
}

// Proof-only module: no production code exists to cover, so unlike other
// modules this one does not wire jacocoTestCoverageVerification into `check`
// (there is no main source set to hold a coverage floor against).
