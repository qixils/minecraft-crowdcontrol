import me.modmuss50.mpp.ReleaseType
import xyz.jpenilla.runpaper.task.RunServer

val minecraftVersion = project.property("minecraftVersion") as String
val paperlibVersion = project.property("paperlibVersion") as String
val configurateVersion = project.property("configurateVersion") as String
val luckPermsVersion = project.property("luckPermsVersion") as String
val paperBuild = project.property("paperBuild") as String

val mcVersionSplit = minecraftVersion.split(".")
val versionId = project.version.toString() + "+paper-" + minecraftVersion
description = "Minecraft Crowd Control: Paper"

plugins {
    id("xyz.jpenilla.run-paper") // Adds runServer and runMojangMappedServer tasks for testing
    id("de.eldoria.plugin-yml.bukkit") // Generates plugin.yml
    id("io.papermc.paperweight.userdev")
    id("me.modmuss50.mod-publish-plugin")
    id("io.papermc.hangar-publish-plugin")
    id("com.gradleup.shadow")
}

dependencies {
    implementation(project(":base-common"))
    implementation("io.papermc:paperlib:$paperlibVersion")
    implementation("org.spongepowered:configurate-yaml:$configurateVersion")
    compileOnly("net.luckperms:api:$luckPermsVersion")

    paperweight.paperDevBundle("$minecraftVersion.build.$paperBuild")
//    compileOnly("io.papermc.paper:paper-api:$minecraftVersion.build.+")
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
//    for (mcVersion in listOf<String>("1.21.11")) {
//        register("runServer$mcVersion", RunServer::class.java) {
//            configure(mcVersion)
//            dependsOn("shadowJar")
//            pluginJars(shadowJar.get().archiveFile)
//        }
//    }

    shadowJar {
        // set name of output file to CrowdControl-PLATFORM+VERSION.jar
        archiveFileName.set("CrowdControl-Paper+$minecraftVersion-$version.jar")

        exclude("net/kyori/adventure/text/minimessage/")
        exclude("net/kyori/adventure/text/serializer/")

        dependencies {
            exclude(dependency("net.kyori.adventure.text.minimessage::"))
            exclude(dependency("net.kyori.adventure.text.serializer.legacy::"))
            exclude(dependency("net.kyori.adventure.text.serializer.plain::"))
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

val changelogGenerator = providers.fileContents(parent!!.layout.projectDirectory.file("CHANGELOG.md")).asText.map { it.split(Regex("## [\\d.]+")).getOrNull(1)?.trim() ?: "" }

publishMods {
    val versionFrom = "26.1"
    val versionTo = "26.1.2"

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
                platformVersions = listOf("26.1", "26.1.1", "26.1.2")
                dependencies {
//                    hangar("TestProject") {
//                        required = false
//                    }
                }
            }
        }
    }
}
