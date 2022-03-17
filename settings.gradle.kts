rootProject.name = "minecraft-crowd-control"

pluginManagement {
    repositories {
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        maven {
            name = "Sponge Snapshots"
            url = uri("https://repo.spongepowered.org/repository/maven-public/")
        }
        gradlePluginPortal()
    }
}

// TODO re-enable Sponge 8 when Cloud 1.7.0 releases
include(":common-platform")
include(":mojmap-common")
include(":sponge7-platform")
//include(":sponge8-platform")
include(":paper-platform")
include(":fabric-platform")
project(":common-platform").projectDir = file("common")
project(":mojmap-common").projectDir = file("mojmap-common")
project(":sponge7-platform").projectDir = file("sponge-7")
//project(":sponge8-platform").projectDir = file("sponge-8")
project(":paper-platform").projectDir = file("paper")
project(":fabric-platform").projectDir = file("fabric")