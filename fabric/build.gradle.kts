val crowdControlVersion: String by project
val minecraftVersion: String by project
val yarnVersion: String by project
val loaderVersion: String by project
val fabricVersion: String by project
val cloudVersion: String by project
val adventureVersion: String by project
val adventurePlatformFabricVersion: String by project
val cardinalComponentsVersion: String by project

val isMinecraftRelease = Regex("^\\d+\\.\\d+\\.\\d+$").matches(minecraftVersion)

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
    maven {
        name = "Parchment"
        url = uri("https://maven.parchmentmc.org")
    }
    mavenLocal()
}

dependencies {
    shade(project(":configurate-common"))
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings("net.fabricmc:yarn:$yarnVersion")
    modCompileOnly("net.fabricmc:fabric-loader:$loaderVersion")
    modCompileOnly("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
    modImplementation(include("net.kyori:adventure-platform-fabric:$adventurePlatformFabricVersion")!!)
    modImplementation(include("cloud.commandframework:cloud-fabric:$cloudVersion")!!)
    modApi(include("dev.onyxstudios.cardinal-components-api:cardinal-components-base:$cardinalComponentsVersion")!!)
    modImplementation(include("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:$cardinalComponentsVersion")!!)

    // misc includes
    include("net.kyori:adventure-api:$adventureVersion")
}

tasks.processResources {
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

// configure loom
loom {
    mixin {
        defaultRefmapName.set("crowd-control-refmap.json")
    }
}

// configure shadowJar
tasks.shadowJar {
    configurations = listOf(shade)
    archiveBaseName.set("shadow-CrowdControl")
    archiveVersion.set("")
    exclude("net/kyori/adventure/")

    dependencies {
        exclude("net.kyori:adventure-api:")
    }
}

tasks.remapJar {
    // configure remapJar to use output of shadowJar
    dependsOn(tasks.shadowJar)
    inputFile.set(project.buildDir.resolve("libs/shadow-CrowdControl.jar"))
    // set name of output file to CrowdControl-XYZ-VERSION.jar | TODO: reduce code repetition
    val titleCaseName = project.name[0].toUpperCase() + project.name.substring(1, project.name.indexOf("-platform"))
    archiveBaseName.set("CrowdControl-$titleCaseName")
    archiveClassifier.set("")
}