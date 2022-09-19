val cloudVersion: String by project
val adventurePlatformVersion: String by project

description = "Minecraft Crowd Control: Sponge 7"

// inherit resources from common module
sourceSets.main { resources.srcDir(project(":base-common").sourceSets["main"].resources.srcDirs) }

repositories {
    maven("https://repo.spongepowered.org/maven/")
    mavenCentral()
}

dependencies {
    implementation(project(":base-common"))
    implementation("cloud.commandframework:cloud-sponge7:$cloudVersion")
    implementation("net.kyori:adventure-platform-spongeapi:$adventurePlatformVersion")
    compileOnly("org.spongepowered:spongeapi:7.4.0")
}
