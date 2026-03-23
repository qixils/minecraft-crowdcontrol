import me.modmuss50.mpp.ReleaseType

val neoforgeVersion: String by project
val adventurePlatformModVersion: String by project
val clothConfigVersion: String by project
val cloudMojmapVersion: String by project
val configurateVersion: String by project
val luckPermsVersion: String by project
val jacksonVersion: String by project
val minecraft_version: String by project
val neoforge_version: String by project
val mod_id: String by project

val versionId = project.version.toString() + "+neoforge-$minecraft_version"

plugins {
    id("com.gradleup.shadow")
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
listOf("apiElements", "runtimeElements", "sourcesElements", "javadocElements").forEach { variant ->
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

// Files in this configuration will be bundled into your mod using the Shadow plugin.
// Don't use the `shadow` configuration from the plugin itself as it's meant for excluding files.
val shadowBundle: Configuration by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

configurations {
    implementation.get().extendsFrom(shadowBundle)
}

repositories {
    maven(url = "https://maven.su5ed.dev/releases")
}

dependencies {
    implementation(jarJar("net.kyori:adventure-platform-neoforge:$adventurePlatformModVersion")!!)
    implementation(jarJar("org.incendo:cloud-neoforge:$cloudMojmapVersion")!!)
    implementation(jarJar("me.shedaniel.cloth:cloth-config-neoforge:$clothConfigVersion")!!)
    compileOnly("net.luckperms:api:$luckPermsVersion")

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
        exclude(group = "com.fasterxml.jackson.annotation")
        exclude(group = "com.fasterxml.jackson.core")
        // TODO: jarjar geantryref, websockets
    }

    // jarInJar/include is not transitive so we have to do this cope instead
    shadowBundle("org.spongepowered:configurate-hocon:$configurateVersion") {
        exclude(group = "net.kyori")
    }
    shadowBundle("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    shadowBundle("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    shadowBundle("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation(jarJar("net.kyori:option:1.1.0")!!)
}

tasks.shadowJar {
    configurations = listOf(shadowBundle)

    exclude("org/slf4j/")
    exclude("io/leangen/geantyref/")
    exclude("net/kyori/option/")

    dependencies {
        exclude("org.slf4j::")
        exclude("io.leangen.geantyref::")
        exclude("net.kyori:option:")
    }
}

publishMods {
    val versionFrom = "26.1"
    val versionTo = "26.1"

    file.set(tasks.shadowJar.get().archiveFile)
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
        javaVersions.add(JavaVersion.VERSION_21)
        serverRequired.set(true)
        clientRequired.set(false)
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
            append(" (NeoForge ")
            append(versionFrom)
            if (versionFrom != versionTo) {
                append("-")
                append(versionTo)
            }
            append(")")
        })
        embeds("cloth-config", "adventure-platform-mod")
    }
}
