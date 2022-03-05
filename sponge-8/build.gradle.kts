val cloudVersion: String by project

dependencies {
    implementation("cloud.commandframework:cloud-sponge:${cloudVersion}")
    compileOnly("org.spongepowered:spongeapi:8.0.0")
}

description = "Minecraft Crowd Control: Sponge 8"
