val configurateVersion: String by project

description = "Minecraft Crowd Control: Common Mixins for Mojang Mappings"

plugins {
    java
    id("org.spongepowered.gradle.vanilla") version "0.2.1-SNAPSHOT"
}

minecraft {
    version("1.18.2")
    runs {
        server()
        client()
    }
}

dependencies {
    api(project(":configurate-common"))
    api("net.kyori:adventure-platform-api:4.1.0")
}
