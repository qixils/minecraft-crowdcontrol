val cloudVersion: String by project
val minecraftVersion: String by project

val mcVersionSplit = minecraftVersion.split(".")
description = "Minecraft Crowd Control: Paper"

plugins {
    id("xyz.jpenilla.run-paper") // Adds runServer and runMojangMappedServer tasks for testing
    id("net.minecrell.plugin-yml.bukkit") // Generates plugin.yml
    id("io.papermc.paperweight.userdev") // Adds Paper-Server dependency
}

repositories {
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    implementation(project(":base-common"))
    implementation("cloud.commandframework:cloud-paper:${cloudVersion}")
    //compileOnly("io.papermc.paper:paper-api:$minecraftVersion-R0.1-SNAPSHOT")
    paperweight.foliaDevBundle("$minecraftVersion-R0.1-SNAPSHOT")
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

tasks.processResources {
    inputs.property("version", project.version)
    filteringCharset = "UTF-8"

    filesMatching("paper-plugin.yml") {
        expand("version" to project.version)
    }
}

// configure runServer task
tasks {
    assemble {
        dependsOn("reobfJar")
    }
    shadowJar {
        archiveBaseName.set("shadow-CrowdControl")
        archiveVersion.set("")
    }
    reobfJar {
        // set name of output file to CrowdControl-XYZ-VERSION.jar | TODO: reduce code repetition
        val titleCaseName = project.name[0].toUpperCase() + project.name.substring(1, project.name.indexOf("-platform"))
        outputJar.set(layout.buildDirectory.file("libs/CrowdControl-$titleCaseName-${project.version}.jar"))
    }
    runServer {
        minecraftVersion(minecraftVersion)
    }
}
