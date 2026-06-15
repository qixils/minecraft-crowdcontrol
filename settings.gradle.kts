rootProject.name = "minecraft-crowd-control"

pluginManagement {
    repositories {
        exclusiveContent {
            forRepository {
                maven {
                    name = "Fabric"
                    url = uri("https://maven.fabricmc.net")
                }
            }
            filter {
                includeGroupAndSubgroups("net.fabricmc")
                includeGroup("fabric-loom")
            }
        }
        exclusiveContent {
            forRepository {
                maven {
                    name = "Forge"
                    url = uri("https://maven.minecraftforge.net")
                }
            }
            filter {
                includeGroupAndSubgroups("net.minecraftforge")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
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