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
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-toml:2.18.2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // Reactive client on the test classpath so @SpringBootTest(RANDOM_PORT) can
    // auto-configure WebTestClient against the live servlet server.
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}
