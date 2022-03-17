val cloudVersion: String by project

description = "Minecraft Crowd Control: Sponge 8"

dependencies {
    implementation(project(":configurate-common"))
    implementation("cloud.commandframework:cloud-sponge:1.7.0-SNAPSHOT") // TODO use cloudVersion var
    compileOnly("org.spongepowered:spongeapi:8.0.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

// TODO: auto apply version to the plugin json
