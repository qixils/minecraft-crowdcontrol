val cloudVersion: String by project

description = "Minecraft Crowd Control: Sponge 7"

repositories {
    maven("https://repo.spongepowered.org/maven/")
    mavenCentral()
}

dependencies {
    implementation(project(":base-common"))
    implementation("cloud.commandframework:cloud-sponge7:${cloudVersion}")
    implementation("net.kyori:adventure-platform-spongeapi:4.1.1")
    compileOnly("org.spongepowered:spongeapi:7.4.0")
}
