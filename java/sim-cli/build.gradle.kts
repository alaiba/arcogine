plugins {
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
    application
}

dependencies {
    implementation(project(":sim-types"))
    implementation(project(":sim-core"))
    implementation(project(":sim-factory"))
    implementation(project(":sim-economy"))
    implementation(project(":sim-agents"))
    implementation(project(":sim-api"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("info.picocli:picocli:4.7.6")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

application {
    mainClass = "com.arcogine.cli.ArcogineCommand"
}

tasks.bootJar {
    archiveFileName = "arcogine.jar"
    mainClass = "com.arcogine.cli.ArcogineCommand"
}
