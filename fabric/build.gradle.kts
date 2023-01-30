val crowdControlVersion: String by project
val minecraftVersion: String by project
val parchmentVersion: String by project
val loaderVersion: String by project
val fabricVersion: String by project
val cloudVersion: String by project
val adventurePlatformFabricVersion: String by project
val cardinalComponentsVersion: String by project

plugins {
    id("fabric-loom")
}

// util to recursively include a project and all its dependencies
// TODO: it might be possible to get loom to directly include a shaded jar of a project
fun DependencyHandlerScope.includeRecursive(project: ProjectDependency): ProjectDependency {
    include(project)
    includeRecursive(project.dependencyProject.configurations.api.get())
    includeRecursive(project.dependencyProject.configurations.implementation.get())
    return project
}
fun DependencyHandlerScope.includeRecursive(configuration: Configuration) {
    configuration.dependencies.forEach {
        when (it) {
            is ProjectDependency -> includeRecursive(it)
            else -> include(it)
        }
    }
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
}

dependencies {
    implementation(includeRecursive(project(":configurate-common")))
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-$minecraftVersion:$parchmentVersion@zip")
    })
    modCompileOnly("net.fabricmc:fabric-loader:$loaderVersion")
    modCompileOnly("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
    modImplementation(include("net.kyori:adventure-platform-fabric:$adventurePlatformFabricVersion")!!)
    modImplementation(include("cloud.commandframework:cloud-fabric:$cloudVersion")!!)
    modApi(include("dev.onyxstudios.cardinal-components-api:cardinal-components-base:$cardinalComponentsVersion")!!)
    modImplementation(include("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:$cardinalComponentsVersion")!!)

    // misc includes
    include("dev.qixils.crowdcontrol:crowd-control-pojos:$crowdControlVersion")
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

// set name of the built jar | TODO: reduce code repetition from main build.gradle.kts
tasks.remapJar {
    val titleCaseName = project.name[0].toUpperCase() + project.name.substring(1, project.name.indexOf("-platform"))
    archiveBaseName.set("CrowdControl-$titleCaseName")
    archiveClassifier.set("")
}