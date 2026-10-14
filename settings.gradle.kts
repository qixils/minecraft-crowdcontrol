rootProject.name = "minecraft-crowd-control"

pluginManagement {
    repositories {
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        maven {
            name = "Architectury"
            url = uri("https://maven.architectury.dev/")
        }
        maven {
            name = "Forge"
            url =  uri("https://files.minecraftforge.net/maven/")
        }
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

fun incl(asPath: String, fromPath: String) {
    include(asPath)
    project(asPath).projectDir = file(fromPath)
}

incl(":base-common", "common")
incl(":paper-platform", "paper")
incl(":mojmap-common", "mojmap")
incl(":fabric-platform", "fabric")
incl(":neoforge-platform", "neoforge")