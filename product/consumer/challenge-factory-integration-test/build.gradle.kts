// This module is test-only proof scaffolding: it has no production sources
// and exists solely to demonstrate, in one place with an actual dependency on
// both sides, that canonical Factory-executability (:factory's
// FactoryModelValidator) and challenge admissibility (:challenge's
// CandidateAdmissibilityPolicy) are independent validation axes. Neither
// :factory nor :challenge gains a dependency on the other from this module;
// only this dedicated test-only module depends on both.
dependencies {
    testImplementation(project(":factory"))
    testImplementation(project(":types"))
    testImplementation(project(":challenge"))
}

// Proof-only module: no production code exists to cover, so unlike other
// modules this one does not wire jacocoTestCoverageVerification into `check`
// (there is no main source set to hold a coverage floor against).
