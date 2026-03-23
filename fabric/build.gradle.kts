import me.modmuss50.mpp.ReleaseType

val adventureVersion: String by project
val adventurePlatformModVersion: String by project
val clothConfigVersion: String by project
val fabricVersion: String by project
val loaderVersion: String by project
val modMenuVersion: String by project
val configurateVersion: String by project
val luckoPermissionsApiVersion: String by project
val languageReloadVersion: String by project
val minecraft_version: String by project
val fabric_loader_version: String by project
val fabric_version: String by project
val mod_id: String by project

val versionId = project.version.toString() + "+fabric-$minecraft_version"

plugins {
    id("multiloader-loader")
    id("fabric-loom")
    id("me.modmuss50.mod-publish-plugin")
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

// Files in this configuration will be bundled into your mod using the Shadow plugin.
// Don't use the `shadow` configuration from the plugin itself as it's meant for excluding files.
val shadowBundle: Configuration by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

configurations {
    implementation.get().extendsFrom(shadowBundle)
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft_version")
    // TODO: to remove in 26.1
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:$fabric_loader_version")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabric_version")

    modImplementation(include("net.kyori:adventure-platform-fabric:$adventurePlatformModVersion")!!)
    modImplementation("com.terraformersmc:modmenu:$modMenuVersion")
    modImplementation(include("me.shedaniel.cloth:cloth-config-fabric:$clothConfigVersion") {
        exclude(group = "net.fabricmc.fabric-api")
    })
    modImplementation(include("me.lucko:fabric-permissions-api:$luckoPermissionsApiVersion")!!)
    modImplementation("maven.modrinth:language-reload:$languageReloadVersion")

    // misc includes
    include("net.kyori:adventure-api:$adventureVersion")

    // TODO: is this still needed? should we shadow everythimg manually? (probably)
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
    shadowBundle("org.spongepowered:configurate-hocon:$configurateVersion")
}

loom {
    val aw = file("src/main/resources/$mod_id.accesswidener")
    if (!aw.exists()) {
        throw RuntimeException("Could not find access widener")
    }
    accessWidenerPath.set(aw)

    mixin {
        defaultRefmapName.set("${mod_id}.refmap.json")
    }

    runs {
        named("client") {
            client()
            setConfigName("Fabric Client")
        }
        named("server") {
            server()
            setConfigName("Fabric Server")
        }
    }
}

val loaderAttribute = Attribute.of("io.github.mcgradleconventions.loader", String::class.java)
listOf("apiElements", "runtimeElements", "sourcesElements", "javadocElements", "includeInternal", "modCompileClasspath").forEach { variant ->
    configurations.named(variant) {
        attributes.attribute(loaderAttribute, "fabric")
    }
}
sourceSets.configureEach {
    listOf(compileClasspathConfigurationName, runtimeClasspathConfigurationName).forEach { variant ->
        configurations.named(variant) {
            attributes.attribute(loaderAttribute, "fabric")
        }
    }
}
// TODO: to remove in 26.1
loom.remapConfigurations.configureEach {
    configurations.named(name) {
        attributes.attribute(loaderAttribute, "fabric")
    }
}
// end todo

// TODO: is this still necessary?
//tasks.processResources {
//    inputs.property("version", project.version)
//    inputs.property("minecraftVersion", mojmapVersion)
//    filteringCharset = "UTF-8"
//
//    filesMatching("fabric.mod.json") {
//        expand("version" to versionId)
//    }
//}

// configure shadowJar
tasks.shadowJar {
    configurations = listOf(shadowBundle)
    relocate("org.spongepowered.configurate", "dev.qixils.relocated.configurate")
}

publishMods {
    val versionFrom = "26.1"
    val versionTo = "26.1"

    file.set(tasks.shadowJar.get().archiveFile)
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
        serverRequired.set(true)
        clientRequired.set(false)
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
        version.set(versionId)
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
        requires("fabric-api")
        optional("modmenu", "language-reload")
        embeds("cloth-config", "adventure-platform-mod")
    }
}
