val configurateVersion: String by project
val adventurePlatformModVersion: String by project
val adventureVersion: String by project
val adventurePlatformVersion: String by project
val yaclVersion: String by project
val neo_form_version: String by project

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

val commonJava by configurations.creating {
    isCanBeResolved = false
    isCanBeConsumed = true
}

val commonResources by configurations.creating {
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