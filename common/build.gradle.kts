import dev.qixils.crowdcontrol.gradle.BucketUpload
import java.security.MessageDigest
import java.util.HexFormat
import java.util.Properties

val crowdControlVersion: String by project
val adventureVersion: String by project
val slf4jVersion: String by project
val jacksonVersion: String by project
val configurateVersion: String by project
val brigadier_version: String by project

dependencies {
    api("dev.qixils:cc4j-pubsub:$crowdControlVersion")
    api("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    api("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    api("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")

    compileOnly("com.mojang:brigadier:$brigadier_version")

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

sourceSets.main { resources.srcDir(generatedResourcesFolder) }

tasks.processResources {
    dependsOn("generateVersionFile")
    dependsOn("generateApplicationFile")
}

/* --------------------------- */
/* screen effect resource pack */
/* --------------------------- */

// build the screen effect resource pack offered to players without the mod
// the plugin only ships its hash and download link, and `release` uploads it to that link
val resourcePackFormat = providers.gradleProperty("resource_pack_format").get()
val shaderPackBaseUrl = providers.gradleProperty("shader_pack_url").getOrElse("").trim()
    .let { if (it.isEmpty() || it.endsWith("/")) it else "$it/" }

val shaderPack = sourceSets.create("shaderPack") {
    val main = sourceSets.main.get()
    // the raw resources are used because the processed ones would depend on the pack's hash
    val mainClasspath = main.output.classesDirs + main.compileClasspath
    compileClasspath += mainClasspath
    runtimeClasspath += mainClasspath + files("src/main/resources") + files(generatedResourcesFolder).builtBy("generateVersionFile", "generateApplicationFile")
}

val shaderPackFile = layout.buildDirectory.file("shaderPack/crowdcontrol-shaders.zip")
val buildShaderPack = tasks.register<JavaExec>("buildShaderPack") {
    group = "build"
    description = "Builds the screen effect resource pack offered to players without the (latest) mod"
    classpath = shaderPack.runtimeClasspath
    mainClass = "dev.qixils.crowdcontrol.common.shader.ShaderPackBuilder"
    inputs.property("resourcePackFormat", resourcePackFormat)
    outputs.file(shaderPackFile)
    args(shaderPackFile.get().asFile.absolutePath, resourcePackFormat)
}

val shaderPackInfoFolder = layout.buildDirectory.dir("generated/shaderPackInfo")
val generateShaderPackInfo = tasks.register("generateShaderPackInfo") {
    inputs.files(buildShaderPack)
    inputs.property("baseUrl", shaderPackBaseUrl)
    outputs.dir(shaderPackInfoFolder)
    doLast {
        val hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(shaderPackFile.get().asFile.readBytes()))
        // the hash is in the name so that every build gets its own never-changing URL
        val url = if (shaderPackBaseUrl.isEmpty()) "" else "${shaderPackBaseUrl}crowdcontrol-shaders-$hash.zip"
        // read by dev.qixils.crowdcontrol.common.shader.ShaderPack
        shaderPackInfoFolder.get().file("mccc-shader-pack.properties").asFile.writeText("hash=$hash\nurl=$url\n")
    }
}

// kept out of the resource folders so that the platforms only get it through this module's jar
tasks.processResources { from(generateShaderPackInfo) }

tasks.register<BucketUpload>("uploadShaderPack") {
    group = "publishing"
    description = "Uploads the screen effect resource pack to where this build links to it"
    // the pack is built without the game around, so make sure it was built for the right version
    dependsOn(":mojmap-common:verifyResourcePackFormat")
    dependsOn(generateShaderPackInfo)
    file = shaderPackFile
    publicUrl = generateShaderPackInfo.map { task ->
        val info = Properties()
        task.outputs.files.singleFile.resolve("mccc-shader-pack.properties").reader().use { info.load(it) }
        info.getProperty("url")
    }
    contentType = "application/zip"
    endpoint = providers.environmentVariable("S3_ENDPOINT")
    bucket = providers.gradleProperty("shader_pack_bucket")
    region = providers.gradleProperty("shader_pack_region")
    accessKeyId = providers.environmentVariable("S3_ACCESS_KEY_ID")
    secretAccessKey = providers.environmentVariable("S3_SECRET_ACCESS_KEY")
}
