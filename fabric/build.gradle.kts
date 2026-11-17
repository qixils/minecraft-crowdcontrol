import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import me.modmuss50.mpp.ReleaseType
import me.modmuss50.mpp.platforms.modrinth.ModrinthEnvironment
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

val adventureVersion = project.property("adventureVersion") as String
val adventurePlatformFabricVersion = project.property("adventurePlatformFabricVersion") as String
val modMenuVersion = project.property("modMenuVersion") as String
val configurateVersion = project.property("configurateVersion") as String
val luckoPermissionsApiVersion = project.property("luckoPermissionsApiVersion") as String
val crowdControlVersion = project.property("crowdControlVersion") as String
val javaWebSocketVersion = project.property("javaWebSocketVersion") as String
val geantyrefVersion = project.property("geantyrefVersion") as String
val yaclVersion = project.property("yaclVersion") as String
val minecraft_version = project.property("minecraft_version") as String
val fabric_loader_version = project.property("fabric_loader_version") as String
val fabric_version = project.property("fabric_version") as String
val mod_id = project.property("mod_id") as String

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

// Patches YACL to allow running on the snapshot; see the bundled notice for legal info
//
// loom resolves mod dependencies (and only nests module dependencies) during configuration,
// so the patched copy is published to a local maven repo;
// thus also necessitates downloading the original mod directly
val patchedYaclVersion = yaclVersion.replace("3.1.1+", "3.1.1.1+")
val patchedYaclRepo = layout.buildDirectory.dir("patched-yacl-repo").get().asFile
run {
    val notice = file("src/main/yacl-patch/NOTICE-crowdcontrol.md")
    val dir = File(patchedYaclRepo, "dev/qixils/crowdcontrol/patched/yacl/$patchedYaclVersion")
    val output = File(dir, "yacl-$patchedYaclVersion.jar")
    if (output.exists() && output.lastModified() > maxOf(notice.lastModified(), buildFile.lastModified()))
        return@run
    dir.mkdirs()
    val original = File(dir, "original.jar")
    uri("https://api.modrinth.com/maven/maven/modrinth/yacl/$yaclVersion/yacl-$yaclVersion.jar").toURL().openStream().use { input ->
        original.outputStream().use { input.copyTo(it) }
    }
    val sha1 = MessageDigest.getInstance("SHA-1").digest(original.readBytes()).joinToString("") { "%02x".format(it) }
    check(sha1 == "52a6c69a822c3592ed5f8b2285a2905d53a4c7aa") { "Unexpected checksum for YACL $yaclVersion: $sha1" }
    ZipFile(original).use { zip ->
        ZipOutputStream(output.outputStream()).use { out ->
            for (entry in zip.entries()) {
                if (entry.name.startsWith("META-INF/jars/yet-another-config-lib-fabric-")) continue
                out.putNextEntry(ZipEntry(entry.name))
                if (entry.name == "fabric.mod.json") {
                    @Suppress("UNCHECKED_CAST")
                    val metadata = JsonSlurper().parse(zip.getInputStream(entry)) as MutableMap<String, Any>
                    metadata["version"] = (metadata["version"] as String).replace("3.1.1+", "3.1.1.1+")
                    @Suppress("UNCHECKED_CAST")
                    val depends = metadata["depends"] as MutableMap<String, Any>
                    depends["minecraft"] = listOf(depends["minecraft"], "1.20-alpha.23.13.ab")
                    @Suppress("UNCHECKED_CAST")
                    metadata["jars"] = (metadata["jars"] as List<Map<String, String>>).filterNot { it["file"]!!.contains("yet-another-config-lib-fabric") }
                    metadata["description"] = "${metadata["description"]} Modified by Crowd Control to load on 23w13a_or_b; see NOTICE-crowdcontrol.md."
                    out.write(JsonOutput.prettyPrint(JsonOutput.toJson(metadata)).toByteArray())
                } else {
                    zip.getInputStream(entry).use { it.copyTo(out) }
                }
                out.closeEntry()
            }
            out.putNextEntry(ZipEntry(notice.name))
            notice.inputStream().use { it.copyTo(out) }
            out.closeEntry()
        }
    }
    original.delete()
    File(dir, "yacl-$patchedYaclVersion.pom").writeText("""
        <?xml version="1.0" encoding="UTF-8"?>
        <project xmlns="http://maven.apache.org/POM/4.0.0">
          <modelVersion>4.0.0</modelVersion>
          <groupId>dev.qixils.crowdcontrol.patched</groupId>
          <artifactId>yacl</artifactId>
          <version>$patchedYaclVersion</version>
        </project>
    """.trimIndent())
}
repositories {
    maven {
        name = "Patched YACL"
        url = uri(patchedYaclRepo)
        content { includeGroup("dev.qixils.crowdcontrol.patched") }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft_version")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:$fabric_loader_version")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabric_version")

    modImplementation(include("net.kyori:adventure-platform-fabric:$adventurePlatformFabricVersion")!!)
    modImplementation("com.terraformersmc:modmenu:$modMenuVersion")
    modImplementation(include("dev.qixils.crowdcontrol.patched:yacl:$patchedYaclVersion")!!)
    // non-transitive filters out fabric-api update
    modImplementation(include("me.lucko:fabric-permissions-api:$luckoPermissionsApiVersion") { isTransitive = false })
    implementation("org.java-websocket:Java-WebSocket:$javaWebSocketVersion")

    // transitives
    include(project(":base-common")) // this is available via api of mojmap-common (which is available via multiloader plugin) but not added to jar
    include("dev.qixils:cc4j-pubsub:$crowdControlVersion")
    include("org.spongepowered:configurate-core:$configurateVersion")
    include("org.spongepowered:configurate-hocon:$configurateVersion")
    include("net.kyori:adventure-serializer-configurate4:$adventureVersion")
    include("org.java-websocket:Java-WebSocket:$javaWebSocketVersion")
    include("io.leangen.geantyref:geantyref:$geantyrefVersion")
    include("net.kyori:option:1.1.0")
    // bundle newer apis to match platform-common
    listOf("api", "key", "text-serializer-gson", "text-serializer-json", "text-serializer-plain", "text-minimessage", "text-logger-slf4j").forEach {
        include("net.kyori:adventure-$it:$adventureVersion")
        localRuntime("net.kyori:adventure-$it:$adventureVersion")
    }

    // mojmap-common only exposes these at compile time, so they also need to be added to the dev runtime
    localRuntime(project(":base-common"))
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
    val versionFrom = "23w13a_or_b"
    val versionTo = "23w13a_or_b"
    val curseforgeVersion = "1.20-Snapshot"

    file.set(tasks.remapJar.get().archiveFile)
    modLoaders.add("fabric")
    modLoaders.add("quilt")
    type.set(ReleaseType.BETA)
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
        javaVersions.add(JavaVersion.VERSION_17)
        server.set(true)
        client.set(false)
        requires("fabric-api")
        optional("modmenu")
        embeds("yacl")
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
        optional("modmenu")
        embeds("yacl")
        embeds("adventure-platform-mod")
    }
}
