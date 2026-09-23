// This module is test-only proof scaffolding: it has no production sources
// and exists solely to run the durable cross-domain module-boundary rules
// (see ArchitectureTest) against every domain module's main sources in one
// place. Historically these rules were hosted in interfaces/api's test
// classpath, which happened to already see every domain module; now that
// interfaces/api is gone, this module depends directly on the same domain
// set so the guardrails keep executing as CI-checked rules rather than
// review discipline alone.
dependencies {
    testImplementation(project(":types"))
    testImplementation(project(":factory"))
    testImplementation(project(":finance"))

    testImplementation("com.tngtech.archunit:archunit-junit5:1.5.0")
}

// Proof-only module: no production code exists to cover, so unlike other
// modules this one does not wire jacocoTestCoverageVerification into `check`
// (there is no main source set to hold a coverage floor against).
