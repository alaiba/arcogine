plugins {
    java
    jacoco
}

allprojects {
    group = "com.arcogine"
    version = "0.1.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "jacoco")
    apply(plugin = "checkstyle")

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    configure<org.gradle.api.plugins.quality.CheckstyleExtension> {
        toolVersion = "13.5.0"
        configDirectory.set(rootProject.layout.projectDirectory.dir("config/checkstyle"))
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        // Apply strict linting to our own sources (main/test/jmh), but not to
        // JMH's machine-generated benchmark classes, whose warnings we can't fix.
        if (name != "jmhCompileGeneratedClasses") {
            options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror"))
        }
    }

    tasks.test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test)
        reports {
            xml.required = true
            html.required = true
        }
    }

    dependencies {
        testImplementation(platform("org.junit:junit-bom:5.11.4"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }
}
