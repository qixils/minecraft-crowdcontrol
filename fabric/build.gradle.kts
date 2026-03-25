import me.modmuss50.mpp.ReleaseType

val adventureVersion: String by project
val adventurePlatformModVersion: String by project
val fabricVersion: String by project
val loaderVersion: String by project
val modMenuVersion: String by project
val configurateVersion: String by project
val luckoPermissionsApiVersion: String by project
val languageReloadVersion: String by project
val crowdControlVersion: String by project
val jacksonVersion: String by project
val geantyrefVersion: String by project
val minecraft_version: String by project
val fabric_loader_version: String by project
val fabric_version: String by project
val mod_id: String by project

val versionId = project.version.toString() + "+fabric-$minecraft_version"

plugins {
    id("multiloader-loader")
    id("net.fabricmc.fabric-loom")
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
    implementation("net.fabricmc:fabric-loader:$fabric_loader_version")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabric_version")

    implementation(include("net.kyori:adventure-platform-fabric:$adventurePlatformModVersion")!!)
    implementation("com.terraformersmc:modmenu:$modMenuVersion")
    implementation(include("me.lucko:fabric-permissions-api:$luckoPermissionsApiVersion")!!)
//    implementation("maven.modrinth:language-reload:$languageReloadVersion")

    // transitives
    include(project(":base-common")) // this is available via api of mojmap-common (which is available via multiloader plugin) but not added to jar
    include("dev.qixils:cc4j-pubsub:$crowdControlVersion")
    include("org.spongepowered:configurate-core:$configurateVersion")
    include("org.spongepowered:configurate-hocon:$configurateVersion")
    include("net.kyori:adventure-serializer-configurate4:$adventureVersion")
    include("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    include("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    include("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    include("io.leangen.geantyref:geantyref:$geantyrefVersion")
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

publishMods {
    val versionFrom = "26.1"
    val versionTo = "26.1"

    file.set(tasks.jar.get().archiveFile)
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
        javaVersions.add(JavaVersion.VERSION_25)
        serverRequired.set(true)
        clientRequired.set(false)
        requires("fabric-api")
        optional("modmenu", "language-reload")
//        embeds("yacl")
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
        embeds("adventure-platform-mod")
    }
}
