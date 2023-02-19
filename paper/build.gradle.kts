val cloudVersion: String by project
val paperMinecraftVersion: String by project
val adventurePlatformVersion: String by project

val mcVersionSplit = paperMinecraftVersion.split(".")
description = "Minecraft Crowd Control: Paper"

plugins {
    id("xyz.jpenilla.run-paper") // Adds runServer and runMojangMappedServer tasks for testing
    id("net.minecrell.plugin-yml.bukkit") // Generates plugin.yml
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    implementation(project(":base-common"))
    implementation("cloud.commandframework:cloud-paper:${cloudVersion}")
    compileOnly("org.spigotmc:spigot-api:$paperMinecraftVersion-R0.1-SNAPSHOT")
    implementation("net.kyori:adventure-platform-bukkit:$adventurePlatformVersion")
}

// Java 17 boilerplate

val targetJavaVersion = 17
tasks.withType<JavaCompile>().configureEach {
    options.release.set(targetJavaVersion)
}

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
//    withSourcesJar()
}

// plugin.yml generation
bukkit {
    name = "CrowdControl"
    version = project.version.toString()
    main = "dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin"
    apiVersion = mcVersionSplit[0] + "." + mcVersionSplit[1]
    prefix = "crowd-control"
    authors = listOf("qixils")
    description = "Allows viewers to interact with your Minecraft world"
    website = "https://github.com/qixils/minecraft-crowdcontrol"
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD
}

// configure runServer task
tasks {
    runServer {
        minecraftVersion(paperMinecraftVersion)
    }
}
