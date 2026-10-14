import java.util.zip.ZipFile

val configurateVersion = project.property("configurateVersion") as String
val adventurePlatformModVersion = project.property("adventurePlatformModVersion") as String
val adventureVersion = project.property("adventureVersion") as String
val yaclVersion = project.property("yaclVersion") as String
val neo_form_version = project.property("neo_form_version") as String

plugins {
    id("multiloader-common")
    id("net.neoforged.moddev")
}

neoForge {
    neoFormVersion = neo_form_version
    // Automatically enable AccessTransformers if the file exists
    val at = file("src/main/resources/META-INF/accesstransformer.cfg")
    if (at.exists()) {
        accessTransformers.from(at.absolutePath)
    }
//    parchment {
//        minecraftVersion = parchment_minecraft
//        mappingsVersion = parchment_version
//    }
}

dependencies {
    // multiloader common
    compileOnly("org.spongepowered:mixin:0.8.5")
    compileOnly("io.github.llamalad7:mixinextras-common:0.5.3")
    annotationProcessor("io.github.llamalad7:mixinextras-common:0.5.3")
    compileOnly("org.ow2.asm:asm:9.9.1")

    //
    api(project(":base-common"))
    api("org.spongepowered:configurate-hocon:$configurateVersion")

    compileOnly("net.kyori:adventure-platform-mod-shared:$adventurePlatformModVersion")
    compileOnly("dev.isxander:yet-another-config-lib:$yaclVersion-neoforge")
}

val commonJava = configurations.create("commonJava") {
    isCanBeResolved = false
    isCanBeConsumed = true
}

val commonResources = configurations.create("commonResources") {
    isCanBeResolved = false
    isCanBeConsumed = true
}

artifacts {
    add("commonJava", sourceSets["main"].java.srcDirs.single())
    add("commonResources", sourceSets["main"].resources.srcDirs.single())
}

val loaderAttribute = Attribute.of("io.github.mcgradleconventions.loader", String::class.java)
listOf("apiElements", "runtimeElements"/*, "sourcesElements", "javadocElements"*/).forEach { variant ->
    configurations.named(variant) {
        attributes.attribute(loaderAttribute, "common")
    }
}

sourceSets.configureEach {
    listOf(compileClasspathConfigurationName, runtimeClasspathConfigurationName).forEach { variant ->
        configurations.named(variant) {
            attributes.attribute(loaderAttribute, "common")
        }
    }
}

// the screen effect resource pack is built without the game around, so check that it declares the right format
val verifyResourcePackFormat = tasks.register("verifyResourcePackFormat") {
    group = "verification"
    description = "Checks resource_pack_format in gradle.properties against the game being built against"
    val expected = providers.gradleProperty("resource_pack_format").get()
    val gameVersion = providers.gradleProperty("minecraft_version").get()
    val classpath: FileCollection = configurations.compileClasspath.get()
    inputs.files(classpath)
    doLast {
        val versionJson = classpath.asSequence()
            .filter { it.isFile && it.extension == "jar" }
            .mapNotNull { jar -> ZipFile(jar).use { zip -> zip.getEntry("version.json")?.let { zip.getInputStream(it).reader().readText() } } }
            .firstOrNull { "pack_version" in it }
            ?: throw GradleException("Could not find Minecraft's version.json on the classpath")
        val actual = Regex("\"resource_major\"\\s*:\\s*(\\d+)").find(versionJson)?.groupValues?.get(1)
            ?: throw GradleException("Could not find the resource pack format in Minecraft's version.json")
        if (actual != expected)
            throw GradleException("resource_pack_format in gradle.properties is $expected, but Minecraft $gameVersion uses $actual; please update it")
    }
}
tasks.check { dependsOn(verifyResourcePackFormat) }
