val crowdControlVersion: String by project
val adventureVersion: String by project
val cloudVersion: String by project
val cloudExtrasVersion: String by project
val slf4jVersion: String by project

dependencies {
//    api("dev.qixils.crowdcontrol:crowd-control-receiver:$crowdControlVersion")
    api("live.crowdcontrol.cc4j:pubsub:1.0.0-SNAPSHOT")
//    api("com.google.code.findbugs:findbugs-annotations:3.0.1")

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
