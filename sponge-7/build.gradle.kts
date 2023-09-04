val cloudVersion: String by project
val adventurePlatformVersion: String by project

description = "Minecraft Crowd Control: Sponge 7"

repositories {
    maven("https://repo.spongepowered.org/maven/")
    mavenCentral()
}

dependencies {
    implementation(project(":base-common"))
    implementation("cloud.commandframework:cloud-sponge7:$cloudVersion")
    implementation("net.kyori:adventure-platform-spongeapi:$adventurePlatformVersion")
    compileOnly("org.spongepowered:spongeapi:10.0.0")
}

tasks.processResources {
    inputs.property("version", project.version)
    filteringCharset = "UTF-8"

    filesMatching("mcmod.info") {
        expand("version" to (project.version.toString() + "+sponge7"))
    }
}
