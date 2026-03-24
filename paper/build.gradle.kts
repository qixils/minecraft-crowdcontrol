import me.modmuss50.mpp.ReleaseType
import xyz.jpenilla.runpaper.task.RunServer

val minecraftVersion: String by project
val paperlibVersion: String by project
val configurateVersion: String by project
val luckPermsVersion: String by project

val mcVersionSplit = minecraftVersion.split(".")
val versionId = project.version.toString() + "+paper-" + minecraftVersion
version = "$minecraftVersion-${rootProject.version}"
description = "Minecraft Crowd Control: Paper"

plugins {
    id("xyz.jpenilla.run-paper") // Adds runServer and runMojangMappedServer tasks for testing
    id("de.eldoria.plugin-yml.bukkit") // Generates plugin.yml
    id("io.papermc.paperweight.userdev")
    id("me.modmuss50.mod-publish-plugin")
    id("com.gradleup.shadow")
}

dependencies {
    implementation(project(":base-common"))
    implementation("io.papermc:paperlib:$paperlibVersion")
    implementation("org.spongepowered:configurate-yaml:$configurateVersion")
    compileOnly("net.luckperms:api:$luckPermsVersion")

    paperweight.paperDevBundle("$minecraftVersion-R0.1-SNAPSHOT")
//    compileOnly("io.papermc.paper:paper-api:$minecraftVersion-R0.1-SNAPSHOT")
}

// plugin.yml generation
bukkit {
    name = "CrowdControl"
    version = versionId
    main = "dev.qixils.crowdcontrol.plugin.paper.PaperLoader"
    apiVersion = minecraftVersion
    prefix = "CrowdControl"
    authors = listOf("qixils")
    description = "The Ultimate Interactive Experience for Streamers"
    website = "https://github.com/qixils/minecraft-crowdcontrol"
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD

    foliaSupported = true

    softDepend = listOf("LuckPerms")
}

// configure runServer task
tasks {
    runServer {
        configure(minecraftVersion)
    }
    runPaper.folia.registerTask {
        configure(minecraftVersion)
    }
    // create extra runServer tasks for later versions of Minecraft
    for (mcVersion in listOf<String>("1.21.11")) {
        register("runServer$mcVersion", RunServer::class.java) {
            configure(mcVersion)
            dependsOn("shadowJar")
            pluginJars(shadowJar.get().archiveFile)
        }
    }

    shadowJar {
        // set name of output file to CrowdControl-PLATFORM+VERSION.jar
        archiveBaseName.set("CrowdControl-Paper+$version")
        archiveClassifier.set("")
        archiveVersion.set("")

        exclude("net/kyori/adventure/text/minimessage/")
        exclude("net/kyori/adventure/text/serializer/")

        dependencies {
            exclude("net.kyori.adventure.text.minimessage::")
            exclude("net.kyori.adventure.text.serializer.legacy::")
            exclude("net.kyori.adventure.text.serializer.plain::")
        }

        relocate("io.papermc.lib", "dev.qixils.relocated.paperlib")
        relocate("org.spongepowered.configurate", "dev.qixils.relocated.configurate")
        relocate("org.jetbrains.annotations", "dev.qixils.relocated.annotations") // TODO: can i just strip these they dont even really need to exist
        relocate("org.intellij.lang.annotations", "dev.qixils.relocated.annotations.alt")
        relocate("javassist", "dev.qixils.relocated.javassist")
        relocate("javax.annotation", "dev.qixils.relocated.javax.annotation")
        relocate("org.checkerframework", "dev.qixils.relocated.checkerframework")
    }
}

fun RunServer.configure(mcVersion: String) {
    minecraftVersion(mcVersion)
    runDirectory.set(layout.projectDirectory.dir("run/$mcVersion"))
    group = "run paper"
}

publishMods {
    val versionFrom = "1.21.9"
    val versionTo = "1.21.11"

    file.set(tasks.shadowJar.get().archiveFile)
    modLoaders.add("paper")
    modLoaders.add("folia")
    modLoaders.add("purpur")
    type.set(ReleaseType.STABLE)
    changelog.set(providers.fileContents(parent!!.layout.projectDirectory.file("CHANGELOG.md")).asText.map { it.split(Regex("## [\\d.]+")).getOrNull(1)?.trim() ?: "" })
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
            append(rootProject.version.toString())
            append(" (Paper ")
            append(versionFrom)
            if (versionFrom != versionTo) {
                append("-")
                append(versionTo)
            }
            append(")")
        })
    }
}
