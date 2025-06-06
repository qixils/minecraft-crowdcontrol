val crowdControlVersion: String by project
val adventureVersion: String by project
val cloudVersion: String by project
val cloudExtrasVersion: String by project
val slf4jVersion: String by project
val jacksonVersion: String by project

dependencies {
    api("dev.qixils.cc4j:pubsub:$crowdControlVersion")
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
    api("org.incendo:cloud-core:$cloudVersion")
    api("org.incendo:cloud-minecraft-extras:$cloudExtrasVersion") {
        exclude(group = "net.kyori", module = "adventure-api")
    }
    api("org.slf4j:slf4j-api:$slf4jVersion")
}

description = "Minecraft Crowd Control: Common"

// generate mccc-version.txt resource file
val generatedResourcesFolder = File(project.buildDir, "generated/resources/")
val versionFile = File(generatedResourcesFolder, "mccc-version.txt")
tasks.register("generateVersionFile") {
    inputs.property("version", project.version)
    outputs.file(versionFile)
    doLast {
        generatedResourcesFolder.mkdirs()
        versionFile.writeText(project.version.toString())
    }
}
val applicationFile = File(generatedResourcesFolder, "mccc-application.txt")
tasks.register("generateApplicationFile") {
    inputs.property("applicationId", findProperty("applicationId") ?: "")
    inputs.property("applicationSecret", findProperty("applicationSecret") ?: "")
    outputs.file(applicationFile)
    doLast {
        generatedResourcesFolder.mkdirs()
        applicationFile.writeText((findProperty("applicationId")?.toString() ?: "") + ':' + (findProperty("applicationSecret")?.toString() ?: ""))
    }
}

sourceSets.main { resources.srcDir(File(project.buildDir, "generated/resources/")) }

tasks.processResources {
    dependsOn("generateVersionFile")
    dependsOn("generateApplicationFile")
}
