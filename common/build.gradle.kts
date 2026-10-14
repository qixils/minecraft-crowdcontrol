val crowdControlVersion = project.property("crowdControlVersion") as String
val adventureVersion = project.property("adventureVersion") as String
val cloudVersion = project.property("cloudVersion") as String
val slf4jVersion = project.property("slf4jVersion") as String
val jacksonVersion = project.property("jacksonVersion") as String
val configurateVersion = project.property("configurateVersion") as String

dependencies {
    api("dev.qixils:cc4j-pubsub:$crowdControlVersion")
    api("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    api("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    api("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")

    compileOnly("net.kyori:adventure-api:$adventureVersion")
    api("net.kyori:adventure-text-minimessage:$adventureVersion") {
        exclude(group = "net.kyori", module = "adventure-api")
    }
    api("net.kyori:adventure-text-serializer-plain:$adventureVersion") {
        exclude(group = "net.kyori", module = "adventure-api")
    }
    api("net.kyori:adventure-text-serializer-legacy:$adventureVersion") {
        exclude(group = "net.kyori", module = "adventure-api")
    }
    api("dev.qixils.cloud:cloud-core:$cloudVersion")
    api("dev.qixils.cloud:cloud-minecraft-extras:$cloudVersion") {
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
