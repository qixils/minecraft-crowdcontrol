val neoforgeVersion: String by project
val architecturyApiVersion: String by project
val mojmapVersion: String by project
val adventurePlatformModVersion: String by project
val clothConfigVersion: String by project
val cloudMojmapVersion: String by project
val fabricForgifiedVersion: String by project
val configurateVersion: String by project

plugins {
    id("com.github.johnrengelman.shadow")
    id("architectury-plugin")
    id("dev.architectury.loom")
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

//// dependency configuration

// architectury
val common: Configuration by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

// Files in this configuration will be bundled into your mod using the Shadow plugin.
// Don't use the `shadow` configuration from the plugin itself as it's meant for excluding files.
val shadowBundle: Configuration by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

configurations {
    compileClasspath.get().extendsFrom(common)
    runtimeClasspath.get().extendsFrom(common)
    implementation.get().extendsFrom(shadowBundle)
    getByName("developmentNeoForge").extendsFrom(common)
}

repositories {
    maven(url = "https://maven.su5ed.dev/releases")
}

dependencies {
    minecraft("net.minecraft:minecraft:$mojmapVersion")
    mappings(loom.officialMojangMappings())

    neoForge("net.neoforged:neoforge:$neoforgeVersion")

    modImplementation("org.sinytra.forgified-fabric-api:forgified-fabric-api:$fabricForgifiedVersion")
    modImplementation("dev.architectury:architectury-neoforge:$architecturyApiVersion")
    modImplementation(include("net.kyori:adventure-platform-neoforge:6.0.0")!!)
    modImplementation(include("org.incendo:cloud-neoforge:$cloudMojmapVersion")!!)
    modImplementation(include("me.shedaniel.cloth:cloth-config-neoforge:$clothConfigVersion")!!)

    shadowBundle(project(":configurate-common")) {
        exclude(group = "com.google.code.gson")
        exclude(group = "com.google.auto.service")
        exclude(group = "com.google.inject")
        exclude(group = "com.google.guava")
        exclude(group = "com.google.errorprone")
        exclude(group = "com.google.j2objc")
        exclude(group = "com.google.code.findbugs")
        exclude(group = "org.incendo", module = "cloud-core")
    }
    common(project(path = ":mojmap-common", configuration = "namedElements")) { isTransitive = false }
    shadowBundle(project(path = ":mojmap-common", configuration = "transformProductionNeoForge"))
    shadowBundle("org.spongepowered:configurate-hocon:$configurateVersion")
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraftVersion", mojmapVersion)
    filteringCharset = "UTF-8"

    filesMatching("META-INF/neoforge.mods.toml") {
        expand("version" to project.version.toString() + "+neoforge-$mojmapVersion")
    }
}

// Java 21 boilerplate

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

//loom {
//    mixin {
//        defaultRefmapName.set("crowd-control-refmap.json")
//    }
//    accessWidenerPath = project(":mojmap-common").projectDir.resolve("src/main/resources/crowdcontrol.accesswidener")
//}

// loom.neoForge {}

tasks.shadowJar {
    configurations = listOf(shadowBundle)
    archiveBaseName.set("shadow-CrowdControl")
    archiveVersion.set("")

    relocate("org.spongepowered.configurate", "dev.qixils.relocated.configurate")

    exclude("org/slf4j/")
    exclude("io/leangen/geantyref/")

    dependencies {
        exclude("org.slf4j::")
        exclude("io.leangen.geantyref::")
    }
}

tasks.remapJar {
    // configure remapJar to use output of shadowJar
    dependsOn(tasks.shadowJar)
    inputFile.set(tasks.shadowJar.get().archiveFile)
    archiveBaseName.set("CrowdControl-NeoForge+$mojmapVersion")
    archiveClassifier.set("")
}
