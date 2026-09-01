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
    "api",
    "cli",
)

project(":factory").projectDir = file("domains/factory")
project(":economy").projectDir = file("domains/economy")
project(":finance").projectDir = file("domains/finance")
project(":challenge").projectDir = file("consumer/challenge")

project(":api").projectDir = file("interfaces/api")
project(":cli").projectDir = file("interfaces/cli")
