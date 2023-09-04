val cloudVersion: String by project

description = "Minecraft Crowd Control: Sponge 8"

plugins {
    id("org.spongepowered.gradle.plugin")
}

repositories {
    maven {
        name = "Sponge"
        url = uri("https://repo.spongepowered.org/")
    }

    maven {
        name = "Jitpack"
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation(project(":configurate-common"))
    implementation("com.github.qixils.cloud:cloud-sponge:feature~sponge-SNAPSHOT")
    compileOnly("org.spongepowered:spongeapi:10.0.0")
    //compileOnly("org.spongepowered:sponge:1.16.5-8.0.0-SNAPSHOT:dev")
}

sponge {
    apiVersion("8.1.0")
    loader {
        name(org.spongepowered.gradle.plugin.config.PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    license("Mozilla Public License Version 2.0")
    plugin("crowdcontrol") {
        displayName("Crowd Control")
        version(project.version.toString() + "+sponge8")
        entrypoint("dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin")
        description("The Ultimate Interactive Experience for Streamers")
        links {
            homepage("https://github.com/qixils/minecraft-crowdcontrol")
            source("https://github.com/qixils/minecraft-crowdcontrol")
            issues("https://github.com/qixils/minecraft-crowdcontrol/issues")
        }
        contributor("qixils") {
            description("Lead Developer")
        }
        dependency("spongeapi") {
            loadOrder(org.spongepowered.plugin.metadata.model.PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
    }
}
