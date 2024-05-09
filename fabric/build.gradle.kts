val crowdControlVersion: String by project
val minecraftVersion: String by project
val parchmentVersion: String by project
val loaderVersion: String by project
val fabricVersion: String by project
val cloudVersion: String by project
val adventureVersion: String by project
val adventurePlatformFabricVersion: String by project
val cardinalComponentsVersion: String by project
val modMenuVersion: String by project
val clothConfigVersion: String by project

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
        url = uri("https://maven.ladysnake.org/releases")
    }
    maven {
        name = "Parchment"
        url = uri("https://maven.parchmentmc.org")
    }
    maven {
        name = "TerraformersMC"
        url = uri("https://maven.terraformersmc.com")
    }
    maven {
        name = "Shedaniel"
        url = uri("https://maven.shedaniel.me")
    }
}

dependencies {
    shade(project(":configurate-common"))
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.officialMojangMappings())
    modCompileOnly("net.fabricmc:fabric-loader:$loaderVersion")
    modCompileOnly("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
    modImplementation(include("net.kyori:adventure-platform-fabric:$adventurePlatformFabricVersion")!!)
    modImplementation(include("cloud.commandframework:cloud-fabric:$cloudVersion")!!)
    modApi(include("org.ladysnake.cardinal-components-api:cardinal-components-base:$cardinalComponentsVersion")!!)
    modImplementation(include("org.ladysnake.cardinal-components-api:cardinal-components-entity:$cardinalComponentsVersion")!!)
    modImplementation("com.terraformersmc:modmenu:$modMenuVersion")
    modImplementation("me.shedaniel.cloth:cloth-config-fabric:$clothConfigVersion") {
        exclude(group = "net.fabricmc.fabric-api")
    }


    // misc includes
    include("net.kyori:adventure-api:$adventureVersion")
    include("me.shedaniel.cloth:cloth-config-fabric:$clothConfigVersion") {
        exclude(group = "net.fabricmc.fabric-api")
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraftVersion", minecraftVersion)
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand("version" to project.version.toString() + "+fabric-$minecraftVersion")
    }
}

// Java 17 boilerplate

val targetJavaVersion = 21
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
    accessWidenerPath = file("src/main/resources/crowdcontrol.accesswidener")
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
    archiveBaseName.set("CrowdControl-Fabric+$minecraftVersion")
    archiveClassifier.set("")
}