val configurateVersion: String by project

description = "Minecraft Crowd Control: Common Implementation for Mojang Mappings"

plugins {
    java
    id("org.spongepowered.gradle.vanilla") version "0.2.1-SNAPSHOT"
}

minecraft {
    version("1.18.2")
    runs {
        server()
        client()
    }
}

dependencies {
    api(project(":configurate-common"))
    api("net.kyori:adventure-platform-api:4.1.0")
    api("org.spongepowered:mixin:0.8.5")
}

// Java 17 boilerplate

val targetJavaVersion = 17
tasks.withType<JavaCompile>().configureEach {
    options.release.set(targetJavaVersion)
}

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
//    withSourcesJar()
}
