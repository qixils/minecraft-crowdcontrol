val cloudVersion: String by project

description = "Minecraft Crowd Control: Sponge 8"

plugins {
    id("org.spongepowered.gradle.plugin") version "2.0.2"
}

dependencies {
    implementation(project(":configurate-common"))
    implementation("cloud.commandframework:cloud-sponge:1.8.0-SNAPSHOT") // TODO: use cloudVersion variable
    compileOnly("org.spongepowered:spongeapi:8.1.0")
}

tasks.withType<ProcessResources> {
    inputs.property("version", project.version)
    filteringCharset = "UTF-8"

    filesMatching("META-INF/sponge_plugins.json") {
        expand("version" to project.version)
    }
}

sponge {
    apiVersion("8.1.0")
    loader {
        name(org.spongepowered.gradle.plugin.config.PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    license("Mozilla Public License Version 2.0")
    plugin("crowd-control") {
        displayName("Crowd Control")
        version("3.3.0")
        entrypoint("dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin")
        description("Allows viewers to interact with your Minecraft world")
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
