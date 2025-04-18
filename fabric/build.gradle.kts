val adventureVersion: String by project
val adventurePlatformModVersion: String by project
val clothConfigVersion: String by project
val cloudMojmapVersion: String by project
val fabricVersion: String by project
val loaderVersion: String by project
val modMenuVersion: String by project
val mojmapVersion: String by project
val configurateVersion: String by project
val luckoPermissionsApiVersion: String by project
val languageReloadVersion: String by project

plugins {
    id("architectury-plugin")
    id("dev.architectury.loom")
}

architectury {
    platformSetupLoomIde()
    fabric()
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
    getByName("developmentFabric").extendsFrom(common)
}

repositories {
    maven {
        // todo: neoforge? idk i think it's deprecated
        name = "Ladysnake Mods"
        url = uri("https://maven.ladysnake.org/releases")
    }
    maven {
        name = "TerraformersMC"
        url = uri("https://maven.terraformersmc.com")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$mojmapVersion")
    mappings(loom.officialMojangMappings())

    modCompileOnly("net.fabricmc:fabric-loader:$loaderVersion")
    modCompileOnly("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
    modImplementation(include("net.kyori:adventure-platform-fabric:$adventurePlatformModVersion")!!)
    modImplementation(include("org.incendo:cloud-fabric:$cloudMojmapVersion")!!)
    modImplementation("com.terraformersmc:modmenu:$modMenuVersion")
    modImplementation(include("me.shedaniel.cloth:cloth-config-fabric:$clothConfigVersion") {
        exclude(group = "net.fabricmc.fabric-api")
    })
    modImplementation(include("me.lucko:fabric-permissions-api:$luckoPermissionsApiVersion")!!)
    modImplementation("maven.modrinth:language-reload:$languageReloadVersion")

    // misc includes
    include("net.kyori:adventure-api:$adventureVersion")

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
    common(project(path = ":mojmap-common", configuration = "namedElements")) // { isTransitive = false }
    shadowBundle(project(path = ":mojmap-common", configuration = "transformProductionNeoForge"))
    shadowBundle("org.spongepowered:configurate-hocon:$configurateVersion")
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraftVersion", mojmapVersion)
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand("version" to project.version.toString() + "+fabric-$mojmapVersion")
    }
}

// configure loom
loom {
//    mixin {
//        useLegacyMixinAp.set(true)
//        defaultRefmapName.set("crowd-control-refmap.json")
//    }
    accessWidenerPath = project(":mojmap-common").projectDir.resolve("src/main/resources/crowdcontrol.accesswidener")
}

// configure shadowJar
tasks.shadowJar {
    configurations = listOf(shadowBundle)
    archiveBaseName.set("shadow-CrowdControl")
    archiveVersion.set("")

    relocate("org.spongepowered.configurate", "dev.qixils.relocated.configurate")
}

tasks.remapJar {
    // configure remapJar to use output of shadowJar
    dependsOn(tasks.shadowJar)
    inputFile.set(tasks.shadowJar.get().archiveFile)
    archiveBaseName.set("CrowdControl-Fabric+$mojmapVersion")
    archiveClassifier.set("")
}
