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
