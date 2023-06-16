dependencies {
    api(libs.guava)
    api(libs.crowdcontrol.receiver)
    api(libs.adventure.api)
    api(libs.adventure.platform.api)
    api(libs.adventure.text.minimessage)
    api(libs.adventure.text.serializer.plain)
    api(libs.adventure.text.serializer.legacy)
    api(libs.cloud.core)
    api(libs.cloud.minecraft.extras)
    implementation(libs.reflections)
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
