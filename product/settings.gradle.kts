rootProject.name = "arcogine"

include(
    "types",
    "governance",
    "storage",
    "simulation",
    "factory",
    "challenge",
    "challenge-factory-integration-test",
    "research-experiments",
    "architecture-conformance-test",
)

project(":factory").projectDir = file("domains/factory")
project(":challenge").projectDir = file("consumer/challenge")
project(":challenge-factory-integration-test").projectDir = file("consumer/challenge-factory-integration-test")
