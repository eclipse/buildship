plugins {
    application
}

repositories {
    mavenCentral()
}

// the shadow plugin declares a custom project publication which we get via a custom libs dependency configuration
val libs by configurations.creating
dependencies {
    libs(project(":org.eclipse.buildship.gradleprop.languageserver", "shadow"))
}

// the shadow jar should be on the compile and on the runtime classpath
dependencies {
    implementation(files("libs/server.jar"))
}

// copies all dependencies from the libs configuration to a local directory
val copyShadowJar = tasks.register("copyShadowJar", Copy::class) {
    from(libs)
    into(layout.projectDirectory.dir("libs"))
}

tasks.named("compileJava") {
    mustRunAfter("copyShadowJar")
}

// automatically update the libs directory before compilation starts
tasks.named("processResources") {
    dependsOn(copyShadowJar)
}
