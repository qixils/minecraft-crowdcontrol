val crowdControlVersion = project.property("crowdControlVersion") as String
val adventureVersion = project.property("adventureVersion") as String
val slf4jVersion = project.property("slf4jVersion") as String
val gsonVersion = project.property("gsonVersion") as String
val configurateVersion = project.property("configurateVersion") as String
val brigadier_version = project.property("brigadier_version") as String

dependencies {
    api("dev.qixils:cc4j-pubsub:$crowdControlVersion") {
        // provided by minecraft and paper
        exclude(group = "com.google.code.gson")
        exclude(group = "org.slf4j")
    }

    compileOnly("com.mojang:brigadier:$brigadier_version")
    // provided by minecraft and paper
    compileOnly("com.google.code.gson:gson:$gsonVersion")

    compileOnly("net.kyori:adventure-api:$adventureVersion")
    api("net.kyori:adventure-text-minimessage:$adventureVersion") {
        exclude(group = "net.kyori", module = "adventure-api")
    }
    api("net.kyori:adventure-text-serializer-plain:$adventureVersion") {
        exclude(group = "net.kyori", module = "adventure-api")
    }
    api("org.slf4j:slf4j-api:$slf4jVersion")

    api("org.spongepowered:configurate-core:$configurateVersion")
    api("net.kyori:adventure-serializer-configurate4:$adventureVersion") {
        exclude(group = "net.kyori", module = "adventure-api")
    }
}

description = "Minecraft Crowd Control: Common"

// generate mccc-version.txt resource file
val resourcesPath = "generated/resources/"
val generatedResourcesFolder = project.layout.buildDirectory.dir(resourcesPath)
val versionFile = project.layout.buildDirectory.file(resourcesPath + "mccc-version.txt")
tasks.register("generateVersionFile") {
    description = "Generates the mccc-version.txt resource file"
    inputs.property("version", project.version)
    outputs.file(versionFile)
    doLast {
        generatedResourcesFolder.get().asFile.mkdirs()
        versionFile.get().asFile.writeText(project.version.toString())
    }
}
val applicationFile = project.layout.buildDirectory.file(resourcesPath + "mccc-application.txt")
tasks.register("generateApplicationFile") {
    description = "Generates the mccc-application.txt resource file"
    inputs.property("applicationId", findProperty("applicationId") ?: "")
    inputs.property("applicationSecret", findProperty("applicationSecret") ?: "")
    outputs.file(applicationFile)
    doLast {
        generatedResourcesFolder.get().asFile.mkdirs()
        applicationFile.get().asFile.writeText((findProperty("applicationId")?.toString() ?: "") + ':' + (findProperty("applicationSecret")?.toString() ?: ""))
    }
}

sourceSets.main { resources.srcDir(generatedResourcesFolder) }

tasks.processResources {
    dependsOn("generateVersionFile")
    dependsOn("generateApplicationFile")
}

// shaded jar is never consumed directly we don't need it
tasks.shadowJar {
    enabled = false
}
