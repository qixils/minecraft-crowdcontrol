import me.modmuss50.mpp.ReleaseType
import me.modmuss50.mpp.platforms.modrinth.ModrinthEnvironment

val adventureVersion = project.property("adventureVersion") as String
val adventurePlatformModVersion = project.property("adventurePlatformModVersion") as String
val clothConfigVersion = project.property("clothConfigVersion") as String
val cloudMojmapVersion = project.property("cloudMojmapVersion") as String
val fabricVersion = project.property("fabricVersion") as String
val loaderVersion = project.property("loaderVersion") as String
val modMenuVersion = project.property("modMenuVersion") as String
val mojmapVersion = project.property("mojmapVersion") as String
val configurateVersion = project.property("configurateVersion") as String
val luckoPermissionsApiVersion = project.property("luckoPermissionsApiVersion") as String
val languageReloadVersion = project.property("languageReloadVersion") as String

val versionId = project.version.toString() + "+fabric-$mojmapVersion"

plugins {
    id("architectury-plugin")
    id("dev.architectury.loom")
    id("me.modmuss50.mod-publish-plugin")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

//// dependency configuration

// architectury
val common: Configuration = configurations.create("common") {
    isCanBeResolved = true
    isCanBeConsumed = false
}

// Files in this configuration will be bundled into your mod using the Shadow plugin.
// Don't use the `shadow` configuration from the plugin itself as it's meant for excluding files.
val shadowBundle: Configuration = configurations.create("shadowBundle") {
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

    shadowBundle(project(":base-common")) {
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
    shadowBundle(project(path = ":mojmap-common", configuration = "transformProductionFabric"))
    shadowBundle("org.spongepowered:configurate-hocon:$configurateVersion")
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraftVersion", mojmapVersion)
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand("version" to versionId)
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

publishMods {
    val versionFrom = "1.21.9"
    val versionTo = "1.21.10"

    file.set(tasks.remapJar.get().archiveFile)
    modLoaders.add("fabric")
    modLoaders.add("quilt")
    type.set(ReleaseType.STABLE)
    changelog.set(providers.fileContents(parent!!.layout.projectDirectory.file("CHANGELOG.md")).asText.map { it.split(Regex("## [\\d.]+")).getOrNull(1)?.trim() ?: "" })
    curseforge {
        accessToken.set(providers.environmentVariable("CURSEFORGE_API_KEY"))
        projectId.set("830331")
        minecraftVersionRange {
            start.set(versionFrom)
            end.set(versionTo)
        }
        version.set(versionId)
        displayName.set(buildString {
            append("[Fabric ")
            append(versionFrom)
            if (versionFrom != versionTo) {
                append("-")
                append(versionTo)
            }
            append("] v")
            append(project.version.toString())
        })
        javaVersions.add(JavaVersion.VERSION_21)
        server.set(true)
        client.set(false)
        requires("fabric-api")
        optional("modmenu", "language-reload")
        embeds("cloth-config")
    }
    modrinth {
        accessToken.set(providers.environmentVariable("MODRINTH_API_KEY"))
        projectId.set("6XhH9LqD")
        minecraftVersionRange {
            start.set(versionFrom)
            end.set(versionTo)
        }
        version.set(versionId.take(32))
        displayName.set(buildString {
            append("v")
            append(project.version.toString())
            append(" (Fabric ")
            append(versionFrom)
            if (versionFrom != versionTo) {
                append("-")
                append(versionTo)
            }
            append(")")
        })
        environment.set(ModrinthEnvironment.SERVER_ONLY_CLIENT_OPTIONAL)
        requires("fabric-api")
        optional("modmenu", "language-reload")
        embeds("cloth-config", "adventure-platform-mod")
    }
}
