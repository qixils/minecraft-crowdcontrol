val configurateVersion: String by project

description = "Minecraft Crowd Control: Common Code for Configurate v4"

dependencies {
    api(project(":base-common"))
    api("org.spongepowered:configurate-hocon:$configurateVersion")
}

tasks.processResources {
    inputs.property("version", project.version)
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}
