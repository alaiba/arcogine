import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

plugins {
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
}

dependencies {
    implementation(project(":sim-types"))
    implementation(project(":sim-core"))
    implementation(project(":sim-factory"))
    implementation(project(":sim-economy"))
    implementation(project(":sim-agents"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    // Jackson 3 (tools.jackson) comes transitively via starter-web and sim-core;
    // no explicit Jackson dependency is needed here.

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // Reactive client on the test classpath so the tests can build a
    // WebTestClient via WebTestClient.bindToServer() against the live
    // @SpringBootTest(RANDOM_PORT) servlet server (SB4 no longer
    // auto-configures a live-server WebTestClient bean).
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
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
