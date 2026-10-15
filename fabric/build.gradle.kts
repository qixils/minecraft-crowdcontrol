import me.modmuss50.mpp.ReleaseType
import me.modmuss50.mpp.platforms.modrinth.ModrinthEnvironment

val adventureVersion = project.property("adventureVersion") as String
val adventurePlatformModVersion = project.property("adventurePlatformModVersion") as String
val modMenuVersion = project.property("modMenuVersion") as String
val configurateVersion = project.property("configurateVersion") as String
val luckoPermissionsApiVersion = project.property("luckoPermissionsApiVersion") as String
val languageReloadVersion = project.property("languageReloadVersion") as String
val crowdControlVersion = project.property("crowdControlVersion") as String
val jacksonVersion = project.property("jacksonVersion") as String
val geantyrefVersion = project.property("geantyrefVersion") as String
val yaclVersion = project.property("yaclVersion") as String
val minecraft_version = project.property("minecraft_version") as String
val fabric_loader_version = project.property("fabric_loader_version") as String
val fabric_version = project.property("fabric_version") as String
val mod_id = project.property("mod_id") as String
val bundleJackson = (findProperty("bundleJackson") as String?)?.toBoolean() ?: true

val versionId = project.version.toString() + "+fabric-$minecraft_version"

plugins {
    id("multiloader-loader")
    id("net.fabricmc.fabric-loom-remap")
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

dependencies {
    minecraft("com.mojang:minecraft:$minecraft_version")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:$fabric_loader_version")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabric_version")

    modImplementation(include("net.kyori:adventure-platform-fabric:$adventurePlatformModVersion")!!)
    modImplementation("com.terraformersmc:modmenu:$modMenuVersion")
    modImplementation(include("me.lucko:fabric-permissions-api:$luckoPermissionsApiVersion")!!)
    modImplementation("maven.modrinth:language-reload:$languageReloadVersion")
    modImplementation("dev.isxander:yet-another-config-lib:$yaclVersion-fabric") {
        // TODO is this even in there
        exclude(group = "net.fabricmc.fabric-api")
    }

    // transitives
    include(project(":base-common")) // this is available via api of mojmap-common (which is available via multiloader plugin) but not added to jar
    include("dev.qixils:cc4j-pubsub:$crowdControlVersion")
    include("org.spongepowered:configurate-core:$configurateVersion")
    include("org.spongepowered:configurate-hocon:$configurateVersion")
    include("net.kyori:adventure-serializer-configurate4:$adventureVersion")
    if (bundleJackson) {
        include("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
        include("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
        include("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    }
    include("io.leangen.geantyref:geantyref:$geantyrefVersion")

    // mojmap-common only exposes these at compile time, so they also need to be added to the dev runtime
    localRuntime(project(":base-common")) {
        if (!bundleJackson) exclude(group = "com.fasterxml.jackson.core")
    }
    localRuntime("org.spongepowered:configurate-hocon:$configurateVersion")
}

loom {
    val aw = file("src/main/resources/$mod_id.accesswidener")
    if (!aw.exists()) {
        throw RuntimeException("Could not find access widener")
    }
    accessWidenerPath.set(aw)

    runs {
        named("client") {
            client()
            configName = "Fabric Client"
        }
        named("server") {
            server()
            configName = "Fabric Server"
        }
    }
}

val loaderAttribute = Attribute.of("io.github.mcgradleconventions.loader", String::class.java)
listOf("apiElements", "runtimeElements"/*, "sourcesElements", "javadocElements"*/, "includeInternal", "modCompileClasspath").forEach { variant ->
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
loom.remapConfigurations.configureEach {
    configurations.named(name) {
        attributes.attribute(loaderAttribute, "fabric")
    }
}

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

tasks.remapJar {
    // use the same file name as the dev jar (which is configured in the root project)
    archiveBaseName.set(tasks.jar.flatMap { it.archiveBaseName })
    archiveClassifier.set("")
    archiveVersion.set("")
}

publishMods {
    val versionFrom = "1.21.5"
    val versionTo = "1.21.5"
    val curseforgeVersion = ""

    file.set(tasks.remapJar.get().archiveFile)
    modLoaders.add("fabric")
    modLoaders.add("quilt")
    type.set(ReleaseType.STABLE)
    changelog.set(providers.fileContents(parent!!.layout.projectDirectory.file("CHANGELOG.md")).asText.map { it.split(Regex("## [\\d.]+")).getOrNull(1)?.trim() ?: "" })
    curseforge {
        accessToken.set(providers.environmentVariable("CURSEFORGE_API_KEY"))
        projectId.set("830331")
        if (curseforgeVersion.isNotEmpty()) {
            minecraftVersions.add(curseforgeVersion)
        } else if (versionFrom != versionTo) {
            minecraftVersionRange {
                start.set(versionFrom)
                end.set(versionTo)
            }
        } else {
            minecraftVersions.add(versionFrom)
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
        optional("modmenu", "language-reload", "yacl")
    }
    modrinth {
        accessToken.set(providers.environmentVariable("MODRINTH_API_KEY"))
        projectId.set("6XhH9LqD")
        if (versionFrom != versionTo) {
            minecraftVersionRange {
                start.set(versionFrom)
                end.set(versionTo)
            }
        } else {
            minecraftVersions.add(versionFrom)
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
        optional("modmenu", "language-reload", "yacl")
        embeds("adventure-platform-mod")
    }
}
