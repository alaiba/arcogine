plugins {
    application
}

// Last Light Foundry is an outward-facing game consumer: it depends on the supported Challenge,
// Factory and shared-type APIs, and nothing in those modules depends on it. It has no third-party
// runtime dependencies (the HTTP adapter is the JDK's built-in server and the browser client is
// bundled, build-free ES modules under src/main/resources/web).
dependencies {
    implementation(project(":types"))
    implementation(project(":factory"))
    implementation(project(":challenge"))
}

application {
    mainClass.set("com.arcogine.game.lastlightfoundry.Main")
}
