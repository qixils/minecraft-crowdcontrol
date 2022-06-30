val cloudVersion: String by project

description = "Minecraft Crowd Control: Sponge 8"

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
