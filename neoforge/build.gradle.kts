import me.modmuss50.mpp.ReleaseType

val neoforgeVersion: String by project
val adventurePlatformModVersion: String by project
val configurateVersion: String by project
val luckPermsVersion: String by project
val jacksonVersion: String by project
val crowdControlVersion: String by project
val adventureVersion: String by project
val geantyrefVersion: String by project
val yaclVersion: String by project
val minecraft_version: String by project
val neoforge_version: String by project
val mod_id: String by project

val versionId = project.version.toString() + "+neoforge-$minecraft_version"

plugins {
    id("multiloader-loader")
    id("net.neoforged.moddev")
    id("me.modmuss50.mod-publish-plugin")
}

neoForge {
    version = neoforge_version
    // Automatically enable neoforge AccessTransformers if the file exists
    val at = project(":mojmap-common").file("src/main/resources/META-INF/accesstransformer.cfg")
    if (!at.exists()) {
        throw RuntimeException("Could not load access transformer")
    }
    accessTransformers.from(at.absolutePath)
//    parchment {
//        minecraftVersion.set(parchment_minecraft)
//        mappingsVersion.set(parchment_version)
//    }
    runs {
        configureEach {
            systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
            ideName = "NeoForge ${name.replaceFirstChar { it.uppercase() }} (${project.path})"
        }
        create("client") {
            client()
        }
        create("data") {
            clientData()
            // DataGen can be run by - "./gradlew :neoforge:runData" in Terminal.
            // Specify the modid for data generation, where to output the resulting resource, and where to look for existing resources.
            programArguments.addAll("--mod", mod_id, "--all", "--output", file("src/generated/resources/").absolutePath, "--existing", file("src/main/resources/").absolutePath)
        }
        create("server") {
            server()
        }
    }
    mods {
        create(mod_id) {
            sourceSet(sourceSets["main"])
        }
    }
}

sourceSets["main"].resources.srcDir("src/generated/resources")

val loaderAttribute = Attribute.of("io.github.mcgradleconventions.loader", String::class.java)
listOf("apiElements", "runtimeElements"/*, "sourcesElements", "javadocElements"*/).forEach { variant ->
    configurations.named(variant) {
        attributes.attribute(loaderAttribute, "neoforge")
    }
}
sourceSets.configureEach {
    listOf(compileClasspathConfigurationName, runtimeClasspathConfigurationName, getTaskName(null, "jarJar")).forEach { variant ->
        configurations.named(variant) {
            attributes.attribute(loaderAttribute, "neoforge")
        }
    }
}

//// dependency configuration

repositories {
    maven(url = "https://maven.su5ed.dev/releases")
}

dependencies {
    implementation(jarJar("net.kyori:adventure-platform-neoforge:$adventurePlatformModVersion")!!)
    implementation(jarJar("dev.isxander:yet-another-config-lib:$yaclVersion-neoforge")!!)
    compileOnly("net.luckperms:api:$luckPermsVersion")

    // add transitive deps
    jarJar(project(":base-common")) // this is available via api of mojmap-common (which is available via multiloader plugin) but not added to jar
    jarJar("dev.qixils:cc4j-pubsub:$crowdControlVersion")
    jarJar("org.spongepowered:configurate-core:$configurateVersion")
    jarJar("org.spongepowered:configurate-hocon:$configurateVersion")
    jarJar("net.kyori:adventure-serializer-configurate4:$adventureVersion")
    jarJar("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    jarJar("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    jarJar("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    jarJar("io.leangen.geantyref:geantyref:$geantyrefVersion")
    jarJar("net.kyori:option:1.1.0")
}

publishMods {
    val versionFrom = "26.1"
    val versionTo = "26.1.2"

    file.set(tasks.jar.get().archiveFile)
    modLoaders.add("neoforge")
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
            append("[NeoForge ")
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
        optional("yacl")
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
            append(" (NeoForge ")
            append(versionFrom)
            if (versionFrom != versionTo) {
                append("-")
                append(versionTo)
            }
            append(")")
        })
        optional("yacl")
        embeds("adventure-platform-mod")
    }
}
