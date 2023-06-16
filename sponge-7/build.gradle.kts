description = "Minecraft Crowd Control: Sponge 7"

repositories {
    maven("https://repo.spongepowered.org/maven/")
    mavenCentral()
}

dependencies {
    implementation(project(":base-common"))
    implementation(libs.cloud.sponge7)
    implementation(libs.adventure.platform.spongeapi)
    compileOnly(libs.sponge7)
}

tasks.processResources {
    inputs.property("version", project.version)
    filteringCharset = "UTF-8"

    filesMatching("mcmod.info") {
        expand("version" to (project.version.toString() + "+sponge7"))
    }
}
