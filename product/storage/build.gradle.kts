dependencies {
    implementation(project(":governance"))
    implementation(project(":types"))
    testImplementation(project(":factory"))
}

tasks.test {
    systemProperty("storage.test.classpath", sourceSets.test.get().runtimeClasspath.asPath)
}
