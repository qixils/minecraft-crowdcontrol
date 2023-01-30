val crowdControlVersion: String by project
val adventureVersion: String by project
val cloudVersion: String by project

dependencies {
    api("com.google.guava:guava:31.1-jre")
    api("dev.qixils.crowdcontrol:crowd-control-receiver:$crowdControlVersion")
    api("net.kyori:adventure-api:$adventureVersion")
    api("net.kyori:adventure-text-serializer-plain:$adventureVersion")
    api("net.kyori:adventure-text-serializer-legacy:$adventureVersion")
    api("cloud.commandframework:cloud-core:$cloudVersion")
    api("cloud.commandframework:cloud-minecraft-extras:$cloudVersion")
}

description = "Minecraft Crowd Control: Common"

// generate mccc-version.txt resource file
val generatedResourcesFile = File(project.buildDir, "generated/resources/")
val versionFile = File(generatedResourcesFile, "mccc-version.txt")
tasks.register("generateVersionFile") {
    inputs.property("version", project.version)
    outputs.file(versionFile)
    doLast {
        generatedResourcesFile.mkdirs()
        versionFile.writeText(project.version.toString())
    }
}

sourceSets.main { resources.srcDir(File(project.buildDir, "generated/resources/")) }

tasks.processResources {
    dependsOn("generateVersionFile")
}
