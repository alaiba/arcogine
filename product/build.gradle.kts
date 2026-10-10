import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    java
    jacoco
    id("org.cyclonedx.bom") version "3.5.0"
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

    configure<org.gradle.api.plugins.quality.CheckstyleExtension> {
        toolVersion = "13.5.0"
        configDirectory.set(rootProject.layout.projectDirectory.dir("config/checkstyle"))
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        // Java 21 is the compatibility floor, not an exact compiler/JDK
        // requirement. Newer JDKs (for example the JDK 25 devcontainer) may
        // compile the project, while --release 21 prevents newer language,
        // API, or bytecode features from leaking into the build.
        options.release.set(21)
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
        testImplementation(platform("org.junit:junit-bom:6.1.3"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    // The research experiment substrate is non-shipped research infrastructure, never a production
    // dependency: no main source set may put it on its classpath. Test source sets may use it.
    // (architecture-conformance-test separately rejects any production reference to its package.)
    afterEvaluate {
        listOf("api", "implementation", "compileOnly", "compileOnlyApi", "runtimeOnly")
            .mapNotNull { configurations.findByName(it) }
            .forEach { configuration ->
                configuration.dependencies.withType<ProjectDependency>()
                    .filter { it.path == ":research-experiments" }
                    .forEach {
                        throw GradleException(
                            "${project.path} declares '${configuration.name}' on ${it.path}: research " +
                                "infrastructure must never be a production dependency",
                        )
                    }
            }
    }
}

// Every executable module has the same 90% LINE floor. Include execution data
// from all Java test suites in each module's report and gate, because downstream
// tests can prove upstream behavior. JaCoCo matches that data to this module's
// classes, so unrelated tests do not alter its coverage ratio.
gradle.projectsEvaluated {
    val allTests = subprojects.map { it.tasks.named<Test>("test") }
    val executionFiles = allTests.map { test ->
        test.map { it.extensions.getByType<JacocoTaskExtension>().destinationFile }
    }
    subprojects.forEach { module ->
        val main = module.extensions.getByType<SourceSetContainer>().getByName("main")
        if (!main.allJava.isEmpty) {
            val moduleTest = module.tasks.named<Test>("test")
            val verifyCoverageEvidence = module.tasks.register("verifyCoverageEvidence") {
                dependsOn(moduleTest)
                doLast {
                    check(!moduleTest.get().state.noSource) {
                        "${module.path} coverage gate requires test sources."
                    }
                    val executionData = moduleTest.get()
                        .extensions.getByType<JacocoTaskExtension>().destinationFile
                    check(executionData != null && executionData.isFile && executionData.length() > 0L) {
                        "${module.path} coverage gate requires JaCoCo execution data from its test task."
                    }
                }
            }
            module.tasks.named<JacocoReport>("jacocoTestReport") {
                dependsOn(allTests)
                executionData.setFrom(executionFiles)
                classDirectories.setFrom(main.output.classesDirs)
            }
            module.tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
                dependsOn(allTests, verifyCoverageEvidence)
                executionData.setFrom(executionFiles)
                classDirectories.setFrom(main.output.classesDirs)
                violationRules {
                    rule {
                        limit {
                            counter = "LINE"
                            minimum = "0.90".toBigDecimal()
                        }
                    }
                }
            }
            module.tasks.named("check") {
                dependsOn(module.tasks.named("jacocoTestCoverageVerification"))
            }
        }
    }
}
