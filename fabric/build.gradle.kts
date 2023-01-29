import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val minecraftVersion: String by project
val loaderVersion: String by project
val fabricVersion: String by project
val cloudVersion: String by project
val adventurePlatformFabricVersion: String by project
val cardinalComponentsVersion: String by project

// inherit resources from common module
sourceSets.main { resources.srcDir(project(":base-common").sourceSets["main"].resources.srcDirs) }

// shading configuration
val shade: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

plugins {
    id("fabric-loom")
}

repositories {
    maven {
        name = "Sonatype Snapshots"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
    maven {
        name = "Ladysnake Mods"
        url = uri("https://ladysnake.jfrog.io/artifactory/mods")
    }
}

dependencies {
    shade(project(":configurate-common"))
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.officialMojangMappings())
    modCompileOnly("net.fabricmc:fabric-loader:$loaderVersion")
    modCompileOnly("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
    modImplementation("net.kyori:adventure-platform-fabric:$adventurePlatformFabricVersion")
    modImplementation("cloud.commandframework:cloud-fabric:$cloudVersion")
    modApi("dev.onyxstudios.cardinal-components-api:cardinal-components-base:$cardinalComponentsVersion")
    modImplementation("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:$cardinalComponentsVersion")
}

tasks.withType<ShadowJar> {
    // manually exclude some misc. dependencies
    configurations = listOf(project.configurations.modImplementation.get(), project.configurations.modApi.get(), shade)
    exclude("net/fabricmc/")
    exclude("fabric-*-v*")
    exclude("client-fabric-*-v*")
    exclude("LICENSE-fabric-*")

    dependencies {
        exclude(dependency("net.fabricmc::"))
    }
}

tasks.withType<ProcessResources> {
    inputs.property("version", project.version)
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
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
