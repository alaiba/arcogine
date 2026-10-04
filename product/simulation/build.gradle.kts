plugins {
    id("me.champeau.jmh") version "0.7.3"
}

dependencies {
    implementation(project(":types"))
}

// On-demand JMH scheduler microbenchmarks. Run with
// `./gradlew :simulation:jmh`. ASM is pinned explicitly for JMH bytecode
// generation; benchmark sources use the same Java 21 release compatibility
// as the rest of the build.
jmh {
    jmhVersion = "1.37"
    fork = 1
    warmupIterations = 2
    iterations = 3
    timeOnIteration = "500ms"
    warmup = "500ms"
}

configurations.named("jmh") {
    resolutionStrategy.force("org.ow2.asm:asm:9.10.1")
}
