import me.modmuss50.mpp.ReleaseType
import xyz.jpenilla.runpaper.task.RunServer

val cloudPaperVersion = project.property("cloudPaperVersion") as String
val minecraftVersion = project.property("minecraftVersion") as String
val paperlibVersion = project.property("paperlibVersion") as String
val configurateVersion = project.property("configurateVersion") as String
val luckPermsVersion = project.property("luckPermsVersion") as String

val mcVersionSplit = minecraftVersion.split(".")
val versionId = project.version.toString() + "+paper-" + minecraftVersion
description = "Minecraft Crowd Control: Paper"

plugins {
    id("xyz.jpenilla.run-paper") // Adds runServer and runMojangMappedServer tasks for testing
    id("de.eldoria.plugin-yml.bukkit") // Generates plugin.yml
    id("io.papermc.paperweight.userdev")
    id("me.modmuss50.mod-publish-plugin")
    id("io.papermc.hangar-publish-plugin")
}

dependencies {
    implementation(project(":base-common"))
    implementation("org.incendo:cloud-paper:$cloudPaperVersion")
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
    for (mcVersion in listOf<String>("1.21.4", "1.21.5")) {
        register("runServer$mcVersion", RunServer::class.java) {
            configure(mcVersion)
            dependsOn("shadowJar")
            pluginJars(shadowJar.get().archiveFile)
        }
    }

    shadowJar {
        archiveFileName.set("CrowdControl-Paper+$minecraftVersion-$version.jar")
        relocate("io.papermc.lib", "dev.qixils.relocated.paperlib")
        relocate("org.spongepowered.configurate", "dev.qixils.relocated.configurate")
    }
}

fun RunServer.configure(mcVersion: String) {
    minecraftVersion(mcVersion)
    runDirectory.set(layout.projectDirectory.dir("run/$mcVersion"))
    group = "run paper"
}

val changelogGenerator = providers.fileContents(parent!!.layout.projectDirectory.file("CHANGELOG.md")).asText.map { it.split(Regex("## [\\d.]+")).getOrNull(1)?.trim() ?: "" }

publishMods {
    val versionFrom = "1.21.2"
    val versionTo = "1.21.8"

    // TODO: update `file.set` to match hangar? i guess that's for javadocs? i dont think those are enabled atm anyway
    file.set(tasks.shadowJar.get().archiveFile)
    modLoaders.add("paper")
    modLoaders.add("folia")
    modLoaders.add("purpur")
    type.set(ReleaseType.STABLE)
    changelog.set(changelogGenerator)
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

val semVerMatcher = Regex("^[0-9.]+")

hangarPublish {
    publications.register("plugin") {

        version.set(providers.provider { (semVerMatcher.find(project.version as String)!!.value + "+" + semVerMatcher.find(minecraftVersion)!!.value).take(15) })
        id.set("CrowdControl")
        channel.set("Release") // or PreRelease
        changelog.set(changelogGenerator)
        apiKey.set(providers.environmentVariable("HANGAR_KEY"))

        // register platforms
        platforms {
            paper {
                jar = tasks.shadowJar.flatMap { it.archiveFile }
                platformVersions = listOf("1.21.2", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8")
                dependencies {
//                    hangar("TestProject") {
//                        required = false
//                    }
                }
            }
        }
    }
}
