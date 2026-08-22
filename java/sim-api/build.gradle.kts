import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

plugins {
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
}

// Override the Spring Boot-managed Tomcat version to patch shipped CVEs
// (CVE-2026-41293/43512/43515 CRITICAL + 41284/42498/43513 HIGH).
extra["tomcat.version"] = "11.0.22"
// Override the Spring Boot-managed Spring Framework version to patch
// CVE-2026-41842/41845/41850 (HIGH).
extra["spring-framework.version"] = "7.0.8"

dependencies {
    implementation(project(":sim-types"))
    implementation(project(":sim-core"))
    implementation(project(":sim-factory"))
    implementation(project(":sim-economy"))
    implementation(project(":sim-agents"))
    implementation(project(":sim-finance"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    // io.spring.dependency-management only lets an explicit version win over
    // its BOM when declared in this project; a version pinned in sim-core
    // (a transitive project dependency) is silently overridden by the BOM's
    // managed version otherwise, so it must be repeated here.
    implementation("tools.jackson.core:jackson-databind:3.2.2")
    implementation("tools.jackson.core:jackson-core:3.2.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.22")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // Reactive client on the test classpath so the tests can build a
    // WebTestClient via WebTestClient.bindToServer() against the live
    // @SpringBootTest(RANDOM_PORT) servlet server (SB4 no longer
    // auto-configures a live-server WebTestClient bean).
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    // Enforces the module-boundary/capability guardrails in CONTRIBUTING.md as CI-checked
    // rules (see ArchitectureTest) rather than review discipline alone. sim-api is where all
    // domain modules are already visible, so it's the natural place for this scan to run from.
    testImplementation("com.tngtech.archunit:archunit-junit5:1.5.0")
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}

// Coverage gate: fails the build if sim-api line coverage drops below the
// floor. Set a few points below measured actual. See docs/java-rewrite-plan.md.
tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn(tasks.named("test"))
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = "0.70".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.named("jacocoTestCoverageVerification"))
}
