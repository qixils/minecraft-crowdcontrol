val cloudVersion: String by project
val minecraftVersion: String by project

val mcVersionSplit = minecraftVersion.split(".")
description = "Minecraft Crowd Control: Paper"

plugins {
    id("xyz.jpenilla.run-paper") // Adds runServer and runMojangMappedServer tasks for testing
    id("net.minecrell.plugin-yml.bukkit") // Generates plugin.yml
}

repositories {
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    implementation(project(":base-common"))
    implementation("cloud.commandframework:cloud-paper:${cloudVersion}")
    compileOnly("io.papermc.paper:paper-api:$minecraftVersion-R0.1-SNAPSHOT")
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
    prefix = "CrowdControl"
    authors = listOf("qixils")
    description = "Allows viewers to interact with your Minecraft world"
    website = "https://github.com/qixils/minecraft-crowdcontrol"
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD
}

// configure runServer task
tasks {
    runServer {
        minecraftVersion(minecraftVersion)
    }
}
