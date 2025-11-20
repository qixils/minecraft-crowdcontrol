import me.modmuss50.mpp.ReleaseType

val neoforgeVersion: String by project
val mojmapVersion: String by project
val adventurePlatformModVersion: String by project
val clothConfigVersion: String by project
val cloudMojmapVersion: String by project
val configurateVersion: String by project
val luckPermsVersion: String by project

val versionId = project.version.toString() + "+neoforge-$mojmapVersion"

plugins {
    id("com.gradleup.shadow")
    id("architectury-plugin")
    id("dev.architectury.loom")
    id("me.modmuss50.mod-publish-plugin")
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

    modImplementation(include("net.kyori:adventure-platform-neoforge:$adventurePlatformModVersion")!!)
    modImplementation(include("org.incendo:cloud-neoforge:$cloudMojmapVersion")!!)
    modImplementation(include("me.shedaniel.cloth:cloth-config-neoforge:$clothConfigVersion")!!)
    modCompileOnly("net.luckperms:api:$luckPermsVersion")

    shadowBundle(project(":configurate-common")) {
        exclude(group = "com.google.code.gson")
        exclude(group = "com.google.auto.service")
        exclude(group = "com.google.inject")
        exclude(group = "com.google.guava")
        exclude(group = "com.google.errorprone")
        exclude(group = "com.google.j2objc")
        exclude(group = "com.google.code.findbugs")
        exclude(group = "org.incendo", module = "cloud-core")
        exclude(group = "com.fasterxml.jackson.annotation")
        exclude(group = "com.fasterxml.jackson.core") // TODO: jarjar? not sure where this is pulled in from
        // TODO: jarjar geantryref, websockets
    }
    common(project(path = ":mojmap-common", configuration = "namedElements")) { isTransitive = false }
    shadowBundle(project(path = ":mojmap-common", configuration = "transformProductionNeoForge"))

    // jarInJar/include is not transitive so we have to do this cope instead
    shadowBundle("org.spongepowered:configurate-hocon:$configurateVersion") {
        exclude(group = "net.kyori")
    }
    modImplementation(include("net.kyori:option:1.1.0")!!)
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraftVersion", mojmapVersion)
    filteringCharset = "UTF-8"

    filesMatching("META-INF/neoforge.mods.toml") {
        expand("version" to versionId)
    }
}

loom {
//    mixin {
//        useLegacyMixinAp.set(true)
//        defaultRefmapName.set("crowd-control-refmap.json")
//    }
//    accessWidenerPath = project(":mojmap-common").projectDir.resolve("src/main/resources/crowdcontrol.accesswidener")
}

// loom.neoForge {}

tasks.shadowJar {
    configurations = listOf(shadowBundle)
    archiveBaseName.set("shadow-CrowdControl")
    archiveVersion.set("")

    exclude("org/slf4j/")
    exclude("io/leangen/geantyref/")
    exclude("net/kyori/option/")

    dependencies {
        exclude("org.slf4j::")
        exclude("io.leangen.geantyref::")
        exclude("net.kyori:option:")
    }
}

tasks.remapJar {
    // configure remapJar to use output of shadowJar
    dependsOn(tasks.shadowJar)
    inputFile.set(tasks.shadowJar.get().archiveFile)
    archiveBaseName.set("CrowdControl-NeoForge+$mojmapVersion")
    archiveClassifier.set("")
}

publishMods {
    val versionFrom = "1.21.7"
    val versionTo = "1.21.7"

    file.set(tasks.remapJar.get().archiveFile)
    modLoaders.add("neoforge")
    type.set(ReleaseType.ALPHA)
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
