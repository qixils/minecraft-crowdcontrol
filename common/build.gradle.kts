val crowdControlVersion: String by project
val adventureVersion: String by project
val adventurePlatformVersion: String by project
val cloudVersion: String by project

dependencies {
    api("dev.qixils.crowdcontrol:crowd-control-receiver:$crowdControlVersion")
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
    api("com.github.qixils.cloud:cloud-core:$cloudVersion")
    api("com.github.qixils.cloud:cloud-minecraft-extras:$cloudVersion") {
        exclude(group = "net.kyori", module = "adventure-api")
    }
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
