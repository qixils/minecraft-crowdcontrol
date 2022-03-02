val adventureVersion: String by project
val cloudVersion: String by project

dependencies {
    api("com.google.guava:guava:31.1-jre")
    api("dev.qixils.crowdcontrol:crowd-control-receiver:3.3.4-SNAPSHOT")
    api("net.kyori:adventure-api:${adventureVersion}")
    api("net.kyori:adventure-text-serializer-plain:${adventureVersion}")
    api("net.kyori:adventure-text-serializer-legacy:${adventureVersion}")
    api("cloud.commandframework:cloud-core:${cloudVersion}")
    api("cloud.commandframework:cloud-minecraft-extras:${cloudVersion}")
}

description = "Minecraft Crowd Control: Common"
