val cloudVersion: String by project

description = "Minecraft Crowd Control: Sponge 7"

repositories {
    maven("https://repo.spongepowered.org/maven/")
}

dependencies {
    implementation(project(":common-platform"))
    implementation("cloud.commandframework:cloud-sponge7:${cloudVersion}")
    implementation("net.kyori:adventure-platform-spongeapi:4.0.1")
    compileOnly("org.spongepowered:spongeapi:7.4.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
