import xyz.jpenilla.runpaper.task.RunServer

val minecraftVersion = libs.versions.minecraft.paper.get()
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
    implementation(libs.cloud.paper)
    compileOnly("io.papermc.paper:paper-api:${libs.versions.minecraft.paper}-R0.1-SNAPSHOT")
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
    version = project.version.toString() + "+paper"
    main = "dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin"
    apiVersion = mcVersionSplit[0] + "." + mcVersionSplit[1]
    prefix = "CrowdControl"
    authors = listOf("qixils")
    description = "The Ultimate Interactive Experience for Streamers"
    website = "https://github.com/qixils/minecraft-crowdcontrol"
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD
}

// configure runServer task
tasks {
    runServer {
        configure(minecraftVersion)
    }
    // create extra runServer tasks for later versions of Minecraft
    for (mcVersion in listOf("1.20", "1.20.1")) {
        register("runServer$mcVersion", RunServer::class.java) {
            configure(mcVersion)
            dependsOn("shadowJar")
            pluginJars(shadowJar.get().archiveFile)
        }
    }
}

fun RunServer.configure(mcVersion: String) {
    minecraftVersion(mcVersion)
    runDirectory.set(layout.projectDirectory.dir("run/$mcVersion"))
    group = "run paper"
}
