import xyz.jpenilla.runpaper.task.RunServer

val cloudVersion: String by project
val minecraftVersion: String by project
val paperlibVersion: String by project

val mcVersionSplit = minecraftVersion.split(".")
description = "Minecraft Crowd Control: Paper"

plugins {
    id("xyz.jpenilla.run-paper") // Adds runServer and runMojangMappedServer tasks for testing
    id("net.minecrell.plugin-yml.paper") // Generates plugin.yml
    //id("io.papermc.paperweight.userdev")
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":base-common"))
    implementation("com.github.qixils.cloud:cloud-paper:$cloudVersion")
    implementation("io.papermc:paperlib:$paperlibVersion")

    //paperweight.paperDevBundle("$minecraftVersion-R0.1-SNAPSHOT")
    implementation("io.papermc.paper:paper-api:$minecraftVersion-R0.1-SNAPSHOT")
}

// Java 17 boilerplate

val targetJavaVersion = 21
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
paper {
    name = "CrowdControl"
    version = project.version.toString() + "+paper"
    main = "dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin"
    apiVersion = minecraftVersion
    prefix = "CrowdControl"
    authors = listOf("qixils")
    description = "The Ultimate Interactive Experience for Streamers"
    website = "https://github.com/qixils/minecraft-crowdcontrol"
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD

    foliaSupported = true
}

// configure runServer task
tasks {
    runServer {
        configure(minecraftVersion)
    }
    runPaper.folia.registerTask {
        configure("1.20.6")
    }
    // create extra runServer tasks for later versions of Minecraft
    for (mcVersion in listOf<String>("1.21")) {
        register("runServer$mcVersion", RunServer::class.java) {
            configure(mcVersion)
            dependsOn("shadowJar")
            pluginJars(shadowJar.get().archiveFile)
        }
    }

    shadowJar {
        relocate("io.papermc.lib", "dev.qixils.relocated.paperlib")
    }
}

fun RunServer.configure(mcVersion: String) {
    minecraftVersion(mcVersion)
    runDirectory.set(layout.projectDirectory.dir("run/$mcVersion"))
    group = "run paper"
}
