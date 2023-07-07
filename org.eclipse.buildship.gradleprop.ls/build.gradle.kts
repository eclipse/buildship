plugins {
    application
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.5")
    implementation("org.slf4j:slf4j-simple:2.0.7")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.1")
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:0.21.0")
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j.jsonrpc:0.21.0")
    implementation("com.google.guava:guava:31.1-jre")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks.withType(Jar::class) {
    manifest {
        attributes["Main-Class"] = "org.eclipse.buildship.gradleprop.ls.GradlePropertiesLanguageServerLauncher";
    }
}

application {
    mainClass.set("org.eclipse.buildship.gradleprop.ls.GradlePropertiesLanguageServerLauncher")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.named<Jar>("shadowJar") {
    archiveFileName.set("language-server.jar")
}
