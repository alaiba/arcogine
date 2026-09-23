rootProject.name = "arcogine"

include(
    "types",
    "governance",
    "simulation",
    "factory",
    "economy",
    "finance",
    "agents",
    "challenge",
    "challenge-factory-integration-test",
    "architecture-conformance-test",
)

project(":factory").projectDir = file("domains/factory")
project(":economy").projectDir = file("domains/economy")
project(":finance").projectDir = file("domains/finance")
project(":challenge").projectDir = file("consumer/challenge")
project(":challenge-factory-integration-test").projectDir = file("consumer/challenge-factory-integration-test")
