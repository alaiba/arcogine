rootProject.name = "arcogine"

include(
    "types",
    "simulation",
    "factory",
    "economy",
    "finance",
    "agents",
    "api",
    "cli",
)

project(":factory").projectDir = file("domains/factory")
project(":economy").projectDir = file("domains/economy")
project(":finance").projectDir = file("domains/finance")

project(":api").projectDir = file("interfaces/api")
project(":cli").projectDir = file("interfaces/cli")
