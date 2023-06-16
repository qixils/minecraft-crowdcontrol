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
    minecraft(libs.minecraft.fabric)
    mappings(loom.officialMojangMappings())
    modCompileOnly(libs.fabric.loader)
    modCompileOnly(libs.fabric.api)

    include(libs.adventure.platform.fabric)
    modImplementation(libs.adventure.platform.fabric)

    include(libs.cloud.fabric)
    modImplementation(libs.cloud.fabric)

    include(libs.cardinalcomponents.base)
    modApi(libs.cardinalcomponents.base)

    include(libs.cardinalcomponents.entity)
    modImplementation(libs.cardinalcomponents.entity)

    modImplementation(libs.modmenu)
    modImplementation(libs.clothconfig.fabric) {
        exclude(group = "net.fabricmc.fabric-api")
    }


    // misc includes
    include(libs.adventure.api)
    include(libs.clothconfig.fabric) {
        exclude(group = "net.fabricmc.fabric-api")
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraftVersion", libs.versions.minecraft.fabric)
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand("version" to project.version.toString() + "+fabric-${libs.versions.minecraft.fabric.get()}")
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
    inputs.property("minecraftVersion", libs.versions.minecraft.fabric)
    // configure remapJar to use output of shadowJar
    dependsOn(tasks.shadowJar)
    inputFile.set(project.buildDir.resolve("libs/shadow-CrowdControl.jar"))
    archiveBaseName.set("CrowdControl-Fabric+${libs.versions.minecraft.fabric.get()}")
    archiveClassifier.set("")
}