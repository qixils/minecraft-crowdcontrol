rootProject.name = "minecraft-crowd-control"

pluginManagement {
    repositories {
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        gradlePluginPortal()
    }
}

include(":common-platform")
include(":sponge7-platform")
include(":sponge8-platform")
include(":paper-platform")
include(":fabric-platform")
project(":common-platform").projectDir = file("common")
project(":sponge7-platform").projectDir = file("sponge-7")
project(":sponge8-platform").projectDir = file("sponge-8")
project(":paper-platform").projectDir = file("paper")
project(":fabric-platform").projectDir = file("fabric")