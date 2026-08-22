import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

plugins {
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
    application
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
    implementation(project(":sim-api"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("info.picocli:picocli:4.7.7")
    // io.spring.dependency-management only lets an explicit version win over
    // its BOM when declared in this project; a version pinned in sim-core
    // (a transitive project dependency) is silently overridden by the BOM's
    // managed version otherwise, so it must be repeated here.
    implementation("tools.jackson.core:jackson-databind:3.1.4")
    implementation("tools.jackson.core:jackson-core:3.1.4")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

application {
    mainClass = "com.arcogine.cli.ArcogineCommand"
}

tasks.bootJar {
    archiveFileName = "arcogine.jar"
    mainClass = "com.arcogine.cli.ArcogineCommand"
}

// Coverage gate: fails the build if sim-cli line coverage drops below the
// floor (e.g. if its ported tests are deleted). Mirrors the gate in
// sim-types. See docs/java-rewrite-plan.md.
tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn(tasks.named("test"))
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = "0.60".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.named("jacocoTestCoverageVerification"))
}
