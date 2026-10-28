dependencies {
    implementation(project(":types"))
    implementation(project(":simulation"))
    implementation(project(":governance"))
}

tasks.test {
    systemProperty("factory.test.classpath", sourceSets.test.get().runtimeClasspath.asPath)
}
